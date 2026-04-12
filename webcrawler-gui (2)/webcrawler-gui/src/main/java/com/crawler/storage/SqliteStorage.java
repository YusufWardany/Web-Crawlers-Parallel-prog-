package com.crawler.storage;

import com.crawler.model.CrawledPage;

import java.nio.file.*;
import java.sql.*;

public class SqliteStorage {
    private final Connection conn;
    private final PreparedStatement insertPage;
    private final PreparedStatement insertLink;

    public SqliteStorage(String outputDir) throws Exception {
        Files.createDirectories(Paths.get(outputDir));
        String dbPath = outputDir + "/crawled_pages.db";
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        conn.setAutoCommit(false);

        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pages (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    url         TEXT UNIQUE NOT NULL,
                    title       TEXT,
                    description TEXT,
                    status_code INTEGER,
                    crawl_time  INTEGER,
                    depth       INTEGER,
                    crawled_at  TEXT
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS links (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    source_url  TEXT NOT NULL,
                    target_url  TEXT NOT NULL
                )
            """);
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_links_src ON links(source_url)");
        }
        conn.commit();

        insertPage = conn.prepareStatement("""
            INSERT OR IGNORE INTO pages (url, title, description, status_code, crawl_time, depth, crawled_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """);
        insertLink = conn.prepareStatement("""
            INSERT INTO links (source_url, target_url) VALUES (?, ?)
        """);
    }

    public synchronized void save(CrawledPage page) throws SQLException {
        insertPage.setString(1, page.getUrl());
        insertPage.setString(2, page.getTitle());
        insertPage.setString(3, page.getDescription());
        insertPage.setInt(4,    page.getStatusCode());
        insertPage.setLong(5,   page.getCrawlTimeMs());
        insertPage.setInt(6,    page.getDepth());
        insertPage.setString(7, page.getCrawledAt().toString());
        insertPage.executeUpdate();

        for (String link : page.getLinks()) {
            insertLink.setString(1, page.getUrl());
            insertLink.setString(2, link);
            insertLink.addBatch();
        }
        insertLink.executeBatch();
        conn.commit();
    }

    public void close() throws SQLException {
        if (insertPage != null) insertPage.close();
        if (insertLink != null) insertLink.close();
        if (conn != null)       conn.close();
    }
}
