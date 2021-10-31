/*
 * Copyright (c) 2017-2020, Draque Thompson, draquemail@gmail.com
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

import java.awt.Color;
import java.awt.Component;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import java.awt.Font;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;

/**
 *
 * @author DThompson
 */
public final class PTable extends JTable {
    private final DictCore core;
    private final PCellEditor disabledEd;
    private final PCellRenderer disabledRend;
    private RenderController rendererController = (r,x,y)->{};
    private EditorController editorController = (e,x,y)->{};

    public PTable(DictCore _core) {
        core = _core;
        disabledEd = new PCellEditor(false, core);
        disabledRend = new PCellRenderer(false, core);
        disabledEd.setBackground(Color.gray);
        disabledRend.setBackground(Color.darkGray);

        if (core != null) {
            Font font = PGTUtil.MENU_FONT.deriveFont((float) PolyGlot.getPolyGlot().getOptionsManager().getMenuFontSize());
            this.getTableHeader().setFont(font);
        }
    }
    
        @Override
    public String getToolTipText(MouseEvent e) {
        String tip = "";
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);

        try {
            Object target = this.getValueAt(rowIndex, colIndex);

            if (target != null) {
                try {
                    tip = core.getPronunciationMgr().getPronunciation(target.toString());
                }
                catch (Exception ex) {
                    // user error: do not log
                    // IOHandler.writeErrorLog(e);
                    tip = "MALFORMED PRONUNCIATION REGEX: " + ex.getLocalizedMessage();
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            // do nothing. This happens if a non-value portion of the table is hovered over due to how objects are fetched
            // all other errors bubble beyond this point
            // IOHandler.writeErrorLog(e);
        }

        return tip.isEmpty() ? super.getToolTipText(e) : tip;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        TableCellRenderer ret = disabledRend;

        if (this.getValueAt(row, column) != null) {
            ret = super.getCellRenderer(row, column);
            
            // only set new value if not already overridden elsewhere
            if (!(ret instanceof PCellRenderer)) {
                boolean conFont = this.getFont().getFamily().equals(core.getPropertiesManager().getFontLocalFamily());
                ret = new PCellRenderer(conFont, core);
                this.prepareRenderer(ret, row, column);
            }
        }

        return ret;
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        TableCellEditor ret = cellEditor;

        if (this.getValueAt(row, column) != null) {
            ret = super.getCellEditor(row, column);
            
            // only set new value if not already overridden elsewhere
            if (!(ret instanceof PCellEditor)) {
                boolean conFont = this.getFont().getFamily().equals(core.getPropertiesManager().getFontConFamily());
                ret = new PCellEditor(conFont, core);
                this.prepareEditor(ret, row, column);
            }
        }

        return ret;
    }
    
    /**
     * Sets row renderer for table (allows for conditional rendering of particular rows, etc.)
     * @param _rendererController   
     */
    public void setRenderController(RenderController _rendererController) {
        rendererController = _rendererController;
    }
    
    public void setEditorController(EditorController _editorController) {
        editorController = _editorController;
    }
    
    public interface RenderController {
        public void r(PCellRenderer renderer, int row, int column);
    }
    
    public interface EditorController {
        public void e(PCellEditor editor, int row, int column);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component ret = super.prepareRenderer(renderer, row, column);
        if (renderer instanceof PCellRenderer) {
            rendererController.r((PCellRenderer)renderer, row, column);
        } else {
            // The only time this will ever happen is if it is done manually via setting renderer to the column/cell
            new DesktopInfoBox(null).error("Table Rendering Error", "Unable to render table cell.");
            DesktopIOHandler.getInstance().writeErrorLog(new Exception("Non PCellRenderer in PTable"));
        }
        
        return ret;
    }
    
    @Override
    public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component ret = super.prepareEditor(editor, row, column);
        
        if (editor instanceof TableCellEditor) {
            editorController.e((PCellEditor)editor, row, column);
        } else {
            // The only time this will ever happen is if it is done manually via setting editor to the column/cell
            new DesktopInfoBox(null).error("Table Rendering Error", "Unable to render table cell.");
            DesktopIOHandler.getInstance().writeErrorLog(new Exception("Non PCellEditor in PTable"));
        }
        
        return ret;
    }
}
