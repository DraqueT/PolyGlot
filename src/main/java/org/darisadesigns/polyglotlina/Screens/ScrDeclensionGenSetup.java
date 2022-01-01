/*
 * Copyright (c) 2019-2020, Draque Thompson
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PRadioButton;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;

/**
 * The is the wrapper class which displays either the classic or simplified view
 * of the declension autogen setup.
 *
 * @author Draque Thompson
 */
public final class ScrDeclensionGenSetup extends PDialog {

    private PDialog curDialog;
    private final int typeId;
    private ScrTestWordConj child;

    /**
     * Creates new form ScrDeclensionGenSetup
     *
     * @param _core
     * @param _typeId
     */
    public ScrDeclensionGenSetup(DictCore _core, int _typeId) {
        super(_core, true, null);
        
        typeId = _typeId;
        initComponents();
        setupRadioButtons();
        setupListeners();
        
        this.setTitle("Conjugation/Declension Autogeneration Setup: " 
                + core.getTypes().getNodeById(typeId).getValue());
    }
    
    private void setupListeners() {
        this.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                // do nothing
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                if (curDialog instanceof ScrDeclensionGenClassic) {
                    ((ScrDeclensionGenClassic)curDialog).saveVolatileValues();
                } else if (curDialog instanceof ScrDeclensionGenSimple) {
                    ((ScrDeclensionGenSimple)curDialog).saveRule();
                }
            }
        });
    }

    private void setupRadioButtons() {
        ButtonGroup bGroup = new ButtonGroup();

        bGroup.add(rdoClassic);
        bGroup.add(rdoSimplified);

        final PDialog parent = this;
        ActionListener clicked = (ActionEvent e) -> {
            boolean simplifiedSelected = rdoSimplified.isSelected();
            boolean rulesExist = core.getConjugationManager().getConjugationRulesForType(typeId).length != 0;
            
            // when switching from classic to simplified, warn users if rules are already defined
            if (rulesExist &&
                    simplifiedSelected && !core.getPropertiesManager().isUseSimplifiedConjugations()) {
                String confMessage = "Simplified conjugations have a single transformation per word form. Any forms edited will be left with a SINGLE RULE.\n"
                        + "Only select this option if you do not use more than one rule per form. Would you like to continue?";
                if (core.getOSHandler().getInfoBox().actionConfirmation("Switch to Simplified Conjugations?", confMessage)) {
                    populateForm();
                    core.getPropertiesManager().setUseSimplifiedConjugations(simplifiedSelected);
                } else {
                    rdoSimplified.setSelected(false);
                    rdoClassic.setSelected(true);
                }
            } else {
                populateForm();
                core.getPropertiesManager().setUseSimplifiedConjugations(simplifiedSelected);
            }
        };

        rdoClassic.addActionListener(clicked);
        rdoSimplified.addActionListener(clicked);

        if (core.getPropertiesManager().isUseSimplifiedConjugations()) {
            rdoSimplified.setSelected(true);
        } else {
            rdoClassic.setSelected(true);
        }
        
        populateForm();
    }

    private void populateForm() {
        if (rdoClassic.isSelected()) {
            if (!(curDialog instanceof ScrDeclensionGenClassic)) {
                if (curDialog != null && !curDialog.isDisposed()) {
                    curDialog.dispose();
                }
                
                setMainPanel(new ScrDeclensionGenClassic(core, typeId, this));
            }
        } else {
            if (!(curDialog instanceof ScrDeclensionGenSimple)) {
                if (curDialog != null && !curDialog.isDisposed()) {
                    curDialog.dispose();
                }
                
                setMainPanel(new ScrDeclensionGenSimple(core, typeId));
            }
        }

        jPanel2.add(curDialog.getRootPane());
    }

    private void setMainPanel(PDialog dialog) {
        curDialog = dialog;
        Component display = curDialog.getWindow();
        jPanel2.removeAll();
        this.repaint();

        // set new screen
        GroupLayout layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(display, javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(display, javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
        );
    }

    /**
     * Opens screen declension window
     *
     * @param _core dictionary core
     * @param _typeId type ID to open window for
     * @return a copy of itself
     */
    public static ScrDeclensionGenSetup run(DictCore _core, int _typeId) {
        ScrDeclensionGenSetup s = new ScrDeclensionGenSetup(_core, _typeId);

        ConjugationPair[] decs = _core.getConjugationManager().getAllCombinedIds(_typeId);

        if (decs.length == 0) {
            _core.getOSHandler().getInfoBox().warning("No Declensions Exist", "Please set up some conjugations/declensions for \""
                    + _core.getTypes().getNodeById(_typeId).getValue() + "\" before setting up automatic patterns.");
            s.dispose();
        } else {
            s.setVisible(true);
        }

        return s;
    }
    
    public String getCurSelectedCombId() {
        String ret;
        
        if (curDialog instanceof ScrDeclensionGenClassic) {
            ret = ((ScrDeclensionGenClassic)curDialog).getCurSelectedCombId();
        } else {
            ret = ((ScrDeclensionGenSimple)curDialog).getCurSelectedCombId();
        }
        
        return ret;
    }

    @Override
    public void updateAllValues(DictCore _core) {
        curDialog.updateAllValues(_core);
    }

    @Override
    public void dispose() {
        if (child != null && !child.isDisposed()) {
            child.dispose();
        }
        curDialog.dispose();
        super.dispose();
    }
    
    private void popoutTester() {
        if (child == null || child.isDisposed()) {
            child = new ScrTestWordConj(core, typeId, this);
            child.setVisible(true);
            child.addWindowListener(new WindowListener() {
                    @Override
                    public void windowOpened(WindowEvent ex) {
                    }

                    @Override
                    public void windowClosing(WindowEvent ex) {
                    }

                    @Override
                    public void windowClosed(WindowEvent ex) {
                        btnTestWord.setEnabled(true);
                    }

                    @Override
                    public void windowIconified(WindowEvent ex) {
                    }

                    @Override
                    public void windowDeiconified(WindowEvent ex) {
                    }

                    @Override
                    public void windowActivated(WindowEvent ex) {
                    }

                    @Override
                    public void windowDeactivated(WindowEvent ex) {
                    }
                });
            btnTestWord.setEnabled(false);
        } else {
            child.toFront();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        rdoClassic = new PRadioButton(core, nightMode);
        rdoSimplified = new PRadioButton(core, nightMode);
        btnTestWord = new PButton(nightMode, menuFontSize);
        jPanel2 = new javax.swing.JPanel();
        btnOK = new PButton(nightMode, menuFontSize);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        rdoClassic.setText("Classic Setup");
        rdoClassic.setToolTipText("Use the classic autodeclension setup window. This is highly powerful, but complex.");

        rdoSimplified.setText("Simplified Setup");
        rdoSimplified.setToolTipText("Use the simplified autoconjugation setup. If you have only one pattern per wordform, this is easier to use.");

        btnTestWord.setText("Test");
        btnTestWord.setToolTipText("Open Conjugation/Declension Test Window");
        btnTestWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestWordActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdoClassic, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdoSimplified)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 247, Short.MAX_VALUE)
                .addComponent(btnTestWord)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(rdoClassic)
                .addComponent(rdoSimplified)
                .addComponent(btnTestWord))
        );

        jPanel2.setMinimumSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 312, Short.MAX_VALUE)
        );

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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnOK))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOK))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnTestWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestWordActionPerformed
        popoutTester();
    }//GEN-LAST:event_btnTestWordActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOK;
    private javax.swing.JButton btnTestWord;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton rdoClassic;
    private javax.swing.JRadioButton rdoSimplified;
    // End of variables declaration//GEN-END:variables
}
