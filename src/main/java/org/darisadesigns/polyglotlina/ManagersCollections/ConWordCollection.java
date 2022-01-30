/*
 * Copyright (c) 2014-2021, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
 * See LICENSE.TXT included with this code to read the full license agreement.
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.FormattedTextHelper;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;
import org.darisadesigns.polyglotlina.Nodes.EtyExternalParent;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.RankedObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import org.darisadesigns.polyglotlina.Nodes.EvolutionPair;
import org.darisadesigns.polyglotlina.Nodes.EvolutionPair.EvolutionType;
import org.darisadesigns.polyglotlina.RegexTools;
import org.darisadesigns.polyglotlina.RegexTools.ReplaceOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Draque Thompson
 *
 */
public class ConWordCollection extends DictionaryCollection<ConWord> {

    private static final String SPLIT_CHAR = ",";
    private final DictCore core;
    private boolean orderByLocal = false;

    public ConWordCollection(DictCore _core) {
        super(new ConWord());

        bufferNode.setCore(_core);
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
        insWord.setCore(core);
        insWord.setEqual(bufferNode);
        insWord.setId(_id);

        bufferNode.setParent(this);
        bufferNode.setCore(core);
        ret = super.insert(_id, bufferNode);

        bufferNode = new ConWord();
        bufferNode.setCore(core);

        return ret;
    }

    /**
     * Gets all words that are illegal in some way
     *
     * @return an iterator full of all illegal conwords
     */
    public ConWord[] illegalFilter() {
        List<ConWord> retList = new ArrayList<>();

        for (ConWord curWord : nodeMap.values()) {
            if ((!curWord.isWordLegal() && !curWord.isRulesOverride()) 
                    && !retList.contains(curWord)) {
                retList.add(curWord);
            }
        }

        this.safeSort(retList);
        return retList.toArray(new ConWord[0]);
    }
    
    /**
     * Loads Swadesh list from buffered input stream.Presumes that list is line separated.Lines PREFIXED with the # character will be skipped.If it appears in the middle of a line, it will be parsed regularly.
     * @param bs
     * @param showPrompt
     * @throws java.io.IOException 
     * @throws java.lang.Exception 
     */
    public void loadSwadesh(BufferedInputStream bs, boolean showPrompt) throws IOException, Exception {
        String line;
        
        try (BufferedReader r = new BufferedReader(new InputStreamReader(bs, StandardCharsets.UTF_8))) {
            if (!showPrompt || core.getOSHandler().getInfoBox().actionConfirmation("Import Swadesh List?", 
                    "This will import all the words defined within this Swadesh list into your lexicon. Continue?")) {
                for (int i = 0; (line = r.readLine()) != null; i++) {
                    if (line.startsWith("#")) {
                        continue;
                    }

                    // even out the lines so they appear in proper order. if a list is over 1k entries... that is too many. I'll fix it if people complain. X|
                    String swadeshNum = Integer.toString(i);
                    while (swadeshNum.length() < 3) {
                        swadeshNum = "0" + swadeshNum;
                    }

                    ConWord newWord = new ConWord();
                    newWord.setValue("#" + swadeshNum + ": " + line.toUpperCase());
                    newWord.setLocalWord(line);
                    this.addWord(newWord);
                }
            }
        }
    }

    /**
     * Checks whether word is legal and returns error reason if not
     * DO NOT RUN IN LOOP. Instead use overridden version with cached count values
     * @param word word to check legality of
     * @return ConWord with any illegal entries saved as word values
     */
    public ConWord testWordLegality(ConWord word) {
        return this.testWordLegality(word, this.getConWordCount(), this.getLocalCount());
    }
    
    /**
     * Checks whether word is legal and returns error reason if not
     *
     * @param word word to check legality of
     * @param wordCount map of conword values to the number of times they appear in a word (use getConWordCount)
     * @param localCount map of localword values to the number of times they appear in a word (use getLocalCount)
     * @return ConWord with any illegal entries saved as word values
     */
    public ConWord testWordLegality(ConWord word, Map<String, Integer> wordCount, Map<String, Integer> localCount) {
        ConWord ret = new ConWord();
        String pronunciation = "";
        
        try {
            pronunciation = word.getPronunciation();
        } catch (Exception e) {
            // IOHandler.writeErrorLog(e);
            ret.setDefinition("Pronunciation cannot be generated, likely due to malformed regex in pronunciation menu.");
        }

        if (word.getValue().isEmpty()) {
            ret.setValue(core.conLabel() + " word value cannot be blank.");
        }

        if (word.getWordTypeId() == 0 && core.getPropertiesManager().isTypesMandatory()) {
            ret.typeError = "Part of Speech set to mandatory.";
        }

        if (word.getLocalWord().isEmpty() && core.getPropertiesManager().isLocalMandatory()) {
            ret.setLocalWord(core.localLabel() + " word set to mandatory.");
        }

        if (core.getPropertiesManager().isWordUniqueness() 
                && wordCount.containsKey(word.getValue())
                && (wordCount.containsKey("") || wordCount.get(word.getValue()) > 1)) {
            ret.setValue(ret.getValue() + (ret.getValue().isEmpty() ? "" : "\n")
                    + core.conLabel() + " words set to enforced unique: this conword exists elsewhere.");
        }

        String localWord = word.getLocalWord();
        if (core.getPropertiesManager().isLocalUniqueness() 
                && !localWord.isEmpty()
                && localCount.containsKey(localWord)
                && (localCount.containsKey("") || localCount.get(localWord) > 1)) {
            ret.setLocalWord(ret.getLocalWord() + (ret.getLocalWord().isEmpty() ? "" : "\n")
                    + core.localLabel() + " words set to enforced unique: this local exists elsewhere.");
        }

        TypeNode wordPos = core.getTypes().getNodeById(word.getWordTypeId());

        ret.setDefinition(ret.getDefinition() + (ret.getDefinition().isEmpty() ? "" : "\n"));

        if (wordPos != null) {
            String posRegex = wordPos.getPattern();

            if (wordPos.isProcMandatory() && pronunciation.isEmpty() && !word.isProcOverride()) {
                ret.setDefinition(ret.getDefinition() + (ret.getDefinition().isEmpty() ? "" : "\n")
                        + "Pronunciation required for " + wordPos.getValue() + " words.");
            }
            
            if (!posRegex.isEmpty() && RegexTools.isRegexLegal(posRegex) && !word.getValue().matches(posRegex)) {
                ret.setDefinition(ret.getDefinition() + (ret.getDefinition().isEmpty() ? "" : "\n")
                        + "Word does not match enforced pattern for type: " + word.getWordTypeDisplay() + ".");
                ret.setProcOverride(true);
            }
            
            if (wordPos.isDefMandatory() && word.getDefinition().isEmpty()) {
                ret.setDefinition(ret.getDefinition() + (ret.getDefinition().isEmpty() ? "" : "\n")
                        + "Definition required for " + wordPos.getValue() + " words.");
            }
        }

        if (!core.getOSHandler().getPFontHandler().canStringBeRendered(word.getValue(), true)) {
            ret.setValue(ret.getValue() + "\nWord: \"" + word.getValue() + "\" with local value: \"" + word.getLocalWord() 
                    + "\" cannot be rendered properly using font: " + core.getPropertiesManager().getFontConFamily());
        }

        return ret;
    }

    /**
     * inserts current buffer to conWord list and generates id; blanks out
     * buffer
     *
     * @return ID of newly created node
     * @throws Exception
     */
    @Override
    public Integer insert() throws Exception {
        Integer ret;

        bufferNode.setParent(this);
        bufferNode.setCore(core);
        ret = super.insert(bufferNode);

        bufferNode = new ConWord();
        bufferNode.setCore(core);

        return ret;
    }

    /**
     * Gets count of conwords in dictionary
     *
     * @return number of conwords in dictionary
     */
    public int getWordCount() {
        return nodeMap.size();
    }

    /**
     * Tests whether a value exists in the dictionary currently
     * Do not use this within a loop.
     *
     * @param conWord value to search for
     * @return true if exists, false otherwise
     */
    public boolean testWordValueExists(String conWord) {
        boolean ret = false;
        
        // don't bother checking blanks
        if (core.getPGTUtil().isBlank(conWord)) {
            ret = false;
        } else {
            for (ConWord word : this.nodeMap.values()) {
                if (conWord.equals(word.getValue())) {
                    ret = true;
                }
            }
        }
        
        return ret;
    }

    /**
     * Tests whether a value exists in the dictionary currently
     * Do not use this within a loop.
     * 
     * @param local value to search for
     * @return true if exists, false otherwise
     */
    public boolean testLocalValueExists(String local) {
        boolean ret = false;
        
        // don't bother checking blanks
        if (core.getPGTUtil().isBlank(local)) {
            ret = true;
        } else {
            for (ConWord word : this.nodeMap.values()) {
                if (local.equals(word.getLocalWord())) {
                    ret = true;
                }
            }
        }
        
        return ret;
    }
    
    /**
     * Generates and returns map with strings to count of
     * string occurrences within local words of the lexicon
     * @return 
     */
    public Map<String, Integer> getLocalCount() {
        Map<String, Integer> ret = new HashMap<>();
        
        for (ConWord word : this.nodeMap.values()) {
            String local = word.getLocalWord();
            if (ret.containsKey(local)) {
                ret.replace(local, ret.get(local) + 1);
            } else {
                ret.put(local,1);
            }
        }
        
        return ret;
    }
    
    /**
     * Generates and returns map with strings to count of
     * string occurrences within conlang words of the lexicon
     * @return 
     */
    public Map<String, Integer> getConWordCount() {
        Map<String, Integer> ret = new HashMap<>();
        
        for (ConWord word : this.nodeMap.values()) {
            String local = word.getValue();
            if (ret.containsKey(local)) {
                ret.replace(local, ret.get(local) + 1);
            } else {
                ret.put(local,1);
            }
        }
        
        return ret;
    }

    /**
     * Deletes word and clears all declensions
     *
     * @param _id ID of word to delete
     * @throws Exception
     */
    @Override
    public void deleteNodeById(Integer _id) throws Exception {
        super.deleteNodeById(_id);
        core.getConjugationManager().clearAllConjugationsWord(_id);
    }

    @Override
    public void modifyNode(Integer _id, ConWord _modNode) throws Exception {
        _modNode.setCore(core);
        super.modifyNode(_id, _modNode);
    }

    /**
     * Performs all actions of superclass, and additionally sets core value of
     * words
     *
     * @param _id same as super
     * @param _buffer same as super
     * @return same as super
     * @throws Exception same as super
     */
    @Override
    protected Integer insert(Integer _id, ConWord _buffer) throws Exception {
        _buffer.setCore(core);
        _buffer.setParent(this);
        return super.insert(_id, _buffer);
    }

    /**
     * Returns list of words in descending list of synonym match
     *
     * @param _match The string value to match for
     * @return list of matching words
     */
    public ConWord[] getSuggestedTransWords(String _match) {
        ArrayList<ConWord> ret = new ArrayList<>();
        List<ConWord> localEquals = new ArrayList<>();
        List<ConWord> localContains = new ArrayList<>();
        List<RankedObject<ConWord>> definitionContains = new ArrayList<>();
        Iterator<Entry<Integer, ConWord>> allWords = nodeMap.entrySet().iterator();

        // on empty, return empty array
        if (!_match.isEmpty()) {
            Entry<Integer, ConWord> curEntry;
            ConWord curWord;

            // cycles through all words, searching for matches
            while (allWords.hasNext()) {
                curEntry = allWords.next();
                curWord = curEntry.getValue();

                String word = curWord.getValue();
                String compare = _match;
                String definition = curWord.getDefinition();

                // on ignore case, force all to lowercase
                if (core.getPropertiesManager().isIgnoreCase()) {
                    word = word.toLowerCase();
                    compare = compare.toLowerCase();
                    definition = definition.toLowerCase();
                }

                if (word.equals(compare)) {
                    // local word equality is the highest ranking match
                    localEquals.add(curWord);
                } else if (word.contains(compare)) {
                    // local word contains value is the second highest ranking match
                    localContains.add(curWord);
                } else if (definition.contains(compare)) {
                    // definition contains is ranked third, and itself raked internally
                    // by match position
                    definitionContains.add(new RankedObject<>(curWord, definition.indexOf(compare)));
                }
            }

            Collections.sort(definitionContains);

            // concatenate results
            ret.addAll(localEquals);
            ret.addAll(localContains);

            // must add through iteration here
            Iterator<RankedObject<ConWord>> it = definitionContains.iterator();
            while (it.hasNext()) {
                RankedObject<ConWord> curObject = it.next();
                ret.add(curObject.getHolder());
            }
        }

        return ret.toArray(new ConWord[0]);
    }
    
    /**
     * This evolves a lexicon based on user input.It cycles through the entire 
 lexicon and updates the values of words accordingly.
     * 
     * @param _filter filter conword used for filtering effects
     * @param percent the chance that any given word will be evolved
     * @param instanceOption
     * @param regex the regex to apply a transformation
     * @param replacement the replacement text
     * @return 
     * @throws java.lang.Exception on filter error
     */
    public EvolutionPair[] evolveLexicon(ConWord _filter, 
            int percent, 
            ReplaceOptions instanceOption, 
            String regex, 
            String replacement) throws Exception {
        Random rand = new Random();
        List<EvolutionPair> ret = new ArrayList<>();
        
        for (ConWord word : filteredList(_filter)) {
            if (rand.nextInt(99) < percent) {
                String originalValue = word.getValue();
                
                try {
                    word.evolveWord(regex, replacement, instanceOption);
                    core.getConjugationManager().evolveConjugatedWordForms(word.getId(), regex, replacement, instanceOption);
                    
                    String newValue = word.getValue();
                    
                    if (core.getPGTUtil().isBlank(newValue)) {
                        throw new Exception("Evolved word form is blank.");
                    }
                    
                    if (originalValue.equals(newValue)) {
                        // no need to record if no change made
                        continue;
                    }
                    
                    ret.add(new EvolutionPair(originalValue, 
                            newValue, 
                            EvolutionType.word, 
                            "", 
                            ""
                    ));
                } catch (Exception e) {
                    // revert on error
                    String errorValue = word.getValue();
                    word.setValue(originalValue);
                    ret.add(new EvolutionPair(originalValue, 
                            errorValue, 
                            EvolutionType.word, 
                            e.getLocalizedMessage(), 
                            ""
                    ));
                }
            }
        }
        
        return ret.toArray(new EvolutionPair[0]);
    }

    /**
     * Uses conword passed as parameter to filter on the entire dictionary of
     * words, based on attributes set on the parameter. Returns iterator of all
     * words that match. As a note: the conword value of the filter parameter is
     * matched not only against the values of all conwords in the dictionary,
     * but also their conjugations/declensions
     *
     * @param _filter A conword object containing filter values
     * @return an list of conwords which match the given search
     * @throws Exception on filtering error
     */
    public ConWord[] filteredList(ConWord _filter) throws Exception {
        ConWordCollection retValues = new ConWordCollection(core);
        retValues.setAlphaOrder(alphaOrder);

        Iterator<Entry<Integer, ConWord>> filterList = nodeMap.entrySet().iterator();
        Entry<Integer, ConWord> curEntry;
        ConWord curWord;
        // definition search should always ignore case
        _filter.setDefinition(_filter.getDefinition().toLowerCase());

        // set filter to lowercase if ignoring case
        if (core.getPropertiesManager().isIgnoreCase()) {
            _filter.setDefinition(_filter.getDefinition().toLowerCase());
            _filter.setLocalWord(_filter.getLocalWord().toLowerCase());
            _filter.setValue(_filter.getValue().toLowerCase());
            _filter.setPronunciation(_filter.getPronunciation().toLowerCase());
        }

        while (filterList.hasNext()) {
            curEntry = filterList.next();
            curWord = curEntry.getValue();
            try {
                // definition should always ignore case
                String definition = FormattedTextHelper.getTextBody(curWord.getDefinition()).toLowerCase();
                int type = curWord.getWordTypeId();
                String local;
                String proc;

                // if set to ignore case, set up caseless matches, normal otherwise
                if (core.getPropertiesManager().isIgnoreCase()) {
                    local = curWord.getLocalWord().toLowerCase();
                    proc = curWord.getPronunciation().toLowerCase();
                } else {
                    local = curWord.getLocalWord();
                    proc = curWord.getPronunciation();
                }

                // each filter test split up to minimize compares                
                // definition
                if (!_filter.getDefinition().trim().isEmpty()) {
                    boolean cont = true;

                    for (String def1 : _filter.getDefinition().split(SPLIT_CHAR)) {
                        if (definition.matches(".*" + def1 + ".*")) {
                            cont = false;
                            break;
                        }
                    }

                    if (cont) {
                        continue;
                    }
                }

                // type (exact match only)
                if (_filter.getWordTypeId() != 0
                        && type != _filter.getWordTypeId()) {
                    continue;
                }

                // local word
                if (!_filter.getLocalWord().trim().isEmpty()) {
                    String match = _filter.getLocalWord().trim();
                    
                    if (!local.contains(match)
                            && !local.matches(match)) {
                        continue;
                    }
                }

                // con word
                if (!_filter.getValue().trim().isEmpty()) {
                    boolean cont = true;

                    for (String val1 : _filter.getValue().split(SPLIT_CHAR)) {
                        if (matchHeadAndDeclensions(val1, curWord)) {
                            cont = false;
                            break;
                        }
                    }

                    if (cont) {
                        continue;
                    }
                }

                // pronunciation
                if (!_filter.getPronunciation().trim().isEmpty()) {
                    String match = _filter.getPronunciation();
                    
                    if (!proc.contains(match)
                            && !proc.matches(match)) {
                        continue;
                    }
                }
                
                // etymological root
                Object parent = _filter.getFilterEtyParent();
                if (parent != null) {
                    if (parent instanceof ConWord) {
                        ConWord parWord = (ConWord)parent;
                        if (parWord.getId() != -1 && !core.getEtymologyManager()
                                .childHasParent(curWord.getId(), parWord.getId())) {
                            continue;
                        }
                    }
                    
                    if (parent instanceof EtyExternalParent) {
                        EtyExternalParent parExt = (EtyExternalParent)parent;
                        if (parExt.getId() != -1 && !core.getEtymologyManager()
                                .childHasExtParent(curWord.getId(), parExt.getUniqueId())) {
                            continue;
                        }
                    } 
                }

                retValues.setBufferWord(curWord);
                retValues.insert(curWord.getId());
            } catch (Exception e) {
                // IOHandler.writeErrorLog(e);
                throw new Exception("FILTERING ERROR: " + e.getMessage(), e);
            }
        }

        return retValues.getWordNodes();
    }

    /**
     * Tests whether matchText matches the headword of the passed word, or any
     * declensions/conjugations of the word.
     *
     * @param matchText Text to match.
     * @param word Word within which to search for matches
     * @return true if match, false otherwise
     */
    private boolean matchHeadAndDeclensions(String matchText, ConWord word) {
        boolean ret = false;
        boolean ignoreCase = core.getPropertiesManager().isIgnoreCase();

        String head = ignoreCase ? word.getValue().toLowerCase() : word.getValue();

        if (matchText.trim().isEmpty()
                || head.matches(matchText)
                || head.contains(matchText)) {
            ret = true;
        }
        TypeNode type = core.getTypes().getNodeById(word.getWordTypeId());

        if (type != null && !ret) {
            int typeId = type.getId();
            ConjugationPair[] decPairs = core.getConjugationManager().getAllCombinedIds(typeId);

            for (ConjugationPair curPair : decPairs) {
                // silently skip erroring entries. Too cumbersome to deal with during a search
                try {
                    String declension = core.getConjugationManager()
                            .declineWord(word, curPair.combinedId);

                    if (!declension.trim().isEmpty()
                            && (declension.matches(matchText)
                            || declension.contains(matchText))) {
                        ret = true;
                    }
                } catch (Exception e) {
                    // do nothing (see above comment)
                    // IOHandler.writeErrorLog(e);
                }
            }
        }

        return ret;
    }

    @Override
    public ConWord getNodeById(Integer _id) {
        return (ConWord) super.getNodeById(_id);
    }

    /**
     * wipes current word buffer
     */
    @Override
    public void clear() {
        bufferNode = new ConWord();
        bufferNode.setCore(core);
    }

    public ConWord getBufferWord() {
        return bufferNode;
    }

    public void setBufferWord(ConWord bufferWord) {
        this.bufferNode = bufferWord;

        if (bufferWord.getCore() == null) {
            bufferWord.setCore(core);
        }
    }

    /**
     * returns iterator of nodes with their IDs as the entry key (ordered)
     *
     * @return
     */
    public ConWord[] getWordNodes() {
        return this.getWordNodesList().toArray(new ConWord[0]);
    }

    /**
     * Returns list of conlang words nodes
     *
     * @return List of ConWords
     */
    public List<ConWord> getWordNodesList() {
        List<ConWord> retList = new ArrayList<>(nodeMap.values());

        this.safeSort(retList);

        return retList;
    }

    /**
     * gets and returns iterator of all words based on alphabetical order of
     * localwords on the entries. Respects default alpha order.
     *
     * @return
     */
    public ConWord[] getNodesLocalOrder() {
        List<ConWord> cycleList = new ArrayList<>(nodeMap.values());
        List<ConWord> retList = new ArrayList<>();

        // cycle through and create copies of words with multiple local values
        cycleList.forEach((ConWord word) -> {
            String localPre = word.getLocalWord();
            if (localPre.contains(",")) {
                String[] allLocals = localPre.split(",");

                // create new temp word for purposes of dictionary creation
                for (String curLocal : allLocals) {
                    ConWord ins = new ConWord();
                    ins.setCore(core);
                    ins.setEqual(word);
                    ins.setLocalWord(curLocal);
                    ins.setParent(this);

                    retList.add(ins);
                }
            } else {
                retList.add(word);
            }
        });

        orderByLocal = true;
        this.safeSort(retList);
        orderByLocal = false;

        return retList.toArray(new ConWord[0]);
    }

    /**
     * Used to determine if lists should currently return in local order (this
     * is almost never used for anything but sorting. There is no setter.)
     *
     * @return whether to sort by local value
     */
    public boolean isLocalOrder() {
        return orderByLocal;
    }

    /**
     * Inserts new word into dictionary
     *
     * @param _addWord word to be inserted
     * @return ID of newly inserted word
     * @throws Exception
     */
    public int addWord(ConWord _addWord) throws Exception {
        int ret;
        if (_addWord.getCore() == null) {
            _addWord.setCore(core);
        }
        
        this.clear();
        bufferNode.setEqual(_addWord);

        ret = insert();

        return ret;
    }

    /**
     * Formats in HTML to a plain font to avoid conlang font
     *
     * @param toPlain text to make plain
     * @param core
     * @return text in plain tag
     */
    public static String formatPlain(String toPlain, DictCore core) {
        String defaultFont = "face=\"" + core.getPropertiesManager().getFontLocalFamily() + "\"";
        return "<font " + defaultFont + ">" + toPlain + "</font>";
    }

    /**
     * Formats in HTML to a conlang font
     *
     * @param toCon text to make confont
     * @param core
     * @return text in plain tag
     */
    public static String formatCon(String toCon, DictCore core) {
        // TODO: This is very bad. Strip this out at the same time that the language stats tool is rewritten. Use css style.
        String defaultFont = "face=\"" + core.getPropertiesManager().getFontConFamily() + "\"";
        return "<font " + defaultFont + ">" + toCon + "</font>";
    }

    /**
     * Writes all word information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        ConWord[] wordLoop = getWordNodes();
        Element lexicon = doc.createElement(PGTUtil.LEXICON_XID);
        
        for (ConWord curWord : wordLoop) {
            curWord.writeXML(doc, lexicon);
        }
        
        rootElement.appendChild(lexicon);
    }

    /**
     * Call this to wipe out the values of all deprecated
     * conjugations/declensions for a particular part of speech in the
     * dictionary
     *
     * @param typeId ID of word type to clear values from
     */
    public void clearDeprecatedDeclensions(Integer typeId) {
        ConjugationManager dm = core.getConjugationManager();
        Map<Integer, ConjugationPair[]> comTypeDecs = new HashMap<>();

        // iterates over every word
        nodeMap.values().stream()
                .filter((word) -> (word).getWordTypeId().equals(typeId))
                .forEach((word) -> {
            ConjugationPair[] curDeclensions;

            // ensure I'm only generating declension patterns for any given part of speech only once
            if (comTypeDecs.containsKey(word.getWordTypeId())) {
                curDeclensions = comTypeDecs.get(word.getWordTypeId());
            } else {
                curDeclensions = dm.getAllCombinedIds(word.getWordTypeId());
                comTypeDecs.put(word.getWordTypeId(), curDeclensions);
            }

            // retrieves all stored declension values for word
            Map<String, ConjugationNode> decMap = dm.getWordConjugation(word.getId());

            // removes all legitimate declensions from map
            for (ConjugationPair curPair : curDeclensions) {
                decMap.remove(curPair.combinedId);
            }

            // wipe remaining values from word
            dm.removeConjugationValues(word.getId(), decMap.values());
        });
    }
    
    /**
     * if a value is deleted from a class, this must be called. This cycles through all 
     * words and eliminates instances where the given class/value combo appear
     * @param classId class from which value was deleted
     * @param valueId value deleted
     */
    public void classValueDeleted(int classId, int valueId) {
        nodeMap.values().forEach((curWord)->{
            if(curWord.wordHasClassValue(classId, valueId)) {
                curWord.setClassValue(classId, -1);
            }
        });
    }
    
    @Override
    public ConWord notFoundNode() {
        ConWord notFound = new ConWord();

        notFound.setValue("WORD NOT FOUND");
        notFound.setCore(core);
        notFound.setDefinition("WORD NOT FOUND");
        notFound.setLocalWord("WORD NOT FOUND");
        notFound.setPronunciation("WORD NOT FOUND");
        
        return notFound;
    }
    
    /**
     * Gets a list of display word nodes. Set up specifically for display rather
     * than programmatic or logical consumption
     * @return 
     */
    public ConWordDisplay[] getWordNodesDisplay() {
        return toDisplayList(getWordNodes());
    }
    
    /**
     * Converts a list of words into a display list and reorders if appropriate
     * @param wordList List of words to convert to display list
     * @return 
     */
    public ConWordDisplay[] toDisplayList(ConWord[] wordList) {
        List<ConWordDisplay> ret = new ArrayList<>();
        
        for (ConWord conWord : wordList) {
            ret.add(new ConWordDisplay(conWord, core));
        }
        
        if (core.getPropertiesManager().isUseLocalWordLex()) {
            this.safeSortDisplay(ret);
        }
        
        return ret.toArray(new ConWordDisplay[0]);
    }
    
    /**
     * Safely sorts a list of the collection display. Workaround for tyical safesort on super
     * (Accounts for possible failure due to incomplete/incoherent alphabet written by user)
     * @param sort 
     */
    public void safeSortDisplay(List<ConWordDisplay> sort) {
        try {
            alphaOrder.setMissingChars(false);
            Collections.sort(sort);
        } catch (Exception e) {
            alphaOrder.setMissingChars(true);
            Collections.sort(sort);
        }
    }
    
    /**
     * Wrapper class of ConWord that allows for more display options in menus
     * Separated to eliminate possibility of display logic interfering with program logic
     */
    public static class ConWordDisplay implements Comparable<ConWordDisplay> {
        private final ConWord conWord;
        private final DictCore core;
        
        public ConWordDisplay(ConWord _conWord, DictCore _core) {
            conWord = _conWord;
            core = _core;
        }
        
        public ConWord getConWord() {
            return conWord;
        }
        
        @Override
        public String toString() {
            String ret;
            
            if (core.getPropertiesManager().isUseLocalWordLex()) {
                ret = conWord.getLocalWord();
            } else {
                ret = conWord.toString();
            }
            
            return ret.isEmpty() ? " " : ret;
        }
        
        /**
        * Respects language property to display/sort lexicon by local words
        * value set for this.
        * @param _compare
        * @return 
        */
       @Override
       public int compareTo(ConWordDisplay _compare) {
           String myLocalWord = conWord.getLocalWord();
           String compareLocalWord = _compare.conWord.getLocalWord();
           int ret;

           if (core.getPropertiesManager().isUseLocalWordLex()) {
               if (core.getPropertiesManager().isIgnoreCase()) {
                    ret = myLocalWord.toLowerCase().compareTo(compareLocalWord.toLowerCase());
               } else {
                    ret = myLocalWord.compareTo(compareLocalWord);
               }
           } else {
               ret = conWord.compareTo(_compare.conWord);
           }

           return ret;
       }
       
       @Override
       public boolean equals(Object o) {
           boolean ret = o != null;
           
           if (ret) {
               ret = o instanceof ConWordDisplay;
               
               if (ret) {
                    ConWord compWord = ((ConWordDisplay)o).conWord;
                    ret = compWord != null && compWord.getId() != null && conWord != null;
                   
                    if (ret && compWord != null) {
                        ret = conWord.getId().equals(compWord.getId());
                    }
               }
           }
           
           return ret;
       }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + Objects.hashCode(this.conWord);
            return hash;
        }
    }
}
