/*
 * Copyright (c) 2016, Draque
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
package PolyGlot.ManagersCollections;

import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.PEntry;
import PolyGlot.Nodes.WordPropValueNode;
import PolyGlot.Nodes.WordProperty;
import PolyGlot.PGTUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Draque
 */
public class WordPropertyCollection extends DictionaryCollection {

    private List<List<PEntry<Integer, Integer>>> comboCache = null;

    public WordPropertyCollection() {
        bufferNode = new WordProperty();
    }

    public List<WordProperty> getAllWordProperties() {
        List<WordProperty> retList = new ArrayList<>(nodeMap.values());

        Collections.sort(retList);

        return retList;
    }

    @Override
    public void clear() {
        bufferNode = new WordProperty();
    }

    /**
     * Inserts and blanks current buffer node
     *
     * @return inserted Id
     * @throws java.lang.Exception
     */
    public int insert() throws Exception {
        int ret;

        if (bufferNode.getId() > 0) {
            ret = this.insert(bufferNode.getId(), bufferNode);
        } else {
            ret = super.insert(bufferNode);
        }

        bufferNode = new WordProperty();
        return ret;
    }

    public List<WordProperty> getClassProps(int classId) {
        List<WordProperty> ret = new ArrayList<>();

        for (WordProperty curProp : (ArrayList<WordProperty>) new ArrayList<>(nodeMap.values())) {
            if (curProp.appliesToType(classId)
                    || curProp.appliesToType(-1)) { // -1 is class "all"
                ret.add(curProp);
            }
        }

        Collections.sort(ret);
        return ret;
    }

    /**
     * Writes all word properties information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        // element containing all properties
        Element wordProperties = doc.createElement(PGTUtil.ClassesNodeXID);

        // creates each property
        for (WordProperty wordProp : (Collection<WordProperty>) nodeMap.values()) {
            // property element
            Element propElement = doc.createElement(PGTUtil.ClassXID);

            // ID element
            Element propProp = doc.createElement(PGTUtil.ClassIdXID);
            propProp.appendChild(doc.createTextNode(wordProp.getId().toString()));
            propElement.appendChild(propProp);

            // Name element
            propProp = doc.createElement(PGTUtil.ClassNameXID);
            propProp.appendChild(doc.createTextNode(wordProp.getValue()));
            propElement.appendChild(propProp);
            
            // Is Text Override
            propProp = doc.createElement(PGTUtil.ClassIsFreetextXID);
            propProp.appendChild(doc.createTextNode(wordProp.isFreeText() ? PGTUtil.True : PGTUtil.False));
            propElement.appendChild(propProp);

            // generates element with all type IDs of types this property applies to
            String applyTypes = "";
            for (Integer typeId : wordProp.getApplyTypes()) {
                if (!applyTypes.equals("")) {
                    applyTypes += ",";
                }

                applyTypes += typeId.toString();
            }
            propProp = doc.createElement(PGTUtil.ClassApplyTypesXID);
            propProp.appendChild(doc.createTextNode(applyTypes));
            propElement.appendChild(propProp);

            // element for collection of values of property
            propProp = doc.createElement(PGTUtil.ClassValuesCollectionXID);
            for (WordPropValueNode curValue : wordProp.getValues()) {
                Element valueNode = doc.createElement(PGTUtil.ClassValueNodeXID);

                Element value = doc.createElement(PGTUtil.ClassValueIdXID);
                value.appendChild(doc.createTextNode(curValue.getId().toString()));
                valueNode.appendChild(value);

                // value string
                value = doc.createElement(PGTUtil.ClassValueNameXID);
                value.appendChild(doc.createTextNode(curValue.getValue()));
                valueNode.appendChild(value);

                propProp.appendChild(valueNode);
            }
            propElement.appendChild(propProp);

            wordProperties.appendChild(propElement);
        }

        rootElement.appendChild(wordProperties);
    }

    /**
     * Gets random assortment of word class combinations based. Number of
     * combinations limited by parameters and by number of combinations
     * available
     *
     * @param numRandom number of entries to return
     * @return randomly generated combinations of word classes
     */
    public List<List<PEntry<Integer, Integer>>> getRandomPropertyCombinations(int numRandom) {
        return getRandomPropertyCombinations(numRandom, null);
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

        if (comboCache != null && comboCache.size() > 0) {
            for (int i = 0; (i - offset) < numRandom && i + offset < comboCache.size(); i++) {
                if (propCombEqual(comboCache.get(i + offset), new ArrayList(excludeWord.getClassValues()))) {
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
     * builds cache of every word property combination
     */
    public void buildComboCache() {
        comboCache = new ArrayList<>();

        if (!nodeMap.isEmpty()) {
            buildComboCacheInternal(0, new ArrayList(nodeMap.values()),
                    new ArrayList<PEntry<Integer, Integer>>());
        }
    }

    private void buildComboCacheInternal(int depth, List<WordProperty> props, List<PEntry<Integer, Integer>> curList) {
        WordProperty curProp = props.get(depth);

        for (WordPropValueNode curVal : curProp.getValues()) {
            ArrayList<PEntry<Integer, Integer>> newList = new ArrayList(curList);
            newList.add(new PEntry(curProp.getId(), curVal.getId()));

            // if at max depth, cease recursion
            if (depth == props.size() - 1) {
                comboCache.add(newList);
            } else {
                buildComboCacheInternal(depth + 1, props, newList);
            }
        }
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

        if (!nodeMap.containsKey(classId)) {
            ret = false;
        } else {
            WordProperty prop = (WordProperty) nodeMap.get(classId);
            if (!prop.isValid(valId)) {
                ret = false;
            }
        }

        return ret;
    }
}
