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
package org.darisadesigns.polyglotlina.CustomControls;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.ConjugationDimension;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import org.apache.commons.lang3.StringUtils;

/**
 * This represents a dimensional grid pane for the representation of word forms.
 * It requires at least two dimensions in the language to work.
 * @author DThompson
 */
public final class PDeclensionGridPanel extends JPanel implements PDeclensionPanelInterface {
    private final PTable table;
    private final DictCore core;
    private final String tabName;
    private final int typeId;
    private final ConjugationManager decMan;
    private final String partialDeclensionIds;
    private final ConWord word;
    private final Map<String, Dimension> decIdsToGridLocation = new HashMap<>();
    private boolean autoPopulated = false;
    
    /**
     * Generates grid panel for displaying/editing word forms
     * @param _partialDeclensionIds
     * @param xDim X dimensional grid value
     * @param yDim Y dimensional grid value
     * @param _typeId part of speech of word being declined/conjugated
     * @param _core Dictionary Core
     * @param _word
     */
    public PDeclensionGridPanel(String _partialDeclensionIds, 
            ConjugationNode xDim, 
            ConjugationNode yDim,
            int _typeId,
            DictCore _core,
            ConWord _word) {
        core = _core;
        typeId = _typeId;
        word = _word;
        decMan = core.getConjugationManager();
        table = new PTable(core);
        partialDeclensionIds = _partialDeclensionIds;
        
        tabName = generateTabName();
        
        setLayout(new BorderLayout());
        
        DefaultTableModel tableModel = buildTableModel();
        
        table.setModel(tableModel);
        setupTableColumns();
        populateYLabels(tableModel, yDim);
        populateTableModelValues(xDim, yDim);
        setupRowHeight();
        
        this.add(table.getTableHeader(), BorderLayout.PAGE_START);
        this.add(table, BorderLayout.CENTER);
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
        int xIndex = getDeclensionIndexOf(partialDeclensionIds, "X");
        int yIndex = getDeclensionIndexOf(partialDeclensionIds, "Y");
        ConjugationNode xNode = decMan.getDimensionalConjugationTemplateByIndex(typeId, xIndex);
        ConjugationNode yNode = decMan.getDimensionalConjugationTemplateByIndex(typeId, yIndex);
        Object[] columnLabels = getLabels(xNode, true);
        Object[] rowLabels = getLabels(yNode, false);
        
        return new DefaultTableModel(columnLabels, rowLabels.length){
            @Override 
            public boolean isCellEditable(int row, int column)
            {
                // the first column is labels and null values are disabled wordforms
                return column != 0 && this.getValueAt(row, column) != null;
            }
        };
    }
    
    /**
     * Populates table model with labels in the first row (uneditable)
     * @param tableModel
     * @param yNode 
     */
    private void populateYLabels(DefaultTableModel tableModel, ConjugationNode yNode) {
        Object[] rowLabels = getLabels(yNode, false);
        
        for (int i = 0; i < rowLabels.length; i++) {
            tableModel.setValueAt(rowLabels[i], i, 0);
        }
    }
    
    /**
     * Generates and populates each word-form into its appropriate place in the grid
     * Also records location of word-form for saving later if ot autogenerated
     * @param xNode
     * @param yNode 
     */
    private void populateTableModelValues(ConjugationNode xNode, ConjugationNode yNode) {
        DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
        
        Iterator<ConjugationDimension> xIt = xNode.getDimensions().iterator();
        
        for (int xPos = 1; xIt.hasNext();) {
            ConjugationDimension decDimX = xIt.next();
            Iterator<ConjugationDimension> yIt = yNode.getDimensions().iterator();
            for (int yPos = 0; yIt.hasNext();) {
                ConjugationDimension decDimY = yIt.next();
                String fullDecId = partialDeclensionIds.replace("X", decDimX.getId().toString());
                fullDecId = fullDecId.replace("Y", decDimY.getId().toString());
                
                // if suppressed, add null value
                if (decMan.isCombinedConjlSurpressed(fullDecId, typeId)) {
                    tableModel.setValueAt(null, yPos, xPos);
                } else {
                    String wordForm = getWordForm(fullDecId);
                    autoPopulated = autoPopulated || !wordForm.isBlank(); // keep track of whether anything in this grid is populated
                    tableModel.setValueAt(wordForm, yPos, xPos);
                    decIdsToGridLocation.put(fullDecId, new Dimension(xPos, yPos));
                }
                
                yPos ++;
            }
            xPos++;
        }
    }
    
    /**
     * Returns true if any value in this panel's list is auto-populated
     * @return 
     */
    public boolean isAutoPopulated() {
        return autoPopulated;
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
        
        decIdsToGridLocation.entrySet().forEach((entry) -> {
            Object val = table.getModel().getValueAt(entry.getValue().height, entry.getValue().width);
            ret.put(entry.getKey(), val == null ? "" : (String)val);
        });
        
        return ret;
    }
    
    private String getWordForm(String fullDecId) {
        String ret;

        if (word.isOverrideAutoDeclen()) {
            ConjugationNode node = decMan.getConjugationByCombinedId(word.getId(), fullDecId);
            ret = node == null ? "" : node.getValue();
        } else {
            try {
                ret = decMan.declineWord(word, fullDecId);
            } catch (Exception e) {
                IOHandler.writeErrorLog(e);
                ret = "ERROR!";
            }
        }

        return ret;
    }
    
    /**
     * Returns array of strings for labels in of columns/rows
     * @param node
     * @return 
     */
    private Object[] getLabels(ConjugationNode node, boolean skipFirst) {
        List<String> labels = new ArrayList<>();
        
        if (skipFirst) {
            labels.add("");
        }
        
        node.getDimensions().forEach((dimension)->{
            labels.add(dimension.getValue());
        });
        
        return labels.toArray();
    }
    
    private void setupRowHeight() {
        FontMetrics fm = this.getFontMetrics(core.getPropertiesManager().getFontCon());
        int rowHeight = fm.getHeight() + 2;
        int numRows = table.getRowCount();
        
        for (int i = 0; i < numRows; i++) {
            table.setRowHeight(i, rowHeight);
        }
    }
    
    /**
     * Based on the partial dimensional ID, return a title for this panel's tab
     * @return 
     */
    private String generateTabName() {
        String[] dimArray = partialDeclensionIds.split(","); 
        dimArray = Arrays.copyOfRange(dimArray, 1, dimArray.length); // first value always empty
        
        String ret = "";
        
        for (int i = 0; i < dimArray.length; i++) {
            String curId = dimArray[i];
            ConjugationNode node = decMan.getDimensionalConjugationTemplateByIndex(typeId, i);
            // skips X and Y elements
            if (StringUtils.isNumeric(curId) && !node.isDimensionless()) {
                ret += node.getConjugationDimensionById(Integer.parseInt(curId)).getValue() + " ";
            }
        }
        
        return ret;
    }
    
    private int getDeclensionIndexOf(String partialDeclensionsId, String marker) {
        String[] dimArray = partialDeclensionsId.split(","); 
        dimArray = Arrays.copyOfRange(dimArray, 1, dimArray.length); // first value always empty
        
        int ret = -1;
        
        for (int i = 0; i < dimArray.length; i++) {
            if (dimArray[i].equals(marker)) {
                ret = i;
                break;
            }
        }
        
        return ret;
    }
    
    @Override
    public String getTabName() {
        return tabName.trim().isEmpty() ? "Declensions/Conjugations" : tabName;
    }
}
