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
package org.darisadesigns.polyglotlina.Nodes;

import org.darisadesigns.polyglotlina.PGTUtil;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A single node in a to do list tree
 * @author DThompson
 */
public class ToDoNode {
    private String value;
    private ToDoNode parent;
    private boolean isDone;
    private final List<ToDoNode> children = new ArrayList<>();
    
    public ToDoNode(ToDoNode _parent, String _value, boolean _isDone) {
        value = _value;
        parent = _parent;
        isDone = _isDone;
    }
    
    public boolean allChildrenDone() {
        boolean ret = true;
        
        for (ToDoNode curNode : children) {
            ret = ret && curNode.isDone && curNode.allChildrenDone();
        }
        
        return ret;
    }
    
    /**
     * Adds a new to-do child to this node
     * @param childValue 
     * @return  
     */
    public ToDoNode addChild(String childValue) {
        ToDoNode newChild = new ToDoNode(this, childValue, false);
        children.add(newChild);
        return newChild;
    }
    
    public void addChild(ToDoNode child) {
        child.parent = this;
        children.add(child);
    }
    
    public void setParent(ToDoNode _parent) {
        parent = _parent;
    }
    
    /**
     * Deletes node
     */
    public void delete() {
        if (parent != null) {
            parent.deleteChild(this);
        }
    }
    
    /**
     * Deletes child from list if it is present
     * @param delNode 
     */
    protected void deleteChild(ToDoNode delNode) {
        children.remove(delNode);
    }
    
    /**
     * moves child up one unless child is at top
     * @param move child to move
     */
    protected void moveChildUp(ToDoNode move) {
        if (children.contains(move)) {
            int index = children.indexOf(move);
            
            if (index > 0) {
                children.remove(index);
                children.add(index - 1, move);
            }
        }
    }
    
    public void setValue(String _value) {
        value = _value;
    }
    
    /**
     * Moves child down one unless it is at the bottom
     * @param move node to move down
     */
    protected void moveChildDown(ToDoNode move) {
        if (children.contains(move)) {
            int index = children.indexOf(move);
            
            if (index < children.size() - 1) {
                children.remove(index);
                children.add(index + 1, move);
            }
        }
    }
    
    public List<ToDoNode> getChildren() {
        return children;
    }
    
    public boolean isDone() {
        return isDone;
    }
    
    public void setDone(boolean _isDone) {
        isDone = _isDone;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    /**
     * Writes XML value of element to rootElement of given doc
     * @param doc
     * @param rootElement 
     */
    public void writeXML(Document doc, Element rootElement) {
        Element writeNode = doc.createElement(PGTUtil.TODO_NODE_XID);
        Element nodeDone = doc.createElement(PGTUtil.TODO_NODE_DONE_XID);
        Element nodeLabel = doc.createElement(PGTUtil.TODO_NODE_LABEL_XID);
        // TODO: Implement color
        //Element nodeColor = doc.createElement(PGTUtil.ToDoNodeColorXID);
        
        nodeDone.appendChild(doc.createTextNode(this.isDone ? PGTUtil.TRUE : PGTUtil.FALSE));
        writeNode.appendChild(nodeDone);
        
        nodeLabel.appendChild(doc.createTextNode(value));
        writeNode.appendChild(nodeLabel);
        
        children.forEach((child) -> {
            child.writeXML(doc, writeNode);
        });
        
        rootElement.appendChild(writeNode);
    }
    
    public ToDoNode getParent() {
        return parent;
    }
}
