/*
 * Copyright (c) 2015, Draque Thompson
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

import PolyGlot.Nodes.ConWord;
import PolyGlot.DictCore;
import PolyGlot.Nodes.GenderNode;
import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PButton;
import PolyGlot.CustomControls.PComboBox;
import PolyGlot.CustomControls.PDialog;
import PolyGlot.CustomControls.PFrame;
import PolyGlot.CustomControls.PList;
import PolyGlot.CustomControls.PTextField;
import PolyGlot.Nodes.TypeNode;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author draque
 */
public final class ScrLexicon extends PFrame {

    private final List<Window> childFrames = new ArrayList<Window>();
    private TitledPane gridTitlePane = null;
    private final JFXPanel fxPanel;
    private final String defConValue = "-- ConWord --";
    private final String defLocalValue = "-- NalLang Word --";
    private final TypeNode defTypeValue = new TypeNode();
    private final GenderNode defGenderValue = new GenderNode();
    private final String defProcValue = "-- Pronunciation --";
    private final String defDefValue = "-- Definition --";
    private final String newTypeValue = "-- New Type --";
    private final String defLexValue = "List of Conlang Words";
    private TextField txtConSrc;
    private TextField txtLocalSrc;
    private TextField txtProcSrc;
    private TextField txtDefSrc;
    private ComboBox cmbTypeSrc;
    private ComboBox cmbGenderSrc;
    private boolean curPopulating = false;
    private boolean namePopulating = false;
    private boolean forceUpdate = false;
    private Thread filterThread = null;

    /**
     * Creates new form scrLexicon
     *
     * @param _core Dictionary Core
     */
    public ScrLexicon(DictCore _core) {
        defTypeValue.setValue("-- Type --");
        defGenderValue.setValue("-- Gender --");

        core = _core;
        fxPanel = new JFXPanel();
        initComponents();

        lstLexicon.setModel(new DefaultListModel());

        setupKeyStrokes();
        setupFilterMenu();
        setupComboBoxesSwing();
        setDefaultValues();
        populateLexicon();
        lstLexicon.setSelectedIndex(0);
        populateProperties();
        setupListeners();

        if (System.getProperty("os.name").startsWith("Mac")) {
            btnAddWord.setToolTipText(btnAddWord.getToolTipText() + " (⌘ +)");
            btnDelWord.setToolTipText(btnDelWord.getToolTipText() + " (⌘ -)");
        } else {
            btnAddWord.setToolTipText(btnAddWord.getToolTipText() + " (CTRL +)");
            btnDelWord.setToolTipText(btnDelWord.getToolTipText() + " (CTRL -)");
        }
    }

    /**
     * Opens quickentry window if not already open
     *
     * @return quickentry window
     */
    public ScrQuickWordEntry openQuickEntry() {
        ScrQuickWordEntry s = ScrQuickWordEntry.run(core, this);
        childFrames.add(s);

        return s;
    }

    @Override
    public boolean thisOrChildrenFocused() {
        boolean ret = this.isFocusOwner();
        for (Window w : childFrames) {
            if (w instanceof PFrame) {
                ret = ret || ((PFrame) w).thisOrChildrenFocused();
            } else if (w instanceof PDialog) {
                ret = ret || ((PDialog) w).thisOrChildrenFocused();
            }
        }
        return ret;
    }

    /**
     * forces refresh of word list
     *
     * @param wordId id of newly created word
     */
    public void refreshWordList(int wordId) {
        populateLexicon();
        try {
            lstLexicon.setSelectedValue(
                    core.getWordCollection().getNodeById(wordId), true);
        } catch (Exception e) {
            InfoBox.error("Refresh Error", "Unable to refresh lexicon: "
                    + e.getLocalizedMessage(), this);
            //e.printStackTrace();
        }
    }

    @Override
    public void updateAllValues(final DictCore _core) {
        // ensure this is on the UI component stack to avoid read/writelocks...
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // first push update to all child frames...
                boolean localPopulating = curPopulating;
                curPopulating = true;
                forceUpdate = true;
                for (Window window : childFrames) {
                    if (window instanceof PFrame) {
                        PFrame frame = ((PFrame) window);
                        if (!frame.isDisposed()) {
                            frame.updateAllValues(_core);
                        }
                    } else if (window instanceof PDialog) {
                        PDialog dialog = ((PDialog) window);
                        if (!dialog.isDisposed()) {
                            dialog.updateAllValues(_core);
                        }
                    }
                }

                if (core != _core) {
                    core = _core;
                    lstLexicon.setModel(new DefaultListModel());
                    setDefaultValues();
                    populateLexicon();
                    lstLexicon.setSelectedIndex(0);
                }
                Runnable fxSetup = new Runnable() {
                    @Override
                    public void run() {
                        setupComboBoxesFX();
                        setFonts();
                    }
                };
                Platform.setImplicitExit(false);
                Platform.runLater(fxSetup);

                ConWord curWord = (ConWord) lstLexicon.getSelectedValue();
                saveValuesTo(curWord);
                lstLexicon.clearSelection();
                setupComboBoxesSwing();
                lstLexicon.setSelectedValue(curWord, true);
                curPopulating = localPopulating;
                forceUpdate = false;
                populateProperties();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    @Override
    protected void setupKeyStrokes() {
        addBindingsToPanelComponents(this.getRootPane());
        super.setupKeyStrokes();
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
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), addKey);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | mask), delKey);
        ActionMap am = c.getActionMap();
        am.put(addKey, addAction);
        am.put(delKey, delAction);
    }

    /**
     * Sets up and drops the filter menu into the UI
     */
    private void setupFilterMenu() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = GridBagConstraints.RELATIVE;
        c.gridwidth = GridBagConstraints.RELATIVE;

        jPanel1.setLayout(new GridBagLayout());
        jPanel1.add(fxPanel, c);

        final CountDownLatch latch = new CountDownLatch(1);
        Runnable fxSetup = new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
                setupComboBoxesFX();
                setFonts();
                latch.countDown();
            }
        };
        Platform.setImplicitExit(false);
        Platform.runLater(fxSetup);

        try {
            latch.await();
        } catch (Exception e) {
            InfoBox.error("Form Load Error", "Unable to load Lexicon: " + e.getLocalizedMessage(), this);
        }
    }

    private void initFX(JFXPanel fxPanel) {
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }

    private Scene createScene() {
        Group root = new Group();
        Scene scene = new Scene(root);
        root.getChildren().add(createSearchPanel());

        return (scene);
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
            txtProc.setText(setText.isEmpty() ? defProcValue : setText);
        } catch (Exception e) {
            InfoBox.error("Pronunciation Error", "Could not generate pronunciation: "
                    + e.getLocalizedMessage(), this);
        }

        curPopulating = localPopulating;
    }

    /**
     * Sets default values to all user editable fields
     */
    private void setDefaultValues() {
        chkProcOverride.setSelected(false);
        chkRuleOverride.setSelected(false);
        txtConWord.setText(defConValue);
        txtDefinition.setText(defDefValue);
        txtLocalWord.setText(defLocalValue);
        txtProc.setText(defProcValue);
        cmbGender.setSelectedIndex(0);
        cmbType.setSelectedIndex(0);
        Runnable fxSetup = new Runnable() {
            @Override
            public void run() {
                txtConSrc.setText("");
                txtDefSrc.setText("");
                txtLocalSrc.setText("");
                txtProcSrc.setText("");
                cmbGenderSrc.getSelectionModel().select(0);
                cmbTypeSrc.getSelectionModel().select(0);
            }
        };
        Platform.setImplicitExit(false);
        Platform.runLater(fxSetup);

        setAllGreyProps();
    }

    /**
     * Runs filter on timed thread to avoid overabundance of filters and prevent
     * filtering overlaps. Run this instead of filterLexicon().
     */
    private void runFilter() {
        if (filterThread != null
                && filterThread.isAlive()) {
            filterThread.interrupt();
        }

        filterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500); // wait for interrupt from user...
                    if (txtConWord.getText().isEmpty()) {
                        return; // prevents freezing scenario with if new word made beore thread continues
                    }
                    filterLexicon();
                    lstLexicon.setSelectedIndex(0);
                    lstLexicon.ensureIndexIsVisible(0);
                    populateProperties();
                    Thread.sleep(50); // wait for other elements to paint first...
                    jPanel1.repaint();
                } catch (InterruptedException e) {
                    // do nothing: interruption is due to additional user input
                }
            }
        });

        filterThread.start();
    }

    /**
     * Filters lexicon. Call RunFilter() instead of this, which runs on a timed
     * session to prevent overlapping filters.
     */
    private void filterLexicon() {
        if (curPopulating) {
            return;
        }

        String filterGend = cmbGenderSrc.getValue().equals(defGenderValue)
                ? "" : ((GenderNode) cmbGenderSrc.getValue()).getValue();
        String filterType = cmbTypeSrc.getValue().equals(defTypeValue)
                ? "" : ((TypeNode) cmbTypeSrc.getValue()).getValue();

        if (txtConSrc.getText().equals("")
                && txtDefSrc.getText().equals("")
                && txtLocalSrc.getText().equals("")
                && txtProcSrc.getText().equals("")
                && filterGend.equals("")
                && filterType.equals("")) {
            populateLexicon();
            lstLexicon.setSelectedIndex(0);
            lstLexicon.ensureIndexIsVisible(0);

            // refresh lexicon if it was already filtered. Do nothing otherwise
            if (lstLexicon.getModel().getSize() < core.getWordCollection().getWordCount())
            {
                populateLexicon();
                lstLexicon.setSelectedIndex(0);
                populateProperties();
            } else {
                return;
            }
        }

        ConWord filter = new ConWord();

        filter.setValue(txtConSrc.getText().trim());
        filter.setDefinition(txtDefSrc.getText().trim());
        filter.setLocalWord(txtLocalSrc.getText().trim());
        filter.setWordType(filterType);
        filter.setGender(filterGend);
        filter.setPronunciation(txtProcSrc.getText().trim());

        // save word before applying filter
        ConWord curWord = (ConWord) lstLexicon.getSelectedValue();
        if (curWord != null) {
            saveValuesTo(curWord);
        }

        try {
            populateLexicon(core.getWordCollection().filteredList(filter));
        } catch (Exception e) {
            InfoBox.error("Filter Error", "Unable to apply filter.\n\n" + e.getMessage(), this);
        }

        lstLexicon.setSelectedIndex(0);
        lstLexicon.ensureIndexIsVisible(0);
    }

    /**
     * Clears lexicon's search/filter
     */
    private void clearFilter() {
        // if no filter in effect, do nothing
        if (txtConSrc.getText().isEmpty()
                && txtDefSrc.getText().isEmpty()
                && txtLocalSrc.getText().isEmpty()
                && txtProcSrc.getText().isEmpty()
                && cmbGenderSrc.getSelectionModel().getSelectedIndex() == 0
                && cmbTypeSrc.getSelectionModel().getSelectedIndex() == 0) {
            return;
        }
        
        // only run process if in FX Application thread. Recurse within thread otherwise
        if (!Platform.isFxApplicationThread()) {
            final CountDownLatch latch = new CountDownLatch(1);
            Runnable fxSetup = new Runnable() {
                @Override
                public void run() {
                    clearFilter();
                    latch.countDown();
                }
            };

            Platform.runLater(fxSetup);

            try {
                latch.await(); // do not continue until filter cleared
            } catch (Exception e) {
                InfoBox.error("JavaFX Problem", "Unable to clear filter: "
                        + e.getLocalizedMessage(), this);
            }
        } else {
            txtConSrc.setText("");
            txtDefSrc.setText("");
            txtLocalSrc.setText("");
            txtProcSrc.setText("");
            cmbGenderSrc.getSelectionModel().select(0);
            cmbTypeSrc.getSelectionModel().select(0);
            gridTitlePane.setExpanded(false);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    populateLexicon();
                }
            });
        }
        
        // this is to address an odd timing error... sloppy, but it's somewhere in the Java API
        try{Thread.sleep(250);}catch(Exception e) {}
    }

    /**
     * Sets currently displayed word's legality (highlighted fields, error
     * message, etc.)
     */
    private void setWordLegality() {
        ConWord testWord = (ConWord) lstLexicon.getSelectedValue();

        if (forceUpdate) {
            return;
        }

        if (testWord == null) {
            setWordLegality(testWord);
        }

        testWord = new ConWord();
        String genderString = cmbGender.getSelectedItem().equals(defGenderValue)
                ? "" : ((GenderNode) cmbGender.getSelectedItem()).getValue();
        String typeString = (cmbType.getSelectedItem().equals(defTypeValue)
                    || cmbType.getSelectedItem().equals(newTypeValue))
                ? "" : ((TypeNode) cmbType.getSelectedItem()).getValue();

        if (curPopulating) {
            return;
        }

        testWord.setValue(txtConWord.getText().equals(defConValue) ? "" : txtConWord.getText());
        testWord.setLocalWord(txtLocalWord.getText().equals(defLocalValue) ? "" : txtLocalWord.getText());
        testWord.setDefinition(txtDefinition.getText().equals(defDefValue) ? "" : txtDefinition.getText());
        testWord.setPronunciation(txtProc.getText().equals(defProcValue) ? "" : txtProc.getText());
        testWord.setGender(genderString);
        testWord.setWordType(typeString);
        testWord.setRulesOverride(chkRuleOverride.isSelected());

        setWordLegality(testWord);
    }

    /**
     * Sets lexicon tab's currently displayed word legality (highlighted fields,
     * error message, etc.)
     *
     * @param results current word
     * @param disableElements whether to disable control elements on fail
     */
    private void setWordLegality(ConWord testWord) {
        if (testWord == null) {
            setLexiconEnabled(true);
            txtErrorBox.setText("");
            return;
        }

        ConWord results = core.isWordLegal(testWord);
        boolean isLegal = true;

        txtErrorBox.setText("");

        isLegal = isLegal && addErrorBoxMessage(txtConWord, results.getValue());
        isLegal = isLegal && addErrorBoxMessage(txtLocalWord, results.getLocalWord());
        isLegal = isLegal && addErrorBoxMessage(txtProc, results.getPronunciation());
        isLegal = isLegal && addErrorBoxMessage(txtConWord, results.getDefinition());
        isLegal = isLegal && addErrorBoxMessage(cmbGender, results.getGender());
        isLegal = isLegal && addErrorBoxMessage(cmbType, results.getWordType());

        if (!testWord.isRulesOverrride()) {
            setLexiconEnabled(isLegal);
        } else {
            setLexiconEnabled(true);
        }
    }

    /**
     * Adds error if any it error box and takes appropriate action to inform
     * user
     *
     * @param element element related to checked value
     * @param message message (if any) returned as error
     * @return true if legal, false otherwise
     */
    private boolean addErrorBoxMessage(JComponent element, String message) {
        Color bColor = new JTextField().getBackground();
        Color hColor = core.getRequiredColor();
        boolean ret = true;

        if (!message.equals("")) {
            if (!txtErrorBox.getText().equals("")) {
                txtErrorBox.setText(txtErrorBox.getText() + "\n");
            }

            txtErrorBox.setText(txtErrorBox.getText() + message);
            element.setBackground(hColor);
            if (element instanceof PComboBox) {
                PComboBox eleComb = (PComboBox) element;
                eleComb.makeFlash(hColor, false);
            }

            ret = false;
        } else {
            element.setBackground(bColor);
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
        Runnable fxSetup = new Runnable() {
            @Override
            public void run() {
                txtConSrc.setDisable(!enable);
                txtDefSrc.setDisable(!enable);
                txtLocalSrc.setDisable(!enable);
                txtProcSrc.setDisable(!enable);
                cmbGenderSrc.setDisable(!enable);
                cmbTypeSrc.setDisable(!enable);
            }
        };

        Platform.runLater(fxSetup);
    }

    /**
     * creates JavaFX Search menu
     *
     * @return
     */
    private TitledPane createSearchPanel() {
        GridPane grid = new GridPane();
        txtConSrc = new TextField();
        txtConSrc.setPromptText("Search ConWord...");
        txtLocalSrc = new TextField();
        txtLocalSrc.setPromptText("Search NatLang Word...");
        txtProcSrc = new TextField();
        txtProcSrc.setPromptText("Search by Pronunciation...");
        txtDefSrc = new TextField();
        txtDefSrc.setPromptText("Search by Definition...");
        cmbTypeSrc = new ComboBox();
        cmbGenderSrc = new ComboBox();
        gridTitlePane = new TitledPane();

        grid.setVgap(4);
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.add(new Label("Con Word: "), 0, 0);
        grid.add(txtConSrc, 1, 0);
        grid.add(new Label("Local Word: "), 0, 1);
        grid.add(txtLocalSrc, 1, 1);
        grid.add(new Label("Type: "), 0, 2);
        grid.add(cmbTypeSrc, 1, 2);
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.add(new Label("Gender: "), 2, 0);
        grid.add(cmbGenderSrc, 3, 0);
        grid.add(new Label("Pronunciation: "), 2, 1);
        grid.add(txtProcSrc, 3, 1);
        grid.add(new Label("Definition: "), 2, 2);
        grid.add(txtDefSrc, 3, 2);
        gridTitlePane.setText("Search/Filter");
        gridTitlePane.setContent(grid);
        gridTitlePane.setExpanded(false);

        return gridTitlePane;
    }

    @Override
    public void dispose() {
        boolean canClose = true;

        if (!txtErrorBox.getText().equals("")
                && !chkRuleOverride.isSelected()) {
            InfoBox.warning("Illegal Word",
                    "Currently selected word is illegal. Please correct, or mark rule override.", this);
            canClose = false;
        }

        if (canClose) {
            ConWord curWord = (ConWord) lstLexicon.getSelectedValue();

            if (curWord != null) {
                saveValuesTo(curWord);
            }

            killAllChildren();
            super.dispose();
        }
    }

    /**
     * Closes all child windows
     */
    private void killAllChildren() {
        Iterator<Window> it = childFrames.iterator();

        while (it.hasNext()) {
            Window curFrame = it.next();

            if (curFrame != null
                    && curFrame.isShowing()) {
                curFrame.setVisible(false);
                curFrame.dispose();
            }
        }

        childFrames.clear();
    }

    public ConWord getCurrentWord() {
        return (ConWord) lstLexicon.getSelectedValue();
    }

    public void selectWordById(int id) {
        ConWord target = null;

        try {
            target = core.getWordCollection().getNodeById(id);
        } catch (Exception e) {
            InfoBox.error("Word Selection Error", "Unable to select word:\n"
                    + e.getLocalizedMessage(), this);
        }

        if (target == null) {
            return;
        }
        lstLexicon.setSelectedValue(target, true);
    }

    /**
     * Sets up all document listeners
     */
    private void setupListeners() {
        gridTitlePane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int contentHeight = newValue.intValue();
                jPanel1.setSize(jPanel1.getSize().width, contentHeight);
                fxPanel.setSize(fxPanel.getSize().width, contentHeight);
            }
        });

        txtConWord.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                genProc();
                setWordLegality();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                genProc();
                setWordLegality();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                genProc();
                setWordLegality();
            }
        });
        txtConWord.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                saveName();
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
                    ConWord curWord = (ConWord) model.getElementAt(index);
                    TypeNode curType = core.getTypes().findTypeByName(curWord.getWordType());
                    String tip = core.getPronunciationMgr().getPronunciation(curWord.getValue());
                    if (curType != null) {
                        tip += " : " + (curType.getGloss().equals("") 
                                ? curType.getValue() : curType.getGloss());
                    }
                    if (!curWord.getDefinition().equals("")) {
                        tip += " : " + curWord.getDefinition();
                    }
                    
                    theList.setToolTipText(tip);
                } else {
                    theList.setToolTipText(defLexValue);
                }
            }            
        });

        addPropertyListeners(txtConWord, defConValue);
        addPropertyListeners(txtLocalWord, defLocalValue);
        addPropertyListeners(txtProc, defProcValue);
        addPropertyListeners(txtDefinition, defDefValue);
        addPropertyListeners(cmbGender, defGenderValue.getValue());
        addPropertyListeners(cmbType, defTypeValue.getValue());
        addFilterListeners(txtConSrc);
        addFilterListeners(txtDefSrc);
        addFilterListeners(txtLocalSrc);
        addFilterListeners(txtProcSrc);
        addFilterListeners(cmbGenderSrc);
        addFilterListeners(cmbTypeSrc);
    }

    /**
     * Adds appropriate listeners to conword property fields
     *
     * @param field field to add lister to
     * @param defValue default string value
     */
    private void addPropertyListeners(JComponent field, final String defValue) {
        if (field instanceof JTextField) {
            final JTextField txtField = (JTextField) field;
            txtField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    setGreyFields(txtField, defValue);
                    setWordLegality();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    setGreyFields(txtField, defValue);
                    setWordLegality();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    setGreyFields(txtField, defValue);
                    setWordLegality();
                }
            });
        } else if (field instanceof JTextArea) {
            final JTextArea txtArea = (JTextArea) field;
            txtArea.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    setGreyFields(txtArea, defValue);
                    setWordLegality();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    setGreyFields(txtArea, defValue);
                    setWordLegality();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    setGreyFields(txtArea, defValue);
                    setWordLegality();
                }
            });
        } else if (field instanceof JComboBox) {
            final JComboBox cmbField = (JComboBox) field;
            cmbField.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setGreyFields(cmbField, defValue);
                    setWordLegality();
                }
            });
        }
    }

    /**
     * Adds appropriate listeners to filter fields (java FX Control objects)
     *
     * @param field field to add listener to
     */
    private void addFilterListeners(final Control field) {
        if (field instanceof TextField) {
            field.addEventHandler(EventType.ROOT, new EventHandler() {
                @Override
                public void handle(Event evt) {
                    Object type = evt.getEventType();
                    if (type.toString().equals(javafx.scene.input.KeyEvent.KEY_PRESSED.toString())) {
                        runFilter();
                    }
                }
            });
        } else if (field instanceof ComboBox) {
            field.addEventHandler(EventType.ROOT, new EventHandler() {
                @Override
                public void handle(Event evt) {
                    Object type = evt.getEventType();
                    if (type.toString().equals(javafx.scene.control.ComboBoxBase.ON_HIDING.toString())) {
                        runFilter();
                    }
                }
            });
        }
    }

    /**
     * Sets up comboboxes based on core values
     */
    private void setupComboBoxesSwing() {
        Iterator<TypeNode> typeIt = core.getTypes().getNodeIterator();
        Iterator<GenderNode> gendIt = core.getGenders().getNodeIterator();

        cmbType.removeAllItems();
        cmbType.addItem(defTypeValue);
        cmbType.addItem(newTypeValue);
        while (typeIt.hasNext()) {
            TypeNode curNode = typeIt.next();
            cmbType.addItem(curNode);
        }

        cmbGender.removeAllItems();
        cmbGender.addItem(defGenderValue);
        while (gendIt.hasNext()) {
            GenderNode curNode = gendIt.next();
            cmbGender.addItem(curNode);
        }
    }

    /**
     * populates properties of currently selected word
     */
    private void populateProperties() {
        ConWord curWord = (ConWord) lstLexicon.getSelectedValue();

        boolean localPopulating = curPopulating;
        curPopulating = true;

        try {
            if (curWord == null) {
                if (!namePopulating) {
                    namePopulating = true;
                    txtConWord.setText(defConValue);
                    namePopulating = false;
                }
                txtDefinition.setText(defDefValue);
                txtLocalWord.setText(defLocalValue);
                txtProc.setText(defProcValue);
                cmbGender.setSelectedItem(defGenderValue);
                cmbType.setSelectedItem(defTypeValue);
                chkProcOverride.setSelected(false);
                chkRuleOverride.setSelected(false);
                setPropertiesEnabled(false);
            } else {
                if (!namePopulating) {
                    namePopulating = true;
                    txtConWord.setText(curWord.getValue());
                    namePopulating = false;
                }
                txtDefinition.setText(curWord.getDefinition().equals("")
                        ? defDefValue : curWord.getDefinition());
                txtLocalWord.setText(curWord.getLocalWord().equals("")
                        ? defLocalValue : curWord.getLocalWord());
                txtProc.setText(curWord.getPronunciation().equals("")
                        ? defProcValue : curWord.getPronunciation());
                GenderNode gender = core.getGenders().findGenderByName(curWord.getGender());
                cmbGender.setSelectedItem(gender == null ? defGenderValue : gender);
                TypeNode type = core.getTypes().findTypeByName(curWord.getWordType());
                cmbType.setSelectedItem(type == null ? defTypeValue : type);
                chkProcOverride.setSelected(curWord.isProcOverride());
                chkRuleOverride.setSelected(curWord.isRulesOverrride());
                setPropertiesEnabled(true);
            }
        } catch (Exception e) {
            InfoBox.error("Error", "Error: " + e.getLocalizedMessage(), this);
            //e.printStackTrace();
        }

        setAllGreyProps();

        curPopulating = localPopulating;
    }

    /**
     * Sets whether word property fields are enabled or disabled
     *
     * @param enable
     */
    private void setPropertiesEnabled(final boolean enable) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                txtConWord.setEnabled(enable);
                txtDefinition.setEnabled(enable);
                txtLocalWord.setEnabled(enable);
                txtProc.setEnabled(enable);
                cmbGender.setEnabled(enable);
                cmbType.setEnabled(enable);
                chkProcOverride.setEnabled(enable);
                chkRuleOverride.setEnabled(enable);
                jLabel1.setEnabled(enable);
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    /**
     * Sets up FX combo boxes (must be run in JavaFX thread)
     */
    private void setupComboBoxesFX() {
        Iterator<TypeNode> typeIt = core.getTypes().getNodeIterator();
        Iterator<GenderNode> gendIt = core.getGenders().getNodeIterator();

        cmbTypeSrc.getItems().clear();
        cmbTypeSrc.getItems().add(defTypeValue);
        cmbTypeSrc.getSelectionModel().selectFirst();
        while (typeIt.hasNext()) {
            TypeNode curNode = typeIt.next();
            cmbTypeSrc.getItems().add(curNode);
        }

        cmbGenderSrc.getItems().clear();
        cmbGenderSrc.getItems().add(defGenderValue);
        cmbGenderSrc.getSelectionModel().selectFirst();
        while (gendIt.hasNext()) {
            GenderNode curNode = gendIt.next();
            cmbGenderSrc.getItems().add(curNode);
        }
    }

    /**
     * Sets appropriate fields grey
     */
    private void setGreyFields(JComponent comp, String defValue) {
        if (comp instanceof JTextField) {
            JTextField compTxt = (JTextField) comp;
            if (compTxt.getText().equals(defValue)) {
                compTxt.setForeground(Color.LIGHT_GRAY);
            } else {
                compTxt.setForeground(Color.black);
            }
        } else if (comp instanceof JTextArea) {
            JTextArea compTxt = (JTextArea) comp;
            if (compTxt.getText().equals(defValue)) {
                compTxt.setForeground(Color.LIGHT_GRAY);
            } else {
                compTxt.setForeground(Color.black);
            }
        } else if (comp instanceof JComboBox) {
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
     * Sets all fields to grey/black as appropriate
     */
    private void setAllGreyProps() {
        setGreyFields(txtConWord, defConValue);
        setGreyFields(txtDefinition, defDefValue);
        setGreyFields(txtLocalWord, defLocalValue);
        setGreyFields(txtProc, defProcValue);
        setGreyFields(cmbGender, defGenderValue.getValue());
        setGreyFields(cmbType, defTypeValue.getValue());
    }

    /**
     * Sets fonts of relevant fields to conlang font Must be run inside JavaFX
     * thread
     */
    private void setFonts() {
        javafx.scene.text.Font fontFx = core.getPropertiesManager().getFXFont();
        txtConSrc.setFont(fontFx);
    }

    /**
     * populates lexicon list with all words from core
     */
    private void populateLexicon() {
        populateLexicon(core.getWordCollection().getNodeIterator());
    }

    /**
     * populates lexicon list with given iterator
     */
    private void populateLexicon(Iterator<ConWord> lexIt) {
        boolean localPopulating = curPopulating;
        curPopulating = true;

        try {
            DefaultListModel listModel = new DefaultListModel();

            while (lexIt.hasNext()) {
                ConWord curNode = lexIt.next();

                listModel.addElement(curNode);
            }

            lstLexicon.setModel(listModel);
        } catch (Exception e) {
            InfoBox.error("Error", "Error: " + e.getLocalizedMessage(), this);
            //e.printStackTrace();
        }

        curPopulating = localPopulating;
    }

    public static ScrLexicon run(DictCore _core) {
        final ScrLexicon s = new ScrLexicon(_core);
        return s;
    }

    /**
     * Saves name to word, then repopulates lexicon to ensure proper
     * alphabetical order. Reselects proper entry.
     */
    private void saveName() {
        if (curPopulating) {
            return;
        }

        curPopulating = true;
        namePopulating = true;
        ConWord curWord = (ConWord) lstLexicon.getSelectedValue();

        try {
            if (curWord == null) {
                return;
            }

            saveValuesTo(curWord);
        } catch (Exception e) {
            InfoBox.error("Error", "Error: " + e.getLocalizedMessage(), this);
        }

        curPopulating = false;

        filterLexicon();

        curPopulating = true;
        lstLexicon.setSelectedValue(curWord, true);
        namePopulating = false;
        curPopulating = false;
    }

    /**
     * Saves current values to argument word. Default values will not be saved.
     *
     * @param saveWord word to save current values to
     */
    private void saveValuesTo(ConWord saveWord) {
        if (txtConWord.getText().equals(defConValue)) {
            return;
        }

        saveWord.setValue(txtConWord.getText());
        saveWord.setDefinition(txtDefinition.getText().equals(defDefValue)
                ? "" : txtDefinition.getText());
        Object curGend = cmbGender.getSelectedItem();
        if (curGend != null) {
            saveWord.setGender(curGend.equals(defGenderValue)
                   ? "" : ((GenderNode)curGend).getValue());
        }
        saveWord.setLocalWord(txtLocalWord.getText().equals(defLocalValue)
                ? "" : txtLocalWord.getText());
        saveWord.setProcOverride(chkProcOverride.isSelected());
        saveWord.setPronunciation(txtProc.getText().equals(defProcValue)
                ? "" : txtProc.getText());
        saveWord.setRulesOverride(chkRuleOverride.isSelected());
        Object curType = cmbType.getSelectedItem();
        if (curType != null) {
            saveWord.setWordType((curType.equals(defTypeValue) || curType.equals(newTypeValue))
                    ? "" : ((TypeNode)curType).getValue());
        }
    }

    private void deleteWord() {
        curPopulating = true;
        int curSelection = lstLexicon.getSelectedIndex();
        ConWord curWord = (ConWord) lstLexicon.getSelectedValue();

        if (curSelection == -1) {
            return;
        }

        try {
            core.getWordCollection().deleteNodeById(curWord.getId());
        } catch (Exception e) {
            InfoBox.error("Deletion Error", "Unable to delete word: "
                    + e.getLocalizedMessage(), this);
        }

        clearFilter();
        populateLexicon();
        lstLexicon.setSelectedIndex(curSelection == 0 ? 0 : curSelection - 1);
        populateProperties();
        setWordLegality();
        curPopulating = false;
    }

    private void addWord() {
        ConWord curNode = (ConWord) lstLexicon.getSelectedValue();
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
            InfoBox.error("Creation Error", "Unable to create word: "
                    + e.getLocalizedMessage(), this);
        }
        curPopulating = false;

        setWordLegality();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                txtConWord.requestFocus();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    private void viewQuickLogographs() {
        ConWord curWord = (ConWord) lstLexicon.getSelectedValue();

        if (curWord == null) {
            return;
        }

        // TODO: put this into a run method like all the other windows have...
        ScrLogoQuickView window = new ScrLogoQuickView(core, curWord);
        window.setupKeyStrokes();
        childFrames.add(window);
        ((PFrame) window).setCore(core);
        window.setVisible(true);
        final Window parent = this;
        this.setEnabled(false);

        window.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent arg0) {
                parent.setEnabled(true);
            }
        });
    }

    private void viewDeclensions() {
        ConWord curWord = (ConWord) lstLexicon.getSelectedValue();

        if (curWord == null) {
            return;
        }

        saveValuesTo(curWord);
        Window window = ScrDeclensions.run(core, curWord);
        childFrames.add(window);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLayeredPane1 = new javax.swing.JLayeredPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        txtConWord = new PTextField(this);
        txtLocalWord = new javax.swing.JTextField();
        cmbType = new PComboBox();
        cmbGender = new PComboBox();
        txtProc = new javax.swing.JTextField();
        chkProcOverride = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        chkRuleOverride = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDefinition = new javax.swing.JTextArea();
        btnDeclensions = new javax.swing.JButton();
        btnLogographs = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtErrorBox = new javax.swing.JTextPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstLexicon = new PList(this);
        btnAddWord = new PButton("+");
        btnDelWord = new PButton("-");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Lexicon");
        setMinimumSize(new java.awt.Dimension(500, 450));
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });

        jLayeredPane1.setMinimumSize(new java.awt.Dimension(351, 350));
        jLayeredPane1.setName(""); // NOI18N
        jLayeredPane1.setPreferredSize(new java.awt.Dimension(351, 380));

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

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(0));

        jSplitPane1.setDividerLocation(123);

        jPanel3.setMaximumSize(new java.awt.Dimension(999999, 999999));
        jPanel3.setMinimumSize(new java.awt.Dimension(51, 80));
        jPanel3.setName(""); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(351, 380));

        txtConWord.setToolTipText("Constructed language word value");
        txtConWord.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtConWordFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtConWordFocusLost(evt);
            }
        });

        txtLocalWord.setToolTipText("Synonym for conword in local natural language");
        txtLocalWord.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtLocalWordFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtLocalWordFocusLost(evt);
            }
        });

        cmbType.setToolTipText("The word's part of speech");
        cmbType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTypeActionPerformed(evt);
            }
        });

        cmbGender.setToolTipText("The word's gender");

        txtProc.setToolTipText("The word's pronunciation");
        txtProc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtProcFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtProcFocusLost(evt);
            }
        });

        chkProcOverride.setToolTipText("Select this to override auto pronunciation generation for this word.");

        jLabel1.setText("Override Rules");

        chkRuleOverride.setToolTipText("Overrides all typically enforced requirements for this word, allowing it to be saved as an exception");
        chkRuleOverride.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkRuleOverrideActionPerformed(evt);
            }
        });

        txtDefinition.setColumns(20);
        txtDefinition.setLineWrap(true);
        txtDefinition.setRows(5);
        txtDefinition.setToolTipText("The word's full definition");
        txtDefinition.setWrapStyleWord(true);
        txtDefinition.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtDefinitionFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtDefinitionFocusLost(evt);
            }
        });
        jScrollPane2.setViewportView(txtDefinition);

        btnDeclensions.setText("Declensions");
        btnDeclensions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeclensionsActionPerformed(evt);
            }
        });

        btnLogographs.setText("Logographs");
        btnLogographs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogographsActionPerformed(evt);
            }
        });

        txtErrorBox.setEditable(false);
        txtErrorBox.setForeground(new java.awt.Color(255, 0, 0));
        txtErrorBox.setToolTipText("Displays problems with a word that must be corrected before deselecting it.");
        txtErrorBox.setDisabledTextColor(new java.awt.Color(255, 0, 0));
        txtErrorBox.setEnabled(false);
        jScrollPane1.setViewportView(txtErrorBox);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(btnDeclensions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogographs))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkRuleOverride)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtConWord)
                    .addComponent(txtLocalWord)
                    .addComponent(cmbType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbGender, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtProc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkProcOverride))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addContainerGap())
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
                .addComponent(cmbGender, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtProc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkProcOverride))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(chkRuleOverride))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDeclensions)
                    .addComponent(btnLogographs))
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel3);

        lstLexicon.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstLexicon.setToolTipText("List of Conlang Words");
        lstLexicon.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        lstLexicon.setMaximumSize(new java.awt.Dimension(99999, 99999));
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

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(btnAddWord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDelWord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddWord)
                    .addComponent(btnDelWord))
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        jLayeredPane1.setLayer(jPanel1, javax.swing.JLayeredPane.DRAG_LAYER);
        jLayeredPane1.setLayer(jPanel2, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtConWordFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtConWordFocusGained
        if (txtConWord.getText().equals(defConValue)) {
            txtConWord.setText("");
        }
    }//GEN-LAST:event_txtConWordFocusGained

    private void txtConWordFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtConWordFocusLost
        if (txtConWord.getText().equals("")) {
            txtConWord.setText(defConValue);
        }
    }//GEN-LAST:event_txtConWordFocusLost

    private void txtLocalWordFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtLocalWordFocusGained
        if (txtLocalWord.getText().equals(defLocalValue)) {
            txtLocalWord.setText("");
        }
    }//GEN-LAST:event_txtLocalWordFocusGained

    private void txtLocalWordFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtLocalWordFocusLost
        if (txtLocalWord.getText().equals("")) {
            txtLocalWord.setText(defLocalValue);
        }
    }//GEN-LAST:event_txtLocalWordFocusLost

    private void txtProcFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtProcFocusGained
        if (txtProc.getText().equals(defProcValue)) {
            txtProc.setText("");
        }
    }//GEN-LAST:event_txtProcFocusGained

    private void txtProcFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtProcFocusLost
        if (txtProc.getText().equals("")) {
            txtProc.setText(defProcValue);
        }
    }//GEN-LAST:event_txtProcFocusLost

    private void txtDefinitionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDefinitionFocusGained
        if (txtDefinition.getText().equals(defDefValue)) {
            txtDefinition.setText("");
        }
    }//GEN-LAST:event_txtDefinitionFocusGained

    private void txtDefinitionFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDefinitionFocusLost
        if (txtDefinition.getText().equals("")) {
            txtDefinition.setText(defDefValue);
            setGreyFields(txtDefinition, defDefValue);
        }
    }//GEN-LAST:event_txtDefinitionFocusLost

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
                ConWord saveWord = (ConWord) lstLexicon.getModel().getElementAt(index);
                saveValuesTo(saveWord);
            }
        }

        populateProperties();
    }//GEN-LAST:event_lstLexiconValueChanged

    private void btnAddWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddWordActionPerformed
        clearFilter();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                addWord();
            }
        });
    }//GEN-LAST:event_btnAddWordActionPerformed

    private void btnDelWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelWordActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                deleteWord();
                filterLexicon();
                gridTitlePane.setExpanded(false);
            }
        });
    }//GEN-LAST:event_btnDelWordActionPerformed

    private void btnDeclensionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeclensionsActionPerformed
        viewDeclensions();
    }//GEN-LAST:event_btnDeclensionsActionPerformed

    private void btnLogographsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogographsActionPerformed
        viewQuickLogographs();
    }//GEN-LAST:event_btnLogographsActionPerformed

    private void lstLexiconFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_lstLexiconFocusGained
        lstLexicon.repaint();
    }//GEN-LAST:event_lstLexiconFocusGained

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        ConWord curWord = (ConWord) lstLexicon.getSelectedValue();
        if (curWord != null) {
            saveValuesTo(curWord);
        }
    }//GEN-LAST:event_formWindowLostFocus

    private void chkRuleOverrideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkRuleOverrideActionPerformed
        setWordLegality();
    }//GEN-LAST:event_chkRuleOverrideActionPerformed

    private void cmbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTypeActionPerformed
        if (cmbType.getSelectedItem() != null
                && cmbType.getSelectedItem().equals(newTypeValue)) {
            final TypeNode newType = ScrTypes.newGetType(core);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    cmbType.setSelectedItem(newType == null ? defTypeValue : newType);
                }
            });
        }
    }//GEN-LAST:event_cmbTypeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddWord;
    private javax.swing.JButton btnDeclensions;
    private javax.swing.JButton btnDelWord;
    private javax.swing.JButton btnLogographs;
    private javax.swing.JCheckBox chkProcOverride;
    private javax.swing.JCheckBox chkRuleOverride;
    private javax.swing.JComboBox cmbGender;
    private javax.swing.JComboBox cmbType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JList lstLexicon;
    private javax.swing.JTextField txtConWord;
    private javax.swing.JTextArea txtDefinition;
    private javax.swing.JTextPane txtErrorBox;
    private javax.swing.JTextField txtLocalWord;
    private javax.swing.JTextField txtProc;
    // End of variables declaration//GEN-END:variables
}
