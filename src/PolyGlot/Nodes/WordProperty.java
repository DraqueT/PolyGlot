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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Word properties cover things such as gender. They may apply to all parts of
 * speech, or only select parts of speech.
 * @author Draque Thompson
 */
public class WordProperty extends DictNode {
    private final Map<Integer, WordPropValueNode> values = new HashMap<>();
    private final List<Integer> applyTypes = new ArrayList<>();
    private int topId = 0;
    public WordPropValueNode buffer = new WordPropValueNode();
    
    public WordProperty() {
        // default to apply to all
        applyTypes.add(-1);
    }
    
    /**
     * Returns true if existing value ID is passed, false otherwise
     * @param valId value id to check
     * @return existance of value id
     */
    public boolean isValid(Integer valId) {
        return values.containsKey(valId);
    }
    
    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        if (!(_node instanceof WordProperty)) {
            throw new ClassCastException("Object not of type WordPropValueNode");
        }
        WordProperty copyProp = (WordProperty)_node;
        
        this.value = copyProp.getValue();
        
        for (WordPropValueNode curNode : copyProp.getValues()) {
            try {
                addValue(curNode.getValue(), curNode.getId());
            } catch (Exception ex) {
                throw new ClassCastException("Problem setting class value: " 
                        + ex.getLocalizedMessage());
            }
        }
    }
    
    /**
     * inserts current buffer node into list of word property values
     * @throws java.lang.Exception
     */
    public void insert() throws Exception {
        addValue(buffer.getValue(), buffer.getId());
        buffer = new WordPropValueNode();
    }
    
    /**
     * Adds type id to list of types this property applies to
     * -1 means "apply to all"
     * @param _typeId ID of type
     */
    public void addApplyType(int _typeId) {
        if (!applyTypes.contains(_typeId)) {
            applyTypes.add(_typeId);
        }
    }
    
    /**
     * Removes type id to list of types this property applies to
     * @param _typeId ID of type
     */
    public void deleteApplyType(Integer _typeId) {
        if (applyTypes.contains(_typeId)) {
            applyTypes.remove(_typeId);
        }
    }
    
    /**
     * Tests whether a this property applies to a given type
     * @param _typeId ID of type
     * @return true if applies
     */
    public boolean appliesToType(int _typeId) {
        return applyTypes.contains(_typeId);
    }
    
    /**
     * Gets copy of list of apply types
     * @return list of int values (ids)
     */
    public List<Integer> getApplyTypes() {
        return new ArrayList<>(applyTypes);
    }
    
    /**
     * Gets iterator of values
     * @return iterator with all values of word property
     */
    public Collection<WordPropValueNode> getValues() {
        return values.values();
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
     * @return value created
     * @throws java.lang.Exception if auto-assigned ID fails
     */
    public WordPropValueNode addValue(String name) throws Exception {
        WordPropValueNode ret = addValue(name, topId);
        topId++;
        return ret;
    }
    
    /**
     * Inserts value with ID (only use on file loading)
     * @param name
     * @param id
     * @return value created
     * @throws Exception if ID already exists
     */
    public WordPropValueNode addValue(String name, int id) throws Exception {
        if (values.containsKey(id)) {
            throw new Exception("Cannot insert value: " + name + " Id: " + id + " into " + this.value + " (already exists).");
        }
        
        WordPropValueNode ret = new WordPropValueNode();
        ret.setId(id);
        ret.setValue(name);
        values.put(id, ret);
        return ret;
    }
}
