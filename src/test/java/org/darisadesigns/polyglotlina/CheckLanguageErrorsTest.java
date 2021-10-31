/*
 * Copyright (c) 2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina;

import TestResources.DummyCore;
import java.io.IOException;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Nodes.LexiconProblemNode;
import org.darisadesigns.polyglotlina.Nodes.LexiconProblemNode.ProblemType;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class CheckLanguageErrorsTest {
    
    private final DictCore badLexEntriesCore;
    private final DictCore badRegexEntriesCore;
    
    public CheckLanguageErrorsTest() {
        badLexEntriesCore = DummyCore.newCore();
        badRegexEntriesCore = DummyCore.newCore();
        
        try {
            badLexEntriesCore.readFile(PGTUtil.TESTRESOURCES + "test_lex_problems.pgd");
            badRegexEntriesCore.readFile(PGTUtil.TESTRESOURCES + "test_regex_problems.pgd");
        } catch (IOException | IllegalStateException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }

    /**
     * Test of checkCore method, of class CheckLanguageErrors.
     */
    @Test
    public void testCheckCore_LexIssues() {
        System.out.println("CheckLanguageErrorsTest.testCheckCore_LexIssues");
        int expectedProblems = 4;
        
        LexiconProblemNode[] problems = CheckLanguageErrors.checkCore(badLexEntriesCore, false);
        assertEquals(expectedProblems, problems.length);
        
        LexiconProblemNode curWord = problems[0];
        assertEquals(curWord.problemWord.getValue(), "bad-pattern");
        assertEquals(curWord.description, "Word does not match enforced pattern for type: noun.");
        
        curWord = problems[1];
        assertEquals(curWord.problemWord.getValue(), "bad-romanization-1-noun");
        assertTrue(curWord.description.contains("Word contains characters undefined in alphabet settings."));
        assertTrue(curWord.description.contains("Suspect characters:\"1\""));
        assertTrue(curWord.description.contains("Word cannot be romanized properly (missing regex pattern)."));
        
        curWord = problems[2];
        assertEquals(curWord.problemWord.getValue(), "missing-POS-and-alphabet");
        assertTrue(curWord.description.contains("Types set to mandatory."));
        assertTrue(curWord.description.contains("Word contains characters undefined in alphabet settings"));
        assertTrue(curWord.description.contains("Suspect characters:\"POS\""));
        assertTrue(curWord.description.contains("Word pronunciation cannot be generated properly (missing regex pattern)."));
        
        curWord = problems[3];
        assertEquals(curWord.problemWord.getValue(), "missing-local-noun");
        assertEquals(curWord.description, "Local Lang word set to mandatory.");
    }
    
    @Test
    public void testCheckCore_regexIssues() {
        System.out.println("CheckLanguageErrorsTest.testCheckCore_regexIssues");
        
        int expectedProblems = 7;
        
        LexiconProblemNode[] problems = CheckLanguageErrors.checkCore(badRegexEntriesCore, false);
        assertEquals(expectedProblems, problems.length);
        
        LexiconProblemNode problem = problems[0];
        assertEquals(ProblemType.PoS, problem.problemType);
        assertEquals("BadConjTransform", problem.problemWord.getValue());
        assertEquals("\nThe replacement text \"(\" within rule broken2 is illegal.\nThe regex transform \"(\" within rule broken2 of up forth is illegal.", problem.description);
        
        problem = problems[1];
        assertEquals(ProblemType.PoS, problem.problemType);
        assertEquals("BadConjTransform", problem.problemWord.getValue());
        assertEquals("\nThe regex transform \"(\" within rule broken-nonD of  is illegal.", problem.description);
        
        problem = problems[2];
        assertEquals(ProblemType.PoS, problem.problemType);
        assertEquals("badTypePattern", problem.problemWord.getValue());
        assertEquals("Illegal regex value: \"(\"", problem.description); 
        assertEquals("Part of Speech: badTypePattern", problem.shortDescription);
        
        problem = problems[3];
        assertEquals(ProblemType.Phonology, problem.problemType);
        assertEquals("(", problem.problemWord.getValue());
        assertEquals("Pronunciation regex: \"(\" is illegal.", problem.description); 
        assertEquals("Phonology Problem", problem.shortDescription);
        
        problem = problems[4];
        assertEquals(ProblemType.Phonology, problem.problemType);
        assertEquals("(", problem.problemWord.getValue());
        assertEquals("Romanization regex: \"(\" is illegal.", problem.description); 
        assertEquals("Phonology Problem", problem.shortDescription);
        
        problem = problems[5];
        assertEquals(ProblemType.Phonology, problem.problemType);
        assertEquals("PronuncRegex", problem.problemWord.getValue());
        assertEquals("Pronunciation text: \"(\" is illegal regex insertion.", problem.description); 
        assertEquals("Phonology Problem", problem.shortDescription);
        
        problem = problems[6];
        assertEquals(ProblemType.Phonology, problem.problemType);
        assertEquals("RomanRegex", problem.problemWord.getValue());
        assertEquals("Romanization value: \"RomanRegex\" is illegal regex insertion.", problem.description); 
        assertEquals("Phonology Problem", problem.shortDescription);
    }
}
