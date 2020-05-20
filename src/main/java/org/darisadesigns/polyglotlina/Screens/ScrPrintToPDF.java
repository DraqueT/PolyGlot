/*
 * Copyright (c) 2016-2020, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.CustomControls.PButton;
import org.darisadesigns.polyglotlina.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.CustomControls.PTextPane;
import org.darisadesigns.polyglotlina.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.IOHandler;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.darisadesigns.polyglotlina.Java8Bridge;

/**
 *
 * @author draque.thompson
 */
public class ScrPrintToPDF extends PDialog {

    /**
     * Creates new form ScrPrintToPDF
     * @param _core Dictionary core
     */
    public ScrPrintToPDF(DictCore _core) {
        super(_core);
        
        initComponents();        
        
        chkConLocal.setText("Print " + core.conLabel() + " -> " + core.localLabel() + " Dictionary");
        chkLocalCon.setText("Print " + core.localLabel() + " -> " + core.conLabel() + " Dictionary");
        
        txtForeword.setText(""); // default test fails to load here... forcing update
        
        setModal(true);
    }

    @Override
    public final void setModal(boolean _modal) {
        super.setModal(_modal);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        txtSavePath = new PTextField(core, true, "-- Save to File --");
        btnSelectSavePath = new PButton(nightMode, menuFontSize);
        txtImageLocation = new PTextField(core, true, "-- Cover Image --");
        btnSelectImagePath = new PButton(nightMode, menuFontSize);
        jLabel4 = new PLabel("", menuFontSize);
        jPanel2 = new javax.swing.JPanel();
        chkLocalCon = new PCheckBox(nightMode, menuFontSize);
        chkConLocal = new PCheckBox(nightMode, menuFontSize);
        chkOrtho = new PCheckBox(nightMode, menuFontSize);
        chkGrammar = new PCheckBox(nightMode, menuFontSize);
        chkLogographs = new PCheckBox(nightMode, menuFontSize);
        chkPageNum = new PCheckBox(nightMode, menuFontSize);
        chkGloss = new PCheckBox(nightMode, menuFontSize);
        chkEtymology = new PCheckBox(nightMode, menuFontSize);
        chkPrintConjugations = new PCheckBox(nightMode, menuFontSize);
        txtTitle = new PTextField(core, true, "-- Title --");
        txtSubtitle = new PTextField(core, true, "-- Title --");
        jScrollPane2 = new javax.swing.JScrollPane();
        txtForeword = new PTextPane(core, true, "-- Author Foreword --");
        btnPrint = new PButton(nightMode, menuFontSize);
        btnCancel = new PButton(nightMode, menuFontSize);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PDF Print Options");
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        txtSavePath.setEditable(false);
        txtSavePath.setBackground(new java.awt.Color(204, 204, 204));

        btnSelectSavePath.setText("Select Location...");
        btnSelectSavePath.setToolTipText("Select location to save PDF document to.");
        btnSelectSavePath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectSavePathActionPerformed(evt);
            }
        });

        txtImageLocation.setEditable(false);
        txtImageLocation.setBackground(new java.awt.Color(204, 204, 204));

        btnSelectImagePath.setText("Select Image...");
        btnSelectImagePath.setToolTipText("Select the image (if any) to display on the front cover of your PDF");
        btnSelectImagePath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectImagePathActionPerformed(evt);
            }
        });

        jLabel4.setText("Print Options");

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        chkLocalCon.setSelected(true);
        chkLocalCon.setText("Print Local -> Conlang Dictionary");
        chkLocalCon.setToolTipText("Select to include a dictionary with lookup by native words");

        chkConLocal.setSelected(true);
        chkConLocal.setText("Print Conlang -> Local Dictionary");
        chkConLocal.setToolTipText("Select to include a dictionary with lookup by constructed words");

        chkOrtho.setSelected(true);
        chkOrtho.setText("Print Orthography");
        chkOrtho.setToolTipText("Include character and pronunciation guides");

        chkGrammar.setSelected(true);
        chkGrammar.setText("Print Grammar");
        chkGrammar.setToolTipText("Include text from the grammar section of your library");

        chkLogographs.setText("Print Logographs");
        chkLogographs.setToolTipText("Coming soon! (if anyone asks for it)");
        chkLogographs.setEnabled(false);

        chkPageNum.setSelected(true);
        chkPageNum.setText("Print Page Number");
        chkPageNum.setToolTipText("Prints page number on each page of PDF");

        chkGloss.setSelected(true);
        chkGloss.setText("Print Gloss Key");
        chkGloss.setToolTipText("Prints a key for part of speech glosses");

        chkEtymology.setText("Print Etymology Trees");

        chkPrintConjugations.setText("Print All Conjugations");
        chkPrintConjugations.setToolTipText("If checked, words will have a list of all conjugated forms printed under their definitions.");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkConLocal)
                            .addComponent(chkGrammar)
                            .addComponent(chkOrtho)
                            .addComponent(chkLocalCon)
                            .addComponent(chkLogographs))
                        .addContainerGap(137, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkGloss)
                            .addComponent(chkPageNum)
                            .addComponent(chkEtymology)
                            .addComponent(chkPrintConjugations))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(chkOrtho)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkGloss)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkConLocal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkLocalCon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkGrammar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkLogographs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkPageNum)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkPrintConjugations)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chkEtymology)
                .addContainerGap())
        );

        txtTitle.setToolTipText("The title of your document");

        txtSubtitle.setToolTipText("The subtitle of your document (if any)");

        jScrollPane2.setViewportView(txtForeword);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtImageLocation)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSelectImagePath))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 266, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtSavePath)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSelectSavePath))
                    .addComponent(txtTitle)
                    .addComponent(txtSubtitle, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSavePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelectSavePath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtImageLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelectImagePath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtSubtitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnPrint.setText("Print");
        btnPrint.setToolTipText("Print to PDF file with selected values");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.setToolTipText("Cancel without creating PDF");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPrint)
                .addGap(9, 9, 9))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnPrint)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectSavePathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectSavePathActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Language");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Documents", "pdf");
        String fileName = core.getCurFileName().replaceAll(".pgd", ".pdf");;
        chooser.setFileFilter(filter);
        chooser.setApproveButtonText("Save");
        chooser.setCurrentDirectory(core.getWorkingDirectory());
        chooser.setSelectedFile(new File(fileName));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
            if (!fileName.contains(".pdf")) {
                fileName += ".pdf";
            }            
            txtSavePath.setText(fileName);
        }
    }//GEN-LAST:event_btnSelectSavePathActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        if (txtSavePath.getText().isEmpty()) {
            InfoBox.warning("File not Specified", "Please specify a file to save to.", core.getRootWindow());
            return;
        }
        
        if (new File(txtSavePath.getText()).exists()
                && !InfoBox.actionConfirmation("Overwrite Confirmation", "File already exists. Overwrite?", this)) {
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try{
            Java8Bridge.exportPdf(txtSavePath.getText(), 
                    txtImageLocation.getText(), 
                    ((PTextPane)txtForeword).getNakedText(), 
                    chkConLocal.isSelected(), 
                    chkLocalCon.isSelected(), 
                    chkOrtho.isSelected(), 
                    txtSubtitle.getText(), 
                    txtTitle.getText(), 
                    chkPageNum.isSelected(), 
                    chkGloss.isSelected(), 
                    chkGrammar.isSelected(), 
                    chkEtymology.isSelected(), 
                    chkPrintConjugations.isSelected(),
                    core);

            if (Desktop.isDesktopSupported()) {
                if (InfoBox.yesNoCancel("Print Success", "PDF successfully printed. Open file now?", this) 
                        == JOptionPane.YES_OPTION) {
                    if (!IOHandler.openFileNativeOS(txtSavePath.getText())) {
                        InfoBox.error("File Error", "Unable to open PDF at location: " + txtSavePath.getText(), core.getRootWindow());
                    }
                }
            } else {
                InfoBox.info("Print Success", "Successfully printed to " + txtSavePath.getText(), core.getRootWindow());
            }
            
            this.dispose();
        } catch (IOException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Save Error", e.getMessage(), core.getRootWindow());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_btnPrintActionPerformed

    private void btnSelectImagePathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectImagePathActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Logograph Image");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images",
                "BMP", "bmp", "jpeg", "wbmp", "gif", "GIF", "png", "JPG", "jpg", "WBMP", "JPEG", "PNG");
        chooser.setFileFilter(filter);
        String fileName;
        chooser.setCurrentDirectory(core.getWorkingDirectory());

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        
        txtImageLocation.setText(fileName);
    }//GEN-LAST:event_btnSelectImagePathActionPerformed

    /**
     * Open PDF print window
     * @param _core Dictionary Core
     */
    public static void run(final DictCore _core) {
        _core.getRootWindow().saveAllValues();
        java.awt.EventQueue.invokeLater(() -> {
            new ScrPrintToPDF(_core).setVisible(true);
        });
    }

    @Override
    public void updateAllValues(DictCore _core) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnSelectImagePath;
    private javax.swing.JButton btnSelectSavePath;
    private javax.swing.JCheckBox chkConLocal;
    private javax.swing.JCheckBox chkEtymology;
    private javax.swing.JCheckBox chkGloss;
    private javax.swing.JCheckBox chkGrammar;
    private javax.swing.JCheckBox chkLocalCon;
    private javax.swing.JCheckBox chkLogographs;
    private javax.swing.JCheckBox chkOrtho;
    private javax.swing.JCheckBox chkPageNum;
    private javax.swing.JCheckBox chkPrintConjugations;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane txtForeword;
    private javax.swing.JTextField txtImageLocation;
    private javax.swing.JTextField txtSavePath;
    private javax.swing.JTextField txtSubtitle;
    private javax.swing.JTextField txtTitle;
    // End of variables declaration//GEN-END:variables
}
