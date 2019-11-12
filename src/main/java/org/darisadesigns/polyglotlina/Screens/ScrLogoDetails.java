/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Screens;

import org.darisadesigns.polyglotlina.ClipboardHandler;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.Nodes.LogoNode;
import org.darisadesigns.polyglotlina.CustomControls.PButton;
import org.darisadesigns.polyglotlina.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.PGTUtil.WindowMode;
import org.darisadesigns.polyglotlina.CustomControls.PCellEditor;
import org.darisadesigns.polyglotlina.CustomControls.PCellRenderer;
import org.darisadesigns.polyglotlina.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.CustomControls.PList;
import org.darisadesigns.polyglotlina.CustomControls.PTable;
import org.darisadesigns.polyglotlina.CustomControls.PTextPane;
import org.darisadesigns.polyglotlina.IOHandler;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.darisadesigns.polyglotlina.CustomControls.PAddRemoveButton;

/**
 *
 * @author draque
 */
public class ScrLogoDetails extends PFrame {
    private boolean selectOnlyMode = false;
    private boolean curPopulating = false;
    private ScrLogoQuickView quickView = null;

    /**
     * Creates new form ScrLogoDetails
     *
     * @param _core
     */
    public ScrLogoDetails(DictCore _core) {
        super(_core);
        createNew(_core, -1);
    }

    /**
     * Opens logo window with particular logograph selected
     *
     * @param _core
     * @param logoId
     */
    public ScrLogoDetails(DictCore _core, int logoId) {
        super(_core);
        createNew(_core, logoId);
    }
    
    private void createNew(DictCore _core, int logoId) {
        core = _core;
        initComponents();
        setupFonts();
            
        if (logoId == -1) {
            mode = WindowMode.STANDARD;
            populateLogographs();

            if (System.getProperty("os.name").startsWith("Mac")) {
                btnAddLogo.setToolTipText(btnAddLogo.getToolTipText() + " (⌘ +)");
                btnDelLogo.setToolTipText(btnDelLogo.getToolTipText() + " (⌘ -)");
            } else {
                btnAddLogo.setToolTipText(btnAddLogo.getToolTipText() + " (CTRL +)");
                btnDelLogo.setToolTipText(btnDelLogo.getToolTipText() + " (CTRL -)");
            }
        } else {
            LogoNode singleModeLogo = (LogoNode) core.getLogoCollection().getNodeById(logoId);
            List<LogoNode> list = new ArrayList<>();
            list.add(singleModeLogo);

            populateLogographs(list.iterator());

            setSingleLogoMode(true);
            setTitle("Logograph Details/Modification");
            mode = WindowMode.SINGLEVALUE;
        }
        
        populateLogoProps();
        setupListeners();
        super.getRootPane().getContentPane().setBackground(Color.white);
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        Action addAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addLogo();
            }
        };
        Action delAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteLogo();
            }
        };
        String addKey = "addLogoGraph";
        String delKey = "delLogoGraph";
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
    public final void setTitle(String title) {
        super.setTitle(title);
    }

    @Override
    public void updateAllValues(DictCore _core) {
        if (core != _core) {
            core = _core;
            populateLogographs();
            populateLogoProps();
        }
        setupFonts();
    }

    /**
     * Sets up fonts based on core properties
     */
    private void setupFonts() {
        Font font = core.getPropertiesManager().getFontCon();

        if (font == null) {
            return;
        }

        lstLogos.setFont(font);
        lstRelWords.setFont(font);
        lstRadicals.setFont(font);
        tblReadings.setFont(font);
    }

    /**
     * sets up custom listeners
     */
    private void setupListeners() {
        txtName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateName();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateName();
            }
        });
        txtNotes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateNotes();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateNotes();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateNotes();
            }
        });
        txtStrokes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStrokes();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateStrokes();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStrokes();
            }
        });
        fltStrokes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (checkStrokeFilter()) {
                    runFilter();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (checkStrokeFilter()) {
                    runFilter();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (checkStrokeFilter()) {
                    runFilter();
                }
            }
        });
        fltRadical.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                runFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                runFilter();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                runFilter();
            }
        });
        fltReading.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                runFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                runFilter();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                runFilter();
            }
        });
        fltRelatedWord.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                runFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                runFilter();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                runFilter();
            }
        });
        fltNotes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                runFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                runFilter();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                runFilter();
            }
        });

        final JPopupMenu ruleMenu = new JPopupMenu();
        final JMenuItem pasteImage = new JMenuItem("Paste Image");
        pasteImage.addActionListener((ActionEvent ae) -> {
            pasteLogograph();
        });
        ruleMenu.add(pasteImage);

        lblLogo.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && txtName.isEnabled()) {
                    pasteImage.setEnabled(true);
                    ruleMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && txtName.isEnabled()) {
                    pasteImage.setEnabled(true);
                    ruleMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * Enables or disables controls for logograph
     *
     * @param enable
     */
    private void setEnableControls(boolean enable) {
        btnLoadImage.setEnabled(enable && !selectOnlyMode);
        btnClipboard.setEnabled(enable && !selectOnlyMode);
        tblReadings.setEnabled(enable && !selectOnlyMode);
        lstRadicals.setEnabled(enable);
        btnAddRad.setEnabled(enable && !selectOnlyMode);
        chkIsRad.setEnabled(enable && !selectOnlyMode);
        btnDelReading.setEnabled(enable && !selectOnlyMode);
        btnAddReading.setEnabled(enable && !selectOnlyMode);
        btnDelRad.setEnabled(enable && !selectOnlyMode);
        txtName.setEnabled(enable && !selectOnlyMode);
        txtStrokes.setEnabled(enable && !selectOnlyMode);
        txtNotes.setEnabled(enable && !selectOnlyMode);
        lstRelWords.setEnabled(enable);
    }

    private void pasteLogograph() {
        if (ClipboardHandler.isClipboardImage()) {
            try {
                LogoNode curNode = (LogoNode) lstLogos.getSelectedValue();

                if (curNode == null) {
                    return;
                }

                Image image = ClipboardHandler.getClipboardImage();
                curNode.setLogoGraph(image);
                saveReadings(lstLogos.getSelectedIndex());
                saveRads(lstLogos.getSelectedIndex());
                populateLogoProps();
            } catch (Exception e) {
                IOHandler.writeErrorLog(e);
                InfoBox.error("Paste Error", "Unable to paste: " + e.getLocalizedMessage(), core.getRootWindow());
            }
        } else {
            InfoBox.warning("Image Format Incompatibility",
                    "The contents of the clipboard is not an image, or is an unrecognized format", core.getRootWindow());
        }
    }

    private void updateStrokes() {
        LogoNode curNode = (LogoNode) lstLogos.getSelectedValue();

        if (curNode == null || curPopulating) {
            return;
        }

        // interpret blank field as 0 to avoid error below
        if (txtStrokes.getText().isEmpty()) {
            curNode.setStrokes(0);
            return;
        }

        try {
            int strokes = Integer.parseInt(txtStrokes.getText());
            curNode.setStrokes(strokes);
        } catch (NumberFormatException e) {
            // run later to avoid update conflicts
            // user error
            // IOHandler.writeErrorLog(e);
            java.awt.EventQueue.invokeLater(() -> {
                curPopulating = true;
                txtStrokes.setText("");
                InfoBox.info("Type mismatch", "Strokes field compatible with integer values only", core.getRootWindow());
                curPopulating = false;
            });
        }
    }

    private void updateNotes() {
        LogoNode curNode = (LogoNode) lstLogos.getSelectedValue();

        if (curNode == null || curPopulating) {
            return;
        }

        curNode.setNotes(txtNotes.getText());
    }

    private void updateName() {
        LogoNode curNode = (LogoNode) lstLogos.getSelectedValue();
        boolean localPopulating = curPopulating;
        
        if (curNode == null || curPopulating) {
            return;
        }
        
        curPopulating = true;
        
        curNode.setValue(txtName.getText());
        populateLogographs();
        lstLogos.setSelectedValue(curNode, true);
        lstLogos.updateUI();
        
        curPopulating = localPopulating;
    }

    private void updateIsRadical() {
        LogoNode curNode = (LogoNode) lstLogos.getSelectedValue();

        if (curNode == null || curPopulating) {
            return;
        }

        curNode.setRadical(chkIsRad.isSelected());
    }

    /**
     * saves radicals to given node index
     *
     * @param nodeIndex
     */
    private void saveRads(int nodeIndex) {
        saveRads(nodeIndex, false);
    }

    /**
     * saves radicals to given node index with option to override population
     * lock
     *
     * @param nodeIndex
     * @param overridePopulatingLock
     */
    private void saveRads(int nodeIndex, boolean overridePopulatingLock) {
        // catches case of saving on a delete (obviously bad)
        if (nodeIndex >= lstLogos.getModel().getSize()
                || nodeIndex < 0) {
            return;
        }

        LogoNode curNode = (LogoNode) ((DefaultListModel) lstLogos.getModel()).get(nodeIndex);

        if (curNode == null
                || (curPopulating && !overridePopulatingLock)) {
            return;
        }

        List<LogoNode> radList = new ArrayList<>();
        DefaultListModel radModel = (DefaultListModel) lstRadicals.getModel();

        for (int i = 0; i < radModel.getSize(); i++) {
            LogoNode curRad = (LogoNode) radModel.get(i);
            radList.add(curRad);
        }

        curNode.setRadicals(radList);
    }

    /**
     * saves readings to given node index
     *
     * @param nodeIndex
     */
    private void saveReadings(int nodeIndex) {
        saveReadings(nodeIndex, false);
    }

    /**
     * saves readings to given node index with option to override population
     * lock
     *
     * @param nodeIndex
     * @param overridePopulatingLock
     */
    private void saveReadings(int nodeIndex, boolean overridePopulatingLock) {
        // catches case of saving on a delete (obviously bad)
        if (nodeIndex >= lstLogos.getModel().getSize()
                || nodeIndex < 0) {
            return;
        }

        LogoNode curNode = (LogoNode) ((DefaultListModel) lstLogos.getModel()).get(nodeIndex);

        if (curNode == null
                || (curPopulating && !overridePopulatingLock)) {
            return;
        }

        // forces current cell to save contents if still active
        if (tblReadings.getCellEditor() != null) {
            tblReadings.getCellEditor().stopCellEditing();
        }

        List<String> readList = new ArrayList<>();

        for (int i = 0; i < tblReadings.getModel().getRowCount(); i++) {
            String curReading = (String) tblReadings.getModel().getValueAt(i, 0);

            readList.add(curReading);
        }

        curNode.setReadings(readList);
    }

    /**
     * sets whether mode is for single logograph, or generalized logographs
     *
     * @param set true for single logograph mode, false otherwise
     */
    private void setSingleLogoMode(boolean set) {
        btnAddLogo.setEnabled(!set);
        btnClipboard.setEnabled(!set);
        btnDelLogo.setEnabled(!set);
        fltNotes.setEnabled(!set);
        fltRadical.setEnabled(!set);
        fltReading.setEnabled(!set);
        fltRelatedWord.setEnabled(!set);
        fltStrokes.setEnabled(!set);
    }

    /**
     * populates all related words of currently selected logonode
     */
    private void populateRelatedWords() {
        LogoNode curNode = (LogoNode) lstLogos.getSelectedValue();

        if (curNode == null) {
            lstRelWords.setModel(new DefaultListModel<Object>());
            return;
        }

        Iterator<ConWord> it = core.getLogoCollection().getLogoWords(curNode).iterator();

        DefaultListModel<Object> wordModel = new DefaultListModel<>();

        while (it.hasNext()) {
            wordModel.addElement(it.next());
        }

        lstRelWords.setModel(wordModel);
    }

    /**
     * Applies filter to logograph list, or populates normally if nothing to
     * filter on
     */
    private void runFilter() {
        // before modifying any values, save...
        saveRads(lstLogos.getSelectedIndex());
        saveReadings(lstLogos.getSelectedIndex());

        if (fltNotes.getText().trim().isEmpty()
                && fltRadical.getText().trim().isEmpty()
                && fltReading.getText().trim().isEmpty()
                && fltRelatedWord.getText().trim().isEmpty()
                && fltStrokes.getText().trim().isEmpty()) {
            populateLogographs();
        }

        int strokes = fltStrokes.getText().trim().isEmpty()
                ? 0 : Integer.parseInt(fltStrokes.getText());

        populateLogographs(core.getLogoCollection().getFilteredList(
                fltReading.getText(),
                fltRelatedWord.getText(),
                fltRadical.getText(),
                strokes,
                fltNotes.getText()).iterator());

        populateLogoProps();
    }

    /**
     * Checks whether valid value is in stroke filter & blanks/warns user if not
     *
     * @return true if valid, false otherwise
     */
    private boolean checkStrokeFilter() {
        if (!fltStrokes.getText().isEmpty()) {
            try {
                Integer.parseInt(fltStrokes.getText());
            } catch (NumberFormatException e) {
                // run later to avoid update conflicts
                // user error
                // IOHandler.writeErrorLog(e);
                java.awt.EventQueue.invokeLater(() -> {
                    curPopulating = true;
                    fltStrokes.setText("");
                    InfoBox.info("Type mismatch", "Strokes field compatible with integer values only", core.getRootWindow());
                    curPopulating = false;
                });

                return false;
            }
        }

        return true;
    }

    /**
     * Clears logograph filter
     */
    private void clearFilter() {
        populateLogographs();
    }

    /**
     * Populates all logographs
     */
    private void populateLogographs() {
        if (mode == WindowMode.SINGLEVALUE) {
            populateLogoProps();
        } else {
            populateLogographs(core.getLogoCollection().getAllLogos().iterator());
        }
    }

    /**
     * Populates logographs based on iterator
     *
     * @param it iterator of all logographs to populate
     */
    private void populateLogographs(Iterator<LogoNode> it) {
        DefaultListModel<Object> logoModel = new DefaultListModel<>();

        while (it.hasNext()) {
            LogoNode curLog = it.next();
            logoModel.addElement(curLog);
        }

        curPopulating = true;
        lstLogos.setModel(logoModel);

        lstLogos.setSelectedIndex(0);
        curPopulating = false;
    }

    /**
     * populates all properties of currently selected logograph
     */
    private void populateLogoProps() {
        populateLogoProps(lstLogos.getSelectedIndex());
    }

    /**
     * populates all properties of given logograph at index
     *
     * @param index index to populate props from
     */
    private void populateLogoProps(int index) {
        if (curPopulating) {
            return;
        }

        curPopulating = true;

        if (index == -1) {
            txtName.setText("");
            txtNotes.setText("");
            txtStrokes.setText("");
            lstRadicals.setModel(new DefaultListModel<Object>());
            tblReadings.setModel(new DefaultTableModel(new Object[]{"Readings"}, 0));
            lstRelWords.setModel(new DefaultListModel<Object>());
            chkIsRad.setSelected(false);
            lblLogo.setIcon(new ImageIcon(new LogoNode().getLogoGraph().getScaledInstance(
                    lblLogo.getWidth(), lblLogo.getHeight(), Image.SCALE_SMOOTH)));
            populateRelatedWords();
            setEnableControls(false);

            curPopulating = false;
            return;
        }

        LogoNode curNode = (LogoNode) lstLogos.getModel().getElementAt(index);

        txtName.setText(curNode.getValue());
        txtNotes.setText(curNode.getNotes());
        txtStrokes.setText(String.valueOf(curNode.getStrokes()));
        chkIsRad.setSelected(curNode.isRadical());

        // Populate radicals
        Iterator<LogoNode> radIt = curNode.getRadicals().iterator();
        DefaultListModel<Object> radModel = new DefaultListModel<>();

        while (radIt.hasNext()) {
            try {
                LogoNode radNode = radIt.next();
                radModel.addElement(radNode);
            } catch (Exception e) {
                // do nothing
                IOHandler.writeErrorLog(e);
            }
        }

        lstRadicals.setModel(radModel);

        // Populate readings
        Iterator<String> procIt = curNode.getReadings().iterator();
        // TODO: figure out a way to make this respect RTL languages... maybe just insert char here? and cut at save time? Messy but effective...

        DefaultTableModel procModel = new DefaultTableModel();
        procModel.addColumn("Readings");
        tblReadings.setModel(procModel);

        // Fonts must be set each time the table is rebuilt
        TableColumn column = tblReadings.getColumnModel().getColumn(0);
        column.setCellEditor(new PCellEditor(true, core));
        column.setCellRenderer(new PCellRenderer(true, core));

        while (procIt.hasNext()) {
            Object[] newRow = {procIt.next()};
            procModel.addRow(newRow);
        }
        tblReadings.setModel(procModel);

        // set logograph picture
        lblLogo.setIcon(new ImageIcon(curNode.getLogoGraph().getScaledInstance(
                lblLogo.getWidth(), lblLogo.getHeight(), Image.SCALE_SMOOTH)));

        populateRelatedWords();
        setEnableControls(true);

        curPopulating = false;
    }

    /**
     * refreshes view of related words
     */
    public void refreshRelatedWords() {
        populateRelatedWords();
    }

    /**
     * allows user to open image file and load into selected logograph
     */
    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Logograph Image");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images",
                "BMP", "bmp", "jpeg", "wbmp", "gif", "GIF", "png", "JPG", "jpg", "WBMP", "JPEG", "PNG");
        chooser.setFileFilter(filter);
        String fileName;
        chooser.setCurrentDirectory(new File("."));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }

        try {
            BufferedImage img = ImageIO.read(new File(fileName));
            LogoNode curNode = (LogoNode) lstLogos.getSelectedValue();
            curNode.setLogoGraph(img);
            saveReadings(lstLogos.getSelectedIndex());
            saveRads(lstLogos.getSelectedIndex());
            populateLogoProps();
        } catch (IOException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Image Load Error", "Unable to load image: " + fileName
                    + ": " + e.getMessage(), core.getRootWindow());
        } catch (NullPointerException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Image Load Error", "Unable to read format of image: "
                    + fileName, core.getRootWindow());
        }
    }

    /**
     * deletes currently selected logograph
     */
    private void deleteLogo() {
        LogoNode curNode = (LogoNode) lstLogos.getSelectedValue();

        if (curNode == null || curPopulating) {
            return;
        }

        if (!InfoBox.deletionConfirmation(core.getRootWindow())) {
            return;
        }

        try {
            core.getLogoCollection().deleteNodeById(curNode.getId());
            populateLogographs();

            curPopulating = true;
            lstLogos.setSelectedIndex(0);
            lstLogos.ensureIndexIsVisible(0);
            curPopulating = false;

            populateLogoProps();

            populateLogoProps();
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Logograph Error", "Unable to delete logograph: "
                    + e.getMessage(), core.getRootWindow());
        }
    }

    /**
     * adds a radical from the quickview window to the currently selected
     * logograph
     */
    public void addRadFromQuickview() {
        LogoNode rad = quickView.getCurrentLogo();
        DefaultListModel<Object> radModel = (DefaultListModel<Object>) lstRadicals.getModel();

        if (rad != null) {
            // ignore calls to add radicals already in list
            for (int i = 0; i < radModel.getSize(); i++) {
                if (rad.getId().equals(((LogoNode) radModel.get(i)).getId())) {
                    return;
                }
            }

            radModel.addElement(rad);
        }
    }

    /**
     * Gets currently selected LogoNode, null if none selected
     *
     * @return currently selected LogoNode
     */
    public LogoNode getSelectedLogo() {
        return (LogoNode) lstLogos.getSelectedValue();
    }

    /**
     * deletes currently selected reading
     */
    private void deleteReading() {
        int selectedRow = tblReadings.getSelectedRow();
        DefaultTableModel myModel = (DefaultTableModel) tblReadings.getModel();
        
        if (selectedRow == -1) {
            return;
        }

        // forces current cell to save contents if still active
        if (tblReadings.getCellEditor() != null) {
            tblReadings.getCellEditor().stopCellEditing();
        }
        
        myModel.removeRow(selectedRow);
        saveAllValues();
    }

    @Override
    public void dispose() {
        saveAllValues();

        // dispose of quickview window if not done already
        if (quickView != null && !quickView.isDisposed()) {
            quickView.dispose();
        }

        core.pushUpdate();
        super.dispose();
    }
    
    @Override
    public void saveAllValues() {
        saveReadings(lstLogos.getSelectedIndex());
        saveRads(lstLogos.getSelectedIndex());
    }

    private void addLogo() {
        LogoNode newNode = new LogoNode();
        newNode.setValue("");

        curPopulating = true;
        // save table/list values before continuing
        int curNode = lstLogos.getSelectedIndex();
        if (curNode != -1) {
            saveReadings(curNode, true);
            saveRads(curNode, true);
        }

        try {
            core.getLogoCollection().addNode(newNode);
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Logograph Error", "Unable to create Logograph: " + e.getMessage(), core.getRootWindow());
        }

        populateLogographs();

        lstLogos.setSelectedIndex(0);
        lstLogos.ensureIndexIsVisible(0);

        curPopulating = false;

        populateLogoProps();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        fltRelatedWord = new PTextField(core, false, "-- Related Word --");
        fltStrokes = new PTextField(core, true, "-- Strokes --");
        jLabel3 = new PLabel("", menuFontSize);
        fltReading = new PTextField(core, false, "-- Reading --");
        fltNotes = new PTextField(core, true, "-- Notes --");
        fltRadical = new PTextField(core, false, "-- Radical --");
        jScrollPane1 = new javax.swing.JScrollPane();
        lstLogos = new PList(core.getPropertiesManager().getFontCon());
        btnAddLogo = new PAddRemoveButton("+");
        btnDelLogo = new PAddRemoveButton("-");
        jPanel2 = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstRelWords = new PList(core.getPropertiesManager().getFontCon());
        jLabel8 = new PLabel("", menuFontSize);
        jScrollPane4 = new javax.swing.JScrollPane();
        lstRadicals = new PList(core.getPropertiesManager().getFontCon());
        jLabel10 = new PLabel("", menuFontSize);
        btnAddReading = new PAddRemoveButton("+");
        btnDelReading = new PAddRemoveButton("-");
        btnAddRad = new PAddRemoveButton("+");
        btnDelRad = new PAddRemoveButton("-");
        chkIsRad = new PCheckBox(nightMode, menuFontSize);
        txtName = new PTextField(core, false, "-- Name --");
        jLabel12 = new PLabel("", menuFontSize);
        txtStrokes = new javax.swing.JTextField();
        btnLoadImage = new PButton(nightMode, menuFontSize);
        jScrollPane6 = new javax.swing.JScrollPane();
        tblReadings = new PTable(core);
        jScrollPane3 = new javax.swing.JScrollPane();
        txtNotes = new PTextPane(core, true, "-- Notes --");
        btnClipboard = new PButton(nightMode, menuFontSize);
        jTextField5 = new javax.swing.JTextField();
        jLabel6 = new PLabel("", menuFontSize);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Logograph Details");
        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        fltRelatedWord.setToolTipText("Filter on related words");

        fltStrokes.setToolTipText("Filter by number of strokes");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("<HTML><b>FILTER/SEARCH</b>");

        fltReading.setToolTipText("Filter by reading");

        fltNotes.setToolTipText("Filter on text in notes");

        fltRadical.setToolTipText("Filter by radical name");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fltRelatedWord)
                            .addComponent(fltStrokes)
                            .addComponent(fltRadical, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(fltReading, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                            .addComponent(fltNotes))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fltRelatedWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fltReading, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fltStrokes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fltNotes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fltRadical, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        lstLogos.setModel(new javax.swing.AbstractListModel<Object>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstLogos.setToolTipText("Logographs");
        lstLogos.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstLogosValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstLogos);

        btnAddLogo.setToolTipText("New logograph");
        btnAddLogo.setPreferredSize(new java.awt.Dimension(40, 29));
        btnAddLogo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddLogoActionPerformed(evt);
            }
        });

        btnDelLogo.setToolTipText("Delete selected logograph");
        btnDelLogo.setPreferredSize(new java.awt.Dimension(40, 29));
        btnDelLogo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelLogoActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblLogo.setText("jLabel6");
        lblLogo.setToolTipText("Logograph image");
        lblLogo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        lblLogo.setMaximumSize(new java.awt.Dimension(49, 49));
        lblLogo.setMinimumSize(new java.awt.Dimension(49, 49));
        lblLogo.setName(""); // NOI18N
        lblLogo.setPreferredSize(new java.awt.Dimension(49, 49));

        jScrollPane2.setMinimumSize(new java.awt.Dimension(0, 0));

        lstRelWords.setModel(new javax.swing.AbstractListModel<Object>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstRelWords.setToolTipText("Words related to logograph (generated from relations set on words, and uneditable)");
        lstRelWords.setMinimumSize(new java.awt.Dimension(0, 80));
        jScrollPane2.setViewportView(lstRelWords);

        jLabel8.setText("Radicals");
        jLabel8.setMinimumSize(new java.awt.Dimension(0, 14));

        jScrollPane4.setPreferredSize(new java.awt.Dimension(0, 130));

        lstRadicals.setModel(new javax.swing.AbstractListModel<Object>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstRadicals.setToolTipText("Radicals of which logograph is comprised");
        lstRadicals.setMinimumSize(new java.awt.Dimension(0, 80));
        lstRadicals.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstRadicalsValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(lstRadicals);

        jLabel10.setText("Related Words");

        btnAddReading.setToolTipText("Add reading");
        btnAddReading.setPreferredSize(new java.awt.Dimension(40, 29));
        btnAddReading.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddReadingActionPerformed(evt);
            }
        });

        btnDelReading.setToolTipText("Remove selected reading");
        btnDelReading.setPreferredSize(new java.awt.Dimension(40, 29));
        btnDelReading.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelReadingActionPerformed(evt);
            }
        });

        btnAddRad.setToolTipText("Add radical");
        btnAddRad.setPreferredSize(new java.awt.Dimension(40, 29));
        btnAddRad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRadActionPerformed(evt);
            }
        });

        btnDelRad.setToolTipText("Remove selected radical");
        btnDelRad.setPreferredSize(new java.awt.Dimension(40, 29));
        btnDelRad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelRadActionPerformed(evt);
            }
        });

        chkIsRad.setText("Is Radical");
        chkIsRad.setToolTipText("Whether logograph is a radical which other logographs can be constructed from");
        chkIsRad.setEnabled(false);
        chkIsRad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkIsRadActionPerformed(evt);
            }
        });

        txtName.setToolTipText("Name of logograph");

        jLabel12.setText("Strokes");

        txtStrokes.setToolTipText("Number of strokes to write logograph");

        btnLoadImage.setText("Load Image");
        btnLoadImage.setToolTipText("Load image for logograph from file");
        btnLoadImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadImageActionPerformed(evt);
            }
        });

        jScrollPane6.setMinimumSize(new java.awt.Dimension(0, 23));

        tblReadings.setModel(new javax.swing.table.DefaultTableModel(
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
        tblReadings.setToolTipText("Logograph readings");
        tblReadings.setMinimumSize(new java.awt.Dimension(0, 120));
        tblReadings.setRowHeight(30);
        jScrollPane6.setViewportView(tblReadings);

        jScrollPane3.setMinimumSize(new java.awt.Dimension(0, 23));
        jScrollPane3.setViewportView(txtNotes);

        btnClipboard.setText("Clipboard");
        btnClipboard.setToolTipText("Copy logograph image from clipboard");
        btnClipboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClipboardActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(15, 15, 15))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtStrokes, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(chkIsRad)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnLoadImage, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClipboard, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAddReading, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                                .addComponent(btnDelReading, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnAddRad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnDelRad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .addComponent(txtName)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(lblLogo, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnLoadImage)
                        .addComponent(btnClipboard))
                    .addComponent(btnAddReading, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelReading, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelRad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddRad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(txtStrokes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkIsRad))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTextField5.setEditable(false);
        jTextField5.setDisabledTextColor(new java.awt.Color(255, 0, 0));
        jTextField5.setDoubleBuffered(true);
        jTextField5.setEnabled(false);

        jLabel6.setText("Logographs");
        jLabel6.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnAddLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnDelLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField5)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(6, 6, 6))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * Adds a logograph while keeping everything tidy
     *
     * @param evt event leading to action
     */
    private void btnAddLogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddLogoActionPerformed
        addLogo();
    }//GEN-LAST:event_btnAddLogoActionPerformed

    private void lstLogosValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstLogosValueChanged
        // prevent double firing
        if (evt.getValueIsAdjusting() || curPopulating) {
            return;
        }

        int selected = lstLogos.getSelectedIndex();
        final int previous = selected == evt.getFirstIndex() ? evt.getLastIndex() : evt.getFirstIndex();

        java.awt.EventQueue.invokeLater(() -> {
            saveReadings(previous);
            saveRads(previous);
            populateLogoProps();
        });
    }//GEN-LAST:event_lstLogosValueChanged

    private void btnLoadImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadImageActionPerformed
        if (lstLogos.getSelectedIndex() == -1) {
            return;
        }

        openImage();
    }//GEN-LAST:event_btnLoadImageActionPerformed

    private void chkIsRadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkIsRadActionPerformed
        updateIsRadical();
    }//GEN-LAST:event_chkIsRadActionPerformed

    private void btnAddReadingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddReadingActionPerformed
        if (lstLogos.getSelectedIndex() == -1) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tblReadings.getModel();
        Object[] newRow = {"New Reading"};
        model.addRow(newRow);
    }//GEN-LAST:event_btnAddReadingActionPerformed

    private void btnDelLogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelLogoActionPerformed
        deleteLogo();
    }//GEN-LAST:event_btnDelLogoActionPerformed

    private void btnDelReadingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelReadingActionPerformed
        deleteReading();
    }//GEN-LAST:event_btnDelReadingActionPerformed

    private void btnAddRadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRadActionPerformed
        if (lstLogos.getSelectedIndex() == -1) {
            return;
        }

        if (quickView == null || quickView.isDisposed()) {
            quickView = new ScrLogoQuickView(core, true);
            quickView.setBeside(this);
            quickView.setLogoParent(this);
            quickView.setVisible(true);
        } else {
            addRadFromQuickview();
        }
    }//GEN-LAST:event_btnAddRadActionPerformed

    private void lstRadicalsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstRadicalsValueChanged
        // set currently selected radical as selected in quickview window if it is open
        if (quickView != null
                && !quickView.isDisposed()) {
            LogoNode curRad = (LogoNode) lstRadicals.getSelectedValue();

            if (curRad != null) {
                quickView.setSelectedLogo(curRad.getId());
            }
        }
    }//GEN-LAST:event_lstRadicalsValueChanged

    private void btnDelRadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelRadActionPerformed
        if (lstRadicals.getSelectedIndex() == -1) {
            return;
        }

        ((DefaultListModel) lstRadicals.getModel()).
                remove(lstRadicals.getSelectedIndex());
    }//GEN-LAST:event_btnDelRadActionPerformed

    private void btnClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClipboardActionPerformed
        pasteLogograph();
    }//GEN-LAST:event_btnClipboardActionPerformed

    /**
     * Sets window to word fetch mode, which alters the title and eliminates the
     * ability of users to modify logographs.
     *
     * @param fetch whether to enable fetch mode
     */
    public void setWordFetchMode(boolean fetch) {
        if (fetch) {
            setTitle("Asscociate Logograph with Selected Conword");
        }

        btnAddLogo.setEnabled(!fetch);
        btnClipboard.setEnabled(!fetch);
        btnAddRad.setEnabled(!fetch);
        btnAddReading.setEnabled(!fetch);
        btnDelLogo.setEnabled(!fetch);
        btnDelRad.setEnabled(!fetch);
        btnDelReading.setEnabled(!fetch);
        btnLoadImage.setEnabled(!fetch);
        txtName.setEnabled(!fetch);
        txtNotes.setEnabled(!fetch);
        txtStrokes.setEnabled(!fetch);
        chkIsRad.setEnabled(!fetch);
        tblReadings.setEnabled(!fetch);
        
        selectOnlyMode = fetch;
    }

    public static ScrLogoDetails run(DictCore _core) {
        final ScrLogoDetails s = new ScrLogoDetails(_core);
        s.addBindingsToPanelComponents(s.getRootPane());

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            s.setVisible(true);
        });

        return s;
    }

    @Override
    public Component getWindow() {
        return this.getRootPane();
    }

    @Override
    public boolean canClose() {
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddLogo;
    private javax.swing.JButton btnAddRad;
    private javax.swing.JButton btnAddReading;
    private javax.swing.JButton btnClipboard;
    private javax.swing.JButton btnDelLogo;
    private javax.swing.JButton btnDelRad;
    private javax.swing.JButton btnDelReading;
    private javax.swing.JButton btnLoadImage;
    private javax.swing.JCheckBox chkIsRad;
    private javax.swing.JTextField fltNotes;
    private javax.swing.JTextField fltRadical;
    private javax.swing.JTextField fltReading;
    private javax.swing.JTextField fltRelatedWord;
    private javax.swing.JTextField fltStrokes;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JList<Object> lstLogos;
    private javax.swing.JList<Object> lstRadicals;
    private javax.swing.JList<Object> lstRelWords;
    private javax.swing.JTable tblReadings;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextPane txtNotes;
    private javax.swing.JTextField txtStrokes;
    // End of variables declaration//GEN-END:variables
}
