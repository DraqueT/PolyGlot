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
package PolyGlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author draque
 */
public class GrammarManager {
    private final List<GrammarChapNode> chapters = new ArrayList<GrammarChapNode>();
    private final Map<Integer, byte[]> soundMap;
    private final DictCore core; // TODO: remove this if I complete work and it's still unnecessary... 
    
    public GrammarManager(DictCore _core) {
        core = _core;
        soundMap = new HashMap<Integer, byte[]>();
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
     * @return ID of sound replaced/created
     */
    public Integer addChangeRecording(Integer id, byte[] newRec) {
        Integer ret = id;
        
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
                throw new Exception("Unable to retrieve related sound with ID: " + id);
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
}
