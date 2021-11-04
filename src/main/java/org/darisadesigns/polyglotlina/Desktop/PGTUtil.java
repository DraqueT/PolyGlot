/*
 * Copyright (c) 2021, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
 * See LICENSE.TXT included with this code to read the full license agreement.

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
package org.darisadesigns.polyglotlina.Desktop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;

public class PGTUtil extends org.darisadesigns.polyglotlina.PGTUtil {
    
    // numeric constants...
    public static final Integer NUM_MENU_FLASHES = 4;
    public static final Integer MENU_FLASH_SLEEP = 200;
    
    // color constants
    public static final Color COLOR_DISABLED_BG;
    public static final Color COLOR_ENABLED_BG;
    public static final Color COLOR_SELECTED_BG;
    public static final Color COLOR_DISABLED_FOREGROUND;
    public static final Color COLOR_MOUSEOVER_BORDER;
    public static final Color COLOR_TEXT;
    public static final Color COLOR_DEFAULT_TEXT;
    public static final Color COLOR_DEFAULT_TEXT_NIGHT;
    public static final Color COLOR_TEXT_BG;
    public static final Color COLOR_TEXT_NIGHT;
    public static final Color COLOR_TEXT_BG_NIGHT;
    public static final Color COLOR_TEXT_DISABLED;
    public static final Color COLOR_TEXT_DISABLED_BG;
    public static final Color COLOR_TEXT_DISABLED_NIGHT;
    public static final Color COLOR_TEXT_DISABLED_BG_NIGHT;
    public static final Color COLOR_CHECKBOX_SELECTED;
    public static final Color COLOR_CHECKBOX_BG;
    public static final Color COLOR_CHECKBOX_OUTLINE;
    public static final Color COLOR_CHECKBOX_HOVER;
    public static final Color COLOR_CHECKBOX_CLICKED;
    public static final Color COLOR_CHECKBOX_FIELD_BACK;
    public static final Color COLOR_CHECKBOX_SELECTED_NIGHT;
    public static final Color COLOR_CHECKBOX_BG_NIGHT;
    public static final Color COLOR_CHECKBOX_OUTLINE_NIGHT;
    public static final Color COLOR_CHECKBOX_HOVER_NIGHT;
    public static final Color COLOR_CHECKBOX_CLICKED_NIGHT;
    public static final Color COLOR_CHECKBOX_FIELD_BACK_NIGHT;
    public static final Color COLOR_CHECKBOX_SELECTED_DISABLED;
    public static final Color COLOR_CHECKBOX_BG_DISABLED;
    public static final Color COLOR_CHECKBOX_OUTLINE_DISABLED;
    public static final Color COLOR_CHECKBOX_HOVER_DISABLED;
    public static final Color COLOR_CHECKBOX_CLICKED_DISABLED;
    public static final Color COLOR_CHECKBOX_FIELD_BACK_DISABLED;
    public static final Color COLOR_REQUIRED_LEX_COLOR;
    public static final Color COLOR_ERROR_FIELD;
    
    // Fonts stored here to cache values single time
    public static final Font MENU_FONT;
    public static final Font CHARIS_UNICODE;
    
    // images and icons that only need to be loaded once
    public static final ImageIcon ADD_BUTTON_ICON;
    public static final ImageIcon DEL_BUTTON_ICON;
    public static final ImageIcon ADD_BUTTON_ICON_PRESSED;
    public static final ImageIcon DEL_BUTTON_ICON_PRESSED;
    public static final ImageIcon POLYGLOT_ICON;
    
    // one time set for code driven static values
    static {
        COLOR_DISABLED_BG = Color.decode("#b0b0b0");
        COLOR_ENABLED_BG = Color.decode("#66b2ff");
        COLOR_SELECTED_BG = Color.decode("#7979ef");
        COLOR_DISABLED_FOREGROUND = Color.decode("#808080");
        COLOR_MOUSEOVER_BORDER = Color.decode("#909090");
        COLOR_TEXT = Color.decode("#000000");
        COLOR_TEXT_BG = Color.decode("#ffffff");
        COLOR_TEXT_NIGHT = Color.decode("#ffffff");
        COLOR_TEXT_BG_NIGHT = Color.decode("#000000");
        COLOR_DEFAULT_TEXT = Color.lightGray;
        COLOR_DEFAULT_TEXT_NIGHT = Color.darkGray;
        COLOR_TEXT_DISABLED = Color.lightGray;
        COLOR_TEXT_DISABLED_BG = Color.darkGray;
        COLOR_TEXT_DISABLED_NIGHT = Color.lightGray;
        COLOR_TEXT_DISABLED_BG_NIGHT = Color.darkGray;
        COLOR_CHECKBOX_SELECTED = Color.black;
        COLOR_CHECKBOX_BG = Color.white;
        COLOR_CHECKBOX_OUTLINE = Color.black;
        COLOR_CHECKBOX_HOVER = Color.black;
        COLOR_CHECKBOX_CLICKED = Color.lightGray;
        COLOR_CHECKBOX_FIELD_BACK = Color.white;
        COLOR_CHECKBOX_SELECTED_NIGHT = Color.gray;
        COLOR_CHECKBOX_BG_NIGHT = Color.black;
        COLOR_CHECKBOX_OUTLINE_NIGHT = Color.darkGray;
        COLOR_CHECKBOX_HOVER_NIGHT = Color.lightGray;
        COLOR_CHECKBOX_CLICKED_NIGHT = Color.white;
        COLOR_CHECKBOX_FIELD_BACK_NIGHT = Color.black;
        COLOR_CHECKBOX_SELECTED_DISABLED = Color.gray;
        COLOR_CHECKBOX_BG_DISABLED = Color.lightGray;
        COLOR_CHECKBOX_OUTLINE_DISABLED = Color.gray;
        COLOR_CHECKBOX_HOVER_DISABLED = Color.darkGray;
        COLOR_CHECKBOX_CLICKED_DISABLED = Color.darkGray;
        COLOR_CHECKBOX_FIELD_BACK_DISABLED = Color.gray;
        COLOR_REQUIRED_LEX_COLOR = new Color(255, 204, 204);
        COLOR_ERROR_FIELD = new Color(255, 204, 204);
        
        // loads default font on system error (never came up, but for completeness...)
        Font tmpFont;
        try {
            tmpFont = PFontHandler.getMenuFont();
        } catch (IOException e) {
            new DesktopInfoBox(null).error("PolyGlot Load Error", "Unable to load default button font.");
            DesktopIOHandler.getInstance().writeErrorLog(e, "Initilization error (PGTUtil)");
            tmpFont = javax.swing.UIManager.getDefaults().getFont("Label.font");
        }
        MENU_FONT = tmpFont;
        
        try {
            tmpFont = PFontHandler.getCharisUnicodeFontInitial();
        } catch (IOException e) {
            new DesktopInfoBox(null).error("PolyGlot Load Error", "Unable to load Charis Unicode.");
            DesktopIOHandler.getInstance().writeErrorLog(e, "Initilization error (PGTUtil)");
            tmpFont = javax.swing.UIManager.getDefaults().getFont("Label.font");
        }
        
        CHARIS_UNICODE = tmpFont;
        
        ADD_BUTTON_ICON = new ImageIcon(new ImageIcon(
                PGTUtil.class.getResource("/assets/org/DarisaDesigns/ImageAssets/add_button.png"))
                .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
        DEL_BUTTON_ICON = new ImageIcon(new ImageIcon(
                PGTUtil.class.getResource("/assets/org/DarisaDesigns/ImageAssets/delete_button.png"))
                .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
        ADD_BUTTON_ICON_PRESSED = new ImageIcon(new ImageIcon(
                PGTUtil.class.getResource("/assets/org/DarisaDesigns/ImageAssets/add_button_pressed.png"))
                .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
        DEL_BUTTON_ICON_PRESSED = new ImageIcon(new ImageIcon(
                PGTUtil.class.getResource("/assets/org/DarisaDesigns/ImageAssets/delete_button_pressed.png"))
                .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
        POLYGLOT_ICON = new ImageIcon(
                PGTUtil.class.getResource("/assets/org/DarisaDesigns/ImageAssets/PolyGlotIcon.png"));
    }
    
    /**
     * This records the mode of a given PDialog or PFrame window. Defaults to
     * STANDARD
     */
    public enum WindowMode {
        STANDARD, SINGLEVALUE, SELECTLIST
    }
    
    /**
     * Adds attributes to fontmapping
     *
     * @param key Key value
     * @param value value-value
     * @param font font to add value to
     * @return newly derived font
     */
    @SuppressWarnings("unchecked") // No good way to do this in a type safe manner.
    public static Font addFontAttribute(Object key, Object value, Font font) {
        Map attributes = font.getAttributes();
        attributes.put(key, value);
        return font.deriveFont(attributes);
    }
    
    /**
     * Checks that the position is in bounds for the screen and places it in
     * visible area if not
     *
     * @param w
     */
    public static void checkPositionInBounds(Window w) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point location = w.getLocationOnScreen();

        // if this would appear offscreen, simply place it in the center of the screen
        if (screenSize.getWidth() < location.x || screenSize.getHeight() < location.y) {
            w.setLocationRelativeTo(null);
        }
    }
    
    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img) {
        BufferedImage ret = null;
        
        if (img instanceof BufferedImage) {
            ret = (BufferedImage) img;
        } else if (img != null) {
            // Create a buffered image with transparency
            ret = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

            // Draw the image on to the buffered image
            Graphics2D bGr = ret.createGraphics();
            bGr.drawImage(img, 0, 0, null);
            bGr.dispose();
        }

        return ret;
    }
    
    /**
     * gets a worker that can make a given component flash
     *
     * @param flashMe component to make flash
     * @param flashColor color to use for flashing
     * @param isBack whether display color is background (rather than foreground)
     * @return SwingWorker that will make given component flash if run
     */
    public static SwingWorker getFlashWorker(final JComponent flashMe, final Color flashColor, final boolean isBack) {
        // this will pop out in its own little thread...
        return new SwingWorker() {
            @Override
            protected Object doInBackground() {
                Color originColor;
                if (isBack) {
                    originColor = flashMe.getBackground();
                } else {
                    originColor = flashMe.getForeground();
                }

                Color requiredColor = flashColor.equals(originColor)
                        ? Color.white : flashColor;

                try {
                    for (int i = 0; i < PGTUtil.NUM_MENU_FLASHES; i++) {
                        if (isBack) {
                            flashMe.setBackground(requiredColor);
                        } else {
                            flashMe.setEnabled(false);
                        }
                        // suppression for this is broken. Super annoying.
                        Thread.sleep(PGTUtil.MENU_FLASH_SLEEP);
                        if (isBack) {
                            flashMe.setBackground(originColor);
                        } else {
                            flashMe.setEnabled(true);
                        }
                        Thread.sleep(PGTUtil.MENU_FLASH_SLEEP);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    // catch of thread interrupt not logworthy
                    // IOHandler.writeErrorLog(e);
                }

                return null;
            }
        };
    }
    
    /**
     * converts arbitrarily sized image to one appropriate for a button icon
     * size
     *
     * @param rawImage image to shrink
     * @return image of appropriate size
     */
    public static ImageIcon getButtonSizeIcon(ImageIcon rawImage) {
        return getSizedIcon(rawImage, 30, 30);
    }

    /**
     * converts an icon to a user defined size for buttons
     *
     * @param rawImage image to convert
     * @param width new width
     * @param height new height
     * @return resized image
     */
    public static ImageIcon getSizedIcon(ImageIcon rawImage, int width, int height) {
        return new ImageIcon(rawImage.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }
    
    @Override
    public boolean isBlank(String test) {
        return test.isBlank();
    }
    
    public PGTUtil() {super();}
}
