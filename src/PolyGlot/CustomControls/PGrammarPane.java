/*
 * Copyright (c) 2016=2017, draque.thompson
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
import PolyGlot.ExternalCode.GlyphVectorEditorKit;
import PolyGlot.Nodes.ImageNode;
import PolyGlot.PGTUtil;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Similar to the PTextPane, but compatible with the HTML stylings performed in
 * the grammar window
 *
 * @author draque.thompson
 */
public class PGrammarPane extends JTextPane {

    private final DictCore core;

    public PGrammarPane(DictCore _core) {
        core = _core;
        setupRightClickMenu();
        setEditorKit(new GlyphVectorEditorKit());
    }

    private void setupRightClickMenu() {
        final JPopupMenu ruleMenu = new JPopupMenu();
        final JMenuItem insertImage = new JMenuItem("Insert Image");
        final JMenuItem cut = new JMenuItem("Cut");
        final JMenuItem copy = new JMenuItem("Copy");
        final JMenuItem paste = new JMenuItem("Paste");
        final PGrammarPane parentPane = this;

        insertImage.setToolTipText("Insert Image into Text");
        insertImage.addActionListener((ActionEvent ae) -> {
            try {
                ImageNode image = core.getImageCollection()
                        .openNewImage((Window) parentPane.getTopLevelAncestor());
                if (image != null) {
                    // null node means user cancelled process
                    addImage(image);
                }
            } catch (Exception e) {
                InfoBox.error("Image Import Error", "Unable to import image: "
                        + e.getLocalizedMessage(), core.getRootWindow());
            }
        });
        cut.addActionListener((ActionEvent ae) -> {
            cut();
        });
        copy.addActionListener((ActionEvent ae) -> {
            copy();
        });
        paste.addActionListener((ActionEvent ae) -> {
            paste();
        });

        ruleMenu.add(insertImage);
        ruleMenu.add(cut);
        ruleMenu.add(copy);
        ruleMenu.add(paste);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && parentPane.isEnabled()) {
                    insertImage.setEnabled(true);
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

    public void addImage(ImageNode image) {
        try {
            MutableAttributeSet inputAttributes = getInputAttributes();
            inputAttributes.removeAttributes(inputAttributes);
            StyleConstants.setIcon(inputAttributes, new ImageIcon(image.getImagePath()));
            inputAttributes.addAttribute(PGTUtil.ImageIdAttribute, image.getId());
            replaceSelection(" ", false);
            inputAttributes.removeAttributes(inputAttributes);
        } catch (IOException e) {
            InfoBox.error("Image Insertion Error", "Unable to insert image: "
                    + e.getLocalizedMessage(), core.getRootWindow());
        }
    }

    /**
     * Captures images in override, passes everything else on to super.
     */
    @Override
    public void paste() {
        // might handle more types in the future
        if (ClipboardHandler.isClipboardImage()) {
            try {
                BufferedImage image = (BufferedImage) ClipboardHandler.getClipboardImage();
                ImageNode imageNode = core.getImageCollection().getFromBufferedImage(image);
                addImage(imageNode);
            } catch (Exception e) {
                InfoBox.error("Paste Error", "Unable to paste: " + e.getLocalizedMessage(), core.getRootWindow());
            }
        } else if (ClipboardHandler.isClipboardString()) {
            super.paste();
        } else {
            super.paste();
        }
    }

    private void replaceSelection(String content, boolean checkEditable) {
        if (checkEditable && !isEditable()) {
            UIManager.getLookAndFeel().provideErrorFeedback(PGrammarPane.this);
            return;
        }
        Document doc = getStyledDocument();
        if (doc != null) {
            try {
                Caret caret = getCaret();
                boolean composedTextSaved = saveComposedText(caret.getDot());
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                AttributeSet attr = getInputAttributes().copyAttributes();
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument) doc).replace(p0, p1 - p0, content, attr);
                } else {
                    if (p0 != p1) {
                        doc.remove(p0, p1 - p0);
                    }
                    if (content != null && content.length() > 0) {
                        doc.insertString(p0, content, attr);
                    }
                }
                if (composedTextSaved) {
                    restoreComposedText();
                }
            } catch (BadLocationException e) {
                UIManager.getLookAndFeel().provideErrorFeedback(PGrammarPane.this);
            }
        }
    }
}
