/*
 * Copyright (c) 2014-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Nodes;

/**
 *
 * @author draque
 */
public class LexiconProblemNode implements Comparable<LexiconProblemNode>{
    public final DictNode problemWord;
    public final String description;
    public final ProblemType problemType;
    public final String shortDescription;
    
    public LexiconProblemNode(DictNode _problemWord, String _description, ProblemType _problemType) {
        problemWord = _problemWord;
        description = _description;
        problemType = _problemType;
        
        switch (_problemType) {
            case ConWord-> shortDescription = _problemWord.getValue();
            case PoS-> shortDescription = "Part of Speech: " + _problemWord.getValue();
            case Phonology-> shortDescription = "Phonology Problem";
            default-> shortDescription = "UNDEFINED VALUE";
        }
    }
    
    @Override
    public String toString() {
        return shortDescription;
    }

    @Override
    public int compareTo(LexiconProblemNode o) {
        int ret;
        
        if (this.problemType == o.problemType) {
            ret= this.problemWord.compareTo(o.problemWord);
        } else {
            ret = this.problemType.value < o.problemType.value ? -1 : 1;
        }
        
        return ret;
    }
    
    public enum ProblemType {
        ConWord(0),
        PoS(1),
        Phonology(2);
        
        public final int value;
        
        ProblemType(int _value) {
            value = _value;
        }
    }
}
