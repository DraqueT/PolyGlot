/*
 * Copyright (c) 2019, Draque Thompson
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

import org.darisadesigns.polyglotlina.PGTUtil;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.accessibility.Accessible;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.ComboPopup;

public class PCheckedComboBox<E extends PCheckableItem> extends JComboBox<E> implements MouseListener {

    private boolean keepOpen;
    private transient ActionListener listener;
    private final boolean nightMode;
    private final double fontSize;
    private boolean mouseOver = false;

    public PCheckedComboBox(boolean _nightMode, double _fontSize) {
        super();
        nightMode = _nightMode;
        fontSize = _fontSize;
    }

    public PCheckedComboBox(ComboBoxModel<E> model, boolean _nightMode, double _fontSize) {
        super(model);
        nightMode = _nightMode;
        fontSize = _fontSize;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 20);
    }

    @Override
    public void updateUI() {
        setRenderer(null);
        removeActionListener(listener);
        super.updateUI();
        listener = e -> {
            if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
                updateItem(getSelectedIndex());
                keepOpen = true;
            }
        };
        setRenderer(new PCheckBoxCellRenderer<>(nightMode, fontSize));
        addActionListener(listener);
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select");
        getActionMap().put("checkbox-select", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Accessible a = getAccessibleContext().getAccessibleChild(0);
                if (a instanceof ComboPopup) {
                    updateItem(((ComboPopup) a).getList().getSelectedIndex());
                }
            }
        });
    }

    protected void updateItem(int index) {
        if (isPopupVisible()) {
            E item = getItemAt(index);
            item.setSelected(!item.isSelected());
            setSelectedIndex(-1);
            setSelectedItem(item);
        }
    }

    @Override
    public void setPopupVisible(boolean v) {
        if (keepOpen) {
            keepOpen = false;
        } else {
            super.setPopupVisible(v);
        }
    }
    
    @Override
    public void setModel(ComboBoxModel<E> model) {
        if (model instanceof PDefaultComboBoxModel) {
            super.setModel(model);
        } else {
            throw new IllegalArgumentException("PCheckComboBox requires a PDefaultComboBoxModel model.");
        }    
    }
    
    /**
     * Gets all selected values from model
     * @return 
     */
    public PCheckableItem[] getCheckedValues() {
        List<PCheckableItem> ret = new ArrayList<>();
        PDefaultComboBoxModel myModel = (PDefaultComboBoxModel)dataModel;
        
        for (Object value : myModel.identityMap.values()) {
            if (value instanceof PCheckableItem) {
                PCheckableItem checkValue = (PCheckableItem) value;
                
                if (checkValue.isSelected()) {
                    ret.add(checkValue);
                }
            }
        }
        
        return ret.toArray(new PCheckableItem[0]);
    }
    
    /**
     * Gets all values from model
     * @return 
     */
    public PCheckableItem[] getAllValues() {
        List<PCheckableItem> ret = new ArrayList<>();
        PDefaultComboBoxModel myModel = (PDefaultComboBoxModel)dataModel;
        
        for (Object value : myModel.identityMap.values()) {
            if (value instanceof PCheckableItem) {
                ret.add((PCheckableItem) value);
            }
        }
        
        return ret.toArray(new PCheckableItem[0]);
    }
    
    /**
     * Returns PCheckableItem value by its identity if it exists.Null otherwise
     * @param identity
     * @return 
     */
    public PCheckableItem getValueByIdentity(String identity) {
        PCheckableItem ret = null;
        PDefaultComboBoxModel myModel = (PDefaultComboBoxModel)dataModel;
        
        if (myModel.identityMap.containsKey(identity)) {
            Object obj = myModel.identityMap.get(identity);
            
            if (obj instanceof PCheckableItem) {
                ret = (PCheckableItem)obj;
            }
        }
        
        return ret;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        final int buttonWidth = 20;
        boolean enabled = this.isEnabled();
        
        // turn on anti-alias mode
        Graphics2D antiAlias = (Graphics2D) g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
        if (enabled) {
            antiAlias.setColor(Color.white);
        } else {
            antiAlias.setColor(Color.decode("#e0e0e4"));
        }
        
        antiAlias.fillRoundRect(1, 1, getWidth(), getHeight() - 2, 5, 5);
        
        if (enabled) {
            antiAlias.setColor(PGTUtil.COLOR_ENABLED_BG);
        } else {
            antiAlias.setColor(Color.decode("#d0d0d0"));
        }
        antiAlias.fillRect(getWidth() - buttonWidth, 1, buttonWidth, getHeight() - 1);
        
        if ((mouseOver || this.hasFocus()) && enabled) {
            antiAlias.setColor(Color.black);
        } else 
        {
            antiAlias.setColor(Color.lightGray);
        }
        antiAlias.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 5, 5);
        
        if (enabled) {
            antiAlias.setColor(Color.black);
        } else {
            antiAlias.setColor(Color.decode("#909090"));
        }
        
        String text = getCheckedItemString();
        if (!text.isEmpty()) { // 0 length text makes bounding box explode
            FontMetrics fm = antiAlias.getFontMetrics(getFont());
            Rectangle2D rec = fm.getStringBounds(text, antiAlias);
            int stringW = (int) Math.round(rec.getWidth());
            int stringH = (int) Math.round(rec.getHeight());
            antiAlias.drawChars(text.toCharArray(), 0, text.length(), ((getWidth() - buttonWidth)/2) 
                    - (stringW/2), (getHeight() - 9)/2 + stringH/2);
        }
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        mouseOver = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseOver = false;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // do nothing
    }
    
    private String getCheckedItemString() {
        String ret = "";
        
        for (PCheckableItem item : this.getCheckedValues()) {
            ret += item.text + " ";
        }
        
        return ret.isEmpty() ? "Select Values" : ret;
    }
}