/*
 * Copyright (c) 2016-2019, Draque
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
package org.darisadesigns.polyglotlina.CustomControls;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provides alphabetical ordering map- compatible with multi-unicode character,
 * constructed lettering.
 * 
 * @author Draque Thompson
 * @param <K>
 * @param <V>
 */
public class PAlphaMap<K, V> {
    private int longestEntry = 0;
    private final HashMap<K, V> delegate = new HashMap<>();
    
    /**
     *
     * @param key String value of alpha set
     * @param orderVal Order in alphabet of of alpha
     * @return 
     */
    public V put(K key, V orderVal) {
        java.lang.String sKey = (java.lang.String)key;
        int keyLen = sKey.length();
        if (keyLen > longestEntry) {
            longestEntry = keyLen;
        }
        
        return delegate.put(key, orderVal);
    }
    
    public int getLongestEntry() {
        return longestEntry;
    }
    
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
    
    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }
    
    public V get(K key) {
        return delegate.get(key);
    }
    
    public void clear() {
        delegate.clear();
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (comp instanceof PAlphaMap) {
            ret = ((PAlphaMap)comp).getLongestEntry() == longestEntry
                    && ((PAlphaMap)comp).getDelegate().equals(delegate);
        }
        
        return ret;
    }

    public Map<K, V> getDelegate() {
        return delegate;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.longestEntry;
        hash = 59 * hash + Objects.hashCode(this.delegate);
        return hash;
    }
}
