/*
 * Copyright (c) 2017-2022, Draque Thompson
 * All rights reserved.
 *
 * Licensed under: MIT Licence
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthToolTipUI;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.PGTUtil;

/**
 * Custom tooltips. This class grabs hold of incoming ToolTip objects' size
 * values (to avoid having to integrate a single use item in a more complex
 * manner)
 * @author DThompson
 */
public class PToolTipUI extends SynthToolTipUI {

    private static final PToolTipUI SHARED_INSTANCE = new PToolTipUI();
    private Font font;
    private String lastTextRendered = "";
    private int height = 0;
    private int width = 0;
    private int fontHeight;
    private Rectangle textRectangle;
    private FontMetrics metrics;
    
    public static ComponentUI createUI(JComponent c) {
        return SHARED_INSTANCE;
    }

    /**
     * To override font, override createToolTip() on component creating tooltip.
     * On creation of ToolTip, change to desired font.
     * @param g
     * @param context
     */
    @Override
    protected void paint(SynthContext context, Graphics g) {
        JComponent c = context.getComponent();
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String tipText = ((JToolTip)c).getTipText();
        tipText = getFormattedTipText(tipText);
        String[] tipLines = tipText.split("\n");
        Insets insets = c.getInsets();
        
        // recalculate size values only if necessary
        if (font == null || !font.getFamily().equals(c.getFont().getFamily()) || !lastTextRendered.equals(tipText)) {
            setRenderingDetails(c);
        }
        
        ((JToolTip)c).setTipText(tipText);
        
        c.setSize(width, height);
        c.getParent().setSize(width, height);
        
        g.setColor(Color.black);
        g.fillRect(insets.left,
            insets.top,
            width - (insets.left + insets.right),
            height - (insets.top + insets.bottom));
        
        g.setColor(Color.white);
        
        for (int i = 0 ; i < tipLines.length; i++) {
            g.drawString(tipLines[i], textRectangle.x + 5,
                    textRectangle.y + metrics.getAscent() + (i * (fontHeight)));
        }
        
        c.setSize(width, height);
        componentChanged(c);
    }

    /**
     * Sets up all measurements needed for rendering (slightly expensive, do not call more than necessary)
     */
    private void setRenderingDetails(JComponent c) {
        Graphics g = c.getGraphics() == null 
                ? PolyGlot.getPolyGlot().getRootWindow().getGraphics() 
                : c.getGraphics();
        String tipText = ((JToolTip)c).getTipText();
        tipText = getFormattedTipText(tipText);
        String[] tipLines = tipText.split("\n");
        Insets insets = c.getInsets();
        lastTextRendered = tipText;
            
        font = c.getFont();

        g.setFont(font);

        metrics = g.getFontMetrics();
        fontHeight = metrics.getHeight();
        height = (fontHeight * tipLines.length) + 2;
        width = this.getWidestStringText(tipLines, metrics) + 10;

        int fontSize = font.getSize();
        fontSize = fontSize == 0 ? PGTUtil.DEFAULT_FONT_SIZE.intValue() : fontSize;
        textRectangle = new Rectangle(
            insets.left,
            insets.top,
            width - (insets.left + insets.right),
            height - (insets.top + insets.bottom));

        c.setFont(font.deriveFont(fontSize));
    }

    /**
     * Formats tip text appropriately with linebreaks if it is not formatted initially.
     * @param text
     * @return 
     */
    private String getFormattedTipText(String text) {
        if (text == null) {
            return "";
        }
        
        // if it contains a linebreak, it has been manually formatted already
        if (text.contains("\n")) {
            return text;
        }
        
        var lines = new ArrayList<String>();
        String curLine = "";
        
        for (String token : text.split(" ")) {
            if (curLine.length() > 0 && curLine.length() + token.length() > PGTUtil.MAX_TOOLTIP_LENGTH) {
                lines.add(curLine);
                curLine = "";
            }
            if (!curLine.isBlank()) {
                curLine += " ";
            }
            curLine += token;
        }
        
        if (!curLine.isBlank()) {
            lines.add(curLine);
        }
        
        return lines.stream().collect(Collectors.joining("\n")).trim();
    }
    
    /**
     * Finds widest segment of text based on its rendering
     * @param test 
     */
    private int getWidestStringText(String[] test, FontMetrics metrics) {
        int ret = 0;
        
        for (String curLine : test) {
            int curWidth = metrics.stringWidth(curLine);
            ret = curWidth > ret ? curWidth : ret;
        }
        
        return ret;
    }
    
    @Override
    public Dimension getPreferredSize(JComponent c) {
        if (width == 1 || width == 0) {
            setRenderingDetails(c);
        }
        
        return new Dimension(width, height);
    }

    /**
     * Invoked when the {@code JComponent} associated with the
     * <code>JToolTip</code> has changed, or at initialization time. This
     * should update any state dependent upon the {@code JComponent}.
     *
     * @param c the JToolTip the JComponent has changed on.
     */
    private void componentChanged(JComponent c) {
        JComponent comp = ((JToolTip)c).getComponent();

        if (comp != null && !(comp.isEnabled())) {
            // For better backward compatibility, only install inactive
            // properties if they are defined.
            if (UIManager.getBorder("ToolTip.borderInactive") != null) {
                LookAndFeel.installBorder(c, "ToolTip.borderInactive");
            } else {
                LookAndFeel.installBorder(c, "ToolTip.border");
            }
            
            if (UIManager.getColor("ToolTip.backgroundInactive") != null) {
                LookAndFeel.installColors(c,"ToolTip.backgroundInactive",
                                          "ToolTip.foregroundInactive");
            } else {
                LookAndFeel.installColors(c,"ToolTip.background",
                                          "ToolTip.foreground");
            }
        } else {
            LookAndFeel.installBorder(c, "ToolTip.border");
            LookAndFeel.installColors(c, "ToolTip.background",
                                      "ToolTip.foreground");
        }
    }
}
