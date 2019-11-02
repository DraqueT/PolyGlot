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

import org.darisadesigns.polyglotlina.PGTUtil;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Custom scrollbar UI for PolyGlot
 * @author draque.thompson
 */
public class PScrollBarUI extends BasicScrollBarUI {
    private boolean vertical;
    private final Color shadow = PGTUtil.COLOR_ENABLED_BG;
    private final Color darkShadow = Color.decode("303030");
    
    public static ComponentUI createUI(JComponent c) {
        return new PScrollBarUI(c);
    }
    
    public PScrollBarUI(JComponent c) {
        scrollbar = (JScrollBar)c;
    }
    
    @Override
    protected JButton createDecreaseButton(int orientation) {
        vertical = (orientation == SwingConstants.NORTH 
                || orientation == SwingConstants.SOUTH);

        return new PArrowButton(orientation);
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return new PArrowButton(orientation);
    }
    
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Graphics2D antiAlias = (Graphics2D) g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(trackBounds.x, trackBounds.y);
        
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {           
            g.setColor(darkShadow);
            g.fillRect(0, 0, trackBounds.width, trackBounds.height);
            g.setColor(shadow);
            g.fillRect(1, 1, trackBounds.width - 2, trackBounds.height - 2);
        } else {
            g.setColor(darkShadow);
            g.fillRect(0, 0, trackBounds.width, trackBounds.height);
            g.setColor(shadow);
            g.fillRect(1, 1, trackBounds.width - 2, trackBounds.height - 2);
        }
        
        g.translate( -trackBounds.x, -trackBounds.y );
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D antiAlias = (Graphics2D) g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(thumbBounds.x, thumbBounds.y);
        
        
        if (vertical) {
            g.setColor(darkShadow);
            g.fillRect(2, 9, thumbBounds.width - 4, thumbBounds.height - 18);
            g.setColor(Color.gray);
            g.fillRect(3, 10, thumbBounds.width - 6, thumbBounds.height - 20);
            
            g.setColor(darkShadow);
            g.drawLine(4, thumbBounds.height/2, thumbBounds.width -5, thumbBounds.height/2);
            g.drawLine(5, thumbBounds.height/2 + 2, thumbBounds.width - 6, thumbBounds.height/2 + 2);
            g.drawLine(5, thumbBounds.height/2 - 2, thumbBounds.width - 6, thumbBounds.height/2 - 2);
        } else {
            g.setColor(darkShadow);            
            g.fillRect(9, 2, thumbBounds.width - 18, thumbBounds.height - 4);
            g.setColor(Color.gray);
            g.fillRect(10, 3, thumbBounds.width - 20, thumbBounds.height - 6);
            
            g.setColor(darkShadow);
            g.drawLine(thumbBounds.width/2, 4, thumbBounds.width/2, thumbBounds.height -5);
            g.drawLine(thumbBounds.width/2 + 2, 5, thumbBounds.width/2 + 2, thumbBounds.height - 6);
            g.drawLine(thumbBounds.width/2 - 2, 5, thumbBounds.width/2 - 2, thumbBounds.height - 6);
        }
        g.translate( -thumbBounds.x, -thumbBounds.y );
    }
}
