/*
 * Copyright (c) 2014-2023, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
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
package org.darisadesigns.polyglotlina.Nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.ManagersCollections.DictionaryCollection;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.RegexTools;
import org.darisadesigns.polyglotlina.RegexTools.ReplaceOptions;
import org.darisadesigns.polyglotlina.WebInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class ConWord extends DictNode {

    private String localWord;
    private int typeId;
    protected String definition;
    private String pronunciation;
    private String etymNotes;
    private boolean procOverride;
    private boolean autoConjugationOverride;
    private boolean rulesOverride;
    protected DictCore core;
    private ConWordCollection parentCollection;
    private final Map<Integer, Integer> classValues = new HashMap<>();
    private final Map<Integer, String> classTextValues = new HashMap<>();
    private Object filterEtyParent;
    public String typeError = ""; // used only for returning error state

    public ConWord() {
        super();
        localWord = "";
        typeId = 0;
        definition = "";
        pronunciation = "";
        procOverride = false;
        autoConjugationOverride = false;
        rulesOverride = false;
        etymNotes = "";
    }
    
    /**
     * Primarily used for convenience in testing
     * @param value
     * @param local 
     */
    public ConWord(String value, String local) {
        this();
        
        this.value = value;
        this.localWord = local;
    }

    /**
     * Applies evolution transform to word and recorded conjugations of word
     *
     * @param regex
     * @param replacement
     * @param instanceOption
     * @throws java.lang.Exception
     */
    public void evolveWord(String regex, String replacement, ReplaceOptions instanceOption) throws Exception {
        this.setValue(RegexTools.advancedReplace(this.value, regex, replacement, instanceOption));
    }

    /**
     * Returns simple boolean of whether conword is legal or not
     *
     * @return
     */
    public boolean isWordLegal() {
        ConWord checkValue = parentCollection.testWordLegality(this);
        String checkProc;

        // catches pronunciations which lead to regex errors
        try {
            checkProc = checkValue.getPronunciation();
        }
        catch (Exception e) {
            // IOHandler.writeErrorLog(e);
            checkProc = "Regex error: " + e.getLocalizedMessage();
        }

        return checkValue.getValue().isEmpty()
                && checkValue.definition.isEmpty()
                && checkValue.localWord.isEmpty()
                && checkProc.isEmpty()
                && checkValue.typeError.isEmpty();
    }

    public boolean isRulesOverride() {
        return rulesOverride;
    }

    public void setRulesOverride(boolean _rulesOverride) {
        rulesOverride = _rulesOverride;
    }

    @Override
    public void setParent(DictionaryCollection _parent) {
        super.setParent(_parent);

        if (_parent instanceof ConWordCollection) {
            parentCollection = (ConWordCollection) _parent;
        }
    }

    /**
     * Gets value of particular class (presuming freetext status)
     *
     * @param classId id of class to retrieve value of
     * @return String's value for the given class. Blank string if not
     * found/set.
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
     *
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
     *
     * @param classId class id to test
     * @param valueId value id with class to test
     * @return true if class + value appear on word
     */
    public boolean wordHasClassValue(int classId, int valueId) {
        return classValues.containsKey(classId) && classValues.get(classId) == valueId;
    }

    /**
     * Gets all freetext class values Purges values which no longer exist
     *
     * @return set of values with their IDs
     */
    public Set<Entry<Integer, String>> getClassTextValues() {
        Iterator<Entry<Integer, String>> classIt = new ArrayList<>(classTextValues.entrySet()).iterator();

        while (classIt.hasNext()) {
            Entry<Integer, String> curEntry = classIt.next();
            if (!core.getWordClassCollection().exists(curEntry.getKey())) {
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

        ConWord set = (ConWord) _set;

        // tight coupling between conword and core due to word classes causes this... might refactor later (high risk)
        if (core == null || set.core == null) {
            throw new ClassCastException("Core must be initialized in conword to use method SetEqual");
        }

        this.value = set.value;
        this.setLocalWord(set.localWord);
        this.typeId = set.typeId;
        this.definition = set.definition;
        this.pronunciation = set.pronunciation;
        this.id = set.id;
        List<Entry<Integer, Integer>> precLock = new ArrayList<>(set.getClassValues()); // avoid read/write collisions
        precLock.forEach((entry) -> {
            this.setClassValue(entry.getKey(), entry.getValue());
        });
        List<Entry<Integer, String>> textLock = new ArrayList<>(set.getClassTextValues()); // avoid read/write collisions
        textLock.forEach((entry) -> {
            this.setClassTextValue(entry.getKey(), entry.getValue());
        });
        this.procOverride = set.procOverride;
        this.autoConjugationOverride = set.autoConjugationOverride;
        this.etymNotes = set.etymNotes;
        this.rulesOverride = set.rulesOverride;
    }

    @Override
    public boolean equals(Object comp) {
        boolean ret = false;

        if (this == comp) {
            ret = true;
        } else if (comp != null && getClass() == comp.getClass()) {
            ConWord c = (ConWord) comp;

            ret = value.equals(c.value);
            ret = ret && localWord.replaceAll("\\s", "").equals(c.localWord.replaceAll("\\s", ""));
            ret = ret && typeId == c.typeId;
            ret = ret && WebInterface.archiveHTML(definition, core).equals(WebInterface.archiveHTML(c.definition, core));
            ret = ret && WebInterface.getTextFromHtml(etymNotes).equals(WebInterface.getTextFromHtml(c.etymNotes));
            ret = ret && procOverride == c.procOverride;
            ret = ret && autoConjugationOverride == c.autoConjugationOverride;
            ret = ret && rulesOverride == c.rulesOverride;
            ret = ret && classValues.equals(c.classValues);
            ret = ret && classTextValues.equals(c.classTextValues);
            
            if (procOverride) {
                ret = ret && pronunciation.replaceAll("\\s", "").equals(c.pronunciation.replaceAll("\\s", ""));
            }
        }

        return ret;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Fetches word's dictionary core. WARNING: VALUE MAY BE NULL.
     *
     * @return
     */
    public DictCore getCore() {
        return core;
    }

    public void setCore(DictCore _core) {
        if (core != null) {
            parentCollection = core.getWordCollection();
        } else {
            parentCollection = null;
        }

        core = _core;
    }

    public boolean isOverrideAutoConjugate() {
        return autoConjugationOverride;
    }

    public void setOverrideAutoConjugate(boolean _autoConjugationOverride) {
        autoConjugationOverride = _autoConjugationOverride;
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
        this.localWord = _localWord.trim();
    }

    /**
     * For display purposes only (use Type ID normally)
     *
     * @return the string value of a word's type
     */
    public String getWordTypeDisplay() {
        String ret = "<TYPE NOT FOUND>";

        if (typeId != 0) {
            try {
                ret = core.getTypes().getNodeById(typeId).getValue();
            }
            catch (Exception e) {
                core.getOSHandler().getIOHandler().writeErrorLog(e);
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

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String _definition) {
        this.definition = _definition;
    }

    /**
     * If pronunciation override is not selected, fetches generated
     * pronunciation for this word. If generated pronunciation is blank, returns
     * saved value.
     *
     * @return pronunciation of word
     * @throws java.lang.Exception when regex error encountered
     */
    public String getPronunciation() throws Exception {
        String ret = pronunciation;

        if (!procOverride && core != null) {
            String gen = core.getPronunciationMgr().getPronunciation(value);
            if (!gen.isEmpty()) {
                ret = gen;
            }
        }

        return ret;
    }

    public void setPronunciation(String _pronunciation) {
        this.pronunciation = _pronunciation;
    }

    /**
     * Sets a the class of a word to a given value. If the class does not exist
     * yet for the word, it is created. If value ID = -1, the class is simply
     * removed and not set to a value at all
     *
     * @param classId ID of class to set value for
     * @param valueId ID of value to set the class to
     */
    public void setClassValue(int classId, int valueId) {
        classValues.remove(classId);

        if (valueId != -1) {
            classValues.put(classId, valueId);
        }
    }

    /**
     * Gets sets of entries representing classes the word contains and their
     * values Note: THIS IS NOT COMPREHENSIVE! If no value has been set for a
     * word, it will not be returned at all.
     *
     * Out of date values will be checked/wiped if the related properties or
     * property values have been deleted from the language file.
     *
     * To get a comprehensive list, look at the WordPropertyCollection values
     * associated with the word's type.
     *
     * @return list of entries of <class id, value id>
     */
    public Set<Entry<Integer, Integer>> getClassValues() {
        // verify validity before returning each: otherwise remove

        Iterator<Entry<Integer, Integer>> classIt = new ArrayList<>(classValues.entrySet()).iterator();

        while (classIt.hasNext()) {
            Entry<Integer, Integer> curEntry = classIt.next();

            if (!core.getWordClassCollection().isValid(curEntry.getKey(), curEntry.getValue())) {
                classValues.remove(curEntry.getKey());
            }
        }

        return classValues.entrySet();
    }

    /**
     * Gets value of a class for a word by class' id
     *
     * @param classId ID of class to get value of
     * @return id of value assigned to class. -1 if not set.
     */
    public Integer getClassValue(int classId) {
        Integer ret = -1;

        if (classValues.containsKey(classId)) {
            Integer tmp = classValues.get(classId);
            if (tmp != null) {
                ret = tmp;
            }
        }

        return ret;
    }

    /**
     * Respects default alpha order and orders by localword (if any) if parent
     * value set for this.
     *
     * @param _compare
     * @return
     */
    @Override
    public int compareTo(DictNode _compare) {
        int ret;

        if (parentCollection != null && parentCollection.isLocalOrder()) {
            ret = this.getLocalWord().compareToIgnoreCase(((ConWord) _compare).getLocalWord());
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
     * @param _etymNotes the etymNotes to set
     */
    public void setEtymNotes(String _etymNotes) {
        this.etymNotes = _etymNotes;
    }

    /**
     * @return the filterEtyParent
     */
    public Object getFilterEtyParent() {
        return filterEtyParent;
    }
    
    /**
     * Gets a summary of the conlang word (primarily for hover text)
     * 
     * @param procGen whether to include pronunciation generation in summary
     * @return 
     * @throws java.lang.Exception
     */
    public String getWordSummaryValue(boolean procGen) throws Exception {
        TypeNode curType = core.getTypes().getNodeById(this.getWordTypeId());
        String summary = "";
        
        try {
            summary = core.getPronunciationMgr().getPronunciation(this.getValue()).trim();
            
            if (summary.isEmpty()) {
                // allow for pronunciation overrides
                summary = this.getPronunciation().trim();
            }
        } catch (Exception ex) {
            // User is informed of this elsewhere.
        }
            
        if (summary.isEmpty()) {
            summary = this.getLocalWord().trim();
        }
        
        if (summary.isEmpty()) {
            summary = this.getValue().trim();
        }
        
        if (curType != null && (curType.getId() != 0 || core.getPropertiesManager().isTypesMandatory())) {
            summary += " : " + (curType.getGloss().isEmpty()
                    ? curType.getValue().trim() : curType.getGloss().trim());
        }
        
        if (!this.getDefinition().isEmpty()) {
            summary += " : " + WebInterface.getTextFromHtml(this.getDefinition()).trim();
        }
        
        return summary.replaceAll("\n", " ");
    }

    /**
     * @param _filterEtyParent the filterEtyParent to set
     */
    public void setFilterEtyParent(Object _filterEtyParent) {
        this.filterEtyParent = _filterEtyParent;
    }
    
    public String getWordForm(String fullDecId) {
        String ret = "ERROR!";
        
        if (core != null) {
            ConjugationManager conMan = core.getConjugationManager();

            if (isOverrideAutoConjugate()) {
                ConjugationNode node = conMan.getConjugationByCombinedId(this.id, fullDecId);
                ret = node == null ? "" : node.getValue();
            } else {
                try {
                    ret = conMan.declineWord(this, fullDecId);
                } catch (Exception e) {
                    core.getOSHandler().getIOHandler().writeErrorLog(e);
                }
            }
        }

        return ret;
    }

    public void writeXML(Document doc, Element rootElement) {
        Element wordNode = doc.createElement(PGTUtil.WORD_XID);

        Element wordValue = doc.createElement(PGTUtil.WORD_ID_XID);
        Integer wordId = this.getId();
        wordValue.appendChild(doc.createTextNode(wordId.toString()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.LOCALWORD_XID);
        wordValue.appendChild(doc.createTextNode(this.localWord));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.CONWORD_XID);
        wordValue.appendChild(doc.createTextNode(this.getValue()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.WORD_POS_ID_XID);
        wordValue.appendChild(doc.createTextNode(this.getWordTypeId().toString()));
        wordNode.appendChild(wordValue);

        try {
            wordValue = doc.createElement(PGTUtil.WORD_PROC_XID);
            wordValue.appendChild(doc.createTextNode(this.getPronunciation()));
            wordNode.appendChild(wordValue);
        }
        catch (Exception e) {
            // Do nothing. Users are made aware of this issue elsewhere.
            // IOHandler.writeErrorLog(e);
        }

        wordValue = doc.createElement(PGTUtil.WORD_DEF_XID);
        wordValue.appendChild(doc.createTextNode(WebInterface.archiveHTML(this.definition, core)));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.WORD_PROCOVERRIDE_XID);
        wordValue.appendChild(doc.createTextNode(this.procOverride ? PGTUtil.TRUE : PGTUtil.FALSE));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.WORD_AUTODECLOVERRIDE_XID);
        wordValue.appendChild(doc.createTextNode(this.autoConjugationOverride ? PGTUtil.TRUE : PGTUtil.FALSE));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.WORD_RULEOVERRIDE_XID);
        wordValue.appendChild(doc.createTextNode(this.rulesOverride ? PGTUtil.TRUE : PGTUtil.FALSE));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.WORD_CLASSCOLLECTION_XID);
        for (Entry<Integer, Integer> entry : this.getClassValues()) {
            Element classVal = doc.createElement(PGTUtil.WORD_CLASS_AND_VALUE_XID);
            classVal.appendChild(doc.createTextNode(entry.getKey() + "," + entry.getValue()));
            wordValue.appendChild(classVal);
        }
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.WORD_CLASS_TEXT_VAL_COLLECTION_XID);
        for (Entry<Integer, String> entry : this.getClassTextValues()) {
            Element classVal = doc.createElement(PGTUtil.WORD_CLASS_TEXT_VAL_XID);
            classVal.appendChild(doc.createTextNode(entry.getKey() + "," + entry.getValue()));
            wordValue.appendChild(classVal);
        }
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.WORD_ETY_NOTES_XID);
        wordValue.appendChild(doc.createTextNode(this.etymNotes));
        wordNode.appendChild(wordValue);

        rootElement.appendChild(wordNode);
    }
}
