/*
 * Copyright (c) 2017-2019, DThompson
 * All rights reserved.
 *
 * Licensed under: MIT Licence
 * See LICENSE.TXT included with this code to read the full license agreement.

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

/**
 * This is a wrapper for HTML/js based pie charts that are made available by Google. Extends base google chart wrapping
 * class.
 *
 * @author DThompson
 */
public class PGooglePieChart extends PGoogleChart {

    public PGooglePieChart(String _caption) {
        super(_caption);
    }

    /**
     * Returns constructed HTML which will generate the visuals for the pie chart
     *
     * @return string value of HTML to insert into document
     */
    @Override
    public String getBuildHTML() {
        String ret = "function " + getFunctionName() + "() {\n"
                + "        let data = google.visualization.arrayToDataTable([\n"
                + "          ['Item', 'Value'],\n";

        ret = chartVals.entrySet().stream().map((curEntry) -> 
        {
            return "          ['" + curEntry.getKey() + "', "
                    + curEntry.getValue().get(0).toString() + "],\n";
        }).reduce(ret, String::concat);

        ret += "]);\n"
                + "\n"
                + "        let options = {\n"
                + "          title: '"
                + caption
                + "'\n"
                + "        };\n"
                + "\n"
                + "        var chart = new google.visualization.PieChart(document.getElementById('"
                + getFunctionName() + "'));\n"
                + "\n"
                + "        chart.draw(data, options);\n"
                + "      }\n";

        return ret;
    }

    @Override
    public String getDisplayHTML() {
        return "    <div id=\"" + getFunctionName() + "\" style=\"width: 900px; height: 500px;\"></div>\n";
    }
}
