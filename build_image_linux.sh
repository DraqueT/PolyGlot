#!/bin/bash
# Constructs PolyGlot Linux Package

JAVAFX_LOCATION="/home/osboxes/.m2/repository/org/openjfx"
JAVAFX_VER="12.0.2"
JAVA_PACKAGER_LOCATION="/usr/Java/jdk-14/bin" # this will go away once Java 14 drops officially...
JAR_W_DEP="PolyGlotLinA-3.0-jar-with-dependencies.jar"
JAR_WO_DEP="PolyGlotLinA-3.0.jar"

if  [ "$1" = "" ] || [ "$1" = "build" ]; then
    echo "cleaning/testing/compiling..."
    mvn clean package
fi

if [ "$1" = "" ] || [ "$1" = "clean" ] || [ "$1" = "image" ]; then
    echo "cleaning build paths..."
    rm -rf target/mods
    rm -rf build
fi
	
if [ "$1" = "" ] || [ "$1" = "image" ]; then
    echo "creating jmod based on jar built without dependencies..."
    mkdir target/mods
    $JAVA_HOME/bin/jmod create \
        --class-path target/$JAR_WO_DEP \
        --main-class org.darisadesigns.polyglotlina.PolyGlot target/mods/PolyGlot.jmod

    echo "creating runnable image..."
    $JAVA_HOME/bin/jlink \
        --module-path "module_injected_jars/:\
target/mods:\
$JAVAFX_LOCATION/javafx-graphics/$JAVAFX_VER/:\
$JAVAFX_LOCATION/javafx-base/$JAVAFX_VER/:\
$JAVAFX_LOCATION/javafx-media/$JAVAFX_VER/:\
$JAVAFX_LOCATION/javafx-swing/$JAVAFX_VER/:\
$JAVAFX_LOCATION/javafx-controls/$JAVAFX_VER/:\
$JAVA_HOME/jmods" \
        --add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" \
        --output "build/image/" \
        --compress=2 \
        --launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot
fi

if [ "$1" = "pack" ]; then
    rm -rf appimage
    echo "packing Linux app..."
    $JAVA_PACKAGER_LOCATION/jpackage \
        --runtime-image build/image \
        --output appimage \
        --name PolyGlot \
        --module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot \
        --copyright "2014-2019 Draque Thompson" \
        --description "PolyGlot is a spoken language construction toolkit." \
        --icon packaging_files/PolyGlot0.png
    cp packaging_files/PolyGlot.sh appimage/PolyGlot/PolyGlot.sh
fi

if [ "$1" = "" ] || [ "$1" = "dist" ]; then
    rm -rf installer
    mkdir installer
    echo "Creating distribution deb..."
    $JAVA_PACKAGER_LOCATION/jpackage \
        --package-type deb \
        --file-associations packaging_files/linux/file_types_linux.prop \
        --runtime-image build/image \
        --output installer \
        --name PolyGlot \
        --module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot \
        --copyright "2014-2019 Draque Thompson" \
        --description "PolyGlot is a spoken language construction toolkit." \
        --icon packaging_files/PolyGlot0.png
fi

echo "Done!"
