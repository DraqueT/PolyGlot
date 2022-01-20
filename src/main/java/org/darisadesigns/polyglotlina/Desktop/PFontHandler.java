/*
 * Copyright (c) 2022, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
 * See LICENSE.TXT included with this code to read the full license agreement.

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
package org.darisadesigns.polyglotlina.Desktop;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.darisadesigns.polyglotlina.DictCore;

/**
 * Split out from IOHandler due to large portion devoted to handling fonts
 *
 * @author Draque Thompson
 */
public class PFontHandler extends org.darisadesigns.polyglotlina.PFontHandler {

    /**
     * Sets the conlang and local lang fonts, if one exists and caches its file
     * for quicksaving
     *
     * @param _path The path of the PGD file
     * @param core the dictionary core
     * @throws java.io.IOException
     * @throws java.awt.FontFormatException
     */
    @Override
    public void setFontFrom(String _path, DictCore core) throws IOException, Exception {
        setFontFrom(_path, core, true);
        setFontFrom(_path, core, false);
    }

    private static void setFontFrom(String _path, DictCore core, boolean isConFont) throws IOException, Exception {
        if (core.getOSHandler().getIOHandler().isFileZipArchive(_path)) {
            try (ZipFile zipFile = new ZipFile(_path)) {
                ZipEntry fontEntry = isConFont
                        ? zipFile.getEntry(org.darisadesigns.polyglotlina.PGTUtil.CON_FONT_FILE_NAME)
                        : zipFile.getEntry(org.darisadesigns.polyglotlina.PGTUtil.LOCAL_FONT_FILE_NAME);

                if (fontEntry != null) {
                    final File tempFile = File.createTempFile("stream2file", ".tmp");

                    tempFile.deleteOnExit();

                    try (FileOutputStream os = new FileOutputStream(tempFile)) {
                        try (InputStream is = zipFile.getInputStream(fontEntry)) {
                            is.transferTo(os);
                        }

                        try {
                            byte[] cachedFont = core.getOSHandler().getIOHandler().getByteArrayFromFile(tempFile);

                            if (isConFont) {
                                ((DesktopPropertiesManager) core.getPropertiesManager())
                                        .setFontFromFile(tempFile.getAbsolutePath());
                                core.getPropertiesManager().setCachedFont(cachedFont);
                            } else {
                                ((DesktopPropertiesManager) core.getPropertiesManager())
                                        .setLocalFontFromFile(tempFile.getAbsolutePath());
                                core.getPropertiesManager().setCachedLocalFont(cachedFont);
                            }
                        } catch (FontFormatException e) {
                            core.getOSHandler().getIOHandler().writeErrorLog(e);
                            throw new Exception("Could not load language font. Possible incompatible font: " + e.getMessage());
                        } catch (IOException e) {
                            throw new IOException("Could not load language font. I/O exception: " + e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Attempts to load the given font from the OS's font folder (due to Java's
     * ligature problems)
     *
     * @param fontFamily
     * @return returns loaded font file on success, null otherwise
     */
    public static Font loadFontFromOSFileFolder(String fontFamily) {
        Font ret = null;
        try {
            File fontFile = getFontFile(fontFamily);
            if (fontFile != null && fontFile.exists()) {
                ret = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                Map attributes = ret.getAttributes();
                attributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
                ret = ret.deriveFont(attributes);
            }
        }
        catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            // do nothing here. Failure means returning null
        }

        return ret;
    }

    /**
     * gets the file of the current conlang font from the user's system
     *
     * @param fontFamily the font to find a file for
     * @return the font's file if found, null otherwise
     * @throws java.lang.Exception for unrecognized OS
     */
    public static File getFontFile(String fontFamily) throws Exception {
        File ret = null;

        if (System.getProperty("os.name").startsWith("Mac")) {
            ret = getFontFromLocation(fontFamily, "/Library/Fonts/");

            if (ret == null) {
                ret = getFontFromLocation(fontFamily, "/System/Library/Fonts/");
            }

            if (ret == null) {
                ret = getFontFromLocation(fontFamily,
                        System.getProperty("user.home") + "/Library/Fonts/");
            }
        } else if (System.getProperty("os.name").startsWith("Win")) {
            var fontFiles = searchForFonts(new File(System.getenv("windir") + "/WinSxS"));
            
            for (var fontFile : fontFiles) {
                var test = loadFont(fontFile.getAbsolutePath(), fontFamily);
                
                if (test!= null) {
                    ret = test;
                    break;
                }
            }
        } else if (System.getProperty("os.name").indexOf("nix") > 0
                || System.getProperty("os.name").indexOf("bunt") > 0
                || System.getProperty("os.name").indexOf("fed") > 0
                || System.getProperty("os.name").indexOf("nux") > 0) {
            ret = getFontFromLocations(fontFamily, "/usr/share/fonts", "/usr/local/share", "~/.fonts");
        } else {
            throw new Exception("Unknown OS; unable to retrieve font.");
        }

        return ret;
    }
    
    /**
     * Recursively searches target directory for fonts
     * @param targetDirectory
     * @return 
     */
    public static List<File> searchForFonts(File targetDirectory) {
        var foundFonts = new ArrayList<File>();
        
        searchForFonts(targetDirectory, foundFonts);
        
        return foundFonts;
    }
    
    /**
     * Populates passed list via side effect to more easily allow for recursion
     * @param target
     * @param foundFonts 
     */
    private static void searchForFonts(File target, List<File> foundFonts) {
        if (target.exists()) {
            FileFilter filter = (File file1) -> {
                java.lang.String name1 = file1.getName();
                return (file1.isDirectory() && name1.toLowerCase().contains("font")) 
                        || name1.toLowerCase().endsWith("ttf") 
                        || name1.toLowerCase().endsWith("otf") 
                        || name1.toLowerCase().endsWith("ttc") 
                        || name1.toLowerCase().endsWith("dfont");
            };

            for (File search : target.listFiles(filter)) {
                if (search.isDirectory()) {
                    searchForFonts(search, foundFonts);
                } else {
                    foundFonts.add(search);
                }
            }
        }
    }

    /**
     * Returns a font's file based on the font and a path Recurses on any
     * subdirectories found.Searches through multiple      *
     * In the case that multiple versions of the font are installed, the most
     * recently modified version will be defaulted to
     *
     * @param paths paths to check for a font
     * @param fontFamily font to check for
     * @return the font's file, null otherwise
     * @throws java.io.IOException
     */
    public static File getFontFromLocations(String fontFamily, String... paths) throws IOException {
        File ret = null;

        for (String path : paths) {
            File match = getFontFromLocation(fontFamily, path);

            if (ret == null
                    || (match != null && compareFreshness(match, ret))) {
                ret = match;
            }
        }

        return ret;
    }

    /**
     * Returns a font's file based on the font and a path Recurses on any
     * subdirectories found.
     *
     * In the case that multiple versions of the font are installed, the most
     * recently modified version will be defaulted to
     *
     * @param path path to check for a font
     * @param fontFamily font to check for
     * @return the font's file, null otherwise
     * * @throws java.io.IOException
     */
    private static File getFontFromLocation(String fontFamily, String path) throws IOException {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        List<File> matches = new ArrayList<>();
        File ret = null;

        if (listOfFiles != null && listOfFiles.length > 0) {
            // inspect all files and subdirectories to find matches
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    File fontFile = loadFont(listOfFile.getPath(), fontFamily);

                    if (fontFile != null) {
                        matches.add(fontFile);
                    }
                } else if (listOfFile.isDirectory()) {
                    File fontFile = getFontFromLocation(fontFamily,
                            path + "/" + listOfFile.getName());

                    if (fontFile != null) {
                        matches.add(fontFile);
                    }
                }
            }

            // return only most recently modified match
            for (File match : matches) {
                if (ret == null) {
                    ret = match;
                } else {
                    if (compareFreshness(match, ret)) {
                        ret = match;
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Compares "freshness" of files. First compares last modified. If the same
     * (and running on Windows), also compares creation date. Returns true if
     * file a is fresher than file b.
     *
     * @param a
     * @param b
     * @return
     * * @throws java.io.IOException
     */
    private static boolean compareFreshness(File a, File b) throws IOException {
        boolean ret = false;
        long aModified = a.lastModified();
        long bModified = b.lastModified();

        if (aModified == bModified && org.darisadesigns.polyglotlina.PGTUtil.IS_WINDOWS) {
            FileTime aCreated = Files.readAttributes(a.toPath(), BasicFileAttributes.class).creationTime();
            FileTime bCreated = Files.readAttributes(b.toPath(), BasicFileAttributes.class).creationTime();

            if (aCreated.compareTo(bCreated) > 0) {
                ret = true;
            }
        } else if (aModified > bModified) {
            ret = true;
        }

        return ret;
    }

    /**
     * compares testfont to loaded file. returns file if it represents the font
     *
     * @param path full path of file to test
     * @param fontFamily font family to search
     * @return file if path leads to passed font, null otherwise
     */
    private static File loadFont(String path, String fontFamily) {
        File fontFile = new File(path);
        File ret = null;

        // unrecognized types won't be loaded
        if (path.toLowerCase().endsWith(".ttf")
                || path.toLowerCase().endsWith(".otf")
                || path.toLowerCase().endsWith(".ttc")
                || path.toLowerCase().endsWith(".dfont")) {
            try {
                Font f = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                Map attributes = f.getAttributes();
                attributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
                f = f.deriveFont(attributes);

                // if names match, set ret to return file
                if (f.getFamily().equals(fontFamily)) {
                    ret = fontFile;
                }

            }
            catch (FontFormatException e) {
                // "Font name not found" errors due to Java bug (Java does not recognize some Mac style ttf fonts)
                // disabling logging until Java bug corrected.
                // IOHandler.writeErrorLog(e, path);
                // null detected and message bubbled to user elsewhere
            }
            catch (IOException e) {
                DesktopIOHandler.getInstance().writeErrorLog(e, path);
                // null detected and message bubbled to user elsewhere
            }
        }

        return ret;
    }

    /**
     * Fetches and returns a font from a given location
     *
     * @param filePath
     * @return collected font
     * @throws java.awt.FontFormatException
     * @throws java.io.IOException
     */
    public static Font getFontFromFile(String filePath) throws FontFormatException, IOException {
        File fontFile = new File(filePath);
        Font ret = Font.createFont(Font.TRUETYPE_FONT, fontFile);
        Map attributes = ret.getAttributes();
        attributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
        ret = ret.deriveFont(attributes);

        return ret;
    }

    public static String writeFont(ZipOutputStream out, Font outputFont, byte[] cachedFont, DictCore core, boolean isConFont) {
        String writeLog = "";
        try {
            // only search for font if the cached font is null
            if (cachedFont == null) {
                // embed font in PGD archive if applicable
                File fontFile = null;
                try {
                    if (outputFont != null) {
                        fontFile = getFontFile(outputFont.getFamily());
                    }
                }
                catch (Exception e) {
                    core.getOSHandler().getIOHandler().writeErrorLog(e);
                    writeLog += "\nerror: " + e.getLocalizedMessage();
                }

                if (fontFile != null) {
                    try (FileInputStream fontInputStream = new FileInputStream(fontFile)) {
                        byte[] fontBytes = core.getOSHandler().getIOHandler().streamToByetArray(fontInputStream);

                        if (isConFont) {
                            core.getPropertiesManager().setCachedFont(fontBytes);
                        } else {
                            core.getPropertiesManager().setCachedLocalFont(fontBytes);
                        }
                    }
                    byte[] buffer = new byte[1024];
                    try (FileInputStream fis = new FileInputStream(fontFile)) {
                        if (isConFont) {
                            out.putNextEntry(new ZipEntry(org.darisadesigns.polyglotlina.PGTUtil.CON_FONT_FILE_NAME));
                        } else {
                            out.putNextEntry(new ZipEntry(org.darisadesigns.polyglotlina.PGTUtil.LOCAL_FONT_FILE_NAME));
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
                    out.putNextEntry(new ZipEntry(org.darisadesigns.polyglotlina.PGTUtil.CON_FONT_FILE_NAME));
                } else {
                    out.putNextEntry(new ZipEntry(org.darisadesigns.polyglotlina.PGTUtil.LOCAL_FONT_FILE_NAME));
                }
                out.write(cachedFont);
                out.closeEntry();
            }
        }
        catch (IOException e) {
            core.getOSHandler().getIOHandler().writeErrorLog(e);
            writeLog += "\nUnable to write font to archive: " + e.getMessage();
        }
        return writeLog;
    }

    public static Font getLcdFont() throws FontFormatException, IOException {
        return new PFontHandler().getLcdFontInternal();
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
        try (InputStream tmp = this.getClass().getResourceAsStream(org.darisadesigns.polyglotlina.PGTUtil.LCD_FONT_LOCATION)) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font ret = Font.createFont(Font.TRUETYPE_FONT, tmp);

            if (ret != null) {
                ge.registerFont(ret);
            }

            return ret;
        }
    }
    
    /**
     * Tests whether given string can have every character represented as a glyph for the given font
     * @param value string to test
     * @param conFont true if using the conlang font, false if using the local language font
     * @return 
     */
    @Override
    public boolean canStringBeRendered(String value, boolean conFont) {
        Font font = conFont ?
                ((DesktopPropertiesManager)PolyGlot.getPolyGlot().getCore().getPropertiesManager()).getFontCon() :
                ((DesktopPropertiesManager)PolyGlot.getPolyGlot().getCore().getPropertiesManager()).getFontLocal();
        
        for (int i = 0; i < value.length(); i++) {
            if (!font.canDisplay(value.charAt(i))) {
                return false;
            }
        }
        
        return true;
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
        return new PFontHandler().getCharisUnicodeFontInternal(org.darisadesigns.polyglotlina.PGTUtil.UNICODE_FONT_LOCATION);
    }

    /**
     * Fetches and returns unicode compatible font NOTE 1: this is a non-static
     * method due to an input stream restriction NOTE 2: this is the default
     * conlang font in PolyGlot
     *
     * @return Charis unicode compatible font
     */
    private Font getCharisUnicodeFontInternal(String location) throws IOException {
        Font ret;
        try (InputStream tmp = this.getClass().getResourceAsStream(location)) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ret = Font.createFont(Font.TRUETYPE_FONT, tmp);

            if (ret != null) {
                ge.registerFont(ret);
            }

        }
        catch (IOException | FontFormatException e) {
            throw new IOException("Unable to load Charis (" + location + "): "
                    + e.getLocalizedMessage(), e);
        }

        return ret;
    }

    /**
     * For getting Font based on javafx's needs NOTE 1: this is a non-static
     * method due to an input stream restriction NOTE 2: this is the default
     * conlang font in PolyGlot
     *
     * @return
     */
    public InputStream getCharisInputStream() {
        return this.getClass().getResourceAsStream(org.darisadesigns.polyglotlina.PGTUtil.UNICODE_FONT_LOCATION);
    }

    /**
     * Fetches and returns default button font Should only be called a single
     * time (font then cached)
     *
     * @return Font to default buttons to
     * @throws java.io.IOException if unable to load font
     */
    public static Font getMenuFont() throws IOException {
        return new PFontHandler().getMenuFontInternal();
    }

    /**
     * Fetches and returns default button font nonstatic
     *
     * @return Font to default buttons to
     * @throws java.io.IOException if unable to load font
     */
    private Font getMenuFontInternal() throws IOException {
        Font ret;
        try (InputStream tmp = this.getClass().getResourceAsStream(org.darisadesigns.polyglotlina.PGTUtil.BUTTON_FONT_LOCATION)) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ret = Font.createFont(Font.TRUETYPE_FONT, tmp);
            ret = ret.deriveFont((float) 12); // default to size 12 font
            if (ret != null) {
                ge.registerFont(ret);
            }
        }
        catch (Exception e) {
            throw new IOException("Unable to load button font.", e);
        }

        return ret;
    }
    
    @Override
    public void updateLocalFont() {
        var scrMain = PolyGlot.getPolyGlot().getRootWindow();
        if (scrMain != null) {
            var local = ((DesktopPropertiesManager)PolyGlot.getPolyGlot().getCore().getPropertiesManager()).getFontLocal();
            scrMain.getToDoTree().setFont(local);
        }
    }
}
