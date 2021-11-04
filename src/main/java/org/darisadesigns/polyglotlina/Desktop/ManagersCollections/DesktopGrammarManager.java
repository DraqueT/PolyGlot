/*
 * Copyright (c) 2015-2021, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop.ManagersCollections;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Objects;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopGrammarChapNode;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopGrammarSectionNode;
import org.darisadesigns.polyglotlina.ManagersCollections.GrammarManager;

/**
 * Grammar manager for PolyGlot organizes and stores all grammar data
 * @author draque
 */
public class DesktopGrammarManager extends GrammarManager {
    public DesktopGrammarManager() {
        super();
        
        buffer = new DesktopGrammarChapNode(this);
    }
    
    /**
     * clears chapter buffer
     */
    @Override
    public void clear() {
        buffer = new DesktopGrammarChapNode(this);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public DesktopGrammarChapNode[] getChapters() {
        
        return chapters.toArray(new DesktopGrammarChapNode[0]);
    }
    
    /**
     * Creates a new grammar section node
     * @return new section node
     */
    public DesktopGrammarSectionNode getNewSection() {
        return new DesktopGrammarSectionNode(this);
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (comp == this) {
            ret = true;
        } else if (comp instanceof DesktopGrammarManager compMan) {
            ret = chapters.equals(compMan.chapters);
            
            if (ret) {
                for (Object o : soundMap.entrySet().toArray()) {
                    Entry<Integer, byte[]> entry = (Entry<Integer, byte[]>)o;

                    int id = entry.getKey();
                    byte[] soundVal = entry.getValue();

                    ret = compMan.soundMap.containsKey(id);

                    if (ret) {
                        ret = Arrays.equals(soundVal, compMan.soundMap.get(id));
                    } else {
                        break;
                    }
                }
            }
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.chapters);
        hash = 89 * hash + Objects.hashCode(this.soundMap);
        return hash;
    }
}