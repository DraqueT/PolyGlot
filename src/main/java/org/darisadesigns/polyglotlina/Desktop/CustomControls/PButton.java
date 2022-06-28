/*
 * Copyright (c) 2017 - 2022, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 *
 * @author draque.thompson
 */
public class PButton extends JButton implements MouseListener {
    private boolean mouseEntered = false;
    private boolean mousePressed = false;
    private boolean activeSelected = false;
    private FontMetrics fontMetrics = null;
    Rectangle2D textRectangle;
    int stringW;
    int stringH;
    
    public PButton() {
        super();
        setupListeners();
    }
    
    public PButton(boolean nightModee) { // nightmode included for future progress
        super.setFont(PGTUtil.MENU_FONT.deriveFont(PGTUtil.DEFAULT_FONT_SIZE.floatValue()));        
        setupListeners();
    }
    
    /**
     * Sets whether button is actively selected (pressed down persistently)
     * @param _activeSelected 
     */
    public void setActiveSelected(boolean _activeSelected) {
        activeSelected = _activeSelected;
    }
    
    /**
     * Returns whether button is actively selected (pressed down persistently)
     * @return 
     */
    public boolean isActiveSelected() {
        return activeSelected;
    }
    
    private void setupListeners() {
        addMouseListener(this);
    }
    
    @Override
    public synchronized final void addMouseListener(MouseListener l) {
        super.addMouseListener(l);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        boolean enabled = isEnabled();
        Color bgColor;
        Color fontColor;
        
        // turn on anti-alias mode
        Graphics2D antiAlias = (Graphics2D) g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int thisHeight = this.getHeight();
        final int thisWidth = this.getWidth();
        
        if (!enabled) {
            bgColor = PGTUtil.COLOR_DISABLED_BG;
            fontColor = PGTUtil.COLOR_DISABLED_FOREGROUND;
        } else if (mousePressed && mouseEntered) {
            bgColor = PGTUtil.COLOR_SELECTED_BG;
            fontColor = getForeground();
        } else {
            bgColor = PGTUtil.COLOR_ENABLED_BG;
            fontColor = getForeground();
        }
        
        if (activeSelected) {
            g.setColor(Color.black);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        g.setColor(bgColor);
        
        g.fillRect(2, 2, thisWidth - 4, thisHeight - 4);

        // draw black border on mouseover if button enabled
        if (mouseEntered && enabled) {
            g.setColor(PGTUtil.COLOR_MOUSEOVER_BORDER);
            g.drawRect(1, 1, thisWidth - 3, thisHeight - 3);
        }
        
        g.setColor(fontColor);
        g.setFont(getFont());
        
        if (fontMetrics == null) {
            fontMetrics = g.getFontMetrics(getFont());
            textRectangle = fontMetrics.getStringBounds(getText(), g);
            stringW = (int) Math.round(textRectangle.getWidth());
            stringH = (int) Math.round(textRectangle.getHeight());
        }
        
        char[] text = getText().toCharArray();
        g.drawChars(text, 0, text.length, (thisWidth/2) - (stringW/2), thisHeight/2 + stringH/4);
        
        Icon icon = this.getIcon();
        if (icon != null) {
            icon.paintIcon(this, g, (thisWidth -icon.getIconWidth())/2, (thisHeight - icon.getIconHeight())/2);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // event will be applied by outside listener
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseEntered = true;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseEntered = false;
        repaint();
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        
        if (text.equals("↑") || text.equals("↓")) {
            setFont(PGTUtil.CHARIS_UNICODE.deriveFont(PGTUtil.DEFAULT_FONT_SIZE.floatValue()));
        }
        
        // force recalc of text size
        fontMetrics = null;
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font.deriveFont(PGTUtil.DEFAULT_FONT_SIZE.floatValue()));
        // force recalc of text size
        fontMetrics = null;
    }
}
