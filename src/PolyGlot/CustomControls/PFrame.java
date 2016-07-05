/*
 * Copyright (c) 2014 - 2015, Draque Thompson - draquemail@gmail.com
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
import PolyGlot.PGTUtil.WindowMode;
import PolyGlot.Screens.ScrDictMenu;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
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

    private final JMenuItem mnuSave = new JMenuItem();
    private final JMenuItem mnuNew = new JMenuItem();
    private final JMenuItem mnuExit = new JMenuItem();
    private final JMenuItem mnuOpen = new JMenuItem();
    protected DictCore core;
    private boolean isDisposed = false;
    private boolean ignoreCenter = false;
    private boolean hasFocus = false;
    protected WindowMode mode = WindowMode.STANDARD;

    /**
     * Returns current running mode of window
     *
     * @return
     */
    public WindowMode getMode() {
        return mode;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            core.getOptionsManager().setScreenPosition(getClass().getName(),
                this.getLocation());
        }
        
        isDisposed = true;
        
        super.dispose();
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
     * Tests whether given window or any child windows posess focus
     *
     * @return true if focus held, false otherwise
     */
    public abstract boolean thisOrChildrenFocused();

    /**
     * enable cut/copy/paste/select all if running on a Mac, and any other
     * specific, text based bindings I might choose to add later
     */
    protected void setupKeyStrokes() {
        int mask;
        String OS = System.getProperty("os.name");

        if (OS.startsWith("Mac")) {
            mask = KeyEvent.META_DOWN_MASK;
        } else {
            mask = KeyEvent.CTRL_DOWN_MASK;
        }

        if (OS.startsWith("Mac")) {
            addTextBindings("TextField.focusInputMap", mask);
            addTextBindings("TextArea.focusInputMap", mask);
            addTextBindings("TextPane.focusInputMap", mask);
        }
    }

    /**
     * Adds copy/paste/cut/select all bindings to the input map provided
     *
     * @param UIElement the string representing a UI Element in UIManager
     * @param mask the mask to associate the binding with (command or control,
     * for Macs or PC/Linux boxes, respectively.)
     */
    private void addTextBindings(final String UIElement, final int mask) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                InputMap im = (InputMap) UIManager.get(UIElement);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.copyAction);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.pasteAction);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.cutAction);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.selectAllAction);
                UIManager.put(UIElement, im);
            }
        };
        SwingUtilities.invokeLater(runnable);

    }

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
        mnuSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                core.coreSave();
            }
        });
        this.rootPane.add(mnuSave);

        mnuNew.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                core.coreNew(true);
            }
        });
        this.rootPane.add(mnuNew);

        mnuOpen.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                core.coreOpen();
            }
        });
        this.rootPane.add(mnuOpen);

        mnuExit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });
        this.rootPane.add(mnuExit);

        String OS = System.getProperty("os.name");
        if (OS.startsWith("Mac")) {
            mnuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
        } else {
            mnuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
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
        core.checkProgramFocus();
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        hasFocus = false;
        core.checkProgramFocus();
    }

    public abstract void addBindingToComponent(JComponent c);

    @Override
    public boolean isFocusOwner() {
        return hasFocus;
    }

    // positions on screen once form has already been build/sized
    @Override
    public void setVisible(boolean visible) {
        if (core != null) {
            Point lastPos = core.getOptionsManager().getScreenPosition(getClass().getName());
            if (lastPos != null) {
                setLocation(lastPos);
            } else if (!ignoreCenter) {
                this.setLocationRelativeTo(null);
            }

            if (core == null && !(this instanceof ScrDictMenu)) {
                InfoBox.error("Dict Core Null", "Dictionary core not set in new window.", this);
            }
            addWindowFocusListener(this);
        }
        super.setVisible(visible);
    }
}
