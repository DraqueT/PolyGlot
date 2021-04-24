/*
 * Copyright (c) 2019-2021, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina;

import java.awt.Color;
import java.awt.Desktop;
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
import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.ManagersCollections.OptionsManager;
import org.darisadesigns.polyglotlina.ManagersCollections.VisualStyleManager;
import org.darisadesigns.polyglotlina.Screens.ScrAbout;
import org.darisadesigns.polyglotlina.Screens.ScrMainMenu;

/**
 * Starts up PolyGlot and does testing for OS/platform that would be
 * inappropriate elsewhere
 *
 * @author Draque Thompson
 */
public final class PolyGlot {

    private final OptionsManager optionsManager;
    private final String overrideProgramPath;
    private Object clipBoard;
    private UIDefaults uiDefaults;
    private DictCore core;

    private PolyGlot(String overridePath) throws Exception {
        overrideProgramPath = overridePath; // TODO: In the future, figure out how this might be better set. In options?
        optionsManager = new OptionsManager();
        DesktopIOHandler.getInstance().loadOptionsIni(optionsManager, getWorkingDirectory().getAbsolutePath());
        refreshUiDefaults();
    }

    /**
     * @param args the command line arguments: open file path (blank if none),
     * in chunks if spaces in path
     */
    public static void main(final String[] args) {
        try {
            // must be set before accessing System to test OS (values will simply be ignored for other OSes
            if (PGTUtil.IS_OSX) {
                // set program icon
                Taskbar.getTaskbar().setIconImage(PGTUtil.POLYGLOT_ICON.getImage());
            }

            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", PGTUtil.DISPLAY_NAME);
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", PGTUtil.DISPLAY_NAME);

            setupNimbus();
            setupCustomUI();
            conditionalBetaSetup();
            setupOSSpecificCutCopyPaste();
        }
        catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, "Startup Exception");
            InfoBox.error("PolyGlot Error", "A serious error has occurred: " + e.getLocalizedMessage(), null);
            throw e;
        }

        java.awt.EventQueue.invokeLater(() -> {
            ScrMainMenu s = null;

            // catch all top level application killing throwables (and bubble up directly to ensure reasonable behavior)
            try {
                // separated due to serious nature of Throwable vs Exception

                PolyGlot polyGlot = new PolyGlot("");
                DictCore core = new DictCore(polyGlot, DesktopIOHandler.getInstance());

                try {
                    DesktopIOHandler.getInstance().loadOptionsIni(polyGlot.optionsManager, polyGlot.getWorkingDirectory().getAbsolutePath());
                }
                catch (Exception ex) {
                    DesktopIOHandler.getInstance().writeErrorLog(ex);
                    InfoBox.error("Options Load Error", "Unable to load options file or file corrupted:\n"
                            + ex.getLocalizedMessage(), core.getRootWindow());
                    DesktopIOHandler.getInstance().deleteIni(polyGlot.getWorkingDirectory().getAbsolutePath());
                }

                s = new ScrMainMenu(core);
                s.checkForUpdates(false);
                s.setVisible(true);

                // runs additional integration if on OSX system
                if (PGTUtil.IS_OSX) {
                    Desktop desk = Desktop.getDesktop();
                    final ScrMainMenu staticScr = s;

                    desk.setOpenFileHandler(e -> {
                        List<File> files = e.getFiles();

                        if (files.size() <= 0) {
                            return;
                        } else if (files.size() > 1) {
                            InfoBox.info("File Limit", "PolyGlot can only open a single file at once.\nOpening first selected file:"
                                    + files.get(0).getName(), null);
                        }

                        try {
                            // saveOrCancelTest to prevent accidental failure to save open languages
                            if (polyGlot.getCore().getRootWindow().saveOrCancelTest()) {
                                String filePath = files.get(0).getCanonicalPath();
                                polyGlot.getCore().getRootWindow().openFileFromPath(filePath);
                            }
                        }
                        catch (IOException | IllegalStateException ex) {
                            InfoBox.error("File Read Error", "Unable to open file due to error:\n"
                                    + ex.getLocalizedMessage(), null);
                        }
                    });

                    desk.setQuitHandler((QuitEvent e, QuitResponse response) -> {
                        staticScr.dispose();
                    });

                    desk.setPreferencesHandler((PreferencesEvent e) -> {
                        staticScr.showOptions();
                    });

                    desk.setAboutHandler((AboutEvent e) -> {
                        ScrAbout.run(new DictCore(polyGlot, DesktopIOHandler.getInstance()));
                    });

                    desk.setPrintFileHandler((PrintFilesEvent e) -> {
                        staticScr.printToPdf();
                    });
                }

                // if a recovery file exists, query user for action
                File recovery = polyGlot.findRecoveryFile(s, core.getWorkingDirectory());

                // open file if one is provided via arguments (but only if no recovery file- that takes precedence)
                if (args.length > 0 && recovery == null) {
                    String filePath = "";

                    // file paths with spaces in their names are broken into multiple arguments. This is a best guess. (multiple spaces could exist)
                    // TODO: Remove once this is fixed in Java
                    for (String pathChunk : args) {
                        filePath += " " + pathChunk;
                    }

                    filePath = filePath.trim();

                    // arguments passed in by the OS choke on special charaters as of Java 14 release (jpackage issue, probably)
                    // TODO: Remove once this is fixed in Java
                    if (new File(filePath).exists()) {
                        s.setFile(filePath);
                    } else {
                        InfoBox.warning("File Path Error", "Unable to open: " + filePath
                                + "\nPlease retry opening this file by clicking File->Open from the menu.", null);
                    }
                }

                // if a language has been loaded, open Lexicon
                if (!polyGlot.getCore().getCurFileName().isBlank()) {
                    polyGlot.getCore().getRootWindow().openLexicon(true);
                }
            }
            catch (ArrayIndexOutOfBoundsException e) {
                DesktopIOHandler.getInstance().writeErrorLog(e, "Problem with top level PolyGlot arguments.");
                InfoBox.error("Unable to start", "Unable to open PolyGlot main frame: \n"
                        + e.getMessage() + "\n"
                        + "Problem with top level PolyGlot arguments.", null);
            }
            catch (Exception e) { // split up for logical clarity... might want to differentiate
                DesktopIOHandler.getInstance().writeErrorLog(e);
                InfoBox.error("Unable to start", "Unable to open PolyGlot main frame: \n"
                        + e.getMessage() + "\n"
                        + "Please contact developer (draquemail@gmail.com) for assistance.", null);

                if (s != null) {
                    s.dispose();
                }
            }
            catch (Throwable t) {
                InfoBox.error("PolyGlot Error", "A serious error has occurred: " + t.getLocalizedMessage(), null);
                DesktopIOHandler.getInstance().writeErrorLog(t);
                throw t;
            }
        });
    }

    /**
     * Seeks recovery file, queries user what to do with it, and returns value
     * as appropriate
     *
     * @param s
     * @param workingDirectory
     * @return
     * @throws IOException
     */
    private File findRecoveryFile(ScrMainMenu s, File workingDirectory) throws IOException {
        File recovery = DesktopIOHandler.getInstance().getTempSaveFileIfExists(workingDirectory);
        if (recovery != null) {
            if (InfoBox.yesNoCancel("Recovery File Detected",
                    "PolyGlot appears to have shut down mid save. Would you like to recover the file?", s) == JOptionPane.YES_OPTION) {
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
                            InfoBox.info("Success!", "Language successfully recovered!", s);
                        } else {
                            throw new IOException("File not copied.");
                        }
                    }
                    catch (IOException e) {
                        InfoBox.error("Recovery Problem", "Unable to recover file due to: "
                                + e.getLocalizedMessage()
                                + ". Recovery file exists at location: "
                                + recovery.toPath()
                                + ". To attempt manual recovery, add .pgd suffix to file name and open with PolyGlot by hand.", s);
                    }
                } else {
                    InfoBox.info("Recovery Cancelled", "Recovery Cancelled. Restart PolyGlot to be prompted again.", s);
                }
            } else {
                if (InfoBox.yesNoCancel("Delete Recovery File", "Archive the recovery file, then?", s) == JOptionPane.YES_OPTION) {
                    DesktopIOHandler.getInstance().archiveFile(recovery, core.getWorkingDirectory());
                }

                recovery = null;
            }
        }

        return recovery;
    }

    public DictCore getNewCore(ScrMainMenu rootWindow) {
        DictCore ret = new DictCore(this, DesktopIOHandler.getInstance());
        ret.setRootWindow(rootWindow);
        return ret;
    }

    /**
     * Displays beta message if appropriate (beta builds have warning text
     * within lib folder) Sets version to display as beta
     */
    private static void conditionalBetaSetup() {
        if (PGTUtil.IS_BETA && !PGTUtil.isInJUnitTest()) { // This requires user interaction and is not covered by the test
            InfoBox.warning("BETA BUILD", "This is a pre-release, beta build of PolyGlot. Please use with care.\n\nBuild Date: " + PGTUtil.BUILD_DATE_TIME, null);
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
        UIManager.put("ScrollBarUI", "org.darisadesigns.polyglotlina.CustomControls.PScrollBarUI");
        UIManager.put("SplitPaneUI", "org.darisadesigns.polyglotlina.CustomControls.PSplitPaneUI");
        UIManager.put("ToolTipUI", "org.darisadesigns.polyglotlina.CustomControls.PToolTipUI");
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
        return overrideProgramPath.isEmpty()
                ? PGTUtil.getDefaultDirectory()
                : new File(overrideProgramPath);
    }

    public String getOverrideProgramPath() {
        return overrideProgramPath;
    }

    public OptionsManager getOptionsManager() {
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
    }

    /**
     * Creates and returns testing shell to be used in file veracity tests
     * (IOHandler writing of files)
     *
     * @return
     * @throws java.io.IOException
     */
    public static PolyGlot getTestShell() throws IOException, Exception {
        return new PolyGlot(Files.createTempDirectory("POLYGLOT").toFile().getAbsolutePath());
    }

    public DictCore getCore() {
        return core;
    }

    public void setCore(DictCore _core) {
        this.core = _core;
    }
}
