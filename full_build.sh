#!/bin/bash
# Builds all 3 versions of PolyGlot / uploads them to their hosting points on my google share
# This won't work on your machine unless you set it up specifically. It's set up to work with the particulars of my VMs.

START=$(date +%s)

# Windows Build/upload
if [ "$#" -eq 0 ] || [ "$1" == "win" ]; then
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
    VBoxManage guestcontrol "Windows 10" \
        run --exe "C:\Users\user\Documents\NetBeansProjects\auto_polyglot_build.bat" \
        --username polyglot \
        --passwordfile /Users/draque/NetBeansProjects/polyglotvmpass \
        --putenv "DISPLAY=:0"
    echo "Waiting for Windows machine to power down..."
    until $(VBoxManage showvminfo --machinereadable "Windows 10" | grep -q ^VMState=.poweroff.)
    do
        sleep 2
    done
    echo "Windows build process complete."
fi

# Linux Build/upload
if [ "$#" -eq 0 ] || [ "$1" == "lin" ]; then
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
    VBoxManage guestcontrol "PolyGlotBuildUbuntu" \
       run --exe "/home/polyglot/NetBeansProjects/auto_polyglot_build.sh" \
       --username polyglot \
       --passwordfile /Users/draque/NetBeansProjects/polyglotvmpass
    echo "Waiting for Ubuntu machine to power down..."
    until $(VBoxManage showvminfo --machinereadable PolyGlotBuildUbuntu | grep -q ^VMState=.poweroff.)
    do
        sleep 2
    done
    echo "Linux build process complete."
fi

# OSX Build/upload
if [ "$#" -eq 0 ] || [ "$1" == "osx" ]; then
    echo "Starting OSX build process..."
    git pull
    python build_image.py -copyDestination "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas"
    echo "OSX build process complete."
fi


END=$(date +%s)
DIFF=$(echo "$END - $START" | bc)
echo "Total time spent: $DIFF seconds"

echo "Full build process complete!"