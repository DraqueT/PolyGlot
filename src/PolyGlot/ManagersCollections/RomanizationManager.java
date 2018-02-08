/*
 * Copyright (c) 2017, draque.thompson
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
package PolyGlot.ManagersCollections;

import PolyGlot.DictCore;
import PolyGlot.Nodes.PronunciationNode;
import PolyGlot.PGTUtil;
import java.util.List;
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
        List<PronunciationNode> romGuide = getPronunciations();
        
        Element guideNode = doc.createElement(PGTUtil.romGuideXID);
        rootElement.appendChild(guideNode);
        
        Element enabledNode = doc.createElement(PGTUtil.romGuideEnabledXID);
        enabledNode.appendChild(doc.createTextNode(enabled ? PGTUtil.True : PGTUtil.False));
        guideNode.appendChild(enabledNode);
        
        enabledNode = doc.createElement(PGTUtil.romGuideRecurseXID);
        enabledNode.appendChild(doc.createTextNode(recurse ? PGTUtil.True : PGTUtil.False));
        guideNode.appendChild(enabledNode);
        
        romGuide.forEach((PronunciationNode curNode) -> {
            Element romNode = doc.createElement(PGTUtil.romGuideNodeXID);
            guideNode.appendChild(romNode);
            
            Element valueNode = doc.createElement(PGTUtil.romGuideBaseXID);
            valueNode.appendChild(doc.createTextNode(curNode.getValue()));
            romNode.appendChild(valueNode);
            
            Element procNode = doc.createElement(PGTUtil.romGuidePhonXID);
            procNode.appendChild(doc.createTextNode(curNode.getPronunciation()));
            romNode.appendChild(procNode);
        });
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled status to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
