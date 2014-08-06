/*
 * Copyright (c) 2014, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under:
 * Creative Commons Attribution-NonCommercial 4.0 International Public License
 * 
 * Please see the included LICENSE.TXT file for the full text of this license.
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

// TODO: finish test suites for classes...
package PolyGlot;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DictCore {
    private final String version = "0.7";
    private final ConWordCollection wordCollection = new ConWordCollection(this);
    private final TypeCollection typeCollection = new TypeCollection();
    private final GenderCollection genderCollection = new GenderCollection();
    private final DeclensionManager declensionMgr = new DeclensionManager();
    private final PropertiesManager propertiesManager = new PropertiesManager();
    private final PronunciationMgr pronuncMgr = new PronunciationMgr();

    public PropertiesManager getPropertiesManager() {
        return propertiesManager;
    }
    
    public String getVersion() {
        return version;
    }
    
    public ConWordCollection getWordCollection() {
        return wordCollection;
    }

    public void setPronunciations(List<PronunciationNode> _pronunciations) {
        pronuncMgr.setPronunciations(_pronunciations);
    }

    public Iterator<PronunciationNode> getPronunciations() {
        return pronuncMgr.getPronunciations();
    }

    public void deletePronunciation(PronunciationNode remove) {
        pronuncMgr.deletePronunciation(remove);
    }

    public void moveProcUp(PronunciationNode node) {
        pronuncMgr.moveProcUp(node);
    }

    public void moveProcDown(PronunciationNode node) {
        pronuncMgr.moveProcDown(node);
    }

    /**
     * Returns pronunciation of a given word
     *
     * @param base word to find pronunciation of
     * @return pronunciation string. If no perfect match found, empty string
     * returned
     */
    public String getPronunciation(String base) {
        return pronuncMgr.getPronunciation(base);
    }
    
    // TODO: make way to find all words that have no valid pronunciations
    
    /**
     * Returns pronunciation elements of word
     * @param base word to find pronunciation elements of
     * @return elements of pronunciation for word. Empty if no perfect match
     * found
     */
    public List<PronunciationNode> getPronunciationElements(String base) {
        return pronuncMgr.getPronunciationElements(base);
    }
    
    /**
     * Builds a report on the conlang. Potentially very computationally 
     * expensive.
     * @return String formatted report
     */
    public String buildLanguageReport() {
        String ret = "<center>---LANGUAGE STAT REPORT---</center><br><br>";
    
        ret += propertiesManager.buildPropertiesReport();
        
        ret += wordCollection.buildWordReport();
        
        return ret;
    }
    
    /**
     * recalculates all non-overridden pronunciations
     * @throws java.lang.Exception
     */
    public void recalcAllProcs() throws Exception {
        wordCollection.recalcAllProcs();
    }
    
    /**
     * Gets conlang's Font (minimizing display class use in core, but this is
     * just too common of a function to handle case by case
     * @return 
     */
    public Font getLangFont() {
        Font ret;

        String fontCon = getFontCon();

        if (!(fontCon.equals(""))) {
            int size = getFontSize();

            // if size = 0 default to 12
            if (size == 0) {
                size = 12;
            }

            // Unrecognized fonts return as OS default font, warning error thrown at time of file load
            ret = new Font(fontCon, getFontStyle(), size);
        } else {
            // set font to standard if no font found
            ret = new JTextField().getFont();
        }

        return ret;
    }

    public DictCore() {
        Map alphaOrder = propertiesManager.getAlphaOrder();

        wordCollection.setAlphaOrder(alphaOrder);
        typeCollection.setAlphaOrder(alphaOrder);
        genderCollection.setAlphaOrder(alphaOrder);
    }

    /**
     * Reads from given file
     *
     * @param _fileName filename to read from
     * @throws Exception
     */
    public void readFile(String _fileName) throws Exception {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            CustHandler handler;
            handler = new CustHandler() {
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
                        declensionMgr.clearBuffer();
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
                        declensionMgr.insertDeclension();
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
                        propertiesManager.setFontCon(new String(ch, start, length));
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

            handler.setWordCollection(wordCollection);
            handler.setTypeCollection(typeCollection);

            saxParser.parse(_fileName, handler);
        } catch (ParserConfigurationException e) {
            throw new Exception(e.getMessage());
        } catch (SAXException e) {
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

    }

    /**
     * Writes to given file
     *
     * @param _fileName filename to write to
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerException
     */
    // TODO: segment XML generation into respective collection managers
    public void writeFile(String _fileName)
            throws ParserConfigurationException, TransformerException {
        Iterator<ConWord> wordLoop = wordCollection.getNodeIterator();
        Iterator<TypeNode> typeLoop = typeCollection.getNodeIterator();
        Iterator<GenderNode> genderLoop = genderCollection.getNodeIterator();
        ConWord curWord;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Element wordNode;
        Element wordValue;

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("dictionary");
        doc.appendChild(rootElement);

        // store version of PolyGlot
        // store font for Conlang words
        wordValue = doc.createElement(XMLIDs.pgVersionXID);
        wordValue.appendChild(doc.createTextNode(version));
        rootElement.appendChild(wordValue);

        // store font for Conlang words
        wordValue = doc.createElement(XMLIDs.fontConXID);
        wordValue.appendChild(doc.createTextNode(propertiesManager.getFontCon()));
        rootElement.appendChild(wordValue);

        // store font style
        wordValue = doc.createElement(XMLIDs.langPropFontStyleXID);
        wordValue.appendChild(doc.createTextNode(propertiesManager.getFontStyle().toString()));
        rootElement.appendChild(wordValue);

        // store font for Local words
        wordValue = doc.createElement(XMLIDs.langPropFontSizeXID);
        wordValue.appendChild(doc.createTextNode(propertiesManager.getFontSize().toString()));
        rootElement.appendChild(wordValue);

        // store name for conlang
        wordValue = doc.createElement(XMLIDs.langPropLangNameXID);
        wordValue.appendChild(doc.createTextNode(propertiesManager.getLangName()));
        rootElement.appendChild(wordValue);

        // store alpha order for conlang
        wordValue = doc.createElement(XMLIDs.langPropAlphaOrderXID);
        wordValue.appendChild(doc.createTextNode(propertiesManager.getAlphaPlainText()));
        rootElement.appendChild(wordValue);

        // store option to autopopulate pronunciations
        wordValue = doc.createElement(XMLIDs.proAutoPopXID);
        wordValue.appendChild(doc.createTextNode(propertiesManager.isProAutoPop() ? "T" : "F"));
        rootElement.appendChild(wordValue);

        // store option for mandatory Types
        wordValue = doc.createElement(XMLIDs.langPropTypeMandatoryXID);
        wordValue.appendChild(doc.createTextNode(propertiesManager.isTypesMandatory() ? "T" : "F"));
        rootElement.appendChild(wordValue);

        // store option for mandatory Local word
        wordValue = doc.createElement(XMLIDs.langPropLocalMandatoryXID);
        wordValue.appendChild(doc.createTextNode(propertiesManager.isLocalMandatory() ? "T" : "F"));
        rootElement.appendChild(wordValue);

        // store option for unique local word
        wordValue = doc.createElement(XMLIDs.langPropLocalUniquenessXID);
        wordValue.appendChild(doc.createTextNode(propertiesManager.isLocalUniqueness() ? "T" : "F"));
        rootElement.appendChild(wordValue);

        // store option for unique conwords
        wordValue = doc.createElement(XMLIDs.langPropWordUniquenessXID);
        wordValue.appendChild(doc.createTextNode(propertiesManager.isWordUniqueness() ? "T" : "F"));
        rootElement.appendChild(wordValue);

        // record all genders
        while (genderLoop.hasNext()) {
            GenderNode curGen = genderLoop.next();

            wordNode = doc.createElement(XMLIDs.genderXID);
            rootElement.appendChild(wordNode);

            wordValue = doc.createElement(XMLIDs.genderIdXID);
            Integer wordId = curGen.getId();
            wordValue.appendChild(doc.createTextNode(wordId.toString()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.genderNameXID);
            wordValue.appendChild(doc.createTextNode(curGen.getValue()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.genderNotesXID);
            wordValue.appendChild(doc.createTextNode(curGen.getNotes()));
            wordNode.appendChild(wordValue);
        }

        // record all word types
        while (typeLoop.hasNext()) {
            TypeNode curType = typeLoop.next();

            wordNode = doc.createElement(XMLIDs.wordClassXID);
            rootElement.appendChild(wordNode);

            wordValue = doc.createElement(XMLIDs.wordClassIdXID);
            Integer wordId = curType.getId();
            wordValue.appendChild(doc.createTextNode(wordId.toString()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.wordClassNameXID);
            wordValue.appendChild(doc.createTextNode(curType.getValue()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.wordClassNotesXID);
            wordValue.appendChild(doc.createTextNode(curType.getNotes()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.wordClassDefManXID);
            wordValue.appendChild(doc.createTextNode(curType.isDefMandatory() ? "T" : "F"));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.wordClassGenderManXID);
            wordValue.appendChild(doc.createTextNode(curType.isGenderMandatory() ? "T" : "F"));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.wordClassPlurManXID);
            wordValue.appendChild(doc.createTextNode(curType.isPluralMandatory() ? "T" : "F"));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.wordClassProcManXID);
            wordValue.appendChild(doc.createTextNode(curType.isProcMandatory() ? "T" : "F"));
            wordNode.appendChild(wordValue);
        }

        // record all words
        while (wordLoop.hasNext()) {
            curWord = wordLoop.next();

            wordNode = doc.createElement(XMLIDs.wordXID);
            rootElement.appendChild(wordNode);

            wordValue = doc.createElement(XMLIDs.wordIdXID);
            Integer wordId = curWord.getId();
            wordValue.appendChild(doc.createTextNode(wordId.toString()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.localWordXID);
            wordValue.appendChild(doc.createTextNode(curWord.getLocalWord()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.conWordXID);
            wordValue.appendChild(doc.createTextNode(curWord.getValue()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.wordTypeXID);
            wordValue.appendChild(doc.createTextNode(curWord.getWordType()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.pronunciationXID);
            wordValue
                    .appendChild(doc.createTextNode(curWord.getPronunciation()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.wordGenderXID);
            wordValue.appendChild(doc.createTextNode(curWord.getGender()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.wordPlurXID);
            wordValue.appendChild(doc.createTextNode(curWord.getPlural()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.definitionXID);
            wordValue.appendChild(doc.createTextNode(curWord.getDefinition()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.wordProcOverrideXID);
            wordValue.appendChild(doc.createTextNode(curWord.isProcOverride() ? "T" : "F"));
            wordNode.appendChild(wordValue);
        }

        // record declension templates
        Set<Entry<Integer, List<DeclensionNode>>> declensionSet = declensionMgr.getTemplateMap().entrySet();
        for (Entry<Integer, List<DeclensionNode>> e : declensionSet) {
            Integer relatedId = e.getKey();

            for (DeclensionNode curNode : e.getValue()) {
                wordNode = doc.createElement(XMLIDs.declensionXID);
                rootElement.appendChild(wordNode);

                wordValue = doc.createElement(XMLIDs.declensionIdXID);
                wordValue.appendChild(doc.createTextNode(curNode.getId().toString()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(XMLIDs.declensionTextXID);
                wordValue.appendChild(doc.createTextNode(curNode.getValue()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(XMLIDs.declensionNotesXID);
                wordValue.appendChild(doc.createTextNode(curNode.getNotes()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(XMLIDs.declensionIsTemplateXID);
                wordValue.appendChild(doc.createTextNode("1"));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(XMLIDs.declensionRelatedIdXID);
                wordValue.appendChild(doc.createTextNode(relatedId.toString()));
                wordNode.appendChild(wordValue);
                
                wordValue = doc.createElement(XMLIDs.declensionMandatoryXID);
                wordValue.appendChild(doc.createTextNode(curNode.isMandatory() ? "T" : "F"));
                wordNode.appendChild(wordValue);
            }
        }

        // record word declensions
        declensionSet = declensionMgr.getDeclensionMap().entrySet();
        for (Entry<Integer, List<DeclensionNode>> e : declensionSet) {
            Integer relatedId = e.getKey();

            for (DeclensionNode curNode : e.getValue()) {
                wordNode = doc.createElement(XMLIDs.declensionXID);
                rootElement.appendChild(wordNode);

                wordValue = doc.createElement(XMLIDs.declensionIdXID);
                wordValue.appendChild(doc.createTextNode(curNode.getId().toString()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(XMLIDs.declensionTextXID);
                wordValue.appendChild(doc.createTextNode(curNode.getValue()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(XMLIDs.declensionNotesXID);
                wordValue.appendChild(doc.createTextNode(curNode.getNotes()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(XMLIDs.declensionRelatedIdXID);
                wordValue.appendChild(doc.createTextNode(relatedId.toString()));
                wordNode.appendChild(wordValue);

                wordValue = doc.createElement(XMLIDs.declensionIsTemplateXID);
                wordValue.appendChild(doc.createTextNode("0"));
                wordNode.appendChild(wordValue);
            }
        }

        // record pronunciation guide
        Iterator<PronunciationNode> procGuide = getPronunciations();
        while (procGuide.hasNext()) {
            PronunciationNode curNode = procGuide.next();

            wordNode = doc.createElement(XMLIDs.proGuideXID);
            rootElement.appendChild(wordNode);

            wordValue = doc.createElement(XMLIDs.proGuideBaseXID);
            wordValue.appendChild(doc.createTextNode(curNode.getValue()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(XMLIDs.proGuidePhonXID);
            wordValue.appendChild(doc.createTextNode(curNode.getPronunciation()));
            wordNode.appendChild(wordValue);
        }

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(_fileName));

        transformer.transform(source, result);
    }

    /**
     * deletes word based on word ID
     *
     * @param _id
     * @throws java.lang.Exception
     */
    public void deleteWordById(Integer _id) throws Exception {
        wordCollection.deleteNodeById(_id);
        clearAllDeclensionsWord(_id);
    }

    public void addDeclensionToWord(Integer wordId, Integer declensionId, DeclensionNode declension) {
        declensionMgr.addDeclensionToWord(wordId, declensionId, declension);
    }

    public void deleteDeclensionFromWord(Integer wordId, Integer declensionId) {
        declensionMgr.deleteDeclensionFromWord(wordId, declensionId);
    }

    public void updateDeclensionWord(Integer wordId, Integer declensionId, DeclensionNode declension) {
        declensionMgr.updateDeclensionWord(wordId, declensionId, declension);
    }

    /**
     * Clears all declensions from word
     *
     * @param wordId ID of word to clear of all declensions
     */
    public void clearAllDeclensionsWord(Integer wordId) {
        declensionMgr.clearAllDeclensionsWord(wordId);
    }

    public DeclensionNode getDeclensionTemplate(Integer typeId, Integer templateId) {
        return declensionMgr.getDeclensionTemplate(typeId, templateId);
    }

    /**
     * Returns list of words in descending list of synonym match
     *
     * @param _match The string value to match for
     * @return List of matching words
     */
    public List<ConWord> getSuggestedTransWords(String _match) {
        return wordCollection.getSuggestedTransWords(_match);
    }

    public List<DeclensionNode> getDeclensionListWord(Integer typeId) {
        return declensionMgr.getDeclensionListWord(typeId);
    }

    public DeclensionNode addDeclensionToTemplate(Integer typeId, String declension) {
        return declensionMgr.addDeclensionToTemplate(typeId, declension);
    }

    public void deleteDeclensionFromTemplate(Integer typeId, Integer declensionId) {
        declensionMgr.deleteDeclensionFromTemplate(typeId, declensionId);
    }

    public void modifyDeclensionTemplate(Integer typeId, Integer declensionId, DeclensionNode declension) {
        declensionMgr.updateDeclensionTemplate(typeId, declensionId, declension);
    }

    /**
     * Checks whether word is legal and returns error reason if not
     *
     * @param word word to check legality of
     * @return String of error is illegal, empty string otherwise (returns first
     * problem)
     */
    public String isWordLegal(ConWord word) {
        String ret = "";
        
        if (word.getValue().equals("")) {
            ret = "Words must have a cownword value set.";
        } else if (word.getWordType().equals("") && propertiesManager.isTypesMandatory()) {
            ret = "Types set to mandatory; please fill in type.";
        } else if (word.getLocalWord().equals("") && propertiesManager.isLocalMandatory()) {
            ret = "Local word set to mandatory; please fill in local word.";
        } else if (propertiesManager.isWordUniqueness() && wordCollection.containsWord(word.getValue())) {
            ret = "ConWords set to enforced unique, plese select spelling without existing homonyms.";
        } else if (propertiesManager.isLocalUniqueness() && !word.getLocalWord().equals("") 
                && wordCollection.containsLocalMultiples(word.getLocalWord())) {
            ret = "Local words set to enforced unique, and this local exists elsewhere.";
        } 
        
        // for more complex checks, use this pattern, only checking if other problems do not exist
        if (ret.equals("")) {
            ret = typeCollection.typeRequirementsMet(word);
        }
        
        if (ret.equals("")) {
            ret = declensionMgr.declensionRequirementsMet(word, typeCollection.findTypeByName(word.getWordType()));
        }

        return ret;
    }

    /**
     * Clears all declensions from word
     *
     * @param typeId ID of word to clear of all declensions
     */
    public void clearAllDeclensionsTemplate(Integer typeId) {
        declensionMgr.clearAllDeclensionsTemplate(typeId);
    }

    public List<DeclensionNode> getDeclensionListTemplate(Integer typeId) {
        return declensionMgr.getDeclensionListTemplate(typeId);
    }

    /**
     * Inserts new word into dictionary
     *
     * @param _addWord word to be inserted
     * @return ID of newly inserted word
     * @throws Exception
     */
    public int addWord(ConWord _addWord) throws Exception {
        int ret;
        wordCollection.getBufferWord().setEqual(_addWord);

        ret = wordCollection.insert();

        return ret;
    }

    /**
     * Safely modify a type (updates words of this type automatically)
     *
     * @param id type id
     * @param modType new type
     * @throws Exception
     */
    public void modifyType(Integer id, TypeNode modType) throws Exception {
        Iterator<ConWord> it;
        ConWord typeWord = new ConWord();

        typeWord.setWordType(typeCollection.getNodeById(id).getValue());
        typeWord.setValue("");
        typeWord.setDefinition("");
        typeWord.setGender("");
        typeWord.setLocalWord("");
        typeWord.setPlural("");
        typeWord.setPronunciation("");

        it = wordCollection.filteredList(typeWord);

        while (it.hasNext()) {
            ConWord modWord = it.next();

            modWord.setWordType(modType.getValue());

            wordCollection.modifyNode(modWord.getId(), modWord);
        }

        typeCollection.modifyNode(id, modType);
    }

    /**
     * Safely modify a gender (updates words of this gender automatically
     *
     * @param id gender id
     * @param modGender new gender
     * @throws Exception
     */
    public void modifyGender(Integer id, GenderNode modGender) throws Exception {
        Iterator<ConWord> it;
        ConWord genderWord = new ConWord();

        genderWord.setGender(genderCollection.getNodeById(id).getValue());
        genderWord.setValue("");
        genderWord.setDefinition("");
        genderWord.setWordType("");
        genderWord.setLocalWord("");
        genderWord.setPlural("");
        genderWord.setPronunciation("");

        it = wordCollection.filteredList(genderWord);

        while (it.hasNext()) {
            ConWord modWord = it.next();

            modWord.setGender(modGender.getValue());

            wordCollection.modifyNode(modWord.getId(), modWord);
        }

        genderCollection.modifyNode(id, modGender);
    }

    /**
     * Inserts new type into dictionary
     *
     * @param _filter word to be inserted
     * @return ID of newly inserted word
     * @throws Exception
     */
    public Iterator<ConWord> filteredWordList(ConWord _filter) throws Exception {
        return wordCollection.filteredList(_filter);
    }

    /**
     * @param id ID of type to modify
     * @param _modWord Updated word
     * @throws Exception If word DNE
     */
    public void modifyWord(Integer id, ConWord _modWord) throws Exception {
        wordCollection.modifyNode(id, _modWord);
    }

    public ConWord getWordById(Integer _id) throws Exception {
        return wordCollection.getNodeById(_id);
    }

    public Iterator<ConWord> getWordIterator() {
        return wordCollection.getNodeIterator();
    }

    public String getFontCon() {
        return propertiesManager.getFontCon();
    }

    public Integer getFontSize() {
        return propertiesManager.getFontSize();
    }

    public Integer getFontStyle() {
        return propertiesManager.getFontStyle();
    }

    public void setFontCon(String _fontCon, Integer _fontStyle, Integer _fontSize) {
        propertiesManager.setFontCon(_fontCon);
        propertiesManager.setFontSize(_fontSize);
        propertiesManager.setFontStyle(_fontStyle);
    }

    public TypeCollection getTypes() {
        return typeCollection;
    }

    public GenderCollection getGenders() {
        return genderCollection;
    }
}
