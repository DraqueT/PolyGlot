/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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

import PolyGlot.Nodes.ConWord;
import PolyGlot.DictCore;
import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PComboBox;
import PolyGlot.CustomControls.PDialog;
import PolyGlot.CustomControls.PTextArea;
import PolyGlot.CustomControls.PTextField;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.Nodes.WordPropValueNode;
import PolyGlot.Nodes.WordProperty;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 *
 * @author draque
 */
public final class ScrQuickWordEntry extends PDialog {

    private final Map<Integer, JComboBox> classComboMap = new HashMap<>();
    private final ScrLexicon parent;

    /**
     * Creates new form scrQuickWordEntry
     *
     * @param _core Dictionary core
     * @param _parent parent dictionary interface
     */
    public ScrQuickWordEntry(DictCore _core, ScrLexicon _parent) {
        core = _core;
        parent = _parent;

        setupKeyStrokes();
        initComponents();
        setupListeners();

        // conword is always required and is initially selected
        txtConWord.setBackground(core.getRequiredColor());
        txtConWord.requestFocus();

        if (core.getPropertiesManager().isLocalMandatory()) {
            txtLocalWord.setBackground(core.getRequiredColor());
            chkLocal.setEnabled(false);
        }
        if (core.getPropertiesManager().isTypesMandatory()) {
            cmbType.setForeground(core.getRequiredColor());
            chkType.setEnabled(false);
        }

        populateTypes();
    }

    // Overridden to meet coding standards...
    @Override
    protected final void setupKeyStrokes() {
        super.setupKeyStrokes();
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
                        && !cmbType.isPopupVisible()) {
                    // tests all class comboboxes
                    for (JComboBox curBox : classComboMap.values()) {
                        if (curBox.isPopupVisible()) {
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

        txtConWord.setFont(core.getPropertiesManager().getFontCon());
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
        
        setupDefText(txtDefinition, "-- Definition --"); // TODO: update this when PTextArea implemented
        txtLocalWord.addKeyListener(enterListener);
        txtProc.addKeyListener(enterListener);
    }

    /**
     * Sets up default text for text fields
     * @param target
     * @param label 
     */
    private void setupDefText(JTextComponent _target, String label) {
        final JTextComponent target = _target;
        final String defLabel = label;
        
        target.setText(label);
        target.setForeground(Color.lightGray);        
        target.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (target.getText().equals(defLabel)) {
                    target.setText("");
                    target.setForeground(Color.black);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (target.getText().equals("")) {
                    target.setText(defLabel);
                    target.setForeground(Color.lightGray);
                }
            }
        });
    }
    
    /**
     * Sets pronunciation value of word
     */
    private void setProc() {
        String proc = core.getPronunciationMgr()
                .getPronunciation(txtConWord.getText());
        
        if (!proc.equals("")) {
            txtProc.setText(proc);
        }
    }

    @Override
    public boolean thisOrChildrenFocused() {
        return this.isFocusOwner();
    }

    @Override
    public void updateAllValues(DictCore _core) {
        core = _core;
        // ensure this is on the UI component stack to avoid read/writelocks...
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                populateTypes();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    private void populateTypes() {
        Iterator<TypeNode> typeIt = core.getTypes().getNodeIterator();
        cmbType.removeAllItems();
        final String defLabel =  "-- Part of Speech --";
        cmbType.addItem(defLabel);
        
        while (typeIt.hasNext()) {
            TypeNode curType = typeIt.next();
            cmbType.addItem(curType);
        }
    }

    /**
     * records a word if appropriate, flashes required fields otherwise
     */
    private void tryRecord() {
        ConWord word = new ConWord();

        word.setValue(txtConWord.getText());
        word.setLocalWord(txtLocalWord.getText());
        word.setPronunciation(txtProc.getText());
        word.setDefinition(txtDefinition.getText());
        
        if (cmbType.getSelectedItem() instanceof TypeNode) {
            word.setWordTypeId(((TypeNode) cmbType.getSelectedItem()).getId());
        }

        // set class values
        for (Entry<Integer, JComboBox> curEntry : classComboMap.entrySet()) {
            if (curEntry.getValue().getSelectedItem() instanceof WordPropValueNode) {
                WordPropValueNode curValue = (WordPropValueNode) curEntry.getValue().getSelectedItem();
                word.setClassValue(curEntry.getKey(), curValue.getId());
            }
        }

        ConWord test = core.getWordCollection().testWordLegality(word);
        String testResults = "";

        if (!test.getValue().isEmpty()) {
            ((PTextField) txtConWord).makeFlash(core.getRequiredColor(), true);
            testResults += test.getValue();
        }
        if (!test.getLocalWord().isEmpty()) {
            ((PTextField) txtLocalWord).makeFlash(core.getRequiredColor(), true);
            testResults += ("\n" + test.getLocalWord());
        }
        if (!test.getPronunciation().isEmpty()) {
            ((PTextField) txtProc).makeFlash(core.getRequiredColor(), true);
            testResults += ("\n" + test.getPronunciation());
        }
        if (!test.getDefinition().isEmpty()) {
            // errors having to do with type patterns returned in def field.
            ((PComboBox) cmbType).makeFlash(core.getRequiredColor(), true);
            testResults += ("\n" + test.getDefinition());
        }
        /*if (!test.getGender().isEmpty()) {
            ((PComboBox)cmbGender).makeFlash(core.getRequiredColor(), true);
            testResults += ("\n" + test.getGender());
        }*/ // TODO: replace with relevant code for class values eventually
        if (!test.typeError.isEmpty()) {
            ((PComboBox) cmbType).makeFlash(core.getRequiredColor(), true);
            testResults += ("\n" + test.typeError);
        }
        if (core.getPropertiesManager().isWordUniqueness()
                && core.getWordCollection().testWordValueExists(txtConWord.getText())) {
            ((PTextField) txtConWord).makeFlash(core.getRequiredColor(), true);
            testResults += ("\nConWords set to enforced unique: this local exists elsewhere.");
        }
        if (core.getPropertiesManager().isLocalUniqueness()
                && core.getWordCollection().testLocalValueExists(txtLocalWord.getText())) {
            ((PTextField) txtLocalWord).makeFlash(core.getRequiredColor(), true);
            testResults += ("\nLocal words set to enforced unique: this work exists elsewhere.");
        }

        if (!testResults.isEmpty()) {
            InfoBox.warning("Illegal Values", "Word contains illegal values:\n\n"
                    + testResults, this);
            return;
        }

        try {
            int wordId = core.getWordCollection().addWord(word);
            blankWord();
            txtConWord.requestFocus();

            parent.refreshWordList(wordId);
        } catch (Exception e) {
            InfoBox.error("Word Error", "Unable to insert word: " + e.getMessage(), this);
        }
    }

    /**
     * blanks out current conword fields
     */
    private void blankWord() {
        txtConWord.setText("");
        txtConWord.requestFocus();
        txtDefinition.setText("");
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
        List<WordProperty> propList = core.getWordPropertiesCollection()
                .getClassProps(setTypeId);
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

        // create dropdown for each class that applies to the curren word
        for (WordProperty curProp : propList) {
            final JComboBox classBox = new JComboBox();
            DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
            classBox.setModel(comboModel);
            comboModel.addElement("-- " + curProp.getValue() + " --");

            // populate class dropdown
            for (WordPropValueNode value : curProp.getValues()) {
                comboModel.addElement(value);
            }

            classBox.setEnabled(chkClasses.isSelected());
            classBox.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    // User only wants to enter word if no popups are visible
                    if (e.getKeyCode() == KeyEvent.VK_ENTER
                            && !cmbType.isPopupVisible()) {
                        // tests all class comboboxes
                        for (JComboBox curBox : classComboMap.values()) {
                            if (curBox.isPopupVisible()) {
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
        if (propList.isEmpty()) {
            // must include at least one item (even a dummy) to resize for some reason
            JComboBox blank = new JComboBox();
            blank.setEnabled(false);
            pnlClasses.add(blank, gbc);
            pnlClasses.setPreferredSize(new Dimension(9999, 0));
        } else {
            pnlClasses.setMaximumSize(new Dimension(99999, 99999));
            pnlClasses.setPreferredSize(new Dimension(9999, propList.size() * new JComboBox().getPreferredSize().height));
        }

        pnlClasses.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
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
        cmbType = new PComboBox();
        txtProc = new PTextField(core, true, "-- Pronunciation --");
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDefinition = new PTextArea();
        pnlClasses = new javax.swing.JPanel();
        btnDone = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Word Quickentry");
        setMinimumSize(new java.awt.Dimension(335, 406));

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

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        cmbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTypeActionPerformed(evt);
            }
        });

        txtDefinition.setColumns(20);
        txtDefinition.setLineWrap(true);
        txtDefinition.setRows(5);
        txtDefinition.setWrapStyleWord(true);
        jScrollPane1.setViewportView(txtDefinition);

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlClasses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cmbType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtConWord)
            .addComponent(txtProc)
            .addComponent(jScrollPane1)
            .addComponent(txtLocalWord, javax.swing.GroupLayout.Alignment.TRAILING)
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnDone.setText("Done");
        btnDone.setToolTipText("Exit quickentry window");
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (cmbType.getSelectedItem() != null
                        && cmbType.getSelectedItem() instanceof TypeNode) {
                    setupClassPanel(((TypeNode) cmbType.getSelectedItem()).getId());
                } else {
                    setupClassPanel(0);
                }
            }
        });
    }//GEN-LAST:event_cmbTypeActionPerformed

    private void chkClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkClassesActionPerformed
        for (JComboBox curBox : classComboMap.values()) {
            curBox.setEnabled(chkClasses.isSelected());
        }
    }//GEN-LAST:event_chkClassesActionPerformed

    /**
     * @param _core Dictionary Core
     * @param _parent parent dictionary interface
     * @return created window
     */
    public static ScrQuickWordEntry run(DictCore _core, ScrLexicon _parent) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            InfoBox.error("Window Error", "Unable to open quick word entry screen: " + ex.getLocalizedMessage(), _parent);
        }
        //</editor-fold>

        //</editor-fold>
        ScrQuickWordEntry ret = new ScrQuickWordEntry(_core, _parent);
        ret.setModal(true);
        return ret;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDone;
    private javax.swing.JCheckBox chkClasses;
    private javax.swing.JCheckBox chkDefinition;
    private javax.swing.JCheckBox chkLocal;
    private javax.swing.JCheckBox chkProc;
    private javax.swing.JCheckBox chkType;
    private javax.swing.JComboBox cmbType;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnlClasses;
    private javax.swing.JTextField txtConWord;
    private javax.swing.JTextArea txtDefinition;
    private javax.swing.JTextField txtLocalWord;
    private javax.swing.JTextField txtProc;
    // End of variables declaration//GEN-END:variables
}
