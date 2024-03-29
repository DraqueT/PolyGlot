/*
 * Copyright (c) 2018-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop.CustomControls;

import org.darisadesigns.polyglotlina.Nodes.ToDoNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 * @author DThompson
 */
public class PToDoTreeModel extends DefaultTreeModel {
    
    public PToDoTreeModel(TreeNode _root) {
        super(_root);
        
        populateTree((ToDoTreeNode)_root);
    }
    
    private void populateTree(ToDoTreeNode treeNode) {
        for(ToDoNode toDoNode : treeNode.getNode().getChildren()) {
            ToDoTreeNode child = ToDoTreeNode.createToDoTreeNode(toDoNode);
            treeNode.initialPopulateAdd(child);
            populateTree(child);
        }
    }
    
    @Override
    public void removeNodeFromParent(MutableTreeNode node) {
        if (node instanceof ToDoTreeNode) {
            ToDoTreeNode toNode = (ToDoTreeNode)node;
            Object userObject = toNode.getUserObject();
            if (userObject instanceof ToDoNode) {
                ToDoNode userNode = (ToDoNode) userObject;
                userNode.deleteFromParent();
            }
        }
        
        super.removeNodeFromParent(node);
    }
}
