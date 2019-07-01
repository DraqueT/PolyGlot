/*
 * Copyright (c) 2017, DThompson
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

import PolyGlot.IOHandler;
import PolyGlot.PFontHandler;
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
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolTipUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

/**
 * Custom tooltips. This class grabs hold of incoming ToolTip objects' size
 * values (to avoid having to integrate a single use item in a more complex
 * manner)
 * @author DThompson
 */
public class PToolTipUI extends ToolTipUI
{
    private static final PToolTipUI sharedInstance = new PToolTipUI();
    private static PropertyChangeListener sharedPropertyChangedListener;
    private PropertyChangeListener propertyChangeListener;
    private Font font;
    
    public PToolTipUI() {
        super();
        
        try {
            font = PFontHandler.getCharisUnicodeFontInitial().deriveFont((float)14.0);
        } catch (IOException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Font error", "Unable to load tooltip font: " 
                    + e.getLocalizedMessage(), null);
        }
    }

    public static ComponentUI createUI(JComponent c) {
        return sharedInstance;
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

    @Override
    public void paint(Graphics g, JComponent c) {
        String tipText = ((JToolTip)c).getTipText();
        
        tipText = tipText == null ? "" : tipText;

        c.setFont(font.deriveFont(font.getSize()));
        ((JToolTip)c).setTipText(tipText);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        Dimension size = new Dimension(metrics.stringWidth(tipText) + 2, metrics.getHeight() + 2);
        
        c.setSize(size);
        c.getParent().setSize(size);
        if (c.getParent().getParent() != null) {
            //c.getParent().getParent().setSize(size);
        }

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
        g.drawString(tipText, paintTextR.x + 1,
                paintTextR.y + metrics.getAscent());
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = c.getFontMetrics(font);
        Insets insets = c.getInsets();

        Dimension prefSize = new Dimension(insets.left+insets.right,
                                           insets.top+insets.bottom);
        String text = ((JToolTip)c).getTipText();

        if (text != null && text.length() != 0) {
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
     * Invoked when the <code>JCompoment</code> associated with the
     * <code>JToolTip</code> has changed, or at initialization time. This
     * should update any state dependant upon the <code>JComponent</code>.
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
