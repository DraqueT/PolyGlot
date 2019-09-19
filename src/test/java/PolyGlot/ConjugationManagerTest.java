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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.DeclensionManager;
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
        core = new DictCore();
    }
    
    @Test
    public void testZeroDimNoExtra() throws Exception {
        core.readFile(PGTUtil.TESTRESOURCES + "zero_dim_zero_extra_zero_dep.pgd");
        DeclensionManager decMan = core.getDeclensionManager();
        ConWord word = core.getWordCollection().getWordNodes().get(0);
        
        assertEquals(decMan.getAllCombinedIds(word.getWordTypeId()).size(), 0);
        assertEquals(decMan.getDimensionalDeclensionListWord(word.getId()).size(), 0);
        assertEquals(decMan.getDeprecatedForms(word).size(), 0);
        assertEquals(decMan.getSingletonDeclensionList(word.getId()).size(), 0);
        assertEquals(decMan.getDimensionalDeclensionListWord(word.getId()).size(), 0);
        assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
    }
    
    @Test
    public void testOneDimNoExtra() throws Exception {
        core.readFile(PGTUtil.TESTRESOURCES + "one_dim_zero_extra_zero_dep.pgd");
        DeclensionManager decMan = core.getDeclensionManager();
        ConWord word = core.getWordCollection().getWordNodes().get(0);
        List<String> expectedForms = Arrays.asList("testa", "testb");
        
        assertEquals(decMan.getAllCombinedIds(word.getWordTypeId()).size(), expectedForms.size());
        assertEquals(decMan.getDimensionalDeclensionListWord(word.getId()).size(), 0);
        assertEquals(decMan.getDeprecatedForms(word).size(), 0);
        assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
        assertTrue(allFormsPresent(decMan, word, expectedForms));
    }
    
    @Test
    public void testOneDimNoExtraFourDep() throws Exception {
        core.readFile(PGTUtil.TESTRESOURCES + "one_dim_zero_extra_four_dep.pgd");
        DeclensionManager decMan = core.getDeclensionManager();
        ConWord word = core.getWordCollection().getWordNodes().get(0);
        List<String> expectedForms = Arrays.asList("testa", "testb");
        List<String> expectedDeprecated = Arrays.asList("testaczzz", "testbczzz", "testadzzz", "testbdzzz");
        
        assertEquals(decMan.getAllCombinedIds(word.getWordTypeId()).size(), expectedForms.size());
        assertEquals(decMan.getDimensionalDeclensionListWord(word.getId()).size(), 4);
        assertEquals(decMan.getDeprecatedForms(word).size(), expectedDeprecated.size());
        assertEquals(decMan.getWordDeclensions(word.getId()).size(), 4);
        assertTrue(allFormsPresent(decMan, word, expectedForms));
        assertTrue(allDeprecatedFormsPresent(decMan, word, expectedDeprecated));
    }
    
    @Test
    public void testOneDimNoExtraNoDep() throws Exception {
        core.readFile(PGTUtil.TESTRESOURCES + "one_dim_zero_extra_zero_dep.pgd");
        DeclensionManager decMan = core.getDeclensionManager();
        ConWord word = core.getWordCollection().getWordNodes().get(0);
        List<String> expectedForms = Arrays.asList("testa", "testb");
        
        assertEquals(decMan.getAllCombinedIds(word.getWordTypeId()).size(), expectedForms.size());
        assertEquals(decMan.getDimensionalDeclensionListWord(word.getId()).size(), 0);
        assertEquals(decMan.getDeprecatedForms(word).size(), 0);
        assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
        assertTrue(allFormsPresent(decMan, word, expectedForms));
    }
    
    @Test
    public void testTwoDimOneExtraNoDep() throws Exception {
        core.readFile(PGTUtil.TESTRESOURCES + "two_dim_one_extra_zero_dep.pgd");
        DeclensionManager decMan = core.getDeclensionManager();
        ConWord word = core.getWordCollection().getWordNodes().get(0);
        List<String> expectedForms = Arrays.asList("testac", "testad", "testbc", "testbd", "testEXTRA");
        List<String> expectedDeprecated = Arrays.asList();
        
        assertEquals(decMan.getAllCombinedIds(word.getWordTypeId()).size(), expectedForms.size());
        assertEquals(decMan.getDimensionalDeclensionListWord(word.getId()).size(), 0);
        assertEquals(decMan.getDeprecatedForms(word).size(), expectedDeprecated.size());
        assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
        assertTrue(allFormsPresent(decMan, word, expectedForms));
        assertTrue(allDeprecatedFormsPresent(decMan, word, expectedDeprecated));
    }
    
    @Test
    public void testTwoDimOneExtraNoDepOneDisabled() throws Exception {
        core.readFile(PGTUtil.TESTRESOURCES + "two_dim_one_extra_zero_dep_one_disabled.pgd");
        DeclensionManager decMan = core.getDeclensionManager();
        ConWord word = core.getWordCollection().getWordNodes().get(0);
        List<String> expectedForms = Arrays.asList("testac", "testad", "testbc", "testEXTRA");
        List<String> expectedDeprecated = Arrays.asList();
        
        assertEquals(decMan.getAllCombinedIds(word.getWordTypeId()).size(), expectedForms.size() + 1); // includes surpressed form
        assertEquals(decMan.getDimensionalDeclensionListWord(word.getId()).size(), 0);
        assertEquals(decMan.getDeprecatedForms(word).size(), expectedDeprecated.size());
        assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
        assertTrue(allFormsPresent(decMan, word, expectedForms));
        assertTrue(allDeprecatedFormsPresent(decMan, word, expectedDeprecated));
    }
    
    @Test
    public void testTwoDimNoExtraNoDep() throws Exception {
        core.readFile(PGTUtil.TESTRESOURCES + "two_dim_zero_extra_zero_dep.pgd");
        DeclensionManager decMan = core.getDeclensionManager();
        ConWord word = core.getWordCollection().getWordNodes().get(0);
        List<String> expectedForms = Arrays.asList("testac", "testad", "testbc", "testbd");
        List<String> expectedDeprecated = Arrays.asList();
        
        assertEquals(decMan.getAllCombinedIds(word.getWordTypeId()).size(), expectedForms.size());
        assertEquals(decMan.getDimensionalDeclensionListWord(word.getId()).size(), 0);
        assertEquals(decMan.getDeprecatedForms(word).size(), expectedDeprecated.size());
        assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
        assertTrue(allFormsPresent(decMan, word, expectedForms));
        assertTrue(allDeprecatedFormsPresent(decMan, word, expectedDeprecated));
    }
    
    public boolean allDeprecatedFormsPresent(DeclensionManager decMan, 
            ConWord word, 
            List<String> expectedForms) throws Exception {
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

    public boolean allFormsPresent(DeclensionManager decMan, ConWord word, List<String> forms) throws Exception {
        boolean ret = true;
        
        List<DeclensionPair> pairs = decMan.getAllCombinedIds(word.getWordTypeId());
        List<String> finalForms = new ArrayList<>();
        
        for (DeclensionPair pair : pairs) {
            if (!decMan.isCombinedDeclSurpressed(pair.combinedId, word.getWordTypeId())) {
                finalForms.add(decMan.declineWord(word, pair.combinedId, word.getValue()));
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
