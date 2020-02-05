/*
 * Copyright (c) 2014-2019, Draque Thompson - draquemail@gmail.com
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

package org.darisadesigns.polyglotlina.CustomControls;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.PGTUtil.WindowMode;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.JDialog;

/**
 * superclass for JDialog windows in PolyGlot. Includes setup instructions for
 * features like mac copy/paste in PolyGlot
 * @author Draque
 */
public abstract class PDialog extends JDialog implements FocusListener, WindowFocusListener {
    private boolean isDisposed = false;
    protected final WindowMode mode = WindowMode.STANDARD;
    private boolean skipCenter = false;
    private boolean hasFocus = false;
    protected DictCore core;
    protected boolean firstVisible = true;
    protected boolean ignoreInitialResize = false;
    protected final double menuFontSize;
    protected final boolean nightMode;
       
    public PDialog(DictCore _core) {
        core = _core;
        menuFontSize = core.getOptionsManager().getMenuFontSize();
        nightMode = core.getOptionsManager().isNightMode();
        this.setupOS();
    }
    
    public PDialog(DictCore _core, boolean _firstVisible) {
        core = _core;
        firstVisible = _firstVisible;
        menuFontSize = core.getOptionsManager().getMenuFontSize();
        nightMode = core.getOptionsManager().isNightMode();
        this.setupOS();
    }
    
    /**
     * Sets up an OS specific display properties
     */
    private void setupOS() {
        if (PGTUtil.IS_LINUX) {
            this.setIconImage(PGTUtil.POLYGLOT_ICON.getImage());
        }
    }
    
    /**
     * Returns current running mode of window
     * @return 
     */
    public WindowMode getMode() {
        return mode;
    }
    
    public void setCore(DictCore _core) {
        core = _core;
    }
    
    @Override
    public void dispose() {
        if (!isDisposed) {
            core.getOptionsManager().setScreenPosition(getClass().getName(),
                this.getLocation());
            if (this.isResizable()) { // do not save size of non-resizable windows
                core.getOptionsManager().setScreenSize(getClass().getName(),
                    this.getSize());
            }
        }
        
        isDisposed = true;
        
        super.dispose();
    }
    
    /**
     * returns true if window has been disposed
     * @return disposed value
     */
    public boolean isDisposed() {
        return isDisposed;
    }
    
    /**
     * Forces window to update all relevant values from core
     * @param _core current dictionary core
     */
    public abstract void updateAllValues(DictCore _core);
    
    /**
     * Sets window visibly to the right of the window handed in
     *
     * @param w window to set location relative to
     */
    public void setBeside(final Window w) {
        final Window self = this;
        skipCenter = true;
        
        int x = w.getLocation().x + w.getWidth();
        int y = w.getLocation().y;

        self.setLocation(x, y);
    }
    
    // positions on screen once form has already been build/sized
    @Override
    public void pack() {
        super.pack();
        
        if (!skipCenter) {
            this.setLocationRelativeTo(null);
        }
    }
    
    @Override
    public void focusGained(FocusEvent fe) {
        // Do nothing
    }

    @Override
    public void focusLost(FocusEvent fe) {
        // Do nothing
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        hasFocus = true;
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        hasFocus = false;
        
        if (core != null) {
            core.pushUpdate();
        }
    }
    
    @Override
    public boolean isFocusOwner() {
        return hasFocus;
    }
    
    @Override
    public void setVisible(boolean visible) {
        if (firstVisible && !ignoreInitialResize) {
            if (core == null) {
                InfoBox.error("Dict Core Null", "Dictionary core not set in new window.", null);
            }

            Point lastPos = core.getOptionsManager().getScreenPosition(getClass().getName());
            if (lastPos != null) {
                setLocation(lastPos);
            }

            Dimension lastDim = core.getOptionsManager().getScreenSize(getClass().getName());
            if (lastDim != null) {
                setSize(lastDim);
            }

            this.setAlwaysOnTop(true);
            super.getRootPane().getContentPane().setBackground(Color.white);
            firstVisible = false;
        }
        
        super.setVisible(visible);

        // reposition appropriately if appears offscreen
        if (visible && this.isVisible()) {
            PGTUtil.checkPositionInBounds(this);
        }
    }
    
    /**
     * Forces fast dispose of window. Used primarily for testing.
     */
    public void hardDispose()  {
        super.dispose();
    }
    
    /**
     * Gets root visual component if dialog. Override to display something other than
     * root pane.
     * @return 
     */
    public Component getWindow() {
        return this.getRootPane();
    }
}
