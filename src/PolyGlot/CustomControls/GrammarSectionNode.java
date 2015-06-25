/*
 * Copyright (c) 2015, draque
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
package PolyGlot.CustomControls;

import PolyGlot.ManagersCollections.GrammarManager;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This is a chapter section of the PolyGlot grammar guide.
 * @author draque
 */
public class GrammarSectionNode extends DefaultMutableTreeNode {
    private final GrammarManager manager;
    private String name;
    private String sectionText;
    private int recordingId;
    
    public GrammarSectionNode(GrammarManager _manager) {
        name = "";
        sectionText = "";
        recordingId = -1;
        manager = _manager;
    }
    
    public void setName(String _name) {
        name = _name;
    }
    public String getName() {
        return name;
    }
    
    public void setRecordingId(int _recordingId) {
        recordingId = _recordingId;
    }
    
    public Integer getRecordingId() {
        return recordingId;
    }
    
    public void setSectionText(String _sectionText) {
        sectionText = _sectionText;
    }
    public String getSectionText() {
        return sectionText;
    }
    
    public void setRecording(byte[] _recording) {
        recordingId = manager.addChangeRecording(recordingId, _recording);
    }
    public byte[] getRecording() throws Exception {
        return manager.getRecording(recordingId);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
