/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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

import PolyGlot.Nodes.DictNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author draque
 */
public abstract class DictionaryCollection {

    protected Map<Character, Integer> alphaOrder;
    protected final Map nodeMap = new HashMap<>();
    protected DictNode bufferNode;

    private int highestNodeId = 1;

    /**
     * Clears value of collection's current buffer
     *
     */
    abstract public void clear();
    
    public int addNode(DictNode _addType) throws Exception {
        int ret;
        
        clear();
        
        bufferNode.setEqual(_addType);

        ret = this.insert(bufferNode);

        return ret;
    }
    
    /**
     * @param _id ID of node to replace
     * @param _modNode Node to replace prior word with
     * @throws Exception Throws exception when ID matches no node in collection
     */
    public void modifyNode(Integer _id, DictNode _modNode) throws Exception {
        if (!nodeMap.containsKey(_id)) {
            throw new Exception("No node with id: " + _id.toString()
                    + "; cannot modify value.");
        }
        if (_id < 1) {
            throw new Exception("Id can never be less than 1.");
        }

        _modNode.setId(_id);
        _modNode.setAlphaOrder(alphaOrder);
        
        nodeMap.remove(_id);
        nodeMap.put(_modNode.getId(), _modNode);
    }
    
    /**
     * Tests whether object in collection exists by object's ID
     * @param objectId id of object to test for
     * @return true if exists, false otherwise
     */
    public boolean exists(Integer objectId) {
        return nodeMap.containsKey(objectId);
    }
    
    public Object getNodeById(Integer _id) throws Exception {
        if (!nodeMap.containsKey(_id)) {
            throw new Exception("Node with id: " + _id.toString()
                    + " does not exist!");
        }

        return nodeMap.get(_id);
    }

    /**
     * @param _id ID to delete
     * @throws Exception if no ID exists as listed
     */
    public void deleteNodeById(Integer _id) throws Exception {
        if (!nodeMap.containsKey(_id)) {
            throw new Exception("Word with ID: " + _id.toString()
                    + " not found.");
        }

        nodeMap.remove(_id);
    }

    public void setAlphaOrder(Map _alphaOrder) {
        alphaOrder = _alphaOrder;
    }
    
    public DictNode getBuffer () {
        return bufferNode;
    }

    /**
     * Inserts buffer node, applying next logical ID to node
     * @param _buffer buffer to insert
     * @return ID of inserted buffer
     * @throws Exception if unable to insert node to nodemap
     */
    protected Integer insert(DictNode _buffer) throws Exception {
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
    protected Integer insert(Integer _id, DictNode _buffer) throws Exception {
        _buffer.setId(_id);
        _buffer.setAlphaOrder(alphaOrder);

        if (nodeMap.containsKey(_id)) {
            throw new Exception("Duplicate ID " + _id.toString() + " for collection object: " + _buffer.getValue());
        }
        if (_id < 1) {
            throw new Exception("Collection node ID may never be zero or less.");
        }

        nodeMap.put(_id, _buffer);

        // sets highest word ID, if current id is higher
        highestNodeId = _id > highestNodeId ? _id : highestNodeId;

        return _id;
    }
    
    /**
     * Returns randomly selected nodes from the collection
     * @param numRandom number of nodes to select
     * @return Either the number of nodes requested, or the total number in the collection (if not enough)
     */
    public List<DictNode> getRandomNodes(int numRandom) {
        return getRandomNodes(numRandom, 0);
    }
    
    /**
     * Returns randomly selected nodes from the collection, excluding a selected value
     * @param numRandom number of nodes to select
     * @param exclude ID of element to exclude
     * @return Either the number of nodes requested, or the total number in the collection (if not enough)
     */
    public List<DictNode> getRandomNodes(int numRandom, Integer exclude) {
        List<DictNode> ret = new ArrayList<>();
        List<DictNode> allValues = new ArrayList(nodeMap.values());
        
        
        if (nodeMap.containsKey(exclude)) {
            allValues.remove((DictNode)nodeMap.get(exclude));
        }
        
        // randommize order...
        Collections.shuffle(allValues, new Random(System.nanoTime()));
        
        // can't return more than exist in the collection
        numRandom = numRandom > allValues.size() ? allValues.size() : numRandom;
        // select from list to return
        for (int i = 0; i < numRandom; i++) {
            ret.add(allValues.get(i));
        }
        
        return ret;
    }
}
