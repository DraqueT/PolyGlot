/*
 * Copyright (c) 2015, draque
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
package PolyGlot.ManagersCollections;

import PolyGlot.DictCore;
import PolyGlot.PGTUtil;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This contains, loads and saves the options for PolyGlot
 *
 * @author draque
 */
public class OptionsManager {

    private List<String> lastFiles = new ArrayList<>();
    private final Map<String, Point> screenPos = new HashMap<>();
    private final Map<String, Dimension> screenSize = new HashMap<>();
    private final List<String> screensUp = new ArrayList<>();
    private final DictCore core;

    public OptionsManager(DictCore _core) {
        core = _core;
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
    public List<String> getLastScreensUp() {
        return screensUp;
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
    public List<String> getLastFiles() {
        return lastFiles;
    }
    
    public void setLastFiles(List<String> _lastFiles) {
        lastFiles = _lastFiles;
    }

    // TODO: move these to IO? What good way would there be to do that? Consider.
    /**
     * Loads all option data from ini file, if none, ignore. One will be created
     * on exit.
     *
     * @throws IOException on failure to open existing file
     */
    public void loadIni() throws Exception {
        File f = new File(core.getWorkingDirectory() + PGTUtil.polyGlotIni);
        if(!f.exists() || f.isDirectory()) {
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(
                core.getWorkingDirectory() + PGTUtil.polyGlotIni))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] bothVal = line.split("=");
                
                // if no value set, move on
                if (bothVal.length == 1) {
                    continue;
                }
                
                // if multiple values, something has gone wrong
                if (bothVal.length != 2) {
                    throw new Exception("PolyGlot.ini corrupt or unreadable.");
                }

                switch(bothVal[0]) {
                    case PGTUtil.optionsLastFiles:
                        lastFiles.addAll(Arrays.asList(bothVal[1].split(",")));
                        break;
                    case PGTUtil.optionsScreensOpen:
                        for (String screen : bothVal[1].split(",")) {
                            addScreenUp(screen);
                        }
                        break;
                    case PGTUtil.optionsScreenPos:
                        for (String curPosSet : bothVal[1].split(",")) {
                            if (curPosSet.isEmpty()) {
                                continue;
                            }
                            
                            String[] splitSet = curPosSet.split(":");
                            
                            if (splitSet.length != 3) {
                                throw new Exception("Malformed Screen Position: " 
                                        + curPosSet);
                            }
                            Point p = new Point(Integer.parseInt(splitSet[1]), Integer.parseInt(splitSet[2]));
                            setScreenPosition(splitSet[0], p);     
                        }
                        break;
                    case PGTUtil.optionsScreensSize:
                        for (String curSizeSet : bothVal[1].split(",")) {
                            if (curSizeSet.isEmpty()) {
                                continue;
                            }
                            
                            String[] splitSet = curSizeSet.split(":");
                            
                            if (splitSet.length != 3) {
                                throw new Exception("Malformed Screen Size: " 
                                        + curSizeSet);
                            }
                            Dimension d = new Dimension(Integer.parseInt(splitSet[1]), Integer.parseInt(splitSet[2]));
                            setScreenSize(splitSet[0], d);
                        }
                        break;
                    case "\n":
                        break;
                    default:
                        throw new Exception ("Unrecognized value: " + bothVal[0] 
                                + " in PolyGlot.ini.");
                }
            }
        }
    }

    /**
     * Saves ini file to disk
     *
     * @throws IOException on failure to write
     */
    public void saveIni() throws IOException {
        try (FileWriter f0 = new FileWriter(core.getWorkingDirectory() 
                + PGTUtil.polyGlotIni)) {
            String newLine = System.getProperty("line.separator");
            String nextLine;
            
            nextLine = PGTUtil.optionsLastFiles + "=";
            for (String file : lastFiles) {
                if (nextLine.endsWith("=")) {
                    nextLine += file;
                } else {
                    nextLine += ("," + file);
                }
            }
            
            f0.write(nextLine + newLine);
            
            nextLine = PGTUtil.optionsScreenPos + "=";            
            for (Entry<String, Point> curPos : screenPos.entrySet()) {
                nextLine += ("," + curPos.getKey() + ":" + curPos.getValue().x + ":" 
                        + curPos.getValue().y);
            }
            
            f0.write(nextLine + newLine);
            
            nextLine = PGTUtil.optionsScreensSize + "=";
            for (Entry<String, Dimension> curSize : screenSize.entrySet()) {
                nextLine += ("," + curSize.getKey() + ":" + curSize.getValue().width + ":" 
                        + curSize.getValue().height);
            }
            
            f0.write(nextLine + newLine);
            
            nextLine = PGTUtil.optionsScreensOpen + "=";
            
            for (String screen : screensUp) {
                nextLine += ("," + screen);
            }
            
            f0.write(nextLine + newLine);
        }
    }
}
