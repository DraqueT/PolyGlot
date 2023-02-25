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
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Node;

/**
 *
 * @author draquethompson
 */
public class DeclensionParser extends BaseParser {

    private ConjugationNode conjugation;
    boolean template = false;
    int relatedId;
    
    public DeclensionParser(List<String> _parseIssues) {
        super(_parseIssues);
    }
    
    @Override
    public void parse(Node parent, DictCore core) throws PDomException {
        conjugation = new ConjugationNode(-1, core.getConjugationManager());
        super.parse(parent, core);
        
        int id  = conjugation.getId();
        
        if (id == Integer.MAX_VALUE || id < 0) {
            throw new PDomException("Invalid conjugation id: " + id);
        }
        
        // declension templates handled differently than actual saved declensions for words
        if (template) {
            core.getConjugationManager().addConjugationToTemplate(relatedId, id, conjugation);
        } else {
            // ensures proper set for nondimensional values
            conjugation.setCombinedDimId(conjugation.getCombinedDimId());
            
            core.getConjugationManager().addConjugationToWord(relatedId, id, conjugation);
        }
    }

    @Override
    public void consumeChild(Node node, DictCore core) throws Exception {
        switch (node.getNodeName()) {
            case PGTUtil.DECLENSION_ID_XID -> {
                conjugation.setId(Integer.valueOf(node.getTextContent()));
            }
            case PGTUtil.DECLENSION_TEXT_XID -> {
                conjugation.setValue(node.getTextContent());
            }
            case PGTUtil.DECLENSION_NOTES_XID -> {
                conjugation.setNotes(node.getTextContent());
            }
            case PGTUtil.DECLENSION_IS_TEMPLATE_XID -> {
                // TODO: Why does it work like this?
                template = node.getTextContent().equals("1");
            }
            case PGTUtil.DECLENSION_RELATED_ID_XID -> {
                relatedId = Integer.parseInt(node.getTextContent());
            }
            case PGTUtil.DECLENSION_IS_DIMENSIONLESS_XID -> {
                conjugation.setDimensionless(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.DIMENSION_NODE_XID -> {
                new DimensionNodeParser(parseIssues, conjugation).parse(node, core);
            }
            case PGTUtil.DECLENSION_COMB_DIM_XID -> {
                conjugation.setCombinedDimId(node.getTextContent());
            }
            case PGTUtil.DECLENSION_MANDATORY_DEPRECATED -> {
                // deprecated functionality: do nothing
            }
            default ->
                throw new PDomException("Unexpected node in " + this.getClass().getName() + " : " + node.getNodeName());
        }
    }
    
}
