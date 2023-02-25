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
import org.darisadesigns.polyglotlina.Nodes.EtyExternalParent;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Node;

/**
 *
 * @author draquethompson
 */
public class EtymologyExternalWordNode extends BaseParser {
    
    int childId;
    EtyExternalParent externalParent;

    public EtymologyExternalWordNode(List<String> _parseIssues, int _childId) {
        super(_parseIssues);
        childId = _childId;
    }
    
    @Override
    public void parse(Node parent, DictCore core) throws PDomException {
        externalParent = new EtyExternalParent();
        super.parse(parent, core);
        core.getEtymologyManager().addExternalRelation(externalParent, childId);
    }

    @Override
    public void consumeChild(Node node, DictCore core) throws Exception {
        switch(node.getNodeName()) {
            case PGTUtil.ETY_EXTERNAL_WORD_VALUE_XID -> {
                externalParent.setValue(node.getTextContent());
            }
            case PGTUtil.ETY_EXTERNAL_WORD_ORIGIN_XID -> {
                externalParent.setExternalLanguage(node.getTextContent());
            }
            case PGTUtil.ETY_EXTERNAL_WORD_DEFINITION_XID -> {
                externalParent.setDefinition(node.getTextContent());
            }
            default ->
                throw new PDomException("Unexpected node in " + this.getClass().getName() + " : " + node.getNodeName());
        }
    }
    
}
