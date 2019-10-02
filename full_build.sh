#!/bin/bash
# Builds all 3 versions of PolyGlot / uploads them to their hosting points on my google share
# This won't work on your machine. It's set up to work with the particulars of my VMs.

# OSX Build/upload
#python build_image.py
cp "installer/PolyGlot-Ins.dmg" "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas/PolyGlot-Ins-Osx.dmg"

# Linux Build/upload
VBoxManage startvm "PolyGlotBuildUbuntu" --type headless
#VBoxManage guestproperty wait "PolyGlotBuildUbuntu" build_ready
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
VBoxManage guestcontrol "PolyGlotBuildUbuntu" run --exe "/home/polyglot/NetBeansProjects/auto_polyglot_build.sh" --username polyglot --password polyglot
#VBoxManage guestproperty wait "PolyGlotBuildUbuntu" build_complete
#VBoxManage controlvm "PolyGlotBuildUbuntu" poweroff