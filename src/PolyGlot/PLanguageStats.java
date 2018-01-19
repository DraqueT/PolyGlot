    /*
 * Copyright (c) 2017-2018, Draque Thompson
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
package PolyGlot;

import static PolyGlot.ManagersCollections.ConWordCollection.formatCon;
import static PolyGlot.ManagersCollections.ConWordCollection.formatPlain;
import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.PronunciationNode;
import PolyGlot.Nodes.TypeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DThompson
 */
public class PLanguageStats {

    /**
     * Builds report on words in ConLang. Potentially computationally expensive.
     *
     * @param core Core of language to analyze
     * @return
     */
    public static String buildWordReport(DictCore core) {
        PGooglePieChart typesPie = new PGooglePieChart("Word Counts by Part of Speech");
        PGoogleBarChart charStatBar = new PGoogleBarChart("Character Stats");

        String ret = "<!doctype html>\n"
                + "<html>"
                + "<meta charset=utf-8>\n";

        Map<String, Integer> wordStart = new HashMap<>();
        Map<String, Integer> wordEnd = new HashMap<>();
        Map<String, Integer> characterCombos2 = new HashMap<>();
        Integer highestCombo2 = 0;
        Map<String, Integer> characterCombos3 = new HashMap<>();
        Map<Integer, Integer> typeCountByWord = new HashMap<>();
        Map<String, Integer> phonemeCount = new HashMap<>();
        Map<String, Integer> charCount = new HashMap<>();
        Map<String, Integer> phonemeCombo2 = new HashMap<>();
        Integer wordCount = core.getWordCollection().getWordNodes().size();
        String allChars = core.getPropertiesManager().getAlphaPlainText();
        String alphabet = core.getPropertiesManager().getAlphaPlainText();

        Iterator<ConWord> wordIt = core.getWordCollection().getWordNodes().iterator();

        // Put values into maps to count/record... 
        while (wordIt.hasNext()) {
            ConWord curWord = wordIt.next();
            final String curValue = curWord.getValue();
            final int curValueLength = curValue.length();
            final int curType = curWord.getWordTypeId();

            // make sure we have all the characters in the word (if they forgot to populate one in their alpha order(
            for (char c : curValue.toCharArray()) {
                if (!allChars.contains(String.valueOf(c))) {
                    allChars += c;
                }
            }

            if (alphabet.length() == 0) {
                alphabet = allChars;
            }

            String beginsWith = curValue.substring(0, 1);
            String endsWith = curValue.substring(curValueLength - 1, curValueLength);

            // either increment or create value for starting character
            if (wordStart.containsKey(beginsWith)) {
                int newValue = wordStart.get(beginsWith) + 1;
                wordStart.remove(beginsWith);
                wordStart.put(beginsWith, newValue);
            } else {
                wordStart.put(beginsWith, 1);
            }

            // either increment or create value for ending character
            if (wordEnd.containsKey(endsWith)) {
                int newValue = wordEnd.get(endsWith) + 1;
                wordEnd.remove(endsWith);
                wordEnd.put(endsWith, newValue);
            } else {
                wordEnd.put(endsWith, 1);
            }

            // only run if no pronunciation recursion.
            if (!core.getPronunciationMgr().isRecurse()) {
                List<PronunciationNode> phonArray;

                // capture and record all phonemes in word and phoneme combinations
                try {
                    phonArray = core.getPronunciationMgr()
                            .getPronunciationElements(curValue);
                } catch (Exception e) {
                    // do nothing. This is just a report, users will be made aware
                    // of illegal pronunciation values elsewhere
                    phonArray = new ArrayList<>();
                }

                for (int i = 0; i < phonArray.size(); i++) {
                    if (phonemeCount.containsKey(phonArray.get(i).getPronunciation())) {
                        int newValue = phonemeCount.get(phonArray.get(i).getPronunciation()) + 1;
                        phonemeCount.remove(phonArray.get(i).getPronunciation());
                        phonemeCount.put(phonArray.get(i).getPronunciation(), newValue);
                    } else {
                        phonemeCount.put(phonArray.get(i).getPronunciation(), 1);
                    }

                    // grab combo if there are additinal phonemes, otherwise you're done
                    if (i + 1 < phonArray.size()) {
                        String curCombo = phonArray.get(i).getPronunciation() + " "
                                + phonArray.get(i + 1).getPronunciation();

                        if (phonemeCombo2.containsKey(curCombo)) {
                            int newValue = phonemeCombo2.get(curCombo) + 1;
                            phonemeCombo2.remove(curCombo);
                            phonemeCombo2.put(curCombo, newValue);
                        } else {
                            phonemeCombo2.put(curCombo, 1);
                        }
                    }
                }
            }

            // caupture all individual characters
            for (int i = 0; i < curValueLength; i++) {
                String curChar = curValue.substring(i, i + 1);

                if (charCount.containsKey(curChar)) {
                    int newValue = charCount.get(curChar) + 1;
                    charCount.remove(curChar);
                    charCount.put(curChar, newValue);
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

                    int newValue = characterCombos2.get(combo) + 1;
                    characterCombos2.remove(combo);
                    characterCombos2.put(combo, newValue);
                } else {
                    characterCombos2.put(combo, 1);
                }
            }

            // record type count...
            if (typeCountByWord.containsKey(curType)) {
                int newValue = typeCountByWord.get(curType) + 1;
                typeCountByWord.remove(curType);
                typeCountByWord.put(curType, newValue);
            } else {
                typeCountByWord.put(curType, 1);
            }
        }

        // build pie chart of type counts
        for (Map.Entry<Integer, Integer> curEntry : typeCountByWord.entrySet()) {
            TypeNode type = core.getTypes().getNodeById(curEntry.getKey());

            if (type != null) {
                String[] label = {type.getValue()};
                Double[] value = {(double) curEntry.getValue()};
                typesPie.addVal(label, value);
            }
        }

        // build bar chart of characters
        charStatBar.setLeftYAxisLabel("Starting With");
        charStatBar.setRightYAxisLabel("Total Count");
        charStatBar.setLabels(new String[]{"Words Starting With", "Overall Count"});
        charStatBar.setConFontName(core.getPropertiesManager().getFontCon().getFamily());
        for (char c : alphabet.toCharArray()) {
            String character = Character.toString(c);
            Double starting = 0.0;
            Double count = 0.0;

            if (wordStart.containsKey(character)) {
                starting = (double) wordStart.get(character);
            }

            if (charCount.containsKey(character)) {
                count = (double) charCount.get(character);
            }

            charStatBar.addVal(new String[]{character}, new Double[]{starting, count});
        }

        ret += "  <head>\n"
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
                + "  </head>\n"
                + "  <body style=\"font-family:" + core.getPropertiesManager().getCharisUnicodeFont().getFamily() + ";\">\n"
                + "    <center>---LANGUAGE STAT REPORT---</center><br><br>";

        ret += formatPlain("Count of words in conlang lexicon: " + wordCount + "<br><br>", core);

        ret += typesPie.getDisplayHTML();

        ret += charStatBar.getDisplayHTML();
        
        // buid grid of 2 letter combos
        char[] alphaGrid = core.getPropertiesManager().getAlphaPlainText().toCharArray();
        ret += formatPlain("Heat map of letter combination frequency:<br>", core);
        ret += "<table border=\"1\">";
        ret += "<tr><td></td>";
        for (char topRow : alphaGrid) {
            ret += "<td>" + formatCon(Character.toString(topRow), core) + "</td>";
        }
        ret += "</tr>";

        for (char y : alphaGrid) {
            ret += "<tr><td>" + formatCon(Character.toString(y), core) + "</td>";
            for (char x : alphaGrid) {
                String search = "" + x + y;
                Integer comboValue = (characterCombos2.containsKey(search)
                        ? characterCombos2.get(search) : 0);

                int red = (255 / highestCombo2) * comboValue;
                int blue = 255 - red;
                ret += "<td bgcolor=\"#" + Integer.toHexString(red)
                        + Integer.toHexString(blue) + Integer.toHexString(blue) + "\")>"
                        + formatCon(Character.toString(x) + Character.toString(y), core) + formatPlain(":"
                        + comboValue.toString(), core) + "</td>";
            }
            ret += "</tr>";
        }
        ret += "</table>" + formatPlain("<br><br>", core);

        // buid grid of 2 phoneme combos if no pronunciation recursion
        if (!core.getPronunciationMgr().isRecurse()) {
            ret += formatPlain("Heat map of phoneme combination frequency:<br>", core);
            ret += "<table border=\"1\">";
            ret += "<tr>" + formatPlain("<td></td>", core);
            Iterator<PronunciationNode> procIty = core.getPronunciationMgr().getPronunciations().iterator();
            while (procIty.hasNext()) {
                ret += "<td>" + formatPlain(formatPlain(procIty.next().getPronunciation(), core), core) + "</td>";
            }
            ret += "</tr>";
            procIty = core.getPronunciationMgr().getPronunciations().iterator();
            while (procIty.hasNext()) {
                PronunciationNode y = procIty.next();
                ret += "<tr><td>" + formatPlain(y.getPronunciation(), core) + "</td>";
                Iterator<PronunciationNode> procItx = core.getPronunciationMgr().getPronunciations().iterator();
                while (procItx.hasNext()) {
                    PronunciationNode x = procItx.next();
                    String search = x.getPronunciation() + " " + y.getPronunciation();
                    Integer comboValue = (phonemeCombo2.containsKey(search)
                            ? phonemeCombo2.get(search) : 0);

                    Integer red = (255 / highestCombo2) * comboValue;
                    Integer blue = 255 - red;
                    ret += "<td bgcolor=\"#" + Integer.toHexString(red)
                            + Integer.toHexString(blue) + Integer.toHexString(blue) + "\")>"
                            + formatPlain(x.getPronunciation() + y.getPronunciation() + ":"
                                    + comboValue.toString(), core) + "</td>";
                }
                ret += "</tr>";
            }
            ret += "</table>";
            
            // build display for phoneme count if no pronunciation recursion
            ret += formatPlain(" Breakdown of phonemes counted across all words:<br>", core);
            Iterator<PronunciationNode> procLoop = core.getPronunciationMgr().getPronunciations().iterator();
            while (procLoop.hasNext()) {
                PronunciationNode curNode = procLoop.next();
                ret += formatPlain(curNode.getPronunciation() + " : "
                        + (phonemeCount.containsKey(curNode.getPronunciation())
                        ? phonemeCount.get(curNode.getPronunciation()) : formatPlain("0", core)) + "<br>", core);
            }
            ret += formatPlain("<br><br>", core);
        }

        return ret;
    }
}
