/*
 * Copyright (c) 2015-2019, draque
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.UIManager;

/**
 * This contains, loads and saves the options for PolyGlot (NOT for individual languages)
 *
 * @author draque
 */
public class OptionsManager {

    private boolean animateWindows = false;
    private boolean nightMode = false;
    private final List<String> lastFiles = new ArrayList<>();
    private final Map<String, Point> screenPos = new HashMap<>();
    private final Map<String, Dimension> screenSize = new HashMap<>();
    private final List<String> screensUp = new ArrayList<>();
    private Double menuFontSize = 0.0;
    private int maxReversionCount = PGTUtil.DEFAULT_MAX_ROLLBACK_NUM;
    private int toDoBarPosition = -1;

    public OptionsManager() {
    }
    
    public double getMenuFontSize() {
        return menuFontSize == 0.0 ? PGTUtil.DEFAULT_FONT_SIZE : menuFontSize;
    }
    
    public void setMenuFontSize(double _size) {
        menuFontSize = _size;
        setDefaultJavaFontSize(_size);
    }
    
    private void setDefaultJavaFontSize(double size) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Font newFont = UIManager.getFont(key);
            
            if (newFont != null) {
                UIManager.put(key, newFont.deriveFont((float)size));
            }
        }
    }
    
    /**
     * returns map of all screen positions
     * @return actual map object (modifying WILL change persistent values)
     */
    public Map<String, Point> getScreenPositions() {
        return screenPos;
    }
    
    /**
     * returns map of all screen sizes
     * @return actual map object (modifying WILL change persistent values)
     */
    public Map<String, Dimension> getScreenSizes() {
        return screenSize;
    }
    
    /**
     * Records screen up at time of program closing
     * @param screen name of screen to be recorded as being up
     */
    public void addScreenUp(String screen) {
        if (!screen.isEmpty() && ! screensUp.contains(screen)) {
            screensUp.add(screen);
        }
    }
    
    /**
     * Retrieve screens up at time of last close
     * @return list of screens up
     */
    public String[] getLastScreensUp() {
        return screensUp.toArray(new String[0]);
    }
    
    /**
     * Adds or replaces screen position of a window
     * @param screen name of window
     * @param position position of window
     */
    public void setScreenPosition(String screen, Point position) {
        if (screenPos.containsKey(screen)) {
            screenPos.replace(screen, position);
        } else {
            screenPos.put(screen, position);
        }
    }
    
    /**
     * Adds or replaces screen size of a window
     * @param screen name of window
     * @param dimension size of window
     */
    public void setScreenSize(String screen, Dimension dimension) {
        if (screenSize.containsKey(screen)) {
            screenSize.replace(screen, dimension);
        } else {
            screenSize.put(screen, dimension);
        }
    }
    
    /**
     * Retrieves last screen position of screen
     * @param screen screen to return position for
     * @return last position of screen. Null otherwise.
     */
    public Point getScreenPosition(String screen) {
        Point ret = null;
        if (screenPos.containsKey(screen)) {
            ret = screenPos.get(screen);
        }        
        return ret;
    }
    
    /**
     * Retrieves last screen size of screen
     * @param screen screen to return size for
     * @return last size of screen (stored in 
     * a Point). Null otherwise.
     */
    public Dimension getScreenSize(String screen) {
        Dimension ret = null;
        if (screenSize.containsKey(screen)) {
            ret = screenSize.get(screen);
        }        
        return ret;
    }
    
    /**
     * Retrieves list of last opened files for PolyGlot
     *
     * @return
     */
    public String[] getLastFiles() {
        return lastFiles.toArray(new String[0]);
    }
    
    /**
     * Pushes a recently opened file (if appropriate) into the recent files list
     *
     * @param file full path of file
     */
    public void pushRecentFile(String file) {
        if (!lastFiles.isEmpty()
                && lastFiles.contains(file)) {
            lastFiles.remove(file);
            lastFiles.add(file);
            return;
        }

        while (lastFiles.size() > PGTUtil.OPTIONS_NUM_LAST_FILES) {
            lastFiles.remove(0);
        }

        lastFiles.add(file);
    }

    /**
     * @return the animateWindows
     */
    public boolean isAnimateWindows() {
        return animateWindows;
    }

    /**
     * @param _animateWindows the animateWindows to set
     */
    public void setAnimateWindows(boolean _animateWindows) {
        this.animateWindows = _animateWindows;
    }

    public boolean isNightMode() {
        return nightMode;
    }

    public void setNightMode(boolean _nightMode) {
        this.nightMode = _nightMode;
    }

    public int getMaxReversionCount() {
        return maxReversionCount;
    }

    public void setMaxReversionCount(int maxRollbackVersions, DictCore core) {
        this.maxReversionCount = maxRollbackVersions;
        
        if (core != null) {
            core.getReversionManager().trimReversions();
        }
    }

    public int getToDoBarPosition() {
        return toDoBarPosition;
    }

    public void setToDoBarPosition(int _toDoBarPosition) {
        this.toDoBarPosition = _toDoBarPosition;
    }
}
