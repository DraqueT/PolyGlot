/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package PolyGlot;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author draque
 */
public class ConWordTest {
    
    public ConWordTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of getPlural and setPlural methods, of class ConWord.
     */
    @Test
    public void testGetPlural() {
        System.out.println("getPlural");
        ConWord instance = new ConWord();
        instance.setPlural("TESTPLURAL");
        String expResult = "TESTPLURAL";
        String result = instance.getPlural();
        assertEquals(expResult, result);
    }

    /**
     * Tests getters, setters and setEqual methods of setEqual method, of class ConWord.
     */
    @Test
    public void testSetEqual() {
        ConWord from = new ConWord();
        ConWord to = new ConWord();
        
        from.setDefinition("1");
        from.setGender("2");
        from.setId(3);
        from.setLocalWord("4");
        from.setPlural("5");
        from.setPronunciation("6");
        from.setValue("7");
        from.setWordType("8");
        to.setEqual(from);
        
        assertEquals(to.getDefinition(), "1");
        assertEquals(to.getGender(), "2");
        assertEquals(to.getId(), (Integer)3);
        assertEquals(to.getLocalWord(), "4");
        assertEquals(to.getPlural(), "5");
        assertEquals(to.getPronunciation(), "6");
        assertEquals(to.getValue(), "7");
        assertEquals(to.getWordType(), "8");
    }

    /**
     * Test of getLocalWord method, of class ConWord.
     */
    @Test
    public void testGetLocalWord() {
        System.out.println("getLocalWord");
        ConWord instance = new ConWord();
        String expResult = "LOCALWORD";
        instance.setLocalWord(expResult);
        String result = instance.getLocalWord();
        assertEquals(expResult, result);
    }

    /**
     * Test of getWordType method, of class ConWord.
     */
    @Test
    public void testGetWordType() {
        System.out.println("getWordType");
        ConWord instance = new ConWord();
        String expResult = "GETWORDTYPE";
        instance.setWordType(expResult);
        String result = instance.getWordType();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDefinition method, of class ConWord.
     */
    @Test
    public void testSetDefinition() {
        System.out.println("setDefinition");
        String definition = "DEFINITION";
        ConWord instance = new ConWord();
        instance.setDefinition(definition);
        String result = instance.getDefinition();
        assertEquals(result, definition);
    }

    /**
     * Test of getPronunciation method, of class ConWord.
     */
    @Test
    public void testGetPronunciation() {
        System.out.println("getPronunciation");
        ConWord instance = new ConWord();
        String expResult = "PRONUNCIATION";
        instance.setPronunciation(expResult);
        String result = instance.getPronunciation();
        assertEquals(expResult, result);
    }

    /**
     * Test of getGender method, of class ConWord.
     */
    @Test
    public void testGetGender() {
        System.out.println("getGender");
        ConWord instance = new ConWord();
        String expResult = "GENDER";
        instance.setGender(expResult);
        String result = instance.getGender();
        assertEquals(expResult, result);
    }
    
    /**
     *  Tests procOverride getter/setter
     */
    @Test
    public void testProcOverride() {
        System.out.println("ProcOverride");
        ConWord instance = new ConWord();
        boolean expResult = true;
        instance.setProcOverride(expResult);
        boolean result = instance.isProcOverride();
        assertEquals(expResult, result);
    }
}
