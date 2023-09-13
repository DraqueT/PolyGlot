/*
 * Copyright (c) 2014-2023, Draque Thompson, draquemail@gmail.com
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.zip.ZipFile;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.darisadesigns.polyglotlina.ManagersCollections.GrammarManager;
import org.darisadesigns.polyglotlina.ManagersCollections.LogoCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.ReversionManager;
import org.w3c.dom.Document;

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
    File createTmpFileWithContents(String contents, String extension) throws IOException;

    File createTmpFileFromImageBytes(byte[] imageBytes, String fileName) throws IOException;

    File createFileWithContents(String path, String contents) throws IOException;

    byte[] recoverFileBytesFromArchive(ZipFile zipFile, String targetFile) throws ParserConfigurationException;

    byte[] getByteArrayFromFile(File file) throws IOException;

    /**
     * Takes input stream and converts it to a raw byte array
     *
     * @param is
     * @return raw byte representation of stream
     * @throws IOException
     */
    byte[] streamToByteArray(InputStream is) throws IOException;

    /**
     * Used for snagging catchable versions of files
     *
     * @param filePath path of file to fetch as byte array
     * @return byte array of file at given path
     * @throws java.io.FileNotFoundException
     */
    byte[] getFileByteArray(String filePath) throws IOException;

    /**
     * returns name of file sans path
     *
     * @param fullPath full path to file
     * @return string of filename
     */
    String getFilenameFromPath(String fullPath);

    /**
     * Deletes options file
     *
     * @param workingDirectory
     */
    void deleteIni(String workingDirectory);

    /**
     * Tests whether or not a file is a zip archive
     *
     * @param _fileName the file to test
     * @return true is passed file is a zip archive
     * @throws java.io.FileNotFoundException
     */
    boolean isFileZipArchive(String _fileName) throws IOException;

    /**
     *
     * @param _fileName
     * @param doc
     * @param core
     * @param workingDirectory home directory
     * @param saveTime time of save initiation
     * @param writeToReversionMgr Whether to add this version to the reversion manager
     * @param forceClean Forces the cleaning of the temp file even on failure
     * @throws IOException
     * @throws TransformerException
     */
    void writeFile(
            String _fileName,
            Document doc,
            DictCore core,
            File workingDirectory,
            Instant saveTime,
            boolean writeToReversionMgr,
            boolean forceClean
    )
            throws IOException, TransformerException;

    /**
     * Gets most recent temporary save file if one exists, null otherwise
     *
     * @param workingDirectory
     * @return
     */
    File getTempSaveFileIfExists(File workingDirectory);

    /**
     * Moves file to archive folder with name prefixed with current epoch time
     *
     * @param source
     * @param workingDirectory
     * @return
     * @throws IOException
     */
    File archiveFile(File source, File workingDirectory) throws IOException;

    void copyFile(Path fromLocation, Path toLocation, boolean replaceExisting) throws IOException;

    /**
     * Tests whether a file at a particular location exists. Wrapped to avoid IO
     * code outside this file
     *
     * @param fullPath path of file to test
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String fullPath);

    void loadImageAssetWithId(InputStream imageStream, int imageId, DictCore core) throws IOException, Exception;

    /**
     * loads all images into their logographs from archive and images into the
     * generalized image collection
     *
     * @param logoCollection logocollection from dictionary core
     * @param zipFile
     * @throws java.io.IOException
     */
    void loadLogographs(LogoCollection logoCollection,
            ZipFile zipFile) throws IOException;

    /**
     * Loads all reversion XML files from polyglot archive
     *
     * @param reversionManager reversion manager to load to
     * @param zipFile
     * @throws IOException on read error
     */
    void loadReversionStates(ReversionManager reversionManager, ZipFile zipFile) throws IOException;

    /**
     * Exports font in PGD to external file
     *
     * @param exportPath path to export to
     * @param dictionaryPath path of PGT dictionary
     * @throws IOException
     */
    void exportConFont(String exportPath, String dictionaryPath) throws IOException;

    /**
     * Exports font in PGD to external file
     *
     * @param exportPath path to export to
     * @param dictionaryPath path of PGT dictionary
     * @throws IOException
     */
    void exportLocalFont(String exportPath, String dictionaryPath) throws IOException;

    /**
     * Exports Charis unicode font to specified location
     *
     * @param exportPath full export path
     * @throws IOException on failure
     */
    void exportCharisFont(String exportPath) throws IOException;

    /**
     * Loads any related grammar recordings into the passed grammar manager via
     * id
     *
     * @param zipFile archive being loaded from
     * @param grammarManager grammar manager to populate with sounds
     * @throws Exception on sound load errors
     */
    void loadGrammarSounds(ZipFile zipFile, GrammarManager grammarManager) throws Exception;

    /**
     * Opens an arbitrary file via the local OS's default. If unable to open for
     * any reason, returns false.
     *
     * @param path
     * @return
     */
    boolean openFileNativeOS(String path);

    /**
     * Returns deepest directory from given path (truncating non-directory files
     * from the end)
     *
     * @param path path to fetch directory from
     * @return File representing directory, null if unable to capture directory
     * path for any reason
     */
    File getDirectoryFromPath(String path);

    /**
     * Wraps File so that I can avoid importing it elsewhere in code
     *
     * @param path path to file
     * @return file
     */
    File getFileFromPath(String path);

    /**
     * Writes to the PolyGlot error log file
     *
     * @param exception
     */
    void writeErrorLog(Throwable exception);

    /**
     * Writes to the PolyGlot error log file
     *
     * @param exception
     * @param comment
     */
    void writeErrorLog(Throwable exception, String comment);

    File getErrorLogFile();

    String getErrorLog() throws IOException;

    void clearErrorLog() throws IOException;

    /**
     * Gets system information in human readable format
     *
     * @return system information
     */
    String getSystemInformation();

    File unzipResourceToTempLocation(String resourceLocation) throws IOException;

    /**
     * Unzips an internal resource to a targeted path.Does not check header.
     *
     * @param internalPath Path to internal zipped resource
     * @param target destination to unzip to
     * @throws java.io.IOException
     */
    void unzipResourceToDir(String internalPath, Path target) throws IOException;

    /**
     * Adds a metadata attribute to an OSX file
     *
     * @param filePath
     * @param attribute
     * @param value
     * @param isHexVal
     * @throws Exception if you try to run it on a nonOSX platform
     */
    void addFileAttributeOSX(String filePath, String attribute, String value, boolean isHexVal) throws Exception;

    /**
     * Runs a command at the console, returning informational and error output.
     *
     * @param arguments command to run as [0], with arguments following
     * @param addSpaces whether blank string argument values should be changed
     * to a single space (some OSes will simply ignore arguments that are empty)
     * @return String array with two entries. [0] = Output, [1] = Error Output
     */
    String[] runAtConsole(String[] arguments, boolean addSpaces);

    /**
     * Does what it says on the tin.Clear those carriage returns away.
     *
     * @param filthyWithWindows
     * @return
     */
    byte[] clearCarrigeReturns(byte[] filthyWithWindows);

    byte[] loadImageBytes(String path) throws IOException;

    /**
     * Reads from given file
     *
     * @param core
     * @param _fileName filename to read from
     * @param overrideXML override to where the XML should be loaded from
     * @return String array of two entries [0] = warnings, [1] = errors
     * @throws java.io.IOException for unrecoverable errors
     * @throws IllegalStateException for recoverable errors
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    String[] readFile(DictCore core, String _fileName, byte[] overrideXML) throws IOException, IllegalStateException, ParserConfigurationException;
}
