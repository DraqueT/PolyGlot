/*
 * Copyright (c) 2015, draque
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
import PolyGlot.ManagersCollections.PropertiesManager;
import PolyGlot.PGTools;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author draque
 */
public class PTextField extends JTextField {
    private final DictCore core;
    boolean skipRepaint = false;
    boolean curSetText = false;
    boolean overrideFont = false;
    private SwingWorker worker = null;
    private final String defText;

    /**
     * Init for PDialogs
     *
     * @param _core dictionary core
     * @param _overideFont whether to override font (default to no)
     * @param _defText default text that will display in grey if otherwise empty
     */
    public PTextField(DictCore _core, boolean _overideFont, String _defText) {
        // remove change listener to add custom one
        DefaultBoundedRangeModel pVis = (DefaultBoundedRangeModel) this.getHorizontalVisibility();
        for (ChangeListener chlist : pVis.getChangeListeners()) {
            pVis.removeChangeListener(chlist);
        }
        pVis.addChangeListener(new PScrollRepainter());
        
        core = _core;
        overrideFont = _overideFont;
        defText = _defText;
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
    
    // Overridden to meet code standards
    @Override
    public final BoundedRangeModel getHorizontalVisibility() {
        return super.getHorizontalVisibility();
    }

    class PScrollRepainter implements ChangeListener, Serializable {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!skipRepaint && !curSetText) {
                repaint();
            }
        }
    }

    /**
     * Prefixes the RTL character if not prefixed already
     */
    private void prefixRTL() {
        if (super.getText().startsWith("\u202e")) {
            return;
        }

        setText('\u202e' + getText());
    }

    @Override
    public void paint(Graphics g) {
        if (skipRepaint || core == null) {
            return;
        }
        
        try {
            PropertiesManager propMan = core.getPropertiesManager();
            skipRepaint = true;
            if (propMan != null
                    && !curSetText
                    && propMan.isEnforceRTL()) {
                prefixRTL();
            }
            skipRepaint = false;
        } catch (Exception e) {
            InfoBox.error("Repaint error", "Could not repaint component: " + e.getLocalizedMessage(), null);
            skipRepaint = false;
        }

        try {
            super.paint(g);
        } catch (NullPointerException e) {
            /* Do nothing. This fires due to a Java bug between the 
             javax.swing.text.GlyphView class returning null values of fonts in 
             some instances where the javax.swing.text.GlyphPainter1.sync() class
             method is unable to properly handle it (it never checks an object for 
             a null value when the object is populated from a method that returns
             null under certain circumstances). Thanks, Java.*/
        }
    }

    @Override
    public final void setText(String t) {
        curSetText = true;
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

        curSetText = false;
    }

    /**
     * Gets text from super with minimal processing
     * @return super's text
     */
    private String getSuperText() {
        return super.getText().replaceAll("\u202e", "").replaceAll("\u202c", "");
    }
    
    @Override
    /**
     * Make certain only to return appropriate text and never default text
     */
    public String getText() {
        String ret = super.getText().replaceAll("\u202e", "").replaceAll("\u202c", "");
        
        if (ret.equals(defText)) {
            ret = "";
        } else {
            ret = core.getPropertiesManager().isEnforceRTL() ? "\u202e" + ret : ret;
        }
        
        return ret;
    }
}
