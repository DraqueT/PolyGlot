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
import javax.swing.border.Border;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.Icon;

/**
 * This class overrides the button class in Java to create better graphical
 * representations of commonly used things. Also lets all iconography to be 
 * changed easily and in one place.
 * @author draque
 */
public class PButton extends JButton {
    @Override
    public final void setText(String text){super.setText(text);}
    @Override
    public final void setBorder(Border border){super.setBorder(border);}
    @Override public final void setBorderPainted(boolean paint) {super.setBorderPainted(paint);}
    @Override
    public final void setCursor(Cursor cursor) {super.setCursor(cursor);}
    @Override
    public final void setFocusPainted(boolean paint) {super.setFocusPainted(paint);}
    @Override
    public final void setFocusTraversalKeysEnabled(boolean enable) {super.setFocusTraversalKeysEnabled(enable);}
    @Override
    public final void setMaximumSize(Dimension dim) {super.setMaximumSize(dim);}
    @Override
    public final void setFocusable(boolean focus) {super.setFocusable(focus);}
    @Override
    public final void setMinimumSize(Dimension dim) {super.setMinimumSize(dim);}
    @Override
    public final void setPreferredSize(Dimension dim) {super.setPreferredSize(dim);}
    @Override
    public final void setRequestFocusEnabled(boolean enabled) {super.setRequestFocusEnabled(enabled);}
    @Override
    public final void setContentAreaFilled(boolean filled) {super.setContentAreaFilled(filled);}
    @Override
    public final void setIcon(Icon icon) {super.setIcon(icon);}
    @Override
    public final void setPressedIcon(Icon icon) {super.setPressedIcon(icon);}
            
    public PButton(String arg) {        
        super();
        
        setText("");
        setBorder(null);
        setBorderPainted(false);
        setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFocusPainted(false);
        setFocusTraversalKeysEnabled(false);
        setFocusable(false);
        setMaximumSize(new Dimension(40, 29));
        setMinimumSize(new Dimension(40, 29));
        setPreferredSize(new Dimension(40, 29));
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
