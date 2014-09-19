/*
 * Copyright (c) 2014, Draque Thompson, draquemail@gmail.com
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
package PolyGlot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author draque
 */
public class DeclensionManager {

    // Integer is ID of related word, list is list of declension nodes
    private final Map dList = new HashMap<Integer, List<DeclensionNode>>();

    // Integer is ID of related type, list is list of declensions for this type
    private final Map dTemplates = new HashMap<Integer, List<DeclensionNode>>();

    private Integer topId = 0;
    private boolean bufferDecTemp = false;
    private Integer bufferRelId = -1;
    private DeclensionNode buffer = new DeclensionNode(-1);

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
     */
    public void deprecateAllDeclensions() {
        Iterator<Entry<Integer, List<DeclensionNode>>> decIt = dList.entrySet().iterator();
        
        while (decIt.hasNext()) {
            List<DeclensionNode> curList = decIt.next().getValue();
            
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
                    label + " " + curDim.value,
                    curDim.isMandatory() || mand);
        }
        
        return retValue;
    }

    /**
     * returns ids of all generated declensions for given type that are mandatory
     * @param typeId the type  to get mandatories for
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

    public void setBufferDecNotes(String _bufferDecNotes) {
        buffer.setNotes(_bufferDecNotes);
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
     * @param typeId ID of type to add to
     * @param node new node to add
     * @return the passed node
     * @throws java.lang.Exception Throws exception if you feed in a dupe node
     */
    public DeclensionNode addDeclensionTemplate(Integer typeId, DeclensionNode node) throws Exception {
        List recList;
        
        if (dTemplates.containsKey(node.getId())) {
            recList = (List)dTemplates.get(node.getId());
            recList.add(node);
        } else {
            recList = new ArrayList<DeclensionNode>();
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
            wordList = new ArrayList<DeclensionNode>();
            list.put(typeId, wordList);
        }

        DeclensionNode addNode = new DeclensionNode(topId);
        addNode.setValue(declension);

        wordList.add(addNode);

        return addNode;
    }

    private DeclensionNode addDeclension(Integer typeId, Integer declensionId, DeclensionNode declension, Map list) {
        List wordList;

        if (declensionId == -1) {
            declensionId = topId + 1;
        }

        deleteDeclensionFromWord(typeId, declensionId);

        if (list.containsKey(typeId)) {
            wordList = (List) list.get(typeId);
        } else {
            wordList = new ArrayList<DeclensionNode>();
            list.put(typeId, wordList);
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

    public void deleteDeclension(Integer typeId, Integer declensionId, Map list) {
        if (list.containsKey(typeId)) {
            List<DeclensionNode> copyTo = new ArrayList<DeclensionNode>();
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
            List<DeclensionNode> copyTo = new ArrayList<DeclensionNode>();
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
        List<DeclensionNode> ret = new ArrayList<DeclensionNode>();

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
     * @param wordId word to get declensions of
     * @return map of all declensions in a word (empty if none)
     */
    public Map getWordDeclensions(Integer wordId) {
        Map<String, DeclensionNode> ret = new HashMap<String, DeclensionNode>();
        
        Iterator<DeclensionNode> decs = getDeclensionListWord(wordId).iterator();
        
        while (decs.hasNext()) {
            DeclensionNode curNode = decs.next();
            
            ret.put(curNode.getCombinedDimId(), curNode);
        }
        
        return ret;
    }
}
