/*
 * Copyright (c) 2017-2021, Draque Thompson, draquemail@gmail.com
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
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PComboBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PPanelDrawEtymology;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextPane;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.EtymologyManager;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.EtyExternalParent;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;

/**
 * This screen is used for viewing and modifying the etymology of a word
 * @author DThompson
 */
public final class ScrEtymRoots extends PDialog {

    private final ConWord word;

    /**
     * Creates new form ScrEtymRoots
     *
     * @param _core
     * @param _word
     */
    public ScrEtymRoots(DictCore _core, ConWord _word) {
        super(_core);
        
        word = _word;

        initComponents();

        if (_word == null) {
            this.dispose();
            return;
        }

        this.setModal(true);
        setupParentsPanels();
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        lblWord.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon());
        setupDrawPanel();
        setupForm();
        txtNotes.setText(word.getEtymNotes());
    }
    
    private void setupForm() {
        int divider = PolyGlot.getPolyGlot().getOptionsManager().getDividerPosition(this.getClass().getName());
        
        if (divider > -1) {
            jSplitPane1.setDividerLocation(divider);
        }
    }

    private void setupDrawPanel() {
        PPanelDrawEtymology myPanel = new PPanelDrawEtymology(core, word);
        jSplitPane1.setRightComponent(myPanel);
    }

    /**
     * Sets up both parent panels (internal and external)
     */
    private void setupParentsPanels() {
        setupInternalParentsPanel();
        setupExternalParentsPanel();
    }

    /**
     * Sets up external parents panel
     */
    private void setupExternalParentsPanel() {
        pnlParentsExt.removeAll();
        pnlParentsExt.setLayout(new BoxLayout(pnlParentsExt, BoxLayout.Y_AXIS));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        
        // Cycle through existing external parents and add them to the display
        List<EtyExternalParent> parents = Arrays.asList(core.getEtymologyManager().getWordExternalParents(word.getId()));
        parents.stream().map((extPar) -> {
            JPanel miniPanel = new JPanel();
            final PTextField p = new PTextField(core, true, "");
            PButton delButton = new PButton(nightMode, menuFontSize);
            p.setEditable(false);
            if (extPar.getExternalLanguage().isEmpty()) {
                p.setText(extPar.getValue());
            } else {
                p.setText(extPar.getValue() + " (" + extPar.getExternalLanguage() + ")");
            }
            p.setAssociatedObject(extPar);
            String toolTipString = extPar.getValue();
            if (!extPar.getExternalLanguage().isEmpty()) {
                toolTipString += " - " + extPar.getExternalLanguage();
            }
            if (!extPar.getDefinition().isEmpty()) {
                toolTipString += " - " + extPar.getDefinition();
            }
            p.setToolTipText(toolTipString);
            delButton.setText("-");
            delButton.setToolTipText("Remove external parent from etymological lineage");
            delButton.addActionListener((ActionEvent e) -> {
                delExtParent((EtyExternalParent) p.getAssociatedObject());
            });
            miniPanel.setMaximumSize(new Dimension(999, delButton.getPreferredSize().height));
            miniPanel.setMinimumSize(new Dimension(30, delButton.getPreferredSize().height));
            miniPanel.setPreferredSize(new Dimension(30, 0));
            miniPanel.setLayout(new BoxLayout(miniPanel, BoxLayout.X_AXIS));
            miniPanel.add(p, gbc);
            miniPanel.add(delButton, gbc);
            return miniPanel;
        }).forEachOrdered((miniPanel) -> {
            pnlParentsExt.add(miniPanel, gbc);
        });

        // create and add button to add new external parents
        PButton addButton = new PButton(nightMode, menuFontSize);
        addButton.setText("+");
        addButton.setToolTipText("Add new etymological parent to lineage");
        final Window parentScreen = this;
        addButton.addActionListener((ActionEvent e) -> {
            PTextInputDialog dialog = new PTextInputDialog(parentScreen, core,
                    "New External Parent", "Please enter details for the new external parent.");
            dialog.addField("External Word", true, "This is the word in an external language which is the etymological root of your invented word.");
            dialog.addField("Origin Language", true, "This is the language where the root originates.");
            dialog.addField("Brief Definition", true, "This is a brief text description of the root's definition.");
            dialog.setVisible(true);
            addNewExtParent(dialog.getOrderedFields());
            setupExternalParentsPanel();
        });
        JPanel vertExpand = new JPanel();
        vertExpand.setPreferredSize(new Dimension(9999,9999));
        vertExpand.setMinimumSize(new Dimension(0,0));
        pnlParentsExt.add(vertExpand, gbc);
        
        JPanel miniPanel = new JPanel();
        miniPanel.setPreferredSize(new Dimension(30, addButton.getPreferredSize().height));
        miniPanel.setLayout(new BoxLayout(miniPanel, BoxLayout.X_AXIS));
        miniPanel.add(Box.createHorizontalGlue(), gbc);
        miniPanel.add(addButton, gbc);        
        
        pnlParentsExt.add(miniPanel, gbc);
        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }

    /**
     * Deletes given external parent from current word, then refreshes view
     *
     * @param parent
     */
    private void delExtParent(EtyExternalParent parent) {
        int divLoc = jSplitPane1.getDividerLocation();
        core.getEtymologyManager().delExternalRelation(parent, word.getId());
        setupExternalParentsPanel();
        jSplitPane1.setDividerLocation(divLoc);
    }

    /**
     * Consumes list of ordered fields from a text input dialog. List must be of
     * 3 fields exactly, corresponding (in order) to word, parent language, and
     * definition, then creates an external parent which is added to the current
     * word
     *
     * @param values Values to add to external parent and associate to current
     * word
     */
    private void addNewExtParent(PTextField[] values) {
        if (values.length > 0) { // empty set means user clicked cancel
            if (values.length != 3) {
                new DesktopInfoBox(this).error("Wrong number number of values", "Wrong number of values provided to create external parent.");
            } else if (values[0].getText().isEmpty()) {
                new DesktopInfoBox(this).error("Blank word not allowed", "At minimum, a value for the external parent's word must be provided.");
            } else {
                EtyExternalParent newParent = new EtyExternalParent();
                newParent.setValue(values[0].getText());
                newParent.setExternalLanguage(values[1].getText());
                newParent.setDefinition(values[2].getText());

                core.getEtymologyManager().addExternalRelation(newParent, word.getId());
            }
        }
    }

    /**
     * Populates panel with internal etymological parents plus one blank field
     */
    private void setupInternalParentsPanel() {
        pnlParentsInt.removeAll();
        pnlParentsInt.setLayout(new BoxLayout(pnlParentsInt, BoxLayout.Y_AXIS));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;

        // populate all existing parents as noneditable text boxes with deletion buttons
        for (Integer parentId : core.getEtymologyManager().getWordParentsIds(word.getId())) {
            JPanel miniPanel = new JPanel();
            final PTextField textField = new PTextField(core, false, "");
            PButton delButton = new PButton(nightMode, menuFontSize);

            // this field holds the text from a parent value
            textField.setEditable(false);
            textField.setText(core.getWordCollection().getNodeById(parentId).getValue());
            textField.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon());
            textField.setMaximumSize(new Dimension(9999, textField.getPreferredSize().height));
            textField.setMinimumSize(new Dimension(1, textField.getPreferredSize().height));
            textField.setToolTipText("Parent word");
            textField.setContentId(parentId);

            // button to remove parent value
            delButton.setText("-");
            delButton.setToolTipText("Delete this parent from your word.");
            delButton.addActionListener((ActionEvent e) -> {
                int divLoc = jSplitPane1.getDividerLocation();
                core.getEtymologyManager().delRelation(textField.getContentId(), word.getId());
                setupDrawPanel();
                setupParentsPanels();
                jSplitPane1.setDividerLocation(divLoc);
            });

            // panel to contain parent value and removal button
            miniPanel.setPreferredSize(new Dimension(30, 0));
            miniPanel.setLayout(new BoxLayout(miniPanel, BoxLayout.X_AXIS));
            miniPanel.add(textField, gbc);
            miniPanel.add(delButton, gbc);

            pnlParentsInt.add(miniPanel, gbc);
        }

        //create new dropdown for potential additional parent to be added
        final PComboBox<Object> newParentBox = new PComboBox<>(((DesktopPropertiesManager)core.getPropertiesManager()).getFontMenu());
        newParentBox.setToolTipText("Add new parent to word here.");
        newParentBox.setDefaultText("Select Parent");
        newParentBox.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon());
        DefaultComboBoxModel<Object> comboModel = new DefaultComboBoxModel<>();
        newParentBox.setModel(comboModel);
        newParentBox.setMaximumSize(new Dimension(99999, newParentBox.getPreferredSize().height));
        comboModel.addElement("");
        for (ConWord newParent : core.getWordCollection().getWordNodes()) {
            comboModel.addElement(newParent);
        }

        // on selection of a parent word, save to selected word and repaint
        newParentBox.addActionListener(new ActionListener() {
            final PComboBox me = newParentBox;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (me.getSelectedItem() instanceof ConWord) {
                    addRelation(((ConWord) me.getSelectedItem()).getId(), word.getId());
                    setupParentsPanels();
                }
            }

        });

        // create entries for all external words
        pnlParentsInt.add(newParentBox, gbc);

        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }

    private void addRelation(Integer parentId, Integer childId) {
        try {
            core.getEtymologyManager().addRelation(parentId, childId);
        } catch (EtymologyManager.IllegalLoopException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("Illegal Loop: Parent not Added", e.getLocalizedMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblWord = new PLabel(word.getValue(), menuFontSize);
        jLabel1 = new PLabel("", menuFontSize);
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtEtyFamily = new javax.swing.JTextArea();
        jLabel4 = new PLabel("", menuFontSize);
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtNotes = new PTextPane(core, true, "-- Etymological Notes --");
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new PLabel("", menuFontSize);
        pnlParentsInt = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new PLabel("", menuFontSize);
        pnlParentsExt = new javax.swing.JPanel();
        btnOK = new PButton(nightMode, menuFontSize);

        setTitle("Etymology Setup and Graphical Tree");
        setMinimumSize(new java.awt.Dimension(0, 425));

        lblWord.setBackground(new java.awt.Color(255, 255, 255));
        lblWord.setMaximumSize(new java.awt.Dimension(0, 20));

        jLabel1.setText("Etymology for:");
        jLabel1.setPreferredSize(new java.awt.Dimension(92, 1));

        jSplitPane1.setBackground(new java.awt.Color(255, 255, 255));
        jSplitPane1.setDividerLocation(225);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(9999, 9999));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        txtEtyFamily.setColumns(20);
        txtEtyFamily.setRows(5);
        jScrollPane1.setViewportView(txtEtyFamily);

        jLabel4.setText("Visual Etymological Graph");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        txtNotes.setPreferredSize(new java.awt.Dimension(0, 200));
        jScrollPane2.setViewportView(txtNotes);

        jSplitPane2.setBackground(new java.awt.Color(255, 255, 255));
        jSplitPane2.setDividerLocation(183);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jLabel2.setText("Internal Parents");
        jLabel2.setToolTipText("Etymological parents internal to your language");

        pnlParentsInt.setBackground(new java.awt.Color(255, 255, 255));
        pnlParentsInt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlParentsInt.setMinimumSize(new java.awt.Dimension(100, 200));

        javax.swing.GroupLayout pnlParentsIntLayout = new javax.swing.GroupLayout(pnlParentsInt);
        pnlParentsInt.setLayout(pnlParentsIntLayout);
        pnlParentsIntLayout.setHorizontalGroup(
            pnlParentsIntLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlParentsIntLayout.setVerticalGroup(
            pnlParentsIntLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(0, 121, Short.MAX_VALUE))
            .addComponent(pnlParentsInt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlParentsInt, javax.swing.GroupLayout.PREFERRED_SIZE, 159, Short.MAX_VALUE))
        );

        jSplitPane2.setTopComponent(jPanel3);

        jLabel3.setText("External Parents");
        jLabel3.setToolTipText("Etymological parents external to your language");

        pnlParentsExt.setBackground(new java.awt.Color(255, 255, 255));
        pnlParentsExt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout pnlParentsExtLayout = new javax.swing.GroupLayout(pnlParentsExt);
        pnlParentsExt.setLayout(pnlParentsExtLayout);
        pnlParentsExtLayout.setHorizontalGroup(
            pnlParentsExtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 217, Short.MAX_VALUE)
        );
        pnlParentsExtLayout.setVerticalGroup(
            pnlParentsExtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 128, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(pnlParentsExt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(1, 1, 1)
                .addComponent(pnlParentsExt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane2.setRightComponent(jPanel4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addComponent(jSplitPane2)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jSplitPane2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel2);

        btnOK.setText("OK");
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnOK))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblWord, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(166, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblWord, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOK))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        dispose();
    }//GEN-LAST:event_btnOKActionPerformed

    @Override
    public void updateAllValues(DictCore _core) {
        // this should never be needed on this window
    }

    @Override
    public void dispose() {
        word.setEtymNotes(txtNotes.getText());
        PolyGlot.getPolyGlot().getOptionsManager().setDividerPosition(getClass().getName(), jSplitPane1.getDividerLocation());
        super.dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JLabel lblWord;
    private javax.swing.JPanel pnlParentsExt;
    private javax.swing.JPanel pnlParentsInt;
    private javax.swing.JTextArea txtEtyFamily;
    private javax.swing.JTextPane txtNotes;
    // End of variables declaration//GEN-END:variables
}
