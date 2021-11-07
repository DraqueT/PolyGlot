/*
 * Copyright (c) 2014-2021, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
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
package org.darisadesigns.polyglotlina.Desktop.CustomControls;

/**
 *
 * @author draque
 */
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.DEFAULT_OPTION;

import javax.swing.UIManager;
import org.darisadesigns.polyglotlina.InfoBox;
import org.darisadesigns.polyglotlina.PGTUtil;

public class DesktopInfoBox extends JFrame implements InfoBox {

    private Window parent;
    private final Icon optionIcon = UIManager.getIcon("FileView.computerIcon");
    
    private PButton getYesButton() {
        var YES = new PButton() {
            @Override
            public boolean equals(Object value) {
                return Integer.class == value.getClass()
                        && value.equals(JOptionPane.YES_OPTION);
            }

            @Override
            public int hashCode() {
                return 7;
            }
        };
        
        YES.setText("Yes");
        YES.addActionListener((ActionEvent e) -> {
            JOptionPane pane = (JOptionPane) ((Component) e.getSource()).getParent().getParent();
            pane.setValue(JOptionPane.YES_OPTION);
        });
        
        return YES;
    }
    
    private PButton getOKButton() {
        var OK = new PButton() {
            @Override
            public boolean equals(Object value) {
                return Integer.class == value.getClass()
                        && value.equals(JOptionPane.OK_OPTION);
            }

            @Override
            public int hashCode() {
                return 7;
            }
        };
        
        OK.setText("OK");
        OK.addActionListener((ActionEvent e) -> {
            JOptionPane pane = (JOptionPane) ((Component) e.getSource()).getParent().getParent();
            // set the value of the option pane
            pane.setValue(JOptionPane.OK_OPTION);
        });
        
        return OK;
    }
    
    private PButton getNoButton() {
        var NO = new PButton() {
            @Override
            public boolean equals(Object value) {
                return Integer.class == value.getClass()
                        && value.equals(JOptionPane.NO_OPTION);
            }

            @Override
            public int hashCode() {
                return 7;
            }
        };
        
        NO.setText("No");
        NO.addActionListener((ActionEvent e) -> {
            JOptionPane pane = (JOptionPane) ((Component) e.getSource()).getParent().getParent();
            pane.setValue(JOptionPane.NO_OPTION);
        });
        
        return NO;
    }
    
    private PButton getCancelButton() {
        var CANCEL = new PButton() {
            @Override
            public boolean equals(Object value) {
                return Integer.class == value.getClass()
                        && value.equals(JOptionPane.CANCEL_OPTION);
            }

            @Override
            public int hashCode() {
                return 7;
            }
        };

        CANCEL.setText("Cancel");
        CANCEL.addActionListener((ActionEvent e) -> {
            JOptionPane pane = (JOptionPane) ((Component) e.getSource()).getParent().getParent();
            pane.setValue(JOptionPane.CANCEL_OPTION);
        });
        
        return CANCEL;
    }
    
    public DesktopInfoBox(Window _parent) {
        super();
        parent = _parent;
    }
   
    
    public void setParentWindow(Window _parent) {
        parent = _parent;
    }
    
    public Window getParentWindow() {
        return parent;
    }
    
    @Override
    public void info(String title, String message) {
        this.doInfo(title, message);
    }

    @Override
    public void error(String title, String message) {
        this.doError(title, message);
    }

    @Override
    public void warning(String title, String message) {
        this.doWarning(title, message);
    }

    @Override
    public Integer yesNoCancel(String title, String message) {
        return this.doYesNoCancel(title, message);
    }

    @Override
    /**
     * Displays confirmation to user for deletion of element
     *
     * @return true if chooser accepts, false otherwise
     */
    public boolean deletionConfirmation() {
        return this.actionConfirmation("Delete Confirmation", "Delete Entry? Cannot be undone.");
    }
    
    @Override
    /**
     * Displays confirmation of user action
     *
     * @param title title of query message
     * @param message shown to user
     * @return true if chooser accepts, false otherwise
     */
    public boolean actionConfirmation(String title, String message) {
        PButton[] buttons = {getYesButton(), getNoButton()};
        
        int option = POptionPane.internalShowOptionDialog(parent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                UIManager.getIcon("OptionPane.questionIcon"),
                buttons,
                null);

        return option == JOptionPane.YES_OPTION;
    }
    
    @Override
    /**
     * Wraps JOptionPane dialog mostly for neatness/standardization
     * @param title title of query window
     * @param message message on window given to user
     * @return string value if input, null if cancel hit
     */
    public String stringInputDialog(String title, String message) {
        return JOptionPane.showInputDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    /**
     * Collects double value form user.Will re-call self if non-double value given.
     * @param title title of query window
     * @param message message on window given to user
     * @param warningMessage Warning message to show if user inputs wrong value (blank for default)
     * @return double value if input, null if cancel hit
     */
    public Double doubleInputDialog(String title, String message, String warningMessage) {
        Double ret = null;
        
        String inputString = JOptionPane.showInputDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
        
        if (inputString != null) {
            try {
                ret = Double.parseDouble(inputString);
            } catch (HeadlessException | NumberFormatException e) {
                warningMessage = warningMessage.isEmpty() ? "Please input numeric value." : warningMessage;
                warning("Incorrect Input", warningMessage);
                ret = doubleInputDialog(title, message, warningMessage);
            }
        }
        
        return ret;
    }

    private Integer doYesNoCancel(String title, String message) {
        int ret;
        PButton[] option = {getYesButton(), getNoButton(), getCancelButton()};

        ret = POptionPane.internalShowOptionDialog(parent, 
                message, 
                title, 
                JOptionPane.YES_NO_CANCEL_OPTION, 
                JOptionPane.INFORMATION_MESSAGE, 
                optionIcon, 
                option, 
                null);

        return ret;
    }

    private void doError(String title, String message) {
        if (!PGTUtil.isForceSuppressDialogs()) {
            Object[] option = {getOKButton()};        
            POptionPane.internalShowOptionDialog(parent, message, title, DEFAULT_OPTION,
                             JOptionPane.ERROR_MESSAGE, null, option, null);
        }
    }

    private void doWarning(String title, String message) {
        if (!PGTUtil.isForceSuppressDialogs()) {
            Object[] option = {getOKButton()};
            POptionPane.internalShowOptionDialog(parent, message, title, DEFAULT_OPTION,
                             JOptionPane.WARNING_MESSAGE, null, option, null);
        }
    }

    private void doInfo(String title, String message) {
        Object[] option = {getOKButton()};        
        POptionPane.internalShowOptionDialog(parent, message, title, DEFAULT_OPTION,
                         JOptionPane.INFORMATION_MESSAGE, null, option, null);
    }
}
