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

import java.nio.file.Path;
import java.util.List;
import org.darisadesigns.polyglotlina.Nodes.LexiconProblemNode;

/**
 *
 * @author pe1uca
 */
public abstract class OSHandler {
    
    protected String overrideProgramPath = "";
    
    protected final IOHandler ioHandler;
    protected final InfoBox infoBox;
    protected final HelpHandler helpHandler;
    protected final PFontHandler fontHandler;
    
    protected CoreUpdatedListener coreUpdatedListener = null;
    protected FileReadListener fileReadListener = null;
    
    public OSHandler(IOHandler _ioHandler, InfoBox _infoBox, HelpHandler _helpHandler, PFontHandler _fontHandler) {
        ioHandler = _ioHandler;
        infoBox = _infoBox;
        helpHandler =_helpHandler;
        fontHandler = _fontHandler;
    }

    public abstract Path getConfigDirectory();

    public abstract Path getStateDirectory();
    
    public abstract void openLanguageProblemDisplay(List<LexiconProblemNode> problems, DictCore _core);
    
    public IOHandler getIOHandler() { return this.ioHandler; }
    
    public InfoBox getInfoBox() { return this.infoBox; }
    
    public HelpHandler getHelpHandler() { return this.helpHandler; }

    public PFontHandler getPFontHandler() { return this.fontHandler; }
    
    public void setCoreUpdatedListener(CoreUpdatedListener listener) {
        this.coreUpdatedListener = listener;
    }
    
    public CoreUpdatedListener getCoreUpdatedListener() {
        return this.coreUpdatedListener;
    }
    
    public void setFileReadListener(FileReadListener listener) {
        this.fileReadListener = listener;
    }
    
    public FileReadListener getFileReadListener() {
        return this.fileReadListener;
    }
    
    public abstract void openLanguageReport(String reportContents);
    
    public interface CoreUpdatedListener {
        void coreUpdated(DictCore core);
    }
    
    public interface FileReadListener {
        void fileRead(DictCore core);
    }
}
