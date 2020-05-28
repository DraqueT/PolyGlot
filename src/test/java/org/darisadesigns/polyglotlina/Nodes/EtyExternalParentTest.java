/*
 * Copyright (c) 2019-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Nodes;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class EtyExternalParentTest {
    
    private final EtyExternalParent testParent;
    
    public EtyExternalParentTest() {
        testParent = new EtyExternalParent();
        
        testParent.setDefinition("DEFINITION");
        testParent.setExternalLanguage("ZOTTOPIA");
        testParent.setValue("VALUE");
    }

    @Test
    public void testGetUniqueId() {
        System.out.println("EtyExternalParentTes.:testGetUniqueId");
        
        assertEquals("VALUEZOTTOPIA", testParent.getUniqueId());
    }

    @Test
    public void testToStringWithLanguage() {
        System.out.println("EtyExternalParentTest.testToStringWithLanguage");
        
        assertEquals("VALUE (ZOTTOPIA)", testParent.toString());
    }
    
     @Test
    public void testToStringWithoutLanguage() {
        System.out.println("EtyExternalParentTest.testToStringWithoutLanguage");
        
        testParent.setExternalLanguage("");
        assertEquals("VALUE", testParent.toString());
    }

    @Test
    public void testSetEqual() {
        System.out.println("EtyExternalParentTest.testSetEqual");
        
        EtyExternalParent copy = new EtyExternalParent();
        copy.setEqual(testParent);
        assertEquals(copy, testParent);
    }

    @Test
    public void testEquals() {
        System.out.println("EtyExternalParentTest.testEquals");
        
        EtyExternalParent copy = new EtyExternalParent();
        copy.setDefinition("DEFINITION");
        copy.setExternalLanguage("ZOTTOPIA");
        copy.setValue("VALUE");
        assertEquals(copy, testParent);
    }
    
    @Test
    public void testNotEquals() {
        System.out.println("EtyExternalParentTest.testEquals");
        
        EtyExternalParent copy = new EtyExternalParent();
        copy.setDefinition("DEFINITION");
        copy.setExternalLanguage("ZOTTOPIA");
        copy.setValue("BADVALUEZZZZ");
        assertNotEquals(copy, testParent);
    }
}
