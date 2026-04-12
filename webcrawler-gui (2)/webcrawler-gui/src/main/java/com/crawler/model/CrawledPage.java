package com.crawler.model;

import java.time.LocalDateTime;
import java.util.List;

public class CrawledPage {
    private String url;
    private String title;
    private String description;
    private List<String> links;
    private int statusCode;
    private long crawlTimeMs;
    private LocalDateTime crawledAt;
    private int depth;

    public CrawledPage(String url, String title, String description,
                       List<String> links, int statusCode, long crawlTimeMs, int depth) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.links = links;
        this.statusCode = statusCode;
        this.crawlTimeMs = crawlTimeMs;
        this.depth = depth;
        this.crawledAt = LocalDateTime.now();
    }

    // Getters
    public String getUrl()            { return url; }
    public String getTitle()          { return title; }
    public String getDescription()    { return description; }
    public List<String> getLinks()    { return links; }
    public int getStatusCode()        { return statusCode; }
    public long getCrawlTimeMs()      { return crawlTimeMs; }
    public LocalDateTime getCrawledAt() { return crawledAt; }
    public int getDepth()             { return depth; }

    @Override
    public String toString() {
        return String.format("[Depth %d] %-60s | %d links | %dms | HTTP %d",
                depth, url.length() > 60 ? url.substring(0, 57) + "..." : url,
                links.size(), crawlTimeMs, statusCode);
    }
}
