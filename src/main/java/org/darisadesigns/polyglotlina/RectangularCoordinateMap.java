/*
 * Copyright (c) 2018-2019, Draque Thompson
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
package org.darisadesigns.polyglotlina;

import java.util.ArrayList;
import java.util.List;

/**
 * This helps to map rectangles in an infinite grid. A point within the grid can be tested, and any value associated 
 * with the area will be returned.
 * @author DThompson
 * @param <K> Type of the rectangle target
 */
public class RectangularCoordinateMap<K> {
    private final List<Rectangle<K>> rectList = new ArrayList<>();
    
    /**
     * Adds rectangle to an infinite grid.
     * @param leftEdge
     * @param rightEdge
     * @param topEdge
     * @param bottomEdge
     * @param target target object of rectangle to add
     * @throws Exception If given rectangle overlaps existing one
     */
    public void addRectangle(int leftEdge, int rightEdge, int topEdge, int bottomEdge, K target) throws Exception {
        if (leftEdge >= rightEdge || topEdge >= bottomEdge) {
            throw new Exception("Right must be > than left; bottom must be > top.");
        }
        
        for (Rectangle rect : rectList) {
            if (rightEdge > rect.leftEdge
                     && leftEdge < rect.rightEdge
                     && topEdge > rect.bottomEdge
                     && bottomEdge < rect.topEdge) {
                 throw new Exception("Unable to add rectangle at given coordinates. Overlap detected.");
             }
        }
        
        rectList.add(new Rectangle<>(leftEdge, rightEdge, topEdge, bottomEdge, target));
    }
    
    /**
     * Fetches target of any rectangle and returns it, or null if none present
     * @param x x position to check
     * @param y y position to check
     * @return target object if any, null otherwise
     */
    public K getObjectAtLocation(int x, int y) {
        K ret = null;
        
        for (Rectangle<K> rect : rectList) {
            if (x >= rect.leftEdge 
                    && x <= rect.rightEdge
                    && y >= rect.topEdge
                    && y <= rect.bottomEdge) {
                ret = rect.target;
            } 
        }
        
        return ret;
    }
    
    private static class Rectangle<L> {
        public final int leftEdge;
        public final int rightEdge;
        public final int topEdge;
        public final int bottomEdge;
        public final L target;
        
        public Rectangle(int _leftEdge, int _rightEdge, int _topEdge, int _bottomEdge, L _target) {
            leftEdge = _leftEdge;
            rightEdge = _rightEdge;
            topEdge = _topEdge;
            bottomEdge = _bottomEdge;
            target = _target;
        }
    }
}