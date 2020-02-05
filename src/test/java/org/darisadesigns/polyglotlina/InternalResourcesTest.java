/*
 * Copyright (c) 2018-2019, Draque Thompson
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

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.darisadesigns.polyglotlina.PGTUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Draque Thompson
 */
public class InternalResourcesTest {
    
    @Test
    public void testImageResources() {
        System.out.println("InternalResourcesTest.testImageResources");
        
        String problemFiles = "";
        String[] testImagePaths = {
            "/PolyGlot/ImageAssets/add_button.png",
            "/PolyGlot/ImageAssets/delete_button.png",
            "/PolyGlot/ImageAssets/add_button_pressed.png",
            "/PolyGlot/ImageAssets/delete_button_pressed.png",
            "/PolyGlot/ImageAssets/EmptyImage.png",
            "/PolyGlot/ImageAssets/IPA_Vowels.png",
            "/PolyGlot/ImageAssets/not-found.png",
            "/PolyGlot/ImageAssets/recording_ON_BIG.png",
            "/PolyGlot/ImageAssets/IPA_NonPulmonicConsonants.png",
            "/PolyGlot/ImageAssets/play_OFF_BIG.png",
            "/PolyGlot/ImageAssets/PolyGlot About.png",
            "/PolyGlot/ImageAssets/treeNode.png",
            "/PolyGlot/ImageAssets/IPA_Other.png",
            "/PolyGlot/ImageAssets/PolyGlotBG.png",
            "/PolyGlot/ImageAssets/play_ON_BIG.png",
            "/PolyGlot/ImageAssets/IPA_Pulmonic_Consonants.png",
            "/PolyGlot/ImageAssets/n0rara_draque.png",
            "/PolyGlot/ImageAssets/recording_OFF_BIG.png",
            PGTUtil.EMPTY_LOGO_IMAGE,
            PGTUtil.IPA_OTHER_IMAGE,
            PGTUtil.TREE_NODE_IMAGE,
            PGTUtil.NOT_FOUND_IMAGE};
        
        for (String imageFile : testImagePaths) {
            try {
                ImageIO.read(getClass().getResource(PGTUtil.NOT_FOUND_IMAGE));
            } catch (IOException e) {
                problemFiles += imageFile + ", ";
            }
        }

        assertTrue(problemFiles.isEmpty(), "Problem with image files: " + problemFiles);
    }
    
    @Test
    public void testFontResources() {
        System.out.println("InternalResourcesTest.testFontResources");
        
        String problemFiles = "";
        String[] fontFileLocations =  {PGTUtil.LCD_FONT_LOCATION,
            PGTUtil.UNICODE_FONT_LOCATION,
            PGTUtil.UNICODE_FONT_BOLD_LOCATION,
            PGTUtil.UNICODE_FONT_ITALIC_LOCATION,
            PGTUtil.UNICODE_FONT_BOLD_ITALIC_LOCATION,
            PGTUtil.BUTTON_FONT_LOCATION};
        
        for (String fontFileLocation : fontFileLocations) {
            try (InputStream tmp = this.getClass().getResourceAsStream(fontFileLocation)) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Font.createFont(Font.TRUETYPE_FONT, tmp);
            } catch (Exception e) {
                problemFiles += fontFileLocation + ", ";
            }
        }
        
        assertTrue(problemFiles.isEmpty(), "Problem with font files: " + problemFiles);
    }
}
