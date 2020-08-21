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
package org.darisadesigns.polyglotlina.Nodes;

/**
 * Holds information about evolved elements of a language
 *
 * @author draque
 */
public class EvolutionPair {

    public final String start;
    public final String end;
    public final String notes;
    public final String issueDescription;
    public final EvolutionType type;
    
    public EvolutionPair(String _start, String _end, EvolutionType _type, String _issueDescription, String _notes) {
        start = _start;
        end = _end;
        issueDescription = _issueDescription;
        type = _type;
        notes = _notes;
    }

    public static enum EvolutionType {
        word,
        savedConjugation,
        conjugationTemplate
    }
}
