/*
 * Copyright (c) 2014-2020, Draque Thompson, draquemail@gmail.com
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

import java.awt.Desktop;
import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.CustomControls.PAlphaMap;
import org.darisadesigns.polyglotlina.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.ManagersCollections.PropertiesManager;
import org.darisadesigns.polyglotlina.ManagersCollections.GrammarManager;
import org.darisadesigns.polyglotlina.ManagersCollections.PronunciationMgr;
import org.darisadesigns.polyglotlina.ManagersCollections.LogoCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.FamilyManager;
import org.darisadesigns.polyglotlina.ManagersCollections.DeclensionManager;
import org.darisadesigns.polyglotlina.ManagersCollections.TypeCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.EtymologyManager;
import org.darisadesigns.polyglotlina.ManagersCollections.ImageCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.OptionsManager;
import org.darisadesigns.polyglotlina.ManagersCollections.ReversionManager;
import org.darisadesigns.polyglotlina.ManagersCollections.RomanizationManager;
import org.darisadesigns.polyglotlina.ManagersCollections.ToDoManager;
import org.darisadesigns.polyglotlina.ManagersCollections.WordClassCollection;
import org.darisadesigns.polyglotlina.Screens.ScrMainMenu;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import javax.swing.UIDefaults;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This is the core of PolyGlot. It manages the top level of all aspects of the program.
 * @author draque
 */
public class DictCore {

    private final PolyGlot polyGlot;
    private ConWordCollection wordCollection;
    private TypeCollection typeCollection;
    private DeclensionManager declensionMgr;
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
    private ToDoManager toDoManager;
    private ScrMainMenu rootWindow;
    private boolean curLoading = false;
    private Instant lastSaveTime = Instant.MIN;
    private String curFileName = "";
    
    /**
     * Language core initialization
     *
     * @param _polyGlot
     */
    public DictCore(PolyGlot _polyGlot) {
        polyGlot = _polyGlot;
        initializeDictCore();
    }
    
    public DictCore getNewCore() {
        return polyGlot.getNewCore(rootWindow);
    }
    
    private void initializeDictCore() {
        try {
            wordCollection = new ConWordCollection(this);
            typeCollection = new TypeCollection(this);
            declensionMgr = new DeclensionManager(this);
            propertiesManager = new PropertiesManager(this);
            pronuncMgr = new PronunciationMgr(this);
            romMgr = new RomanizationManager(this);
            famManager = new FamilyManager(this);
            logoCollection = new LogoCollection(this);
            grammarManager = new GrammarManager();
            wordClassCollection = new WordClassCollection(this);
            imageCollection = new ImageCollection();
            etymologyManager = new EtymologyManager(this);
            reversionManager = new ReversionManager(this);
            toDoManager = new ToDoManager();

            PAlphaMap<String, Integer> alphaOrder = propertiesManager.getAlphaOrder();

            wordCollection.setAlphaOrder(alphaOrder);
            typeCollection.setAlphaOrder(alphaOrder);
            logoCollection.setAlphaOrder(alphaOrder);
            wordClassCollection.setAlphaOrder(alphaOrder);
            rootWindow = null;
            
            PGTUtil.validateVersion();
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("CORE ERROR", "Error creating language core: " + e.getLocalizedMessage(), null);
        }
    }
    
    /**
     * Reloads main menu (force refresh of visual elements)
     */
    public void refreshMainMenu() {
        polyGlot.refreshUiDefaults();
        rootWindow.dispose(false);
        rootWindow = new ScrMainMenu(this);
        rootWindow.setVisible(true);
        rootWindow.selectFirstAvailableButton();
    }
    
    public UIDefaults getUiDefaults() {
        return polyGlot.getUiDefaults();
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
     * Sets root window (the ScrMainMenu object that the user sees)
     * @param _rootWindow 
     */
    public void setRootWindow(ScrMainMenu _rootWindow) {
        rootWindow = _rootWindow;
    }

    /**
     * Gets options manager
     * @return 
     */
    public OptionsManager getOptionsManager() {
        return polyGlot.getOptionsManager();
    }

    /**
     * Gets collection of all images available within current file
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
     * @return 
     */
    public WordClassCollection getWordClassCollection() {
        return wordClassCollection;
    }

    /**
     * Clipboard can be used to hold any object
     *
     * @param c object to hold
     */
    public void setClipBoard(Object c) {
        polyGlot.setClipBoard(c);
    }

    /**
     * Retrieves object held in clipboard, even if null, regardless of type
     *
     * @return contents of clipboard
     */
    public Object getClipBoard() {
        return polyGlot.getClipBoard();
    }

    /**
     * Pushes save signal to main interface menu
     */
    public void coreOpen() {
        rootWindow.open();
    }

    /**
     * Pushes save signal to main interface menu
     *
     * @param performTest whether to prompt user to save
     */
    public void coreNew(boolean performTest) {
        rootWindow.newFile(performTest);
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
     * @param _core new core to push
     */
    public void pushUpdateWithCore(DictCore _core) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        // prevent recursion (exclude check of top method, obviously)
        for (int i = (stack.length - 1); i > 1; i--) {
            StackTraceElement element = stack[i];
            if (element.getMethodName().equals("pushUpdateWithCore")) {
                return;
            }
        }

        // null root window indicates that this is a virtual dict core used for library analysis
        if (rootWindow != null) {
            rootWindow.updateAllValues(_core);
        }
    }

    /**
     * Returns root window of PolyGlot
     *
     * @return
     */
    public PFrame getRootWindow() {
        return rootWindow;
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
     * @return 
     */
    public LogoCollection getLogoCollection() {
        return logoCollection;
    }

    /**
     * Returns grammar guide in language file
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
     */
    public void buildLanguageReport() {
        final DictCore core = this;
        
        new Thread() {
            @Override
            public void run() {
                String reportContents = PLanguageStats.buildWordReport(core);
                
                try {
                    File report = IOHandler.createTmpFileWithContents(reportContents, ".html");

                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(report.toURI());
                    } else if (PGTUtil.IS_LINUX) {
                        Desktop.getDesktop().open(report);
                    } else {
                        InfoBox.warning("Menu Warning", "Unable to open browser. Please load manually at: \n" 
                                + report.getAbsolutePath() + "\n (copied to clipboard for convenience)", rootWindow);
                        new ClipboardHandler().setClipboardContents(report.getAbsolutePath());
                    }
                } catch (IOException e) {
                    InfoBox.error("Report Build Error", "Unable to generate/display language statistics: " 
                            + e.getLocalizedMessage(), rootWindow);
                    IOHandler.writeErrorLog(e);
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
        curLoading = true;
        curFileName = _fileName;
        String errorLog = "";
        String warningLog = "";

        // test file exists
        if (!IOHandler.fileExists(_fileName)) {
            throw new IOException("File " + _fileName + " does not exist.");
        }
        
        // inform user if file is not an archive
        if (!IOHandler.isFileZipArchive(_fileName)) {
            throw new IOException("File " + _fileName + " is not a valid PolyGlot archive.");
        }

        // load image assets first to allow referencing as dictionary loads
        try {
            IOHandler.loadImageAssets(imageCollection, _fileName);
        } catch (Exception e) {
            throw new IOException("Image loading error: " + e.getLocalizedMessage(), e);
        }

        try {
            PFontHandler.setFontFrom(_fileName, this);
        } catch (IOException | FontFormatException e) {
            IOHandler.writeErrorLog(e);
            warningLog += e.getLocalizedMessage() + "\n";
        }
        
        try {
            CustHandler handler;
            // if override XML value, load from that, otherwise pull from file
            if (overrideXML == null) {
                handler = IOHandler.getHandlerFromFile(_fileName, this);
                IOHandler.parseHandler(_fileName, handler);
            } else {
                handler = IOHandler.getHandlerFromByteArray(overrideXML, this);
                IOHandler.parseHandlerByteArray(overrideXML, handler);
            }
            
            errorLog += handler.getErrorLog();
            warningLog += handler.getWarningLog();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IOException(e.getMessage(), e);
        }

        try {
            IOHandler.loadGrammarSounds(_fileName, grammarManager);
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            warningLog += e.getLocalizedMessage() + "\n";
        }

        try {
            logoCollection.loadRadicalRelations();
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            warningLog += e.getLocalizedMessage() + "\n";
        }

        try {
            IOHandler.loadLogographs(logoCollection, _fileName);
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            warningLog += e.getLocalizedMessage() + "\n";
        }
        
        try {
            IOHandler.loadReversionStates(reversionManager, _fileName);
        } catch (IOException e) {
            IOHandler.writeErrorLog(e);
            warningLog += e.getLocalizedMessage() + "\n";
        }

        curLoading = false;

        if (!errorLog.trim().isEmpty()) {
            throw new IOException(errorLog);
        }

        if (!warningLog.trim().isEmpty()) {
            throw new IllegalStateException(warningLog);
        }
        
        // do not run in headless environments...
        if (rootWindow != null) {
            refreshMainMenu();
        }
    }
    
    /**
     * loads revision XML from revision byte array (does not support media revisions)
     * @param revision 
     * @param fileName 
     * @throws java.io.IOException 
     */
    public void revertToState(byte[] revision, String fileName) throws IOException{
        DictCore revDict = new DictCore(polyGlot);
        revDict.setRootWindow(rootWindow);
        revDict.readFile(fileName, revision);
        
        pushUpdateWithCore(revDict);
    }
    
    /**
     * Used for test loading reversion XMLs. Cannot successfully load actual revision into functioning DictCore
     * @param reversion 
     * @return
     */
    public String testLoadReversion(byte[] reversion) {
        String errorLog;
        
        try {
            CustHandler handler = IOHandler.getHandlerFromByteArray(reversion, this);
            IOHandler.parseHandlerByteArray(reversion, handler);

            errorLog = handler.getErrorLog();
            // errorLog += handler.getWarningLog(); // warnings may be disregarded here
        } catch (IOException | ParserConfigurationException | SAXException e) {
            IOHandler.writeErrorLog(e);
            errorLog = e.getLocalizedMessage();
        }
        
        // if no save time present, simply timestamp for current time (only relevant for first time revision log added)
        if (lastSaveTime == Instant.MIN) {
            lastSaveTime = Instant.now();
        }
        
        return errorLog;
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
            throws ParserConfigurationException, TransformerException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Instant newSaveTime = Instant.now();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement(PGTUtil.DICTIONARY_XID);
        doc.appendChild(rootElement);

        // collect XML representation of all dictionary elements
        writeXMLHeader(doc, rootElement, newSaveTime);
        propertiesManager.writeXML(doc, rootElement);
        wordClassCollection.writeXML(doc, rootElement);
        typeCollection.writeXML(doc, rootElement);
        wordCollection.writeXML(doc, rootElement);
        etymologyManager.writeXML(doc, rootElement);
        declensionMgr.writeXML(doc, rootElement);
        pronuncMgr.writeXML(doc, rootElement);
        romMgr.writeXML(doc, rootElement);
        logoCollection.writeXML(doc, rootElement);
        grammarManager.writeXML(doc, rootElement);
        toDoManager.writeXML(doc, rootElement);

        // write family entries
        rootElement.appendChild(famManager.writeToSaveXML(doc));

        // have IOHandler write constructed document to file
        IOHandler.writeFile(_fileName, doc, this, polyGlot.getWorkingDirectory(), newSaveTime);
        
        lastSaveTime = newSaveTime;
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
     * @return 
     */
    public DeclensionManager getDeclensionManager() {
        return declensionMgr;
    }

    /**
     * Returns all parts of speech
     * @return 
     */
    public TypeCollection getTypes() {
        return typeCollection;
    }

    /**
     * Returns pronunciations within language file
     * @return 
     */
    public PronunciationMgr getPronunciationMgr() {
        return pronuncMgr;
    }

    /**
     * Returns romanization manager within language file
     * @return 
     */
    public RomanizationManager getRomManager() {
        return romMgr;
    }
    
    /**
     * Returns etymology manager within language file
     * @return 
     */
    public EtymologyManager getEtymologyManager() {
        return etymologyManager;
    }
    
    /**
     * Returns XML file reversions within language file
     * @return 
     */
    public ReversionManager getReversionManager() {
        return reversionManager;
    }
    
    /**
     * Returns to do manager within language file
     * @return 
     */
    public ToDoManager getToDoManager() {
        return toDoManager;
    }
    
    /**
     * Returns last time language file was saved
     * @return 
     */
    public Instant getLastSaveTime() {
        return lastSaveTime;
    }

    /**
     * Sets time of last save
     * @param _lastSaveTime 
     */
    public void setLastSaveTime(Instant _lastSaveTime) {
        lastSaveTime = _lastSaveTime;
    }
    
    public void saveOptionsIni() throws IOException {
        polyGlot.saveOptionsIni();
    }
    
    public File getWorkingDirectory() {
        return polyGlot.getWorkingDirectory();
    }
    
    public void setCurFileName(String _curFileName) {
        this.curFileName = _curFileName;
    }
    
    public String getCurFileName() {
        return curFileName;
    }
    
    /**
     * Returns true if the language has no contents (blank language)
     * @return 
     */
    public boolean isLanguageEmpty() {
        return wordCollection.isEmpty()
                && typeCollection.isEmpty()
                && declensionMgr.isEmpty()
                && pronuncMgr.isEmpty()
                && romMgr.isEmpty()
                && logoCollection.isEmpty()
                && grammarManager.isEmpty()
                && wordClassCollection.isEmpty()
                && imageCollection.isEmpty();
    }
    
    /**
     * Compares equality of languages.
     * Does not compare save version.
     * Does not compare filename.
     * Does not compare last save time.
     * Does not compare reversion states.
     * 
     * Depending on size of language, might take some time.
     * @param comp
     * @return 
     */
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;

        if (comp instanceof DictCore) {
            DictCore compCore = (DictCore)comp;
            
            ret = wordCollection.equals(compCore.wordCollection)
                    && typeCollection.equals(compCore.typeCollection)
                    && declensionMgr.equals(compCore.declensionMgr)
                    && propertiesManager.equals(compCore.propertiesManager)
                    && pronuncMgr.equals(compCore.pronuncMgr)
                    && romMgr.equals(compCore.romMgr)
                    && famManager.equals(compCore.famManager)
                    && logoCollection.equals(compCore.logoCollection)
                    && grammarManager.equals(compCore.grammarManager)
                    && wordClassCollection.equals(compCore.wordClassCollection)
                    && imageCollection.equals(compCore.imageCollection)
                    && etymologyManager.equals(compCore.etymologyManager)
                    && toDoManager.equals(compCore.toDoManager);
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
