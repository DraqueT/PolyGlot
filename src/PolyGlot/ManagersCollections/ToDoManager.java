/*
 * Copyright (c) 2018, DThompson
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
package PolyGlot.ManagersCollections;

import PolyGlot.Nodes.ToDoNode;
import PolyGlot.PGTUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Manager of the To Do list
 * @author DThompson
 */
public class ToDoManager {
    private ToDoNode root = null;
    private ToDoNode bufferNode;
    
    public ToDoNode getRoot() {
        if (root == null) {
            root = new ToDoNode(null, PGTUtil.ToDoRoot, false);
        }
        return root;
    }
    
    /**
     * Writes todo information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element toDos = doc.createElement(PGTUtil.ToDoLogXID);
        
        getRoot().writeXML(doc, toDos);
        
        rootElement.appendChild(toDos);
    }
    
    public ToDoNode getBuffer() {
        if (root == null) {
            bufferNode = getRoot();
        }
        
        return bufferNode;
    }
    
    public void pushBuffer() {
       if (root == null) {
           getBuffer();
       } 
       else {
            ToDoNode newBuffer = new ToDoNode(bufferNode, "", false);
            bufferNode.addChild(newBuffer);
            bufferNode = newBuffer;
       }
    }
    
    public void popBuffer() {
        bufferNode = bufferNode.getParent();
    }
}
