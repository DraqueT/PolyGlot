/*
 * Copyright (c) 2015, draque thompson
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
package PolyGlot.CustomControls;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * This class overrides the button class in Java to create better graphical
 * representations of commonly used things. Also lets all iconography to be 
 * changed easily and in one place.
 * @author draque
 */
public class PButton extends JButton {
    public PButton(String arg) {        
        super();
        
        setText("");
        setBorder(null);
        setBorderPainted(false);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFocusPainted(false);
        setFocusTraversalKeysEnabled(false);
        setFocusable(false);
        setMaximumSize(new java.awt.Dimension(40, 29));
        setMinimumSize(new java.awt.Dimension(40, 29));
        setPreferredSize(new java.awt.Dimension(40, 29));
        setRequestFocusEnabled(false);
        setContentAreaFilled(false);
        
        switch (arg) {
            case "-":
                setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/PolyGlot/ImageAssets/delete_button.png")).getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH)));
                setPressedIcon(new ImageIcon(new ImageIcon(getClass().getResource("/PolyGlot/ImageAssets/delete_button_pressed.png")).getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH)));
                break;
            case "+":
                setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/PolyGlot/ImageAssets/add_button.png")).getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH))); 
                setPressedIcon(new ImageIcon(new ImageIcon(getClass().getResource("/PolyGlot/ImageAssets/add_button_pressed.png")).getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH)));
                break;
        }
    }    
}
