/*
 * Copyright (c) 2014-2020, Draque Thompson, draquemail@gmail.com
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

package org.darisadesigns.polyglotlina.ManagersCollections;

import org.darisadesigns.polyglotlina.CustomControls.PAlphaMap;
import org.darisadesigns.polyglotlina.Nodes.DictNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 *
 * @author draque
 * @param <N> Type of node
 */
public abstract class DictionaryCollection<N extends DictNode> {

    protected PAlphaMap<String, Integer> alphaOrder;
    protected final Map<Integer, N> nodeMap = new HashMap<>();
    protected N bufferNode;

    private int highestNodeId = 1;

    protected DictionaryCollection(N _bufferNode) {
        bufferNode = _bufferNode;
    }
    
    /**
     * Clears value of collection's current buffer
     *
     */
    public abstract void clear();
    
    /**
     * Returns an error type node with not-found information of an appropriate type
     * @return not-found node
     */
    public abstract N notFoundNode();
    
    public int addNode(N _addType) throws Exception {
        int ret;
        
        clear();
        
        bufferNode.setEqual(_addType);

        ret = this.insert(bufferNode);

        return ret;
    }

    /**
     * @param _id ID of node to replace
     * @param _modNode Node to replace prior node with
     * @throws Exception Throws exception when ID matches no node in collection
     */
    public void modifyNode(Integer _id, N _modNode) throws Exception {
        if (!nodeMap.containsKey(_id)) {
            throw new Exception("No node with id: " + _id
                    + "; cannot modify value.");
        }
        if (_id < 1) {
            throw new Exception("Id can never be less than 1.");
        }

        DictNode myNode = _modNode;

        myNode.setId(_id);
        myNode.setAlphaOrder(alphaOrder);

        nodeMap.remove(_id);
        nodeMap.put(myNode.getId(), _modNode);
    }
    
    /**
     * Tests whether object in collection exists by object's ID
     * @param objectId id of object to test for
     * @return true if exists, false otherwise
     */
    public boolean exists(Integer objectId) {
        return nodeMap.containsKey(objectId);
    }
    
    /**
     * Returns node by ID if exists, "NOT FOUND" node otherwise
     * @param _id
     * @return 
     */
    public N getNodeById(Integer _id) {
        N ret;

        if (nodeMap.containsKey(_id)) {
            ret = nodeMap.get(_id);
        } else {
            ret = this.notFoundNode();
        }

        return ret;
    }

    /**
     * @param _id ID to delete
     * @throws Exception if no ID exists as listed
     */
    public void deleteNodeById(Integer _id) throws Exception {
        if (!nodeMap.containsKey(_id)) {
            throw new Exception("Word with ID: " + _id
                    + " not found.");
        }

        nodeMap.remove(_id);
    }

    public void setAlphaOrder(PAlphaMap<String, Integer> _alphaOrder) {
        alphaOrder = _alphaOrder;
    }
    
    public N getBuffer () {
        return bufferNode;
    }
    
    /**
     * Simply inserts buffer as it currently exists
     * @return ID of inserted buffer
     * @throws Exception 
     */
    protected Integer insert() throws Exception {
        return insert(bufferNode);
    }

    /**
     * Inserts buffer node, applying next logical ID to node
     * @param _buffer buffer to insert
     * @return ID of inserted buffer
     * @throws Exception if unable to insert node to nodemap
     */
    protected Integer insert(N _buffer) throws Exception {
        highestNodeId++;

        return this.insert(highestNodeId, _buffer);
    }

    /**
     * Inserts given buffer node to nodemap
     * @param _id ID to apply to buffer
     * @param _buffer buffer to be inserted
     * @return ID of inserted buffer
     * @throws Exception if unable to insert
     */
    protected Integer insert(Integer _id, N _buffer) throws Exception {
        DictNode myBuffer = _buffer;

        if (nodeMap.containsKey(_id)) {
            throw new Exception("Duplicate ID " + _id + " for collection object: " + myBuffer.getValue());
        } else if (_id < 1) {
            throw new Exception("Collection node ID may never be zero or less.");
        } else if (_id == null) {
            throw new Exception("ID cannot be null.");
        }

        // sets highest word ID, if current id is higher
        highestNodeId = _id > highestNodeId ? _id : highestNodeId;
        myBuffer.setId(_id);
        myBuffer.setAlphaOrder(alphaOrder);

        nodeMap.put(_id, _buffer);

        return _id;
    }
    
    /**
     * Returns randomly selected nodes from the collection
     * NOTE: returns as list because Java does not support generic arrays
     * @param numRandom number of nodes to select
     * @return Either the number of nodes requested, or the total number in the collection (if not enough)
     */
    public List<N> getRandomNodes(int numRandom) {
        return getRandomNodes(numRandom, 0);
    }
    
    /**
     * Returns randomly selected nodes from the collection, excluding a selected value
     * @param numRandom number of nodes to select
     * @param exclusions IDs of elements to exclude
     * @return Either the number of nodes requested, or the total number in the collection (if not enough)
     */
    public List<N> getRandomNodes(int numRandom, Integer... exclusions) {
        List<N> ret = new ArrayList<>();
        List<N> allValues = new ArrayList<>(nodeMap.values());
        
        
        for (Integer exclude : exclusions) {
            if (nodeMap.containsKey(exclude)) {
                allValues.remove(nodeMap.get(exclude));
            }
        }
        
        // randomize order...
        Collections.shuffle(allValues, new Random(System.nanoTime()));
        
        // can't return more than exist in the collection
        numRandom = Math.min(numRandom, allValues.size());
        // select from list to return
        for (int i = 0; i < numRandom; i++) {
            ret.add(allValues.get(i));
        }
        
        return ret;
    }
    
    public boolean isEmpty() {
        return nodeMap.isEmpty();
    }
    
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (comp instanceof DictionaryCollection) {
            DictionaryCollection dictComp = (DictionaryCollection) comp;
            
            ret = ((alphaOrder == null && dictComp.alphaOrder == null) || alphaOrder.equals(dictComp.alphaOrder));
            ret = ret && nodeMap.equals(dictComp.nodeMap);
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.nodeMap);
        return hash;
    }
    
    /**
     * Generates mapping of node's string values to node objects. Lists are returned
     * rather than raw objects, as many collections allow for nodes with duplicate
     * value fields.
     * 
     * @return Map of node value -> list of nodes with this value
     */
    public Map<String, List<N>> getValueMapping() {
        Map<String, List<N>> ret = new HashMap<>();
        
        for (N node : nodeMap.values()) {
            String nodeVal = node.getValue();
            List<N> nodeList;
            
            if (ret.containsKey(nodeVal)) {
                nodeList = ret.get(nodeVal);
            } else {
                nodeList = new ArrayList<>();
                ret.put(nodeVal, nodeList);
            }
            
            nodeList.add(node);
        }
        
        return ret;
    }
}
