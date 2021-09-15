#!/bin/bash
# Builds all 3 versions of PolyGlot / uploads them to their hosting points on my google share
# This won't work on your machine unless you set it up specifically. It's set up to work with the particulars of my VMs.

START=$(date +%s)
OSX_BUILD_TIME=0
WIN_BUILD_TIME=0
LIN_BUILD_TIME=0
BUILD_STEP=""
CONST_RELEASE="-release"
CONST_WIN="win"
CONST_WIN_VIRTUAL="WinDev2001Eval"
CONST_LINUX="lin"
CONST_LINUX_VIRTUAL="PolyGlotBuildUbuntu"
CONST_OSX="osx"

# ensure java home properly defined
source ~/.bash_profile

# if a build step is specified, capture it here (can only specify build step per platform)
if [ "$2" != "" ]; then
    BUILD_STEP="$2"
    echo "Platform: $1, Step: $2"
fi

# release tag means pass release to all builds
if [ "$1" == "$CONST_RELEASE" ]; then
    BUILD_STEP="$1"
fi

# Windows Build/upload
if [ "$#" -eq 0 ] || [ "$1" == "$CONST_WIN" ] || [ "$1" == "$CONST_RELEASE" ]; then
    WIN_START_TIME=$(date +%s)
    echo "Starting Windows build process..."
###### For whatever reason, jpackage will simply not function correctly unless the PC is booted up in advance
#    VBoxManage startvm "$CONST_WIN_VIRTUAL" --type gui
#    echo "Waiting 25 seconds for target machine to start up..."
#    sleep 5
#    echo "Waiting 20 seconds for target machine to start up..."
#    sleep 5
#    echo "Waiting 15 seconds for target machine to start up..."
#    sleep 5
#    echo "Waiting 10 seconds for target machine to start up..."
#    sleep 5
#    echo "Waiting 05 seconds for target machine to start up..."
#    sleep 5

    if [ $(vboxmanage showvminfo "$CONST_WIN_VIRTUAL" | grep -c "running .since") == 0 ] ; then
        echo "Windows must be running to be built due to limitations in jpackage for Windows."
        sleep 5
        exit
    else
        if [ "$BUILD_STEP" == "" ] ; then
            VBoxManage guestcontrol "$CONST_WIN_VIRTUAL" \
                run --exe "C:\Users\polyglot\Documents\NetBeansProjects\auto_polyglot_build.bat" \
                --username polyglot \
                --passwordfile /Users/draque/NetBeansProjects/polyglotvmpass
        else
            VBoxManage guestcontrol "$CONST_WIN_VIRTUAL" \
                run --exe "C:\Users\polyglot\Documents\NetBeansProjects\auto_polyglot_build.bat" \
                --username polyglot \
                --passwordfile /Users/draque/NetBeansProjects/polyglotvmpass \
                -- auto_polyglot_build.bat/arg0 "$BUILD_STEP"
        fi

        VBoxManage guestcontrol "$CONST_WIN_VIRTUAL" \
            run --exe "C:\Users\polyglot\Documents\NetBeansProjects\auto_polyglot_build_shutdown.bat" \
            --username polyglot \
            --passwordfile /Users/draque/NetBeansProjects/polyglotvmpass

        echo "Waiting for Windows machine to power down..."
        until $(VBoxManage showvminfo --machinereadable "$CONST_WIN_VIRTUAL" | grep -q ^VMState=.poweroff.)
        do
            sleep 2
        done

        echo "Windows build process complete."

        WIN_END_TIME=$(date +%s)
        WIN_BUILD_TIME=$(echo "$WIN_END_TIME - $WIN_START_TIME" | bc)
    fi
fi

# Linux Build/upload
if [ "$#" -eq 0 ] || [ "$1" == "$CONST_LINUX" ] || [ "$1" == "$CONST_RELEASE" ]; then
    LIN_START_TIME=$(date +%s)
    echo "Starting Ubuntu build process..."
    VBoxManage startvm "$CONST_LINUX_VIRTUAL" --type headless
    echo "Waiting 25 seconds for target machine to start up..."
    sleep 5
    echo "Waiting 20 seconds for target machine to start up..."
    sleep 5
    echo "Waiting 15 seconds for target machine to start up..."
    sleep 5
    echo "Waiting 10 seconds for target machine to start up..."
    sleep 5
    echo "Waiting 05 seconds for target machine to start up..."
    sleep 5
    if [ "$BUILD_STEP" == "" ] ; then
        VBoxManage guestcontrol "$CONST_LINUX_VIRTUAL" \
            run --exe "/home/polyglot/NetBeansProjects/auto_polyglot_build.sh" \
            --username polyglot \
            --passwordfile /Users/draque/NetBeansProjects/polyglotvmpass
    else
        VBoxManage guestcontrol "$CONST_LINUX_VIRTUAL" \
            run --exe "/home/polyglot/NetBeansProjects/auto_polyglot_build.sh" \
            --username polyglot \
            --passwordfile /Users/draque/NetBeansProjects/polyglotvmpass \
            -- auto_polyglot_build.bat/arg0 "$BUILD_STEP"
    fi
    echo "Waiting for Ubuntu machine to power down..."
    until $(VBoxManage showvminfo --machinereadable PolyGlotBuildUbuntu | grep -q ^VMState=.poweroff.)
    do
        sleep 2
    done
    echo "Linux build process complete."
    
    LIN_END_TIME=$(date +%s)
    LIN_BUILD_TIME=$(echo "$LIN_END_TIME - $LIN_START_TIME" | bc)
fi

# OSX Build/upload
if [ "$#" -eq 0 ] || [ "$1" == "$CONST_OSX" ] || [ "$1" == "$CONST_RELEASE" ]; then
    OSX_START_TIME=$(date +%s)
    echo "Starting OSX build process..."
    git pull
    
    # Apple signature must be provided. Must have xcode installed and pull in from keychain.
    # Python3 must be brew installed if not present (not included on macs by default)
    if [ "$BUILD_STEP" == "" ] ; then
        python3 build_image.py \
            -copyDestination "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas" \
            -mac-sign-identity "Apple Development: Draque Thompson (A3YEXQ2CB4)" \
            -java-home-o "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" #\ #DISABLED FOR NOW
            #-mac-distrib-cert "Apple Distribution: Draque Thompson (HS2SXD98BV)"
    else
        python3 build_image.py \
            "$BUILD_STEP" -copyDestination "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas" \
            -mac-sign-identity "Apple Development: Draque Thompson (A3YEXQ2CB4)" \
            -java-home-o "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" #\ #DISABLED FOR NOW
            #-mac-distrib-cert "Apple Distribution: Draque Thompson (HS2SXD98BV)"
    fi
    echo "OSX build process complete."
    
    OSX_END_TIME=$(date +%s)
    OSX_BUILD_TIME=$(echo "$OSX_END_TIME - $OSX_START_TIME" | bc)
fi

END=$(date +%s)

if [ $WIN_BUILD_TIME != 0 ]; then
    echo "Windows build time: "$WIN_BUILD_TIME" seconds."
fi

if [ $LIN_BUILD_TIME != 0 ]; then
    echo "Linux build time: "$LIN_BUILD_TIME" seconds (25 seconds allowed for start)"
fi

if [ $OSX_BUILD_TIME != 0 ]; then
    echo "OSX build time: "$OSX_BUILD_TIME" seconds"
fi

DIFF=$(echo "$END - $START" | bc)
echo "Total time spent: "$DIFF" seconds"

LINECOUNT=$(git ls-files | grep ".java" | xargs wc -l | grep -o '[0-9]\+ total')
echo "Java lines built: $LINECOUNT"

if [ "$1" == "$CONST_RELEASE" ]; then
    echo -e "\x1B[34m---- RELEASE BUILD ----\x1B[0m" 
else
    echo -e "\x1B[96m---- BETA BUILD ----\x1B[0m"
fi 

# Announce any build failures...
if [ -f "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas/Windows_BUILD_FAILED" ] &&  [ $WIN_BUILD_TIME != 0 ]; then
    echo -e "\x1B[41mWindows build failed.\x1B[0m"
else
    echo -e "\x1B[32mWindows build success.\x1B[0m"
fi
if [ -f "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas/Linux_BUILD_FAILED" ] && [ $LIN_BUILD_TIME != 0 ]; then
    echo -e "\x1B[41mLinux build failed.\x1B[0m"
else
    echo -e "\x1B[32mLinux build success.\x1B[0m"
fi
if [ -f "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas/Darwin_BUILD_FAILED" ] && [ $OSX_BUILD_TIME != 0 ]; then
    echo -e "\x1B[41mOSX build failed.\x1B[0m"
else
    echo -e "\x1B[32mOSX build success.\x1B[0m"
fi
echo "Full build process complete!"