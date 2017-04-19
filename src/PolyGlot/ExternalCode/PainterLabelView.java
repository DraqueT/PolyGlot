/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
package PolyGlot.ExternalCode;

import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.LabelView;

public class PainterLabelView extends LabelView {
    static GlyphView.GlyphPainter defaultPainter;
    public PainterLabelView(Element elem) {
        super(elem);
    }

    protected void checkPainter() {
        if (getGlyphPainter() == null) {
            if (defaultPainter == null) {
                defaultPainter = new GlyphVectorPainter(null, null);
            }
            setGlyphPainter(defaultPainter.getPainter(this, getStartOffset(), getEndOffset()));
        }
    }
}
