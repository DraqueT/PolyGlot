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
package org.darisadesigns.polyglotlina.ToolsHelpers;

import TestResources.DummyCore;
import java.io.IOException;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class ExportSpellingDictionaryTest {

    private final static String TEST_FILE = "testExportWordDict.pgd";
    private final ExportSpellingDictionary export;
    private final DictCore core;

    public ExportSpellingDictionaryTest() {
        core = DummyCore.newCore();
        export = new ExportSpellingDictionary(core);

        try {
            core.readFile(PGTUtil.TESTRESOURCES + TEST_FILE);
        }
        catch (IOException | IllegalStateException e) {
            fail(e);
        }
    }

    @Test
    public void testExportForWordNoConjugations() {
        ConWord word = getByValue("noConjugations");
        String expectedValue = "noConjugations\n";

        export.populateFromWord(word);

        String result = export.getCurrentStringValue();

        assertEquals(expectedValue, result);
    }

    @Test
    public void testExportForWordDimensional() {
        ConWord word = getByValue("dimensionalConjugations");
        String expectedValue = "dimensionalConjugations\n"
                + "dimensionalConjugations-1a\n"
                + "dimensionalConjugations-1b\n"
                + "dimensionalConjugations-2a\n"
                + "dimensionalConjugations-2b\n";

        export.populateFromWord(word);

        String result = export.getCurrentStringValue();

        assertEquals(expectedValue, result);
    }

    @Test
    public void testExportForWordNonDimensional() {
        ConWord word = getByValue("nonDimensional");
        String expectedValue = "nonDimensional\n"
                + "nonDimensional-1\n"
                + "nonDimensional-2\n"
                + "nonDimensional-3\n";

        export.populateFromWord(word);

        String result = export.getCurrentStringValue();

        assertEquals(expectedValue, result);
    }

    @Test
    public void testExportForWordBoth() {
        ConWord word = getByValue("both");
        String expectedValue = "both\n"
                + "both-1a\n"
                + "both-1b\n"
                + "both-1d\n"
                + "both-2a\n"
                + "both-2b\n"
                + "both-2d\n";

        export.populateFromWord(word);

        String result = export.getCurrentStringValue();

        assertEquals(expectedValue, result);
    }
    
    @Test
    public void testExportForWordBothOverridenValues() {
        ConWord word = getByValue("bothOverride");
        String expectedValue = "1a\n"
                + "1b\n"
                + "1d\n"
                + "2a\n"
                + "2b\n"
                + "2d\n"
                + "bothOverride\n";

        export.populateFromWord(word);

        String result = export.getCurrentStringValue();

        assertEquals(expectedValue, result);
    }
    
    @Test
    public void testExportForWordDisabledWordform() {
        ConWord word = getByValue("bothDisabledWordform");
        String expectedValue = "bothDisabledWordform\n"
                + "bothDisabledWordform-2\n"
                + "bothDisabledWordform-22\n";

        export.populateFromWord(word);

        String result = export.getCurrentStringValue();

        assertEquals(expectedValue, result);
    }

    @Test
    public void testExportForWordHasDupes() {
        ConWord word = getByValue("wordHasDupes");
        String expectedValue = "dupe\n"
                + "wordHasDupes\n";

        export.populateFromWord(word);

        String result = export.getCurrentStringValue();

        assertEquals(expectedValue, result);
    }
    /**
     * Sloppy way to snag my test conwords
     *
     * @param value
     * @return
     */
    private ConWord getByValue(String value) {
        ConWord[] words = core.getWordCollection().getWordNodes();

        for (ConWord word : words) {
            if (value.equals(word.getValue())) {
                return word;
            }
        }

        fail("Word not found: " + value);
        return null;
    }
}
