/*
 * Copyright (c) 2014-2016, Draque Thompson, draquemail@gmail.com
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

import PolyGlot.CustomControls.InfoBox;
import PolyGlot.DictCore;
import PolyGlot.ManagersCollections.ConWordCollection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    private String gender;
    private boolean procOverride;
    private boolean autoDeclensionOverride;
    private boolean rulesOverride;
    private DictCore core;
    private ConWordCollection parent;
    private final Map<Integer, Integer> classValues = new HashMap<>();
    public String typeError = ""; // used only for returning error state

    public ConWord() {
        value = "";
        localWord = "";
        typeId = 0;
        definition = "";
        pronunciation = "";
        gender = "";
        id = -1;
        procOverride = false;
        autoDeclensionOverride = false;
        rulesOverride = false;
    }
    
    /**
     * Returns simple boolean of whether conword is legal or not
     * @return 
     */
    public boolean isWordLegal() {
        ConWord checkValue = parent.testWordLegality(this);
        
        return checkValue.getValue().equals("") &&
                checkValue.getDefinition().equals("") &&
                checkValue.getGender().equals("") &&
                checkValue.getLocalWord().equals("") &&
                checkValue.getPronunciation().equals("") &&
                checkValue.typeError.equals("");
    }

    public boolean isRulesOverrride() {
        return rulesOverride;
    }
    
    public void setRulesOverride(boolean _rulesOverride) {
        rulesOverride = _rulesOverride;
    }
    
    public void setParent(ConWordCollection _parent) {
        parent = _parent;
    }
        
    /**
     * @param _set sets all non ID values equal to that of parameter
     */
    @Override
    public void setEqual(DictNode _set) {
        ConWord set = (ConWord) _set;
        
        this.setValue(set.getValue());
        this.setLocalWord(set.getLocalWord());
        this.setWordTypeId(set.getWordTypeId());
        this.setDefinition(set.getDefinition());
        this.setPronunciation(set.getPronunciation());
        this.setId(set.getId());
        this.setGender(set.getGender());
        this.setProcOverride(set.isProcOverride());
        this.setOverrideAutoDeclen(set.isOverrideAutoDeclen());
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
        if (parent != null) {
            try {
                parent.extertalBalanceWordCounts(id, value, _localWord);
            } catch (Exception e) {
                InfoBox.error("Word balance error.", "Unable to balance word: " 
                        + value, null);
            }
        }
        
        this.localWord = _localWord.trim();
    }
    
    @Override
    public void setValue(String _value) {
        if (parent != null) {
            try {
                parent.extertalBalanceWordCounts(id, _value, localWord);
            } catch (Exception e) {
                InfoBox.error("Word balance error.", "Unable to balance word: " 
                        + value, null);
            }
        }        
        super.setValue(_value);
    }

    /**
     * For display purposes only (use Type ID normally)
     * @return the string value of a word's type
     */
    public String getWordTypeDisplay() {
        String ret = "";
        
        if (typeId != 0) {
            try {
                ret = core.getTypes().getNodeById(typeId).getValue();
            } catch (Exception e) {
                // TODO: How to better handle this? Silent failure will eventually cause problems...
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
        ret = ret && (!value.equals(""));

        return ret;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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
     * To get a comprehensive list, look at the WordPropertyCollection values 
     * associated with the word's type.
     * @return list of entries of <class id, value id>
     */
    public Set<Entry<Integer, Integer>> getClassValues() {
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
        
        if (parent != null && parent.isLocalOrder()) {
            ret = this.getLocalWord().compareToIgnoreCase(((ConWord)_compare).getLocalWord()); // TODO: check to make certain _compare is type conword?
        } else {
            ret = super.compareTo(_compare);
        }
        
        return ret;
    }
}
