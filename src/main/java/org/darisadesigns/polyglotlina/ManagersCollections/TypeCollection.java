/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.darisadesigns.polyglotlina.DictCore;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class TypeCollection extends DictionaryCollection<TypeNode> {
    private final DictCore core;

    public TypeCollection(DictCore _core) {
        super(new TypeNode());
        core = _core;
    }
    
    public TypeNode getBufferType() {
        return bufferNode;
    }
    
    @Override
    public void deleteNodeById(Integer _id) throws Exception {
        super.deleteNodeById(_id);
        
        // only push update if not core loading file
        if (!core.isCurLoading()) {
            core.pushUpdate();
        }
    }

    @Override
    public int addNode(TypeNode _addType) throws Exception {
        bufferNode = new TypeNode();
        
        return super.addNode(_addType);
    }

    /**
     * inserts current buffer word to conWord list based on id; blanks out
     * buffer
     *
     * @param _id
     * @return
     * @throws Exception
     */
    public Integer insert(Integer _id) throws Exception {
        Integer ret;

        TypeNode insType = new TypeNode();
        insType.setEqual(bufferNode);
        insType.setId(_id);

        ret = super.insert(_id, bufferNode);

        bufferNode = new TypeNode();
        
        // only push update if not due to a core file load
        if (!core.isCurLoading()) {
            core.pushUpdate();
        }

        return ret;
    }

    /**
     * inserts current buffer to conWord list and generates id; blanks out
     * buffer
     *
     * @return ID of newly created node
     * @throws Exception
     */
    @Override
    public Integer insert() throws Exception {
        Integer ret;

        ret = super.insert(bufferNode);

        bufferNode = new TypeNode();
        
        // only push update if not core loading file
        if (!core.isCurLoading()) {
            core.pushUpdate();
        }

        return ret;
    }

    @Override
    public TypeNode getNodeById(Integer _id) {
        return (TypeNode) super.getNodeById(_id);
    }
    
    @Override
    public void clear() {
        bufferNode = new TypeNode();
    }

    /**
     * returns iterator of nodes with their IDs as the entry key (ordered)
     *
     * @return
     */
    public TypeNode[] getNodes() {
        List<TypeNode> retList = new ArrayList<>(nodeMap.values());

        Collections.sort(retList);

        return retList.toArray(new TypeNode[0]);
    }
    
    public boolean nodeExists(int id) {
        return nodeMap.containsKey(id);
    }
    
    public TypeNode findOrCreate(String name) throws Exception {
        TypeNode node = new TypeNode();
        node.setValue(name);
        return findOrCreate(node);
    }
    
    public TypeNode findOrCreate(TypeNode node) throws Exception {
        TypeNode ret = null;
        
        for (Object n : nodeMap.values()) {
            TypeNode compNode = (TypeNode)n;
            if (compNode.getValue().equals(node.getValue())
                    && compNode.getGloss().equals(node.getGloss())) {
                ret = compNode;
                break;
            }
        }
        
        if (ret == null) {
            ret = getNodeById(insert(node));
        }
        
        return ret;
    }
    
    /**
     * Writes all type information to XML document
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element typeContainer = doc.createElement(PGTUtil.POS_COLLECTION_XID);
        
        for (TypeNode curNode : getNodes()) {
            curNode.writeXML(doc, typeContainer);
        }
        
        rootElement.appendChild(typeContainer);
    }

    @Override
    public Object notFoundNode() {
        TypeNode emptyNode = new TypeNode();
        emptyNode.setValue("POS NOT FOUND");
        emptyNode.setNotes("POT NOT FOUND");
        return emptyNode;
    }
}
