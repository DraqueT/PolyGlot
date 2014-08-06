/*
 * Copyright (c) 2014, draque
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * This source code may not be included in any commercial or for profit 
 *  software without the express written and signed consent of the copyright
 *  holder.
 *
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
package PolyGlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author draque
 *
 */
public class ConWordCollection extends DictionaryCollection {
    private final DictCore core;
    Map<String, Integer> allConWords;
    Map<String, Integer> allLocalWords;

    public ConWordCollection(DictCore _core) {
        bufferNode = new ConWord();
        allConWords = new HashMap<String, Integer>();
        allLocalWords = new HashMap<String, Integer>();
        core = _core;
    }

    /**
     * inserts current buffer word to conWord list based on id; blanks out
     * buffer
     *
     * @param _id
     * @return
     * @throws Exception
     */
    public Integer insert(Integer _id) throws Exception {
        Integer ret;

        ConWord insWord = new ConWord();
        insWord.setEqual(bufferNode);
        insWord.setId(_id);

        ret = super.insert(_id, bufferNode);

        balanceWordCounts(insWord, true);

        bufferNode = new ConWord();

        return ret;
    }

    /**
     * inserts current buffer to conWord list and generates id; blanks out
     * buffer
     *
     * @return ID of newly created node
     * @throws Exception
     */
    public Integer insert() throws Exception {
        Integer ret;

        ret = super.insert(bufferNode);

        balanceWordCounts((ConWord) bufferNode, true);

        bufferNode = new ConWord();

        return ret;
    }

    /**
     * Tests whether collection contains a particular local word
     *
     * @param local string value to search for
     * @return whether multiples of local word exists in collection
     */
    public boolean containsLocalMultiples(String local) {
        boolean ret = false;

        if (allLocalWords.containsKey(local)) {
            ret = allLocalWords.get(local) > 1;
        }

        return ret;
    }

    /**
     * Tests whether collection contains a particular conword
     *
     * @param word string value to search for
     * @return whether multiples of conword exists in the collection
     */
    public boolean containsWord(String word) {
        boolean ret = false;

        if (allConWords.containsKey(word)) {
            ret = allConWords.get(word) > 1;
        }

        return ret;
    }

    /**
     * Balances count of conwords and localwords (string values)
     *
     * @param insWord word to factor into counts
     * @param additive true if adding, false if removing
     */
    private void balanceWordCounts(ConWord insWord, boolean additive) {
        Integer curCount = allConWords.containsKey(insWord.getValue())
                ? allConWords.get(insWord.getValue()) : 0;
        allConWords.remove(insWord.getValue());
        allConWords.put(insWord.getValue(), curCount + (additive ? 1 : -1));

        curCount = allLocalWords.containsKey(insWord.getLocalWord())
                ? allLocalWords.get(insWord.getLocalWord()) : 0;
        allLocalWords.remove(insWord.getLocalWord());
        allLocalWords.put(insWord.getLocalWord(), curCount + (additive ? 1 : -1));
    }

    @Override
    public void deleteNodeById(Integer _id) throws Exception {
        ConWord deleteWord = this.getNodeById(_id);

        balanceWordCounts(deleteWord, false);

        super.deleteNodeById(_id);
    }

    @Override
    public void modifyNode(Integer _id, DictNode _modNode) throws Exception {
        // do bookkeepingfor word counts
        ConWord oldWord = getNodeById(_id);
        balanceWordCounts(oldWord, false);
        balanceWordCounts((ConWord) _modNode, true);

        super.modifyNode(_id, _modNode);
    }
    
    /**
     * recalculates all non-overridden pronunciations
     * @throws java.lang.Exception
     */
    void recalcAllProcs() throws Exception {
        Iterator<ConWord> it = this.getNodeIterator();
        
        while (it.hasNext()) {
            ConWord curWord = it.next();
            
            // only runs if word's pronunciation not overridden
            if (!curWord.isProcOverride()) {
                curWord.setPronunciation(core.getPronunciation(curWord.getValue()));
                this.modifyNode(curWord.getId(), curWord);
            }
        }
    }

    /**
     * Returns list of words in descending list of synonym match
     *
     * @param _match The string value to match for
     * @return list of matching words
     */
    public List<ConWord> getSuggestedTransWords(String _match) {
        List<ConWord> localEquals = new ArrayList<ConWord>();
        List<ConWord> localContains = new ArrayList<ConWord>();
        List<RankedObject> definitionContains = new ArrayList<RankedObject>();
        Iterator<Entry<Integer, ConWord>> allWords = nodeMap.entrySet().iterator();

        // on empty, return empty list
        if (_match.equals("")) {
            return localEquals;
        }
        
        Entry<Integer, ConWord> curEntry;
        ConWord curWord;
        
        // cycles through all words, searching for matches
        while (allWords.hasNext()) {
            curEntry = allWords.next();
            curWord = curEntry.getValue();
            
            if(curWord.getLocalWord().equals(_match)) {
                // local word equility is the hiest ranking matc
                localEquals.add(curWord);
            } else if (curWord.getLocalWord().contains(_match)) {
                // local word contains value is the second highest ranking match
                localContains.add(curWord);
            } else if (curWord.getDefinition().contains(_match)) {
                // definition contains is ranked third, and itself raked inernally
                // by match poition
                definitionContains.add(new RankedObject(curWord, curWord.getDefinition().indexOf(_match))); 
            }            
        }
        
        Collections.sort(definitionContains);
        
        
        // concatinate results
        ArrayList<ConWord> ret = new ArrayList<ConWord>();
        ret.addAll(localEquals);
        ret.addAll(localContains);
        
        // must add through iteration here
        Iterator<RankedObject> it = definitionContains.iterator();
        while (it.hasNext()) {
            RankedObject curObject = it.next();
            ConWord curDefMatch = (ConWord)curObject.getHolder();
            
            ret.add(curDefMatch);
        }

        return ret;
    }

    public Iterator<ConWord> filteredList(ConWord _filter) throws Exception {
        ConWordCollection retValues = new ConWordCollection(core);
        retValues.setAlphaOrder(alphaOrder);

        Iterator<Entry<Integer, ConWord>> filterList = nodeMap.entrySet()
                .iterator();
        Entry<Integer, ConWord> curEntry;
        ConWord curWord;

        while (filterList.hasNext()) {
            curEntry = filterList.next();
            curWord = curEntry.getValue();

            try {
		// each filter test split up to minimize compares

                // definition
                if (!_filter.getDefinition().trim().equals("")
                        && !curWord.getDefinition().contains(
                                _filter.getDefinition())) {
                    continue;
                }

                // type (exact match only)
                if (!_filter.getWordType().trim().equals("")
                        && !curWord.getWordType().equals(_filter.getWordType())) {
                    continue;
                }

                // local word
                if (!_filter.getLocalWord().trim().equals("")
                        && !curWord.getLocalWord().contains(
                                _filter.getLocalWord())) {
                    continue;
                }

                // con word
                if (!_filter.getValue().trim().equals("")
                        && !curWord.getValue().contains(_filter.getValue())) {
                    continue;
                }

                // gender (exact match only)
                if (!_filter.getGender().trim().equals("")
                        && !curWord.getGender().equals(_filter.getGender())) {
                    continue;
                }

                // pronunciation
                if (!_filter.getPronunciation().trim().equals("")
                        && !curWord.getPronunciation().contains(_filter.getPronunciation())) {
                    continue;
                }

                retValues.getBufferWord().setEqual(curWord);
                retValues.insert(curWord.getId());
            } catch (Exception e) {
                throw new Exception("FILTERING ERROR" + e.getMessage());
            }

        }

        return retValues.getNodeIterator();
    }

    @Override
    public ConWord getNodeById(Integer _id) throws Exception {
        return (ConWord) super.getNodeById(_id);
    }

    /**
     * wipes current word buffer
     */
    @Override
    public void clear() {
        bufferNode = new ConWord();
    }

    public ConWord getBufferWord() {
        return (ConWord) bufferNode;
    }

    public void setBufferWord(ConWord bufferWord) {
        this.bufferNode = bufferWord;
    }

    /**
     * returns iterator of nodes with their IDs as the entry key (ordered)
     *
     * @return
     */
    public Iterator<ConWord> getNodeIterator() {
        List<ConWord> retList = new ArrayList<ConWord>(nodeMap.values());

        Collections.sort(retList);

        return retList.iterator();
    }
    
    /**
     * Builds report on words in ConLang. Potentially computationally expensive.
     * @return 
     */
    public String buildWordReport() {
        String ret = "";
        String conFontTag = "face=\"" + core.getLangFont().getFontName() + "\"";
        
        Map<String, Integer> wordStart = new HashMap<String, Integer>();
        Map<String, Integer> wordEnd = new HashMap<String, Integer>();
        Map<String, Integer> characterCombos2 = new HashMap<String, Integer>();
        Integer highestCombo2 = 0;
        Map<String, Integer> characterCombos3 = new HashMap<String, Integer>(); // TODO: display this? Too much?
        Map<String, Integer> typeCountByWord = new HashMap<String, Integer>();
        Map<String, Integer> phonemeCount = new HashMap<String, Integer>();
        Map<String, Integer> charCount = new HashMap<String, Integer>();
        Map<String, Integer> phonemeCombo2 = new HashMap<String, Integer>(); 
        Integer wordCount = nodeMap.size();
        
        Iterator<ConWord> wordIt = new ArrayList<ConWord>(nodeMap.values()).iterator();
        
        // Put values into maps to count/record... 
        while (wordIt.hasNext()) {
            ConWord curWord = wordIt.next();
            final String curValue = curWord.getValue();
            final int curValueLength = curValue.length();
            final String curType = curWord.getWordType();
            
            String beginsWith = curValue.substring(0, 1);
            String endsWith = curValue.substring(curValueLength - 1, curValueLength);
            
            // either increment or create value for starting character
            if (wordStart.containsKey(beginsWith)) {
                wordStart.replace(beginsWith, wordStart.get(beginsWith) + 1);
            } else {
                wordStart.put(beginsWith, 1);
            }
            
            // either increment or create value for ending character
            if (wordEnd.containsKey(endsWith)) {
                wordEnd.replace(endsWith, wordEnd.get(endsWith) + 1);                
            } else {
                wordEnd.put(endsWith, 1);
            }
            
            // capture and record all phonemes in word and phoneme combinations
            List<PronunciationNode> phonArray = core.getPronunciationElements(curValue);
            for (int i = 0; i < phonArray.size(); i++) {
                if (phonemeCount.containsKey(phonArray.get(i).getPronunciation())) {
                    phonemeCount.replace(phonArray.get(i).getPronunciation(),
                            phonemeCount.get(phonArray.get(i).getPronunciation()) + 1);
                } else {
                    phonemeCount.put(phonArray.get(i).getPronunciation(), 1);
                }
                
                // grab combo if there are additinal phonemes, otherwise you're done
                if (i + 1 < phonArray.size()) {
                    String curCombo = phonArray.get(i).getPronunciation() + " " 
                            + phonArray.get(i+1).getPronunciation();
                    
                    if (phonemeCombo2.containsKey(curCombo)) {
                        phonemeCombo2.replace(curCombo, phonemeCombo2.get(curCombo) + 1);
                    } else {
                        phonemeCombo2.put(curCombo, 1);
                    }
                }
            }
            
            // caupture all individual characters
            for (int i = 0; i < curValueLength; i++) {
                String curChar = curValue.substring(i, i + 1);
                
                if (charCount.containsKey(curChar)) {
                    charCount.replace(curChar, charCount.get(curChar) + 1);
                } else {
                    charCount.put(curChar, 1);
                }
            }
            
            // capture and record all 2 character combinations in words
            for (int i = 0; i < curValueLength - 1; i++) {
                String combo = curValue.substring(i, i + 2);
                
                if (characterCombos2.containsKey(combo)) {
                    int curComboCount = characterCombos2.get(combo);
                    
                    if (highestCombo2 <= curComboCount) {
                        highestCombo2 = curComboCount + 1;
                    }
                    
                    characterCombos2.replace(combo, curComboCount + 1);
                } else {
                    characterCombos2.put(combo, 1);
                }
            }
            
            // capture and record all 3 character combinations in words
            for (int i = 0; i < curValueLength - 2; i++) {
                String combo = curValue.substring(i, i + 3);
                
                if (characterCombos3.containsKey(combo)) {
                    characterCombos3.replace(combo, characterCombos3.get(combo) + 1);
                } else {
                    characterCombos3.put(combo, 1);
                }
            }
            
            // record type count...
            if (typeCountByWord.containsKey(curType)) {
                typeCountByWord.replace(curType, typeCountByWord.get(curType) + 1);
            } else {
                typeCountByWord.put(curType, 1);
            }
        } 
        
        ret += "Count of words in conlang lexicon: " + wordCount + "<br><br>";
        
        // build display of type counts
        ret += "count of words by type:<br>";
        for(Entry<String, Integer> curEntry : typeCountByWord.entrySet()) {
            ret += curEntry.getKey() + " : " + curEntry.getValue() + "<br>";
        }
        ret += "<br><br>";        

        // build display for starts-with statistics
        ret += " Breakdown of words counted starting with letter:<br>";
        for (char letter : core.getPropertiesManager().getAlphaPlainText().toCharArray()) {
            ret += "<font " + conFontTag + ">" + letter + "</font> : " 
                    + (wordStart.containsKey(""+letter) ? wordStart.get(""+letter) : "0") + "<br>";
        }        
        ret += "<br><br>";
        
        // build display for ends-with statistics
        ret += " Breakdown of words counted ending with letter:<br>";
        for (char letter : core.getPropertiesManager().getAlphaPlainText().toCharArray()) {
            ret += "<font " + conFontTag + ">" + letter + "</font> : " 
                    + (wordEnd.containsKey(""+letter) ? wordEnd.get(""+letter) : "0") + "<br>";
        }        
        ret += "<br><br>";
        
        // build display for character counts
        ret += " Breakdown of characters counted across all words:<br>";
        for (char letter : core.getPropertiesManager().getAlphaPlainText().toCharArray()) {
            ret += "<font " + conFontTag + ">" + letter + "</font> : " 
                    + (charCount.containsKey(""+letter) ? charCount.get(""+letter) : "0") + "<br>";
        }        
        ret += "<br><br>";
        
        // build display for phoneme count
        ret += " Breakdown of phonemes counted across all words:<br>";
        Iterator<PronunciationNode> procLoop = core.getPronunciations();
        while (procLoop.hasNext()) {
            PronunciationNode curNode = procLoop.next();
            ret += curNode.getPronunciation() + " : " 
                    + (phonemeCount.containsKey(curNode.getPronunciation()) ? 
                    phonemeCount.get(curNode.getPronunciation()) : "0") + "<br>";
        }        
        ret += "<br><br>";
       
        // buid grid of 2 letter combos
        ret += "Heat map of letter combination frequency:<br>";        
        ret += "<table border=\"1\">";        
        ret += "<tr><td></td>";
        for (char topRow : core.getPropertiesManager().getAlphaPlainText().toCharArray()) {
            ret += "<td><font " + conFontTag + ">" + topRow + "</font></td>";
        }
        ret += "</tr>";               
        for (char y : core.getPropertiesManager().getAlphaPlainText().toCharArray()) {
            ret += "<tr><td><font " + conFontTag + ">" + y + "</font></td>";
            for (char x : core.getPropertiesManager().getAlphaPlainText().toCharArray()) {
                String search = ""+x+y;
                Integer comboValue = (characterCombos2.containsKey(search) ? 
                        characterCombos2.get(search) : 0);
                
                Integer red = (255 / highestCombo2) * comboValue;
                Integer blue = 255 - red;
                ret += "<td bgcolor=rgb(" + red + "," + blue + "," + blue + ")>" 
                        + "<font " + conFontTag + ">" + x + y + "</font>" + ":" 
                        + comboValue.toString() + "</td>";
            }
            ret += "</tr>";
        }        
        ret += "</table><br><br>";
        
        // buid grid of 2 phoneme combos
        ret += "Heat map of phoneme combination frequency:<br>";        
        ret += "<table border=\"1\">";        
        ret += "<tr><td></td>";
        Iterator<PronunciationNode> procIty = core.getPronunciations();
        while (procIty.hasNext()) {
            ret += "<td>" + procIty.next().getPronunciation() + "</td>";
        }
        ret += "</tr>";               
        procIty = core.getPronunciations();
        while (procIty.hasNext()) {
            PronunciationNode y = procIty.next();            
            ret += "<tr><td>" + y.getPronunciation() + "</td>";            
            Iterator<PronunciationNode> procItx = core.getPronunciations();
            while (procItx.hasNext()) {
                PronunciationNode x = procItx.next();
                String search = x.getPronunciation() + " " +y.getPronunciation();
                Integer comboValue = (phonemeCombo2.containsKey(search) ? 
                        phonemeCombo2.get(search) : 0);
                
                Integer red = (255 / highestCombo2) * comboValue;
                Integer blue = 255 - red;
                ret += "<td bgcolor=rgb(" + red + "," + blue + "," + blue + ")>" 
                        + x.getPronunciation() + y.getPronunciation() + ":" 
                        + comboValue.toString() + "</td>";
            }
            ret += "</tr>";
        }        
        ret += "</table>";
        
        return ret;
    }
}
