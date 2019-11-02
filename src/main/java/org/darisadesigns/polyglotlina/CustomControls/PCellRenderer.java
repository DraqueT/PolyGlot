/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
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

package org.darisadesigns.polyglotlina.CustomControls;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
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

/**
 *
 * @author draque
 */
public class PCellRenderer implements TableCellRenderer {
    private final Font myFont;
    private Color background = Color.white;
    private DocumentListener docListener;
    private final DictCore core;
    private final boolean useConFont;
    private final double fontSize;

    public PCellRenderer(boolean _useConFont, DictCore _core) {
        core = _core; 
        useConFont = _useConFont;
        Integer preSize = core.getPropertiesManager().getFontSize();
        preSize = preSize == null ? 0 : preSize;
        
        Font selectedFont = useConFont ? 
                core.getPropertiesManager().getFontCon() : 
                core.getPropertiesManager().getFontLocal();
        fontSize = useConFont ? 
                preSize :
                core.getOptionsManager().getMenuFontSize();
        
        myFont = PGTUtil.addFontAttribute(TextAttribute.SIZE, (float)fontSize, selectedFont);
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
            editor.setFont(myFont.deriveFont((float)fontSize));
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
                if (useConFont) {
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
}
