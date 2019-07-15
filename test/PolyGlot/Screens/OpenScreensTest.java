/*
 * Copyright (c) 2019, Draque Thompson
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
package PolyGlot.Screens;

import PolyGlot.DictCore;
import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.LexiconProblemNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * This test simply opens every window in a language that has basic values saved, then tests for errors
 * @author draque
 */
public class OpenScreensTest {
    private final File file;
    private final DictCore core;
    
    public OpenScreensTest() throws IOException {
        core = new DictCore();
        core.readFile("test/TestResources/basic_lang.pgd");
        
        file = new File("error_log.log");
        if (file.exists()) {
            file.delete();
        }
    }
    
    @Test
    public void testPTextInputDialog() throws Exception {
        PTextInputDialog s = new PTextInputDialog(null, core, "", "");
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrAbout () throws Exception {
        ScrAbout s = new ScrAbout(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeclensionGenSetup() throws Exception {
        ScrDeclensionGenSetup s = new ScrDeclensionGenSetup(core, core.getTypes().getNodes().get(0).getId());
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeclensionSetup() throws Exception {
        ScrDeclensionSetup s = new ScrDeclensionSetup(core, core.getTypes().getNodes().get(0).getId());
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeclensionGrids() throws Exception {
        ScrDeclensionsGrids s = new ScrDeclensionsGrids(core, core.getWordCollection().getWordNodes().get(0));
        s.setCloseWithoutSave(true);
        s.hardDispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeprecatedDeclensions() throws Exception {
        ScrDeprecatedDeclensions s = new ScrDeprecatedDeclensions(core, core.getWordCollection().getWordNodes().get(0));
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrEasterEgg() throws Exception {
        ScrEasterEgg s = new ScrEasterEgg();
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrEtymRoots() throws Exception {
        for (ConWord word : core.getWordCollection().getWordNodes()) {
            ScrEtymRoots s = new ScrEtymRoots(core, null, word);
            s.dispose();
        }
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrExcelImport() throws Exception {
        ScrExcelImport s = new ScrExcelImport(core, null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrFamilies() throws Exception {
        ScrFamilies s = new ScrFamilies(core, null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrGrammarGuide() throws Exception {
        ScrGrammarGuide s = new ScrGrammarGuide(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrIPARefChart() throws Exception {
        ScrIPARefChart s = new ScrIPARefChart(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrIpaTranslator() throws Exception {
        ScrIpaTranslator s = new ScrIpaTranslator(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLangProps() throws Exception {
        ScrLangProps s = new ScrLangProps(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLexicon() throws Exception {
        ScrLexicon s = new ScrLexicon(core, null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }

        @Test
    public void testScrLexiconProblemDisplay() throws Exception {
        List<LexiconProblemNode> problemNodes = new ArrayList<>();
        problemNodes.add(new LexiconProblemNode(new ConWord(), "PROBLEM"));
        ScrLexiconProblemDisplay s = new ScrLexiconProblemDisplay(problemNodes, core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLogoDetails() throws Exception {
        ScrLogoDetails s = new ScrLogoDetails(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLogoQuickView() throws Exception {
        ScrLogoQuickView s = new ScrLogoQuickView(core, core.getWordCollection().getWordNodes().get(0));
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrMainMenu() throws Exception {
        ScrMainMenu s = new ScrMainMenu("");
        s.updateAllValues(core);
        s.hardDispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrOptions() throws Exception {
        ScrOptions s = new ScrOptions(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrPhonology() throws Exception {
        ScrPhonology s = new ScrPhonology(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrPrintToPDF() throws Exception {
        ScrPrintToPDF s = new ScrPrintToPDF(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrProgressMenu() throws Exception {
        ScrProgressMenu s = new ScrProgressMenu("TEST", 1, false, false);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrQuickWordEntry() throws Exception {
        ScrQuickWordEntry s = new ScrQuickWordEntry(core, null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrReversion() throws Exception {
        ScrReversion s = new ScrReversion(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrTypes() throws Exception {
        ScrTypes s = new ScrTypes(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrUpdateAlert() throws Exception {
        ScrUpdateAlert s = new ScrUpdateAlert(false, core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrWordClasses() throws Exception {
        ScrWordClasses s = new ScrWordClasses(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    private void testExceptions(String scrName) throws Exception {
        if (file.exists()) {
            file.delete();
            throw new Exception("Errors opening or closing " + scrName);
        }
    }
}
