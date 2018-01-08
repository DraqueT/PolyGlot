/*
 * Copyright (c) 2014-2017, Draque Thompson, draquemail@gmail.com
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

import PolyGlot.Nodes.ConWord;
import PolyGlot.DictCore;
import PolyGlot.CustomControls.InfoBox;
import PolyGlot.Nodes.LogoNode;
import PolyGlot.CustomControls.PButton;
import PolyGlot.CustomControls.PFrame;
import PolyGlot.CustomControls.PList;
import PolyGlot.PGTUtil.WindowMode;
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
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 *
 * @author draque
 */
public class ScrLogoQuickView extends PFrame {
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
        super.setupKeyStrokes();
        initComponents();
        super.getRootPane().getContentPane().setBackground(Color.white);

        core = _core;
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

        mode = WindowMode.SELECTLIST;
    }
    
    /**
     * Opens window populated with all logographs for a word
     *
     * @param _core dictionary core
     * @param _conWord conword to open logographs for
     */
    public ScrLogoQuickView(DictCore _core, ConWord _conWord) {
        initComponents();
        super.getRootPane().getContentPane().setBackground(Color.white);
        
        conWord = _conWord;
        core = _core;

        setupFonts();
        setupResize();

        setTitle("Associated Logographs");
        populateLogos(core.getLogoCollection().getWordLogos(conWord));
        mode = WindowMode.SELECTLIST;
        
        if (System.getProperty("os.name").startsWith("Mac")) {
            btnAdd.setToolTipText(btnAdd.getToolTipText() + " (⌘ +)");
            btnDel.setToolTipText(btnDel.getToolTipText() + " (⌘ -)");
        } else {
            btnAdd.setToolTipText(btnAdd.getToolTipText() + " (CTRL +)");
            btnDel.setToolTipText(btnDel.getToolTipText() + " (CTRL -)");
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
    public boolean thisOrChildrenFocused() {
        boolean ret = this.isFocusOwner();
        ret = ret || (logoFinder != null && logoFinder.thisOrChildrenFocused());
        return ret;
    }
    
    @Override
    public final void setTitle(String _title) {
        super.setTitle(_title);
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
    
    @Override
    protected void setupKeyStrokes() {
        addBindingsToPanelComponents(this.getRootPane());
        super.setupKeyStrokes();
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
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), addKey);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), delKey);
        ActionMap am = c.getActionMap();
        am.put(addKey, addAction);
        am.put(delKey, delAction);
    }

    /**
     * Sets up fonts based on core properties
     */
    private void setupFonts() {
        Font font = core.getPropertiesManager().getFontCon();

        if (font == null) {
            return;
        }

        lstLogos.setFont(font);
    }

    private void populateLogos(List<LogoNode> logos) {
        Iterator<LogoNode> it = logos.iterator();

        DefaultListModel newModel = new DefaultListModel();

        while (it.hasNext()) {
            newModel.addElement(it.next());
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
                BufferedImage imgLogo = lstLogos.getSelectedValue() == null
                        ? null
                        : ((LogoNode) lstLogos.getSelectedValue()).getLogoGraph();

                if (imgLogo != null) {
                    lblLogoPic.setIcon(new ImageIcon(imgLogo.getScaledInstance(lblLogoPic.getWidth(), lblLogoPic.getHeight(), Image.SCALE_SMOOTH)));
                    lblLogoPic.setMinimumSize(new Dimension(1, 1));
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
        return (LogoNode) lstLogos.getSelectedValue();
    }
    
    private void addLogo() {
        if (logoFinder != null && !logoFinder.isDisposed()
                && logoFinder.getMode() == WindowMode.SINGLEVALUE) {
            InfoBox.info("Action currently unavailable.",
                    "Please close Logograph Details/Modification window before adding logographs.",
                    core.getRootWindow());

            return;
        }

        if (logoFinder == null || logoFinder.isDisposed()) {
            logoFinder = new ScrLogoDetails(core);
            logoFinder.setWordFetchMode(true);
            logoFinder.setBeside(this);

            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    logoFinder.setVisible(true);
                }
            });
        } else {
            LogoNode addLogo = logoFinder.getSelectedLogo();
            
            if (core.getLogoCollection().addWordLogoRelation(conWord, addLogo)) {
                ((DefaultListModel) lstLogos.getModel()).addElement(addLogo);
                logoFinder.refreshRelatedWords();
            }
        }
    }
    
    private void delLogo() {
        LogoNode curNode = (LogoNode) lstLogos.getSelectedValue();

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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblLogoPic = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstLogos = new PList(core, true);
        btnAdd = new PolyGlot.CustomControls.PAddRemoveButton("+");
        btnDel = new PolyGlot.CustomControls.PAddRemoveButton("-");
        jButton1 = new PButton(core);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblLogoPic.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lstLogos.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
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

        jButton1.setText("OK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
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
                        .addComponent(jButton1)))
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
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING)))
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

        LogoNode curNode = (LogoNode) lstLogos.getSelectedValue();

        if (curNode == null) {
            return;
        }

        lblLogoPic.setIcon(new ImageIcon(curNode.getLogoGraph().getScaledInstance(
                lblLogoPic.getWidth() - 4, lblLogoPic.getHeight() - 4, Image.SCALE_SMOOTH)));
    }//GEN-LAST:event_lstLogosValueChanged

    private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelActionPerformed
        delLogo();
    }//GEN-LAST:event_btnDelActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (logoParent != null) {
            logoParent.addRadFromQuickview();
        }
        
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

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
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblLogoPic;
    private javax.swing.JList lstLogos;
    // End of variables declaration//GEN-END:variables
}
