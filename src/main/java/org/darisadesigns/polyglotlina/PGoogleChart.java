/*
 * Copyright (c) 2017-2019, DThompson
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a wrapper class for Google charts. Other specific classes implement
 * particular charts, like pie, bar, graph, etc.
 * @author DThompson
 */
public abstract class PGoogleChart {
    protected final String caption;
    protected final Map<String, List<Double>> chartVals;
    
    
    public abstract String getBuildHTML();
    
    public abstract String getDisplayHTML();

    /**
     * Creates chart object. Caption is what chart will be labeled.
     *
     * @param _caption caption for piechart
     */
    public PGoogleChart(String _caption) {
        caption = _caption;
        chartVals = new HashMap<>();
    }

    /**
     * Adds item to chart. Number is weight of slice, label is its label
     *
     * @param labels label(s)
     * @param values value(s)
     */
    public void addVal(String[] labels, Double[] values) {
        String key = String.join("-", labels);
        if (chartVals.containsKey(key)) {
            chartVals.replace(key, Arrays.asList(values));
        } else {
            chartVals.put(key, Arrays.asList(values));
        }
    }

    /**
     * Gets name of function to draw chart
     *
     * @return function name without parentheses
     */
    public String getFunctionName() {
        return caption.replace(" ", "");
    }
}
