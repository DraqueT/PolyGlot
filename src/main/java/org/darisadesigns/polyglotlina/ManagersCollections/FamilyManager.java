/*
 * Copyright (c) 2014-2020, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.Nodes.FamNode;
import org.darisadesigns.polyglotlina.WebInterface;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is the manager class for dictionary family entries
 * @author draque
 */
public class FamilyManager {
    private FamNode famRoot = null;
    private FamNode buffer;
    private final DictCore core;
    
    public FamilyManager(DictCore _core) {
        core = _core;
    }
    
    /**
     * Gets root family node
     * @return 
     */
    public FamNode getRoot() {
        if (famRoot == null) {
            famRoot = new FamNode(null, "Families", this);
        }
        
        return famRoot;
    }
    
    /**
     * This deletes words from the word list that no longer exist in the lexicon
     * @param fam family entry to clean
     * @param wordList raw words from entry
     */
    public void removeDeadWords(FamNode fam, List<ConWord> wordList) {
        Iterator<ConWord> wordIt = new ArrayList<>(wordList).iterator();
        
        while (wordIt.hasNext()) {
            ConWord curWord = wordIt.next();
            
            if (!core.getWordCollection().exists(curWord.getId())) {
                fam.removeWord(curWord);
            }
        }
    }
    
    /**
     * Used when loading from save file and building families.
     * Either creates new root or adds child/sets buffer to that.
     */
    public void buildNewBuffer() {
        if (famRoot == null) {
            famRoot = new FamNode(null, this);
            
            buffer = famRoot;
        } else {
            FamNode newBuffer = new FamNode(buffer, this);
            if (buffer != null) {
                buffer.addNode(newBuffer);
            }
            
            buffer = newBuffer;
        }
    }
    
    /**
     * Gets buffer for building families from saved file
     * @return current FamNode buffer
     */
    public FamNode getBuffer() {
        return buffer;
    }
    
    /**
     * jumps to buffer parent, or does nothing if at root
     */
    public void bufferDone() {
        if(buffer.getParent() == null) {
            return;
        }
        
        buffer = buffer.getParent();
    }
    
    /**
     * returns Element containing all family data to be saved to XML
     * @param doc the document this is to be inserted into
     * @return an element containing all family data
     */
    public Element writeToSaveXML(Document doc) {
        return writeToSaveXML(doc, famRoot);
    }
    
    /**
     * this is the recursive function that completes the work of its overridden method
     * @param doc the document this is to be inserted into
     * @param curNode node to build element for
     * @return an element containing all family data
     */
    private Element writeToSaveXML(Document doc, FamNode curNode) {
        Element curElement = doc.createElement(PGTUtil.FAM_NODE_XID);
        
        if (curNode == null) {
            return curElement;
        }

        // save name
        Element property = doc.createElement(PGTUtil.FAM_NAME_XID);
        property.appendChild(doc.createTextNode(curNode.getValue()));
        curElement.appendChild(property);
        
        // save notes
        property = doc.createElement(PGTUtil.FAM_NOTES_XID);
        property.appendChild(doc.createTextNode(WebInterface.archiveHTML(curNode.getNotes())));
        curElement.appendChild(property);
        
        // save words
        for (ConWord curWord : curNode.getWords()) {    
            property = doc.createElement(PGTUtil.FAM_WORD_XID);
            property.appendChild(doc.createTextNode(curWord.getId().toString()));
            curElement.appendChild(property);
        }
        
        // save subnodes
        for (FamNode child : curNode.getNodes()) {
            curElement.appendChild(writeToSaveXML(doc, child));
        }
        
        return curElement;
    }
    
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp instanceof FamilyManager) {
            FamilyManager compMan = (FamilyManager)comp;
            ret = (famRoot == null && compMan.famRoot == null) 
                    || famRoot.equals(compMan.famRoot); 
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + Objects.hashCode(this.famRoot);
        hash = 13 * hash + Objects.hashCode(this.buffer);
        return hash;
    }
}
