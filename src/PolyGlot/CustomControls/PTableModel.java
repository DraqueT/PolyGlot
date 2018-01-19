/*
 * Copyright (c) 2016-2018, Draque Thompson
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

import PolyGlot.Nodes.DictNode;
import javax.swing.table.DefaultTableModel;

/**
 * Obsolete collection used because that's what underlying class API is built on...
 * @author draque.thompson
 */
public class PTableModel extends DefaultTableModel{
    public PTableModel(Object[] columnNames, int rowCount) {
        super(convertToVector(columnNames), rowCount);
    }
    
    @Override
    @SuppressWarnings("UseOfObsoleteCollectionType")
    public void setValueAt(Object aValue, int row, int column) {
        java.util.Vector rowVector = (java.util.Vector)dataVector.elementAt(row);
        DictNode node = (DictNode)rowVector.get(column);
        node.setValue(aValue.toString());
        fireTableCellUpdated(row, column);
    }
}
