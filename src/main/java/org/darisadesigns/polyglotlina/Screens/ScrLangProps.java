/*
 * Copyright (c) 2015-2021, Draque Thompson, draquemail@gmail.com
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
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.ExternalCode.JFontChooser;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCellEditor;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCellRenderer;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTable;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextPane;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopOptionsManager;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;

/**
 *
 * @author draque
 */
public class ScrLangProps extends PFrame {

    private final DecimalFormat decimalFormat;
    private final DesktopPropertiesManager propMan;

    /**
     * Creates new form ScrLangProps
     *
     * @param _core Dictionary Core
     */
    public ScrLangProps(DictCore _core) {
        super(_core);
        propMan = (DesktopPropertiesManager)core.getPropertiesManager();

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.setGroupingUsed(false);

        initComponents();
        populateProperties();
        setAlphaLegal();
        
        float fontSize = (float)((DesktopOptionsManager)PolyGlot.getPolyGlot().getOptionsManager()).getMenuFontSize();
        Font charis = PGTUtil.CHARIS_UNICODE.deriveFont(fontSize);
        btnAlphaUp.setFont(charis);
        btnAlphaDown.setFont(charis);
        
        super.getRootPane().getContentPane().setBackground(Color.white);
        this.setupListeners();
    }

    /**
     * Sets up custom labeling that can't be modified in generated code
     */
    public void setCustomLabels() {
        chkLocalMandatory.setToolTipText("Check to enforce as mandatory a(n) "
                + core.localLabel() + " word on each created lexicon entry.");
        chkLocalUniqueness.setToolTipText("Check to enforce as mandatory uniqueness in entries on the "
                + core.localLabel() + " word field.");
        btnChangeFont.setText(core.conLabel() + " Font");
        txtFont.setToolTipText(core.conLabel() + " Font");
        txtLocalFont.setToolTipText(core.localLabel() + " Font");
    }
    
    private List<String> getAlphaOrderFromTable() {
        var alphaOrder = new ArrayList<String>();
        var curSelected = tblAlphabet.getSelectedRow();
        
        for (int i = 0; i < tblAlphabet.getRowCount(); i++) {
            var renderer = (PCellRenderer)tblAlphabet.getCellRenderer(i, 0);
            var val = renderer.getStringContents();
            
            var cellEditor = (PCellEditor)((PTable)tblAlphabet).getCellEditor();
            if (i == curSelected && cellEditor != null) {
                val = (String)cellEditor.getCellEditorValue();
            }
            
            alphaOrder.add(val);
        }
        
        return alphaOrder;
    }
    
    /**
     * Saves alphabetic order and updates errored status if necessary
     */
    private void saveAlphaOrder() {
        try {
            var alphaOrder = getAlphaOrderFromTable();
            propMan.setAlphaOrder(alphaOrder.stream().collect(Collectors.joining(",")));
        } catch (Exception e) {
            setAlphaProblems(e.getLocalizedMessage());
            return;
        }

        setAlphaLegal();
    }
    
    private void moveAlphaUp() {
        int curSelection = tblAlphabet.getSelectedRow();
        
        if (curSelection > 0) {
            var editor = tblAlphabet.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }

            var model = (DefaultTableModel)tblAlphabet.getModel();
            Object value = model.getValueAt(curSelection - 1, 0);
            model.setValueAt(model.getValueAt(curSelection, 0), curSelection -1, 0);
            model.setValueAt(value, curSelection, 0);
            tblAlphabet.changeSelection(curSelection - 1, 0, false, false);
            saveAlphaOrder();
        }
    }
    
    private void moveAlphaDown() {
        int curSelection = tblAlphabet.getSelectedRow();
        
        if (curSelection < tblAlphabet.getRowCount() - 1) {
            var editor = tblAlphabet.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }

            var model = (DefaultTableModel)tblAlphabet.getModel();
            Object value = model.getValueAt(curSelection + 1, 0);
            model.setValueAt(model.getValueAt(curSelection, 0), curSelection +1, 0);
            model.setValueAt(value, curSelection, 0);
            tblAlphabet.changeSelection(curSelection + 1, 0, false, false);
            saveAlphaOrder();
        }
    }
    
    private void addAlpha() {
        var editor = tblAlphabet.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }
        
        int curSelection = tblAlphabet.getSelectedRow();
        var model = (DefaultTableModel)tblAlphabet.getModel();
        
        model.insertRow(curSelection + 1, new String[]{""});
        tblAlphabet.changeSelection(curSelection + 1, 0, false, false);
    }
    
    private void deleteAlpha() {
        int curSelection = tblAlphabet.getSelectedRow();
        
        if (curSelection > -1) {
            var editor = tblAlphabet.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
            
            var model = (DefaultTableModel)tblAlphabet.getModel();
            
            model.removeRow(curSelection);
            
            if (curSelection > 0) {
                tblAlphabet.changeSelection(curSelection - 1, 0, false, false);
            } else if (tblAlphabet.getRowCount() > 0) {
                tblAlphabet.changeSelection(0, 0, false, false);
            }
            
            saveAlphaOrder();
        }
    }

    private void setupListeners() {
        txtKerning.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                save();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                save();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                save();
            }

            private void save() {
                try {
                    core.getPropertiesManager().setKerningSpace(Double.parseDouble(txtKerning.getText()));
                }
                catch (NumberFormatException e) {
                    // do nothing. This fails on non-numeric values, which is handled elsewhere
                    // IOHandler.writeErrorLog(e);
                }
            }
        });
        
        ((PTable)tblAlphabet).setDefaultCellListender(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                saveAlphaOrder();
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
                saveAlphaOrder();
            }
        });
        
        ((PTable)tblAlphabet).setDefaultCellFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // nothing
            }

            @Override
            public void focusLost(FocusEvent e) {
                saveAlphaOrder();
            }
        });
        
        ((PTable)tblAlphabet).setDefaultCellEditorDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                saveAlphaOrder();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveAlphaOrder();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                saveAlphaOrder();
            }
        });
    }
    
    private void populateAlphaTable() {
        DefaultTableModel procTableModel = new DefaultTableModel();
        procTableModel.addColumn("");
        tblAlphabet.setFont(propMan.getFontCon());
        
        for (var character : propMan.getOrderedAlphaList()) {
            procTableModel.addRow(new String[]{character});
        }
        
        tblAlphabet.setModel(procTableModel);
    }

    // sets problems for the alphabet order and makes appropriate ui updates
    private void setAlphaProblems(String errors) {
        if (errors.isBlank()) {
            tblAlphabet.setBackground(PGTUtil.COLOR_TEXT_BG);
        } else {
            tblAlphabet.setBackground(PGTUtil.COLOR_REQUIRED_LEX_COLOR);
        }
        
        txtAlphaProblems.setText(errors);
    }
    
    // detects whether the alphabet covers all characters in words of the lexicon and sets screen up appropriately
    private void setAlphaLegal() {
        setAlphaProblems("");
        
        if (tblAlphabet.getRowCount() > 0) {
            if (!core.getWordCollection().canSafelySort()) {
                setAlphaProblems("There is a logical problem in your alphabetic ordering system. If using values greater than 2 characters, ensure that there can be no ambiguity in order.");
            } else if (!core.getPropertiesManager().isAlphabetComplete()) {
                setAlphaProblems("Characters missing from Alpha Order. Please select Tools->Check Language to see which words contain unordered characters. (Note: some characters look the same, but are not!)");
            } else if (alphaContainsRegexChars()) {
                setAlphaProblems("Some of the characters defined in your alphabetic order are used "
                        + "in regular expressions. If you are planning on autogenerating pronunciations or "
                        + "conjugations/declensions, please consider using alternate characters from these:\n\n"
                        + "[ ] \\ ^ $ . | ? * + ( ) { }");
            }
        }
    }

    @Override
    public void updateAllValues(DictCore _core) {
        core = _core;
        populateProperties();
    }

    @Override
    public void dispose() {
        saveAllValues();
        core.pushUpdate();
        super.dispose();
    }

    @Override
    public void saveAllValues() {
        propMan.setDisableProcRegex(chkDisableProcRegex.isSelected());
        propMan.setIgnoreCase(chkIgnoreCase.isSelected());
        propMan.setLangName(txtLangName.getText());
        propMan.setCopyrightAuthorInfo(txtAuthorCopyright.getText());
        propMan.setLocalLangName(txtLocalLanguage.getText());
        propMan.setLocalMandatory(chkLocalMandatory.isSelected());
        propMan.setLocalUniqueness(chkLocalUniqueness.isSelected());
        propMan.setTypesMandatory(chkTypesMandatory.isSelected());
        propMan.setWordUniqueness(chkWordUniqueness.isSelected());
        propMan.setEnforceRTL(chkEnforceRTL.isSelected());
        propMan.setOverrideRegexFont(chkOverrideRegexFont.isSelected());
        propMan.setExpandedLexListDisplay(chkExpandedLexList.isSelected());
        propMan.setUseLocalWordLex(chkUseLocalWordLex.isSelected());
        propMan.setKerningSpace(Double.parseDouble(txtKerning.getText()));

        TableCellEditor cellEdit = tblAlphabet.getCellEditor();
        if (cellEdit != null) {
            cellEdit.stopCellEditing();
        }
        
        saveAlphaOrder();
    }

    private void populateProperties() {
        txtLangName.setText(propMan.getLangName());
        txtFont.setText(propMan.getFontCon().getFamily());
        txtLocalFont.setText(propMan.getFontLocal().getFamily());
        txtLocalLanguage.setText(propMan.getLocalLangName());
        txtAuthorCopyright.setText(propMan.getCopyrightAuthorInfo());
        chkDisableProcRegex.setSelected(propMan.isDisableProcRegex());
        chkIgnoreCase.setSelected(propMan.isIgnoreCase());
        chkLocalMandatory.setSelected(propMan.isLocalMandatory());
        chkLocalUniqueness.setSelected(propMan.isLocalUniqueness());
        chkTypesMandatory.setSelected(propMan.isTypesMandatory());
        chkWordUniqueness.setSelected(propMan.isWordUniqueness());
        chkEnforceRTL.setSelected(propMan.isEnforceRTL());
        chkOverrideRegexFont.setSelected(propMan.isOverrideRegexFont());
        chkExpandedLexList.setSelected(propMan.isExpandedLexListDisplay());
        chkUseLocalWordLex.setSelected(propMan.isUseLocalWordLex());
        txtKerning.setValue(propMan.getKerningSpace());
        
        populateAlphaTable();
    }

    /**
     * Instantiates font chooser and returns user defined font
     *
     * @return font selected by user
     */
    private Font fontDialog() {
        JFontChooser fontChooser = new JFontChooser(core);
        setAlwaysOnTop(false);
        int result = fontChooser.showDialog(btnChangeFont);
        Font font = null;

        if (result == JFontChooser.OK_OPTION) {
            font = fontChooser.getSelectedFont();
        }
        setAlwaysOnTop(true);

        return font;
    }

    private void setConFont(Font _font, int fontStyle, int fontSize) {
        propMan.setFontCon(_font, fontStyle, fontSize);
        Font conFont = propMan.getFontCon();
        txtFont.setText(conFont.getFamily());

        try {
            boolean synced = propMan.syncCachedFontCon();

            if (!synced) {
                core.getOSHandler().getInfoBox().warning("Font Not Cached",
                        "Unable to locate physical font file. If your font uses ligatures, they may not appear correctly.\n"
                        + "To address this, please load your font manually via Tools->Import Font");
            }
        }
        catch (Exception e) {
            core.getOSHandler().getInfoBox().error("Font Caching Error",
                    "Unable to locate physical font file. If your font uses ligatures, they may not appear correctly.\n"
                    + "To address this, please load your font manually via Tools->Import Font\n\nError: " + e.getLocalizedMessage());
        }

        testRTLWarning();
        core.pushUpdate();
    }

    private void setLocalFont(Font localFont) {
        if (localFont != null) {

            propMan.setLocalFont(localFont, localFont.getSize2D());

            txtLocalFont.setText(localFont.getFamily());
            core.pushUpdate();
        }
    }

    /**
     * Displays warning to user if RTL is enforced and confont is standard
     */
    private void testRTLWarning() {
        Font conFont = propMan.getFontCon();
        Font stdFont = (new JTextField()).getFont();

        if (core.getPropertiesManager().isEnforceRTL()
                && (conFont == null
                || conFont.getFamily().equals(stdFont.getFamily()))) {
            core.getOSHandler().getInfoBox().warning("RTL Font Warning", "Enforcing RTL with default font"
                    + " is not recommended. For best results, please set distinct"
                    + " conlang font.");
        }
    }

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

    /**
     * Alerts user if their alphabetic order contains regex characters
     */
    private boolean alphaContainsRegexChars() {
        for (var letter : getAlphaOrderFromTable()) {
            if (letter.matches(".*(\\[|\\]|\\{|\\}|\\\\|\\^|\\$|\\.|\\||\\?|\\*|\\+|\\(|\\)).*")) { 
                return true;
            }
        }
        
        return false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        btnChangeFont = new PButton(nightMode, menuFontSize);
        txtFont = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        chkTypesMandatory = new PCheckBox(nightMode, menuFontSize);
        chkLocalMandatory = new PCheckBox(nightMode, menuFontSize);
        chkWordUniqueness = new PCheckBox(nightMode, menuFontSize);
        chkLocalUniqueness = new PCheckBox(nightMode, menuFontSize);
        chkIgnoreCase = new PCheckBox(nightMode, menuFontSize);
        chkDisableProcRegex = new PCheckBox(nightMode, menuFontSize);
        chkEnforceRTL = new PCheckBox(nightMode, menuFontSize);
        jLabel2 = new PLabel("", menuFontSize);
        chkOverrideRegexFont = new PCheckBox(nightMode, menuFontSize);
        chkUseLocalWordLex = new PCheckBox(nightMode, menuFontSize);
        chkExpandedLexList = new PCheckBox(nightMode, menuFontSize);
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new PLabel("", menuFontSize);
        txtKerning = new javax.swing.JFormattedTextField(decimalFormat);
        btnFontLocal = new PButton(nightMode, menuFontSize);
        txtLocalFont = new javax.swing.JTextField();
        btnFontRefresh = new PButton(nightMode, menuFontSize);
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAlphaProblems = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        txtLangName = new PTextField(core, true, "-- Language Name --");
        txtLocalLanguage = new PTextField(core, true, "-- Local Language --");
        jScrollPane4 = new javax.swing.JScrollPane();
        txtAuthorCopyright = new PTextPane(core, true, "-- Author/Copyright Info --");
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAlphabet = new PTable(core);
        jLabel4 = new javax.swing.JLabel();
        btnAlphaUp = new PButton(nightMode, menuFontSize);
        btnAlphaDown = new PButton(nightMode, menuFontSize);
        btnDeleteAlpha = new PAddRemoveButton("-");
        btnAddAlpha = new PAddRemoveButton("+");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Language Properties");
        setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        btnChangeFont.setText("Conlang Font");
        btnChangeFont.setToolTipText("Change native conlang font");
        btnChangeFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeFontActionPerformed(evt);
            }
        });

        txtFont.setToolTipText("Conlang Font");
        txtFont.setEnabled(false);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setMaximumSize(new java.awt.Dimension(470, 32767));

        chkTypesMandatory.setText("Part of Speech Mandatory");
        chkTypesMandatory.setToolTipText("Select to enforce selection of a part of speech on each created word.");

        chkLocalMandatory.setText("Local Mandatory");
        chkLocalMandatory.setToolTipText("Select to enforce mandatory values for local word equivalents.");

        chkWordUniqueness.setText("Word Uniqueness");
        chkWordUniqueness.setToolTipText("Select to enforce mandatory uniqueness of each word");

        chkLocalUniqueness.setText("Local Uniqueness");
        chkLocalUniqueness.setToolTipText("Select to enforce mandatory uniqueness for local word equivalents.");

        chkIgnoreCase.setText("Ignore Case");
        chkIgnoreCase.setToolTipText("Ignore character casing through PolyGlot. (only applies to western characters)");
        chkIgnoreCase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkIgnoreCaseActionPerformed(evt);
            }
        });

        chkDisableProcRegex.setText("Disable Orthographic Regex");
        chkDisableProcRegex.setToolTipText("Disable regex features in orthograpy. (this allows for ignoring case properly)");
        chkDisableProcRegex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDisableProcRegexActionPerformed(evt);
            }
        });

        chkEnforceRTL.setText("Enforce RTL");
        chkEnforceRTL.setToolTipText("Check this to force all conlang text to appear in RTL fashion through PolyGlot. This works even if the character set you are using is not typically RTL.");
        chkEnforceRTL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkEnforceRTLActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Language Toggles");

        chkOverrideRegexFont.setText("Override Regex Font");
        chkOverrideRegexFont.setToolTipText("This overrides the font of all display elements within PolyGlot to show a default font (if your script makes regexes hard to work with)");

        chkUseLocalWordLex.setText("Local Word Lex Display");
        chkUseLocalWordLex.setToolTipText("Display the local word rather than the conlang word in the lexicon display");

        chkExpandedLexList.setText("Expanded Lexicon List Text");
        chkExpandedLexList.setToolTipText("Show both the conword and local definition side by side in the lexicon list display");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkTypesMandatory)
                            .addComponent(chkWordUniqueness)
                            .addComponent(chkDisableProcRegex)
                            .addComponent(chkLocalMandatory)
                            .addComponent(chkUseLocalWordLex))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkExpandedLexList)
                            .addComponent(chkOverrideRegexFont)
                            .addComponent(chkEnforceRTL)
                            .addComponent(chkLocalUniqueness)
                            .addComponent(chkIgnoreCase))
                        .addGap(0, 47, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkTypesMandatory)
                    .addComponent(chkLocalUniqueness))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkWordUniqueness)
                    .addComponent(chkIgnoreCase))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkDisableProcRegex)
                    .addComponent(chkEnforceRTL))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkLocalMandatory)
                    .addComponent(chkOverrideRegexFont))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkUseLocalWordLex)
                    .addComponent(chkExpandedLexList))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Kerning");

        txtKerning.setToolTipText("Values between -0.1 and 0.3 work best. 0 is default(blank) WARNING: Values over 0 will cause PolyGlot to ignore ligatures.");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtKerning, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtKerning, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnFontLocal.setText("Local Font");
        btnFontLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFontLocalActionPerformed(evt);
            }
        });

        txtLocalFont.setToolTipText("Local Language Font");
        txtLocalFont.setEnabled(false);

        btnFontRefresh.setText("Refresh Fonts");
        btnFontRefresh.setToolTipText("Refreshes fonts used in PolyGlot from those stored installed locally on your system");
        btnFontRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFontRefreshActionPerformed(evt);
            }
        });

        txtAlphaProblems.setBackground(new java.awt.Color(230, 230, 230));
        txtAlphaProblems.setColumns(20);
        txtAlphaProblems.setForeground(new java.awt.Color(255, 0, 0));
        txtAlphaProblems.setLineWrap(true);
        txtAlphaProblems.setRows(1);
        txtAlphaProblems.setWrapStyleWord(true);
        jScrollPane2.setViewportView(txtAlphaProblems);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnChangeFont, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFont))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnFontLocal, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtLocalFont))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnFontRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnChangeFont)
                    .addComponent(txtFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFontLocal)
                    .addComponent(txtLocalFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFontRefresh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        txtLangName.setToolTipText("Your Conlang's Name");

        txtLocalLanguage.setToolTipText("The natural language you use when writing your conlang");

        jScrollPane4.setViewportView(txtAuthorCopyright);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtLangName, javax.swing.GroupLayout.DEFAULT_SIZE, 667, Short.MAX_VALUE)
            .addComponent(txtLocalLanguage, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jScrollPane4)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtLangName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtLocalLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblAlphabet.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblAlphabet);

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Alphabetic Order");

        btnAlphaUp.setText("↑");
        btnAlphaUp.setToolTipText("Move selected letter up in alphabetic order");
        btnAlphaUp.setMaximumSize(new java.awt.Dimension(75, 29));
        btnAlphaUp.setMinimumSize(new java.awt.Dimension(75, 29));
        btnAlphaUp.setPreferredSize(new java.awt.Dimension(75, 29));
        btnAlphaUp.setSize(new java.awt.Dimension(75, 29));
        btnAlphaUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlphaUpActionPerformed(evt);
            }
        });

        btnAlphaDown.setText("↓");
        btnAlphaDown.setToolTipText("Move selected letter down in alphabetic order");
        btnAlphaDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlphaDownActionPerformed(evt);
            }
        });

        btnDeleteAlpha.setToolTipText("Delete currently selected alphabetic entry");
        btnDeleteAlpha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteAlphaActionPerformed(evt);
            }
        });

        btnAddAlpha.setToolTipText("Add an additional alphabetic entry");
        btnAddAlpha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAlphaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAddAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDeleteAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAlphaUp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAlphaDown, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAlphaUp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAlphaDown))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddAlpha)
                    .addComponent(btnDeleteAlpha)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnChangeFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeFontActionPerformed
        // Font not set manually because JAVA IS BROKEN. Gotta pull the binary for ligatures to load...
        Font selectedFont = fontDialog();

        if (selectedFont != null) {
            setConFont(selectedFont, selectedFont.getStyle(), selectedFont.getSize());
        }
    }//GEN-LAST:event_btnChangeFontActionPerformed

    private void chkEnforceRTLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkEnforceRTLActionPerformed
        // needs to update value immediately for text elements on this form affected by change
        core.getPropertiesManager().setEnforceRTL(chkEnforceRTL.isSelected());
        testRTLWarning();
    }//GEN-LAST:event_chkEnforceRTLActionPerformed

    private void btnFontLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFontLocalActionPerformed
        setLocalFont(fontDialog());
    }//GEN-LAST:event_btnFontLocalActionPerformed

    private void btnFontRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFontRefreshActionPerformed
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            core.getPropertiesManager().refreshFonts();
            tblAlphabet.setFont(propMan.getFontCon());
        }
        catch (Exception e) {
            new DesktopInfoBox(this).error("Font Refresh Failed", e.getLocalizedMessage());
            DesktopIOHandler.getInstance().writeErrorLog(e, "Top level exception caught here. See prior exception.");
        }

        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnFontRefreshActionPerformed

    private void chkIgnoreCaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkIgnoreCaseActionPerformed
        if (chkIgnoreCase.isSelected()) {
            core.getOSHandler().getInfoBox().warning("Ignore Case Warning",
                    "This feature does not work with all charactrers, and can disrupt regex features. Please use with caution.");
        }
    }//GEN-LAST:event_chkIgnoreCaseActionPerformed

    private void chkDisableProcRegexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDisableProcRegexActionPerformed
        // if this is selected, inform users that recursion cannot be used at the same time
        if (chkDisableProcRegex.isSelected()
                && (core.getPronunciationMgr().isRecurse()
                || core.getRomManager().isRecurse())) {
            if (new DesktopInfoBox(this).actionConfirmation("Disable Regex?", "You have recursion enabled in the Phonology section. "
                    + "If you disable regex, this will also be disabled. Continue?")) {
                core.getPronunciationMgr().setRecurse(false);
                core.getRomManager().setRecurse(false);
            } else {
                chkDisableProcRegex.setSelected(false);
            }
        }
    }//GEN-LAST:event_chkDisableProcRegexActionPerformed

    private void btnAlphaUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlphaUpActionPerformed
        moveAlphaUp();
    }//GEN-LAST:event_btnAlphaUpActionPerformed

    private void btnAlphaDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlphaDownActionPerformed
        moveAlphaDown();
    }//GEN-LAST:event_btnAlphaDownActionPerformed

    private void btnAddAlphaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAlphaActionPerformed
        addAlpha();
    }//GEN-LAST:event_btnAddAlphaActionPerformed

    private void btnDeleteAlphaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteAlphaActionPerformed
        deleteAlpha();
    }//GEN-LAST:event_btnDeleteAlphaActionPerformed

    public static ScrLangProps run(DictCore _core) {
        ScrLangProps s = new ScrLangProps(_core);
        s.setCore(_core);
        return s;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddAlpha;
    private javax.swing.JButton btnAlphaDown;
    private javax.swing.JButton btnAlphaUp;
    private javax.swing.JButton btnChangeFont;
    private javax.swing.JButton btnDeleteAlpha;
    private javax.swing.JButton btnFontLocal;
    private javax.swing.JButton btnFontRefresh;
    private javax.swing.JCheckBox chkDisableProcRegex;
    private javax.swing.JCheckBox chkEnforceRTL;
    private javax.swing.JCheckBox chkExpandedLexList;
    private javax.swing.JCheckBox chkIgnoreCase;
    private javax.swing.JCheckBox chkLocalMandatory;
    private javax.swing.JCheckBox chkLocalUniqueness;
    private javax.swing.JCheckBox chkOverrideRegexFont;
    private javax.swing.JCheckBox chkTypesMandatory;
    private javax.swing.JCheckBox chkUseLocalWordLex;
    private javax.swing.JCheckBox chkWordUniqueness;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable tblAlphabet;
    private javax.swing.JTextArea txtAlphaProblems;
    private javax.swing.JTextPane txtAuthorCopyright;
    private javax.swing.JTextField txtFont;
    private javax.swing.JFormattedTextField txtKerning;
    private javax.swing.JTextField txtLangName;
    private javax.swing.JTextField txtLocalFont;
    private javax.swing.JTextField txtLocalLanguage;
    // End of variables declaration//GEN-END:variables
}
