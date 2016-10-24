/*
 * Copyright (c) 2016, draque
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
package PolyGlot.CustomControls;

import PolyGlot.DictCore;
import PolyGlot.PGTools;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 *
 * @author draque
 */
public class PTextArea extends JTextArea{
    private SwingWorker worker = null;
    private final String defText;
    private final DictCore core;
    private final boolean overrideFont;
    
    public PTextArea(DictCore _core, boolean _overideFont, String _defText) {
        core = _core;
        defText = _defText;
        overrideFont = _overideFont;
        
        setupListeners();
        setText(defText);
        setForeground(Color.lightGray);
        if (!overrideFont) {
            setFont(core.getPropertiesManager().getFontCon());
        } else {
            setFont(core.getPropertiesManager().getCharisUnicodeFont());
        }
    }
    
    @Override
    public final void setFont(Font _font) {
        super.setFont(_font);
    }
        
    @Override
    public final void setForeground(Color _color) {
        super.setForeground(_color);
    }
    
    @Override
    public final void setText(String t) {
        try {
            if (t.equals("") && !this.hasFocus()) {
                super.setText(defText);
            } else {
                super.setText(t);
            }
        } catch (Exception e) {
            InfoBox.error("Set text error", "Could not set text component: " + e.getLocalizedMessage(), null);
        }
        
        if (isDefaultText()) {
            setForeground(Color.lightGray);
        } else {
            setForeground(Color.black);
        }
    }
    
    /**
     * makes this component flash. If already flashing, does nothing.
     * @param _flashColor color to flash
     * @param isBack whether display color is background (rather than foreground)
     */
    public void makeFlash(Color _flashColor, boolean isBack) {
        if (worker == null || worker.isDone()) {
            worker = PGTools.getFlashWorker(this, _flashColor, isBack);
            worker.execute();
        }
    }
    
    /**
     * gets default value string of text
     * @return default text
     */
    public String getDefaultValue() {
        return defText;
    }
    
    /**
     * Tests whether the current text value is the default value
     * @return 
     */
    public boolean isDefaultText() {
        // account for RtL languages
        String curText = super.getText().replaceAll("\u202e", "").replaceAll("\u202c", "");
        return curText.equals(defText);
    }
    
    /**
     * sets text to default value
     */
    public void setDefault() {
        setText(defText);
    }
    
    private void setupListeners() {
        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getSuperText().equals(defText)) {
                    setText("");
                    setForeground(Color.black);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getSuperText().equals("")) {
                    setText(defText);
                    setForeground(Color.lightGray);
                }
            }
        });
    }
    
    @Override
    public String getText() {
        String ret = super.getText().replaceAll("\u202e", "").replaceAll("\u202c", "");
        
        if (ret.equals(defText)) {
            ret = "";
        } else {
            ret = core.getPropertiesManager().isEnforceRTL() ? "\u202e" + ret : ret;
        }
        
        return ret;
    }
    
    /**
     * Allows super method to be called in listeners
     * @return 
     */
    private String getSuperText() {
        return super.getText().replaceAll("\u202e", "").replaceAll("\u202c", "");
    }
}
