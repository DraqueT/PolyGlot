/*
 * Copyright (c) 2015, draque
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

import PolyGlot.DictCore;
import PolyGlot.ManagersCollections.PropertiesManager;
import PolyGlot.PGTools;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.Serializable;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author draque
 */
public class PTextField extends JTextField {

    private PFrame parent;
    private DictCore core;
    boolean skipRepaint = false;
    boolean curSetText = false;
    boolean overrideFont = false;
    SwingWorker worker = null;

    /**
     * Init for PDialogs
     *
     * @param _core
     */
    public PTextField(DictCore _core) {
        core = _core;
    }
    
    public PTextField(DictCore _core, boolean _overideFont) {
        core = _core;
        overrideFont = _overideFont;
    }

    /**
     * makes this component flash. If already flashing, does nothing.
     * @param _flashColor color to flash
     * @param isBack whether display color is background (rather than foreground)
     */
    public void makeFlash(Color _flashColor, boolean isBack) {
        if (worker == null || worker.isDone()) {
            worker = PGTools.getFlashWorker(this, _flashColor, isBack);
            worker.execute();
        }
    }
    
    /**
     * Init for PFrames
     *
     * @param _parent
     * @param _core
     */
    public PTextField(PFrame _parent, DictCore _core) {
        parent = _parent;
        DefaultBoundedRangeModel pVis = (DefaultBoundedRangeModel) this.getHorizontalVisibility();

        // remove change listener to add custom one
        for (ChangeListener chlist : pVis.getChangeListeners()) {
            pVis.removeChangeListener(chlist);
        }

        pVis.addChangeListener(new PScrollRepainter());
        core = _core;
    }
    
    // Overridden to meet code standards
    @Override
    public final BoundedRangeModel getHorizontalVisibility() {
        return super.getHorizontalVisibility();
    }

    class PScrollRepainter implements ChangeListener, Serializable {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!skipRepaint && !curSetText) {
                repaint();
            }
        }
    }

    /**
     * Prefixes the RTL character if not prefixed already
     */
    private void prefixRTL() {
        if (super.getText().startsWith("\u202e")) {
            return;
        }

        setText('\u202e' + getText());
    }

    private void defixRTL() {
        if (!super.getText().startsWith("\u202e")) {
            return;
        }

        setText(getText());
    }

    @Override
    public void repaint() {
        if (skipRepaint) {
            return;
        }

        try {
            if (parent != null) {
                core = parent.getCore();
            }
            if (core == null) {
                //InfoBox.error("RENDERING ERROR", "Unable to render text.", parent);
                return;
            }

            PropertiesManager propMan = core.getPropertiesManager();
            skipRepaint = true;
            if (!curSetText) {
                /*if (propMan.isEnforceRTL()) {
                    prefixRTL();
                } else {
                    defixRTL();
                }*/

                Font testFont = propMan.getFontCon();
                if (testFont != null 
                        && !overrideFont
                        && !testFont.getFamily().equals(getFont().getFamily())) {
                    setFont(testFont);
                }
            }
            skipRepaint = false;
        } catch (Exception e) {
            InfoBox.error("Repaint error", "Could not repaint component: " + e.getLocalizedMessage(), null);
            skipRepaint = false;
        }

        super.repaint();
    }

    @Override
    public void paint(Graphics g) {
        if (skipRepaint) {
            return;
        }

        if (parent != null) {
            core = parent.getCore();
        }
        if (core == null) {
            //InfoBox.error("RENDERING ERROR", "Unable to render text.", parent);
            return;
        }

        try {
            PropertiesManager propMan = core.getPropertiesManager();
            skipRepaint = true;
            if (propMan != null
                    && !curSetText
                    && propMan.isEnforceRTL()) {
                prefixRTL();
            }
            skipRepaint = false;
        } catch (Exception e) {
            InfoBox.error("Repaint error", "Could not repaint component: " + e.getLocalizedMessage(), null);
            skipRepaint = false;
        }

        try {
            super.paint(g);
        } catch (NullPointerException e) {
            /* Do nothing. This fires due to a Java bug between the 
             javax.swing.text.GlyphView class returning null values of fonts in 
             some instances where the javax.swing.text.GlyphPainter1.sync() class
             method is unable to properly handle it (it never checks an object for 
             a null value when the object is populated from a method that returns
             null under certain circumstances). Thanks, Java.*/
        }
    }

    @Override
    public void setText(String t
    ) {
        curSetText = true;
        try {
            super.setText(t);
        } catch (Exception e) {
            InfoBox.error("Set text error", "Could not set text component: " + e.getLocalizedMessage(), null);
        }

        curSetText = false;
    }

    @Override
    public String getText() {
        String ret = super.getText().replaceAll("\u202e", "");
        return core.getPropertiesManager().isEnforceRTL() ? "\u202e" + ret : ret;
    }
}
