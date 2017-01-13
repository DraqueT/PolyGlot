/*
 * Copyright (c) 2017, draque.thompson
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

import PolyGlot.PGTUtil;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

/**
 * Default button for directional scrolling. Handles only cardinal directions
 * @author draque.thompson
 */
public class PArrowButton extends JButton {

    private final int direction;
    private Image imgBtn;
    private Image imgBtnPressed;
    private Image imgBtnDisabled;

    public PArrowButton(int _direction) {
        direction = _direction;
        setupButtonImg();
    }

    private void setupButtonImg() {
        switch (direction) {
            case SwingUtilities.NORTH:
                imgBtn = PGTUtil.directionButtonNorth;
                imgBtnPressed = PGTUtil.directionButtonNorthPressed;
                imgBtnDisabled = PGTUtil.directionButtonNorthDisabled;
                break;
            case SwingUtilities.WEST:
                imgBtn = PGTUtil.directionButtonWest;
                imgBtnPressed = PGTUtil.directionButtonWestPressed;
                imgBtnDisabled = PGTUtil.directionButtonWestDisabled;
                break;
            case SwingUtilities.SOUTH:
                imgBtn = PGTUtil.directionButtonSouth;
                imgBtnPressed = PGTUtil.directionButtonSouthPressed;
                imgBtnDisabled = PGTUtil.directionButtonSouthDisabled;
                break;
            case SwingUtilities.EAST:
                imgBtn = PGTUtil.directionButtonEast;
                imgBtnPressed = PGTUtil.directionButtonEastPressed;
                imgBtnDisabled = PGTUtil.directionButtonEastDisabled;
                break;
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D antiAlias = (Graphics2D) g;
        boolean isPressed = getModel().isPressed();
        boolean isEnabled = isEnabled();
        int w = getWidth();
        int h = getHeight();

        if (isPressed) {
            antiAlias.drawImage(imgBtnPressed, 0, 0, w, h, null);
        } else if (isEnabled) {
            antiAlias.drawImage(imgBtn, 0, 0, w, h, null);
        } else {
            antiAlias.drawImage(imgBtnDisabled, 0, 0, w, h, null);
        }
    }

    @Override
    // prevents this from shutting popups that it appears on
    public boolean isFocusTraversable() {
        return false;
    }
}
