package com.crawler.storage;

import com.crawler.model.CrawledPage;
import com.google.gson.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class JsonStorage {
    private final String filePath;
    private final Gson gson;

    public JsonStorage(String outputDir) throws IOException {
        Files.createDirectories(Paths.get(outputDir));
        filePath = outputDir + "/crawled_pages.json";
        gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    }

    /** Called per-page for incremental NDJSON (newline-delimited JSON) */
    public synchronized void save(CrawledPage page) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, true))) {
            JsonObject obj = new JsonObject();
            obj.addProperty("url",          page.getUrl());
            obj.addProperty("title",        page.getTitle());
            obj.addProperty("description",  page.getDescription());
            obj.addProperty("statusCode",   page.getStatusCode());
            obj.addProperty("crawlTimeMs",  page.getCrawlTimeMs());
            obj.addProperty("depth",        page.getDepth());
            obj.addProperty("crawledAt",    page.getCrawledAt().toString());
            obj.addProperty("linkCount",    page.getLinks().size());

            JsonArray linksArr = new JsonArray();
            page.getLinks().forEach(linksArr::add);
            obj.add("links", linksArr);

            pw.println(gson.toJson(obj));
        }
    }

    /** Optionally write a combined summary JSON at the end */
    public void finalize(List<CrawledPage> pages) throws IOException {
        String summaryPath = filePath.replace(".json", "_summary.json");
        try (PrintWriter pw = new PrintWriter(new FileWriter(summaryPath, false))) {
            JsonObject summary = new JsonObject();
            summary.addProperty("totalPages", pages.size());
            summary.addProperty("successPages",
                    pages.stream().filter(p -> p.getStatusCode() == 200).count());
            summary.addProperty("errorPages",
                    pages.stream().filter(p -> p.getStatusCode() != 200).count());
            summary.addProperty("totalLinksFound",
                    pages.stream().mapToInt(p -> p.getLinks().size()).sum());

            JsonArray arr = new JsonArray();
            pages.forEach(p -> {
                JsonObject o = new JsonObject();
                o.addProperty("url",    p.getUrl());
                o.addProperty("title",  p.getTitle());
                o.addProperty("depth",  p.getDepth());
                o.addProperty("status", p.getStatusCode());
                arr.add(o);
            });
            summary.add("pages", arr);
            pw.println(gson.toJson(summary));
        }
    }

    public String getFilePath() { return filePath; }
}
