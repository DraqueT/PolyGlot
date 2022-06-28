/*
 * Copyright (c) 2015-2022, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButtonDropdown;
import org.darisadesigns.polyglotlina.ExternalCode.JFontChooser;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.PlainDocument;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCellEditor;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCellRenderer;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTable;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextFieldFilter;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextPane;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;

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
        
        Font charis = PGTUtil.CHARIS_UNICODE;
        btnAlphaUp.setFont(charis);
        btnAlphaDown.setFont(charis);
        chkTypesMandatory.setEnabled(!core.getTypes().getAllValues().isEmpty());
        
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
            propMan.setAlphaOrder(alphaOrder.stream().collect(Collectors.joining(",")) + ",");
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
        ((PlainDocument)txtConSize.getDocument()).setDocumentFilter(new PTextFieldFilter());
        txtConSize.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSize();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSize();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSize();
            }
            
            private void updateSize() {
                String text = txtConSize.getText();
                
                if (!text.isBlank()) {
                    propMan.setFontSize(Double.parseDouble(txtConSize.getText()));
                }
                
                updateScreenFonts();
            }
        });
        
        ((PlainDocument)txtLocalSize.getDocument()).setDocumentFilter(new PTextFieldFilter());
        txtLocalSize.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSize();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSize();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSize();
            }
            
            private void updateSize() {
                String text = txtLocalSize.getText();
                
                if (!text.isBlank()) {
                    propMan.setLocalFontSize(Double.parseDouble(txtLocalSize.getText()));
                }
                
                updateScreenFonts();
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
    
    private void updateScreenFonts() {
        populateAlphaTable();
        
        Font localFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
        
        txtLangName.setFont(localFont);
        txtAuthorCopyright.setFont(localFont);
        txtLocalLanguage.setFont(localFont);
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
        txtConSize.setText(Double.toString(propMan.getFontSize()));
        txtLocalSize.setText(Double.toString(propMan.getLocalFontSize()));
        
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
        } catch (Exception e) {
            core.getOSHandler().getInfoBox().error("Font Caching Error",
                    "Unable to locate physical font file or font file incompatible with java. If your font uses ligatures, they may not appear correctly.\n"
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
    
    private JPopupMenu getConFontPopupMenu() {
        JPopupMenu ret = new JPopupMenu();
        
        JMenuItem selectFile = new JMenuItem("Select Font File");
        selectFile.setToolTipText("Select conlang font via direct file selection (recommended)");
        selectFile.addActionListener((ActionEvent e) -> {
            var fontDialog = new ScrFontImportDialog(core);
            fontDialog.setConSelected();
            fontDialog.setVisible(true);
        });
        
        JMenuItem osSelect = new JMenuItem("Font Selector Window");
        osSelect.setToolTipText("Select conlang font via OS based selection window (not recommended)");
        osSelect.addActionListener((ActionEvent e) -> {
            if (new DesktopInfoBox().yesNoCancel("Deprecated Functionality",
                    "It is recommended to maximize compatibility that you select your font files directly."
                            + "\nContinue to OS font selection anyway?") == JOptionPane.YES_OPTION) {
                Font selectedFont = fontDialog();

                if (selectedFont != null) {
                    setConFont(selectedFont, selectedFont.getStyle(), selectedFont.getSize());
                }
            }
        });
        
        ret.add(selectFile);
        ret.add(osSelect);  
        
        return ret;
    }
    
    private JPopupMenu getLocalFontPopupMenu() {
        JPopupMenu ret = new JPopupMenu();
        JMenuItem selectFile = new JMenuItem("Select Font File");
        selectFile.setToolTipText("Select local font via direct file selection (recommended)");
        selectFile.addActionListener((ActionEvent e) -> {
            var fontDialog = new ScrFontImportDialog(core);
            fontDialog.setLocalSelected();
            fontDialog.setVisible(true);
        });
        
        JMenuItem osSelect = new JMenuItem("Font Selector Window");
        osSelect.setToolTipText("Select local font via OS based selection window (not recommended)");
        osSelect.addActionListener((ActionEvent e) -> {
            if (new DesktopInfoBox().yesNoCancel("Deprecated Functionality",
                    "It is recommended to maximize compatibility that you select your font files directly."
                            + "\nContinue to OS font selection anyway?") == JOptionPane.YES_OPTION) {
                Font selectedFont = fontDialog();

                if (selectedFont != null) {
                    setLocalFont(selectedFont);
                }
            }
        });
        
        ret.add(selectFile);
        ret.add(osSelect); 
        
        ret.add(selectFile);
        ret.add(osSelect);
        
        return ret;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        btnChangeFont = new PButtonDropdown(getConFontPopupMenu());
        txtFont = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        chkTypesMandatory = new PCheckBox(nightMode);
        chkLocalMandatory = new PCheckBox(nightMode);
        chkWordUniqueness = new PCheckBox(nightMode);
        chkLocalUniqueness = new PCheckBox(nightMode);
        chkIgnoreCase = new PCheckBox(nightMode);
        chkDisableProcRegex = new PCheckBox(nightMode);
        chkEnforceRTL = new PCheckBox(nightMode);
        jLabel2 = new PLabel("");
        chkOverrideRegexFont = new PCheckBox(nightMode);
        chkUseLocalWordLex = new PCheckBox(nightMode);
        chkExpandedLexList = new PCheckBox(nightMode);
        btnFontLocal = new PButtonDropdown(getLocalFontPopupMenu());
        txtLocalFont = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAlphaProblems = new javax.swing.JTextArea();
        txtConSize = new javax.swing.JTextField();
        txtLocalSize = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        txtLangName = new PTextField(core, true, "Language Name");
        txtLocalLanguage = new PTextField(core, true, "Local Language");
        jScrollPane4 = new javax.swing.JScrollPane();
        txtAuthorCopyright = new PTextPane(core, true, "-- Author/Copyright Info --");
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAlphabet = new PTable(core);
        jLabel4 = new PLabel("");
        btnAlphaUp = new PButton(nightMode);
        btnAlphaDown = new PButton(nightMode);
        btnDeleteAlpha = new PAddRemoveButton("-");
        btnAddAlpha = new PAddRemoveButton("+");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Language Properties");
        setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        btnChangeFont.setText("Conlang Font");
        btnChangeFont.setToolTipText("Change conlang font");
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
                .addContainerGap(107, Short.MAX_VALUE))
        );

        btnFontLocal.setText("Local Font");
        btnFontLocal.setToolTipText("Change local language font");
        btnFontLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFontLocalActionPerformed(evt);
            }
        });

        txtLocalFont.setToolTipText("Local Language Font");
        txtLocalFont.setEnabled(false);

        txtAlphaProblems.setEditable(false);
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
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(btnFontLocal, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtLocalSize))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(btnChangeFont, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtConSize, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtFont)
                            .addComponent(txtLocalFont))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnChangeFont)
                        .addComponent(txtConSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFontLocal)
                    .addComponent(txtLocalFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLocalSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            .addComponent(txtLangName, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
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
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
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
                        .addComponent(btnAlphaUp)
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
        var fontDialog = new ScrFontImportDialog(core);
        fontDialog.setConSelected();
        fontDialog.setVisible(true);
    }//GEN-LAST:event_btnChangeFontActionPerformed

    private void chkEnforceRTLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkEnforceRTLActionPerformed
        // needs to update value immediately for text elements on this form affected by change
        core.getPropertiesManager().setEnforceRTL(chkEnforceRTL.isSelected());
        testRTLWarning();
    }//GEN-LAST:event_chkEnforceRTLActionPerformed

    private void btnFontLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFontLocalActionPerformed
        var fontDialog = new ScrFontImportDialog(core);
        fontDialog.setLocalSelected();
        fontDialog.setVisible(true);
    }//GEN-LAST:event_btnFontLocalActionPerformed

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
            if (new DesktopInfoBox().actionConfirmation("Disable Regex?", "You have recursion enabled in the Phonology section. "
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable tblAlphabet;
    private javax.swing.JTextArea txtAlphaProblems;
    private javax.swing.JTextPane txtAuthorCopyright;
    private javax.swing.JTextField txtConSize;
    private javax.swing.JTextField txtFont;
    private javax.swing.JTextField txtLangName;
    private javax.swing.JTextField txtLocalFont;
    private javax.swing.JTextField txtLocalLanguage;
    private javax.swing.JTextField txtLocalSize;
    // End of variables declaration//GEN-END:variables
}
