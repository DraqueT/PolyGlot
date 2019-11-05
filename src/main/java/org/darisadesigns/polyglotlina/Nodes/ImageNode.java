/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Nodes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Draque
 */
public class ImageNode extends DictNode {
    private BufferedImage image = null;
    private File tmpFile = null;
    
    /**
     * @param _image the image to set
     */
    public void setImage(BufferedImage _image) {
        image = _image;
    }
    
    /**
     * Gets path to temporary file in which image has been stored (if one exists)
     * for consumption in HTML based text areas
     * @return path of image file
     * @throws java.io.IOException on file read error, or image not initialized
     */
    public String getImagePath() throws IOException {
        if (image == null) {
            throw new IOException("Image not instantiated. Cannot generate path.");
        }
        
        if (id == -1) {
            throw new IOException("Image not inserted into image collection (id = -1). Cannot generate path.");
        }
        
        // create tmp file if none exists
        if (tmpFile == null || !tmpFile.exists()) {
            tmpFile = File.createTempFile(id + "_polyGlotImage", ".png");
            ImageIO.write(image, "PNG", new FileOutputStream(tmpFile));
        }
        
        return tmpFile.getAbsolutePath();
    }
    
    /**
     * Sets image equal to.
     * Only sets equal the buffered image and the id. Nothing else.
     * @param _node
     * @throws ClassCastException 
     */
    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        if (!(_node instanceof ImageNode)) {
            String name = _node == null ? "null" : _node.getClass().getName();
            throw new ClassCastException("Cannot convert type: " 
                    + name + " to type ImageNode.");
        }
        ImageNode tmpNode = (ImageNode)_node;
        
        image = tmpNode.image;
        id = tmpNode.getId();
    }

    /**
     * @return the image
     */
    public BufferedImage getImage() {
        return image;
    }
    
    @Override
    public boolean equals(Object comp) {
        boolean ret = false;
        
        if (this == comp) {
            ret = true;
        } else if (comp != null && getClass() == comp.getClass()) {
            ImageNode c = (ImageNode)comp;
            
            ret = (image == c.image && image == null) || image.equals(c.image);
            ret = ret && value.equals(c.value);
        }
        
        return ret;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
