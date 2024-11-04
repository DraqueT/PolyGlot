#!/usr/bin/python3
"""
This script builds PolyGlot into a distributable package on Linux,
OSX, and Windows.
"""

__author__      = "Draque Thompson"
__copyright__   = "2019-2024"
__license__     = "MIT"
__maintainer__  = "draquemail@gmail.com"
__status__      = "Production"

import datetime
from datetime import date
import os
from os import path
import platform
import shutil
import sys
import time
import uuid
from xml.dom import minidom

buildResult = ''
copyDestination = ''
failFile = ''
osString = platform.system()

linString = 'Linux'
osxString = 'Darwin'
winString = 'Windows'

separatorCharacter = '/'
macIntelBuild = False
skipTests = False

###############################
# LINUX BUILD CONSTANTS
LIN_INS_NAME = 'PolyGlot-Ins-Lin.deb'

###############################
# OSX BUILD CONSTANTS
OSX_INS_NAME = 'PolyGlot-Ins-Osx.dmg'
OSX_INTEL_INS_NAME = 'PolyGlot-Ins-Osx-intel.dmg'
SIGN_IDENTITY = ''  # set in main for timing reasons
DISTRIB_IDENTITY = ''  # set in main for timing reasons

###############################
# WINDOWS BUILD CONSTANTS
WIN_INS_NAME = 'PolyGlot-Ins-Win.exe'

###############################
# UNIVERSAL BUILD CONSTANTS
# You will not need to change these
JAR_W_DEP = ''  # set in main for timing reasons
JAR_WO_DEP = ''  # set in main for timing reasons
JAVAFX_VER = ''  # set in main for timing reasons
JACKSON_VER = ''  # set in main for timing reasons
POLYGLOT_VERSION = ''  # set in main for timing reasons
POLYGLOT_BUILD = ''  # set in main for timing reasons
JAVA_HOME = ''  # set in main for timing reasons
IS_RELEASE = False
CUR_YEAR = str(date.today().year)


######################################
#   PLATFORM AGNOSTIC FUNCTIONALITY
######################################

def main(args):
    global POLYGLOT_VERSION
    global POLYGLOT_BUILD
    global JAVA_HOME
    global SIGN_IDENTITY
    global DISTRIB_IDENTITY
    global IS_RELEASE
    global JAR_W_DEP
    global JAR_WO_DEP
    global JAVAFX_VER
    global JACKSON_VER
    global JSOUP_VER
    global LANG3_VER
    global failFile
    global copyDestination
    global separatorCharacter
    global macIntelBuild
    global skipTests

    if 'help' in args or '-help' in args or '--help' in args:
        printHelp()
        return

    skip_steps = []

    if osString == winString:
        separatorCharacter = '\\'

    if '-skipTests' in args or '-skiptests' in args:
        skipTests = True
        command_index = args.index('-skipTests') if '-skipTests' in args else args.index('-skiptests')
        del args[command_index]

    # gather list of steps marked to be skipped
    while '-skip' in args:
        command_index = args.index('-skip')
        skip_steps.append(args[command_index + 1])

        print("Skipping: " + args[command_index + 1] + " step.")

        # remove args after consuming
        del args[command_index + 1]
        del args[command_index]

    # allows specifying code signing identity for mac builds
    if '-mac-sign-identity' in args:
        command_index = args.index('-mac-sign-identity')
        SIGN_IDENTITY = args[command_index + 1]

        # remove args after consuming
        del args[command_index + 1]
        del args[command_index]

    # allows specifying code signing for mac distribution
    if '-mac-distrib-cert' in args:
        command_index = args.index('-mac-distrib-cert')
        DISTRIB_IDENTITY = args[command_index + 1]

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

    if JAVA_HOME is None:
        print('JAVA_HOME must be set. If necessary, use -java-home-o command to override')
        return

    POLYGLOT_VERSION = getVersion()
    POLYGLOT_BUILD = getBuildNum()
    print('Building Version: ' + POLYGLOT_VERSION)
    updateVersionResource(POLYGLOT_VERSION)
    JAR_W_DEP = 'PolyGlotLinA-' + POLYGLOT_VERSION + '-jar-with-dependencies.jar'
    JAR_WO_DEP = 'PolyGlotLinA-' + POLYGLOT_VERSION + '.jar'
    JAVAFX_VER = getDependencyVersionByGroupId('org.openjfx')
    JACKSON_VER = getDependencyVersionByGroupId('com.fasterxml.jackson.core')
    JSOUP_VER = getDependencyVersionByGroupId('org.jsoup')
    LANG3_VER = getDependencyVersionByGroupIdAndName('org.apache.commons', 'commons-lang3')

    if osString == winString:
        os.system('echo off')
    elif osString == osxString and '-intelBuild' in args:
        macIntelBuild = True
        command_index = args.index('-intelBuild')
        del args[command_index]

    full_build = (len(args) == 1)  # length of 1 means no arguments (full build)

    if (full_build and 'docs' not in skip_steps) or 'docs' in args:
        injectDocs()
    if (full_build and 'build' not in skip_steps) or 'build' in args:
        build()
    if (full_build and 'clean' not in skip_steps) or 'clean' in args or 'image' in args:
        clean()
    if (full_build and 'image' not in skip_steps) or 'image' in args:
        image()
    if (full_build and 'dist' not in skip_steps) or 'dist' in args:
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
    global macIntelBuild

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
    global skipTests
    print('cleaning/testing/compiling...')
    command = 'mvn clean package'

    if skipTests:
        command += ' -DskipTests'

    os.system(command)


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
              '--main-class org.darisadesigns.polyglotlina.Desktop.PolyGlot target/mods/PolyGlot.jmod')

    javafx_location = getJfxLocation()
    repo_location = getRepositoryLocation()

    print('creating runnable image...')
    command = (JAVA_HOME + '/bin/jlink ' +
               '--module-path "module_injected_jars/:' +
               'target/mods:' +
               javafx_location + '/javafx-graphics/' + JAVAFX_VER + '/:' +
               javafx_location + '/javafx-base/' + JAVAFX_VER + '/:' +
               javafx_location + '/javafx-media/' + JAVAFX_VER + '/:' +
               javafx_location + '/javafx-swing/' + JAVAFX_VER + '/:' +
               javafx_location + '/javafx-controls/' + JAVAFX_VER + '/:' +
               repo_location + '/com/fasterxml/jackson/core/jackson-core/' + JACKSON_VER + '/:' +
               repo_location + '/com/fasterxml/jackson/core/jackson-databind/' + JACKSON_VER + '/:' +
               repo_location + '/com/fasterxml/jackson/core/jackson-annotations/' + JACKSON_VER + '/:' +
               repo_location + '/org/jsoup/jsoup/' + JSOUP_VER + '/:' +
               repo_location + '/org/apache/commons/commons-lang3/' + LANG3_VER + '/:' +
               JAVA_HOME + '/jmods" ' +
               '--add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" ' +
               '--output "build/image/" ' +
               '--compress=2 ' +
               '--launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot')

    os.system(command)


def distLinux():
    print('creating linux distribution...')
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
    if os.system('command -v rpm') == 0:
        print('detected rpm')
        command = command + ' --linux-rpm-license-type MIT '
    os.system(command)

    # search for the generated system package
    # (jpackage is inconsistent with how it chooses to name packages
    # on different architectures)
    installer_file = ''
    for item in os.listdir():
        if os.path.isfile(item) and item.startswith('polyglot-linear-a') and POLYGLOT_BUILD in item:
            installer_file = item
            print('generated: ' + installer_file)
            break
    if installer_file == '':
        print('failed to locate jpackage output')

    if copyDestination != "":
        copyInstaller(installer_file)


######################################
#       Mac OS FUNCTIONALITY
######################################

def buildOsx():
    global skipTests
    print('cleaning/testing/compiling...')
    command = 'mvn clean package'

    if skipTests:
        command += ' -DskipTests'

    os.system(command)


def cleanOsx():
    print('cleaning build paths...')
    os.system('rm -rf target/mods')
    os.system('rm -rf build')


def imageOsx():
    print('creating jmod based on jar built without dependencies...')
    os.system('mkdir target/mods')
    os.system(JAVA_HOME + '/bin/jmod create ' +
              '--class-path target/' + JAR_WO_DEP + ' ' +
              '--main-class org.darisadesigns.polyglotlina.Desktop.PolyGlot target/mods/PolyGlot.jmod')

    print('creating runnable image...')
    repo_location = getRepositoryLocation()
    command = (JAVA_HOME + '/bin/jlink ' +
               '--module-path "module_injected_jars/:' +
               'target/mods:' +
               repo_location + '/com/fasterxml/jackson/core/jackson-core/' + JACKSON_VER + '/:' +
               repo_location + '/com/fasterxml/jackson/core/jackson-databind/' + JACKSON_VER + '/:' +
               repo_location + '/com/fasterxml/jackson/core/jackson-annotations/' + JACKSON_VER + '/:' +
               repo_location + '/org/jsoup/jsoup/' + JSOUP_VER + '/:' +
               repo_location + '/org/apache/commons/commons-lang3/' + LANG3_VER + '/:' +
               getJfxTargetModsOsx() +
               '--add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" ' +
               '--output "build/image/" ' +
               '--compress=2 ' +
               '--launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot')

    os.system(command)


def getJfxTargetModsOsx():
    javafx_location = getJfxLocation()
    return (
            javafx_location + '/javafx-graphics/' + JAVAFX_VER + '/:' +
            javafx_location + '/javafx-base/' + JAVAFX_VER + '/:' +
            javafx_location + '/javafx-media/' + JAVAFX_VER + '/:' +
            javafx_location + '/javafx-swing/' + JAVAFX_VER + '/:' +
            javafx_location + '/javafx-controls/' + JAVAFX_VER + '/:' +
            javafx_location + '/jmods" '
    )


def distOsx():
    print('Creating app image...')

    command = (JAVA_HOME + '/bin/jpackage ' +
               '--runtime-image build/image ' +
               '--icon "PolyGlot.app" ' +
               '--name PolyGlot ' +
               '--module org.darisadesigns.polyglotlina.polyglot/org.darisadesigns.polyglotlina.PolyGlot ' +
               '--copyright "2014-' + CUR_YEAR + ' Draque Thompson" ' +
               '--description "PolyGlot is a spoken language construction toolkit." ' +
               '--type app-image ' +
               '--mac-package-name "PolyGlot" ' +
               '--file-associations packaging_files/mac/file_types_mac.prop ' +
               '--icon packaging_files/mac/PolyGlot.icns ' +
               '--app-version "' + POLYGLOT_VERSION + '"')

    os.system(command)

    # Remove the extra copy of libjli.dylib which causes notarization to fail
    if path.exists('PolyGlot.app/Contents/runtime/Contents/MacOS/libjli.dylib'):
        os.remove('PolyGlot.app/Contents/runtime/Contents/MacOS/libjli.dylib')

    if SIGN_IDENTITY and not DISTRIB_IDENTITY:  # only sign with dev identity
        print('Code signing app image with developer certificate...')
        command = ('codesign ' +
                   '--force ' +  # Overwrite existing signature
                   '--timestamp ' +  # Embed secure timestamps
                   '--options runtime ' +  # Enable hardened runtime
                   '--entitlements packaging_files/mac/entitlements.plist ' +  # Add entitlements
                   '--sign "' + SIGN_IDENTITY + '" ' +
                   'PolyGlot.app')

        os.system(command)
    elif not DISTRIB_IDENTITY:
        print('No code signing identity specified, app image will not be signed as developer')

    if DISTRIB_IDENTITY:
        print('Code signing app image with distribution certificate...')
        command = ('codesign ' +
                   '--force ' +  # Overwrite existing signature
                   '--timestamp ' +  # Embed secure timestamps
                   '--options runtime ' +  # Enable hardened runtime
                   '--entitlements packaging_files/mac/entitlements.plist ' +  # Add entitlements
                   '--sign "' + DISTRIB_IDENTITY + '" ' +
                   'PolyGlot.app')

        os.system(command)
    else:
        print('No distribution signing identity specified, app image will not be signed for distribution')

    polyglot_dmg = 'PolyGlot-' + POLYGLOT_VERSION + '.dmg'

    try:
        print('Creating distribution package...')
        command = ('dmgbuild ' +
                   '-s packaging_files/mac/dmg_settings.py PolyGlot ' + polyglot_dmg)

        os.system(command)

        if DISTRIB_IDENTITY:
            print('Code signing dmg installer image with distribution certificate...')
            command = ('codesign ' +
                       '--timestamp ' +  # Embed secure timestamps
                       '--entitlements packaging_files/mac/entitlements.plist ' +  # Add entitlements
                       '--sign "' + DISTRIB_IDENTITY + '" ' + polyglot_dmg)

            os.system(command)
        else:
            print('No distribution signing identity specified, dmg installer will not be signed for distribution')

        if copyDestination != "":
            copyInstaller('PolyGlot-' + POLYGLOT_VERSION + '.dmg')

    except Exception as e:
        print('Exception: ' + str(e))
        print('\'dmgbuild\' does not exist in PATH, distribution packaging will be skipped')
        print('Run \'pip install dmgbuild\' to install it')

    # cleanup created app
    if path.exists('PolyGlot.app'):
        shutil.rmtree('PolyGlot.app')


######################################
#       WINDOWS FUNCTIONALITY
######################################

def buildWin():
    global skipTests
    print('cleaning/testing/compiling...')
    command = 'mvn clean package'

    if skipTests:
        command += ' -DskipTests'

    os.system(command)


def cleanWin():
    print('cleaning build paths...')
    os.system('rmdir target\\mods /s /q')
    os.system('rmdir build /s /q')


def imageWin():
    print('creating jmod based on jar built without dependencies...')
    os.system('mkdir target\\mods')
    os.system('jmod create ' +
              '--class-path target\\' + JAR_WO_DEP +
              ' --main-class org.darisadesigns.polyglotlina.Desktop.PolyGlot ' +
              'target\\mods\\PolyGlot.jmod')

    javafx_location = getJfxLocation()
    repo_location = getRepositoryLocation()

    print('creating runnable image...')
    command = ('jlink ' +
               '--module-path "module_injected_jars;' +
               'target\\mods;' +
               javafx_location + '\\javafx-graphics\\' + JAVAFX_VER + ';' +
               javafx_location + '\\javafx-base\\' + JAVAFX_VER + ';' +
               javafx_location + '\\javafx-media\\' + JAVAFX_VER + ';' +
               javafx_location + '\\javafx-swing\\' + JAVAFX_VER + ';' +
               javafx_location + '\\javafx-controls\\' + JAVAFX_VER + ';' +
               repo_location + '\\com\\fasterxml\\jackson\\core\\jackson-core\\' + JACKSON_VER + ';' +
               repo_location + '\\com\\fasterxml\\jackson\\core\\jackson-databind\\' + JACKSON_VER + ';' +
               repo_location + '\\com\\fasterxml\\jackson\\core\\jackson-annotations\\' + JACKSON_VER + ';' +
               repo_location + '\\org\\jsoup\\jsoup\\' + JSOUP_VER + ';' +
               repo_location + '\\org\\apache\\commons\\commons-lang3\\' + LANG3_VER + ';' +
               '%JAVA_HOME%\\jmods" ' +
               '--add-modules "org.darisadesigns.polyglotlina.polyglot","jdk.crypto.ec" ' +
               '--output "build\\image" ' +
               '--compress=2 ' +
               '--launcher PolyGlot=org.darisadesigns.polyglotlina.polyglot')
    os.system(command)


def distWin():
    package_location = 'PolyGlot-' + POLYGLOT_BUILD + '.exe'
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
               '--win-upgrade-uuid  ' + str(
                uuid.uuid4()) + ' ' +  # Unique identifier to keep versioned installers from erroring in Windows
               '--icon packaging_files/win/PolyGlot0.ico')

    os.system(command)

    if copyDestination != "":
        copyInstaller(package_location)


# injects current time into file which lives in PolyGlot resources
def injectBuildDate():
    build_time = datetime.datetime.now().strftime('%Y-%m-%d %H:%M')
    file_path = 'assets/assets/org/DarisaDesigns/buildDate'

    if osString == winString:
        file_path = file_path.replace('/', '\\')

    f = open(file_path, 'w')
    f.write(build_time)
    f.close()


####################################
#       UTIL FUNCTIONALITY
####################################

# handled here for timing reasons...
def getJfxLocation():
    ret = os.path.expanduser('~')

    if osString == winString:
        ret += '\\.m2\\repository\\org\\openjfx'
    elif osString == osxString and macIntelBuild:
        ret += '/.m2/repository/org/openjfx_intel'
    elif osString == osxString or osString == linString:
        ret += '/.m2/repository/org/openjfx'

    return ret


def getRepositoryLocation():
    ret = os.path.expanduser('~')
    if osString == winString:
        ret += '\\.m2\\repository'
    elif osString == osxString or osString == linString:
        ret += '/.m2/repository'
    return ret


def getDependencyVersionByGroupId(group_id):
    ret = ''
    doc = minidom.parse('pom.xml')
    dependencies = doc.getElementsByTagName('dependency')

    for dependency in dependencies:
        if dependency.getElementsByTagName('groupId')[0].childNodes[0].nodeValue == group_id:
            ret = dependency.getElementsByTagName('version')[0].childNodes[0].nodeValue
            break
    return ret

def getDependencyVersionByGroupIdAndName(group_id, artifact_id):
    ret = ''
    doc = minidom.parse('pom.xml')
    dependencies = doc.getElementsByTagName('dependency')

    for dependency in dependencies:
        group = dependency.getElementsByTagName('groupId')
        artifact = dependency.getElementsByTagName('artifactId')
        if group[0].childNodes[0].nodeValue == group_id and artifact[0].childNodes[0].nodeValue == artifact_id:
            ret = dependency.getElementsByTagName('version')[0].childNodes[0].nodeValue
            break
    return ret

# fetches version of PolyGlot from pom file
def getVersion():
    doc = minidom.parse('pom.xml')
    version_items = doc.getElementsByTagName('version')

    return version_items[0].firstChild.data


# for releases, this will match the version. For beta builds, a UTC timestamp is appended (OS registration reasons on
# installation)
def getBuildNum():
    ret = getVersion()

    if not IS_RELEASE and osString == winString:
        # truncate build from version string if present
        if ret.count('.') > 1:
            ret = ret[0:ret.rfind('.')]

        if osString == winString:
            # windows has max build num of 65535
            auto_build_num = int(time.time())  # base build on system time
            auto_build_num = auto_build_num / 100  # truncate by 100 seconds (max one build per 16 minutes 40 seconds)
            auto_build_num = int(auto_build_num % 65535)  # reduce build to number between 0 - 65534
            ret = ret + '.' + str(auto_build_num)
        else:
            ret = ret + '.' + str(int(time.time()))

    return ret


def updateVersionResource(version_string):
    global IS_RELEASE

    if osString == winString:
        location = 'assets\\assets\\org\\DarisaDesigns\\version'
    else:
        location = 'assets/assets/org/DarisaDesigns/version'

    if path.exists(location):
        os.remove(location)

    with open(location, 'w+') as versionFile:
        if IS_RELEASE:
            versionFile.write(version_string)
        else:
            versionFile.write(version_string + 'B')


# Injects readme (with resources), example dictionaries, etc.
def injectDocs():
    print('Injecting documentation...')

    # readme and resources...
    extension = '.zip'
    if osString == winString:
        readme_location = 'assets\\assets\\org\\DarisaDesigns\\readme'
    else:
        readme_location = 'assets/assets/org/DarisaDesigns/readme'

    if path.exists(readme_location + extension):
        os.remove(readme_location + extension)

    shutil.make_archive(readme_location, 'zip', 'docs')

    # example dictionaries
    if osString == winString:
        source_location = 'packaging_files\\example_lexicons'
        dict_location = 'assets\\assets\\org\\DarisaDesigns\\exlex'
    else:
        source_location = 'packaging_files/example_lexicons'
        dict_location = 'assets/assets/org/DarisaDesigns/exlex'

    if path.exists(source_location + extension):
        os.remove(readme_location + extension)

    shutil.make_archive(dict_location, 'zip', source_location)


# Copies installer file to final destination and removes error indicator file
def copyInstaller(source):
    global copyDestination
    global macIntelBuild

    if path.exists(source):
        ins_file = ""

        if osString == winString:
            ins_file = WIN_INS_NAME
        elif osString == linString:
            ins_file = LIN_INS_NAME
        elif osString == osxString and macIntelBuild:
            ins_file = OSX_INTEL_INS_NAME
        elif osString == osxString:
            ins_file = OSX_INS_NAME

        # release candidates copied to their own location
        if IS_RELEASE:
            copyDestination = copyDestination + separatorCharacter + 'Release'

        destination = copyDestination + separatorCharacter + ins_file
        print('Copying installer to ' + destination)
        shutil.copy(source, destination)

        # only remove failure signal once process is successful
        os.remove(failFile)
        os.remove(source)
    else:
        print('FAILURE: Built installer missing: ' + source)


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
    
    -mac-sign-identity <identity> : Sign the Mac app image with the specified code signing identity.

    -copyDestination <destination-path> : sets location for the final created installer file to be copied to (ignored if distribution not built)
    
    -skip <step> : skips the given step (can be used multiple times)
    
    -release : marks build as release build. Otherwise will be build as beta

    -skipTests : skips test step in Maven

    -intelBuild : MacOS only, indicates to use intel libraries rather than Arch64 when building

Example: python build_image.py image pack -java-home-o /usr/lib/jvm/jdk-14

The above will presume that the maven build has already taken place. It will use the produced jar files to create a runnable image, then from that image, create a packed application for the platform you are currently running. The JAVA_HOME path is overridden to point to /usr/lib/jvm/jdk-14.
""")


if __name__ == "__main__":
    main(sys.argv)
