/*
 * Copyright (c) 2019, Draque Thompson, draquemail@gmail.com
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

package org.darisadesigns.polyglotlina;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 *
 * @author draque
 */
public class Java8Bridge {

    public static File getNewJavaBridgeLocation() throws IOException {
        Path tmpDirectory = Files.createTempDirectory("PolyGlot");
        IOHandler.unzipResourceToDir(PGTUtil.JAVA8_BRIDGERESOURCE, tmpDirectory);
        return new File(tmpDirectory + File.separator + PGTUtil.JAVA8_JAR_FOLDER + File.separator + PGTUtil.JAVA8_JAR);
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
            DictCore core) throws IOException, InterruptedException {
        
        File bridge = PGTUtil.getJava8BridgeLocation();
        File tmpLangFile = createTmpLangFile(core);
        
        String[] command = {PGTUtil.JAVA8_JAVA_COMMAND,
            PGTUtil.JAVA8_JAR_ARG,
            bridge.getAbsolutePath(),
            PGTUtil.JAVA8_PDFCOMMAND,
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
            (printWordEtymologies ? PGTUtil.TRUE : PGTUtil.FALSE)
        };
        
        String[] result = IOHandler.runAtConsole(command);
        
        if (result[1].contains("ERROR") || !new File(target).exists()) {
            throw new IOException(result[1]);
        }
    }

    /**
     * Creates temporary csv file from excel sheet
     * @param excelFile path of excel file to convert to csv
     * @param sheetNumber sheet number to convert
     * @return csv file temp path to created csv file
     * @throws IOException
     * @throws InterruptedException 
     */
    public static File excelToCvs(String excelFile, int sheetNumber)
            throws IOException, InterruptedException {
        File bridge = PGTUtil.getJava8BridgeLocation();
        File tmpTarget = File.createTempFile("PolyGlotTmp", ".csv");
        
        String[] command = {
            PGTUtil.JAVA8_JAVA_COMMAND,
            PGTUtil.JAVA8_JAR_ARG,
            bridge.getAbsolutePath(),
            PGTUtil.JAVA8_EXCELTOCVSCOMMAND,
            excelFile,
            tmpTarget.getAbsolutePath(),
            Integer.toString(sheetNumber),
        };
        
        String[] result = IOHandler.runAtConsole(command);
        
        if (!result[1].isEmpty() || !tmpTarget.exists()) {
            throw new IOException(result[1]);
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
     * @throws java.lang.InterruptedException
     */
    public static void exportExcelDict(String fileName, DictCore core,
            boolean separateDeclensions) throws IOException, InterruptedException {
        if (!IOHandler.isJavaAvailableInTerminal()) {
            throw new IOException("Java runtime missing.");
        }
        
        File bridge = PGTUtil.getJava8BridgeLocation();
        File tmpLangFile = createTmpLangFile(core);
        
        String[] command = {
            PGTUtil.JAVA8_JAVA_COMMAND, // java
            PGTUtil.JAVA8_JAR_ARG, // -jar
            bridge.getAbsolutePath(), // path to bridge
            PGTUtil.JAVA8_EXPORTTOEXCELCOMMAND, // export command 
            tmpLangFile.getAbsolutePath(), // language file
            fileName, // target file
            (separateDeclensions ? PGTUtil.TRUE : PGTUtil.FALSE) // separate declensions
        };
        
        String[] result = IOHandler.runAtConsole(command);
        
        if (!result[1].isEmpty()) {
            throw new IOException("Unable to export to excel: " + result[1]);
        } else if (!new File(fileName).exists()) {
            throw new IOException("Unable to export to excel: File not found post export.");
        }
    }

    public class OutputInterceptor extends PrintStream {
        private String intercepted = "";

        public OutputInterceptor(OutputStream out) {
            super(out, true);
        }

        @Override
        public void print(String s) {
            super.print(s);
            intercepted += s;
        }

        public String getIntercepted() {
            return intercepted;
        }
    }
    
    private static File createTmpLangFile(DictCore core) throws IOException {
        File ret = File.createTempFile("PolyGlot", "LangFile");
        
        try {
            core.writeFile(ret.getAbsolutePath());
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            throw new IOException("Unable to save temp file to export from.", e);
        }
        
        return ret;
    }
}
