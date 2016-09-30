/*
 * Copyright (c) 2016, draque.thompson
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
import PolyGlot.CustomControls.PTableModel;
import PolyGlot.DictCore;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.Nodes.WordPropValueNode;
import PolyGlot.Nodes.WordProperty;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.CellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author draque.thompson
 */
public class ScrWordProperties extends PFrame {

    private final Map<Integer, JCheckBox> typeChecks = new HashMap<>();

    public ScrWordProperties(DictCore _core) {
        core = _core;
        initComponents();
        setupKeyStrokes();
        populateTypes();
        populateWordProperties();
        setupComponents();
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

                if (prop != null) {
                    prop.setValue(txtName.getText());
                    lstProperties.repaint();
                }
            }
        });

        tblValues.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    @Override
    public final void setupKeyStrokes() {
        super.setupKeyStrokes();
    }

    /**
     * Sets up type checkboxes.
     */
    private void populateTypes() {
        Iterator<TypeNode> types = core.getTypes().getNodeIterator();
        pnlTypes.setLayout(new GridLayout(0, 1));

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
                }
            });

            pnlTypes.add(checkAll);
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
            
            pnlTypes.add(checkType);
            typeChecks.put(typeId, checkType);
        }

        pnlTypes.setVisible(false);
        pnlTypes.setVisible(true);
    }

    private void populateWordProperties() {
        Iterator<WordProperty> it = core.getWordPropertiesCollection().getAllWordProperties();
        DefaultListModel listModel = new DefaultListModel();

        while (it.hasNext()) {
            WordProperty curNode = it.next();
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

        CellEditor cellEditor = tblValues.getCellEditor(); // TODO: DELETE BLOCK?
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }

        if (curProp == null) {
            txtName.setText("");
            enableValues(false);
            for (JCheckBox checkBox : typeChecks.values()) {
                checkBox.setSelected(false);
            }
        } else {
            PTableModel tableModel = new PTableModel(new Object[]{"Values"}, 0);
            enableValues(true);

            // set name
            txtName.setText(curProp.getValue());

            Iterator<WordPropValueNode> it = curProp.getValues();
            // add property values
            
            while (it.hasNext()) {
                tableModel.addRow(new Object[]{it.next()});
            }
            
            // set checkboxes for types this applies to
            for (Entry<Integer, JCheckBox> e : typeChecks.entrySet()) {
                e.getValue().setSelected(curProp.appliesToType(e.getKey()));
            }

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
            InfoBox.error("Property Creation Error", "Unable to create new word property: " + e.getLocalizedMessage(), this);
            return;
        }

        DefaultListModel listModel = (DefaultListModel) lstProperties.getModel();
        listModel.addElement(prop);
        lstProperties.setSelectedValue(prop, true);
    }

    private void deleteWordProperty() {
        WordProperty prop = lstProperties.getSelectedValue();
        int position = lstProperties.getSelectedIndex();

        if (prop == null) {
            return;
        }

        try {
            core.getWordPropertiesCollection().deleteNodeById(prop.getId());
        } catch (Exception e) {
            InfoBox.error("Unable to Delete", "Unable to delete property: " + e.getLocalizedMessage(), this);
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
            InfoBox.error("Value Add Error", e.getLocalizedMessage(), this);
            return;
        }

        tableModel.addRow(new Object[]{value});
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

        jScrollPane1 = new javax.swing.JScrollPane();
        lstProperties = new javax.swing.JList<>();
        btnAddProp = new PButton("+");
        btnDelProp = new PButton("-");
        jPanel1 = new javax.swing.JPanel();
        txtName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnAddValue = new PButton("+");
        btnDelValue = new PButton("-");
        pnlTypes = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblValues = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lstProperties.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstPropertiesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstProperties);

        btnAddProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPropActionPerformed(evt);
            }
        });

        btnDelProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelPropActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel1.setText("Values");

        btnAddValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddValueActionPerformed(evt);
            }
        });

        btnDelValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelValueActionPerformed(evt);
            }
        });

        pnlTypes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel2.setText("Apply to parts of Speech");

        javax.swing.GroupLayout pnlTypesLayout = new javax.swing.GroupLayout(pnlTypes);
        pnlTypes.setLayout(pnlTypesLayout);
        pnlTypesLayout.setHorizontalGroup(
            pnlTypesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTypesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(172, Short.MAX_VALUE))
        );
        pnlTypesLayout.setVerticalGroup(
            pnlTypesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTypesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblValues.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Value"
            }
        ));
        jScrollPane3.setViewportView(tblValues);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAddValue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtName)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(77, 77, 77)
                                .addComponent(btnDelValue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTypes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(pnlTypes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddValue)
                            .addComponent(btnDelValue)))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAddProp, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDelProp, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDelProp, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnAddProp)))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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

    static ScrWordProperties run(DictCore _core) {
        return new ScrWordProperties(_core);
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // This doesn't currently need to do anything
    }

    @Override
    public boolean thisOrChildrenFocused() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        addWordProperty();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProp;
    private javax.swing.JButton btnAddValue;
    private javax.swing.JButton btnDelProp;
    private javax.swing.JButton btnDelValue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList<WordProperty> lstProperties;
    private javax.swing.JPanel pnlTypes;
    private javax.swing.JTable tblValues;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables
}
