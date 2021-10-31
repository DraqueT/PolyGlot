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

import TestResources.DummyCore;
import TestResources.TestResources;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class DeclensionNodeTest {
    private final ConjugationNode testNode;
    private final DictCore core;
    
    public DeclensionNodeTest() {
        core = DummyCore.newCore();
        testNode = new ConjugationNode(1, core.getConjugationManager());
        
        testNode.setCombinedDimId("combId");
        testNode.setDimensionless(false);
        testNode.setNotes("notes");
        testNode.setValue("value");
    }
    
    @Test
    public void testEquals() {
        System.out.println("DeclensionNodeTest.testEquals");
        
        ConjugationNode compNode = new ConjugationNode(1, core.getConjugationManager());
        
        compNode.setCombinedDimId("combId");
        compNode.setDimensionless(false);
        compNode.setNotes("notes");
        compNode.setValue("value");
        
        assertTrue(compNode.equals(testNode));
    }
    
    @Test
    public void testNotEquals() {
        System.out.println("DeclensionNodeTest.testNotEquals");
        
        ConjugationNode compNode = new ConjugationNode(1, core.getConjugationManager());
        
        compNode.setCombinedDimId("combId");
        compNode.setDimensionless(false);
        compNode.setNotes("notes");
        compNode.setValue("NOTVALUE");
        
        assertFalse(compNode.equals(testNode));
    }

    @Test
    public void testInsertBuffer() {
        System.out.println("DeclensionNodeTest.testInsertBuffer");
        
        int expectedSize = 1;
        ConjugationDimension dim = new ConjugationDimension();
        dim.setValue("zot");
        
        testNode.getBuffer().setEqual(dim);
        testNode.getBuffer().setId(0);
        
        try {
            testNode.insertBuffer();

            Object[] dimensions = testNode.getDimensions().toArray();
            ConjugationDimension resultDim = (ConjugationDimension)dimensions[0];

            assertEquals(expectedSize, dimensions.length);
            assertEquals(dim, resultDim);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testInsertBufferBadId() {
        System.out.println("DeclensionNodeTest.testInsertBufferBadId");
        
        String expectedMessage = "Dimension with ID -1 cannot be inserted.";
        ConjugationDimension dim = new ConjugationDimension();
        dim.setValue("zot");
        
        testNode.getBuffer().setEqual(dim);
        
        Throwable t = assertThrows(Exception.class, () -> {
            testNode.insertBuffer();
        });

        assertEquals(expectedMessage, t.getLocalizedMessage());
    }
    
    @Test
    public void testClearBuffer() {
        System.out.println("DeclensionNodeTest.testClearBuffer");
        
        ConjugationDimension dim = new ConjugationDimension();
        dim.setValue("zot");
        
        testNode.getBuffer().setEqual(dim);
        testNode.clearBuffer();
        
        assertEquals("", testNode.getBuffer().value);
    }

    @Test
    public void testAddDimension() {
        System.out.println("DeclensionNodeTest.testAddDimension");
        
        int expectedSize = 1;
        ConjugationDimension dim = new ConjugationDimension();
        dim.setValue("zot");
        
        testNode.addDimension(dim);
        
        Object[] dimensions = testNode.getDimensions().toArray();
        ConjugationDimension resultDim = (ConjugationDimension)dimensions[0];
        
        assertEquals(expectedSize, dimensions.length);
        assertEquals(dim, resultDim);
    }

    @Test
    public void testDeleteDimension() {
        System.out.println("DeclensionNodeTest.testDeleteDimension");
        
        int dimId = 0;
        ConjugationDimension dim = new ConjugationDimension();
        dim.setValue("zot");
        
        testNode.getBuffer().setEqual(dim);
        testNode.getBuffer().setId(dimId);
        
        try {
            testNode.insertBuffer();
            testNode.deleteDimension(dimId);

            assertEquals(0, testNode.getDimensions().size());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testWriteXMLTemplate() {
        System.out.println("DeclensionNodeTest.testWriteXMLTemplate");
        
        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                +"<dictionary>"
                + "<declensionNode>"
                + "<declensionId>1</declensionId>" 
                + "<declensionText>value</declensionText>"
                + "<declensionNotes>notes</declensionNotes>"
                + "<declensionTemplate>1</declensionTemplate>"
                + "<declensionRelatedId>1</declensionRelatedId>"
                + "<declensionDimensionless>F</declensionDimensionless>"
                + "<dimensionNode>"
                + "<dimensionId>0</dimensionId>"
                + "<dimensionName>zot</dimensionName>"
                + "</dimensionNode></declensionNode></dictionary>";
        int relatedId = 1;
        ConjugationDimension dim = new ConjugationDimension();
        dim.setValue("zot");
        
        testNode.getBuffer().setEqual(dim);
        testNode.getBuffer().setId(0);
        
        try {
            testNode.insertBuffer();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(PGTUtil.DICTIONARY_XID);
            doc.appendChild(rootElement);

            testNode.writeXMLTemplate(doc, rootElement, relatedId);
            
            assertTrue(TestResources.textXmlDocEquals(doc, expectedXml));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testWriteXMLWordDeclension() {
        System.out.println("DeclensionNodeTest.testWriteXMLWordDeclension");
        
        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<dictionary>"
                + "<declensionNode>"
                + "<declensionId>1</declensionId>"
                + "<declensionText>value</declensionText>"
                + "<declensionNotes>notes</declensionNotes>"
                + "<declensionTemplate>1</declensionTemplate>"
                + "<declensionRelatedId>1</declensionRelatedId>"
                + "<declensionDimensionless>F</declensionDimensionless>"
                + "<dimensionNode>"
                + "<dimensionId>0</dimensionId>"
                + "<dimensionName>zot</dimensionName>"
                + "</dimensionNode></declensionNode></dictionary>";
        int relatedId = 1;
        ConjugationDimension dim = new ConjugationDimension();
        dim.setValue("zot");
        
        testNode.getBuffer().setEqual(dim);
        testNode.getBuffer().setId(0);
        
        try {
            testNode.insertBuffer();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(PGTUtil.DICTIONARY_XID);
            doc.appendChild(rootElement);

            testNode.writeXMLTemplate(doc, rootElement, relatedId);
            
            assertTrue(TestResources.textXmlDocEquals(doc, expectedXml));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testSetEqual() {
        System.out.println("DeclensionNodeTest.testSetEqual");
        
        ConjugationNode node = new ConjugationNode(0, core.getConjugationManager());
        node.setEqual(testNode);
        assertTrue(node.equals(testNode));
    }
    
    @Test
    public void testSetEqualWrongType() {
        System.out.println("DeclensionNodeTest.testSetEqual");
        
        String expectedMessage = "Object not of type DeclensionNode";
        
        Exception exception = assertThrows(ClassCastException.class, () -> {
            ConjugationNode node = new ConjugationNode(0, core.getConjugationManager());
            node.setEqual(new ConWord());
        });

        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage,actualMessage);
    }
    
    @Test
    public void testGetDeclensionDimensionByIdExists() {
        System.out.println("DeclensionNodeTest.testSetEqual");
        
        String expectedValue = "TESTO DEBESTO";
        int dimId = 2;
        
        ConjugationNode node = new ConjugationNode(0, core.getConjugationManager());
        ConjugationDimension dim = new ConjugationDimension(dimId);
        dim.setValue(expectedValue);
        node.addDimension(dim);
        
        String result = node.getConjugationDimensionById(dimId).getValue();
        
        assertEquals(expectedValue, result);
    }
    
    @Test
    public void testGetDeclensionDimensionByIdNotExists() {
        System.out.println("DeclensionNodeTest.testSetEqual");
        
        ConjugationNode node = new ConjugationNode(0, core.getConjugationManager());
        assertNull(node.getConjugationDimensionById(1));
    }
    
    @Test
    public void testHashCode() {
        // just make sure it doesn't explode.
        testNode.hashCode();
    }
}
