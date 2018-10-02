/*
 * Copyright (c) 2018, DThompson
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

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author DThompson
 */
public class PExportToPDFTest {
    
    final DictCore core;
    
    public PExportToPDFTest() throws Exception {
        core = new DictCore();
        core.readFile("test/TestResources/Lodenkur_TEST.pgd");
    }

    @Test
    public void testPrint() throws Exception {
        String testPdfPath = "test/TestResources/Lodenkur_TEST.pdf";
        PExportToPDF testPrint = new PExportToPDF(core, testPdfPath);
        testPrint.setCoverImagePath("test/TestResources/EmptyImage.png");
        testPrint.setForewardText("Test Forward Text");
        testPrint.setPrintAllConjugations(true);
        testPrint.setPrintConLocal(true);
        testPrint.setPrintGlossKey(true);
        testPrint.setPrintGrammar(true);
        testPrint.setPrintLocalCon(true);
        testPrint.setPrintLocalCon(true);
        testPrint.setPrintOrtho(true);
        testPrint.setPrintPageNumber(true);
        testPrint.setSubTitleText("Test Subtitle Text");
        testPrint.setTitleText("Test Title Text");
        testPrint.print();
        File f = new File(testPdfPath);
        assert(f.exists());
        f.delete();
    }    
}
