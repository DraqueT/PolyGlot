/*
 * Copyright (c) 2020-2022, Draque Thompson, draquemail@gmail.com
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

import java.util.Map;
import java.util.stream.Stream;
import org.darisadesigns.polyglotlina.XMLRecoveryTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Edgar
 */
public class XMLRecoveryToolTest {

    @ParameterizedTest
    @MethodSource("corruptedXMLProvider")
    public void testRecoverXml(String caseName, String corrupted, String expected) {
        System.out.printf("XMLRecoveryToolTest.testRecoverXml (%s)", caseName).println();
        XMLRecoveryTool tool = new XMLRecoveryTool(corrupted);
        String recover = tool.recoverXml();
        
        assertEquals(expected, recover);
    }
    
    static Stream<Arguments> corruptedXMLProvider() {
        return Stream.of(
            Arguments.of(
                "Missing definition close tag",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <dictionary>
                    <PolyGlotVer>3.6</PolyGlotVer>
                    <lexicon>
                        <word>
                            <wordId>2</wordId>
                            <pronunciation/>
                            <definition>definition
                            <wordProcOverride>F</wordProcOverride>
                            <autoDeclOverride>F</autoDeclOverride>
                            <wordRuleOverride>F</wordRuleOverride>
                        </word>
                    </lexicon>
                </dictionary>
                """,
                """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <dictionary>
                    <PolyGlotVer>3.6</PolyGlotVer>
                    <lexicon>
                        <word>
                            <wordId>2</wordId>
                            <pronunciation/>
                            <definition>definition
                            <wordProcOverride>F</wordProcOverride>
                            <autoDeclOverride>F</autoDeclOverride>
                            <wordRuleOverride>F</wordRuleOverride>
                        </definition></word>
                    </lexicon>
                </dictionary>
                """
            ),
            Arguments.of(
                "Missing definition open tag",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <dictionary>
                    <PolyGlotVer>3.6</PolyGlotVer>
                    <lexicon>
                        <word>
                            <wordId>2</wordId>
                            <pronunciation/>
                            definition</definition>
                            <wordProcOverride>F</wordProcOverride>
                            <autoDeclOverride>F</autoDeclOverride>
                            <wordRuleOverride>F</wordRuleOverride>
                        </word>
                    </lexicon>
                </dictionary>
                """,
                """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <dictionary>
                    <PolyGlotVer>3.6</PolyGlotVer>
                    <lexicon>
                        <word>
                            <wordId>2</wordId>
                            <pronunciation/>
                            definition
                            <wordProcOverride>F</wordProcOverride>
                            <autoDeclOverride>F</autoDeclOverride>
                            <wordRuleOverride>F</wordRuleOverride>
                        </word>
                    </lexicon>
                </dictionary>
                """
            )
        );
    }
}
