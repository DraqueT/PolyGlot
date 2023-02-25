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
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Node;

/**
 *
 * @author draquethompson
 */
public class PartOfSpeechParser extends BaseParser {
    private TypeNode partOfSpeech;
    private int id;

    public PartOfSpeechParser(List<String> _parseIssues) {
        super(_parseIssues);
    }
    
    @Override
    public void parse(Node parent, DictCore core) throws PDomException {
        partOfSpeech = core.getTypes().getBufferType();
        super.parse(parent, core);
        
        try {
            core.getTypes().insert(id);
        } catch (Exception e) {
            throw new PDomException("Part of Speech Load error: " + partOfSpeech.getValue(), e);
        }
    }

    @Override
    public void consumeChild(Node node, DictCore core) throws Exception {
        switch (node.getNodeName()) {
            case PGTUtil.POS_ID_XID, PGTUtil.POS_ID_XID_LEGACY -> {
                id = Integer.parseInt(node.getTextContent());
            }
            case PGTUtil.POS_NAME_XID, PGTUtil.POS_NAME_XID_LEGACY -> {
                partOfSpeech.setValue(node.getTextContent());
            }
            case PGTUtil.POS_NOTES_XID, PGTUtil.POS_NOTES_XID_LEGACY -> {
                partOfSpeech.setNotes(node.getTextContent());
            }
            case PGTUtil.POS_DEF_MAN_XID, PGTUtil.POS_DEF_MAN_XID_LEGACY -> {
                partOfSpeech.setDefMandatory(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.POS_PROC_MAN_XID, PGTUtil.POS_PROC_MAN_XID_LEGACY -> {
                partOfSpeech.setProcMandatory(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.POS_PATTERN_XID, PGTUtil.POS_PATTERN_XID_LEGACY -> {
                partOfSpeech.setPattern(node.getTextContent(), core);
            }
            case PGTUtil.POS_GLOSS_XID, PGTUtil.POS_GLOSS_XID_LEGACY -> {
                partOfSpeech.setGloss(node.getTextContent());
            }
            default ->
                throw new PDomException("Unexpected node in " + this.getClass().getName() + " : " + node.getNodeName());
        }
    }
    
}
