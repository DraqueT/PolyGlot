/*
 * Copyright (c) 2015, draque
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

import PolyGlot.CustomControls.GrammarSectionNode;
import PolyGlot.CustomControls.GrammarChapNode;
import PolyGlot.PGTUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Grammar manager for PolyGlot organizes and stores all grammar data
 * @author draque
 */
public class GrammarManager {
    private final List<GrammarChapNode> chapters = new ArrayList<>();
    private final Map<Integer, byte[]> soundMap;
    private GrammarChapNode buffer;
    
    public GrammarManager() {
        soundMap = new HashMap<>();
        buffer = new GrammarChapNode(this);
    }
    
    /**
     * Fetches buffer chapter node
     * @return buffer chapter node
     */
    public GrammarChapNode getBuffer() {
        return buffer;
    }
    
    /**
     * Inserts current buffer node to chapter list and clears buffer
     */
    public void insert() {
        chapters.add(buffer);
        clear();
    }
    
    /**
     * clears chapter buffer
     */
    public void clear() {
        buffer = new GrammarChapNode(this);
    }
    
    public List<GrammarChapNode> getChapters() {
        return chapters;
    }

    /**
     * Adds new chapter to index
     * @param newChap new chapter to add
     */
    public void addChapter(GrammarChapNode newChap) {
        chapters.add(newChap);
    }
    
    public Map<Integer, byte[]> getSoundMap() {
        return soundMap;
    }
    
    /**
     * Adds new chapter at particular index position
     * @param newChap chapter to add
     * @param index location to add chapter at
     */
    public void addChapterAtIndex(GrammarChapNode newChap, int index) {
        if (index > chapters.size()) {
            chapters.add(newChap);
        } else {
            chapters.add(index, newChap);
        }
    }
    
    /**
     * removes given node from chapter list
     * @param remove chapter to remove
     */
    public void removeChapter(GrammarChapNode remove) {
        chapters.remove(remove);
    }
    
    /**
     * builds and returns new grammar node
     */
    
    /**
     * Adds or changes a grammar recording.
     * @param id ID of sound to replace. -1 if newly adding
     * @param newRec New wave recording
     * @return ID of sound replaced/created, -1 if null passed in
     */
    public Integer addChangeRecording(Integer id, byte[] newRec) {
        Integer ret = id;
        
        if (newRec == null) {
            return -1;
        }
        
        if (ret == -1) {
            for (ret = 0; soundMap.containsKey(ret); ret++){}
            soundMap.put(ret, newRec);
        } else {
            soundMap.remove(ret);
            soundMap.put(ret, newRec);
        }
        
        return ret;
    }
    
    public byte[] getRecording(Integer id) throws Exception {
        byte[] ret = null;
        
        if (id != -1) {
            if (soundMap.containsKey(id)) {
                ret = soundMap.get(id);
            } else {
                throw new Exception("Unable to retrieve related recording with ID: " + id);
            }
        }
        
        return ret;
    }    
    
    /**
     * Creates a new grammar section node
     * @return new section node
     */
    public GrammarSectionNode getNewSection() {
        return new GrammarSectionNode(this);
    }
    
    /**
     * Writes all Grammar information to XML document
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Iterator<GrammarChapNode> it = chapters.iterator();
        Element grammarRoot = doc.createElement(PGTUtil.grammarSectionXID);
        rootElement.appendChild(grammarRoot);
        Element chapNode;
        GrammarChapNode curChap;
        
        while (it.hasNext()) {
            curChap = it.next();

            chapNode = doc.createElement(PGTUtil.grammarChapterNodeXID);
            grammarRoot.appendChild(chapNode);

            Element chapElement = doc.createElement(PGTUtil.grammarChapterNameXID);
            chapElement.appendChild(doc.createTextNode(curChap.getName()));
            chapNode.appendChild(chapElement);
            
            chapElement = doc.createElement(PGTUtil.grammarSectionsListXID);
            
            for (int i = 0; i < curChap.getChildCount(); i++) {
                GrammarSectionNode curSec = (GrammarSectionNode)curChap.getChildAt(i);
                
                Element secNode = doc.createElement(PGTUtil.grammarSectionNodeXID);
                
                Element secElement = doc.createElement(PGTUtil.grammarSectionNameXID);
                secElement.appendChild(doc.createTextNode(curSec.getName()));
                secNode.appendChild(secElement);
                
                secElement = doc.createElement(PGTUtil.grammarSectionRecordingXID);
                secElement.appendChild(doc.createTextNode(curSec.getRecordingId().toString()));
                secNode.appendChild(secElement);
                
                secElement = doc.createElement(PGTUtil.grammarSectionTextXID);
                secElement.appendChild(doc.createTextNode(curSec.getSectionText()));
                secNode.appendChild(secElement);
                
                chapElement.appendChild(secNode);
            }
            
            chapNode.appendChild(chapElement);
        }
    }
}
