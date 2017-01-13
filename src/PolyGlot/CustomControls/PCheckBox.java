/*
 * Copyright (c) 2017, draque.thompson
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
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import javax.swing.JCheckBox;

/**
 * Custom PolyGlot implementation of checkbox
 * @author draque.thompson
 */
public class PCheckBox extends JCheckBox implements MouseListener {
    boolean mouseOver = false;
    boolean clicked = false;
    DictCore core;
    
    public PCheckBox(DictCore _core) {
        core = _core;
        
        // default to unicode compatible font
        super.setFont(core.getPropertiesManager().getCharisUnicodeFont());
        setupListeners();
    }
    
    private void setupListeners() {
        this.addMouseListener(this);
    }
    
    // TODO: this whole thing, obviously
    @Override
    public void paint(Graphics g) {
        if (this.isSelected()) {
            g.setColor(Color.red);
        } else {
            g.setColor(Color.blue);
        }        
        g.fillRect(4, 4, getHeight() - 8, getHeight() - 8);
        
        g.setColor(Color.black);
        
        char[] text = getText().toCharArray();
        FontMetrics fm = g.getFontMetrics(getFont());
        Rectangle2D rec = fm.getStringBounds(getText(), g);
        int stringH = (int) Math.round(rec.getHeight());
        g.drawChars(text, 0, text.length, getHeight(), getHeight()/2 + stringH/3);
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
