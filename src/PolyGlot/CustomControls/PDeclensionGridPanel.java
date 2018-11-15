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
package PolyGlot.CustomControls;

import PolyGlot.Nodes.DeclensionNode;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This represents a dimensional grid pane for the representation of word forms.
 * It requires at least two dimensions in the language to work.
 * @author DThompson
 */
public class PDeclensionGridPanel extends JPanel {
    public PDeclensionGridPanel(String partialDeclensions, DeclensionNode xDim, DeclensionNode yDim) {
        this.add(new JLabel(partialDeclensions));
    }
}
