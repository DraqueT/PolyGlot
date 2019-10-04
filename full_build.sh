#!/bin/bash
# Builds all 3 versions of PolyGlot / uploads them to their hosting points on my google share
# This won't work on your machine. It's set up to work with the particulars of my VMs.

# OSX Build/upload
git pull
python build_image.py
cp "installer/PolyGlot-Ins.dmg" "/Users/draque/Google Drive/Permanent_Share/PolyGlotBetas/PolyGlot-Ins-Osx.dmg"

# Linux Build/upload
VBoxManage startvm "PolyGlotBuildUbuntu" --type headless
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

# Windows Build/upload
VBoxManage startvm "Windows 10" --type headless
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
VBoxManage guestcontrol "Windows 10" run --exe "C:\Users\user\Documents\NetBeansProjects\auto_polyglot_build.bat" --username polyglot --password polyglot --putenv "DISPLAY=:0"
