/*
 * Copyright (c) 2016-2019, draque.thompson
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

import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PButton;
import PolyGlot.CustomControls.PFrame;
import PolyGlot.CustomControls.PLabel;
import PolyGlot.CustomControls.PRadioButton;
import PolyGlot.DictCore;
import PolyGlot.IOHandler;
import PolyGlot.Nodes.ConWord;
import PolyGlot.QuizEngine.Quiz;
import PolyGlot.QuizEngine.QuizQuestion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 *
 * @author draque.thompson
 */
public final class ScrQuizScreen extends PFrame {

    private final Quiz quiz;
    private final PLabel lblQNode;
    private QuizQuestion curQuestion;
    private final Color correctGreen = new Color(15, 175, 15);

    /**
     * Creates new form ScrQuizScreen
     *
     * @param _quiz
     * @param _core
     */
    public ScrQuizScreen(Quiz _quiz, DictCore _core) {
        core = _core;
        lblQNode = new PLabel("", PLabel.CENTER, core);
        initComponents();
        quiz = _quiz;

        super.repaint();
        lblQNode.setResize(true);
        lblQNode.setMinimumSize(new Dimension(1, 1));
        jPanel3.setLayout(new BorderLayout());

        if (!quiz.hasNext()) {
            InfoBox.error("Empty Quiz", "Quiz has no questions. If generated, filter.", core.getRootWindow());
        }

        nextQuestion();

        // due to initilization process, this forces resize of tet in PLabel at appropriate time
        SwingUtilities.invokeLater(() -> {
            jPanel3.add(lblQNode);
            jPanel3.repaint();
        });
    }

    private void nextQuestion() {
        setQuestion(quiz.next());
    }
    
    @Override
    public void saveAllValues() {
        // no values to save
    }

    private void backQuestion() {
        setQuestion(quiz.prev());
    }

    private void finishQuiz() {
        int numRight = quiz.getNumCorrect();
        int quizLen = quiz.getLength();

        if (numRight == quizLen) {
            InfoBox.info("Quiz Complete", "Perfect score! " + numRight
                    + " out of " + quizLen + " correct!", this);
            dispose();
        } else {
            int retake = JOptionPane.showConfirmDialog(this, numRight
                    + " out of " + quizLen + " correct. Retake?", "Quiz Complete", JOptionPane.YES_NO_OPTION);

            if (retake == JOptionPane.YES_OPTION) {
                int trim = JOptionPane.showConfirmDialog(this, "Quiz only on incorrectly answered questions?",
                        "Trim Quiz?", JOptionPane.YES_NO_OPTION);

                if (trim == JOptionPane.YES_OPTION) {
                    quiz.trimQuiz();
                } else {
                    quiz.resetQuiz();
                }

                nextQuestion();
            } else {
                dispose();
            }
        }
    }

    /**
     * Moves display to next question
     */
    private void setQuestion(QuizQuestion question) {
        curQuestion = question;

        lblQNum.setText((quiz.getCurQuestion() + 1) + "/" + quiz.getLength());

        try {
            lblQuestion.setText(question.getQuestionValue());
            switch (question.getType()) {
                case Local:
                case PoS:
                case Proc:
                case Def:
                case Classes: {
                    ConWord sourceWord = (ConWord) question.getSource();
                    lblQNode.setFont(core.getPropertiesManager().getFontCon());
                    lblQNode.setText(sourceWord.getValue());
                    break;
                }
                case ConEquiv: {
                    ConWord sourceWord = (ConWord) question.getSource();
                    lblQNode.setFont(core.getPropertiesManager().getFontLocal());
                    lblQNode.setText(sourceWord.getLocalWord());
                    break;
                }
                default:
                    throw new Exception("Unhandled qustion type: " + question.getType());
            }

            // force firing of resize code
            ((PLabel) lblQNode).adaptLabelFont(lblQNode);

            pnlChoices.removeAll();
            pnlChoices.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 9999;

            question.getChoices().forEach((choiceNode) -> {
                final PRadioButton choice = new PRadioButton(core);
                choice.setValue(choiceNode);
                choice.setType(question.getType());

                // on button selection, record user choice and right/wrong status
                choice.addActionListener((ActionEvent e) -> {
                    QuizQuestion question1 = getCurQuestion();
                    if (choice.getValue().getId().equals(question1.getAnswer().getId())) {
                        question1.setAnswered(QuizQuestion.Answered.Correct);
                        question1.setUserAnswer(question1.getAnswer());
                    } else {
                        question1.setAnswered(QuizQuestion.Answered.Incorrect);
                        question1.setUserAnswer(choice.getValue());
                    }
                    setupScreen();
                });

                choice.setHorizontalAlignment(SwingConstants.LEFT);
                grpAnswerSelection.add(choice);
                pnlChoices.add(choice, gbc);
                gbc.gridy++;

                // if question is answered already, set answer
                if (question.getAnswered() == QuizQuestion.Answered.Correct
                        || question.getAnswered() == QuizQuestion.Answered.Incorrect) {
                    Collections.list(grpAnswerSelection.getElements()).forEach((curComp) -> {
                        PRadioButton radio = (PRadioButton) curComp;
                        if (Objects.equals(radio.getValue().getId(), question.getUserAnswer().getId())) {
                            grpAnswerSelection.setSelected(radio.getModel(), true);
                        }

                        curComp.setEnabled(false);
                    });
                }
            });
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("Population Error", "Problem populating question: "
                    + e.getLocalizedMessage(), core.getRootWindow());
        }

        setupScreen();
    }

    /**
     * Exposes current question to listener methods
     *
     * @return current question
     */
    private QuizQuestion getCurQuestion() {
        return curQuestion;
    }

    /**
     * Run this each time something changes that makes the screen need to be
     * revised
     */
    private void setupScreen() {
        // set up screen visually
        if (quiz.getCurQuestion() > 0) {
            btnBackward.setEnabled(true);
        } else {
            btnBackward.setEnabled(false);
        }

        if (quiz.getCurQuestion() >= quiz.getLength() - 1) {
            btnForward.setText("Finish");
        } else {
            btnForward.setText("Next");
        }

        switch (curQuestion.getAnswered()) {
            case Unanswered:
                Collections.list(grpAnswerSelection.getElements()).forEach((curComp) -> {
                    curComp.setEnabled(true);
                    curComp.setForeground(Color.black);
                });

                lblAnsStat.setText("");
                break;
            case Correct:
                Collections.list(grpAnswerSelection.getElements()).forEach((curComp) -> {
                    int ansId = ((PRadioButton) curComp).getValue().getId();
                    curComp.setEnabled(false);

                    if (ansId == curQuestion.getAnswer().getId()) {
                        curComp.setForeground(correctGreen);
                    }
                });

                lblAnsStat.setText("CORRECT");
                lblAnsStat.setForeground(correctGreen);
                break;
            case Incorrect:
                Collections.list(grpAnswerSelection.getElements()).forEach((curComp) -> {
                    int ansId = ((PRadioButton) curComp).getValue().getId();
                    curComp.setEnabled(false);

                    if (ansId == curQuestion.getAnswer().getId()) {
                        curComp.setForeground(correctGreen);
                    } else if (ansId == curQuestion.getUserAnswer().getId()) {
                        curComp.setForeground(Color.red);
                    }
                });

                lblAnsStat.setText("INCORRECT");
                lblAnsStat.setForeground(Color.red);
                break;
            default:
                InfoBox.error("Unhandled Answer Type", "Answer type "
                        + curQuestion.getAnswered() + " is not handled.", this);
        }

        pnlChoices.repaint();
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpAnswerSelection = new javax.swing.ButtonGroup();
        pnlChoices = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblQuestion = new PLabel("", PLabel.CENTER, core);
        jPanel3 = new javax.swing.JPanel();
        btnForward = new PButton(core);
        btnBackward = new PButton(core);
        lblAnsStat = new javax.swing.JLabel();
        lblQNum = new PLabel("", core);
        jLabel1 = new PLabel("", core);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PolyGlot Quiz");

        pnlChoices.setBackground(new java.awt.Color(255, 255, 255));
        pnlChoices.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        pnlChoices.setAutoscrolls(true);
        pnlChoices.setMinimumSize(new java.awt.Dimension(10, 10));
        pnlChoices.setName(""); // NOI18N

        javax.swing.GroupLayout pnlChoicesLayout = new javax.swing.GroupLayout(pnlChoices);
        pnlChoices.setLayout(pnlChoicesLayout);
        pnlChoicesLayout.setHorizontalGroup(
            pnlChoicesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlChoicesLayout.setVerticalGroup(
            pnlChoicesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 53, Short.MAX_VALUE)
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setMinimumSize(new java.awt.Dimension(10, 10));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 209, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblQuestion, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblQuestion, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnForward.setText("Next");
        btnForward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnForwardActionPerformed(evt);
            }
        });

        btnBackward.setText("Previous");
        btnBackward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackwardActionPerformed(evt);
            }
        });

        lblAnsStat.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblAnsStat.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlChoices, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(btnBackward)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblQNum, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblAnsStat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnForward))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlChoices, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblAnsStat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnBackward)
                    .addComponent(lblQNum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnForward)
                        .addComponent(jLabel1)))
                .addGap(2, 2, 2))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackwardActionPerformed
        backQuestion();
    }//GEN-LAST:event_btnBackwardActionPerformed

    private void btnForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnForwardActionPerformed
        if (quiz.getCurQuestion() >= quiz.getLength() - 1) {
            finishQuiz();
        } else {
            nextQuestion();
        }
    }//GEN-LAST:event_btnForwardActionPerformed

    /**
     * @param quiz
     * @param core
     * @return
     */
    public static ScrQuizScreen run(Quiz quiz, DictCore core) {
        ScrQuizScreen s = new ScrQuizScreen(quiz, core);
        s.setVisible(true);
        return s;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackward;
    private javax.swing.JButton btnForward;
    private javax.swing.ButtonGroup grpAnswerSelection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblAnsStat;
    private javax.swing.JLabel lblQNum;
    private javax.swing.JLabel lblQuestion;
    private javax.swing.JPanel pnlChoices;
    // End of variables declaration//GEN-END:variables

    @Override
    public void updateAllValues(DictCore _core) {
        core = _core;
        setupScreen();
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        // does nothing
    }
    
    @Override
    public Component getWindow() {
        return this.getRootPane();
    }
}
