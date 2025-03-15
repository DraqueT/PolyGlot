/*
 * Copyright (c) 2019-2025, Draque Thompson, draquemail@gmail.com
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.ExportFileHelper;
import org.darisadesigns.polyglotlina.Desktop.ImportFileHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import TestResources.DummyCore;

public class ExportFileHelperTest {
    private DictCore core;
    private static final String OUTPUT = "testFile.xls";
    public ExportFileHelperTest() {
        core = DummyCore.newCore();
    }

    @AfterEach
    public void tearDown() {
        new File(OUTPUT).delete();
        core = DummyCore.newCore();
    }
 
    @Test
    public void testExportExcelDict() {
        System.out.println("ExportFileHelperTest.testExportExcelDict");
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            core.readFile(PGTUtil.TESTRESOURCES + "excel_exp_test.pgd");
            boolean separateDeclensions = true;
            ExportFileHelper.exportExcelToDict(OUTPUT, core, separateDeclensions);
            File tmpExcel = new File(OUTPUT);

            for (int i = 0 ; i < 6; i++) {
                File expectedFile = new File(PGTUtil.TESTRESOURCES + "excel_export_check_" + i + ".csv");
                File result = ImportFileHelper.convertExcelToCSV(tmpExcel.getAbsolutePath(), i);
                
                // On modification of functionality, check output, then uncomment below to update test files if good
//                expectedFile.delete();
//                Files.copy(result.toPath(), expectedFile.toPath());

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
        } catch (IOException | IllegalStateException | ParserConfigurationException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }

    @Test
    public void testExportPdf() {
        System.out.println("ExportFileHelperTest.testExportPdf");
        core.getPropertiesManager().setCopyrightAuthorInfo("Copyright testing");
        try {
            ExportFileHelper.exportMarkdown(
                "test.md", 
                "", 
                "this is a test", 
                false, 
                false, 
                true,
                "Example Sub-Title",
                "Example Title", 
                false, 
                false, 
                false, 
                false, 
                false, 
                false,
                "0,1,2,3,4,5",
                core);
        } catch (IOException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
}
