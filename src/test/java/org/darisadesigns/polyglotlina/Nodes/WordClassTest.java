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
package org.darisadesigns.polyglotlina.Nodes;

import TestResources.TestResources;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class WordClassTest {
    
    private final WordClass testClass;
    
    public WordClassTest() {
        testClass = new WordClass();
        
        try {
            testClass.addApplyType(1);
            testClass.addApplyType(3);
            testClass.addValue("TEST0", 0);
            testClass.addValue("TEST2", 2);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testIsValid() {
        assertTrue(testClass.isValid(0));
    }
    
    @Test
    public void testIsNotValid() {
        assertFalse(testClass.isValid(1));
    }

    @Test
    public void testSetEqual() {
        WordClass copy = new WordClass();
        copy.setEqual(testClass);
        assertEquals(copy, testClass);
    }

    @Test
    public void testDeleteApplyType() {
        testClass.deleteApplyType(0);
        assertFalse(testClass.appliesToType(0));
    }

    @Test
    public void testAppliesToType() {
        assertTrue(testClass.appliesToType(1));
    }
    
    @Test
    public void testNotAppliesToType() {
        assertFalse(testClass.appliesToType(0));
    }

    @Test
    public void testGetValues() {
        assertEquals(2, testClass.getValues().size());
    }

    @Test
    public void testDeleteValue() {
        try {
            testClass.deleteValue(0);
            assertEquals(1, testClass.getValues().size());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetValueById() {
        try {
            WordClassValue value = testClass.getValueById(2);
            assertEquals("TEST2", value.value);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testWriteXML() {
        String expectedValue = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<dictionary>"
                + "<wordGrammarClassNode>"
                + "<wordGrammarClassID>0</wordGrammarClassID>"
                + "<wordGrammarClassName/>"
                + "<wordGrammarIsFreeTextField>F</wordGrammarIsFreeTextField>"
                + "<wordGrammarApplyTypes>-1,1,3</wordGrammarApplyTypes>"
                + "<wordGrammarClassValuesCollection>"
                + "<wordGrammarClassValueNode>"
                + "<wordGrammarClassValueId>0</wordGrammarClassValueId>"
                + "<wordGrammarClassValueName>TEST0</wordGrammarClassValueName>"
                + "</wordGrammarClassValueNode>"
                + "<wordGrammarClassValueNode>"
                + "<wordGrammarClassValueId>2</wordGrammarClassValueId>"
                + "<wordGrammarClassValueName>TEST2</wordGrammarClassValueName>"
                + "</wordGrammarClassValueNode>"
                + "</wordGrammarClassValuesCollection></wordGrammarClassNode></dictionary>";
        System.out.println("WordClassTest:testWriteXML");
        
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(PGTUtil.DICTIONARY_XID);
            doc.appendChild(rootElement);
            
            testClass.writeXML(doc, rootElement);
        
            assertTrue(TestResources.textXmlDocEquals(doc, expectedValue));
        } catch (IOException | ParserConfigurationException | TransformerException | DOMException e) {
            fail(e);
        }
    }

    @Test
    public void testEquals() {
        WordClass copy = new WordClass();
        
        try {
            copy.addApplyType(1);
            copy.addApplyType(3);
            copy.addValue("TEST0", 0);
            copy.addValue("TEST2", 2);
        } catch (Exception e) {
            fail(e);
        }
        
        assertEquals(copy, testClass);
    }
    
    @Test
    public void testNotEquals() {
        WordClass copy = new WordClass();
        
        try {
            copy.addApplyType(0);
            copy.addApplyType(2);
            copy.addValue("TEST0", 0);
            copy.addValue("XXXFAILXXX", 2);
        } catch (Exception e) {
            fail(e);
        }
        
        assertNotEquals(copy, testClass);
    }
 
}
