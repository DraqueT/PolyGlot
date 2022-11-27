/*
 * Copyright (c) 2019-2022, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Screens;

import TestResources.DummyCore;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.LexiconProblemNode;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.QuizEngine.Quiz;
import org.darisadesigns.polyglotlina.QuizEngine.QuizQuestion;
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
        PGTUtil.enterUITestingMode();
        core = DummyCore.newCore();
        errors = DesktopIOHandler.getInstance().getErrorLogFile();
        
        try {
            core.readFile(PGTUtil.TESTRESOURCES + "basic_lang.pgd");

            if (errors.exists()) {
                errors.delete();
            }
        } catch (IOException | IllegalStateException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testPTextInputDialog() {
        System.out.println("OpenScreensTest.testPTextInputDialog");
        
        if (headless) {
            return;
        }
        
        Window parent = new Window(new Frame());
        PTextInputDialog s = new PTextInputDialog(parent, core, "", "");
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrAbout () {
        System.out.println("OpenScreensTest.testScrAbout");
        
        if (headless) {
            return;
        }
        
        ScrAbout s = new ScrAbout(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeclensionGenSetup() {
        System.out.println("OpenScreensTest.testScrDeclensionGenSetup");
        
        if (headless) {
            return;
        }
        
        ScrDeclensionGenSetup s = new ScrDeclensionGenSetup(core, core.getTypes().getNodes()[0].getId());
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeclensionSetup() {
        System.out.println("OpenScreensTest.testScrDeclensionSetup");
        
        if (headless) {
            return;
        }
        
        ScrDeclensionSetup s = new ScrDeclensionSetup(core, core.getTypes().getNodes()[0].getId());
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeclensionGrids() {
        System.out.println("OpenScreensTest.testScrDeclensionGrids");
        
        if (headless) {
            return;
        }
        
        ScrDeclensionsGrids s = new ScrDeclensionsGrids(core, core.getWordCollection().getWordNodes()[0]);
        s.setCloseWithoutSave(true);
        s.hardDispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeprecatedDeclensions() {
        System.out.println("OpenScreensTest.testScrDeprecatedDeclensions");
        
        if (headless) {
            return;
        }
        
        ScrDeprecatedDeclensions s = new ScrDeprecatedDeclensions(core, core.getWordCollection().getWordNodes()[0]);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrEasterEgg() {
        System.out.println("OpenScreensTest.testScrEasterEgg");
        
        if (headless) {
            return;
        }
        
        ScrEasterEgg s = new ScrEasterEgg();
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrEtymRoots() {
        System.out.println("OpenScreensTest.testScrEtymRoots");
        
        if (headless) {
            return;
        }
        
        for (ConWord word : core.getWordCollection().getWordNodes()) {
            ScrEtymRoots s = new ScrEtymRoots(core, word);
            s.dispose();
        }
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrExcelImport() {
        System.out.println("OpenScreensTest.testScrExcelImport");
        
        if (headless) {
            return;
        }
        
        ScrExcelImport s = new ScrExcelImport(core, null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrFamilies() {
        System.out.println("OpenScreensTest.testScrFamilies");
        
        if (headless) {
            return;
        }
        
        ScrFamilies s = new ScrFamilies(core, null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrGrammarGuide() {
        System.out.println("OpenScreensTest.testScrGrammarGuide");
        
        if (headless) {
            return;
        }
        
        ScrGrammarGuide s = new ScrGrammarGuide(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrIPARefChart() {
        System.out.println("OpenScreensTest.testScrIPARefChart");
        
        if (headless) {
            return;
        }
        
        ScrIPARefChart s = new ScrIPARefChart(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrIpaTranslator() {
        System.out.println("OpenScreensTest.testScrIpaTranslator");
        
        if (headless) {
            return;
        }
        
        ScrIpaTranslator s = new ScrIpaTranslator(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLangProps() {
        System.out.println("OpenScreensTest.testScrLangProps");
        
        if (headless) {
            return;
        }
        
        ScrLangProps s = new ScrLangProps(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLexicon() {
        System.out.println("OpenScreensTest.testScrLexicon");
        
        if (headless) {
            return;
        }
        
        try {
            ScrLexicon s = new ScrLexicon(core, null);
            s.dispose();
        } catch (Exception e) {
            // e.printStackTrace();
            fail(e);
        }
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }

        @Test
    public void testScrLexiconProblemDisplay() {
        System.out.println("OpenScreensTest.testScrLexiconProblemDisplay");
        
        if (headless) {
            return;
        }
        
        List<LexiconProblemNode> problemNodes = new ArrayList<>();
        problemNodes.add(new LexiconProblemNode(
                new ConWord(), 
                "PROBLEM", 
                LexiconProblemNode.ProblemType.ConWord,
                LexiconProblemNode.SEVARITY_ERROR
        ));
        ScrLanguageProblemDisplay s = new ScrLanguageProblemDisplay(problemNodes, core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLogoDetails() {
        System.out.println("OpenScreensTest.testScrLogoDetails");
        
        if (headless) {
            return;
        }
        
        ScrLogoDetails s = new ScrLogoDetails(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrLogoQuickView() {
        System.out.println("OpenScreensTest.testScrLogoQuickView");
        
        if (headless) {
            return;
        }
        
        ScrLogoQuickView s = new ScrLogoQuickView(core, core.getWordCollection().getWordNodes()[0]);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrMainMenu() {
        System.out.println("OpenScreensTest.testScrMainMenu");
        
        if (headless) {
            return;
        }
        
        ScrMainMenu s = new ScrMainMenu(DummyCore.newCore());
        s.updateAllValues(core);
        s.hardDispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrOptions() {
        System.out.println("OpenScreensTest.testScrOptions");
        
        if (headless) {
            return;
        }
        
        ScrOptions s = new ScrOptions(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrPhonology() {
        System.out.println("OpenScreensTest.testScrPhonology");
        
        if (headless) {
            return;
        }
        
        ScrPhonology s = new ScrPhonology(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrPrintToPDF() {
        System.out.println("OpenScreensTest.testScrPrintToPDF");
        
        if (headless) {
            return;
        }
        
        ScrPrintToPDF s = new ScrPrintToPDF(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrProgressMenu() {
        System.out.println("OpenScreensTest.testScrProgressMenu");
        
        if (headless) {
            return;
        }
        
        ScrProgressMenu s = new ScrProgressMenu("TEST", 1, false, false);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrQuickWordEntry() {
        System.out.println("OpenScreensTest.testScrQuickWordEntry");
        
        if (headless) {
            return;
        }
        
        ScrQuickWordEntry s = new ScrQuickWordEntry(core, null);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrReversion() {
        System.out.println("OpenScreensTest.testScrReversion");
        
        if (headless) {
            return;
        }
        
        ScrReversion s = new ScrReversion(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrTypes() {
        System.out.println("OpenScreensTest.testScrTypes");
        
        if (headless) {
            return;
        }
        
        ScrTypes s = new ScrTypes(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrUpdateAlert() {
        System.out.println("OpenScreensTest.testScrUpdateAlert");
        
        if (headless) {
            return;
        }
        
        try {
            // TODO: Re-enstate this later when I have network access...
            // TODO: Rewrite so that these are skipped when there is no internet onnection (use Assumptions as above)
//            ScrUpdateAlert s = new ScrUpdateAlert(false, core);
//            s.dispose();
//            testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testScrWordClasses() {
        System.out.println("OpenScreensTest.testScrWordClasses");
        
        if (headless) {
            return;
        }
        
        ScrWordClasses s = new ScrWordClasses(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrQuizScreen() {
        System.out.println("OpenScreensTest.testScrQuizScreen");
        
        if (headless) {
            return;
        }
        
        try {
            Quiz testQuiz = new Quiz(core);
            QuizQuestion testQuestion = new QuizQuestion(core);
            testQuestion.addChoice(new ConWord());
            testQuestion.setAnswer(new ConWord());
            testQuestion.setType(QuizQuestion.QuestionType.Local);
            testQuestion.setSource(new ConWord());
            testQuiz.addNode(testQuestion);

            ScrQuizScreen s = new ScrQuizScreen(testQuiz, core);
            s.dispose();
        } catch (Exception e) {
            fail(e);
        }
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrTestWordConj() {
        System.out.println("OpenScreensTest.testScrTestWordConj");
        
        if (headless) {
            return;
        }
        
        try {
            int type = core.getTypes().addNode(new TypeNode());

            ScrTestWordConj s = new ScrTestWordConj(core, type , null);
            s.dispose();
        } catch (Exception e) {
            fail(e);
        }
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrQuizGenDialog() {
        System.out.println("OpenScreensTest.testScrQuizGenDialog");
        
        if (headless) {
            return;
        }
        
        ScrQuizGenDialog s = new ScrQuizGenDialog(core);
        s.dispose();
        
        testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
    }
    
    @Test
    public void testScrDeclensionGenSimple() {
        System.out.println("OpenScreensTest.testScrDeclensionGenSimple");
        
        if (headless) {
            return;
        }
        
        try {
            int type = core.getTypes().addNode(new TypeNode());

            ScrDeclensionGenSimple s = new ScrDeclensionGenSimple(core, type);
            s.dispose();

            testExceptions(new Object() {}.getClass().getEnclosingMethod().getName());
        } catch (Exception e) {
            fail(e);
        }
    }
    
    private void testExceptions(String scrName) {
        if (headless) {
            return;
        }
        
        if (errors.exists()) {
            String failState = "Errors opening or closing " + scrName + ": \n";
            
            try {
                failState += new String (Files.readAllBytes(errors.toPath()));
            } catch (IOException e) {
                failState += "UNABLE TO READ ERROR LOG: " + e.getLocalizedMessage();
            }
            
            errors.delete();
            fail(failState);
        }
    }
}