/*
 * Copyright (c) 2019, Draque Thompson
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

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.ManagersCollections.OptionsManager;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class IOHandlerTest {
    
    public IOHandlerTest() {
        wipeErrorLog();
    }
    
    @Test
    public void testWriteErrorLogBasic() throws IOException {
        IOHandler.writeErrorLog(new Exception("This is a test."));
        File myLog = new File(PGTUtil.getErrorDirectory().getAbsolutePath() 
                + File.separator + PGTUtil.ERROR_LOG_FILE);
        
        assertTrue(myLog.exists());
        
        Scanner logScanner = new Scanner(myLog).useDelimiter("\\Z");
        String contents = logScanner.hasNext() ? logScanner.next() : "";
        
        assertTrue(contents.contains("This is a test.-java.lang.Exception"));
        assertTrue(contents.contains("java.lang.Exception: This is a test."));
        assertTrue(contents.contains("PolyGlot.IOHandlerTest.testWriteErrorLogBasic(IOHandlerTest.java"));
        
        wipeErrorLog();
    }
    
    @Test
    public void testWriteMultipleErrorLogs() throws IOException {
        IOHandler.writeErrorLog(new Exception("This is a test."));
        File myLog = new File(PGTUtil.ERROR_LOG_FILE);
        long logLenFirst = myLog.length() - 1;
        IOHandler.writeErrorLog(new Exception("This is a test."));
        long logLenSecond = myLog.length();
        
        assertTrue(logLenSecond >= logLenFirst * 2);
        wipeErrorLog();
    }
    
    @Test
    public void testWriteErrorLogsMaxLength() throws FileNotFoundException {
        for (int i = 0; i < 20; i++) {
            IOHandler.writeErrorLog(new Exception("This is a test: " + i));
        }
        
        int logLength = IOHandler.getErrorLog().length();
        int systemInfoLength = IOHandler.getSystemInformation().length();
        
        // off by one due to concatination effect when adding system info (newline)
        assertTrue(logLength == PGTUtil.maxLogCharacters + systemInfoLength + 1);        
        wipeErrorLog();
    }
    
    @Test
    public void testWriteErrorLogAccountsForCause() throws FileNotFoundException {
        String testErrorString = "UR INPUTS & OUTPUTS!";
        String bubblingException = "Bubbobbula!";
        IOException testException = new IOException(testErrorString);
        IOHandler.writeErrorLog(new Exception(bubblingException, testException));
        String log = IOHandler.getErrorLog();
        
        assertTrue(log.contains(testErrorString));
        assertTrue(log.contains(bubblingException));
    }
    
    @Test
    public void testGoodConsoleCommand() throws InterruptedException {
        System.out.println("Testing good console command");
        String[] result = IOHandler.runAtConsole(new String[]{"java", "--version"});
        
        assertTrue(!result[0].isEmpty()); // various versions of Java return every damned thing you can imagine... just test that it's SOMETHING
        assertTrue(result[1].isEmpty());
    }
    
    @Test
    public void testBadConsoleCommand() throws InterruptedException {
        System.out.println("Testing bad console command");
        String[] result = IOHandler.runAtConsole(new String[]{"WAT", "AM", "COMAND?!"});
        
        assertTrue(result[0].isEmpty());
        assertTrue(!result[1].isEmpty()); // different errors for different systems, but should be SOMETHING
    }
    
    @Test
    public void testGetTerminalJavaVersion() {
        System.out.println("getTerminalJavaVersion");
        assertTrue(!IOHandler.getTerminalJavaVersion().isEmpty());
    }
    
    @Test
    public void textIsJavaAvailableInTerminal() {
        System.out.println("isJavaAvailableInTerminal");
        assertTrue(IOHandler.isJavaAvailableInTerminal());
    }
    
    @Test
    public void testInputStreamToByteArray() throws FileNotFoundException, IOException {
        System.out.println("Testing input stream to byte array");
        byte[] expectedResult = "!@)*\ntest\n".getBytes();
        InputStream is = new FileInputStream(PGTUtil.TESTRESOURCES + "inputTest.txt");
        byte[] result = IOHandler.streamToBytArray(is);
        assertTrue(java.util.Arrays.equals(expectedResult, result));
    }
    
    @Test
    public void iniFile() throws Exception {
        System.out.println("write/read of ini file");
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
            DictCore core = new DictCore();
            core.getPropertiesManager().setOverrideProgramPath(PGTUtil.TESTRESOURCES);
            OptionsManager opt = core.getOptionsManager();

            opt.setAnimateWindows(animatedExpected);
            opt.setMaxReversionCount(reversionCountExpected);
            opt.setMenuFontSize(menuFontExpected);
            opt.setNightMode(nightModeExpected);
            opt.setScreenPosition(testScreenName, expectedScreenPosition);
            opt.setScreenSize(testScreenName, expectedScreenDimension);
            opt.setToDoBarPosition(toDoBarPositionExpected);

            // save values to disk...
            IOHandler.saveOptionsIni(core);

            // create new core to load saved values into...
            core = new DictCore();
            core.getPropertiesManager().setOverrideProgramPath(PGTUtil.TESTRESOURCES);
            opt = core.getOptionsManager();
            
            // relaod saved values...
            IOHandler.loadOptionsIni(core);
            
            assertEquals(opt.isAnimateWindows(), animatedExpected);
            assertEquals(opt.getMaxReversionCount(), reversionCountExpected);
            assertEquals(opt.getMenuFontSize(), menuFontExpected);
            assertEquals(opt.isNightMode(), nightModeExpected);
            assertEquals(opt.getScreenPosition(testScreenName), expectedScreenPosition);
            assertEquals(opt.getScreenSize(testScreenName), expectedScreenDimension);
            assertEquals(opt.getToDoBarPosition(), toDoBarPositionExpected);
        } catch (Exception e) {
            throw e;
        } finally {
            // clean up
            new File(PGTUtil.TESTRESOURCES + PGTUtil.POLYGLOT_INI).delete();
        }
    }
    
    private void wipeErrorLog() {
        File log = new File(PGTUtil.ERROR_LOG_FILE);
        if (log.exists()) {
            log.delete();
        }
    }
}
