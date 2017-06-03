/*
 * Copyright (c) 2017, DThompson
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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * This is a filter to apply to all text fields that should only accept double
 * values
 * @author DThompson
 */
public class PTextFieldFilter extends DocumentFilter {
    
    private final DictCore core;
    private final FilterType type;
    
    public PTextFieldFilter(DictCore _core, FilterType _type) {
        core = _core;
        type = _type;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string,
            AttributeSet attr) throws BadLocationException {

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.insert(offset, string);

        if (test(sb.toString())) {
            super.insertString(fb, offset, string, attr);
        } else {
            InfoBox.warning("Numbers Only", "This field allows only numeric values.", 
                    core.getRootWindow());
        }
    }

    private boolean test(String text) {
        boolean ret = true;
        
        // blank must always be allowed
        if (!text.equals("")) {
            try {
                Double.parseDouble(text);
            } catch (NumberFormatException e) {
                ret = false;
            }
        }
        
        return ret;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text,
            AttributeSet attrs) throws BadLocationException {

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.replace(offset, offset + length, text);

        if (test(sb.toString())) {
            super.replace(fb, offset, length, text, attrs);
        } else {
            InfoBox.warning("Numbers Only", "This field allows only numeric values.", 
                    core.getRootWindow());
        }

    }

    @Override
    public void remove(FilterBypass fb, int offset, int length)
            throws BadLocationException {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.delete(offset, offset + length);

        if (test(sb.toString())) {
            super.remove(fb, offset, length);
        } else {
            InfoBox.warning("Numbers Only", "This field allows only numeric values.", 
                    core.getRootWindow());
        }
    }
    
    public enum FilterType {
        Double, Int
    }
}
