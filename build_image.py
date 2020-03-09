##############################################################################
#
#   PolyGlot build script Copyright 2019-2020 Draque Thompson
#
#   This script builds PolyGlot into a distributable package on Linux,
#   OSX, and Windows. Windows does not come with Python installed by default.
#   This runs on both Python 2.7 and 3.x. 
#
#   From: https://github.com/DraqueT/PolyGlot/
#
##############################################################################

import datetime
import platform
import os
import shutil
import subprocess
import sys
import time
import uuid
from os import path
from xml.dom import minidom
from datetime import date

buildResult = ''
copyDestination = ''
failFile = ''
osString = platform.system()

linString = 'Linux'
osxString = 'Darwin'
winString = 'Windows'

separatorCharacter = '/'


###############################
# LINUX BUILD CONSTANTS
# update the packager location for your Linux build
LIN_INS_NAME = 'PolyGlot-Ins-Lin.deb'


###############################
# OSX BUILD CONSTANTS

# update the packager location for your OSX build
OSX_INS_NAME = 'PolyGlot-Ins-Osx.dmg'


###############################
# WINDOWS BUILD CONSTANTS
# update the packager location for your Windows build
WIN_INS_NAME = 'PolyGlot-Ins-Win.exe'

###############################
# UNIVERSAL BUILD CONSTANTS
# You will not need to change these
JAR_W_DEP = '' # set in main for timing reasons
JAR_WO_DEP = '' # set in main for timing reasons
JAVAFX_VER = '' # set in main for timing reasons
POLYGLOT_VERSION = '' # set in main for timing reasons
POLYGLOT_BUILD = '' # set in main for timing reasons
JAVA_HOME = '' # set in main for timing reasons
IS_RELEASE = False
CUR_YEAR = str(date.today().year)



######################################
#   PLATFORM AGNOSTIC FUNCTIONALITY
######################################

def main(args):
    global POLYGLOT_VERSION
    global POLYGLOT_BUILD
    global JAVA_HOME
    global IS_RELEASE
    global JAR_W_DEP
    global JAR_WO_DEP
    global JAVAFX_VER
    global failFile
    global copyDestination
    global separatorCharacter
    
    skip_steps = []
    
    if osString == winString:
        separatorCharacter = '\\'

    # gather list of steps marked to be skipped
    while '-skip' in args:
        command_index = args.index('-skip')
        skip_steps.append(args[command_index + 1])
        
        print("Skipping: " + args[command_index + 1] + " step.")
        
        # remove args after consuming
        del args[command_index + 1]
        del args[command_index]
    
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

    # detects if marked for release
    if '-release' in args:
        command_index = args.index('-release')
        print('RELEASE BUILD')
        IS_RELEASE = True
        del args[command_index]
    else:
        print('BETA BUILD')
    
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
    POLYGLOT_BUILD = getBuildNum();
    print('Building Version: ' + POLYGLOT_VERSION)
    updateVersionResource(POLYGLOT_VERSION)
    JAR_W_DEP = 'PolyGlotLinA-' + POLYGLOT_VERSION + '-jar-with-dependencies.jar'
    JAR_WO_DEP = 'PolyGlotLinA-' + POLYGLOT_VERSION + '.jar'
    JAVAFX_VER = getJfxVersion()
    
    if 'help' in args or '-help' in args or '--help' in args:
        printHelp()

    if osString == winString:
        os.system('echo off')
    
    if (fullBuild and 'docs' not in skip_steps) or 'docs' in args:
        injectDocs()
    if (fullBuild and 'build' not in skip_steps) or 'build' in args:
        build()
    if (fullBuild and 'clean' not in skip_steps) or 'clean' in args or 'image' in args:
        clean()
    if (fullBuild and 'image' not in skip_steps) or 'image' in args:
        image()
    if (fullBuild and 'dist' not in skip_steps) or 'dist' in args:
        dist()
        
    print('Done!')

def build():
    print('Injecting build date/time...')
    injectBuildDate()
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

    JAVAFX_LOCATION = getJfxLocation()

    print('creating runnable image...')
    command = (JAVA_HOME + '/bin/jlink ' +
        '--module-path "module_injected_jars/:' +
        'target/mods:' +
        JAVAFX_LOCATION + '/javafx-graphics/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION + '/javafx-base/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION + '/javafx-media/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION + '/javafx-swing/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION + '/javafx-controls/' + JAVAFX_VER + '/:' +
        JAVA_HOME + '/jmods" ' +
        '--add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" ' +
        '--output "build/image/" ' +
        '--compress=2 ' +
        '--launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot')

    os.system(command)
    
def distLinux():
    print('Creating distribution deb...')
    os.system('rm -rf installer')
    os.system('mkdir installer')
    command = (JAVA_HOME + '/bin/jpackage ' +
        '--app-version ' + POLYGLOT_BUILD + ' ' +
        '--copyright "2014-' + CUR_YEAR + ' Draque Thompson" ' +
        '--description "PolyGlot is a spoken language construction toolkit." ' +
        '--file-associations packaging_files/linux/file_types_linux.prop ' +
        '--icon packaging_files/PolyGlot0.png ' +
        '--linux-package-name polyglot-linear-a ' +
        '--linux-app-category Education ' +
        '--module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot ' +
        '--name "PolyGlot" ' +
        '--license-file LICENSE.TXT ' +
        '--runtime-image build/image')
    os.system(command)
    
    if copyDestination != "":
        copyInstaller('polyglot-linear-a_' + POLYGLOT_BUILD + '-1_amd64.deb')


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

    JAVAFX_LOCATION = getJfxLocation()

    print('creating runnable image...')
    os.system(JAVA_HOME + '/bin/jlink ' +
        '--module-path "module_injected_jars/:' +
        'target/mods:' +
        JAVAFX_LOCATION + '/javafx-graphics/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION + '/javafx-base/' + JAVAFX_VER + '/:'+
        JAVAFX_LOCATION + '/javafx-media/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION + '/javafx-swing/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION + '/javafx-controls/' + JAVAFX_VER + '/:' +
        JAVAFX_LOCATION + '/jmods" ' +
        '--add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" ' +
        '--output "build/image/" ' +
        '--compress=2 ' +
        '--launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot')
    
def distOsx():
    print('Creating distribution package...')
    command = (JAVA_HOME + '/bin/jpackage ' +
        '--runtime-image build/image ' +
        '--icon "PolyGlot.app" ' +
        '--name PolyGlot ' +
        '--module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot ' +
        '--copyright "2014-' + CUR_YEAR + ' Draque Thompson" ' +
        '--description "PolyGlot is a spoken language construction toolkit." ' +
        '--mac-package-name "PolyGlot" ' +
        '--file-associations packaging_files/mac/file_types_mac.prop ' +
        '--icon packaging_files/mac/PolyGlot.icns ' +
        '--license-file LICENSE.TXT ' +
        '--app-version "' + POLYGLOT_BUILD + '"')

    os.system(command)
      
    if copyDestination != "":
        copyInstaller('PolyGlot-' + POLYGLOT_BUILD + '.dmg')


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
    os.system('jmod create ' +
        '--class-path target\\' + JAR_WO_DEP +
        ' --main-class org.darisadesigns.polyglotlina.PolyGlot ' +
        'target\mods\PolyGlot.jmod')

    JAVAFX_LOCATION = getJfxLocation()

    print('creating runnable image...')
    command = ('jlink ' +
        '--module-path "module_injected_jars;' +
        'target\\mods;' +
        JAVAFX_LOCATION + '\\javafx-graphics\\' + JAVAFX_VER + ';' +
        JAVAFX_LOCATION + '\\javafx-base\\' + JAVAFX_VER + ';' +
        JAVAFX_LOCATION + '\\javafx-media\\' + JAVAFX_VER + ';' +
        JAVAFX_LOCATION + '\\javafx-swing\\' + JAVAFX_VER + ';' +
        JAVAFX_LOCATION + '\\javafx-controls\\' + JAVAFX_VER + ';' +
        '%JAVA_HOME%\jmods" ' +
        '--add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" ' +
        '--output "build\image" ' +
        '--compress=2 ' +
        '--launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot')
    os.system(command)

def distWin():
    packageLocation = 'PolyGlot-' + POLYGLOT_VERSION + '.exe'
    print('Creating distribution package...')
    os.system('rmdir /s /q installer')

    # If missing, install WiX Toolset: https://wixtoolset.org/releases/
    command = ('jpackage ' + 
        '--runtime-image build\\image ' +
        '--win-shortcut ' +
        '--win-menu ' +
        '--win-dir-chooser ' +
        '--file-associations packaging_files\\win\\file_types_win.prop ' +
        '--name PolyGlot ' +
        '--module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot ' +
        '--copyright "2014-' + CUR_YEAR + ' Draque Thompson" ' +
        '--description "PolyGlot is a spoken language construction toolkit." ' +
        '--app-version "' + POLYGLOT_BUILD + '" ' +
        '--license-file LICENSE.TXT ' +
        '--win-upgrade-uuid  ' + str(uuid.uuid4()) + ' ' + # Unique identifier to keep versioned installers from erroring in Windows
        '--icon packaging_files/win/PolyGlot0.ico')

    os.system(command)
    
    if copyDestination != "":
        copyInstaller(packageLocation)

# injects current time into file which lives in PolyGlot resources
def injectBuildDate():
    buildTime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M')
    filePath = 'assets/assets/org/DarisaDesigns/buildDate'
    
    if (osString == winString):
        filePath = filePath.replace('/', '\\')
    
    f = open(filePath, 'w')
    f.write(buildTime)
    f.close()

####################################
#       UTIL FUNCTIONALITY
####################################

# handled here for timing reasons...
def getJfxLocation():
    ret = os.path.expanduser('~')

    if (osString == winString):
        ret += '\\.m2\\repository\\org\\openjfx'
    elif (osString == osxString or osString == linString):
        ret += '/.m2/repository/org/openjfx'

    return ret

# What it says on the tin
def getJfxVersion():
    ret = ''
    mydoc = minidom.parse('pom.xml')
    dependencies = mydoc.getElementsByTagName('dependency')
    
    for dependency in dependencies:
        if (dependency.getElementsByTagName('groupId')[0].childNodes[0].nodeValue == 'org.openjfx'):
            ret = dependency.getElementsByTagName('version')[0].childNodes[0].nodeValue
            break
    return ret

# fetches version of PolyGlot from pom file
def getVersion():
    mydoc = minidom.parse('pom.xml')
    versionItems = mydoc.getElementsByTagName('version')
    
    return versionItems[0].firstChild.data

# for releases, this will match the version. For beta builds, a UTC timestamp is appended (OS registration reasons on install)
def getBuildNum():
    ret = getVersion()
    
    if not IS_RELEASE:
        ret = ret + '_' + str(int(time.time()))

    return ret

def updateVersionResource(versionString):
    global IS_RELEASE
    
    if osString == winString:
        location = 'assets\\assets\\org\\DarisaDesigns\\version'
    else:
        location = 'assets/assets/org/DarisaDesigns/version'
    
    if path.exists(location):
        os.remove(location)
    
    with open(location, 'w+') as versionFile:
        if IS_RELEASE:
            versionFile.write(versionString)
        else:
            versionFile.write(versionString + 'B')

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
    else:
        print('Built installer missing: ' + source)

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
    
    dist : Creates distribution files for the application. This is platform dependent. Produced files stored in the installer folder.

    -java-home-o <jdk-path> : Overrides JAVA_HOME. Useful for stubborn VMs that will not normally recognize environment variables.
    
    -copyDestination <destination-path> : sets location for the final created installer file to be copied to (ignored if distribution not built)
    
    -skip <step> : skips the given step (can be used multiple times)
    
    -release: marks build as release build. Otherwise will be build as beta

Example: python build_image.py image pack -java-home-o /usr/lib/jvm/jdk-14

The above will presume that the maven build has already taken place. It will use the produced jar files to create a runnable image, then from that image, create a packed application for the platform you are currently running. The JAVA_HOME path is overridden to point to /usr/lib/jvm/jdk-14.
""")

if __name__ == "__main__":
    main(sys.argv)
    
