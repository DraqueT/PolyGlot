/*
 * Copyright (c) 2016-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.ManagersCollections;

import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.Nodes.ImageNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Draque
 */
public class ImageCollection extends DictionaryCollection<ImageNode> {
    public ImageCollection() {
        super(new ImageNode());
    }
    
    @Override
    public void clear() {
        bufferNode = new ImageNode();
    }
    
    /**
     * Gets list of all images
     * @return 
     */
    public ImageNode[] getAllImages() {
        return nodeMap.values().toArray(new ImageNode[0]);
    }
    
    /**
     * Pulls in new image from user selected file
     * Returns null if user cancels process
     * @param parent parent form
     * @return ImageNode inserted into collection with populated image
     * @throws IOException on file read error
     */
    public ImageNode openNewImage(Window parent) throws Exception {
        ImageNode image = null;
        try {
            BufferedImage buffImg = IOHandler.openImage(parent);
            
            if (buffImg != null) {
                image = new ImageNode();
                image.setImage(buffImg);
                insert(image);
            }
        } catch (IOException e) {
            throw new IOException("Problem loading image: " + e.getLocalizedMessage(), e);
        }
        return image;
    }
    
    /**
     * inserts current buffer image to conWord list based on id; blanks out
     * buffer
     *
     * @param _id id to insert node with
     * @throws Exception
     */
    public void insert(Integer _id) throws Exception {
        super.insert(_id, bufferNode);

        bufferNode = new ImageNode();
    }
    
    /**
     * Takes a buffered image, and returns a node containing it, having inserted
     * the node with ID to persist on save
     * @param _image Image to get node of.
     * @return populated Image node
     * @throws java.lang.Exception
     */
    public ImageNode getFromBufferedImage(BufferedImage _image) throws Exception {
        ImageNode ret = new ImageNode();
        ret.setImage(_image);
        this.insert(ret);
        
        return ret;
    }

    @Override
    public Object notFoundNode() {
        ImageNode emptyImage = new ImageNode();
        
        try {
            emptyImage.setImage(ImageIO.read(getClass().getResource(PGTUtil.NOT_FOUND_IMAGE)));
        } catch (IOException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("INTERNAL ERROR", 
                    "Unable to locate missing-image image.\nThis is kind of an ironic error.", null);
        }
        
        return emptyImage;
    }
}
