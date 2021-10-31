/*
 * Copyright (c) 2014-2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Nodes;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.WebInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class LogoNode extends DictNode {

    private int strokes = 0;
    private String notes = "";
    private byte[] logoBytes;
    private boolean isRadical = false;
    private String tmpRads = "";
    private String tmpReadingBuffer = "";
    private DictCore core;
    protected List<LogoNode> radicals = new ArrayList<>();
    protected List<String> readings = new ArrayList<>();

    public LogoNode(DictCore _core) {
        core = _core;
        try {
            logoBytes = _core.getOSHandler().getIOHandler().loadImageBytes(PGTUtil.EMPTY_LOGO_IMAGE);
        }
        catch (IOException ex) {
            _core.getOSHandler().getIOHandler().writeErrorLog(ex);
        }
    }
    
    public byte[] getLogoBytes() {
        return logoBytes;
    }
    
    public void setLogoBytes(byte[] _logoBytes) {
        logoBytes = _logoBytes;
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

    public LogoNode[] getRadicals() {
        return radicals.toArray(new LogoNode[0]);
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

    public String[] getReadings() {
        return readings.toArray(new String[0]);
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
        radicals = new ArrayList<>(setNode.radicals);
        readings = new ArrayList<>(setNode.readings);
        logoBytes = setNode.logoBytes;
        notes = setNode.notes;
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
        node.appendChild(doc.createTextNode(this.isRadical ? PGTUtil.TRUE : PGTUtil.FALSE));
        logoElement.appendChild(node);

        node = doc.createElement(PGTUtil.LOGO_NOTES_XID);
        node.appendChild(doc.createTextNode(WebInterface.archiveHTML(this.notes, core)));
        logoElement.appendChild(node);

        node = doc.createElement(PGTUtil.LOGO_RADICAL_LIST_XID);
        node.appendChild(doc.createTextNode(this.getRadicalListString()));
        logoElement.appendChild(node);

        node = doc.createElement(PGTUtil.LOGO_STROKES_XID);
        node.appendChild(doc.createTextNode(this.getStrokes().toString()));
        logoElement.appendChild(node);

        Iterator<String> readIt = this.readings.iterator();
        while (readIt.hasNext()) {
            String curReading = readIt.next();

            node = doc.createElement(PGTUtil.LOGO_READING_LIST_XID);
            node.appendChild(doc.createTextNode(curReading));
            logoElement.appendChild(node);
        }

        rootElement.appendChild(logoElement);
    }

    @Override
    public boolean equals(Object comp) {
        boolean ret = false;

        if (this == comp) {
            ret = true;
        } else if (comp instanceof LogoNode) {
            LogoNode c = (LogoNode) comp;

            ret = value.equals(c.value);
            ret = ret && notes.equals(c.notes);
            ret = ret && Arrays.equals(logoBytes, c.logoBytes);
            ret = ret && isRadical == c.isRadical;
            ret = ret && radicals.equals(c.radicals);
            ret = ret && readings.equals(c.readings);
        }

        return ret;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
