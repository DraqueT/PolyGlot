/*
 * Copyright (c) 2018-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Screens;

import java.awt.GraphicsEnvironment;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author DThompson
 */
public class ScrUpdateAlertTest {
    private ScrUpdateAlert updateAlert;
    
    public ScrUpdateAlertTest() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        
            try {
                // TODO: Re-enstate this later when I have network access...
                // TODO: Rewrite so that these are skipped when there is no internet onnection (use Assumptions as above)
//                updateAlert = new ScrUpdateAlert(false, DummyCore.newCore());
            } catch (Exception e) {
                boolean noConnection = e.getLocalizedMessage().contains("No Internet connection detected.");
                
                Assumptions.assumeFalse(noConnection);
                
                if (!noConnection) {
                    fail(e);
                }
            }
    }

    /**
     * Test of testRun method, of class ScrUpdateAlert.
     */
    @Test
    public void testTestRun() {
        System.out.println("ScrUpdateAlertTest.testTestRun");
        try {
            // TODO: Re-enstate this later when I have network access...
            // TODO: Rewrite so that these are skipped when there is no internet onnection (use Assumptions as above)
//            updateAlert.testRun();
//            updateAlert.dispose();
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
}
