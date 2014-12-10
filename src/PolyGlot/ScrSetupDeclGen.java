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

import java.awt.Font;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author draque
 */
public class ScrSetupDeclGen extends javax.swing.JDialog {

    final String depRulesLabel = "DEPRECATED RULES";
    final int typeId;
    final DictCore core;
    DefaultListModel decListModel;
    DefaultListModel rulesModel;
    DefaultTableModel transModel;
    boolean curPopulating = false;
    List<DeclensionGenRule> depRulesList;

    /**
     * Creates new form scrSetupDeclGen
     *
     * @param _core dictionary core
     * @param _typeId ID of type to pull rules for
     */
    private ScrSetupDeclGen(DictCore _core, int _typeId) {
        core = _core;
        typeId = _typeId;
        depRulesList = core.getDeclensionManager().getAllDepGenerationRules(_typeId);

        initComponents();
        setupObjectModels();
        setupListeners();
        setObjectProperties();

        populateCombinedDecl();
    }

    @Override
    public void dispose() {
        if (tblTransforms.getCellEditor() != null) {
            tblTransforms.getCellEditor().stopCellEditing();
        }

        saveTransPairs(lstRules.getSelectedIndex());

        super.dispose();
    }

    /**
     * sets fonts of objects
     */
    private void setObjectProperties() {
        Font setFont = core.getFontCon();
        txtRuleRegex.setFont(setFont);
    }

    /**
     * Opens screen declension window
     *
     * @param _core dictionary core
     * @param _typeId type ID to open window for
     * @return a copy of itself
     */
    public static ScrSetupDeclGen run(DictCore _core, int _typeId) {
        ScrSetupDeclGen s = new ScrSetupDeclGen(_core, _typeId);
        s.setModal(true);

        // center window in screen
        s.setLocationRelativeTo(null);

        s.setVisible(true);
        return s;
    }

    /**
     * populates rules for currently selected declension pair, returns if
     * nothing selected
     */
    private void populateRules() {
        rulesModel.clear();
        
        // population of rules works differently if deprecated rules are selected
        if (lstCombinedDec.getSelectedValue().equals(depRulesLabel)) {
            depRulesList = core.getDeclensionManager().getAllDepGenerationRules(typeId);
            
            for (DeclensionGenRule curRule : depRulesList) {
                rulesModel.addElement(curRule);
            }
            
            enableEditing(false);
        } else {

            DeclensionPair curPair = (DeclensionPair) lstCombinedDec.getSelectedValue();

            if (curPair == null) {
                return;
            }

            List<DeclensionGenRule> ruleList = core.getDeclensionManager().getDeclensionRules(typeId);

            for (DeclensionGenRule curRule : ruleList) {
                if (curRule.getCombinationId().equals(curPair.combinedId)) {
                    rulesModel.addElement(curRule);
                }
            }
            
            enableEditing(true);
        }

        lstRules.setSelectedIndex(0);
    }

    /**
     * populates all rule values from currently selected rule
     */
    public void populateRuleProperties() {
        if (curPopulating) {
            return;
        }

        curPopulating = true;

        DeclensionGenRule curRule = (DeclensionGenRule) lstRules.getSelectedValue();

        if (curRule == null) {
            txtRuleName.setText("");
            txtRuleRegex.setText("");
            populateTransforms();

            curPopulating = false;
            return;
        }

        txtRuleName.setText(curRule.getName());
        txtRuleRegex.setText(curRule.getRegex());

        populateTransforms();

        curPopulating = false;
    }

    /**
     * populates transforms of currently selected rule
     */
    private void populateTransforms() {
        DeclensionGenRule curRule = (DeclensionGenRule) lstRules.getSelectedValue();
        Font setFont = core.getFontCon();

        transModel = new DefaultTableModel();
        tblTransforms.setModel(transModel);

        transModel.addColumn("Regex");
        transModel.addColumn("Replacement");

        TableColumn column = tblTransforms.getColumnModel().getColumn(0);
        column.setCellEditor(new TableColumnEditor(setFont));
        column.setCellRenderer(new TableColumnRenderer(setFont));

        column = tblTransforms.getColumnModel().getColumn(1);
        column.setCellEditor(new TableColumnEditor(setFont));
        column.setCellRenderer(new TableColumnRenderer(setFont));

        // do nothing if nothing selected in rule list
        if (curRule == null) {
            return;
        }

        Iterator<DeclensionGenTransform> curTransform = curRule.getTransforms();

        while (curTransform.hasNext()) {
            DeclensionGenTransform curTrans = curTransform.next();
            String[] newRow = {curTrans.regex, curTrans.replaceText};

            transModel.addRow(newRow);

            TableColumnEditor editor = (TableColumnEditor) tblTransforms.getCellEditor(transModel.getRowCount() - 1, 0);
            editor.setInitialValue(curTrans.regex);

            editor = (TableColumnEditor) tblTransforms.getCellEditor(transModel.getRowCount() - 1, 1);
            editor.setInitialValue(curTrans.replaceText);
        }
    }

    /**
     * populates constructed declension list
     */
    private void populateCombinedDecl() {
        Iterator<DeclensionPair> it = core.getDeclensionManager().getAllCombinedIds(typeId).iterator();
        while (it.hasNext()) {
            DeclensionPair curNode = it.next();

            decListModel.addElement(curNode);
        }

        if (!depRulesList.isEmpty()) {
            decListModel.addElement(depRulesLabel);
        }

        lstCombinedDec.setSelectedIndex(0);
    }
    
    /**
     * Enables or disables editing of the properties/rules/transforms
     * @param choice 
     */
    public void enableEditing(boolean choice) {
        txtRuleName.setEditable(choice);
        txtRuleRegex.setEditable(choice);
        tblTransforms.setEnabled(choice);
        btnAddRule.setEnabled(choice);
        btnAddTransform.setEnabled(choice);
    }

    /**
     * sets up object models for visual components
     */
    private void setupObjectModels() {
        decListModel = new DefaultListModel();
        lstCombinedDec.setModel(decListModel);

        rulesModel = new DefaultListModel();
        lstRules.setModel(rulesModel);

        transModel = new DefaultTableModel();
        tblTransforms.setModel(transModel);
    }

    /**
     * sets up object listeners for form objects
     */
    private void setupListeners() {
        txtRuleName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setRuleName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setRuleName();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setRuleName();
            }
        });
        txtRuleRegex.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setRuleRegex();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setRuleRegex();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setRuleRegex();
            }
        });
    }

    /**
     * Saves transformation pairs to appropriate rule
     *
     * @param saveIndex index of rule to save to
     */
    private void saveTransPairs(int saveIndex) {
        if (saveIndex == -1) {
            return;
        }

        DeclensionGenRule saveRule = (DeclensionGenRule) rulesModel.get(saveIndex);

        if (saveRule == null) {
            return;
        }

        // return if nothing to save
        if (tblTransforms.getRowCount() == 0) {
            return;
        }

        saveRule.wipeTransforms();

        if (tblTransforms.getCellEditor() != null) {
            tblTransforms.getCellEditor().stopCellEditing();
        }

        for (int i = 0; i < tblTransforms.getRowCount(); i++) {
            String regex = tblTransforms.getValueAt(i, 0).toString();
            String replaceText = tblTransforms.getValueAt(i, 1).toString();

            saveRule.addTransform(new DeclensionGenTransform(regex, replaceText));
        }
    }

    /**
     * Sets currently selected rule's name equal to proper text box if not
     * already equal
     */
    private void setRuleName() {
        DeclensionGenRule rule = (DeclensionGenRule) lstRules.getSelectedValue();
        String ruleName = txtRuleName.getText().trim();

        if (!curPopulating && rule != null && !rule.getName().equals(ruleName)) {
            rule.setName(ruleName);
            lstRules.updateUI();
        }
    }

    /**
     * Sets currently selected rule's regex equal to proper text box if not
     * already equal
     */
    private void setRuleRegex() {
        DeclensionGenRule rule = (DeclensionGenRule) lstRules.getSelectedValue();
        String ruleRegex = txtRuleRegex.getText().trim();

        if (!curPopulating && rule != null && !rule.getName().equals(ruleRegex)) {
            rule.setRegex(ruleRegex);
        }
    }

    /**
     * adds new rule
     */
    private void addRule() {
        DeclensionPair curPair = (DeclensionPair) lstCombinedDec.getSelectedValue();

        if (curPair == null) {
            return;
        }

        saveTransPairs(lstRules.getSelectedIndex());

        DeclensionGenRule newRule = new DeclensionGenRule(typeId, curPair.combinedId);

        core.getDeclensionManager().addDeclensionGenRule(newRule);
        rulesModel.addElement(newRule);
        lstRules.setSelectedIndex(lstRules.getLastVisibleIndex());
        txtRuleName.setText("NEW RULE");
        txtRuleRegex.setText("");
        populateTransforms();
    }

    /**
     * deletes currently selected rule
     */
    private void deleteRule() {
        DeclensionGenRule curRule = (DeclensionGenRule) lstRules.getSelectedValue();

        if (curRule == null) {
            return;
        }

        core.getDeclensionManager().deleteDeclensionGenRule(curRule);
        populateRules();
        populateRuleProperties();
        populateTransforms();
    }

    /**
     * adds transform set to currently selected rule
     */
    private void addTransform() {
        if (lstRules.getSelectedValue() == null) {
            return;
        }

        transModel.addRow(new Object[]{"", ""});
    }

    /**
     * deletes currently selected transform from currently selected rule
     */
    private void deleteTransform() {
        if (lstRules.getSelectedValue() != null
                && tblTransforms.getSelectedRow() != -1) {
            int removeRow = tblTransforms.convertRowIndexToModel(tblTransforms.getSelectedRow());
            transModel.removeRow(removeRow);
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

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstCombinedDec = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstRules = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        btnAddRule = new javax.swing.JButton();
        btnDeleteRule = new javax.swing.JButton();
        sclTransforms = new javax.swing.JScrollPane();
        tblTransforms = new javax.swing.JTable();
        btnAddTransform = new javax.swing.JButton();
        btnDeleteTransform = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtRuleName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtRuleRegex = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Conjugation/Declension Autogeneration Setup");

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel1.setText("Conjugation/Declensions");

        lstCombinedDec.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstCombinedDec.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstCombinedDec.setToolTipText("Combined Conjugations/Declensions");
        lstCombinedDec.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstCombinedDecValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstCombinedDec);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel2.setText("Rules");

        lstRules.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstRules.setToolTipText("List of rules associated with the selected conjugation");
        lstRules.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstRulesValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(lstRules);

        jLabel3.setText("Transformations");

        btnAddRule.setText("+");
        btnAddRule.setToolTipText("Add Rule");
        btnAddRule.setSize(new java.awt.Dimension(40, 29));
        btnAddRule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRuleActionPerformed(evt);
            }
        });

        btnDeleteRule.setText("-");
        btnDeleteRule.setToolTipText("Delete Rule");
        btnDeleteRule.setPreferredSize(new java.awt.Dimension(40, 29));
        btnDeleteRule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteRuleActionPerformed(evt);
            }
        });

        tblTransforms.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblTransforms.setToolTipText("Transformations to be applied.");
        sclTransforms.setViewportView(tblTransforms);

        btnAddTransform.setText("+");
        btnAddTransform.setToolTipText("Add Transformation");
        btnAddTransform.setPreferredSize(new java.awt.Dimension(40, 29));
        btnAddTransform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTransformActionPerformed(evt);
            }
        });

        btnDeleteTransform.setText("-");
        btnDeleteTransform.setToolTipText("Delete Transformation");
        btnDeleteTransform.setPreferredSize(new java.awt.Dimension(40, 29));
        btnDeleteTransform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteTransformActionPerformed(evt);
            }
        });

        jLabel4.setText("Rule Name");

        txtRuleName.setToolTipText("Name of rule");

        jLabel5.setText("Regex");

        txtRuleRegex.setToolTipText("Regex expression a word must match before tranformations are applied to it");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(btnDeleteRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sclTransforms, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAddTransform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDeleteTransform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtRuleName))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 131, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtRuleRegex)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnAddRule, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtRuleName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtRuleRegex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sclTransforms, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddRule)
                    .addComponent(btnDeleteRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddTransform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteTransform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lstCombinedDecValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstCombinedDecValueChanged
        saveTransPairs(lstRules.getSelectedIndex());
        populateRules();
        populateRuleProperties();
        populateTransforms();
    }//GEN-LAST:event_lstCombinedDecValueChanged

    private void btnAddRuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRuleActionPerformed
        addRule();
    }//GEN-LAST:event_btnAddRuleActionPerformed

    private void lstRulesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstRulesValueChanged
        // only fire this once
        if (!evt.getValueIsAdjusting()) {
            return;
        }

        int selected = lstRules.getSelectedIndex();
        int previous = selected == evt.getFirstIndex() ? evt.getLastIndex() : evt.getFirstIndex();

        saveTransPairs(previous);
        populateRuleProperties();
    }//GEN-LAST:event_lstRulesValueChanged

    private void btnDeleteRuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRuleActionPerformed
        deleteRule();
    }//GEN-LAST:event_btnDeleteRuleActionPerformed

    private void btnAddTransformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTransformActionPerformed
        addTransform();
    }//GEN-LAST:event_btnAddTransformActionPerformed

    private void btnDeleteTransformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteTransformActionPerformed
        deleteTransform();
    }//GEN-LAST:event_btnDeleteTransformActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddRule;
    private javax.swing.JButton btnAddTransform;
    private javax.swing.JButton btnDeleteRule;
    private javax.swing.JButton btnDeleteTransform;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lstCombinedDec;
    private javax.swing.JList lstRules;
    private javax.swing.JScrollPane sclTransforms;
    private javax.swing.JTable tblTransforms;
    private javax.swing.JTextField txtRuleName;
    private javax.swing.JTextField txtRuleRegex;
    // End of variables declaration//GEN-END:variables
}
