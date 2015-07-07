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
// TODO: Implement greyed values, as in other new forms
import PolyGlot.DictCore;
import PolyGlot.CustomControls.InfoBox;
import PolyGlot.JFontChooser;
import PolyGlot.CustomControls.PButton;
import PolyGlot.CustomControls.PDialog;
import PolyGlot.CustomControls.PTextField;
import PolyGlot.Nodes.PronunciationNode;
import PolyGlot.ManagersCollections.PropertiesManager;
import PolyGlot.CustomControls.TableColumnEditor;
import PolyGlot.CustomControls.TableColumnRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author draque
 */
public class ScrLangProps extends PDialog {
    private final DictCore core;
    private boolean curPopulating = false;
    private static final String defName = "- Language Name -";
    private static final String defAlpha = "- Alphabetical Order -";
    
    /**
     * Creates new form ScrLangProps
     * @param _core Dictionary Core
     */
    public ScrLangProps(DictCore _core) {
        core = _core;
        initComponents();
        populateProperties();
        txtAlphaOrder.setFont(core.getPropertiesManager().getFontCon());
        
        setModal(true);
    }
    
    @Override
    public void updateAllValues(DictCore _core) {
        // due to modal nature of form, does nothing
    }
    
    @Override
    public void dispose() {
        saveAllProps();
        core.pushUpdate();
        super.dispose();
    }
    
    private void populateProperties() {
        PropertiesManager prop = core.getPropertiesManager();
        
        txtLangName.setText(prop.getLangName().equals("") ?
                defName : prop.getLangName());
        txtFont.setText(prop.getFontCon().getFamily());
        txtAlphaOrder.setText(prop.getAlphaPlainText().equals("") ?
                defAlpha : prop.getAlphaPlainText());
        chkDisableProcRegex.setSelected(prop.isDisableProcRegex());
        chkIgnoreCase.setSelected(prop.isIgnoreCase());
        chkLocalMandatory.setSelected(prop.isLocalMandatory());
        chkLocalUniqueness.setSelected(prop.isLocalUniqueness());
        chkTypesMandatory.setSelected(prop.isTypesMandatory());
        chkWordUniqueness.setSelected(prop.isWordUniqueness());
        chkEnforceRTL.setSelected(prop.isEnforceRTL());
        
        txtAlphaOrder.setForeground(txtAlphaOrder.getText().equals(defAlpha) ?
                Color.lightGray : Color.black);
        txtLangName.setForeground(txtLangName.getText().equals(defName) ?
                Color.lightGray : Color.black);
                
        populateProcs();
    }
    
    /**
     * saves all language properties
     */
    private void saveAllProps() {
        PropertiesManager propMan = core.getPropertiesManager();
        
        propMan.setAlphaOrder(txtAlphaOrder.getText().trim());
        propMan.setDisableProcRegex(chkDisableProcRegex.isSelected());
        propMan.setIgnoreCase(chkIgnoreCase.isSelected());
        propMan.setLangName(txtLangName.getText().equals(defName) ?
                "" : txtLangName.getText());
        propMan.setLocalMandatory(chkLocalMandatory.isSelected());
        propMan.setLocalUniqueness(chkLocalUniqueness.isSelected());
        propMan.setTypesMandatory(chkTypesMandatory.isSelected());
        propMan.setWordUniqueness(chkWordUniqueness.isSelected());
        propMan.setEnforceRTL(chkEnforceRTL.isSelected());
        
        saveProcGuide();
    }
    
    private void setupProcTable() {
        DefaultTableModel procTableModel = new DefaultTableModel();
        procTableModel.addColumn("Character(s)");
        procTableModel.addColumn("Pronuncation");
        tblProcs.setModel(procTableModel); // TODO: find way to make tblProcs display RTL order when appropriate Maybe something on my custom cell editor

        procTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                saveProcGuide();
            }
        });

        Font defaultFont = new JLabel().getFont();
        Font conFont = core.getPropertiesManager().getFontCon();

        TableColumn column = tblProcs.getColumnModel().getColumn(0);
        column.setCellEditor(new TableColumnEditor(conFont));
        column.setCellRenderer(new TableColumnRenderer(conFont));

        column = tblProcs.getColumnModel().getColumn(1);
        column.setCellEditor(new TableColumnEditor(defaultFont));
        column.setCellRenderer(new TableColumnRenderer(defaultFont));

        // TODO: analyze whether I can drop this
        // disable tab/arrow selection
        InputMap procInput = tblProcs.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), "none");
    }
    
    /**
     * Saves pronunciation guide to core
     */
    private void saveProcGuide() {
        if (curPopulating) {
            return;
        }
        boolean localPopulating = curPopulating;
        curPopulating = true;

        if (tblProcs.getCellEditor() != null) {
            tblProcs.getCellEditor().stopCellEditing();
        }

        List<PronunciationNode> newPro = new ArrayList<PronunciationNode>();

        for (int i = 0; i < tblProcs.getRowCount(); i++) {
            PronunciationNode newNode = new PronunciationNode();
            
            newNode.setValue((String) tblProcs.getModel().getValueAt(i, 0));
            newNode.setPronunciation((String) tblProcs.getModel().getValueAt(i, 1));

            newPro.add(newNode);
        }

        core.getPronunciationMgr().setPronunciations(newPro);
        curPopulating = localPopulating;
    }
    
    /**
     * populates pronunciation values
     */
    private void populateProcs() {
        Iterator<PronunciationNode> popGuide = core.getPronunciationMgr().getPronunciations();

        // wipe current rows, repopulate from core
        setupProcTable();

        while (popGuide.hasNext()) {
            PronunciationNode curNode = popGuide.next();

            addProcWithValues(curNode.getValue(), curNode.getPronunciation());
        }
    }
    
    /**
     * Adds pronunciation with values existing
     * @param base base characters 
     * @param proc pronunciation
     */
    private void addProcWithValues(String base, String proc) {
        boolean populatingLocal = curPopulating;
        curPopulating = true;

        DefaultTableModel procTableModel = (DefaultTableModel)tblProcs.getModel();
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
        TableColumnEditor editor = (TableColumnEditor) tblProcs.getCellEditor(procTableModel.getRowCount() - 1, 0);
        editor.setDocuListener(docuListener);
        editor.setInitialValue(base);

        // set saving properties for second column editor
        editor = (TableColumnEditor) tblProcs.getCellEditor(procTableModel.getRowCount() - 1, 1);
        editor.setDocuListener(docuListener);
        editor.setInitialValue(proc);

        curPopulating = populatingLocal;
    }
    
    /**
     * adds new, blank pronunciation entry
     */
    private void addProc() {
        final int curPosition = tblProcs.getSelectedRow();
        
        core.getPronunciationMgr().addAtPosition(curPosition + 1, new PronunciationNode());
        populateProcs();

        // perform this action later, once the scroll object is properly updated
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tblProcs.getSelectionModel().setSelectionInterval(curPosition + 1, curPosition + 1);
                tblProcs.scrollRectToVisible(new Rectangle(tblProcs.getCellRect(curPosition + 1, 0, true)));
                tblProcs.changeSelection(curPosition + 1, 0, false, false);
            }
        });
    }
    
    /**
     * delete currently selected pronunciation (with confirmation)
     */
    private void deleteProc() {
        Integer curRow = tblProcs.getSelectedRow();

        if (curRow == -1
                || !InfoBox.deletionConfirmation(this)) {
            return;
        }

        PronunciationNode delNode = new PronunciationNode();

        delNode.setValue(tblProcs.getValueAt(curRow, 0).toString());
        delNode.setPronunciation(tblProcs.getValueAt(curRow, 1).toString());

        core.getPronunciationMgr().deletePronunciation(delNode);
        populateProcs();
    }
    
    /**
     * moves selected pronunciation down one priority slot
     */
    private void moveProcUp() {
        Integer curRow = tblProcs.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getPronunciationMgr().moveProcUp(curRow);

        populateProcs();

        if (curRow != 0) {
            tblProcs.setRowSelectionInterval(curRow - 1, curRow - 1);
        } else {
            tblProcs.setRowSelectionInterval(curRow, curRow);
        }
    }

    /**
     * moves selected pronunciation up one priority slot
     */
    private void moveProcDown() {
        Integer curRow = tblProcs.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getPronunciationMgr().moveProcDown(curRow);

        populateProcs();

        if (curRow != tblProcs.getRowCount() - 1) {
            tblProcs.setRowSelectionInterval(curRow + 1, curRow + 1);
        } else {
            tblProcs.setRowSelectionInterval(curRow, curRow);
        }
    }
    
    /**
     * Instantiates font chooser and returns user defined font
     * @return font selected by user
     */
    private Font fontDialog() {
        JFontChooser fontChooser = new JFontChooser();
        Integer result = fontChooser.showDialog(btnChangeFont);
        Font font = null;

        if (result == JFontChooser.OK_OPTION) {
            font = fontChooser.getSelectedFont();
        }

        return font;
    }
    
    private void setConFont(Font conFont) {
        if (conFont == null) {
            return;
        }
        
        txtAlphaOrder.setFont(conFont);

        core.getPropertiesManager().setFontCon(conFont, conFont.getStyle(), conFont.getSize());
        txtFont.setText(conFont.getFamily());

        TableColumn column = tblProcs.getColumnModel().getColumn(0);
        column.setCellEditor(new TableColumnEditor(conFont));
        column.setCellRenderer(new TableColumnRenderer(conFont));
        populateProcs();
        testRTLWarning();
    }
    
    /**
     * Displays warning to user if RTL is enforced and confont is standard
     */
    private void testRTLWarning() {
        Font conFont = core.getPropertiesManager().getFontCon();
        Font stdFont = (new JTextField()).getFont();
        
        if (core.getPropertiesManager().isEnforceRTL()
                && (conFont == null
                    || conFont.getFamily().equals(stdFont.getFamily()))) {
            InfoBox.warning("RTL Font Warning", "Enforcing RTL with default font"
                    + " is not recommended. For best results, please set distinct"
                    + " conlang font.", this);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtLangName = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        btnChangeFont = new javax.swing.JButton();
        txtFont = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        txtAlphaOrder = new PTextField(core);
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        chkTypesMandatory = new javax.swing.JCheckBox();
        chkLocalMandatory = new javax.swing.JCheckBox();
        chkWordUniqueness = new javax.swing.JCheckBox();
        chkLocalUniqueness = new javax.swing.JCheckBox();
        chkIgnoreCase = new javax.swing.JCheckBox();
        chkDisableProcRegex = new javax.swing.JCheckBox();
        chkEnforceRTL = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnAddProc = new PButton("+");
        jButton3 = new PButton("-");
        btnUpProc = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProcs = new javax.swing.JTable();
        btnDownProc = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Language Properties");

        txtLangName.setToolTipText("Your Conlang's Name");
        txtLangName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtLangNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtLangNameFocusLost(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        btnChangeFont.setText("Change Font");
        btnChangeFont.setToolTipText("Change native conlang font");
        btnChangeFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeFontActionPerformed(evt);
            }
        });

        txtFont.setToolTipText("Conlang Font");
        txtFont.setEnabled(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtAlphaOrder.setToolTipText("List of all characters in conlang in alphabetical order (both upper and lower case)");
        txtAlphaOrder.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtAlphaOrderFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtAlphaOrderFocusLost(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(2);
        jTextArea1.setText("List all characters used in your conlang here in their alphabetical order without spaces or commas. Leave blank for system default.");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setEnabled(false);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtAlphaOrder))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane1)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtAlphaOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        chkTypesMandatory.setText("Types Mandatory");
        chkTypesMandatory.setToolTipText("Check to enforce as mandatory a type on each created conword.");

        chkLocalMandatory.setText("Local Mandatory");
        chkLocalMandatory.setToolTipText("Check to enforce as mandatory a local word on each created word entry.");

        chkWordUniqueness.setText("Word Uniqueness");
        chkWordUniqueness.setToolTipText("Check to enforce as mandatory unique constructed words.");

        chkLocalUniqueness.setText("Local Uniqueness");
        chkLocalUniqueness.setToolTipText("Check to enforce as mandatory uniqueness in entries on the Local Word field.");

        chkIgnoreCase.setText("Ignore Case");
        chkIgnoreCase.setToolTipText("Ignore casing through PolyGlot.");

        chkDisableProcRegex.setText("Disable Proc Regex");
        chkDisableProcRegex.setToolTipText("Disable regex features in the pronunciation guide. (this allows for ignoring case properly there)");

        chkEnforceRTL.setText("Enforce RTL");
        chkEnforceRTL.setToolTipText("Check this to force all conlang text to appear in RTL fashion through PolyGlot. This works even if the character set you are using is not typically RTL.");
        chkEnforceRTL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkEnforceRTLActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chkTypesMandatory)
            .addComponent(chkLocalMandatory)
            .addComponent(chkWordUniqueness)
            .addComponent(chkLocalUniqueness)
            .addComponent(chkIgnoreCase)
            .addComponent(chkDisableProcRegex)
            .addComponent(chkEnforceRTL)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(chkTypesMandatory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkLocalMandatory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkWordUniqueness)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkLocalUniqueness)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkIgnoreCase)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkDisableProcRegex)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkEnforceRTL)
                .addGap(0, 14, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setMinimumSize(new java.awt.Dimension(10, 10));

        jLabel1.setText("Pronunciation Guide");

        btnAddProc.setToolTipText("Add new pronunciation entry.");
        btnAddProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProcActionPerformed(evt);
            }
        });

        jButton3.setToolTipText("Delete selected pronunciation entry.");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        btnUpProc.setText("↑");
        btnUpProc.setToolTipText("Move selected entry up one position.");
        btnUpProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpProcActionPerformed(evt);
            }
        });

        tblProcs.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null}
            },
            new String [] {
                "Character(s)", "Pronunciation"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblProcs.setToolTipText("Add characters (or sets of characters) here with their associated pronunciations.");
        tblProcs.setMinimumSize(new java.awt.Dimension(30, 20));
        tblProcs.setRowHeight(30);
        jScrollPane2.setViewportView(tblProcs);

        btnDownProc.setText("↓");
        btnDownProc.setToolTipText("Move selected entry down one position.");
        btnDownProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownProcActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(btnAddProc, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(99, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnDownProc, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnUpProc, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btnUpProc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDownProc)
                        .addGap(12, 12, 12))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddProc, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnChangeFont)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFont))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnChangeFont)
                    .addComponent(txtFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtLangName))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtLangName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnUpProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpProcActionPerformed
        moveProcUp();
    }//GEN-LAST:event_btnUpProcActionPerformed

    private void btnDownProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownProcActionPerformed
        moveProcDown();
    }//GEN-LAST:event_btnDownProcActionPerformed

    private void btnAddProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProcActionPerformed
        addProc();
    }//GEN-LAST:event_btnAddProcActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        deleteProc();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void btnChangeFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeFontActionPerformed
        setConFont(fontDialog());
    }//GEN-LAST:event_btnChangeFontActionPerformed

    private void chkEnforceRTLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkEnforceRTLActionPerformed
        // needs to update value immediately for text elements on this form affected by change
        core.getPropertiesManager().setEnforceRTL(chkEnforceRTL.isSelected());
        testRTLWarning();
    }//GEN-LAST:event_chkEnforceRTLActionPerformed

    private void txtLangNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtLangNameFocusGained
        if (txtLangName.getText().equals(defName)) {
            txtLangName.setText("");
            txtLangName.setForeground(Color.black);
        }
    }//GEN-LAST:event_txtLangNameFocusGained

    private void txtLangNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtLangNameFocusLost
        if (txtLangName.getText().equals("")) {
            txtLangName.setText(defName);
            txtLangName.setForeground(Color.lightGray);
        }
    }//GEN-LAST:event_txtLangNameFocusLost

    private void txtAlphaOrderFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAlphaOrderFocusGained
        if (txtAlphaOrder.getText().equals(defAlpha)) {
            txtAlphaOrder.setText("");
            txtAlphaOrder.setForeground(Color.black);
        }
    }//GEN-LAST:event_txtAlphaOrderFocusGained

    private void txtAlphaOrderFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAlphaOrderFocusLost
        if (txtAlphaOrder.getText().equals("")) {
            txtAlphaOrder.setText(defAlpha);
            txtAlphaOrder.setForeground(Color.lightGray);
        }
    }//GEN-LAST:event_txtAlphaOrderFocusLost

    public static ScrLangProps run(DictCore _core) {
        ScrLangProps s = new ScrLangProps(_core);
        s.setupKeyStrokes();
        return s;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProc;
    private javax.swing.JButton btnChangeFont;
    private javax.swing.JButton btnDownProc;
    private javax.swing.JButton btnUpProc;
    private javax.swing.JCheckBox chkDisableProcRegex;
    private javax.swing.JCheckBox chkEnforceRTL;
    private javax.swing.JCheckBox chkIgnoreCase;
    private javax.swing.JCheckBox chkLocalMandatory;
    private javax.swing.JCheckBox chkLocalUniqueness;
    private javax.swing.JCheckBox chkTypesMandatory;
    private javax.swing.JCheckBox chkWordUniqueness;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTable tblProcs;
    private javax.swing.JTextField txtAlphaOrder;
    private javax.swing.JTextField txtFont;
    private javax.swing.JTextField txtLangName;
    // End of variables declaration//GEN-END:variables
}
