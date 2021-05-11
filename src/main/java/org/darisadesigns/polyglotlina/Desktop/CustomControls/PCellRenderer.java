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

package org.darisadesigns.polyglotlina.Desktop.CustomControls;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;

/**
 *
 * @author draque
 */
public final class PCellRenderer implements TableCellRenderer {

    private Font myFont;
    private Color background = Color.white;
    private DocumentListener docListener;
    private final DictCore core;
    private boolean useConFont;
    private double fontSize;

    public PCellRenderer(boolean _useConFont, DictCore _core) {
        core = _core;
        setUseConFont(_useConFont);
    }
    
    public void setBackground(Color _background) {
        background = _background;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final JTextField editor = new JTextField();
        
        editor.setBackground(background);
        
        if (value != null) {
            editor.setText(value.toString());
        }
        
        if (myFont != null) {
            editor.setFont(myFont.deriveFont((float)getFontSize()));
        }
        
        if (table.isEnabled()) {
            editor.setForeground(Color.black);
        } else {
            editor.setForeground(Color.gray);
        }
        
        editor.getDocument().addDocumentListener(docListener);

        if (isSelected) {
            editor.setBorder(BorderFactory.createBevelBorder(1));
        } else {
            editor.setBorder(BorderFactory.createEtchedBorder());
        }
        
        editor.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // only use replacement if writing in confont
                if (isUseConFont()) {
                    PTextField.handleCharacterReplacement(core, e, editor);
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
        
        return editor;
    }
      
    public void setDocuListener(DocumentListener _listener) {
        docListener = _listener;
    }
    
    public boolean isUseConFont() {
        return useConFont;
    }

    public void setUseConFont(boolean _useConFont) {
        this.useConFont = _useConFont;
        double preSize = core.getPropertiesManager().getFontSize();
        
        Font selectedFont = useConFont ? 
                ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon() : 
                ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
        fontSize = useConFont ? 
                preSize :
                PolyGlot.getPolyGlot().getOptionsManager().getMenuFontSize();
        
        myFont = PGTUtil.addFontAttribute(TextAttribute.SIZE, (float)fontSize, selectedFont);
    }

    public double getFontSize() {
        return fontSize;
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }
}
