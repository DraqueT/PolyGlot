/*
 * Copyright (c) 2014-2022, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
 * See LICENSE.TXT included with this code to read the full license agreement.
 *
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
package org.darisadesigns.polyglotlina;

import org.darisadesigns.polyglotlina.Nodes.ConjugationDimension;
import org.darisadesigns.polyglotlina.Nodes.ConjugationGenTransform;
import org.darisadesigns.polyglotlina.Nodes.ConjugationGenRule;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.darisadesigns.polyglotlina.Nodes.LogoNode;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import org.darisadesigns.polyglotlina.Nodes.FamNode;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.ManagersCollections.PropertiesManager;
import org.darisadesigns.polyglotlina.ManagersCollections.GrammarManager;
import org.darisadesigns.polyglotlina.ManagersCollections.PronunciationMgr;
import org.darisadesigns.polyglotlina.ManagersCollections.FamilyManager;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.CustomControls.GrammarSectionNode;
import org.darisadesigns.polyglotlina.CustomControls.GrammarChapNode;
import org.darisadesigns.polyglotlina.ManagersCollections.RomanizationManager;
import org.darisadesigns.polyglotlina.Nodes.EtyExternalParent;
import org.darisadesigns.polyglotlina.Nodes.ToDoNode;
import org.darisadesigns.polyglotlina.Nodes.WordClassValue;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import java.io.InputStream;
import java.time.Instant;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.darisadesigns.polyglotlina.ManagersCollections.PhraseManager;
import org.darisadesigns.polyglotlina.Nodes.PhraseNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class reads PGT files and loads them into memory. Incompatible with
 * files saved earlier than ver 0.75 (there aren't many of those out there,
 * though)
 *
 * @author draque
 */
public final class CustHandlerFactory {

    /**
     * Creates appropriate handler to read file (based on version of PolyGlot
     * file was saved with)
     *
     * @param iStream stream of file to be loaded
     * @param core dictionary core
     * @return an appropriate handler for the xml file
     * @throws java.lang.Exception when unable to read given file or if file is
     * from newer version of PolyGlot
     */
    public static CustHandler getCustHandler(InputStream iStream, DictCore core) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc;

        doc = dBuilder.parse(iStream);
        doc.getDocumentElement().normalize();

        // test for version number in pgd file, set to 0 if none found (pre 0.6)
        Node versionNode = doc.getDocumentElement().getElementsByTagName(PGTUtil.PGVERSION_XID).item(0);
        String versionNumber = versionNode == null ? "0" : versionNode.getTextContent();
        int fileVersionHierarchy = PGTUtil.getVersionHierarchy(versionNumber);
        
        if (fileVersionHierarchy == -1) {
            throw new Exception("Please upgrade PolyGlot. The PGD file you are loading was "
                        + "written with a newer version with additional features: Ver " + versionNumber + ".");
        } else if (fileVersionHierarchy < PGTUtil.getVersionHierarchy("0.7.5")) {
            throw new Exception("Version " + versionNumber + " no longer supported. Load/save with older version of "
                        + "PolyGlot (0.7.5 through 1.2) to upconvert.");
        }

        return get075orHigherHandler(core, fileVersionHierarchy);
    }

    private static CustHandler get075orHigherHandler(final DictCore core, final int versionHierarchy) {
        return new CustHandler() {

            private StringBuilder stringBuilder;
            PronunciationNode proBuffer;
            PronunciationNode romBuffer;
            String charRepCharBuffer = "";
            String charRepValBuffer = "";
            int ruleIdBuffer = 0;
            String ruleValBuffer = "";
            boolean bwordClassTextVal = false;
            boolean bgenderId = false;
            boolean bgenderNotes = false;
            boolean bgenderName = false;
            boolean bgender = false;
            boolean bwordPlur = false;
            boolean bphraseBook = false;
            boolean bphraseNode = false;
            
            int wId;
            int wCId;
            int wGId;
            String combinedDecId = "";
            String tmpString; // placeholder for building serialized values that are guaranteed processed in a single pass

            final ConjugationManager conjugationMgr = core.getConjugationManager();
            final PronunciationMgr procMan = core.getPronunciationMgr();
            final RomanizationManager romanizationMgr = core.getRomManager();
            final PropertiesManager propMan = core.getPropertiesManager();
            final FamilyManager famMan = core.getFamManager();
            final PhraseManager phraseMan = core.getPhraseManager();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                stringBuilder = new StringBuilder();

                if (qName.equalsIgnoreCase(PGTUtil.WORD_XID)) {
                    core.getWordCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_XID)) {
                    proBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_NODE_XID)) {
                    romBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_CLASS_TEXT_VAL_XID)){
                    bwordClassTextVal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_XID)) {
                    // from old versions, declensions are loaded as dimensions of a master declension
                    conjugationMgr.getBuffer().clearBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NODE_XID)) {
                    famMan.buildNewBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGOGRAPH_NODE_XID)) {
                    core.getLogoCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_XID)) {
                    // logic not used
                    //bclassNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_ID_XID)) {
                    // the buffer should not default to "apply to all."
                    core.getWordClassCollection().getBuffer().deleteApplyType(-1);
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_XID)) {
                     core.getToDoManager().pushBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASEBOOK_XID)) {
                    // no subsequent logic required.
                    //bphraseBook = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_NODE_XID)) {
                    // no subsequent logicrequired.
                    //bphraseNode = true;
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                String value = stringBuilder.toString();

                if (qName.equalsIgnoreCase(PGTUtil.DICTIONARY_SAVE_DATE)) {
                    core.setLastSaveTime(Instant.parse(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_XID)) {
                    try {
                        core.getWordCollection().insert(wId);
                    } catch (Exception e) {
                        throw new SAXException("Word insertion error: " + e.getLocalizedMessage(), e);
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_XID)) {
                    // insertion for word types is much simpler
                    try {
                        core.getTypes().insert(wCId);
                    } catch (Exception e) {
                        throw new SAXException("Type insertion error: " + e.getLocalizedMessage(), e);
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_XID)) {
                    procMan.addPronunciation(proBuffer);
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_NODE_XID)) {
                    romanizationMgr.addPronunciation(romBuffer);
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_XID)) {
                    ConjugationNode curBuffer = conjugationMgr.getBuffer();

                    // old bug set IDs to crazy values... this should clean it up.
                    // IDs can never be less than 0, and a max of MAX_VALUE can be stored.
                    // If that's not enough... your language is too damned complex.
                    if (curBuffer.getId() != Integer.MAX_VALUE
                            && curBuffer.getId() > 0) {
                        // dec templates handled differently than actual saved declensions for words
                        if (conjugationMgr.isBufferDecTemp()) {
                            conjugationMgr.insertBuffer();
                        } else {
                            Integer relId = conjugationMgr.getBufferRelId();
                            curBuffer.setCombinedDimId(curBuffer.getCombinedDimId());
                            conjugationMgr.addConjugationToWord(relId, curBuffer.getId(), curBuffer);
                        }
                    }

                    conjugationMgr.clearBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.LOCALWORD_XID)) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    bufferWord.setLocalWord(bufferWord.getLocalWord() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.CONWORD_XID)) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    bufferWord.setValue(bufferWord.getValue() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_POS_ID_XID)) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    bufferWord.setWordTypeId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_ID_XID)) {
                    wId = Integer.parseInt(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_RULEOVERRIDE_XID)) {
                    core.getWordCollection().getBufferWord()
                            .setRulesOverride(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_CLASS_AND_VALUE_XID)) {
                    String[] classValIds = stringBuilder.toString().split(",");
                    int classId = Integer.parseInt(classValIds[0]);
                    int valId = Integer.parseInt(classValIds[1]);
                    core.getWordCollection().getBufferWord().setClassValue(classId, valId);
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_CLASS_TEXT_VAL_XID)){
                    core.getWordCollection().getBufferWord().setClassTextValue(ruleIdBuffer, ruleValBuffer);
                    ruleIdBuffer = 0;
                    ruleValBuffer = "";
                    bwordClassTextVal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_DEF_XID)) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    bufferWord.setDefinition(bufferWord.getDefinition() + value);
                    // finalize loading of def (if it contains archived HTML elements
                    ConWord curWord = core.getWordCollection().getBufferWord();
                    try {
                        curWord.setDefinition(WebInterface.unarchiveHTML(curWord.getDefinition(), core));
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nWord image load error: " + e.getLocalizedMessage();
                    }
                    
                    curWord.setDefinition(curWord.getDefinition().replaceAll("<br>\\s*[<br>\\s*]+<br>\\s*", ""));
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_ETY_NOTES_XID)) {
                    ConWord buffer = core.getWordCollection().getBufferWord();
                    buffer.setEtymNotes(buffer.getEtymNotes() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.FONT_CON_XID) &&
                        core.getPropertiesManager().getCachedFont() == null) {
                    try {
                        propMan.setFontCon(value);
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nFont load error: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_ID_XID)) {
                    wCId = Integer.parseInt(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_NAME_XID)) {
                    TypeNode bufferType = core.getTypes().getBufferType();
                    bufferType.setValue(bufferType.getValue() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_NOTES_XID)) {
                    TypeNode node = core.getTypes().getBufferType();
                    node.setNotes(node.getNotes() + value);
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(node.getNotes(), core));
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nProblem loading part of speech note image: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_PROC_MAN_XID)) {
                    core.getTypes().getBufferType().setProcMandatory(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_DEF_MAN_XID)) {
                    core.getTypes().getBufferType().setDefMandatory(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_PATTERN_XID)) {
                    TypeNode bufferType = core.getTypes().getBufferType();
                    bufferType.setPattern(bufferType.getPattern() + value, core);
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_GLOSS_XID)) {
                    TypeNode bufferType = core.getTypes().getBufferType();
                    bufferType.setGloss(bufferType.getGloss() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_PROC_XID)) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    try {
                        bufferWord.setPronunciation(bufferWord.getPronunciation() + value);
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        // Don't bother raising an exception. This is regenerated
                        // each time the word is accessed if the error pops
                        // users will be informed at that more obvious point.
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_AUTODECLOVERRIDE_XID)) {
                    core.getWordCollection().getBufferWord()
                            .setOverrideAutoConjugate(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LANG_NAME_XID)) {
                    propMan.setLangName(propMan.getLangName() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_OVERRIDE_REGEX_FONT_XID)) {
                    propMan.setOverrideRegexFont(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_FONT_SIZE_XID)) {
                    propMan.setFontSize(Double.parseDouble(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_FONT_STYLE_XID)) {
                    propMan.setFontStyle(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ALPHA_ORDER_XID)) {
                    try {
                        propMan.setAlphaOrder(value);
                    } catch (Exception e) {
                        throw new SAXException("Load error: " + e.getLocalizedMessage(), e);
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ENFORCE_RTL_XID)) {
                    propMan.setEnforceRTL(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_AUTH_COPYRIGHT_XID)) {
                    propMan.setCopyrightAuthorInfo(propMan.getCopyrightAuthorInfo() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_USE_SIMPLIFIED_CONJ)) {
                    propMan.setUseSimplifiedConjugations(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_EXPANDED_LEX_LIST_DISP)) {
                    propMan.setExpandedLexListDisplay(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_NAME_XID)) {
                    propMan.setLocalLangName(propMan.getLocalLangName() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_ID_XID)) {
                    conjugationMgr.setBufferId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_TEXT_XID)) {
                    conjugationMgr.setBufferDecText(conjugationMgr.getBufferDecText() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_NOTES_XID)) {
                    conjugationMgr.setBufferDecNotes(conjugationMgr.getBufferDecNotes() + value);
                    try {
                        conjugationMgr.setBufferDecNotes(WebInterface.unarchiveHTML(conjugationMgr.getBufferDecNotes(), core));
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nProblem loading declension notes image: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_IS_TEMPLATE_XID)) {
                    conjugationMgr.setBufferDecTemp(value.equals("1"));
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_IS_DIMENSIONLESS_XID)) {
                    conjugationMgr.getBuffer().setDimensionless(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_RELATED_ID_XID)) {
                    conjugationMgr.setBufferRelId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_COMB_DIM_XID)) {
                    conjugationMgr.getBuffer().setCombinedDimId(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_BASE_XID)) {
                    proBuffer.setValue(proBuffer.getValue() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_PHON_XID)) {
                    proBuffer.setPronunciation(proBuffer.getPronunciation() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_BASE_XID)) {
                    romBuffer.setValue(romBuffer.getValue() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_ENABLED_XID)) {
                    romanizationMgr.setEnabled(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_PHON_XID)) {
                    romBuffer.setPronunciation(romBuffer.getPronunciation() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_PROCOVERRIDE_XID)) {
                    core.getWordCollection().getBufferWord()
                            .setProcOverride(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_NODE_XID)) {
                    try {
                        conjugationMgr.getBuffer().insertBuffer();
                        conjugationMgr.getBuffer().clearBuffer();
                    } catch (Exception e) {
                        throw new SAXException(e);
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_ID_XID)) {
                    conjugationMgr.getBuffer().getBuffer().setId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_NAME_XID)) {
                    ConjugationDimension dimBuffer = conjugationMgr.getBuffer().getBuffer();
                    dimBuffer.setValue(dimBuffer.getValue() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NAME_XID)) {
                    FamNode famBuffer = famMan.getBuffer();
                    famBuffer.setValue(famBuffer.getValue() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NODE_XID)) {
                    famMan.bufferDone();
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NOTES_XID)) {
                    FamNode node = core.getFamManager().getBuffer();
                    node.setNotes(node.getNotes() + value);
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(node.getNotes(), core));
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nProblem loading family note image: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_WORD_XID)) {
                    try {
                        famMan.getBuffer().addWord(core.getWordCollection().getNodeById(
                                Integer.parseInt(value)));
                    } catch (NumberFormatException e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nFamily load error: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_IGNORE_CASE_XID)) {
                    core.getPropertiesManager().setIgnoreCase(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_DISABLE_PROC_REGEX)) {
                    core.getPropertiesManager().setDisableProcRegex(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_XID)) {
                    core.getConjugationManager().insRuleBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_COMB_XID)) {
                    core.getConjugationManager().getRuleBuffer().setCombinationId(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_NAME_XID)) {
                    ConjugationGenRule ruleBuffer = core.getConjugationManager().getRuleBuffer();
                    ruleBuffer.setName(ruleBuffer.getName() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_REGEX_XID)) {
                    ConjugationGenRule ruleBuffer = core.getConjugationManager().getRuleBuffer();
                    ruleBuffer.setRegex(ruleBuffer.getRegex() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_APPLY_TO_CLASS_VALUE_XID)) {
                    String[] classValueIds = value.split(",");
                    core.getConjugationManager().getRuleBuffer().addClassToFilterList(
                            Integer.parseInt(classValueIds[0]),
                            Integer.parseInt(classValueIds[1]));
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_TYPE_XID)) {
                    core.getConjugationManager().getRuleBuffer().setTypeId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_TRANS_XID)) {
                    core.getConjugationManager().getRuleBuffer().insertTransBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_TRANS_REGEX_XID)) {
                    ConjugationGenTransform transBuffer = core.getConjugationManager().getRuleBuffer().getTransBuffer();
                    transBuffer.regex += value;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_TRANS_REPLACE_XID)) {
                    ConjugationGenTransform transBuffer = core.getConjugationManager().getRuleBuffer().getTransBuffer();
                    transBuffer.replaceText += value;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_INDEX_XID)) {
                    core.getConjugationManager().getRuleBuffer().setIndex(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_COMBINED_ID_XID)) {
                    combinedDecId += value;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_COMBINED_SURPRESS_XID)) {
                    core.getConjugationManager().setCombinedConjugationSuppressedRaw(combinedDecId,
                            value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_COMBINED_FORM_XID)) {
                    combinedDecId = "";
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_STROKES_XID)) {
                    try {
                        core.getLogoCollection().getBufferNode().setStrokes(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nLogograph load error: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_NOTES_XID)) {
                    LogoNode node = core.getLogoCollection().getBufferNode();
                    node.setNotes(node.getNotes() + value);
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(node.getNotes(), core));
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nProblem loading logograph note image: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_IS_RADICAL_XID)) {
                    core.getLogoCollection().getBufferNode().setRadical(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_RADICAL_LIST_XID)) {
                    core.getLogoCollection().getBufferNode().setTmpRadEntries(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_READING_LIST_XID)) {
                    LogoNode curNode = core.getLogoCollection().getBufferNode();
                    curNode.setReadingBuffer(curNode.getReadingBuffer() + value);
                    core.getLogoCollection().getBufferNode().insertReadingBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGOGRAPH_VALUE_XID)) {
                    LogoNode curNode = core.getLogoCollection().getBufferNode();
                    curNode.setValue(curNode.getValue() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGOGRAPH_ID_XID)) {
                    try {
                        core.getLogoCollection().getBufferNode().setId(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nLogograph load error: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGOGRAPH_NODE_XID)) {
                    try {
                        core.getLogoCollection().insert();
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nLogograph load error: " + e.getLocalizedMessage();
                    }
                    core.getLogoCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_WORD_RELATION_XID)) {
                    try {
                        core.getLogoCollection().loadLogoRelations(value);
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nLogograph relation load error: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_CHAPTER_NODE_XID)) {
                    GrammarManager gMan = core.getGrammarManager();
                    gMan.insert();
                    gMan.clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_CHAPTER_NAME_XID)) {
                    GrammarChapNode buffer = core.getGrammarManager().getBuffer();
                    buffer.setName(buffer.getName() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_NODE_XID)) {
                    GrammarChapNode gChap = core.getGrammarManager().getBuffer();
                    gChap.insert();
                    gChap.clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_NAME_XID)) {
                    GrammarSectionNode buffer = core.getGrammarManager().getBuffer().getBuffer();
                    buffer.setName(buffer.getName() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_RECORDING_XID)) {
                    core.getGrammarManager().getBuffer().getBuffer()
                            .setRecordingId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_TEXT_XID)) {
                    GrammarSectionNode buffer = core.getGrammarManager().getBuffer().getBuffer();
                    buffer.setSectionText(buffer.getSectionText() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_XID)) {
                    try {
                        core.getWordClassCollection().insert();
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nWord class load error: " + e.getLocalizedMessage();
                    }
                } else if(qName.equalsIgnoreCase(PGTUtil.CLASS_XID)) {
                    // logic not used
                    //bclassNode = false;
                }  else if (qName.equalsIgnoreCase(PGTUtil.CLASS_ID_XID)) {
                    core.getWordClassCollection().getBuffer().setId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_NAME_XID)) {
                    WordClass buffer = core.getWordClassCollection().getBuffer();
                    buffer.setValue(buffer.getValue() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_APPLY_TYPES_XID)) {
                    WordClass buffer = core.getWordClassCollection().getBuffer();
                    for (String curType : value.split(",")) {
                        int typeId = Integer.parseInt(curType);
                        buffer.addApplyType(typeId);
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_IS_FREETEXT_XID)) {
                    core.getWordClassCollection().getBuffer().setFreeText(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_IS_ASSOCIATIVE_XID)) {
                    core.getWordClassCollection().getBuffer().setAssociative(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUES_NODE_XID)) {
                    try {
                        core.getWordClassCollection().getBuffer().insert();
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nWord class load error: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUE_ID_XID)) {
                    core.getWordClassCollection().getBuffer().buffer.setId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUE_NAME_XID)) {
                    WordClassValue classValue = core.getWordClassCollection().getBuffer().buffer;
                    classValue.setValue(classValue.getValue() + value);
                }  else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROPCHAR_REP_NODE_XID)) {
                    core.getPropertiesManager().addCharacterReplacement(charRepCharBuffer, charRepValBuffer);
                    charRepCharBuffer = "";
                    charRepValBuffer = "";
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_UNIQUE_XID)) {
                    propMan.setLocalUniqueness(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_USE_LOCAL_LEX_XID)) {
                    propMan.setUseLocalWordLex(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_CHAR_REP_CHAR_XID)) {
                    // can only pull single character, so no need to concatenate
                    charRepCharBuffer = value;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_CHAR_REP_VAL_XID)) {
                    charRepValBuffer += value;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_FONT_SIZE_XID)) {
                    propMan.setLocalFontSize(Double.parseDouble(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_WORD_UNIQUE_XID)) {
                    propMan.setWordUniqueness(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_MAND_XID)) {
                    propMan.setLocalMandatory(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_TYPE_MAND_XID)) {
                    propMan.setTypesMandatory(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_RECURSIVE_XID)) {
                    core.getPronunciationMgr().setRecurse(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_RECURSE_XID)) {
                    core.getRomManager().setRecurse(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_INT_RELATION_NODE_XID)) {
                    core.getEtymologyManager().setBufferParent(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_INT_CHILD_XID)) {
                    core.getEtymologyManager().setBufferChild(Integer.parseInt(value));
                     core.getEtymologyManager().insert();
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_CHILD_EXTERNALS_XID)) {
                    core.getEtymologyManager().setBufferChild(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_NODE_XID)) {
                     core.getEtymologyManager().insertBufferExtParent();
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_VALUE_XID)) {
                    EtyExternalParent ext = core.getEtymologyManager().getBufferExtParent();
                    ext.setValue(ext.getValue() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_ORIGIN_XID)) {
                    EtyExternalParent ext = core.getEtymologyManager().getBufferExtParent();
                    ext.setExternalLanguage(ext.getExternalLanguage() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_DEFINITION_XID)) {
                    EtyExternalParent ext = core.getEtymologyManager().getBufferExtParent();
                    ext.setDefinition(ext.getDefinition() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_XID)) {
                     core.getToDoManager().popBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_LABEL_XID)) {
                    ToDoNode node = core.getToDoManager().getBuffer();
                    node.setValue(node.toString() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_DONE_XID)) {
                    core.getToDoManager().getBuffer().setDone(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASEBOOK_XID)) {
                    bphraseBook = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_NODE_XID)) {
                    PhraseNode buffer = phraseMan.getBuffer();
                    try {
                        phraseMan.insert(buffer.getId(), buffer);
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nPhrase load error: " + e.getLocalizedMessage();
                    }
                    phraseMan.clear();
                    bphraseNode = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_ID_XID)) {
                    phraseMan.getBuffer().setId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_CONPHRASE_XID)) {
                    PhraseNode buffer = phraseMan.getBuffer();
                    buffer.setConPhrase(buffer.getConPhrase() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_LOCALPHRASE_XID)) {
                    PhraseNode buffer = phraseMan.getBuffer();
                    buffer.setLocalPhrase(buffer.getLocalPhrase() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_PRONUNCIATION_XID)) {
                    PhraseNode buffer = phraseMan.getBuffer();
                    buffer.setPronunciation(buffer.getPronunciation() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_PRONUNCIATION_OVERRIDE_XID)) {
                    phraseMan.getBuffer().setProcOverride(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_NOTES_XID)) {
                    PhraseNode buffer = phraseMan.getBuffer();
                    buffer.setNotes(buffer.getNotes() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_GLOSS_XID)) {
                    PhraseNode buffer = phraseMan.getBuffer();
                    buffer.setGloss(buffer.getGloss() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_ORDER_XID)) {
                    phraseMan.getBuffer().setOrderId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_CATEGORIES)) {
                    propMan.setZompistCategories(propMan.getZompistCategories() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_ILLEGAL_CLUSTERS)) {
                    propMan.setZompistIllegalClusters(propMan.getZompistIllegalClusters() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_REWRITE_RULES)) {
                    propMan.setZompistRewriteRules(propMan.getZompistRewriteRules() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_SYLLABLES)) {
                    propMan.setZompistSyllableTypes(propMan.getZompistSyllableTypes() + value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_DROPOFF_RATE)) {
                    propMan.setZompistDropoffRate(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_MONOSYLLABLE_FREQUENCY)) {
                    propMan.setZompistMonosylableFrequency(Integer.parseInt(value));
                }  else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_SYLLABLE)) {
                    procMan.addSyllable(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_COMPOSITION_SYLLABLE)) {
                    procMan.setSyllableCompositionEnabled(value.equals(PGTUtil.TRUE));
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {

                stringBuilder.append(ch, start, length);

                if (bwordPlur) {
                    // plurality now handled as declension
                    conjugationMgr.setBufferDecTemp(false);
                    conjugationMgr.setBufferDecText(new String(ch, start, length));
                    conjugationMgr.setBufferDecNotes("Plural");
                    bwordPlur = false;
                } else if (bwordClassTextVal) {
                    if (ruleIdBuffer == 0) {
                        String[] classValIds = new String(ch, start, length).split(",");
                        ruleIdBuffer = Integer.parseInt(classValIds[0]);
                        for (int i = 1; i < classValIds.length; i++) {
                            ruleValBuffer += classValIds[i];
                        }
                    } else {
                        ruleValBuffer += new String(ch, start, length);
                    }
                } else if (bgender) {
                    tmpString += new String(ch, start, length);
                } else if (bgenderId) {
                    wGId = Integer.parseInt(new String(ch, start, length));
                    bgenderId = false;
                } else if (bgenderName) {
                    // Deprecated
                } else if (bgenderNotes) {
                    // Deprecated
                } else if (bphraseBook) {
                    // nothing to do: blank book populated in DictCore already
                    bphraseBook = false; // set false here so not to consume action from subnodes
                } else if (bphraseNode) {
                    bphraseNode = false; // set false here so not to consume action from subnodes
                }
            }
            
            @Override
            public void endDocument() {
                // Version 2.3 implemented class filters for conj rules. Default to all on.
                if (versionHierarchy < PGTUtil.getVersionHierarchy("2.2")) {
                    core.getConjugationManager().setAllConjugationRulesToAllClasses();
                }
            }
        };
    }

    private CustHandlerFactory() {}
}
