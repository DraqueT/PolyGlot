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
import java.awt.Component;
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
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
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

    public static byte[] getByteArrayFromFile(File file) throws FileNotFoundException, IOException {
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
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    /**
     * Used for snagging cachable versions of files
     *
     * @param filePath path of file to fetch as byte array
     * @return byte array of file at given path
     * @throws java.io.FileNotFoundException
     */
    public static byte[] getFileByteArray(String filePath) throws FileNotFoundException, IOException {
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

        if (IOHandler.isFileZipArchive(_fileName)) {
            try (ZipFile zipFile = new ZipFile(_fileName)) {
                ZipEntry xmlEntry = zipFile.getEntry(PGTUtil.LANG_FILE_NAME);
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
     * Deletes options file
     *
     * @param core
     */
    public static void deleteIni(DictCore core) {
        File f = new File(core.getWorkingDirectory().getAbsolutePath() + File.separator + PGTUtil.POLYGLOT_INI);
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
     * Loads all option data from ini file, if none, ignore. One will be created
     * on exit.
     *
     * @param core dictionary core
     * @throws IOException on failure to open existing file
     */
    public static void loadOptionsIni(DictCore core) throws Exception {
        OptionsManager opMan = core.getOptionsManager();
        File f = new File(core.getWorkingDirectory().getAbsolutePath() 
                + File.separator + PGTUtil.POLYGLOT_INI);
        if (!f.exists() || f.isDirectory()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(
                core.getWorkingDirectory().getAbsolutePath()
                        + File.separator + PGTUtil.POLYGLOT_INI))) {
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
                        opMan.getLastFiles().addAll(Arrays.asList(bothVal[1].split(",")));
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
                        opMan.setMaxReversionCount(Integer.parseInt(bothVal[1]));
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
            final File f = File.createTempFile(_fileName, null); // TODO: Make this save a temp file in the SAME DIRECTORY as the initial file (per #744)
            try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
                try (ZipOutputStream out = new ZipOutputStream(fileOutputStream, Charset.forName("ISO-8859-1"))) {
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
            out.putNextEntry(new ZipEntry(PGTUtil.REVERSION_SAVE_PATH));

            for (Integer i = 0; i < reversionList.size(); i++) {
                ReversionNode node = reversionList.get(i);

                out.putNextEntry(new ZipEntry(PGTUtil.REVERSION_SAVE_PATH + PGTUtil.REVERSION_BASE_FILE_NAME
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
                out.putNextEntry(new ZipEntry(PGTUtil.LOGOGRAPH_SAVE_PATH));
                for (LogoNode curNode : logoNodes) {
                    try {
                        out.putNextEntry(new ZipEntry(PGTUtil.LOGOGRAPH_SAVE_PATH
                                + curNode.getId().toString() + ".png"));

                        BufferedImage write = PGTUtil.toBufferedImage(curNode.getLogoGraph());
                        ImageIO.write(write, "png", out);

                        out.closeEntry();
                    } catch (IOException e) {
                        IOHandler.writeErrorLog(e);
                        writeLog += "\nUnable to save logograph: " + e.getLocalizedMessage();
                    }
                }
            } catch (IOException e) {
                IOHandler.writeErrorLog(e);
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
                                + curId.toString() + ".raw"));
                        out.write(curSound);
                        out.closeEntry();
                    } catch (IOException e) {
                        IOHandler.writeErrorLog(e);
                        writeLog += "\nUnable to save sound: " + e.getLocalizedMessage();
                    }

                }
            } catch (IOException e) {
                IOHandler.writeErrorLog(e);
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
                                + curNode.getId().toString() + ".png"));

                        ImageIO.write(curNode.getImage(), "png", out);

                        out.closeEntry();
                    } catch (IOException e) {
                        IOHandler.writeErrorLog(e);
                        writeLog += "\nUnable to save image: " + e.getLocalizedMessage();
                    }
                }
            } catch (IOException e) {
                IOHandler.writeErrorLog(e);
                writeLog += "\nUnable to save Images: " + e.getLocalizedMessage();
            }
        }
        return writeLog;
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
        File ret = new File(System.getProperty("user.dir"));

//        try {
//            ret = new File(IOHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
//        } catch (URISyntaxException | IllegalArgumentException e) {
//            IOHandler.writeErrorLog(e, "Unable to get base program path");
//        }
        System.out.println(ret.getAbsolutePath());

        return ret;
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
                ZipEntry imgEntry = zipFile.getEntry(PGTUtil.LOGOGRAPH_SAVE_PATH
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
     *
     * @param reversionManager reversion manager to load to
     * @param fileName full path of polyglot archive
     * @throws IOException on read error
     */
    public static void loadReversionStates(ReversionManager reversionManager,
            String fileName) throws IOException {
        try (ZipFile zipFile = new ZipFile(fileName)) {
            Integer i = 0;
            DictCore tmpCore;

            ZipEntry reversion = zipFile.getEntry(PGTUtil.REVERSION_SAVE_PATH
                    + PGTUtil.REVERSION_BASE_FILE_NAME + i.toString());

            while (reversion != null && i < reversionManager.getMaxReversionsCount()) {
                tmpCore = new DictCore();

                reversionManager.addVersionToEnd(streamToBytArray(zipFile.getInputStream(reversion)),
                        tmpCore.getLastSaveTime());
                i++;
                reversion = zipFile.getEntry(PGTUtil.REVERSION_SAVE_PATH
                        + PGTUtil.REVERSION_BASE_FILE_NAME + i.toString());
            }

            // remember to load latest state in addition to all prior ones
            reversion = zipFile.getEntry(PGTUtil.LANG_FILE_NAME);
            tmpCore = new DictCore();

            reversionManager.addVersionToEnd(streamToBytArray(zipFile.getInputStream(reversion)),
                    tmpCore.getLastSaveTime());
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

                    String soundPath = PGTUtil.GRAMMAR_SOUNDS_SAVE_PATH
                            + curNode.getRecordingId().toString() + ".raw";
                    ZipEntry soundEntry = zipFile.getEntry(soundPath);

                    byte[] sound = null;

                    try (InputStream soundStream = zipFile.getInputStream(soundEntry)) {
                        sound = streamToBytArray(soundStream);
                    } catch (IOException e) {
                        IOHandler.writeErrorLog(e);
                        loadLog += "\nUnable to load sound: " + e.getLocalizedMessage();
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
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
                new FileOutputStream(core.getWorkingDirectory().getAbsolutePath()
                        + File.separator + PGTUtil.POLYGLOT_INI), "UTF-8"))) {
            OptionsManager opMan = core.getOptionsManager();
            String newLine = System.getProperty("line.separator");
            String nextLine;

            if (!testCanWrite(core.getWorkingDirectory().getAbsolutePath() + File.separator + PGTUtil.POLYGLOT_INI)) {
                throw new IOException("Unable to save settings. Polyglot does not have permission to write to folder: "
                        + core.getWorkingDirectory().getAbsolutePath()
                        + ". This is most common when running from Program Files in Windows.");
            }

            nextLine = PGTUtil.OPTIONS_LAST_FILES + "=";
            for (String file : opMan.getLastFiles()) {
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

            nextLine = PGTUtil.OPTIONS_MENU_FONT_SIZE + "=" + Double.toString(opMan.getMenuFontSize());
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
     * Gets Unicode compatible font as byte array
     *
     * @return byte array of font's file
     * @throws FileNotFoundException if this throws, something is wrong
     * internally
     * @throws IOException if this throws, something is wrong internally
     */
    public byte[] getUnicodeFontByteArray() throws FileNotFoundException, IOException {
        try (InputStream localStream = this.getClass().getResourceAsStream(PGTUtil.UNICODE_FONT_LOCATION)) {
            return streamToBytArray(localStream);
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
        try (InputStream localStream = this.getClass().getResourceAsStream(PGTUtil.UNICODE_FONT_ITALIC_LOCATION)) {
            return streamToBytArray(localStream);
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

        if (!ret.exists()) {
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
        String errorMessage = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());
        errorMessage += "-" + exception.getLocalizedMessage() + "-" + exception.getClass().getName();
        Throwable rootCause = ExceptionUtils.getRootCause(exception);
        rootCause = rootCause == null ? exception : rootCause;
        errorMessage += "\n" + ExceptionUtils.getStackTrace(rootCause);
        BufferedWriter writer;

        if (!comment.isEmpty()) {
            errorMessage = comment + ":\n" + errorMessage;
        }

        File errorLog = new File(PGTUtil.getErrorDirectory().getAbsolutePath() 
                + File.separator + PGTUtil.ERROR_LOG_FILE);

        try {
            String output;

            if (errorLog.exists()) {
                Scanner logScanner = new Scanner(errorLog).useDelimiter("\\Z");
                String contents = logScanner.hasNext() ? logScanner.next() : "";

                writer = new BufferedWriter(new FileWriter(errorLog));
                int length = contents.length();
                int newLength = length + errorMessage.length();

                if (newLength > PGTUtil.MAX_LOG_CHARACTERS) {
                    contents = contents.substring(newLength - PGTUtil.MAX_LOG_CHARACTERS);
                }

                output = contents + errorMessage + "\n";
            } else {
                writer = new BufferedWriter(new FileWriter(errorLog));
                output = errorMessage + "\n";
            }

            output = getSystemInformation() + "\n" + output;

            System.out.println("Writing error to: " + errorLog.getAbsolutePath());
            writer.write(output);
            writer.close();
        } catch (IOException e) {
            // Fail silently. This fails almost exclusively due to being run in write protected folder, caught elsewhere
            // do not log to written file for obvious reasons (causes further write failure)
            // IOHandler.writeErrorLog(e);
        }
    }
    
    public static File gettErrorLogFile() {
        return new File(PGTUtil.getErrorDirectory().getAbsolutePath() 
                + File.separator + PGTUtil.ERROR_LOG_FILE);
    }
    
    public static String getErrorLog() throws FileNotFoundException {
        String ret = "";
        File errorLog = gettErrorLogFile();

        if (errorLog.exists()) {
            Scanner logScanner = new Scanner(errorLog).useDelimiter("\\Z");
            ret = logScanner.hasNext() ? logScanner.next() : "";
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

    /**
     * Unzips given archive to destination
     *
     * @param archive
     * @param destination
     * @throws java.io.IOException If target archive, location does not exist,
     * target is not a recognized format, or on un-packaging error
     */
    public static void unzipToLocation(File archive, Path destination) throws IOException {
        if (!archive.exists()) {
            throw new IOException("File: " + archive.getAbsolutePath() + " does not exist.");
        } else if (isFileZipArchive(archive.getAbsolutePath())) {
            try (ZipFile file = new ZipFile(archive)) {
                FileSystem fileSystem = FileSystems.getDefault();
                Enumeration<? extends ZipEntry> entries = file.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();

                    if (entry.isDirectory()) {
                        Files.createDirectories(fileSystem.getPath(destination + File.separator + entry.getName()));
                    } else {
                        InputStream is = file.getInputStream(entry);
                        BufferedInputStream bis = new BufferedInputStream(is);
                        String uncompressedFileName = destination + File.separator + entry.getName();
                        Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
                        Files.createFile(uncompressedFilePath);
                        try (FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName)) {
                            while (bis.available() > 0) {
                                fileOutput.write(bis.read());
                            }
                        }
                    }
                }
            }
        } else {
            throw new IOException("File: " + archive.getAbsolutePath() + " is not an archive.");
        }
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
     * @throws InterruptedException
     */
    public static String[] runAtConsole(String[] arguments) throws InterruptedException {
        String output = "";
        String error = "";

        try {
            Process p = new ProcessBuilder(arguments).start();
            System.out.println(Arrays.toString(arguments));

            // get general output
            InputStream is = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                output += line;
            }

            // get error output
            is = p.getErrorStream();
            reader = new BufferedReader(new InputStreamReader(is));
            while ((line = reader.readLine()) != null) {
                error += line;
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
        try {
            String[] result = runAtConsole(command);

            // only return result if no error
            if (result[1].isEmpty()) {
                ret = result[0];
            }
        } catch (InterruptedException e) {
            writeErrorLog(e, "Unable to run terminal command: " + Arrays.toString(command));
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
}
