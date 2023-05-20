/*
 * Copyright (c) 2016-2022, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Nodes;

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
    private final Map<Integer, WordClassValue> values = new HashMap<>();
    private final List<Integer> applyTypes = new ArrayList<>();
    private boolean freeText = false;
    private boolean associative = false;
    private int topId = 0;
    public WordClassValue buffer = new WordClassValue();
    
    public WordClass() {
        // default to apply to all
        applyTypes.add(-1);
    }
    
    /**
     * Returns true if existing value ID is passed, false otherwise
     * @param valId value id to check
     * @return existence of value id
     */
    public boolean isValid(Integer valId) {
        return values.containsKey(valId);
    }
    
    @Override
    public void setEqual(DictNode _node) {
        if (!(_node instanceof WordClass)) {
            throw new ClassCastException("Object not of type WordPropValueNode");
        }
        WordClass copyProp = (WordClass)_node;
        
        this.value = copyProp.getValue();
        this.values.clear();
        this.values.putAll(copyProp.values);
        this.applyTypes.clear();
        this.applyTypes.addAll(copyProp.applyTypes);
        this.freeText = copyProp.freeText;
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
        applyTypes.remove(_typeId);
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
     * @return Integer[] of int values (ids)
     */
    public Integer[] getApplyTypes() {
        return applyTypes.toArray(new Integer[0]);
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
     * @param _id
     * @return value created
     * @throws Exception if ID already exists
     */
    public WordClassValue addValue(String name, int _id) throws Exception {
        if (values.containsKey(_id)) {
            throw new Exception("Cannot insert value: " + name + " Id: " + _id + " into " + this.value + " (already exists).");
        }
        
        WordClassValue ret = new WordClassValue();
        ret.setId(_id);
        ret.setValue(name);
        values.put(_id, ret);
        
        if (_id >= topId) {
            topId = _id + 1;
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
     * @param _freeText freetext value
     */
    public void setFreeText(boolean _freeText) {
        this.freeText = _freeText;
        this.associative = this.associative && !_freeText;
    }
    
    /**
     * Whether or not this represents a free text field, rather than a multi-
     * selection with predefined values
     * 
     * @return Whether the property is an associative property
     */
    public boolean isAssociative() {
        return associative;
    }

    /**
     * Sets whether or not this represents a free text field, rather than a
     * multi-selection with predefined values
     * 
     * @param _associative
     */
    public void setAssociative(boolean _associative) {
        this.associative = _associative;
        this.freeText = this.freeText && !_associative;
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
        classValue.appendChild(doc.createTextNode(this.freeText ? PGTUtil.TRUE : PGTUtil.FALSE));
        classElement.appendChild(classValue);
        
        // Is class associative CLASS_IS_ASSOCIATIVE_XID
        classValue = doc.createElement(PGTUtil.CLASS_IS_ASSOCIATIVE_XID);
        classValue.appendChild(doc.createTextNode(this.associative ? PGTUtil.TRUE : PGTUtil.FALSE));
        classElement.appendChild(classValue);

        // generates element with all type IDs of types this class applies to
        String applyTypesRec = "";
        for (Integer typeId : this.getApplyTypes()) {
            if (!applyTypesRec.isEmpty()) {
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
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp != null && getClass() == comp.getClass()) {
            WordClass c = (WordClass) comp;
            
            ret = value.replaceAll("\\s", "").equals(c.value.replaceAll("\\s", ""));
            ret = ret && values.equals(c.values);
            ret = ret && applyTypes.equals(c.applyTypes);
            ret = ret && freeText == c.freeText;
            ret = ret && associative == c.associative;
        }
        
        return ret;
    }
}
