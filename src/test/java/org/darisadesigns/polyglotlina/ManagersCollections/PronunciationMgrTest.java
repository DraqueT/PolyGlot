/*
 * Copyright (c) 2020, Draque Thompson, draquemail@gmail.com
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
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class PronunciationMgrTest {
    
    public PronunciationMgrTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testNoRegexPronunciationGeneration() {
        System.out.println("PronunciationMgtTest.testNoRegexPronunciationGeneration");
        
        String expected = "ABcAcBA";
        String testVal = "abcacba";
        
        DictCore core = DummyCore.newCore();
        core.getPropertiesManager().setDisableProcRegex(true); // disable regex
        PronunciationMgr procMan = core.getPronunciationMgr();
        procMan.setRecurse(false); // disable recursion
        procMan.addPronunciation(new PronunciationNode("a.", "X"));
        procMan.addPronunciation(new PronunciationNode("a", "A"));
        procMan.addPronunciation(new PronunciationNode("b", "B"));
        procMan.addPronunciation(new PronunciationNode("c", "c"));
        
        try {
            String result = procMan.getPronunciation(testVal);

            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testRegexPronunciationGeneration() {
        System.out.println("PronunciationMgtTest.testRegexPronunciationGeneration");
        
        String expected = "XcXBA";
        String testVal = "abcacba";
        
        DictCore core = DummyCore.newCore();
        core.getPropertiesManager().setDisableProcRegex(false); // enable regex
        PronunciationMgr procMan = core.getPronunciationMgr();
        procMan.setRecurse(false); // disable recursion
        procMan.addPronunciation(new PronunciationNode("a.", "X"));
        procMan.addPronunciation(new PronunciationNode("a", "A"));
        procMan.addPronunciation(new PronunciationNode("b", "B"));
        procMan.addPronunciation(new PronunciationNode("c", "c"));
        
        try {
            String result = procMan.getPronunciation(testVal);

            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    // with regex patterns but regex disabled
    @Test
    public void testRegexPronunciationGenerationNoRegex() {
        System.out.println("PronunciationMgtTest.testRegexPronunciationGenerationNoRegex");
        
        String expected = "ABcAcBA";
        String testVal = "abcacba";
        
        DictCore core = DummyCore.newCore();
        core.getPropertiesManager().setDisableProcRegex(true); // disable regex
        PronunciationMgr procMan = core.getPronunciationMgr();
        procMan.setRecurse(false); // disable recursion
        procMan.addPronunciation(new PronunciationNode("a.", "X"));
        procMan.addPronunciation(new PronunciationNode("a", "A"));
        procMan.addPronunciation(new PronunciationNode("b", "B"));
        procMan.addPronunciation(new PronunciationNode("c", "c"));
        
        try {
            String result = procMan.getPronunciation(testVal);

            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testRecursivePronuciationGenerationLookaheadRegex() {
        System.out.println("PronunciationMgtTest.testRecursivePronuciationGenerationLookaheadRegex");
        
        String expected = "XBcAcBA";
        String testVal = "abcacba";
        
        DictCore core = DummyCore.newCore();
        core.getPropertiesManager().setDisableProcRegex(false); // enable regex
        PronunciationMgr procMan = core.getPronunciationMgr();
        procMan.setRecurse(true); // enable recursion
        procMan.addPronunciation(new PronunciationNode("a(?=b)", "X"));
        procMan.addPronunciation(new PronunciationNode("a", "A"));
        procMan.addPronunciation(new PronunciationNode("b", "B"));
        procMan.addPronunciation(new PronunciationNode("c", "c"));
        
        try {
            String result = procMan.getPronunciation(testVal);

            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    // with lookahead patterns, but no recursion
    @Test
    public void testRecursivePronuciationGenerationLookaheadRegexRecursionDisabled() {
        System.out.println("PronunciationMgtTest.testRecursivePronuciationGenerationLookaheadRegexRecursionDisabled");
        
        String expected = "XBcAcBA";
        String testVal = "abcacba";
        
        DictCore core = DummyCore.newCore();
        core.getPropertiesManager().setDisableProcRegex(false); // enable regex
        PronunciationMgr procMan = core.getPronunciationMgr();
        procMan.setRecurse(true); // disable recursion
        procMan.addPronunciation(new PronunciationNode("a(?=b)", "X"));
        procMan.addPronunciation(new PronunciationNode("a", "A"));
        procMan.addPronunciation(new PronunciationNode("b", "B"));
        procMan.addPronunciation(new PronunciationNode("c", "c"));
        
        try {
            String result = procMan.getPronunciation(testVal);

            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    // with lookahead patterns, but no regex
    @Test
    public void testNoRecursivePronuciationGenerationLookaheadRegex() {
        System.out.println("PronunciationMgtTest.testNoRecursivePronuciationGenerationLookaheadRegex");
        
        String expected = "ABcAcBA";
        String testVal = "abcacba";
        
        DictCore core = DummyCore.newCore();
        core.getPropertiesManager().setDisableProcRegex(true); // disable regex
        PronunciationMgr procMan = core.getPronunciationMgr();
        procMan.setRecurse(false); // disable recursion
        procMan.addPronunciation(new PronunciationNode("a(?=b)", "X"));
        procMan.addPronunciation(new PronunciationNode("a", "A"));
        procMan.addPronunciation(new PronunciationNode("b", "B"));
        procMan.addPronunciation(new PronunciationNode("c", "c"));
        
        try {
            String result = procMan.getPronunciation(testVal);

            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void isRegexLookaheadBehind() {
        assertTrue(PronunciationMgr.isRegexLookaheadBehind("This is a test (?=h) asdad"));
        assertTrue(PronunciationMgr.isRegexLookaheadBehind("This is a test (?!h) asdad"));
        assertTrue(PronunciationMgr.isRegexLookaheadBehind("This is a test (?<=h) asdad"));
        assertTrue(PronunciationMgr.isRegexLookaheadBehind("This is a test (?<!h) asdad"));
        
        assertFalse(PronunciationMgr.isRegexLookaheadBehind("This is a test (\\?=h) asdad"));
        assertFalse(PronunciationMgr.isRegexLookaheadBehind("This is a test (?\\=h) asdad"));
        assertFalse(PronunciationMgr.isRegexLookaheadBehind("This is a test (\\?!h) asdad"));
        assertFalse(PronunciationMgr.isRegexLookaheadBehind("This is a test (?<\\=h) asdad"));
        assertFalse(PronunciationMgr.isRegexLookaheadBehind("This is a test (?\\<=h) asdad"));
        assertFalse(PronunciationMgr.isRegexLookaheadBehind("This is a test (\\?<=h) asdad"));
    }
}