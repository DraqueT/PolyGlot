/*
 * Copyright (c) 2016-2022, Draque Thompson
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
package org.darisadesigns.polyglotlina.Desktop.CustomControls;

import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.DictNode;
import org.darisadesigns.polyglotlina.QuizEngine.QuizQuestion.QuestionType;
import javax.swing.JRadioButton;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;

/**
 * 
 * @author draque.thompson
 */
public final class PRadioButton extends JRadioButton {
    private DictNode value = null;
    private QuestionType type;
    private final DictCore core;
    
    public PRadioButton() {
        core = PolyGlot.getPolyGlot().getCore();
        this.setFont(PGTUtil.MENU_FONT);
    }
    
    /**
     * @return the value
     */
    public DictNode getValue() {
        return value;
    }

    /**
     * @param _value the value to set
     */
    public void setValue(DictNode _value) {
        this.value = _value;
    }
    
    @Override
    public String getText() {
        String ret = super.getText();
        
        if (!(type == null || value == null)) {
            switch (type) {
                case ConEquiv, PoS, Classes -> ret = value.getValue();
                case Local -> ret = ((ConWord)value).getLocalWord();
                case Proc -> {
                    try {
                        ret = ((ConWord)value).getPronunciation();
                    } catch (Exception e) {
                        DesktopIOHandler.getInstance().writeErrorLog(e);
                        ret = "<ERROR>";
                    }
                }
                case Def -> ret = ((ConWord)value).getDefinition();
                default -> ret = "UNHANDLED TYPE";
            }
        }
        
        return ret;
    }

    /**
     * @param _type the type to set
     */
    public void setType(QuestionType _type) {
        if (_type == QuestionType.ConEquiv) {
            this.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon());
        } else {
            this.setFont(PGTUtil.CHARIS_UNICODE);
        }
        
        this.type = _type;
    }
}
