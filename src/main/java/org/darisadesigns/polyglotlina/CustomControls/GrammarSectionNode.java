/*
 * Copyright (c) 2015-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.CustomControls;

import java.util.Objects;
import org.darisadesigns.polyglotlina.ManagersCollections.GrammarManager;
import org.darisadesigns.polyglotlina.PGTUtil;

import org.darisadesigns.polyglotlina.WebInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is a chapter section of the PolyGlot grammar guide.
 * @author draque
 */
public class GrammarSectionNode extends TreeNode {
    private final GrammarManager manager;
    private String name;
    private String sectionText;
    private int recordingId;
    
    public GrammarSectionNode(GrammarManager _manager) {
        super(null);
        name = "";
        sectionText = "";
        recordingId = -1;
        manager = _manager;
    }
    
    public void setName(String _name) {
        name = _name;
    }
    public String getName() {
        return name;
    }
    
    public void setRecordingId(int _recordingId) {
        recordingId = _recordingId;
    }
    
    public Integer getRecordingId() {
        return recordingId;
    }
    
    public void setSectionText(String _sectionText) {
        sectionText = _sectionText;
    }
    public String getSectionText() {
        return sectionText;
    }
    
    public void setRecording(byte[] _recording) {
        recordingId = manager.addChangeRecording(recordingId, _recording);
    }
    public byte[] getRecording() throws Exception {
        return manager.getRecording(recordingId);
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public void writeXML(Document doc, Element rootElement) {
        Element secNode = doc.createElement(PGTUtil.GRAMMAR_SECTION_NODE_XID);
                
        Element secElement = doc.createElement(PGTUtil.GRAMMAR_SECTION_NAME_XID);
        secElement.appendChild(doc.createTextNode(this.name));
        secNode.appendChild(secElement);

        secElement = doc.createElement(PGTUtil.GRAMMAR_SECTION_RECORDING_XID);
        secElement.appendChild(doc.createTextNode(this.getRecordingId().toString()));
        secNode.appendChild(secElement);

        secElement = doc.createElement(PGTUtil.GRAMMAR_SECTION_TEXT_XID);
        secElement.appendChild(doc.createTextNode(this.sectionText));
        secNode.appendChild(secElement);

        rootElement.appendChild(secNode);
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp instanceof GrammarSectionNode) {
            GrammarSectionNode compSec = (GrammarSectionNode)comp;
            
            ret = WebInterface.archiveHTML(sectionText, manager.getCore())
                    .equals(WebInterface.archiveHTML(compSec.sectionText, manager.getCore()));
            ret = ret && name.equals(compSec.name);
            ret = ret && recordingId == compSec.recordingId;
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + this.recordingId;
        return hash;
    }
}
