/*
 * Copyright (c) 2018-2020, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;

/**
 *
 * @author DThompson
 */
public final class PDeclensionListPanel extends JPanel implements PDeclensionPanelInterface {
    private final Map<String, Integer> decIdsToListLocation = new HashMap<>();
    private final List<ConjugationPair> declensionPairs;
    private final DictCore core;
    private final ConWord word;
    private final boolean onlyTab;
    private final PTable table;
    private final ConjugationManager decMan;
    private boolean autoPopulated = false;
    
    /**
     * Instantiates declension list tab (grid with only one column)
     * @param _declensionPairs All combined IDs to include in this display
     * @param _core dict core
     * @param _word word for which forms are being generated
     * @param _onlyTab true if this is the only tab being displayed
     */
    public PDeclensionListPanel(ConjugationPair[] _declensionPairs, DictCore _core, ConWord _word, boolean _onlyTab) {
        core = _core;
        declensionPairs = Arrays.asList(_declensionPairs);
        word = _word;
        onlyTab = _onlyTab;
        table = new PTable(core);
        decMan = core.getConjugationManager();
        
        setLayout(new BorderLayout());
        
        DefaultTableModel tableModel = buildTableModel();
        
        table.setModel(tableModel);
        setupTableColumns();
        populateLabels(tableModel);
        populateTableModelValues(tableModel);
        setupRowHeight();
        
        this.add(table.getTableHeader(), BorderLayout.PAGE_START);
        this.add(table, BorderLayout.CENTER);
    }
    
    private void populateTableModelValues(DefaultTableModel tableModel) {
        int yPos = 0;
        for (ConjugationPair pair : declensionPairs) {
            // if suppressed, add null value
            if (decMan.isCombinedConjlSurpressed(pair.combinedId, word.getWordTypeId())) {
                tableModel.setValueAt(null, yPos, 1);
            } else {
                String wordForm = word.getWordForm(pair.combinedId);
                autoPopulated = autoPopulated || !wordForm.isBlank(); // keep track of whether anything in this list is populated
                tableModel.setValueAt(wordForm, yPos, 1);
                decIdsToListLocation.put(pair.combinedId, yPos);
            }
            
            yPos++;
        }
    }
    
    /**
     * Returns true if any value in this panel's list is auto-populated
     * @return 
     */
    public boolean isAutoPopulated() {
        return autoPopulated;
    }
    
    private void setupRowHeight() {
        FontMetrics fm = this.getFontMetrics(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon());
        int rowHeight = fm.getHeight() + 2;
        int numRows = table.getRowCount();
        
        for (int i = 0; i < numRows; i++) {
            table.setRowHeight(i, rowHeight);
        }
    }
    
    /**
     * Populates table model with labels in the first row (uneditable)
     * @param tableModel
     */
    private void populateLabels(DefaultTableModel tableModel) {
        Object[] rowLabels = getLabels();
        
        for (int i = 0; i < rowLabels.length; i++) {
            tableModel.setValueAt(rowLabels[i], i, 0);
        }
    }
    
    private void setupTableColumns() {
        TableColumn column = table.getColumnModel().getColumn(0);
        PCellEditor editor = new PCellEditor(false, core);
        PCellRenderer renderer = new PCellRenderer(false, core);
        editor.setBackground(Color.gray);
        renderer.setBackground(Color.lightGray);
        column.setCellEditor(editor);
        column.setCellRenderer(renderer);
        
        Enumeration<TableColumn> colIt = table.getColumnModel().getColumns();
        colIt.nextElement(); // first column is always labels
        while (colIt.hasMoreElements()) {
            TableColumn col = colIt.nextElement();
            col.setCellEditor(new PCellEditor(true, core));
            col.setCellRenderer(new PCellRenderer(true, core));
        }
    }
    
    private DefaultTableModel buildTableModel() {
        Object[] columnLabels = {"Wordform", "Value"};
        Object[] rowLabels = getLabels();
        
        return new DefaultTableModel(columnLabels, rowLabels.length){
            @Override 
            public boolean isCellEditable(int row, int column)
            {
                // the first column is labels and null values are disabled wordforms
                return column != 0 && this.getValueAt(row, column) != null;
            }
        };
    }
    
    private Object[] getLabels() {
        List<String> ret = new ArrayList<>();
        
        declensionPairs.forEach((pair) -> {
            ret.add(pair.label);
        });
        
        return ret.toArray();
    }

    /**
     * Gets map of all declined word forms. Key = combined ID, value = word form
     * @return 
     */
    @Override
    public Map<String, String> getAllDecValues() {
        Map<String, String> ret = new HashMap<>();
        
        TableCellEditor editor = table.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }
        
        decIdsToListLocation.entrySet().forEach((entry) -> {
            Object val = table.getModel().getValueAt(entry.getValue(), 1);
            ret.put(entry.getKey(), val == null ? "" : (String)val);
        });
        
        return ret;
    }   

    @Override
    public String getTabName() {
        return onlyTab ? "Declensions/Conjugations" : "Other";
    }
}
