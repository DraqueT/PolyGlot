/*
 * Copyright (c) 2021, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop;

import java.awt.Window;
import org.darisadesigns.polyglotlina.SoundRecorder;

/**
 *
 * @author edga_
 */
public class IPAHandler extends org.darisadesigns.polyglotlina.IPAHandler {
    
    private final SoundRecorder soundRecorder;
    
    public IPAHandler(Window _parent) {
        super();
        soundRecorder = new SoundRecorder(_parent);
    }
    
    public String playVowelGetChar(int x, int y, IPALibrary ipaLibrary) throws Exception {
        String ret = getVowelChar(x, y);
        playChar(ret, ipaLibrary);

        return ret;
    }
    
    public String getVowelChar(int x, int y) {
        String ret = "";

        if (y > 67 && y < 102) {
            if (x > 72 && x < 89) {
                ret = "i";
            } else if (x > 101 && x < 118) {
                ret = "y";
            } else if (x > 296 && x < 313) {
                ret = "ɨ";
            } else if (x > 318 && x < 342) {
                ret = "ʉ";
            } else if (x > 513 && x < 544) {
                ret = "ɯ";
            } else if (x > 549 && x < 574) {
                ret = "u";
            }
        } else if (y > 112 && y < 148) {
            if (x > 178 && x < 196) {
                ret = "ɪ";
            } else if (x > 202 && x < 225) {
                ret = "ʏ";
            } else  if (x > 458 && x < 482) {
                ret = "ʊ";
            }
        } else if (y > 160 && y < 196) {
            if (x > 147 && x < 171) {
                ret = "e";
            } else if (x > 176 && x < 200) {
                ret = "ø";
            } else  if (x > 332 && x < 356) {
                ret = "ɘ";
            } else if (x > 361 && x < 386) {
                ret = "ɵ";
            } else if (x > 517 && x < 540) {
                ret = "ɤ";
            } else  if (x > 546 && x < 569) {
                ret = "o";
            }
        } else if (y > 205 && y < 242) {
            if (x > 363 && x < 388) {
                ret = "ə";
            }
        } else if (y > 250 && y < 286) {
            if (x > 216 && x < 239) {
                ret = "ɛ";
            } else if (x > 244 && x < 276) {
                ret = "œ";
            } else  if (x > 371 && x < 393) {
                ret = "ɜ";
            } else if (x > 399 && x < 424) {
                ret = "ɞ";
            } else if (x > 518 && x < 541) {
                ret = "ʌ";
            } else  if (x > 546 && x < 569) {
                ret = "ɔ";
            }
        } else if (y > 308 && y < 343 && x > 247 && x < 279) {
            ret = "æ";
        } else if (y > 287 && y < 324 && x > 396 && x < 420) {
            ret = "ɐ";
        } else if (y > 343 && y < 379) {
            if (x > 290 && x < 314) {
                ret = "a";
            } else if (x > 319 && x < 348) {
                ret = "ɶ";
            } else if (x > 516 && x < 540) {
                ret = "ɑ";
            } else if (x > 546 && x < 570) {
                ret = "ɒ";
            }
        }

        return ret;
    }

    public String playPulConsGetChar(int x, int y, IPALibrary ipaLibrary) throws Exception {
        String ret = getPulConsChar(x, y);
        playChar(ret, ipaLibrary);

        return ret;
    }
    
    public String getPulConsChar(int x, int y) {
        String ret;
        int col = ((x - 182) / 28) + 1;
        int row = ((y - 80) / 29) + 1;
        
        if (col < 1 || col > 22 || row < 1 || row > 8) {
            ret = "";
        } else {
            ret = pulmonicTable[col][row];
            
            if (ret == null) {
                ret = "";
            }
        }

        return ret;
    }

    public String playNonPulConsGetChar(int x, int y, IPALibrary ipaLibrary) throws Exception {
        String ret = getNonPulConsChar(x, y);
        playChar(ret, ipaLibrary);

        return ret;
    }
    
    public String getNonPulConsChar(int x, int y) {
        String ret = "*";

        if (y < 66 || y > 226 || x < 12 || x > 530 || (x > 353 && y > 194)) {
            ret = "";
        } else {
            int col = ((x - 12) / 172) + 1;
            int row = ((y - 66) / 32) + 1;
            switch (col) {
                case 1:
                    switch (row) {
                        case 1:
                            ret = "ʘ";
                            break;
                        case 2:
                            ret = "ǀ";
                            break;
                        case 3:
                            ret = "ǃ";
                            break;
                        case 4:
                            ret = "ǂ";
                            break;
                        case 5:
                            ret = "ǁ";
                            break;
                    }
                    break;
                case 2:
                    switch (row) {
                        case 1:
                            ret = "ɓ";
                            break;
                        case 2:
                            ret = "ɗ";
                            break;
                        case 3:
                            ret = "ʄ";
                            break;
                        case 4:
                            ret = "ɠ";
                            break;
                        case 5:
                            ret = "ʛ";
                            break;
                    }
                    break;
                case 3:
                    switch (row) {
                        case 1:
                            ret = "p'";
                            break;
                        case 2:
                            ret = "t'";
                            break;
                        case 3:
                            ret = "k'";
                            break;
                        case 4:
                            ret = "s'";
                            break;
                    }
                    break;
            }
        }

        return ret;
    }

    /**
     * 
     * @param x
     * @param y
     * @param ipaLibrary
     * @return character mapped to location
     * @throws Exception on any playback error, unknown IPA character, unknown IPA sound library
     */
    public String playOtherGetChar(int x, int y, IPALibrary ipaLibrary) throws Exception {
        String ret = getOtherChar(x, y);
        playChar(ret, ipaLibrary);

        return ret;
    }
    
    public String getOtherChar(int x, int y) {
        String ret = "*";

        // returns blank if in a non-viable position
        if (x < 15 || x > 631 || y < 45 || y > 365 || (x > 313 && x < 332)
                || (x > 332 && y > 301)) {
            ret = "";
        } else {
            int row = ((y - 45) / 32) + 1;
            int col = x > 313 ? 2 : 1;

            if (col == 1) {
                switch (row) {
                    case 1:
                        ret = "ʍ";
                        break;
                    case 2:
                        ret = "w";
                        break;
                    case 3:
                        ret = "ɥ";
                        break;
                    case 4:
                        ret = "ʜ";
                        break;
                    case 5:
                        ret = "ʢ";
                        break;
                    case 6:
                        ret = "ʡ";
                        break;
                    case 7:
                        ret = "ɕ";
                        break;
                    case 8:
                        ret = "ʑ";
                        break;
                    case 9:
                        ret = "ɺ";
                        break;
                    case 10:
                        ret = "ɧ";
                        break;
                }
            } else {
                switch (row) {
                    case 1://
                        ret = "t͡s";
                        break;
                    case 2:
                        ret = "t͡ʃ";
                        break;
                    case 3:
                        ret = "t͡ɕ";
                        break;
                    case 4:
                        ret = "t͡ʂ";
                        break;
                    case 5:
                        ret = "d͡z";
                        break;
                    case 6:
                        ret = "d͡ʒ";
                        break;
                    case 7:
                        ret = "d͡ʑ";
                        break;
                    case 8:
                        ret = "d͡ʐ";
                        break;
                }
            }
        }

        return ret;
    }
    
    /**
     * Plays sound of IPA pronunciation based on Stringacter
     *
     * @param c IPA character to play
     * @param ipaLibrary library to use to play sound
     * @throws Exception on any playback error, unknown IPA character, unknown IPA sound library
     */
    public void playChar(String c, IPALibrary ipaLibrary) throws Exception {
        if (c.isEmpty()) {
            return;
        }

        if (charMap.containsKey(c)) {
            playProc(charMap.get(c), ipaLibrary);
        } else {
            throw new Exception("Unable to find character " + c
                    + " in pronunciations.");
        }
    }
    
    /**
     * Plays the sound associated with an IPA letter
     * @param _soundName IPA character associated with sound
     * @param ipaLibrary library to use to play sound
     * @throws Exception on any playback error, unknown IPA character, unknown IPA sound library
     */
    private void playProc(String _soundName, IPALibrary ipaLibrary) throws Exception {
        String soundName = _soundName;
        
        switch (ipaLibrary) {
            case UCLA_IPA:
                soundName = PGTUtil.UCLA_WAV_LOCATION + soundName + PGTUtil.WAV_SUFFIX;
                break;
            case WIKI_IPA:
                soundName = PGTUtil.WIKI_WAV_LOCATION + soundName + PGTUtil.WAV_SUFFIX;
                break;
            default:
                throw new Exception("Unrecognized IPA sound library: " + ipaLibrary);
        }
        
        try {
            soundRecorder.playAudioFile(PGTUtil.IPA_SOUNDS_LOCATION + soundName);
        } catch (Exception e) {
            throw new Exception("Playback error: " + e.getLocalizedMessage(), e);
        }
    }
}
