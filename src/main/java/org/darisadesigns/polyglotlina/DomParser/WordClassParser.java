/*
 * Copyright (c) 2023, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.DomParser;

import java.util.List;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Node;

/**
 *
 * @author draquethompson
 */
public class WordClassParser extends BaseParser{

    private WordClass wordClass;
    
    public WordClassParser(List<String> _parseIssues) {
        super(_parseIssues);
    }
        
    @Override
    public void parse(Node parent, DictCore core) throws PDomException {
        // the buffer should not default to "apply to all."
        wordClass = core.getWordClassCollection().getBuffer();
        wordClass.deleteApplyType(-1);
        super.parse(parent, core);
        
        try {
            core.getWordClassCollection().insert();
        } catch (Exception e) {
            throw new PDomException(
                    String.format("Problem loading word class: ", wordClass.getValue()), e
            );
        }
    }
    
    @Override
    public void consumeChild(Node node, DictCore core) throws Exception {
        switch (node.getNodeName()) {
            case PGTUtil.CLASS_ID_XID-> {
                wordClass.setId(Integer.valueOf(node.getTextContent()));
            }
            case PGTUtil.CLASS_NAME_XID-> {
                wordClass.setValue(node.getTextContent());
            }
            case PGTUtil.CLASS_IS_FREETEXT_XID-> {
                wordClass.setFreeText(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.CLASS_IS_ASSOCIATIVE_XID-> {
                wordClass.setAssociative(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.CLASS_APPLY_TYPES_XID-> {
                String types = node.getTextContent();
                
                for (String curType : types.split(",")) {
                    int typeId = Integer.parseInt(curType);
                    wordClass.addApplyType(typeId);
                }
            }
            case PGTUtil.CLASS_VALUES_COLLECTION_XID-> {
                new WordClassValueCollectionParser(parseIssues, wordClass).parse(node, core);
            }
            default ->
                throw new PDomException("Unexpected node in " + this.getClass().getName() + " : " + node.getNodeName());
        }
    }
    
}
