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
package PolyGlot.ManagersCollections;

import PolyGlot.CustomControls.PAlphaMap;
import PolyGlot.DictCore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author draque
 */
public class PropertiesManagerTest {
    
    DictCore core;
    
    public PropertiesManagerTest() {
        core = new DictCore();
    }

    /**
     * Test of getAlphaOrder method, of class PropertiesManager.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetAlphaOrder() throws Exception {
        System.out.println("getAlphaOrder");
        PropertiesManager instance = core.getPropertiesManager();
        instance.setAlphaOrder("a");
        PAlphaMap<String, Integer> expResult = new PAlphaMap<>();
        expResult.put("a", 0);
        PAlphaMap result = instance.getAlphaOrder();
        
        assertEquals(expResult, result);
    }

    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     * @throws java.lang.Exception
     */
    @Test
    public void testSetAlphaOrder_String_No_Dupes_Commas() throws Exception {
        System.out.println("testSetAlphaOrder_String_No_Dupes_Commas");
        String order = "a,aa,b,bb,ab";
        PropertiesManager instance = core.getPropertiesManager();
        instance.setAlphaOrder(order);
    }
    
    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     * @throws java.lang.Exception
     */
    @Test
    public void testSetAlphaOrder_String_Dupes_Commas() {
        System.out.println("testSetAlphaOrder_String_Dupes_Commas");
        String order = "a, aa, b, bb, ab, a";
        PropertiesManager instance = null; // TODO: this seems incorrect... should not be null (inspect test intent)
        try {
            instance.setAlphaOrder(order);
            fail("Expected exception not hit");
        } catch (Exception e) {
            // exception is passing state
        }
    }
    
    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     * @throws java.lang.Exception
     */
    @Test
    public void testSetAlphaOrder_String_No_Dupes_No_Commas() throws Exception {
        System.out.println("testSetAlphaOrder_String_No_Dupes_No_Commas");
        String order = "abcdefg";
        PropertiesManager instance = core.getPropertiesManager();
        instance.setAlphaOrder(order);
    }
    
    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     * @throws java.lang.Exception
     */
    @Test
    public void testSetAlphaOrder_String_Dupes_No_Commas() throws Exception {
        System.out.println("testSetAlphaOrder_String_Dupes_No_Commas");
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
     * @throws java.lang.Exception
     */
    @Test
    public void testSetAlphaOrder_String_dupes_boolean_override() throws Exception {
        System.out.println("testSetAlphaOrder_String_dupes_boolean_override");
        String order = "a,b,a";
        PropertiesManager instance = core.getPropertiesManager();
        instance.setAlphaOrder(order, true);
    }
    
    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     * @throws java.lang.Exception
     */
    @Test
    public void testSetAlphaOrder_String_no_dupes_boolean_override() throws Exception {
        System.out.println("testSetAlphaOrder_String_no_dupes_boolean_override");
        String order = "a,b,c";
        PropertiesManager instance = core.getPropertiesManager();
        instance.setAlphaOrder(order, true);
    }
    
    /**
     * Test of setAlphaOrder method, of class PropertiesManager.
     */
    @Test
    public void testSetAlphaOrder_String_dupes_boolean_no_override() {
        System.out.println("testSetAlphaOrder_String_dupes_boolean_no_override");
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
     * @throws java.lang.Exception
     */
    @Test
    public void testSetAlphaOrder_String_no_dupes_boolean_no_override() throws Exception {
        System.out.println("testSetAlphaOrder_String_no_dupes_boolean_no_override");
        String order = "a,b,c";
        PropertiesManager instance = core.getPropertiesManager();
        instance.setAlphaOrder(order, false);
    }

    /**
     * Test of getAlphaPlainText method, of class PropertiesManager.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetAlphaPlainText() throws Exception {
        System.out.println("getAlphaPlainText");
        PropertiesManager instance = core.getPropertiesManager();
        String expResult = "a,b,c,d,e";
        instance.setAlphaOrder(expResult);
        String result = instance.getAlphaPlainText();
        assertEquals(expResult, result);
    }
}
