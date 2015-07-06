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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This contains, loads and saves the options for PolyGlot
 *
 * @author draque
 */
public class OptionsManager {

    private List<String> lastFiles = new ArrayList<String>();
    private final DictCore core;

    public OptionsManager(DictCore _core) {
        core = _core;
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
        }
    }
}
