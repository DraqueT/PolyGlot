/*
 * Copyright (c) 2019-2022, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
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

package org.darisadesigns.polyglotlina.Desktop;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;

/**
 *
 * @author draque
 */
public final class NonModularBridge {
    
    private static File nonModularBridgeLocation = null;
    
    /**
     * Gets non modular java bridge class location. Caches value.
     *
     * @return
     * @throws java.io.IOException
     */
    public static File getNonModularBridgeLocation() throws IOException {
        if (nonModularBridgeLocation == null || !nonModularBridgeLocation.exists()) {
            nonModularBridgeLocation = NonModularBridge.getNewNonModularBridgeLocation();
        }

        return nonModularBridgeLocation;
    }

    public static File getNewNonModularBridgeLocation() throws IOException {
        Path tmpDirectory = Files.createTempDirectory("PolyGlot");
        DesktopIOHandler.getInstance().unzipResourceToDir(PGTUtil.JAVA_BRIDGERESOURCE, tmpDirectory);
        return new File(tmpDirectory + File.separator + PGTUtil.JAVA_JAR_FOLDER + File.separator + PGTUtil.JAVA_JAR);
    }

    public static void exportPdf(String target,
            String coverImage,
            String foreward,
            boolean printConLocal,
            boolean printLocalCon,
            boolean printOrtho,
            String subTitleText,
            String titleText,
            boolean printPageNumber,
            boolean printGlossKey,
            boolean printGrammar,
            boolean printWordEtymologies,
            boolean printAllConjugations,
            boolean printPhrases,
            String chapterOrder,
            DictCore core) throws IOException {
        
        File bridge = NonModularBridge.getNonModularBridgeLocation();
        File tmpLangFile = createTmpLangFile(core);
        File tmpConFontFile = File.createTempFile("PolyGlotConFont", ".ttf", core.getWorkingDirectory());
        File tmpLocalFontFile = File.createTempFile("PolyGlotLocalFont", ".ttf", core.getWorkingDirectory());
        tmpConFontFile.deleteOnExit();
        tmpLocalFontFile.deleteOnExit();
        
        String tmpConFontFileLocation = tmpConFontFile.getCanonicalPath();
        String tmpLocalFontFileLocation = tmpLocalFontFile.getCanonicalPath();
        
        try {
            DesktopIOHandler.getInstance().exportConFont(tmpConFontFileLocation, core.getCurFileName());
        } catch (IOException e) {
            tmpConFontFileLocation = "";
        }
        
        try {
            DesktopIOHandler.getInstance().exportLocalFont(tmpLocalFontFileLocation, core.getCurFileName());
        } catch (IOException e) {
            tmpLocalFontFileLocation = "";
        }
        
        if (tmpConFontFile.length() == 500) {
            tmpConFontFileLocation = "";
        }
        
        if (tmpLocalFontFile.length() < 500) {
            tmpLocalFontFileLocation = "";
        }
        
        String[] command = {
            getJavaExecutablePath(),
            PGTUtil.JAVA_JAR_ARG,
            bridge.getAbsolutePath(),
            PGTUtil.JAVA_PDFCOMMAND,
            tmpLangFile.getAbsolutePath(),
            target,
            titleText,
            subTitleText,
            coverImage,
            foreward,
            (printAllConjugations ? PGTUtil.TRUE : PGTUtil.FALSE),
            (printConLocal ? PGTUtil.TRUE : PGTUtil.FALSE),
            (printGlossKey ? PGTUtil.TRUE : PGTUtil.FALSE),
            (printGrammar ? PGTUtil.TRUE : PGTUtil.FALSE),
            (printLocalCon ? PGTUtil.TRUE : PGTUtil.FALSE),
            (printOrtho ? PGTUtil.TRUE : PGTUtil.FALSE),
            (printPageNumber ? PGTUtil.TRUE : PGTUtil.FALSE),
            (printWordEtymologies ? PGTUtil.TRUE : PGTUtil.FALSE),
            tmpConFontFileLocation,
            PGTUtil.PGT_VERSION,
            (printPhrases ? PGTUtil.TRUE : PGTUtil.FALSE),
            tmpLocalFontFileLocation,
            chapterOrder
        };
        
        String[] results = DesktopIOHandler.getInstance().runAtConsole(command, true);
        
        Set<String> warnings = new HashSet<>();
        
        for (String result : results) {
            result = result.toLowerCase();
            
            if (result.contains("error")) {
                throw new IOException("Unable to print to PDF: " + result);
            } else if (result.contains("warning") && !warnings.contains(result)) {
                warnings.add(result);
            }
        }
        
        String warningString = "";
        
        for (String warning : warnings.toArray(new String[0])) {
            warningString += warning + "\n";
        }
        
        if (!warningString.isBlank()) {
            
            new DesktopInfoBox().warning("PDF Print Warnings", 
                    "The following warnings were generated in the print process:\n" + warningString);
        }
        
        if (!new File(target).exists()) {
            String resultsString = String.join("\n", results);
            DesktopIOHandler.getInstance().writeErrorLog(new Exception(), resultsString);
            throw new IOException("Unable to print to PDF for unknown reasons. Please contact developer with details:\n" + resultsString);
        }
    }

    /**
     * Creates temporary csv file from excel sheet
     * @param excelFile path of excel file to convert to csv
     * @param sheetNumber sheet number to convert
     * @return csv file temp path to created csv file
     * @throws IOException
     */
    public static File excelToCvs(String excelFile, int sheetNumber)
            throws IOException {
        
        File bridge = NonModularBridge.getNonModularBridgeLocation();
        File tmpTarget = File.createTempFile("PolyGlotTmp", ".csv");
        
        String[] command = {
            getJavaExecutablePath(),
            PGTUtil.JAVA_JAR_ARG,
            bridge.getAbsolutePath(),
            PGTUtil.JAVA_EXCELTOCVSCOMMAND,
            excelFile,
            tmpTarget.getAbsolutePath(),
            Integer.toString(sheetNumber),
        };
        
        String[] result = DesktopIOHandler.getInstance().runAtConsole(command, false);
        
        if (!result[1].isEmpty() || !tmpTarget.exists()) {
            throw new IOException(result[1]);
        } else if (result[0].toLowerCase().contains("error") || result[0].toLowerCase().contains("exception")) {
            throw new IOException("Unable to import excel: " + result[0]);
        }
        
        return tmpTarget;
    }

    /**
     * Exports a dictionary to an excel file (externally facing)
     *
     * @param fileName Filename to export to
     * @param core dictionary core
     * @param separateDeclensions whether to separate parts of speech into
     * separate pages for declension values
     * @throws java.io.IOException
     */
    public static void exportExcelDict(String fileName, DictCore core,
            boolean separateDeclensions) throws IOException {
        
        File bridge = NonModularBridge.getNonModularBridgeLocation();
        File tmpLangFile = createTmpLangFile(core);
        
        String[] command = {
            getJavaExecutablePath(),
            PGTUtil.JAVA_JAR_ARG, // -jar
            bridge.getAbsolutePath(), // path to bridge
            PGTUtil.JAVA_EXPORTTOEXCELCOMMAND, // export command 
            tmpLangFile.getAbsolutePath(), // language file
            fileName, // target file
            (separateDeclensions ? PGTUtil.TRUE : PGTUtil.FALSE) // separate declensions
        };
        
        String[] result = DesktopIOHandler.getInstance().runAtConsole(command, false);
        
        if (!result[1].isEmpty()) {
            throw new IOException("Unable to export to excel: " + result[1]);
        } else if (result[0].toLowerCase().contains("error") || result[0].toLowerCase().contains("exception")) {
            throw new IOException("Unable to export to excel: " + result[0]);
        } else if (!new File(fileName).exists()) {
            throw new IOException("Unable to export to excel: File not found post export.");
        }
    }

    public static class OutputInterceptor extends PrintStream {
        //private String intercepted = "";

        public OutputInterceptor(OutputStream _out) {
            super(_out, true);
        }

        @Override
        public void print(String s) {
            super.print(s);
            //intercepted += s;
        }
    }
    
    private static File createTmpLangFile(DictCore core) throws IOException {
        File ret = File.createTempFile("PolyGlot", "LangFile");
        
        try {
            core.writeFile(ret.getAbsolutePath(), false);
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            throw new IOException("Unable to save temp file to export from.", e);
        }
        
        return ret;
    }
    
    // TODO: Test that the location of java is valid on program startup (since this cannot be done in pre-compiled testing)
    /**
     * Gets executable location of java - differs per OS
     * When in dev mode, it may be presumed that a valid version
     * of Java is on the system path
     * @return 
     */
    private static String getJavaExecutablePath() {
        String macOSEnvVar = "DYLD_LIBRARY_PATH";
        String path = PGTUtil.JAVA_JAVA_COMMAND; // java
        
        if (PGTUtil.IS_OSX) {
            if (System.getenv().containsKey(macOSEnvVar)) {
                // ":Applications/PolyGlot.app/Contents/app" -> Applications/PolyGlot.app/Contents/runtime/Contents/Home/bin/java"
                path = System.getenv(macOSEnvVar).substring(1).replaceAll("/app", "/runtime/Contents/Home/bin") + "/" + path;
            }
        } else if(PGTUtil.IS_LINUX) {
            
        } else if (PGTUtil.IS_WINDOWS) {
            
        }
        
        return path;
    }

    private NonModularBridge() {}
}
