/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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

package PolyGlot;

import java.awt.Font;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class PropertiesManager {
    private String overrideProgramPath = "";
    private Font font = null;
    private Integer fontStyle = 0;
    private Integer fontSize = 0;
    private boolean proAutoPop = false;
    private final Map alphaOrder;
    private String alphaPlainText = "";
    private String langName = "";
    private boolean typesMandatory = false;
    private boolean localMandatory = false;
    private boolean wordUniqueness = false;
    private boolean localUniqueness = false;
    private boolean ignoreCase = false;
    private boolean disableProcRegex = false;
    private String fontName = "";
    private byte[] cachedFont = null;
    
    /**
     * Gets the java FX version of an AWT font
     * @return javafx font
     */
    public javafx.scene.text.Font getFXFont() {
        javafx.scene.text.Font ret;
        
        if (font == null) {
            ret = (new javafx.scene.control.TextField()).getFont();
        } else {
            java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            ret = javafx.scene.text.Font.font(font.getFamily(), fontSize);
        }
        
        return ret;
    }
    
    /**
     * Sets value of cached font file as byte array
     * @param _cachedFont value of cached font
     */
    public void setCachedFont(byte[] _cachedFont) {
        cachedFont = _cachedFont;
    }
    
    /**
     * Gets cached font file if one exists, null otherwise
     * @return byte array of cached font file
     */
    public byte[] getCachedFont() {
        return cachedFont;
    } 
    
    
    public PropertiesManager() {
        alphaOrder = new HashMap<Character, Integer>();
    }

    public void setOverrideProgramPath(String override) {
        if (override.equals(PGTUtil.emptyFile)) {
            overrideProgramPath = "";
        } else {
            overrideProgramPath = override;
        }
    }
    
    public String getOverrideProgramPath() {
        return overrideProgramPath;
    }
    
    public void setDisableProcRegex(boolean _disableProcRegex) {
        disableProcRegex = _disableProcRegex;
    }
    
    public boolean isDisableProcRegex() {
        return disableProcRegex;
    }
    
    /**
     * Sets ignore case value for dictionary
     * @param _ignoreCase new value
     */
    public void setIgnoreCase(boolean _ignoreCase) {
        ignoreCase = _ignoreCase;
    }
    
    /**
     * Retrieves ignore case 
     * @return ignore case status of dictionary
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }
    
    /**
     * gets font name for table keeping loading purposes. Does NOT populate from actual font
     * @return font name if any
     */
    public String getFontName() {
        return fontName;
    }
    
    /**
     * Sets font.
     * @param _fontCon The font being set
     * @param _fontStyle The style of the font (bold, underlined, etc.)
     * @param _fontSize Size of font
     */
    public void setFontCon(Font _fontCon, Integer _fontStyle, Integer _fontSize) {
        setFontCon(_fontCon);
        setFontSize(_fontSize);
        setFontStyle(_fontStyle);
    }
    
    // TODO: consider removing- this is a relic from older versions
    /**
     * Sets font name for table keeping loading purposes. Does NOT populate from actual font
     * @param _fontName name to set
     */
    void setFontName(String _fontName) {
        fontName = _fontName;
    }
    
    /**
     * Gets language's font
     * @return the fontCon
     */
    public Font getFontCon() {
        return font == null? new JTextField().getFont() : font.deriveFont(fontStyle, fontSize);
    }

    /**
     * Sets conlang font and nulls cached font value
     * @param fontCon the fontCon to set
     */
    public void setFontCon(Font fontCon) {
        // null cached font if being set to new font
        if (font != null && !font.getFamily().equals(fontCon.getFamily())) {
            cachedFont = null;
        }
        
        font = fontCon;
    }

    /**
     * @return the fontStyle
     */
    public Integer getFontStyle() {
        return fontStyle;
    }

    /**
     * @param _fontStyle the fontStyle to set
     */
    public void setFontStyle(Integer _fontStyle) {
        fontStyle = _fontStyle;
    }

    /**
     * @return the fontSize
     */
    public Integer getFontSize() {
        return fontSize;
    }

    /**
     * @param _fontSize the fontSize to set
     */
    public void setFontSize(Integer _fontSize) {
        fontSize = _fontSize;
    }

    /**
     * @return the proAutoPop
     */
    public boolean isProAutoPop() {
        return proAutoPop;
    }

    /**
     * @param _proAutoPop the proAutoPop to set
     */
    public void setProAutoPop(boolean _proAutoPop) {
        proAutoPop = _proAutoPop;
    }

    /**
     * @return the alphaOrder
     */
    public Map getAlphaOrder() {
        return alphaOrder;
    }

    /**
     * @param order alphabetical order
     */
    public void setAlphaOrder(String order) {
        alphaPlainText = order;
        
        alphaOrder.clear();
        
        for (int i = 0; i < order.length(); i++) {
            alphaOrder.put(order.charAt(i), i);
        }
    }

    /**
     * @return the alphaPlainText
     */
    public String getAlphaPlainText() {
        return alphaPlainText;
    }

    /**
     * @return the langName
     */
    public String getLangName() {
        return langName;
    }

    /**
     * @param langName the langName to set
     */
    public void setLangName(String langName) {
        this.langName = langName;
    }

    /**
     * @return the typesMandatory
     */
    public boolean isTypesMandatory() {
        return typesMandatory;
    }

    /**
     * @param typesMandatory the typesMandatory to set
     */
    public void setTypesMandatory(boolean typesMandatory) {
        this.typesMandatory = typesMandatory;
    }

    /**
     * @return the localMandatory
     */
    public boolean isLocalMandatory() {
        return localMandatory;
    }

    /**
     * @param localMandatory the localMandatory to set
     */
    public void setLocalMandatory(boolean localMandatory) {
        this.localMandatory = localMandatory;
    }

    /**
     * @return the wordUniqueness
     */
    public boolean isWordUniqueness() {
        return wordUniqueness;
    }

    /**
     * @param wordUniqueness the wordUniqueness to set
     */
    public void setWordUniqueness(boolean wordUniqueness) {
        this.wordUniqueness = wordUniqueness;
    }

    /**
     * @return the localUniqueness
     */
    public boolean isLocalUniqueness() {
        return localUniqueness;
    }

    /**
     * @param localUniqueness the localUniqueness to set
     */
    public void setLocalUniqueness(boolean localUniqueness) {
        this.localUniqueness = localUniqueness;
    }

    public String buildPropertiesReport() {
        String ret = "";
        
        ret += ConWordCollection.formatPlain("Language Name: " + langName + "<br><br>");
        
        return ret;
    }
    
    /**
     * Tests whether system has given font installed
     * @param testFont Font to test system for
     * @return true if system has font, false otherwise
     */
    public static boolean testSystemHasFont(Font testFont) {
        boolean ret = false;
        String[] fontNames = java.awt.GraphicsEnvironment
                .getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        
        if (Arrays.asList(fontNames).contains(testFont.getName())) {
            ret = true;
        }
        
        return ret;
    }
    
    /**
     * Writes all dictionary properties to XML document
     * @param doc Document to write dictionary properties to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element wordValue;
        
        // store font for Conlang words
        wordValue = doc.createElement(PGTUtil.fontConXID);
        Font curFont = getFontCon();
        wordValue.appendChild(doc.createTextNode(curFont == null ? "" : curFont.getName()));
        rootElement.appendChild(wordValue);

        // store font style
        wordValue = doc.createElement(PGTUtil.langPropFontStyleXID);
        wordValue.appendChild(doc.createTextNode(getFontStyle().toString()));
        rootElement.appendChild(wordValue);

        // store font for Local words
        wordValue = doc.createElement(PGTUtil.langPropFontSizeXID);
        wordValue.appendChild(doc.createTextNode(getFontSize().toString()));
        rootElement.appendChild(wordValue);

        // store name for conlang
        wordValue = doc.createElement(PGTUtil.langPropLangNameXID);
        wordValue.appendChild(doc.createTextNode(getLangName()));
        rootElement.appendChild(wordValue);

        // store alpha order for conlang
        wordValue = doc.createElement(PGTUtil.langPropAlphaOrderXID);
        wordValue.appendChild(doc.createTextNode(getAlphaPlainText()));
        rootElement.appendChild(wordValue);

        // store option to autopopulate pronunciations
        wordValue = doc.createElement(PGTUtil.proAutoPopXID);
        wordValue.appendChild(doc.createTextNode(isProAutoPop() ? "T" : "F"));
        rootElement.appendChild(wordValue);

        // store option for mandatory Types
        wordValue = doc.createElement(PGTUtil.langPropTypeMandatoryXID);
        wordValue.appendChild(doc.createTextNode(isTypesMandatory() ? "T" : "F"));
        rootElement.appendChild(wordValue);

        // store option for mandatory Local word
        wordValue = doc.createElement(PGTUtil.langPropLocalMandatoryXID);
        wordValue.appendChild(doc.createTextNode(isLocalMandatory() ? "T" : "F"));
        rootElement.appendChild(wordValue);

        // store option for unique local word
        wordValue = doc.createElement(PGTUtil.langPropLocalUniquenessXID);
        wordValue.appendChild(doc.createTextNode(isLocalUniqueness() ? "T" : "F"));
        rootElement.appendChild(wordValue);

        // store option for unique conwords
        wordValue = doc.createElement(PGTUtil.langPropWordUniquenessXID);
        wordValue.appendChild(doc.createTextNode(isWordUniqueness() ? "T" : "F"));
        rootElement.appendChild(wordValue);
        
        // store option for ignoring case
        wordValue = doc.createElement(PGTUtil.langPropIgnoreCase);
        wordValue.appendChild(doc.createTextNode(isIgnoreCase() ? "T" : "F"));
        rootElement.appendChild(wordValue);
        
        // store option for disabling regex or pronunciations
        wordValue = doc.createElement(PGTUtil.langPropDisableProcRegex);
        wordValue.appendChild(doc.createTextNode(isDisableProcRegex()? "T" : "F"));
        rootElement.appendChild(wordValue);
    }
}
