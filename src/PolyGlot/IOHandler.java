/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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

import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.poi.util.IOUtils;
import org.w3c.dom.Document;

/**
 * This class handles file IO for PolyGlot
 *
 * @author draque
 */
public class IOHandler {

    /**
     * Gets the dictionary File from the filename of the pgd, whether it's raw
     * XML or an archive
     *
     * @param _filename the filename of the actual file
     * @return the File object of the dictionary
     * @throws java.io.IOException
     */
    public static InputStream getDictFile(String _filename) throws IOException {
        InputStream rawFile = new FileInputStream(_filename);

        if (isFileZipArchive(_filename)) {
            ZipFile zipFile = new ZipFile(_filename);

            ZipEntry xmlEntry = zipFile.getEntry(PGTUtil.dictFileName);

            return zipFile.getInputStream(xmlEntry);
        }

        return rawFile;
    }

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
     * Sets the conlang font, if one exists and caches its file for quicksaving
     *
     * @param _path The path of the PGD file
     * @param core the dictionary core
     * @throws java.io.IOException
     * @throws java.awt.FontFormatException
     */
    public static void setFontFrom(String _path, DictCore core) throws IOException, FontFormatException {
        if (isFileZipArchive(_path)) {
            ZipFile zipFile = new ZipFile(_path);

            ZipEntry fontEntry = zipFile.getEntry(PGTUtil.fontFileName);

            if (fontEntry != null) {
                final File tempFile = File.createTempFile("stream2file", ".tmp");
                tempFile.deleteOnExit();

                FileOutputStream out = new FileOutputStream(tempFile);
                IOUtils.copy(zipFile.getInputStream(fontEntry), out);

                try {
                    Font conFont = Font.createFont(Font.TRUETYPE_FONT, tempFile);
                    
                    if (conFont == null) {
                        return;
                    }
                    
                    byte[] cachedFont = IOUtils.toByteArray(new FileInputStream(tempFile));
                    core.getPropertiesManager().setFontCon(conFont);
                    core.getPropertiesManager().setCachedFont(cachedFont);
                } catch (FontFormatException e) {
                    throw new FontFormatException("Could not load language font. Possible incompatible font: " + e.getMessage());
                } catch (IOException e) {
                    throw new IOException("Could not load language font. I/O exception: " + e.getMessage());
                }

                zipFile.close();
            }
        }
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

        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        int test = in.readInt();
        in.close();
        return test == 0x504b0304;
    }

    public static void writeFile(String _fileName, Document doc, DictCore core) throws IOException, TransformerException {
        String directoryPath;

        File finalFile = new File(_fileName);

        directoryPath = finalFile.getParentFile().getAbsolutePath();

        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        StringBuilder sb = new StringBuilder();

        sb.append(writer.getBuffer().toString());

        // save file to temp location initially.
        final File f = new File(directoryPath, PGTUtil.tempFile);
        final ZipOutputStream out;

        if (System.getProperty("java.version").startsWith("1.6")) {
            out = new ZipOutputStream(new FileOutputStream(f));
        } else {
            out = new ZipOutputStream(new FileOutputStream(f), Charset.forName("ISO-8859-1"));
        }

        ZipEntry e = new ZipEntry(PGTUtil.dictFileName);
        out.putNextEntry(e);

        byte[] data = sb.toString().getBytes("UTF-8");
        out.write(data, 0, data.length);

        out.closeEntry();

        byte[] cachedFont = core.getPropertiesManager().getCachedFont();
        
        // only search for font if the cached font is null
        if (cachedFont == null) {
            // embed font in PGD archive if applicable
            File fontFile = IOHandler.getFontFile(core.getPropertiesManager().getFontCon());
            
            if (fontFile != null) {
                core.getPropertiesManager().setCachedFont(IOUtils.toByteArray(new FileInputStream(fontFile)));
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(fontFile);
                out.putNextEntry(new ZipEntry(PGTUtil.fontFileName));
                int length;

                while ((length = fis.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                out.closeEntry();
                fis.close();
            }
        } else {
            out.putNextEntry(new ZipEntry(PGTUtil.fontFileName));
            out.write(cachedFont);
            out.closeEntry();
        }

        // write all logograph images to file
        Iterator<LogoNode> it = core.getLogoCollection().getAllLogos().iterator();
        if (it.hasNext()) {
            out.putNextEntry(new ZipEntry(PGTUtil.logoGraphSavePath));

            while (it.hasNext()) {
                LogoNode curNode = it.next();
                out.putNextEntry(new ZipEntry(PGTUtil.logoGraphSavePath
                        + curNode.getId().toString() + ".png"));

                ImageIO.write(curNode.getLogoGraph(), "png", out);

                out.closeEntry();
            }
        }
        
        // write all grammar wav recordings to file
        Map<Integer, byte[]> grammarSoundMap = core.getGrammarManager().getSoundMap();
        Iterator<Entry<Integer, byte[]>> gramSoundIt = grammarSoundMap.entrySet().iterator();
        if (gramSoundIt.hasNext()) {
            out.putNextEntry(new ZipEntry(PGTUtil.grammarSoundSavePath));
            
            while (gramSoundIt.hasNext()) {
                Entry<Integer, byte[]> curEntry = gramSoundIt.next();
                Integer curId = curEntry.getKey();
                byte[] curSound = curEntry.getValue();
                
                out.putNextEntry(new ZipEntry(PGTUtil.grammarSoundSavePath
                        + curId.toString() + ".raw"));
                out.write(curSound);
                out.closeEntry();
            }
        }

        out.finish();
        out.close();

        // attempt to open file in dummy core. On success, copy file to end
        // destination, on fail, delete file, and inform user by bubbling error
        try {
            DictCore test = new DictCore();
            test.readFile(f.getAbsolutePath());

        } catch (Exception ex) {
            f.delete();

            throw new IOException(ex);
        }

        try {
            if (System.getProperty("java.version").startsWith("1.6")) {
                if (finalFile.exists()) {
                    finalFile.delete();
                }
                FileUtils.copyFile(f, finalFile);
            } else {
                java.nio.file.Files.copy(f.toPath(), finalFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            f.delete();
        } catch (IOException ex) {
            f.delete();
            throw new IOException("Unable to save file: " + ex.getMessage());
        }
    }

    /**
     * gets the file of the current conlang font from the user's system
     *
     * @param font the font to find a file for
     * @return the font's file if found, null otherwise
     */
    public static File getFontFile(Font font) {
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
            // TODO: throw cathable error to inform user of unrecognized OS
        }

        // TODO: Inform user via catchable error if font cannot be found (mostly a mac issue...)
        return ret;
    }

    /**
     * Returns a font's file based on the font and a path
     * Recurses on any subdirectories found
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
                || path.toLowerCase().endsWith(".ttc")
                || path.toLowerCase().endsWith(".dfont")) {
            try {
                Font f = Font.createFont(Font.TRUETYPE_FONT, fontFile);

                // if names match, set ret to return file
                if (f.getFamily().equals(testFont.getFamily())
                        || f.getFamily().equals(testFont.getFamily())) {
                    ret = fontFile;
                }

            } catch (FontFormatException e) {
                // null detected and message bubbled to user elsewhere
                ret = null;
            } catch (IOException e) {
                // null detected and message bubbled to user elsewhere
                ret = null;
            }
        }

        return ret;
    }

    /**
     * loads all images into their logographs from archive
     *
     * @param logoCollection logocollection from dictionary core
     * @param fileName name/path of archive
     * @throws java.lang.Exception
     */
    public static void loadImages(LogoCollection logoCollection, String fileName) throws Exception {
        if (!isFileZipArchive(fileName)) {
            return;
        }

        Iterator<LogoNode> it = logoCollection.getAllLogos().iterator();
        ZipFile zipFile = new ZipFile(fileName);

        while (it.hasNext()) {
            LogoNode curNode = it.next();
            ZipEntry imgEntry = zipFile.getEntry(PGTUtil.logoGraphSavePath
                    + curNode.getId().toString() + ".png");

            BufferedImage img = ImageIO.read(zipFile.getInputStream(imgEntry));
            curNode.setLogoGraph(img);
        }
        zipFile.close();
    }

    /**
     * Exports font in PGD to external file
     *
     * @param exportPath path to export to
     * @param dictionaryPath path of PGT dictionary
     * @throws IOException
     */
    public static void exportFont(String exportPath, String dictionaryPath) throws IOException {
        ZipFile zipFile = new ZipFile(dictionaryPath);

        if (zipFile == null) {
            throw new IOException("Dictionary must be saved before font can be exported.");
        }

        // ensure export file has the proper extension
        if (!exportPath.toLowerCase().endsWith(".ttf")) {
            exportPath += ".ttf";
        }

        ZipEntry fontEntry = zipFile.getEntry(PGTUtil.fontFileName);

        if (fontEntry != null) {
            Path path = Paths.get(exportPath);
            Files.copy(zipFile.getInputStream(fontEntry), path, StandardCopyOption.REPLACE_EXISTING);
        } else {
            throw new IOException("Custom font not found in PGD dictionary file.");
        }
    }

    /**
     * Loads any related grammar recordings into the passed grammar manager via id
     * @param _fileName name of file to load sound recordings from
     * @param grammarManager grammar manager to populate with sounds
     */
    static void loadGrammarSounds(String fileName, GrammarManager grammarManager) {
        ZipFile zipFile = null;
        
        try {
            if (!isFileZipArchive(fileName)) {
                return;
            }
            
            zipFile = new ZipFile(fileName);
        } catch (Exception e) {
            // Do nothing: if the file were missing, it would have blown up earlier
            if (zipFile == null) {
                return;
            }
        }
    
        Iterator<GrammarChapNode> chapIt = grammarManager.getChapters().iterator();
        
        while (chapIt.hasNext()) {
            GrammarChapNode curChap = chapIt.next();
            
            for (int i = 0; i < curChap.getChildCount(); i++) {
                GrammarSectionNode curNode = (GrammarSectionNode)curChap.getChildAt(i);
                
                if (curNode.getRecordingId() == -1) {
                    continue;
                }
                
                String soundPath = PGTUtil.grammarSoundSavePath
                    + curNode.getRecordingId().toString() + ".raw";
                ZipEntry soundEntry = zipFile.getEntry(soundPath);
                
                byte[] sound = null;
                
                try {
                    sound = IOUtils.toByteArray(zipFile.getInputStream(soundEntry));
                } catch (IOException e) {
                    // TODO: create load log?
                } catch (Exception e) {
                    // TODO: create load log? (sound not found error)
                }
                
                if (sound == null) {
                    continue;
                }
                
                grammarManager.addChangeRecording(curNode.getRecordingId(), sound);
            }
        }
    }

    /**
     * Fetches and returns LCD style font 
     * NOTE 1: the font returned is very small, use deriveFont() to make it a
     * usable size
     * NOTE 2: this is a nonstatic method due to an inputstream restriction
     *
     * @return LCD display font
     * @throws java.awt.FontFormatException if font corrupted
     * @throws java.io.IOException if unable to load font
     */
    public Font getLcdFont() throws FontFormatException, IOException {
        // TODO: move font to a resource folder, and change to a constant
        InputStream tmp = this.getClass().getResourceAsStream("/PolyGlot/lcdFont.ttf");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font ret = Font.createFont(Font.TRUETYPE_FONT, tmp);
        
        if (ret != null) {
            ge.registerFont(ret);
        }

        return ret;
    }
}
