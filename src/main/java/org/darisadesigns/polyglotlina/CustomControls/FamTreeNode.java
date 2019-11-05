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
package org.darisadesigns.polyglotlina.CustomControls;

import org.darisadesigns.polyglotlina.Nodes.FamNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * This overrides the typical tree node to link it directly to family nodes
 *
 * @author draque
 */
public class FamTreeNode extends DefaultMutableTreeNode {

    private FamNode famNode;

    /**
     * constructs any child FamTreeNode
     *
     * @param parent
     */
    public FamTreeNode(FamNode parent) {
        famNode = new FamNode(parent, parent.getManager());
    }

    /**
     * constructor allows for root creation without parent
     */
    public FamTreeNode() {
    }

    /**
     * Sets node to FamNode value and test to object's value
     *
     * @param _node node to set from
     */
    public void setNode(FamNode _node) {
        famNode = _node;
        setUserObject(_node.getValue());
    }

    /**
     * Sets passed node as root relative to position and populates from there
     *
     * @param _myRoot
     */
    public void setAsRootNode(FamNode _myRoot) {
        setNode(_myRoot);

        _myRoot.getNodes().forEach((curNode) -> {
            FamTreeNode treeNode = new FamTreeNode(_myRoot);
            treeNode.setAsRootNode(curNode);

            add(treeNode);
        });
    }

    public FamNode getNode() {
        return famNode;
    }

    @Override
    public void setUserObject(Object userObject) {
        if (userObject instanceof String) {
            famNode.setValue((String) userObject);
        }
        super.setUserObject(userObject);
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        // only modify node if new. ID = 0 indicates loading from existing tree
        if (((FamTreeNode) child).famNode.getId() == 1) {
            famNode.addNode(((FamTreeNode) child).famNode);
            
            // once read, this is no longer a new node
            ((FamTreeNode) child).famNode.setId(0);
        }

        super.insert(child, index);
    }

    @Override
    /**
     * This serves to clean up the internal data structure for PolyGlot. Must be
     * called each time a node is removed.
     */
    public void removeFromParent() {
        // if this is the root node, return
        famNode.removeFromParent();

        // handled elsewhere. This method is now just for internal cleanup.
        //super.removeFromParent();
    }
}
