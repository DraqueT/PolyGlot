/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 * See LICENSE.TXT included with this code to read the full license agreement.
 *
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

package org.darisadesigns.polyglotlina.Nodes;

import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.WebInterface;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents both the header for declension templates, and the actual
 * body object for fully realized declension constructs (with full combined Dim Ids)
 * @author draque
 */
public class DeclensionNode extends DictNode {
    private String notes = "";
    private String combinedDimId = "";
    private boolean dimensionless = false;
    private int highestDimension = 1;
    private final Map<Integer, DeclensionDimension> dimensions = new HashMap<>();
    private DeclensionDimension buffer = new DeclensionDimension(-1);
    
    public DeclensionNode(Integer _declensionId) {
        super(_declensionId);
    }
    
    /**
     * gets dimensional buffer
     * @return current buffer
     */
    public DeclensionDimension getBuffer() {
        return buffer;
    }
    
    /**
     * Inserts current value of dimensional buffer.
     * Clears buffer after insert.
     * @throws java.lang.Exception if buffer ID is -1
     */
    public void insertBuffer() throws Exception {
        if (buffer.getId() == -1) {
            throw new Exception("Dimension with ID -1 cannot be inserted.");
        }
        
        this.addDimension(buffer);
        buffer = new DeclensionDimension(-1);
    }
    
    /**
     * clears current value of buffer
     */
    public void clearBuffer() {
        buffer = new DeclensionDimension(-1);
    }
    
    public boolean isDimensionless() {
        return dimensionless;
    }
    
    /**
     * Sets value of dimensionless to this declension.
     * WARNING: will erase ALL existing dimensions and add 1, which is a fixed value, rather than a true dimension when
     * set to "true". Will wipe single existing singleton-dimension when setting to false. Does nothing if set value
     * matches current value.
     * @param _dimensionless 
     */
    public void setDimensionless(boolean _dimensionless) {
        if (dimensionless != _dimensionless) {
            dimensionless = _dimensionless;
            dimensions.clear();

            if (_dimensionless) {
                DeclensionDimension dim = new DeclensionDimension();
                dim.setValue("SINGLETON-DIMENSION");
                addDimension(dim);
            }
        }
    }
    
    /**
     * Adds a dimension to this declension
     * @param dim Dimension to be added. Set id if desired, generated otherwise
     * @return id of created dimension (whether user or system set)
     */
    public Integer addDimension(DeclensionDimension dim) {
        DeclensionDimension addDim;
        Integer ret;
        
        // use given ID if available, create one otherwise
        if (dim.getId().equals(-1)) {
            ret = highestDimension + 1;            
        } else {
            ret = dim.getId();
        }
        
        // highest current dimension is always whichever value is larger. Prevent overlap.
        if (highestDimension < ret) {
            highestDimension = ret;
        }
        
        addDim = new DeclensionDimension(ret);
        addDim.setValue(dim.getValue());
        
        dimensions.put(ret, addDim);
               
        return ret;
    }
    
    /**
     * Deletes a dimension from this declension (it rhymes!)
     * @param id id of dimension to be deleted
     */
    public void deleteDimension(Integer id) {
        dimensions.remove(id);
    }
    
    public void setNotes(String _notes) {
        // handles very specific case where notes are set to null rather than blank
        notes = _notes == null ? "" : _notes;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public Collection<DeclensionDimension> getDimensions() {
        return dimensions.values();

    }

    protected Map<Integer, DeclensionDimension> getRawDimensions() {
        return dimensions;
    }
    
    /**
     * Selects and returns a declension dimension by its id if it exists
     * @param id id of declension dimension
     * @return declension dimension if it exists, null otherwise
     */
    public DeclensionDimension getDeclensionDimensionById(int id) {
        DeclensionDimension ret = null;
        
        if (dimensions.containsKey(id)) {
            ret = dimensions.get(id);
        }
        
        return ret;
    }
    
    public void setCombinedDimId(String _id) {
        combinedDimId = _id;
    }
    
    /**
     * Fetches combined id. In the case that this is a dimensionless template, it generates one.
     * @return 
     */
    public String getCombinedDimId() {
        String ret = combinedDimId;
        if (dimensionless) {
            // normal dimensional ids are comma delimited with guaranteed commas before and after. This should be unique.
            ret = id.toString();
        }
        
        return ret;
    }
    
    public void writeXMLTemplate(Document doc, Element rootElement, Integer relatedId) {
        Element wordNode = doc.createElement(PGTUtil.DECLENSION_XID);
        rootElement.appendChild(wordNode);

        Element nodeValue = doc.createElement(PGTUtil.DECLENSION_ID_XID);
        nodeValue.appendChild(doc.createTextNode(this.getId().toString()));
        wordNode.appendChild(nodeValue);

        nodeValue = doc.createElement(PGTUtil.DECLENSION_TEXT_XID);
        nodeValue.appendChild(doc.createTextNode(this.getValue()));
        wordNode.appendChild(nodeValue);

        nodeValue = doc.createElement(PGTUtil.DECLENSION_NOTES_XID);
        nodeValue.appendChild(doc.createTextNode(WebInterface.archiveHTML(this.getNotes())));
        wordNode.appendChild(nodeValue);

        nodeValue = doc.createElement(PGTUtil.DECLENSION_IS_TEMPLATE_XID);
        nodeValue.appendChild(doc.createTextNode("1"));
        wordNode.appendChild(nodeValue);

        nodeValue = doc.createElement(PGTUtil.DECLENSION_RELATED_ID_XID);
        nodeValue.appendChild(doc.createTextNode(relatedId.toString()));
        wordNode.appendChild(nodeValue);
        
        nodeValue = doc.createElement(PGTUtil.DECLENSION_IS_DIMENSIONLESS_XID);
        nodeValue.appendChild(doc.createTextNode(this.isDimensionless() ? PGTUtil.TRUE : PGTUtil.FALSE));
        wordNode.appendChild(nodeValue);

        // record dimensions of declension
        Iterator<DeclensionDimension> dimIt = this.getDimensions().iterator();
        while (dimIt.hasNext()) {
            dimIt.next().writeXML(doc, wordNode);
        }
    }
    
    public void writeXMLWordDeclension(Document doc, Element rootElement, Integer relatedId) {
        Element wordNode = doc.createElement(PGTUtil.DECLENSION_XID);
        rootElement.appendChild(wordNode);

        Element wordValue = doc.createElement(PGTUtil.DECLENSION_ID_XID);
        wordValue.appendChild(doc.createTextNode(this.getId().toString()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.DECLENSION_TEXT_XID);
        wordValue.appendChild(doc.createTextNode(this.getValue()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.DECLENSION_NOTES_XID);
        wordValue.appendChild(doc.createTextNode(this.getNotes()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.DECLENSION_RELATED_ID_XID);
        wordValue.appendChild(doc.createTextNode(relatedId.toString()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.DECLENSION_COMB_DIM_XID);
        wordValue.appendChild(doc.createTextNode(this.getCombinedDimId()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.DECLENSION_IS_TEMPLATE_XID);
        wordValue.appendChild(doc.createTextNode("0"));
        wordNode.appendChild(wordValue);
    }
    
    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        if (!(_node instanceof DeclensionNode)) {
            throw new ClassCastException("Object not of type DeclensionNode");
        }
        
        DeclensionNode node = (DeclensionNode) _node;
        
        this.setNotes(node.getNotes());
        this.setValue(node.getValue());
        this.setCombinedDimId(node.getCombinedDimId());
        this.setDimensionless(node.dimensionless);

        node.getRawDimensions().entrySet().forEach((entry) -> {
            DeclensionDimension copyOfDim = new DeclensionDimension(entry.getKey());
            copyOfDim.setEqual(entry.getValue());
            dimensions.put(entry.getKey(), copyOfDim);
        });
    }
}
