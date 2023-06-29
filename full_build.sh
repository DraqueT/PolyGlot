#!/bin/bash
# Builds all 3 versions of PolyGlot / uploads them to their hosting points on my google share
# This won't work on your machine unless you set it up specifically. It's set up to work with the particulars of my VMs.

START=$(date +%s)
OSX_BUILD_TIME=0
BUILD_STEP=""
CONST_RELEASE="-release"
CONST_OSX_GOOGLE_DRIVE="/Users/draquethompson/Library/CloudStorage/GoogleDrive-draquemail@gmail.com/My Drive/Permanent_Share/PolyGlotBetas"

# ensure java home properly defined
source ~/.profile
source ~/.zshrc

# deal with Mac OS
# find . -name ".DS_Store" -type f -delete

# if a build step is specified, capture it here (can only specify build step per platform)
if [ "$1" != "" ]; then
    BUILD_STEP="$1"
fi

# OSX Build/upload
OSX_START_TIME=$(date +%s)
echo "Starting OSX build process..."
git pull

# Apple signature must be provided. Must have xcode installed and pull in from keychain.
# Python3 must be brew installed if not present (not included on macs by default)
if [ "$BUILD_STEP" == "" ] ; then
     python3 build_image.py \
        -copyDestination "$CONST_OSX_GOOGLE_DRIVE" \
        -mac-sign-identity "Apple Development: Draque Thompson (A3YEXQ2CB4)" \
        -intelBuild \
        -java-home-o "/Library/Java/JavaVirtualMachines/jdk-17-intel.0.2.jdk/Contents/Home" #\
        #-mac-distrib-cert "Apple Distribution: Draque Thompson (HS2SXD98BV)" #DISABLED FOR NOW

    python3 build_image.py \
        -copyDestination "$CONST_OSX_GOOGLE_DRIVE" \
        -mac-sign-identity "Apple Development: Draque Thompson (A3YEXQ2CB4)" \
        -java-home-o "/Library/Java/JavaVirtualMachines/jdk-17.0.2.jdk/Contents/Home" #\
        #-mac-distrib-cert "Apple Distribution: Draque Thompson (HS2SXD98BV)" #DISABLED FOR NOW

else
    python3 build_image.py \
        "$BUILD_STEP" -copyDestination "$CONST_OSX_GOOGLE_DRIVE" \
        -mac-sign-identity "Apple Development: Draque Thompson (A3YEXQ2CB4)" \
        -intelBuild \
        -java-home-o "/Library/Java/JavaVirtualMachines/jdk-17-intel.0.2.jdk/Contents/Home" #\ 
        #-mac-distrib-cert "Apple Distribution: Draque Thompson (HS2SXD98BV)" #DISABLED FOR NOW

    python3 build_image.py \
        "$BUILD_STEP" -copyDestination "$CONST_OSX_GOOGLE_DRIVE" \
        -mac-sign-identity "Apple Development: Draque Thompson (A3YEXQ2CB4)" \
        -java-home-o "/Library/Java/JavaVirtualMachines/jdk-17.0.2.jdk/Contents/Home" #\
        #-mac-distrib-cert "Apple Distribution: Draque Thompson (HS2SXD98BV)" #DISABLED FOR NOW
fi
echo "OSX build process complete."

OSX_END_TIME=$(date +%s)
OSX_BUILD_TIME=$(echo "$OSX_END_TIME - $OSX_START_TIME" | bc)

END=$(date +%s)

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
if [ -f "$CONST_GOOGLE_DRIVE/Darwin_BUILD_FAILED" ] && [ $OSX_BUILD_TIME != 0 ]; then
    echo -e "\x1B[41mOSX build failed.\x1B[0m"
elif [ $OSX_BUILD_TIME != 0 ]; then
    echo -e "\x1B[32mOSX build success.\x1B[0m"
fi
echo "Full build process complete!"
