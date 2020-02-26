/*
 * Copyright (c) 2019-2020, Draque Thompson, draquemail@gmail.com
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

import TestResources.DummyCore;
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
        questionTest = new QuizQuestion(DummyCore.newCore());
        
        answer = new ConWord();
        answer.setValue("BALYOO");
        
        questionTest.setAnswer(answer);
        questionTest.setSource(answer);
        questionTest.setType(QuizQuestion.QuestionType.PoS);
        questionTest.setUserAnswer(answer);
    }

    @Test
    public void testSetEqual() {
        System.out.println("QuizQuestionTest.testSetEqual");
        
        QuizQuestion copy = new QuizQuestion(DummyCore.newCore());
        copy.setEqual(questionTest);
        assertEquals(copy, questionTest);
    }

    @Test
    public void testAddChoice() {
        System.out.println("QuizQuestionTest.testAddChoice");
        
        ConWord choice = new ConWord();
        int expectedLength = questionTest.getChoices().length + 1;
        
        questionTest.addChoice(choice);
        
        assertEquals(expectedLength, questionTest.getChoices().length);
    }

    @Test
    public void testEquals() {
        System.out.println("QuizQuestionTest.testEquals");
        
        QuizQuestion copyQ = new QuizQuestion(DummyCore.newCore());
        
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
        System.out.println("QuizQuestionTest.testEquals");
        
        QuizQuestion copyQ = new QuizQuestion(DummyCore.newCore());
        
        ConWord copyA = new ConWord();
        copyA.setValue("WRONGZZZ");
        
        copyQ.setAnswer(copyA);
        copyQ.setSource(copyA);
        copyQ.setType(QuizQuestion.QuestionType.PoS);
        copyQ.setUserAnswer(copyA);
        
        assertNotEquals(copyQ, questionTest);
    }
}
