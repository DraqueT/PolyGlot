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
import org.darisadesigns.polyglotlina.Nodes.FamNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Node;

/**
 *
 * @author draquethompson
 */
public class FamilyNodeParser extends BaseParser {

    private final FamNode parentNode;
    private FamNode famNode;

    public FamilyNodeParser(List<String> _parseIssues, FamNode _parentNode) {
        super(_parseIssues);
        parentNode = _parentNode;
    }

    @Override
    public void parse(Node parent, DictCore core) throws PDomException {
        famNode = new FamNode(parentNode, core.getFamManager());
        super.parse(parent, core);
        parentNode.addNode(famNode);
    }

    @Override
    public void consumeChild(Node node, DictCore core) throws Exception {
        switch (node.getNodeName()) {
            case PGTUtil.FAM_NAME_XID -> {
                famNode.setValue(node.getTextContent());
            }
            case PGTUtil.FAM_NOTES_XID -> {
                famNode.setNotes(node.getTextContent());
            }
            case PGTUtil.FAM_WORD_XID -> {
                try {
                    famNode.addWord(core.getWordCollection()
                            .getNodeById(Integer.valueOf(node.getTextContent())));
                }
                catch (NumberFormatException e) {
                    throw new PDomException(e);
                }
            }
            case PGTUtil.FAM_NODE_XID -> {
                new FamilyNodeParser(parseIssues, famNode).parse(node, core);
            }
            default ->
                throw new PDomException("Unexpected node in " + this.getClass().getName() + " : " + node.getNodeName());
        }
    }

}
