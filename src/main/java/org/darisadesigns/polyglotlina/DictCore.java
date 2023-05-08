/*
 * Copyright (c) 2014-2023, Draque Thompson, draquemail@gmail.com
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

import java.io.ByteArrayInputStream;
import org.darisadesigns.polyglotlina.PLanguageStats.PLanguageStatsProgress;
import org.darisadesigns.polyglotlina.CustomControls.PAlphaMap;
import org.darisadesigns.polyglotlina.ManagersCollections.PropertiesManager;
import org.darisadesigns.polyglotlina.ManagersCollections.GrammarManager;
import org.darisadesigns.polyglotlina.ManagersCollections.PronunciationMgr;
import org.darisadesigns.polyglotlina.ManagersCollections.LogoCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.FamilyManager;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.ManagersCollections.TypeCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.EtymologyManager;
import org.darisadesigns.polyglotlina.ManagersCollections.ImageCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.ReversionManager;
import org.darisadesigns.polyglotlina.ManagersCollections.RomanizationManager;
import org.darisadesigns.polyglotlina.ManagersCollections.ToDoManager;
import org.darisadesigns.polyglotlina.ManagersCollections.WordClassCollection;
import org.darisadesigns.polyglotlina.OSHandler.CoreUpdatedListener;
import org.darisadesigns.polyglotlina.OSHandler.FileReadListener;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.darisadesigns.polyglotlina.CustomControls.CoreUpdateSubscriptionInterface;
import org.darisadesigns.polyglotlina.DomParser.PDomParser;
import org.darisadesigns.polyglotlina.ManagersCollections.PhraseManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This is the core of PolyGlot. It manages the top level of all aspects of the
 * program.
 *
 * @author draque
 */
public class DictCore {

    private final PGTUtil pgtUtil;
    private ConWordCollection wordCollection;
    private TypeCollection typeCollection;
    private ConjugationManager conjugationMgr;
    private PropertiesManager propertiesManager;
    private PronunciationMgr pronuncMgr;
    private RomanizationManager romMgr;
    private FamilyManager famManager;
    private LogoCollection logoCollection;
    private GrammarManager grammarManager;
    private WordClassCollection wordClassCollection;
    private ImageCollection imageCollection;
    private EtymologyManager etymologyManager;
    private ReversionManager reversionManager;
    private PhraseManager phraseManager;
    private ToDoManager toDoManager;
    private final OSHandler osHandler;
    private boolean curLoading = false;
    private Instant lastSaveTime = Instant.MIN;
    private String curFileName = "";
    private List<CoreUpdateSubscriptionInterface> subscribers;
    private DictCore loadState = null;

    /**
     * Language core initialization
     *
     * @param _propertiesManager
     * @param _osHandler
     * @param _util
     * @param _grammarManager
     */
    public DictCore(PropertiesManager _propertiesManager, OSHandler _osHandler, PGTUtil _util, GrammarManager _grammarManager) {
        osHandler = _osHandler;
        pgtUtil = _util;
        initializeDictCore(_propertiesManager, _grammarManager);
    }

    private void initializeDictCore(PropertiesManager _propertiesManager, GrammarManager _grammarManager) {
        try {
            wordCollection = new ConWordCollection(this);
            typeCollection = new TypeCollection(this);
            conjugationMgr = new ConjugationManager(this);
            propertiesManager = _propertiesManager;
            propertiesManager.setDictCore(this);
            pronuncMgr = new PronunciationMgr(this);
            romMgr = new RomanizationManager(this);
            famManager = new FamilyManager(this);
            logoCollection = new LogoCollection(this);
            _grammarManager.setCore(this);
            grammarManager = _grammarManager;
            wordClassCollection = new WordClassCollection(this);
            imageCollection = new ImageCollection(this);
            etymologyManager = new EtymologyManager(this);
            reversionManager = new ReversionManager(this);
            toDoManager = new ToDoManager();
            phraseManager = new PhraseManager(this);

            PAlphaMap<String, Integer> alphaOrder = propertiesManager.getAlphaOrder();
            subscribers = new ArrayList<>();

            wordCollection.setAlphaOrder(alphaOrder);
            logoCollection.setAlphaOrder(alphaOrder);

            PGTUtil.validateVersion();
        }
        catch (Exception e) {
            this.osHandler.getIOHandler().writeErrorLog(e);
            this.osHandler.getInfoBox().error("CORE ERROR", "Error creating language core: " + e.getLocalizedMessage());
        }
    }

    /**
     * Gets conlang name or CONLANG. Put on core because it's used a lot.
     *
     * @return either name of conlang or "Conlang"
     */
    public String conLabel() {
        return propertiesManager.getLangName().isEmpty()
                ? "Conlang"
                : propertiesManager.getLangName();
    }

    /**
     * Gets local language name or Local Lang. Put on core because it's used a
     * lot.
     *
     * @return either name of local language or "Local Lang"
     */
    public String localLabel() {
        return propertiesManager.getLocalLangName().isEmpty()
                ? "Local Lang"
                : propertiesManager.getLocalLangName();
    }

    /**
     * Gets collection of all images available within current file
     *
     * @return
     */
    public ImageCollection getImageCollection() {
        return imageCollection;
    }

    /**
     * Returns whether core is currently loading a file
     *
     * @return true if currently loading
     */
    public boolean isCurLoading() {
        return curLoading;
    }

    /**
     * Returns all word classes
     *
     * @return
     */
    public WordClassCollection getWordClassCollection() {
        return wordClassCollection;
    }

    /**
     * Pushes signal to all forms to update their values from the core. Cascades
     * through windows and their children.
     */
    public void pushUpdate() {
        pushUpdateWithCore(this);
    }

    /**
     * Pushes signal to all forms to update their values from the core. Cascades
     * through windows and their children.
     *
     * @param _core new core to push
     */
    public void pushUpdateWithCore(DictCore _core) {
        // ------------------------------
        // OLD STYLE OF UPDATING OBJECTS
        // ------------------------------
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        // prevent recursion (exclude check of top method, obviously)
        for (int i = (stack.length - 1); i > 1; i--) {
            StackTraceElement element = stack[i];
            if (element.getMethodName().equals("pushUpdateWithCore")) {
                return;
            }
        }

        CoreUpdatedListener listener = _core.getOSHandler().getCoreUpdatedListener();
        if (null != listener) {
            listener.coreUpdated(_core);
        }

        // ------------------------------
        // NEW STYLE OF UPDATING OBJECTS
        // ------------------------------
        sendUpdate(_core);
    }

    /**
     * subscribes to updates from core
     *
     * @param newSub
     */
    public void subscribe(CoreUpdateSubscriptionInterface newSub) {
        if (!subscribers.contains(newSub)) {
            subscribers.add(newSub);
        }
    }

    public void unSubscribe(CoreUpdateSubscriptionInterface newSub) {
        if (subscribers.contains(newSub)) {
            subscribers.remove(newSub);
        }
    }

    /**
     * Migrates all subscriptions to new core
     *
     * @param core
     */
    public void migrateSubscriptions(DictCore core) {
        for (CoreUpdateSubscriptionInterface subscriber : subscribers) {
            subscriber.setCore(core);
        }
        subscribers.clear();
    }

    /**
     * Sends out update to all subscribers
     *
     * @param core
     */
    public void sendUpdate(DictCore core) {
        subscribers.forEach(subscriber -> {
            subscriber.setCore(core);
            subscriber.updateFromCore();
        });
    }

    /**
     * gets family manager
     *
     * @return FamilyManager object from core
     */
    public FamilyManager getFamManager() {
        return famManager;
    }

    /**
     * Returns collection of all logographs in language file
     *
     * @return
     */
    public LogoCollection getLogoCollection() {
        return logoCollection;
    }

    /**
     * Returns grammar guide in language file
     *
     * @return
     */
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
     * @param progress
     */
    public void buildLanguageReport(PLanguageStatsProgress progress) {
        final DictCore core = this;

        new Thread() {
            @Override
            public void run() {
                String reportContents = PLanguageStats.buildWordReport(core, progress);

                if (!core.getPGTUtil().isBlank(reportContents)) {
                    core.getOSHandler().openLanguageReport(reportContents);
                }
            }
        }.start();
    }

    /**
     * Reads from given file
     *
     * @param _fileName filename to read from
     * @throws java.io.IOException for unrecoverable errors
     * @throws IllegalStateException for recoverable errors
     */
    public void readFile(String _fileName) throws IOException, IllegalStateException {
        readFile(_fileName, null);
    }

    /**
     * Reads from given file
     *
     * @param _fileName filename to read from
     * @param overrideXML override to where the XML should be loaded from
     * @throws java.io.IOException for unrecoverable errors
     * @throws IllegalStateException for recoverable errors
     */
    public void readFile(String _fileName, byte[] overrideXML) throws IOException, IllegalStateException {
        readFile(_fileName, overrideXML, true);
    }

    /**
     * Reads from given file
     *
     * @param _fileName filename to read from
     * @param overrideXML override to where the XML should be loaded from
     * @param useFileReadListener whether to use file read listener
     * @throws java.io.IOException for unrecoverable errors
     * @throws IllegalStateException for recoverable errors
     */
    public void readFile(String _fileName, byte[] overrideXML, boolean useFileReadListener) throws IOException, IllegalStateException {
        curLoading = true;
        curFileName = _fileName;
        String errorLog = "";
        String warningLog = "";

        try {
            // test file exists
            if (!this.osHandler.getIOHandler().fileExists(_fileName)) {
                throw new IOException("File " + _fileName + " does not exist.");
            }

            // inform user if file is not an archive
            if (!this.osHandler.getIOHandler().isFileZipArchive(_fileName)) {
                throw new IOException("File " + _fileName + " is not a valid PolyGlot archive.");
            }

            // load image assets first to allow referencing as dictionary loads
            try {
                this.osHandler.getIOHandler().loadImageAssets(imageCollection, _fileName, this);
            }
            catch (Exception e) {
                warningLog += "Image loading error: " + e.getLocalizedMessage() + "\n";
            }

            try {
                this.osHandler.fontHandler.setFontFrom(_fileName, this);
            }
            catch (Exception e) {
                this.osHandler.getIOHandler().writeErrorLog(e);
                warningLog += e.getLocalizedMessage() + "\n";
            }

            PDomParser parser = new PDomParser(this);

            byte[] rawXml = overrideXML == null
                    ? this.osHandler.getIOHandler().getXmlBytesFromArchive(_fileName)
                    : overrideXML;

            parser.readXml(new ByteArrayInputStream(rawXml));
            Exception parseException = parser.getError();

            if (parseException instanceof SAXException) {
                // attempt to repair malformed XML on IOException
                parser = new PDomParser(this);
                XMLRecoveryTool recovery = new XMLRecoveryTool(new String(rawXml, StandardCharsets.UTF_8));
                String recoveredXml = recovery.recoverXml();
                parser.readXml(new ByteArrayInputStream(recoveredXml.getBytes()));

                // if not possible to recover, bubble error
                if (parser.getError() != null) {
                    throw new IOException(parser.getError());
                }
            } else if (parseException != null) {
                throw new IOException(parseException);
            }

            for (String issue : parser.getIssues()) {
                warningLog += issue + "\n";
            }

            try {
                this.osHandler.getIOHandler().loadGrammarSounds(_fileName, grammarManager);
            }
            catch (Exception e) {
                this.osHandler.getIOHandler().writeErrorLog(e);
                warningLog += e.getLocalizedMessage() + "\n";
            }

            try {
                logoCollection.loadRadicalRelations();
            }
            catch (Exception e) {
                this.osHandler.getIOHandler().writeErrorLog(e);
                warningLog += e.getLocalizedMessage() + "\n";
            }

            try {
                this.osHandler.getIOHandler().loadLogographs(logoCollection, _fileName);
            }
            catch (Exception e) {
                this.osHandler.getIOHandler().writeErrorLog(e);
                warningLog += e.getLocalizedMessage() + "\n";
            }

            try {
                this.osHandler.getIOHandler().loadReversionStates(reversionManager, _fileName);
            }
            catch (IOException e) {
                this.osHandler.getIOHandler().writeErrorLog(e);
                warningLog += e.getLocalizedMessage() + "\n";
            }
        }
        finally {
            curLoading = false;
        }

        if (!errorLog.trim().isEmpty()) {
            throw new IOException(errorLog);
        }

        if (!warningLog.trim().isEmpty()) {
            throw new IllegalStateException(warningLog);
        }

        if (useFileReadListener) {
            FileReadListener listener = this.getOSHandler().getFileReadListener();
            if (null != listener) {
                listener.fileRead(this);
            }
        }
    }

    public void setLoadState(DictCore _loadState) {
        loadState = _loadState;
    }

    /**
     * Returns true if language has changed state since last load
     *
     * @return
     */
    public boolean hasChanged() {
        return loadState != null && !this.equals(loadState);
    }

    /**
     * loads revision XML from revision byte array (does not support media
     * revisions)
     *
     * @param revision
     * @param fileName
     * @return
     * @throws java.io.IOException
     */
    public DictCore revertToState(byte[] revision, String fileName) throws IOException {
        DictCore revDict = new DictCore(this.propertiesManager, this.osHandler, this.pgtUtil, this.grammarManager);
        revDict.readFile(fileName, revision);

        pushUpdateWithCore(revDict);

        return revDict;
    }

    /**
     * Used for test loading reversion XMLs. Cannot successfully load actual
     * revision into functioning DictCore
     *
     * @param reversion
     * @return
     */
    public String testLoadReversion(byte[] reversion) {
        PDomParser parser = new PDomParser(this);
        parser.readXml(new ByteArrayInputStream(reversion));
        String error = "";

        // if no save time present, simply timestamp for current time (only relevant for first time revision log added)
        if (lastSaveTime == Instant.MIN) {
            lastSaveTime = Instant.now();
        }

        Exception parseException = parser.getError();
        if (parseException != null) {
            this.osHandler.getIOHandler().writeErrorLog(parseException);
            error = parseException.getLocalizedMessage();
        }

        // we only care about errors here, rather than warnings
        return error;
    }

    /**
     * Writes to given file
     *
     * @param _fileName filename to write to
     * @param writeToReversionMgr
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerException
     * @throws java.io.FileNotFoundException
     */
    public void writeFile(String _fileName, boolean writeToReversionMgr)
            throws ParserConfigurationException, TransformerException, IOException {
        PGTUtil.waitForWritePermission();

        try {
            PGTUtil.claimWriteLock();
            Instant newSaveTime = Instant.now();
            Document doc = getXmlDoc();

            // have IOHandler write constructed document to file
            this.osHandler.getIOHandler().writeFile(
                    _fileName,
                    doc,
                    this,
                    this.getWorkingDirectory(),
                    newSaveTime,
                    writeToReversionMgr
            );

            lastSaveTime = newSaveTime;
        }
        finally {
            if (PGTUtil.isWriteLock()) {
                PGTUtil.releaseWriteLock();
            }
        }
    }
    
    /**
     * Generates and returns the raw XML of a language file
     * @return 
     * @throws javax.xml.parsers.ParserConfigurationException 
     * @throws java.io.IOException 
     * @throws javax.xml.transform.TransformerException 
     */
    public String getRawXml() throws ParserConfigurationException, IOException, TransformerException {
        Document doc = getXmlDoc();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        try (StringWriter writer = new StringWriter()) {
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            StringBuilder sb = new StringBuilder();
            sb.append(writer.getBuffer());
            return sb.toString();
        }
    }

    /**
     * Gets full XML document of language
     */
    private Document getXmlDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Instant newSaveTime = Instant.now();

        // clean up etymological entries which might be orphaned
        etymologyManager.cleanBrokenEtymologyRoots();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement(PGTUtil.DICTIONARY_XID);
        doc.appendChild(rootElement);

        // store system info for troubleshooting
        Element sysInfo = doc.createElement(PGTUtil.SYS_INFO_XID);
        sysInfo.appendChild(doc.createTextNode(osHandler.getIOHandler().getSystemInformation()));
        rootElement.appendChild(sysInfo);

        // collect XML representation of all dictionary elements
        writeXMLHeader(doc, rootElement, newSaveTime);
        propertiesManager.writeXML(doc, rootElement);
        wordClassCollection.writeXML(doc, rootElement);
        typeCollection.writeXML(doc, rootElement);
        wordCollection.writeXML(doc, rootElement);
        etymologyManager.writeXML(doc, rootElement);
        conjugationMgr.writeXML(doc, rootElement);
        pronuncMgr.writeXML(doc, rootElement);
        romMgr.writeXML(doc, rootElement);
        logoCollection.writeXML(doc, rootElement);
        grammarManager.writeXML(doc, rootElement);
        toDoManager.writeXML(doc, rootElement);
        phraseManager.writeXML(doc, rootElement);

        // write family entries
        rootElement.appendChild(famManager.writeToSaveXML(doc));
        
        return doc;
    }

    private static void writeXMLHeader(Document doc, Element rootElement, Instant saveTime) {
        Element headerElement = doc.createElement(PGTUtil.PGVERSION_XID);
        headerElement.appendChild(doc.createTextNode(PGTUtil.PGT_VERSION));
        rootElement.appendChild(headerElement);

        headerElement = doc.createElement(PGTUtil.DICTIONARY_SAVE_DATE);
        headerElement.appendChild(doc.createTextNode(saveTime.toString()));
        rootElement.appendChild(headerElement);
    }

    /**
     * Returns declension manager within language file
     *
     * @return
     */
    public ConjugationManager getConjugationManager() {
        return conjugationMgr;
    }

    /**
     * Returns all parts of speech
     *
     * @return
     */
    public TypeCollection getTypes() {
        return typeCollection;
    }

    /**
     * Returns pronunciations within language file
     *
     * @return
     */
    public PronunciationMgr getPronunciationMgr() {
        return pronuncMgr;
    }

    /**
     * Returns romanization manager within language file
     *
     * @return
     */
    public RomanizationManager getRomManager() {
        return romMgr;
    }

    /**
     * Returns etymology manager within language file
     *
     * @return
     */
    public EtymologyManager getEtymologyManager() {
        return etymologyManager;
    }

    /**
     * Returns XML file reversions within language file
     *
     * @return
     */
    public ReversionManager getReversionManager() {
        return reversionManager;
    }

    /**
     * Returns to do manager within language file
     *
     * @return
     */
    public ToDoManager getToDoManager() {
        return toDoManager;
    }

    public PhraseManager getPhraseManager() {
        return phraseManager;
    }

    /**
     * Returns DictCore OS handler
     *
     * @return
     */
    public OSHandler getOSHandler() {
        return this.osHandler;
    }

    public PGTUtil getPGTUtil() {
        return this.pgtUtil;
    }

    /**
     * Returns last time language file was saved
     *
     * @return
     */
    public Instant getLastSaveTime() {
        return lastSaveTime;
    }

    /**
     * Sets time of last save
     *
     * @param _lastSaveTime
     */
    public void setLastSaveTime(Instant _lastSaveTime) {
        lastSaveTime = _lastSaveTime;
    }

    public File getWorkingDirectory() {
        return this.osHandler.getWorkingDirectory();
    }

    public void setCurFileName(String _curFileName) {
        this.curFileName = _curFileName;
    }

    public String getCurFileName() {
        return curFileName;
    }

    /**
     * Returns true if the language has no contents (blank language)
     *
     * @return
     */
    public boolean isLanguageEmpty() {
        return wordCollection.isEmpty()
                && typeCollection.isEmpty()
                && conjugationMgr.isEmpty()
                && pronuncMgr.isEmpty()
                && romMgr.isEmpty()
                && logoCollection.isEmpty()
                && grammarManager.isEmpty()
                && wordClassCollection.isEmpty()
                && imageCollection.isEmpty()
                && phraseManager.isEmpty();
    }

    /**
     * Compares equality of languages. Does not compare save version. Does not
     * compare filename. Does not compare last save time. Does not compare
     * reversion states.
     *
     * Depending on size of language, might take some time.
     *
     * @param comp
     * @return
     */
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;

        if (comp instanceof DictCore) {
            DictCore compCore = (DictCore) comp;

            ret = wordCollection.equals(compCore.wordCollection);
            ret = ret && typeCollection.equals(compCore.typeCollection);
            ret = ret && conjugationMgr.equals(compCore.conjugationMgr);
            ret = ret && propertiesManager.equals(compCore.propertiesManager);
            ret = ret && pronuncMgr.equals(compCore.pronuncMgr);
            ret = ret && romMgr.equals(compCore.romMgr);
            ret = ret && famManager.equals(compCore.famManager);
            ret = ret && logoCollection.equals(compCore.logoCollection);
            ret = ret && grammarManager.equals(compCore.grammarManager); // here it is
            ret = ret && wordClassCollection.equals(compCore.wordClassCollection);
            ret = ret && imageCollection.equals(compCore.imageCollection);
            ret = ret && etymologyManager.equals(compCore.etymologyManager);
            ret = ret && toDoManager.equals(compCore.toDoManager);
            ret = ret && phraseManager.equals(compCore.phraseManager);
        }

        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.propertiesManager);
        return hash;
    }
}
