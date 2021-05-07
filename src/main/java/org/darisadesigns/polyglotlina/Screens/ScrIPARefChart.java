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

import org.darisadesigns.polyglotlina.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ClipboardHandler;
import org.darisadesigns.polyglotlina.CustomControls.PComboBox;
import org.darisadesigns.polyglotlina.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.IPAHandler;
import org.darisadesigns.polyglotlina.IPAHandler.IPALibrary;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.JComponent;
import org.darisadesigns.polyglotlina.Desktop.PropertiesManager;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.PolyGlot;

/**
 *
 * @author draque
 */
public final class ScrIPARefChart extends PFrame {
    private final IPAHandler handler;
    private final Map<String, String[]> ipaToChars;
    
    /**
     * Creates new form ScrIPARefChart
     * @param _core
     */
    public ScrIPARefChart(DictCore _core) {
        super(_core);
        
        initComponents();
        handler = new IPAHandler(this);
        ipaToChars = core.getPronunciationMgr().getCharactersPerIpaSound();
        this.setSize(849, 595);
        setupToolTips();
    }
    
    /**
     * Sets up tooltips for IPA label displays
     */
    private void setupToolTips() {
        lblVowels.setToolTipText(" ");
        ((PLabel)lblVowels).setToolTipOverrideFont(((PropertiesManager)core.getPropertiesManager()).getFontCon());
        ((PLabel)lblVowels).setToolTipAction((e) -> {
            String ret = "";
            
            if (e instanceof MouseEvent) {
                MouseEvent event = (MouseEvent)e;
                String ipa = handler.getVowelChar(event.getX(), event.getY());
                
                if (ipaToChars.containsKey(ipa)) {
                    for (String curChar : ipaToChars.get(ipa)) {
                        ret += curChar + " ";
                    }
                }
            }
                    
            return ret;
        });
        
        lblNonPulmonicConsonants.setToolTipText(" ");
        ((PLabel)lblNonPulmonicConsonants).setToolTipOverrideFont(((PropertiesManager)core.getPropertiesManager()).getFontCon());
        ((PLabel)lblNonPulmonicConsonants).setToolTipAction((e) -> {
            String ret = "";
            
            if (e instanceof MouseEvent) {
                MouseEvent event = (MouseEvent)e;
                String ipa = handler.getNonPulConsChar(event.getX(), event.getY());
                
                if (ipaToChars.containsKey(ipa)) {
                    for (String curChar : ipaToChars.get(ipa)) {
                        ret += curChar + " ";
                    }
                }
            }
                    
            return ret;
        });
        
        lblOtherSymbols.setToolTipText(" ");
        ((PLabel)lblOtherSymbols).setToolTipOverrideFont(((PropertiesManager)core.getPropertiesManager()).getFontCon());
        ((PLabel)lblOtherSymbols).setToolTipAction((e) -> {
            String ret = "";
            
            if (e instanceof MouseEvent) {
                MouseEvent event = (MouseEvent)e;
                String ipa = handler.getOtherChar(event.getX(), event.getY());
                
                if (ipaToChars.containsKey(ipa)) {
                    for (String curChar : ipaToChars.get(ipa)) {
                        ret += curChar + " ";
                    }
                }
            }
                    
            return ret;
        });
        
        lblPulmonicConsonants.setToolTipText(" ");
        ((PLabel)lblPulmonicConsonants).setToolTipOverrideFont(((PropertiesManager)core.getPropertiesManager()).getFontCon());
        ((PLabel)lblPulmonicConsonants).setToolTipAction((e) -> {
            String ret = "";
            
            if (e instanceof MouseEvent) {
                MouseEvent event = (MouseEvent)e;
                String ipa = handler.getPulConsChar(event.getX(), event.getY());
                
                if (ipaToChars.containsKey(ipa)) {
                    for (String curChar : ipaToChars.get(ipa)) {
                        ret += curChar + " ";
                    }
                }
            }
                    
            return ret;
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        lblVowels = new PLabel("", 0);
        jPanel2 = new javax.swing.JPanel();
        lblPulmonicConsonants = new PLabel("",0);
        jPanel3 = new javax.swing.JPanel();
        lblNonPulmonicConsonants = new PLabel("",0);
        jPanel4 = new javax.swing.JPanel();
        lblOtherSymbols = new PLabel("",0);
        txtIPAChars = new javax.swing.JTextField();
        cmbIpaLibSelect = new PComboBox<IPALibrary>(((PropertiesManager)core.getPropertiesManager()).getFontMenu());
        lblHover = new PLabel("Hover over an IPA character to display which characters in your language express it.", PolyGlot.getPolyGlot().getOptionsManager().getMenuFontSize());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("IPA Pronunciation/Character Guide");
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));
        jTabbedPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTabbedPane1.setMaximumSize(new java.awt.Dimension(301, 455));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMaximumSize(new java.awt.Dimension(301, 455));

        lblVowels.setIcon(new javax.swing.ImageIcon(getClass().getResource(PGTUtil.IPA_VOWEL_IMAGE)));
        lblVowels.setToolTipText("");
        lblVowels.setMaximumSize(new java.awt.Dimension(301, 455));
        lblVowels.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblVowelsMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(106, 106, 106)
                .addComponent(lblVowels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(714, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblVowels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Vowels", jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        lblPulmonicConsonants.setIcon(new javax.swing.ImageIcon(getClass().getResource(PGTUtil.IPA_PULMONIC_CONSONANT_IMAGE)));
        lblPulmonicConsonants.setToolTipText("");
        lblPulmonicConsonants.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblPulmonicConsonantsMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblPulmonicConsonants)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblPulmonicConsonants)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Pulmonic consonants", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        lblNonPulmonicConsonants.setIcon(new javax.swing.ImageIcon(getClass().getResource(PGTUtil.IPA_NON_PULMONIC_CONSONANTS_IMAGE)));
        lblNonPulmonicConsonants.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblNonPulmonicConsonantsMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(139, 139, 139)
                .addComponent(lblNonPulmonicConsonants)
                .addContainerGap(681, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblNonPulmonicConsonants)
                .addContainerGap(401, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Non-pulmonic consonants", jPanel3);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        lblOtherSymbols.setIcon(new javax.swing.ImageIcon(getClass().getResource(PGTUtil.IPA_OTHER_IMAGE)));
        lblOtherSymbols.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblOtherSymbolsMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(88, 88, 88)
                .addComponent(lblOtherSymbols)
                .addContainerGap(732, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblOtherSymbols)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Other/Affricates", jPanel4);

        txtIPAChars.setToolTipText("Symbols Copied Here");

        cmbIpaLibSelect.setModel(new javax.swing.DefaultComboBoxModel<IPALibrary>(new IPALibrary[] {IPALibrary.WIKI_IPA, IPALibrary.UCLA_IPA}));
        cmbIpaLibSelect.setSelectedIndex(0);
        cmbIpaLibSelect.setSelectedItem(IPALibrary.WIKI_IPA);

        lblHover.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtIPAChars)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblHover, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbIpaLibSelect, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblHover)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbIpaLibSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtIPAChars, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("Vowels");
        jTabbedPane1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblOtherSymbolsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblOtherSymbolsMouseClicked
        int x=evt.getX();
        int y=evt.getY();
        try {
            String ipaChar = handler.playOtherGetChar(x, y, (IPALibrary)cmbIpaLibSelect.getSelectedItem());
            new ClipboardHandler().setClipboardContents(ipaChar);
            String curText = txtIPAChars.getText();
            txtIPAChars.setText((curText.isEmpty() ? "" : curText + " ") + ipaChar);
        } catch(Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("IPA Error", e.getLocalizedMessage());
        }
    }//GEN-LAST:event_lblOtherSymbolsMouseClicked

    @Override
    public void saveAllValues() {
        // no values to save
    }
    
    private void lblVowelsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblVowelsMouseClicked
        int x = evt.getX();
        int y = evt.getY();
        try {
            String ipaChar = handler.playVowelGetChar(x, y, (IPALibrary)cmbIpaLibSelect.getSelectedItem());
            new ClipboardHandler().setClipboardContents(ipaChar);
            String text = txtIPAChars.getText();
            text = ipaChar.isEmpty() ? text : text + " " + ipaChar;
            txtIPAChars.setText(text);
        } catch(Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("IPA Error", e.getLocalizedMessage());
        }
    }//GEN-LAST:event_lblVowelsMouseClicked

    private void lblPulmonicConsonantsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblPulmonicConsonantsMouseClicked
        int x=evt.getX();
        int y=evt.getY();
        try {
            String ipaChar = handler.playPulConsGetChar(x, y, (IPALibrary)cmbIpaLibSelect.getSelectedItem());
            new ClipboardHandler().setClipboardContents(ipaChar);
            String curText = txtIPAChars.getText();
            txtIPAChars.setText((curText.isEmpty() || ipaChar.isEmpty() ? "" : curText + " ") + ipaChar);
        } catch(Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("IPA Error", e.getLocalizedMessage());
        }
    }//GEN-LAST:event_lblPulmonicConsonantsMouseClicked

    private void lblNonPulmonicConsonantsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblNonPulmonicConsonantsMouseClicked
        int x=evt.getX();
        int y=evt.getY();
        try {
            String ipaChar = handler.playNonPulConsGetChar(x, y, (IPALibrary)cmbIpaLibSelect.getSelectedItem());
            
            // empty string indicates invalid click location
            if (ipaChar.isEmpty()) {
                return;
            }
            
            new ClipboardHandler().setClipboardContents(ipaChar);
            String curText = txtIPAChars.getText();
            txtIPAChars.setText((curText.isEmpty() ? "" : curText + " ") + ipaChar);
        } catch(Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("IPA Error", e.getLocalizedMessage());
        }
    }//GEN-LAST:event_lblNonPulmonicConsonantsMouseClicked
    
    @Override
    public void updateAllValues(DictCore _core) {
        // values can't be updated in this window.
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        // nothing to bind in this window
    }
    
    @Override
    public Component getWindow() {
        return this.getRootPane();
    }
    
    @Override
    public boolean canClose() {
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<IPALibrary> cmbIpaLibSelect;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblHover;
    private javax.swing.JLabel lblNonPulmonicConsonants;
    private javax.swing.JLabel lblOtherSymbols;
    private javax.swing.JLabel lblPulmonicConsonants;
    private javax.swing.JLabel lblVowels;
    private javax.swing.JTextField txtIPAChars;
    // End of variables declaration//GEN-END:variables
}
