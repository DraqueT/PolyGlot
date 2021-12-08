/*
 * Copyright (c) 2016-2021, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.ClipboardHandler;
import org.darisadesigns.polyglotlina.FormattedTextHelper;
import org.darisadesigns.polyglotlina.Nodes.ImageNode;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.WebInterface;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import org.darisadesigns.polyglotlina.CustomControls.CoreUpdateSubscriptionInterface;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;

/**
 *
 * @author draque
 */
public final class PTextPane extends JTextPane implements CoreUpdateSubscriptionInterface {

    private SwingWorker worker = null;
    private final String defText;
    private DictCore core;
    private boolean disableMedia = false;
    private final boolean overrideFont;

    public PTextPane(DictCore _core, boolean _overrideFont, String _defText) {
        overrideFont = _overrideFont;
        setCore(_core);
        defText = _defText;
        this.setContentType("text/html");
        setupRightClickMenu();

        setupListeners();
        setText(defText);
        
        this.setEditorKit(new PHTMLEditorKit());
    }
    
    public void setDisableMedia(boolean _disableMedia) {
        disableMedia = _disableMedia;
    }
    
    public boolean isDisableMedia() {
        return disableMedia;
    }
    
    /**
     * Sets/updates font from the core
     */
    public void setFontFromCore() {
        if (overrideFont) {
            setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal());
        } else {
            setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon());
        }
    }
    
    /**
     * Captures images in override, passes everything else on to super.
     */
    @Override
    public void paste() {
        // might handle more types in the future
        if (ClipboardHandler.isClipboardString()) {
            try {
                // sanitize contents to plain text
                ClipboardHandler board = new ClipboardHandler();
                board.setClipboardContents(ClipboardHandler.getClipboardText());
                super.paste();
            } catch (UnsupportedFlavorException | IOException e) {
                DesktopIOHandler.getInstance().writeErrorLog(e);
                core.getOSHandler().getInfoBox().error("Paste Error", "Unable to paste text: " + e.getLocalizedMessage());
            }
        } else if (ClipboardHandler.isClipboardImage() && !disableMedia) {
            try {
                Image imageObject = ClipboardHandler.getClipboardImage();
                BufferedImage image = null;
                if (imageObject instanceof BufferedImage) {
                    image = (BufferedImage)imageObject;
                } else if (imageObject != null) {
                    image = new BufferedImage(imageObject.getWidth(null),
                            imageObject.getHeight(null), BufferedImage.TYPE_INT_ARGB);

                    // Draw the image on to the buffered image
                    Graphics2D bGr = image.createGraphics();
                    bGr.drawImage(imageObject, 0, 0, null);
                    bGr.dispose();
                } else {
                    new DesktopInfoBox().error("Paste Error", "Unable to paste image. Object is null.");
                }
                
                if (image != null) {
                    ImageNode imageNode = DesktopIOHandler.getInstance().getFromBufferedImage(image);
                    addImage(imageNode);
                }
            } catch (Exception e) {
                DesktopIOHandler.getInstance().writeErrorLog(e);
                core.getOSHandler().getInfoBox().error("Paste Error", "Unable to paste: " + e.getLocalizedMessage());
            }
        } else {
            super.paste();
        }
    }
    
    /**
     * Takes raw text and sets it as html
     * @param t 
     */
    public void setHtmlTextFromRaw(String t) {
        var html = t.replaceAll("\n", "<br>");
        setText(html);
    }

    @Override
    public void setText(String t) {
        try {
            if (t.isEmpty() && !this.hasFocus()) {
                super.setText(defText);
            } else {
                super.setText(t);
            }
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Set text error", "Could not set text component: " + e.getLocalizedMessage());
        }

        if (isDefaultText() && !defText.isBlank()) {
            setForeground(Color.lightGray);
        } else {
            setForeground(Color.BLACK);
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

    /**
     * gets default value string of text
     *
     * @return default text
     */
    public String getDefaultValue() {
        return defText;
    }

    @Override
    public String getToolTipText() {
        String ret = super.getToolTipText();
        
        if (ret != null && !disableMedia) {
            ret += " (right click to insert images)";
        }
        
        return ret;
    }
    
    /**
     * Exposes super's set text to menu items
     * @param text 
     */
    private void superSetText(String text) {
        super.setText(text);
    }

    private void setupRightClickMenu() {
        final JPopupMenu ruleMenu = new JPopupMenu();
        final JMenuItem insertImage = new JMenuItem("Insert Image");
        final JMenuItem cut = new JMenuItem("Cut");
        final JMenuItem copy = new JMenuItem("Copy");
        final JMenuItem paste = new JMenuItem("Paste");
        final PTextPane parentPane = this;

        insertImage.setToolTipText("Insert Image into Text");
        insertImage.addActionListener((ActionEvent ae) -> {
            try {
                ImageNode image = DesktopIOHandler.getInstance()
                        .openNewImage((Window)parentPane.getTopLevelAncestor(), core.getWorkingDirectory());
                if (image != null) {
                    // null node means user cancelled process
                    addImage(image);
                }
            } catch (Exception e) {
                DesktopIOHandler.getInstance().writeErrorLog(e);
                core.getOSHandler().getInfoBox().error("Image Import Error", "Unable to import image: "
                        + e.getLocalizedMessage());
            }
        });
        cut.addActionListener((ActionEvent ae) -> {
            cut();
        });
        copy.addActionListener((ActionEvent ae) -> {
            copy();
        });
        paste.addActionListener((ActionEvent ae) -> {
            if(isDefaultText()) { //removes default text if appropriate
                superSetText("");
            }
            paste();
            setText(getText()); // ensures text is not left grey
        });

        ruleMenu.add(insertImage);
        ruleMenu.add(cut);
        ruleMenu.add(copy);
        ruleMenu.add(paste);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && parentPane.isEnabled()) {
                    insertImage.setEnabled(!disableMedia);
                    cut.setEnabled(true);
                    copy.setEnabled(true);
                    paste.setEnabled(true);
                    ruleMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && parentPane.isEnabled()) {
                    insertImage.setEnabled(true);
                    cut.setEnabled(true);
                    copy.setEnabled(true);
                    paste.setEnabled(true);
                    ruleMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    @Override
    public void setCore(DictCore _core) {
        if (core != _core && _core != null) {
            _core.subscribe(this);
        }
        
        core = _core;
        setFontFromCore();
    }

    /**
     * Adds image to text at point of current carat
     * @param image
     * @throws Exception 
     */
    private void addImage(ImageNode image) throws Exception {
        final String placeHold = "-POLYGLOTIMAGE-";
        
        if (isDefaultText()) {
            super.setText("");
        }
        
        ClipboardHandler test = new ClipboardHandler();

        test.cacheClipboard();
        test.setClipboardContents(placeHold);

        super.paste();
        String newText = getRawHTML();
        String imagePath = image.getImagePath();
        imagePath = "<img src=\"file:///" + imagePath + "\">";
        setText(newText.replace(placeHold, imagePath));
        test.restoreClipboard();
    }

    /**
     * Tests whether the current text value is the default value
     *
     * @return
     */
    public boolean isDefaultText() {
        String body = super.getText();
        body = body.substring(0, body.indexOf("</body>"));
        body = body.substring(body.indexOf("<body>") + 6);
        return body.trim().equals(defText);
    }
    
    /**
     * Returns true if object's body is empty (accounts for HTML elements within
     * body)
     * @return 
     */
    public boolean isEmpty() {
        boolean ret;
        
        try{
            String body = super.getText();
            body = body.substring(0, body.indexOf("</body>"));
            body = body.substring(body.lastIndexOf("<body>") + 6);
            ret = !body.contains("<img src");
            if (ret) {
                body = body.replaceAll("<.*?>", "");
                ret = body.trim().isEmpty();
            }
        } catch (Exception e) {
            // questionable use of try catch... 
            // IOHandler.writeErrorLog(e);
            ret = true;
        }
        
        return ret;
    }

    /**
     * sets text to default value
     */
    public void setDefault() {
        setText(defText);
    }

    private void setupListeners() {
        // if blank, this field is being used for something more complex: no listeners
        if (!defText.isEmpty()) {
            FocusListener listener = new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (isDefaultText()) {
                        setText("");
                    }
                    
                    setForeground(Color.BLACK);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (isEmpty() && !defText.isEmpty()) {
                        setText(defText);
                        setForeground(Color.lightGray);
                    }
                }
            };
            
            this.addFocusListener(listener);
        }
        
        // add a listener for character replacement if conlang font not overridden
        if (!overrideFont) {
            this.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    Character c = e.getKeyChar();
                    String repString = core.getPropertiesManager().getCharacterReplacement(c.toString());
                    if (!repString.isEmpty()) {
                        try {
                            e.consume();
                            ClipboardHandler cb = new ClipboardHandler();
                            cb.cacheClipboard();
                            cb.setClipboardContents(repString);
                            paste();
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

    /**
     * Gets raw html content of panel without any postprocessing
     *
     * @return
     */
    public String getRawHTML() {
        return super.getText();
    }

    /**
     * Gets plain text that appears in body (but not HTML) and nothing else
     *
     * @return
     */
    public String getNakedText() {
        var ret = WebInterface.getTextFromHtml(getSuperText()).trim();
  
        if (ret.equals(defText)) {
            ret = "";
        }
        
        return ret;
    }

    @Override
    public String getText() {
        final String bodyS = "<body>";
        final String bodyE = "</body>";        
        String ret = super.getText().replaceAll(PGTUtil.RTL_CHARACTER, "").replaceAll(PGTUtil.LTR_MARKER, "");

        // special logic needed if this is written in the conlang's font and RTL is enforced.
        if (isDefaultText()) {
            ret = "";
        } else if (core.getPropertiesManager().isEnforceRTL() && !overrideFont
                && ret.contains(bodyS) && ret.contains(bodyE)) {
            String body = ret.substring(0, ret.indexOf(bodyE));
            body = body.substring(body.lastIndexOf(bodyS) + bodyS.length());
            String start = ret.substring(0, ret.indexOf(bodyS) + bodyS.length());
            String end = ret.substring(ret.indexOf(bodyE));
            ret = start + PGTUtil.RTL_CHARACTER + body.trim() + end;
        }

        return FormattedTextHelper.HTMLLineBreakParse(ret);
    }

    /**
     * Allows super method to be called in listeners
     *
     * @return
     */
    private String getSuperText() {
        return super.getText().replaceAll(PGTUtil.RTL_CHARACTER, "").replaceAll(PGTUtil.LTR_MARKER, "");
    }

    @Override
    public void updateFromCore() {
        setFontFromCore();
    }
}
