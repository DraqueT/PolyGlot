module org.darisadesigns.polyglotlina.polyglot {
    // All modules labeled as automatic must be modularized when upgrading
    requires jakarta.json;
    requires javafx.controls;
    requires javafx.media;
    requires javafx.swing;
    requires java.desktop;
    requires java.logging;
    requires java.xml;
    requires jdk.charsets;
    requires jdk.httpserver;
    requires jsr305; //AUT - fixed with module injector - only needed for jsoup
    requires org.apache.commons.lang3; //AUT - fixed with module injector
    requires org.apache.commons.csv; //AUT - fixed with module injector
    requires org.jsoup; //AUT - fixed with module injector
    
    exports org.darisadesigns.polyglotlina;
    exports org.darisadesigns.polyglotlina.CustomControls;
    exports org.darisadesigns.polyglotlina.Desktop.CustomControls;
}
