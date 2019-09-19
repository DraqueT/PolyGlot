/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.QuizEngine;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.DictionaryCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

/**
 *
 * @author draque.thompson
 */
public class Quiz extends DictionaryCollection<QuizQuestion> {
    private final DictCore core;
    List<QuizQuestion> quizList = null;
    int quizPos = -1; // start at -1 because initial next() call bumps to 0
    QuizQuestion curQuestion;
    
    public int getLength() {
        return quizList.size();
    }
    
    /**
     * Gets current position within quiz.
     * Index begins at 0.
     * @return current position
     */
    public int getCurQuestion() {
        return quizPos;
    }
    
    public Quiz(DictCore _core) {
        core = _core;
    }
    
    @Override
    public void clear() {
        bufferNode = new QuizQuestion(core);
    }
    
    /**
     * Gets list of questions in randomized order
     * @return 
     */
    public List<QuizQuestion> getQuestions() {
        long seed = System.nanoTime();
        List<QuizQuestion> questions = new ArrayList<>(nodeMap.values());
        Collections.shuffle(questions, new Random(seed));
        return questions;
    }
    
    public int getQuizLength() {
        return nodeMap.size();
    }
    
    /**
     * Gets number of correctly answered questions (even if quiz is not completed)
     * @return number of correct answers
     */
    public int getNumCorrect() {
        int ret = 0;
        
        for (Object o : nodeMap.values().toArray()) {
            QuizQuestion question = (QuizQuestion)o;
            if (question.getAnswered() == QuizQuestion.Answered.Correct) {
                ret++;
            }
        }
        
        return ret;
    }
    
    /**
     * Sets test back to non-taken, original status.
     */
    public void resetQuiz() {
        for (Object o : nodeMap.values().toArray()) {
            QuizQuestion question = (QuizQuestion)o;
            question.setAnswered(QuizQuestion.Answered.Unanswered);
            question.setUserAnswer(null);
        }
        
        curQuestion = null;
        quizPos = -1;
        quizList = null;
    }
    
    public void trimQuiz() {
        for (Entry<Integer, QuizQuestion> o : nodeMap.entrySet()) {
            QuizQuestion question = o.getValue();
            
            if (question.getAnswered() == QuizQuestion.Answered.Correct) {
                nodeMap.remove(o.getKey());
            } else {
                question.setAnswered(QuizQuestion.Answered.Unanswered);
                question.setUserAnswer(null);
            }            
        }
        
        curQuestion = null;
        quizPos = -1;
        quizList = null;
    }
    
    /**
     * Tests whether more questions exist in quiz
     * @return true if more questions
     */
    public boolean hasNext() {
        if (quizList == null) {
            quizList = new ArrayList<>(nodeMap.values());
        }
        
        return quizList.size() > quizPos;
    }
    
    /**
     * Gets next quiz question (if one exists)
     * Will throw out of bounds exception if no
     * next question.
     * 
     * @return next quiz question
     */
    public QuizQuestion next() {
        if (quizList == null) {
            quizList = new ArrayList<>(nodeMap.values());
        }
        
        quizPos++;        
        curQuestion = quizList.get(quizPos);        
        
        return curQuestion;
    }
    
    /**
     * Gets previous question. Throws null exception if quizList not initialized.
     * Throws out of bounds exception if called while on first question
     * @return 
     */
    public QuizQuestion prev() {
        if (quizPos == 0) {
            throw new IndexOutOfBoundsException("You can't call this when on the first entry.");
        }
        
        quizPos--;
        
        return quizList.get(quizPos);
    }

    @Override
    public Object notFoundNode() {
        QuizQuestion emptyQuestion = new QuizQuestion(core);
        emptyQuestion.setValue("QUESTION NOT FOUND");
        return emptyQuestion;
    }
}
