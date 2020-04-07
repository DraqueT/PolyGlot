/*
 * Copyright (c) 2019-2020, Draque Thompson
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
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.LexiconProblemNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class ConWordCollectionTest {
    
    private final DictCore badLexEntriesCore;
    
    public ConWordCollectionTest() {
        badLexEntriesCore = DummyCore.newCore();
        
        try {
            badLexEntriesCore.readFile(PGTUtil.TESTRESOURCES + "test_lex_problems.pgd");
        } catch (IOException | IllegalStateException e) {
            IOHandler.writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }

    /**
     * Test of checkLexicon method, of class ConWordCollection.
     */
    @Test
    public void testCheckLexicon() {
        System.out.println("ConWordCollectionTest.checkLexicon");
        
        ConWordCollection instance = badLexEntriesCore.getWordCollection();
        LexiconProblemNode[] problems = instance.checkLexicon(false);
        if (problems.length != 4) {
            fail("There should be 4 problems in this language file.");
        }
        
        LexiconProblemNode curWord = problems[0];
        assertEquals(curWord.problemWord.getValue(), "bad-pattern");
        assertEquals(curWord.description, "Word does not match enforced pattern for type: noun.");
        
        curWord = problems[1];
        assertEquals(curWord.problemWord.getValue(), "bad-romanization-1-noun");
        assertEquals(curWord.description, "Word contains characters undefined in alphabet settings.\n" +
            "Word cannot be romanized properly (missing regex pattern).");
        
        curWord = problems[2];
        assertEquals(curWord.problemWord.getValue(), "missing-POS-and-alphabet");
        assertEquals(curWord.description, "Types set to mandatory.\n" +
            "Word contains characters undefined in alphabet settings.\n" +
            "Word pronunciation cannot be generated properly (missing regex pattern).");
        
        curWord = problems[3];
        assertEquals(curWord.problemWord.getValue(), "missing-local-noun");
        assertEquals(curWord.description, "Local Lang word set to mandatory.");
        
        
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
    public void testMissingLocalWordWithRequirement() {
        System.out.println("ConWordCollectionTest.testMissingLocalWordWithRequirement");
        
        DictCore core = DummyCore.newCore();
        ConWord test = new ConWord();
        
        core.getPropertiesManager().setLocalMandatory(true);
        test.setValue("TEST");
        ConWord results = core.getWordCollection().testWordLegality(test);
        
        assertEquals(results.getLocalWord(), "Local Lang word set to mandatory.");
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
        
        assertEquals(results.typeError, "Types set to mandatory.");
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
}
