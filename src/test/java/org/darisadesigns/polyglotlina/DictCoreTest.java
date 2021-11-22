/*
 * Copyright (c) 2020-2021, Draque Thompson, draquemail@gmail.com
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopGrammarChapNode;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class DictCoreTest {
    DictCore core;
    
    public DictCoreTest() {
        core = DummyCore.newCore();
    }

    @Test
    public void testIsLanguageEmptyYes() {
        System.out.println("DictCoreTest.testIsLanguageEmptyYes");
        assertTrue(core.isLanguageEmpty(), "DictCoreTest.testIsLanguageEmptyYes:F");
    }
    
    @Test
    public void testIsLanguageEmptyNoLexicon() {
        System.out.println("DictCoreTest.testIsLanguageEmptyNoLexicon");
        
        try {
            core.getWordCollection().addWord(new ConWord());
            assertFalse(core.isLanguageEmpty(), "DictCoreTest.testIsLanguageEmptyNoLexicon:F");
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testIsLanguageEmptyNoGrammar() {
        System.out.println("DictCoreTest.testIsLanguageEmptyNoGrammar");
        
        try {
            core.getGrammarManager().addChapter(new DesktopGrammarChapNode(null));
            assertFalse(core.isLanguageEmpty(), "DictCoreTest.testIsLanguageEmptyNoGrammar:F");
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testIsLanguageEmptyNoPronunciation() {
        System.out.println("DictCoreTest.testIsLanguageEmptyNoPronunciation");
        
        try {
            core.getPronunciationMgr().addPronunciation(new PronunciationNode());
            assertFalse(core.isLanguageEmpty(), "DictCoreTest.testIsLanguageEmptyNoPronunciation:F");
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testIsLanguageEmptyNoPOS() {
        System.out.println("DictCoreTest.testIsLanguageEmptyNoPOS");
        try {
            core.getTypes().addNode(new TypeNode());
            assertFalse(core.isLanguageEmpty(), "DictCoreTest.testIsLanguageEmptyNoPOS:F");
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testSaveLanguageIntegrityDeep() {
        // this tests the deep integrity of saved languages
        System.out.println("DictCoreTest.testSaveLanguageIntegrityDeep");
        
        try {
            DictCore origin = DummyCore.newCore();
            DictCore target = DummyCore.newCore();
            Path targetPath = Files.createTempFile("POLYGLOT", "pgt");
            
            origin.readFile(PGTUtil.TESTRESOURCES + "test_equality.pgd");
            origin.writeFile(targetPath.toString(), false);
            target.readFile(targetPath.toString());
            
            assertEquals(origin, target, "DictCoreTest.testIsLanguageEmptyNoPOS:F");
        } catch (IOException | IllegalStateException | ParserConfigurationException | TransformerException e) {
            //e.printStackTrace();
            //System.out.println(e.getMessage());
            fail(e);
        }
    }
}
