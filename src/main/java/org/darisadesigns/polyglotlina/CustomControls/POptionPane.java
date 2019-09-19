/*
 * Copyright (c) 2016-2019, Draque Thompson
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.CLOSED_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.VALUE_PROPERTY;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.getRootFrame;
import javax.swing.JRootPane;
import javax.swing.UIManager;

/**
 * Much code pulled up from JOptionPane so that I could make that damned thing
 * set to always on top if I wanted to. No other easy way to do it that I could
 * find.
 *
 * @author draque.thompson
 */
public class POptionPane extends JOptionPane {
    
    public POptionPane(Object message, int messageType, int optionType,
            Icon icon, Object[] options, Object initialValue) {
        super(message, messageType, optionType, icon, options, initialValue);
        
    }

    public static int showOptionDialog(final Component parentComponent,
            Object message, String title, int optionType, int messageType,
            Icon icon, Object[] options, Object initialValue)
            throws HeadlessException {
        Window parentWindow = POptionPane.getWindowForComponent(parentComponent);
        boolean parentIsModal = parentWindow instanceof Dialog 
                && ((Dialog)parentWindow).isModal();
        int ret = CLOSED_OPTION;
        POptionPane pane = new POptionPane(message, messageType,
                optionType, icon,
                options, initialValue);

        pane.setInitialValue(initialValue);
        pane.setComponentOrientation(((parentComponent == null)
                ? getRootFrame() : parentComponent).getComponentOrientation());

        int style = styleFromMessageType(messageType);
        JDialog dialog = pane.createDialog(parentWindow, parentComponent, title, style);
        dialog.setAlwaysOnTop(true);
        dialog.setModal(true);

        setAllWhite(pane);
        
        // prevent locking of application
        if(parentIsModal) {
            ((Dialog)parentWindow).setModal(false);
        }

        pane.selectInitialValue();

        dialog.toFront();
        dialog.setVisible(true);
        dialog.dispose();

        Object selectedValue = pane.getValue();

        if (selectedValue != null) {
            if (options != null) {
                for (int counter = 0, maxCounter = options.length;
                        counter < maxCounter; counter++) {
                    if (options[counter].equals(selectedValue)) {
                        ret = counter;
                    }
                }
            } else {
                if (selectedValue instanceof Integer) {
                    ret = ((Integer) selectedValue);
                }
            }
        }
        
        // prevent locking of application
        if(parentIsModal) {
            ((Dialog)parentWindow).setModal(true);
        }

        return ret;
    }

    private static void setAllWhite(Container c) {
        Component[] comp = c.getComponents();

        c.setBackground(Color.white);
        for (Component curComp : comp) {
            curComp.setBackground(Color.white);
            setAllWhite((Container) curComp);
        }
    }

    private static int styleFromMessageType(int messageType) {
        switch (messageType) {
            case ERROR_MESSAGE:
                return JRootPane.ERROR_DIALOG;
            case QUESTION_MESSAGE:
                return JRootPane.QUESTION_DIALOG;
            case WARNING_MESSAGE:
                return JRootPane.WARNING_DIALOG;
            case INFORMATION_MESSAGE:
                return JRootPane.INFORMATION_DIALOG;
            case PLAIN_MESSAGE:
            default:
                return JRootPane.PLAIN_DIALOG;
        }
    }

    private JDialog createDialog(final Window parentWindow, 
            final Component parentComponent, String title, int style)
            throws HeadlessException {

        final JDialog dialog;

        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, title, true);
        } else {
            dialog = new JDialog((Dialog) parentWindow, title, true);
        }

        dialog.setAlwaysOnTop(true);
        initDialog(dialog, style, parentComponent);

        return dialog;
    }

    static Window getWindowForComponent(Component parentComponent)
            throws HeadlessException {
        if (parentComponent == null) {
            return getRootFrame();
        }
        if (parentComponent instanceof Frame || parentComponent instanceof Dialog) {
            return (Window) parentComponent;
        }
        return POptionPane.getWindowForComponent(parentComponent.getParent());
    }

    private void initDialog(final JDialog dialog, int style, Component parentComponent) {
        dialog.setComponentOrientation(this.getComponentOrientation());
        Container contentPane = dialog.getContentPane();

        contentPane.setLayout(new BorderLayout());
        contentPane.add(this, BorderLayout.CENTER);
        dialog.setResizable(false);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations
                    = UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                dialog.setUndecorated(true);
                getRootPane().setWindowDecorationStyle(style);
            }
        }
        dialog.pack();
        dialog.setLocationRelativeTo(parentComponent);

        final PropertyChangeListener listener = (PropertyChangeEvent event) -> {
            // Let the defaultCloseOperation handle the closing
            // if the user closed the window without selecting a button
            // (newValue = null in that case).  Otherwise, close the dialog.
            if (dialog.isVisible() && event.getSource() == POptionPane.this
                    && (event.getPropertyName().equals(VALUE_PROPERTY))
                    && event.getNewValue() != null
                    && event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                dialog.setVisible(false);
            }
        };

        WindowAdapter adapter = new WindowAdapter() {
            private boolean gotFocus = false;

            @Override
            public void windowClosing(WindowEvent we) {
                setValue(null);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                removePropertyChangeListener(listener);
                dialog.getContentPane().removeAll();
            }

            @Override
            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    selectInitialValue();
                    gotFocus = true;
                }
            }
        };
        dialog.addWindowListener(adapter);
        dialog.addWindowFocusListener(adapter);
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
                // reset value to ensure closing works properly
                setValue(JOptionPane.UNINITIALIZED_VALUE);
            }
        });

        addPropertyChangeListener(listener);
    }
}
