/*
 * Copyright (c) 2016, draque
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
        charMap = new HashMap<String, String>();
        setupMap();
    }

    private void setupMap() {
        charMap.put("ʍ", "Voiceless_labio-velar_fricative.mp3");
        charMap.put("w", "Voiced_labio-velar_approximant.mp3");
        charMap.put("ɥ", "Labial-palatal_approximant.mp3");
        charMap.put("ʜ", "Voiceless_epiglottal_fricative.mp3");
        charMap.put("ʢ", "Voiced_epiglottal_fricative.mp3");
        charMap.put("ʡ", "Voiceless_epiglottal_plosive.mp3");
        charMap.put("ɕ", "Voiceless_alveolo-palatal_fricative.mp3");
        charMap.put("ʑ", "Voiced_alveolo-palatal_fricative.mp3");
        charMap.put("ɺ", "Alveolar_lateral_flap.mp3");
        charMap.put("ɧ", "Voiceless_dorso-palatal_velar_fricative.mp3");
        charMap.put("t͡s", "Voiceless_alveolar_affricate.mp3");
        charMap.put("t͡ʃ", "Voiceless_palato-alveolar_affricate.mp3");
        charMap.put("t͡ɕ", "Voiceless_alveolo-palatal_affricate.mp3");
        charMap.put("t͡ʂ", "Voiceless_retroflex_affricate.mp3");
        charMap.put("d͡z", "Voiceless_alveolar_affricate.mp3");
        charMap.put("d͡ʒ", "Voiced_postalveolar_affricate.mp3");
        charMap.put("d͡ʑ", "Voiced_alveolo-palatal_affricate.mp3");
        charMap.put("d͡ʐ", "Voiceless_retroflex_affricate.mp3");
        charMap.put("ʘ", "Bilabial_click.mp3");
        charMap.put("ǀ", "Dental_click.mp3");
        charMap.put("ǃ", "Postalveolar_click.mp3");
        charMap.put("ǂ", "Palatoalveolar_click.mp3");
        charMap.put("ǁ", "Alveolar_lateral_click.mp3");
        charMap.put("ɓ", "Voiced_bilabial_implosive.mp3");
        charMap.put("ɗ", "Voiced_alveolar_implosive.mp3");
        charMap.put("ʄ", "Voiced_palatal_implosive.mp3");
        charMap.put("ɠ", "Voiced_velar_implosive.mp3");
        charMap.put("ʛ", "Voiced_uvular_implosive.mp3");
        charMap.put("p'", "Bilabial_ejective_plosive.mp3");
        charMap.put("t'", "Alveolar_ejective_plosive.mp3");
        charMap.put("k'", "Velar_ejective_plosive.mp3");
        charMap.put("s'", "Alveolar_ejective_fricative.mp3");
        charMap.put("p", "Voiceless_bilabial_plosive.mp3");
        charMap.put("b", "Voiced_bilabial_plosive.mp3");
        charMap.put("t", "Voiceless_alveolar_plosive.mp3");
        charMap.put("d", "Voiced_alveolar_plosive.mp3");
        charMap.put("ʈ", "Voiceless_retroflex_plosive.mp3");
        charMap.put("ɖ", "Voiced_retroflex_plosive.mp3");
        charMap.put("c", "Voiceless_palatal_plosive.mp3");
        charMap.put("ɟ", "Voiced_palatal_plosive.mp3");
        charMap.put("k", "Voiceless_velar_plosive.mp3");
        charMap.put("g", "Voiced_velar_plosive.mp3");
        charMap.put("q", "Voiceless_uvular_plosive.mp3");
        charMap.put("ɢ", "Voiced_uvular_plosive.mp3");
        charMap.put("ʔ", "Glottal_stop.mp3");
        charMap.put("m", "Bilabial_nasal.mp3");
        charMap.put("ɱ", "Labiodental_nasal.mp3");
        charMap.put("n", "Alveolar_nasal.mp3");
        charMap.put("ɳ", "Retroflex_nasal.mp3");
        charMap.put("ɲ", "Palatal_nasal.mp3");
        charMap.put("ŋ", "Velar_nasal.mp3");
        charMap.put("ɴ", "Uvular_nasal.mp3");
        charMap.put("ʙ", "Bilabial_trill.mp3");
        charMap.put("r", "Alveolar_trill.mp3");
        charMap.put("ʀ", "Uvular_trill.mp3");
        charMap.put("ɾ", "Alveolar_tap.mp3");
        charMap.put("ɽ", "Retroflex_flap.mp3");
        charMap.put("ɸ", "Voiceless_bilabial_fricative.mp3");
        charMap.put("β", "Voiced_bilabial_fricative.mp3");
        charMap.put("f", "Voiceless_labiodental_fricative.mp3");
        charMap.put("v", "Voiced_labiodental_fricative.mp3");
        charMap.put("θ", "Voiceless_dental_fricative.mp3");
        charMap.put("ð", "Voiced_dental_fricative.mp3");
        charMap.put("s", "Voiceless_alveolar_fricative.mp3");
        charMap.put("z", "Voiced_alveolar_fricative.mp3");
        charMap.put("ʃ", "Voiceless_postalveolar_fricative.mp3");
        charMap.put("ʒ", "Voiced_postalveolar_fricative.mp3");
        charMap.put("ʂ", "Voiceless_retroflex_fricative.mp3");
        charMap.put("ʐ", "Voiced_retroflex_fricative.mp3");
        charMap.put("ç", "Voiceless_palatal_fricative.mp3");
        charMap.put("ʝ", "Voiced_palatal_fricative.mp3");
        charMap.put("x", "Voiceless_velar_fricative.mp3");
        charMap.put("ɣ", "Voiced_velar_fricative.mp3");
        charMap.put("χ", "Voiceless_uvular_fricative.mp3");
        charMap.put("ʁ", "Voiced_uvular_fricative.mp3");
        charMap.put("ħ", "Voiceless_pharyngeal_fricative.mp3");
        charMap.put("ʕ", "Voiced_pharyngeal_fricative.mp3");
        charMap.put("h", "Voiceless_glottal_fricative.mp3");
        charMap.put("ɦ", "Voiced_glottal_fricative.mp3");
        charMap.put("ɬ", "Voiceless_alveolar_lateral_fricative.mp3");
        charMap.put("ɮ", "Voiced_alveolar_lateral_fricative.mp3");
        charMap.put("ʋ", "Labiodental_approximant.mp3");      
        charMap.put("ɹ", "Alveolar_approximant.mp3");
        charMap.put("ɻ", "Retroflex_approximant.mp3");
        charMap.put("j", "Palatal_approximant.mp3");
        charMap.put("ɰ", "Voiced_velar_approximant.mp3");
        charMap.put("l", "Alveolar_lateral_approximant.mp3");
        charMap.put("ɭ", "Retroflex_lateral_approximant.mp3");
        charMap.put("ʎ", "Palatal_lateral_approximant.mp3");
        charMap.put("ʟ", "Velar_lateral_approximant.mp3");
        charMap.put("i", "Close_front_unrounded_vowel.mp3");
        charMap.put("y", "Close_front_rounded_vowel.mp3");
        charMap.put("ɨ", "Close_central_unrounded_vowel.mp3");
        charMap.put("ʉ", "Close_central_rounded_vowel.mp3");
        charMap.put("ɯ", "Close_back_unrounded_vowel.mp3");
        charMap.put("u", "Close_back_rounded_vowel.mp3");
        charMap.put("ɪ", "Near-close_near-front_unrounded_vowel.mp3");
        charMap.put("ʏ", "Near-close_near-front_rounded_vowel.mp3");
        charMap.put("ʊ", "Near-close_near-back_rounded_vowel.mp3");
        charMap.put("e", "Close-mid_front_unrounded_vowel.mp3");
        charMap.put("ø", "Close-mid_front_rounded_vowel.mp3");
        charMap.put("ɘ", "Close-mid_central_unrounded_vowel.mp3");
        charMap.put("ɵ", "Close-mid_central_rounded_vowel.mp3");
        charMap.put("ɤ", "Close-mid_back_unrounded_vowel.mp3");
        charMap.put("o", "Close-mid_back_rounded_vowel.mp3");
        charMap.put("ə", "Mid-central_vowel.mp3");
        charMap.put("ɛ", "Open-mid_front_unrounded_vowel.mp3");
        charMap.put("œ", "Open-mid_front_rounded_vowel.mp3");
        charMap.put("ɜ", "Open-mid_central_unrounded_vowel.mp3");
        charMap.put("ɞ", "Open-mid_central_rounded_vowel.mp3");
        charMap.put("ʌ", "Open-mid_back_unrounded_vowel.mp3");
        charMap.put("ɔ", "Open-mid_back_rounded_vowel.mp3");
        charMap.put("æ", "Near-open_front_unrounded_vowel.mp3");
        charMap.put("ɐ", "Near-open_central_unrounded_vowel.mp3");
        charMap.put("a", "Open_front_unrounded_vowel.mp3");
        charMap.put("ɶ", "Open_front_rounded_vowel.mp3");
        charMap.put("ɑ", "Open_back_unrounded_vowel.mp3");
        charMap.put("ɒ", "Open_back_rounded_vowel.mp3");
        
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

    public String playVowelGetChar(int x, int y) throws Exception {
        String ret = "*";

        // DO STUFF
        playChar(ret);

        return ret;
    }

    public String playPulConsGetChar(int x, int y) throws Exception {
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
        
        playChar(ret);

        return ret;
    }

    public String playNonPulConsGetChar(int x, int y) throws Exception {
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

        playChar(ret);

        return ret;
    }

    public String playOtherGetChar(int x, int y) throws Exception {
        String ret = "*";

        // returns blank if in a non-viable position
        if (x < 45 || x > 631 || y < 45 || y > 365 || (x > 313 && x < 332)
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

        playChar(ret);

        return ret;
    }

    /**
     * Plays sound of IPA pronunciation based on Stringacter
     *
     * @param c
     */
    private void playChar(String c) throws Exception {
        if (c.equals("")) {
            return;
        }

        if (charMap.containsKey(c)) {
            playProc(charMap.get(c));
        } else {
            throw new Exception("Unable to find character " + c
                    + " in pronunciations.");
        }
    }

    private void playProc(String soundName) {
        // TODO: HANDLE UNRECOGNIZE FILE SIGNATURE ERROR BUBBLE BETTER
        soundRecorder.playMP3(PGTUtil.ipaSoundsLocation + soundName);
    }
}
