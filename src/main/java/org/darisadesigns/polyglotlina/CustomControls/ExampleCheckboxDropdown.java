/*
 * Copyright (c) 2019, Draque Thompson, draquemail@gmail.com
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

/*
EXAMPLE CODE: This serves as a functional example as to how to use the checkbox
dropdown. Simply uncomment the main function and run this file to see it in 
action.

Once this is used elsewhere in code, delete this example, as it will be evident
how to use it there.
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.IOHandler;

public final class ExampleCheckboxDropdown extends JPanel {

    private ExampleCheckboxDropdown() {
        super(new BorderLayout());

        PCheckableItem[] m = {
            new PCheckableItem("aaa", false, "0"),
            new PCheckableItem("bbbbb", true, "1"),
            new PCheckableItem("111", false, "2"),
            new PCheckableItem("33333", true, "3"),
            new PCheckableItem("2222", true, "4"),
            new PCheckableItem("ccccccc", false, "5")
        };

        JPanel p = new JPanel(new GridLayout(0, 1));
        p.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        p.add(new JLabel("Default:"));
        p.add(new JComboBox<>(m));
        p.add(Box.createVerticalStrut(20));
        p.add(new JLabel("CheckedComboBox:"));
        p.add(new PCheckedComboBox<>(new PDefaultComboBoxModel<>(m), new DictCore()));

        add(p, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

//    public static void main(String... args) {
//        EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                createAndShowGui();
//            }
//        });
//    }

    public static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            IOHandler.writeErrorLog(e);
            //e.printStackTrace();
        }
        JFrame frame = new JFrame("CheckedComboBox");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ExampleCheckboxDropdown());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
