/*
 * Copyright (c) 2015-2018, Draque Thompson
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
package PolyGlot;

import PolyGlot.CustomControls.PGrammarPane;
import PolyGlot.Nodes.ImageNode;
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

/**
 * This is a helper class, which deals with formatted text in Java
 *
 * @author draque
 */
public class FormattedTextHelper {
    private final static String findBody = "<body>";
    private final static String findBodyEnd = "</body>";
    private final static String BLACK = "black";
    private final static String RED = "red";
    private final static String GRAY = "gray";
    private final static String GREEN = "green";
    private final static String YELLOW = "yellow";
    private final static String BLUE = "blue";
    private final static String COLOR = "color";
    private final static String FACE = "face";
    private final static String SIZE = "size";

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
            Font conFont = core.getPropertiesManager().getFontCon();
            
            remaining = remaining.substring(nextNode.length(), remaining.length());
            
            if (nextNode.startsWith("<font")) {
                
                font = extractFamily(nextNode);
                fontSize = extractSize(nextNode);
                fontColor = extractColor(nextNode);
                
                if (font.equals(conFont.getFamily())) {
                    font = PGTUtil.conLangFont;
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
                if (font.equals(PGTUtil.conLangFont)) {
                    if (core.getPropertiesManager().isEnforceRTL()) {
                        nextNode = PGTUtil.RTLMarker + nextNode;
                    }
                    StyleConstants.setFontFamily(aset, conFont.getFamily());
                } else {
                    if (core.getPropertiesManager().isEnforceRTL()) {
                        nextNode = PGTUtil.LTRMarker + nextNode;
                    }
                    if (font.length() != 0) {
                        StyleConstants.setFontFamily(aset, font);
                    }
                }
                
                if (fontSize != -1) {
                    StyleConstants.setFontSize(aset, fontSize);
                }
                
                StyleConstants.setForeground(aset, fontColor);
                
                if (nextNode.length() != 0){
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
    public static List<Entry<String, PFontInfo>> getSectionTextFontSpecifec(String savedVal, DictCore core) {
        String remaining = savedVal;
        String font = "";
        List<Entry<String, PFontInfo>> ret = new ArrayList<>();
        PFontInfo conFont = new PFontInfo();
                
        while (!remaining.isEmpty()) {
            String nextNode = getNextNode(remaining);
            conFont.awtFont = core.getPropertiesManager().getFontCon();
            
            remaining = remaining.substring(nextNode.length(), remaining.length());
            
            if (nextNode.startsWith("<font")) {                
                font = extractFamily(nextNode);
                conFont.size = extractSize(nextNode);
                conFont.awtColor = extractColor(nextNode);
            } else if (nextNode.startsWith("</font")) {
                // do nothing. All font changes are prefixed with<font
            } else {
                if (font.equals(conFont.awtFont.getFamily()) && core.getPropertiesManager().isEnforceRTL()) {
                    nextNode = PGTUtil.RTLMarker + nextNode;
                } else if (core.getPropertiesManager().isEnforceRTL()) {
                    nextNode = PGTUtil.LTRMarker + nextNode;
                }
                
                if (nextNode.length() != 0){
                    conFont.awtFont = font.equals(core.getPropertiesManager().getFontCon().getFamily()) ? 
                            core.getPropertiesManager().getFontCon() : new JLabel().getFont();
                    SecEntry temp = new SecEntry(nextNode, conFont);
                    ret.add(temp);
                    conFont = new PFontInfo();
                }
            }
        }
        
        return ret;
    }
    
    public static com.itextpdf.kernel.color.Color swtColorToItextColor(Color awtc) {
        com.itextpdf.kernel.color.Color ret = com.itextpdf.kernel.color.Color.BLACK;
        if (awtc == Color.BLACK) {
            ret = com.itextpdf.kernel.color.Color.BLACK;
        } else if (awtc == Color.BLUE) {
            ret = com.itextpdf.kernel.color.Color.BLUE;
        } else if (awtc == Color.CYAN) {
            ret = com.itextpdf.kernel.color.Color.CYAN;
        }  else if (awtc == Color.DARK_GRAY) {
            ret = com.itextpdf.kernel.color.Color.DARK_GRAY;
        } else if (awtc == Color.GRAY) {
            ret = com.itextpdf.kernel.color.Color.GRAY;
        } else if (awtc == Color.GREEN) {
            ret = com.itextpdf.kernel.color.Color.GREEN;
        } else if (awtc == Color.LIGHT_GRAY) {
            ret = com.itextpdf.kernel.color.Color.LIGHT_GRAY;
        } else if (awtc == Color.MAGENTA) {
            ret = com.itextpdf.kernel.color.Color.MAGENTA;
        } else if (awtc == Color.ORANGE) {
            ret = com.itextpdf.kernel.color.Color.ORANGE;
        } else if (awtc == Color.PINK) {
            ret = com.itextpdf.kernel.color.Color.PINK;
        } else if (awtc == Color.RED) {
            ret = com.itextpdf.kernel.color.Color.RED;
        } else if (awtc == Color.WHITE) {
            ret = com.itextpdf.kernel.color.Color.WHITE;
        } else if (awtc == Color.YELLOW) {
            ret = com.itextpdf.kernel.color.Color.YELLOW;
        } else if (awtc == Color.black) {
            ret = com.itextpdf.kernel.color.Color.BLACK;
        } else if (awtc == Color.blue) {
            ret = com.itextpdf.kernel.color.Color.BLUE;
        } else if (awtc == Color.cyan) {
            ret = com.itextpdf.kernel.color.Color.CYAN;
        } else if (awtc == Color.darkGray) {
            ret = com.itextpdf.kernel.color.Color.DARK_GRAY;
        } else if (awtc == Color.gray) {
            ret = com.itextpdf.kernel.color.Color.GRAY;
        } else if (awtc == Color.green) {
            ret = com.itextpdf.kernel.color.Color.GREEN;
        } else if (awtc == Color.lightGray) {
            ret = com.itextpdf.kernel.color.Color.LIGHT_GRAY;
        } else if (awtc == Color.magenta) {
            ret = com.itextpdf.kernel.color.Color.MAGENTA;
        } else if (awtc == Color.orange) {
            ret = com.itextpdf.kernel.color.Color.ORANGE;
        } else if (awtc == Color.pink) {
            ret = com.itextpdf.kernel.color.Color.PINK;
        } else if (awtc == Color.red) {
            ret = com.itextpdf.kernel.color.Color.RED;
        } else if (awtc == Color.white) {
            ret = com.itextpdf.kernel.color.Color.WHITE;
        } else if (awtc == Color.yellow) {
            ret = com.itextpdf.kernel.color.Color.YELLOW;
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
            } else if (posStart < posEnd) {
                pos = posStart;
            } else {
                pos = posEnd;
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
    public static String storageFormat(JTextPane pane) throws BadLocationException, Exception {
        String ret = storeFormatRecurse(pane.getDocument().getDefaultRootElement(), pane);
        return ret.replace(PGTUtil.RTLMarker, "").replace(PGTUtil.LTRMarker, "");
    }

    /**
     * Recursing method implementing functionality of storageFormat()
     * @param e element to be cycled through
     * @param pane top parent JTextPane
     * @return string format value of current node and its children
     * @throws BadLocationException if unable to create string format
     */
    private static String storeFormatRecurse(Element e, JTextPane pane) throws BadLocationException, Exception {
        String ret = "";
        int ec = e.getElementCount();

        if (ec == 0) {
            // if more media addable in the future, this is where to process it...
            // hard coded values because they're hard coded in Java. Eh.
            if (e.getAttributes().getAttribute("$ename") != null
                    && e.getAttributes().getAttribute("$ename").equals("icon")) {
                if (e.getAttributes().getAttribute(PGTUtil.ImageIdAttribute) == null) {
                    throw new Exception("ID For image not stored. Unable to store section.");
                }
                
                ret += "<img src=\"" + e.getAttributes().getAttribute(PGTUtil.ImageIdAttribute) + "\">";
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
        
        if (body.contains(findBody)) {
            int pos = body.indexOf(findBody);
            preFix = body.substring(0, pos + findBody.length());
            body = body.substring(pos + findBody.length());
        }
        
        if (body.contains(findBodyEnd)) {
            int pos = body.indexOf(findBodyEnd);
            postFix = body.substring(pos);
            body = body.substring(0, pos);
        }
        
        body = body.trim();
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
        
        if (html.contains(findBody) && html.contains(findBodyEnd)) {
            ret = html.substring(html.indexOf(findBody) + findBody.length(), html.indexOf(findBodyEnd));
        }
        
        return ret.replaceAll("<.*?>", "");
    }
    
    static class SecEntry<String, PFontInfo> implements Entry {
        final String key;
        Object fontInfo;        
        public SecEntry(String _key, PFontInfo _fontInfo) {
            key = _key;
            fontInfo = _fontInfo;
        }        
        @Override
        public Object getKey() {
            return key;
        }
        @Override
        public Object getValue() {
            return fontInfo;
        }
        @Override
        public Object setValue(Object value) {
            fontInfo = value;
            return fontInfo;
        }        
    }
}
