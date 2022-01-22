/*
 * Copyright (c) 2020-2022, Draque Thompson, draquemail@gmail.com
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.darisadesigns.polyglotlina.PGTUtil;
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
        System.out.println("PronunciationMgtTest.isRegexLookaheadBehind");
        
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
    
    @Test
    public void testGetIpaSoundsPerCharacter() {
        System.out.println("PronunciationMgtTest.isRegexLookaheadBehind");
        
        Map<String, String[]> expectedResult = new HashMap<>();
        expectedResult.put("0", new String[]{"i","0","s","0"});
        expectedResult.put("1", new String[]{"ɑ","^1$","h","^1$"});
        expectedResult.put("2", new String[]{"ɑ","^2$","w","^2$"});
        expectedResult.put("3", new String[]{"n","^3$","o","^3$","ʊ","^3$"});
        expectedResult.put("4", new String[]{"ə","^4$","ʀ","^4$"});
        expectedResult.put("5", new String[]{"l","5","o","5","ʊ","5"});
        expectedResult.put("6", new String[]{"k","^6$","u","^6$"});
        expectedResult.put("7", new String[]{"i","^7$","m","^7$"});
        expectedResult.put("8", new String[]{"ɛ","^8$","d","^8$"});
        expectedResult.put("9", new String[]{"j","9","æ","9"});
        expectedResult.put(";", new String[]{"i",";","s",";"});
        expectedResult.put("A", new String[]{"ə","A","ʀ","A"});
        expectedResult.put("B", new String[]{"ə","B","k","B","u","B","ʀ","B"});
        expectedResult.put("C", new String[]{"a","C","f","C","h","C","t","C"});
        expectedResult.put("D", new String[]{"ŋ","D","l","D","o","D","ʊ","D","θ","D"});
        expectedResult.put("E", new String[]{"ə","E","i","E","s","E","ʀ","E"});
        expectedResult.put("F", new String[]{"ə","F","i","F","m","F","ʀ","F"});
        expectedResult.put("G", new String[]{"ɑ","G","ɛ","G","d","G","w","G"});
        expectedResult.put("H", new String[]{"l","H","o","H","ʊ","H","θ","H"});
        expectedResult.put("I", new String[]{"k","I","n","I","o","I","u","I","ʊ","I"});
        expectedResult.put("J", new String[]{"i","J","m","J","n","J","o","J","t","J","ʊ","J"});
        expectedResult.put("K", new String[]{"i","K","m","K"});
        expectedResult.put("L", new String[]{"ɑ","L","h","L","l","L","o","L","ʊ","L"});
        expectedResult.put("N", new String[]{"ə","N","ɛ","N","d","N","ʀ","N"});
        expectedResult.put("O", new String[]{"k","O","n","O","u","O"});
        expectedResult.put("P", new String[]{"f","P","l","P","o","P","t","P","ʊ","P"});
        expectedResult.put("Q", new String[]{"i","^QW","j","^QW","m","^QW","æ","^QW","ɛ","Q","d","Q"});
        expectedResult.put("R", new String[]{"ŋ","R","k","R","u","R"});
        expectedResult.put("S", new String[]{"i","S","m","S","ʀ","S"});
        expectedResult.put("T", new String[]{"ɑ","T","h","T","w","T"});
        expectedResult.put("U", new String[]{"i","U","l","U","m","U","o","U","ʊ","U","θ","U"});
        expectedResult.put("V", new String[]{"ɑ","V","i","V","m","V","w","V"});
        expectedResult.put("W", new String[]{"i","^QW","j","^QW","m","^QW","æ","^QW","ŋ","W","ɑ","W","w","W"});
        expectedResult.put("X", new String[]{"i","X","l","X","m","X","n","X","o","X","t","X","ʊ","X"});
        expectedResult.put("Y", new String[]{"ɑ","Y","ə","Y","n","Y","o","Y","w","Y","ʀ","Y","ʊ","Y"});
        expectedResult.put("Z", new String[]{"k","Z","l","Z","o","Z","ʊ","Z"});
        expectedResult.put("a", new String[]{"ɑ","a","h","a"});
        expectedResult.put("b", new String[]{"ŋ","b"});
        expectedResult.put("c", new String[]{"ʃ","c"});
        expectedResult.put("d", new String[]{"n","d","o","d","ʊ","d"});
        expectedResult.put("e", new String[]{"n","e","o","e","ʊ","e"});
        expectedResult.put("f", new String[]{"ə","f","ʀ","f"});
        expectedResult.put("g", new String[]{"l","g","o","g","ʊ","g"});
        expectedResult.put("h", new String[]{"k","h","u","h"});
        expectedResult.put("i", new String[]{"ɛ","i","d","i"});
        expectedResult.put("j", new String[]{"i","j","m","j"});
        expectedResult.put("k", new String[]{"ɛ","k","d","k"});
        expectedResult.put("l", new String[]{"j","l","æ","l"});
        expectedResult.put("o", new String[]{"j","o","æ","o"});
        expectedResult.put("p", new String[]{"i","p","s","p"});
        expectedResult.put("q", new String[]{"ɑ","q","h","q"});
        expectedResult.put("r", new String[]{"ə","r","ʀ","r"});
        expectedResult.put("s", new String[]{"ɑ","s","w","s"});
        expectedResult.put("t", new String[]{"l","t","o","t","ʊ","t"});
        expectedResult.put("u", new String[]{"i","u","m","u"});
        expectedResult.put("v", new String[]{"f","v"});
        expectedResult.put("w", new String[]{"ɑ","w"});
        expectedResult.put("x", new String[]{"θ","x"});
        expectedResult.put("y", new String[]{"k","y","u","y"});
        expectedResult.put("z", new String[]{"t","z"});
        
        DictCore core = DummyCore.newCore();
        
        try {
            core.readFile(PGTUtil.TESTRESOURCES + "Lodenkur_TEST.pgd");
            
            Map<String, String[]> result = core.getPronunciationMgr().getIpaSoundsPerCharacter();
            
            for (String key : result.keySet()) {
                assert(Arrays.equals(expectedResult.get(key), result.get(key)));
            }
        } catch (IOException | IllegalStateException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testSyllableGeneration_goodWord() {
        String testWord = "šösinpükɛh";
        String expectedProc = "šö˙sin˙pü˙kɛh";
        
        System.out.println("PronunciationMgtTest.testSyllableGeneration_goodWord");
        
        try {
            DictCore core = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "testSyllableGen.pgd");
            
            String result = core.getPronunciationMgr().getPronunciation(testWord);
            assertEquals(expectedProc, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testSyllableGeneration_badWord() {
        String testWord = "šösinpükɛh!";
        String expectedProc = "";
        
        System.out.println("PronunciationMgtTest.testSyllableGeneration_goodWord");
        
        try {
            DictCore core = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "testSyllableGen.pgd");
            
            String result = core.getPronunciationMgr().getPronunciation(testWord);
            assertEquals(expectedProc, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testSyllableGeneration_goodWord_regexDisabled() {
        String testWord = "šösinpükɛh";
        String expectedProc = "šö˙sin˙pü˙kɛh";
        
        System.out.println("PronunciationMgtTest.testSyllableGeneration_goodWord");
        
        try {
            DictCore core = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "testSyllableGen.pgd");
            core.getPropertiesManager().setDisableProcRegex(true);
            
            String result = core.getPronunciationMgr().getPronunciation(testWord);
            assertEquals(expectedProc, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testSyllableGeneration_badWord_regexDisabled() {
        String testWord = "šösinpükɛh!";
        String expectedProc = "";
        
        System.out.println("PronunciationMgtTest.testSyllableGeneration_goodWord");
        
        try {
            DictCore core = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "testSyllableGen.pgd");
            core.getPropertiesManager().setDisableProcRegex(true);
            
            String result = core.getPronunciationMgr().getPronunciation(testWord);
            assertEquals(expectedProc, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testSyllableGeneration_systemDisabled() {
        String testWord = "šösinpükɛh";
        String expectedProc = "šösinpükɛh";
        
        System.out.println("PronunciationMgtTest.testSyllableGeneration_goodWord");
        
        try {
            DictCore core = DummyCore.newCore();
            core.readFile(PGTUtil.TESTRESOURCES + "testSyllableGen.pgd");
            core.getPronunciationMgr().setSyllableCompositionEnabled(false);
            
            String result = core.getPronunciationMgr().getPronunciation(testWord);
            assertEquals(expectedProc, result);
        } catch (Exception e) {
            fail(e);
        }
    }
}
