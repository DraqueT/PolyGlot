/*
 * Copyright (c) 2014-2023, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.darisadesigns.polyglotlina.IPAHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class PronunciationMgr {

    private final DictCore core;
    protected boolean recurse = false;
    private List<PronunciationNode> pronunciations = new ArrayList<>();
    private final Set<String> syllables = new HashSet<>();
    private boolean syllableCompositionEnabled = false;
    
    public PronunciationMgr(DictCore _core) {
        core = _core;
    }
    
    public void addSyllable(String syllable) {
        syllables.add(syllable);
    }
    
    public boolean isSyllable(String testSyllable) {
        return syllables.contains(testSyllable);
    }
    
    public void clearSyllables() {
        syllables.clear();
    }
    
    public boolean isSyllableCompositionEnabled() {
        return syllableCompositionEnabled;
    }
    
    /**
     * Returns all stored compositional syllable values.
     * Order not guaranteed.
     * @return 
     */
    public String[] getSyllables() {
        return syllables.toArray(new String[0]);
    }
    
    public void setSyllableCompositionEnabled(boolean _syllableCompositionEnabled) {
        syllableCompositionEnabled = _syllableCompositionEnabled;
    }

    /**
     * Sets list of pronunciations
     *
     * @param _pronunciations new list to replace old
     */
    public void setPronunciations(List<PronunciationNode> _pronunciations) {
        pronunciations = _pronunciations;
    }

    /**
     * gets iterator with all pronunciation pairs
     *
     * @return list of PronunciationNodes
     */
    public PronunciationNode[] getPronunciations() {
        // CORRECT FOR FILTERING/CREATION OF COPY OBJECT
        return pronunciations.toArray(new PronunciationNode[0]);
    }

    /**
     * Inserts a node at an arbitrary position
     *
     * @param index position to insert
     * @param newNode node to be inserted
     */
    public void addAtPosition(int index, PronunciationNode newNode) {
        pronunciations.add(index, newNode);
    }

    /**
     * moves a pronunciation up one slot to increase priority by 1
     *
     * @param index index of node to move up
     */
    public void moveProcUp(int index) {
        PronunciationNode node = pronunciations.get(index);

        // -1 = not found, size 0 = start of list
        if (index == -1 || index == 0) {
            return;
        }

        pronunciations.remove(index);
        pronunciations.add(index - 1, node);
    }

    /**
     * moves a pronunciation down one slot to decrease priority by 1
     *
     * @param index index of the node to move down
     */
    public void moveProcDown(int index) {
        PronunciationNode node = pronunciations.get(index);

        // -1 = not found, size - 1 = end of list already
        if (index == -1 || index == pronunciations.size() - 1) {
            return;
        }

        pronunciations.remove(index);
        pronunciations.add(index + 1, node);
    }
    
    public void deletePronunciation(int orderedLocation) {
        if (orderedLocation >= 0 && orderedLocation < pronunciations.size()) {
            pronunciations.remove(orderedLocation);
        }
    }

    public void addPronunciation(PronunciationNode newNode) {
        pronunciations.add(newNode);
    }

    /**
     * Returns pronunciation of a given word
     *
     * @param base word to find pronunciation of
     * @return pronunciation string. If no perfect match found, empty string
     * returned
     * @throws java.lang.Exception on malformed regex statements encountered
     */
    public String getPronunciation(String base) throws Exception {
        String[] spaceDelimited = base.trim().split(" ");
        String ret = "";
        
        for (String fragment : spaceDelimited) {
            ret += " " + getPronunciationInternal(fragment);
        }
        
        return ret.trim();
    }
    
    private String getPronunciationInternal(String base) throws Exception {
        String ret = "";

        int[] syllableBreaks = new int[0];
        if (syllableCompositionEnabled) {
            syllableBreaks = this.getSyllableBreaks(base);
        }
        
        // -base.length() fed as initial depth to ensure that longer words cannot be artificially labeled as breaking max depth
        List<PronunciationNode> procCycle = getPronunciationElements(base, -base.length(), true);
        int charCount = 0;
        for (PronunciationNode curProc : procCycle) {
            ret += curProc.getPronunciation();
            
            charCount += curProc.getOriginPattern().length();
            final int godDamnIt = charCount;
            if (syllableCompositionEnabled && IntStream.of(syllableBreaks).anyMatch(x -> x == godDamnIt)) {
                ret += "˙";
            }
        }

        return ret;
    }
    
    /**
     * Generates and returns locations within string where syllable breaks
     * may be placed based on populated syllables. Returns empty array if 
     * no valid breakup possible
     * @param base
     * @return 
     */
    private int[] getSyllableBreaks(String base) {
        return getSyllableBreaksRecurse(base, 0);
    }
    
    private int[] getSyllableBreaksRecurse(String base, int cur) {
        for (int i = 1; i <= base.length(); i++) {
            if (syllables.contains(base.substring(0, i))) {
                // syllables ending the word do not need demarkation
                if (i == base.length()) {
                    int[] ending = {-1};
                    return ending;
                }
                
                int[] subSearch = getSyllableBreaksRecurse(base.substring(i), cur + i);
                
                // length > 0 means the subsearch was a success, continue otherwise
                if (subSearch.length > 0) {
                    int[] myLocation = {i + cur};
                    return concatIntArrays(myLocation, subSearch);
                }
            }
        }
        
        return new int[0];
    }
    
    private int[] concatIntArrays(int [] array1, int[] array2) {
        int[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    /**
     * Returns pronunciation elements of word
     *
     * @param base word to find pronunciation elements of
     * @return elements of pronunciation for word. Empty if no perfect match
     * found
     * @throws java.lang.Exception if malformed regex expression encountered
     */
    public PronunciationNode[] getPronunciationElements(String base) throws Exception {
        // -base.length() fed as initial depth to ensure that longer words cannot be artificially labeled as breaking max depth
        return getPronunciationElements(base, -base.length(), true).toArray(new PronunciationNode[0]);
    }
    
    /**
     * Generates IPA version of an entire phrase
     * @param phrase
     * @return 
     */
    public String getIpaOfPhrase(String phrase) {
        String[] words = phrase.split("\\s");
        String curWord = "";
        String result = "";
        
        try {
            for (String word : words) {
                curWord = word;
                result += this.getPronunciation(word) + " ";
            }
        } catch (Exception e) {
            result += "\n ERROR CONVERTING PATTERN: " + curWord; 
        }
        
        return result;
    }
    
    protected String getToolLabel() {
        return "Pronunciation Manager";
    }

    /**
     * returns pronunciation objects of a given word
     *
     * @param base word to find pronunciation objects of
     * @param depth current depth
     * @param beginning true if beginning of word (cannot rely on depth)
     * @return pronunciation object list. If no perfect match found, empty
     * string returned
     */
    private List<PronunciationNode> getPronunciationElements(String base, int depth, boolean beginning) throws Exception {
        List<PronunciationNode> ret;

        if (depth > PGTUtil.MAX_PROC_RECURSE) {
            throw new Exception("Max recursions for " + getToolLabel() + " exceeded.");
        }
        
        // return blank for empty string
        if (base.isEmpty() || pronunciations.isEmpty()) {
            ret = new ArrayList<>();
        } else {
            // split logic here to use recursion, string comparison, or regex matching
            if (recurse) {
                ret = getPronunciationElementsRecurse(base);
            } else if (core.getPropertiesManager().isDisableProcRegex()) {
                ret = getPronunciationElementsNoRegex(base, depth);
            } else {
                ret = getPronunciationElementsWithRegex(base, depth, beginning);
            }
        }

        return ret;
    }
    
    private List<PronunciationNode> getPronunciationElementsWithRegex(String base, int depth, boolean beginning) throws Exception {
        List<PronunciationNode> ret = new ArrayList<>();
        
        for (PronunciationNode curNode : pronunciations) {
            String pattern = curNode.getValue();
            // skip if set as starting characters, but later in word
            if (pattern.startsWith("^") && !beginning) {
                continue;
            }

            // original pattern
            String origPattern = pattern;

            // make pattern a starting pattern if not already, if it is already, allow it to accept following strings
            if (pattern.startsWith("^")) {
                pattern = "^(" + pattern.substring(1) + ").*";
            } else {
                pattern = "^(" + pattern + ").*";
            }

            Pattern findString = Pattern.compile(pattern);
            Matcher matcher = findString.matcher(base);

            if (matcher.matches()) {
                String leadingChars = matcher.group(1);

                // if a user has entered an empty pattern... just continue.
                if (leadingChars.isEmpty()) {
                    continue;
                }
                List<PronunciationNode> temp
                        = getPronunciationElementsWithRegex(base.substring(leadingChars.length()), depth + 1, false);

                try {
                    if (leadingChars.length() == base.length() || !temp.isEmpty()) {
                        PronunciationNode finalNode = new PronunciationNode();
                        finalNode.setEqual(curNode);
                        finalNode.setPronunciation(leadingChars.replaceAll(origPattern, curNode.getPronunciation()));
                        finalNode.setOriginPattern(leadingChars);
                        ret.add(finalNode);
                        ret.addAll(temp);
                        break;
                    }
                } catch (IndexOutOfBoundsException e) {
                    throw new Exception("The pronunciation pair " + curNode.getValue() + "->"
                            + curNode.getPronunciation() + " is generating a regex error. Please correct."
                            + "\nError: " + e.getLocalizedMessage() + e.getClass().getName(), e);
                }
            }
        }
        
        return ret;
    }
    
    private List<PronunciationNode> getPronunciationElementsNoRegex(String base, int depth) throws Exception {
        List<PronunciationNode> ret = new ArrayList<>();
        
        for (PronunciationNode curNode : pronunciations) {
            String pattern = curNode.getValue();
            // do not overstep string
            if (pattern.length() <= base.length()) {
                // capture string to compare based on pattern length
                String comp = base.substring(0, curNode.getValue().length());

                if (core.getPropertiesManager().isIgnoreCase()) {
                    comp = comp.toLowerCase();
                    pattern = pattern.toLowerCase();
                }

                if (comp.equals(pattern)) {
                    List<PronunciationNode> temp
                            = getPronunciationElementsNoRegex(base.substring(pattern.length()), depth + 1);

                    // if lengths are equal, success! return. If unequal and no further match found-failure
                    if (pattern.length() == base.length() || !temp.isEmpty()) {
                        PronunciationNode newNode = new PronunciationNode();
                        newNode.setEqual(curNode);
                        newNode.setOriginPattern(pattern);
                        ret.add(newNode);
                        ret.addAll(temp);
                        break;
                    }
                }
            }
        }
        
        return ret;
    }
    
    private List<PronunciationNode> getPronunciationElementsRecurse(String base) {
        List<PronunciationNode> ret = new ArrayList<>();
        
        // when using recursion, only a single node can be returned, inherently.
        String retStr = base;
        PronunciationNode retNode = new PronunciationNode();

        for (PronunciationNode curNode : pronunciations) {
            retStr = retStr.replaceAll(curNode.getValue(), curNode.getPronunciation());
        }

        retNode.setPronunciation(retStr);
        ret.add(retNode);
        
        return ret;
    }

    /**
     * Writes all pronunciation information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element collection = doc.createElement(PGTUtil.PRONUNCIATION_COLLECTION_XID);
        
        rootElement.appendChild(collection);
        
        Element syllableList = doc.createElement(PGTUtil.PRO_GUIDE_SYLLABLES_LIST);
        for (String syllable : syllables) {
            Element syllableNode = doc.createElement(PGTUtil.PRO_GUIDE_SYLLABLE);
            syllableNode.appendChild(doc.createTextNode(syllable));
            syllableList.appendChild(syllableNode);
        }
        collection.appendChild(syllableList);
        
        Element syllableComposition = doc.createElement(PGTUtil.PRO_GUIDE_COMPOSITION_SYLLABLE);
        syllableComposition.appendChild(doc.createTextNode(syllableCompositionEnabled ?
                PGTUtil.TRUE : PGTUtil.FALSE));
        collection.appendChild(syllableComposition);
        
        Element recurseNode = doc.createElement(PGTUtil.PRO_GUIDE_RECURSIVE_XID);
        recurseNode.appendChild(doc.createTextNode(recurse ?
                PGTUtil.TRUE : PGTUtil.FALSE));
        collection.appendChild(recurseNode);
        
        pronunciations.forEach((proc)->{
            proc.writeXML(doc, collection);
        });
    }

    public boolean isRecurse() {
        return recurse;
    }

    public void setRecurse(boolean _recurse) {
        this.recurse = _recurse;
    }
    
    /**
     * Returns true if there are any pronunciation rules defined
     * @return 
     */
    public boolean isInUse() {
        return !pronunciations.isEmpty();
    }
    
    public boolean isEmpty() {
        return pronunciations.isEmpty();
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp instanceof PronunciationMgr) {
            PronunciationMgr compProp = (PronunciationMgr)comp;
            
            ret = recurse == compProp.recurse
                    && pronunciations.equals(compProp.pronunciations);
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.recurse ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.pronunciations);
        return hash;
    }
    
    /**
     * Tests whether look-ahead or look-behind patterns are being used
     * @return 
     */
    public boolean usingLookaheadsLookbacks() {
        boolean ret = false;
        
        for (PronunciationNode curNode : pronunciations) {
            String pattern = curNode.getValue();
            
            // checks for all positive and negative lookaheads and lookbehinds
            if (isRegexLookaheadBehind(pattern)) {
                ret = true;
                break;
            }
        }
        
        return ret;
    }
    
    /**
     * Returns true if a given regex pattern passed in contains look-ahead or look-behinds
     * @param testRegex
     * @return 
     */
    public static boolean isRegexLookaheadBehind(String testRegex) {
        return testRegex.matches(".*\\((\\?=|\\?\\!|\\?<=|\\?<!).+?\\).*");
    }
    
    /**
     * Generates and returns a map of characters to to the IPA sounds that they
     * can create and their context. The returned map has values which come in
     * pairs. Each pair of two represents; 
     * 1) The IPA character which is pronounced as a result of the key alphabetic character
     * 2) The context within which the key alphabetic character creates the IPA character
     * @return 
     */
    public Map<String, String[]> getIpaSoundsPerCharacter() {
        Map<String, String[]> ret = new HashMap<>();
        String[] allIpaChars = IPAHandler.getAllIpaChars();
        String[] alphaValues = core.getPropertiesManager().getAlphaOrder().keySet().toArray(new String[0]);
        Map<String, List<PronunciationNode>> alphaAssociations = new HashMap<>();
        
        // Test if the VALUE for each pronunciation pair (containing the match pattern) includes any given
        // alhpabetic character. If so, associate the alphabetic character with the pronunciation
        for (PronunciationNode pronunciation : pronunciations) {
            for (String alphaChar : alphaValues) {
                if (pronunciation.getValue().contains(alphaChar)) {
                    if (alphaAssociations.containsKey(alphaChar)) {
                        alphaAssociations.get(alphaChar).add(pronunciation);
                    } else {
                        List<PronunciationNode> associationList = new ArrayList<>();
                        associationList.add(pronunciation);
                        alphaAssociations.put(alphaChar, associationList);
                    }
                }
            }
        }
        
        // Next, check through each REPLACEMENT pattern of every pattern for each IPA
        // character. For those that match, add the IPA Character, then the pronunciaton
        // VALUE to the return. Ths returns the Alphabet character as a key value leading
        // to paired IPA characters they can represent and the situation WHEN they
        // represent those characters
        for (String alphaChar : alphaValues) {
            List<String> retValues = new ArrayList<>();
            
            if (alphaAssociations.containsKey(alphaChar)) {
                for (PronunciationNode procNode : alphaAssociations.get(alphaChar)) {
                    for (String ipaChar : allIpaChars) {
                        if (procNode.getPronunciation().contains(ipaChar) && !retValues.contains(ipaChar)) {
                            retValues.add(ipaChar);
                            retValues.add(procNode.getValue());
                        }
                    }
                }
            }
            
            ret.put(alphaChar, retValues.toArray(new String[0]));
        }
        
        return ret;
    }
    
    /**
     * The inverse of getIpaSoundsPerCharacter
     * @return 
     */
    public Map<String, String[]> getCharactersPerIpaSound() {
        Map<String, String[]> ret = new HashMap<>();
        Map<String, String[]> charsPerIpa = getIpaSoundsPerCharacter();
        
        // values and keys are swapped
        for (String key : charsPerIpa.keySet()) {
            String[] values = charsPerIpa.get(key);
            for (int i = 0; i < values.length; i += 2) {
                String value = values[i];
                if (ret.containsKey(value)) {
                    List<String> curVals = new  ArrayList(Arrays.asList(ret.get(value)));
                    if (!curVals.contains(key)) {
                        curVals.add(key);
                    }
                    
                    ret.replace(value, curVals.toArray(new String[0]));
                } else {
                    ret.put(value, new String[]{key});
                }
            }
        }
        
        return ret;
    }
}
