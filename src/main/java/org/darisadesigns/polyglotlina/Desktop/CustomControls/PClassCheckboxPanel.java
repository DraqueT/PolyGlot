/*
 * Copyright (c) 2018-2022, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop.CustomControls;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConjugationGenRule;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box.Filler;
import javax.swing.JPanel;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;

/**
 *
 * @author DThompson
 */
public class PClassCheckboxPanel extends JPanel {
    private ConjugationGenRule rule = new ConjugationGenRule();
    private final HashMap<PCheckBox, List<PCheckBox>> applyClassesCheckboxes = new HashMap<>();
    private final TypeNode type;
    private GridBagConstraints gbc;
    private final PClassCheckboxPanel parent = this;
    private PCheckBox allCheckBox = null;
    private final boolean includeAll;
    private final boolean nightMode;
    private final Font checkBoxFont;
    
    /**
     * 
     * @param core
     * @param _type
     * @param _includeAll Controls whether "All" checkbox is included
     */
    public PClassCheckboxPanel(DictCore core, TypeNode _type, boolean _includeAll) {
        type = _type;
        includeAll = _includeAll;
        nightMode = PolyGlot.getPolyGlot().getOptionsManager().isNightMode();
        checkBoxFont = ((DesktopPropertiesManager)core.getPropertiesManager()).getFontLocal();
        this.init(core);
    }
    
    private void init(DictCore core) {
        // this should not be displayed if there are no classes for this type
        if (rule != null && core.getWordClassCollection().getClassesForType(type.getId()).length == 0) {
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

            PLabel label = new PLabel("Match by Class Value");
            label.setSize(30, 20);
            label.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            this.add(label, gbc);
            this.createCheckBoxes(core);
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
    
    private void createCheckBoxes(DictCore core) {
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        for (WordClass wordClass : core.getWordClassCollection().getClassesForType(type.getId())) {
            this.addTitle(wordClass.getValue() + ":");
            
            final var subCheckBoxes = wordClass.getValues().stream().map((classValue)->{

                final Map.Entry<Integer, PCheckBox> checkBox = this.createCheckBox(classValue.getValue(), 
                        "Apply to " + wordClass.getValue() + ":" + classValue.getValue(), 
                        new ItemListener() {
                            final int thisClassId = wordClass.getId();
                            final int classValueId = classValue.getId();

                            @Override
                            public void itemStateChanged(ItemEvent e) {
                                PCheckBox thisBox = (PCheckBox)e.getSource();
                                if (thisBox.isSelected()) {
                                    rule.addClassToFilterList(thisClassId, classValueId);
                                    init(core);
                                } else {
                                    rule.removeClassFromFilterList(thisClassId, classValueId);
                                }
                            }
                        },
                    wordClass.getId(),
                    classValue.getId());
            
                checkBox.getValue().setSelected(rule.doesRuleApplyToClassValue(wordClass.getId(), classValue.getId(), true));
                return checkBox;
            }).collect(Collectors.toList());
                        
            final PCheckBox classCheckBox = createClassCheckbox(wordClass, new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    PCheckBox thisBox = (PCheckBox)e.getSource();
                    if (thisBox.isSelected()) {
                        rule.addClassToFilterList(wordClass.getId(), -1);
                        uncheckDisableClassChecks(thisBox);
                    } else {
                        rule.removeClassFromFilterList(wordClass.getId(), -1);
                    }

                    for (final PCheckBox child : applyClassesCheckboxes.get(thisBox)) {
                        child.setEnabled(!thisBox.isSelected());
                    }
                }
            });

            this.addClassCheckBox(classCheckBox);
            for (final Map.Entry<Integer, PCheckBox> entry : subCheckBoxes) {
                this.addCheckBox(classCheckBox, entry);
            }

            if (rule.isUniversalInclusion(wordClass.getId()) && !classCheckBox.isSelected()) {
                classCheckBox.doClick();
            }
        }
    }

    private void addTitle(String title) {
        gbc.weightx = 1;
        gbc.gridx = 0;

        final Label label = new Label();
        label.setText(title);
        this.add(label, gbc);

        gbc.weightx = 9999;
        gbc.gridx = 1;
        this.add(new Filler(new Dimension(0,0),new Dimension(9999,9999),new Dimension(9999,9999)), gbc);  
        gbc.gridy++;
    }

    private PCheckBox createClassCheckbox(WordClass wordClass, ItemListener listener) {
        final PCheckBox classCheck = new PCheckBox() {
            public void repaint() {
                this.setSize(parent.getWidth(), 20);
                super.repaint();
            }
        };

        classCheck.setFont(checkBoxFont);
        classCheck.setText("Any");
        classCheck.setSelected(false);
        classCheck.addItemListener(listener);
        classCheck.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        classCheck.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        return classCheck;
    }

    /**
     * @return entry of [classId, checkBox]
     */
    private Map.Entry<Integer, PCheckBox> createCheckBox(String title, String toolTip, ItemListener listener, int classId, int valueId) {
        final PCheckBox check = new PCheckBox(nightMode) {
            @Override
            public void repaint() {
                this.setSize(parent.getWidth(), 20);
                super.repaint();
            }
        };

        check.setFont(checkBoxFont);
        check.setText(title);
        
        check.setSelected(rule.doesRuleApplyToClassValue(classId, valueId, true)); // st value before listener
        check.addItemListener(listener);
        check.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        check.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        check.setToolTipText(toolTip);

        return Map.entry(classId, check);
    }
    
    private void addClassCheckBox(PCheckBox classCheckBox) {
        gbc.weightx = 1;
        gbc.gridx = 0;
    
        this.add(classCheckBox, gbc);
        applyClassesCheckboxes.put(classCheckBox, new ArrayList<>());
        
        gbc.weightx = 9999;
        gbc.gridx = 1;
        this.add(new Filler(new Dimension(0,0),new Dimension(9999,9999),new Dimension(9999,9999)), gbc);  
        gbc.gridy++;
    }

    private void addCheckBox(PCheckBox parent, Map.Entry<Integer, PCheckBox> checkEntry) {
        final int classId = checkEntry.getKey();
        final PCheckBox check = checkEntry.getValue();
        gbc.weightx = 1;
        gbc.gridx = 0;
    
        this.add(check, gbc);
        
        // do not add the "all" checkbox (with -1 ID) to the apply all list
        if (classId == -1) {
            allCheckBox = check;
        } else {
            final List<PCheckBox> oldChildren = applyClassesCheckboxes.get(parent);
            if (oldChildren == null) {
                applyClassesCheckboxes.put(parent, new ArrayList<>());
            }

            applyClassesCheckboxes.get(parent).add(check);
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

    private void uncheckDisableClassChecks(PCheckBox parent) {
        applyClassesCheckboxes.get(parent).forEach((checkBox)->{
            checkBox.setSelected(false);
        });

        setEnabledClassChecks(parent, false);
    }
    
    private void setEnabledClassChecks(PCheckBox parent, boolean enable) {
        applyClassesCheckboxes.get(parent).forEach((checkBox)->{
            checkBox.setSelected(enable);
        });
    }
    
    public void setRule(ConjugationGenRule _rule, DictCore core) {
        this.rule = _rule;
        this.init(core);
    }
    
    @Override
    public final void setBorder(javax.swing.border.Border border) {
        super.setBorder(border);
    }
}
