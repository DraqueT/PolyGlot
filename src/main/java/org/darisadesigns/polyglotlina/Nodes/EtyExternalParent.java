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
package org.darisadesigns.polyglotlina.Nodes;

/**
 * This node represents an external etymological parent
 * @author DThompson
 */
public class EtyExternalParent extends ConWord {
    
    private String externalLanguage = "";

    /**
     * @return the externalLanguage
     */
    public String getExternalLanguage() {
        return externalLanguage;
    }

    /**
     * @param _externalLanguage the externalLanguage to set
     */
    public void setExternalLanguage(String _externalLanguage) {
        this.externalLanguage = _externalLanguage;
    }
    
    public String getUniqueId() {
        return value + externalLanguage;
    }
    
    @Override
    public String toString() {
        return value + (externalLanguage.isEmpty() ? ""
                : " (" + externalLanguage + ")");
    }

    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        if (_node instanceof EtyExternalParent) {
            EtyExternalParent node = (EtyExternalParent)_node;
            definition = node.definition;
            externalLanguage = node.externalLanguage;
            value = node.value;
        } else if (_node != null) {
            throw new ClassCastException("Type: " 
                    + _node.getClass().getCanonicalName() 
                    + " cannot be explicitly converted to " 
                    + getClass().getCanonicalName() + ".");
        }
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp != null && getClass() == comp.getClass()) {
            EtyExternalParent c = (EtyExternalParent)comp;
            ret = value.equals(c.value);
            ret = ret && externalLanguage.equals(c.externalLanguage);
            ret = ret && definition.equals(c.definition);
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
