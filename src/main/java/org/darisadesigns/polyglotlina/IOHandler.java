/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina;

import org.darisadesigns.polyglotlina.Nodes.LogoNode;
import org.darisadesigns.polyglotlina.ManagersCollections.GrammarManager;
import org.darisadesigns.polyglotlina.ManagersCollections.LogoCollection;
import org.darisadesigns.polyglotlina.CustomControls.GrammarSectionNode;
import org.darisadesigns.polyglotlina.CustomControls.GrammarChapNode;
import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.ManagersCollections.ImageCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.OptionsManager;
import org.darisadesigns.polyglotlina.ManagersCollections.ReversionManager;
import org.darisadesigns.polyglotlina.Nodes.ImageNode;
import org.darisadesigns.polyglotlina.Nodes.ReversionNode;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class handles file IO for PolyGlot
 *
 * @author draque
 */
public final class IOHandler {

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
     * Creates and returns a temporary file with the contents specified.
     * File will be deleted on exit of PolyGlot.
     *
     * @param contents Contents to put in file.
     * @param extension extension name for tmp file (defaults to tmp if none given)
     * @return Temporary file with specified contents
     * @throws IOException on write error
     */
    public static File createTmpFileWithContents(String contents, String extension) throws IOException {
        File ret = File.createTempFile("POLYGLOT", extension);
        Files.write(ret.toPath(), contents.getBytes());
        ret.deleteOnExit();
        
        return ret;
    }

    public static byte[] getByteArrayFromFile(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return streamToBytArray(inputStream);
        }
    }

    /**
     * Takes input stream and converts it to a raw byte array
     *
     * @param is
     * @return raw byte representation of stream
     * @throws IOException
     */
    public static byte[] streamToBytArray(InputStream is) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            return buffer.toByteArray();
        }
    }

    /**
     * Used for snagging catchable versions of files
     *
     * @param filePath path of file to fetch as byte array
     * @return byte array of file at given path
     * @throws java.io.FileNotFoundException
     */
    public static byte[] getFileByteArray(String filePath) throws IOException {
        byte[] ret;
        final File toByteArrayFile = new File(filePath);

        try (InputStream inputStream = new FileInputStream(toByteArrayFile)) {
            ret = streamToBytArray(inputStream);
        }

        return ret;
    }

    /**
     * Given file name, returns appropriate cust handler
     *
     * @param _fileName full path of target file to read
     * @param _core dictionary core
     * @return CustHandler class
     * @throws java.io.IOException on read problem
     */
    public static CustHandler getHandlerFromFile(String _fileName, DictCore _core) throws IOException {
        CustHandler ret = null;

        if (isFileZipArchive(_fileName)) {
            try (ZipFile zipFile = new ZipFile(_fileName)) {
                ZipEntry xmlEntry = zipFile.getEntry(PGTUtil.LANG_FILE_NAME);
                try (InputStream ioStream = zipFile.getInputStream(xmlEntry)) {
                    ret = CustHandlerFactory.getCustHandler(ioStream, _core);
                } catch (Exception e) {
                    throw new IOException(e.getLocalizedMessage(), e);
                }
            }
        } else {
            try (InputStream ioStream = new FileInputStream(_fileName)) {
                ret = CustHandlerFactory.getCustHandler(ioStream, _core);
            } catch (Exception e) {
                throw new IOException(e.getLocalizedMessage(), e);
            }
        }

        return ret;
    }

    /**
     * Creates a custhandler object from a reversion byte array of a language
     * state
     *
     * @param byteArray byte array containing XML of language state
     * @param _core dictionary core
     * @return new custhandler class
     * @throws IOException on parse error
     */
    public static CustHandler getHandlerFromByteArray(byte[] byteArray, DictCore _core) throws IOException {
        try {
            return CustHandlerFactory.getCustHandler(new ByteArrayInputStream(byteArray), _core);
        } catch (Exception e) {
            throw new IOException(e.getLocalizedMessage(), e);
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
     * Deletes options file
     *
     * @param workingDirectory
     */
    public static void deleteIni(String workingDirectory) {
        File f = new File(workingDirectory + File.separator + PGTUtil.POLYGLOT_INI);
        if (!f.exists()) {
            return;
        }

        try {
            f.delete();
        } catch (Exception e) {
            // can't write to folder, so don't bother trying to write log file...
            // IOHandler.writeErrorLog(e);
            InfoBox.error("Permissions Error", "PolyGlot lacks permissions to write to its native folder.\n"
                    + "Please move to a folder with full write permissions: " + e.getLocalizedMessage(), null);
        }
    }

    /**
     * Loads all option data from ini file, if none, ignore.One will be created
 on exit.
     *
     * @param opMan
     * @param workingDirectory
     * @throws IOException on failure to open existing file
     */
    public static void loadOptionsIni(OptionsManager opMan, String workingDirectory) throws Exception {
        File f = new File(workingDirectory + File.separator + PGTUtil.POLYGLOT_INI);
        if (!f.exists() || f.isDirectory()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(
                workingDirectory + File.separator + PGTUtil.POLYGLOT_INI))) {
            String loadProblems = "";

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
                    case PGTUtil.OPTIONS_LAST_FILES:
                        for (String last : bothVal[1].split(",")) {
                            opMan.pushRecentFile(last);
                        }
                        break;
                    case PGTUtil.OPTIONS_SCREENS_OPEN:
                        for (String screen : bothVal[1].split(",")) {
                            opMan.addScreenUp(screen);
                        }
                        break;
                    case PGTUtil.OPTIONS_SCREEN_POS:
                        for (String curPosSet : bothVal[1].split(",")) {
                            if (curPosSet.isEmpty()) {
                                continue;
                            }

                            String[] splitSet = curPosSet.split(":");

                            if (splitSet.length != 3) {
                                loadProblems += "Malformed Screen Position: " + curPosSet + "\n";
                            }
                            Point p = new Point(Integer.parseInt(splitSet[1]), Integer.parseInt(splitSet[2]));
                            opMan.setScreenPosition(splitSet[0], p);
                        }
                        break;
                    case PGTUtil.OPTIONS_SCREENS_SIZE:
                        for (String curSizeSet : bothVal[1].split(",")) {
                            if (curSizeSet.isEmpty()) {
                                continue;
                            }

                            String[] splitSet = curSizeSet.split(":");

                            if (splitSet.length != 3) {
                                loadProblems += "Malformed Screen Size: " + curSizeSet + "\n";
                            }
                            Dimension d = new Dimension(Integer.parseInt(splitSet[1]), Integer.parseInt(splitSet[2]));
                            opMan.setScreenSize(splitSet[0], d);
                        }
                        break;
                    case PGTUtil.OPTIONS_AUTO_RESIZE:
                        opMan.setAnimateWindows(bothVal[1].equals(PGTUtil.TRUE));
                        break;
                    case PGTUtil.OPTIONS_MENU_FONT_SIZE:
                        opMan.setMenuFontSize(Double.parseDouble(bothVal[1]));
                        break;
                    case PGTUtil.OPTIONS_NIGHT_MODE:
                        opMan.setNightMode(bothVal[1].equals(PGTUtil.TRUE));
                        break;
                    case PGTUtil.OPTIONS_REVERSIONS_COUNT:
                        opMan.setMaxReversionCount(Integer.parseInt(bothVal[1]), null);
                        break;
                    case PGTUtil.OPTIONS_TODO_DIV_LOCATION:
                        opMan.setToDoBarPosition(Integer.parseInt(bothVal[1]));
                        break;
                    case "\n":
                        break;
                    default:
                        loadProblems += "Unrecognized value: " + bothVal[0] + " in PolyGlot.ini." + "\n";
                }
            }

            if (!loadProblems.isEmpty()) {
                throw new Exception("Problems loading ini file: \n" + loadProblems);
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
            ZipEntry xmlEntry = zipFile.getEntry(PGTUtil.LANG_FILE_NAME);
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
    public static boolean isFileZipArchive(String _fileName) throws IOException {
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

    public static void writeFile(String _fileName, Document doc, DictCore core, File workingDirectory, Instant saveTime)
            throws IOException, TransformerException {
        File finalFile = new File(_fileName);
        String writeLog = "";
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        try (StringWriter writer = new StringWriter()) {
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            StringBuilder sb = new StringBuilder();
            sb.append(writer.getBuffer());
            byte[] xmlData = sb.toString().getBytes(StandardCharsets.UTF_8);

            final File f = makeTempSaveFile(workingDirectory);

            try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
                try (ZipOutputStream out = new ZipOutputStream(fileOutputStream, StandardCharsets.UTF_8)) {
                    ZipEntry e = new ZipEntry(PGTUtil.LANG_FILE_NAME);
                    out.putNextEntry(e);

                    out.write(xmlData, 0, xmlData.length);

                    out.closeEntry();

                    writeLog += PFontHandler.writeFont(out,
                            core.getPropertiesManager().getFontCon(),
                            core.getPropertiesManager().getCachedFont(),
                            core,
                            true);

                    writeLog += PFontHandler.writeFont(out,
                            core.getPropertiesManager().getFontLocal(),
                            core.getPropertiesManager().getCachedLocalFont(),
                            core,
                            false);

                    writeLog += writeLogoNodesToArchive(out, core);
                    writeLog += writeImagesToArchive(out, core);
                    writeLog += writeWavToArchive(out, core);
                    writeLog += writePriorStatesToArchive(out, core);

                    out.finish();
                }
            }

            // attempt to open file in dummy core. On success, copy file to end
            // destination, on fail, delete file and inform user by bubbling error
            try {
                // pass null shell class because this will ultimately be discarded
                DictCore test = new DictCore(PolyGlot.getTestShell());
                test.readFile(f.getAbsolutePath());

            } catch (Exception ex) {
                throw new IOException(ex);
            }

            try {
                copyFile(f.toPath(), finalFile.toPath(), true);
                f.delete(); // wipe temp file if successful
            } catch (IOException e) {
                throw new IOException("Unable to save file: " + e.getMessage(), e);
            }

            core.getReversionManager().addVersion(xmlData, saveTime);
        }

        if (!writeLog.isEmpty()) {
            InfoBox.warning("File Save Issues", "Problems encountered when saving file " + _fileName + writeLog, null);
        }
    }
    
    private static File makeTempSaveFile(File workingDirectory) {
        final File ret = new File(workingDirectory + File.separator + PGTUtil.TEMP_FILE);
        if (ret.exists()) { // If PolyGlot is open and working, user has already been alerted and ignored it.
            ret.delete();
        }
        return ret;
    }
    
    public static void copyFile(Path fromLocation, Path toLocation, boolean replaceExisting) throws IOException {
        StandardCopyOption option = replaceExisting ? StandardCopyOption.REPLACE_EXISTING : StandardCopyOption.ATOMIC_MOVE;
        Files.copy(fromLocation, toLocation, option);
    }
    
    /**
     * Gets temporary save file if one exists, null otherwise
     * @param workingDirectory
     * @return 
     */
    public static File getTempSaveFileIfExists(File workingDirectory) {
        File ret = new File(workingDirectory + File.separator + PGTUtil.TEMP_FILE);
        return ret.exists() ? ret : null;
    }

    private static String writePriorStatesToArchive(ZipOutputStream out, DictCore core) throws IOException {
        String writeLog = "";
        List<ReversionNode> reversionList = core.getReversionManager().getReversionList();

        try {
            out.putNextEntry(new ZipEntry(PGTUtil.REVERSION_SAVE_PATH));

            for (int i = 0; i < reversionList.size(); i++) {
                ReversionNode node = reversionList.get(i);

                out.putNextEntry(new ZipEntry(PGTUtil.REVERSION_SAVE_PATH + PGTUtil.REVERSION_BASE_FILE_NAME + i));
                out.write(node.getValue());
                out.closeEntry();
            }
        } catch (IOException e) {
            throw new IOException("Unable to create reversion files.", e);
        }

        return writeLog;
    }

    private static String writeLogoNodesToArchive(ZipOutputStream out, DictCore core) {
        String writeLog = "";
        List<LogoNode> logoNodes = core.getLogoCollection().getAllLogos();
        if (!logoNodes.isEmpty()) {
            try {
                out.putNextEntry(new ZipEntry(PGTUtil.LOGOGRAPH_SAVE_PATH));
                for (LogoNode curNode : logoNodes) {
                    try {
                        out.putNextEntry(new ZipEntry(PGTUtil.LOGOGRAPH_SAVE_PATH
                                + curNode.getId() + ".png"));

                        BufferedImage write = PGTUtil.toBufferedImage(curNode.getLogoGraph());
                        ImageIO.write(write, "png", out);

                        out.closeEntry();
                    } catch (IOException e) {
                        writeErrorLog(e);
                        writeLog += "\nUnable to save logograph: " + e.getLocalizedMessage();
                    }
                }
            } catch (IOException e) {
                writeErrorLog(e);
                writeLog += "\nUnable to save Logographs: " + e.getLocalizedMessage();
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
                out.putNextEntry(new ZipEntry(PGTUtil.GRAMMAR_SOUNDS_SAVE_PATH));

                while (gramSoundIt.hasNext()) {
                    Entry<Integer, byte[]> curEntry = gramSoundIt.next();
                    Integer curId = curEntry.getKey();
                    byte[] curSound = curEntry.getValue();

                    try {
                        out.putNextEntry(new ZipEntry(PGTUtil.GRAMMAR_SOUNDS_SAVE_PATH
                                + curId + ".raw"));
                        out.write(curSound);
                        out.closeEntry();
                    } catch (IOException e) {
                        writeErrorLog(e);
                        writeLog += "\nUnable to save sound: " + e.getLocalizedMessage();
                    }

                }
            } catch (IOException e) {
                writeErrorLog(e);
                writeLog += "\nUnable to save sounds: " + e.getLocalizedMessage();
            }
        }
        return writeLog;
    }

    private static String writeImagesToArchive(ZipOutputStream out, DictCore core) {
        String writeLog = "";
        List<ImageNode> imageNodes = core.getImageCollection().getAllImages();
        if (!imageNodes.isEmpty()) {
            try {
                out.putNextEntry(new ZipEntry(PGTUtil.IMAGES_SAVE_PATH));
                for (ImageNode curNode : imageNodes) {
                    try {
                        out.putNextEntry(new ZipEntry(PGTUtil.IMAGES_SAVE_PATH
                                + curNode.getId() + ".png"));

                        ImageIO.write(curNode.getImage(), "png", out);

                        out.closeEntry();
                    } catch (IOException e) {
                        writeErrorLog(e);
                        writeLog += "\nUnable to save image: " + e.getLocalizedMessage();
                    }
                }
            } catch (IOException e) {
                writeErrorLog(e);
                writeLog += "\nUnable to save Images: " + e.getLocalizedMessage();
            }
        }
        return writeLog;
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
            String fileName) throws Exception {
        try (ZipFile zipFile = new ZipFile(fileName)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry;
            while (entries.hasMoreElements()) { // find images directory (zip paths are linear, only simulating tree structure)
                entry = entries.nextElement();
                if (!entry.getName().equals(PGTUtil.IMAGES_SAVE_PATH)) {
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
                            .replace(PGTUtil.IMAGES_SAVE_PATH, "");
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
                ZipEntry imgEntry = zipFile.getEntry(PGTUtil.LOGOGRAPH_SAVE_PATH
                        + curNode.getId() + ".png");

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
     *
     * @param reversionManager reversion manager to load to
     * @param fileName full path of polyglot archive
     * @throws IOException on read error
     */
    public static void loadReversionStates(ReversionManager reversionManager,
            String fileName) throws IOException {
        try (ZipFile zipFile = new ZipFile(fileName)) {
            int i = 0;

            ZipEntry reversion = zipFile.getEntry(PGTUtil.REVERSION_SAVE_PATH
                    + PGTUtil.REVERSION_BASE_FILE_NAME + i);

            while (reversion != null && i < reversionManager.getMaxReversionsCount()) {
                reversionManager.addVersionToEnd(streamToBytArray(zipFile.getInputStream(reversion)));
                i++;
                reversion = zipFile.getEntry(PGTUtil.REVERSION_SAVE_PATH
                        + PGTUtil.REVERSION_BASE_FILE_NAME + i);
            }

            // remember to load latest state in addition to all prior ones
            reversion = zipFile.getEntry(PGTUtil.LANG_FILE_NAME);
            reversionManager.addVersionToEnd(streamToBytArray(zipFile.getInputStream(reversion)));
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
            // ensure export file has the proper extension
            if (!exportPath.toLowerCase().endsWith(".ttf")) {
                exportPath += ".ttf";
            }

            ZipEntry fontEntry = zipFile.getEntry(PGTUtil.CON_FONT_FILE_NAME);

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
     *
     * @param exportPath full export path
     * @throws IOException on failure
     */
    public static void exportCharisFont(String exportPath) throws IOException {
        try (InputStream fontStream = IOHandler.class.getResourceAsStream(PGTUtil.UNICODE_FONT_LOCATION)) {
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
     * @param fileName name of file to load sound recordings from
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

                    String soundPath = PGTUtil.GRAMMAR_SOUNDS_SAVE_PATH
                            + curNode.getRecordingId() + ".raw";
                    ZipEntry soundEntry = zipFile.getEntry(soundPath);

                    byte[] sound = null;

                    try (InputStream soundStream = zipFile.getInputStream(soundEntry)) {
                        sound = streamToBytArray(soundStream);
                    } catch (IOException e) {
                        writeErrorLog(e);
                        loadLog += "\nUnable to load sound: " + e.getLocalizedMessage();
                    } catch (Exception e) {
                        writeErrorLog(e);
                        loadLog += "\nUnable to load sound: " + e.getLocalizedMessage();
                    }

                    if (sound == null) {
                        continue;
                    }

                    grammarManager.addChangeRecording(curNode.getRecordingId(), sound);
                }
            }
        }

        if (!loadLog.isEmpty()) {
            throw new Exception(loadLog);
        }
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
     * @param workingDirectory
     * @param opMan
     * @throws IOException on failure or lack of permission to write
     */
    public static void writeOptionsIni(String workingDirectory, OptionsManager opMan) throws IOException {

        try (Writer f0 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(workingDirectory
                        + File.separator + PGTUtil.POLYGLOT_INI), StandardCharsets.UTF_8))) {
            String newLine = System.getProperty("line.separator");
            String nextLine;

            if (!testCanWrite(workingDirectory + File.separator + PGTUtil.POLYGLOT_INI)) {
                throw new IOException("Unable to save settings. Polyglot does not have permission to write to folder: "
                        + workingDirectory
                        + ". This is most common when running from Program Files in Windows.");
            }

            nextLine = PGTUtil.OPTIONS_LAST_FILES + "=";
            int lastFileCount = 0;
            for (String file : opMan.getLastFiles()) {
                if(lastFileCount > PGTUtil.OPTIONS_NUM_LAST_FILES) {
                    break;
                }
                
                lastFileCount++;
                // only write to ini if 1) the max file path length is not absurd/garbage, and 2) the file exists
                if (file.length() < PGTUtil.MAX_FILE_PATH_LENGTH && new File(file).exists()) {
                    if (nextLine.endsWith("=")) {
                        nextLine += file;
                    } else {
                        nextLine += ("," + file);
                    }
                }
            }
            f0.write(nextLine + newLine);

            nextLine = PGTUtil.OPTIONS_SCREEN_POS + "=";
            for (Entry<String, Point> curPos : opMan.getScreenPositions().entrySet()) {
                nextLine += ("," + curPos.getKey() + ":" + curPos.getValue().x + ":"
                        + curPos.getValue().y);
            }
            f0.write(nextLine + newLine);

            nextLine = PGTUtil.OPTIONS_SCREENS_SIZE + "=";
            for (Entry<String, Dimension> curSize : opMan.getScreenSizes().entrySet()) {
                nextLine += ("," + curSize.getKey() + ":" + curSize.getValue().width + ":"
                        + curSize.getValue().height);
            }

            f0.write(nextLine + newLine);
            nextLine = PGTUtil.OPTIONS_SCREENS_OPEN + "=";

            for (String screen : opMan.getLastScreensUp()) {
                nextLine += ("," + screen);
            }
            f0.write(nextLine + newLine);

            nextLine = PGTUtil.OPTIONS_AUTO_RESIZE + "="
                    + (opMan.isAnimateWindows() ? PGTUtil.TRUE : PGTUtil.FALSE);
            f0.write(nextLine + newLine);

            nextLine = PGTUtil.OPTIONS_MENU_FONT_SIZE + "=" + opMan.getMenuFontSize();
            f0.write(nextLine + newLine);

            nextLine = PGTUtil.OPTIONS_NIGHT_MODE + "="
                    + (opMan.isNightMode() ? PGTUtil.TRUE : PGTUtil.FALSE);
            f0.write(nextLine + newLine);

            nextLine = PGTUtil.OPTIONS_REVERSIONS_COUNT + "=" + opMan.getMaxReversionCount();
            f0.write(nextLine + newLine);

            nextLine = PGTUtil.OPTIONS_TODO_DIV_LOCATION + "=" + opMan.getToDoBarPosition();
            f0.write(nextLine + newLine);
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
            // internal logic based on thrown exception due to specific use case. No logging required.
            // IOHandler.writeErrorLog(e);
            ret = false;
        }

        return ret;
    }

    /**
     * Returns deepest directory from given path (truncating non-directory files
     * from the end)
     *
     * @param path path to fetch directory from
     * @return File representing directory, null if unable to capture directory
     * path for any reason
     */
    public static File getDirectoryFromPath(String path) {
        File ret = new File(path);

        if (ret.exists()) {
            while (ret != null && ret.exists() && !ret.isDirectory()) {
                ret = ret.getParentFile();
            }
        }

        if (ret  != null && !ret.exists()) {
            ret = null;
        }

        return ret;
    }

    /**
     * Wraps File so that I can avoid importing it elsewhere in code
     *
     * @param path path to file
     * @return file
     */
    public static File getFileFromPath(String path) {
        return new File(path);
    }

    /**
     * Writes to the PolyGlot error log file
     *
     * @param exception
     */
    public static void writeErrorLog(Throwable exception) {
        writeErrorLog(exception, "");
    }

    /**
     * Writes to the PolyGlot error log file
     *
     * @param exception
     * @param comment
     */
    public static void writeErrorLog(Throwable exception, String comment) {
        String curContents = "";
        String errorMessage = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());
        errorMessage += "-" + exception.getLocalizedMessage() + "-" + exception.getClass().getName();
        Throwable rootCause = ExceptionUtils.getRootCause(exception);
        rootCause = rootCause == null ? exception : rootCause;
        errorMessage += "\n" + ExceptionUtils.getStackTrace(rootCause);

        if (!comment.isEmpty()) {
            errorMessage = comment + ":\n" + errorMessage;
        }

        File errorLog = new File(PGTUtil.getErrorDirectory().getAbsolutePath() 
                + File.separator + PGTUtil.ERROR_LOG_FILE);

        try {
            if (errorLog.exists()) {
                try (Scanner logScanner = new Scanner(errorLog).useDelimiter("\\Z")) {
                    curContents = logScanner.hasNext() ? logScanner.next() : "";
                    int length = curContents.length();
                    int newLength = length + errorMessage.length();

                    if (newLength > PGTUtil.MAX_LOG_CHARACTERS) {
                        curContents = curContents.substring(newLength - PGTUtil.MAX_LOG_CHARACTERS);
                    }
                }
            }
        
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(errorLog))) {
                String output = getSystemInformation() + "\n" + curContents + errorMessage + "\n";
                System.out.println("Writing error to: " + errorLog.getAbsolutePath());
                writer.write(output);
            }
        } catch (IOException e) {
            // Fail silently. This fails almost exclusively due to being run in write protected folder, caught elsewhere
            // do not log to written file for obvious reasons (causes further write failure)
            // WHY DO PEOPLE INSTALL THIS TO WRITE PROTECTED FOLDERS AND SYSTEM32. WHY.
            // IOHandler.writeErrorLog(e);
        }
    }
    
    public static File getErrorLogFile() {
        return new File(PGTUtil.getErrorDirectory().getAbsolutePath() 
                + File.separator + PGTUtil.ERROR_LOG_FILE);
    }
    
    public static String getErrorLog() throws FileNotFoundException {
        String ret = "";
        File errorLog = getErrorLogFile();

        if (errorLog.exists()) {
            try (Scanner logScanner = new Scanner(errorLog).useDelimiter("\\Z")) {
                ret = logScanner.hasNext() ? logScanner.next() : "";
            }
        }
        return ret;
    }

    /**
     * Gets system information in human readable format
     *
     * @return system information
     */
    public static String getSystemInformation() {
        List<String> attributes = Arrays.asList("java.version",
                "java.vendor",
                "java.vendor.url",
                "java.vm.specification.version",
                "java.vm.specification.name",
                "java.vm.version",
                "java.vm.vendor",
                "java.vm.name",
                "java.specification.version",
                "java.specification.vendor",
                "java.specification.name",
                "java.class.version",
                "java.ext.dirs",
                "os.name",
                "os.arch",
                "os.version");
        String ret = "";

        for (String attribute : attributes) {
            ret += attribute + " : " + System.getProperty(attribute) + "\n";
        }

        return ret;
    }

    public static File unzipResourceToTempLocation(String resourceLocation)throws IOException {
        Path tmpPath = Files.createTempDirectory(PGTUtil.DISPLAY_NAME);
        unzipResourceToDir(resourceLocation, tmpPath);
        return tmpPath.toFile();
    }
    
    /**
     * Unzips an internal resource to a targeted path.Does not check header.
     *
     * @param internalPath Path to internal zipped resource
     * @param target destination to unzip to
     * @throws java.io.IOException
     */
    public static void unzipResourceToDir(String internalPath, Path target) throws IOException {
        InputStream fin = IOHandler.class.getResourceAsStream(internalPath);
        try (ZipInputStream zin = new ZipInputStream(fin)) {
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                File extractTo = new File(target + File.separator + ze.getName());
                if (ze.isDirectory()) {
                    extractTo.mkdir();
                } else {
                    try (FileOutputStream out = new FileOutputStream(extractTo)) {
                        int  nRead;
                        byte[] data = new byte[16384];
                        
                        while ((nRead = zin.read(data, 0, data.length)) != -1) {
                            out.write(data, 0, nRead);
                        }
                    }
                }
            }
        }
    }

    /**
     * Runs a command at the console, returning informational and error output.
     *
     * @param arguments command to run as [0], with arguments following
     * @return String array with two entries. [0] = Output, [1] = Error Output
     */
    public static String[] runAtConsole(String[] arguments) {
        String output = "";
        String error = "";

        try {
            Process p = new ProcessBuilder(arguments).start();
            System.out.println(Arrays.toString(arguments));

            // get general output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output += line;
                }
                
                while ((line = errorReader.readLine()) != null) {
                    error += line;
                }
            }
        } catch (IOException e) {
            error = e.getLocalizedMessage();
        }

        return new String[]{output, error};
    }

    /**
     * Tests whether Java is available at the system/terminal level
     *
     * @return current version of Java, or blank if none available
     */
    public static String getTerminalJavaVersion() {
        String ret = "";
        String[] command = {PGTUtil.JAVA8_JAVA_COMMAND, PGTUtil.JAVA8_VERSION_ARG};
            String[] result = runAtConsole(command);

        if (result[1].isEmpty()) {
            ret = result[0];
        }

        return ret;
    }

    /**
     * Tests whether Java can be called at the terminal
     *
     * @return
     */
    public static boolean isJavaAvailableInTerminal() {
        return !getTerminalJavaVersion().isEmpty();
    }

    private IOHandler() {}
}
