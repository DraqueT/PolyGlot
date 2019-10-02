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
package org.darisadesigns.polyglotlina.Screens;

import java.awt.GraphicsEnvironment;
import org.darisadesigns.polyglotlina.DictCore;
import org.junit.jupiter.api.Test;

/**
 *
 * @author DThompson
 */
public class ScrUpdateAlertTest {
    private final DictCore core = new DictCore();
    private final boolean headless = GraphicsEnvironment.isHeadless();
    private ScrUpdateAlert updateAlert;
    
    public ScrUpdateAlertTest() {
        if (!headless) {
            try {
                updateAlert = new ScrUpdateAlert(false, core);
            } catch (Exception e) {
                // Instantiation tested elsewhere, skip errors here.
            }
        }
    }

    /**
     * Test of testRun method, of class ScrUpdateAlert.
     * @throws java.lang.Exception
     */
    @Test
    public void testTestRun() throws Exception {
        if (headless) {
            return;
        }
        
        updateAlert.testRun();
        updateAlert.dispose();
    }
}
