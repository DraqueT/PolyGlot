/*
 * Copyright (c) 2016-2019, Draque Thompson
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
package org.darisadesigns.polyglotlina;

import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is poorly written because it"s based on functionality pulled from
 * an IPA website. Not very Extensible. The IPA chart is probably not going to
 * change though. So it"s fine. All the classes are basically just maps over the
 * images users can click. Return of "" character indicates non clickable area
 * selected. No sound played in this case. "*" is the general error character
 * and will show up when something unexpected happens (error should bubble).
 * Also, there are atrocious switch statements. Don't care.
 *
 * @author Draque Thompson
 */
public class IPAHandler {

    private final SoundRecorder soundRecorder;
    private final Map<String, String> charMap;
    private final String[][] pulmonicTable = new String[23][9];

    public IPAHandler(Window _parent) {
        soundRecorder = new SoundRecorder(_parent);
        charMap = new HashMap<>();
        setupMap();
    }

    private void setupMap() {
        charMap.put("ʍ", "Voiceless_labio-velar_fricative");
        charMap.put("w", "Voiced_labio-velar_approximant");
        charMap.put("ɥ", "Labial-palatal_approximant");
        charMap.put("ʜ", "Voiceless_epiglottal_fricative");
        charMap.put("ʢ", "Voiced_epiglottal_fricative");
        charMap.put("ʡ", "Voiceless_epiglottal_plosive");
        charMap.put("ɕ", "Voiceless_alveolo-palatal_fricative");
        charMap.put("ʑ", "Voiced_alveolo-palatal_fricative");
        charMap.put("ɺ", "Alveolar_lateral_flap");
        charMap.put("ɧ", "Voiceless_dorso-palatal_velar_fricative");
        charMap.put("t͡s", "Voiceless_alveolar_affricate");
        charMap.put("t͡ʃ", "Voiceless_palato-alveolar_affricate");
        charMap.put("t͡ɕ", "Voiceless_alveolo-palatal_affricate");
        charMap.put("t͡ʂ", "Voiceless_retroflex_affricate");
        charMap.put("d͡z", "Voiceless_alveolar_affricate"); // - FIX??
        charMap.put("d͡ʒ", "Voiced_postalveolar_affricate");
        charMap.put("d͡ʑ", "Voiced_alveolo-palatal_affricate");
        charMap.put("d͡ʐ", "Voiceless_retroflex_affricate");
        charMap.put("ʘ", "Bilabial_click");
        charMap.put("ǀ", "Dental_click");
        charMap.put("ǃ", "Postalveolar_click");
        charMap.put("ǂ", "Palatoalveolar_click");
        charMap.put("ǁ", "Alveolar_lateral_click");
        charMap.put("ɓ", "Voiced_bilabial_implosive");
        charMap.put("ɗ", "Voiced_alveolar_implosive");
        charMap.put("ʄ", "Voiced_palatal_implosive");
        charMap.put("ɠ", "Voiced_velar_implosive");
        charMap.put("ʛ", "Voiced_uvular_implosive");
        charMap.put("p'", "Bilabial_ejective_plosive");
        charMap.put("t'", "Alveolar_ejective_plosive");
        charMap.put("k'", "Velar_ejective_plosive");
        charMap.put("s'", "Alveolar_ejective_fricative");
        charMap.put("p", "Voiceless_bilabial_plosive");
        charMap.put("b", "Voiced_bilabial_plosive");
        charMap.put("t", "Voiceless_alveolar_plosive");
        charMap.put("d", "Voiced_alveolar_plosive");
        charMap.put("ʈ", "Voiceless_retroflex_plosive");
        charMap.put("ɖ", "Voiced_retroflex_plosive");
        charMap.put("c", "Voiceless_palatal_plosive");
        charMap.put("ɟ", "Voiced_palatal_plosive");
        charMap.put("k", "Voiceless_velar_plosive");
        charMap.put("g", "Voiced_velar_plosive");
        charMap.put("q", "Voiceless_uvular_plosive");
        charMap.put("ɢ", "Voiced_uvular_plosive");
        charMap.put("ʔ", "Glottal_stop");
        charMap.put("m", "Bilabial_nasal");
        charMap.put("ɱ", "Labiodental_nasal");
        charMap.put("n", "Alveolar_nasal");
        charMap.put("ɳ", "Retroflex_nasal");
        charMap.put("ɲ", "Palatal_nasal");
        charMap.put("ŋ", "Velar_nasal");
        charMap.put("ɴ", "Uvular_nasal");
        charMap.put("ʙ", "Bilabial_trill");
        charMap.put("r", "Alveolar_trill");
        charMap.put("ʀ", "Uvular_trill");
        charMap.put("ɾ", "Alveolar_tap");
        charMap.put("ɽ", "Retroflex_flap");
        charMap.put("ɸ", "Voiceless_bilabial_fricative");
        charMap.put("β", "Voiced_bilabial_fricative");
        charMap.put("f", "Voiceless_labiodental_fricative");
        charMap.put("v", "Voiced_labiodental_fricative");
        charMap.put("θ", "Voiceless_dental_fricative");
        charMap.put("ð", "Voiced_dental_fricative");
        charMap.put("s", "Voiceless_alveolar_fricative");
        charMap.put("z", "Voiced_alveolar_fricative");
        charMap.put("ʃ", "Voiceless_postalveolar_fricative");
        charMap.put("ʒ", "Voiced_postalveolar_fricative");
        charMap.put("ʂ", "Voiceless_retroflex_fricative");
        charMap.put("ʐ", "Voiced_retroflex_fricative");
        charMap.put("ç", "Voiceless_palatal_fricative");
        charMap.put("ʝ", "Voiced_palatal_fricative");
        charMap.put("x", "Voiceless_velar_fricative");
        charMap.put("ɣ", "Voiced_velar_fricative");
        charMap.put("χ", "Voiceless_uvular_fricative");
        charMap.put("ʁ", "Voiced_uvular_fricative");
        charMap.put("ħ", "Voiceless_pharyngeal_fricative");
        charMap.put("ʕ", "Voiced_pharyngeal_fricative");
        charMap.put("h", "Voiceless_glottal_fricative");
        charMap.put("ɦ", "Voiced_glottal_fricative");
        charMap.put("ɬ", "Voiceless_alveolar_lateral_fricative");
        charMap.put("ɮ", "Voiced_alveolar_lateral_fricative");
        charMap.put("ʋ", "Labiodental_approximant");      
        charMap.put("ɹ", "Alveolar_approximant");
        charMap.put("ɻ", "Retroflex_approximant");
        charMap.put("j", "Palatal_approximant");
        charMap.put("ɰ", "Voiced_velar_approximant");
        charMap.put("l", "Alveolar_lateral_approximant");
        charMap.put("ɭ", "Retroflex_lateral_approximant");
        charMap.put("ʎ", "Palatal_lateral_approximant");
        charMap.put("ʟ", "Velar_lateral_approximant");
        charMap.put("i", "Close_front_unrounded_vowel");
        charMap.put("y", "Close_front_rounded_vowel");
        charMap.put("ɨ", "Close_central_unrounded_vowel");
        charMap.put("ʉ", "Close_central_rounded_vowel");
        charMap.put("ɯ", "Close_back_unrounded_vowel");
        charMap.put("u", "Close_back_rounded_vowel");
        charMap.put("ɪ", "Near-close_near-front_unrounded_vowel");
        charMap.put("ʏ", "Near-close_near-front_rounded_vowel");
        charMap.put("ʊ", "Near-close_near-back_rounded_vowel");
        charMap.put("e", "Close-mid_front_unrounded_vowel");
        charMap.put("ø", "Close-mid_front_rounded_vowel");
        charMap.put("ɘ", "Close-mid_central_unrounded_vowel");
        charMap.put("ɵ", "Close-mid_central_rounded_vowel");
        charMap.put("ɤ", "Close-mid_back_unrounded_vowel");
        charMap.put("o", "Close-mid_back_rounded_vowel");
        charMap.put("ə", "Mid-central_vowel");
        charMap.put("ɛ", "Open-mid_front_unrounded_vowel");
        charMap.put("œ", "Open-mid_front_rounded_vowel");
        charMap.put("ɜ", "Open-mid_central_unrounded_vowel");
        charMap.put("ɞ", "Open-mid_central_rounded_vowel");
        charMap.put("ʌ", "Open-mid_back_unrounded_vowel");
        charMap.put("ɔ", "Open-mid_back_rounded_vowel");
        charMap.put("æ", "Near-open_front_unrounded_vowel");
        charMap.put("ɐ", "Near-open_central_unrounded_vowel");
        charMap.put("a", "Open_front_unrounded_vowel");
        charMap.put("ɶ", "Open_front_rounded_vowel");
        charMap.put("ɑ", "Open_back_unrounded_vowel");
        charMap.put("ɒ", "Open_back_rounded_vowel");
        
        pulmonicTable[1][1] = "p";
        pulmonicTable[2][1] = "b";
        pulmonicTable[5][1] = "t";
        pulmonicTable[6][1] = "t";
        pulmonicTable[7][1] = "t";
        pulmonicTable[8][1] = "d";
        pulmonicTable[9][1] = "d";
        pulmonicTable[10][1] = "d";
        pulmonicTable[11][1] = "ʈ";
        pulmonicTable[12][1] = "ɖ";
        pulmonicTable[13][1] = "c";
        pulmonicTable[14][1] = "ɟ";
        pulmonicTable[15][1] = "k";
        pulmonicTable[16][1] = "g";
        pulmonicTable[17][1] = "q";
        pulmonicTable[18][1] = "ɢ";
        pulmonicTable[21][1] = "ʔ";
        pulmonicTable[2][2] = "m";
        pulmonicTable[4][2] = "ɱ";
        pulmonicTable[8][2] = "n";
        pulmonicTable[9][2] = "n";
        pulmonicTable[10][2] = "n";
        pulmonicTable[12][2] = "ɳ";
        pulmonicTable[14][2] = "ɲ";
        pulmonicTable[16][2] = "ŋ";
        pulmonicTable[18][2] = "ɴ";
        pulmonicTable[2][3] = "ʙ";
        pulmonicTable[8][3] = "r";
        pulmonicTable[9][3] = "r";
        pulmonicTable[10][3] = "r";
        pulmonicTable[18][3] = "ʀ";
        pulmonicTable[8][4] = "ɾ";
        pulmonicTable[9][4] = "ɾ";
        pulmonicTable[10][4] = "ɾ";
        pulmonicTable[12][4] = "ɽ";
        pulmonicTable[1][5] = "ɸ";
        pulmonicTable[2][5] = "β";
        pulmonicTable[3][5] = "f";
        pulmonicTable[4][5] = "v";
        pulmonicTable[5][5] = "θ";
        pulmonicTable[6][5] = "ð";
        pulmonicTable[7][5] = "s";
        pulmonicTable[8][5] = "z";
        pulmonicTable[9][5] = "ʃ";
        pulmonicTable[10][5] = "ʒ";
        pulmonicTable[11][5] = "ʂ";
        pulmonicTable[12][5] = "ʐ";
        pulmonicTable[13][5] = "ç";
        pulmonicTable[14][5] = "ʝ";
        pulmonicTable[15][5] = "x";
        pulmonicTable[16][5] = "ɣ";
        pulmonicTable[17][5] = "χ";
        pulmonicTable[18][5] = "ʁ";
        pulmonicTable[19][5] = "ħ";
        pulmonicTable[20][5] = "ʕ";
        pulmonicTable[21][5] = "h";
        pulmonicTable[22][5] = "ɦ";
        pulmonicTable[5][6] = "ɬ";
        pulmonicTable[6][6] = "ɬ";
        pulmonicTable[7][6] = "ɬ";
        pulmonicTable[8][6] = "ɮ";
        pulmonicTable[9][6] = "ɮ";
        pulmonicTable[10][6] = "ɮ";
        pulmonicTable[4][7] = "ʋ";
        pulmonicTable[8][7] = "ɹ";
        pulmonicTable[9][7] = "ɹ";
        pulmonicTable[10][7] = "ɹ";
        pulmonicTable[12][7] = "ɻ";
        pulmonicTable[14][7] = "j";
        pulmonicTable[16][7] = "ɰ";
        pulmonicTable[8][8] = "l";
        pulmonicTable[9][8] = "l";
        pulmonicTable[10][8] = "l";
        pulmonicTable[12][8] = "ɭ";
        pulmonicTable[14][8] = "ʎ";
        pulmonicTable[16][8] = "ʟ";
        
    }

    public String playVowelGetChar(int x, int y, IPALibrary ipaLibrary) throws Exception {
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
        
        playChar(ret, ipaLibrary);

        return ret;
    }

    public String playPulConsGetChar(int x, int y, IPALibrary ipaLibrary) throws Exception {
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
        
        playChar(ret, ipaLibrary);

        return ret;
    }

    public String playNonPulConsGetChar(int x, int y, IPALibrary ipaLibrary) throws Exception {
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

        playChar(ret, ipaLibrary);

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

        playChar(ret, ipaLibrary);

        return ret;
    }

    /**
     * Plays sound of IPA pronunciation based on Stringacter
     *
     * @param c IPA character to play
     * @param ipaLibrary library to use to play sound
     * @throws Exception on any playback error, unknown IPA character, unknown IPA sound library
     */
    private void playChar(String c, IPALibrary ipaLibrary) throws Exception {
        if (c.length() == 0) {
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
    
    public enum IPALibrary {
        WIKI_IPA("Wikimedia Commons IPA"),
        UCLA_IPA("UCLA Phonetics Lab IPA");
        
        public final String label;
        
        private IPALibrary(String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return this.label;
        }
    }
}
