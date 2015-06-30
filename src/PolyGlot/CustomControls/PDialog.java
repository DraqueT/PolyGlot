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

import PolyGlot.PGTUtil.WindowMode;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.InputMap;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

/**
 * superclass for JDialog windows in PolyGlot. Includes setup instructions for
 * features like mac copy/paste in PolyGlot
 * @author Draque
 */
public abstract class PDialog extends JDialog{
    private boolean isDisposed = false;
    protected WindowMode mode = WindowMode.STANDARD;
    private boolean skipCenter = false;
    
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
     * @return disposed value
     */
    public boolean isDisposed() {
        return isDisposed;
    }
    
    /**
     * Forces window to update all relevant values from core
     */
    public abstract void updateAllValues();
    
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
    
    /**
     * enable cut/copy/paste if running on a mac
     */
    protected void setupKeyStrokes() {
        if (System.getProperty("os.name").startsWith("Mac")) {
            InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
            UIManager.put("TextField.focusInputMap", im);
            im = (InputMap) UIManager.get("TextArea.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
            UIManager.put("TextArea.focusInputMap", im);
            im = (InputMap) UIManager.get("TextPane.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
            UIManager.put("TextPane.focusInputMap", im);
        }
    }
    
    // positions on screen once form has already been build/sized
    @Override
    public void pack() {
        super.pack();
        
        if (!skipCenter) {
            this.setLocationRelativeTo(null);
        }
    }
}
