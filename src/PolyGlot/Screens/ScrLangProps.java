/*
 * Copyright (c) 2015-2017, draque
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

import PolyGlot.DictCore;
import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PButton;
import PolyGlot.JFontChooser;
import PolyGlot.CustomControls.PTextField;
import PolyGlot.ManagersCollections.PropertiesManager;
import PolyGlot.CustomControls.PCheckBox;
import PolyGlot.CustomControls.PFrame;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author draque
 */
public class ScrLangProps extends PFrame {
   
    /**
     * Creates new form ScrLangProps
     * @param _core Dictionary Core
     */
    public ScrLangProps(DictCore _core) {
        core = _core;
        initComponents();
        populateProperties();
        txtAlphaOrder.setFont(core.getPropertiesManager().getFontCon());
        super.getRootPane().getContentPane().setBackground(Color.white);
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
    }
    
    @Override
    public void updateAllValues(DictCore _core) {
        // due to modal nature of form, does nothing
    }
    
    @Override
    public void dispose() {
        saveAllProps();
        core.pushUpdate();
        super.dispose();
    }
    
    @Override
    public boolean thisOrChildrenFocused() {
        return this.isFocusOwner();
    }
    
    private void populateProperties() {
        PropertiesManager prop = core.getPropertiesManager();
        
        txtLangName.setText(prop.getLangName());
        txtFont.setText(prop.getFontCon().getFamily());
        txtAlphaOrder.setText(prop.getAlphaPlainText());
        txtLocalLanguage.setText(prop.getLocalLangName());
        txtAuthorCopyright.setText(prop.getCopyrightAuthorInfo());
        chkDisableProcRegex.setSelected(prop.isDisableProcRegex());
        chkIgnoreCase.setSelected(prop.isIgnoreCase());
        chkLocalMandatory.setSelected(prop.isLocalMandatory());
        chkLocalUniqueness.setSelected(prop.isLocalUniqueness());
        chkTypesMandatory.setSelected(prop.isTypesMandatory());
        chkWordUniqueness.setSelected(prop.isWordUniqueness());
        chkEnforceRTL.setSelected(prop.isEnforceRTL());
    }
    
    /**
     * saves all language properties
     */
    private void saveAllProps() {
        PropertiesManager propMan = core.getPropertiesManager();
        
        propMan.setAlphaOrder(txtAlphaOrder.getText().trim());
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
    }
    
    /**
     * Instantiates font chooser and returns user defined font
     * @return font selected by user
     */
    private Font fontDialog() {
        JFontChooser fontChooser = new JFontChooser();
        setAlwaysOnTop(false);
        Integer result = fontChooser.showDialog(btnChangeFont);
        Font font = null;
        
        if (result == JFontChooser.OK_OPTION) {
            font = fontChooser.getSelectedFont();
        }
        setAlwaysOnTop(true);

        return font;
    }
    
    private void setConFont(Font conFont) {
        if (conFont == null) {
            return;
        }
        
        txtAlphaOrder.setFont(conFont);

        core.getPropertiesManager().setFontCon(conFont, conFont.getStyle(), conFont.getSize());
        txtFont.setText(conFont.getFamily());

        testRTLWarning();
    }
    
    /**
     * Displays warning to user if RTL is enforced and confont is standard
     */
    private void testRTLWarning() {
        Font conFont = core.getPropertiesManager().getFontCon();
        Font stdFont = (new JTextField()).getFont();
        
        if (core.getPropertiesManager().isEnforceRTL()
                && (conFont == null
                    || conFont.getFamily().equals(stdFont.getFamily()))) {
            InfoBox.warning("RTL Font Warning", "Enforcing RTL with default font"
                    + " is not recommended. For best results, please set distinct"
                    + " conlang font.", this);
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
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        btnChangeFont = new PButton(core);
        txtFont = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        txtAlphaOrder = new PTextField(core, false, "-- Alphabetical Order --");
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        chkTypesMandatory = new PCheckBox(core);
        chkLocalMandatory = new PCheckBox(core);
        chkWordUniqueness = new PCheckBox(core);
        chkLocalUniqueness = new PCheckBox(core);
        chkIgnoreCase = new PCheckBox(core);
        chkDisableProcRegex = new PCheckBox(core);
        chkEnforceRTL = new PCheckBox(core);
        jLabel2 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        txtLangName = new PTextField(core, true, "-- Language Name --");
        txtLocalLanguage = new PTextField(core, true, "-- Local Language --");
        jScrollPane4 = new javax.swing.JScrollPane();
        txtAuthorCopyright = new PolyGlot.CustomControls.PTextPane(core, true, "-- Author/Copyright Info --");

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

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtAlphaOrder.setToolTipText("List of all characters in conlang in alphabetical order (both upper and lower case)");

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(2);
        jTextArea1.setText("List all characters used in your conlang here in their alphabetical order without spaces or commas. Leave blank for system default.");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setEnabled(false);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtAlphaOrder))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtAlphaOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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

        chkDisableProcRegex.setText("Disable Orthographic Regex");
        chkDisableProcRegex.setToolTipText("Disable regex features in orthograpy. (this allows for ignoring case properly)");

        chkEnforceRTL.setText("Enforce RTL");
        chkEnforceRTL.setToolTipText("Check this to force all conlang text to appear in RTL fashion through PolyGlot. This works even if the character set you are using is not typically RTL.");
        chkEnforceRTL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkEnforceRTLActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Lexical Enfocement");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkDisableProcRegex)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkTypesMandatory)
                            .addComponent(chkWordUniqueness))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkLocalMandatory)
                            .addComponent(chkLocalUniqueness))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkEnforceRTL)
                            .addComponent(chkIgnoreCase))))
                .addGap(32, 32, 32))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkTypesMandatory)
                    .addComponent(chkLocalMandatory)
                    .addComponent(chkIgnoreCase))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkWordUniqueness)
                    .addComponent(chkLocalUniqueness)
                    .addComponent(chkEnforceRTL))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkDisableProcRegex)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnChangeFont)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFont))
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnChangeFont)
                    .addComponent(txtFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        txtLangName.setToolTipText("Your Conlang's Name");

        txtLocalLanguage.setToolTipText("The natural language you use when writing your conlang");

        jScrollPane4.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane4.setViewportView(txtAuthorCopyright);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4)
            .addComponent(txtLangName)
            .addComponent(txtLocalLanguage, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtLangName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtLocalLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnChangeFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeFontActionPerformed
        setConFont(fontDialog());
    }//GEN-LAST:event_btnChangeFontActionPerformed

    private void chkEnforceRTLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkEnforceRTLActionPerformed
        // needs to update value immediately for text elements on this form affected by change
        core.getPropertiesManager().setEnforceRTL(chkEnforceRTL.isSelected());
        testRTLWarning();
    }//GEN-LAST:event_chkEnforceRTLActionPerformed

    public static ScrLangProps run(DictCore _core) {
        ScrLangProps s = new ScrLangProps(_core);
        s.setupKeyStrokes();
        s.setCore(_core);
        return s;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChangeFont;
    private javax.swing.JCheckBox chkDisableProcRegex;
    private javax.swing.JCheckBox chkEnforceRTL;
    private javax.swing.JCheckBox chkIgnoreCase;
    private javax.swing.JCheckBox chkLocalMandatory;
    private javax.swing.JCheckBox chkLocalUniqueness;
    private javax.swing.JCheckBox chkTypesMandatory;
    private javax.swing.JCheckBox chkWordUniqueness;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField txtAlphaOrder;
    private javax.swing.JTextPane txtAuthorCopyright;
    private javax.swing.JTextField txtFont;
    private javax.swing.JTextField txtLangName;
    private javax.swing.JTextField txtLocalLanguage;
    // End of variables declaration//GEN-END:variables
}
