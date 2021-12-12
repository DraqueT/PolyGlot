/*
 * Copyright (c) 2016-2021, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.Nodes.ConWord;

/**
 *
 * @author draque
 * @param <E> Type to display
 */
public class PComboBox<E> extends JComboBox<E> implements MouseListener {
    private SwingWorker worker = null;
    private boolean mouseOver = false;
    private String defaultText = "";
    private List<E> baseObjects;
    private boolean filterActive = false;
    private Object lastSetValue = null;

    public PComboBox(Font font) {
        doSetup(font, "");
    }
    
    /**
     * If default text is set, the first entry will be used as default (you are
     * responsible for inserting an appropriately blank entry of type E)
     * @param font
     * @param _defaultText default selection text (no value)
     */
    public PComboBox(Font font, String _defaultText) {
        doSetup(font, _defaultText);
    }
    
    private void doSetup(Font font, String _defaultText) {
        setupListeners();
        super.setFont(font);
        var cellRenderer = new PListCellRenderer();
        cellRenderer.setAddLocalExtraText(PolyGlot.getPolyGlot().getCore().getPropertiesManager().isExpandedLexListDisplay());
        cellRenderer.setFont(font);
        this.setRenderer(cellRenderer);
        ((JTextField)this.getEditor().getEditorComponent()).setHorizontalAlignment(JTextField.CENTER);
        defaultText = _defaultText;
    }
    
    private void refreshFromDataModel(ComboBoxModel<E> model) {
        if (filterActive) {
            return; // never refresh this value during a filter
        }
        
        baseObjects.clear();
        
        for (int i = 0; i < model.getSize(); i++) {
            baseObjects.add(model.getElementAt(i));
        }
    }
    
    @Override
    public void setModel(final ComboBoxModel<E> model) {
        super.setModel(model);
        
        model.addListDataListener(new ListDataListener(){
            @Override
            public void intervalAdded(ListDataEvent e) {
                refreshFromDataModel(model);
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                refreshFromDataModel(model);
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                refreshFromDataModel(model);
            }
        });
        
        if (baseObjects == null) {
            baseObjects = new ArrayList<>();
        }
        
        refreshFromDataModel(model);
    }
    
    @Override
    public void addItem(E item) {
        super.addItem(item);
        baseObjects.add(item);
    }
    
    @Override
    public void removeItemAt(int itemIndex) {
        super.removeItemAt(itemIndex);
        baseObjects.remove(itemIndex);
    }
    
    @Override
    public void removeItem(Object item) {
        super.removeItem(item);
        baseObjects.remove((E)item);
    }
    
    @Override
    public void removeAllItems() {
        super.removeAllItems();
        baseObjects.clear();
    }
    
    private void comboFilter(String filter, String ignoreText) {
        List<E> filterArray = new ArrayList<>();
        var textfield = (JTextField) this.getEditor().getEditorComponent();
        var curText = textfield.getText();
        var caretPosition = textfield.getCaretPosition();
        
        if (!filter.isBlank() && !filter.equals(ignoreText) && !filter.equals(defaultText)) {
            filterActive = true;
            for (var item : baseObjects) {
                if (item.toString().startsWith(filter)) {
                    filterArray.add(item);
                }
            }
        } else {
            filterActive = false;
            filterArray = baseObjects;
        }
        
        setModel(new DefaultComboBoxModel(filterArray.toArray()));
        textfield.setText(curText);
        textfield.setCaretPosition(caretPosition);
        
        if (filterArray.size() > 0) {
            showPopup();
        } else {
            hidePopup();
        }
    }

    /**
     * makes this component flash. If already flashing, does nothing.
     * @param _flashColor color to flash
     * @param isBack whether display color is background (rather than foreground)
     */
    public void makeFlash(Color _flashColor, boolean isBack) {
        if (worker == null || worker.isDone() && ! this.isFocusOwner()) {
            worker = PGTUtil.getFlashWorker(this, _flashColor, isBack);
            worker.execute();
        }
    }

    @Override
    public void setSelectedIndex(int i) {
        super.setSelectedIndex(i);
        lastSetValue = getItemAt(i);
    }
    
    @Override
    public void setSelectedItem(Object o) {
        super.setSelectedItem(o);
        lastSetValue = o;
    }
    
    private void setupListeners() {
        final JTextField textField = (JTextField) this.getEditor().getEditorComponent();
        final PComboBox self = this;

        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                self.setEditable(true);
                if (lastSetValue != null) {
                    textField.setText(lastSetValue.toString());
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // do nothing
            }
        });
        
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                self.showPopup();
            }

            @Override
            public void focusLost(FocusEvent e) {
                self.setEditable(false);
                var text = textField.getText();
                var priorSelection = self.getSelectedItem();
                
                for (var i = 0; i < self.getItemCount(); i++) {
                    var item = self.getItemAt(i);
                    if (text.equals(item.toString())) {
                        self.setSelectedItem(item);
                        return;
                    }
                }
                
                if (priorSelection != null) {
                    textField.setText(priorSelection.toString());
                } else {
                    textField.setText("");
                    comboFilter("", defaultText);
                    hidePopup();
                }
                
                self.setSelectedItem(priorSelection);
            }
        });
        
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                // no arrow keys are only selection events
                var keyCode = ke.getKeyCode();
                if ((keyCode & KeyEvent.VK_DOWN) == KeyEvent.VK_DOWN 
                        || (keyCode & KeyEvent.VK_UP) == KeyEvent.VK_UP
                        || (keyCode & KeyEvent.VK_RIGHT) == KeyEvent.VK_RIGHT
                        || (keyCode & KeyEvent.VK_LEFT) == KeyEvent.VK_LEFT) {
                    return;
                }
                final var priorSelection = self.getSelectedItem();
                SwingUtilities.invokeLater(() -> {
                    var priorString = priorSelection == null ? "" : priorSelection.toString();
                    comboFilter(textField.getText(), priorString);
                });
            }
        });
        
        addMouseListener(this);
    }
    
    public boolean isDefaultValue() {
        return this.getSelectedIndex() == 0 && !defaultText.isEmpty();
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
        
        // draw text
        if (enabled) {
            antiAlias.setColor(Color.black);
        } else {
            antiAlias.setColor(Color.decode("#909090"));
        } 
        Object selectedItem = getSelectedItem();
        String text = selectedItem == null ? "" : selectedItem.toString();
        
        Font tmpFont = this.getFont();
        Font defaultMenuFont = tmpFont;
        
        // display default text if appropriate
        if (text.isBlank() && this.getSelectedIndex() == 0) {
            text = getDefaultText();
            antiAlias.setColor(Color.decode("#909090"));
            defaultMenuFont = PGTUtil.MENU_FONT.deriveFont((float)PolyGlot.getPolyGlot().getOptionsManager().getMenuFontSize());
        }
        
        if (!text.isEmpty()) { // 0 length text makes bounding box explode
            FontMetrics fm = antiAlias.getFontMetrics(defaultMenuFont);
            antiAlias.setFont(defaultMenuFont);
            Rectangle2D rec = fm.getStringBounds(text, antiAlias);
            int stringW = (int) Math.round(rec.getWidth());
            int stringH = (int) Math.round(rec.getHeight());
            antiAlias.drawChars(text.toCharArray(), 0, text.length(), ((getWidth() - buttonWidth)/2) 
                    - (stringW/2), (getHeight() - 9)/2 + stringH/2);
        }
        
        antiAlias.setFont(tmpFont);
        this.setFont(tmpFont);
        
        if (enabled) {
            antiAlias.setColor(PGTUtil.COLOR_ENABLED_BG);
        } else {
            antiAlias.setColor(Color.decode("#d0d0d0"));
        }
        antiAlias.fillRect(getWidth() - buttonWidth, 1, buttonWidth, getHeight() - 1);
        
        // draw outline
        if ((mouseOver || this.hasFocus()) && enabled) {
            antiAlias.setColor(Color.black);
        } else 
        {
            antiAlias.setColor(Color.lightGray);
        }
        antiAlias.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 5, 5);
        
        // draw local word if appropriate
        var core = PolyGlot.getPolyGlot().getCore();
        if (!filterActive && core.getPropertiesManager().isExpandedLexListDisplay()
                && getSelectedItem() instanceof ConWord word) {
            Font localFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
            Font conFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon();
            FontMetrics localMetrics = g.getFontMetrics(localFont);
            FontMetrics conMetrics = g.getFontMetrics(conFont);
            
            var printValue = word.getLocalWord();
            var drawPosition = (conMetrics.stringWidth(word.getValue()) / 2) 
                    + (getWidth()/2);
            g.setFont(localFont);
            
            if (!printValue.isBlank()) {
                g.setColor(Color.blue);
                g.drawLine(drawPosition + 10, 5, drawPosition + 10, conMetrics.getHeight());
                g.setColor(Color.darkGray);
                g.drawString(printValue, drawPosition + 15, localMetrics.getHeight());
            }
        }
        
        // draw default text if appropriate
        if (!defaultText.isBlank() && !isDefaultValue()) {
            var localFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
            var thisFont = getFont();
            var localMetrics = g.getFontMetrics(localFont);
            var conMetrics = g.getFontMetrics(thisFont);
            var drawPosition = (getWidth() / 2) 
                    - (conMetrics.stringWidth(text) / 2)
                    - localMetrics.stringWidth(defaultText)
                    - 10;
            
            g.setFont(localFont);
            g.setColor(Color.lightGray);
            
            g.drawString(defaultText, drawPosition - 10, localMetrics.getHeight());
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
        comboFilter("", defaultText);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        comboFilter("", defaultText);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // do nothing
    }

    public String getDefaultText() {
        return defaultText;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }
}
