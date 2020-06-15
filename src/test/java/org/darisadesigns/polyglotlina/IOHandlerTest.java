/*
 * Copyright (c) 2019-2020, Draque Thompson, draquemail@gmail.com
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

import TestResources.DummyCore;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.darisadesigns.polyglotlina.ManagersCollections.OptionsManager;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;

/**
 *
 * @author draque
 */
public class IOHandlerTest {
    
    public IOHandlerTest() {
        wipeErrorLog();
    }
    
    @AfterAll
    public static void cleanup() {
        wipeErrorLog();
        wipeTempSaveFiles();
    }
    
    @Test
    public void testWriteErrorLogBasic() {
        System.out.println("IOHandlerTest.testWriteErrorLogBasic");
        
        IOHandler.writeErrorLog(new Exception("This is a test."));
        File myLog = new File(PGTUtil.getErrorDirectory().getAbsolutePath() 
                + File.separator + PGTUtil.ERROR_LOG_FILE);
        
        assertTrue(myLog.exists());
        
        try (Scanner logScanner = new Scanner(myLog).useDelimiter("\\Z")) {
            String contents = logScanner.hasNext() ? logScanner.next() : "";

            assertTrue(contents.contains("This is a test.-java.lang.Exception"));
            assertTrue(contents.contains("java.lang.Exception: This is a test."));
            assertTrue(contents.contains("IOHandlerTest.testWriteErrorLogBasic(IOHandlerTest.java"));
        } catch (FileNotFoundException e) {
            IOHandler.writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
        
        wipeErrorLog();
    }
    
    @Test
    public void testWriteMultipleErrorLogs() {
        System.out.println("IOHandlerTest.testWriteMultipleErrorLogs");
        
        IOHandler.writeErrorLog(new Exception("This is a test."));
        File myLog = new File(PGTUtil.ERROR_LOG_FILE);
        long logLenFirst = myLog.length() - 1;
        IOHandler.writeErrorLog(new Exception("This is a test."));
        long logLenSecond = myLog.length();
        
        assertTrue(logLenSecond >= logLenFirst * 2);
        wipeErrorLog();
    }
    
    @Test
    public void testWriteErrorLogsMaxLength() {
        System.out.println("IOHandlerTest.testWriteErrorLogsMaxLength");
        
        for (int i = 0; i < 20; i++) {
            IOHandler.writeErrorLog(new Exception("This is a test: " + i));
        }
        
        try {
            int logLength = IOHandler.getErrorLog().length();
            int systemInfoLength = IOHandler.getSystemInformation().length();

            // off by one due to concatenation effect when adding system info (newline)
            assertEquals(logLength, PGTUtil.MAX_LOG_CHARACTERS + systemInfoLength + 1);
        } catch (FileNotFoundException e) {
            IOHandler.writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
        
        wipeErrorLog();
    }
    
    @Test
    public void testWriteErrorLogAccountsForCause() {
        System.out.println("IOHandlerTest.testWriteErrorLogAccountsForCause");
        
        String testErrorString = "UR INPUTS & OUTPUTS!";
        String bubblingException = "Bubbobbula!";
        IOException testException = new IOException(testErrorString);
        IOHandler.writeErrorLog(new Exception(bubblingException, testException));
        
        try {
            String log = IOHandler.getErrorLog();

            assertTrue(log.contains(testErrorString));
            assertTrue(log.contains(bubblingException));
        } catch (FileNotFoundException e) {
            IOHandler.writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testGoodConsoleCommand() {
        System.out.println("IOHandlerTest.testGoodConsoleCommand");
        
        
        String[] result = IOHandler.runAtConsole(new String[]{"java", "--version"}, false);

        assertTrue(!result[0].isEmpty()); // various versions of Java return every damned thing you can imagine... just test that it's SOMETHING
        assertTrue(result[1].isEmpty());
    }
    
    @Test
    public void testBadConsoleCommand() {
        System.out.println("IOHandlerTest.testBadConsoleCommand");
        
        
        String[] result = IOHandler.runAtConsole(new String[]{"WAT", "AM", "COMMAND?!"}, false);

        assertTrue(result[0].isEmpty());
        assertTrue(!result[1].isEmpty()); // different errors for different systems, but should be SOMETHING
    }
    
    @Test
    public void testGetTerminalJavaVersion() {
        System.out.println("IOHandlerTest.testGetTerminalJavaVersion");
        
        assertFalse(IOHandler.getTerminalJavaVersion().isEmpty());
    }
    
    @Test
    public void textIsJavaAvailableInTerminal() {
        System.out.println("IOHandlerTest.textIsJavaAvailableInTerminal");
        
        assertTrue(IOHandler.isJavaAvailableInTerminal());
    }
    
    @Test
    public void testInputStreamToByteArray() {
        System.out.println("IOHandlerTest.testInputStreamToByteArray");
        
        try {
            byte[] expectedResult = "!@)*\ntest\n".getBytes();
            InputStream is = new FileInputStream(PGTUtil.TESTRESOURCES + "inputTest.txt");
            byte[] result = IOHandler.clearCarrigeReturns(IOHandler.streamToByetArray(is));
            assertTrue(java.util.Arrays.equals(expectedResult, result));
        } catch (IOException e) {
            IOHandler.writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void iniFile() {
        System.out.println("IOHandlerTest.iniFile");
        
        try {
            String testScreenName = "Silly Sergal Merp Screen";

            boolean animatedExpected = true;
            int reversionCountExpected = 13;
            double menuFontExpected = 16;
            boolean nightModeExpected = true;
            Point expectedScreenPosition = new Point(13, 42);
            Dimension expectedScreenDimension = new Dimension(69, 420);
            int toDoBarPositionExpected = 666;

            // create test core to set values in...
            DictCore core = DummyCore.newCore();
            OptionsManager opt = core.getOptionsManager();

            opt.setAnimateWindows(animatedExpected);
            opt.setMaxReversionCount(reversionCountExpected, core);
            opt.setMenuFontSize(menuFontExpected);
            opt.setNightMode(nightModeExpected);
            opt.setScreenPosition(testScreenName, expectedScreenPosition);
            opt.setScreenSize(testScreenName, expectedScreenDimension);
            opt.setToDoBarPosition(toDoBarPositionExpected);

            // save values to disk...
            IOHandler.writeOptionsIni(core.getWorkingDirectory().getAbsolutePath(), opt);

            // create new core to load saved values into...
            core = DummyCore.newCore();
            opt = core.getOptionsManager();
            
            // relaod saved values...
            IOHandler.loadOptionsIni(opt, core.getWorkingDirectory().getAbsolutePath());
            
            assertEquals(opt.isAnimateWindows(), animatedExpected);
            assertEquals(opt.getMaxReversionCount(), reversionCountExpected);
            assertEquals(opt.getMenuFontSize(), menuFontExpected);
            assertEquals(opt.isNightMode(), nightModeExpected);
            assertEquals(opt.getScreenPosition(testScreenName), expectedScreenPosition);
            assertEquals(opt.getScreenSize(testScreenName), expectedScreenDimension);
            assertEquals(opt.getToDoBarPosition(), toDoBarPositionExpected);
        } catch (Exception e) {
            IOHandler.writeErrorLog(e);
            fail(e);
        } finally {
            // clean up
            new File(PGTUtil.TESTRESOURCES + PGTUtil.POLYGLOT_INI).delete();
        }
    }
    
    @Test
    public void testMakeTempSaveFile(){
        System.out.println("IOHandlerTest.testMakeTempSaveFile");
        
        try {
            wipeTempSaveFiles();
            Method m = IOHandler.class.getDeclaredMethod("makeTempSaveFile", File.class);
            m.setAccessible(true);
            File tmpFile = (File)m.invoke(null, new File(PGTUtil.TESTRESOURCES));
            
            assertEquals(tmpFile.getName(), PGTUtil.TEMP_FILE);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            fail(e);
        }
    }
    
    @Test
    public void testMakeTempSaveFile_Backup(){
        System.out.println("IOHandlerTest.testMakeTempSaveFile_Backup");
        
        try {
            wipeTempSaveFiles();
            Method m = IOHandler.class.getDeclaredMethod("makeTempSaveFile", File.class);
            m.setAccessible(true);
            File tmpFile = (File)m.invoke(null, new File(PGTUtil.TESTRESOURCES));
            tmpFile.createNewFile();
            
            m.invoke(null, new File(PGTUtil.TESTRESOURCES));
            
            m = IOHandler.class.getDeclaredMethod("getTempSaveFileIfExists", File.class);
            m.setAccessible(true);
            tmpFile = (File)m.invoke(null, new File(PGTUtil.TESTRESOURCES));
            
            assertTrue(tmpFile.getName().startsWith(PGTUtil.TEMP_FILE));
            assertNotEquals(tmpFile.getName(), PGTUtil.TEMP_FILE);
        } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            fail(e);
        }
    }
    
    @Test
    public void getTempSaveFileIfExists_Basic(){
        System.out.println("IOHandlerTest.getTempSaveFileIfExists_Basic");
        
        try {
            wipeTempSaveFiles();
            Method m = IOHandler.class.getDeclaredMethod("makeTempSaveFile", File.class);
            m.setAccessible(true);
            File tmpFile = (File)m.invoke(null, new File(PGTUtil.TESTRESOURCES));
            tmpFile.createNewFile();
            
            m = IOHandler.class.getDeclaredMethod("getTempSaveFileIfExists", File.class);
            m.setAccessible(true);
            tmpFile = (File)m.invoke(null, new File(PGTUtil.TESTRESOURCES));
            
            assertEquals(tmpFile.getName(), PGTUtil.TEMP_FILE);
        } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            fail(e);
        }
    }
    
    @Test
    public void testCreateTmpFileWithContents() {
        String testValue = "ß and ä\nZOT";
        
        try {
            File testFile = IOHandler.createTmpFileWithContents(testValue, "txt");
            String result = "";
            
            try (Scanner myReader = new Scanner(testFile, StandardCharsets.UTF_8)) {
                while (myReader.hasNextLine()) {
                    result += myReader.nextLine() + "\n";
                }
                
                // truncate trailing \n
                result = result.substring(0, result.length() - 1);
            }
            
            assertEquals(testValue, result);
        }
        catch (IOException e) {
            fail(e);
        }
    }
    
    /**
     * Wipes all test temp files
     */
    private static void wipeTempSaveFiles(){
        File testDir = new File(PGTUtil.TESTRESOURCES);
        
        for (File test : testDir.listFiles()) {
            if (test.getName().startsWith(PGTUtil.TEMP_FILE)) {
                test.delete();
            }
        }
    }
    
    private static void wipeErrorLog() {
        File log = new File(PGTUtil.ERROR_LOG_FILE);
        if (log.exists()) {
            log.delete();
        }
    }
}
