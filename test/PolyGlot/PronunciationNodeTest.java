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
public class PronunciationNodeTest {
    
    public PronunciationNodeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of getPronunciation and set methods, of class PronunciationNode.
     */
    @Test
    public void testGetPronunciation() {
        System.out.println("getPronunciation/setPronuncation");
        PronunciationNode instance = new PronunciationNode();
        String expResult = "TESTPROC";
        instance.setPronunciation(expResult);
        String result = instance.getPronunciation();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEqual method, of class PronunciationNode.
     */
    @Test
    public void testSetEqual() {
        System.out.println("setEqual");
        PronunciationNode instance = new PronunciationNode();
        PronunciationNode copyTo = new PronunciationNode();
        
        String ExpProc = "EXPPROC";
        String ExpValue = "EXPVALUE";
        
        instance.setId(1);
        instance.setPronunciation(ExpProc);
        instance.setValue(ExpValue);
        
        copyTo.setEqual(instance);
        
        assertEquals(copyTo.getId(), (Integer)1);
        assertEquals(copyTo.getPronunciation(), ExpProc);
        assertEquals(copyTo.getValue(), ExpValue);
    }
    
    /**
     * Test of equals method, of class PronunciationNode.
     */
    @Test
    public void testEquals() {
        System.out.println("equals True");
        System.out.println("setEqual");
        PronunciationNode instance = new PronunciationNode();
        PronunciationNode copyTo = new PronunciationNode();

        String ExpProc = "EXPPROC";
        String ExpValue = "EXPVALUE";

        instance.setId(1);
        instance.setPronunciation(ExpProc);
        instance.setValue(ExpValue);

        copyTo.setEqual(instance);

        assertTrue(instance.equals(copyTo));
    }
    
    /**
     * Test of equals method, of class PronunciationNode.
     */
    @Test
    public void testNotEquals() {
        System.out.println("equals True");
        System.out.println("setEqual");
        PronunciationNode instance = new PronunciationNode();
        PronunciationNode copyTo = new PronunciationNode();

        String ExpProc = "EXPPROC";
        String ExpValue = "EXPVALUE";

        instance.setId(1);
        instance.setPronunciation(ExpProc);
        instance.setValue(ExpValue);

        copyTo.setId(2);
        copyTo.setPronunciation(ExpProc);
        copyTo.setValue(ExpValue);

        assertFalse(instance.equals(copyTo));
        
        copyTo.setId(1);
        copyTo.setPronunciation("WRONG");
        copyTo.setValue(ExpValue);

        assertFalse(instance.equals(copyTo));
        
        copyTo.setId(1);
        copyTo.setPronunciation(ExpProc);
        copyTo.setValue("WRONG");

        assertFalse(instance.equals(copyTo));
    }
}
