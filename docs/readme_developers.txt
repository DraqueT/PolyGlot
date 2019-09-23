If you're looking to work on PolyGlot independently or to contribute, here's how you set it up to work with. Although not all the instructions are 100% fleshed out, googling any given step should show you exactly what you need to know to complete the task. Setting up the JDK on Windows can be a bit of a pain though, as a heads up. Be patient and you'll get it. Many of the steps here are order dependant, so don't go backwards or something.

	-----------			BASIC DEVELOPMENT SETUP					-----------

1) Download Open JDK 12 (or higher)
	- Copy to appropriate directory
	- add environment variables
2) Install Maven
3) Download/install Netbeans
	- remember to set the jdk path as appropriate on install (might not auto-populate)
4) Clone PolyGlot from the git repo
5) Open the PolyGlot project
6) In the project explorer on the left side and within the PolyGot project, right click on the Dependencies node, then click Download Declared Dependencies
7) Wait while the dependencies download that Netbeans scans the project. All program errors should not disappear.

You are now ready to begin work! Building this will result in a runnable jar file (PolyGlotLinA-3.0-jar-with-dependencies.jar) being created in the target folder. Be aware that this jar is NOT cross platform compatible. In J8, PolyGlot was distributed as a single file for all OSes (but the great module wars of J9 changed all that. 'S how I got this eyepatch and lost my leg).


	
	-----------		OPTIONAL STEPS TO BUILDING DISTRIBUTABLE APPLICATION IMAGES	-----------

This will show you how to package PolyGlot for OSX, Windows, and Linux.

1) Download the JDK14 preview and drop it in a folder somewhere. (I probably won't update this in forever, so maybe it's no longer a preview as you read this! :D)
2) Open build_image.sh (build_image_linux.sh if you are on Linux, build_image_win.bat if you are on Windows) in a text editor.
3) Change the value of JAVAFX_LOCATION to wherever jfx got dumped (you can get this by looking at the properties of any of the jfx dependencies)
	- the end of the location should read "openjfx." Do NOT copy the full path of one of the dependencies, or it will not work.
4) Change the value of JAVA_PACKAGER_LOCATION to the JDK14 bin folder
5) In a terminal, nagivate to the PolyGlot project folder.
5.5) (Windows only) Download and install WiX Toolset: https://wixtoolset.org/releases/
6) On OSX, enter the command "./build_image.sh" without the quotes
	- On Linux, enter the command "./build_image_linux.sh" without the quotes
	- On Windows, enter the command "build_image_win.bat" without the quotes
	- If you open the script files, you'll see that they're segmented so that you can give arguments and just execute one section them at a time for convenience.

PolyGlot will now build itself into a platform specific application for you! This can be run on machines regardless of whether they have Java installed, as it builds a Java runtime into the distribution.



	-----------		OPTIONAL STEPS TO BUILDING THE JAVA 8 BRIDGE			-----------

So. Some parts of PolyGlot rely on libraries that are pretty fundamentally rooted in Java 8. This was a bummer to deal with. Hopefully they will be modularized in the future and I can get rid of this hack, but for now, this is unfortunately how it is. Users WILL have to have some version of Java installed on their machine to use these features. The include: PDF Generation, Import from Excel, Export to Excel. If you want to modify this functionality, follow these steps.

1) Clone the git repo found at: https://github.com/DraqueT/PolyGlotPDF
2) Open it in Netbeans
3) Modify whatever you like.
4) Build the project.
5) Copy the newly created "java_8_bridge.zip" file into the assets/assets/org/DarisaDesigns folder inside of the primary PolyGlot application's project folder.

This will replace the Java 8 dependencies that run these select functions in PolyGlot.
