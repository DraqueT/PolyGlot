/*
 * Copyright (c) 2015-2022, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
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
package org.darisadesigns.polyglotlina.Desktop.CustomControls;

import org.darisadesigns.polyglotlina.PGTUtil;
import java.util.Enumeration;
import java.util.Objects;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import org.darisadesigns.polyglotlina.CustomControls.GrammarChapNode;
import org.darisadesigns.polyglotlina.CustomControls.GrammarSectionNode;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopGrammarManager;
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
public class DesktopGrammarChapNode extends DefaultMutableTreeNode implements GrammarChapNode {

    private String name = "";
    private DesktopGrammarSectionNode buffer;
    private final DesktopGrammarManager parentManager;

    public DesktopGrammarChapNode(DesktopGrammarManager _parentManager) {
        parentManager = _parentManager;
        buffer = new DesktopGrammarSectionNode(parentManager);
    }

    public DesktopGrammarChapNode(String _name, DesktopGrammarManager _parentManager) {
        super(_name);
        parentManager = _parentManager;
    }

    @Override
    public void setName(String _name) {
        name = _name;
    }

    @Override
    public String getName() {
        return name;
    }

    public Enumeration children(String _filter) {
        return internalChildren(_filter);
    }
    
    @Override
    /**
     * Overridden to prevent unwanted removals
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
        
        if (parentManager != null && node instanceof DesktopGrammarChapNode) {
            parentManager.removeChapter((DesktopGrammarChapNode)node);
        }
    }
    
    public void doInsert(MutableTreeNode node, int index) {
        super.insert(node, index);
        
        if (parentManager != null && node instanceof DesktopGrammarChapNode) {
            parentManager.addChapterAtIndex((DesktopGrammarChapNode)node, index);
        }
    }

    @SuppressWarnings("UseOfObsoleteCollectionType")
    private Enumeration internalChildren(String filter) {
        Enumeration ret;
        if (filter.isEmpty() || children == null) {
            ret = super.children();
        } else if (children.elementAt(0) instanceof DesktopGrammarSectionNode) {
            java.util.Vector<DesktopGrammarSectionNode> v = new java.util.Vector<>();

            for (Object curObject : children.toArray()) {
                DesktopGrammarSectionNode curNode = (DesktopGrammarSectionNode)curObject;
                if (curNode.getName().toLowerCase().contains(filter.toLowerCase())
                        || curNode.getSectionText().toLowerCase().contains(filter.toLowerCase())) {
                    v.add(curNode);
                }
            }

            ret = v.elements();
        } else if (children.elementAt(0) instanceof DesktopGrammarChapNode) {
            java.util.Vector<DesktopGrammarChapNode> v = new java.util.Vector<>();
            
            for (DesktopGrammarChapNode curNode : (DesktopGrammarChapNode[]) children.toArray()) {
                if (curNode.name.toLowerCase().contains(filter.toLowerCase())) {
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
    @Override
    public GrammarSectionNode getBuffer() {
        return buffer;
    }

    /**
     * inserts current section buffer to sections and clears it
     */
    @Override
    public void insert() {
        this.add(buffer);
        clear();
    }

    /**
     * clears current buffer
     */
    @Override
    public void clear() {
        buffer = new DesktopGrammarSectionNode(parentManager);
    }

    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public void writeXML(Document doc, Element rootElement) {
        Element chapNode = doc.createElement(PGTUtil.GRAMMAR_CHAPTER_NODE_XID);
            rootElement.appendChild(chapNode);

            Element chapElement = doc.createElement(PGTUtil.GRAMMAR_CHAPTER_NAME_XID);
            chapElement.appendChild(doc.createTextNode(this.name));
            chapNode.appendChild(chapElement);
            
            chapElement = doc.createElement(PGTUtil.GRAMMAR_SECTIONS_LIST_XID);
            
            for (int i = 0; i < this.getChildCount(); i++) {
                DesktopGrammarSectionNode curSec = (DesktopGrammarSectionNode)this.getChildAt(i);                
                curSec.writeXML(doc, chapElement);
            }
            
            chapNode.appendChild(chapElement);
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (comp == this) {
            ret = true;
        } else if (comp instanceof DesktopGrammarChapNode) {
            DesktopGrammarChapNode compChap = (DesktopGrammarChapNode)comp;
            
            ret = (children == null && compChap.children == null) 
                    || ( children != null && children.equals(compChap.children));
            ret = ret && name.trim().equals(compChap.name.trim());
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.name);
        return hash;
    }
}
