/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 * See LICENSE.TXT included with this code to read the full license agreement.
 *
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

package PolyGlot.Nodes;

/**
 * Records orthographic pronunciation values
 * @author draque
 */
public class PronunciationNode extends DictNode {
    private String pronunciation = "";
    
    public String getPronunciation() {
        return pronunciation;
    }
    
    public void setPronunciation(String _pronunciation) {
        pronunciation = _pronunciation;
    }

    public boolean equals(PronunciationNode test) {
        return (pronunciation.equals(test.getPronunciation())
                && value.equals(test.getValue()));
    }
    
    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        if (!(_node instanceof PronunciationNode)) {
            throw new ClassCastException("Object not of type PronunciationNode");
        }
        
        PronunciationNode node = (PronunciationNode) _node;
        
        this.setPronunciation(node.getPronunciation());
        this.setValue(node.getValue());
        this.setId(node.getId());
    }
}
