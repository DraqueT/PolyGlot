/*
 * Copyright (c) 2019, Draque Thompson
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
package org.darisadesigns.polyglotlina.Desktop.CustomControls;

import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection.ConWordDisplay;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;

/**
 *
 * @author draque
 */
public class PListModelLexicon extends DefaultListModel<ConWordDisplay> {
    private final Map<Integer, ConWordDisplay> wordToDisplay = new HashMap<>();
    
    @Override
    public void addElement(ConWordDisplay element) {
        wordToDisplay.put(element.getConWord().getId(), element);
        super.addElement(element);
    }
    
    /**
     * Gets display of conword from the conword itself
     * returns null if not exists
     * @param conWord
     * @return 
     */
    public ConWordDisplay getDisplayFromWord(ConWord conWord) {
        ConWordDisplay ret = null;
        
        if (wordToDisplay.containsKey(conWord.getId())) {
            ret = wordToDisplay.get(conWord.getId());
        }
        
        return ret;
    }
}
