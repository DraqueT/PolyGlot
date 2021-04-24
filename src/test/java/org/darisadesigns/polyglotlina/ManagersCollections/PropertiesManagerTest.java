/*
 * Copyright (c) 2019, Draque Thompson, draquemail@gmail.com
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

import TestResources.DummyCore;
import org.darisadesigns.polyglotlina.CustomControls.PAlphaMap;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.DictCore;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class PropertiesManagerTest {
    
    private final DictCore core;
    
    public PropertiesManagerTest() {
        core = DummyCore.newCore();
    }

    /**
     * Test of getAlphaOrder method, of class PropertiesManager.
     */
    @Test
    public void testGetAlphaOrder() {
        System.out.println("PropertiesManagerTest.getAlphaOrder");
        PropertiesManager instance = core.getPropertiesManager();
        
        try {
            instance.setAlphaOrder("a");
            PAlphaMap<String, Integer> expResult = new PAlphaMap<>();
            expResult.put("a", 0);
            PAlphaMap result = instance.getAlphaOrder();

            assertEquals(expResult, result);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }

    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     */
    @Test
    public void testSetAlphaOrder_String_No_Dupes_Commas() {
        System.out.println("PropertiesManagerTest.testSetAlphaOrder_String_No_Dupes_Commas");
        String order = "a,aa,b,bb,ab";
        PropertiesManager instance = core.getPropertiesManager();
        try {
            instance.setAlphaOrder(order);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     */
    @Test
    public void testSetAlphaOrder_String_Dupes_Commas() {
        System.out.println("PropertiesManagerTest.testSetAlphaOrder_String_Dupes_Commas");
        String order = "a, aa, b, bb, ab, a";
        PropertiesManager instance = core.getPropertiesManager();
        try {
            instance.setAlphaOrder(order);
            fail("Expected exception not hit");
        } catch (Exception e) {
            // exception is passing state
        }
    }
    
    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     */
    @Test
    public void testSetAlphaOrder_String_No_Dupes_No_Commas() {
        System.out.println("PropertiesManagerTest.testSetAlphaOrder_String_No_Dupes_No_Commas");
        String order = "abcdefg";
        PropertiesManager instance = core.getPropertiesManager();
        try {
            instance.setAlphaOrder(order);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     */
    @Test
    public void testSetAlphaOrder_String_Dupes_No_Commas() {
        System.out.println("PropertiesManagerTest.testSetAlphaOrder_String_Dupes_No_Commas");
        String order = "abcdefa";
        PropertiesManager instance = core.getPropertiesManager();
        try {
            instance.setAlphaOrder(order);
            fail("Expected exception not hit");
        } catch (Exception e) {
            // exception is passing state
        }
    }

    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     */
    @Test
    public void testSetAlphaOrder_String_dupes_boolean_override() {
        System.out.println("PropertiesManagerTest.testSetAlphaOrder_String_dupes_boolean_override");
        String order = "a,b,a";
        PropertiesManager instance = core.getPropertiesManager();
        
        try {
            instance.setAlphaOrder(order, true);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     */
    @Test
    public void testSetAlphaOrder_String_no_dupes_boolean_override() {
        System.out.println("PropertiesManagerTest.testSetAlphaOrder_String_no_dupes_boolean_override");
        String order = "a,b,c";
        PropertiesManager instance = core.getPropertiesManager();
        
        try {
            instance.setAlphaOrder(order, true);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     */
    @Test
    public void testSetAlphaOrder_String_dupes_boolean_no_override() {
        System.out.println("PropertiesManagerTest.testSetAlphaOrder_String_dupes_boolean_no_override");
        String order = "a,b,a";
        PropertiesManager instance = core.getPropertiesManager();
        try {
            instance.setAlphaOrder(order, false);
            fail("Expected exception not hit.");
        } catch (Exception e) {
            // exception is passing state
        }
    }
    
    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     */
    @Test
    public void testSetAlphaOrder_String_no_dupes_boolean_no_override() {
        System.out.println("PropertiesManagerTest.testSetAlphaOrder_String_no_dupes_boolean_no_override");
        String order = "a,b,c";
        PropertiesManager instance = core.getPropertiesManager();
        
        try {
            instance.setAlphaOrder(order, false);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }

    /**
     * Test of getAlphaPlainText method, of class PropertiesManager.
     */
    @Test
    public void testGetAlphaPlainText() {
        System.out.println("PropertiesManagerTest.getAlphaPlainText");
        PropertiesManager instance = core.getPropertiesManager();
        String expResult = "a,b,c,d,e";
        
        try {
            instance.setAlphaOrder(expResult);
            String result = instance.getAlphaPlainText();
            assertEquals(expResult, result);
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
}
