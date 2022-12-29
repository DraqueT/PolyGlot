/*
 * Copyright (c) 2018-2022, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PComboBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDeclensionGridPanel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDeclensionListPanel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.DefaultComboBoxModel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDeclensionPanelInterface;
import java.awt.Window;
import java.awt.event.ItemEvent;
import javax.swing.JOptionPane;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;

/**
 *
 * @author DThompson
 */
public final class ScrDeclensionsGrids extends PDialog {

    private final List<String> combDecIds = new ArrayList<>();
    private final ConWord word;
    private final ConjugationManager decMan;
    private final Map<String, String> labelMap = new HashMap<>();
    private boolean closeWithoutSave = false;
    private boolean autoPopulated = false;
    private Window depVals = null;

    /**
     * Creates new form ScrDeclensionsGrids
     *
     * @param _core
     * @param _word
     */
    public ScrDeclensionsGrids(DictCore _core, ConWord _word) {
        super(_core);

        word = _word;
        decMan = core.getConjugationManager();

        initComponents();
        this.setModal(true);
        setupDimDropdowns();
        populateDecIdToValues();
        buildWindowBody();
        setupListeners();
        chkAutogenOverride.setSelected(word.isOverrideAutoConjugate() || !autoPopulated);
        btnOk.requestFocus();
    }

    /**
     * Creates new declension grid window. Returns null if cannot open.
     *
     * @param core
     * @param word
     * @return
     */
    public static ScrDeclensionsGrids run(DictCore core, ConWord word) {
        ScrDeclensionsGrids ret = null;

        if (canOpen(core, word)) {
            ret = new ScrDeclensionsGrids(core, word);
        }

        return ret;
    }

    public static boolean canOpen(DictCore core, ConWord word) {
        boolean ret = true;
        int typeId = word.getWordTypeId();
        ConjugationManager decMan = core.getConjugationManager();

        if (typeId == 0) {
            core.getOSHandler().getInfoBox().info("Missing Part of Speech",
                    "Word must have a part of Speech set and the part of speech must have declensions defined before using this feature.");
            ret = false;
        } else if ((decMan.getDimensionalConjugationListTemplate(typeId) == null
                || decMan.getDimensionalConjugationListTemplate(typeId).length == 0)
                && decMan.getDimensionalConjugationListWord(word.getId()).length == 0
                && decMan.getSingletonCombinedIds(typeId).length == 0) {
            core.getOSHandler().getInfoBox().info("Declensions", "No declensions for part of speech: " + word.getWordTypeDisplay()
                    + " set. Declensions can be created per part of speech under the Part of Speech menu by clicking the "
                    + "Declensions button.");
            ret = false;
        }

        return ret;
    }

    private void setupListeners() {
        cmbDimX.addItemListener((ItemEvent event) -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                buildWindowBody();
            }
        });
        cmbDimY.addItemListener((ItemEvent event) -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                buildWindowBody();
            }
        });
    }

    private void populateDecIdToValues() {
        ConjugationPair[] decTemplateList = decMan.getDimensionalCombinedIds(word.getWordTypeId());

        for (ConjugationPair curPair : decTemplateList) {
            // skip forms that have been suppressed
            if (decMan.isCombinedConjlSurpressed(curPair.combinedId, word.getWordTypeId())) {
                continue;
            }

            combDecIds.add(curPair.combinedId);
            labelMap.put(curPair.combinedId, curPair.label);
        }
    }

    private void setupDimDropdowns() {
        ConjugationNode[] declensionNodes = decMan.getDimensionalConjugationListTemplate(word.getWordTypeId());
        DefaultComboBoxModel<ConjugationNode> modelX = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<ConjugationNode> modelY = new DefaultComboBoxModel<>();
        cmbDimX.setModel(modelX);
        cmbDimY.setModel(modelY);

        for (ConjugationNode node : declensionNodes) {
            modelX.addElement(node);
            modelY.addElement(node);
        }

        if (declensionNodes.length > 1) {
            cmbDimX.setSelectedIndex(0);
            cmbDimY.setSelectedIndex(1);
        }
    }

    private void buildWindowBody() {
        ConjugationNode dimX = (ConjugationNode) cmbDimX.getSelectedItem();
        ConjugationNode dimY = (ConjugationNode) cmbDimY.getSelectedItem();

        pnlTabDeclensions.removeAll();

        // only renders as dimensional if there are at least two dimensional declensions. Renders as list otherwise.
        if (shouldRenderDimensional()) {
            if (dimX == dimY) {
                new DesktopInfoBox(this).warning("Dimension Selection", "Please select differing Row and Column values.");
                return;
            } else {
                // get all partial dim ID patterns (sans X & Y dims)/feed to grid panel class
                getPanelPartialDimIds().forEach((partialDim) -> {
                    PDeclensionGridPanel gridPanel
                            = new PDeclensionGridPanel(partialDim, dimX, dimY, word.getWordTypeId(), core, word);
                    pnlTabDeclensions.addTab(gridPanel.getTabName(), gridPanel);
                    autoPopulated = autoPopulated || gridPanel.isAutoPopulated();
                });

                // add singleton values when appropriate
                ConjugationPair[] singletons = decMan.getSingletonCombinedIds(word.getWordTypeId());
                if (singletons.length != 0) {
                    PDeclensionListPanel listPanel = new PDeclensionListPanel(singletons, core, word, false);
                    pnlTabDeclensions.addTab(listPanel.getTabName(), listPanel);
                    autoPopulated = listPanel.isAutoPopulated();
                }
            }
        } else {
            cmbDimX.setVisible(false);
            cmbDimY.setVisible(false);
            jLabel1.setVisible(false);
            jLabel2.setVisible(false);
            ConjugationPair[] completeList = decMan.getAllCombinedIds(word.getWordTypeId());
            PDeclensionListPanel listPanel
                    = new PDeclensionListPanel(completeList, core, word, true);

            pnlTabDeclensions.addTab(listPanel.getTabName(), listPanel);
            autoPopulated = listPanel.isAutoPopulated();
        }

        btnDeprecated.setVisible(decMan.wordHasDeprecatedForms(word));
    }

    private boolean shouldRenderDimensional() {
        return decMan.getDimensionalConjugationListTemplate(word.getWordTypeId()).length > 1;
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
     * Gets partial dim ids to be fed to grid panels Positions containing the
     * values of the dimensions selected by the user replaced with "X" and "Y"
     * respectively Filters out null values (values for disabled word forms)
     */
    private List<String> getPanelPartialDimIds() {
        List<String> partialIds = new ArrayList<>();
        int xNode = decMan.getDimensionTemplateIndex(word.getWordTypeId(), (ConjugationNode) cmbDimX.getSelectedItem());
        int yNode = decMan.getDimensionTemplateIndex(word.getWordTypeId(), (ConjugationNode) cmbDimY.getSelectedItem());

        for (var dimId : combDecIds) {
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
        core.getConjugationManager().clearAllConjugationsWord(word.getId());

        if (chkAutogenOverride.isSelected()) {
            int count = pnlTabDeclensions.getTabCount();
            for (int i = 0; i < count; i++) {
                PDeclensionPanelInterface comp = (PDeclensionPanelInterface) pnlTabDeclensions.getComponentAt(i);

                for (Entry<String, String> entry : comp.getAllDecValues().entrySet()) {
                    ConjugationNode saveNode = new ConjugationNode(-1, core.getConjugationManager());

                    saveNode.setValue(entry.getValue().trim());
                    saveNode.setCombinedDimId(entry.getKey());
                    saveNode.setNotes(labelMap.get(entry.getKey()));

                    // declensions per word not saved via int id any longer
                    decMan.addConjugationToWord(word.getId(), -1, saveNode);
                }
            }
        }

        word.setOverrideAutoConjugate(chkAutogenOverride.isSelected());
    }

    @Override
    public void dispose() {
        if (isDisposed()) {
            super.dispose();
        } else {
            if (!closeWithoutSave && chkAutogenOverride.isSelected()) {
                int userChoice = new DesktopInfoBox(this).yesNoCancel("Save Confirmation", "Save changes?");

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
        }
    }

    public void setCloseWithoutSave(boolean _closeWithoutSave) {
        closeWithoutSave = _closeWithoutSave;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmbDimX = new PComboBox(((DesktopPropertiesManager)core.getPropertiesManager()).getFontMenu());
        cmbDimY = new PComboBox(((DesktopPropertiesManager)core.getPropertiesManager()).getFontMenu());
        pnlTabDeclensions = new javax.swing.JTabbedPane();
        chkAutogenOverride = new PCheckBox();
        btnOk = new PButton(nightMode);
        btnCancel = new PButton(nightMode);
        jLabel1 = new PLabel("");
        jLabel2 = new PLabel("");
        btnDeprecated = new PButton(nightMode);

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
    private javax.swing.JComboBox<org.darisadesigns.polyglotlina.Nodes.ConjugationNode> cmbDimX;
    private javax.swing.JComboBox<org.darisadesigns.polyglotlina.Nodes.ConjugationNode> cmbDimY;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTabbedPane pnlTabDeclensions;
    // End of variables declaration//GEN-END:variables
}
