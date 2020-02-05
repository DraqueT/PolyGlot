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
package org.darisadesigns.polyglotlina.Screens;

import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.CustomControls.PButton;
import org.darisadesigns.polyglotlina.CustomControls.PCellEditor;
import org.darisadesigns.polyglotlina.CustomControls.PCellRenderer;
import org.darisadesigns.polyglotlina.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.PropertiesManager;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.darisadesigns.polyglotlina.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.CustomControls.PCheckBox;

/**
 *
 * @author draque.thompson
 */
public final class ScrPhonology extends PFrame {

    private boolean curPopulating = false;
    private final static String RECURSION_ENABLED_TOOLTIP 
            = "Enable recursion if using lookahead or lookbehind. (more details in manual)";
    private final static String RECURSION_DISABLED_TOOLTIP 
            = "Recursion requires regex. Reenable in language properties to enable this option.";

    /**
     * Creates new form scrPhonology
     *
     * @param _core
     */
    public ScrPhonology(DictCore _core) {
        super(_core);
        
        initComponents();

        populateProcs();
        populateRoms();
        populateReps();

        getRootPane().setBackground(Color.white);
        chkEnableRom.setSelected(core.getRomManager().isEnabled());
        enableRomanization(chkEnableRom.isSelected());
        setupButtons();
        setupRecursionEnabled();
    }

    @Override
    public void dispose() {
        saveAllValues();
        
        if (core.getRomManager().usingLookaheadsLookbacks() && !core.getRomManager().isRecurse()) {
            InfoBox.warning("Possible Regex Issue", "It looks like your romanizations use lookahead or lookbehind patterns. "
                    + "Please enable the recursion checkbox or these will not function correctly.", core.getRootWindow());
        }
        
        if (core.getPronunciationMgr().usingLookaheadsLookbacks() && !core.getPronunciationMgr().isRecurse()) {
            InfoBox.warning("Possible Regex Issue", "It looks like your pronunciations use lookahead or lookbehind patterns. "
                    + "Please enable the recursion checkbox or these will not function correctly.", core.getRootWindow());
        }
        
        super.dispose();
    }
    
    @Override
    public void saveAllValues() {
        if (tblRep.getCellEditor() != null) {
            tblRep.getCellEditor().stopCellEditing();
        }
        if (tblProcs.getCellEditor() != null) {
            tblProcs.getCellEditor().stopCellEditing();
        }
        if (tblRom.getCellEditor() != null) {
            tblRom.getCellEditor().stopCellEditing();
        }
        
        saveProcGuide();
        saveRepTable();
        saveRomGuide();
    }

    private void setupButtons() {
        Font charis = core.getPropertiesManager().getFontLocal();
        btnDownProc.setFont(charis);
        btnDownRom.setFont(charis);
        btnUpProc.setFont(charis);
        btnUpRom.setFont(charis);
    }

    private void enableRomanization(boolean enable) {
        tblRom.setEnabled(enable);
        btnAddRom.setEnabled(enable);
        btnDelRom.setEnabled(enable);
        btnDownRom.setEnabled(enable);
        btnUpRom.setEnabled(enable);
        setupRecursionEnabled();
        chkRomRecurse.setEnabled(chkRomRecurse.isEnabled() && enable);
    }

    /**
     * adds new, blank pronunciation entry
     */
    private void addProc() {
        final int curPosition = tblProcs.getSelectedRow();

        core.getPronunciationMgr().addAtPosition(curPosition + 1, new PronunciationNode());
        populateProcs();

        // perform this action later, once the scroll object is properly updated
        SwingUtilities.invokeLater(() -> {
            tblProcs.getSelectionModel().setSelectionInterval(curPosition + 1, curPosition + 1);
            tblProcs.scrollRectToVisible(new Rectangle(tblProcs.getCellRect(curPosition + 1, 0, true)));
            tblProcs.changeSelection(curPosition + 1, 0, false, false);
        });
    }

    /**
     * adds new, blank romanization entry
     */
    private void addRom() {
        final int curPosition = tblRom.getSelectedRow();

        core.getRomManager().addAtPosition(curPosition + 1, new PronunciationNode());
        populateRoms();

        // perform this action later, once the scroll object is properly updated
        SwingUtilities.invokeLater(() -> {
            tblRom.getSelectionModel().setSelectionInterval(curPosition + 1, curPosition + 1);
            tblRom.scrollRectToVisible(new Rectangle(tblRom.getCellRect(curPosition + 1, 0, true)));
            tblRom.changeSelection(curPosition + 1, 0, false, false);
        });
    }

    /**
     * Adds new character replacement entry
     */
    private void addRep() {
        boolean localPopulating = curPopulating;
        curPopulating = true;

        if (tblRep.getCellEditor() != null && tblRep.getSelectedRow() != -1 && tblRep.getRowCount() > 0) {
            tblRep.getCellEditor().stopCellEditing();
        }
        saveRepTable();
        
        core.getPropertiesManager().AddEmptyRep();
        populateReps();
        int end = tblRep.getModel().getRowCount();
        tblRep.getSelectionModel().setSelectionInterval(end, end);
        tblRep.scrollRectToVisible(new Rectangle(tblRep.getCellRect(end, 0, true)));
        tblRep.changeSelection(end, 0, false, false);

        curPopulating = localPopulating;
    }

    /**
     * populates pronunciation values
     */
    private void populateProcs() {
        // wipe current rows, repopulate from core
        setupProcTable();

        for (PronunciationNode curNode : core.getPronunciationMgr().getPronunciations()) {
            addProcWithValues(curNode.getValue(), curNode.getPronunciation());
        }
        
        chkPhonRecurse.setSelected(core.getPronunciationMgr().isRecurse());
    }

    /**
     * populates romanization values
     */
    private void populateRoms() {
        // wipe current rows, repopulate from core
        setupRomTable();

        for (PronunciationNode curNode : core.getRomManager().getPronunciations()) {
            addRomWithValues(curNode.getValue(), curNode.getPronunciation());
        }
        
        chkRomRecurse.setSelected(core.getRomManager().isRecurse());
    }

    /**
     * Populates replacement character/string pairs
     */
    private void populateReps() {
        setupRepTable();

        core.getPropertiesManager().getAllCharReplacements().forEach((entry) -> {
            addRep(entry.getKey(), entry.getValue());
        });
    }
    
    /**
     * Performs logic to see whether recursion should be enabled and updates menu accordingly
     */
    private void setupRecursionEnabled() {
        if (core.getPropertiesManager().isDisableProcRegex()) {
            chkPhonRecurse.setEnabled(false);
            chkPhonRecurse.setToolTipText(RECURSION_DISABLED_TOOLTIP);
            chkRomRecurse.setEnabled(false);
            chkRomRecurse.setToolTipText(RECURSION_DISABLED_TOOLTIP);
        } else {
            chkPhonRecurse.setEnabled(true);
            chkPhonRecurse.setToolTipText(RECURSION_ENABLED_TOOLTIP);
            chkRomRecurse.setEnabled(true);
            chkRomRecurse.setToolTipText(RECURSION_ENABLED_TOOLTIP);
        }
    }

    //private void addRep(Entry<String, String> entry) {
    private void addRep(String key, String value) {
        boolean populatingLocal = curPopulating;
        curPopulating = true;

        DefaultTableModel romTableModel = (DefaultTableModel) tblRep.getModel();
        romTableModel.addRow(new Object[]{key, value});

        // set saving properties for character column editor
        final int thisRow = romTableModel.getRowCount() - 1;
        final PCellEditor editChar = (PCellEditor) tblRom.getCellEditor(thisRow, 0);

        editChar.setInitialValue(key);

        // set saving properties for value column editor
        PCellEditor editor = (PCellEditor) tblRom.getCellEditor(romTableModel.getRowCount() - 1, 1);
        editor.setInitialValue(value);

        curPopulating = populatingLocal;
    }

    /**
     * Tests whether the replacement table contains duplicates
     *
     * @param value value to be checked
     * @return true if dups, false otherwise
     */
    private boolean checkRepRepeats(String value) {
        boolean ret = false;

        if (!value.isEmpty()) {
            for (int i = 0; i < tblRep.getRowCount(); i++) {
                if (value.equals(tblRep.getModel().getValueAt(i, 0))) {
                    ret = true;
                    break;
                }
            }
        }
        
        return ret;
    }

    /**
     * Adds pronunciation with values existing
     *
     * @param base base characters
     * @param proc pronunciation
     */
    private void addProcWithValues(String base, String proc) {
        boolean populatingLocal = curPopulating;
        curPopulating = true;

        DefaultTableModel procTableModel = (DefaultTableModel) tblProcs.getModel();
        procTableModel.addRow(new Object[]{base, proc});

        // TODO: Delete if not broken from this: #839
        // document listener to be fed into editor/renderers for cells...
//        DocumentListener docuListener = new DocumentListener() {
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                saveProcGuide();
//            }
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                saveProcGuide();
//            }
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                saveProcGuide();
//            }
//        };
//
//        // set saving properties for first column editor
//        PCellEditor editor = (PCellEditor) tblProcs.getCellEditor(procTableModel.getRowCount() - 1, 0);
//        editor.setDocuListener(docuListener);
//        editor.setInitialValue(base);
//
//        // set saving properties for second column editor
//        editor = (PCellEditor) tblProcs.getCellEditor(procTableModel.getRowCount() - 1, 1);
//        editor.setDocuListener(docuListener);
//        editor.setInitialValue(proc);        

        curPopulating = populatingLocal;
    }

    /**
     * Adds romanization with values existing
     *
     * @param base base characters
     * @param proc pronunciation
     */
    private void addRomWithValues(String base, String proc) {
        boolean populatingLocal = curPopulating;
        curPopulating = true;

        DefaultTableModel romTableModel = (DefaultTableModel) tblRom.getModel();
        romTableModel.addRow(new Object[]{base, proc});

        // TODO: Delete if doesn't break things #839
        // document listener to be fed into editor/renderers for cells...
//        DocumentListener docuListener = new DocumentListener() {
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                saveRomGuide();
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                saveRomGuide();
//            }
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                saveRomGuide();
//            }
//        };
//
//        // set saving properties for first column editor
//        PCellEditor editor = (PCellEditor) tblRom.getCellEditor(romTableModel.getRowCount() - 1, 0);
//        editor.setDocuListener(docuListener);
//        editor.setInitialValue(base);
//
//        // set saving properties for second column editor
//        editor = (PCellEditor) tblRom.getCellEditor(romTableModel.getRowCount() - 1, 1);
//        editor.setDocuListener(docuListener);
//        editor.setInitialValue(proc);

        curPopulating = populatingLocal;
    }

    private void setupProcTable() {
        DefaultTableModel procTableModel = new DefaultTableModel();
        procTableModel.addColumn("Character(s)");
        procTableModel.addColumn("Pronunciation");
        tblProcs.setModel(procTableModel); // TODO: find way to make tblProcs display RTL order when appropriate Maybe something on my custom cell editor
        
        // TODO: Delete if not broken from this: #839
//        procTableModel.addTableModelListener((TableModelEvent e) -> {
//            saveProcGuide();
//        });
        
        boolean useConFont = !core.getPropertiesManager().isOverrideRegexFont();

        TableColumn column = tblProcs.getColumnModel().getColumn(0);
        column.setCellEditor(new PCellEditor(useConFont, core));
        column.setCellRenderer(new PCellRenderer(useConFont, core));

        column = tblProcs.getColumnModel().getColumn(1);
        column.setCellEditor(new PCellEditor(false, core));
        column.setCellRenderer(new PCellRenderer(false, core));

        // disable tab/arrow selection
        InputMap procInput = tblProcs.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), "none");
    }

    private void setupRepTable() {
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Character");
        tableModel.addColumn("Replacement");

        tblRep.setModel(tableModel); // TODO: find way to make rom display RTL order when appropriate Maybe something on my custom cell editor
        
        // TODO: Delete if this doesn't break #839
//        tableModel.addTableModelListener((TableModelEvent e) -> {
//            saveRepTable();
//        });
        
        boolean useConFont = !core.getPropertiesManager().isOverrideRegexFont();

        TableColumn column = tblRep.getColumnModel().getColumn(0);
        final PCellEditor editChar = new PCellEditor(false, core);
        editChar.setIgnoreListenerSilencing(true);
        editChar.setDocuListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                doSave(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doSave(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                doSave(e);
            }

            private void doSave(DocumentEvent e) {
                final String value = editChar.getCellEditorValue().toString();

                if (!curPopulating && value.length() > 1) {
                    SwingUtilities.invokeLater(() -> {
                        editChar.setIgnoreListenerSilencing(false);
                        editChar.setValue(value.substring(0, 1));
                        editChar.setIgnoreListenerSilencing(true);
                        InfoBox.warning("Single Character Only", "Replacement characters can only be 1 character long.", core.getRootWindow());
                    });
                }
                // TODO: Delete if doesn't break things: #839
//                else {
//                    saveRepTable();
//                }
            }
        });        
        column.setCellEditor(editChar);
        column.setCellRenderer(new PCellRenderer(false, core));

        column = tblRep.getColumnModel().getColumn(1);
        PCellEditor valueEdit = new PCellEditor(useConFont, core);
        valueEdit.setIgnoreListenerSilencing(true);
        // TODO: Delete if doesn't break things #839
//        valueEdit.setDocuListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                saveRepTable();
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                saveRepTable();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                saveRepTable();
//            }
//        });
        column.setCellEditor(valueEdit);
        column.setCellRenderer(new PCellRenderer(useConFont, core));

        // disable tab/arrow selection
        InputMap procInput = tblRom.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), "none");
    }

    private void setupRomTable() {
        DefaultTableModel romTableModel = new DefaultTableModel();
        romTableModel.addColumn("Character(s)");
        romTableModel.addColumn("Romanization");
        tblRom.setModel(romTableModel); // TODO: find way to make rom display RTL order when appropriate Maybe something on my custom cell editor

        // TODO: Delete if doesn't break anything #839
//        romTableModel.addTableModelListener((TableModelEvent e) -> {
//            saveRomGuide();
//        });
        
        boolean useConFont = !core.getPropertiesManager().isOverrideRegexFont();

        TableColumn column = tblRom.getColumnModel().getColumn(0);
        column.setCellEditor(new PCellEditor(useConFont, core));
        column.setCellRenderer(new PCellRenderer(useConFont, core));

        column = tblRom.getColumnModel().getColumn(1);
        column.setCellEditor(new PCellEditor(false, core));
        column.setCellRenderer(new PCellRenderer(false, core));

        // disable tab/arrow selection
        InputMap procInput = tblRom.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        procInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), "none");
    }

    // TODO: THIS
    // save on lose focus
    // save on close
    // do not save on field update
    
    /**
     * Saves pronunciation guide to core
     */
    private void saveProcGuide() {
        if (curPopulating) {
            return;
        }
        boolean localPopulating = curPopulating;
        curPopulating = true;

        if (tblProcs.getCellEditor() != null) {
            tblProcs.getCellEditor().stopCellEditing();
        }

        List<PronunciationNode> newPro = new ArrayList<>();

        for (int i = 0; i < tblProcs.getRowCount(); i++) {
            PronunciationNode newNode = new PronunciationNode();

            newNode.setValue((String) tblProcs.getModel().getValueAt(i, 0));
            newNode.setPronunciation((String) tblProcs.getModel().getValueAt(i, 1));

            newPro.add(newNode);
        }

        core.getPronunciationMgr().setPronunciations(newPro);
        curPopulating = localPopulating;
    }

    private void saveRepTable() {
        if (curPopulating) {
            return;
        }

        boolean localPopulating = curPopulating;
        curPopulating = true;
        PropertiesManager propMan = core.getPropertiesManager();

        if (tblRep.getCellEditor() != null && tblRep.getSelectedRow() != -1 && tblRep.getRowCount() > 0) {
            tblRep.getCellEditor().stopCellEditing();
        }

        propMan.clearCharacterReplacement();

        for (int i = 0; i < tblRep.getRowCount(); i++) {
            String repChar = tblRep.getValueAt(i, 0).toString();
            String value = tblRep.getValueAt(i, 1).toString();

            if (repChar.isEmpty()) {
                continue;
            }

            propMan.addCharacterReplacement(repChar, value);
        }

        curPopulating = localPopulating;
    }

    /**
     * Saves pronunciation guide to core
     */
    private void saveRomGuide() {
        if (curPopulating) {
            return;
        }

        boolean localPopulating = curPopulating;
        curPopulating = true;

        if (tblRom.getCellEditor() != null) {
            tblRom.getCellEditor().stopCellEditing();
        }

        List<PronunciationNode> newRom = new ArrayList<>();

        for (int i = 0; i < tblRom.getRowCount(); i++) {
            PronunciationNode newNode = new PronunciationNode();

            newNode.setValue((String) tblRom.getModel().getValueAt(i, 0));
            newNode.setPronunciation((String) tblRom.getModel().getValueAt(i, 1));

            newRom.add(newNode);
        }

        core.getRomManager().setPronunciations(newRom);
        curPopulating = localPopulating;
    }

    /**
     * delete currently selected pronunciation (with confirmation)
     */
    private void deleteProc() {
        int curRow = tblProcs.getSelectedRow();

        if (curRow == -1
                || !InfoBox.deletionConfirmation(core.getRootWindow())) {
            return;
        }

        PronunciationNode delNode = new PronunciationNode();

        delNode.setValue(tblProcs.getValueAt(curRow, 0).toString());
        delNode.setPronunciation(tblProcs.getValueAt(curRow, 1).toString());

        core.getPronunciationMgr().deletePronunciation(delNode);
        populateProcs();
    }

    /**
     * Deletes currently selected replacement character
     */
    private void deleteRep() {
        int curRow = tblRep.getSelectedRow();

        if (curRow == -1
                || !InfoBox.deletionConfirmation(core.getRootWindow())) {
            return;
        }

        core.getPropertiesManager().delCharacterReplacement(
                tblRep.getValueAt(curRow, 0).toString());
        populateReps();
    }

    /**
     * delete currently selected pronunciation (with confirmation)
     */
    private void deleteRom() {
        int curRow = tblRom.getSelectedRow();

        if (curRow == -1
                || !InfoBox.deletionConfirmation(core.getRootWindow())) {
            return;
        }

        PronunciationNode delNode = new PronunciationNode();

        delNode.setValue(tblRom.getValueAt(curRow, 0).toString());
        delNode.setPronunciation(tblRom.getValueAt(curRow, 1).toString());

        core.getRomManager().deletePronunciation(delNode);
        populateRoms();
    }

    /**
     * moves selected pronunciation down one priority slot
     */
    private void moveProcUp() {
        int curRow = tblProcs.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getPronunciationMgr().moveProcUp(curRow);

        populateProcs();

        if (curRow == 0) {
            tblProcs.setRowSelectionInterval(curRow, curRow);
        } else {
            tblProcs.setRowSelectionInterval(curRow - 1, curRow - 1);
        }
    }

    /**
     * moves selected pronunciation down one priority slot
     */
    private void moveRomUp() {
        int curRow = tblRom.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getRomManager().moveProcUp(curRow);

        populateRoms();

        if (curRow == 0) {
            tblRom.setRowSelectionInterval(curRow, curRow);
        } else {
            tblRom.setRowSelectionInterval(curRow - 1, curRow - 1);
        }
    }

    /**
     * moves selected pronunciation up one priority slot
     */
    private void moveProcDown() {
        int curRow = tblProcs.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getPronunciationMgr().moveProcDown(curRow);

        populateProcs();

        if (curRow == tblProcs.getRowCount() - 1) {
            tblProcs.setRowSelectionInterval(curRow, curRow);
        } else {
            tblProcs.setRowSelectionInterval(curRow + 1, curRow + 1);
        }
    }

    /**
     * moves selected pronunciation up one priority slot
     */
    private void moveRomDown() {
        int curRow = tblRom.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getRomManager().moveProcDown(curRow);

        populateRoms();

        if (curRow == tblRom.getRowCount() - 1) {
            tblRom.setRowSelectionInterval(curRow, curRow);
        } else {
            tblRom.setRowSelectionInterval(curRow + 1, curRow + 1);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlRomanization = new javax.swing.JPanel();
        jLabel2 = new PLabel("", menuFontSize);
        btnAddRom = new PAddRemoveButton("+");
        btnDelRom = new PAddRemoveButton("-");
        btnUpRom = new PButton(nightMode, menuFontSize);
        jScrollPane3 = new javax.swing.JScrollPane();
        tblRom = new javax.swing.JTable();
        btnDownRom = new PButton(nightMode, menuFontSize);
        chkEnableRom = new PCheckBox(nightMode, menuFontSize);
        chkRomRecurse = new PCheckBox(nightMode, menuFontSize);
        pnlOrthography = new javax.swing.JPanel();
        jLabel1 = new PLabel("", menuFontSize);
        btnAddProc = new PAddRemoveButton("+");
        btnDelProc = new PAddRemoveButton("-");
        btnUpProc = new PButton(nightMode, menuFontSize);
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProcs = new javax.swing.JTable();
        btnDownProc = new PButton(nightMode, menuFontSize);
        chkPhonRecurse = new PCheckBox(nightMode, menuFontSize);
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new PLabel("", menuFontSize);
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRep = new javax.swing.JTable();
        btnAddCharRep = new PAddRemoveButton("+");
        btnDelCharRep = new PAddRemoveButton("-");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Phonology & Text");
        setBackground(new java.awt.Color(255, 255, 255));

        pnlRomanization.setBackground(new java.awt.Color(255, 255, 255));
        pnlRomanization.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlRomanization.setMinimumSize(new java.awt.Dimension(10, 10));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Romanization");
        jLabel2.setToolTipText("The Pronunciation Guide");

        btnAddRom.setToolTipText("Add new Romanization entry.");
        btnAddRom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRomActionPerformed(evt);
            }
        });

        btnDelRom.setToolTipText("Delete selected romanization entry.");
        btnDelRom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelRomActionPerformed(evt);
            }
        });

        btnUpRom.setText("↑");
        btnUpRom.setToolTipText("Move selected entry up one position.");
        btnUpRom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpRomActionPerformed(evt);
            }
        });

        jScrollPane3.setToolTipText("");

        tblRom.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null}
            },
            new String [] {
                "Character(s)", "Pronunciation"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblRom.setToolTipText("Add characters (or sets of characters) here with their associated pronunciations. Characters column accepts regex patterns.");
        tblRom.setMinimumSize(new java.awt.Dimension(10, 20));
        tblRom.setRowHeight(30);
        jScrollPane3.setViewportView(tblRom);

        btnDownRom.setText("↓");
        btnDownRom.setToolTipText("Move selected entry down one position.");
        btnDownRom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownRomActionPerformed(evt);
            }
        });

        chkEnableRom.setText("Enable Romanization");
        chkEnableRom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkEnableRomActionPerformed(evt);
            }
        });

        chkRomRecurse.setText("Recurse Patterns");
        chkRomRecurse.setToolTipText("");
        chkRomRecurse.setEnabled(false);
        chkRomRecurse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkRomRecurseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlRomanizationLayout = new javax.swing.GroupLayout(pnlRomanization);
        pnlRomanization.setLayout(pnlRomanizationLayout);
        pnlRomanizationLayout.setHorizontalGroup(
            pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRomanizationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRomanizationLayout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(pnlRomanizationLayout.createSequentialGroup()
                        .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRomanizationLayout.createSequentialGroup()
                                .addComponent(btnAddRom, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnDelRom, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(pnlRomanizationLayout.createSequentialGroup()
                                .addComponent(chkRomRecurse)
                                .addGap(0, 53, Short.MAX_VALUE))
                            .addComponent(chkEnableRom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnDownRom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnUpRom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );
        pnlRomanizationLayout.setVerticalGroup(
            pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRomanizationLayout.createSequentialGroup()
                .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRomanizationLayout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnUpRom)
                            .addComponent(chkEnableRom))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(pnlRomanizationLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnDownRom)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkRomRecurse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddRom)
                    .addComponent(btnDelRom, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        pnlOrthography.setBackground(new java.awt.Color(255, 255, 255));
        pnlOrthography.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlOrthography.setMinimumSize(new java.awt.Dimension(10, 10));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Phonemic Orthography");
        jLabel1.setToolTipText("The Pronunciation Guide");

        btnAddProc.setToolTipText("Add new pronunciation entry.");
        btnAddProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProcActionPerformed(evt);
            }
        });

        btnDelProc.setToolTipText("Delete selected pronunciation entry.");
        btnDelProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelProcActionPerformed(evt);
            }
        });

        btnUpProc.setText("↑");
        btnUpProc.setToolTipText("Move selected entry up one position.");
        btnUpProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpProcActionPerformed(evt);
            }
        });

        tblProcs.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null}
            },
            new String [] {
                "Character(s)", "Pronunciation"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblProcs.setToolTipText("Add characters (or sets of characters) here with their associated pronunciations. Characters column accepts regex patterns.");
        tblProcs.setMinimumSize(new java.awt.Dimension(10, 20));
        tblProcs.setRowHeight(30);
        jScrollPane2.setViewportView(tblProcs);

        btnDownProc.setText("↓");
        btnDownProc.setToolTipText("Move selected entry down one position.");
        btnDownProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownProcActionPerformed(evt);
            }
        });

        chkPhonRecurse.setText("Recurse Patterns");
        chkPhonRecurse.setToolTipText("");
        chkPhonRecurse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkPhonRecurseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlOrthographyLayout = new javax.swing.GroupLayout(pnlOrthography);
        pnlOrthography.setLayout(pnlOrthographyLayout);
        pnlOrthographyLayout.setHorizontalGroup(
            pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
            .addGroup(pnlOrthographyLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlOrthographyLayout.createSequentialGroup()
                        .addComponent(btnAddProc, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDelProc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(chkPhonRecurse, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDownProc, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpProc, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        pnlOrthographyLayout.setVerticalGroup(
            pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOrthographyLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlOrthographyLayout.createSequentialGroup()
                        .addComponent(btnUpProc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 217, Short.MAX_VALUE)
                        .addComponent(btnDownProc))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkPhonRecurse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddProc, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnDelProc, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Character Replacement");

        jScrollPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jScrollPane1.setName(""); // NOI18N

        tblRep.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null}
            },
            new String [] {
                "Character", "Replacement"
            }
        ));
        tblRep.setToolTipText("Active typing character replacement entries. Character you type on the left, character(s) to replace it with on the right.");
        tblRep.setMinimumSize(new java.awt.Dimension(0, 0));
        tblRep.setRowHeight(30);
        jScrollPane1.setViewportView(tblRep);

        btnAddCharRep.setToolTipText("Add new character replacement entry");
        btnAddCharRep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCharRepActionPerformed(evt);
            }
        });

        btnDelCharRep.setToolTipText("Delete currently selected character replacement entry");
        btnDelCharRep.setMaximumSize(new java.awt.Dimension(80, 80));
        btnDelCharRep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelCharRepActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 123, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnAddCharRep, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDelCharRep, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDelCharRep, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddCharRep, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlOrthography, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlRomanization, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlOrthography, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlRomanization, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProcActionPerformed
        saveAllValues();
        addProc();
    }//GEN-LAST:event_btnAddProcActionPerformed

    private void btnDelProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelProcActionPerformed
        saveAllValues();
        deleteProc();
    }//GEN-LAST:event_btnDelProcActionPerformed

    private void btnUpProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpProcActionPerformed
        saveAllValues();
        moveProcUp();
    }//GEN-LAST:event_btnUpProcActionPerformed

    private void btnDownProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownProcActionPerformed
        saveAllValues();
        moveProcDown();
    }//GEN-LAST:event_btnDownProcActionPerformed

    private void btnAddRomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRomActionPerformed
        saveAllValues();
        addRom();
    }//GEN-LAST:event_btnAddRomActionPerformed

    private void btnDelRomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelRomActionPerformed
        saveAllValues();
        deleteRom();
    }//GEN-LAST:event_btnDelRomActionPerformed

    private void btnUpRomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpRomActionPerformed
        saveAllValues();
        moveRomUp();
    }//GEN-LAST:event_btnUpRomActionPerformed

    private void btnDownRomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownRomActionPerformed
        saveAllValues();
        moveRomDown();
    }//GEN-LAST:event_btnDownRomActionPerformed

    private void chkEnableRomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkEnableRomActionPerformed
        core.getRomManager().setEnabled(chkEnableRom.isSelected());
        enableRomanization(chkEnableRom.isSelected());
    }//GEN-LAST:event_chkEnableRomActionPerformed

    private void btnAddCharRepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCharRepActionPerformed
        saveAllValues();
        addRep();
    }//GEN-LAST:event_btnAddCharRepActionPerformed

    private void btnDelCharRepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelCharRepActionPerformed
        saveAllValues();
        deleteRep();
    }//GEN-LAST:event_btnDelCharRepActionPerformed

    private void chkPhonRecurseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPhonRecurseActionPerformed
        core.getPronunciationMgr().setRecurse(chkPhonRecurse.isSelected());
    }//GEN-LAST:event_chkPhonRecurseActionPerformed

    private void chkRomRecurseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkRomRecurseActionPerformed
        core.getRomManager().setRecurse(chkRomRecurse.isSelected());
    }//GEN-LAST:event_chkRomRecurseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCharRep;
    private javax.swing.JButton btnAddProc;
    private javax.swing.JButton btnAddRom;
    private javax.swing.JButton btnDelCharRep;
    private javax.swing.JButton btnDelProc;
    private javax.swing.JButton btnDelRom;
    private javax.swing.JButton btnDownProc;
    private javax.swing.JButton btnDownRom;
    private javax.swing.JButton btnUpProc;
    private javax.swing.JButton btnUpRom;
    private javax.swing.JCheckBox chkEnableRom;
    private javax.swing.JCheckBox chkPhonRecurse;
    private javax.swing.JCheckBox chkRomRecurse;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel pnlOrthography;
    private javax.swing.JPanel pnlRomanization;
    private javax.swing.JTable tblProcs;
    private javax.swing.JTable tblRep;
    private javax.swing.JTable tblRom;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void updateAllValues(DictCore _core) {
        core = _core;
        populateProcs();
        populateRoms();
        populateReps();
        chkEnableRom.setSelected(core.getRomManager().isEnabled());
        enableRomanization(chkEnableRom.isSelected());
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        // no bindings to add
    }

    @Override
    public Component getWindow() {
        return this.getRootPane();
    }
}
