/*
 * Copyright (c) 2021, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
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
import java.awt.Font;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextPane;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopOptionsManager;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.PhraseManager;
import org.darisadesigns.polyglotlina.Nodes.PhraseNode;

/**
 *
 * @author draque
 */
public class ScrPhrasebook extends PFrame {
    private final PhraseManager phraseManager;
    private boolean isCurPopulating = false;
    
    /**
     * Creates new form ScrPhrasebook
     * @param _core
     */
    public ScrPhrasebook(DictCore _core) {
        super(_core);
        phraseManager = core.getPhraseManager();
        
        initComponents();
        setupComponents();
        populatePhrases();
        populatePhraseAttributes();
    }
    
    private void setupComponents() {
        ((PTextPane)txtConPhrase).setDisableMedia(true);
        ((PTextPane)txtLocalPhrase).setDisableMedia(true);
        ((PTextPane)txtPronunciation).setDisableMedia(true);
        ((PTextPane)txtNotes).setDisableMedia(true);
        
        txtGloss.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateGloss();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateGloss();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateGloss();
            }
            
            private void updateGloss() {
                PhraseNode node = lstPhrases.getSelectedValue();
                if (node != null) {
                    node.setGloss(txtGloss.getText());
                    lstPhrases.repaint();
                }
            }
        });
        
        txtConPhrase.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePronunciation();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePronunciation();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePronunciation();
            }
            
            private void updatePronunciation() {
                PhraseNode node = lstPhrases.getSelectedValue();
                
                if (node != null && !chkOverrideProc.isSelected()) {
                    node.setConPhrase(((PTextPane)txtConPhrase).getNakedText());
                    var localPopulating = isCurPopulating;
                    isCurPopulating = true;
                    txtPronunciation.setText(node.getPronunciation());
                    isCurPopulating = localPopulating;
                }
            }
        });
        
        float fontSize = (float)((DesktopOptionsManager)PolyGlot.getPolyGlot().getOptionsManager()).getMenuFontSize();
        Font charis = PGTUtil.CHARIS_UNICODE.deriveFont(fontSize);
        btnUp.setFont(charis);
        btnDown.setFont(charis);
    }
    
    private void populatePhrases() {
        boolean localPopulating = isCurPopulating;
        isCurPopulating = true;
        
        DefaultListModel<PhraseNode> listModel = new DefaultListModel<>();
        
        try {
            for (var phrase : phraseManager.getAllValues()) {
                listModel.addElement(phrase);
            }

            lstPhrases.setModel(listModel);
            lstPhrases.setSelectedIndex(0);
            lstPhrases.ensureIndexIsVisible(0);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("Type Population Error", "Unable to populate types: "
                    + e.getLocalizedMessage());
        } finally {
            isCurPopulating = localPopulating;
        }
    }
    
    private void populatePhraseAttributes() {
        boolean localPopulating = isCurPopulating;
        isCurPopulating = true;
        
        PhraseNode phrase = lstPhrases.getSelectedValue();
        
        try {
            if (phrase != null) {
                txtGloss.setText(phrase.getGloss());
                ((PTextPane)txtConPhrase).setText(phrase.getConPhrase());
                ((PTextPane)txtLocalPhrase).setText(phrase.getLocalPhrase());
                ((PTextPane)txtPronunciation).setText(phrase.getPronunciation());
                ((PTextPane)txtNotes).setText(phrase.getNotes());
                chkOverrideProc.setSelected(phrase.isProcOverride());
                
                setEnableAttributeEdit(true);
            } else {
                setEnableAttributeEdit(false);
            }
        } finally{
            isCurPopulating = localPopulating;
        }
    }
    
    private void savePhraseAttributes(PhraseNode phrase) {
        if (!isCurPopulating && phrase != null) {
            phrase.setGloss(((PTextField)txtGloss).isDefaultText() ? "" : txtGloss.getText());
            phrase.setConPhrase(((PTextPane)txtConPhrase).isDefaultText() ? "" : ((PTextPane)txtConPhrase).getNakedText());
            phrase.setLocalPhrase(((PTextPane)txtLocalPhrase).isDefaultText() ? "" : ((PTextPane)txtLocalPhrase).getNakedText());
            phrase.setPronunciation(((PTextPane)txtPronunciation).isDefaultText() ? "" : ((PTextPane)txtPronunciation).getNakedText());
            phrase.setNotes(((PTextPane)txtNotes).isDefaultText() ? "" : ((PTextPane)txtNotes).getNakedText());
            phrase.setProcOverride(chkOverrideProc.isSelected());
        }
    }
    
    private void setEnableAttributeEdit(boolean enable) {
        txtGloss.setEnabled(enable);
        txtConPhrase.setEnabled(enable);
        txtLocalPhrase.setEnabled(enable);
        txtPronunciation.setEnabled(enable && chkOverrideProc.isSelected());
        txtNotes.setEnabled(enable);
        chkOverrideProc.setEnabled(enable);
    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void saveAllValues() {
        savePhraseAttributes(lstPhrases.getSelectedValue());
    }

    @Override
    public void updateAllValues(DictCore _core) {
        PhraseNode curPhrase = lstPhrases.getSelectedValue();
        
        populatePhrases();
        lstPhrases.setSelectedValue(curPhrase, true);
        populatePhraseAttributes();
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Component getWindow() {
        return jSplitPane1;
    }
    
    @Override
    public String getTitle() {
        return "Phrasebook";
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
        txtGloss = new PTextField(core, true, "-- Gloss --");
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLocalPhrase = new PTextPane(core, true, "-- Local Phrase --");
        jScrollPane3 = new javax.swing.JScrollPane();
        txtConPhrase = new PTextPane(core, false, "-- Con Phrase --");
        jScrollPane4 = new javax.swing.JScrollPane();
        txtPronunciation = new PTextPane(core, true, "-- Pronunciation Guide --");
        jScrollPane5 = new javax.swing.JScrollPane();
        txtNotes = new PTextPane(core, true, "-- Phrase Notes --");
        chkOverrideProc = new PCheckBox(nightMode, menuFontSize);
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstPhrases = new javax.swing.JList<>();
        btnUp = new PButton(false, (((DesktopOptionsManager)PolyGlot.getPolyGlot().getOptionsManager()).getMenuFontSize()));
        btnDown = new PButton(false, (((DesktopOptionsManager)PolyGlot.getPolyGlot().getOptionsManager()).getMenuFontSize()));
        btnAdd = new PAddRemoveButton("+");
        btnDelete = new PAddRemoveButton("-");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSplitPane1.setDividerLocation(170);

        jScrollPane1.setViewportView(txtLocalPhrase);

        jScrollPane3.setViewportView(txtConPhrase);

        jScrollPane4.setViewportView(txtPronunciation);

        jScrollPane5.setViewportView(txtNotes);

        chkOverrideProc.setText("Override Pronunciation");
        chkOverrideProc.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkOverrideProcItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtGloss)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(chkOverrideProc)
                                .addGap(0, 190, Short.MAX_VALUE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane4)))))
            .addComponent(jScrollPane5)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(txtGloss, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkOverrideProc)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel1);

        lstPhrases.setModel(new javax.swing.DefaultListModel<PhraseNode>() {

        });
        lstPhrases.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstPhrasesValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(lstPhrases);

        btnUp.setText("↑");
        btnUp.setToolTipText("Move selected entry up one position.");
        btnUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpActionPerformed(evt);
            }
        });

        btnDown.setText("↓");
        btnDown.setToolTipText("Move selected entry down one position.");
        btnDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownActionPerformed(evt);
            }
        });

        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnUp, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDown, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnUp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDown))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDelete, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnAdd, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
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

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lstPhrasesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstPhrasesValueChanged
        if (!isCurPopulating
                && evt.getFirstIndex() != evt.getLastIndex()
                && evt.getValueIsAdjusting()) {
            
            int selected = lstPhrases.getSelectedIndex();
            int index = selected == evt.getFirstIndex()
                    ? evt.getLastIndex() : evt.getFirstIndex();

            if (index != -1
                    && index < lstPhrases.getModel().getSize()) {
                PhraseNode phrase = lstPhrases.getModel().getElementAt(index);
                savePhraseAttributes(phrase);
            }
            
            populatePhraseAttributes();
        }
    }//GEN-LAST:event_lstPhrasesValueChanged

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        try {
            saveAllValues();

            phraseManager.clear();
            int newPhraseId = phraseManager.insert();
            
            populatePhrases();
            lstPhrases.setSelectedValue(phraseManager.getNodeById(newPhraseId), true);
            populatePhraseAttributes();
        } catch(Exception e) {
            new DesktopInfoBox(this).error("Phrase Creation Error", "Unable to create phrase due to: " + e.getMessage());
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        PhraseNode phrase = lstPhrases.getSelectedValue();
        int listIndex = lstPhrases.getSelectedIndex();
        
        if (phrase != null) {
            try {
                phraseManager.deleteNodeById(phrase.getId());
                populatePhrases();
                lstPhrases.setSelectedIndex(listIndex > 1 ? listIndex - 1 : 0);
                populatePhraseAttributes();
            } catch (Exception e) {
                new DesktopInfoBox(this).error("Phrase Deletion Error", "Unable to delete phrase due to: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void chkOverrideProcItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkOverrideProcItemStateChanged
        PhraseNode node = lstPhrases.getSelectedValue();
        
        if (node != null) {
            node.setProcOverride(chkOverrideProc.isSelected());
            node.setConPhrase(((PTextPane)txtConPhrase).getNakedText());
            var localPopulating = isCurPopulating;
            isCurPopulating = true;
            txtPronunciation.setText(node.getPronunciation());
            txtPronunciation.setEnabled(chkOverrideProc.isSelected());
            isCurPopulating = localPopulating;
        }
    }//GEN-LAST:event_chkOverrideProcItemStateChanged

    private void btnUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpActionPerformed
        PhraseNode node = lstPhrases.getSelectedValue();
        int selectionIndex = lstPhrases.getSelectedIndex();
        
        if (node != null && selectionIndex > 0) {
            phraseManager.moveNodeUp(node);
            populatePhrases();
            lstPhrases.setSelectedIndex(selectionIndex - 1);
        }
    }//GEN-LAST:event_btnUpActionPerformed

    private void btnDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownActionPerformed
        PhraseNode node = lstPhrases.getSelectedValue();
        int selectionIndex = lstPhrases.getSelectedIndex();
        
        if (node != null && selectionIndex < lstPhrases.getModel().getSize()) {
            phraseManager.moveNodeDown(node);
            populatePhrases();
            populatePhraseAttributes();
            lstPhrases.setSelectedIndex(selectionIndex + 1);
        }
    }//GEN-LAST:event_btnDownActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDown;
    private javax.swing.JButton btnUp;
    private javax.swing.JCheckBox chkOverrideProc;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JList<PhraseNode> lstPhrases;
    private javax.swing.JTextPane txtConPhrase;
    private javax.swing.JTextField txtGloss;
    private javax.swing.JTextPane txtLocalPhrase;
    private javax.swing.JTextPane txtNotes;
    private javax.swing.JTextPane txtPronunciation;
    // End of variables declaration//GEN-END:variables
}
