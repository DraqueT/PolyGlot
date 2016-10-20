/*
 * Copyright (c) 2016, draque.thompson
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
package QuizEngine;

import PolyGlot.DictCore;
import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.DictNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
     * @param numQuestions number of question in the quiz (will be less if
     * filter too restrictive)
     * @param multipleChoice generates multiple choice questions if set. User
     * must fill in answers otherwise.
     * @param quizLocal whether to quiz on local word values of quiz words
     * @param partOfSpeech whether to quiz on part of speech
     * @param proc whether to quiz on pronunciation
     * @param def whether to quiz on definition (probably turn multiple choice
     * on for this one...
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
     * @param numQuestions number of question in the quiz (will be less if
     * filter too restrictive)
     * @param multipleChoice generates multiple choice questions if set. User
     * must fill in answers otherwise.
     * @param quizLocal whether to quiz on local word values of quiz words
     * @param partOfSpeech whether to quiz on part of speech
     * @param proc whether to quiz on pronunciation
     * @param wordClass whether to quiz on word classes
     * @param def whether to quiz on definition (probably turn multiple choice
     * on for this one...
     * @return a constructed quiz based on given parameters
     * @throws java.lang.Exception
     */
    public Quiz generateLexicalQuiz(int numQuestions, boolean multipleChoice,
            boolean quizLocal, boolean partOfSpeech, boolean proc, boolean def,
            boolean wordClass, ConWord filter) throws Exception {
        Quiz ret = new Quiz(core);
        List<ConWord> wordList;
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

        if (filter == null) {
            wordList = core.getWordCollection().getWordNodes();
        } else {
            wordList = core.getWordCollection().filteredList(filter);
        }

        Collections.shuffle(wordList, new Random(System.nanoTime()));

        // make certain the number of questions never exceeds the number of words available
        numQuestions = wordList.size() < numQuestions ? wordList.size() : numQuestions;
        Random randGen = new Random();

        for (int i = 0; i < numQuestions; i++) {
            ConWord curWord = wordList.get(i);
            QuizQuestion.QuestionType questionType = quizOn.get(randGen.nextInt(quizOn.size()));
            QuizQuestion question = new QuizQuestion(core.getPropertiesManager().isIgnoreCase());

            switch (questionType) {
                case Local:
                    for (DictNode node : core.getWordCollection().getRandomNodes(numChoices - 1)) {
                        question.addChoice(node);
                    }
                    question.addChoice(curWord);
                    question.setAnswer(curWord);
                    break;
                case PoS:
                    break;
                case Proc:
                    break;
                case Def:
                    break;
                case Classes:
                    break;
                default:
                    throw new Exception("Unhandled question type.");
            }

            ret.addNode(question);
        }

        return ret;
    }
}
