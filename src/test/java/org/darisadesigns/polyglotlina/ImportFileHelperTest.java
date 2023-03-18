/*
 * Copyright (c) 2021-2021, Draque Thompson, draquemail@gmail.com
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
import org.darisadesigns.polyglotlina.Desktop.ImportFileHelper;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class ImportFileHelperTest {
    
    private static final String ORIGIN_VAL = "word1";
    private static final String ORIGIN_DEF = "ORIGIN DEF";
    private static final String TEST_FILE = PGTUtil.TESTRESOURCES + "excel_import_check.csv";
    private DictCore core;
    
    public ImportFileHelperTest() {
        core = DummyCore.newCore();
        
        try {
            ConWord origin = new ConWord();
            origin.setCore(core);
            origin.setValue(ORIGIN_VAL);
            origin.setDefinition(ORIGIN_DEF);
            core.getWordCollection().addNode(origin);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testImportFileAll() {
        System.out.println("ImportFileHelperTest.testImportFileAll");
        
        int expectedWordCount = 5;
        int expectedWordmapSize = 4;
        int expectedMatchCount = 2;
        String expectedImportDef = "male";
        
        ImportFileHelper helper = new ImportFileHelper(core);
        
        try {
            helper.setOptions("0", 
                    "1", 
                    "2", 
                    "3", 
                    "4", 
                    "5", 
                    CSVFormat.DEFAULT, 
                    true, 
                    true, 
                    "\"", 
                    ImportFileHelper.DuplicateOption.IMPORT_ALL);
            
            helper.importFile(TEST_FILE, 0);
            
            assertEquals(expectedWordCount, core.getWordCollection().getWordCount());
            Map<String, List<ConWord>> wordMap = core.getWordCollection().getValueMapping();
            assertEquals(expectedWordmapSize, wordMap.size());
            assertTrue(wordMap.containsKey(ORIGIN_VAL));
            List<ConWord> matchWords = wordMap.get(ORIGIN_VAL);
            assertEquals(expectedMatchCount, matchWords.size());
            assertEquals(ORIGIN_DEF, matchWords.get(0).getDefinition());
            assertEquals(expectedImportDef, matchWords.get(1).getDefinition());
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testImportFileIgnoreDupes() {
        System.out.println("ImportFileHelperTest.testImportFileIgnoreDupes");
        
        int expectedWordCount = 4;
        int expectedWordmapSize = 4;
        int expectedMatchCount = 1;
        
        ImportFileHelper helper = new ImportFileHelper(core);
        
        try {
            helper.setOptions("0", 
                    "1", 
                    "2", 
                    "3", 
                    "4", 
                    "5", 
                    CSVFormat.DEFAULT, 
                    true, 
                    true, 
                    "\"", 
                    ImportFileHelper.DuplicateOption.IGNORE_DUPES);
            
            helper.importFile(TEST_FILE, 0);
            
            assertEquals(expectedWordCount, core.getWordCollection().getWordCount());
            Map<String, List<ConWord>> wordMap = core.getWordCollection().getValueMapping();
            assertEquals(expectedWordmapSize, wordMap.size());
            assertTrue(wordMap.containsKey(ORIGIN_VAL));
            List<ConWord> matchWords = wordMap.get(ORIGIN_VAL);
            assertEquals(expectedMatchCount, matchWords.size());
            assertEquals(ORIGIN_DEF, matchWords.get(0).getDefinition());
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testImportFileOverwriteDupes() {
        System.out.println("ImportFileHelperTest.testImportFileOverwriteDupes");
        
        int expectedWordCount = 4;
        int expectedWordmapSize = 4;
        int expectedMatchCount = 1;
        String expectedImportDef = "male";
        
        ImportFileHelper helper = new ImportFileHelper(core);
        
        try {
            helper.setOptions("0", 
                    "1", 
                    "2", 
                    "3", 
                    "4", 
                    "5", 
                    CSVFormat.DEFAULT, 
                    true, 
                    true, 
                    "\"", 
                    ImportFileHelper.DuplicateOption.OVERWRITE_DUPES);
            
            helper.importFile(TEST_FILE, 0);
            
            assertEquals(expectedWordCount, core.getWordCollection().getWordCount());
            Map<String, List<ConWord>> wordMap = core.getWordCollection().getValueMapping();
            assertEquals(expectedWordmapSize, wordMap.size());
            assertTrue(wordMap.containsKey(ORIGIN_VAL));
            List<ConWord> matchWords = wordMap.get(ORIGIN_VAL);
            assertEquals(expectedMatchCount, matchWords.size());
            assertEquals(expectedImportDef, matchWords.get(0).getDefinition());
        } catch (Exception e) {
            fail(e);
        }
    }
    
}
