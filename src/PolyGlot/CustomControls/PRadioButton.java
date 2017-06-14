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
package PolyGlot.CustomControls;

import PolyGlot.DictCore;
import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.DictNode;
import PolyGlot.QuizEngine.QuizQuestion.QuestionType;
import javax.swing.JRadioButton;

/**
 * 
 * @author draque.thompson
 */
public class PRadioButton extends JRadioButton {
    private DictNode value = null;
    private QuestionType type;
    private final DictCore core;

    public PRadioButton(DictCore _core) {
        core = _core;
    }
    
    /**
     * @return the value
     */
    public DictNode getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(DictNode value) {
        this.value = value;
    }
    
    @Override
    public String getText() {
        String ret = super.getText();
        
        if (!(type == null || value == null)) {
            switch (type) {
                case ConEquiv:
                    setFont(core.getPropertiesManager().getFontCon());
                case PoS:
                case Classes:
                    ret = value.getValue();
                    break;
                case Local:
                    ret = ((ConWord)value).getLocalWord();
                    break;
                case Proc:
                    try {
                        ret = ((ConWord)value).getPronunciation();
                    } catch (Exception e) {
                        ret = "<ERROR>";
                    }
                    break;
                case Def:
                    ret = ((ConWord)value).getDefinition();
                    break;
                default:
                    ret = "UNHANDLED TYPE";
            }
        }
        
        return ret;
    }

    /**
     * @return the type
     */
    public QuestionType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(QuestionType type) {
        this.type = type;
    }
}
