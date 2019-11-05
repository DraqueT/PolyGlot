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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
    private final DeclensionNode testNode;
    
    public DeclensionNodeTest() {
        System.out.print("DeclensionNodeTest");
        testNode = new DeclensionNode(1);
        
        testNode.setCombinedDimId("combId");
        testNode.setDimensionless(false);
        testNode.setNotes("notes");
        testNode.setValue("value");
    }
    
    @Test
    public void testEquals() {
        System.out.print("DeclensionNodeTest:testEquals");
        DeclensionNode compNode = new DeclensionNode(1);
        
        compNode.setCombinedDimId("combId");
        compNode.setDimensionless(false);
        compNode.setNotes("notes");
        compNode.setValue("value");
        
        assertTrue(compNode.equals(testNode));
    }
    
    @Test
    public void testNotEquals() {
        System.out.print("DeclensionNodeTest:testNotEquals");
        DeclensionNode compNode = new DeclensionNode(1);
        
        compNode.setCombinedDimId("combId");
        compNode.setDimensionless(false);
        compNode.setNotes("notes");
        compNode.setValue("NOTVALUE");
        
        assertFalse(compNode.equals(testNode));
    }

    @Test
    public void testInsertBuffer() {
        System.out.print("DeclensionNodeTest:testInsertBuffer");
        int expectedSize = 1;
        DeclensionDimension dim = new DeclensionDimension();
        dim.setValue("zot");
        
        testNode.getBuffer().setEqual(dim);
        testNode.getBuffer().setId(0);
        
        try {
            testNode.insertBuffer();

            Object[] dimensions = testNode.getDimensions().toArray();
            DeclensionDimension resultDim = (DeclensionDimension)dimensions[0];

            assertEquals(expectedSize, dimensions.length);
            assertEquals(dim, resultDim);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testInsertBufferBadId() {
        System.out.print("DeclensionNodeTest:testInsertBufferBadId");
        String expectedMessage = "Dimension with ID -1 cannot be inserted.";
        DeclensionDimension dim = new DeclensionDimension();
        dim.setValue("zot");
        
        testNode.getBuffer().setEqual(dim);
        
        Throwable t = assertThrows(Exception.class, () -> {
            testNode.insertBuffer();
        });

        assertEquals(expectedMessage, t.getLocalizedMessage());
    }
    
    @Test
    public void testClearBuffer() {
        System.out.print("DeclensionNodeTest:testClearBuffer");
        DeclensionDimension dim = new DeclensionDimension();
        dim.setValue("zot");
        
        testNode.getBuffer().setEqual(dim);
        testNode.clearBuffer();
        
        assertEquals("", testNode.getBuffer().value);
    }

    @Test
    public void testAddDimension() {
        System.out.print("DeclensionNodeTest:testAddDimension");
        int expectedSize = 1;
        DeclensionDimension dim = new DeclensionDimension();
        dim.setValue("zot");
        
        testNode.addDimension(dim);
        
        Object[] dimensions = testNode.getDimensions().toArray();
        DeclensionDimension resultDim = (DeclensionDimension)dimensions[0];
        
        assertEquals(expectedSize, dimensions.length);
        assertEquals(dim, resultDim);
    }

    @Test
    public void testDeleteDimension() {
        System.out.print("DeclensionNodeTest:testDeleteDimension");
        int dimId = 0;
        DeclensionDimension dim = new DeclensionDimension();
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
        System.out.print("DeclensionNodeTest:testWriteXMLTemplate");
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
        DeclensionDimension dim = new DeclensionDimension();
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
        System.out.print("DeclensionNodeTest:testWriteXMLWordDeclension");
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
        DeclensionDimension dim = new DeclensionDimension();
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
        System.out.print("DeclensionNodeTest:testSetEqual");
        DeclensionNode node = new DeclensionNode(0);
        node.setEqual(testNode);
        assertTrue(node.equals(testNode));
    }
}
