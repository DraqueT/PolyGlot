/*
 * Copyright (c) 2019, draque
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
package org.darisadesigns.polyglotlina;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draque
 */
public class PFontHandlerTest {
    
    private final static String FONT_FAM = "Kukun_Linear_A";
    
    public PFontHandlerTest() {
    }
    
    /**
     * Runs to ensure that the correct files are the most recently updated
     */
    @BeforeAll
    public static void setupTests() {
        String path = PGTUtil.TESTRESOURCES + File.separator + "FontsAll" + File.separator + "Kukun_LinearA_V05.ttf";
        File file1 = new File(path);
        File file2 = new File(path + "X");
        try {
            Files.copy(file1.toPath(), file2.toPath());
            file1.delete();
            Files.copy(file2.toPath(), file1.toPath());
            file2.delete();
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void testGetFontFromLocationsAllSingleFolder() {
        System.out.println("PFontHandlerTest.testGetFontFromLocationsAllSingleFolder");
        String expectedFileName = "Kukun_LinearA_V05.ttf";
        
        File file = PFontHandler.getFontFromLocations(FONT_FAM, PGTUtil.TESTRESOURCES + File.separator + "FontsAll");
        
        assertEquals(file.getName(), expectedFileName);
    }
    
    @Test
    public void testGetFontFromLocationsAllTwoFolders() {
        System.out.println("PFontHandlerTest.testGetFontFromLocationsAllTwoFolders");
        String expectedFileName = "Kukun_LinearA_V05.ttf";
        
        File file = PFontHandler.getFontFromLocations(FONT_FAM, 
                PGTUtil.TESTRESOURCES + File.separator + "FontsPartial",
                PGTUtil.TESTRESOURCES + File.separator + "FontsAll"
        );
        
        assertEquals(file.getName(), expectedFileName);
    }
    
    @Test
    public void testGetFontFromLocationsEmptyLocation() {
        System.out.println("PFontHandlerTest.testGetFontFromLocationsEmptyLocation");
        File file = PFontHandler.getFontFromLocations(FONT_FAM, 
                PGTUtil.TESTRESOURCES + File.separator + "EmptyFolder");
        
        assertNull(file);
    }
    
    @Test
    public void testGetFontFromLocationsBadLocation() {
        System.out.println("PFontHandlerTest.testGetFontFromLocationsBadLocation");
        File file = PFontHandler.getFontFromLocations(FONT_FAM, 
                PGTUtil.TESTRESOURCES + File.separator + "BIBBITYBOBBITYBOO");
        
        assertNull(file);
    }

}
