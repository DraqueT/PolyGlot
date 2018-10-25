/*
 * Copyright (c) 2014-2018, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 * See LICENSE.TXT included with this code to read the full license agreement.
 *
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

import PolyGlot.DictCore;
import PolyGlot.ManagersCollections.ConWordCollection;
import PolyGlot.PGTUtil;
import PolyGlot.WebInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class ConWord extends DictNode {
    // so long as the conword is not blank, this can be blank
    private String localWord;
    private int typeId;
    private String definition;
    private String pronunciation;
    private String etymNotes;
    private boolean procOverride;
    private boolean autoDeclensionOverride;
    private boolean rulesOverride;
    private DictCore core;
    private ConWordCollection parentCollection;
    private final Map<Integer, Integer> classValues = new HashMap<>();
    private final Map<Integer, String> classTextValues = new HashMap<>();
    private Object filterEtyParent;
    public String typeError = ""; // used only for returning error state

    public ConWord() {
        value = "";
        localWord = "";
        typeId = 0;
        definition = "";
        pronunciation = "";
        id = -1;
        procOverride = false;
        autoDeclensionOverride = false;
        rulesOverride = false;
        etymNotes = "";
    }
    
    /**
     * Returns simple boolean of whether conword is legal or not
     * @return 
     */
    public boolean isWordLegal() {
        ConWord checkValue = parentCollection.testWordLegality(this);
        String checkProc;
        
        // catches pronunciations which lead to regex errors
        try {
            checkProc = checkValue.getPronunciation();
        } catch (Exception e) {
            checkProc = "Regex error: " + e.getLocalizedMessage();
        }
        
        return checkValue.getValue().length() == 0 &&
                checkValue.getDefinition().length() == 0 &&
                checkValue.getLocalWord().length() == 0 &&
                checkProc.length() == 0 &&
                checkValue.typeError.length() == 0;
    }

    public boolean isRulesOverrride() {
        return rulesOverride;
    }
    
    public void setRulesOverride(boolean _rulesOverride) {
        rulesOverride = _rulesOverride;
    }
    
    public void setParent(ConWordCollection _parent) {
        parentCollection = _parent;
    }
    
    /**
     * Gets value of particular class (presuming freetext status)
     * @param classId id of class to retrieve value of
     * @return String's value for the given class. Blank string if not found/set.
     */
    public String getClassTextValue(int classId) {
        String ret = "";
        
        if (classTextValues.containsKey(classId)) {
            ret = classTextValues.get(classId);
        }
        
        return ret;
    }
    
    /**
     * Set's a world's class value (of class specified) to the given value
     * @param classId ID of class to set on word
     * @param classValue new value to set class to
     */
    public void setClassTextValue(int classId, String classValue) {
        if (classTextValues.containsKey(classId)) {
            classTextValues.replace(classId, classValue);
        } else {
            classTextValues.put(classId, classValue);
        }
    }
    
    /**
     * Tests whether this word has a class value applied to it. 
     * @param classId class id to test
     * @param valueId value id withi class to test
     * @return true if class + value appear on word
     */
    public boolean wordHasClassValue(int classId, int valueId) {
        return classValues.containsKey(classId) && classValues.get(classId) == valueId;
    }
    
    /**
     * Gets all freetext class values
     * Purges values which no longer exist
     * @return set of values with their IDs
     */
    public Set<Entry<Integer, String>> getClassTextValues() {
        Iterator<Entry<Integer, String>> classIt = new ArrayList<>(classTextValues.entrySet()).iterator();
        
        while (classIt.hasNext()) {
            Entry<Integer, String> curEntry = classIt.next();
            if (!core.getWordPropertiesCollection().exists(curEntry.getKey())) {
                classTextValues.remove(curEntry.getKey());
            }
        }
        
        return classTextValues.entrySet();
    }
        
    /**
     * @param _set sets all non ID values equal to that of parameter
     */
    @Override
    public void setEqual(DictNode _set) throws ClassCastException {
        if (!(_set instanceof ConWord)) {
            throw new ClassCastException("Object not of type ConWord");
        }
        
        if (core == null) {
            throw new ClassCastException("Core must be initialized in conword to use method SetEqual");
        }
                
        ConWord set = (ConWord) _set;
        set.setCore(core);
        
        this.setValue(set.getValue());
        this.setLocalWord(set.getLocalWord());
        this.setWordTypeId(set.getWordTypeId());
        this.setDefinition(set.getDefinition());
        try {
            this.setPronunciation(set.getPronunciation());
        } catch (Exception e) {
            this.setPronunciation("<ERROR>");
        }
        this.setId(set.getId());
        List<Entry<Integer, Integer>> precLock = new ArrayList<>(set.getClassValues()); // avoid read/write collisions
        precLock.forEach((entry) -> {
            this.setClassValue(entry.getKey(), entry.getValue());
        });
        List<Entry<Integer, String>> textLock = new ArrayList<>(set.getClassTextValues()); // avoid read/write collisions
        textLock.forEach((entry) -> {
            this.setClassTextValue(entry.getKey(), entry.getValue());
        });
        this.setProcOverride(set.isProcOverride());
        this.setOverrideAutoDeclen(set.isOverrideAutoDeclen());
        this.setEtymNotes(set.getEtymNotes());
    }
    
    public DictCore getCore() {
        return core;
    }
    
    public void setCore(DictCore _core) {
        core = _core;
    }
    
    /**
     * Returns string value of conword (reversed if appropriate)
     * @return 
     */
    @Override
    public String toString() {
        String ret;
        
        if (core == null
                || !core.getPropertiesManager().isEnforceRTL())
        {
            ret = super.toString();
        } else {
            ret = '\u202e' + super.toString();
        }
        
        return ret;
    }

    public boolean isOverrideAutoDeclen() {
        return autoDeclensionOverride;
    }
    
    public void setOverrideAutoDeclen(boolean _autoDeclensionOverride) {
        autoDeclensionOverride = _autoDeclensionOverride;
    }
    
    public boolean isProcOverride() {
        return procOverride;
    }
    
    public void setProcOverride(boolean _procOverride) {
        procOverride = _procOverride;
    }
    
    public String getLocalWord() {
        return localWord;
    }

    public void setLocalWord(String _localWord) {
        if (parentCollection != null) {
            parentCollection.extertalBalanceWordCounts(id, value, _localWord);
        }
        
        this.localWord = _localWord.trim();
    }
    
    @Override
    public void setValue(String _value) {
        if (parentCollection != null) {
            parentCollection.extertalBalanceWordCounts(id, _value, localWord);
        }        
        super.setValue(_value.replace(PGTUtil.RTLMarker, "").replace(PGTUtil.LTRMarker, ""));
    }

    /**
     * For display purposes only (use Type ID normally)
     * @return the string value of a word's type
     */
    public String getWordTypeDisplay() {
        String ret = "<TYPE NOT FOUND>";
        
        if (typeId != 0) {
            try {
                ret = core.getTypes().getNodeById(typeId).getValue();
            } catch (Exception e) {
                // If a type no longer exists, set the type ID to 0, then continue
                typeId = 0;
            }
        }
        return ret;
    }

    public void setWordTypeId(int _typeId) {
        typeId = _typeId;
    }
    
    public Integer getWordTypeId() {
        return typeId;
    }

    /**
     * Returns false if the word is invalid for any reason
     *
     * @return false if invalid
     */
    public boolean checkValid() {
        boolean ret = true;

        // There might be no local translation, but the constructed word must exist
        ret = ret && (value.length() != 0);

        return ret;
    }

    public String getDefinition() {      
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /**
     * If pronunciation override is not selected, fetches generated pronunciation
     * for this word. If generated pronunciation is blank, returns saved value.
     * @return pronunciation of word
     * @throws java.lang.Exception when regex error encountered
     */
    public String getPronunciation() throws Exception {
        String ret = pronunciation;
        
        if (!procOverride && core != null) {
            String gen = core.getPronunciationMgr().getPronunciation(value);
            if (gen.length() != 0) {
                ret = gen;
            }
        }
        
        return ret;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }
    
    /**
     * Sets a the class of a word to a given value. If the class does not exist yet for the word, it is created.
     * If value ID = -1, the class is simply removed and not set to a value at all
     * @param classId ID of class to set value for
     * @param valueId ID of value to set the class to
     */
    public void setClassValue(int classId, int valueId) {
        if (classValues.containsKey(classId)) {
            classValues.remove(classId);
        } 
        if (valueId != -1) {
            classValues.put(classId, valueId);
        }
    }
    
    /**
     * Gets sets of entries representing classes the word contains and their values
     * Note: THIS IS NOT COMPREHENSIVE! If no value has been set for a word, it will
     * not be returned at all.
     * 
     * Out of date values will be checked/wiped if the related properties or
     * property values have been deleted from the language file.
     * 
     * To get a comprehensive list, look at the WordPropertyCollection values 
     * associated with the word's type.
     * @return list of entries of <class id, value id>
     */
    public Set<Entry<Integer, Integer>> getClassValues() {
        // verify validity before returning each: otherwise remove
        
        Iterator<Entry<Integer, Integer>> classIt = new ArrayList<>(classValues.entrySet()).iterator();
        
        while (classIt.hasNext()) {
            Entry<Integer, Integer> curEntry = classIt.next();
            
            if (!core.getWordPropertiesCollection().isValid(curEntry.getKey(), 
                    curEntry.getValue())) {
                classValues.remove(curEntry.getKey());
            }
        }
        
        return classValues.entrySet();
    }
    
    /**
     * Gets value of a class for a word by class' id
     * @param classId ID of class to get value of
     * @return id of value assigned to class. -1 if not set.
     */
    public Integer getClassValue(int classId) {
        return classValues.containsKey(classId) ? classValues.get(classId) : -1;
    }
    
    /**
     * Respects default alpha order and orders by localword (if any) if parent
     * value set for this.
     * @param _compare
     * @return 
     */
    @Override
    public int compareTo(DictNode _compare) {
        int ret;
        
        if (parentCollection != null && parentCollection.isLocalOrder()) {
            ret = this.getLocalWord().compareToIgnoreCase(((ConWord)_compare).getLocalWord());
        } else {
            ret = super.compareTo(_compare);
        }
        
        return ret;
    }

    /**
     * @return the etymNotes
     */
    public String getEtymNotes() {
        return etymNotes;
    }

    /**
     * @param etymNotes the etymNotes to set
     */
    public void setEtymNotes(String etymNotes) {
        this.etymNotes = etymNotes;
    }

    /**
     * @return the filterEtyParent
     */
    public Object getFilterEtyParent() {
        return filterEtyParent;
    }

    /**
     * @param filterEtyParent the filterEtyParent to set
     */
    public void setFilterEtyParent(Object filterEtyParent) {
        this.filterEtyParent = filterEtyParent;
    }
    
    public void writeXML(Document doc, Element rootElement) {
        Element wordNode = doc.createElement(PGTUtil.wordXID);

            Element wordValue = doc.createElement(PGTUtil.wordIdXID);
            Integer wordId = this.getId();
            wordValue.appendChild(doc.createTextNode(wordId.toString()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.localWordXID);
            wordValue.appendChild(doc.createTextNode(this.getLocalWord()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.conWordXID);
            wordValue.appendChild(doc.createTextNode(this.getValue()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordTypeIdXID);
            wordValue.appendChild(doc.createTextNode(this.getWordTypeId().toString()));
            wordNode.appendChild(wordValue);

            try {
                wordValue = doc.createElement(PGTUtil.wordProcXID);
                wordValue
                        .appendChild(doc.createTextNode(this.getPronunciation()));
                wordNode.appendChild(wordValue);
            } catch (Exception e) {
                // Do nothing. Users are made aware of this issue elsewhere.
            }

            wordValue = doc.createElement(PGTUtil.wordDefXID);
            wordValue.appendChild(doc.createTextNode(WebInterface.archiveHTML(this.getDefinition())));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordProcOverrideXID);
            wordValue.appendChild(doc.createTextNode(this.isProcOverride() ? PGTUtil.True : PGTUtil.False));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordAutoDeclenOverrideXID);
            wordValue.appendChild(doc.createTextNode(this.isOverrideAutoDeclen() ? PGTUtil.True : PGTUtil.False));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordRuleOverrideXID);
            wordValue.appendChild(doc.createTextNode(this.isRulesOverrride() ? PGTUtil.True : PGTUtil.False));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordClassCollectionXID);
            for (Entry<Integer, Integer> entry : this.getClassValues()) {
                Element classVal = doc.createElement(PGTUtil.wordClassAndValueXID);
                classVal.appendChild(doc.createTextNode(entry.getKey() + "," + entry.getValue()));
                wordValue.appendChild(classVal);
            }
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordClassTextValueCollectionXID);
            for (Entry<Integer, String> entry : this.getClassTextValues()) {
                Element classVal = doc.createElement(PGTUtil.wordClassTextValueXID);
                classVal.appendChild(doc.createTextNode(entry.getKey() + "," + entry.getValue()));
                wordValue.appendChild(classVal);
            }
            wordNode.appendChild(wordValue);
            
            wordValue = doc.createElement(PGTUtil.wordEtymologyNotesXID);
            wordValue.appendChild(doc.createTextNode(this.getEtymNotes()));
            wordNode.appendChild(wordValue);
            
            rootElement.appendChild(wordNode);
    }
}
