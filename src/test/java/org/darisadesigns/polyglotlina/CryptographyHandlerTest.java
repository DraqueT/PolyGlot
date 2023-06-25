/*
 * Copyright (c) 2023, Draque Thompson, draquemail@gmail.com
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

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draquethompson
 */
public class CryptographyHandlerTest {
    
    public CryptographyHandlerTest() {
    }
    
    @Test
    public void testEncryptDecrypt() {
        System.out.println("CryptographyHandlerTest.testEncryptDecrypt");
        
        String testKey = "ZimZamMbamMcSlam";
        String testString = "Now is the time for all good men to come to the aid of their parties.";
       
        try {
            String encrypted = CryptographyHandler.encrypt(testString, testKey);
            String dectypted = CryptographyHandler.decrypt(encrypted, testKey);
            
            assertEquals(testString, dectypted);
        } catch (CryptographyHandler.PCryptographyException e) {
            fail(e);
        }
    }
    
    @Test
    public void testEncryptKeyTooShort() {
        System.out.println("CryptographyHandlerTest.testEncryptKeyTooShort");
        
        Exception exception = assertThrows(
                CryptographyHandler.PCryptographyException.class,
                () -> CryptographyHandler.encrypt("blah blah", "tooSmall")
        );
        
        assertEquals(exception.getLocalizedMessage(), "Your secret key must have at least 16 bytes.");
    }
    
    @Test
    public void testDecryptKeyTooShort() {
        System.out.println("CryptographyHandlerTest.testDecryptKeyTooShort");
        
        Exception exception = assertThrows(
                CryptographyHandler.PCryptographyException.class,
                () -> CryptographyHandler.decrypt("blah blah", "tooSmall")
        );
        
        assertEquals(exception.getLocalizedMessage(), "Your secret key must have at least 16 bytes.");
    }
}
