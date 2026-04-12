package com.crawler.util;

import com.crawler.model.CrawledPage;
import com.crawler.model.CrawlerConfig;

import java.util.List;

public class ConsoleDisplay {

    private static final String RESET  = "\u001B[0m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String BOLD   = "\u001B[1m";
    private static final String BLUE   = "\u001B[34m";

    public static void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║          🕷  JAVA MULTI-THREADED WEB CRAWLER         ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println(RESET);
    }

    public static void printConfig(CrawlerConfig config) {
        System.out.println(YELLOW + "━━━━━━━━━━━━━━━━━━━━━━ Configuration ━━━━━━━━━━━━━━━━━━━━━━" + RESET);
        System.out.printf("  Seed URL      : %s%s%s%n", CYAN,  config.getSeedUrl(), RESET);
        System.out.printf("  Max Depth     : %s%d%s%n", BOLD,  config.getMaxDepth(), RESET);
        System.out.printf("  Max Pages     : %s%d%s%n", BOLD,  config.getMaxPages(), RESET);
        System.out.printf("  Threads       : %s%d%s%n", GREEN, config.getThreadCount(), RESET);
        System.out.printf("  Politeness    : %sms%n",          config.getPolitenessDelayMs());
        System.out.printf("  Storage       : %s%s%s%n", BLUE,
                (config.isSaveToCSV() ? "CSV " : "") +
                (config.isSaveToJSON() ? "JSON " : "") +
                (config.isSaveToSQLite() ? "SQLite" : ""), RESET);
        System.out.println(YELLOW + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
        System.out.println();
    }

    public static void printPageResult(CrawledPage page) {
        String statusColor = page.getStatusCode() == 200 ? GREEN : RED;
        System.out.printf("%s[HTTP %d]%s %s%n",
                statusColor, page.getStatusCode(), RESET, page);
    }

    public static void printSummary(List<CrawledPage> results, long elapsedMs, String outputDir) {
        long success = results.stream().filter(p -> p.getStatusCode() == 200).count();
        long errors  = results.size() - success;
        long links   = results.stream().mapToInt(p -> p.getLinks().size()).sum();

        System.out.println();
        System.out.println(CYAN + BOLD + "╔══════════════════════════════════════╗");
        System.out.println(              "║           CRAWL SUMMARY              ║");
        System.out.println(              "╚══════════════════════════════════════╝" + RESET);
        System.out.printf("  Total pages   : %s%d%s%n",  BOLD,  results.size(), RESET);
        System.out.printf("  Successful    : %s%d%s%n",  GREEN, success, RESET);
        System.out.printf("  Errors        : %s%d%s%n",  RED,   errors,  RESET);
        System.out.printf("  Links found   : %s%d%s%n",  CYAN,  links,   RESET);
        System.out.printf("  Total time    : %s%.2fs%s%n", YELLOW, elapsedMs / 1000.0, RESET);
        System.out.printf("  Avg/page      : %s%.0fms%s%n", YELLOW,
                results.isEmpty() ? 0 : (double) elapsedMs / results.size(), RESET);
        System.out.println();
        System.out.printf("  Output saved to: %s%s/%s%n", BLUE, outputDir, RESET);
        System.out.printf("    • %scrawled_pages.csv%s%n",     GREEN, RESET);
        System.out.printf("    • %scrawled_pages.json%s%n",    GREEN, RESET);
        System.out.printf("    • %scrawled_pages_summary.json%s%n", GREEN, RESET);
        System.out.printf("    • %scrawled_pages.db%s%n",      GREEN, RESET);
    }
}
