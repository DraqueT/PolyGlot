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
package PolyGlot.ManagersCollections;

import PolyGlot.DictCore;
import PolyGlot.Nodes.DeclensionGenRule;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author DThompson
 */
public class DeclensionManagerTest {
    final DictCore core;
    final DeclensionManager decMan;
    
    public DeclensionManagerTest() throws Exception {
        core = new DictCore();
        core.readFile("test/TestResources/Lodenkur_TEST.pgd");
        decMan = core.getDeclensionManager();
    }

    /**
     * In the test Lodenkur file, there is a second dimensional value added to 
     * adverbials specifically to test this feature. It is disabled.
     */
    @Test
    public void testIsCombinedDeclSurpressed() {
        assertTrue(decMan.isCombinedDeclSurpressed(",3,", 3));
    }

    @Test
    public void testSetCombinedDeclSurpressed() {
        decMan.setCombinedDeclSurpressed(",3,", 4, false);
        assertFalse(decMan.isCombinedDeclSurpressed(",3,", 4));
    }

    @Test
    public void testSetCombinedDeclSurpressedRaw() {
        decMan.setCombinedDeclSurpressedRaw("4,TESTVAL", false);
        assertFalse(decMan.isCombinedDeclSurpressed("TESTVAL", 4));
    }

    @Test
    public void testAddDeclensionGenRule() {
        DeclensionGenRule rule = new DeclensionGenRule();
        rule.setCombinationId("COMBID");
        rule.setName("TESTNAME");
        rule.setRegex("TESTREGEX");
        rule.setTypeId(99);
        rule.setIndex(1);
        decMan.addDeclensionGenRule(rule);
        List<DeclensionGenRule> rules = decMan.getDeclensionRulesForType(99);
        assertTrue(rules.contains(rule));
    }

    @Test
    public void testWipeDeclensionGenRules() {
        DeclensionGenRule rule = new DeclensionGenRule();
        rule.setCombinationId("COMBID");
        rule.setName("TESTNAME");
        rule.setRegex("TESTREGEX");
        rule.setTypeId(98);
        rule.setIndex(1);
        decMan.addDeclensionGenRule(rule);
        decMan.wipeDeclensionGenRules(98);
        assertTrue(decMan.getDeclensionRulesForType(98).isEmpty());
    }

    @Test
    public void testDeleteDeclensionGenRule() {
        DeclensionGenRule rule = new DeclensionGenRule();
        rule.setCombinationId("COMBID");
        rule.setName("TESTNAME");
        rule.setRegex("TESTREGEX");
        rule.setTypeId(97);
        rule.setIndex(1);
        decMan.addDeclensionGenRule(rule);
        decMan.deleteDeclensionGenRule(rule);
        assertFalse(decMan.getDeclensionRulesForType(97).contains(rule));
    }

    @Test
    public void testGetDeclensionRulesForType() {
        DeclensionGenRule rule1 = new DeclensionGenRule();
        rule1.setCombinationId("COMBID");
        rule1.setName("TESTNAME");
        rule1.setRegex("TESTREGEX");
        rule1.setTypeId(96);
        rule1.setIndex(1);
        decMan.addDeclensionGenRule(rule1);
        DeclensionGenRule rule2 = new DeclensionGenRule();
        rule2.setCombinationId("COMBID");
        rule2.setName("TESTNAME");
        rule2.setRegex("TESTREGEX");
        rule2.setTypeId(96);
        rule2.setIndex(1);
        decMan.addDeclensionGenRule(rule2);
        
        List<DeclensionGenRule> rules = decMan.getDeclensionRulesForType(96);

        assertTrue(rules.contains(rule1) && rules.contains(rule2));
    }

    // TODO: all tests blelow this point
    
    @Test
    public void testGetAllDepGenerationRules() {
    }
    
    @Test
    public void testGetDeclensionRules() {
    }

    @Test
    public void testDeclineWord() throws Exception {
    }

    @Test
    public void testGetDeclensionMap() {
    }

    @Test
    public void testAddDeclensionToWord() {
    }

    @Test
    public void testDeleteDeclensionFromWord() {
    }

    @Test
    public void testUpdateDeclensionWord() {
    }

    @Test
    public void testDeprecateAllDeclensions() {
    }

    @Test
    public void testGetDeclension() {
    }

    @Test
    public void testGetMandDims() {
    }

    @Test
    public void testDeclensionRequirementsMet() {
    }

    @Test
    public void testClearAllDeclensionsWord() {
    }

    @Test
    public void testGetAllCombinedIds() {
    }

    @Test
    public void testGetDeclensionListWord() {
    }

    @Test
    public void testGetDimensionalDeclensionListTemplate() {
    }

    @Test
    public void testGetFullDeclensionListTemplate() {
    }

    @Test
    public void testAddDeclensionToTemplate_3args() {
    }

    @Test
    public void testAddDeclensionToTemplate_Integer_String() {
    }

    @Test
    public void testDeleteDeclensionFromTemplate() {
    }

    @Test
    public void testUpdateDeclensionTemplate() {
    }

    @Test
    public void testGetDeclensionTemplate() {
    }

    @Test
    public void testClearAllDeclensionsTemplate() {
    }

    @Test
    public void testSetBufferId() {
    }

    @Test
    public void testSetBufferDecText() {
    }

    @Test
    public void testGetBufferDecText() {
    }

    @Test
    public void testSetBufferDecNotes() {
    }

    @Test
    public void testGetBufferDecNotes() {
    }

    @Test
    public void testSetBufferDecTemp() {
    }

    @Test
    public void testSetBufferRelId() {
    }

    @Test
    public void testGetBufferRelId() {
    }

    @Test
    public void testIsBufferDecTemp() {
    }

    @Test
    public void testInsertBuffer() {
    }

    @Test
    public void testGetBuffer() {
    }

    @Test
    public void testClearBuffer() {
    }

    @Test
    public void testGetDeclensionByCombinedId() {
    }

    @Test
    public void testGetCombNameFromCombId() {
    }

    @Test
    public void testDeleteDeclension() {
    }

    @Test
    public void testIsBufferDecMandatory() {
    }

    @Test
    public void testSetBufferDecMandatory() {
    }

    @Test
    public void testGetWordDeclensions() {
    }

    @Test
    public void testRemoveDeclensionValues() {
    }

    @Test
    public void testWriteXML() {
    }

    @Test
    public void testSetAllDeclensionRulesToAllClasses() {
    }
    
}
