/*
 * Copyright (c) 2015-2020, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.CustomControls.PButton;
import org.darisadesigns.polyglotlina.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.CustomControls.PComboBox;
import org.darisadesigns.polyglotlina.CustomControls.PFocusTraversalPolicy;
import org.darisadesigns.polyglotlina.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.CustomControls.PListLexicon;
import org.darisadesigns.polyglotlina.CustomControls.PListModelLexicon;
import org.darisadesigns.polyglotlina.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.CustomControls.PTextPane;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection.ConWordDisplay;
import org.darisadesigns.polyglotlina.Nodes.EtyExternalParent;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.Nodes.WordClassValue;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import org.darisadesigns.polyglotlina.PFontHandler;
import org.darisadesigns.polyglotlina.WebInterface;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.darisadesigns.polyglotlina.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.PGTUtil;

/**
 *
 * @author draque
 */
public final class ScrLexicon extends PFrame {

    private static final String FILTER_LABEL = "Lexicon Filter";
    private ScrLogoQuickView logoQuick = null;
    private final Map<Integer, JComponent> classPropMap = new HashMap<>();
    private TitledPane gridTitlePane = null;
    private CheckBox chkFindBad;
    private final JFXPanel fxPanel;
    private final TypeNode defTypeValue = new TypeNode();
    private final EtyExternalParent defRootValue = new EtyExternalParent();
    private final static String DEF_LEX_VALUE = "List of Conlang Words";
    private TextField txtConSrc;
    private TextField txtLocalSrc;
    private TextField txtProcSrc;
    private TextField txtDefSrc;
    private ComboBox<Object> cmbTypeSrc;
    private ComboBox<Object> cmbRootSrc;
    private boolean curPopulating = false;
    private boolean namePopulating = false;
    private boolean forceUpdate = false;
    private Thread filterThread = null;
    private final ScrMainMenu menuParent;
    private final PTextField txtRom;
    private boolean enableProcGen = true;

    /**
     * Creates new form scrLexicon
     *
     * @param _core Dictionary Core
     * @param _menuParent
     */
    public ScrLexicon(DictCore _core, ScrMainMenu _menuParent) {
        super(_core);
        
        defTypeValue.setValue("-- Part of Speech --");
        defTypeValue.setId(-1);

        defRootValue.setValue("-- Root --");
        defRootValue.setId(-1);

        menuParent = _menuParent;
        fxPanel = new JFXPanel();
        txtRom = new PTextField(core, true, "-- Romanization --");
        txtRom.setToolTipText("Romanized representation of word");
        initComponents();

        lstLexicon.setModel(new PListModelLexicon());

        setupFilterMenu();
        setupComboBoxesSwing();
        setDefaultValues();
        populateLexicon();
        lstLexicon.setSelectedIndex(0);
        populateProperties();
        setupListeners();
        setCustomLabels();
        setupForm();
    }
    
    private void setupForm() {
        int divider = core.getOptionsManager().getDividerPosition(this.getClass().getName());
        
        if (divider > -1) {
            jSplitPane1.setDividerLocation(divider);
        }
    }

    @Override
    public Component getWindow() {
        return jLayeredPane1;
    }

    private void setCustomLabels() {
        if (System.getProperty("os.name").startsWith("Mac")) {
            btnAddWord.setToolTipText(btnAddWord.getToolTipText() + " (Option +)");
            btnDelWord.setToolTipText(btnDelWord.getToolTipText() + " (Option -)");
        } else {
            btnAddWord.setToolTipText(btnAddWord.getToolTipText() + " (CTRL +)");
            btnDelWord.setToolTipText(btnDelWord.getToolTipText() + " (CTRL -)");
        }

        txtConWord.setToolTipText(core.conLabel() + " word value");
        txtLocalWord.setToolTipText(core.localLabel() + " word value");
    }

    /**
     * forces refresh of word list
     *
     * @param wordId id of newly word to select (-1 if no selection)
     */
    public void refreshWordList(int wordId) {
        populateLexicon();
        if (wordId != -1) {
            lstLexicon.setSelectedValue(
                    core.getWordCollection().getNodeById(wordId), true);
        }
    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void updateAllValues(final DictCore _core) {
        // ensure this is on the UI component stack to avoid read/writelocks...
        Runnable runnable = () -> {
            boolean localPopulating = curPopulating;
            curPopulating = true;
            forceUpdate = true;

            if (core != _core) {
                core = _core;
            }
            
            // ensure same value is selected post update
            ConWordDisplay selectedWord = lstLexicon.getSelectedValue();
            
            lstLexicon.setModel(new PListModelLexicon());
            setDefaultValues();
            populateLexicon();
            
            if (selectedWord != null) {
                lstLexicon.setSelectedValue(selectedWord, forceUpdate);
            } else {
                lstLexicon.setSelectedIndex(0);
            }
            
            setupComboBoxesSwing();
            populateProperties();
                
            Runnable fxSetup = () -> {
                setupComboBoxesFX();
            };
            Platform.setImplicitExit(false);
            wrapPlatformRunnable(fxSetup);

            ConWord curWord = getCurrentWord();
            saveValuesTo(curWord);
            Font listFont = core.getPropertiesManager().isUseLocalWordLex() ?
                    core.getPropertiesManager().getFontLocal() :
                    core.getPropertiesManager().getFontCon();
            lstLexicon.setFont(listFont);
            setupComboBoxesSwing();
            curPopulating = localPopulating;
            forceUpdate = false;
            populateProperties();
            ((PTextField) txtConWord).setCore(_core);
            ((PTextField)txtLocalWord).setCore(_core);
            ((PTextField)txtProc).setCore(_core);
            ((PTextPane)txtDefinition).setCore(_core);
            
            // ensures multiple logograph screens can't be open at once
            btnLogographs.setEnabled(btnLogoShouldEnable());
        };
        SwingUtilities.invokeLater(runnable);
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        Action addAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addWord();
            }
        };
        Action delAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteWord();
            }
        };
        String addKey = "addWord";
        String delKey = "delWord";
        int mask;
        if (System.getProperty("os.name").startsWith("Mac")) {
            mask = KeyEvent.META_DOWN_MASK;
        } else {
            mask = KeyEvent.CTRL_DOWN_MASK;
        }
        InputMap im = c.getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | mask), addKey);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | mask), delKey);
        ActionMap am = c.getActionMap();
        am.put(addKey, addAction);
        am.put(delKey, delAction);
    }

    private void populateClassPanel() {
        ConWord curWord = getCurrentWord();

        curWord.getClassValues().stream()
                .filter((curProp) -> (classPropMap.containsKey(curProp.getKey())))
                .forEachOrdered((curProp) -> {
                    JComponent component = classPropMap.get(curProp.getKey());

                    try {
                        if (component instanceof JComboBox) {
                            JComboBox combo = (JComboBox) component;
                            combo.setSelectedItem(((WordClass) core.getWordClassCollection()
                                    .getNodeById(curProp.getKey())).getValueById(curProp.getValue()));
                        }
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        InfoBox.error("Word Class Error", "Unable to retrieve class/value pair "
                                + curProp.getKey() + "/" + curProp.getValue(), core.getRootWindow());
                    }
                });

        curWord.getClassTextValues().stream()
                .filter((curProp) -> (classPropMap.containsKey(curProp.getKey())))
                .forEachOrdered((curProp) -> {
                    JComponent component = classPropMap.get(curProp.getKey());

                    try {
                        if (component instanceof PTextField) {
                            PTextField textField = (PTextField) component;
                            textField.setText(curProp.getValue());
                        }
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        InfoBox.error("Word Class Error", "Unable to retrieve class/value pair "
                                + curProp.getKey() + "/" + curProp.getValue(), core.getRootWindow());
                    }
                });
    }

    /**
     * Sets up the romanization field. Should be run after setupClassPanel, as it utilizes the class panel space
     */
    private void setupRomField() {
        if (core.getRomManager().isEnabled()) {
            txtRom.setEditable(false);
            txtRom.setFocusable(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weighty = 1;
            gbc.weightx = 1;
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.BOTH;
            pnlClasses.add(txtRom, gbc);
            pnlClasses.setFocusTraversalPolicy(new PFocusTraversalPolicy());

            genRom();
            pnlClasses.repaint();
        }
    }

    /**
     * Sets up the class panel. Should be run whenever a new word is loaded
     *
     * @param setTypeId ID of class to set panel up for
     */
    private void setupClassPanel(int setTypeId) {
        ConWord curWord = getCurrentWord();

        pnlClasses.removeAll();

        // on no word selected, simply blank all classes
        if (curWord == null) {
            return;
        }

        WordClass[] propList = core.getWordClassCollection().getClassesForType(setTypeId);
        pnlClasses.setPreferredSize(new Dimension(4000, 1));

        pnlClasses.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;

        // empty map of all class information before filling it again
        classPropMap.clear();

        // create dropdown for each class that applies to the current word
        for (WordClass curProp : propList) {
            final int classId = curProp.getId();

            if (curProp.isFreeText()) {
                final PTextField classText = new PTextField(core, false, "--" + curProp.getValue() + "--");
                classText.setToolTipText(curProp.getValue() + " value");

                classText.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateWord();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateWord();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateWord();
                    }

                    public void updateWord() {
                        if (curPopulating) {
                            return;
                        }

                        ConWord curWord = getCurrentWord();
                        if (curWord != null) {
                            curWord.setClassTextValue(classId, classText.getText());
                        }
                    }
                });

                pnlClasses.add(classText, gbc);
                classPropMap.put(curProp.getId(), classText); // text box mapped to related class ID.
            } else {
                final JComboBox<Object> classBox = new PComboBox<>(core.getPropertiesManager().getFontMenu());
                DefaultComboBoxModel<Object> comboModel = new DefaultComboBoxModel<>();
                classBox.setModel(comboModel);
                comboModel.addElement("-- " + curProp.getValue() + " --");

                // populate class dropdown
                curProp.getValues().forEach((value) -> {
                    comboModel.addElement(value);
                });

                classBox.addActionListener((ActionEvent e) -> {
                    // don't run if populating currently
                    if (curPopulating) {
                        return;
                    }
                    ConWord curWord1 = getCurrentWord();
                    if (classBox.getSelectedItem() instanceof WordClassValue) {
                        WordClassValue curValue = (WordClassValue) classBox.getSelectedItem();
                        curWord1.setClassValue(classId, curValue.getId());
                    } else {
                        // if not an instance of a value, then it's the default selection: remove class from word
                        curWord1.setClassValue(classId, -1);
                    }
                });

                classBox.setToolTipText(curProp.getValue() + " value");
                classBox.setPreferredSize(new Dimension(4000, classBox.getPreferredSize().height));
                pnlClasses.add(classBox, gbc);
                classPropMap.put(curProp.getId(), classBox); // combobox mapped to related class ID.
            }

            if (menuParent != null) {
                // messy, but gets a full rebuild of screen since this is happening post-initial visibility-pop
                Dimension dim = menuParent.getSize();
                menuParent.setSize(dim.width, dim.height + 1);
                menuParent.setSize(dim.width, dim.height);
            }
        }

        if (propList.length == 0) {
            // must include at least one item (even a dummy) to resize for some reason
            JComboBox dummy = new JComboBox();
            dummy.setEnabled(false);
            dummy.setSize(1, 0);
            dummy.setVisible(false);
            pnlClasses.add(dummy, gbc);
            pnlClasses.setPreferredSize(new Dimension(4000, 0));
        } else {
            pnlClasses.setMaximumSize(new Dimension(4000, 4000));
            pnlClasses.setPreferredSize(new Dimension(4000, propList.length * new JComboBox().getPreferredSize().height));
        }

        pnlClasses.repaint();
    }

    /**
     * Sets up and drops the filter menu into the UI
     */
    private void setupFilterMenu() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = GridBagConstraints.RELATIVE;
        c.gridwidth = GridBagConstraints.RELATIVE;

        jPanel1.setLayout(new GridLayout());
        jPanel1.add(fxPanel, c);
        jPanel1.setBackground(Color.white);
        fxPanel.setBackground(Color.white);
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            fxPanel.setScene(createScene());
            setupComboBoxesFX();
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Form Load Error", "Unable to load Lexicon: " + e.getLocalizedMessage(), core.getRootWindow());
        }
        
        gridTitlePane.setTooltip(new Tooltip(FILTER_LABEL));
    }

    private Scene createScene() {
        Group root = new Group();
        Scene scene = new Scene(root);
        root.getChildren().add(createSearchPanel());

        return scene;
    }

    /**
     * Generates and populates pronunciation if appropriate
     */
    private void genProc() {
        if (curPopulating
                || chkProcOverride.isSelected()) {
            return;
        }

        boolean localPopulating = curPopulating;

        curPopulating = true;

        try {
            String setText = core.getPronunciationMgr().getPronunciation(txtConWord.getText());

            // avoid setting text if it comes back empty (unless word itself is now blank)
            if (!setText.isEmpty() || txtConWord.getText().isEmpty()) {
                txtProc.setText(setText);
            }
        } catch (Exception e) {
            // IOHandler.writeErrorLog(e);
            InfoBox.error("Pronunciation Error", "Could not generate pronunciation: "
                    + e.getLocalizedMessage(), core.getRootWindow());
        }

        curPopulating = localPopulating;
    }

    /**
     * generates
     */
    private void genRom() {
        if (enableProcGen) {
            SwingUtilities.invokeLater(() -> {
                try {
                    txtRom.setText(core.getRomManager().getPronunciation(txtConWord.getText()));
                } catch (Exception e) {
                    // simply disable pronunciation generation for now. user informed of error elsewhere.
                    enableProcGen = false;
                }
            });
        }
    }

    /**
     * Sets default values to all user editable fields
     */
    private void setDefaultValues() {
        chkProcOverride.setSelected(false);
        chkRuleOverride.setSelected(false);
        cmbType.setSelectedIndex(0);
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            txtConSrc.setText("");
            txtDefSrc.setText("");
            txtLocalSrc.setText("");
            txtProcSrc.setText("");
            cmbTypeSrc.getSelectionModel().select(0);
        });
    }

    /**
     * Runs filter on timed thread to avoid overabundance of filters and prevent filtering overlaps. Run this instead of
     * filterLexicon().
     */
    private void runFilter() {
        if (filterThread != null
                && filterThread.isAlive()) {
            filterThread.interrupt();
        }

        filterThread = new Thread(() -> {
            if (txtConWord.getText().isEmpty()
                    && lstLexicon.getSelectedIndex() != -1) {
                return; // prevents freezing scenario if new word made before thread continues
            }
            filterLexicon();
            lstLexicon.setSelectedIndex(0);
            lstLexicon.ensureIndexIsVisible(0);
            populateProperties();
        });

        Platform.runLater(filterThread::start);
        gridTitlePane.setExpanded(false);
    }
    
    /**
     * Closes and clears search panel on the top of the screen
     */
    public void closeAndClearSearchPanel() {
        Platform.runLater(() -> {
            try {
                clearFilter();
                gridTitlePane.setExpanded(false);
            } catch (Exception e) {
                // Not a huge deal to users. Just silently log.
                IOHandler.writeErrorLog(e);
            }
        });
    }
    
    /**
     * Tests whether filter is currently empty
     * If testing fields are null, they aren't yet initialized, and therefore must be blank
     * @return 
     */
    private boolean isFilterBlank() {
        boolean ret = true;
        if (txtConSrc != null 
                && txtLocalSrc != null 
                && txtProcSrc != null 
                && cmbRootSrc != null 
                && cmbRootSrc.getValue() != null) {
            int filterType = 0;
            if (cmbTypeSrc != null
                    && cmbTypeSrc.getValue() != null 
                    && cmbTypeSrc.getValue().equals(defTypeValue)) {
                filterType = ((TypeNode) cmbTypeSrc.getValue()).getId();
            }

            ret = txtConSrc.getText().isEmpty()
                    && txtDefSrc.getText().isEmpty()
                    && txtLocalSrc.getText().isEmpty()
                    && txtProcSrc.getText().isEmpty()
                    && filterType == defTypeValue.getId()
                    && cmbRootSrc.getValue().toString().equals(defRootValue.getValue());
        }
        
        return ret;
    }

    /**
     * Filters lexicon. Call RunFilter() instead of this, which runs on a timed session to prevent overlapping filters.
     */
    private void filterLexicon() {
        if (curPopulating) {
            return;
        }

        int filterType;

        if (cmbTypeSrc.getValue().equals(defTypeValue)) {
            filterType = 0;
        } else {
            filterType = ((TypeNode) cmbTypeSrc.getValue()).getId();
        }
        
        saveValuesTo(getCurrentWord());

        if (isFilterBlank()) {
            populateLexicon();
            lstLexicon.setSelectedIndex(0);
            lstLexicon.ensureIndexIsVisible(0);
            
            gridTitlePane.setTooltip(new Tooltip(FILTER_LABEL));
            gridTitlePane.setTextFill(javafx.scene.paint.Color.BLACK);

            // refresh lexicon if it was already filtered. Do nothing otherwise
            if (lstLexicon.getModel().getSize() < core.getWordCollection().getWordCount()) {
                populateLexicon();
                lstLexicon.setSelectedIndex(0);
                populateProperties();
            } else {
                return;
            }
        }
        
        gridTitlePane.setTooltip(new Tooltip(FILTER_LABEL + " ACTIVE"));
        gridTitlePane.setTextFill(javafx.scene.paint.Color.BLUEVIOLET);

        ConWord filter = new ConWord();

        filter.setValue(txtConSrc.getText().trim());
        filter.setDefinition(txtDefSrc.getText().trim());
        filter.setLocalWord(txtLocalSrc.getText().trim());
        filter.setWordTypeId(filterType);
        filter.setPronunciation(txtProcSrc.getText().trim());
        filter.setFilterEtyParent(cmbRootSrc.getValue());

        // save word before applying filter
        ConWord curWord = getCurrentWord();
        if (curWord != null) {
            saveValuesTo(curWord);
        }

        try {
            populateLexicon(core.getWordCollection().toDisplayList(
                    core.getWordCollection().filteredList(filter)));
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Filter Error", "Unable to apply filter.\n\n" + e.getMessage(), core.getRootWindow());
        }

        lstLexicon.setSelectedIndex(0);
        lstLexicon.ensureIndexIsVisible(0);
    }

    /**
     * Clears lexicon's search/filter
     * ALWAYS use within Platform.runLater to ensure this is run within a JFX thread
     */
    private void clearFilter() throws Exception {
        if (!Platform.isFxApplicationThread()) {
            throw new Exception("This method must be run within a JFX thread.");
        }
        
        // if no filter in effect, do nothing
        if (txtConSrc.getText().isEmpty()
                && txtDefSrc.getText().isEmpty()
                && txtLocalSrc.getText().isEmpty()
                && txtProcSrc.getText().isEmpty()
                && cmbTypeSrc.getSelectionModel().getSelectedIndex() == 0) {
            return;
        }

        txtConSrc.setText("");
        txtDefSrc.setText("");
        txtLocalSrc.setText("");
        txtProcSrc.setText("");
        cmbTypeSrc.getSelectionModel().select(0);
        SwingUtilities.invokeLater(this::populateLexicon);
    }

    /**
     * Sets currently displayed word's legality (highlighted fields, error message, etc.)
     */
    private void setWordLegality() {
        ConWord testWord = getCurrentWord();

        if (forceUpdate) {
            return;
        }

        if (testWord != null) {
            Integer origWordId = testWord.getId();
            testWord = new ConWord();
            testWord.setId(origWordId);
            int typeId = 0;
            Object selectedType = cmbType.getSelectedItem();
            if (selectedType != null && !selectedType.equals(defTypeValue)) {
                typeId = ((TypeNode) cmbType.getSelectedItem()).getId();
            }

            if (curPopulating) {
                return;
            }

            testWord.setValue(((PTextField) txtConWord).isDefaultText() ? "" : txtConWord.getText());
            testWord.setLocalWord(((PTextField) txtLocalWord).isDefaultText() ? "" : txtLocalWord.getText());
            testWord.setDefinition(txtDefinition.getText());
            testWord.setPronunciation(((PTextField) txtProc).isDefaultText() ? "" : txtProc.getText());
            testWord.setWordTypeId(typeId);
            testWord.setRulesOverride(chkRuleOverride.isSelected());
            testWord.setCore(core);
        }
        setWordLegality(testWord);
    }

    /**
     * Sets lexicon tab's currently displayed word legality (highlighted fields, error message, etc.)
     *
     * @param testWord current word
     */
    private void setWordLegality(ConWord testWord) {
        if (testWord == null) {
            setLexiconEnabled(true);
            txtErrorBox.setText("");
            return;
        }

        ConWord results = core.getWordCollection().testWordLegality(testWord);

        txtErrorBox.setText("");

        String procLegality = "";

        if (enableProcGen) {
            try {
                procLegality = results.getPronunciation();
            } catch (Exception e) {
                // IOHandler.writeErrorLog(e);
                enableProcGen = false;
                procLegality = e.getLocalizedMessage();
            }
        }

        boolean isLegal = addErrorBoxMessage(txtConWord, results.getValue());
        isLegal = isLegal && addErrorBoxMessage(txtLocalWord, results.getLocalWord());
        isLegal = isLegal && addErrorBoxMessage(txtProc, procLegality);
        isLegal = isLegal && addErrorBoxMessage(txtConWord, results.getDefinition());
        isLegal = isLegal && addErrorBoxMessage(cmbType, results.typeError);

        if (!testWord.isRulesOverride()
                && !chkFindBad.isSelected()) { // if looking for illegals, allow free movement
            setLexiconEnabled(isLegal);
        } else {
            setLexiconEnabled(true);
        }
    }

    /**
     * Adds error if any it error box and takes appropriate action to inform user
     *
     * @param element element related to checked value
     * @param message message (if any) returned as error
     * @return true if legal, false otherwise
     */
    private boolean addErrorBoxMessage(JComponent element, String message) {
        Color bColor = new JTextField().getBackground();
        Color hColor = PGTUtil.COLOR_REQUIRED_LEX_COLOR;
        boolean ret = true;

        if (message.isEmpty()) {
            element.setBackground(bColor);
        } else {
            if (!txtErrorBox.getText().isEmpty()) {
                txtErrorBox.setText(txtErrorBox.getText() + "\n");
            }

            txtErrorBox.setText(txtErrorBox.getText() + message);
            element.setBackground(hColor);
            if (element instanceof PComboBox) {
                PComboBox eleComb = (PComboBox) element;
                eleComb.makeFlash(hColor, false);
            }

            ret = false;
        }

        return ret;
    }

    /**
     * Sets whether user is able to select another entry in the lexicon
     *
     * @param enable true for enable, false for disable
     */
    private void setLexiconEnabled(boolean enable) {
        setFilterEnabled(enable);
        lstLexicon.setEnabled(enable);
        btnAddWord.setEnabled(enable);
    }

    /**
     * Sets whether user can modify the filter (does not clear filter)
     *
     * @param enable true for enable, false for disable
     */
    private void setFilterEnabled(final boolean enable) {
        Platform.runLater(() -> {
            txtConSrc.setDisable(!enable);
            txtDefSrc.setDisable(!enable);
            txtLocalSrc.setDisable(!enable);
            txtProcSrc.setDisable(!enable);
            cmbTypeSrc.setDisable(!enable);
            chkFindBad.setDisable(!enable);
        });
    }

    /**
     * creates JavaFX Search menu
     *
     * @return
     */
    private TitledPane createSearchPanel() {
        GridPane grid = new GridPane();
        javafx.scene.text.Font font = javafx.scene.text.Font.loadFont(new PFontHandler().getCharisInputStream(), core.getOptionsManager().getMenuFontSize());
        javafx.scene.text.Font conFont = core.getPropertiesManager().getFXFont();
        
        gridTitlePane = new TitledPane();
        gridTitlePane.setFont(font);

        grid.setPrefWidth(4000);
        txtConSrc = new TextField();
        txtConSrc.setPromptText("Search ConWord...");
        txtConSrc.setFont(conFont);
        txtConSrc.setTooltip(new Tooltip("Filter lexicon entries based on the value of your constructed words"));
        txtLocalSrc = new TextField();
        txtLocalSrc.setPromptText("Search NatLang Word...");
        txtLocalSrc.setFont(font);
        txtLocalSrc.setTooltip(new Tooltip("Filter lexicon entries based on the translated value of your language's words"));
        txtProcSrc = new TextField();
        txtProcSrc.setPromptText("Search by Pronunciation...");
        txtProcSrc.setFont(font);
        txtProcSrc.setTooltip(new Tooltip("Filter lexicon entries based on the pronunciation of your language's words"));
        txtDefSrc = new TextField();
        txtDefSrc.setPromptText("Search by Definition...");
        txtDefSrc.setFont(font);
        txtDefSrc.setTooltip(new Tooltip("Filter lexicon entries based on definition value of your language's words"));
        cmbTypeSrc = new ComboBox<>();
        cmbTypeSrc.setTooltip(new Tooltip("Filter lexicon entries based on their parts of speech"));
        chkFindBad = new CheckBox();
        chkFindBad.setFont(font);
        chkFindBad.setTooltip(new Tooltip("Filter lexicon entries to find words with illegal values"));
        chkFindBad.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            applyIllegalFilter();
        });
        cmbRootSrc = new ComboBox<>();
        cmbRootSrc.setCellFactory((ListView<Object> param) -> {
            final ListCell<Object> cell = new ListCell<Object>() {
                @Override
                public void updateItem(Object item,
                        boolean empty) {
                    super.updateItem(item, empty);
                    if (item instanceof ConWord || item instanceof ConWordDisplay) {
                        setFont(core.getPropertiesManager().getFXFont());
                        setText(item.toString());
                    } else if (item instanceof EtyExternalParent) {
                        setFont(font);
                        setText(item.toString());
                    } else {
                        setText(null);
                    }
                }
            };
            return cell;
        });

        grid.setVgap(4);
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.add(new Label("Con Word: "), 0, 0);
        grid.add(txtConSrc, 1, 0);
        grid.add(new Label("Local Word: "), 0, 1);
        grid.add(txtLocalSrc, 1, 1);
        grid.add(new Label("Part of Speech: "), 0, 2);
        grid.add(cmbTypeSrc, 1, 2);
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.add(new Label("            "), 2, 0); // adds spacing
        grid.add(new Label("Pronunciation: "), 3, 0);
        grid.add(txtProcSrc, 4, 0);
        grid.add(new Label("Definition: "), 3, 1);
        grid.add(txtDefSrc, 4, 1);
        grid.add(new Label("Root: "), 3, 2);
        grid.add(cmbRootSrc, 4, 2);
        grid.add(new Label("Illegals"), 0, 3);
        grid.add(chkFindBad, 1, 3);

        javafx.scene.control.Button srcButton = new javafx.scene.control.Button("Filter");
        srcButton.setOnAction((javafx.event.ActionEvent t) -> {
            runFilter();
        });
        grid.add(srcButton, 4, 3);
        
        // sets up button to clear filter
        javafx.scene.control.Button clearButton = new javafx.scene.control.Button("Clear Filter");
        clearButton.setOnAction((javafx.event.ActionEvent t) -> {
            clearFilterInternal();
            runFilter();
        });
        grid.add(clearButton, 4, 4);
        
        gridTitlePane.setText("Search/Filter");
        gridTitlePane.setContent(grid);
        gridTitlePane.setExpanded(false);

        return gridTitlePane;
    }

    /**
     * Should only be called from logic within the filter pane Does not close filter, and is guaranteed running inside
     * fxProcess, so no latch logic necessary.
     */
    private void clearFilterInternal() {
        Font localFont = core.getPropertiesManager().getFontLocal();
        txtConSrc.setText("");
        txtLocalSrc.setText("");
        txtProcSrc.setText("");
        txtDefSrc.setText("");
        cmbTypeSrc.getSelectionModel().select(defTypeValue);
        cmbRootSrc.getSelectionModel().select(defRootValue);
        cmbRootSrc.setStyle("-fx-font: "
                + localFont.getSize() + "px \""
                + localFont.getFamily() + "\";");
    }

    /**
     * Filters on illegal words. Does NOT respect "override" marker. This is to allow users to easily see what words are
     * causing uniqueness errors, even if they themselves are legal via exception.
     */
    private void applyIllegalFilter() {
        clearFilterInternal();

        txtConSrc.setDisable(chkFindBad.isSelected());
        txtDefSrc.setDisable(chkFindBad.isSelected());
        txtLocalSrc.setDisable(chkFindBad.isSelected());
        txtProcSrc.setDisable(chkFindBad.isSelected());
        cmbTypeSrc.setDisable(chkFindBad.isSelected());
        cmbRootSrc.setDisable(chkFindBad.isSelected());

        if (chkFindBad.isSelected()) {
            populateLexicon(core.getWordCollection().toDisplayList(
                    core.getWordCollection().illegalFilter()));
        } else {
            populateLexicon();
        }
    }

    @Override
    public void dispose() {
        if (this.canClose()) {
            saveAllValues();
            killLogoChild();
            core.getOptionsManager().setDividerPosition(getClass().getName(), jSplitPane1.getDividerLocation());
            super.dispose();
        }
    }
    
    @Override
    public void saveAllValues() {
        ConWord curWord = getCurrentWord();
        if (curWord != null) {
            saveValuesTo(curWord);
        }
    }

    /**
     * Closes all child windows
     */
    private void killLogoChild() {
        if (logoQuick != null && !logoQuick.isDisposed()) {
            logoQuick.dispose();
        }
    }

    public ConWord getCurrentWord() {
        ConWordDisplay retVal = lstLexicon.getSelectedValue();
        return retVal == null ? null : retVal.getConWord();
    }

    public void setWordSelectedById(int id) {
        lstLexicon.setSelectedValue(core.getWordCollection().getNodeById(id), true);
        setWordLegality();
    }

    /**
     * Sets up all document listeners
     */
    private void setupListeners() {
        gridTitlePane.heightProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            int contentHeight = newValue.intValue();
            jPanel1.setSize(jPanel1.getSize().width, contentHeight);
            fxPanel.setSize(fxPanel.getSize().width, contentHeight);
        });

        txtConWord.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                genProc();
                genRom();
                setWordLegality();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                genProc();
                genRom();
                setWordLegality();
                if (isFilterBlank()) {
                    // if filter is in place, do not trigger a rerender of the values
                    saveName();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                genProc();
                genRom();
                setWordLegality();
                if (isFilterBlank()) {
                    // if filter is in place, do not trigger a rerender of the values
                    saveName();
                }
            }
        });

        txtLocalWord.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setWordLegality();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setWordLegality();
                if (isFilterBlank()) {
                    // if filter is in place, do not trigger a rerender of the values
                    saveName();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setWordLegality();
                if (isFilterBlank()) {
                    // if filter is in place, do not trigger a rerender of the values
                    saveName();
                }
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (gridTitlePane.isExpanded()) {
                    gridTitlePane.setExpanded(false);
                }
            }
        });
        
        txtDefinition.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setWordLegality();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setWordLegality();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setWordLegality();
            }
        });
        
        txtProc.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setWordLegality();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setWordLegality();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setWordLegality();
            }
        });

        lstLexicon.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                JList theList = (JList) e.getSource();
                ListModel model = theList.getModel();
                int index = theList.locationToIndex(e.getPoint());
                if (index > -1) {
                    ConWord curWord = ((ConWordDisplay)model.getElementAt(index)).getConWord();
                    TypeNode curType = null;
                    try {
                        curType = core.getTypes().getNodeById(curWord.getWordTypeId());
                    } catch (Exception ex) {
                        IOHandler.writeErrorLog(ex);
                        InfoBox.error("Type error on lookup.", ex.getMessage(), core.getRootWindow());
                    }
                    String tip = "";
                    if (enableProcGen) {
                        try {
                            tip = core.getPronunciationMgr().getPronunciation(curWord.getValue());
                            if (tip.isEmpty()) {
                                tip = curWord.getPronunciation();
                            }
                        } catch (Exception ex) {
                            // IOHandler.writeErrorLog(ex);
                            enableProcGen = false;
                        }
                    }
                    if (tip.isEmpty()) {
                        tip = curWord.getLocalWord();
                    }
                    if (tip.isEmpty()) {
                        tip = curWord.getValue();
                    }
                    if (curType != null && (curType.getId() != 0 || core.getPropertiesManager().isTypesMandatory())) {
                        tip += " : " + (curType.getGloss().isEmpty()
                                ? curType.getValue() : curType.getGloss());
                    }
                    if (!curWord.getDefinition().isEmpty()) {
                        tip += " : " + WebInterface.getTextFromHtml(curWord.getDefinition());
                    }

                    theList.setToolTipText(tip);
                } else {
                    theList.setToolTipText(DEF_LEX_VALUE);
                }
            }
        });

        addFilterListeners(txtConSrc);
        addFilterListeners(txtDefSrc);
        addFilterListeners(txtLocalSrc);
        addFilterListeners(txtProcSrc);
        addFilterListeners(cmbTypeSrc);
        addFilterListeners(cmbRootSrc);

        // handles swapping of font for root box as appropriate
        cmbRootSrc.addEventHandler(EventType.ROOT, (Event evt) -> {
            if (cmbRootSrc.getValue() instanceof ConWord || cmbRootSrc.getValue() instanceof ConWordDisplay) {
                cmbRootSrc.setStyle("-fx-font: "
                        + core.getPropertiesManager()
                                .getFontCon().getSize()
                        + "px \""
                        + core.getPropertiesManager().getFontCon()
                                .getFamily() + "\";");
            } else {
                Font localFont = core.getPropertiesManager().getFontLocal();
                cmbRootSrc.setStyle("-fx-font: "
                        + localFont.getSize() + "px \""
                        + localFont.getFamily() + "\";");
            }
        });
    }

    /**
     * Adds appropriate listeners to conword property fields
     *
     * @param field field to add lister to
     * @param defValue default string value
     */
    private void addPropertyListeners(JComponent field, final String defValue) {
        if (field instanceof JComboBox) {
            final JComboBox cmbField = (JComboBox) field;
            cmbField.addActionListener((java.awt.event.ActionEvent evt) -> {
                setGreyFields(cmbField, defValue);
                setWordLegality();
            });
        }
    }

    /**
     * Adds appropriate listeners to filter fields (java FX Control objects)
     *
     * @param field field to add listener to
     */
    private void addFilterListeners(final Control field) {
        field.setOnKeyPressed((javafx.scene.input.KeyEvent ke) -> {
            if (ke.getCode() == KeyCode.ENTER) {
                runFilter();
            }
        });
    }

    /**
     * Sets up comboboxes based on core values
     */
    private void setupComboBoxesSwing() {
        cmbType.removeAllItems();
        cmbType.addItem(defTypeValue);
        for (TypeNode curNode : core.getTypes().getNodes()) {
            cmbType.addItem(curNode);
        }
    }

    /**
     * populates properties of currently selected word
     */
    private void populateProperties() {
        ConWord curWord = getCurrentWord();

        boolean localPopulating = curPopulating;
        curPopulating = true;

        try {
            if (curWord == null) {
                if (!namePopulating) {
                    namePopulating = true;
                    try {
                        ((PTextField) txtConWord).setDefault();
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                    }
                    namePopulating = false;
                }
                ((PTextField) txtLocalWord).setDefault();
                ((PTextField) txtProc).setDefault();
                ((PTextPane) txtDefinition).setDefault();
                cmbType.setSelectedItem(defTypeValue);
                chkProcOverride.setSelected(false);
                chkRuleOverride.setSelected(false);
                setPropertiesEnabled(false);
            } else {
                if (!namePopulating) {
                    namePopulating = true;
                    try {
                        txtConWord.setText(curWord.getValue());
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                    }
                    namePopulating = false;
                }
                txtDefinition.setText(curWord.getDefinition());
                txtLocalWord.setText(curWord.getLocalWord().isEmpty()
                        ? ((PTextField) txtLocalWord).getDefaultValue() : curWord.getLocalWord());
                if (enableProcGen) {
                    txtProc.setText(curWord.getPronunciation().isEmpty()
                            ? ((PTextField) txtProc).getDefaultValue() : curWord.getPronunciation());
                }
                TypeNode type = curWord.getWordTypeId() == 0 ? null : core.getTypes().getNodeById(curWord.getWordTypeId());
                cmbType.setSelectedItem(type == null ? defTypeValue : type);
                chkProcOverride.setSelected(curWord.isProcOverride());
                chkRuleOverride.setSelected(curWord.isRulesOverride());
                setupClassPanel(curWord.getWordTypeId());
                setupRomField();
                populateClassPanel();
                setPropertiesEnabled(true);
            }
        } catch (IllegalArgumentException e) {
            // IOHandler.writeErrorLog(e);
            enableProcGen = false;
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Error", "Error: " + e.getLocalizedMessage(), core.getRootWindow());
        }

        curPopulating = localPopulating;
    }

    /**
     * Sets whether word property fields are enabled or disabled
     *
     * @param enable
     */
    private void setPropertiesEnabled(final boolean enable) {
        Runnable runnable = () -> {
            txtConWord.setEnabled(enable);
            txtDefinition.setEnabled(enable);
            txtLocalWord.setEnabled(enable);
            txtProc.setEnabled(enable);
            txtRom.setEnabled(enable);
            cmbType.setEnabled(enable);
            chkProcOverride.setEnabled(enable);
            chkRuleOverride.setEnabled(enable);
            btnDeclensions.setEnabled(enable);
            btnLogographs.setEnabled(enable && btnLogoShouldEnable());
            btnEtymology.setEnabled(enable);
            classPropMap.values().forEach((classComp) -> {
                classComp.setEnabled(enable);
            });
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    private boolean btnLogoShouldEnable() {
        boolean ret = true;
        
        if (menuParent != null) {
            ret = menuParent.isEnabledLogoButton() 
                    && (logoQuick == null || logoQuick.isDisposed());
        }
        
        return ret;
    }

    /**
     * Sets up FX combo boxes (must be run in JavaFX thread)
     */
    private void setupComboBoxesFX() {
        cmbTypeSrc.getItems().clear();
        cmbTypeSrc.getItems().add(defTypeValue);
        cmbTypeSrc.getSelectionModel().selectFirst();
        cmbTypeSrc.getItems().addAll(Arrays.asList(core.getTypes().getNodes()));

        cmbRootSrc.getItems().clear();
        cmbRootSrc.getItems().add(defRootValue);
        cmbRootSrc.getSelectionModel().selectFirst();
        cmbRootSrc.getItems().addAll(Arrays.asList(core.getEtymologyManager().getAllRoots()));
    }

    /**
     * Sets appropriate fields grey
     */
    private void setGreyFields(JComponent comp, String defValue) {
        if (comp instanceof JComboBox) {
            JComboBox compCmb = (JComboBox) comp;
            if (compCmb.getSelectedItem() != null
                    && compCmb.getSelectedItem().toString().equals(defValue)) {
                compCmb.setForeground(Color.red);
            } else {
                compCmb.setForeground(Color.black);
            }
        }
    }

    /**
     * populates lexicon list with all words from core
     */
    private void populateLexicon() {
        populateLexicon(core.getWordCollection().getWordNodesDisplay());
    }

    /**
     * populates lexicon list with given iterator
     */
    private void populateLexicon(ConWordDisplay[] wordList) {
        boolean localPopulating = curPopulating;
        curPopulating = true;

        try {
            PListModelLexicon listModel = new PListModelLexicon();
            
            for (ConWordDisplay conWord : wordList) {
                listModel.addElement(conWord);
            }

            lstLexicon.setModel(listModel);
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Error", "Error: " + e.getLocalizedMessage(), core.getRootWindow());
        }

        curPopulating = localPopulating;
    }

    public static ScrLexicon run(DictCore _core, ScrMainMenu _scrMainMenu) {
        return new ScrLexicon(_core, _scrMainMenu);
    }

    /**
     * Saves name to word, then repopulates lexicon to ensure proper alphabetical order. Reselects proper entry.
     */
    private void saveName() {
        if (!curPopulating) {
            curPopulating = true;
            namePopulating = true;

            try {
                ConWord curWord = getCurrentWord();

                try {
                    if (curWord != null) {
                        saveValuesTo(curWord);
                    }
                } catch (Exception e) {
                    IOHandler.writeErrorLog(e);
                    InfoBox.error("Error", "Error: " + e.getLocalizedMessage(), core.getRootWindow());
                }

                curPopulating = false;

                // don't repopulate if looking for illegals
                if (!chkFindBad.isSelected()) {
                    filterLexicon();
                }

                curPopulating = true;
                lstLexicon.setSelectedValue(curWord, true);
            } catch (Exception e) {
                IOHandler.writeErrorLog(e);
            }
            namePopulating = false;
            curPopulating = false;
            setWordLegality();
        }
    }

    /**
     * Saves current values to argument word. Default values will not be saved.
     *
     * @param saveWord word to save current values to
     */
    private void saveValuesTo(ConWord saveWord) {
        if (((PTextField) txtConWord).isDefaultText() || saveWord == null) {
            return;
        }

        saveWord.setValue(txtConWord.getText());
        saveWord.setDefinition(txtDefinition.getText());
        saveWord.setLocalWord(((PTextField) txtLocalWord).isDefaultText()
                ? "" : txtLocalWord.getText());
        saveWord.setProcOverride(chkProcOverride.isSelected());
        saveWord.setPronunciation(((PTextField) txtProc).isDefaultText()
                ? "" : txtProc.getText());
        saveWord.setRulesOverride(chkRuleOverride.isSelected());
        Object curType = cmbType.getSelectedItem();
        if (curType != null) {
            if (curType.equals(defTypeValue)) {
                saveWord.setWordTypeId(0);
            } else {
                saveWord.setWordTypeId(((TypeNode) curType).getId());
            }
        }

        // save all class values
        classPropMap.entrySet().forEach((entry) -> {
            if (entry.getValue() instanceof PTextField) {
                PTextField textField = (PTextField) entry.getValue();
                saveWord.setClassTextValue(entry.getKey(), textField.getText());
            } else if (entry.getValue() instanceof PComboBox) {
                PComboBox comboBox = (PComboBox) entry.getValue();
                if (comboBox.getSelectedItem() instanceof WordClassValue) {
                    WordClassValue curValue = (WordClassValue) comboBox.getSelectedItem();
                    saveWord.setClassValue(entry.getKey(), curValue.getId());
                } else {
                    // if not an instance of a value, then it's the default selection: remove class from word
                    saveWord.setClassValue(entry.getKey(), -1);
                }
            } else {
                InfoBox.error("Value Save Error", "Unknown class value type.", core.getRootWindow());
            }
        });
    }

    /**
     * Always run with scheduling due to UI updates
     */
    private void deleteWord() {
        boolean localPopulating = curPopulating;
        curPopulating = true;
        boolean filterBlank = isFilterBlank();
        int curSelection = lstLexicon.getSelectedIndex();
        ConWord curWord = getCurrentWord();

        if (curSelection == -1 || curWord == null) {
            return;
        }

        try {
            core.getWordCollection().deleteNodeById(curWord.getId());
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Deletion Error", "Unable to delete word: "
                    + e.getLocalizedMessage(), core.getRootWindow());
        }

        gridTitlePane.setExpanded(false);
        populateLexicon();
        
        populateProperties();
        setWordLegality();
        curPopulating = localPopulating;
        
        if (!filterBlank) {
            filterLexicon();
        }
        
        lstLexicon.setSelectedIndex(curSelection == 0 ? 0 : curSelection - 1);
    }

    private void addWord() {
        ConWord curNode = getCurrentWord();
        if (curNode != null) {
            saveValuesTo(curNode);
        }

        curPopulating = true;
        core.getWordCollection().clear();
        try {
            int newId = core.getWordCollection().insert();
            ConWord newWord = core.getWordCollection().getNodeById(newId);
            populateLexicon();
            lstLexicon.setSelectedValue(newWord, true);
            populateProperties();
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Creation Error", "Unable to create word: "
                    + e.getLocalizedMessage(), core.getRootWindow());
        }
        curPopulating = false;

        setWordLegality();

        SwingUtilities.invokeLater(txtConWord::requestFocus);
    }

    /**
     * Open quickview on logographs for currently selected word
     */
    private void viewQuickLogographs() {
        ConWord curWord = getCurrentWord();

        if (curWord == null) {
            return;
        }
        
        killLogoChild();
        menuParent.setEnabledLogoButton(false);
        btnLogographs.setEnabled(btnLogoShouldEnable());

        logoQuick = new ScrLogoQuickView(core, curWord);
        logoQuick.addBindingToComponent(logoQuick.getRootPane());
        logoQuick.setCore(core);
        logoQuick.setVisible(true);
        logoQuick.addWindowListener(new WindowListener(){
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}

            @Override
            public void windowClosed(WindowEvent e) {
                menuParent.setEnabledLogoButton(true);
                btnLogographs.setEnabled(btnLogoShouldEnable());
            }

            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
    }

    private void viewDeclensions() {
        ConWord curWord = getCurrentWord();

        if (curWord == null) {
            return;
        }

        saveValuesTo(curWord);
        Window window = ScrDeclensionsGrids.run(core, this.getCurrentWord());
        
        if (window != null) {
            window.setVisible(true);
        }
    }

    /**
     * Wraps platform runnables within swing system. Messy but necessary to avoid race deadlocks
     *
     * @param r
     */
    private void wrapPlatformRunnable(final Runnable r) {
        SwingUtilities.invokeLater(() -> {
            Platform.runLater(r);
        });
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLayeredPane1 = new javax.swing.JLayeredPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        txtConWord = new PTextField(core, false, "-- ConWord --");
        txtLocalWord = new PTextField(core, true, "-- " + core.localLabel() + " Word --");
        cmbType = new PComboBox(core.getPropertiesManager().getFontMenu());
        txtProc = new PTextField(core, true, "-- Pronunciation --");
        chkProcOverride = new PCheckBox(nightMode, menuFontSize);
        chkRuleOverride = new PCheckBox(nightMode, menuFontSize);
        btnDeclensions = new PButton(nightMode, menuFontSize);
        btnLogographs = new PButton(nightMode, menuFontSize);
        jScrollPane1 = new javax.swing.JScrollPane();
        txtErrorBox = new javax.swing.JTextPane();
        pnlClasses = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtDefinition = new PTextPane(core, true, "-- Definition --");
        btnEtymology = new PButton(nightMode, menuFontSize);
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstLexicon = new PListLexicon(core.getPropertiesManager().getFontCon());
        btnAddWord = new PAddRemoveButton("+");
        btnDelWord = new PAddRemoveButton("-");
        jButton1 = new PButton(nightMode, menuFontSize);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Lexicon");
        setBackground(new java.awt.Color(255, 255, 255));
        setEnabled(false);
        setMinimumSize(new java.awt.Dimension(500, 450));
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });

        jLayeredPane1.setBackground(new java.awt.Color(255, 255, 255));
        jLayeredPane1.setMaximumSize(new java.awt.Dimension(4000, 4000));
        jLayeredPane1.setMinimumSize(new java.awt.Dimension(351, 350));
        jLayeredPane1.setName(""); // NOI18N
        jLayeredPane1.setPreferredSize(new java.awt.Dimension(351, 380));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMaximumSize(new java.awt.Dimension(4000, 4000));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setMaximumSize(new java.awt.Dimension(4000, 4000));

        jSplitPane1.setBackground(new java.awt.Color(255, 255, 255));
        jSplitPane1.setDividerLocation(123);
        jSplitPane1.setMaximumSize(new java.awt.Dimension(4000, 4000));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setMaximumSize(new java.awt.Dimension(4000, 4000));
        jPanel3.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanel3.setName(""); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(351, 380));

        txtConWord.setToolTipText("Constructed language word value");

        txtLocalWord.setToolTipText("Synonym for conword in local natural language");

        cmbType.setToolTipText("The word's part of speech");
        cmbType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTypeActionPerformed(evt);
            }
        });

        txtProc.setToolTipText("The word's pronunciation");

        chkProcOverride.setText("Override Pronunciation Rules");
        chkProcOverride.setToolTipText("Select this to override auto pronunciation generation for this word.");

        chkRuleOverride.setText("Override Lexical Rules");
        chkRuleOverride.setToolTipText("Overrides all typically enforced requirements for this word, allowing it to be saved as an exception");
        chkRuleOverride.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkRuleOverrideActionPerformed(evt);
            }
        });

        btnDeclensions.setText("Conjugations");
        btnDeclensions.setToolTipText("Edit or view declined/conjugated forms of your words here.");
        btnDeclensions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeclensionsActionPerformed(evt);
            }
        });

        btnLogographs.setText("Logographs");
        btnLogographs.setToolTipText("Jump to logographs associated with this word.");
        btnLogographs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogographsActionPerformed(evt);
            }
        });

        jScrollPane1.setMaximumSize(new java.awt.Dimension(4000, 4000));

        txtErrorBox.setEditable(false);
        txtErrorBox.setForeground(new java.awt.Color(255, 0, 0));
        txtErrorBox.setToolTipText("Displays problems with a word that must be corrected before deselecting it.");
        txtErrorBox.setDisabledTextColor(new java.awt.Color(255, 0, 0));
        txtErrorBox.setEnabled(false);
        jScrollPane1.setViewportView(txtErrorBox);

        pnlClasses.setMaximumSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout pnlClassesLayout = new javax.swing.GroupLayout(pnlClasses);
        pnlClasses.setLayout(pnlClassesLayout);
        pnlClassesLayout.setHorizontalGroup(
            pnlClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlClassesLayout.setVerticalGroup(
            pnlClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jScrollPane4.setMaximumSize(new java.awt.Dimension(4000, 4000));

        txtDefinition.setToolTipText("The long form definition of a word");
        txtDefinition.setMaximumSize(new java.awt.Dimension(4000, 4000));
        txtDefinition.setName(""); // NOI18N
        jScrollPane4.setViewportView(txtDefinition);

        btnEtymology.setText("Etymology");
        btnEtymology.setToolTipText("A word's etymological roots are stored and set here.");
        btnEtymology.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEtymologyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlClasses, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtProc, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(chkRuleOverride)
                .addContainerGap(333, Short.MAX_VALUE))
            .addComponent(txtLocalWord)
            .addComponent(cmbType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtConWord)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkProcOverride)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnDeclensions)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEtymology)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLogographs)))
                .addGap(0, 91, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtConWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtLocalWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlClasses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtProc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkProcOverride)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkRuleOverride)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLogographs, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeclensions, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEtymology, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jSplitPane1.setRightComponent(jPanel3);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setMaximumSize(new java.awt.Dimension(4000, 4000));

        jScrollPane3.setMaximumSize(new java.awt.Dimension(4000, 4000));

        lstLexicon.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstLexicon.setToolTipText("List of Conlang Words");
        lstLexicon.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        lstLexicon.setDragEnabled(true);
        lstLexicon.setMaximumSize(new java.awt.Dimension(4000, 4000));
        lstLexicon.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                lstLexiconFocusGained(evt);
            }
        });
        lstLexicon.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstLexiconValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(lstLexicon);

        btnAddWord.setToolTipText("Adds new word to dictionary");
        btnAddWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddWordActionPerformed(evt);
            }
        });

        btnDelWord.setToolTipText("Deletes selected word from dictionary");
        btnDelWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelWordActionPerformed(evt);
            }
        });

        jButton1.setText("Q");
        jButton1.setToolTipText("Open Quickentry Window");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(btnAddWord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDelWord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnAddWord)
                        .addComponent(btnDelWord))
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 593, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jLayeredPane1.setLayer(jPanel1, javax.swing.JLayeredPane.DRAG_LAYER);
        jLayeredPane1.setLayer(jPanel2, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 593, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lstLexiconValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstLexiconValueChanged
        if (evt.getValueIsAdjusting()
                || namePopulating
                || forceUpdate) {
            return;
        }

        if (!curPopulating
                && evt.getFirstIndex() != evt.getLastIndex()) {
            JList list = (JList) evt.getSource();
            int selected = list.getSelectedIndex();
            int index = selected == evt.getFirstIndex()
                    ? evt.getLastIndex() : evt.getFirstIndex();

            if (index != -1
                    && index < lstLexicon.getModel().getSize()) {
                ConWord saveWord = lstLexicon.getModel().getElementAt(index).getConWord();
                saveValuesTo(saveWord);
            }
            
            txtErrorBox.setText("");
        }

        populateProperties();

        // if looking for illegals, always check legality value of word, otherwise let it slide for user convenience
        if (chkFindBad.isSelected()) {
            setWordLegality();
        }
    }//GEN-LAST:event_lstLexiconValueChanged

    private void btnAddWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddWordActionPerformed
        Platform.runLater(() -> {
            try {
                clearFilter();
            } catch (Exception e) {
                IOHandler.writeErrorLog(e);
                InfoBox.error("Filter Error", e.getLocalizedMessage(), menuParent);
            }
            SwingUtilities.invokeLater(() -> {
                this.addWord();
                gridTitlePane.setExpanded(false);
            });
        });
    }//GEN-LAST:event_btnAddWordActionPerformed

    private void btnDelWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelWordActionPerformed
        Platform.runLater(()->{
            deleteWord();
        });
    }//GEN-LAST:event_btnDelWordActionPerformed

    private void btnDeclensionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeclensionsActionPerformed
        viewDeclensions();
        setWordLegality();
    }//GEN-LAST:event_btnDeclensionsActionPerformed

    private void btnLogographsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogographsActionPerformed
        viewQuickLogographs();
    }//GEN-LAST:event_btnLogographsActionPerformed

    private void lstLexiconFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_lstLexiconFocusGained
        lstLexicon.repaint();
    }//GEN-LAST:event_lstLexiconFocusGained

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        ConWord curWord = getCurrentWord();
        if (curWord != null) {
            saveValuesTo(curWord);
        }
    }//GEN-LAST:event_formWindowLostFocus

    private void chkRuleOverrideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkRuleOverrideActionPerformed
        setWordLegality();
    }//GEN-LAST:event_chkRuleOverrideActionPerformed

    private void cmbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTypeActionPerformed
        final Object typeObject = cmbType.getSelectedItem();

        if (!curPopulating) {
            if (typeObject == null) {
                setupClassPanel(0);
            } else {
                setupClassPanel(((TypeNode) typeObject).getId());
            }

            setupRomField();
            setWordLegality();
        }
    }//GEN-LAST:event_cmbTypeActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ScrQuickWordEntry.run(core, this);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnEtymologyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEtymologyActionPerformed
        new ScrEtymRoots(core, getCurrentWord()).setVisible(true);
    }//GEN-LAST:event_btnEtymologyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddWord;
    private javax.swing.JButton btnDeclensions;
    private javax.swing.JButton btnDelWord;
    private javax.swing.JButton btnEtymology;
    private javax.swing.JButton btnLogographs;
    private javax.swing.JCheckBox chkProcOverride;
    private javax.swing.JCheckBox chkRuleOverride;
    private javax.swing.JComboBox<Object> cmbType;
    private javax.swing.JButton jButton1;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JList<ConWordDisplay> lstLexicon;
    private javax.swing.JPanel pnlClasses;
    private javax.swing.JTextField txtConWord;
    private javax.swing.JTextPane txtDefinition;
    private javax.swing.JTextPane txtErrorBox;
    private javax.swing.JTextField txtLocalWord;
    private javax.swing.JTextField txtProc;
    // End of variables declaration//GEN-END:variables
}
