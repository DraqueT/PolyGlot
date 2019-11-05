/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.DeclensionDimension;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.DeclensionGenRule;
import org.darisadesigns.polyglotlina.Nodes.DeclensionGenTransform;
import org.darisadesigns.polyglotlina.Nodes.DeclensionNode;
import org.darisadesigns.polyglotlina.Nodes.DeclensionPair;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
public class DeclensionManager {

    private final DictCore core;
    private final List<String> decGenDebug = new ArrayList<>();

    public DeclensionManager(DictCore _core) {
        core = _core;
    }

    // Integer is ID of related word, list is list of declension nodes
    private final Map<Integer, List<DeclensionNode>> dList = new HashMap<>();

    // Integer is ID of related type, list is list of declensions for this type
    private final Map<Integer, List<DeclensionNode>> dTemplates = new HashMap<>();

    // If specific combined declensions require additional settings in the future,
    // change the boolean here to an object which will store them
    private final Map<String, Boolean> combSettings = new HashMap<>();

    private Integer topId = 0;
    private boolean bufferDecTemp = false;
    private Integer bufferRelId = -1;
    private DeclensionNode buffer = new DeclensionNode(-1);
    private final Map<Integer, List<DeclensionGenRule>> generationRules = new HashMap<>();
    private DeclensionGenRule ruleBuffer = new DeclensionGenRule();

    public boolean isCombinedDeclSurpressed(String _combId, Integer _typeId) {
        String storeId = _typeId + "," + _combId;

        if (!combSettings.containsKey(storeId)) {
            return false;
        }

        return combSettings.get(storeId);
    }

    public void setCombinedDeclSuppressed(String _combId, Integer _typeId, boolean _suppress) {
        String storeId = _typeId + "," + _combId;

        if (combSettings.containsKey(storeId)) {
            combSettings.replace(storeId, _suppress);
        } else {
            combSettings.put(storeId, _suppress);
        }
    }

    /**
     * This sets the suppression data raw. Should only be used when loading from
     * a file
     *
     * @param _completeId complete, raw ID of data
     * @param _suppress suppression value
     */
    public void setCombinedDeclSuppressedRaw(String _completeId, boolean _suppress) {
        combSettings.put(_completeId, _suppress);
    }

    /**
     * Gets list of all deprecated autogeneration rules
     *
     * @param typeId type to get deprecated values for
     * @return list of all deprecated gen rules
     */
    public List<DeclensionGenRule> getAllDepGenerationRules(int typeId) {
        List<DeclensionGenRule> ret = new ArrayList<>();
        List<DeclensionPair> typeRules = getAllCombinedIds(typeId);
        Map<String, Integer> ruleMap = new HashMap<>();

        // creates searchable map of extant combination IDs
        typeRules.forEach((curPair) -> {
            ruleMap.put(curPair.combinedId, 0);
        });

        int highestIndex = 0;
        for (List<DeclensionGenRule> list : generationRules.values()) {
            for (DeclensionGenRule curRule : list) {
                int curRuleIndex = curRule.getIndex();
                highestIndex = highestIndex > curRuleIndex ? highestIndex : curRuleIndex;
            }
        }

        for (List<DeclensionGenRule> list : generationRules.values()) {
            for (DeclensionGenRule curRule : list) {
                // adds to return value only if rule matches ID but is orphaned
                if (curRule.getIndex() == 0) {
                    highestIndex++;
                    curRule.setIndex(highestIndex);
                }

                if (curRule.getTypeId() == typeId
                        && !ruleMap.containsKey(curRule.getCombinationId())) {
                    ret.add(curRule);
                }
            }
        }
        
        Collections.sort(ret);

        return ret;
    }

    /**
     * Gets current declension rule buffer
     *
     * @return current declension rule buffer
     */
    public DeclensionGenRule getRuleBuffer() {
        return ruleBuffer;
    }

    /**
     * inserts current rule buffer and sets to blank value
     */
    public void insRuleBuffer() {
        addDeclensionGenRule(ruleBuffer);
        ruleBuffer = new DeclensionGenRule();
    }

    /**
     * add a declension generation rule to the list
     *
     * @param newRule rule to add
     */
    public void addDeclensionGenRule(DeclensionGenRule newRule) {
        int typeId = newRule.getTypeId();
        List<DeclensionGenRule> rules;

        if (generationRules.containsKey(typeId)) {
            rules = generationRules.get(typeId);
        } else {
            rules = new ArrayList<>();
            generationRules.put(typeId, rules);
        }

        rules.add(newRule);
    }

    /**
     * delete all rules of a particular typeID from rule set
     *
     * @param typeId ID of type to wipe
     */
    public void wipeDeclensionGenRules(int typeId) {
        generationRules.remove(typeId);
    }

    /**
     * Ensures all rules have contiguous indices. Run prior to generating XML for save
     */
    private void smoothRules() {
        generationRules.values().forEach((ruleList) -> {
            Collections.sort(ruleList);
            int newIndex = 1;
            
            for (DeclensionGenRule rule : ruleList) {
                rule.setIndex(newIndex);
                
                newIndex++;
            }
        });
    }
    
    /**
     * Deletes rule based on unique regex value
     *
     * @param delRule rule to delete
     */
    public void deleteDeclensionGenRule(DeclensionGenRule delRule) {
        int typeId = delRule.getTypeId();

        if (generationRules.containsKey(typeId)) {
            generationRules.get(typeId).remove(delRule);
        }
    }

    /**
     * Deletes all DeclensionGenRule entries for a given POS/declension pairing.
     *
     * @param typeId
     * @param combinedId
     */
    public void deleteDeclensionGenRules(int typeId, String combinedId) {
        if (generationRules.containsKey(typeId)) {
            List<DeclensionGenRule> rules = generationRules.get(typeId);
            List<DeclensionGenRule> iter = new ArrayList<>(rules);
            
            // iterate on copy of array to avoid concurrent modification
            for (DeclensionGenRule rule : iter) {
                if (rule.getCombinationId().equals(combinedId)) {
                    rules.remove(rule);
                }
            }
        }
    }

    /**
     * get list of all declension rules for a particular type
     *
     * @param typeId id of part of speech to collect all rules for (does not
     * account for class filtering)
     * @return list of rules
     */
    public List<DeclensionGenRule> getDeclensionRulesForType(int typeId) {
        List<DeclensionGenRule> ret;

        if (generationRules.containsKey(typeId)) {
            ret = generationRules.get(typeId);
        } else {
            ret = new ArrayList<>();
        }

        Collections.sort(ret);

        return ret;
    }
    
    /**
     * get list of all declension rules for a particular type
     *
     * @param typeId id of part of speech to collect all rules for (does not
     * @param combinedId combined ID of rules to select
     * account for class filtering)
     * @return list of rules
     */
    public List<DeclensionGenRule> getDeclensionRulesForTypeAndCombId(int typeId, String combinedId) {
        List<DeclensionGenRule> ret = new ArrayList<>();
        List<DeclensionGenRule> typeRules = getDeclensionRulesForType(typeId);
        
        for (DeclensionGenRule rule : typeRules) {
            if (rule.getCombinationId().equals(combinedId)) {
                ret.add(rule);
            }
        }
        
        Collections.sort(ret);
        
        return ret;
    }

    /**
     * get list of all declension rules for a given word based on word type and
     * word class values
     *
     * @param word word to get rules for (takes into account word type (PoS) &
     * classes/class values it has
     * @return list of rules
     */
    public List<DeclensionGenRule> getDeclensionRules(ConWord word) {
        List<DeclensionGenRule> ret = new ArrayList<>();
        int typeId = word.getWordTypeId();
        
        if (generationRules.containsKey(typeId)) {
            List<DeclensionGenRule> decRules = generationRules.get(word.getWordTypeId());
            int missingId = 0; //used for missing index values (index system bolton)

            for (DeclensionGenRule curRule : decRules) {
                if (curRule.getIndex() == 0 || curRule.getIndex() == missingId) {
                    missingId++;
                    curRule.setIndex(missingId);
                } else {
                    missingId = curRule.getIndex();
                }

                ret.add(curRule);
            }
        }

        Collections.sort(ret);

        // ensure that all rules cave contiguous IDs before returning
        int i = 1;
        for (DeclensionGenRule curRule : ret) {
            curRule.setIndex(i);
            i++;
        }

        return ret;
    }

    /**
     * Generates the new form of a declined/conjugated word based on rules for
     * its type
     *
     * @param word to transform
     * @param combinedId combined ID of word form to create
     * @return new word value if exists, empty string otherwise
     * @throws java.lang.Exception on bad regex
     */
    public String declineWord(ConWord word, String combinedId) throws Exception {
        List<DeclensionGenRule> rules = getDeclensionRules(word);
        decGenDebug.clear();
        decGenDebug.add("APPLIED RULES BREAKDOWN:\n");
        String ret = word.getValue();

        for (DeclensionGenRule curRule : rules) {
            boolean ruleAppliesCombId = curRule.getCombinationId().equals(combinedId);
            boolean ruleAppliesToWord = curRule.doesRuleApplyToWord(word);
            
            String debugString = "--------------------------------------\n";
            
            // skip all entries not applicable to this particular combined word ID
            if (!ruleAppliesCombId) {
                continue;
            } else if (!ruleAppliesToWord) {
                debugString += curRule.getDebugString();
                decGenDebug.add(debugString);
                continue;
            }
            
            debugString += curRule.getDebugString();

            List<DeclensionGenTransform> transforms = curRule.getTransforms();

            for (DeclensionGenTransform curTrans : transforms) {
                try {
                    String orig = ret;
                    ret = ret.replaceAll(curTrans.regex, curTrans.replaceText);
                    debugString += "    -------------------------\n"
                            + "    Transformation:\n"
                            + "        Regex: \"" + curTrans.regex + "\"\n"
                            + "        Text: \"" + curTrans.replaceText + "\"\n" 
                            + "        Effect: " + orig + " -> " + ret + "\n";
                } catch (Exception e) {
                    throw new Exception("Unable to create declension/conjugation "
                            + "due to malformed regex (modify in Parts of Speech->Autogeneration): "
                            + e.getLocalizedMessage(), e);
                }
            }
            
            decGenDebug.add(debugString);
        }

        // if rules are empty, no transformation took place: return blank string
        ret = rules.isEmpty() ? "" : ret;
        return ret;
    }

    public Map<Integer, List<DeclensionNode>> getDeclensionMap() {
        return dList;
    }

    public void addDeclensionToWord(Integer wordId, Integer declensionId, DeclensionNode declension) {
        addDeclension(wordId, declensionId, declension, dList);
    }

    public void deleteDeclensionFromWord(Integer wordId, Integer declensionId) {
        deleteDeclension(wordId, declensionId, dList);
    }

    /**
     * sets all declensions to deprecated state
     *
     * @param typeId ID of type to deprecate declensions for
     */
    public void deprecateAllDeclensions(Integer typeId) {
        Iterator<Entry<Integer, List<DeclensionNode>>> decIt = dList.entrySet().iterator();

        while (decIt.hasNext()) {
            Entry<Integer, List<DeclensionNode>> curEntry = decIt.next();
            List<DeclensionNode> curList = curEntry.getValue();

            // only run for declensions of words with particular type
            if (!core.getWordCollection().getNodeById(curEntry.getKey()).getWordTypeId().equals(typeId)) {
                continue;
            }

            Iterator<DeclensionNode> nodeIt = curList.iterator();

            while (nodeIt.hasNext()) {
                DeclensionNode curNode = nodeIt.next();

                curNode.setCombinedDimId("D" + curNode.getCombinedDimId());
            }
        }
    }

    /**
     * Gets a particular declension template of a particular word type
     *
     * @param typeId the type which contains the declension in question
     * @param declensionId the declension within the type to retrieve
     * @return the object representing the declension
     */
    public DeclensionNode getDeclension(Integer typeId, Integer declensionId) {
        DeclensionNode ret = null;
        List<DeclensionNode> decList = dTemplates.get(typeId);

        // only search farther if declension itself actually exists
        if (decList != null) {
            Iterator<DeclensionNode> decIt = decList.iterator();

            while (decIt.hasNext()) {
                DeclensionNode curNode = decIt.next();

                if (curNode.getId().equals(declensionId)) {
                    ret = curNode;
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * Clears all declensions from word
     *
     * @param wordId ID of word to clear of all declensions
     */
    public void clearAllDeclensionsWord(Integer wordId) {
        clearAllDeclensions(wordId, dList);
    }

    /**
     * get list of all labels and combined IDs of all declension combinations
     * for a type
     *
     * @param typeId ID of type to fetch combined IDs for
     * @return list of labels and IDs
     */
    public List<DeclensionPair> getAllCombinedIds(Integer typeId) {
        List<DeclensionNode> dimensionalDeclensionNodes = getDimensionalDeclensionListTemplate(typeId);
        List<DeclensionNode> singletonDeclensionNodes = getSingletonDeclensionList(typeId, dTemplates);
        List<DeclensionPair> ret = getAllCombinedDimensionalIds(0, ",", "", dimensionalDeclensionNodes);
        ret.addAll(getAllSingletonIds(singletonDeclensionNodes));

        return ret;
    }

    /**
     * get list of all labels and combined IDs of dimensional declension
     * combinations for a type
     *
     * @param typeId ID of type to fetch combined IDs for
     * @return list of labels and IDs
     */
    public List<DeclensionPair> getDimensionalCombinedIds(Integer typeId) {
        List<DeclensionNode> dimensionalDeclensionNodes = getDimensionalDeclensionListTemplate(typeId);
        return getAllCombinedDimensionalIds(0, ",", "", dimensionalDeclensionNodes);
    }

    /**
     * get list of all labels and combined IDs of singleton declension
     * combinations for a type
     *
     * @param typeId ID of type to fetch combined IDs for
     * @return list of labels and IDs
     */
    public List<DeclensionPair> getSingletonCombinedIds(Integer typeId) {
        List<DeclensionNode> singletonDeclensionNodes = getSingletonDeclensionList(typeId, dTemplates);
        return getAllSingletonIds(singletonDeclensionNodes);
    }

    public List<DeclensionPair> getAllSingletonIds(List<DeclensionNode> declensionList) {
        List<DeclensionPair> ret = new ArrayList<>();

        declensionList.forEach((curNode) -> {
            DeclensionPair curPair = new DeclensionPair(curNode.getCombinedDimId(), curNode.getValue());
            ret.add(curPair);
        });

        return ret;
    }

    /**
     * Gets the location of a dimension's id in a dimensionalID string
     *
     * @param typeId part of speech associated with dimension
     * @param node dimension to find
     * @return locational index within dimensional ids (-1 if no value found)
     */
    public int getDimensionTemplateIndex(int typeId, DeclensionNode node) {
        int ret = -1;

        if (dTemplates.containsKey(typeId)) {
            List<DeclensionNode> declensionValues = dTemplates.get(typeId);

            ret = declensionValues.indexOf(node);

            // must loop through due to inclusion of singleton declensions here
            int numSingleton = 0;
            for (DeclensionNode testNode : declensionValues) {
                if (testNode.isDimensionless()) {
                    numSingleton++;
                }
                if (node.getId().equals(testNode.getId())) {
                    break;
                }
            }

            ret -= numSingleton;
        }

        return ret;
    }

    /**
     * Same as above, but SKIPS indices of singleton declensions
     *
     * @param typeId
     * @param index
     * @return null if none found
     */
    public DeclensionNode getDimensionalDeclensionTemplateByIndex(int typeId, int index) {
        DeclensionNode ret = null;
        List<DeclensionNode> nodes = dTemplates.get(typeId);

        int curIndex = 0;
        for (DeclensionNode node : nodes) {
            if (node.isDimensionless()) {
                continue;
            } else if (curIndex == index) {
                ret = node;
                break;
            }
            curIndex++;
        }

        return ret;
    }

    /**
     * recursive method to calculate value of overridden method
     *
     * @param depth current depth in calculation
     * @param curId current combined ID
     * @param curLabel current constructed label
     * @param declensionList list of template declensions for type
     * @return list of currently constructed labels and ids
     */
    private List<DeclensionPair> getAllCombinedDimensionalIds(int depth, String curId, String curLabel, List<DeclensionNode> declensionList) {
        List<DeclensionPair> ret = new ArrayList<>();

        // for the specific case that a word with no declension patterns has a deprecated declension
        if (declensionList.isEmpty()) {
            return ret;
        }

        if (depth >= declensionList.size()) {
            ret.add(new DeclensionPair(curId, curLabel));
        } else {

            DeclensionNode curNode = declensionList.get(depth);
            Collection<DeclensionDimension> dimensions = curNode.getDimensions();
            Iterator<DeclensionDimension> dimIt = dimensions.iterator();

            while (dimIt.hasNext()) {
                DeclensionDimension curDim = dimIt.next();

                ret.addAll(getAllCombinedDimensionalIds(depth + 1, curId + curDim.getId() + ",",
                        curLabel + (curLabel.isEmpty() ? "" : " ") + curDim.getValue(), declensionList));
            }
        }

        return ret;
    }

    // TODO: Do I need this at all? Can I have ONLY the full pull, rather than including dimensional?
    /**
     * Fetches list of declined/conjugated wordforms for a given word. Only
     * pulls dimensional values. Singletons like gerunds are not included Note:
     * This DOES include deprecated wordforms! Be aware!
     *
     * @param wordId
     * @return
     */
    public List<DeclensionNode> getDimensionalDeclensionListWord(Integer wordId) {
        return getDimensionalDeclensionList(wordId, dList);
    }

    /**
     * Gets list of dimensional template values. Does not pull singletons such
     * as gerunds.
     *
     * @param typeId
     * @return
     */
    public List<DeclensionNode> getDimensionalDeclensionListTemplate(Integer typeId) {
        return getDimensionalDeclensionList(typeId, dTemplates);
    }

    /**
     * Gets full list of dimensional template values including singletons such
     * as gerunds.
     *
     * @param typeId
     * @return
     */
    public List<DeclensionNode> getFullDeclensionListTemplate(Integer typeId) {
        return getFullDeclensionList(typeId, dTemplates);
    }

    public DeclensionNode addDeclensionToTemplate(Integer typeId, Integer declensionId, DeclensionNode declension) {
        return addDeclension(typeId, declensionId, declension, dTemplates);
    }

    public DeclensionNode addDeclensionToTemplate(Integer typeId, String declension) {
        return addDeclension(typeId, declension, dTemplates);
    }

    public void deleteDeclensionFromTemplate(Integer typeId, Integer declensionId) {
        deleteDeclension(typeId, declensionId, dTemplates);
    }

    public void updateDeclensionTemplate(Integer typeId, Integer declensionId, DeclensionNode declension) {
        updateDeclension(typeId, declensionId, declension, dTemplates);
    }

    public DeclensionNode getDeclensionTemplate(Integer typeId, Integer templateId) {
        List<DeclensionNode> searchList = dTemplates.get(typeId);
        Iterator search = searchList.iterator();
        DeclensionNode ret = null;

        while (search.hasNext()) {
            DeclensionNode test = (DeclensionNode) search.next();

            if (test.getId().equals(templateId)) {
                ret = test;
                break;
            }
        }

        return ret;
    }

    public void setBufferId(Integer _bufferId) {
        buffer.setId(_bufferId);
    }

    public void setBufferDecText(String _bufferDecText) {
        buffer.setValue(_bufferDecText);
    }

    public String getBufferDecText() {
        return buffer.getValue();
    }

    public void setBufferDecNotes(String _bufferDecNotes) {
        buffer.setNotes(_bufferDecNotes);
    }

    public String getBufferDecNotes() {
        return buffer.getNotes();
    }

    public void setBufferDecTemp(boolean _bufferDecTemp) {
        bufferDecTemp = _bufferDecTemp;
    }

    public void setBufferRelId(Integer _bufferRelId) {
        bufferRelId = _bufferRelId;
    }

    public Integer getBufferRelId() {
        return bufferRelId;
    }

    public boolean isBufferDecTemp() {
        return bufferDecTemp;
    }

    public void insertBuffer() {
        if (bufferDecTemp) {
            this.addDeclensionToTemplate(bufferRelId, buffer.getId(), buffer);
        } else {
            this.addDeclensionToWord(bufferRelId, buffer.getId(), buffer);
        }
    }

    /**
     * gets current declension node buffer object
     *
     * @return buffer node object
     */
    public DeclensionNode getBuffer() {
        return buffer;
    }

    public void clearBuffer() {
        buffer = new DeclensionNode(-1);
        bufferDecTemp = false;
        bufferRelId = -1;
    }

    private DeclensionNode addDeclension(Integer typeId, String declension, Map<Integer, List<DeclensionNode>> idToDecNodes) {
        List<DeclensionNode> wordList;

        topId++;

        if (idToDecNodes.containsKey(typeId)) {
            wordList = idToDecNodes.get(typeId);
        } else {
            wordList = new ArrayList<>();
            idToDecNodes.put(typeId, wordList);
        }

        DeclensionNode addNode = new DeclensionNode(topId);
        addNode.setValue(declension);

        wordList.add(addNode);

        return addNode;
    }

    /**
     * Adds declension to related object (type or word)
     *
     * @param relId ID of related object
     * @param declensionId ID of declension to be created
     * @param declension declension node to be created
     * @param list list to add node to (word list or type list)
     * @return declension node created
     */
    private DeclensionNode addDeclension(Integer relId, Integer declensionId, DeclensionNode declension, Map<Integer, List<DeclensionNode>> list) {
        List<DeclensionNode> wordList;

        if (declensionId == -1) {
            declensionId = topId + 1;
        }

        deleteDeclensionFromWord(relId, declensionId);

        if (list.containsKey(relId)) {
            wordList = list.get(relId);
        } else {
            wordList = new ArrayList<>();
            list.put(relId, wordList);
        }

        DeclensionNode addNode = new DeclensionNode(declensionId);
        addNode.setEqual(declension);

        wordList.add(addNode);

        if (declensionId > topId) {
            topId = declensionId;
        }

        return addNode;
    }

    /**
     * Gets stored declension for a word from combined dimensional Id of
     * declension. This does NOT generate a new declension, and is primarily of
     * use with overridden values and language files which do not use
     * autodeclension.
     *
     * @param wordId the id of the root word
     * @param dimId the combined dim Id of the dimension
     * @return The declension node if found, null if otherwise
     */
    public DeclensionNode getDeclensionByCombinedId(Integer wordId, String dimId) {
        DeclensionNode ret = null;

        if (dList.containsKey(wordId)) {
            for (DeclensionNode test : dList.get(wordId)) {
                if (dimId.equals(test.getCombinedDimId())) {
                    ret = test;
                    break;
                }
            }
        }

        return ret;
    }

    public String getCombNameFromCombId(int typeId, String combId) {
        String ret = "";
        Iterator<DeclensionNode> it = getDimensionalDeclensionListTemplate(typeId).iterator();
        String[] splitIds = combId.split(",");

        for (int i = 0; it.hasNext(); i++) {
            DeclensionNode curNode = it.next();
            int dimId = Integer.parseInt(splitIds[i + 1]);
            Iterator<DeclensionDimension> dimIt = curNode.getDimensions().iterator();
            DeclensionDimension curDim = null;

            while (dimIt.hasNext()) {
                curDim = dimIt.next();

                if (curDim.getId().equals(dimId)) {
                    break;
                }
            }

            if (curDim != null) {
                ret += " " + curDim.getValue();
            }
        }

        return ret.trim();
    }

    public void deleteDeclension(Integer typeId, Integer declensionId, Map<Integer, List<DeclensionNode>> list) {
        if (list.containsKey(typeId)) {
            List<DeclensionNode> copyTo = new ArrayList<>();
            Iterator<DeclensionNode> copyFrom = list.get(typeId).iterator();

            while (copyFrom.hasNext()) {
                DeclensionNode curNode = copyFrom.next();

                if (curNode.getId().equals(declensionId)) {
                    continue;
                }

                copyTo.add(curNode);
            }

            list.remove(typeId);

            // if unpopulated, allow to not exist. Cleaner.
            if (!copyTo.isEmpty()) {
                list.put(typeId, copyTo);
            }
        }
    }

    private void updateDeclension(Integer typeId,
            Integer declensionId,
            DeclensionNode declension,
            Map<Integer, List<DeclensionNode>> list) {
        if (list.containsKey(typeId)) {
            List<DeclensionNode> copyTo = new ArrayList<>();
            Iterator<DeclensionNode> copyFrom = list.get(typeId).iterator();

            while (copyFrom.hasNext()) {
                DeclensionNode curNode = copyFrom.next();

                if (curNode.getId().equals(declensionId)) {
                    DeclensionNode modified = new DeclensionNode(declensionId);
                    modified.setEqual(declension);
                    copyTo.add(modified);
                    continue;
                }

                copyTo.add(curNode);
            }

            list.remove(typeId);
            list.put(typeId, copyTo);
        }
    }

    /**
     * Clears all declensions from word
     *
     * @param wordId ID of word to clear of all declensions
     */
    private void clearAllDeclensions(Integer wordId, Map list) {
        list.remove(wordId);
    }

    /**
     * Retrieves all dimensional declensions based on related ID and the list to
     * be pulled from. The list can either be the templates (related via typeId)
     * or actual words, related by wordId
     *
     * @param relatedId ID of related value
     * @param valueMap list of relations to search through
     * @return
     */
    private List<DeclensionNode> getDimensionalDeclensionList(Integer relatedId,
            Map<Integer, List<DeclensionNode>> valueMap) {
        List<DeclensionNode> ret = new ArrayList<>();

        if (valueMap.containsKey(relatedId)) {
            List<DeclensionNode> allNodes = valueMap.get(relatedId);

            allNodes.forEach((curNode) -> {
                // dimensionless nodes
                if (!curNode.isDimensionless()) {
                    ret.add(curNode);
                }
            });
        }

        return ret;
    }

    /**
     * Public version of private method directly below. Retrieves all singleton
     * declensions based on related ID and the list to be pulled from. The list
     * can either be the templates (related via typeId) or actual words, related
     * by wordId
     *
     * @param relatedId ID of related value
     * @return
     */
    public List<DeclensionNode> getSingletonDeclensionList(Integer relatedId) {
        return getSingletonDeclensionList(relatedId, dTemplates);
    }

    /**
     * Retrieves all singleton declensions based on related ID and the list to
     * be pulled from. The list can either be the templates (related via typeId)
     * or actual words, related by wordId
     *
     * @param relatedId ID of related value
     * @param list list of relations to search through
     * @return
     */
    private List<DeclensionNode> getSingletonDeclensionList(Integer relatedId,
            Map<Integer, List<DeclensionNode>> list) {
        List<DeclensionNode> ret = new ArrayList<>();

        if (list.containsKey(relatedId)) {
            List<DeclensionNode> allNodes = list.get(relatedId);

            allNodes.forEach((curNode) -> {
                // dimensionless nodes
                if (curNode.isDimensionless()) {
                    ret.add(curNode);
                }
            });
        }

        return ret;
    }

    /**
     * Returns full list of declensions irrespective of whether they are
     * dimensional or not. Will return singletons such as gerunds.
     *
     * @param relatedId ID of related value
     * @param list list of relations to search through
     * @return
     */
    private List<DeclensionNode> getFullDeclensionList(Integer relatedId, Map<Integer, List<DeclensionNode>> list) {
        List<DeclensionNode> ret = new ArrayList<>();

        if (list.containsKey(relatedId)) {
            ret = list.get(relatedId);
        }

        return ret;
    }

    public List<DeclensionNode> getFullDeclensionListWord(Integer wordId) {
        return getFullDeclensionList(wordId, dList);
    }

    /**
     * Gets a word's declensions, with their combined dim Ids as the keys DOES
     * NOT GENERATE DECLENSIONS THAT ARE SET TO AUTOGENERATE, BUT HAVE NOT YET
     * BEEN SAVED. Note: This returns deprecated wordforms as well as current
     * ones.
     *
     * @param wordId word to get declensions of
     * @return map of all declensions in a word (empty if none)
     */
    public Map<String, DeclensionNode> getWordDeclensions(Integer wordId) {
        Map<String, DeclensionNode> ret = new HashMap<>();

        Iterator<DeclensionNode> decs = getDimensionalDeclensionListWord(wordId).iterator();

        while (decs.hasNext()) {
            DeclensionNode curNode = decs.next();

            ret.put(curNode.getCombinedDimId(), curNode);
        }

        return ret;
    }

    /**
     * Removes all declensions contained in decMap from word with wordid
     *
     * @param wordId ID of word to clear values from
     * @param removeVals values to clear from word
     */
    public void removeDeclensionValues(Integer wordId, Collection<DeclensionNode> removeVals) {
        List<DeclensionNode> wordList = dList.get(wordId);

        removeVals.forEach((remNode) -> {
            wordList.remove(remNode);
        });
    }

    /**
     * Writes all declension information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Set<Entry<Integer, List<DeclensionNode>>> declensionSet;
        Element declensionCollection = doc.createElement(PGTUtil.DECLENSION_COLLECTION_XID);
        rootElement.appendChild(declensionCollection);
        
        // ensure rule IDs are contiguous before save
        this.smoothRules();

        // record declension templates
        declensionSet = dTemplates.entrySet();
        for (Entry<Integer, List<DeclensionNode>> e : declensionSet) {
            final Integer relatedId = e.getKey();

            e.getValue().forEach((curNode) -> {
                curNode.writeXMLTemplate(doc, declensionCollection, relatedId);
            });
        }

        // record word declensions
        declensionSet = dList.entrySet();
        for (Entry<Integer, List<DeclensionNode>> e : declensionSet) {
            final Integer relatedId = e.getKey();

            e.getValue().forEach((curNode) -> {
                curNode.writeXMLWordDeclension(doc, declensionCollection, relatedId);
            });
        }

        // record declension autogeneration rules
        generationRules.values().forEach((rules) -> {
            rules.forEach((rule) -> {
                rule.writeXML(doc, declensionCollection);
            });
        });

        // record combined form settings
        Element combinedForms = doc.createElement(PGTUtil.DEC_COMBINED_FORM_SECTION_XID);
        rootElement.appendChild(combinedForms);

        combSettings.entrySet().stream().map((pairs) -> {
            Element curCombForm = doc.createElement(PGTUtil.DEC_COMBINED_FORM_XID);
            Element curAttrib;
            // This section will have to be slightly rewritten if the combined settings become more complex
            curAttrib = doc.createElement(PGTUtil.DEC_COMBINED_ID_XID);
            curAttrib.appendChild(doc.createTextNode(pairs.getKey()));
            curCombForm.appendChild(curAttrib);
            curAttrib = doc.createElement(PGTUtil.DEC_COMBINED_SURPRESS_XID);
            curAttrib.appendChild(doc.createTextNode(pairs.getValue() ? PGTUtil.TRUE : PGTUtil.FALSE));
            curCombForm.appendChild(curAttrib);
            return curCombForm;
        }).forEachOrdered((curCombForm) -> {
            combinedForms.appendChild(curCombForm);
        });
    }

    /**
     * This copies a list of rules to the bottom of the list of all declension
     * templates for a given part of speech that share a declension (decId) with
     * the value defined by dimId
     *
     * NOTE: Only applies to dimensional declensions.Singletons must be copied
     * to manually.
     *
     * @param typeId Part of speech to target
     * @param decId declension dimension to target
     * @param dimId dimension value to target
     * @param rules rules to be copied
     * @param selfCombId The combined ID of the form this was initially called
     * from (do not copy duplicate of rule to self)
     */
    public void copyRulesToDeclensionTemplates(int typeId,
            int decId, int dimId,
            List<DeclensionGenRule> rules,
            String selfCombId) {
        List<DeclensionNode> allNodes = getDimensionalDeclensionListTemplate(typeId);
        List<DeclensionPair> decList = getAllCombinedDimensionalIds(0, ",", "", allNodes);

        decList.forEach((decPair) -> {
            // only copy rule if distinct from base word form && it matches the dimensional value matches
            if (!decPair.combinedId.equals(selfCombId) && combDimIdMatches(decId, dimId, decPair.combinedId)) {
                rules.forEach((rule) -> {
                    // insert rule
                    DeclensionGenRule newRule = new DeclensionGenRule();
                    newRule.setEqual(rule, false);
                    newRule.setTypeId(typeId);
                    newRule.setCombinationId(decPair.combinedId);
                    newRule.setIndex(0);

                    addDeclensionGenRule(newRule);

                    // call get rules for type (will automatically assign next highest index to rule
                    this.getAllDepGenerationRules(typeId);
                });
            }
        });
    }

    //deleteRulesFromDeclensionTemplates
    /**
     * This copies a list of rules to the bottom of the list of all declension
     * templates for a given part of speech that share a declension (decId) with
     * the value defined by dimId
     *
     * NOTE: Only applies to dimensional declensions.Singletons must be copied
     * to manually.
     *
     * @param typeId Part of speech to target
     * @param decId declension dimension to target
     * @param dimId dimension value to target
     * @param rulesToDelete rules to be deleted
     */
    public void deleteRulesFromDeclensionTemplates(int typeId,
            int decId, int dimId,
            List<DeclensionGenRule> rulesToDelete) {

        List<DeclensionGenRule> rules = new ArrayList<>(this.getDeclensionRulesForType(typeId));
        
        for (DeclensionGenRule rule : rules) {
            if (combDimIdMatches(decId, dimId, rule.getCombinationId())) {
                for (DeclensionGenRule ruleDelete : rulesToDelete) {
                    if (rule.valuesEqual(ruleDelete)) {
                        this.deleteDeclensionGenRule(rule);
                    }
                }
            }
        }
    }

    /**
     * Deletes ALL instances of a rule within a given word type
     *
     * @param typeId part of speech to clear
     * @param rulesToDelete rules in this pos to delete
     */
    public void bulkDeleteRuleFromDeclensionTemplates(int typeId, List<DeclensionGenRule> rulesToDelete) {
        List<DeclensionGenRule> rules = new ArrayList<>(this.getDeclensionRulesForType(typeId));

        for (DeclensionGenRule rule : rules) {
            for (DeclensionGenRule ruleDelete : rulesToDelete) {
                if (rule.valuesEqual(ruleDelete)) {
                    this.deleteDeclensionGenRule(rule);
                }
            }
        }
    }

    private boolean combDimIdMatches(int decId, int dimId, String combDimId) {
        String[] strIds = combDimId.split(",");
        String strId = strIds[decId + 1]; // account for leading comma
        int dimValId = Integer.parseInt(strId);
        return dimValId == dimId;
    }

    public String getDeclensionLabel(int typeId, int decId) {
        return dTemplates.get(typeId).get(decId).getValue();
    }

    public String getDeclensionValueLabel(int typeId, int decId, int decValId) {
        return dTemplates.get(typeId).get(decId).getDeclensionDimensionById(decValId).getValue();
    }

    /**
     * On load of older pgt files, must be called to maintain functionality of
     * declension rules
     */
    public void setAllDeclensionRulesToAllClasses() {
        generationRules.values().forEach((rules) -> {
            rules.forEach((rule) -> {
                rule.addClassToFilterList(-1, -1);
            });
        });
    }

    /**
     * Returns all saved yet deprecated wordforms of a word
     *
     * @param word
     * @return
     */
    public Map<String, DeclensionNode> getDeprecatedForms(ConWord word) {
        Map<String, DeclensionNode> ret = new HashMap<>();

        // first get all values that exist for this word
        getFullDeclensionListWord(word.getId()).forEach((node) -> {
            ret.put(node.getCombinedDimId(), node);
        });

        // then remove all values which match existing combined type ids
        getAllCombinedIds(word.getWordTypeId()).forEach((pair) -> {
            ret.remove(pair.combinedId);
        });

        return ret;
    }

    /**
     * Returns true if given word has deprecated wordforms
     *
     * @param word
     * @return
     */
    public boolean wordHasDeprecatedForms(ConWord word) {
        return !getDeprecatedForms(word).isEmpty();
    }
    
    /**
     * Fetches debug values for the most recently created declension
     * @return 
     */
    public List<String> getDecGenDebug() {
        return new ArrayList<>(decGenDebug);
    }
}
