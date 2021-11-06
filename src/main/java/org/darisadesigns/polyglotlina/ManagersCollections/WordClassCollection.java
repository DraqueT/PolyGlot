/*
 * Copyright (c) 2016-2021, Draque Thompson
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.PEntry;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Contains all word classes and what parts of speech they may apply to
 * @author Draque
 */
public class WordClassCollection extends DictionaryCollection<WordClass> {

    private List<List<PEntry<Integer, Integer>>> comboCache = null;
    private final DictCore core;

    public WordClassCollection(DictCore _core) {
        super(new WordClass());
        core = _core;
    }

    public WordClass[] getAllWordClasses() {
        List<WordClass> retList = new ArrayList<>(nodeMap.values());

        Collections.sort(retList);

        return retList.toArray(new WordClass[0]);
    }

    @Override
    public void clear() {
        bufferNode = new WordClass();
    }

    /**
     * Inserts and blanks current buffer node
     *
     * @return inserted Id
     * @throws java.lang.Exception
     */
    @Override
    public Integer insert() throws Exception {
        int ret;

        if (bufferNode.getId() > 0) {
            ret = this.insert(bufferNode.getId(), bufferNode);
        } else {
            ret = super.insert(bufferNode);
        }

        bufferNode = new WordClass();
        return ret;
    }

    public WordClass[] getClassesForType(int classId) {
        List<WordClass> ret = new ArrayList<>();

        nodeMap.values().forEach((prop) -> {
            if (prop.appliesToType(classId)
                    || prop.appliesToType(-1)) { // -1 is class "all"
                ret.add(prop);
            }
        });

        // alternative way to do it
//        nodeMap.values().stream()
//                .filter((prop) -> prop.appliesToType(classId) || prop.appliesToType(-1))
//                .forEach((prop) -> ret.add(prop));

        Collections.sort(ret);
        return ret.toArray(new WordClass[0]);
    }

    /**
     * Writes all word class information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        // element containing all classes
        Element wordClasses = doc.createElement(PGTUtil.CLASSES_NODE_XID);

        // creates each class
        nodeMap.values().forEach((curClass) -> {
            curClass.writeXML(doc, wordClasses);
        });

        rootElement.appendChild(wordClasses);
    }

    /**
     * Gets random assortment of word class combinations based. Number of
     * combinations limited by parameters and by number of combinations
     * available. a value can be excluded
     *
     * @param numRandom number of entries to return
     * @param excludeWord word with class properties to exclude (quiz generation
     * purposes)
     * @return randomly generated combinations of word classes
     */
    public List<List<PEntry<Integer, Integer>>> getRandomPropertyCombinations(int numRandom, ConWord excludeWord) {
        // combocache should generally be pre-built before something like this is done, but cover the contingency
        if (comboCache == null)
            buildComboCache();

        Collections.shuffle(comboCache, new Random(System.nanoTime()));

        if (comboCache == null || comboCache.isEmpty())
            return new ArrayList<>();

        List<List<PEntry<Integer, Integer>>> ret = new ArrayList<>();
        int offset = 0;

        for (int i = 0; (i - offset) < numRandom && i + offset < comboCache.size(); i++) {
            if (propCombEqual(comboCache.get(i + offset), new ArrayList<>(excludeWord.getClassValues()))) {
                offset++;
                continue;
            }
            ret.add(comboCache.get(i + offset));
        }

        return ret;
    }

    private boolean propCombEqual(List<PEntry<Integer, Integer>> a, List<Entry<Integer, Integer>> b) {
        if (a.size() != b.size())
            return false;

        for (Entry aEntry : a) {
            boolean result = false;

            for (Entry bEntry : b) {
                if (aEntry.equals(bEntry)) {
                    result = true;
                    break;
                }
            }
            if (!result)
                return false;

            // alternative way using a for + stream
//            boolean result = b.stream().anyMatch((bEntry) -> aEntry.equals(bEntry));
//            if (!result)
//                return false;
        }
        return true;

        // one more way using 2 streams
//        return a.stream().allMatch(
//                (aEntry) -> b.stream().anyMatch(
//                        (bEntry) -> aEntry.equals(bEntry)
//                ));
    }

    /**
     * builds cache of every word class combination
     */
    public void buildComboCache() {
        comboCache = new ArrayList<>();

        if (!nodeMap.isEmpty()) {
            buildComboCacheInternal(0, new ArrayList<>(nodeMap.values()),
                    new ArrayList<>());
        }
    }

    private void buildComboCacheInternal(int depth, List<WordClass> props, List<PEntry<Integer, Integer>> curList) {
        WordClass curProp = props.get(depth);

        curProp.getValues().forEach((curVal) -> {
            List<PEntry<Integer, Integer>> newList = new ArrayList<>(curList);
            newList.add(new PEntry<>(curProp.getId(), curVal.getId()));

            // if at max depth, cease recursion
            if (depth == props.size() - 1) {
                comboCache.add(newList);
            } else {
                buildComboCacheInternal(depth + 1, props, newList);
            }
        });
    }

    /**
     * Call this after done with any functionality that uses the combo cache.
     * This must be cleared manually, as there is no predictive way to know that
     * the cache is finished with
     */
    public void clearComboCache() {
        comboCache = null;
    }

    /**
     * returns true if the class/value ids given match up to existing values
     * returns false otherwise
     *
     * @param classId ID of word class to test
     * @param valId ID of value within word class to test
     * @return true if pair exists
     */
    public boolean isValid(Integer classId, Integer valId) {
        boolean ret = true;

        if (nodeMap.containsKey(classId)) {
            WordClass wordClass = nodeMap.get(classId);
            
            if (wordClass.isAssociative()) {
                // This will revert to an unknown value if unable to be found
                ret = true;
            }else if (!wordClass.isValid(valId)) {
                ret = false;
            }
        } else {
            ret = false;
        }

        return false;
    }
    
    /**
     * if a value is deleted from a class, this must be called. It tells the lexicon collection to cycle through all 
     * words and eliminate instances where the given class/value combo appear
     * @param classId class from which value was deleted
     * @param valueId value deleted
     */
    public void classValueDeleted(int classId, int valueId) {
        core.getWordCollection().classValueDeleted(classId, valueId);
    }

    @Override
    public WordClass notFoundNode() {
        WordClass emptyClass = new WordClass();
        emptyClass.setValue("CLASS NOT FOUND");
        return emptyClass;
    }
    
    @Override
    public boolean equals(Object comp) {
        if (this == comp)
            return true;

        if (comp instanceof WordClassCollection compCol)
            return ((comboCache == null && compCol.comboCache == null) || comboCache.equals(compCol.comboCache))
                    && super.equals(comp);

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.comboCache);
        return hash;
    }
}
