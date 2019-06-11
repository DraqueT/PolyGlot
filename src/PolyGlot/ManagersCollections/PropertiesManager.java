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
package PolyGlot.ManagersCollections;

import PolyGlot.CustomControls.PAlphaMap;
import PolyGlot.DictCore;
import PolyGlot.IOHandler;
import PolyGlot.PGTUtil;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Contains and manages properties of given language
 * @author draque
 */
public class PropertiesManager {
    private String overrideProgramPath = "";
    private Font font = null;
    private Integer fontStyle = 0;
    private Integer conFontSize = 12;
    private double localFontSize = 12;
    private final PAlphaMap<String, Integer> alphaOrder;
    private String alphaPlainText = "";
    private String langName = "";
    private String localLangName = "";
    private String copyrightAuthorInfo = "";
    private boolean typesMandatory = false;
    private boolean localMandatory = false;
    private boolean wordUniqueness = false;
    private boolean localUniqueness = false;
    private boolean overrideRegexFont = false;
    private boolean ignoreCase = false;
    private boolean enableRomanization = false;
    private boolean disableProcRegex = false;
    private boolean enforceRTL = false;
    private byte[] cachedConFont = null;
    private byte[] cachedLocalFont = null;
    private final Font charisUnicode;
    private Font localFont;
    private final Map<String, String> charRep = new HashMap<>();
    private final DictCore core;
    private Double kerningSpace = 0.0;

    public PropertiesManager(DictCore _core) throws IOException {
        alphaOrder = new PAlphaMap<>();
        core = _core;

        // set default font to Charis, as it's unicode compatible
        charisUnicode = IOHandler.getCharisUnicodeFontInitial();
        localFont = charisUnicode;
    }   
    
    /**
     * Gets replacement string for given character. Returns blank otherwise.
     * @param repChar character (string in case I decide to use this for something more complex) to be replaced
     * @return replacement string. empty if none exists.
     */
    public String getCharacterReplacement(String repChar) {
        String ret;
        
        if (!charRep.isEmpty() && charRep.containsKey(repChar)) {
            ret = charRep.get(repChar);
        } else {
            ret = "";
        }
        
        return ret;
    }
    
    /**
     * Adds character/replacement set
     * @param character character to look for/be replaced in text
     * @param _replacement the string to replace the character with
     */
    public void addCharacterReplacement(String character, String _replacement) {
        String replacement = PGTUtil.stripRTL(_replacement);
        
        if (charRep.containsKey(character)) {
            charRep.replace(character, replacement);
        } else {
            charRep.put(character, replacement);
        }
    }
    
    /**
     * Deletes replacement value for a character
     * @param character character for replacement values to be wiped for
     */
    public void delCharacterReplacement(String character) {
        if (charRep.containsKey(character)) {
            charRep.remove(character);
        }
    }
    
    /**
     * Clears all character replacements
     */
    public void clearCharacterReplacement() {
        charRep.clear();
    }
    
    /**
     * Gets all character replacement pairs
     * @return iterator of map entries with two strings apiece
     */
    public ArrayList<Entry<String, String>> getAllCharReplacements() {
        return new ArrayList<>(charRep.entrySet());
    }

    public void AddEmptyRep() {
        charRep.put("", "");
    }
    
    /**
     * Gets unicode charis font. Defaults/hard coded to size 12
     *
     * @return
     */
    public Font getFontMenu() {
        return charisUnicode.deriveFont(0, (float)core.getOptionsManager().getMenuFontSize());
    }
    
    public Font getFontLocal() {
        return getFontLocal(localFontSize);
    }
    
    public Font getFontLocal(double size) {
        return localFont.deriveFont(0, (float)size);
    }
    
    public void setLocalFont(Font _localFont) {
        setLocalFont(_localFont, localFontSize);
    }
    
    public void setLocalFont(Font _localFont, double size) {
        // null cached font if being set to new font
        if (localFont != null && !localFont.getFamily().equals(_localFont.getFamily())) {
            cachedLocalFont = null;
        }
        
        localFont = _localFont; 
        localFontSize = size;
    }
    
    public void setLocalFontSize(double size) {
        localFontSize = size;
    }
    
    /**
     * Gets PolyGlot's cannonical directory, regardless of what the OS returns
     * @return working directory
     */
    public File getCannonicalDirectory() {
        File ret;
        
        if (overrideProgramPath.length() == 0) {
            ret = IOHandler.getBaseProgramPath();
        } else {
            ret = new File(overrideProgramPath);
        }
        
        return ret;
    }

    /**
     * Gets the java FX version of an AWT font
     *
     * @return javafx font
     */
    public javafx.scene.text.Font getFXFont() {
        javafx.scene.text.Font ret;

        if (font == null) {
            ret = (new javafx.scene.control.TextField()).getFont();
        } else {
            java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            ret = javafx.scene.text.Font.font(font.getFamily(), conFontSize);
        }

        return ret;
    }

    /**
     * Sets value of cached font file as byte array
     *
     * @param _cachedFont value of cached font
     */
    public void setCachedFont(byte[] _cachedFont) {
        cachedConFont = _cachedFont;
    }

    /**
     * Gets cached font file if one exists, null otherwise
     *
     * @return byte array of cached font file
     */
    public byte[] getCachedFont() {
        return cachedConFont;
    }
    
    public void setCachedLocalFont(byte[] _cachedLocalFont) {
        cachedLocalFont = _cachedLocalFont;
    }

    public byte[] getCachedLocalFont() {
        return cachedLocalFont;
    }
    
    public void setFontFromFile(String fontPath) throws IOException, FontFormatException {
        cachedConFont = IOHandler.getFileByteArray(fontPath);
        
        setFontCon(IOHandler.getFontFromFile(fontPath).deriveFont(fontStyle, conFontSize), fontStyle, conFontSize);
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

    public void setEnforceRTL(boolean _enforceRTL) {
        enforceRTL = _enforceRTL;
    }

    public boolean isEnforceRTL() {
        return enforceRTL;
    }

    /**
     * Sets ignore case value for dictionary
     *
     * @param _ignoreCase new value
     */
    public void setIgnoreCase(boolean _ignoreCase) {
        ignoreCase = _ignoreCase;
    }

    /**
     * Retrieves ignore case
     *
     * @return ignore case status of dictionary
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * Sets font.
     * 
     * Will first try to re-load the font from OS font repository folder (due to ligature error in Java)
     *
     * @param _fontCon The font being set
     * @param _fontStyle The style of the font (bold, underlined, etc.)
     * @param _fontSize Size of font
     */
    public void setFontCon(Font _fontCon, Integer _fontStyle, Integer _fontSize) {
        Font switchToFont = IOHandler.loadFontFromOSFileFolder(_fontCon);
        
        if (switchToFont == null) {
            switchToFont = _fontCon;
        }
        
        setFontConRaw(switchToFont);
        setFontSize(_fontSize);
        setFontStyle(_fontStyle);
    }
    
    /**
     * Tries to load font from OS file, defaults to pulling from Font if unable
     * (pulling from Font disables ligatures)
     * @param _fontFamily 
     * @throws java.lang.Exception if unable to load font 
     */
    public void setFontCon(String _fontFamily) throws Exception {
        try {
            Font newFont = IOHandler.loadFontFromOSFileFolder(Font.getFont(_fontFamily));

            if (newFont == null) {
                newFont = Font.getFont(_fontFamily);
            } 

            setFontConRaw(newFont);
        } catch (Exception e) {
            throw new Exception ("Unable to find or set font: " + _fontFamily + " due to: \n");
        }
    }

    public void setFontCon(Font _font) {
        setFontCon(_font, getFontStyle(), getFontSize());
    }
    
    /**
     * Gets language's font
     *
     * @return the fontCon
     */
    public Font getFontCon() {
        // create copy so that initial font properties (such as kerning) is never lost
        Font retFont = font == null ? null : font.deriveFont((float)0.0);
        
        // under certain circumstances, this can default to 0...
        if (conFontSize == 0) {
            conFontSize = 12;
        }

        if (retFont != null && kerningSpace != 0.0) {
            retFont = PGTUtil.addFontAttribute(TextAttribute.TRACKING, kerningSpace, font);
        }
        
        return retFont == null ? 
                charisUnicode.deriveFont((float)core.getOptionsManager().getMenuFontSize()) : 
                retFont.deriveFont(fontStyle, conFontSize);
    }

    /**
     * Sets conlang font and nulls cached font value.
     * This is to be used only by the CustHandlerFactory and internally, as it sets the font as its raw
     * value rather than by first searching for an appropriate file beforehand.
     *
     * @param fontCon the fontCon to set
     */
    public void setFontConRaw(Font fontCon) {
        // null cached font if being set to new font
        if (font != null && !font.getFamily().equals(fontCon.getFamily())) {
            cachedConFont = null;
        }

        font = fontCon == null ? charisUnicode : fontCon;
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
        font = font.deriveFont(fontStyle, conFontSize);
    }

    /**
     * @return the fontSize
     */
    public Integer getFontSize() {
        return conFontSize;
    }

    /**
     * Cannot be set to 0 or lower. Will default to 12 if set to 0 or lower.
     *
     * @param _fontSize the fontSize to set
     */
    public void setFontSize(Integer _fontSize) {
        if (_fontSize != null) {
            conFontSize = _fontSize < 0 ? 12 : _fontSize;
            font = font.deriveFont(fontStyle, conFontSize);
        }
    }

    /**
     * @return the alphaOrder
     */
    public PAlphaMap<String, Integer> getAlphaOrder() {
        return alphaOrder;
    }

    public void setAlphaOrder(String order) throws Exception {
        setAlphaOrder(order, false);
    }
    
    /**
     * @param order alphabetical order
     * @param overrideDupe Override provides a method to prevent a check on repetitions
     * (necessary for loading of legacy language files prior to v 2.4)
     * @throws java.lang.Exception
     */
    public void setAlphaOrder(String order, boolean overrideDupe) throws Exception {
        alphaPlainText = order;
        String error = "";

        alphaOrder.clear();

        // if comma delimited, alphabet may contain multiple character values
        if (order.contains(",")) {
            String[] orderVals = order.split(",");
            
            for (int i = 0; i < orderVals.length; i++) {
                String newEntry = orderVals[i].trim();
                if (newEntry.isEmpty()) {
                    continue;
                }
                
                if (alphaOrder.containsKey(newEntry) && ! overrideDupe) {
                    error += "Alphabet contains duplicate entry: " + newEntry;
                }
                else {
                    alphaOrder.put(newEntry, i);
                }
            }
        } else {
            for (int i = 0; i < order.length(); i++) {
                String newEntry = order.substring(i, i+1);
                
                if (alphaOrder.containsKey(newEntry) && !overrideDupe) {
                    error += "Alphabet contains duplicate entry: " + newEntry;
                }
                else {
                    alphaOrder.put(newEntry, i);
                }
            }
        }
        
        if (!error.isEmpty()) {
            throw new Exception(error.trim());
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

    public String buildPropertiesReportTitle() {
        String ret = "";

        ret += ConWordCollection.formatPlain("Language Name: " + langName + "<br><br>", core);

        return ret;
    }

    /**
     * Tests whether system has given font installed
     *
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
     *
     * @param doc Document to write dictionary properties to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element propContainer = doc.createElement(PGTUtil.langPropertiesXID);
        Element wordValue;
        
        rootElement.appendChild(propContainer);

        // store font for Conlang words
        wordValue = doc.createElement(PGTUtil.fontConXID);
        Font curFont = getFontCon();
        wordValue.appendChild(doc.createTextNode(curFont == null ? "" : curFont.getFamily()));
        propContainer.appendChild(wordValue);

        // store font style
        wordValue = doc.createElement(PGTUtil.langPropFontStyleXID);
        wordValue.appendChild(doc.createTextNode(getFontStyle().toString()));
        propContainer.appendChild(wordValue);

        // store font size
        wordValue = doc.createElement(PGTUtil.langPropFontSizeXID);
        wordValue.appendChild(doc.createTextNode(getFontSize().toString()));
        propContainer.appendChild(wordValue);
        
        // store font size for local language font
        wordValue = doc.createElement(PGTUtil.langPropLocalFontSizeXID);
        wordValue.appendChild(doc.createTextNode(Double.toString(localFontSize)));
        propContainer.appendChild(wordValue);

        // store name for conlang
        wordValue = doc.createElement(PGTUtil.langPropLangNameXID);
        wordValue.appendChild(doc.createTextNode(getLangName()));
        propContainer.appendChild(wordValue);

        // store alpha order for conlang
        wordValue = doc.createElement(PGTUtil.langPropAlphaOrderXID);
        wordValue.appendChild(doc.createTextNode(getAlphaPlainText()));
        propContainer.appendChild(wordValue);

        // store option for mandatory Types
        wordValue = doc.createElement(PGTUtil.langPropTypeMandatoryXID);
        wordValue.appendChild(doc.createTextNode(isTypesMandatory() ? PGTUtil.True : PGTUtil.False));
        propContainer.appendChild(wordValue);

        // store option for mandatory Local word
        wordValue = doc.createElement(PGTUtil.langPropLocalMandatoryXID);
        wordValue.appendChild(doc.createTextNode(isLocalMandatory() ? PGTUtil.True : PGTUtil.False));
        propContainer.appendChild(wordValue);

        // store option for unique local word
        wordValue = doc.createElement(PGTUtil.langPropLocalUniquenessXID);
        wordValue.appendChild(doc.createTextNode(isLocalUniqueness() ? PGTUtil.True : PGTUtil.False));
        propContainer.appendChild(wordValue);

        // store option for unique conwords
        wordValue = doc.createElement(PGTUtil.langPropWordUniquenessXID);
        wordValue.appendChild(doc.createTextNode(isWordUniqueness() ? PGTUtil.True : PGTUtil.False));
        propContainer.appendChild(wordValue);

        // store option for ignoring case
        wordValue = doc.createElement(PGTUtil.langPropIgnoreCaseXID);
        wordValue.appendChild(doc.createTextNode(isIgnoreCase() ? PGTUtil.True : PGTUtil.False));
        propContainer.appendChild(wordValue);

        // store option for disabling regex or pronunciations
        wordValue = doc.createElement(PGTUtil.langPropDisableProcRegexXID);
        wordValue.appendChild(doc.createTextNode(isDisableProcRegex() ? PGTUtil.True : PGTUtil.False));
        propContainer.appendChild(wordValue);

        // store option for enforcing RTL in conlang
        wordValue = doc.createElement(PGTUtil.langPropEnforceRTLXID);
        wordValue.appendChild(doc.createTextNode(isEnforceRTL() ? PGTUtil.True : PGTUtil.False));
        propContainer.appendChild(wordValue);
        
        // store option for overriding the regex display font
        wordValue = doc.createElement(PGTUtil.langPropOverrideRegexFont);
        wordValue.appendChild(doc.createTextNode(isOverrideRegexFont() ? PGTUtil.True : PGTUtil.False));
        propContainer.appendChild(wordValue);

        // store option for Author and copyright info
        wordValue = doc.createElement(PGTUtil.langPropAuthCopyrightXID);
        wordValue.appendChild(doc.createTextNode(copyrightAuthorInfo));
        propContainer.appendChild(wordValue);

        // store option local language name
        wordValue = doc.createElement(PGTUtil.langPropLocalLangNameXID);
        wordValue.appendChild(doc.createTextNode(localLangName));
        propContainer.appendChild(wordValue);
        
        // store kerning value (default 0)
        wordValue = doc.createElement(PGTUtil.langPropKerningVal);
        wordValue.appendChild(doc.createTextNode(kerningSpace.toString()));
        propContainer.appendChild(wordValue);
        
        // store all replacement pairs
        wordValue = doc.createElement(PGTUtil.langPropCharRepContainerXID);
        for (Entry<String, String> pair : getAllCharReplacements()) {
            Element node = doc.createElement(PGTUtil.langPropCharRepNodeXID);
            
            Element val = doc.createElement(PGTUtil.langPropCharRepCharacterXID);
            val.appendChild(doc.createTextNode(pair.getKey()));
            node.appendChild(val);
            
            val = doc.createElement(PGTUtil.langPropCharRepValueXID);
            val.appendChild(doc.createTextNode(pair.getValue()));
            node.appendChild(val);
            
            wordValue.appendChild(node);
        }
        propContainer.appendChild(wordValue);
    }

    /**
     * @return the localLangName
     */
    public String getLocalLangName() {
        return localLangName;
    }

    /**
     * @param localLangName the localLangName to set
     */
    public void setLocalLangName(String localLangName) {
        this.localLangName = localLangName;
    }

    /**
     * @return the copyrightAuthorInfo
     */
    public String getCopyrightAuthorInfo() {
        return copyrightAuthorInfo;
    }

    /**
     * @param copyrightAuthorInfo the copyrightAuthorInfo to set
     */
    public void setCopyrightAuthorInfo(String copyrightAuthorInfo) {
        this.copyrightAuthorInfo = copyrightAuthorInfo;
    }

    /**
     * @return the enableRomanization
     */
    public boolean isEnableRomanization() {
        return enableRomanization;
    }

    /**
     * @param enableRomanization the enableRomanization to set
     */
    public void setEnableRomanization(boolean enableRomanization) {
        this.enableRomanization = enableRomanization;
    }

    /**
     * @return the kerningSpace
     */
    public Double getKerningSpace() {
        return kerningSpace;
    }

    /**
     * @param kerningSpace the kerningSpace to set
     */
    public void setKerningSpace(Double kerningSpace) {
        this.kerningSpace = kerningSpace;
    }
    
    /**
     * Tests whether all characters within word are covered by ordered alphabet
     * @param testString string to test
     * @return true if string comprised of only characters defined in alphabet or if no alphabet defined
     * order menu
     */
    public boolean testStringAgainstAlphabet(String testString) {
        boolean ret = true;
        int longestChar = alphaOrder.getLongestEntry();
        
        if (!alphaOrder.isEmpty()) {
            String currentCharacter = ""; // Linguistic character (can be made up of multiple string entries)
            
            // loop on every character
            for (char c : testString.toCharArray()) {
                currentCharacter += c; // add current character to unmatched prior character (or set value if last character matched)
                
                // if current string longer than any recorded, fail
                if (currentCharacter.length() > longestChar) {
                    ret = false;
                    break;
                }
                
                // if current character found, blank (otherwise loop to add)
                if (alphaOrder.containsKey(currentCharacter)) {
                    currentCharacter = "";
                }
            }
            
            // if not blanked, the last characte never matched
            if (!currentCharacter.isEmpty()) {
                ret = false;
            }
        }
        
        return ret;
    }

    /**
     * @return the overrideRegexFont
     */
    public boolean isOverrideRegexFont() {
        return overrideRegexFont;
    }

    /**
     * @param overrideRegexFont the overrideRegexFont to set
     */
    public void setOverrideRegexFont(boolean overrideRegexFont) {
        this.overrideRegexFont = overrideRegexFont;
    }
}
