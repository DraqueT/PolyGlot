/*
 * Copyright (c) 2021, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.HelpHandler;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.WebInterface;

/**
 *
 * @author draque
 */
public class DesktopHelpHandler implements HelpHandler {

    @Override
    public void openHelp() {
        openHelpToLocation("");
    }

    @Override
    public void openHelpToLocation(String location) {
        try {
            if (WebInterface.isInternetConnected()) {
                DesktopOSHandler.browseToLocation(PGTUtil.HELP_FILE_URL + "#" + location);
            } else {
                openHelpLocal();
            }
        }
        catch (IOException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox().error("Help File Error", "Unable to open help file.");
        }
    }

    @Override
    public void openHelpLocal() throws IOException {
        File readmeDir = DesktopIOHandler.getInstance().unzipResourceToSystemTempLocation(PGTUtil.HELP_FILE_ARCHIVE_LOCATION);
        File readmeFile = new File(readmeDir.getAbsolutePath() + File.separator + PGTUtil.HELP_FILE_NAME);

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(readmeFile.toURI());
        } else if (PGTUtil.IS_LINUX) {
            Desktop.getDesktop().open(readmeFile);
        } else {
            new DesktopInfoBox().warning("Menu Warning", "Unable to open browser. Please load manually at:\n"
                    + "http://draquet.github.io/PolyGlot/readme.html\n(copied to clipboard for convenience)");
            new ClipboardHandler().setClipboardContents("http://draquet.github.io/PolyGlot/readme.html");
        }
    }
}
