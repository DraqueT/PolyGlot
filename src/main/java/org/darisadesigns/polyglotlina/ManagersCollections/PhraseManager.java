/*
 * Copyright (c) 2021, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.darisadesigns.polyglotlina.Nodes.PhraseNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class PhraseManager extends DictionaryCollection<PhraseNode> {
    public PhraseManager() {
        super(new PhraseNode());
    }
    
    /**
     * Moves given node one farther spot down in the 
     * @param node 
     */
    public void moveNodeUp(PhraseNode node) {
        smoothOrder();
        var orderedNodes = this.getAllValues();
        
        if (node.getOrderId() > 0) {
            orderedNodes.get(node.getOrderId() - 1).setOrderId(node.getOrderId());
            node.setOrderId(node.getOrderId() - 1);
        }
    }
    
    public void moveNodeDown(PhraseNode node) {
        smoothOrder();
        var orderedNodes = this.getAllValues();
        
        if (node.getOrderId() < orderedNodes.size() - 1) {
            orderedNodes.get(node.getOrderId() + 1).setOrderId(node.getOrderId());
            node.setOrderId(node.getOrderId() + 1);
        }
    }

    @Override
    public void clear() {
        this.bufferNode = new PhraseNode();
    }
    
    @Override
    public Integer insert(Integer _id, PhraseNode _buffer) throws Exception {
        return super.insert(_id, _buffer);
    }
    
    @Override
    public List<PhraseNode> getAllValues() {
        var ret = new ArrayList<PhraseNode>(super.getAllValues());
        Collections.sort(ret);
        return ret;
    }
    
    private void smoothOrder() {
        List<PhraseNode> orderedNodes = new ArrayList<>(this.getAllValues());
        Collections.sort(orderedNodes);
        
        int i = 0;
        for (var node : orderedNodes) {
            node.setOrderId(i);
            i++;
        }
    }
    
    @Override
    public Integer insert() throws Exception {
        List<PhraseNode> orderedNodes = new ArrayList<>(this.getAllValues());
        Collections.sort(orderedNodes);
        
        if (orderedNodes.size() > 0) {
            bufferNode.setOrderId(orderedNodes.get(orderedNodes.size() - 1).getOrderId() + 1);
        } else {
            bufferNode.setOrderId(0);
        }
        
        return super.insert();
    }
    
    /**
     * Writes all word information to XML document
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element phraseCollection = doc.createElement(PGTUtil.PHRASEBOOK_XID);
        
        for (PhraseNode curNode : this.getAllValues()) {
            curNode.writeXML(doc, phraseCollection);
        }
        
        rootElement.appendChild(phraseCollection);
    }

    @Override
    public PhraseNode notFoundNode() {
        var notFound = new PhraseNode();
        
        notFound.setConPhrase("NOTFOUND");
        notFound.setLocalPhrase("NOT FOUND");
        notFound.setNotes("NOT FOUND");
        notFound.setProcOverride(true);
        notFound.setPronunciation("NOT FOUND");
        
        return notFound;
    }
}
