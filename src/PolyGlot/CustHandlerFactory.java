/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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
import PolyGlot.Nodes.ThesNode;
import PolyGlot.Nodes.GenderNode;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.ManagersCollections.GenderCollection;
import PolyGlot.ManagersCollections.PropertiesManager;
import PolyGlot.ManagersCollections.GrammarManager;
import PolyGlot.ManagersCollections.PronunciationMgr;
import PolyGlot.ManagersCollections.ThesaurusManager;
import PolyGlot.ManagersCollections.DeclensionManager;
import PolyGlot.CustomControls.GrammarSectionNode;
import PolyGlot.CustomControls.GrammarChapNode;
import java.awt.Font;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class reads PGT files and loads them into memory. Starting with v.
 * 0.7.5, there are some custom loading properties that require individual
 * readers.
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
        //File XMLFile = new File(fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc;

        doc = dBuilder.parse(fileStream);
        doc.getDocumentElement().normalize();
        Node versionNode;
        String versionNumber;
        CustHandler ret = null;

        // test for version number in pgd file, set to 0 if none found (pre 0.6)
        versionNode = doc.getDocumentElement().getElementsByTagName(PGTUtil.pgVersionXID).item(0);
        versionNumber = versionNode == null ? "0" : versionNode.getTextContent();

        switch (versionNumber) {
            case "0":
            case "0.5":
            case "0.5.1":
            case "0.6":
            case "0.6.1":
            case "0.6.5":
            case "0.7":
                ret = CustHandlerFactory.get7orLowerHandler(core);
                break;
            case "0.7.5":
            case "0.7.6":
            case "0.7.6.1":
            case "0.8":
            case "0.8.1":
            case "0.8.1.1":
            case "0.8.1.2":
            case "0.8.5":
            case "0.9":
            case "0.9.1":
            case "0.9.2":
            case "0.9.9":
            case "0.9.9.1":
            case "1.0":
            case "1.0.1":
                ret = CustHandlerFactory.get075orHigherHandler(core);
                break;
            default:
                throw new Exception("Please upgrade PolyGlot. The PGD file you are loading was "
                        + "written with a newer version with additional features: Ver " + versionNumber + ".");
        }

        return ret;
    }

    private static CustHandler get7orLowerHandler(final DictCore core) {
        return new CustHandler() {

            PronunciationNode proBuffer;
            boolean blocalWord = false;
            boolean bconWord = false;
            boolean btype = false;
            boolean bId = false;
            boolean bdef = false;
            boolean bfontcon = false;
            boolean bfontlocal = false;
            boolean bwordClassName = false;
            boolean bwordClassId = false;
            boolean bwordClassNotes = false;
            boolean bpronuncation = false;
            boolean bgenderId = false;
            boolean bgenderNotes = false;
            boolean bgenderName = false;
            boolean bgender = false;
            boolean blangName = false;
            boolean bfontSize = false;
            boolean bfontStyle = false;
            boolean balphaOrder = false;
            boolean bDecId = false;
            boolean bDecText = false;
            boolean bDecNotes = false;
            boolean bDecIsTemp = false;
            boolean bDecRelId = false;
            boolean bpronBase = false;
            boolean bpronPhon = false;
            boolean bwordPlur = false;
            boolean blangPropTypeMandatory = false;
            boolean blangPropLocalMandatory = false;
            boolean blangPropWordUniqueness = false;
            boolean blangPropLocalUniqueness = false;
            boolean bdeclensionMandatory = false;
            boolean bwordClassDefMan = false;
            boolean bwordClassGenderMan = false;
            boolean bwordClassProcMan = false;
            boolean bwordClassPlurMan = false;
            boolean bwordProcOverride = false;
            boolean bwordRuleOverride = false;

            int wId;
            int wCId;
            int wGId;

            DeclensionManager declensionMgr = core.getDeclensionManager();
            GenderCollection genderCollection = core.getGenders();
            PronunciationMgr pronuncMgr = core.getPronunciationMgr();
            PropertiesManager propertiesManager = core.getPropertiesManager();

            @Override
            public void startElement(String uri, String localName,
                    String qName, Attributes attributes)
                    throws SAXException {

                // test to see whether this is the first node in a word
                if (qName.equalsIgnoreCase(PGTUtil.wordXID)) {
                    this.getWordCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideXID)) {
                    proBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(PGTUtil.localWordXID)) {
                    blocalWord = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.conWordXID)) {
                    bconWord = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordTypeXID)) {
                    btype = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordIdXID)) {
                    bId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.definitionXID)) {
                    bdef = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordPlurXID)) {
                    // plurality made into declension-deprecated from main screen
                    declensionMgr.clearBuffer();
                    bwordPlur = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontConXID)) {
                    bfontcon = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontLocalXID)) {
                    bfontlocal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassIdXID)) {
                    bwordClassId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassNameXID)) {
                    bwordClassName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassNotesXID)) {
                    bwordClassNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.pronunciationXID)) {
                    bpronuncation = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordGenderXID)) {
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
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropFontStyleXID)) {
                    bfontStyle = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropAlphaOrderXID)) {
                    balphaOrder = true;
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
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionRelatedIdXID)) {
                    bDecRelId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideBaseXID)) {
                    bpronBase = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuidePhonXID)) {
                    bpronPhon = true;
                } /*else if (qName.equalsIgnoreCase(PGTUtil.proAutoPopXID)) {
                    // Removed as of 1.0
                    bproAutoPop = true;
                }*/ else if (qName.equalsIgnoreCase(PGTUtil.wordProcOverrideXID)) {
                    bwordProcOverride = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassPlurManXID)) {
                    bwordClassPlurMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassProcManXID)) {
                    bwordClassProcMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassGenderManXID)) {
                    bwordClassGenderMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassDefManXID)) {
                    bwordClassDefMan = true;
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
                } else if (qName.equalsIgnoreCase(PGTUtil.wordRuleOverrideXID)) {
                    bwordRuleOverride = true;
                }
            }

            @Override
            public void endElement(String uri, String localName,
                    String qName) throws SAXException {

                // save word to word collection
                if (qName.equalsIgnoreCase(PGTUtil.wordXID)) {
                    ConWord curWord = this.getWordCollection()
                            .getBufferWord();

                    try {
                        // if word is valid, save. Throw error otherwise
                        if (curWord.checkValid()) {
                            this.getWordCollection().insert(wId);
                        } else {
                            throw new Exception("Word ("
                                    + curWord.getLocalWord() + " : "
                                    + curWord.getValue()
                                    + ") is a malformed entry.");
                        }
                    } catch (Exception e) {
                        throw new SAXException();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassXID)) {
                    // insertion for word types is much simpler
                    try {
                        this.getTypeCollection().insert(wCId);
                    } catch (Exception e) {
                        throw new SAXException();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.genderXID)) {
                    try {
                        genderCollection.insert(wGId);
                    } catch (Exception e) {
                        throw new SAXException();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideXID)) {
                    pronuncMgr.addPronunciation(proBuffer);
                } else if (qName.equalsIgnoreCase(PGTUtil.wordGenderXID)) {
                    bgender = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionXID)) {
                    Integer relId = declensionMgr.getBufferRelId();

                    // dec templates handled differently than actual saved declensions for words
                    if (declensionMgr.isBufferDecTemp()) {
                        // old style declensions/conjugations all thrown into a single bucket by type
                        TypeNode parentType;
                        try {
                            parentType = core.getTypes().getNodeById(relId);
                        } catch (Exception e) {
                            throw new SAXException(e);
                        }

                        DeclensionNode head = core.getDeclensionManager().getDeclension(relId, relId);
                        String headerName = parentType.getValue() + " decl/conj";

                        if (head == null) {
                            head = new DeclensionNode(relId);
                            head.setNotes("Autogenerated from pre-0.7.5 version of PolyGlot.");
                            head.setValue(headerName);

                            try {
                                core.getDeclensionManager().addDeclensionTemplate(relId, head);
                            } catch (Exception e) {
                                throw new SAXException(e);
                            }
                        }

                        DeclensionDimension newDim = new DeclensionDimension(declensionMgr.getBuffer().getId());

                        newDim.setValue(declensionMgr.getBuffer().getValue());
                        newDim.setMandatory(declensionMgr.getBuffer().isMandatory());

                        head.addDimension(newDim);
                    } else {
                        // adding a declension to a word is easier than making a template...
                        declensionMgr.getBuffer().setCombinedDimId("," + declensionMgr.getBuffer().getId().toString() + ",");
                        declensionMgr.addDeclensionToWord(relId, declensionMgr.getBuffer().getId(), declensionMgr.getBuffer());
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.localWordXID)) {
                    blocalWord = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.conWordXID)) {
                    bconWord = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordPlurXID)) {
                    // plurality now a declension (as it should be)
                    // special position granted to plurals... fixes awful ID collision error
                    
                    // skip if empty
                    if (!declensionMgr.getBuffer().getValue().trim().equals("")) {
                        declensionMgr.getBuffer().setCombinedDimId("," + wId + "," + PGTUtil.wordPlurXID + ",");
                        declensionMgr.addDeclensionToWord(wId, Integer.MAX_VALUE, declensionMgr.getBuffer());
                    }
                    
                    declensionMgr.clearBuffer();
                    bwordPlur = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordTypeXID)) {
                    btype = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordIdXID)) {
                    bId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.definitionXID)) {
                    bdef = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontConXID)) {
                    bfontcon = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontLocalXID)) {
                    bfontlocal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassNameXID)) {
                    bwordClassName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassNotesXID)) {
                    bwordClassNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassPlurManXID)) {
                    bwordClassPlurMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassProcManXID)) {
                    bwordClassProcMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassGenderManXID)) {
                    bwordClassGenderMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassDefManXID)) {
                    bwordClassDefMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.pronunciationXID)) {
                    bpronuncation = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.genderIdXID)) {
                    bgenderId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.genderNameXID)) {
                    bgenderName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.genderNotesXID)) {
                    bgenderNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropLangNameXID)) {
                    blangName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropFontSizeXID)) {
                    bfontSize = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropFontStyleXID)) {
                    bfontStyle = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropAlphaOrderXID)) {
                    balphaOrder = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionIdXID)) {
                    bDecId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionTextXID)) {
                    bDecText = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionNotesXID)) {
                    bDecNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionIsTemplateXID)) {
                    bDecIsTemp = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionRelatedIdXID)) {
                    bDecRelId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionMandatoryXID)) {
                    bdeclensionMandatory = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideBaseXID)) {
                    bpronBase = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuidePhonXID)) {
                    bpronPhon = false;
                } /*else if (qName.equalsIgnoreCase(PGTUtil.proAutoPopXID)) {
                    // Removed as of 1.0
                    bproAutoPop = false;
                }*/  else if (qName.equalsIgnoreCase(PGTUtil.wordRuleOverrideXID)) { 
                    bwordRuleOverride = false;
                }
            }

            @Override
            public void characters(char ch[], int start, int length)
                    throws SAXException {

                if (blocalWord) {
                    this.getWordCollection().getBufferWord()
                            .setLocalWord(new String(ch, start, length));
                    blocalWord = false;
                } else if (bconWord) {
                    this.getWordCollection().getBufferWord()
                            .setValue(new String(ch, start, length));
                    bconWord = false;
                } else if (btype) {
                    this.getWordCollection().getBufferWord()
                            .setWordType(new String(ch, start, length));
                    btype = false;
                } else if (bId) {
                    wId = Integer.parseInt(new String(ch, start, length));
                    bId = false;
                } else if (bdef) {
                    this.getWordCollection().getBufferWord()
                            .setDefinition(new String(ch, start, length));
                    bdef = false;
                } else if (bwordPlur) {
                    // plurality now handled as declension
                    declensionMgr.setBufferDecTemp(false);
                    declensionMgr.setBufferDecText(new String(ch, start, length));
                    declensionMgr.setBufferDecNotes("Plural");
                    bwordPlur = false;
                } else if (bwordProcOverride) {
                    this.getWordCollection().getBufferWord()
                            .setProcOverride(new String(ch, start, length).equals("T"));
                    bwordProcOverride = false;
                } else if (bfontcon) {
                    propertiesManager.setFontCon(new Font(new String(ch, start, length), 0, 0));
                    bfontcon = false;
                } else if (bwordClassNotes) {
                    this.getTypeCollection().getBufferType()
                            .setNotes(new String(ch, start, length));
                    bwordClassNotes = false;
                } else if (bwordClassName) {
                    this.getTypeCollection().getBufferType()
                            .setValue(new String(ch, start, length));
                    bwordClassName = false;
                } else if (bwordClassId) {
                    wCId = Integer.parseInt(new String(ch, start, length));
                    bwordClassId = false;
                } else if (bpronuncation) {
                    wordCollection.getBufferWord().setPronunciation(
                            new String(ch, start, length));
                    bpronuncation = false;
                } else if (bwordRuleOverride) { 
                    wordCollection.getBufferWord().setRulesOverride(
                        new String(ch, start, length).equals("T"));
                    bwordRuleOverride = false;
                } else if (bgender) {
                    wordCollection.getBufferWord().setGender(
                            new String(ch, start, length));
                    bgender = false;
                } else if (bgenderId) {
                    wGId = Integer.parseInt(new String(ch, start, length));
                    bgenderId = false;
                } else if (bgenderName) {
                    genderCollection.getGenderBuffer().setValue(
                            new String(ch, start, length));
                    bgenderName = false;
                } else if (bgenderNotes) {
                    genderCollection.getGenderBuffer().setNotes(
                            new String(ch, start, length));
                    bgenderNotes = false;
                } else if (blangName) {
                    propertiesManager.setLangName(new String(ch, start, length));
                    blangName = false;
                } else if (bfontSize) {
                    propertiesManager.setFontSize(Integer.parseInt(new String(ch, start, length)));
                    bfontSize = false;
                } else if (bfontStyle) {
                    propertiesManager.setFontStyle(Integer.parseInt(new String(ch, start, length)));
                    bfontStyle = false;
                } else if (balphaOrder) {
                    propertiesManager.setAlphaOrder(new String(ch, start, length));
                    balphaOrder = false;
                } else if (bDecId) {
                    declensionMgr.setBufferId(Integer.parseInt(new String(ch, start, length)));
                    bDecId = false;
                } else if (bDecText) {
                    declensionMgr.setBufferDecText(new String(ch, start, length));
                    bDecText = false;
                } else if (bDecNotes) {
                    declensionMgr.setBufferDecNotes(new String(ch, start, length));
                    bDecNotes = false;
                } else if (bDecIsTemp) {
                    declensionMgr.setBufferDecTemp(new String(ch, start, length).equals("1"));
                    bDecIsTemp = false;
                } else if (bDecRelId) {
                    declensionMgr.setBufferRelId(Integer.parseInt(new String(ch, start, length)));
                    bDecRelId = false;
                } else if (bpronBase) {
                    proBuffer.setValue(new String(ch, start, length));
                    bpronBase = false;
                } else if (bpronPhon) {
                    proBuffer.setPronunciation(new String(ch, start, length));
                    bpronPhon = false;
                } /*else if (bproAutoPop) {
                    // Removed as of 1.0
                    propertiesManager.setProAutoPop((new String(ch, start, length).equalsIgnoreCase("T")));
                    bproAutoPop = false;
                }*/ else if (bwordClassProcMan) {
                    typeCollection.getBufferType().setProcMandatory(new String(ch, start, length).equals("T"));
                    bwordClassProcMan = false;
                } else if (bwordClassGenderMan) {
                    typeCollection.getBufferType().setGenderMandatory(new String(ch, start, length).equals("T"));
                    bwordClassGenderMan = false;
                } else if (bwordClassDefMan) {
                    typeCollection.getBufferType().setDefMandatory(new String(ch, start, length).equals("T"));
                    bwordClassDefMan = false;
                } else if (bdeclensionMandatory) {
                    declensionMgr.setBufferDecMandatory(new String(ch, start, length).equals("T"));
                    bdeclensionMandatory = false;
                } else if (blangPropLocalUniqueness) {
                    propertiesManager.setLocalUniqueness(new String(ch, start, length).equals("T"));
                    blangPropLocalUniqueness = false;
                } else if (blangPropWordUniqueness) {
                    propertiesManager.setWordUniqueness(new String(ch, start, length).equals("T"));
                    blangPropWordUniqueness = false;
                } else if (blangPropLocalMandatory) {
                    propertiesManager.setLocalMandatory(new String(ch, start, length).equals("T"));
                    blangPropLocalMandatory = false;
                } else if (blangPropTypeMandatory) {
                    propertiesManager.setTypesMandatory(new String(ch, start, length).equals("T"));
                    blangPropTypeMandatory = false;
                } 
            }
        };
    }

    private static CustHandler get075orHigherHandler(final DictCore core) {
        return new CustHandler() {

            PronunciationNode proBuffer;
            boolean blocalWord = false;
            boolean bconWord = false;
            boolean btype = false;
            boolean bId = false;
            boolean bdef = false;
            boolean bfontcon = false;
            boolean bfontlocal = false;
            boolean bwordClassName = false;
            boolean bwordClassId = false;
            boolean bwordClassNotes = false;
            boolean bwordClassGloss = false;
            boolean bpronuncation = false;
            boolean bgenderId = false;
            boolean bgenderNotes = false;
            boolean bgenderName = false;
            boolean bgender = false;
            boolean blangName = false;
            boolean bfontSize = false;
            boolean bfontStyle = false;
            boolean balphaOrder = false;
            boolean bDecId = false;
            boolean bDecText = false;
            boolean bDecNotes = false;
            boolean bDecIsTemp = false;
            boolean bDecRelId = false;
            boolean bDecCombId = false;
            boolean bpronBase = false;
            boolean bpronPhon = false;
            boolean bwordPlur = false;
            boolean blangPropTypeMandatory = false;
            boolean blangPropLocalMandatory = false;
            boolean blangPropWordUniqueness = false;
            boolean blangPropLocalUniqueness = false;
            boolean blangPropEnforceRTL = false;
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
            boolean bthesName = false;
            boolean bthesNotes = false;
            boolean bthesWord = false;
            boolean bignoreCase = false;
            boolean bdisableProcRegex = false;
            boolean bwordoverAutoDec = false;
            boolean bdecGenRuleComb = false;
            boolean bdecGenRuleName = false;
            boolean bdecGenRuleRegex = false;
            boolean bdecGenRuleType = false;
            boolean bdecGenTransRegex = false;
            boolean bdecGenTransRep = false;
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
            String loadLog = "";
            
            int wId;
            int wCId;
            int wGId;
            String combinedDecId = "";

            DeclensionManager declensionMgr = core.getDeclensionManager();
            GenderCollection genderCollection = core.getGenders();
            PronunciationMgr pronuncMgr = core.getPronunciationMgr();
            PropertiesManager propertiesManager = core.getPropertiesManager();
            ThesaurusManager thesMgr = core.getThesManager();

            @Override
            public void startElement(String uri, String localName,
                    String qName, Attributes attributes)
                    throws SAXException {

                // test to see whether this is the first node in a word
                if (qName.equalsIgnoreCase(PGTUtil.wordXID)) {
                    this.getWordCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideXID)) {
                    proBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(PGTUtil.localWordXID)) {
                    blocalWord = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.conWordXID)) {
                    bconWord = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordTypeXID)) {
                    btype = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordIdXID)) {
                    bId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.definitionXID)) {
                    bdef = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordPlurXID)) {
                    // plurality made into declension-deprecated from main screen
                    declensionMgr.clearBuffer();
                    bwordPlur = true;
                }  else if (qName.equalsIgnoreCase(PGTUtil.wordRuleOverrideXID)) {
                    bwordRuleOverride = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontConXID)) {
                    bfontcon = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontLocalXID)) {
                    bfontlocal = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassIdXID)) {
                    bwordClassId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassNameXID)) {
                    bwordClassName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassNotesXID)) {
                    bwordClassNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassGlossXID)) {
                    bwordClassGloss = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.pronunciationXID)) {
                    bpronuncation = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordGenderXID)) {
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
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropFontStyleXID)) {
                    bfontStyle = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropAlphaOrderXID)) {
                    balphaOrder = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.langPropEnforceRTLXID)) {
                    blangPropEnforceRTL = true;
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
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionRelatedIdXID)) {
                    bDecRelId = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideBaseXID)) {
                    bpronBase = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuidePhonXID)) {
                    bpronPhon = true;
                } /*else if (qName.equalsIgnoreCase(PGTUtil.proAutoPopXID)) {
                    // Removed as of 1.0
                    bproAutoPop = true;
                }*/ else if (qName.equalsIgnoreCase(PGTUtil.wordProcOverrideXID)) {
                    bwordProcOverride = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassPlurManXID)) {
                    bwordClassPlurMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassProcManXID)) {
                    bwordClassProcMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassGenderManXID)) {
                    bwordClassGenderMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassDefManXID)) {
                    bwordClassDefMan = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassPatternXID)) {
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
                } else if (qName.equalsIgnoreCase(PGTUtil.thesNameXID)) {
                    bthesName = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.thesNodeXID)) {
                    thesMgr.buildNewBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.thesNotesXID)) {
                    bthesNotes = true;
                } else if (qName.equalsIgnoreCase(PGTUtil.thesWordXID)) {
                    bthesWord = true;
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
                }
            }

            @Override
            public void endElement(String uri, String localName,
                    String qName) throws SAXException {

                // save word to word collection
                if (qName.equalsIgnoreCase(PGTUtil.wordXID)) {
                    ConWord curWord = this.getWordCollection()
                            .getBufferWord();

                    try {
                        // if word is valid, save. Throw error otherwise
                        if (curWord.checkValid()) {
                            this.getWordCollection().insert(wId);
                        } else {
                            throw new Exception("Word ("
                                    + curWord.getLocalWord() + " : "
                                    + curWord.getValue()
                                    + ") is a malformed entry.");
                        }
                    } catch (Exception e) {
                        throw new SAXException();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassXID)) {
                    // insertion for word types is much simpler
                    try {
                        this.getTypeCollection().insert(wCId);
                    } catch (Exception e) {
                        throw new SAXException();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.genderXID)) {
                    try {
                        genderCollection.insert(wGId);
                    } catch (Exception e) {
                        throw new SAXException();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.proGuideXID)) {
                    pronuncMgr.addPronunciation(proBuffer);
                } else if (qName.equalsIgnoreCase(PGTUtil.wordGenderXID)) {
                    bgender = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionXID)) {
                    // dec templates handled differently than actual saved declensions for words
                    if (declensionMgr.isBufferDecTemp()) {
                        declensionMgr.insertBuffer();
                        declensionMgr.clearBuffer();
                    } else {
                        Integer relId = declensionMgr.getBufferRelId();
                        declensionMgr.getBuffer().setCombinedDimId(declensionMgr.getBuffer().getCombinedDimId());
                        declensionMgr.addDeclensionToWord(relId, declensionMgr.getBuffer().getId(), declensionMgr.getBuffer());
                        declensionMgr.clearBuffer();
                    }
                } else if (qName.equalsIgnoreCase(PGTUtil.localWordXID)) {
                    blocalWord = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.conWordXID)) {
                    bconWord = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordPlurXID)) {
                    // plurality now a declension (as it should be)
                    // special position granted to plurals... fixes awful ID collision error

                    // skip insertion of empty
                    if (!declensionMgr.getBuffer().getValue().trim().equals("")) {
                        declensionMgr.getBuffer().setCombinedDimId("," + wId + "," + PGTUtil.wordPlurXID + ",");
                        declensionMgr.addDeclensionToWord(wId, Integer.MAX_VALUE, declensionMgr.getBuffer());
                    }

                    declensionMgr.clearBuffer();
                    bwordPlur = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordTypeXID)) {
                    btype = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordIdXID)) {
                    bId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordRuleOverrideXID)) {
                    bwordRuleOverride = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.definitionXID)) {
                    bdef = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontConXID)) {
                    bfontcon = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.fontLocalXID)) {
                    bfontlocal = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassNameXID)) {
                    bwordClassName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassNotesXID)) {
                    bwordClassNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassPlurManXID)) {
                    bwordClassPlurMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassProcManXID)) {
                    bwordClassProcMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassGenderManXID)) {
                    bwordClassGenderMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassDefManXID)) {
                    bwordClassDefMan = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassPatternXID)) {
                    bwordClassPattern = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.wordClassGlossXID)) {
                    bwordClassGloss = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.pronunciationXID)) {
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
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionIdXID)) {
                    bDecId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionTextXID)) {
                    bDecText = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionNotesXID)) {
                    bDecNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.declensionIsTemplateXID)) {
                    bDecIsTemp = false;
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
                } /*else if (qName.equalsIgnoreCase(PGTUtil.proAutoPopXID)) {
                    // Removed as of 1.0
                    bproAutoPop = false;
                }*/ else if (qName.equalsIgnoreCase(PGTUtil.dimensionNodeXID)) {
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
                } else if (qName.equalsIgnoreCase(PGTUtil.thesNameXID)) {
                    bthesName = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.thesNodeXID)) {
                    thesMgr.bufferDone();
                } else if (qName.equalsIgnoreCase(PGTUtil.thesNotesXID)) {
                    bthesNotes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.thesWordXID)) {
                    bthesWord = false;
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
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenRuleTypeXID)) {
                    bdecGenRuleType = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenTransXID)) {
                    core.getDeclensionManager().getRuleBuffer().insertTransBuffer();
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenTransRegexXID)) {
                    bdecGenTransRegex = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decGenTransReplaceXID)) {
                    bdecGenTransRep = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decCombinedIdXID)) {
                    bcombinedFormId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decCombinedSurpressXID)) {
                    bcombinedFormSurpress = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.decCombinedFormXID)) {
                    combinedDecId = "";
                } else if (qName.equalsIgnoreCase(PGTUtil.logoStrokesXID)) {
                    blogoStrokes = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.logoNotesXID)) {
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
                        loadLog += "\nLogograph load error: " + e.getLocalizedMessage();
                    }
                    core.getLogoCollection().clear();
                } else if (qName.equalsIgnoreCase(PGTUtil.logoWordRelationXID)) {
                    blogoWordRelation = false;
                }  else if (qName.equalsIgnoreCase(PGTUtil.grammarChapterNodeXID)) {
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
                }  else if (qName.equalsIgnoreCase(PGTUtil.grammarSectionRecordingXID)) {
                    bgrammarSecRecId = false;
                } else if (qName.equalsIgnoreCase(PGTUtil.grammarSectionTextXID)) {
                    bgrammarSecText = true;
                }
            }

            @Override
            public void characters(char ch[], int start, int length)
                    throws SAXException {

                if (blocalWord) {
                    ConWord bufferWord = this.getWordCollection().getBufferWord();
                    bufferWord.setLocalWord(bufferWord.getLocalWord() 
                            + new String(ch, start, length));
                } else if (bconWord) {
                    ConWord bufferWord = this.getWordCollection().getBufferWord();
                    bufferWord.setValue(bufferWord.getValue()
                            + new String(ch, start, length));
                } else if (btype) {
                    ConWord bufferWord = this.getWordCollection().getBufferWord();
                    bufferWord.setWordType(bufferWord.getWordType()
                            + new String(ch, start, length));
                } else if (bId) {
                    wId = Integer.parseInt(new String(ch, start, length));
                } else if (bdef) {
                    ConWord bufferWord = this.getWordCollection().getBufferWord();
                    bufferWord.setDefinition(bufferWord.getDefinition()
                            + new String(ch, start, length));
                } else if (bwordPlur) {
                    // plurality now handled as declension
                    declensionMgr.setBufferDecTemp(false);
                    declensionMgr.setBufferDecText(new String(ch, start, length));
                    declensionMgr.setBufferDecNotes("Plural");
                    bwordPlur = false;
                } else if (bwordProcOverride) {
                    this.getWordCollection().getBufferWord()
                            .setProcOverride(new String(ch, start, length).equals("T"));
                    bwordProcOverride = false;
                } else if (bwordRuleOverride) {
                    wordCollection.getBufferWord()
                            .setRulesOverride(new String(ch, start, length).equals("T"));
                    bwordRuleOverride = false;
                } else if (bwordoverAutoDec) {
                    this.getWordCollection().getBufferWord()
                            .setOverrideAutoDeclen(new String(ch, start, length).equals("T"));
                    bwordoverAutoDec = false;
                } else if (bfontcon) {
                    propertiesManager.setFontCon(new Font(new String(ch, start, length), 0, 0));
                    bfontcon = false;
                } else if (bwordClassNotes) {
                    TypeNode bufferType = this.getTypeCollection().getBufferType();
                    bufferType.setNotes(bufferType.getNotes()
                            + new String(ch, start, length));
                } else if (bwordClassName) {
                    TypeNode bufferType = this.getTypeCollection().getBufferType();
                    bufferType.setValue(bufferType.getValue()
                            + new String(ch, start, length));
                } else if (bwordClassPattern) {
                    TypeNode bufferType = this.getTypeCollection().getBufferType();
                    bufferType.setPattern(bufferType.getPattern()
                            + new String(ch, start, length));
                } else if (bwordClassGloss) {
                    TypeNode bufferType = this.getTypeCollection().getBufferType();
                    bufferType.setGloss(bufferType.getGloss()
                            +  new String(ch, start, length));
                } else if (bwordClassId) {
                    wCId = Integer.parseInt(new String(ch, start, length));
                    bwordClassId = false;
                } else if (bpronuncation) {
                    ConWord bufferWord = this.getWordCollection().getBufferWord();
                    bufferWord.setPronunciation(bufferWord.getPronunciation()
                            + new String(ch, start, length));
                } else if (bgender) {
                    ConWord bufferWord = this.getWordCollection().getBufferWord();
                    bufferWord.setGender(bufferWord.getGender()
                            + new String(ch, start, length));
                } else if (bgenderId) {
                    wGId = Integer.parseInt(new String(ch, start, length));
                    bgenderId = false;
                } else if (bgenderName) {
                    GenderNode genderBuffer = genderCollection.getGenderBuffer();
                    genderBuffer.setValue(genderBuffer.getValue()
                            + new String(ch, start, length));
                } else if (bgenderNotes) {
                    GenderNode genderBuffer = genderCollection.getGenderBuffer();
                    genderBuffer.setNotes(genderBuffer.getNotes()
                            + new String(ch, start, length));
                } else if (blangName) {
                    propertiesManager.setLangName(propertiesManager.getLangName()
                            +new String(ch, start, length));
                } else if (bfontSize) {
                    propertiesManager.setFontSize(Integer.parseInt(new String(ch, start, length)));
                    bfontSize = false;
                } else if (bfontStyle) {
                    propertiesManager.setFontStyle(Integer.parseInt(new String(ch, start, length)));
                    bfontStyle = false;
                } else if (balphaOrder) {
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
                } else if (bDecCombId) {
                    declensionMgr.getBuffer().setCombinedDimId(new String(ch, start, length));
                    bDecIsTemp = false;
                } else if (bDecRelId) {
                    declensionMgr.setBufferRelId(Integer.parseInt(new String(ch, start, length)));
                    bDecRelId = false;
                } else if (bpronBase) {
                    proBuffer.setValue(proBuffer.getValue()
                            +new String(ch, start, length));
                } else if (bpronPhon) {
                    proBuffer.setPronunciation(proBuffer.getPronunciation()
                            + new String(ch, start, length));
                } /*else if (bproAutoPop) {
                    // Removed as of 1.0
                    propertiesManager.setProAutoPop((new String(ch, start, length).equalsIgnoreCase("T")));
                    bproAutoPop = false;
                }*/ else if (bwordClassProcMan) {
                    typeCollection.getBufferType().setProcMandatory(new String(ch, start, length).equals("T"));
                    bwordClassProcMan = false;
                } else if (bwordClassGenderMan) {
                    typeCollection.getBufferType().setGenderMandatory(new String(ch, start, length).equals("T"));
                    bwordClassGenderMan = false;
                } else if (bwordClassDefMan) {
                    typeCollection.getBufferType().setDefMandatory(new String(ch, start, length).equals("T"));
                    bwordClassDefMan = false;
                } else if (bdeclensionMandatory) {
                    declensionMgr.setBufferDecMandatory(new String(ch, start, length).equals("T"));
                    bdeclensionMandatory = false;
                } else if (blangPropLocalUniqueness) {
                    propertiesManager.setLocalUniqueness(new String(ch, start, length).equals("T"));
                    blangPropLocalUniqueness = false;
                } else if (blangPropWordUniqueness) {
                    propertiesManager.setWordUniqueness(new String(ch, start, length).equals("T"));
                    blangPropWordUniqueness = false;
                } else if (blangPropLocalMandatory) {
                    propertiesManager.setLocalMandatory(new String(ch, start, length).equals("T"));
                    blangPropLocalMandatory = false;
                } else if (blangPropTypeMandatory) {
                    propertiesManager.setTypesMandatory(new String(ch, start, length).equals("T"));
                    blangPropTypeMandatory = false;
                } else if (blangPropEnforceRTL) {
                    propertiesManager.setEnforceRTL(new String(ch, start, length).equals("T"));
                    blangPropEnforceRTL = false;
                } else if (bdimMand) {
                    declensionMgr.getBuffer().getBuffer().setMandatory(new String(ch, start, length).equals("T"));
                    bdimMand = false;
                } else if (bdimId) {
                    declensionMgr.getBuffer().getBuffer().setId(Integer.parseInt(new String(ch, start, length)));
                    bdimId = false;
                } else if (bdimName) {
                    DeclensionDimension dimBuffer = declensionMgr.getBuffer().getBuffer();
                    dimBuffer.setValue(dimBuffer.getValue()
                            +new String(ch, start, length));
                } else if (bthesName) {
                    ThesNode thesBuffer = thesMgr.getBuffer();
                    thesBuffer.setValue(thesBuffer.getValue()
                            + new String(ch, start, length));
                } else if (bthesNotes) {
                    thesMgr.getBuffer().setNotes(new String(ch, start, length));
                    bthesNotes = false;
                } else if (bthesWord) {
                    try {
                        thesMgr.getBuffer().addWord(core.getWordCollection().getNodeById(
                                Integer.parseInt(new String(ch, start, length))));
                    } catch (Exception e) {
                        loadLog += "\nThesaurus load error: " + e.getLocalizedMessage();
                    }
                    bthesWord = false;
                } else if (bignoreCase) {
                    core.getPropertiesManager().setIgnoreCase(new String(ch, start, length).equals("T"));
                    bignoreCase = false;
                } else if (bdisableProcRegex) {
                    core.getPropertiesManager().setDisableProcRegex(new String(ch, start, length).equals("T"));
                    bdisableProcRegex = false;
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
                } else if (bcombinedFormId) {
                    combinedDecId += new String(ch, start, length);
                } else if (bcombinedFormSurpress) {
                    core.getDeclensionManager().setCombinedDeclSurpressed(combinedDecId,
                            new String(ch, start, length).equals("T"));
                } else if (blogoStrokes) {
                    try {
                        core.getLogoCollection().getBufferNode().setStrokes(Integer.parseInt(new String(ch, start, length)));
                    } catch (Exception e) {
                        loadLog += "\nLogograph load error: " +e.getLocalizedMessage();
                    }
                } else if (blogoNotes) {
                    LogoNode curNode = core.getLogoCollection().getBufferNode();
                    curNode.setNotes(curNode.getNotes() + new String(ch, start, length));
                } else if (blogoRadical) {
                    core.getLogoCollection().getBufferNode().setRadical(
                        new String(ch, start, length).equals("T"));
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
                    } catch (Exception e) {
                        loadLog += "\nLogograph load error: " +e.getLocalizedMessage();
                    }
                } else if (blogoWordRelation) {
                    try {
                        core.getLogoCollection().loadLogoRelations(new String(ch, start, length));
                    } catch (Exception e) {
                        loadLog += "\nLogograph relation load error: " + e.getLocalizedMessage();
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
                }
            }
        };
    }
}
