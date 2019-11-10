/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Screens;

import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.CustomControls.FamTreeNode;
import org.darisadesigns.polyglotlina.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.CustomControls.PTree;
import org.darisadesigns.polyglotlina.IOHandler;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.darisadesigns.polyglotlina.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.CustomControls.PTextPane;

/**
 *
 * @author draque
 */
public final class ScrFamilies extends PFrame {

    private boolean curUpdating = false;
    private final ScrMainMenu parent;

    /**
     * Creates new form ScrFamilies
     *
     * @param _core dictionary core
     * @param _parent parent window
     */
    public ScrFamilies(DictCore _core, ScrMainMenu _parent) {
        super(_core);
        
        parent = _parent;
        initComponents();
        setupComponents();

        setupListeners();

        if (System.getProperty("os.name").startsWith("Mac")) {
            btnAddFamily.setToolTipText(btnAddFamily.getToolTipText() + " (⌘ +)");
            btnDelFamily.setToolTipText(btnDelFamily.getToolTipText() + " (⌘ -)");
        } else {
            btnAddFamily.setToolTipText(btnAddFamily.getToolTipText() + " (CTRL +)");
            btnDelFamily.setToolTipText(btnDelFamily.getToolTipText() + " (CTRL -)");
        }
        
        addBindingsToPanelComponents(this.getRootPane());
    }
    
    @Override
    public void saveAllValues() {
        // not needed in this object. Items save inherently.
    }
    
    /**
     * Sets up all screen components. Can be run more than once if core is 
     * replaced.
     */
    private void setupComponents() {
        FamTreeNode root = new FamTreeNode();
        root.setAsRootNode(core.getFamManager().getRoot());
        TreeModel newModel = new DefaultTreeModel(root);
        treFam.setModel(newModel);
        treFam.setRootVisible(false);
        lstWords.setModel(new DefaultListModel<Object>());
        lstWords.setFont(core.getPropertiesManager().getFontCon());
    }
    
    @Override
    public void updateAllValues(DictCore _core) {
        if (core != _core) {
            core = _core;
            updateFamNotes();
            setupComponents();
        }
    }
    
    @Override
    public void addBindingToComponent(JComponent c) {
        Action addAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFamNode();
            }
        };
        Action delAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeFamNode();
            }
        };
        String addKey = "addNode";
        String delKey = "delNode";
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
     * adds family node
     */
    private void addFamNode() {
        FamTreeNode curNode = (FamTreeNode) treFam.getLastSelectedPathComponent();

        // select root if no user selection
        if (curNode == null) {
            curNode = (FamTreeNode) treFam.getModel().getRoot();
        }

        FamTreeNode newNode = new FamTreeNode(curNode.getNode());
        newNode.setUserObject("");

        // set ID = 1 to indicate new node
        newNode.getNode().setId(1);

        ((DefaultTreeModel) treFam.getModel()).insertNodeInto(newNode, curNode, 0);
        treFam.setSelectionPath(new TreePath(newNode.getPath()));
        treFam.expandRow(treFam.getSelectionRows()[0]);
    }

    /**
     * deletes currently selected node
     */
    private void removeFamNode() {
        FamTreeNode curNode = (FamTreeNode) treFam.getLastSelectedPathComponent();
        int position = treFam.getLeadSelectionRow();

        // the root node may not be deleted/do nothing if nothing selected
        if (position < 1
                || curNode == null) {
            return;
        }

        if (!InfoBox.deletionConfirmation(core.getRootWindow())) {
            return;
        }

        // only remove if not root node
        if (!curNode.isRoot()) {
            ((DefaultTreeModel) treFam.getModel()).removeNodeFromParent(curNode);
            curNode.removeFromParent();
        }

        treFam.setSelectionRow(position - 1);
    }

    /**
     * Adds word currently selected in lexicon to currently selected family
     */
    private void addWord() {
        ConWord curWord = parent.getCurrentWord();

        if (curWord != null) {
            ((FamTreeNode) treFam.getLastSelectedPathComponent())
                    .getNode().addWord(curWord);

            updateWordsProp();
        }
    }

    /**
     * removes currently selected word (in family window) from currently
     * selected family
     */
    private void removeWord() {
        if (chkInclSubFam.isSelected()) {
            InfoBox.info("Alert", "Words may only be removed when \"Include Subfamilies\" box is unchecked.", this);
            return;
        }

        ConWord curWord = (ConWord) lstWords.getSelectedValue();
        FamTreeNode curNode = (FamTreeNode) treFam.getLastSelectedPathComponent();

        if (curWord == null
                || curNode == null) {
            return;
        }

        curNode.getNode().removeWord(curWord);

        updateWordsProp();
    }

    /**
     * Updates family name from text box
     */
    private void updateFamName() {
        curUpdating = true;

        FamTreeNode curNode = (FamTreeNode) treFam.getLastSelectedPathComponent();

        if (curNode != null) {
            curNode.setUserObject(txtFamName.getText());
        }

        ((DefaultTreeModel) treFam.getModel()).nodeChanged(curNode);

        curUpdating = false;
    }

    /**
     * Updates family notes from text box
     */
    private void updateFamNotes() {
        curUpdating = true;

        FamTreeNode curNode = (FamTreeNode) treFam.getLastSelectedPathComponent();

        if (curNode != null) {
            curNode.getNode().setNotes(txtNotes.getText());
        }

        curUpdating = false;
    }

    /**
     * updates all properties of family
     */
    private void updateAllProps() {
        updateNameProp();
        updateNotesProp();
        updateWordsProp();
    }

    /**
     * updates family name property from currently selected node
     */
    private void updateNameProp() {
        curUpdating = true;

        FamTreeNode curNode = (FamTreeNode) treFam.getLastSelectedPathComponent();

        if (curNode != null) {
            txtFamName.setText(curNode.getNode().getValue());
        }

        curUpdating = false;
    }

    /**
     * updates family notes property from currently selected node
     */
    private void updateNotesProp() {
        curUpdating = true;

        FamTreeNode curNode = (FamTreeNode) treFam.getLastSelectedPathComponent();

        if (curNode != null) {
            txtNotes.setText(curNode.getNode().getNotes());
        }

        curUpdating = false;
    }

    /**
     * updates words list for family
     */
    private void updateWordsProp() {
        FamTreeNode curNode = (FamTreeNode) treFam.getLastSelectedPathComponent();

        DefaultListModel<Object> model = (DefaultListModel<Object>) lstWords.getModel();

        model.clear();

        if (curNode == null) {
            return;
        }

        Iterator<ConWord> wordIt;

        if (chkInclSubFam.isSelected()) {
            wordIt = curNode.getNode().getWordsIncludeSubs().iterator();
        } else {
            wordIt = curNode.getNode().getWords();
        }

        for (int i = 0; wordIt.hasNext(); i++) {
            model.add(i, wordIt.next());
        }
    }

    /**
     * cause lexicon to select currently selected word
     */
    private void updateLexSelection() {
        ConWord curWord = (ConWord) lstWords.getSelectedValue();

        if (curWord == null) {
            return;
        }

        parent.selectWordById(curWord.getId());
    }

    /**
     * sets up object listeners
     */
    private void setupListeners() {
        txtFamName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!curUpdating) {
                    updateFamName();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!curUpdating) {
                    updateFamName();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!curUpdating) {
                    updateFamName();
                }
            }
        });

        txtNotes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!curUpdating) {
                    updateFamNotes();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!curUpdating) {
                    updateFamNotes();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!curUpdating) {
                    updateFamNotes();
                }
            }
        });

        treFam.addTreeSelectionListener((TreeSelectionEvent e) -> {
            if (treFam.getLastSelectedPathComponent() != null) {
                updateAllProps();
                setEnabledMenuItems();
            }
        });
        
        treFam.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                TreePath selPath = treFam.getPathForLocation(e.getX(), e.getY());
                
                if (selPath == null) {
                    treFam.setSelectionPath(null);
                }
            }
        });
    }
    
    private void setEnabledMenuItems() {
        boolean enable = treFam.getLastSelectedPathComponent() != null;
        
        chkInclSubFam.setEnabled(enable);
        txtFamName.setEnabled(enable);
        txtNotes.setEnabled(enable);
        btnAddWord.setEnabled(enable);
        btnDelWord.setEnabled(enable && lstWords.getSelectedValue() != null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        treFam = new PTree(core);
        jPanel1 = new javax.swing.JPanel();
        chkInclSubFam = new PCheckBox(nightMode, menuFontSize);
        jScrollPane3 = new javax.swing.JScrollPane();
        lstWords = new javax.swing.JList<>();
        jLabel1 = new PLabel("", core);
        btnAddWord = new PAddRemoveButton("+");
        btnDelWord = new PAddRemoveButton("-");
        txtFamName = new PTextField(core, true, "-- Name --");
        jScrollPane2 = new javax.swing.JScrollPane();
        txtNotes = new PTextPane(core, true, "-- Notes --");
        btnAddFamily = new PAddRemoveButton("+");
        btnDelFamily = new PAddRemoveButton("-");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Lexical Families");

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        treFam.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(treFam);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        chkInclSubFam.setText("Include Subfamilies");
        chkInclSubFam.setToolTipText("Include all words from subfamilies in list disables remove (-) button");
        chkInclSubFam.setEnabled(false);
        chkInclSubFam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkInclSubFamActionPerformed(evt);
            }
        });

        lstWords.setToolTipText("Words in family");
        lstWords.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstWordsValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(lstWords);

        jLabel1.setText("Words in family");

        btnAddWord.setToolTipText("Add word selected in lexicon to family");
        btnAddWord.setEnabled(false);
        btnAddWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddWordActionPerformed(evt);
            }
        });

        btnDelWord.setToolTipText("Remove word selected in list from family");
        btnDelWord.setEnabled(false);
        btnDelWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelWordActionPerformed(evt);
            }
        });

        txtFamName.setToolTipText("A family's name");
        txtFamName.setEnabled(false);

        txtNotes.setEnabled(false);
        jScrollPane2.setViewportView(txtNotes);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAddWord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDelWord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(chkInclSubFam)
                        .addGap(0, 21, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(txtFamName)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(chkInclSubFam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFamName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddWord)
                    .addComponent(btnDelWord))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnAddFamily.setToolTipText("Add Family Node");
        btnAddFamily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFamilyActionPerformed(evt);
            }
        });

        btnDelFamily.setToolTipText("Remove family node");
        btnDelFamily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelFamilyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAddFamily, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDelFamily, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddFamily)
                            .addComponent(btnDelFamily))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddFamilyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFamilyActionPerformed
        addFamNode();
    }//GEN-LAST:event_btnAddFamilyActionPerformed

    private void btnDelFamilyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelFamilyActionPerformed
        removeFamNode();
    }//GEN-LAST:event_btnDelFamilyActionPerformed

    private void btnAddWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddWordActionPerformed
        addWord();
    }//GEN-LAST:event_btnAddWordActionPerformed

    private void btnDelWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelWordActionPerformed
        removeWord();
    }//GEN-LAST:event_btnDelWordActionPerformed

    private void lstWordsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstWordsValueChanged
        if (!curUpdating) {
            updateLexSelection();
            setEnabledMenuItems();
        }
    }//GEN-LAST:event_lstWordsValueChanged

    private void chkInclSubFamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkInclSubFamActionPerformed
        if (!curUpdating) {
            updateWordsProp();
        }
    }//GEN-LAST:event_chkInclSubFamActionPerformed

    /**
     * Opens families window and returns instance of self
     *
     * @param _core the dictionary core
     * @param parent the parent calling window
     * @return an instantiated copy of itself
     */
    public static ScrFamilies run(final DictCore _core, ScrMainMenu parent) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Window Error", "Unable to open families: " + e.getLocalizedMessage(), _core.getRootWindow());
        }

        // set the leaf icon to be a folder, since all nodes are for containing words
        UIManager.put("Tree.leafIcon", UIManager.get("Tree.closedIcon"));

        final ScrFamilies s = new ScrFamilies(_core, parent);

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            s.setVisible(true);
        });

        return s;
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
    private javax.swing.JButton btnAddFamily;
    private javax.swing.JButton btnAddWord;
    private javax.swing.JButton btnDelFamily;
    private javax.swing.JButton btnDelWord;
    private javax.swing.JCheckBox chkInclSubFam;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList<Object> lstWords;
    private javax.swing.JTree treFam;
    private javax.swing.JTextField txtFamName;
    private javax.swing.JTextPane txtNotes;
    // End of variables declaration//GEN-END:variables
}
