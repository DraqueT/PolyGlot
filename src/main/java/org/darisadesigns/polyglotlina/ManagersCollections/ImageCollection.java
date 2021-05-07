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

import org.darisadesigns.polyglotlina.Nodes.ImageNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.io.IOException;
import org.darisadesigns.polyglotlina.DictCore;

/**
 *
 * @author Draque
 */
public class ImageCollection extends DictionaryCollection<ImageNode> {
    
    private final DictCore core;
    
    public ImageCollection(DictCore _core) {
        super(new ImageNode(_core));
        core = _core;
    }
    
    @Override
    public void clear() {
        bufferNode = new ImageNode(core);
    }
    
    /**
     * Gets list of all images
     * @return 
     */
    public ImageNode[] getAllImages() {
        return nodeMap.values().toArray(new ImageNode[0]);
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

        bufferNode = new ImageNode(core);
    }
    
    @Override
    public Integer insert(ImageNode _buffer) throws Exception {
        return super.insert(_buffer);
    }

    @Override
    public ImageNode notFoundNode() {
        ImageNode emptyImage = new ImageNode(core);
        
        try {
            emptyImage.setImageBytes(core.getOSHandler().getIOHandler().loadImageBytes(PGTUtil.NOT_FOUND_IMAGE));
        } catch (IOException e) {
            core.getOSHandler().getIOHandler().writeErrorLog(e);
            core.getOSHandler().getInfoBox().error("INTERNAL ERROR", 
                    "Unable to locate missing-image image.\nThis is kind of an ironic error.");
        }
        
        return emptyImage;
    }
}
