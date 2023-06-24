/*
 * Copyright (c) 2015-2023, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop.CustomControls;

import java.util.Objects;
import java.util.regex.Pattern;
import javax.swing.tree.DefaultMutableTreeNode;
import org.darisadesigns.polyglotlina.CustomControls.GrammarSectionNode;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopGrammarManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.WebInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is a chapter section of the PolyGlot grammar guide.
 * @author draque
 */
public class DesktopGrammarSectionNode extends DefaultMutableTreeNode implements GrammarSectionNode {

    private final DesktopGrammarManager manager;
    private String name;
    private String sectionText;
    private int recordingId;
    private boolean gptSelected;
    
    private static final String LOCAL_PACK = "LLOOCCAALL__PPAACCKK";
    private static final String CON_PACK = "CCOONN__PPAACCKK";
    
    public DesktopGrammarSectionNode(DesktopGrammarManager _manager) {
        name = "";
        sectionText = "";
        recordingId = -1;
        manager = _manager;
    }
    
    @Override
    public void setName(String _name) {
        name = _name;
    }
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setRecordingId(int _recordingId) {
        recordingId = _recordingId;
    }
    
    @Override
    public Integer getRecordingId() {
        return recordingId;
    }
    
    @Override
    public void setSectionText(String _sectionText) {
         sectionText = packSectionText(_sectionText);
    }

    @Override
    public String getSectionText() {
        return unpackSectionText(sectionText);
    }
    
    @Override
    public void setRecording(byte[] _recording) {
        recordingId = manager.addChangeRecording(recordingId, _recording);
    }
    
    @Override
    public byte[] getRecording() throws Exception {
        return manager.getRecording(recordingId);
    }
    
    @Override
    public void clearRecording() {
        manager.deleteRecording(recordingId);
        recordingId = -1;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Puts the text into a storage format and ensures only appropriate fonts
     * are used.
     * @param _sectionText
     * @return 
     */
    private String packSectionText(String _sectionText) {
        var propMan = (DesktopPropertiesManager)manager.getCore().getPropertiesManager();
        var conFontFam = propMan.getFontCon().getFamily();
        var localFontFam = propMan.getFontLocal().getFamily();
        
        // if only using a single font, presume all sections are "local" for clarity
        if (conFontFam.equals(localFontFam)) {
            return packer(_sectionText, conFontFam, LOCAL_PACK, LOCAL_PACK);
        }
        
        return packer(_sectionText, conFontFam, CON_PACK, LOCAL_PACK);
    }
    
    /**
     * Returns text to a normal format
     * @param _sectionText
     * @return 
     */
    private String unpackSectionText(String _sectionText) {
        var propMan = (DesktopPropertiesManager)manager.getCore().getPropertiesManager();
        var conFontFam = propMan.getFontCon().getFamily();
        var localFontFam = propMan.getFontLocal().getFamily();
        
        return packer(_sectionText, CON_PACK, conFontFam, localFontFam);
    }
    
    private String packer(String _sectionText, String matchName, String matchReplace, String defaultReplace) {
        var regex = "<font\s+face=\"([^\"]+)\"\s*size=\"([^\"]+)\"\s*color=\"([^\"]+)\"\s*>";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(_sectionText);
        
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            var fontName = matcher.group(1);
            var fontSize = matcher.group(2);
            var fontColor = matcher.group(3);
            
            if (fontName.equals(matchName)) {
                matcher.appendReplacement(sb, "<font face=\"" + matchReplace + "\"size=\"" + fontSize + "\"color=\"" + fontColor +  "\">");
            } else { // default to local font
                matcher.appendReplacement(sb, "<font face=\"" + defaultReplace + "\"size=\"" + fontSize + "\"color=\"" + fontColor +  "\">");
            }
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    @Override
    public boolean isGptSelected() {
        return gptSelected;
    }

    @Override
    public void setGptSelected(boolean gptSelected) {
        this.gptSelected = gptSelected;
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
        secElement.appendChild(doc.createTextNode(unpackSectionText(this.sectionText)));
        secNode.appendChild(secElement);
        
        secElement = doc.createElement(PGTUtil.GRAMMAR_SECTION_IS_GPT_SELECTED);
        secElement.appendChild(doc.createTextNode(gptSelected ? PGTUtil.TRUE : PGTUtil.FALSE));
        secNode.appendChild(secElement);

        rootElement.appendChild(secNode);
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp instanceof DesktopGrammarSectionNode compSec) {
            DictCore core = manager.getCore();
            ret = WebInterface.archiveHTML(sectionText, core).equals(WebInterface.archiveHTML(compSec.sectionText, core));
            ret = ret && name.trim().equals(compSec.name.trim());
            ret = ret && recordingId == compSec.recordingId;
            ret = ret && gptSelected == compSec.gptSelected;
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
