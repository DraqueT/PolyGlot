/*
 * Copyright (c) 2020-2022, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopOptionsManager;
import TestResources.DummyCore;
import java.awt.Dimension;
import java.awt.Point;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class OptionsManagerTest {
    
    public OptionsManagerTest() {
    }

    @Test
    public void testResetOptions() {
        System.out.println("OptionsManagerTest.testResetOptions");
        
        DesktopOptionsManager mgr = new DesktopOptionsManager(DummyCore.newCore());
        
        mgr.setAnimateWindows(true);
        mgr.setMaxReversionCount(999);
        mgr.setNightMode(true);
        mgr.setScreenPosition("ZIMZAM", new Point(999,999));
        mgr.setScreenSize("ZIMZAM", new Dimension(999,999));
        mgr.setToDoBarPosition(999);
        mgr.pushRecentFile("BLEEPBLOOP");
        
        mgr.resetOptions();
        
        assertFalse(mgr.isAnimateWindows());
        assertEquals(PGTUtil.DEFAULT_MAX_ROLLBACK_NUM, mgr.getMaxReversionCount());
        assertFalse(mgr.isNightMode());
        assertTrue(mgr.getScreenPositions().isEmpty());
        assertTrue(mgr.getScreenSizes().isEmpty());
        assertEquals(-1, mgr.getToDoBarPosition());
        assertEquals(0, mgr.getLastFiles().length);
    }
}
