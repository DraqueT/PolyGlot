/*
 * Copyright (c) 2016, Draque
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

import PolyGlot.Nodes.WordProperty;
import PolyGlot.PGTUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Draque
 */
public class WordPropertyCollection extends DictionaryCollection {
    
    public Iterator<WordProperty> getAllWordProperties() {
        List<WordProperty> retList = new ArrayList<>(nodeMap.values());

        Collections.sort(retList);

        return retList.iterator();
    }
    
    @Override
    public void clear() {
        bufferNode = new WordProperty();
    }
    
    /**
     * Writes all word properties information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element wordProperties = doc.createElement(PGTUtil.ClassesNodeXID);
        
        for (WordProperty wordProp : (Collection<WordProperty>)nodeMap.values()) {
            Element propElement = doc.createElement(PGTUtil.ClassXID);
            
            Element propProp = doc.createElement(PGTUtil.ClassIdXID);
            propProp.appendChild(doc.createTextNode(wordProp.getId().toString()));
            
            // TODO: FINISH
            
            wordProperties.appendChild(propElement);
        }
        
        doc.appendChild(wordProperties);
    }
}
