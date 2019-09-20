/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
import java.util.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Container class for declension auto-transform transformation pairs
 * @author draque
 */
public class DeclensionGenTransform {
    public String regex;
    public String replaceText;
    
    public DeclensionGenTransform(String _regex, String _replaceText) {
        regex = _regex;
        replaceText = _replaceText;
    }
    
    public DeclensionGenTransform() {
        regex = "";
        replaceText = "";
    }
    
    /**
     * Sets transform equal to that of passed value
     * @param d transform to copy values from
     */
    public void setEqual(DeclensionGenTransform d) {
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
    public boolean equals(Object o) {
        boolean ret = true;
        
        if (this != o) {
            if (o != null && o instanceof DeclensionGenTransform) {
                DeclensionGenTransform comp = (DeclensionGenTransform)o;
                ret = this.regex.equals(comp.regex) 
                        && this.replaceText.equals(comp.replaceText);
            } else {
                ret = false;
            }
        }
        
        return ret;
    }
    
    public void writeXML(Document doc, Element rootElement) {
        Element transNode = doc.createElement(PGTUtil.DEG_GEN_TRANS_XID);
        rootElement.appendChild(transNode);

        Element wordValue = doc.createElement(PGTUtil.DEC_GEN_TRANS_REGEX_XID);
        wordValue.appendChild(doc.createTextNode(this.regex));
        transNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.DEC_GEN_TRANS_REPLACE_XID);
        wordValue.appendChild(doc.createTextNode(this.replaceText));
        transNode.appendChild(wordValue);
    }
}
