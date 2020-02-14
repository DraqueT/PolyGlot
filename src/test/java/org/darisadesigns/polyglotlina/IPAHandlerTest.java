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
package org.darisadesigns.polyglotlina;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class IPAHandlerTest {

    @Test
    public void testPlaySounds() {
        // do not test in headless environment (hangs on 100% CPU consumption)
        if (!GraphicsEnvironment.isHeadless()) {
            System.out.println("IPAHandlerTest.testPlaySounds");
            IPAHandler handler = new IPAHandler(null);

            try {
                Class<?> myClass = handler.getClass();
                Field field = myClass.getDeclaredField("charMap");
                field.setAccessible(true);
                Map<String, String> charMap = (Map<String, String>)field.get(handler);

                field = myClass.getDeclaredField("soundRecorder");
                field.setAccessible(true);
                SoundRecorder soundRecorder = (SoundRecorder)field.get(handler);

                // only test playing the first sounds of each library (just test the rest exist)
                String firstSound = (String)charMap.values().toArray()[0];
            
                soundRecorder.playAudioFile(PGTUtil.IPA_SOUNDS_LOCATION + PGTUtil.UCLA_WAV_LOCATION + firstSound + PGTUtil.WAV_SUFFIX);
                soundRecorder.playAudioFile(PGTUtil.IPA_SOUNDS_LOCATION + PGTUtil.WIKI_WAV_LOCATION + firstSound + PGTUtil.WAV_SUFFIX);
            } catch (Exception e) {
                IOHandler.writeErrorLog(e, e.getLocalizedMessage());
                fail(e);
            }
        } else {
            System.out.println("HEADLESS SKIP: test sound playback");
        }
    }
    
    @Test
    public void testAllSoundsExist() {
        System.out.println("IPAHandlerTest.testAllSoundsExist");
        
        IPAHandler handler = new IPAHandler(null);
        
        try {
            Class<?> classs = handler.getClass();
            Field field = classs.getDeclaredField("charMap");
            field.setAccessible(true);
            Map<String, String> charMap = (Map<String, String>)field.get(handler);

            field = classs.getDeclaredField("soundRecorder");
            field.setAccessible(true);

            String results = "";
            String expectedResults = "";

            for (String soundName : charMap.values()) {
                String sound = "";
                try {
                    System.out.println(PGTUtil.UCLA_WAV_LOCATION + soundName + PGTUtil.WAV_SUFFIX);
                    sound = PGTUtil.IPA_SOUNDS_LOCATION + PGTUtil.UCLA_WAV_LOCATION + soundName + PGTUtil.WAV_SUFFIX;
                    assertNotEquals(sound, null);
                } catch (Exception e) {
                    System.out.println("FAILED: " + sound);
                    results += sound + e.getLocalizedMessage() + "\n";
                }
            }

            for (String soundName : charMap.values()) {
                String sound = "";
                try {
                    System.out.println(PGTUtil.WIKI_WAV_LOCATION + soundName + PGTUtil.WAV_SUFFIX);
                    sound = PGTUtil.IPA_SOUNDS_LOCATION + PGTUtil.WIKI_WAV_LOCATION + soundName + PGTUtil.WAV_SUFFIX;
                    assertNotEquals(sound, null);
                } catch (Exception e) {
                    System.out.println("FAILED: " + sound);
                    results += sound + e.getLocalizedMessage() + "\n";
                }
            }

            assertEquals(expectedResults, results);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            IOHandler.writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testPlayProcUcla() {
        System.out.println("IPAHandlerTest.testPlayProcUcla");
        
        IPAHandler handler = new IPAHandler(null);
        
        try {
            handler.playChar("a", IPAHandler.IPALibrary.UCLA_IPA);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testPlayProcWiki() {
        System.out.println("IPAHandlerTest.testPlayProcWiki");
        
        IPAHandler handler = new IPAHandler(null);
        
        try {
            handler.playChar("a", IPAHandler.IPALibrary.WIKI_IPA);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testyPlayProcBadChar() {
        System.out.println("IPAHandlerTest.testyPlayProcBadChar");

        String playChar = "¯\\_(ツ)_/¯";
        String expectedMessage = "Unable to find character " + playChar + " in pronunciations.";
        IPAHandler handler = new IPAHandler(null);
        
        Exception exception = assertThrows(Exception.class, () -> {
            handler.playChar(playChar, IPAHandler.IPALibrary.WIKI_IPA);
        });

        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}
