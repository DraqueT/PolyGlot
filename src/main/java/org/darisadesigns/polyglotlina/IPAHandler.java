/*
 * Copyright (c) 2016-2020, Draque Thompson, draquemail@gmail.com
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

    protected final Map<String, String> charMap;
    protected final String[][] pulmonicTable = new String[23][9];

    public IPAHandler() {
        charMap = getSetupMap();
        setupPulmonicTable();
    }
    
    public static Map<String, String> getSetupMap() {
        Map<String, String> ret = new HashMap<>();
        
        ret.put("ʍ", "Voiceless_labio-velar_fricative");
        ret.put("w", "Voiced_labio-velar_approximant");
        ret.put("ɥ", "Labial-palatal_approximant");
        ret.put("ʜ", "Voiceless_epiglottal_fricative");
        ret.put("ʢ", "Voiced_epiglottal_fricative");
        ret.put("ʡ", "Voiceless_epiglottal_plosive");
        ret.put("ɕ", "Voiceless_alveolo-palatal_fricative");
        ret.put("ʑ", "Voiced_alveolo-palatal_fricative");
        ret.put("ɺ", "Alveolar_lateral_flap");
        ret.put("ɧ", "Voiceless_dorso-palatal_velar_fricative");
        ret.put("t͡s", "Voiceless_alveolar_affricate");
        ret.put("t͡ʃ", "Voiceless_palato-alveolar_affricate");
        ret.put("t͡ɕ", "Voiceless_alveolo-palatal_affricate");
        ret.put("t͡ʂ", "Voiceless_retroflex_affricate");
        ret.put("d͡z", "Voiceless_alveolar_affricate"); // - FIX??
        ret.put("d͡ʒ", "Voiced_postalveolar_affricate");
        ret.put("d͡ʑ", "Voiced_alveolo-palatal_affricate");
        ret.put("d͡ʐ", "Voiceless_retroflex_affricate");
        ret.put("ʘ", "Bilabial_click");
        ret.put("ǀ", "Dental_click");
        ret.put("ǃ", "Postalveolar_click");
        ret.put("ǂ", "Palatoalveolar_click");
        ret.put("ǁ", "Alveolar_lateral_click");
        ret.put("ɓ", "Voiced_bilabial_implosive");
        ret.put("ɗ", "Voiced_alveolar_implosive");
        ret.put("ʄ", "Voiced_palatal_implosive");
        ret.put("ɠ", "Voiced_velar_implosive");
        ret.put("ʛ", "Voiced_uvular_implosive");
        ret.put("p'", "Bilabial_ejective_plosive");
        ret.put("t'", "Alveolar_ejective_plosive");
        ret.put("k'", "Velar_ejective_plosive");
        ret.put("s'", "Alveolar_ejective_fricative");
        ret.put("p", "Voiceless_bilabial_plosive");
        ret.put("b", "Voiced_bilabial_plosive");
        ret.put("t", "Voiceless_alveolar_plosive");
        ret.put("d", "Voiced_alveolar_plosive");
        ret.put("ʈ", "Voiceless_retroflex_plosive");
        ret.put("ɖ", "Voiced_retroflex_plosive");
        ret.put("c", "Voiceless_palatal_plosive");
        ret.put("ɟ", "Voiced_palatal_plosive");
        ret.put("k", "Voiceless_velar_plosive");
        ret.put("g", "Voiced_velar_plosive");
        ret.put("q", "Voiceless_uvular_plosive");
        ret.put("ɢ", "Voiced_uvular_plosive");
        ret.put("ʔ", "Glottal_stop");
        ret.put("m", "Bilabial_nasal");
        ret.put("ɱ", "Labiodental_nasal");
        ret.put("n", "Alveolar_nasal");
        ret.put("ɳ", "Retroflex_nasal");
        ret.put("ɲ", "Palatal_nasal");
        ret.put("ŋ", "Velar_nasal");
        ret.put("ɴ", "Uvular_nasal");
        ret.put("ʙ", "Bilabial_trill");
        ret.put("r", "Alveolar_trill");
        ret.put("ʀ", "Uvular_trill");
        ret.put("ɾ", "Alveolar_tap");
        ret.put("ɽ", "Retroflex_flap");
        ret.put("ɸ", "Voiceless_bilabial_fricative");
        ret.put("β", "Voiced_bilabial_fricative");
        ret.put("f", "Voiceless_labiodental_fricative");
        ret.put("v", "Voiced_labiodental_fricative");
        ret.put("θ", "Voiceless_dental_fricative");
        ret.put("ð", "Voiced_dental_fricative");
        ret.put("s", "Voiceless_alveolar_fricative");
        ret.put("z", "Voiced_alveolar_fricative");
        ret.put("ʃ", "Voiceless_postalveolar_fricative");
        ret.put("ʒ", "Voiced_postalveolar_fricative");
        ret.put("ʂ", "Voiceless_retroflex_fricative");
        ret.put("ʐ", "Voiced_retroflex_fricative");
        ret.put("ç", "Voiceless_palatal_fricative");
        ret.put("ʝ", "Voiced_palatal_fricative");
        ret.put("x", "Voiceless_velar_fricative");
        ret.put("ɣ", "Voiced_velar_fricative");
        ret.put("χ", "Voiceless_uvular_fricative");
        ret.put("ʁ", "Voiced_uvular_fricative");
        ret.put("ħ", "Voiceless_pharyngeal_fricative");
        ret.put("ʕ", "Voiced_pharyngeal_fricative");
        ret.put("h", "Voiceless_glottal_fricative");
        ret.put("ɦ", "Voiced_glottal_fricative");
        ret.put("ɬ", "Voiceless_alveolar_lateral_fricative");
        ret.put("ɮ", "Voiced_alveolar_lateral_fricative");
        ret.put("ʋ", "Labiodental_approximant");      
        ret.put("ɹ", "Alveolar_approximant");
        ret.put("ɻ", "Retroflex_approximant");
        ret.put("j", "Palatal_approximant");
        ret.put("ɰ", "Voiced_velar_approximant");
        ret.put("l", "Alveolar_lateral_approximant");
        ret.put("ɭ", "Retroflex_lateral_approximant");
        ret.put("ʎ", "Palatal_lateral_approximant");
        ret.put("ʟ", "Velar_lateral_approximant");
        ret.put("i", "Close_front_unrounded_vowel");
        ret.put("y", "Close_front_rounded_vowel");
        ret.put("ɨ", "Close_central_unrounded_vowel");
        ret.put("ʉ", "Close_central_rounded_vowel");
        ret.put("ɯ", "Close_back_unrounded_vowel");
        ret.put("u", "Close_back_rounded_vowel");
        ret.put("ɪ", "Near-close_near-front_unrounded_vowel");
        ret.put("ʏ", "Near-close_near-front_rounded_vowel");
        ret.put("ʊ", "Near-close_near-back_rounded_vowel");
        ret.put("e", "Close-mid_front_unrounded_vowel");
        ret.put("ø", "Close-mid_front_rounded_vowel");
        ret.put("ɘ", "Close-mid_central_unrounded_vowel");
        ret.put("ɵ", "Close-mid_central_rounded_vowel");
        ret.put("ɤ", "Close-mid_back_unrounded_vowel");
        ret.put("o", "Close-mid_back_rounded_vowel");
        ret.put("ə", "Mid-central_vowel");
        ret.put("ɛ", "Open-mid_front_unrounded_vowel");
        ret.put("œ", "Open-mid_front_rounded_vowel");
        ret.put("ɜ", "Open-mid_central_unrounded_vowel");
        ret.put("ɞ", "Open-mid_central_rounded_vowel");
        ret.put("ʌ", "Open-mid_back_unrounded_vowel");
        ret.put("ɔ", "Open-mid_back_rounded_vowel");
        ret.put("æ", "Near-open_front_unrounded_vowel");
        ret.put("ɐ", "Near-open_central_unrounded_vowel");
        ret.put("a", "Open_front_unrounded_vowel");
        ret.put("ɶ", "Open_front_rounded_vowel");
        ret.put("ɑ", "Open_back_unrounded_vowel");
        ret.put("ɒ", "Open_back_rounded_vowel");
        
        return ret;
    }
    
    private void setupPulmonicTable() {
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
    
    /**
     * Returns array containing string list of all supported IPA characters
     * @return 
     */
    public static String[] getAllIpaChars() {
        return getSetupMap().keySet().toArray(new String[0]);
    }
    
    public enum IPALibrary {
        WIKI_IPA("Wikimedia Commons IPA"),
        UCLA_IPA("UCLA Phonetics Lab IPA");
        
        public final String label;
        
        IPALibrary(String _label) {
            this.label = _label;
        }
        
        @Override
        public String toString() {
            return this.label;
        }
    }
}
