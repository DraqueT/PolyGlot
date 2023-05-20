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
package org.darisadesigns.polyglotlina.DomParser;

import TestResources.DummyCore;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draquethompson
 */
public class PDomParserTest {

    private static final String TEMP_FILE = PGTUtil.TESTRESOURCES + "test_equality_TEMP.pgd";

    public PDomParserTest() {
    }

    @Test
    public void testReadSaveIntegrityDeep() {
        System.out.println("PDomParserTest.testReadSaveIntegrityDeep");
        
        File recurseRead = new File(PGTUtil.TESTRESOURCES);
        recurseRead(recurseRead);
    }

    private void recurseRead(File curFile) {
        // do not run on currupted files: they fundamentally lack integrity
        if (curFile.getName().equals("corrupted")) {
            return;
        }
        
        if (curFile.isDirectory()) {
            for (File child : curFile.listFiles()) {
                recurseRead(child);
            }
        } else if (curFile.getAbsolutePath().endsWith("pgd")) {
            System.out.println("\t" + curFile.getName());
            DictCore dom = DummyCore.newCore();
            DictCore reload = DummyCore.newCore();

            try {
                dom.readFile(curFile.getAbsolutePath());
                dom.writeFile(TEMP_FILE, true);
                reload.readFile(TEMP_FILE);
                assertEquals(reload, dom);
            } catch (IOException | IllegalStateException | ParserConfigurationException | TransformerException e) {
                System.out.println(e.getLocalizedMessage());
                fail(e);
            } finally {
                File temp = new File(TEMP_FILE);
                
                if (temp.exists()) {
                    temp.delete();
                }
            }
        }
    }

}
