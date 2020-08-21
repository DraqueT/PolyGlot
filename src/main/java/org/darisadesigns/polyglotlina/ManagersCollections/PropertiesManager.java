/*
 * Copyright (c) 2014-2020, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import org.darisadesigns.polyglotlina.CustomControls.PAlphaMap;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.PFontHandler;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.TextAttribute;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.WebInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Contains and manages properties of given language
 * @author draque
 */
public class PropertiesManager {
    private Font conFont = null;
    private Integer conFontStyle = Font.PLAIN;
    private double conFontSize = 12;
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
    private boolean disableProcRegex = false;
    private boolean enforceRTL = false;
    private boolean useLocalWordLex = false;
    private byte[] cachedConFont = null;
    private byte[] cachedLocalFont = null;
    private Font localFont = PGTUtil.CHARIS_UNICODE;
    private final Map<String, String> charRep = new HashMap<>();
    private final DictCore core;
    private Double kerningSpace = 0.0;
    private boolean useSimplifiedConjugations = false;

    public PropertiesManager(DictCore _core) {
        alphaOrder = new PAlphaMap<>();
        core = _core;
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
        charRep.remove(character);
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
    public Iterable<Entry<String, String>> getAllCharReplacements() {
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
        return PGTUtil.CHARIS_UNICODE.deriveFont(Font.PLAIN, (float)core.getOptionsManager().getMenuFontSize());
    }
    
    public Font getFontLocal() {
        return getFontLocal(localFontSize);
    }
    
    public Font getFontLocal(double size) {
        return localFont.deriveFont(Font.PLAIN, (float)size);
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
     * Gets the java FX version of an AWT font
     *
     * @return javafx font
     */
    public javafx.scene.text.Font getFXFont() {
        javafx.scene.text.Font ret;

        if (cachedConFont != null) { // first try to load from cached file...
            ret = javafx.scene.text.Font.loadFont(new ByteArrayInputStream(cachedConFont), conFontSize);
        } else if (conFont != null) { // second try to load from registered OS fonts...
            java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(conFont);
            ret = javafx.scene.text.Font.font(conFont.getFamily(), conFontSize);
        } else { // last default to menu standard
            ret = javafx.scene.text.Font.loadFont(new PFontHandler().getCharisInputStream(), core.getOptionsManager().getMenuFontSize());
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
        setFontCon(PFontHandler.getFontFromFile(fontPath)
                .deriveFont(conFontStyle, (float)conFontSize), conFontStyle, (float)conFontSize);
        cachedConFont = IOHandler.getFileByteArray(fontPath);
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
     * Synchronizes cached font with confont set. 
     * @return true on success, false if no matching font found
     * @throws java.lang.Exception on read error
     */
    public boolean syncCachedFontCon() throws Exception {
        File fontFile = PFontHandler.getFontFile(conFont.getFamily());
        cachedConFont = null;
        if (fontFile.getName().toLowerCase().endsWith("ttc")) {
            throw new Exception("PolyGlot does not currently support ttc (true type collection) caching or ligatures.");
        } else if (fontFile.exists()) {
            // set cached version
            cachedConFont = IOHandler.getByteArrayFromFile(fontFile);
            
            // load font from binary location (superior due to ligature support from binaries)
            conFont = PFontHandler.getFontFromFile(fontFile.getCanonicalPath());
        }
        
        return cachedConFont != null;
    }
    
    /**
     * Sets font. Cached font byte array will be cleared.
     * 
     * Will first try to re-load the font from OS font repository folder (due to ligature error in Java)
     *
     * @param _fontCon The font being set
     * @param _fontStyle The style of the font (bold, underlined, etc.)
     * @param _fontSize Size of font
     */
    public void setFontCon(Font _fontCon, Integer _fontStyle, double _fontSize) {
        setFontConRaw(_fontCon);
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
            Font newFont = PFontHandler.loadFontFromOSFileFolder(_fontFamily);

            if (newFont == null) {
                newFont = Font.getFont(_fontFamily);
            } 

            setFontConRaw(newFont);
        } catch (Exception e) {
            throw new Exception ("Unable to find or set font: " + _fontFamily + " due to: \n", e);
        }
    }
    
    /**
     * Gets language's font
     *
     * @return the fontCon
     */
    public Font getFontCon() {
        // create copy so that initial font properties (such as kerning) is never lost
        Font retFont = conFont == null ? null : conFont.deriveFont((float)0.0);
        
        // under certain circumstances, this can default to 0...
        if (conFontSize == 0) {
            conFontSize = 12;
        }

        if (retFont != null && kerningSpace != 0.0) {
            retFont = PGTUtil.addFontAttribute(TextAttribute.TRACKING, kerningSpace, conFont);
        }
        
        return retFont == null ? 
                PGTUtil.CHARIS_UNICODE.deriveFont((float)core.getOptionsManager().getMenuFontSize()) : 
                retFont.deriveFont(conFontStyle, (float)conFontSize);
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
        if (conFont != null && !conFont.getFamily().equals(fontCon.getFamily())) {
            cachedConFont = null;
        }

        conFont = fontCon == null ? PGTUtil.CHARIS_UNICODE : fontCon;
    }

    /**
     * @return the fontStyle
     */
    public Integer getFontStyle() {
        return conFontStyle;
    }

    /**
     * @param _fontStyle the fontStyle to set
     */
    public void setFontStyle(Integer _fontStyle) {
        conFontStyle = _fontStyle;
        conFont = conFont.deriveFont(conFontStyle, (float)conFontSize);
    }

    /**
     * @return the fontSize
     */
    public double getFontSize() {
        return conFontSize;
    }

    /**
     * Cannot be set to 0 or lower. Will default to 12 if set to 0 or lower.
     *
     * @param _fontSize the fontSize to set
     */
    public void setFontSize(double _fontSize) {
        conFontSize = _fontSize < 0 ? 12 : _fontSize;
        conFont = conFont.deriveFont(conFontStyle, (float)conFontSize);
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
     * @param _langName the langName to set
     */
    public void setLangName(String _langName) {
        this.langName = _langName;
    }

    /**
     * @return the typesMandatory
     */
    public boolean isTypesMandatory() {
        return typesMandatory;
    }

    /**
     * @param _typesMandatory the typesMandatory to set
     */
    public void setTypesMandatory(boolean _typesMandatory) {
        this.typesMandatory = _typesMandatory;
    }

    /**
     * @return the localMandatory
     */
    public boolean isLocalMandatory() {
        return localMandatory;
    }

    /**
     * @param _localMandatory the localMandatory to set
     */
    public void setLocalMandatory(boolean _localMandatory) {
        this.localMandatory = _localMandatory;
    }

    /**
     * @return the wordUniqueness
     */
    public boolean isWordUniqueness() {
        return wordUniqueness;
    }

    /**
     * @param _wordUniqueness the wordUniqueness to set
     */
    public void setWordUniqueness(boolean _wordUniqueness) {
        this.wordUniqueness = _wordUniqueness;
    }

    /**
     * @return the localUniqueness
     */
    public boolean isLocalUniqueness() {
        return localUniqueness;
    }

    /**
     * @param _localUniqueness the localUniqueness to set
     */
    public void setLocalUniqueness(boolean _localUniqueness) {
        this.localUniqueness = _localUniqueness;
    }

    public String buildPropertiesReportTitle() {
        String ret = "";

        ret += ConWordCollection.formatPlain("Language Name: " + WebInterface.encodeHTML(langName) + "<br><br>", core);

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
        Element propContainer = doc.createElement(PGTUtil.LANG_PROPERTIES_XID);
        Element wordValue;
        
        rootElement.appendChild(propContainer);

        // store font for Conlang words ONLY if no cached font
        if (cachedConFont == null) {
            wordValue = doc.createElement(PGTUtil.FONT_CON_XID);
            Font curFont = getFontCon();
            wordValue.appendChild(doc.createTextNode(curFont == null ? "" : curFont.getFamily()));
            propContainer.appendChild(wordValue);
        }

        // store font style
        wordValue = doc.createElement(PGTUtil.LANG_PROP_FONT_STYLE_XID);
        wordValue.appendChild(doc.createTextNode(conFontStyle.toString()));
        propContainer.appendChild(wordValue);

        // store font size
        wordValue = doc.createElement(PGTUtil.LANG_PROP_FONT_SIZE_XID);
        wordValue.appendChild(doc.createTextNode(Double.toString(conFontSize)));
        propContainer.appendChild(wordValue);
        
        // store font size for local language font
        wordValue = doc.createElement(PGTUtil.LANG_PROP_LOCAL_FONT_SIZE_XID);
        wordValue.appendChild(doc.createTextNode(Double.toString(localFontSize)));
        propContainer.appendChild(wordValue);

        // store name for conlang
        wordValue = doc.createElement(PGTUtil.LANG_PROP_LANG_NAME_XID);
        wordValue.appendChild(doc.createTextNode(langName));
        propContainer.appendChild(wordValue);

        // store alpha order for conlang
        wordValue = doc.createElement(PGTUtil.LANG_PROP_ALPHA_ORDER_XID);
        wordValue.appendChild(doc.createTextNode(alphaPlainText));
        propContainer.appendChild(wordValue);

        // store option for mandatory Types
        wordValue = doc.createElement(PGTUtil.LANG_PROP_TYPE_MAND_XID);
        wordValue.appendChild(doc.createTextNode(typesMandatory ? PGTUtil.TRUE : PGTUtil.FALSE));
        propContainer.appendChild(wordValue);

        // store option for mandatory Local word
        wordValue = doc.createElement(PGTUtil.LANG_PROP_LOCAL_MAND_XID);
        wordValue.appendChild(doc.createTextNode(localMandatory ? PGTUtil.TRUE : PGTUtil.FALSE));
        propContainer.appendChild(wordValue);

        // store option for unique local word
        wordValue = doc.createElement(PGTUtil.LANG_PROP_LOCAL_UNIQUE_XID);
        wordValue.appendChild(doc.createTextNode(localUniqueness ? PGTUtil.TRUE : PGTUtil.FALSE));
        propContainer.appendChild(wordValue);

        // store option for unique conwords
        wordValue = doc.createElement(PGTUtil.LANG_PROP_WORD_UNIQUE_XID);
        wordValue.appendChild(doc.createTextNode(wordUniqueness ? PGTUtil.TRUE : PGTUtil.FALSE));
        propContainer.appendChild(wordValue);

        // store option for ignoring case
        wordValue = doc.createElement(PGTUtil.LANG_PROP_IGNORE_CASE_XID);
        wordValue.appendChild(doc.createTextNode(ignoreCase ? PGTUtil.TRUE : PGTUtil.FALSE));
        propContainer.appendChild(wordValue);

        // store option for disabling regex or pronunciations
        wordValue = doc.createElement(PGTUtil.LANG_PROP_DISABLE_PROC_REGEX);
        wordValue.appendChild(doc.createTextNode(disableProcRegex ? PGTUtil.TRUE : PGTUtil.FALSE));
        propContainer.appendChild(wordValue);

        // store option for enforcing RTL in conlang
        wordValue = doc.createElement(PGTUtil.LANG_PROP_ENFORCE_RTL_XID);
        wordValue.appendChild(doc.createTextNode(enforceRTL ? PGTUtil.TRUE : PGTUtil.FALSE));
        propContainer.appendChild(wordValue);
        
        // store option for overriding the regex display font
        wordValue = doc.createElement(PGTUtil.LANG_PROP_OVERRIDE_REGEX_FONT_XID);
        wordValue.appendChild(doc.createTextNode(overrideRegexFont ? PGTUtil.TRUE : PGTUtil.FALSE));
        propContainer.appendChild(wordValue);

        // store option for displaying local words in lexicon
        wordValue = doc.createElement(PGTUtil.LANG_PROP_USE_LOCAL_LEX_XID);
        wordValue.appendChild(doc.createTextNode(useLocalWordLex ? PGTUtil.TRUE : PGTUtil.FALSE));
        propContainer.appendChild(wordValue);

        // store option for Author and copyright info
        wordValue = doc.createElement(PGTUtil.LANG_PROP_AUTH_COPYRIGHT_XID);
        wordValue.appendChild(doc.createTextNode(copyrightAuthorInfo));
        propContainer.appendChild(wordValue);

        // store option local language name
        wordValue = doc.createElement(PGTUtil.LANG_PROP_LOCAL_NAME_XID);
        wordValue.appendChild(doc.createTextNode(localLangName));
        propContainer.appendChild(wordValue);
        
        // store kerning value (default 0)
        wordValue = doc.createElement(PGTUtil.LANG_PROP_KERN_VAL_XID);
        wordValue.appendChild(doc.createTextNode(kerningSpace.toString()));
        propContainer.appendChild(wordValue);
        
        // store option to use simplified conjugation autogeneration
        wordValue = doc.createElement(PGTUtil.LANG_PROP_USE_SIMPLIFIED_CONJ);
        wordValue.appendChild(doc.createTextNode(useSimplifiedConjugations ? PGTUtil.TRUE : PGTUtil.FALSE));
        propContainer.appendChild(wordValue);
        
        // store all replacement pairs
        wordValue = doc.createElement(PGTUtil.LANG_PROP_CHAR_REP_CONTAINER_XID);
        for (Entry<String, String> pair : getAllCharReplacements()) {
            Element node = doc.createElement(PGTUtil.LANG_PROPCHAR_REP_NODE_XID);
            
            Element val = doc.createElement(PGTUtil.LANG_PROP_CHAR_REP_CHAR_XID);
            val.appendChild(doc.createTextNode(pair.getKey()));
            node.appendChild(val);
            
            val = doc.createElement(PGTUtil.LANG_PROP_CHAR_REP_VAL_XID);
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
     * @param _localLangName the localLangName to set
     */
    public void setLocalLangName(String _localLangName) {
        this.localLangName = _localLangName;
    }

    /**
     * @return the copyrightAuthorInfo
     */
    public String getCopyrightAuthorInfo() {
        return copyrightAuthorInfo;
    }

    /**
     * @param _copyrightAuthorInfo the copyrightAuthorInfo to set
     */
    public void setCopyrightAuthorInfo(String _copyrightAuthorInfo) {
        this.copyrightAuthorInfo = _copyrightAuthorInfo;
    }

    /**
     * @return the kerningSpace
     */
    public Double getKerningSpace() {
        return kerningSpace;
    }

    /**
     * @param _kerningSpace the kerningSpace to set
     */
    public void setKerningSpace(Double _kerningSpace) {
        this.kerningSpace = _kerningSpace;
    }
    
    /**
     * Tests whether the alphabet covers all words in the lexicon
     * @return 
     */
    public boolean isAlphabetComplete() {
        boolean ret = true;
        
        for (ConWord word : core.getWordCollection().getWordNodes()) {
            if (!testStringAgainstAlphabet(word.getValue())) {
                ret = false;
                break;
            }
        }
        
        return ret;
    }
    
    /**
     * Tests whether all characters within word are covered by ordered alphabet
     * @param testString string to test
     * @return true if string comprised of only characters defined in alphabet or if no alphabet defined
     * order menu
     */
    public boolean testStringAgainstAlphabet(String testString) {
        int longestChar = alphaOrder.getLongestEntry();
        boolean ret = false;
        
        // an empty string means having reached the end of the word without issue. Return true.
        if (testString.isEmpty()) {
            ret = true;
        } else if (!alphaOrder.isEmpty()) {
            String currentCharacter = ""; // Linguistic character (can be made up of multiple string entries)
            
            for (char c : testString.toCharArray()) {
                if (c == ' ') { // spaces are skipped in all parsing
                    continue;
                }
                
                currentCharacter += c; // add current character to unmatched prior character (or set value if last character matched)
                
                // if current string longer than any recorded, fail
                if (currentCharacter.length() > longestChar) {
                    ret = false;
                    break;
                } else if (alphaOrder.containsKey(currentCharacter) 
                        && testStringAgainstAlphabet(testString.substring(currentCharacter.length()))) {
                    ret = true;
                    break;
                }
            }
        }
        
        return ret;
    }
    
    /**
     * Attempts to find characters which are present in a word but missing from the alphabet
     * @param search
     * @return 
     */
    public String findBadLetters(String search) {
        for (int curLength = 1; curLength < search.length(); curLength++) {
            for (int pos = 0; pos + curLength < search.length(); pos++) {
                // remove segment to test
                String clearedString = search.substring(0, pos)
                        + search.substring(pos + curLength, search.length() - 1);
                
                // if removed segment makes the string legal, it's likely the culprit
                if (testStringAgainstAlphabet(clearedString)) {
                    return search.substring(pos, pos + curLength);
                }
            }
        }
        
        return "???";
    }
    
    /**
     * @return the overrideRegexFont
     */
    public boolean isOverrideRegexFont() {
        return overrideRegexFont;
    }

    /**
     * @param _overrideRegexFont the overrideRegexFont to set
     */
    public void setOverrideRegexFont(boolean _overrideRegexFont) {
        this.overrideRegexFont = _overrideRegexFont;
    }

    /**
     * @return the useLocalWordLex
     */
    public boolean isUseLocalWordLex() {
        return useLocalWordLex;
    }

    /**
     * @param _useLocalWordLex the useLocalWordLex to set
     */
    public void setUseLocalWordLex(boolean _useLocalWordLex) {
        this.useLocalWordLex = _useLocalWordLex;
    }
    
    /**
     * Refreshes all fonts in PolyGlot, ensuring that the most recent versions
     * of given fonts installed on the system are used.
     * @throws java.lang.Exception
     */
    public void refreshFonts() throws Exception {
        try {
            File updatedConFont = null;
            File updatedLocalFont = null;
            
            if (conFont != null) {
                updatedConFont = PFontHandler.getFontFile(conFont.getFamily());
            }
            
            if (localFont != null) {
                updatedLocalFont = PFontHandler.getFontFile(localFont.getFamily());
            }
        
            if (updatedConFont != null) {
                conFont = PFontHandler.getFontFromFile(updatedConFont.getAbsolutePath());
                conFont = conFont.deriveFont(conFontStyle, (float)conFontSize);
                cachedConFont = IOHandler.getByteArrayFromFile(updatedConFont);
            }
            
            if (updatedLocalFont != null) {
                localFont = PFontHandler.getFontFromFile(updatedLocalFont.getAbsolutePath());
                localFont = localFont.deriveFont((float)localFontSize);
                cachedLocalFont = IOHandler.getByteArrayFromFile(updatedLocalFont);
            }
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            throw new Exception("Unable to refresh fonts: " + e.getLocalizedMessage());
        }
    }

    /**
     * @return the useSimplifiedConjugations
     */
    public boolean isUseSimplifiedConjugations() {
        return useSimplifiedConjugations;
    }

    /**
     * @param _useSimplifiedConjugations the useSimplifiedConjugations to set
     */
    public void setUseSimplifiedConjugations(boolean _useSimplifiedConjugations) {
        this.useSimplifiedConjugations = _useSimplifiedConjugations;
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp instanceof PropertiesManager) {
            PropertiesManager prop = (PropertiesManager) comp;
            ret = ((conFont == null) && (prop.conFont == null)) || conFont.equals(prop.conFont);
            ret = ret && conFontStyle.equals(prop.conFontStyle);
            ret = ret && conFontSize == prop.conFontSize;
            ret = ret && localFontSize == prop.localFontSize;
            ret = ret && alphaPlainText.equals(prop.alphaPlainText);
            ret = ret && langName.equals(prop.langName);
            ret = ret && localLangName.equals(prop.localLangName);
            ret = ret && copyrightAuthorInfo.equals(prop.copyrightAuthorInfo);
            ret = ret && typesMandatory == prop.typesMandatory;
            ret = ret && localMandatory == prop.localMandatory;
            ret = ret && wordUniqueness == prop.wordUniqueness;
            ret = ret && localUniqueness == prop.localUniqueness;
            ret = ret && overrideRegexFont == prop.overrideRegexFont;
            ret = ret && ignoreCase == prop.ignoreCase;
            ret = ret && disableProcRegex == prop.disableProcRegex;
            ret = ret && enforceRTL == prop.enforceRTL;
            ret = ret && useLocalWordLex == prop.useLocalWordLex;
            ret = ret && localFont.equals(prop.localFont);
            ret = ret && charRep.equals(prop.charRep);
            ret = ret && kerningSpace.equals(prop.kerningSpace);
            ret = ret && useSimplifiedConjugations == prop.useSimplifiedConjugations;
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.langName);
        return hash;
    }
}
