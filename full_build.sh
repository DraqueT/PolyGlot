#!/bin/bash
# Builds all 3 versions of PolyGlot / uploads them to their hosting points on my google share
# This won't work on your machine unless you set it up specifically. It's set up to work with the particulars of my VMs.

START=$(date +%s)
OSX_BUILD_TIME=0
WIN_BUILD_TIME=0
LIN_BUILD_TIME=0
BUILD_STEP=""

# if a build step is specified, capture it here (can only specify build step per platform)
if [ "$2" != "" ]; then
    BUILD_STEP="$2"
    echo "Platform: $1, Step: $2"
fi

# Windows Build/upload
if [ "$#" -eq 0 ] || [ "$1" == "win" ]; then
    WIN_START_TIME=$(date +%s)
    echo "Starting Windows build process..."
    VBoxManage startvm "Windows 10" --type headless
    echo "Waiting 50 seconds for target machine to start up..."
    sleep 5
    echo "Waiting 45 seconds for target machine to start up..."
    sleep 5
    echo "Waiting 40 seconds for target machine to start up..."
    sleep 5
    echo "Waiting 35 seconds for target machine to start up..."
    sleep 5
    echo "Waiting 30 seconds for target machine to start up..."
    sleep 5
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
        VBoxManage guestcontrol "Windows 10" \
            run --exe "C:\Users\polyglot\Documents\NetBeansProjects\auto_polyglot_build.bat" \
            --username polyglot \
            --passwordfile /Users/draque/NetBeansProjects/polyglotvmpass \
            --putenv "DISPLAY=:0"
    else
        VBoxManage guestcontrol "Windows 10" \
            run --exe "C:\Users\polyglot\Documents\NetBeansProjects\auto_polyglot_build.bat" \
            --username polyglot \
            --passwordfile /Users/draque/NetBeansProjects/polyglotvmpass \
            --putenv "DISPLAY=:0" \
            -- auto_polyglot_build.bat/arg0 "$BUILD_STEP"
    fi
    
    VBoxManage guestcontrol "Windows 10" \
        run --exe "C:\Users\polyglot\Documents\NetBeansProjects\auto_polyglot_build_shutdown.bat" \
        --username polyglot \
        --passwordfile /Users/draque/NetBeansProjects/polyglotvmpass \
        --putenv "DISPLAY=:0"
    
    echo "Waiting for Windows machine to power down..."
    until $(VBoxManage showvminfo --machinereadable "Windows 10" | grep -q ^VMState=.poweroff.)
    do
        sleep 2
    done
    
    echo "Windows build process complete."
    
    WIN_END_TIME=$(date +%s)
    WIN_BUILD_TIME=$(echo "$WIN_END_TIME - $WIN_START_TIME" | bc)
fi

# Linux Build/upload
if [ "$#" -eq 0 ] || [ "$1" == "lin" ]; then
    LIN_START_TIME=$(date +%s)
    echo "Starting Ubuntu build process..."
    VBoxManage startvm "PolyGlotBuildUbuntu" --type headless
    echo "Waiting 40 seconds for target machine to start up..."
    sleep 5
    echo "Waiting 35 seconds for target machine to start up..."
    sleep 5
    echo "Waiting 30 seconds for target machine to start up..."
    sleep 5
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
        VBoxManage guestcontrol "PolyGlotBuildUbuntu" \
            run --exe "/home/polyglot/NetBeansProjects/auto_polyglot_build.sh" \
            --username polyglot \
            --passwordfile /Users/draque/NetBeansProjects/polyglotvmpass
    else
        VBoxManage guestcontrol "PolyGlotBuildUbuntu" \
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
if [ "$#" -eq 0 ] || [ "$1" == "osx" ]; then
    OSX_START_TIME=$(date +%s)
    echo "Starting OSX build process..."
    git pull
    
    if [ "$BUILD_STEP" == "" ] ; then
        python build_image.py -copyDestination "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas"
    else
        python build_image.py "$BUILD_STEP" -copyDestination "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas"
    fi
    echo "OSX build process complete."
    
    OSX_END_TIME=$(date +%s)
    OSX_BUILD_TIME=$(echo "$OSX_END_TIME - $OSX_START_TIME" | bc)
fi

END=$(date +%s)

if [ $WIN_BUILD_TIME != 0 ]; then
    echo "Windows build time: "$WIN_BUILD_TIME" seconds (50 seconds allowed for start)"
fi

if [ $LIN_BUILD_TIME != 0 ]; then
    echo "Linux build time: "$LIN_BUILD_TIME" seconds (40 seconds allowed for start)"
fi

if [ $OSX_BUILD_TIME != 0 ]; then
    echo "OSX build time: "$OSX_BUILD_TIME" seconds"
fi

DIFF=$(echo "$END - $START" | bc)
echo "Total time spent: "$DIFF" seconds"

LINECOUNT=$(git ls-files | grep ".java" | xargs wc -l | grep -o '[0-9]\+ total')
echo "Java lines built: $LINECOUNT"

# Announce any build failures...
if [ -f "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas/Windows_BUILD_FAILED" ] &&  [ $WIN_BUILD_TIME != 0 ]; then
    echo "Windows build failed."
fi
if [ -f "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas/Linux_BUILD_FAILED" ] && [ $LIN_BUILD_TIME != 0 ]; then
    echo "Linux build failed."
fi
if [ -f "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas/Darwin_BUILD_FAILED" ] && [ $OSX_BUILD_TIME != 0 ]; then
    echo "OSX build failed."
fi
echo "Full build process complete!"