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
package org.darisadesigns.polyglotlina;

import java.util.List;
import java.util.Map.Entry;

/**
 * This is a wrapper class for google bar charts.
 * @author DThompson
 */
public class PGoogleBarChart extends PGoogleChart {
    
    /**
     * @return the leftYAxisLabel
     */
    public String getLeftYAxisLabel() {
        return leftYAxisLabel;
    }

    /**
     * @param leftYAxisLabel the leftYAxisLabel to set
     */
    public void setLeftYAxisLabel(String leftYAxisLabel) {
        this.leftYAxisLabel = leftYAxisLabel;
    }

    /**
     * @return the rightYAxisLabel
     */
    public String getRightYAxisLabel() {
        return rightYAxisLabel;
    }

    /**
     * @param rightYAxisLabel the rightYAxisLabel to set
     */
    public void setRightYAxisLabel(String rightYAxisLabel) {
        this.rightYAxisLabel = rightYAxisLabel;
    }

    private String leftYAxisLabel = "";
    private String rightYAxisLabel = "";
    private String[] labels = {};
    private String conFontName = "";

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
                + "        var " + data;
        
        ret += " = new google.visualization.DataTable();\n"
                + "        " + data + ".addColumn('string', 'MyData');\n"
                + "        " + data + ".addColumn({'type': 'string', 'role': 'tooltip', 'p': {'html': true}});\n";
        
        for (String label : labels) {
            ret += "        " + data + ".addColumn('number', '" + label + "');\n";
        }
        
        ret += "        " + data + ".addRows([\n";
        
        for (Entry<String, List<Double>> e : chartVals.entrySet()) {
            String dataSet = "";
            
            dataSet = e.getValue().stream().map((datum) -> 
            {
                return Integer.toString(datum.intValue()) + ",";        
            }).reduce(dataSet, String::concat);
            
            dataSet = dataSet.substring(0, dataSet.length()-1); // remove trailing comma...
            
            ret += "          ['" + e.getKey() + "', createCustomHTMLContent('" + e.getKey() + "'," + dataSet + ")," + dataSet + "],\n";
        }
        ret = ret.substring(0, ret.length()-2); // remove trailing comma and \n...
        ret += "\n        ]);\n"
                + "\n"
                + "        var " + materialOptions + " = {\n"
                + "          width: 1300,\n"
                + "          title: '" + caption + "',\n"
                + "          hAxis: {\n"
                + "            textStyle: {\n"
                + "              fontName: '" + conFontName + "'\n"
                + "            }\n"
                + "          },\n"
                
                
                + "          series: {\n"
                + "            0: {targetAxisIndex : 0},\n"
                + "            1: {targetAxisIndex : 1}\n"
                + "          },\n"
                + "          vAxes: {\n"
                + "            // Adds titles to each axis.\n"
                + "            0: {title: '" + getLeftYAxisLabel() + "'}, // Left y-axis.\n"
                + "            1: {title: '" + getRightYAxisLabel() + "'} // Right y-axis.\n"
                + "          },\n"
                + "          focusTarget: 'category',\n"
                + "          tooltip: { isHtml: true }\n"
                + "        };\n"
                + "\n"
                + "        function drawMaterialChart() {\n"
                + "          var " + materialChart + " = new google.visualization.ColumnChart(document.getElementById('" + getFunctionName() + "'));\n"
                + "          " + materialChart + ".draw(" + data + ", " + materialOptions + ");\n"
                + "        }\n"
                + "\n"
                + "        drawMaterialChart();\n"
                + "    };\n\n"
                + "    function createCustomHTMLContent(label, val1, val2) {\n"
                + "      return \'<div style=\"padding:5px 5px 5px 5px;\">\'+\n"
                + "        \'Character: \' + '<span STYLE=\"font-family: \\'" + conFontName + "\\'\">' + label + '</span>' +\n"
                + "        '<br/>Starting: ' + val1 + '<br/>Count: ' + val2 +\n"
                + "        \'</td>\'\n"
                + "    }\n";

        return ret;
    }

    @Override
    public String getDisplayHTML() {
        return "<div id=\"" + this.getFunctionName() + "\" style=\"width: 800px; height: 500px;\"></div>";
    }

    /**
     * @param labels the labels to set
     */
    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    /**
     * @return the conFontName
     */
    public String getConFontName() {
        return conFontName;
    }

    /**
     * @param conFontName the conFontName to set
     */
    public void setConFontName(String conFontName) {
        this.conFontName = conFontName;
    }
}
