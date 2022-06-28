/*
 * Copyright (c) 2014-2022, Draque Thompson, draquemail@gmail.com
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

package org.darisadesigns.polyglotlina.Nodes;

import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Records orthographic pronunciation values
 * @author draque
 */
public class PronunciationNode extends DictNode {
    private String pronunciation = "";
    private String originPattern = "";
    
    public PronunciationNode() {
    }
    
    /**
     * 
     * @param pattern The pattern (regex compatible) to search for/transform
     * @param _pronunciation The pronunciation to transform the pattern encountered to
     */
    public PronunciationNode(String pattern, String _pronunciation) {
        value = pattern;
        pronunciation = _pronunciation;
    }
    
    public String getPronunciation() {
        return pronunciation;
    }
    
    public void setPronunciation(String _pronunciation) {
        pronunciation = _pronunciation;
    }

    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        if (!(_node instanceof PronunciationNode)) {
            throw new ClassCastException("Object not of type PronunciationNode");
        }
        
        PronunciationNode node = (PronunciationNode) _node;
        
        this.pronunciation = node.pronunciation;
        this.originPattern = node.originPattern;
        this.setValue(node.getValue());
        this.setId(node.getId());
    }
    
    public void writeXML(Document doc, Element rootElement) {
        Element wordNode = doc.createElement(PGTUtil.PRO_GUIDE_XID);

        Element wordValue = doc.createElement(PGTUtil.PRO_GUIDE_BASE_XID);
        wordValue.appendChild(doc.createTextNode(this.getValue()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.PRO_GUIDE_PHON_XID);
        wordValue.appendChild(doc.createTextNode(this.pronunciation));
        wordNode.appendChild(wordValue);
        
        rootElement.appendChild(wordNode);
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp != null && getClass() == comp.getClass()) {
            PronunciationNode c = (PronunciationNode)comp;
            ret = value.trim().equals(c.value.trim());
            ret = ret && pronunciation.trim().equals(c.pronunciation.trim());
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getOriginPattern() {
        return originPattern;
    }

    public void setOriginPattern(String originPattern) {
        this.originPattern = originPattern;
    }
}
