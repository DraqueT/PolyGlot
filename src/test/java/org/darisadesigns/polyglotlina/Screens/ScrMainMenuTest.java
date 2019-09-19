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
package org.darisadesigns.polyglotlina.Screens;

// TODO: Most of these tests

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.junit.jupiter.api.Test;

/**
 *
 * @author DThompson
 */
public class ScrMainMenuTest {
    final ScrMainMenu mainMenu;
    
    public ScrMainMenuTest() {
        mainMenu = new ScrMainMenu("");
    }
    
    @Test
    public void testManyOpensAndClosesLexCountMaintained() throws SecurityException, 
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, 
            InvocationTargetException, ParserConfigurationException, TransformerException, IOException {
        final String testFileName = PGTUtil.TESTRESOURCES + "earien_TEST.pgd";
        Method setFileMethod = mainMenu.getClass().getDeclaredMethod("setFile", String.class);
        setFileMethod.setAccessible(true);
        
        setFileMethod.invoke(mainMenu, PGTUtil.TESTRESOURCES +  "earien_ORIGINAL.pgd");
        int correctLexiconCount = mainMenu.getCore().getWordCollection().getWordCount();
        mainMenu.getCore().writeFile(testFileName);
        
        for (int i = 0; i < 10 ; i++) {
            setFileMethod.invoke(mainMenu, testFileName);
            int curLexCount = mainMenu.getCore().getWordCollection().getWordCount();
            try {
                assert(curLexCount == correctLexiconCount);
            } catch (AssertionError e) {
                throw new AssertionError("Run: " + i + "->" + curLexCount + " != " + correctLexiconCount);
            }
            mainMenu.getCore().writeFile(testFileName);
            
        }
        
        File testFile = new File(testFileName);
        testFile.delete();
    }

    /**
     * Test of saveAllValues method, of class ScrMainMenu.
     */
    @Test
    public void testSaveAllValues() {
    }

    /**
     * Test of openLexicon method, of class ScrMainMenu.
     */
    @Test
    public void testOpenLexicon() {
    }

    /**
     * Test of dispose method, of class ScrMainMenu.
     */
    @Test
    public void testDispose() {
    }

    /**
     * Test of saveFile method, of class ScrMainMenu.
     */
    @Test
    public void testSaveFile() {
    }

    /**
     * Test of changeToLexicon method, of class ScrMainMenu.
     */
    @Test
    public void testChangeToLexicon() {
    }

    /**
     * Test of newFile method, of class ScrMainMenu.
     */
    @Test
    public void testNewFile() {
    }

    /**
     * Test of genTitle method, of class ScrMainMenu.
     */
    @Test
    public void testGenTitle() {
    }

    /**
     * Test of open method, of class ScrMainMenu.
     */
    @Test
    public void testOpen() {
    }

    /**
     * Test of exportFont method, of class ScrMainMenu.
     */
    @Test
    public void testExportFont() {
    }

    /**
     * Test of selectWordById method, of class ScrMainMenu.
     */
    @Test
    public void testSelectWordById() {
    }

    /**
     * Test of getCurrentWord method, of class ScrMainMenu.
     */
    @Test
    public void testGetCurrentWord() {
    }

    /**
     * Test of main method, of class ScrMainMenu.
     */
    @Test
    public void testMain() {
    }

    /**
     * Test of updateAllValues method, of class ScrMainMenu.
     */
    @Test
    public void testUpdateAllValues() {
    }

    /**
     * Test of thisOrChildrenFocused method, of class ScrMainMenu.
     */
    @Test
    public void testThisOrChildrenFocused() {
    }

    /**
     * Test of addBindingToComponent method, of class ScrMainMenu.
     */
    @Test
    public void testAddBindingToComponent() {
    }

    /**
     * Test of getWindow method, of class ScrMainMenu.
     */
    @Test
    public void testGetWindow() {
    }

    /**
     * Test of canClose method, of class ScrMainMenu.
     */
    @Test
    public void testCanClose() {
    }
    
}
