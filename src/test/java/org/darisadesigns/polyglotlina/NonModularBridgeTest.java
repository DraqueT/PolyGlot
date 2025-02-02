/*
 * Copyright (c) 2019-2023, Draque Thompson, draquemail@gmail.com
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

import TestResources.DummyCore;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.NonModularBridge;
import org.darisadesigns.polyglotlina.Desktop.ImportFileHelper;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class NonModularBridgeTest {
    private DictCore core;
    private static final String OUTPUT = "testFile.xls";
    
    public NonModularBridgeTest() {
        core = DummyCore.newCore();
    }

    @AfterEach
    public void tearDown() {
        cleanup();
    }

    @Test
    public void testGetNewJavaBridgeLocation() {
        System.out.println("NonModularBridgeTest.getNewJavaBridgeLocation");
        
        try {
            File result = NonModularBridge.getNewNonModularBridgeLocation();
            assertTrue(result.exists());
        } catch (IOException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }

    @Test
    public void testExportPdf() {
        // no current way to open/test contents of PDF, so just make sure it is created without error
        System.out.println("NonModularBridgeTest.exportPdf");
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
        boolean printPhrases = false;
        
        try {
            NonModularBridge.exportPdf(
                    OUTPUT, 
                    coverImage, 
                    foreward, 
                    printConLocal, 
                    printLocalCon, 
                    printOrtho, 
                    subTitleText, 
                    titleText, 
                    printPageNumber, 
                    printGlossKey, 
                    printGrammar, 
                    printWordEtymologies, 
                    printAllConjugations,
                    printPhrases,
                    "0,1,2,3,4,5",
                    core
            );
            assertTrue(new File(OUTPUT).exists());
        } catch (IOException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    private void cleanup() {
        new File(OUTPUT).delete();
        core = DummyCore.newCore();
    }
}
