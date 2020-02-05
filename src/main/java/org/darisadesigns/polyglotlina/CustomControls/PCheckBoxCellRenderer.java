/*
 * Copyright (c) 2019, Draque Thopmson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.CustomControls;

import java.awt.Component;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

/**
 *
 * @author draque
 * @param <E>
 */
public class PCheckBoxCellRenderer<E extends PCheckableItem> implements ListCellRenderer<E> {

    private final JLabel label;
    private final PCheckBox check;

    public PCheckBoxCellRenderer(boolean nightMode, double fontSize) {
        label = new JLabel(" ");
        check = new PCheckBox(nightMode, fontSize);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        if (index < 0) {
            String txt = getCheckedItemString(list.getModel());
            label.setText(txt.isEmpty() ? " " : txt);
            return label;
        } else {
            check.setText(Objects.toString(value, ""));
            check.setSelected(value.isSelected());
            if (isSelected) {
                check.setBackground(list.getSelectionBackground());
                check.setForeground(list.getSelectionForeground());
            } else {
                check.setBackground(list.getBackground());
                check.setForeground(list.getForeground());
            }
            return check;
        }
    }

    private static <E extends PCheckableItem> String getCheckedItemString(ListModel<E> model) {
        return IntStream.range(0, model.getSize())
                .mapToObj(model::getElementAt)
                .filter(PCheckableItem::isSelected)
                .map(Objects::toString)
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
