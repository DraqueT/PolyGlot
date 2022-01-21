/*
 * Copyright (c) 2022, Draque Thompson, draquemail@gmail.com
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

package org.darisadesigns.polyglotlina.Screens;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.PlainDocument;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCellEditor;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PComboBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PListLexicon;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PRadioButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTable;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextFieldFilter;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection.ConWordDisplay;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.ZompistVocabGenerator;

/**
 *
 * @author draque
 */
public class ScrZompistLexiconGen extends PFrame {
    
    private int curDefaults = 0;
    private boolean isWordImport = true;

    public ScrZompistLexiconGen(DictCore _core) {
        super(_core);
        initComponents();
        populateSwadeshList();
        loadValues();
        setupListeners();
        setupMenus();
    }
    
    private void setupMenus() {
        var localFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
        var conFont =  ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon();
        
        txtCategories.setFont(conFont);
        txtGenerationNum.setFont(localFont);
        txtRewriteRules.setFont(conFont);
        txtSyllableTypes.setFont(conFont);
        txtIllegalClusters.setFont(conFont);
        tblGeneratedValues.setModel(new DefaultTableModel());
        tblGeneratedValues.setFont(conFont);
        tblSwadesh.setModel(new DefaultTableModel());
        tblSwadesh.setFont(localFont);
        lstImport.setModel(new DefaultListModel<>());
        setDropoffLabel();
        setMonosyllableLabel();
        tableValuesUpdated();
        
        ((PlainDocument) txtGenerationNum.getDocument())
                .setDocumentFilter(new PTextFieldFilter());
    }
    
    private void loadValues() {
        var categoriesStr = core.getPropertiesManager().getZompistCategories();
        var illegalsStr = core.getPropertiesManager().getZompistIllegalClusters();
        var rewriteStr = core.getPropertiesManager().getZompistRewriteRules();
        var syllablesStr = core.getPropertiesManager().getZompistSyllableTypes();
        
        setDefaultValues();
        
        if (!categoriesStr.isBlank() 
                || !illegalsStr.isBlank() 
                || !rewriteStr.isBlank() 
                || !syllablesStr.isBlank()) {
            txtCategories.setText(categoriesStr);
            txtIllegalClusters.setText(illegalsStr);
            txtRewriteRules.setText(rewriteStr);
            txtSyllableTypes.setText(syllablesStr);
        }
    }
    
    private void setDefaultValues() {    
        String defaultPhonotacticConstraints = "pw\nfw\nbw\ntl\ndl\nθl\ngw\nmb\nmv";
        switch (curDefaults) {
            case 0 -> {
                // Large inventory
                txtCategories.setText("C=ptknslrmbdgfvwyhšzñxčžŋ\nV=aiuoeɛɔâôüö\nR=rly");
                txtSyllableTypes.setText("CV\nV\nCVC\nCRV");
                txtRewriteRules.setText("â|ai\nô|au");
                txtIllegalClusters.setText(defaultPhonotacticConstraints);
            }
            case 1 -> {
                // Latinate
                txtCategories.setText("C=tkpnslrmfbdghvyh\nV=aiueo\nU=aiuôê\nR=rl\nM=nsrmltc\nK=ptkbdg");
                txtSyllableTypes.setText("CV\nCUM\nV\nUM\nKRV\nKRUM");
                txtRewriteRules.setText("ka|ca\nko|co\nku|cu\nkr|cr");
                txtIllegalClusters.setText(defaultPhonotacticConstraints);
            }
            case 2 -> {
                // Simple
                txtCategories.setText("C=tpknlrsmʎbdgñfh\nV=aieuoāīūēō\nN=nŋ");
                txtSyllableTypes.setText("CV\nV\nCVN");
                txtRewriteRules.setText("aa|ā\nii|ī\nuu|ū\nee|ē\noo|ō\nnb|mb\nnp|mp");
                txtIllegalClusters.setText(defaultPhonotacticConstraints);
            }
            case 3 -> {
                // Chinese
                txtCategories.setText("C=ptknlsmšywčhfŋ\nV=auieo\nR=rly\nN=nnŋmktp\nW=io\nQ=ptkč");
                txtSyllableTypes.setText("CV\nQʰV\nCVW\nCVN\nVN\nV\nQʰVN");
                txtRewriteRules.setText("uu|wo\noo|ou\nii|iu\naa|ia\nee|ie");
                txtIllegalClusters.setText(defaultPhonotacticConstraints);
            }
            case 4 -> {
                // Original default
                txtCategories.setText("C=ptkbdg\nR=rl\nV=ieaou");
                txtSyllableTypes.setText("CV\nV\nCRV");
                txtRewriteRules.setText("ki|či");
                txtIllegalClusters.setText(defaultPhonotacticConstraints);
            }
            case 5-> {
                // Original default
                txtCategories.setText("C=tknsmrh\n" +
                        "V=aioeu\n" +
                        "U=auoāēū\n" +
                        "L=āīōēū");
                txtSyllableTypes.setText("CV\n" +
                        "CVn\n" +
                        "CL\n" +
                        "CLn\n" +
                        "CyU\n" +
                        "CyUn\n" +
                        "Vn\n" +
                        "Ln\n" +
                        "CVq\n" +
                        "CLq\n" +
                        "yU\n" +
                        "yUn\n" +
                        "wa\n" +
                        "L\n" +
                        "V");
                txtRewriteRules.setText("hu|fu\n" +
                        "hū|fū\n" +
                        "si|shi\n" +
                        "sī|shī\n" +
                        "sy|sh\n" +
                        "ti|chi\n" +
                        "tī|chī\n" +
                        "ty|ch\n" +
                        "tu|tsu\n" +
                        "tū|tsū\n" +
                        "qk|kk\n" +
                        "qp|pp\n" +
                        "qt|tt\n" +
                        "q[^ptk]|");
                txtIllegalClusters.setText(defaultPhonotacticConstraints);
            }
            default -> {
                // something weird happened, just use 0 value and reset
                curDefaults = 0;
                txtCategories.setText("C=ptknslrmbdgfvwyhšzñxčžŋ\nV=aiuoeɛɔâôüö\nR=rly");
                txtSyllableTypes.setText("CV\nV\nCVC\nCRV");
                txtRewriteRules.setText("â|ai\nô|au");
                return;
            }
        }
        
        txtGenerationNum.setText("150");
        rdoGenWords.setSelected(true);
        chkShowSyllables.setSelected(false);
        chkSlowSyllableDropoff.setSelected(false);
        sldDropoff.setValue(31);
        sldMonoSyllables.setValue(15);
        
        curDefaults = (curDefaults + 1) % 6;
    }
    
    private void setDropoffLabel() {
        String dropOffLabel = "Dropoff: ";
            
            int dropOffVal = sldDropoff.getValue();
            
            if (dropOffVal >= 40) {
                dropOffLabel += "Fast";
            } else if (dropOffVal > 30) {
                dropOffLabel += "Medium";
            } else if (dropOffVal > 15) {
                dropOffLabel += "Slow";
            } else if (dropOffVal > 0) {
                dropOffLabel += "Molasses";
            } else {
                dropOffLabel += "Equiprobable";
            }
            
            lblDropoff.setText(dropOffLabel);
    }
    
    private void setMonosyllableLabel() {
        String monoSylLabel = "Monosylable Frequency: ";
            
            int monoSylVal = sldMonoSyllables.getValue();
            
            if (monoSylVal == 85) {
                monoSylLabel += "Always";
            } else if (monoSylVal > 50) {
                monoSylLabel += "Mostly";
            } else if (monoSylVal > 20) {
                monoSylLabel += "Frequent";
            } else if (monoSylVal > 7) {
                monoSylLabel += "Less Frequent";
            } else {
                monoSylLabel += "Rare";
            }
            
            lblMonoSyllables.setText(monoSylLabel);
    }
    
    private void setupListeners() {
        sldDropoff.addChangeListener((ChangeEvent e) -> {
            setDropoffLabel();
        });
        
        sldMonoSyllables.addChangeListener((ChangeEvent e) -> {
            setMonosyllableLabel();
        });
        
        cmbSwadesh.addActionListener((ActionEvent e) -> {
            var index = cmbSwadesh.getSelectedIndex();
            
            switch (index) {
                case -1 -> {
                    return;
                }
                case 0 -> {
                    tblSwadesh.setModel(new DefaultTableModel());
                    return;
                }
                case 1 -> {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setDialogTitle("Select line delimited Swadesh list.");

                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        try (InputStream is = new FileInputStream(file);  BufferedInputStream bs = new BufferedInputStream(is)) {
                            loadSwadesh(bs);
                        } catch (Exception ex) {
                            new DesktopInfoBox(this).error("Swadesh Load Error",
                                    "Could not load selected Swadesh List. Please make certain it is formatted correctly (newline separated): \n" 
                                            + ex.getLocalizedMessage());
                            DesktopIOHandler.getInstance().writeErrorLog(ex, "Swadesh load error");
                        }
                    }
                }
                default -> {
                    var swadObject = (SwadeshObject)cmbSwadesh.getSelectedItem();
                    var swadUrl = ScrMainMenu.class.getResource(swadObject.location);
                    try (BufferedInputStream bs = new BufferedInputStream(swadUrl.openStream())) {
                        loadSwadesh(bs);
                    } catch (Exception ex) {
                        new DesktopInfoBox(PolyGlot.getPolyGlot().getRootWindow()).error("Unable to load internal resource: ", swadObject.location);
                        DesktopIOHandler.getInstance().writeErrorLog(ex, "Resource read error on open.");
                    }
                }
            }
        });
        
        rdoGenWords.addActionListener((ActionEvent e) -> {
            setWordGenerationControlsEnables(true);
        });
        
        rdoGenSyllables.addActionListener((ActionEvent e) -> {
            setWordGenerationControlsEnables(false);
        });
        
        txtCategories.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistCategories(txtCategories.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistCategories(txtCategories.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistCategories(txtCategories.getText());
            }
        });
        
        txtIllegalClusters.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistIllegalClusters(txtIllegalClusters.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistIllegalClusters(txtIllegalClusters.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistIllegalClusters(txtIllegalClusters.getText());
            }
        });
        
        txtRewriteRules.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistRewriteRules(txtRewriteRules.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistRewriteRules(txtRewriteRules.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistRewriteRules(txtRewriteRules.getText());
            }
        });
        
        txtSyllableTypes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistSyllableTypes(txtSyllableTypes.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistSyllableTypes(txtSyllableTypes.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                core.getPropertiesManager().setZompistSyllableTypes(txtSyllableTypes.getText());
            }
        });
    }
    
    private void setWordGenerationControlsEnables(boolean enable) {
        chkShowSyllables.setEnabled(enable);
        chkSlowSyllableDropoff.setEnabled(enable);
        sldDropoff.setEnabled(enable);
        sldMonoSyllables.setEnabled(enable);
        txtGenerationNum.setEnabled(enable);
    }
    
    private void loadSwadesh (BufferedInputStream bs) throws IOException {
        try (var r = new BufferedReader(new InputStreamReader(bs, StandardCharsets.UTF_8))) {
            String[] columns = {"Swadesh Words"};
            var model = new DefaultTableModel(columns, 0);
            
            for (var line = r.readLine(); line != null; line = r.readLine()) {
                line = line.trim();
                if (line.startsWith("#")) {
                    // ignore comments
                    continue;
                }
                String[] column = {line};
                model.addRow(column);
            }
            
            tblSwadesh.setModel(model);
            var cellEditor = new PCellEditor(false);
            cellEditor.getComponent().addFocusListener(new FocusListener(){
                @Override
                public void focusGained(FocusEvent e) {
                    // do nothing
                }

                @Override
                public void focusLost(FocusEvent e) {
                    cellEditor.stopCellEditing();
                }
            });
            tblSwadesh.getColumnModel().getColumn(0).setCellEditor(cellEditor);
        }
    }
    
    private void genWords() {
        try {
            String[] columns = {"Generated Words"};
            var model = new DefaultTableModel(columns, 0);
            var words = getGenerator().genWords(Integer.parseInt(txtGenerationNum.getText()));

            for (var word : words) {
                String[] column = {word};
                model.addRow(column);
            }

            model.addTableModelListener((TableModelEvent e) -> {
                tableValuesUpdated();
            });
            tableValuesUpdated();
            tblGeneratedValues.setModel(model);
            tableValuesUpdated();
            var cellEditor = new PCellEditor(true);
            cellEditor.getComponent().addFocusListener(new FocusListener(){
                @Override
                public void focusGained(FocusEvent e) {
                    // do nothing
                }

                @Override
                public void focusLost(FocusEvent e) {
                    cellEditor.stopCellEditing();
                }
            });
            tblGeneratedValues.getColumnModel().getColumn(0).setCellEditor(cellEditor);
        } catch (Exception e) {
            // e.printStackTrace();
            new DesktopInfoBox().error("Generation error:", e.getLocalizedMessage());
        }
    }
    
    private void genSyllables() {
        try {
            String[] columns = {"Generated Syllables"};
            var model = new DefaultTableModel(columns, 0);
            var syllables = getGenerator().genAllSyllables();

            for (var syllable : syllables) {
                String[] column = {syllable};
                model.addRow(column);
            }

            model.addTableModelListener((TableModelEvent e) -> {
                tableValuesUpdated();
            });
            tableValuesUpdated();
            tblGeneratedValues.setModel(model);
            tableValuesUpdated();
            var cellEditor = new PCellEditor(true);
            cellEditor.getComponent().addFocusListener(new FocusListener(){
                @Override
                public void focusGained(FocusEvent e) {
                    // do nothing
                }

                @Override
                public void focusLost(FocusEvent e) {
                    cellEditor.stopCellEditing();
                }
            });
            tblGeneratedValues.getColumnModel().getColumn(0).setCellEditor(cellEditor);
        } catch (Exception e) {
            new DesktopInfoBox().error("Generation error:", e.getLocalizedMessage());
        }
    }
    
    private void tableValuesUpdated() {
        txtItemCount.setText(Integer.toString(tblGeneratedValues.getModel().getRowCount()));
    }
    
    private ZompistVocabGenerator getGenerator() throws Exception {
        int monoSylVal = sldMonoSyllables.getValue() == 85 ? 0 : sldMonoSyllables.getValue();

        return new ZompistVocabGenerator(
                chkSlowSyllableDropoff.isSelected(),
                chkShowSyllables.isSelected(),
                ((float)monoSylVal)/100, 
                sldDropoff.getValue(),
                txtCategories.getText(),
                txtSyllableTypes.getText(),
                txtRewriteRules.getText(),
                txtIllegalClusters.getText(),
                core.getOSHandler()
        );
    }
    
    private void populateSwadeshList() {
        var model = new DefaultComboBoxModel<SwadeshObject>();
        
        model.addElement(new SwadeshObject("none", ""));
        model.addElement(new SwadeshObject("Custom...", ""));
        
        for (String list : PGTUtil.SWADESH_LISTS) {
            // ignore coments
            if (list.startsWith("#")) {
                continue;
            }
            
            model.addElement(new SwadeshObject(list.replace("_", " "), PGTUtil.SWADESH_LOCATION + list));
        }
        
        cmbSwadesh.setModel(model);
    }
    
    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void saveAllValues() {
        // nothing to do here
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // nothing to do here
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        // nothing to do here
    }

    @Override
    public Component getWindow() {
        return pnlTop;
    }
    
    public class SwadeshObject {
        public final String label;
        public final String location;
        
        public SwadeshObject(String _label, String _location) {
            label = _label;
            location = _location;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    private void deleteSelectedWord() {
        int row = tblGeneratedValues.getSelectedRow();
        
        if (tblGeneratedValues.getCellEditor() != null) {
            tblGeneratedValues.getCellEditor().stopCellEditing();
        }
        
        if (row != -1) {
            ((DefaultTableModel)tblGeneratedValues.getModel()).removeRow(row);
            
            if (row > 0) {
                tblGeneratedValues.setRowSelectionInterval(row - 1, row - 1);
            } else if (tblGeneratedValues.getModel().getRowCount() > 0) {
                tblGeneratedValues.setRowSelectionInterval(0, 0);
            }
        }
    }
    
    private void addWord(String word) {
        int row = tblGeneratedValues.getSelectedRow();
        
        if (tblGeneratedValues.getCellEditor() != null) {
            tblGeneratedValues.getCellEditor().stopCellEditing();
        }
        
        String[] newRow = {word};
        ((DefaultTableModel)tblGeneratedValues.getModel()).insertRow(row + 1, newRow);
        tblGeneratedValues.setRowSelectionInterval(row + 1, row + 1);
    }
    
    private void deleteSelectedSwasesh() {
        int row = tblSwadesh.getSelectedRow();
        
        if (tblSwadesh.getCellEditor() != null) {
            tblSwadesh.getCellEditor().stopCellEditing();
        }
        
        if (row != -1) {
            ((DefaultTableModel)tblSwadesh.getModel()).removeRow(row);
            
            if (row > 0) {
                tblSwadesh.setRowSelectionInterval(row - 1, row - 1);
            } else if (tblSwadesh.getModel().getRowCount() > 0) {
                tblSwadesh.setRowSelectionInterval(0, 0);
            }
        }
    }
    
    private void addSwadesh(String word) {
        int row = tblSwadesh.getSelectedRow();
        
        if (tblSwadesh.getCellEditor() != null) {
            tblSwadesh.getCellEditor().stopCellEditing();
        }
        
        String[] newRow = {word};
        ((DefaultTableModel)tblSwadesh.getModel()).insertRow(row + 1, newRow);
        tblSwadesh.setRowSelectionInterval(row + 1, row + 1);
    }
    
    private void copyToImport() {
        var word = new ConWord();
        var wordIndex = tblGeneratedValues.getSelectedRow();
        var swadeshIndex = tblSwadesh.getSelectedRow();
        
        if (wordIndex == -1) {
            new DesktopInfoBox(this).warning("No Word Selected", "Please select a generated word value.");
            return;
        }

        // do not import syllable breaks
        var conValue = ((String)tblGeneratedValues.getValueAt(wordIndex, 0)).replaceAll(ZompistVocabGenerator.INTERPUNCT, "");
        word.setValue(conValue);
        deleteSelectedWord();
        
        if (swadeshIndex != -1) {
            word.setLocalWord((String)tblSwadesh.getValueAt(swadeshIndex, 0));
            deleteSelectedSwasesh();
        }
        
        var displayWord = new ConWordDisplay(word, core);
        ((DefaultListModel)lstImport.getModel()).addElement(displayWord);
        lstImport.ensureIndexIsVisible(lstImport.getModel().getSize() - 1);
    }
    
    private void copyFromImport() {
        var wordIndex = lstImport.getSelectedIndex();
        
        if (wordIndex == -1) {
            new DesktopInfoBox(this).warning("No Word Selected", "No word selected to move back to values/swadesh lists.");
            return;
        }
        var word = (ConWordDisplay)((DefaultListModel)lstImport.getModel()).get(wordIndex);
        
        addWord(word.getConWord().getValue());
        
        var swadesh = word.getConWord().getLocalWord();
        if (!swadesh.isBlank()) {
            addSwadesh(swadesh);
        }
        
        ((DefaultListModel)lstImport.getModel()).remove(wordIndex);
        
        if (wordIndex > 0) {
            lstImport.setSelectedIndex(wordIndex - 1);
        } else {
            lstImport.setSelectedIndex(0);
        }
    }
    
    private void setEnableWordImport(boolean enable) {
        if (!enable) {
            cmbSwadesh.setSelectedIndex(0);
            ((DefaultListModel)lstImport.getModel()).clear();
        }
        
        cmbSwadesh.setEnabled(enable);
        tblSwadesh.setEnabled(enable);
        btnAddSwadesh.setEnabled(enable);
        btnDelSwadesh.setEnabled(enable);
        btnCopyToImport.setEnabled(enable);
        btnCopyFromImport.setEnabled(enable);
        lstImport.setEnabled(enable);
        btnImport.setEnabled(enable);
        btnImport.setText("Import Words");
    }
    
    private void setEnableSyllableImport(boolean enable) {
        setEnableWordImport(false);
        btnImport.setText("Import Syllables");
        btnImport.setEnabled(enable);
    }
    
    private void generateValues() {
        pnlTop.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setEnableMenuButtons(false);
        isWordImport = rdoGenWords.isSelected();
        
        SwingUtilities.invokeLater(() ->{
            if (isWordImport) {
                genWords();
                setEnableWordImport(true);
            } else {
                genSyllables();
                setEnableSyllableImport(true);
            }
        
            pnlTop.setCursor(Cursor.getDefaultCursor());
            setEnableMenuButtons(true);
        });
    }
    
    private void setEnableMenuButtons(boolean enable) {
        btnClear.setEnabled(enable);
        btnDefaults.setEnabled(enable);
        btnGenerate.setEnabled(enable);
        btnHelp.setEnabled(enable);
        btnIpa.setEnabled(enable);
        btnSampleText.setEnabled(enable);
    }
    
    private void importValues() {
        if (isWordImport) {
            importValuesWords();
        } else {
            importValuesSyllables();
        }
    }
    
    private void importValuesWords() {
        var valueCount = lstImport.getModel().getSize();
        
        if (valueCount <= 0) {
            new DesktopInfoBox(this).warning("No Values to Import", "Please pull some values to import into the import list first!");
            return;
        }
        
        var choice = new DesktopInfoBox(this).yesNoCancel("Import Words?", "Import " + valueCount + " words into your language?");
        
        if (choice == JOptionPane.YES_OPTION) {
            var model = lstImport.getModel();

            try {
                for (int i = 0; i < model.getSize(); i++) {
                    var word = model.getElementAt(i).getConWord();
                    word.setCore(core);
                    core.getWordCollection().addNode(word);
                }
            } catch (Exception e) {
                new DesktopInfoBox(this).error("Import Error", "Unable to import values!\n" + e.getLocalizedMessage());
            }

            PolyGlot.getPolyGlot().getRootWindow().changeToLexicon();
        }
    }
    
    private void importValuesSyllables() {
        if (core.getPronunciationMgr().isRecurse()) {
            new DesktopInfoBox(this).warning("Unable to Import", "Please disable Recurse Patterns under "
                    + "Phonemic Orthography to import syllables.\nThe features are incompatible).");
            return;
        }
        
        var valueCount = tblGeneratedValues.getModel().getRowCount();
        
        if (valueCount <= 0) {
            new DesktopInfoBox(this).warning("No Values to Import", "Please pull some values to import into the import list first!");
            return;
        }
        
        var choice = new DesktopInfoBox(this).yesNoCancel("Import Syllables?", "Import " + valueCount + " syllables into your language?\n" 
                + "This will populate syllable composition and enable the feature when generating pronunciations.");
        
        if (choice == JOptionPane.YES_OPTION) {
            var procMan = core.getPronunciationMgr();
            var model = tblGeneratedValues.getModel();
            var rowCount = model.getRowCount();

            procMan.clearSyllables();
            procMan.setSyllableCompositionEnabled(true);
            for (int i = 0; i < rowCount; i++) {
                procMan.addSyllable((String)model.getValueAt(i, 0));
            }

            new DesktopInfoBox().info("Syllable Import", "Imported " + rowCount
                    + " syllable values. Syllable composition in pronunciation generation activated.");
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpValueChoice = new javax.swing.ButtonGroup();
        pnlTop = new javax.swing.JPanel();
        btnImport = new PButton();
        jPanel3 = new javax.swing.JPanel();
        btnAddWord = new PAddRemoveButton("+");
        btnDelWord = new PAddRemoveButton("-");
        jScrollPane4 = new javax.swing.JScrollPane();
        tblGeneratedValues = new PTable();
        txtItemCount = new PTextField("Item Count");
        jLabel5 = new PLabel();
        cmbSwadesh = new PComboBox<SwadeshObject>(false);
        jScrollPane7 = new javax.swing.JScrollPane();
        lstImport = new PListLexicon(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon());
        jLabel6 = new PLabel();
        btnCopyToImport = new PButton();
        btnCopyFromImport = new PButton();
        btnAddSwadesh = new PAddRemoveButton("+");
        btnDelSwadesh = new PAddRemoveButton("-");
        jScrollPane8 = new javax.swing.JScrollPane();
        tblSwadesh = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        rdoGenWords = new PRadioButton();
        rdoGenSyllables = new PRadioButton();
        chkShowSyllables = new PCheckBox(false, PolyGlot.getPolyGlot().getOptionsManager().getMenuFontSize());
        chkSlowSyllableDropoff = new PCheckBox(false, PolyGlot.getPolyGlot().getOptionsManager().getMenuFontSize());
        jPanel5 = new javax.swing.JPanel();
        lblDropoff = new PLabel();
        sldDropoff = new javax.swing.JSlider();
        lblMonoSyllables = new PLabel();
        sldMonoSyllables = new javax.swing.JSlider();
        txtGenerationNum = new PTextField("Target");
        jPanel6 = new javax.swing.JPanel();
        btnGenerate = new PButton();
        btnClear = new PButton();
        btnHelp = new PButton();
        btnIpa = new PButton();
        btnDefaults = new PButton();
        btnSampleText = new PButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtSyllableTypes = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtRewriteRules = new javax.swing.JTextArea();
        jLabel1 = new PLabel();
        jLabel2 = new PLabel();
        jLabel3 = new PLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtCategories = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        txtIllegalClusters = new javax.swing.JTextArea();
        jLabel4 = new PLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Lexical Generator");
        setMinimumSize(new java.awt.Dimension(620, 682));

        btnImport.setText("Import");
        btnImport.setToolTipText("Import words in the import list to your language");
        btnImport.setEnabled(false);
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnAddWord.setToolTipText("Add value");
        btnAddWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddWordActionPerformed(evt);
            }
        });

        btnDelWord.setToolTipText("Delete value");
        btnDelWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelWordActionPerformed(evt);
            }
        });

        tblGeneratedValues.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblGeneratedValues.setToolTipText("Values generated: values can be edited, deleted, or created");
        jScrollPane4.setViewportView(tblGeneratedValues);

        txtItemCount.setEditable(false);
        txtItemCount.setText("0");
        txtItemCount.setToolTipText("Count of values generated");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Swadesh List");
        jLabel5.setToolTipText("Loads a Swdesh list to match newly generated words with");

        cmbSwadesh.setToolTipText("Swadesh lists that may be imported to match generated words with for import");

        lstImport.setToolTipText("List of words to import into your language");
        jScrollPane7.setViewportView(lstImport);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Words to Import");

        btnCopyToImport.setText(">");
        btnCopyToImport.setToolTipText("Create a word to be imported into your language from a generated value paired with a swadesh list value (or alone)");
        btnCopyToImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCopyToImportActionPerformed(evt);
            }
        });

        btnCopyFromImport.setText("<");
        btnCopyFromImport.setToolTipText("Remove a word from the import list- generated and Swdesh list values returned to their lists as unused");
        btnCopyFromImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCopyFromImportActionPerformed(evt);
            }
        });

        btnAddSwadesh.setToolTipText("Add value");
        btnAddSwadesh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddSwadeshActionPerformed(evt);
            }
        });

        btnDelSwadesh.setToolTipText("Delete value");
        btnDelSwadesh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelSwadeshActionPerformed(evt);
            }
        });

        tblSwadesh.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblSwadesh.setToolTipText("Imported Swadesh values: Can be edited, deleted, or added to");
        tblSwadesh.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane8.setViewportView(tblSwadesh);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnAddWord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(98, 98, 98)
                        .addComponent(btnDelWord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtItemCount)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbSwadesh, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(btnAddSwadesh, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
                        .addComponent(btnDelSwadesh, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCopyToImport, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCopyFromImport, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane7))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtItemCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddWord)
                            .addComponent(btnDelWord)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(cmbSwadesh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(btnCopyToImport)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCopyFromImport)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAddSwadesh, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnDelSwadesh, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        grpValueChoice.add(rdoGenWords);
        rdoGenWords.setSelected(true);
        rdoGenWords.setText("Generate Words");
        rdoGenWords.setToolTipText("Generate words based on your rules above");

        grpValueChoice.add(rdoGenSyllables);
        rdoGenSyllables.setText("Generate All Possible Syllables");
        rdoGenSyllables.setToolTipText("Generates every possible combination of syllable (might be very large!)");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdoGenWords)
                    .addComponent(rdoGenSyllables))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(rdoGenWords)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdoGenSyllables))
        );

        chkShowSyllables.setText("Show Syllables");
        chkShowSyllables.setToolTipText("Include syllable break character in generated words");

        chkSlowSyllableDropoff.setText("Slow Syllable Dropoff");
        chkSlowSyllableDropoff.setToolTipText("Applies slower dropoff rate to syllables");

        lblDropoff.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDropoff.setText("Dropoff");

        sldDropoff.setMaximum(45);
        sldDropoff.setToolTipText("In your categories, letters that come earlier are more likely. This slider affects how MUCH more likely early letters are.");
        sldDropoff.setValue(0);

        lblMonoSyllables.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMonoSyllables.setText("Monosyllables");

        sldMonoSyllables.setMaximum(85);
        sldMonoSyllables.setMinimum(1);
        sldMonoSyllables.setToolTipText("Controls the liklihood that any given word will be a single syllable");
        sldMonoSyllables.setValue(0);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDropoff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(lblMonoSyllables, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sldDropoff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sldMonoSyllables, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(lblDropoff)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sldDropoff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblMonoSyllables)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sldMonoSyllables, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        txtGenerationNum.setToolTipText("Target number of words to generate");

        btnGenerate.setText("Generate Values");
        btnGenerate.setToolTipText("Generate values below based on your rules");
        btnGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateActionPerformed(evt);
            }
        });

        btnClear.setText("Clear Values");
        btnClear.setToolTipText("Clear Generated Values");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnHelp.setText("Help");
        btnHelp.setToolTipText("Open Relevant Doumentation to Zompist Generator");

        btnIpa.setText("IPA Chart");
        btnIpa.setToolTipText("Open the interactive IPA chart");
        btnIpa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIpaActionPerformed(evt);
            }
        });

        btnDefaults.setText("Defaults");
        btnDefaults.setToolTipText("Cycle betweeen different defaut setups");
        btnDefaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDefaultsActionPerformed(evt);
            }
        });

        btnSampleText.setText("Sample Text");
        btnSampleText.setToolTipText("Generates random sample text. Zero grammar, just what a sampling of your words might look like in text.");
        btnSampleText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSampleTextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(btnGenerate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClear)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnIpa)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDefaults)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnHelp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSampleText)
                .addContainerGap(43, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGenerate)
                    .addComponent(btnClear)
                    .addComponent(btnHelp)
                    .addComponent(btnIpa)
                    .addComponent(btnDefaults)
                    .addComponent(btnSampleText))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkShowSyllables)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(txtGenerationNum))
                        .addComponent(chkSlowSyllableDropoff)))
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkShowSyllables)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkSlowSyllableDropoff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtGenerationNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        txtSyllableTypes.setColumns(20);
        txtSyllableTypes.setLineWrap(true);
        txtSyllableTypes.setRows(5);
        txtSyllableTypes.setToolTipText("Syllable types, constructed from categories");
        txtSyllableTypes.setWrapStyleWord(true);
        txtSyllableTypes.setMaximumSize(new java.awt.Dimension(170, 2147483647));
        jScrollPane1.setViewportView(txtSyllableTypes);

        txtRewriteRules.setColumns(20);
        txtRewriteRules.setLineWrap(true);
        txtRewriteRules.setRows(5);
        txtRewriteRules.setToolTipText("Rewrite rules: (accepts regex)");
        txtRewriteRules.setWrapStyleWord(true);
        txtRewriteRules.setMaximumSize(new java.awt.Dimension(170, 2147483647));
        jScrollPane2.setViewportView(txtRewriteRules);

        jLabel1.setText("Categories");

        jLabel2.setText("Rewrite Rules");

        jLabel3.setText("Syllable Types");

        txtCategories.setColumns(20);
        txtCategories.setRows(5);
        txtCategories.setToolTipText("Word category definitions");
        jScrollPane3.setViewportView(txtCategories);

        txtIllegalClusters.setColumns(20);
        txtIllegalClusters.setLineWrap(true);
        txtIllegalClusters.setRows(5);
        txtIllegalClusters.setToolTipText("Illegal clusters: words with these clusters will not be generated.");
        txtIllegalClusters.setWrapStyleWord(true);
        jScrollPane5.setViewportView(txtIllegalClusters);

        jLabel4.setText("Illegal Clusters");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlTopLayout = new javax.swing.GroupLayout(pnlTop);
        pnlTop.setLayout(pnlTopLayout);
        pnlTopLayout.setHorizontalGroup(
            pnlTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTopLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnImport))
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlTopLayout.setVerticalGroup(
            pnlTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTopLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnImport))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlTop, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
        generateValues();
    }//GEN-LAST:event_btnGenerateActionPerformed

    private void btnDefaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDefaultsActionPerformed
        setDefaultValues();
    }//GEN-LAST:event_btnDefaultsActionPerformed

    private void btnIpaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIpaActionPerformed
        PFrame ipa = new ScrIPARefChart(core);
        ipa.setVisible(true);
        ipa.toFront();
    }//GEN-LAST:event_btnIpaActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        tblGeneratedValues.setModel(new DefaultTableModel());
        tableValuesUpdated();
        cmbSwadesh.setSelectedIndex(0);
        ((DefaultListModel)lstImport.getModel()).clear();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnDelWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelWordActionPerformed
        deleteSelectedWord();
    }//GEN-LAST:event_btnDelWordActionPerformed

    private void btnAddWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddWordActionPerformed
        addWord("");
    }//GEN-LAST:event_btnAddWordActionPerformed

    private void btnAddSwadeshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddSwadeshActionPerformed
        addSwadesh("");
    }//GEN-LAST:event_btnAddSwadeshActionPerformed

    private void btnDelSwadeshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelSwadeshActionPerformed
        deleteSelectedSwasesh();
    }//GEN-LAST:event_btnDelSwadeshActionPerformed

    private void btnCopyToImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCopyToImportActionPerformed
        copyToImport();
    }//GEN-LAST:event_btnCopyToImportActionPerformed

    private void btnCopyFromImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCopyFromImportActionPerformed
        copyFromImport();
    }//GEN-LAST:event_btnCopyFromImportActionPerformed

    private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
        importValues();
    }//GEN-LAST:event_btnImportActionPerformed

    private void btnSampleTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSampleTextActionPerformed
        try {
            var generator = this.getGenerator();
            new ScrSimpleTextDisplay(core, "Generated Sentences Example", generator.createText(), false)
                    .setVisible(true);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("Text Generation Error", "Unable to generate text: " + e.getLocalizedMessage());
        }
    }//GEN-LAST:event_btnSampleTextActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddSwadesh;
    private javax.swing.JButton btnAddWord;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnCopyFromImport;
    private javax.swing.JButton btnCopyToImport;
    private javax.swing.JButton btnDefaults;
    private javax.swing.JButton btnDelSwadesh;
    private javax.swing.JButton btnDelWord;
    private javax.swing.JButton btnGenerate;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnIpa;
    private javax.swing.JButton btnSampleText;
    private javax.swing.JCheckBox chkShowSyllables;
    private javax.swing.JCheckBox chkSlowSyllableDropoff;
    private javax.swing.JComboBox<SwadeshObject> cmbSwadesh;
    private javax.swing.ButtonGroup grpValueChoice;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JLabel lblDropoff;
    private javax.swing.JLabel lblMonoSyllables;
    private javax.swing.JList<ConWordDisplay> lstImport;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JRadioButton rdoGenSyllables;
    private javax.swing.JRadioButton rdoGenWords;
    private javax.swing.JSlider sldDropoff;
    private javax.swing.JSlider sldMonoSyllables;
    private javax.swing.JTable tblGeneratedValues;
    private javax.swing.JTable tblSwadesh;
    private javax.swing.JTextArea txtCategories;
    private javax.swing.JTextField txtGenerationNum;
    private javax.swing.JTextArea txtIllegalClusters;
    private javax.swing.JTextField txtItemCount;
    private javax.swing.JTextArea txtRewriteRules;
    private javax.swing.JTextArea txtSyllableTypes;
    // End of variables declaration//GEN-END:variables
}
