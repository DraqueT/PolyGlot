/*
 * Copyright (c) 2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.QuizEngine;

import TestResources.DummyCore;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class QuizTest {
    DictCore core;
    Quiz quiz;
    
    public QuizTest() {
    }
    
    @BeforeEach
    public void setUp() {
        core = DummyCore.newCore();
        quiz = new Quiz(core);
        
        try {
            for (int i =0; i < 10; i++) {
                ConWord word = new ConWord();
                word.setCore(core);
                word.setValue(i + " CON");
                word.setLocalWord(i + " LOCAL");

                QuizQuestion question = new QuizQuestion(core);
                question.addChoice(word);
                question.setAnswer(word);

                for (int j = 0; j < 3; j++) {
                    ConWord wrong = new ConWord();
                    wrong.setCore(core);
                    wrong.setValue(i + ":" + j + " WRONG CON");
                    wrong.setLocalWord(i + ":" + j + " WRONG LOCAL");
                    question.addChoice(wrong);
                }

                quiz.addNode(question);
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test of getLength method, of class Quiz.
     */
    @Test
    public void testGetLength() {
        System.out.println("QuizTest.testGetLength");
        
        int expectedLength = 10;
        
        assertEquals(expectedLength, quiz.getLength());
    }

    /**
     * Test of getCurQuestion method, of class Quiz.
     */
    @Test
    public void testGetCurQuestion() {
        System.out.println("QuizTest.testGetCurQuestion");
        
        String expectedAnswerValue = "3 CON";
        
        try {
            quiz.next();
            quiz.next();
            quiz.next();
            QuizQuestion question = quiz.next();
            String result = question.getAnswer().getValue();

            assertEquals(expectedAnswerValue, result);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test of clear method, of class Quiz.
     */
    @Test
    public void testClear() {
        System.out.println("QuizTest.testClear");
        
        String expectedBufferValue = "UNSUPPORTED TYPE: NONE";
        
        try {
            quiz.getBuffer().setValue("BLARF");
            quiz.clear();
            String result = quiz.getBuffer().getQuestionValue();

            assertEquals(expectedBufferValue, result);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test of getQuestions method, of class Quiz.
     */
    @Test
    public void testGetQuestions() {
        System.out.println("QuizTest.testGetQuestions");
        
        QuizQuestion[] questions = quiz.getQuestions();
        
        try {
            for (int i = 0; i > 10; i++) {
                assertEquals(i + " CON", questions[i].getQuestionValue());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test of getQuizLength method, of class Quiz.
     */
    @Test
    public void testGetQuizLength() {
        System.out.println("QuizTest.testGetQuizLength");
        
        int expectedLength = 10;
        
        assertEquals(expectedLength, quiz.getQuizLength());
    }

    /**
     * Test of getNumCorrect method, of class Quiz.
     */
    @Test
    public void testGetNumCorrect() {
        System.out.println("QuizTest.testGetNumCorrect");
        
        int expectedResult = 2;
        
        QuizQuestion[] questions = quiz.getQuestions();
        questions[1].setAnswered(QuizQuestion.Answered.Correct);
        questions[5].setAnswered(QuizQuestion.Answered.Correct);
        int result = quiz.getNumCorrect();
        
        assertEquals(result, expectedResult);
    }

    /**
     * Test of resetQuiz method, of class Quiz.
     */
    @Test
    public void testResetQuiz() {
        System.out.println("QuizTest.testResetQuiz");
        
        int expectedPosition = -1;
        int expectedCorrect = 0;
        
        try {
            QuizQuestion question = quiz.next();
            question.setAnswered(QuizQuestion.Answered.Correct);
            question = quiz.next();
            question.setAnswered(QuizQuestion.Answered.Correct);
            question = quiz.next();
            question.setAnswered(QuizQuestion.Answered.Correct);
            question = quiz.next();
            question.setAnswered(QuizQuestion.Answered.Incorrect);
            quiz.next();
            question = quiz.next();
            question.setAnswered(QuizQuestion.Answered.Correct);
            quiz.resetQuiz();

            int resultPosition = quiz.getCurQuestion();
            int resultCorrect = quiz.getNumCorrect();

            assertEquals(expectedPosition, resultPosition);
            assertEquals(expectedCorrect, resultCorrect);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test of trimQuiz method, of class Quiz.
     */
    @Test
    public void testTrimQuiz() {
        System.out.println("QuizTest.testTrimQuiz");
        
        int expectedLength = 9;
        int expectedQuizLength = 9;
        
        QuizQuestion question = quiz.getQuestions()[0];
        question.setAnswered(QuizQuestion.Answered.Correct);
        quiz.trimQuiz();
        
        assertEquals(expectedLength, quiz.getLength());
        assertEquals(expectedQuizLength, quiz.getQuizLength());
    }

    /**
     * Test of hasNext method, of class Quiz.
     */
    @Test
    public void testHasNext() {
        System.out.println("QuizTest.testHasNext");
        
        assertTrue(quiz.hasNext());
    }
    
    /**
     * Test of hasNext method, of class Quiz.
     */
    @Test
    public void testHasNextFull() {
        System.out.println("QuizTest.testHasNextFull");
        
        try {
            for (int i = 0; i < quiz.getLength(); i++) {
                assertTrue(quiz.hasNext());
                quiz.next();
            }

            assertFalse(quiz.hasNext());
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testExceptionNextOutOfBounds() {
        System.out.println("QuizTest.testExceptionNextOutOfBounds");
        
        String expectedMessage = "java.lang.IndexOutOfBoundsException: Index 10 out of bounds for length 10";
        
        Exception exception = assertThrows(Exception.class, () -> {
            for (int i = 0; i < quiz.getLength() + 1; i++) {
                quiz.next();
            }
        });

        String resultMessage = exception.getMessage();

        assertEquals(expectedMessage, resultMessage);
    }

    /**
     * Test of prev method, of class Quiz.
     */
    @Test
    public void testPrev() {
        System.out.println("QuizTest.testPrev");
        
        String expectedValue = "0 CON";
        
        try {
            quiz.next();
            quiz.next();
            QuizQuestion prev = quiz.prev();
            String resultValue = prev.getAnswer().getValue();
            
            assertEquals(expectedValue, resultValue);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testPrevBeforeFirst() {
        System.out.println("QuizTest.testPrevBeforeFirst");
        
        String expectedMessage = "You can't call this when on the first entry.";
        
        Exception exception = assertThrows(Exception.class, () -> {
            quiz.next();
            quiz.prev();
        });

        String resultMessage = exception.getMessage();

        assertEquals(expectedMessage, resultMessage);
    }
    
    @Test
    public void testPrevInitial() {
        System.out.println("QuizTest.testPrevInitial");
        
        String expectedMessage = "You can't call this when on the first entry.";
        
        Exception exception = assertThrows(Exception.class, () -> {
            quiz.prev();
        });

        String resultMessage = exception.getMessage();
        System.out.println(resultMessage);

        assertEquals(expectedMessage, resultMessage);
    }

    /**
     * Test of notFoundNode method, of class Quiz.
     */
    @Test
    public void testNotFoundNode() {
        System.out.println("QuizTest.testNotFoundNode");
        
        String expectedValue = "QUESTION NOT FOUND";
        
        try {
            QuizQuestion question = quiz.notFoundNode();
            String result = question.getQuestionValue();

            assertEquals(expectedValue, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
}
