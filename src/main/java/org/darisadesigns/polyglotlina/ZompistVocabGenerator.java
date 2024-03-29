/*
 * Copyright (c) 2022, Draque Thompson, draquemail@gmail.com
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;

/**
 * Derived from the Zompist Vocab Generator.
 * Original script/algorithm c 2012 Mark Rosenfelder
 */
public class ZompistVocabGenerator {

    public final static String INTERPUNCT = "·";
    private static final int MAX_RECURSE = 30;
    private static final int SENTENCE_GEN_COUNT = 30;
    private static final String PUNCTUATION = ".?!";
    private final float monosyllableRarity;
    private final int dropoff;
    private final String[] rewriteValues;
    private final boolean slowSyllables;
    private final Map<String, List<String>> categories;
    private final String categoryIndex;
    private final String[] userSyllables;
    private final int sylableDropoffRate;
    private final boolean syllableBreaks; // adds dot breaks between sylables  
    private final Set<String> results = new LinkedHashSet<>();
    private final String[] illegalClusters;
    private final OSHandler osHandler;
    private boolean abort = false;
    
    /**
     * @param _slowSyllables Slow syllable (default = false)
     * @param _syllableBreaks add syllable break character in display
     * @param _monosyllableRarity rarity of monosylables - maybe make this a slider? - 0.00
     *  = Always - 0.85 = Mostly - 0.50 = Frequent - 0.20 = Less Frequent
     *  (default) - 0.07 = Rare
     * @param _dropoff syllable dropoff rate - 30 = fast - 45 = equal - 15 =
     * medium (default) - 8 = slow - 0 = equiprobable
     * @param rawCategories - from text box
     * @param rawSyllables - from text box
     * @param rawRewriteValues - from text box
     * @param rawIllegalClusters -- illegal clusters to disallow from generation
     * @param _osHandler
     * @throws java.lang.Exception on malformed input
     */
    public ZompistVocabGenerator(
            boolean _slowSyllables,
            boolean _syllableBreaks,
            float _monosyllableRarity,
            int _dropoff,
            String rawCategories,
            String rawSyllables,
            String rawRewriteValues,
            String rawIllegalClusters,
            OSHandler _osHandler) throws Exception {
        slowSyllables = _slowSyllables;
        syllableBreaks = _syllableBreaks;
        monosyllableRarity = _monosyllableRarity;
        dropoff = _dropoff;
        categories = parseCatrgories(rawCategories);
        categoryIndex = getCategoryIndex();
        userSyllables = rawSyllables.replaceAll(" ", "").split("\n");
        rewriteValues = rawRewriteValues.replaceAll(" ", "").split("\n");
        sylableDropoffRate = getSyllableDropoffRate(slowSyllables, userSyllables.length);
        illegalClusters = getIllegalClusters(rawIllegalClusters.replace(" ", ""));
        osHandler = _osHandler;

        if (categories.isEmpty() || userSyllables.length == 0) {
            throw new Exception("You must have both categories and syllables to generate text.");
        }
    }

    public String[] genAllSyllables() {
        results.clear();
        for (String syllable : userSyllables) {
            if (abort) {
                return new String[0];
            }
            
            genall("", syllable);
        }

        return results.toArray(new String[0]);
    }

    /**
     * Generates requested number of words
     * @param lexiconLength
     * @return 
     * @throws java.lang.Exception 
     */
    public String[] genWords(int lexiconLength) throws Exception {
        results.clear();
        for (int w = 0; w < lexiconLength; w++) {
            if (abort) {
                return new String[0];
            }
            
            genNewWord();
        }
        
        return results.toArray(new String[0]);
    }
    
    /**
     * Output a pseudo-text
     * @return 
     * @throws java.lang.Exception 
     */
    public String createText() throws Exception {
        String text = "";
        
	for (int sent = 0; sent < SENTENCE_GEN_COUNT; sent++) {
            int nWord = 1 + peakedPowerLaw(15, 5, 50); 
            for (int w = 0; w < nWord; w++) {
                var nextWord = genNewWord();

                if (w == 0) {
                    nextWord = nextWord.substring(0, 1).toUpperCase() + nextWord.substring(1);
                }
                
                text += nextWord;
                
                if (w == nWord - 1) {
                    text += PUNCTUATION.charAt(powerLaw(PUNCTUATION.length(), 75)); 
                }
                
                text += " ";
            }
	}
        
        return text;
    }
    
    /**
     * Checks formatting of category rules
     * @param catCheck
     * @return 
     */
    public static boolean checkCatFormattingCorrect(String catCheck) {
        for (String line : catCheck.split("\n")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            
            if (!line.trim().matches("^[A-Z]=[^=]+$")) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns true if the illegal clusters input is legit
     * @param illegals
     * @return 
     */
    public static boolean checkIllegalsFormattingCorrect(String illegals) {
        return !illegals.contains(",");
    }
    
    /**
     * Checks formatting of rewrite rules
     * @param rewrites
     * @return 
     */
    public static boolean checkRewriteRules(String rewrites) {
        for (String line : rewrites.split("\n")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            
            if (!line.trim().matches("^[^\\|]+\\|[^\\|]*$") || line.contains(",")) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Technically, nothing is illegal here, but warn if users enter constants
     * @param catCheck
     * @param sylCheck
     * @return 
     */
    public static String checkSyllableRules(String catCheck, String sylCheck) {
        if (!checkCatFormattingCorrect(catCheck)) {
            return "Cannot check syllables while categories misformatted.";
        }
        
        String reducedRules = sylCheck.trim();
        
        for (String catLine : catCheck.split("\n")) {
            if (catLine.trim().isEmpty()) {
                continue;
            }
            String curCat = catLine.substring(0, catLine.indexOf("="));
            reducedRules = reducedRules.replaceAll(curCat, "").trim();
        }
        
        if (reducedRules.isEmpty()) {
            return "";
        }
        
        List<String> constants = new ArrayList<>();
        
        while (!reducedRules.isEmpty()) {
            String targetChar = reducedRules.substring(0, 1);
            constants.add(targetChar);
            reducedRules = reducedRules.replace(targetChar, "").trim();
        }
        
        return "WARNING: The following constant values are found in your syllable types: "
                + String.join(", ", constants);
    }
    
    private String[] getIllegalClusters(String rawIllegalClusters) {
        List<String> illegalClustersSet = new ArrayList<>();
        
        for (String illegal : rawIllegalClusters.split("\n")) {
            if (illegal.isBlank() || illegalClustersSet.contains(illegal)) {
                continue;
            }
            
            illegalClustersSet.add(illegal);
        }
        
        return illegalClustersSet.toArray(new String[0]);
    }
    
    private int getSyllableDropoffRate(boolean slowSyllables, int syllableLength) {
        int dropoffRate = 12;
        
        if (slowSyllables) {
            if (syllableLength < 9) {
                dropoffRate = 46 - syllableLength * 4;
            } else {
                dropoffRate = 11;
            }
        } else {
            if (syllableLength < 9) {
                dropoffRate = 60 - syllableLength * 5;
            }
        }
        
        return dropoffRate;
    }
    
    private Map<String, List<String>> parseCatrgories(String rawCategories) throws Exception {
        Map<String, List<String>> categoriesMap = new LinkedHashMap<>();
        
        for (String line : rawCategories.split("\n")) {
            line = line.trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            String[] split = line.split("=");
            
            if (split.length != 2) {
                throw new Exception("Improperly formatted categories");
            }

            categoriesMap.put(split[0], parseCategoryDefinition(split[1]));
        }
        
        return categoriesMap;
    }
    
    private List<String> parseCategoryDefinition(String rawDefinition) {
        List<String> definition = new ArrayList<>();
        String splitter = (rawDefinition.contains(",") ? "," : "(?!^)");
        definition.addAll(Arrays.asList(rawDefinition.split(splitter)));
        
        return definition;
    }

    private String getCategoryIndex() throws Exception {
        String index = "";

        for (String key : categories.keySet()) {
            index += key;
        }
        
        return index;
    }

    /**
     * Apply rewrite rules on just one string
     */
    private String applyRewriteRule(String s) {
        String newVal = s;
        
        for (String rwString : rewriteValues) {
            if (rwString.length() > 1 && rwString.contains("|")) {
                String[] parse = rwString.split("\\|");
                String replacement = parse.length > 1 ? parse[1] : "";
                newVal = newVal.replaceAll(parse[0], replacement);
            }
        }

        return newVal;
    }

    /**
     * Cheap iterative implementation of a power law: our chances of staying at
     * a bin are pct %.
     *
     * @param max
     * @param pct if this is over 100, this will run forever
     * @return
     */
    private int powerLaw(int max, int pct) {
        for (int r = 0; true; r = (r + 1) % max) {
            int randomPercent = (int) Math.floor(Math.random() * 101);
            if (randomPercent < pct) {
                return r;
            }
        }
    }
    
    /**
     * Similar, but there's a peak at mode.
     * @param max
     * @param mode
     * @param pct
     * @return 
     */
    private int peakedPowerLaw(int max, int mode, int pct) {
	if (Math.random() > 0.5) {
            // going upward from mode
            return mode + powerLaw(max - mode, pct);
	} else {
            // going downward from mode
            return mode - powerLaw(mode + 1, pct);
	}
    }

    /**
     * Output a single syllable - this is the guts of the program
     */
    private String createSyllable(String curVal) {
        // Choose the pattern
        int r = powerLaw(userSyllables.length, sylableDropoffRate);
        String pattern = userSyllables[r];

        // For each letter in the pattern, find the category
        for (int c = 0; c < pattern.length(); c++) {
            String theCat = pattern.substring(c, c + 1);
            // Go find it in the categories list
            int ix = categoryIndex.indexOf(theCat);
            if (ix == -1) {
                // Not found: output syllable directly
                curVal += theCat;
            } else {
                // Choose from this category
                List<String> expansion = categories.get(theCat);
                int r2;

                if (dropoff == 0) {
                    r2 = (int) (Math.random() * expansion.size());
                } else {
                    r2 = powerLaw(expansion.size(), dropoff);
                }

                curVal += expansion.get(r2);
            }
        }
        
        return curVal;
    }

    /**
     * Output a single word
     * Recurses, but will give up after enough retries if illegal clusters is too restrictive
     * @return generated value
     * @throws Exception 
     */
    private String genNewWord() throws Exception {
        return genNewWordRecurse(0);
    }

    private String genNewWordRecurse(int level) throws Exception {
        String curVal = "";
        
        if (level > MAX_RECURSE) {
            throw new Exception(
                    "Illegal Clusters settings too restrictive or too few possible combinations to generate desired number of entries.\n"
                            +"Try playing with settings to allow for more posibilities or reducing the target number.");
        }

        int nw = 1;
        if (monosyllableRarity > 0.0) {
            if (Math.random() > monosyllableRarity) {
                nw += 1 + powerLaw(4, 50);
            }
        }

        for (int w = 0; w < nw; w++) {
            curVal = createSyllable(curVal);
            
            if (syllableBreaks && w < nw - 1) {
                curVal += INTERPUNCT;
            }
        }
        
        curVal = applyRewriteRule(curVal);
        
        // once value is complete, make final inspection for illegal clusters and retry if appropriate
        if (containsIllegalCluster(curVal) || results.contains(curVal)) {
            genNewWordRecurse(level + 1);
        } else {
            this.addToResults(curVal);
        }
        
        return curVal;
    }

    /**
     * Generate all the syllables following a particular pattern, plus an
     * initial. 1. Look at the first item in pattern, e.g. V 2. For each member
     * m of that class (e.g. aeiou)… a. If it ends the pattern, just generate
     * the word initial + m b. If not, call genall recursively with m added to
     * the initial, and a pattern consisting of the rest of the string.
     *
     * @param initial
     * @param pattern
     */
    private void genall(String initial, String pattern) {
        if (pattern.length() == 0 || abort) {
            return;
        }

        String theCat = pattern.substring(0, 1);
        boolean lastOne = pattern.length() == 1;

        // Find category
        int ix = categoryIndex.indexOf(theCat);
        if (ix == -1) {
            // Not a category, just output it straight
            if (lastOne) {
                addToResults(applyRewriteRule(initial + theCat));
            } else {
                genall(initial + theCat, pattern.substring(1));
            }
        } else {
            // It's a category; iterate over its members
            List<String> members = categories.get(theCat);

            for (int i = 0; i < members.size(); i++) {
                String m = members.get(i);
                if (lastOne) {
                    addToResults(applyRewriteRule(initial + m));
                } else {
                    genall(initial + m, pattern.substring(1));
                }
            }
        }
    }
    
    private void addToResults(String value) {
        int count = results.size();
        
        // At key points, ask if user wishes to continue. After 10M they're on their own journey to hell.
        if (count == 1000000
                || count == 5000000
                || count == 10000000) {
            String dialog = "At " + count + " values. Continue?";
            
            if (count == 10000000) {
                dialog += "\nSeriouslty, last warning. It'll just go until it's done after this. PolyGlot might freeze.";
            }
            
            InfoBox info = osHandler.getInfoBox();
            if (info.yesNoCancel("Long Process", dialog)
                    != JOptionPane.YES_OPTION) {
                abort = true;
            }
        }
        
        if (value.trim().length() != 0 && !containsIllegalCluster(value)) {
            results.add(value);
        }
    }
    
    private boolean containsIllegalCluster(String test) {
        for (String illegalCluster : illegalClusters) {
            if (test.matches(illegalCluster) || test.contains(illegalCluster)) {
                return true;
            }
        }
        
        return false;
    }
}
