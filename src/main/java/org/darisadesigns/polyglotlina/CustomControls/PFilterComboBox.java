/*
 * Copyright (c) 2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.CustomControls;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.Nodes.DictNode;
import org.darisadesigns.polyglotlina.Nodes.DummyNode;
import org.darisadesigns.polyglotlina.PGTUtil;

/**
 *
 * @author draque
 * @param <K>
 */
public class PFilterComboBox<K extends DictNode> extends PComboBox {
    private ComboBoxModel<K> unfilteredModel;
    private final DummyNode searchNode;
    
    public PFilterComboBox(Font font) {
        super(font);
        
        searchNode = new DummyNode();
        this.setEditable(true);
        
        final JTextField textField = (JTextField) this.getEditor().getEditorComponent();

        // on mouse selection, clear filter
        this.addActionListener((ActionEvent e) -> {
            int modifiers = e.getModifiers();

            // I would stop using deprecated masks if BASE JAVA STOPPED SUPPLYING THEM
            if ((modifiers & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK
                    || (modifiers & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
                try {
                    clearFilter();
                } catch (Exception ex) {
                    // fail silently here
                    IOHandler.writeErrorLog(ex);
                }
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                int keyCode = ke.getExtendedKeyCode();
                
                // exclude additional action if the keycode is from arrow keys
                if (keyCode >40 || keyCode < 37) {
                    SwingUtilities.invokeLater(() -> {
                        int caretPosition = textField.getCaretPosition();
                        String text = textField.getText();
                        comboFilter(text);
                        textField.setText(text);
                        textField.setCaretPosition(caretPosition);
                    });
                }
            }
        });

        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    clearFilter();
                }
                catch (Exception ex) {
                    IOHandler.writeErrorLog(ex);

                    // only inform of error if debugging
                    if (PGTUtil.isDebugMode()) {
                        InfoBox.error("Filter Box Error", "Unable to clear filter.", null);
                    }
                }
            }
        });
    }

    private List<K> getModelContents() {
        List<K> ret = new ArrayList<>();

        for (int i = 0; i < unfilteredModel.getSize(); i++) {
            ret.add(unfilteredModel.getElementAt(i));
        }

        return ret;
    }

    public void comboFilter(String enteredText) {
        List<DictNode> filterArray = new ArrayList<>();
        searchNode.setValue(enteredText);
        filterArray.add(searchNode);

        for (K curNode : this.getModelContents()) {
            // just ignore case by degault
            if (curNode.toString().toLowerCase().contains(enteredText.toLowerCase())) {
                filterArray.add(curNode);
            }
        }
        if (filterArray.size() > 0) {
            // jump to super to avoid losing ref to the unfiltered list
            super.setModel(new DefaultComboBoxModel(filterArray.toArray()));
            this.setSelectedItem(searchNode);
            this.showPopup();
        } else {
            this.hidePopup();
        }
    }

    /**
     * Clears the filter
     *
     * @throws java.lang.Exception
     */
    public void clearFilter() throws Exception {
        Object selectionObject = getModel().getSelectedItem();
        K selection = null;
        
        // always set model before failurepoint
        setModel(unfilteredModel);

        if (selectionObject instanceof DictNode) {
            selection = (K) selectionObject;
        } else {
            if (selectionObject != null
                    && !(selectionObject instanceof String)) {
                throw new Exception("Unexpected object type");
            }

            selection = matchStringToNode((String) selectionObject);
        }

        // do NOT do a .equals selection here. We are testing IDENTITY.
        selection = selection == searchNode ? null : selection;
        
        setSelectedItem(selection);
    }

    /**
     * Returns first string match of value typed by user and returns object.
     * Returns null if no match
     *
     * @param match
     * @return
     */
    private K matchStringToNode(String match) {
        K ret = null;

        for (int i = 0; i < unfilteredModel.getSize(); i++) {
            Object curObject = unfilteredModel.getElementAt(i);

            if (curObject.toString().equals(match)) {
                ret = (K) curObject;
                break;
            }
        }

        return ret;
    }

    @Override
    /**
     * Caches model before passing up to superclass
     */
    public void setModel(ComboBoxModel model) {
        unfilteredModel = model;
        super.setModel(model);
    }

    @Override
    /**
     * Returns cached model to avoid possibility of filtered model being
     * consumed
     */
    public ComboBoxModel getModel() {
        return unfilteredModel;
    }

    @Override
    /**
     * Never returns search value, only nodes of type K
     */
    public Object getSelectedItem() {
        Object curSelection = super.getSelectedItem();
        return curSelection == searchNode ? null : curSelection;
    }
}
