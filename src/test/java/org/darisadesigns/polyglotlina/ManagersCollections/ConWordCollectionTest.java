/*
 * Copyright (c) 2019, Draque Thompson
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

import TestResources.DummyCore;
import java.io.IOException;
import java.util.List;
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
        System.out.println("Test: checkLexicon");
        ConWordCollection instance = badLexEntriesCore.getWordCollection();
        List<LexiconProblemNode> problems = instance.checkLexicon(false);
        if (problems.size() != 4) {
            fail("There should be 4 problems in this language file.");
        }
        
        LexiconProblemNode curWord = problems.get(0);
        assertEquals(curWord.problemWord.getValue(), "bad-pattern");
        assertEquals(curWord.description, "Word does not match enforced pattern for type: noun.");
        
        curWord = problems.get(1);
        assertEquals(curWord.problemWord.getValue(), "bad-romanization-1-noun");
        assertEquals(curWord.description, "Word contains characters undefined in alphabet settings.\n" +
            "Word cannot be romanized properly (missing regex pattern).");
        
        curWord = problems.get(2);
        assertEquals(curWord.problemWord.getValue(), "missing-POS-and-alphabet");
        assertEquals(curWord.description, "Types set to mandatory.\n" +
            "Word contains characters undefined in alphabet settings.\n" +
            "Word pronunciation cannot be generated properly (missing regex pattern).");
        
        curWord = problems.get(3);
        assertEquals(curWord.problemWord.getValue(), "missing-local-noun");
        assertEquals(curWord.description, "Local Lang word set to mandatory.");
        
        
    }
    
    @Test
    public void testMissingConWord() {
        DictCore core = DummyCore.newCore();
        ConWord test = new ConWord();
        
        ConWord results = core.getWordCollection().testWordLegality(test);
        
        assertEquals(results.getValue(), "Conlang word value cannot be blank.");
    }
    
    @Test
    public void testMissingLocalWordWithRequirement() {
        DictCore core = DummyCore.newCore();
        ConWord test = new ConWord();
        
        core.getPropertiesManager().setLocalMandatory(true);
        test.setValue("TEST");
        ConWord results = core.getWordCollection().testWordLegality(test);
        
        assertEquals(results.getLocalWord(), "Local Lang word set to mandatory.");
    }
    
    @Test
    public void testMissingLocalWordNoRequirement() {
        DictCore core = DummyCore.newCore();
        ConWord test = new ConWord();
        
        ConWord results = core.getWordCollection().testWordLegality(test);
        
        assertEquals(results.getLocalWord(), "");
    }
    
    @Test
    public void testMissingPOSWithoutRequirement() {
        DictCore core = DummyCore.newCore();
        ConWord test = new ConWord();
        
        core.getPropertiesManager().setTypesMandatory(true);
        ConWord results = core.getWordCollection().testWordLegality(test);
        
        assertEquals(results.typeError, "Types set to mandatory.");
    }
    
    @Test
    public void testMissingPOSNoRequirement() {
        DictCore core = DummyCore.newCore();
        ConWord test = new ConWord();
        
        ConWord results = core.getWordCollection().testWordLegality(test);
        
        assertEquals(results.getLocalWord(), "");
    }
}
