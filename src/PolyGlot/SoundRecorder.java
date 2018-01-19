/*
 * Copyright (c) 2015-2018, Draque Thompson
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
package PolyGlot;

import PolyGlot.CustomControls.InfoBox;
import java.awt.Window;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JTextField;

/**
 * Basic sound recording code. Allows recording, playing, pausing. Will
 * automatically update a textbox with time, and a slider to its appropriate
 * position while playing/recording.
 *
 * @author draque
 */
public class SoundRecorder {

    private byte[] sound;
    private boolean curRecording = false;
    private boolean playing = false;
    private boolean killPlay = false;
    private ByteArrayOutputStream out;
    private Thread soundThread;
    private JSlider slider;
    private JTextField timer;
    private final AudioFormat format;
    private final int timeToDie = 100;
    private String playThread = "";
    private String recordThread = "";
    private final Window parentWindow;
    private JButton playPauseBut;
    private JButton recordBut;
    private MediaPlayer mediaPlayer;
    ImageIcon playUp;
    ImageIcon playDown;
    ImageIcon recUp;
    ImageIcon recDown;

    /**
     * Instantiates recorder with default format
     *
     * @param _parent parent window (for error communication)
     */
    public SoundRecorder(Window _parent) {
        format = getAudioFormat();
        parentWindow = _parent;
        
        // JFX used in some instances when initialized in this case
        // This ensures JFX initialized
        @SuppressWarnings("unused") 
        JFXPanel makeCertainJFXStarted = new JFXPanel();
    }

    /**
     * Set buttons to be managed by Sound Recorder
     *
     * @param _playPause play/pause button
     * @param _record record button
     * @param _playUp play button up graphic
     * @param _playDown play button down graphic
     * @param _recUp record button up graphic
     * @param _recDown record button down graphic
     */
    public void setButtons(JButton _record, JButton _playPause,
            ImageIcon _playUp, ImageIcon _playDown, ImageIcon _recUp,
            ImageIcon _recDown) {
        playPauseBut = _playPause;
        recordBut = _record;
        playUp = _playUp;
        playDown = _playDown;
        recUp = _recUp;
        recDown = _recDown;

        playPauseBut.setIcon(playUp);
        recordBut.setIcon(recUp);
    }

    /**
     * Instantiates recorder with custom format
     *
     * @param _format custom format for recorder
     * @param _parent parent window (for error communication)
     */
    public SoundRecorder(AudioFormat _format, Window _parent) {
        format = _format;
        parentWindow = _parent;
    }

    public void setTimer(JTextField _timer) {
        timer = _timer;
    }

    public void setSlider(JSlider _slider) {
        slider = _slider;
    }

    /**
     * Returns default audio format
     */
    private static AudioFormat getAudioFormat() {
        float sampleRate = 8000;
        int sampleSizeInBits = 8;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    /**
     * Returns recorded sound as byte array
     *
     * @return recorded sound, null if none
     */
    public byte[] getSound() {
        return sound;
    }

    public void setSound(byte[] _sound) {
        sound = _sound;
        playPauseBut.setIcon(_sound == null ? playUp : playDown);
    }

    /**
     * wipes prior sound (if any) and begins recording of new one
     *
     * @throws javax.sound.sampled.LineUnavailableException if no audio input
     * line
     */
    public void beginRecording() throws LineUnavailableException, Exception {
        sound = null;
        killPlay = true;
        try {
            Thread.sleep(timeToDie);
        } // max amount of time before player kills self
        catch (InterruptedException e) {
            // if it's interrupted, it's fine. The recording will end.
        }

        if (soundThread != null
                && soundThread.isAlive()
                && soundThread.toString().equals(playThread)) {
            throw new Exception("Play thread not killed.");
        } else if (soundThread != null
                && soundThread.isAlive()
                && soundThread.toString().equals(recordThread)) {
            throw new Exception("Can't begin recording while current recording active.");
        }

        timer.setText("00:00:00");
        slider.setValue(0);

        final SoundRecorder parent = this;
        final DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);

        // 8000 is the typical rate, but I wanted to get increments of 1/100 second
        int bufferSize = 80; //(int) format.getSampleRate() * format.getFrameSize();

        final byte buffer[] = new byte[bufferSize];
        out = new ByteArrayOutputStream();

        curRecording = true;
        recordBut.setIcon(recDown);

        line.open(format);
        line.start();

        resetUIValues();

        if (soundThread != null && soundThread.isAlive()) {
            soundThread.interrupt();
        }

        soundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int bytesRecorded = 0;
                float BPS = (format.getSampleRate()
                        * format.getSampleSizeInBits()) / 8;

                while (parent.isRecording()) {
                    int count = line.read(buffer, 0, buffer.length);
                    if (count > 0) {
                        out.write(buffer, 0, count);
                    }

                    bytesRecorded += buffer.length;
                    float seconds = bytesRecorded / BPS;
                    timer.setText(getTimerValue(seconds));
                }

                recordBut.setIcon(recUp);

                try {
                    out.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                    InfoBox.error("Recording wrror: ", "Unable to record: "
                            + e.getLocalizedMessage(), parentWindow);
                }
                line.close();

            }
        });

        recordThread = soundThread.toString();
        soundThread.start();
    }

    /**
     * Ends recording and writes to sound byte array
     *
     * @throws java.io.IOException on outstream close error
     */
    public void endRecording() throws IOException {
        curRecording = false;

        if (soundThread == null
                || !soundThread.isAlive()) {
            return;
        }

        // wait for recording thread to die
        try {
            Thread.sleep(timeToDie);
        } // longest time for thread to die
        catch (InterruptedException e) {/*do nothing*/
        }

        out.close();

        sound = out.toByteArray();
    }

    /**
     * Plays sound that has been recorded
     *
     * @throws javax.sound.sampled.LineUnavailableException on no output line
     * @throws java.io.IOException on no output line
     */
    @SuppressWarnings("SleepWhileHoldingLock")
    public void playPause() throws LineUnavailableException, IOException {
        // kill any recording session before initilizing playback
        if (isRecording()) {
            endRecording();
        }

        if (sound == null) {
            return;
        }

        final InputStream input = new ByteArrayInputStream(sound);
        final AudioInputStream ais = new AudioInputStream(input,
                format, sound.length / format.getFrameSize());

        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (soundThread != null) {
            String testThread = soundThread.toString();
            if (soundThread.isAlive()
                    && testThread.equals(playThread)) {
                playing = !playing;
                return;
            }
        }

        playing = true;
        killPlay = false;
        playPauseBut.setIcon(playDown);

        soundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info)) {
                    sourceLine.open(format);
                    sourceLine.start();

                    int bufferSize = 1;
                    byte[] buffer = new byte[bufferSize];
                    int totalCycles = ais.available() / bufferSize;

                    int count;
                    int cycles = 0;
                    while ((count
                            = ais.read(buffer, 0, buffer.length)) != -1) {
                        if (count > 0) {
                            while (playing == false) { // if paused, wait until unpaused
                                // suppression for this warning is nonfunctional. Very annoying.
                                Thread.sleep(timeToDie);
                                playPauseBut.setIcon(playUp);

                                if (killPlay) { // immediately ends playing process
                                    killPlay = false;
                                    playing = false;
                                    sourceLine.drain();
                                    sourceLine.close();
                                    ais.close();
                                    input.close();
                                    return;
                                }
                            }

                            playPauseBut.setIcon(playDown);

                            if (killPlay) { // immediately ends playing process
                                killPlay = false;
                                playing = false;
                                sourceLine.drain();
                                sourceLine.close();
                                ais.close();
                                input.close();
                                playPauseBut.setIcon(playUp);
                                return;
                            }

                            sourceLine.write(buffer, 0, count);

                            if (slider != null) {
                                double percentPlayed = 1.0 - (((double) (totalCycles - cycles)) / (double) totalCycles);
                                slider.setValue((int) (percentPlayed * slider.getMaximum()));
                            }

                            if (timer != null) {
                                timer.setText(getTimerValue(bufferSize, cycles));
                            }
                        }

                        cycles++;
                    }

                    if (slider != null) {
                        slider.setValue(slider.getMaximum());
                    }

                    playing = false;
                    sourceLine.drain();
                    sourceLine.close();
                    ais.close();
                    input.close();
                } catch (LineUnavailableException | IOException | InterruptedException e) {
                    //e.printStackTrace();
                    InfoBox.error("Play Error", "Unable to play audio: "
                            + e.getLocalizedMessage(), parentWindow);
                }

                playPauseBut.setIcon(playUp);
            }
        });

        playThread = soundThread.toString();
        soundThread.start();
    }

    /**
     * takes the buffer size/current cycle and returns the appropriate time
     * stamp
     *
     * @param bufferSize size of buffer being used
     * @param cycles current cycle through current play
     * @return string representation of current play/record position
     */
    private String getTimerValue(int bufferSize, int cycles) {
        float ticks = (bufferSize * cycles) / ((format.getSampleSizeInBits() * format.getSampleRate()) / 8);
        return getTimerValue(ticks);

    }

    /**
     * Given seconds, returns formatted time
     *
     * @param ticks number of seconds
     * @return formatted time
     */
    private String getTimerValue(float ticks) {
        String ret = "";

        int hundredths = (int) (ticks * 100) % 100;
        ticks -= ticks % 1;
        int seconds = (int) ticks % 60;
        ticks = (ticks - seconds) / 60;
        int minutes = (int) ticks % 60;
        // If someone makes a recording over an hour, then they don't get a nice display. XP

        ret += minutes >= 10 ? minutes : "0" + minutes;
        ret += ":";
        ret += seconds >= 10 ? seconds : "0" + seconds;
        ret += ":";
        ret += hundredths >= 10 ? hundredths : "0" + hundredths;

        return ret;
    }

    /**
     * Returns true if currently recording
     *
     * @return recording status
     */
    public boolean isRecording() {
        return curRecording;
    }

    /**
     * Returns true if currently playing
     *
     * @return playing status
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Resets values of all associated UI elements
     */
    private void resetUIValues() {
        if (slider != null) {
            slider.setValue(0);
        }

        if (timer != null) {
            timer.setText("00:00:00");
        }
    }

    /**
     * Plays MP3 file back
     *
     * @param filePath path to load MP3
     */
    public void playMP3(String filePath) {
        Media hit = new Media(parentWindow.getClass().getResource(filePath).toExternalForm());
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();
    }
}
