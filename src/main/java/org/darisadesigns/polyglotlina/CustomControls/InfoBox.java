/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.CustomControls;

/**
 *
 * @author draque
 */
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.DEFAULT_OPTION;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class InfoBox extends JFrame {

    private final Icon optionIcon = UIManager.getIcon("FileView.computerIcon");
    private static final PButton YES;
    private static final PButton NO;
    private static final PButton OK;
    private static final PButton CANCEL;

    static {
        OK = new PButton() {
            @Override
            public boolean equals(Object value) {
                return Integer.class == value.getClass()
                        && value.equals(JOptionPane.OK_OPTION);
            }

            @Override
            public int hashCode() {
                int hash = 7;
                return hash;
            }
        };
        YES = new PButton() {
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

        NO = new PButton() {
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
        CANCEL = new PButton() {
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

        OK.setText("OK");
        OK.addActionListener((ActionEvent e) -> {
            JOptionPane pane = (JOptionPane) ((JPanel) ((JComponent) e.getSource()).getParent()).getParent();
            // set the value of the option pane
            pane.setValue(JOptionPane.OK_OPTION);
        });
        
        YES.setText("Yes");
        YES.addActionListener((ActionEvent e) -> {
            JOptionPane pane = (JOptionPane) ((JPanel) ((JComponent) e.getSource()).getParent()).getParent();
            pane.setValue(JOptionPane.YES_OPTION);
        });

        NO.setText("No");
        NO.addActionListener((ActionEvent e) -> {
            JOptionPane pane = (JOptionPane) ((JPanel) ((JComponent) e.getSource()).getParent()).getParent();
            pane.setValue(JOptionPane.NO_OPTION);
        });

        CANCEL.setText("Cancel");
        CANCEL.addActionListener((ActionEvent e) -> {
            JOptionPane pane = (JOptionPane) ((JPanel) ((JComponent) e.getSource()).getParent()).getParent();
            pane.setValue(JOptionPane.CANCEL_OPTION);
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
        return actionConfirmation("Delete Confirmation", "Delete Entry? Cannot be undone.", parent);
    }
    
    /**
     * Displays confirmation of user action
     *
     * @param title title of query message
     * @param message shown to user
     * @param parent parent caller
     * @return true if chooser accepts, false otherwise
     */
    public static boolean actionConfirmation(String title, String message, Window parent) {
        PButton[] buttons = {YES, NO};
        int option = POptionPane.showOptionDialog(parent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                UIManager.getIcon("OptionPane.questionIcon"),
                buttons,
                null);

        return option == JOptionPane.YES_OPTION;
    }
    
    /**
     * Wraps JOptionPane dialog mostly for neatness/standardization
     * @param title title of query window
     * @param message message on window given to user
     * @param parent owner of dialog
     * @return string value if input, null if cancel hit
     */
    public static String stringInputDialog(String title, String message, Window parent) {
        return JOptionPane.showInputDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Collects double value form user.Will re-call self if non-double value given.
     * @param title title of query window
     * @param message message on window given to user
     * @param warningMessage Warning message to show if user inputs wrong value (blank for default)
     * @param parent owner of dialog
     * @return double value if input, null if cancel hit
     */
    public static Double doubleInputDialog(String title, String message, String warningMessage, Window parent) {
        Double ret = null;
        
        String inputString = JOptionPane.showInputDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
        
        if (inputString != null) {
            try {
                ret = Double.parseDouble(inputString);
            } catch (HeadlessException | NumberFormatException e) {
                // TODO: this message should be passed in rather than hardcoded here. It is particular to fonts.
                warningMessage = warningMessage.isEmpty() ? "Please input numeric value." : warningMessage;
                InfoBox.warning("Incorrect Input", warningMessage, parent);
                ret = InfoBox.doubleInputDialog(title, message, warningMessage, parent);
            }
        }
        
        return ret;
    }

    private Integer doYesNoCancel(String title, String message, Window parent) {
        int ret;
        PButton[] option = {YES, NO, CANCEL};

        ret = POptionPane.showOptionDialog(parent, 
                message, 
                title, 
                JOptionPane.YES_NO_CANCEL_OPTION, 
                JOptionPane.INFORMATION_MESSAGE, 
                optionIcon, 
                option, 
                null);

        return ret;
    }

    private void doError(String title, String message, Window parent) {
        Object[] option = {OK};        
        POptionPane.showOptionDialog(parent, message, title, DEFAULT_OPTION,
                         JOptionPane.ERROR_MESSAGE, null, option, null);
    }

    private void doWarning(String title, String message, Window parent) {
        Object[] option = {OK};        
        POptionPane.showOptionDialog(parent, message, title, DEFAULT_OPTION,
                         JOptionPane.WARNING_MESSAGE, null, option, null);
    }

    private void doInfo(String title, String message, Window parent) {
        Object[] option = {OK};        
        POptionPane.showOptionDialog(parent, message, title, DEFAULT_OPTION,
                         JOptionPane.INFORMATION_MESSAGE, null, option, null);
    }
}
