/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.QuizEngine;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.PEntry;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.Nodes.WordClassValue;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

/**
 * This class allows for the creation of various quiz types
 *
 * @author draque.thompson
 */
public class QuizFactory {

    private final DictCore core;
    private int numChoices = 4;

    public QuizFactory(DictCore _core) {
        core = _core;
    }

    /**
     * Changes the number of multiple choice answers (defaults to 4)
     *
     * @param _numChoices
     */
    public void setNumChoices(int _numChoices) {
        numChoices = _numChoices;
    }

    /**
     * This randomly creates a quiz based on words from your language
     *
     * @param numQuestions number of question in the quiz (will be less if filter too restrictive)
     * @param multipleChoice generates multiple choice questions if set. User must fill in answers otherwise.
     * @param quizLocal whether to quiz on local word values of quiz words
     * @param partOfSpeech whether to quiz on part of speech
     * @param proc whether to quiz on pronunciation
     * @param def whether to quiz on definition (probably turn multiple choice on for this one...
     * @param wordClass whether to quiz on word classes
     * @return a constructed quiz based on given parameters
     * @throws java.lang.Exception
     */
    public Quiz generateLexicalQuiz(int numQuestions, boolean multipleChoice,
            boolean quizLocal, boolean partOfSpeech, boolean proc, boolean def,
            boolean wordClass) throws Exception {
        return generateLexicalQuiz(numQuestions, multipleChoice, quizLocal, partOfSpeech,
                proc, def, wordClass, null);
    }

    /**
     * This randomly creates a quiz based on words from your language
     *
     * @param filter a filter conword, which quiz words must match
     * @param conFromDef
     * @param numQuestions number of question in the quiz (will be less if filter too restrictive)
     * @param quizLocal whether to quiz on local word values of quiz words
     * @param partOfSpeech whether to quiz on part of speech
     * @param proc whether to quiz on pronunciation
     * @param wordClass whether to quiz on word classes
     * @param def whether to quiz on definition (probably turn multiple choice on for this one...
     * @return a constructed quiz based on given parameters
     * @throws java.lang.Exception
     */
    public Quiz generateLexicalQuiz(int numQuestions, boolean conFromDef,
            boolean quizLocal, boolean partOfSpeech, boolean proc, boolean def,
            boolean wordClass, ConWord filter) throws Exception {
        Quiz ret = new Quiz(core);
        ConWord[] wordList;
        List<QuizQuestion.QuestionType> quizOn = new ArrayList<>();

        if (quizLocal) {
            quizOn.add(QuizQuestion.QuestionType.Local);
        }
        if (partOfSpeech) {
            quizOn.add(QuizQuestion.QuestionType.PoS);
        }
        if (proc) {
            quizOn.add(QuizQuestion.QuestionType.Proc);
        }
        if (def) {
            quizOn.add(QuizQuestion.QuestionType.Def);
        }
        if (wordClass) {
            quizOn.add(QuizQuestion.QuestionType.Classes);
        }
        if (conFromDef) {
            quizOn.add(QuizQuestion.QuestionType.ConEquiv);
        }

        if (filter == null) {
            wordList = core.getWordCollection().getWordNodes();
        } else {
            wordList = core.getWordCollection().filteredList(filter);
        }

        // shuffle by converting to list, then back...
        List<ConWord> shuffleList = Arrays.asList(wordList);
        Collections.shuffle(shuffleList, new Random(System.nanoTime()));
        wordList = shuffleList.toArray(new ConWord[0]);

        // make certain the number of questions never exceeds the number of words available
        numQuestions = Math.min(wordList.length, numQuestions);
        Random randGen = new Random();

        // make certain word properties have all combos built before making quiz
        core.getWordClassCollection().buildComboCache();

        for (int i = 0; i < numQuestions; i++) {
            ConWord curWord = wordList[i];
            QuizQuestion.QuestionType questionType = quizOn.get(randGen.nextInt(quizOn.size()));
            QuizQuestion question = new QuizQuestion(core);
            question.setType(questionType);
            question.setSource(curWord);

            switch (questionType) {
                case Local:
                case Proc:
                case Def:
                case ConEquiv:
                    core.getWordCollection().getRandomNodes(numChoices - 1, curWord.getId()).forEach((node) -> {
                        question.addChoice(node);
                    });
                    question.addChoice(curWord);
                    question.setAnswer(curWord);
                    question.setSource(curWord);
                    break;
                case PoS:
                    core.getTypes().getRandomNodes(numChoices - 1, curWord.getWordTypeId()).forEach((node) -> {
                        question.addChoice(node);
                    });
                    TypeNode typeAnswer = core.getTypes().getNodeById(curWord.getWordTypeId());
                    question.addChoice(typeAnswer);
                    question.setAnswer(typeAnswer);
                    question.setSource(curWord);
                    break;
                case Classes:
                    int curId = 0;
                    for (List<PEntry<Integer, Integer>> curCombo
                            : core.getWordClassCollection()
                                    .getRandomPropertyCombinations(numChoices - 1, curWord)) {
                        curId++;
                        WordClassValue choiceNode = new WordClassValue();

                        for (PEntry<Integer, Integer> curEntry : curCombo) {
                            WordClass wordProp = (WordClass) core.getWordClassCollection().getNodeById(curEntry.getKey());
                            WordClassValue valueNode = wordProp.getValueById(curEntry.getValue());

                            if (!choiceNode.getValue().isEmpty()) {
                                choiceNode.setValue(choiceNode.getValue() + ", ");
                            }

                            choiceNode.setValue(choiceNode.getValue() + valueNode.getValue());
                        }

                        choiceNode.setId(curId);
                        question.addChoice(choiceNode);
                    }

                    WordClassValue valAnswer = new WordClassValue();
                    Iterator<Entry<Integer, Integer>> propIt = curWord.getClassValues().iterator();

                    while (propIt.hasNext()) {
                        Entry<Integer, Integer> curEntry = propIt.next();
                        WordClass curProp = (WordClass) core.getWordClassCollection().getNodeById(curEntry.getKey());
                        WordClassValue curVal = curProp.getValueById(curEntry.getValue());

                        if (!valAnswer.getValue().isEmpty()) {
                            valAnswer.setValue(valAnswer.getValue() + ", ");
                        }

                        valAnswer.setValue(valAnswer.getValue() + curVal.getValue());
                    }
                    valAnswer.setId(0);

                    // handle case of words with no class
                    if (valAnswer.getValue().isEmpty()) {
                        valAnswer.setValue("No Class");
                    }

                    question.addChoice(valAnswer);
                    question.setAnswer(valAnswer);
                    question.setSource(curWord);
                    break;
                default:
                    throw new Exception("Unhandled question type.");
            }

            ret.addNode(question);
        }

        // clear combo cache from memory after done
        core.getWordClassCollection().clearComboCache();

        return ret;
    }
}
