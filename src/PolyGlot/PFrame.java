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
package PolyGlot;

import PolyGlot.PGTUtil.WindowMode;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

/**
 * superclass for JFrame windows in PolyGlot. Includes setup instructions for
 * features like mac copy/paste in PolyGlot
 *
 * @author Draque
 */
public abstract class PFrame extends JFrame {
    private boolean isDisposed = false;
    private boolean ignoreCenter = false;
    protected WindowMode mode = WindowMode.STANDARD;
    
    /**
     * Returns current running mode of window
     * @return 
     */
    public WindowMode getMode() {
        return mode;
    }
    
    @Override
    public void dispose() {
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
     * enable cut/copy/paste if running on a mac
     */
    protected void setupKeyStrokes() {
        int mask;
        
        if (System.getProperty("os.name").startsWith("Mac")) {
            mask = KeyEvent.META_DOWN_MASK;           
        } else {
            mask = KeyEvent.CTRL_DOWN_MASK;
        }
        
        InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.cutAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.selectAllAction);
            UIManager.put("TextField.focusInputMap", im);
            
            im = (InputMap) UIManager.get("TextArea.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.cutAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.cutAction);
            UIManager.put("TextArea.focusInputMap", im);
            
            im = (InputMap) UIManager.get("TextPane.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.cutAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), DefaultEditorKit.cutAction);
            UIManager.put("TextPane.focusInputMap", im); 
    }
    
    /**
     * Recursive method that adds appropriate key bindings to all components
     * @param curObject parent object within form
     */
    protected void addBindingsToPanelComponents(Object curObject) {        
        if (curObject instanceof JRootPane) {
            addBindingToComponent((JComponent)curObject);
            JRootPane root = (JRootPane)curObject;
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
            addBindingToComponent((JComponent)curObject);
        } 
    }
    
    public abstract void addBindingToComponent(JComponent c);
    
    // positions on screen once form has already been build/sized
    @Override
    public void setVisible(boolean visible) {
        if (!ignoreCenter) {
            this.setLocationRelativeTo(null);
        }
        
        super.setVisible(visible);     
    }
}
