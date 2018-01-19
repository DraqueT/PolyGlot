/*
 * Copyright (c) 2014-2018, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
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
package PolyGlot.ManagersCollections;

import PolyGlot.Nodes.ConWord;
import PolyGlot.DictCore;
import PolyGlot.FormattedTextHelper;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.Nodes.DeclensionPair;
import PolyGlot.Nodes.DictNode;
import PolyGlot.Nodes.EtyExternalParent;
import PolyGlot.PGTUtil;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.RankedObject;
import PolyGlot.WebInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author draque
 *
 */
public class ConWordCollection extends DictionaryCollection {

    private final String splitChar = ",";
    private final DictCore core;
    private final Map<String, Integer> allConWords;
    private final Map<String, Integer> allLocalWords;
    private boolean orderByLocal = false;

    public ConWordCollection(DictCore _core) {
        bufferNode = new ConWord();
        ((ConWord) bufferNode).setCore(_core);
        allConWords = new HashMap<>();
        allLocalWords = new HashMap<>();
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

        ((ConWord) bufferNode).setParent(this);
        ((ConWord) bufferNode).setCore(core);
        ret = super.insert(_id, bufferNode);

        balanceWordCounts(insWord, true);

        bufferNode = new ConWord();
        ((ConWord) bufferNode).setCore(core);

        return ret;
    }

    /**
     * Gets all words that are illegal in some way
     *
     * @return an iterator full of all illegal conwords
     */
    public Iterator<ConWord> illegalFilter() {
        List<ConWord> retList = new ArrayList<>();

        for (Object object : nodeMap.values()) {
            ConWord curWord = (ConWord) object;

            if (!curWord.isWordLegal()) {
                retList.add(curWord);
            }
        }

        Collections.sort(retList);
        return retList.iterator();
    }

    /**
     * Checks whether word is legal and returns error reason if not
     *
     * @param word word to check legality of
     * @return Conword with any illegal entries saved as word values
     */
    public ConWord testWordLegality(ConWord word) {
        ConWord ret = new ConWord();

        if (word.getValue().length() == 0) {
            ret.setValue(core.conLabel() + " word value cannot be blank.");
        }

        if (word.getWordTypeId() == 0 && core.getPropertiesManager().isTypesMandatory()) {
            ret.typeError = "Types set to mandatory.";
        }

        if (word.getLocalWord().length() == 0 && core.getPropertiesManager().isLocalMandatory()) {
            ret.setLocalWord(core.localLabel() + " word set to mandatory.");
        }

        if (core.getPropertiesManager().isWordUniqueness() && core.getWordCollection().containsWord(word.getValue())) {
            ret.setValue(ret.getValue() + (ret.getValue().length() == 0 ? "" : "\n")
                    + core.conLabel() + " words set to enforced unique: this conword exists elsewhere.");
        }

        if (core.getPropertiesManager().isLocalUniqueness() && word.getLocalWord().length() != 0
                && core.getWordCollection().containsLocalMultiples(word.getLocalWord())) {
            ret.setLocalWord(ret.getLocalWord() + (ret.getLocalWord().length() == 0 ? "" : "\n")
                    + core.localLabel() + " words set to enforced unique: this local exists elsewhere.");
        }

        TypeNode wordType = core.getTypes().getNodeById(word.getWordTypeId());

        ret.setDefinition(ret.getDefinition() + (ret.getDefinition().length() == 0 ? "" : "\n")
                + core.getDeclensionManager().declensionRequirementsMet(word, wordType));

        if (wordType != null) {
            String typeRegex = wordType.getPattern();

            if (typeRegex.length() != 0 && !word.getValue().matches(typeRegex)) {
                ret.setDefinition(ret.getDefinition() + (ret.getDefinition().length() == 0 ? "" : "\n")
                        + "Word does not match enforced pattern for type: " + word.getWordTypeDisplay() + ".");
                ret.setProcOverride(true);
            }
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
    public Integer insert() throws Exception {
        Integer ret;

        ((ConWord) bufferNode).setParent(this);
        ((ConWord) bufferNode).setCore(core);
        ret = super.insert(bufferNode);

        balanceWordCounts((ConWord) bufferNode, true);

        bufferNode = new ConWord();
        ((ConWord) bufferNode).setCore(core);

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

    /**
     * Balances word counts when modifying word value or local word MUST BE RUN
     * BEFORE PERSISTING NEW VALUES TO WORD
     *
     * @param id id of word to modify
     * @param wordVal new conword value
     * @param wordLoc new local word value
     * @throws java.lang.Exception if word not found
     */
    public void extertalBalanceWordCounts(Integer id, String wordVal, String wordLoc) throws Exception {
        ConWord oldWord = getNodeById(id);
        ConWord newWord = new ConWord();

        newWord.setValue(wordVal);
        newWord.setLocalWord(wordLoc);

        balanceWordCounts(oldWord, false);
        balanceWordCounts(newWord, true);
    }

    /**
     * Tests whether a value exists in the dictionary currently
     *
     * @param word value to search for
     * @return true if exists, false otherwise
     */
    public boolean testWordValueExists(String word) {
        return allConWords.containsKey(word) && allConWords.get(word) > 0;
    }

    /**
     * Tests whether a value exists in the dictionary currently
     *
     * @param local value to search for
     * @return true if exists, false otherwise
     */
    public boolean testLocalValueExists(String local) {
        return allLocalWords.containsKey(local) && allLocalWords.get(local) > 0;
    }

    /**
     * Deletes word and balances all dependencies
     *
     * @param _id ID of word to delete
     * @throws Exception
     */
    @Override
    public void deleteNodeById(Integer _id) throws Exception {
        ConWord deleteWord = this.getNodeById(_id);

        balanceWordCounts(deleteWord, false);
        super.deleteNodeById(_id);
        core.getDeclensionManager().clearAllDeclensionsWord(_id);
    }

    @Override
    public void modifyNode(Integer _id, DictNode _modNode) throws Exception {
        // do bookkeepingfor word counts
        ConWord oldWord = getNodeById(_id);
        balanceWordCounts(oldWord, false);
        balanceWordCounts((ConWord) _modNode, true);
        ((ConWord) _modNode).setCore(core);

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
    protected Integer insert(Integer _id, DictNode _buffer) throws Exception {
        ((ConWord) _buffer).setCore(core);
        ((ConWord) _buffer).setParent(this);
        return super.insert(_id, _buffer);
    }

    /**
     * recalculates all non-overridden pronunciations
     *
     * @throws java.lang.Exception
     */
    public void recalcAllProcs() throws Exception {
        List<ConWord> words = this.getWordNodes();

        for (ConWord curWord : words) {
            // only runs if word's pronunciation not overridden
            if (!curWord.isProcOverride()) {
                curWord.setPronunciation(core.getPronunciationMgr().getPronunciation(curWord.getValue()));
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
        List<ConWord> localEquals = new ArrayList<>();
        List<ConWord> localContains = new ArrayList<>();
        List<RankedObject> definitionContains = new ArrayList<>();
        Iterator<Entry<Integer, ConWord>> allWords = nodeMap.entrySet().iterator();

        // on empty, return empty list
        if (_match.length() == 0) {
            return localEquals;
        }

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
                // local word equility is the highest ranking match
                localEquals.add(curWord);
            } else if (word.contains(compare)) {
                // local word contains value is the second highest ranking match
                localContains.add(curWord);
            } else if (definition.contains(compare)) {
                // definition contains is ranked third, and itself raked inernally
                // by match position
                definitionContains.add(new RankedObject(curWord, definition.indexOf(compare)));
            }
        }

        Collections.sort(definitionContains);

        // concatinate results
        ArrayList<ConWord> ret = new ArrayList<>();
        ret.addAll(localEquals);
        ret.addAll(localContains);

        // must add through iteration here
        Iterator<RankedObject> it = definitionContains.iterator();
        while (it.hasNext()) {
            RankedObject curObject = it.next();
            ConWord curDefMatch = (ConWord) curObject.getHolder();

            ret.add(curDefMatch);
        }

        return ret;
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
    public List<ConWord> filteredList(ConWord _filter) throws Exception {
        ConWordCollection retValues = new ConWordCollection(core);
        retValues.setAlphaOrder(alphaOrder);

        Iterator<Entry<Integer, ConWord>> filterList = nodeMap.entrySet()
                .iterator();
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

                    for (String def1 : _filter.getDefinition().split(splitChar)) {
                        if (definition.contains(def1)) {
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
                    boolean cont = true;

                    for (String loc1 : _filter.getLocalWord().split(splitChar)) {
                        if (local.contains(loc1)) {
                            cont = false;
                            break;
                        }
                    }
                    if (cont) {
                        continue;
                    }
                }

                // con word
                if (!_filter.getValue().trim().isEmpty()) {
                    boolean cont = true;

                    for (String val1 : _filter.getValue().split(splitChar)) {
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
                    boolean cont = true;

                    for (String proc1 : _filter.getPronunciation().split(splitChar)) {
                        if (proc.contains(proc1)) {
                            cont = false;
                        }
                    }

                    if (cont) {
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
                    } if (parent instanceof EtyExternalParent) {
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
                throw new Exception("FILTERING ERROR: " + e.getMessage());
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
                || head.startsWith(matchText)) {
            ret = true;
        }
        TypeNode type = core.getTypes().getNodeById(word.getWordTypeId());

        if (type != null && !ret) {
            int typeId = type.getId();
            Iterator<DeclensionPair> decIt = core.getDeclensionManager().getAllCombinedIds(typeId).iterator();

            while (!ret && decIt.hasNext()) {
                // silently skip erroring entries. Too cumbersone to deal with during a search
                try {
                    DeclensionPair curPair = decIt.next();
                    String declension = core.getDeclensionManager()
                            .declineWord(typeId, curPair.combinedId, word.getValue());

                    if (!declension.trim().isEmpty()
                            && (declension.matches(matchText)
                            || declension.startsWith(matchText))) {
                        ret = true;
                    }
                } catch (Exception e) {
                    // do nothing (see above comment)
                }
            }
        }

        return ret;
    }

    @Override
    public ConWord getNodeById(Integer _id) throws WordNotExistsException {
        try {
            return (ConWord) super.getNodeById(_id);
        } catch (NodeNotExistsException e) {
            throw new WordNotExistsException(e.getLocalizedMessage());
        }
    }

    /**
     * wipes current word buffer
     */
    @Override
    public void clear() {
        bufferNode = new ConWord();
        ((ConWord) bufferNode).setCore(core);
    }

    public ConWord getBufferWord() {
        return (ConWord) bufferNode;
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
    public List<ConWord> getWordNodes() {
        List<ConWord> retList = new ArrayList<>(nodeMap.values());

        Collections.sort(retList);

        return retList;
    }

    /**
     * gets and returns iterator of all words based on alphabetical order of
     * localwords on the entries. Respects default alpha order.
     *
     * @return
     */
    public Iterator<ConWord> getNodeIteratorLocalOrder() {
        List<ConWord> cycleList = new ArrayList<>(nodeMap.values());
        List<ConWord> retList = new ArrayList<>();

        // cycle through and create copies of words with multiple local values
        for (ConWord curWord : cycleList) {
            String localPre = curWord.getLocalWord();
            if (localPre.contains(",")) {
                String[] allLocals = localPre.split(",");

                // create new temp word for purposes of dictionary creation
                for (String curLocal : allLocals) {
                    ConWord ins = new ConWord();
                    ins.setCore(core);
                    ins.setEqual(curWord);
                    ins.setLocalWord(curLocal);
                    ins.setParent(this);

                    retList.add(ins);
                }
            } else {
                retList.add(curWord);
            }
        }

        orderByLocal = true;
        Collections.sort(retList);
        orderByLocal = false;

        return retList.iterator();
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
        String defaultFont = "face=\"" + core.getPropertiesManager().getCharisUnicodeFont().getFamily() + "\"";
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
        String defaultFont = "face=\"" + core.getPropertiesManager().getFontCon().getFamily() + "\"";
        return "<font " + defaultFont + ">" + toCon + "</font>";
    }

    /**
     * Writes all word information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        List<ConWord> wordLoop = getWordNodes();
        Element lexicon = doc.createElement(PGTUtil.lexiconXID);
        Element wordNode;
        Element wordValue;

        rootElement.appendChild(lexicon);
        
        for (ConWord curWord : wordLoop) {
            wordNode = doc.createElement(PGTUtil.wordXID);
            lexicon.appendChild(wordNode);

            wordValue = doc.createElement(PGTUtil.wordIdXID);
            Integer wordId = curWord.getId();
            wordValue.appendChild(doc.createTextNode(wordId.toString()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.localWordXID);
            wordValue.appendChild(doc.createTextNode(curWord.getLocalWord()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.conWordXID);
            wordValue.appendChild(doc.createTextNode(curWord.getValue()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordTypeIdXID);
            wordValue.appendChild(doc.createTextNode(curWord.getWordTypeId().toString()));
            wordNode.appendChild(wordValue);

            try {
                wordValue = doc.createElement(PGTUtil.wordProcXID);
                wordValue
                        .appendChild(doc.createTextNode(curWord.getPronunciation()));
                wordNode.appendChild(wordValue);
            } catch (Exception e) {
                // Do nothing. Users are made aware of this issue elsewhere.
            }

            wordValue = doc.createElement(PGTUtil.wordDefXID);
            wordValue.appendChild(doc.createTextNode(WebInterface.archiveHTML(curWord.getDefinition())));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordProcOverrideXID);
            wordValue.appendChild(doc.createTextNode(curWord.isProcOverride() ? PGTUtil.True : PGTUtil.False));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordAutoDeclenOverrideXID);
            wordValue.appendChild(doc.createTextNode(curWord.isOverrideAutoDeclen() ? PGTUtil.True : PGTUtil.False));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordRuleOverrideXID);
            wordValue.appendChild(doc.createTextNode(curWord.isRulesOverrride() ? PGTUtil.True : PGTUtil.False));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordClassCollectionXID);
            for (Entry<Integer, Integer> entry : curWord.getClassValues()) {
                Element classVal = doc.createElement(PGTUtil.wordClassAndValueXID);
                classVal.appendChild(doc.createTextNode(entry.getKey() + "," + entry.getValue()));
                wordValue.appendChild(classVal);
            }
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.wordClassTextValueCollectionXID);
            for (Entry<Integer, String> entry : curWord.getClassTextValues()) {
                Element classVal = doc.createElement(PGTUtil.wordClassTextValueXID);
                classVal.appendChild(doc.createTextNode(entry.getKey() + "," + entry.getValue()));
                wordValue.appendChild(classVal);
            }
            wordNode.appendChild(wordValue);
            
            wordValue = doc.createElement(PGTUtil.wordEtymologyNotesXID);
            wordValue.appendChild(doc.createTextNode(curWord.getEtymNotes()));
            wordNode.appendChild(wordValue);
        }
    }

    /**
     * Call this to wipe out the values of all deprecated
     * conjugations/declensions for a particular part of speech in the
     * dictionary
     *
     * @param typeId ID of word type to clear values from
     */
    public void clearDeprecatedDeclensions(Integer typeId) {
        DeclensionManager dm = core.getDeclensionManager();
        Map<Integer, List<DeclensionPair>> comTypeDecs = new HashMap();

        // iterates over every word
        for (Object curNode : nodeMap.values()) {
            ConWord curWord = (ConWord) curNode;
            List<DeclensionPair> curDeclensions;

            // skip words not of given type
            if (!curWord.getWordTypeId().equals(typeId)) {
                continue;
            }

            // ensure I'm only generating decelnsion patterns for any given part of speech only once
            if (comTypeDecs.containsKey(curWord.getWordTypeId())) {
                curDeclensions = comTypeDecs.get(curWord.getWordTypeId());
            } else {
                curDeclensions = dm.getAllCombinedIds(curWord.getWordTypeId());
                comTypeDecs.put(curWord.getWordTypeId(), curDeclensions);
            }

            // retrieves all stored declension values for word
            Map<String, DeclensionNode> decMap = dm.getWordDeclensions(curWord.getId());

            // removes all legitimate declensions from map
            for (DeclensionPair curPair : curDeclensions) {
                decMap.remove(curPair.combinedId);
            }

            // wipe remaining values from word
            dm.removeDeclensionValues(curWord.getId(), decMap.values());
        }
    }

    public class WordNotExistsException extends NodeNotExistsException {
        public WordNotExistsException(String message) {
            super(message);
        }
    }
}
