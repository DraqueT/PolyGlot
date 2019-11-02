/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 * See LICENSE.TXT included with this code to read the full license agreement.
 *
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

/**
 * Used to rank any given object by numeric value.
 * @author draque
 * @param <N>
 */
public class RankedObject<N> implements Comparable<RankedObject>{
    private final int rank;
    private int LOWER = -1;
    private int HIGHER = 1;
    private final N holder;
    
    public RankedObject(N _holder, int _rank) {
        holder = _holder;        
        rank = _rank;
    }
    
    public int getRank() {
        return rank;
    }
    
    public N getHolder() {
        return holder;
    }
    
    public void setDescending(boolean descending) {
        if (descending) {
            LOWER = 1;
            HIGHER = -1;
        } else {
            LOWER = -1;
            HIGHER = 1;
        }
    }
    
    // does not handle equal values. Returning 0 would merge, and this is undesirable.
    @Override
    public int compareTo(RankedObject _compare) {        
        if (_compare.getRank() > this.getRank()) {
            return LOWER;
        }
        else {
            return HIGHER;
        }    
    }
}
