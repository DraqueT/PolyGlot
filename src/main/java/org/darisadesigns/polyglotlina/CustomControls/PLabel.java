/*
 * Copyright (c) 2016-2019, Draque Thompson
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

import org.darisadesigns.polyglotlina.PGTUtil;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JLabel;

/**
 * Extends Labels with some useful functionality. If resize set to true, ensures
 * that font will grow to fill label container
 *
 * @author draque.thompson
 */
public final class PLabel extends JLabel {
    private Graphics g;
    private boolean resize = false;

    public PLabel(String text, double fontSize) {
        super(text);
        setFont(PGTUtil.MENU_FONT.deriveFont((float)fontSize));
        init();
    }
    
    public PLabel(String text, int alignment, double fontSize) {
        super(text, alignment);
        setFont(PGTUtil.MENU_FONT.deriveFont((float)fontSize));
        init();
    }

    private void init() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adaptLabelFont(PLabel.this);
            }
        });
    }

    public void adaptLabelFont(JLabel l) {
        if (g == null || !resize) {
            return;
        }
        Rectangle r = l.getBounds();
        int fontSize = PGTUtil.PLABEL_MIN_FONT_SIZE;
        Font f = l.getFont();

        Rectangle r1 = new Rectangle();
        Rectangle r2 = new Rectangle();
        while (fontSize < PGTUtil.PLABEL_MAX_FONT_SIZE) {
            r1.setSize(getTextSize(l, f.deriveFont(f.getStyle(), fontSize)));
            r2.setSize(getTextSize(l, f.deriveFont(f.getStyle(), fontSize + 2)));
            if (r.contains(r1) && !r.contains(r2)) {
                break;
            }
            fontSize++;
        }

        setFont(f.deriveFont(f.getStyle(), fontSize));
        repaint();
    }

    private Dimension getTextSize(JLabel l, Font f) {
        Dimension size = new Dimension();
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics(f);
        size.width = fm.stringWidth(l.getText());
        size.height = fm.getHeight();

        return size;
    }

    @Override
    protected void paintComponent(Graphics _g) {
        // turn on anti-alias mode
        Graphics2D antiAlias = (Graphics2D) _g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(antiAlias);
        this.g = antiAlias;
    }

    public void setResize(boolean _resize) {
        resize = _resize;
    }
}
