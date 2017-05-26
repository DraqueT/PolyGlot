/*
 * Copyright (c) 2016 - 2017, draque.thompson
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
import PolyGlot.CustomControls.PCellEditor;
import PolyGlot.CustomControls.PCellRenderer;
import PolyGlot.CustomControls.PCheckBox;
import PolyGlot.CustomControls.PFrame;
import PolyGlot.CustomControls.PList;
import PolyGlot.CustomControls.PTableModel;
import PolyGlot.CustomControls.PTextField;
import PolyGlot.DictCore;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.Nodes.WordPropValueNode;
import PolyGlot.Nodes.WordProperty;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Box.Filler;
import javax.swing.CellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;

// TODO: make correct element autoselect after hitting + for immediate editing
// TODO: make proper choices when illegal value left in field

/**
 *
 * @author draque.thompson
 */
public class ScrWordClasses extends PFrame {

    private final Map<Integer, JCheckBox> typeChecks = new HashMap<>();

    public ScrWordClasses(DictCore _core) {
        core = _core;
        initComponents();
        setupKeyStrokes();
        populateTypes();
        populateWordProperties();
        populatePropertyValues();
        setupComponents();
        super.setPreferredSize(new Dimension(584, 377));
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
                WordProperty prop = lstProperties.getSelectedValue();

                if (prop != null && !((PTextField)txtName).isDefaultText()) {
                    prop.setValue(txtName.getText());
                    lstProperties.repaint();
                }
            }
        });

        tblValues.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        TableColumn column = tblValues.getColumnModel().getColumn(0);
        Font defFont = core.getPropertiesManager().getCharisUnicodeFont();
        column.setCellEditor(new PCellEditor(defFont, 0.0));
        column.setCellRenderer(new PCellRenderer(defFont, 0.0));
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
        
        super.dispose();
    }

    @Override
    public final void setupKeyStrokes() {
        super.setupKeyStrokes();
    }

    /**
     * Sets up type checkboxes.
     */
    private void populateTypes() {
        Iterator<TypeNode> types = core.getTypes().getNodes().iterator();
        GridBagConstraints gbc = new GridBagConstraints();
        
        pnlTypes.removeAll();
        pnlTypes.setLayout(new GridBagLayout());
        
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1;
        
        if (types.hasNext()) {
            final JCheckBox checkAll = new JCheckBox();
            checkAll.setText("All");

            checkAll.addItemListener(new ItemListener() {
                final JCheckBox thisBox = checkAll;

                @Override
                public void itemStateChanged(ItemEvent e) {
                    WordProperty prop = lstProperties.getSelectedValue();
                    
                    if (thisBox.isSelected()) {
                        prop.addApplyType(-1);                       
                    } else {
                        prop.deleteApplyType(-1);
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

        while (types.hasNext()) {
            TypeNode curNode = types.next();
            final int typeId = curNode.getId();
            final JCheckBox checkType = new JCheckBox();
            
            checkType.setText(curNode.getValue());
            checkType.addItemListener(new ItemListener() {
                final JCheckBox thisBox = checkType;
                final int thisTypeId = typeId;

                @Override
                public void itemStateChanged(ItemEvent e) {
                    WordProperty prop = lstProperties.getSelectedValue();
                    
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
            JCheckBox all = typeChecks.get(-1);
            for (JCheckBox check : typeChecks.values()) {
                check.setEnabled(!all.isSelected());
            }
            // "ALL should always be enabled
            all.setEnabled(true);
        }
    }

    private void populateWordProperties() {
        DefaultListModel listModel = new DefaultListModel();

        for (WordProperty curNode : core.getWordPropertiesCollection().getAllWordProperties()) {
            listModel.addElement(curNode);
        }

        lstProperties.setModel(listModel);
        lstProperties.setSelectedIndex(0);
    }

    /**
     * Populates all values associated with property
     */
    private void populatePropertyValues() {
        WordProperty curProp = lstProperties.getSelectedValue();

        CellEditor cellEditor = tblValues.getCellEditor();
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }

        if (curProp == null) {
            ((PTextField)txtName).setDefault();
            enableValues(false);
            for (JCheckBox checkBox : typeChecks.values()) {
                checkBox.setSelected(false);
            }
        } else {
            PTableModel tableModel = new PTableModel(new Object[]{"Values"}, 0);
            enableValues(true);

            // set name
            txtName.setText(curProp.getValue());
            
            chkFreeText.setSelected(curProp.isFreeText());

            // add property values
            for (WordPropValueNode curValue : curProp.getValues()) {
                tableModel.addRow(new Object[]{curValue});
            }
            
            // set checkboxes for types this applies to
            for (Entry<Integer, JCheckBox> e : typeChecks.entrySet()) {
                e.getValue().setSelected(curProp.appliesToType(e.getKey()));
            }

            setFreeTextEnabled();
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
        btnAddValue.setEnabled(enable);
        btnDelValue.setEnabled(enable);
        
        for (JCheckBox curBox : typeChecks.values()) {
            curBox.setEnabled(enable);
        }
    }

    private void addWordProperty() {
        int propId;
        WordProperty prop;

        try {
            propId = core.getWordPropertiesCollection().addNode(new WordProperty());
            prop = (WordProperty) core.getWordPropertiesCollection().getNodeById(propId);
        } catch (Exception e) {
            InfoBox.error("Property Creation Error", "Unable to create new word property: " + e.getLocalizedMessage(), core.getRootWindow());
            return;
        }

        DefaultListModel listModel = (DefaultListModel) lstProperties.getModel();
        listModel.addElement(prop);
        lstProperties.setSelectedValue(prop, true);
    }

    private void deleteWordProperty() {
        WordProperty prop = lstProperties.getSelectedValue();
        int position = lstProperties.getSelectedIndex();

        if (prop == null || InfoBox.yesNoCancel("Are you sure?", "This will delete the property from all words."
                + " Values will be irretrevably lost.", core.getRootWindow()) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            core.getWordPropertiesCollection().deleteNodeById(prop.getId());
        } catch (Exception e) {
            InfoBox.error("Unable to Delete", "Unable to delete property: " + e.getLocalizedMessage(), core.getRootWindow());
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
        WordProperty curProp = lstProperties.getSelectedValue();

        if (curProp == null) {
            return;
        }

        WordPropValueNode value;

        try {
            value = curProp.addValue("");
        } catch (Exception e) {
            InfoBox.error("Value Add Error", e.getLocalizedMessage(), core.getRootWindow());
            return;
        }

        tableModel.addRow(new Object[]{value});
        txtName.requestFocus();
    }

    private void delPropertyValue() {
        PTableModel tableModel = (PTableModel) tblValues.getModel();

        if (tblValues.getCellEditor() != null) {
            tblValues.getCellEditor().stopCellEditing();
        }

        int index = tblValues.getSelectedRow();

        if (index >= 0) {
            tableModel.removeRow(index);
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

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstProperties = new PList(core, false);
        btnAddProp = new PolyGlot.CustomControls.PAddRemoveButton("+");
        btnDelProp = new PolyGlot.CustomControls.PAddRemoveButton("-");
        jPanel1 = new javax.swing.JPanel();
        txtName = new PTextField(core, true, "-- Name --");
        btnAddValue = new PolyGlot.CustomControls.PAddRemoveButton("+");
        btnDelValue = new PolyGlot.CustomControls.PAddRemoveButton("-");
        pnlTypes = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblValues = new javax.swing.JTable();
        chkFreeText = new PCheckBox(core);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Word Classes");
        setBackground(new java.awt.Color(255, 255, 255));

        jSplitPane1.setDividerLocation(150);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

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
            .addGap(0, 244, Short.MAX_VALUE)
        );
        pnlTypesLayout.setVerticalGroup(
            pnlTypesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jScrollPane3.setBackground(new java.awt.Color(255, 255, 255));

        tblValues.setBackground(new java.awt.Color(220, 220, 220));
        tblValues.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Values"
            }
        ));
        tblValues.setToolTipText("Values the selected property might take (such as \"female\" for gender)");
        tblValues.setGridColor(new java.awt.Color(255, 255, 255));
        jScrollPane3.setViewportView(tblValues);

        chkFreeText.setText("Free Text Value");
        chkFreeText.setToolTipText("Check to make this class value a freely editable text field, rather than a dropdown with preset values.");
        chkFreeText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkFreeTextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAddValue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(47, 47, 47)
                        .addComponent(btnDelValue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkFreeText)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))))
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
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
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
    }//GEN-LAST:event_btnAddPropActionPerformed

    private void btnDelPropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelPropActionPerformed
        deleteWordProperty();
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
        WordProperty prop = lstProperties.getSelectedValue();
        prop.setFreeText(chkFreeText.isSelected());
        setFreeTextEnabled();
    }//GEN-LAST:event_chkFreeTextActionPerformed

    private void setFreeTextEnabled() {
        boolean enable = !chkFreeText.isSelected();
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

    @Override
    public boolean thisOrChildrenFocused() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProp;
    private javax.swing.JButton btnAddValue;
    private javax.swing.JButton btnDelProp;
    private javax.swing.JButton btnDelValue;
    private javax.swing.JCheckBox chkFreeText;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JList<WordProperty> lstProperties;
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
