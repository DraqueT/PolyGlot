/*
 * Copyright (c) 2019-2020, Draque Thompson, draquemail@gmail.com
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

import java.awt.Component;
import javax.swing.DefaultListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.darisadesigns.polyglotlina.CustomControls.PButton;
import org.darisadesigns.polyglotlina.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.CustomControls.PList;
import org.darisadesigns.polyglotlina.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Desktop.PropertiesManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.Nodes.ConjugationGenRule;
import org.darisadesigns.polyglotlina.Nodes.ConjugationGenTransform;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;

/**
 *
 * @author draque
 */
public class ScrDeclensionGenSimple extends PDialog {

    private final int typeId;
    private boolean curPopulating = false;

    /**
     * Creates new form ScrDeclensionGenSimple
     *
     * @param _core
     * @param _typeId
     */
    public ScrDeclensionGenSimple(DictCore _core, int _typeId) {
        super(_core);
        typeId = _typeId;
        initComponents();
        populateCombinedDecl();
        populateRule();
        setupListeners();
        setupForm();
    }
    
    private void setupForm() {
        int divider = core.getOptionsManager().getDividerPosition(this.getClass().getName());
        
        if (divider > -1) {
            jSplitPane1.setDividerLocation(divider);
        }
    }
    
    public String getCurSelectedCombId() {
        ConjugationPair curPair = lstCombinedDec.getSelectedValue();
        return curPair == null ? "" : curPair.combinedId;
    }

    private void setupListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!curPopulating) {
                    saveRule();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!curPopulating) {
                    saveRule();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!curPopulating) {
                    saveRule();
                }
            }
        };

        txtRegex.getDocument().addDocumentListener(listener);
        txtReplace.getDocument().addDocumentListener(listener);
    }

    /**
     * Deletes all existing rules from current combined ID and replaces with
     * single displayed rule.
     *
     */
    public void saveRule() {
        ConjugationPair curPair = lstCombinedDec.getSelectedValue();

        if (curPair != null) {
            ConjugationManager decMan = core.getConjugationManager();
            decMan.deleteConjugationGenRules(typeId, curPair.combinedId);

            ConjugationGenTransform trans = new ConjugationGenTransform();
            trans.regex = txtRegex.getText();
            trans.replaceText = txtReplace.getText();

            ConjugationGenRule rule = new ConjugationGenRule(typeId, curPair.combinedId);
            rule.setRegex(".*"); // reduced complexity, so all rules apply universally
            rule.addTransform(trans);
            rule.setName("SIMPLE-SETUP");

            decMan.addConjugationGenRule(rule);
        }
    }

    /**
     * populates constructed declension list
     */
    private void populateCombinedDecl() {
        ConjugationPair[] decs = core.getConjugationManager().getAllCombinedIds(typeId);
        DefaultListModel decListModel = new DefaultListModel<>();
        lstCombinedDec.setModel(decListModel);

        for (ConjugationPair curPair : decs) {
            decListModel.addElement(curPair);
        }

        lstCombinedDec.setSelectedIndex(0);
    }

    private void populateRule() {
        ConjugationPair selected = lstCombinedDec.getSelectedValue();

        if (selected != null) {
            chkDisableForm.setEnabled(true);
            chkDisableForm.setSelected(core.getConjugationManager()
                    .isCombinedConjlSurpressed(selected.combinedId, typeId));
            enableEditing(!chkDisableForm.isSelected());

            ConjugationGenRule[] rules
                    = core.getConjugationManager().getConjugationRulesForTypeAndCombId(typeId, selected.combinedId);

            txtRegex.setText("");
            txtReplace.setText("");

            if (rules.length != 0) {
                // in the simplified display, there is only one rule/transform per combined ID
                ConjugationGenRule rule = rules[0];
                ConjugationGenTransform[] transforms = rule.getTransforms();
                if (transforms.length != 0) {
                    ConjugationGenTransform transform = transforms[0];
                    txtRegex.setText(transform.regex);
                    txtReplace.setText(transform.replaceText);
                }
            }
        } else {
            chkDisableForm.setEnabled(false);
            enableEditing(false);
        }
    }

    private void enableEditing(boolean enable) {
        txtRegex.setEnabled(enable);
        txtReplace.setEnabled(enable);
        btnPostfix.setEnabled(enable);
        btnPrefix.setEnabled(enable);
    }

    @Override
    public void dispose() {
        saveRule();
        core.getOptionsManager().setDividerPosition(getClass().getName(), jSplitPane1.getDividerLocation());
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // no action needed
    }
    
    @Override
    public Component getWindow() {
        return jPanel1;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        txtRegex = new PTextField(core, core.getPropertiesManager().isOverrideRegexFont(), "Replacement Regex");
        txtReplace = new PTextField(core, core.getPropertiesManager().isOverrideRegexFont(), "Replacement Text");
        btnPrefix = new PButton(nightMode, menuFontSize);
        btnPostfix = new PButton(nightMode, menuFontSize);
        chkDisableForm = new PCheckBox(nightMode, menuFontSize);
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstCombinedDec = new PList(((PropertiesManager)core.getPropertiesManager()).getFontLocal(), menuFontSize);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Conjugation/Declensions");

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(160);
        jSplitPane1.setDividerSize(10);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(0, 0));

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        txtRegex.setToolTipText("Put the regex pattern here representing what you would like to replace.");

        txtReplace.setToolTipText("The text here will replace what is defined in the regex field above.");

        btnPrefix.setText("Prefix Template");
        btnPrefix.setToolTipText("Apply prefix template to current conjugation. Anything in the replacement textbox will be prefixed for this form.");
        btnPrefix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrefixActionPerformed(evt);
            }
        });

        btnPostfix.setText("Postfix Template");
        btnPostfix.setToolTipText("Apply postfix template to current conjugation. Anything in the replacement textbox will be prefixed for this form.");
        btnPostfix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPostfixActionPerformed(evt);
            }
        });

        chkDisableForm.setLabel("Disable Wordform");
        chkDisableForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDisableFormActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtRegex)
                    .addComponent(txtReplace)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnPostfix, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnPrefix, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(chkDisableForm))
                        .addGap(0, 497, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkDisableForm)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtRegex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPrefix)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPostfix)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel2);

        lstCombinedDec.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstCombinedDec.setToolTipText("This lists every possible form of this part of speech.");
        lstCombinedDec.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstCombinedDecValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstCombinedDec);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel3);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void lstCombinedDecValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstCombinedDecValueChanged
        boolean tmpPopulating = curPopulating;
        try {
            curPopulating = true;
            populateRule();
        } finally {
            curPopulating = tmpPopulating;
        }
    }//GEN-LAST:event_lstCombinedDecValueChanged

    private void chkDisableFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDisableFormActionPerformed
        ConjugationPair curPair = lstCombinedDec.getSelectedValue();

        if (curPair == null) {
            return;
        }

        core.getConjugationManager().setCombinedConjSuppressed(curPair.combinedId, typeId, chkDisableForm.isSelected());

        enableEditing(!chkDisableForm.isSelected()
                && lstCombinedDec.getSelectedIndex() != -1);
    }//GEN-LAST:event_chkDisableFormActionPerformed

    private void btnPrefixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrefixActionPerformed
        txtRegex.setText("^");
    }//GEN-LAST:event_btnPrefixActionPerformed

    private void btnPostfixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPostfixActionPerformed
        txtRegex.setText("$");
    }//GEN-LAST:event_btnPostfixActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPostfix;
    private javax.swing.JButton btnPrefix;
    private javax.swing.JCheckBox chkDisableForm;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JList<ConjugationPair> lstCombinedDec;
    private javax.swing.JTextField txtRegex;
    private javax.swing.JTextField txtReplace;
    // End of variables declaration//GEN-END:variables
}
