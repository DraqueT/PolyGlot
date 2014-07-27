/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package PolyGlot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author draque
 */
public class PronunciationMgrTest {
    
    List<PronunciationNode> _pronunciations = new ArrayList<PronunciationNode>();
    
    public PronunciationMgrTest() {
        PronunciationNode node = new PronunciationNode();
        
        node.setValue("1");
        node.setPronunciation("1p");
        
        _pronunciations.add(node);
        
        node = new PronunciationNode();
        node.setValue("2");
        node.setPronunciation("2p");
        
        _pronunciations.add(node);
        
        node = new PronunciationNode();
        node.setValue("3");
        node.setPronunciation("3p");
        
        _pronunciations.add(node);
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Tests setting and getting of proc list
     */
    @Test
    public void testSetPronunciations() {
        System.out.println("setPronunciations/getPronunciations");
        
        PronunciationMgr instance = new PronunciationMgr();
        instance.setPronunciations(_pronunciations);
        
        Iterator<PronunciationNode> it = instance.getPronunciations();
        
        PronunciationNode node = it.next();
        assertEquals(node.getValue(), "1");
        assertEquals(node.getPronunciation(), "1p");
        
        node = it.next();
        assertEquals(node.getValue(), "2");
        assertEquals(node.getPronunciation(), "2p");
        
        node = it.next();
        assertEquals(node.getValue(), "3");
        assertEquals(node.getPronunciation(), "3p");
     
        assertFalse(it.hasNext());
    }

    /**
     * Test of getProcIndex method, of class PronunciationMgr.
     */
    @Test
    public void testGetProcIndex() {
        System.out.println("getProcIndex");
        
        int expResult = 1;
        PronunciationNode node = new PronunciationNode();
        
        node.setValue("2");
        node.setPronunciation("2p");
        
        PronunciationMgr instance = new PronunciationMgr();
        instance.setPronunciations(_pronunciations);
        
        int result = instance.getProcIndex(node);
        
        assertEquals(expResult, result);
    }

    /**
     * Test of moveProcUp method, of class PronunciationMgr.
     */
    @Test
    public void testMoveProcUp() {
        System.out.println("moveProcUp");
        
        int expResult = 0;
        PronunciationNode node = new PronunciationNode();
        
        node.setValue("2");
        node.setPronunciation("2p");
        
        PronunciationMgr instance = new PronunciationMgr();
        
        instance.setPronunciations(_pronunciations);
        
        instance.moveProcUp(node);
                
        int result = instance.getProcIndex(node);
        
        assertEquals(expResult, result);
    }

    /**
     * Test of moveProcDown method, of class PronunciationMgr.
     */
    @Test
    public void testMoveProcDown() {
        System.out.println("moveProcUp");
        
        int expResult = 2;
        PronunciationNode node = new PronunciationNode();
        
        node.setValue("2");
        node.setPronunciation("2p");
        
        PronunciationMgr instance = new PronunciationMgr();
        
        instance.setPronunciations(_pronunciations);
        
        instance.moveProcDown(node);
                
        int result = instance.getProcIndex(node);
        
        assertEquals(expResult, result);
    }

    /**
     * Test of deletePronunciation method, of class PronunciationMgr.
     */
    @Test
    public void testDeletePronunciation() {
        System.out.println("deletePronunciation");
        int expResult = -1;
        PronunciationNode node = new PronunciationNode();
        
        node.setValue("2");
        node.setPronunciation("2p");
        
        PronunciationMgr instance = new PronunciationMgr();
        
        instance.setPronunciations(_pronunciations);
        
        instance.deletePronunciation(node);
        int result = instance.getProcIndex(node);
        
        assertEquals(expResult, result);
    }

    /**
     * Test of addPronunciation method, of class PronunciationMgr.
     */
    @Test
    public void testAddPronunciation() {
        int badResult = -1;
        PronunciationNode node = new PronunciationNode();
        
        node.setValue("4");
        node.setPronunciation("4p");
        
        PronunciationMgr instance = new PronunciationMgr();
        
        instance.setPronunciations(_pronunciations);
        
        instance.addPronunciation(node);
        int result = instance.getProcIndex(node);
        
        assertTrue(badResult != result);
    }

    /**
     * Test of getPronunciation method, of class PronunciationMgr.
     */
    @Test
    public void testGetPronunciation() {
        System.out.println("deletePronunciation");
        String expResult = "1p 2p 3p ";
        PronunciationNode node = new PronunciationNode();
        
        PronunciationMgr instance = new PronunciationMgr();
        
        instance.setPronunciations(_pronunciations);
        
        String result = instance.getPronunciation("123");
        
        assertEquals(expResult, result);
    }    
}
