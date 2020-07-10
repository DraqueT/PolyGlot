module org.darisadesigns.polyglotlina.polyglot {
    // All modules labeled as automatic must be modularized when upgrading
    requires commons.csv;
    requires javafx.controls;
    requires javafx.media;
    requires javafx.swing;
    requires java.desktop;
    requires java.logging;
    requires java.management;
    requires org.apache.commons.lang3; //AUT - fixed with module injector
    requires org.jsoup; // AUT - fixed with module injector

    exports org.darisadesigns.polyglotlina;
    exports org.darisadesigns.polyglotlina.CustomControls;
}
