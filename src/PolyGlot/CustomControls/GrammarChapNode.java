/*
 * Copyright (c) 2015-2018, Draque Thompson
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
package PolyGlot.CustomControls;

import PolyGlot.ManagersCollections.GrammarManager;
import PolyGlot.PGTUtil;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This node represents a chapter within the grammar recording section of
 * PolyGlot
 *
 * Uses obsolete Vector class due to how the underlying class API is written...
 * 
 * @author draque
 */
@SuppressWarnings( "deprecation" )
public class GrammarChapNode extends DefaultMutableTreeNode {

    private String name = "";
    private GrammarSectionNode buffer;
    private final GrammarManager parentManager;

    public GrammarChapNode(GrammarManager _parentManager) {
        parentManager = _parentManager;
        buffer = new GrammarSectionNode(parentManager);
    }

    public GrammarChapNode(String name, GrammarManager _parentManager) {
        super(name);
        parentManager = _parentManager;
    }

    public void setName(String _name) {
        name = _name;
    }

    public String getName() {
        return name;
    }

    public Enumeration children(String _filter) {
        return internalChildren(_filter);
    }
    
    @Override
    /**
     * Overriden to prevent unwanted removals
     */
    public void remove(MutableTreeNode node) {
        // do nothing (preserves tree)
    }

    /**
     * Actual removal code
     * @param node node to remove
     */
    public void doRemove(MutableTreeNode node) {
        super.remove(node);
    }
    
    @SuppressWarnings("UseOfObsoleteCollectionType")
    private Enumeration internalChildren(String filter) {
        Enumeration ret;
        if (filter.length() == 0 || children == null) {
            ret = super.children();
        } else if (children.elementAt(0) instanceof GrammarSectionNode) {
            java.util.Vector<GrammarSectionNode> v = new java.util.Vector<>();

            for (Object curObject : children.toArray()) {
                GrammarSectionNode curNode = (GrammarSectionNode)curObject;
                if (curNode.getName().toLowerCase().contains(filter.toLowerCase())
                        || curNode.getSectionText().toLowerCase().contains(filter.toLowerCase())) {
                    v.add(curNode);
                }
            }

            ret = v.elements();
        } else if (children.elementAt(0) instanceof GrammarChapNode) {
            java.util.Vector<GrammarChapNode> v = new java.util.Vector<>();
            
            for (GrammarChapNode curNode : (GrammarChapNode[]) children.toArray()) {
                if (curNode.getName().toLowerCase().contains(filter.toLowerCase())) {
                    v.add(curNode);
                }
            }
            
            ret = v.elements();
        } else {
            // return null if unknown child. Error will bubble above this.
            ret = null;
        }
        
        return ret;
    }

    /**
     * fetches section buffer
     *
     * @return section buffer
     */
    public GrammarSectionNode getBuffer() {
        return buffer;
    }

    /**
     * inserts current section buffer to sections and clears it
     */
    public void insert() {
        this.add(buffer);
        clear();
    }

    /**
     * clears current buffer
     */
    public void clear() {
        buffer = new GrammarSectionNode(parentManager);
    }

    @Override
    public String toString() {
        return name;
    }
    
    public void writeXML(Document doc, Element rootElement) {
        Element chapNode = doc.createElement(PGTUtil.grammarChapterNodeXID);
            rootElement.appendChild(chapNode);

            Element chapElement = doc.createElement(PGTUtil.grammarChapterNameXID);
            chapElement.appendChild(doc.createTextNode(this.getName()));
            chapNode.appendChild(chapElement);
            
            chapElement = doc.createElement(PGTUtil.grammarSectionsListXID);
            
            for (int i = 0; i < this.getChildCount(); i++) {
                GrammarSectionNode curSec = (GrammarSectionNode)this.getChildAt(i);                
                curSec.writeXML(doc, chapElement);
            }
            
            chapNode.appendChild(chapElement);
    }
}
