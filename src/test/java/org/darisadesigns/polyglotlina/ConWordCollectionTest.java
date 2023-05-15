/*
 * Copyright (c) 2019-2023, Draque Thompson draquemail@gmail.com
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
package org.darisadesigns.polyglotlina;

import TestResources.DummyCore;
import java.io.BufferedInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class ConWordCollectionTest {
    
    @Test
    public void loadSwadeshTestRealFile() {
        System.out.println("ConWordCollectionTest.loadSwadeshTestRealFile");
        
        String searchValue = "#002: YOU";
        String expectedValue = "#002: YOU (2.SG! 1952 THOU & YE)";
        int expectedSize = 100;
        int expectedFoundWords = 1;
        
        DictCore core = DummyCore.newCore();
        ConWordCollection words = core.getWordCollection();
        BufferedInputStream bs = new BufferedInputStream(
                ConWordCollection.class.getResourceAsStream(PGTUtil.SWADESH_LOCATION + PGTUtil.SWADESH_LISTS[0]));
        
        try {
            words.loadSwadesh(bs, false);
            ConWord filterWord = new ConWord();
            filterWord.setValue(searchValue);
            ConWord[] foundWords = words.filteredList(filterWord);

            int resultLexSize = words.getWordCount();
            int resultFoundSize = foundWords.length;

            assertEquals(resultLexSize, expectedSize);
            assertEquals(resultFoundSize, expectedFoundWords);

            String resultWordVal = foundWords[0].getValue();
            assertEquals(resultWordVal, expectedValue);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void loadSwadeshTestMissingFile() {
        System.out.println("ConWordCollectionTest.loadSwadeshTestMissingFile");
        
        DictCore core = DummyCore.newCore();
        ConWordCollection words = core.getWordCollection();
        String expectedMessage = "Stream closed";
        
        
        Throwable t = assertThrows(IOException.class, () -> {
            BufferedInputStream bs = new BufferedInputStream(
                ConWordCollection.class.getResourceAsStream(PGTUtil.SWADESH_LOCATION + "FAKE_FILE"));
            words.loadSwadesh(bs, false);
        });
        
        String resultMessage = t.getLocalizedMessage();
        
        assertEquals(expectedMessage, resultMessage);
    }
    
    @Test
    public void testIllegalFilter() {
        System.out.println("ConWordCollectionTest.loadSwadeshTestMissingFile");
        int expectedRulesBroken = 7;
        
        try {
            DictCore core = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "TestWordRules.pgd");

            ConWord[] words = core.getWordCollection().illegalFilter();
            
            assertEquals(expectedRulesBroken, words.length);
            assertEquals("LOCALCOPY1", words[0].getValue());
            assertEquals("LOCALCOPY2", words[1].getValue());
            assertEquals("NOLOCAL", words[2].getValue());
            assertEquals("NO_POS", words[3].getValue());
            assertEquals("Verb", words[4].getValue());
            assertEquals("copy", words[5].getValue());
            assertEquals("copy", words[6].getValue());
        } catch (IOException | IllegalStateException | ParserConfigurationException e) {
            fail(e);
        }
    }
    
    @Test
    public void getWordsConOrder() {
        System.out.println("ConWordCollectionTest.getWordsConOrder");
        
        DictCore core = DummyCore.newCore();
        ConWordCollection lex = core.getWordCollection();
        
        try {
            ConWord word = new ConWord();
            word.setCore(core);
            word.setValue("a");
            word.setLocalWord("a");
            lex.addNode(word);
            word = new ConWord();
            word.setCore(core);
            word.setValue("b");
            word.setLocalWord("b");
            lex.addNode(word);
            word = new ConWord();
            word.setCore(core);
            word.setValue("c");
            word.setLocalWord("c");
            lex.addNode(word);

            core.getPropertiesManager().setAlphaOrder("c,b,a");

            ConWord[] words = lex.getWordNodes();

            assertEquals("c", words[0].getValue());
            assertEquals("b", words[1].getValue());
            assertEquals("a", words[2].getValue());
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void getWordsLocalOrder() {
        System.out.println("ConWordCollectionTest.getWordsLocalOrder");
        
        DictCore core = DummyCore.newCore();
        ConWordCollection lex = core.getWordCollection();
        
        try {
            ConWord word = new ConWord();
            word.setCore(core);
            word.setValue("a");
            word.setLocalWord("a");
            lex.addNode(word);
            word = new ConWord();
            word.setCore(core);
            word.setValue("b");
            word.setLocalWord("b");
            lex.addNode(word);
            word = new ConWord();
            word.setCore(core);
            word.setValue("c");
            word.setLocalWord("c");
            lex.addNode(word);

            core.getPropertiesManager().setAlphaOrder("c,b,a");

            ConWord[] words = lex.getNodesLocalOrder();

            assertEquals("a", words[0].getValue());
            assertEquals("b", words[1].getValue());
            assertEquals("c", words[2].getValue());
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testWordValueExists() {
        System.out.println("ConWordCollectionTest.testWordValueExists");
        
        String conVal = "CONVAL";
        String localVal = "LOCALVAL";
        
        DictCore core = DummyCore.newCore();
        ConWord word = new ConWord();
        
        assertFalse(core.getWordCollection().testWordValueExists(conVal));
        
        word.setValue(conVal);
        word.setLocalWord(localVal);
        word.setCore(core);
        
        try {
            core.getWordCollection().addWord(word);
        } catch (Exception e) {
            fail(e);
        }
        
        assertTrue(core.getWordCollection().testWordValueExists(conVal));
    }
    
    public void testLocalValueExists() {
        System.out.println("ConWordCollectionTest.testLocalValueExists");
        
        String conVal = "CONVAL";
        String localVal = "LOCALVAL";
        
        DictCore core = DummyCore.newCore();
        ConWord word = new ConWord();
        
        assertFalse(core.getWordCollection().testLocalValueExists(localVal));
        
        word.setValue(conVal);
        word.setLocalWord(localVal);
        word.setCore(core);
        
        try {
            core.getWordCollection().addNode(word);
        } catch (Exception e) {
            fail(e);
        }
        
        assertTrue(core.getWordCollection().testLocalValueExists(localVal));
    }
    
    @Test
    public void testFilterConword_noRegex() {
        System.out.println("ConWordCollectionTest.testFilterConword_noRegex");
        
        int expectedResultCount = 22;
        
        DictCore dictCore = DummyCore.newCore();
        ConWord filter = new ConWord();
        filter.setWordTypeId(0);
        filter.setValue("w");
        
        try {
            dictCore.readFile(PGTUtil.TESTRESOURCES + "Lodenkur_TEST.pgd");
            
            var filteredList = dictCore.getWordCollection().filteredList(filter);
            
            assertEquals(expectedResultCount, filteredList.length, "Expected: " + expectedResultCount + " Saw: " + filteredList.length);
            
            for (var word : filteredList) {
                assertTrue(word.getValue().contains("w"));
            }
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testFilterLocalword_noRegex() {
        System.out.println("ConWordCollectionTest.testFilterConword_noRegex");
        
        int expectedResultCount = 65;
        
        DictCore dictCore = DummyCore.newCore();
        ConWord filter = new ConWord();
        filter.setWordTypeId(0);
        filter.setLocalWord("w");
        
        try {
            dictCore.readFile(PGTUtil.TESTRESOURCES + "Lodenkur_TEST.pgd");
            
            var filteredList = dictCore.getWordCollection().filteredList(filter);
            
            assertEquals(expectedResultCount, filteredList.length, "Expected: " + expectedResultCount + " Saw: " + filteredList.length);
            
            for (var word : filteredList) {
                assertTrue(word.getLocalWord().contains("w"));
            }
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testFilterLocalword_withRegex() {
        System.out.println("ConWordCollectionTest.testFilterLocalword_withRegex");
        
        int expectedResultCount = 2;
        
        DictCore dictCore = DummyCore.newCore();
        ConWord filter = new ConWord();
        filter.setWordTypeId(0);
        filter.setLocalWord("b.d");
        
        try {
            dictCore.readFile(PGTUtil.TESTRESOURCES + "Lodenkur_TEST.pgd");
            
            var filteredList = dictCore.getWordCollection().filteredList(filter);
            
            assertEquals(expectedResultCount, filteredList.length, "Expected: " + expectedResultCount + " Saw: " + filteredList.length);
            
            for (var word : filteredList) {
                assertTrue(word.getLocalWord().matches("b.d"));
            }
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testFilterPartOfSpeech() {
        System.out.println("ConWordCollectionTest.testFilterPartOfSpeech");
        
        int expectedResultCount = 17;
        
        DictCore dictCore = DummyCore.newCore();
        ConWord filter = new ConWord();
        filter.setWordTypeId(2);
        
        try {
            dictCore.readFile(PGTUtil.TESTRESOURCES + "Lodenkur_TEST.pgd");
            
            var filteredList = dictCore.getWordCollection().filteredList(filter);
            
            assertEquals(expectedResultCount, filteredList.length, "Expected: " + expectedResultCount + " Saw: " + filteredList.length);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testFilterProcSpeech() {
        System.out.println("ConWordCollectionTest.testFilterPartOfSpeech");
        
        int expectedResultCount = 1;
        
        DictCore dictCore = DummyCore.newCore();
        ConWord filter = new ConWord();
        filter.setPronunciation("ʃsiːʃ↘kuː");
        
        try {
            dictCore.readFile(PGTUtil.TESTRESOURCES + "Lodenkur_TEST.pgd");
            
            var filteredList = dictCore.getWordCollection().filteredList(filter);
            
            assertEquals(expectedResultCount, filteredList.length, "Expected: " + expectedResultCount + " Saw: " + filteredList.length);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testFilterDefinition() {
        System.out.println("ConWordCollectionTest.testFilterDefinition");
        
        int expectedResultCount = 1;
        
        DictCore dictCore = DummyCore.newCore();
        ConWord filter = new ConWord();
        filter.setDefinition("bed");
        
        try {
            dictCore.readFile(PGTUtil.TESTRESOURCES + "Lodenkur_TEST.pgd");
            
            var filteredList = dictCore.getWordCollection().filteredList(filter);
            
            assertEquals(expectedResultCount, filteredList.length, "Expected: " + expectedResultCount + " Saw: " + filteredList.length);
            assertTrue(filteredList[0].getDefinition().contains("bed"));
        } catch (Exception e) {
            fail(e);
        }
  }
        
    @Test
    public void testFilterEtymology() {
        System.out.println("ConWordCollectionTest.testFilterEtymology");
        
        int expectedResultCount = 1;
        
        DictCore dictCore = DummyCore.newCore();
        ConWord filter = new ConWord();
        
        try {
            dictCore.readFile(PGTUtil.TESTRESOURCES + "Lodenkur_TEST.pgd");
            filter.setFilterEtyParent(dictCore.getWordCollection().getNodeById(398));
            
            var filteredList = dictCore.getWordCollection().filteredList(filter);
            assertEquals(expectedResultCount, filteredList.length, "Expected: " + expectedResultCount + " Saw: " + filteredList.length);
            assertTrue(filteredList[0].getValue().equals("8d6"));
        } catch (Exception e) {
            fail(e);
        }
    }
}
