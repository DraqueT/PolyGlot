/*
 * Copyright (c) 2018-2019, Draque Thompson, draquemail@gmail.com
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

import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PComboBox;
import PolyGlot.CustomControls.PDeclensionGridPanel;
import PolyGlot.CustomControls.PDeclensionListPanel;
import PolyGlot.CustomControls.PDialog;
import PolyGlot.DictCore;
import PolyGlot.ManagersCollections.DeclensionManager;
import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.Nodes.DeclensionPair;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.DefaultComboBoxModel;
import PolyGlot.CustomControls.PDeclensionPanelInterface;
import java.awt.Window;
import javax.swing.JOptionPane;

/**
 *
 * @author DThompson
 */
public final class ScrDeclensionsGrids extends PDialog {
    private final List<String> combDecIds = new ArrayList<>();
    private final ConWord word;
    private final DeclensionManager decMan;
    private final Map<String, String> labelMap = new HashMap<>();
    private boolean closeWithoutSave = false;
    Window depVals = null;
    
    /**
     * Creates new form ScrDeclensionsGrids
     * @param _core
     * @param _word
     */
    public ScrDeclensionsGrids(DictCore _core, ConWord _word) {
        core = _core;
        word = _word;
        decMan = core.getDeclensionManager();
        
        if (canOpen()) {
            initComponents();
            this.setModal(true);
            setupDimDropdowns();
            populateDecIdToValues();
            buildWindowBody();
            setupListeners();
            chkAutogenOverride.setSelected(word.isOverrideAutoDeclen());
        } else {
            closeWithoutSave = true;
            dispose();
        }
    }
    
    private boolean canOpen() {
        boolean ret = true;
        int typeId = word.getWordTypeId();
        
        if (typeId == -1) {
            InfoBox.info("Missing Part of Speech", 
                    "Word must have a part of Speech set, and declensions defined before using this feature.", 
                    core.getRootWindow());
            ret = false;
        } else if ((decMan.getDimensionalDeclensionListTemplate(typeId) == null
                    || decMan.getDimensionalDeclensionListTemplate(typeId).isEmpty())
                && decMan.getDimensionalDeclensionListWord(word.getId()).isEmpty()) {
            InfoBox.info("Declensions", "No declensions for part of speech: " + word.getWordTypeDisplay()
                    + " set. Declensions can be created per part of speech under the Part of Speech menu by clicking the "
                            + "Declensions button.", this);
            ret = false;
        }
        
        return ret;
    }
    
    private void setupListeners() {
        cmbDimX.addActionListener((ActionEvent ae) -> {
            buildWindowBody();
        });
        cmbDimY.addActionListener((ActionEvent ae) -> {
            buildWindowBody();
        });
    }
    
    private void populateDecIdToValues() {
        List<DeclensionPair> decTemplateList = decMan.getDimensionalCombinedIds(word.getWordTypeId());
        
        for (DeclensionPair curPair : decTemplateList) {
            // skip forms that have been surpressed
            if (decMan.isCombinedDeclSurpressed(curPair.combinedId, word.getWordTypeId())) {
                continue;
            }
            
            combDecIds.add(curPair.combinedId);
            labelMap.put(curPair.combinedId, curPair.label);
        }
    }
    
    private void setupDimDropdowns() {
        List<DeclensionNode> declensionNodes = decMan.getDimensionalDeclensionListTemplate(word.getWordTypeId());
        DefaultComboBoxModel<DeclensionNode> modelX = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<DeclensionNode> modelY = new DefaultComboBoxModel<>();
        cmbDimX.setModel(modelX);
        cmbDimY.setModel(modelY);
        
        for (DeclensionNode node : declensionNodes) {
            modelX.addElement(node);
            modelY.addElement(node);
        }
        
        if (declensionNodes.size() > 1) {
            cmbDimX.setSelectedIndex(0);
            cmbDimY.setSelectedIndex(1);
        }
    }
    
    private void buildWindowBody() {
        DeclensionNode dimX = (DeclensionNode)cmbDimX.getSelectedItem();
        DeclensionNode dimY = (DeclensionNode)cmbDimY.getSelectedItem();
        
        pnlTabDeclensions.removeAll();
        
        // only renders as dimensional if there are at least two dimensional declensions. Renders as list otherwise.
        if (shouldRenderDimensional()) {
            if (dimX == dimY) {
                    InfoBox.warning("Dimension Selection", "Please select differing Row and Column values.", this);
            }
            else {
                // TODO: Disabled cells should be light grey to indicate this
                // get all partial dim ID patterns (sans X & Y dims)/feed to grid pannel class
                getPanelPartialDimIds().forEach((partialDim)->{
                    PDeclensionGridPanel gridPanel 
                            = new PDeclensionGridPanel(partialDim, dimX, dimY, word.getWordTypeId(), core, word);
                    pnlTabDeclensions.addTab(gridPanel.getTabName(), gridPanel);
                });

                // add singleton values when appropriate
                List<DeclensionPair> singletons = decMan.getSingletonCombinedIds(word.getWordTypeId());
                if (!singletons.isEmpty()) {
                    PDeclensionListPanel listPanel = new PDeclensionListPanel(singletons, core, word, false);
                    pnlTabDeclensions.addTab(listPanel.getTabName(), listPanel);
                }
            }
        } else {
            cmbDimX.setVisible(false);
            cmbDimY.setVisible(false);
            jLabel1.setVisible(false);
            jLabel2.setVisible(false);
            List<DeclensionPair> completeList = decMan.getAllCombinedIds(word.getWordTypeId());
            PDeclensionListPanel listPanel 
                    = new PDeclensionListPanel(completeList, core, word, true);
            
            pnlTabDeclensions.addTab(listPanel.getTabName(), listPanel);
        }
        
        btnDeprecated.setVisible(decMan.wordHasDeprecatedForms(word));
    }
    
    private boolean shouldRenderDimensional() {
        return decMan.getDimensionalDeclensionListTemplate(word.getWordTypeId()).size() > 1;
    }
    
    private String replaceDimensionByIndex(String dimensions, int index, String replacement) {
        String[] dimArray = dimensions.split(","); 
        dimArray = Arrays.copyOfRange(dimArray, 1, dimArray.length); // first value always empty
        String ret = ",";
        
        // rebuild dimensionID, replacing the index values as appropriate
        for (int i = 0; i < dimArray.length; i++) {
            ret += (i == index ? replacement : dimArray[i]) + ",";
        }
        
        return ret;
    }
    
    /**
     * Gets partial dim ids to be fed to grid panels
     * Positions containing the values of the dimensions selected by the user
     * replaced with "X" and "Y" respectively
     * Filters out null values (values for disabled word forms)
     */
    private List<String> getPanelPartialDimIds() {
        List<String> partialIds = new ArrayList<>();
        int xNode = decMan.getDimensionTemplateIndex(word.getWordTypeId(), (DeclensionNode)cmbDimX.getSelectedItem());
        int yNode = decMan.getDimensionTemplateIndex(word.getWordTypeId(), (DeclensionNode)cmbDimY.getSelectedItem());
        
        for (String dimId : combDecIds) {
            if (dimId == null) {
                continue;
            }
            
            String partialDecId = replaceDimensionByIndex(dimId, xNode, "X");
            partialDecId = replaceDimensionByIndex(partialDecId, yNode, "Y");
            
            if (!partialIds.contains(partialDecId)) {
                partialIds.add(partialDecId);
            }
        }
        
        return partialIds;
    }
    
    private void saveValues() {
        core.clearAllDeclensionsWord(word.getId());
        
        if (chkAutogenOverride.isSelected()) {
            int count = pnlTabDeclensions.getTabCount();
            for (int i = 0; i < count; i++) {
                PDeclensionPanelInterface comp = (PDeclensionPanelInterface)pnlTabDeclensions.getComponentAt(i);
                
                for (Entry<String, String> entry : comp.getAllDecValues().entrySet()) {
                    DeclensionNode saveNode = new DeclensionNode(-1);

                    saveNode.setValue(entry.getValue().trim());
                    saveNode.setCombinedDimId(entry.getKey());
                    saveNode.setNotes(labelMap.get(entry.getKey()));

                    // declensions per word not saved via int id any longer
                    decMan.addDeclensionToWord(word.getId(), -1, saveNode);
                }
            }
        }
        
        word.setOverrideAutoDeclen(chkAutogenOverride.isSelected());
    }
    
    @Override
    public void dispose() {
        if (!isDisposed()) {
            if (!closeWithoutSave && chkAutogenOverride.isSelected()) {
                Integer userChoice = InfoBox.yesNoCancel("Save Confirmation", "Save changes?", this);

                // yes = save, no = don't save, any other choice = cancel & do not exit
                if (userChoice == JOptionPane.YES_OPTION) {
                    saveValues();
                    super.dispose();
                } else if (userChoice == JOptionPane.NO_OPTION) {
                    super.dispose();
                }
            } else {
                if (depVals != null && !depVals.isVisible()) {
                    depVals.dispose();
                }
                super.dispose();
            }
        } else {
            super.dispose();
        }
    }
    
    public void setCloseWithoutSave(boolean _closeWithoutSave) {
        closeWithoutSave = _closeWithoutSave;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmbDimX = new PComboBox(core);
        cmbDimY = new PComboBox(core);
        pnlTabDeclensions = new javax.swing.JTabbedPane();
        chkAutogenOverride = new javax.swing.JCheckBox();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnDeprecated = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Conjugations/Declensions");

        cmbDimX.setToolTipText("Rows in wordform grids will be based on this donjugation/declension dimension");

        cmbDimY.setToolTipText("Columns in wordform grids will be based on this donjugation/declension dimension");

        chkAutogenOverride.setText("Autogen Override");
        chkAutogenOverride.setToolTipText("Check to override autogeneration of declension forms (if autogeneration patterns exist)");

        btnOk.setText("OK");
        btnOk.setToolTipText("Record changes and exit");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.setToolTipText("Discard changes and exit");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jLabel1.setText("Rows:");

        jLabel2.setText("Columns:");

        btnDeprecated.setText("Deprecated");
        btnDeprecated.setToolTipText("Deprecated Conjugations/Declensions");
        btnDeprecated.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeprecatedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlTabDeclensions)
            .addGroup(layout.createSequentialGroup()
                .addComponent(chkAutogenOverride)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
                .addComponent(btnDeprecated)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOk))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbDimY, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbDimX, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbDimX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(cmbDimY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTabDeclensions, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkAutogenOverride, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnOk)
                        .addComponent(btnCancel)
                        .addComponent(btnDeprecated))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        closeWithoutSave = true;
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        saveValues();
        super.dispose(); // skips save dialog
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnDeprecatedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeprecatedActionPerformed
        depVals = ScrDeprecatedDeclensions.run(core, word);
    }//GEN-LAST:event_btnDeprecatedActionPerformed
    
    @Override
    public void updateAllValues(DictCore _core) {
        // DOES NOTHING IN THIS WINDOW
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDeprecated;
    private javax.swing.JButton btnOk;
    private javax.swing.JCheckBox chkAutogenOverride;
    private javax.swing.JComboBox<DeclensionNode> cmbDimX;
    private javax.swing.JComboBox<DeclensionNode> cmbDimY;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTabbedPane pnlTabDeclensions;
    // End of variables declaration//GEN-END:variables
}
