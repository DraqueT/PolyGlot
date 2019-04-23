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
package PolyGlot;

import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PAlphaMap;
import PolyGlot.CustomControls.PFrame;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.ManagersCollections.PropertiesManager;
import PolyGlot.ManagersCollections.GrammarManager;
import PolyGlot.ManagersCollections.PronunciationMgr;
import PolyGlot.ManagersCollections.LogoCollection;
import PolyGlot.ManagersCollections.FamilyManager;
import PolyGlot.ManagersCollections.DeclensionManager;
import PolyGlot.ManagersCollections.TypeCollection;
import PolyGlot.ManagersCollections.ConWordCollection;
import PolyGlot.ManagersCollections.EtymologyManager;
import PolyGlot.ManagersCollections.ImageCollection;
import PolyGlot.ManagersCollections.OptionsManager;
import PolyGlot.ManagersCollections.ReversionManager;
import PolyGlot.ManagersCollections.RomanizationManager;
import PolyGlot.ManagersCollections.ToDoManager;
import PolyGlot.ManagersCollections.VisualStyleManager;
import PolyGlot.ManagersCollections.WordClassCollection;
import PolyGlot.Screens.ScrMainMenu;
import java.awt.Color;
import java.awt.FontFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class DictCore {

    private final String version = "2.4";
    private ConWordCollection wordCollection;
    private TypeCollection typeCollection;
    private DeclensionManager declensionMgr;
    private PropertiesManager propertiesManager;
    private PronunciationMgr pronuncMgr;
    private RomanizationManager romMgr;
    private FamilyManager famManager;
    private LogoCollection logoCollection;
    private GrammarManager grammarManager;
    private OptionsManager optionsManager;
    private WordClassCollection wordPropCollection;
    private ImageCollection imageCollection;
    private EtymologyManager etymologyManager;
    private VisualStyleManager visualStyleManager;
    private ReversionManager reversionManager;
    private ToDoManager toDoManager;
    private ScrMainMenu rootWindow;
    private Object clipBoard;
    private boolean curLoading = false;
    private final Map<String, Integer> versionHierarchy = new HashMap<>();
    private Instant lastSaveTime = Instant.MIN;

    /**
     * Language core initialization
     *
     */
    public DictCore() {
        initializeDictCore();
    }
    
    /**
     * Initializes a new core based on the old one. Options, contents of prior clipboard, and the prior root
     * window are retained.
     * @param oldCore 
     */
    public DictCore(DictCore oldCore) {
        initializeDictCore();
        
        optionsManager = oldCore.optionsManager;
        optionsManager.setCore(this);
        clipBoard = oldCore.clipBoard;
        rootWindow = oldCore.rootWindow;
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
            optionsManager = new OptionsManager(this);
            wordPropCollection = new WordClassCollection(this);
            imageCollection = new ImageCollection();
            etymologyManager = new EtymologyManager(this);
            visualStyleManager = new VisualStyleManager(this);
            reversionManager = new ReversionManager(this);
            toDoManager = new ToDoManager();

            PAlphaMap alphaOrder = propertiesManager.getAlphaOrder();

            wordCollection.setAlphaOrder(alphaOrder);
            typeCollection.setAlphaOrder(alphaOrder);
            logoCollection.setAlphaOrder(alphaOrder);
            wordPropCollection.setAlphaOrder(alphaOrder);
            rootWindow = null;
            
            populateVersionHierarchy();
            validateVersion();
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("CORE ERROR", "Error creating language core: " + e.getLocalizedMessage(), null);
        }
    }

    /**
     * Gets conlang name or CONLANG. Put on core because it's used a lot.
     *
     * @return either name of conlang or "Conlang"
     */
    public String conLabel() {
        return propertiesManager.getLangName().length() == 0
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
        return propertiesManager.getLocalLangName().length() == 0
                ? "Local Lang"
                : propertiesManager.getLocalLangName();
    }

    public void setRootWindow(ScrMainMenu _rootWindow) {
        rootWindow = _rootWindow;
    }

    public OptionsManager getOptionsManager() {
        return optionsManager;
    }

    public ImageCollection getImageCollection() {
        return imageCollection;
    }
    
    public VisualStyleManager getVisualStyleManager() {
        return visualStyleManager;
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
     * Checks whether PolyGlot has focus, and sets main menu to always on top
     * ONLY if so
     */
    public void checkProgramFocus() {
        // currently does nothing. Previously used for assisting program focus.
    }

    /**
     * Retrieves working directory of PolyGlot
     *
     * @return current working directory
     */
    public String getWorkingDirectory() {
        String ret = propertiesManager.getOverrideProgramPath();

        try {
            ret = ret.isEmpty() ? DictCore.class.getProtectionDomain().getCodeSource().getLocation().toURI().g‌​etPath() : ret;
        } catch (URISyntaxException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("PATH ERROR", "Unable to resolve root path of PolyGlot:\n"
                    + e.getLocalizedMessage(), rootWindow);
        }

        // in some circumstances (but not others) the name of the jar will be appended... remove
        if (ret.endsWith(PGTUtil.jarArchiveName)) {
            ret = ret.replace(PGTUtil.jarArchiveName, "");
        }

        return ret;
    }

    public WordClassCollection getWordPropertiesCollection() {
        return wordPropCollection;
    }

    /**
     * Clipboard can be used to hold any object
     *
     * @param c object to hold
     */
    public void setClipBoard(Object c) {
        clipBoard = c;
    }

    /**
     * Retrieves object held in clipboard, even if null, regardless of type
     *
     * @return contents of clipboard
     */
    public Object getClipBoard() {
        return clipBoard;
    }

    /**
     * Pushes save signal to main interface menu
     */
    public void coreSave() {
        ((ScrMainMenu) rootWindow).saveFile();
    }

    /**
     * Pushes save signal to main interface menu
     */
    public void coreOpen() {
        ((ScrMainMenu) rootWindow).open();
    }

    /**
     * Pushes save signal to main interface menu
     *
     * @param performTest whether to prompt user to save
     */
    public void coreNew(boolean performTest) {
        ((ScrMainMenu) rootWindow).newFile(performTest);
    }

    /**
     * Pushes signal to all forms to update their values from the core. Cascades
     * through windows and their children.
     */
    public void pushUpdate() {
        pushUpdate(this);
    }
    
    /**
     * Pushes signal to all forms to update their values from the core. Cascades
     * through windows and their children.
     * @param _core new core to push
     */
    public void pushUpdate(DictCore _core) {
        StackTraceElement stack[] = Thread.currentThread().getStackTrace();

        // prevent recursion (exclude check of top method, obviously)
        for (int i = (stack.length - 1); i > 1; i--) {
            StackTraceElement element = stack[i];
            if (element.getMethodName().equals("pushUpdate")) {
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
     * Gets proper color for fields marked as required
     *
     * @return
     */
    public Color getRequiredColor() {
        return new Color(255, 204, 204);
    }

    /**
     * gets family manager
     *
     * @return FamilyManager object from core
     */
    public FamilyManager getFamManager() {
        return famManager;
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
     */
    public void buildLanguageReport() {
        final DictCore core = this;
        
        new Thread() {
            @Override
            public void run() {
                try {
                    // create temp file for report, then let OS handle call to browser
                    java.awt.Desktop.getDesktop().browse(IOHandler.createTmpURL(
                            PLanguageStats.buildWordReport(core)));
                } catch (IOException | URISyntaxException e) {
                    IOHandler.writeErrorLog(e);
                    InfoBox.error("Statistics Error", "Unable to generate/display language statistics: " 
                            + e.getLocalizedMessage(), getRootWindow());
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
            throw new IOException("Image loading error: " + e.getLocalizedMessage());
        }

        try {
            IOHandler.setFontFrom(_fileName, this);
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
            throw new IOException(e.getMessage());
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

        if (errorLog.trim().length() != 0) {
            throw new IOException(errorLog);
        }

        if (warningLog.trim().length() != 0) {
            throw new IllegalStateException(warningLog);
        }
    }
    
    /**
     * loads revision XML from revision byte array (does not support media revisions)
     * @param revision 
     * @param fileName 
     * @throws java.io.IOException 
     */
    public void revertToState(byte[] revision, String fileName) throws IOException, Exception {
        DictCore revDict = new DictCore();
        revDict.readFile(fileName, revision);
        revDict.setRootWindow(rootWindow);
        
        pushUpdate(revDict);
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
            throws ParserConfigurationException, TransformerException, FileNotFoundException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Instant newSaveTime = Instant.now();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement(PGTUtil.dictionaryXID);
        doc.appendChild(rootElement);

        // collect XML representation of all dictionary elements
        this.writeXMLHeader(doc, rootElement, newSaveTime);
        propertiesManager.writeXML(doc, rootElement);
        wordPropCollection.writeXML(doc, rootElement);
        typeCollection.writeXML(doc, rootElement);
        wordCollection.writeXML(doc, rootElement);
        getEtymologyManager().writeXML(doc, rootElement);
        declensionMgr.writeXML(doc, rootElement);
        pronuncMgr.writeXML(doc, rootElement);
        romMgr.writeXML(doc, rootElement);
        logoCollection.writeXML(doc, rootElement);
        grammarManager.writeXML(doc, rootElement);
        toDoManager.writeXML(doc, rootElement);

        // write family entries
        rootElement.appendChild(famManager.writeToSaveXML(doc));

        // have IOHandler write constructed document to file
        IOHandler.writeFile(_fileName, doc, this, newSaveTime);
        
        setLastSaveTime(newSaveTime);
    }
    
    private void writeXMLHeader(Document doc, Element rootElement, Instant saveTime) {
        Element headerElement = doc.createElement(PGTUtil.pgVersionXID);
        headerElement.appendChild(doc.createTextNode(version));
        rootElement.appendChild(headerElement);
        
        headerElement = doc.createElement(PGTUtil.dictionarySaveDate);
        headerElement.appendChild(doc.createTextNode(saveTime.toString()));
        rootElement.appendChild(headerElement);
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

    public TypeCollection getTypes() {
        return typeCollection;
    }

    public PronunciationMgr getPronunciationMgr() {
        return pronuncMgr;
    }

    public RomanizationManager getRomManager() {
        return romMgr;
    }
    
    public EtymologyManager getEtymologyManager() {
        return etymologyManager;
    }
    
    public ReversionManager getReversionManager() {
        return reversionManager;
    }
    
    public ToDoManager getToDoManager() {
        return toDoManager;
    }
    
    public Instant getLastSaveTime() {
        return lastSaveTime;
    }

    public void setLastSaveTime(Instant _lastSaveTime) {
        lastSaveTime = _lastSaveTime;
    }
    
    /**
     * 
     * @param version
     * @return 
     */
    public int getVersionHierarchy(String version) {
        int ret = -1;
        
        if (versionHierarchy.containsKey(version)) {
            ret = versionHierarchy.get(version);
        }
        
        return ret;
    }
    
    private void validateVersion() throws Exception {
        if (!versionHierarchy.containsKey(this.getVersion())) {
            throw new Exception("ERROR: CURRENT VERSION NOT ACCOUNTED FOR IN VERSION HISTORY.");
        }
    }
    
    public String getCurFileName() {
        return rootWindow.getCurFileName();
    }
    
    private void populateVersionHierarchy() {
        versionHierarchy.put("0", 0);
        versionHierarchy.put("0.5", 1);
        versionHierarchy.put("0.5.1", 2);
        versionHierarchy.put("0.6", 3);
        versionHierarchy.put("0.6.1", 4);
        versionHierarchy.put("0.6.5", 5);
        versionHierarchy.put("0.7", 6);
        versionHierarchy.put("0.7.5", 7);
        versionHierarchy.put("0.7.6", 8);
        versionHierarchy.put("0.7.6.1", 9);
        versionHierarchy.put("0.8", 10);
        versionHierarchy.put("0.8.1", 11);
        versionHierarchy.put("0.8.1.1", 12);
        versionHierarchy.put("0.8.1.2", 13);
        versionHierarchy.put("0.8.5", 14);
        versionHierarchy.put("0.9", 15);
        versionHierarchy.put("0.9.1", 16);
        versionHierarchy.put("0.9.2", 17);
        versionHierarchy.put("0.9.9", 18);
        versionHierarchy.put("0.9.9.1", 19);
        versionHierarchy.put("1.0", 20);
        versionHierarchy.put("1.0.1", 21);
        versionHierarchy.put("1.1", 22);
        versionHierarchy.put("1.2", 23);
        versionHierarchy.put("1.2.1", 24);
        versionHierarchy.put("1.2.2", 25);
        versionHierarchy.put("1.3", 26);
        versionHierarchy.put("1.4", 27);
        versionHierarchy.put("2.0", 28);
        versionHierarchy.put("2.1", 29);
        versionHierarchy.put("2.2", 30);
        versionHierarchy.put("2.3", 31);
        versionHierarchy.put("2.3.1", 32);
        versionHierarchy.put("2.3.2", 33);
        versionHierarchy.put("2.3.3", 34);
        versionHierarchy.put("2.4", 35);
    }
    
    /**
     * @param args the command line arguments args[0] = open file path (blank if none) args[1] = working directory of
     * PolyGlot (blank if none)
     */
    public static void main(final String args[]) {
        setupNumbus();

        java.awt.EventQueue.invokeLater(() -> {
            // catch all top level application killing throwables (and bubble up directly to ensure reasonable behavior)
            try {
                String overridePath = args.length > 1 ? args[1] : "";
                ScrMainMenu s = null;

                // set DPI scaling to false (requires Java 9)
                System.getProperties().setProperty("Dsun.java2d.dpiaware", "false");

                if (canStart()) {
                    try {
                        // separated due to serious nature of Thowable vs Exception
                        s = new ScrMainMenu(overridePath);
                        s.checkForUpdates(false);
                        s.setVisible(true);

                        // open file if one is provided via arguments
                        if (args.length > 0) {
                            s.setFile(args[0]);
                            s.openLexicon();
                        }
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        InfoBox.error("Unable to start", "Unable to open PolyGlot main frame: \n"
                                + e.getMessage() + "\n"
                                        + "Please contact developer (draquemail@gmail.com) for assistance.", null);

                        if (s != null) {
                            s.dispose();
                        }
                        // ex.printStackTrace();
                    }
                }
            } catch (Throwable t) {
                InfoBox.error("PolyGlot Error", "A serious error has occurred: " + t.getLocalizedMessage(), null);
                IOHandler.writeErrorLog(t);
                throw t;
            }
        });
    }
    
    private static void setupNumbus() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException e) {
            java.util.logging.Logger.getLogger(ScrMainMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
            IOHandler.writeErrorLog(e);
        }
    }
    
    /**
     * Tests whether PolyGlot can start, informs user of startup problems.
     * @return 
     */
    private static boolean canStart() {
        String startProblems = "";
        boolean ret = true;
        
        // Test for minimum version of Java (8)
        String jVer = System.getProperty("java.version");
        if (jVer.startsWith("1.5") || jVer.startsWith("1.6") || jVer.startsWith("1.7")) {
            startProblems += "Unable to start PolyGlot without Java 8 or higher.\n";
        }

        // keep people from running PolyGlot from within a zip file...
        if (System.getProperty("user.dir").toLowerCase().startsWith("c:\\windows\\system")) {
            startProblems += "PolyGlot cannot be run from within a zip archive. Please unzip all files to a folder.\n";
        }

        try {
            // Test for JavaFX and inform user that it is not present, they cannot run PolyGlot
            ScrMainMenu.class.getClassLoader().loadClass("javafx.embed.swing.JFXPanel");
        } catch (ClassNotFoundException e) {
            IOHandler.writeErrorLog(e);
            startProblems += "Unable to load Java FX. Download and install to use PolyGlot "
                    + "(JavaFX not included in some builds of Java 8 for Linux).\n";
        }
        
        if (startProblems.length() != 0) {
            InfoBox.error("Unable to start PolyGlot", startProblems, null);
            ret = false;
        }
        
        return ret;
    }
}
