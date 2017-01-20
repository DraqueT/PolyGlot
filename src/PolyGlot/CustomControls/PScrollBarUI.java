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

import PolyGlot.PGTUtil;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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
    private PArrowButton increase;
    private PArrowButton decrease;
    private boolean vertical;
    
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
        
        decrease = new PArrowButton(orientation);
        return decrease;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        increase = new PArrowButton(orientation);
        return increase;
    }
    
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Graphics2D antiAlias = (Graphics2D) g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Image slideTop;
        Image slideCenter;
        Image slideBottom;

        g.translate(trackBounds.x, trackBounds.y);
        
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            slideTop = PGTUtil.sliderTop;
            slideCenter = PGTUtil.sliderMiddleVert;
            slideBottom = PGTUtil.sliderBottom;
            
            int increaseHeight = increase.getHeight() - 4; // why -4 to make this look right? Weird.
            int decreaseHeight = decrease.getHeight() - 4;
            int slideTopHeight = slideTop.getHeight(scrollbar);
            int slideBotHeight = slideBottom.getHeight(scrollbar);            
            
            antiAlias.drawImage(slideTop, 0, increaseHeight, trackBounds.width, slideTopHeight, scrollbar);
            antiAlias.drawImage(slideCenter, 0, increaseHeight + slideTopHeight, trackBounds.width, 
                    trackBounds.height - (increaseHeight + slideTopHeight + slideBotHeight), scrollbar);
            antiAlias.drawImage(slideBottom, 0, trackBounds.height - (decreaseHeight + slideBotHeight), 
                    trackBounds.width, slideBotHeight, scrollbar);
        } else {
            try {
                slideTop = PGTUtil.sliderEast;
                slideCenter = PGTUtil.sliderMiddleHoriz;
                slideBottom = PGTUtil.sliderWest;
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
                return;
            }
            int increaseWidth = increase.getWidth() - 20; // Why 20 here??? Won't render in the right spot otherwise...
            int decreaseWidth = decrease.getWidth() - 20;
            int slideTopWidth = slideTop.getWidth(scrollbar);
            int slideBotWidth = slideBottom.getWidth(scrollbar);
            
            antiAlias.drawImage(slideBottom, decreaseWidth, 0, slideBotWidth, trackBounds.height, scrollbar);
            antiAlias.drawImage(slideCenter, increaseWidth + slideTopWidth, 0, 
                    trackBounds.width - (increaseWidth + slideTopWidth + slideBotWidth), trackBounds.height, scrollbar);
            antiAlias.drawImage(slideTop, trackBounds.width - (slideTopWidth + increaseWidth), 0, 
                    slideTopWidth, trackBounds.height, scrollbar);
        }
        
        g.translate( -trackBounds.x, -trackBounds.y );
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D antiAlias = (Graphics2D) g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(thumbBounds.x, thumbBounds.y);
        
        // TODO: Obviously rework the thumb section with custom graphic
        g.setColor( Color.red );
        if (vertical) {            
            g.fillRect( 4, 9, thumbBounds.width - 8, thumbBounds.height - 18 );
        } else {
            g.fillRect( 9, 4, thumbBounds.width - 18, thumbBounds.height - 8 );
        }
        g.translate( -thumbBounds.x, -thumbBounds.y );
    }
}
