/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Nodes;

import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.WebInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This represents a part of speech. Apologies for the naming scheme. "Type" doesn't really fit.
 * @author draque
 */
@SuppressWarnings("EqualsAndHashcode")
public class TypeNode extends DictNode {

    private String notes = "";
    private String regexPattern = "";
    private String gloss = "";
    private boolean procMandatory = false;
    private boolean defMandatory = false;

    public void setPattern(String _regexPattern) {
        regexPattern = _regexPattern;
    }

    public String getPattern() {
        return regexPattern;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String _notes) {
        notes = _notes;
    }

    public void setGloss(String _gloss) {
        gloss = _gloss;
    }

    public String getGloss() {
        return gloss;
    }

    @Override
    public boolean equals(Object o) {
        boolean ret = false;

        if (o != null) {
            ret = o instanceof TypeNode;
            if (ret) {
                ret = ((TypeNode) o).getId().equals(this.id);
            }
        }

        return ret;
    }

    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        if (!(_node instanceof TypeNode)) {
            throw new ClassCastException("Object not of type TypeNode");
        }

        TypeNode set = (TypeNode) _node;

        this.setId(set.getId());
        this.setValue(set.getValue());
        this.setDefMandatory(set.isDefMandatory());
        this.setProcMandatory(set.isProcMandatory());
        this.setGloss(set.getGloss());
    }

    /**
     * @return the procMandatory
     */
    public boolean isProcMandatory() {
        return procMandatory;
    }

    /**
     * @param procMandatory the procMandatory to set
     */
    public void setProcMandatory(boolean procMandatory) {
        this.procMandatory = procMandatory;
    }

    /**
     * @return the defMandatory
     */
    public boolean isDefMandatory() {
        return defMandatory;
    }

    /**
     * @param defMandatory the defMandatory to set
     */
    public void setDefMandatory(boolean defMandatory) {
        this.defMandatory = defMandatory;
    }

    public void writeXML(Document doc, Element rootElement) {
        Element wordNode = doc.createElement(PGTUtil.POS_XID);
        
        Element wordValue = doc.createElement(PGTUtil.POS_ID_XID);
        Integer wordId = this.getId();
        wordValue.appendChild(doc.createTextNode(wordId.toString()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.POS_NAME_XID);
        wordValue.appendChild(doc.createTextNode(this.getValue()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.POS_NOTES_XID);
        wordValue.appendChild(doc.createTextNode(WebInterface.archiveHTML(this.getNotes())));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.POS_DEF_MAN_XID);
        wordValue.appendChild(doc.createTextNode(this.isDefMandatory() ? PGTUtil.True : PGTUtil.False));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.POS_PROC_MAN_XID);
        wordValue.appendChild(doc.createTextNode(this.isProcMandatory() ? PGTUtil.True : PGTUtil.False));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.POS_PATTERN_XID);
        wordValue.appendChild(doc.createTextNode(this.getPattern()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.POS_GLOSS_XID);
        wordValue.appendChild(doc.createTextNode(this.getGloss()));
        wordNode.appendChild(wordValue);
        
        rootElement.appendChild(wordNode);
    }
}
