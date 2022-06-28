/*
 * Copyright (c) 2014-2022, Draque Thompson, draquemail@gmail.com
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

package org.darisadesigns.polyglotlina.Nodes;

import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A single dimensional value of a conjugation element (for example, tense)
 * @author draque
 */
public class ConjugationDimension extends DictNode {
    
    public ConjugationDimension(Integer _id) {
        super(_id);
    }
    
    public ConjugationDimension() {
        super(-1);
    }
    
    @Override
    public void setEqual(DictNode _node) {
        ConjugationDimension copyNode = (ConjugationDimension)_node;
        
        this.value = copyNode.getValue();
    }
    
    public void writeXML(Document doc, Element rootElement) {
        Element wordValue = doc.createElement(PGTUtil.DIMENSION_NODE_XID);
        Element dimNode = doc.createElement(PGTUtil.DIMENSION_ID_XID);
        dimNode.appendChild(doc.createTextNode(this.getId().toString()));
        wordValue.appendChild(dimNode);

        dimNode = doc.createElement(PGTUtil.DIMENSION_NAME_XID);
        dimNode.appendChild(doc.createTextNode(this.getValue()));
        wordValue.appendChild(dimNode);

        rootElement.appendChild(wordValue);
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp != null && getClass() == comp.getClass()) {
            ret = this.value.trim().equals(((DictNode) comp).value.trim());
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
