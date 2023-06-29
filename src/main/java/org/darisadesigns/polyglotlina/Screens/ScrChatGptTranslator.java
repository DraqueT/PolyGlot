/*
 * Copyright (c) 2023, Draque Thompson, draquemail@gmail.com
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

import ChatGPTInterface.GPTException;
import ChatGPTInterface.PChatGptInterface;
import java.awt.Component;
import java.awt.Cursor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.darisadesigns.polyglotlina.CustomControls.GrammarSectionNode;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBoxGrammarSelection;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PComboBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PGDocument;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PGrammarPane;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextPane;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.FormattedTextHelper;
import org.darisadesigns.polyglotlina.WebInterface;

/**
 *
 * @author draquethompson
 */
public class ScrChatGptTranslator extends PFrame {
    private final PChatGptInterface gpt;
    private final List<PCheckBoxGrammarSelection> grammarCheckboxes;

    /** Creates new form ChatGptTranslator
     * @param _core
     * @throws java.net.MalformedURLException */
    public ScrChatGptTranslator(DictCore _core) throws MalformedURLException {
        super(_core);
        initComponents();
 
        gpt = new PChatGptInterface(core, PolyGlot.getPolyGlot().getOptionsManager().getGptApiKey());
        grammarCheckboxes = new ArrayList<>();
        txtTranslated.setDocument(new PGDocument(((DesktopPropertiesManager)core.getPropertiesManager()).getFontCon()));
        
        populateModelSelection();
        populateGrammarSelection();
    }

    private void populateModelSelection() {
        try {
            var modelsData = gpt.getGptModels();

            if (!modelsData.has("data")) {
                throw new GPTException("Unexpected reply from server.");
            }
            
            var models = modelsData.get("data");

            for (int i = 0; i < models.size(); i++) {
                var modelId = models.get(i).get("id").textValue();
                
                if (gpt.isModelSupported(modelId)) {
                    cmbModelSelection.addItem(modelId);
                }
            }
        } catch (GPTException e) {
            DesktopInfoBox.error("GPT Initilization Problem", "Unable to fetch GPT models: " + e.getLocalizedMessage(), this);
            // TODO: Make this actually dispose itself
            this.dispose();
        } catch (UnknownHostException e) {
            DesktopInfoBox.error("Connection Error", "Unable connect to GPT servers. Please check internet connection.", this);
            // TODO: Make this actually dispose itself
            this.dispose();
        }
        
        cmbModelSelection.setSelectedItem(PChatGptInterface.DEFAULT_GPT_MODEL);
    }
    
    private void populateGrammarSelection() {
        pnlGrammarSelection.setLayout(new BoxLayout(pnlGrammarSelection, BoxLayout.Y_AXIS));
        
        for (var chapter : core.getGrammarManager().getChapters()) {
            var label = new PLabel(chapter.getName());
            pnlGrammarSelection.add(label);
            
            for (int i = 0; i < chapter.getChildCount(); i++) {
                var checkBox = new PCheckBoxGrammarSelection(chapter.getChild(i));
                checkBox.addActionListener((e)-> {
                    calculateGrammarTokensTotal();
                });
                pnlGrammarSelection.add(checkBox);
                grammarCheckboxes.add(checkBox);
            }
        }
        
        calculateGrammarTokensTotal();
    }
    
    private void calculateGrammarTokensTotal() {
        int total = 0;
        
        for (var checkBox : grammarCheckboxes) {
            if (checkBox.isSelected()) {
                total += checkBox.getGptTokens();
            }
        }
        
        txtEstimatedNodes.setText(Integer.toString(total));
    }
    
    private void setSelectAllGrammar(boolean select) {
        for (var checkBox : grammarCheckboxes) {
            checkBox.setSelected(select);
        }
    }
    
    private void translateText() {
        try {
            var grammarSections = new ArrayList<GrammarSectionNode>();
            
            for (var checkBox : grammarCheckboxes) {
                if (checkBox.isSelected()) {
                    grammarSections.add(checkBox.getGrammarSection());
                }
            }
            
            String result = gpt.getTranslate(
                    WebInterface.getTextFromHtml(txtFrom.getText()), 
                    grammarSections, 
                    cmbModelSelection.getSelectedItem().toString()
            );

            txtTranslated.setText("");
            addFormattedText(result);
        } catch (IOException | GPTException e) {
            new DesktopInfoBox(this).error("Translation error", "Error: " + e.getLocalizedMessage());
        }
    }
    
    private void addFormattedText(String text) {
        var doc = txtTranslated.getDocument();
        
        // Selects all text either contained in square brackets or not as groups
        var patternString = "(\\[[^\\[|\\]]*\\])|([^\\[|\\]]*)";
        var pattern = Pattern.compile(patternString);
        var matcher = pattern.matcher(text);
        
        try {
            while (matcher.find()) {
                var segment = matcher.group().trim();

                boolean isConFont = segment.startsWith("[");
                
                var propMan = core.getPropertiesManager();
                
                var fontFamily = isConFont ?
                        propMan.getFontConFamily() :
                        propMan.getFontLocalFamily();
                
                var ptSize = isConFont ?
                        propMan.getConFontSize() :
                        propMan.getLocalFontSize();
                        
                segment = segment.replaceAll("\\[|\\]", "");

                MutableAttributeSet aset = new SimpleAttributeSet();
                StyleConstants.setFontFamily(aset, fontFamily);
                StyleConstants.setFontSize(aset, FormattedTextHelper.fontSizePtToRem(ptSize));
                doc.insertString(doc.getLength(), segment, aset);
            }
        } catch (BadLocationException e) {
            new DesktopInfoBox(this).error("Text Parsing Error", "Error parsing translated text.");
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

        pnlWindowSplit = new javax.swing.JSplitPane();
        pnlOptions = new javax.swing.JPanel();
        jLabel1 = new PLabel();
        txtEstimatedNodes = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        pnlGrammarSelection = new javax.swing.JPanel();
        jLabel2 = new PLabel();
        btnSelectAllGrammar = new PButton();
        btnDeselectAllGrammar = new PButton();
        pnlTranslate = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        btnTranslate = new PButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtFrom = new PTextPane(core, true, "FROM");
        cmbModelSelection = new PComboBox(false, core);
        jLabel3 = new PLabel("");
        jButton1 = new PButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtTranslated = new PGrammarPane(core);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("GPT Translation Tool");

        pnlWindowSplit.setDividerLocation(450);

        jLabel1.setText("Estimated Nodes Count");

        txtEstimatedNodes.setEditable(false);

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        javax.swing.GroupLayout pnlGrammarSelectionLayout = new javax.swing.GroupLayout(pnlGrammarSelection);
        pnlGrammarSelection.setLayout(pnlGrammarSelectionLayout);
        pnlGrammarSelectionLayout.setHorizontalGroup(
            pnlGrammarSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 519, Short.MAX_VALUE)
        );
        pnlGrammarSelectionLayout.setVerticalGroup(
            pnlGrammarSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 561, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(pnlGrammarSelection);

        jLabel2.setText("Grammar Sections");

        btnSelectAllGrammar.setText("Select All");
        btnSelectAllGrammar.setToolTipText("Select all grammar sections");
        btnSelectAllGrammar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectAllGrammarActionPerformed(evt);
            }
        });

        btnDeselectAllGrammar.setText("Deselect All");
        btnDeselectAllGrammar.setToolTipText("Deselect all grammar sections");
        btnDeselectAllGrammar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeselectAllGrammarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlOptionsLayout = new javax.swing.GroupLayout(pnlOptions);
        pnlOptions.setLayout(pnlOptionsLayout);
        pnlOptionsLayout.setHorizontalGroup(
            pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionsLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtEstimatedNodes)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
            .addGroup(pnlOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSelectAllGrammar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeselectAllGrammar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlOptionsLayout.setVerticalGroup(
            pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(btnSelectAllGrammar)
                    .addComponent(btnDeselectAllGrammar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtEstimatedNodes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pnlWindowSplit.setRightComponent(pnlOptions);

        pnlTranslate.setDividerLocation(250);
        pnlTranslate.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        btnTranslate.setText("Translate");
        btnTranslate.setToolTipText("Click to translate text");
        btnTranslate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTranslateActionPerformed(evt);
            }
        });

        txtFrom.setToolTipText("Add text to translate here.");
        jScrollPane3.setViewportView(txtFrom);

        cmbModelSelection.setToolTipText("GPT Model Selection");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Text to Translate");

        jButton1.setText("Help");
        jButton1.setToolTipText("Open the help document to the GPT section");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbModelSelection, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnTranslate, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTranslate)
                    .addComponent(cmbModelSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pnlTranslate.setTopComponent(jPanel1);

        txtTranslated.setEditable(false);
        jScrollPane4.setViewportView(txtTranslated);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
        );

        pnlTranslate.setRightComponent(jPanel2);

        pnlWindowSplit.setLeftComponent(pnlTranslate);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlWindowSplit)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlWindowSplit, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTranslateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTranslateActionPerformed
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            btnTranslate.setEnabled(false);
            translateText();
        } finally {
            setCursor(Cursor.getDefaultCursor());
            btnTranslate.setEnabled(true);
        }
    }//GEN-LAST:event_btnTranslateActionPerformed

    private void btnSelectAllGrammarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllGrammarActionPerformed
        setSelectAllGrammar(true);
    }//GEN-LAST:event_btnSelectAllGrammarActionPerformed

    private void btnDeselectAllGrammarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeselectAllGrammarActionPerformed
        setSelectAllGrammar(false);
    }//GEN-LAST:event_btnDeselectAllGrammarActionPerformed

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void saveAllValues() {
        //do nothing
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // do nothing
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        // do nothing
    }

    @Override
    public Component getWindow() {
        // don't implement this unless it's going in the main window
        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDeselectAllGrammar;
    private javax.swing.JButton btnSelectAllGrammar;
    private javax.swing.JButton btnTranslate;
    private javax.swing.JComboBox<String> cmbModelSelection;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel pnlGrammarSelection;
    private javax.swing.JPanel pnlOptions;
    private javax.swing.JSplitPane pnlTranslate;
    private javax.swing.JSplitPane pnlWindowSplit;
    private javax.swing.JTextField txtEstimatedNodes;
    private javax.swing.JTextPane txtFrom;
    private javax.swing.JTextPane txtTranslated;
    // End of variables declaration//GEN-END:variables

}
