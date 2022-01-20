/*
 * Copyright (c) 2020 - 2021, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.ConjugationGenRule;
import org.darisadesigns.polyglotlina.Nodes.ConjugationGenTransform;
import org.darisadesigns.polyglotlina.Nodes.DictNode;
import org.darisadesigns.polyglotlina.Nodes.LexiconProblemNode;
import org.darisadesigns.polyglotlina.Nodes.LexiconProblemNode.ProblemType;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;

/**
 * This checks a given language file for correctable errors
 *
 * @author draque
 */
public class CheckLanguageErrors {

    /**
     * Checks the lexicon for erroneous values & returns values
     *
     * @param core core to check
     * @param display whether to make a visual display of this
     * @return
     */
    public static LexiconProblemNode[] checkCore(DictCore core, boolean display) {
        List<LexiconProblemNode> problems = new ArrayList<>();

        try {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    // cycle through each word individually, searching for problems
                    checkLexicon(core, problems);

                    // gather any etymological loops (illegal, as word cannot be its own ancestor) and record them
                    checkEtyLoops(core, problems);

                    // test all PoS
                    checkPos(core, problems);

                    // test conjugation rules regex patterns
                    checkConjugationRules(core, problems);

                    // Check Phonology
                    checkPhonology(core, problems);
                    
                    // Check Alphabet
                    checkAlphabet(core, problems);
                }
            };

            thread.start();
            thread.join();
        }
        catch (InterruptedException e) {
            core.getOSHandler().getIOHandler().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Thread Error", "Language validation thread error: " + e.getLocalizedMessage());
        }

        Collections.sort(problems);

        if (!problems.isEmpty() && display) {
            core.getOSHandler().openLanguageProblemDisplay(problems, core);
        } else if (display) {
            core.getOSHandler().getInfoBox().info("Lexicon Check Results", "No problems found in language file!");
        }

        return problems.toArray(new LexiconProblemNode[0]);
    }
    
    private static void checkAlphabet(DictCore core, List<LexiconProblemNode> problems) {
        String[] alpha = core.getPropertiesManager().getOrderedAlphaList();
        
        // checks for ambiguity in alphabet
        for (int i = 0; i < alpha.length; i++) {
            for (int j = 0; j < alpha.length; j++) {
                if (i == j) {
                    continue;
                }
                
                if (alpha[i].startsWith(alpha[j]) || alpha[i].endsWith(alpha[j])) {
                    var problem = new LexiconProblemNode(
                            new AlphaProblem(alpha[i], alpha[j]), 
                            "Your alphabet creates ambiguity. The characters representing " +
                                    "one letter are a prefix or suffix to another, which may " +
                                    "lead to things like inconsistent alphabetic order.",
                            ProblemType.Alphabet,
                            LexiconProblemNode.SEVARITY_WARNING
                    );
                    
                    problems.add(problem);
                }
            }
        }
    }

    private static void checkPhonology(DictCore core, List<LexiconProblemNode> problems) {
        for (PronunciationNode node : core.getPronunciationMgr().getPronunciations()) {
            String problemDescription = "";
            
            if (!RegexTools.isRegexLegal(node.getValue())) {
                problemDescription = "Pronunciation regex: \"" + node.getValue() + "\" is illegal.";
            }
            
            if (!RegexTools.isRegexLegal(node.getPronunciation())) {
                problemDescription = "Pronunciation text: \"" + node.getPronunciation() + "\" is illegal regex insertion.";
            }
            
            if (!core.getPGTUtil().isBlank(problemDescription)) {
                problems.add(
                        new LexiconProblemNode(
                                node, 
                                problemDescription, 
                                ProblemType.Phonology, 
                                LexiconProblemNode.SEVARITY_ERROR
                        )
                );
            }
        }
        
        for (PronunciationNode node : core.getRomManager().getPronunciations()) {
            String problemDescription = "";
            
            if (!RegexTools.isRegexLegal(node.getValue())) {
                problemDescription = "Romanization regex: \"" + node.getValue() + "\" is illegal.";
            }
            
            if (!RegexTools.isRegexLegal(node.getPronunciation())) {
                problemDescription = "Romanization value: \"" + node.getValue() + "\" is illegal regex insertion.";
            }
            
            if (!core.getPGTUtil().isBlank(problemDescription)) {
                problems.add(new LexiconProblemNode(
                        node, 
                        problemDescription, 
                        ProblemType.Phonology,
                        LexiconProblemNode.SEVARITY_ERROR
                ));
            }
        }
    }
    
    private static void checkConjugationRules(DictCore core, List<LexiconProblemNode> problems) {
        ConjugationManager conjMan = core.getConjugationManager();
        for (TypeNode pos : core.getTypes().getAllValues()) {
            int posId = pos.getId();
            for (ConjugationGenRule rule : conjMan.getConjugationRulesForType(posId)) {
                String conjName = conjMan.getCombNameFromCombId(pos.getId(), rule.getCombinationId());

                String problemDescription = "";

                if (!RegexTools.isRegexLegal(rule.getRegex())) {
                    problemDescription += "The regex filter pattern \"" + rule.getRegex() + "\" within rule "
                            + rule.getName() + " of " + conjName + " is illegal.";
                }

                // need to test all transformations within rule now
                for (ConjugationGenTransform transform : rule.getTransforms()) {
                    if (!RegexTools.isRegexLegal(transform.regex)) {
                        problemDescription += "\nThe regex transform \"" + transform.regex + "\" within rule "
                                + rule.getName() + " of " + conjName + " is illegal.";
                    }

                    if (!RegexTools.isRegexLegal(transform.replaceText)) {
                        problemDescription += "\nThe replacement text \"" + transform.replaceText + "\" within rule "
                                + rule.getName() + " is illegal.";
                    }
                }

                if (!core.getPGTUtil().isBlank(problemDescription)) {
                    problems.add(new LexiconProblemNode(
                            pos,
                            problemDescription,
                            ProblemType.PoS, 
                            LexiconProblemNode.SEVARITY_ERROR
                    ));
                }
            }
        }
    }

    private static void checkPos(DictCore core, List<LexiconProblemNode> problems) {
        for (TypeNode node : core.getTypes().getAllValues()) {
            if (!RegexTools.isRegexLegal(node.getPattern())) {
                problems.add(new LexiconProblemNode(
                        node,
                        "Illegal regex value: \"" + node.getPattern() + "\"",
                        ProblemType.PoS, 
                        LexiconProblemNode.SEVARITY_ERROR
                ));
            }
        }
    }

    private static void checkEtyLoops(DictCore core, List<LexiconProblemNode> problems) {
        for (ConWord loopWord : core.getEtymologyManager().checkAllForIllegalLoops()) {
            problems.add(new LexiconProblemNode(
                    loopWord,
                    "This word is included in an illegal etymological loop. "
                    + "Select the word in the lexicon then click the Etymology button to correct.",
                    ProblemType.ConWord,
                    LexiconProblemNode.SEVARITY_ERROR
            ));
        }
    }

    private static void checkLexicon(DictCore core, List<LexiconProblemNode> problems) {
        ConWordCollection wordCollection = core.getWordCollection();
        Map<String, Integer> conWordCount = wordCollection.getConWordCount();
        Map<String, Integer> localWordCount = wordCollection.getLocalCount();

        for (ConWord curWord : wordCollection.getAllValues()) {
            String problemString = "";

            // check word legality (if not overridden)
            if (!curWord.isRulesOverride()) {
                ConWord testLegal = wordCollection.testWordLegality(curWord, conWordCount, localWordCount);

                problemString += testLegal.getValue().isEmpty() ? "" : testLegal.getValue() + "\n";
                problemString += testLegal.typeError.isEmpty() ? "" : testLegal.typeError + "\n";
                problemString += testLegal.getLocalWord().isEmpty() ? "" : testLegal.getLocalWord() + "\n";
                problemString += testLegal.getDefinition().isEmpty() ? "" : testLegal.getDefinition() + "\n";
            }

            // check word made up of defined characters (document if not) if alphabet defined
            if (!core.getPropertiesManager().getAlphaOrder().isEmpty()
                    && !core.getPropertiesManager().testStringAgainstAlphabet(curWord.getValue())) {
                problemString += "Word contains characters undefined in alphabet settings.\n";
                problemString += "Suspect characters:\""
                        + core.getPropertiesManager().findBadLetters(curWord.getValue())
                        + "\"\n";
            }

            // check word pronunciation can be generated (if pronunciations set up and not overridden)
            if (core.getPronunciationMgr().isInUse()) {
                try {
                    if (core.getPronunciationMgr().getPronunciation(curWord.getValue()).isEmpty()) {
                        problemString += "Word pronunciation cannot be generated properly (missing regex pattern).\n";
                    }
                }
                catch (Exception e) {
                    problemString += "Word encountered malformed regex when generating pronunciation.\n";
                    // IOHandler.writeErrorLog(e);
                }
            }

            // check word romanization can be generated (if rominzations set up)
            if (core.getRomManager().isEnabled()) {
                try {
                    if (core.getRomManager().getPronunciation(curWord.getValue()).isEmpty()) {
                        problemString += "Word cannot be romanized properly (missing regex pattern).\n";
                    }
                }
                catch (Exception e) {
                    problemString += "Word encounters malformed regex when generating Romanization.\n";
                    // IOHandler.writeErrorLog(e);
                }
            }

            // record results of each for report
            if (!problemString.trim().isEmpty()) {
                problems.add(new LexiconProblemNode(
                        curWord,
                        problemString.trim(),
                        ProblemType.ConWord, 
                        LexiconProblemNode.SEVARITY_ERROR
                ));
            }
        }
    }
    
    public static class AlphaProblem extends DictNode {
        private String secondVal;
        
        public AlphaProblem(String _value, String _secondVal) {
            value = _value;
            secondVal = _secondVal;
        }
        
        public String getSecondVal() {
            return secondVal;
        }

        @Override
        public boolean equals(Object comp) {
            if (comp instanceof AlphaProblem) {
                AlphaProblem alphaComp = (AlphaProblem)comp;
                
                return value.equals(alphaComp.value) 
                        && secondVal.equals(((AlphaProblem) comp).secondVal);
            }
            
            return false;
        }

        @Override
        public void setEqual(DictNode _node) throws ClassCastException {
            AlphaProblem node = (AlphaProblem)_node;
            
            value = node.value;
            secondVal = node.secondVal;
        }
        
    }
}
