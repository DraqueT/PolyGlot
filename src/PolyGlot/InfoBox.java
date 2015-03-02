/*
 * Copyright (c) 2014, Draque Thompson, draquemail@gmail.com
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

package PolyGlot;

/**
 *
 * @author draque
 */
import java.awt.Window;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class InfoBox extends JFrame {

    //Using a standard Java icon
    private final Icon optionIcon = UIManager.getIcon("FileView.computerIcon");

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
        return JOptionPane.showOptionDialog(parent, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, optionIcon, null, null);
    }
    
    private void doError(String title, String message, Window parent) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    private void doWarning(String title, String message, Window parent) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }
    
    private void doInfo(String title, String message, Window parent) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public InfoBox() {
    }
}
