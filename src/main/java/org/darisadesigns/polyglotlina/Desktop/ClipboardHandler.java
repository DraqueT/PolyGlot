/*
 * Copyright (c) 2016-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop;

import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.io.IOException;
import org.darisadesigns.polyglotlina.PGTUtil;

public final class ClipboardHandler implements ClipboardOwner {

    private Transferable cachedContents;

    @Override
    public void lostOwnership(Clipboard aClipboard, Transferable aContents) {
        windowsPause();
    }

    /**
     * Windows sometimes requires a moment to regain ownership of clipboard. If
     * windows is not the host OS, does nothing
     */
    private static void windowsPause() {
        if (PGTUtil.IS_WINDOWS) {
            try {
                Thread.sleep(PGTUtil.WINDOWS_CLIPBOARD_DELAY);
            }
            catch (InterruptedException e) {
                DesktopIOHandler.getInstance().writeErrorLog(e);
            }
        }
    }

    /**
     * Place a String on the clipboard, and make this class the owner of the
     * Clipboard's contents.
     *
     * @param aString
     */
    public void setClipboardContents(String aString) {
        windowsPause();
        StringSelection stringSelection = new StringSelection(aString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    /**
     * Get the String residing on the clipboard.
     *
     * @return any text found on the Clipboard; if none found, return an empty
     * String.
     * @throws java.awt.datatransfer.UnsupportedFlavorException
     * @throws java.io.IOException
     */
    public static String getClipboardText() throws UnsupportedFlavorException, IOException {
        String result = "";
        windowsPause();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);

        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            }
            catch (IOException e) {
                throw new IOException("Clipboard error: " + e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Tests whether the contents of clipboard is an image
     *
     * @return true if image
     */
    public static boolean isClipboardImage() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        return contents.isDataFlavorSupported(DataFlavor.imageFlavor);
    }

    /**
     * Tests whether the contents of clipboard is text
     *
     * @return true if image
     */
    public static boolean isClipboardString() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        return contents.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    /**
     * Gets any image contained in clipboard
     *
     * @return image if one present, null otherwise
     * @throws java.lang.Exception
     */
    public static Image getClipboardImage() throws Exception {
        Image ret = null;

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);

        if (contents != null && contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
                ret = (Image) contents.getTransferData(DataFlavor.imageFlavor);
            }
            catch (UnsupportedFlavorException | IOException e) {
                throw new Exception("Clipboard error: " + e.getLocalizedMessage(), e);
            }
        }

        return ret;
    }

    /**
     * Temporarily caches contents of clipboard
     *
     * @throws Exception
     */
    public void cacheClipboard() throws Exception {
        try {
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            cachedContents = clip.getContents(null);

        }
        catch (HeadlessException e) {
            throw new Exception("System busy, unable to perform action: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * restores clipboard contents from cache
     */
    public void restoreClipboard() {
        if (cachedContents != null) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            windowsPause();

            clipboard.setContents(cachedContents, null);
        }
    }
}
