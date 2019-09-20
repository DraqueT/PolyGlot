/*
 * Copyright (c) 2016-2019, Draque Thompson
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

import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.ManagersCollections.WordClassCollection;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Word properties cover things such as gender. They may apply to all parts of
 * speech, or only select parts of speech.
 * @author Draque Thompson
 */
public class WordClass extends DictNode {
    private WordClassCollection parent;
    private final Map<Integer, WordClassValue> values = new HashMap<>();
    private final List<Integer> applyTypes = new ArrayList<>();
    private boolean freeText = false;
    private int topId = 0;
    public WordClassValue buffer = new WordClassValue();
    
    public WordClass() {
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
        if (!(_node instanceof WordClass)) {
            throw new ClassCastException("Object not of type WordPropValueNode");
        }
        WordClass copyProp = (WordClass)_node;
        
        this.value = copyProp.getValue();
        
        copyProp.getValues().forEach((node) -> {
            try {
                addValue(node.getValue(), node.getId());
            } catch (Exception e) {
                IOHandler.writeErrorLog(e);
                throw new ClassCastException("Problem setting class value: " 
                        + e.getLocalizedMessage());
            }
        });
    }
    
    /**
     * inserts current buffer node into list of word property values
     * @throws java.lang.Exception
     */
    public void insert() throws Exception {
        addValue(buffer.getValue(), buffer.getId());
        buffer = new WordClassValue();
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
    public Collection<WordClassValue> getValues() {
        return values.values();
    }
    
    /**
     * Deletes value
     * @param valueId id of value to delete
     * @throws Exception on id notexists
     */
    public void deleteValue(int valueId) throws Exception {
        if (!values.containsKey(valueId)) {
            throw new Exception("Id: " + valueId + " does not exist in WordProperty: " + this.value + ".");
        }
        
        values.remove(valueId);
        
        if (parent != null) {
            parent.classValueDeleted(this.id, valueId);
        }
    }
    
    public WordClassValue getValueById(int _id) throws Exception {
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
    public WordClassValue addValue(String name) throws Exception {
        WordClassValue ret = addValue(name, topId);
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
    public WordClassValue addValue(String name, int id) throws Exception {
        if (values.containsKey(id)) {
            throw new Exception("Cannot insert value: " + name + " Id: " + id + " into " + this.value + " (already exists).");
        }
        
        WordClassValue ret = new WordClassValue();
        ret.setId(id);
        ret.setValue(name);
        values.put(id, ret);
        
        if (id >= topId) {
            topId = id + 1;
        }
        
        return ret;
    }

    /**
     * Whether or not this represents a free text field, rather than a multi-
     * selection with predefined values
     * 
     * @return Whether the property is a freetext property
     */
    public boolean isFreeText() {
        return freeText;
    }

    /**
     * Sets whether or not this represents a free text field, rather than a
     * multi-selection with predefined values
     * 
     * @param freeText freetext value
     */
    public void setFreeText(boolean freeText) {
        this.freeText = freeText;
    }
    
    public void setParent(WordClassCollection _parent) {
        parent = _parent;
    }
    
    public void writeXML(Document doc, Element rootElement) {
        Element classElement = doc.createElement(PGTUtil.CLASS_XID);

        // ID element
        Element classValue = doc.createElement(PGTUtil.CLASS_ID_XID);
        classValue.appendChild(doc.createTextNode(this.getId().toString()));
        classElement.appendChild(classValue);

        // Name element
        classValue = doc.createElement(PGTUtil.CLASS_NAME_XID);
        classValue.appendChild(doc.createTextNode(this.getValue()));
        classElement.appendChild(classValue);

        // Is Text Override
        classValue = doc.createElement(PGTUtil.CLASS_IS_FREETEXT_XID);
        classValue.appendChild(doc.createTextNode(this.isFreeText() ? PGTUtil.TRUE : PGTUtil.FALSE));
        classElement.appendChild(classValue);

        // generates element with all type IDs of types this class applies to
        String applyTypesRec = "";
        for (Integer typeId : this.getApplyTypes()) {
            if (applyTypesRec.length() != 0) {
                applyTypesRec += ",";
            }

            applyTypesRec += typeId.toString();
        }
        classValue = doc.createElement(PGTUtil.CLASS_APPLY_TYPES_XID);
        classValue.appendChild(doc.createTextNode(applyTypesRec));
        classElement.appendChild(classValue);

        // element for collection of values of class
        classValue = doc.createElement(PGTUtil.CLASS_VALUES_COLLECTION_XID);
        for (WordClassValue curValue : this.getValues()) {
            curValue.writeXML(doc, classValue);
        }
        classElement.appendChild(classValue);

        rootElement.appendChild(classElement);
    }
}
