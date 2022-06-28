/*
 * Copyright (c) 2021-2022, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import javax.swing.JPopupMenu;

/**
 * A version of PButton which supports dropdown functionality
 * @author draque
 */
public class PButtonDropdown extends PButton {
    private GeneralPath arrow;
    private final JPopupMenu popupMenu;
    boolean firstTime = true;
    int dropDownPosition;
    int curMouseRelativeX;
    
    public PButtonDropdown(JPopupMenu _popupMenu) {
        super(false);
        
        setupMousePositionListener();
        popupMenu = _popupMenu;
    }
    
    private void setupMousePositionListener() {
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // do nothing
            }

            @Override
            public void mousePressed(MouseEvent e) {
                curMouseRelativeX = e.getX();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // do nothing
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // do nothing
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // do nothing
            }
        });
    }
    
    /**
     * Wraps action listener to display popup menu when appropriate
     * @param listener 
     */
    @Override
    public void addActionListener(ActionListener listener) {
        final var self = this;
        super.addActionListener((ActionEvent e) -> {
            if (curMouseRelativeX > dropDownPosition) {
                popupMenu.show(PButtonDropdown.this, 0, getHeight()); // force width to render
                var position = self.getWidth() - popupMenu.getWidth();
                popupMenu.show(PButtonDropdown.this, position, getHeight());
            } else {
                listener.actionPerformed(e);
            }
        });
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        Font font = this.getFont();
        g2.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D r = font.getStringBounds(this.getText(), frc);
        
        float sx = 12f;
        float xOffset = (float)((r.getWidth() + this.getWidth()) / 2) + sx;
        dropDownPosition = (int)xOffset - 5;
        
        if(firstTime) {
            createArrow(xOffset - 3, getHeight());
            firstTime = false;
        }
        
        g2.setColor(Color.black);
        g2.fill(arrow);
        
        g2.setColor(Color.gray);
        g2.drawLine(dropDownPosition, 5, dropDownPosition, getHeight() - 10);
    }
    
    @Override
    public void setText(String text) {
        super.setText(text);
        firstTime = true;
    }
    
    private void createArrow(float x, int h) {
        arrow = new GeneralPath();
        arrow.moveTo(x, h/3f);
        arrow.lineTo(x + 10f, h/3f);
        arrow.lineTo(x + 5f, h*2/3f);
        arrow.closePath();
    }
}
