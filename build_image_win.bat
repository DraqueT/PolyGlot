:: Constructs PolyGlot Windows Package

echo off

set JAVAFX_LOCATION=C:\Users\user\.m2\repository\org\openjfx
set JAVAFX_VER=12.0.2
set JAVA_PACKAGER_LOCATION=C:\Java\jdk-14\bin
set JAR_W_DEP=PolyGlotLinA-3.0-jar-with-dependencies.jar
set JAR_WO_DEP=PolyGlotLinA-3.0.jar
set RES=F

IF "%1"=="" set RES=T
IF "%1"=="build" set RES=T
IF  "%RES%"=="T" (
    echo "cleaning/testing/compiling..."
    echo on
    ::mvn clean package
    echo off
)
set RES=F

IF "%1"=="" set RES=T
IF "%1"=="clean" set RES=T
IF "%1"=="image" set RES=T
IF  "%RES%"=="T" (
    echo "cleaning build paths..."
    rmdir target\mods /s /q
    rmdir build /s /q
)
set RES=F

IF "%1"=="" set RES=T
IF "%1"=="image" set RES=T
IF  "%RES%"=="T" (
    echo "creating jmod based on jar built without dependencies..."
    mkdir target\mods
    echo on
    %JAVA_PACKAGER_LOCATION%\jmod create --class-path target\%JAR_WO_DEP% --main-class org.darisadesigns.polyglotlina.PolyGlot target\mods\PolyGlot.jmod
    echo off

    echo "creating runnable image..."
    echo on
    %JAVA_HOME%\bin\jlink --module-path "module_injected_jars\;target\mods;%JAVAFX_LOCATION%\javafx-graphics\%JAVAFX_VER%;%JAVAFX_LOCATION%\javafx-base\%JAVAFX_VER%;%JAVAFX_LOCATION%\javafx-media\%JAVAFX_VER%;%JAVAFX_LOCATION%\javafx-swing\%JAVAFX_VER%\;%JAVAFX_LOCATION%\javafx-controls\%JAVAFX_VER%;%JAVA_HOME%\jmods" --add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" --output "build\image" --compress=2 --launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot
    echo off
)
set RES=F

IF "%1"=="" set RES=T
IF "%1"=="pack" set RES=T
IF  "%RES%"=="T" (
    rmdir appimage /s /q
    echo "packing Linux app..."
    echo on
    %JAVA_PACKAGER_LOCATION%\jpackage --runtime-image build\image --input target --output appimage --name PolyGlot --main-jar %JAR_W_DEP% --copyright "2014-2019 Draque Thompson" --description "PolyGlot is a spoken language construction toolkit." --icon packaging_files/PolyGlot0.ico
    echo off
    copy appimage\PolyGlot\app\PolyGlot.cfg appimage\PolyGlot\PolyGlot.cfg
    rmdir appimage\PolyGlot\app /s /q
    mkdir appimage\PolyGlot\app
    copy appimage\PolyGlot\PolyGlot.cfg appimage\PolyGlot\app\PolyGlot.cfg
    del appimage\PolyGlot\PolyGlot.cfg
)
set RES=F

echo "Done!"

