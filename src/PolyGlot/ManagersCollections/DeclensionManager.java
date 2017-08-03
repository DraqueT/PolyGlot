/*
 * Copyright (c) 2014-2017, Draque Thompson, draquemail@gmail.com
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
import PolyGlot.DeclensionDimension;
import PolyGlot.DictCore;
import PolyGlot.Nodes.DeclensionGenRule;
import PolyGlot.Nodes.DeclensionGenTransform;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.Nodes.DeclensionPair;
import PolyGlot.PGTUtil;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.WebInterface;
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

    public boolean isCombinedDeclSurpressed(String _combId) {
        if (!combSettings.containsKey(_combId)) {
            return false;
        }

        return combSettings.get(_combId);
    }

    public void setCombinedDeclSurpressed(String _combId, boolean _surpress) {
        if (!combSettings.containsKey(_combId)) {
            combSettings.put(_combId, _surpress);
        } else {
            combSettings.replace(_combId, _surpress);
        }
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
        for (DeclensionPair curPair : typeRules) {
            ruleMap.put(curPair.combinedId, 0);
        }

        // adds to return value only if rule matches ID, and is orphaned
        int missingId = 0; //used for missing index values (index system bolton)
        for (DeclensionGenRule curRule : generationRules) {
            if (curRule.getIndex() == 0 || curRule.getIndex() == missingId) {
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
        Iterator<DeclensionGenRule> itRules = generationRules.iterator();

        while (itRules.hasNext()) {
            DeclensionGenRule curRule = itRules.next();

            if (curRule.getTypeId() == typeId) {
                generationRules.remove(curRule);
            }
        }
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
     * @param typeId typeID of type to get rules for
     * @return list of rules
     */
    public List<DeclensionGenRule> getDeclensionRules(int typeId) {
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
     * Generates the new form of a declined/conjugated word based on rules for
     * its type
     *
     * @param typeId type of word to transform
     * @param combinedId combined ID of word form to create
     * @param base base word string
     * @return new word value if exists, empty string otherwise
     */
    public String declineWord(int typeId, String combinedId, String base) {
        Iterator<DeclensionGenRule> typeRules = getDeclensionRules(typeId).iterator();
        String ret = "";

        while (typeRules.hasNext()) {
            DeclensionGenRule curRule = typeRules.next();

            // skip all entries not applicable to this particular combined word ID
            if (!curRule.getCombinationId().equals(combinedId)) {
                continue;
            }

            // apply transforms within rule if rule matches current base
            if (base.matches(curRule.getRegex())) {
                List<DeclensionGenTransform> transforms = curRule.getTransforms();

                for (DeclensionGenTransform curTrans : transforms) {
                    base = base.replaceAll(curTrans.regex, curTrans.replaceText);

                    ret = base;
                }
            }
        }

        return ret;
    }

    public Map<Integer, List<DeclensionNode>> getTemplateMap() {
        return dTemplates;
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
            try {
                if (!core.getWordCollection().getNodeById(curEntry.getKey()).getWordTypeId().equals(typeId)) {
                    continue;
                }
            } catch (Exception e) {
                // if a word isn't found, then the value is orphaned and declension values will be wiped next time the user saves
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
        List<DeclensionNode> decList = (List) dTemplates.get(typeId);

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
        return getMandDims(0, ",", new ArrayList<DeclensionNode>(), getDeclensionListTemplate(typeId), "", false);
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
                if (isCombinedDeclSurpressed(curMand.getCombinedDimId())) {
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
        return getAllCombinedIds(0, ",", "", getDeclensionListTemplate(typeId));
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
    private List<DeclensionPair> getAllCombinedIds(int depth, String curId, String curLabel, List<DeclensionNode> declensionList) {
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

                ret.addAll(getAllCombinedIds(depth + 1, curId + curDim.getId().toString() + ",",
                        curLabel + (curLabel.equals("") ? "" : " ") + curDim.getValue(), declensionList));
            }
        }

        return ret;
    }

    public List<DeclensionNode> getDeclensionListWord(Integer wordId) {
        return getDeclensionList(wordId, dList);
    }

    public List<DeclensionNode> getDeclensionListTemplate(Integer typeId) {
        return getDeclensionList(typeId, dTemplates);
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

    /**
     * adds declension template directly to the map
     *
     * @param typeId ID of type to add to
     * @param node new node to add
     * @return the passed node
     * @throws java.lang.Exception Throws exception if you feed in a dupe node
     */
    public DeclensionNode addDeclensionTemplate(Integer typeId, DeclensionNode node) throws Exception {
        List recList;

        if (dTemplates.containsKey(node.getId())) {
            recList = (List) dTemplates.get(node.getId());
            recList.add(node);
        } else {
            recList = new ArrayList<>();
            recList.add(node);
            dTemplates.put(typeId, recList);
        }

        return node;
    }

    private DeclensionNode addDeclension(Integer typeId, String declension, Map list) {
        List wordList;

        topId++;

        if (list.containsKey(typeId)) {
            wordList = (List) list.get(typeId);
        } else {
            wordList = new ArrayList<>();
            list.put(typeId, wordList);
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
     * Gets declension for a word from combined dimensional Id of declension
     *
     * @param wordId the id of the root word
     * @param dimId the combined dim Id of the dimension
     * @return The declension node if found, null if otherwise
     */
    public DeclensionNode getDeclensionByCombinedId(Integer wordId, String dimId) {
        DeclensionNode ret = null;

        if (dList.containsKey(wordId)) {
            List<DeclensionNode> searchList = (List<DeclensionNode>) dList.get(wordId);
            Iterator<DeclensionNode> searchIt = searchList.iterator();

            while (searchIt.hasNext()) {
                DeclensionNode test = searchIt.next();

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
        Iterator<DeclensionNode> it = getDeclensionListTemplate(typeId).iterator();
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

    private List<DeclensionNode> getDeclensionList(Integer wordId, Map list) {
        List<DeclensionNode> ret = new ArrayList<>();

        if (list.containsKey(wordId)) {
            ret = (List<DeclensionNode>) list.get(wordId);
        }

        return ret;
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
    public Map getWordDeclensions(Integer wordId) {
        Map<String, DeclensionNode> ret = new HashMap<>();

        Iterator<DeclensionNode> decs = getDeclensionListWord(wordId).iterator();

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
        
        for (DeclensionNode remNode : removeVals) {
            wordList.remove(remNode);
        }
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
        Element wordNode;
        Element wordValue;

        rootElement.appendChild(declensionCollection);
        
        // record declension templates
        declensionSet = getTemplateMap().entrySet();

        for (Entry<Integer, List<DeclensionNode>> e : declensionSet) {
            Integer relatedId = e.getKey();

            for (DeclensionNode curNode : e.getValue()) {
                wordNode = doc.createElement(PGTUtil.declensionXID);
                declensionCollection.appendChild(wordNode);

                wordValue = doc.createElement(PGTUtil.declensionIdXID);
                wordValue.appendChild(doc.createTextNode(curNode.getId().toString()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(PGTUtil.declensionTextXID);
                wordValue.appendChild(doc.createTextNode(curNode.getValue()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(PGTUtil.declensionNotesXID);
                wordValue.appendChild(doc.createTextNode(WebInterface.archiveHTML(curNode.getNotes())));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(PGTUtil.declensionIsTemplateXID);
                wordValue.appendChild(doc.createTextNode("1"));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(PGTUtil.declensionRelatedIdXID);
                wordValue.appendChild(doc.createTextNode(relatedId.toString()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(PGTUtil.declensionMandatoryXID);
                wordValue.appendChild(doc.createTextNode(curNode.isMandatory() ? PGTUtil.True : PGTUtil.False));
                wordNode.appendChild(wordValue);

                Iterator<DeclensionDimension> dimIt = curNode.getDimensions().iterator();
                while (dimIt.hasNext()) {
                    wordValue = doc.createElement(PGTUtil.dimensionNodeXID);

                    DeclensionDimension curDim = dimIt.next();

                    Element dimNode = doc.createElement(PGTUtil.dimensionIdXID);
                    dimNode.appendChild(doc.createTextNode(curDim.getId().toString()));
                    wordValue.appendChild(dimNode);

                    dimNode = doc.createElement(PGTUtil.dimensionNameXID);
                    dimNode.appendChild(doc.createTextNode(curDim.getValue()));
                    wordValue.appendChild(dimNode);

                    dimNode = doc.createElement(PGTUtil.dimensionMandXID);
                    dimNode.appendChild(doc.createTextNode(curDim.isMandatory() ? PGTUtil.True : PGTUtil.False));
                    wordValue.appendChild(dimNode);

                    wordNode.appendChild(wordValue);
                }
            }
        }

        // record word declensions
        declensionSet = getDeclensionMap().entrySet();
        for (Entry<Integer, List<DeclensionNode>> e : declensionSet) {
            Integer relatedId = e.getKey();

            for (DeclensionNode curNode : e.getValue()) {
                wordNode = doc.createElement(PGTUtil.declensionXID);
                declensionCollection.appendChild(wordNode);

                wordValue = doc.createElement(PGTUtil.declensionIdXID);
                wordValue.appendChild(doc.createTextNode(curNode.getId().toString()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(PGTUtil.declensionTextXID);
                wordValue.appendChild(doc.createTextNode(curNode.getValue()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(PGTUtil.declensionNotesXID);
                wordValue.appendChild(doc.createTextNode(curNode.getNotes()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(PGTUtil.declensionRelatedIdXID);
                wordValue.appendChild(doc.createTextNode(relatedId.toString()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(PGTUtil.declensionComDimIdXID);
                wordValue.appendChild(doc.createTextNode(curNode.getCombinedDimId()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(PGTUtil.declensionIsTemplateXID);
                wordValue.appendChild(doc.createTextNode("0"));
                wordNode.appendChild(wordValue);
            }
        }

        // record declension autogeneration rules
        for (DeclensionGenRule curRule : generationRules) {
            Element ruleNode = doc.createElement(PGTUtil.decGenRuleXID);
            declensionCollection.appendChild(ruleNode);

            wordValue = doc.createElement(PGTUtil.decGenRuleCombXID);
            wordValue.appendChild(doc.createTextNode(curRule.getCombinationId()));
            ruleNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.decGenRuleNameXID);
            wordValue.appendChild(doc.createTextNode(curRule.getName()));
            ruleNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.decGenRuleRegexXID);
            wordValue.appendChild(doc.createTextNode(curRule.getRegex()));
            ruleNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.decGenRuleTypeXID);
            wordValue.appendChild(doc.createTextNode(Integer.toString(curRule.getTypeId())));
            ruleNode.appendChild(wordValue);
            
            wordValue = doc.createElement(PGTUtil.decGenRuleIndexXID);
            wordValue.appendChild(doc.createTextNode(Integer.toString(curRule.getIndex())));
            ruleNode.appendChild(wordValue);

            List<DeclensionGenTransform> transIt = curRule.getTransforms();
            for (DeclensionGenTransform curTransform : transIt) {
                Element transNode = doc.createElement(PGTUtil.decGenTransXID);
                ruleNode.appendChild(transNode);

                wordValue = doc.createElement(PGTUtil.decGenTransRegexXID);
                wordValue.appendChild(doc.createTextNode(curTransform.regex));
                transNode.appendChild(wordValue);

                wordValue = doc.createElement(PGTUtil.decGenTransReplaceXID);
                wordValue.appendChild(doc.createTextNode(curTransform.replaceText));
                transNode.appendChild(wordValue);
            }
        }

        // record combined form settings
        Element combinedForms = doc.createElement(PGTUtil.decCombinedFormSectionXID);
        rootElement.appendChild(combinedForms);

        for (Map.Entry pairs : combSettings.entrySet()) {
            Element curCombForm = doc.createElement(PGTUtil.decCombinedFormXID);
            Element curAttrib;
            
            // This section will have to be slightly rewritten if the combined settings become more complex
            curAttrib = doc.createElement(PGTUtil.decCombinedIdXID);
            curAttrib.appendChild(doc.createTextNode((String)pairs.getKey()));
            curCombForm.appendChild(curAttrib);
            
            curAttrib = doc.createElement(PGTUtil.decCombinedSurpressXID);
            curAttrib.appendChild(doc.createTextNode((Boolean)pairs.getValue() ? PGTUtil.True : PGTUtil.False));
            curCombForm.appendChild(curAttrib);
            
            combinedForms.appendChild(curCombForm);
        }
    }
}
