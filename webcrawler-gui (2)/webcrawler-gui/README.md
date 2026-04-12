# 🕷️ Java Multi-Threaded Web Crawler

A production-grade web crawler written in Java with multi-threading support and three storage backends.

## ✨ Features

| Feature | Details |
|---|---|
| 🚀 Multi-threading | Configurable thread pool (default: 6 threads) |
| 🌊 BFS Crawling | Breadth-first traversal with configurable depth |
| 💾 CSV Output | One row per page with all metadata |
| 📄 JSON Output | NDJSON per-page + full summary JSON |
| 🗄️ SQLite Output | Relational DB with `pages` and `links` tables |
| 🖥️ Console Output | Colored real-time progress display |
| 🤝 Politeness | Configurable delay between requests |
| 🔒 Dedup | Visited URL set prevents re-crawling |

## 📦 Requirements

- Java 17+
- Maven 3.6+

## 🚀 Quick Start

```bash
# 1. Build
mvn clean package -q

# 2. Run
java -jar target/web-crawler.jar
```

## ⚙️ Configuration (Main.java)

```java
CrawlerConfig config = new CrawlerConfig.Builder()
    .seedUrl("https://example.com")  // Target website
    .maxDepth(2)                      // Link depth (0 = seed only)
    .maxPages(100)                    // Max pages total
    .threadCount(6)                   // Parallel threads
    .timeoutMs(10_000)                // Request timeout (ms)
    .politenessDelayMs(300)           // Delay per thread (ms)
    .saveToCSV(true)
    .saveToJSON(true)
    .saveToSQLite(true)
    .outputDir("output")
    .build();
```

## 📂 Output Files

```
output/
├── crawled_pages.csv           ← All pages (spreadsheet-friendly)
├── crawled_pages.json          ← Per-page NDJSON with links
├── crawled_pages_summary.json  ← Aggregate summary
├── crawled_pages.db            ← SQLite database
└── crawler.log                 ← Debug log
```

### SQLite Schema

```sql
-- Pages table
SELECT url, title, status_code, depth FROM pages;

-- Link graph
SELECT source_url, target_url FROM links;

-- Pages by depth
SELECT depth, COUNT(*) FROM pages GROUP BY depth;
```

## 🏗️ Project Structure

```
src/main/java/com/crawler/
├── Main.java                   ← Entry point & config
├── core/
│   └── CrawlerEngine.java      ← Multi-threaded crawler logic
├── model/
│   ├── CrawledPage.java        ← Page data model
│   └── CrawlerConfig.java      ← Config builder
├── storage/
│   ├── CsvStorage.java         ← CSV writer
│   ├── JsonStorage.java        ← JSON/NDJSON writer
│   └── SqliteStorage.java      ← SQLite writer
└── util/
    └── ConsoleDisplay.java     ← Colored terminal output
```

## ⚠️ Ethical Usage

- Respect `robots.txt` of target sites
- Use `politenessDelayMs` to avoid overloading servers
- Only crawl websites you have permission to crawl
- Built for educational & research purposes
