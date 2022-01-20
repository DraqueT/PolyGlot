/*
 * Copyright (c) 2020-2021, Draque Thompson, draquemail@gmail.com
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
import java.awt.FontFormatException;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.PlainDocument;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextFieldFilter;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PFontHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;

/**
 *
 * @author draque
 */
public final class ScrFontImportDialog extends PDialog {

    /**
     * Creates new form ScrFontImportDialog
     *
     * @param _core
     */
    public ScrFontImportDialog(DictCore _core) {
        super(_core);
        this.setModal(true);
        initComponents();
        setupListeners();
    }
    
    private void setupListeners() {
        txtFontSize.setText(Double.toString(core.getPropertiesManager().getFontSize()));
        ((PlainDocument) txtFontSize.getDocument())
                .setDocumentFilter(new PTextFieldFilter());
        txtFontSize.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDisplayFont();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDisplayFont();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDisplayFont();
            }
        });
        
        txtFontLocation.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDisplayFont();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDisplayFont();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDisplayFont();
            }
        });
    }
    
    public void setLocalSelected() {
        rdoConlang.setSelected(false);
        rdoLocal.setSelected(true);
    }
    
    public void setConSelected() {
        rdoLocal.setSelected(false);
        rdoConlang.setSelected(true);
    }
    
    private void selectFont() {
        var chooser = getWinCaseJFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Font Files", "ttf", "otf", "ttc", "dfont");

        chooser.setDialogTitle("Select Font File");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(core.getWorkingDirectory());
        chooser.setApproveButtonText("Open");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtFontLocation.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    /**
     * Generates a special JFileChooser that accounts for the awfulness of the 
     * Windows Fonts folder
     * @return 
     */
    public JFileChooser getWinCaseJFileChooser() {
        return new JFileChooser() {
            JDialog curDialog = null;
            boolean alreadyRun = false;
            
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                curDialog = super.createDialog(parent);
                return curDialog;
            }
            
            @Override
            public void setCurrentDirectory(File dir) {
                super.setCurrentDirectory(dir);
                if (dir != null 
                        && dir.getAbsolutePath().endsWith(System.getenv("windir") + "\\Fonts")
                        && curDialog != null
                        && !alreadyRun) {
                    alreadyRun = true;
                    var winFonts = new ScrWinFontFolderSelector(core, PolyGlot.getPolyGlot().getRootWindow());
                    curDialog.setVisible(false);
                    winFonts.pack();
                    winFonts.setModal(true);
                    winFonts.setVisible(true);
                    winFonts.toFront();
                    
                    File result = winFonts.getSeletedFont();
                    this.setSelectedFile(result);
                    
                    if (result == null) {
                        this.cancelSelection();
                    } else {
                        this.approveSelection();
                    }
                    
                    curDialog.dispose();
                }
            }
        };
    }
    
    private void updateDisplayFont() {
        if (!txtFontLocation.getText().isBlank()) {
            try {
                Font newFont = PFontHandler.getFontFromFile(txtFontLocation.getText());
                txtDemoText.setFont(newFont.deriveFont(Float.parseFloat(txtFontSize.getText())));
            } catch (FontFormatException | IOException e) {
                new DesktopInfoBox(this).error("Font Load Error", "Unable to load font: " + e.getLocalizedMessage());
            }
        }
    }
    
    private void closeOk() {
        var fileName = txtFontLocation.getText();
        
        try {
            var propertiesManager = ((DesktopPropertiesManager)core.getPropertiesManager());
            var size = Double.valueOf(txtFontSize.getText());
            
            if (rdoConlang.isSelected()) {
                propertiesManager.setFontFromFile(fileName);
                propertiesManager.setFontSize(size);
            } else {
                propertiesManager.setLocalFontFromFile(fileName);
                propertiesManager.setLocalFontSize(size);
            }
            
            PolyGlot.getPolyGlot().getRootWindow().openProperties();
            dispose();
        } catch (IOException e) {
            core.getOSHandler().getInfoBox().error("IO Error", "Unable to open " + fileName + " due to: " + e.getLocalizedMessage());
        } catch (FontFormatException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("Font Format Error", "Unable to read " + fileName + " due to: "
                    + e.getLocalizedMessage());
        }
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // nothing to update
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        btnOk = new PButton();
        btnCancel = new PButton();
        jPanel1 = new javax.swing.JPanel();
        txtFontLocation = new PTextField(core, true, "Font File");
        btnSelectFont = new PButton();
        jLabel1 = new PLabel("Font Size", PolyGlot.getPolyGlot().getOptionsManager().getMenuFontSize());
        txtFontSize = new PTextField(core, true, "");
        txtDemoText = new PTextField(core, false, "");
        rdoConlang = new javax.swing.JRadioButton();
        rdoLocal = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select Font to Import");

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

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        txtFontLocation.setEditable(false);

        btnSelectFont.setText("Select Font");
        btnSelectFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectFontActionPerformed(evt);
            }
        });

        jLabel1.setText("Font Size");

        txtDemoText.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtDemoText.setText("Demo Text");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtFontLocation)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSelectFont))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(txtDemoText))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFontLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelectFont))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDemoText, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                .addContainerGap())
        );

        buttonGroup1.add(rdoConlang);
        rdoConlang.setSelected(true);
        rdoConlang.setText("ConLang Font");

        buttonGroup1.add(rdoLocal);
        rdoLocal.setText("Local Font");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdoConlang, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdoLocal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                .addComponent(btnCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOk)
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel)
                    .addComponent(rdoConlang)
                    .addComponent(rdoLocal))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        closeOk();
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnSelectFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectFontActionPerformed
        selectFont();
    }//GEN-LAST:event_btnSelectFontActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JButton btnSelectFont;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton rdoConlang;
    private javax.swing.JRadioButton rdoLocal;
    private javax.swing.JTextField txtDemoText;
    private javax.swing.JTextField txtFontLocation;
    private javax.swing.JTextField txtFontSize;
    // End of variables declaration//GEN-END:variables
}
