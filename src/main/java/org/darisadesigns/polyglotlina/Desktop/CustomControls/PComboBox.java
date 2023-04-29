/*
 * Copyright (c) 2016-2023, Draque Thompson, draquemail@gmail.com
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
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;

/**
 *
 * @author draque
 * @param <E> Type to display
 */
public class PComboBox<E> extends JComboBox<E> implements MouseListener {
    private FontMetrics menuFontMetrics;
    private FontMetrics displayFontMetrics;
    private FontMetrics localFontMetrics;
    private FontMetrics conFontMetrics;
    private boolean mouseOver = false;
    private String defaultText;
    private List<E> baseObjects;
    private boolean filterActive = false;
    private Object lastSetValue = null;
    private final Font font;
    private final DictCore core;

    public PComboBox(boolean useConFont, DictCore _core) {
        this(useConFont ?
                ((DesktopPropertiesManager)_core.getPropertiesManager()).getFontCon() :
                ((DesktopPropertiesManager)_core.getPropertiesManager()).getFontLocal(),
                _core
        );
        
        this.setBackground(PolyGlot.getPolyGlot().getOptionsManager().isNightMode() ?
                PGTUtil.COLOR_TEXT_BG_NIGHT : PGTUtil.COLOR_TEXT_BG);
    }
    
    public PComboBox(Font _font, DictCore _core) {
        this(_font, "", _core);
    }
    
    /**
     * If default text is set, the first entry will be used as default (you are
     * responsible for inserting an appropriately blank entry of type E)
     * @param _font
     * @param _defaultText default selection text (no value)
     * @param _core
     */
    public PComboBox(Font _font, String _defaultText, DictCore _core) {
        font = _font;
        core = _core;
        setupListeners();
        super.setFont(font);
        var cellRenderer = new PListCellRenderer(core);
        cellRenderer.setAddLocalExtraText(core.getPropertiesManager().isExpandedLexListDisplay());
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
        
        if (!filterArray.isEmpty() && this.isEnabled()) {
            showPopup();
        } else {
            hidePopup();
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
                if (self.isEnabled()) {
                    self.setEditable(true);
                    if (lastSetValue != null) {
                        textField.setText(lastSetValue.toString());
                    }
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
                if (self.isEnabled()) {
                    self.showPopup();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (self.isEnabled()) {
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
    }
    
    public boolean isDefaultValue() {
        return this.getSelectedIndex() == 0 && !defaultText.isEmpty();
    }
    
    @Override
    public void paint(Graphics g) {
        Graphics2D antiAlias = (Graphics2D) g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        super.paint(g);
        
        // paint selection box no right of dropdown menu
        if (this.isEnabled()) {
            if (this.isPopupVisible()) {
                antiAlias.setColor(PGTUtil.COLOR_SELECTED_BG);
            } else {
                antiAlias.setColor(PGTUtil.COLOR_ENABLED_BG);
            }
        } else {
            antiAlias.setColor(PGTUtil.COLOR_COMBOBOX_DISABLED);
        }
        antiAlias.fillRect(getWidth() - 20, 1, 20, getHeight() - 1);
        
        // paint down arrow
        Path2D myPath = new Path2D.Double();
        myPath.moveTo(getWidth() - 15, 12);
        myPath.lineTo(getWidth() - 5, 12);
        myPath.lineTo(getWidth() - 10, getHeight() - 12);
        myPath.closePath();
        antiAlias.setColor(PGTUtil.COLOR_TEXT);
        antiAlias.fill(myPath);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        final int buttonWidth = 20;
        boolean enabled = this.isEnabled();
        
        // turn on anti-alias mode
        Graphics2D antiAlias = (Graphics2D) g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        populateFontMetrics(antiAlias);
                
        if (enabled) {
            antiAlias.setColor(getBackground());
        } else {
            antiAlias.setColor(PGTUtil.COLOR_COMBOBOX_DISABLED_BG);
        }
        antiAlias.fillRoundRect(1, 1, getWidth(), getHeight() - 2, 5, 5);
        
        // draw text
        if (enabled) {
            antiAlias.setColor(PGTUtil.COLOR_TEXT); 
        } else {
            antiAlias.setColor(PGTUtil.COLOR_COMBOBOX_DISABLED_TEXT);
        } 
        Object selectedItem = getSelectedItem();
        String text = selectedItem == null ? "" : selectedItem.toString();
        
        Font tmpFont = this.getFont();
        Font defaultMenuFont = tmpFont;
        FontMetrics fm  = localFontMetrics;
        
        // display default text if appropriate
        if ((text.isBlank() && this.getSelectedIndex() == 0) || text.equals(defaultText)) {
            text = getDefaultText();
            antiAlias.setColor(PGTUtil.COLOR_TEXT_DISABLED);
            defaultMenuFont = PGTUtil.MENU_FONT;
            fm = menuFontMetrics;
        }
        
        if (!text.isEmpty()) { // 0 length text makes bounding box explode
            antiAlias.setFont(defaultMenuFont);
            Rectangle2D rec = fm.getStringBounds(text, antiAlias);
            int stringW = (int) Math.round(rec.getWidth());
            int stringH = (int) Math.round(rec.getHeight());
            antiAlias.drawChars(text.toCharArray(), 0, text.length(), ((getWidth() - buttonWidth)/2) 
                    - (stringW/2), (getHeight() - 9)/2 + stringH/2);
        }
        
        antiAlias.setFont(tmpFont);
        this.setFont(tmpFont);
        
        // draw outline
        if ((mouseOver || this.hasFocus()) && enabled) {
            antiAlias.setColor(PGTUtil.COLOR_COMBOBOX_OUTLINE);
        } else 
        {
            antiAlias.setColor(PGTUtil.COLOR_COMBOBOX_DISABLED_OUTLINE);
        }
        antiAlias.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 5, 5);
        
        // draw local word if appropriate
        if (!filterActive && core.getPropertiesManager().isExpandedLexListDisplay()
                && getSelectedItem() instanceof ConWord word) {
            
            var printValue = word.getLocalWord();
            var drawPosition = (conFontMetrics.stringWidth(word.getValue()) / 2) 
                    + (getWidth()/2);
            g.setFont(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal());
            
            if (!printValue.isBlank()) {
                g.setColor(PGTUtil.COLOR_COMBOBOX_LOCAL_TEXT_LINE);
                g.drawLine(drawPosition + 10, 5, drawPosition + 10, conFontMetrics.getHeight());
                g.setColor(PGTUtil.COLOR_COMBOBOX_LOCAL_TEXT);
                g.drawString(printValue, drawPosition + 15, localFontMetrics.getHeight());
            }
        }
        
        // draw default text if appropriate
        if (!defaultText.isBlank() && !isDefaultValue() && text.isBlank()) {
            var drawPosition = (getWidth() / 2) 
                    - menuFontMetrics.stringWidth(defaultText)
                    - 10;
            
            g.setFont(PGTUtil.MENU_FONT);
            g.setColor(PGTUtil.COLOR_DEFAULT_TEXT);
            
            g.drawString(defaultText, drawPosition - 10, localFontMetrics.getHeight());
        }
    }
    
    private void populateFontMetrics(Graphics g) {
        if (menuFontMetrics == null) {
            menuFontMetrics = g.getFontMetrics(PGTUtil.MENU_FONT);
        }
        
        if (displayFontMetrics == null) {
            displayFontMetrics = g.getFontMetrics(this.getFont());
        }
        
        if (localFontMetrics == null) {
            var localFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
            localFontMetrics = g.getFontMetrics(localFont);
        }
        
        if (conFontMetrics == null) {
            var conFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon();
            conFontMetrics = g.getFontMetrics(conFont);
        }
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        displayFontMetrics = null;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
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

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseOver = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseOver = false;
    }
}
