/*
 * Copyright (c) 2018-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.ManagersCollections;
import TestResources.DummyCore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.DeclensionPair;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author DThompson
 */
public class ConjugationManagerTest {
    final DictCore core;

    public ConjugationManagerTest() {
        core = DummyCore.newCore();
    }
    
    @Test
    public void testZeroDimNoExtra() {
        try {
            System.out.println("ConjugationManagerTest.testZeroDimNoExtra");
            core.readFile(PGTUtil.TESTRESOURCES + "zero_dim_zero_extra_zero_dep.pgd");
            DeclensionManager decMan = core.getDeclensionManager();
            ConWord word = core.getWordCollection().getWordNodes()[0];

            assertEquals(0, decMan.getAllCombinedIds(word.getWordTypeId()).length);
            assertEquals(0, decMan.getDimensionalDeclensionListWord(word.getId()).length);
            assertEquals(decMan.getDeprecatedForms(word).size(), 0);
            assertEquals(0, decMan.getSingletonDeclensionList(word.getId()).length);
            assertEquals(0, decMan.getDimensionalDeclensionListWord(word.getId()).length);
            assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
        } catch (IOException | IllegalStateException e) {
            IOHandler.writeErrorLog(e, "testZeroDimNoExtra");
            fail(e);
        }
    }
    
    @Test
    public void testOneDimNoExtra() {
        try {
            System.out.println("ConjugationManagerTest.testOneDimNoExtra");
            core.readFile(PGTUtil.TESTRESOURCES + "one_dim_zero_extra_zero_dep.pgd");
            DeclensionManager decMan = core.getDeclensionManager();
            ConWord word = core.getWordCollection().getWordNodes()[0];
            String[] expectedForms = {"testa", "testb"};

            assertEquals(expectedForms.length, decMan.getAllCombinedIds(word.getWordTypeId()).length);
            assertEquals(0, decMan.getDimensionalDeclensionListWord(word.getId()).length);
            assertEquals(decMan.getDeprecatedForms(word).size(), 0);
            assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
            assertTrue(allFormsPresent(decMan, word, expectedForms));
        } catch (Exception e) {
            IOHandler.writeErrorLog(e, "testZeroDimNoExtra");
            fail(e);
        }
    }
    
    @Test
    public void testOneDimNoExtraFourDep() {
        try{
            System.out.println("ConjugationManagerTest.testOneDimNoExtraFourDep");
            core.readFile(PGTUtil.TESTRESOURCES + "one_dim_zero_extra_four_dep.pgd");
            DeclensionManager decMan = core.getDeclensionManager();
            ConWord word = core.getWordCollection().getWordNodes()[0];
            String[] expectedForms = {"testa", "testb"};
            List<String> expectedDeprecated = Arrays.asList("testaczzz", "testbczzz", "testadzzz", "testbdzzz");

            assertEquals(expectedForms.length, decMan.getAllCombinedIds(word.getWordTypeId()).length);
            assertEquals(4, decMan.getDimensionalDeclensionListWord(word.getId()).length);
            assertEquals(decMan.getDeprecatedForms(word).size(), expectedDeprecated.size());
            assertEquals(decMan.getWordDeclensions(word.getId()).size(), 4);
            assertTrue(allFormsPresent(decMan, word, expectedForms));
            assertTrue(allDeprecatedFormsPresent(decMan, word, expectedDeprecated));
        } catch (Exception e) {
            IOHandler.writeErrorLog(e, "testZeroDimNoExtra");
            fail(e);
        }
    }
    
    @Test
    public void testOneDimNoExtraNoDep() {
        try {
            System.out.println("ConjugationManagerTest.testOneDimNoExtraNoDep");
            core.readFile(PGTUtil.TESTRESOURCES + "one_dim_zero_extra_zero_dep.pgd");
            DeclensionManager decMan = core.getDeclensionManager();
            ConWord word = core.getWordCollection().getWordNodes()[0];
            String[] expectedForms = {"testa", "testb"};

            assertEquals(expectedForms.length, decMan.getAllCombinedIds(word.getWordTypeId()).length);
            assertEquals(0, decMan.getDimensionalDeclensionListWord(word.getId()).length);
            assertEquals(decMan.getDeprecatedForms(word).size(), 0);
            assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
            assertTrue(allFormsPresent(decMan, word, expectedForms));
        } catch (Exception e) {
            IOHandler.writeErrorLog(e, "testZeroDimNoExtra");
            fail(e);
        }
    }
    
    @Test
    public void testTwoDimOneExtraNoDep() {
        try{ 
            System.out.println("ConjugationManagerTest.testTwoDimOneExtraNoDep");
            core.readFile(PGTUtil.TESTRESOURCES + "two_dim_one_extra_zero_dep.pgd");
            DeclensionManager decMan = core.getDeclensionManager();
            ConWord word = core.getWordCollection().getWordNodes()[0];
            String[] expectedForms = {"testac", "testad", "testbc", "testbd", "testEXTRA"};
            List<String> expectedDeprecated = Arrays.asList();

            assertEquals(expectedForms.length, decMan.getAllCombinedIds(word.getWordTypeId()).length);
            assertEquals(0, decMan.getDimensionalDeclensionListWord(word.getId()).length);
            assertEquals(decMan.getDeprecatedForms(word).size(), expectedDeprecated.size());
            assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
            assertTrue(allFormsPresent(decMan, word, expectedForms));
            assertTrue(allDeprecatedFormsPresent(decMan, word, expectedDeprecated));
        } catch (Exception e) {
            IOHandler.writeErrorLog(e, "testZeroDimNoExtra");
            fail(e);
        }
    }
    
    @Test
    public void testTwoDimOneExtraNoDepOneDisabled() {
        try{
            System.out.println("ConjugationManagerTest.testTwoDimOneExtraNoDepOneDisabled");
            core.readFile(PGTUtil.TESTRESOURCES + "two_dim_one_extra_zero_dep_one_disabled.pgd");
            DeclensionManager decMan = core.getDeclensionManager();
            ConWord word = core.getWordCollection().getWordNodes()[0];
            String[] expectedForms = {"testac", "testad", "testbc", "testEXTRA"};
            List<String> expectedDeprecated = Arrays.asList();

            assertEquals(expectedForms.length + 1, decMan.getAllCombinedIds(word.getWordTypeId()).length); // includes surpressed form
            assertEquals(0, decMan.getDimensionalDeclensionListWord(word.getId()).length);
            assertEquals(decMan.getDeprecatedForms(word).size(), expectedDeprecated.size());
            assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
            assertTrue(allFormsPresent(decMan, word, expectedForms));
            assertTrue(allDeprecatedFormsPresent(decMan, word, expectedDeprecated));
        } catch (Exception e) {
            IOHandler.writeErrorLog(e, "testZeroDimNoExtra");
            fail(e);
        }
    }
    
    @Test
    public void testTwoDimNoExtraNoDep() {
        try {
            System.out.println("ConjugationManagerTest.testTwoDimOneExtraNoDepOneDisabled");
            core.readFile(PGTUtil.TESTRESOURCES + "two_dim_zero_extra_zero_dep.pgd");
            DeclensionManager decMan = core.getDeclensionManager();
            ConWord word = core.getWordCollection().getWordNodes()[0];
            String[] expectedForms = {"testac", "testad", "testbc", "testbd"};
            List<String> expectedDeprecated = Arrays.asList();

            assertEquals(expectedForms.length, decMan.getAllCombinedIds(word.getWordTypeId()).length);
            assertEquals(0, decMan.getDimensionalDeclensionListWord(word.getId()).length);
            assertEquals(decMan.getDeprecatedForms(word).size(), expectedDeprecated.size());
            assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
            assertTrue(allFormsPresent(decMan, word, expectedForms));
            assertTrue(allDeprecatedFormsPresent(decMan, word, expectedDeprecated));
        } catch (Exception e) {
            IOHandler.writeErrorLog(e, "testZeroDimNoExtra");
            fail(e);
        }
    }
    
    public boolean allDeprecatedFormsPresent(DeclensionManager decMan, 
            ConWord word, 
            List<String> expectedForms) {
        boolean ret = true;
        List<String> depForms = new ArrayList<>();
        
        decMan.getDeprecatedForms(word).values().forEach((depWord)->{
            depForms.add(depWord.getValue());
        });
        
        for (String expectedForm : expectedForms) {
            if (!depForms.contains(expectedForm)) {
                ret = false;
                break;
            }
        }
        
        return ret;
    }

    public boolean allFormsPresent(DeclensionManager decMan, ConWord word, String[] forms) throws Exception {
        boolean ret = true;
        
        DeclensionPair[] pairs = decMan.getAllCombinedIds(word.getWordTypeId());
        List<String> finalForms = new ArrayList<>();
        
        for (DeclensionPair pair : pairs) {
            if (!decMan.isCombinedDeclSurpressed(pair.combinedId, word.getWordTypeId())) {
                finalForms.add(decMan.declineWord(word, pair.combinedId));
            }
        }
        
        for (String form : forms) {
            if (!finalForms.contains(form)) {
                ret = false;
                break;
            }
        }
        
        return ret;
    }
}
