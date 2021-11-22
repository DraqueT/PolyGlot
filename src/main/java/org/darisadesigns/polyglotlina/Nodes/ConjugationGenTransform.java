/*
 * Copyright (c) 2014-2021, Draque Thompson, draquemail@gmail.com
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
import java.util.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Container class for declension auto-transform transformation pairs
 * @author draque
 */
public final class ConjugationGenTransform {
    public String regex;
    public String replaceText;
    
    public ConjugationGenTransform(String _regex, String _replaceText) {
        regex = _regex;
        replaceText = _replaceText;
    }
    
    public ConjugationGenTransform() {
        regex = "";
        replaceText = "";
    }
    
    public ConjugationGenTransform(ConjugationGenTransform source) {
        this.setEqual(source);
    }
    
    /**
     * Sets transform equal to that of passed value
     * @param d transform to copy values from
     */
    public void setEqual(ConjugationGenTransform d) {
        regex = d.regex;
        replaceText = d.replaceText;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.regex);
        hash = 59 * hash + Objects.hashCode(this.replaceText);
        return hash;
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (comp == this) {
            ret = true;
        } else if (comp instanceof ConjugationGenTransform) {
            ConjugationGenTransform compDec = (ConjugationGenTransform)comp;
            ret = regex.equals(compDec.regex);
            ret = ret && replaceText.equals(compDec.replaceText);
        }
        
        return ret;
    }
    
    public void writeXML(Document doc, Element rootElement) {
        Element transNode = doc.createElement(PGTUtil.DEC_GEN_TRANS_XID);
        rootElement.appendChild(transNode);

        Element wordValue = doc.createElement(PGTUtil.DEC_GEN_TRANS_REGEX_XID);
        wordValue.appendChild(doc.createTextNode(this.regex));
        transNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.DEC_GEN_TRANS_REPLACE_XID);
        wordValue.appendChild(doc.createTextNode(this.replaceText));
        transNode.appendChild(wordValue);
    }
}
