/*
 * Copyright (c) 2020-2022, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
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
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class EtymologyManagerTest {
    
    public EtymologyManagerTest() {
    }

    /**
     * Test of checkAllForIllegalLoops method, of class EtymologyManager.
     */
    @Test
    public void testCheckAllForIllegalLoops_None() {
        System.out.println("EtymologyManagerTest.testCheckAllForIllegalLoops_None");
        
        try {
            DictCore core  = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "Lodenkur_TEST.pgd");
            ConWord[] result = core.getEtymologyManager().checkAllForIllegalLoops();
            
            assertEquals(0, result.length);
        } catch (IOException | ParserConfigurationException e) {
            fail(e);
        }
        
    }
    
    @Test
    public void testFailsOnLoopInsertion() {
        System.out.println("EtymologyManagerTest.testFailsOnLoopInsertion");
        
        String expectedMessage = "Parent/Child relation creates illegal loop. A word may never have itself in its own etymological lineage.";
        
        Throwable t = assertThrows(EtymologyManager.IllegalLoopException.class, () -> {
            DictCore core = DummyCore.newCore();
            ConWordCollection words = core.getWordCollection();
            EtymologyManager etMan = core.getEtymologyManager();

            ConWord maker = new ConWord();
            maker.setValue("BLAH1");
            int word1 = words.addWord(maker);
            maker = new ConWord();
            maker.setValue("BLAH2");
            int word2 = words.addWord(maker);
            maker = new ConWord();
            maker.setValue("BLAH3");
            int word3 = words.addWord(maker);
            
            etMan.addRelation(word1, word2);
            etMan.addRelation(word2, word3);
            etMan.addRelation(word3, word1);
        });
        
        String resultMessage = t.getLocalizedMessage();
        
        assertEquals(expectedMessage, resultMessage);
    }
    
    @Test
    public void testCheckAllForIllegalLoops_Present() {
        System.out.println("EtymologyManagerTest.testCheckAllForIllegalLoops_Present");
        int expectedCount = 3;
        
        try {
            DictCore core = DummyCore.newCore();
            ConWordCollection words = core.getWordCollection();
            EtymologyManager etMan = core.getEtymologyManager();

            ConWord maker = new ConWord();
            maker.setValue("BLAH1");
            int word1 = words.addWord(maker);
            maker = new ConWord();
            maker.setValue("BLAH2");
            int word2 = words.addWord(maker);
            maker = new ConWord();
            maker.setValue("BLAH3");
            int word3 = words.addWord(maker);
            maker = new ConWord();
            maker.setValue("BLAH4");
            int word4 = words.addWord(maker);
            
            etMan.addRelation(word1, word2);
            etMan.addRelation(word2, word3);
            etMan.addRelation(word3, word1, true);
            
            ConWord[] results = etMan.checkAllForIllegalLoops();
            assertEquals(expectedCount, results.length);
            
            for (ConWord curWord : results) {
                assertNotEquals(word4, curWord.getId());
            }
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testEtymology_delete_parent_of_single_child() {
        System.out.println("testEtymology_delete_parent_of_single_child");
        String testFileLocation = PGTUtil.TESTRESOURCES + "testoid.pgd";
        
        try {
            DictCore core = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "etymology_parents_deletion.pgd");
            core.getWordCollection().deleteNodeById(2);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        } finally {
            File testFile = new File(testFileLocation);
            if (testFile.exists()) {
                testFile.delete();
            }
        }
    }
    
    @Test
    public void testEtymology_delete_single_child_of_parent() {
        System.out.println("testEtymology_delete_single_child_of_parent");
        String testFileLocation = PGTUtil.TESTRESOURCES + "testoid.pgd";
        
        try {
            DictCore core = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "etymology_parents_deletion.pgd");
            core.getWordCollection().deleteNodeById(3);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        } finally {
            File testFile = new File(testFileLocation);
            if (testFile.exists()) {
                testFile.delete();
            }
        }
    }
    
    @Test
    public void testEtymology_delete_both_children_of_external_parent() {
        System.out.println("testEtymology_delete_both_children_of_external_parent");
        String testFileLocation = PGTUtil.TESTRESOURCES + "testoid.pgd";
        
        try {
            DictCore core = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "etymology_parents_deletion.pgd");
            core.getWordCollection().deleteNodeById(3);
            core.getWordCollection().deleteNodeById(4);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        } finally {
            File testFile = new File(testFileLocation);
            if (testFile.exists()) {
                testFile.delete();
            }
        }
    }
    
    @Test
    public void testEtymology_delete_single_child_of_parent_with_other_children() {
        System.out.println("testEtymology_delete_single_child_of_parent_with_other_children");
        String testFileLocation = PGTUtil.TESTRESOURCES + "testoid.pgd";
        
        try {
            DictCore core = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "etymology_parents_deletion.pgd");
            core.getWordCollection().deleteNodeById(6);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        } finally {
            File testFile = new File(testFileLocation);
            if (testFile.exists()) {
                testFile.delete();
            }
        }
    }
    
    @Test
    public void testEtymology_delete_parent_of_multiple_children() {
        System.out.println("testEtymology_delete_parent_of_multiple_children");
        String testFileLocation = PGTUtil.TESTRESOURCES + "testoid.pgd";
        
        try {
            DictCore core = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "etymology_parents_deletion.pgd");
            core.getWordCollection().deleteNodeById(5);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        } finally {
            File testFile = new File(testFileLocation);
            if (testFile.exists()) {
                testFile.delete();
            }
        }
    }
}
