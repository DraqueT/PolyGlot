/*
 * Copyright (c) 2016, Draque
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
package PolyGlot.ManagersCollections;

import PolyGlot.IOHandler;
import PolyGlot.Nodes.ImageNode;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Draque
 */
public class ImageCollection extends DictionaryCollection {
    public ImageCollection() {
        bufferNode = new ImageNode();
    }
    
    @Override
    public void clear() {
        bufferNode = new ImageNode();
    }
    
    /**
     * Gets list of all images
     * @return 
     */
    public List<ImageNode> getAllImages() {
        return new ArrayList<>(nodeMap.values());
    }
    
    /**
     * Pulls in new image from user selected file
     * Returns null if user cancels process
     * @param parent parent form
     * @return ImageNode inserted into collection with populated image
     * @throws IOException on file read error
     */
    public ImageNode openNewImage(Window parent) throws IOException, Exception {
        ImageNode image = null;
        try {
            BufferedImage buffImg = IOHandler.openImage(parent);
            
            if (buffImg != null) {
                image = new ImageNode();
                image.setImage(buffImg);
                insert(image);
            }
        } catch (IOException e) {
            throw new IOException("Problem loading image: " + e.getLocalizedMessage());
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
        insert(ret);
        
        return ret;
    }
}
