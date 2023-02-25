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
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.WebInterface;
import org.w3c.dom.Node;

/**
 *
 * @author draquethompson
 */
public class WordParser extends BaseParser {
    private Integer id;
    private ConWord conWord;

    public WordParser(List<String> _parseIssues) {
        super(_parseIssues);
    }
    
    @Override
    public void parse(Node parent, DictCore core) throws PDomException {
        core.getWordCollection().clear();
        conWord = core.getWordCollection().getBuffer();
        super.parse(parent, core);
        
        try {
            core.getWordCollection().insert(id);
        } catch (Exception e) {
            throw new PDomException("Word insertion error: " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void consumeChild(Node node, DictCore core) throws Exception {
        switch(node.getNodeName()) {
            case PGTUtil.WORD_ID_XID -> {
                id = Integer.valueOf(node.getTextContent());
            }
            case PGTUtil.LOCALWORD_XID -> {
                conWord.setLocalWord(node.getTextContent());
            }
            case PGTUtil.CONWORD_XID -> {
                conWord.setValue(node.getTextContent());
            }
            case PGTUtil.WORD_POS_ID_XID, PGTUtil.WORD_POS_ID_XID_LEGACY -> {
                conWord.setWordTypeId(Integer.parseInt(node.getTextContent()));
            }
            case PGTUtil.WORD_PROC_XID -> {
                conWord.setPronunciation(node.getTextContent());
            }
            case PGTUtil.WORD_DEF_XID -> {
                try {
                    conWord.setDefinition(WebInterface.unarchiveHTML(node.getTextContent(), core));
                } catch (Exception e) {
                    throw new PDomException(e);
                }
            }
            case PGTUtil.WORD_PROCOVERRIDE_XID -> {
                conWord.setProcOverride(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.WORD_AUTODECLOVERRIDE_XID -> {
                conWord.setOverrideAutoConjugate(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.WORD_RULEOVERRIDE_XID -> {
                conWord.setRulesOverride(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.WORD_CLASSCOLLECTION_XID -> {
                new AssignedClassCollectionParser(parseIssues, conWord).parse(node, core);
            }
            case PGTUtil.WORD_CLASS_TEXT_VAL_COLLECTION_XID -> {
                new AssignedTextClassCollectionParser(parseIssues, conWord).parse(node, core);
            }
            case PGTUtil.WORD_ETY_NOTES_XID -> {
                conWord.setEtymNotes(node.getTextContent());
            }
            default ->
                throw new PDomException("Unexpected node in " + this.getClass().getName() + " : " + node.getNodeName());
        }
    }
    
}
