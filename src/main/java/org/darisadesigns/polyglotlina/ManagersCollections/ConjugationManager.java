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

import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.ConjugationDimension;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConjugationGenRule;
import org.darisadesigns.polyglotlina.Nodes.ConjugationGenTransform;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.darisadesigns.polyglotlina.Nodes.EvolutionPair;
import org.darisadesigns.polyglotlina.Nodes.EvolutionPair.EvolutionType;
import org.darisadesigns.polyglotlina.RegexTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class ConjugationManager {

    private final DictCore core;
    private final List<String> decGenDebug = new ArrayList<>();
    private Integer topId = 0;
    private boolean bufferDecTemp = false;
    private Integer bufferRelId = -1;
    private ConjugationNode buffer = new ConjugationNode(-1);
    private final Map<Integer, List<ConjugationGenRule>> generationRules = new HashMap<>();
    private ConjugationGenRule ruleBuffer = new ConjugationGenRule();
    
    // Integer is ID of related word, list is list of declension nodes
    private final Map<Integer, List<ConjugationNode>> dList = new HashMap<>();

    // Integer is ID of related PoS, list is list of declensions for this PoS
    private final Map<Integer, List<ConjugationNode>> dTemplates = new HashMap<>();

    // If specific combined declensions require additional settings in the future,
    // change the boolean here to an object which will store them
    private final Map<String, Boolean> combSettings = new HashMap<>();

    public ConjugationManager(DictCore _core) {
        core = _core;
    }
    
    /**
     * Applies evolution transforms to all recorded forms of a given word
     * @param wordId
     * @param regex
     * @param replacement 
     * @param instanceOption 
     * @return  
     */
    public EvolutionPair[] evolveConjugatedWordForms(int wordId, 
            String regex, 
            String replacement, 
            RegexTools.ReplaceOptions instanceOption) {
        List<EvolutionPair> ret = new ArrayList<>();
        if (dList.containsKey(wordId)) {
            for (ConjugationNode curNode : dList.get(wordId)) {
                String startValue = curNode.getValue();
                try {
                    curNode.evolveConjugatedNode(regex, replacement, instanceOption);
                    
                    // only report error if prior value did not start out as blank
                    if (curNode.getValue().isBlank() && !startValue.isBlank()) {
                        throw new Exception("Conjugation set to blank value.");
                    }
                    
                    ret.add(new EvolutionPair(startValue, 
                            curNode.getValue(), 
                            EvolutionType.savedConjugation,
                            "",
                            "Evolved Wordform"
                    ));
                } catch (Exception e) {
                    
                }
            }
        }
        
        return ret.toArray(new EvolutionPair[0]);
    }
    
    /**
     * Applies language evolution rules to conjugation rules based on a part of 
     * speech filter. If the filter is set to 0, it is not used, and the evolutions
     * are applied to all entries. The evolutions are applied to both the regex and
     * the replacement text of the rules' transformations.
     * @param posFilter
     * @param regex
     * @param replacement
     * @return Array of EvolutionPair values representing results (including errors)
     */
    public EvolutionPair[] evolveConjugationRules(int posFilter, String regex, String replacement) {
        List<EvolutionPair> ret = new ArrayList<>();
        if (posFilter > 0) {
            if (generationRules.containsKey(posFilter)) {
                ret = this.evolveSingleRuleList(generationRules.get(posFilter), regex, replacement);
            }
        } else {
            for (List<ConjugationGenRule> ruleList : generationRules.values()) {
                ret.addAll(this.evolveSingleRuleList(ruleList, regex, replacement));
            }
        }
        
        return ret.toArray(new EvolutionPair[0]);
    }
    
    /**
     * Applies language evolution rules to conjugation rules based on a part of 
     * speech filter. If the filter is set to 0, it is not used, and the evolutions
     * are applied to all entries. The evolutions are applied to both the regex and
     * the replacement text of the rules' transformations.
     * @param ruleList
     * @param regex
     * @param replacement
     * @return List of EvolutionPair values representing results (including errors)
     */
    private List<EvolutionPair> evolveSingleRuleList(List<ConjugationGenRule> ruleList, String regex, String replacement) {
        List<EvolutionPair> ret = new ArrayList<>();
        
        for (ConjugationGenRule rule : ruleList) {
            for (ConjugationGenTransform transform : rule.getTransforms()) {
                String originalRegex = transform.regex;
                String originalReplacement = transform.replaceText;

                try {
                    transform.regex = transform.regex.replace(regex, replacement);
                    transform.replaceText = transform.replaceText.replace(regex, replacement);
                    
                    // Do NOT check the replaceText for being blank. There can be legit reasons for this.
                    if (transform.regex.isBlank()) {
                        throw new Exception("regex blanked");
                    }
                    
                    // only record if changes actually made
                    if (!originalRegex.equals(transform.regex) 
                            || !originalReplacement.equals(transform.replaceText)) {
                        ret.add(new EvolutionPair(originalRegex + "->" + originalReplacement, 
                                transform.regex + "->" + transform.replaceText, 
                                EvolutionType.savedConjugation, 
                                "",
                                rule.getName()
                        ));
                    }
                } catch (Exception e) {
                    // revert on error
                    String regexError = transform.regex;
                    String replacementError = transform.replaceText;
                    transform.regex = originalRegex;
                    transform.replaceText = originalReplacement;
                    
                    ret.add(new EvolutionPair(originalRegex + "->" + originalReplacement, 
                            regexError + "->" + replacementError, 
                            EvolutionType.savedConjugation, 
                            e.getLocalizedMessage() + "(value reverted to original)", 
                            rule.getName()
                    ));
                }
            }
        }
        
        return ret;
    }
    
    public boolean isCombinedConjlSurpressed(String _combId, Integer _typeId) {
        String storeId = _typeId + "," + _combId;

        if (!combSettings.containsKey(storeId)) {
            return false;
        }

        return combSettings.get(storeId);
    }

    public void setCombinedConjSuppressed(String _combId, Integer _typeId, boolean _suppress) {
        String storeId = _typeId + "," + _combId;

        if (combSettings.containsKey(storeId)) {
            combSettings.replace(storeId, _suppress);
        } else {
            combSettings.put(storeId, _suppress);
        }
    }

    /**
     * This sets the suppression data raw. Should only be used when loading from
     * a file
     *
     * @param _completeId complete, raw ID of data
     * @param _suppress suppression value
     */
    public void setCombinedConjugationSuppressedRaw(String _completeId, boolean _suppress) {
        combSettings.put(_completeId, _suppress);
    }

    /**
     * Gets list of all deprecated autogeneration rules
     *
     * @param typeId type to get deprecated values for
     * @return list of all deprecated gen rules
     */
    public ConjugationGenRule[] getAllDepGenerationRules(int typeId) {
        List<ConjugationGenRule> ret = new ArrayList<>();
        ConjugationPair[] typeRules = getAllCombinedIds(typeId);
        Map<String, Integer> ruleMap = new HashMap<>();

        // creates searchable map of extant combination IDs
        for (ConjugationPair curPair : typeRules) {
            ruleMap.put(curPair.combinedId, 0);
        }

        int highestIndex = 0;
        for (List<ConjugationGenRule> list : generationRules.values()) {
            for (ConjugationGenRule curRule : list) {
                int curRuleIndex = curRule.getIndex();
                highestIndex = Math.max(highestIndex, curRuleIndex);
            }
        }

        for (List<ConjugationGenRule> list : generationRules.values()) {
            for (ConjugationGenRule curRule : list) {
                // adds to return value only if rule matches ID but is orphaned
                if (curRule.getIndex() == -1) {
                    highestIndex++;
                    curRule.setIndex(highestIndex);
                }

                if (curRule.getTypeId() == typeId
                        && !ruleMap.containsKey(curRule.getCombinationId())) {
                    ret.add(curRule);
                }
            }
        }
        
        Collections.sort(ret);

        return ret.toArray(new ConjugationGenRule[0]);
    }

    /**
     * Gets current declension rule buffer
     *
     * @return current declension rule buffer
     */
    public ConjugationGenRule getRuleBuffer() {
        return ruleBuffer;
    }

    /**
     * inserts current rule buffer and sets to blank value
     */
    public void insRuleBuffer() {
        addConjugationGenRule(ruleBuffer);
        ruleBuffer = new ConjugationGenRule();
    }

    /**
     * add a declension generation rule to the list
     *
     * @param newRule rule to add
     */
    public void addConjugationGenRule(ConjugationGenRule newRule) {
        int typeId = newRule.getTypeId();
        List<ConjugationGenRule> rules;

        if (generationRules.containsKey(typeId)) {
            rules = generationRules.get(typeId);
        } else {
            rules = new ArrayList<>();
            generationRules.put(typeId, rules);
        }
        
        // only set index if not already set to value
        if (newRule.getIndex() == -1) {
            // give rule the next available index (0 if no current rules, last rule index + 1 otherwise)
            if (rules.isEmpty()) {
                newRule.setIndex(0);
            } else {
                Collections.sort(rules);
                newRule.setIndex(rules.get(rules.size() - 1).getIndex() + 1);
            }
        }

        rules.add(newRule);
    }

    /**
     * delete all rules of a particular typeID from rule set
     *
     * @param typeId ID of type to wipe
     */
    public void wipeConjugationGenRules(int typeId) {
        generationRules.remove(typeId);
    }

    /**
     * Ensures all rules have contiguous indices. Run prior to generating XML for save
     */
    private void smoothRules() {
        generationRules.values().forEach((ruleList) -> {
            Collections.sort(ruleList);
            int newIndex = 1;
            
            for (ConjugationGenRule rule : ruleList) {
                rule.setIndex(newIndex);
                
                newIndex++;
            }
        });
    }
    
    /**
     * Deletes rule based on unique regex value
     *
     * @param delRule rule to delete
     */
    public void deleteConjugationGenRule(ConjugationGenRule delRule) {
        int typeId = delRule.getTypeId();

        if (generationRules.containsKey(typeId)) {
            generationRules.get(typeId).remove(delRule);
        }
    }

    /**
     * Deletes all ConjugationGenRule entries for a given POS/declension pairing.
     *
     * @param typeId
     * @param combinedId
     */
    public void deleteConjugationGenRules(int typeId, String combinedId) {
        if (generationRules.containsKey(typeId)) {
            List<ConjugationGenRule> rules = generationRules.get(typeId);
            List<ConjugationGenRule> iter = new ArrayList<>(rules);
            
            // iterate on copy of array to avoid concurrent modification
            for (ConjugationGenRule rule : iter) {
                if (rule.getCombinationId().equals(combinedId)) {
                    rules.remove(rule);
                }
            }
        }
    }
    
     /**
     * get list of all declension rules for a particular type/combined declension id
     *
     * @param typeId id of part of speech to collect all rules for (does not
     * account for class filtering)
     * @param combinedId the combinedId for the rules to fetch
     * @return list of rules
     */
    public ConjugationGenRule[] getConjugationRulesForConjugation(int typeId, String combinedId) {
        List<ConjugationGenRule> ret = new ArrayList<>();
        
        for (ConjugationGenRule curRule : getConjugationRulesForType(typeId)) {
            if (curRule.getCombinationId().equals(combinedId)) {
                ret.add(curRule);
            }
        }
        
        Collections.sort(ret);
        
        return ret.toArray(new ConjugationGenRule[0]);
    }

    /**
     * get list of all declension rules for a particular type
     *
     * @param typeId id of part of speech to collect all rules for (does not
     * account for class filtering)
     * @return list of rules
     */
    public ConjugationGenRule[] getConjugationRulesForType(int typeId) {
        List<ConjugationGenRule> ret;

        if (generationRules.containsKey(typeId)) {
            ret = generationRules.get(typeId);
        } else {
            ret = new ArrayList<>();
        }

        Collections.sort(ret);

        return ret.toArray(new ConjugationGenRule[0]);
    }
    
    /**
     * get list of all declension rules for a particular type
     *
     * @param typeId id of part of speech to collect all rules for (does not
     * @param combinedId combined ID of rules to select
     * account for class filtering)
     * @return list of rules
     */
    public ConjugationGenRule[] getConjugationRulesForTypeAndCombId(int typeId, String combinedId) {
        List<ConjugationGenRule> ret = new ArrayList<>();
        ConjugationGenRule[] typeRules = getConjugationRulesForType(typeId);
        
        for (ConjugationGenRule rule : typeRules) {
            if (rule.getCombinationId().equals(combinedId)) {
                ret.add(rule);
            }
        }
        
        Collections.sort(ret);
        
        return ret.toArray(new ConjugationGenRule[0]);
    }

    /**
     * get list of all declension rules for a given word based on word type and
     * word class values
     *
     * @param word word to get rules for (takes into account word type (PoS) &
     * classes/class values it has
     * @return list of rules
     */
    public ConjugationGenRule[] getConjugationRules(ConWord word) {
        List<ConjugationGenRule> ret = new ArrayList<>();
        int typeId = word.getWordTypeId();
        
        if (generationRules.containsKey(typeId)) {
            List<ConjugationGenRule> decRules = generationRules.get(word.getWordTypeId());

            for (ConjugationGenRule curRule : decRules) {
                ret.add(curRule);
            }
        }

        Collections.sort(ret);

        // ensure that all rules cave contiguous IDs before returning
        int i = 1;
        for (ConjugationGenRule curRule : ret) {
            curRule.setIndex(i);
            i++;
        }

        return ret.toArray(new ConjugationGenRule[0]);
    }

    /**
     * Generates the new form of a declined/conjugated word based on rules for
     * its type
     *
     * @param word to transform
     * @param combinedId combined ID of word form to create
     * @return new word value if exists, empty string otherwise
     * @throws java.lang.Exception on bad regex
     */
    public String declineWord(ConWord word, String combinedId) throws Exception {
        ConjugationGenRule[] rules = getConjugationRules(word);
        decGenDebug.clear();
        decGenDebug.add("APPLIED RULES BREAKDOWN:\n");
        String ret = word.getValue();

        for (ConjugationGenRule curRule : rules) {
            boolean ruleAppliesCombId = curRule.getCombinationId().equals(combinedId);
            boolean ruleAppliesToWord = curRule.doesRuleApplyToWord(word);
            
            String debugString = "--------------------------------------\n";
            
            // skip all entries not applicable to this particular combined word ID
            if (!ruleAppliesCombId) {
                continue;
            } else if (!ruleAppliesToWord) {
                debugString += curRule.getDebugString();
                decGenDebug.add(debugString);
                continue;
            }
            
            debugString += curRule.getDebugString();

            ConjugationGenTransform[] transforms = curRule.getTransforms();

            for (ConjugationGenTransform curTrans : transforms) {
                try {
                    String orig = ret;
                    ret = ret.replaceAll(curTrans.regex, curTrans.replaceText);
                    debugString += "    -------------------------\n"
                            + "    Transformation:\n"
                            + "        Regex: \"" + curTrans.regex + "\"\n"
                            + "        Text: \"" + curTrans.replaceText + "\"\n" 
                            + "        Effect: " + orig + " -> " + ret + "\n";
                } catch (Exception e) {
                    throw new Exception("Unable to create declension/conjugation "
                            + "due to malformed regex (modify in Parts of Speech->Autogeneration): "
                            + e.getLocalizedMessage(), e);
                }
            }
            
            decGenDebug.add(debugString);
        }

        // if rules are empty, no transformation took place: return blank string
        ret = rules.length == 0 ? "" : ret;
        
        return ret;
    }

    public void addConjugationToWord(Integer wordId, Integer declensionId, ConjugationNode declension) {
        ConjugationManager.this.addConjugation(wordId, declensionId, declension, dList);
    }

    public void deleteConjugationFromWord(Integer wordId, Integer declensionId) {
        deleteConjugation(wordId, declensionId, dList);
    }

    /**
     * sets all declensions to deprecated state
     *
     * @param typeId ID of type to deprecate declensions for
     */
    public void deprecateAllConjugations(Integer typeId) {
        Iterator<Entry<Integer, List<ConjugationNode>>> decIt = dList.entrySet().iterator();

        while (decIt.hasNext()) {
            Entry<Integer, List<ConjugationNode>> curEntry = decIt.next();
            List<ConjugationNode> curList = curEntry.getValue();

            // only run for declensions of words with particular type
            if (!core.getWordCollection().getNodeById(curEntry.getKey()).getWordTypeId().equals(typeId)) {
                continue;
            }

            Iterator<ConjugationNode> nodeIt = curList.iterator();

            while (nodeIt.hasNext()) {
                ConjugationNode curNode = nodeIt.next();

                curNode.setCombinedDimId("D" + curNode.getCombinedDimId());
            }
        }
    }

    /**
     * Gets a particular declension template of a particular word type
     *
     * @param typeId the type which contains the declension in question
     * @param declensionId the declension within the type to retrieve
     * @return the object representing the declension
     */
    public ConjugationNode getConjugation(Integer typeId, Integer declensionId) {
        ConjugationNode ret = null;
        List<ConjugationNode> decList = dTemplates.get(typeId);

        // only search farther if declension itself actually exists
        if (decList != null) {
            Iterator<ConjugationNode> decIt = decList.iterator();

            while (decIt.hasNext()) {
                ConjugationNode curNode = decIt.next();

                if (curNode.getId().equals(declensionId)) {
                    ret = curNode;
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * Clears all declensions from word
     *
     * @param wordId ID of word to clear of all declensions
     */
    public void clearAllConjugationsWord(Integer wordId) {
        clearAllConjugations(wordId, dList);
    }

    /**
     * get list of all labels and combined IDs of all declension combinations
     * for a type
     *
     * @param typeId ID of type to fetch combined IDs for
     * @return list of labels and IDs
     */
    public ConjugationPair[] getAllCombinedIds(Integer typeId) {
        ConjugationNode[] dimensionalConjugationNodes = getDimensionalConjugationListTemplate(typeId);
        List<ConjugationNode> singletonConjugationNodes = ConjugationManager.this.getSingletonConjugationList(typeId, dTemplates);
        List<ConjugationPair> ret = getAllCombinedDimensionalIds(0, ",", "", dimensionalConjugationNodes);
        ret.addAll(Arrays.asList(getAllSingletonIds(singletonConjugationNodes)));

        return ret.toArray(new ConjugationPair[0]);
    }

    /**
     * get list of all labels and combined IDs of dimensional declension
     * combinations for a type
     *
     * @param typeId ID of type to fetch combined IDs for
     * @return list of labels and IDs
     */
    public ConjugationPair[] getDimensionalCombinedIds(Integer typeId) {
        ConjugationNode[] dimensionalConjugationNodes = getDimensionalConjugationListTemplate(typeId);
        return getAllCombinedDimensionalIds(0, ",", "", dimensionalConjugationNodes).toArray(new ConjugationPair[0]);
    }

    /**
     * get list of all labels and combined IDs of singleton declension
     * combinations for a type
     *
     * @param typeId ID of type to fetch combined IDs for
     * @return list of labels and IDs
     */
    public ConjugationPair[] getSingletonCombinedIds(Integer typeId) {
        List<ConjugationNode> singletonConjugationNodes = ConjugationManager.this.getSingletonConjugationList(typeId, dTemplates);
        return getAllSingletonIds(singletonConjugationNodes);
    }

    public ConjugationPair[] getAllSingletonIds(List<ConjugationNode> declensionList) {
        List<ConjugationPair> ret = new ArrayList<>();

        declensionList.forEach((curNode) -> {
            ConjugationPair curPair = new ConjugationPair(curNode.getCombinedDimId(), curNode.getValue());
            ret.add(curPair);
        });

        return ret.toArray(new ConjugationPair[0]);
    }

    /**
     * Gets the location of a dimension's id in a dimensionalID string
     *
     * @param typeId part of speech associated with dimension
     * @param node dimension to find
     * @return locational index within dimensional ids (-1 if no value found)
     */
    public int getDimensionTemplateIndex(int typeId, ConjugationNode node) {
        int ret = -1;

        if (dTemplates.containsKey(typeId)) {
            List<ConjugationNode> declensionValues = dTemplates.get(typeId);

            ret = declensionValues.indexOf(node);

            // must loop through due to inclusion of singleton declensions here
            int numSingleton = 0;
            for (ConjugationNode testNode : declensionValues) {
                if (testNode.isDimensionless()) {
                    numSingleton++;
                }
                if (node.getId().equals(testNode.getId())) {
                    break;
                }
            }

            ret -= numSingleton;
        }

        return ret;
    }

    /**
     * Same as above, but SKIPS indices of singleton declensions
     *
     * @param typeId
     * @param index
     * @return null if none found
     */
    public ConjugationNode getDimensionalConjugationTemplateByIndex(int typeId, int index) {
        ConjugationNode ret = null;
        List<ConjugationNode> nodes = dTemplates.get(typeId);

        int curIndex = 0;
        for (ConjugationNode node : nodes) {
            if (node.isDimensionless()) {
                continue;
            } else if (curIndex == index) {
                ret = node;
                break;
            }
            curIndex++;
        }

        return ret;
    }

    /**
     * recursive method to calculate value of overridden method
     *
     * @param depth current depth in calculation
     * @param curId current combined ID
     * @param curLabel current constructed label
     * @param declensionList list of template declensions for type
     * @return list of currently constructed labels and ids
     */
    private List<ConjugationPair> getAllCombinedDimensionalIds(int depth, String curId, String curLabel, ConjugationNode[] declensionList) {
        List<ConjugationPair> ret = new ArrayList<>();

        // for the specific case that a word with no declension patterns has a deprecated declension
        if (declensionList.length == 0) {
            return ret;
        }

        if (depth >= declensionList.length) {
            ret.add(new ConjugationPair(curId, curLabel));
        } else {

            ConjugationNode curNode = declensionList[depth];
            Collection<ConjugationDimension> dimensions = curNode.getDimensions();
            Iterator<ConjugationDimension> dimIt = dimensions.iterator();

            while (dimIt.hasNext()) {
                ConjugationDimension curDim = dimIt.next();

                ret.addAll(getAllCombinedDimensionalIds(depth + 1, curId + curDim.getId() + ",",
                        curLabel + (curLabel.isEmpty() ? "" : " ") + curDim.getValue(), declensionList));
            }
        }

        return ret;
    }

    /**
     * Fetches list of declined/conjugated wordforms for a given word. Only
     * pulls dimensional values. Singletons like gerunds are not included Note:
     * This DOES include deprecated wordforms! Be aware!
     *
     * @param wordId
     * @return
     */
    public ConjugationNode[] getDimensionalConjugationListWord(Integer wordId) {
        return getDimensionalConjugationList(wordId, dList).toArray(new ConjugationNode[0]);
    }

    /**
     * Gets list of dimensional template values. Does not pull singletons such
     * as gerunds.
     *
     * @param typeId
     * @return
     */
    public ConjugationNode[] getDimensionalConjugationListTemplate(Integer typeId) {
        return getDimensionalConjugationList(typeId, dTemplates).toArray(new ConjugationNode[0]);
    }

    /**
     * Gets full list of dimensional template values including singletons such
     * as gerunds.
     *
     * @param typeId
     * @return
     */
    public ConjugationNode[] getFullConjugationListTemplate(Integer typeId) {
        return getFullConjugationList(typeId, dTemplates).toArray(new ConjugationNode[0]);
    }

    public ConjugationNode addConjugationToTemplate(Integer typeId, Integer declensionId, ConjugationNode declension) {
        return ConjugationManager.this.addConjugation(typeId, declensionId, declension, dTemplates);
    }

    public ConjugationNode addConjugationToTemplate(Integer typeId, String declension) {
        return addConjugation(typeId, declension, dTemplates);
    }

    public void deleteConjugationFromTemplate(Integer typeId, Integer declensionId) {
        deleteConjugation(typeId, declensionId, dTemplates);
    }

    public void updateConjugationTemplate(Integer typeId, Integer declensionId, ConjugationNode declension) {
        updateConjugation(typeId, declensionId, declension, dTemplates);
    }

    public ConjugationNode getConjugationTemplate(Integer typeId, Integer templateId) {
        List<ConjugationNode> searchList = dTemplates.get(typeId);
        Iterator search = searchList.iterator();
        ConjugationNode ret = null;

        while (search.hasNext()) {
            ConjugationNode test = (ConjugationNode) search.next();

            if (test.getId().equals(templateId)) {
                ret = test;
                break;
            }
        }

        return ret;
    }

    public void setBufferId(Integer _bufferId) {
        buffer.setId(_bufferId);
    }

    public void setBufferDecText(String _bufferDecText) {
        buffer.setValue(_bufferDecText);
    }

    public String getBufferDecText() {
        return buffer.getValue();
    }

    public void setBufferDecNotes(String _bufferDecNotes) {
        buffer.setNotes(_bufferDecNotes);
    }

    public String getBufferDecNotes() {
        return buffer.getNotes();
    }

    public void setBufferDecTemp(boolean _bufferDecTemp) {
        bufferDecTemp = _bufferDecTemp;
    }

    public void setBufferRelId(Integer _bufferRelId) {
        bufferRelId = _bufferRelId;
    }

    public Integer getBufferRelId() {
        return bufferRelId;
    }

    public boolean isBufferDecTemp() {
        return bufferDecTemp;
    }

    public void insertBuffer() {
        if (bufferDecTemp) {
            this.addConjugationToTemplate(bufferRelId, buffer.getId(), buffer);
        } else {
            this.addConjugationToWord(bufferRelId, buffer.getId(), buffer);
        }
    }

    /**
     * gets current declension node buffer object
     *
     * @return buffer node object
     */
    public ConjugationNode getBuffer() {
        return buffer;
    }

    public void clearBuffer() {
        buffer = new ConjugationNode(-1);
        bufferDecTemp = false;
        bufferRelId = -1;
    }

    private ConjugationNode addConjugation(Integer typeId, String declension, Map<Integer, List<ConjugationNode>> idToDecNodes) {
        List<ConjugationNode> wordList;

        topId++;

        if (idToDecNodes.containsKey(typeId)) {
            wordList = idToDecNodes.get(typeId);
        } else {
            wordList = new ArrayList<>();
            idToDecNodes.put(typeId, wordList);
        }

        ConjugationNode addNode = new ConjugationNode(topId);
        addNode.setValue(declension);

        wordList.add(addNode);

        return addNode;
    }

    /**
     * Adds declension to related object (type or word)
     *
     * @param relId ID of related object
     * @param declensionId ID of declension to be created
     * @param declension declension node to be created
     * @param list list to add node to (word list or type list)
     * @return declension node created
     */
    private ConjugationNode addConjugation(Integer relId, Integer declensionId, ConjugationNode declension, Map<Integer, List<ConjugationNode>> list) {
        List<ConjugationNode> wordList;

        if (declensionId == -1) {
            declensionId = topId + 1;
        }

        deleteConjugationFromWord(relId, declensionId);

        if (list.containsKey(relId)) {
            wordList = list.get(relId);
        } else {
            wordList = new ArrayList<>();
            list.put(relId, wordList);
        }

        ConjugationNode addNode = new ConjugationNode(declensionId);
        addNode.setEqual(declension);

        wordList.add(addNode);

        if (declensionId > topId) {
            topId = declensionId;
        }

        return addNode;
    }

    /**
     * Gets stored declension for a word from combined dimensional Id of
     * declension. This does NOT generate a new declension, and is primarily of
     * use with overridden values and language files which do not use
     * autodeclension.
     *
     * @param wordId the id of the root word
     * @param dimId the combined dim Id of the dimension
     * @return The declension node if found, null if otherwise
     */
    public ConjugationNode getConjugationByCombinedId(Integer wordId, String dimId) {
        ConjugationNode ret = null;

        if (dList.containsKey(wordId)) {
            for (ConjugationNode test : dList.get(wordId)) {
                if (dimId.equals(test.getCombinedDimId())) {
                    ret = test;
                    break;
                }
            }
        }

        return ret;
    }

    public String getCombNameFromCombId(int typeId, String combId) {
        String ret = "";
        ConjugationNode[] decNodes = getDimensionalConjugationListTemplate(typeId);
        String[] splitIds = combId.split(",");

        for (int i = 0; i < decNodes.length && i < (splitIds.length - 1); i++) {
            ConjugationNode curNode = decNodes[i];
            int dimId = Integer.parseInt(splitIds[i + 1]);
            Iterator<ConjugationDimension> dimIt = curNode.getDimensions().iterator();
            ConjugationDimension curDim = null;

            while (dimIt.hasNext()) {
                curDim = dimIt.next();

                if (curDim.getId().equals(dimId)) {
                    break;
                }
            }

            if (curDim != null) {
                ret += " " + curDim.getValue();
            }
        }

        return ret.trim();
    }

    public void deleteConjugation(Integer typeId, Integer declensionId, Map<Integer, List<ConjugationNode>> list) {
        if (list.containsKey(typeId)) {
            List<ConjugationNode> copyTo = new ArrayList<>();
            Iterator<ConjugationNode> copyFrom = list.get(typeId).iterator();

            while (copyFrom.hasNext()) {
                ConjugationNode curNode = copyFrom.next();

                if (curNode.getId().equals(declensionId)) {
                    continue;
                }

                copyTo.add(curNode);
            }

            list.remove(typeId);

            // if unpopulated, allow to not exist. Cleaner.
            if (!copyTo.isEmpty()) {
                list.put(typeId, copyTo);
            }
        }
    }

    private void updateConjugation(Integer typeId,
            Integer declensionId,
            ConjugationNode declension,
            Map<Integer, List<ConjugationNode>> list) {
        if (list.containsKey(typeId)) {
            List<ConjugationNode> copyTo = new ArrayList<>();
            Iterator<ConjugationNode> copyFrom = list.get(typeId).iterator();

            while (copyFrom.hasNext()) {
                ConjugationNode curNode = copyFrom.next();

                if (curNode.getId().equals(declensionId)) {
                    ConjugationNode modified = new ConjugationNode(declensionId);
                    modified.setEqual(declension);
                    copyTo.add(modified);
                    continue;
                }

                copyTo.add(curNode);
            }

            list.remove(typeId);
            list.put(typeId, copyTo);
        }
    }

    /**
     * Clears all declensions from word
     *
     * @param wordId ID of word to clear of all declensions
     */
    private void clearAllConjugations(Integer wordId, Map list) {
        list.remove(wordId);
    }

    /**
     * Retrieves all dimensional declensions based on related ID and the list to
     * be pulled from. The list can either be the templates (related via typeId)
     * or actual words, related by wordId
     *
     * @param relatedId ID of related value
     * @param valueMap list of relations to search through
     * @return
     */
    private List<ConjugationNode> getDimensionalConjugationList(Integer relatedId,
            Map<Integer, List<ConjugationNode>> valueMap) {
        List<ConjugationNode> ret = new ArrayList<>();

        if (valueMap.containsKey(relatedId)) {
            List<ConjugationNode> allNodes = valueMap.get(relatedId);

            allNodes.forEach((curNode) -> {
                // dimensionless nodes
                if (!curNode.isDimensionless()) {
                    ret.add(curNode);
                }
            });
        }

        return ret;
    }

    /**
     * Public version of private method directly below. Retrieves all singleton
     * declensions based on related ID and the list to be pulled from. The list
     * can either be the templates (related via typeId) or actual words, related
     * by wordId
     * 
     * NOTE: Currently used in single test... will keep for now for use in testing data integrity
     *
     * @param relatedId ID of related value
     * @return
     */
    public ConjugationNode[] getSingletonConjugationList(Integer relatedId) {
        return ConjugationManager.this.getSingletonConjugationList(relatedId, dTemplates).toArray(new ConjugationNode[0]);
    }

    /**
     * Retrieves all singleton declensions based on related ID and the list to
     * be pulled from. The list can either be the templates (related via typeId)
     * or actual words, related by wordId
     *
     * @param relatedId ID of related value
     * @param list list of relations to search through
     * @return
     */
    private List<ConjugationNode> getSingletonConjugationList(Integer relatedId,
            Map<Integer, List<ConjugationNode>> list) {
        List<ConjugationNode> ret = new ArrayList<>();

        if (list.containsKey(relatedId)) {
            List<ConjugationNode> allNodes = list.get(relatedId);

            allNodes.forEach((curNode) -> {
                // dimensionless nodes
                if (curNode.isDimensionless()) {
                    ret.add(curNode);
                }
            });
        }

        return ret;
    }

    /**
     * Returns full list of declensions irrespective of whether they are
     * dimensional or not. Will return singletons such as gerunds.
     *
     * @param relatedId ID of related value
     * @param list list of relations to search through
     * @return
     */
    private List<ConjugationNode> getFullConjugationList(Integer relatedId, Map<Integer, List<ConjugationNode>> list) {
        List<ConjugationNode> ret = new ArrayList<>();

        if (list.containsKey(relatedId)) {
            ret = list.get(relatedId);
        }

        return ret;
    }

    public ConjugationNode[] getFullConjugationListWord(Integer wordId) {
        return getFullConjugationList(wordId, dList).toArray(new ConjugationNode[0]);
    }

    /**
     * Gets a word's declensions, with their combined dim Ids as the keys DOES
     * NOT GENERATE DECLENSIONS THAT ARE SET TO AUTOGENERATE, BUT HAVE NOT YET
     * BEEN SAVED. Note: This returns deprecated wordforms as well as current
     * ones.
     *
     * @param wordId word to get declensions of
     * @return map of all declensions in a word (empty if none)
     */
    public Map<String, ConjugationNode> getWordConjugation(Integer wordId) {
        Map<String, ConjugationNode> ret = new HashMap<>();

        ConjugationNode[] decs = getDimensionalConjugationListWord(wordId);

        for (ConjugationNode curNode : decs) {
            ret.put(curNode.getCombinedDimId(), curNode);
        }

        return ret;
    }

    /**
     * Removes all declensions contained in decMap from word with wordid
     *
     * @param wordId ID of word to clear values from
     * @param removeVals values to clear from word
     */
    public void removeConjugationValues(Integer wordId, Collection<ConjugationNode> removeVals) {
        List<ConjugationNode> wordList = dList.get(wordId);

        removeVals.forEach((remNode) -> {
            wordList.remove(remNode);
        });
    }

    /**
     * Writes all declension information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Set<Entry<Integer, List<ConjugationNode>>> declensionSet;
        Element declensionCollection = doc.createElement(PGTUtil.DECLENSION_COLLECTION_XID);
        rootElement.appendChild(declensionCollection);
        
        // ensure rule IDs are contiguous before save
        this.smoothRules();

        // record declension templates
        declensionSet = dTemplates.entrySet();
        for (Entry<Integer, List<ConjugationNode>> e : declensionSet) {
            final Integer relatedId = e.getKey();

            e.getValue().forEach((curNode) -> {
                curNode.writeXMLTemplate(doc, declensionCollection, relatedId);
            });
        }

        // record word declensions
        declensionSet = dList.entrySet();
        for (Entry<Integer, List<ConjugationNode>> e : declensionSet) {
            final Integer relatedId = e.getKey();

            e.getValue().forEach((curNode) -> {
                curNode.writeXMLWordConjugation(doc, declensionCollection, relatedId);
            });
        }

        // record declension autogeneration rules
        generationRules.values().forEach((rules) -> {
            rules.forEach((rule) -> {
                rule.writeXML(doc, declensionCollection);
            });
        });

        // record combined form settings
        Element combinedForms = doc.createElement(PGTUtil.DEC_COMBINED_FORM_SECTION_XID);
        rootElement.appendChild(combinedForms);

        combSettings.entrySet().stream().map((pairs) -> {
            Element curCombForm = doc.createElement(PGTUtil.DEC_COMBINED_FORM_XID);
            Element curAttrib;
            // This section will have to be slightly rewritten if the combined settings become more complex
            curAttrib = doc.createElement(PGTUtil.DEC_COMBINED_ID_XID);
            curAttrib.appendChild(doc.createTextNode(pairs.getKey()));
            curCombForm.appendChild(curAttrib);
            curAttrib = doc.createElement(PGTUtil.DEC_COMBINED_SURPRESS_XID);
            curAttrib.appendChild(doc.createTextNode(pairs.getValue() ? PGTUtil.TRUE : PGTUtil.FALSE));
            curCombForm.appendChild(curAttrib);
            return curCombForm;
        }).forEachOrdered((curCombForm) -> {
            combinedForms.appendChild(curCombForm);
        });
    }

    /**
     * This copies a list of rules to the bottom of the list of all declension
     * templates for a given part of speech that share a declension (decId) with
     * the value defined by dimId
     *
     * NOTE: Only applies to dimensional declensions.Singletons must be copied
     * to manually.
     *
     * @param typeId Part of speech to target
     * @param decId declension dimension to target
     * @param dimId dimension value to target
     * @param rules rules to be copied
     * @param selfCombId The combined ID of the form this was initially called
     * from (do not copy duplicate of rule to self)
     */
    public void copyRulesToConjugationTemplates(int typeId,
            int decId, int dimId,
            List<ConjugationGenRule> rules,
            String selfCombId) {
        ConjugationNode[] allNodes = getDimensionalConjugationListTemplate(typeId);
        List<ConjugationPair> decList = getAllCombinedDimensionalIds(0, ",", "", allNodes);

        decList.forEach((decPair) -> {
            // only copy rule if distinct from base word form && it matches the dimensional value matches
            if (!decPair.combinedId.equals(selfCombId) && combDimIdMatches(decId, dimId, decPair.combinedId)) {
                rules.forEach((rule) -> {
                    // insert rule
                    ConjugationGenRule newRule = new ConjugationGenRule();
                    newRule.setEqual(rule, false);
                    newRule.setTypeId(typeId);
                    newRule.setCombinationId(decPair.combinedId);

                    addConjugationGenRule(newRule);

                    // call get rules for type (will automatically assign next highest index to rule
                    this.getAllDepGenerationRules(typeId);
                });
            }
        });
    }


    /**
     * This copies a list of rules to the bottom of the list of all declension
     * templates for a given part of speech that share a declension (decId) with
     * the value defined by dimId
     *
     * NOTE: Only applies to dimensional declensions.Singletons must be copied
     * to manually.
     *
     * @param typeId Part of speech to target
     * @param decId declension dimension to target
     * @param dimId dimension value to target
     * @param rulesToDelete rules to be deleted
     */
    public void deleteRulesFromConjugationTemplates(int typeId,
            int decId, int dimId,
            List<ConjugationGenRule> rulesToDelete) {

        ConjugationGenRule[] rules = this.getConjugationRulesForType(typeId);
        
        for (ConjugationGenRule rule : rules) {
            if (combDimIdMatches(decId, dimId, rule.getCombinationId())) {
                for (ConjugationGenRule ruleDelete : rulesToDelete) {
                    if (rule.valuesEqual(ruleDelete)) {
                        this.deleteConjugationGenRule(rule);
                    }
                }
            }
        }
    }
    
    /**
     * Updates all instances rules matching those passed in within a given word type
     *
     * @param typeId part of speech to update rules for
     * @param rulesToUpdate rules in this pos to update
     */
    public void bulkUpdateRuleInConjugationTemplates(int typeId, List<ConjugationGenRule> rulesToUpdate) {
        ConjugationGenRule[] rules = this.getConjugationRulesForType(typeId);

        for (ConjugationGenRule rule : rules) {
            for (ConjugationGenRule ruleUpdateFrom : rulesToUpdate) {
                if (rule != ruleUpdateFrom && rule.valuesShallowEqual(ruleUpdateFrom)) {
                    rule.setEqual(ruleUpdateFrom, false);
                }
            }
        }
    }
    
    /**
     * Deletes ALL instances of a rule within a given word type
     *
     * @param typeId part of speech to clear
     * @param rulesToDelete rules in this pos to delete
     */
    public void bulkDeleteRuleFromConjugationTemplates(int typeId, List<ConjugationGenRule> rulesToDelete) {
        ConjugationGenRule[] rules = this.getConjugationRulesForType(typeId);

        for (ConjugationGenRule rule : rules) {
            for (ConjugationGenRule ruleDelete : rulesToDelete) {
                if (rule.valuesEqual(ruleDelete)) {
                    this.deleteConjugationGenRule(rule);
                }
            }
        }
    }

    private boolean combDimIdMatches(int decId, int dimId, String combDimId) {
        String[] strIds = combDimId.split(",");
        String strId = strIds[decId + 1]; // account for leading comma
        int dimValId = Integer.parseInt(strId);
        return dimValId == dimId;
    }

    public String getConjugationLabel(int typeId, int decId) {
        return dTemplates.get(typeId).get(decId).getValue();
    }

    public String getConjugationValueLabel(int typeId, int decId, int decValId) {
        return dTemplates.get(typeId).get(decId).getConjugationDimensionById(decValId).getValue();
    }

    /**
     * On load of older pgt files, must be called to maintain functionality of
     * declension rules
     */
    public void setAllConjugationRulesToAllClasses() {
        generationRules.values().forEach((rules) -> {
            rules.forEach((rule) -> {
                rule.addClassToFilterList(-1, -1);
            });
        });
    }

    /**
     * Returns all saved yet deprecated wordforms of a word
     *
     * @param word
     * @return
     */
    public Map<String, ConjugationNode> getDeprecatedForms(ConWord word) {
        Map<String, ConjugationNode> ret = new HashMap<>();

        // first get all values that exist for this word
        for (ConjugationNode curNode : getFullConjugationListWord(word.getId())) {
            ret.put(curNode.getCombinedDimId(), curNode);
        }

        // then remove all values which match existing combined type ids
        ConjugationPair[] allCombIds = getAllCombinedIds(word.getWordTypeId());
        for (ConjugationPair curPair : allCombIds) {
            ret.remove(curPair.combinedId);
        }

        return ret;
    }

    /**
     * Returns true if given word has deprecated wordforms
     *
     * @param word
     * @return
     */
    public boolean wordHasDeprecatedForms(ConWord word) {
        return !getDeprecatedForms(word).isEmpty();
    }
    
    /**
     * Fetches debug values for the most recently created declension
     * @return 
     */
    public String[] getDecGenDebug() {
        return decGenDebug.toArray(new String[0]);
    }
    
    public boolean isEmpty() {
        return generationRules.isEmpty();
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp instanceof ConjugationManager) {
            ConjugationManager compMan = (ConjugationManager)comp;
            ret = (generationRules == null && compMan.generationRules == null) || generationRules.equals(compMan.generationRules);
            ret = ret && ((dList == null && compMan.dList == null) || dList.equals(compMan.dList));
            ret = ret && combSettings.equals(compMan.combSettings);
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.generationRules);
        hash = 41 * hash + Objects.hashCode(this.dList);
        hash = 41 * hash + Objects.hashCode(this.dTemplates);
        hash = 41 * hash + Objects.hashCode(this.combSettings);
        return hash;
    }
    
    /**
     * Moves set of rules up in priority
     * @param typeId
     * @param combId
     * @param ruleBlock 
     */
    public void moveRulesUp(int typeId, String combId, List<ConjugationGenRule> ruleBlock) {
        if (ruleBlock.isEmpty()) {
            return;
        }
        
        List<ConjugationGenRule> formRules = new ArrayList<>();
        
        for (ConjugationGenRule curRule : generationRules.get(typeId)) {
            if (curRule.getCombinationId().equals(combId)) {
                formRules.add(curRule);
            }
        }
        
        Collections.sort(formRules);
        
        // find the rule BEFORE the first rule to be moved up
        ConjugationGenRule beforeFirst = null;
        for (int i = 0; i < formRules.size(); i ++) {
            ConjugationGenRule curRule = formRules.get(i);
            if (curRule.equals(ruleBlock.get(0))) {
                // only set beforeFirst value if this is not already the first rule for this declension
                if (i != 0) {
                    beforeFirst = formRules.get(i - 1);
                }
            }
        }
        
        // if beforeFirst is null, the rule block given is already first
        if (beforeFirst != null) {
            int lastIndex = ruleBlock.get(ruleBlock.size() - 1).getIndex();
            for (int i = 0; i < ruleBlock.size(); i++) {
                ruleBlock.get(i).setIndex(beforeFirst.getIndex() + i);
            }
            
            // finally, take the rule which was previously above the block and give it the last index
            beforeFirst.setIndex(lastIndex);
        }
    }
    
    /**
     * Moves set of rules down in priority
     * @param typeId
     * @param combId
     * @param ruleBlock 
     */
    public void moveRulesDown(int typeId, String combId, List<ConjugationGenRule> ruleBlock) {
        if (ruleBlock.isEmpty()) {
            return;
        }
        
        List<ConjugationGenRule> formRules = new ArrayList<>();
        
        for (ConjugationGenRule curRule : generationRules.get(typeId)) {
            if (curRule.getCombinationId().equals(combId)) {
                formRules.add(curRule);
            }
        }
        
        Collections.sort(formRules);
        
        // find the rule AFTER the last rule to be moved down
        ConjugationGenRule afterLast = null;
        for (int i = 0; i < formRules.size(); i ++) {
            ConjugationGenRule curRule = formRules.get(i);
            if (curRule.equals(ruleBlock.get(ruleBlock.size() - 1))) {
                // only set afterList value if this is not already the last rule for this declension
                if (i != (formRules.size() - 1)) {
                    afterLast = formRules.get(i + 1);
                }
            }
        }
        
        // if afterLast is null, the rule block given is already last
        if (afterLast != null) {
            int firstIndex = ruleBlock.get(0).getIndex();
            for (int i = 0; i < ruleBlock.size(); i++) {
                ruleBlock.get(i).setIndex(ruleBlock.get(i).getIndex() + 1);
            }
            
            // finally, take the rule which was previously belo the block and give it the first index
            afterLast.setIndex(firstIndex);
        }
    }
}
