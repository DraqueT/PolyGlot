/*
 * Copyright (c) 2014 - 2019, Draque Thompson - draquemail@gmail.com
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
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil.WindowMode;
import org.darisadesigns.polyglotlina.Screens.ScrMainMenu;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.darisadesigns.polyglotlina.PolyGlot;

/**
 * superclass for JFrame windows in PolyGlot. Includes setup instructions for
 * features like mac copy/paste in PolyGlot
 *
 * @author Draque
 */
public abstract class PFrame extends JFrame implements FocusListener {

    protected DictCore core;
    private boolean isDisposed = false;
    private boolean ignoreCenter = false;
    protected WindowMode mode = WindowMode.STANDARD;
    protected int frameState = -1;
    private boolean firstVisible = true;
    private boolean curResizing;
    protected final double menuFontSize;
    protected final boolean nightMode;

    protected PFrame(DictCore _core) {
        core = _core;
        menuFontSize = PolyGlot.getPolyGlot().getOptionsManager().getMenuFontSize();
        nightMode = PolyGlot.getPolyGlot().getOptionsManager().isNightMode();
        this.addWindowStateListener(this::setWindowState);
        this.setupOS();
    }
    
    protected PFrame(DictCore _core, WindowMode _mode) {
        core = _core;
        mode = _mode;
        menuFontSize = PolyGlot.getPolyGlot().getOptionsManager().getMenuFontSize();
        nightMode = PolyGlot.getPolyGlot().getOptionsManager().isNightMode();
        this.addWindowStateListener(this::setWindowState);
        this.setupOS();
    }

    @Override
    public synchronized final void addWindowStateListener(WindowStateListener listener) {
        super.addWindowStateListener(listener);
    }
    
    /**
     * Sets up an OS specific display properties
     */
    private void setupOS() {
        if (PGTUtil.IS_LINUX || PGTUtil.IS_WINDOWS) {
            this.setIconImage(PGTUtil.POLYGLOT_ICON.getImage());
        }
    }

    /**
     * Gets frame state of frame
     *
     * @return -1 for none set, otherwise Frame.ICONIFIED or
     * Frame.MAXIMIZED_BOTH
     */
    public Integer getFrameState() {
        return frameState;
    }

    /**
     * Used to set frame state of window
     *
     * @param e
     */
    private void setWindowState(WindowEvent e) {
        if ((e.getNewState() & Frame.ICONIFIED) == Frame.ICONIFIED) {
            frameState = Frame.ICONIFIED;
        } else if ((e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            frameState = Frame.MAXIMIZED_BOTH;
        } else {
            frameState = -1;
        }
    }

    /**
     * Returns current running mode of window
     *
     * @return
     */
    public WindowMode getMode() {
        return mode;
    }

    /**
     * Returns whether target window can close. In implementation, all cases
     * where false is returned should also generate a pop-up explaining to the
     * user why it cannot close.
     *
     * @return true if can close. False otherwise.
     */
    public abstract boolean canClose();

    /**
     * Records all active/volatile values to core
     */
    public abstract void saveAllValues();

    @Override
    public void dispose() {
        if (!isDisposed) {
            PolyGlot.getPolyGlot().getOptionsManager().setScreenPosition(getClass().getName(),
                    this.getLocation());
            if (this.isResizable()) { // do not save size of non-resizable windows
                PolyGlot.getPolyGlot().getOptionsManager().setScreenSize(getClass().getName(),
                        this.getSize());
            }
        }

        isDisposed = true;

        try {
            super.dispose();
        } catch (IllegalStateException e) {
            try {
                TimeUnit.MICROSECONDS.sleep(250);
                super.dispose();
            } catch (IllegalStateException | InterruptedException ex) {
                DesktopIOHandler.getInstance().writeErrorLog(e);
                core.getOSHandler().getInfoBox().error("Closing Error", "Window failed to close: " + ex.getLocalizedMessage());
            }
        }
    }

    /**
     * returns true if window has been disposed
     *
     * @return disposed value
     */
    public boolean isDisposed() {
        return isDisposed;
    }

    /**
     * Sets window visibly to the right of the window handed in
     *
     * @param w window to set location relative to
     */
    public void setBeside(final Window w) {
        int x = w.getLocation().x + w.getWidth();
        int y = w.getLocation().y;

        setLocation(x, y);
        ignoreCenter = true;
    }

    /**
     * Forces window to update all relevant values from core
     *
     * @param _core current dictionary core
     */
    public abstract void updateAllValues(DictCore _core);

    /**
     * Recursive method that adds appropriate key bindings to all components
     *
     * @param curObject parent object within form
     */
    protected void addBindingsToPanelComponents(Object curObject) {
        if (curObject instanceof JRootPane) {
            addBindingToComponent((JComponent) curObject);
            JRootPane root = (JRootPane) curObject;
            Component[] components = root.getComponents();

            for (Component c : components) {
                addBindingsToPanelComponents(c);
            }
        } else if (curObject instanceof JLayeredPane) {
            JLayeredPane lPane = (JLayeredPane) curObject;
            Component[] components = lPane.getComponents();

            for (Component c : components) {
                addBindingsToPanelComponents(c);
            }
        } else if (curObject instanceof JPanel) {
            JPanel jPanel = (JPanel) curObject;
            Component[] components = jPanel.getComponents();

            for (Component c : components) {
                addBindingsToPanelComponents(c);
            }
        } else if (curObject instanceof JSplitPane) {
            JSplitPane sPane = (JSplitPane) curObject;
            Component[] components = sPane.getComponents();

            for (Component c : components) {
                addBindingsToPanelComponents(c);
            }
        } else if (curObject instanceof JScrollPane) {
            JScrollPane sPane = (JScrollPane) curObject;
            Component[] components = sPane.getComponents();

            for (Component c : components) {
                addBindingsToPanelComponents(c);
            }
        } else if (curObject instanceof JComponent) {
            addBindingToComponent((JComponent) curObject);
        }
    }

    /**
     * Get core from PFrame (used by custom PolyGlot elements)
     *
     * @return current dictionary core
     */
    public DictCore getCore() {
        return core;
    }

    public void setCore(DictCore _core) {
        core = _core;
    }

    @Override
    public void focusGained(FocusEvent fe) {
        // Do nothing
    }

    @Override
    public void focusLost(FocusEvent fe) {
        // Do nothing
    }

    public abstract void addBindingToComponent(JComponent c);

    // positions on screen once form has already been build/sized
    @Override
    public void setVisible(boolean visible) {
        // only run setup stuff the initial visibility setting
        if (firstVisible) {
            if (core != null) {
                Point lastPos = PolyGlot.getPolyGlot().getOptionsManager().getScreenPosition(getClass().getName());
                if (lastPos != null) {
                    setLocation(lastPos);
                } else if (!ignoreCenter) {
                    this.setLocationRelativeTo(null);
                }

                Dimension lastDim = PolyGlot.getPolyGlot().getOptionsManager().getScreenSize(getClass().getName());
                if (lastDim != null) {
                    setSize(lastDim);
                }

                if (core == null && !(this instanceof ScrMainMenu)) {
                    new DesktopInfoBox(null).error("Dict Core Null", "Dictionary core not set in new window.");
                }
            }

            super.getRootPane().getContentPane().setBackground(Color.white);
            firstVisible = false;
        }

        super.setVisible(visible);

        // reposition appropriately if appears offscreen
        if (visible && this.isVisible()) {
            PGTUtil.checkPositionInBounds(this);
        }
    }

    @Override
    public void paint(Graphics g) {
        if (!curResizing) {
            super.paint(g);
        }
    }

    @Override
    public void paintComponents(Graphics g) {
        if (!curResizing) {
            super.paintComponents(g);
        }
    }

    @Override
    public void repaint() {
        if (!curResizing) {
            super.repaint();
        }
    }

    public void setCurResizing(boolean _resizing) {
        curResizing = _resizing;
    }

    /**
     * Smoothly resizes window with animation
     *
     * @param width new width of element
     * @param height new height of element
     */
    public void setSizeSmooth(final int width, final int height) {
        final int numFrames = 20; // total number of frames to animate
        final int msDelay = 20; // ms delay between frames
        final int initialX = this.getWidth();
        final int initialY = this.getHeight();
        final float xDif = width - initialX;
        final float yDif = height - initialY;
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        curResizing = true;

        executorService.scheduleAtFixedRate(new Runnable() {
            int framesRun = 0;

            @Override
            public void run() {
                if (framesRun >= numFrames) {
                    PFrame.super.setSize(width, height);
                    setCurResizing(false);
                    repaint();
                    executorService.shutdown();
                    return;
                }

                float newX = initialX + (xDif / numFrames) * (framesRun + 1);
                float newY = initialY + (yDif / numFrames) * (framesRun + 1);
                PFrame.super.setSize((int) newX, (int) newY);

                framesRun++;
            }
        }, 0, msDelay, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Forces fast dispose of window. Used primarily for testing.
     */
    public void hardDispose()  {
        super.dispose();
    }
            

    public abstract Component getWindow();
}
