/*
 * Copyright (c) 2015-2022, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
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
package org.darisadesigns.polyglotlina.Desktop.ManagersCollections;

import org.darisadesigns.polyglotlina.DictCore;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;

/**
 * This contains, loads and saves the options for PolyGlot (NOT for individual languages)
 *
 * @author draque
 */
public class DesktopOptionsManager {

    private boolean animateWindows = false;
    private boolean nightMode = false;
    private final List<String> lastFiles = new ArrayList<>();
    private final Map<String, Point> screenPos = new HashMap<>();
    private final Map<String, Dimension> screenSize = new HashMap<>();
    private final Map<String, Integer> dividerPosition = new HashMap<>();
    private boolean maximized = false;
    private final List<String> screensUp = new ArrayList<>();
    private int toDoBarPosition = -1;
    private DictCore core;
    private final javafx.scene.text.Font menuFontFX;
    private int msBetweenSaves;
    private double uiScale = 2.0;

    public DesktopOptionsManager() {
        msBetweenSaves = PGTUtil.DEFAULT_MS_BETWEEN_AUTO_SAVES;
        java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(PGTUtil.MENU_FONT);
        menuFontFX = javafx.scene.text.Font.font(PGTUtil.MENU_FONT.getFamily(), PGTUtil.DEFAULT_FONT_SIZE);
    }
    
    public DesktopOptionsManager(DictCore _core) {
        this();
        core = _core;
    }
    
    public void setCore(DictCore _core) {
        core = _core;
    }
    
    /**
     * Resets all options to their base values (including things like screen position/size)
     */
    public void resetOptions() {
        animateWindows = false;
        nightMode = false;
        lastFiles.clear();
        screenPos.clear();
        screenSize.clear();
        screensUp.clear();
        toDoBarPosition = -1;
        uiScale = 2;
        
        if (core != null) {
            core.getReversionManager().setMaxReversionCount(PGTUtil.DEFAULT_MAX_ROLLBACK_NUM);
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
    
    public Map<String, Integer> getDividerPositions() {
        return dividerPosition;
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
    
    public void setDividerPosition(String screen, int position) {
        if (dividerPosition.containsKey(screen)) {
            dividerPosition.replace(screen, position);
        } else {
            dividerPosition.put(screen, position);
        }
    }
    
    /**
     * Gets divider position saved for screen. Returns -1 if none recorded.
     * @param screen
     * @return 
     */
    public int getDividerPosition(String screen) {
        int ret = -1;
        
        if (dividerPosition.containsKey(screen)) {
            ret = dividerPosition.get(screen);
        }
        
        return ret;
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
        return core == null ? 0 : core.getReversionManager().getMaxReversionsCount();
    }

    public void setMaxReversionCount(int maxRollbackVersions) {
        if (core != null) {
            core.getReversionManager().setMaxReversionCount(maxRollbackVersions, true);
        }
    }

    public int getToDoBarPosition() {
        return toDoBarPosition;
    }

    public void setToDoBarPosition(int _toDoBarPosition) {
        this.toDoBarPosition = _toDoBarPosition;
    }
    
    public void setMaximized(boolean _maximized) {
        maximized = _maximized;
    }
    
    public boolean isMaximized() {
        return maximized;
    }
    
    /**
     * Gets the java FX version of an AWT font
     *
     * @return javafx font
     */
    public javafx.scene.text.Font getFXMenuFont() {
        return menuFontFX;
    }

    public int getMsBetweenSaves() {
        return msBetweenSaves;
    }

    public void setMsBetweenSaves(int msBetweenSaves) {
        this.msBetweenSaves = msBetweenSaves;
    }

    public double getUiScale() {
        return uiScale;
    }

    public void setUiScale(double uiScale) {
        if (uiScale < 0.5) {
            uiScale = 0.5;
        }
        this.uiScale = uiScale;
    }
    
    /**
     * Loads all option data from ini file, if none, ignore.One will be created
     * on exit.
     *
     * @param workingDirectory
     * @throws java.lang.Exception
     */
    public void loadOptionsIni(String workingDirectory) throws Exception {
        File f = new File(workingDirectory + File.separator + org.darisadesigns.polyglotlina.Desktop.PGTUtil.POLYGLOT_INI);
        if (!f.exists() || f.isDirectory()) {
            return;
        }

        try ( BufferedReader br = new BufferedReader(new FileReader(
                workingDirectory + File.separator + org.darisadesigns.polyglotlina.Desktop.PGTUtil.POLYGLOT_INI, StandardCharsets.UTF_8))) {
            String loadProblems = "";

            for (String line; (line = br.readLine()) != null;) {
                try {
                    String[] bothVal = line.split("=");

                    // if no value set, move on
                    if (bothVal.length == 1) {
                        continue;
                    }

                    // if multiple values, something has gone wrong
                    if (bothVal.length != 2) {
                        throw new Exception("Unreadable value in PolyGlot.ini" + (bothVal.length > 0 ? ": " + bothVal[0] : "."));
                    }

                    switch (bothVal[0]) {
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_LAST_FILES -> {
                            for (String last : bothVal[1].split(",")) {
                                pushRecentFile(last);
                            }
                        }
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_SCREENS_OPEN -> {
                            for (String screen : bothVal[1].split(",")) {
                                addScreenUp(screen);
                            }
                        }
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_SCREEN_POS -> {
                            for (String curPosSet : bothVal[1].split(",")) {
                                if (curPosSet.isEmpty()) {
                                    continue;
                                }

                                String[] splitSet = curPosSet.split(":");

                                if (splitSet.length != 3) {
                                    loadProblems += "Malformed Screen Position: " + curPosSet + "\n";
                                    continue;
                                }
                                Point p = new Point(Integer.parseInt(splitSet[1]), Integer.parseInt(splitSet[2]));
                                setScreenPosition(splitSet[0], p);
                            }
                        }
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_SCREENS_SIZE -> {
                            for (String curSizeSet : bothVal[1].split(",")) {
                                if (curSizeSet.isEmpty()) {
                                    continue;
                                }

                                String[] splitSet = curSizeSet.split(":");

                                if (splitSet.length != 3) {
                                    loadProblems += "Malformed Screen Size: " + curSizeSet + "\n";
                                    continue;
                                }
                                Dimension d = new Dimension(Integer.parseInt(splitSet[1]), Integer.parseInt(splitSet[2]));
                                setScreenSize(splitSet[0], d);
                            }
                        }
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_DIVIDER_POSITION -> {
                            for (String curPosition : bothVal[1].split(",")) {
                                if (curPosition.isEmpty()) {
                                    continue;
                                }

                                String[] splitSet = curPosition.split(":");

                                if (splitSet.length != 2) {
                                    loadProblems += "Malformed divider position: " + curPosition + "\n";
                                    continue;
                                }
                                Integer position = Integer.parseInt(splitSet[1]);
                                setDividerPosition(splitSet[0], position);
                            }
                        }
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_MSBETWEENSAVES ->
                            setMsBetweenSaves(Integer.parseInt(bothVal[1]));
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_AUTO_RESIZE ->
                            setAnimateWindows(bothVal[1].equals(org.darisadesigns.polyglotlina.Desktop.PGTUtil.TRUE));
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_MAXIMIZED ->
                            setMaximized(bothVal[1].equals(org.darisadesigns.polyglotlina.Desktop.PGTUtil.TRUE));
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_NIGHT_MODE ->
                            setNightMode(bothVal[1].equals(org.darisadesigns.polyglotlina.Desktop.PGTUtil.TRUE));
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_REVERSIONS_COUNT ->
                            setMaxReversionCount(Integer.parseInt(bothVal[1]));
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_TODO_DIV_LOCATION ->
                            setToDoBarPosition(Integer.parseInt(bothVal[1]));
                        case org.darisadesigns.polyglotlina.Desktop.PGTUtil.OPTIONS_UI_SCALE ->
                            setUiScale(Double.parseDouble(bothVal[1]));
                        default -> {}
                    }
                } catch (Exception e) {
                    loadProblems += e.getLocalizedMessage() + "\n";
                }
            }

            if (!loadProblems.isEmpty()) {
                throw new DesktopOptionsManagerException("Problems encountered when loading configuration file.\n" 
                        + "Corrupted values discarded: \n" + loadProblems);
            }
        }
    }
}
