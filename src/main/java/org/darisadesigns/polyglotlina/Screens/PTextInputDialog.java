/*
 * Copyright (c) 2017-2022, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PTextField;
import org.darisadesigns.polyglotlina.DictCore;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JDialog;

/**
 * Simple dialog to collect arbitrary number of text values
 * @author DThompson
 */
public final class PTextInputDialog extends PDialog {
    private final Window parent;
    private boolean parentModal;
    private boolean parentAlwaysOnTop;
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final List<PTextField> orderedFields = new ArrayList<>();
    private List<PTextField> retVal = new ArrayList<>();
    
    /**
     * Creates new form PTextInputDialog
     * @param _parent
     * @param _core
     * @param caption
     * @param dialog
     */
    public PTextInputDialog(Window _parent, DictCore _core, String caption, String dialog) {
        super(_core);
        
        initComponents();
        parent = _parent;
        if (parent != null) {
            this.setLocation(parent.getLocation());
        }
        this.setModal(true);
        this.setAlwaysOnTop(true);
        this.setTitle(caption);
        
        setParentModalOntop(_parent);
        
        txtDialog.setText("<HTML>" + dialog + "</HTML>");
        
        pnlTextFields.setLayout(new BoxLayout(pnlTextFields, BoxLayout.Y_AXIS));
        
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
    }
    
    private void setParentModalOntop(Window _parent) {
        if (_parent instanceof JDialog && ((JDialog)_parent).isModal()) {
            parentModal = true;
            ((JDialog)_parent).setModal(false);
        }
        
        if (_parent.isAlwaysOnTop()) {
            parentAlwaysOnTop = true;
            _parent.setAlwaysOnTop(false);
        }
    }
    
    /**
     * Adds a text field to the dialog
     * @param defaultText default (gray) text for the field
     * @param overrideText false if conlang font to be used, true if otherwise
     * @param toolTipText tooltiptext for field
     * @param text text to populate the field with
     */
    public void addField(String defaultText, boolean overrideText, String toolTipText, String text) {
        PTextField newField = new PTextField(core, overrideText, defaultText);
        newField.setMinimumSize(new Dimension(1, newField.getPreferredSize().height));
        newField.setMaximumSize(new Dimension(9999, newField.getPreferredSize().height));
        newField.setPreferredSize(new Dimension(9999, newField.getPreferredSize().height));
        newField.setToolTipText(toolTipText);
        newField.setText(text);
        pnlTextFields.add(newField, gbc);
        orderedFields.add(newField);
    }
    
    @Override
    public void setVisible(boolean visible) {
        this.ignoreInitialResize = true;
        var size = new Dimension(initialSize());
        this.setSize(size);
        this.setMinimumSize(size);
        this.toFront();
        super.setVisible(visible);
        
        if (parentAlwaysOnTop) {
            parent.setAlwaysOnTop(true);
        }
        
        if (parentModal) {
            ((JDialog)parent).setModal(true);
        }
    }
    
    private Dimension initialSize() {
        return new Dimension(this.getPreferredSize().width, txtDialog.getPreferredSize().height 
                + pnlTextFields.getPreferredSize().height + jButton1.getPreferredSize().height * 2 + 20);
    }
    
    /**
     * @return text fields in the same order they were added to the dialog
     */
    public PTextField[] getOrderedFields() {
        return retVal.toArray(new PTextField[0]);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtDialog = new javax.swing.JLabel();
        pnlTextFields = new javax.swing.JPanel();
        jButton1 = new PButton(nightMode);
        jButton2 = new PButton(nightMode);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(416, 2));

        txtDialog.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtDialog.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout pnlTextFieldsLayout = new javax.swing.GroupLayout(pnlTextFields);
        pnlTextFields.setLayout(pnlTextFieldsLayout);
        pnlTextFieldsLayout.setHorizontalGroup(
            pnlTextFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlTextFieldsLayout.setVerticalGroup(
            pnlTextFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 215, Short.MAX_VALUE)
        );

        jButton1.setText("OK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtDialog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(pnlTextFields, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 249, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtDialog)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTextFields, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        retVal = orderedFields;
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    @Override
    public void updateAllValues(DictCore _core) {
        // unnecessary in this dialog
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel pnlTextFields;
    private javax.swing.JLabel txtDialog;
    // End of variables declaration//GEN-END:variables
}
