##############################################################################
#
#   PolyGlot build script Copyright 2019 Draque Thompson
#
#   This script builds PolyGlot into a distributable package on Linux,
#   OSX, and Windows. Windows does not come with Python installed by default.
#   This should run on both Python 2.7 and 3.x. 
#
#   From: https://github.com/DraqueT/PolyGlot/
#
##############################################################################

import platform
import sys
import os

osString = platform.system()
linString = "Linux"
osxString = "Darwin"
winString = "Windows"

#######################
# UTIL FUNCTIONALITY
#######################

def getVersion():
    if osString == winString:
        location = 'assets\assets\org\DarisaDesigns\version'
    else:
        location = 'assets/assets/org/DarisaDesigns/version'

    with open(location, 'r') as myfile:
        data = myfile.read()
        
    return data


###############################
# LINUX BUILD CONSTANTS
JAVAFX_LOCATION_LINUX = "/home/osboxes/.m2/repository/org/openjfx"
JAVA_PACKAGER_LOCATION_LINUX = "/usr/Java/jdk-14/bin" # this will go away once Java 14 drops officially...


###############################
# OSX BUILD CONSTANTS
JAVAFX_LOCATION_OSX = "/Users/draque/.m2/repository/org/openjfx"
JAVA_PACKAGER_LOCATION_OSX = "/Users/draque/NetBeansProjects/jdk_14_packaging/Contents/Home/bin"  # this will go away once Java 14 drops officially...


###############################
# WINDOWS BUILD CONSTANTS


###############################
# UNIVERSAL BUILD CONSTANTS
JAR_W_DEP = "PolyGlotLinA-3.0-jar-with-dependencies.jar"
JAR_WO_DEP = "PolyGlotLinA-3.0.jar"
JAVAFX_VER = "12.0.2"
POLYGLOT_VERSION = getVersion()



def main(args):
    fullBuild = (len(args) == 1) # length of 1 means no arguments (full build)
    
    if "help" in args:
        print("this should print help. use textblock")
    
    if fullBuild or "build" in args:
        build()
    if fullBuild or "clean" in args or "image" in args:
        clean()
    if fullBuild or "image" in args:
        image()
    if fullBuild or "pack" in args: # not all OSes need this. consider
        pack()
    if fullBuild or "dist" in args:
        dist()
        
    print('Done!')
    

def build():
    if osString == linString:
        buildLinux()
    elif osString == osxString:
        buildOsx()
    elif osString == winString:
        buildWin()
    
def clean():
    if osString == linString:
        cleanLinux()
    elif osString == osxString:
        cleanOsx()
    elif osString == winString:
        cleanWin()
    
def image():
    if osString == linString:
        imageLinux()
    elif osString == osxString:
        imageOsx()
    elif osString == winString:
        imageWin()
        
    
def pack():
    if osString == linString:
        packLinux()
    elif osString == osxString:
        packOsx()
    elif osString == winString:
        packWin()
    
def dist():
    if osString == linString:
        distLinux()
    elif osString == osxString:
        distOsx()
    elif osString == winString:
        distWin()


#######################
# LINUX FUNCTIONALITY
#######################

def buildLinux():
    print('cleaning/testing/compiling...')
    os.system('mvn clean package')
    
def cleanLinux():
    print('cleaning build paths...')
    os.system('rm -rf target/mods')
    os.system('rm -rf build')
    
def imageLinux():
    print('creating jmod based on jar built without dependencies...')
    os.system('mkdir target/mods')
    os.system('$JAVA_HOME/bin/jmod create ' +
        '--class-path target/' + JAR_WO_DEP + ' ' +
        '--main-class org.darisadesigns.polyglotlina.PolyGlot target/mods/PolyGlot.jmod')

    print('creating runnable image...')
    command = ('$JAVA_HOME/bin/jlink ' +
        '--module-path "module_injected_jars/:' +
        'target/mods:' +
        JAVAFX_LOCATION_LINUX + '/javafx-graphics/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_LINUX + '/javafx-base/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_LINUX + '/javafx-media/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_LINUX + '/javafx-swing/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_LINUX + '/javafx-controls/' + JAVAFX_VER + '/:' +
        '$JAVA_HOME/jmods" ' +
        '--add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" ' +
        '--output "build/image/" ' +
        '--compress=2 ' +
        '--launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot')

    os.system(command)
    
def packLinux():
    print("packing Linux app...")
    os.system('rm -rf appimage')

    command = (JAVA_PACKAGER_LOCATION_LINUX + '/jpackage ' +
        '--runtime-image build/image ' +
        '--output appimage ' +
        '--name PolyGlot ' +
        '--module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot ' +
        '--copyright "2014-2019 Draque Thompson" ' +
        '--description "PolyGlot is a spoken language construction toolkit." ' +
        '--icon packaging_files/PolyGlot0.png') 
        # adding version number in Linux with modular build currently broken in jpackage. Check back after J14 release...
        # '--app-version ' + POLYGLOT_VERSION)

    os.system(command)
    
def distLinux():
    print('Creating distribution deb...')
    os.system('rm -rf installer')
    os.system('mkdir installer')
    command = (JAVA_PACKAGER_LOCATION_LINUX + '/jpackage ' +
        '--package-type deb ' +
        '--file-associations packaging_files/linux/file_types_linux.prop ' +
        '--runtime-image build/image ' +
        '--output installer ' +
        '--name PolyGlot ' +
        '--module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot ' +
        '--copyright "2014-2019 Draque Thompson" ' +
        '--description "PolyGlot is a spoken language construction toolkit." ' +
        '--icon packaging_files/PolyGlot0.png')

    os.system(command)


#######################
# OSX FUNCTIONALITY
#######################

def buildOsx():
    print('cleaning/testing/compiling...')
    os.system('mvn clean package')
    
def cleanOsx():
    print('cleaning build paths...')
    os.system('rm -rf target/mods')
    os.system('rm -rf build')
    
def imageOsx():
    print('creating jmod based on jar built without dependencies...')
    os.system('mkdir target/mods')
    os.system('$JAVA_HOME/bin/jmod create ' +
        '--class-path target/' + JAR_WO_DEP + ' ' +
        '--main-class org.darisadesigns.polyglotlina.PolyGlot target/mods/PolyGlot.jmod')

    print('creating runnable image...')
    os.system('$JAVA_HOME/bin/jlink ' +
        '--module-path "module_injected_jars/:' +
        'target/mods:' +
        JAVAFX_LOCATION_OSX + '/javafx-graphics/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_OSX + '/javafx-base/' + JAVAFX_VER + '/:'+
        JAVAFX_LOCATION_OSX + '/javafx-media/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_OSX + '/javafx-swing/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_OSX + '/javafx-controls/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_OSX + '/jmods" ' +
        '--add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" ' +
        '--output "build/image/" ' +
        '--compress=2 ' +
        '--launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot')
    
def packOsx():
    print("Packing mac app...")
    os.system('rm -rf appimage')
    command = (JAVA_PACKAGER_LOCATION_OSX + '/jpackage ' +
        '--runtime-image build/image --output appimage ' +
        '--name PolyGlot ' +
        '--module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot ' +
        '--copyright "2014-2019 Draque Thompson" ' +
        '--description "PolyGlot is a spoken language construction toolkit." ' +
        '--mac-bundle-identifier "PolyGlot" ' +
        '--mac-bundle-name "PolyGlot" ' +
        '--file-associations packaging_files/mac/file_types_mac.prop ' +
        '--icon packaging_files/mac/PolyGlot.icns ' +
        '--app-version "' + POLYGLOT_VERSION + '"')

    os.system(command)
    
def distOsx():
    print('Creating distribution package...')
    os.system('rm -rf installer')
    os.system('mkdir installer')
    # if this does not work correctly: brew install create-dmg
    os.system('create-dmg ' +
        '--volname "PolyGlot Installer" ' +
        '--volicon "packaging_files/mac/PolyGlot.icns" ' +
        '--app-drop-link 450 250 ' +
        '--hide-extension "PolyGlot.app" ' +
        '--background "packaging_files/mac/bg.png" ' +
        '--window-pos 200 120 ' +
        '--window-size 650 591 ' +
        '--icon-size 120 ' +
        '--icon "PolyGlot.app" 200 250 ' +
        '"installer/PolyGlot-Ins.dmg" ' +
        '"appimage/"')


#######################
# WINDOWS FUNCTIONALITY
#######################

def buildWin():
    print(__name__)
    
def cleanWin():
    print(__name__)
    
def imageWin():
    print(__name__)
    
def packWin():
    print(__name__)
    
def distWin():
    print(__name__)

if __name__ == "__main__":
    main(sys.argv)
    