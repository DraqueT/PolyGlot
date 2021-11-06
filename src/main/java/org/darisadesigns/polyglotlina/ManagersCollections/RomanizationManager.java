/*
 * Copyright (c) 2017-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Records romanization values
 * @author draque.thompson
 */
public class RomanizationManager extends PronunciationMgr {
    
    private boolean enabled = false;
    
    public RomanizationManager(DictCore _core) {
        super(_core);
    }
    
    /**
     * Writes all romanization information to XML document
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    @Override
    public void writeXML(Document doc, Element rootElement) {
        PronunciationNode[] romGuide = getPronunciations();
        
        Element guideNode = doc.createElement(PGTUtil.ROM_GUIDE_XID);
        rootElement.appendChild(guideNode);
        
        Element enabledNode = doc.createElement(PGTUtil.ROM_GUIDE_ENABLED_XID);
        enabledNode.appendChild(doc.createTextNode(enabled ? PGTUtil.TRUE : PGTUtil.FALSE));
        guideNode.appendChild(enabledNode);
        
        enabledNode = doc.createElement(PGTUtil.ROM_GUIDE_RECURSE_XID);
        enabledNode.appendChild(doc.createTextNode(recurse ? PGTUtil.TRUE : PGTUtil.FALSE));
        guideNode.appendChild(enabledNode);
        
        for (PronunciationNode curNode : romGuide) {
            Element romNode = doc.createElement(PGTUtil.ROM_GUIDE_NODE_XID);
            guideNode.appendChild(romNode);
            
            Element valueNode = doc.createElement(PGTUtil.ROM_GUIDE_BASE_XID);
            valueNode.appendChild(doc.createTextNode(curNode.getValue()));
            romNode.appendChild(valueNode);
            
            Element procNode = doc.createElement(PGTUtil.ROM_GUIDE_PHON_XID);
            procNode.appendChild(doc.createTextNode(curNode.getPronunciation()));
            romNode.appendChild(procNode);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean _enabled) {
        this.enabled = _enabled;
    }
    
    @Override
    protected String getToolLabel() {
        return "Romanization Manager";
    }
    
    @Override
    public boolean equals(Object comp) {
        if (this == comp)
            return true;

        if (comp instanceof RomanizationManager roMan)
            return enabled == roMan.enabled && super.equals(comp);

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.enabled ? 1 : 0);
        return hash;
    }
}
