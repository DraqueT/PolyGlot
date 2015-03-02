/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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
package PolyGlot;

import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 *
 * @author draque
 */
public class ScrThesaurus extends PFrame {

    private final DictCore core;
    private boolean curUpdating = false;
    private final ScrDictInterface parent;
    
    /**
     * Creates new form ScrThesaurus
     * @param _core dictionary core
     * @param _parent parent window
     */
    public ScrThesaurus(DictCore _core, ScrDictInterface _parent) {
        core = _core;
        parent = _parent;
        setupKeyStrokes();
        initComponents();
        ThesTreeNode root = new ThesTreeNode();
        root.setAsRootNode(core.getThesManager().getRoot());

        TreeModel newModel = new DefaultTreeModel(root);
        treThes.setModel(newModel);
        
        lstWords.setModel(new DefaultListModel());

        setupListeners();
        
        lstWords.setFont(core.getPropertiesManager().getFontCon());
    }

    /**
     * adds thesaurus family node
     */
    private void addThesNode() {
        ThesTreeNode curNode = (ThesTreeNode) treThes.getLastSelectedPathComponent();

        // don't continue of no node selected
        if (curNode == null) {
            return;
        }

        ThesTreeNode newNode = new ThesTreeNode(curNode.getNode());
        newNode.setUserObject("NEW FAMILY");
        
        // set ID = 1 to indicate new node
        newNode.getNode().setId(1);

        ((DefaultTreeModel) treThes.getModel()).insertNodeInto(newNode, curNode, 0);

        treThes.expandRow(treThes.getSelectionRows()[0]);
    }

    /**
     * deletes currently selected node
     */
    private void removeThesNode() {
        ThesTreeNode curNode = (ThesTreeNode) treThes.getLastSelectedPathComponent();
        int position = treThes.getLeadSelectionRow();
        
        // the root node may not be deleted/do nothing if nothing selected
        if (position < 1
                || curNode == null) {
            return;
        }
        
        if (!InfoBox.deletionConfirmation(this)) {
            return;
        }
                    
        // only remove if not root node
        if (!curNode.isRoot()) {
            ((DefaultTreeModel) treThes.getModel()).removeNodeFromParent(curNode);
            curNode.removeFromParent();
        }
        
        treThes.setSelectionRow(position - 1);
    }
    
    /**
     * Adds word currently selected in lexicon to currently selected family
     */
    private void addWord() {
        ConWord curWord = parent.getCurrentWord();
        
        if (curWord == null) {
            return;
        }
        
        ((ThesTreeNode)treThes.getLastSelectedPathComponent())
                .getNode().addWord(curWord);
        
        updateWordsProp();
    }
    
    /**
     * removes currently selected word (in thesaurus window) from currently selected family
     */
    private void removeWord() {
        if (chkInclSubFam.isSelected()) {
            InfoBox.info("Alert", "Words may only be removed when \"Include Subfamilies\" box is unchecked.", this);
            return;
        }
        
        ConWord curWord = (ConWord)lstWords.getSelectedValue();
        ThesTreeNode curNode  = (ThesTreeNode) treThes.getLastSelectedPathComponent();
        
        if (curWord == null
                || curNode == null) {
            return;
        }
        
        curNode.getNode().removeWord(curWord.getId());
        
        updateWordsProp();
    }

    /**
     * Updates family name from text box
     */
    private void updateThesName() {
        curUpdating = true;

        ThesTreeNode curNode = (ThesTreeNode) treThes.getLastSelectedPathComponent();

        if (curNode != null) {
            curNode.setUserObject(txtFamName.getText());
        }

        ((DefaultTreeModel) treThes.getModel()).nodeChanged(curNode);

        curUpdating = false;
    }

    /**
     * Updates family notes from text box
     */
    private void updateThesNotes() {
        curUpdating = true;

        ThesTreeNode curNode = (ThesTreeNode) treThes.getLastSelectedPathComponent();

        if (curNode != null) {
            curNode.getNode().setNotes(txtNotes.getText());
        }

        curUpdating = false;
    }

    /**
     * updates all properties of thesaurus family
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

        ThesTreeNode curNode = (ThesTreeNode) treThes.getLastSelectedPathComponent();

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

        ThesTreeNode curNode = (ThesTreeNode) treThes.getLastSelectedPathComponent();

        if (curNode != null) {
            txtNotes.setText(curNode.getNode().getNotes());
        }

        curUpdating = false;
    }
    
    /**
     * updates words list for family
     */
    private void updateWordsProp() {
        ThesTreeNode curNode = (ThesTreeNode) treThes.getLastSelectedPathComponent();
        
        DefaultListModel model = (DefaultListModel)lstWords.getModel();
        
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
        ConWord curWord = (ConWord)lstWords.getSelectedValue();
        
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
                    updateThesName();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!curUpdating) {
                    updateThesName();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!curUpdating) {
                    updateThesName();
                }
            }
        });

        txtNotes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!curUpdating) {
                    updateThesNotes();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!curUpdating) {
                    updateThesNotes();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!curUpdating) {
                    updateThesNotes();
                }
            }
        });

        treThes.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (treThes.getLastSelectedPathComponent() != null) updateAllProps();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        treThes = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtNotes = new javax.swing.JTextArea();
        chkInclSubFam = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstWords = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnAddWord = new javax.swing.JButton();
        btnDelWord = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtFamName = new javax.swing.JTextField();
        btnAddFamily = new javax.swing.JButton();
        btnDelFamily = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Thesaurus");

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        treThes.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(treThes);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        txtNotes.setColumns(20);
        txtNotes.setLineWrap(true);
        txtNotes.setRows(5);
        txtNotes.setWrapStyleWord(true);
        jScrollPane2.setViewportView(txtNotes);

        chkInclSubFam.setText("Include Subfamilies");
        chkInclSubFam.setToolTipText("Include all words from subfamilies in list disables remove (-) button");
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

        jLabel2.setText("Notes");
        jLabel2.setToolTipText("Notes on family");

        btnAddWord.setText("+");
        btnAddWord.setToolTipText("Add word selected in lexicon to family");
        btnAddWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddWordActionPerformed(evt);
            }
        });

        btnDelWord.setText("-");
        btnDelWord.setToolTipText("Remove word selected in list from family");
        btnDelWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelWordActionPerformed(evt);
            }
        });

        jLabel3.setText("Name");

        txtFamName.setToolTipText("A family's name");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(chkInclSubFam)
                .addGap(0, 29, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnAddWord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDelWord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFamName)))))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(chkInclSubFam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtFamName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddWord)
                    .addComponent(btnDelWord))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        btnAddFamily.setText("+");
        btnAddFamily.setToolTipText("Add Family Node");
        btnAddFamily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFamilyActionPerformed(evt);
            }
        });

        btnDelFamily.setText("-");
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
        addThesNode();
    }//GEN-LAST:event_btnAddFamilyActionPerformed

    private void btnDelFamilyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelFamilyActionPerformed
        removeThesNode();
    }//GEN-LAST:event_btnDelFamilyActionPerformed

    private void btnAddWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddWordActionPerformed
        addWord();
    }//GEN-LAST:event_btnAddWordActionPerformed

    private void btnDelWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelWordActionPerformed
        removeWord();
    }//GEN-LAST:event_btnDelWordActionPerformed

    private void lstWordsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstWordsValueChanged
        if (!curUpdating) updateLexSelection();
    }//GEN-LAST:event_lstWordsValueChanged

    private void chkInclSubFamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkInclSubFamActionPerformed
        if (!curUpdating) updateWordsProp();
    }//GEN-LAST:event_chkInclSubFamActionPerformed

    /**
     * Opens thesaurus window and returns instance of self
     * @param _core the dictionary core
     * @param parent the parent calling window
     * @return an instantiated copy of itself
     */
    public static ScrThesaurus run(final DictCore _core, ScrDictInterface parent) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ScrThesaurus.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ScrThesaurus.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ScrThesaurus.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ScrThesaurus.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        // set the leaf icon to be a folder, since all nodes are for containing words
        UIManager.put("Tree.leafIcon", UIManager.get("Tree.closedIcon"));

        final ScrThesaurus s = new ScrThesaurus(_core, parent);
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                s.setVisible(true);
            }
        });
        
        return s;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddFamily;
    private javax.swing.JButton btnAddWord;
    private javax.swing.JButton btnDelFamily;
    private javax.swing.JButton btnDelWord;
    private javax.swing.JCheckBox chkInclSubFam;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList lstWords;
    private javax.swing.JTree treThes;
    private javax.swing.JTextField txtFamName;
    private javax.swing.JTextArea txtNotes;
    // End of variables declaration//GEN-END:variables
}
