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

import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.ManagersCollections.GenderCollection;
import PolyGlot.ManagersCollections.PropertiesManager;
import PolyGlot.ManagersCollections.GrammarManager;
import PolyGlot.ManagersCollections.PronunciationMgr;
import PolyGlot.ManagersCollections.LogoCollection;
import PolyGlot.ManagersCollections.ThesaurusManager;
import PolyGlot.ManagersCollections.DeclensionManager;
import PolyGlot.ManagersCollections.TypeCollection;
import PolyGlot.ManagersCollections.ConWordCollection;
import java.awt.Color;
import java.awt.FontFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class DictCore {

    private final String version = "0.9.9.1";
    private final ConWordCollection wordCollection = new ConWordCollection(this);
    private final TypeCollection typeCollection = new TypeCollection(this);
    private final GenderCollection genderCollection = new GenderCollection(this);
    private final DeclensionManager declensionMgr = new DeclensionManager();
    private final PropertiesManager propertiesManager = new PropertiesManager();
    private final PronunciationMgr pronuncMgr = new PronunciationMgr(this);
    private final ThesaurusManager thesManager = new ThesaurusManager(this);
    private final LogoCollection logoCollection = new LogoCollection(this);
    private final GrammarManager grammarManager = new GrammarManager();

    /**
     * Gets proper color for fields marked as required
     *
     * @return
     */
    public Color getRequiredColor() {
        return new Color(255, 204, 204);
    }

    /**
     * gets thesaurus manager
     *
     * @return ThesaurusManager object from core
     */
    public ThesaurusManager getThesManager() {
        return thesManager;
    }

    public LogoCollection getLogoCollection() {
        return logoCollection;
    }

    public GrammarManager getGrammarManager() {
        return grammarManager;
    }

    /**
     * gets properties manager
     *
     * @return PropertiesManager object from core
     */
    public PropertiesManager getPropertiesManager() {
        return propertiesManager;
    }

    /**
     * gets version ID of PolyGlot
     *
     * @return String value of version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets lexicon manager
     *
     * @return ConWordCollection from core
     */
    public ConWordCollection getWordCollection() {
        return wordCollection;
    }

    /**
     * Builds a report on the conlang. Potentially very computationally
     * expensive.
     *
     * @return String formatted report
     */
    public String buildLanguageReport() {
        String ret = ConWordCollection.formatPlain("<center>---LANGUAGE STAT REPORT---</center><br><br>");

        ret += propertiesManager.buildPropertiesReport();

        ret += wordCollection.buildWordReport();

        return ret;
    }

    public DictCore() {
        Map alphaOrder = propertiesManager.getAlphaOrder();

        wordCollection.setAlphaOrder(alphaOrder);
        typeCollection.setAlphaOrder(alphaOrder);
        genderCollection.setAlphaOrder(alphaOrder);
        logoCollection.setAlphaOrder(alphaOrder);
    }

    /**
     * Reads from given file
     *
     * @param _fileName filename to read from
     * @throws Exception detailing any loading problems
     */
    public void readFile(String _fileName) throws Exception {
        String loadLog = "";
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            CustHandler handler = CustHandlerFactory.getCustHandler(IOHandler.getDictFile(_fileName), this);

            handler.setWordCollection(wordCollection);
            handler.setTypeCollection(typeCollection);

            saxParser.parse(IOHandler.getDictFile(_fileName), handler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new Exception(e.getMessage());
        }

        try {
            IOHandler.setFontFrom(_fileName, this);
        } catch (IOException | FontFormatException e) {
            loadLog += e.getLocalizedMessage() + "\n";
        }

        try {
            IOHandler.loadGrammarSounds(_fileName, grammarManager);
        } catch (Exception e) {
            loadLog += e.getLocalizedMessage() + "\n";
        }

        try {
            logoCollection.loadRadicalRelations();
        } catch (Exception e) {
            loadLog += e.getLocalizedMessage() + "\n";
        }

        try {
            IOHandler.loadImages(logoCollection, _fileName);
        } catch (Exception e) {
            loadLog += e.getLocalizedMessage() + "\n";
        }

        if (!loadLog.equals("")) {
            throw new Exception("Problems lodaing file:\n");
        }
    }

    /**
     * Writes to given file
     *
     * @param _fileName filename to write to
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerException
     * @throws java.io.FileNotFoundException
     */
    public void writeFile(String _fileName)
            throws ParserConfigurationException, TransformerException, FileNotFoundException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Element wordValue;

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement(PGTUtil.dictionaryXID);
        doc.appendChild(rootElement);

        // store version of PolyGlot
        wordValue = doc.createElement(PGTUtil.pgVersionXID);
        wordValue.appendChild(doc.createTextNode(version));
        rootElement.appendChild(wordValue);

        // collect XML representation of all dictionary elements
        propertiesManager.writeXML(doc, rootElement);
        genderCollection.writeXML(doc, rootElement);
        typeCollection.writeXML(doc, rootElement);
        wordCollection.writeXML(doc, rootElement);
        declensionMgr.writeXML(doc, rootElement);
        pronuncMgr.writeXML(doc, rootElement);
        logoCollection.writeXML(doc, rootElement);
        grammarManager.writeXML(doc, rootElement);

        // write thesaurus entries
        rootElement.appendChild(thesManager.writeToSaveXML(doc));

        // have IOHandler write constructed document to file
        IOHandler.writeFile(_fileName, doc, this);
    }

    /**
     * deletes word based on word ID Makes sure to clear all records of word
     * declension
     *
     * @param _id
     * @throws java.lang.Exception
     */
    public void deleteWordById(Integer _id) throws Exception {
        wordCollection.deleteNodeById(_id);
        clearAllDeclensionsWord(_id);
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

    public DeclensionManager getDeclensionManager() {
        return declensionMgr;
    }

    /**
     * Checks whether word is legal and returns error reason if not
     *
     * @param word word to check legality of
     * @return Conword with any illegal entries saved as word values
     */
    public ConWord isWordLegal(ConWord word) {
        ConWord ret = new ConWord();

        if (word.getValue().equals("")) {
            ret.setValue("ConWord value cannot be blank.");
        }

        if (word.getWordType().equals("") && propertiesManager.isTypesMandatory()) {
            ret.setWordType("Types set to mandatory.");
        }

        if (word.getLocalWord().equals("") && propertiesManager.isLocalMandatory()) {
            ret.setLocalWord("Local word set to mandatory.");
        }

        if (propertiesManager.isWordUniqueness() && wordCollection.containsWord(word.getValue())) {
            ret.setValue(ret.getValue() + (ret.getValue().equals("") ? "" : "\n")
                    + "ConWords set to enforced unique: this conword exists elsewhere.");
        }

        if (propertiesManager.isLocalUniqueness() && !word.getLocalWord().equals("")
                && wordCollection.containsLocalMultiples(word.getLocalWord())) {
            ret.setLocalWord(ret.getLocalWord() + (ret.getLocalWord().equals("") ? "" : "\n")
                    + "Local words set to enforced unique: this local exists elsewhere.");
        }

        ret.setWordType(ret.getWordType() + (ret.getWordType().equals("") ? "" : "\n")
                + typeCollection.typeRequirementsMet(word));

        ret.setDefinition(ret.getDefinition() + (ret.getDefinition().equals("") ? "" : "\n")
                + declensionMgr.declensionRequirementsMet(word, typeCollection.findTypeByName(word.getWordType())));

        TypeNode wordType = typeCollection.findTypeByName(word.getWordType());

        if (wordType != null) {
            String typeRegex = wordType.getPattern();

            if (!typeRegex.equals("") && !word.getValue().matches(typeRegex)) {
                ret.setDefinition(ret.getDefinition() + (ret.getDefinition().equals("") ? "" : "\n")
                        + "Word does not match enforced pattern for type: " + word.getWordType() + ".");
                ret.setProcOverride(true);
            }
        }

        return ret;
    }

    public TypeCollection getTypes() {
        return typeCollection;
    }

    public GenderCollection getGenders() {
        return genderCollection;
    }

    public PronunciationMgr getPronunciationMgr() {
        return pronuncMgr;
    }
}
