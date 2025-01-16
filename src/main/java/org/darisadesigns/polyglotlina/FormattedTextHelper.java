/*
 * Copyright (c) 2015-2023, Draque Thompson
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
package org.darisadesigns.polyglotlina;

import java.util.Map.Entry;

/**
 * This is a helper class, which deals with formatted text in Java
 *
 * @author draque
 */
public class FormattedTextHelper {
    private static final String FINDBODY = "<body>";
    private static final String FINDBODYEND = "</body>";
    
    /**
     * Takes html with linebreaks in body and converts the linebreaks to proper
     * <br> tags
     * @param html input html to be sanitized
     * @return linebreak sanitized html
     */
    public static String HTMLLineBreakParse(String html) {
        String preFix = "";
        String postFix = "";
        String body = html;
        
        if (body.contains(FINDBODY)) {
            int pos = body.indexOf(FINDBODY);
            preFix = body.substring(0, pos + FINDBODY.length());
            body = body.substring(pos + FINDBODY.length());
        }
        
        if (body.contains(FINDBODYEND)) {
            int pos = body.indexOf(FINDBODYEND);
            postFix = body.substring(pos);
            body = body.substring(0, pos);
        }
        
        preFix = preFix.replace("\n", "");
        postFix = postFix.replace("\n", "");
        
        body = body.trim();
        body = body.replaceAll("\s*</?p.*?>\n?",""); // remove paragraph tags added by quick add
        body = body.replaceAll("\s*<br>\n", "<br>"); // prevents doubling of <br> statements due to formatting
        body = body.replace("\n", "<br>");
        
        return preFix + body + postFix;
    }
    
    /**
     * Returns text only body from html
     * @param html full html to reduce
     * @return only the body of the HTML, stripped of all tags
     */
    public static String getTextBody(String html) {
        String ret = html;
        
        if (html.contains(FINDBODY) && html.contains(FINDBODYEND)) {
            ret = html.substring(html.indexOf(FINDBODY) + FINDBODY.length(), html.indexOf(FINDBODYEND));
        }
        
        return ret.replaceAll("<.*?>", "");
    }
    
    public static class SecEntry<K, V> implements Entry<K, V> {
        private final K key;
        private V fontInfo;
        public SecEntry(K _key, V _fontInfo) {
            key = _key;
            fontInfo = _fontInfo;
        }        
        @Override
        public K getKey() {
            return key;
        }
        @Override
        public V getValue() {
            return fontInfo;
        }
        
        @Override
        public V setValue(V value) {
            V old = this.fontInfo;
            this.fontInfo = value;
            return old;
        }
    }
    
    public static int fontSizePtToRem(double ptSize) {
        return (int)(ptSize*4)/3;
    }

    protected FormattedTextHelper() {
    }
}
