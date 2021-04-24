/*
 * Copyright (c) 2017-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Screens;

import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.CustomControls.PButton;
import org.darisadesigns.polyglotlina.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.CustomControls.PToDoTree;
import org.darisadesigns.polyglotlina.CustomControls.PToDoTreeModel;
import org.darisadesigns.polyglotlina.CustomControls.ToDoTreeNode;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.darisadesigns.polyglotlina.CheckLanguageErrors;
import org.darisadesigns.polyglotlina.ClipboardHandler;
import org.darisadesigns.polyglotlina.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.HelpHandler;
import org.darisadesigns.polyglotlina.Java8Bridge;
import org.darisadesigns.polyglotlina.ManagersCollections.OptionsManager;
import org.darisadesigns.polyglotlina.ToolsHelpers.ExportSpellingDictionary;
import org.darisadesigns.polyglotlina.WebInterface;

/**
 * Primary window for PolyGlot interface. Main running class that instantiates
 * core and handles other windows/UI. Depends on DictCore for all heavy logical
 * lifting behind the scenes.
 *
 * @author draque.thompson
 */
public final class ScrMainMenu extends PFrame {

    private final JMenuItem accelPublish = new JMenuItem();
    private final JMenuItem accelSave = new JMenuItem();
    private final JMenuItem accelNew = new JMenuItem();
    private final JMenuItem accelExit = new JMenuItem();
    private final JMenuItem accelOpen = new JMenuItem();
    private PToDoTree toDoTree;
    private PFrame curWindow = null;
    private final ScrLexicon cacheLexicon;
    private Image backGround;
    private final List<Window> childWindows = new ArrayList<>();

    /**
     * Creates new form ScrMainMenu
     *
     * Note: Single time setup per app run requires handing self to other objects
     * 
     * @param _core DictCore menus run on
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ScrMainMenu(DictCore _core) {
        super(_core);

        initComponents();

        core.setRootWindow(this);
        toDoTree = new PToDoTree(nightMode, menuFontSize);
        cacheLexicon = ScrLexicon.run(core, this);

        setupEasterEgg();

        try {
            backGround = ImageIO.read(getClass().getResource(PGTUtil.MAIN_MENU_IMAGE));
            jLabel1.setFont(PGTUtil.MENU_FONT.deriveFont(45f));
        } catch (IOException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            InfoBox.error("Resource Error",
                    "Unable to load internal resource: " + e.getLocalizedMessage(),
                    core.getRootWindow());
        }

        updateAllValues(core);
        genTitle();
        setupButtonPopouts();
        setupAccelerators();
        setupToDo();
        populateRecentOpened();
        populateExampleLanguages();
        populateSwadeshMenu();
        checkJavaVersion();
        super.setSize(super.getPreferredSize());
        addBindingsToPanelComponents(this.getRootPane());
        setupStartButtonPositining();
        setupForm();
    }
    
    private void setupForm() {
        if (core.getOptionsManager().isMaximized()) {
            setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
    }
    
    /**
     * ensures buttons are positioned in a way that doesn't look stupid
     */
    private void setupStartButtonPositining() {
        pnlStartButtons.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e) {
                btnNewLang.setLocation((pnlStartButtons.getWidth() - btnNewLang.getSize().width)/2, btnNewLang.getLocation().y);
            }
        });
    }

    /**
     * Populates Swadesh menu list with both predefined lists and the option to
     * import one
     */
    private void populateSwadeshMenu() {
        final DictCore finalCore = core;
        mnuSwadesh.removeAll();

        for (String list : PGTUtil.SWADESH_LISTS) {
            String menuName = list.replace("_", " ");
            final String finalLocation = PGTUtil.SWADESH_LOCATION + list;

            JMenuItem nextList = new JMenuItem("Load " + menuName);
            nextList.setToolTipText("Loads words from " + menuName + " into your lexicon.");
            nextList.addActionListener((ActionEvent evt)-> {
                URL swadUrl = ScrMainMenu.class.getResource(finalLocation);
                try ( BufferedInputStream bs = new BufferedInputStream(swadUrl.openStream())) {
                    finalCore.getWordCollection().loadSwadesh(bs, true);
                    updateAllValues(finalCore);
                } catch (Exception ex) {
                    InfoBox.error("Unable to load internal resource: ", finalLocation, curWindow);
                    DesktopIOHandler.getInstance().writeErrorLog(ex, "Resource read error on open.");
                }
            });

            mnuSwadesh.add(nextList);
        }

        JMenuItem customImport = new JMenuItem("Load Custom Swadesh");
        customImport.setToolTipText("Loads words from custom Swadesh list into your lexicon.");
        customImport.addActionListener((ActionEvent e) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setDialogTitle("Select line delimited Swadesh list.");
            
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try ( InputStream is = new FileInputStream(file);  BufferedInputStream bs = new BufferedInputStream(is)) {
                    finalCore.getWordCollection().loadSwadesh(bs, true);
                    updateAllValues(finalCore);
                } catch (Exception ex) {
                    InfoBox.error("Swadesh Load Error",
                            "Could not load selected Swadesh List. Please make certain it is formatted correctly (newline separated)", null);
                    DesktopIOHandler.getInstance().writeErrorLog(ex, "Swadesh load error");
                }
            }
        });

        mnuSwadesh.add(customImport);
    }

    /**
     * Populates help menu with example dictionary files.
     */
    private void populateExampleLanguages() {
        try {
            File exLangFolder = DesktopIOHandler.getInstance().unzipResourceToTempLocation(PGTUtil.EXAMPLE_LANGUAGE_ARCHIVE_LOCATION);
            File[] files = exLangFolder.listFiles();

            if (files != null) {
                for (File exampleLang : files) {
                    final String title = exampleLang.getName().replace("_", " ").replace(".pgd", "");
                    final String location = exampleLang.getAbsolutePath();

                    JMenuItem mnuExample = new JMenuItem(title);
                    mnuExample.addActionListener((ActionEvent evt) -> {
                        // only open if save/cancel test is passed
                        if (!saveOrCancelTest()) {
                            return;
                        }
                        
                        setFile(location);
                    });

                    mnuExLex.add(mnuExample);
                }
            }
        } catch (IOException e) {
            InfoBox.error("Resource Error", "Failed to load example dictionaries.", this);
            DesktopIOHandler.getInstance().writeErrorLog(e, "Failed to load example dictionaries.");
        }
    }

    /**
     * Warns user if they are using a beta version (based on beta warning file)
     */
    public void warnBeta() {
        if (DesktopIOHandler.getInstance().fileExists("BETA_WARNING.txt")) {
            InfoBox.warning("BETA VERSION", "You are using a beta version of PolyGlot. Please proceed with caution!", this);
        }
    }

    @Override
    public void saveAllValues() {
        if (curWindow != null) {
            curWindow.saveAllValues();
        }

        cacheLexicon.saveAllValues();
    }

    /**
     * Externally visible method for enabling logograph menu button
     *
     * @param enable
     */
    public void setEnabledLogoButton(boolean enable) {
        btnLogos.setEnabled(enable);

        if (((PButton) btnLogos).isActiveSelected() && !enable) {
            changeToLexicon();
        }
    }

    public boolean isEnabledLogoButton() {
        return btnLogos.isEnabled();
    }

    /**
     * For the purposes of startup with file
     *
     * @param switchTo
     * @return
     */
    public Window openLexicon(boolean switchTo) {
        saveAllValues();
        cacheLexicon.updateAllValues(core);

        if (switchTo) {
            changeScreen(cacheLexicon, cacheLexicon.getWindow(), null);
        }

        return cacheLexicon;
    }

    @Override
    public void dispose() {
        this.dispose(true);
    }

    public void dispose(boolean doExit) {
        if (doExit) { // skip saving file if not exiting program...
            // only exit if save/cancel test is passed and current window is legal to close
            if (!saveOrCancelTest() || (curWindow != null && !curWindow.canClose())) {
                return;
            }

            if (curWindow != null && !curWindow.isDisposed()) {
                // make certain that all actions necessary for saving information are complete
                curWindow.dispose();
            }
        }

        killAllChildren();

        super.dispose();

        if (doExit) { // skip saving options if not exiting program...
            OptionsManager opMan = core.getOptionsManager();
            
            // Note: this only applies to Windows - Mac OS requires reflection or implementing mac only class on form classes (no)
            boolean isMaximized = (this.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
            opMan.setMaximized(isMaximized);
            
            // do not overwerite last windowed size if maximized
            if (!isMaximized) {
                opMan.setScreenPosition(getClass().getName(), getLocation());
                opMan.setToDoBarPosition(pnlToDoSplit.getDividerLocation());
            }
            

            try {
                core.saveOptionsIni();
            } catch (IOException e) {
                // save error likely due to inability to write to disk, disable logging
                // IOHandler.writeErrorLog(e);
                InfoBox.warning("INI Save Error", e.getLocalizedMessage(), core.getRootWindow());
            }

            System.exit(0);
        }
    }

    /**
     * Kills all children. Hiding under the covers won't save them.
     */
    private void killAllChildren() {
        for (Window child : childWindows) {
            child.dispose();
        }

        childWindows.clear();
    }

    /**
     * Checks to make certain Java is a high enough version. Informs user and
     * quits otherwise.
     */
    private void checkJavaVersion() {
        String javaVersion = System.getProperty("java.version");

        if (javaVersion.startsWith("1.0")
                || javaVersion.startsWith("1.1")
                || javaVersion.startsWith("1.2")
                || javaVersion.startsWith("1.3")
                || javaVersion.startsWith("1.4")
                || javaVersion.startsWith("1.5")
                || javaVersion.startsWith("1.6")) {
            InfoBox.error("Please Upgrade Java", "Java " + javaVersion
                    + " must be upgraded to run PolyGlot. Version 1.7 or higher is required.\n\n"
                    + "Please upgrade at https://java.com/en/download/.", core.getRootWindow());
            System.exit(0);
        }
    }

    /**
     * Populates recently opened files menu
     */
    private void populateRecentOpened() {
        mnuRecents.removeAll();
        String[] lastFiles = core.getOptionsManager().getLastFiles();
        DefaultListModel<RecentFile> mdlRecentOpened = new DefaultListModel<>();
        lstRecentOpened.setModel(mdlRecentOpened);

        for (int i = lastFiles.length - 1; i >= 0; i--) {
            final String curFile = lastFiles[i];
            Path p = Paths.get(curFile);
            String fileName = p.getFileName().toString();
            JMenuItem lastFile = new JMenuItem();
            lastFile.setText(fileName);
            lastFile.setToolTipText(curFile);
            lastFile.addActionListener((java.awt.event.ActionEvent evt) -> {
                // only open if save/cancel test is passed
                if (!saveOrCancelTest()) {
                    return;
                }

                setFile(curFile);
                populateRecentOpened();
            });
            
            mnuRecents.add(lastFile);
            mdlRecentOpened.addElement(new RecentFile(fileName, curFile));
        }
    }

    public void setFile(String fileName) {
        core = core.getNewCore();

        try {
            core.readFile(fileName);

            if (curWindow == null) {
                saveAllValues();
                cacheLexicon.updateAllValues(core);
                changeScreen(cacheLexicon, cacheLexicon.getWindow(), null);
            }
        } catch (IOException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            core = core.getNewCore(); // don't allow partial loads
            InfoBox.error("File Read Error", "Could not read file: " + fileName
                    + "\n\n " + e.getMessage(), core.getRootWindow());
        } catch (IllegalStateException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            InfoBox.warning("File Read Problems", "Problems reading file:\n"
                    + e.getLocalizedMessage(), core.getRootWindow());
        }

        genTitle();
        updateAllValues(core);
    }

    /**
     * Gives user option to save file, returns continue/don't continue
     *
     * @return true to signal continue, false to signal stop
     */
    public boolean saveOrCancelTest() {
        boolean ret = true;

        if (core != null && !core.isLanguageEmpty()) {
            int saveFirst = InfoBox.yesNoCancel("Save First?",
                    "Save current language before performing action?", core.getRootWindow());

            if (saveFirst == JOptionPane.YES_OPTION) {
                boolean saved = saveFile();

                // if the file didn't save (usually due to a last minute cancel) don't continue.
                if (!saved) {
                    ret = false;
                }
            } else if (saveFirst == JOptionPane.CANCEL_OPTION
                    || saveFirst == JOptionPane.DEFAULT_OPTION) {
                ret = false;
            }
        }

        return ret;
    }

    /**
     * save file, open save as dialog if no file name already
     *
     * @return true if file saved, false otherwise
     */
    public boolean saveFile() {
        boolean ret;
        String curFileName = core.getCurFileName();

        if (curFileName.isEmpty()) {
            saveFileAsDialog();
            curFileName = core.getCurFileName();
        }

        // if it still is blank, the user has hit cancel on the save as dialog
        if (curFileName.isEmpty()) {
            ret = false;
        } else {
            core.getOptionsManager().pushRecentFile(curFileName);
            populateRecentOpened();
            saveAllValues();
            genTitle();

            ret = doWrite(curFileName);
        }

        return ret;
    }

    /**
     * Writes the file by calling the core
     *
     * @param _fileName path to write to
     * @return returns success/failure
     */
    private boolean doWrite(final String _fileName) {
        boolean cleanSave = false;
        String curFileName = core.getCurFileName();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            core.writeFile(_fileName);
            cleanSave = true;
        } catch (IOException | ParserConfigurationException
                | TransformerException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            InfoBox.error("Save Error", "Unable to save to file: "
                    + curFileName + "\n\n" + e.getMessage(), core.getRootWindow());
        }

        setCursor(Cursor.getDefaultCursor());

        if (cleanSave) {
            InfoBox.info("Success", "Language saved to: "
                    + curFileName + ".", core.getRootWindow());
        }

        return cleanSave;
    }

    /**
     * Changes window to lexicon (or refreshes if currently selected.
     */
    public void changeToLexicon() {
        btnLexicon.doClick();
    }

    /**
     * Provides dialog for Save As, and returns boolean to determine whether
     * file save should take place. DOES NOT SAVE.
     *
     * @return true if file saved, false otherwise
     */
    private boolean saveFileAsDialog() {
        boolean ret = false;
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PolyGlot Dictionaries", "pgd");
        String curFileName = core.getCurFileName();

        chooser.setDialogTitle("Save Language");
        chooser.setFileFilter(filter);
        chooser.setApproveButtonText("Save");
        if (curFileName.isEmpty()) {
            chooser.setCurrentDirectory(core.getWorkingDirectory());
        } else {
            chooser.setCurrentDirectory(DesktopIOHandler.getInstance().getDirectoryFromPath(curFileName));
            chooser.setSelectedFile(DesktopIOHandler.getInstance().getFileFromPath(curFileName));
        }

        String fileName;

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
            // if user has not provided an extension, add one
            if (!fileName.contains(".pgd")) {
                fileName += ".pgd";
            }

            if (DesktopIOHandler.getInstance().fileExists(fileName)) {
                int overWrite = InfoBox.yesNoCancel("Overwrite Dialog",
                        "Overwrite existing file? " + fileName, core.getRootWindow());

                if (overWrite == JOptionPane.NO_OPTION) {
                    ret = saveFileAsDialog();
                } else if (overWrite == JOptionPane.YES_OPTION) {
                    core.setCurFileName(fileName);
                    ret = true;
                }
            } else {
                core.setCurFileName(fileName);
                ret = true;
            }
        }

        return ret;
    }

    /**
     * Creates new, blank language file
     *
     * @param performTest whether the UI ask for confirmation
     */
    public void newFile(boolean performTest) {
        if (performTest && !saveOrCancelTest()) {
            return;
        }

        core = core.getNewCore();
        core.refreshMainMenu();

        genTitle();

        if (curWindow == null && performTest) {
            saveAllValues();
            cacheLexicon.updateAllValues(core);
            changeScreen(cacheLexicon, cacheLexicon.getWindow(), null);
        }
    }

    public void genTitle() {
        String title = "PolyGlot";
        String curFileName = core.getCurFileName();

        if (curWindow != null && !curWindow.getTitle().isEmpty()) {
            title += "-" + curWindow.getTitle();
            String langName = core.getPropertiesManager().getLangName();

            if (!langName.isEmpty()) {
                title += " : " + langName;
            } else if (!curFileName.isEmpty()) {
                title += " : " + curFileName;
            }
        }

        setTitle(title);
    }

    /**
     * opens Language file
     */
    public void open() {
        String curFileName = core.getCurFileName();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Language File");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PolyGlot Languages", "pgd");
        chooser.setFileFilter(filter);
        String fileName;
        if (curFileName.isEmpty()) {
            chooser.setCurrentDirectory(core.getWorkingDirectory());
        } else {
            chooser.setCurrentDirectory(DesktopIOHandler.getInstance().getDirectoryFromPath(curFileName));
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // only open if save/cancel test is passed
            if (!saveOrCancelTest()) {
                return;
            }

            fileName = chooser.getSelectedFile().getAbsolutePath();
            openFileFromPath(fileName);
        }

        genTitle();
    }
    
    /**
     * Opens a file from a given path
     * @param path 
     */
    public void openFileFromPath(String path) {
        setFile(path);
        core.getOptionsManager().pushRecentFile(path);
        populateRecentOpened();
        updateAllChildValues(core);
    }

    /**
     * checks web for updates to PolyGlot
     *
     * @param verbose Set this to have messages post to user.
     */
    public void checkForUpdates(final boolean verbose) {
        Thread check = new Thread() {
            @Override
            public void run() {
                try {
                    ScrUpdateAlert.run(verbose, core);
                } catch (Exception e) {
                    DesktopIOHandler.getInstance().writeErrorLog(e);
                    if (verbose) {
                        InfoBox.error("Update Problem",
                                "Unable to check for update:\n"
                                + e.getLocalizedMessage(), core.getRootWindow());
                    }
                }
            }
        };

        check.start();
    }

    /**
     * Export dictionary to excel file
     */
    private void exportToExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Language to Excel");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xls");
        chooser.setFileFilter(filter);
        chooser.setApproveButtonText("Save");
        chooser.setCurrentDirectory(core.getWorkingDirectory());

        String fileName = core.getCurFileName().replaceAll(".pgd", ".xls");
        chooser.setSelectedFile(new File(fileName));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }

        if (!fileName.contains(".xls")) {
            fileName += ".xls";
        }

        if (!new File(fileName).exists()
                || InfoBox.actionConfirmation("Overwrite File?", "File with this name and location already exists. Continue/Overwrite?", this)) {
            try {
                Java8Bridge.exportExcelDict(fileName, core,
                        InfoBox.actionConfirmation("Excel Export",
                                "Export all declensions? (Separates parts of speech into individual tabs)",
                                core.getRootWindow()));

                // only prompt user to open if Desktop supported
                if (Desktop.isDesktopSupported()) {
                    if (InfoBox.actionConfirmation("Export Sucess", "Language exported to " + fileName + ".\nOpen now?", this)) {
                        Desktop.getDesktop().open(new File(fileName));
                    }
                } else {
                    InfoBox.info("Export Status", "Language exported to " + fileName + ".", core.getRootWindow());
                }
            } catch (IOException e) {
                DesktopIOHandler.getInstance().writeErrorLog(e);
                InfoBox.info("Export Problem", e.getLocalizedMessage(), core.getRootWindow());
            }
        }
    }

    /**
     * Prompts user for a location and exports font within PGD to given path
     *
     * @param exportCharis set to true to export charis, false to export con
     * font
     */
    public void exportFont(boolean exportCharis) {
        JFileChooser chooser = new JFileChooser();
        String fileName = core.getCurFileName().replaceAll(".pgd", ".ttf");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Font Files", "ttf");

        chooser.setDialogTitle("Export Font");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(core.getWorkingDirectory());
        chooser.setApproveButtonText("Save");
        chooser.setSelectedFile(new File(fileName));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }

        if (!fileName.contains(".")) {
            fileName += ".ttf";
        }

        if (DesktopIOHandler.getInstance().fileExists(fileName)
                && !InfoBox.actionConfirmation("Overwrite Confirmation", "File will be overwritten. Continue?", this)) {
            return;
        }

        try {
            if (exportCharis) {
                DesktopIOHandler.getInstance().exportCharisFont(fileName);
            } else {
                DesktopIOHandler.getInstance().exportFont(fileName, core.getCurFileName());
            }
            InfoBox.info("Export Success", "Font exported to: " + fileName, core.getRootWindow());
        } catch (IOException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            InfoBox.error("Export Error", "Unable to export font: " + e.getMessage(), core.getRootWindow());
        }
    }

    private void ipaHit() {
        PFrame ipa = new ScrIPARefChart(core);
        ipa.setVisible(true);
        ipa.toFront();
    }

    private void openHelp() throws IOException {
        File readmeDir = DesktopIOHandler.getInstance().unzipResourceToTempLocation(PGTUtil.HELP_FILE_ARCHIVE_LOCATION);
        File readmeFile = new File(readmeDir.getAbsolutePath() + File.separator + PGTUtil.HELP_FILE_NAME);

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(readmeFile.toURI());
        } else if (PGTUtil.IS_LINUX) {
            Desktop.getDesktop().open(readmeFile);
        } else {
            InfoBox.warning("Menu Warning", "Unable to open browser. Please load manually at:\n"
                    + "http://draquet.github.io/PolyGlot/readme.html\n(copied to clipboard for convenience)", this);
            new ClipboardHandler().setClipboardContents("http://draquet.github.io/PolyGlot/readme.html");
        }
    }

    /**
     * Sets selection on lexicon by word id
     *
     * @param id
     */
    public void selectWordById(int id) {
        if (curWindow instanceof ScrLexicon) {
            ScrLexicon scrLexicon = (ScrLexicon) curWindow;
            scrLexicon.setWordSelectedById(id);
        } else {
            InfoBox.warning("Open Lexicon",
                    "Please open the Lexicon and select a word to use this feature.",
                    core.getRootWindow());
        }
    }

    /**
     * Retrieves current user selection (form dependent, and coded per each)
     *
     * @return current word selected in scrLexicon, null otherwise (or if
     * lexicon is not visible)
     */
    public ConWord getCurrentUserSelection() {
        ConWord ret = null;

        if (curWindow instanceof ScrLexicon) {
            ScrLexicon scrLexicon = (ScrLexicon) curWindow;
            scrLexicon.saveAllValues();
            ret = scrLexicon.getCurrentWord();
        } else {
            InfoBox.warning("Open Lexicon",
                    "Please open the Lexicon and select a word to use this feature.",
                    core.getRootWindow());
        }

        return ret;
    }

    private void deselectButtons() {
        ((PButton) btnProp).setActiveSelected(false);
        ((PButton) btnPos).setActiveSelected(false);
        ((PButton) btnLogos).setActiveSelected(false);
        ((PButton) btnLexicon).setActiveSelected(false);
        ((PButton) btnGrammar).setActiveSelected(false);
        ((PButton) btnClasses).setActiveSelected(false);
        ((PButton) btnPhonology).setActiveSelected(false);
        ((PButton) btnQuiz).setActiveSelected(false);
    }

    private void setupToDo() {
        int toDoPosition = core.getOptionsManager().getToDoBarPosition();

        javax.swing.JScrollPane jScrollPane = new javax.swing.JScrollPane();
        toDoTree = new PToDoTree(nightMode, menuFontSize);
        toDoTree.setToolTipText("To-Do list. Right click to add, remove, or rename tasks. Tasks can contain subtasks.");

        toDoTree.setSelectionModel(null);
        jScrollPane.setViewportView(toDoTree);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(pnlToDo);
        pnlToDo.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
        );

        if (toDoPosition >= 0) {
            pnlToDoSplit.setDividerLocation(toDoPosition);
        }

        populateToDo();
    }

    private void populateToDo() {
        toDoTree.setRootVisible(false);
        toDoTree.setModel(new PToDoTreeModel(ToDoTreeNode.createToDoTreeNode(core.getToDoManager().getRoot())));
        ((DefaultTreeModel) toDoTree.getModel()).nodeStructureChanged((ToDoTreeNode) toDoTree.getModel().getRoot());
    }

    private void setupEasterEgg() {
        class EnterKeyListener implements KeyEventPostProcessor {

            String lastChars = "------------------------------";

            @Override
            public boolean postProcessKeyEvent(KeyEvent e) {
                if (e != null && e.getKeyCode() == 0) {
                    lastChars = lastChars.substring(1);
                    lastChars += e.getKeyChar();

                    if (lastChars.toLowerCase().endsWith("what did you see last tuesday")) {
                        InfoBox.info("Coded Response", "A pink elephant.", null);
                    } else if (lastChars.toLowerCase().endsWith("this is the forest primeval")) {
                        InfoBox.info("Bearded with moss", "The murmuring pines and the hemlocks.", null);
                    } else if (lastChars.toLowerCase().endsWith("it can't outlast you")) {
                        InfoBox.info("Just human...", "Yes it can. You're not a kukun.", null);
                    } else if (lastChars.toLowerCase().endsWith("who's draque")
                            || lastChars.toLowerCase().endsWith("who is draque")) {
                        ScrEasterEgg.run(core.getRootWindow());
                    } else if (lastChars.endsWith("uuddlrlrba")) {
                        InfoBox.info("コナミコマンド", "30の命を与えます。", null);
                    }
                }

                return false;
            }
        }
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventPostProcessor(new EnterKeyListener());
    }

    public void setWordSelectedById(Integer id) {
        if (cacheLexicon != null) {
            cacheLexicon.setWordSelectedById(id);
        }
    }

    /**
     * Performs all actions necessary for changing the viewed panel
     *
     * @param newScreen new window to display
     * @param display component to be added as main display
     */
    private void changeScreen(PFrame newScreen, Component display, PButton button) {
        // simply fail if current window cannot close. Window is responsible
        // for informing user of reason.
        if (curWindow != null && !curWindow.canClose()) {
            newScreen.dispose();
            return;
        }
        
        // the search menu causes problems with cached Lexicon if left open
        if (curWindow instanceof ScrLexicon) {
            ((ScrLexicon)curWindow).closeAndClearSearchPanel();
        }

        // blank the menu
        pnlMain.removeAll();
        this.repaint();

        // resize screen
        if (curWindow != null) {
            // set size before disposing so that it will be properly saved to options
            curWindow.getWindow().setSize(pnlMain.getSize());
            curWindow.dispose();

            // after disposing, update new window in case old wrote anything to the core
            newScreen.updateAllValues(core);
        }

        // only resize if animation is enabled and the window isn't maximized
        if (core.getOptionsManager().isAnimateWindows() && getFrameState() != Frame.MAXIMIZED_BOTH) {
            Dimension dim = core.getOptionsManager().getScreenSize(newScreen.getClass().getName());

            if (dim == null) {
                dim = newScreen.getPreferredSize();
            }

            Insets insets = getInsets();

            this.setSizeSmooth(dim.width + pnlSideButtons.getWidth() + insets.left + insets.right,
                    dim.height + insets.bottom + insets.top);
        }

        // set new screen
        GroupLayout layout = new GroupLayout(pnlMain);
        pnlMain.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(display, javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(display, javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
        );

        curWindow = newScreen;

        if (button != null) {
            deselectButtons();
            button.setActiveSelected(true);
        }

        genTitle();
    }

    /**
     * Switches to first active menu button
     */
    public void selectFirstAvailableButton() {
        openLexicon();
    }

    private void setupButtonPopouts() {
        // not all buttons support this feature, but those that don't should still display it in grey
        setupDropdownMenu((PButton) btnLexicon, () -> {
            return null;
        }, HelpHandler.LEXICON_HELP, false);
        setupDropdownMenu((PButton) btnPos, () -> {
            return ScrTypes.run(core);
        }, HelpHandler.PARTSOFSPEECH_HELP, true);
        setupDropdownMenu((PButton) btnClasses, () -> {
            return new ScrWordClasses(core);
        }, HelpHandler.LEXICALCLASSES_HELP, true);
        setupDropdownMenu((PButton) btnGrammar, () -> {
            return new ScrGrammarGuide(core);
        }, HelpHandler.GRAMMAR_HELP, true);
        setupDropdownMenu((PButton) btnLogos, () -> {
            return new ScrLogoDetails(core);
        }, HelpHandler.LOGOGRAPHS_HELP, true);
        setupDropdownMenu((PButton) btnPhonology, () -> {
            return new ScrPhonology(core);
        }, HelpHandler.PHONOLOGY_HELP, true);
        setupDropdownMenu((PButton) btnProp, () -> {
            return new ScrLangProps(core);
        }, HelpHandler.LANGPROPERTIES_HELP, true);
        setupDropdownMenu((PButton) btnQuiz, () -> {
            return null;
        }, HelpHandler.QUIZGENERATOR_HELP, false);
    }

    /**
     *
     * @param button
     * @param setNewWindow
     * @param helpLocation
     * @param _enable
     */
    private void setupDropdownMenu(final PButton button, 
            Supplier<Window> setNewWindow, 
            String helpLocation, 
            boolean _enable) {
        final JPopupMenu buttonMenu = new JPopupMenu();
        final JMenuItem popOut = new JMenuItem("Pop Window Out");
        final JMenuItem help = new JMenuItem("Open Relevant Help Section");
        final boolean enable = _enable;

        if (enable) {
            button.setToolTipText(button.getToolTipText() + "\n(right click to pop window out or for help)");
            popOut.setToolTipText("Pops " + button.getText() + " into independent window.");
        } else {
            popOut.setToolTipText(button.getText() + " cannot be popped out.");
        }
        
        help.setToolTipText("Open readme to relevant section.");

        popOut.addActionListener((ActionEvent e) -> {
            Window w = setNewWindow.get();
            button.setEnabled(false);
            w.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent ex) {
                }
                
                @Override
                public void windowClosing(WindowEvent ex) {
                }
                
                @Override
                public void windowClosed(WindowEvent ex) {
                    childWindows.remove(w);
                    button.setEnabled(true);
                }
                
                @Override
                public void windowIconified(WindowEvent ex) {
                }
                
                @Override
                public void windowDeiconified(WindowEvent ex) {
                }
                
                @Override
                public void windowActivated(WindowEvent ex) {
                }
                
                @Override
                public void windowDeactivated(WindowEvent ex) {
                }
            });
            
            if (w instanceof PFrame) {
                ((PFrame) w).addWindowFocusListener(new WindowFocusListener() {
                    @Override
                    public void windowGainedFocus(WindowEvent e) {
                        
                    }
                    
                    @Override
                    public void windowLostFocus(WindowEvent e) {
                        ((PFrame) w).saveAllValues();
                    }
                });
            }
            
            w.setVisible(true);
            w.toFront();
            childWindows.add(w);
            
            if (button.isActiveSelected()) {
                selectFirstAvailableButton();
            }
        });

        buttonMenu.add(popOut);
        
        help.addActionListener((ActionEvent e) -> {
            HelpHandler.openHelpToLocation(helpLocation);
        });
        
        buttonMenu.add(help);

        button.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isPopupTrigger()
                        || SwingUtilities.isRightMouseButton(e)) {
                    doPop(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()
                        || SwingUtilities.isRightMouseButton(e)) {
                    doPop(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            private void doPop(MouseEvent e) {
                popOut.setEnabled(button.isEnabled() && enable);
                buttonMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    private void openLexicon() {
        saveAllValues();
        cacheLexicon.updateAllValues(core);
        changeScreen(cacheLexicon, cacheLexicon.getWindow(), (PButton) btnLexicon);
    }

    @Override
    public void updateAllValues(DictCore _core) {
        if (curWindow != null) {
            curWindow.updateAllValues(_core);
        }

        if (cacheLexicon != null) {
            cacheLexicon.updateAllValues(_core);
        }

        core = _core;

        updateAllChildValues(_core);
        populateSwadeshMenu();
    }

    private void updateAllChildValues(DictCore _core) {
        for (Window win : childWindows) {
            if (win instanceof PFrame) {
                ((PFrame) win).updateAllValues(_core);
            } else if (win instanceof PDialog) {
                ((PDialog) win).updateAllValues(_core);
            }
        }
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        // none for this window
    }

    @Override
    public Component getWindow() {
        throw new UnsupportedOperationException("The main window never returns a value here. Do not call this.");
    }

    /**
     * For now, always returns true... shouldn't ever be any upstream window,
     * regardless.
     *
     * @return
     */
    @Override
    public boolean canClose() {
        return true;
    }

    public void printToPdf() {
        ScrPrintToPDF.run(core);
    }

    /**
     * sets menu accelerators and menu item text to reflect this
     */
    public void setupAccelerators() {
        accelPublish.addActionListener((ActionEvent e) -> {
            ScrPrintToPDF.run(core);
        });
        this.rootPane.add(accelPublish);

        accelSave.addActionListener((java.awt.event.ActionEvent evt) -> {
            saveFile();
        });
        this.rootPane.add(accelSave);

        accelNew.addActionListener((java.awt.event.ActionEvent evt) -> {
            core.coreNew(true);
        });
        this.rootPane.add(accelNew);

        accelOpen.addActionListener((java.awt.event.ActionEvent evt) -> {
            core.coreOpen();
        });
        this.rootPane.add(accelOpen);

        accelExit.addActionListener((java.awt.event.ActionEvent evt) -> {
            dispose();
        });
        this.rootPane.add(accelExit);

        String OS = System.getProperty("os.name");
        if (OS.startsWith("Mac")) {
            accelSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.META_DOWN_MASK));
            accelNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.META_DOWN_MASK));
            accelExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.META_DOWN_MASK));
            accelOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.META_DOWN_MASK));
            accelPublish.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.META_DOWN_MASK));
        } else {
            accelSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.CTRL_DOWN_MASK));
            accelNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.CTRL_DOWN_MASK));
            accelExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.CTRL_DOWN_MASK));
            accelOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.CTRL_DOWN_MASK));
            accelPublish.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.CTRL_DOWN_MASK));
        }
    }
    
    @Override
    public DictCore getCore() {
        return core;
    }
    
    /**
     * This scales UI elements for the welcome window
     */
    private void scaleWelcomWindow() {
        if (pnlStartButtons != null && btnNewLang != null) {
            btnNewLang.setLocation((pnlStartButtons.getWidth() - btnNewLang.getSize().width)/2, btnNewLang.getLocation().y);
        }
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        scaleWelcomWindow();
    }
    
    private class RecentFile {
        public final String fileName;
        public final String path;
        
        public RecentFile(String _fileName, String _path) {
            fileName = _fileName;
            path = _path;
        }
        
        @Override
        public String toString() {
            return fileName;
        }
    }

    /**
     * Opens selected recent file from list on welcome screen (if any selected)
     */
    private void openRecentFromList() {
        if (lstRecentOpened.getSelectedIndex() != -1) {
            setFile(lstRecentOpened.getSelectedValue().path);
            openLexicon(true);
            populateRecentOpened();
        }
    }
    
    private void ExportDictionaryFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Dictionary File", "dic");
        String curFileName = core.getCurFileName();

        chooser.setDialogTitle("Export Custom Dictionary File");
        chooser.setFileFilter(filter);
        chooser.setApproveButtonText("Save");
        if (curFileName.isEmpty() || !curFileName.contains(".pgd")) {
            chooser.setCurrentDirectory(core.getWorkingDirectory());
        } else {
            String suggestedDicFile = curFileName.replaceAll("\\.pgd", ".dic");
            chooser.setCurrentDirectory(DesktopIOHandler.getInstance().getDirectoryFromPath(curFileName));
            chooser.setSelectedFile(DesktopIOHandler.getInstance().getFileFromPath(suggestedDicFile));
        }

        String fileName;

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();

            if (!fileName.contains(".dic")) {
                fileName += ".dic";
            }

            if (DesktopIOHandler.getInstance().fileExists(fileName)) {
                int overWrite = InfoBox.yesNoCancel("Overwrite Dialog",
                        "Overwrite existing file? " + fileName, core.getRootWindow());

                if (overWrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            ExportSpellingDictionary export = new ExportSpellingDictionary(core);
            try {
                export.ExportSpellingDictionary(fileName);
                InfoBox.info("Success", "File written to: " + fileName, this);
            }
            catch (IOException e) {
                DesktopIOHandler.getInstance().writeErrorLog(e);
                InfoBox.error("File Write Error", "Unable to export dictionary to: " 
                        + curFileName + "\n\n" + e.getLocalizedMessage(), this);
            }
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem2 = new javax.swing.JMenuItem();
        pnlToDoSplit = new javax.swing.JSplitPane();
        pnlToDo = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        pnlSideButtons = new javax.swing.JPanel();
        btnLexicon = new PButton(nightMode, menuFontSize);
        btnPos = new PButton(nightMode, menuFontSize);
        btnClasses = new PButton(nightMode, menuFontSize);
        btnGrammar = new PButton(nightMode, menuFontSize);
        btnLogos = new PButton(nightMode, menuFontSize);
        btnProp = new PButton(nightMode, menuFontSize);
        btnPhonology = new PButton(nightMode, menuFontSize);
        btnQuiz = new PButton(nightMode, menuFontSize);
        pnlMain = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backGround != null && curWindow == null) {
                    g.drawImage(backGround, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        jLabel1 = new PLabel("", menuFontSize);
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        pnlStartButtons = new javax.swing.JPanel();
        btnOpenLang = new PButton(nightMode, menuFontSize);
        btnOpenManual = new PButton(nightMode, menuFontSize);
        btnNewLang = new PButton(nightMode, menuFontSize) {
            @Override
            public void repaint() {
                scaleWelcomWindow();
                super.repaint();
            }
        };
        jScrollPane1 = new javax.swing.JScrollPane();
        lstRecentOpened = new javax.swing.JList<>();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        mnuNewLocal = new javax.swing.JMenuItem();
        mnuSaveLocal = new javax.swing.JMenuItem();
        mnuSaveAs = new javax.swing.JMenuItem();
        mnuOpenLocal = new javax.swing.JMenuItem();
        mnuRecents = new javax.swing.JMenu();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mnuPublish = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mnuExit = new javax.swing.JMenuItem();
        mnuTools = new javax.swing.JMenu();
        mnuExport = new javax.swing.JMenu();
        mnuExportToExcel = new javax.swing.JMenuItem();
        mnuExportFont = new javax.swing.JMenuItem();
        mnuExportDictFile = new javax.swing.JMenuItem();
        mnuImport = new javax.swing.JMenu();
        mnuImportFile = new javax.swing.JMenuItem();
        mnuImportFont = new javax.swing.JMenuItem();
        mnuCheckLanguage = new javax.swing.JMenuItem();
        mnuIpaTranslator = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuLexFamilies = new javax.swing.JMenuItem();
        mnuLangStats = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mnuIPAChart = new javax.swing.JMenuItem();
        mnuSwadesh = new javax.swing.JMenu();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        mnuOptions = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        mnuReversion = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuAbout = new javax.swing.JMenuItem();
        mnuChkUpdate = new javax.swing.JMenuItem();
        mnuExLex = new javax.swing.JMenu();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem8 = new javax.swing.JMenuItem();

        jMenuItem2.setText("jMenuItem2");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PolyGlot Language Construction Toolkit");
        setBackground(new java.awt.Color(255, 255, 255));

        pnlToDoSplit.setBackground(new java.awt.Color(255, 255, 255));
        pnlToDoSplit.setDividerLocation(675);

        pnlToDo.setBackground(new java.awt.Color(255, 255, 255));
        pnlToDo.setToolTipText("");
        pnlToDo.setMinimumSize(new java.awt.Dimension(1, 1));

        javax.swing.GroupLayout pnlToDoLayout = new javax.swing.GroupLayout(pnlToDo);
        pnlToDo.setLayout(pnlToDoLayout);
        pnlToDoLayout.setHorizontalGroup(
            pnlToDoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 117, Short.MAX_VALUE)
        );
        pnlToDoLayout.setVerticalGroup(
            pnlToDoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 430, Short.MAX_VALUE)
        );

        pnlToDoSplit.setRightComponent(pnlToDo);

        pnlSideButtons.setBackground(new java.awt.Color(102, 204, 255));
        pnlSideButtons.setMaximumSize(new java.awt.Dimension(4000, 4000));

        btnLexicon.setText("Lexicon");
        btnLexicon.setToolTipText("A customizable dictionary for your language's vocabulary");
        btnLexicon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLexiconActionPerformed(evt);
            }
        });

        btnPos.setText("Parts of Speech");
        btnPos.setToolTipText("Create both parts of speech and define how their declension/conjugation rules work here.");
        btnPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPosActionPerformed(evt);
            }
        });

        btnClasses.setText("Lexical Classes");
        btnClasses.setToolTipText("Create customizable classes, like gender, or create freetext fieldsfor vocabulary here.");
        btnClasses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClassesActionPerformed(evt);
            }
        });

        btnGrammar.setText("Grammar");
        btnGrammar.setToolTipText("Define grammar in a chapter/section based fashion, with formatted text, and audio recording/playback here.");
        btnGrammar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGrammarActionPerformed(evt);
            }
        });

        btnLogos.setText("Logographs");
        btnLogos.setToolTipText("Create and maintain a visual dictionary of logographs here.");
        btnLogos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogosActionPerformed(evt);
            }
        });

        btnProp.setText("Lang Properties");
        btnProp.setToolTipText("Miscelanious properties associated with your language such as alphabetical order go here.");
        btnProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPropActionPerformed(evt);
            }
        });

        btnPhonology.setText("Phonology & Text");
        btnPhonology.setToolTipText("Edit language phonology, orthography, etc. This is also where typographical replacement is set.");
        btnPhonology.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPhonologyActionPerformed(evt);
            }
        });

        btnQuiz.setText("Quiz Generator");
        btnQuiz.setToolTipText("Generate quizzes to help gain mastery of languages");
        btnQuiz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuizActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSideButtonsLayout = new javax.swing.GroupLayout(pnlSideButtons);
        pnlSideButtons.setLayout(pnlSideButtonsLayout);
        pnlSideButtonsLayout.setHorizontalGroup(
            pnlSideButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSideButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSideButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnLexicon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnClasses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnProp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLogos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnGrammar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPhonology, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnQuiz, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlSideButtonsLayout.setVerticalGroup(
            pnlSideButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSideButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnLexicon, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPos, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGrammar, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLogos, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPhonology, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnProp, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnQuiz)
                .addContainerGap(157, Short.MAX_VALUE))
        );

        pnlMain.setBackground(new java.awt.Color(255, 255, 255));
        pnlMain.setMaximumSize(new java.awt.Dimension(4000, 4000));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Welcome to PolyGlot");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("From here, you can open an existing language file, open the PolyGlot manual,");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("or simply begin work on the blank file currently loaded.");

        pnlStartButtons.setOpaque(false);

        btnOpenLang.setText("OPEN LANGUAGE");
        btnOpenLang.setToolTipText("Open an existing language");
        btnOpenLang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenLangActionPerformed(evt);
            }
        });

        btnOpenManual.setText("OPEN MANUAL");
        btnOpenManual.setToolTipText("Open the PolyGlot manual");
        btnOpenManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenManualActionPerformed(evt);
            }
        });

        btnNewLang.setText("NEW LANGUAGE");
        btnNewLang.setToolTipText("Open blank new language file.");
        btnNewLang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewLangActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlStartButtonsLayout = new javax.swing.GroupLayout(pnlStartButtons);
        pnlStartButtons.setLayout(pnlStartButtonsLayout);
        pnlStartButtonsLayout.setHorizontalGroup(
            pnlStartButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStartButtonsLayout.createSequentialGroup()
                .addComponent(btnOpenLang)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNewLang)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnOpenManual)
                .addContainerGap())
        );
        pnlStartButtonsLayout.setVerticalGroup(
            pnlStartButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStartButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnOpenLang)
                .addComponent(btnOpenManual)
                .addComponent(btnNewLang))
        );

        lstRecentOpened.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstRecentOpened.setToolTipText("Select a recently opened language to re-open it");
        lstRecentOpened.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstRecentOpenedMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(lstRecentOpened);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlStartButtons, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlStartButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(pnlSideButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSideButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pnlToDoSplit.setLeftComponent(jPanel3);

        jMenuBar1.setOpaque(false);

        mnuFile.setText("File");

        mnuNewLocal.setText("New");
        mnuNewLocal.setToolTipText("New PolyGlot Language File");
        mnuNewLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuNewLocalActionPerformed(evt);
            }
        });
        mnuFile.add(mnuNewLocal);

        mnuSaveLocal.setText("Save");
        mnuSaveLocal.setToolTipText("Save Current Language File");
        mnuSaveLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveLocalActionPerformed(evt);
            }
        });
        mnuFile.add(mnuSaveLocal);

        mnuSaveAs.setText("Save As");
        mnuSaveAs.setToolTipText("Save Current Language File As");
        mnuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveAsActionPerformed(evt);
            }
        });
        mnuFile.add(mnuSaveAs);

        mnuOpenLocal.setText("Open");
        mnuOpenLocal.setToolTipText("Open Existing Language File");
        mnuOpenLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenLocalActionPerformed(evt);
            }
        });
        mnuFile.add(mnuOpenLocal);

        mnuRecents.setText("Recent");
        mnuRecents.setToolTipText("Recently Opened Language Files");
        mnuFile.add(mnuRecents);
        mnuFile.add(jSeparator5);

        mnuPublish.setText("Publish Language to PDF");
        mnuPublish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPublishActionPerformed(evt);
            }
        });
        mnuFile.add(mnuPublish);
        mnuFile.add(jSeparator2);

        mnuExit.setText("Exit");
        mnuExit.setToolTipText("Exit PolyGlot");
        mnuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExitActionPerformed(evt);
            }
        });
        mnuFile.add(mnuExit);

        jMenuBar1.add(mnuFile);

        mnuTools.setText("Tools");

        mnuExport.setText("Export Tools");

        mnuExportToExcel.setText("Export to Excel");
        mnuExportToExcel.setToolTipText("Export language values to excel sheet");
        mnuExportToExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExportToExcelActionPerformed(evt);
            }
        });
        mnuExport.add(mnuExportToExcel);

        mnuExportFont.setText("Export Font");
        mnuExportFont.setToolTipText("Export font to file");
        mnuExportFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExportFontActionPerformed(evt);
            }
        });
        mnuExport.add(mnuExportFont);

        mnuExportDictFile.setText("Export Dic File");
        mnuExportDictFile.setToolTipText("Exports a dictionary file of all word forms (consumable for spellchecks and other programs)");
        mnuExportDictFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExportDictFileActionPerformed(evt);
            }
        });
        mnuExport.add(mnuExportDictFile);

        mnuTools.add(mnuExport);

        mnuImport.setText("Import Tools");
        mnuImport.setToolTipText("Tools for importing to PolyGlot");

        mnuImportFile.setText("Import from File");
        mnuImportFile.setToolTipText("Import language values from comma delimited file or excel sheet");
        mnuImportFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuImportFileActionPerformed(evt);
            }
        });
        mnuImport.add(mnuImportFile);

        mnuImportFont.setText("Import Font");
        mnuImportFont.setToolTipText("Import font from file");
        mnuImportFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuImportFontActionPerformed(evt);
            }
        });
        mnuImport.add(mnuImportFont);

        mnuTools.add(mnuImport);

        mnuCheckLanguage.setText("Check Language");
        mnuCheckLanguage.setToolTipText("Checks language for problems and inconsistencies.");
        mnuCheckLanguage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCheckLanguageActionPerformed(evt);
            }
        });
        mnuTools.add(mnuCheckLanguage);

        mnuIpaTranslator.setText("IPA Conversion Tool");
        mnuIpaTranslator.setToolTipText("Converts parahraph form phrases into IPA");
        mnuIpaTranslator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuIpaTranslatorActionPerformed(evt);
            }
        });
        mnuTools.add(mnuIpaTranslator);

        jMenuItem1.setText("Evolve Language");
        jMenuItem1.setToolTipText("Modify large segments of vocabulary at once (regex compatible)");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        mnuTools.add(jMenuItem1);
        mnuTools.add(jSeparator1);

        mnuLexFamilies.setText("Lexical Families");
        mnuLexFamilies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLexFamiliesActionPerformed(evt);
            }
        });
        mnuTools.add(mnuLexFamilies);

        mnuLangStats.setText("Language Statistics");
        mnuLangStats.setToolTipText("Generate Language Statistics page");
        mnuLangStats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLangStatsActionPerformed(evt);
            }
        });
        mnuTools.add(mnuLangStats);
        mnuTools.add(jSeparator4);

        mnuIPAChart.setText("Interactive IPA Chart");
        mnuIPAChart.setToolTipText("Opens interactive IPA chart");
        mnuIPAChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuIPAChartActionPerformed(evt);
            }
        });
        mnuTools.add(mnuIPAChart);

        mnuSwadesh.setText("Swadesh Lists");
        mnuSwadesh.setToolTipText("You can import Swadesh lists here");
        mnuTools.add(mnuSwadesh);
        mnuTools.add(jSeparator6);

        mnuOptions.setText("Options");
        mnuOptions.setToolTipText("PolyGlot Options");
        mnuOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOptionsActionPerformed(evt);
            }
        });
        mnuTools.add(mnuOptions);
        mnuTools.add(jSeparator7);

        mnuReversion.setText("Revert Language");
        mnuReversion.setToolTipText("Allows reversion to an earlier save state of your language file.");
        mnuReversion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuReversionActionPerformed(evt);
            }
        });
        mnuTools.add(mnuReversion);

        jMenuBar1.add(mnuTools);

        mnuHelp.setText("Help");

        mnuAbout.setText("Help");
        mnuAbout.setToolTipText("Opens Help File");
        mnuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAboutActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuAbout);

        mnuChkUpdate.setText("About");
        mnuChkUpdate.setToolTipText("About PolyGlot");
        mnuChkUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuChkUpdateActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuChkUpdate);

        mnuExLex.setText("Example Languages");
        mnuExLex.setToolTipText("Languages with exmples to copy from");
        mnuHelp.add(mnuExLex);
        mnuHelp.add(jSeparator3);

        jMenuItem8.setText("Check for Updates");
        jMenuItem8.setToolTipText("Check web for newer versions of PolyGlot");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        mnuHelp.add(jMenuItem8);

        jMenuBar1.add(mnuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlToDoSplit, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlToDoSplit)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLexiconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLexiconActionPerformed
        openLexicon();
    }//GEN-LAST:event_btnLexiconActionPerformed

    private void btnPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPosActionPerformed
        saveAllValues();
        ScrTypes types = ScrTypes.run(core);
        changeScreen(types, types.getWindow(), (PButton) evt.getSource());
    }//GEN-LAST:event_btnPosActionPerformed

    private void mnuNewLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuNewLocalActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        newFile(true);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuNewLocalActionPerformed

    private void mnuSaveLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveLocalActionPerformed
        saveFile();
    }//GEN-LAST:event_mnuSaveLocalActionPerformed

    private void mnuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveAsActionPerformed
        if (saveFileAsDialog()) {
            saveFile();
        }
    }//GEN-LAST:event_mnuSaveAsActionPerformed

    private void mnuOpenLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOpenLocalActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        open();
        setCursor(Cursor.getDefaultCursor());
        updateAllValues(core);
    }//GEN-LAST:event_mnuOpenLocalActionPerformed

    private void mnuPublishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPublishActionPerformed
        printToPdf();
    }//GEN-LAST:event_mnuPublishActionPerformed

    private void mnuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExitActionPerformed
        dispose();
    }//GEN-LAST:event_mnuExitActionPerformed

    private void mnuImportFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuImportFileActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrExcelImport.run(core, this);
        cacheLexicon.refreshWordList(-1);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuImportFileActionPerformed

    private void mnuExportToExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExportToExcelActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        exportToExcel();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuExportToExcelActionPerformed

    private void mnuExportFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExportFontActionPerformed
        exportFont(false);
    }//GEN-LAST:event_mnuExportFontActionPerformed

    private void mnuLangStatsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLangStatsActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (InfoBox.yesNoCancel("Continue Operation?", "The statistics report can"
                + " take a long time to complete, depending on the complexity\n"
                + "of your conlang. Continue?", core.getRootWindow()) == JOptionPane.YES_OPTION) {

            if (!WebInterface.isInternetConnected()) {
                InfoBox.warning("No Net Connection", "No network connection detected. Google generated graphs will not be rendered.", this);
            }

            core.buildLanguageReport();

            // test whether con-font family is installed on computer
            GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String conFontFamily = core.getPropertiesManager().getFontCon().getFamily();
            if (!Arrays.asList(g.getAvailableFontFamilyNames()).contains(conFontFamily)) {
                // prompt user to install font (either Charis or their chosen con-font) if not currently on system
                InfoBox.warning("Font Not Installed",
                        "The font used for your language is not installed on this computer.\n"
                        + "This may result in the statistics page appearing incorrectly.\n"
                        + "Please select a path to save font to, install from this location, "
                        + "and re-run the statistics option.", this);
                if (conFontFamily.equals(PGTUtil.UNICODE_FONT_FAMILY_NAME)) {
                    exportFont(true);
                } else {
                    exportFont(false);
                }
            }
        }
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuLangStatsActionPerformed

    private void mnuIPAChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuIPAChartActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ipaHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuIPAChartActionPerformed

    private void mnuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAboutActionPerformed
        try {
            openHelp();
        } catch (IOException e) {
            InfoBox.error("Help Error", "Unable to open help file: " + e.getLocalizedMessage(), this);
        }
    }//GEN-LAST:event_mnuAboutActionPerformed

    private void mnuChkUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuChkUpdateActionPerformed
        ScrAbout.run(core);
    }//GEN-LAST:event_mnuChkUpdateActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        checkForUpdates(true);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void btnClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClassesActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        saveAllValues();
        ScrWordClasses s = new ScrWordClasses(core);
        changeScreen(s, s.getWindow(), (PButton) evt.getSource());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnClassesActionPerformed

    private void btnGrammarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrammarActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        saveAllValues();
        ScrGrammarGuide s = new ScrGrammarGuide(core);
        changeScreen(s, s.getWindow(), (PButton) evt.getSource());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnGrammarActionPerformed

    private void btnLogosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogosActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        saveAllValues();
        ScrLogoDetails s = new ScrLogoDetails(core);
        changeScreen(s, s.getWindow(), (PButton) evt.getSource());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnLogosActionPerformed

    private void btnPropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPropActionPerformed

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        saveAllValues();
        ScrLangProps s = new ScrLangProps(core);
        changeScreen(s, s.getWindow(), (PButton) evt.getSource());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnPropActionPerformed

    private void mnuLexFamiliesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLexFamiliesActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mnuLexFamilies.setEnabled(false);
        ScrFamilies s = new ScrFamilies(core, this);
        s.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                mnuLexFamilies.setEnabled(true);
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
        s.setVisible(true);
        setCursor(Cursor.getDefaultCursor());

    }//GEN-LAST:event_mnuLexFamiliesActionPerformed

    private void btnOpenLangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenLangActionPerformed
        // default to general Open menu of no prior language selected here
        if (lstRecentOpened.getSelectedIndex() == -1) {
            mnuOpenLocalActionPerformed(evt);
        } else {
            openRecentFromList();
        }
    }//GEN-LAST:event_btnOpenLangActionPerformed

    private void btnOpenManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenManualActionPerformed
        mnuAboutActionPerformed(evt);
    }//GEN-LAST:event_btnOpenManualActionPerformed

    private void btnPhonologyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPhonologyActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        saveAllValues();
        ScrPhonology s = new ScrPhonology(core);
        changeScreen(s, s.getWindow(), (PButton) evt.getSource());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnPhonologyActionPerformed

    private void mnuOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOptionsActionPerformed
        showOptions();
    }//GEN-LAST:event_mnuOptionsActionPerformed

    public void showOptions() {
        ScrOptions s = new ScrOptions(core);
        s.setLocation(getLocation());
        s.setVisible(true);
    }

    private void mnuImportFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuImportFontActionPerformed
        new ScrFontImportDialog(core).setVisible(true);
    }//GEN-LAST:event_mnuImportFontActionPerformed

    private void btnQuizActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuizActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        saveAllValues();
        ScrQuizGenDialog s = new ScrQuizGenDialog(core);
        changeScreen(s, s.getWindow(), (PButton) evt.getSource());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnQuizActionPerformed

    private void mnuReversionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuReversionActionPerformed
        new ScrReversion(core).setVisible(true);
    }//GEN-LAST:event_mnuReversionActionPerformed

    private void mnuCheckLanguageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCheckLanguageActionPerformed
        CheckLanguageErrors.checkCore(core, true);
    }//GEN-LAST:event_mnuCheckLanguageActionPerformed

    private void mnuIpaTranslatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuIpaTranslatorActionPerformed
        new ScrIpaTranslator(core).setVisible(true);
    }//GEN-LAST:event_mnuIpaTranslatorActionPerformed

    private void btnNewLangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewLangActionPerformed
        newFile(true);
    }//GEN-LAST:event_btnNewLangActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        new ScrEvolveLang(core).setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void lstRecentOpenedMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstRecentOpenedMouseClicked
        // only run on double click
        if (evt.getClickCount() == 2) {
            openRecentFromList();
        }
    }//GEN-LAST:event_lstRecentOpenedMouseClicked

    private void mnuExportDictFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExportDictFileActionPerformed
        ExportDictionaryFile();
    }//GEN-LAST:event_mnuExportDictFileActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClasses;
    private javax.swing.JButton btnGrammar;
    private javax.swing.JButton btnLexicon;
    private javax.swing.JButton btnLogos;
    private javax.swing.JButton btnNewLang;
    private javax.swing.JButton btnOpenLang;
    private javax.swing.JButton btnOpenManual;
    private javax.swing.JButton btnPhonology;
    private javax.swing.JButton btnPos;
    private javax.swing.JButton btnProp;
    private javax.swing.JButton btnQuiz;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JList<RecentFile> lstRecentOpened;
    private javax.swing.JMenuItem mnuAbout;
    private javax.swing.JMenuItem mnuCheckLanguage;
    private javax.swing.JMenuItem mnuChkUpdate;
    private javax.swing.JMenu mnuExLex;
    private javax.swing.JMenuItem mnuExit;
    private javax.swing.JMenu mnuExport;
    private javax.swing.JMenuItem mnuExportDictFile;
    private javax.swing.JMenuItem mnuExportFont;
    private javax.swing.JMenuItem mnuExportToExcel;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mnuIPAChart;
    private javax.swing.JMenu mnuImport;
    private javax.swing.JMenuItem mnuImportFile;
    private javax.swing.JMenuItem mnuImportFont;
    private javax.swing.JMenuItem mnuIpaTranslator;
    private javax.swing.JMenuItem mnuLangStats;
    private javax.swing.JMenuItem mnuLexFamilies;
    private javax.swing.JMenuItem mnuNewLocal;
    private javax.swing.JMenuItem mnuOpenLocal;
    private javax.swing.JMenuItem mnuOptions;
    private javax.swing.JMenuItem mnuPublish;
    private javax.swing.JMenu mnuRecents;
    private javax.swing.JMenuItem mnuReversion;
    private javax.swing.JMenuItem mnuSaveAs;
    private javax.swing.JMenuItem mnuSaveLocal;
    private javax.swing.JMenu mnuSwadesh;
    private javax.swing.JMenu mnuTools;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlSideButtons;
    private javax.swing.JPanel pnlStartButtons;
    private javax.swing.JPanel pnlToDo;
    private javax.swing.JSplitPane pnlToDoSplit;
    // End of variables declaration//GEN-END:variables
}
