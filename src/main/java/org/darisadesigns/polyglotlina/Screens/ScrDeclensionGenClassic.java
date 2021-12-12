/*
 * Copyright (c) 2014-2021, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Nodes.ConjugationGenRule;
import org.darisadesigns.polyglotlina.Nodes.ConjugationGenTransform;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCellEditor;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCellRenderer;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PClassCheckboxPanel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PList;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTable;
import org.darisadesigns.polyglotlina.Nodes.ConjugationDimension;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;

/**
 *
 * @author draque
 */
public final class ScrDeclensionGenClassic extends PDialog {

    private final static String DEP_RULES_LABEL = "DEPRECATED RULES";
    private final int typeId;
    private DefaultListModel<Object> decListModel;
    private DefaultListModel<ConjugationGenRule> rulesModel;
    private DefaultTableModel transModel;
    private boolean curPopulating = false;
    private boolean upDownPress = false;
    private ConjugationGenRule[] depRulesList;

    /**
     * Creates new form scrSetupDeclGen
     *
     * @param _core dictionary core
     * @param _typeId ID of type to pull rules for
     * @param _parent
     */
    public ScrDeclensionGenClassic(DictCore _core, int _typeId, Window _parent) {
        super(_core);
        
        typeId = _typeId;
        depRulesList = core.getConjugationManager().getAllDepGenerationRules(_typeId);

        initComponents();
        setupObjectModels();
        setupListeners();
        setObjectProperties();
        setModal(true);
        super.getRootPane().getContentPane().setBackground(Color.white);

        populateCombinedDecl();
        pnlApplyClasses.setVisible(false);
        pnlApplyClasses.setVisible(true);
        scrApplyClasses.setVisible(false);
        scrApplyClasses.setVisible(true);
    }

    public String getCurSelectedCombId() {
        String ret = "";
        
        ConjugationPair curPair = (ConjugationPair) lstCombinedDec.getSelectedValue();
        
        if (curPair != null) {
            ret = curPair.combinedId;
        }
        
        return ret;
    }
    
    @Override
    public void dispose() {
        if (this.isDisposed()) {
            // this is sometimes called DOZENS of times. WHY?
            return;
        }
        
        saveVolatileValues();

        if (canClose()) {
            super.dispose();
        }
    }
    
    /**
     * Forces save of any values that may be displayed but are not yet committed.
     */
    public void saveVolatileValues() {
        if (tblTransforms.getCellEditor() != null) {
            tblTransforms.getCellEditor().stopCellEditing();
        }

        saveTransPairs(lstRules.getSelectedIndex());
    }

    /**
     * only allows the form to close on success, displays user prompt otherwise
     *
     * @return whether form may close
     */
    private boolean canClose() {
        boolean ret = true;
        String userMessage = "";

        ConjugationGenRule[] typeRules = core.getConjugationManager().getConjugationRulesForType(typeId);

        for (ConjugationGenRule curRule : typeRules) {
            try {
                Pattern.compile(curRule.getRegex());
            } catch (Exception e) {
                // user error
                // IOHandler.writeErrorLog(e);
                userMessage += "\nProblem with word match regex in rule " + curRule.getName() + ": " + e.getMessage();
                ret = false;
            }

            for (ConjugationGenTransform curTransform : curRule.getTransforms()) {
                try {
                    if (curTransform.regex.contains("$&")) {
                        throw new Exception("Java regex does not recognize the regex pattern \"$&\"");
                    }

                    Pattern.compile(curTransform.regex);
                } catch (Exception e) {
                    // user error
                    // IOHandler.writeErrorLog(e);
                    userMessage += "\nProblem with regular expression under declension \'"
                            + core.getConjugationManager().getCombNameFromCombId(typeId, curRule.getCombinationId())
                            + "\' in rule \'" + curRule.getName() + "\' transform \'" + curTransform.regex + " -> "
                            + curTransform.replaceText + "\':\n " + e.getMessage();
                    ret = false;
                }
            }
        }

        if (!ret) {
            core.getOSHandler().getInfoBox().error("Unable to Close With Error", userMessage);
        }

        return ret;
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // No values to update due to modular nature of window
    }

    /**
     * sets fonts of objects
     */
    private void setObjectProperties() {
        if (!core.getPropertiesManager().isOverrideRegexFont()) {
            Font setFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon();
            txtRuleRegex.setFont(setFont);
        }
    }

    /**
     * populates rules for currently selected declension pair, returns if nothing selected
     */
    private void populateRules() {
        rulesModel.clear();

        // population of rules works differently if deprecated rules are selected
        if (lstCombinedDec.getSelectedValue().equals(DEP_RULES_LABEL)) {
            depRulesList = core.getConjugationManager().getAllDepGenerationRules(typeId);

            for (ConjugationGenRule curRule : depRulesList) {
                rulesModel.addElement(curRule);
            }

            enableEditing(false);
        } else {
            // done first in the case that it is later disabled due to a disabled wordform
            enableEditing(true);
            
            ConjugationPair curPair = (ConjugationPair) lstCombinedDec.getSelectedValue();

            if (curPair == null) {
                return;
            }

            ConjugationGenRule[] ruleList = core.getConjugationManager()
                    .getConjugationRulesForTypeAndCombId(typeId, curPair.combinedId);

            // only allow editing if there are actually rules to be populated... 
            enableTransformEditing(ruleList.length != 0);
            
            for (ConjugationGenRule curRule : ruleList) {
                rulesModel.addElement(curRule);
            }
        }

        lstRules.setSelectedIndex(0);
    }

    /**
     * populates all rule values from currently selected rule also sets controls to allow editing only if a rule is
     * selected
     */
    public void populateRuleProperties() {
        if (curPopulating) {
            return;
        }

        curPopulating = true;

        ConjugationGenRule curRule = (ConjugationGenRule) lstRules.getSelectedValue();
        ((PClassCheckboxPanel)pnlApplyClasses).setRule(curRule, core);
        
        if (curRule == null || lstRules.getSelectedIndices().length > 1) {
            txtRuleName.setText("");
            txtRuleRegex.setText("");
            populateTransforms();

            enableTransformEditing(false);
            curPopulating = false;
            return;
        }

        enableTransformEditing(true);

        txtRuleName.setText(curRule.getName());
        txtRuleRegex.setText(curRule.getRegex());

        populateTransforms();

        curPopulating = false;
    }

    /**
     * populates transforms of currently selected rule
     */
    private void populateTransforms() {
        ConjugationGenRule curRule = (ConjugationGenRule) lstRules.getSelectedValue();

        transModel = new DefaultTableModel();
        transModel.addColumn("Regex");
        transModel.addColumn("Replacement");
        tblTransforms.setModel(transModel);

        // do not populate if multiple selections
        if (lstRules.getSelectedIndices().length > 1) {
            return;
        }
        
        boolean useConFont = !core.getPropertiesManager().isOverrideRegexFont();

        TableColumn column = tblTransforms.getColumnModel().getColumn(0);
        column.setCellEditor(new PCellEditor(useConFont, core));
        column.setCellRenderer(new PCellRenderer(useConFont, core));
        
        column = tblTransforms.getColumnModel().getColumn(1);
        column.setCellEditor(new PCellEditor(useConFont, core));
        column.setCellRenderer(new PCellRenderer(useConFont, core));

        // do nothing if nothing selected in rule list
        if (curRule == null) {
            return;
        }

        ConjugationGenTransform[] curTransforms = curRule.getTransforms();

        for (ConjugationGenTransform curTrans : curTransforms) {
            Object[] newRow = {curTrans.regex, curTrans.replaceText};
            transModel.addRow(newRow);
        }

        tblTransforms.setModel(transModel);
    }

    /**
     * populates constructed declension list
     */
    private void populateCombinedDecl() {
        ConjugationPair[] decs = core.getConjugationManager().getAllCombinedIds(typeId);

        for (ConjugationPair curPair : decs) {
            decListModel.addElement(curPair);
        }

        if (depRulesList.length != 0) {
            decListModel.addElement(DEP_RULES_LABEL);
        }

        lstCombinedDec.setSelectedIndex(0);
    }

    /**
     * Enables or disables editing of the properties/rules/transforms
     *
     * @param choice
     */
    public void enableEditing(boolean choice) {
        txtRuleName.setEnabled(choice);
        txtRuleRegex.setEnabled(choice);
        tblTransforms.setEnabled(choice);
        btnAddRule.setEnabled(choice);
        btnAddTransform.setEnabled(choice);
    }

    /**
     * Enables or disables editing of the properties/rules/transforms
     *
     * @param choice
     */
    public void enableTransformEditing(boolean choice) {
        txtRuleName.setEnabled(choice);
        txtRuleRegex.setEnabled(choice);
        tblTransforms.setEnabled(choice);
        btnAddTransform.setEnabled(choice);
        btnDeleteTransform.setEnabled(choice);
    }

    /**
     * sets up object models for visual components
     */
    private void setupObjectModels() {
        decListModel = new DefaultListModel<>();
        lstCombinedDec.setModel(decListModel);

        rulesModel = new DefaultListModel<>();
        lstRules.setModel(rulesModel);

        transModel = new DefaultTableModel();
        tblTransforms.clearSelection();
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
        lstRules.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent k) {
                if (k.getKeyCode() == KeyEvent.VK_DOWN
                        || k.getKeyCode() == KeyEvent.VK_UP) {
                    upDownPress = true;
                }
            }
        });

        Action ruleCopyAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyRuleToClipboard();
            }
        };
        Action rulePasteAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteRuleFromClipboard();
            }
        };
        String copyKey = "copyRule";
        String pasteKey = "pasteRule";
        int mask;
        if (System.getProperty("os.name").startsWith("Mac")) {
            mask = KeyEvent.META_DOWN_MASK;
        } else {
            mask = KeyEvent.CTRL_DOWN_MASK;
        }
        InputMap ruleIm = lstRules.getInputMap();
        ActionMap ruleAm = lstRules.getActionMap();
        ruleIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | mask), copyKey);
        ruleIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | mask), pasteKey);
        ruleAm.put(copyKey, ruleCopyAction);
        ruleAm.put(pasteKey, rulePasteAction);

        final JPopupMenu ruleMenu = new JPopupMenu();
        final JMenu pushToDimension = new JMenu("Push To Dimension");
        final JMenu deleteFromDimension = new JMenu("Delete From Dimension");
        final JMenuItem copyItem = new JMenuItem("Copy Rule");
        final JMenuItem pasteItem = new JMenuItem("Paste Rule");
        final JMenuItem bulkDelete = new JMenuItem("Bulk Delete Rule");
        final JMenuItem bulkUpdate = new JMenuItem("Bulk Update Rule");
        copyItem.setToolTipText("Copy currently selected rule.");
        pasteItem.setToolTipText("Paste rule in clipboard to rule list.");
        bulkDelete.setToolTipText("Bulk delete all instances of this rule from this part of speech");
        bulkUpdate.setToolTipText("Bulk update all instances of this rule to match this one");

        copyItem.addActionListener((ActionEvent ae) -> {
            copyRuleToClipboard();
        });
        pasteItem.addActionListener((ActionEvent ae) -> {
            pasteRuleFromClipboard();
        });
        bulkDelete.addActionListener((ActionEvent ae) -> {
            bulkDelete();
        });
        bulkUpdate.addActionListener((ActionEvent ae) -> {
            bulkUpdate();
        });
        ruleMenu.add(copyItem);
        ruleMenu.add(pasteItem);
        ruleMenu.addSeparator();
        ruleMenu.add(pushToDimension);
        ruleMenu.add(deleteFromDimension);
        ruleMenu.add(bulkDelete);
        ruleMenu.add(bulkUpdate);
        lstRules.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    doPop(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    doPop(e);
                }
            }

            private void doPop(MouseEvent e) {
                if (lstRules.getSelectedValue() != null) {
                    copyItem.setEnabled(true);
                    setupPushToDimension((ConjugationPair)lstCombinedDec.getSelectedValue());
                    pushToDimension.setEnabled(true);
                    setupDeleteFromDimension((ConjugationPair)lstCombinedDec.getSelectedValue());
                    deleteFromDimension.setEnabled(true);
                } else {
                    copyItem.setEnabled(false);
                    pushToDimension.setEnabled(false);
                    deleteFromDimension.setEnabled(true);
                }

                ruleMenu.show(e.getComponent(), e.getX(), e.getY());
            }
            
            // sets up all menu items for copying rules to dimensions
            private void setupPushToDimension(ConjugationPair selDec) {
                pushToDimension.removeAll();                
                
                ConjugationNode[] nodes = core.getConjugationManager().getDimensionalConjugationListTemplate(typeId);
                List<Integer> ids = new ArrayList<>();
                
                for (String singleId : selDec.combinedId.split(",")) {
                    if (!singleId.isEmpty()) {
                        ids.add(Integer.parseInt(singleId));
                    }
                }

                for (int i = 0; i < ids.size() ; i++) {
                    int dimId = ids.get(i);
                    ConjugationNode decNode = nodes[i];
                    ConjugationDimension decDim = decNode.getConjugationDimensionById(dimId);
                    if (decDim != null) {
                        final JMenuItem pushTo = new JMenuItem("Push To " + decDim.getValue());
                        pushTo.setToolTipText("Push selected rule(s) to word forms with dimension: " 
                                + decDim.getValue());
                        pushTo.addActionListener(
                                buildCopyToDimensionAction(i, decDim.getId(), selDec.combinedId));
                        pushToDimension.add(pushTo);
                    }
                }
            }
            
            // sets up all menu items for deleting rules from dimensions
            private void setupDeleteFromDimension(ConjugationPair selDec) {
                deleteFromDimension.removeAll();                
                
                ConjugationNode[] nodes = core.getConjugationManager().getDimensionalConjugationListTemplate(typeId);
                List<Integer> ids = new ArrayList<>();
                
                for (String singleId : selDec.combinedId.split(",")) {
                    if (!singleId.isEmpty()) {
                        ids.add(Integer.parseInt(singleId));
                    }
                }

                for (int i = 0; i < ids.size() ; i++) {
                    int dimId = ids.get(i);
                    ConjugationNode decNode = nodes[i];
                    ConjugationDimension decDim = decNode.getConjugationDimensionById(dimId);
                    if (decDim != null) {
                        final JMenuItem deleteFrom = new JMenuItem("Delete From " + decDim.getValue());
                        deleteFrom.setToolTipText("Delete selected rule(s) from word forms with dimension: " 
                                + decDim.getValue());
                        deleteFrom.addActionListener(
                                buildDeleteFromDimensionAction(i, decDim.getId()));
                        deleteFromDimension.add(deleteFrom);
                    }
                }
            }
            
            private ActionListener buildDeleteFromDimensionAction(int decId, int dimId) {
                return (ActionEvent ae) -> {
                    List<ConjugationGenRule> rules = getSelectedRules();
                    if (verifyDeleteRulesToDimension(decId, dimId, rules)) {
                        core.getConjugationManager().deleteRulesFromConjugationTemplates(typeId, 
                                decId, 
                                dimId, 
                                rules);
                        populateRules();
                        populateRuleProperties();
                        populateTransforms();
                    }
                };
            }
            
            private ActionListener buildCopyToDimensionAction(int decId, int dimId, String combId) {
                return (ActionEvent ae) -> {
                    saveTransPairs(lstRules.getSelectedIndex());
                    List<ConjugationGenRule> rules = getSelectedRules();
                    if (verifyCopyRulesToDimension(decId, dimId, rules)) {
                        core.getConjugationManager().copyRulesToConjugationTemplates(typeId, 
                                decId, 
                                dimId, 
                                rules,
                                combId);
                    }
                };
            }
        });
    }
    
    public boolean verifyCopyRulesToDimension(int decId, int dimId, List<ConjugationGenRule> rules) {
        String decLabel = core.getConjugationManager().getConjugationLabel(typeId, decId);
        String decDimLabel = core.getConjugationManager().getConjugationValueLabel(typeId, decId, dimId);
        String message = "You are about to copy the following rule(s):\n\n";
        
        for (ConjugationGenRule rule : rules) {
            message += rule.getName() + "\n";
        }
        
        message += "\nto all word forms for this part of speech with the " + decLabel + " value of " + decDimLabel + ". Continue?";
        
        return core.getOSHandler().getInfoBox().actionConfirmation("Confirm Rule Copy", message);
    }
    
    public boolean verifyDeleteRulesToDimension(int decId, int dimId, List<ConjugationGenRule> rules) {
        String decLabel = core.getConjugationManager().getConjugationLabel(typeId, decId);
        String decDimLabel = core.getConjugationManager().getConjugationValueLabel(typeId, decId, dimId);
        String message = "You are about to delete the following rule(s):\n\n";
        
        for (ConjugationGenRule rule : rules) {
            message += rule.getName() + "\n";
        }
        
        message += "\nfrom all word forms for this part of speech with the " + decLabel + " value of " + decDimLabel + ". Continue?";
        
        return core.getOSHandler().getInfoBox().actionConfirmation("Confirm Rule Copy", message);
    }
    
    public boolean verifyBulkUpdateRule(List<ConjugationGenRule> rules) {
        String message = "You are about to bulk update the following rule(s):\n\n";
        
        for (ConjugationGenRule rule : rules) {
            message += rule.getName() + "\n\nRules with matching names/patterns will be updated to the exact parameters of those selected.";
        }
        
        message += "\n\nContinue?";
        
        return core.getOSHandler().getInfoBox().actionConfirmation("Confirm Rule Copy", message);
    }
    
    public boolean verifyBulkDeleteRule(List<ConjugationGenRule> rules) {
        String message = "You are about to bulk delete the following rule(s):\n\n";
        
        for (ConjugationGenRule rule : rules) {
            message += rule.getName() + "\n";
        }
        
        message += "Continue?";
        
        return core.getOSHandler().getInfoBox().actionConfirmation("Confirm Rule Copy", message);
    }
    
    /**
     * Gets all currently selected rules
     * @return list of all selected rule objects
     */
    private List<ConjugationGenRule> getSelectedRules() {
        List<ConjugationGenRule> rules = new ArrayList<>();

        for (int i : lstRules.getSelectedIndices()) {
            ConjugationGenRule copyRule = new ConjugationGenRule();
            copyRule.setEqual((ConjugationGenRule) lstRules.getModel().getElementAt(i), true);
            rules.add(copyRule);
        }
        
        return rules;
    }

    /**
     * Bulk updates all matching rules to match the selected one
     */
    private void bulkUpdate() {
        saveVolatileValues();
        List<ConjugationGenRule> selectedRules = getSelectedRules();
        
        if (verifyBulkUpdateRule(selectedRules)) {
            core.getConjugationManager().bulkUpdateRuleInConjugationTemplates(typeId, selectedRules);
            populateRules();
            populateRuleProperties();
            populateTransforms();
        }
    }
    
    /**
     * Bulk deletes selected rule from current part of speech
     */
    private void bulkDelete() {
        List<ConjugationGenRule> selectedRules = getSelectedRules();
        
        if (verifyBulkDeleteRule(selectedRules)) {
            core.getConjugationManager().bulkDeleteRuleFromConjugationTemplates(typeId, selectedRules);
            populateRules();
            populateRuleProperties();
            populateTransforms();
        }
    }
    
    /**
     * Copies selected rule (if any) to clipboard
     */
    private void copyRuleToClipboard() {
        this.saveTransPairs(lstRules.getSelectedIndex());
        PolyGlot.getPolyGlot().setClipBoard(getSelectedRules());
    }

    /**
     * If rule exists on clipboard, copy to current rule list (with appropriate changes to type made, if necessary)
     */
    private void pasteRuleFromClipboard() {
        Object fromClipBoard = PolyGlot.getPolyGlot().getClipBoard();
        ConjugationPair curPair = (ConjugationPair) lstCombinedDec.getSelectedValue();

        if (!(fromClipBoard instanceof ArrayList)
                || ((ArrayList) fromClipBoard).isEmpty()
                || !(((ArrayList) fromClipBoard).get(0) instanceof ConjugationGenRule)
                || curPair == null) {
            return;
        }

        List<ConjugationGenRule> rules = (ArrayList<ConjugationGenRule>) fromClipBoard;

        rules.forEach((curRule) -> {
            ConjugationGenRule copyRule = new ConjugationGenRule(typeId, curPair.combinedId);
            copyRule.setEqual(curRule, false);

            core.getConjugationManager().addConjugationGenRule(copyRule);
            rulesModel.addElement(copyRule);
            lstRules.setSelectedValue(copyRule, true);
        });

        populateRuleProperties();
        populateTransforms();
        enableTransformEditing(true);
    }

    /**
     * Saves transformation pairs to appropriate rule
     *
     * @param saveIndex index of rule to save to
     */
    private void saveTransPairs(int saveIndex) {
        if (saveIndex == -1 || lstRules.getSelectedIndices().length > 1) {
            return;
        }

        ConjugationGenRule saveRule = (ConjugationGenRule) rulesModel.get(saveIndex);

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

            saveRule.addTransform(new ConjugationGenTransform(regex, replaceText));
        }
    }

    /**
     * Sets currently selected rule's name equal to proper text box if not already equal
     */
    private void setRuleName() {
        ConjugationGenRule rule = (ConjugationGenRule) lstRules.getSelectedValue();
        String ruleName = txtRuleName.getText().trim();

        if (!curPopulating && rule != null && !rule.getName().equals(ruleName)) {
            rule.setName(ruleName);
            lstRules.updateUI();
        }
    }

    /**
     * Sets currently selected rule's regex equal to proper text box if not already equal
     */
    private void setRuleRegex() {
        ConjugationGenRule rule = (ConjugationGenRule) lstRules.getSelectedValue();
        String ruleRegex = txtRuleRegex.getText().trim();

        if (!curPopulating && rule != null) {
            rule.setRegex(ruleRegex);
        }
    }

    /**
     * adds new rule
     */
    private void addRule() {
        ConjugationPair curPair = (ConjugationPair) lstCombinedDec.getSelectedValue();

        if (curPair == null) {
            return;
        }

        saveTransPairs(lstRules.getSelectedIndex());

        ConjugationGenRule newRule = new ConjugationGenRule(typeId, curPair.combinedId);

        core.getConjugationManager().addConjugationGenRule(newRule);
        populateRules();
        lstRules.setSelectedIndex(lstRules.getLastVisibleIndex());
        txtRuleName.setText("");
        txtRuleRegex.setText(".*");
        populateTransforms();
        enableTransformEditing(true);
        ((PClassCheckboxPanel)pnlApplyClasses).setRule(newRule, core);
    }

    /**
     * deletes currently selected rule
     */
    private void deleteRule() {
        if (!core.getOSHandler().getInfoBox().deletionConfirmation()) {
            return;
        }

        if (lstRules.getSelectedValue() == null) {
            return;
        }

        for (int i : lstRules.getSelectedIndices()) {
            lstRules.setSelectedIndex(i);
            core.getConjugationManager().deleteConjugationGenRule((ConjugationGenRule) lstRules.getSelectedValue());
        }

        populateRules();
        populateRuleProperties();
        populateTransforms();
    }

    /**
     * adds transform set to currently selected rule
     */
    private void addTransform() { // TODO: somehow integrate RTL letters here?
        if (lstRules.getSelectedValue() == null) {
            return;
        }

        transModel.addRow(new Object[]{"", ""});
    }

    /**
     * deletes currently selected transform from currently selected rule
     */
    private void deleteTransform() {
        if (!core.getOSHandler().getInfoBox().deletionConfirmation()) {
            return;
        }

        if (lstRules.getSelectedValue() != null
                && tblTransforms.getSelectedRow() != -1) {
            int removeRow = tblTransforms.convertRowIndexToModel(tblTransforms.getSelectedRow());
            transModel.removeRow(removeRow);
            tblTransforms.setModel(new DefaultTableModel());

            // perform this action later, once the model is properly updated
            SwingUtilities.invokeLater(() -> {
                tblTransforms.setModel(transModel);
                saveTransPairs(lstRules.getSelectedIndex());
                populateTransforms();
            });
        }
    }

    /**
     * move selected transform up in the list
     */
    private void moveTransformUp() {
        int tblIndex = tblTransforms.getSelectedRow();
        int index = tblTransforms.convertRowIndexToModel(tblIndex);
        int column = tblTransforms.getSelectedColumn();

        if (index <= 0) {
            return;
        }

        if (tblTransforms.getCellEditor() != null) {
            tblTransforms.getCellEditor().stopCellEditing();
        }

        String regex = transModel.getValueAt(index, 0).toString();
        String replaceText = transModel.getValueAt(index, 1).toString();

        transModel.removeRow(index);
        transModel.insertRow(index - 1, new Object[]{regex, replaceText});

        tblTransforms.changeSelection(tblIndex - 1, column, false, false);
    }

    /**
     * move selected transform down in list
     */
    private void moveTransformDown() {
        int tblIndex = tblTransforms.getSelectedRow();
        int index = tblTransforms.convertRowIndexToModel(tblIndex);
        int column = tblTransforms.getSelectedColumn();

        if (index == -1 || index == tblTransforms.getRowCount() - 1) {
            return;
        }

        if (tblTransforms.getCellEditor() != null) {
            tblTransforms.getCellEditor().stopCellEditing();
        }

        String regex = transModel.getValueAt(index, 0).toString();
        String replaceText = transModel.getValueAt(index, 1).toString();

        transModel.removeRow(index);
        transModel.insertRow(index + 1, new Object[]{regex, replaceText});

        tblTransforms.changeSelection(tblIndex + 1, column, false, false);
    }

    /**
     * move rule up in list
     */
    private void moveRuleUp() {
        // Deprecated rules stored as String. Maybe address that or something.
        if (lstCombinedDec.getSelectedValue() instanceof ConjugationPair
                && lstRules.getSelectedIndex() != -1) {
            int selectedIndex = lstRules.getSelectedIndex();
            ConjugationPair curPair = (ConjugationPair) lstCombinedDec.getSelectedValue();

            if (selectedIndex > 0 && curPair != null) {
                List<ConjugationGenRule> selectedRules = lstRules.getSelectedValuesList();
                core.getConjugationManager().moveRulesUp(typeId, curPair.combinedId, selectedRules);
                populateRules();
                int[] selectedIndicies = new int[selectedRules.size()];
                for (int i = 0; i < selectedRules.size(); i++) {
                    selectedIndicies[i] = selectedIndex - 1 + i;
                }
                lstRules.setSelectedIndices(selectedIndicies);
            }
        }
    }

    /**
     * move rule down in list
     */
    private void moveRuleDown() {
        // Deprecated rules stored as String. Maybe address that or something.
        if (lstCombinedDec.getSelectedValue() instanceof ConjugationPair 
                && lstRules.getSelectedIndex() != -1) {
            int[] fullSelection = lstRules.getSelectedIndices();
            int topIndex = fullSelection[0];
            int bottomIndex = fullSelection[fullSelection.length - 1];
            ConjugationPair curPair = (ConjugationPair) lstCombinedDec.getSelectedValue();

            if (bottomIndex < lstRules.getLastVisibleIndex() && curPair != null) {
                List<ConjugationGenRule> selectedRules = lstRules.getSelectedValuesList();
                core.getConjugationManager().moveRulesDown(typeId, curPair.combinedId, selectedRules);
                populateRules();
                int[] selectedIndicies = new int[selectedRules.size()];
                for (int i = 0; i < selectedRules.size(); i++) {
                    selectedIndicies[i] = topIndex + 1 + i;
                }
                lstRules.setSelectedIndices(selectedIndicies);
            }
        }
    }
    
    @Override
    public Component getWindow() {
        return jPanel2;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new PLabel("", menuFontSize);
        jScrollPane1 = new javax.swing.JScrollPane();
        lstCombinedDec = new PList(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal(), menuFontSize);
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new PLabel("", menuFontSize);
        jScrollPane2 = new javax.swing.JScrollPane();
        lstRules = new PList(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal(), menuFontSize);
        btnAddRule = new PAddRemoveButton("+");
        btnDeleteRule = new PAddRemoveButton("-");
        chkDisableWordform = new PCheckBox(nightMode, menuFontSize);
        btnMoveRuleUp = new PButton(nightMode, menuFontSize); btnMoveRuleUp.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontMenu()) ;
        btnMoveRuleDown = new PButton(nightMode, menuFontSize); btnMoveRuleDown.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontMenu());
        jPanel3 = new javax.swing.JPanel();
        txtRuleName = new PTextField(core, true, "Name");
        txtRuleRegex = new PTextField(core,
            core.getPropertiesManager().isOverrideRegexFont(),
            "Filter Regex");
        jLabel3 = new PLabel("", menuFontSize);
        sclTransforms = new javax.swing.JScrollPane();
        tblTransforms = new PTable(core);
        btnAddTransform = new PAddRemoveButton("+");
        btnMoveTransformUp = new PButton(nightMode, menuFontSize); btnMoveTransformUp.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontMenu());
        btnMoveTransformDown = new PButton(nightMode, menuFontSize); btnMoveTransformDown.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontMenu());
        btnDeleteTransform = new PAddRemoveButton("-");
        scrApplyClasses = new javax.swing.JScrollPane();
        pnlApplyClasses = new PClassCheckboxPanel(core, core.getTypes().getNodeById(typeId), true);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Conjugation/Declension Autogeneration Setup");
        setMinimumSize(new java.awt.Dimension(568, 532));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel1.setText("Conjugation/Declensions");

        lstCombinedDec.setModel(new javax.swing.AbstractListModel<Object>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstCombinedDec.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstCombinedDec.setToolTipText("This lists every possible form of this part of speech.");
        lstCombinedDec.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstCombinedDecValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstCombinedDec);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel2.setText("Rules");

        lstRules.setModel(new DefaultListModel<org.darisadesigns.polyglotlina.Nodes.ConjugationGenRule>());
        lstRules.setToolTipText("List of rules associated with the selected conjugation (right click to copy/paste)");
        lstRules.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstRulesValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(lstRules);

        btnAddRule.setToolTipText("Add Rule");
        btnAddRule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRuleActionPerformed(evt);
            }
        });

        btnDeleteRule.setToolTipText("Delete Rule");
        btnDeleteRule.setPreferredSize(new java.awt.Dimension(40, 29));
        btnDeleteRule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteRuleActionPerformed(evt);
            }
        });

        chkDisableWordform.setText("Disable Wordform");
        chkDisableWordform.setToolTipText("Disables currently selected conjugation/declension, and prevents it from being displayed at any point.");
        chkDisableWordform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDisableWordformActionPerformed(evt);
            }
        });

        btnMoveRuleUp.setText("↑");
        btnMoveRuleUp.setToolTipText("Move rule up");
        btnMoveRuleUp.setMaximumSize(new java.awt.Dimension(40, 29));
        btnMoveRuleUp.setMinimumSize(new java.awt.Dimension(40, 29));
        btnMoveRuleUp.setPreferredSize(new java.awt.Dimension(40, 29));
        btnMoveRuleUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveRuleUpActionPerformed(evt);
            }
        });

        btnMoveRuleDown.setText("↓");
        btnMoveRuleDown.setToolTipText("Move rule down");
        btnMoveRuleDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveRuleDownActionPerformed(evt);
            }
        });

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        txtRuleName.setToolTipText("Name of rule");
        txtRuleName.setEnabled(false);

        txtRuleRegex.setToolTipText("Only words matching this regex pattern will have transformations from this rule applied to them.");
        txtRuleRegex.setEnabled(false);

        jLabel3.setText("Transformations");

        sclTransforms.setToolTipText("Transformations to be applied. The pattern on the left will be replaced with characters on the right.");

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
        tblTransforms.setToolTipText("");
        tblTransforms.setCellSelectionEnabled(true);
        tblTransforms.setRowHeight(30);
        sclTransforms.setViewportView(tblTransforms);

        btnAddTransform.setToolTipText("Add Transformation");
        btnAddTransform.setEnabled(false);
        btnAddTransform.setPreferredSize(new java.awt.Dimension(40, 29));
        btnAddTransform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTransformActionPerformed(evt);
            }
        });

        btnMoveTransformUp.setText("↑");
        btnMoveTransformUp.setToolTipText("Move Transform Up");
        btnMoveTransformUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveTransformUpActionPerformed(evt);
            }
        });

        btnMoveTransformDown.setText("↓");
        btnMoveTransformDown.setToolTipText("Move TransformDown");
        btnMoveTransformDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveTransformDownActionPerformed(evt);
            }
        });

        btnDeleteTransform.setToolTipText("Delete Transformation");
        btnDeleteTransform.setPreferredSize(new java.awt.Dimension(40, 29));
        btnDeleteTransform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteTransformActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtRuleName)
            .addComponent(txtRuleRegex)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(btnAddTransform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
                                .addComponent(btnDeleteTransform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(sclTransforms, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnMoveTransformUp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMoveTransformDown, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(txtRuleName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtRuleRegex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnMoveTransformUp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnMoveTransformDown))
                    .addComponent(sclTransforms, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddTransform, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteTransform, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pnlApplyClasses.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout pnlApplyClassesLayout = new javax.swing.GroupLayout(pnlApplyClasses);
        pnlApplyClasses.setLayout(pnlApplyClassesLayout);
        pnlApplyClassesLayout.setHorizontalGroup(
            pnlApplyClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 165, Short.MAX_VALUE)
        );
        pnlApplyClassesLayout.setVerticalGroup(
            pnlApplyClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 333, Short.MAX_VALUE)
        );

        scrApplyClasses.setViewportView(pnlApplyClasses);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(btnAddRule, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnDeleteRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2)
                    .addComponent(chkDisableWordform))
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnMoveRuleUp, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnMoveRuleDown, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrApplyClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(chkDisableWordform)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addGap(9, 9, 9)
                        .addComponent(jScrollPane2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scrApplyClasses)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnMoveRuleUp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnMoveRuleDown)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddRule)
                    .addComponent(btnDeleteRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lstCombinedDecValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstCombinedDecValueChanged
        saveTransPairs(lstRules.getSelectedIndex());
        
        // in case of 'DEPRECATED RULES' selection
        if (lstCombinedDec.getSelectedValue() instanceof ConjugationPair) {
        ConjugationPair curPair = (ConjugationPair) lstCombinedDec.getSelectedValue();
            chkDisableWordform.setSelected(core.getConjugationManager()
                    .isCombinedConjlSurpressed(curPair == null ? "" : curPair.combinedId, typeId));
        } else {
            chkDisableWordform.setSelected(false);
        }
        populateRules();
        populateRuleProperties();
        populateTransforms();
    }//GEN-LAST:event_lstCombinedDecValueChanged

    private void btnAddRuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRuleActionPerformed
        addRule();
    }//GEN-LAST:event_btnAddRuleActionPerformed

    private void lstRulesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstRulesValueChanged
        // only fire this once
        if (!evt.getValueIsAdjusting()
                && !upDownPress) {
            return;
        }

        upDownPress = false;

        int selected = lstRules.getSelectedIndex();

        int previous;
        if (lstRules.getSelectedIndices().length > 1 || !tblTransforms.isEnabled()) {
            previous = -1;
        } else {
            previous = selected == evt.getFirstIndex() ? evt.getLastIndex() : evt.getFirstIndex();
        }

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

    private void chkDisableWordformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDisableWordformActionPerformed
        ConjugationPair curPair = (ConjugationPair) lstCombinedDec.getSelectedValue();

        if (curPair == null) {
            return;
        }

        core.getConjugationManager().setCombinedConjSuppressed(curPair.combinedId, typeId, chkDisableWordform.isSelected());

        enableEditing(!chkDisableWordform.isSelected()
                && lstCombinedDec.getSelectedIndex() != -1);
        enableTransformEditing(!chkDisableWordform.isSelected()
                && lstRules.getSelectedIndex() != -1);
    }//GEN-LAST:event_chkDisableWordformActionPerformed

    private void btnMoveRuleUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveRuleUpActionPerformed
        moveRuleUp();
    }//GEN-LAST:event_btnMoveRuleUpActionPerformed

    private void btnMoveRuleDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveRuleDownActionPerformed
        moveRuleDown();
    }//GEN-LAST:event_btnMoveRuleDownActionPerformed

    private void btnMoveTransformUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveTransformUpActionPerformed
        moveTransformUp();
    }//GEN-LAST:event_btnMoveTransformUpActionPerformed

    private void btnMoveTransformDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveTransformDownActionPerformed
        moveTransformDown();
    }//GEN-LAST:event_btnMoveTransformDownActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddRule;
    private javax.swing.JButton btnAddTransform;
    private javax.swing.JButton btnDeleteRule;
    private javax.swing.JButton btnDeleteTransform;
    private javax.swing.JButton btnMoveRuleDown;
    private javax.swing.JButton btnMoveRuleUp;
    private javax.swing.JButton btnMoveTransformDown;
    private javax.swing.JButton btnMoveTransformUp;
    private javax.swing.JCheckBox chkDisableWordform;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<Object> lstCombinedDec;
    private javax.swing.JList<org.darisadesigns.polyglotlina.Nodes.ConjugationGenRule> lstRules;
    private javax.swing.JPanel pnlApplyClasses;
    private javax.swing.JScrollPane sclTransforms;
    private javax.swing.JScrollPane scrApplyClasses;
    private javax.swing.JTable tblTransforms;
    private javax.swing.JTextField txtRuleName;
    private javax.swing.JTextField txtRuleRegex;
    // End of variables declaration//GEN-END:variables
}
