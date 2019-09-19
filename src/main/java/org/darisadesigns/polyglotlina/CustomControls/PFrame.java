/*
 * Copyright (c) 2014 - 2019, Draque Thompson - draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.CustomControls;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.PGTUtil.WindowMode;
import org.darisadesigns.polyglotlina.Screens.ScrMainMenu;
import org.darisadesigns.polyglotlina.Screens.ScrPrintToPDF;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

/**
 * superclass for JFrame windows in PolyGlot. Includes setup instructions for
 * features like mac copy/paste in PolyGlot
 *
 * @author Draque
 */
public abstract class PFrame extends JFrame implements FocusListener, WindowFocusListener {

    private final JMenuItem mnuPublish = new JMenuItem();
    private final JMenuItem mnuSave = new JMenuItem();
    private final JMenuItem mnuNew = new JMenuItem();
    private final JMenuItem mnuExit = new JMenuItem();
    private final JMenuItem mnuOpen = new JMenuItem();
    protected DictCore core;
    private boolean isDisposed = false;
    private boolean ignoreCenter = false;
    private boolean hasFocus = false;
    protected WindowMode mode = WindowMode.STANDARD;
    protected int frameState = -1;
    private boolean firstVisible = true;
    private boolean curResizing;

    public PFrame() {
        this.addWindowStateListener(this::setWindowState);
    }

    @Override
    public final void addWindowStateListener(WindowStateListener listener) {
        super.addWindowStateListener(listener);
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
            core.getOptionsManager().setScreenPosition(getClass().getName(),
                    this.getLocation());
            if (this.isResizable()) { // do not save size of non-resizable windows
                core.getOptionsManager().setScreenSize(getClass().getName(),
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
                IOHandler.writeErrorLog(e);
                InfoBox.error("Closing Error", "Window failed to close: " + ex.getLocalizedMessage(), core.getRootWindow());
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
            setupAccelerators();

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

    /**
     * sets menu accelerators and menu item text to reflect this
     */
    public void setupAccelerators() {
        mnuPublish.addActionListener((ActionEvent e) -> {
            ScrPrintToPDF.run(core);
        });
        this.rootPane.add(mnuPublish);

        mnuSave.addActionListener((java.awt.event.ActionEvent evt) -> {
            core.coreSave();
        });
        this.rootPane.add(mnuSave);

        mnuNew.addActionListener((java.awt.event.ActionEvent evt) -> {
            core.coreNew(true);
        });
        this.rootPane.add(mnuNew);

        mnuOpen.addActionListener((java.awt.event.ActionEvent evt) -> {
            core.coreOpen();
        });
        this.rootPane.add(mnuOpen);

        mnuExit.addActionListener((java.awt.event.ActionEvent evt) -> {
            dispose();
        });
        this.rootPane.add(mnuExit);

        String OS = System.getProperty("os.name");
        if (OS.startsWith("Mac")) {
            mnuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.META_DOWN_MASK));
            mnuNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.META_DOWN_MASK));
            mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.META_DOWN_MASK));
            mnuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.META_DOWN_MASK));
            mnuPublish.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.META_DOWN_MASK));
        } else {
            mnuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.CTRL_DOWN_MASK));
            mnuNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.CTRL_DOWN_MASK));
            mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.CTRL_DOWN_MASK));
            mnuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.CTRL_DOWN_MASK));
            mnuPublish.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.CTRL_DOWN_MASK));
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
        saveAllValues();
        
        if (core != null) {
            core.pushUpdate();
        }
    }

    public abstract void addBindingToComponent(JComponent c);

    @Override
    public boolean isFocusOwner() {
        return hasFocus;
    }

    // positions on screen once form has already been build/sized
    @Override
    public void setVisible(boolean visible) {
        // only run setup stuff the initial visibility setting
        if (firstVisible) {
            if (core != null) {
                Point lastPos = core.getOptionsManager().getScreenPosition(getClass().getName());
                if (lastPos != null) {
                    setLocation(lastPos);
                } else if (!ignoreCenter) {
                    this.setLocationRelativeTo(null);
                }

                Dimension lastDim = core.getOptionsManager().getScreenSize(getClass().getName());
                if (lastDim != null) {
                    setSize(lastDim);
                }

                if (core == null && !(this instanceof ScrMainMenu)) {
                    InfoBox.error("Dict Core Null", "Dictionary core not set in new window.", core.getRootWindow());
                }
                addWindowFocusListener(this);
            }

            super.getRootPane().getContentPane().setBackground(Color.white);
            firstVisible = false;
        }

        setupAccelerators();

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
     * @param wait whether to wait on animation finishing before continuing
     * @throws java.lang.InterruptedException
     */
    public void setSizeSmooth(final int width, final int height, boolean wait) throws InterruptedException {
        final int numFrames = 20; // total number of frames to animate
        final int msDelay = 20; // ms delay between frames
        final int initialX = this.getWidth();
        final int initialY = this.getHeight();
        final float xDif = width - initialX;
        final float yDif = height - initialY;
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        setCurResizing(true);

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
