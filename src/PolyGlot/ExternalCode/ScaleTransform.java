/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
package PolyGlot.ExternalCode;

import java.awt.geom.AffineTransform;
import javax.swing.text.View;

public class ScaleTransform implements TextShapeTransform {
    private float yScale=1;
    private float y=0;
    private float ascent =0;

    public ScaleTransform(float yScale) {
        this.setYScale(yScale);
    }
    
    public AffineTransform getTransform(View gv, AffineTransform original) {
        AffineTransform newTr=new AffineTransform();
        newTr.translate(0, -(getY()- ascent+3)*getYScale());
        newTr.scale(1, getYScale());
        newTr.translate(0, (getY()-ascent+3)/getYScale());

        return newTr;
    }
    
    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getYScale() {
        return yScale;
    }

    public void setYScale(float yScale) {
        this.yScale = yScale;
    }

    public float getAscent() {
        return ascent;
    }

    public void setAscent(float ascent) {
        this.ascent = ascent;
    }
}
