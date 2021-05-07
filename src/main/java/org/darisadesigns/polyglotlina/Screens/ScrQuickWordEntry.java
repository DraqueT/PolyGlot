/*
 * Copyright (c) 2014-2020, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.CustomControls.PButton;
import org.darisadesigns.polyglotlina.CustomControls.PComboBox;
import org.darisadesigns.polyglotlina.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.CustomControls.PTextPane;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.Nodes.WordClassValue;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.darisadesigns.polyglotlina.Desktop.PropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;

/**
 *
 * @author draque
 */
public final class ScrQuickWordEntry extends PDialog {

    private final Map<Integer, Component> classComboMap = new HashMap<>();
    private final ScrLexicon parent;

    /**
     * Creates new form scrQuickWordEntry
     *
     * @param _core Dictionary core
     * @param _parent parent dictionary interface
     */
    public ScrQuickWordEntry(DictCore _core, ScrLexicon _parent) {
        super(_core);
        
        parent = _parent;

        initComponents();
        setupListeners();
        blankWord();

        // conword is always required and is initially selected
        txtConWord.setBackground(PGTUtil.COLOR_REQUIRED_LEX_COLOR);
        txtConWord.requestFocus();

        if (core.getPropertiesManager().isLocalMandatory()) {
            txtLocalWord.setBackground(PGTUtil.COLOR_REQUIRED_LEX_COLOR);
            chkLocal.setEnabled(false);
        }
        if (core.getPropertiesManager().isTypesMandatory()) {
            cmbType.setForeground(PGTUtil.COLOR_REQUIRED_LEX_COLOR);
            chkType.setEnabled(false);
        }

        populateTypes();
    }

    /**
     * Sets up all component listeners
     */
    private void setupListeners() {
        KeyListener enterListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                // User only wants to enter word if no popups are visible
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                        && !cmbType.isPopupVisible()
                        && !e.isShiftDown()) {
                    // tests all class comboboxes
                    for (Component curBox : classComboMap.values()) {
                        if (curBox instanceof PComboBox && ((PComboBox) curBox).isPopupVisible()) {
                            return;
                        }
                    }
                    tryRecord();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                /*DO NOTHING*/
            }

            @Override
            public void keyTyped(KeyEvent e) {
                /*DO NOTHING*/
            }
        };

        txtConWord.setFont(((PropertiesManager)core.getPropertiesManager()).getFontCon());
        txtConWord.addKeyListener(enterListener);
        txtConWord.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setProc();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setProc();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setProc();
            }
        });

        txtLocalWord.addKeyListener(enterListener);
        txtProc.addKeyListener(enterListener);
        cmbType.addKeyListener(enterListener);
        txtDefinition.addKeyListener(enterListener);
    }

    /**
     * Sets pronunciation value of word
     */
    private void setProc() {
        String proc = "";
        
        try {
            proc  = core.getPronunciationMgr()
                .getPronunciation(txtConWord.getText());
        } catch (Exception e) {
            // user error
            // IOHandler.writeErrorLog(e);
            new DesktopInfoBox(this).error("Regex Error", "Unable to generate pronunciation: " 
                    + e.getLocalizedMessage());
        }

        if (!proc.isEmpty()) {
            txtProc.setText(proc);
        }
    }

    @Override
    public void updateAllValues(DictCore _core) {
        core = _core;
        // ensure this is on the UI component stack to avoid read/writelocks...
        SwingUtilities.invokeLater(this::populateTypes);
    }

    private void populateTypes() {
        cmbType.removeAllItems();
        final String defLabel = "-- Part of Speech --";
        cmbType.addItem(defLabel);

        for (TypeNode curNode : core.getTypes().getNodes()) {
            cmbType.addItem(curNode);
        }
    }

    /**
     * records a word if appropriate, flashes required fields otherwise
     */
    private void tryRecord() {
        ConWord word = new ConWord();
        word.setCore(core);

        word.setValue(txtConWord.getText());
        word.setLocalWord(txtLocalWord.getText());
        word.setPronunciation(txtProc.getText());
        word.setDefinition(txtDefinition.getText());

        if (cmbType.getSelectedItem() instanceof TypeNode) {
            word.setWordTypeId(((TypeNode) cmbType.getSelectedItem()).getId());
        }

        // set class values
        classComboMap.entrySet().forEach((curEntry) -> {
            if (curEntry.getValue() instanceof PComboBox) {
                PComboBox boxEntry = (PComboBox) curEntry.getValue();
                if (boxEntry.getSelectedItem() instanceof WordClassValue) {
                    WordClassValue curValue = (WordClassValue) boxEntry.getSelectedItem();
                    word.setClassValue(curEntry.getKey(), curValue.getId());
                }
            } else if (curEntry.getValue() instanceof PTextField) {
                PTextField curText = (PTextField) curEntry.getValue();
                word.setClassTextValue(curEntry.getKey(), curText.getText());
            }
        });

        ConWord test = core.getWordCollection().testWordLegality(word);
        String testResults = "";

        if (!test.getValue().isEmpty()) {
            ((PTextField) txtConWord).makeFlash(PGTUtil.COLOR_REQUIRED_LEX_COLOR, true);
            testResults += test.getValue();
        }
        if (!test.getLocalWord().isEmpty()) {
            ((PTextField) txtLocalWord).makeFlash(PGTUtil.COLOR_REQUIRED_LEX_COLOR, true);
            testResults += ("\n" + test.getLocalWord());
        }
        try {
            if (!test.getPronunciation().isEmpty()) {
                ((PTextField) txtProc).makeFlash(PGTUtil.COLOR_REQUIRED_LEX_COLOR, true);
                testResults += ("\n" + test.getPronunciation());
            }
        } catch (Exception e) {
            // do nothing. The user will be informed of this elsewhere.
            // IOHandler.writeErrorLog(e);
        }
        if (!test.getDefinition().isEmpty()) {
            // errors having to do with type patterns returned in def field.
            ((PComboBox) cmbType).makeFlash(PGTUtil.COLOR_REQUIRED_LEX_COLOR, true);
            testResults += ("\n" + test.getDefinition());
        }
        if (!test.typeError.isEmpty()) {
            ((PComboBox) cmbType).makeFlash(PGTUtil.COLOR_REQUIRED_LEX_COLOR, true);
            testResults += ("\n" + test.typeError);
        }
        if (core.getPropertiesManager().isWordUniqueness()
                && core.getWordCollection().testWordValueExists(txtConWord.getText())) {
            ((PTextField) txtConWord).makeFlash(PGTUtil.COLOR_REQUIRED_LEX_COLOR, true);
            testResults += ("\nConWords set to enforced unique: this local exists elsewhere.");
        }
        if (core.getPropertiesManager().isLocalUniqueness()
                && core.getWordCollection().testLocalValueExists(txtLocalWord.getText())) {
            ((PTextField) txtLocalWord).makeFlash(PGTUtil.COLOR_REQUIRED_LEX_COLOR, true);
            testResults += ("\nLocal words set to enforced unique: this work exists elsewhere.");
        }

        if (!testResults.isEmpty()) {
            new DesktopInfoBox(this).warning("Illegal Values", "Word contains illegal values:\n\n"
                    + testResults);
            return;
        }

        try {
            int wordId = core.getWordCollection().addWord(word);
            blankWord();
            txtConWord.requestFocus();

            parent.refreshWordList(wordId);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("Word Error", "Unable to insert word: " + e.getMessage());
        }
    }

    /**
     * blanks out current conword fields
     */
    private void blankWord() {
        ((PTextField)txtConWord).setDefault();
        txtConWord.requestFocus();
        ((PTextPane)txtDefinition).setDefault();
        txtDefinition.requestFocus();
        txtLocalWord.setText("");
        txtLocalWord.requestFocus();
        txtProc.setText("");
        txtProc.requestFocus();
        setupClassPanel(-1);
        cmbType.setSelectedIndex(0);
    }

    /**
     * Sets up the class panel. Should be run whenever a new word is loaded
     *
     * @param setTypeId ID of class to set panel up for, -1 if no class selected
     */
    private void setupClassPanel(int setTypeId) {
        WordClass[] propList = core.getWordClassCollection().getClassesForType(setTypeId);
        pnlClasses.removeAll();
        pnlClasses.setPreferredSize(new Dimension(999999, 1));

        pnlClasses.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;

        // empty map of all class information before filling it again
        classComboMap.clear();

        // create dropdown for each class that applies to the current word
        for (WordClass curProp : propList) {
            if (curProp.isFreeText()) {
                PTextField textField = new PTextField(core, false, "-- " + curProp.getValue() + " --");
                textField.setEnabled(chkClasses.isSelected());
                textField.addActionListener((ActionEvent e) -> {
                    tryRecord();
                });
                
                textField.setToolTipText(curProp.getValue() + " value");
                textField.setPreferredSize(new Dimension(99999, textField.getPreferredSize().height));
                pnlClasses.add(textField, gbc);
                classComboMap.put(curProp.getId(), textField); // dropbox mapped to related class ID.
            } else {
                final PComboBox<Object> classBox = new PComboBox<>(((PropertiesManager)core.getPropertiesManager()).getFontMenu());
                DefaultComboBoxModel<Object> comboModel = new DefaultComboBoxModel<>();
                classBox.setModel(comboModel);
                comboModel.addElement("-- " + curProp.getValue() + " --");

                // populate class dropdown
                curProp.getValues().forEach((value) -> {
                    comboModel.addElement(value);
                });

                classBox.setEnabled(chkClasses.isSelected());
                classBox.addKeyListener(new KeyListener() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        // User only wants to enter word if no popups are visible
                        if (e.getKeyCode() == KeyEvent.VK_ENTER
                                && !cmbType.isPopupVisible()) {
                            // tests all class comboboxes
                            for (Component curBox : classComboMap.values()) {
                                if (curBox instanceof PComboBox && ((PComboBox) curBox).isPopupVisible()) {
                                    return;
                                }
                            }
                            tryRecord();
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        /*DO NOTHING*/
                    }

                    @Override
                    public void keyTyped(KeyEvent e) {
                        /*DO NOTHING*/
                    }
                });

                classBox.setToolTipText(curProp.getValue() + " value");
                classBox.setPreferredSize(new Dimension(99999, classBox.getPreferredSize().height));
                pnlClasses.add(classBox, gbc);
                classComboMap.put(curProp.getId(), classBox); // dropbox mapped to related class ID.
            }
        }
        if (propList.length == 0) {
            // must include at least one item (even a dummy) to resize for some reason
            PComboBox blank = new PComboBox(((PropertiesManager)core.getPropertiesManager()).getFontMenu());
            blank.setEnabled(false);
            pnlClasses.add(blank, gbc);
            pnlClasses.setPreferredSize(new Dimension(9999, 0));
        } else {
            pnlClasses.setMaximumSize(new Dimension(99999, 99999));
            pnlClasses.setPreferredSize(new Dimension(9999, propList.length * new PComboBox(((PropertiesManager)core.getPropertiesManager()).getFontMenu()).getPreferredSize().height));
        }

        pnlClasses.repaint();
        setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        chkLocal = new javax.swing.JCheckBox();
        chkType = new javax.swing.JCheckBox();
        chkProc = new javax.swing.JCheckBox();
        chkDefinition = new javax.swing.JCheckBox();
        chkClasses = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        txtConWord = new PTextField(core, false, "-- " + core.conLabel() + " word --");
        txtLocalWord = new PTextField(core, true, "-- " + core.localLabel() + " word --");
        cmbType = new PComboBox(((PropertiesManager)core.getPropertiesManager()).getFontMenu());
        txtProc = new PTextField(core, true, "-- Pronunciation --");
        pnlClasses = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDefinition = new PTextPane(core, true, "-- Definition --");
        btnDone = new PButton(nightMode, menuFontSize);
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Word Quickentry");
        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(335, 406));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.setMinimumSize(new java.awt.Dimension(265, 57));

        chkLocal.setSelected(true);
        chkLocal.setText("Local Word");
        chkLocal.setToolTipText("Enable/Disable Local Word Entry");
        chkLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLocalActionPerformed(evt);
            }
        });

        chkType.setSelected(true);
        chkType.setText("Part of Speech");
        chkType.setToolTipText("Enable/Disable Type Entry");
        chkType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTypeActionPerformed(evt);
            }
        });

        chkProc.setSelected(true);
        chkProc.setText("Pronunciation");
        chkProc.setToolTipText("Enable/Disable Pronunciation Entry");
        chkProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkProcActionPerformed(evt);
            }
        });

        chkDefinition.setSelected(true);
        chkDefinition.setText("Definition");
        chkDefinition.setToolTipText("Enable/Disable Definition Entry");
        chkDefinition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDefinitionActionPerformed(evt);
            }
        });

        chkClasses.setSelected(true);
        chkClasses.setText("Classes");
        chkClasses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkClassesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(chkType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkClasses))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(chkLocal)
                        .addGap(18, 18, 18)
                        .addComponent(chkProc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkDefinition)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkLocal)
                    .addComponent(chkDefinition)
                    .addComponent(chkProc))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkType)
                    .addComponent(chkClasses))
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        cmbType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlClassesLayout = new javax.swing.GroupLayout(pnlClasses);
        pnlClasses.setLayout(pnlClassesLayout);
        pnlClassesLayout.setHorizontalGroup(
            pnlClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlClassesLayout.setVerticalGroup(
            pnlClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(txtDefinition);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlClasses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cmbType, 0, 408, Short.MAX_VALUE)
            .addComponent(txtConWord)
            .addComponent(txtProc)
            .addComponent(txtLocalWord, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtConWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtLocalWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlClasses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtProc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnDone.setText("Close Window");
        btnDone.setToolTipText("Exit quickentry window (this does NOT save the word)");
        btnDone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDoneActionPerformed(evt);
            }
        });

        jLabel7.setText("Hit Enter/Return to save word and clear values");
        jLabel7.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                .addComponent(btnDone))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDone)
                    .addComponent(jLabel7)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDoneActionPerformed
        dispose();
    }//GEN-LAST:event_btnDoneActionPerformed

    private void chkLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLocalActionPerformed
        txtLocalWord.setEnabled(chkLocal.isSelected());
    }//GEN-LAST:event_chkLocalActionPerformed

    private void chkDefinitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDefinitionActionPerformed
        txtDefinition.setEnabled(chkDefinition.isSelected());
    }//GEN-LAST:event_chkDefinitionActionPerformed

    private void chkTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTypeActionPerformed
        cmbType.setEnabled(chkType.isSelected());
    }//GEN-LAST:event_chkTypeActionPerformed

    private void chkProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkProcActionPerformed
        txtProc.setEnabled(chkProc.isSelected());
    }//GEN-LAST:event_chkProcActionPerformed

    private void cmbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTypeActionPerformed
        SwingUtilities.invokeLater(() -> {
            if (cmbType.getSelectedItem() != null
                    && cmbType.getSelectedItem() instanceof TypeNode) {
                setupClassPanel(((TypeNode) cmbType.getSelectedItem()).getId());
            } else {
                setupClassPanel(0);
            }
        });
    }//GEN-LAST:event_cmbTypeActionPerformed

    private void chkClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkClassesActionPerformed
        classComboMap.values().forEach((curBox) -> {
            curBox.setEnabled(chkClasses.isSelected());
        });
    }//GEN-LAST:event_chkClassesActionPerformed

    /**
     * @param _core Dictionary Core
     * @param _parent parent dictionary interface
     * @return created window
     */
    public static ScrQuickWordEntry run(DictCore _core, ScrLexicon _parent) {
        ScrQuickWordEntry ret = new ScrQuickWordEntry(_core, _parent);
        ret.setModal(false);
        ret.setVisible(true);
        return ret;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDone;
    private javax.swing.JCheckBox chkClasses;
    private javax.swing.JCheckBox chkDefinition;
    private javax.swing.JCheckBox chkLocal;
    private javax.swing.JCheckBox chkProc;
    private javax.swing.JCheckBox chkType;
    private javax.swing.JComboBox<Object> cmbType;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel pnlClasses;
    private javax.swing.JTextField txtConWord;
    private javax.swing.JTextPane txtDefinition;
    private javax.swing.JTextField txtLocalWord;
    private javax.swing.JTextField txtProc;
    // End of variables declaration//GEN-END:variables
}
