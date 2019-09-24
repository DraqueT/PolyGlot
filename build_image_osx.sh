#!/bin/bash
# Constructs PolyGlot Mac package

JAVAFX_LOCATION="/Users/draque/.m2/repository/org/openjfx"
JAVAFX_VER="12.0.2"
JAVA_PACKAGER_LOCATION="/Users/draque/NetBeansProjects/jdk_14_packaging/Contents/Home/bin"  # this will go away once Java 14 drops officially...
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
$JAVAFX_LOCATION/javafx-media/12.0.2/:\
$JAVAFX_LOCATION/javafx-swing/$JAVAFX_VER/:\
$JAVAFX_LOCATION/javafx-controls/$JAVAFX_VER/:\
$JAVA_HOME/jmods" \
        --add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" \
        --output "build/image/" \
        --compress=2 \
        --launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot
fi

if [ "$1" = "" ] || [ "$1" = "pack" ]; then
    rm -rf appimage
    echo "packing mac app..."
    $JAVA_PACKAGER_LOCATION/jpackage \
        --runtime-image build/image --output appimage \
        --name PolyGlot \
        --module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot \
        --copyright "2014-2019 Draque Thompson" \
        --description "PolyGlot is a spoken language construction toolkit." \
        --mac-bundle-identifier "PolyGlot" \
        --mac-bundle-name "PolyGlot" \
        --file-associations packaging_files/mac/file_types_mac.prop \
        --icon packaging_files/mac/PolyGlot.icns
fi

if [ "$1" = "" ] || [ "$1" = "dist" ]; then
    echo "Creating distribution package..."
    rm -rf installer
    mkdir installer
    # if this does not work correctly: brew install create-dmg
    create-dmg \
        --volname "PolyGlot Installer" \
        --volicon "packaging_files/mac/PolyGlot.icns" \
        --app-drop-link 450 250 \
        --hide-extension "PolyGlot.app" \
        --background "packaging_files/mac/bg.png" \
        --window-pos 200 120 \
        --window-size 650 591 \
        --icon-size 120 \
        --icon "PolyGlot.app" 200 250 \
        "installer/PolyGlot-Ins.dmg" \
        "appimage/"
fi

echo "Done!"
