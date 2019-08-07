/*
 * Copyright (c) 2019, Draque Thompson
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 *  See LICENSE.TXT included with this code to read the full license agreement.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package PolyGlot;

import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PFrame;
import PolyGlot.Screens.ScrAbout;
import PolyGlot.Screens.ScrMainMenu;

/**
 * Starts up PolyGlot and does testing for OS/platform that would be inappropriate elsewhere
 * @author Draque Thompson
 */
public class PolyGlot {
    /**
     * @param args the command line arguments: 
     * args[0] = open file path (blank if none) 
     * args[1] = working directory of PolyGlot (blank if none)
     * args[2] = set to PGTUtils.True to skip OS Integration
     */
    public static void main(final String args[]) {
        try {
            Object preNimbusMenu = null;
            System.setProperty("apple.awt.application.name", PGTUtil.displayName);
            boolean osIntegration = shouldUseOSInegration(args);
            
            // must be set before accessing System to test OS (values will simply be ignored for other OSes
            if (osIntegration) {
                // TODO: apple.laf.useScreenMenuBar should be enabled (and the whole preNimbusMenu
                // mess removed) once upgrading past Java 9. useScreenMenuBar is automatic and cleaner
                // System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("apple.awt.application.name", PGTUtil.displayName);
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", PGTUtil.displayName);
            }

            if (PGTUtil.isOSX && osIntegration) {
                preNimbusMenu = osintegration_mac.OSIntegration_Mac.setBlankAppleMenuBar();

                osintegration_mac.OSIntegration_Mac.setIcon(PGTUtil.polyGlotIcon.getImage());

                Runnable scrAboutRun = new Runnable(){
                    @Override
                    public void run() {
                        ScrAbout.run(new DictCore());
                    }
                };

                osintegration_mac.OSIntegration_Mac.setAboutHandler(scrAboutRun);
            }
            setupNimbus();
            
            conditionalBetaSetup();

            final Object finalPreNimbusMenu = preNimbusMenu;

            java.awt.EventQueue.invokeLater(() -> {

                // catch all top level application killing throwables (and bubble up directly to ensure reasonable behavior)
                try {
                    String overridePath = args.length > 1 ? args[1] : "";
                    ScrMainMenu s = null;

                    // set DPI scaling to false (requires Java 9)
                    System.getProperties().setProperty("Dsun.java2d.dpiaware", "false");

                    if (canStart()) {
                        try {
                            // separated due to serious nature of Thowable vs Exception
                            PFrame.setupOSSpecificCutCopyPaste();
                            s = new ScrMainMenu(overridePath);
                            s.checkForUpdates(false);
                            s.setVisible(true);

                            // open file if one is provided via arguments
                            if (args.length > 0) {
                                s.setFile(args[0]);
                                s.openLexicon(true);
                            }

                            // runs additional integration if on OSX system
                            if (PGTUtil.isOSX && osIntegration) {
                                final ScrMainMenu staticScr = s;
                                osintegration_mac.OSIntegration_Mac.integrateMacMenuBar(s.getJMenuBar(), finalPreNimbusMenu);
                                osintegration_mac.OSIntegration_Mac.setQuitAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        staticScr.dispose();
                                    }

                                });

                                osintegration_mac.OSIntegration_Mac.setPreferanceManager(new Runnable() {
                                    @Override
                                    public void run() {
                                        staticScr.showOptions();
                                    }

                                });
                            } else if (PGTUtil.isWindows && osIntegration) {
                                s.setIconImage(PGTUtil.polyGlotIcon.getImage());
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            IOHandler.writeErrorLog(e, "Problem with top level PolyGlot arguments.");
                            InfoBox.error("Unable to start", "Unable to open PolyGlot main frame: \n"
                                    + e.getMessage() + "\n"
                                            + "Problem with top level PolyGlot arguments.", null);
                        } catch (Exception e) { // split up for logical clarity... migt want to differn
                            IOHandler.writeErrorLog(e);
                            InfoBox.error("Unable to start", "Unable to open PolyGlot main frame: \n"
                                    + e.getMessage() + "\n"
                                            + "Please contact developer (draquemail@gmail.com) for assistance.", null);

                            if (s != null) {
                                s.dispose();
                            }
                        }
                    }
                } catch (Throwable t) {
                    InfoBox.error("PolyGlot Error", "A serious error has occurred: " + t.getLocalizedMessage(), null);
                    IOHandler.writeErrorLog(t);
                    throw t;
                }
            });
        } catch (Exception e) {
            IOHandler.writeErrorLog(e, "Startup Exception");
            InfoBox.error("PolyGlot Error", "A serious error has occurred: " + e.getLocalizedMessage(), null);
            throw e;
        }
    }
    
    /**
     * Displays beta message if appropriate (beta builds have warning text within lib folder)
     * Sets version to display as beta
     */
    private static void conditionalBetaSetup() {
        if (testIsBeta()) {
            InfoBox.warning("BETA BUILD", "This is a pre-release, beta build of PolyGlot. Please use with care.", null);
        }
    }
    
    private static void setupNimbus() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException e) {
            java.util.logging.Logger.getLogger(ScrMainMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
            IOHandler.writeErrorLog(e);
        }
    }
    
    /**
     * Tests whether PolyGlot can start, informs user of startup problems.
     * @return 
     */
    private static boolean canStart() {
        String startProblems = "";
        boolean ret = true;
        
        // Test for minimum version of Java (8)
        String jVer = System.getProperty("java.version");
        if (jVer.startsWith("1.5") || jVer.startsWith("1.6") || jVer.startsWith("1.7")) {
            startProblems += "Unable to start PolyGlot without Java 8 or higher.\n";
        }

        // keep people from running PolyGlot from within a zip file...
        if (System.getProperty("user.dir").toLowerCase().startsWith("c:\\windows\\system")) {
            startProblems += "PolyGlot cannot be run from within a zip archive. Please unzip all files to a folder.\n";
        }

        try {
            // Test for JavaFX and inform user that it is not present, they cannot run PolyGlot
            ScrMainMenu.class.getClassLoader().loadClass("javafx.embed.swing.JFXPanel");
        } catch (ClassNotFoundException e) {
            IOHandler.writeErrorLog(e);
            startProblems += "Unable to load Java FX. Download and install to use PolyGlot ";
            
            if (PGTUtil.isOSX) {
                startProblems += "The default Java Virtual Machine for OSX does not include JFX. Please download from java.com/en/download/";
            } else if (PGTUtil.isWindows) {
                startProblems += "The version of Java you are using does not include JFX.  Please download from java.com/en/download/";
            } else {
                startProblems += "(JavaFX not included in some builds of Java for Linux).\n";
            }
        }
        
        if (startProblems.length() != 0) {
            InfoBox.error("Unable to start PolyGlot", startProblems, null);
            ret = false;
        }
        
        return ret;
    }
    
        private static boolean shouldUseOSInegration(String args[]) {
        return args == null || args.length < 3 || !args[2].equals(PGTUtil.True);
    }

    public static boolean testIsBeta() {
        return IOHandler.fileExists("lib/BETA_WARNING.txt");
    }
}
