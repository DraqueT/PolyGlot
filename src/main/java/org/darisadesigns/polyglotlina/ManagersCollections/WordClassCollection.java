/*
 * Copyright (c) 2016-2020, Draque Thompson
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
        List<List<PEntry<Integer, Integer>>> ret = new ArrayList<>();
        int offset = 0;

        Collections.shuffle(comboCache, new Random(System.nanoTime()));

        if (comboCache != null && !comboCache.isEmpty()) {
            for (int i = 0; (i - offset) < numRandom && i + offset < comboCache.size(); i++) {
                if (propCombEqual(comboCache.get(i + offset), new ArrayList<>(excludeWord.getClassValues()))) {
                    offset++;
                    continue;
                }

                ret.add(comboCache.get(i + offset));
            }
        }

        return ret;
    }

    private boolean propCombEqual(List<PEntry<Integer, Integer>> a, List<Entry<Integer, Integer>> b) {
        boolean ret = true;

        if (a.size() == b.size()) {
            for (Entry aEntry : a) {
                boolean aRet = false;

                for (Entry bEntry : b) {
                    if (aEntry.equals(bEntry)) {
                        aRet = true;
                        break;
                    }
                }

                ret = ret && aRet;
            }
        } else {
            ret = false;
        }

        return ret;
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
            if (!nodeMap.get(classId).isValid(valId)) {
                ret = false;
            }
        } else {
            ret = false;
        }

        return ret;
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
    public Object notFoundNode() {
        WordClass emptyClass = new WordClass();
        emptyClass.setValue("CLASS NOT FOUND");
        return emptyClass;
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        /*
        DictionaryCollection<WordClass> {
        private List<List<PEntry<Integer, Integer>>> comboCache = null;
        */
        
        if (this == comp) {
            ret = true;
        } else if (comp instanceof WordClassCollection) {
            WordClassCollection compCol = (WordClassCollection)comp;
            ret = ((comboCache == null && compCol.comboCache == null) || comboCache.equals(compCol.comboCache));
            ret = ret && super.equals(comp);
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.comboCache);
        return hash;
    }
}
