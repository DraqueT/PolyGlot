/*
 * Copyright (c) 2018, DThompson
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
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author DThompson
 */
public class ScrDeclensionsGrids extends PDialog {
    private final Map<String, String> decIdToValue = new HashMap<>();
    private final Map<String, DeclensionPair> decIdToTemplatePair = new HashMap<>();
    private final ConWord word;
    private final DeclensionManager decMan;
    
    /**
     * Creates new form ScrDeclensionsGrids
     * @param _core
     * @param _word
     */
    public ScrDeclensionsGrids(DictCore _core, ConWord _word) {
        core = _core;
        word = _word;
        decMan = core.getDeclensionManager();
        initComponents();
        this.setModal(true);
        setupDimDropdowns();
        populateDecIdToValues();
        setupListeners();
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
        List<DeclensionPair> decTemplateList = decMan.getAllCombinedIds(word.getWordTypeId());
        
        for (DeclensionPair curPair : decTemplateList) {
            // skip forms that have been surpressed and sets pair values to null (indicating disabled table cells)
            if (decMan.isCombinedDeclSurpressed(curPair.combinedId, word.getWordTypeId())) {
                // TODO: TEST FOR NULL ENTRIES LATER. VERY, VERY IMPORTANT. Maybe wrap to avoid directly touching the map.
                decIdToTemplatePair.put(curPair.combinedId, null);
                decIdToValue.put(curPair.combinedId, null);
                continue;
            }
            
            // if the autodeclension override is not set, create a value, else gather existing form
            if (!word.isOverrideAutoDeclen()) {
                try {
                    decIdToValue.put(curPair.combinedId, 
                            decMan.declineWord(word, curPair.combinedId, word.getValue()));
                } catch (Exception e) {
                    InfoBox.error("Declension Error", e.getLocalizedMessage(), this);
                }
            } else {
                DeclensionNode userNode = decMan.getDeclensionByCombinedId(word.getId(), 
                        curPair.combinedId);
                decIdToValue.put(curPair.combinedId, userNode.getValue());
            }
            
            decIdToTemplatePair.put(curPair.combinedId, curPair);
        }
    }
    
    private void setupDimDropdowns() {
        List<DeclensionNode> declensionNodes = decMan.getDimensionalDeclensionListTemplate(word.getWordTypeId());
        DefaultComboBoxModel<DeclensionNode> modelX = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<DeclensionNode> modelY = new DefaultComboBoxModel<>();
        cmbDimX.setModel(modelX);
        cmbDimY.setModel(modelY);
        
        modelX.addElement(new DeclensionNode(-1));
        modelY.addElement(new DeclensionNode(-1));
        
        for (DeclensionNode node : declensionNodes) {
            modelX.addElement(node);
            modelY.addElement(node);
        }
    }
    
    private void buildWindowBody() {
        DeclensionNode dimX = (DeclensionNode)cmbDimX.getSelectedItem();
        DeclensionNode dimY = (DeclensionNode)cmbDimY.getSelectedItem();
        
        pnlTabDeclensions.removeAll();
        
        if (dimX == dimY || dimX.getId() < 0 || dimY.getId() < 0) {
            System.out.println("YALL CAN'T DO THAT. IT'S AGIN' GAWD.");
        } else {
            // TODO: remember that cells should have a tooltip value that is equal to their pronunciation (see l:276 of ScrDeclensions.java)

            // 2) get all partial dim ID patterns (sans X & Y dims)/feed to grid pannel class
            getPanelPartialDimIds().forEach((partialDim)->{
                PDeclensionGridPanel gridPanel 
                        = new PDeclensionGridPanel(partialDim, dimX, dimY, word.getWordTypeId(), core, word);
                pnlTabDeclensions.addTab(gridPanel.getTabName(), gridPanel);
            });
             
            // 2.5) only do 2 if there is more than 1 dimension... otherwise display everything in 3

            // 3) make a panel with singleton forms (if they exist)
        }
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
        
        for (String dimId : decIdToValue.keySet()) {
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
        if (chkAutogenOverride.isSelected()) {
            // TODO: save all values as populated
        } else {
            // TODO: make sure all saved values are wiped
        }
        
        word.setOverrideAutoDeclen(chkAutogenOverride.isSelected());
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        chkAutogenOverride.setText("Autogen Override");

        btnOk.setText("OK");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbDimX, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbDimY, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(178, Short.MAX_VALUE))
            .addComponent(pnlTabDeclensions)
            .addGroup(layout.createSequentialGroup()
                .addComponent(chkAutogenOverride)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOk))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbDimX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbDimY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTabDeclensions, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkAutogenOverride, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnOk)
                        .addComponent(btnCancel))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        saveValues();
        dispose();
    }//GEN-LAST:event_btnOkActionPerformed

    @Override
    public void updateAllValues(DictCore _core) {
        // DOES NOTHING IN THIS WINDOW
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JCheckBox chkAutogenOverride;
    private javax.swing.JComboBox<DeclensionNode> cmbDimX;
    private javax.swing.JComboBox<DeclensionNode> cmbDimY;
    private javax.swing.JTabbedPane pnlTabDeclensions;
    // End of variables declaration//GEN-END:variables
}
