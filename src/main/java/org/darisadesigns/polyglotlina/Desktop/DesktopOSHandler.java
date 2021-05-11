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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.HelpHandler;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.InfoBox;
import org.darisadesigns.polyglotlina.Nodes.LexiconProblemNode;
import org.darisadesigns.polyglotlina.OSHandler;
import org.darisadesigns.polyglotlina.PFontHandler;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.Screens.ScrLanguageProblemDisplay;

/**
 *
 * @author pe1uca
 */
public class DesktopOSHandler extends OSHandler {
    
    public DesktopOSHandler(IOHandler _ioHandler, InfoBox _infoBox, HelpHandler _helpHandler, PFontHandler _fontHandler) {
        super(_ioHandler, _infoBox, _helpHandler, _fontHandler);
    }
    
    public void setWorkingDirectory(String cwd) {
        overrideProgramPath = cwd;
    }
    
    @Override
    public File getWorkingDirectory() {
        return overrideProgramPath != null || overrideProgramPath.isEmpty()
                ? PGTUtil.getDefaultDirectory()
                : new File(overrideProgramPath);
    }
    
    @Override
    public void openLanguageProblemDisplay(List<LexiconProblemNode> problems, DictCore _core) {
        new ScrLanguageProblemDisplay(problems, _core).setVisible(true);
    }
    
    @Override
    public void openLanguageReport(String reportContents) {
        try {
            File report = ioHandler.createTmpFileWithContents(reportContents, ".html");

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(report.toURI());
            } else if (PGTUtil.IS_LINUX) {
                Desktop.getDesktop().open(report);
            } else {
                infoBox.warning("Menu Warning", "Unable to open browser. Please load manually at: \n" 
                        + report.getAbsolutePath() + "\n (copied to clipboard for convenience)");
                new ClipboardHandler().setClipboardContents(report.getAbsolutePath());
            }
        } catch (IOException e) {
            infoBox.error("Report Build Error", "Unable to generate/display language statistics: " 
                    + e.getLocalizedMessage());
            ioHandler.writeErrorLog(e);
        }
    }
    
    /**
     * Tell the OS to open a browser and go to a given web address.(distinct
     * behavior in Linux due to lack of certain features)
     *
     * @param url
     * @throws java.io.IOException
     */
    public static void browseToLocation(String url) throws IOException {
        if (PGTUtil.IS_WINDOWS) {
            try {
                URI help = new URI(url);
                Desktop.getDesktop().browse(help);
            }
            catch (URISyntaxException e) {
                DesktopIOHandler.getInstance().writeErrorLog(e);
                new DesktopInfoBox(null).warning("Menu Warning", "Unable to open browser. Please load manually at:\n"
                        + url + "\n(copied to your clipboard for convenience)");
                new ClipboardHandler().setClipboardContents(url);
            }
        } else if (PGTUtil.IS_OSX) {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("open " + url);
        } else if (PGTUtil.IS_LINUX) {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("xdg-open " + url);
        } else {
            new DesktopInfoBox(null).warning("Menu Warning", "Unable to open browser. Please load manually at:\n"
                    + url + "\n(copied to your clipboard for convenience)");
            new ClipboardHandler().setClipboardContents(url);
        }
    }
}
