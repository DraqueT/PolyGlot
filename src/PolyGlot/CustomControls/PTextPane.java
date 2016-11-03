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
import PolyGlot.ExternalCode.TextTransfer;
import PolyGlot.Nodes.ImageNode;
import PolyGlot.PGTUtil;
import PolyGlot.PGTools;
import PolyGlot.WebInterface;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

/**
 *
 * @author draque
 */
public class PTextPane extends JTextPane {

    private SwingWorker worker = null;
    private final String defText;
    private final DictCore core;
    private final boolean overrideFont;

    public PTextPane(DictCore _core, boolean _overideFont, String _defText) {
        core = _core;
        defText = _defText;
        overrideFont = _overideFont;
        this.setContentType("text/html");
        setupRightClickMenu();

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
        
        if (ret != null) {
            ret += " (right click to insert images)";
        }
        
        return ret;
    }

    private void setupRightClickMenu() {
        final JPopupMenu ruleMenu = new JPopupMenu();
        final JMenuItem insertImage = new JMenuItem("Insert Image");
        final PTextPane parentPane = this;

        insertImage.setToolTipText("Insert Image into Text");
        insertImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    addImage(core.getImageCollection()
                            .openNewImage((Window)parentPane.getTopLevelAncestor()));
                } catch (Exception e) {
                    InfoBox.error("Image Import Error", "Unable to import image: " 
                            + e.getLocalizedMessage(), null);
                }
            }
        });

        ruleMenu.add(insertImage);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    insertImage.setEnabled(true);
                    ruleMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    insertImage.setEnabled(true);
                    ruleMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void addImage(ImageNode image) throws Exception {
        final String placeHold = "-POLYGLOTIMAGE-";
        
        if (isDefaultText()) {
            super.setText("");
        }
        
        TextTransfer test = new TextTransfer();

        test.cacheClipboard();
        test.setClipboardContents(placeHold);

        paste();
        String newText = getRawHTML();
        setText(newText.replace(placeHold, "<img src=\"file:///" + image.getImagePath() + "\">"));
        test.restoreClipboard();
    }

    /**
     * Tests whether the current text value is the default value
     *
     * @return
     */
    public boolean isDefaultText() {
        // account for RtL languages
        String curText = getNakedText().replaceAll(PGTUtil.RTLMarker, "").replaceAll(PGTUtil.LTRMarker, "");
        return curText.contains(defText);
    }

    /**
     * sets text to default value
     */
    public void setDefault() {
        setText(defText);
    }

    private void setupListeners() {
        FocusListener listener = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getNakedText().equals(defText)) {
                    setText("");
                    setForeground(Color.black);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getNakedText().equals("")) {
                    setText(defText);
                    setForeground(Color.lightGray);
                }
            }
        };

        this.addFocusListener(listener);
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
        /*String ret = getSuperText();
        if (getContentType().equals("text/html")) {
            int start = ret.indexOf("<body>") + 7;
            int end = ret.indexOf("</body>");
            ret = ret.substring(start, end);
        }
        return ret.trim();*/
        return WebInterface.getTextFromHtml(getSuperText()).trim();
    }

    @Override
    public String getText() {
        String ret = super.getText().replaceAll(PGTUtil.RTLMarker, "").replaceAll(PGTUtil.LTRMarker, "");

        if (ret.equals(defText)) {
            ret = "";
        } else {
            ret = core.getPropertiesManager().isEnforceRTL() ? PGTUtil.RTLMarker + ret : ret;
        }

        return ret;
    }

    /**
     * Allows super method to be called in listeners
     *
     * @return
     */
    private String getSuperText() {
        return super.getText().replaceAll(PGTUtil.RTLMarker, "").replaceAll(PGTUtil.LTRMarker, "");
    }
}
