/*
 * Copyright (c) 2019, draque
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
import java.io.BufferedInputStream;
import java.io.IOException;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class ConWordCollectionTest {
    
    @Test
    public void loadSwadeshTestRealFile() {
        System.out.println("ConWordCollectionTest.loadSwadeshTestRealFile");
        
        String searchValue = "#002: YOU";
        String expectedValue = "#002: YOU (2.SG! 1952 THOU & YE)";
        int expectedSize = 100;
        int expectedFoundWords = 1;
        
        DictCore core = DummyCore.newCore();
        ConWordCollection words = core.getWordCollection();
        BufferedInputStream bs = new BufferedInputStream(
                ConWordCollection.class.getResourceAsStream(PGTUtil.SWADESH_LOCATION + PGTUtil.SWADESH_LISTS[0]));
        
        try {
            words.loadSwadesh(bs, false);
            ConWord filterWord = new ConWord();
            filterWord.setValue(searchValue);
            ConWord[] foundWords = words.filteredList(filterWord);

            int resultLexSize = words.getWordCount();
            int resultFoundSize = foundWords.length;

            assertEquals(resultLexSize, expectedSize);
            assertEquals(resultFoundSize, expectedFoundWords);

            String resultWordVal = foundWords[0].getValue();
            assertEquals(resultWordVal, expectedValue);
        } catch (Exception e) {
            IOHandler.writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void loadSwadeshTestMissingFile() {
        System.out.println("ConWordCollectionTest.loadSwadeshTestMissingFile");
        
        DictCore core = DummyCore.newCore();
        ConWordCollection words = core.getWordCollection();
        String expectedMessage = "Stream closed";
        
        
        Throwable t = assertThrows(IOException.class, () -> {
            BufferedInputStream bs = new BufferedInputStream(
                ConWordCollection.class.getResourceAsStream(PGTUtil.SWADESH_LOCATION + "FAKE_FILE"));
            words.loadSwadesh(bs, false);
        });
        
        String resultMessage = t.getLocalizedMessage();
        
        assertEquals(resultMessage, expectedMessage);
    }
}
