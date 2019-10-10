##############################################################################
#
#   PolyGlot build script Copyright 2019 Draque Thompson
#
#   This script builds PolyGlot into a distributable package on Linux,
#   OSX, and Windows. Windows does not come with Python installed by default.
#   This runs on both Python 2.7 and 3.x. 
#
#   From: https://github.com/DraqueT/PolyGlot/
#
##############################################################################

import platform
import sys
import os
from os import path
import shutil

osString = platform.system()
linString = 'Linux'
osxString = 'Darwin'
winString = 'Windows'
copyDestination = ''
failFile = ''
separatorCharacter = '/'


###############################
# LINUX BUILD CONSTANTS
# update the JFX and packager locations for your Linux build

JAVAFX_LOCATION_LINUX = "/home/polyglot/.m2/repository/org/openjfx"
JAVA_PACKAGER_LOCATION_LINUX = "/usr/lib/jvm/jdk-14/bin" # this will go away once Java 14 drops officially...
LIN_INS_NAME = 'PolyGlot-Ins-Lin.db'


###############################
# OSX BUILD CONSTANTS

# update the JFX and packager locations for your OSX build
JAVAFX_LOCATION_OSX = "/Users/draque/.m2/repository/org/openjfx"
JAVA_PACKAGER_LOCATION_OSX = "/Users/draque/NetBeansProjects/jdk_14_packaging/Contents/Home/bin" # this will go away once Java 14 drops officially...
OSX_INS_NAME = 'PolyGlot-Ins-Osx.dmg'


###############################
# WINDOWS BUILD CONSTANTS
# update the JFX and packager locations for your Windows build

JAVAFX_LOCATION_WIN = 'C:\\Users\\user\\.m2\\repository\\org\\openjfx'
JAVA_PACKAGER_LOCATION_WIN = 'C:\\Java\\jdk-14\\bin'  # this will go away once Java 14 drops officially...
WIN_INS_NAME = 'PolyGlot-Ins-Win.exe'

###############################
# UNIVERSAL BUILD CONSTANTS
# You will not need to change these
JAR_W_DEP = "PolyGlotLinA-3.0-jar-with-dependencies.jar"
JAR_WO_DEP = "PolyGlotLinA-3.0.jar"
JAVAFX_VER = "12.0.2"
POLYGLOT_VERSION = '' # set in main for timing reasons
JAVA_HOME = '' # set in main for timing reasons



######################################
#   PLATFORM AGNOSTIC FUNCTIONALITY
######################################

def main(args):
    global POLYGLOT_VERSION
    global JAVA_HOME
    global failFile
    global copyDestination
    global separatorCharacter
    
    if osString == winString:
        separatorCharacter = '\\'

    # allows for override of java home (virtual environments make this necessary at times)
    if '-java-home-o' in args:
        command_index = args.index('-java-home-o')
        print('JAVA_HOME overriden to: ' + args[command_index + 1])
        JAVA_HOME = args[command_index + 1]
        
        # remove args after consuming
        del args[command_index + 1]
        del args[command_index]
    else:
        JAVA_HOME = os.getenv('JAVA_HOME')

    if not JAVA_HOME is not None:
        print('JAVA_HOME must be set. If necessary, use -java-home-o command to override')
        return
        
    if '-copyDestination' in args:
        command_index = args.index('-copyDestination')
        print('Destination for final install file set to: ' + args[command_index + 1])
        copyDestination = args[command_index + 1]
        
        # failure message file created here, deleted at end of process conditionally upon success
        failFile = copyDestination + separatorCharacter + osString + "_BUILD_FAILED"
        open(failFile, 'a').close()
        
        # remove args after consuming
        del args[command_index + 1]
        del args[command_index]

    fullBuild = (len(args) == 1) # length of 1 means no arguments (full build)
    POLYGLOT_VERSION = getVersion()
    
    if 'help' in args or '-help' in args or '--help' in args:
        printHelp()

    if osString == winString:
        os.system('echo off')
    
    if fullBuild or 'docs' in args:
        injectDocs()
    if fullBuild or 'build' in args:
        build()
    if fullBuild or 'clean' in args or "image" in args:
        clean()
    if fullBuild or 'image' in args:
        image()
    if fullBuild or 'pack' in args:
        pack()
    if fullBuild or 'dist' in args:
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


######################################
#       LINUX FUNCTIONALITY
######################################

def buildLinux():
    print('cleaning/testing/compiling...')
    os.system('mvn clean package')
    
def cleanLinux():
    print('cleaning build paths...')
    os.system('rm -rf target/mods')
    os.system('rm -rf build')
    
def imageLinux():
    print('POLYGLOT_VERSION: ' + POLYGLOT_VERSION)
    print('creating jmod based on jar built without dependencies...')
    os.system('mkdir target/mods')
    os.system(JAVA_HOME + '/bin/jmod create ' +
        '--class-path target/' + JAR_WO_DEP + ' ' +
        '--main-class org.darisadesigns.polyglotlina.PolyGlot target/mods/PolyGlot.jmod')

    print('creating runnable image...')
    command = (JAVA_HOME + '/bin/jlink ' +
        '--module-path "module_injected_jars/:' +
        'target/mods:' +
        JAVAFX_LOCATION_LINUX + '/javafx-graphics/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_LINUX + '/javafx-base/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_LINUX + '/javafx-media/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_LINUX + '/javafx-swing/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION_LINUX + '/javafx-controls/' + JAVAFX_VER + '/:' +
        JAVA_HOME + '/jmods" ' +
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


######################################
#       OSX FUNCTIONALITY
######################################

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
    os.system(JAVA_HOME + '/bin/jmod create ' +
        '--class-path target/' + JAR_WO_DEP + ' ' +
        '--main-class org.darisadesigns.polyglotlina.PolyGlot target/mods/PolyGlot.jmod')

    print('creating runnable image...')
    os.system(JAVA_HOME + '/bin/jlink ' +
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
        '--runtime-image build/image ' +
        '--output appimage ' +
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
        
    copyInstaller('installer/PolyGlot-Ins.dmg')


######################################
#       WINDOWS FUNCTIONALITY
######################################

def buildWin():
    print('cleaning/testing/compiling...')
    os.system('mvn clean package')
    
def cleanWin():
    print('cleaning build paths...')
    os.system('rmdir target\mods /s /q')
    os.system('rmdir build /s /q')
    
def imageWin():
    print('creating jmod based on jar built without dependencies...')
    os.system('mkdir target\mods')
    os.system(JAVA_PACKAGER_LOCATION_WIN + '\\jmod create ' +
        '--class-path target\\' + JAR_WO_DEP +
        ' --main-class org.darisadesigns.polyglotlina.PolyGlot ' +
        'target\mods\PolyGlot.jmod')

    print('creating runnable image...')
    command = ('%JAVA_HOME%\\bin\\jlink ' +
        '--module-path "module_injected_jars;' +
        'target\\mods;' +
        JAVAFX_LOCATION_WIN + '\\javafx-graphics\\' + JAVAFX_VER + ';' +
        JAVAFX_LOCATION_WIN + '\\javafx-base\\' + JAVAFX_VER + ';' +
        JAVAFX_LOCATION_WIN + '\\javafx-media\\' + JAVAFX_VER + ';' +
        JAVAFX_LOCATION_WIN + '\\javafx-swing\\' + JAVAFX_VER + ';' +
        JAVAFX_LOCATION_WIN + '\\javafx-controls\\' + JAVAFX_VER + ';' +
        '%JAVA_HOME%\jmods" ' +
        '--add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" ' +
        '--output "build\image" ' +
        '--compress=2 ' +
        '--launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot')
    os.system(command)

def packWin():
    print('Packing Windows app...')
    os.system('rmdir /s /q appimage')
    command = (JAVA_PACKAGER_LOCATION_WIN + '\\jpackage ' +
        '--runtime-image build\\image ' +
        '--output appimage ' +
        '--name PolyGlot ' +
        '--module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot ' +
        '--copyright "2014-2019 Draque Thompson" ' +
        '--description "PolyGlot is a spoken language construction toolkit." ' +
        '--icon packaging_files/win/PolyGlot0.ico ' +
        '--app-version "' + POLYGLOT_VERSION + '"')
    os.system(command)

def distWin():
    print('Creating distribution package...')
    os.system('rmdir /s /q installer')
    # If this does not work correctly, install WiX Toolset: https://wixtoolset.org/releases/
    command = (JAVA_PACKAGER_LOCATION_WIN + '\\jpackage ' + 
        '--runtime-image build\\image ' +
        '--win-shortcut ' +
        '--win-menu ' +
        '--win-dir-chooser ' +
        '--package-type exe ' +
        '--file-associations packaging_files\\win\\file_types_win.prop ' +
        '--output installer ' +
        '--name PolyGlot ' +
        '--module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot ' +
        '--copyright "2014-2019 Draque Thompson" ' +
        '--description "PolyGlot is a spoken language construction toolkit."' +
        ' --icon packaging_files/win/PolyGlot0.ico')
    os.system(command)
    
    if copyDestination != "":
        copyInstaller('installer\PolyGlot-1.0.exe')


####################################
#       UTIL FUNCTIONALITY
####################################

def getVersion():
    if osString == winString:
        location = 'assets\\assets\\org\\DarisaDesigns\\version'
    else:
        location = 'assets/assets/org/DarisaDesigns/version'

    with open(location, 'r') as myfile:
        data = myfile.read()

    return data

# Injects readme (with resources), example dictionaries, etc.
def injectDocs():
    print('Injecting documentation...')

    # readme and resources...
    extension = '.zip'
    if osString == winString:
        readmeLocation = 'assets\\assets\\org\\DarisaDesigns\\readme'
    else:
        readmeLocation = 'assets/assets/org/DarisaDesigns/readme'

    if path.exists(readmeLocation + extension):
        os.remove(readmeLocation + extension)

    shutil.make_archive(readmeLocation, 'zip', 'docs')

    # example dictionaries
    if osString == winString:
        sourceLocation = 'packaging_files\\example_lexicons'
        dictLocation = 'assets\\assets\\org\\DarisaDesigns\\exlex'
    else:
        sourceLocation = 'packaging_files/example_lexicons'
        dictLocation = 'assets/assets/org/DarisaDesigns/exlex'

    if path.exists(sourceLocation + extension):
        os.remove(readmeLocation + extension)

    shutil.make_archive(dictLocation, 'zip', sourceLocation)
    
# Copies installer file to final destination and removes error indicator file
def copyInstaller(source):
    if path.exists(source):
        if osString == winString:
            insFile = WIN_INS_NAME
        elif osString == linString:
            insFile = LIN_INS_NAME
        elif osString == osxString:
            insFile = OSX_INS_NAME

        destination = copyDestination + separatorCharacter + insFile
        print('Copying installer to ' + destination)
        shutil.copy(source, destination)
    
        # only remove failure signal once process is successful
        os.remove(failFile)

def printHelp():
    print("""
#################################################
#       PolyGlot Build Script
#################################################

To use this utility, simply execute this script with no arguments to run the entire application construction sequence. To target particular steps, use any combination of the following arguments:

    docs : Zips and injects documentation into the application assets.

    build : Performs a maven build. creates both the jar with and the jar without dependencies included. Produced files stored in the target folder.
    
    clean : Wipes the product of build.

    image : From the built jar files (which must exist), creates a runnable image. This image is platform dependent. Produced files stored in the build folder.
    
    pack : Packs the image (which must exist) into a distributable application. This is platform dependent. Produced files stored in the appimage folder.
    
    dist : Creates distribution files for the packed application (which must exist). This is platform dependent. Produced files stores in the installer folder.

    -java-home-o <jdk-path> : Overrides JAVA_HOME. Useful for stubborn VMs that will not normally recognize environment variables.
    
    -copyDestination <destination-path> : sets location for the final created installer file to be copied to (ignored if distribution not built)

Example: python build_image.py image pack -java-home-o /usr/lib/jvm/jdk-14

The above will presume that the maven build has already taken place. It will use the produced jar files to create a runnable image, then from that image, create a packed application for the platform you are currently running. The JAVA_HOME path is overridden to point to /usr/lib/jvm/jdk-14.
""")

if __name__ == "__main__":
    main(sys.argv)
    
