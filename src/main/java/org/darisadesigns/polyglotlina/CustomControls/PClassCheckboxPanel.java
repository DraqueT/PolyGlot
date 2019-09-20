/*
 * Copyright (c) 2018-2019, Draque Thompson
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
package org.darisadesigns.polyglotlina.CustomControls;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.DeclensionGenRule;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box.Filler;
import javax.swing.JPanel;

/**
 *
 * @author DThompson
 */
public class PClassCheckboxPanel extends JPanel {
    private final DictCore core;
    private DeclensionGenRule rule = new DeclensionGenRule();
    private final List<PCheckBox> applyClassesCheckboxes = new ArrayList<>();
    private final TypeNode type;
    private GridBagConstraints gbc;
    private final PClassCheckboxPanel parent = this;
    private PCheckBox allCheckBox = null;
    
    public PClassCheckboxPanel(DictCore _core, TypeNode _type) {
        core = _core;
        type = _type;
        this.init();
    }
    
    private void init() {
        // this should not be displayed if there are no classes for this type
        if (rule != null &&core.getWordClassCollection().getClassesForType(type.getId()).isEmpty()) {
            this.setVisible(false);
        } else if (rule != null) {
            applyClassesCheckboxes.clear();
            this.removeAll();
            this.setToolTipText("Rule will only be applied to words of EVERY class value selected."
                    + " Select \"All\" to apply to all words.");
            super.setLayout(new GridBagLayout());
            this.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            gbc = new GridBagConstraints();
            gbc.weighty = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.NORTH;

            PLabel label = new PLabel("Match by Class Value", core);
            label.setSize(30, 20);
            label.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            this.add(label, gbc);
            this.createCheckBoxes();
            this.createBottomSpacer();
            // below is sloppy, but checkboxes all paint on top of one another without this for some reason...
            this.setVisible(false);
            this.setVisible(true);
        } else {
            this.removeAll();
            this.setVisible(false);
            this.setVisible(true);
        }
    }
    
    private void createBottomSpacer() {
        gbc.weighty = 999;
        this.add(new Filler(new Dimension(0,0),new Dimension(999,999),new Dimension(9999,9999)), gbc);
        this.setPreferredSize(new Dimension(150, gbc.gridy * 20));
    }
    
    private void createCheckBoxes() {
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        this.addCheckBox("All", 
                "Apply to all classes", 
                (ItemEvent e) -> {
                        PCheckBox thisBox = (PCheckBox)e.getSource();
                        if (thisBox.isSelected()) {
                            rule.addClassToFilterList(-1, -1);
                            uncheckDisableClassChecks();
                        } else {
                            rule.removeClassFromFilterList(-1, -1);
                            setEnabledClassChecks(true);
                        }
                    },
                -1, -1);
                        
        core.getWordClassCollection().getClassesForType(type.getId()).forEach((wordClass)->{
            wordClass.getValues().forEach((classValue)->{
                this.addCheckBox(classValue.getValue(), 
                            "Apply to " + wordClass.getValue() + ":" + classValue.getValue(), 
                            new ItemListener() {
                                final int thisClassId = wordClass.getId();
                                final int classValueId = classValue.getId();

                                @Override
                                public void itemStateChanged(ItemEvent e) {
                                    PCheckBox thisBox = (PCheckBox)e.getSource();
                                    if (thisBox.isSelected()) {
                                        rule.addClassToFilterList(thisClassId, classValueId);
                                        init();
                                    } else {
                                        rule.removeClassFromFilterList(thisClassId, classValueId);
                                    }
                                }
                            },
                        wordClass.getId(),
                        classValue.getId());
            });
        });
        
        // if this is the default rule without any rule set, disable checkboxes
        setEnabledClassChecks(rule.getTypeId() != -1);
        allCheckBox.setEnabled(rule.getTypeId() != -1);
    }
    
    private void addCheckBox(String title, String toolTip, ItemListener listener, int classId, int valueId) {
        gbc.weightx = 1;
        gbc.gridx = 0;
        
        final PCheckBox check = new PCheckBox(core) {
            @Override
            public void repaint() {
                this.setSize(parent.getWidth(), 20);
                super.repaint();
            }
        };
        check.setText(title);
        check.setSelected(rule.doesRuleApplyToClassValue(classId, valueId, true)); // st value before listener
        check.addItemListener(listener);
        check.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        check.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        check.setToolTipText(toolTip);
        this.add(check, gbc);
        
        // do not add the "all" checkbox (with -1 ID) to the apply all list
        if (classId != -1) {
            applyClassesCheckboxes.add(check);
        } else {
            allCheckBox = check;
        }
        
        gbc.weightx = 9999;
        gbc.gridx = 1;
        this.add(new Filler(new Dimension(0,0),new Dimension(9999,9999),new Dimension(9999,9999)), gbc);  
        gbc.gridy++;
    }
    
    @Override
    public void setLayout(LayoutManager l) {
        // ignore. Set up internally only.
    }

    private void uncheckDisableClassChecks() {
        applyClassesCheckboxes.forEach((checkBox)->{
            checkBox.setSelected(false);
        });
        setEnabledClassChecks(false);
    }
    
    private void setEnabledClassChecks(boolean enable) {
        applyClassesCheckboxes.forEach((checkBox)->{
            checkBox.setEnabled(enable);
        });
    }
    
    public void setRule(DeclensionGenRule _rule) {
        this.rule = _rule;
        this.init();
    }
    
    @Override
    public final void setBorder(javax.swing.border.Border border) {
        super.setBorder(border);
    }
}
