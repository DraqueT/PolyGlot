/*
 * Copyright (c) 2018-2021, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import java.util.Objects;
import org.darisadesigns.polyglotlina.Nodes.ToDoNode;
import org.darisadesigns.polyglotlina.PGTUtil;
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
            root = new ToDoNode(null, "", false);
        }
        return root;
    }
    
    /**
     * Writes to do information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element toDos = doc.createElement(PGTUtil.TODO_LOG_XID);
        
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
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (comp == this) {
            ret = true;
        } else if (comp instanceof ToDoManager) {
            ToDoManager compMan = (ToDoManager)comp;
            
            ret = (root == null && compMan.root == null) || root.equals(compMan.root);
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.root);
        return hash;
    }
}
