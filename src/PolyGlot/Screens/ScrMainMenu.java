/*
 * Copyright (c) 2017-2019, Draque Thompson, draquemail@gmail.com
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
package PolyGlot.Screens;

import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PButton;
import PolyGlot.CustomControls.PFrame;
import PolyGlot.CustomControls.PLabel;
import PolyGlot.CustomControls.PToDoTree;
import PolyGlot.CustomControls.PToDoTreeModel;
import PolyGlot.CustomControls.ToDoTreeNode;
import PolyGlot.DictCore;
import PolyGlot.ExcelExport;
import PolyGlot.IOHandler;
import PolyGlot.Nodes.ConWord;
import PolyGlot.PFontHandler;
import PolyGlot.PGTUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontFormatException;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Primary window for PolyGlot interface. Main running class that instantiates core and handles other windows/UI.
 * Depends on DictCore for all heavy logical lifting behind the scenes.
 *
 * @author draque.thompson
 */
public final class ScrMainMenu extends PFrame {

    private PToDoTree toDoTree;
    private PFrame curWindow = null;
    private ScrLexicon cacheLexicon;
    private String curFileName = "";
    private Image backGround;

    /**
     * Creates new form ScrMainMenu
     *
     * @param overridePath Path PolyGlot should treat as home directory (blank if default)
     */
    @SuppressWarnings("LeakingThisInConstructor") // only passing as later reference
    public ScrMainMenu(String overridePath) {
        super();
        core = new DictCore(); // needed for initialization
        core.setRootWindow(this);
        toDoTree = new PToDoTree(core);
        cacheLexicon = ScrLexicon.run(core, this);

        UIManager.put("ScrollBarUI", "PolyGlot.CustomControls.PScrollBarUI");
        UIManager.put("SplitPaneUI", "PolyGlot.CustomControls.PSplitPaneUI");
        UIManager.put("OptionPane.background", Color.white);
        UIManager.put("Panel.background", Color.white);
        UIManager.put("ToolTipUI", "PolyGlot.CustomControls.PToolTipUI");
        UIManager.getLookAndFeelDefaults().put("Panel.background", Color.WHITE);

        initComponents();
        setupEasterEgg();

        try {
            backGround = ImageIO.read(getClass().getResource("/PolyGlot/ImageAssets/PolyGlotBG.png"));
            jLabel1.setFont(PFontHandler.getButtonFont().deriveFont(45f));
        } catch (IOException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Resource Error",
                    "Unable to load internal resource: " + e.getLocalizedMessage(),
                    core.getRootWindow());
        }

        newFile(false);
        core.getPropertiesManager().setOverrideProgramPath(overridePath);
        
        try {
            core.getOptionsManager().loadIni();
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Options Load Error", "Unable to load options file or file corrupted:\n"
                    + e.getLocalizedMessage(), core.getRootWindow());
            IOHandler.deleteIni(core);
        }
        
        setupToDo();
        populateRecentOpened();
        checkJavaVersion();
        super.setSize(super.getPreferredSize());
        setupKeyStrokes();
    }
    
    /**
     * Warns user if they are using a beta version (based on beta warning file)
     */
    public void warnBeta() {
        if (IOHandler.fileExists("BETA_WARNING.txt")) {
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
     * For the purposes of startup with file
     */
    public void openLexicon() {
        cacheLexicon.updateAllValues(core);
        changeScreen(cacheLexicon, cacheLexicon.getWindow(), null);
    }

    @Override
    public void dispose() {
        // only exit if save/cancel test is passed and current window is legal to close
        if (!saveOrCancelTest() || (curWindow != null && !curWindow.canClose())) {
            return;
        }

        if (curWindow != null && !curWindow.isDisposed()) {
            // make certain that all actions necessary for saving information are complete
            curWindow.dispose();
        }

        super.dispose();

        core.getOptionsManager().setScreenPosition(getClass().getName(), getLocation());
        core.getOptionsManager().setToDoBarPosition(pnlToDoSplit.getDividerLocation());
        
        try {
            core.getOptionsManager().saveIni();
        } catch (IOException e) {
            // save error likely due to inability to write to disk, disable logging
            // IOHandler.writeErrorLog(e);
            InfoBox.warning("INI Save Error", "Unable to save settings file on exit.", core.getRootWindow());
        }

        System.exit(0);
    }

    /**
     * Checks to make certain Java is a high enough version. Informs user and quits otherwise.
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
        List<String> lastFiles = core.getOptionsManager().getLastFiles();

        for (int i = lastFiles.size() - 1; i >= 0; i--) {
            final String curFile = lastFiles.get(i);
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
        }
    }

    public void setFile(String fileName) {
        // some wrappers communicate empty files like this
        if (fileName.equals(PGTUtil.emptyFile)
                || fileName.isEmpty()) {
            return;
        }

        core = new DictCore(core);

        try {
            core.readFile(fileName);
            curFileName = fileName;

            if (curWindow == null) {
                cacheLexicon.updateAllValues(core);
                changeScreen(cacheLexicon, cacheLexicon.getWindow(), null);
            }
        } catch (IOException e) {
            IOHandler.writeErrorLog(e);
            core = new DictCore(core); // don't allow partial loads
            InfoBox.error("File Read Error", "Could not read file: " + fileName
                    + "\n\n " + e.getMessage(), core.getRootWindow());
        } catch (IllegalStateException e) {
            IOHandler.writeErrorLog(e);
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
    private boolean saveOrCancelTest() {
        // if there's a current dictionary loaded, prompt user to save before creating new
        if (core != null
                && !core.getWordCollection().getWordNodes().isEmpty()) {
            Integer saveFirst = InfoBox.yesNoCancel("Save First?",
                    "Save current dictionary before performing action?", core.getRootWindow());

            if (saveFirst == JOptionPane.YES_OPTION) {
                boolean saved = saveFile();

                // if the file didn't save (usually due to a last minute cancel) don't continue.
                if (!saved) {
                    return false;
                }
            } else if (saveFirst == JOptionPane.CANCEL_OPTION
                    || saveFirst == JOptionPane.DEFAULT_OPTION) {
                return false;
            }
        }

        return true;
    }

    /**
     * save file, open save as dialog if no file name already
     *
     * @return true if file saved, false otherwise
     */
    public boolean saveFile() {
        if (getCurFileName().length() == 0) {
            saveFileAs();
        }

        // if it still is blank, the user has hit cancel on the save as dialog
        if (getCurFileName().length() == 0) {
            return false;
        }
        
        core.getOptionsManager().pushRecentFile(getCurFileName());
        populateRecentOpened();
        saveAllValues();
        genTitle();
        return doWrite(getCurFileName());
    }

    /**
     * Writes the file by calling the core
     *
     * @param _fileName path to write to
     * @return returns success/failure
     */
    private boolean doWrite(final String _fileName) {
        boolean cleanSave = false;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            core.writeFile(_fileName);
            cleanSave = true;
        } catch (IOException | ParserConfigurationException
                | TransformerException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Save Error", "Unable to save to file: "
                    + getCurFileName() + "\n\n" + e.getMessage(), core.getRootWindow());
        }

        setCursor(Cursor.getDefaultCursor());

        if (cleanSave) {
            InfoBox.info("Success", "Dictionary saved to: "
                    + getCurFileName() + ".", core.getRootWindow());
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
     * saves file as particular filename
     *
     * @return true if file saved, false otherwise
     */
    private boolean saveFileAs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Dictionary");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PolyGlot Dictionaries", "pgd");
        chooser.setFileFilter(filter);
        chooser.setApproveButtonText("Save");
        if (curFileName.isEmpty()) {
            chooser.setCurrentDirectory(core.getPropertiesManager().getCannonicalDirectory());
        } else {
            chooser.setCurrentDirectory(IOHandler.getDirectoryFromPath(curFileName));
            chooser.setSelectedFile(IOHandler.getFileFromPath(curFileName));
        }

        String fileName;

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return false;
        }

        // if user has not provided an extension, add one
        if (!fileName.contains(".pgd")) {
            fileName += ".pgd";
        }

        if (IOHandler.fileExists(fileName)) {
            Integer overWrite = InfoBox.yesNoCancel("Overwrite Dialog",
                    "Overwrite existing file? " + fileName, core.getRootWindow());

            if (overWrite == JOptionPane.NO_OPTION) {
                return saveFileAs();
            } else if (overWrite == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }

        curFileName = fileName;
        // TODO: refactor to single exit point
        return true;
    }

    /**
     * Creates new, blank language file
     *
     * @param performTest whether the UI ask for confirmation
     */
    final public void newFile(boolean performTest) {
        if (performTest && !saveOrCancelTest()) {
            return;
        }

        core = new DictCore(core);
        updateAllValues(core);
        curFileName = "";

        genTitle();
        updateAllValues(core);

        if (curWindow == null && performTest) {
            cacheLexicon.updateAllValues(core);
            changeScreen(cacheLexicon, cacheLexicon.getWindow(), null);
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
            try {
                this.setSizeSmooth(dim.width + pnlSideButtons.getWidth() + insets.left + insets.right,
                        dim.height + insets.bottom + insets.top,
                        true);
            } catch (InterruptedException e) {
                IOHandler.writeErrorLog(e);
                InfoBox.error("Resize Error",
                        "Unable to run resize animation: " + e.getLocalizedMessage(),
                        core.getRootWindow());
            }
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

    public void genTitle() {
        String title = "PolyGlot";

        if (curWindow != null && curWindow.getTitle().length() != 0) {
            title += "-" + curWindow.getTitle();
            String langName = core.getPropertiesManager().getLangName();

            if (langName.length() != 0) {
                title += " : " + langName;
            } else if (getCurFileName().length() != 0) {
                title += " : " + getCurFileName();
            }
        }

        setTitle(title);
    }

    private void viewAbout() {
        ScrAbout.run(core);
    }

    /**
     * opens dictionary file
     */
    public void open() {
        // only open if save/cancel test is passed
        if (!saveOrCancelTest()) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Dictionary");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PolyGlot Dictionaries", "pgd");
        chooser.setFileFilter(filter);
        String fileName;
        if (curFileName.isEmpty()) {
            chooser.setCurrentDirectory(core.getPropertiesManager().getCannonicalDirectory());
        } else {
            chooser.setCurrentDirectory(IOHandler.getDirectoryFromPath(curFileName));
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
            core = new DictCore(core);
            setFile(fileName);
            core.getOptionsManager().pushRecentFile(fileName);
            populateRecentOpened();
        }

        genTitle();
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
                    IOHandler.writeErrorLog(e);
                    if (verbose) {
                        PolyGlot.CustomControls.InfoBox.error("Update Problem",
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
        chooser.setDialogTitle("Export Dictionary to Excel");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xls");
        chooser.setFileFilter(filter);
        chooser.setApproveButtonText("Save");
        chooser.setCurrentDirectory(core.getPropertiesManager().getCannonicalDirectory());

        String fileName;

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }

        if (!fileName.contains(".xls")) {
            fileName += ".xls";
        }

        try {
            ExcelExport.exportExcelDict(fileName, core, 
                    InfoBox.actionConfirmation("Excel Export", 
                            "Export all declensions? (Separates parts of speech into individual tabs)", 
                            core.getRootWindow()));
            
            InfoBox.info("Export Status", "Dictionary exported to " + fileName + ".", core.getRootWindow());
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.info("Export Problem", e.getLocalizedMessage(), core.getRootWindow());
        }
    }

    /**
     * Prompts user for a location and exports font within PGD to given path
     *
     * @param exportCharis set to true to export charis, false to export con font
     */
    public void exportFont(boolean exportCharis) {
        JFileChooser chooser = new JFileChooser();
        String fileName;
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Font Files", "ttf");

        chooser.setDialogTitle("Export Font");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(new File("."));
        chooser.setApproveButtonText("Save");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }

        if (!fileName.contains(".")) {
            fileName += ".ttf";
        }
        
        if (IOHandler.fileExists(fileName) 
                && !InfoBox.actionConfirmation("Overwrite Confirmation", "File will be overwritten. Continue?", this)) {
            return;
        }

        try {
            if (exportCharis) {
                IOHandler.exportCharisFont(fileName);
            } else {
                IOHandler.exportFont(fileName, getCurFileName());
            }
            InfoBox.info("Export Success", "Font exported to: " + fileName, core.getRootWindow());
        } catch (IOException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Export Error", "Unable to export font: " + e.getMessage(), core.getRootWindow());
        }
    }

    private void ipaHit() {
        PFrame ipa = new ScrIPARefChart(core);
        ipa.setVisible(true);
        ipa.toFront();
    }

    private void openHelp() {
        URI uri;
        try {
            String OS = System.getProperty("os.name");
            String overridePath = core.getPropertiesManager().getOverrideProgramPath();
            if (OS.startsWith("Windows")) {
                String relLocation = new File(".").getAbsolutePath();
                relLocation = relLocation.substring(0, relLocation.length() - 1);
                relLocation = "file:///" + relLocation + "readme.html";
                relLocation = relLocation.replaceAll(" ", "%20");
                relLocation = relLocation.replaceAll("\\\\", "/");
                uri = new URI(relLocation);
                uri.normalize();
                java.awt.Desktop.getDesktop().browse(uri);
            } else if (OS.startsWith("Mac")) {
                String relLocation;
                if (overridePath.length() == 0) {
                    relLocation = new File(".").getAbsolutePath();
                    relLocation = relLocation.substring(0, relLocation.length() - 1);
                    relLocation = "file://" + relLocation + "readme.html";
                } else {
                    relLocation = core.getPropertiesManager().getOverrideProgramPath();
                    relLocation = "file://" + relLocation + "/Contents/Resources/readme.html";
                }
                relLocation = relLocation.replaceAll(" ", "%20");
                uri = new URI(relLocation);
                uri.normalize();
                java.awt.Desktop.getDesktop().browse(uri);
            } else {
                // TODO: Implement this for Linux
                InfoBox.error("Help", "This is not yet implemented for OS: " + OS
                        + ". Please open readme.html in the application directory", core.getRootWindow());
            }
        } catch (URISyntaxException | IOException e) {
            // no need to log this.
            // IOHandler.writeErrorLog(e);
            InfoBox.error("Missing File", "Unable to open readme.html.", core.getRootWindow());
        }
    }

    private void quizHit() {
        ScrQuizGenDialog.run(core);
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
     * Retrieves currently selected word (if any) from ScrLexicon
     *
     * @return current word selected in scrLexicon, null otherwise (or if lexicon is not visible)
     */
    public ConWord getCurrentWord() {
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
        toDoTree = new PToDoTree(core);
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
        ((DefaultTreeModel)toDoTree.getModel()).nodeStructureChanged((ToDoTreeNode)toDoTree.getModel().getRoot());
    }
    
    private void setupEasterEgg() {
        class EnterKeyListener implements KeyEventPostProcessor {
            String lastChars = "------------------------------";
            @Override
            public boolean postProcessKeyEvent(KeyEvent e) {
                if(e != null && e.getKeyCode() == 0) {
                    lastChars = lastChars.substring(1, lastChars.length());
                    lastChars = lastChars + e.getKeyChar();
                    
                    if(lastChars.toLowerCase().endsWith("what did you see last tuesday")) {
                        InfoBox.info("Coded Response", "A pink elephant.", null);
                    } else if (lastChars.toLowerCase().endsWith("this is the forest primeval")) {
                        InfoBox.info("Bearded with moss", "The murmuring pines and the hemlocks.", null);
                    } else if (lastChars.toLowerCase().endsWith("it can't outlast you")) {
                        InfoBox.info("Just human...", "Yes it can. You're not a kukun.", null);
                    } else if (lastChars.toLowerCase().endsWith("who's draque") 
                            || lastChars.toLowerCase().endsWith("who is draque")) {
                        ScrEasterEgg.run(core.getRootWindow());
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
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem2 = new javax.swing.JMenuItem();
        pnlToDoSplit = new javax.swing.JSplitPane();
        pnlToDo = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        pnlSideButtons = new javax.swing.JPanel();
        btnLexicon = new PButton(core);
        btnPos = new PButton(core);
        btnClasses = new PButton(core);
        btnGrammar = new PButton(core);
        btnLogos = new PButton(core);
        btnProp = new PButton(core);
        btnPhonology = new PButton(core);
        btnQuiz = new PButton(core);
        pnlMain = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backGround != null && curWindow == null) {
                    g.drawImage(backGround, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        jButton1 = new PButton(core);
        jButton2 = new PButton(core);
        jLabel1 = new PLabel("", core);
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
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
        mnuImportFile = new javax.swing.JMenuItem();
        mnuExportToExcel = new javax.swing.JMenuItem();
        mnuExportFont = new javax.swing.JMenuItem();
        mnuImportFont = new javax.swing.JMenuItem();
        mnuCheckLexicon = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        mnuLangStats = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mnuIPAChart = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        mnuOptions = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        mnuRevertion = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuAbout = new javax.swing.JMenuItem();
        mnuChkUpdate = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem8 = new javax.swing.JMenuItem();

        jMenuItem2.setText("jMenuItem2");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PolyGlot Language Construction Toolkit");
        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(4000, 4000));

        pnlToDoSplit.setBackground(new java.awt.Color(255, 255, 255));
        pnlToDoSplit.setDividerLocation(675);
        pnlToDoSplit.setDividerSize(10);

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
                .addContainerGap(12, Short.MAX_VALUE))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlMain.setBackground(new java.awt.Color(255, 255, 255));
        pnlMain.setMaximumSize(new java.awt.Dimension(4000, 4000));

        jButton1.setText("OPEN LANGUAGE");
        jButton1.setToolTipText("Open an existing language");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("OPEN MANUAL");
        jButton2.setToolTipText("Open the PolyGlot manual");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Welcome to PolyGlot");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("From here, you can open an existing language file, open the PolyGlot manual,");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("or simply begin work on the blank file currently loaded.");

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(101, 101, 101)
                .addComponent(jButton1)
                .addGap(95, 95, 95)
                .addComponent(jButton2)
                .addContainerGap(10, Short.MAX_VALUE))
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addContainerGap(129, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addGap(118, 118, 118))
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

        jMenu1.setText("File");

        mnuNewLocal.setText("New");
        mnuNewLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuNewLocalActionPerformed(evt);
            }
        });
        jMenu1.add(mnuNewLocal);

        mnuSaveLocal.setText("Save");
        mnuSaveLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveLocalActionPerformed(evt);
            }
        });
        jMenu1.add(mnuSaveLocal);

        mnuSaveAs.setText("Save As");
        mnuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveAsActionPerformed(evt);
            }
        });
        jMenu1.add(mnuSaveAs);

        mnuOpenLocal.setText("Open");
        mnuOpenLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenLocalActionPerformed(evt);
            }
        });
        jMenu1.add(mnuOpenLocal);

        mnuRecents.setText("Recent");
        jMenu1.add(mnuRecents);
        jMenu1.add(jSeparator5);

        mnuPublish.setText("Publish to PDF");
        mnuPublish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPublishActionPerformed(evt);
            }
        });
        jMenu1.add(mnuPublish);
        jMenu1.add(jSeparator2);

        mnuExit.setText("Exit");
        mnuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExitActionPerformed(evt);
            }
        });
        jMenu1.add(mnuExit);

        jMenuBar1.add(jMenu1);

        mnuTools.setText("Tools");

        mnuImportFile.setText("Import from File");
        mnuImportFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuImportFileActionPerformed(evt);
            }
        });
        mnuTools.add(mnuImportFile);

        mnuExportToExcel.setText("Export to Excel");
        mnuExportToExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExportToExcelActionPerformed(evt);
            }
        });
        mnuTools.add(mnuExportToExcel);

        mnuExportFont.setText("Export Font");
        mnuExportFont.setToolTipText("Export font to file");
        mnuExportFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExportFontActionPerformed(evt);
            }
        });
        mnuTools.add(mnuExportFont);

        mnuImportFont.setText("Import Font");
        mnuImportFont.setToolTipText("Import font from file");
        mnuImportFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuImportFontActionPerformed(evt);
            }
        });
        mnuTools.add(mnuImportFont);

        mnuCheckLexicon.setText("Check Lexicon");
        mnuCheckLexicon.setToolTipText("Checks lexicon for problems and inconsistencies.");
        mnuCheckLexicon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCheckLexiconActionPerformed(evt);
            }
        });
        mnuTools.add(mnuCheckLexicon);
        mnuTools.add(jSeparator1);

        jMenuItem1.setText("Lexical Families");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        mnuTools.add(jMenuItem1);

        mnuLangStats.setText("Language Statistics");
        mnuLangStats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLangStatsActionPerformed(evt);
            }
        });
        mnuTools.add(mnuLangStats);
        mnuTools.add(jSeparator4);

        mnuIPAChart.setText("Interactive IPA Chart");
        mnuIPAChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuIPAChartActionPerformed(evt);
            }
        });
        mnuTools.add(mnuIPAChart);
        mnuTools.add(jSeparator6);

        mnuOptions.setText("Options");
        mnuOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOptionsActionPerformed(evt);
            }
        });
        mnuTools.add(mnuOptions);
        mnuTools.add(jSeparator7);

        mnuRevertion.setText("Revert Language");
        mnuRevertion.setToolTipText("Allows reversion to an earlier save state of your language file.");
        mnuRevertion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRevertionActionPerformed(evt);
            }
        });
        mnuTools.add(mnuRevertion);

        jMenuBar1.add(mnuTools);

        mnuHelp.setText("Help");

        mnuAbout.setText("Help");
        mnuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAboutActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuAbout);

        mnuChkUpdate.setText("About");
        mnuChkUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuChkUpdateActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuChkUpdate);
        mnuHelp.add(jSeparator3);

        jMenuItem8.setText("Check for Updates");
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
        cacheLexicon.updateAllValues(core);
        changeScreen(cacheLexicon, cacheLexicon.getWindow(), (PButton) evt.getSource());
    }//GEN-LAST:event_btnLexiconActionPerformed

    private void btnPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPosActionPerformed
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
        if (saveFileAs()) {
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
        ScrPrintToPDF.run(core);
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
            core.buildLanguageReport();

            // test whether con-font family is installed on computer
            GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String conFontFamily = core.getPropertiesManager().getFontCon().getFamily();
            if (!Arrays.asList(g.getAvailableFontFamilyNames()).contains(conFontFamily)) {
                // prompt user to install font (either Charis or their chosen con-font) if not currently on system
                InfoBox.warning("Font Not Installed",
                        "The font used for your language is not installe on this computer.\n"
                        + "This may result in the statistics page appearing incorrectly.\n"
                        + "Please select a path to save font to, install from this location, "
                        + "and re-run the statistics option.", this);
                if (conFontFamily.equals(PGTUtil.UnicodeFontFamilyName)) {
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
        openHelp();
    }//GEN-LAST:event_mnuAboutActionPerformed

    private void mnuChkUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuChkUpdateActionPerformed
        ScrAbout.run(core);
    }//GEN-LAST:event_mnuChkUpdateActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        checkForUpdates(true);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void btnClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClassesActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrWordClasses s = new ScrWordClasses(core);
        changeScreen(s, s.getWindow(), (PButton) evt.getSource());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnClassesActionPerformed

    private void btnGrammarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrammarActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrGrammarGuide s = new ScrGrammarGuide(core);
        changeScreen(s, s.getWindow(), (PButton) evt.getSource());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnGrammarActionPerformed

    private void btnLogosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogosActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        ScrLogoDetails s = new ScrLogoDetails(core);
        changeScreen(s, s.getWindow(), (PButton) evt.getSource());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnLogosActionPerformed

    private void btnPropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPropActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrLangProps s = new ScrLangProps(core);
        changeScreen(s, s.getWindow(), (PButton) evt.getSource());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnPropActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrFamilies s = new ScrFamilies(core, this);
        s.setVisible(true);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        mnuOpenLocalActionPerformed(evt);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        mnuAboutActionPerformed(evt);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnPhonologyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPhonologyActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
        JFileChooser chooser = new JFileChooser();
        String fileName;
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Font Files", "ttf", "otf", "ttc", "dfont");
        
        chooser.setDialogTitle("Import Font");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(new File("."));
        chooser.setApproveButtonText("Open");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        
        Double fontSize = InfoBox.doubleInputDialog("Font Size", "Enter a numeric value for font size.", this);
        
        if (fontSize == null) {
            return;
        }

        try {
            core.getPropertiesManager().setFontFromFile(fileName);
            core.getPropertiesManager().setFontSize(fontSize.intValue());
        } catch (IOException e) {
            InfoBox.error("IO Error", "Unable to open " + fileName + " due to: " + e.getLocalizedMessage(), this);
        } catch (FontFormatException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Font Format Error", "Unable to read " + fileName + " due to: " 
                    + e.getLocalizedMessage(), this);
        }
    }//GEN-LAST:event_mnuImportFontActionPerformed

    private void btnQuizActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuizActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrQuizGenDialog s = new ScrQuizGenDialog(core);
        changeScreen(s, s.getWindow(), (PButton) evt.getSource());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnQuizActionPerformed

    private void mnuRevertionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRevertionActionPerformed
        new ScrReversion(core).setVisible(true);
    }//GEN-LAST:event_mnuRevertionActionPerformed

    private void mnuCheckLexiconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCheckLexiconActionPerformed
        core.getWordCollection().checkLexicon(true);
    }//GEN-LAST:event_mnuCheckLexiconActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClasses;
    private javax.swing.JButton btnGrammar;
    private javax.swing.JButton btnLexicon;
    private javax.swing.JButton btnLogos;
    private javax.swing.JButton btnPhonology;
    private javax.swing.JButton btnPos;
    private javax.swing.JButton btnProp;
    private javax.swing.JButton btnQuiz;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JMenuItem mnuAbout;
    private javax.swing.JMenuItem mnuCheckLexicon;
    private javax.swing.JMenuItem mnuChkUpdate;
    private javax.swing.JMenuItem mnuExit;
    private javax.swing.JMenuItem mnuExportFont;
    private javax.swing.JMenuItem mnuExportToExcel;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mnuIPAChart;
    private javax.swing.JMenuItem mnuImportFile;
    private javax.swing.JMenuItem mnuImportFont;
    private javax.swing.JMenuItem mnuLangStats;
    private javax.swing.JMenuItem mnuNewLocal;
    private javax.swing.JMenuItem mnuOpenLocal;
    private javax.swing.JMenuItem mnuOptions;
    private javax.swing.JMenuItem mnuPublish;
    private javax.swing.JMenu mnuRecents;
    private javax.swing.JMenuItem mnuRevertion;
    private javax.swing.JMenuItem mnuSaveAs;
    private javax.swing.JMenuItem mnuSaveLocal;
    private javax.swing.JMenu mnuTools;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlSideButtons;
    private javax.swing.JPanel pnlToDo;
    private javax.swing.JSplitPane pnlToDoSplit;
    // End of variables declaration//GEN-END:variables

    @Override
    public void updateAllValues(DictCore _core) {
        if (curWindow != null) {
            curWindow.updateAllValues(_core);
        }
        core = _core;
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
     * For now, always returns true... shouldn't ever be any upstream window, regardless.
     *
     * @return
     */
    @Override
    public boolean canClose() {
        return true;
    }
    
    public String getCurFileName() {
        return curFileName;
    }
}
