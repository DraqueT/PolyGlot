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
import PolyGlot.DictCore;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.Nodes.WordProperty;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 *
 * @author draque.thompson
 */
public class ScrWordProperties extends PFrame {
    
    private List<JCheckBox> typeChecks = new ArrayList<>();

    public ScrWordProperties(DictCore _core) {
        core = _core;
        initComponents();
        setupKeyStrokes();
        populateTypes();
        populateValues();
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
                    // TODO: add "check all" here
                }
            });
            
            pnlTypes.add(checkAll);
            typeChecks.add(checkAll);
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
                    // TODO: selection event update here
                }
            });

            pnlTypes.add(checkType);
        }

        pnlTypes.setVisible(false);
        pnlTypes.setVisible(true);
    }

    private void populateValues() {
        Iterator<WordProperty> it = core.getWordPropertiesCollection().getAllWordProperties();
        DefaultListModel listModel = new DefaultListModel();
        
        while (it.hasNext()) {
            WordProperty curNode = it.next();
            listModel.addElement(curNode);
        }
        
        lstProperties.setModel(listModel);
        lstProperties.setSelectedIndex(0);
    }
    
    private void addWordProperty() {
        int propId;
        WordProperty prop;
        
        try {
            propId = core.getWordPropertiesCollection().addNode(new WordProperty());
            prop = (WordProperty)core.getWordPropertiesCollection().getNodeById(propId);
        } catch (Exception e) {
            InfoBox.error("Property Creation Error", "Unable to create new word property: " + e.getLocalizedMessage(), this);
            return;
        }        
        prop.setValue("foo");
        DefaultListModel listModel = (DefaultListModel)lstProperties.getModel();
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
        DefaultListModel listModel = (DefaultListModel)lstProperties.getModel();
        listModel.removeElement(prop);
        
        if (position == 0) {
            lstProperties.setSelectedIndex(position);
        } else {
            lstProperties.setSelectedIndex(position - 1);
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
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jButton1 = new PButton("+");
        jButton2 = new PButton("-");
        pnlTypes = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

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

        jScrollPane2.setViewportView(jList2);

        pnlTypes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel2.setText("Apply to parts of Speech");

        javax.swing.GroupLayout pnlTypesLayout = new javax.swing.GroupLayout(pnlTypes);
        pnlTypes.setLayout(pnlTypesLayout);
        pnlTypesLayout.setHorizontalGroup(
            pnlTypesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTypesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(53, Short.MAX_VALUE))
        );
        pnlTypesLayout.setVerticalGroup(
            pnlTypesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTypesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(jButton2)))))
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
                .addComponent(jScrollPane1)
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
    private javax.swing.JButton btnDelProp;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList<String> jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JList<WordProperty> lstProperties;
    private javax.swing.JPanel pnlTypes;
    // End of variables declaration//GEN-END:variables
}
