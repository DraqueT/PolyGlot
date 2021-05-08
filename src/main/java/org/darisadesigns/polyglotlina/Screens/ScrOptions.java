/*
 * Copyright (c) 2017-2020, Draque Thompson, draquemail@gmail.com
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
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextFieldFilter;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.OptionsManager;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.PlainDocument;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;

/**
 *
 * @author draque.thompson
 */
public final class ScrOptions extends PDialog {

    /**
     * Creates new form ScrOptions
     *
     * @param _core
     */
    public ScrOptions(DictCore _core) {
        super(_core, false);
        
        initComponents();
        setOptions();
        final ScrOptions optionsParent = this;
        
        // TODO: Either finish night mode or remove references to it.
        chkNightMode.setVisible(false);
        
        txtRevisionNumbers.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                try {
                    Integer.parseInt(((JTextField)input).getText());
                } catch (NumberFormatException e) {
                    // user error
                    // IOHandler.writeErrorLog(e);
                    new DesktopInfoBox(optionsParent).warning("Bad Input", "Please provide an integer (number) value.");
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public void dispose() {
        if (testWarnClose()) {
            OptionsManager options = PolyGlot.getPolyGlot().getOptionsManager();
            
            double fontSize = Double.parseDouble(txtTextFontSize.getText());
            double fontSizeOriginal = options.getMenuFontSize();
            boolean nightModeOriginal = options.isNightMode();
            int maxReversion = Integer.parseInt(txtRevisionNumbers.getText());
            maxReversion = maxReversion > -1 ? maxReversion : 1;
            
            options.setAnimateWindows(chkResize.isSelected());
            options.setNightMode(chkNightMode.isSelected());
            options.setMenuFontSize(fontSize);
            options.setMaxReversionCount(maxReversion);
            
            // only refresh if font size changed or night mode setting switched
            if (fontSizeOriginal != fontSize|| nightModeOriginal != chkNightMode.isSelected()) {
                PolyGlot.getPolyGlot().refreshUiDefaults();
            }
            
            super.dispose();
        }
    }

    public boolean testWarnClose() {
        boolean ret = true;
        
        if (Double.parseDouble(txtTextFontSize.getText()) > 30.0) {
            ret = false;
            new DesktopInfoBox(this).warning("Inlaid text size", "Text cannot be larger thant 30.0 points.");
        }
        
        return ret;
    }

    private void setOptions() {
        ((PlainDocument) txtTextFontSize.getDocument())
                .setDocumentFilter(new PTextFieldFilter());

        updateAllValues(core);
    }
    
    private void resetOptions() {
        if (new DesktopInfoBox(this).actionConfirmation("Verify Options Reset", 
                "This will reset all options, including last saved file data, screen positions, screen sizes, etc.\n\nContinue?")) {
            PolyGlot.getPolyGlot().getOptionsManager().resetOptions();
            updateAllValues(core);
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
        chkResize = new PCheckBox(nightMode, menuFontSize);
        jLabel1 = new PLabel("", menuFontSize);
        txtTextFontSize = new javax.swing.JTextField();
        chkNightMode = new PCheckBox(nightMode, menuFontSize);
        jLabel2 = new PLabel("", menuFontSize);
        txtRevisionNumbers = new javax.swing.JTextField();
        btnResetOptions = new PButton();
        btnOk = new PButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(319, 278));
        setModal(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        chkResize.setText("Auto Resize Window");
        chkResize.setToolTipText("Resize window to last size of given module automatically");

        jLabel1.setText("Default Font Size");

        chkNightMode.setText("Night Mode");

        jLabel2.setText("Revision States Saved");
        jLabel2.setToolTipText("The max number of prior versions to save in your PGD files. 0 is unlimited (can lead to large files ).");

        txtRevisionNumbers.setToolTipText("The max number of prior versions to save in your PGD files. 0 is unlimited (can lead to large files ).");

        btnResetOptions.setText("Reset Options");
        btnResetOptions.setToolTipText("Resets all options to default values\\n(including last opened files, screen position, etc.)");
        btnResetOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetOptionsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkResize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtTextFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtRevisionNumbers, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 148, Short.MAX_VALUE))
                    .addComponent(chkNightMode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnResetOptions)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkResize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkNightMode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtTextFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtRevisionNumbers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 95, Short.MAX_VALUE)
                .addComponent(btnResetOptions))
        );

        btnOk.setText("OK");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnOk))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOk))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        dispose();
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnResetOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetOptionsActionPerformed
        resetOptions();
        super.dispose();
        PolyGlot.getPolyGlot().refreshUiDefaults();
    }//GEN-LAST:event_btnResetOptionsActionPerformed

    @Override
    public void updateAllValues(DictCore _core) {
        OptionsManager mgr = PolyGlot.getPolyGlot().getOptionsManager();
        
        chkResize.setSelected(mgr.isAnimateWindows());
        chkNightMode.setSelected(mgr.isNightMode());
        txtTextFontSize.setText(Double.toString(mgr.getMenuFontSize()));
        txtRevisionNumbers.setText(Integer.toString(mgr.getMaxReversionCount()));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOk;
    private javax.swing.JButton btnResetOptions;
    private javax.swing.JCheckBox chkNightMode;
    private javax.swing.JCheckBox chkResize;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtRevisionNumbers;
    private javax.swing.JTextField txtTextFontSize;
    // End of variables declaration//GEN-END:variables
}
