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

            private StringBuilder stringBuilder = new StringBuilder();
            PronunciationNode proBuffer;
            PronunciationNode romBuffer;
            String charRepCharBuffer = "";
            String charRepValBuffer = "";
            int ruleIdBuffer = 0;
            String ruleValBuffer = "";
            boolean betyIntRelationNode = false;
            boolean betyChildExternals = false;
            boolean bfontlocal = false;
            boolean bgenderId = false;
            boolean bgenderNotes = false;
            boolean bgenderName = false;
            boolean bgender = false;
            boolean bDecCombId = false;
            boolean bwordPlur = false;
            boolean bfamName = false;
            boolean bfamNotes = false;
            boolean bfamWord = false;
            boolean blogoStrokes = false;
            boolean blogoNotes = false;
            boolean blogoRadical = false;
            boolean blogoRadicalList = false;
            boolean blogoReading = false;
            boolean blogoValue = false;
            boolean blogoId = false;
            boolean blogoNode = false;
            boolean blogoWordRelation = false;
            boolean bgrammarChapNode = false;
            boolean bgrammarChapName = false;
            boolean bgrammarSecNode = false;
            boolean bgrammarSecName = false;
            boolean bgrammarSecRecId = false;
            boolean bgrammarSecText = false;
            boolean btoDoNodeDone = false;
            boolean btoDoNodeLabel = false;
            boolean bphraseBook = false;
            boolean bphraseNode = false;
            boolean bphraseid = false;
            boolean bphrasegloss = false;
            boolean bconPhrase = false;
            boolean blocalPhrase = false;
            boolean bphrasePronunciation = false;
            boolean bphrasePronunciationOverride = false;
            boolean bphraseNotes = false;
            boolean bphraseOrder = false;
            
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
                stringBuilder.setLength(0);

                if (qName.equalsIgnoreCase(PGTUtil.CLASS_XID)) {
                    // Make sure we have a clean buffer node
                    core.getWordClassCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUES_NODE_XID)) {
                    // WordClass doesn't have utility method clear() to reset buffer
                    core.getWordClassCollection().getBuffer().buffer = new WordClassValue();
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_XID)) {
                    core.getTypes().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_XID)) {
                    core.getWordCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_INT_RELATION_NODE_XID)) {
                    // This tag has mixed content, which still requires to be processed like this
                    betyIntRelationNode= true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_CHILD_EXTERNALS_XID)) {
                    // This tag has mixed content, which still requires to be processed like this
                    betyChildExternals= true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_XID)) {
                    // from old versions, declensions are loaded as dimensions of a master declension
                    conjugationMgr.getBuffer().clearBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_NODE_XID)) {
                    conjugationMgr.getBuffer().clearBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_XID)) {
                    // ConjugationManager doesn't have a way to clear ruleBuffer
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_TRANS_XID)) {
                    // ConjugationGenRule doesn't have a way to clear transBuffer
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_COMBINED_FORM_XID)) {
                    combinedDecId = "";
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_XID)) {
                    proBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_NODE_XID)) {
                    romBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(PGTUtil.FONT_LOCAL_XID)) {
                    bfontlocal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_COMB_DIM_XID)) {
                    bDecCombId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NAME_XID)) {
                    bfamName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NODE_XID)) {
                    famMan.buildNewBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NOTES_XID)) {
                    bfamNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_WORD_XID)) {
                    bfamWord = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_STROKES_XID)) {
                    blogoStrokes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_NOTES_XID)) {
                    blogoNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_IS_RADICAL_XID)) {
                    blogoRadical = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_RADICAL_LIST_XID)) {
                    blogoRadicalList = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_READING_LIST_XID)) {
                    blogoReading = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGOGRAPH_VALUE_XID)) {
                    blogoValue = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGOGRAPH_ID_XID)) {
                    blogoId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGOGRAPH_NODE_XID)) {
                    blogoNode = true;
                    core.getLogoCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_WORD_RELATION_XID)) {
                    blogoWordRelation = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_CHAPTER_NODE_XID)) {
                    bgrammarChapNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_CHAPTER_NAME_XID)) {
                    bgrammarChapName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_NODE_XID)) {
                    bgrammarSecNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_NAME_XID)) {
                    bgrammarSecName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_RECORDING_XID)) {
                    bgrammarSecRecId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_TEXT_XID)) {
                    bgrammarSecText = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_XID)) {
                     core.getToDoManager().pushBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_LABEL_XID)) {
                     btoDoNodeLabel = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_DONE_XID)) {
                     btoDoNodeDone = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASEBOOK_XID)) {
                    // no subsequent logic required.
                    //bphraseBook = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_NODE_XID)) {
                    // no subsequent logicrequired.
                    //bphraseNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_ID_XID)) {
                    bphraseid = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_CONPHRASE_XID)) {
                    bconPhrase = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_LOCALPHRASE_XID)) {
                    blocalPhrase = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_PRONUNCIATION_XID)) {
                    bphrasePronunciation = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_PRONUNCIATION_OVERRIDE_XID)) {
                    bphrasePronunciationOverride = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_NOTES_XID)) {
                    bphraseNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_GLOSS_XID)) {
                    bphrasegloss = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_ORDER_XID)) {
                    bphraseOrder = true;
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                String value = stringBuilder.toString();

                //region DictCore
                if (qName.equalsIgnoreCase(PGTUtil.DICTIONARY_SAVE_DATE)) {
                    core.setLastSaveTime(Instant.parse(value));
                } 
                //endregion
                //region PropertiesManager
                else if (qName.equalsIgnoreCase(PGTUtil.FONT_CON_XID) &&
                    core.getPropertiesManager().getCachedFont() == null) {
                    try {
                        propMan.setFontCon(value);
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nFont load error: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_FONT_STYLE_XID)) {
                    propMan.setFontStyle(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_FONT_SIZE_XID)) {
                    propMan.setFontSize(Double.valueOf(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_FONT_SIZE_XID)) {
                    propMan.setLocalFontSize(Double.parseDouble(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LANG_NAME_XID)) {
                    propMan.setLangName(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ALPHA_ORDER_XID)) {
                    try {
                        propMan.setAlphaOrder(value);
                    } catch (Exception e) {
                        throw new SAXException("Load error: " + e.getLocalizedMessage(), e);
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_TYPE_MAND_XID)) {
                    propMan.setTypesMandatory(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_MAND_XID)) {
                    propMan.setLocalMandatory(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_UNIQUE_XID)) {
                    propMan.setLocalUniqueness(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_WORD_UNIQUE_XID)) {
                    propMan.setWordUniqueness(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_IGNORE_CASE_XID)) {
                    core.getPropertiesManager().setIgnoreCase(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_DISABLE_PROC_REGEX)) {
                    core.getPropertiesManager().setDisableProcRegex(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ENFORCE_RTL_XID)) {
                    propMan.setEnforceRTL(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_OVERRIDE_REGEX_FONT_XID)) {
                    propMan.setOverrideRegexFont(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_USE_LOCAL_LEX_XID)) {
                    propMan.setUseLocalWordLex(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_AUTH_COPYRIGHT_XID)) {
                    propMan.setCopyrightAuthorInfo(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_NAME_XID)) {
                    propMan.setLocalLangName(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_USE_SIMPLIFIED_CONJ)) {
                    propMan.setUseSimplifiedConjugations(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_EXPANDED_LEX_LIST_DISP)) {
                    propMan.setExpandedLexListDisplay(value.equals(PGTUtil.TRUE));
                } 
                //region PropertiesManager.Zompist
                else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_CATEGORIES)) {
                    propMan.setZompistCategories(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_ILLEGAL_CLUSTERS)) {
                    propMan.setZompistIllegalClusters(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_REWRITE_RULES)) {
                    propMan.setZompistRewriteRules(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_SYLLABLES)) {
                    propMan.setZompistSyllableTypes(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_DROPOFF_RATE)) {
                    propMan.setZompistDropoffRate(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ZOMPIST_MONOSYLLABLE_FREQUENCY)) {
                    propMan.setZompistMonosylableFrequency(Integer.parseInt(value));
                } 
                //endregion
                else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_CHAR_REP_CHAR_XID)) {
                    // can only pull single character, so no need to concatinate
                    charRepCharBuffer = value;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_CHAR_REP_VAL_XID)) {
                    charRepValBuffer = value;
                } 
                //endregion
                //region WordClassColletion.WordClass
                else if (qName.equalsIgnoreCase(PGTUtil.CLASS_XID)) {
                    try {
                        core.getWordClassCollection().insert();
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nWord class load error: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_ID_XID)) {
                    // the buffer should not default to "apply to all."
                    core.getWordClassCollection().getBuffer().deleteApplyType(-1);
                    core.getWordClassCollection().getBuffer().setId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_NAME_XID)) {
                    WordClass buffer = core.getWordClassCollection().getBuffer();
                    buffer.setValue(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_IS_FREETEXT_XID)) {
                    core.getWordClassCollection().getBuffer().setFreeText(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_IS_ASSOCIATIVE_XID)) {
                    core.getWordClassCollection().getBuffer().setAssociative(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_APPLY_TYPES_XID)) {
                    WordClass buffer = core.getWordClassCollection().getBuffer();
                    for (String curType : value.split(",")) {
                        int typeId = Integer.parseInt(curType);
                        buffer.addApplyType(typeId);
                    }
                } 
                //region WordClassColletion.WordClass.WordClassValue
                else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUES_NODE_XID)) {
                    try {
                        core.getWordClassCollection().getBuffer().insert();
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nWord class load error: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUE_ID_XID)) {
                    core.getWordClassCollection().getBuffer().buffer.setId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUE_NAME_XID)) {
                    core.getWordClassCollection().getBuffer().buffer.setValue(value);
                } 
                //endregion
                //endregion
                //region TypeCollection.TypeNode
                else if (qName.equalsIgnoreCase(PGTUtil.POS_XID)) {
                    // insertion for word types is much simpler
                    try {
                        core.getTypes().insert(wCId);
                    } catch (Exception e) {
                        throw new SAXException("Type insertion error: " + e.getLocalizedMessage(), e);
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_ID_XID)) {
                    wCId = Integer.parseInt(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_NAME_XID)) {
                    core.getTypes().getBufferType().setValue(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_NOTES_XID)) {
                    TypeNode node = core.getTypes().getBufferType();
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(value, core));
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nProblem loading part of speech note image: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_DEF_MAN_XID)) {
                    core.getTypes().getBufferType().setDefMandatory(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_PROC_MAN_XID)) {
                    core.getTypes().getBufferType().setProcMandatory(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_PATTERN_XID)) {
                    core.getTypes().getBufferType().setPattern(value, core);
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_GLOSS_XID)) {
                    core.getTypes().getBufferType().setGloss(value);
                } 
                //endregion
                //region ConWordCollection
                else if (qName.equalsIgnoreCase(PGTUtil.WORD_XID)) {
                    try {
                        core.getWordCollection().insert(wId);
                    } catch (Exception e) {
                        throw new SAXException("Word insertion error: " + e.getLocalizedMessage(), e);
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_ID_XID)) {
                    wId = Integer.parseInt(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.LOCALWORD_XID)) {
                    core.getWordCollection().getBufferWord().setLocalWord(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.CONWORD_XID)) {
                    core.getWordCollection().getBufferWord().setValue(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_POS_ID_XID)) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    bufferWord.setWordTypeId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_PROC_XID)) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    try {
                        bufferWord.setPronunciation(value);
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        // Don't bother raising an exception. This is regenerated
                        // each time the word is accessed if the error pops
                        // users will be informed at that more obvious point.
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_DEF_XID)) {
                    // finalize loading of def (if it contains archived HTML elements
                    ConWord curWord = core.getWordCollection().getBufferWord();
                    try {
                        curWord.setDefinition(WebInterface.unarchiveHTML(value, core));
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nWord image load error: " + e.getLocalizedMessage();
                    }
                    
                    curWord.setDefinition(curWord.getDefinition().replaceAll("<br>\\s*[<br>\\s*]+<br>\\s*", ""));
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_PROCOVERRIDE_XID)) {
                    core.getWordCollection().getBufferWord().setProcOverride(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_AUTODECLOVERRIDE_XID)) {
                    core.getWordCollection().getBufferWord().setOverrideAutoConjugate(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_RULEOVERRIDE_XID)) {
                    core.getWordCollection().getBufferWord().setRulesOverride(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_CLASS_AND_VALUE_XID)) {
                    String[] classValIds = value.split(",");
                    int classId = Integer.parseInt(classValIds[0]);
                    int valId = Integer.parseInt(classValIds[1]);
                    core.getWordCollection().getBufferWord().setClassValue(classId, valId);
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_CLASS_TEXT_VAL_XID)){
                    if (ruleIdBuffer == 0) {
                        String[] classValIds = value.split(",");
                        ruleIdBuffer = Integer.parseInt(classValIds[0]);
                        for (int i = 1; i < classValIds.length; i++) {
                            ruleValBuffer += classValIds[i];
                        }
                    } else {
                        ruleValBuffer += value;
                    }
                    core.getWordCollection().getBufferWord().setClassTextValue(ruleIdBuffer, ruleValBuffer);
                    ruleIdBuffer = 0;
                    ruleValBuffer = "";
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_ETY_NOTES_XID)) {
                    core.getWordCollection().getBufferWord().setEtymNotes(value);
                } 
                //endregion
                //region EtymologyManager
                else if (qName.equalsIgnoreCase(PGTUtil.ETY_INT_RELATION_NODE_XID)) {
                    betyIntRelationNode= false;
                    core.getEtymologyManager().setBufferParent(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_INT_CHILD_XID)) {
                    core.getEtymologyManager().setBufferChild(Integer.parseInt(value));
                    core.getEtymologyManager().insert();
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_CHILD_EXTERNALS_XID)) {
                    betyChildExternals= false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_NODE_XID)) {
                    core.getEtymologyManager().insertBufferExtParent();
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_VALUE_XID)) {
                    core.getEtymologyManager().getBufferExtParent().setValue(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_ORIGIN_XID)) {
                    core.getEtymologyManager().getBufferExtParent().setExternalLanguage(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_DEFINITION_XID)) {
                    core.getEtymologyManager().getBufferExtParent().setDefinition(value);
                } 
                //endregion
                //region ConjugationManager.ConjugationNode
                else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_XID)) {
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
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_ID_XID)) {
                    conjugationMgr.setBufferId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_TEXT_XID)) {
                    conjugationMgr.setBufferDecText(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_NOTES_XID)) {
                    try {
                        conjugationMgr.setBufferDecNotes(WebInterface.unarchiveHTML(value, core));
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nProblem loading declension notes image: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_IS_TEMPLATE_XID)) {
                    conjugationMgr.setBufferDecTemp(value.equals("1"));
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_RELATED_ID_XID)) {
                    conjugationMgr.setBufferRelId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_IS_DIMENSIONLESS_XID)) {
                    conjugationMgr.getBuffer().setDimensionless(value.equals(PGTUtil.TRUE));
                } 
                //region ConjugationManager.ConjugationNode.ConjugationDimension
                else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_NODE_XID)) {
                    try {
                        conjugationMgr.getBuffer().insertBuffer();
                        conjugationMgr.getBuffer().clearBuffer();
                    } catch (Exception e) {
                        throw new SAXException(e);
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_ID_XID)) {
                    conjugationMgr.getBuffer().getBuffer().setId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_NAME_XID)) {
                    conjugationMgr.getBuffer().getBuffer().setValue(value);
                } 
                //endregion
                //endregion
                //region ConjugationManager.ConjugationGenRule
                else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_XID)) {
                    core.getConjugationManager().insRuleBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_COMB_XID)) {
                    core.getConjugationManager().getRuleBuffer().setCombinationId(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_NAME_XID)) {
                    core.getConjugationManager().getRuleBuffer().setName(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_REGEX_XID)) {
                    core.getConjugationManager().getRuleBuffer().setRegex(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_TYPE_XID)) {
                    core.getConjugationManager().getRuleBuffer().setTypeId(Integer.parseInt(value));
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_INDEX_XID)) {
                    core.getConjugationManager().getRuleBuffer().setIndex(Integer.parseInt(value));
                } 
                //region ConjugationGenRule.ConjugationGenTransform
                else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_TRANS_XID)) {
                    core.getConjugationManager().getRuleBuffer().insertTransBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_TRANS_REGEX_XID)) {
                    core.getConjugationManager().getRuleBuffer().getTransBuffer().regex = value;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_TRANS_REPLACE_XID)) {
                    core.getConjugationManager().getRuleBuffer().getTransBuffer().replaceText = value;
                } 
                //endregion
                else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_APPLY_TO_CLASS_VALUE_XID)) {
                    String[] classValueIds = value.split(",");
                    core.getConjugationManager().getRuleBuffer().addClassToFilterList(
                            Integer.parseInt(classValueIds[0]),
                            Integer.parseInt(classValueIds[1]));
                } 
                //endregion
                //region ConjugationManager Combined forms settings
                else if (qName.equalsIgnoreCase(PGTUtil.DEC_COMBINED_ID_XID)) {
                    combinedDecId = value;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_COMBINED_SURPRESS_XID)) {
                    core.getConjugationManager().setCombinedConjugationSuppressedRaw(combinedDecId, value.equals(PGTUtil.TRUE));
                } 
                //endregion
                //region PronunciationMgr
                else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_SYLLABLE)) {
                    procMan.addSyllable(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_COMPOSITION_SYLLABLE)) {
                    procMan.setSyllableCompositionEnabled(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_RECURSIVE_XID)) {
                    core.getPronunciationMgr().setRecurse(value.equals(PGTUtil.TRUE));
                } 
                //region PronunciationMgr.PronunciationNode
                else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_XID)) {
                    procMan.addPronunciation(proBuffer);
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_BASE_XID)) {
                    proBuffer.setValue(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_PHON_XID)) {
                    proBuffer.setPronunciation(value);
                } 
                //endregion
                //endregion
                //region RomanizationManager
                else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_ENABLED_XID)) {
                    romanizationMgr.setEnabled(value.equals(PGTUtil.TRUE));
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_RECURSE_XID)) {
                    core.getRomManager().setRecurse(value.equals(PGTUtil.TRUE));
                } 
                //region RomanizationManager.PronunciationNode
                else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_NODE_XID)) {
                    romanizationMgr.addPronunciation(romBuffer);
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_BASE_XID)) {
                    romBuffer.setValue(value);
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_PHON_XID)) {
                    romBuffer.setPronunciation(value);
                } 
                //endregion
                //endregion
                else if (qName.equalsIgnoreCase(PGTUtil.FONT_LOCAL_XID)) {
                    bfontlocal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_COMB_DIM_XID)) {
                    bDecCombId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NAME_XID)) {
                    bfamName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NODE_XID)) {
                    famMan.bufferDone();
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NOTES_XID)) {
                    FamNode node = core.getFamManager().getBuffer();
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(node.getNotes(), core));
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nProblem loading family note image: " + e.getLocalizedMessage();
                    }
                    bfamNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_WORD_XID)) {
                    bfamWord = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_STROKES_XID)) {
                    blogoStrokes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_NOTES_XID)) {
                    LogoNode node = core.getLogoCollection().getBufferNode();
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(node.getNotes(), core));
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nProblem loading logograph note image: " + e.getLocalizedMessage();
                    }
                    blogoNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_IS_RADICAL_XID)) {
                    blogoRadical = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_RADICAL_LIST_XID)) {
                    blogoRadicalList = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_READING_LIST_XID)) {
                    core.getLogoCollection().getBufferNode().insertReadingBuffer();
                    blogoReading = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGOGRAPH_VALUE_XID)) {
                    blogoValue = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGOGRAPH_ID_XID)) {
                    blogoId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGOGRAPH_NODE_XID)) {
                    blogoNode = false;
                    try {
                        core.getLogoCollection().insert();
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nLogograph load error: " + e.getLocalizedMessage();
                    }
                    core.getLogoCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_WORD_RELATION_XID)) {
                    blogoWordRelation = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_CHAPTER_NODE_XID)) {
                    GrammarManager gMan = core.getGrammarManager();
                    gMan.insert();
                    gMan.clear();
                    bgrammarChapNode = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_CHAPTER_NAME_XID)) {
                    bgrammarChapName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_NODE_XID)) {
                    GrammarChapNode gChap = core.getGrammarManager().getBuffer();
                    gChap.insert();
                    gChap.clear();
                    bgrammarSecNode = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_NAME_XID)) {
                    bgrammarSecName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_RECORDING_XID)) {
                    bgrammarSecRecId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.GRAMMAR_SECTION_TEXT_XID)) {
                    bgrammarSecText = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROPCHAR_REP_NODE_XID)) {
                    core.getPropertiesManager().addCharacterReplacement(charRepCharBuffer, charRepValBuffer);
                    charRepCharBuffer = "";
                    charRepValBuffer = "";
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_XID)) {
                     core.getToDoManager().popBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_LABEL_XID)) {
                     btoDoNodeLabel = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_DONE_XID)) {
                     btoDoNodeDone = false;
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
                    bphraseid = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_CONPHRASE_XID)) {
                    bconPhrase = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_LOCALPHRASE_XID)) {
                    blocalPhrase = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_PRONUNCIATION_XID)) {
                    bphrasePronunciation = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_PRONUNCIATION_OVERRIDE_XID)) {
                    bphrasePronunciationOverride = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_NOTES_XID)) {
                    bphraseNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_GLOSS_XID)) {
                    bphrasegloss = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PHRASE_ORDER_XID)) {
                    bphraseOrder = false;
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                stringBuilder.append(ch, start, length);

                if (betyIntRelationNode) {
                    core.getEtymologyManager().setBufferParent(Integer.parseInt(
                            new String(ch, start, length)));
                    betyIntRelationNode = false;
                } else if (betyChildExternals) {
                    core.getEtymologyManager().setBufferChild(Integer.parseInt(
                            new String(ch, start, length)));
                    betyChildExternals = false;
                } else if (bwordPlur) {
                    // plurality now handled as declension
                    conjugationMgr.setBufferDecTemp(false);
                    conjugationMgr.setBufferDecText(new String(ch, start, length));
                    conjugationMgr.setBufferDecNotes("Plural");
                    bwordPlur = false;
                } else if (bgender) {
                    tmpString += new String(ch, start, length);
                } else if (bgenderId) {
                    wGId = Integer.parseInt(new String(ch, start, length));
                    bgenderId = false;
                } else if (bgenderName) {
                    // Deprecated
                } else if (bgenderNotes) {
                    // Deprecated
                } else if (bDecCombId) {
                    conjugationMgr.getBuffer().setCombinedDimId(new String(ch, start, length));
                } else if (bfamName) {
                    FamNode famBuffer = famMan.getBuffer();
                    famBuffer.setValue(famBuffer.getValue()
                            + new String(ch, start, length));
                } else if (bfamNotes) {
                    FamNode node = famMan.getBuffer();
                    node.setNotes(node.getNotes() + new String(ch, start, length));
                } else if (bfamWord) {
                    try {
                        famMan.getBuffer().addWord(core.getWordCollection().getNodeById(
                                Integer.parseInt(new String(ch, start, length))));
                    } catch (NumberFormatException e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nFamily load error: " + e.getLocalizedMessage();
                    }
                    bfamWord = false;
                } else if (blogoStrokes) {
                    try {
                        core.getLogoCollection().getBufferNode().setStrokes(Integer.parseInt(new String(ch, start, length)));
                    } catch (NumberFormatException e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nLogograph load error: " + e.getLocalizedMessage();
                    }
                } else if (blogoNotes) {
                    LogoNode curNode = core.getLogoCollection().getBufferNode();
                    curNode.setNotes(curNode.getNotes() + new String(ch, start, length));
                } else if (blogoRadical) {
                    core.getLogoCollection().getBufferNode().setRadical(
                            new String(ch, start, length).equals(PGTUtil.TRUE));
                } else if (blogoRadicalList) {
                    core.getLogoCollection().getBufferNode().setTmpRadEntries(new String(ch, start, length));
                } else if (blogoReading) {
                    LogoNode curNode = core.getLogoCollection().getBufferNode();
                    curNode.setReadingBuffer(curNode.getReadingBuffer() + new String(ch, start, length));
                } else if (blogoValue) {
                    LogoNode curNode = core.getLogoCollection().getBufferNode();
                    curNode.setValue(curNode.getValue() + new String(ch, start, length));
                } else if (blogoId) {
                    try {
                        core.getLogoCollection().getBufferNode().setId(Integer.parseInt(new String(ch, start, length)));
                    } catch (NumberFormatException e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nLogograph load error: " + e.getLocalizedMessage();
                    }
                } else if (blogoWordRelation) {
                    try {
                        core.getLogoCollection().loadLogoRelations(new String(ch, start, length));
                    } catch (Exception e) {
                        core.getOSHandler().getIOHandler().writeErrorLog(e);
                        warningLog += "\nLogograph relation load error: " + e.getLocalizedMessage();
                    }
                } else if (bgrammarChapName) {
                    GrammarChapNode buffer = core.getGrammarManager().getBuffer();
                    buffer.setName(buffer.getName() + new String(ch, start, length));
                } else if (bgrammarSecName) {
                    GrammarSectionNode buffer = core.getGrammarManager().getBuffer().getBuffer();
                    buffer.setName(buffer.getName() + new String(ch, start, length));
                } else if (bgrammarSecRecId) {
                    core.getGrammarManager().getBuffer().getBuffer()
                            .setRecordingId(Integer.parseInt(new String(ch, start, length)));
                } else if (bgrammarSecText) {
                    GrammarSectionNode buffer = core.getGrammarManager().getBuffer().getBuffer();
                    buffer.setSectionText(buffer.getSectionText() + new String(ch, start, length));
                } else if (btoDoNodeLabel) {
                    ToDoNode node = core.getToDoManager().getBuffer();
                    node.setValue(node.toString() + new String(ch, start, length));
                } else if (btoDoNodeDone) {
                    core.getToDoManager().getBuffer().setDone(new String(ch, start, length).equals(PGTUtil.TRUE));
                } else if (bphraseBook) {
                    // nothing to do: blank book populated in DictCore already
                    bphraseBook = false; // set false here so not to consume action from subnodes
                } else if (bphraseNode) {
                    bphraseNode = false; // set false here so not to consume action from subnodes
                } else if (bphraseid) {
                    int id = Integer.parseInt(new String(ch, start, length));
                    phraseMan.getBuffer().setId(id);
                } else if (bphrasegloss) {
                    PhraseNode buffer = phraseMan.getBuffer();
                    String gloss = buffer.getGloss();
                    gloss += new String(ch, start, length);
                    buffer.setGloss(gloss);
                } else if (bconPhrase) {
                    PhraseNode buffer = phraseMan.getBuffer();
                    String conPhrase = buffer.getConPhrase();
                    conPhrase += new String(ch, start, length);
                    buffer.setConPhrase(conPhrase);
                } else if (blocalPhrase) {
                    PhraseNode buffer = phraseMan.getBuffer();
                    String localPhrase = buffer.getLocalPhrase();
                    localPhrase += new String(ch, start, length);
                    buffer.setLocalPhrase(localPhrase);
                } else if (bphrasePronunciation) {
                    PhraseNode buffer = phraseMan.getBuffer();
                    String proc = buffer.getPronunciation();
                    proc += new String(ch, start, length);
                    buffer.setPronunciation(proc);
                } else if (bphrasePronunciationOverride) {
                    phraseMan.getBuffer().setProcOverride(new String(ch, start, length).equals(PGTUtil.TRUE));
                } else if (bphraseNotes) {
                    PhraseNode buffer = phraseMan.getBuffer();
                    String notes = buffer.getNotes();
                    notes += new String(ch, start, length);
                    buffer.setNotes(notes);
                } else if (bphraseOrder) {
                    int orderId = Integer.parseInt(new String(ch, start, length));
                    phraseMan.getBuffer().setOrderId(orderId);
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
