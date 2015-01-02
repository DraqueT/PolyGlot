/*
 * Copyright (c) 2014, draque
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
package PolyGlot;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author draque
 */
public class RankedObjectTest {
    
    public RankedObjectTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of getRank method, of class RankedObject.
     */
    @Test
    public void testGetRank() {
        System.out.println("getRank");
        RankedObject instance = null;
        int expResult = 0;
        int result = instance.getRank();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHolder method, of class RankedObject.
     */
    @Test
    public void testGetHolder() {
        System.out.println("getHolder");
        RankedObject instance = null;
        Object expResult = null;
        Object result = instance.getHolder();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescending method, of class RankedObject.
     */
    @Test
    public void testSetDescending() {
        System.out.println("setDescending");
        boolean descending = false;
        RankedObject instance = null;
        instance.setDescending(descending);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of compareTo method, of class RankedObject.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        RankedObject _compare = null;
        RankedObject instance = null;
        int expResult = 0;
        int result = instance.compareTo(_compare);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
