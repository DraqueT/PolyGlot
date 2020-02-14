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
package org.darisadesigns.polyglotlina;

import TestResources.DummyCore;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class PLanguageStatsTest {
    
    public PLanguageStatsTest() {
    }

    /**
     * Test of buildWordReport method, of class PLanguageStats.
     * 
     * Only tests that this does not blow up. Needs to be revisited once
     * the stats generation is itself rewritten.
     * 
     */
    @Test
    public void testBuildWordReport() {
        System.out.println("PLanguageStatsTest.testBuildWordReport");
      
        DictCore core = DummyCore.newCore();
        
        try {
            core.readFile(PGTUtil.TESTRESOURCES + "LangStatTest.pgd");
            String result = PLanguageStats.buildWordReport(core).trim();
            assertFalse(result.isBlank());
        } catch (IOException | IllegalStateException e) {
            fail(e);
        }
    }
    
    @Test
    public void testBuildWordReportWithAlphaSet() {
        System.out.println("PLanguageStatsTest.testBuildWordReport");
      
        DictCore core = DummyCore.newCore();
        
        try {
            core.readFile(PGTUtil.TESTRESOURCES + "LangStatTest.pgd");
            core.getPropertiesManager().setAlphaOrder("qwertyuiop-asdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM");
            String result = PLanguageStats.buildWordReport(core).trim();
            assertFalse(result.isBlank());
        } catch (Exception e) {
            fail(e);
        }
    }
    
}
