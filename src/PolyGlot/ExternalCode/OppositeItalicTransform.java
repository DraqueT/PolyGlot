/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
package PolyGlot.ExternalCode;

import java.awt.geom.AffineTransform;
import javax.swing.text.View;

public class OppositeItalicTransform implements TextShapeTransform {
    private float y=0;
    public AffineTransform getTransform(View gv, AffineTransform original) {
        AffineTransform newTr=new AffineTransform(original);
        newTr.translate(0, getY());
        newTr.shear(0.3,0);
        newTr.translate(2,-getY());

        return newTr;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
