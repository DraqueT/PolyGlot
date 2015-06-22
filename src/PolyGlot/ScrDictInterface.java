/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 * See LICENSE.TXT included with this code to read the full license agreement.
 *
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.simplericity.macify.eawt.*;

/**
 * This is the main interface for PolyGlot.
 *
 * @author draque
 */
public class ScrDictInterface extends PFrame implements ApplicationListener {
// implementation of ApplicationListener is part of macify

    private DictCore core;
    private Map scrToCoreMap = new HashMap<Integer, Integer>();
    private Map scrTypeMap = new HashMap<String, Integer>();
    private Map scrToCoreTypes = new HashMap<Integer, Integer>();
    private Map scrToCoreGenders = new HashMap<Integer, Integer>();
    private Map scrGenderMap = new HashMap<String, Integer>();
    private final List<Window> childFrames = new ArrayList<Window>();
    private final DefaultListModel dListModel;
    private final DefaultListModel tListModel;
    private final DefaultListModel gListModel;
    private DefaultTableModel procTableModel;
    private String curFileName = "";
    private boolean curPopulating = false;
    private final String screenTitle = "PolyGlot BETA";
    private boolean filterListenersActive = true;
    private String saveError = "";

    /**
     * Creates new form scrDictInterface
     */
    public ScrDictInterface() {
        this.setupKeyStrokes();

        initComponents();

        // models for controling list objects
        dListModel = new DefaultListModel();
        lstDict.setModel(dListModel);
        tListModel = new DefaultListModel();
        lstTypesList.setModel(tListModel);
        gListModel = new DefaultListModel();
        lstGenderList.setModel(gListModel);

        newFile(true);

        setTitle(screenTitle + " " + core.getVersion());

        setupListeners();
        setupProcTable();
        setupAccelerators();

        // activates macify for menu integration...
        if (System.getProperty("os.name").startsWith("Mac")) {
            activateMacify();
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
        InfoBox.info("Printing", "PolyGlot does not currently support printing.", this);
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
     * sets menu accelerators and menu item text to reflect this
     */
    private void setupAccelerators() {
        String OS = System.getProperty("os.name");
        if (OS.startsWith("Mac")) {
            mnuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
        } else {
            // I'm pretty sure all other OSes just use CTRL+ to do stuff
            mnuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
        }
    }

    private void setupProcTable() {
        procTableModel = new DefaultTableModel();
        procTableModel.addColumn("Character(s)");
        procTableModel.addColumn("Pronuncation");
        tblProcGuide.setModel(procTableModel);

        procTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                saveProcGuide();
            }
        });

        Font defaultFont = new JLabel().getFont();

        TableColumn column = tblProcGuide.getColumnModel().getColumn(0);
        column.setCellEditor(new TableColumnEditor(core.getPropertiesManager().getFontCon()));
        column.setCellRenderer(new TableColumnRenderer(core.getPropertiesManager().getFontCon()));

        column = tblProcGuide.getColumnModel().getColumn(1);
        column.setCellEditor(new TableColumnEditor(defaultFont));
        column.setCellRenderer(new TableColumnRenderer(defaultFont));

        // disable tab/arrow selection
        tblProcGuide.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "none");
        tblProcGuide.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "none");
        tblProcGuide.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
        tblProcGuide.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK), "none");
        tblProcGuide.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
        tblProcGuide.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK), "none");
        tblProcGuide.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        tblProcGuide.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), "none");
        tblProcGuide.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        tblProcGuide.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), "none");
    }

    /**
     * @param args the command line arguments
     * argument 1 = file to open. Blank of value of PGTUtil.emptyFile is no file
     * argument 2 = override path of program. Blank or value of PGTUtil.emptyFile if none
     */
    public static void main(String args[]) {
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ScrDictInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ScrDictInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ScrDictInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ScrDictInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        ScrDictInterface dictInterface = new ScrDictInterface();

        // open file if one is provided via arguments
        if (args.length > 0) {
            dictInterface.setFile(args[0]);
        }
        
        if (args.length > 1) {
            dictInterface.setOverrideProgramPath(args[1]);
        }

        dictInterface.checkForUpdates(false); // verbose set to only notify if update
        dictInterface.setupKeyStrokes();
        dictInterface.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings({"unchecked", "Convert2Lambda"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        tabDict = new javax.swing.JPanel();
        pnlFilter = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblConWordFilter = new javax.swing.JLabel();
        lblLocalWordFilter = new javax.swing.JLabel();
        lblTypeFilter = new javax.swing.JLabel();
        txtConWordFilter = new javax.swing.JTextField();
        txtLocalWordFilter = new javax.swing.JTextField();
        cmbTypeFilter = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        lblGenderFilter = new javax.swing.JLabel();
        lblPronunciationFilter = new javax.swing.JLabel();
        lblDefFilter = new javax.swing.JLabel();
        cmbGenderFilter = new javax.swing.JComboBox();
        txtPronunciationFilter = new javax.swing.JTextField();
        txtDefFilter = new javax.swing.JTextField();
        scrlDict = new javax.swing.JScrollPane();
        lstDict = new javax.swing.JList();
        btnAdd = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        pnlProperties = new javax.swing.JPanel();
        txtConWordProp = new javax.swing.JTextField();
        lblConWordProp = new javax.swing.JLabel();
        lblLocalWordProp = new javax.swing.JLabel();
        txtLocalWordProp = new javax.swing.JTextField();
        lblTypeProp = new javax.swing.JLabel();
        cmbTypeProp = new javax.swing.JComboBox();
        lblGenderProp = new javax.swing.JLabel();
        cmbGenderProp = new javax.swing.JComboBox();
        lblPrononciationProp = new javax.swing.JLabel();
        txtPronunciationProp = new javax.swing.JTextField();
        lblDefinitionProp = new javax.swing.JLabel();
        sclDefProp = new javax.swing.JScrollPane();
        txtDefProp = new javax.swing.JTextArea();
        btnConwordDeclensions = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        txtWordErrorBox = new javax.swing.JTextPane();
        chkPronunciationOverrideProp = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        chkWordRulesOverride = new javax.swing.JCheckBox();
        btnWordLogographs = new javax.swing.JButton();
        tabType = new javax.swing.JPanel();
        sclTypesList = new javax.swing.JScrollPane();
        lstTypesList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        txtTypeName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtTypesNotes = new javax.swing.JTextArea();
        btnAddType = new javax.swing.JButton();
        btnDeleteType = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        chkTypeGenderMandatory = new javax.swing.JCheckBox();
        chkTypeProcMandatory = new javax.swing.JCheckBox();
        chkTypeDefinitionMandatory = new javax.swing.JCheckBox();
        txtTypesErrorBox = new javax.swing.JTextField();
        btnConjDecl = new javax.swing.JButton();
        btnAutoConjDecSetup = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        txtTypePattern = new javax.swing.JTextField();
        tabGender = new javax.swing.JPanel();
        sclGenderList = new javax.swing.JScrollPane();
        lstGenderList = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        txtGenderName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtGenderNotes = new javax.swing.JTextArea();
        btnAddGender = new javax.swing.JButton();
        btnDeleteGender = new javax.swing.JButton();
        txtGendersErrorBox = new javax.swing.JTextField();
        tabLangProp = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtLangName = new javax.swing.JTextField();
        txtLangFont = new javax.swing.JTextField();
        btnChangeFont = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        txtAlphaOrder = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        chkPropTypesMandatory = new javax.swing.JCheckBox();
        chkPropLocalMandatory = new javax.swing.JCheckBox();
        chkPropWordUniqueness = new javax.swing.JCheckBox();
        chkPropLocalUniqueness = new javax.swing.JCheckBox();
        chkIgnoreCase = new javax.swing.JCheckBox();
        chkDisableProcRegex = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        sclProcGuide = new javax.swing.JScrollPane();
        tblProcGuide = new javax.swing.JTable();
        chkAutopopProcs = new javax.swing.JCheckBox();
        btnUpProc = new javax.swing.JButton();
        btnDownProc = new javax.swing.JButton();
        btnAddProcGuide = new javax.swing.JButton();
        btnDeleteProcGuide = new javax.swing.JButton();
        btnRecalcProc = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        mnuSave = new javax.swing.JMenuItem();
        mnuSaveAs = new javax.swing.JMenuItem();
        mnuOpen = new javax.swing.JMenuItem();
        mnuNew = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mnuExit = new javax.swing.JMenuItem();
        mnuTools = new javax.swing.JMenu();
        mnuQuickEntry = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuImportExcel = new javax.swing.JMenuItem();
        mnuExcelExport = new javax.swing.JMenuItem();
        mnuExportFont = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mnuTranslation = new javax.swing.JMenuItem();
        mnuLangStats = new javax.swing.JMenuItem();
        mnuView = new javax.swing.JMenu();
        mnuGrammarGuide = new javax.swing.JMenuItem();
        mnuViewLogographsDetail = new javax.swing.JMenuItem();
        mnuThesaurus = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuPloyHelp = new javax.swing.JMenuItem();
        mnuAbout = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mnuCheckForUpdates = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTabbedPane1.setToolTipText("Language Statistics");
        jTabbedPane1.setName("Language Properties"); // NOI18N
        jTabbedPane1.setSize(new java.awt.Dimension(21, 2147483647));
        jTabbedPane1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTabbedPane1FocusGained(evt);
            }
        });

        pnlFilter.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("<HTML><b>FILTER/SEARCH</b>");
        jLabel3.setToolTipText("");
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        lblConWordFilter.setText("Con Word");

        lblLocalWordFilter.setText("Local Word");

        lblTypeFilter.setText("Type");

        txtConWordFilter.setToolTipText("filter via Con Word");

        txtLocalWordFilter.setToolTipText("filter via local word");

        cmbTypeFilter.setToolTipText("filter via type");
        cmbTypeFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTypeFilterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblConWordFilter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtConWordFilter))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblLocalWordFilter)
                            .addComponent(lblTypeFilter))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtLocalWordFilter)
                            .addComponent(cmbTypeFilter, 0, 144, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConWordFilter)
                    .addComponent(txtConWordFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLocalWordFilter)
                    .addComponent(txtLocalWordFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTypeFilter)
                    .addComponent(cmbTypeFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblGenderFilter.setText("Gender");

        lblPronunciationFilter.setText("Pronunciation");

        lblDefFilter.setText("Definition");
        lblDefFilter.setToolTipText("");

        cmbGenderFilter.setToolTipText("filter via word gender");
        cmbGenderFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbGenderFilterActionPerformed(evt);
            }
        });

        txtPronunciationFilter.setToolTipText("filter via pronunciation");

        txtDefFilter.setToolTipText("filter on definition");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lblGenderFilter)
                        .addGap(49, 49, 49)
                        .addComponent(cmbGenderFilter, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPronunciationFilter)
                            .addComponent(lblDefFilter))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPronunciationFilter)
                            .addComponent(txtDefFilter))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGenderFilter)
                    .addComponent(cmbGenderFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPronunciationFilter)
                    .addComponent(txtPronunciationFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDefFilter)
                    .addComponent(txtDefFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlFilterLayout = new javax.swing.GroupLayout(pnlFilter);
        pnlFilter.setLayout(pnlFilterLayout);
        pnlFilterLayout.setHorizontalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jLabel3)
        );
        pnlFilterLayout.setVerticalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterLayout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        lstDict.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstDict.setToolTipText("List of Conlang Words");
        lstDict.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        lstDict.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstDictValueChanged(evt);
            }
        });
        scrlDict.setViewportView(lstDict);

        btnAdd.setText("+");
        btnAdd.setToolTipText("Adds new word to dictionary");
        btnAdd.setActionCommand("");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnDelete.setText("-");
        btnDelete.setToolTipText("Deletes selected word from dictionary");
        btnDelete.setActionCommand("");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        pnlProperties.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        txtConWordProp.setToolTipText("Constructed language word value");

        lblConWordProp.setText("Con Word");

        lblLocalWordProp.setText("Local Word");

        txtLocalWordProp.setToolTipText("Synonym for conword in local language");

        lblTypeProp.setText("Type");

        cmbTypeProp.setToolTipText("word's part of speech");
        cmbTypeProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTypePropActionPerformed(evt);
            }
        });

        lblGenderProp.setText("Gender");

        cmbGenderProp.setToolTipText("word's gender (if any)");
        cmbGenderProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbGenderPropActionPerformed(evt);
            }
        });

        lblPrononciationProp.setText("Pronunciation");

        txtPronunciationProp.setToolTipText("word's pronunciation");

        lblDefinitionProp.setText("Definition");

        txtDefProp.setColumns(20);
        txtDefProp.setLineWrap(true);
        txtDefProp.setRows(5);
        txtDefProp.setToolTipText("Extended defition of word and notes");
        txtDefProp.setWrapStyleWord(true);
        sclDefProp.setViewportView(txtDefProp);

        btnConwordDeclensions.setText("Declensions");
        btnConwordDeclensions.setToolTipText("Displays fields for declensions/conjugations forms of current word");
        btnConwordDeclensions.setEnabled(false);
        btnConwordDeclensions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConwordDeclensionsActionPerformed(evt);
            }
        });

        txtWordErrorBox.setEditable(false);
        txtWordErrorBox.setForeground(new java.awt.Color(255, 0, 0));
        txtWordErrorBox.setToolTipText("Displays problems with a word that must be corrected before deselecting it.");
        txtWordErrorBox.setDisabledTextColor(new java.awt.Color(255, 0, 0));
        txtWordErrorBox.setEnabled(false);
        jScrollPane5.setViewportView(txtWordErrorBox);

        chkPronunciationOverrideProp.setToolTipText("Select this to override auto pronunciation generation for this word.");
        chkPronunciationOverrideProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkPronunciationOverridePropActionPerformed(evt);
            }
        });

        jLabel11.setText("Override Rules");
        jLabel11.setToolTipText("");

        chkWordRulesOverride.setToolTipText("Overrides all typically enforced requirements for this word, allowing it to be saved as an exception");
        chkWordRulesOverride.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkWordRulesOverrideActionPerformed(evt);
            }
        });

        btnWordLogographs.setText("Logographs");
        btnWordLogographs.setToolTipText("Logographs associated with this word");
        btnWordLogographs.setEnabled(false);
        btnWordLogographs.setMaximumSize(new java.awt.Dimension(121, 29));
        btnWordLogographs.setMinimumSize(new java.awt.Dimension(121, 29));
        btnWordLogographs.setPreferredSize(new java.awt.Dimension(121, 29));
        btnWordLogographs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWordLogographsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlPropertiesLayout = new javax.swing.GroupLayout(pnlProperties);
        pnlProperties.setLayout(pnlPropertiesLayout);
        pnlPropertiesLayout.setHorizontalGroup(
            pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sclDefProp)
                    .addGroup(pnlPropertiesLayout.createSequentialGroup()
                        .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblConWordProp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblLocalWordProp, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtLocalWordProp)
                            .addComponent(txtConWordProp)))
                    .addGroup(pnlPropertiesLayout.createSequentialGroup()
                        .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblGenderProp, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPrononciationProp)
                            .addComponent(lblTypeProp, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbTypeProp, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnlPropertiesLayout.createSequentialGroup()
                                .addComponent(txtPronunciationProp)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkPronunciationOverrideProp))
                            .addComponent(cmbGenderProp, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlPropertiesLayout.createSequentialGroup()
                        .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(lblDefinitionProp))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(chkWordRulesOverride)))
                .addContainerGap())
            .addGroup(pnlPropertiesLayout.createSequentialGroup()
                .addComponent(btnConwordDeclensions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnWordLogographs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlPropertiesLayout.setVerticalGroup(
            pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConWordProp)
                    .addComponent(txtConWordProp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLocalWordProp)
                    .addComponent(txtLocalWordProp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTypeProp)
                    .addComponent(cmbTypeProp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGenderProp)
                    .addComponent(cmbGenderProp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPrononciationProp)
                        .addComponent(txtPronunciationProp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(chkPronunciationOverrideProp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlPropertiesLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDefinitionProp))
                    .addComponent(chkWordRulesOverride))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sclDefProp, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConwordDeclensions)
                    .addComponent(btnWordLogographs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout tabDictLayout = new javax.swing.GroupLayout(tabDict);
        tabDict.setLayout(tabDictLayout);
        tabDictLayout.setHorizontalGroup(
            tabDictLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabDictLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabDictLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(tabDictLayout.createSequentialGroup()
                        .addGroup(tabDictLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(scrlDict, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(tabDictLayout.createSequentialGroup()
                                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlProperties, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        tabDictLayout.setVerticalGroup(
            tabDictLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabDictLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabDictLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tabDictLayout.createSequentialGroup()
                        .addComponent(scrlDict, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tabDictLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAdd)
                            .addComponent(btnDelete))
                        .addGap(6, 6, 6))
                    .addGroup(tabDictLayout.createSequentialGroup()
                        .addComponent(pnlProperties, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        jTabbedPane1.addTab("Lexicon", null, tabDict, "Your lexicon");

        lstTypesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstTypesList.setToolTipText("List of types for your con lang");
        lstTypesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstTypesListValueChanged(evt);
            }
        });
        sclTypesList.setViewportView(lstTypesList);

        jLabel1.setText("Type Name");

        txtTypeName.setToolTipText("type name");
        txtTypeName.setEnabled(false);

        jLabel2.setText("Notes");

        txtTypesNotes.setColumns(20);
        txtTypesNotes.setLineWrap(true);
        txtTypesNotes.setRows(5);
        txtTypesNotes.setToolTipText("notes for type");
        txtTypesNotes.setWrapStyleWord(true);
        txtTypesNotes.setEnabled(false);
        jScrollPane2.setViewportView(txtTypesNotes);

        btnAddType.setText("+");
        btnAddType.setToolTipText("Creates a new type");
        btnAddType.setVerifyInputWhenFocusTarget(false);
        btnAddType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTypeActionPerformed(evt);
            }
        });

        btnDeleteType.setText("-");
        btnDeleteType.setToolTipText("Deletes selected type");
        btnDeleteType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteTypeActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        chkTypeGenderMandatory.setText("Gender Mandatory");
        chkTypeGenderMandatory.setToolTipText("Select to make the Gender field mandatory for all words of this type.");
        chkTypeGenderMandatory.setEnabled(false);
        chkTypeGenderMandatory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTypeGenderMandatoryActionPerformed(evt);
            }
        });

        chkTypeProcMandatory.setText("Pronunciation Mandatory");
        chkTypeProcMandatory.setToolTipText("Select to make the Pronunciation field mandatory for all words of this type.");
        chkTypeProcMandatory.setEnabled(false);
        chkTypeProcMandatory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTypeProcMandatoryActionPerformed(evt);
            }
        });

        chkTypeDefinitionMandatory.setText("Definition Mandatory");
        chkTypeDefinitionMandatory.setToolTipText("Select to make the Definition field mandatory for all words of this type.");
        chkTypeDefinitionMandatory.setEnabled(false);
        chkTypeDefinitionMandatory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTypeDefinitionMandatoryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkTypeGenderMandatory)
                    .addComponent(chkTypeProcMandatory)
                    .addComponent(chkTypeDefinitionMandatory))
                .addContainerGap(68, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chkTypeGenderMandatory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkTypeProcMandatory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkTypeDefinitionMandatory)
                .addGap(35, 35, 35))
        );

        txtTypesErrorBox.setEditable(false);
        txtTypesErrorBox.setDisabledTextColor(new java.awt.Color(255, 51, 51));
        txtTypesErrorBox.setEnabled(false);

        btnConjDecl.setText("Conjugations/Declensions");
        btnConjDecl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConjDeclActionPerformed(evt);
            }
        });

        btnAutoConjDecSetup.setText("Conj/Decl Autogeneration");
        btnAutoConjDecSetup.setToolTipText("Set up ules for autogeneration of conjugations or declensions for this part of speech");
        btnAutoConjDecSetup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAutoConjDecSetupActionPerformed(evt);
            }
        });

        jLabel10.setText("Type Pattern");

        txtTypePattern.setToolTipText("Regex pattern words of this type must conform to (blank for no enforcement). Text red if an invalid regex.");
        txtTypePattern.setEnabled(false);

        javax.swing.GroupLayout tabTypeLayout = new javax.swing.GroupLayout(tabType);
        tabType.setLayout(tabTypeLayout);
        tabTypeLayout.setHorizontalGroup(
            tabTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabTypeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(tabTypeLayout.createSequentialGroup()
                        .addComponent(btnAddType, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDeleteType, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(sclTypesList, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tabTypeLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(tabTypeLayout.createSequentialGroup()
                        .addGroup(tabTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnConjDecl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtTypesErrorBox, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(tabTypeLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtTypeName))
                            .addComponent(btnAutoConjDecSetup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(tabTypeLayout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtTypePattern)))
                        .addContainerGap())))
        );
        tabTypeLayout.setVerticalGroup(
            tabTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabTypeLayout.createSequentialGroup()
                .addGroup(tabTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tabTypeLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(tabTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtTypeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tabTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(txtTypePattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnConjDecl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAutoConjDecSetup)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(sclTypesList))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tabTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnDeleteType)
                        .addComponent(txtTypesErrorBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabTypeLayout.createSequentialGroup()
                        .addComponent(btnAddType)
                        .addContainerGap())))
        );

        jTabbedPane1.addTab("Types", null, tabType, "Add or edit parts of speech");

        lstGenderList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstGenderList.setToolTipText("list of genders for your conlang");
        lstGenderList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstGenderListValueChanged(evt);
            }
        });
        sclGenderList.setViewportView(lstGenderList);

        jLabel4.setText("Gender");

        txtGenderName.setToolTipText("gender name");
        txtGenderName.setEnabled(false);

        jLabel5.setText("Notes");

        txtGenderNotes.setColumns(20);
        txtGenderNotes.setLineWrap(true);
        txtGenderNotes.setRows(5);
        txtGenderNotes.setToolTipText("notes on gender");
        txtGenderNotes.setWrapStyleWord(true);
        txtGenderNotes.setEnabled(false);
        jScrollPane3.setViewportView(txtGenderNotes);

        btnAddGender.setText("+");
        btnAddGender.setToolTipText("Add a new gender to conlang");
        btnAddGender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddGenderActionPerformed(evt);
            }
        });

        btnDeleteGender.setText("-");
        btnDeleteGender.setToolTipText("Remove gender from conlang");
        btnDeleteGender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteGenderActionPerformed(evt);
            }
        });

        txtGendersErrorBox.setDisabledTextColor(new java.awt.Color(255, 51, 51));
        txtGendersErrorBox.setEnabled(false);

        javax.swing.GroupLayout tabGenderLayout = new javax.swing.GroupLayout(tabGender);
        tabGender.setLayout(tabGenderLayout);
        tabGenderLayout.setHorizontalGroup(
            tabGenderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabGenderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabGenderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(tabGenderLayout.createSequentialGroup()
                        .addComponent(btnAddGender, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(79, 79, 79)
                        .addComponent(btnDeleteGender, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(sclGenderList, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabGenderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtGendersErrorBox)
                    .addGroup(tabGenderLayout.createSequentialGroup()
                        .addGroup(tabGenderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(tabGenderLayout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtGenderName, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE))
                            .addGroup(tabGenderLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane3))
                        .addContainerGap())))
        );
        tabGenderLayout.setVerticalGroup(
            tabGenderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabGenderLayout.createSequentialGroup()
                .addGroup(tabGenderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tabGenderLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(tabGenderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtGenderName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
                    .addComponent(sclGenderList))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabGenderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tabGenderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnDeleteGender)
                        .addComponent(txtGendersErrorBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabGenderLayout.createSequentialGroup()
                        .addComponent(btnAddGender)
                        .addContainerGap())))
        );

        jTabbedPane1.addTab("Genders", null, tabGender, "Add or edit genders to be assigned to words");

        jLabel6.setText("Conlang Name");

        jLabel7.setText("Conlang Font");

        txtLangName.setToolTipText("Name of your constructed language");

        txtLangFont.setEditable(false);
        txtLangFont.setToolTipText("Your conlang's font");

        btnChangeFont.setText("Change Font");
        btnChangeFont.setToolTipText("Browse for a new conlang font");
        btnChangeFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeFontActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel8.setText("Alphabetical Order");

        txtAlphaOrder.setToolTipText("Blank = standard order");

        jLabel9.setText("Letter order here determines alphabetic listing in your dictionary.");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtAlphaOrder)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAlphaOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        chkPropTypesMandatory.setText("Types Mandatory");
        chkPropTypesMandatory.setToolTipText("Select to make types a mandatory field for all words.");
        chkPropTypesMandatory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkPropTypesMandatoryActionPerformed(evt);
            }
        });

        chkPropLocalMandatory.setText("Local Mandatory");
        chkPropLocalMandatory.setToolTipText("Select to make local word a mandatory field for all words.");
        chkPropLocalMandatory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkPropLocalMandatoryActionPerformed(evt);
            }
        });

        chkPropWordUniqueness.setText("Word Uniqueness");
        chkPropWordUniqueness.setToolTipText("Enforce uniqueness of words based on Con Word field.");
        chkPropWordUniqueness.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkPropWordUniquenessActionPerformed(evt);
            }
        });

        chkPropLocalUniqueness.setText("Local Uniqueness");
        chkPropLocalUniqueness.setToolTipText("Enforce uniqueness of words based on Local Word field.");
        chkPropLocalUniqueness.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkPropLocalUniquenessActionPerformed(evt);
            }
        });

        chkIgnoreCase.setText("Ignore Case");
        chkIgnoreCase.setToolTipText("Check this to ignore case in searches and setting alphabetic order. (does NOT include regex statements)");
        chkIgnoreCase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkIgnoreCaseActionPerformed(evt);
            }
        });

        chkDisableProcRegex.setText("Disable Proc Regex");
        chkDisableProcRegex.setToolTipText("Disable regex on pronunciations (this allows for ignoring case on pronunciation)");
        chkDisableProcRegex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDisableProcRegexActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkPropTypesMandatory)
                    .addComponent(chkPropLocalMandatory)
                    .addComponent(chkPropWordUniqueness)
                    .addComponent(chkPropLocalUniqueness)
                    .addComponent(chkIgnoreCase)
                    .addComponent(chkDisableProcRegex))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkPropTypesMandatory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkPropLocalMandatory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkPropWordUniqueness)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkPropLocalUniqueness)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkIgnoreCase)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkDisableProcRegex)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel14.setText("Character Pronunciation Guide");

        tblProcGuide.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Character", "Pronunciation"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblProcGuide.setToolTipText("Add characters (or sets of characters) here with their associated pronunciations.");
        tblProcGuide.setColumnSelectionAllowed(true);
        tblProcGuide.setRowHeight(30);
        tblProcGuide.getTableHeader().setReorderingAllowed(false);
        sclProcGuide.setViewportView(tblProcGuide);
        tblProcGuide.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        chkAutopopProcs.setText("Autopopulate");
        chkAutopopProcs.setToolTipText("If selected, PolyGlot will attempt to generate pronunciations as you type your words.");
        chkAutopopProcs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAutopopProcsActionPerformed(evt);
            }
        });

        btnUpProc.setText("↑");
        btnUpProc.setToolTipText("Move pronunciation up, increasing priority.");
        btnUpProc.setSize(new java.awt.Dimension(97, 30));
        btnUpProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpProcActionPerformed(evt);
            }
        });

        btnDownProc.setText("↓");
        btnDownProc.setToolTipText("Move pronunciation down, lowering priority.");
        btnDownProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownProcActionPerformed(evt);
            }
        });

        btnAddProcGuide.setText("+");
        btnAddProcGuide.setToolTipText("Add a row");
        btnAddProcGuide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProcGuideActionPerformed(evt);
            }
        });

        btnDeleteProcGuide.setText("-");
        btnDeleteProcGuide.setToolTipText("Delete a row");
        btnDeleteProcGuide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteProcGuideActionPerformed(evt);
            }
        });

        btnRecalcProc.setText("Recalculate");
        btnRecalcProc.setToolTipText("Recalculates pronunciations for all words which do not have the pronunciation override feature set");
        btnRecalcProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecalcProcActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(chkAutopopProcs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRecalcProc)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(sclProcGuide, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel7Layout.createSequentialGroup()
                                        .addComponent(btnAddProcGuide, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(125, 125, 125)
                                        .addComponent(btnDeleteProcGuide, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnUpProc, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addComponent(btnDownProc, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())))))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(btnUpProc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 107, Short.MAX_VALUE)
                        .addComponent(btnDownProc))
                    .addComponent(sclProcGuide, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddProcGuide)
                    .addComponent(btnDeleteProcGuide))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chkAutopopProcs)
                    .addComponent(btnRecalcProc)))
        );

        javax.swing.GroupLayout tabLangPropLayout = new javax.swing.GroupLayout(tabLangProp);
        tabLangProp.setLayout(tabLangPropLayout);
        tabLangPropLayout.setHorizontalGroup(
            tabLangPropLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabLangPropLayout.createSequentialGroup()
                .addComponent(btnChangeFont)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(tabLangPropLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabLangPropLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(tabLangPropLayout.createSequentialGroup()
                        .addGroup(tabLangPropLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tabLangPropLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtLangName, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                            .addComponent(txtLangFont))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(tabLangPropLayout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        tabLangPropLayout.setVerticalGroup(
            tabLangPropLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabLangPropLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabLangPropLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtLangName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(tabLangPropLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtLangFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnChangeFont)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(tabLangPropLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Language Properties", null, tabLangProp, "Edit global language properties here");

        mnuFile.setText("File");

        mnuSave.setText("Save");
        mnuSave.setToolTipText("Save your current dictionary");
        mnuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveActionPerformed(evt);
            }
        });
        mnuFile.add(mnuSave);

        mnuSaveAs.setText("Save As");
        mnuSaveAs.setToolTipText("Save your current dictionary to a particular name/location");
        mnuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveAsActionPerformed(evt);
            }
        });
        mnuFile.add(mnuSaveAs);

        mnuOpen.setText("Open");
        mnuOpen.setToolTipText("Open a PolyGlot dictionary");
        mnuOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenActionPerformed(evt);
            }
        });
        mnuFile.add(mnuOpen);

        mnuNew.setText("New");
        mnuNew.setToolTipText("Create new language file");
        mnuNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuNewActionPerformed(evt);
            }
        });
        mnuFile.add(mnuNew);
        mnuFile.add(jSeparator2);

        mnuExit.setText("Exit");
        mnuExit.setToolTipText("Exit PolyGLot");
        mnuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExitActionPerformed(evt);
            }
        });
        mnuFile.add(mnuExit);

        jMenuBar1.add(mnuFile);

        mnuTools.setText("Tools");

        mnuQuickEntry.setText("Word Quickentry");
        mnuQuickEntry.setToolTipText("A tool for quickly entering many words, one after another");
        mnuQuickEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuQuickEntryActionPerformed(evt);
            }
        });
        mnuTools.add(mnuQuickEntry);
        mnuTools.add(jSeparator1);

        mnuImportExcel.setText("Import from File");
        mnuImportExcel.setToolTipText("Import words from Excel or CSV file");
        mnuImportExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuImportExcelActionPerformed(evt);
            }
        });
        mnuTools.add(mnuImportExcel);

        mnuExcelExport.setText("Export to Excel");
        mnuExcelExport.setToolTipText("Export words to excel file");
        mnuExcelExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExcelExportActionPerformed(evt);
            }
        });
        mnuTools.add(mnuExcelExport);

        mnuExportFont.setText("Export Font");
        mnuExportFont.setToolTipText("Export conlang font embedded in PGD file");
        mnuExportFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExportFontActionPerformed(evt);
            }
        });
        mnuTools.add(mnuExportFont);
        mnuTools.add(jSeparator4);

        mnuTranslation.setText("Translation Window");
        mnuTranslation.setToolTipText("A tool to help you translate to your ConLang");
        mnuTranslation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuTranslationActionPerformed(evt);
            }
        });
        mnuTools.add(mnuTranslation);

        mnuLangStats.setText("Language Statistics");
        mnuLangStats.setToolTipText("A statistical breakdown of linguistic features");
        mnuLangStats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLangStatsActionPerformed(evt);
            }
        });
        mnuTools.add(mnuLangStats);

        jMenuBar1.add(mnuTools);

        mnuView.setText("View");

        mnuGrammarGuide.setText("Grammar Guide");
        mnuGrammarGuide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuGrammarGuideActionPerformed(evt);
            }
        });
        mnuView.add(mnuGrammarGuide);

        mnuViewLogographsDetail.setText("Logograph Dictionary");
        mnuViewLogographsDetail.setToolTipText("Logographic dictionary tool");
        mnuViewLogographsDetail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuViewLogographsDetailActionPerformed(evt);
            }
        });
        mnuView.add(mnuViewLogographsDetail);

        mnuThesaurus.setText("Thesaurus");
        mnuThesaurus.setToolTipText("A thesaurus to help you organize your words.");
        mnuThesaurus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuThesaurusActionPerformed(evt);
            }
        });
        mnuView.add(mnuThesaurus);

        jMenuBar1.add(mnuView);

        mnuHelp.setText("Help");

        mnuPloyHelp.setText("Help");
        mnuPloyHelp.setToolTipText("Open PolyGlot help file");
        mnuPloyHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPloyHelpActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuPloyHelp);

        mnuAbout.setText("About");
        mnuAbout.setToolTipText("About PolyGlot");
        mnuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAboutActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuAbout);
        mnuHelp.add(jSeparator3);

        mnuCheckForUpdates.setText("Check for Updates");
        mnuCheckForUpdates.setToolTipText("Check online for new versions of PolyGlot");
        mnuCheckForUpdates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCheckForUpdatesActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuCheckForUpdates);

        jMenuBar1.add(mnuHelp);

        jMenu1.setText("DEV");

        jMenuItem2.setText("LEXICON");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem3.setText("TYPES");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mnuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveActionPerformed
        saveFile();
    }//GEN-LAST:event_mnuSaveActionPerformed

    private void mnuOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOpenActionPerformed
        openFile();
    }//GEN-LAST:event_mnuOpenActionPerformed

    private void mnuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveAsActionPerformed
        if (saveFileAs()) {
            saveFile();
        }
    }//GEN-LAST:event_mnuSaveAsActionPerformed

    private void mnuNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuNewActionPerformed
        newFile(true);
    }//GEN-LAST:event_mnuNewActionPerformed

    private void mnuImportExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuImportExcelActionPerformed
        importExcel();
    }//GEN-LAST:event_mnuImportExcelActionPerformed

    private void lstDictValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstDictValueChanged
        // to avoid multiple, useless firings
        if (!evt.getValueIsAdjusting()) {
            this.popWordProps(false);
        }
    }//GEN-LAST:event_lstDictValueChanged

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        this.newWord();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        this.deleteWord();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnAddTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTypeActionPerformed
        addType();
    }//GEN-LAST:event_btnAddTypeActionPerformed

    private void lstTypesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstTypesListValueChanged
        // to avoid multiple, useless firings
        if (!evt.getValueIsAdjusting()) {
            populateTypeProps();
        }
    }//GEN-LAST:event_lstTypesListValueChanged

    private void cmbTypePropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTypePropActionPerformed
        saveModWord();
    }//GEN-LAST:event_cmbTypePropActionPerformed

    private void cmbGenderPropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbGenderPropActionPerformed
        saveModWord();
    }//GEN-LAST:event_cmbGenderPropActionPerformed

    private void cmbTypeFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTypeFilterActionPerformed
        if (filterListenersActive) {
            filterDict();
        }
    }//GEN-LAST:event_cmbTypeFilterActionPerformed

    private void cmbGenderFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbGenderFilterActionPerformed
        if (filterListenersActive) {
            filterDict();
        }
    }//GEN-LAST:event_cmbGenderFilterActionPerformed

    private void btnDeleteTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteTypeActionPerformed
        deleteType();
    }//GEN-LAST:event_btnDeleteTypeActionPerformed

    private void btnAddGenderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddGenderActionPerformed
        addGender();
    }//GEN-LAST:event_btnAddGenderActionPerformed

    private void btnDeleteGenderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteGenderActionPerformed
        deleteGender();
    }//GEN-LAST:event_btnDeleteGenderActionPerformed

    private void lstGenderListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstGenderListValueChanged
        // to avoid multiple, useless firings
        if (!evt.getValueIsAdjusting()) {
            populateGenderProps();
        }
    }//GEN-LAST:event_lstGenderListValueChanged

    private void jTabbedPane1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTabbedPane1FocusGained
        populateDict();
        popWordProps();
        setLexiconEnabled(true);
    }//GEN-LAST:event_jTabbedPane1FocusGained

    private void mnuPloyHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPloyHelpActionPerformed
        this.openHelp();
    }//GEN-LAST:event_mnuPloyHelpActionPerformed

    private void mnuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAboutActionPerformed
        viewAbout();
    }//GEN-LAST:event_mnuAboutActionPerformed

    private void btnConwordDeclensionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConwordDeclensionsActionPerformed
        viewDeclensions();
    }//GEN-LAST:event_btnConwordDeclensionsActionPerformed

    private void btnDeleteProcGuideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteProcGuideActionPerformed
        deleteProcGuide();
    }//GEN-LAST:event_btnDeleteProcGuideActionPerformed

    private void btnAddProcGuideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProcGuideActionPerformed
        addProcGuide();
    }//GEN-LAST:event_btnAddProcGuideActionPerformed

    private void btnChangeFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeFontActionPerformed
        // Fonts selected via font dialog automatically exist in the system, and do not need a name
        setConFont(fontDialog());
    }//GEN-LAST:event_btnChangeFontActionPerformed

    private void chkAutopopProcsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAutopopProcsActionPerformed
        core.getPropertiesManager().setProAutoPop(chkAutopopProcs.isSelected());
    }//GEN-LAST:event_chkAutopopProcsActionPerformed

    private void mnuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExitActionPerformed
        dispose();
    }//GEN-LAST:event_mnuExitActionPerformed

    private void mnuTranslationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuTranslationActionPerformed
        viewTranslationWindow();
    }//GEN-LAST:event_mnuTranslationActionPerformed

    private void btnUpProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpProcActionPerformed
        moveProcUp();
    }//GEN-LAST:event_btnUpProcActionPerformed

    private void btnDownProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownProcActionPerformed
        moveProcDown();
    }//GEN-LAST:event_btnDownProcActionPerformed

    private void chkPronunciationOverridePropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPronunciationOverridePropActionPerformed
        saveModWord();
    }//GEN-LAST:event_chkPronunciationOverridePropActionPerformed

    private void chkTypeGenderMandatoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTypeGenderMandatoryActionPerformed
        saveType();
    }//GEN-LAST:event_chkTypeGenderMandatoryActionPerformed

    private void chkTypeProcMandatoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTypeProcMandatoryActionPerformed
        saveType();
    }//GEN-LAST:event_chkTypeProcMandatoryActionPerformed

    private void chkTypeDefinitionMandatoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTypeDefinitionMandatoryActionPerformed
        saveType();
    }//GEN-LAST:event_chkTypeDefinitionMandatoryActionPerformed

    private void chkPropTypesMandatoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPropTypesMandatoryActionPerformed
        core.getPropertiesManager().setTypesMandatory(chkPropTypesMandatory.isSelected());
    }//GEN-LAST:event_chkPropTypesMandatoryActionPerformed

    private void chkPropLocalMandatoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPropLocalMandatoryActionPerformed
        core.getPropertiesManager().setLocalMandatory(chkPropLocalMandatory.isSelected());
    }//GEN-LAST:event_chkPropLocalMandatoryActionPerformed

    private void chkPropWordUniquenessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPropWordUniquenessActionPerformed
        core.getPropertiesManager().setWordUniqueness(chkPropWordUniqueness.isSelected());
    }//GEN-LAST:event_chkPropWordUniquenessActionPerformed

    private void chkPropLocalUniquenessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPropLocalUniquenessActionPerformed
        core.getPropertiesManager().setLocalUniqueness(chkPropLocalUniqueness.isSelected());
    }//GEN-LAST:event_chkPropLocalUniquenessActionPerformed

    private void mnuLangStatsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLangStatsActionPerformed
        viewStats();
    }//GEN-LAST:event_mnuLangStatsActionPerformed

    private void btnRecalcProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecalcProcActionPerformed
        recalcAllProcs();
    }//GEN-LAST:event_btnRecalcProcActionPerformed

    private void btnConjDeclActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConjDeclActionPerformed
        viewDeclensionSetup((Integer) scrToCoreTypes.get(lstTypesList.getSelectedIndex()));
    }//GEN-LAST:event_btnConjDeclActionPerformed

    private void mnuCheckForUpdatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCheckForUpdatesActionPerformed
        checkForUpdates(true);
    }//GEN-LAST:event_mnuCheckForUpdatesActionPerformed

    private void mnuExcelExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExcelExportActionPerformed
        exportToExcel();
    }//GEN-LAST:event_mnuExcelExportActionPerformed

    private void mnuThesaurusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuThesaurusActionPerformed
        viewThesaurus();
    }//GEN-LAST:event_mnuThesaurusActionPerformed

    private void chkIgnoreCaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkIgnoreCaseActionPerformed
        core.getPropertiesManager().setIgnoreCase(chkIgnoreCase.isSelected());
    }//GEN-LAST:event_chkIgnoreCaseActionPerformed

    private void chkDisableProcRegexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDisableProcRegexActionPerformed
        core.getPropertiesManager().setDisableProcRegex(chkDisableProcRegex.isSelected());
    }//GEN-LAST:event_chkDisableProcRegexActionPerformed

    private void btnAutoConjDecSetupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAutoConjDecSetupActionPerformed
        viewConjAutoGen((Integer) scrToCoreTypes.get(lstTypesList.getSelectedIndex()));
    }//GEN-LAST:event_btnAutoConjDecSetupActionPerformed

    private void mnuQuickEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuQuickEntryActionPerformed
        viewQuickEntry();
    }//GEN-LAST:event_mnuQuickEntryActionPerformed

    private void chkWordRulesOverrideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkWordRulesOverrideActionPerformed
        saveModWord();
    }//GEN-LAST:event_chkWordRulesOverrideActionPerformed

    private void mnuViewLogographsDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuViewLogographsDetailActionPerformed
        viewLogographDetail();
    }//GEN-LAST:event_mnuViewLogographsDetailActionPerformed

    private void btnWordLogographsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWordLogographsActionPerformed
        viewQuickLogoGraph();
    }//GEN-LAST:event_btnWordLogographsActionPerformed

    private void mnuExportFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExportFontActionPerformed
        exportFont();
    }//GEN-LAST:event_mnuExportFontActionPerformed

    private void mnuGrammarGuideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuGrammarGuideActionPerformed
        viewGrammarDetail();
    }//GEN-LAST:event_mnuGrammarGuideActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        Window window = ScrLexicon.run(core);
        childFrames.add(window);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        Window window = ScrTypes.run(core);
        childFrames.add(window);
    }//GEN-LAST:event_jMenuItem3ActionPerformed
    
    @Override
    public void dispose() {
        // only exit if save/cancel test is passed
        if (!saveOrCancelTest()) {
            return;
        }

        killAllChildren();

        this.setVisible(false);
        super.dispose();
        System.exit(0);
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
            InfoBox.error("Export Problem", e.getLocalizedMessage(), this);
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
                     ScrUpdateAlert.run(verbose, core.getVersion());
                } catch (Exception e) {
                    if (verbose) {
                        InfoBox.error("Update Problem", "Unable to check for update for reason:" 
                                + e.getLocalizedMessage(), parent);
                    }
                }
            }
        };

        check.start();
    }

    /**
     * Gets the currently selected word in your dictionary
     *
     * @return conword object, null if nothing selected
     */
    public ConWord getCurrentWord() {
        ConWord ret;

        try {
            ret = core.getWordCollection().getNodeById((Integer) scrToCoreMap.get(lstDict.getSelectedIndex()));
        } catch (Exception e) {
            ret = null;
        }

        return ret;
    }

    private void recalcAllProcs() {
        try {
            core.getWordCollection().recalcAllProcs();
        } catch (Exception e) {
            InfoBox.error("Recauculation Error", "Unable to recalculate pronunciations: "
                    + e.getLocalizedMessage(), this);
        }

        InfoBox.info("Success", "Pronunciation recalculation successfully completed.", this);
    }

    private void addProcGuide() {
        final int curPosition = tblProcGuide.getSelectedRow();
        
        core.getPronunciationMgr().addAtPosition(curPosition + 1, new PronunciationNode());
        populateProcGuide();

        // perform this action later, once the scroll object is properly updated
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tblProcGuide.getSelectionModel().setSelectionInterval(curPosition + 1, curPosition + 1);
                tblProcGuide.scrollRectToVisible(new Rectangle(tblProcGuide.getCellRect(curPosition + 1, 0, true)));
                tblProcGuide.changeSelection(curPosition + 1, 0, false, false);
            }
        });
    }

    private void addProcGuideWithValues(String base, String proc) {
        boolean populatingLocal = curPopulating;
        curPopulating = true;

        procTableModel.addRow(new Object[]{base, proc});

        // document listener to be fed into editor/renderers for cells...
        DocumentListener docuListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveProcGuide();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveProcGuide();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                saveProcGuide();
            }
        };

        // set saving properties for first column editor
        TableColumnEditor editor = (TableColumnEditor) tblProcGuide.getCellEditor(procTableModel.getRowCount() - 1, 0);
        editor.setDocuListener(docuListener);
        editor.setInitialValue(base);

        // set saving properties for second column editor
        editor = (TableColumnEditor) tblProcGuide.getCellEditor(procTableModel.getRowCount() - 1, 1);
        editor.setDocuListener(docuListener);
        editor.setInitialValue(proc);

        curPopulating = populatingLocal;
    }

    private void deleteProcGuide() {
        Integer curRow = tblProcGuide.getSelectedRow();

        // return if nothing selected
        if (curRow == -1) {
            return;
        }
        
        if (!InfoBox.deletionConfirmation(this)) {
            return;
        }

        PronunciationNode delNode = new PronunciationNode();

        delNode.setValue(tblProcGuide.getValueAt(curRow, 0).toString());
        delNode.setPronunciation(tblProcGuide.getValueAt(curRow, 1).toString());

        core.getPronunciationMgr().deletePronunciation(delNode);
        populateProcGuide();
    }

    private void saveProcGuide() {
        if (curPopulating) {
            return;
        }

        boolean localPopulating = curPopulating;
        curPopulating = true;

        if (tblProcGuide.getCellEditor() != null) {
            tblProcGuide.getCellEditor().stopCellEditing();
        }

        List<PronunciationNode> newPro = new ArrayList<PronunciationNode>();

        for (int i = 0; i < tblProcGuide.getRowCount(); i++) {
            PronunciationNode newNode = new PronunciationNode();
            
            newNode.setValue((String) tblProcGuide.getModel().getValueAt(i, 0));
            newNode.setPronunciation((String) tblProcGuide.getModel().getValueAt(i, 1));

            newPro.add(newNode);
        }

        core.getPronunciationMgr().setPronunciations(newPro);

        curPopulating = localPopulating;
    }

    /**
     * kills all child windows
     */
    private void killAllChildren() {
        Iterator<Window> it = childFrames.iterator();

        while (it.hasNext()) {
            Window curFrame = it.next();

            if (curFrame != null) {
                curFrame.setVisible(false);
                curFrame.dispose();
            }
        }

        childFrames.clear();
    }

    private void populateProcGuide() {
        Iterator<PronunciationNode> popGuide = core.getPronunciationMgr().getPronunciations();

        // wipe current rows, repopulate from core
        setupProcTable();

        while (popGuide.hasNext()) {
            PronunciationNode curNode = popGuide.next();

            addProcGuideWithValues(curNode.getValue(), curNode.getPronunciation());
        }

        tblProcGuide.setModel(procTableModel);
    }

    private void moveProcUp() {
        Integer curRow = tblProcGuide.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getPronunciationMgr().moveProcUp(curRow);

        populateProcGuide();

        if (curRow != 0) {
            tblProcGuide.setRowSelectionInterval(curRow - 1, curRow - 1);
        } else {
            tblProcGuide.setRowSelectionInterval(curRow, curRow);
        }
    }

    private void moveProcDown() {
        Integer curRow = tblProcGuide.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getPronunciationMgr().moveProcDown(curRow);

        populateProcGuide();

        if (curRow != tblProcGuide.getRowCount() - 1) {
            tblProcGuide.setRowSelectionInterval(curRow + 1, curRow + 1);
        } else {
            tblProcGuide.setRowSelectionInterval(curRow, curRow);
        }
    }

    private void generatePronunciation() {
        // return if currently populating or pronunciation overridden
        if (curPopulating || chkPronunciationOverrideProp.isSelected()) {
            return;
        }

        boolean localPopulating = curPopulating;

        curPopulating = true;

        if (chkAutopopProcs.isSelected()) {
            String setText = core.getPronunciationMgr().getPronunciation(txtConWordProp.getText());

            txtPronunciationProp.setText(setText);
        }

        curPopulating = localPopulating;

        // save word to populate core with new pronunciation
        saveModWord();
    }
    
    /**
     * forces refresh of word list
     * @param wordId id of newly created word
     */
    public void refreshWordList(int wordId) {
        populateDict();
        selectListWordById(wordId);
    }
    
    /**
     * ensures that conword with given ID is selected and visible
     * @param wordId ID of word to select
     */
    private void selectListWordById(Integer wordId) {
        Iterator<Entry<Integer, Integer>> it = scrToCoreMap.entrySet().iterator();
        
        while (it.hasNext()) {
            Entry<Integer, Integer> curNode = it.next();
            
            if (curNode.getValue().equals(wordId)) {
                lstDict.setSelectedIndex(curNode.getKey());
                lstDict.ensureIndexIsVisible(curNode.getKey());
                break;
            }
        }
    }

    private void viewDeclensions() {
        Integer wordId = (Integer) scrToCoreMap.get(lstDict.getSelectedIndex());

        if (wordId == -1) {
            return;
        }

        try {
            ScrDeclensions.run(core, core.getWordCollection().getNodeById(wordId),
                    (Integer) scrToCoreTypes.get(cmbTypeProp.getSelectedIndex()), core.getPropertiesManager().getFontCon());
        } catch (Exception ex) {
            InfoBox.error("Delcension Error", ex.getLocalizedMessage(), this);
        }

        saveModWord();
    }

    private void viewAbout() {
        Window window = ScrAbout.run(core);
        childFrames.add(window);
    }

    private void viewTranslationWindow() {
        Window window = ScrTranslationWindow.run(core, this);
        childFrames.add(window);
    }

    private void viewConjAutoGen(int _typeId) {
        Window window = ScrDeclensionGenSetup.run(core, _typeId);
        childFrames.add(window);
    }
    
    private void viewStats() {
        ScrLangStats.run(core);
    }

    private void viewDeclensionSetup(Integer _typeId) {
        Window window = ScrDeclensionSetup.run(core, _typeId);
        childFrames.add(window);
    }

    /**
     * Opens thesaurus window, adds to children windows
     */
    private void viewThesaurus() {
        Window window = ScrThesaurus.run(core, this);
        childFrames.add(window);
    }
    
    private void viewLogographDetail() {
        Window window = ScrLogoDetails.run(core);
        childFrames.add(window);
    }
    
    private void viewQuickLogoGraph() {
        Integer curIndex = lstDict.getSelectedIndex();
        Integer wordId = (Integer) scrToCoreMap.get(curIndex);
        ConWord curWord = new ConWord();

        if (curIndex == -1) {
            return;
        }
        
        try {
            curWord = core.getWordCollection().getNodeById(wordId);
        } catch (Exception e) {
            InfoBox.error("Error", "Unable to open logographs for word: "
                    + (String) lstDict.getSelectedValue() + "\n\n" + e.getMessage(), this);
        }
        
        Window window = new ScrLogoQuickView(core, curWord);
        childFrames.add(window);
        window.setVisible(true);
        final Window parent = this;
        this.setEnabled(false);
        
        window.addWindowListener(new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent arg0) {
                parent.setEnabled(true);
            }
        });
    }
    
    private void viewQuickEntry() {
        Window window = ScrQuickWordEntry.run(core, this);
        childFrames.add(window);
    }
    
    private void viewGrammarDetail() {
        Window window = ScrGrammarGuide.run(core);
        childFrames.add(window);
    }

    private void importExcel() {
        ScrExcelImport.run(core);

        populateDict();
        populateGenders();
        populateTypes();
        popWordProps();
    }

    private void setConFont(Font conFont) {
        lstDict.setFont(conFont);
        txtConWordFilter.setFont(conFont);
        txtConWordProp.setFont(conFont);
        txtAlphaOrder.setFont(conFont);

        core.getPropertiesManager().setFontCon(conFont, conFont.getStyle(), conFont.getSize());
        txtLangFont.setText(conFont.getFontName());

        // set font for first column of pronunciation grid
        TableColumn column = tblProcGuide.getColumnModel().getColumn(0);
        column.setCellEditor(new TableColumnEditor(conFont));
        column.setCellRenderer(new TableColumnRenderer(conFont));
    }

    private Font fontDialog() {
        JFontChooser fontChooser = new JFontChooser();
        Integer result = fontChooser.showDialog(btnChangeFont);
        Font font = null;

        if (result == JFontChooser.OK_OPTION) {
            font = fontChooser.getSelectedFont();
        }

        return font;
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
                InfoBox.info("Help", "This is not yet implemented for OS: " + OS 
                        + ". Please open readme.html in the application directory", this);
            }
        } catch (URISyntaxException e) {
            InfoBox.info("Missing File", "Unable to open readme.html.", this);
        } catch (IOException e) {
            InfoBox.info("Missing File", "Unable to open readme.html.", this);
        }
    }

    private void setupListeners() {
        txtConWordProp.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveModWord();
                generatePronunciation();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveModWord();
                generatePronunciation();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                saveModWord();
                generatePronunciation();
            }
        });
        txtDefProp.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveModWord();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveModWord();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                saveModWord();
            }
        });
        txtLocalWordProp.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveModWord();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveModWord();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                saveModWord();
            }
        });
        txtPronunciationProp.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveModWord();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveModWord();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                saveModWord();
            }
        });
        txtAlphaOrder.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                core.getPropertiesManager().setAlphaOrder(txtAlphaOrder.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                core.getPropertiesManager().setAlphaOrder(txtAlphaOrder.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                core.getPropertiesManager().setAlphaOrder(txtAlphaOrder.getText());
            }
        });
        txtConWordFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }
        });
        txtDefFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }
        });
        txtLocalWordFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }
        });
        txtPronunciationFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                // only filter if filtering is active
                if (filterListenersActive) {
                    filterDict();
                }
            }
        });
        txtTypeName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                checkTypeLexEnabled(true);
                saveType();
                updateTypeListName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkTypeLexEnabled(true);
                saveType();
                updateTypeListName();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkTypeLexEnabled(true);
                saveType();
                updateTypeListName();
            }
        });
        txtTypePattern.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkTypeLexEnabled(false);
                saveType();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                checkTypeLexEnabled(false);
                saveType();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                checkTypeLexEnabled(false);
                saveType();
            }
        });
        txtTypesNotes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveType();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveType();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                saveType();
            }
        });
        txtGenderName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (curPopulating) {
                    return;
                }

                checkGenderLexEnabled();
                saveGender();
                updateGenderListName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (curPopulating) {
                    return;
                }

                checkGenderLexEnabled();
                saveGender();
                updateGenderListName();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (curPopulating) {
                    return;
                }

                checkGenderLexEnabled();
                saveGender();
                updateGenderListName();
            }
        });
        txtGenderNotes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveGender();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveGender();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                saveGender();
            }
        });
        txtLangName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                core.getPropertiesManager().setLangName(txtLangName.getText());
                setTitle(screenTitle + " " + core.getVersion() + ": " + txtLangName.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                core.getPropertiesManager().setLangName(txtLangName.getText());
                setTitle(screenTitle + " " + core.getVersion() + ": " + txtLangName.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                core.getPropertiesManager().setLangName(txtLangName.getText());
                setTitle(screenTitle + " " + core.getVersion() + ": " + txtLangName.getText());
            }
        });
    }

    /**
     * saves file as particular type
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

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return false;
        }

        // if user has not provided an extension, add one
        if (!fileName.contains(".pgd")) {
            fileName += ".pgd";
        }

        File f = new File(fileName);

        if (f.exists()) {
            Integer overWrite = InfoBox.yesNoCancel("Overwrite Dialog",
                    "Overwrite existing file? " + fileName, this);

            if (overWrite == JOptionPane.NO_OPTION) {
                saveFileAs();
            } else if (overWrite == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }

        curFileName = fileName;
        return true;
    }

    /**
     * Gives user option to save file, returns continue/don't continue
     *
     * @return true to signal continue, false to signal stop
     */
    private boolean saveOrCancelTest() {
        // if there's a current dictionary loaded, prompt user to save before creating new
        if (lstDict.getModel().getSize() > 0) {
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
     * Creates totally new file
     * @param performTest whether the UI ask for confirmation
     */
    private void newFile(boolean performTest) {
        if (performTest && !saveOrCancelTest()) {
            return;
        }

        core = new DictCore();
        curFileName = "";

        setLexiconEnabled(true);        
        killAllChildren();
        populateDict();
        popWordProps();
        populateTypes();
        populateTypeProps();
        populateGenders();
        populateGenderProps();
        populateProcGuide();
        popLangProps();        
        setConFont(new JTextField().getFont());
    }

    /**
     * save file, open save as dialog if no file name already
     *
     * @return true if file saved, false otherwise
     */
    private boolean saveFile() {
        if (curFileName.equals("")) {
            saveFileAs();
        }
        
        // if it still is blank, the user has hit cancel on the save as dialog
        if (curFileName.equals("")) {
            return false;
        }

        return doWrite(curFileName);
    }

    /**
     * sends the write command to the core in a new thread
     *
     * @param _fileName path to write to
     * @return returns success
     */
    private boolean doWrite(final String _fileName) {
        final ScrDictInterface parent = this;
        boolean ret;

        parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        final SwingWorker worker = new SwingWorker() {

            //Runs on the event-dispatching thread.
            public void finished() {
                parent.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            protected Object doInBackground() throws Exception {
                try {
                    core.writeFile(_fileName);
                } catch (ParserConfigurationException e) {
                    parent.setSaveError("Unable to save to file: " + curFileName + "\n\n" + e.getMessage());
                } catch (TransformerException e) {
                    parent.setSaveError("Unable to save to file: " + curFileName + "\n\n" + e.getMessage());
                } catch (FileNotFoundException e) {
                    parent.setSaveError("Unable to write file: " + curFileName + "\n\n" + e.getMessage());
                } catch (IOException e) {
                    parent.setSaveError("Problems writing to file: " + curFileName + "\n\n" + e.getMessage());
                }

                parent.setCursor(Cursor.getDefaultCursor());
                return null;
            }
        };

        worker.execute();

        while (!worker.isDone()) {
            // do nothing
        }

        if (!saveError.equals("")) {
            ret = false;
            InfoBox.error("Save Error", saveError, this);
        } else {
            ret = true;
            InfoBox.info("Success", "Dictionary saved to: " + curFileName + ".", parent);
        }

        saveError = "";

        return ret;
    }

    /**
     * Allows workers to return error messages to polyglot
     *
     * @param error The string error to return
     */
    public void setSaveError(String error) {
        saveError = error;
    }

    private void openFile() {
        // only open another if save/cancel test is passed
        if (!saveOrCancelTest()) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Dictionary");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PolyGlot Dictionaries", "pgd", "xml");
        chooser.setFileFilter(filter);
        String fileName;
        chooser.setCurrentDirectory(new File("."));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }

        // TODO: might want to clean up where this is enabled/disabled later...
        btnConwordDeclensions.setEnabled(true);
        btnWordLogographs.setEnabled(true);

        newFile(false); // wipe out all current settings before loading
        setFile(fileName);
    }

    /**
     * Provided for cases where the jave is run from an odd source folder (such
     * as under an app file in OSX)
     * @param override directory for base PolyGlot directory
     */
    private void setOverrideProgramPath(String override) {
        core.getPropertiesManager().setOverrideProgramPath(override);
    }
    
    private void setFile(String fileName) {
        // some wrappers communicate emty files like this
        if (fileName.equals(PGTUtil.emptyFile)) {
            return;
        }
        
        core = new DictCore();
        
        try {
            core.readFile(fileName);
            curFileName = fileName;
        } catch (FontFormatException e) {
            InfoBox.error("Loading Problem", "Problem reading file: " + e.getMessage(), this);
            return;
        } catch (Exception e) {
            core = new DictCore(); // don't allow partial loads
            InfoBox.error("File Read Error", "Could not read file: " + fileName
                    + "\n\n " + e.getMessage(), this);
            //e.printStackTrace();
            return;
        }

        populateDict();
        populateTypes();
        populateGenders();
        popWordProps();
        popLangProps();
        
        // TODO: might want to clean up where this is enabled/disabled later...
        btnConwordDeclensions.setEnabled(true);
        btnWordLogographs.setEnabled(true);
        
        // TODO: Delete this if I find a good way around the problem
        //testPromptUserFont();
    }
    
    /**
     * Tests whether conlang font is installed on current computer. If not,
     * prompts user to export font for install
     */
    /*private void testPromptUserFont() {
        Font testFont = core.getPropertiesManager().getFontCon();
        if (PropertiesManager.testSystemHasFont(testFont)) {
            if (InfoBox.yesNoCancel("Font Export", "The font " + testFont.getName() +
                    " is not installed on this system. This may cause some viewing errors"
                    + " with this dictionary. Export font as file?", this) == JOptionPane.YES_OPTION) {
                exportFont();
            }
        }
    }*/
    
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

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        
        try {
            IOHandler.exportFont(fileName, curFileName);
        } catch (IOException e) {
            InfoBox.error("Export Error", "Unable to export font: " + e.getMessage(), this);
        }
    }

    private void popLangProps() {
        txtLangName.setText(core.getPropertiesManager().getLangName());
        txtAlphaOrder.setText(core.getPropertiesManager().getAlphaPlainText());
        txtLangFont.setText(core.getPropertiesManager().getFontCon().getFontName());
        chkAutopopProcs.setSelected(core.getPropertiesManager().isProAutoPop());
        chkPropLocalMandatory.setSelected(core.getPropertiesManager().isLocalMandatory());
        chkPropLocalUniqueness.setSelected(core.getPropertiesManager().isLocalUniqueness());
        chkPropTypesMandatory.setSelected(core.getPropertiesManager().isTypesMandatory());
        chkPropWordUniqueness.setSelected(core.getPropertiesManager().isWordUniqueness());
        chkIgnoreCase.setSelected(core.getPropertiesManager().isIgnoreCase());
        chkDisableProcRegex.setSelected(core.getPropertiesManager().isDisableProcRegex());

        // only set conlang font if it is not the OS default font
        Font setFont = core.getPropertiesManager().getFontCon();
        if (!(new JTextField()).getFont().equals(setFont)) {
            setConFont(setFont);
        }

        populateProcGuide();
    }

    private void addType() {
        if (lstTypesList.getModel().getSize() != 0
                && scrToCoreTypes.containsKey((Integer) lstTypesList.getSelectedIndex())
                && (Integer) scrToCoreTypes.get((Integer) lstTypesList.getSelectedIndex()) == -1) {
            return;
        }

        curPopulating = true;

        clearTypeProps();

        Integer typeIndex = tListModel.getSize();
        tListModel.add(typeIndex, "NEW TYPE");
        lstTypesList.setSelectedIndex(typeIndex);
        lstTypesList.ensureIndexIsVisible(typeIndex);
        scrToCoreTypes.put(typeIndex, -1);
        curPopulating = false;

        // types always have blank name on creation...
        setEnabledTypeLexicon(false);
        txtTypesErrorBox.setText("Types cannot have blank name.");
    }

    private void clearTypeProps() {
        curPopulating = true;

        txtTypeName.setText("");
        txtTypesNotes.setText("");

        txtTypeName.setEnabled(false);
        txtTypesNotes.setEnabled(false);
        txtTypePattern.setEnabled(false);

        curPopulating = false;
    }

    private void setEnabledTypeProps(boolean enable) {
        txtTypeName.setEnabled(enable);
        txtTypesNotes.setEnabled(enable);
        txtTypePattern.setEnabled(enable);
        chkTypeDefinitionMandatory.setEnabled(enable);
        chkTypeGenderMandatory.setEnabled(enable);
        chkTypeProcMandatory.setEnabled(enable);
        setEnabledConjDeclBtn();
    }

    // only sets conjugation as enabled if there is an active type
    private void setEnabledConjDeclBtn() {
        Integer typeId = scrToCoreTypes.containsKey(lstTypesList.getSelectedIndex())
                ? (Integer) scrToCoreTypes.get(lstTypesList.getSelectedIndex()) : -1;

        if (typeId != -1) {
            btnConjDecl.setEnabled(true);
            btnAutoConjDecSetup.setEnabled(true);
        } else {
            btnConjDecl.setEnabled(false);
            btnAutoConjDecSetup.setEnabled(false);
        }
    }

    private void populateTypeProps() {
        //avoid recursive population
        if (curPopulating) {
            return;
        }

        testTypePattern();
        
        curPopulating = true;
        TypeNode curType = new TypeNode();
        Integer typeIndex = lstTypesList.getSelectedIndex();
        Integer typeId = (Integer) scrToCoreTypes.get(typeIndex);

        if (typeId != null && typeId != -1) {
            try {
                curType = core.getTypes().getNodeById(typeId);
            } catch (Exception e) {
                InfoBox.error("Type Error", "Unable to load word types. Reload dictionary, if possible. \n" + e.getMessage(), this);
            }
        }

        // set type props enabled only of a type is selected
        setEnabledTypeProps(typeIndex != -1);

        // prevent self setting (from user mod)
        if (!txtTypeName.getText().trim().equals(curType.getValue().trim())) {
            txtTypeName.setText(curType.getValue());
        }
        if (!txtTypesNotes.getText().equals(curType.getNotes())) {
            txtTypesNotes.setText(curType.getNotes());
        }
        if (!txtTypePattern.getText().trim().equals(curType.getPattern())) {
            txtTypePattern.setText((curType.getPattern()));
        }
        
        chkTypeDefinitionMandatory.setSelected(curType.isDefMandatory());
        chkTypeGenderMandatory.setSelected(curType.isGenderMandatory());
        chkTypeProcMandatory.setSelected(curType.isProcMandatory());

        curPopulating = false;
    }
    
    /**
     * tests/sets the color of the type pattern box as is appropriate
     */
    public void testTypePattern() {
        String regex = txtTypePattern.getText();
        
        if (regex.equals("") || RegexTools.testRegex(regex)) {
            txtTypePattern.setBackground(new JTextField().getBackground());
        } else {
            txtTypePattern.setBackground(core.getRequiredColor());
        }
    }

    private void updateTypeListName() {
        Integer typeIndex = lstTypesList.getSelectedIndex();

        if (typeIndex == -1 || curPopulating) {
            return;
        }

        curPopulating = true;

        tListModel.remove(typeIndex);
        tListModel.add(typeIndex, txtTypeName.getText().trim());

        lstTypesList.setSelectedIndex(typeIndex);
        lstTypesList.ensureIndexIsVisible(typeIndex);

        curPopulating = false;
    }

    private void deleteType() {
        Integer curIndex = lstTypesList.getSelectedIndex();

        if (curIndex == -1) {
            return;
        }
        
        if (!InfoBox.deletionConfirmation(this)) {
            return;
        }

        // avoid attempt to delete unsaved types
        if ((Integer) scrToCoreTypes.get(curIndex) != -1) {
            try {
                core.getTypes().deleteNodeById((Integer) scrToCoreTypes.get(curIndex));
            } catch (Exception e) {
                InfoBox.error("Type Deletion Error", "Unable to delete type: "
                        + (String) lstTypesList.getSelectedValue() + "\n\n" + e.getMessage(), this);
            }
        }

        // clears type block if user deleted illgal type
        lstTypesList.setEnabled(true);
        btnAddType.setEnabled(true);
        txtTypesErrorBox.setText("");

        if (curIndex > 0) {
            curIndex--;
        }

        populateTypes();

        lstTypesList.setSelectedIndex(curIndex);
        lstTypesList.ensureIndexIsVisible(curIndex);
        populateTypeProps();
    }

    private void saveType() {
        Integer typeIndex = lstTypesList.getSelectedIndex();

        if (curPopulating) {
            return;
        }

        if (typeIndex == -1) {
            return;
        }
        
        testTypePattern();

        boolean localPopulating = curPopulating;

        curPopulating = true;

        Integer typeId = scrToCoreTypes.containsKey(typeIndex)
                ? (Integer) scrToCoreTypes.get(typeIndex) : -1;

        TypeNode type = new TypeNode();

        type.setValue(txtTypeName.getText().trim());
        type.setNotes(txtTypesNotes.getText());
        type.setPattern(txtTypePattern.getText().trim());
        type.setDefMandatory(chkTypeDefinitionMandatory.isSelected());
        type.setGenderMandatory(chkTypeGenderMandatory.isSelected());
        type.setProcMandatory(chkTypeProcMandatory.isSelected());

        try {
            // split logic for creating, rather than modifying types
            if (typeId == -1) {
                typeId = core.getTypes().addNode(type);
                scrToCoreTypes.put(typeIndex, typeId);
                scrTypeMap.put(type.getValue(), typeIndex);
            } else {
                core.getTypes().modifyNode(typeId, type);
            }
        } catch (Exception e) {
            InfoBox.error("Type Population Error", "Error populating types. Please reload dictionary. \n\n"
                    + e.getMessage(), this);
        }

        curPopulating = localPopulating;

        populateTypes();
        setEnabledConjDeclBtn();
    }

    /**
     * Checks/sets whether the type lexicon should be and is enabled
     * @param setName set this to true if the type's name has been changed
     */
    private void checkTypeLexEnabled(boolean setName) {
        if (curPopulating) {
            return;
        }

        // do not allow duplicate types
        if (setName && core.getTypes().findTypeByName(txtTypeName.getText()) != null) {
            setEnabledTypeLexicon(false);
            txtTypesErrorBox.setText("Types must have unique names.");
        } else if (txtTypeName.getText().trim().equals("")) {
            setEnabledTypeLexicon(false);
            txtTypesErrorBox.setText("Types cannot have blank name.");
        } else if (!RegexTools.testRegex(txtTypePattern.getText())) { 
            setEnabledTypeLexicon(false);
            txtTypesErrorBox.setText("Illegal regex in type pattern.");
        } else {
            setEnabledTypeLexicon(true);
            txtTypesErrorBox.setText("");
        }
    }

    private void setEnabledTypeLexicon(boolean enabled) {
        lstTypesList.setEnabled(enabled);
        btnAddType.setEnabled(enabled);

        if (enabled) {
            txtTypesErrorBox.setText("");
        }
    }

    private void addGender() {
        if (lstGenderList.getModel().getSize() != 0
                && scrToCoreGenders.containsKey((Integer) lstGenderList.getSelectedIndex())
                && (Integer) scrToCoreGenders.get((Integer) lstGenderList.getSelectedIndex()) == -1) {
            return;
        }

        curPopulating = true;

        clearGenderProps();

        Integer genderIndex = gListModel.getSize();
        gListModel.add(genderIndex, "NEW GENDER");
        lstGenderList.setSelectedIndex(genderIndex);
        lstGenderList.ensureIndexIsVisible(genderIndex);
        scrToCoreGenders.put(genderIndex, -1);
        curPopulating = false;

        // genders are always created with a blank name
        setEnabledGenderLexicon(false);
        txtGendersErrorBox.setText("Genders cannot have blank names.");
    }

    private void clearGenderProps() {
        curPopulating = true;

        txtGenderName.setText("");
        txtGenderNotes.setText("");

        curPopulating = false;
    }

    private void populateGenderProps() {
        //avoid recursive population
        if (curPopulating) {
            return;
        }

        curPopulating = true;
        GenderNode curGender = new GenderNode();
        Integer genderIndex = lstGenderList.getSelectedIndex();
        Integer genderId = (Integer) scrToCoreGenders.get(genderIndex);

        if (genderIndex == -1) {
            txtGenderName.setEnabled(false);
            txtGenderNotes.setEnabled(false);
        } else {
            txtGenderName.setEnabled(true);
            txtGenderNotes.setEnabled(true);
        }

        if (genderId != null && genderId != -1) {
            try {
                curGender = core.getGenders().getNodeById(genderId);
            } catch (Exception e) {
                InfoBox.error("Gender Population Error", "Unable to populate genders. Please reload dictionary.\n\n"
                        + e.getMessage(), this);
            }
        }

        // prevent self setting (from user mod)
        if (!txtGenderName.getText().trim().equals(curGender.getValue().trim())) {
            txtGenderName.setText(curGender.getValue());
        }
        if (!txtGenderNotes.getText().equals(curGender.getNotes())) {
            txtGenderNotes.setText(curGender.getNotes());
        }

        curPopulating = false;
    }

    private void updateGenderListName() {
        Integer genderIndex = lstGenderList.getSelectedIndex();

        if (genderIndex == -1 || curPopulating) {
            return;
        }

        curPopulating = true;

        gListModel.remove(genderIndex);
        gListModel.add(genderIndex, txtGenderName.getText().trim());

        lstGenderList.setSelectedIndex(genderIndex);
        lstGenderList.ensureIndexIsVisible(genderIndex);

        curPopulating = false;
    }

    private void deleteGender() {
        Integer curIndex = lstGenderList.getSelectedIndex();

        if (curIndex == -1) {
            return;
        }
        
        if (!InfoBox.deletionConfirmation(this)) {
            return;
        }

        // don't try to delete new genders, just populate to eliminate them.
        if ((Integer) scrToCoreGenders.get(curIndex) != -1) {
            try {
                core.getGenders().deleteNodeById((Integer) scrToCoreGenders.get(curIndex));
            } catch (Exception e) {
                InfoBox.error("Gender Deletion Error", "Unable to delete gender: "
                        + (String) lstGenderList.getSelectedValue() + "\n\n" + e.getMessage(), this);
            }

            if (curIndex > 0) {
                curIndex--;
            }
        }

        populateGenders();
        populateGenderProps();

        lstGenderList.setSelectedIndex(curIndex);
        lstGenderList.ensureIndexIsVisible(curIndex);

        // reenables gender selection/creation in case user deleted illegal gender
        lstGenderList.setEnabled(true);
        btnAddGender.setEnabled(true);
        txtGendersErrorBox.setText("");
    }

    private void saveGender() {
        Integer genderIndex = lstGenderList.getSelectedIndex();

        if (curPopulating) {
            return;
        }

        if (genderIndex == -1) {
            return;
        }

        curPopulating = true;

        Integer genderId = scrToCoreGenders.containsKey(genderIndex)
                ? (Integer) scrToCoreGenders.get(genderIndex) : -1;

        GenderNode gender = new GenderNode();

        gender.setValue(txtGenderName.getText());
        gender.setNotes(txtGenderNotes.getText());

        try {
            // split logic for creating, rather than modifying Gender
            if (genderId == -1) {
                genderId = core.getGenders().addNode(gender);
                scrToCoreGenders.put(genderIndex, genderId);
                scrGenderMap.put(gender.getValue(), genderIndex);
            } else {
                core.getGenders().modifyNode(genderId, gender);
            }
        } catch (Exception e) {
            InfoBox.error("Gender Creation Error", "Unable to create gender "
                    + gender.getValue() + "\n\n" + e.getMessage(), this);
        }

        curPopulating = false;

        populateGenders();
    }

    private void checkGenderLexEnabled() {
        GenderNode genNode = core.getGenders().findGenderByName(txtGenderName.getText().trim());

        // do not allow duplicate types
        if (genNode != null) {
            setEnabledGenderLexicon(false);
            txtGendersErrorBox.setText("Genders must have unique names.");
        } else if (txtGenderName.getText().trim().equals("")) {
            setEnabledGenderLexicon(false);
            txtGendersErrorBox.setText("Genders cannot have blank names.");
        } else {
            setEnabledGenderLexicon(true);
            txtGendersErrorBox.setText("");
        }
    }

    private void setEnabledGenderLexicon(boolean enabled) {
        lstGenderList.setEnabled(enabled);
        btnAddGender.setEnabled(enabled);

        if (enabled) {
            txtGendersErrorBox.setText("");
        }
    }

    private void deleteWord() {
        Integer selectedIndex = lstDict.getSelectedIndex();

        if (selectedIndex == -1) {
            return;
        }
        
        if (!InfoBox.deletionConfirmation(this)) {
            return;
        }

        Integer wordId = (Integer) scrToCoreMap.get(selectedIndex);

        // only delete if new word. Otherwise, simply repopulate dictionary.
        if (wordId != -1) {
            try {
                core.deleteWordById(wordId);
            } catch (Exception e) {
                InfoBox.error("Word Deletion Error", "Unable to delete word: "
                        + (String) lstDict.getSelectedValue() + "\n\n" + e.getMessage(), this);
            }
        }

        populateDict();
        
        // disable conjugation button if no words left
        if (lstDict.getModel().getSize() == 0) {
            btnConwordDeclensions.setEnabled(false);
            btnWordLogographs.setEnabled(false);
        }

        // select the next lowest word (if it exists)
        if (lstDict.getModel().getSize() > 1) {
            lstDict.setSelectedIndex(selectedIndex - 1);
            lstDict.ensureIndexIsVisible(selectedIndex - 1);
            popWordProps();
        } else if (lstDict.getModel().getSize() == 1) {
            lstDict.setSelectedIndex(0);
            popWordProps();
        } else {
            lstDict.setSelectedIndex(-1);
            clearProps();
        }

        // after word deletion, lexicon should always be enabled
        setLexiconEnabled(true);
    }

    private void populateDict() {
        curPopulating = true;
        Iterator<ConWord> itWords = core.getWordCollection().getNodeIterator();

        this.populateWordsFromList(itWords);
        curPopulating = false;
    }

    private void populateWordsFromList(Iterator<ConWord> _itWords) {
        ConWord curWord;
        Integer curId = (Integer) scrToCoreMap.get(lstDict.getSelectedIndex());
        Integer newIndex = -1;

        lstDict.setVisible(false);

        dListModel.clear();

        scrToCoreMap = new HashMap<Integer, Integer>();

        for (int i = 0; _itWords.hasNext(); i++) {
            curWord = _itWords.next();

            if (curWord.getId().equals(curId)) {
                newIndex = i;
            }

            dListModel.add(i, curWord.getValue());

            scrToCoreMap.put(i, curWord.getId());
        }

        lstDict.setSelectedIndex(newIndex);
        lstDict.ensureIndexIsVisible(newIndex);
        lstDict.setVisible(true);
    }

    private void populateTypes() {
        // avoid recursive population
        if (curPopulating) {
            return;
        }

        Integer curTypeId = (Integer) scrToCoreTypes.get(lstTypesList.getSelectedIndex());
        Integer setIndex = -1;

        curPopulating = true;
        Iterator<TypeNode> typeIt = core.getTypes().getNodeIterator();
        TypeNode curType;

        // relevant objects should be rebuilt
        scrTypeMap = new HashMap<String, Integer>();
        scrToCoreTypes = new HashMap<Integer, Integer>();

        tListModel.clear();

        cmbTypeProp.removeAllItems();
        cmbTypeFilter.removeAllItems();

        for (int i = 0; typeIt.hasNext(); i++) {
            curType = typeIt.next();

            cmbTypeProp.insertItemAt(curType.getValue(), i);
            cmbTypeFilter.insertItemAt(curType.getValue(), i);

            scrTypeMap.put(curType.getValue(), i);

            tListModel.add(i, curType.getValue());
            scrToCoreTypes.put(i, curType.getId());

            // replaced call to Object type here
            if (curTypeId != null
                    && curTypeId.equals(curType.getId())) {
                setIndex = i;
            }
        }

        cmbTypeFilter.insertItemAt("", cmbTypeFilter.getModel().getSize());
        cmbTypeProp.insertItemAt("", cmbTypeProp.getModel().getSize());

        lstTypesList.setSelectedIndex(setIndex);
        lstTypesList.ensureIndexIsVisible(setIndex);

        curPopulating = false;
    }

    private void populateGenders() {
        // avoid recursive population
        if (curPopulating) {
            return;
        }

        Integer curGenderId = (Integer) scrToCoreGenders.get(lstGenderList.getSelectedIndex());
        Integer setIndex = -1;

        curPopulating = true;
        Iterator<GenderNode> genderIt = core.getGenders().getNodeIterator();
        GenderNode curGender;

        // relevant objects should be rebuilt
        scrGenderMap = new HashMap<String, Integer>();
        scrToCoreGenders = new HashMap<Integer, Integer>();

        gListModel.clear();

        cmbGenderProp.removeAllItems();
        cmbGenderFilter.removeAllItems();

        for (int i = 0; genderIt.hasNext(); i++) {
            curGender = genderIt.next();

            cmbGenderProp.insertItemAt(curGender.getValue(), i);
            cmbGenderFilter.insertItemAt(curGender.getValue(), i);

            scrGenderMap.put(curGender.getValue(), i);

            gListModel.add(i, curGender.getValue());
            scrToCoreGenders.put(i, curGender.getId());

            // replaced calle to Object type here
            if (curGenderId != null
                    && curGenderId.equals(curGender.getId())) {
                setIndex = i;
            }
        }

        cmbGenderFilter.insertItemAt("", cmbGenderFilter.getModel().getSize());

        lstGenderList.setSelectedIndex(setIndex);
        lstGenderList.ensureIndexIsVisible(setIndex);

        curPopulating = false;
    }

    private void clearProps() {
        curPopulating = true;
        ConWord curWord = new ConWord();

        txtConWordProp.setText(curWord.getValue());
        txtLocalWordProp.setText(curWord.getLocalWord());
        txtDefProp.setText(curWord.getDefinition());
        txtPronunciationProp.setText(curWord.getPronunciation());
        chkPronunciationOverrideProp.setSelected(false);
        cmbTypeProp.setSelectedIndex(-1);
        cmbGenderProp.setSelectedIndex(-1);

        txtConWordProp.setEnabled(false);
        txtLocalWordProp.setEnabled(false);
        txtDefProp.setEnabled(false);
        txtPronunciationProp.setEnabled(false);
        cmbTypeProp.setEnabled(false);
        cmbGenderProp.setEnabled(false);
        chkPronunciationOverrideProp.setEnabled(false);
        chkWordRulesOverride.setEnabled(false);

        curPopulating = false;
    }

    /**
     * Populates properties of currently selected word, checks legality
     */
    private void popWordProps() {
        popWordProps(true);
    }
    
    /**
     * Populates properties of currently selected word
     * @param disableIllegal whether to disable controls if illegal word
     */
    private void popWordProps(boolean disableIllegal) {
        // if currently populating, abandon recursive process
        if (curPopulating) {
            return;
        }

        Integer curIndex = lstDict.getSelectedIndex();
        Integer wordId = (Integer) scrToCoreMap.get(curIndex);
        ConWord curWord = new ConWord();

        if (lstDict.getSelectedIndex() == -1) {
            clearProps();
            return;
        }

        // split to ensure that word may still be modified
        if (wordId == null || wordId == -1) {
            clearProps();
        }

        txtConWordProp.setEnabled(true);
        txtLocalWordProp.setEnabled(true);
        txtDefProp.setEnabled(true);
        txtPronunciationProp.setEnabled(true);
        cmbTypeProp.setEnabled(true);
        cmbGenderProp.setEnabled(true);
        chkPronunciationOverrideProp.setEnabled(true);
        chkWordRulesOverride.setEnabled(true);

        // split to ensure that word may still be modified
        if (wordId == null || wordId == -1) {
            return;
        }

        try {
            curWord = core.getWordCollection().getNodeById(wordId);
        } catch (Exception e) {
            InfoBox.error("Property Population Error", "Unable to populate properties of word: "
                    + (String) lstDict.getSelectedValue() + "\n\n" + e.getMessage(), this);
        }

        curPopulating = true;

        txtConWordProp.setText(curWord.getValue());
        txtLocalWordProp.setText(curWord.getLocalWord());
        txtDefProp.setText(curWord.getDefinition());
        txtPronunciationProp.setText(curWord.getPronunciation());
        chkPronunciationOverrideProp.setSelected(curWord.isProcOverride());
        chkWordRulesOverride.setSelected(curWord.isRulesOverrride());

        if (scrTypeMap.containsKey(curWord.getWordType())) {
            cmbTypeProp.setSelectedIndex((Integer) scrTypeMap.get(curWord.getWordType()));
        } else {
            cmbTypeProp.setSelectedIndex(-1);
        }
        // forces combobox display to refresh...
        cmbTypeProp.setVisible(false);
        cmbTypeProp.setVisible(true);

        if (scrGenderMap.containsKey(curWord.getGender())) {
            cmbGenderProp.setSelectedIndex((Integer) scrGenderMap.get(curWord.getGender()));

        } else {
            cmbGenderProp.setSelectedIndex(-1);
        }

        // forces combobox display to refresh. This is sloppy, but it works...
        cmbGenderProp.setVisible(false);
        cmbGenderProp.setVisible(true);

        // make sure scroll is set to top for definition
        txtDefProp.setCaretPosition(0);

        curPopulating = false;
        
        setWordLegality(curWord, disableIllegal);
    }

    private void filterDict() {
        if (curPopulating) {
            return;
        }

        // no filter necessary if all blank
        if (txtConWordFilter.getText().equals("")
                && txtDefFilter.getText().equals("")
                && txtLocalWordFilter.getText().equals("")
                && txtPronunciationFilter.getText().equals("")
                && cmbGenderFilter.getSelectedIndex() == -1
                && cmbTypeFilter.getSelectedIndex() == -1) {
            populateDict();
            lstDict.setSelectedIndex(lstDict.getModel().getSize() > 0 ? 0 : -1);
            if (lstDict.getModel().getSize() > 0) {
                lstDict.ensureIndexIsVisible(0);
            }
            return;
        }

        ConWord filter = new ConWord();

        filter.setValue(txtConWordFilter.getText() != null ? txtConWordFilter.getText().trim() : "");
        filter.setDefinition(txtDefFilter.getText() != null ? txtDefFilter.getText().trim() : "");
        filter.setLocalWord(txtLocalWordFilter.getText() != null ? txtLocalWordFilter.getText().trim() : "");
        filter.setWordType(cmbTypeFilter.getSelectedItem() != null ? (String) cmbTypeFilter.getSelectedItem() : "");
        filter.setGender(cmbGenderFilter.getSelectedItem() != null ? (String) cmbGenderFilter.getSelectedItem() : "");
        filter.setPronunciation(txtPronunciationFilter.getText() != null ? txtPronunciationFilter.getText().trim() : "");

        try {
            populateWordsFromList(core.getWordCollection().filteredList(filter));
        } catch (Exception e) {
            InfoBox.error("Filter Error", "Unable to apply filter.\n\n" + e.getMessage(), this);
        }

        if (lstDict.getModel().getSize() > 0) {
            lstDict.setSelectedIndex(0);
            lstDict.ensureIndexIsVisible(0);
        } else {
            lstDict.setSelectedIndex(-1);
        }

        popWordProps(false);
    }

    private void newWord() {
        // Do not create any new words if the current word is new
        if (lstDict.getModel().getSize() != 0
                && scrToCoreMap.containsKey((Integer) lstDict.getSelectedIndex())
                && (Integer) scrToCoreMap.get((Integer) lstDict.getSelectedIndex()) == -1) {
            return;
        }

        Integer curIndex = dListModel.getSize();

        dListModel.add(curIndex, "NEW WORD");

        // new words mapped to -1 until inserted into dictionary
        scrToCoreMap.put(curIndex, -1);

        lstDict.setSelectedIndex(curIndex);
        lstDict.ensureIndexIsVisible(curIndex);

        clearProps();

        txtConWordProp.setEnabled(true);
        txtLocalWordProp.setEnabled(true);
        txtDefProp.setEnabled(true);
        txtPronunciationProp.setEnabled(true);
        cmbTypeProp.setEnabled(true);
        cmbGenderProp.setEnabled(true);
        chkPronunciationOverrideProp.setEnabled(true);
        chkWordRulesOverride.setEnabled(true);
        btnConwordDeclensions.setEnabled(true);
        btnWordLogographs.setEnabled(true);

        setWordLegality(new ConWord());
    }

    private void saveModWord() {
        // if currently populating, abandon recursive process
        if (curPopulating) {
            return;
        }

        // if word has been modified, clear filters
        clearLexFilterValues();

        curPopulating = true;
        ConWord saveWord = new ConWord();
        Integer scrIndex = lstDict.getSelectedIndex();

        if (scrIndex == -1) {
            curPopulating = false;
            return;
        }

        // populate word object to save
        Integer wordId = (Integer) scrToCoreMap.get(scrIndex);

        // if word already exists, prepopulate with default values to cover non-displayed elements
        if (core.getWordCollection().exists(wordId))
        {
            try {
                saveWord.setEqual(core.getWordCollection().getNodeById(wordId));
            } catch (Exception e){/*do nothing*/}
        }
        
        saveWord.setValue(txtConWordProp.getText());
        saveWord.setLocalWord(txtLocalWordProp.getText());
        saveWord.setWordType(scrTypeMap.containsKey(cmbTypeProp.getSelectedItem())
                ? (String) cmbTypeProp.getSelectedItem() : "");
        saveWord.setDefinition(txtDefProp.getText());
        saveWord.setPronunciation(txtPronunciationProp.getText());
        saveWord.setGender(scrGenderMap.containsKey(cmbGenderProp.getSelectedItem())
                ? (String) cmbGenderProp.getSelectedItem() : "");
        saveWord.setProcOverride(chkPronunciationOverrideProp.isSelected());
        saveWord.setRulesOverride(chkWordRulesOverride.isSelected());

        if (wordId == -1) {
            try {
                wordId = core.getWordCollection().addWord(saveWord);
            } catch (Exception e) {
                InfoBox.error("Word Creation Error", "Unable to create word:"
                        + saveWord.getValue() + "\n\n" + e.getMessage(), this);
            }
        } else {
            try {
                core.getWordCollection().modifyNode(wordId, saveWord);
            } catch (Exception e) {
                InfoBox.error("Word Save Error", "Unable to save word:"
                        + saveWord.getValue() + "\n\n" + e.getMessage(), this);
            }
        }

        dListModel.remove(scrIndex);
        dListModel.add(scrIndex, saveWord.getValue());
        setLexPosition(scrIndex);

        // update word map
        scrToCoreMap.remove(scrIndex);
        scrToCoreMap.put(scrIndex, wordId);

        curPopulating = false;

        setWordLegality(saveWord);

        populateDict();
    }
    
    /**
     * Sets lexicon tab's currently displayed word legality (highlighted fields,
     * error message, etc.)
     * @param results current word
     */
    private void setWordLegality(ConWord testWord) {
        setWordLegality(testWord, true);
    }
    
    /**
     * Sets lexicon tab's currently displayed word legality (highlighted fields,
     * error message, etc.)
     * @param results current word
     * @param disableElements whether to disable control elements on fail
     */
    private void setWordLegality(ConWord testWord, boolean disableElements) {
        ConWord results = core.isWordLegal(testWord);
        Color bColor = new JTextField().getBackground();
        Color hColor = core.getRequiredColor();
        boolean isLegal = true;
        
        txtWordErrorBox.setText("");
                
        if (!results.getValue().equals("") || results.isProcOverride()) {
            if (!results.getValue().equals("")) {
                txtWordErrorBox.setText(txtWordErrorBox.getText() 
                        + (txtWordErrorBox.getText().equals("")?"":"\n") + results.getValue());
            }
            
            txtConWordProp.setBackground(hColor);
            isLegal = false;
        } else {
            txtConWordProp.setBackground(bColor);
        }
        
        if (!results.getWordType().equals("")) {
            txtWordErrorBox.setText(txtWordErrorBox.getText() 
                    + (txtWordErrorBox.getText().equals("")?"":"\n") + results.getWordType());
            PGTools.flashComponent(cmbTypeProp, hColor, false);
            isLegal = false;
        } else {
            cmbTypeProp.setForeground(bColor);
        }
        
        if (!results.getLocalWord().equals("")) {
            txtWordErrorBox.setText(txtWordErrorBox.getText() 
                    + (txtWordErrorBox.getText().equals("")?"":"\n") + results.getLocalWord());
            txtLocalWordProp.setBackground(hColor);
            isLegal = false;
        } else {
            txtLocalWordProp.setBackground(bColor);
        }
        
        if (!results.getDefinition().equals("")) {
            txtWordErrorBox.setText(txtWordErrorBox.getText() 
                    + (txtWordErrorBox.getText().equals("")?"":"\n") + results.getDefinition());
            isLegal = false;
        }
        
        if (disableElements) {
            setLexiconEnabled(isLegal || testWord.isRulesOverrride());
        }
    }

    private void setLexiconEnabled(boolean isEnabled) {
        setFilterEnabled(isEnabled);
        lstDict.setEnabled(isEnabled);
        btnAdd.setEnabled(isEnabled);

        if (isEnabled) {
            txtWordErrorBox.setText("");
        }
    }

    private void setFilterEnabled(boolean enabled) {
        txtConWordFilter.setEnabled(enabled);
        txtDefFilter.setEnabled(enabled);
        txtLocalWordFilter.setEnabled(enabled);
        txtPronunciationFilter.setEnabled(enabled);
        cmbGenderFilter.setEnabled(enabled);
        cmbTypeFilter.setEnabled(enabled);
    }

    /**
     * sets index of lexicon list and positions scroller appropriately
     *
     * @param scrIndex
     */
    private void setLexPosition(Integer scrIndex) {
        lstDict.setSelectedIndex(scrIndex);
        lstDict.ensureIndexIsVisible(scrIndex);
    }

    /**
     * Selects word in lexicon by ID, pulls ScrDictInterface to the fore,
     * selects Lexicon tab, removes all filters
     *
     * @param id the ID of the word to select
     */
    public void selectWordById(Integer id) {
        Integer index = -1;

        clearLexFilter();

        for (Entry<Integer, Integer> entry : (Set<Entry<Integer, Integer>>) scrToCoreMap.entrySet()) {
            if (entry.getValue().equals(id)) {
                index = entry.getKey();
            }
        }

        // if no match, inform user, then do nothing.
        if (index == -1) {
            InfoBox.error("Word Not Found", "word with ID: " + id.toString() + " not found.", this);
            return;
        }

        setLexPosition(index);
        jTabbedPane1.setSelectedIndex(0);
    }

    /**
     * clears all filters set on lexicon
     */
    private void clearLexFilter() {
        clearLexFilterValues();

        filterDict();
    }

    /**
     * Clears values of LexFilter without re-filtering
     */
    private void clearLexFilterValues() {
        filterListenersActive = false;

        txtConWordFilter.setText("");
        txtLocalWordFilter.setText("");
        txtPronunciationFilter.setText("");
        txtDefFilter.setText("");
        cmbGenderFilter.setSelectedIndex(cmbGenderFilter.getModel().getSize() - 1);
        cmbTypeFilter.setSelectedIndex(cmbTypeFilter.getModel().getSize() - 1);

        filterListenersActive = true;
    }

    /**
     * Creates new word by a local word, leaving other fields blank
     *
     * @param newLocal new local word value
     */
    public void createNewWordByLocal(String newLocal) {
        // do not allow blank creation
        if (newLocal.equals("")) {
            return;
        }

        newWord();
        txtLocalWordProp.setText(newLocal);
        this.requestFocus();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAddGender;
    private javax.swing.JButton btnAddProcGuide;
    private javax.swing.JButton btnAddType;
    private javax.swing.JButton btnAutoConjDecSetup;
    private javax.swing.JButton btnChangeFont;
    private javax.swing.JButton btnConjDecl;
    private javax.swing.JButton btnConwordDeclensions;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDeleteGender;
    private javax.swing.JButton btnDeleteProcGuide;
    private javax.swing.JButton btnDeleteType;
    private javax.swing.JButton btnDownProc;
    private javax.swing.JButton btnRecalcProc;
    private javax.swing.JButton btnUpProc;
    private javax.swing.JButton btnWordLogographs;
    private javax.swing.JCheckBox chkAutopopProcs;
    private javax.swing.JCheckBox chkDisableProcRegex;
    private javax.swing.JCheckBox chkIgnoreCase;
    private javax.swing.JCheckBox chkPronunciationOverrideProp;
    private javax.swing.JCheckBox chkPropLocalMandatory;
    private javax.swing.JCheckBox chkPropLocalUniqueness;
    private javax.swing.JCheckBox chkPropTypesMandatory;
    private javax.swing.JCheckBox chkPropWordUniqueness;
    private javax.swing.JCheckBox chkTypeDefinitionMandatory;
    private javax.swing.JCheckBox chkTypeGenderMandatory;
    private javax.swing.JCheckBox chkTypeProcMandatory;
    private javax.swing.JCheckBox chkWordRulesOverride;
    private javax.swing.JComboBox cmbGenderFilter;
    private javax.swing.JComboBox cmbGenderProp;
    private javax.swing.JComboBox cmbTypeFilter;
    private javax.swing.JComboBox cmbTypeProp;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblConWordFilter;
    private javax.swing.JLabel lblConWordProp;
    private javax.swing.JLabel lblDefFilter;
    private javax.swing.JLabel lblDefinitionProp;
    private javax.swing.JLabel lblGenderFilter;
    private javax.swing.JLabel lblGenderProp;
    private javax.swing.JLabel lblLocalWordFilter;
    private javax.swing.JLabel lblLocalWordProp;
    private javax.swing.JLabel lblPrononciationProp;
    private javax.swing.JLabel lblPronunciationFilter;
    private javax.swing.JLabel lblTypeFilter;
    private javax.swing.JLabel lblTypeProp;
    private javax.swing.JList lstDict;
    private javax.swing.JList lstGenderList;
    private javax.swing.JList lstTypesList;
    private javax.swing.JMenuItem mnuAbout;
    private javax.swing.JMenuItem mnuCheckForUpdates;
    private javax.swing.JMenuItem mnuExcelExport;
    private javax.swing.JMenuItem mnuExit;
    private javax.swing.JMenuItem mnuExportFont;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenuItem mnuGrammarGuide;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mnuImportExcel;
    private javax.swing.JMenuItem mnuLangStats;
    private javax.swing.JMenuItem mnuNew;
    private javax.swing.JMenuItem mnuOpen;
    private javax.swing.JMenuItem mnuPloyHelp;
    private javax.swing.JMenuItem mnuQuickEntry;
    private javax.swing.JMenuItem mnuSave;
    private javax.swing.JMenuItem mnuSaveAs;
    private javax.swing.JMenuItem mnuThesaurus;
    private javax.swing.JMenu mnuTools;
    private javax.swing.JMenuItem mnuTranslation;
    private javax.swing.JMenu mnuView;
    private javax.swing.JMenuItem mnuViewLogographsDetail;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JPanel pnlProperties;
    private javax.swing.JScrollPane sclDefProp;
    private javax.swing.JScrollPane sclGenderList;
    private javax.swing.JScrollPane sclProcGuide;
    private javax.swing.JScrollPane sclTypesList;
    private javax.swing.JScrollPane scrlDict;
    private javax.swing.JPanel tabDict;
    private javax.swing.JPanel tabGender;
    private javax.swing.JPanel tabLangProp;
    private javax.swing.JPanel tabType;
    private javax.swing.JTable tblProcGuide;
    private javax.swing.JTextField txtAlphaOrder;
    private javax.swing.JTextField txtConWordFilter;
    private javax.swing.JTextField txtConWordProp;
    private javax.swing.JTextField txtDefFilter;
    private javax.swing.JTextArea txtDefProp;
    private javax.swing.JTextField txtGenderName;
    private javax.swing.JTextArea txtGenderNotes;
    private javax.swing.JTextField txtGendersErrorBox;
    private javax.swing.JTextField txtLangFont;
    private javax.swing.JTextField txtLangName;
    private javax.swing.JTextField txtLocalWordFilter;
    private javax.swing.JTextField txtLocalWordProp;
    private javax.swing.JTextField txtPronunciationFilter;
    private javax.swing.JTextField txtPronunciationProp;
    private javax.swing.JTextField txtTypeName;
    private javax.swing.JTextField txtTypePattern;
    private javax.swing.JTextField txtTypesErrorBox;
    private javax.swing.JTextArea txtTypesNotes;
    private javax.swing.JTextPane txtWordErrorBox;
    // End of variables declaration//GEN-END:variables
}
