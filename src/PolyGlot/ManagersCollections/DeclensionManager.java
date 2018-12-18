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
package PolyGlot.ManagersCollections;

import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.DeclensionDimension;
import PolyGlot.DictCore;
import PolyGlot.Nodes.DeclensionGenRule;
import PolyGlot.Nodes.DeclensionGenTransform;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.Nodes.DeclensionPair;
import PolyGlot.PGTUtil;
import PolyGlot.Nodes.TypeNode;
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
    private final List<DeclensionGenRule> generationRules = new ArrayList<>();
    private DeclensionGenRule ruleBuffer = new DeclensionGenRule();

    public boolean isCombinedDeclSurpressed(String _combId, Integer _typeId) {
        String storeId = _typeId.toString() + "," + _combId;
        
        if (!combSettings.containsKey(storeId)) {
            return false;
        }

        return combSettings.get(storeId);
    }

    public void setCombinedDeclSurpressed(String _combId, Integer _typeId, boolean _surpress) {
        String storeId = _typeId.toString() + "," + _combId;
        
        if (!combSettings.containsKey(storeId)) {
            combSettings.put(storeId, _surpress);
        } else {
            combSettings.replace(storeId, _surpress);
        }
    }
    
    /**
     * This sets the surpression data raw. Should only be used when loading from a file
     * @param _completeId complete, raw ID of data
     * @param _surpress surpression value
     */
    public void setCombinedDeclSurpressedRaw(String _completeId, boolean _surpress) {
        combSettings.put(_completeId, _surpress);
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

        // adds to return value only if rule matches ID, and is orphaned
        int missingId = 0; //used for missing index values (index system bolton)
        for (DeclensionGenRule curRule : generationRules) {
            if (curRule.getIndex() <= missingId) {
                missingId++;
                curRule.setIndex(missingId);
            } else {
                missingId = curRule.getIndex();
            }
            
            if (curRule.getTypeId() == typeId
                    && !ruleMap.containsKey(curRule.getCombinationId())) {
                ret.add(curRule);
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
        generationRules.add(newRule);
    }

    /**
     * delete all rules of a particular typeID from rule set
     *
     * @param typeId ID of type to wipe
     */
    public void wipeDeclensionGenRules(int typeId) {
        List<DeclensionGenRule> rulesList = new ArrayList<>(generationRules);
        
        rulesList.forEach((rule) -> {
            if (rule.getTypeId() == typeId) {
                generationRules.remove(rule);
            }
        });
    }

    /**
     * Deletes rule based on unique regex value
     *
     * @param delRule rule to delete
     */
    public void deleteDeclensionGenRule(DeclensionGenRule delRule) {
        generationRules.remove(delRule);
    }

    /**
     * get list of all declension rules for a particular type
     *
     * @param typeId id of part of speech to collect all rules for (does not account for class filtering)
     * @return list of rules
     */
    public List<DeclensionGenRule> getDeclensionRulesForType(int typeId) {
        List<DeclensionGenRule> ret = new ArrayList<>();
        List<DeclensionGenRule> itRules = generationRules;
        int missingId = 0; //used for missing index values (index system bolton)
        
        for(DeclensionGenRule curRule : itRules) {
            if (curRule.getIndex() == 0 || curRule.getIndex() == missingId) {
                missingId++;
                curRule.setIndex(missingId);
            } else {
                missingId = curRule.getIndex();
            }

            if (curRule.getTypeId() == typeId) {
                ret.add(curRule);
            }
        }

        Collections.sort(ret);
        
        // ensure that all rules cave continguous IDs before returning
        int i = 1;
        for (DeclensionGenRule curRule : ret) {
            curRule.setIndex(i);
            i++;
        }
        
        return ret;
    }    
    
    /**
     * get list of all declension rules for a given word based on word type and word class values
     *
     * @param word word to get rules for (takes into account word type (PoS) & classes/class values it has
     * @return list of rules
     */
    public List<DeclensionGenRule> getDeclensionRules(ConWord word) {
        List<DeclensionGenRule> ret = new ArrayList<>();
        List<DeclensionGenRule> itRules = generationRules;
        int missingId = 0; //used for missing index values (index system bolton)
        
        for(DeclensionGenRule curRule : itRules) {
            if (curRule.getIndex() == 0 || curRule.getIndex() == missingId) {
                missingId++;
                curRule.setIndex(missingId);
            } else {
                missingId = curRule.getIndex();
            }

            if (curRule.doesRuleApplyToWord(word)) {
                ret.add(curRule);
            }
        }

        Collections.sort(ret);
        
        // ensure that all rules cave continguous IDs before returning
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
     * @param base base word string
     * @return new word value if exists, empty string otherwise
     * @throws java.lang.Exception on bad regex
     */
    public String declineWord(ConWord word, String combinedId, String base) throws Exception {
        Iterator<DeclensionGenRule> typeRules = getDeclensionRules(word).iterator();
        String ret = "";

        while (typeRules.hasNext()) {
            DeclensionGenRule curRule = typeRules.next();

            // skip all entries not applicable to this particular combined word ID
            if (!curRule.getCombinationId().equals(combinedId) || !curRule.doesRuleApplyToWord(word)) {
                continue;
            }

            // apply transforms within rule if rule matches current base
            if (base.matches(curRule.getRegex())) {
                List<DeclensionGenTransform> transforms = curRule.getTransforms();

                for (DeclensionGenTransform curTrans : transforms) {
                    try {
                        base = base.replaceAll(curTrans.regex, curTrans.replaceText);
                    } catch (Exception e) {
                        throw new Exception("Unable to create declension/conjugation "
                                + "due to malformed regex (modify in Parts of Speech->Autogeneration): " 
                                + e.getLocalizedMessage());
                    }

                    ret = base;
                }
            }
        }

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

    public void updateDeclensionWord(Integer wordId, Integer declensionId, DeclensionNode declension) {
        updateDeclension(wordId, declensionId, declension, dList);
    }

    /**
     * sets all declensions to deprecated state
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
        List<DeclensionNode> decList = (List<DeclensionNode>) dTemplates.get(typeId);

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
     * Recursive method to get all mandatory declensions for a type
     *
     * @param depth current dim depth
     * @param curId current generated id
     * @param retValue return list(passed by ref)
     * @param declensionList list of all declensions headers
     * @param mand whether any dimensions were mandatory
     * @return string list of all mandatory declensions
     */
    private List<DeclensionNode> getMandDims(int depth,
            String curId,
            List<DeclensionNode> retValue,
            List<DeclensionNode> declensionList,
            String label,
            boolean mand) {
        if (depth >= declensionList.size()) {
            if (mand) {
                DeclensionNode ret = new DeclensionNode(-1);
                ret.setCombinedDimId(curId);
                ret.setValue(label);
                retValue.add(ret);
            }

            return retValue;
        }

        DeclensionNode curNode = declensionList.get(depth);
        Collection<DeclensionDimension> dimensions = curNode.getDimensions();
        Iterator<DeclensionDimension> dimIt = dimensions.iterator();

        while (dimIt.hasNext()) {
            DeclensionDimension curDim = dimIt.next();

            getMandDims(depth + 1,
                    curId + curDim.getId().toString() + ",",
                    retValue,
                    declensionList,
                    label + " " + curDim.getValue(),
                    curDim.isMandatory() || mand);
        }

        return retValue;
    }

    /**
     * returns ids of all generated declensions for given type that are
     * mandatory
     *
     * @param typeId the type to get mandatories for
     * @return a list of all mandatory declensions
     */
    public List<DeclensionNode> getMandDims(Integer typeId) {
        return getMandDims(0, ",", new ArrayList<>(), getDimensionalDeclensionListTemplate(typeId), "", false);
    }

    /**
     * Tests whether type based requirements met for word
     *
     * @param word word to check
     * @param type type of word
     * @return empty if no problems, string with problem description otherwise
     */
    public String declensionRequirementsMet(ConWord word, TypeNode type) {
        String ret = "";
        // type will be null if no type (or bad type) on word, no type = no requirements
        if (type != null) {
            Iterator<DeclensionNode> mandIt = getMandDims(type.getId()).iterator();

            while (mandIt.hasNext()) {
                DeclensionNode curMand = mandIt.next();

                // skip surpressed forms
                if (isCombinedDeclSurpressed(curMand.getCombinedDimId(), type.getId())) {
                    continue;
                }

                DeclensionNode dimExists = getDeclensionByCombinedId(word.getId(), curMand.getCombinedDimId());

                if (dimExists == null) {
                    ret = "Required Decl/Conj " + curMand.getValue() + " must be filled in.";
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
     * get list of all labels and combined IDs of dimensional declension combinations
     * for a type
     *
     * @param typeId ID of type to fetch combined IDs for
     * @return list of labels and IDs
     */
    public List<DeclensionPair> getDimensionalCombinedIds(Integer typeId) {
        List<DeclensionNode> dimensionalDeclensionNodes = getDimensionalDeclensionListTemplate(typeId);
        return getAllCombinedDimensionalIds(0, ",", "", dimensionalDeclensionNodes);
    }
    
    /**
     * get list of all labels and combined IDs of singleton declension combinations
     * for a type
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
        
        declensionList.forEach((curNode)->{
            DeclensionPair curPair = new DeclensionPair(curNode.getCombinedDimId(), curNode.getValue());
            ret.add(curPair);
        });
        
        return ret; 
    }
    
    /**
     * Gets the location of a dimension's id in a dimensionalID string
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
                    numSingleton ++;
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
     * Gets a declension node based on positional index (rather than ID)
     * @param typeId
     * @param index
     * @return 
     */
    public DeclensionNode getDeclentionTemplateByIndex(int typeId, int index) {
        return dTemplates.get(typeId).get(index);
    }
    
    /**
     * Same as above, but SKIPS indecies of singleton declensions
     * @param typeId
     * @param index
     * @return null if none found
     */
    public DeclensionNode getDimensionalDeclentionTemplateByIndex(int typeId, int index) {
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

                ret.addAll(getAllCombinedDimensionalIds(depth + 1, curId + curDim.getId().toString() + ",",
                        curLabel + (curLabel.length() == 0 ? "" : " ") + curDim.getValue(), declensionList));
            }
        }

        return ret;
    }

    // TODO: Do I need this at all? Can I have ONLY the full pull, rather than including dimensional?
    /**
     * Fetches list of declined/conjugated wordforms for a given word. Only pulls dimensional values. Singletons like
     * gerunds are not included
     * @param wordId
     * @return 
     */
    public List<DeclensionNode> getDimensionalDeclensionListWord(Integer wordId) {
        return getDimensionalDeclensionList(wordId, dList);
    }

    /**
     * Gets list of dimensional template values. Does not pull singletons such as gerunds.
     * @param typeId
     * @return 
     */
    public List<DeclensionNode> getDimensionalDeclensionListTemplate(Integer typeId) {
        return getDimensionalDeclensionList(typeId, dTemplates);
    }
    
    /**
     * Gets full list of dimensional template values including singletons such as gerunds.
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
        List<DeclensionNode> searchList = (List<DeclensionNode>) dTemplates.get(typeId);
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

    /**
     * Clears all declensions from word
     *
     * @param typeId ID of word to clear of all declensions
     */
    public void clearAllDeclensionsTemplate(Integer typeId) {
        clearAllDeclensions(typeId, dTemplates);
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
        buffer.setMandatory(isBufferDecMandatory());

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
            wordList = (List<DeclensionNode>) idToDecNodes.get(typeId);
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
    private DeclensionNode addDeclension(Integer relId, Integer declensionId, DeclensionNode declension, Map list) {
        List wordList;

        if (declensionId == -1) {
            declensionId = topId + 1;
        }

        deleteDeclensionFromWord(relId, declensionId);

        if (list.containsKey(relId)) {
            wordList = (List) list.get(relId);
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
     * Gets stored declension for a word from combined dimensional Id of declension.
     * This does NOT generate a new declension, and is primarily of use with overridden
     * values and language files which do not use autodeclension.
     *
     * @param wordId the id of the root word
     * @param dimId the combined dim Id of the dimension
     * @return The declension node if found, null if otherwise
     */
    public DeclensionNode getDeclensionByCombinedId(Integer wordId, String dimId) {
        DeclensionNode ret = null;

        if (dList.containsKey(wordId)) {
            for (DeclensionNode test : (List<DeclensionNode>) dList.get(wordId)) {
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
            int dimId = Integer.parseInt(splitIds[i+1]);
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
    
    public void deleteDeclension(Integer typeId, Integer declensionId, Map list) {
        if (list.containsKey(typeId)) {
            List<DeclensionNode> copyTo = new ArrayList<>();
            Iterator<DeclensionNode> copyFrom = ((List) list.get(typeId)).iterator();

            while (copyFrom.hasNext()) {
                DeclensionNode curNode = copyFrom.next();

                if (curNode.getId().equals(declensionId)) {
                    continue;
                }

                copyTo.add(curNode);
            }

            list.remove(typeId);

            // if unpopulated, allow to not exist. Cleaner.
            if (copyTo.size() > 0) {
                list.put(typeId, copyTo);
            }
        }
    }

    private void updateDeclension(Integer typeId, Integer declensionId, DeclensionNode declension, Map list) {
        if (list.containsKey(typeId)) {
            List<DeclensionNode> copyTo = new ArrayList<>();
            Iterator<DeclensionNode> copyFrom = ((List) list.get(typeId)).iterator();

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
        if (list.containsKey(wordId)) {
            list.remove(wordId);
        }
    }

    /**
     * Retrieves all dimensional declensions based on related ID and the list to be pulled from. The list can either
     * be the templates (related via typeId) or actual words, related by wordId
     * @param relatedId ID of related value
     * @param valueMap list of relations to search through
     * @return 
     */
    private List<DeclensionNode> getDimensionalDeclensionList(Integer relatedId, 
            Map<Integer, List<DeclensionNode>> valueMap) {
        List<DeclensionNode> ret = new ArrayList<>();

        if (valueMap.containsKey(relatedId)) {
            List<DeclensionNode> allNodes = valueMap.get(relatedId);
            
            allNodes.forEach((curNode)->{
                // dimensionless nodes
                if (!curNode.isDimensionless()) {
                    ret.add(curNode);
                }
            });
        }

        return ret;
    }
    
    /**
     * Public version of private method directly below.
     * Retrieves all singleton declensions based on related ID and the list to be pulled from. The list can either
     * be the templates (related via typeId) or actual words, related by wordId
     * @param relatedId ID of related value
     * @return 
     */
    public List<DeclensionNode> getSingletonDeclensionList(Integer relatedId) {
        return getSingletonDeclensionList(relatedId, dTemplates);
    }
    
    /**
     * Retrieves all singleton declensions based on related ID and the list to be pulled from. The list can either
     * be the templates (related via typeId) or actual words, related by wordId
     * @param relatedId ID of related value
     * @param list list of relations to search through
     * @return 
     */
    private List<DeclensionNode> getSingletonDeclensionList(Integer relatedId, 
            Map<Integer, List<DeclensionNode>> list) {
        List<DeclensionNode> ret = new ArrayList<>();

        if (list.containsKey(relatedId)) {
            List<DeclensionNode> allNodes = list.get(relatedId);
            
            allNodes.forEach((curNode)->{
                // dimensionless nodes
                if (curNode.isDimensionless()) {
                    ret.add(curNode);
                }
            });
        }

        return ret;
    }
    
    /**
     * Returns full list of declensions irrespective of whether they are dimensional or not. Will return singletons
     * such as gerunds.
     * @param relatedId ID of related value
     * @param list list of relations to search through
     * @return 
     */
    private List<DeclensionNode> getFullDeclensionList(Integer relatedId, Map list) {
        List<DeclensionNode> ret = new ArrayList<>();

        if (list.containsKey(relatedId)) {
            ret = (List<DeclensionNode>) list.get(relatedId);
        }

        return ret;
    }
    
    public List<DeclensionNode> getFullDeclensionListWord(Integer wordId) {
        return getFullDeclensionList(wordId, dList);
    }

    /**
     * @return the bufferDecMandatory
     */
    public boolean isBufferDecMandatory() {
        return buffer.isMandatory();
    }

    /**
     * @param bufferDecMandatory the bufferDecMandatory to set
     */
    public void setBufferDecMandatory(boolean bufferDecMandatory) {
        buffer.setMandatory(bufferDecMandatory);
    }

    /**
     * Gets a word's declensions, with their combined dim Ids as the keys
     * DOES NOT GENERATE DECLENSIONS THAT ARE SET TO AUTOGENERATE, BUT HAVE
     * NOT YET BEEN SAVED.
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
     * @param wordId ID of word to clear values from
     * @param removeVals values to clear from word
     */
    public void removeDeclensionValues(Integer wordId, Collection<DeclensionNode> removeVals) {
        List<DeclensionNode> wordList = (List<DeclensionNode>) dList.get(wordId);
        
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
        Element declensionCollection = doc.createElement(PGTUtil.declensionCollectionXID);
        rootElement.appendChild(declensionCollection);
        
        // record declension templates
        declensionSet = dTemplates.entrySet();
        for (Entry<Integer, List<DeclensionNode>> e : declensionSet) {
            final Integer relatedId = e.getKey();

            e.getValue().forEach((curNode) -> {
                curNode.writeXMLTemplate(doc, declensionCollection, relatedId);
            });
        }

        // record word declensions
        declensionSet = getDeclensionMap().entrySet();
        for (Entry<Integer, List<DeclensionNode>> e : declensionSet) {
            final Integer relatedId = e.getKey();

            e.getValue().forEach((curNode) -> {
                curNode.writeXMLWordDeclension(doc, declensionCollection, relatedId);
            });
        }

        // record declension autogeneration rules
        generationRules.forEach((curRule) -> {
            curRule.writeXML(doc, declensionCollection);
        });

        // record combined form settings
        Element combinedForms = doc.createElement(PGTUtil.decCombinedFormSectionXID);
        rootElement.appendChild(combinedForms);

        combSettings.entrySet().stream().map((pairs) -> {
            Element curCombForm = doc.createElement(PGTUtil.decCombinedFormXID);
            Element curAttrib;
            // This section will have to be slightly rewritten if the combined settings become more complex
            curAttrib = doc.createElement(PGTUtil.decCombinedIdXID);
            curAttrib.appendChild(doc.createTextNode((String)pairs.getKey()));
            curCombForm.appendChild(curAttrib);
            curAttrib = doc.createElement(PGTUtil.decCombinedSurpressXID);
            curAttrib.appendChild(doc.createTextNode(pairs.getValue() ? PGTUtil.True : PGTUtil.False));
            curCombForm.appendChild(curAttrib);
            return curCombForm;            
        }).forEachOrdered((curCombForm) -> {
            combinedForms.appendChild(curCombForm);
        });
    }
    
    /**
     * This copies a list of rules to the bottom of the list of all declension templates for a given part of speech
     * that share a declension (decId) with the value defined by dimId
     * 
     * NOTE: Only applies to dimensional declensions.Singletons must be copied to manually.
     * 
     * @param typeId Part of speech to target
     * @param decId declension dimension to target
     * @param dimId dimension value to target
     * @param rules rules to be copied
     * @param selfCombId The combined ID of the form this was initially called from (do not copy duplicate of rule to self)
     */
    public void copyRulesToDeclensionTemplates(int typeId, 
            int decId, int dimId, 
            List<DeclensionGenRule> rules, 
            String selfCombId) {
        List<DeclensionNode> allNodes = getDimensionalDeclensionListTemplate(typeId);
        List<DeclensionPair> decList =  getAllCombinedDimensionalIds(0, ",", "", allNodes);
        
        decList.forEach((decPair)->{
            // only copy rule if distinct from base word form && it matches the dimensional value matches
            if (!decPair.combinedId.equals(selfCombId) && combDimIdMatches(decId, dimId, decPair.combinedId)) {
                rules.forEach((rule)->{
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
     * On load of older pgt files, must be called to maintain functionality of declension rules
     */
    public void setAllDeclensionRulesToAllClasses() {
        generationRules.forEach((curRule)->{
            curRule.addClassToFilterList(-1, -1);
        });
    }
    
    /**
     * Returns all saved yet deprecated wordforms of a word
     * @param word
     * @return 
     */
    public Map<String, DeclensionNode> getDeprecatedForms(ConWord word) {
        Map<String, DeclensionNode> ret = new HashMap<>();
        
        // first get all values that exist for this word
        getFullDeclensionListWord(word.getId()).forEach((node)->{
            ret.put(node.getCombinedDimId(), node);
        });
        
        // then remove all values which match existing combined type ids
        getAllCombinedIds(word.getWordTypeId()).forEach((pair)->{
            ret.remove(pair.combinedId);
        });
        
        return ret;
    }
    
    /**
     * Returns true if given word has deprecated wordforms
     * @param word
     * @return 
     */
    public boolean wordHasDeprecatedForms(ConWord word) {
        return !getDeprecatedForms(word).isEmpty();
    }
}
