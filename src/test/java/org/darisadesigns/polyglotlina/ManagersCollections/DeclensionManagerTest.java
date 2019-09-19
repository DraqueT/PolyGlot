/*
 * Copyright (c) 2018-2019, Draque Thompson
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.DeclensionManager;
import org.darisadesigns.polyglotlina.Nodes.DeclensionGenRule;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author DThompson
 */
public class DeclensionManagerTest {
    final DictCore core;
    final DeclensionManager decMan;
    
    public DeclensionManagerTest() throws Exception {
        core = new DictCore();
        core.readFile(PGTUtil.TESTRESOURCES + "Lodenkur_TEST.pgd");
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
    
    /**
     * Test of deleteRulesFromDeclensionTemplates method, of class DeclensionManager.
     * @throws java.io.IOException
     */
    @Test
    public void testDeleteRulesFromDeclensionTemplatesInitial() throws IOException {
        DictCore subCore = new DictCore();
        subCore.readFile(PGTUtil.TESTRESOURCES + "testBulkDelete.pgd");
        int expectedInitialSize = 8;
        
        DeclensionManager decManSub = subCore.getDeclensionManager();
        assertEquals(decManSub.getDeclensionRulesForType(2).size(), expectedInitialSize);
    }
    
    @Test
    public void testDeleteRulesFromDeclensionTemplatesDelPast() throws IOException {
        DictCore subCore = new DictCore();
        subCore.readFile(PGTUtil.TESTRESOURCES + "testBulkDelete.pgd");
        int expectedFinalSize = 6;
        
        DeclensionManager decManSub = subCore.getDeclensionManager();
        DeclensionGenRule toDelete = decManSub.getDeclensionRulesForType(2).get(2);
        List<DeclensionGenRule> rulesToDelete = new ArrayList<>();
        rulesToDelete.add(toDelete);
        decManSub.deleteRulesFromDeclensionTemplates(2, 1, 2, rulesToDelete);
        assertEquals(decManSub.getDeclensionRulesForType(2).size(), expectedFinalSize);
    }
    
    @Test
    public void testDeleteRulesFromDeclensionTemplatesDelFemale() throws IOException {
        DictCore subCore = new DictCore();
        subCore.readFile(PGTUtil.TESTRESOURCES + "testBulkDelete.pgd");
        int expectedFinalSize = 6;
        
        DeclensionManager decManSub = subCore.getDeclensionManager();
        DeclensionGenRule toDelete = decManSub.getDeclensionRulesForType(2).get(2);
        List<DeclensionGenRule> rulesToDelete = new ArrayList<>();
        rulesToDelete.add(toDelete);
        decManSub.deleteRulesFromDeclensionTemplates(2, 0, 3, rulesToDelete);
        assertEquals(decManSub.getDeclensionRulesForType(2).size(), expectedFinalSize);
    }

    /**
     * Test of bulkDeleteRuleFromDeclensionTemplates method, of class DeclensionManager.
     * @throws java.io.IOException
     */
    @Test
    public void testBulkDeleteRuleFromDeclensionTemplates() throws IOException {
        DictCore subCore = new DictCore();
        subCore.readFile(PGTUtil.TESTRESOURCES + "testBulkDelete.pgd");
        int expectedFinalSize = 5;
        
        DeclensionManager decManSub = subCore.getDeclensionManager();
        DeclensionGenRule toDelete = decManSub.getDeclensionRulesForType(2).get(2);
        List<DeclensionGenRule> rulesToDelete = new ArrayList<>();
        rulesToDelete.add(toDelete);
        decManSub.bulkDeleteRuleFromDeclensionTemplates(2, rulesToDelete);
        assertEquals(decManSub.getDeclensionRulesForType(2).size(), expectedFinalSize);
    }
    
    @Test
    public void testBulkDeleteRuleFromDeclensionTemplatesMultiSelect() throws IOException {
        DictCore subCore = new DictCore();
        subCore.readFile(PGTUtil.TESTRESOURCES + "testBulkDelete.pgd");
        List<DeclensionGenRule> rulesToDelete = new ArrayList<>();
        int expectedFinalSize = 4;
        
        DeclensionManager decManSub = subCore.getDeclensionManager();
        rulesToDelete.add(decManSub.getDeclensionRulesForType(2).get(2));
        rulesToDelete.add(decManSub.getDeclensionRulesForType(2).get(1));
        decManSub.bulkDeleteRuleFromDeclensionTemplates(2, rulesToDelete);
        assertEquals(decManSub.getDeclensionRulesForType(2).size(), expectedFinalSize);
    }

    // TODO: all tests blelow this point
    
//    @Test
//    public void testGetAllDepGenerationRules() {
//    }
//    
//    @Test
//    public void testGetDeclensionRules() {
//    }
//
//    @Test
//    public void testDeclineWord() throws Exception {
//    }
//
//    @Test
//    public void testGetDeclensionMap() {
//    }
//
//    @Test
//    public void testAddDeclensionToWord() {
//    }
//
//    @Test
//    public void testDeleteDeclensionFromWord() {
//    }
//
//    @Test
//    public void testUpdateDeclensionWord() {
//    }
//
//    @Test
//    public void testDeprecateAllDeclensions() {
//    }
//
//    @Test
//    public void testGetDeclension() {
//    }
//
//    @Test
//    public void testGetMandDims() {
//    }
//
//    @Test
//    public void testDeclensionRequirementsMet() {
//    }
//
//    @Test
//    public void testClearAllDeclensionsWord() {
//    }
//
//    @Test
//    public void testGetAllCombinedIds() {
//    }
//
//    @Test
//    public void testGetDeclensionListWord() {
//    }
//
//    @Test
//    public void testGetDimensionalDeclensionListTemplate() {
//    }
//
//    @Test
//    public void testGetFullDeclensionListTemplate() {
//    }
//
//    @Test
//    public void testAddDeclensionToTemplate_3args() {
//    }
//
//    @Test
//    public void testAddDeclensionToTemplate_Integer_String() {
//    }
//
//    @Test
//    public void testDeleteDeclensionFromTemplate() {
//    }
//
//    @Test
//    public void testUpdateDeclensionTemplate() {
//    }
//
//    @Test
//    public void testGetDeclensionTemplate() {
//    }
//
//    @Test
//    public void testClearAllDeclensionsTemplate() {
//    }
//
//    @Test
//    public void testSetBufferId() {
//    }
//
//    @Test
//    public void testSetBufferDecText() {
//    }
//
//    @Test
//    public void testGetBufferDecText() {
//    }
//
//    @Test
//    public void testSetBufferDecNotes() {
//    }
//
//    @Test
//    public void testGetBufferDecNotes() {
//    }
//
//    @Test
//    public void testSetBufferDecTemp() {
//    }
//
//    @Test
//    public void testSetBufferRelId() {
//    }
//
//    @Test
//    public void testGetBufferRelId() {
//    }
//
//    @Test
//    public void testIsBufferDecTemp() {
//    }
//
//    @Test
//    public void testInsertBuffer() {
//    }
//
//    @Test
//    public void testGetBuffer() {
//    }
//
//    @Test
//    public void testClearBuffer() {
//    }
//
//    @Test
//    public void testGetDeclensionByCombinedId() {
//    }
//
//    @Test
//    public void testGetCombNameFromCombId() {
//    }
//
//    @Test
//    public void testDeleteDeclension() {
//    }
//
//    @Test
//    public void testIsBufferDecMandatory() {
//    }
//
//    @Test
//    public void testSetBufferDecMandatory() {
//    }
//
//    @Test
//    public void testGetWordDeclensions() {
//    }
//
//    @Test
//    public void testRemoveDeclensionValues() {
//    }
//
//    @Test
//    public void testWriteXML() {
//    }
//
//    @Test
//    public void testSetAllDeclensionRulesToAllClasses() {
//    }
//
//    /**
//     * Test of getRuleBuffer method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetRuleBuffer() {
//    }
//
//    /**
//     * Test of insRuleBuffer method, of class DeclensionManager.
//     */
//    @Test
//    public void testInsRuleBuffer() {
//    }
//
//    /**
//     * Test of getDimensionalCombinedIds method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetDimensionalCombinedIds() {
//    }
//
//    /**
//     * Test of getSingletonCombinedIds method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetSingletonCombinedIds() {
//    }
//
//    /**
//     * Test of getAllSingletonIds method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetAllSingletonIds() {
//    }
//
//    /**
//     * Test of getDimensionTemplateIndex method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetDimensionTemplateIndex() {
//    }
//
//    /**
//     * Test of getDeclentionTemplateByIndex method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetDeclentionTemplateByIndex() {
//    }
//
//    /**
//     * Test of getDimensionalDeclentionTemplateByIndex method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetDimensionalDeclentionTemplateByIndex() {
//    }
//
//    /**
//     * Test of getDimensionalDeclensionListWord method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetDimensionalDeclensionListWord() {
//    }
//
//    /**
//     * Test of getSingletonDeclensionList method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetSingletonDeclensionList() {
//    }
//
//    /**
//     * Test of getFullDeclensionListWord method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetFullDeclensionListWord() {
//    }
//
//    /**
//     * Test of copyRulesToDeclensionTemplates method, of class DeclensionManager.
//     */
//    @Test
//    public void testCopyRulesToDeclensionTemplates() {
//    }
//
//    /**
//     * Test of getDeclensionLabel method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetDeclensionLabel() {
//    }
//
//    /**
//     * Test of getDeclensionValueLabel method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetDeclensionValueLabel() {
//    }
//
//    /**
//     * Test of getDeprecatedForms method, of class DeclensionManager.
//     */
//    @Test
//    public void testGetDeprecatedForms() {
//    }
//
//    /**
//     * Test of wordHasDeprecatedForms method, of class DeclensionManager.
//     */
//    @Test
//    public void testWordHasDeprecatedForms() {
//    }
    
}
