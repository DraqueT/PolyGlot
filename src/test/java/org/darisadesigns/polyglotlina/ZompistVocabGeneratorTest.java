/*
 * Copyright (c) 2022-2023, Draque Thompson, draquemail@gmail.com
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class ZompistVocabGeneratorTest {
    
    public ZompistVocabGeneratorTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of genAllSyllables method, of class ZompistVocabGenerator.
     */
    @Test
    public void testGenAllSyllables_noCommas() {
        System.out.println("ZompistVocabGeneratorTest.genAllSyllables_noCommas");
        
        String categories = "C=ptknlsmšywčhfŋ\n" +
            "V=auieo\n" +
            "R=rly\n" +
            "N=nnŋmktp\n" +
            "W=io\n" +
            "Q=ptkč";
        String illegals = "pa";
        String rewrites = "uu|wo\n" +
            "oo|ou\n" +
            "ii|iu\n" +
            "aa|ia\n" +
            "ee|ie";
        String syllables = "CV\n" +
            "QʰV\n" +
            "CVW\n" +
            "CVN\n" +
            "VN\n" +
            "V\n" +
            "QʰVN";
        
        try {
            ZompistVocabGenerator instance = new ZompistVocabGenerator(false, false, 1, 1, categories, syllables, rewrites, illegals, null);
            String[] expResult = getTestSyllables_noCommas();
            String[] result = instance.genAllSyllables();
            
            assertArrayEquals(expResult, result);
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testGenAllSyllables_withCommas() {
        System.out.println("ZompistVocabGeneratorTest.genAllSyllables_withCommas");
        
        String categories = "C=p,b,mz\n" +
            "V=auie\n" +
            "R=rly\n";
        String illegals = "pa";
        String rewrites = "uu|wo\n" +
            "oo|ou\n" +
            "ii|iu\n" +
            "aa|ia\n" +
            "ee|ie";
        String syllables = "CV\n" +
            "CVR\n" +
            "RV\n" +
            "CRV\n";
        
        try {
            ZompistVocabGenerator instance = new ZompistVocabGenerator(false, false, 1, 1, categories, syllables, rewrites, illegals, null);
            String[] expResult = getTestSyllables_withCommas();
            String[] result = instance.genAllSyllables();
            
            assertArrayEquals(expResult, result);
        } catch (Exception e) {
            fail(e);
        }
    }

//    /**
//     * Test of genWords method, of class ZompistVocabGenerator.
//     */
//    @Test
//    public void testGenWords() throws Exception {
//        // NONDETERMINISTIC, CANNOT EASILY TEST
//    }

//    /**
//     * Test of createText method, of class ZompistVocabGenerator.
//     */
//    @Test
//    public void testCreateText() throws Exception {
//        // NONDETERMINISTIC, CANNOT EASILY TEST
//    }

    /**
     * Test of checkCatFormattingCorrect method, of class ZompistVocabGenerator.
     */
    @Test
    public void testCheckCatFormattingCorrect_right() {
        System.out.println("ZompistVocabGeneratorTest.checkCatFormattingCorrect_right");
        String catCheck = "C=p,t,k,n,l,s,m,šy,w,čhf,ŋ\n" +
            "V=auieo\n" +
            "R=rly\n" +
            "N=nnŋmktp\n" +
            "W=io\n" +
            "Q=ptkč";
        boolean expResult = true;
        boolean result = ZompistVocabGenerator.checkCatFormattingCorrect(catCheck);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of checkCatFormattingCorrect method, of class ZompistVocabGenerator.
     */
    @Test
    public void testCheckCatFormattingCorrect_Wring() {
        System.out.println("ZompistVocabGeneratorTest.checkCatFormattingCorrect_wrong");
        String catCheck = "C=p,t,k,n=,l,s,m,šy,w,čhf,ŋ\n" +
            "V=auieo\n" +
            "R=rly\n" +
            "N=nnŋmktp\n" +
            "W=io\n" +
            "Q=ptkč";
        boolean expResult = false;
        boolean result = ZompistVocabGenerator.checkCatFormattingCorrect(catCheck);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of checkIllegalsFormattingCorrect method, of class ZompistVocabGenerator.
     */
    @Test
    public void testCheckIllegalsFormattingCorrect_right() {
        System.out.println("ZompistVocabGeneratorTest.checkIllegalsFormattingCorrect_right");
        String illegals = "pw\n" +
            "fw\n" +
            "bw\n" +
            "tl\n" +
            "dl\n" +
            "θl\n" +
            "gw\n" +
            "mb\n" +
            "mv";
        boolean expResult = true;
        boolean result = ZompistVocabGenerator.checkIllegalsFormattingCorrect(illegals);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkIllegalsFormattingCorrect method, of class ZompistVocabGenerator.
     */
    @Test
    public void testCheckIllegalsFormattingCorrect_wrong() {
        System.out.println("ZompistVocabGeneratorTest.checkIllegalsFormattingCorrect_wrong");
        String illegals = "p,w\n" +
            "fw\n" +
            "bw\n" +
            "tl\n" +
            "dl\n" +
            "θl\n" +
            "gw\n" +
            "mb\n" +
            "mv";
        boolean expResult = false;
        boolean result = ZompistVocabGenerator.checkIllegalsFormattingCorrect(illegals);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkRewriteRules method, of class ZompistVocabGenerator.
     */
    @Test
    public void testCheckRewriteRules_right() {
        System.out.println("ZompistVocabGeneratorTest.checkRewriteRules_right");
        String rewrites = "â|ai\n" +
                "ô|au";
        boolean expResult = true;
        boolean result = ZompistVocabGenerator.checkRewriteRules(rewrites);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of checkRewriteRules method, of class ZompistVocabGenerator.
     */
    @Test
    public void testCheckRewriteRules_wrong_extraPipes() {
        System.out.println("ZompistVocabGeneratorTest.checkRewriteRules_wrong_extraPipes");
        String rewrites = "â|a|i\n" +
                "ô|au";
        boolean expResult = false;
        boolean result = ZompistVocabGenerator.checkRewriteRules(rewrites);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of checkRewriteRules method, of class ZompistVocabGenerator.
     */
    @Test
    public void testCheckRewriteRules_wrong_commas() {
        System.out.println("ZompistVocabGeneratorTest.checkRewriteRules_wrong_commas");
        String rewrites = "â|a,i\n" +
                "ô|au";
        boolean expResult = false;
        boolean result = ZompistVocabGenerator.checkRewriteRules(rewrites);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkSyllableRules method, of class ZompistVocabGenerator.
     */
    @Test
    public void testCheckSyllableRules_right() {
        System.out.println("ZompistVocabGeneratorTest.checkSyllableRules_right");
        String catCheck = "C=ptknslrmbdgfvwyhšzñxčžŋ\n" +
                "V=aiuoeɛɔâôüö\n" +
                "R=rly";
        String sylCheck = "CV\n" +
                "V\n" +
                "CVC\n" +
                "CRV";
        String expResult = "";
        String result = ZompistVocabGenerator.checkSyllableRules(catCheck, sylCheck);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of checkSyllableRules method, of class ZompistVocabGenerator.
     */
    @Test
    public void testCheckSyllableRules_warning() {
        System.out.println("ZompistVocabGeneratorTest.checkSyllableRules_warning");
        String catCheck = "C=ptknslrmbdgfvwyhšzñxčžŋ\n" +
                "V=aiuoeɛɔâôüö\n" +
                "R=rly";
        String sylCheck = "CV\n" +
                "Vj\n" +
                "CVC\n" +
                "CRVX";
        String expResult = "WARNING: The following constant values are found in your syllable types: j, X";
        String result = ZompistVocabGenerator.checkSyllableRules(catCheck, sylCheck);
        assertEquals(expResult, result);
    }
    
    private String[] getTestSyllables_noCommas() {
        return ("pu\npi\npe\npo\nta\ntu\nti\nte\nto\nka\nku\nki\nke\nko\nna\nnu\nni\nne\nno\nla\nlu\nli\nle\nlo\nsa\nsu\nsi\nse\nso\nma\nmu\nmi\nme\nmo\nša\nšu\nši\n" +
            "še\nšo\nya\nyu\nyi\nye\nyo\nwa\nwu\nwi\nwe\nwo\nča\nču\nči\nče\nčo\nha\nhu\nhi\nhe\nho\nfa\nfu\nfi\nfe\nfo\nŋa\nŋu\nŋi\nŋe\nŋo\npʰa\npʰu\npʰi\npʰe\npʰo\n" +
            "tʰa\ntʰu\ntʰi\ntʰe\ntʰo\nkʰa\nkʰu\nkʰi\nkʰe\nkʰo\nčʰa\nčʰu\nčʰi\nčʰe\nčʰo\npui\npuo\npiu\npio\npei\npeo\npoi\npou\ntai\ntao\ntui\ntuo\ntiu\n" +
            "tio\ntei\nteo\ntoi\ntou\nkai\nkao\nkui\nkuo\nkiu\nkio\nkei\nkeo\nkoi\nkou\nnai\nnao\nnui\nnuo\nniu\nnio\nnei\nneo\nnoi\nnou\nlai\nlao\nlui\nluo\nliu\n" +
            "lio\nlei\nleo\nloi\nlou\nsai\nsao\nsui\nsuo\nsiu\nsio\nsei\nseo\nsoi\nsou\nmai\nmao\nmui\nmuo\nmiu\nmio\nmei\nmeo\nmoi\nmou\nšai\nšao\nšui\nšuo\nšiu\n" +
            "šio\nšei\nšeo\nšoi\nšou\nyai\nyao\nyui\nyuo\nyiu\nyio\nyei\nyeo\nyoi\nyou\nwai\nwao\nwui\nwuo\nwiu\nwio\nwei\nweo\nwoi\nwou\nčai\nčao\nčui\nčuo\nčiu\n" +
            "čio\nčei\nčeo\nčoi\nčou\nhai\nhao\nhui\nhuo\nhiu\nhio\nhei\nheo\nhoi\nhou\nfai\nfao\nfui\nfuo\nfiu\nfio\nfei\nfeo\nfoi\nfou\nŋai\nŋao\nŋui\nŋuo\nŋiu\n" +
            "ŋio\nŋei\nŋeo\nŋoi\nŋou\npun\npuŋ\npum\npuk\nput\npup\npin\npiŋ\npim\npik\npit\npip\npen\npeŋ\npem\npek\npet\npep\npon\npoŋ\n" +
            "pom\npok\npot\npop\ntan\ntaŋ\ntam\ntak\ntat\ntap\ntun\ntuŋ\ntum\ntuk\ntut\ntup\ntin\ntiŋ\ntim\ntik\ntit\ntip\nten\nteŋ\ntem\ntek\ntet\ntep\nton\ntoŋ\n" +
            "tom\ntok\ntot\ntop\nkan\nkaŋ\nkam\nkak\nkat\nkap\nkun\nkuŋ\nkum\nkuk\nkut\nkup\nkin\nkiŋ\nkim\nkik\nkit\nkip\nken\nkeŋ\nkem\nkek\nket\nkep\nkon\nkoŋ\n" +
            "kom\nkok\nkot\nkop\nnan\nnaŋ\nnam\nnak\nnat\nnap\nnun\nnuŋ\nnum\nnuk\nnut\nnup\nnin\nniŋ\nnim\nnik\nnit\nnip\nnen\nneŋ\nnem\nnek\nnet\nnep\nnon\n" +
            "noŋ\nnom\nnok\nnot\nnop\nlan\nlaŋ\nlam\nlak\nlat\nlap\nlun\nluŋ\nlum\nluk\nlut\nlup\nlin\nliŋ\nlim\nlik\nlit\nlip\nlen\nleŋ\nlem\nlek\nlet\nlep\nlon\nloŋ\nlom\n" +
            "lok\nlot\nlop\nsan\nsaŋ\nsam\nsak\nsat\nsap\nsun\nsuŋ\nsum\nsuk\nsut\nsup\nsin\nsiŋ\nsim\nsik\nsit\nsip\nsen\nseŋ\nsem\nsek\nset\nsep\nson\nsoŋ\n" +
            "som\nsok\nsot\nsop\nman\nmaŋ\nmam\nmak\nmat\nmap\nmun\nmuŋ\nmum\nmuk\nmut\nmup\nmin\nmiŋ\nmim\nmik\nmit\nmip\nmen\nmeŋ\nmem\nmek\nmet\nmep\nmon\n" +
            "moŋ\nmom\nmok\nmot\nmop\nšan\nšaŋ\nšam\nšak\nšat\nšap\nšun\nšuŋ\nšum\nšuk\nšut\nšup\nšin\nšiŋ\nšim\nšik\nšit\nšip\nšen\nšeŋ\nšem\nšek\nšet\nšep\nšon\n" +
            "šoŋ\nšom\nšok\nšot\nšop\nyan\nyaŋ\nyam\nyak\nyat\nyap\nyun\nyuŋ\nyum\nyuk\nyut\nyup\nyin\nyiŋ\nyim\nyik\nyit\nyip\nyen\nyeŋ\nyem\nyek\nyet\nyep\nyon\n" +
            "yoŋ\nyom\nyok\nyot\nyop\nwan\nwaŋ\nwam\nwak\nwat\nwap\nwun\nwuŋ\nwum\nwuk\nwut\nwup\nwin\nwiŋ\nwim\nwik\nwit\nwip\nwen\nweŋ\nwem\nwek\nwet\nwep\nwon\nwoŋ\nwom\n" +
            "wok\nwot\nwop\nčan\nčaŋ\nčam\nčak\nčat\nčap\nčun\nčuŋ\nčum\nčuk\nčut\nčup\nčin\nčiŋ\nčim\nčik\nčit\nčip\nčen\nčeŋ\nčem\nček\nčet\nčep\nčon\nčoŋ\nčom\nčok\n" +
            "čot\nčop\nhan\nhaŋ\nham\nhak\nhat\nhap\nhun\nhuŋ\nhum\nhuk\nhut\nhup\nhin\nhiŋ\nhim\nhik\nhit\nhip\nhen\nheŋ\nhem\nhek\nhet\nhep\nhon\nhoŋ\nhom\nhok\nhot\n" +
            "hop\nfan\nfaŋ\nfam\nfak\nfat\nfap\nfun\nfuŋ\nfum\nfuk\nfut\nfup\nfin\nfiŋ\nfim\nfik\nfit\nfip\nfen\nfeŋ\nfem\nfek\nfet\nfep\nfon\nfoŋ\nfom\nfok\nfot\nfop\n" +
            "ŋan\nŋaŋ\nŋam\nŋak\nŋat\nŋap\nŋun\nŋuŋ\nŋum\nŋuk\nŋut\nŋup\nŋin\nŋiŋ\nŋim\nŋik\nŋit\nŋip\nŋen\nŋeŋ\nŋem\nŋek\nŋet\nŋep\nŋon\nŋoŋ\nŋom\nŋok\nŋot\nŋop\n" +
            "an\naŋ\nam\nak\nat\nap\nun\nuŋ\num\nuk\nut\nup\nin\niŋ\nim\nik\nit\nip\nen\neŋ\nem\nek\net\nep\non\noŋ\nom\nok\not\nop\na\nu\ni\ne\no\n" +
            "pʰan\npʰaŋ\npʰam\npʰak\npʰat\npʰap\npʰun\npʰuŋ\npʰum\npʰuk\npʰut\npʰup\npʰin\npʰiŋ\npʰim\npʰik\npʰit\npʰip\npʰen\npʰeŋ\npʰem\npʰek\npʰet\npʰep\npʰon\npʰoŋ\npʰom\n" +
            "pʰok\npʰot\npʰop\ntʰan\ntʰaŋ\ntʰam\ntʰak\ntʰat\ntʰap\ntʰun\ntʰuŋ\ntʰum\ntʰuk\ntʰut\ntʰup\ntʰin\ntʰiŋ\ntʰim\ntʰik\ntʰit\ntʰip\ntʰen\ntʰeŋ\ntʰem\ntʰek\ntʰet\n" +
            "tʰep\ntʰon\ntʰoŋ\ntʰom\ntʰok\ntʰot\ntʰop\nkʰan\nkʰaŋ\nkʰam\nkʰak\nkʰat\nkʰap\nkʰun\nkʰuŋ\nkʰum\nkʰuk\nkʰut\nkʰup\nkʰin\nkʰiŋ\nkʰim\nkʰik\nkʰit\nkʰip\nkʰen\n" +
            "kʰeŋ\nkʰem\nkʰek\nkʰet\nkʰep\nkʰon\nkʰoŋ\nkʰom\nkʰok\nkʰot\nkʰop\nčʰan\nčʰaŋ\nčʰam\nčʰak\nčʰat\nčʰap\nčʰun\nčʰuŋ\nčʰum\nčʰuk\nčʰut\nčʰup\nčʰin\nčʰiŋ\nčʰim\n" +
            "čʰik\nčʰit\nčʰip\nčʰen\nčʰeŋ\nčʰem\nčʰek\nčʰet\nčʰep\nčʰon\nčʰoŋ\nčʰom\nčʰok\nčʰot\nčʰop").split("\n");
    }
    
    private String[] getTestSyllables_withCommas() {
        return ("pu\npi\npe\nba\nbu\nbi\nbe\nmza\nmzu\nmzi\nmze\npur\npul\npuy\npir\npil\npiy\nper\npel\npey\nbar\nbal\nbay\n" +
            "bur\nbul\nbuy\nbir\nbil\nbiy\nber\nbel\nbey\nmzar\nmzal\nmzay\nmzur\nmzul\nmzuy\nmzir\nmzil\nmziy\nmzer\nmzel\nmzey\nra\nru\nri\nre\nla\n" +
            "lu\nli\nle\nya\nyu\nyi\nye\npra\npru\npri\npre\npla\nplu\npli\nple\npya\npyu\npyi\npye\nbra\nbru\nbri\nbre\nbla\nblu\nbli\nble\nbya\nbyu\nbyi\n" +
            "bye\nmzra\nmzru\nmzri\nmzre\nmzla\nmzlu\nmzli\nmzle\nmzya\nmzyu\nmzyi\nmzye").split("\n");
    }
    
}
