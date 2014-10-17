/*
 * Copyright (c) 2014, Draque Thompson
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
        versionNode = doc.getDocumentElement().getElementsByTagName(XMLIDs.pgVersionXID).item(0);
        versionNumber = versionNode == null ? "0" : versionNode.getTextContent();

        // switch not used to maintain Java 6 compatibility... fucking 6. X(
        if (versionNumber.equals("0")
                || versionNumber.equals("0.5")
                || versionNumber.equals("0.5.1")
                || versionNumber.equals("0.6")
                || versionNumber.equals("0.6.1")
                || versionNumber.equals("0.6.5")
                || versionNumber.equals("0.7")) {
            ret = CustHandlerFactory.get7orLowerHandler(core);
        } else if (versionNumber.equals("0.7.5")
                || versionNumber.equals("0.7.6")
                || versionNumber.equals("0.7.6.1")
                || versionNumber.equals("0.8")
                || versionNumber.equals("0.8.1")
                || versionNumber.equals("0.8.1.1")
                || versionNumber.equals("0.8.1.2")) {
            ret = CustHandlerFactory.get075Handler(core);
        } else {
            throw new Exception("Please upgrade PolyGlot. The PGD file you are loading was "
                    + "written with an unsupported version: Ver " + versionNumber + ".");
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
            boolean bproAutoPop = false;
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
                if (qName.equalsIgnoreCase(XMLIDs.wordXID)) {
                    this.getWordCollection().clear();
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuideXID)) {
                    proBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(XMLIDs.localWordXID)) {
                    blocalWord = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.conWordXID)) {
                    bconWord = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordTypeXID)) {
                    btype = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordIdXID)) {
                    bId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.definitionXID)) {
                    bdef = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordPlurXID)) {
                    bwordPlur = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.fontConXID)) {
                    bfontcon = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.fontLocalXID)) {
                    bfontlocal = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassIdXID)) {
                    bwordClassId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassNameXID)) {
                    bwordClassName = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassNotesXID)) {
                    bwordClassNotes = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.pronunciationXID)) {
                    bpronuncation = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordGenderXID)) {
                    bgender = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderIdXID)) {
                    bgenderId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderNameXID)) {
                    bgenderName = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderNotesXID)) {
                    bgenderNotes = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropLangNameXID)) {
                    blangName = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropFontSizeXID)) {
                    bfontSize = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropFontStyleXID)) {
                    bfontStyle = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropAlphaOrderXID)) {
                    balphaOrder = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionXID)) {
                    // from old versions, declensions are loaded as dimensions of a master declension
                    declensionMgr.getBuffer().clearBuffer();
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionIdXID)) {
                    bDecId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionTextXID)) {
                    bDecText = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionNotesXID)) {
                    bDecNotes = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionIsTemplateXID)) {
                    bDecIsTemp = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionRelatedIdXID)) {
                    bDecRelId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuideBaseXID)) {
                    bpronBase = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuidePhonXID)) {
                    bpronPhon = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.proAutoPopXID)) {
                    bproAutoPop = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordProcOverrideXID)) {
                    bwordProcOverride = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassPlurManXID)) {
                    bwordClassPlurMan = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassProcManXID)) {
                    bwordClassProcMan = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassGenderManXID)) {
                    bwordClassGenderMan = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassDefManXID)) {
                    bwordClassDefMan = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionMandatoryXID)) {
                    bdeclensionMandatory = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropLocalUniquenessXID)) {
                    blangPropLocalUniqueness = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropWordUniquenessXID)) {
                    blangPropWordUniqueness = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropLocalMandatoryXID)) {
                    blangPropLocalMandatory = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropTypeMandatoryXID)) {
                    blangPropTypeMandatory = true;
                }
            }

            @Override
            public void endElement(String uri, String localName,
                    String qName) throws SAXException {

                // save word to word collection
                if (qName.equalsIgnoreCase(XMLIDs.wordXID)) {
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
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassXID)) {
                    // insertion for word types is much simpler
                    try {
                        this.getTypeCollection().insert(wCId);
                    } catch (Exception e) {
                        throw new SAXException();
                    }
                } else if (qName.equalsIgnoreCase(XMLIDs.genderXID)) {
                    try {
                        genderCollection.insert(wGId);
                    } catch (Exception e) {
                        throw new SAXException();
                    }
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuideXID)) {
                    pronuncMgr.addPronunciation(proBuffer);
                } else if (qName.equalsIgnoreCase(XMLIDs.wordGenderXID)) {
                    bgender = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionXID)) {
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
                } else if (qName.equalsIgnoreCase(XMLIDs.localWordXID)) {
                    blocalWord = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.conWordXID)) {
                    bconWord = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordPlurXID)) {
                    bwordPlur = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordTypeXID)) {
                    btype = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordIdXID)) {
                    bId = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.definitionXID)) {
                    bdef = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.fontConXID)) {
                    bfontcon = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.fontLocalXID)) {
                    bfontlocal = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassNameXID)) {
                    bwordClassName = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassNotesXID)) {
                    bwordClassNotes = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassPlurManXID)) {
                    bwordClassPlurMan = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassProcManXID)) {
                    bwordClassProcMan = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassGenderManXID)) {
                    bwordClassGenderMan = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassDefManXID)) {
                    bwordClassDefMan = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.pronunciationXID)) {
                    bpronuncation = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderIdXID)) {
                    bgenderId = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderNameXID)) {
                    bgenderName = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderNotesXID)) {
                    bgenderNotes = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropLangNameXID)) {
                    blangName = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropFontSizeXID)) {
                    bfontSize = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropFontStyleXID)) {
                    bfontStyle = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropAlphaOrderXID)) {
                    balphaOrder = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionIdXID)) {
                    bDecId = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionTextXID)) {
                    bDecText = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionNotesXID)) {
                    bDecNotes = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionIsTemplateXID)) {
                    bDecIsTemp = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionRelatedIdXID)) {
                    bDecRelId = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionMandatoryXID)) {
                    bdeclensionMandatory = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuideBaseXID)) {
                    bpronBase = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuidePhonXID)) {
                    bpronPhon = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.proAutoPopXID)) {
                    bproAutoPop = false;
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
                    this.getWordCollection().getBufferWord()
                            .setPlural(new String(ch, start, length));
                    bdef = false;
                } else if (bwordProcOverride) {
                    this.getWordCollection().getBufferWord()
                            .setProcOverride(new String(ch, start, length).equals("T"));
                    bwordProcOverride = false;
                } else if (bfontcon) {
                    propertiesManager.setFontCon(new Font(new String(ch, start, length), 0, 0));
                    propertiesManager.setFontName(new String(ch, start, length));
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
                } else if (bproAutoPop) {
                    propertiesManager.setProAutoPop((new String(ch, start, length).equalsIgnoreCase("T")));
                    bproAutoPop = false;
                } else if (bwordClassPlurMan) {
                    typeCollection.getBufferType().setPluralMandatory(new String(ch, start, length).equals("T"));
                    bwordClassPlurMan = false;
                } else if (bwordClassProcMan) {
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

    private static CustHandler get075Handler(final DictCore core) {
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
            boolean bDecCombId = false;
            boolean bpronBase = false;
            boolean bpronPhon = false;
            boolean bproAutoPop = false;
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
            boolean bdimNode = false;
            boolean bdimId = false;
            boolean bdimMand = false;
            boolean bdimName = false;
            boolean bthesName = false;
            boolean bthesNotes = false;
            boolean bthesWord = false;

            int wId;
            int wCId;
            int wGId;

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
                if (qName.equalsIgnoreCase(XMLIDs.wordXID)) {
                    this.getWordCollection().clear();
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuideXID)) {
                    proBuffer = new PronunciationNode();
                } else if (qName.equalsIgnoreCase(XMLIDs.localWordXID)) {
                    blocalWord = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.conWordXID)) {
                    bconWord = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordTypeXID)) {
                    btype = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordIdXID)) {
                    bId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.definitionXID)) {
                    bdef = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordPlurXID)) {
                    bwordPlur = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.fontConXID)) {
                    bfontcon = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.fontLocalXID)) {
                    bfontlocal = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassIdXID)) {
                    bwordClassId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassNameXID)) {
                    bwordClassName = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassNotesXID)) {
                    bwordClassNotes = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.pronunciationXID)) {
                    bpronuncation = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordGenderXID)) {
                    bgender = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderIdXID)) {
                    bgenderId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderNameXID)) {
                    bgenderName = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderNotesXID)) {
                    bgenderNotes = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropLangNameXID)) {
                    blangName = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropFontSizeXID)) {
                    bfontSize = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropFontStyleXID)) {
                    bfontStyle = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropAlphaOrderXID)) {
                    balphaOrder = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionXID)) {
                    // from old versions, declensions are loaded as dimensions of a master declension
                    declensionMgr.getBuffer().clearBuffer();
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionIdXID)) {
                    bDecId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionTextXID)) {
                    bDecText = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionNotesXID)) {
                    bDecNotes = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionIsTemplateXID)) {
                    bDecIsTemp = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionRelatedIdXID)) {
                    bDecRelId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuideBaseXID)) {
                    bpronBase = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuidePhonXID)) {
                    bpronPhon = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.proAutoPopXID)) {
                    bproAutoPop = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordProcOverrideXID)) {
                    bwordProcOverride = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassPlurManXID)) {
                    bwordClassPlurMan = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassProcManXID)) {
                    bwordClassProcMan = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassGenderManXID)) {
                    bwordClassGenderMan = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassDefManXID)) {
                    bwordClassDefMan = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionMandatoryXID)) {
                    bdeclensionMandatory = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropLocalUniquenessXID)) {
                    blangPropLocalUniqueness = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropWordUniquenessXID)) {
                    blangPropWordUniqueness = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropLocalMandatoryXID)) {
                    blangPropLocalMandatory = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropTypeMandatoryXID)) {
                    blangPropTypeMandatory = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.dimensionNodeXID)) {
                    bdimNode = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.dimensionIdXID)) {
                    bdimId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.dimensionMandXID)) {
                    bdimMand = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.dimensionNameXID)) {
                    bdimName = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionComDimIdXID)) {
                    bDecCombId = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.thesNameXID)) {
                    bthesName = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.thesNodeXID)) {
                    thesMgr.buildNewBuffer();
                } else if (qName.equalsIgnoreCase(XMLIDs.thesNotesXID)) {
                    bthesNotes = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.thesWordXID)) {
                    bthesWord = true;
                }
            }

            @Override
            public void endElement(String uri, String localName,
                    String qName) throws SAXException {

                // save word to word collection
                if (qName.equalsIgnoreCase(XMLIDs.wordXID)) {
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
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassXID)) {
                    // insertion for word types is much simpler
                    try {
                        this.getTypeCollection().insert(wCId);
                    } catch (Exception e) {
                        throw new SAXException();
                    }
                } else if (qName.equalsIgnoreCase(XMLIDs.genderXID)) {
                    try {
                        genderCollection.insert(wGId);
                    } catch (Exception e) {
                        throw new SAXException();
                    }
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuideXID)) {
                    pronuncMgr.addPronunciation(proBuffer);
                } else if (qName.equalsIgnoreCase(XMLIDs.wordGenderXID)) {
                    bgender = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionXID)) {
                    // dec templates handled differently than actual saved declensions for words
                    if (declensionMgr.isBufferDecTemp()) {
                        declensionMgr.insertBuffer();
                        declensionMgr.clearBuffer();
                    } else {
                        Integer relId = declensionMgr.getBufferRelId();
                        declensionMgr.getBuffer().setCombinedDimId(declensionMgr.getBuffer().getCombinedDimId());
                        declensionMgr.addDeclensionToWord(relId, declensionMgr.getBuffer().getId(), declensionMgr.getBuffer());
                    }
                } else if (qName.equalsIgnoreCase(XMLIDs.localWordXID)) {
                    blocalWord = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.conWordXID)) {
                    bconWord = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordPlurXID)) {
                    bwordPlur = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordTypeXID)) {
                    btype = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordIdXID)) {
                    bId = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.definitionXID)) {
                    bdef = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.fontConXID)) {
                    bfontcon = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.fontLocalXID)) {
                    bfontlocal = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassNameXID)) {
                    bwordClassName = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassNotesXID)) {
                    bwordClassNotes = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassPlurManXID)) {
                    bwordClassPlurMan = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassProcManXID)) {
                    bwordClassProcMan = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassGenderManXID)) {
                    bwordClassGenderMan = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.wordClassDefManXID)) {
                    bwordClassDefMan = true;
                } else if (qName.equalsIgnoreCase(XMLIDs.pronunciationXID)) {
                    bpronuncation = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderIdXID)) {
                    bgenderId = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderNameXID)) {
                    bgenderName = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.genderNotesXID)) {
                    bgenderNotes = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropLangNameXID)) {
                    blangName = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropFontSizeXID)) {
                    bfontSize = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropFontStyleXID)) {
                    bfontStyle = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.langPropAlphaOrderXID)) {
                    balphaOrder = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionIdXID)) {
                    bDecId = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionTextXID)) {
                    bDecText = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionNotesXID)) {
                    bDecNotes = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionIsTemplateXID)) {
                    bDecIsTemp = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionRelatedIdXID)) {
                    bDecRelId = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionMandatoryXID)) {
                    bdeclensionMandatory = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.declensionComDimIdXID)) {
                    bDecCombId = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuideBaseXID)) {
                    bpronBase = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.proGuidePhonXID)) {
                    bpronPhon = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.proAutoPopXID)) {
                    bproAutoPop = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.dimensionNodeXID)) {
                    try {
                        declensionMgr.getBuffer().insertBuffer();
                        declensionMgr.getBuffer().clearBuffer();
                    } catch (Exception e) {
                        throw new SAXException(e);
                    }
                    bdimNode = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.dimensionIdXID)) {
                    bdimId = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.dimensionMandXID)) {
                    bdimMand = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.dimensionNameXID)) {
                    bdimName = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.thesNameXID)) {
                    bthesName = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.thesNodeXID)) {
                     thesMgr.bufferDone();
                } else if (qName.equalsIgnoreCase(XMLIDs.thesNotesXID)) {
                    bthesNotes = false;
                } else if (qName.equalsIgnoreCase(XMLIDs.thesWordXID)) {
                    bthesWord = false;
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
                    this.getWordCollection().getBufferWord()
                            .setPlural(new String(ch, start, length));
                    bdef = false;
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
                } else if (bDecCombId) {
                    declensionMgr.getBuffer().setCombinedDimId(new String(ch, start, length));
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
                } else if (bproAutoPop) {
                    propertiesManager.setProAutoPop((new String(ch, start, length).equalsIgnoreCase("T")));
                    bproAutoPop = false;
                } else if (bwordClassPlurMan) {
                    typeCollection.getBufferType().setPluralMandatory(new String(ch, start, length).equals("T"));
                    bwordClassPlurMan = false;
                } else if (bwordClassProcMan) {
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
                } else if (bdimMand) {
                    declensionMgr.getBuffer().getBuffer().setMandatory(new String(ch, start, length).equals("T"));
                    bdimMand = false;
                } else if (bdimId) {
                    declensionMgr.getBuffer().getBuffer().setId(Integer.parseInt(new String(ch, start, length)));
                    bdimId = false;
                } else if (bdimName) {
                    declensionMgr.getBuffer().getBuffer().setValue(new String(ch, start, length));
                    bdimName = false;
                } else if (bthesName) {
                    thesMgr.getBuffer().setValue(new String(ch, start, length));
                    bthesName = false;
                } else if (bthesNotes) {
                    thesMgr.getBuffer().setNotes(new String(ch, start, length));
                    bthesNotes = false;
                } else if (bthesWord) {
                    try {
                        thesMgr.getBuffer().addWord(core.getWordById(
                            Integer.parseInt(new String(ch, start, length))));
                    } catch (Exception e) {
                        // I really shouldn't have made the word search return error on not found...
                    }
                    bthesWord = false;
                }
            }
        };
    }
}
