/*
 * Copyright (c) 2017-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina;

import java.util.ArrayList;
import java.util.Collections;
import static org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection.formatCon;
import static org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection.formatPlain;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.Screens.ScrProgressMenu;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DThompson
 */
public final class PLanguageStats {
    
    private final PGooglePieChart typesPie = new PGooglePieChart("Word Counts by Part of Speech");
    private final PGoogleBarChart charStatBar = new PGoogleBarChart("Character Stats");
    private final DictCore core;
    private final ScrProgressMenu progress;
    private final Map<String, Integer> wordStart = new HashMap<>();
    private final Map<String, Integer> letterCount = new HashMap<>();
    private final Map<String, Integer> letterComboCount = new HashMap<>();
    private final Map<Integer, Integer> typeCountByWord = new HashMap<>();
    private final Map<String, Integer> phonemeCount = new HashMap<>();
    private final Map<String, Integer> phonemeComboCount = new HashMap<>();
    private final ConWord[] wordList;
    private final String[] alphabet;
    private final String[] alphaCombinations;
    
    private PLanguageStats(DictCore _core, ScrProgressMenu _progress) {
        core = _core;
        progress = _progress;
        wordList = core.getWordCollection().getWordNodes();
        
        String rawAlphabet = core.getPropertiesManager().getAlphaPlainText();
        String splitRegex = rawAlphabet.contains(",") ? "," : "(?!^)";
        alphabet = rawAlphabet.split(splitRegex);
        alphaCombinations = getAllCombinations();
    } 

    /**
     * Builds report on words in ConLang. Potentially computationally expensive.
     *
     * @param core Core of language to analyze
     * @return
     */
    public static String buildWordReport(DictCore core) {
        final String[] ret = new String[1]; // using array so I can set value in a thread...
        ret[0] = "";

        try {
            final int wordCount = core.getWordCollection().getWordCount();
            final ScrProgressMenu progress = ScrProgressMenu.createScrProgressMenu("Generating Language Stats", wordCount + 5, true, true);
            progress.setVisible(true);

            // unnessecary to test UI positioning here (and no root window in tests)
            if (core.getPolyGlot().getRootWindow() != null) {
                progress.setLocation(core.getPolyGlot().getRootWindow().getLocation());
            }

            Thread thread = new Thread() {
                @Override
                public void run() {
                    ret[0] = new PLanguageStats(core, progress).buildWordReport();
                }
            };

            thread.start();
            thread.join();
        }
        catch (InterruptedException e) {
            core.getOSHandler().getIOHandler().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Language Stat Error", "Unable to generate language statistics: " + e.getLocalizedMessage());
        }

        return ret[0];
    }

    private String buildWordReport() {
        String ret = "<!doctype html>\n"
                + "<html>"
                + "<meta charset=utf-8>\n";

        ret += core.getPropertiesManager().buildPropertiesReportTitle();

        collectValuesFromWords();

        progress.iterateTask("Building report...");

        // build pie chart of type counts
        typeCountByWord.entrySet().forEach((curEntry) -> {
            TypeNode type = core.getTypes().getNodeById(curEntry.getKey());
            if (type != null) {
                String[] label = {type.getValue()};
                Double[] value = {(double) curEntry.getValue()};
                typesPie.addVal(label, value);
            }
        });

        // build bar chart of characters
        charStatBar.setLeftYAxisLabel("Starting With");
        charStatBar.setRightYAxisLabel("Overall Count");
        charStatBar.setLabels(new String[]{"Words Starting With", "Overall Count"});
        charStatBar.setConFontName(core.getPropertiesManager().getFontCon().getFamily());
        for (String character : alphabet) {
            double starting = 0.0;
            double count = 0.0;

            if (wordStart.containsKey(character)) {
                starting = (double) wordStart.get(character);
            }

            if (letterCount.containsKey(character)) {
                count = (double) letterCount.get(character);
            }

            charStatBar.addVal(new String[]{character}, new Double[]{starting, count});
        }

        ret += "\n"
                + "    <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>\n"
                + "    <script type=\"text/javascript\">\n"
                + "      google.charts.load('current', {'packages':['corechart']});\n"
                + "      google.charts.load('current', {'packages':['corechart', 'bar']});\n"
                + "      google.charts.setOnLoadCallback(" + typesPie.getFunctionName() + ");\n"
                + "      google.charts.setOnLoadCallback(" + charStatBar.getFunctionName() + ");\n"
                + "\n";

        ret += typesPie.getBuildHTML();
        ret += charStatBar.getBuildHTML();

        ret += "    </script>\n"
                + "  <body style=\"font-family:" + core.getPropertiesManager().getFontLocal().getFamily() + ";\">\n"
                + "    <center>---LANGUAGE STAT REPORT---</center><br><br>";

        ret += formatPlain("Count of words in conlang lexicon: " + wordList.length + "<br><br>", core);

        progress.iterateTask("Building charts...");
        ret += typesPie.getDisplayHTML();

        ret += charStatBar.getDisplayHTML();

        // build list of conlang characters and the IPA characters they express
        ret += "<p>" + formatPlain("List of letters to IPA sounds which they can express", core) + "<br>";

        Map<String, String[]> charsToIpa = core.getPronunciationMgr().getIpaSoundsPerCharacter();
        for (String key : charsToIpa.keySet()) {
            String sanitizedKey = WebInterface.encodeHTML(key);
            ret += "<br>" + formatCon(sanitizedKey, core) + formatPlain(" : ", core);

            String[] ipaChars = charsToIpa.get(key);
            for (int i = 0; i < ipaChars.length; i += 2) {
                String value = WebInterface.encodeHTML(ipaChars[i]);
                ret += formatPlain(value, core) + " ";
            }
        }

        ret += "</p>";

        progress.iterateTask("Building character combo grid...");
        ret += buildLetterComboTable();

        // only build phoneme table and count of not using recursion
        progress.iterateTask("Building phoneme combo grid...");
        if (!core.getPronunciationMgr().isRecurse()) {
            ret += buildPhonemeTable();
            ret += buildPhonemeCount();
        }

        progress.iterateTask("DONE!");

        return ret;
    }
    
    private String buildLetterComboTable() {
        String ret = "";

        ret += formatPlain("Heat map of letter combination frequency:<br>", core);
        ret += "<table border=\"1\">";
        ret += "<tr><td></td>";
        for (String columnsHead : alphabet) {
            String cleanedHead = WebInterface.encodeHTML(columnsHead);
            ret += "<td>" + formatCon(cleanedHead, core) + "</td>";
        }

        int highestLetterComboCount = getHighestCount(letterComboCount);

        ret += "</tr>";
        for (String y : alphabet) {
            String cleanedY = WebInterface.encodeHTML(y);
            ret += "<tr><td>" + formatCon(cleanedY, core) + "</td>";
            for (String x : alphabet) {
                String search = x + y;
                Integer comboValue = 0;

                if (letterComboCount.containsKey(search)) {
                    Integer tmp = letterComboCount.get(search);
                    if (tmp != null) {
                        comboValue = tmp;
                    }
                }

                int red = (255 / highestLetterComboCount) * comboValue;
                int blue = 255 - red;
                String format = "%02X"; // 2 digit hex format
                String comboStringCleaned = WebInterface.encodeHTML(x + y);
                ret += "<td bgcolor=\"#" + String.format(format, red)
                        + String.format(format, blue)
                        + String.format(format, blue) + "\")>"
                        + formatCon(comboStringCleaned, core) + formatPlain(":"
                        + comboValue, core) + "</td>";

            }
            ret += "</tr>";
        }
        ret += "</table>" + formatPlain("<br><br>", core);

        return ret;
    }

    private void collectValuesFromWords() {
        for (ConWord curWord : core.getWordCollection().getWordNodes()) {
            final String curValue = curWord.getValue();
            final int curType = curWord.getWordTypeId();

            progress.iterateTask("Analyzing: " + curWord.getValue());

            String startsWith = startsWith(curValue);

            // either increment or create value for starting character
            if (wordStart.containsKey(startsWith)) {
                int newValue = wordStart.get(startsWith) + 1;
                wordStart.remove(startsWith);
                wordStart.put(startsWith, newValue);
            } else {
                wordStart.put(startsWith, 1);
            }

            // only collect phoneme combos if no pronunciation recursion.
            if (!core.getPronunciationMgr().isRecurse()) {
                PronunciationNode[] phonArray;

                // capture and record all phonemes in word and phoneme combinations
                try {
                    phonArray = core.getPronunciationMgr()
                            .getPronunciationElements(curValue);
                }
                catch (Exception e) {
                    // do nothing. This is just a report, users will be made aware
                    // of illegal pronunciation values elsewhere
                    // IOHandler.writeErrorLog(e);
                    phonArray = new PronunciationNode[0];
                }

                for (int i = 0; i < phonArray.length; i++) {
                    if (phonemeCount.containsKey(phonArray[i].getPronunciation())) {
                        int newValue = phonemeCount.get(phonArray[i].getPronunciation()) + 1;
                        phonemeCount.remove(phonArray[i].getPronunciation());
                        phonemeCount.put(phonArray[i].getPronunciation(), newValue);
                    } else {
                        phonemeCount.put(phonArray[i].getPronunciation(), 1);
                    }

                    // grab combo if there are additional phonemes, otherwise you're done
                    if (i + 1 < phonArray.length) {
                        String curCombo = phonArray[i].getPronunciation() + " "
                                + phonArray[i + 1].getPronunciation();

                        if (phonemeComboCount.containsKey(curCombo)) {
                            int newValue = phonemeComboCount.get(curCombo) + 1;
                            phonemeComboCount.remove(curCombo);
                            phonemeComboCount.put(curCombo, newValue);
                        } else {
                            phonemeComboCount.put(curCombo, 1);
                        }
                    }
                }
            }

            // capture all individual characters
            addCharacterStringsToMap(letterCount, alphabet, curValue);

            // capture and record all 2 character combinations in words
            addCharacterStringsToMap(letterComboCount, alphaCombinations, curValue);

            // record type count...
            if (typeCountByWord.containsKey(curType)) {
                int newValue = typeCountByWord.get(curType) + 1;
                typeCountByWord.remove(curType);
                typeCountByWord.put(curType, newValue);
            } else {
                typeCountByWord.put(curType, 1);
            }
        }
    }
    
    private String buildPhonemeTable() {
        String ret = "";
        
        ret += formatPlain("Heat map of phoneme combination frequency:<br>", core);
        ret += "<table border=\"1\">";
        ret += "<tr>" + formatPlain("<td></td>", core);

        for (PronunciationNode curNode : core.getPronunciationMgr().getPronunciations()) {
            String procCleaned = WebInterface.encodeHTML(curNode.getPronunciation());
            ret += "<td>" + formatPlain(formatPlain(procCleaned, core), core) + "</td>";
        }
        ret += "</tr>";

        int highestPhonemeComboCount = getHighestCount(phonemeComboCount);
        for (PronunciationNode y : core.getPronunciationMgr().getPronunciations()) {
            String procYCleaned = WebInterface.encodeHTML(y.getPronunciation());
            ret += "<tr><td>" + formatPlain(procYCleaned, core) + "</td>";

            for (PronunciationNode x : core.getPronunciationMgr().getPronunciations()) {
                String search = x.getPronunciation() + " " + y.getPronunciation();
                Integer comboValue = 0;
                if (phonemeComboCount.containsKey(search)) {
                    Integer tmp = phonemeComboCount.get(search);
                    if (tmp != null) {
                        comboValue = tmp;
                    }
                }

                // This is fine because these should correlate with the letter combo counts.
                int red = (255 / highestPhonemeComboCount) * comboValue;
                int blue = 255 - red;
                String procComboCleaned = WebInterface.encodeHTML(x.getPronunciation() + y.getPronunciation());
                ret += "<td bgcolor=\"#" + Integer.toHexString(red)
                        + Integer.toHexString(blue) + Integer.toHexString(blue) + "\")>"
                        + formatPlain(procComboCleaned + ":"
                                + comboValue, core) + "</td>";
            }
            ret += "</tr>";
        }
        ret += "</table>";
        
        return ret;
    }
    
    private String buildPhonemeCount() {
        String ret = "";
        
        ret += formatPlain("<br>Breakdown of phonemes counted across all words:<br>", core);
        for (PronunciationNode curNode : core.getPronunciationMgr().getPronunciations()) {
            String procCleaned = WebInterface.encodeHTML(curNode.getPronunciation());
            ret += formatPlain(procCleaned + " : "
                    + (phonemeCount.containsKey(curNode.getPronunciation())
                    ? phonemeCount.get(curNode.getPronunciation()) : formatPlain("0", core)) + "<br>", core);
        }
        ret += formatPlain("<br><br>", core);
        
        return ret;
    }

    /**
     * Given a word and an alphabet, find which letter (potentially
     * multi-character) it begins with
     *
     * @param word
     * @param alphabet
     * @return
     */
    private String startsWith(String word) {
        String ret = word.substring(0, 1);

        for (String letter : alphabet) {
            if (word.startsWith(letter)) {
                ret = letter;
                break;
            }
        }

        return ret;
    }

    /**
     * Given a word and an alphabet, find which letter (potentially
     * multi-character) it begins with
     *
     * @param word
     * @param alphabet
     * @return
     */
    private String endsWith(String word) {
        int length = word.length();
        String ret = word.substring(length - 1, length);

        for (String letter : alphabet) {
            if (word.endsWith(letter)) {
                ret = letter;
                break;
            }
        }

        return ret;
    }

    /**
     * Adds all character strings found in word string to map passed in. This
     * works via side effect because rebuilding the map every run is expensive.
     *
     * @param letterCombos
     * @param combinations
     * @param word
     */
    private void addCharacterStringsToMap(Map<String, Integer> letterCombos, String[] combinations, String word) {
        String value = word;

        while (!value.isBlank()) {
            int startLength = value.length();

            for (String combo : combinations) {
                if (value.startsWith(combo)) {
                    // remove found combo from string
                    value = value.substring(combo.length());

                    if (letterCombos.containsKey(combo)) {
                        //letterCombos.replace(combo, letterCombos.get(combo) + 1);
                        int curCount = letterCombos.get(combo);
                        letterCombos.replace(combo, curCount + 1);
                        continue;
                    }
                    letterCombos.put(combo, 1);
                }
            }

            if (startLength == value.length()) {
                // current character not in defined alphabet - skip
                value = value.substring(1);
            }
        }
    }

    private int getHighestCount(Map<String, Integer> toCount) {
        int ret = 1;

        List<Integer> values = new ArrayList<>(toCount.values());

        if (values.size() > 0) {
            Collections.sort(values, Collections.reverseOrder());
            ret = values.get(0);
        }

        return ret;
    }

    /**
     * Gets every combination of characters and returns as an array
     *
     * @param combinations
     * @return
     */
    private String[] getAllCombinations() {
        List<String> ret = new ArrayList<>();

        for (String first : alphabet) {
            for (String second : alphabet) {
                ret.add(first + second);
            }
        }

        return ret.toArray(new String[0]);
    }
}
