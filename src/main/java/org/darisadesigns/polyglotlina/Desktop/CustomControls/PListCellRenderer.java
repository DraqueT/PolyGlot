/*
 * Copyright (c) 2021-2023, Draque Thompson, draquemail@gmail.com
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
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.LexiconProblemNode;
import org.darisadesigns.polyglotlina.Screens.ScrPrintToPDF.PrintOrderNode;

/**
 *
 * @author draque
 */
public class PListCellRenderer extends DefaultListCellRenderer {
    private Font localFont;
    private FontMetrics localMetrics;
    private FontMetrics conMetrics;
    private boolean addLocalExtraText = false;
    private Object curVal = null;
    private int wordEnd;
    private int dropPosition;
    private int height;
    private String printValue = "";
    private final DictCore core;
    
    public PListCellRenderer(DictCore _core) {
        super();
        core = _core;
    }
    
    @Override
    public String getToolTipText() {
        String tip = super.getToolTipText();
        
        if (curVal instanceof ConWord conWord) {
            try {
                tip = conWord.getWordSummaryValue(true);
            } catch (Exception e) {
                // user is informed of this elsewhere. Simply default to stringified value
                tip = conWord.toString();
            }
        } else if (curVal != null) {
            tip = curVal.toString();
        }
        
        return tip;
    }
    
    @Override
    public void paint(Graphics g) {
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (curVal instanceof PrintOrderNode orderNode) {
            if (orderNode.isSelected()) {
                this.setForeground(Color.black);
            } else {
                this.setForeground(Color.gray);
            }
        } else if (curVal instanceof LexiconProblemNode problemNode) {
            if (!problemNode.useConFont) {
                this.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal());
            }
            
            switch (problemNode.severity) {
                case LexiconProblemNode.SEVARITY_INFO -> {
                    this.setForeground(Color.black);
                }
                case LexiconProblemNode.SEVARITY_WARNING -> {
                    this.setForeground(Color.orange);
                }
                case LexiconProblemNode.SEVARITY_ERROR -> {
                    this.setForeground(Color.red);
                }
            }
        }
        
        super.paint(g);
        
        // prints expanded word display if set in properties
        if (this.addLocalExtraText && curVal instanceof ConWord word) {
            var newPrintValue = word.getLocalWord();
            
            if (!newPrintValue.isBlank()) {
                if (conMetrics == null) {
                    setupFontMetrics(g);
                }
                
                if (!printValue.equals(newPrintValue)) {
                    printValue = newPrintValue;
                    wordEnd = conMetrics.stringWidth(word.getValue());
                    dropPosition = (localMetrics.getHeight() * 6) / 7;
                    height = conMetrics.getHeight();
                }
                
                g.setFont(localFont);
                g.setColor(Color.blue);
                g.drawLine(wordEnd + 10, 5, wordEnd + 10, height);
                g.setColor(Color.darkGray);
                g.drawString(printValue, wordEnd + 15, dropPosition);
            }
        }
    }
    
    private void setupFontMetrics(Graphics g) {
        Font conFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon();
        conMetrics = g.getFontMetrics(conFont);
        localFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
        localMetrics = g.getFontMetrics(localFont);
    }

    public boolean isAddLocalExtraText() {
        return addLocalExtraText;
    }

    public void setAddLocalExtraText(boolean addLocalExtraText) {
        this.addLocalExtraText = addLocalExtraText;
    }
    
    @Override
    public Component getListCellRendererComponent(
        JList<?> list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus)
    {
        curVal = value;
        
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
