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

/**
 *
 * @author draque
 */
public class PListCellRenderer extends DefaultListCellRenderer {
    private boolean addLocalExtraText = false;
    Object curVal = null;
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String tip = "";
        curVal = value;
        
        if (value instanceof ConWord conWord) {
            try {
                tip = conWord.getWordSummaryValue(true);
            } catch (Exception e) {
                // user is informed of this elsewhere. Simply default to stringified value
                tip = conWord.toString();
            }
        } else if (value != null) {
            tip = value.toString();
        }
        
        setToolTipText(tip);
        
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        if (this.addLocalExtraText) {
            DictCore core = PolyGlot.getPolyGlot().getCore();
            
            if (curVal instanceof ConWord word) {
                Font localFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
                Font conFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon();
                FontMetrics localMetrics = g.getFontMetrics(localFont);
                FontMetrics conMetrics = g.getFontMetrics(conFont);

                int wordEnd;
                int dropPosition;
                int height;
                String printValue;

                printValue = word.getLocalWord();
                wordEnd = conMetrics.stringWidth(word.getValue());
                dropPosition = localMetrics.getHeight();
                height = conMetrics.getHeight();
                g.setFont(localFont);
                g.setColor(Color.blue);
                g.drawLine(wordEnd + 10, 0, wordEnd + 10, height);
                g.setColor(Color.black);
                g.drawString(printValue, wordEnd + 15, dropPosition);
            }
        }
    }
    
    //        // paint basic word definition next to word if that's what's in here
//        DictCore core = PolyGlot.getPolyGlot().getCore();
//        if (selectedItem instanceof ConWord word && core.getPropertiesManager().isExpandedLexListDisplay()) {
//            Font localFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
//            Font conFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon();
//            FontMetrics localMetrics = g.getFontMetrics(localFont);
//            FontMetrics conMetrics = g.getFontMetrics(conFont);
//            g.setColor(Color.black);
//            
//            int wordEnd;
//            int dropPosition;
//            int height;
//            String printValue;
//            
//            printValue = word.getLocalWord();
//            wordEnd = conMetrics.stringWidth(word.getValue());
//            dropPosition = localMetrics.getHeight();
//            height = conMetrics.getHeight();
//            g.setFont(localFont);
//            g.drawLine(wordEnd + 10, 0, wordEnd + 10, height);
//            g.drawString(printValue, wordEnd + 15, dropPosition);
//        }

    public boolean isAddLocalExtraText() {
        return addLocalExtraText;
    }

    public void setAddLocalExtraText(boolean addLocalExtraText) {
        this.addLocalExtraText = addLocalExtraText;
    }
}
