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

import PolyGlot.PGTUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A single dimensional value of a conjugation element (for example, tense)
 * @author draque
 */
public class DeclensionDimension extends DictNode {
    private boolean mandatory = false;
    
    public DeclensionDimension(Integer _id) {
        id = _id;
    }
    
    public DeclensionDimension() {
        id = -1;
    }
    
    @Override
    public void setEqual(DictNode _node) {
        DeclensionDimension copyNode = (DeclensionDimension)_node;
        
        this.value = copyNode.getValue();
        mandatory = copyNode.isMandatory();
    }
    
    public void setMandatory(boolean _mandatory) {
        mandatory = _mandatory;
    }
    
    public boolean isMandatory() {
        return mandatory;
    }
    
    public void writeXML(Document doc, Element rootElement) {
        Element wordValue = doc.createElement(PGTUtil.dimensionNodeXID);
        Element dimNode = doc.createElement(PGTUtil.dimensionIdXID);
        dimNode.appendChild(doc.createTextNode(this.getId().toString()));
        wordValue.appendChild(dimNode);

        dimNode = doc.createElement(PGTUtil.dimensionNameXID);
        dimNode.appendChild(doc.createTextNode(this.getValue()));
        wordValue.appendChild(dimNode);

        dimNode = doc.createElement(PGTUtil.dimensionMandXID);
        dimNode.appendChild(doc.createTextNode(this.isMandatory() ? PGTUtil.True : PGTUtil.False));
        wordValue.appendChild(dimNode);

        rootElement.appendChild(wordValue);
    }
}
