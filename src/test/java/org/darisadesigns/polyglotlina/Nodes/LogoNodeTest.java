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
public class LogoNodeTest {
    private final LogoNode logoTest;
    private final LogoNode radical;
    
    public LogoNodeTest() {
        System.out.println("LogoNodeTest");
        logoTest = new LogoNode();
        radical = new LogoNode();
        
        radical.setValue("RADICAL!");
        radical.setRadical(true);
        
        logoTest.setValue("VALUE");
        logoTest.setNotes("NOTES");
        logoTest.addRadical(radical);
        logoTest.addReading("REEDING");
        logoTest.setStrokes(3);
    }
    
    @Test
    public void testAddRadical() {
        String expectedReading = "ZIMZAM!";
        System.out.println("LogoNodeTest:testAddRadical");
        LogoNode newRad = new LogoNode();
        newRad.setValue(expectedReading);
        logoTest.addRadical(newRad);
        
        assertTrue(logoTest.containsRadicalString(expectedReading, true));
    }

    @Test
    public void testContainsReading() {
        System.out.println("LogoNodeTest:testContainsReading");
        assertTrue(logoTest.containsReading("REEDING", true));
    }
    
    @Test
    public void testNotContainsReading() {
        System.out.println("LogoNodeTest:testContainsReading");
        assertFalse(logoTest.containsReading("I say good sir, are you literate?", true));
    }

    @Test
    public void testContainsRadicalString() {
        System.out.println("LogoNodeTest:testContainsRadicalString");
        
        assertTrue(logoTest.containsRadicalString(radical.value, false));
    }

    @Test
    public void testSetEqual() {
        System.out.println("LogoNodeTest:testSetEqual");
        
        LogoNode copy = new LogoNode();
        copy.setEqual(logoTest);
        assertEquals(copy, logoTest);
    }

    @Test
    public void testWriteXML() {
        System.out.println("LogoNodeTest:testWriteXML");
        
        String expectedValue = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<dictionary>"
                + "<LogoGraphNode>"
                + "<logoGraphId>0</logoGraphId>"
                + "<logoGraphValue>VALUE</logoGraphValue>"
                + "<logoIsRadical>F</logoIsRadical>"
                + "<logoNotes>NOTES</logoNotes>"
                + "<logoRadicalList>0</logoRadicalList>"
                + "<logoStrokes>3</logoStrokes>"
                + "<logoReading>REEDING</logoReading>"
                + "</LogoGraphNode></dictionary>";
        System.out.println("DeclensionDimenstion:testWriteXML");
        
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(PGTUtil.DICTIONARY_XID);
            doc.appendChild(rootElement);
            
            logoTest.writeXML(doc, rootElement);
        
            assertTrue(TestResources.textXmlDocEquals(doc, expectedValue));
        } catch (IOException | ParserConfigurationException | TransformerException | DOMException e) {
            fail(e);
        }
    }

    @Test
    public void testEquals() {
        System.out.println("LogoNodeTest:testEquals");
        LogoNode comp = new LogoNode();
        
        comp.setValue("VALUE");
        comp.setNotes("NOTES");
        comp.addRadical(radical);
        comp.addReading("REEDING");
        comp.setStrokes(3);
        
        assertEquals(comp, logoTest);
    }
    
    @Test
    public void testNotEquals() {
        System.out.println("LogoNodeTest:testNotEquals");
        
        LogoNode comp = new LogoNode();
        
        comp.setValue("VALUE");
        comp.setNotes("NOTES");
        comp.addRadical(radical);
        comp.addReading("REEDING");
        comp.addReading("BLEEDING?!");
        comp.setStrokes(3);
        
        assertNotEquals(comp, logoTest);
    }
}
