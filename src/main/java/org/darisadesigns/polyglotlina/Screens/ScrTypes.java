/*
 * Copyright (c) 2015-2021, Draque Thompson
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

import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.CustomControls.PButton;
import org.darisadesigns.polyglotlina.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.CustomControls.PList;
import org.darisadesigns.polyglotlina.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import java.awt.Color;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.darisadesigns.polyglotlina.CustomControls.PTextPane;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JComponent;
import org.darisadesigns.polyglotlina.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.Desktop.PropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.PolyGlot;

/**
 *
 * @author draque
 */
public final class ScrTypes extends PFrame {

    private final List<Window> childFrames = new ArrayList<>();
    private TypeNode selectionAtClosing = null;
    private boolean updatingName = false;
    private boolean ignoreUpdate = false;

    public ScrTypes(DictCore _core) {
        super(_core);
        
        initComponents();

        populateTypes();
        populateProperties();
        setupListeners();
        setupForm();
    }
    
    private void setupForm() {
        int divider = PolyGlot.getPolyGlot().getOptionsManager().getDividerPosition(this.getClass().getName());
        
        if (divider > -1) {
            jSplitPane1.setDividerLocation(divider);
        }
    }

    @Override
    public void dispose() {
        // prevent this from running twice
        if (this.isDisposed()) {
            return;
        }

        saveAllValues();
        PolyGlot.getPolyGlot().getOptionsManager().setDividerPosition(getClass().getName(), jSplitPane1.getDividerLocation());

        if (canClose()) {
            killAllChildren();
            super.dispose();
        }
    }
    
    @Override
    public void saveAllValues() {
        TypeNode curType = lstTypes.getSelectedValue();
        if (curType != null) {
            savePropertiesTo(curType);
            selectionAtClosing = curType;
        }
    }

    @Override
    public void updateAllValues(DictCore _core) {
        core = _core;
        
        populateTypes();
        populateProperties();
        setupListeners();
    }

    @Override
    public Component getWindow() {
        return jSplitPane1;
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
     * Sets whether objects with listeners should ignore
     * @param listenValue 
     */
    private void setListeningActive(boolean listenValue) {
        if (listenValue) {
            ((PTextField)txtName).startListening();
        } else {
            ((PTextField)txtName).stopListening();
        }
    }

    /**
     * Updates name value so that display can populate properly
     */
    private void updateName() {
        TypeNode curNode = lstTypes.getSelectedValue();
        
        if (ignoreUpdate) {
            return;
        }

        if (((PTextField) txtName).isDefaultText() || txtName.getText().isEmpty()) {
            txtErrorBox.setText("Types must have name populated.");
            txtName.setBackground(PGTUtil.COLOR_REQUIRED_LEX_COLOR);
            lstTypes.setEnabled(false);
        } else {
            txtErrorBox.setText("");
            lstTypes.setEnabled(true);
            ((PTextField)txtName).setupLook();
        }

        if (updatingName || curNode == null) {
            return;
        }
        updatingName = true;
        curNode.setValue(((PTextField) txtName).isDefaultText()
                ? "" : txtName.getText());

        populateTypes();
        lstTypes.setSelectedValue(curNode, true);
        updatingName = false;
    }

    /**
     * Clears all current types and re-populates values, selecting first value
     */
    private void populateTypes() {
        try {
            DefaultListModel<TypeNode> listModel = new DefaultListModel<>();

            for (TypeNode curNode : core.getTypes().getNodes()) {
                listModel.addElement(curNode);
            }

            lstTypes.setModel(listModel);
            lstTypes.setSelectedIndex(0);
            lstTypes.ensureIndexIsVisible(0);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Type Population Error", "Unable to populate types: "
                    + e.getLocalizedMessage());
        }
    }

    /**
     * Populates properties of currently selected type, if any
     */
    private void populateProperties() {
        TypeNode curNode = lstTypes.getSelectedValue();

        if (curNode == null) {
            if (!updatingName) {
                updatingName = true;
                txtName.setText("");
                updatingName = false;
            }
            txtNotes.setText("");
            txtTypePattern.setText("");
            chkDefMand.setSelected(false);
            chkProcMand.setSelected(false);
            setPropertiesEnabled(false);
        } else {
            if (!updatingName) {
                updatingName = true;
                txtName.setText(curNode.getValue().isEmpty()
                        ? ((PTextField) txtName).getDefaultValue() : curNode.getValue());
                updatingName = false;
            }
            txtNotes.setText(curNode.getNotes().isEmpty()
                    ? ((PTextPane) txtNotes).getDefaultValue() : curNode.getNotes());
            txtTypePattern.setText(curNode.getPattern().isEmpty()
                    ? ((PTextField) txtTypePattern).getDefaultValue() : curNode.getPattern());
            txtGloss.setText(curNode.getGloss().isEmpty()
                    ? ((PTextField) txtGloss).getDefaultValue() : curNode.getGloss());
            chkDefMand.setSelected(curNode.isDefMandatory());
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
        if (!updatingName) {
            saveNode.setValue(((PTextField) txtName).isDefaultText()
                    ? "" : txtName.getText());
            saveNode.setNotes(((PTextPane) txtNotes).isDefaultText()
                    ? "" : txtNotes.getText());
            saveNode.setPattern(((PTextField) txtTypePattern).isDefaultText()
                    ? "" : txtTypePattern.getText(), core);
            saveNode.setGloss(((PTextField) txtGloss).isDefaultText()
                    ? "" : txtGloss.getText());
            saveNode.setDefMandatory(chkDefMand.isSelected());
            saveNode.setProcMandatory(chkProcMand.isSelected());
        }
    }

    /**
     * creates blank type, selects value for editing
     */
    private void addType() {
        TypeNode curType = lstTypes.getSelectedValue();

        if (curType != null) {
            savePropertiesTo(curType);
        }

        core.getTypes().clear();
        
        updatingName = true;
        try {
            core.getTypes().insert();
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Type Creation Error", "Could not create new type: "
                    + e.getLocalizedMessage());
        }
        
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
        TypeNode curType = lstTypes.getSelectedValue();

        if (curType == null) {
            return;
        }

        ignoreUpdate = true;
        setListeningActive(false);
        
        try {
            core.getTypes().deleteNodeById(curType.getId());
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Deletion Error", 
                    "Unable to delete type." + e.getLocalizedMessage());
        }
        
        
        populateTypes();
        populateProperties();
        
        setListeningActive(true);
        ignoreUpdate = false;
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
        chkProcMand.setEnabled(enable);
        btnSetup.setEnabled(enable);
        btnAutogen.setEnabled(enable);
    }

    public static ScrTypes run(DictCore _core) {
        ScrTypes s = new ScrTypes(_core);
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
    
    @Override
    public boolean canClose() {
        boolean ret = true;
        
        ret = ret && setLegal();
        
        return ret;
    }
    
    /**
     * Tests and sets legality of current type
     * @return 
     */
    private boolean setLegal() {
        boolean ret = true;
        TypeNode curType = lstTypes.getSelectedValue();

        if (txtName.getText().isEmpty()
                && curType != null) {
            core.getOSHandler().getInfoBox().warning("Illegal Type",
                    "Currently selected type is illegal. Please correct or delete.");
            ret = false;
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

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        txtName = new PTextField(core, true, "-- Part of Speech Name --");
        txtTypePattern = new PTextField(core,
            core.getPropertiesManager().isOverrideRegexFont(),
            "-- Type Pattern --");
        btnSetup = new PButton(nightMode, menuFontSize);
        btnAutogen = new PButton(nightMode, menuFontSize);
        txtErrorBox = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        chkDefMand = new PCheckBox(nightMode, menuFontSize);
        chkProcMand = new PCheckBox(nightMode, menuFontSize);
        txtGloss = new PTextField(core, true, "-- Part of Speech Gloss --");
        jScrollPane3 = new javax.swing.JScrollPane();
        txtNotes = new PTextPane(core, true, "-- Notes --");
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstTypes = new PList(((PropertiesManager)core.getPropertiesManager()).getFontLocal(), menuFontSize);
        btnAddType = new PAddRemoveButton("+");
        btnDelType = new PAddRemoveButton("-");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Parts of Speech");
        setBackground(new java.awt.Color(255, 255, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jSplitPane1.setBackground(new java.awt.Color(255, 255, 255));
        jSplitPane1.setDividerLocation(140);
        jSplitPane1.setForeground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        txtName.setToolTipText("Part of speech name");

        txtTypePattern.setToolTipText("Regex pattern which words of this type must conform to");

        btnSetup.setText("Conjugations/Declensions Setup");
        btnSetup.setToolTipText("Create declension and conjugation dimensins here.");
        btnSetup.setEnabled(false);
        btnSetup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetupActionPerformed(evt);
            }
        });

        btnAutogen.setText("Conjugations/Declensions Autogeneration");
        btnAutogen.setToolTipText("Setup rules to automatically generate conjugations and declensions for words of this type here.");
        btnAutogen.setEnabled(false);
        btnAutogen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAutogenActionPerformed(evt);
            }
        });

        txtErrorBox.setForeground(new java.awt.Color(255, 0, 0));
        txtErrorBox.setEnabled(false);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        chkDefMand.setText("Definition Mandatory");
        chkDefMand.setToolTipText("Select to enforce definition text for this par of speech.");

        chkProcMand.setText("Pronunciation Mandatory");
        chkProcMand.setToolTipText("Select to enforce pronunciation text for this par of speech.");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkDefMand)
                    .addComponent(chkProcMand))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkDefMand)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(chkProcMand))
        );

        txtGloss.setToolTipText("Part of speech's gloss");

        jScrollPane3.setViewportView(txtNotes);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAutogen, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                    .addComponent(btnSetup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtTypePattern, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtGloss, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtName)
                    .addComponent(txtErrorBox))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
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
                .addComponent(txtErrorBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setRightComponent(jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
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
            .addComponent(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        getAccessibleContext().setAccessibleName("Parts of Speech");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDelTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelTypeActionPerformed
        deleteType();
    }//GEN-LAST:event_btnDelTypeActionPerformed

    private void btnAddTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTypeActionPerformed
        addType();
    }//GEN-LAST:event_btnAddTypeActionPerformed

    private void lstTypesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstTypesValueChanged
        if (evt.getValueIsAdjusting()
                || updatingName
                || ignoreUpdate) {
            return;
        }

        if (evt.getFirstIndex() != evt.getLastIndex()) {
            JList list = (JList) evt.getSource();
            int selected = list.getSelectedIndex();
            int index = selected == evt.getFirstIndex()
                    ? evt.getLastIndex() : evt.getFirstIndex();

            if (index != -1) {
                TypeNode curNode = lstTypes.getModel().getElementAt(index);

                if (curNode != null) {
                    savePropertiesTo(curNode);
                }
            }
        }

        populateProperties();
    }//GEN-LAST:event_lstTypesValueChanged

    private void btnSetupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetupActionPerformed
        TypeNode curNode = lstTypes.getSelectedValue();
        if (curNode == null) {
            return;
        }

        Window window = ScrDeclensionSetup.run(core, curNode.getId());
        childFrames.add(window);
    }//GEN-LAST:event_btnSetupActionPerformed

    private void btnAutogenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAutogenActionPerformed
        TypeNode curNode = lstTypes.getSelectedValue();
        if (curNode == null) {
            return;
        }

        Window window = ScrDeclensionGenSetup.run(core, curNode.getId());
        
        PolyGlot.getPolyGlot().getRootWindow().setVisible(false);
        window.addWindowListener(new WindowListener() {
                    @Override
                    public void windowOpened(WindowEvent e) {
                    }

                    @Override
                    public void windowClosing(WindowEvent e) {
                    }

                    @Override
                    public void windowClosed(WindowEvent e) {
                        PolyGlot.getPolyGlot().getRootWindow().setVisible(true);
                    }

                    @Override
                    public void windowIconified(WindowEvent e) {
                    }

                    @Override
                    public void windowDeiconified(WindowEvent e) {
                    }

                    @Override
                    public void windowActivated(WindowEvent e) {
                    }

                    @Override
                    public void windowDeactivated(WindowEvent e) {
                    }
                });
    }//GEN-LAST:event_btnAutogenActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddType;
    private javax.swing.JButton btnAutogen;
    private javax.swing.JButton btnDelType;
    private javax.swing.JButton btnSetup;
    private javax.swing.JCheckBox chkDefMand;
    private javax.swing.JCheckBox chkProcMand;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JList<TypeNode> lstTypes;
    private javax.swing.JTextField txtErrorBox;
    private javax.swing.JTextField txtGloss;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextPane txtNotes;
    private javax.swing.JTextField txtTypePattern;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addBindingToComponent(JComponent c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
