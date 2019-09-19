/*
 * Copyright (c) 2017-2019, Draque Thompson
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
package org.darisadesigns.polyglotlina.CustomControls;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.RectangularCoordinateMap;
import org.darisadesigns.polyglotlina.WebInterface;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author DThompson
 */
public final class PPanelDrawEtymology extends JPanel {

    private final DictCore core;
    private final EtymologyPrintingNode myWordPosition;
    private final Map<Integer, Integer> columnWidth = new HashMap<>();
    private FontMetrics conFontMetrics;
    private FontMetrics charisFontMetrics;
    private int curYDepth = 0;
    private int lowestDepth = 0;
    private final int xWordSpaceBuffer = 20;
    private RectangularCoordinateMap<ConWord> wordMap = new RectangularCoordinateMap<>();

    public PPanelDrawEtymology(DictCore _core, ConWord _word) {
        super();
        core = _core;
        myWordPosition = new EtymologyPrintingNode();
        myWordPosition.word = _word;
        this.setToolTipText("");
    }
    
    @Override
    public String getToolTipText(MouseEvent event) {
        ConWord tipWord = wordMap.getObjectAtLocation(event.getX(), event.getY());
        String ret;
        
        if (tipWord != null) {
            ret = WebInterface.getTextFromHtml(tipWord.getDefinition());
            ret = ret.trim().isEmpty() ? tipWord.getLocalWord() : ret;
        } else {
            ret = super.getToolTipText(event);
        }
        
        return ret;
    }

    /**
     * Builds the etymology tree that the graphic representation object consumes
     */
    private void buildEtTree() {
        addEtTreeParents(myWordPosition);
        addEtTreeChildren(myWordPosition, myWordPosition.depth);
        columnWidth.clear();
        calcColumnWidth();
    }

    /**
     * Calculates how wide each visual column should be
     */
    private void calcColumnWidth() {
        calcColumnWidthChildren(myWordPosition);
        calcColumnWidthParents(myWordPosition);
    }

    /**
     * Adds column width from parents. Recursive.
     *
     * @param curNode
     */
    private void calcColumnWidthParents(EtymologyPrintingNode curNode) {
        Integer curDepth = curNode.depth;
        Integer mySize;
        
        if (curNode.isExternal ) {
            int wordSize = charisFontMetrics.stringWidth(curNode.extWordValue);
            int originSize = charisFontMetrics.stringWidth(curNode.extWordOrigin);
            mySize = (wordSize > originSize ? wordSize : originSize) + xWordSpaceBuffer;
        } else {
            mySize = conFontMetrics.stringWidth(curNode.word.getValue()) + xWordSpaceBuffer;
        }

        if (columnWidth.containsKey(curDepth)) {
            // the maximum width of each depth is saved, as each is one "column" in the visual display
            Integer depthSize = columnWidth.get(curDepth);
            if (mySize > depthSize) {
                columnWidth.replace(curDepth, mySize);
            }
        } else {
            columnWidth.put(curDepth, mySize);
        }

        curNode.parents.forEach((parentNode) -> {
            calcColumnWidthParents(parentNode);
        });
    }

    /**
     * Adds column width from children. Recursive.
     *
     * @param curNode
     */
    private void calcColumnWidthChildren(EtymologyPrintingNode curNode) {
        Integer curDepth = curNode.depth;
        Integer mySize = conFontMetrics.stringWidth(curNode.word.getValue()) + xWordSpaceBuffer;

        if (columnWidth.containsKey(curDepth)) {
            // the maximum width of each depth is saved, as each is one "column" in the visual display
            Integer depthSize = columnWidth.get(curDepth);
            if (mySize > depthSize) {
                columnWidth.replace(curDepth, mySize);
            }
        } else {
            columnWidth.put(curDepth, mySize);
        }

        curNode.children.forEach((childNode) -> {
            calcColumnWidthChildren(childNode);
        });
    }

    /**
     * Builds parent nodes Recursive. populates depth of node handed to it
     *
     * @param curNode node to populate parentage of
     */
    private void addEtTreeParents(EtymologyPrintingNode curNode) {
        core.getEtymologyManager().getWordParentsIds(curNode.word.getId()).forEach((curParentId) -> {
            EtymologyPrintingNode parentNode = new EtymologyPrintingNode();
            ConWord parentWord = core.getWordCollection().getNodeById(curParentId);
            parentNode.word = parentWord;
            parentNode.depth = curNode.depth - 1;
            addEtTreeParents(parentNode);
            parentNode.children.add(curNode);
            curNode.parents.add(parentNode);
        });
        
        // adds external parents
        core.getEtymologyManager().getWordExternalParents(curNode.word.getId()).forEach((extPar) -> {
            EtymologyPrintingNode parentNode = new EtymologyPrintingNode();
            parentNode.isExternal = true;
            parentNode.extWordValue = extPar.getExternalWord();
            if (!extPar.getExternalLanguage().isEmpty()) {
                parentNode.extWordOrigin = "(" + extPar.getExternalLanguage() + ")";
            }
            
            parentNode.depth = curNode.depth - 1;
            parentNode.children.add(curNode);
            curNode.parents.add(parentNode);
            lowestDepth = lowestDepth > curNode.depth-1 ? curNode.depth-1 : lowestDepth;
        });

        // make certain to update the lowest depth if needed
        lowestDepth = lowestDepth > curNode.depth ? curNode.depth : lowestDepth;

        // sort in order of node depth
        Collections.sort(curNode.children);
    }

    /**
     * Builds child nodes Recursive
     *
     * @param curNode node to populate children of
     */
    private void addEtTreeChildren(EtymologyPrintingNode curNode, int depth) {
        curNode.depth = depth;
        core.getEtymologyManager().getChildren(curNode.word.getId()).forEach((curChildId) -> {
            EtymologyPrintingNode childNode = new EtymologyPrintingNode();
            ConWord childWord = core.getWordCollection().getNodeById(curChildId);
            childNode.word = childWord;
            addEtTreeChildren(childNode, depth + 1);
            curNode.children.add(childNode);
            childNode.parents.add(curNode);
        });

        // sort in order of node depth
        Collections.sort(curNode.children);
    }

    /**
     * Given a graphical interface, prints the etymology tree visually
     */
    private void paintEt(Graphics g) {
        wordMap = new RectangularCoordinateMap<>();
        myWordPosition.children.clear();
        myWordPosition.parents.clear();
        conFontMetrics = g.getFontMetrics(
                core.getPropertiesManager().getFontCon());
        charisFontMetrics = g.getFontMetrics(
                core.getPropertiesManager().getFontLocal());
        curYDepth = conFontMetrics.getHeight();

        buildEtTree();
        int lastParentHeight = paintEtParents(myWordPosition, g, true);
        paintEtChildren(myWordPosition, g, 0, lastParentHeight, true);
    }

    /**
     * recursively prints parent nodes
     *
     * @param myNode current node
     * @param g graphics object
     * @param firstEntry whether this is the first entry (skip printing)
     * @return the line height of this entry as printed
     */
    private int paintEtParents(EtymologyPrintingNode myNode, Graphics g, boolean firstEntry) {
        int xOffset = 0;
        int topParentHeight = 0;
        int bottomParentHeight = 0;
        int halfTextHeight;
        int myLineHeight;
        
        // recursively print all parents 
        for (EtymologyPrintingNode parNode : myNode.parents) {
            int curLineHeight = paintEtParents(parNode, g, false);

            // record line positions of top/bottom parents for visual graphing
            if (topParentHeight == 0) {
                topParentHeight = curLineHeight;
            }
            bottomParentHeight = curLineHeight;
        }

        // only make offsets for parent nodes if they're not top level.
        if (!myNode.parents.isEmpty()) {
            xOffset = myNode.getDepthMeasurement();
        }

        // print vertical line connecting parents to children if it makes sense to do so
        if (topParentHeight != 0) {
            int middleHorizontal = xOffset - 5;

            int topLastParent = bottomParentHeight + conFontMetrics.getHeight();
            paintLine(new Dimension(middleHorizontal, topParentHeight),
                    new Dimension(middleHorizontal, topLastParent), g);
            paintLine(new Dimension(middleHorizontal, topLastParent),
                    new Dimension(xOffset - 2, topLastParent), g);
        }

        // the first entry (current word) will be displayed in blue.
        if (firstEntry) {
            g.setColor(Color.blue);
        }

        if (myNode.isExternal) {
            g.setFont(core.getPropertiesManager().getFontLocal());
            if (!myNode.extWordOrigin.isEmpty()) {
                g.setColor(Color.gray);
                g.drawString(myNode.extWordOrigin, xOffset, curYDepth);
                g.setColor(Color.black);
                curYDepth += (charisFontMetrics.getHeight() - 10);
            }
            g.drawString(myNode.extWordValue, xOffset, curYDepth);
            halfTextHeight = charisFontMetrics.getHeight() / 3;
            myLineHeight = curYDepth - halfTextHeight;
            curYDepth += charisFontMetrics.getHeight();
        } else {
            g.setFont(core.getPropertiesManager().getFontCon());
            g.drawString(myNode.word.getValue(), xOffset, curYDepth);
            halfTextHeight = conFontMetrics.getHeight() / 3;
            myLineHeight = curYDepth - halfTextHeight;
            
            try {
                wordMap.addRectangle(xOffset, xOffset + conFontMetrics.stringWidth(myNode.word.getValue()), 
                        curYDepth - conFontMetrics.getHeight(), curYDepth, myNode.word);
            } catch (Exception e) {
                IOHandler.writeErrorLog(e);
                InfoBox.error("Tooltip Generation error", "Error generating tooltip values: " 
                        + e.getLocalizedMessage(), core.getRootWindow());
            }
            curYDepth += conFontMetrics.getHeight();
        }
        
        g.setColor(Color.black);        

        // paint line leading to depth of child (guaranteed one or zero)
        if (!myNode.children.isEmpty()) {
            String myText = myNode.isExternal ? myNode.extWordValue : myNode.word.getValue();
            int childDepth = myNode.children.get(0).getDepthMeasurement();
            int xStart;
            if (myNode.isExternal) {
                xStart = xOffset + charisFontMetrics.stringWidth(myText);
            } else {
                xStart = xOffset + conFontMetrics.stringWidth(myText);
            }

            paintLine(new Dimension(xStart, myLineHeight),
                    new Dimension(childDepth - 5, myLineHeight), g);
        }

        return myLineHeight;
    }

    /**
     * recursively prints child nodes
     */
    private void paintEtChildren(EtymologyPrintingNode myNode, Graphics g,
            int xParentEnd, int yParentEnd, boolean firstEntry) {
        int xOffset = 0;
        int startYDepth = curYDepth - (conFontMetrics.getHeight() / 3);
        String myText = myNode.word.getValue();

        for (int i = lowestDepth; i < myNode.depth; i++) {
            xOffset += columnWidth.get(i);
        }

        // the first entry is not printed.
        if (!firstEntry) {
            g.setFont(core.getPropertiesManager().getFontCon());
            g.drawString(myNode.word.getValue(), xOffset, curYDepth);
            
            try {
                wordMap.addRectangle(xOffset, xOffset + conFontMetrics.stringWidth(myText), 
                        curYDepth - conFontMetrics.getHeight(), curYDepth, myNode.word);
            } catch (Exception e) {
                IOHandler.writeErrorLog(e);
                InfoBox.error("Tooltip Generation error", "Error generating tooltip values: " 
                        + e.getLocalizedMessage(), core.getRootWindow());
            }
            
            curYDepth += conFontMetrics.getHeight();

            // only paint connector lines if not first entry (covered otherwise)
            paintLine(new Dimension(xParentEnd, yParentEnd), new Dimension(xOffset - 5, yParentEnd), g);
            paintLine(new Dimension(xOffset - 5, yParentEnd), new Dimension(xOffset - 5, startYDepth), g);
            paintLine(new Dimension(xOffset - 5, startYDepth), new Dimension(xOffset - 2, startYDepth), g);
        } else {
            startYDepth = yParentEnd;
        }

        for (EtymologyPrintingNode parNode : myNode.children) {
            paintEtChildren(parNode, g, xOffset + conFontMetrics.charsWidth(
                    myText.toCharArray(), 0, myText.length()),
                    startYDepth, false);
        }
    }

    @Override
    /**
     * Unlike the normal paint, this removes all content before continuing
     */
    public void paintComponent(Graphics g) {
        this.removeAll();

        // turn on anti-alias mode
        Graphics2D antiAlias = (Graphics2D) g;
        antiAlias.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintEt(g);
    }

    /**
     * paints a blue line then returns color to previous state
     *
     * @param startPoint line start point
     * @param endPoint line end point
     * @param g graphics object to paint to
     */
    private void paintLine(Dimension startPoint, Dimension endPoint, Graphics g) {
        Color lastColor = g.getColor();
        g.setColor(Color.BLUE);
        g.drawLine(startPoint.width, startPoint.height, endPoint.width,
                endPoint.height);

        g.setColor(lastColor);
    }

    private class EtymologyPrintingNode implements Comparable<EtymologyPrintingNode> {

        public List<EtymologyPrintingNode> children = new ArrayList<>();
        public List<EtymologyPrintingNode> parents = new ArrayList<>();
        public ConWord word = new ConWord();
        public String extWordValue = "";
        public String extWordOrigin = "";
        public int depth = 0;
        public boolean isExternal = false;

        @Override
        public int compareTo(EtymologyPrintingNode o) {
            return Integer.valueOf(this.depth).compareTo(o.depth);
        }

        /**
         * Calculates actual graphical position of depth based on column widths
         *
         * @return
         */
        public int getDepthMeasurement() {
            int ret = 0;

            for (int i = lowestDepth; i < this.depth; i++) {
                ret += columnWidth.get(i);
            }

            return ret;
        }
    }
    
    /**
     * Generates and returns properly sized image of the etymology panel
     * Returns null if no etymology
     * @return buffered image of rendered panel, null if no etymology
     */
    public BufferedImage getPanelImage() {
        BufferedImage ret = null;
        JFrame temp = new JFrame();
        
        temp.getRootPane().add(this);
        temp.setVisible(true);
        paintComponent(temp.getGraphics());
        
        // only paint if greater than one (no etymology otherwise)
        if (columnWidth.size() > 1) {
            this.setSize(getCalcWidth(),getCalcHeight());             
            ret = new BufferedImage(getCalcWidth(),getCalcHeight(), BufferedImage.TYPE_INT_ARGB);
            paint(ret.getGraphics());
        }
        temp.setVisible(false);
        temp.dispose();
        
        return ret;
    }
    
    /**
     * Calculates and returns minimum appropriate height for panel
     * @return minimum height
     */
    private int getCalcWidth() {
        return columnWidth.values().stream().mapToInt(Number::intValue).sum();
    }
    
    /**
     * Calculates and returns minimum appropriate width for panel
     * @return minimum width
     */
    private int getCalcHeight() {
        return curYDepth;
    }
}
