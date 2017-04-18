/*
 * Copyright (c) 2014-2017, Draque Thompson, draquemail@gmail.com
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
package PolyGlot.CustomControls;

/**
 *
 * @author draque
 */
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class InfoBox extends JFrame {

    private final Icon optionIcon = UIManager.getIcon("FileView.computerIcon");
    private static final PButton yes;
    private static final PButton no;
    private static final PButton cancel;

    static {
        yes = new PButton() {
            @Override
            public boolean equals(Object value) {
                return Integer.class == value.getClass()
                        && value.equals(JOptionPane.YES_OPTION);
            }

            @Override
            public int hashCode() {
                int hash = 7;
                return hash;
            }
        };

        no = new PButton() {
            @Override
            public boolean equals(Object value) {
                return Integer.class == value.getClass()
                        && value.equals(JOptionPane.NO_OPTION);
            }

            @Override
            public int hashCode() {
                int hash = 7;
                return hash;
            }
        };
        cancel = new PButton() {
            @Override
            public boolean equals(Object value) {
                return Integer.class == value.getClass()
                        && value.equals(JOptionPane.CANCEL_OPTION);
            }

            @Override
            public int hashCode() {
                int hash = 7;
                return hash;
            }
        };

        yes.setText("Yes");
        yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane pane = (JOptionPane) ((JPanel) ((JComponent) e.getSource()).getParent()).getParent();
                // set the value of the option pane
                pane.setValue(JOptionPane.YES_OPTION);
            }
        });

        no.setText("No");
        no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane pane = (JOptionPane) ((JPanel) ((JComponent) e.getSource()).getParent()).getParent();
                // set the value of the option pane
                pane.setValue(JOptionPane.NO_OPTION);
            }
        });

        cancel.setText("Cancal");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane pane = (JOptionPane) ((JPanel) ((JComponent) e.getSource()).getParent()).getParent();
                // set the value of the option pane
                pane.setValue(JOptionPane.CANCEL_OPTION);
            }
        });
    }

    public static void info(String title, String message, Window parent) {
        new InfoBox().doInfo(title, message, parent);
    }

    public static void error(String title, String message, Window parent) {
        new InfoBox().doError(title, message, parent);
    }

    public static void warning(String title, String message, Window parent) {
        new InfoBox().doWarning(title, message, parent);
    }

    public static Integer yesNoCancel(String title, String message, Window parent) {
        return new InfoBox().doYesNoCancel(title, message, parent);
    }

    /**
     * Displays confirmation to user for deletion of element
     *
     * @param parent parent caller
     * @return true if chooser accepts, false otherwise
     */
    public static boolean deletionConfirmation(Window parent) {
        int option = JOptionPane.showOptionDialog(parent,
                "Delete Entry? Cannot be undone.",
                "Delete Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                UIManager.getIcon("OptionPane.questionIcon"),
                null,
                null);

        return option == JOptionPane.YES_OPTION;
    }

    private Integer doYesNoCancel(String title, String message, Window parent) {
        int ret;
        PButton[] option = {yes, no, cancel};

        if (parent == null) {
            ret = JOptionPane.showOptionDialog(parent, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, optionIcon, option, null);
        } else {
            ret = POptionPane.showOptionDialog(parent, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, optionIcon, option, null);
        }
        return ret;
    }

    private void doError(String title, String message, Window parent) {
        if (parent == null) {
            JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);

        } else {
            POptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doWarning(String title, String message, Window parent) {
        if (parent == null) {
            JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
        } else {
            POptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
        }
    }

    private void doInfo(String title, String message, Window parent) {
        if (parent == null) {
            JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
        } else {
            POptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
