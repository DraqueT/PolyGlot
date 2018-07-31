/*
 * Copyright (c) 2014-2017, Draque Thompson, draquemail@gmail.com
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

package PolyGlot.CustomControls;

import PolyGlot.DictCore;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.util.Map;
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
    private DocumentListener docListener;
    private final DictCore core;
    private final boolean useConFont;
    private final double fontSize;

    public PCellRenderer(boolean _useConFont, DictCore _core) {
        core = _core; 
        useConFont = _useConFont;
        Double kernVal = useConFont ? 
                core.getPropertiesManager().getKerningSpace() : 0.0;
        Font selectedFont = useConFont ? 
                core.getPropertiesManager().getFontCon() : 
                core.getPropertiesManager().getFontLocal();
        fontSize = useConFont ? 
                core.getPropertiesManager().getFontSize() :
                core.getOptionsManager().getMenuFontSize();
        
        Map attr = selectedFont.getAttributes();
        
        attr.put(TextAttribute.TRACKING, kernVal);
        attr.put(TextAttribute.SIZE, (float) fontSize);
        myFont = selectedFont.deriveFont(attr);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final JTextField editor = new JTextField();
        
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
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();        
    }
      
    public void setDocuListener(DocumentListener _listener) {
        docListener = _listener;
    }
}
