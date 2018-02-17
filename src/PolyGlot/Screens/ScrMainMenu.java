/*
 * Copyright (c) 2017-2018, Draque Thompson
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
import PolyGlot.DictCore;
import PolyGlot.ExcelExport;
import PolyGlot.IOHandler;
import PolyGlot.Nodes.ConWord;
import PolyGlot.PGTUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
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
import javax.swing.plaf.ToolTipUI;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;

/**
 * Primary window for PolyGlot interface. Main running class that instantiates core and handles other windows/UI.
 * Depends on DictCore for all heavy logical lifting behind the scenes.
 *
 * @author draque.thompson
 */
public class ScrMainMenu extends PFrame implements ApplicationListener {

    private PFrame curWindow = null;
    private ScrLexicon cacheLexicon;
    private final List<String> lastFiles;
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
        cacheLexicon = ScrLexicon.run(core, this);

        UIManager.put("ScrollBarUI", "PolyGlot.CustomControls.PScrollBarUI");
        UIManager.put("SplitPaneUI", "PolyGlot.CustomControls.PSplitPaneUI");
        UIManager.put("OptionPane.background", Color.white);
        UIManager.put("Panel.background", Color.white);
        UIManager.put("ToolTipUI", "PolyGlot.CustomControls.PToolTipUI");
        UIManager.getLookAndFeelDefaults().put("Panel.background", Color.WHITE);

        initComponents();

        try {
            backGround = ImageIO.read(getClass().getResource("/PolyGlot/ImageAssets/PolyGlotBG.png"));
            jLabel1.setFont(IOHandler.getButtonFont().deriveFont(45f));
        } catch (IOException e) {
            InfoBox.error("Resource Error",
                    "Unable to load internal resource: " + e.getLocalizedMessage(),
                    core.getRootWindow());
        }

        newFile(false);
        setOverrideProgramPath(overridePath);
        lastFiles = core.getOptionsManager().getLastFiles();
        populateRecentOpened();
        checkJavaVersion();

        // activates macify for menu integration...
        if (System.getProperty("os.name").startsWith("Mac")) {
            try {
                activateMacify();
            } catch (Exception ex) {
                //ex.printStackTrace();
                // TODO: Consider removing macify entirely
                // Inform user? Don't see a pressing need to...
            }
        }

        ToolTipUI t;
        super.setSize(super.getPreferredSize());
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

        core.getOptionsManager().setScreenPosition(getClass().getName(),
                getLocation());
        core.getOptionsManager().setLastFiles(lastFiles);
        try {
            core.getOptionsManager().saveIni();
        } catch (IOException ex) {
            InfoBox.warning("INI Save Error", "Unable to save settings file on exit.", core.getRootWindow());
        }

        System.exit(0);
    }

    // TODO: Consider removing Macify if it continues giving trouble/no benefit
    // MACIFY RELATED CODE ->    
    private void activateMacify() {
        Application application = new DefaultApplication();
        application.addApplicationListener(this);
        application.addApplicationListener(this);
        application.addPreferencesMenuItem();
        application.setEnabledPreferencesMenu(true);
    }

    @Override
    public void handleAbout(ApplicationEvent event) {
        viewAbout();
        event.setHandled(true);
    }

    @Override
    public void handleOpenApplication(ApplicationEvent event) {
        // Ok, we know our application started
        // Not much to do about that..
    }

    @Override
    public void handleOpenFile(ApplicationEvent event) {
        //openFileInEditor(new File(event.getFilename()));
    }

    @Override
    public void handlePreferences(ApplicationEvent event) {
        //preferencesAction.actionPerformed(null);
    }

    @Override
    public void handlePrintFile(ApplicationEvent event) {
        ScrPrintToPDF.run(core);
    }

    @Override
    public void handleQuit(ApplicationEvent event) {
        dispose();
    }

    @Override
    public void handleReOpenApplication(ApplicationEvent event) {
        setVisible(true);
    }
    // <- MACIFY RELATED CODE

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
                pushRecentFile(curFile);
                populateRecentOpened();
            });
            mnuRecents.add(lastFile);
        }
    }

    /**
     * Pushes a recently opened file (if appropriate) into the recent files list
     *
     * @param file full path of file
     */
    private void pushRecentFile(String file) {
        if (!lastFiles.isEmpty()
                && lastFiles.contains(file)) {
            lastFiles.remove(file);
            lastFiles.add(file);
            return;
        }

        while (lastFiles.size() > PGTUtil.optionsNumLastFiles) {
            lastFiles.remove(0);
        }

        lastFiles.add(file);
    }

    private void setFile(String fileName) {
        // some wrappers communicate empty files like this
        if (fileName.equals(PGTUtil.emptyFile)
                || fileName.isEmpty()) {
            return;
        }

        core = new DictCore();
        core.setRootWindow(this);

        try {
            core.readFile(fileName);
            curFileName = fileName;

            if (curWindow == null) {
                cacheLexicon.updateAllValues(core);
                changeScreen(cacheLexicon, cacheLexicon.getWindow(), null);
            }
        } catch (IOException e) {
            core = new DictCore(); // don't allow partial loads
            InfoBox.error("File Read Error", "Could not read file: " + fileName
                    + "\n\n " + e.getMessage(), core.getRootWindow());
        } catch (IllegalStateException e) {
            InfoBox.warning("File Read Problems", "Problems reading file:\n"
                    + e.getLocalizedMessage(), core.getRootWindow());
        }

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
        if (curFileName.length() == 0) {
            saveFileAs();
        }

        // if it still is blank, the user has hit cancel on the save as dialog
        if (curFileName.length() == 0) {
            return false;
        }

        pushRecentFile(curFileName);
        populateRecentOpened();
        return doWrite(curFileName);
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
            InfoBox.error("Save Error", "Unable to save to file: "
                    + curFileName + "\n\n" + e.getMessage(), core.getRootWindow());
        }

        setCursor(Cursor.getDefaultCursor());

        if (cleanSave) {
            InfoBox.info("Success", "Dictionary saved to: "
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
     * saves file as particular filename
     *
     * @return true if file saved, false otherwise
     */
    private boolean saveFileAs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Dictionary");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PolyGlot Dictionaries", "pgd", "xml");
        chooser.setFileFilter(filter);
        chooser.setApproveButtonText("Save");
        chooser.setCurrentDirectory(core.getPropertiesManager().getCannonicalDirectory());

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
        return true;
    }

    /**
     * Provided for cases where the java is run from an odd source folder (such as under an app file in OSX)
     *
     * @param override directory for base PolyGlot directory
     */
    private void setOverrideProgramPath(String override) {
        core.getPropertiesManager().setOverrideProgramPath(override);
        try {
            core.getOptionsManager().loadIni();
        } catch (Exception e) {
            InfoBox.error("Options Load Error", "Unable to load or create options file:\n"
                    + e.getLocalizedMessage(), core.getRootWindow());
        }
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

        core = new DictCore();
        core.setRootWindow(this);
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
        jPanel2.removeAll();
        this.repaint();

        // resize screen
        if (curWindow != null) {
            // set size before disposing so that it will be properly saved to options
            curWindow.getWindow().setSize(jPanel2.getSize());
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
                this.setSizeSmooth(dim.width + jPanel1.getWidth() + insets.left + insets.right,
                        dim.height + insets.bottom + insets.top,
                        true);
            } catch (InterruptedException e) {
                InfoBox.error("Resize Error",
                        "Unable to run resize animation: " + e.getLocalizedMessage(),
                        core.getRootWindow());
            }
        }

        // set new screen
        GroupLayout layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(layout);
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
            } else if (curFileName.length() != 0) {
                title += " : " + curFileName;
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
        chooser.setCurrentDirectory(core.getPropertiesManager().getCannonicalDirectory());

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
            core = new DictCore();
            core.setRootWindow(this);
            setFile(fileName);
            pushRecentFile(fileName);
            populateRecentOpened();
        }

        genTitle();
    }

    /**
     * checks web for updates to PolyGlot
     *
     * @param verbose Set this to have messages post to user.
     */
    private void checkForUpdates(final boolean verbose) {
        Thread check = new Thread() {
            @Override
            public void run() {
                try {
                    ScrUpdateAlert.run(verbose, core);
                } catch (Exception e) {
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
            ExcelExport.exportExcelDict(fileName, core);
            InfoBox.info("Export Status", "Dictionary exported to " + fileName + ".", core.getRootWindow());
        } catch (Exception e) {
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

        try {
            if (exportCharis) {
                IOHandler.exportCharisFont(fileName);
            } else {
                IOHandler.exportFont(fileName, curFileName);
            }
            InfoBox.info("Export Success", "Font exported to: " + fileName, core.getRootWindow());
        } catch (IOException e) {
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
            scrLexicon.selectWordById(id);
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
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnLexicon = new PButton(core);
        btnPos = new PButton(core);
        btnClasses = new PButton(core);
        btnGrammar = new PButton(core);
        btnLogos = new PButton(core);
        btnProp = new PButton(core);
        btnPhonology = new PButton(core);
        jPanel2 = new javax.swing.JPanel() {
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
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        mnuLangStats = new javax.swing.JMenuItem();
        mnuQuiz = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mnuIPAChart = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        mnuOptions = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuAbout = new javax.swing.JMenuItem();
        mnuChkUpdate = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem8 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PolyGlot Language Construction Toolkit");
        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(4000, 4000));

        jPanel1.setBackground(new java.awt.Color(102, 204, 255));
        jPanel1.setMaximumSize(new java.awt.Dimension(4000, 4000));

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnLexicon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnClasses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnProp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLogos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnGrammar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPhonology, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setMaximumSize(new java.awt.Dimension(4000, 4000));

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(101, 101, 101)
                .addComponent(jButton1)
                .addGap(95, 95, 95)
                .addComponent(jButton2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(97, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addGap(118, 118, 118))
        );

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
        mnuExportFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExportFontActionPerformed(evt);
            }
        });
        mnuTools.add(mnuExportFont);
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

        mnuQuiz.setText("Quiz Generator");
        mnuQuiz.setToolTipText("Generate customized flashcard quizzes to help increase fluency.");
        mnuQuiz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuQuizActionPerformed(evt);
            }
        });
        mnuTools.add(mnuQuiz);
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
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void mnuQuizActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuQuizActionPerformed
        quizHit();
    }//GEN-LAST:event_mnuQuizActionPerformed

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
        ScrOptions s = new ScrOptions(core);
        s.setLocation(getLocation());
        s.setVisible(true);
    }//GEN-LAST:event_mnuOptionsActionPerformed

    /**
     * @param args the command line arguments args[0] = open file path (blank if none) args[1] = working directory of
     * PolyGlot (blank if none)
     */
    public static void main(final String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ScrMainMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String overridePath = args.length > 1 ? args[1] : "";
                String startProblems = "";
                ScrMainMenu s = null;

                // Test for minimum version of Java (8)
                String jVer = System.getProperty("java.version");
                if (jVer.startsWith("1.5") || jVer.startsWith("1.6") || jVer.startsWith("1.7")) {
                    startProblems += "Unable to start PolyGlot without Java 8 or higher.\n";
                }

                try {
                    // Test for JavaFX and inform user that it is not present, they cannot run PolyGlot
                    this.getClass().getClassLoader().loadClass("javafx.embed.swing.JFXPanel");
                } catch (ClassNotFoundException e) {
                    startProblems += "Unable to load Java FX. Download and install to use PolyGlot "
                            + "(JavaFX not included in some builds of Java 8 for Linux).\n";
                }

                if (startProblems.length() == 0) {
                    try {
                        // separated due to serious nature of Thowable vs Exception
                        s = new ScrMainMenu(overridePath);
                        s.checkForUpdates(false);
                        s.setupKeyStrokes();
                        s.setVisible(true);

                        // open file if one is provided via arguments
                        if (args.length > 0) {
                            s.setFile(args[0]);
                            s.openLexicon();
                        }
                    } catch (Exception ex) {
                        startProblems += "Unable to open PolyGlot main frame: \n"
                                + ex.getMessage() + "\n"
                                + "Please contact developer (draquemail@gmail.com) for assistance.";
                    }
                }

                if (startProblems.length() != 0) {
                    InfoBox.error("Unable to start", startProblems, s);
                    if (s != null) {
                        s.dispose();
                    }
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClasses;
    private javax.swing.JButton btnGrammar;
    private javax.swing.JButton btnLexicon;
    private javax.swing.JButton btnLogos;
    private javax.swing.JButton btnPhonology;
    private javax.swing.JButton btnPos;
    private javax.swing.JButton btnProp;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JMenuItem mnuAbout;
    private javax.swing.JMenuItem mnuChkUpdate;
    private javax.swing.JMenuItem mnuExit;
    private javax.swing.JMenuItem mnuExportFont;
    private javax.swing.JMenuItem mnuExportToExcel;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mnuIPAChart;
    private javax.swing.JMenuItem mnuImportFile;
    private javax.swing.JMenuItem mnuLangStats;
    private javax.swing.JMenuItem mnuNewLocal;
    private javax.swing.JMenuItem mnuOpenLocal;
    private javax.swing.JMenuItem mnuOptions;
    private javax.swing.JMenuItem mnuPublish;
    private javax.swing.JMenuItem mnuQuiz;
    private javax.swing.JMenu mnuRecents;
    private javax.swing.JMenuItem mnuSaveAs;
    private javax.swing.JMenuItem mnuSaveLocal;
    private javax.swing.JMenu mnuTools;
    // End of variables declaration//GEN-END:variables

    @Override
    public void updateAllValues(DictCore _core) {
        if (curWindow != null) {
            curWindow.updateAllValues(_core);
        }
    }

    @Override
    public boolean thisOrChildrenFocused() {
        // TODO: Ehhhh...... maybe just remove this functionality altogether with the 2.0 revision
        return false;
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
}
