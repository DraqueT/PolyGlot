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
package org.darisadesigns.polyglotlina;

import org.darisadesigns.polyglotlina.Desktop.SoundRecorder;
import java.awt.GraphicsEnvironment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;

/**
 *
 * @author draque
 */
public class SoundRecorderTest {
    
    public SoundRecorderTest() {
    }

    @Test
    public void testSoundRecordingSuite() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        
        System.out.println("SoundRecorderTest.testSoundRecordingSuite");
        
        ImageIcon playButtonUp = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.PLAY_BUTTON_UP)));
        ImageIcon playButtonDown = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.PLAY_BUTTON_DOWN)));
        ImageIcon recordButtonUp = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.RECORD_BUTTON_UP)));
        ImageIcon recordButtonDown = PGTUtil.getButtonSizeIcon(new ImageIcon(getClass().getResource(PGTUtil.RECORD_BUTTON_DOWN)));
        
        try {
            SoundRecorder recorder = new SoundRecorder(null);
            recorder.setButtons(new JButton(), new JButton(), playButtonUp, playButtonDown, recordButtonUp, recordButtonDown);
            recorder.setTimer(new JTextField());
            recorder.setSlider(new JSlider());
            recorder.beginRecording();
            Thread.sleep(1000);
            recorder.endRecording();
            byte[] sound = recorder.getSound();
            recorder.setSound(sound);
            recorder.playPause();
            assertTrue(recorder.isPlaying());
        } catch (Exception e) {
            fail(e);
        } 
    }
}
