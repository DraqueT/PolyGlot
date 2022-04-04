/*
 * Copyright (c) 2014-2022, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCellEditor;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCellRenderer;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PList;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTableModel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.Nodes.WordClassValue;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box.Filler;
import javax.swing.CellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;

/**
 *
 * @author draque.thompson
 */
public final class ScrWordClasses extends PFrame {

    private final Map<Integer, PCheckBox> typeChecks = new HashMap<>();

    public ScrWordClasses(DictCore _core) {
        super(_core);
        
        initComponents();
        populateTypes();
        populateWordProperties();
        populatePropertyValues();
        setupComponents();
        super.setPreferredSize(new Dimension(584, 377));
        setupForm();
        setupListeners();
        setLegal();
    }
    
    private void setupForm() {
        int divider = PolyGlot.getPolyGlot().getOptionsManager().getDividerPosition(this.getClass().getName());
        
        if (divider > -1) {
            jSplitPane1.setDividerLocation(divider);
        }
    }

    private void setupComponents() {
        txtName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                WordClass prop = lstProperties.getSelectedValue();

                if (prop != null && !((PTextField)txtName).isDefaultText()) {
                    prop.setValue(txtName.getText());
                    lstProperties.repaint();
                }
            }
        });

        tblValues.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        TableColumn column = tblValues.getColumnModel().getColumn(0);
        column.setCellEditor(new PCellEditor(false, core));
        column.setCellRenderer(new PCellRenderer(false, core));
        jScrollPane3.getViewport().setBackground(Color.white);// colors the BG of the table white
        this.setBackground(Color.white);
    }
    /**
     * Make certain editing finalized before leaving window
     */
    @Override
    public void dispose() {
        if (tblValues.getCellEditor() != null) {
            tblValues.getCellEditor().stopCellEditing();
        }
        
        PolyGlot.getPolyGlot().getOptionsManager().setDividerPosition(getClass().getName(), jSplitPane1.getDividerLocation());
        super.dispose();
    }
    
    @Override
    public void saveAllValues() {
        // not needed - saved in real time
    }
    
    private void setLegal() {
        if (txtName.getText().isBlank() && lstProperties.getModel().getSize() > 0) {
            txtName.setBackground(PGTUtil.COLOR_REQUIRED_LEX_COLOR);
            btnAddProp.setEnabled(false);
        } else {
            txtName.setBackground(PGTUtil.COLOR_TEXT_BG);
            btnAddProp.setEnabled(true);
        }
    }

    /**
     * Sets up type checkboxes.
     */
    private void populateTypes() {
        TypeNode[] types = core.getTypes().getNodes();
        GridBagConstraints gbc = new GridBagConstraints();
        
        pnlTypes.removeAll();
        pnlTypes.setLayout(new GridBagLayout());
        
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1;
        
        if (types.length > 0) {
            final PCheckBox checkAll = new PCheckBox(nightMode);
            checkAll.setText("All");

            checkAll.addItemListener(new ItemListener() {
                final PCheckBox thisBox = checkAll;

                @Override
                public void itemStateChanged(ItemEvent e) {
                    WordClass prop = lstProperties.getSelectedValue();
                    
                    if (prop != null) {
                        if (thisBox.isSelected()) {
                            prop.addApplyType(-1);                       
                        } else {
                            prop.deleteApplyType(-1);
                        }
                    }
                    
                    setEnabledTypeText();
                }
            });
            
            checkAll.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            checkAll.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            checkAll.setToolTipText("Apply to all parts of speech");
            pnlTypes.add(checkAll, gbc);
            gbc.gridx = 1;
            gbc.weightx = 9999;
            pnlTypes.add(new Filler(new Dimension(0,0),new Dimension(9999,9999),new Dimension(9999,9999)), gbc);
            gbc.gridy = 1;
            typeChecks.put(-1, checkAll);
        }

        for (TypeNode curNode : types) {
            final int typeId = curNode.getId();
            final PCheckBox checkType = new PCheckBox(nightMode);
            
            checkType.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal());
            checkType.setText(curNode.getValue());
            checkType.addItemListener(new ItemListener() {
                final PCheckBox thisBox = checkType;
                final int thisTypeId = typeId;

                @Override
                public void itemStateChanged(ItemEvent e) {
                    WordClass prop = lstProperties.getSelectedValue();
                    
                    if (prop != null) {
                        if (thisBox.isSelected()) {
                            prop.addApplyType(typeId);
                        } else {
                            prop.deleteApplyType(typeId);
                        }
                    }
                }
            });
            
            checkType.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            checkType.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            checkType.setToolTipText("Apply to " + curNode.getValue());
            gbc.gridx = 0;
            gbc.weightx = 1;
            pnlTypes.add(checkType, gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            pnlTypes.add(new Filler(new Dimension(0,0),new Dimension(999,999),new Dimension(9999,9999)), gbc);
            gbc.gridy++;
            typeChecks.put(typeId, checkType);
        }
        
        // eats up space at bottom
        gbc.weighty = 999;
        pnlTypes.add(new Filler(new Dimension(0,0),new Dimension(999,999),new Dimension(9999,9999)), gbc);

        pnlTypes.setVisible(false);
        pnlTypes.setVisible(true);
    }
    
    private void setEnabledTypeText() {
        // if "ALL" is selected, disable other choices, as they are redundant
        if (typeChecks.containsKey(-1)) {
            PCheckBox all = typeChecks.get(-1);
            typeChecks.values().forEach((check) -> {
                check.setEnabled(!all.isSelected());
            });
            // "ALL should always be enabled
            all.setEnabled(true);
        }
    }
    
    private void setupListeners() {
        txtName.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                setLegal();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setLegal();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setLegal();
            }
        });
    }

    private void populateWordProperties() {
        DefaultListModel<WordClass> listModel = new DefaultListModel<>();

        for (WordClass curNode : core.getWordClassCollection().getAllWordClasses()) {
            listModel.addElement(curNode);
        }

        lstProperties.setModel(listModel);
        lstProperties.setSelectedIndex(0);
    }

    /**
     * Populates all values associated with property
     */
    private void populatePropertyValues() {
        WordClass curProp = lstProperties.getSelectedValue();

        CellEditor cellEditor = tblValues.getCellEditor();
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }

        if (curProp == null) {
            ((PTextField)txtName).setDefault();
            enableValues(false);
            typeChecks.values().forEach((checkBox) -> {
                checkBox.setSelected(false);
            });
            PTableModel tableModel = new PTableModel(new Object[]{"Values"}, 0);
            tblValues.setModel(tableModel);
        } else {
            PTableModel tableModel = new PTableModel(new Object[]{"Values"}, 0);
            enableValues(true);

            // set name
            txtName.setText(curProp.getValue());
            
            chkFreeText.setSelected(curProp.isFreeText());
            chkAssociative.setSelected(curProp.isAssociative());

            // add property values
            curProp.getValues().forEach((curValue) -> {
                tableModel.addRow(new Object[]{curValue});
            });
            
            // set checkboxes for types this applies to
            typeChecks.entrySet().forEach((e) -> {
                e.getValue().setSelected(curProp.appliesToType(e.getKey()));
            });

            enableProperMenuItems();
            setEnabledTypeText();
            tblValues.setModel(tableModel);
        }
    }

    /**
     * Enables all values for properties (disable when none selected)
     *
     * @param enable
     */
    private void enableValues(boolean enable) {
        txtName.setEnabled(enable);
        tblValues.setEnabled(enable);
        chkFreeText.setEnabled(enable);
        chkAssociative.setEnabled(enable);
        btnAddValue.setEnabled(enable);
        btnDelValue.setEnabled(enable);
        
        typeChecks.values().forEach((curBox) -> {
            curBox.setEnabled(enable);
        });
    }

    private void addWordProperty() {
        int propId;
        WordClass prop;

        try {
            propId = core.getWordClassCollection().addNode(new WordClass());
            prop = (WordClass) core.getWordClassCollection().getNodeById(propId);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Property Creation Error", 
                    "Unable to create new word property: " + e.getLocalizedMessage());
            return;
        }

        DefaultListModel<WordClass> listModel = (DefaultListModel<WordClass>) lstProperties.getModel();
        listModel.addElement(prop);
        lstProperties.setSelectedValue(prop, true);
    }

    private void deleteWordProperty() {
        WordClass prop = lstProperties.getSelectedValue();
        int position = lstProperties.getSelectedIndex();

        if (prop == null || core.getOSHandler().getInfoBox().yesNoCancel("Are you sure?", "This will delete the class from all words."
                + " Values will be irretrievably lost.") != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            core.getWordClassCollection().deleteNodeById(prop.getId());
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Unable to Delete", 
                    "Unable to delete property: " + e.getLocalizedMessage());
        }
        DefaultListModel listModel = (DefaultListModel) lstProperties.getModel();
        listModel.removeElement(prop);

        if (position == 0) {
            lstProperties.setSelectedIndex(position);
        } else {
            lstProperties.setSelectedIndex(position - 1);
        }
    }

    private void addPropertyValue() {
        PTableModel tableModel = (PTableModel) tblValues.getModel();
        WordClass curProp = lstProperties.getSelectedValue();

        if (curProp == null) {
            return;
        }

        WordClassValue value;

        try {
            value = curProp.addValue("");
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Value Add Error", 
                    e.getLocalizedMessage());
            return;
        }

        tableModel.addRow(new Object[]{value});
        txtName.requestFocus();
    }

    private void delPropertyValue() {
        PTableModel tableModel = (PTableModel) tblValues.getModel();
        WordClass curProp = lstProperties.getSelectedValue();

        if (tblValues.getCellEditor() != null) {
            tblValues.getCellEditor().stopCellEditing();
        }

        int index = tblValues.getSelectedRow();

        if (index >= 0) {
            WordClassValue value = (WordClassValue)tableModel.getValueAt(index, 0);
            try {
                curProp.deleteValue(value.getId());
            } catch (Exception e) {
                // do nothing. if it doesn't exist, deleting it is fine.
                // IOHandler.writeErrorLog(e);
            }
            tableModel.removeRow(index);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstProperties = new PList(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal());
        btnAddProp = new PAddRemoveButton("+");
        btnDelProp = new PAddRemoveButton("-");
        jPanel1 = new javax.swing.JPanel();
        txtName = new PTextField(core, true, "Name");
        btnAddValue = new PAddRemoveButton("+");
        btnDelValue = new PAddRemoveButton("-");
        pnlTypes = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblValues = new javax.swing.JTable();
        chkFreeText = new PCheckBox(nightMode);
        chkAssociative = new PCheckBox(false);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Word Classes");
        setBackground(new java.awt.Color(255, 255, 255));

        jSplitPane1.setDividerLocation(150);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        lstProperties.setToolTipText("Properties (such as gender) of words in your language");
        lstProperties.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstPropertiesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstProperties);

        btnAddProp.setBackground(new java.awt.Color(255, 255, 255));
        btnAddProp.setToolTipText("Add new property");
        btnAddProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPropActionPerformed(evt);
            }
        });

        btnDelProp.setBackground(new java.awt.Color(255, 255, 255));
        btnDelProp.setToolTipText("Delete selected property");
        btnDelProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelPropActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(btnAddProp, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDelProp, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddProp, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnDelProp, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        jSplitPane1.setLeftComponent(jPanel2);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.setToolTipText("");

        txtName.setToolTipText("Name of property");

        btnAddValue.setToolTipText("Add new value");
        btnAddValue.setEnabled(false);
        btnAddValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddValueActionPerformed(evt);
            }
        });

        btnDelValue.setToolTipText("Delete selected value");
        btnDelValue.setEnabled(false);
        btnDelValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelValueActionPerformed(evt);
            }
        });

        pnlTypes.setBackground(new java.awt.Color(255, 255, 255));
        pnlTypes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlTypes.setToolTipText("Parts of speech this property applies to");

        javax.swing.GroupLayout pnlTypesLayout = new javax.swing.GroupLayout(pnlTypes);
        pnlTypes.setLayout(pnlTypesLayout);
        pnlTypesLayout.setHorizontalGroup(
            pnlTypesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 182, Short.MAX_VALUE)
        );
        pnlTypesLayout.setVerticalGroup(
            pnlTypesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        tblValues.setBackground(new java.awt.Color(220, 220, 220));
        tblValues.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Values"
            }
        ));
        tblValues.setToolTipText("Values the selected property might take (such as \"female\" for gender)");
        jScrollPane3.setViewportView(tblValues);

        chkFreeText.setText("Free Text Value");
        chkFreeText.setToolTipText("Check to make this class value a freely editable text field, rather than a dropdown with preset values.");
        chkFreeText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkFreeTextActionPerformed(evt);
            }
        });

        chkAssociative.setText("Associative");
        chkAssociative.setToolTipText("Check this if the class associates a word with one other (ex: antonyms)");
        chkAssociative.setEnabled(false);
        chkAssociative.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAssociativeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAddValue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(47, 47, 47)
                        .addComponent(btnDelValue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(chkFreeText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtName)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(chkAssociative, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTypes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlTypes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkFreeText)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkAssociative)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAddValue, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnDelValue, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddPropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPropActionPerformed
        addWordProperty();
        txtName.requestFocus();
        setLegal();
    }//GEN-LAST:event_btnAddPropActionPerformed

    private void btnDelPropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelPropActionPerformed
        deleteWordProperty();
        setLegal();
    }//GEN-LAST:event_btnDelPropActionPerformed

    private void lstPropertiesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstPropertiesValueChanged
        populatePropertyValues();
    }//GEN-LAST:event_lstPropertiesValueChanged

    private void btnAddValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddValueActionPerformed
        addPropertyValue();
    }//GEN-LAST:event_btnAddValueActionPerformed

    private void btnDelValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelValueActionPerformed
        delPropertyValue();
    }//GEN-LAST:event_btnDelValueActionPerformed

    private void chkFreeTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkFreeTextActionPerformed
        WordClass prop = lstProperties.getSelectedValue();
        prop.setFreeText(chkFreeText.isSelected());
        enableProperMenuItems();
        
        if (chkFreeText.isSelected()) {
            chkAssociative.setSelected(false);
        }
    }//GEN-LAST:event_chkFreeTextActionPerformed

    private void chkAssociativeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAssociativeActionPerformed
        WordClass prop = lstProperties.getSelectedValue();
        prop.setAssociative(chkAssociative.isSelected());
        enableProperMenuItems();
        
        if (chkAssociative.isSelected()) {
            chkFreeText.setSelected(false);
        }
    }//GEN-LAST:event_chkAssociativeActionPerformed

    private void enableProperMenuItems() {
        boolean enable = !chkFreeText.isSelected() && !chkAssociative.isSelected();
        tblValues.setEnabled(enable);
        btnAddValue.setEnabled(enable);
        btnDelValue.setEnabled(enable);
    }
    
    static ScrWordClasses run(DictCore _core) {
        return new ScrWordClasses(_core);
    }

    @Override
    public void updateAllValues(DictCore _core) {
        core = _core;
        populateTypes();
        populateWordProperties();
        populatePropertyValues();        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProp;
    private javax.swing.JButton btnAddValue;
    private javax.swing.JButton btnDelProp;
    private javax.swing.JButton btnDelValue;
    private javax.swing.JCheckBox chkAssociative;
    private javax.swing.JCheckBox chkFreeText;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JList<WordClass> lstProperties;
    private javax.swing.JPanel pnlTypes;
    private javax.swing.JTable tblValues;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        // none to add
    }

    @Override
    public Component getWindow() {
        return this.getRootPane();
    }
}
