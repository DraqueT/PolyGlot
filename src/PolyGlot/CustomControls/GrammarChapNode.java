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
package PolyGlot.CustomControls;

import PolyGlot.ManagersCollections.GrammarManager;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This node represents a chapter within the grammar recording section of
 * PolyGlot
 * @author draque
 */
public class GrammarChapNode extends DefaultMutableTreeNode {
    private String name = "";
    private GrammarSectionNode buffer;
    private final GrammarManager parentManager;
    
    public GrammarChapNode(GrammarManager _parentManager) {
        parentManager = _parentManager;
        buffer = new GrammarSectionNode(parentManager);
    }
    
    public void setName(String _name) {
        name = _name;
    }
    public String getName() {
        return name;
    }
    
    /**
     * fetches section buffer
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
}
