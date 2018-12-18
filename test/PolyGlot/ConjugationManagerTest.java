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
import PolyGlot.ManagersCollections.DeclensionManager;
import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.DeclensionPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

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
        core.readFile("test/TestResources/zero_dim_zero_extra_zero_dep.pgd");
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
        core.readFile("test/TestResources/one_dim_zero_extra_zero_dep.pgd");
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
        core.readFile("test/TestResources/one_dim_zero_extra_four_dep.pgd");
        DeclensionManager decMan = core.getDeclensionManager();
        ConWord word = core.getWordCollection().getWordNodes().get(0);
        List<String> expectedForms = Arrays.asList("testa", "testb");
        
        assertEquals(decMan.getAllCombinedIds(word.getWordTypeId()).size(), expectedForms.size());
        assertEquals(decMan.getDimensionalDeclensionListWord(word.getId()).size(), 0);
        assertEquals(decMan.getDeprecatedForms(word).size(), 0);
        assertEquals(decMan.getWordDeclensions(word.getId()).size(), 0);
        assertTrue(allFormsPresent(decMan, word, expectedForms));
    }

    public boolean allFormsPresent(DeclensionManager decMan, ConWord word, List<String> forms) throws Exception {
        boolean ret = true;
        
        List<DeclensionPair> pairs = decMan.getAllCombinedIds(word.getWordTypeId());
        List<String> finalForms = new ArrayList<>();
        
        for (DeclensionPair pair : pairs) {
            finalForms.add(decMan.declineWord(word, pair.combinedId, word.getValue()));
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
