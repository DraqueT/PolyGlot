/*
 * Copyright (c) 2018-2019, Draque Thompson
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
package org.darisadesigns.polyglotlina.CustomControls;

import org.darisadesigns.polyglotlina.Nodes.ToDoNode;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author DThompson
 */
public final class PToDoTree extends JTree {
    private final PToDoTree selfPointer = this;
    private HashMap<TreePath, ToDoNode> nodesCheckingState;
    private AbstractCollection<TreePath> checkedPaths = new HashSet<>();
    private TreePath clickedPath;
    private final boolean nightMode;
    private final double fontSize;

    public PToDoTree(boolean _nightMode, double _fontSize) {
        super();
        nightMode = _nightMode;
        fontSize = _fontSize;
        
        // Disabling toggling by double-click
        this.setToggleClickCount(0);
        // Overriding cell renderer by new one defined above
        this.setCellRenderer(new CheckBoxCellRenderer());

        // Overriding selection model by an empty one
        DefaultTreeSelectionModel dtsm = new DefaultTreeSelectionModel() {      
            // Totally disabling the selection mechanism
            @Override
            public void setSelectionPath(TreePath path) {
            }           
            @Override
            public void addSelectionPath(TreePath path) {                       
            }           
            @Override
            public void removeSelectionPath(TreePath path) {
            }
            @Override
            public void setSelectionPaths(TreePath[] pPaths) {
            }
        };
        setupMenusAndListeners();
        this.setSelectionModel(dtsm);
    }
    
    private void setupMenusAndListeners() {
        final JPopupMenu ruleMenu = new JPopupMenu();
        final JMenuItem addItem = new JMenuItem("Add Todo Node");
        final JMenuItem deleteItem = new JMenuItem("Delete Todo Node");
        final JMenuItem renameItem = new JMenuItem("Rename Todo Node");
        
        addItem.setToolTipText("Add new node beneath selected node");
        deleteItem.setToolTipText("Delete selected node");
        renameItem.setToolTipText("Rename selected node");
        ruleMenu.add(addItem);
        ruleMenu.add(deleteItem);
        ruleMenu.add(renameItem);
        
        addItem.addActionListener((ActionEvent ae) -> {
            ToDoTreeNode addNode = clickedPath == null ? 
                    (ToDoTreeNode)this.getModel().getRoot()
                    : (ToDoTreeNode)clickedPath.getLastPathComponent();
            
            String toDoLabel = InfoBox.stringInputDialog("ToDo Label", "Create ToDo Label", null);
            
            if (toDoLabel != null && !toDoLabel.isEmpty()) {
                ToDoTreeNode childNode = ToDoTreeNode.createToDoTreeNode(new ToDoNode(null, toDoLabel, false));
                addNode.add(childNode);
                ((DefaultTreeModel)this.getModel()).nodeStructureChanged(addNode);
                
                // forces update to make horizontal size of label match length of text
                SwingUtilities.invokeLater(() -> {
                    ((DefaultTreeModel)this.getModel()).nodeStructureChanged(childNode);
                });
                
                if (clickedPath != null) {
                    expandPath(clickedPath);
                }

                resetCheckingState();
            }
        });
        
        deleteItem.addActionListener((ActionEvent ae) -> {
            if (clickedPath != null) {
                ((DefaultTreeModel)getModel()).removeNodeFromParent((ToDoTreeNode)clickedPath.getLastPathComponent());
            }
        });
        
        renameItem.addActionListener((ActionEvent ae) -> {
            if (clickedPath != null) {
                String toDoLabel = InfoBox.stringInputDialog("New Todo Label", 
                                "What would you like the new label to be?", 
                                null);
                
                if (toDoLabel != null && !toDoLabel.isEmpty()) {
                    ToDoTreeNode clickedNode = (ToDoTreeNode)clickedPath.getLastPathComponent();
                    ((ToDoNode)clickedNode.getUserObject()).setValue(toDoLabel);
                    ((DefaultTreeModel)this.getModel()).nodeStructureChanged(clickedNode);
                }
            }
        });
        
        // Calling checking mechanism on mouse click
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent me) {
                clickedPath = selfPointer.getPathForLocation(me.getX(), me.getY());
                if (SwingUtilities.isRightMouseButton(me)) {
                    deleteItem.setEnabled(clickedPath != null);
                    renameItem.setEnabled(clickedPath != null);
                    ruleMenu.show(me.getComponent(), me.getX(), me.getY());
                } else {
                    if (clickedPath == null) {
                        return;
                    }
                    boolean checkMode = ! nodesCheckingState.get(clickedPath).isDone();
                    checkSubTree(clickedPath, checkMode);
                    updatePredecessorsWithCheckMode(clickedPath, checkMode);
                    // Firing the check change event
                    fireCheckChangeEvent(new CheckChangeEvent(new Object()));
                    // Repainting tree after the data structures were updated
                    selfPointer.repaint();
                }
            }           
            @Override
            public void mouseEntered(MouseEvent arg0) {         
            }           
            @Override
            public void mouseExited(MouseEvent arg0) {              
            }
            @Override
            public void mousePressed(MouseEvent arg0) {             
            }
            @Override
            public void mouseReleased(MouseEvent arg0) {
            }           
        });
    }

    public static class CheckChangeEvent extends EventObject {
        public CheckChangeEvent(Object _source) {
            super(_source);
        }       
    }

    public static TreePath getTreePath(TreeNode treeNode) {
    List<Object> nodes = new ArrayList<>();
    if (treeNode != null) {
      nodes.add(treeNode);
      treeNode = treeNode.getParent();
      while (treeNode != null) {
        nodes.add(0, treeNode);
        treeNode = treeNode.getParent();
      }
    }

    return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
  }
    
    public interface CheckChangeEventListener extends EventListener {
        void checkStateChanged(CheckChangeEvent event);
    }

    public void addCheckChangeEventListener(CheckChangeEventListener listener) {
        listenerList.add(CheckChangeEventListener.class, listener);
    }
    public void removeCheckChangeEventListener(CheckChangeEventListener listener) {
        listenerList.remove(CheckChangeEventListener.class, listener);
    }

    void fireCheckChangeEvent(CheckChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == CheckChangeEventListener.class) {
                ((CheckChangeEventListener) listeners[i + 1]).checkStateChanged(evt);
            }
        }
    }

    @Override
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
        resetCheckingState();
    }

    // New method that returns only the checked paths (totally ignores original "selection" mechanism)
    public TreePath[] getCheckedPaths() {
        return checkedPaths.toArray(new TreePath[checkedPaths.size()]);
    }

    // Returns true in case that the node is selected, has children but not all of them are selected
    public boolean isSelectedPartially(TreePath path) {
        ToDoNode cn = nodesCheckingState.get(path);
        return cn.isDone() && cn.hasChildren() && !cn.allChildrenDone();
    }

    public void resetCheckingState() { 
        nodesCheckingState = new HashMap<>();
        checkedPaths = new HashSet<>();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)getModel().getRoot();
        if (node == null) {
            return;
        }
        addSubtreeToCheckingStateTracking(node);
    }

    // Creating data structure of the current model for the checking mechanism
    private void addSubtreeToCheckingStateTracking(DefaultMutableTreeNode node) {
            if (node instanceof ToDoTreeNode) {
            TreeNode[] path = node.getPath();   
            TreePath tp = new TreePath(path);
            ToDoNode cn = ((ToDoTreeNode)node).getNode();
            nodesCheckingState.put(tp, cn);
            for (int i = 0 ; i < node.getChildCount() ; i++) {              
                addSubtreeToCheckingStateTracking(
                        (DefaultMutableTreeNode) tp.pathByAddingChild(node.getChildAt(i)).getLastPathComponent());
            }
        }
    }

    // Overriding cell renderer by a class that ignores the original "selection" mechanism
    // It decides how to show the nodes due to the checking-mechanism
    private final class CheckBoxCellRenderer extends JPanel implements TreeCellRenderer {    
        private final PCheckBox checkBox;
        
        CheckBoxCellRenderer() {
            super();      

            this.setLayout(new BorderLayout());
            checkBox = new PCheckBox(nightMode, fontSize);
            add(checkBox, BorderLayout.CENTER);
            setOpaque(false);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            Object obj = node.getUserObject();          
            TreePath tp = new TreePath(node.getPath());
            ToDoNode cn = nodesCheckingState.get(tp);
            if (cn == null) {
                return this;
            }
            
            checkBox.setSelected(cn.isDone() && cn.allChildrenDone());
            checkBox.setText(obj.toString());
            checkBox.setOpaque(cn.isDone() && cn.hasChildren() && !cn.allChildrenDone());
            
            return this;
        }
    }

    // When a node is checked/unchecked, updating the states of the predecessors
    private void updatePredecessorsWithCheckMode(TreePath tp, boolean check) {
        TreePath parentPath = tp.getParentPath();
        // If it is the root, stop the recursive calls and return
        if (parentPath == null) {
            return;
        }       
        ToDoNode parentCheckedNode = nodesCheckingState.get(parentPath);  
        parentCheckedNode.setDone(false);
        if (parentCheckedNode.getChildren().length != 0) {
            if (parentCheckedNode.allChildrenDone()) {
                parentCheckedNode.setDone(true);
            } else {
                parentCheckedNode.setDone(false);
            }
        }
        if (parentCheckedNode.isDone()) {
            checkedPaths.add(parentPath);
        } else {
            checkedPaths.remove(parentPath);
        }
        // Go to upper predecessor
        updatePredecessorsWithCheckMode(parentPath, check);
    }

    // Recursively checks/unchecks a subtree
    private void checkSubTree(TreePath tp, boolean check) {
        ToDoNode cn = nodesCheckingState.get(tp);
        cn.setDone(check);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
        for (int i = 0 ; i < node.getChildCount() ; i++) {              
            checkSubTree(tp.pathByAddingChild(node.getChildAt(i)), check);
        }
        
        if (check) {
            checkedPaths.add(tp);
        } else {
            checkedPaths.remove(tp);
        }
    }
}
