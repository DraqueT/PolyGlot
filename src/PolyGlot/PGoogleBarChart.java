/*
 * Copyright (c) 2017, DThompson
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
package PolyGlot;

import java.util.List;
import java.util.Map.Entry;

/**
 * This is a wrapper class for google bar charts.
 * @author DThompson
 */
public class PGoogleBarChart extends PGoogleChart {

    private String subCaption = "";

    public PGoogleBarChart(String _caption) {
        super(_caption);
    }

    /**
     * To use: The first added set should have all of the labels for the values
     * of each set of columns, but no values. The first label in this first set
     * will be placed below the graph. Each subsequent set should have a single
     * label value and one value for each column of the set.
     *
     * For each addition, if the first added set has n labels, then each
     * subsequent set should have 1 label and n - 1 values. It will not display
     * otherwise.
     *
     * @param labels
     * @param values
     */
    @Override
    public void addVal(String[] labels, Double[] values) {
        super.addVal(labels, values);
    }

    @Override
    public String getBuildHTML() {
        String chartDiv = "chartDiv" + this.getFunctionName();
        String data = "data" + this.getFunctionName();
        String materialOptions = "materialOptions" + this.getFunctionName();
        String materialChart = "materialChart" + this.getFunctionName();
        String ret = "function " + this.getFunctionName() + "() {\n"
                + "\n"
                + "        var " + chartDiv + " = document.getElementById('" + this.getFunctionName() + "');\n"
                + "\n"
                + "        var " + data + " = google.visualization.arrayToDataTable([\n";
        for (Entry<String, List<Double>> e : chartVals.entrySet()) {
            ret += "[";
            
            for(String entLabel : e.getKey().split("-")) {
                ret += "'" + entLabel + "',";
            }
            
            for(Double dub : e.getValue()) {
                ret += "'" + dub.toString() + "',";
            }
            
            // remove trailing comma
            ret = ret.substring(0, ret.length()-1);
            
            ret += "],";

        }
        
        ret = ret.substring(0, ret.length()-1) + '\n';
        
        ret += "        ]);\n"
                + "\n"
                + "        var " + materialOptions + " = {\n"
                + "          width: 900,\n"
                + "          chart: {\n"
                + "            title: '" + caption + "',\n"
                + "            subtitle: '" + subCaption + "'\n"
                + "          },\n"
                + "          series: {\n"
                + "            0: { axis: 'distance' }, // Bind series 0 to an axis named 'distance'.\n"
                + "            1: { axis: 'brightness' } // Bind series 1 to an axis named 'brightness'.\n"
                + "          },\n"
                + "          axes: {\n"
                + "            y: {\n"
                + "              distance: {label: 'parsecs'}, // Left y-axis.\n"
                + "              brightness: {side: 'right', label: 'apparent magnitude'} // Right y-axis.\n"
                + "            }\n"
                + "          }\n"
                + "        };\n"
                + "\n"
                + "        function drawMaterialChart() {\n"
                + "          var " + materialChart + " = new google.charts.Bar(" + chartDiv + ");\n"
                + "          " + materialChart + ".draw(" + data + ", google.charts.Bar.convertOptions(" + materialOptions + "));\n"
                + "        }\n"
                + "\n"
                + "        drawMaterialChart();\n"
                + "    };";

        return ret;
    }

    @Override
    public String getDisplayHTML() {
        return "<div id=\"" + this.getFunctionName() + "\" style=\"width: 800px; height: 500px;\"></div>";
    }

    /**
     * @return the subCaption
     */
    public String getSubCaption() {
        return subCaption;
    }

    /**
     * @param subCaption the subCaption to set
     */
    public void setSubCaption(String subCaption) {
        this.subCaption = subCaption;
    }
}
