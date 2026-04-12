package com.crawler.gui;

import com.crawler.core.CrawlerEngine;
import com.crawler.model.CrawledPage;
import com.crawler.model.CrawlerConfig;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MainController {

    // Config controls
    @FXML TextField urlField, outputDirField;
    @FXML Spinner<Integer> depthSpinner, pagesSpinner, threadsSpinner, delaySpinner, timeoutSpinner;
    @FXML CheckBox csvCheck, jsonCheck, sqliteCheck;

    // Buttons
    @FXML Button startBtn, stopBtn, clearBtn;

    // Stats
    @FXML Label statPages, statSuccess, statErrors, statLinks, statTime, statusLabel, bottomStatus;
    @FXML ProgressBar progressBar;

    // Log
    @FXML TextArea logArea;

    // Table
    @FXML TableView<PageRow> resultsTable;
    @FXML TableColumn<PageRow, Integer> colDepth, colStatus, colLinks;
    @FXML TableColumn<PageRow, String>  colTitle, colUrl;
    @FXML TableColumn<PageRow, Long>    colTime;

    private final ObservableList<PageRow> tableData = FXCollections.observableArrayList();
    private CrawlerEngine engine;
    private Thread crawlThread;
    private long crawlStart;

    private final AtomicInteger totalPages   = new AtomicInteger(0);
    private final AtomicInteger successPages = new AtomicInteger(0);
    private final AtomicInteger errorPages   = new AtomicInteger(0);
    private final AtomicLong    totalLinks   = new AtomicLong(0);

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        // Spinner factories
        depthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 2));
        pagesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 50));
        threadsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 6));
        delaySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000, 300));
        timeoutSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1000, 60000, 10000));

        // Table columns
        colDepth.setCellValueFactory(d -> d.getValue().depth.asObject());
        colStatus.setCellValueFactory(d -> d.getValue().status.asObject());
        colTitle.setCellValueFactory(d -> d.getValue().title);
        colUrl.setCellValueFactory(d -> d.getValue().url);
        colLinks.setCellValueFactory(d -> d.getValue().links.asObject());
        colTime.setCellValueFactory(d -> d.getValue().time.asObject());

        // Color status cells
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(val));
                setStyle(val == 200 ? "-fx-text-fill: #50fa7b; -fx-alignment: CENTER;"
                                    : "-fx-text-fill: #ff5555; -fx-alignment: CENTER;");
            }
        });

        resultsTable.setItems(tableData);
        logArea.setStyle("-fx-font-family: 'Courier New', monospace;");

        appendLog("🕷  Web Crawler ready. Configure settings and press START.");
    }

    @FXML
    void onStart() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            showAlert("Please enter a target URL.");
            return;
        }
        if (!url.startsWith("http")) {
            showAlert("URL must start with http:// or https://");
            return;
        }

        CrawlerConfig config = new CrawlerConfig.Builder()
                .seedUrl(url)
                .maxDepth(depthSpinner.getValue())
                .maxPages(pagesSpinner.getValue())
                .threadCount(threadsSpinner.getValue())
                .timeoutMs(timeoutSpinner.getValue())
                .politenessDelayMs(delaySpinner.getValue())
                .saveToCSV(csvCheck.isSelected())
                .saveToJSON(jsonCheck.isSelected())
                .saveToSQLite(sqliteCheck.isSelected())
                .outputDir(outputDirField.getText().trim().isEmpty() ? "output" : outputDirField.getText().trim())
                .build();

        // Reset stats
        totalPages.set(0); successPages.set(0); errorPages.set(0); totalLinks.set(0);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        setStatus("● CRAWLING", "status-running");
        startBtn.setDisable(true);
        stopBtn.setDisable(false);
        crawlStart = System.currentTimeMillis();

        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("▶  Starting crawl: " + url);
        appendLog("   Depth=" + config.getMaxDepth() + "  Pages=" + config.getMaxPages()
                + "  Threads=" + config.getThreadCount());

        engine = new CrawlerEngine(config);
        engine.setOnPageCrawled(this::onPageResult);

        final int maxPages = config.getMaxPages();
        crawlThread = new Thread(() -> {
            try {
                engine.start();
                Platform.runLater(() -> onCrawlComplete(false));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    appendLog("❌ Error: " + e.getMessage());
                    onCrawlComplete(true);
                });
            }
        }, "crawl-thread");
        crawlThread.setDaemon(true);
        crawlThread.start();

        // Elapsed timer
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override public void handle(long now) {
                long elapsed = (System.currentTimeMillis() - crawlStart) / 1000;
                statTime.setText(elapsed + "s");
                double prog = (double) totalPages.get() / maxPages;
                progressBar.setProgress(Math.min(prog, 1.0));
                if (!startBtn.isDisable()) stop();
            }
        };
        timer.start();
    }

    private void onPageResult(CrawledPage page) {
        int p = totalPages.incrementAndGet();
        if (page.getStatusCode() == 200) successPages.incrementAndGet();
        else errorPages.incrementAndGet();
        totalLinks.addAndGet(page.getLinks().size());

        Platform.runLater(() -> {
            String icon = page.getStatusCode() == 200 ? "✓" : "✗";
            String shortUrl = page.getUrl().length() > 70
                    ? page.getUrl().substring(0, 67) + "..." : page.getUrl();
            appendLog(String.format("[%s] [D%d] [HTTP %d] [%dms] %s",
                    icon, page.getDepth(), page.getStatusCode(), page.getCrawlTimeMs(), shortUrl));

            tableData.add(new PageRow(page));
            statPages.setText(String.valueOf(p));
            statSuccess.setText(String.valueOf(successPages.get()));
            statErrors.setText(String.valueOf(errorPages.get()));
            statLinks.setText(String.valueOf(totalLinks.get()));
        });
    }

    private void onCrawlComplete(boolean error) {
        long elapsed = System.currentTimeMillis() - crawlStart;
        progressBar.setProgress(1.0);
        startBtn.setDisable(false);
        stopBtn.setDisable(true);

        if (error) {
            setStatus("● ERROR", "status-error");
        } else {
            setStatus("● DONE", "status-done");
        }

        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog(String.format("■  Crawl complete  |  %d pages  |  %d links  |  %.1fs",
                totalPages.get(), totalLinks.get(), elapsed / 1000.0));
        bottomStatus.setText("Crawl finished — " + totalPages.get() + " pages in " + (elapsed / 1000) + "s");
    }

    @FXML void onStop() {
        if (engine != null) engine.stop();
        appendLog("■  Stop requested...");
        stopBtn.setDisable(true);
        setStatus("● STOPPING", "status-running");
    }

    @FXML void onClear() {
        logArea.clear();
        tableData.clear();
        statPages.setText("0"); statSuccess.setText("0");
        statErrors.setText("0"); statLinks.setText("0"); statTime.setText("0s");
        progressBar.setProgress(0);
        totalPages.set(0); successPages.set(0); errorPages.set(0); totalLinks.set(0);
        setStatus("● READY", "status-ready");
        bottomStatus.setText("Results cleared.");
        appendLog("🕷  Results cleared. Ready for next crawl.");
    }

    private void appendLog(String msg) {
        String line = "[" + LocalTime.now().format(TIME_FMT) + "] " + msg + "\n";
        logArea.appendText(line);
    }

    private void setStatus(String text, String styleClass) {
        statusLabel.setText(text);
        statusLabel.getStyleClass().removeAll("status-ready","status-running","status-done","status-error");
        statusLabel.getStyleClass().add(styleClass);
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Input Error");
        a.showAndWait();
    }

    // ── Table row model ──────────────────────────────────────────────────────
    public static class PageRow {
        public final SimpleIntegerProperty depth;
        public final SimpleIntegerProperty status;
        public final SimpleStringProperty  title;
        public final SimpleStringProperty  url;
        public final SimpleIntegerProperty links;
        public final SimpleLongProperty    time;

        public PageRow(CrawledPage p) {
            depth  = new SimpleIntegerProperty(p.getDepth());
            status = new SimpleIntegerProperty(p.getStatusCode());
            title  = new SimpleStringProperty(p.getTitle());
            url    = new SimpleStringProperty(p.getUrl());
            links  = new SimpleIntegerProperty(p.getLinks().size());
            time   = new SimpleLongProperty(p.getCrawlTimeMs());
        }
    }
}
