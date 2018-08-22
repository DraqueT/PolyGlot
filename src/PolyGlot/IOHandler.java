/*
 * Copyright (c) 2014-2018, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 *  See LICENSE.TXT included with this code to read the full license agreement.

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

import PolyGlot.Nodes.LogoNode;
import PolyGlot.ManagersCollections.GrammarManager;
import PolyGlot.ManagersCollections.LogoCollection;
import PolyGlot.CustomControls.GrammarSectionNode;
import PolyGlot.CustomControls.GrammarChapNode;
import PolyGlot.CustomControls.InfoBox;
import PolyGlot.ManagersCollections.ImageCollection;
import PolyGlot.ManagersCollections.ReversionManager;
import PolyGlot.Nodes.ImageNode;
import PolyGlot.Nodes.ReversionNode;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.poi.util.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class handles file IO for PolyGlot
 *
 * @author draque
 */
public class IOHandler {

    /**
     * Opens and returns image from URL given (can be file path)
     *
     * @param filePath path of image
     * @return BufferedImage
     * @throws IOException in IO
     */
    public static BufferedImage getImage(String filePath) throws IOException {
        return ImageIO.read(new File(filePath));
    }

    /**
     * Queries user for image file, and returns it
     *
     * @param parent the parent window from which this is called
     * @return the image chosen by the user, null if canceled
     * @throws IOException If the image cannot be opened for some reason
     */
    public static BufferedImage openImageFile(Component parent) throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Images");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "gif", "jpg", "jpeg", "bmp", "png", "wbmp");
        chooser.setFileFilter(filter);
        String fileName;
        chooser.setCurrentDirectory(new File("."));

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return null;
        }

        return getImage(fileName);
    }

    /**
     * Creates a temporary file with the contents handed in the arguments and
     * returns its URL location.
     *
     * @param contents Contents to put in file.
     * @return
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    public static URI createTmpURL(String contents) throws IOException, URISyntaxException {
        File tmpFile = File.createTempFile("POLYGLOT", ".html");
        Files.write(tmpFile.toPath(), contents.getBytes());
        tmpFile.deleteOnExit();
        URI ret = createURIFromFullPath(tmpFile.getAbsolutePath());

        return ret;
    }

    public static URI createURIFromFullPath(String path) throws URISyntaxException, IOException {
        String OS = System.getProperty("os.name");
        URI uri;

        if (OS.startsWith("Windows")) {
            String relLocation = path;
            relLocation = "file:///" + relLocation;
            relLocation = relLocation.replaceAll(" ", "%20");
            relLocation = relLocation.replaceAll("\\\\", "/");
            uri = new URI(relLocation);
        } else if (OS.startsWith("Mac")) {
            String relLocation;
            relLocation = path;
            relLocation = "file://" + relLocation;
            relLocation = relLocation.replaceAll(" ", "%20");
            uri = new URI(relLocation);
        } else {
            // TODO: Implement this for Linux
            throw new IOException("This is not yet implemented for OS: " + OS
                    + ". Please open readme.html in the application directory");
        }
        
        return uri;
    }

    /**
     * Sets the conlang and local lang fonts, if one exists and caches its file for quicksaving
     *
     * @param _path The path of the PGD file
     * @param core the dictionary core
     * @throws java.io.IOException
     * @throws java.awt.FontFormatException
     */
    public static void setFontFrom(String _path, DictCore core) throws IOException, FontFormatException {
        setFontFrom(_path, core, true);
        setFontFrom(_path, core, false);
    }
    
    
    private static void setFontFrom(String _path, DictCore core, boolean isConFont) throws IOException, FontFormatException {
        if (isFileZipArchive(_path)) {
            try (ZipFile zipFile = new ZipFile(_path)) {
                ZipEntry fontEntry = isConFont ? 
                        zipFile.getEntry(PGTUtil.conFontFileName) : 
                        zipFile.getEntry(PGTUtil.localFontFileName);

                if (fontEntry != null) {
                    final File tempFile = File.createTempFile("stream2file", ".tmp");
                    
                    tempFile.deleteOnExit();

                    try (FileOutputStream out = new FileOutputStream(tempFile)) {
                        try (InputStream inputStream = zipFile.getInputStream(fontEntry)) {
                            IOUtils.copy(inputStream, out);
                        }

                        try {
                            Font font = Font.createFont(Font.TRUETYPE_FONT, tempFile);

                            if (font == null) {
                                return;
                            }

                            font = wrapFont(font);
                            
                            byte[] cachedFont;
                            try (InputStream inputStream = new FileInputStream(tempFile)) {
                                cachedFont = IOUtils.toByteArray(inputStream);
                            }
                            if (isConFont) {
                                core.getPropertiesManager().setFontConRaw(font);
                                core.getPropertiesManager().setCachedFont(cachedFont);
                            } else {
                                core.getPropertiesManager().setLocalFont(font);
                                core.getPropertiesManager().setCachedLocalFont(cachedFont);
                            }
                        } catch (FontFormatException e) {
                            throw new FontFormatException("Could not load language font. Possible incompatible font: " + e.getMessage());
                        } catch (IOException e) {
                            throw new IOException("Could not load language font. I/O exception: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Used for snagging cachable versions of files
     * @param filePath path of file to fetch as byte array
     * @return byte array of file at given path
     * @throws java.io.FileNotFoundException
     */
    public static byte[] getFileByteArray(String filePath) throws FileNotFoundException, IOException {
        byte[] ret;
        final File toByteArrayFile = new File(filePath);
        
        try (InputStream inputStream = new FileInputStream(toByteArrayFile)) {
            ret = IOUtils.toByteArray(inputStream);
        }
        
        return ret;
    }
    
    /**
     * Fetches and returns a font from a given location
     * @param filePath
     * @return collected font
     * @throws java.awt.FontFormatException
     * @throws java.io.IOException
     */
    public static Font getFontFromFile(String filePath) throws FontFormatException, IOException {
        File fontFile = new File(filePath);
        return wrapFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
    }

    /**
     * Given file name, returns appropriate cust handler
     *
     * @param _fileName full path of target file to read
     * @param _core dictionary core
     * @return cushandler class
     * @throws java.io.IOException on read problem
     */
    public static CustHandler getHandlerFromFile(String _fileName, DictCore _core) throws IOException {
        CustHandler ret = null;

        if (IOHandler.isFileZipArchive(_fileName)) {
            try (ZipFile zipFile = new ZipFile(_fileName)) {
                ZipEntry xmlEntry = zipFile.getEntry(PGTUtil.dictFileName);
                try (InputStream ioStream = zipFile.getInputStream(xmlEntry)) {
                    ret = CustHandlerFactory.getCustHandler(ioStream, _core);
                } catch (Exception e) {
                    throw new IOException(e.getLocalizedMessage());
                }
            }
        } else {
            try (InputStream ioStream = new FileInputStream(_fileName)) {
                ret = CustHandlerFactory.getCustHandler(ioStream, _core);
            } catch (Exception e) {
                throw new IOException(e.getLocalizedMessage());
            }
        }

        return ret;
    }
    
    /**
     * Creates a custhandler object from a reversion byte array of a language state
     * @param byteArray byte array containing XML of language state
     * @param _core dictionary core
     * @return new custhandler class
     * @throws IOException on parse error
     */
    public static CustHandler getHandlerFromByteArray(byte[] byteArray, DictCore _core) throws IOException {
        try {
            return CustHandlerFactory.getCustHandler(new ByteArrayInputStream(byteArray), _core);
        } catch (Exception e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    /**
     * Opens an image via GUI and returns as buffered image Returns null if user
     * cancels.
     *
     * @param parent parent window of operation
     * @return buffered image selected by user
     * @throws IOException on file read error
     */
    public static BufferedImage openImage(Window parent) throws IOException {
        BufferedImage ret = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Image");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "tiff", "bmp", "png");
        chooser.setFileFilter(filter);
        String fileName;
        chooser.setCurrentDirectory(new File("."));

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
            ret = ImageIO.read(new File(fileName));
        }

        return ret;
    }

    /**
     * returns name of file sans path
     *
     * @param fullPath full path to file
     * @return string of filename
     */
    public static String getFilenameFromPath(String fullPath) {
        File file = new File(fullPath);
        return file.getName();
    }

    /**
     * Loads all option data from ini file, if none, ignore. One will be created
     * on exit.
     *
     * @param core dictionary core
     * @throws IOException on failure to open existing file
     */
    public static void loadOptionsIni(DictCore core) throws Exception {
        File f = new File(core.getWorkingDirectory() + PGTUtil.polyGlotIni);
        if (!f.exists() || f.isDirectory()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(
                core.getWorkingDirectory() + PGTUtil.polyGlotIni))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] bothVal = line.split("=");

                // if no value set, move on
                if (bothVal.length == 1) {
                    continue;
                }

                // if multiple values, something has gone wrong
                if (bothVal.length != 2) {
                    throw new Exception("PolyGlot.ini corrupt or unreadable.");
                }

                switch (bothVal[0]) {
                    case PGTUtil.optionsLastFiles:
                        core.getOptionsManager().getLastFiles().addAll(Arrays.asList(bothVal[1].split(",")));
                        break;
                    case PGTUtil.optionsScreensOpen:
                        for (String screen : bothVal[1].split(",")) {
                            core.getOptionsManager().addScreenUp(screen);
                        }
                        break;
                    case PGTUtil.optionsScreenPos:
                        for (String curPosSet : bothVal[1].split(",")) {
                            if (curPosSet.isEmpty()) {
                                continue;
                            }

                            String[] splitSet = curPosSet.split(":");

                            if (splitSet.length != 3) {
                                throw new Exception("Malformed Screen Position: "
                                        + curPosSet);
                            }
                            Point p = new Point(Integer.parseInt(splitSet[1]), Integer.parseInt(splitSet[2]));
                            core.getOptionsManager().setScreenPosition(splitSet[0], p);
                        }
                        break;
                    case PGTUtil.optionsScreensSize:
                        for (String curSizeSet : bothVal[1].split(",")) {
                            if (curSizeSet.isEmpty()) {
                                continue;
                            }

                            String[] splitSet = curSizeSet.split(":");

                            if (splitSet.length != 3) {
                                throw new Exception("Malformed Screen Size: "
                                        + curSizeSet);
                            }
                            Dimension d = new Dimension(Integer.parseInt(splitSet[1]), Integer.parseInt(splitSet[2]));
                            core.getOptionsManager().setScreenSize(splitSet[0], d);
                        }
                        break;
                    case PGTUtil.optionsAutoResize:
                        core.getOptionsManager().setAnimateWindows(bothVal[1].equals(PGTUtil.True));
                        break;
                    case PGTUtil.optionsMenuFontSize:
                        core.getOptionsManager().setMenuFontSize(Double.parseDouble(bothVal[1]));
                        break;
                    case PGTUtil.optionsNightMode:
                        core.getOptionsManager().setNightMode(bothVal[1].equals(PGTUtil.True));
                        break;
                    case PGTUtil.optionsReversionsCount:
                        core.getOptionsManager().setMaxReversionCount(Integer.parseInt(bothVal[1]));
                        break;
                    case "\n":
                        break;
                    default:
                        throw new Exception("Unrecognized value: " + bothVal[0]
                                + " in PolyGlot.ini.");
                }
            }
        }
    }

    /**
     * Given handler class, parses XML document within file (archive or not)
     *
     * @param _fileName full path of target file
     * @param _handler custom handler to consume XML document
     * @throws IOException on read error
     * @throws ParserConfigurationException on parser factory config error
     * @throws SAXException on XML interpretation error
     */
    public static void parseHandler(String _fileName, CustHandler _handler)
            throws IOException, ParserConfigurationException, SAXException {
        try (ZipFile zipFile = new ZipFile(_fileName)) {
            ZipEntry xmlEntry = zipFile.getEntry(PGTUtil.dictFileName);
            try (InputStream ioStream = zipFile.getInputStream(xmlEntry)) {
                parseHandlerInternal(ioStream, _handler);
            }
        }
    }
    
    public static void parseHandlerByteArray(byte[] reversion, CustHandler _handler) 
            throws ParserConfigurationException, IOException, SAXException {
        parseHandlerInternal(new ByteArrayInputStream(reversion), _handler);
    }
    
    private static void parseHandlerInternal(InputStream stream, CustHandler _handler) 
            throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(stream, _handler);
    }

    /**
     * Tests whether or not a file is a zip archive
     *
     * @param _fileName the file to test
     * @return true is passed file is a zip archive
     * @throws java.io.FileNotFoundException
     */
    public static boolean isFileZipArchive(String _fileName) throws FileNotFoundException, IOException {
        File file = new File(_fileName);

        // ignore directories and files too small to possibly be archives
        if (file.isDirectory()
                || file.length() < 4) {
            return false;
        }

        int test;
        try (FileInputStream fileStream = new FileInputStream(file)) {
            try (BufferedInputStream buffer = new BufferedInputStream(fileStream)) {
                try (DataInputStream in = new DataInputStream(buffer)) {
                    test = in.readInt();
                }
            }
        }
        return test == 0x504b0304;
    }

    private static String writeFont(ZipOutputStream out, Font ouputFont, byte[] cachedFont, DictCore core, boolean isConFont) {
        String writeLog = "";
        try {
            // only search for font if the cached font is null
            if (cachedFont == null) {
                // embed font in PGD archive if applicable
                File fontFile = null;
                try {
                    fontFile = IOHandler.getFontFile(ouputFont);
                } catch (Exception ex) {
                    writeLog += "\nerror: " + ex.getLocalizedMessage();
                }


                if (fontFile != null) {
                    try (FileInputStream fontInputStream = new FileInputStream(fontFile)) {
                        if (isConFont) {
                            core.getPropertiesManager().setCachedFont(IOUtils.toByteArray(fontInputStream));
                        } else {
                            core.getPropertiesManager().setCachedLocalFont(IOUtils.toByteArray(fontInputStream));
                        }
                    }
                    byte[] buffer = new byte[1024];
                    try (FileInputStream fis = new FileInputStream(fontFile)) {
                        if (isConFont) {
                            out.putNextEntry(new ZipEntry(PGTUtil.conFontFileName));
                        } else {
                            out.putNextEntry(new ZipEntry(PGTUtil.localFontFileName));
                        }
                        int length;

                        while ((length = fis.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }

                        out.closeEntry();
                    }
                }
            } else {
                if (isConFont) {
                    out.putNextEntry(new ZipEntry(PGTUtil.conFontFileName));
                } else {
                    out.putNextEntry(new ZipEntry(PGTUtil.localFontFileName));
                }
                out.write(cachedFont);
                out.closeEntry();
            }
        } catch (IOException e) {
            writeLog += "\nUnable to write font to archive: " + e.getMessage();
        }
        return writeLog;
    }
    
    public static void writeFile(String _fileName, Document doc, DictCore core, Instant saveTime) 
            throws IOException, TransformerException {
        File finalFile = new File(_fileName);
        String writeLog = "";
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        try (StringWriter writer = new StringWriter()) {
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            StringBuilder sb = new StringBuilder();
            sb.append(writer.getBuffer().toString());
            byte[] xmlData = sb.toString().getBytes("UTF-8");

            // save file to temp location initially.
            final File f = File.createTempFile(_fileName, null);
            try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
                try (ZipOutputStream out = new ZipOutputStream(fileOutputStream, Charset.forName("ISO-8859-1"))) {
                    ZipEntry e = new ZipEntry(PGTUtil.dictFileName);
                    out.putNextEntry(e);

                    out.write(xmlData, 0, xmlData.length);

                    out.closeEntry();
                    
                    writeLog += writeFont(out, 
                            core.getPropertiesManager().getFontCon(), 
                            core.getPropertiesManager().getCachedFont(),
                            core,
                            true);
                    
                    writeLog += writeFont(out, 
                            core.getPropertiesManager().getFontLocal(), 
                            core.getPropertiesManager().getCachedLocalFont(),
                            core,
                            false);

                    writeLog += writeLogoNodesToArchive(out, core);
                    writeLog += writeImagesToArchive(out, core);
                    writeLog += writeWavToArchive(out, core);
                    writeLog += writePriorStatesToArchive(out, core);

                    out.finish();
                    out.close();
                }
            }

            // attempt to open file in dummy core. On success, copy file to end
            // destination, on fail, delete file and inform user by bubbling error
            try {
                DictCore test = new DictCore();
                test.readFile(f.getAbsolutePath());

            } catch (IOException | IllegalStateException ex) {
                throw new IOException(ex);
            }

            try {
                java.nio.file.Files.copy(f.toPath(), finalFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new IOException("Unable to save file: " + ex.getMessage());
            }
            
            core.getReversionManager().addVersion(xmlData, saveTime);
        }

        if (writeLog.length() != 0) {
            InfoBox.warning("File Save Issues", "Problems encountered when saving file " + _fileName + writeLog, null);
        }
    }
    
    private static String writePriorStatesToArchive(ZipOutputStream out, DictCore core) throws IOException {
        String writeLog = "";
        List<ReversionNode> reversionList = core.getReversionManager().getReversionList();
        
        try {
            out.putNextEntry(new ZipEntry(PGTUtil.reversionSavePath));

            for (Integer i = 0; i < reversionList.size(); i++) {
                ReversionNode node = reversionList.get(i);
                
                out.putNextEntry(new ZipEntry(PGTUtil.reversionSavePath + PGTUtil.reversionBaseFileName 
                        + i.toString()));
                out.write(node.value);
                out.closeEntry();
            }
        } catch (IOException e) {
            throw new IOException("Unable to create reversion files.");
        }
        
        return writeLog;
    }
    
    private static String writeLogoNodesToArchive(ZipOutputStream out, DictCore core) {
        String writeLog = "";
        List<LogoNode> logoNodes = core.getLogoCollection().getAllLogos();
        if (!logoNodes.isEmpty()) {
            try {
                out.putNextEntry(new ZipEntry(PGTUtil.logoGraphSavePath));
                for (LogoNode curNode : logoNodes) {
                    try {
                        out.putNextEntry(new ZipEntry(PGTUtil.logoGraphSavePath
                                + curNode.getId().toString() + ".png"));

                        ImageIO.write(curNode.getLogoGraph(), "png", out);

                        out.closeEntry();
                    } catch (IOException ex) {
                        writeLog += "\nUnable to save logograph: " + ex.getLocalizedMessage();
                    }
                }
            } catch (IOException ex) {
                writeLog += "\nUnable to save Logographs: " + ex.getLocalizedMessage();
            }
        }
        
        return writeLog;
    }
    
    private static String writeWavToArchive(ZipOutputStream out, DictCore core) {
        String writeLog = "";
        Map<Integer, byte[]> grammarSoundMap = core.getGrammarManager().getSoundMap();
        Iterator<Entry<Integer, byte[]>> gramSoundIt = grammarSoundMap.entrySet().iterator();
        if (gramSoundIt.hasNext()) {
            try {
                out.putNextEntry(new ZipEntry(PGTUtil.grammarSoundSavePath));

                while (gramSoundIt.hasNext()) {
                    Entry<Integer, byte[]> curEntry = gramSoundIt.next();
                    Integer curId = curEntry.getKey();
                    byte[] curSound = curEntry.getValue();

                    try {
                        out.putNextEntry(new ZipEntry(PGTUtil.grammarSoundSavePath
                                + curId.toString() + ".raw"));
                        out.write(curSound);
                        out.closeEntry();
                    } catch (IOException ex) {
                        writeLog += "\nUnable to save sound: " + ex.getLocalizedMessage();
                    }

                }
            } catch (IOException ex) {
                writeLog += "\nUnable to save sounds: " + ex.getLocalizedMessage();
            }
        }
        return writeLog;
    }
    
    private static String writeImagesToArchive(ZipOutputStream out, DictCore core) {
        String writeLog = "";
        List<ImageNode> imageNodes = core.getImageCollection().getAllImages();
        if (!imageNodes.isEmpty()) {
            try {
                out.putNextEntry(new ZipEntry(PGTUtil.imagesSavePath));
                for (ImageNode curNode : imageNodes) {
                    try {
                        out.putNextEntry(new ZipEntry(PGTUtil.imagesSavePath
                                + curNode.getId().toString() + ".png"));

                        ImageIO.write(curNode.getImage(), "png", out);

                        out.closeEntry();
                    } catch (IOException ex) {
                        writeLog += "\nUnable to save image: " + ex.getLocalizedMessage();
                    }
                }
            } catch (IOException ex) {
                writeLog += "\nUnable to save Images: " + ex.getLocalizedMessage();
            }
        }
        return writeLog;
    }

    public static byte[] getFontFileArray(Font font) throws Exception {
        return Files.readAllBytes(getFontFile(font).toPath());
    }
    
    /**
     * Performs wrapping operations on fonts (such as enabling ligatures) and returns the wrapped font
     * @param font
     * @return 
     */
    private static Font wrapFont(Font font) {
        Map attributes = font.getAttributes();
        attributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
        attributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        return font.deriveFont(attributes);
    }

    /**
     * Attempts to load the given font from the OS's font folder (due to Java's ligature problems)
     * @param font
     * @return returns loaded font file on success, null otherwise
     */
    public static Font loadFontFromOSFileFolder(Font font) {
        Font ret = null;
        try {
            File fontFile = getFontFile(font);
            if (fontFile != null && fontFile.exists()) {
                ret = wrapFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
            }
        } catch (Exception e) {
            // do nothing here. Failure means returning null
        }
        
        return ret;
    }
    
    /**
     * gets the file of the current conlang font from the user's system
     *
     * @param font the font to find a file for
     * @return the font's file if found, null otherwise
     * @throws java.lang.Exception for unrecognized OS
     */
    public static File getFontFile(Font font) throws Exception {
        File ret = null;

        if (font == null) {
            return ret;
        }

        if (System.getProperty("os.name").startsWith("Mac")) {
            ret = IOHandler.getFontFromLocation("/Library/Fonts/", font);

            if (ret == null) {
                ret = IOHandler.getFontFromLocation("/System/Library/Fonts/", font);
            }

            if (ret == null) {
                ret = IOHandler.getFontFromLocation(System.getProperty("user.home")
                        + "/Library/Fonts/", font);
            }
        } else if (System.getProperty("os.name").startsWith("Win")) {
            ret = getFontFromLocation(System.getenv("WINDIR") + "\\Fonts", font);
        } else if (System.getProperty("os.name").indexOf("nix") > 0
                || System.getProperty("os.name").indexOf("bunt") > 0
                || System.getProperty("os.name").indexOf("fed") > 0
                || System.getProperty("os.name").indexOf("nux") > 0) {
            ret = getFontFromLocation("/usr/share/fonts", font);
        } else {
            throw new Exception("Unknown OS; unable to retrieve font.");
        }

        return ret;
    }

    /**
     * Returns a font's file based on the font and a path Recurses on any
     * subdirectories found
     *
     * @param path path to check for a font
     * @param font font to check for
     * @return the font's file, null otherwise
     */
    private static File getFontFromLocation(String path, Font font) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        File ret = null;

        if (listOfFiles.length == 0) {
            return null;
        }

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                File fontFile = loadFont(listOfFile.getPath(), font);

                if (fontFile != null) {
                    ret = fontFile;
                    break;
                }
            } else if (listOfFile.isDirectory()) {
                File fontFile = getFontFromLocation(path + "/"
                        + listOfFile.getName(), font);

                if (fontFile != null) {
                    ret = fontFile;
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * compares testfont to loaded file. returns file if it represents the font
     *
     * @param path full path of file to test
     * @param testFont font to test against
     * @return file if path leads to passed font, null otherwise
     */
    private static File loadFont(String path, Font testFont) {
        File fontFile = new File(path);
        File ret = null;

        // unrecgnized types won't be loaded
        if (path.toLowerCase().endsWith(".ttf")
                || path.toLowerCase().endsWith(".otf")
                || path.toLowerCase().endsWith(".ttc")
                || path.toLowerCase().endsWith(".dfont")) {
            try {
                Font f = Font.createFont(Font.TRUETYPE_FONT, fontFile);

                // if names match, set ret to return file
                if (f.getFamily().equals(testFont.getFamily())
                        || f.getFamily().equals(testFont.getFamily())) {
                    ret = fontFile;
                }

            } catch (FontFormatException | IOException e) {
                // null detected and message bubbled to user elsewhere
                ret = null;
                // e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Returns the default path of PolyGlot's running directory NOTE: If the
     * path is overridden, in the properties manager, use that. This returns
     * only what the OS tells PolyGlot it is running under (not always
     * trustworthy)
     *
     * @return default path
     */
    public static File getBaseProgramPath() {
        return new File(".");
    }

    /**
     * Tests whether a file at a particular location exists. Wrapped to avoid IO
     * code outside this file
     *
     * @param fullPath path of file to test
     * @return true if file exists, false otherwise
     */
    public static boolean fileExists(String fullPath) {
        File f = new File(fullPath);
        return f.exists();
    }

    /**
     * Loads image assets from file. Does not load logographs due to legacy
     * coding/logic
     *
     * @param imageCollection from dictCore to populate
     * @param fileName of file containing assets
     * @throws java.io.IOException
     */
    public static void loadImageAssets(ImageCollection imageCollection,
            String fileName) throws IOException, Exception {
        try (ZipFile zipFile = new ZipFile(fileName)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry;
            while (entries.hasMoreElements()) { // find images directory (zip paths are linear, only simulating tree structure)
                entry = entries.nextElement();
                if (!entry.getName().equals(PGTUtil.imagesSavePath)) {
                    continue;
                }
                break;
            }

            while (entries.hasMoreElements()) {
                entry = entries.nextElement();

                if (entry.isDirectory()) { // kills process after last image found
                    break;
                }

                BufferedImage img;
                try (InputStream imageStream = zipFile.getInputStream(entry)) {
                    String name = entry.getName().replace(".png", "")
                            .replace(PGTUtil.imagesSavePath, "");
                    int imageId = Integer.parseInt(name);
                    img = ImageIO.read(imageStream);
                    ImageNode imageNode = new ImageNode();
                    imageNode.setId(imageId);
                    imageNode.setImage(img);
                    imageCollection.getBuffer().setEqual(imageNode);
                    imageCollection.insert(imageId);
                }
            }
        }
    }

    /**
     * Encapsulates image loading to keep IO within IOHandler class
     *
     * @param fileName path of image
     * @return buffered image loaded
     * @throws IOException on file read error
     */
    public BufferedImage fetchImageFromLocation(String fileName) throws IOException {
        try (InputStream imageStream = new FileInputStream(fileName)) {
            return ImageIO.read(imageStream);
        }
    }

    public static byte[] getBufferedImageByteArray(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }

    /**
     * loads all images into their logographs from archive and images into the
     * generalized image collection
     *
     * @param logoCollection logocollection from dictionary core
     * @param fileName name/path of archive
     * @throws java.lang.Exception
     */
    public static void loadLogographs(LogoCollection logoCollection,
            String fileName) throws Exception {
        Iterator<LogoNode> it = logoCollection.getAllLogos().iterator();
        try (ZipFile zipFile = new ZipFile(fileName)) {
            while (it.hasNext()) {
                LogoNode curNode = it.next();
                ZipEntry imgEntry = zipFile.getEntry(PGTUtil.logoGraphSavePath
                        + curNode.getId().toString() + ".png");

                BufferedImage img;
                try (InputStream imageStream = zipFile.getInputStream(imgEntry)) {
                    img = ImageIO.read(imageStream);
                }
                curNode.setLogoGraph(img);
            }
        }
    }
    
    /**
     * Loads all reversion XML files from polyglot archive
     * @param reversionManager reversion manager to load to
     * @param fileName full path of polyglot archive
     * @throws IOException on read error
     */
    public static void loadReversionStates(ReversionManager reversionManager,
            String fileName) throws IOException, Exception {
        try (ZipFile zipFile = new ZipFile(fileName)) {
            Integer i = 0;
            String errorLog = "";
            DictCore tmpCore;
            
            ZipEntry reversion = zipFile.getEntry(PGTUtil.reversionSavePath
                    + PGTUtil.reversionBaseFileName + i.toString());
            
            while (reversion != null) {
                tmpCore = new DictCore();
                errorLog += tmpCore.testLoadReversion(IOUtils.toByteArray(zipFile.getInputStream(reversion))) + "\n";
                
                reversionManager.addVersionToEnd(IOUtils.toByteArray(zipFile.getInputStream(reversion)),
                        tmpCore.getLastSaveTime());
                i++;
                reversion = zipFile.getEntry(PGTUtil.reversionSavePath
                        + PGTUtil.reversionBaseFileName + i.toString());
            }
            
            // remember to load latest state in addition to all prior ones
            reversion = zipFile.getEntry(PGTUtil.dictFileName);
            tmpCore = new DictCore();
            errorLog += tmpCore.testLoadReversion(IOUtils.toByteArray(zipFile.getInputStream(reversion))) + "\n";
            reversionManager.addVersionToEnd(IOUtils.toByteArray(zipFile.getInputStream(reversion)),
                        tmpCore.getLastSaveTime());
            
            // bubble any loading problems up as error
            if (!errorLog.isEmpty()) {
                throw new Exception(errorLog);
            }
        }
    }

    /**
     * Exports font in PGD to external file
     *
     * @param exportPath path to export to
     * @param dictionaryPath path of PGT dictionary
     * @throws IOException
     */
    public static void exportFont(String exportPath, String dictionaryPath) throws IOException {
        try (ZipFile zipFile = new ZipFile(dictionaryPath)) {
            if (zipFile == null) {
                throw new IOException("Dictionary must be saved before font can be exported.");
            }

            // ensure export file has the proper extension
            if (!exportPath.toLowerCase().endsWith(".ttf")) {
                exportPath += ".ttf";
            }

            ZipEntry fontEntry = zipFile.getEntry(PGTUtil.conFontFileName);

            if (fontEntry != null) {
                Path path = Paths.get(exportPath);
                try (InputStream copyStream = zipFile.getInputStream(fontEntry)) {
                    Files.copy(copyStream, path, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                throw new IOException("Custom font not found in PGD dictionary file.");
            }
        }
    }
    
    /**
     * Exports Charis unicode font to specified location
     * @param exportPath full export path
     * @throws IOException on failure
     */
    public static void exportCharisFont(String exportPath) throws IOException {
        try (InputStream fontStream = IOHandler.class.getResourceAsStream(PGTUtil.UnicodeFontLocation)){
            byte[] buffer = new byte[fontStream.available()];
            fontStream.read(buffer);

            try (OutputStream oStream = new FileOutputStream(new File(exportPath))) {
                oStream.write(buffer);
            }
        }
    }

    /**
     * Loads any related grammar recordings into the passed grammar manager via
     * id
     *
     * @param _fileName name of file to load sound recordings from
     * @param grammarManager grammar manager to populate with sounds
     * @throws Exception on sound load errors
     */
    static void loadGrammarSounds(String fileName, GrammarManager grammarManager) throws Exception {
        String loadLog = "";

        try (ZipFile zipFile = new ZipFile(fileName)) {
            Iterator<GrammarChapNode> chapIt = grammarManager.getChapters().iterator();

            while (chapIt.hasNext()) {
                GrammarChapNode curChap = chapIt.next();

                for (int i = 0; i < curChap.getChildCount(); i++) {
                    GrammarSectionNode curNode = (GrammarSectionNode) curChap.getChildAt(i);

                    if (curNode.getRecordingId() == -1) {
                        continue;
                    }

                    String soundPath = PGTUtil.grammarSoundSavePath
                            + curNode.getRecordingId().toString() + ".raw";
                    ZipEntry soundEntry = zipFile.getEntry(soundPath);

                    byte[] sound = null;

                    try (InputStream soundStream = zipFile.getInputStream(soundEntry)) {
                        sound = IOUtils.toByteArray(soundStream);
                    } catch (IOException e) {
                        loadLog += "\nUnable to load sound: " + e.getLocalizedMessage();
                    } catch (Exception e) {
                        loadLog += "\nUnable to load sound: " + e.getLocalizedMessage();
                    }

                    if (sound == null) {
                        continue;
                    }

                    grammarManager.addChangeRecording(curNode.getRecordingId(), sound);
                }
            }
        }

        if (loadLog.length() != 0) {
            throw new Exception(loadLog);
        }
    }

    public static Font getLcdFont() throws FontFormatException, IOException {
        return new IOHandler().getLcdFontInternal();
    }

    /**
     * Fetches and returns LCD style font NOTE 1: the font returned is very
     * small, use deriveFont() to make it a usable size NOTE 2: this is a
     * non-static method due to an input stream restriction
     *
     * @return LCD display font
     * @throws java.awt.FontFormatException if font corrupted
     * @throws java.io.IOException if unable to load font
     */
    private Font getLcdFontInternal() throws FontFormatException, IOException {
        try (InputStream tmp = this.getClass().getResourceAsStream(PGTUtil.LCDFontLocation)) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font ret = Font.createFont(Font.TRUETYPE_FONT, tmp);

            if (ret != null) {
                ge.registerFont(ret);
            }

            return ret;
        }
    }

    /**
     * Fetches and returns unicode compatible font NOTE 1: this is a non-static
     * method due to an input stream restriction NOTE 2: this is the default
     * conlang font in PolyGlot
     *
     * @return Charis unicode compatible font
     * @throws java.io.IOException
     */
    public static Font getCharisUnicodeFontInitial() throws IOException {
        return new IOHandler().getCharisUnicodeFontInternal(PGTUtil.UnicodeFontLocation);
    }

    /**
     * ditto
     *
     * @return
     * @throws java.io.IOException
     */
    public static Font getCharisUnicodeFontBoldInitial() throws IOException {
        return new IOHandler().getCharisUnicodeFontInternal(PGTUtil.UnicodeFontBoldLocation);
    }

    /**
     * ditto
     *
     * @return
     * @throws java.io.IOException
     */
    public static Font getCharisUnicodeFontItalicInitial() throws IOException {
        return new IOHandler().getCharisUnicodeFontInternal(PGTUtil.UnicodeFontItalicLocation);
    }

    /**
     * ditto
     *
     * @return
     * @throws java.io.IOException
     */
    public static Font getCharisUnicodeFontBoldItalicInitial() throws IOException {
        return new IOHandler().getCharisUnicodeFontInternal(PGTUtil.UnicodeFontBoldItalicLocation);
    }

    /**
     * Fetches and returns unicode compatible font NOTE 1: this is a non-static
     * method due to an input stream restriction NOTE 2: this is the default
     * conlang font in PolyGlot
     *
     * @return Charis unicode compatible font
     */
    private Font getCharisUnicodeFontInternal(String location) throws IOException {
        Font ret = null;
        try (InputStream tmp = this.getClass().getResourceAsStream(location)) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ret = Font.createFont(Font.TRUETYPE_FONT, tmp);

            if (ret != null) {
                ge.registerFont(ret);
            }

        } catch (IOException | FontFormatException e) {
            throw new IOException("Unable to load Charis (" + location + "): "
                    + e.getLocalizedMessage());
        }

        return ret;
    }

    /**
     * For getting Font based on javafx's needs NOTE 1: this is a non-static
     * method due to an input stream restriction NOTE 2: this is the default
     * conlang font in PolyGlot NOTE 2: this is the default conlang font in
     * PolyGlot
     *
     * @return
     */
    public InputStream getCharisInputStream() {
        return this.getClass().getResourceAsStream(PGTUtil.UnicodeFontLocation);
    }

    /**
     * Fetches and returns default button font
     *
     * @return Font to default buttons to
     * @throws java.io.IOException if unable to load font
     */
    public static Font getButtonFont() throws IOException {
        return new IOHandler().getButtonFontInternal();
    }

    /**
     * Fetches and returns default button font nonstatic
     *
     * @return Font to default buttons to
     * @throws java.io.IOException if unable to load font
     */
    private Font getButtonFontInternal() throws IOException {
        Font ret = null;
        try (InputStream tmp = this.getClass().getResourceAsStream(PGTUtil.ButtonFontLocation)) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ret = Font.createFont(Font.TRUETYPE_FONT, tmp);
            ret = ret.deriveFont(new Float(12)); // default to size 12 font
            if (ret != null) {
                ge.registerFont(ret);
            }
        } catch (Exception e) {
            throw new IOException("Unable to load button font.");
        }

        return ret;
    }

    /**
     * Tests whether the path can be written to
     *
     * @param path
     * @return
     */
    private static boolean testCanWrite(String path) {
        return new File(path).canWrite();
    }

    /**
     * Saves ini file with polyglot options
     *
     * @param core
     * @throws IOException on failure or lack of permission to write
     */
    public static void saveOptionsIni(DictCore core) throws IOException {

        try (Writer f0 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(core.getWorkingDirectory()
                        + PGTUtil.polyGlotIni), "UTF-8"))) {
            String newLine = System.getProperty("line.separator");
            String nextLine;

            if (!testCanWrite(core.getWorkingDirectory() + PGTUtil.polyGlotIni)) {
                throw new IOException("Unable to save settings. Polyglot does not have permission to write to folder: "
                        + core.getWorkingDirectory() + ". This is most common when running from Program Files in Windows.");
            }

            nextLine = PGTUtil.optionsLastFiles + "=";
            for (String file : core.getOptionsManager().getLastFiles()) {
                if (nextLine.endsWith("=")) {
                    nextLine += file;
                } else {
                    nextLine += ("," + file);
                }
            }
            f0.write(nextLine + newLine);

            nextLine = PGTUtil.optionsScreenPos + "=";
            for (Entry<String, Point> curPos : core.getOptionsManager().getScreenPositions().entrySet()) {
                nextLine += ("," + curPos.getKey() + ":" + curPos.getValue().x + ":"
                        + curPos.getValue().y);
            }
            f0.write(nextLine + newLine);

            nextLine = PGTUtil.optionsScreensSize + "=";
            for (Entry<String, Dimension> curSize : core.getOptionsManager().getScreenSizes().entrySet()) {
                nextLine += ("," + curSize.getKey() + ":" + curSize.getValue().width + ":"
                        + curSize.getValue().height);
            }

            f0.write(nextLine + newLine);
            nextLine = PGTUtil.optionsScreensOpen + "=";

            for (String screen : core.getOptionsManager().getLastScreensUp()) {
                nextLine += ("," + screen);
            }
            f0.write(nextLine + newLine);

            nextLine = PGTUtil.optionsAutoResize + "=" 
                    + (core.getOptionsManager().isAnimateWindows() ? PGTUtil.True : PGTUtil.False);
            f0.write(nextLine + newLine);

            nextLine = PGTUtil.optionsMenuFontSize + "=" + Double.toString(core.getOptionsManager().getMenuFontSize());
            f0.write(nextLine + newLine);
            
            nextLine = PGTUtil.optionsNightMode + "="
                    + (core.getOptionsManager().isNightMode() ? PGTUtil.True : PGTUtil.False);
            f0.write(nextLine + newLine);
            
            nextLine = PGTUtil.optionsReversionsCount + "=" + core.getOptionsManager().getMaxReversionCount();
            f0.write(nextLine + newLine);
        }
    }

    /**
     * Gets Unicode compatible font as byte array
     *
     * @return byte array of font's file
     * @throws FileNotFoundException if this throws, something is wrong
     * internally
     * @throws IOException if this throws, something is wrong internally
     */
    public byte[] getUnicodeFontByteArray() throws FileNotFoundException, IOException {
        try (InputStream localStream = this.getClass().getResourceAsStream(PGTUtil.UnicodeFontLocation)) {
            return IOUtils.toByteArray(localStream);
        }
    }

    /**
     * Gets Unicode compatible font as byte array
     *
     * @return byte array of font's file
     * @throws FileNotFoundException if this throws, something is wrong
     * internally
     * @throws IOException if this throws, something is wrong internally
     */
    public byte[] getUnicodeFontItalicByteArray() throws FileNotFoundException, IOException {
        try (InputStream localStream = this.getClass().getResourceAsStream(PGTUtil.UnicodeFontItalicLocation)) {
            return IOUtils.toByteArray(localStream);
        }
    }

    /**
     * Opens an arbitrary file via the local OS's default. If unable to open for
     * any reason, returns false.
     *
     * @param path
     * @return
     */
    public static boolean openFileNativeOS(String path) {
        boolean ret = true;

        try {
            File file = new File(path);
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            ret = false;
        }

        return ret;
    }
}
