/*
 * Copyright (c) 2017-2019, Draque Thompson
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
package org.darisadesigns.polyglotlina.CustomControls;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import javax.swing.JCheckBox;

/**
 * Custom PolyGlot implementation of checkbox
 * @author draque.thompson
 */
public class PCheckBox extends JCheckBox implements MouseListener {
    private boolean mouseOver = false;
    private boolean clicked = false;
    private final DictCore core;
    
    public PCheckBox(DictCore _core) {
        core = _core == null ? new DictCore() : _core;
        
        float fontSize = (float)core.getOptionsManager().getMenuFontSize();
        super.setFont(PGTUtil.MENU_FONT.deriveFont(fontSize));
        
        setupListeners();
    }
    
    private void setupListeners() {
        this.addMouseListener(this);
    }
    
    @Override
    public void paint(Graphics g) {
        boolean enabled = this.isEnabled();
        Color selected = core.getVisualStyleManager().getCheckBoxSelected(enabled);
        Color backGround = core.getVisualStyleManager().getCheckBoxBG(enabled);
        Color outline = core.getVisualStyleManager().getCheckBoxOutline(enabled);
        Color hover = core.getVisualStyleManager().getCheckBoxHover(enabled);
        Color click = core.getVisualStyleManager().getCheckBoxClicked(enabled);
        Color fieldBack = core.getVisualStyleManager().getCheckBoxFieldBack(enabled);
        int rounding = PGTUtil.CHECKBOX_ROUNDING;
        int thisHeight = this.getHeight();
        
        if (this.hasFocus()) {
            g.setColor(selected);
            g.drawRect(0, 0, this.getWidth() - 1, thisHeight - 1);
        }
        
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (enabled) {
            if (mouseOver) {
                g.setColor(hover);
                g.drawRoundRect(3, 3, thisHeight - 6, thisHeight - 6, rounding, rounding);
            }
            if (clicked) {
                backGround = click;
            }            
        }
        
        g.setColor(outline);
        g.drawRoundRect(4, 4, thisHeight - 8, thisHeight - 8, rounding, rounding);
        
        g.setColor(backGround);
        g.drawRoundRect(5, 5, thisHeight - 10, thisHeight - 10, rounding, rounding);
        
        g.setColor(fieldBack);
        g.fillRect(6, 6, thisHeight - 11, thisHeight - 11);
        
        g.setColor(selected);
        
        if (this.isSelected()) {
            g.fillRect(7, 7, thisHeight - 14, thisHeight - 14);
        }
        
        g.setColor(outline);
        char[] text = getText().toCharArray();
        FontMetrics fm = g.getFontMetrics(getFont());
        Rectangle2D rec = fm.getStringBounds(getText(), g);
        int stringH = (int) Math.round(rec.getHeight());
        g.drawChars(text, 0, text.length, thisHeight, thisHeight/2 + stringH/3);
    }

    
    @Override
    public void mousePressed(MouseEvent e) {
        clicked = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        clicked = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseOver = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseOver = false;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        // no action
    }
}
