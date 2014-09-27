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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author draque
 */
public class ThesNode extends DictNode {
    private final List<ThesNode> subNodes = new ArrayList<ThesNode>();
    private final List<ConWord> words = new ArrayList<ConWord>();
    private ThesNode parent;
    private String notes = "";
    
    /**
     * sets notes
     * @param _notes new notes
     */
    public void setNotes(String _notes) {
        notes = _notes;
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
     */    
    public ThesNode(ThesNode _parent) {
        parent = _parent;
    }
    
    /**
     * sets parent and value of node
     * @param _parent parent of note (null if root)
     * @param _value node's string value
     */
    public ThesNode(ThesNode _parent, String _value) {
        parent = _parent;
        this.setValue(_value);
    }
    
    /**
     * NOT IMPLEMENTED IN THESAURUSNODE
     * @param _node NOTHING
     */
    @Override
    public void setEqual(DictNode _node) {
        // DO NOT IMPLEMENT
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
     * @param _word word to remove
     */
    public void removeWord(ConWord _word) {
        words.remove(_word);
    }
    
    /**
     * gets all words in immediate family
     * @return iterator of all words in immediate family
     */
    public Iterator<ConWord> getWords() {
        Collections.sort(words);
        return words.iterator();
    }
    
    /**
     * gets all subnodes
     * @return alphabetically sorted iterator of all subnodes
     */
    public Iterator<ThesNode> getNodes() {
        Collections.sort(subNodes);
        
        return subNodes.iterator();
    }
    
    public void addNode(ThesNode _node) {
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
    public void removeChild(ThesNode _child) {
        subNodes.remove(_child);
    }
}
