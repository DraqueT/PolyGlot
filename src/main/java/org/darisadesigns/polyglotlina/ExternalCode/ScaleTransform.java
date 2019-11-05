/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
package org.darisadesigns.polyglotlina.ExternalCode;

import java.awt.geom.AffineTransform;
import javax.swing.text.View;

public final class ScaleTransform implements TextShapeTransform {
    private float yScale=1;
    private float y=0;
    private float ascent =0;

    public ScaleTransform(float yScale) {
        this.yScale = yScale;
    }
    
    public AffineTransform getTransform(View gv, AffineTransform original) {
        AffineTransform newTr=new AffineTransform();
        newTr.translate(0, -(y - ascent+3)* yScale);
        newTr.scale(1, yScale);
        newTr.translate(0, (y -ascent+3)/ yScale);

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
