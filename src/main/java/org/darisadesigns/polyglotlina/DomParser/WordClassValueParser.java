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
public class WordClassValueParser extends BaseParser {

    private final WordClass parent;
    private Integer id;
    private String value;
    
    public WordClassValueParser(List<String> _parseIssues, WordClass _parent) {
        super(_parseIssues);
        parent = _parent;
    }
    
    @Override
    public void parse(Node parentNode, DictCore core) throws PDomException {
        super.parse(parentNode, core);
        
        try {
            parent.addValue(value, id);
        } catch (Exception e) {
            throw new PDomException(
                    String.format(
                            "Duplicate class value: %s in word class %s", 
                            value, 
                            parent.getValue()
                    )
            );
        }
    }

    @Override
    public void consumeChild(Node node, DictCore core) throws Exception {
        switch(node.getNodeName()) {
            case PGTUtil.CLASS_VALUE_ID_XID -> {
                id = Integer.valueOf(node.getTextContent());
            }
            case PGTUtil.CLASS_VALUE_NAME_XID -> {
                value = node.getTextContent();
            }
            default ->
                throw new PDomException("Unexpected node in " + this.getClass().getName() + " : " + node.getNodeName());
        }
    }
    
}
