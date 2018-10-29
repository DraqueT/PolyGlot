/*
 * Copyright (c) 2018, Draque Thompson
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

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Draque Thompson
 */
public class InternalResourcesTest {
    
    @Test
    public void testImageResources() {
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
            PGTUtil.emptyLogoImage,
            PGTUtil.IPAOtherSounds,
            PGTUtil.treeNodeImage,
            PGTUtil.notFoundImage};
        
        for (String imageFile : testImagePaths) {
            try {
                ImageIO.read(getClass().getResource(PGTUtil.notFoundImage));
            } catch (IOException e) {
                problemFiles += imageFile + ", ";
            }
        }

        assertTrue("Problem with image files: " + problemFiles, problemFiles.isEmpty());
    }
    
    @Test
    public void testFontResources() {
        String problemFiles = "";
        String[] fontFileLocations =  {PGTUtil.LCDFontLocation,
            PGTUtil.UnicodeFontLocation,
            PGTUtil.UnicodeFontBoldLocation,
            PGTUtil.UnicodeFontItalicLocation,
            PGTUtil.UnicodeFontBoldItalicLocation,
            PGTUtil.ButtonFontLocation};
        
        for (String fontFileLocation : fontFileLocations) {
            try (InputStream tmp = this.getClass().getResourceAsStream(fontFileLocation)) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Font.createFont(Font.TRUETYPE_FONT, tmp);
            } catch (Exception e) {
                problemFiles += fontFileLocation + ", ";
            }
        }
        
        assertTrue("Problem with font files: " + problemFiles, problemFiles.isEmpty());
    }
}
