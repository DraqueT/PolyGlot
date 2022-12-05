/*
 * Copyright (c) 2017-2022, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.EtyExternalParent;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This records parent->child relationships between lexical entries and serves
 * as the interface for interacting with etymological relationships between 
 * words.
 * 
 * @author Draque Thompson
 */
public class EtymologyManager {
    private final DictCore core;
    private final Map<Integer, Set<Integer>> parentToChild = new HashMap<>();
    private final Map<Integer, Set<Integer>> childToParent = new HashMap<>();
    private final Map<String, Set<Integer>> extParentToChild = new HashMap<>();
    private final Map<Integer, Map<String, EtyExternalParent>> childToExtParent = new HashMap<>();
    private Integer bufferParent = 0;
    private Integer bufferChild = 0;
    private EtyExternalParent bufferExtParent = new EtyExternalParent();
    
    public EtymologyManager(DictCore _core) {
        core = _core;
    }
    
    /**
     * Checks entire lexicon for illegal loops
     * @return list of conwords with illegal loops. Empty if none.
     */
    public ConWord[] checkAllForIllegalLoops() {
        List<ConWord> ret = new ArrayList<>();
        ConWord[] allWords = core.getWordCollection().getWordNodes();
        
        for (ConWord curWord : allWords) {
            if (checkLoopChildren(curWord.getId(), getChildren(curWord.getId()))) {
                ret.add(curWord);
            }
        }
        
        return ret.toArray(new ConWord[0]);
    }
    
    /**
     * Recursively checks for any loops based on a top level parent
     * @return true if illegal loop(s) present
     */
    private boolean checkLoopChildren(int topParentId, Integer[] childrenIds) {
        boolean ret = false;
        
        for (int childId : childrenIds) {
            if (createsLoop(topParentId, childId) 
                    || checkLoopChildren(topParentId, getChildren(childId))) {
                ret = true;
                break;
            }
        }
        
        return ret;
    }
    
    /**
     * Adds a parent->child relationship to two words if the relationship does
     * not already exist.
     * @param parent the parent
     * @param child the child
     * @throws IllegalLoopException if relationship creates looping dependency
     */
    public void addRelation(Integer parent, Integer child) throws IllegalLoopException {
        addRelation(parent, child, false);
    }
    
    /**
     * Adds a parent->child relationship to two words if the relationship does
     * not already exist.
     * @param parent the parent
     * @param child the child
     * @param overrideChecks force overriding of all checks (used primarily for testing)
     * @throws IllegalLoopException if relationship creates looping dependency
     */
    public void addRelation(Integer parent, Integer child, boolean overrideChecks) throws IllegalLoopException {
        ConWordCollection collection = core.getWordCollection();
        
        if (!overrideChecks) {
            if (createsLoop(parent, child)) {
                throw new IllegalLoopException("Parent/Child relation creates illegal loop."
                        + " A word may never have itself in its own etymological lineage.");
            }

            // fail silently if either doesn't exist        
            if (!collection.exists(parent) || !collection.exists(child)) {
                return;
            }
        }

        if (parentToChild.containsKey(parent)) {
            Set<Integer> myList = parentToChild.get(parent);

            if (!myList.contains(child)) {
                myList.add(child);
            }
        } else {
            Set<Integer> newList = new HashSet<>();
            newList.add(child);
            parentToChild.put(parent, newList);
        }

        if (childToParent.containsKey(child)) {
            Set<Integer> myList = childToParent.get(child);

            if (!myList.contains(parent)) {
                myList.add(parent);
            }
        } else {
            Set<Integer> newList = new HashSet<>();
            newList.add(parent);
            childToParent.put(child, newList);
        }
    }
    
    /**
     * Collects and returns full list of all extant parents (both internal and
     * external)
     * @return 
     */
    public ConWord[] getAllRoots() {
        List<ConWord> ret = new ArrayList<>();
        
        parentToChild.keySet().forEach((id) -> {
            ConWord curParent = core.getWordCollection().getNodeById(id);
            ret.add(curParent);
        });
        
        ret.addAll(this.getExtParentList());
        core.getWordCollection().safeSort(ret);
        return ret.toArray(new ConWord[0]);
    }
    
    /**
     * Returns an array of children that a word has
     * @param wordId ID of word to retrieve children of
     * @return list of integer IDs of child words (empty array if none)
     */
    public Integer[] getChildren(Integer wordId) {
        Set<Integer> ret;
        
        if (parentToChild.containsKey(wordId)) {
            ret = parentToChild.get(wordId);
        } else {
            ret = new HashSet<>();
        }
        
        return ret.toArray(new Integer[0]);
    }
    
    /**
     * Gets all external parents of a child by child id
     * @param childId id of child to get parents of
     * @return all external parents of child (empty if none)
     */
    public EtyExternalParent[] getWordExternalParents(Integer childId) {
        return childToExtParent.containsKey(childId) ? 
                childToExtParent.get(childId).values().toArray(new EtyExternalParent[0]) : 
                new EtyExternalParent[0];
    }
    
    /**
     * Gets all parent ids of child word by child id
     * @param childId id of child to query for parents
     * @return list of parent ids (empty if none)
     */
    public Integer[] getWordParentsIds(Integer childId) {
        return childToParent.containsKey(childId) ? childToParent.get(childId).toArray(new Integer[0])
                : new Integer[0];
    }
    
    /**
     * Sets relation of external parent to child word
     * NOTE: USE UNIQUE ID OF PARENT RATHER THAN SIMPLE VALUE
     * @param parent Full unique ID of external parent
     * @param child child's ID
     */
    public void addExternalRelation(EtyExternalParent parent, Integer child) {
        // return immediately if child does not exist
        if (core.getWordCollection().exists(child)) {
            if (extParentToChild.containsKey(parent.getUniqueId())) {
                Set<Integer> myList = extParentToChild.get(parent.getUniqueId());
                if (!myList.contains(child)) {
                    myList.add(child);
                }
            } else {
                Set<Integer> myList = new HashSet<>();
                myList.add(child);
                extParentToChild.put(parent.getUniqueId(), myList);
            }

            if (childToExtParent.containsKey(child)) {
                Map<String, EtyExternalParent> myMap = childToExtParent.get(child);
                if (!myMap.containsKey(parent.getUniqueId())) {
                    myMap.put(parent.getUniqueId(), parent);
                }
            } else {
                Map<String, EtyExternalParent> myMap = new HashMap<>();
                myMap.put(parent.getUniqueId(), parent);
                childToExtParent.put(child, myMap);
            }
        }
    }
    
    public void delExternalRelation(EtyExternalParent parent, Integer child) {
        // only run if child exists
        if (core.getWordCollection().exists(child)) {
            if (extParentToChild.containsKey(parent.getUniqueId())) {
                Set<Integer> myList = extParentToChild.get(parent.getUniqueId());
                myList.remove(child);
            }
            
            if (childToExtParent.containsKey(child)) {
                Map<String, EtyExternalParent> myMap = childToExtParent.get(child);
                myMap.remove(parent.getUniqueId());
            }
        }
    }
    
    /**
     * Gets list of every external parent referenced in entire language
     * @return alphabetical list by word + (language)
     */
    private List<EtyExternalParent> getExtParentList() {
        Map<String, EtyExternalParent> retValsMap = new HashMap<>();
        
        // iterate on each set of parent values (key = wordId)
        for (Entry<Integer, Map<String, EtyExternalParent>> parentMap : childToExtParent.entrySet()) {
            // iterate on each parent (key = parentId, which is a string)
            for (Entry<String, EtyExternalParent> parent : parentMap.getValue().entrySet()) {
                if (!retValsMap.containsKey(parent.getKey())) {
                    retValsMap.put(parent.getKey(), parent.getValue());
                }
            }
        }
        
        List<EtyExternalParent> ret = new ArrayList(retValsMap.values());
        Collections.sort(ret);
        return ret;
    }
    
    /**
     * Deletes relationship between parent and child if one exists
     * @param parentId
     * @param childId 
     */
    public void delRelation(Integer parentId, Integer childId) {
        if (parentToChild.containsKey(parentId)) {
            Set<Integer> myList = parentToChild.get(parentId);
            myList.remove(childId);
        }
        
        if (childToParent.containsKey(childId)) {
            Set<Integer> myList = childToParent.get(childId);
            myList.remove(parentId);
        }
    }
    
    /**
     * Deletes all broken etymological roots
     */
    public void cleanBrokenEtymologyRoots() {
        cleanEtymologyMap(parentToChild);
        cleanEtymologyMap(childToParent);
        cleanExternalEtymologyMap();
        
    }
    
    private void cleanEtymologyMap(Map<Integer, Set<Integer>> cleanMap) {
        ConWordCollection wordCollection = core.getWordCollection();
        
        for (Entry<Integer, Set<Integer>> curEntry : cleanMap.entrySet()) {
            if (!wordCollection.exists(curEntry.getKey())) {
                cleanMap.remove(curEntry.getKey());
            } else {
                Set<Integer> childIds = curEntry.getValue();
                int childIdLength = childIds.size();
                
                // remove all dead children
                for (Integer id : childIds.toArray(new Integer[0])) {
                    if (!wordCollection.exists(id)) {
                        childIds.remove(id);
                    }
                }
                
                // remove entry entirely if empty
                if (childIds.isEmpty()) {
                    cleanMap.remove(curEntry.getKey());
                }
            }
        }
    }
    
    private void cleanExternalEtymologyMap() {
        ConWordCollection wordCollection = core.getWordCollection();
        for (Integer extChildId : childToExtParent.keySet()) {
            if (!wordCollection.exists(extChildId)) {
                // first delete all parnt referenes to this child
                for (String extParentId : childToExtParent.get(extChildId).keySet()) {
                    Set<Integer> cleanParentToChild = extParentToChild.get(extParentId);
                    
                    cleanParentToChild.remove(extChildId);
                    
                    if (cleanParentToChild.isEmpty()) {
                        extParentToChild.remove(extParentId);
                    }
                }
                
                childToExtParent.remove(extChildId);
            }
        }
    }

    /**
     * Writes all word information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        ConWordCollection wordCollection = core.getWordCollection();
        Element collection = doc.createElement(PGTUtil.ETY_COLLECTION_XID);
        
        // we only need to record the relationship one way, the bidirection will be regenerated
        for (Entry<Integer, Set<Integer>> curEntry : parentToChild.entrySet()) {
            // skip nonexistent words
            if (!wordCollection.exists(curEntry.getKey())) {
                continue;
            }
            
            Element myNode = doc.createElement(PGTUtil.ETY_INT_RELATION_NODE_XID);
            myNode.appendChild(doc.createTextNode(curEntry.getKey().toString()));
            
            for (Integer curChild : curEntry.getValue()) {
                if (!wordCollection.exists(curChild)) {
                    continue;
                }
                
                Element child = doc.createElement(PGTUtil.ETY_INT_CHILD_XID);
                child.appendChild(doc.createTextNode(curChild.toString()));
                myNode.appendChild(child);
            }
            collection.appendChild(myNode);
        }
        
        // adds a node for each word with at least one external parent
        childToExtParent.entrySet().stream().map((curEntry) -> {
            Element childContainer = doc.createElement(PGTUtil.ETY_CHILD_EXTERNALS_XID);
            childContainer.appendChild(doc.createTextNode(curEntry.getKey().toString()));
            // creates a node for each external parent within a word
            curEntry.getValue().values().forEach((parent) -> {
                Element extParentNode = doc.createElement(PGTUtil.ETY_EXTERNAL_WORD_NODE_XID);
                // record external word value
                Element curElement = doc.createElement(PGTUtil.ETY_EXTERNAL_WORD_VALUE_XID);
                curElement.appendChild(doc.createTextNode(parent.getValue()));
                extParentNode.appendChild(curElement);
                // record external word origin
                curElement = doc.createElement(PGTUtil.ETY_EXTERNAL_WORD_ORIGIN_XID);
                curElement.appendChild(doc.createTextNode(parent.getExternalLanguage()));
                extParentNode.appendChild(curElement);
                // record external word definition
                curElement = doc.createElement(PGTUtil.ETY_EXTERNAL_WORD_DEFINITION_XID);
                curElement.appendChild(doc.createTextNode(parent.getDefinition()));
                extParentNode.appendChild(curElement);
                
                childContainer.appendChild(extParentNode);
                
            });
            return childContainer;
        }).forEachOrdered((childContainer) -> {
            collection.appendChild(childContainer);
        });
        
        rootElement.appendChild(collection);
    }
    
    /**
     * Tests whether adding a parent-child relationship would create an illegal
     * looping scenario
     * @param parentId parent word ID to check
     * @param childId child word ID to check
     * @return true if illegal due to loop, false otherwise
     */
    private boolean createsLoop(Integer parentId, Integer childId) {
        return parentId.equals(childId) || createsLoopParent(parentId, childId)
                || createsLoopChild(parentId, childId);
    }
    
    /**
     * Tests whether a child->parent addition creates an illegal loop.
     * Recursive.
     * @param childId bottommost child ID being checked
     * @return true if illegal due to loop, false otherwise
     */
    private boolean createsLoopParent(Integer curWordId, Integer childId) {
        boolean ret = false;
        
        if (childToParent.containsKey(curWordId)) {
            for (Integer selectedParent : childToParent.get(curWordId)) {
                ret = selectedParent.equals(childId) 
                        || createsLoopParent(selectedParent, childId);
                
                // break on single loop occurrence and return
                if (ret) {
                    break;
                }
            }
        }
           
        return ret;
    }
    
    /**
     * Tests whether a parent->child addition creates an illegal loop.
     * Recursive.
     * @param parentId topmost parent ID to check against
     * @param curWordId ID of current word being checked against
     * @return true if illegal due to loop, false otherwise
     */
    private boolean createsLoopChild(Integer parentId, Integer curWordId) {
        boolean ret = false;
        
        // test base parent ID against all children of current word
        // and of all subsequent children down the chain
        for (Integer childId : this.getChildren(curWordId)) {
            ret = parentId.equals(childId) && createsLoopChild(parentId, childId);
            
            // break on single loop occurrence and return
            if (ret) {
                break;
            }
        }
        
        return ret;
    }
    
    public void setBufferParent(Integer _bufferParent) {
        bufferParent = _bufferParent;
    }
    
    public void setBufferChild(Integer _bufferChild) {
        bufferChild = _bufferChild;
    }
    
    public EtyExternalParent getBufferExtParent() {
        return bufferExtParent;
    }
    
    public void insertBufferExtParent() {
        addExternalRelation(bufferExtParent, bufferChild);
        bufferExtParent = new EtyExternalParent();
    }
    
    /**
     * Tests whether child word has parent word in its etymology
     * @param childId id of child word
     * @param parId id of parent word
     * @return true if in etymology
     */
    public boolean childHasParent(Integer childId, Integer parId) {
        boolean ret = false;
        
        if (childToParent.containsKey(childId)) {
            Set<Integer> myList = childToParent.get(childId);
            ret = myList.contains(parId);
            
            if (!ret) {
                for (Integer newChild : myList) {
                    ret = childHasParent(newChild, parId);
                    
                    if (ret) {
                        break;
                    }
                }
            }
        }
        
        return ret;
    }
    
    /**
     * Tests whether child word has external parent word in its etymology
     * @param childId id of child word
     * @param parId unique external id of parent word
     * @return true if in etymology
     */
    public boolean childHasExtParent(Integer childId, String parId) {
        return childToExtParent.containsKey(childId) 
                && childToExtParent.get(childId).containsKey(parId);
    }
    
    /**
     * Inserts buffer values and clears buffer
     */
    public void insert() {
        try {
            addRelation(bufferParent, bufferChild);
            // Do NOT set these to 0. This relies on the parent buffer persisting.
        } catch (IllegalLoopException e) {
            core.getOSHandler().getIOHandler().writeErrorLog(e);
            // do nothing. These will have been eliminated at the time of archiving.
        }
    }
    
    /**
     * removes empty lists from parent and child lists
     */
    private void cleanParentChildLists() {
        clearParentChildList(parentToChild);
        clearParentChildList(childToParent);
        clearParentChildList(extParentToChild);
        clearParentChildList(childToExtParent);
    }
    
    /**
     * removes empty entries from passed map
     * @param clean 
     */
    private void clearParentChildList(Map clean) {
        for(Object key : clean.keySet().toArray()) {
            Object value = clean.get(key);
            
            if (value instanceof List) {
                if (((List)value).isEmpty()) {
                    clean.remove(key);
                }
            } else if (value instanceof Map) {
                if (((Map)value).isEmpty()) {
                    clean.remove(key);
                }
            }
        }
    }
    
    public static class IllegalLoopException extends Exception {
        public IllegalLoopException(String message) {
            super(message);
        }
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        this.cleanParentChildLists();
        
        if (this == comp) {
            ret = true;
        } else if (comp instanceof EtymologyManager) {
            EtymologyManager compEt = (EtymologyManager)comp;
            
            ret = parentToChild.equals(compEt.parentToChild);
            ret = ret && childToParent.equals(compEt.childToParent);
            ret = ret && extParentToChild.equals(compEt.extParentToChild);
            ret = ret && childToExtParent.equals(compEt.childToExtParent);
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.parentToChild);
        return hash;
    }
}
