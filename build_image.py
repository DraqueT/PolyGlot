#!/usr/bin/python3
"""
This script builds PolyGlot into a distributable package on Linux,
OSX, and Windows.
"""

__author__      = "Draque Thompson"
__copyright__   = "2019-2025"
__license__     = "MIT"
__maintainer__  = "draquemail@gmail.com"
__status__      = "Production"

import argparse
import datetime
from datetime import date
import os
from os import path
import platform
import shutil
import subprocess
import sys
import time
from typing import Union
import uuid
from xml.dom import minidom

buildResult = ''
copyDestination = ''
failFile = ''
osString = platform.system()

linString = 'Linux'
osxString = 'Darwin'
winString = 'Windows'

macIntelBuild = False

# OSX BUILD CONSTANTS
SIGN_IDENTITY = ''  # set in main for timing reasons
DISTRIB_IDENTITY = ''  # set in main for timing reasons

###############################
# UNIVERSAL BUILD CONSTANTS
# You will not need to change these
JAR_W_DEP = ''  # set in main for timing reasons
JAR_WO_DEP = ''  # set in main for timing reasons
POLYGLOT_VERSION = ''  # set in main for timing reasons
POLYGLOT_BUILD = ''  # set in main for timing reasons
JAVA_HOME = ''  # set in main for timing reasons
CUR_YEAR = str(date.today().year)


######################################
#   PLATFORM AGNOSTIC FUNCTIONALITY
######################################

def main() -> int:
    global POLYGLOT_VERSION
    global POLYGLOT_BUILD
    global JAVA_HOME
    global SIGN_IDENTITY
    global DISTRIB_IDENTITY
    global JAR_W_DEP
    global JAR_WO_DEP
    global failFile
    global copyDestination
    global macIntelBuild

    parser = argparse.ArgumentParser(prog='PolyGlot Build Script',
        description = ('Handles builds of PolyGlot on all supported platforms. Examples:\n'
            '\tLinux:   ./build_image.py --step build --java_home_0 /usr/lib/jvm/jdk-14\n'
            '\tWindows: python3 build_image.py --skipTests\n'
            '\tOSX:     python3 build_image.py --intelBuild'),
        formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--profile', '-p', type=str,
        choices=['linux-aarch64', 'linux', 'mac-aarch64', 'mac', 'windows'],
        required=True,
        help='Maven profile to use during build')
    parser.add_argument('--step', default = [], nargs='?', action='append',
        choices=['docs', 'build', 'clean', 'dist'],
        help='Run a specific step. Leave empty to run all steps')
    parser.add_argument('--release', action='store_true',
        help='marks build as release build. Otherwise will be build as beta')
    parser.add_argument('--copyDestination', type=str,
        help='sets location for the final created installer file to be copied to (ignored if distribution not built)')
    parser.add_argument('--skipTests', default=False, action='store_true',
        help='skips test step in Maven')
    parser.add_argument('--java_home_o', type=str,
        help='Overrides JAVA_HOME. Useful for stubborn VMs that will not normally recognize environment variables')
    parser.add_argument('--type_o', type=str,
        choices=['app-image', 'exe', 'msi', 'rpm', 'deb', 'pkg', 'dmg'],
        help='Overrides default jpackage target')
    
    # MacOS specific
    parser.add_argument('--mac_sign_identity', type=str,
        help='Sign the Mac app image with the specified code signing identity')
    parser.add_argument('--mac_distrib_cert', type=str,
        help='Specify the certificate to use for signing the MacOS app image')
    parser.add_argument('--intelBuild', default=False, action='store_true',
        help='MacOS only, indicates to use intel libraries rather than Arch64 when building')
    
    args = parser.parse_args()

    # allows specifying code signing identity for mac builds
    if args.mac_sign_identity is not None:
        SIGN_IDENTITY = args.mac_sign_identity

    # allows specifying code signing for mac distribution
    if args.mac_distrib_cert is not None:
        DISTRIB_IDENTITY = args.mac_distrib_cert

    # allows for override of java home (virtual environments make this necessary at times)
    if args.java_home_o is not None:
        print(f'JAVA_HOME overriden to: {args.java_home_o}')
        JAVA_HOME = args.java_home_o
    else:
        JAVA_HOME = os.getenv('JAVA_HOME')

    # detects if marked for release
    if args.release:
        print('RELEASE BUILD')
    else:
        print('BETA_BUILD')

    if args.copyDestination is not None:
        print(f'Destination for final install file set to: {args.copyDestination}')
        copyDestination = args.copyDestination

        # failure message file created here, deleted at end of process conditionally upon success
        failFile = os.path.join(copyDestination, osString + "_BUILD_FAILED")
        open(failFile, 'a').close()

    if JAVA_HOME is None:
        print('JAVA_HOME must be set. If necessary, use -java-home-o command to override')
        return 1

    POLYGLOT_VERSION = getVersion()
    POLYGLOT_BUILD = getBuildNum(args.release)
    print('Building Version: ' + POLYGLOT_VERSION)
    updateVersionResource(args.release, POLYGLOT_VERSION)
    JAR_W_DEP = 'PolyGlotLinA-' + POLYGLOT_VERSION + '-jar-with-dependencies.jar'
    JAR_WO_DEP = 'PolyGlotLinA-' + POLYGLOT_VERSION + '.jar'

    if osString == winString:
        os.system('echo off')
    elif osString == osxString and args.intelBuild:
        macIntelBuild = True

    profile = args.profile

    full_build = len(args.step) == 0

    if full_build or 'clean' in args.step:
        clean()
    if full_build or 'docs' in args.step:
        injectDocs()
    if full_build or 'build' in args.step:
        build(profile, args.skipTests)
    if full_build or 'dist' in args.step:
        dist(args.release, args.type_o)

    print('Done!')
    return 0

def build(profile : str, skipTests : bool):
    print('Injecting build date/time...')
    injectBuildDate()

    print('cleaning/testing/compiling...')
    command = f'mvn clean package -P {profile}'

    if skipTests:
        command += ' -DskipTests'
    stat = subprocess.run(command, shell=True)
    if stat.returncode != 0:
        print(stat.args)
        sys.exit(1)

def clean():
    print('cleaning build paths...')
    command = 'mvn clean'
    stat = subprocess.run('mvn clean', shell=True)
    if stat.returncode != 0:
        print(stat.args)
        sys.exit(1)
    for folder in ['build']:
        if os.path.exists(folder):
            shutil.rmtree(folder)

def dist(is_release : bool, target_type : Union[str, None]):
    if os.path.exists('installer'):
        shutil.rmtree('installer')
    
    print('Unzipping runtime image...')
    if osString == winString:
        stat = subprocess.run(["powershell.exe", "-Command", f'Expand-Archive target\{JAR_WO_DEP.replace(".jar", "-runtime-image.zip")} -DestinationPath target\image'],
            capture_output=True, text=True, check=True)
    else:
        stat = subprocess.run(f'unzip -q target/{JAR_WO_DEP.replace(".jar", "-runtime-image.zip")} -d target/image', shell=True)
    if stat.returncode != 0:
        print(stat.args)
        sys.exit(1)

    print('Calling jpackage for distribution...')
    if osString == linString:
        distLinux(is_release, target_type)
    elif osString == osxString:
        distOsx(is_release, target_type)
    elif osString == winString:
        distWin(is_release, target_type)

######################################
#       LINUX FUNCTIONALITY
######################################
def distLinux(IS_RELEASE : bool, target_type : Union[str, None]):
    print('creating linux distribution...')
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
               '--runtime-image target/image')
    if target_type is None:
        os_info = subprocess.check_output(('cat', '/etc/os-release')).decode("utf-8")
        if "debian" in os_info:
            target_type = "deb"
        elif "fedora" in os_info:
            target_type = "rpm"
        else:
            raise Exception("Check on /etc/os-release failed to identify linux packaging format")
    command += f' --type {target_type}'
    if target_type == 'rpm':
        command += ' --linux-rpm-license-type MIT '
    os.system(command)

    # search for the generated system package
    # (jpackage is inconsistent with how it chooses to name packages
    # on different architectures)
    installer_file = ''
    for item in os.listdir():
        if os.path.isfile(item) and item.startswith('polyglot-linear-a') \
                and POLYGLOT_BUILD in item \
                and item.endswith(target_type):
            installer_file = item
            print('generated: ' + installer_file)
            break
    if installer_file == '':
        raise Exception('failed to locate jpackage output')

    if copyDestination != "":
        copyInstaller(copyDestination, installer_file, IS_RELEASE)


######################################
#       Mac OS FUNCTIONALITY
######################################
def distOsx(IS_RELEASE : bool, target_type : Union[str, None]):
    print('Creating app image...')

    command = (JAVA_HOME + '/bin/jpackage ' +
               '--runtime-image target/image ' +
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
    if target_type is not None:
        command += f' --type {target_type}'

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
            copyInstaller(copyDestination, 'PolyGlot-' + POLYGLOT_VERSION + '.dmg', IS_RELEASE)

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
def distWin(IS_RELEASE : bool, target_type : Union[str, None]):
    package_location = 'PolyGlot-' + POLYGLOT_BUILD + '.exe'
    print('Creating distribution package...')

    # If missing, install WiX Toolset: https://wixtoolset.org/releases/
    command = ('jpackage ' +
               '--runtime-image target\\image ' +
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
    if target_type is not None:
        command += f' --type {target_type}'

    os.system(command)

    if copyDestination != "":
        copyInstaller(copyDestination, package_location, IS_RELEASE)


# injects current time into file which lives in PolyGlot resources
def injectBuildDate():
    build_time = datetime.datetime.now().strftime('%Y-%m-%d %H:%M')
    file_path = os.path.join('assets', 'assets', 'org', 'DarisaDesigns', 'buildDate')

    f = open(file_path, 'w')
    f.write(build_time)
    f.close()


####################################
#       UTIL FUNCTIONALITY
####################################

# fetches version of PolyGlot from pom file
def getVersion():
    doc = minidom.parse('pom.xml')
    version_items = doc.getElementsByTagName('version')

    return version_items[0].firstChild.data


# for releases, this will match the version. For beta builds, a UTC timestamp is appended (OS registration reasons on
# installation)
def getBuildNum(IS_RELEASE : bool):
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


def updateVersionResource(IS_RELEASE : bool, version_string : str):
    location = os.path.join('assets', 'assets', 'org', 'DarisaDesigns', 'version')

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
    readme_location = os.path.join('assets', 'assets', 'org', 'DarisaDesigns', 'readme')

    if path.exists(readme_location + extension):
        os.remove(readme_location + extension)

    shutil.make_archive(readme_location, 'zip', 'docs')

    # example dictionaries
    source_location = os.path.join('packaging_files', 'example_lexicons')
    dict_location = os.path.join('assets', 'assets', 'org', 'DarisaDesigns', 'exlex')

    if path.exists(source_location + extension):
        os.remove(readme_location + extension)

    shutil.make_archive(dict_location, 'zip', source_location)


# Copies installer file to final destination and removes error indicator file
def copyInstaller(copyDestination : str, source : str, IS_RELEASE : bool):
    global macIntelBuild

    if not path.exists(source):
        print('FAILURE: Built installer missing: ' + source)

    # release candidates copied to their own location
    if IS_RELEASE:
        copyDestination = os.path.join(copyDestination, 'Release')
        destination = os.path.join(copyDestination, source)
    else:
        copyDestination = os.path.join(copyDestination, 'Beta')
        destination = os.path.join(copyDestination,
            f'_BETA_{datetime.datetime.now().strftime("%Y-%m-%d-%H-%M")}_{source}')

    if not path.exists(copyDestination):
        os.makedirs(copyDestination)

    print('Copying installer to ' + destination)
    shutil.copy(source, destination)

    # only remove failure signal once process is successful
    os.remove(failFile)
    os.remove(source)

if __name__ == "__main__":
    sys.exit(main())
