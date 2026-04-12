# 🕷️ Parallel Web Crawler

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-GUI-474A8A?style=for-the-badge&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-Database-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build_Tool-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

A high-performance, multi-threaded Web Crawler built to extract web data efficiently. Designed for **Open-Source Intelligence (OSINT)** and data engineering, this crawler replaces slow, sequential I/O operations with a highly concurrent pipeline.

Built by **Team Ragnarok** as part of the CSE373 Parallel Programming course at Alamein International University (AIU).

---

## 🎯 Project Goal
The main goal of this project is to solve the bottleneck of network latency in web crawling. Instead of waiting for one web page to download before starting the next, we use **Parallel Programming** to fetch, parse, and save multiple pages at the exact same time. This is inspired by the architecture proposed by Cho & Garcia-Molina (Stanford University) to maximize CPU usage and minimize idle time.

## 📊 Performance Comparison (Sequential vs. Parallel)
We conducted a benchmark by crawling 50 pages from a test website. The results prove the massive advantage of Parallelism:

| Mode | Threads | Execution Time | Efficiency |
| :--- | :---: | :--- | :--- |
| **Sequential** | 1 | 30.3 Seconds | Baseline (Slow) |
| **Parallel** | 6 | **8.0 Seconds** | **~3.78x Speedup** 🚀 |

*The Parallel implementation is nearly 4 times faster because threads work simultaneously while others wait for network responses.*

## 📐 System Architecture & Diagrams
To ensure a robust design, we developed 6 UML diagrams:
1. **Use Case Diagram:** Defines user interactions like configuring settings and starting the crawl.
2. **Class Diagram:** Shows the code structure, including the core engine, storage, and data models.
3. **Sequence Diagram:** Traces the communication flow between the GUI, Engine, and Web Servers.
4. **Activity Diagram:** Zooms into the logic of a single thread, showing link extraction and **Retry Logic**.
5. **Component Diagram:** Illustrates the 3-Layer Architecture (Presentation, Core, Storage).
6. **State Diagram:** Maps the application's lifecycle from Ready to Crawling and Done.

## ⚙️ How It Works (Code Overview)
Our system follows a **Producer-Consumer** model implemented with high-level concurrency tools:

* **Parallel Engine:** Uses `ExecutorService` (Thread Pool) to dispatch worker threads.
* **Task Management:** A thread-safe `LinkedBlockingQueue` manages discovered URLs.
* **Visited Set:** A `ConcurrentHashMap` ensures each page is crawled only once (Atomic Deduplication).
* **Resilient Logic:** Includes **Retry Logic** that catches network errors and retries the fetch once before failing.
* **Synchronized Storage:** Uses `synchronized` blocks to safely write results into **SQLite**, **CSV**, and **JSON** simultaneously without data corruption.

## 👨‍💻 Team Ragnarok
* **Yusuf Yasser Said** - 22100809
* **Noureen Sayed Rezk** - 22101183
* **Lojain Wael Mohamed** - 22101063
* **Mazen Ashraf** - 22100105
* **Fares Mohammed** - 22101153


---
*Developed for academic purposes in the Parallel Programming course (CSE373).*
