/*
 * Copyright (c) 2015-2023, Draque Thompson, draquemail@gmail.com
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
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.darisadesigns.polyglotlina.CustomControls.CoreUpdateSubscriptionInterface;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;

/**
 *
 * @author draque
 */
public final class PTextField extends JTextField implements CoreUpdateSubscriptionInterface {

    private DictCore core;
    private boolean overrideFont = false;
    private String defText;
    private Integer contentId = -1;
    private Object associatedObject = null;
    
    public PTextField() {
        this("");
    }
    
    public PTextField(String _defText) {
        this(true, _defText);
    }
    
    public PTextField(boolean _overrideFont, String _defText) {
        this(PolyGlot.getPolyGlot().getCore(), _overrideFont, _defText);
    }
    
    /**
     * Init for PTextField
     *
     * @param _core dictionary core
     * @param _overrideFont true overrides ConFont, false sets to default
     * @param _defText default text that will display in grey if otherwise empty
     */
    public PTextField(DictCore _core, boolean _overrideFont, String _defText) {
        setCore(_core);
        defText = _defText;
        overrideFont = _overrideFont;
        setupListeners();
        setupRightClickMenu();
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
            setForeground(VisualStyleManager.getTextColor(nightMode));
            setBackground(VisualStyleManager.getTextBGColor(nightMode));
        } else {
            setForeground(VisualStyleManager.getDisabledTextColor(nightMode));
            setBackground(Color.black);
        }
        
        this.putClientProperty("Nimbus.Overrides", PolyGlot.getPolyGlot().getUiDefaults());
    }

    @Override
    public void setCore(DictCore _core) {
        if (core != _core && _core != null) {
            _core.subscribe(this);
        }
        
        core = _core;
    }

    public void setDefaultValue(String _defText) {
        defText = _defText;
    }

    private void setupListeners() {
        final PTextField self = this;
        
        // add a listener for character replacement if conlang font not overridden
        if (!overrideFont) {
            this.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    handleCharacterReplacement(core, e, self);
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
        
        // unsub from core when window is disposed
        var topLevelParent = SwingUtilities.getWindowAncestor(this);
        if (topLevelParent != null) {
            topLevelParent.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    core.unSubscribe(self);
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

    @Override
    public void updateFromCore() {
        if (overrideFont) {
            this.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal());
        } else {
            this.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon());
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        try {
            super.paintComponent(g); 
        } catch (Exception e) {
            // Almost certainly unneccessary, to catch a rare javax.swing.text.GlyphPainter1.sync() bug in Java
        }
        
        if (core == null) {
            return;
        }
        
        // display default text if blank and is either not focused, or is not editable
        if (getText().isEmpty() && (!isFocusOwner() || !isEditable())) {
            var fontMetrics = g.getFontMetrics();
            var displayDefText = "-- " + defText + " --";
            g.setColor(Color.lightGray);
            g.setFont(PGTUtil.MENU_FONT);
            var xPosition = (getWidth()/2) - (fontMetrics.stringWidth(displayDefText)/2);
            g.drawString(displayDefText, xPosition, fontMetrics.getHeight());
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
            paste();
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
