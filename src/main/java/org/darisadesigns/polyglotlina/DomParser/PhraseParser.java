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
import org.darisadesigns.polyglotlina.Nodes.PhraseNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Node;

/**
 *
 * @author draquethompson
 */
public class PhraseParser extends BaseParser {
    
    PhraseNode phrase;

    public PhraseParser(List<String> _parseIssues) {
        super(_parseIssues);
    }
    
    @Override
    public void parse(Node parent, DictCore core) throws PDomException {
        phrase = new PhraseNode();
        super.parse(parent, core);
        
        try {
            core.getPhraseManager().addNode(phrase);
        } catch (Exception e) {
            throw new PDomException(e);
        }
    }

    @Override
    public void consumeChild(Node node, DictCore core) throws Exception {
        switch (node.getNodeName()) {
            case PGTUtil.PHRASE_ID_XID -> {
                phrase.setId(Integer.valueOf(node.getTextContent()));
            }
            case PGTUtil.PHRASE_GLOSS_XID -> {
                phrase.setGloss(node.getTextContent());
            }
            case PGTUtil.PHRASE_CONPHRASE_XID -> {
                phrase.setConPhrase(node.getTextContent());
            }
            case PGTUtil.PHRASE_LOCALPHRASE_XID -> {
                phrase.setLocalPhrase(node.getTextContent());
            }
            case PGTUtil.PHRASE_PRONUNCIATION_XID -> {
                phrase.setPronunciation(node.getTextContent());
            }
            case PGTUtil.PHRASE_PRONUNCIATION_OVERRIDE_XID -> {
                phrase.setProcOverride(PGTUtil.TRUE.equals(node.getTextContent()));
            }
            case PGTUtil.PHRASE_NOTES_XID -> {
                phrase.setNotes(node.getTextContent());
            }
            case PGTUtil.PHRASE_ORDER_XID -> {
                phrase.setOrderId(Integer.parseInt(node.getTextContent()));
            }
            default ->
                throw new PDomException("Unexpected node in " + this.getClass().getName() + " : " + node.getNodeName());
        }
    }
    
}
