/*
 * Copyright (c) 2014, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 * See LICENSE.TXT included with this code to read the full license agreement.
 *
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

package PolyGlot;

import java.awt.Component;
import java.awt.Font;
import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author draque
 */
public class TableColumnEditor extends AbstractCellEditor implements TableCellEditor {

    JComponent component = new JTextField();
    Font myFont;
    DocumentListener docListener;

    public void setDocuListener(DocumentListener _listener) {
        docListener = _listener;
    }
    
    public TableColumnEditor(Font _myFont) {
        myFont = _myFont;
    }

    public Component tableColumnEditor(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
        ((JTextField) component).setText((String) value);
        ((JTextField) component).setFont(myFont);
        ((JTextField) component).getDocument().addDocumentListener(docListener);
        
        return component;
    }

    // This method is called when a cell value is edited by the user.
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int rowIndex, int vColIndex) {

        ((JTextField) component).setText((String) value);
        ((JTextField) component).setFont(myFont);
        ((JTextField) component).getDocument().addDocumentListener(docListener);

        return component;
    }

    // This method is called when editing is completed.
    // It must return the new value to be stored in the cell.
    @Override
    public Object getCellEditorValue() {
        ((JTextField) component).setFont(myFont);
        ((JTextField) component).getDocument().addDocumentListener(docListener);
        
        return ((JTextField) component).getText();
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        ((JTextField) component).setFont(myFont);
        ((JTextField) component).getDocument().addDocumentListener(docListener);
        
        return super.clone();
    }
}
