/*
 * Copyright (c) 2015-2021, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.CustomControls.GrammarChapNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.darisadesigns.polyglotlina.DictCore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Grammar manager for PolyGlot organizes and stores all grammar data
 * @author draque
 */
public abstract class GrammarManager {
    protected final List<GrammarChapNode> chapters;
    protected final Map<Integer, byte[]> soundMap;
    protected GrammarChapNode buffer;
    protected DictCore core;
    
    public GrammarManager() {
        soundMap = new HashMap<>();
        chapters = new ArrayList<>();
    }
    
    public void setCore(DictCore _core) {
        core = _core;
    }

    public DictCore getCore() {
        return this.core;
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
     * Writes all Grammar information to XML document
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element grammarRoot = doc.createElement(PGTUtil.GRAMMAR_SECTION_XID);
        rootElement.appendChild(grammarRoot);
        
        chapters.forEach((chapter)->{
            chapter.writeXML(doc, grammarRoot);
        });
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
        } else {
            soundMap.remove(ret);
        }
        soundMap.put(ret, newRec);

        return ret;
    }
    
    public void deleteRecording(int id) {
        if (soundMap.containsKey(id)) {
            soundMap.remove(id);
        }
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
    
    public boolean isEmpty() {
        return chapters.isEmpty();
    }
    
    public abstract void clear();
    public abstract GrammarChapNode[] getChapters();
    
    @Override
    public abstract boolean equals(Object comp);
    @Override
    public abstract int hashCode();
}
