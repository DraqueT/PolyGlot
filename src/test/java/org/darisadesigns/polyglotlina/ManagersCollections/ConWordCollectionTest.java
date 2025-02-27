/*
 * Copyright (c) 2019-2023, Draque Thompson
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
import javax.xml.parsers.ParserConfigurationException;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.PTest;
import org.darisadesigns.polyglotlina.RegexTools.ReplaceOptions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class ConWordCollectionTest extends PTest {
    
    private final DictCore badLexEntriesCore;
    
    public ConWordCollectionTest() {
        badLexEntriesCore = DummyCore.newCore();
        
        try {
            badLexEntriesCore.readFile(PGTUtil.TESTRESOURCES + "test_lex_problems.pgd");
        } catch (IOException | IllegalStateException | ParserConfigurationException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }

    @Test
    public void testMissingConWord() {
        System.out.println("ConWordCollectionTest.testMissingConWord");
        
        DictCore core = DummyCore.newCore();
        ConWord test = new ConWord();
        
        ConWord results = core.getWordCollection().testWordLegality(test);
        
        assertEquals(results.getValue(), "Conlang word value cannot be blank.");
    }
    
    @Test
    public void testDuplicatedLocalWithCommas() {
        System.out.println("ConWordCollectionTest.testDuplicatedLocalWithCommas");
        
        DictCore core = DummyCore.newCore();
        ConWordCollection collection = core.getWordCollection();
        ConWord testOne = new ConWord();
        ConWord testTwo = new ConWord();
        
        testOne.setValue("ZOT");
        testOne.setLocalWord("bloober, doober");
        
        testTwo.setValue("SLPAT");
        testTwo.setLocalWord("zorba, bloober");
        
        core.getPropertiesManager().setLocalUniqueness(true);
        
        try {
            collection.addWord(testOne);
            collection.addWord(testTwo);
            
            ConWord result = collection.testWordLegality(testOne);
            
            assertNotEquals(result.getLocalWord(), "");
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    @Test
    public void testMissingLocalWordWithRequirement() {
        System.out.println("ConWordCollectionTest.testMissingLocalWordWithRequirement");
        String expectedResult = "Local Lang word set to mandatory.";
        
        DictCore core = DummyCore.newCore();
        ConWord test = new ConWord();
        
        core.getPropertiesManager().setLocalMandatory(true);
        test.setValue("TEST");
        ConWord resultWord = core.getWordCollection().testWordLegality(test);
        String result = resultWord.getLocalWord();
        
        assertEquals(expectedResult, result);
    }
    
    @Test
    public void testMissingLocalWordNoRequirement() {
        System.out.println("ConWordCollectionTest.testMissingLocalWordNoRequirement");
        
        DictCore core = DummyCore.newCore();
        ConWord test = new ConWord();
        
        ConWord results = core.getWordCollection().testWordLegality(test);
        
        assertEquals(results.getLocalWord(), "");
    }
    
    @Test
    public void testMissingPOSWithoutRequirement() {
        System.out.println("ConWordCollectionTest.testMissingPOSWithoutRequirement");
        
        DictCore core = DummyCore.newCore();
        ConWord test = new ConWord();
        
        core.getPropertiesManager().setTypesMandatory(true);
        ConWord results = core.getWordCollection().testWordLegality(test);
        
        assertEquals(results.typeError, "Part of Speech set to mandatory.");
    }
    
    @Test
    public void testMissingPOSNoRequirement() {
        System.out.println("ConWordCollectionTest.testMissingPOSNoRequirement");
        
        DictCore core = DummyCore.newCore();
        ConWord test = new ConWord();
        
        ConWord results = core.getWordCollection().testWordLegality(test);
        
        assertEquals(results.getLocalWord(), "");
    }
    
    @Test
    public void testMissingCharsInAlphabet() {
        System.out.println("ConWordCollectionTest.testMissingCharsInAlphabet");
        
        DictCore core = DummyCore.newCore();
        String[] words = new String[]{"a", "b", "c", "d"};
        
        try {
            for (String word : words) {
                ConWord newWord = new ConWord();
                newWord.setValue(word);
                core.getWordCollection().addWord(newWord);
            }

            core.getPropertiesManager().setAlphaOrder("a,b,c");
        } catch (Exception e) {
            fail(e);
        }
        
        assertFalse(core.getPropertiesManager().isAlphabetComplete());
    }
    
    @Test
    public void testNotMissingCharsInAlphabetMulticharVals() {
        System.out.println("ConWordCollectionTest.testMissingCharsInAlphabetMulticharVals");
        
        DictCore core = DummyCore.newCore();
        String[] words = new String[]{"abi", "b", "c", "d"};
        
        try {
            for (String word : words) {
                ConWord newWord = new ConWord();
                newWord.setValue(word);
                core.getWordCollection().addWord(newWord);
            }

            core.getPropertiesManager().setAlphaOrder("a,b,c,d,bi");
        } catch (Exception e) {
            fail(e);
        }
        
        assertTrue(core.getPropertiesManager().isAlphabetComplete());
    }
    
    @Test
    public void testNoMissingCharsInAlphabet() {
        System.out.println("ConWordCollectionTest.testNoMissingCharsInAlphabet");
        
        DictCore core = DummyCore.newCore();
        String[] words = new String[]{"a", "b", "c", "d"};
        
        try {
            for (String word : words) {
                ConWord newWord = new ConWord();
                newWord.setValue(word);
                core.getWordCollection().addWord(newWord);
            }

            core.getPropertiesManager().setAlphaOrder("a,b,c,d");
        } catch (Exception e) {
            fail(e);
        }
        
        assertTrue(core.getPropertiesManager().isAlphabetComplete());
    }
    
    @Test
    public void testEvolveLanguageBasicReplaceAll() {
        System.out.println("ConWordCollectionTest.testEvolveLanguageBasicReplaceAll");
        
        String[] words =        new String[]{"aaa", "aab", "abb", "bab", "bba", "bbb"};
        String[] expectedVals = new String[]{"bbb", "bbz", "bzb", "zbb", "zzb", "zzz"};
        
        this.evolveLanguageTest(words, expectedVals, "a", "z", ReplaceOptions.All);
    }
 
    @Test
    public void testEvolveLanguageBasicReplaceFirstMiddle() {
        System.out.println("ConWordCollectionTest.testEvolveLanguageBasicReplaceFirstMiddle");
        
        String[] words =        new String[]{"aaa", "aab", "abb", "bab", "bba", "bbb"};
        String[] expectedVals = new String[]{"zza", "zab", "zbb", "bzb", "bbz", "bbb"};
        
        this.evolveLanguageTest(words, expectedVals, "a", "z", ReplaceOptions.FirstAndMiddleInstances);
    }
    
    @Test
    public void testEvolveLanguageBasicReplaceFirst() {
        System.out.println("ConWordCollectionTest.testEvolveLanguageBasicReplaceFirst");
        
        String[] words =        new String[]{"aaa", "aab", "abb", "bab", "bba", "bbb"};
        String[] expectedVals = new String[]{"zaa", "zab", "zbb", "bzb", "bbz", "bbb"};
        
        this.evolveLanguageTest(words, expectedVals, "a", "z", ReplaceOptions.FirstInstanceOnly);
    }
    
    @Test
    public void testEvolveLanguageBasicReplaceLast() {
        System.out.println("ConWordCollectionTest.testEvolveLanguageBasicReplaceLast");
        
        String[] words =        new String[]{"aaa", "aab", "abb", "bab", "bba", "bbb"};
        String[] expectedVals = new String[]{"aaz", "azb", "zbb", "bzb", "bbz", "bbb"};
        
        this.evolveLanguageTest(words, expectedVals, "a", "z", ReplaceOptions.LastInsanceOnly);
    }
    
    @Test
    public void testEvolveLanguageBasicReplaceMiddleAndLast() {
        System.out.println("ConWordCollectionTest.testEvolveLanguageBasicReplaceMiddleAndLast");
        
        String[] words =        new String[]{"aaa", "aab", "abb", "bab", "bba", "bbb"};
        String[] expectedVals = new String[]{"azz", "azb", "zbb", "bzb", "bbz", "bbb"};
        
        this.evolveLanguageTest(words, expectedVals, "a", "z", ReplaceOptions.MiddleAndLastInsances);
    }
    
    @Test
    public void testEvolveLanguageBasicReplaceMiddle() {
        System.out.println("ConWordCollectionTest.testEvolveLanguageBasicReplaceMiddle");
        
        String[] words =        new String[]{"aaa", "aab", "abb", "bab", "bba", "bbb"};
        String[] expectedVals = new String[]{"aza", "aab", "abb", "bab", "bba", "bbb"};
        
        this.evolveLanguageTest(words, expectedVals, "a", "z", ReplaceOptions.MiddleInstancesOnly);
    }
    
    @Test
    public void testEvolveLanguageComplexAll() {
        System.out.println("ConWordCollectionTest.testEvolveLanguageComplexAll");
        
        String[] words =        new String[]{"aaa", "aab", "abb", "bab", "bba", "bbb"};
        String[] expectedVals = new String[]{"aaa", "z", "zb", "bz", "bba", "bbb"};
        
        this.evolveLanguageTest(words, expectedVals, "a+b", "z", ReplaceOptions.All);
    }
    
    private void evolveLanguageTest(String[] words, 
            String[] expectedVals,
            String regex,
            String replace,
            ReplaceOptions transformOption) {
        DictCore core = DummyCore.newCore();
        Arrays.sort(expectedVals); // gonna be returned in alphabetic order...
        
        try {
            for (String word : words) {
                ConWord newWord = new ConWord();
                newWord.setValue(word);
                core.getWordCollection().addWord(newWord);
            }

        } catch (Exception e) {
            fail(e);
        }
        
        try {
            core.getWordCollection().evolveLexicon(new ConWord(),
                    100, 
                    transformOption, 
                    regex, 
                    replace);
        }
        catch (Exception e) {
            fail(e);
        }
        
        List<String> resultVals = new ArrayList<>();
        
        for (ConWord word : core.getWordCollection().getWordNodes()) {
            resultVals.add(word.getValue());
        }
        
        String[] finalResults = resultVals.toArray(new String[0]);
        
        assertTrue(Arrays.equals(expectedVals, finalResults));
    }
}
