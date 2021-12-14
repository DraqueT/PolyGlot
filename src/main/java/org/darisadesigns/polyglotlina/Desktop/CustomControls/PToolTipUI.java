/*
 * Copyright (c) 2017-2021, Draque Thompson
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolTipUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import org.darisadesigns.polyglotlina.PGTUtil;

/**
 * Custom tooltips. This class grabs hold of incoming ToolTip objects' size
 * values (to avoid having to integrate a single use item in a more complex
 * manner)
 * @author DThompson
 */
public class PToolTipUI extends ToolTipUI
{
    private static final PToolTipUI SHARED_INSTANCE = new PToolTipUI();
    private static PropertyChangeListener sharedPropertyChangedListener;
    private PropertyChangeListener propertyChangeListener;
    
    public PToolTipUI() {
        super();
    }

    public static ComponentUI createUI(JComponent c) {
        return SHARED_INSTANCE;
    }

    @Override
    public void installUI(JComponent c) {
        installDefaults(c);
        installComponents(c);
        installListeners(c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        // REMIND: this is NOT getting called
        uninstallDefaults(c);
        uninstallComponents(c);
        uninstallListeners(c);
    }

    protected void installDefaults(JComponent c){
        LookAndFeel.installColorsAndFont(c, "ToolTip.background",
                                         "ToolTip.foreground",
                                         "ToolTip.font");
        LookAndFeel.installProperty(c, "opaque", Boolean.TRUE);
        componentChanged(c);
    }

   protected void uninstallDefaults(JComponent c){
        LookAndFeel.uninstallBorder(c);
    }

    /* Unfortunately this has to remain private until we can make API additions.
     */
    private void installComponents(JComponent c){
        BasicHTML.updateRenderer(c, ((JToolTip)c).getTipText());
    }

    /* Unfortunately this has to remain private until we can make API additions.
     */
    private void uninstallComponents(JComponent c){
        BasicHTML.updateRenderer(c, "");
    }

    protected void installListeners(JComponent c) {
        propertyChangeListener = createPropertyChangeListener(c);

        c.addPropertyChangeListener(propertyChangeListener);
    }

    protected void uninstallListeners(JComponent c) {
        c.removePropertyChangeListener(propertyChangeListener);

        propertyChangeListener = null;
    }

    /* Unfortunately this has to remain private until we can make API additions.
     */
    private PropertyChangeListener createPropertyChangeListener(JComponent c) {
        if (sharedPropertyChangedListener == null) {
            sharedPropertyChangedListener = new PropertyChangeHandler();
        }
        return sharedPropertyChangedListener;
    }

    /**
     * To override font, override createToolTip() on component creating tooltip.
     * On creation of ToolTip, change to desired font.
     * @param g
     * @param c 
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        String tipText = ((JToolTip)c).getTipText();
        tipText = getFormattedTipText(tipText);
        String[] tipLines = tipText.split("\n");
        Font font = c.getFont();
        
        g.setFont(font);
        
        FontMetrics metrics = g.getFontMetrics();
        int fontHeight = metrics.getHeight();
        int height = (fontHeight * tipLines.length) + 2;
        int width = this.getWidestStringText(tipLines, metrics) + 10;
        
        int fontSize = font.getSize();
        fontSize = fontSize == 0 ? PGTUtil.DEFAULT_FONT_SIZE.intValue() : fontSize;
        c.setFont(font.deriveFont(fontSize));
        ((JToolTip)c).setTipText(tipText);
        
        
        Dimension size = new Dimension(width, height);
        
        c.setSize(size);
        c.getParent().setSize(size);
        
        Insets insets = c.getInsets();
        Rectangle paintTextR = new Rectangle(
            insets.left,
            insets.top,
            size.width - (insets.left + insets.right),
            size.height - (insets.top + insets.bottom));
        
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(Color.black);
        g.fillRect(insets.left,
            insets.top,
            size.width - (insets.left + insets.right),
            size.height - (insets.top + insets.bottom));
        
        g.setColor(Color.white);
        
        for (int i = 0 ; i < tipLines.length; i++) {
            g.drawString(tipLines[i], paintTextR.x + 5,
                    paintTextR.y + metrics.getAscent() + (i * (fontHeight)));
        }
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
        
        return lines.stream().collect(Collectors.joining("\n"));
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
        FontMetrics fm = c.getFontMetrics(c.getFont());
        Insets insets = c.getInsets();

        Dimension prefSize = new Dimension(insets.left+insets.right,
                                           insets.top+insets.bottom);
        String text = ((JToolTip)c).getTipText();

        if (text != null && !text.isEmpty()) {
            prefSize.width += fm.stringWidth(text);
            prefSize.height += fm.getHeight();
        }
        return prefSize;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        Dimension d = getPreferredSize(c);
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d.width -= v.getPreferredSpan(View.X_AXIS) - v.getMinimumSpan(View.X_AXIS);
        }
        return d;
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        Dimension d = getPreferredSize(c);
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d.width += v.getMaximumSpan(View.X_AXIS) - v.getPreferredSpan(View.X_AXIS);
        }
        return d;
    }

    /**
     * Invoked when the {@code JComponent} associated with the
     * <code>JToolTip</code> has changed, or at initialization time. This
     * should update any state dependant upon the {@code JComponent}.
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
            }
            else {
                LookAndFeel.installBorder(c, "ToolTip.border");
            }
            if (UIManager.getColor("ToolTip.backgroundInactive") != null) {
                LookAndFeel.installColors(c,"ToolTip.backgroundInactive",
                                          "ToolTip.foregroundInactive");
            }
            else {
                LookAndFeel.installColors(c,"ToolTip.background",
                                          "ToolTip.foreground");
            }
        } else {
            LookAndFeel.installBorder(c, "ToolTip.border");
            LookAndFeel.installColors(c, "ToolTip.background",
                                      "ToolTip.foreground");
        }
    }


    private static class PropertyChangeHandler implements
                                 PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if (name.equals("tiptext") || "font".equals(name) ||
                "foreground".equals(name)) {
                // remove the old html view client property if one
                // existed, and install a new one if the text installed
                // into the JLabel is html source.
                JToolTip tip = ((JToolTip) e.getSource());
                String text = tip.getTipText();
                BasicHTML.updateRenderer(tip, text);
            }
            else if ("component".equals(name)) {
                JToolTip tip = ((JToolTip) e.getSource());

                if (tip.getUI() instanceof PToolTipUI) {
                    ((PToolTipUI)tip.getUI()).componentChanged(tip);
                }
            }
        }
    }
}
