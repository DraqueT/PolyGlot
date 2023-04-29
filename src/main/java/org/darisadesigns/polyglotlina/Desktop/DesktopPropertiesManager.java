/*
 * Copyright (c) 2021-2023, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.ManagersCollections.PropertiesManager;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class DesktopPropertiesManager extends PropertiesManager {
    private Font conFont = null;
    private Font localFont = PGTUtil.CHARIS_UNICODE;
    
    public DesktopPropertiesManager() {
        super();
        this.conFontStyle = Font.PLAIN;
    }
    
    /**
     * Gets unicode charis font. Defaults/hard coded to size 12
     * 
     * @return
     */
    public Font getFontMenu() {
        return PGTUtil.CHARIS_UNICODE.deriveFont(PGTUtil.DEFAULT_FONT_SIZE.floatValue());
    }
    
    public Font getFontLocal() {
        return getFontLocal(localFontSize);
    }
    
    public Font getFontLocal(double size) {
        return localFont.deriveFont(localFont.getStyle(), (float)size);
    }
    
    public void setLocalFont(Font _localFont) {
        setLocalFont(_localFont, localFontSize);
        core.getOSHandler().getPFontHandler().updateLocalFont(core);
    }
    
    public void setLocalFont(Font _localFont, double size) {
        // null cached font if being set to new font
        if (localFont != null && !localFont.getFamily().equals(_localFont.getFamily())) {
            cachedLocalFont = null;
        }
        
        localFont = _localFont; 
        localFontSize = size;
        core.getOSHandler().getPFontHandler().updateLocalFont(core);
    }
    
    @Override
    public void setLocalFontSize(double size) {
        localFontSize = size;
        core.getOSHandler().getPFontHandler().updateLocalFont(core);
    }
    
    public double getLocalFontSize() {
        return localFontSize;
    }
    
    /**
     * Gets the java FX version of an AWT font
     *
     * @return javafx font
     */
    public javafx.scene.text.Font getFXConFont() {
        javafx.scene.text.Font ret;

        if (cachedConFont != null) { // first try to load from cached file...
            ret = javafx.scene.text.Font.loadFont(new ByteArrayInputStream(cachedConFont), conFontSize);
        } else if (conFont != null) { // second try to load from registered OS fonts...
            java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(conFont);
            ret = javafx.scene.text.Font.font(conFont.getFamily(), conFontSize);
        } else { // last default to menu standard
            ret = javafx.scene.text.Font.loadFont(new PFontHandler().getCharisInputStream(), PGTUtil.DEFAULT_FONT_SIZE);
        }

        return ret;
    }
    
    /**
     * Gets the java FX version of an AWT font
     *
     * @return javafx font
     */
    public javafx.scene.text.Font getFXLocalFont() {
        javafx.scene.text.Font ret;

        if (cachedLocalFont != null) { // first try to load from cached file...
            ret = javafx.scene.text.Font.loadFont(new ByteArrayInputStream(cachedLocalFont), localFontSize);
        } else if (conFont != null) { // second try to load from registered OS fonts...
            java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(localFont);
            ret = javafx.scene.text.Font.font(localFont.getFamily(), localFontSize);
        } else { // last default to menu standard
            ret = javafx.scene.text.Font.loadFont(new PFontHandler().getCharisInputStream(), PGTUtil.DEFAULT_FONT_SIZE);
        }

        return ret;
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
        if (conFont != null 
                && fontCon!= null 
                && !conFont.getFamily().equals(fontCon.getFamily())) {
            cachedConFont = null;
        }

        conFont = fontCon == null ? PGTUtil.CHARIS_UNICODE : fontCon;
    }
    
    public void setFontFromFile(String fontPath) throws IOException, FontFormatException {
        setFontCon(PFontHandler.getFontFromFile(fontPath)
                .deriveFont(conFontStyle, (float)conFontSize), conFontStyle, (float)conFontSize);
        cachedConFont = core.getOSHandler().getIOHandler().getFileByteArray(fontPath);
    }
    
    public void setLocalFontFromFile(String fontPath) throws IOException, FontFormatException {
        var font = PFontHandler.getFontFromFile(fontPath)
                .deriveFont((float)localFontSize);
        setLocalFont(font);
        cachedLocalFont = core.getOSHandler().getIOHandler().getFileByteArray(fontPath);
    }

    /**
     * Synchronizes cached font with confont set. 
     * @return true on success, false if no matching font found
     * @throws java.lang.Exception on read error
     */
    public boolean syncCachedFontCon() throws Exception {
        File fontFile = PFontHandler.getFontFile(conFont.getFamily());
        
        if (fontFile == null) {
            return false;
        }
        
        cachedConFont = null;
        if (fontFile.getName().toLowerCase().endsWith("ttc")) {
            throw new Exception("PolyGlot does not currently support ttc (true type collection) caching or ligatures.");
        } else if (fontFile.exists()) {
            // set cached version
            cachedConFont = core.getOSHandler().getIOHandler().getByteArrayFromFile(fontFile);
            
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
    @Override
    public void setFontCon(String _fontFamily) throws Exception {
        try {
            Font newFont = PFontHandler.loadFontFromOSFileFolder(_fontFamily);

            if (newFont == null) {
                newFont = Font.getFont(_fontFamily);
            } 

            // only replace the conFont like this if it does not already match (you risk losing diacritic placement otherwise)
            if (newFont != null 
                    && !(conFont != null && newFont.getFamily().equals(conFont.getFamily()))) {
                setFontConRaw(newFont);
            }
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
        
        return retFont == null ? 
                PGTUtil.CHARIS_UNICODE.deriveFont(conFontStyle, (float)conFontSize) : 
                retFont.deriveFont(conFontStyle, (float)conFontSize);
    }
    
    /**
     * @param _fontStyle the fontStyle to set
     */
    @Override
    public void setFontStyle(Integer _fontStyle) {
        conFontStyle = _fontStyle;
        
        if (conFont != null) {
            conFont = conFont.deriveFont(conFontStyle, (float)conFontSize);
        }
    }
    
    /**
     * Cannot be set to 0 or lower. Will default to 12 if set to 0 or lower.
     *
     * @param _fontSize the fontSize to set
     */
    @Override
    public void setFontSize(double _fontSize) {
        var curFont = this.getFontCon();
        conFontSize = _fontSize < 0 ? 12 : _fontSize;
        conFont = curFont.deriveFont(conFontStyle, (float)conFontSize);
    }
    
    /**
     * 
     * @return font con family
     */
    @Override
    public String getFontConFamily() {
        Font curFont = getFontCon();
        return curFont == null ? "" : curFont.getFamily();
    }
    
    /**
     * 
     * @return font local family
     */
    @Override
    public String getFontLocalFamily() {
        Font curFont = getFontLocal();
        return curFont == null ? "" : curFont.getFamily();
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
     * Refreshes all fonts in PolyGlot, ensuring that the most recent versions
     * of given fonts installed on the system are used.
     * @throws java.lang.Exception
     */
    @Override
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
                cachedConFont = core.getOSHandler().getIOHandler().getByteArrayFromFile(updatedConFont);
            }
            
            if (updatedLocalFont != null) {
                localFont = PFontHandler.getFontFromFile(updatedLocalFont.getAbsolutePath());
                localFont = localFont.deriveFont((float)localFontSize);
                cachedLocalFont = core.getOSHandler().getIOHandler().getByteArrayFromFile(updatedLocalFont);
            }
        } catch (Exception e) {
            core.getOSHandler().getIOHandler().writeErrorLog(e);
            throw new Exception("Unable to refresh fonts: " + e.getLocalizedMessage());
        }
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp instanceof DesktopPropertiesManager) {
            DesktopPropertiesManager prop = (DesktopPropertiesManager) comp;
            ret = getFontCon().equals(prop.getFontCon());
            ret = ret && conFontStyle.equals(prop.conFontStyle);
            ret = ret && conFontSize == prop.conFontSize;
            ret = ret && localFontSize == prop.localFontSize;
            ret = ret && localFont.equals(prop.localFont);
            ret = ret && super.equals(comp);
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.conFont);
        hash = 53 * hash + Objects.hashCode(this.localFont);
        return hash;
    }
}
