/*
 * Copyright (c) 2019, draque
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
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class QuizQuestionTest {
    private final QuizQuestion questionTest;
    private final ConWord answer;
    
    public QuizQuestionTest() {
        System.out.println("QuizQuestionText");
        
        questionTest = new QuizQuestion(new DictCore());
        
        answer = new ConWord();
        answer.setValue("BALYOO");
        
        questionTest.setAnswer(answer);
        questionTest.setSource(answer);
        questionTest.setType(QuizQuestion.QuestionType.PoS);
        questionTest.setUserAnswer(answer);
    }

    @Test
    public void testSetEqual() {
        System.out.println("QuizQuestionText:testSetEqual");
        QuizQuestion copy = new QuizQuestion(new DictCore());
        copy.setEqual(questionTest);
        assertEquals(copy, questionTest);
    }

    @Test
    public void testAddChoice() {
        System.out.println("QuizQuestionText:testAddChoice");
        ConWord choice = new ConWord();
        int expectedLength = questionTest.getChoices().size() + 1;
        
        questionTest.addChoice(choice);
        
        assertEquals(expectedLength, questionTest.getChoices().size());
    }

    @Test
    public void testEquals() {
        System.out.println("QuizQuestionText:testEquals");
        
        QuizQuestion copyQ = new QuizQuestion(new DictCore());
        
        ConWord copyA = new ConWord();
        copyA.setValue("BALYOO");
        
        copyQ.setAnswer(copyA);
        copyQ.setSource(copyA);
        copyQ.setType(QuizQuestion.QuestionType.PoS);
        copyQ.setUserAnswer(copyA);
        
        assertEquals(copyQ, questionTest);
    }
    
    @Test
    public void testNotEquals() {
        System.out.println("QuizQuestionText:testEquals");
        
        QuizQuestion copyQ = new QuizQuestion(new DictCore());
        
        ConWord copyA = new ConWord();
        copyA.setValue("WRONGZZZ");
        
        copyQ.setAnswer(copyA);
        copyQ.setSource(copyA);
        copyQ.setType(QuizQuestion.QuestionType.PoS);
        copyQ.setUserAnswer(copyA);
        
        assertNotEquals(copyQ, questionTest);
    }
}
