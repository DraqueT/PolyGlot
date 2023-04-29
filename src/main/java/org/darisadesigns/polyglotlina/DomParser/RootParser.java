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

import java.time.Instant;
import java.util.List;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Node;

/**
 *
 * @author draquethompson
 */
public class RootParser extends BaseParser {

    int fileVersionHierarchy = 0;

    public RootParser(List<String> _parseIssues) {
        super(_parseIssues);
    }

    @Override
    public void parse(Node parent, DictCore core) throws PDomException {
        super.parse(parent, core);

        // Version 2.3 implemented class filters for conj rules. Default to all on.
        if (fileVersionHierarchy < PGTUtil.getVersionHierarchy("2.2")) {
            core.getConjugationManager().setAllConjugationRulesToAllClasses();
        }
    }
    
    public List<String> getIssues() {
        return parseIssues;
    }

    @Override
    public void consumeChild(Node node, DictCore core) throws PDomException {
        switch (node.getNodeName()) {
            case PGTUtil.SYS_INFO_XID -> {
                // only saved for purposes of diagnostics: skip
            }
            case PGTUtil.PGVERSION_XID -> {
                String versionNumber = node.getTextContent();
                fileVersionHierarchy = PGTUtil.getVersionHierarchy(versionNumber);

                if (fileVersionHierarchy == -1) {
                    throw new PDomException("Please upgrade PolyGlot. The PGD file you are loading was "
                            + "written with a newer version with additional features: Ver " + versionNumber + ".");
                } else if (fileVersionHierarchy < PGTUtil.getVersionHierarchy("0.7.5")) {
                    throw new PDomException("Version " + versionNumber + " no longer supported. Load/save with older version of "
                            + "PolyGlot (0.7.5 through 1.2) to upconvert.");
                }
            }
            case PGTUtil.DICTIONARY_SAVE_DATE -> {
                core.setLastSaveTime(Instant.parse(node.getTextContent()));
            }
            case PGTUtil.LANG_PROPERTIES_XID -> {
                new LangPropertiesParser(this.parseIssues).parse(node, core);
            }
            case PGTUtil.CLASSES_NODE_XID -> {
                new WordClasseCollectionParser(parseIssues).parse(node, core);
            }
            case PGTUtil.POS_COLLECTION_XID, "" -> {
                new PartOfSpeechCollectionParser(parseIssues).parse(node, core);
            }
            case PGTUtil.LEXICON_XID -> {
                new LexiconParser(parseIssues).parse(node, core);
            }
            case PGTUtil.PRONUNCIATION_COLLECTION_XID, 
                    PGTUtil.PRONUNCIATION_COLLECTION_XID_LEGACY -> {
                new pronunciationParser(parseIssues).parse(node, core);
            }
            case PGTUtil.DECLENSION_COLLECTION_XID -> {
                new DeclensionsCollectionParser(parseIssues).parse(node, core);
            }
            case PGTUtil.DEC_COMBINED_FORM_SECTION_XID -> {
                new CombinedDeclensionCollectionDisabledParser(parseIssues).parse(node, core);
            }
            case PGTUtil.ETY_COLLECTION_XID -> {
                new EtymologyCollectionParser(parseIssues).parse(node, core);
            }
            case PGTUtil.ROM_GUIDE_XID -> {
                new RomanizationGuideParser(parseIssues).parse(node, core);
            }
            case PGTUtil.LOGO_ROOT_NOTE_XID -> {
                new LogoParser(parseIssues).parse(node, core);
            }
            case PGTUtil.GRAMMAR_SECTION_XID -> {
                new GrammarCollectionParser(parseIssues).parse(node, core);
            }
            case PGTUtil.TODO_LOG_XID -> {
                new TodoCollectionParser(parseIssues).parse(node, core);
                core.getToDoManager().fixTodoNodeLoad();
            }
            case PGTUtil.PHRASEBOOK_XID -> {
                new PhraseCollectionParser(parseIssues).parse(node, core);
            }
            case PGTUtil.FAM_NODE_XID -> {
                new FamilyNodeParser(parseIssues, core.getFamManager().getRoot()).parse(node, core);
                core.getFamManager().fixFamilyNodeLoad();
            }
            default ->
                throw new PDomException("Unexpected node in " + this.getClass().getName() + " : " + node.getNodeName());
        }
    }
}
