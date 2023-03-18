/*
 * Copyright (c) 2019-2022, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop.CustomControls;

import TestResources.DummyCore;
import org.darisadesigns.polyglotlina.PTest;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.darisadesigns.polyglotlina.Desktop.ClipboardHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.FormattedTextHelper;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ImageNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class PGrammarPaneTest extends PTest {
    
    private final DictCore core;
    private final boolean headless = GraphicsEnvironment.isHeadless();
    
    public PGrammarPaneTest() {
        
        System.out.println("PGrammarPaneTest.PGrammarPaneTest");
        core = DummyCore.newCore();
    }

    @Test
    public void testPasteRegular() {
        // do not run test in headless mode (paste explodes without UI)
        if (headless) {
            return;
        }
        
        System.out.println("PGrammarPaneTest.testPasteRegular");
        
        String sourceText = "This is a test! Yeehaw!";
        String expectedResult = sourceText;
        PGrammarPane pane = new PGrammarPane(core);
        ClipboardHandler board = new ClipboardHandler();
        
        board.setClipboardContents(sourceText);
        pane.paste();
        String resut = pane.getText();
        
        assertEquals(expectedResult, resut);
    }
    
    @Test
    public void testPasteTabSanitize() {
        // do not run test in headless mode (paste explodes without UI)
        if (headless) {
            return;
        }
        
        System.out.println("PGrammarPaneTest.testPasteTabSanitize");
        String sourceText = "This is a test!	Yeehaw!";
        String expectedResult = "This is a test!    Yeehaw!";
        PGrammarPane pane = new PGrammarPane(core);
        ClipboardHandler board = new ClipboardHandler();
        
        board.setClipboardContents(sourceText);
        pane.paste();
        String resut = pane.getText();
        
        assertEquals(expectedResult, resut);
    }
    
    @Test
    public void testProcessKeyEvent() {
        boolean success = true;
        String errorMessage = "";
        System.out.println("PGrammarPaneTest.testPasteTabSanitize");
        
        try {
            PGrammarPane pane = new PGrammarPane(core);
            KeyEvent testEvent = new KeyEvent(pane, 0, 1L, 0, KeyEvent.VK_TAB, '	');

            pane.processKeyEvent(testEvent);
        } catch (Exception e) {
            errorMessage = e.getLocalizedMessage();
            DesktopIOHandler.getInstance().writeErrorLog(e, errorMessage);
            success = false;
        }
        
        assertTrue(success, errorMessage);
    }

    @Test
    public void testAddImage() {
        boolean success = true;
        String errorMessage = "";
        String expectedResult = "<img src=\"0\">";
        PGrammarPane pane = new PGrammarPane(core);
        
        try {
            BufferedImage imageBuff = ImageIO.read(PGrammarPaneTest.class.getResource(PGTUtil.MAIN_MENU_IMAGE));
            ImageNode image = new ImageNode(core);
            image.setImageBytes(DesktopIOHandler.getInstance().loadImageBytesFromImage(imageBuff));

            pane.addImage(image);
            
            String result = FormattedTextHelper.storageFormat(pane);
            
            assertEquals(expectedResult, result);
        } catch (Exception e) {
            success = false;
            errorMessage = e.getLocalizedMessage();
            DesktopIOHandler.getInstance().writeErrorLog(e, errorMessage);
        }
        
        assertTrue(success, errorMessage);
    }
    
}
