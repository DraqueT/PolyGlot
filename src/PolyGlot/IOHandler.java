/*
 * Copyright (c) 2014, draque
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

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
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

            ZipEntry xmlEntry = zipFile.getEntry(XMLIDs.dictFileName);

            return zipFile.getInputStream(xmlEntry);
        }

        return rawFile;
    }

    /**
     * Gets font from save file if possible, null otherwise
     *
     * @param _path The path of the PGD file
     * @return a Font object if the PGD file is both a zip archive and contains
     * a font
     * @throws java.io.IOException
     * @throws java.awt.FontFormatException
     */
    public static Font getFontFrom(String _path) throws IOException, FontFormatException {
        Font ret = null;

        if (isFileZipArchive(_path)) {
            ZipFile zipFile = new ZipFile(_path);

            ZipEntry fontEntry = zipFile.getEntry(XMLIDs.fontFileName);

            if (fontEntry != null) {
                final File tempFile = File.createTempFile("stream2file", ".tmp");
                tempFile.deleteOnExit();

                FileOutputStream out = new FileOutputStream(tempFile);
                IOUtils.copy(zipFile.getInputStream(fontEntry), out);

                ret = Font.createFont(Font.TRUETYPE_FONT, tempFile);
            }
        }

        return ret;
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
        final String tempFileName = "xxTEMPPGTFILExx";
        String fileName;
        String directoryPath;
        
        {
            File file = new File(_fileName);
            
            fileName = file.getName();
            directoryPath = file.getParentFile().getAbsolutePath();
        }
        
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        
        StringBuilder sb = new StringBuilder();

        sb.append(writer.getBuffer().toString().replaceAll("\n|\r", ""));

        // save file to temp location initially.
        final File f = new File(directoryPath, tempFileName);
        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        ZipEntry e = new ZipEntry(XMLIDs.dictFileName);
        out.putNextEntry(e);

        byte[] data = sb.toString().getBytes();
        out.write(data, 0, data.length);

        // embed font in PGD archive if applicable
        File fontFile = IOHandler.getFontFile(core.getLangFont());

        if (fontFile != null) {
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(fontFile);
            out.putNextEntry(new ZipEntry(XMLIDs.fontFileName));
            int length;

            while ((length = fis.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            out.closeEntry();
            fis.close();
        }

        out.closeEntry();

        out.finish();
        out.close();
        
        // attempt to open file in dummy core. On success, copy file to end
        // destination, on fail, delete file, and inform user by bubbling error
        try {
            File file = new File(directoryPath, tempFileName);
            
            DictCore test = new DictCore();
            test.readFile(file.getAbsolutePath());
            
        } catch (Exception ex) {
            File file = new File(directoryPath, tempFileName);
            file.delete();
            
            throw new IOException(ex);
        }
        
        File fileTemp = new File(directoryPath, tempFileName);
        File fileFinal = new File(directoryPath, fileName);
        
        boolean success = fileTemp.renameTo(fileFinal);
        
        if (!success) {
            fileTemp.delete();
            throw new IOException("Unable to save file. Check permissions.");
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
        } else if (System.getProperty("os.name").startsWith("Windows")) {
            ret = getFontFromLocation(System.getenv("WINDIR") + "\\Fonts", font);
        } else {
            // Other OSes don't support this yet
        }

        // TODO: Inform user if font cannot be found (mostly a mac issue...)
        return ret;
    }

    /**
     * Returns a font's file based on the font and a path
     *
     * @param path path to check for a font
     * @param font font to check for
     * @return the font's file, null otherwise
     */
    private static File getFontFromLocation(String path, Font font) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        File ret = null;

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                File fontFile = loadFont(listOfFile.getPath(), font);

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
                if (f.getName().equals(testFont.getName())
                        || f.getName().equals(testFont.getName() + " Regular")) {
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
}
