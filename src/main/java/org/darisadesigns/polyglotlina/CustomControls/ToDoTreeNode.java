/*
 * Copyright (c) 2018-2019, Draque Thompson
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
package org.darisadesigns.polyglotlina.CustomControls;

import org.darisadesigns.polyglotlina.Nodes.ToDoNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author DThompson
 */
public final class ToDoTreeNode extends DefaultMutableTreeNode {
    /**
     * marked private because construction requires preprocessing logic
     * @param _userObject 
     */
    private ToDoTreeNode(Object _userObject) {
        super(_userObject);
        
        if (parent instanceof ToDoTreeNode) {
            ((ToDoNode)_userObject).setParent(((ToDoTreeNode)this.getParent()).getNode());
        }
    }
    
    public static ToDoTreeNode createToDoTreeNode(Object userObject) {
        return new ToDoTreeNode(wrapUserObject(userObject));
    }
    
    private static ToDoNode wrapUserObject(Object userObject) {
        ToDoNode ret;
        
        if (userObject instanceof ToDoNode) {
            ret = (ToDoNode)userObject;
        } else if (userObject instanceof String) {
            ret = new ToDoNode(null, (String)userObject, false);
        } else {
            throw new NotImplementedException("Unable to apply method to type: " + Object.class.getName());
        }
        
        return ret;
    }
    
    public ToDoNode getNode() {
        return (ToDoNode) this.getUserObject();
    }
    
    public boolean isSelected() {
        return getNode().isDone();
    }
    
    public void setSelected(boolean _selected) {
        getNode().setDone(_selected);
    }
    
    @Override
    public void add(MutableTreeNode childNode) {
        super.add(childNode);
        
        ((ToDoNode)this.getUserObject()).addChild(((ToDoTreeNode)childNode).getNode());
    }
    
    /**
     * Used for the initial construction of the tree from the core's root record
     * (avoids concurrent access/writing to child nodes)
     * @param childNode 
     */
    public void initialPopulateAdd(MutableTreeNode childNode) {
        super.add(childNode);
    }
}
