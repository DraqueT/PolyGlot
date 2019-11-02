/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Nodes;

import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.WebInterface;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class LogoNode extends DictNode {

    private int strokes = 0;
    private String notes = "";
    private Image logoGraph;
    private boolean isRadical = false;
    private String tmpRads = "";
    private String tmpReadingBuffer = "";
    protected List<LogoNode> radicals = new ArrayList<>();
    protected List<String> readings = new ArrayList<>();

    public LogoNode() {
        ImageIcon loadBlank = new ImageIcon(getClass().getResource(PGTUtil.EMPTY_LOGO_IMAGE));
        BufferedImage image = new BufferedImage(
                loadBlank.getIconWidth(),
                loadBlank.getIconHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics g = image.createGraphics();

        loadBlank.paintIcon(null, g, 0, 0);
        g.dispose();

        logoGraph = image;
    }

    public Image getLogoGraph() {
        return logoGraph;
    }

    public void setLogoGraph(Image _logoGraph) {
        logoGraph = _logoGraph;
    }

    public boolean isRadical() {
        return isRadical;
    }

    public void setRadical(boolean _isRadical) {
        isRadical = _isRadical;
    }

    public void setStrokes(int _strokes) {
        strokes = _strokes;
    }

    public Integer getStrokes() {
        return strokes;
    }

    public void setNotes(String _notes) {
        notes = _notes;
    }

    public String getNotes() {
        return notes;
    }

    public void addRadical(LogoNode radicalId) {
        if (!radicals.contains(radicalId)) {
            radicals.add(radicalId);
        }
    }

    public List<LogoNode> getRadicals() {
        return radicals;
    }

    public void setRadicals(List<LogoNode> _radicals) {
        radicals = _radicals;
    }

    public void addReading(String reading) {
        if (!readings.contains(reading)) {
            readings.add(reading);
        }
    }

    public void setReadings(List<String> _readings) {
        readings = _readings;
    }

    public List<String> getReadings() {
        return readings;
    }

    /**
     * Tests whether logoNode contains a reading
     *
     * @param _reading reading to test
     * @param ignoreCase whether to ignore case (found in core properties
     * manager)
     * @return true if contains reading, false otherwise
     */
    public boolean containsReading(String _reading, boolean ignoreCase) {
        Iterator<String> it = readings.iterator();

        while (it.hasNext()) {
            String curRead = it.next();

            if ((ignoreCase && curRead.equalsIgnoreCase(_reading))
                    || curRead.equals(_reading)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Tests (by name) whether logoNode contains radical
     *
     * @param radString name of radical to search for
     * @param ignoreCase whether to ignore case (found in core properties manage
     * @return true if contains radical, false otherwise
     */
    public boolean containsRadicalString(String radString, boolean ignoreCase) {
        Iterator<LogoNode> it = radicals.iterator();

        while (it.hasNext()) {
            LogoNode curNode = it.next();

            if ((ignoreCase && curNode.getValue().equalsIgnoreCase(radString))
                    || curNode.getValue().equals(radString)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets comma delimited string list of all radical IDs
     *
     * @return delimited list of radical IDs
     */
    public String getRadicalListString() {
        String ret = "";
        Iterator<LogoNode> it = radicals.iterator();

        while (it.hasNext()) {
            if (!ret.isEmpty()) {
                ret += ",";
            }

            ret += it.next().getId().toString();
        }

        return ret;
    }

    /**
     * For initial loading of data from file only. Consumes comma delimited list
     * of radical IDs to associate with once all other logographs are loaded
     * into memory
     *
     * @param _tmpRads list to load
     */
    public void setTmpRadEntries(String _tmpRads) {
        tmpRads = _tmpRads;
    }

    /**
     * Used when loading from file
     *
     * @return reading buffer
     */
    public String getReadingBuffer() {
        return tmpReadingBuffer;
    }

    /**
     * Used when loading from file
     *
     * @param _tmpReadingBuffer new buffer
     */
    public void setReadingBuffer(String _tmpReadingBuffer) {
        tmpReadingBuffer += _tmpReadingBuffer;
    }

    /**
     * inserts reading buffer and blanks value
     */
    public void insertReadingBuffer() {
        addReading(tmpReadingBuffer);
        tmpReadingBuffer = "";
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * after pass 1 file loading, this tells all logoNodes to load their
     * radicals
     *
     * @param nodeMap pass nodeMap from parent collection for reference
     * @throws java.lang.Exception on load error
     */
    public void loadRadicalRelations(Map<Integer, LogoNode> nodeMap) throws Exception {
        if (tmpRads.isEmpty()) {
            return;
        }

        String loadLog = "";
        
        String[] radIds = tmpRads.split(",");

        for (String radId : radIds) {
            try {
                int nodeId = Integer.parseInt(radId);
                addRadical(nodeMap.get(nodeId));
            } catch (NumberFormatException e) {
                loadLog += "\nlogograph error: " + e.getLocalizedMessage();
                // IOHandler.writeErrorLog(e);
            }
        }
        
        if (!loadLog.isEmpty()) {
            throw new Exception("Logograph load error(s):" + loadLog);
        }
    }

    @Override
    /**
     * Sets node equal to node passed in
     */
    public void setEqual(DictNode _node) throws ClassCastException {
        if (!(_node instanceof LogoNode)) {
            throw new ClassCastException("Object not of type LogoNode");
        }
        
        LogoNode setNode = (LogoNode) _node;
        radicals = setNode.radicals;
        readings = setNode.readings;
        logoGraph = setNode.getLogoGraph();
        notes = setNode.getNotes();
        value = setNode.value;
        strokes = setNode.getStrokes();
        id = setNode.getId();
    }
    
    public void writeXML(Document doc, Element rootElement) {
        Element logoElement = doc.createElement(PGTUtil.LOGOGRAPH_NODE_XID);
        Element node;

        node = doc.createElement(PGTUtil.LOGOGRAPH_ID_XID);
        node.appendChild(doc.createTextNode(this.getId().toString()));
        logoElement.appendChild(node);

        node = doc.createElement(PGTUtil.LOGOGRAPH_VALUE_XID);
        node.appendChild(doc.createTextNode(this.getValue()));
        logoElement.appendChild(node);

        node = doc.createElement(PGTUtil.LOGO_IS_RADICAL_XID);
        node.appendChild(doc.createTextNode(this.isRadical()? PGTUtil.TRUE :PGTUtil.FALSE));
        logoElement.appendChild(node);

        node = doc.createElement(PGTUtil.LOGO_NOTES_XID);
        node.appendChild(doc.createTextNode(WebInterface.archiveHTML(this.getNotes())));
        logoElement.appendChild(node);

        node = doc.createElement(PGTUtil.LOGO_RADICAL_LIST_XID);
        node.appendChild(doc.createTextNode(this.getRadicalListString()));
        logoElement.appendChild(node);

        node = doc.createElement(PGTUtil.LOGO_STROKES_XID);
        node.appendChild(doc.createTextNode(this.getStrokes().toString()));
        logoElement.appendChild(node);

        Iterator<String> readIt = this.getReadings().iterator();
        while (readIt.hasNext()) {
            String curReading = readIt.next();

            node = doc.createElement(PGTUtil.LOGO_READING_LIST_XID);
            node.appendChild(doc.createTextNode(curReading));
            logoElement.appendChild(node);
        }

        rootElement.appendChild(logoElement);
    }
}
