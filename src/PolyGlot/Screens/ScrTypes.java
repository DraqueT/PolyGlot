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
import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PButton;
import PolyGlot.CustomControls.PDialog;
import PolyGlot.CustomControls.PTextField;
import PolyGlot.Nodes.TypeNode;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author draque
 */
public class ScrTypes extends PDialog {

    private final List<Window> childFrames = new ArrayList<>();
    private final String defName = " -- Part of Speech Name --";
    private final String defNotes = " -- Part of Speech Notes --";
    private final String defPattern = " -- Part of Speech Pattern --";
    private final String defGloss = " -- Part of Speech Gloss --";
    private TypeNode selectionAtClosing = null;
    private boolean updatingName = false;

    public ScrTypes(DictCore _core) {
        core = _core;
        initComponents();

        populateTypes();
        populateProperties();
        setupListeners();
        setModal(true);
    }
    
    // To avoid BP error
    @Override
    public final void setModal(boolean _modal) {
        super.setModal(_modal);
    }

    @Override
    public void dispose() {
        // prevent this from running twice
        if (this.isDisposed()) {
            return;
        }

        TypeNode curType = (TypeNode) lstTypes.getSelectedValue();
        if (curType != null) {
            savePropertiesTo(curType);
            selectionAtClosing = curType;
        }

        core.pushUpdate();

        if (txtName.getText().equals("")
                && curType != null) {
            InfoBox.warning("Illegal Type",
                    "Currently selected type is illegal. Please correct or delete.", this);
        } else {
            killAllChildren();
            super.dispose();
        }
    }

    @Override
    public boolean thisOrChildrenFocused() {
        return this.isFocusOwner();
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // Due to modal nature of form, no need to update
    }

    /**
     * Closes all child windows
     */
    private void killAllChildren() {
        Iterator<Window> it = childFrames.iterator();

        while (it.hasNext()) {
            Window curFrame = it.next();

            if (curFrame != null
                    && curFrame.isShowing()) {
                curFrame.setVisible(false);
                curFrame.dispose();
            }
        }

        childFrames.clear();
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
        TypeNode curNode = (TypeNode) lstTypes.getSelectedValue();

        if (txtName.getText().equals("") || txtName.getText().equals(defName)) {
            txtErrorBox.setText("Types must have name populated.");
            txtName.setBackground(core.getRequiredColor());
            lstTypes.setEnabled(false);
        } else {
            txtErrorBox.setText("");
            lstTypes.setEnabled(true);
            txtName.setBackground(new JTextField().getBackground());
        }

        if (updatingName || curNode == null) {
            return;
        }
        updatingName = true;
        curNode.setValue(txtName.getText().equals(defName)
                ? "" : txtName.getText());

        populateTypes();
        lstTypes.setSelectedValue(curNode, true);
        updatingName = false;
    }

    /**
     * Clears all current types and re-populates values, selecting first value
     */
    private void populateTypes() {
        Iterator<TypeNode> typeIt = core.getTypes().getNodeIterator();

        try {
            DefaultListModel listModel = new DefaultListModel();

            while (typeIt.hasNext()) {
                listModel.addElement(typeIt.next());
            }

            lstTypes.setModel(listModel);
            lstTypes.setSelectedIndex(0);
            lstTypes.ensureIndexIsVisible(0);
        } catch (Exception e) {
            InfoBox.error("Type Population Error", "Unable to populate types: "
                    + e.getLocalizedMessage(), this);
        }
    }

    /**
     * Populates properties of currently selected type, if any
     */
    private void populateProperties() {
        TypeNode curNode = (TypeNode) lstTypes.getSelectedValue();

        if (curNode == null) {
            if (!updatingName) {
                updatingName = true;
                txtName.setText("");
                updatingName = false;
            }
            txtName.setForeground(Color.lightGray);
            txtNotes.setText("");
            txtNotes.setForeground(Color.lightGray);
            txtTypePattern.setText("");
            txtTypePattern.setForeground(Color.lightGray);
            chkDefMand.setSelected(false);
            chkGendMand.setSelected(false);
            chkProcMand.setSelected(false);
            setPropertiesEnabled(false);
        } else {
            if (!updatingName) {
                updatingName = true;
                txtName.setText(curNode.getValue().equals("")
                        ? defName : curNode.getValue());
                txtName.setForeground(curNode.getValue().equals("")
                        ? Color.lightGray : Color.black);
                updatingName = false;
            }
            txtNotes.setText(curNode.getNotes().equals("")
                    ? defNotes : curNode.getNotes());
            txtNotes.setForeground(curNode.getNotes().equals("")
                    ? Color.lightGray : Color.black);
            txtTypePattern.setText(curNode.getPattern().equals("")
                    ? defPattern : curNode.getPattern());
            txtTypePattern.setForeground(curNode.getPattern().equals("")
                    ? Color.lightGray : Color.black);
            txtGloss.setText(curNode.getGloss().equals("")
                    ? defGloss : curNode.getGloss());
            txtGloss.setForeground(curNode.getGloss().equals("")
                    ? Color.lightGray : Color.black);
            chkDefMand.setSelected(curNode.isDefMandatory());
            chkGendMand.setSelected(curNode.isGenderMandatory());
            chkProcMand.setSelected(curNode.isProcMandatory());
            setPropertiesEnabled(true);
        }
    }

    /**
     * Saves properties to given node
     *
     * @param saveNode node to save to
     */
    private void savePropertiesTo(TypeNode saveNode) {
        saveNode.setValue(txtName.getText().equals(defName)
                ? "" : txtName.getText());
        saveNode.setNotes(txtNotes.getText().equals(defNotes)
                ? "" : txtNotes.getText());
        saveNode.setPattern(txtTypePattern.getText().equals(defPattern)
                ? "" : txtTypePattern.getText());
        saveNode.setGloss(txtGloss.getText().equals(defGloss)
                ? "" : txtGloss.getText());
        saveNode.setDefMandatory(chkDefMand.isSelected());
        saveNode.setGenderMandatory(chkGendMand.isSelected());
        saveNode.setProcMandatory(chkProcMand.isSelected());
    }

    /**
     * creates blank type, selects value for editing
     */
    private void addType() {
        TypeNode curType = (TypeNode) lstTypes.getSelectedValue();

        if (curType != null) {
            savePropertiesTo(curType);
        }

        core.getTypes().clear();
        try {
            core.getTypes().insert();
        } catch (Exception e) {
            InfoBox.error("Type Creation Error", "Could not create new type: "
                    + e.getLocalizedMessage(), this);
        }
        updatingName = true;
        populateTypes();
        lstTypes.setSelectedIndex(0);
        txtName.setText("");
        populateProperties();
        updatingName = false;

        txtName.requestFocus();
        txtName.setForeground(Color.black);
    }

    /**
     * deletes currently selected type
     */
    private void deleteType() {
        TypeNode curType = (TypeNode) lstTypes.getSelectedValue();

        if (curType == null) {
            return;
        }

        try {
            core.getTypes().deleteNodeById(curType.getId());
        } catch (Exception e) {
            InfoBox.error("Deletion Error", "Unable to delete type." + e.getLocalizedMessage(), this);
        }

        populateTypes();
        populateProperties();
    }

    /**
     * Sets type properties controls enabled or disables
     *
     * @param enable: whether to enable properties
     */
    private void setPropertiesEnabled(boolean enable) {
        txtName.setEnabled(enable);
        txtNotes.setEnabled(enable);
        txtTypePattern.setEnabled(enable);
        txtGloss.setEnabled(enable);
        chkDefMand.setEnabled(enable);
        chkGendMand.setEnabled(enable);
        chkProcMand.setEnabled(enable);
    }

    public static ScrTypes run(DictCore _core) {
        ScrTypes s = new ScrTypes(_core);
        s.setupKeyStrokes();
        s.setCore(_core);
        return s;
    }

    /**
     * Opens window, creates new, blank type, then returns type selected by user
     *
     * @param _core dictionary core
     * @return selected type at time of window close
     */
    public static TypeNode newGetType(DictCore _core) {
        final ScrTypes s = new ScrTypes(_core);
        s.addType();
        s.setVisible(true);
        return s.closedGetSelectedType();
    }

    /**
     * returns type currently selected by user, null if none
     *
     * @return selected type
     */
    public TypeNode closedGetSelectedType() {
        return selectionAtClosing;
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
        txtName = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtNotes = new javax.swing.JTextArea();
        txtTypePattern = new PTextField(core);
        btnSetup = new javax.swing.JButton();
        btnAutogen = new javax.swing.JButton();
        txtErrorBox = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        chkDefMand = new javax.swing.JCheckBox();
        chkProcMand = new javax.swing.JCheckBox();
        chkGendMand = new javax.swing.JCheckBox();
        txtGloss = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstTypes = new javax.swing.JList();
        btnAddType = new PButton("+");
        btnDelType = new PButton("-");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Types/Parts of Speech");

        jSplitPane1.setDividerLocation(140);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.setMinimumSize(new java.awt.Dimension(10, 10));

        txtName.setToolTipText("Part of speech name");
        txtName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNameFocusLost(evt);
            }
        });

        txtNotes.setColumns(20);
        txtNotes.setLineWrap(true);
        txtNotes.setRows(5);
        txtNotes.setToolTipText("Notes on part of speech");
        txtNotes.setWrapStyleWord(true);
        txtNotes.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtNotesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNotesFocusLost(evt);
            }
        });
        jScrollPane2.setViewportView(txtNotes);

        txtTypePattern.setToolTipText("Regex pattern to enforce on part of speech");
        txtTypePattern.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtTypePatternFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtTypePatternFocusLost(evt);
            }
        });

        btnSetup.setText("Conjugations/Declensions Setup");
        btnSetup.setToolTipText("Create declension and conjugation dimensins here.");
        btnSetup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetupActionPerformed(evt);
            }
        });

        btnAutogen.setText("Conjugations/Declensions Autogeneration");
        btnAutogen.setToolTipText("Setup rules to automatically generate conjugations and declensions for words of this type here.");
        btnAutogen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAutogenActionPerformed(evt);
            }
        });

        txtErrorBox.setForeground(new java.awt.Color(255, 0, 0));
        txtErrorBox.setEnabled(false);

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        chkDefMand.setText("Definition Mandatory");
        chkDefMand.setToolTipText("Select to enforce definition text for this par of speech.");

        chkProcMand.setText("Pronunciation Mandatory");
        chkProcMand.setToolTipText("Select to enforce pronunciation text for this par of speech.");

        chkGendMand.setText("Gender Mandatory");
        chkGendMand.setToolTipText("Select to enforce gender selection for this par of speech.");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkDefMand)
                    .addComponent(chkProcMand)
                    .addComponent(chkGendMand))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chkGendMand)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkProcMand)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkDefMand)
                .addContainerGap())
        );

        txtGloss.setToolTipText("Part of speech's gloss");
        txtGloss.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtGlossFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtGlossFocusLost(evt);
            }
        });

        jButton1.setText("OK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtName)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton1))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(txtTypePattern)
                    .addComponent(btnSetup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAutogen, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(txtGloss)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtErrorBox)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtGloss, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtTypePattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSetup)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAutogen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtErrorBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
        );

        jSplitPane1.setRightComponent(jPanel1);

        lstTypes.setToolTipText("Parts of Speech");
        lstTypes.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstTypesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstTypes);

        btnAddType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTypeActionPerformed(evt);
            }
        });

        btnDelType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(btnAddType, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addComponent(btnDelType, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddType)
                    .addComponent(btnDelType)))
        );

        jSplitPane1.setLeftComponent(jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNameFocusLost
        if (txtName.getText().equals("")) {
            updatingName = true;
            txtName.setText(defName);
            updatingName = false;
            txtName.setForeground(Color.lightGray);
        }
    }//GEN-LAST:event_txtNameFocusLost

    private void txtNotesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNotesFocusLost
        if (txtNotes.getText().equals("")) {
            txtNotes.setText(defNotes);
            txtNotes.setForeground(Color.lightGray);
        }
    }//GEN-LAST:event_txtNotesFocusLost

    private void txtTypePatternFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTypePatternFocusLost
        if (txtTypePattern.getText().equals("")) {
            txtTypePattern.setText(defPattern);
            txtTypePattern.setForeground(Color.lightGray);
        }
    }//GEN-LAST:event_txtTypePatternFocusLost

    private void txtNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNameFocusGained
        if (txtName.getText().equals(defName)) {
            updatingName = true;
            txtName.setText("");
            updatingName = false;
            txtName.setForeground(Color.black);
        }
    }//GEN-LAST:event_txtNameFocusGained

    private void txtNotesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNotesFocusGained
        if (txtNotes.getText().equals(defNotes)) {
            txtNotes.setText("");
            txtNotes.setForeground(Color.black);
        }
    }//GEN-LAST:event_txtNotesFocusGained

    private void txtTypePatternFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTypePatternFocusGained
        if (txtTypePattern.getText().equals(defPattern)) {
            txtTypePattern.setText("");
            txtTypePattern.setForeground(Color.black);
        }
    }//GEN-LAST:event_txtTypePatternFocusGained

    private void btnDelTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelTypeActionPerformed
        deleteType();
    }//GEN-LAST:event_btnDelTypeActionPerformed

    private void btnAddTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTypeActionPerformed
        addType();
    }//GEN-LAST:event_btnAddTypeActionPerformed

    private void lstTypesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstTypesValueChanged
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
                TypeNode curNode = (TypeNode) lstTypes.getModel().getElementAt(index);

                if (curNode != null) {
                    savePropertiesTo(curNode);
                }
            }
        }

        populateProperties();
    }//GEN-LAST:event_lstTypesValueChanged

    private void btnSetupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetupActionPerformed
        TypeNode curNode = (TypeNode) lstTypes.getSelectedValue();
        if (curNode == null) {
            return;
        }

        Window window = ScrDeclensionSetup.run(core, curNode.getId());
        childFrames.add(window);
    }//GEN-LAST:event_btnSetupActionPerformed

    private void btnAutogenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAutogenActionPerformed
        TypeNode curNode = (TypeNode) lstTypes.getSelectedValue();
        if (curNode == null) {
            return;
        }

        Window window = ScrDeclensionGenSetup.run(core, curNode.getId());
        childFrames.add(window);
    }//GEN-LAST:event_btnAutogenActionPerformed

    private void txtGlossFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtGlossFocusGained
        if (txtGloss.getText().equals(defGloss)) {
            txtGloss.setText("");
            txtGloss.setForeground(Color.black);
        }
    }//GEN-LAST:event_txtGlossFocusGained

    private void txtGlossFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtGlossFocusLost
        if (txtGloss.getText().equals("")) {
            txtGloss.setText(defGloss);
            txtGloss.setForeground(Color.lightGray);
        }
    }//GEN-LAST:event_txtGlossFocusLost

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddType;
    private javax.swing.JButton btnAutogen;
    private javax.swing.JButton btnDelType;
    private javax.swing.JButton btnSetup;
    private javax.swing.JCheckBox chkDefMand;
    private javax.swing.JCheckBox chkGendMand;
    private javax.swing.JCheckBox chkProcMand;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JList lstTypes;
    private javax.swing.JTextField txtErrorBox;
    private javax.swing.JTextField txtGloss;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextArea txtNotes;
    private javax.swing.JTextField txtTypePattern;
    // End of variables declaration//GEN-END:variables
}
