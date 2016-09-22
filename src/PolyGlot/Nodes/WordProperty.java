/*
 * Copyright (c) 2016, Draque
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Word properties cover things such as gender. They may apply to all parts of
 * speech, or only select parts of speech.
 * @author Draque Thompson
 */
public class WordProperty extends DictNode {
    private final Map<Integer, WordPropValueNode> values = new HashMap<>();
    int topId = 0;
    
    @Override
    public void setEqual(DictNode _node) {
        if (!(_node instanceof WordProperty)) {
            // TODO: All nodes should throw exceptions if told to be set equal to the wrong type (enhancement #291)
        }
        WordProperty copyProp = (WordProperty)_node;
        
        this.value = copyProp.getValue();
        
        Iterator<WordPropValueNode> it = copyProp.getValues();
        while (it.hasNext()) {
            WordPropValueNode curNode = it.next();
            
            try {
                addValue(curNode.name, curNode.getId());
            } catch (Exception ex) {
                // TODO: Make this bubble properly once setEqual throws (enhancement #291)
            }
        }
    }
    
    /**
     * Gets iterator of values
     * @return iterator with all values of word property
     */
    public Iterator<WordPropValueNode> getValues() {
        return values.values().iterator();
    }
    
    /**
     * Deletes value
     * @param _id id of value to delete
     * @throws Exception on id notexists
     */
    public void deleteValue(int _id) throws Exception {
        if (!values.containsKey(_id)) {
            throw new Exception("Id: " + _id + " does not exist in WordProperty: " + this.value + ".");
        }
        
        values.remove(_id);
    }
    
    public WordPropValueNode getValueById(int _id) throws Exception {
        if (!values.containsKey(_id)) {
            throw new Exception("No value with id: " + _id + " in property: " + this.value + ".");
        }
        
        return values.get(_id);
    }
    
    /**
     * Adds new value, assigning ID automatically.
     * @param name 
     * @throws java.lang.Exception if auto-assigned ID fails
     */
    public void addValue(String name) throws Exception {
        addValue(name, topId);
        topId++;
    }
    
    /**
     * Inserts value with ID (only use on file loading)
     * @param name
     * @param id
     * @throws Exception if ID already exists
     */
    public void addValue(String name, int id) throws Exception {
        if (values.containsKey(id)) {
            throw new Exception("Cannot insert value: " + name + " Id: " + id + " into " + this.value + " (already exists).");
        }
        
        values.put(id, new WordPropValueNode(id, name));
    }
}
