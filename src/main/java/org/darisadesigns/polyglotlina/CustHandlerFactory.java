/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
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

import org.darisadesigns.polyglotlina.Nodes.DeclensionDimension;
import org.darisadesigns.polyglotlina.Nodes.DeclensionGenTransform;
import org.darisadesigns.polyglotlina.Nodes.DeclensionGenRule;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.darisadesigns.polyglotlina.Nodes.LogoNode;
import org.darisadesigns.polyglotlina.Nodes.DeclensionNode;
import org.darisadesigns.polyglotlina.Nodes.FamNode;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.ManagersCollections.PropertiesManager;
import org.darisadesigns.polyglotlina.ManagersCollections.GrammarManager;
import org.darisadesigns.polyglotlina.ManagersCollections.PronunciationMgr;
import org.darisadesigns.polyglotlina.ManagersCollections.FamilyManager;
import org.darisadesigns.polyglotlina.ManagersCollections.DeclensionManager;
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
            throw new Exception("Version " + versionNumber + " no longer supported. Load/save with older version of"
                        + "PolyGlot (0.7.5 through 1.2) to upconvert.");
        }

        return get075orHigherHandler(core, fileVersionHierarchy);
    }

    private static CustHandler get075orHigherHandler(final DictCore core, final int versionHierarchy) {
        return new CustHandler() {

            PronunciationNode proBuffer;
            PronunciationNode romBuffer;
            String charRepCharBuffer = "";
            String charRepValBuffer = "";
            int ruleIdBuffer = 0;
            String ruleValBuffer = "";
            boolean blastSave = false;
            boolean blocalWord = false;
            boolean bconWord = false;
            boolean btypeId = false;
            boolean bId = false;
            boolean bdef = false;
            boolean bfontcon = false;
            boolean bfontlocal = false;
            boolean bwordClassName = false;
            boolean bwordClassTextVal = false;
            boolean bwordClassId = false;
            boolean bwordClassNotes = false;
            boolean bwordClassGloss = false;
            boolean bwordEtymNotes = false;
            boolean bpronuncation = false;
            boolean bclassVal = false;
            boolean bgenderId = false;
            boolean bgenderNotes = false;
            boolean bgenderName = false;
            boolean bgender = false;
            boolean blangName = false;
            boolean blangRegexFontOvr = false;
            boolean bfontSize = false;
            boolean bfontStyle = false;
            boolean bfontLocalSize = false;
            boolean balphaOrder = false;
            boolean bDecId = false;
            boolean bDecText = false;
            boolean bDecNotes = false;
            boolean bDecIsTemp = false;
            boolean bDecIsDimless = false;
            boolean bDecRelId = false;
            boolean bDecCombId = false;
            boolean bpronBase = false;
            boolean bpronPhon = false;
            boolean bromBase = false;
            boolean bromActive = false;
            boolean bromPhon = false;
            boolean bwordPlur = false;
            boolean blangPropTypeMandatory = false;
            boolean blangPropLocalMandatory = false;
            boolean blangPropWordUniqueness = false;
            boolean blangPropLocalUniqueness = false;
            boolean blangPropUseLocalLex = false;
            boolean blangPropEnforceRTL = false;
            boolean blangPropLocalLangName = false;
            boolean blangPropAuthCopyright = false;
            boolean blangPropSimpConj = false;
            boolean bwordClassDefMan = false;
            boolean bwordClassProcMan = false;
            boolean bwordClassPattern = false;
            boolean bwordProcOverride = false;
            boolean bdimNode = false;
            boolean bdimId = false;
            boolean bdimName = false;
            boolean bfamName = false;
            boolean bfamNotes = false;
            boolean bfamWord = false;
            boolean bignoreCase = false;
            boolean bdisableProcRegex = false;
            boolean bwordoverAutoDec = false;
            boolean bdecGenRuleComb = false;
            boolean bdecGenRuleName = false;
            boolean bdecGenRuleRegex = false;
            boolean bdecGenRuleType = false;
            boolean bdecGenRuleIndex = false;
            boolean bdecGenTransRegex = false;
            boolean bdecGenTransRep = false;
            boolean bdecGenTransClassVal = false;
            boolean bcombinedFormId = false;
            boolean bcombinedFormSurpress = false;
            boolean bwordRuleOverride = false;
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
            boolean bclassNode = false;
            boolean bclassId = false;
            boolean bclassName = false;
            boolean bclassApplyTypes = false;
            boolean bclassFreeText = false;
            boolean bclassValueNode = false;
            boolean bclassValueId = false;
            boolean bclassValueName = false;
            boolean bcharRepChar = false;
            boolean bcharRepValue = false;
            boolean bKerningValue = false;
            boolean bromRecurse = false;
            boolean bprocRecurse = false;
            boolean betyIntRelationNode = false;
            boolean betyIntChild = false;
            boolean betyChildExternals = false;
            boolean betyExternalWordNode = false;
            boolean betyExternalWordValue = false;
            boolean betyExternalWordOrigin = false;
            boolean betyExternalWordDefinition = false;
            boolean btoDoNodeDone = false;
            boolean btoDoNodeLabel = false;
            
            int wId;
            int wCId;
            int wGId;
            String combinedDecId = "";
            String tmpString; // used mostly for converting deprecated values (as they no longer have placeholder points)

            final DeclensionManager declensionMgr = core.getDeclensionManager();
            final PronunciationMgr pronuncMgr = core.getPronunciationMgr();
            final RomanizationManager romanizationMgr = core.getRomManager();
            final PropertiesManager propertiesManager = core.getPropertiesManager();
            final FamilyManager famMgr = core.getFamManager();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if (qName.equalsIgnoreCase(PGTUtil.DICTIONARY_SAVE_DATE)) {
                    blastSave = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_XID)) {
                    core.getWordCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_XID)) {
                    proBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_NODE_XID)) {
                    romBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(PGTUtil.LOCALWORD_XID)) {
                    blocalWord = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.CONWORD_XID)) {
                    bconWord = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_POS_ID_XID)) {
                    btypeId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_ID_XID)) {
                    bId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_DEF_XID)) {
                    bdef = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_ETY_NOTES_XID)) {
                    bwordEtymNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_RULEORVERRIDE_XID)) {
                    bwordRuleOverride = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_CLASS_AND_VALUE_XID)) {
                    bclassVal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_CLASS_TEXT_VAL_XID)){
                    bwordClassTextVal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.FONT_CON_XID)) {
                    bfontcon = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.FONT_LOCAL_XID)) {
                    bfontlocal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_ID_XID)) {
                    bwordClassId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_NAME_XID)) {
                    bwordClassName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_NOTES_XID)) {
                    bwordClassNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_GLOSS_XID)) {
                    bwordClassGloss = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_PROC_XID)) {
                    bpronuncation = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LANG_NAME_XID)) {
                    blangName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_OVERRIDE_REGEX_FONT_XID)) {
                    blangRegexFontOvr = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_FONT_SIZE_XID)) {
                    bfontSize = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_FONT_SIZE_XID)) {
                    bfontLocalSize = true;
                }
                else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_FONT_STYLE_XID)) {
                    bfontStyle = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ALPHA_ORDER_XID)) {
                    balphaOrder = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ENFORCE_RTL_XID)) {
                    blangPropEnforceRTL = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_AUTH_COPYRIGHT_XID)) {
                    blangPropAuthCopyright = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_USE_SIMPLIFIED_CONJ)) {
                    blangPropSimpConj = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_NAME_XID)) {
                    blangPropLocalLangName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_AUTODECLOVERRIDE_XID)) {
                    bwordoverAutoDec = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_XID)) {
                    // from old versions, declensions are loaded as dimensions of a master declension
                    declensionMgr.getBuffer().clearBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_ID_XID)) {
                    bDecId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_TEXT_XID)) {
                    bDecText = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_NOTES_XID)) {
                    bDecNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_IS_TEMPLATE_XID)) {
                    bDecIsTemp = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_IS_DIMENSIONLESS_XID)) {
                    bDecIsDimless = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_RELATED_ID_XID)) {
                    bDecRelId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_BASE_XID)) {
                    bpronBase = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_PHON_XID)) {
                    bpronPhon = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_BASE_XID)) {
                    bromBase = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_ENABLED_XID)) {
                    bromActive = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_PHON_XID)) {
                    bromPhon = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_PROCOVERRIDE_XID)) {
                    bwordProcOverride = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_PROC_MAN_XID)) {
                    bwordClassProcMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_DEF_MAN_XID)) {
                    bwordClassDefMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_PATTERN_XID)) {
                    bwordClassPattern = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_UNIQUE_XID)) {
                    blangPropLocalUniqueness = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_USE_LOCAL_LEX_XID)) {
                    blangPropUseLocalLex = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_WORD_UNIQUE_XID)) {
                    blangPropWordUniqueness = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_MAND_XID)) {
                    blangPropLocalMandatory = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_TYPE_MAND_XID)) {
                    blangPropTypeMandatory = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_NODE_XID)) {
                    bdimNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_ID_XID)) {
                    bdimId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_NAME_XID)) {
                    bdimName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_COMB_DIM_XID)) {
                    bDecCombId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NAME_XID)) {
                    bfamName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NODE_XID)) {
                    famMgr.buildNewBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NOTES_XID)) {
                    bfamNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_WORD_XID)) {
                    bfamWord = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_IGNORE_CASE_XID)) {
                    bignoreCase = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_DISABLE_PROC_REGEX)) {
                    bdisableProcRegex = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_COMB_XID)) {
                    bdecGenRuleComb = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_NAME_XID)) {
                    bdecGenRuleName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_REGEX_XID)) {
                    bdecGenRuleRegex = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_TYPE_XID)) {
                    bdecGenRuleType = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_TRANS_REGEX_XID)) {
                    bdecGenTransRegex = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_TRANS_REPLACE_XID)) {
                    bdecGenTransRep = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_APPLY_TO_CLASS_VALUE_XID)) {
                    bdecGenTransClassVal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_INDEX_XID)) {
                    bdecGenRuleIndex = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_COMBINED_ID_XID)) {
                    bcombinedFormId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_COMBINED_SURPRESS_XID)) {
                    bcombinedFormSurpress = true;
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
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_XID)) {
                    bclassNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_ID_XID)) {
                    bclassId = true;
                    // the buffer should not default to "apply to all."
                    core.getWordClassCollection().getBuffer().deleteApplyType(-1);
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_NAME_XID)) {
                    bclassName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_APPLY_TYPES_XID)) {
                    bclassApplyTypes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_IS_FREETEXT_XID)) {
                    bclassFreeText = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUES_NODE_XID)) {
                    bclassValueNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUE_ID_XID)) {
                    bclassValueId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUE_NAME_XID)) {
                    bclassValueName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_CHAR_REP_CHAR_XID)) {
                    bcharRepChar = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_CHAR_REP_VAL_XID)) {
                    bcharRepValue = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_KERN_VAL_XID)) {
                    bKerningValue = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_RECURSIVE_XID)) {
                    bprocRecurse = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_RECURSE_XID)) {
                    bromRecurse = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_INT_RELATION_NODE_XID)) {
                     betyIntRelationNode= true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_INT_CHILD_XID)) {
                     betyIntChild= true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_CHILD_EXTERNALS_XID)) {
                     betyChildExternals= true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_NODE_XID)) {
                     betyExternalWordNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_VALUE_XID)) {
                     betyExternalWordValue = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_ORIGIN_XID)) {
                     betyExternalWordOrigin = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_DEFINITION_XID)) {
                     betyExternalWordDefinition = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_XID)) {
                     core.getToDoManager().pushBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_LABEL_XID)) {
                     btoDoNodeLabel = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_DONE_XID)) {
                     btoDoNodeDone = true;
                }
            }

            @Override
            public void endElement(String uri, String localName,
                    String qName) throws SAXException {

                if (qName.equalsIgnoreCase(PGTUtil.DICTIONARY_SAVE_DATE)) {
                    blastSave = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_XID)) {
                    ConWord curWord = core.getWordCollection()
                            .getBufferWord();

                    try {
                        // if word is valid, save. Throw error otherwise
                        if (curWord.checkValid()) {
                            core.getWordCollection().insert(wId);
                        } else {
                            throw new Exception("Word ("
                                    + curWord.getLocalWord() + " : "
                                    + curWord.getValue()
                                    + ") is a malformed entry.");
                        }
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
                    pronuncMgr.addPronunciation(proBuffer);
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_NODE_XID)) {
                    romanizationMgr.addPronunciation(romBuffer);
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_XID)) {
                    DeclensionNode curBuffer = declensionMgr.getBuffer();

                    // old bug set IDs to crazy values... this should clean it up.
                    // IDs can never be less than 0, and a max of MAX_VALUE can be stored.
                    // If that's not enough... your language is too damned complex.
                    if (curBuffer.getId() != Integer.MAX_VALUE
                            && curBuffer.getId() > 0) {
                        // dec templates handled differently than actual saved declensions for words
                        if (declensionMgr.isBufferDecTemp()) {
                            declensionMgr.insertBuffer();
                        } else {
                            Integer relId = declensionMgr.getBufferRelId();
                            curBuffer.setCombinedDimId(curBuffer.getCombinedDimId());
                            declensionMgr.addDeclensionToWord(relId, curBuffer.getId(), curBuffer);
                        }
                    }

                    declensionMgr.clearBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.LOCALWORD_XID)) {
                    blocalWord = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.CONWORD_XID)) {
                    bconWord = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_POS_ID_XID)) {
                    btypeId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_ID_XID)) {
                    bId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_RULEORVERRIDE_XID)) {
                    bwordRuleOverride = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_CLASS_AND_VALUE_XID)) {
                    bclassVal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_CLASS_TEXT_VAL_XID)){
                    core.getWordCollection().getBufferWord().setClassTextValue(ruleIdBuffer, ruleValBuffer);
                    ruleIdBuffer = 0;
                    ruleValBuffer = "";
                    bwordClassTextVal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_DEF_XID)) {
                    // finalize loading of def (if it contains archived HTML elements
                    ConWord curWord = core.getWordCollection().getBufferWord();
                    try {
                        curWord.setDefinition(WebInterface.unarchiveHTML(curWord.getDefinition(), core));
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        warningLog += "\nWord image load error: " + e.getLocalizedMessage();
                    }
                    
                    curWord.setDefinition(curWord.getDefinition().replaceAll("<br>\\s*[<br>\\s*]+<br>\\s*", ""));
                    bdef = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_ETY_NOTES_XID)) {
                    bwordEtymNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.FONT_CON_XID)) {
                    bfontcon = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.FONT_LOCAL_XID)) {
                    bfontlocal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_NAME_XID)) {
                    bwordClassName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_NOTES_XID)) {
                    TypeNode node = core.getTypes().getBufferType();
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(node.getNotes(), core));
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        warningLog += "\nProblem loading part of speech note image: " + e.getLocalizedMessage();
                    }
                    bwordClassNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_PROC_MAN_XID)) {
                    bwordClassProcMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_DEF_MAN_XID)) {
                    bwordClassDefMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_PATTERN_XID)) {
                    bwordClassPattern = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.POS_GLOSS_XID)) {
                    bwordClassGloss = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_PROC_XID)) {
                    bpronuncation = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.WORD_AUTODECLOVERRIDE_XID)) {
                    bwordoverAutoDec = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LANG_NAME_XID)) {
                    blangName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_OVERRIDE_REGEX_FONT_XID)) {
                    blangRegexFontOvr = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_FONT_SIZE_XID)) {
                    bfontSize = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_FONT_STYLE_XID)) {
                    bfontStyle = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ALPHA_ORDER_XID)) {
                    balphaOrder = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_ENFORCE_RTL_XID)) {
                    blangPropEnforceRTL = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_AUTH_COPYRIGHT_XID)) {
                    blangPropAuthCopyright = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_USE_SIMPLIFIED_CONJ)) {
                    blangPropSimpConj = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_NAME_XID)) {
                    blangPropLocalLangName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_ID_XID)) {
                    bDecId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_TEXT_XID)) {
                    bDecText = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_NOTES_XID)) {
                    try {
                        declensionMgr.setBufferDecNotes(WebInterface.unarchiveHTML(declensionMgr.getBufferDecNotes(), core));
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        warningLog += "\nProblem loading declension notes image: " + e.getLocalizedMessage();
                    }
                    bDecNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_IS_TEMPLATE_XID)) {
                    bDecIsTemp = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_IS_DIMENSIONLESS_XID)) {
                    bDecIsDimless = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_RELATED_ID_XID)) {
                    bDecRelId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DECLENSION_COMB_DIM_XID)) {
                    bDecCombId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_BASE_XID)) {
                    bpronBase = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_PHON_XID)) {
                    bpronPhon = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_BASE_XID)) {
                    bromBase = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_ENABLED_XID)) {
                    bromActive = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_PHON_XID)) {
                    bromPhon = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_NODE_XID)) {
                    try {
                        declensionMgr.getBuffer().insertBuffer();
                        declensionMgr.getBuffer().clearBuffer();
                    } catch (Exception e) {
                        throw new SAXException(e);
                    }
                    bdimNode = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_ID_XID)) {
                    bdimId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DIMENSION_NAME_XID)) {
                    bdimName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NAME_XID)) {
                    bfamName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NODE_XID)) {
                    famMgr.bufferDone();
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_NOTES_XID)) {
                    FamNode node = core.getFamManager().getBuffer();
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(node.getNotes(), core));
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        warningLog += "\nProblem loading family note image: " + e.getLocalizedMessage();
                    }
                    bfamNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.FAM_WORD_XID)) {
                    bfamWord = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_IGNORE_CASE_XID)) {
                    bignoreCase = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_DISABLE_PROC_REGEX)) {
                    bdisableProcRegex = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_XID)) {
                    core.getDeclensionManager().insRuleBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_COMB_XID)) {
                    bdecGenRuleComb = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_NAME_XID)) {
                    bdecGenRuleName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_REGEX_XID)) {
                    bdecGenRuleRegex = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_APPLY_TO_CLASS_VALUE_XID)) {
                    bdecGenTransClassVal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_TYPE_XID)) {
                    bdecGenRuleType = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEG_GEN_TRANS_XID)) {
                    core.getDeclensionManager().getRuleBuffer().insertTransBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_TRANS_REGEX_XID)) {
                    bdecGenTransRegex = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_TRANS_REPLACE_XID)) {
                    bdecGenTransRep = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_GEN_RULE_INDEX_XID)) {
                    bdecGenRuleIndex = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_COMBINED_ID_XID)) {
                    bcombinedFormId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_COMBINED_SURPRESS_XID)) {
                    bcombinedFormSurpress = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.DEC_COMBINED_FORM_XID)) {
                    combinedDecId = "";
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_STROKES_XID)) {
                    blogoStrokes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LOGO_NOTES_XID)) {
                    LogoNode node = core.getLogoCollection().getBufferNode();
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(node.getNotes(), core));
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
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
                        IOHandler.writeErrorLog(e);
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
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_XID)) {
                    try {
                        core.getWordClassCollection().insert();
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        warningLog += "\nWord class load error: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_ID_XID)) {
                    bclassId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_NAME_XID)) {
                    bclassName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_APPLY_TYPES_XID)) {
                    bclassApplyTypes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_IS_FREETEXT_XID)) {
                    bclassFreeText = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUES_NODE_XID)) {
                    try {
                        core.getWordClassCollection().getBuffer().insert();
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        warningLog += "\nWord class load error: " + e.getLocalizedMessage();
                    }
                    bclassValueNode = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUE_ID_XID)) {
                    bclassValueId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.CLASS_VALUE_NAME_XID)) {
                    bclassValueName = false;
                }  else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROPCHAR_REP_NODE_XID)) {
                    core.getPropertiesManager().addCharacterReplacement(charRepCharBuffer, charRepValBuffer);
                    charRepCharBuffer = "";
                    charRepValBuffer = "";
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_USE_LOCAL_LEX_XID)) {
                    blangPropUseLocalLex = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_CHAR_REP_CHAR_XID)) {
                    bcharRepChar = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_CHAR_REP_VAL_XID)) {
                    bcharRepValue = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_KERN_VAL_XID)) {
                    bKerningValue = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.LANG_PROP_LOCAL_FONT_SIZE_XID)) {
                    bfontLocalSize = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.PRO_GUIDE_RECURSIVE_XID)) {
                    bprocRecurse = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ROM_GUIDE_RECURSE_XID)) {
                    bromRecurse = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_INT_RELATION_NODE_XID)) {
                     betyIntRelationNode= false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_INT_CHILD_XID)) {
                     betyIntChild= false;
                     core.getEtymologyManager().insert();
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_CHILD_EXTERNALS_XID)) {
                     betyChildExternals= false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_NODE_XID)) {
                     betyExternalWordNode = false;
                     core.getEtymologyManager().insertBufferExtParent();
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_VALUE_XID)) {
                     betyExternalWordValue = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_ORIGIN_XID)) {
                     betyExternalWordOrigin = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ETY_EXTERNAL_WORD_DEFINITION_XID)) {
                     betyExternalWordDefinition = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_XID)) {
                     core.getToDoManager().popBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_LABEL_XID)) {
                     btoDoNodeLabel = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.TODO_NODE_DONE_XID)) {
                     btoDoNodeDone = false;
                }
            }

            @Override
            public void characters(char[] ch, int start, int length)
                    throws SAXException {

                if (blastSave) {
                    core.setLastSaveTime(Instant.parse(new String(ch, start, length)));
                } else if (blocalWord) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    bufferWord.setLocalWord(bufferWord.getLocalWord()
                            + new String(ch, start, length));
                } else if (bconWord) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    bufferWord.setValue(bufferWord.getValue()
                            + new String(ch, start, length));
                } else if (btypeId) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    bufferWord.setWordTypeId(Integer.parseInt(new String(ch, start, length)));
                } else if (bId) {
                    wId = Integer.parseInt(new String(ch, start, length));
                } else if (bdef) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    bufferWord.setDefinition(bufferWord.getDefinition()
                            + new String(ch, start, length));
                } else if (bwordPlur) {
                    // plurality now handled as declension
                    declensionMgr.setBufferDecTemp(false);
                    declensionMgr.setBufferDecText(new String(ch, start, length));
                    declensionMgr.setBufferDecNotes("Plural");
                    bwordPlur = false;
                } else if (bwordProcOverride) {
                    core.getWordCollection().getBufferWord()
                            .setProcOverride(new String(ch, start, length).equals(PGTUtil.TRUE));
                    bwordProcOverride = false;
                } else if (bwordRuleOverride) {
                    core.getWordCollection().getBufferWord()
                            .setRulesOverride(new String(ch, start, length).equals(PGTUtil.TRUE));
                    bwordRuleOverride = false;
                } else if (bclassVal) {
                    String[] classValIds = new String(ch, start, length).split(",");
                    int classId = Integer.parseInt(classValIds[0]);
                    int valId = Integer.parseInt(classValIds[1]);
                    core.getWordCollection().getBufferWord().setClassValue(classId, valId);
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
                } else if (bwordEtymNotes) {
                    ConWord buffer = core.getWordCollection().getBufferWord();
                    buffer.setEtymNotes(buffer.getEtymNotes() + new String(ch, start, length));
                }else if (bwordoverAutoDec) {
                    core.getWordCollection().getBufferWord()
                            .setOverrideAutoDeclen(new String(ch, start, length).equals(PGTUtil.TRUE));
                    bwordoverAutoDec = false;
                } else if (bfontcon && core.getPropertiesManager().getCachedFont() == null) {
                    try {
                        propertiesManager.setFontCon(new String(ch, start, length));
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        warningLog += "\nFont load error: " + e.getLocalizedMessage();
                    }
                    bfontcon = false;
                } else if (bwordClassNotes) {
                    TypeNode bufferType = core.getTypes().getBufferType();
                    bufferType.setNotes(bufferType.getNotes()
                            + new String(ch, start, length));
                } else if (bwordClassName) {
                    TypeNode bufferType = core.getTypes().getBufferType();
                    bufferType.setValue(bufferType.getValue()
                            + new String(ch, start, length));
                } else if (bwordClassPattern) {
                    TypeNode bufferType = core.getTypes().getBufferType();
                    bufferType.setPattern(bufferType.getPattern()
                            + new String(ch, start, length));
                } else if (bwordClassGloss) {
                    TypeNode bufferType = core.getTypes().getBufferType();
                    bufferType.setGloss(bufferType.getGloss()
                            + new String(ch, start, length));
                } else if (bwordClassId) {
                    wCId = Integer.parseInt(new String(ch, start, length));
                    bwordClassId = false;
                } else if (bpronuncation) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    try {
                        bufferWord.setPronunciation(bufferWord.getPronunciation()
                                + new String(ch, start, length));
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        // Don't bother raising an exception. This is regenerated
                        // each time the word is accessed if the error pops
                        // users will be informed at that more obvious point.
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
                } else if (blangName) {
                    propertiesManager.setLangName(propertiesManager.getLangName()
                            + new String(ch, start, length));
                } else if (blangRegexFontOvr) {
                    propertiesManager.setOverrideRegexFont(new String(ch, start, length).equals(PGTUtil.TRUE));
                } else if (bfontSize) {
                    propertiesManager.setFontSize(Integer.parseInt(new String(ch, start, length)));
                    bfontSize = false;
                } else if (bfontStyle) {
                    propertiesManager.setFontStyle(Integer.parseInt(new String(ch, start, length)));
                    bfontStyle = false;
                } else if (bfontLocalSize) {
                    propertiesManager.setLocalFontSize(Double.parseDouble(new String(ch, start, length)));
                }
                else if (balphaOrder) {
                    try {
                        propertiesManager.setAlphaOrder(propertiesManager.getAlphaPlainText()
                                + new String(ch, start, length), true);
                    } catch (Exception e) {
                        throw new SAXException("Load error: " + e.getLocalizedMessage(), e);
                    }
                } else if (bDecId) {
                    declensionMgr.setBufferId(Integer.parseInt(new String(ch, start, length)));
                    bDecId = false;
                } else if (bDecText) {
                    declensionMgr.setBufferDecText(declensionMgr.getBufferDecText()
                            + new String(ch, start, length));
                } else if (bDecNotes) {
                    declensionMgr.setBufferDecNotes(declensionMgr.getBufferDecNotes()
                            + new String(ch, start, length));
                } else if (bDecIsTemp) {
                    declensionMgr.setBufferDecTemp(new String(ch, start, length).equals("1"));
                    bDecIsTemp = false;
                } else if (bDecIsDimless) {
                    declensionMgr.getBuffer().setDimensionless(new String(ch, start, length).equals(PGTUtil.TRUE));
                } else if (bDecCombId) {
                    declensionMgr.getBuffer().setCombinedDimId(new String(ch, start, length));
                    bDecIsTemp = false;
                } else if (bDecRelId) {
                    declensionMgr.setBufferRelId(Integer.parseInt(new String(ch, start, length)));
                    bDecRelId = false;
                } else if (bpronBase) {
                    proBuffer.setValue(proBuffer.getValue()
                            + new String(ch, start, length));
                } else if (bpronPhon) {
                    proBuffer.setPronunciation(proBuffer.getPronunciation()
                            + new String(ch, start, length));
                } else if (bromBase) {
                    romBuffer.setValue(romBuffer.getValue()
                            + new String(ch, start, length));
                } else if (bromActive) {
                    romanizationMgr.setEnabled(new String(ch, start, length).equals(PGTUtil.TRUE));
                } else if (bromPhon) {
                    romBuffer.setPronunciation(romBuffer.getPronunciation()
                            + new String(ch, start, length));
                } else if (bwordClassProcMan) {
                    core.getTypes().getBufferType().setProcMandatory(new String(ch, start, length).equals(PGTUtil.TRUE));
                    bwordClassProcMan = false;
                } else if (bwordClassDefMan) {
                    core.getTypes().getBufferType().setDefMandatory(new String(ch, start, length).equals(PGTUtil.TRUE));
                    bwordClassDefMan = false;
                } else if (blangPropLocalUniqueness) {
                    propertiesManager.setLocalUniqueness(new String(ch, start, length).equals(PGTUtil.TRUE));
                    blangPropLocalUniqueness = false;
                } else if (blangPropUseLocalLex) {
                    propertiesManager.setUseLocalWordLex(new String(ch, start, length).equals(PGTUtil.TRUE));
                } else if (blangPropWordUniqueness) {
                    propertiesManager.setWordUniqueness(new String(ch, start, length).equals(PGTUtil.TRUE));
                    blangPropWordUniqueness = false;
                } else if (blangPropLocalMandatory) {
                    propertiesManager.setLocalMandatory(new String(ch, start, length).equals(PGTUtil.TRUE));
                    blangPropLocalMandatory = false;
                } else if (blangPropTypeMandatory) {
                    propertiesManager.setTypesMandatory(new String(ch, start, length).equals(PGTUtil.TRUE));
                    blangPropTypeMandatory = false;
                } else if (blangPropEnforceRTL) {
                    propertiesManager.setEnforceRTL(new String(ch, start, length).equals(PGTUtil.TRUE));
                    blangPropEnforceRTL = false;
                } else if (blangPropAuthCopyright) {
                    propertiesManager.setCopyrightAuthorInfo(propertiesManager.getCopyrightAuthorInfo()
                            + new String(ch, start, length));
                } else if (blangPropSimpConj) {
                    propertiesManager.setUseSimplifiedConjugations(new String(ch, start, length).equals(PGTUtil.TRUE));
                } else if (blangPropLocalLangName) {
                    propertiesManager.setLocalLangName(propertiesManager.getLocalLangName()
                            + new String(ch, start, length));
                } else if (bdimId) {
                    declensionMgr.getBuffer().getBuffer().setId(Integer.parseInt(new String(ch, start, length)));
                    bdimId = false;
                } else if (bdimName) {
                    DeclensionDimension dimBuffer = declensionMgr.getBuffer().getBuffer();
                    dimBuffer.setValue(dimBuffer.getValue()
                            + new String(ch, start, length));
                } else if (bfamName) {
                    FamNode famBuffer = famMgr.getBuffer();
                    famBuffer.setValue(famBuffer.getValue()
                            + new String(ch, start, length));
                } else if (bfamNotes) {
                    FamNode node = famMgr.getBuffer();
                    node.setNotes(node.getNotes() + new String(ch, start, length));
                } else if (bfamWord) {
                    try {
                        famMgr.getBuffer().addWord(core.getWordCollection().getNodeById(
                                Integer.parseInt(new String(ch, start, length))));
                    } catch (NumberFormatException e) {
                        IOHandler.writeErrorLog(e);
                        warningLog += "\nFamily load error: " + e.getLocalizedMessage();
                    }
                    bfamWord = false;
                } else if (bignoreCase) {
                    core.getPropertiesManager().setIgnoreCase(new String(ch, start, length).equals(PGTUtil.TRUE));
                    bignoreCase = false;
                } else if (bdisableProcRegex) {
                    core.getPropertiesManager().setDisableProcRegex(new String(ch, start, length).equals(PGTUtil.TRUE));
                    bdisableProcRegex = false;
                } else if (bdecGenTransClassVal) {
                    String[] classValueIds = new String(ch, start, length).split(",");
                    core.getDeclensionManager().getRuleBuffer().addClassToFilterList(
                            Integer.parseInt(classValueIds[0]),
                            Integer.parseInt(classValueIds[1]));
                } else if (bdecGenRuleComb) {
                    core.getDeclensionManager().getRuleBuffer().setCombinationId(new String(ch, start, length));
                    bdecGenRuleComb = false;
                } else if (bdecGenRuleName) {
                    DeclensionGenRule ruleBuffer = core.getDeclensionManager().getRuleBuffer();
                    ruleBuffer.setName(ruleBuffer.getName()
                            + new String(ch, start, length));
                } else if (bdecGenRuleRegex) {
                    DeclensionGenRule ruleBuffer = core.getDeclensionManager().getRuleBuffer();
                    ruleBuffer.setRegex(ruleBuffer.getRegex()
                            + new String(ch, start, length));
                } else if (bdecGenRuleType) {
                    core.getDeclensionManager().getRuleBuffer().setTypeId(Integer.parseInt(new String(ch, start, length)));
                    bdecGenRuleType = false;
                } else if (bdecGenTransRegex) {
                    DeclensionGenTransform transBuffer = core.getDeclensionManager().getRuleBuffer().getTransBuffer();
                    transBuffer.regex += new String(ch, start, length);
                } else if (bdecGenTransRep) {
                    DeclensionGenTransform transBuffer = core.getDeclensionManager().getRuleBuffer().getTransBuffer();
                    transBuffer.replaceText += new String(ch, start, length);
                } else if (bdecGenRuleIndex) {
                    core.getDeclensionManager().getRuleBuffer().setIndex(Integer.parseInt(new String(ch, start, length)));
                    bdecGenRuleIndex = false;
                } else if (bcombinedFormId) {
                    combinedDecId += new String(ch, start, length);
                } else if (bcombinedFormSurpress) {
                    core.getDeclensionManager().setCombinedDeclSuppressedRaw(combinedDecId,
                            new String(ch, start, length).equals(PGTUtil.TRUE));
                } else if (blogoStrokes) {
                    try {
                        core.getLogoCollection().getBufferNode().setStrokes(Integer.parseInt(new String(ch, start, length)));
                    } catch (NumberFormatException e) {
                        IOHandler.writeErrorLog(e);
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
                        IOHandler.writeErrorLog(e);
                        warningLog += "\nLogograph load error: " + e.getLocalizedMessage();
                    }
                } else if (blogoWordRelation) {
                    try {
                        core.getLogoCollection().loadLogoRelations(new String(ch, start, length));
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
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
                } else if (bclassId) {
                    core.getWordClassCollection().getBuffer().setId(Integer.parseInt(new String(ch, start, length)));
                } else if (bclassName) {
                    WordClass buffer = core.getWordClassCollection().getBuffer();
                    buffer.setValue(buffer.getValue() + new String(ch, start, length));
                } else if (bclassApplyTypes) {
                    String types = new String(ch, start, length);
                    WordClass buffer = core.getWordClassCollection().getBuffer();
                    for (String curType : types.split(",")) {
                        int typeId = Integer.parseInt(curType);
                        buffer.addApplyType(typeId);
                    }
                } else if (bclassFreeText) {
                    String freeText = new String(ch, start, length);                    
                    if (freeText.equals(PGTUtil.TRUE)) {
                        core.getWordClassCollection().getBuffer().setFreeText(true);
                    } else {
                        core.getWordClassCollection().getBuffer().setFreeText(false);
                    }
                } else if (bclassValueId) {
                    core.getWordClassCollection().getBuffer().buffer.setId(Integer.parseInt(new String(ch, start, length)));
                } else if (bclassValueName) {
                    WordClassValue value = core.getWordClassCollection().getBuffer().buffer;
                    value.setValue(value.getValue() + new String(ch, start, length));
                } else if (bcharRepChar) {
                    // can only pull single character, so no need to concatinate
                    charRepCharBuffer = new String(ch, start, length);
                } else if (bcharRepValue) {
                    charRepValBuffer += new String(ch, start, length);
                } else if (bKerningValue) {
                    try {
                        core.getPropertiesManager().setKerningSpace(Double.parseDouble(new String(ch, start, length)));
                    } catch (NumberFormatException e) {
                        IOHandler.writeErrorLog(e);
                        warningLog += "\nProblem loading kerning value: " + e.getLocalizedMessage();
                    }
                } else if (bprocRecurse) {
                    core.getPronunciationMgr().setRecurse(
                            new String(ch, start, length).equals(PGTUtil.TRUE));
                } else if (bromRecurse) {
                    core.getRomManager().setRecurse(
                            new String(ch, start, length).equals(PGTUtil.TRUE));
                } else if (betyIntRelationNode) {
                    core.getEtymologyManager().setBufferParent(Integer.parseInt(
                            new String(ch, start, length)));
                    betyIntRelationNode = false;
                } else if (betyIntChild) {
                    core.getEtymologyManager().setBufferChild(Integer.parseInt(
                            new String(ch, start, length)));
                    betyIntChild = false;
                } else if (betyChildExternals) {
                    core.getEtymologyManager().setBufferChild(Integer.parseInt(
                            new String(ch, start, length)));
                    betyChildExternals = false;
                } else if (betyExternalWordValue) {
                    EtyExternalParent ext = core.getEtymologyManager().getBufferExtParent();
                    ext.setValue(ext.getExternalWord() + new String(ch, start, length));
                } else if (betyExternalWordOrigin) {
                    EtyExternalParent ext = core.getEtymologyManager().getBufferExtParent();
                    ext.setExternalLanguage(ext.getExternalLanguage() + new String(ch, start, length));
                } else if (betyExternalWordDefinition) {
                    EtyExternalParent ext = core.getEtymologyManager().getBufferExtParent();
                    ext.setDefinition(ext.getDefinition() + new String(ch, start, length));
                } else if (btoDoNodeLabel) {
                    ToDoNode node = core.getToDoManager().getBuffer();
                    node.setValue(node.toString() + new String(ch, start, length));
                } else if (btoDoNodeDone) {
                    core.getToDoManager().getBuffer().setDone(new String(ch, start, length).equals(PGTUtil.TRUE));
                }
            }
            
            @Override
            public void endDocument() {
                // Version 2.3 implemented class filters for conj rules. Default to all on.
                if (versionHierarchy < PGTUtil.getVersionHierarchy("2.2")) {
                    core.getDeclensionManager().setAllDeclensionRulesToAllClasses();
                }
            }
        };
    }

    private CustHandlerFactory() {}
}
