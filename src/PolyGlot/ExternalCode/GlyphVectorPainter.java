/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
package PolyGlot.ExternalCode;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.lang.reflect.Field;
import javax.swing.text.BadLocationException;
import javax.swing.text.GlyphView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.TabExpander;
import javax.swing.text.View;

public class GlyphVectorPainter extends GlyphView.GlyphPainter {
    public static final String KEY_KERNING="KEY_KERNING";
    public static final String KEY_OPPOSITE_ITALIC="KEY_OPPOSITE_ITALIC";
    public static final String KEY_SCALE_TRANSFORM="KEY_SCALE_TRANSFORM";

    protected GlyphVector glyphVector;
    protected GlyphVector spaceVector;
    protected String text;
    protected Font font;
    protected LineMetrics lm;
    protected boolean isOppositeItalic=false;
    protected boolean isScale=false;
    TextShapeTransform transform=null;

    public GlyphVectorPainter(String text, GlyphView v) {
        if (v!=null) {
            this.text=text;
            this.font=v.getFont();
            init(v);
        }
    }
    
    void sync(GlyphView v) {
        try {
            String localText=v.getDocument().getText(v.getStartOffset(), v.getEndOffset()-v.getStartOffset());
            if (!localText.equals(text)) {
                this.text=localText;
                init(v);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void init(GlyphView v) {
        AffineTransform transform = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
        FontRenderContext frc = new FontRenderContext(transform, true, true);
        lm = font.getLineMetrics(text.toCharArray(), 0, text.toCharArray().length, frc);

        glyphVector=font.layoutGlyphVector(frc,text.toCharArray(), 0, text.length(), 0);
        checkKerning(v);
        checkOppositeItalic(v);
        checkScale(v);
        spaceVector=font.layoutGlyphVector(frc," ".toCharArray(), 0, 1, 0);
    }

    private void checkKerning(GlyphView v) {
        Float kValue=(Float)v.getElement().getAttributes().getAttribute(KEY_KERNING);
        if (kValue!=null) {
            for (int i=glyphVector.getNumGlyphs()-1; i>=0; i--) {
                Point2D p=glyphVector.getGlyphPosition(i);
                p.setLocation(p.getX()+i*kValue.floatValue(), p.getY());
                glyphVector.setGlyphPosition(i, p);
            }
        }
    }

    private void checkOppositeItalic(GlyphView v) {
        TextShapeTransform value=(TextShapeTransform)v.getElement().getAttributes().getAttribute(KEY_OPPOSITE_ITALIC);
        if (value!=null) {
            isOppositeItalic=true;
            transform=value;
        }
    }

    private void checkScale(GlyphView v) {
        TextShapeTransform value=(TextShapeTransform)v.getElement().getAttributes().getAttribute(KEY_SCALE_TRANSFORM);
        if (value!=null) {
            isScale=true;
            transform=value;
        }
    }

    public GlyphView.GlyphPainter getPainter(GlyphView v, int p0, int p1) {
        try {
            String localText=v.getDocument().getText(p0, p1-p0);
            return new GlyphVectorPainter(localText, v);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Determine the span the glyphs given a start location
     * (for tab expansion).
     */
    public float getSpan(GlyphView v, int p0, int p1,
                         TabExpander e, float x) {
        try {
            sync(v);
            String text = v.getDocument().getText(p0,p1-p0);
            int[] justificationData = getJustificationData(v);
            float width = getTabbedTextWidth(v, text, x, e, p0, justificationData);
            return width;
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

        return 0;
    }

    public float getHeight(GlyphView v) {
        sync(v);
        return (lm.getHeight())*getYScale();
    }

    /**
     * Fetches the ascent above the baseline for the glyphs
     * corresponding to the given range in the model.
     */
    public float getAscent(GlyphView v) {
        sync(v);
        return (lm.getAscent())*getYScale();
    }

    /**
     * Fetches the descent below the baseline for the glyphs
     * corresponding to the given range in the model.
     */
    public float getDescent(GlyphView v) {
        sync(v);
        return (lm.getDescent())*getYScale();
    }

    /**
     * Paints the glyphs representing the given range.
     */
    public void paint(GlyphView v, Graphics g, Shape a, int p0, int p1) {
        try {
            sync(v);

            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            String text;
            TabExpander expander = v.getTabExpander();
            Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();

            // determine the x coordinate to render the glyphs
            int x = alloc.x;
            int p = v.getStartOffset();
            int[] justificationData = getJustificationData(v);
            if (p != p0) {
                text = v.getDocument().getText(p, p0-p);
                float width = getTabbedTextWidth(v, text, x, expander, p, justificationData);
                x += width;
            }

            // determine the y coordinate to render the glyphs
            int y = alloc.y + Math.round(lm.getHeight() - lm.getDescent());

            // render the glyphs
            text = v.getDocument().getText(p0, p1-p0);
            g.setFont(font);

            if( p0 > v.getStartOffset() || p1 < v.getEndOffset() ) {
                Shape s = v.modelToView(p0, Position.Bias.Forward,
                                        p1, Position.Bias.Backward, a);
                Shape savedClip = g.getClip();
                ((Graphics2D)g).clip(s);
                x=v.modelToView(v.getStartOffset(), a, Position.Bias.Forward).getBounds().x;
                drawTabbedText(v, text, x, y, g, expander,p0, justificationData);
                g.setClip(savedClip);
            }
            else {
                drawTabbedText(v, text, x, y, g, expander,p0, justificationData);                
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public Shape modelToView(GlyphView v, int pos, Position.Bias bias,
                             Shape a) throws BadLocationException {
        sync(v);
        Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
        int p0 = v.getStartOffset();
        int p1 = v.getEndOffset();
        TabExpander expander = v.getTabExpander();
        String text;

        if(pos == p1) {
            // The caller of this is left to right and borders a right to
            // left view, return our end location.
            return new Rectangle(alloc.x + alloc.width, alloc.y, 0, (int)lm.getHeight());
        }
        if ((pos >= p0) && (pos <= p1)) {
            // determine range to the left of the position
            text = v.getDocument().getText(p0, pos-p0);
            int[] justificationData = getJustificationData(v);
            int width = (int)getTabbedTextWidth(v, text, alloc.x, expander, p0, justificationData);
            return new Rectangle(alloc.x + width, alloc.y, 0, (int)getHeight(v));
        }
        throw new BadLocationException("modelToView - can't convert", p1);
    }

    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model.
     *
     * @param v the view containing the view coordinates
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param a the allocated region to render into
     * @param biasReturn always returns <code>Position.Bias.Forward</code>
     *   as the zero-th element of this array
     * @return the location within the model that best represents the
     *  given point in the view
     * @see View#viewToModel
     */
    public int viewToModel(GlyphView v, float x, float y, Shape a,
                           Position.Bias[] biasReturn) {
        try {
            sync(v);
            Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
            int p0 = v.getStartOffset();
            int p1 = v.getEndOffset();
            TabExpander expander = v.getTabExpander();
            String text = v.getDocument().getText(p0, p1-p0);
            int[] justificationData = getJustificationData(v);
            int offs = getTabbedTextOffset(v, text, alloc.x, (int) x, expander, p0, true,justificationData);
            int retValue = p0 + offs;
            if(retValue == p1) {
                // No need to return backward bias as GlyphPainter1 is used for
                // ltr text only.
                retValue--;
            }
            biasReturn[0] = Position.Bias.Forward;
            return retValue;
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Determines the best location (in the model) to break
     * the given view.
     * This method attempts to break on a whitespace
     * location.  If a whitespace location can't be found, the
     * nearest character location is returned.
     *
     * @param v the view
     * @param p0 the location in the model where the
     *  fragment should start its representation >= 0
     * @param x the graphic location along the axis that the
     *  broken view would occupy >= 0; this may be useful for
     *  things like tab calculations
     * @param len specifies the distance into the view
     *  where a potential break is desired >= 0
     * @return the model location desired for a break
     * @see View#breakView
     */
    public int getBoundedPosition(GlyphView v, int p0, float x, float len) {
        try {
            sync(v);
            TabExpander expander = v.getTabExpander();
            String s = v.getDocument().getText(p0, v.getEndOffset()-p0);
            int[] justificationData = getJustificationData(v);
            int index = getTabbedTextOffset(v,s, (int)x, (int)(x+len), expander, p0, false, justificationData);
            int p1 = p0 + index;
            return p1;
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @SuppressWarnings("unchecked") // not my code - surpress
    private int[] getJustificationData(GlyphView v) {
        View parent = v.getParent();
        int [] ret = null;

        //use reflection to get the data
        Class pClass=parent.getClass();
        if (pClass.isAssignableFrom(ParagraphView.class.getDeclaredClasses()[0])) {
            try {
                Field f=pClass.getDeclaredField("justificationData");
                if (f!=null) {
                    f.setAccessible(true);
                    ret=(int[])f.get(parent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    float getTabbedTextWidth(View view, String txtStr, float x,
                             TabExpander e, int startOffset,
                             int[] justificationData) {
        float nextX = x;
        char[] txt = txtStr.toCharArray();
        int txtOffset = 0;
        int n = txtStr.length();
        int charCount = 0;
        int spaceAddon = 0;
        int spaceAddonLeftoverEnd = -1;
        int startJustifiableContent = 0;
        int endJustifiableContent = 0;
        if (justificationData != null) {
            int offset = - startOffset + txtOffset;
            View parent = null;
            if (view != null && (parent = view.getParent()) != null) {
                offset += parent.getStartOffset();
            }
            spaceAddon =justificationData[0];
            spaceAddonLeftoverEnd =justificationData[1] + offset;
            startJustifiableContent =justificationData[2] + offset;
            endJustifiableContent =justificationData[3] + offset;
        }

        for (int i = txtOffset; i < n; i++) {
            if (txt[i] == '\t'
                    || ((spaceAddon != 0 || i <= spaceAddonLeftoverEnd)
                    && (txt[i] == ' ')
                    && startJustifiableContent <= i
                    && i <= endJustifiableContent
            )) {
                nextX += (glyphVector.getGlyphPosition(i).getX()-glyphVector.getGlyphPosition(i-charCount).getX());
                charCount = 0;
                if (txt[i] == '\t') {
                    if (e != null) {
                        nextX = e.nextTabStop(nextX, startOffset + i - txtOffset);
                    } else {
                        nextX += (spaceVector.getGlyphPosition(1).getX());
                    }
                } else if (txt[i] == ' ') {
                    nextX += (spaceVector.getGlyphPosition(1).getX()) + spaceAddon;
                    if (i <= spaceAddonLeftoverEnd) {
                        nextX++;
                    }
                }
            } else if(txt[i] == '\n') {
                // Ignore newlines, they take up space and we shouldn't be counting them.
                nextX += (glyphVector.getGlyphPosition(i).getX()-glyphVector.getGlyphPosition(i-charCount).getX());
                charCount = 0;
            } else {
                charCount++;
            }
        }

        nextX += (glyphVector.getGlyphPosition(n).getX()-glyphVector.getGlyphPosition(n-charCount).getX());
        return nextX - x;
    }

    float drawTabbedText(View view, String txtStr, float x, float y, Graphics g,
                         TabExpander e, int startOffset, int [] justificationData) {
        float nextX = x;
        char[] txt = txtStr.toCharArray();
        int txtOffset = 0;
        int flushLen = 0;
        int flushIndex = txtStr.length();
        int spaceAddon = 0;
        int spaceAddonLeftoverEnd = -1;
        int startJustifiableContent = 0;
        int endJustifiableContent = 0;
        if (justificationData != null) {
            int offset = - startOffset + txtOffset;
            View parent = null;
            if (view != null && (parent = view.getParent()) != null) {
                offset += parent.getStartOffset();
            }
            spaceAddon =justificationData[0];
            spaceAddonLeftoverEnd =justificationData[1] + offset;
            startJustifiableContent =justificationData[2] + offset;
            endJustifiableContent =justificationData[3] + offset;
        }
        int n = txtStr.length();
        for (int i = txtOffset; i < n; i++) {
            if (txt[i] == '\t'
                    || ((spaceAddon != 0 || i <= spaceAddonLeftoverEnd)
                    && (txt[i] == ' ')
                    && startJustifiableContent <= i
                    && i <= endJustifiableContent
            )) {
                if (flushLen > 0) {
                    AffineTransform old=((Graphics2D)g).getTransform();
                    if (isOppositeItalic) {
                        ((OppositeItalicTransform)transform).setY(y);
                    }
                    else if (isScale) {
                        ((ScaleTransform)transform).setY(y);
                        ((ScaleTransform)transform).setAscent(lm.getAscent());
                    }
                    if (transform!=null) {
                        ((Graphics2D)g).setTransform(transform.getTransform(view, old));
                    }
                    ((Graphics2D)g).drawGlyphVector(glyphVector,x,y);
                    ((Graphics2D)g).setTransform(old);
                    //corrected position
                    nextX += (glyphVector.getGlyphPosition(flushIndex+flushLen).getX()-glyphVector.getGlyphPosition(flushIndex).getX());
                    flushLen = 0;
                }
                flushIndex = i + 1;
                if (txt[i] == '\t') {
                    if (e != null) {
                        nextX = e.nextTabStop((float) nextX, startOffset + i - txtOffset);
                    } else {
                        nextX += (spaceVector.getGlyphPosition(1).getX());
                    }
                } else if (txt[i] == ' ') {
                    nextX += (spaceVector.getGlyphPosition(1).getX()+spaceAddon);
                    if (i <= spaceAddonLeftoverEnd) {
                        nextX++;
                    }
                }
                x = nextX;
            } else if ((txt[i] == '\n') || (txt[i] == '\r')) {
                if (flushLen > 0) {
                    AffineTransform old=((Graphics2D)g).getTransform();
                    if (isOppositeItalic) {
                        ((OppositeItalicTransform)transform).setY(y);
                    }
                    else if (isScale) {
                        ((ScaleTransform)transform).setY(y);
                        ((ScaleTransform)transform).setAscent(lm.getAscent());
                    }
                    if (transform!=null) {
                        ((Graphics2D)g).setTransform(transform.getTransform(view, old));
                    }
                    ((Graphics2D)g).drawGlyphVector(glyphVector,x,y);
                    ((Graphics2D)g).setTransform(old);
                    //corrected
                    nextX += (glyphVector.getGlyphPosition(flushLen).getX()-glyphVector.getGlyphPosition(flushIndex).getX());
                    flushLen = 0;
                }
                flushIndex = i + 1;
                x = nextX;
            } else {
                flushLen += 1;
            }
        }
        if (flushLen > 0) {
            AffineTransform old=((Graphics2D)g).getTransform();
            if (isOppositeItalic) {
                ((OppositeItalicTransform)transform).setY(y);
            }
            else if (isScale) {
                ((ScaleTransform)transform).setY(y);
                ((ScaleTransform)transform).setAscent(lm.getAscent());
            }
            if (transform!=null) {
                ((Graphics2D)g).setTransform(transform.getTransform(view, old));
            }
            ((Graphics2D)g).drawGlyphVector(glyphVector,x,y);
            ((Graphics2D)g).setTransform(old);

            //corrected
            nextX += (glyphVector.getGlyphPosition(flushLen).getX()-glyphVector.getGlyphPosition(flushIndex).getX());
        }
        return nextX;
    }

    int getTabbedTextOffset(View view,
                             String s,
                             int x0, int x, TabExpander e,
                             int startOffset,
                             boolean round,
                             int[] justificationData) {
        if (x0 >= x) {
            // x before x0, return.
            return 0;
        }
        float currX = x0;
        float nextX = currX;
        // s may be a shared String, so it is copied prior to calling
        // the tab expander
        char[] txt = s.toCharArray();
        int txtOffset = 0;
        int txtCount = s.length();
        int spaceAddon = 0 ;
        int spaceAddonLeftoverEnd = -1;
        int startJustifiableContent = 0 ;
        int endJustifiableContent = 0;
        if (justificationData != null) {
            int offset = - startOffset + txtOffset;
            View parent = null;
            if (view != null && (parent = view.getParent()) != null) {
                offset += parent.getStartOffset();
            }
            spaceAddon =justificationData[0];
            spaceAddonLeftoverEnd =justificationData[1] + offset;
            startJustifiableContent =justificationData[2] + offset;
            endJustifiableContent =justificationData[3] + offset;
        }
        int n = s.length();
        for (int i = 0; i < n; i++) {
            if (txt[i] == '\t'
                    || ((spaceAddon != 0 || i <= spaceAddonLeftoverEnd)
                    && (txt[i] == ' ')
                    && startJustifiableContent <= i
                    && i <= endJustifiableContent
            )){
                if (txt[i] == '\t') {
                    if (e != null) {
                        nextX = (int) e.nextTabStop((float) nextX,
                                startOffset + i - txtOffset);
                    } else {
                        nextX += (spaceVector.getGlyphPosition(1).getX());
                    }
                } else if (txt[i] == ' ') {
                    nextX += (spaceVector.getGlyphPosition(1).getX())+spaceAddon;

                    if (i <= spaceAddonLeftoverEnd) {
                        nextX++;
                    }
                }
            } else {
                nextX += (glyphVector.getGlyphPosition(i+1).getX()-glyphVector.getGlyphPosition(i).getX());
            }
            if ((x >= currX) && (x < nextX)) {
                // found the hit position... return the appropriate side
                if ((round == false) || ((x - currX) < (nextX - x))) {
                    return i - txtOffset;
                } else {
                    return i + 1 - txtOffset;
                }
            }
            currX = nextX;
        }

        // didn't find, return end offset
        return txtCount;
    }

    protected float getYScale() {
        if (isScale) {
            return ((ScaleTransform)transform).getYScale();
        }
        return 1;
    }
}
