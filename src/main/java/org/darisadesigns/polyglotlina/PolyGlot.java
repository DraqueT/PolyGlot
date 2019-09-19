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
package org.darisadesigns.polyglotlina;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.PrintFilesEvent;
import java.awt.desktop.PrintFilesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.event.KeyEvent;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.Screens.ScrAbout;
import org.darisadesigns.polyglotlina.Screens.ScrMainMenu;

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
            boolean osIntegration = shouldUseOSInegration(args);
            
            // must be set before accessing System to test OS (values will simply be ignored for other OSes
            if (osIntegration) {
                if (PGTUtil.IS_OSX) {
                    // set program icon
                    Taskbar.getTaskbar().setIconImage(PGTUtil.POLYGLOT_ICON.getImage());
                }
                
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("apple.awt.application.name", PGTUtil.displayName);
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", PGTUtil.displayName);
            }

            setupNimbus();
            setupCustomUI();
            conditionalBetaSetup();
            setupOSSpecificCutCopyPaste();

            java.awt.EventQueue.invokeLater(() -> {

                // catch all top level application killing throwables (and bubble up directly to ensure reasonable behavior)
                try {
                    String overridePath = args.length > 1 ? args[1] : "";
                    ScrMainMenu s = null;

                    // TODO: JAVA 12 UPGRADE - see whether this is necessary any more
                    System.getProperties().setProperty("Dsun.java2d.dpiaware", "false");

                    if (canStart()) {
                        try {
                            // separated due to serious nature of Thowable vs Exception
                            s = new ScrMainMenu(overridePath);
                            s.checkForUpdates(false);
                            s.setVisible(true);

                            // open file if one is provided via arguments
                            if (args.length > 0) {
                                s.setFile(args[0]);
                                s.openLexicon(true);
                            }

                            // runs additional integration if on OSX system
                            if (PGTUtil.IS_OSX && osIntegration) {
                                Desktop desk = Desktop.getDesktop();
                                final ScrMainMenu staticScr = s;
                                
                                desk.setQuitHandler(new QuitHandler(){
                                    @Override
                                    public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
                                        staticScr.dispose();
                                    }
                                });
                                
                                desk.setPreferencesHandler(new PreferencesHandler(){
                                    @Override
                                    public void handlePreferences(PreferencesEvent e) {
                                        staticScr.showOptions();
                                    }
                                });

                                desk.setAboutHandler(new AboutHandler(){
                                    @Override
                                    public void handleAbout(AboutEvent e) {
                                        ScrAbout.run(new DictCore());
                                    }
                                });
                                
                                desk.setPrintFileHandler(new PrintFilesHandler(){
                                    @Override
                                    public void printFiles(PrintFilesEvent e) {
                                        staticScr.printToPdf();
                                    }
                                });
                            } else if (PGTUtil.IS_WINDOWS && osIntegration) {
                                s.setIconImage(PGTUtil.POLYGLOT_ICON.getImage());
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
            IOHandler.writeErrorLog(e);
        }
    }
    
    private static void setupCustomUI() {
        UIManager.put("ScrollBarUI", "org.darisadesigns.polyglotlina.CustomControls.PScrollBarUI");
        UIManager.put("SplitPaneUI", "org.darisadesigns.polyglotlina.CustomControls.PSplitPaneUI");
        UIManager.put("ToolTipUI", "org.darisadesigns.polyglotlina.CustomControls.PToolTipUI");
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.getLookAndFeelDefaults().put("Panel.background", Color.WHITE);
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
            
            if (PGTUtil.IS_OSX) {
                startProblems += "The default Java Virtual Machine for OSX does not include JFX. Please download from java.com/en/download/";
            } else if (PGTUtil.IS_WINDOWS) {
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
    
    /**
     * enable cut/copy/paste/select all if running on a Mac, and any other
     * specific, text based bindings I might choose to add later
     */
    private static void setupOSSpecificCutCopyPaste() {
        if (System.getProperty("os.name").startsWith("Mac")) {
            for (String inputMap : PGTUtil.INPUT_MAPS) {
                addTextBindings(inputMap, KeyEvent.META_DOWN_MASK);
            }
        }
    }
    
    /**
     * Adds copy/paste/cut/select all bindings to the input map provided
     *
     * @param UIElement the string representing a UI Element in UIManager
     * @param mask the mask to associate the binding with (command or control,
     * for Macs or PC/Linux boxes, respectively.)
     */
    private static void addTextBindings(final String UIElement, final int mask) {
        SwingUtilities.invokeLater(() -> {
            try {
                InputMap im = (InputMap) UIManager.get(UIElement);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | mask), DefaultEditorKit.copyAction);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | mask), DefaultEditorKit.pasteAction);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | mask), DefaultEditorKit.cutAction);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | mask), DefaultEditorKit.selectAllAction);
                UIManager.put(UIElement, im);
            } catch (NullPointerException e) {
                IOHandler.writeErrorLog(e, "Unable to get input map for: " + UIElement);
            }
        });
    }
}
