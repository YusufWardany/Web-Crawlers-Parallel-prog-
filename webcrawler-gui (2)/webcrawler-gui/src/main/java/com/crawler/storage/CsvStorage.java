package com.crawler.storage;

import com.crawler.model.CrawledPage;

import java.io.*;
import java.nio.file.*;

public class CsvStorage {
    private final PrintWriter writer;
    private final String filePath;

    public CsvStorage(String outputDir) throws IOException {
        Files.createDirectories(Paths.get(outputDir));
        filePath = outputDir + "/crawled_pages.csv";
        writer = new PrintWriter(new FileWriter(filePath, false));
        writer.println("URL,Title,Description,LinkCount,StatusCode,CrawlTimeMs,Depth,CrawledAt");
        writer.flush();
    }

    public synchronized void save(CrawledPage page) {
        writer.printf("\"%s\",\"%s\",\"%s\",%d,%d,%d,%d,\"%s\"%n",
                escape(page.getUrl()),
                escape(page.getTitle()),
                escape(page.getDescription()),
                page.getLinks().size(),
                page.getStatusCode(),
                page.getCrawlTimeMs(),
                page.getDepth(),
                page.getCrawledAt().toString());
        writer.flush();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"").replace("\n", " ").replace("\r", "");
    }

    public String getFilePath() { return filePath; }
}
