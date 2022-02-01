/*
 * Copyright (c) 2019-2022, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopOptionsManager;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;
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
        
        DesktopIOHandler.getInstance().writeErrorLog(new Exception("This is a test."));
        File myLog = new File(PGTUtil.getErrorDirectory().getAbsolutePath() 
                + File.separator + PGTUtil.ERROR_LOG_FILE);
        
        assertTrue(myLog.exists());
        
        try (Scanner logScanner = new Scanner(myLog).useDelimiter("\\Z")) {
            String contents = logScanner.hasNext() ? logScanner.next() : "";

            assertTrue(contents.contains("This is a test.-java.lang.Exception"));
            assertTrue(contents.contains("java.lang.Exception: This is a test."));
            assertTrue(contents.contains("IOHandlerTest.testWriteErrorLogBasic(IOHandlerTest.java"));
        } catch (FileNotFoundException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
        
        wipeErrorLog();
    }
    
    @Test
    public void testWriteMultipleErrorLogs() {
        System.out.println("IOHandlerTest.testWriteMultipleErrorLogs");
        
        DesktopIOHandler.getInstance().writeErrorLog(new Exception("This is a test."));
        File myLog = new File(PGTUtil.ERROR_LOG_FILE);
        long logLenFirst = myLog.length() - 1;
        DesktopIOHandler.getInstance().writeErrorLog(new Exception("This is a test."));
        long logLenSecond = myLog.length();
        
        assertTrue(logLenSecond >= logLenFirst * 2);
        wipeErrorLog();
    }
    
    @Test
    public void testWriteErrorLogsMaxLength() {
        System.out.println("IOHandlerTest.testWriteErrorLogsMaxLength");
        
        for (int i = 0; i < 20; i++) {
            DesktopIOHandler.getInstance().writeErrorLog(new Exception("This is a test: " + i));
        }
        
        try {
            int logLength = DesktopIOHandler.getInstance().getErrorLog().length();
            int systemInfoLength = DesktopIOHandler.getInstance().getSystemInformation().length();

            // off by one due to concatenation effect when adding system info (newline)
            assertEquals(logLength, PGTUtil.MAX_LOG_CHARACTERS 
                    + systemInfoLength + PGTUtil.ERROR_LOG_SPEARATOR.length());
        } catch (FileNotFoundException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
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
        DesktopIOHandler.getInstance().writeErrorLog(new Exception(bubblingException, testException));
        
        try {
            String log = DesktopIOHandler.getInstance().getErrorLog();

            assertTrue(log.contains(testErrorString));
            assertTrue(log.contains(bubblingException));
        } catch (FileNotFoundException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testGoodConsoleCommand() {
        System.out.println("IOHandlerTest.testGoodConsoleCommand");
        String expectedValue = "bloop";
        
        String[] command = new String[]{"echo", expectedValue};
        
        if (PGTUtil.IS_WINDOWS) {
            command = new String[]{"cmd.exe", "/C", "echo", expectedValue};
        } else if (PGTUtil.IS_LINUX) {
            command = new String[]{"/bin/bash", "-c", "echo " + expectedValue};
        }
        
        String[] result = DesktopIOHandler.getInstance().runAtConsole(command, false);

        assertEquals(expectedValue, result[0]);
        assertTrue(result[1].isEmpty());
    }
    
    @Test
    public void testBadConsoleCommand() {
        System.out.println("IOHandlerTest.testBadConsoleCommand");
        
        
        String[] result = DesktopIOHandler.getInstance().runAtConsole(new String[]{"WAT", "AM", "COMMAND?!"}, false);

        assertTrue(result[0].isEmpty());
        assertTrue(!result[1].isEmpty()); // different errors for different systems, but should be SOMETHING
    }
    
    @Test
    public void testInputStreamToByteArray() {
        System.out.println("IOHandlerTest.testInputStreamToByteArray");
        
        try {
            byte[] expectedResult = "!@)*\ntest\n".getBytes();
            InputStream is = new FileInputStream(PGTUtil.TESTRESOURCES + "inputTest.txt");
            byte[] result = DesktopIOHandler.getInstance().clearCarrigeReturns(DesktopIOHandler.getInstance().streamToByetArray(is));
            assertTrue(java.util.Arrays.equals(expectedResult, result));
        } catch (IOException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e, e.getLocalizedMessage());
            fail(e);
        }
    }
    
    @Test
    public void testIniFile() {
        System.out.println("IOHandlerTest.testIniFile");
        
        try {
            String testScreenName = "Silly Sergal Merp Screen";

            boolean animatedExpected = true;
            int reversionCountExpected = 13;
            double menuFontExpected = 16;
            boolean nightModeExpected = true;
            Point expectedScreenPosition = new Point(13, 42);
            Dimension expectedScreenDimension = new Dimension(69, 420);
            int toDoBarPositionExpected = 666;
            int autoSavMs = 12345678;

            // create test core to set values in...
            DictCore core = DummyCore.newCore();
            DesktopOptionsManager opt = PolyGlot.getPolyGlot().getOptionsManager();

            opt.setAnimateWindows(animatedExpected);
            opt.setMaxReversionCount(reversionCountExpected);
            opt.setMenuFontSize(menuFontExpected);
            opt.setNightMode(nightModeExpected);
            opt.setScreenPosition(testScreenName, expectedScreenPosition);
            opt.setScreenSize(testScreenName, expectedScreenDimension);
            opt.setToDoBarPosition(toDoBarPositionExpected);
            opt.setMaximized(true);
            opt.setDividerPosition(testScreenName, toDoBarPositionExpected);
            opt.setMsBetweenSaves(autoSavMs);

            // save values to disk...
            DesktopIOHandler.getInstance().writeOptionsIni(core.getWorkingDirectory().getAbsolutePath(), opt);

            // create new core to load saved values into...
            core = DummyCore.newCore();
            opt = PolyGlot.getPolyGlot().getOptionsManager();
            
            // relaod saved values...
            DesktopIOHandler.getInstance().loadOptionsIni(opt, core.getWorkingDirectory().getAbsolutePath());
            
            assertEquals(animatedExpected, opt.isAnimateWindows());
            assertEquals(reversionCountExpected, opt.getMaxReversionCount());
            assertEquals(menuFontExpected, opt.getMenuFontSize());
            assertEquals(nightModeExpected, opt.isNightMode());
            assertEquals(expectedScreenPosition, opt.getScreenPosition(testScreenName));
            assertEquals(expectedScreenDimension, opt.getScreenSize(testScreenName));
            assertEquals(toDoBarPositionExpected, opt.getToDoBarPosition());
            assertEquals(toDoBarPositionExpected, opt.getDividerPosition(testScreenName));
            assertTrue(opt.isMaximized());
            assertEquals(autoSavMs, opt.getMsBetweenSaves());
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
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
            IOHandler ioHandler = DesktopIOHandler.getInstance();
            Method m = DesktopIOHandler.class.getDeclaredMethod("makeTempSaveFile", File.class);
            m.setAccessible(true);
            File tmpFile = (File)m.invoke(ioHandler, new File(PGTUtil.TESTRESOURCES));
            
            assertEquals(tmpFile.getName(), PGTUtil.TEMP_FILE);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            fail(e);
        }
    }
    
    @Test
    public void testMakeTempSaveFile_Backup() throws IOException{
        System.out.println("IOHandlerTest.testMakeTempSaveFile_Backup");
        
        try {
            wipeTempSaveFiles();
            IOHandler ioHandler = DesktopIOHandler.getInstance();
            Method m = DesktopIOHandler.class.getDeclaredMethod("makeTempSaveFile", File.class);
            m.setAccessible(true);
            File tmpFile = (File)m.invoke(ioHandler, new File(PGTUtil.TESTRESOURCES));
            tmpFile.createNewFile();
            
            m.invoke(ioHandler, new File(PGTUtil.TESTRESOURCES));
            
            m = DesktopIOHandler.class.getDeclaredMethod("getTempSaveFileIfExists", File.class);
            m.setAccessible(true);
            tmpFile = (File)m.invoke(ioHandler, new File(PGTUtil.TESTRESOURCES));
            
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
            IOHandler ioHandler = DesktopIOHandler.getInstance();
            Method m = DesktopIOHandler.class.getDeclaredMethod("makeTempSaveFile", File.class);
            m.setAccessible(true);
            File tmpFile = (File)m.invoke(ioHandler, new File(PGTUtil.TESTRESOURCES));
            tmpFile.createNewFile();
            
            m = DesktopIOHandler.class.getDeclaredMethod("getTempSaveFileIfExists", File.class);
            m.setAccessible(true);
            tmpFile = (File)m.invoke(ioHandler, new File(PGTUtil.TESTRESOURCES));
            
            assertEquals(tmpFile.getName(), PGTUtil.TEMP_FILE);
        } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            fail(e);
        }
    }
    
    @Test
    public void testCreateTmpFileWithContents() {
        System.out.println("IOHandlerTest.testCreateTmpFileWithContents");
        
        String testValue = "ß and ä\nZOT";
        
        try {
            File testFile = DesktopIOHandler.getInstance().createTmpFileWithContents(testValue, "txt");
            String result = "";
            
            if (!testFile.exists()) {
                fail("file not created");
            }
            
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
    
    @Test
    public void testCreateFileWithContents() {
        System.out.println("IOHandlerTest.testCreateFileWithContents");
        
        String testFileName = "testFile.tst";
        String testValue = "ß and ä\nZOT";
        File testFile = null;
        
        try {
            testFile = DesktopIOHandler.getInstance().createFileWithContents(testFileName, testValue);
            String result = "";
            
            if (!testFile.exists()) {
                fail("file not created");
            }
            
            try (Scanner myReader = new Scanner(testFile, StandardCharsets.UTF_8)) {
                while (myReader.hasNextLine()) {
                    result += myReader.nextLine() + "\n";
                }
                
                // truncate trailing \n
                result = result.substring(0, result.length() - 1);
            }
            
            assertEquals(testValue, result);
        } catch (IOException e) {
            fail(e);
        } finally {
            if (testFile != null && testFile.exists()) {
                testFile.delete();
            }
        }
    }
    
    @Test
    public void testWriteOSXMetadataAttributeHex() {
        Assumptions.assumeTrue(PGTUtil.IS_OSX);
        
        System.out.println("IOHandlerTest.testWriteOSXMetadataAttributeHex");
        
        String expectedEnd = "com.apple.FinderInfo:00000000  57 44 43 44 4D 53 53 50 00 00 00 00 00 00 00 00  |WDCDMSSP........|00000010  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  |................|00000020";
        
        try {
            File testFile = DesktopIOHandler.getInstance().createTmpFileWithContents("test", "tst");
            String filePath = testFile.getAbsolutePath();
            
            DesktopIOHandler.getInstance().addFileAttributeOSX(filePath,
                    PGTUtil.OSX_FINDER_METADATA_NAME,
                    PGTUtil.OSX_FINDER_INFO_VALUE_DIC_FILES,
                    true);
            
            String result = DesktopIOHandler.getInstance().getFileAttributeOSX(filePath, PGTUtil.OSX_FINDER_METADATA_NAME);
            assertTrue(result.endsWith(expectedEnd));
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testWriteOSXMetadataAttributeString() {
        Assumptions.assumeTrue(PGTUtil.IS_OSX);
        
        System.out.println("IOHandlerTest.testWriteOSXMetadataAttributeString");
        
        String attribName = "zimzam";
        String value = "mbam";
        
        try {
            File testFile = DesktopIOHandler.getInstance().createTmpFileWithContents("test", "tst");
            String filePath = testFile.getAbsolutePath();
            
            DesktopIOHandler.getInstance().addFileAttributeOSX(filePath,
                    attribName,
                    value,
                    false);
            
            String result = DesktopIOHandler.getInstance().getFileAttributeOSX(filePath, attribName);
            assertTrue(result.endsWith(value));
        } catch (Exception e) {
            fail(e);
        }
    }
    
    @Test
    public void testArchiveFile() {
        System.out.println("IOHandlerTest.testArchiveFile");
        
        try {
            String testFileContents = "THIS IS A TEST OF FILE ARCHIVAL";
            File workingDirectory = new File(PGTUtil.TESTRESOURCES);
            Path archiveFilePath = Files.write(Paths.get(PGTUtil.TESTRESOURCES + "testArchive.txt"), testFileContents.getBytes());
            File archiveFile = archiveFilePath.toFile();
            File resultFile = DesktopIOHandler.getInstance().archiveFile(archiveFile, workingDirectory);
            
            assertTrue(resultFile.exists());
            assertFalse(archiveFile.exists());
            resultFile.delete();
            assertFalse(resultFile.exists());
        } catch (IOException e) {
            fail(e);
        }
    }
    
    @Test
    public void testMultipleErrorLogs() {
        IOHandler ioHandler = DesktopIOHandler.getInstance();
        
        ioHandler.writeErrorLog(new Exception("testError"));
        ioHandler.writeErrorLog(new Exception("testError"));
        
        try {
            String errorLog = ioHandler.getErrorLog();
            assertEquals(errorLog.indexOf(PGTUtil.ERROR_LOG_SPEARATOR),
                    errorLog.lastIndexOf(PGTUtil.ERROR_LOG_SPEARATOR));
        } catch (FileNotFoundException e) {
            fail(e);
        }
    }
    
    @Test
    public void testUnpackRepackLanguage() {
        System.out.println("IOHandlerTest.testUnpackRepackLanguage");
        
        File tmpLangFile = new File(PGTUtil.TESTRESOURCES + "tmpPackedLang");
        File tmpDir = new File(PGTUtil.TESTRESOURCES + "tmpExtractedLang");
        
        try {
            String originFilePath = PGTUtil.TESTRESOURCES + "test_equality.pgd";
            DictCore origin = DummyCore.newCore();
            DictCore target = DummyCore.newCore();

            origin.readFile(originFilePath);
            
            DesktopIOHandler.getInstance().unzipFileToDir(originFilePath, tmpDir.toPath());
            DesktopIOHandler.packDirectoryToZip(tmpDir.getPath(), tmpLangFile.getPath(), true);
            target.readFile(tmpLangFile.getPath());
            
            assertEquals(origin, target);
        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
            fail(e);
        } finally {
            if (tmpLangFile.exists()) {
                tmpLangFile.delete();
            }
            
            if (tmpDir.exists()) {
                deleteDirectory(tmpDir);
            }
        }
    }
    
    /**
     * Recursively deletes directory
     * @param directoryToBeDeleted 
     */
    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        
        directoryToBeDeleted.delete();
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
