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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * Custom implementation of split pane divider for PolyGlot
 * @author draque.thompson
 */
public class PSplitPaneDivider extends BasicSplitPaneDivider {  
    public PSplitPaneDivider(BasicSplitPaneUI ui) {
        super(ui);
    }
    
    @Override
    public void paint(Graphics g) {
        final int rounding = 5;
        final int barWidth = 4;
        final int thisWidth = this.getWidth();
        final int thisHeight = this.getHeight();
        boolean isVertical = thisHeight > thisWidth;
        final int ballGap = 16;
        final int ball = 4;
        
        Graphics2D antiAlias = (Graphics2D) g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (isVertical) {
            g.setColor(Color.lightGray);
            g.fillRoundRect((thisWidth - barWidth)/2, ballGap, barWidth, ((thisHeight - 2 * ballGap) / 2) - ballGap, rounding, rounding);
            g.fillRoundRect((thisWidth - barWidth)/2, ((thisHeight - 2 * ballGap) / 2) + ballGap, barWidth, ((thisHeight - 2 * ballGap) / 2) - ballGap, rounding, rounding);
            g.setColor(Color.darkGray);
            g.drawOval((thisWidth - ball)/2, (thisHeight / 2) - 11, ball, ball);
        } else {
            // TODO: horizontal ones
            g.setColor(Color.red);        
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }
}
