/*
 * Copyright (c) 2021-2022, Draque Thompson, draquemail@gmail.com
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PComboBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;

/**
 *
 * @author draque
 */
public class ScrWordFormConstructor extends PFrame {
    private ConWord word;
    private final List<PComboBox> dimensionalValues;
    
    public ScrWordFormConstructor(DictCore _core, ConWord _word) {
        super(_core);
        dimensionalValues = new ArrayList<>();
        word = _word;
        initComponents();
        setupMenu();
    }
    
    public void setWord(ConWord _word) {
        word = _word;
        setupMenu();
    }
    
    private void setupMenu() {
        lblBaseWord.setText(word.getValue());
        lblBaseWord.setFont(txtWordForm.getFont());
        txtWordForm.setText(word.getValue());
        setupDropdowns();
        setupSize();
        composeWord();
    }
    
    private void setupDropdowns() {
        dimensionalValues.clear();
        pnlDimensions.removeAll();
        pnlDimensions.setLayout(new GridBagLayout());
        
        var gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        
        if (word != null) {
            Font local = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
            int maxHeight = 0;
            for (var dimension : core.getConjugationManager().getDimensionalConjugationListTemplate(word.getWordTypeId())) {
                var dimensionDropdown = new PComboBox<LexDimension>(local, dimension.getValue());
                
                if (maxHeight == 0) {
                    FontMetrics metrics = this.getGraphics().getFontMetrics();
                    maxHeight = metrics.getHeight();
                }
                
                dimensionDropdown.setMaximumSize(new Dimension(999999,  maxHeight));
                
                var model = new DefaultComboBoxModel<LexDimension>();

                for (var value : dimension.getDimensions()) {
                    model.addElement(new LexDimension(value.getValue(), value.getId()));
                }
                
                dimensionDropdown.setModel(model);
                
                if (model.getSize() > 0) {
                    dimensionDropdown.setSelectedIndex(0);
                }
                
                dimensionDropdown.addActionListener((ActionEvent e) -> {
                    composeWord();
                });
                
                pnlDimensions.add(dimensionDropdown, gbc);
                dimensionalValues.add(dimensionDropdown);
            }
        }
    }
    
    private void composeWord() {
        String combinedId = ",";
        
        for (var combo : dimensionalValues) {
            var lexDimension = (LexDimension)combo.getSelectedItem();
            if (lexDimension == null) {
                // bail from anything with incomplete dimensions
                txtWordForm.setText("");
                return;
            }
            combinedId += lexDimension.id + ",";
        }
        
        try {
            var form = core.getConjugationManager().declineWord(word, combinedId);
            txtWordForm.setText(form);
            txtWordForm.setToolTipText("Composed form of word: " + core.getPronunciationMgr().getPronunciation(form));
        } catch (Exception e) {
            new DesktopInfoBox().error("Composition Error", "Unable to compose word form: " + e.getMessage());
            DesktopIOHandler.getInstance().writeErrorLog(e);
        }
    }
    
    public void setupSize() {
        int width = getWidth();
        int height = 53; // accounts for size of menu borders, etc.
        
        height += lblBaseWord.getHeight();
        height += txtWordForm.getHeight();
        
        for (var combo : dimensionalValues) {
            combo.repaint();
            height += combo.getPreferredSize().height;
        }
        
        setSize(width, height);
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        setupSize();
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblBaseWord = new PLabel("");
        txtWordForm = new PTextField(core, false, "");
        scrlDimensions = new javax.swing.JScrollPane();
        pnlDimensions = new javax.swing.JPanel();
        jLabel1 = new PLabel("Base Word");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Word Form Composition Helper");

        lblBaseWord.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblBaseWord.setText("jLabel1");
        lblBaseWord.setToolTipText("Base form of word");

        txtWordForm.setEditable(false);
        txtWordForm.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtWordForm.setToolTipText("Composed form of word");

        javax.swing.GroupLayout pnlDimensionsLayout = new javax.swing.GroupLayout(pnlDimensions);
        pnlDimensions.setLayout(pnlDimensionsLayout);
        pnlDimensionsLayout.setHorizontalGroup(
            pnlDimensionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 384, Short.MAX_VALUE)
        );
        pnlDimensionsLayout.setVerticalGroup(
            pnlDimensionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 346, Short.MAX_VALUE)
        );

        scrlDimensions.setViewportView(pnlDimensions);

        jLabel1.setText("Base Word:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(scrlDimensions)
                            .addComponent(txtWordForm))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblBaseWord, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBaseWord)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtWordForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrlDimensions)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblBaseWord;
    private javax.swing.JPanel pnlDimensions;
    private javax.swing.JScrollPane scrlDimensions;
    private javax.swing.JTextField txtWordForm;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void saveAllValues() {
        // nothing to save
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // nothing to update
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        // nothing to bind
    }

    @Override
    public Component getWindow() {
        throw new UnsupportedOperationException("This is not run as a panel");
    }
    
    public class LexDimension {
        public final String label;
        public final int id;
        
        public LexDimension(String _label, int _id) {
            label = _label;
            id = _id;
        }
        
        @Override
        public String toString() {
            return label.isEmpty() ? " " : label;
        }
    }

}
