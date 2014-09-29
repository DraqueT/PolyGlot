/*
 * Copyright (c) 2014, draque
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

import java.util.Iterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is the manager class for dictionary thesaurus entries
 * @author draque
 */
public class ThesaurusManager {
    private final ThesNode thesRoot = new ThesNode(null, "Thesarus");
    
    /**
     * Gets root thesaurus node
     * @return 
     */
    public ThesNode getRoot() {
        return thesRoot;
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
        Element curElement = doc.createElement(XMLIDs.thesNodeXID);
        
        // save name
        Element property = doc.createElement(XMLIDs.thesNameXID);
        property.appendChild(doc.createTextNode(curNode.getValue()));
        curElement.appendChild(property);
        
        // save notes
        property = doc.createElement(XMLIDs.thesNotesXID);
        property.appendChild(doc.createTextNode(curNode.getNotes()));
        curElement.appendChild(property);
        
        // save words
        Iterator<ConWord> wordIt = curNode.getWords();
        while (wordIt.hasNext()) {
            ConWord curWord = wordIt.next();
            
            property = doc.createElement(XMLIDs.thesWordXID);
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
