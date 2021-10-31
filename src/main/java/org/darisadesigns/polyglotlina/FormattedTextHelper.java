/*
 * Copyright (c) 2015-2018, Draque Thompson
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

import org.darisadesigns.polyglotlina.Nodes.ImageNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * This is a helper class, which deals with formatted text in Java
 *
 * @author draque
 */
public class FormattedTextHelper {
    private static final String FINDBODY = "<body>";
    private static final String FINDBODYEND = "</body>";
    private static final String BLACK = "black";
    private static final String RED = "red";
    private static final String GRAY = "gray";
    private static final String GREEN = "green";
    private static final String YELLOW = "yellow";
    private static final String BLUE = "blue";
    private static final String COLOR = "color";
    private static final String FACE = "face";
    private static final String SIZE = "size";

    /**
     * Given an HTML <font~> node, return the font family
     * @param targetNode string value of HTML node
     * @return string value of family name, blank if none
     */
    private static String extractFamily(String targetNode) {
        String ret = "";
        
        int pos = targetNode.indexOf(FACE) + 6;
        
        if (pos == -1) {
            return ret;
        }
        
        String strip = targetNode.substring(pos);
        pos = strip.indexOf("\"");
        ret = strip.substring(0, pos);
        
        return ret;
    }
    
    /**
     * Given an HTML <font~> node, return the font size
     * @param targetNode string value of HTML node
     * @return integer value of family size, -1 if none
     */
    private static int extractSize(String targetNode) {
        int ret = -1;
        
        int pos = targetNode.indexOf(SIZE) + 6;
        
        if (pos == -1) {
            return ret;
        }
        
        String strip = targetNode.substring(pos);
        pos = strip.indexOf("\"");
        ret = Integer.parseInt(strip.substring(0, pos));
        
        return ret;
    }
    
    /**
     * Gets next node from saved text
     * @param fromText remaining text from which to pull node
     * @return next node in string form
     */
    private static String getNextNode(String fromText) {
        String ret;
        
        if (fromText.startsWith("<font") ||
                fromText.startsWith("</font")) {
            int pos = fromText.indexOf('>');
            ret = fromText.substring(0, pos + 1);
        } else {
            int posStart = fromText.indexOf("<font");
            int posEnd = fromText.indexOf("</font");
            
            // get the nearest start/end of a font ascription
            int pos;
            if (posStart == -1 && posEnd == -1) {
                pos = fromText.length();
            } else if (posStart == -1) {
                pos = posEnd;
            } else if (posEnd == -1) {
                pos = posStart;
            } else {
                pos = Math.min(posStart, posEnd);
            }
            
            ret = fromText.substring(0, pos);
        }
        
        return ret;
    }
    
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
        
        body = body.trim();
        body = body.replace("<br>\n", "<br>"); // prevents doubling of <br> statements due to formatting
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

    protected FormattedTextHelper() {
    }
}
