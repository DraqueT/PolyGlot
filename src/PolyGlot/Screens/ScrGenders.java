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

import PolyGlot.DictCore;
import PolyGlot.Nodes.GenderNode;
import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PButton;
import PolyGlot.CustomControls.PDialog;
import java.awt.Color;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author draque
 */
public class ScrGenders extends PDialog {
    private final DictCore core;
    private static final String defName = " -- Gender Name --";
    private static final String defNotes = " -- Gender Notes --";
    private boolean updatingName = false;
    
    public ScrGenders(DictCore _core) {
        initComponents();
        
        core = _core;
        
        populateGenders();
        populateProperties();
        setupListeners();
        setModal(true);
    }
    
    @Override
    public void dispose() {
        GenderNode curGender = (GenderNode)lstGenders.getSelectedValue();
        if (curGender != null) {
            savePropertiesTo(curGender);
        }
        // TODO: signal core to updae relevant windows
        
        if (txtName.getText().equals("")
                && lstGenders.getSelectedIndex() != -1) {
            InfoBox.warning("Illegal Gender",
                    "Currently selected gender is illegal. Please correct or delete.", this);
        } else {
            super.dispose();
        }
    }
    
    @Override
    public void updateAllValues(DictCore _core) {
        // due to modal nature of screen, nothing to update
    }
    
    /**
     * Sets up object listeners
     */
    private void setupListeners() {
        txtName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateName();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateName();
            }
        });
    }
    
    /**
     * Updates name value so that display can populate properly
     */
    private void updateName() {
        GenderNode curNode = (GenderNode)lstGenders.getSelectedValue();
        
        if (txtName.getText().equals("") || txtName.getText().equals(defName)) {
            txtName.setBackground(core.getRequiredColor());
            lstGenders.setEnabled(false);
        } else {
            lstGenders.setEnabled(true);
            txtName.setBackground(new JTextField().getBackground());
        }
        
        if (updatingName || curNode == null) {
            return;
        }
        updatingName = true;
        curNode.setValue(txtName.getText().equals(defName) ? 
                "" : txtName.getText());
        
        populateGenders();
        lstGenders.setSelectedValue(curNode, true);
        updatingName = false;
        core.pushUpdate();
    }
    
    /**
     * Clears all current genders and re-populates values, selecting first value
     */
    private void populateGenders() {
        Iterator<GenderNode> gendIt = core.getGenders().getNodeIterator();
        
        try {
            DefaultListModel listModel = new DefaultListModel();
            
            while (gendIt.hasNext()) {
                listModel.addElement(gendIt.next());
            }
            
            lstGenders.setModel(listModel);
            lstGenders.setSelectedIndex(0);
            lstGenders.ensureIndexIsVisible(0);
        } catch (Exception e) {
            InfoBox.error("Gender Population Error", "Unable to populate genders: " 
                    + e.getLocalizedMessage(), this);
        }
    }
    
    /**
     * Populates properties of currently selected gender, if any
     */
    private void populateProperties() {
        GenderNode curNode = (GenderNode)lstGenders.getSelectedValue();
        
        if (curNode == null) {
            if (!updatingName) {
                updatingName = true;
                txtName.setText("");
                updatingName = false;
            }
            txtName.setForeground(Color.lightGray);
            txtNotes.setText("");
            txtNotes.setForeground(Color.lightGray);
            setPropertiesEnabled(false);
        } else {
            if (!updatingName) {
                updatingName = true;
                txtName.setText(curNode.getValue().equals("") ?
                        defName : curNode.getValue());
                txtName.setForeground(curNode.getValue().equals("") ?
                        Color.lightGray : Color.black);
                updatingName = false;
            }
            txtNotes.setText(curNode.getNotes().equals("") ?
                    defNotes : curNode.getNotes());
            txtNotes.setForeground(curNode.getNotes().equals("") ?
                        Color.lightGray : Color.black);
            setPropertiesEnabled(true);
        }
    }
    
    /**
     * Saves properties to given node
     * @param saveNode node to save to
     */
    private void savePropertiesTo(GenderNode saveNode) {
        saveNode.setValue(txtName.getText().equals(defName) ?
                "" : txtName.getText());
        saveNode.setNotes(txtNotes.getText().equals(defNotes) ?
                "" : txtNotes.getText());
        core.pushUpdate();
    }
    
    /**
     * creates blank gender, selects value for editing
     */
    private void addGender() {
        GenderNode curGend = (GenderNode)lstGenders.getSelectedValue();
        
        if (curGend != null) {
            savePropertiesTo(curGend);
        }
        
        core.getGenders().clear();
        try {
            core.getGenders().insert();
        } catch (Exception e) {
            InfoBox.error("Gender Creation Error", "Could not create new gender: " 
                    + e.getLocalizedMessage(), this);
        }
        populateGenders();
        lstGenders.setSelectedIndex(0);        
        txtName.requestFocus();
    }
    
    /**
     * deletes currently selected gender
     */
    private void deleteGender() {
        GenderNode curGend = (GenderNode)lstGenders.getSelectedValue();
        
        if (curGend == null) {
            return;
        }
        
        try {
            core.getGenders().deleteNodeById(curGend.getId());
        } catch (Exception e) {
            InfoBox.error("Deletion Error", "Unable to delete gender." + e.getLocalizedMessage(), this);
        }
        
        populateGenders();
        populateProperties();
    }
    
    /**
     * Sets gender properties controls enabled or disables
     * 
     * @param enable: whether to enable properties
     */
    private void setPropertiesEnabled(boolean enable) {
        txtName.setEnabled(enable);
        txtNotes.setEnabled(enable);
    }
    
    public static ScrGenders run(DictCore _core) {
        ScrGenders s = new ScrGenders(_core);
        s.setupKeyStrokes();
        return s;
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
        jPanel1 = new javax.swing.JPanel();
        btnDelGend = new PButton("-");
        btnAddGend = new PButton("+");
        jScrollPane1 = new javax.swing.JScrollPane();
        lstGenders = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        txtName = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtNotes = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Genders");

        jSplitPane1.setDividerLocation(140);

        btnDelGend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelGendActionPerformed(evt);
            }
        });

        btnAddGend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddGendActionPerformed(evt);
            }
        });

        lstGenders.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstGendersValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstGenders);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnAddGend, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addComponent(btnDelGend, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddGend)
                    .addComponent(btnDelGend)))
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        txtName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNameFocusLost(evt);
            }
        });

        txtNotes.setColumns(20);
        txtNotes.setRows(5);
        txtNotes.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtNotesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNotesFocusLost(evt);
            }
        });
        jScrollPane2.setViewportView(txtNotes);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtName)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNameFocusGained
        if (txtName.getText().equals(defName)) {
            updatingName = true;
            txtName.setText("");
            updatingName = false;
            txtName.setForeground(Color.black);
        }
    }//GEN-LAST:event_txtNameFocusGained

    private void txtNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNameFocusLost
       if (txtName.getText().equals("")) {
            updatingName = true;
            txtName.setText(defName);
            updatingName = false;
            txtName.setForeground(Color.lightGray);
        }
    }//GEN-LAST:event_txtNameFocusLost

    private void txtNotesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNotesFocusGained
        if (txtNotes.getText().equals(defNotes)) {
            txtNotes.setText("");
            txtNotes.setForeground(Color.black);
        }
    }//GEN-LAST:event_txtNotesFocusGained

    private void txtNotesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNotesFocusLost
        if (txtNotes.getText().equals("")) {
            txtNotes.setText(defNotes);
            txtNotes.setForeground(Color.lightGray);
        }
    }//GEN-LAST:event_txtNotesFocusLost

    private void lstGendersValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstGendersValueChanged
        if (evt.getValueIsAdjusting()
                || updatingName) {
            return;
        }

        if (evt.getFirstIndex() != evt.getLastIndex()) {
            JList list = (JList) evt.getSource();
            int selected = list.getSelectedIndex();
            int index = selected == evt.getFirstIndex()
                    ? evt.getLastIndex() : evt.getFirstIndex();

            if (index != -1) {
                GenderNode curNode = (GenderNode) lstGenders.getModel().getElementAt(index);
                
                if (curNode != null) {
                    savePropertiesTo(curNode);
                }
            }
        }

        populateProperties();
    }//GEN-LAST:event_lstGendersValueChanged

    private void btnAddGendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddGendActionPerformed
        addGender();
    }//GEN-LAST:event_btnAddGendActionPerformed

    private void btnDelGendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelGendActionPerformed
        deleteGender();
    }//GEN-LAST:event_btnDelGendActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddGend;
    private javax.swing.JButton btnDelGend;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JList lstGenders;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextArea txtNotes;
    // End of variables declaration//GEN-END:variables
}
