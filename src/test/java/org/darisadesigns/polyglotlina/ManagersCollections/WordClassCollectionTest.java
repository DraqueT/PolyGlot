/*
 * Copyright (c) 2020-2023, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import TestResources.DummyCore;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.PEntry;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class WordClassCollectionTest {
    
    public WordClassCollectionTest() {
    }

    /**
     * Test of getRandomPropertyCombinations method, of class WordClassCollection.
     */
    @Test
    public void testGetRandomPropertyCombinations() {
        DictCore core = DummyCore.newCore();
        
        try {
            core.readFile(PGTUtil.TESTRESOURCES + "WordClassTesto.pgd");

            ConWord excludeWord = new ConWord();
            excludeWord.setCore(core);
            WordClass wClass = core.getWordClassCollection().getAllWordClasses()[1];
            excludeWord.setClassValue(wClass.getId(), 1);

            List<List<PEntry<Integer, Integer>>> random1 = core.getWordClassCollection().getRandomPropertyCombinations(5, excludeWord);
            List<List<PEntry<Integer, Integer>>> random2 = core.getWordClassCollection().getRandomPropertyCombinations(5, excludeWord);

            assertNotEquals(random1, random2);
        } catch (IOException | IllegalStateException | ParserConfigurationException e) {
            fail(e);
        }
    }
}
