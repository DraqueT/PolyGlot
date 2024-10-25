/*
 * Copyright (c) 2023, Draque Thompson, draquemail@gmail.com
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
import java.awt.EventQueue;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.PlainDocument;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.NumericDocumentFilter;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopOptionsManager;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.DictCore;

/**
 *
 * @author draquethompson
 */
public class ScrWebService extends PFrame {
    
    private final DesktopOptionsManager optMan;
    
    public ScrWebService(DictCore core) {
        super(core);
        optMan = PolyGlot.getPolyGlot().getOptionsManager();
        initComponents();
        setup();
        setupListeners();
    }
    
    private void setup() {
        filterForNumeric(txtPort);
        filterForNumeric(txtMTokenCapacity);
        filterForNumeric(txtMTokenRefil);
        filterForNumeric(txtITokenCapacity);
        filterForNumeric(txtITokenRefil);
        
        populateValues();
        
        var parent = this;
        
        // thread to update log
        new Thread() {
            @Override
            public void run() {
                while (!parent.isDisposed()) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        DesktopIOHandler.getInstance().writeErrorLog(e);
                        EventQueue.invokeLater(() -> {
                            txtLog.setText("ERROR UPDATING SERVER LOG");
                        });
                    }
                    EventQueue.invokeLater(() -> {
                        try {
                            var vScroll = pnlLog.getVerticalScrollBar();
                            var atBottom = vScroll.getValue() == vScroll.getMaximum();
                            txtLog.setText(PolyGlot.getWebServiceLog());

                            // set vertical scroll to bottom if it started there
                            if (atBottom) {
                                pnlLog.getVerticalScrollBar().setValue(vScroll.getMaximum());
                            }
                        } catch (Exception e) {
                            DesktopIOHandler.getInstance().writeErrorLog(e);
                        }
                    });
                }
            }
        }.start();
    }
    
    private void setupListeners() {
        chkActivateWebservice.addActionListener((event) -> {
            if(chkActivateWebservice.isSelected()) {
                try{
                PolyGlot.startWebService();
                } catch (Exception e) {
                    DesktopIOHandler.getInstance().writeErrorLog(e);
                    EventQueue.invokeLater(() -> {
                        txtLog.setText("ERROR STARTING WEBSERVICE: " + e.getLocalizedMessage());
                    });
                }
            } else {
                PolyGlot.stopWebService();
            }
        });
    }
    
    private void filterForNumeric(JTextField textField) {
        var document = (PlainDocument)textField.getDocument();
        document.setDocumentFilter(new NumericDocumentFilter(false));
    }
    
    private void populateValues() {
        EventQueue.invokeLater(() -> {
            txtPort.setText("" + optMan.getWebServicePort());
            txtMTokenCapacity.setText("" + optMan.getWebServiceMasterTokenCapacity());
            txtMTokenRefil.setText("" + optMan.getWebServiceMasterTokenRefill());
            txtITokenCapacity.setText("" + optMan.getWebServiceIndividualTokenCapacity());
            txtITokenRefil.setText("" + optMan.getWebServiceIndividualTokenRefil());
            chkActivateWebservice.setSelected(PolyGlot.isWebServiceRunning());
            txtServedFolder.setText(optMan.getWebServiceTargetFolder().toString());
        });
    }
    
    private void saveAndExit() {
        optMan.setWebServicePort(Integer.parseInt(txtPort.getText()));
        optMan.setWebServiceMasterTokenCapacity(Integer.parseInt(txtMTokenCapacity.getText()));
        optMan.setWebServiceMasterTokenRefill(Integer.parseInt(txtMTokenRefil.getText()));
        optMan.setWebServiceIndividualTokenCapacity(Integer.parseInt(txtITokenCapacity.getText()));
        optMan.setWebServiceIndividualTokenRefil(Integer.parseInt(txtITokenRefil.getText()));
        this.dispose();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new PLabel();
        jLabel2 = new PLabel();
        txtPort = new javax.swing.JTextField();
        txtMTokenCapacity = new javax.swing.JTextField();
        jLabel3 = new PLabel();
        txtMTokenRefil = new javax.swing.JTextField();
        jLabel4 = new PLabel();
        txtITokenCapacity = new javax.swing.JTextField();
        jLabel5 = new PLabel();
        txtITokenRefil = new javax.swing.JTextField();
        jLabel7 = new PLabel();
        txtServedFolder = new javax.swing.JTextField();
        pnlLog = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jButton2 = new PButton();
        jButton3 = new PButton();
        jLabel6 = new PLabel();
        chkActivateWebservice = new PCheckBox();
        jLabel8 = new PLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PolyGlot Webservice Panel");

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setText("Port");

        jLabel2.setText("Master Token Capacity");
        jLabel2.setToolTipText("");

        jLabel3.setText("Master Token Refil");

        jLabel4.setText("Individual Token Capacity");

        jLabel5.setText("Individual Token Refil");

        jLabel7.setText("Served From Folder");

        txtServedFolder.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtServedFolder)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPort)
                            .addComponent(txtMTokenCapacity)
                            .addComponent(txtMTokenRefil)
                            .addComponent(txtITokenCapacity)
                            .addComponent(txtITokenRefil, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtMTokenCapacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMTokenRefil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(txtITokenCapacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(txtITokenRefil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtServedFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txtLog.setEditable(false);
        txtLog.setColumns(20);
        txtLog.setRows(5);
        pnlLog.setViewportView(txtLog);

        jButton2.setText("OK");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Cancel");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel6.setText("Server Log");

        chkActivateWebservice.setText("Activate Webservice");

        jLabel8.setText("Options");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlLog)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel8)
                                    .addComponent(chkActivateWebservice))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkActivateWebservice)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlLog, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        saveAndExit();
    }//GEN-LAST:event_jButton2ActionPerformed

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void saveAllValues() {
        // does notthing here
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // does nothing here
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        // does nothing here
    }

    @Override
    public Component getWindow() {
        throw new UnsupportedOperationException("This does not run in windowed mode.");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkActivateWebservice;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane pnlLog;
    private javax.swing.JTextField txtITokenCapacity;
    private javax.swing.JTextField txtITokenRefil;
    private javax.swing.JTextArea txtLog;
    private javax.swing.JTextField txtMTokenCapacity;
    private javax.swing.JTextField txtMTokenRefil;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtServedFolder;
    // End of variables declaration//GEN-END:variables
}
