/*
 * Copyright (c) 2015, draque
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * This node represents a chapter within the grammar recording section of
 * PolyGlot
 * @author draque
 */
public class GrammarChapNode extends DefaultMutableTreeNode {
    private String name = "";
    private List<GrammarSectionNode> sections = new ArrayList<GrammarSectionNode>();
    
    public void setName(String _name) {
        name = _name;
    }
    public String getName() {
        return name;
    }
    
    public List<GrammarSectionNode> getSections() {
        return sections;
    }    
    public void setSections(List<GrammarSectionNode> _sections) {
        sections = _sections;
    }
    
    /**
     * Adds a new section to the end of the list of sections in the chapter
     * @param newNode 
     */
    public void addSection(GrammarSectionNode newNode) {
        sections.add(newNode);
    }
    
    public void addSectionAtIndex(GrammarSectionNode newNode, int index) {
        if (index > sections.size()){
            sections.add(newNode);
        } else {            
            sections.add(index, newNode);
        }
    }
    
    /**
     * Removes passed node from list of sections in chapter.
     * @param remNode Section to be removed.
     */
    @Override
    public void remove(MutableTreeNode remNode) {
        sections.remove(remNode);
        super.remove(remNode);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
