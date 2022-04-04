/*
 * Copyright (c) 2015-2022, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.FormattedTextHelper;
import org.darisadesigns.polyglotlina.ExternalCode.GlyphVectorPainter;
import org.darisadesigns.polyglotlina.Desktop.PFontHandler;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.darisadesigns.polyglotlina.CustomControls.GrammarChapNode;
import org.darisadesigns.polyglotlina.CustomControls.GrammarSectionNode;
import org.darisadesigns.polyglotlina.Desktop.ClipboardHandler;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopGrammarChapNode;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopGrammarSectionNode;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.HighlightCaret;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PAddRemoveButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PComboBox;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PGDocument;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PGrammarPane;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTree;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopGrammarManager;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.Desktop.SoundRecorder;

/**
 * This form displays and allows editing of a chapter/section style grammar text
 * for Conlanging authors to use.
 *
 * @author draque
 */
public final class ScrGrammarGuide extends PFrame {

    private final String defTime;
    private final DesktopGrammarManager gramMan;
    private SoundRecorder soundRecorder;
    private boolean isUpdating;
    private final ImageIcon playButtonUp;
    private final ImageIcon playButtonDown;
    private final ImageIcon recordButtonUp;
    private final ImageIcon recordButtonDown;
    private final ImageIcon trashButtonUp;
    private final ImageIcon trashButtonDown;
    private final ImageIcon trashButtonDisabled;
    private final ImageIcon addButton;
    private final ImageIcon deleteButton;
    private final ImageIcon addButtonPressed;
    private final ImageIcon deleteButtonPressed;

    /**
     * Creates new form scrGrammarGuide
     *
     * @param _core Dictionary core
     */
    public ScrGrammarGuide(DictCore _core) {
        super(_core);
        gramMan = (DesktopGrammarManager)core.getGrammarManager();
        
        isUpdating = false;
        defTime = "00:00:00";
        playButtonUp = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.PLAY_BUTTON_UP)));
        playButtonDown = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.PLAY_BUTTON_DOWN)));
        recordButtonUp = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.RECORD_BUTTON_UP)));
        recordButtonDown = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.RECORD_BUTTON_DOWN)));
        addButton = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.ADD_BUTTON)));
        addButtonPressed = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.ADD_BUTTON_PRESSED)));
        deleteButton = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.DELETE_BUTTON)));
        deleteButtonPressed = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.DELETE_BUTTON_PRESSED)));
        trashButtonUp = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.TRASH_BUTTON_UP)));
        trashButtonDown = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.TRASH_BUTTON_DOWN)));
        trashButtonDisabled = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.TRASH_BUTTON_DISABLED)));

        initComponents();
        
        initFontDrop();

        setupRecordButtons();
        setupChapTreeModel();
        addBindingsToPanelComponents(this.getRootPane());

        txtSection.setCaret(new HighlightCaret());

        soundRecorder = new SoundRecorder(this);
        soundRecorder.setButtons(
                btnRecordAudio, 
                btnPlayPauseAudio, 
                btnDeleteRecordedAudio,
                playButtonUp, 
                playButtonDown, 
                recordButtonUp, 
                recordButtonDown,
                trashButtonDown,
                trashButtonUp,
                trashButtonDisabled
        );

        setInitialValues();
        setupListeners();
        populateSections();

        if (System.getProperty("os.name").startsWith("Mac")) {
            btnAddSection.setToolTipText(btnAddSection.getToolTipText() + " (Option +)");
            btnDelete.setToolTipText(btnDelete.getToolTipText() + " (Option -)");
        } else {
            btnAddSection.setToolTipText(btnAddSection.getToolTipText() + " (CTRL +)");
            btnDelete.setToolTipText(btnDelete.getToolTipText() + " (CTRL -)");
        }
        
        setupForm();
    }
    
    private void setupForm() {        
        int divider = PolyGlot.getPolyGlot().getOptionsManager().getDividerPosition(this.getClass().getName());
        
        if (divider > -1) {
            jSplitPane1.setDividerLocation(divider);
        }
    }
    
    private void initFontDrop() {
        cmbFonts.removeAllItems();
        cmbFonts.addItem(core.localLabel() + " Font");
        cmbFonts.addItem(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon().getName());
    }
    
    private void setupChapTreeModel() {
        DefaultMutableTreeNode rootNode = new DesktopGrammarChapNode("Root Node", gramMan);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        treChapList.setModel(treeModel);
        treChapList.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    private void setupRecordButtons() {
        btnPlayPauseAudio.setBorder(null);
        btnPlayPauseAudio.setBorderPainted(false);
        btnPlayPauseAudio.setFocusPainted(false);
        btnPlayPauseAudio.setFocusTraversalKeysEnabled(false);
        btnPlayPauseAudio.setFocusable(false);
        btnPlayPauseAudio.setRequestFocusEnabled(false);
        btnPlayPauseAudio.setContentAreaFilled(false);
        btnRecordAudio.setBorder(null);
        btnRecordAudio.setBorderPainted(false);
        btnRecordAudio.setFocusPainted(false);
        btnRecordAudio.setFocusTraversalKeysEnabled(false);
        btnRecordAudio.setFocusable(false);
        btnRecordAudio.setRequestFocusEnabled(false);
        btnRecordAudio.setContentAreaFilled(false);
        btnDeleteRecordedAudio.setBorder(null);
        btnDeleteRecordedAudio.setBorderPainted(false);
        btnDeleteRecordedAudio.setFocusPainted(false);
        btnDeleteRecordedAudio.setFocusTraversalKeysEnabled(false);
        btnDeleteRecordedAudio.setFocusable(false);
        btnDeleteRecordedAudio.setRequestFocusEnabled(false);
        btnDeleteRecordedAudio.setContentAreaFilled(false);
    }

    @Override
    public void updateAllValues(DictCore _core) {
        savePropsToNode((DefaultMutableTreeNode) treChapList.getLastSelectedPathComponent());
        if (core != _core) {
            core = _core;
            setInitialValues();
            populateSections();
        }

        populateProperties();
        initFontDrop();
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        Action addAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSection();
            }
        };
        Action delAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteNode();
            }
        };
        String addKey = "addSection";
        String delKey = "delNode";
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

    @Override
    public void dispose() {
        saveAllValues();
        stopAllSounds();
        PolyGlot.getPolyGlot().getOptionsManager().setDividerPosition(getClass().getName(), jSplitPane1.getDividerLocation());
        super.dispose();
    }
    
    @Override
    public void saveAllValues() {
        savePropsToNode((DefaultMutableTreeNode) treChapList.getLastSelectedPathComponent());
    }
    
    private void stopAllSounds() {
        if (soundRecorder != null && soundRecorder.isPlaying()) {
            try {
                soundRecorder.playPause();
            } catch (IOException e) {
                // ignore any sound errors here. We're killing the screen, and no process can be orphaned in doing so.
                // core.getOSHandler().getIOHandler().writeErrorLog(e);
            }
        }
    }
    
    private void updateFontSize() {
        try {
            Integer.parseInt(txtFontSize.getText());
        } catch (NumberFormatException e) {
            // user error
            // core.getOSHandler().getIOHandler().writeErrorLog(e);
            core.getOSHandler().getInfoBox().warning("Font Size", "Invalid size: " + txtFontSize.getText());
            txtFontSize.setText("12");
        }
        setFont();
    }

    /**
     * Sets up initial values of components
     */
    private void setInitialValues() {
        // popping to back somewhere here
        treChapList.requestFocus();
        soundRecorder.setTimer(txtTimer);
        soundRecorder.setSlider(sldSoundPosition);
        txtSection.setDocument(new PGDocument(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon()));
        treChapList.setRootVisible(false);
        txtTimer.setText(defTime);
        txtSection.addStyle("default", null); // default style makes word wrap active

        try {
            txtTimer.setFont(PFontHandler.getLcdFont().deriveFont(Font.PLAIN, 18f));
        } catch (FontFormatException | IOException e) {
            core.getOSHandler().getIOHandler().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Font Error", "Unable to load LCD font due to: " + e.getMessage());
        }

        btnPlayPauseAudio.setText("");
        btnRecordAudio.setText("");
        btnAddSection.setText("");
        btnDeleteRecordedAudio.setText("");
        btnDelete.setText("");
        btnAddSection.setIcon(PGTUtil.getSizedIcon(addButton, 21, 21));
        btnAddSection.setPressedIcon(PGTUtil.getSizedIcon(addButtonPressed, 21, 21));
        btnDelete.setIcon(PGTUtil.getSizedIcon(deleteButton, 21, 21));
        btnDelete.setPressedIcon(PGTUtil.getSizedIcon(deleteButtonPressed, 21, 21));
        btnAddSection.setContentAreaFilled(false);
        btnDelete.setContentAreaFilled(false);
    }

    /**
     * Sets input font/font of selected text
     */
    private void setFont() {
        Font natFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
        Font conFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon();
        SimpleAttributeSet aset = new SimpleAttributeSet();

        // natlang font is always 0, conlang font always 1
        if (cmbFonts.getSelectedIndex() == 0) {
            StyleConstants.setFontFamily(aset, natFont.getFamily());
        } else {
            aset.addAttribute(GlyphVectorPainter.KEY_KERNING, core.getPropertiesManager().getKerningSpace() * 10);
            StyleConstants.setFontFamily(aset, conFont.getFamily());
        }

        StyleConstants.setForeground(aset,
                FormattedTextHelper.textToColor((String) cmbFontColor.getSelectedItem()));

        StyleConstants.setFontSize(aset, Integer.parseInt(txtFontSize.getText()));

        txtSection.setCharacterAttributes(aset, true);

        int caretStart = txtSection.getSelectionStart();
        int caretEnd = txtSection.getSelectionEnd();

        // logic for ensuring LTR enforcement if no text is currently selected
        if (caretStart == caretEnd
                && core.getPropertiesManager().isEnforceRTL()) {
            StyledDocument doc = txtSection.getStyledDocument();
            try {
                doc.insertString(caretStart, " ", aset);
                caretEnd++;
            } catch (BadLocationException e) {
                core.getOSHandler().getIOHandler().writeErrorLog(e);
                core.getOSHandler().getInfoBox().warning("Font Error", "Problem setting font: "
                        + e.getLocalizedMessage());
            }
        }

        if (core.getPropertiesManager().isEnforceRTL()) {
            // this ensures that the correct sections are displayed RTL
            savePropsToNode((DefaultMutableTreeNode) treChapList.getLastSelectedPathComponent());
            populateProperties();
        }

        txtSection.requestFocus();
        txtSection.setSelectionStart(caretStart);
        txtSection.setSelectionEnd(caretEnd);
    }

    /**
     * sets up object listeners
     */
    private void setupListeners() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!isUpdating) {
                    populateFromSearch();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isUpdating) {
                    populateFromSearch();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isUpdating) {
                    populateFromSearch();
                }
            }
        });
        txtName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!isUpdating) {
                    updateName();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isUpdating) {
                    updateName();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isUpdating) {
                    updateName();
                }
            }
        });
        treChapList.addTreeSelectionListener((TreeSelectionEvent e) -> {
            TreePath oldPath = e.getOldLeadSelectionPath();
            if (oldPath != null) {
                Object oldNode = oldPath.getPathComponent(oldPath.getPathCount() - 1);
                savePropsToNode((DefaultMutableTreeNode) oldNode);
            }
            
            closeAllPlayRecord();
            populateProperties();
        });
        // add listener for character replacement logic
        txtSection.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                Character c = e.getKeyChar();
                String repString = core.getPropertiesManager().getCharacterReplacement(c.toString());
                
                // only run replacement logic if currently set to Conlang font
                // replacement logic at form level because this text editor can handle multiple fonts simultaneously
                if (!repString.isEmpty()
                        && cmbFonts.getSelectedItem().equals(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon().getName())) {
                    try {
                        e.consume();
                        ClipboardHandler cb = new ClipboardHandler();
                        cb.cacheClipboard();
                        cb.setClipboardContents(repString);
                        txtSection.paste();
                        cb.restoreClipboard();
                    } catch (Exception ex) {
                        core.getOSHandler().getIOHandler().writeErrorLog(ex);
                        core.getOSHandler().getInfoBox().error("Character Replacement Error",
                                "Clipboard threw error during character replacement process:"
                                + ex.getLocalizedMessage());
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // do nothing
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });
    }

    /**
     * Saves current grammar properties to the node passed in. If null, nothing.
     *
     * @param node node to save values to
     */
    public void savePropsToNode(DefaultMutableTreeNode node) {
        if (node instanceof GrammarSectionNode secNode) {
            secNode.setName(txtName.getText());
            secNode.setRecording(soundRecorder.getSound());
            try {
                secNode.setSectionText(FormattedTextHelper.storageFormat(txtSection));
            } catch (Exception e) {
                core.getOSHandler().getIOHandler().writeErrorLog(e);
                core.getOSHandler().getInfoBox().error("Section Save Error", "Unable to save section text: "
                        + e.getLocalizedMessage());
            }
        } else if (node instanceof GrammarChapNode chapNode) {
            chapNode.setName(txtName.getText());
        }
    }

    /**
     * Updates name from UI element of currently selected chapter or section
     */
    private void updateName() {
        boolean localUpdating = isUpdating;
        isUpdating = true;

        Object selection = treChapList.getLastSelectedPathComponent();
        var name = txtName.getText();

        if (selection instanceof GrammarSectionNode grammarSectionNode) {
            grammarSectionNode.setName(name);
        } else if (selection instanceof GrammarChapNode grammarChapNode) {
            grammarChapNode.setName(name);
        }
        
        btnAddChapter.setEnabled(!name.isEmpty() || selection == null);
        btnAddSection.setEnabled(!name.isEmpty() || selection == null);
        treChapList.setEnabled(!name.isEmpty());

        treChapList.repaint();
        isUpdating = localUpdating;
    }

    /**
     * Stops all recording and play streams
     */
    private void closeAllPlayRecord() {
        try {
            if (soundRecorder.isPlaying()) {
                soundRecorder.playPause();
            }
            if (soundRecorder.isRecording()) {
                soundRecorder.endRecording();
            }
        } catch (IOException e) {
            // on exception, inform user and replace sound recorder
            core.getOSHandler().getIOHandler().writeErrorLog(e);
            soundRecorder = new SoundRecorder(this);
            soundRecorder.setButtons(
                    btnRecordAudio, 
                    btnPlayPauseAudio, 
                    btnDeleteRecordedAudio,
                    playButtonUp, 
                    playButtonDown, 
                    recordButtonUp, 
                    recordButtonDown,
                    trashButtonDown,
                    trashButtonUp,
                    trashButtonDisabled
            );
            
            core.getOSHandler().getInfoBox().error("Recorder Error", "Unable to end audio stream: " + e.getLocalizedMessage());
        }
    }

    /**
     * Populates properties of chapter/section and sets appropriate controls
     */
    private void populateProperties() {
        if (isUpdating) {
            return;
        }

        isUpdating = true;

        Object selection = treChapList.getLastSelectedPathComponent();
        if (selection instanceof GrammarChapNode chapNode) {
            txtName.setText(chapNode.getName());
            txtName.setEnabled(true);
            txtSection.setText("");
            txtSection.setEnabled(false);
            btnApply.setEnabled(false);
            txtFontSize.setEnabled(false);
            cmbFontColor.setEnabled(false);
            cmbFonts.setEnabled(false);
            btnPlayPauseAudio.setEnabled(false);
            btnDeleteRecordedAudio.setEnabled(false);
            btnRecordAudio.setEnabled(false);
            sldSoundPosition.setValue(0);
            sldSoundPosition.setEnabled(false);
            txtTimer.setText(defTime);
            soundRecorder.setSound(null);
        } else if (selection instanceof GrammarSectionNode secNode) {
            txtName.setText(secNode.getName());
            txtName.setEnabled(true);
            txtSection.setEnabled(true);
            btnApply.setEnabled(true);
            txtFontSize.setEnabled(true);
            cmbFontColor.setEnabled(true);
            cmbFonts.setEnabled(true);
            btnPlayPauseAudio.setEnabled(true);
            btnRecordAudio.setEnabled(true);
            btnDeleteRecordedAudio.setEnabled(true);
            sldSoundPosition.setValue(0);
            sldSoundPosition.setEnabled(false);
            txtTimer.setText(defTime);
            try {
                byte[] sound = secNode.getRecording();
                
                btnPlayPauseAudio.setEnabled(sound != null);
                btnDeleteRecordedAudio.setEnabled(sound != null);   
                soundRecorder.setSound(sound);
            } catch (Exception e) {
                core.getOSHandler().getIOHandler().writeErrorLog(e);
                core.getOSHandler().getInfoBox().error("Recording Load Failure", "Unable to load recording: "
                        + e.getLocalizedMessage());
            }
            try {
                FormattedTextHelper.restoreFromString(secNode.getSectionText(),
                        txtSection, core);
            } catch (BadLocationException e) {
                core.getOSHandler().getIOHandler().writeErrorLog(e);
                core.getOSHandler().getInfoBox().error("Section Load Error", "Unable to load section text: "
                        + e.getLocalizedMessage());
            }
            SwingUtilities.invokeLater(() -> {
                panSection.getVerticalScrollBar().setValue(0);
            });

        } else {
            // if neither is selected, then the whole tree has been deleted by the user
            txtName.setText("");
            txtName.setForeground(Color.gray);
            txtName.setEnabled(false);
            txtSection.setText("");
            txtSection.setEnabled(false);
            btnApply.setEnabled(false);
            txtFontSize.setEnabled(false);
            cmbFontColor.setEnabled(false);
            cmbFonts.setEnabled(false);
            btnPlayPauseAudio.setEnabled(false);
            btnDeleteRecordedAudio.setEnabled(false);
            btnRecordAudio.setEnabled(false);
            sldSoundPosition.setValue(0);
            sldSoundPosition.setEnabled(false);
            txtTimer.setText(defTime);
            soundRecorder.setSound(null);
        }

        isUpdating = false;
    }

    private void deleteNode() {
        Object selection = treChapList.getLastSelectedPathComponent();
        DefaultTreeModel model = (DefaultTreeModel) treChapList.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

        if (selection != null
                && core.getOSHandler().getInfoBox().yesNoCancel("Confirmation", "Really delete? This cannot be undone.")
                        == JOptionPane.YES_OPTION) {
            if (selection instanceof GrammarSectionNode) {
                DesktopGrammarSectionNode curNode = (DesktopGrammarSectionNode) selection;
                DesktopGrammarChapNode parent = (DesktopGrammarChapNode) curNode.getParent();
                parent.doRemove(curNode);
                treChapList.expandPath(new TreePath(model.getPathToRoot(parent)));
                treChapList.setSelectionPath(new TreePath(model.getPathToRoot(parent)));
            } else if (selection instanceof GrammarChapNode) {
                ((DesktopGrammarChapNode) root).doRemove((DesktopGrammarChapNode) selection);
                gramMan.removeChapter((DesktopGrammarChapNode) selection);
            }

            model.reload(root);
        }
    }
    
    private void moveNodeUp() {
        moveNode(-1);
    }
    
    private void moveNodeDown() {
        moveNode(1);
    }
    
    private void moveNode(int distance) {
        Object selection = treChapList.getLastSelectedPathComponent();
        DefaultTreeModel model = (DefaultTreeModel) treChapList.getModel();
        DesktopGrammarChapNode root = (DesktopGrammarChapNode) model.getRoot();
        TreePath selectedPath = treChapList.getSelectionPath();
        
        if (selection != null) {
            if (selection instanceof DesktopGrammarSectionNode curNode) {
                DesktopGrammarChapNode parent = (DesktopGrammarChapNode) curNode.getParent();
                int nodeIndex = parent.getIndex(curNode);
                int newLocation = nodeIndex + distance;
                
                if (newLocation >= 0 && newLocation < parent.getChildCount()) {
                    parent.remove(nodeIndex);
                    parent.insert(curNode, newLocation);
                    model.reload(root);
                    treChapList.setSelectionPath(selectedPath);
                }
            } else if (selection instanceof DesktopGrammarChapNode chapter) {
                int chapIndex = root.getIndex(chapter);
                int newLocation = chapIndex + distance;
                
                if (newLocation >= 0 && newLocation < root.getChildCount()) {
                    root.doRemove(chapter);
                    root.doInsert(chapter, newLocation);
                    model.reload(root);
                    treChapList.setSelectionPath(selectedPath);
                }
            }
        }
    }

    private void addChapter() {
        DefaultTreeModel model = (DefaultTreeModel) treChapList.getModel();
        Object selection = treChapList.getLastSelectedPathComponent();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        DesktopGrammarChapNode newNode = new DesktopGrammarChapNode(gramMan);
        newNode.setName("NEW CHAPTER");

        if (selection instanceof DesktopGrammarChapNode selectionNode) {
            DesktopGrammarChapNode parent = (DesktopGrammarChapNode)selectionNode.getParent();
            int index = root.getIndex(parent);
            model.insertNodeInto(newNode, root, index + 1);
            gramMan.addChapterAtIndex(newNode, index + 1);
        } else if (selection instanceof DesktopGrammarChapNode selectionNode) {
            int index = root.getIndex(selectionNode);
            model.insertNodeInto(newNode, root, index + 1);
            gramMan.addChapterAtIndex(newNode, index + 1);
        } else {
            root.add(newNode);
            gramMan.addChapter(newNode);
        }

        model.reload();
        treChapList.setSelectionPath(new TreePath(model.getPathToRoot(newNode)));
        txtName.setText("");
        txtName.setForeground(Color.gray);
        
        btnAddChapter.setEnabled(false);
        btnAddSection.setEnabled(false);
    }

    private void addSection() {
        DefaultTreeModel model = (DefaultTreeModel) treChapList.getModel();
        Object selection = treChapList.getLastSelectedPathComponent();

        if (selection instanceof DesktopGrammarSectionNode selectionNode) {
            DesktopGrammarChapNode parent = (DesktopGrammarChapNode) selectionNode.getParent();
            int index = parent.getIndex(selectionNode);
            DesktopGrammarSectionNode newNode = gramMan.getNewSection();
            newNode.setName("NEW SECTION");
            model.insertNodeInto(newNode, parent, index + 1);
            model.reload();
            treChapList.setSelectionPath(new TreePath(model.getPathToRoot(newNode)));
            btnAddChapter.setEnabled(false);
            btnAddSection.setEnabled(false);
        } else if (selection instanceof DesktopGrammarChapNode parent) {
            DesktopGrammarSectionNode newNode = gramMan.getNewSection();
            newNode.setName("NEW SECTION");
            parent.add(newNode);
            model.reload();
            treChapList.setSelectionPath(new TreePath(model.getPathToRoot(newNode)));
            btnAddChapter.setEnabled(false);
            btnAddSection.setEnabled(false);
        } else {
            core.getOSHandler().getInfoBox().warning("Section Creation", "Select a chapter in which to create a section.");
        }

        txtName.setText("");
        txtName.setForeground(Color.gray);
    }

    private void playPauseAudio() {
        try {
            soundRecorder.playPause();
        } catch (IOException e) {
            core.getOSHandler().getIOHandler().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Play Error", "Unable to play due to: " + e.getLocalizedMessage());
        }
    }

    private void recordAudio() {
        try {
            if (soundRecorder.isRecording()) {
                btnDeleteRecordedAudio.setEnabled(true);
                btnPlayPauseAudio.setEnabled(true);
                soundRecorder.endRecording();
            } else {
                if (soundRecorder.getSound() != null) { // confirm overwrite of existing data
                    if (core.getOSHandler().getInfoBox().yesNoCancel("Overwrite Confirmation",
                            "Discard existing audio recording?") != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                soundRecorder.beginRecording();
            }
        } catch (Exception e) {
            core.getOSHandler().getIOHandler().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("Recording Error", "Unable to record due to: " + e.getLocalizedMessage());
        }
    }

    public static ScrGrammarGuide run(DictCore _core) {
        final ScrGrammarGuide s = new ScrGrammarGuide(_core);

        // For some reason, adding items to the combobox moves this to the back... this fixes it
        SwingUtilities.invokeLater(() -> {
            s.toFront();
            s.requestFocus();
        });

        return s;
    }

    /**
     * populates all grammar chapters and sections
     */
    private void populateSections() {
        DefaultMutableTreeNode rootNode = new DesktopGrammarChapNode("Root Node", gramMan);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        treChapList.setModel(treeModel);

        for (DesktopGrammarChapNode curChap : gramMan.getChapters()) {
            rootNode.add(curChap);
        }

        treeModel.reload(rootNode);
        treChapList.setLargeModel(true);
    }

    /**
     * Populates all grammar chapters and sections that match search value
     */
    private void populateFromSearch() {
        savePropsToNode((DefaultMutableTreeNode) treChapList.getLastSelectedPathComponent());
        DesktopGrammarChapNode rootNode = new DesktopGrammarChapNode("Root Node", gramMan);

        if (((PTextField) txtSearch).isDefaultText() || txtSearch.getText().isEmpty()) {
            populateSections();
            return;
        }

        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        treChapList.setModel(treeModel);
        for (DesktopGrammarChapNode chapter : gramMan.getChapters()) {
            DesktopGrammarChapNode srcChap = new DesktopGrammarChapNode(gramMan);
            srcChap.setName(chapter.getName());

            Enumeration sections = chapter.children(txtSearch.getText());
            while (sections.hasMoreElements()) {
                DesktopGrammarSectionNode curSec = (DesktopGrammarSectionNode) sections.nextElement();
                srcChap.add(curSec);
            }

            if (srcChap.children().hasMoreElements()) {
                rootNode.add(srcChap);
            }
        }
        
        treeModel.reload(rootNode);
    }

    @Override
    public Component getWindow() {
        return jSplitPane1;
    }

    @Override
    public boolean canClose() {
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        txtName = new PTextField(core, true, "Name");
        jPanel3 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        cmbFonts = new PComboBox(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal());
        txtFontSize = new javax.swing.JTextField();
        cmbFontColor = new PComboBox(((DesktopPropertiesManager)core.getPropertiesManager()).getFontMenu());
        btnApply = new PButton(nightMode);
        panSection = new javax.swing.JScrollPane();
        txtSection = new PGrammarPane(core);
        jPanel4 = new javax.swing.JPanel();
        sldSoundPosition = new javax.swing.JSlider();
        jToolBar2 = new javax.swing.JToolBar();
        btnRecordAudio = new JButton();
        btnPlayPauseAudio = new JButton();
        jPanel5 = new javax.swing.JPanel();
        txtTimer = new javax.swing.JTextField();
        btnDeleteRecordedAudio = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        treChapList = new PTree(((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal(), nightMode);
        txtSearch = new PTextField(core, true, "Search");
        jLabel1 = new PLabel("");
        btnAddSection = new PAddRemoveButton("+");
        btnDelete = new PAddRemoveButton("-");
        btnAddChapter = new PButton(nightMode);
        btnMoveNodeDown = new PButton();
        btnMoveNodeUp = new PButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Grammar Guide");

        jSplitPane1.setBackground(new java.awt.Color(255, 255, 255));
        jSplitPane1.setDividerLocation(220);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        txtName.setToolTipText("Name of chapter/section");
        txtName.setEnabled(false);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        cmbFonts.setToolTipText("Text font");
        cmbFonts.setEnabled(false);
        cmbFonts.setMaximumSize(new java.awt.Dimension(120, 32767));
        cmbFonts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFontsActionPerformed(evt);
            }
        });
        jToolBar1.add(cmbFonts);

        txtFontSize.setText("12");
        txtFontSize.setToolTipText("Font Size");
        txtFontSize.setEnabled(false);
        txtFontSize.setMaximumSize(new java.awt.Dimension(40, 20));
        txtFontSize.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtFontSizeFocusLost(evt);
            }
        });
        txtFontSize.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtFontSizeKeyPressed(evt);
            }
        });
        jToolBar1.add(txtFontSize);

        cmbFontColor.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "black", "red", "blue", "green", "gray", "yellow" }));
        cmbFontColor.setToolTipText("Font Color");
        cmbFontColor.setEnabled(false);
        cmbFontColor.setMaximumSize(new java.awt.Dimension(96, 20));
        cmbFontColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFontColorActionPerformed(evt);
            }
        });
        jToolBar1.add(cmbFontColor);

        btnApply.setText(" Apply ");
        btnApply.setToolTipText("Switches currently selected text to current font style");
        btnApply.setEnabled(false);
        btnApply.setFocusable(false);
        btnApply.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnApply.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplyActionPerformed(evt);
            }
        });
        jToolBar1.add(btnApply);

        txtSection.setToolTipText("Formatted segment text");
        txtSection.setEnabled(false);
        panSection.setViewportView(txtSection);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(panSection)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panSection, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setMaximumSize(new java.awt.Dimension(32767, 76));
        jPanel4.setMinimumSize(new java.awt.Dimension(100, 76));

        sldSoundPosition.setToolTipText("");
        sldSoundPosition.setValue(0);
        sldSoundPosition.setEnabled(false);
        sldSoundPosition.setMinimumSize(new java.awt.Dimension(10, 29));

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        btnRecordAudio.setText("Record");
        btnRecordAudio.setToolTipText("Records spoken example of grammar (erases any current example)");
        btnRecordAudio.setBorderPainted(false);
        btnRecordAudio.setEnabled(false);
        btnRecordAudio.setFocusable(false);
        btnRecordAudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRecordAudio.setMaximumSize(new java.awt.Dimension(40, 40));
        btnRecordAudio.setMinimumSize(new java.awt.Dimension(40, 40));
        btnRecordAudio.setPreferredSize(new java.awt.Dimension(40, 40));
        btnRecordAudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRecordAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecordAudioActionPerformed(evt);
            }
        });

        btnPlayPauseAudio.setText("Play");
        btnPlayPauseAudio.setToolTipText("Plays spoken example of grammar, if it exists.");
        btnPlayPauseAudio.setBorderPainted(false);
        btnPlayPauseAudio.setEnabled(false);
        btnPlayPauseAudio.setFocusable(false);
        btnPlayPauseAudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPlayPauseAudio.setMaximumSize(new java.awt.Dimension(40, 40));
        btnPlayPauseAudio.setMinimumSize(new java.awt.Dimension(40, 40));
        btnPlayPauseAudio.setName(""); // NOI18N
        btnPlayPauseAudio.setPreferredSize(new java.awt.Dimension(40, 40));
        btnPlayPauseAudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnPlayPauseAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlayPauseAudioActionPerformed(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txtTimer.setEditable(false);
        txtTimer.setBackground(new java.awt.Color(0, 0, 0));
        txtTimer.setForeground(new java.awt.Color(51, 255, 51));
        txtTimer.setText("00:00:00");
        txtTimer.setToolTipText("Recording/play time");
        txtTimer.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(txtTimer, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(txtTimer, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        btnDeleteRecordedAudio.setText("delete");
        btnDeleteRecordedAudio.setToolTipText("Deletes recorded audio");
        btnDeleteRecordedAudio.setBorderPainted(false);
        btnDeleteRecordedAudio.setEnabled(false);
        btnDeleteRecordedAudio.setFocusable(false);
        btnDeleteRecordedAudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDeleteRecordedAudio.setMaximumSize(new java.awt.Dimension(40, 40));
        btnDeleteRecordedAudio.setMinimumSize(new java.awt.Dimension(40, 40));
        btnDeleteRecordedAudio.setPreferredSize(new java.awt.Dimension(40, 40));
        btnDeleteRecordedAudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDeleteRecordedAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteRecordedAudioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sldSoundPosition, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPlayPauseAudio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnRecordAudio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDeleteRecordedAudio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnRecordAudio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnPlayPauseAudio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDeleteRecordedAudio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(sldSoundPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(300, Short.MAX_VALUE))
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setRightComponent(jPanel2);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("JTree");
        treChapList.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        treChapList.setToolTipText("Chapter Guide");
        jScrollPane1.setViewportView(treChapList);

        jLabel1.setText("Sections");

        btnAddSection.setText("+");
        btnAddSection.setToolTipText("Add a new section to a chapter");
        btnAddSection.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnAddSection.setMaximumSize(new java.awt.Dimension(40, 29));
        btnAddSection.setMinimumSize(new java.awt.Dimension(40, 29));
        btnAddSection.setPreferredSize(new java.awt.Dimension(40, 29));
        btnAddSection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddSectionActionPerformed(evt);
            }
        });

        btnDelete.setText("-");
        btnDelete.setToolTipText("Delete current chapter/node");
        btnDelete.setMaximumSize(new java.awt.Dimension(40, 29));
        btnDelete.setMinimumSize(new java.awt.Dimension(40, 29));
        btnDelete.setPreferredSize(new java.awt.Dimension(40, 29));
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnAddChapter.setText("Chapter");
        btnAddChapter.setToolTipText("Create a new chapter");
        btnAddChapter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddChapterActionPerformed(evt);
            }
        });

        btnMoveNodeDown.setText("↓");
        btnMoveNodeDown.setToolTipText("Move chapter/section down");
        btnMoveNodeDown.setMaximumSize(new java.awt.Dimension(40, 29));
        btnMoveNodeDown.setMinimumSize(new java.awt.Dimension(40, 29));
        btnMoveNodeDown.setPreferredSize(new java.awt.Dimension(40, 29));
        btnMoveNodeDown.setSize(new java.awt.Dimension(40, 29));
        btnMoveNodeDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveNodeDownActionPerformed(evt);
            }
        });

        btnMoveNodeUp.setText("↑");
        btnMoveNodeUp.setToolTipText("Move chapter/section up");
        btnMoveNodeUp.setMaximumSize(new java.awt.Dimension(40, 29));
        btnMoveNodeUp.setMinimumSize(new java.awt.Dimension(40, 29));
        btnMoveNodeUp.setPreferredSize(new java.awt.Dimension(40, 29));
        btnMoveNodeUp.setSize(new java.awt.Dimension(40, 29));
        btnMoveNodeUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveNodeUpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtSearch)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnMoveNodeUp, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMoveNodeDown, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(btnAddSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAddChapter)
                .addGap(4, 4, 4)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 35, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnMoveNodeUp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnMoveNodeDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddChapter)))
        );

        jSplitPane1.setLeftComponent(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRecordAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordAudioActionPerformed
        recordAudio();
    }//GEN-LAST:event_btnRecordAudioActionPerformed

    private void btnPlayPauseAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayPauseAudioActionPerformed
        playPauseAudio();
    }//GEN-LAST:event_btnPlayPauseAudioActionPerformed

    private void btnAddChapterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddChapterActionPerformed
        addChapter();
    }//GEN-LAST:event_btnAddChapterActionPerformed

    private void btnAddSectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddSectionActionPerformed
        addSection();
    }//GEN-LAST:event_btnAddSectionActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        deleteNode();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyActionPerformed
        setFont();
    }//GEN-LAST:event_btnApplyActionPerformed

    private void cmbFontsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFontsActionPerformed
        setFont();
    }//GEN-LAST:event_cmbFontsActionPerformed

    private void cmbFontColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFontColorActionPerformed
        setFont();
    }//GEN-LAST:event_cmbFontColorActionPerformed

    private void txtFontSizeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtFontSizeFocusLost
        updateFontSize();
    }//GEN-LAST:event_txtFontSizeFocusLost

    private void txtFontSizeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFontSizeKeyPressed
        int event = evt.getKeyCode();
        if (event == KeyEvent.VK_ENTER) {
            updateFontSize();
        }
    }//GEN-LAST:event_txtFontSizeKeyPressed

    private void btnMoveNodeUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveNodeUpActionPerformed
        moveNodeUp();
    }//GEN-LAST:event_btnMoveNodeUpActionPerformed

    private void btnMoveNodeDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveNodeDownActionPerformed
        moveNodeDown();
    }//GEN-LAST:event_btnMoveNodeDownActionPerformed

    private void btnDeleteRecordedAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRecordedAudioActionPerformed
        var infoBox = new DesktopInfoBox();
        Object selection = treChapList.getSelectionPath().getLastPathComponent();
        
        closeAllPlayRecord();

        if (selection instanceof GrammarSectionNode grammarSectionNode
                && infoBox.deletionConfirmation()) {
            grammarSectionNode.clearRecording();
            soundRecorder.setSound(null);
            btnPlayPauseAudio.setEnabled(false);
            btnDeleteRecordedAudio.setEnabled(false);
        }
    }//GEN-LAST:event_btnDeleteRecordedAudioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddChapter;
    private javax.swing.JButton btnAddSection;
    private javax.swing.JButton btnApply;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDeleteRecordedAudio;
    private javax.swing.JButton btnMoveNodeDown;
    private javax.swing.JButton btnMoveNodeUp;
    private javax.swing.JButton btnPlayPauseAudio;
    private javax.swing.JButton btnRecordAudio;
    private javax.swing.JComboBox cmbFontColor;
    private javax.swing.JComboBox<Object> cmbFonts;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JScrollPane panSection;
    private javax.swing.JSlider sldSoundPosition;
    private javax.swing.JTree treChapList;
    private javax.swing.JTextField txtFontSize;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextPane txtSection;
    private javax.swing.JTextField txtTimer;
    // End of variables declaration//GEN-END:variables
}
