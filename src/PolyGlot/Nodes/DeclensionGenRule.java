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
package PolyGlot.Nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * class to contain declension transformation rule and all transformations
 * associated with the rule
 * @author draque
 */
public class DeclensionGenRule implements Comparable<DeclensionGenRule> {
    private int typeId;
    private int index;
    private String combinationId;
    private String regex = "";
    private String name = "";
    private List<DeclensionGenTransform> transformations = new ArrayList<>();
    private DeclensionGenTransform transBuffer = new DeclensionGenTransform();
    
    /**
     * Gets current declension transform buffer
     * @return current transform buffer
     */
    public DeclensionGenTransform getTransBuffer() {
        return transBuffer;
    }
    
    /**
     * Inserts current transform buffer, then sets to blank
     */
    public void insertTransBuffer() {
        addTransform(transBuffer);
        transBuffer = new DeclensionGenTransform();
    }
    
    /**
     * Sets declension gen rule equal to passed value, copying all subnodes
     * @param r rule to copy from
     * @param setTypeAndComb set to true to copy the typeId and combinationId
     * from the original, false to skip values
     */
    public void setEqual(DeclensionGenRule r, boolean setTypeAndComb) {
        if (setTypeAndComb) {
            typeId = r.getTypeId();
            combinationId = r.getCombinationId();
        }
        name = r.getName();
        regex = r.getRegex();
        transformations.clear();
        for (DeclensionGenTransform copyFrom : r.getTransforms()) {
            DeclensionGenTransform copyTo = new DeclensionGenTransform();
            copyTo.setEqual(copyFrom);
            transformations.add(copyTo);
        }
    }
    
    /**
     * initializes new declension rule
     * @param _typeId TypeID of type this rule applies to
     * @param _combinationId the combined ID of the constructed declension rule applies to
     */
    public DeclensionGenRule(int _typeId, String _combinationId) {
        typeId = _typeId;
        combinationId = _combinationId;
    }
    
    public DeclensionGenRule() {
        typeId = -1;
        combinationId = "";
    }
    
    /**
     * adds transformation to rule
     * @param trans transformation to add
     */
    public void addTransform(DeclensionGenTransform trans) {
        transformations.add(trans);
    }
    
    /**
     * gets all transformations for this rule
     * @return iterator of DeclensionGenTransform objects
     */
    public List<DeclensionGenTransform> getTransforms() {
        return transformations;
    }
    
    /**
     * wipes all transformations
     */
    public void wipeTransforms() {
        transformations = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String _name) {
        name = _name;
    }
    
    public int getTypeId() {
        return typeId;
    }
    
    public void setTypeId(int _typeId) {
        typeId = _typeId;
    }
    
    public String getCombinationId() {
        return combinationId;
    }
    
    public void setCombinationId(String _combinationId) {
        combinationId = _combinationId;
    }
        
    public String getRegex() {
        return regex;
    }
    
    public void setRegex(String _regex) {
        regex = _regex;
    }
    
    @Override
    public String toString() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
    /**
     * organizes by index number
     * @param _compare node to compare
     * @return 
     */
    @Override
    public int compareTo(DeclensionGenRule _compare) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;
        int compIndex = _compare.getIndex();
        int ret;
        
        
        if (index > compIndex) {
            ret = AFTER;
        } else if (index == compIndex) {
            ret = EQUAL;
        } else {
            ret = BEFORE;
        }
        
        return ret;
    }
}
