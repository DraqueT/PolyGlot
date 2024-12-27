# README for Developers
If you're looking to work on PolyGlot independently or to contribute, here's how you set it up to work with it.

Note: GUI elements were built with the NetBeans GUI Builder.

## Dependencies
- Java 17 or higher
- Maven 3.8.1 or higher
- Python 3
- An IDE of your choice:
  - Netbeans 12 or newer
  - Visual Studio Code with java/maven plugins
  - Eclipse


## Development Setup
1) Install dependencies.
  - Set the JAVA_HOME environment variable.
  - Make sure `java` and `mvn` are in your path (try `java -version` and `mvn -v` in a terminal).
  - If using Netbeans as an IDE, remember to set the jdk path as appropriate on install if asked (might not auto-populate).
2) (Optional) Fork the repo on GitHub.
3) Clone the repository.
4) Perform the steps below corresponding to your IDE.

Afterwards, you will be ready to begin work! Building this will result in a runnable jar file (PolyGlotLinA-<VER>-jar-with-dependencies.jar) being created in the target folder. Be aware that this jar is NOT cross platform compatible. In J8, PolyGlot was distributed as a single file for all OSes (but the great module wars of J9 changed all that. 'S how I got this eyepatch and lost my leg).

### NetBeans
1) Open the PolyGlot project
2) In the project explorer on the left side and within the PolyGot project, right click on the Dependencies node, then click Download Declared Dependencies
3) After the dependencies download and Netbeans scans the project all errors should disappear.
4) In a terminal, go to the PolyGlot root directory and enter `python build_image.py`. The scripted build will fail, but that is ok. It will set PolyGlot up to be built through Netbeans.

### Visual Studio Code
1) Install the `Extension Pack for Java` extension. This should come with integration for Maven
2) Open the PolyGlot project. You can set up the maven extension to automatically download dependencies
3) Open a terminal in the root directory and execute `python3 build_image.py`.

### Eclipse
1) Click on File > Import
2) Select Maven > Existing Maven Projects and click Next.
3) Under Root Directory, browse to wherever you've cloned the repo to.
4) Make sure the checkbox under `/pom.xml` is selected and click Finish. This will create the project for you to work on.
5) Open a terminal and run `python build_image.py`. Some tests may fail, everything else should be fine. You will be able to run the project from Eclipse afterwards.

## OPTIONAL STEPS TO BUILDING DISTRIBUTABLE APPLICATION IMAGES
This will show you how to package PolyGlot for OSX, Windows, and Linux. If your system does not have python, install it.

1) Open [build_image.py](../build_image.py) in a text editor.
2) ONLY IF YOUR MAVEN USES A CUSTOM DEPENDENCY DIRECTORY - Change the value of JAVAFX_LOCATION_<OS> to wherever jfx got dumped (you can get this by looking at the properties of any of the jfx dependencies)
  - the end of the location should read "openjfx." Do NOT copy the full path of one of the dependencies, or it will not work.
  - there are separate variables for each OS, as I presume the directory structures of the environments you build on will differ.
3) In a terminal, navigate to the PolyGlot project folder.

Windows Setup
>- Download and install WiX Toolset: https://wixtoolset.org/releases/
>   - If you get a .Net error installing WiX:
>     - go to Windows Settings
>     - go to Turn Windows Features on or off
>     - check the selection for .Net Framework 3.5 and hit OK/allow Windows to download files
>     - the WiX install should work correctly now

MacOS Setup
>- install python (e.g. with homebrew)
>- install dmgbuild (at terminal: `pip install dmgbuild`)
>   - if pip isn't installed:
>     - curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
>     - python3 get-pip.py

Linux Debian Setup
>- your system might not have the program fakeroot. Install it (`sudo apt install fakeroot`)

Linux Fedora Setup
>- your system might not have the java jmods package installed for your distro. Use `dnf search jmods` to search for the package name.
>   - e.g. `java-17-openjdk-jmods.aarch64`


4) In a terminal, enter `python build_image.py` in PolyGlot's base directory
  - If you open the script file, you'll see that it's segmented so that you can give arguments and just execute one section them at a time for convenience.
  - You MUST build it first with this script. PolyGlot will fail to run if you try to run it from Netbeans before this. (necessary files are built in the Python file)
5) (Mac) Download binaries for modules with Intel/M1 specific builds and drop them into their own folder under the .m2 repo folder/match paths to what is in the build python script

PolyGlot will now build itself into a platform specific application for you! This can be run on machines regardless of whether they have Java installed, as it builds a Java runtime into the distribution.

## Flatpak
### Setup
Install the runtime and SDK.
```bash
flatpak install flathub org.freedesktop.Platform//24.08 org.freedesktop.Sdk//24.08
```

### Structure
The way that flathub operates prevents us from running Maven directly as the downloads it'll perform will be blocked. Because of that, a tar file is created from the build contents generated by the Apache tools on x86 debian and rpm arm. The flatpak manifest is setup to download those tar files from the releases and copy files from there.

### Build & Install
Build & installs the flatpak locally.
```bash
flatpak-builder --force-clean --user  --repo=repo --install build-dir io.github.DraqueT.PolyGlot.yml
```

Execute to run the flatpak version of PolyGlot.
```
flatpak run io.github.DraqueT.PolyGlot
```

## OPTIONAL STEPS TO BUILDING THE NON MODULAR BRIDGE
So. Some parts of PolyGlot rely on libraries that are pretty fundamentally rooted in Java 8. This was a bummer to deal with. Hopefully they will be modularized in the future and I can get rid of this hack, but for now, this is unfortunately how it is. Users WILL have to have some version of Java installed on their machine to use these features. The include: PDF Generation, Import from Excel, Export to Excel. If you want to modify this functionality, follow these steps.

1) Clone the git repo found at: https://github.com/DraqueT/PolyGlotPDF
2) Open it in Netbeans
3) Modify whatever you like.
4) Build the project.
5) Copy the newly created "java_8_bridge.zip" file into the assets/assets/org/DarisaDesigns folder inside of the primary PolyGlot application's project folder.

This will replace the non modular dependencies that run these select functions in PolyGlot.

<!-- Vim: set et ts=2 : -->
