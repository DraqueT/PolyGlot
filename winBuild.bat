echo off
git pull

:: build local
python3 build_image.py -copyDestination "C:\Users\draqu\Google Drive\Permanent_Share\PolyGlotBetas" -java-home-o "C:\Program Files\Java\jdk-17" %1 %2 %3 %4

vmrun start "C:\Users\draqu\OneDrive\Documents\Virtual Machines\Ubuntu_22_PolyGlot\Ubuntu_22_PolyGlot.vmx" 

echo "Waiting 25 seconds before running Linux build...
timeout /t 25
echo "Initiating Linux build...

:: build virtual
vmrun -gu polyglot -gp polyglot runProgramInGuest  "C:\Users\draqu\OneDrive\Documents\Virtual Machines\Ubuntu_22_PolyGlot\Ubuntu_22_PolyGlot.vmx" -activeWindow "bin/sh" "/home/polyglot/JavaPojects/build.sh"

:: provide build output
type "C:\Users\draqu\Google Drive\Permanent_Share\PolyGlotBetas\out.log"
del "C:\Users\draqu\Google Drive\Permanent_Share\PolyGlotBetas\out.log"

:: Shutdown virtual
vmrun stop "C:\Users\draqu\OneDrive\Documents\Virtual Machines\Ubuntu_22_PolyGlot\Ubuntu_22_PolyGlot.vmx" 