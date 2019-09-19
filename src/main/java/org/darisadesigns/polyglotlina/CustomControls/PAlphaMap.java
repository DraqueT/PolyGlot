/*
 * Copyright (c) 2016-2019, Draque
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
package org.darisadesigns.polyglotlina.CustomControls;

import java.util.HashMap;

/**
 * Provides alphabetical ordering map- compatible with multi-unicode character,
 * constructed lettering.
 * 
 * @author Draque Thompson
 * @param <K>
 * @param <V>
 */
public class PAlphaMap<K, V> extends HashMap<K, V> {
    int longestEntry = 0;
    
    /**
     *
     * @param key String value of alpha set
     * @param orderVal Order in alphabet of of alpha
     * @return 
     */
    @Override
    public V put(K key, V orderVal) {
        java.lang.String sKey = (java.lang.String)key;
        int keyLen = sKey.length();
        if (keyLen > longestEntry) {
            longestEntry = keyLen;
        }
        
        return (V)super.put((K)key, (V)orderVal);
    }
    
    public int getLongestEntry() {
        return longestEntry;
    }
}
