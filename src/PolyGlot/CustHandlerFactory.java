/*
 * Copyright (c) 2014-2018, Draque Thompson, draquemail@gmail.com
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
package PolyGlot;

import PolyGlot.Nodes.DeclensionGenTransform;
import PolyGlot.Nodes.DeclensionGenRule;
import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.PronunciationNode;
import PolyGlot.Nodes.LogoNode;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.Nodes.FamNode;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.ManagersCollections.PropertiesManager;
import PolyGlot.ManagersCollections.GrammarManager;
import PolyGlot.ManagersCollections.PronunciationMgr;
import PolyGlot.ManagersCollections.FamilyManager;
import PolyGlot.ManagersCollections.DeclensionManager;
import PolyGlot.CustomControls.GrammarSectionNode;
import PolyGlot.CustomControls.GrammarChapNode;
import PolyGlot.ManagersCollections.ConWordCollection;
import PolyGlot.ManagersCollections.RomanizationManager;
import PolyGlot.Nodes.EtyExternalParent;
import PolyGlot.Nodes.WordClassValue;
import PolyGlot.Nodes.WordClass;
import java.io.InputStream;
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
public class CustHandlerFactory {

    /**
     * Creates appropriate handler to read file (based on version of PolyGlot
     * file was saved with)
     *
     * @param fileStream stream of file to be loaded
     * @param core dictionary core
     * @return an appropriate handler for the xml file
     * @throws java.lang.Exception when unable to read given file or if file is
     * from newer version of PolyGlot
     */
    public static CustHandler getCustHandler(InputStream fileStream, DictCore core) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc;

        doc = dBuilder.parse(fileStream);
        doc.getDocumentElement().normalize();

        // test for version number in pgd file, set to 0 if none found (pre 0.6)
        Node versionNode = doc.getDocumentElement().getElementsByTagName(PGTUtil.pgVersionXID).item(0);
        String versionNumber = versionNode == null ? "0" : versionNode.getTextContent();
        int fileVersionHierarchy = core.getVersionHierarchy(versionNumber);
        
        if (fileVersionHierarchy == -1) {
            throw new Exception("Please upgrade PolyGlot. The PGD file you are loading was "
                        + "written with a newer version with additional features: Ver " + versionNumber + ".");
        } else if (fileVersionHierarchy < core.getVersionHierarchy("0.7.5")) {
            throw new Exception("Version " + versionNumber + " no longer supported. Load/save with older version of"
                        + "PolyGlot (0.7.5 through 1.2) to upconvert.");
        }

        return CustHandlerFactory.get075orHigherHandler(core, fileVersionHierarchy);
    }

    private static CustHandler get075orHigherHandler(final DictCore core, final int versionHierarchy) {
        return new CustHandler() {

            PronunciationNode proBuffer;
            PronunciationNode romBuffer;
            String charRepCharBuffer = "";
            String charRepValBuffer = "";
            int ruleIdBuffer = 0;
            String ruleValBuffer = "";
            boolean blocalWord = false;
            boolean bconWord = false;
            boolean btype = false;
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
            boolean blangPropEnforceRTL = false;
            boolean blangPropLocalLangName = false;
            boolean blangPropAuthCopyright = false;
            boolean bdeclensionMandatory = false;
            boolean bwordClassDefMan = false;
            boolean bwordClassGenderMan = false;
            boolean bwordClassProcMan = false;
            boolean bwordClassPlurMan = false;
            boolean bwordClassPattern = false;
            boolean bwordProcOverride = false;
            boolean bdimNode = false;
            boolean bdimId = false;
            boolean bdimMand = false;
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
            
            int wId;
            int wCId;
            int wGId;
            String combinedDecId = "";
            String tmpString; // used mostly for converting deprecated values (as they no longer have placeholder points)

            DeclensionManager declensionMgr = core.getDeclensionManager();
            PronunciationMgr pronuncMgr = core.getPronunciationMgr();
            RomanizationManager romanizationMgr = core.getRomManager();
            PropertiesManager propertiesManager = core.getPropertiesManager();
            FamilyManager famMgr = core.getFamManager();

            @Override
            public void startElement(String uri, String localName,
                    String qName, Attributes attributes)
                    throws SAXException {

                if (qName.equalsIgnoreCase(PGTUtil.wordXID)) {
                    core.getWordCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideXID)) {
                    proBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(PGTUtil.romGuideNodeXID)) {
                    romBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(PGTUtil.localWordXID)) {
                    blocalWord = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.conWordXID)) {
                    bconWord = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordTypeXID)) {
                    btype = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordTypeIdXID)) {
                    btypeId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordIdXID)) {
                    bId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordDefXID)) {
                    bdef = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordPlurXID)) {
                    // plurality made into declension-deprecated from main screen
                    declensionMgr.clearBuffer();
                    bwordPlur = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordEtymologyNotesXID)) {
                    bwordEtymNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordRuleOverrideXID)) {
                    bwordRuleOverride = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassAndValueXID)) {
                    bclassVal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassTextValueXID)){
                    bwordClassTextVal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontConXID)) {
                    bfontcon = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontLocalXID)) {
                    bfontlocal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeIdXID)) {
                    bwordClassId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeNameXID)) {
                    bwordClassName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeNotesXID)) {
                    bwordClassNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeGlossXID)) {
                    bwordClassGloss = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordProcXID)) {
                    bpronuncation = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordGenderXID)) {
                    tmpString = ""; // temp value to store deprecated field
                    bgender = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.genderIdXID)) {
                    bgenderId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.genderNameXID)) {
                    bgenderName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.genderNotesXID)) {
                    bgenderNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropLangNameXID)) {
                    blangName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropFontSizeXID)) {
                    bfontSize = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropLocalFontSizeXID)) {
                    bfontLocalSize = true;
                }
                else if (qName.equalsIgnoreCase(PGTUtil.langPropFontStyleXID)) {
                    bfontStyle = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropAlphaOrderXID)) {
                    balphaOrder = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropEnforceRTLXID)) {
                    blangPropEnforceRTL = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropAuthCopyrightXID)) {
                    blangPropAuthCopyright = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropLocalLangNameXID)) {
                    blangPropLocalLangName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordAutoDeclenOverrideXID)) {
                    bwordoverAutoDec = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionXID)) {
                    // from old versions, declensions are loaded as dimensions of a master declension
                    declensionMgr.getBuffer().clearBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionIdXID)) {
                    bDecId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionTextXID)) {
                    bDecText = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionNotesXID)) {
                    bDecNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionIsTemplateXID)) {
                    bDecIsTemp = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionIsDimensionless)) {
                    bDecIsDimless = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionRelatedIdXID)) {
                    bDecRelId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideBaseXID)) {
                    bpronBase = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuidePhonXID)) {
                    bpronPhon = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.romGuideBaseXID)) {
                    bromBase = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.romGuideEnabledXID)) {
                    bromActive = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.romGuidePhonXID)) {
                    bromPhon = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordProcOverrideXID)) {
                    bwordProcOverride = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.typePlurManXID)) {
                    bwordClassPlurMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeProcManXID)) {
                    bwordClassProcMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeGenderManXID)) {
                    bwordClassGenderMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeDefManXID)) {
                    bwordClassDefMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.typePatternXID)) {
                    bwordClassPattern = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionMandatoryXID)) {
                    bdeclensionMandatory = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropLocalUniquenessXID)) {
                    blangPropLocalUniqueness = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropWordUniquenessXID)) {
                    blangPropWordUniqueness = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropLocalMandatoryXID)) {
                    blangPropLocalMandatory = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropTypeMandatoryXID)) {
                    blangPropTypeMandatory = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.dimensionNodeXID)) {
                    bdimNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.dimensionIdXID)) {
                    bdimId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.dimensionMandXID)) {
                    bdimMand = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.dimensionNameXID)) {
                    bdimName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionComDimIdXID)) {
                    bDecCombId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.famNameXID)) {
                    bfamName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.famNodeXID)) {
                    famMgr.buildNewBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.famNotesXID)) {
                    bfamNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.famWordXID)) {
                    bfamWord = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropIgnoreCaseXID)) {
                    bignoreCase = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropDisableProcRegexXID)) {
                    bdisableProcRegex = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleCombXID)) {
                    bdecGenRuleComb = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleNameXID)) {
                    bdecGenRuleName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleRegexXID)) {
                    bdecGenRuleRegex = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleTypeXID)) {
                    bdecGenRuleType = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenTransRegexXID)) {
                    bdecGenTransRegex = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenTransReplaceXID)) {
                    bdecGenTransRep = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleApplyToClassValue)) {
                    bdecGenTransClassVal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleIndexXID)) {
                    bdecGenRuleIndex = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.decCombinedIdXID)) {
                    bcombinedFormId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.decCombinedSurpressXID)) {
                    bcombinedFormSurpress = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoStrokesXID)) {
                    blogoStrokes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoNotesXID)) {
                    blogoNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoIsRadicalXID)) {
                    blogoRadical = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoRadicalListXID)) {
                    blogoRadicalList = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoReadingXID)) {
                    blogoReading = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoGraphValueXID)) {
                    blogoValue = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoGraphIdXID)) {
                    blogoId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoGraphNodeXID)) {
                    blogoNode = true;
                    core.getLogoCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.logoWordRelationXID)) {
                    blogoWordRelation = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarChapterNodeXID)) {
                    bgrammarChapNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarChapterNameXID)) {
                    bgrammarChapName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarSectionNodeXID)) {
                    bgrammarSecNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarSectionNameXID)) {
                    bgrammarSecName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarSectionRecordingXID)) {
                    bgrammarSecRecId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarSectionTextXID)) {
                    bgrammarSecText = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassXID)) {
                    bclassNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassIdXID)) {
                    bclassId = true;
                    // the buffer should not default to "apply to all."
                    ((WordClass)core.getWordPropertiesCollection().getBuffer()).deleteApplyType(-1);
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassNameXID)) {
                    bclassName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassApplyTypesXID)) {
                    bclassApplyTypes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassIsFreetextXID)) {
                    bclassFreeText = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassValueNodeXID)) {
                    bclassValueNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassValueIdXID)) {
                    bclassValueId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassValueNameXID)) {
                    bclassValueName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropCharRepCharacterXID)) {
                    bcharRepChar = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropCharRepValueXID)) {
                    bcharRepValue = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropKerningVal)) {
                    bKerningValue = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideRecurseXID)) {
                    bprocRecurse = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.romGuideRecurseXID)) {
                    bromRecurse = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyIntRelationNodeXID)) {
                     betyIntRelationNode= true;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyIntChildXID)) {
                     betyIntChild= true;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyChildExternalsXID)) {
                     betyChildExternals= true;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyExternalWordNodeXID)) {
                     betyExternalWordNode = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyExternalWordValueXID)) {
                     betyExternalWordValue = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyExternalWordOriginXID)) {
                     betyExternalWordOrigin = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyExternalWordDefinitionXID)) {
                     betyExternalWordDefinition = true;
                }
            }

            @Override
            public void endElement(String uri, String localName,
                    String qName) throws SAXException {

                // save word to word collection
                if (qName.equalsIgnoreCase(PGTUtil.wordXID)) {
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
                        throw new SAXException("Word insertion error: " + e.getLocalizedMessage());
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.typeXID)) {
                    // insertion for word types is much simpler
                    try {
                        core.getTypes().insert(wCId);
                    } catch (Exception e) {
                        throw new SAXException("Type insertion error: " + e.getLocalizedMessage());
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.genderXID)) {
                    // Deprecated
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideXID)) {
                    pronuncMgr.addPronunciation(proBuffer);
                } else if (qName.equalsIgnoreCase(PGTUtil.romGuideNodeXID)) {
                    romanizationMgr.addPronunciation(romBuffer);
                } else if (qName.equalsIgnoreCase(PGTUtil.wordGenderXID)) {
                    // only create property if necessary.
                    if (tmpString.length() != 0) {
                        // this uses a slow, heuristic method because it's a one time process
                        // that is replacing the existing, inexact method with an ID based one
                        WordClass writeProp = null;
                        // find gender property
                        for (WordClass prop : core.getWordPropertiesCollection().getAllWordClasses()) {
                            if (prop.getValue().equals("Gender")) {
                                writeProp = prop;
                                break;
                            }
                        }

                        try {
                            // create gender if doesn't exist
                            if (writeProp == null) {
                                core.getWordPropertiesCollection().clear();
                                core.getWordPropertiesCollection().getBuffer().setValue("Gender");
                                int id = core.getWordPropertiesCollection().insert();
                                writeProp = (WordClass) core.getWordPropertiesCollection().getNodeById(id);
                            }

                            WordClassValue valueWrite = null;

                            for (WordClassValue value : writeProp.getValues()) {
                                // test against constructed gender string
                                if (value.getValue().equals(tmpString)) {
                                    valueWrite = value;
                                    break;
                                }
                            }

                            if (valueWrite == null) {
                                valueWrite = writeProp.addValue(tmpString);
                            }

                            ConWord bufferWord = core.getWordCollection().getBufferWord();
                            bufferWord.setClassValue(writeProp.getId(), valueWrite.getId());
                            
                            // when pulling from legacy gender system, apply to all words initially
                            writeProp.addApplyType(-1);
                        } catch (Exception e) {
                            warningLog += "\nGender class load error: " + e.getLocalizedMessage();
                        }
                    }
                    bgender = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionXID)) {
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
                } else if (qName.equalsIgnoreCase(PGTUtil.localWordXID)) {
                    blocalWord = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.conWordXID)) {
                    bconWord = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordPlurXID)) {
                    // plurality now a declension (as it should be)
                    // special position granted to plurals... fixes awful ID collision error

                    // skip insertion of empty
                    if (declensionMgr.getBuffer().getValue().trim().length() != 0) {
                        declensionMgr.getBuffer().setCombinedDimId("," + wId + "," + PGTUtil.wordPlurXID + ",");
                        declensionMgr.addDeclensionToWord(wId, Integer.MAX_VALUE, declensionMgr.getBuffer());
                    }

                    declensionMgr.clearBuffer();
                    bwordPlur = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordTypeXID)) {
                    btype = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordTypeIdXID)) {
                    btypeId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordIdXID)) {
                    bId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordRuleOverrideXID)) {
                    bwordRuleOverride = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassAndValueXID)) {
                    bclassVal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassTextValueXID)){
                    core.getWordCollection().getBufferWord().setClassTextValue(ruleIdBuffer, ruleValBuffer);
                    ruleIdBuffer = 0;
                    ruleValBuffer = "";
                    bwordClassTextVal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordDefXID)) {
                    // finalize loading of def (if it contains archived HTML elements
                    ConWord curWord = core.getWordCollection().getBufferWord();
                    try {
                        curWord.setDefinition(WebInterface.unarchiveHTML(curWord.getDefinition(), core));
                    } catch (Exception e) {
                        warningLog += "\nWord image load error: " + e.getLocalizedMessage();
                    }
                    bdef = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordEtymologyNotesXID)) {
                    bwordEtymNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontConXID)) {
                    bfontcon = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontLocalXID)) {
                    bfontlocal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeNameXID)) {
                    bwordClassName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeNotesXID)) {
                    TypeNode node = core.getTypes().getBufferType();
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(node.getNotes(), core));
                    } catch (Exception e) {
                        warningLog += "\nProblem loading part of speech note image: " + e.getLocalizedMessage();
                    }
                    bwordClassNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.typePlurManXID)) {
                    bwordClassPlurMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeProcManXID)) {
                    bwordClassProcMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeGenderManXID)) {
                    bwordClassGenderMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeDefManXID)) {
                    bwordClassDefMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.typePatternXID)) {
                    bwordClassPattern = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.typeGlossXID)) {
                    bwordClassGloss = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordProcXID)) {
                    bpronuncation = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.genderIdXID)) {
                    bgenderId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.genderNameXID)) {
                    bgenderName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.genderNotesXID)) {
                    bgenderNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordAutoDeclenOverrideXID)) {
                    bwordoverAutoDec = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropLangNameXID)) {
                    blangName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropFontSizeXID)) {
                    bfontSize = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropFontStyleXID)) {
                    bfontStyle = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropAlphaOrderXID)) {
                    balphaOrder = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropEnforceRTLXID)) {
                    blangPropEnforceRTL = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropAuthCopyrightXID)) {
                    blangPropAuthCopyright = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropLocalLangNameXID)) {
                    blangPropLocalLangName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionIdXID)) {
                    bDecId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionTextXID)) {
                    bDecText = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionNotesXID)) {
                    try {
                        declensionMgr.setBufferDecNotes(WebInterface.unarchiveHTML(declensionMgr.getBufferDecNotes(), core));
                    } catch (Exception e) {
                        warningLog += "\nProblem loading declension notes image: " + e.getLocalizedMessage();
                    }
                    bDecNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionIsTemplateXID)) {
                    bDecIsTemp = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionIsDimensionless)) {
                    bDecIsDimless = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionRelatedIdXID)) {
                    bDecRelId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionMandatoryXID)) {
                    bdeclensionMandatory = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionComDimIdXID)) {
                    bDecCombId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideBaseXID)) {
                    bpronBase = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuidePhonXID)) {
                    bpronPhon = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.romGuideBaseXID)) {
                    bromBase = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.romGuideEnabledXID)) {
                    bromActive = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.romGuidePhonXID)) {
                    bromPhon = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.dimensionNodeXID)) {
                    try {
                        declensionMgr.getBuffer().insertBuffer();
                        declensionMgr.getBuffer().clearBuffer();
                    } catch (Exception e) {
                        throw new SAXException(e);
                    }
                    bdimNode = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.dimensionIdXID)) {
                    bdimId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.dimensionMandXID)) {
                    bdimMand = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.dimensionNameXID)) {
                    bdimName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.famNameXID)) {
                    bfamName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.famNodeXID)) {
                    famMgr.bufferDone();
                } else if (qName.equalsIgnoreCase(PGTUtil.famNotesXID)) {
                    FamNode node = core.getFamManager().getBuffer();
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(node.getNotes(), core));
                    } catch (Exception e) {
                        warningLog += "\nProblem loading family note image: " + e.getLocalizedMessage();
                    }
                    bfamNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.famWordXID)) {
                    bfamWord = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropIgnoreCaseXID)) {
                    bignoreCase = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropDisableProcRegexXID)) {
                    bdisableProcRegex = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleXID)) {
                    core.getDeclensionManager().insRuleBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleCombXID)) {
                    bdecGenRuleComb = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleNameXID)) {
                    bdecGenRuleName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleRegexXID)) {
                    bdecGenRuleRegex = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleApplyToClassValue)) {
                    bdecGenTransClassVal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleTypeXID)) {
                    bdecGenRuleType = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenTransXID)) {
                    core.getDeclensionManager().getRuleBuffer().insertTransBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenTransRegexXID)) {
                    bdecGenTransRegex = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenTransReplaceXID)) {
                    bdecGenTransRep = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleIndexXID)) {
                    bdecGenRuleIndex = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decCombinedIdXID)) {
                    bcombinedFormId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decCombinedSurpressXID)) {
                    bcombinedFormSurpress = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decCombinedFormXID)) {
                    combinedDecId = "";
                } else if (qName.equalsIgnoreCase(PGTUtil.logoStrokesXID)) {
                    blogoStrokes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoNotesXID)) {
                    LogoNode node = core.getLogoCollection().getBufferNode();
                    try {
                        node.setNotes(WebInterface.unarchiveHTML(node.getNotes(), core));
                    } catch (Exception e) {
                        warningLog += "\nProblem loading logograph note image: " + e.getLocalizedMessage();
                    }
                    blogoNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoIsRadicalXID)) {
                    blogoRadical = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoRadicalListXID)) {
                    blogoRadicalList = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoReadingXID)) {
                    core.getLogoCollection().getBufferNode().insertReadingBuffer();
                    blogoReading = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoGraphValueXID)) {
                    blogoValue = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoGraphIdXID)) {
                    blogoId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoGraphNodeXID)) {
                    blogoNode = false;
                    try {
                        core.getLogoCollection().insert();
                    } catch (Exception e) {
                        warningLog += "\nLogograph load error: " + e.getLocalizedMessage();
                    }
                    core.getLogoCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.logoWordRelationXID)) {
                    blogoWordRelation = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarChapterNodeXID)) {
                    GrammarManager gMan = core.getGrammarManager();
                    gMan.insert();
                    gMan.clear();
                    bgrammarChapNode = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarChapterNameXID)) {
                    bgrammarChapName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarSectionNodeXID)) {
                    GrammarChapNode gChap = core.getGrammarManager().getBuffer();
                    gChap.insert();
                    gChap.clear();
                    bgrammarSecNode = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarSectionNameXID)) {
                    bgrammarSecName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarSectionRecordingXID)) {
                    bgrammarSecRecId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarSectionTextXID)) {
                    bgrammarSecText = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassXID)) {
                    try {
                        core.getWordPropertiesCollection().insert();
                    } catch (Exception e) {
                        warningLog += "\nWord class load error: " + e.getLocalizedMessage();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassIdXID)) {
                    bclassId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassNameXID)) {
                    bclassName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassApplyTypesXID)) {
                    bclassApplyTypes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassIsFreetextXID)) {
                    bclassFreeText = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassValueNodeXID)) {
                    try {
                        ((WordClass) core.getWordPropertiesCollection().getBuffer()).insert();
                    } catch (Exception e) {
                        warningLog += "\nWord class load error: " + e.getLocalizedMessage();
                    }
                    bclassValueNode = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassValueIdXID)) {
                    bclassValueId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.ClassValueNameXID)) {
                    bclassValueName = false;
                }  else if (qName.equalsIgnoreCase(PGTUtil.langPropCharRepNodeXID)) {
                    core.getPropertiesManager().addCharacterReplacement(charRepCharBuffer, charRepValBuffer);
                    charRepCharBuffer = "";
                    charRepValBuffer = "";
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropCharRepCharacterXID)) {
                    bcharRepChar = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropCharRepValueXID)) {
                    bcharRepValue = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropKerningVal)) {
                    bKerningValue = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropLocalFontSizeXID)) {
                    bfontLocalSize = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideRecurseXID)) {
                    bprocRecurse = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.romGuideRecurseXID)) {
                    bromRecurse = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyIntRelationNodeXID)) {
                     betyIntRelationNode= false;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyIntChildXID)) {
                     betyIntChild= false;
                     core.getEtymologyManager().insert();
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyChildExternalsXID)) {
                     betyChildExternals= false;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyExternalWordNodeXID)) {
                     betyExternalWordNode = false;
                     core.getEtymologyManager().insertBufferExtParent();
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyExternalWordValueXID)) {
                     betyExternalWordValue = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyExternalWordOriginXID)) {
                     betyExternalWordOrigin = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.EtyExternalWordDefinitionXID)) {
                     betyExternalWordDefinition = false;
                }
            }

            @Override
            public void characters(char ch[], int start, int length)
                    throws SAXException {

                if (blocalWord) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    bufferWord.setLocalWord(bufferWord.getLocalWord()
                            + new String(ch, start, length));
                } else if (bconWord) {
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    bufferWord.setValue(bufferWord.getValue()
                            + new String(ch, start, length));
                } else if (btype) { // THIS IS NOW ONLY FOR LEGACY PGT FILES. NO LONGER SAVED TO XML
                    ConWord bufferWord = core.getWordCollection().getBufferWord();
                    try {
                        bufferWord.setWordTypeId(core.getTypes().findByName(new String(ch, start, length)).getId());
                    } catch (Exception e) {
                        warningLog += "\nWord type load error: " + e.getLocalizedMessage();
                    }
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
                            .setProcOverride(new String(ch, start, length).equals(PGTUtil.True));
                    bwordProcOverride = false;
                } else if (bwordRuleOverride) {
                    core.getWordCollection().getBufferWord()
                            .setRulesOverride(new String(ch, start, length).equals(PGTUtil.True));
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
                            .setOverrideAutoDeclen(new String(ch, start, length).equals(PGTUtil.True));
                    bwordoverAutoDec = false;
                } else if (bfontcon && core.getPropertiesManager().getCachedFont() == null) {
                    try {
                        propertiesManager.setFontCon(new String(ch, start, length));
                    } catch (Exception e) {
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
                        // Don't bother raising an exception. This is regenerated
                        // each time the word is accessed if the error pops
                        // users will be informed at that, more obvious point.
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
                    propertiesManager.setAlphaOrder(propertiesManager.getAlphaPlainText()
                            + new String(ch, start, length));
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
                    declensionMgr.getBuffer().setDimensionless(new String(ch, start, length).equals(PGTUtil.True));
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
                    romanizationMgr.setEnabled(new String(ch, start, length).equals(PGTUtil.True));
                } else if (bromPhon) {
                    romBuffer.setPronunciation(romBuffer.getPronunciation()
                            + new String(ch, start, length));
                } /*else if (bproAutoPop) {
                    // Removed as of 1.0
                    propertiesManager.setProAutoPop((new String(ch, start, length).equalsIgnoreCase(PGTUtil.True)));
                    bproAutoPop = false;
                }*/ else if (bwordClassProcMan) {
                    core.getTypes().getBufferType().setProcMandatory(new String(ch, start, length).equals(PGTUtil.True));
                    bwordClassProcMan = false;
                } else if (bwordClassGenderMan) {
                    //typeCollection.getBufferType().setGenderMandatory(new String(ch, start, length).equals(PGTUtil.True)); // Deprecated
                    bwordClassGenderMan = false;
                } else if (bwordClassDefMan) {
                    core.getTypes().getBufferType().setDefMandatory(new String(ch, start, length).equals(PGTUtil.True));
                    bwordClassDefMan = false;
                } else if (bdeclensionMandatory) {
                    declensionMgr.setBufferDecMandatory(new String(ch, start, length).equals(PGTUtil.True));
                    bdeclensionMandatory = false;
                } else if (blangPropLocalUniqueness) {
                    propertiesManager.setLocalUniqueness(new String(ch, start, length).equals(PGTUtil.True));
                    blangPropLocalUniqueness = false;
                } else if (blangPropWordUniqueness) {
                    propertiesManager.setWordUniqueness(new String(ch, start, length).equals(PGTUtil.True));
                    blangPropWordUniqueness = false;
                } else if (blangPropLocalMandatory) {
                    propertiesManager.setLocalMandatory(new String(ch, start, length).equals(PGTUtil.True));
                    blangPropLocalMandatory = false;
                } else if (blangPropTypeMandatory) {
                    propertiesManager.setTypesMandatory(new String(ch, start, length).equals(PGTUtil.True));
                    blangPropTypeMandatory = false;
                } else if (blangPropEnforceRTL) {
                    propertiesManager.setEnforceRTL(new String(ch, start, length).equals(PGTUtil.True));
                    blangPropEnforceRTL = false;
                } else if (blangPropAuthCopyright) {
                    propertiesManager.setCopyrightAuthorInfo(propertiesManager.getCopyrightAuthorInfo()
                            + new String(ch, start, length));
                } else if (blangPropLocalLangName) {
                    propertiesManager.setLocalLangName(propertiesManager.getLocalLangName()
                            + new String(ch, start, length));
                } else if (bdimMand) {
                    declensionMgr.getBuffer().getBuffer().setMandatory(new String(ch, start, length).equals(PGTUtil.True));
                    bdimMand = false;
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
                    } catch (ConWordCollection.WordNotExistsException e) {
                        // do nothing. if a word has been deleted, simply do not load it here.
                    } catch (NumberFormatException e) {
                        warningLog += "\nFamily load error: " + e.getLocalizedMessage();
                    }
                    bfamWord = false;
                } else if (bignoreCase) {
                    core.getPropertiesManager().setIgnoreCase(new String(ch, start, length).equals(PGTUtil.True));
                    bignoreCase = false;
                } else if (bdisableProcRegex) {
                    core.getPropertiesManager().setDisableProcRegex(new String(ch, start, length).equals(PGTUtil.True));
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
                    core.getDeclensionManager().setCombinedDeclSurpressedRaw(combinedDecId,
                            new String(ch, start, length).equals(PGTUtil.True));
                } else if (blogoStrokes) {
                    try {
                        core.getLogoCollection().getBufferNode().setStrokes(Integer.parseInt(new String(ch, start, length)));
                    } catch (NumberFormatException e) {
                        warningLog += "\nLogograph load error: " + e.getLocalizedMessage();
                    }
                } else if (blogoNotes) {
                    LogoNode curNode = core.getLogoCollection().getBufferNode();
                    curNode.setNotes(curNode.getNotes() + new String(ch, start, length));
                } else if (blogoRadical) {
                    core.getLogoCollection().getBufferNode().setRadical(
                            new String(ch, start, length).equals(PGTUtil.True));
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
                        warningLog += "\nLogograph load error: " + e.getLocalizedMessage();
                    }
                } else if (blogoWordRelation) {
                    try {
                        core.getLogoCollection().loadLogoRelations(new String(ch, start, length));
                    } catch (Exception e) {
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
                    core.getWordPropertiesCollection().getBuffer().setId(Integer.parseInt(new String(ch, start, length)));
                } else if (bclassName) {
                    WordClass buffer = (WordClass) core.getWordPropertiesCollection().getBuffer();
                    buffer.setValue(buffer.getValue() + new String(ch, start, length));
                } else if (bclassApplyTypes) {
                    String types = new String(ch, start, length);
                    WordClass buffer = (WordClass) core.getWordPropertiesCollection().getBuffer();
                    for (String curType : types.split(",")) {
                        int typeId = Integer.parseInt(curType);
                        buffer.addApplyType(typeId);
                    }
                } else if (bclassFreeText) {
                    String freeText = new String(ch, start, length);                    
                    if (freeText.equals(PGTUtil.True)) {
                        ((WordClass) core.getWordPropertiesCollection().getBuffer()).setFreeText(true);
                    } else {
                        ((WordClass) core.getWordPropertiesCollection().getBuffer()).setFreeText(false);
                    }
                } else if (bclassValueId) {
                    ((WordClass) core.getWordPropertiesCollection().getBuffer()).buffer.setId(Integer.parseInt(new String(ch, start, length)));
                } else if (bclassValueName) {
                    WordClassValue value = ((WordClass) core.getWordPropertiesCollection().getBuffer()).buffer;
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
                        warningLog += "\nProblem loading kerning value: " + e.getLocalizedMessage();
                    }
                } else if (bprocRecurse) {
                    core.getPronunciationMgr().setRecurse(
                            new String(ch, start, length).equals(PGTUtil.True));
                } else if (bromRecurse) {
                    core.getRomManager().setRecurse(
                            new String(ch, start, length).equals(PGTUtil.True));
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
                    ext.setExternalWord(ext.getExternalWord() + new String(ch, start, length));
                } else if (betyExternalWordOrigin) {
                    EtyExternalParent ext = core.getEtymologyManager().getBufferExtParent();
                    ext.setExternalLanguage(ext.getExternalLanguage() + new String(ch, start, length));
                } else if (betyExternalWordDefinition) {
                    EtyExternalParent ext = core.getEtymologyManager().getBufferExtParent();
                    ext.setDefinition(ext.getDefinition() + new String(ch, start, length));
                }
            }
            
            @Override
            public void endDocument() {
                // Version 2.3 implemented class filters for conj rules. Default to all on.
                if (versionHierarchy < core.getVersionHierarchy("2.2")) {
                    core.getDeclensionManager().setAllDeclensionRulesToAllClasses();
                }
            }
        };
    }
}
