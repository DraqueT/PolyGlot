/*
 * Copyright (c) 2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import TestResources.DummyCore;
import java.time.Instant;
import org.darisadesigns.polyglotlina.DictCore;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class ReversionManagerTest {
    
    public ReversionManagerTest() {
    }
    
    /**
     * Test of trimReversions method, of class ReversionManager.
     */
    @Test
    public void testTrimReversions() {
        System.out.println("ReversionManagerTest.testTrimReversions");
        
        int expectedCount = 1;
        
        DictCore core = DummyCore.newCore();
        core.getOptionsManager().setMaxReversionCount(10, core);
        ReversionManager revMan = core.getReversionManager();
        
        byte[] dummyRev = {1,2,3,4};
        
        revMan.addVersion(dummyRev, Instant.now());
        revMan.addVersion(dummyRev, Instant.now());
        revMan.addVersion(dummyRev, Instant.now());
        core.getOptionsManager().setMaxReversionCount(1, core);
        
        int result = revMan.getReversionList().length;
        
        assertEquals(expectedCount, result);
    }
    
}
