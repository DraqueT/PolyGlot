/*
 * Copyright (c) 2017-2023, Draque Thompson, draquemail@gmail.com
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

import javax.swing.event.ChangeEvent;
import javax.swing.text.PlainDocument;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextFieldFilter;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopOptionsManager;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;

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
        setupListeners();
        
        if (PGTUtil.IS_OSX) {
            lblUiScaling.setEnabled(false);
            sldUiScaling.setEnabled(false);
            sldUiScaling.setToolTipText("UI scaling unavailable on macOS");
        } else {
            lblUiScaling.setText("UI Scaling: " + (sldUiScaling.getValue() / 10.0));
        }
        
        ((PlainDocument)txtRevisionNumbers.getDocument()).setDocumentFilter(new PTextFieldFilter());
        ((PlainDocument)txtAutoSave.getDocument()).setDocumentFilter(new PTextFieldFilter());
    }
    
    private void setupListeners() {
        sldUiScaling.addChangeListener((ChangeEvent e) -> {
            lblUiScaling.setText("UI Scaling: " + (sldUiScaling.getValue() / 10.0));
        });
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        
        var dimensions = this.getMinimumSize();
        
        if (this.getWidth() < dimensions.getWidth() || this.getHeight() < dimensions.getHeight()) {
            this.setSize(dimensions);
        }
    }

    @Override
    public void dispose() {
        if (testWarnClose()) {
            DesktopOptionsManager options = PolyGlot.getPolyGlot().getOptionsManager();
            
            double scalingOriginal = options.getUiScale();
            int maxReversion = Integer.parseInt(txtRevisionNumbers.getText());
            maxReversion = maxReversion > -1 ? maxReversion : 1;
            int autoSaveInteral = (int) 
                    (Float.parseFloat(txtAutoSave.getText()) * 60000); // multiply to get val in MS
            
            options.setAnimateWindows(chkResize.isSelected());
            options.setMaxReversionCount(maxReversion);
            options.setMsBetweenSaves(autoSaveInteral);
            options.setUiScale(sldUiScaling.getValue() / 10.0);
            options.setGptApiKey(txtGptApiKey.getText());
            
            if (scalingOriginal != (sldUiScaling.getValue() / 10.0) && !isDisposed()) {
                new DesktopInfoBox().info(
                        "Please Restart PolyGlot", "Please Restart PolyGlot for UI scaling change to be applied."
                );
            }
            
            super.dispose();
        }
    }

    public boolean testWarnClose() {
        boolean ret = true;
        
        return ret;
    }

    private void setOptions() {
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
        chkResize = new PCheckBox(nightMode);
        jLabel2 = new PLabel("");
        txtRevisionNumbers = new javax.swing.JTextField();
        btnResetOptions = new PButton();
        jLabel3 = new PLabel("");
        txtAutoSave = new javax.swing.JTextField();
        sldUiScaling = new javax.swing.JSlider();
        lblUiScaling = new PLabel("");
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new PLabel("");
        txtGptApiKey = new javax.swing.JTextField();
        btnOk = new PButton();

        setTitle("PolyGlot Options");
        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(319, 330));
        setModal(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        chkResize.setText("Auto Resize Window");
        chkResize.setToolTipText("Resize window to last size of given module automatically");

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

        jLabel3.setText("Autosave Frequency");

        txtAutoSave.setToolTipText("Frequency in minutes between saving to temp recovery file");

        sldUiScaling.setMajorTickSpacing(1);
        sldUiScaling.setMaximum(40);
        sldUiScaling.setMinimum(1);
        sldUiScaling.setToolTipText("Allows for scaling of UI. Left is smaller, right is bigger (requires restart)");
        sldUiScaling.setValue(20);

        lblUiScaling.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblUiScaling.setText("UI Scaling");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("GPT API Key");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnResetOptions)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sldUiScaling, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                            .addComponent(chkResize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblUiScaling, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtAutoSave, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtRevisionNumbers, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jSeparator1)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtGptApiKey)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkResize)
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtRevisionNumbers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtAutoSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(sldUiScaling, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblUiScaling, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtGptApiKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
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
        DesktopOptionsManager mgr = PolyGlot.getPolyGlot().getOptionsManager();
        
        chkResize.setSelected(mgr.isAnimateWindows());
        txtRevisionNumbers.setText(Integer.toString(mgr.getMaxReversionCount()));
        txtAutoSave.setText(Float.toString(mgr.getMsBetweenSaves()/60000.0f));
        sldUiScaling.setValue((int)(mgr.getUiScale() * 10));
        txtGptApiKey.setText(mgr.getGptApiKey());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOk;
    private javax.swing.JButton btnResetOptions;
    private javax.swing.JCheckBox chkResize;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblUiScaling;
    private javax.swing.JSlider sldUiScaling;
    private javax.swing.JTextField txtAutoSave;
    private javax.swing.JTextField txtGptApiKey;
    private javax.swing.JTextField txtRevisionNumbers;
    // End of variables declaration//GEN-END:variables
}
