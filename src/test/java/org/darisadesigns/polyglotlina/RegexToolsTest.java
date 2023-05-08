/*
 * Copyright (c) 2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class RegexToolsTest {
    
    public RegexToolsTest() {
    }

    /**
     * Test of isRegexLegal method, of class RegexTools.
     */
    @Test
    public void testIsRegexLegal_legal() {
        assertTrue(RegexTools.isRegexLegal(".*"));
        assertTrue(RegexTools.isRegexLegal("e.+"));
        assertTrue(RegexTools.isRegexLegal("t"));
        assertTrue(RegexTools.isRegexLegal("r|t"));
        assertTrue(RegexTools.isRegexLegal("r(r=?)"));
    }
    
    @Test
    public void testIsRegexLegal_Illegal() {
        assertFalse(RegexTools.isRegexLegal("*a"));
        assertFalse(RegexTools.isRegexLegal("[rr"));
        assertFalse(RegexTools.isRegexLegal("\\"));
        assertFalse(RegexTools.isRegexLegal("?\\@"));
    }
    
    @Test
    public void deleteMe() {
        for (String word : "hi, , you".split(",")) {
            System.out.println("Word: " + word.trim());
        }
    }
}
