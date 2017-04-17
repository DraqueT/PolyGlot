/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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
package PolyGlot.CustomControls;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author draque
 */
public class PCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JComponent component = new JTextField();
    Font myFont;
    DocumentListener docListener;
    private boolean ignoreListenerSilenceing = false;

    public void setDocuListener(DocumentListener _listener) {
        docListener = _listener;

        ((JTextField) component).getDocument().addDocumentListener(docListener);
    }

    public PCellEditor(Font _myFont) {
        myFont = _myFont;
        JTextField setupText = (JTextField) component;

        setupRightClickMenu(setupText);

        setupText.setBorder(BorderFactory.createEmptyBorder());
    }

    public Component tableColumnEditor(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
        ((JTextField) component).setText((String) value);
        ((JTextField) component).setFont(myFont);

        return component;
    }

    // This method is called when a cell value is edited by the user.
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int rowIndex, int vColIndex) {
        JTextField curComp = (JTextField) component;

        setValue(curComp, (String) value);

        curComp.setFont(myFont);

        return component;
    }

    public void setValue(String value) {
        setValue((JTextField) component, value);
    }

    private void setValue(JTextField curComp, String value) {
        if (ignoreListenerSilenceing) {
            curComp.setText((String) value);
        } else {
            curComp.getDocument().removeDocumentListener(docListener);
            curComp.setText((String) value);
            curComp.getDocument().addDocumentListener(docListener);
        }
    }

    /**
     * Allows user to set initial value (helps avoid unnecessary listener firing
     * later
     *
     * @param value The value to set.
     */
    public void setInitialValue(String value) {
        JTextField curComp = (JTextField) component;

        curComp.setText(value);
    }

    // This method is called when editing is completed.
    // It must return the new value to be stored in the cell.
    @Override
    public Object getCellEditorValue() {
        ((JTextField) component).setFont(myFont);

        return ((JTextField) component).getText();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ((JTextField) component).setFont(myFont);

        return super.clone();
    }

    private void setupRightClickMenu(JTextField editor) {
        final JPopupMenu ruleMenu = new JPopupMenu();
        final JMenuItem cut = new JMenuItem("Cut");
        final JMenuItem copy = new JMenuItem("Copy");
        final JMenuItem paste = new JMenuItem("Paste");
        final JTextField parentField = editor;

        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                parentField.cut();
            }
        });
        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                parentField.copy();
            }
        });
        paste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                parentField.paste();
            }
        });

        ruleMenu.add(cut);
        ruleMenu.add(copy);
        ruleMenu.add(paste);

        editor.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && parentField.isEnabled()) {
                    cut.setEnabled(true);
                    copy.setEnabled(true);
                    paste.setEnabled(true);
                    ruleMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && parentField.isEnabled()) {
                    cut.setEnabled(true);
                    copy.setEnabled(true);
                    paste.setEnabled(true);
                    ruleMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * @return the ignoreListenerSilenceing
     */
    public boolean isIgnoreListenerSilenceing() {
        return ignoreListenerSilenceing;
    }

    /**
     * @param ignoreListenerSilenceing the ignoreListenerSilenceing to set
     */
    public void setIgnoreListenerSilenceing(boolean ignoreListenerSilenceing) {
        this.ignoreListenerSilenceing = ignoreListenerSilenceing;
    }
}
