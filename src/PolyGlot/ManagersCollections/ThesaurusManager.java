/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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

import PolyGlot.Nodes.ConWord;
import PolyGlot.DictCore;
import PolyGlot.PGTUtil;
import PolyGlot.Nodes.ThesNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is the manager class for dictionary thesaurus entries
 * @author draque
 */
public class ThesaurusManager {
    private ThesNode thesRoot = null;
    private ThesNode buffer;
    DictCore core;
    
    public ThesaurusManager(DictCore _core) {
        core = _core;
    }
    
    /**
     * Returns dict core for use in nodes
     * @return dictionary core
     */
    public DictCore getCore() {
        return core;
    }
    
    /**
     * Gets root thesaurus node
     * @return 
     */
    public ThesNode getRoot() {
        if (thesRoot == null) {
            thesRoot = new ThesNode(null, "Thesarus", this);
        }
        
        return thesRoot;
    }
    
    /**
     * This deletes words from the word list that no longer exist in the lexicon
     * @param thes thesaurus entry to clean
     * @param wordList raw words from entry
     */
    public void removeDeadWords(ThesNode thes, List<ConWord> wordList) {
        Iterator<ConWord> wordIt = new ArrayList(wordList).iterator();
        
        while (wordIt.hasNext()) {
            ConWord curWord = wordIt.next();
            
            if (!core.getWordCollection().exists(curWord.getId())) {
                thes.removeWord(curWord);
            }
        }
    }
    
    /**
     * Used when loading from save file and building thesaurus.
     * Either creates new root or adds child/sets buffer to that.
     */
    public void buildNewBuffer() {
        if (thesRoot == null) {
            thesRoot = new ThesNode(null, this);
            
            buffer = thesRoot;
        } else {
            ThesNode newBuffer = new ThesNode(buffer, this);
            buffer.addNode(newBuffer);
            buffer = newBuffer;
        }
    }
    
    /**
     * Gets buffer for building thesaurus from saved file
     * @return current ThesNode buffer
     */
    public ThesNode getBuffer() {
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
     * returns Element containing all thesaurus data to be saved to XML
     * @param doc the document this is to be inserted into
     * @return an element containing all thesaurus data
     */
    public Element writeToSaveXML(Document doc) {
        return writeToSaveXML(doc, thesRoot);
    }
    
    /**
     * this is the recursive function that completes the work of its overridden method
     * @param doc the document this is to be inserted into
     * @param curNode node to build element for
     * @return an element containing all thesaurus data
     */
    private Element writeToSaveXML(Document doc, ThesNode curNode) {
        Element curElement = doc.createElement(PGTUtil.thesNodeXID);
        
        if (curNode == null) {
            return curElement;
        }

        // save name
        Element property = doc.createElement(PGTUtil.thesNameXID);
        property.appendChild(doc.createTextNode(curNode.getValue()));
        curElement.appendChild(property);
        
        // save notes
        property = doc.createElement(PGTUtil.thesNotesXID);
        property.appendChild(doc.createTextNode(curNode.getNotes()));
        curElement.appendChild(property);
        
        // save words
        Iterator<ConWord> wordIt = curNode.getWords();
        while (wordIt.hasNext()) {
            ConWord curWord = wordIt.next();
            
            property = doc.createElement(PGTUtil.thesWordXID);
            property.appendChild(doc.createTextNode(curWord.getId().toString()));
            curElement.appendChild(property);
        }
        
        // save subnodes
        Iterator<ThesNode> thesIt = curNode.getNodes();
        while (thesIt.hasNext()) {
            ThesNode curChild = thesIt.next();
            
            curElement.appendChild(writeToSaveXML(doc, curChild));
        }
        
        return curElement;
    }
}
