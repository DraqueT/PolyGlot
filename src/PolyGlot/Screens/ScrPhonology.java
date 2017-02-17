/*
 * Copyright (c) 2017, draque.thompson
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
package PolyGlot.Screens;

import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PButton;
import PolyGlot.CustomControls.PCellEditor;
import PolyGlot.CustomControls.PCellRenderer;
import PolyGlot.CustomControls.PFrame;
import PolyGlot.DictCore;
import PolyGlot.Nodes.PronunciationNode;
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
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author draque.thompson
 */
public class ScrPhonology extends PFrame {
    
    private boolean curPopulating = false;

    /**
     * Creates new form scrPhonology
     * @param _core
     */
    public ScrPhonology(DictCore _core) {
        core = _core;        
        initComponents();
        
        populateProcs();
        populateRoms();
        
        getRootPane().setBackground(Color.white);
        chkEnableRom.setSelected(core.getRomManager().isEnabled());
        enableRomanization(chkEnableRom.isSelected());
        setupButtons();
    }
    
    private void setupButtons() {
        Font charis = core.getPropertiesManager().getCharisUnicodeFont();
        btnDownProc.setFont(charis);
        btnDownRom.setFont(charis);
        btnUpProc.setFont(charis);
        btnUpRom.setFont(charis);
    }
    
    @Override
    public final JRootPane getRootPane() {
        return super.getRootPane();
    }
    
    private void enableRomanization(boolean enable) {
        tblRom.setEnabled(enable);
        btnAddRom.setEnabled(enable);
        btnDelRom.setEnabled(enable);
        btnDownRom.setEnabled(enable);
        btnUpRom.setEnabled(enable);
    }
    
    /**
     * adds new, blank pronunciation entry
     */
    private void addProc() {
        final int curPosition = tblProcs.getSelectedRow();
        
        core.getPronunciationMgr().addAtPosition(curPosition + 1, new PronunciationNode());
        populateProcs();

        // perform this action later, once the scroll object is properly updated
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tblProcs.getSelectionModel().setSelectionInterval(curPosition + 1, curPosition + 1);
                tblProcs.scrollRectToVisible(new Rectangle(tblProcs.getCellRect(curPosition + 1, 0, true)));
                tblProcs.changeSelection(curPosition + 1, 0, false, false);
            }
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tblRom.getSelectionModel().setSelectionInterval(curPosition + 1, curPosition + 1);
                tblRom.scrollRectToVisible(new Rectangle(tblRom.getCellRect(curPosition + 1, 0, true)));
                tblRom.changeSelection(curPosition + 1, 0, false, false);
            }
        });
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
    }
    
    /**
     * Adds pronunciation with values existing
     * @param base base characters 
     * @param proc pronunciation
     */
    private void addProcWithValues(String base, String proc) {
        boolean populatingLocal = curPopulating;
        curPopulating = true;

        DefaultTableModel procTableModel = (DefaultTableModel)tblProcs.getModel();
        procTableModel.addRow(new Object[]{base, proc});

        // document listener to be fed into editor/renderers for cells...
        DocumentListener docuListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveProcGuide();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                saveProcGuide();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                saveProcGuide();
            }
        };

        // set saving properties for first column editor
        PCellEditor editor = (PCellEditor) tblProcs.getCellEditor(procTableModel.getRowCount() - 1, 0);
        editor.setDocuListener(docuListener);
        editor.setInitialValue(base);

        // set saving properties for second column editor
        editor = (PCellEditor) tblProcs.getCellEditor(procTableModel.getRowCount() - 1, 1);
        editor.setDocuListener(docuListener);
        editor.setInitialValue(proc);

        curPopulating = populatingLocal;
    }
    
    /**
     * Adds romanization with values existing
     * @param base base characters 
     * @param proc pronunciation
     */
    private void addRomWithValues(String base, String proc) {
        boolean populatingLocal = curPopulating;
        curPopulating = true;

        DefaultTableModel romTableModel = (DefaultTableModel)tblRom.getModel();
        romTableModel.addRow(new Object[]{base, proc});

        // document listener to be fed into editor/renderers for cells...
        DocumentListener docuListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveRomGuide();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                saveRomGuide();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                saveRomGuide();
            }
        };

        // set saving properties for first column editor
        PCellEditor editor = (PCellEditor) tblRom.getCellEditor(romTableModel.getRowCount() - 1, 0);
        editor.setDocuListener(docuListener);
        editor.setInitialValue(base);

        // set saving properties for second column editor
        editor = (PCellEditor) tblRom.getCellEditor(romTableModel.getRowCount() - 1, 1);
        editor.setDocuListener(docuListener);
        editor.setInitialValue(proc);

        curPopulating = populatingLocal;
    }
    
    private void setupProcTable() {
        DefaultTableModel procTableModel = new DefaultTableModel();
        procTableModel.addColumn("Character(s)");
        procTableModel.addColumn("Pronuncation");
        tblProcs.setModel(procTableModel); // TODO: find way to make tblProcs display RTL order when appropriate Maybe something on my custom cell editor

        procTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                saveProcGuide();
            }
        });

        Font defaultFont = core.getPropertiesManager().getCharisUnicodeFont();
        Font conFont = core.getPropertiesManager().getFontCon();

        TableColumn column = tblProcs.getColumnModel().getColumn(0);
        column.setCellEditor(new PCellEditor(conFont));
        column.setCellRenderer(new PCellRenderer(conFont));

        column = tblProcs.getColumnModel().getColumn(1);
        column.setCellEditor(new PCellEditor(defaultFont));
        column.setCellRenderer(new PCellRenderer(defaultFont));

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
    
    private void setupRomTable() {
        DefaultTableModel romTableModel = new DefaultTableModel();
        romTableModel.addColumn("Character(s)");
        romTableModel.addColumn("Romanization");
        tblRom.setModel(romTableModel); // TODO: find way to make rom display RTL order when appropriate Maybe something on my custom cell editor

        romTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                saveRomGuide();
            }
        });

        Font defaultFont = core.getPropertiesManager().getCharisUnicodeFont();
        Font conFont = core.getPropertiesManager().getFontCon();

        TableColumn column = tblRom.getColumnModel().getColumn(0);
        column.setCellEditor(new PCellEditor(conFont));
        column.setCellRenderer(new PCellRenderer(conFont));

        column = tblRom.getColumnModel().getColumn(1);
        column.setCellEditor(new PCellEditor(defaultFont));
        column.setCellRenderer(new PCellRenderer(defaultFont));

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
        Integer curRow = tblProcs.getSelectedRow();

        if (curRow == -1
                || !InfoBox.deletionConfirmation(this)) {
            return;
        }

        PronunciationNode delNode = new PronunciationNode();

        delNode.setValue(tblProcs.getValueAt(curRow, 0).toString());
        delNode.setPronunciation(tblProcs.getValueAt(curRow, 1).toString());

        core.getPronunciationMgr().deletePronunciation(delNode);
        populateProcs();
    }
    
    /**
     * delete currently selected pronunciation (with confirmation)
     */
    private void deleteRom() {
        Integer curRow = tblRom.getSelectedRow();

        if (curRow == -1
                || !InfoBox.deletionConfirmation(this)) {
            return;
        }

        PronunciationNode delNode = new PronunciationNode();

        delNode.setValue(tblRom.getValueAt(curRow, 0).toString());
        delNode.setPronunciation(tblRom.getValueAt(curRow, 1).toString());

        core.getRomManager().deletePronunciation(delNode);
        populateProcs();
    }
    
    /**
     * moves selected pronunciation down one priority slot
     */
    private void moveProcUp() {
        Integer curRow = tblProcs.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getPronunciationMgr().moveProcUp(curRow);

        populateProcs();

        if (curRow != 0) {
            tblProcs.setRowSelectionInterval(curRow - 1, curRow - 1);
        } else {
            tblProcs.setRowSelectionInterval(curRow, curRow);
        }
    }
    
    /**
     * moves selected pronunciation down one priority slot
     */
    private void moveRomUp() {
        Integer curRow = tblRom.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getRomManager().moveProcUp(curRow);

        populateRoms();

        if (curRow != 0) {
            tblRom.setRowSelectionInterval(curRow - 1, curRow - 1);
        } else {
            tblRom.setRowSelectionInterval(curRow, curRow);
        }
    }

    /**
     * moves selected pronunciation up one priority slot
     */
    private void moveProcDown() {
        Integer curRow = tblProcs.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getPronunciationMgr().moveProcDown(curRow);

        populateProcs();

        if (curRow != tblProcs.getRowCount() - 1) {
            tblProcs.setRowSelectionInterval(curRow + 1, curRow + 1);
        } else {
            tblProcs.setRowSelectionInterval(curRow, curRow);
        }
    }
    
    /**
     * moves selected pronunciation up one priority slot
     */
    private void moveRomDown() {
        Integer curRow = tblRom.getSelectedRow();

        if (curRow == -1) {
            return;
        }

        core.getRomManager().moveProcDown(curRow);

        populateRoms();

        if (curRow != tblRom.getRowCount() - 1) {
            tblRom.setRowSelectionInterval(curRow + 1, curRow + 1);
        } else {
            tblRom.setRowSelectionInterval(curRow, curRow);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        pnlOrthography = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnAddProc = new PolyGlot.CustomControls.PAddRemoveButton("+");
        btnDelProc = new PolyGlot.CustomControls.PAddRemoveButton("-");
        btnUpProc = new PButton(core);
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProcs = new javax.swing.JTable();
        btnDownProc = new PButton(core);
        pnlRomanization = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        btnAddRom = new PolyGlot.CustomControls.PAddRemoveButton("+");
        btnDelRom = new PolyGlot.CustomControls.PAddRemoveButton("-");
        btnUpRom = new PButton(core);
        jScrollPane3 = new javax.swing.JScrollPane();
        tblRom = new javax.swing.JTable();
        btnDownRom = new PButton(core);
        chkEnableRom = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        jSplitPane1.setBackground(new java.awt.Color(255, 255, 255));
        jSplitPane1.setDividerLocation(255);

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
        tblProcs.setToolTipText("Add characters (or sets of characters) here with their associated pronunciations.");
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

        javax.swing.GroupLayout pnlOrthographyLayout = new javax.swing.GroupLayout(pnlOrthography);
        pnlOrthography.setLayout(pnlOrthographyLayout);
        pnlOrthographyLayout.setHorizontalGroup(
            pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOrthographyLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlOrthographyLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnDelProc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDownProc, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpProc, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(pnlOrthographyLayout.createSequentialGroup()
                .addComponent(btnAddProc, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlOrthographyLayout.setVerticalGroup(
            pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOrthographyLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlOrthographyLayout.createSequentialGroup()
                        .addComponent(btnUpProc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 282, Short.MAX_VALUE)
                        .addComponent(btnDownProc)
                        .addGap(12, 12, 12))
                    .addGroup(pnlOrthographyLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(pnlOrthographyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddProc, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnDelProc, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        jSplitPane1.setLeftComponent(pnlOrthography);

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
        tblRom.setToolTipText("Add characters (or sets of characters) here with their associated pronunciations.");
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

        javax.swing.GroupLayout pnlRomanizationLayout = new javax.swing.GroupLayout(pnlRomanization);
        pnlRomanization.setLayout(pnlRomanizationLayout);
        pnlRomanizationLayout.setHorizontalGroup(
            pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRomanizationLayout.createSequentialGroup()
                .addComponent(btnAddRom, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlRomanizationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRomanizationLayout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(pnlRomanizationLayout.createSequentialGroup()
                        .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRomanizationLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnDelRom, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(pnlRomanizationLayout.createSequentialGroup()
                                .addComponent(chkEnableRom)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnDownRom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnUpRom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );
        pnlRomanizationLayout.setVerticalGroup(
            pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRomanizationLayout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUpRom)
                    .addComponent(chkEnableRom))
                .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRomanizationLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 282, Short.MAX_VALUE)
                        .addComponent(btnDownRom)
                        .addGap(12, 12, 12))
                    .addGroup(pnlRomanizationLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(pnlRomanizationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddRom, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnDelRom, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        jSplitPane1.setRightComponent(pnlRomanization);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProcActionPerformed
        addProc();
    }//GEN-LAST:event_btnAddProcActionPerformed

    private void btnDelProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelProcActionPerformed
        deleteProc();
    }//GEN-LAST:event_btnDelProcActionPerformed

    private void btnUpProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpProcActionPerformed
        moveProcUp();
    }//GEN-LAST:event_btnUpProcActionPerformed

    private void btnDownProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownProcActionPerformed
        moveProcDown();
    }//GEN-LAST:event_btnDownProcActionPerformed

    private void btnAddRomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRomActionPerformed
        addRom();
    }//GEN-LAST:event_btnAddRomActionPerformed

    private void btnDelRomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelRomActionPerformed
        deleteRom();
    }//GEN-LAST:event_btnDelRomActionPerformed

    private void btnUpRomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpRomActionPerformed
        moveRomUp();
    }//GEN-LAST:event_btnUpRomActionPerformed

    private void btnDownRomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownRomActionPerformed
        moveRomDown();
    }//GEN-LAST:event_btnDownRomActionPerformed

    private void chkEnableRomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkEnableRomActionPerformed
        core.getRomManager().setEnabled(chkEnableRom.isSelected());
        enableRomanization(chkEnableRom.isSelected());
    }//GEN-LAST:event_chkEnableRomActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProc;
    private javax.swing.JButton btnAddRom;
    private javax.swing.JButton btnDelProc;
    private javax.swing.JButton btnDelRom;
    private javax.swing.JButton btnDownProc;
    private javax.swing.JButton btnDownRom;
    private javax.swing.JButton btnUpProc;
    private javax.swing.JButton btnUpRom;
    private javax.swing.JCheckBox chkEnableRom;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel pnlOrthography;
    private javax.swing.JPanel pnlRomanization;
    private javax.swing.JTable tblProcs;
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
        chkEnableRom.setSelected(core.getRomManager().isEnabled());
        enableRomanization(chkEnableRom.isSelected());
    }

    @Override
    public boolean thisOrChildrenFocused() {
        return this.hasFocus();
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
