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
import PolyGlot.ManagersCollections.DictionaryCollection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author draque.thompson
 */
public class Quiz extends DictionaryCollection {
    private final DictCore core;
    Iterator<QuizQuestion> quizIt = null;
    QuizQuestion curQuestion;
    
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
        List questions = Arrays.asList(nodeMap.values().toArray());
        Collections.shuffle(questions, new Random(seed));
        return questions;
    }
    
    public int getQuizLength() {
        return nodeMap.size();
    }
    
    /**
     * Tests whether more questions exist in quiz
     * @return true if more questions
     */
    public boolean hasNext() {
        if (quizIt == null) {
            quizIt = nodeMap.values().iterator();
        }
        
        return quizIt.hasNext();
    }
    
    /**
     * Gets next quiz question (if one exists)
     * Will throw exception if no next question.
     * 
     * @return next quiz question
     */
    public QuizQuestion next() {
        if (quizIt == null) {
            quizIt = nodeMap.values().iterator();
        }
        
        curQuestion = quizIt.next();
        
        return curQuestion;
    }
}
