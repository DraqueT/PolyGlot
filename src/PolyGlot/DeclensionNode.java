/*
 * Copyright (c) 2014, Draque Thompson, draquemail@gmail.com
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

package PolyGlot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// TODO: mandatory now moved to dimensions, remove references here and fix elsewhere (worth the bother?)

/**
 *
 * @author draque
 */
public class DeclensionNode extends DictNode{
    private String notes = "";
    private String combinedDimId = "";
    private boolean mandatory = false;
    private int highestDimension = 1;
    private Map<Integer, DeclensionDimension> dimensions = new HashMap<Integer, DeclensionDimension>();
    private DeclensionDimension buffer = new DeclensionDimension(-1);
    
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
     */
    public void insertBuffer() {
        this.addDimension(buffer);
        buffer = new DeclensionDimension(-1);
    }
    
    /**
     * clears current value of buffer
     */
    public void clearBuffer() {
        buffer = new DeclensionDimension(-1);
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
        highestDimension = highestDimension > ret ? highestDimension : ret;
        
        addDim = new DeclensionDimension(ret);
        addDim.setValue(dim.getValue());
        addDim.setMandatory(dim.isMandatory());
        
        dimensions.put(ret, addDim);
               
        return ret;
    }
    
    /**
     * eliminates all dimensions from node
     */
    public void clearDimensions() {
        dimensions.clear();
    }
    
    /**
     * Deletes a dimension from this declension (it rhymes!)
     * @param id id of dimension to be deleted
     */
    public void deleteDimension(Integer id) {
        if (dimensions.containsKey(id)) {
            dimensions.remove(id);
        }
    }
        
    public DeclensionNode(Integer _declentionId) {
        id = _declentionId;
    }
    
    public boolean isMandatory() {
        return mandatory;
    }
    
    public void setMandatory(boolean _mandatory) {
        mandatory = _mandatory;
    }
    
    public void setNotes(String _notes) {
        notes = _notes;
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
    
    public void setCombinedDimId(String _id) {
        combinedDimId = _id;
    }
    
    public String getCombinedDimId() {
        return combinedDimId;
    }
    
    @Override
    public void setEqual(DictNode _node) {
        DeclensionNode node = (DeclensionNode) _node;
        
        this.setNotes(node.getNotes());
        this.setValue(node.getValue());
        this.setMandatory(node.isMandatory());
        this.setCombinedDimId(node.getCombinedDimId());
        dimensions = node.getRawDimensions();
    }
}
