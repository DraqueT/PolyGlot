/*
 * Copyright (c) 2014, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 * See LICENSE.TXT included with this code to read the full license agreement.
 *
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
 
package PolyGlot;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author draque
 */
public class ScrTranslationWindow extends PFrame {
    private final DictCore core;
    public final ScrDictInterface parent;
    private boolean curPopulating = false;
    private List<String> localText = new ArrayList<String>();
    private List<String> transText = new ArrayList<String>();
    private final DefaultListModel matchListModel;
    private final Map<Integer, Integer> scrToCoreMap = new HashMap<Integer, Integer>();
    private final Map<Integer, Integer> transToLocal = new HashMap<Integer, Integer>();
    private boolean curTranslating = false;
    private final List<PFrame> childFrames = new ArrayList<PFrame>();
    
    // list of related indexes, the first being 
    List<Entry<Integer, Integer>> links = new ArrayList<Entry<Integer, Integer>>();
    
    Integer transPosition = 0;
    
    /**
     * Creates new form ScrTranslationWindow
     * @param _core Set dictionary core
     * @param _parent Set parent value for callbacks
     */
    public ScrTranslationWindow(DictCore _core, ScrDictInterface _parent) {
        core = _core;
        parent = _parent;
        
        setupKeyStrokes();
        initComponents();
        txtLocalText.setCaret(new HighlightCaret());
        
        setObjectFonts();
        
        matchListModel = new DefaultListModel();
        lstMatchList.setModel(matchListModel);
        
        setupListeners();
        
        setCurTranslating(false);
    }
    
    private void startTranslation() {
        txtLocalText.setEditable(false);
        
        localText = new ArrayList<String>();
        transText = new ArrayList<String>();
        scrToCoreMap.clear();
        transToLocal.clear();
        
        
        transPosition = 0;
        loadLocalText();
        txtSearchText.setText(localText.get(transPosition));
        selectLocalText(transPosition);
        setCurTranslating(true);
        txtTransText.setText("");
    }
    
    private void nextWord() {
        txtTransWord.setText("");
        transPosition++;
        
        // stop translation one complete
        if (transPosition == localText.size()) {
            setCurTranslating(false);
            viewFinalTextBox();
            return;
        }
        
        selectLocalText(transPosition);
        txtSearchText.setText(localText.get(transPosition));
    }
    
    private void viewFinalTextBox() {
        PFrame window = ScrTranslationFreewrite.run(core, txtTransText.getText());
        childFrames.add(window);
    }
    
    /**
     * Sets the properties of the translation window
     * @param _curTranslating whether translation is currently happening
     */
    private void setCurTranslating(boolean _curTranslating) {
        txtSearchText.setEnabled(_curTranslating);
        txtTransWord.setEnabled(_curTranslating);
        lstMatchList.setEnabled(_curTranslating);
        btnSkipWord.setEnabled(_curTranslating);
        btnTranslate.setEnabled(!_curTranslating);
        btnCreateNew.setEnabled(_curTranslating);
        btnUseWord.setEnabled(_curTranslating);
        
        curTranslating = _curTranslating;
    }
    
    private void useTransWord() {
        String useWord = txtTransWord.getText();
        
        // skip addition of space for first word
        if (!txtTransText.getText().equals("")) {
            useWord = " " + useWord;
        }
        
        txtTransText.setText(txtTransText.getText() + useWord);
        transText.add(useWord);
        transToLocal.put(transText.size() - 1, transPosition);
                
        nextWord();
    }
    
    /**
     * kills all child windows
     */
    private void killAllChildren() {
        Iterator<PFrame> it = childFrames.iterator();
        
        while (it.hasNext()) {
            PFrame curFrame = it.next();
            
            if (curFrame != null) {
                curFrame.setVisible(false);
                curFrame.dispose();
            }
        }
    }
    
    @Override
    public void dispose() {
        killAllChildren();
        
        this.setVisible(false);
        super.dispose();
    }
        
    private void populateMatchList() {
        List<ConWord> printMe = core.getWordCollection()
                .getSuggestedTransWords(txtSearchText.getText());
                
        matchListModel.clear();
        
        for (ConWord curWord : printMe) {
            matchListModel.addElement(curWord.getValue());
            scrToCoreMap.put(matchListModel.size() - 1, curWord.getId());
        }
    }
    
    private void setObjectFonts() {
        Font conFont = getLangFont();
        
        lstMatchList.setFont(conFont);
        txtTransText.setFont(conFont);
        txtTransWord.setFont(conFont);
    }
    
    /**
     * Fetches proper language font
     * @return language font, default font otherwise
     */
    private Font getLangFont() {
        Font fontCon = core.getPropertiesManager().getFontCon();

        return fontCon == null ? new JTextField().getFont() : fontCon;
    }
    
    private void setupListeners() {
        txtSearchText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                populateMatchList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                populateMatchList();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                populateMatchList();
            }
        });
        
        // set Use Word button to only be activated when translation word populated
        txtTransWord.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                btnUseWord.setEnabled(!txtTransWord.getText().equals(""));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                btnUseWord.setEnabled(!txtTransWord.getText().equals(""));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                btnUseWord.setEnabled(!txtTransWord.getText().equals(""));
            }
        });
    }
    
    public static ScrTranslationWindow run(DictCore _core, ScrDictInterface parent) {
        ScrTranslationWindow s = new ScrTranslationWindow(_core, parent);
       
        s.setVisible(true);
        
        return s;
    }
    
    private void loadLocalText() {
        if (curPopulating) {
            return;
        }
        boolean localPopulating = curPopulating;
        curPopulating = true;
                
        txtLocalText.setText(txtLocalText.getText().replaceAll("[^\\S\\r\\n]{2,}", " "));
        String userInput = txtLocalText.getText();
        String[] splitInput = userInput.split("\\s");
        
        localText = new ArrayList<String>();
        
        localText.addAll(Arrays.asList(splitInput));
        
        curPopulating = localPopulating;
    }
    
    private void selectLocalText(Integer index) {
        int startPosition = 0;
        
        if (localText.isEmpty() || index > localText.size()) {
            return;
        }
        
        for (int i = 0; i < index; i++) {
            startPosition += localText.get(i).length() + 1; // +1 accounts for spaces
        }
        
        txtLocalText.select(startPosition, startPosition + localText.get(index).length());
    }

    private void selectCurWord() {
        int index = lstMatchList.getSelectedIndex();
        
        if (index == -1 || !scrToCoreMap.containsKey(index)) {
            return;
        }
        
        int id = scrToCoreMap.get(index);
        
        parent.selectWordById(id);
        
        txtTransWord.setText((String)lstMatchList.getSelectedValue());
    }
    
    /**
     * clear away everything from the translation
     */
    private void clearAll() {
        if (InfoBox.yesNoCancel("Clear All?", "Clear all text and work from translation window?", this) 
                == JOptionPane.YES_OPTION) {
        setCurTranslating(false);
        
        txtLocalText.setEditable(true);
        
        txtLocalText.setText("");
        txtSearchText.setText("");
        txtTransText.setText("");
        txtTransWord.setText("");
        }
    }
    
    /**
     * Sets position of highlighted text in local text field based on caret position
     * of translated text
     */
    private void transPositionUpdate() {
        int curCaretLocation = txtTransText.getCaretPosition();
        int curTextSize = 0;
        int localIndex = -1;
        
        if (curPopulating) {
            return;
        }
        
        // this feature should only run if not currently translating
        if(curTranslating) {
            return;
        } 
        
        boolean localPopulating = curPopulating;
        curPopulating = true;
                
        for (int i = 0; i < transText.size(); i++) {
            curTextSize += transText.get(i).length();
            
            if(curTextSize >= curCaretLocation) {
                localIndex = transToLocal.get(i);
                int offset =  i == 0 ? - transText.get(i).length() : - transText.get(i).length() + 1;
                txtTransText.select(curTextSize + offset, curTextSize);
                break;
            }
        }
        
        if (localIndex > -1) {
            selectLocalText(localIndex);
        }
        
        curPopulating = localPopulating;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtTransText = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        pnlCtrl = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstMatchList = new javax.swing.JList();
        lblSearchWord = new javax.swing.JLabel();
        txtSearchText = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnCreateNew = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtTransWord = new javax.swing.JTextField();
        btnUseWord = new javax.swing.JButton();
        btnSkipWord = new javax.swing.JButton();
        btnTranslate = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLocalText = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Translation Window");

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txtTransText.setEditable(false);
        txtTransText.setColumns(20);
        txtTransText.setLineWrap(true);
        txtTransText.setRows(5);
        txtTransText.setToolTipText("Transliterated text.");
        txtTransText.setWrapStyleWord(true);
        txtTransText.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                txtTransTextCaretUpdate(evt);
            }
        });
        jScrollPane3.setViewportView(txtTransText);

        jLabel4.setText("Transliterated Text");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3))
        );

        pnlCtrl.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        lstMatchList.setToolTipText("Possible matches for search word");
        lstMatchList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstMatchListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(lstMatchList);

        lblSearchWord.setText("Searching On:");

        txtSearchText.setToolTipText("Word to search on.");

        jLabel1.setText("Possible Matches");

        btnCreateNew.setText("Create New");
        btnCreateNew.setToolTipText("Create new word based on Search word in lexicon.");
        btnCreateNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateNewActionPerformed(evt);
            }
        });

        jLabel2.setText("Translation");

        txtTransWord.setToolTipText("Translated word");

        btnUseWord.setText("Use");
        btnUseWord.setToolTipText("Use currently translated word");
        btnUseWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUseWordActionPerformed(evt);
            }
        });

        btnSkipWord.setText("Skip");
        btnSkipWord.setToolTipText("Move on to next word to translate");
        btnSkipWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSkipWordActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlCtrlLayout = new javax.swing.GroupLayout(pnlCtrl);
        pnlCtrl.setLayout(pnlCtrlLayout);
        pnlCtrlLayout.setHorizontalGroup(
            pnlCtrlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCtrlLayout.createSequentialGroup()
                .addGroup(pnlCtrlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCtrlLayout.createSequentialGroup()
                        .addComponent(btnUseWord)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSkipWord))
                    .addGroup(pnlCtrlLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlCtrlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTransWord)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(pnlCtrlLayout.createSequentialGroup()
                                .addGroup(pnlCtrlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblSearchWord)
                                    .addComponent(jLabel1)
                                    .addComponent(txtSearchText, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnCreateNew)
                                    .addComponent(jLabel2))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        pnlCtrlLayout.setVerticalGroup(
            pnlCtrlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCtrlLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSearchWord)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSearchText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnCreateNew)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtTransWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCtrlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUseWord)
                    .addComponent(btnSkipWord))
                .addContainerGap())
        );

        btnTranslate.setText("Begin Translation");
        btnTranslate.setToolTipText("Start translation process for local language text.");
        btnTranslate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTranslateActionPerformed(evt);
            }
        });

        btnClear.setText("Reset");
        btnClear.setToolTipText("Completely reset window. Erase all values.");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setText("Text To Translate");

        txtLocalText.setColumns(20);
        txtLocalText.setLineWrap(true);
        txtLocalText.setRows(5);
        txtLocalText.setToolTipText("Text to translate.");
        txtLocalText.setWrapStyleWord(true);
        jScrollPane1.setViewportView(txtLocalText);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnTranslate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnClear))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlCtrl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlCtrl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnTranslate)
                            .addComponent(btnClear))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lstMatchListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstMatchListValueChanged
        selectCurWord();
    }//GEN-LAST:event_lstMatchListValueChanged

    private void btnTranslateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTranslateActionPerformed
        startTranslation();
    }//GEN-LAST:event_btnTranslateActionPerformed

    private void btnUseWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUseWordActionPerformed
        useTransWord();
    }//GEN-LAST:event_btnUseWordActionPerformed

    private void btnSkipWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSkipWordActionPerformed
        nextWord();
    }//GEN-LAST:event_btnSkipWordActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearAll();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnCreateNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateNewActionPerformed
        parent.createNewWordByLocal(txtSearchText.getText());
    }//GEN-LAST:event_btnCreateNewActionPerformed

    private void txtTransTextCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtTransTextCaretUpdate
        transPositionUpdate();
    }//GEN-LAST:event_txtTransTextCaretUpdate

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnCreateNew;
    private javax.swing.JButton btnSkipWord;
    private javax.swing.JButton btnTranslate;
    private javax.swing.JButton btnUseWord;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblSearchWord;
    private javax.swing.JList lstMatchList;
    private javax.swing.JPanel pnlCtrl;
    private javax.swing.JTextArea txtLocalText;
    private javax.swing.JTextField txtSearchText;
    private javax.swing.JTextArea txtTransText;
    private javax.swing.JTextField txtTransWord;
    // End of variables declaration//GEN-END:variables
}
