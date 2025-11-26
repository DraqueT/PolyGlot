module org.darisadesigns.polyglotlina.polyglot {
    requires com.fasterxml.jackson.databind;
    requires javafx.controls;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;
    requires transitive java.desktop;
    requires java.logging;
    requires java.xml;
    requires jdk.charsets;
    requires jdk.httpserver;
    requires org.apache.commons.lang3;
    requires org.apache.commons.csv;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.jsoup;
    
    exports org.darisadesigns.polyglotlina;
    exports org.darisadesigns.polyglotlina.CustomControls;
    exports org.darisadesigns.polyglotlina.Desktop;
    exports org.darisadesigns.polyglotlina.Desktop.CustomControls;
    exports org.darisadesigns.polyglotlina.Desktop.ManagersCollections;
    exports org.darisadesigns.polyglotlina.ManagersCollections;
    exports org.darisadesigns.polyglotlina.Nodes;
    exports org.darisadesigns.polyglotlina.QuizEngine;
    exports org.darisadesigns.polyglotlina.Screens;
}
