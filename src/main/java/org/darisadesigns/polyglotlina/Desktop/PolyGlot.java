/*
 * Copyright (c) 2019-2023, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
 * See LICENSE.TXT included with this code to read the full license agreement.

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
package org.darisadesigns.polyglotlina.Desktop;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PrintFilesEvent;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopGrammarManager;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopOptionsManager;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.VisualStyleManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.OSHandler;
import org.darisadesigns.polyglotlina.OSHandler.CoreUpdatedListener;
import org.darisadesigns.polyglotlina.OSHandler.FileReadListener;
import org.darisadesigns.polyglotlina.Screens.ScrAbout;
import org.darisadesigns.polyglotlina.Screens.ScrMainMenu;
import org.darisadesigns.polyglotlina.Webservice.WebService;

/**
 * Starts up PolyGlot and does testing for OS/platform that would be
 * inappropriate elsewhere
 *
 * @author Draque Thompson
 */
public final class PolyGlot {

    private static PolyGlot polyGlot;
    private static WebService webService;
    private Object clipBoard;
    private UIDefaults uiDefaults;
    private DictCore core;
    private ScrMainMenu rootWindow;
    private DesktopOSHandler osHandler;
    private final DesktopOptionsManager optionsManager;
    private CoreUpdatedListener coreUpdatedListener;
    private FileReadListener fileReadListener;
    private final File autoSaveFile;

    public PolyGlot(DictCore _core, DesktopOSHandler _osHandler) throws Exception {
        this(_core, _osHandler, new DesktopOptionsManager(_core));
    }

    public PolyGlot(
            DictCore _core,
            DesktopOSHandler _osHandler,
            DesktopOptionsManager _optionsManager
    ) throws Exception {
        core = _core;
        osHandler = _osHandler;
        optionsManager = _optionsManager;
        autoSaveFile = this.getNewAutoSaveFile();
        refreshUiDefaults();
    }

    /**
     * @param args the command line arguments: open file path (blank if none),
     * in chunks if spaces in path
     */
    public static void main(final String[] args) {
        // must be done before absolutely anything else
        setupScaling();

        var opMan = new DesktopOptionsManager();
        var ioHandler = DesktopIOHandler.getInstance();
        var cInfoBox = new DesktopInfoBox();
        var helpHandler = new DesktopHelpHandler();
        var fontHandler = new PFontHandler();
        var osHandler = new DesktopOSHandler(ioHandler, cInfoBox, helpHandler, fontHandler);

        try {
            var workingDirectory = org.darisadesigns.polyglotlina.PGTUtil.getDefaultDirectory().getAbsolutePath();
            ioHandler.loadOptionsIni(workingDirectory, opMan);
        }
        catch (Exception e) {
            ioHandler.writeErrorLog(e, "Startup config file failure.");
            cInfoBox.warning("Config Load Failure", "Unable to load options file or file corrupted:\n"
                    + e.getLocalizedMessage());
            DesktopIOHandler.getInstance().deleteIni(polyGlot.getWorkingDirectory().getAbsolutePath());
        }

        try {
            // must be set before accessing System to test OS (values will simply be ignored for other OSes
            if (PGTUtil.IS_OSX) {
                Taskbar.getTaskbar().setIconImage(PGTUtil.POLYGLOT_ICON.getImage());
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("apple.awt.application.name", PGTUtil.DISPLAY_NAME);
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", PGTUtil.DISPLAY_NAME);
            }

            setupNimbus();
            setupCustomUI();
            conditionalBetaSetup();
            testNonModularBridge();
            setupOSSpecificCutCopyPaste();
        }
        catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, "Startup Exception");
            cInfoBox.error("PolyGlot Error", "A serious error has occurred: " + e.getLocalizedMessage());
            throw e;
        }

        java.awt.EventQueue.invokeLater(() -> {
            ScrMainMenu s = null;

            // catch all top level application killing throwables (and bubble up directly to ensure reasonable behavior)
            try {
                // separated due to serious nature of Throwable vs Exception
                DictCore core = new DictCore(
                        new DesktopPropertiesManager(),
                        osHandler,
                        new PGTUtil(),
                        new DesktopGrammarManager()
                );

                PolyGlot.polyGlot = new PolyGlot(core, osHandler, opMan);

                s = new ScrMainMenu(core);
                polyGlot.setRootWindow(s);
                s.checkForUpdates(false);
                s.setVisible(true);
                cInfoBox.setParentWindow(s);

                polyGlot.coreUpdatedListener = (DictCore _core) -> {
                    polyGlot.getRootWindow().updateAllValues(_core);
                };
                polyGlot.fileReadListener = (DictCore _core) -> {
                    polyGlot.refreshUiDefaults();
                };
                osHandler.setCoreUpdatedListener(polyGlot.coreUpdatedListener);
                osHandler.setFileReadListener(polyGlot.fileReadListener);

                // runs additional integration if on OSX system
                if (PGTUtil.IS_OSX) {
                    setupMacOs(osHandler, s);
                }

                // if a recovery file exists, query user for action
                boolean recoveredFile = polyGlot.handleFileRecoveries(s, core.getWorkingDirectory());

                // open file if one is provided via arguments (but only if no recovery file- that takes precedence)
                if (args.length > 0 && recoveredFile == false) {
                    String filePath = "";

                    for (var arg : args) {
                        filePath += arg.trim();
                    }

                    if (new File(filePath).exists()) {
                        s.setFile(filePath);
                    } else {
                        polyGlot.getOSHandler().getInfoBox().warning("File Path Error",
                                "Unable to open: " + filePath
                                + "\nPlease retry opening this file by clicking File->Open from the menu.");
                    }
                }

                // if a language has been loaded, open Lexicon
                if (!polyGlot.core.getCurFileName().isBlank()) {
                    polyGlot.getRootWindow().openLexicon(true);
                }

                // only begin autosave loop once checks for recovery files are complete
                polyGlot.autoSave();
            }
            catch (ArrayIndexOutOfBoundsException e) {
                DesktopIOHandler.getInstance().writeErrorLog(e, "Problem with top level PolyGlot arguments.");
                polyGlot.getOSHandler().getInfoBox().error("Unable to start", "Unable to open PolyGlot main frame: \n"
                        + e.getMessage() + "\n"
                        + "Problem with top level PolyGlot arguments.");
            }
            catch (Exception e) { // split up for logical clarity... might want to differentiate
                // e.printStackTrace();
                DesktopIOHandler.getInstance().writeErrorLog(e);
                polyGlot.getOSHandler().getInfoBox().error("Unable to start", "Unable to open PolyGlot main frame: \n"
                        + e.getMessage() + "\n"
                        + "Please contact developer (draquemail@gmail.com) for assistance.");

                if (s != null) {
                    s.dispose();
                }

                if (!PGTUtil.isInJUnitTest() && !PGTUtil.isUITestingMode()) {
                    System.exit(0);
                }
            }
            catch (Throwable t) {
                // t.printStackTrace();
                cInfoBox.error("PolyGlot Error", "A serious error has occurred: " + t.getLocalizedMessage());
                DesktopIOHandler.getInstance().writeErrorLog(t);

                if (!PGTUtil.isInJUnitTest() && !PGTUtil.isUITestingMode()) {
                    System.exit(0);
                }
            }
        });
    }

    /**
     * Sets up Mac OS integration
     */
    private static void setupMacOs(OSHandler osHandler, final ScrMainMenu staticScr) {
        Desktop desk = Desktop.getDesktop();

        desk.setOpenFileHandler(e -> {
            List<File> files = e.getFiles();

            if (files.size() <= 0) {
                return;
            } else if (files.size() > 1) {
                polyGlot.getOSHandler().getInfoBox().info("File Limit",
                        "PolyGlot can only open a single file at once.\nOpening first selected file:"
                        + files.get(0).getName());
            }

            try {
                // saveOrCancelTest to prevent accidental failure to save open languages
                if (polyGlot.getRootWindow().saveOrCancelTest()) {
                    String filePath = files.get(0).getCanonicalPath();
                    polyGlot.getRootWindow().openFileFromPath(filePath);
                }
            }
            catch (IOException | IllegalStateException ex) {
                polyGlot.getOSHandler().getInfoBox().error("File Read Error",
                        "Unable to open file due to error:\n"
                        + ex.getLocalizedMessage());
            }
        });

        desk.setQuitHandler((QuitEvent e, QuitResponse response) -> {
            staticScr.dispose();
        });

        desk.setPreferencesHandler((PreferencesEvent e) -> {
            staticScr.showOptions();
        });

        desk.setAboutHandler((AboutEvent e) -> {
            DictCore _core = new DictCore(
                    new DesktopPropertiesManager(),
                    osHandler,
                    new PGTUtil(),
                    new DesktopGrammarManager()
            );

            polyGlot.setCore(_core);
            ScrAbout.run(_core);
        });

        desk.setPrintFileHandler((PrintFilesEvent e) -> {
            staticScr.printToPdf();
        });
    }

    private static void setupScaling() {
        Path p = Path.of(org.darisadesigns.polyglotlina.Desktop.PGTUtil.getDefaultDirectory()
                + File.separator + org.darisadesigns.polyglotlina.Desktop.PGTUtil.POLYGLOT_INI);
        if (!p.toFile().exists() || p.toFile().isDirectory()) {
            return;
        }

        try {
            String ini = Files.readString(p);
            int loc = ini.indexOf("UiScale=");

            if (loc == -1) {
                return;
            }

            ini = ini.substring(loc + 8);

            String value = "";

            for (int i = 0; i < ini.length(); i++) {
                String curChar = ini.substring(i, i + 1);

                if (ini.substring(i, i + 1).equals("\n") || ini.substring(i, i + 1).equals("\r")) {
                    break;
                }

                value += curChar;
            }

            System.setProperty("sun.java2d.uiScale", value);
        }
        catch (IOException e) {
            polyGlot.getOSHandler().getIOHandler().writeErrorLog(e);
        }
    }

    /**
     * Tests to make certain PolyGlot can access its own java runtime on disk
     * for use with non-modularized components NOTE: Only runs for
     * linked/deployed executions, not in dev or tests
     */
    private static void testNonModularBridge() {
        if (PGTUtil.IS_DEV_MODE || PGTUtil.isInJUnitTest() || PGTUtil.isUITestingMode()) {
            return;
        }

        String javaPath = NonModularBridge.getJavaExecutablePath();

        if (!new File(javaPath).exists()) {
            new DesktopInfoBox().warning("Runtime Location Error",
                    "Unable to access external java runtime. Certain features such as Print to PDF may not function properly.");
            DesktopIOHandler.getInstance().writeErrorLog(new Exception("Missing external jave runtime: " + javaPath));
        }
    }

    /**
     * Seeks recovery file or autosave file, queries user what to do with it,
     * and returns true if a recovery was made
     *
     * @param s
     * @param workingDirectory
     * @return
     * @throws IOException
     */
    private boolean handleFileRecoveries(ScrMainMenu s, File workingDirectory) throws IOException {
        File recovery = DesktopIOHandler.getInstance().getTempSaveFileIfExists(workingDirectory);

        if (recovery == null) {
            recovery = this.autoSaveFile;
        }

        if (recovery != null && recovery.exists()) {
            if (polyGlot.getOSHandler().getInfoBox().yesNoCancel("Recovery File Detected",
                    "PolyGlot appears to have shut down incorrectly. Would you like to recover the latest stable autosave?") == JOptionPane.YES_OPTION) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Recover Language To");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("PolyGlot Dictionaries", "pgd");
                chooser.setFileFilter(filter);
                chooser.setApproveButtonText("Recover");
                chooser.setCurrentDirectory(workingDirectory);

                String fileName;

                if (chooser.showOpenDialog(s) == JFileChooser.APPROVE_OPTION) {
                    fileName = chooser.getSelectedFile().getAbsolutePath();
                    if (!fileName.toLowerCase().endsWith(PGTUtil.POLYGLOT_FILE_SUFFIX)) {
                        fileName += "." + PGTUtil.POLYGLOT_FILE_SUFFIX;
                    }
                    File copyTo = new File(fileName);
                    try {
                        DesktopIOHandler.getInstance().copyFile(recovery.toPath(), copyTo.toPath(), true);

                        if (copyTo.exists()) {
                            s.setFile(copyTo.getAbsolutePath());
                            s.openLexicon(true);
                            DesktopIOHandler.getInstance().archiveFile(recovery, core.getWorkingDirectory());
                            osHandler.getInfoBox().info("Success!", "Language successfully recovered!");
                        } else {
                            throw new IOException("File not copied.");
                        }
                    }
                    catch (IOException e) {
                        osHandler.getInfoBox().error("Recovery Problem", "Unable to recover file due to: "
                                + e.getLocalizedMessage()
                                + ". Recovery file exists at location: "
                                + recovery.toPath()
                                + ". To attempt manual recovery, add .pgd suffix to file name and open with PolyGlot by hand.");
                    }
                } else {
                    osHandler.getInfoBox().info("Recovery Cancelled", "Recovery Cancelled. Restart PolyGlot to be prompted again.");
                }
            } else {
                if (polyGlot.getOSHandler().getInfoBox().yesNoCancel("Archive Recovery File", "Archive the recovery file, then? (stops this dialog from appearing)") == JOptionPane.YES_OPTION) {
                    DesktopIOHandler.getInstance().archiveFile(recovery, core.getWorkingDirectory());
                }

                recovery = null;
            }
        }

        return recovery != null && recovery.exists();
    }

    /**
     * Cleans up and exits program definitively
     */
    public void exitCleanup() {
        if (autoSaveFile.exists()) {
            autoSaveFile.delete();
        }

        // allow JUnit to handle this state itself
        if (!PGTUtil.isInJUnitTest() && !PGTUtil.isUITestingMode()) {
            System.exit(0);
        }
    }

    public DictCore getNewCore() {
        DictCore oldCore = this.core;

        this.core = new DictCore(
                new DesktopPropertiesManager(),
                this.getOSHandler(),
                new PGTUtil(),
                new DesktopGrammarManager()
        );

        oldCore.migrateSubscriptions(this.core);

        return this.core;
    }

    /**
     * Displays beta message if appropriate (beta builds have warning text
     * within lib folder) Sets version to display as beta
     */
    private static void conditionalBetaSetup() {
        if (PGTUtil.IS_BETA && !PGTUtil.isInJUnitTest() && !PGTUtil.IS_DEV_MODE) { // This requires user interaction and is not covered by the test
            new DesktopInfoBox().warning("BETA BUILD", "This is a pre-release, beta build of PolyGlot. Please use with care.\n\nBuild Date: " + PGTUtil.BUILD_DATE_TIME);
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
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
        }
    }

    private static void setupCustomUI() {
        UIManager.put("ScrollBarUI", "org.darisadesigns.polyglotlina.Desktop.CustomControls.PScrollBarUI");
        UIManager.put("SplitPaneUI", "org.darisadesigns.polyglotlina.Desktop.CustomControls.PSplitPaneUI");
        UIManager.put("ToolTipUI", "org.darisadesigns.polyglotlina.Desktop.CustomControls.PToolTipUI");
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.getLookAndFeelDefaults().put("Panel.background", Color.WHITE);
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
            }
            catch (NullPointerException e) {
                DesktopIOHandler.getInstance().writeErrorLog(e, "Unable to get input map for: " + UIElement);
            }
        });
    }

    /**
     * Retrieves working directory of PolyGlot
     *
     * @return current working directory
     */
    public File getWorkingDirectory() {
        return core.getOSHandler().getWorkingDirectory();
    }

    public static PolyGlot getPolyGlot() {
        return PolyGlot.polyGlot;
    }

    public DesktopOptionsManager getOptionsManager() {
        return optionsManager;
    }

    public void saveOptionsIni() throws IOException {
        DesktopIOHandler.getInstance().writeOptionsIni(getWorkingDirectory().getAbsolutePath(), optionsManager);
    }

    /**
     * Clipboard can be used to hold any object
     *
     * @param c object to hold
     */
    public void setClipBoard(Object c) {
        clipBoard = c;
    }

    /**
     * Retrieves object held in clipboard, even if null, regardless of type
     *
     * @return contents of clipboard
     */
    public Object getClipBoard() {
        return clipBoard;
    }

    public UIDefaults getUiDefaults() {
        return uiDefaults;
    }

    public void refreshUiDefaults() {
        uiDefaults = VisualStyleManager.generateUIOverrides(optionsManager.isNightMode());
        if (rootWindow != null) {
            Point location = rootWindow.getLocation();
            Dimension size = rootWindow.getSize();

            rootWindow.dispose(false);
            rootWindow = new ScrMainMenu(core);
            rootWindow.setVisible(true);
            rootWindow.selectFirstAvailableButton();
            rootWindow.setLocation(location);
            rootWindow.setSize(size);
        }
    }

    /**
     * Creates and returns testing shell to be used in file veracity tests
     * (IOHandler writing of files)
     *
     * @param _core
     * @return
     * @throws java.io.IOException
     */
    public static PolyGlot getTestShell(DictCore _core) throws IOException, Exception {
        var osHandler = new DesktopOSHandler(
                DesktopIOHandler.getInstance(),
                new DummyInfoBox(),
                new DesktopHelpHandler(),
                new PFontHandler()
        );

        osHandler.setWorkingDirectory(Files.createTempDirectory("POLYGLOT").toFile().getAbsolutePath());

        return new PolyGlot(_core, osHandler);
    }

    public void setCore(DictCore _core) {
        this.core = _core;
    }

    public DesktopOSHandler getOSHandler() {
        return this.osHandler;
    }

    public void setOSHandler(DesktopOSHandler _osHandler) {
        this.osHandler = _osHandler;
    }

    /**
     * Sets root window (the ScrMainMenu object that the user sees)
     *
     * @param _rootWindow
     */
    public void setRootWindow(ScrMainMenu _rootWindow) {
        rootWindow = _rootWindow;
    }

    /**
     * Returns root window of PolyGlot
     *
     * @return
     */
    public ScrMainMenu getRootWindow() {
        return rootWindow;
    }
    
    public static void startWebService() throws Exception {
        if (webService == null) {
            webService = new WebService(polyGlot);
        }
        
        if (!webService.isRunning()) {
            webService.doServe();
        }
    }
    
    public static void stopWebService() {
        if (webService != null) {
            webService.shutDown();
            webService = null;
        }
    }
    
    public static boolean isWebServiceRunning() {
        return webService != null && webService.isRunning();
    }
    
    public static String getWebServiceLog() {
        if (webService != null) {
            return webService.getLog();
        }
        
        return "";
    }

    /**
     * Saves periodically to temp file location (to be reopened on
     * freeze/disaster)
     */
    public void autoSave() {
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                if (core != null && !rootWindow.isDisposed() && !PGTUtil.isInJUnitTest()) {
                    try {
                        core.writeFile(autoSaveFile.getAbsolutePath(), false);
                    }
                    catch (IOException e) {
                        // Fail silently
                    }
                    catch (ParserConfigurationException | TransformerException e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e, "Autosave Exeption");
                    }
                    finally {
                        autoSave();
                    }
                }
            }
        }, polyGlot.getOptionsManager().getMsBetweenSaves());
    }

    /**
     * Fetches autosave file for PolyGlot
     *
     * @return
     */
    public File getNewAutoSaveFile() {
        String path = getWorkingDirectory().getAbsolutePath()
                + File.separator
                + PGTUtil.AUTO_SAVE_FILE_NAME;

        return new File(path);
    }

    /**
     * Used for testing purposes only
     *
     * @param _polyGlot
     * @throws java.lang.Exception
     */
    public static void setTestPolyGlot(PolyGlot _polyGlot) throws Exception {
        if (!PGTUtil.isInJUnitTest() && !PGTUtil.isUITestingMode()) {
            throw new Exception("ONLY TO BE RUN AS SETUP FOR TESTING");
        }

        PolyGlot.polyGlot = _polyGlot;
    }
}
