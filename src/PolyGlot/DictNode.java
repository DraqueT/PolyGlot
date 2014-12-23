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

// This is the type which all nodes and storage types extend.
package PolyGlot;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author draque
 */
public abstract class DictNode implements Comparable<DictNode> {
    // this represents the primary string value of the node, whether it is
    // a word, a declension type, etc.
    protected String value = "";
    
    protected Integer id = 0;
    
    // used for alphabetic ordering of nodes
    private Map<Character, Integer> alphaOrder = new HashMap<Character, Integer>();
    
    /**
     * Sets a node equal to the argument node
     * @param _node Node to set all values equal to.
     */
    abstract public void setEqual(DictNode _node);
    
    public void setId(Integer _id) {
        id = _id;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setAlphaOrder(Map _alphaOrder) {
        alphaOrder = _alphaOrder;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String conWord) {
        this.value = conWord.trim();
    }

    // implements compareTo in way that custom alpha sorting may be used
    @Override
    public int compareTo(DictNode _compare) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;
        final String comp = _compare.getValue();
        final String me = this.getValue();
        
        if (comp.equals(me)) {
            return EQUAL;
        }
        
        if (comp.equals("")) {
            return AFTER;
        }
        if (me.equals("")) {
            return BEFORE;
        }
        
        Character compChar = comp.charAt(0);
        Character meChar = me.charAt(0);

        // if no settings,or missing settings for given character, just use default
        if (alphaOrder.isEmpty() || !alphaOrder.containsKey(meChar) || !alphaOrder.containsKey(compChar)) {
            return this.getValue().compareToIgnoreCase(_compare.getValue());
        }
        
        if(compChar.equals(meChar)) {
            ConWord compChild = new ConWord();
            ConWord thisChild = new ConWord();
            
            compChild.setAlphaOrder(alphaOrder);
            thisChild.setAlphaOrder(alphaOrder);
            
            compChild.setValue(_compare.getValue().substring(1));
            thisChild.setValue(this.getValue().substring(1));
            
            return thisChild.compareTo(compChild);
        } else if (alphaOrder.get(comp.charAt(0)) > alphaOrder.get(me.charAt(0))) {
            return BEFORE;
        } else {
            return AFTER;
        }    
    }
    
    @Override
    public String toString() {
        return value;
    }
}
