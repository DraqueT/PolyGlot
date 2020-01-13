/*
 * Copyright (c) 2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina;

import TestResources.DummyCore;
import org.darisadesigns.polyglotlina.CustomControls.GrammarChapNode;
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
        assertTrue(core.isLanguageEmpty());
    }
    
    @Test
    public void testIsLanguageEmptyNoLexicon() {
        System.out.println("DictCoreTest.testIsLanguageEmptyNoLexicon");
        
        try {
            core.getWordCollection().addWord(new ConWord());
            assertFalse(core.isLanguageEmpty());
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testIsLanguageEmptyNoGrammar() {
        System.out.println("DictCoreTest.testIsLanguageEmptyNoGrammar");
        
        try {
            core.getGrammarManager().addChapter(new GrammarChapNode(null));
            assertFalse(core.isLanguageEmpty());
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testIsLanguageEmptyNoPronunciation() {
        System.out.println("DictCoreTest.testIsLanguageEmptyNoPronunciation");
        
        try {
            core.getPronunciationMgr().addPronunciation(new PronunciationNode());
            assertFalse(core.isLanguageEmpty());
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testIsLanguageEmptyNoPOS() {
        System.out.println("DictCoreTest.testIsLanguageEmptyNoPOS");
        try {
            core.getTypes().addNode(new TypeNode());
            assertFalse(core.isLanguageEmpty());
        } catch (Exception e) {
            fail(e);
        }
    }
}
