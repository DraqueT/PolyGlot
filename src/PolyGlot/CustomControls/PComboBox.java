/*
 * Copyright (c) 2016 - 2017, draque
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
import PolyGlot.PGTUtil;
import PolyGlot.PGTools;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;

/**
 *
 * @author draque
 */
public class PComboBox extends JComboBox implements MouseListener {
    private SwingWorker worker = null;
    private boolean mouseOver = false;
    private final DictCore core;
    public PComboBox(DictCore _core) {
        core = _core;
        setupListeners();
        
        // default font to Charis
        super.setFont(core.getPropertiesManager().getCharisUnicodeFont());
    }

    /**
     * makes this component flash. If already flashing, does nothing.
     * @param _flashColor color to flash
     * @param isBack whether display color is background (rather than foreground)
     */
    public void makeFlash(Color _flashColor, boolean isBack) {
        if (worker == null || worker.isDone()) {
            worker = PGTools.getFlashWorker(this, _flashColor, isBack);
            worker.execute();
        }
    }
    
    private void setupListeners() {
        addMouseListener(this);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        final int buttonWidth = 20;
        boolean enabled = this.isEnabled();
        
        // turn on anti-alias mode
        Graphics2D antiAlias = (Graphics2D) g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
        if (enabled) {
            antiAlias.setColor(Color.white);
        } else {
            antiAlias.setColor(Color.decode("#e0e0e4"));
        }
        
        antiAlias.fillRoundRect(1, 1, getWidth(), getHeight() - 2, 5, 5);
        
        if (enabled) {
            antiAlias.setColor(PGTUtil.colorEnabledBG);
        } else {
            antiAlias.setColor(Color.decode("#d0d0d0"));
        }
        antiAlias.fillRect(getWidth() - buttonWidth, 1, buttonWidth, getHeight() - 1);
        
        if ((mouseOver || this.hasFocus()) && enabled) {
            antiAlias.setColor(Color.black);
        } else 
        {
            antiAlias.setColor(Color.lightGray);
        }
        antiAlias.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 5, 5);
        
        if (enabled) {
            antiAlias.setColor(Color.black);
        } else {
            antiAlias.setColor(Color.decode("#909090"));
        }
        
        String text = getSelectedItem().toString();
        FontMetrics fm = antiAlias.getFontMetrics(getFont());
        Rectangle2D rec = fm.getStringBounds(text, antiAlias);
        int stringW = (int) Math.round(rec.getWidth());
        int stringH = (int) Math.round(rec.getHeight());
        antiAlias.drawChars(text.toCharArray(), 0, text.length(), ((getWidth() - buttonWidth)/2) 
                - (stringW/2), (getHeight() - 9)/2 + stringH/2);
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
        // do nothing
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // do nothing
    }
}
