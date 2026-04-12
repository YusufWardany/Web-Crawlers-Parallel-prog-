package com.crawler.model;

public class CrawlerConfig {
    private String seedUrl;
    private int maxDepth;
    private int maxPages;
    private int threadCount;
    private int timeoutMs;
    private long politenessDelayMs;
    private boolean saveToCSV;
    private boolean saveToJSON;
    private boolean saveToSQLite;
    private String outputDir;

    private CrawlerConfig() {}

    public static class Builder {
        private String seedUrl;
        private int maxDepth = 2;
        private int maxPages = 100;
        private int threadCount = 4;
        private int timeoutMs = 10000;
        private long politenessDelayMs = 500;
        private boolean saveToCSV = true;
        private boolean saveToJSON = true;
        private boolean saveToSQLite = true;
        private String outputDir = "output";

        public Builder seedUrl(String url)              { this.seedUrl = url; return this; }
        public Builder maxDepth(int d)                  { this.maxDepth = d; return this; }
        public Builder maxPages(int p)                  { this.maxPages = p; return this; }
        public Builder threadCount(int t)               { this.threadCount = t; return this; }
        public Builder timeoutMs(int ms)                { this.timeoutMs = ms; return this; }
        public Builder politenessDelayMs(long ms)       { this.politenessDelayMs = ms; return this; }
        public Builder saveToCSV(boolean b)             { this.saveToCSV = b; return this; }
        public Builder saveToJSON(boolean b)            { this.saveToJSON = b; return this; }
        public Builder saveToSQLite(boolean b)          { this.saveToSQLite = b; return this; }
        public Builder outputDir(String dir)            { this.outputDir = dir; return this; }

        public CrawlerConfig build() {
            if (seedUrl == null || seedUrl.isBlank())
                throw new IllegalArgumentException("Seed URL must be provided.");
            CrawlerConfig c = new CrawlerConfig();
            c.seedUrl = seedUrl; c.maxDepth = maxDepth; c.maxPages = maxPages;
            c.threadCount = threadCount; c.timeoutMs = timeoutMs;
            c.politenessDelayMs = politenessDelayMs; c.saveToCSV = saveToCSV;
            c.saveToJSON = saveToJSON; c.saveToSQLite = saveToSQLite;
            c.outputDir = outputDir;
            return c;
        }
    }

    public String getSeedUrl()           { return seedUrl; }
    public int getMaxDepth()             { return maxDepth; }
    public int getMaxPages()             { return maxPages; }
    public int getThreadCount()          { return threadCount; }
    public int getTimeoutMs()            { return timeoutMs; }
    public long getPolitenessDelayMs()   { return politenessDelayMs; }
    public boolean isSaveToCSV()         { return saveToCSV; }
    public boolean isSaveToJSON()        { return saveToJSON; }
    public boolean isSaveToSQLite()      { return saveToSQLite; }
    public String getOutputDir()         { return outputDir; }
}
