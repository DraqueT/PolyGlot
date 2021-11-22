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
package org.darisadesigns.polyglotlina.Nodes;

import java.util.Objects;
import org.darisadesigns.polyglotlina.ManagersCollections.PhraseManager;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class PhraseNode extends DictNode {
    private String conPhrase = "";
    private String localPhrase = "";
    private String pronunciation = "";
    private String notes = "";
    private boolean procOverride = false;
    private int orderId = 0;
    
    public String getConPhrase() {
        return conPhrase;
    }

    public void setConPhrase(String conPhrase) {
        this.conPhrase = conPhrase;
    }

    public String getLocalPhrase() {
        return localPhrase;
    }

    public void setLocalPhrase(String localPhrase) {
        this.localPhrase = localPhrase;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isProcOverride() {
        return procOverride;
    }

    public void setProcOverride(boolean procOverride) {
        this.procOverride = procOverride;
    }
    
    
    public String getPronunciation() {
        String ret = pronunciation;
        
        if (!isProcOverride() && this.parent != null) {
            ret = ((PhraseManager)this.parent).getCore().getPronunciationMgr().getIpaOfPhrase(getConPhrase());
        }
        
        return ret;
    }
    
    public String getGloss() {
        return this.value;
    }

    public void setGloss(String gloss) {
        this.value = gloss;
    }
    
    /**
     * Writes all word information to XML document
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element phrase = doc.createElement(PGTUtil.PHRASE_NODE_XID);
        
        Element phraseValue = doc.createElement(PGTUtil.PHRASE_ID_XID);
        phraseValue.appendChild(doc.createTextNode(getId().toString()));
        phrase.appendChild(phraseValue);
        
        phraseValue = doc.createElement(PGTUtil.PHRASE_GLOSS_XID);
        phraseValue.appendChild(doc.createTextNode(this.getValue()));
        phrase.appendChild(phraseValue);
        
        phraseValue = doc.createElement(PGTUtil.PHRASE_CONPHRASE_XID);
        phraseValue.appendChild(doc.createTextNode(conPhrase));
        phrase.appendChild(phraseValue);
        
        phraseValue = doc.createElement(PGTUtil.PHRASE_LOCALPHRASE_XID);
        phraseValue.appendChild(doc.createTextNode(localPhrase));
        phrase.appendChild(phraseValue);
        
        phraseValue = doc.createElement(PGTUtil.PHRASE_PRONUNCIATION_XID);
        phraseValue.appendChild(doc.createTextNode(pronunciation));
        phrase.appendChild(phraseValue);
        
        phraseValue = doc.createElement(PGTUtil.PHRASE_PRONUNCIATION_OVERRIDE_XID);
        phraseValue.appendChild(doc.createTextNode(procOverride ? PGTUtil.TRUE : PGTUtil.FALSE));
        phrase.appendChild(phraseValue);
        
        phraseValue = doc.createElement(PGTUtil.PHRASE_NOTES_XID);
        phraseValue.appendChild(doc.createTextNode(notes));
        phrase.appendChild(phraseValue);
        
        phraseValue = doc.createElement(PGTUtil.PHRASE_ORDER_XID);
        phraseValue.appendChild(doc.createTextNode(Integer.toString(orderId)));
        phrase.appendChild(phraseValue);
        
        rootElement.appendChild(phrase);
    }
    
    @Override
    public boolean equals(Object comp) {
        if (!(comp instanceof PhraseNode)) {
            return false;
        }
        
        PhraseNode compNode = (PhraseNode)comp;
        
        boolean ret = getGloss().equals(compNode.getGloss());
        ret = ret && getConPhrase().equals(compNode.getConPhrase());
        ret = ret && getLocalPhrase().equals(compNode.getLocalPhrase());
        ret = ret && (getPronunciation().equals(compNode.getPronunciation())
                || (isProcOverride() && compNode.isProcOverride()));
        ret = ret && getNotes().equals(compNode.getNotes());
        ret = ret && isProcOverride() == compNode.isProcOverride();
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.conPhrase);
        return hash;
    }

    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        if (!(_node instanceof PhraseNode)) {
            String className = _node == null ? "null" : _node.getClass().getName();
            throw new ClassCastException("Cannot set PhraseNode equal to type: " + className);
        }
        
        PhraseNode node = (PhraseNode)_node;
        
        setConPhrase(node.getConPhrase());
        setLocalPhrase(node.getLocalPhrase());
        setPronunciation(node.pronunciation);
        setNotes(node.getNotes());
        setProcOverride(node.isProcOverride());
        setGloss(node.getGloss());
        setOrderId(node.orderId);
    } 
    
    @Override
    public String toString() {
        return this.value.equals("") ? " " : this.value;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    @Override
    public int compareTo(DictNode _compare) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;
        
        if (!(_compare instanceof PhraseNode)) {
            return AFTER;
        }
        
        PhraseNode compare = (PhraseNode) _compare;
        
        if (orderId > compare.orderId) {
            return AFTER;
        } else if (orderId == compare.orderId) {
            return EQUAL;
        }
        
        return BEFORE;
    }
}
