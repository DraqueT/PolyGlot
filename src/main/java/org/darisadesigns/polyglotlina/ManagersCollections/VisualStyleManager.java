/*
 * Copyright (c) 2018-2019, Draque Thompson
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JTextField;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * Handles all elements of PolyGlot relating to visual styles and colors of the program
 * @author DThompson
 */
public class VisualStyleManager {
    private final DictCore core;
    private final UIDefaults uiOverrides;
    
    public VisualStyleManager(DictCore _core) {
        core = _core;
        uiOverrides = generateUIDefaultOverrides();
    }
    
    // color of regular text
    public Color getTextColor() {
        return core.getOptionsManager().isNightMode() ?
                PGTUtil.COLOR_TEXT_NIGHT:
                PGTUtil.COLOR_TEXT;
    }
    
    // color of regular text background
    public Color getTextBGColor() {
        return core.getOptionsManager().isNightMode() ?
                PGTUtil.COLOR_TEXT_BG_NIGHT:
                PGTUtil.COLOR_TEXT_BG;
    }
    
    // color of default value text
    public Color getDefaultTextColor() {
        return core.getOptionsManager().isNightMode() ?
                PGTUtil.COLOR_DEFAULT_TEXT_NIGHT:
                PGTUtil.COLOR_DEFAULT_TEXT;
    }
    
    // color of disabled text
    public Color getDisabledTextColor() {
        return core.getOptionsManager().isNightMode() ?
                PGTUtil.COLOR_TEXT_DISABLED_NIGHT:
                PGTUtil.COLOR_TEXT_DISABLED;
    }
    
    // color of disabled text BG
    public Color getDisabledTextColorBG() {
        return core.getOptionsManager().isNightMode() ?
                PGTUtil.COLOR_TEXT_DISABLED_BG_NIGHT:
                PGTUtil.COLOR_TEXT_DISABLED_BG;
    }
    
    public UIDefaults getUIOverrides() {
        return uiOverrides;
    }
    
    public Color getCheckBoxSelected(boolean isEnabled) {
        Color ret = PGTUtil.COLOR_CHECKBOX_SELECTED_DISABLED;
        
        if (isEnabled && core.getOptionsManager().isNightMode()) {
            ret = PGTUtil.COLOR_CHECKBOX_SELECTED_NIGHT;
        } else if (isEnabled) {
            ret = PGTUtil.COLOR_CHECKBOX_SELECTED;
        }
        
        return ret;
    }
    
    public Color getCheckBoxBG(boolean isEnabled) {
        Color ret = PGTUtil.COLOR_CHECKBOX_BG_DISABLED;
        
        if (isEnabled && core.getOptionsManager().isNightMode()) {
            ret = PGTUtil.COLOR_CHECKBOX_BG_NIGHT;
        } else if (isEnabled) {
            ret = PGTUtil.COLOR_CHECKBOX_BG;
        }
        
        return ret;
    }
    
    public Color getCheckBoxOutline(boolean isEnabled) {
        Color ret = PGTUtil.COLOR_CHECKBOX_OUTLINE_DISABLED;
        
        if (isEnabled && core.getOptionsManager().isNightMode()) {
            ret = PGTUtil.COLOR_CHECKBOX_OUTLINE_NIGHT;
        } else if (isEnabled) {
            ret = PGTUtil.COLOR_CHECKBOX_OUTLINE;
        }
        
        return ret;
    }
    
    public Color getCheckBoxHover(boolean isEnabled) {
        Color ret = PGTUtil.COLOR_CHECKBOX_HOVER_DISABLED;
        
        if (isEnabled && core.getOptionsManager().isNightMode()) {
            ret = PGTUtil.COLOR_CHECKBOX_HOVER_NIGHT;
        } else if (isEnabled) {
            ret = PGTUtil.COLOR_CHECKBOX_HOVER;
        }
        
        return ret;
    }
    
    public Color getCheckBoxClicked(boolean isEnabled) {
        Color ret = PGTUtil.COLOR_CHECKBOX_CLICKED_DISABLED;
        
        if (isEnabled && core.getOptionsManager().isNightMode()) {
            ret = PGTUtil.COLOR_CHECKBOX_CLICKED_NIGHT;
        } else if (isEnabled) {
            ret = PGTUtil.COLOR_CHECKBOX_CLICKED;
        }
        
        return ret;
    }
    
    
    public Color getCheckBoxFieldBack(boolean isEnabled) {
        Color ret = PGTUtil.COLOR_CHECKBOX_FIELD_BACK_DISABLED;
        
        if (isEnabled && core.getOptionsManager().isNightMode()) {
            ret = PGTUtil.COLOR_CHECKBOX_FIELD_BACK_NIGHT;
        } else if (isEnabled) {
            ret = PGTUtil.COLOR_CHECKBOX_FIELD_BACK;
        }
        
        return ret;
    }
        
    private UIDefaults generateUIDefaultOverrides() {
        UIDefaults overrides = new UIDefaults();
        UIManager.put("TextField.inactiveBackground",Color.red);
        overrides.put("TextField[Disabled].backgroundPainter", (Painter<JTextField>) 
                (Graphics2D g, JTextField field, int width, int height) -> {
            if (core.getOptionsManager().isNightMode()) {
                g.setColor(Color.darkGray);
                //Insets insets = field.getInsets();
                g.fill(new Rectangle(1, 1, width-2, height-2));
            }
        });
        overrides.put("TextField[Disabled].borderPainter", (Painter<JTextField>) 
                (Graphics2D g, JTextField field, int width, int height) -> {
            if (core.getOptionsManager().isNightMode()) {
                g.setColor(Color.gray);
            } else {
                g.setColor(Color.lightGray);
            }
            g.drawLine(1, 1, width-2, 1);
            g.drawLine(1, height-2, width-2, height-2);
            g.drawLine(width-2, 1, width-2, height-2);
            g.drawLine(1, 1, 1, height-2);
        });
        overrides.put("TextArea[Disabled].backgroundPainter", (Painter<JTextField>) 
                (Graphics2D g, JTextField field, int width, int height) -> {
            if (core.getOptionsManager().isNightMode()) {
                g.setColor(Color.darkGray);
                //Insets insets = field.getInsets();
                g.fill(new Rectangle(1, 1, width-2, height-2));
            }
        });
        overrides.put("TextArea[Disabled].borderPainter", (Painter<JTextField>) 
                (Graphics2D g, JTextField field, int width, int height) -> {
            if (core.getOptionsManager().isNightMode()) {
                g.setColor(Color.gray);
            } else {
                g.setColor(Color.lightGray);
            }
            g.drawLine(1, 1, width-2, 1);
            g.drawLine(1, height-2, width-2, height-2);
            g.drawLine(width-2, 1, width-2, height-2);
            g.drawLine(1, 1, 1, height-2);
        });
        overrides.put("TextPane[Disabled].backgroundPainter", (Painter<JTextField>) 
                (Graphics2D g, JTextField field, int width, int height) -> {
            if (core.getOptionsManager().isNightMode()) {
                g.setColor(Color.darkGray);
                //Insets insets = field.getInsets();
                g.fill(new Rectangle(1, 1, width-2, height-2));
            }
        });
        overrides.put("TextPane[Disabled].borderPainter", (Painter<JTextField>) 
                (Graphics2D g, JTextField field, int width, int height) -> {
            if (core.getOptionsManager().isNightMode()) {
                g.setColor(Color.gray);
            } else {
                g.setColor(Color.lightGray);
            }
            g.drawLine(1, 1, width-2, 1);
            g.drawLine(1, height-2, width-2, height-2);
            g.drawLine(width-2, 1, width-2, height-2);
            g.drawLine(1, 1, 1, height-2);
        });
        
        return overrides;
    }
}
