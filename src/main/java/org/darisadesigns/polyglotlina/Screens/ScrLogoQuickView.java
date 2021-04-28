/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.LogoNode;
import org.darisadesigns.polyglotlina.CustomControls.PButton;
import org.darisadesigns.polyglotlina.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.CustomControls.PList;
import org.darisadesigns.polyglotlina.PGTUtil.WindowMode;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.darisadesigns.polyglotlina.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.Desktop.PropertiesManager;
import org.darisadesigns.polyglotlina.PGTUtil;

/**
 *
 * @author draque
 */
public final class ScrLogoQuickView extends PFrame {
    private ScrLogoDetails logoFinder = null;
    private ConWord conWord = null;
    private ScrLogoDetails logoParent = null;

    /**
     * opens window to show all logographs, or all radicals
     *
     * @param _core dictionary core
     * @param showRadicalsOnly set true to limit to only radicals
     */
    public ScrLogoQuickView(DictCore _core, boolean showRadicalsOnly) {
        super(_core, WindowMode.SELECTLIST);
        
        addBindingsToPanelComponents(this.getRootPane());
        initComponents();
        super.getRootPane().getContentPane().setBackground(Color.white);

        setupFonts();
        setupResize();

        btnAdd.setVisible(!showRadicalsOnly);
        btnDel.setVisible(!showRadicalsOnly);

        if (showRadicalsOnly) {
            populateLogos(core.getLogoCollection().getRadicals());
            this.setTitle("Logographic Radicals");
        } else {
            populateLogos(core.getLogoCollection().getAllLogos());
        }
    }
    
    /**
     * Opens window populated with all logographs for a word
     *
     * @param _core dictionary core
     * @param _conWord conword to open logographs for
     */
    public ScrLogoQuickView(DictCore _core, ConWord _conWord) {
        super(_core, WindowMode.SELECTLIST);
        
        initComponents();
        super.getRootPane().getContentPane().setBackground(Color.white);
        
        conWord = _conWord;

        setupFonts();
        setupResize();

        setTitle("Associated Logographs");
        populateLogos(core.getLogoCollection().getWordLogos(conWord));
        
        if (System.getProperty("os.name").startsWith("Mac")) {
            btnAdd.setToolTipText("Add (Option +)");
            btnDel.setToolTipText("Remove (Option -)");
        } else {
            btnAdd.setToolTipText("Add (CTRL +)");
            btnDel.setToolTipText("Remove (CTRL -)");
        }
    }
    
    @Override
    public void dispose() {
        if (logoFinder != null 
                && !logoFinder.isDisposed()) {
            logoFinder.dispose();
        }
        super.dispose();
    }
    
    @Override
    public void saveAllValues() {
        // not needed here
    }
    
    @Override
    public void updateAllValues(DictCore _core) {
        if (core != _core) {
            // this window by its very nature is for single words. Close on load of new dictionary.
            dispose();
        }
        
        if (logoFinder != null
                && !logoFinder.isDisposed()) {
            logoFinder.updateAllValues(_core);
        }
    }
    
    public void setLogoParent(ScrLogoDetails _logoParent) {
        logoParent = _logoParent;
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        Action addAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addLogo();
            }
        };
        Action delAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delLogo();
            }
        };
        String addKey = "addLogo";
        String delKey = "delLogo";
        int mask;
        if (System.getProperty("os.name").startsWith("Mac")) {
            mask = KeyEvent.META_DOWN_MASK;
        } else {
            mask = KeyEvent.CTRL_DOWN_MASK;
        }
        InputMap im = c.getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | mask), addKey);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | mask), delKey);
        ActionMap am = c.getActionMap();
        am.put(addKey, addAction);
        am.put(delKey, delAction);
    }

    /**
     * Sets up fonts based on core properties
     */
    private void setupFonts() {
        Font font = ((PropertiesManager)core.getPropertiesManager()).getFontCon();

        if (font == null) {
            return;
        }

        lstLogos.setFont(font);
    }

    private void populateLogos(LogoNode[] logos) {

        DefaultListModel<LogoNode> newModel = new DefaultListModel<>();

        for (LogoNode curNode : logos) {
            newModel.addElement(curNode);
        }

        lstLogos.setModel(newModel);
        lstLogos.setSelectedIndex(0);
        lstLogos.ensureIndexIsVisible(0);
    }

    /**
     * sets up window resize stuff
     */
    private void setupResize() {
        // set resize instructions
        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                LogoNode selected = lstLogos.getSelectedValue();
                
                if (selected != null) {
                    try {
                        BufferedImage imgLogo = ImageIO.read(new ByteArrayInputStream(selected.getLogoBytes()));
                    lblLogoPic.setIcon(new ImageIcon(imgLogo.getScaledInstance(lblLogoPic.getWidth(), 
                            lblLogoPic.getHeight(), 
                            Image.SCALE_SMOOTH)));
                    lblLogoPic.setMinimumSize(new Dimension(1, 1));
                }
                    catch (IOException ex) {
                        // TODO: show error
                    }
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {/*do nothing*/

            }

            @Override
            public void componentShown(ComponentEvent e) {/*do nothing*/

            }

            @Override
            public void componentHidden(ComponentEvent e) {/*do nothing*/

            }
        });
    }

    /**
     * Returns currently selected LogoNode, null if none selected
     *
     * @return current LogoNode
     */
    public LogoNode getCurrentLogo() {
        return lstLogos.getSelectedValue();
    }
    
    private void addLogo() {
        if (logoFinder != null && !logoFinder.isDisposed()
                && logoFinder.getMode() == WindowMode.SINGLEVALUE) {
            core.getOSHandler().getInfoBox().info("Action currently unavailable.",
                    "Please close Logograph Details/Modification window before adding logographs.");

            return;
        }

        if (logoFinder == null || logoFinder.isDisposed()) {
            logoFinder = new ScrLogoDetails(core);
            logoFinder.setWordFetchMode(true);
            logoFinder.setBeside(this);

            java.awt.EventQueue.invokeLater(() -> {
                logoFinder.setVisible(true);
            });
        } else {
            LogoNode addLogo = logoFinder.getSelectedLogo();
            
            if (core.getLogoCollection().addWordLogoRelation(conWord, addLogo)) {
                ((DefaultListModel<LogoNode>) lstLogos.getModel()).addElement(addLogo);
                logoFinder.refreshRelatedWords();
            }
        }
    }
    
    private void delLogo() {
        LogoNode curNode = lstLogos.getSelectedValue();

        if (curNode == null) {
            return;
        }

        core.getLogoCollection().removeWordLogoRelation(conWord, curNode);
        populateLogos(core.getLogoCollection().getWordLogos(conWord));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblLogoPic = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstLogos = new PList(((PropertiesManager)core.getPropertiesManager()).getFontCon());
        btnAdd = new PAddRemoveButton("+");
        btnDel = new PAddRemoveButton("-");
        btnOK = new PButton(nightMode, menuFontSize);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblLogoPic.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lstLogos.setModel(new javax.swing.AbstractListModel<LogoNode>() {
            // set up this way as an artifact of netbeans being annoying...
            public int getSize() { return 1; }
            public LogoNode getElementAt(int i) { return new LogoNode(core); }
        });
        lstLogos.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstLogosValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstLogos);

        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnDel.setMaximumSize(new java.awt.Dimension(30, 29));
        btnDel.setMinimumSize(new java.awt.Dimension(30, 29));
        btnDel.setPreferredSize(new java.awt.Dimension(30, 29));
        btnDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelActionPerformed(evt);
            }
        });

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
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLogoPic, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnOK)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnAdd))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblLogoPic, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOK, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        addLogo();
    }//GEN-LAST:event_btnAddActionPerformed

    private void lstLogosValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstLogosValueChanged
        // prevent double firing
        if (evt.getValueIsAdjusting()) {
            return;
        }

        LogoNode curNode = lstLogos.getSelectedValue();

        if (curNode == null) {
            return;
        }

        ImageIcon icon = new ImageIcon(new LogoNode(core).getLogoBytes());
            icon.getImage().getScaledInstance(lblLogoPic.getWidth() - 4, lblLogoPic.getHeight() - 4, Image.SCALE_SMOOTH);
        lblLogoPic.setIcon(icon);
    }//GEN-LAST:event_lstLogosValueChanged

    private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelActionPerformed
        delLogo();
    }//GEN-LAST:event_btnDelActionPerformed

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        if (logoParent != null) {
            logoParent.addRadFromQuickview();
        }
        
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        dispose();
    }//GEN-LAST:event_btnOKActionPerformed

    /**
     * selects given logograph if it exists in the current list
     *
     * @param logoId
     */
    public void setSelectedLogo(int logoId) {
        DefaultListModel logoList = (DefaultListModel) lstLogos.getModel();

        for (int i = 0; i < logoList.getSize(); i++) {
            LogoNode curNode = (LogoNode) logoList.get(i);

            if (curNode.getId() == logoId) {
                lstLogos.setSelectedIndex(i);
                lstLogos.ensureIndexIsVisible(i);

                break;
            }
        }
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
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDel;
    private javax.swing.JButton btnOK;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblLogoPic;
    private javax.swing.JList<LogoNode> lstLogos;
    // End of variables declaration//GEN-END:variables
}
