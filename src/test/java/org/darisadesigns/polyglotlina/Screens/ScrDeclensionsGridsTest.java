/*
 * Copyright (c) 2019, draque
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
import java.io.IOException;
import java.lang.reflect.Field;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class ScrDeclensionsGridsTest {
    final boolean headless = GraphicsEnvironment.isHeadless();
    DictCore core;
    ConWord oneDimPop;
    ConWord OneDimNopop;
    ConWord TwoDimPop;
    ConWord TwoDimNopop;
    
    public ScrDeclensionsGridsTest() throws IOException {
        core = new DictCore();
        core.readFile(PGTUtil.TESTRESOURCES + "conj_autopop_check_test.pgd");
        
        ConWordCollection words = core.getWordCollection();
        
        oneDimPop = words.getNodeById(3);
        OneDimNopop = words.getNodeById(4);
        TwoDimPop = words.getNodeById(5);
        TwoDimNopop = words.getNodeById(6);
    }
    
    @Test
    public void testOneDimPopAutoPopCheck() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (headless) {
            return;
        }
        
        ScrDeclensionsGrids screen = ScrDeclensionsGrids.run(core, oneDimPop);
        Class<?> classs = screen.getClass();
        Field field = classs.getDeclaredField("autoPopulated");
        field.setAccessible(true);
        boolean autoPop = (boolean)field.get(screen);
        
        assertTrue(autoPop);
    }
    
    @Test
    public void testOneDimNopopAutoPopCheck() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (headless) {
            return;
        }
        
        ScrDeclensionsGrids screen = ScrDeclensionsGrids.run(core, OneDimNopop);
        Class<?> classs = screen.getClass();
        Field field = classs.getDeclaredField("autoPopulated");
        field.setAccessible(true);
        boolean autoPop = (boolean)field.get(screen);
        
        assertFalse(autoPop);
    }
    
    @Test
    public void testTwoDimPopAutoPopCheck() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (headless) {
            return;
        }
        
        ScrDeclensionsGrids screen = ScrDeclensionsGrids.run(core, TwoDimPop);
        Class<?> classs = screen.getClass();
        Field field = classs.getDeclaredField("autoPopulated");
        field.setAccessible(true);
        boolean autoPop = (boolean)field.get(screen);
        
        assertTrue(autoPop);
    }
    
    @Test
    public void testTwoDimNopopAutoPopCheck() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (headless) {
            return;
        }
        
        ScrDeclensionsGrids screen = ScrDeclensionsGrids.run(core, TwoDimNopop);
        Class<?> classs = screen.getClass();
        Field field = classs.getDeclaredField("autoPopulated");
        field.setAccessible(true);
        boolean autoPop = (boolean)field.get(screen);
        
        assertFalse(autoPop);
    }
}
