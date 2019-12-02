/*
 * Copyright (c) 2018-2019, Draque Thompson, draquemail@gmail.com
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

import TestResources.DummyCore;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 *
 * @author DThompson
 */
public class ScrMainMenuTest {
    private final ScrMainMenu mainMenu;
    private final boolean headless = GraphicsEnvironment.isHeadless();
    
    public ScrMainMenuTest() {
        if (!headless) {
            mainMenu = new ScrMainMenu(DummyCore.newCore());
        } else {
            mainMenu = null;
        }
    }
    
    @Test
    public void testManyOpensAndClosesLexCountMaintained() {
        if (headless) {
            return;
        }
        
        final String testFileName = PGTUtil.TESTRESOURCES + "earien_TEST.pgd";
        
        try {
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
        } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException 
                | SecurityException | InvocationTargetException | ParserConfigurationException 
                | TransformerException e) {
            IOHandler.writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        } finally {
            File testFile = new File(testFileName);
            testFile.delete();
        }
    }
}
