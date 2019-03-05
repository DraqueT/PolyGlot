/*
 * Copyright (c) 2018, DThompson
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
import PolyGlot.Nodes.ReversionNode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This keeps track of reversion versions of a language and handles their interaction/rollbacks with the larger
 * system.
 * @author DThompson
 */
public class ReversionManager {
    private List<ReversionNode> reversionList = new ArrayList<>();
    private final DictCore core;
    
    public ReversionManager(DictCore _core) {
        core = _core;
    }
    
    /**
     * Adds a version to the beginning of the list. Truncates if versions greater than max value set in options
     * Max versions set to 0 means no limit to backup saves
     * @param addVersion byte array of raw XML of language file
     * @param saveTime The time at which this was saved
     */
    public void addVersion(byte[] addVersion, Instant saveTime) {
        ReversionNode reversion = new ReversionNode(addVersion, this);
        reversion.saveTime = saveTime;
        reversionList.add(0, reversion);
        
        int maxVersions = core.getOptionsManager().getMaxReversionCount();
        if (reversionList.size() > maxVersions && maxVersions != 0) {
            reversionList = reversionList.subList(0, maxVersions);
        }
    }
    
    /**
     * Adds a version to the end of the list. (used when loading from file)
     * @param addVersion byte array of raw XML of language file
     * @param saveTime
     */
    public void addVersionToEnd(byte[] addVersion, Instant saveTime) {
        ReversionNode reg = new ReversionNode(addVersion, this);
        reg.saveTime = saveTime;
        reversionList.add(reg);
    }

    public List<ReversionNode> getReversionList() {
        Collections.sort(reversionList);
        return reversionList;
    }
    
    public int getMaxReversionsCount() {
        return core.getOptionsManager().getMaxReversionCount();
    }
    
    /**
     * Trims reversions down to the max number allowed in the options
     */
    public void trimReversions() {
        int maxReversions = core.getOptionsManager().getMaxReversionCount();
        
        if (reversionList.size() > maxReversions) {
            reversionList = reversionList.subList(0, maxReversions);
        }
    }
}
