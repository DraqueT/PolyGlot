/*
 * Copyright (c) 2015-2019, Draque Thompson
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
package org.darisadesigns.polyglotlina.CustomControls;

import org.darisadesigns.polyglotlina.DictCore;
import java.awt.Font;
import javax.swing.JList;

/**
 *
 * @author draque
 * @param <N>
 */
public class PList<N> extends JList<N> {
    private DictCore core;
    private boolean isConFont;
    boolean ignoreRepaint = false;
    
    public PList(DictCore _core, boolean _isConFont) {        
        core = _core;
        isConFont = _isConFont;
    }
    
    public void setCore(DictCore _core) {
        core = _core;
    }

    @Override
    public void repaint() {
        if (ignoreRepaint) {
            return;
        }
        
        if (core != null) { // initial paint happens before initilization complete
            Font testFont = core.getPropertiesManager().getFontCon();
            ignoreRepaint = true;
            if (isConFont) {
                setFont(testFont);
            } else {
                setFont(core.getPropertiesManager().getFontLocal());
            }
            ignoreRepaint = false;
        }
        
        super.repaint(); 
    }
    
    public void setConFont(boolean _isConFont) {
        isConFont = _isConFont;
    }
}
