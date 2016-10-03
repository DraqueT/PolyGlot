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

import PolyGlot.Nodes.WordPropValueNode;
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
    
    public WordPropertyCollection() {
        bufferNode = new WordProperty();
    }
    
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
     * Inserts and blanks current buffer node
     */
    public void insert() throws Exception {
        this.insert(bufferNode.getId(), bufferNode);
        bufferNode = new WordProperty();
    }
    
    /**
     * Writes all word properties information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        // element containing all properties
        Element wordProperties = doc.createElement(PGTUtil.ClassesNodeXID);
        
        // creates each property
        for (WordProperty wordProp : (Collection<WordProperty>)nodeMap.values()) {
            // property element
            Element propElement = doc.createElement(PGTUtil.ClassXID);
            
            // ID element
            Element propProp = doc.createElement(PGTUtil.ClassIdXID);
            propProp.appendChild(doc.createTextNode(wordProp.getId().toString()));
            propElement.appendChild(propProp);
            
            // Name element
            propProp = doc.createElement(PGTUtil.ClassNameXID);
            propProp.appendChild(doc.createTextNode(wordProp.getValue()));
            propElement.appendChild(propProp);
            
            // generates element with all type IDs of types this property applies to
            String applyTypes = "";
            for (Integer typeId : wordProp.getApplyTypes()) {
                if (!applyTypes.equals("")) {
                    applyTypes += ",";
                }
                
                applyTypes += typeId.toString();
            }
            propProp = doc.createElement(PGTUtil.ClassApplyTypesXID);
            propProp.appendChild(doc.createTextNode(applyTypes));
            propElement.appendChild(propProp);
            
            // element for collection of values of property
            propProp = doc.createElement(PGTUtil.ClassValuesCollectionXID);
            for (WordPropValueNode curValue : wordProp.getValues()) {
                Element valueNode = doc.createElement(PGTUtil.ClassValueNodeXID);
                
                Element value = doc.createElement(PGTUtil.ClassValueIdXID);
                value.appendChild(doc.createTextNode(curValue.getId().toString()));
                valueNode.appendChild(value);
                
                // value string
                value = doc.createElement(PGTUtil.ClassValueNameXID);
                value.appendChild(doc.createTextNode(curValue.getValue()));
                valueNode.appendChild(value);
                
                propProp.appendChild(valueNode);
            }
            propElement.appendChild(propProp);

            wordProperties.appendChild(propElement);
        }
        
        rootElement.appendChild(wordProperties);
    }
}
