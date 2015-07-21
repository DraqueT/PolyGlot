/*
 * Copyright (c) 2015, draque
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

//import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PFrame;
import PolyGlot.DictCore;
import PolyGlot.ExcelExport;
import PolyGlot.IOHandler;
import PolyGlot.Nodes.ConWord;
import PolyGlot.PGTUtil;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;

/**
 *
 * @author draque
 */
public class ScrDictMenu extends PFrame implements ApplicationListener {
// implementation of ApplicationListener is part of macify

    private String curFileName = "";
    private ScrLexicon scrLexicon;
    private ScrGrammarGuide scrGrammar;
    private ScrLogoDetails scrLogos;
    private ScrThesaurus scrThes;
    private boolean cleanSave = true;
    private boolean holdFront = false;
    private final List<String> lastFiles;

    /**
     * Creates new form ScrDictMenu
     *
     * @param overridePath Path PolyGlot should treat as home directory (blank
     * if default)
     */
    public ScrDictMenu(String overridePath) {
        initComponents();
        newFile(true);
        setOverrideProgramPath(overridePath);
        lastFiles = core.getOptionsManager().getLastFiles();
        populateRecentOpened();
        checkJavaVersion();

        // activates macify for menu integration...
        if (System.getProperty("os.name").startsWith("Mac")) {
            activateMacify();
        }
    }

    @Override
    public boolean thisOrChildrenFocused() {
        boolean ret = this.isFocusOwner() || holdFront;
        ret = ret || (scrLexicon != null && scrLexicon.thisOrChildrenFocused());
        ret = ret || (scrGrammar != null && scrGrammar.thisOrChildrenFocused());
        ret = ret || (scrLogos != null && scrLogos.thisOrChildrenFocused());
        ret = ret || (scrThes != null && scrThes.thisOrChildrenFocused());
        return ret;
    }

    @Override
    public void dispose() {
        // only exit if save/cancel test is passed
        if (!saveOrCancelTest()) {
            return;
        }

        try {
            core.getOptionsManager().setLastFiles(lastFiles);
            core.getOptionsManager().saveIni();
        } catch (IOException e) {
            localError("Ini Save Error", "Unable to save PolyGlot.ini:\n"
                    + e.getLocalizedMessage());
        }
        super.dispose();
        System.exit(0);
    }

    @Override
    public void setupAccelerators() {
        String OS = System.getProperty("os.name");
        if (OS.startsWith("Mac")) {
            mnuSaveLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuNewLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuOpenLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
        } else {
            // I'm pretty sure all other OSes just use CTRL+ to do stuff
            mnuSaveLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuNewLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuOpenLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
        }
    }

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
        localInfo("Printing", "PolyGlot does not currently support printing.");
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
     * Populates recently opened files menu
     */
    private void populateRecentOpened() {
        mnuRecents.removeAll();

        for (final String curFile : lastFiles) {
            Path p = Paths.get(curFile);
            String fileName = p.getFileName().toString();
            JMenuItem lastFile = new JMenuItem();
            lastFile.setText(fileName);
            lastFile.setToolTipText(curFile);
            lastFile.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setFile(curFile);
                }
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
                && lastFiles.get(lastFiles.size() - 1).equals(file)) {
            return;
        }

        while (lastFiles.size() > PGTUtil.optionsNumLastFiles) {
            lastFiles.remove(0);
        }

        lastFiles.add(file);
    }

    /**
     * Used by saving worker to communicate whether files saved were successful
     *
     * @param _cleanSave whether the save was a success
     */
    public void setCleanSave(boolean _cleanSave) {
        cleanSave = _cleanSave;
    }

    /**
     * Creates totally new file
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
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PolyGlot Dictionaries", "pgd", "xml");
        chooser.setFileFilter(filter);
        String fileName;
        chooser.setCurrentDirectory(new File("."));

        holdFront = true;
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        holdFront = false;

        core = new DictCore();
        core.setRootWindow(this);
        setFile(fileName);
        pushRecentFile(fileName);
        populateRecentOpened();
    }

    /**
     * Gives user option to save file, returns continue/don't continue
     *
     * @return true to signal continue, false to signal stop
     */
    private boolean saveOrCancelTest() {
        // if there's a current dictionary loaded, prompt user to save before creating new
        if (core != null
                && core.getWordCollection().getNodeIterator().hasNext()) {
            Integer saveFirst = localYesNoCancel("Save First?",
                    "Save current dictionary before performing action?");

            if (saveFirst == JOptionPane.YES_OPTION) {
                boolean saved = saveFile();

                // if the file didn't save (usually due to a last minute cancel) don't continue.
                if (!saved) {
                    return false;
                }
            } else if (saveFirst == JOptionPane.CANCEL_OPTION) {
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
        if (curFileName.equals("")) {
            saveFileAs();
        }

        // if it still is blank, the user has hit cancel on the save as dialog
        if (curFileName.equals("")) {
            return false;
        }

        pushRecentFile(curFileName);
        populateRecentOpened();
        return doWrite(curFileName);
    }

    /**
     * sends the write command to the core in a new thread
     *
     * @param _fileName path to write to
     * @return returns success
     */
    private boolean doWrite(final String _fileName) {
        final ScrDictMenu parent = this;
        final CountDownLatch latch = new CountDownLatch(1);
        boolean ret;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final SwingWorker worker = new SwingWorker() {
            //Runs on the event-dispatching thread.
            @Override
            protected Object doInBackground() throws Exception {
                try {
                    core.writeFile(_fileName);
                } catch (IOException | ParserConfigurationException |
                        TransformerException e) {
                    parent.setCleanSave(false);
                    localError("Save Error", "Unable to save to file: "
                            + curFileName + "\n\n" + e.getMessage());
                }

                latch.countDown();
                return null;
            }
        };

        worker.execute();
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            localError("Save Error", "Save attempt timed out.");
        }
        setCursor(Cursor.getDefaultCursor());

        if (cleanSave) {
            localInfo("Success", "Dictionary saved to: "
                    + curFileName + ".");
            ret = true;
        } else {
            ret = false;
        }
        cleanSave = true;
        return ret;
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
        chooser.setCurrentDirectory(new File("."));

        String fileName;
 
        holdFront = true;
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return false;
        }
        holdFront = false;

        // if user has not provided an extension, add one
        if (!fileName.contains(".pgd")) {
            fileName += ".pgd";
        }

        File f = new File(fileName);

        if (f.exists()) {
            Integer overWrite = localYesNoCancel("Overwrite Dialog",
                    "Overwrite existing file? " + fileName);

            if (overWrite == JOptionPane.NO_OPTION) {
                saveFileAs();
            } else if (overWrite == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }

        curFileName = fileName;
        return true;
    }

    @Override
    public void updateAllValues(DictCore _core) {
        core = _core;
        String title = "PolyGlot " + core.getVersion();
        String langName = core.getPropertiesManager().getLangName().trim();
        if (!langName.isEmpty()) {
            title += (" : " + langName);
        }

        if (scrLexicon != null
                && !scrLexicon.isDisposed()) {
            scrLexicon.updateAllValues(_core);
        }
        if (scrGrammar != null
                && !scrGrammar.isDisposed()) {
            scrGrammar.updateAllValues(_core);
        }
        if (scrLogos != null
                && !scrLogos.isDisposed()) {
            scrLogos.updateAllValues(_core);
        }
        if (scrThes != null
                && !scrThes.isDisposed()) {
            scrThes.updateAllValues(_core);
        }

        this.setTitle(title);
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        // no bindings necessary for this window
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
            localError("Please Upgrade Java", "Java " + javaVersion
                    + " must be upgraded to run PolyGlot. Version 1.7 or higher is required.\n\n"
                    + "Please upgrade at https://java.com/en/download/.");
            System.exit(0);
        }
    }

    private void setFile(String fileName) {
        // some wrappers communicate emty files like this
        if (fileName.equals(PGTUtil.emptyFile)
                || fileName.isEmpty()) {
            return;
        }

        core = new DictCore();
        core.setRootWindow(this);
        updateAllValues(core);

        try {
            core.readFile(fileName);
            curFileName = fileName;
        } catch (Exception e) {
            core = new DictCore(); // don't allow partial loads
            localError("File Read Error", "Could not read file: " + fileName
                    + "\n\n " + e.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * checks web for updates to PolyGlot
     *
     * @param verbose Set this to have messages post to user.
     */
    private void checkForUpdates(final boolean verbose) {
        final Window parent = this;

        Thread check = new Thread() {
            @Override
            public void run() {
                try {
                    ScrUpdateAlert.run(verbose, core);
                } catch (Exception e) {
                    if (verbose) {
                        PolyGlot.CustomControls.InfoBox.error("Update Problem",
                                "Unable to check for update:\n"
                                + e.getLocalizedMessage(), parent);
                    }
                }
            }
        };

        check.start();
    }

    private void bindButtonToWindow(Window w, final JToggleButton b) {
        w.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        b.setSelected(false);
                    }
                };
                SwingUtilities.invokeLater(runnable);

            }
        });
    }

    /**
     * Provided for cases where the java is run from an odd source folder (such
     * as under an app file in OSX)
     *
     * @param override directory for base PolyGlot directory
     */
    private void setOverrideProgramPath(String override) {
        core.getPropertiesManager().setOverrideProgramPath(override);
        try {
            core.getOptionsManager().loadIni();
        } catch (Exception e) {
            localError("Options Load Error", "Unable to load or create options file:\n"
                    + e.getLocalizedMessage());
        }
    }

    /**
     * Retrieves currently selected word (if any) from ScrLexicon
     *
     * @return current word selected in scrLexicon, null otherwise (or if
     * lexicon is not visible)
     */
    public ConWord getCurrentWord() {
        ConWord ret;

        if (scrLexicon == null
                || !scrLexicon.isVisible()
                || scrLexicon.isDisposed()) {
            ret = null;
        } else {
            ret = scrLexicon.getCurrentWord();
        }

        return ret;
    }

    /**
     * Sets selection on lexicon by word id
     *
     * @param id
     */
    public void selectWordById(int id) {
        scrLexicon.selectWordById(id);
    }

    @Override
    protected void setupKeyStrokes() {
        addBindingsToPanelComponents(this.getRootPane());
        super.setupKeyStrokes();
    }

    private void viewAbout() {
        ScrAbout.run(core);
    }

    private void lexHit() {
        if (btnLexicon.isSelected()) {
            try {
                if (scrLexicon == null
                        || scrLexicon.isDisposed()) {
                    scrLexicon = ScrLexicon.run(core);
                    bindButtonToWindow(scrLexicon, btnLexicon);
                }

                scrLexicon.setVisible(true);
            } catch (Exception e) {
                localError("Open Window Error", "Error Opening Lexicon: "
                        + e.getLocalizedMessage());
            }
        } else {
            try {
                scrLexicon.setVisible(false);
            } catch (Exception e) {
                localError("Close Window Error", "Error Closing Lexicon: "
                        + e.getLocalizedMessage());
            }
        }
    }

    public void quickEntryHit() {
        ScrQuickWordEntry s = scrLexicon.openQuickEntry();
        bindButtonToWindow(s, btnQuickEntry);
        s.setVisible(true);
    }

    public void grammarHit() {
        if (btnGrammar.isSelected()) {
            try {
                if (scrGrammar == null
                        || scrGrammar.isDisposed()) {
                    scrGrammar = ScrGrammarGuide.run(core);
                    bindButtonToWindow(scrGrammar, btnGrammar);
                }

                scrGrammar.setVisible(true);
            } catch (Exception e) {
                localError("Open Window Error", "Error Opening Grammar Guide: "
                        + e.getLocalizedMessage());
            }
        } else {
            try {
                scrGrammar.setVisible(false);
            } catch (Exception e) {
                localError("Close Window Error", "Error Closing Grammar Guide: "
                        + e.getLocalizedMessage());
            }
        }
    }

    private void logoHit() {
        if (btnLogos.isSelected()) {
            try {
                if (scrLogos == null
                        || scrLogos.isDisposed()) {
                    scrLogos = ScrLogoDetails.run(core);
                    bindButtonToWindow(scrLogos, btnLogos);
                }

                scrLogos.setVisible(true);
            } catch (Exception e) {
                localError("Open Window Error", "Error Opening Logograph Guide: "
                        + e.getLocalizedMessage());
            }
        } else {
            try {
                scrLogos.setVisible(false);
            } catch (Exception e) {
                localError("Close Window Error", "Error Closing Grammar Guide: "
                        + e.getLocalizedMessage());
            }
        }
    }

    private void thesHit() {
        if (btnThes.isSelected()) {
            try {
                if (scrThes == null
                        || scrThes.isDisposed()) {
                    scrThes = ScrThesaurus.run(core, this);
                    bindButtonToWindow(scrThes, btnThes);
                }

                scrThes.setVisible(true);
            } catch (Exception e) {
                localError("Open Window Error", "Error Opening Logograph Guide: "
                        + e.getLocalizedMessage());
            }
        } else {
            try {
                scrThes.setVisible(false);
            } catch (Exception e) {
                localInfo("Close Window Error", "Error Closing Grammar Guide: "
                        + e.getLocalizedMessage());
            }
        }
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
        chooser.setCurrentDirectory(new File("."));

        String fileName;

        holdFront = true;
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        holdFront = false;

        if (!fileName.contains(".xls")) {
            fileName += ".xls";
        }

        try {
            ExcelExport.exportExcelDict(fileName, core);
            localInfo("Export Status", "Dictionary exported to " + fileName + ".");
        } catch (Exception e) {
            localError("Export Problem", e.getLocalizedMessage());
        }
    }

    /**
     * Prompts user for a location and exports font within PGD to given path
     */
    public void exportFont() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Font");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Font Files", "ttf");
        chooser.setFileFilter(filter);
        String fileName;
        chooser.setCurrentDirectory(new File("."));
        chooser.setApproveButtonText("Save");

        holdFront = true;
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        holdFront = false;

        try {
            IOHandler.exportFont(fileName, curFileName);
        } catch (IOException e) {
            localError("Export Error", "Unable to export font: " + e.getMessage());
        }
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
                if (overridePath.equals("")) {
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
                localError("Help", "This is not yet implemented for OS: " + OS
                        + ". Please open readme.html in the application directory");
            }
        } catch (URISyntaxException | IOException e) {
            localError("Missing File", "Unable to open readme.html.");
        }
    }
    
    
    /**
     * Wrapped locally to ensure front position of menu not disturbed
     * @param infoHead title text
     * @param infoText message text
     */
    private void localInfo(String infoHead, String infoText) {
        holdFront = true;
        PolyGlot.CustomControls.InfoBox.info(infoHead, infoText, null);
        holdFront = false;
    }
    
    /**
     * Wrapped locally to ensure front position of menu not disturbed
     * @param infoHead title text
     * @param infoText message text
     */
    private void localError(String infoHead, String infoText) {
        holdFront = true;
        PolyGlot.CustomControls.InfoBox.error(infoHead, infoText, null);
        holdFront = false;
    }
    
    /**
     * Wrapped locally to ensure front position of menu not disturbed
     * @param infoHead title text
     * @param infoText message text
     */
    private int localYesNoCancel(String infoHead, String infoText) {
        holdFront = true;
        int ret = PolyGlot.CustomControls.InfoBox.yesNoCancel(infoHead, infoText, this);
        holdFront = false;
        return ret;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnLexicon = new javax.swing.JToggleButton();
        btnGrammar = new javax.swing.JToggleButton();
        btnTypes = new javax.swing.JToggleButton();
        btnLangProp = new javax.swing.JToggleButton();
        btnGenders = new javax.swing.JToggleButton();
        btnQuickEntry = new javax.swing.JToggleButton();
        btnLogos = new javax.swing.JToggleButton();
        btnThes = new javax.swing.JToggleButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        mnuNewLocal = new javax.swing.JMenuItem();
        mnuSaveLocal = new javax.swing.JMenuItem();
        mnuSaveAs = new javax.swing.JMenuItem();
        mnuOpenLocal = new javax.swing.JMenuItem();
        mnuRecents = new javax.swing.JMenu();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mnuExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        mnuImportFile = new javax.swing.JMenuItem();
        mnuExportToExcel = new javax.swing.JMenuItem();
        mnuExportFont = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuLangStats = new javax.swing.JMenuItem();
        mnuTransWindow = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuAbout = new javax.swing.JMenuItem();
        mnuChkUpdate = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem8 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        btnLexicon.setText("Lexicon");
        btnLexicon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLexiconActionPerformed(evt);
            }
        });

        btnGrammar.setText("Grammar");
        btnGrammar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGrammarActionPerformed(evt);
            }
        });

        btnTypes.setText("Types");
        btnTypes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTypesActionPerformed(evt);
            }
        });

        btnLangProp.setText("Lang Properties");
        btnLangProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLangPropActionPerformed(evt);
            }
        });

        btnGenders.setText("Genders");
        btnGenders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGendersActionPerformed(evt);
            }
        });

        btnQuickEntry.setText("Quickentry");
        btnQuickEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickEntryActionPerformed(evt);
            }
        });

        btnLogos.setText("Logographs");
        btnLogos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogosActionPerformed(evt);
            }
        });

        btnThes.setText("Thesaurus");
        btnThes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThesActionPerformed(evt);
            }
        });

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
        jMenu1.add(jSeparator2);

        mnuExit.setText("Exit");
        mnuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExitActionPerformed(evt);
            }
        });
        jMenu1.add(mnuExit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Tools");

        mnuImportFile.setText("Import from File");
        mnuImportFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuImportFileActionPerformed(evt);
            }
        });
        jMenu2.add(mnuImportFile);

        mnuExportToExcel.setText("Export to Excel");
        mnuExportToExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExportToExcelActionPerformed(evt);
            }
        });
        jMenu2.add(mnuExportToExcel);

        mnuExportFont.setText("Export Font");
        mnuExportFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExportFontActionPerformed(evt);
            }
        });
        jMenu2.add(mnuExportFont);
        jMenu2.add(jSeparator1);

        mnuLangStats.setText("Language Statistics");
        mnuLangStats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLangStatsActionPerformed(evt);
            }
        });
        jMenu2.add(mnuLangStats);

        mnuTransWindow.setText("Translation Window");
        mnuTransWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuTransWindowActionPerformed(evt);
            }
        });
        jMenu2.add(mnuTransWindow);

        jMenuBar1.add(jMenu2);

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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnLangProp, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                    .addComponent(btnGenders, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnThes, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLexicon, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnQuickEntry, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnTypes, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnGrammar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLogos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(btnLexicon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnQuickEntry)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnTypes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGrammar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLangProp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLogos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnThes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGenders))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mnuImportFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuImportFileActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrExcelImport.run(core);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuImportFileActionPerformed

    private void btnLexiconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLexiconActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lexHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnLexiconActionPerformed

    private void mnuSaveLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveLocalActionPerformed
        saveFile();
    }//GEN-LAST:event_mnuSaveLocalActionPerformed

    private void btnQuickEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickEntryActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (!btnLexicon.isSelected()) {
            btnLexicon.setSelected(true);
            lexHit();
        }
        quickEntryHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnQuickEntryActionPerformed

    private void btnGrammarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrammarActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        grammarHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnGrammarActionPerformed

    private void btnTypesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTypesActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrTypes s = ScrTypes.run(core);
        bindButtonToWindow(s, btnTypes);
        s.setVisible(true);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnTypesActionPerformed

    private void btnGendersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGendersActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrGenders s = ScrGenders.run(core);
        bindButtonToWindow(s, btnGenders);
        s.setVisible(true);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnGendersActionPerformed

    private void btnLangPropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLangPropActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrLangProps s = ScrLangProps.run(core);
        bindButtonToWindow(s, btnLangProp);
        s.setVisible(true);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnLangPropActionPerformed

    private void btnLogosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogosActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        logoHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnLogosActionPerformed

    private void btnThesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThesActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        thesHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnThesActionPerformed

    private void mnuNewLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuNewLocalActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        newFile(true);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuNewLocalActionPerformed

    private void mnuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveAsActionPerformed
        saveFileAs();
    }//GEN-LAST:event_mnuSaveAsActionPerformed

    private void mnuOpenLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOpenLocalActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        open();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuOpenLocalActionPerformed

    private void mnuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExitActionPerformed
        dispose();
    }//GEN-LAST:event_mnuExitActionPerformed

    private void mnuExportToExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExportToExcelActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        exportToExcel();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuExportToExcelActionPerformed

    private void mnuExportFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExportFontActionPerformed
        exportFont();
    }//GEN-LAST:event_mnuExportFontActionPerformed

    private void mnuLangStatsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLangStatsActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrLangStats.run(core);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuLangStatsActionPerformed

    private void mnuTransWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuTransWindowActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrTranslationWindow.run(core, this);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuTransWindowActionPerformed

    private void mnuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAboutActionPerformed
        openHelp();
    }//GEN-LAST:event_mnuAboutActionPerformed

    private void mnuChkUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuChkUpdateActionPerformed
        ScrAbout.run(core);
    }//GEN-LAST:event_mnuChkUpdateActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        checkForUpdates(true);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    /**
     * @param args the command line arguments
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
            // This is the only form that should have the traditional logger.
            java.util.logging.Logger.getLogger(ScrDictMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String overridePath = args.length > 1 ? args[1] : "";
                ScrDictMenu s = new ScrDictMenu(overridePath);

                // open file if one is provided via arguments
                if (args.length > 0) {
                    s.setFile(args[0]);
                }

                s.checkForUpdates(false);
                s.setupKeyStrokes();
                s.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnGenders;
    private javax.swing.JToggleButton btnGrammar;
    private javax.swing.JToggleButton btnLangProp;
    private javax.swing.JToggleButton btnLexicon;
    private javax.swing.JToggleButton btnLogos;
    private javax.swing.JToggleButton btnQuickEntry;
    private javax.swing.JToggleButton btnThes;
    private javax.swing.JToggleButton btnTypes;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JMenuItem mnuAbout;
    private javax.swing.JMenuItem mnuChkUpdate;
    private javax.swing.JMenuItem mnuExit;
    private javax.swing.JMenuItem mnuExportFont;
    private javax.swing.JMenuItem mnuExportToExcel;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mnuImportFile;
    private javax.swing.JMenuItem mnuLangStats;
    private javax.swing.JMenuItem mnuNewLocal;
    private javax.swing.JMenuItem mnuOpenLocal;
    private javax.swing.JMenu mnuRecents;
    private javax.swing.JMenuItem mnuSaveAs;
    private javax.swing.JMenuItem mnuSaveLocal;
    private javax.swing.JMenuItem mnuTransWindow;
    // End of variables declaration//GEN-END:variables
}
