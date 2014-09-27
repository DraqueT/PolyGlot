/*
 * Copyright (c) 2014, draque
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * This overrides the typical tree node to link it directly to thesaurus nodes
 *
 * @author draque
 */
public class ThesTreeNode extends DefaultMutableTreeNode {

    ThesNode thesNode;

    /**
     * constructs any child ThesTreeNode
     *
     * @param parent
     */
    public ThesTreeNode(ThesNode parent) {
        thesNode = new ThesNode(parent);
    }

    /**
     * constructor allows for root creation without parent
     */
    public ThesTreeNode() {
    }

    /**
     * Sets node to ThesNode value and test to object's value
     *
     * @param _node node to set from
     */
    public void setNode(ThesNode _node) {
        thesNode = _node;
        setUserObject(_node.getValue());
    }

    /**
     * Sets passed node as root relative to position and populates from there
     *
     * @param _myRoot
     */
    public void setAsRootNode(ThesNode _myRoot) {
        setNode(_myRoot);

        Iterator<ThesNode> thesIt = _myRoot.getNodes();

        while (thesIt.hasNext()) {
            ThesNode curNode = thesIt.next();

            ThesTreeNode treeNode = new ThesTreeNode(_myRoot);
            treeNode.setAsRootNode(curNode);

            add(treeNode);
        }
    }

    public ThesNode getNode() {
        return thesNode;
    }

    @Override
    public void setUserObject(Object userObject) {
        if (userObject instanceof String) {
            thesNode.setValue((String) userObject);
        }
        super.setUserObject(userObject);
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        // only modify node if new. ID = 0 indicates loading from existing tree
        if (((ThesTreeNode) child).getNode().getId() == 1) {
            thesNode.addNode(((ThesTreeNode) child).getNode());
            
            // once read, this is no longer a new node
            ((ThesTreeNode) child).getNode().setId(0);
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
        thesNode.removeFromParent();

        // handled elsewhere. This method is now just for internal cleanup.
        //super.removeFromParent();
    }
}
