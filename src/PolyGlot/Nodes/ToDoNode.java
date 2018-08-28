/*
 * Copyright (c) 2018, DThompson
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
package PolyGlot.Nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * A single node in a to do list tree
 * @author DThompson
 */
public class ToDoNode {
    private final String value;
    private final ToDoNode parent;
    private boolean isDone = false;
    private final List<ToDoNode> children = new ArrayList<>();
    
    public ToDoNode(ToDoNode _parent, String _value) {
        value = _value;
        parent = _parent;
    }
    
    /**
     * Adds a new to-do child to this node
     * @param childValue 
     */
    public void addChild(String childValue) {
        children.add(new ToDoNode(this, childValue));
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
     * moves node up unless already at top
     */
    public void moveUp() {
        if (parent != null) {
            parent.moveChildUp(this);
        }
    }
    
    /**
     * Moves node down unless already at bottom
     */
    public void moveDown() {
        if (parent != null) {
            parent.moveChildDown(this);
        }
    }
    
    /**
     * Deletes child from list if it is present
     * @param delNode 
     */
    protected void deleteChild(ToDoNode delNode) {
        if (children.contains(delNode)) {
            children.remove(delNode);
        }
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
}
