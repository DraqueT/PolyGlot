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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class DeclensionDimensionTest {

    @Test
    public void testSetEqual() {
        System.out.println("DeclensionDimensionTest.testSetEqual");
        
        String expectedValue = "TESTVAL";
        DeclensionDimension source = new DeclensionDimension();
        DeclensionDimension copyTo = new DeclensionDimension();
        
        source.setValue(expectedValue);
        copyTo.setEqual(source);
        
        assertEquals(expectedValue, copyTo.value);
    }

    @Test
    public void testWriteXML() {
        System.out.println("DeclensionDimensionTest.testWriteXML");
        
        String expectedValue = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<dictionary>"
                + "<dimensionNode>"
                + "<dimensionId>-1</dimensionId>"
                + "<dimensionName>TESTVAL</dimensionName>"
                + "</dimensionNode></dictionary>";
        
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(PGTUtil.DICTIONARY_XID);
            doc.appendChild(rootElement);
            
            DeclensionDimension testDim = new DeclensionDimension();
            testDim.setValue("TESTVAL");
            testDim.writeXML(doc, rootElement);
        
            assertTrue(TestResources.textXmlDocEquals(doc, expectedValue));
        } catch (IOException | TransformerException |ParserConfigurationException e) {
            fail(e);
        }
    }

    @Test
    public void testEquals() {
        System.out.println("DeclensionDimensionTest.testEquals");
        
        DeclensionDimension testDim1 = new DeclensionDimension();
        DeclensionDimension testDim2 = new DeclensionDimension();
        String testVal = "TESTO";
        
        testDim1.setValue(testVal);
        testDim2.setValue(testVal);
        
        assertTrue(testDim1.equals(testDim2));
    }
    
    @Test
    public void testNotEquals() {
        System.out.println("DeclensionDimensionTest.testNotEquals");
        
        DeclensionDimension testDim1 = new DeclensionDimension();
        DeclensionDimension testDim2 = new DeclensionDimension();
        String testVal1 = "TESTO";
        String testVal2 = "BESTO";
        
        testDim1.setValue(testVal1);
        testDim2.setValue(testVal2);
        
        assertFalse(testDim1.equals(testDim2));
    }
}
