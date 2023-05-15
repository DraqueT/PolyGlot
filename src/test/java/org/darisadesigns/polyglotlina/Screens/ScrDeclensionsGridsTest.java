/*
 * Copyright (c) 2019-2023, Draque Thompson, draquemail@gmail.com
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

import TestResources.DummyCore;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.lang.reflect.Field;
import javax.xml.parsers.ParserConfigurationException;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class ScrDeclensionsGridsTest {
    private final boolean headless = GraphicsEnvironment.isHeadless();
    private final DictCore core;
    private ConWord oneDimPop;
    private ConWord OneDimNopop;
    private ConWord TwoDimPop;
    private ConWord TwoDimNopop;
    
    public ScrDeclensionsGridsTest() {
        core = DummyCore.newCore();
        PGTUtil.enterUITestingMode();
        
        try {
            core.readFile(PGTUtil.TESTRESOURCES + "conj_autopop_check_test.pgd");

            ConWordCollection words = core.getWordCollection();

            oneDimPop = words.getNodeById(3);
            OneDimNopop = words.getNodeById(4);
            TwoDimPop = words.getNodeById(5);
            TwoDimNopop = words.getNodeById(6);
        } catch (IOException | IllegalStateException | ParserConfigurationException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testOneDimPopAutoPopCheck() {
        System.out.println("ScrDeclensionsGridsTest.testOneDimPopAutoPopCheck");
        
        if (headless) {
            return;
        }
        
        try {
            ScrDeclensionsGrids screen = ScrDeclensionsGrids.run(core, oneDimPop);
            Class<?> classs = screen.getClass();
            Field field = classs.getDeclaredField("autoPopulated");
            field.setAccessible(true);
            boolean autoPop = (boolean)field.get(screen);
            
            assertTrue(autoPop);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testOneDimNopopAutoPopCheck() {
        System.out.println("ScrDeclensionsGridsTest.testOneDimNopopAutoPopCheck");
        
        if (headless) {
            return;
        }
        
        ScrDeclensionsGrids screen = ScrDeclensionsGrids.run(core, OneDimNopop);
        Class<?> classs = screen.getClass();

        try {
            Field field = classs.getDeclaredField("autoPopulated");
            field.setAccessible(true);
            boolean autoPop = (boolean)field.get(screen);

            assertFalse(autoPop);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }   
    }
    
    @Test
    public void testTwoDimPopAutoPopCheck() {
        System.out.println("ScrDeclensionsGridsTest.testTwoDimPopAutoPopCheck");
        
        if (headless) {
            return;
        }
        
        ScrDeclensionsGrids screen = ScrDeclensionsGrids.run(core, TwoDimPop);
        Class<?> classs = screen.getClass();
        
        try {
            Field field = classs.getDeclaredField("autoPopulated");
            field.setAccessible(true);
            boolean autoPop = (boolean)field.get(screen);

            assertTrue(autoPop);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testTwoDimNopopAutoPopCheck() {
        System.out.println("ScrDeclensionsGridsTest.testTwoDimNopopAutoPopCheck");
        
        if (headless) {
            return;
        }
        
        ScrDeclensionsGrids screen = ScrDeclensionsGrids.run(core, TwoDimNopop);
        Class<?> classs = screen.getClass();
        
        try {
            Field field = classs.getDeclaredField("autoPopulated");
            field.setAccessible(true);
            boolean autoPop = (boolean)field.get(screen);

            assertFalse(autoPop);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
}
