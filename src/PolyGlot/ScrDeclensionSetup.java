/*
 * Copyright (c) 2014, draque
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
package PolyGlot;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * This is the setup form for word forms (declensions/conjugations and their
 * dimensional values.
 * @author draque
 */
public final class ScrDeclensionSetup extends PDialog {

    private Map scrToCoreDeclensions = new HashMap<Integer, Integer>();
    private Map scrDeclensionMap = new HashMap<String, Integer>();
    private TypeNode myType;
    private DictCore core;
    private boolean curPopulating = false;
    private final DefaultListModel declListModel;

    /**
     * Creates new form ScrDeclensionSetup
     *
     * @param _core the dictionary core
     * @param _typeId ID of the type for which declensions are to be modified
     */
    public ScrDeclensionSetup(DictCore _core, Integer _typeId) {
        setupKeyStrokes();
        initComponents();
        try {
            core = _core;
            myType = _core.getTypes().getNodeById(_typeId);
            this.setTitle("Declensions/Conjugations for type: " + myType.getValue());
        } catch (Exception e) {
            InfoBox.error("Type Error", "Type not found, unable to open declensions for type with id: " + _typeId + " " + e.getMessage(), this);
            this.dispose();
        }

        declListModel = new DefaultListModel();
        lstDeclensionList.setModel(declListModel);

        populateDeclensionList();
        setupListeners();

        setupDimTable();
    }
    
    @Override
    public void dispose() {
        if (canClose()) {
            super.dispose();
        }
    }
    
    /**
     * Tests whether window can be closed. Displays error to user if it cannot be.
     * @return boolean as to whether it is legal to close the window.
     */
    private boolean canClose() {
        Iterator<DeclensionNode> decIt = core.getDeclensionManager().getDeclensionListTemplate(myType.getId()).iterator();
        
        while (decIt.hasNext()) {
            DeclensionNode curDec = decIt.next();
            if (curDec.getDimensions().isEmpty())
            {
                InfoBox.error("Illegal Declension", "Declension \'" + curDec.getValue() + "\' must have at least one dimension.", this);
                return false;
            }
        }
        
        return true;
    }

    private void setupDimTable() {
        DefaultTableModel dimTableModel = new javax.swing.table.DefaultTableModel(
                new Object[][][]{},
                new String[]{
                    "Dimension", "Mandatory", "ID"
                }
        ) {
            // col 0 = dimension name, col 1 = manditory selection, col 3 = hidden dim ID
            Class[] types = new Class[]{
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
            tblDimensions.getColumnModel().getColumn(1).setMinWidth(0);
            tblDimensions.getColumnModel().getColumn(1).setPreferredWidth(10);
            tblDimensions.removeColumn(tblDimensions.getColumn(tblDimensions.getColumnName(2)));
        }

        TableColumn column = tblDimensions.getColumnModel().getColumn(0);
        column.setCellEditor(new TableColumnEditor(new JLabel().getFont()));

        column = tblDimensions.getColumnModel().getColumn(1);
        column.setCellEditor(new TableBooleanEditor());

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
                Cursor cursor = txtDeclensionName.getCursor();
                saveDeclension();
                updateDeclensionListName();
                txtDeclensionName.requestFocus();
                txtDeclensionName.setCursor(cursor);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                Cursor cursor = txtDeclensionName.getCursor();
                saveDeclension();
                updateDeclensionListName();
                txtDeclensionName.requestFocus();
                txtDeclensionName.setCursor(cursor);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
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
                Cursor cursor = txtDeclensionNotes.getCursor();
                saveDeclension();
                txtDeclensionNotes.requestFocus();
                txtDeclensionNotes.setCursor(cursor);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                Cursor cursor = txtDeclensionNotes.getCursor();
                saveDeclension();
                txtDeclensionNotes.requestFocus();
                txtDeclensionNotes.setCursor(cursor);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                Cursor cursor = txtDeclensionNotes.getCursor();
                saveDeclension();
                txtDeclensionNotes.requestFocus();
                txtDeclensionNotes.setCursor(cursor);
            }
        });
    }

    /**
     * adds new, empty dimensional row
     */
    private void addDimension() {
        addDemensionWithValues("", false, -1);

        // perform this action later, once the scroll object is properly updated
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JScrollBar bar = sclDimensions.getVerticalScrollBar();
                bar.setValue(bar.getMaximum() + bar.getBlockIncrement());
            }
        });
    }

    /**
     * deletes selected dimension row, if one is selected
     */
    private void delDimension() {
        if (!InfoBox.deletionConfirmation(this)) {
            return;
        }
        
        Integer curRow = tblDimensions.getSelectedRow();

        // return if nothing selected
        if (curRow == -1) {
            return;
        }

        Integer nodeId = (Integer) scrToCoreDeclensions.get(lstDeclensionList.getSelectedIndex());
        DeclensionNode delFrom = core.getDeclensionTemplate(myType.getId(), nodeId);
        Integer delDimId = (Integer)tblDimensions.getModel().getValueAt(tblDimensions.getSelectedRow(), 2);
        delFrom.deleteDimension(delDimId);

        populateDimensions();
    }

    private void populateDimensions() {
        Integer declensionId = (Integer) scrToCoreDeclensions.get((Integer) lstDeclensionList.getSelectedIndex());
        DeclensionNode curDec = core.getDeclensionManager().getDeclension(myType.getId(), declensionId);

        // if no current declension, simply clear table.
        if (curDec == null) {
            setupDimTable();
            return;
        }

        Iterator<DeclensionDimension> dimIt = curDec.getDimensions().iterator();

        setupDimTable();

        while (dimIt.hasNext()) {
            DeclensionDimension curNode = dimIt.next();

            addDemensionWithValues(curNode.value, curNode.isMandatory(), curNode.getId());
        }
    }

    private void addDemensionWithValues(String name, boolean mandatory, Integer dimId) {
        DefaultTableModel model = (DefaultTableModel) tblDimensions.getModel();

        model.addRow(new Object[]{name, mandatory, dimId});

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
        TableColumnEditor editor1 = (TableColumnEditor) tblDimensions.getCellEditor(model.getRowCount() - 1, 0);
        editor1.setDocuListener(docuListener);
        
        ActionListener actListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDimension();
            }
        };

        // set saving properties for second column editor
        TableBooleanEditor editor2 = (TableBooleanEditor) tblDimensions.getCellEditor(model.getRowCount() - 1, 1);
        editor2.setDocuListener(actListener);
    }

    private void saveDimension() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int curRow = tblDimensions.getSelectedRow();
                int curCol = tblDimensions.getSelectedColumn();

                DeclensionNode curDec = core.getDeclensionManager().getDeclension(myType.getId(),
                        (Integer) scrToCoreDeclensions.get(lstDeclensionList.getSelectedIndex()));

                for (int i = 0; i < tblDimensions.getRowCount(); i++) {

                    String dimName = "";
                    Boolean dimMand = false;

                    // The currently selected row will have name information in buffer, not in model
                    if (i == curRow) {
                        if (curCol == 0) {
                            dimName = (String) tblDimensions.getCellEditor(i, 0).getCellEditorValue();
                            dimMand = (Boolean) tblDimensions.getModel().getValueAt(i, 1);
                        } else if (curCol == 1) {
                            dimMand = (Boolean) tblDimensions.getCellEditor(i, 1).getCellEditorValue();
                            dimName = (String) tblDimensions.getModel().getValueAt(i, 0);
                        }
                    } else {
                        dimName = (String) tblDimensions.getModel().getValueAt(i, 0);
                        dimMand = (Boolean) tblDimensions.getModel().getValueAt(i, 1);
                    }

                    Integer dimId = (Integer) tblDimensions.getModel().getValueAt(i, 2);
                    DeclensionDimension dim = new DeclensionDimension();

                    dim.setId(dimId);
                    dim.setValue(dimName);
                    dim.setMandatory(dimMand);

                    Integer setId = curDec.addDimension(dim);
                    tblDimensions.getModel().setValueAt(setId, i, 2);
                }
            }
        });
    }
    
    
    /**
     * confirms with user an action that deprecates all current word forms
     * @return user choice yes/no
     */
    private boolean confirmDeprecate() {
        boolean ret = false;
        
        if (InfoBox.yesNoCancel("Confirm action", "This action will deprecate all currently filled out \n"
                + " declensions/conjugations (they won't be lost, but set to a deprecated\nstatus). Continue?", this) == JOptionPane.YES_OPTION) {
            ret = true;
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
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstDeclensionList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDeclensionNotes = new javax.swing.JTextArea();
        txtDeclensionName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnAddDimension = new javax.swing.JButton();
        btnDelDimension = new javax.swing.JButton();
        sclDimensions = new javax.swing.JScrollPane();
        tblDimensions = new javax.swing.JTable();
        btnDeleteDeclension = new javax.swing.JButton();
        btnAddDeclension = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Conjugations");

        lstDeclensionList.setToolTipText("Conjugation class");
        lstDeclensionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstDeclensionListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstDeclensionList);

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel2.setText("Name");

        txtDeclensionNotes.setColumns(20);
        txtDeclensionNotes.setLineWrap(true);
        txtDeclensionNotes.setRows(5);
        txtDeclensionNotes.setWrapStyleWord(true);
        txtDeclensionNotes.setDragEnabled(false);
        txtDeclensionNotes.setEnabled(false);
        txtDeclensionNotes.setMinimumSize(new java.awt.Dimension(0, 0));
        jScrollPane2.setViewportView(txtDeclensionNotes);

        txtDeclensionName.setEnabled(false);
        txtDeclensionName.setMinimumSize(new java.awt.Dimension(0, 0));

        jLabel3.setText("Dimensions");

        jLabel4.setText("Notes");

        btnAddDimension.setText("+");
        btnAddDimension.setToolTipText("Add a dimension to selected conjugation class");
        btnAddDimension.setEnabled(false);
        btnAddDimension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDimensionActionPerformed(evt);
            }
        });

        btnDelDimension.setText("-");
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(0, 131, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDeclensionName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnAddDimension, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 120, Short.MAX_VALUE)
                                .addComponent(btnDelDimension, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(sclDimensions, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtDeclensionName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sclDimensions, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDelDimension)
                    .addComponent(btnAddDimension))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnDeleteDeclension.setText("-");
        btnDeleteDeclension.setToolTipText("Delete selected conjugation class");
        btnDeleteDeclension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteDeclensionActionPerformed(evt);
            }
        });

        btnAddDeclension.setText("+");
        btnAddDeclension.setToolTipText("Add conjugation class");
        btnAddDeclension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDeclensionActionPerformed(evt);
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
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnDeleteDeclension)
                            .addComponent(btnAddDeclension))))
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

    public static ScrDeclensionSetup run(final DictCore _core, final Integer _typeId) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ScrDeclensionSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ScrDeclensionSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ScrDeclensionSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ScrDeclensionSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        ScrDeclensionSetup s = new ScrDeclensionSetup(_core, _typeId);
        
        s.setModal(true);
        s.setVisible(true);
        
        return s;
    }

    /**
     * adds a declension to the list and readies the system to create it in the core on modification
     */
    private void addDeclension() {
        // confirm user is will to deprecate all existing forms
        if (!confirmDeprecate()) {
            return;
        }
        
        // deprecate all existing forms
        core.getDeclensionManager().deprecateAllDeclensions();
        
        if (lstDeclensionList.getModel().getSize() != 0
                && scrToCoreDeclensions.containsKey((Integer) lstDeclensionList.getSelectedIndex())
                && (Integer) scrToCoreDeclensions.get((Integer) lstDeclensionList.getSelectedIndex()) == -1) {
            return;
        }

        boolean localPopulating = curPopulating;

        curPopulating = true;

        clearDeclensionProps();
        Integer declIndex = declListModel.getSize();
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
        DeclensionNode curDec = new DeclensionNode(-1);
        Integer decIndex = lstDeclensionList.getSelectedIndex();

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

        Integer decId = (Integer) scrToCoreDeclensions.get(decIndex);

        if (decId != null && decId != -1) {
            try {
                curDec = core.getDeclensionTemplate(myType.getId(), decId);
            } catch (Exception e) {
                InfoBox.error("Declension Population Error", "Unable to populate declension.\n\n"
                        + e.getMessage(), this);
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

        curPopulating = populatingLocal;
    }

    /**
     * Sets the dimension controls active if appropriate, inactive otherwise
     */
    private void setIsActiveDimensions() {
        Integer decId = scrToCoreDeclensions.containsKey(lstDeclensionList.getSelectedIndex())
                ? (Integer) scrToCoreDeclensions.get(lstDeclensionList.getSelectedIndex()) : -1;

        if (decId == -1) {
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
        Integer decIndex = lstDeclensionList.getSelectedIndex();

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
        core.getDeclensionManager().deprecateAllDeclensions();
        
        Integer curIndex = lstDeclensionList.getSelectedIndex();

        try {
            core.getDeclensionManager().deleteDeclensionFromTemplate(myType.getId(), (Integer) scrToCoreDeclensions.get(curIndex));
        } catch (Exception e) {
            InfoBox.error("Declension Deletion Error", "Unable to delete Declension: "
                    + (String) lstDeclensionList.getSelectedValue() + "\n\n" + e.getMessage(), this);
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

        Integer decId = scrToCoreDeclensions.containsKey(decIndex)
                ? (Integer) scrToCoreDeclensions.get(decIndex) : -1;

        DeclensionNode decl;

        try {
            // split logic for creating, rather than modifying Declension
            if (decId == -1) {
                decl = core.getDeclensionManager().addDeclensionToTemplate(myType.getId(), txtDeclensionName.getText());
                decl.setValue(txtDeclensionName.getText().trim());
                decl.setNotes(txtDeclensionNotes.getText().trim());

                scrToCoreDeclensions.put(decIndex, decl.getId());
                scrDeclensionMap.put(decl.getValue(), decIndex);
            } else {
                decl = new DeclensionNode(-1);
                DeclensionNode oldDecl = core.getDeclensionManager().getDeclension(myType.getId(), decId);

                decl.setEqual(oldDecl);

                decl.setValue(txtDeclensionName.getText().trim());
                decl.setNotes(txtDeclensionNotes.getText().trim());

                core.getDeclensionManager().updateDeclensionTemplate(myType.getId(), decId, decl);
            }
        } catch (Exception e) {
            InfoBox.error("Declension Creation Error", "Unable to create Declension "
                    + txtDeclensionName.getText() + "\n\n" + e.getMessage(), this);
        }

        setIsActiveDimensions();

        curPopulating = false;
    }

    private void populateDeclensionList() {
        // avoid recursive population
        if (curPopulating) {
            return;
        }

        Integer curdecId = (Integer) scrToCoreDeclensions.get(lstDeclensionList.getSelectedIndex());
        Integer setIndex = -1;

        curPopulating = true;

        Iterator<DeclensionNode> declIt = core.getDeclensionManager()
                .getDeclensionListTemplate(myType.getId()).iterator();
        DeclensionNode curdec;

        // relevant objects should be rebuilt
        scrDeclensionMap = new HashMap<String, Integer>();
        scrToCoreDeclensions = new HashMap<Integer, Integer>();

        declListModel.clear();

        for (int i = 0; declIt.hasNext(); i++) {
            curdec = declIt.next();

            scrDeclensionMap.put(curdec.getValue(), i);

            declListModel.add(i, curdec.getValue());
            scrToCoreDeclensions.put(i, curdec.getId());

            // replaced call to Object type here
            if (curdecId != null
                    && curdecId.equals(curdec.getId())) {
                setIndex = i;
            }
        }

        lstDeclensionList.setSelectedIndex(setIndex);

        curPopulating = false;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDeclension;
    private javax.swing.JButton btnAddDimension;
    private javax.swing.JButton btnDelDimension;
    private javax.swing.JButton btnDeleteDeclension;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lstDeclensionList;
    private javax.swing.JScrollPane sclDimensions;
    private javax.swing.JTable tblDimensions;
    private javax.swing.JTextField txtDeclensionName;
    private javax.swing.JTextArea txtDeclensionNotes;
    // End of variables declaration//GEN-END:variables
}
