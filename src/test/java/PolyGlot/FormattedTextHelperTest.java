/*
 * Copyright (c) 2018, DThompson
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

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JTextPane;
import org.darisadesigns.polyglotlina.CustomControls.GrammarChapNode;
import org.darisadesigns.polyglotlina.CustomControls.GrammarSectionNode;
import org.darisadesigns.polyglotlina.CustomControls.PGrammarPane;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.FormattedTextHelper;
import org.darisadesigns.polyglotlina.PFontInfo;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author DThompson
 */
public class FormattedTextHelperTest {
    private final DictCore core;
    private final boolean headless = GraphicsEnvironment.isHeadless();
    private final static String BLACK = "black";
    private final static String RED = "red";
    private final static String GRAY = "gray";
    private final static String GREEN = "green";
    private final static String YELLOW = "yellow";
    private final static String BLUE = "blue";
    
    public FormattedTextHelperTest() throws Exception {
        System.out.println("FormattedTextHelperTest");
        core = new DictCore();
        core.readFile(PGTUtil.TESTRESOURCES + "Lodenkur_TEST.pgd");
    }
    
    @Test
    public void testHTMLLineBreakParseNoBreaks() {
        System.out.println("testHTMLLineBreakParseNoBreaks");
        String testVal = "golly gee";
        String expectedVal = "golly gee";
        String result = FormattedTextHelper.HTMLLineBreakParse(testVal);
        
        assertEquals(expectedVal, result);
    }
    
    @Test
    public void testHTMLLineBreakParseBr() {
        System.out.println("testHTMLLineBreakParseBr");
        String testVal = "<br>golly gee";
        String expectedVal = "<br>golly gee";
        String result = FormattedTextHelper.HTMLLineBreakParse(testVal);
        
        assertEquals(expectedVal, result);
    }
    
    @Test
    public void testHTMLLineBreakParseNewlineThenBr() {
        System.out.println("testHTMLLineBreakParseNewlineThenBr");
        String testVal = "x\n<br>golly gee";
        String expectedVal = "x<br><br>golly gee";
        String result = FormattedTextHelper.HTMLLineBreakParse(testVal);
        
        assertEquals(expectedVal, result);
    }
    
    @Test
    public void testHTMLLineBreakParseBrThenNewline() {
        System.out.println("testHTMLLineBreakParseBrThenNewline");
        String testVal = "<br>\ngolly gee";
        String expectedVal = "<br>golly gee";
        String result = FormattedTextHelper.HTMLLineBreakParse(testVal);
        
        assertEquals(expectedVal, result);
    }

    @Test
    public void testRestoreFromString() throws Exception {
        if (headless) {
            return;
        }
        
        System.out.println("testRestoreFromString");
        String testVal = "<font face=\"Lucida Grande\"size=\"12\"color=\"black\">Lodenkur is a language that technically does not have pronunciation in any way that we are able to perceive. It is a language \"spoken\" via radio frequency, similar to what we might think of as telepathy. This having been said, it is useful to have a way to speak and vocalize this language in a more familiar way, as a helpful mnemonic to remember vocabulary and to allow better interaction with it. Below is an attempt to convert this it a more human-friendly form.  </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"18\"color=\"black\">A Basic Explanation of Characters</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\">	There are two types of characters, the tonal and the non-tonal. Tonal characters can be pronounced in one of three ways. The first comes with no special markers, and is flat. The second has a marker on the left side. This is pronounced with a rising tone. The third has a marker on the right side. This is pronounced with a descending tone. </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\">Flat Characters:  </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">1234567890</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\">Rising Tone Characters: </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">qwertyuiop</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\">Falling Tone Characters: </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">asdfghjkl;</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\">	The second type of characters are not tonal. They are pronounced in ways the precludes rising or falling tone. In KLA, you will never see more than two of these consecutively, as this would be difficult or impossible to pronounce. </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\">Non-Tonal Characters: </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">zxcvb</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\">Pronunciation </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\">	Below is a basic pronunciation guide for each of the characters. These pronunciations do not account for tone, which is explained in greater detail later in this document. Rising and falling tones do not affect the pronunciation of characters. All pronunciations are in IPA style. </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">z</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : t - t - (today) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">x</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : th - θ (theigh) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">1</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : ha - hɑː (hall) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">2</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : wa - wɑː (water) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">3</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : no - noʊ (no) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">4</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : reh - ʀə (red) (note: french, guttural R) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">5</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : lo - loʊ (load) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">6</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : ku - kuː (cocoon) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">7</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : mi - miː (me) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">8</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : de - dɛ (debt) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">9</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : ya - jæ (yak) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">0</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : si - siː (see) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">c</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : sh - ʃ (shy) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">v</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : f - f (fan) </font><font face=\"Kukun_Linear_A\"size=\"18\"color=\"black\">b</font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> : ng - ŋ (sang) </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\">	Neutral tone is typically segmented by sentence. A speaker has their \"neutral\" voice tone, which is where each sentence begins. A character that is atonal or flat will leave the speaker's voice in the tone in which it began. A character with rising or falling tone however, will raise or lower the speaker's tone before the character is pronounced. This new tone replaces the base tone as the speaker continues. This results in a lyrical sound to speech, tone rising and falling through phrases and sentences. </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\"> </font><font face=\"Lucida Grande\"size=\"12\"color=\"black\">	In cases where the tone is too low or too high for a speaker to continue, they may return to the neutral tone between words by leaving a stressed pause between the words, although this is considered indicitive of poorly considered phrasing. Typically a speaker should manage this themselves. Tone may be raised or lowered as much as the speaker likes on an appropriate character, and the meaning remains the same. In this way, a speaker should take care that their words do not tonally escape them. Sentences tend to rise in tone as they continue, rather than dipping or staying in the same tonal position.</font>";
        JTextPane pane = new JTextPane();
        FormattedTextHelper.restoreFromString(testVal, pane, core);
        assertEquals(pane.getText(), "Lodenkur is a language that technically does not have pronunciation in any way that we are able to perceive. It is a language \"spoken\" via radio frequency, similar to what we might think of as telepathy. This having been said, it is useful to have a way to speak and vocalize this language in a more familiar way, as a helpful mnemonic to remember vocabulary and to allow better interaction with it. Below is an attempt to convert this it a more human-friendly form.   A Basic Explanation of Characters  	There are two types of characters, the tonal and the non-tonal. Tonal characters can be pronounced in one of three ways. The first comes with no special markers, and is flat. The second has a marker on the left side. This is pronounced with a rising tone. The third has a marker on the right side. This is pronounced with a descending tone.  Flat Characters:   1234567890  Rising Tone Characters:  qwertyuiop  Falling Tone Characters:  asdfghjkl;  	The second type of characters are not tonal. They are pronounced in ways the precludes rising or falling tone. In KLA, you will never see more than two of these consecutively, as this would be difficult or impossible to pronounce.  Non-Tonal Characters:  zxcvb  Pronunciation  	Below is a basic pronunciation guide for each of the characters. These pronunciations do not account for tone, which is explained in greater detail later in this document. Rising and falling tones do not affect the pronunciation of characters. All pronunciations are in IPA style.  z : t - t - (today) x : th - θ (theigh) 1 : ha - hɑː (hall) 2 : wa - wɑː (water) 3 : no - noʊ (no) 4 : reh - ʀə (red) (note: french, guttural R) 5 : lo - loʊ (load) 6 : ku - kuː (cocoon) 7 : mi - miː (me) 8 : de - dɛ (debt) 9 : ya - jæ (yak) 0 : si - siː (see) c : sh - ʃ (shy) v : f - f (fan) b : ng - ŋ (sang)  	Neutral tone is typically segmented by sentence. A speaker has their \"neutral\" voice tone, which is where each sentence begins. A character that is atonal or flat will leave the speaker's voice in the tone in which it began. A character with rising or falling tone however, will raise or lower the speaker's tone before the character is pronounced. This new tone replaces the base tone as the speaker continues. This results in a lyrical sound to speech, tone rising and falling through phrases and sentences.  	In cases where the tone is too low or too high for a speaker to continue, they may return to the neutral tone between words by leaving a stressed pause between the words, although this is considered indicitive of poorly considered phrasing. Typically a speaker should manage this themselves. Tone may be raised or lowered as much as the speaker likes on an appropriate character, and the meaning remains the same. In this way, a speaker should take care that their words do not tonally escape them. Sentences tend to rise in tone as they continue, rather than dipping or staying in the same tonal position.");
    }

    @Test
    public void testGetSectionTextFontSpecifec() {
        if (headless) {
            return;
        }
        
        System.out.println("testGetSectionTextFontSpecifec");
        GrammarChapNode chap = core.getGrammarManager().getChapters().get(0);
        
        String sectionText = ((GrammarSectionNode)chap.getFirstChild()).getSectionText();
        List<Entry<String, PFontInfo>> results = FormattedTextHelper.getSectionTextFontSpecifec(sectionText, core);
        Entry<String, PFontInfo> title = results.get(0);
        Entry<String, PFontInfo> lodenkurExample = results.get(6);
        
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        List<String> fonts = Arrays.asList(g.getAvailableFontFamilyNames());
        boolean fontInstalled = fonts.contains("Lucidia Grande");
        
        // if the font isn't installed, it'll just be the default font/size...
        if (fontInstalled) {
            assertEquals(title.getValue().awtFont.getName(), "Lucida Grande");
            assertEquals(title.getValue().size, 28);
        }
        
        assertEquals(results.size(), 12);
        assertEquals(title.getKey(), "LODENKUR");
        assertEquals(title.getValue().awtColor, Color.black);     
        assertEquals(lodenkurExample.getKey(), "6f");
        assertEquals(lodenkurExample.getValue().awtColor, Color.black);
        assertEquals(lodenkurExample.getValue().awtFont.getName(), "Kukun_Linear_A Regular");
        assertEquals(lodenkurExample.getValue().size, 18);
    }

    @Test
    public void testStorageFormat() throws Exception {
        if (headless) {
            return;
        }
        
        System.out.println("testStorageFormat");
        PGrammarPane pane = new PGrammarPane(core);
        GrammarChapNode chap = core.getGrammarManager().getChapters().get(0);
        String sectionText = ((GrammarSectionNode)chap.getChildAt(1)).getSectionText();
        
        FormattedTextHelper.restoreFromString(sectionText, pane, core);
        assertEquals(sectionText, FormattedTextHelper.storageFormat(pane));
    }

    @Test
    public void testColorToText() {
        System.out.println("testColorToText");
        assertEquals(FormattedTextHelper.colorToText(Color.black), BLACK);
        assertEquals(FormattedTextHelper.colorToText(Color.red), RED);
        assertEquals(FormattedTextHelper.colorToText(Color.green), GREEN);
        assertEquals(FormattedTextHelper.colorToText(Color.yellow), YELLOW);
        assertEquals(FormattedTextHelper.colorToText(Color.blue), BLUE);
        assertEquals(FormattedTextHelper.colorToText(Color.gray), GRAY);
    }

    @Test
    public void testTextToColor() {
        System.out.println("testTextToColor");
        assertEquals(FormattedTextHelper.textToColor(BLACK), Color.black);
        assertEquals(FormattedTextHelper.textToColor(RED), Color.red);
        assertEquals(FormattedTextHelper.textToColor(GREEN), Color.green);
        assertEquals(FormattedTextHelper.textToColor(YELLOW), Color.yellow);
        assertEquals(FormattedTextHelper.textToColor(BLUE), Color.blue);
        assertEquals(FormattedTextHelper.textToColor(GRAY), Color.gray);
    }

    @Test
    public void testHTMLLineBreakParse() {
        System.out.println("testHTMLLineBreakParse");
        String initial = "hello!\nto you!";
        String expected = "hello!<br>to you!";
        
        assertEquals(FormattedTextHelper.HTMLLineBreakParse(initial), expected);
    }

    @Test
    public void testGetTextBody() {
        System.out.println("testGetTextBody");
        String initial = "<html><head>adhgvasdjvsh</head><body>Zip zop!<br>Have a hyperlink! <a href =\"poop\">here!</a></body></html>";
        String expected = "Zip zop!Have a hyperlink! here!";
        assertEquals(FormattedTextHelper.getTextBody(initial), expected);
    }  
}
