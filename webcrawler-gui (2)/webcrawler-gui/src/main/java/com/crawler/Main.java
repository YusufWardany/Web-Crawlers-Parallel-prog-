package com.crawler;

import com.crawler.core.CrawlerEngine;
import com.crawler.model.CrawledPage;
import com.crawler.model.CrawlerConfig;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        // ─── Configure the crawler here ───────────────────────────────────
        CrawlerConfig config = new CrawlerConfig.Builder()
                .seedUrl("https://example.com")   // 👈 Change this to your target URL
                .maxDepth(2)                        // How deep to follow links (0 = seed only)
                .maxPages(50)                       // Max pages to crawl total
                .threadCount(6)                     // Parallel threads
                .timeoutMs(10_000)                  // Per-request timeout
                .politenessDelayMs(300)             // Delay between requests per thread
                .saveToCSV(true)
                .saveToJSON(true)
                .saveToSQLite(true)
                .outputDir("output")
                .build();
        // ──────────────────────────────────────────────────────────────────

        CrawlerEngine engine = new CrawlerEngine(config);
        List<CrawledPage> results = engine.start();

        System.out.printf("%n✅ Done! Crawled %d pages.%n", results.size());
    }
}
