/*
 * Copyright (c) 2016-2019, Draque Thompson
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 *  See LICENSE.TXT included with this code to read the full license agreement.

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
 * This represents a single value within a word property
 * @author Draque Thompson
 */
public class WordClassValue extends DictNode {
    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        if (!(_node instanceof WordClassValue)) {
            throw new ClassCastException("Object not of type WordClassValue");
        }
        
        this.value = _node.getValue();
        this.id = _node.getId();
    }
    
     public void writeXML(Document doc, Element rootElement) {
         Element valueNode = doc.createElement(PGTUtil.ClassValueNodeXID);

        Element valueElement = doc.createElement(PGTUtil.ClassValueIdXID);
        valueElement.appendChild(doc.createTextNode(this.getId().toString()));
        valueNode.appendChild(valueElement);

        // value string
        valueElement = doc.createElement(PGTUtil.ClassValueNameXID);
        valueElement.appendChild(doc.createTextNode(this.getValue()));
        valueNode.appendChild(valueElement);

        rootElement.appendChild(valueNode);
     }
}
