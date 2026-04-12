package com.crawler.core;

import com.crawler.model.CrawledPage;
import com.crawler.model.CrawlerConfig;
import com.crawler.storage.CsvStorage;
import com.crawler.storage.JsonStorage;
import com.crawler.storage.SqliteStorage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CrawlerEngine {

    private final CrawlerConfig config;
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final BlockingQueue<UrlTask> queue = new LinkedBlockingQueue<>();
    private final List<CrawledPage> results = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger pageCount = new AtomicInteger(0);
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    private Consumer<CrawledPage> onPageCrawled;
    private CsvStorage csvStorage;
    private JsonStorage jsonStorage;
    private SqliteStorage sqliteStorage;

    public CrawlerEngine(CrawlerConfig config) { this.config = config; }

    public void setOnPageCrawled(Consumer<CrawledPage> cb) { this.onPageCrawled = cb; }
    public void stop() { stopped.set(true); }

    public List<CrawledPage> start() throws Exception {
        stopped.set(false);
        long startTime = System.currentTimeMillis(); // Start measuring total time

        if (config.isSaveToCSV())    csvStorage    = new CsvStorage(config.getOutputDir());
        if (config.isSaveToJSON())   jsonStorage   = new JsonStorage(config.getOutputDir());
        if (config.isSaveToSQLite()) sqliteStorage = new SqliteStorage(config.getOutputDir());

        queue.add(new UrlTask(config.getSeedUrl(), 0));
        visitedUrls.add(config.getSeedUrl());

        System.out.println("Starting crawl with " + config.getThreadCount() + " threads...");

        if (config.getThreadCount() == 1) {
            runSequential();
        } else {
            runParallel();
        }

        if (config.isSaveToJSON())   jsonStorage.finalize(results);
        if (config.isSaveToSQLite()) sqliteStorage.close();

        long endTime = System.currentTimeMillis();
        System.out.println("=========================================");
        System.out.println("Total Execution Time: " + (endTime - startTime) + " ms");
        System.out.println("=========================================");

        return results;
    }

    private void runSequential() {
        while (!stopped.get() && pageCount.get() < config.getMaxPages() && !queue.isEmpty()) {
            UrlTask task = queue.poll();
            if (task != null) {
                CrawledPage page = crawlPageWithRetry(task, 0);
                if (page != null) {
                    processResult(page);
                }
            }
        }
    }

    private void runParallel() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(config.getThreadCount());

        while (!stopped.get() && pageCount.get() < config.getMaxPages()) {
            List<UrlTask> batch = new ArrayList<>();
            queue.drainTo(batch, config.getThreadCount() * 2);

            if (batch.isEmpty()) {
                Thread.sleep(200);
                if (queue.isEmpty()) break;
                continue;
            }

            List<Future<CrawledPage>> futures = new ArrayList<>();
            for (UrlTask task : batch) {
                if (pageCount.get() >= config.getMaxPages() || stopped.get()) break;
                futures.add(executor.submit(() -> crawlPageWithRetry(task, 0)));
            }

            for (Future<CrawledPage> f : futures) {
                try {
                    CrawledPage page = f.get(config.getTimeoutMs() + 2000, TimeUnit.MILLISECONDS);
                    if (page != null) {
                        processResult(page);
                    }
                } catch (ExecutionException | TimeoutException e) {
                    System.out.println("Task failed: " + e.getMessage());
                }
            }
        }
        executor.shutdownNow();
    }

    private void processResult(CrawledPage page) {
        results.add(page);
        if (onPageCrawled != null) onPageCrawled.accept(page);
        try {
            if (config.isSaveToCSV())    csvStorage.save(page);
            if (config.isSaveToJSON())   jsonStorage.save(page);
            if (config.isSaveToSQLite()) sqliteStorage.save(page);
        } catch (Exception e) {
            System.out.println("Storage error: " + e.getMessage());
        }
    }

    private CrawledPage crawlPageWithRetry(UrlTask task, int attempt) {
        if (pageCount.get() >= config.getMaxPages() || stopped.get()) return null;
        if (task.depth() > config.getMaxDepth()) return null;
        try {
            Thread.sleep(config.getPolitenessDelayMs());
            long t0 = System.currentTimeMillis();

            Document doc = Jsoup.connect(task.url())
                    .userAgent("JavaWebCrawler/1.0").timeout(config.getTimeoutMs())
                    .followRedirects(true).get();

            long elapsed = System.currentTimeMillis() - t0;
            String title = doc.title();
            String desc = Optional.ofNullable(doc.selectFirst("meta[name=description]"))
                    .map(e -> e.attr("content")).orElse("");

            List<String> links = new ArrayList<>();
            for (Element a : doc.select("a[href]")) {
                String href = a.absUrl("href");
                if (isValidUrl(href)) {
                    links.add(href);
                    if (task.depth() < config.getMaxDepth() && visitedUrls.add(href)
                            && pageCount.get() < config.getMaxPages()) {
                        queue.offer(new UrlTask(href, task.depth() + 1));
                    }
                }
            }

            pageCount.incrementAndGet();
            return new CrawledPage(task.url(), title, desc, links, 200, elapsed, task.depth());

        } catch (IOException e) {
            if (attempt < 1) {
                System.out.println("Network Error on " + task.url() + ". Retrying...");
                return crawlPageWithRetry(task, attempt + 1);
            }
            pageCount.incrementAndGet();
            return new CrawledPage(task.url(), "ERROR", e.getMessage(), Collections.emptyList(), 0, 0, task.depth());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"))
                && !url.contains("#")
                && !url.matches(".*\\.(pdf|jpg|jpeg|png|gif|zip|exe|mp4|mp3)$");
    }

    record UrlTask(String url, int depth) {}
}