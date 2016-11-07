/*
 * Copyright (c) 2016, draque.thompson
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

import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.io.IOException;

public final class ClipboardHandler implements ClipboardOwner {
    private Transferable cachedContents;
    /**
     * Empty implementation of the ClipboardOwner interface.
     *
     * @param aClipboard
     * @param aContents
     */
    @Override
    public void lostOwnership(Clipboard aClipboard, Transferable aContents) {
        //do nothing
    }

    /**
     * Place a String on the clipboard, and make this class the owner of the
     * Clipboard's contents.
     *
     * @param aString
     */
    public void setClipboardContents(String aString) {
        StringSelection stringSelection = new StringSelection(aString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    /**
     * Get the String residing on the clipboard.
     *
     * @return any text found on the Clipboard; if none found, return an empty
     * String.
     * @throws java.lang.Exception
     */
    public String getClipboardText() throws Exception {
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);

        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                throw new Exception("Clipboard error: " + e.getLocalizedMessage());
            }
        }
        return result;
    }
    
    /**
     * Tests whether the contents of clipboard is an image
     * @return true if image
     */
    public static boolean isClipboardImage() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        return contents.isDataFlavorSupported(DataFlavor.imageFlavor);
    }
    
    /**
     * Tests whether the contents of clipboard is text
     * @return true if image
     */
    public static boolean isClipboardString() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        return contents.isDataFlavorSupported(DataFlavor.stringFlavor);
    }
    
    /**
     * Gets any image contained in clipboard
     * @return image if one present, null otherwise
     * @throws java.lang.Exception
     */
    public static Image getClipboardImage() throws Exception {
        Image ret = null;
        
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
                ret = (Image)contents.getTransferData(DataFlavor.imageFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                throw new Exception("Clipboard error: " + e.getLocalizedMessage());
            }
        }
        
        return ret;
    }
    
    public void cacheClipboard() throws Exception {
        try {
            cachedContents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        } catch (Exception e) {
            throw new Exception("System busy, unable to perform action: " + e.getLocalizedMessage());
        }
    }
    
    public void restoreClipboard() {
        if (cachedContents == null)
        {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(cachedContents, null);
    }
}
