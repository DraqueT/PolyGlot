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
package PolyGlot.CustomControls;

import PolyGlot.ClipboardHandler;
import PolyGlot.DictCore;
import PolyGlot.ManagersCollections.PropertiesManager;
import PolyGlot.PGTUtil;
import PolyGlot.PGTools;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.Serializable;
import java.util.Map;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.JTextComponent;

/**
 *
 * @author draque
 */
public class PTextField extends JTextField {

    private DictCore core;
    boolean skipRepaint = false;
    boolean curSetText = false;
    boolean overrideFont = false;
    private SwingWorker worker = null;
    private String defText;
    private EventListenerList tmpListenerList = null;
    private Integer contentId = -1;
    private Object associatedObject = null;

    /**
     * Init for PDialogs
     *
     * @param _core dictionary core
     * @param _overideFont true overrides ConFont, false sets to default
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
        setForeground(Color.lightGray);
        setupRightClickMenu();
        if (!overrideFont) {
            setFont(core.getPropertiesManager().getFontCon());
        } else {
            setFont(core.getPropertiesManager().getFontLocal());
        }
        setText(defText);
    }

    @Override
    public final void setFont(Font _font) {
        Font setFont = _font;

        // if conlang font and core exists, set font kerning
        if (core != null && !overrideFont) {
            Map attr = _font.getAttributes();
            attr.put(TextAttribute.TRACKING, core.getPropertiesManager().getKerningSpace());
            setFont = _font.deriveFont(attr);
        }

        super.setFont(setFont);
    }

    public void setOverrideFont(boolean _overrideFont) {
        overrideFont = _overrideFont;

        if (!overrideFont) {
            setFont(core.getPropertiesManager().getFontCon());
        } else {
            setFont(core.getPropertiesManager().getFontLocal());
        }
    }

    public boolean getOverrideFont() {
        return overrideFont;
    }

    public void setCore(DictCore _core) {
        core = _core;
        setOverrideFont(overrideFont);
    }

    @Override
    public final void setForeground(Color _color) {
        super.setForeground(_color);
    }

    /**
     * gets default value string of text
     *
     * @return default text
     */
    public String getDefaultValue() {
        return defText;
    }

    public void setDefaultValue(String _default) {
        defText = _default;
    }

    /**
     * Tests whether the current text value is the default value
     *
     * @return
     */
    public boolean isDefaultText() {
        // account for RtL languages
        String curText = super.getText().replaceAll(PGTUtil.RTLMarker, "").replaceAll(PGTUtil.LTRMarker, "");
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
                SwingUtilities.invokeLater(() -> {
                    if (getSuperText().equals(defText)) {
                        setText("");
                        setForeground(Color.black);
                    }
                });
            }

            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (getSuperText().length() == 0) {
                        setText(defText);
                        setForeground(Color.lightGray);
                    }
                });
            }
        });

        final PTextField me = this;
        // add a listener for character replacement if conlang font not overridden
        if (!overrideFont) {
            this.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    handleCharacterReplacement(core, e, me);
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    // do nothing
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    // do nothing
                }
            });
        }
    }

    /***
     * Handles character replacement for arbitrary text target of KeyEvents
     * @param core Dictionary core
     * @param e key event (passed from listener)
     * @param target (target object, typically "this")
     */
    public static void handleCharacterReplacement(DictCore core, KeyEvent e, JTextComponent target) {
        Character c = e.getKeyChar();
        String repString = core.getPropertiesManager().getCharacterReplacement(c.toString());
        if (repString.length() != 0) {
            try {
                e.consume();
                ClipboardHandler cb = new ClipboardHandler();
                cb.cacheClipboard();
                cb.setClipboardContents(repString);
                target.paste();
                cb.restoreClipboard();
            } catch (Exception ex) {
                InfoBox.error("Character Replacement Error",
                        "Clipboard threw error during character replacement process:"
                        + ex.getLocalizedMessage(), core.getRootWindow());
            }
        }
    }

    /**
     * makes this component flash. If already flashing, does nothing.
     *
     * @param _flashColor color to flash
     * @param isBack whether display color is background (rather than
     * foreground)
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
        if (super.getText().startsWith(PGTUtil.RTLMarker)) {
            return;
        }

        setText(PGTUtil.RTLMarker + getText());
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
                    && propMan.isEnforceRTL()
                    && !overrideFont) {
                prefixRTL();
            }
            skipRepaint = false;
        } catch (Exception e) {
            InfoBox.error("Repaint error", "Could not repaint component: " + e.getLocalizedMessage(), core.getRootWindow());
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

    /**
     * Returns true if currently setting text (useful in constructed listeners)
     *
     * @return
     */
    public boolean isSettingText() {
        return curSetText;
    }

    @Override
    public final void setText(String t) {
        curSetText = true;
        try {
            if (t.length() == 0 && !this.hasFocus()) {
                super.setText(defText);
            } else {
                super.setText(t);
            }
        } catch (Exception e) {
            InfoBox.error("Set text error", "Could not set text component: " 
                    + e.getLocalizedMessage(), core.getRootWindow());
            //e.printStackTrace();
        }

        if (isDefaultText()) {
            setFont(core.getPropertiesManager().getFontLocal());
            setForeground(Color.lightGray);
        } else {
            if (!overrideFont) {
                setFont(core.getPropertiesManager().getFontCon());
            }
            setForeground(Color.black);
        }

        curSetText = false;
    }

    /**
     * Gets text from super with minimal processing
     *
     * @return super's text
     */
    private String getSuperText() {
        return super.getText().replaceAll(PGTUtil.RTLMarker, "").replaceAll(PGTUtil.LTRMarker, "");
    }

    @Override
    /**
     * Make certain only to return appropriate text and never default text
     */
    public String getText() {
        String ret = super.getText().replaceAll(PGTUtil.RTLMarker, "").replaceAll(PGTUtil.LTRMarker, "");

        if (ret.equals(defText)) {
            ret = "";
        } else {
            ret = (core.getPropertiesManager().isEnforceRTL() && !overrideFont) ? PGTUtil.RTLMarker + ret : ret;
        }

        return ret;
    }

    /**
     * Stops current listeners from listening (can only be called once before
     * needing to be told to listen once again
     */
    public void stopListening() {
        if (tmpListenerList == null) {
            tmpListenerList = listenerList;
            listenerList = new EventListenerList();
        }
    }

    /**
     * Turns listeners on again (can only be called when not listening
     */
    public void startListening() {
        if (tmpListenerList != null) {
            listenerList = tmpListenerList;
            tmpListenerList = null;
        }
    }

    private void setupRightClickMenu() {
        final JPopupMenu ruleMenu = new JPopupMenu();
        final JMenuItem cut = new JMenuItem("Cut");
        final JMenuItem copy = new JMenuItem("Copy");
        final JMenuItem paste = new JMenuItem("Paste");
        final PTextField parentField = this;

        cut.addActionListener((ActionEvent ae) -> {
            cut();
        });
        copy.addActionListener((ActionEvent ae) -> {
            copy();
        });
        paste.addActionListener((ActionEvent ae) -> {
            if (isDefaultText()) { //removes default text if appropriate
                superSetText("");
            }
            paste();
            setText(getText()); // ensures text is not left grey
        });

        ruleMenu.add(cut);
        ruleMenu.add(copy);
        ruleMenu.add(paste);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && parentField.isEnabled()) {
                    cut.setEnabled(true);
                    copy.setEnabled(true);
                    paste.setEnabled(true);
                    ruleMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && parentField.isEnabled()) {
                    cut.setEnabled(true);
                    copy.setEnabled(true);
                    paste.setEnabled(true);
                    ruleMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * Exposes super's set text to menu items
     *
     * @param text
     */
    private void superSetText(String text) {
        super.setText(text);
    }

    /**
     * @return The ID of whatever content this field holds
     */
    public Integer getContentId() {
        return contentId;
    }

    /**
     * @param contentId Sets the ID for whatever content this field holds
     */
    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    /**
     * @return the associatedObject
     */
    public Object getAssociatedObject() {
        return associatedObject;
    }

    /**
     * @param associatedObject the associatedObject to set
     */
    public void setAssociatedObject(Object associatedObject) {
        this.associatedObject = associatedObject;
    }
}
