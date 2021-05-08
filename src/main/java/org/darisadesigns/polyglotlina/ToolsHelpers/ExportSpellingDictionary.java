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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;
import org.darisadesigns.polyglotlina.PGTUtil;

/**
 * This class exists to export spelling dictionaries from a language.
 * @author draque
 */
public class ExportSpellingDictionary {
    private final DictCore core;
    private final ConjugationManager conjMan;
    private final Set<String> wordSet;
    
    public ExportSpellingDictionary(DictCore _core) {
        core = _core;
        conjMan = core.getConjugationManager();
        wordSet = new HashSet<>();
    }
    
    public void ExportSpellingDictionary(String targetLocation) throws IOException {
        ConWord[] words = core.getWordCollection().getWordNodes();
        
        populateFromWordArray(words);
        saveToFile(targetLocation);
    }
    
    private void populateFromWordArray(ConWord[] words) {
        for (ConWord word : words) {
            populateFromWord(word);
        }
    }
    
    public void populateFromWord(ConWord word) {
        wordSet.add(word.getValue());
        
        addSingletonValues(word);
        addDimensionalValues(word);  
    }
    
    private void addDimensionalValues(ConWord word) {
        ConjugationPair[] dimensionals = conjMan.getAllCombinedIds(word.getWordTypeId());
        addValuesByCombId(dimensionals, word);
    }
    
    private void addSingletonValues(ConWord word) {
        ConjugationPair[] singletons = core.getConjugationManager().getSingletonCombinedIds(word.getWordTypeId());
        addValuesByCombId(singletons, word);
    }
    
    private void addValuesByCombId(ConjugationPair[] conjPairs, ConWord word) {
        int typeId = word.getWordTypeId();
        
        for (ConjugationPair pair : conjPairs) {
            if (conjMan.isCombinedConjlSurpressed(pair.combinedId, typeId)) {
                continue;
            }
            
            wordSet.add(word.getWordForm(pair.combinedId));
        }
    }
    
    public String getCurrentStringValue() {
        StringBuilder sb = new StringBuilder();
        
        // ensure consistent ordering
        List<String> wordList = new ArrayList(wordSet);
        Collections.sort(wordList);
        
        for (String wordForm : wordList) {
            if (wordForm.isBlank()) {
                continue;
            }
            
            sb.append(wordForm);
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    public void saveToFile(String targetLocation) throws IOException {
        String output = getCurrentStringValue();
        core.getOSHandler().getIOHandler().createFileWithContents(targetLocation, output);
        
        // set wretched metadata on OSX so that MS Word will recognize it...
        if (PGTUtil.IS_OSX) {
            try {
                core.getOSHandler().getIOHandler().addFileAttributeOSX(targetLocation,
                        PGTUtil.OSX_FINDER_METADATA_NAME, 
                        PGTUtil.OSX_FINDER_INFO_VALUE_DIC_FILES,
                        true);
            } catch (Exception e) {
                core.getOSHandler().getIOHandler().writeErrorLog(e);
                new DesktopInfoBox(null).warning("Metadata Error", "Problem writing metadata.");
            }
        }
    }
}
