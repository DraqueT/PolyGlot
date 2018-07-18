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
import PolyGlot.ManagersCollections.RomanizationManager;
import PolyGlot.ManagersCollections.WordClassCollection;
import PolyGlot.Screens.ScrMainMenu;
import java.awt.Color;
import java.awt.FontFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
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

    private final String version = "2.3";
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
    private PFrame rootWindow;
    private Object clipBoard;
    private boolean curLoading = false;
    private Map<String, Integer> versionHierarchy = new HashMap<>();

    /**
     * Language core initialization
     *
     */
    public DictCore() {
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
            wordPropCollection = new WordClassCollection();
            imageCollection = new ImageCollection();
            etymologyManager = new EtymologyManager(this);

            PAlphaMap alphaOrder = propertiesManager.getAlphaOrder();

            wordCollection.setAlphaOrder(alphaOrder);
            typeCollection.setAlphaOrder(alphaOrder);
            logoCollection.setAlphaOrder(alphaOrder);
            wordPropCollection.setAlphaOrder(alphaOrder);
            rootWindow = null;
            
            populateVersionHierarchy();
            validateVersion();
        } catch (Exception e) {
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

    public void setRootWindow(PFrame _rootWindow) {
        rootWindow = _rootWindow;
    }

    public OptionsManager getOptionsManager() {
        return optionsManager;
    }

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
        StackTraceElement stack[] = Thread.currentThread().getStackTrace();

        // prevent recursion (exclude check of top method, obviously)
        for (int i = (stack.length - 1); i > 1; i--) {
            StackTraceElement element = stack[i];
            if (element.getMethodName().equals("pushUpdate")) {
                return;
            }
        }

        if (rootWindow == null) {
            InfoBox.warning("Bad Update", "This warning indicates that a root"
                    + " window was null at the time of an update push.",
                    rootWindow);
        } else {
            rootWindow.updateAllValues(this);
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
        try {
            // create temp file for report, then let OS handle call to browser
            java.awt.Desktop.getDesktop().browse(IOHandler.createTmpURL(
                    PLanguageStats.buildWordReport(this)));
        } catch (IOException | URISyntaxException e) {
            InfoBox.error("Statistics Error", "Unable to generate/display language statistics: " 
                    + e.getLocalizedMessage(), getRootWindow());
        }
    }

    /**
     * Reads from given file
     *
     * @param _fileName filename to read from
     * @throws java.io.IOException for unrecoverable errors
     * @throws IllegalStateException for recoverable errors
     */
    public void readFile(String _fileName) throws IOException, IllegalStateException {
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
            CustHandler handler = IOHandler.getHandlerFromFile(_fileName, this);
            IOHandler.parseHandler(_fileName, handler);

            errorLog += handler.getErrorLog();
            warningLog += handler.getWarningLog();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IOException(e.getMessage());
        }

        try {
            IOHandler.setFontFrom(_fileName, this);
        } catch (IOException | FontFormatException e) {
            warningLog += e.getLocalizedMessage() + "\n";
        }

        try {
            IOHandler.loadGrammarSounds(_fileName, grammarManager);
        } catch (Exception e) {
            warningLog += e.getLocalizedMessage() + "\n";
        }

        try {
            logoCollection.loadRadicalRelations();
        } catch (Exception e) {
            warningLog += e.getLocalizedMessage() + "\n";
        }

        try {
            IOHandler.loadLogographs(logoCollection, _fileName);
        } catch (Exception e) {
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

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement(PGTUtil.dictionaryXID);
        doc.appendChild(rootElement);

        // store version of PolyGlot
        Element header = doc.createElement(PGTUtil.pgVersionXID);
        header.appendChild(doc.createTextNode(version));
        rootElement.appendChild(header);

        // collect XML representation of all dictionary elements
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

        // write family entries
        rootElement.appendChild(famManager.writeToSaveXML(doc));

        // have IOHandler write constructed document to file
        IOHandler.writeFile(_fileName, doc, this);
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
    }
}
