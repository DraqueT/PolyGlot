module org.darisadesigns.polyglotlina.polyglot {
    // All modules labeled as automatic must be modularized when upgrading

    requires com.fasterxml.jackson.databind;
    requires javafx.controls;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;
    requires java.desktop;
    requires java.logging;
    requires java.xml;
    requires jdk.charsets;
    requires jdk.httpserver;
    requires org.apache.commons.lang3;
    requires org.apache.commons.csv; //AUT - fixed with module injector
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.jsoup;
    
    exports org.darisadesigns.polyglotlina;
    exports org.darisadesigns.polyglotlina.CustomControls;
    exports org.darisadesigns.polyglotlina.Desktop.CustomControls;
}
