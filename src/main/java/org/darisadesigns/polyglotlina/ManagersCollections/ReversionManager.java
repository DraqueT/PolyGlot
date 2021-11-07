/*
 * Copyright (c) 2018-2021, Draque Thompson
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ReversionNode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.darisadesigns.polyglotlina.PGTUtil;

/**
 * This keeps track of reversion versions of a language and handles their interaction/rollbacks with the larger
 * system.
 * @author DThompson
 */
public class ReversionManager {
    private List<ReversionNode> reversionList = new ArrayList<>();
    private final DictCore core;
    private int maxReversionCount = PGTUtil.DEFAULT_MAX_ROLLBACK_NUM;
    
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
        ReversionNode reversion = new ReversionNode(addVersion, saveTime, core);
        reversionList.add(0, reversion);
        
        trimReversions();
    }
    
    /**
     * Adds a version to the end of the list. (used when loading from file)
     * @param addVersion byte array of raw XML of language file
     */
    public void addVersionToEnd(byte[] addVersion) {
        ReversionNode reg = new ReversionNode(addVersion, core);
        reversionList.add(reg);
    }

    public ReversionNode[] getReversionList() {
        Collections.sort(reversionList);
        return reversionList.toArray(new ReversionNode[0]);
    }
    
    public int getMaxReversionsCount() {
        return maxReversionCount;
    }
    
    public void setMaxReversionCount(int maxRollbackVersions) {
        setMaxReversionCount(maxRollbackVersions, false);
    }
    
    public void setMaxReversionCount(int maxRollbackVersions, boolean trimRevisions) {
        this.maxReversionCount = maxRollbackVersions;
        if(trimRevisions)
            trimReversions();
    }
    
    /**
     * Trims reversions down to the max number allowed in the options
     */
    public void trimReversions() {
        if (reversionList.size() > maxReversionCount && maxReversionCount != 0)
            reversionList = reversionList.subList(0, maxReversionCount);
    }
}
