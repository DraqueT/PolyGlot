/*
 * Copyright (c) 2022-2023, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop.ManagersCollections;

import TestResources.DummyCore;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.nio.file.Paths;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class DesktopOptionsManagerTest {
    
    public DesktopOptionsManagerTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testloadOptionsIni() {
        System.out.println("DesktopOptionsManager.testloadOptionsIni");
        
        try {
            String testScreenName = "Silly Sergal Merp Screen";

            boolean animatedExpected = true;
            int reversionCountExpected = 13;
            boolean nightModeExpected = true;
            Point expectedScreenPosition = new Point(13, 42);
            Dimension expectedScreenDimension = new Dimension(69, 420);
            int toDoBarPositionExpected = 666;
            int autoSavMs = 12345678;
            String gptKey = "bleeblebloop";
            boolean useConFontZompist = false;

            // create test core to set values in...
            DictCore core = DummyCore.newCore();
            DesktopOptionsManager opt = PolyGlot.getPolyGlot().getOptionsManager();

            opt.setAnimateWindows(animatedExpected);
            opt.setMaxReversionCount(reversionCountExpected);
            opt.setNightMode(nightModeExpected);
            opt.setScreenPosition(testScreenName, expectedScreenPosition);
            opt.setScreenSize(testScreenName, expectedScreenDimension);
            opt.setToDoBarPosition(toDoBarPositionExpected);
            opt.setMaximized(true);
            opt.setDividerPosition(testScreenName, toDoBarPositionExpected);
            opt.setMsBetweenSaves(autoSavMs);
            opt.setGptApiKey(gptKey);
            opt.setZompistUseConlangFont(useConFontZompist);

            // save values to disk...
            DesktopIOHandler.getInstance().writeOptionsIni(core.getConfigDirectory(), opt);

            // create new core to load saved values into...
            core = DummyCore.newCore();
            opt = PolyGlot.getPolyGlot().getOptionsManager();
            
            // reload saved values...
            DesktopIOHandler.getInstance().loadOptionsIni(core.getConfigDirectory(), opt);
            
            assertEquals(animatedExpected, opt.isAnimateWindows());
            assertEquals(reversionCountExpected, opt.getMaxReversionCount());
            assertEquals(nightModeExpected, opt.isNightMode());
            assertEquals(expectedScreenPosition, opt.getScreenPosition(testScreenName));
            assertEquals(expectedScreenDimension, opt.getScreenSize(testScreenName));
            assertEquals(toDoBarPositionExpected, opt.getToDoBarPosition());
            assertEquals(toDoBarPositionExpected, opt.getDividerPosition(testScreenName));
            assertTrue(opt.isMaximized());
            assertEquals(autoSavMs, opt.getMsBetweenSaves());
            assertEquals(gptKey, opt.getGptApiKey());
            assertEquals(useConFontZompist, opt.isZompistUseConlangFont());
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            fail(e);
        } finally {
            // clean up
            new File(PGTUtil.TESTRESOURCES + PGTUtil.POLYGLOT_INI).delete();
        }
    }
    
    @Test
    public void testloadCorruptedOptionsIni() {
        System.out.println("DesktopOptionsManager.testloadCorruptedOptionsIni");
        
        try {
            String testScreenName = "Silly Sergal Merp Screen";

            boolean animatedExpected = true;
            boolean expectedException = false;
            int reversionCountExpected = 13;
            boolean nightModeExpected = true;
            Point expectedScreenPosition = new Point(13, 42);
            Dimension expectedScreenDimension = new Dimension(69, 420);
            int toDoBarPositionExpected = 666;
            int autoSavMs = 12345678;

            // create new core to load corrupted values into...
            DictCore core = DummyCore.newCore();
            DesktopOptionsManager opt = PolyGlot.getPolyGlot().getOptionsManager();
            
            try {
                DesktopIOHandler.getInstance().loadOptionsIni(core.getConfigDirectory().resolve("iniCorrupted"), opt);
            } catch (DesktopOptionsManagerException e) {
                expectedException = true;
            }

            assertTrue(expectedException);
            assertEquals(animatedExpected, opt.isAnimateWindows());
            assertEquals(reversionCountExpected, opt.getMaxReversionCount());
            assertEquals(nightModeExpected, opt.isNightMode());
            assertEquals(expectedScreenPosition, opt.getScreenPosition(testScreenName));
            assertEquals(expectedScreenDimension, opt.getScreenSize(testScreenName));
            assertEquals(toDoBarPositionExpected, opt.getToDoBarPosition());
            assertEquals(toDoBarPositionExpected, opt.getDividerPosition(testScreenName));
            assertTrue(opt.isMaximized());
            assertEquals(autoSavMs, opt.getMsBetweenSaves());
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            fail(e);
        }
    }
}
