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

import java.util.ArrayList;
import java.util.List;

/**
 * class to contain declension transformation rule and all transformations
 * associated with the rule
 * @author draque
 */
public class DeclensionGenRule {
    private int typeId;
    private String combinationId;
    private String regex = "";
    private String name = "";
    private List<DeclensionGenTransform> transformations = new ArrayList<DeclensionGenTransform>();
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
        transformations = new ArrayList<DeclensionGenTransform>();
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
}
