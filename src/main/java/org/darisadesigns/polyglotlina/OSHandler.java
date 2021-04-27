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
package org.darisadesigns.polyglotlina;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author pe1uca
 */
public abstract class OSHandler {
    
    private final IOHandler ioHandler;
    private final InfoBox infoBox;
    private final HelpHandler helpHandler;
    
    public OSHandler(IOHandler _ioHandler, InfoBox _infoBox, HelpHandler _helpHandler) {
        ioHandler = _ioHandler;
        infoBox = _infoBox;
        helpHandler =_helpHandler;
    }
    
    public IOHandler getIOHandler() { return this.ioHandler; }
    
    public InfoBox getInfoBox() { return this.infoBox; }
    
    public HelpHandler getHelpHandler() { return this.helpHandler; }
    
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
}
