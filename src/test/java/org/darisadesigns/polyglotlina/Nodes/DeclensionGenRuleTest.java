/*
 * Copyright (c) 2019-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Nodes;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class DeclensionGenRuleTest {

    @Test
    public void setEqual() {
        System.out.println("DeclensionGenRuleTest.setEqual");
        DeclensionGenRule a = getGenericRule();
        DeclensionGenRule b = new DeclensionGenRule();
        
        b.setEqual(a, true);
        
        assertTrue(a.valuesEqual(b));
    }

    @Test
    public void testEqualsEmpty() {
        System.out.println("DeclensionGenRuleTest.testEqualsEmpty");
        DeclensionGenRule a = new DeclensionGenRule();
        DeclensionGenRule b = new DeclensionGenRule();
        
        assertTrue(a.valuesEqual(b));
    }
    
    @Test
    public void testEqualsPopulated() {
        System.out.println("DeclensionGenRuleTest.testEqualsPopulated");
        DeclensionGenRule a = getGenericRule();
        DeclensionGenRule b = getGenericRule();
        
        assertTrue(a.valuesEqual(b));
    }
    
    @Test
    public void testNotEqualsNull() {
        System.out.println("DeclensionGenRuleTest.testNotEqualsNull");
        DeclensionGenRule a = getGenericRule();
        DeclensionGenRule b = null;
        
        assertFalse(a.valuesEqual(b));
    }
    
    @Test
    public void testNotEqualsDiffType() {
        System.out.println("DeclensionGenRuleTest.testNotEqualsDiffType");
        DeclensionGenRule a = getGenericRule();
        Integer b = 5;
        
        assertFalse(a.valuesEqual(b));
    }
    
    @Test
    public void testNotEqualsDiffName() {
        System.out.println("DeclensionGenRuleTest.testNotEqualsDiffName");
        DeclensionGenRule a = getGenericRule();
        DeclensionGenRule b = getGenericRule();
        
        b.setName("DIFFERENT NAME");
        
        assertFalse(a.valuesEqual(b));
    }
    
    @Test
    public void testNotEqualsDiffRegex() {
        System.out.println("DeclensionGenRuleTest.testNotEqualsDiffRegex");
        DeclensionGenRule a = getGenericRule();
        DeclensionGenRule b = getGenericRule();
        
        b.setRegex("BLARGH");
        
        assertFalse(a.valuesEqual(b));
    }
    
    @Test
    public void testNotEqualsDiffPOS() {
        System.out.println("DeclensionGenRuleTest.testNotEqualsDiffPOS");
        DeclensionGenRule a = getGenericRule();
        DeclensionGenRule b = getGenericRule();
        
        b.setTypeId(666);
        
        assertFalse(a.valuesEqual(b));
    }
    
    @Test
    public void testNotEqualsDiffRules() {
        System.out.println("DeclensionGenRuleTest.testNotEqualsDiffRules");
        
        DeclensionGenRule a = getGenericRule();
        DeclensionGenRule b = getGenericRule();
        
        a.addTransform(new DeclensionGenTransform("a", "b"));
        b.addTransform(new DeclensionGenTransform("y", "z"));
        
        assertFalse(a.valuesEqual(b));
    }
    
    private DeclensionGenRule getGenericRule() {
        DeclensionGenRule ret = new DeclensionGenRule();
        
        ret.setName("NAME");
        ret.setCombinationId("1,3,5,7,8");
        ret.setRegex(".*");
        ret.setTypeId(4);
        
        ret.addTransform(new DeclensionGenTransform("1", "2"));
        ret.addTransform(new DeclensionGenTransform("2", "3"));
        ret.addTransform(new DeclensionGenTransform("4", "5"));
        
        return ret;
    }
}
