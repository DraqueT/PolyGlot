/*
 * Copyright (c) 2014-2020, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Nodes.ConjugationDimension;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.CustomControls.PButton;
import org.darisadesigns.polyglotlina.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.CustomControls.PCellEditor;
import org.darisadesigns.polyglotlina.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.CustomControls.PList;
import org.darisadesigns.polyglotlina.CustomControls.PTable;
import org.darisadesigns.polyglotlina.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.darisadesigns.polyglotlina.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.CustomControls.PTextPane;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;

/**
 * This is the setup form for word forms (declensions/conjugations and their
 * dimensional values.
 *
 * @author draque
 */
public final class ScrDeclensionSetup extends PDialog {

    private Map<Integer, Integer> scrToCoreDeclensions = new HashMap<>();
    private TypeNode myType;
    private int typeId;
    private boolean curPopulating = false;
    private final DefaultListModel<String> declListModel;

    /**
     * Creates new form ScrDeclensionSetup
     *
     * @param _core the dictionary core
     * @param _typeId ID of the type for which declensions are to be modified
     */
    public ScrDeclensionSetup(DictCore _core, Integer _typeId) {
        super(_core);
        typeId = _typeId;
        
        initComponents();
        try {
            myType = _core.getTypes().getNodeById(_typeId);
            this.setTitle("Declensions/Conjugations for type: " + myType.getValue());
            btnClearDep.setToolTipText(btnClearDep.getToolTipText() + myType.getValue());
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            _core.getInfoBox().error("Part of Speech Error",
                    "Part of Speech not found, unable to open declensions for type with id: "
                    + _typeId + " " + e.getMessage());
            this.dispose();
        }

        declListModel = new DefaultListModel<>();
        lstDeclensionList.setModel(declListModel);
        setModal(true);

        populateDeclensionList();
        populateDeclensionProps();
        populateDimensions();
        setupListeners();
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // No values to update due to modal nature of window
    }

    @Override
    public void dispose() {
        if (canClose()) {
            super.dispose();
        }
    }

    /**
     * Tests whether window can be closed. Displays error to user if it cannot
     * be.
     *
     * @return boolean as to whether it is legal to close the window.
     */
    private boolean canClose() {
        ConjugationNode[] decNodes = core.getConjugationManager().getDimensionalConjugationListTemplate(typeId);

        for (ConjugationNode curDec : decNodes) {
            if (curDec.getDimensions().isEmpty()) {
                new DesktopInfoBox(this).error("Illegal Declension", "Declension \'" 
                        + curDec.getValue() 
                        + "\' must have at least one dimension.");
                return false;
            }
        }

        return true;
    }

    private void setupDimTable() {
        DefaultTableModel dimTableModel = new javax.swing.table.DefaultTableModel(
                new Object[][][]{},
                new String[]{
                    "Dimension", "ID"
                }
        ) {
            Class[] types =  {
                java.lang.String.class, java.lang.Boolean.class, java.lang.Integer.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        tblDimensions.setModel(dimTableModel);

        tblDimensions.setColumnSelectionAllowed(true);
        tblDimensions.setMinimumSize(new java.awt.Dimension(0, 0));
        tblDimensions.getTableHeader().setReorderingAllowed(false);
        tblDimensions.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblDimensions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (tblDimensions.getColumnModel().getColumnCount() > 0) {
            tblDimensions.getColumnModel().getColumn(0).setMinWidth(0);
            tblDimensions.removeColumn(tblDimensions.getColumn(tblDimensions.getColumnName(1)));
        }

        TableColumn column = tblDimensions.getColumnModel().getColumn(0);
        column.setCellEditor(new PCellEditor(false, core));

        // disable tab/arrow selection
        tblDimensions.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "none");
        tblDimensions.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "none");
        tblDimensions.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
        tblDimensions.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK), "none");
        tblDimensions.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
        tblDimensions.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK), "none");
        tblDimensions.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        tblDimensions.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), "none");
        tblDimensions.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        tblDimensions.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), "none");
    }

    private void setupListeners() {
        txtDeclensionName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (((PTextField) txtDeclensionName).isSettingText()) {
                    return;
                }
                Cursor cursor = txtDeclensionName.getCursor();
                saveDeclension();
                updateDeclensionListName();
                txtDeclensionName.requestFocus();
                txtDeclensionName.setCursor(cursor);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (((PTextField) txtDeclensionName).isSettingText()) {
                    return;
                }
                Cursor cursor = txtDeclensionName.getCursor();
                saveDeclension();
                updateDeclensionListName();
                txtDeclensionName.requestFocus();
                txtDeclensionName.setCursor(cursor);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (((PTextField) txtDeclensionName).isSettingText()) {
                    return;
                }
                Cursor cursor = txtDeclensionName.getCursor();
                saveDeclension();
                updateDeclensionListName();
                txtDeclensionName.requestFocus();
                txtDeclensionName.setCursor(cursor);
            }
        });
        txtDeclensionNotes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                boolean isSelected = txtDeclensionNotes.isFocusOwner();
                Cursor cursor = txtDeclensionNotes.getCursor();
                saveDeclension();
                if (isSelected) {
                    txtDeclensionNotes.requestFocus();
                    txtDeclensionNotes.setCursor(cursor);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                boolean isSelected = txtDeclensionNotes.isFocusOwner();
                Cursor cursor = txtDeclensionNotes.getCursor();
                saveDeclension();
                if (isSelected) {
                    txtDeclensionNotes.requestFocus();
                    txtDeclensionNotes.setCursor(cursor);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                boolean isSelected = txtDeclensionNotes.isFocusOwner();
                Cursor cursor = txtDeclensionNotes.getCursor();
                saveDeclension();
                if (isSelected) {
                    txtDeclensionNotes.requestFocus();
                    txtDeclensionNotes.setCursor(cursor);
                }
            }
        });
        
        final JPopupMenu ruleMenu = new JPopupMenu();
        final JMenuItem copyItem = new JMenuItem("Copy Conjugation(s)");
        final JMenuItem pasteItem = new JMenuItem("Paste Conjugation(s)");
        copyItem.setToolTipText("Copy currently selected conjugation(s).");
        pasteItem.setToolTipText("Paste conjugation(s) in clipboard to list.");

        copyItem.addActionListener((ActionEvent ae) -> {
            copyConjToClipboard();
        });
        pasteItem.addActionListener((ActionEvent ae) -> {
            pasteConjFromClipboard();
        });
        ruleMenu.add(copyItem);
        ruleMenu.add(pasteItem);
        lstDeclensionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    doPop(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    doPop(e);
                }
            }

            private void doPop(MouseEvent e) {
                if (lstDeclensionList.getSelectedValue() != null) {
                    copyItem.setEnabled(true);
                } else {
                    copyItem.setEnabled(false);
                }

                ruleMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
        
        chkNonDimensional.addActionListener((ActionEvent e) -> {
            if (chkNonDimensional.isSelected()) {
                if (!new DesktopInfoBox(this).actionConfirmation("Are you sure?", "Are you sure you wish to make this dimensionless?\n"
                        + "The dimensions for this declension/conjugation will be erased permanently.")) {
                    chkNonDimensional.setSelected(false);
                }                
            }
            
            saveDeclension();
            core.getConjugationManager().deprecateAllConjugations(myType.getId());
            populateDimensions();
        });
    }
    
    private void copyConjToClipboard() {
        List<ConjugationNode> declensionTemplates = new ArrayList<>();

        for (int i : lstDeclensionList.getSelectedIndices()) {
            ConjugationNode curNodeToCopy = core.getConjugationManager().getConjugationTemplate(myType.getId(), 
                    scrToCoreDeclensions.get(i));
            
            ConjugationNode copyNode = new ConjugationNode(-1);
            copyNode.setEqual(curNodeToCopy);
            declensionTemplates.add(copyNode);
        }

        core.setClipBoard(declensionTemplates);
    }

    private void pasteConjFromClipboard() {
        Object fromClipBoard = core.getClipBoard();
        
        // only paste if appropriate type from clipboard
        if (!(fromClipBoard instanceof ArrayList)
                || ((ArrayList) fromClipBoard).isEmpty()
                || !(((ArrayList) fromClipBoard).get(0) instanceof ConjugationNode)) {
            return;
        }
        
        Iterable<ConjugationNode> conjNodes = (ArrayList)fromClipBoard;
        ConjugationManager decMan = core.getConjugationManager();
        
        try {
            conjNodes.forEach((curNode)->{
                ConjugationNode copyNode = new ConjugationNode(-1);
                copyNode.setEqual(curNode);
                decMan.addConjugationToTemplate(myType.getId(), -1, curNode);
            });
        } catch (ClassCastException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("Error Copying Conjugations", "Unable to copy conjugations: " 
                    + e.getLocalizedMessage());
        }
        
        saveDimension();
        saveDeclension();
        populateDeclensionList();
        populateDeclensionProps();
    }
    
    /**
     * adds new, empty dimensional row
     */
    private void addDimension() {
        addDimensionWithValues("", -1);

        // perform this action later, once the scroll object is properly updated
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = sclDimensions.getVerticalScrollBar();
            bar.setValue(bar.getMaximum() + bar.getBlockIncrement());
        });
    }

    /**
     * deletes selected dimension row, if one is selected
     */
    private void delDimension() {
        if (!new DesktopInfoBox(this).deletionConfirmation()) {
            return;
        }

        int curRow = tblDimensions.getSelectedRow();

        // return if nothing selected
        if (curRow == -1) {
            return;
        }

        Integer nodeId = scrToCoreDeclensions.get(lstDeclensionList.getSelectedIndex());
        ConjugationNode delFrom = core.getConjugationManager().getConjugationTemplate(myType.getId(), nodeId);
        Object o = tblDimensions.getModel().getValueAt(curRow, 1);
        Integer delDimId = (Integer) o;
        delFrom.deleteDimension(delDimId);

        populateDimensions();
    }

    private void populateDimensions() {
        Integer declensionId = scrToCoreDeclensions.get(lstDeclensionList.getSelectedIndex());
        ConjugationNode curDec = core.getConjugationManager().getConjugation(myType.getId(), declensionId);

        // if no current declension, simply clear table.
        if (curDec == null) {
            setupDimTable();
            return;
        }

        List<ConjugationDimension> dimensionList = new ArrayList<>(curDec.getDimensions());

        setupDimTable();

        // do not display singleton dimension for singleton declensions
        if (!curDec.isDimensionless()) {
            dimensionList.forEach((curNode) -> {
                addDimensionWithValues(curNode.getValue(), curNode.getId());
            });
        }
        
        setMenuDimensionless(curDec.isDimensionless());
    }
    
    /**
     * Sets the menu up for a dimensioned/singleton declension node
     * @param _dimensionless 
     */
    private void setMenuDimensionless(boolean _dimensionless) {
        tblDimensions.setEnabled(!_dimensionless);
        btnAddDimension.setEnabled(!_dimensionless);
        btnDelDimension.setEnabled(!_dimensionless);
    }

    private void addDimensionWithValues(String name, Integer dimId) {
        DefaultTableModel model = (DefaultTableModel) tblDimensions.getModel();

        model.addRow(new Object[]{name, dimId});

        // document listener to be fed into editor/renderers for cells...
        DocumentListener docuListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveDimension();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                saveDimension();
            }
        };

        // set saving properties for first column editor
        PCellEditor editor1 = (PCellEditor) tblDimensions.getCellEditor(model.getRowCount() - 1, 0);
        editor1.setDocuListener(docuListener);
    }

    private void saveDimension() {
        SwingUtilities.invokeLater(() -> {
            int curRow = tblDimensions.getSelectedRow();
            int curCol = tblDimensions.getSelectedColumn();
            
            ConjugationNode curDec = core.getConjugationManager().getConjugation(myType.getId(),
                    scrToCoreDeclensions.get(lstDeclensionList.getSelectedIndex()));
            
            for (int i = 0; i < tblDimensions.getRowCount(); i++) {
                
                String dimName;
                
                // The currently selected row will have name information in buffer, not in model
                if (i == curRow && curCol == 0) {
                    dimName = (String) tblDimensions.getCellEditor(i, 0).getCellEditorValue();
                } else {
                    dimName = (String) tblDimensions.getModel().getValueAt(i, 0);
                }
                
                Integer dimId = (Integer) tblDimensions.getModel().getValueAt(i, 1);
                ConjugationDimension dim = new ConjugationDimension();
                
                dim.setId(dimId);
                dim.setValue(dimName);
                
                Integer setId = curDec.addDimension(dim);
                tblDimensions.getModel().setValueAt(setId, i, 1);
            }
        });
    }

    /**
     * confirms with user an action that deprecates all current word forms
     *
     * @return user choice yes/no
     */
    private boolean confirmDeprecate() {
        boolean ret = false;

        if (new DesktopInfoBox(this).yesNoCancel("Confirm action", "This action will deprecate all currently filled out \n"
                + " declensions/conjugations (they won't be lost, but set to a deprecated\nstatus). Continue?") == JOptionPane.YES_OPTION) {
            ret = true;
        }

        return ret;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new PLabel("", menuFontSize);
        jScrollPane1 = new javax.swing.JScrollPane();
        lstDeclensionList = new PList(core.getPropertiesManager().getFontLocal(), menuFontSize);
        jPanel2 = new javax.swing.JPanel();
        txtDeclensionName = new PTextField(core, true, "-- Name --");//PTextField(core, true, "-- Name --");
        jLabel3 = new PLabel("", menuFontSize);
        btnAddDimension = new PAddRemoveButton("+");
        btnDelDimension = new PAddRemoveButton("-");
        sclDimensions = new javax.swing.JScrollPane();
        tblDimensions = new PTable(core);
        jScrollPane3 = new javax.swing.JScrollPane();
        txtDeclensionNotes = new PTextPane(core, true, "-- Notes --");
        chkNonDimensional = new javax.swing.JCheckBox();
        btnDeleteDeclension = new PAddRemoveButton("-");
        btnAddDeclension = new PAddRemoveButton("+");
        btnOK = new PButton(nightMode, menuFontSize);
        btnClearDep = new PButton(nightMode, menuFontSize);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Conjugations");

        lstDeclensionList.setToolTipText("Conjugation class");
        lstDeclensionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstDeclensionListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstDeclensionList);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        txtDeclensionName.setEnabled(false);
        txtDeclensionName.setMinimumSize(new java.awt.Dimension(0, 0));

        jLabel3.setText("Dimensions");

        btnAddDimension.setToolTipText("Add a dimension to selected conjugation class");
        btnAddDimension.setEnabled(false);
        btnAddDimension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDimensionActionPerformed(evt);
            }
        });

        btnDelDimension.setToolTipText("Delete selected dimension");
        btnDelDimension.setEnabled(false);
        btnDelDimension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelDimensionActionPerformed(evt);
            }
        });

        tblDimensions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Dimension", "Mandatory"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblDimensions.setColumnSelectionAllowed(true);
        tblDimensions.setEnabled(false);
        tblDimensions.setMinimumSize(new java.awt.Dimension(0, 0));
        tblDimensions.getTableHeader().setReorderingAllowed(false);
        sclDimensions.setViewportView(tblDimensions);
        tblDimensions.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tblDimensions.getColumnModel().getColumnCount() > 0) {
            tblDimensions.getColumnModel().getColumn(0).setMinWidth(0);
            tblDimensions.getColumnModel().getColumn(1).setMinWidth(0);
            tblDimensions.getColumnModel().getColumn(1).setPreferredWidth(10);
        }

        jScrollPane3.setViewportView(txtDeclensionNotes);

        chkNonDimensional.setText("Non-Dimensional");
        chkNonDimensional.setToolTipText("Check this if this is a non dimensional form, such as a gerund.");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3)
                            .addComponent(txtDeclensionName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnAddDimension, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnDelDimension, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(sclDimensions, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkNonDimensional)
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtDeclensionName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkNonDimensional)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sclDimensions, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDelDimension)
                    .addComponent(btnAddDimension))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnDeleteDeclension.setToolTipText("Delete selected conjugation class");
        btnDeleteDeclension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteDeclensionActionPerformed(evt);
            }
        });

        btnAddDeclension.setToolTipText("Add conjugation class");
        btnAddDeclension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDeclensionActionPerformed(evt);
            }
        });

        btnOK.setText("OK");
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });

        btnClearDep.setText("Clear Deprecated Values");
        btnClearDep.setToolTipText("Deletes all deprecated conjugation values from  words of type: ");
        btnClearDep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearDepActionPerformed(evt);
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
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnAddDeclension, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnDeleteDeclension, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(btnClearDep)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnOK))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnDeleteDeclension)
                            .addComponent(btnAddDeclension)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnOK)
                            .addComponent(btnClearDep))))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddDeclensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDeclensionActionPerformed
        addDeclension();
    }//GEN-LAST:event_btnAddDeclensionActionPerformed

    private void btnDeleteDeclensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteDeclensionActionPerformed
        deleteDeclension();
    }//GEN-LAST:event_btnDeleteDeclensionActionPerformed

    private void lstDeclensionListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstDeclensionListValueChanged
        populateDeclensionProps();
    }//GEN-LAST:event_lstDeclensionListValueChanged

    private void btnAddDimensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDimensionActionPerformed
        addDimension();
    }//GEN-LAST:event_btnAddDimensionActionPerformed

    private void btnDelDimensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelDimensionActionPerformed
        delDimension();
    }//GEN-LAST:event_btnDelDimensionActionPerformed

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        dispose();
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnClearDepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearDepActionPerformed
        if (new DesktopInfoBox(this).yesNoCancel("Wipe All Deprecated Declensions?", 
                "Are you sure? This cannot be undone, and will delete the values of all deprecated declensions of the type: "
                + myType.getValue() + ".") == JOptionPane.YES_OPTION) {
            core.getWordCollection().clearDeprecatedDeclensions(myType.getId());
        }
    }//GEN-LAST:event_btnClearDepActionPerformed

    public static ScrDeclensionSetup run(final DictCore _core, final Integer _typeId) {
        ScrDeclensionSetup s = new ScrDeclensionSetup(_core, _typeId);

        s.setModal(true);
        s.setVisible(true);

        return s;
    }

    /**
     * adds a declension to the list and readies the system to create it in the
     * core on modification
     */
    private void addDeclension() {
        // confirm user is will to deprecate all existing forms
        if (!confirmDeprecate()) {
            return;
        }

        // deprecate all existing forms
        core.getConjugationManager().deprecateAllConjugations(myType.getId());

        if (lstDeclensionList.getModel().getSize() != 0
                && scrToCoreDeclensions.containsKey(lstDeclensionList.getSelectedIndex())
                && scrToCoreDeclensions.get(lstDeclensionList.getSelectedIndex()) == -1) {
            return;
        }

        boolean localPopulating = curPopulating;

        curPopulating = true;

        clearDeclensionProps();
        int declIndex = declListModel.getSize();
        declListModel.add(declIndex, "NEW DECLENSION");
        lstDeclensionList.setSelectedIndex(declIndex);
        scrToCoreDeclensions.put(declIndex, -1);
        curPopulating = localPopulating;

        populateDeclensionProps();
    }

    private void clearDeclensionProps() {
        // prevents this from resetting population values
        boolean localPop = curPopulating;

        curPopulating = true;

        txtDeclensionName.setText("");
        txtDeclensionNotes.setText("");

        this.setupDimTable();

        curPopulating = localPop;
    }

    private void setEnabledDecProps(boolean enable) {
        txtDeclensionName.setEnabled(enable);
        tblDimensions.setEnabled(enable);
        btnAddDimension.setEnabled(enable);
        btnDelDimension.setEnabled(enable);
        txtDeclensionNotes.setEnabled(enable);
    }

    private void populateDeclensionProps() {
        ConjugationNode curDec = new ConjugationNode(-1);
        int decIndex = lstDeclensionList.getSelectedIndex();

        // keep local settings from stomping on higher level population
        boolean populatingLocal = curPopulating;

        //avoid recursive population
        if (curPopulating) {
            return;
        }

        curPopulating = true;

        if (decIndex == -1) {
            txtDeclensionName.setText("");
            txtDeclensionNotes.setText("");
            curPopulating = populatingLocal;
        }

        Integer decId = scrToCoreDeclensions.get(decIndex);

        if (decId != null && decId != -1) {
            try {
                curDec = core.getConjugationManager().getConjugationTemplate(myType.getId(), decId);
            } catch (Exception e) {
                DesktopIOHandler.getInstance().writeErrorLog(e);
                new DesktopInfoBox(this).error("Declension Population Error", "Unable to populate declension.\n\n"
                        + e.getMessage());
                curPopulating = populatingLocal;
                return;
            }
        }

        setEnabledDecProps(decIndex != -1);

        if (curDec == null) {
            curPopulating = populatingLocal;
            return;
        }

        setIsActiveDimensions();

        // prevent self setting (from user mod)
        if (!txtDeclensionName.getText().trim().equals(curDec.getValue().trim())) {
            txtDeclensionName.setText(curDec.getValue());
        }
        if (!txtDeclensionNotes.getText().equals(curDec.getNotes())) {
            txtDeclensionNotes.setText(curDec.getNotes());
        }

        populateDimensions();
        
        chkNonDimensional.setSelected(curDec.isDimensionless());

        curPopulating = populatingLocal;
    }

    /**
     * Sets the dimension controls active if appropriate, inactive otherwise
     */
    private void setIsActiveDimensions() {
        int decId = -1;
        
        if (scrToCoreDeclensions.containsKey(lstDeclensionList.getSelectedIndex())) {
            decId = scrToCoreDeclensions.get(lstDeclensionList.getSelectedIndex());
        }
        
        // also checks whether the form is dimensional in the first place
        if (decId == -1 || chkNonDimensional.isSelected()) {
            // the dimensions can only be added without error if there is a declension
            tblDimensions.setEnabled(false);
            btnAddDimension.setEnabled(false);
            btnDelDimension.setEnabled(false);
        } else {
            tblDimensions.setEnabled(true);
            btnAddDimension.setEnabled(true);
            btnDelDimension.setEnabled(true);
        }
    }

    private void updateDeclensionListName() {
        int decIndex = lstDeclensionList.getSelectedIndex();

        // keep local settings from stomping on higher level population
        boolean populatingLocal = curPopulating;

        if (decIndex == -1 || curPopulating) {
            return;
        }

        curPopulating = true;

        declListModel.remove(decIndex);
        declListModel.add(decIndex, txtDeclensionName.getText().trim());

        lstDeclensionList.setSelectedIndex(decIndex);

        curPopulating = populatingLocal;
    }

    /**
     * deletes currently selected declension in list
     */
    private void deleteDeclension() {
        // confirm user is will to deprecate all existing forms
        if (!confirmDeprecate()) {
            return;
        }

        // deprecate all existing forms
        core.getConjugationManager().deprecateAllConjugations(myType.getId());

        int curIndex = lstDeclensionList.getSelectedIndex();

        try {
            core.getConjugationManager().deleteConjugationFromTemplate(myType.getId(), scrToCoreDeclensions.get(curIndex));
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("Declension Deletion Error", "Unable to delete Declension: "
                    + lstDeclensionList.getSelectedValue() + "\n\n" + e.getMessage());
        }

        if (curIndex > 0) {
            curIndex--;
        }

        populateDeclensionList();

        populateDeclensionProps();

        lstDeclensionList.setSelectedIndex(curIndex);
    }

    private void saveDeclension() {
        Integer decIndex = lstDeclensionList.getSelectedIndex();

        if (curPopulating) {
            return;
        }

        if (decIndex == -1) {
            return;
        }

        curPopulating = true;

        int decId = -1;
        
        if (scrToCoreDeclensions.containsKey(decIndex)) {
            decId = scrToCoreDeclensions.get(decIndex);
        }

        ConjugationNode decl;

        try {
            // split logic for creating, rather than modifying Declension
            if (decId == -1) {
                decl = core.getConjugationManager().addConjugationToTemplate(myType.getId(), txtDeclensionName.getText());
                decl.setValue(txtDeclensionName.getText().trim());
                decl.setNotes(txtDeclensionNotes.getText().trim());
                decl.setDimensionless(chkNonDimensional.isSelected());

                scrToCoreDeclensions.put(decIndex, decl.getId());
            } else {
                decl = new ConjugationNode(-1);
                ConjugationNode oldDecl = core.getConjugationManager().getConjugation(myType.getId(), decId);

                decl.setEqual(oldDecl);

                decl.setValue(txtDeclensionName.getText().trim());
                decl.setNotes(txtDeclensionNotes.getText().trim());
                decl.setDimensionless(chkNonDimensional.isSelected());

                core.getConjugationManager().updateConjugationTemplate(myType.getId(), decId, decl);
            }
        } catch (ClassCastException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("Declension Creation Error", "Unable to create Declension "
                    + txtDeclensionName.getText() + "\n\n" + e.getMessage());
        }

        setIsActiveDimensions();

        curPopulating = false;
    }

    private void populateDeclensionList() {
        // avoid recursive population
        if (curPopulating) {
            return;
        }

        Integer curdecId = scrToCoreDeclensions.get(lstDeclensionList.getSelectedIndex());
        int setIndex = -1;

        curPopulating = true;
        
        // relevant objects should be rebuilt
        scrToCoreDeclensions = new HashMap<>();

        declListModel.clear();

        ConjugationNode[] decNodes = core.getConjugationManager().getFullConjugationListTemplate(myType.getId());
        ConjugationNode curdec;
        for (int i = 0; i < decNodes.length; i++) {
            curdec = decNodes[i];

            declListModel.add(i, curdec.getValue());
            scrToCoreDeclensions.put(i, curdec.getId());

            // replaced call to Object type here
            if (curdecId != null
                    && curdecId.equals(curdec.getId())) {
                setIndex = i;
            } else {
                setIndex = 0;
            }
        }

        lstDeclensionList.setSelectedIndex(setIndex);

        curPopulating = false;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDeclension;
    private javax.swing.JButton btnAddDimension;
    private javax.swing.JButton btnClearDep;
    private javax.swing.JButton btnDelDimension;
    private javax.swing.JButton btnDeleteDeclension;
    private javax.swing.JButton btnOK;
    private javax.swing.JCheckBox chkNonDimensional;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList<String> lstDeclensionList;
    private javax.swing.JScrollPane sclDimensions;
    private javax.swing.JTable tblDimensions;
    private javax.swing.JTextField txtDeclensionName;
    private javax.swing.JTextPane txtDeclensionNotes;
    // End of variables declaration//GEN-END:variables
}
