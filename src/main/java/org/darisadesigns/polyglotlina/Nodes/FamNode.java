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

import org.darisadesigns.polyglotlina.ManagersCollections.FamilyManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author draque
 */
public class FamNode extends DictNode {
    private final List<FamNode> subNodes = new ArrayList<>();
    private final List<ConWord> words = new ArrayList<>();
    private final FamNode parent;
    private String notes = "";
    private final FamilyManager manager;
    
    /**
     * sets notes
     * @param _notes new notes
     */
    public void setNotes(String _notes) {
        notes = _notes;
    }
    
    /**
     * Gets node's parent
     * @return FamNode representing node's parent. null if root
     */
    public FamNode getParent() {
        return parent;
    }
    
    /**
     * gets notes
     * @return notes of node
     */
    public String getNotes() {
        return notes;
    }
    
    /**
     * sets parent of node
     * @param _parent node's parent (null if root)
     * @param _manager a link to the parent manager
     */    
    public FamNode(FamNode _parent, FamilyManager _manager) {
        parent = _parent;
        manager = _manager;
    }
    
    /**
     * sets parent and value of node
     * @param _parent parent of note (null if root)
     * @param _value node's string value
     * @param _manager A link to the parent manager
     */
    public FamNode(FamNode _parent, String _value, FamilyManager _manager) {
        parent = _parent;
        this.setValue(_value);
        manager = _manager;
    }
    
    @Override
    public final void setValue(String _value) {
        super.setValue(_value);
    }
    
    /**
     * gets node's manager
     * @return Family Manager
     */
    public FamilyManager getManager() {
        return manager;
    }
    
    /**
     * NOT IMPLEMENTED IN FAMILYNODE
     * @param _node NOTHING
     */
    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        throw new ClassCastException("setEqual should never be called on FamNode instances.");
    }
        
    /**
     * adds word to family. ignores dupes
     * @param _word the word to add
     */
    public void addWord(ConWord _word) {
        if (!words.contains(_word)) {
            words.add(_word);
        }
    }
    
    /**
     * removes word from family
     * @param _word id of word to remove
     */
    public void removeWord(ConWord _word) {
        words.remove(_word);
    }
    
    /**
     * gets all words in immediate family
     * @return iterator of all words in immediate family
     */
    public Iterator<ConWord> getWords() {
        manager.removeDeadWords(this, words);

        List<ConWord> ret = new ArrayList<>(words);
        Collections.sort(ret);
        
        return ret.iterator();
    }
    
    /**
     * returns all words within family and subfamilies
     * @return sorted list of ConWords
     */
    public List<ConWord> getWordsIncludeSubs() {
        List<ConWord> ret = getWordsIncludeSubsInternal();
        Collections.sort(ret);
        
        return ret;
    }
    
    /**
     * internally facing, recursive method for getting all words in this and subfamilies
     * @return list of (non duped) words in this and all subnodes
     */
    private List<ConWord> getWordsIncludeSubsInternal() {
        manager.removeDeadWords(this, words);
        
        List<ConWord> ret = new ArrayList<>(words);
        
        Iterator<FamNode> subIt = subNodes.iterator();
        
        while (subIt.hasNext()) {
            FamNode curNode = subIt.next();
            
            Iterator<ConWord> wordIt = curNode.getWordsIncludeSubsInternal()
                    .iterator();
            
            while (wordIt.hasNext()) {
                ConWord curWord = wordIt.next();
                
                // only add current word to return value if not already present
                if (!ret.contains(curWord)) {
                    ret.add(curWord);
                }
            }
        }
        
        return ret;
    }
    
    /**
     * gets all subnodes
     * @return alphabetically sorted iterator of all subnodes
     */
    public List<FamNode> getNodes() {
        Collections.sort(subNodes);
        
        return subNodes;
    }
    
    public void addNode(FamNode _node) {
        subNodes.add(_node);
    }
    
    /** 
     * removes self from parent, does nothing of root
     * @return false if root
     */
    public boolean removeFromParent() {
        if (parent == null) {
            return false;
        }
        
        parent.removeChild(this);
        
        return true;
    }
    
    /**
     * removes given child from this parent node
     * @param _child 
     */
    public void removeChild(FamNode _child) {
        subNodes.remove(_child);
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp != null && getClass() == comp.getClass()) {
            FamNode c = (FamNode)comp;
            
            ret = value.equals(c.value);
            ret = ret && subNodes.equals(c.subNodes);
            ret = ret && words.equals(c.words);
            ret = ret && parent == c.parent; // test IDENTITY here, rather than contents
            ret = ret && notes.equals(c.notes);
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
