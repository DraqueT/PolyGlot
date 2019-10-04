/*
 * Copyright (c) 2019, draque
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author draque
 */
public class Java8BridgeTest {
    private DictCore core;
    private final static String OUTPUT = "testFile";
    
    public Java8BridgeTest() {
        core = new DictCore();
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
        cleanup();
    }

    @Test
    public void testGetNewJavaBridgeLocation() throws Exception {
        System.out.println("getNewJavaBridgeLocation");
        File result = Java8Bridge.getNewJavaBridgeLocation();
        assertTrue(result.exists());
    }

    @Test
    public void testExportPdf() throws Exception {
        // no current way to open/test contents of PDF, so just make sure it is created without error
        System.out.println("exportPdf");
        String target = OUTPUT;
        String coverImage = PGTUtil.TESTRESOURCES + "test.jpg";
        String foreward = "blap";
        boolean printConLocal = false;
        boolean printLocalCon = false;
        boolean printOrtho = false;
        String subTitleText = "bloop";
        String titleText = "bleep";
        boolean printPageNumber = false;
        boolean printGlossKey = false;
        boolean printGrammar = false;
        boolean printWordEtymologies = false;
        boolean printAllConjugations = false;
        Java8Bridge.exportPdf(target, coverImage, foreward, printConLocal, printLocalCon, printOrtho, subTitleText, titleText, printPageNumber, printGlossKey, printGrammar, printWordEtymologies, printAllConjugations, core);
        
        assertTrue(new File(OUTPUT).exists());
    }

    @Test
    public void testExcelToCvs() throws Exception {
        System.out.println("excelToCvs");
        String expectedContents = "\"COL 1\",\"COL 2\",\"COL 3\"\n" +
            "\"A\",\"AA\",\"AAA\"\n" +
            "\"B\"\n" +
            "\"C\",\"CC\",\"CCC\",\"CCCC\"\n" +
            "\"E\",,\"EEE\"\n" +
            "\"F\",\"F\n" +
            "F\",\"F\n" +
            "F\n" +
            "F\"\n" +
            "\"\"\"G\"\"\",\"G'\",\",G\"";
        String excelFile = PGTUtil.TESTRESOURCES + "excelImport.xlsx";
        int sheetNumber = 0;
        
        File result = Java8Bridge.excelToCvs(excelFile, sheetNumber);
        String outputContents = readFile(result.getAbsolutePath());
        
        assertTrue(result.exists());
        assertEquals(outputContents, expectedContents);
    }
 
    @Test
    public void testExportExcelDict() throws Exception {
        System.out.println("exportExcelDict");
        String os = System.getProperty("os.name").toLowerCase();
        
        core.readFile(PGTUtil.TESTRESOURCES + "excel_exp_test.pgd");
        boolean separateDeclensions = true;
        Java8Bridge.exportExcelDict(OUTPUT, core, separateDeclensions);
        File tmpExcel = new File(OUTPUT);
        
        for (int i = 0 ; i < 6; i++) {
            File expectedFile = new File(PGTUtil.TESTRESOURCES + "excel_export_check_" + i + ".csv");
            File result = Java8Bridge.excelToCvs(tmpExcel.getAbsolutePath(), i);
            
            byte[] expBytes = Files.readAllBytes(expectedFile.toPath());
            byte[] resBytes = Files.readAllBytes(result.toPath());

            // windows expected file will be in crlf (due to git translation of file)... gotta sanitize.
            int carRet = 13;
            if (os.toLowerCase().contains("win")) {
                int index = 0; 
                for (int k = 0; k < expBytes.length; k++) {
                   if (expBytes[k] != carRet) {
                      expBytes[index++] = expBytes[k];
                   }
                }
                
               expBytes = Arrays.copyOf(expBytes, index); 
            }
            
            assertTrue(Arrays.equals(expBytes, resBytes));
        }  
    }
    
    private void cleanup() {
        new File(OUTPUT).delete();
        core = new DictCore();
    }
    
    private String readFile(String fileIn) throws FileNotFoundException, IOException {
        String ret = "";
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileIn))) {
            String line = reader.readLine();
            
            while (line != null) {
                ret += line + "\n";
                line = reader.readLine();
            }
            
            if (!ret.isEmpty()) {
                ret = ret.substring(0, ret.length() - 1);
            }
        } catch (FileNotFoundException e) {
            ret = null;
        }
        
        return ret;
    }
}
