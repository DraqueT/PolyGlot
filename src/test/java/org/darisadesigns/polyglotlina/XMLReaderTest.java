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

import TestResources.DummyCore;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.darisadesigns.polyglotlina.ManagersCollections.PropertiesManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author pe1uca
 */
public class XMLReaderTest {

    @ParameterizedTest
    @MethodSource("invalidVersionProvider")
    public void testVersionException(String caseName, String version, String partialMessage) throws IOException {
        System.out.printf("Case: %s", caseName).println();
        String testXML = """
            <dictionary>
                <PolyGlotVer>%s</PolyGlotVer>
            </dictionary>
            """.formatted(version);
        DummyCore core = DummyCore.newCore();
        
        try(InputStream inputStream = new ByteArrayInputStream(testXML.getBytes())) {
            PolyglotException exception = assertThrowsExactly(PolyglotException.class, () -> { core.loadFromXMLStream(inputStream); });

            String message = exception.getMessage();
            assertTrue(message.contains(partialMessage), "Unexpected message (%s)".formatted(message));
            assertTrue(message.contains(version));
        }
    }

    static Stream<Arguments> invalidVersionProvider() {
        return Stream.of(
            Arguments.of("Non existing version", "99.99", "Please upgrade PolyGlot"),
            Arguments.of("Older version", "0.7", "no longer supported")
        );
    }

    private Document getDocumentFromStream(InputStream stream) {
        Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(stream);
            doc.getDocumentElement().normalize();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            fail(e);
        }
        return doc;
    }

    @Test
    public void testPropertiesManagerLoad() throws IOException {
        DummyCore core = DummyCore.newCore();
        PropertiesManager manager = core.getPropertiesManager();
        
        String testXML = """
            <languageProperties>
                <fontCon>Charis SIL</fontCon>
                <fontStyle>0</fontStyle>
                <fontSize>13.0</fontSize>
                <localFontSize>12.0</localFontSize>
                <langName>Test language name</langName>
                <alphaOrder/>
                <langPropTypeMandatory>T</langPropTypeMandatory>
                <langPropLocalMandatory>F</langPropLocalMandatory>
                <langPropLocalUniqueness>T</langPropLocalUniqueness>
                <langPropWordUniqueness>F</langPropWordUniqueness>
                <langPropIgnoreCase>T</langPropIgnoreCase>
                <langPropDisableProcRegex>F</langPropDisableProcRegex>
                <langPropEnforceRTL>T</langPropEnforceRTL>
                <langPropOverrideRegexFont>F</langPropOverrideRegexFont>
                <langPropUseLocalLexicon>T</langPropUseLocalLexicon>
                <langPropAuthorCopyright/>
                <langPropLocalLangName>Local language name</langPropLocalLangName>
                <langPropUseSimplifiedConjugations>F</langPropUseSimplifiedConjugations>
                <expandedLexListDisplay>T</expandedLexListDisplay>
                <zompistCategories>V=a,e,i,u
            N=m,n
            S=p,t,k</zompistCategories>
                <zompistIllegalClusters>na
            ne</zompistIllegalClusters>
                <zompistRewriteRules>aa|a~
            ee|e~
            ii|i~
            uu|u~</zompistRewriteRules>
                <zompistSyllables>SV
            SVVN</zompistSyllables>
                <zompistDropoffRate>34</zompistDropoffRate>
                <zompistMonosyllableFrequency>61</zompistMonosyllableFrequency>
                <langPropCharRep>
                    <langPropCharRepNode>
                    <langPropCharRepCharacter>a</langPropCharRepCharacter>
                    <langPropCharRepValue>b</langPropCharRepValue>
                    </langPropCharRepNode>
                    <langPropCharRepNode>
                    <langPropCharRepCharacter>u</langPropCharRepCharacter>
                    <langPropCharRepValue>v</langPropCharRepValue>
                    </langPropCharRepNode>
                    <langPropCharRepNode>
                    <langPropCharRepCharacter>e</langPropCharRepCharacter>
                    <langPropCharRepValue>f</langPropCharRepValue>
                    </langPropCharRepNode>
                    <langPropCharRepNode>
                    <langPropCharRepCharacter>i</langPropCharRepCharacter>
                    <langPropCharRepValue>j</langPropCharRepValue>
                    </langPropCharRepNode>
                </langPropCharRep>
            </languageProperties>
            """;
        
        try(InputStream inputStream = new ByteArrayInputStream(testXML.getBytes())) {
            Document doc = getDocumentFromStream(inputStream);
            manager.loadXML(doc.getFirstChild());

            assertEquals("Charis SIL", manager.getFontConFamily());
            assertEquals(0, manager.getFontStyle());
            assertEquals(13.0, manager.getFontSize());
            assertEquals(12.0, manager.getLocalFontSize());
            assertEquals("Test language name", manager.getLangName());
            assertArrayEquals(new String[0], manager.getOrderedAlphaList());
            assertEquals(true, manager.isTypesMandatory());
            assertEquals(false, manager.isLocalMandatory());
            assertEquals(true, manager.isLocalUniqueness());
            assertEquals(false, manager.isWordUniqueness());
            assertEquals(true, manager.isIgnoreCase());
            assertEquals(false, manager.isDisableProcRegex());
            assertEquals(true, manager.isEnforceRTL());
            assertEquals(false, manager.isOverrideRegexFont());
            assertEquals(true, manager.isUseLocalWordLex());
            assertEquals("", manager.getCopyrightAuthorInfo());
            assertEquals("Local language name", manager.getLocalLangName());
            assertEquals(false, manager.isUseSimplifiedConjugations());
            assertEquals(true, manager.isExpandedLexListDisplay());
            assertEquals("V=a,e,i,u\nN=m,n\nS=p,t,k", manager.getZompistCategories());
            assertEquals("na\nne", manager.getZompistIllegalClusters());
            assertEquals("aa|a~\nee|e~\nii|i~\nuu|u~", manager.getZompistRewriteRules());
            assertEquals("SV\nSVVN", manager.getZompistSyllableTypes());
            assertEquals(34, manager.getZompistDropoffRate());
            assertEquals(61, manager.getZompistMonosylableFrequency());

            Map<String, String> characterReplacements = manager.getCharacterReplacements();
            assertEquals(4, characterReplacements.size());
            assertEquals("b", characterReplacements.get("a"));
            assertEquals("v", characterReplacements.get("u"));
            assertEquals("f", characterReplacements.get("e"));
            assertEquals("j", characterReplacements.get("i"));
        } catch (SAXException e) {
            fail(e);
        }
    }

    @Test
    public void testFullXMLLoad() {
        String fileName = PGTUtil.TESTRESOURCES + "PGDictionary.xml";
        DummyCore core = DummyCore.newCore();

        try(InputStream inputStream = new FileInputStream(fileName)) {
            core.loadFromXMLStream(inputStream);
        } catch (IOException | ParserConfigurationException | SAXException | PolyglotException e) {
            fail(e);
        }
    }
}
