module com.example.smartclass {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.github.librepdf.openpdf;
    requires org.apache.commons.io;
    requires java.desktop;
    requires org.json;
    requires okhttp3;

    opens com.example.smartclass to javafx.fxml;
    exports com.example.smartclass;
}