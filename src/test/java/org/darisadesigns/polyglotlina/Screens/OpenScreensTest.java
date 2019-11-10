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
package org.darisadesigns.polyglotlina.Screens;

import TestResources.DummyCore;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.LexiconProblemNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 * This test simply opens every window in a language that has basic values saved, then tests for errors
 * @author draque
 */
public class OpenScreensTest {
    private final File errors;
    private final DictCore core;
    private final boolean headless = GraphicsEnvironment.isHeadless(); // testing this in a headless environment makes no sense
    
    public OpenScreensTest() {
        core = DummyCore.newCore();
        errors = IOHandler.getErrorLogFile();
        
        try {
            core.readFile(PGTUtil.TESTRESOURCES + "basic_lang.pgd");

            if (errors.exists()) {
                errors.delete();
            }
        } catch (IOException | IllegalStateException e) {
            IOHandler.writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testPTextInputDialog() {
        if (headless) {
            return;
        }
        
        System.out.println("testPTextInputDialog");
        PTextInputDialog s = new PTextInputDialog(new ScrAbout(core), core, "", "");
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrAbout () {
        if (headless) {
            return;
        }
        
        System.out.println("testScrAbout");
        ScrAbout s = new ScrAbout(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeclensionGenSetup() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrDeclensionGenSetup");
        ScrDeclensionGenClassic s = new ScrDeclensionGenClassic(core, core.getTypes().getNodes().get(0).getId(), null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeclensionSetup() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrDeclensionSetup");
        ScrDeclensionSetup s = new ScrDeclensionSetup(core, core.getTypes().getNodes().get(0).getId());
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeclensionGrids() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrDeclensionGrids");
        ScrDeclensionsGrids s = new ScrDeclensionsGrids(core, core.getWordCollection().getWordNodes().get(0));
        s.setCloseWithoutSave(true);
        s.hardDispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeprecatedDeclensions() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrDeprecatedDeclensions");
        ScrDeprecatedDeclensions s = new ScrDeprecatedDeclensions(core, core.getWordCollection().getWordNodes().get(0));
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrEasterEgg() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrEasterEgg");
        ScrEasterEgg s = new ScrEasterEgg();
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrEtymRoots() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrEtymRoots");
        for (ConWord word : core.getWordCollection().getWordNodes()) {
            ScrEtymRoots s = new ScrEtymRoots(core, word);
            s.dispose();
        }
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrExcelImport() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrExcelImport");
        ScrExcelImport s = new ScrExcelImport(core, null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrFamilies() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrFamilies");
        ScrFamilies s = new ScrFamilies(core, null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrGrammarGuide() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrGrammarGuide");
        ScrGrammarGuide s = new ScrGrammarGuide(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrIPARefChart() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrIPARefChart");
        ScrIPARefChart s = new ScrIPARefChart(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrIpaTranslator() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrIpaTranslator");
        ScrIpaTranslator s = new ScrIpaTranslator(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLangProps() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrLangProps");
        ScrLangProps s = new ScrLangProps(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLexicon() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrLexicon");
        ScrLexicon s = new ScrLexicon(core, null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }

        @Test
    public void testScrLexiconProblemDisplay() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrLexiconProblemDisplay");
        List<LexiconProblemNode> problemNodes = new ArrayList<>();
        problemNodes.add(new LexiconProblemNode(new ConWord(), "PROBLEM"));
        ScrLexiconProblemDisplay s = new ScrLexiconProblemDisplay(problemNodes, core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLogoDetails() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrLogoDetails");
        ScrLogoDetails s = new ScrLogoDetails(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLogoQuickView() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrLogoQuickView");
        ScrLogoQuickView s = new ScrLogoQuickView(core, core.getWordCollection().getWordNodes().get(0));
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrMainMenu() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrMainMenu");
        ScrMainMenu s = new ScrMainMenu(DummyCore.newCore());
        s.updateAllValues(core);
        s.hardDispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrOptions() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrOptions");
        ScrOptions s = new ScrOptions(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrPhonology() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrPhonology");
        ScrPhonology s = new ScrPhonology(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrPrintToPDF() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrPrintToPDF");
        ScrPrintToPDF s = new ScrPrintToPDF(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrProgressMenu() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrProgressMenu");
        ScrProgressMenu s = new ScrProgressMenu("TEST", 1, false, false);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrQuickWordEntry() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrQuickWordEntry");
        ScrQuickWordEntry s = new ScrQuickWordEntry(core, null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrReversion() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrReversion");
        ScrReversion s = new ScrReversion(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrTypes() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrTypes");
        ScrTypes s = new ScrTypes(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrUpdateAlert() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrUpdateAlert");
        
        try {
            ScrUpdateAlert s = new ScrUpdateAlert(false, core);
            s.dispose();

            testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
        } catch (Exception e) {
            IOHandler.writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testScrWordClasses() {
        if (headless) {
            return;
        }
        
        System.out.println("testScrWordClasses");
        ScrWordClasses s = new ScrWordClasses(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    private void testExceptions(String scrName) {
        if (headless) {
            return;
        }
        
        if (errors.exists()) {
            errors.delete();
            fail("Errors opening or closing " + scrName);
        }
    }
}