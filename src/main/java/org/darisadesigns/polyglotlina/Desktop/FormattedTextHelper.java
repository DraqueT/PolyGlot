/*
 * Copyright (c) 2021, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop;

import org.darisadesigns.polyglotlina.Desktop.CustomControls.PGrammarPane;
import org.darisadesigns.polyglotlina.Nodes.ImageNode;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.darisadesigns.polyglotlina.DictCore;

/**
 * This is a helper class, which deals with formatted text in Java
 *
 * @author draque
 */
public final class FormattedTextHelper extends org.darisadesigns.polyglotlina.FormattedTextHelper {
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
     * Restores to the JTextPane the formatted text values encoded in the saved
     * value string
     * @param savedVal value to restore formatted text from
     * @param pane Text pane to restore formatted text to.
     * @param core Dictionary Core (needed for references)
     * @throws javax.swing.text.BadLocationException if unable to load
     */
    public static void restoreFromString(String savedVal, JTextPane pane, DictCore core) throws BadLocationException {
        String remaining = savedVal;
        pane.setText("");
        Color fontColor = Color.black;
        String font = "";
        int fontSize = -1;
                
        while (!remaining.isEmpty()) {
            String nextNode = getNextNode(remaining);
            Font conFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon();
            
            remaining = remaining.substring(nextNode.length());
            
            if (nextNode.startsWith("<font")) {
                
                font = extractFamily(nextNode);
                fontSize = extractSize(nextNode);
                fontColor = extractColor(nextNode);
                
                if (font.equals(conFont.getFamily())) {
                    font = PGTUtil.CONLANG_FONT;
                }
            } else if (nextNode.startsWith("</font")) {
                // do nothing
            } else if (nextNode.startsWith("<img src=")) {
                String idString = nextNode.replace("<img src=\"", "").replace("\">", "");
                Integer id = Integer.parseInt(idString);
                ImageNode imageNode = (ImageNode)core.getImageCollection().getNodeById(id);
                ((PGrammarPane)pane).addImage(imageNode);      
            } else {
                Document doc = pane.getDocument();
                
                MutableAttributeSet aset = new SimpleAttributeSet();
                if (font.equals(PGTUtil.CONLANG_FONT)) {
                    if (core.getPropertiesManager().isEnforceRTL()) {
                        nextNode = PGTUtil.RTL_CHARACTER + nextNode;
                    }
                    StyleConstants.setFontFamily(aset, conFont.getFamily());
                } else {
                    if (core.getPropertiesManager().isEnforceRTL()) {
                        nextNode = PGTUtil.LTR_MARKER + nextNode;
                    }
                    if (!font.isEmpty()) {
                        StyleConstants.setFontFamily(aset, font);
                    }
                }
                
                if (fontSize != -1) {
                    StyleConstants.setFontSize(aset, fontSize);
                }
                
                StyleConstants.setForeground(aset, fontColor);
                
                if (!nextNode.isEmpty()){
                    doc.insertString(doc.getLength(), nextNode, aset);
                }
            }
        }
    }
    
    /**
     * Returns list of strings representing a chapter section. The paired boolean
     * is set to true if the segment of text is in the conlang's font
     * @param savedVal section of text to analyze and return
     * @param core
     * @return ordered list of text
     */
    public static List<Entry<String, PFontInfo>> getSectionTextFontSpecific(String savedVal, DictCore core) {
        String remaining = savedVal;
        String font = "";
        List<Entry<String, PFontInfo>> ret = new ArrayList<>();
        PFontInfo conFont = new PFontInfo();
                
        while (!remaining.isEmpty()) {
            String nextNode = getNextNode(remaining);
            conFont.awtFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon();
            
            remaining = remaining.substring(nextNode.length());
            
            if (nextNode.startsWith("<font")) {                
                font = extractFamily(nextNode);
                conFont.size = extractSize(nextNode);
                conFont.awtColor = extractColor(nextNode);
            } else if (nextNode.startsWith("</font")) {
                // do nothing. All font changes are prefixed with<font
            } else {
                if (font.equals(conFont.awtFont.getFamily()) && core.getPropertiesManager().isEnforceRTL()) {
                    nextNode = PGTUtil.RTL_CHARACTER + nextNode;
                } else if (core.getPropertiesManager().isEnforceRTL()) {
                    nextNode = PGTUtil.LTR_MARKER + nextNode;
                }
                
                if (!nextNode.isEmpty()){
                    conFont.awtFont = font.equals(core.getPropertiesManager().getFontConFamily()) ? 
                            ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon() : new JLabel().getFont();
                    Entry<String, PFontInfo> temp = new SecEntry<>(nextNode, conFont);
                    ret.add(temp);
                    conFont = new PFontInfo();
                }
            }
        }
        
        return ret;
    }
    
    /**
     * Given an HTML <font~> node, return the font family
     * @param targetNode string value of HTML node
     * @return string value of family name, blank if none
     */
    private static Color extractColor(String targetNode) {
        Color ret = Color.black;
        
        int pos = targetNode.indexOf(COLOR) + 7;
        
        if (pos == -1) {
            return ret;
        }
        
        String strip = targetNode.substring(pos);
        pos = strip.indexOf("\"");
        strip = strip.substring(0, pos);
        
        switch (strip) {
            case BLACK:
                ret = Color.black;
                break;
            case RED:
                ret = Color.red;
                break;
            case BLUE:
                ret = Color.blue;
                break;
            case GRAY:
                ret = Color.gray;
                break;
            case YELLOW:
                ret = Color.yellow;
                break;
            case GREEN:
                ret = Color.green;
                break;
            default:
                ret = Color.black;
                break;
        }
                
        return ret;
    }
    
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
     * Creates and returns string representing complex formatted text, which 
     * can be saved. Filters out all RTL and LTR characters before returning.
     * @param pane JTextPane containing text to be saved
     * @return encoded values of pane
     * @throws BadLocationException if unable to create string format
     */
    public static String storageFormat(JTextPane pane) throws Exception {
        String ret = storeFormatRecurse(pane.getDocument().getDefaultRootElement(), pane);
        return ret.replace(PGTUtil.RTL_CHARACTER, "").replace(PGTUtil.LTR_MARKER, "");
    }

    /**
     * Recursing method implementing functionality of storageFormat()
     * @param e element to be cycled through
     * @param pane top parent JTextPane
     * @return string format value of current node and its children
     * @throws BadLocationException if unable to create string format
     */
    private static String storeFormatRecurse(Element e, JTextPane pane) throws Exception {
        String ret = "";
        int ec = e.getElementCount();

        if (ec == 0) {
            // if more media addable in the future, this is where to process it...
            // hard coded values because they're hard coded in Java. Eh.
            if (e.getAttributes().getAttribute("$ename") != null
                    && e.getAttributes().getAttribute("$ename").equals("icon")) {
                if (e.getAttributes().getAttribute(PGTUtil.IMAGE_ID_ATTRIBUTE) == null) {
                    throw new Exception("ID For image not stored. Unable to store section.");
                }
                
                ret += "<img src=\"" + e.getAttributes().getAttribute(PGTUtil.IMAGE_ID_ATTRIBUTE) + "\">";
            } else {
                int start = e.getStartOffset();
                int len = e.getEndOffset() - start;
                if (start < pane.getDocument().getLength()) {
                    AttributeSet a = e.getAttributes();
                    String font = StyleConstants.getFontFamily(a);
                    String fontColor = colorToText(StyleConstants.getForeground(a));
                    int fontSize = StyleConstants.getFontSize(a);
                    ret += "<font face=\"" + font + "\""
                            + "size=\"" + fontSize + "\""
                            + "color=\"" + fontColor + "\"" + ">";
                    ret += pane.getDocument().getText(start, len);
                    ret += "</font>";
                }
            }
        } else {
            for (int i = 0; i < ec; i++) {
                ret += storeFormatRecurse(e.getElement(i), pane);
            }
        }

        return ret;
    }

    /**
     * Gets standardized string value for color
     *
     * @param c color to get standard value for
     * @return string format standard value
     */
    public static String colorToText(Color c) {
        String ret;

        // Java 1.6 can't switch on an enum...
        if (c == Color.black) {
            ret = BLACK;
        } else if (c == Color.red) {
            ret = RED;
        } else if (c == Color.green) {
            ret = GREEN;
        } else if (c == Color.yellow) {
            ret = YELLOW;
        } else if (c == Color.blue) {
            ret = BLUE;
        } else if (c == Color.gray) {
            ret = GRAY;
        } else {
            ret = BLACK;
        }

        return ret;
    }
    
    public static Color textToColor(String color) {
        Color ret;
        
        switch (color) {
            case BLACK:
                ret = Color.black;
                break;
            case RED:
                ret = Color.red;
                break;
            case GREEN:
                ret = Color.green;
                break;
            case YELLOW:
                ret = Color.yellow;
                break;
            case BLUE:
                ret = Color.blue;
                break;
            case GRAY:
                ret = Color.gray;
                break;
            default:
                ret = Color.black;
                break;
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

    private FormattedTextHelper() {
        super();
    }
}

