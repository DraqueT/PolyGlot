/*
 * Copyright (c) 2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.CustomControls;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.KeyPressInfo;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author draque
 */
public class TestPFilterComboBox {

    private FrameFixture window;
    private final GenericTypeMatcher comboMatcher;
    private final GenericTypeMatcher buttonMatcher;
    private final List<String> testVals = Arrays.asList(" ", "TEST_0", "TEST_1", "TEST_2", "TEST_3", "TEST_4");

    @BeforeEach
    public void setUp() {
        TestResources.TestPFilterComboBox testComboBox = GuiActionRunner.execute(() -> new TestResources.TestPFilterComboBox(testVals));
        window = new FrameFixture(testComboBox);
        window.show();
    }

    @AfterEach
    public void tearDown() {
        window.cleanUp();
    }

    public TestPFilterComboBox() {
        Assumptions.assumeTrue(!GraphicsEnvironment.isHeadless());
        
        comboMatcher = new GenericTypeMatcher<PFilterComboBox>(PFilterComboBox.class){
            @Override
            protected boolean isMatching(PFilterComboBox t) {
                return t.getClass() == PFilterComboBox.class;
            }
        };
        
         buttonMatcher= new GenericTypeMatcher<JButton>(JButton.class) {
            @Override
            protected boolean isMatching(JButton t) {
                return t.getClass() == JButton.class;
            }
        };
    }

//    @Test
//    public void testInitialValue() {
//        System.out.println("PComboBoxTest.testInitialValue");
//        JComboBoxFixture comp = window.comboBox(comboMatcher);
//        String[] contents = comp.contents();
//        
//        assertEquals(testVals.size(), contents.length);
//        for (int i = 0; i < contents.length; i++) {
//            assertEquals(testVals.get(i), contents[i]);
//        }
//    }
//    
//    @Test
//    public void testBadValOnDeselection() {
//        System.out.println("PComboBoxTest.testBadValOnDeselection");
//        
//        try {
//            Robot r = new Robot();
//
//            JComboBoxFixture comp = window.comboBox(comboMatcher);
//            JButtonFixture altSelect = window.button(buttonMatcher);
//
//            comp.target().requestFocus();
//
//            r.setAutoDelay(900);
//            r.keyPress(KeyEvent.VK_X);
//            r.keyPress(KeyEvent.VK_T);
//            r.keyPress(KeyEvent.VK_E);
//            r.keyPress(KeyEvent.VK_S);
//
//            altSelect.target().requestFocus();
//            
//            String resultVal = comp.selectedItem();
//
//            assertTrue(resultVal != null && resultVal.isBlank());
//        } catch(AWTException e) {
//            fail(e);
//        }
//    }
    
    @Test
    public void testGoodValOnDeselection() {
        System.out.println("PComboBoxTest.testBadValOnDeselection");
        
        String expectedValue = "TEST_0";
        
        try {
            Robot r = new Robot();

            JComboBoxFixture comp = window.comboBox(comboMatcher);
            JButtonFixture altSelect = window.button(buttonMatcher);

            comp.click();
            comp.pressAndReleaseKey(KeyPressInfo.keyCode('T'));
            comp.pressAndReleaseKey(KeyPressInfo.keyCode('E'));
            comp.pressAndReleaseKey(KeyPressInfo.keyCode('S'));
            comp.enterText("TES");
            comp.replaceText("TES");
            comp.target().requestFocus();
            comp.target().getEditor().getEditorComponent().requestFocus();

//            r.setAutoDelay(900);
//            Point location = comp.target().getLocationOnScreen();
//            r.mouseMove(location.x + 1, location.y + 1);
//            r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
//            r.keyPress(KeyEvent.VK_T);
//            r.keyRelease(KeyEvent.VK_T);
//            r.keyPress(KeyEvent.VK_E);
//            r.keyRelease(KeyEvent.VK_E);
//            r.keyPress(KeyEvent.VK_S);
//            r.keyRelease(KeyEvent.VK_S);

            altSelect.target().requestFocus();
            
            String resultVal = comp.selectedItem();

            assertEquals(expectedValue, resultVal);
        } catch(AWTException e) {
            fail(e);
        }
    }
}