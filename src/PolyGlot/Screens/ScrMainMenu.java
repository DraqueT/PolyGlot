/*
 * Copyright (c) 2017, draque.thompson
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
import PolyGlot.DictCore;
import PolyGlot.ExcelExport;
import PolyGlot.IOHandler;
import PolyGlot.Nodes.ConWord;
import PolyGlot.PGTUtil;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;

/**
 * Primary window for PolyGlot interface. Main running class that instantiates
 * core and handles other windows/UI. Depends on DictCore for all heavy logical 
 * lifting behind the scenes.
 * @author draque.thompson
 */
public class ScrMainMenu extends PFrame implements ApplicationListener {
    private PFrame curWindow = null;
    private final List<String> lastFiles;
    private String curFileName = "";
    /**
     * Creates new form ScrMainMenu
     * @param overridePath Path PolyGlot should treat as home directory (blank
     * if default)
     */
    public ScrMainMenu(String overridePath) {
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
        UIManager.put("ScrollBarUI", "PolyGlot.CustomControls.PScrollBarUI");
        UIManager.put("SplitPaneUI", "PolyGlot.CustomControls.PSplitPaneUI");
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
            InfoBox.warning("INI Save Error", "Unable to save settings file on exit.", this);
        }

        System.exit(0);
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
                    + "Please upgrade at https://java.com/en/download/.", this);
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
            lastFile.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    // only open if save/cancel test is passed
                    if (!saveOrCancelTest()) {
                        return;
                    }

                    setFile(curFile);
                    pushRecentFile(curFile);
                    populateRecentOpened();
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
        } catch (IOException e) {
            core = new DictCore(); // don't allow partial loads
            InfoBox.error("File Read Error", "Could not read file: " + fileName
                    + "\n\n " + e.getMessage(), this);
        } catch (IllegalStateException e) {
            InfoBox.warning("File Read Problems", "Problems reading file:\n"
                    + e.getLocalizedMessage(), this);
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
                    "Save current dictionary before performing action?", this);

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
        } catch (IOException | ParserConfigurationException |
                TransformerException e) {
            InfoBox.error("Save Error", "Unable to save to file: "
                    + curFileName + "\n\n" + e.getMessage(), this);
        }

        setCursor(Cursor.getDefaultCursor());

        if (cleanSave) {
            InfoBox.info("Success", "Dictionary saved to: "
                    + curFileName + ".", this);
        }

        return cleanSave;
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
                    "Overwrite existing file? " + fileName, this);

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
            InfoBox.error("Options Load Error", "Unable to load or create options file:\n"
                    + e.getLocalizedMessage(), this);
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
    }
    
    /**
     * Performs all actions necessary for changing the viewed panel
     * @param newScreen new window to display
     * @param display component to be added as main display
     */
    private void changeScreen(PFrame newScreen, Component display) {
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
            curWindow.setSize(jPanel2.getSize());
            curWindow.dispose();
        }
        
        Dimension dim = core.getOptionsManager().getScreenSize(newScreen.getClass().getName());
        
        if (dim == null) {
            dim = newScreen.getPreferredSize();
        }
        
        Insets insets = getInsets();
        try {
            this.setSizeSmooth(dim.width + jPanel1.getWidth() + insets.left + insets.right, dim.height + insets.bottom + insets.top, true);
        } catch (InterruptedException e) {
            InfoBox.error("Resize Error", "Unable to run resize animation: " + e.getLocalizedMessage(), this);
        }
        
        // set new screen
        GroupLayout layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(display, javax.swing.GroupLayout.Alignment.TRAILING, 
                    javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(display, javax.swing.GroupLayout.Alignment.TRAILING, 
                    javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        
        curWindow = newScreen;
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
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PolyGlot Dictionaries", "pgd", "xml");
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
            InfoBox.info("Export Status", "Dictionary exported to " + fileName + ".", this);
        } catch (Exception e) {
            InfoBox.info("Export Problem", e.getLocalizedMessage(), this);
        }
    }
    
    /**
     * Prompts user for a location and exports font within PGD to given path
     */
    public void exportFont() {
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
            IOHandler.exportFont(fileName, curFileName);
        } catch (IOException e) {
            InfoBox.error("Export Error", "Unable to export font: " + e.getMessage(), this);
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
                InfoBox.error("Help", "This is not yet implemented for OS: " + OS
                        + ". Please open readme.html in the application directory", this);
            }
        } catch (URISyntaxException | IOException e) {
            InfoBox.error("Missing File", "Unable to open readme.html.", this);
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
            InfoBox.warning("Open Lexicon", "Please open the Lexicon and select a word to use this feature.", this);
        }
    }
    
    /**
     * Retrieves currently selected word (if any) from ScrLexicon
     *
     * @return current word selected in scrLexicon, null otherwise (or if
     * lexicon is not visible)
     */
    public ConWord getCurrentWord() {
        ConWord ret = null;

        if (curWindow instanceof ScrLexicon) {
            ScrLexicon scrLexicon = (ScrLexicon) curWindow;
            ret = scrLexicon.getCurrentWord();
        } else {
            InfoBox.warning("Open Lexicon", "Please open the Lexicon and select a word to use this feature.", this);
        }

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

        jPanel1 = new javax.swing.JPanel();
        btnLexicon = new PButton(core);
        btnPos = new PButton(core);
        btnClasses = new PButton(core);
        btnGrammar = new PButton(core);
        btnLogos = new PButton(core);
        btnProp = new PButton(core);
        jPanel2 = new javax.swing.JPanel();
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
        mnuHelp = new javax.swing.JMenu();
        mnuAbout = new javax.swing.JMenuItem();
        mnuChkUpdate = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem8 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(102, 204, 255));

        btnLexicon.setText("Lexicon");
        btnLexicon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLexiconActionPerformed(evt);
            }
        });

        btnPos.setText("Parts of Speech");
        btnPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPosActionPerformed(evt);
            }
        });

        btnClasses.setText("Lexical Classes");
        btnClasses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClassesActionPerformed(evt);
            }
        });

        btnGrammar.setText("Grammar");
        btnGrammar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGrammarActionPerformed(evt);
            }
        });

        btnLogos.setText("Logographs");
        btnLogos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogosActionPerformed(evt);
            }
        });

        btnProp.setText("Lang Properties");
        btnProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPropActionPerformed(evt);
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
                    .addComponent(btnGrammar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnLexicon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClasses)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGrammar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLogos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnProp)
                .addContainerGap(254, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 426, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
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
        ScrLexicon lex = ScrLexicon.run(core);
        changeScreen(lex, lex.getWindow());
    }//GEN-LAST:event_btnLexiconActionPerformed

    private void btnPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPosActionPerformed
        ScrTypes types = ScrTypes.run(core);
        changeScreen(types, types.getWindow());
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
    }//GEN-LAST:event_mnuOpenLocalActionPerformed

    private void mnuPublishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPublishActionPerformed
        ScrPrintToPDF.run(core);
    }//GEN-LAST:event_mnuPublishActionPerformed

    private void mnuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExitActionPerformed
        dispose();
    }//GEN-LAST:event_mnuExitActionPerformed

    private void mnuImportFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuImportFileActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrExcelImport.run(core);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuImportFileActionPerformed

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
        if (InfoBox.yesNoCancel("Continue Operation?", "The statistics report can"
            + " take a long time to complete, depending on the complexity\n"
            + "of your conlang. Continue?", this) == JOptionPane.YES_OPTION) {
        ScrLangStats.run(core);
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
        changeScreen(s, s.getWindow());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnClassesActionPerformed

    private void btnGrammarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrammarActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrGrammarGuide s = new ScrGrammarGuide(core);
        changeScreen(s, s.getWindow());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnGrammarActionPerformed

    private void btnLogosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogosActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrLogoDetails s = new ScrLogoDetails(core);
        changeScreen(s, s.getWindow());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnLogosActionPerformed

    private void btnPropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPropActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrLangProps s = new ScrLangProps(core);
        changeScreen(s, s.getWindow());
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnPropActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrFamilies s = new ScrFamilies(core, this);
        s.setVisible(true);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_jMenuItem1ActionPerformed

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
            java.util.logging.Logger.getLogger(ScrMainMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String overridePath = args.length > 1 ? args[1] : "";
                ScrMainMenu s = new ScrMainMenu(overridePath);

                // open file if one is provided via arguments
                if (args.length > 0) {
                    s.setFile(args[0]);
                }

                s.checkForUpdates(false);
                s.setupKeyStrokes();
                s.setVisible(true);

                String problems = "";
                // Test for JavaFX and inform user that it is not present, they cannot run PolyGlot
                // Test for minimum version of Java (8)
                String jVer = System.getProperty("java.version");
                if (jVer.startsWith("1.5") || jVer.startsWith("1.6") || jVer.startsWith("1.7")) {
                    problems += "Unable to start PolyGlot without Java 8.";
                }
                try {
                    this.getClass().getClassLoader().loadClass("javafx.embed.swing.JFXPanel");
                } catch (ClassNotFoundException e) {
                    problems += "\nUnable to load Java FX. Download and install to use PolyGlot.";
                }

                if (!problems.equals("")) {
                    InfoBox.error("Unable to start", problems + "\nPlease upgrade and restart to continue.", s);
                    s.dispose();
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClasses;
    private javax.swing.JButton btnGrammar;
    private javax.swing.JButton btnLexicon;
    private javax.swing.JButton btnLogos;
    private javax.swing.JButton btnPos;
    private javax.swing.JButton btnProp;
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
     * @return 
     */
    @Override 
    public boolean canClose() {
        return true;
    }
}
