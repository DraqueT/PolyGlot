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

import java.io.File;
import org.darisadesigns.polyglotlina.HelpHandler;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.InfoBox;
import org.darisadesigns.polyglotlina.OSHandler;
import org.darisadesigns.polyglotlina.PGTUtil;

/**
 *
 * @author pe1uca
 */
public class DesktopOSHandler extends OSHandler {
    
    public DesktopOSHandler(IOHandler _ioHandler, InfoBox _infoBox, HelpHandler _helpHandler) {
        super(_ioHandler, _infoBox, _helpHandler);
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
}
