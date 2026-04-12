module com.crawler {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires java.sql;
    requires com.google.gson;
    requires org.slf4j;

    opens com.crawler.gui to javafx.fxml;
    exports com.crawler.gui;
    exports com.crawler.model;
}
