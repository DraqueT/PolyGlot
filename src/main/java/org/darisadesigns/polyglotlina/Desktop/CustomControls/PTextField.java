/*
 * Copyright (c) 2015-2021, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop.CustomControls;

import org.darisadesigns.polyglotlina.Desktop.ClipboardHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.VisualStyleManager;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
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
import org.darisadesigns.polyglotlina.CustomControls.CoreUpdateSubscriptionInterface;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;

/**
 *
 * @author draque
 */
public final class PTextField extends JTextField implements CoreUpdateSubscriptionInterface {

    private DictCore core;
    private boolean skipRepaint = false;
    private boolean curSetText = false;
    private boolean overrideFont = false;
    private SwingWorker worker = null;
    private String defText;
    private EventListenerList tmpListenerList = null;
    private Integer contentId = -1;
    private Object associatedObject = null;
    private Font localFont;
    private boolean refreshFontRender = true;
    private String lastRenderedValue = "";
    private FontMetrics localMetrics;
    private FontMetrics conMetrics;
    private int drawPosition;
    
    /**
     * Init for PDialogs
     *
     * @param _core dictionary core
     * @param _overrideFont true overrides ConFont, false sets to default
     * @param _defText default text that will display in grey if otherwise empty
     */
    public PTextField(DictCore _core, boolean _overrideFont, String _defText) {
        // remove change listener to add custom one
        DefaultBoundedRangeModel pVis = (DefaultBoundedRangeModel) this.getHorizontalVisibility();
        for (ChangeListener chlist : pVis.getChangeListeners()) {
            pVis.removeChangeListener(chlist);
        }
        pVis.addChangeListener(new PScrollRepainter());

        setCore(_core);
        defText = _defText;
        overrideFont = _overrideFont;
        setupListeners();
        setForeground(Color.lightGray);
        setupRightClickMenu();
        setText(defText);
        setupLook();
        
        if (overrideFont || !defText.isBlank()) {
            setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal());
        } else {
            setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon());
        }
        
        setHorizontalAlignment(JTextField.CENTER);
    }
    
    public void setupLook() {
        boolean nightMode = PolyGlot.getPolyGlot().getOptionsManager().isNightMode();
        
        if (this.isEnabled()) {
            if (isDefaultText()) {
                setForeground(VisualStyleManager.getDefaultTextColor(nightMode));
            } else {
                setForeground(VisualStyleManager.getTextColor(nightMode));
            }
            setBackground(VisualStyleManager.getTextBGColor(nightMode));
        } else {
            setForeground(VisualStyleManager.getDisabledTextColor(nightMode));
            setBackground(Color.black);
        }
        
        this.putClientProperty("Nimbus.Overrides", PolyGlot.getPolyGlot().getUiDefaults());
    }
    
    @Override
    public void setBackground(Color b) {
        super.setBackground(b);
    }

    @Override
    public void setCore(DictCore _core) {
        if (core != _core && _core != null) {
            _core.subscribe(this);
        }
        
        core = _core;
    }

    @Override
    public void setForeground(Color _color) {
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
        String curText = super.getText().replaceAll(PGTUtil.RTL_CHARACTER, "").replaceAll(PGTUtil.LTR_MARKER, "");
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
                    if (getSuperText().isEmpty()) {
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
        if (!repString.isEmpty()) {
            try {
                e.consume();
                ClipboardHandler cb = new ClipboardHandler();
                cb.cacheClipboard();
                cb.setClipboardContents(repString);
                target.paste();
                cb.restoreClipboard();
            } catch (Exception ex) {
                DesktopIOHandler.getInstance().writeErrorLog(ex);
                core.getOSHandler().getInfoBox().error("Character Replacement Error",
                        "Clipboard threw error during character replacement process:"
                        + ex.getLocalizedMessage());
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
            worker = PGTUtil.getFlashWorker(this, _flashColor, isBack);
            worker.execute();
        }
    }

    @Override
    public void updateFromCore() {
        if (overrideFont) {
            this.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal());
        } else {
            this.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon());
        }
    }

    public class PScrollRepainter implements ChangeListener, Serializable {

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
        if (super.getText().startsWith(PGTUtil.RTL_CHARACTER)) {
            return;
        }

        setText(PGTUtil.RTL_CHARACTER + getText());
    }

    @Override
    public void paint(Graphics g) {
        if (skipRepaint || core == null) {
            return;
        }

        if (isDefaultText()) {
            setForeground(Color.lightGray);
        } else {
            setForeground(Color.black);
        }
        
        try {
            DesktopPropertiesManager propMan = ((DesktopPropertiesManager)core.getPropertiesManager());
            skipRepaint = true;
            if (propMan != null
                    && !curSetText
                    && propMan.isEnforceRTL()
                    && !overrideFont) {
                prefixRTL();
            }
            skipRepaint = false;
        } catch (Exception e) {
            core.getOSHandler().getInfoBox().error("Repaint error", "Could not repaint component: " + e.getLocalizedMessage());
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
        
        // paint default text if appropriate
        if (!defText.isBlank() && !isDefaultText()) {
            if (refreshFontRender) {
                localFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
                localMetrics = g.getFontMetrics(localFont);
                conMetrics = g.getFontMetrics(getFont());
                refreshFontRender = false;
            }
            
            var curText = getText();
            
            if (curText.isBlank() && this.isFocusOwner()) { // initial selection w/ empty text
                drawPosition = (getWidth() / 2) - localMetrics.stringWidth(defText);
            } else if (!lastRenderedValue.equals(curText)) { // recalc width only if text changed (expensive)
                lastRenderedValue = curText;
                drawPosition = (getWidth() / 2) 
                    - (conMetrics.stringWidth(lastRenderedValue) / 2)
                    - localMetrics.stringWidth(defText);
            }
            
            g.setFont(localFont);
            g.setColor(Color.lightGray);
            g.drawString(defText, drawPosition - 10, conMetrics.getHeight());
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
    public void setFont(Font font) {
        super.setFont(font);
        refreshFontRender = true;
    }

    @Override
    public void setText(String t) {
        curSetText = true;
        try {
            if (t.isEmpty() && !this.hasFocus()) {
                super.setText(defText);
            } else {
                super.setText(t);
            }
        } catch (Exception e) {
            // e.printStackTrace();
            DesktopIOHandler.getInstance().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Set text error", "Could not set text component: " 
                    + e.getLocalizedMessage());
            DesktopIOHandler.getInstance().writeErrorLog(e);
        }

        if (isDefaultText() && !defText.isEmpty()) {
            float menuFontSize = (float)PolyGlot.getPolyGlot().getOptionsManager().getMenuFontSize();
            setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal().deriveFont(menuFontSize));
            setForeground(Color.lightGray);
        } else {
            if (!overrideFont) {
                setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon());
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
        return super.getText().replaceAll(PGTUtil.RTL_CHARACTER, "").replaceAll(PGTUtil.LTR_MARKER, "");
    }

    @Override
    /**
     * Make certain only to return appropriate text and never default text
     */
    public String getText() {
        String ret = super.getText().replaceAll(PGTUtil.RTL_CHARACTER, "").replaceAll(PGTUtil.LTR_MARKER, "");

        if (ret.equals(defText)) {
            ret = "";
        } else {
            ret = (core.getPropertiesManager().isEnforceRTL() && !overrideFont) ? PGTUtil.RTL_CHARACTER + ret : ret;
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
     * @param _contentId Sets the ID for whatever content this field holds
     */
    public void setContentId(Integer _contentId) {
        this.contentId = _contentId;
    }

    /**
     * @return the associatedObject
     */
    public Object getAssociatedObject() {
        return associatedObject;
    }

    /**
     * @param _associatedObject the associatedObject to set
     */
    public void setAssociatedObject(Object _associatedObject) {
        this.associatedObject = _associatedObject;
    }
}
