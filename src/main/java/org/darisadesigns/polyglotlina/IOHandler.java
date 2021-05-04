/*
 * Copyright (c) 2014-2021, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
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
package org.darisadesigns.polyglotlina;

import org.darisadesigns.polyglotlina.ManagersCollections.GrammarManager;
import org.darisadesigns.polyglotlina.ManagersCollections.LogoCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.ImageCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.ReversionManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class handles file IO for PolyGlot
 *
 * @author draque
 */
public interface IOHandler {

    /**
     * Creates and returns a temporary file with the contents specified. File
     * will be deleted on exit of PolyGlot.
     *
     * @param contents Contents to put in file.
     * @param extension extension name for tmp file (defaults to tmp if none
     * given)
     * @return Temporary file with specified contents
     * @throws IOException on write error
     */
    public File createTmpFileWithContents(String contents, String extension) throws IOException;
    
    public File createTmpFileFromImageBytes(byte[] imageBytes, String fileName) throws IOException;
    
    public File createFileWithContents(String path, String contents) throws IOException;

    public byte[] getByteArrayFromFile(File file) throws IOException;

    /**
     * Takes input stream and converts it to a raw byte array
     *
     * @param is
     * @return raw byte representation of stream
     * @throws IOException
     */
    public byte[] streamToByetArray(InputStream is) throws IOException;

    /**
     * Used for snagging catchable versions of files
     *
     * @param filePath path of file to fetch as byte array
     * @return byte array of file at given path
     * @throws java.io.FileNotFoundException
     */
    public byte[] getFileByteArray(String filePath) throws IOException;

    /**
     * Given file name, returns appropriate cust handler
     *
     * @param _fileName full path of target file to read
     * @param _core dictionary core
     * @return CustHandler class
     * @throws java.io.IOException on read problem
     */
    public CustHandler getHandlerFromFile(String _fileName, DictCore _core) throws IOException;

    /**
     * Creates a custhandler object from a reversion byte array of a language
     * state
     *
     * @param byteArray byte array containing XML of language state
     * @param _core dictionary core
     * @return new custhandler class
     * @throws IOException on parse error
     */
    public CustHandler getHandlerFromByteArray(byte[] byteArray, DictCore _core) throws IOException;

    /**
     * returns name of file sans path
     *
     * @param fullPath full path to file
     * @return string of filename
     */
    public String getFilenameFromPath(String fullPath);

    /**
     * Deletes options file
     *
     * @param workingDirectory
     */
    public void deleteIni(String workingDirectory);

    /**
     * Given handler class, parses XML document within file (archive or not)
     *
     * @param _fileName full path of target file
     * @param _handler custom handler to consume XML document
     * @throws IOException on read error
     * @throws ParserConfigurationException on parser factory config error
     * @throws SAXException on XML interpretation error
     */
    public void parseHandler(String _fileName, CustHandler _handler)
            throws IOException, ParserConfigurationException, SAXException;

    public void parseHandlerByteArray(byte[] reversion, CustHandler _handler)
            throws ParserConfigurationException, IOException, SAXException;

    /**
     * Tests whether or not a file is a zip archive
     *
     * @param _fileName the file to test
     * @return true is passed file is a zip archive
     * @throws java.io.FileNotFoundException
     */
    public boolean isFileZipArchive(String _fileName) throws IOException;

    public void writeFile(String _fileName, Document doc, DictCore core, File workingDirectory, Instant saveTime)
            throws IOException, TransformerException;

    /**
     * Gets most recent temporary save file if one exists, null otherwise
     *
     * @param workingDirectory
     * @return
     */
    public File getTempSaveFileIfExists(File workingDirectory);
    
    /**
     * Moves file to archive folder with name prefixed with current epoch time
     * @param source
     * @param workingDirectory 
     * @return  
     * @throws IOException
     */
    public File archiveFile(File source, File workingDirectory) throws IOException;

    public void copyFile(Path fromLocation, Path toLocation, boolean replaceExisting) throws IOException;

    /**
     * Tests whether a file at a particular location exists. Wrapped to avoid IO
     * code outside this file
     *
     * @param fullPath path of file to test
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String fullPath);

    /**
     * Loads image assets from file. Does not load logographs due to legacy
     * coding/logic
     *
     * @param imageCollection from dictCore to populate
     * @param fileName of file containing assets
     * @throws java.io.IOException
     */
    public void loadImageAssets(ImageCollection imageCollection,
            String fileName) throws Exception;

    /**
     * loads all images into their logographs from archive and images into the
     * generalized image collection
     *
     * @param logoCollection logocollection from dictionary core
     * @param fileName name/path of archive
     * @throws java.lang.Exception
     */
    public void loadLogographs(LogoCollection logoCollection,
            String fileName) throws Exception;

    /**
     * Loads all reversion XML files from polyglot archive
     *
     * @param reversionManager reversion manager to load to
     * @param fileName full path of polyglot archive
     * @throws IOException on read error
     */
    public void loadReversionStates(ReversionManager reversionManager,
            String fileName) throws IOException;

    /**
     * Exports font in PGD to external file
     *
     * @param exportPath path to export to
     * @param dictionaryPath path of PGT dictionary
     * @throws IOException
     */
    public void exportFont(String exportPath, String dictionaryPath) throws IOException;

    /**
     * Exports Charis unicode font to specified location
     *
     * @param exportPath full export path
     * @throws IOException on failure
     */
    public void exportCharisFont(String exportPath) throws IOException;

    /**
     * Loads any related grammar recordings into the passed grammar manager via
     * id
     *
     * @param fileName name of file to load sound recordings from
     * @param grammarManager grammar manager to populate with sounds
     * @throws Exception on sound load errors
     */
    void loadGrammarSounds(String fileName, GrammarManager grammarManager) throws Exception;

    /**
     * Opens an arbitrary file via the local OS's default. If unable to open for
     * any reason, returns false.
     *
     * @param path
     * @return
     */
    public boolean openFileNativeOS(String path);

    /**
     * Returns deepest directory from given path (truncating non-directory files
     * from the end)
     *
     * @param path path to fetch directory from
     * @return File representing directory, null if unable to capture directory
     * path for any reason
     */
    public File getDirectoryFromPath(String path);

    /**
     * Wraps File so that I can avoid importing it elsewhere in code
     *
     * @param path path to file
     * @return file
     */
    public File getFileFromPath(String path);

    /**
     * Writes to the PolyGlot error log file
     *
     * @param exception
     */
    public void writeErrorLog(Throwable exception);

    /**
     * Writes to the PolyGlot error log file
     *
     * @param exception
     * @param comment
     */
    public void writeErrorLog(Throwable exception, String comment);

    public File getErrorLogFile();

    public String getErrorLog() throws FileNotFoundException;

    /**
     * Gets system information in human readable format
     *
     * @return system information
     */
    public String getSystemInformation();

    public File unzipResourceToTempLocation(String resourceLocation) throws IOException;

    /**
     * Unzips an internal resource to a targeted path.Does not check header.
     *
     * @param internalPath Path to internal zipped resource
     * @param target destination to unzip to
     * @throws java.io.IOException
     */
    public void unzipResourceToDir(String internalPath, Path target) throws IOException;
    
    /**
     * Adds a metadata attribute to an OSX file
     * @param filePath
     * @param attribute
     * @param value
     * @param isHexVal
     * @throws Exception if you try to run it on a nonOSX platform
     */
    public void addFileAttributeOSX(String filePath, String attribute, String value, boolean isHexVal) throws Exception;
    
    // TODO: If I ever need this, refine it. It currently gives very little back.
    // Consider returning object which specifies whether hex or string data. or something.
    // No need exept for testing at this point.
    public String getFileAttributeOSX(String filePath, String attribute) throws Exception;

    /**
     * Runs a command at the console, returning informational and error output.
     *
     * @param arguments command to run as [0], with arguments following
     * @param addSpaces whether blank string argument values should be changed
     * to a single space (some OSes will simply ignore arguments that are empty)
     * @return String array with two entries. [0] = Output, [1] = Error Output
     */
    public String[] runAtConsole(String[] arguments, boolean addSpaces);

    /**
     * Tests whether Java is available at the system/terminal level
     *
     * @return current version of Java, or blank if none available
     */
    public String getTerminalJavaVersion();

    /**
     * Tests whether Java can be called at the terminal
     *
     * @return
     */
    public boolean isJavaAvailableInTerminal();

    /**
     * Does what it says on the tin.Clear those carriage returns away.
     *
     * @param filthyWithWindows
     * @return
     */
    public byte[] clearCarrigeReturns(byte[] filthyWithWindows);
    
    public byte[] loadImageBytes(String path) throws IOException;
}
