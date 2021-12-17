/*
 * Copyright (c) 2021, Draque Thompson, draquemail@gmail.com
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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Screens.ScrPrintToPDF.PrintOrderNode;

/**
 *
 * @author draque
 */
public class PListCellRenderer extends DefaultListCellRenderer {
    private boolean addLocalExtraText = false;
    Object curVal = null;
    
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
        if (curVal instanceof PrintOrderNode orderNode) {
            if (orderNode.isSelected()) {
                this.setForeground(Color.black);
            } else {
                this.setForeground(Color.gray);
            }
        }
        
        super.paint(g);
        
        // prints expanded word display if set in properties
        if (this.addLocalExtraText && curVal instanceof ConWord word) {
            DictCore core = PolyGlot.getPolyGlot().getCore();
            Font localFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
            Font conFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon();
            FontMetrics localMetrics = g.getFontMetrics(localFont);
            FontMetrics conMetrics = g.getFontMetrics(conFont);

            int wordEnd;
            int dropPosition;
            int height;
            String printValue;

            printValue = word.getLocalWord();
            
            if (!printValue.isBlank()) {
                wordEnd = conMetrics.stringWidth(word.getValue());
                dropPosition = (localMetrics.getHeight() * 6) / 7;
                height = conMetrics.getHeight();
                g.setFont(localFont);
                g.setColor(Color.blue);
                g.drawLine(wordEnd + 10, 5, wordEnd + 10, height);
                g.setColor(Color.darkGray);
                g.drawString(printValue, wordEnd + 15, dropPosition);
            }
        }
    }

    public boolean isAddLocalExtraText() {
        return addLocalExtraText;
    }

    public void setAddLocalExtraText(boolean addLocalExtraText) {
        this.addLocalExtraText = addLocalExtraText;
    }
}
