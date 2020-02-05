/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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

import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * A node representing one prior state of a language XML file
 * @author DThompson
 */
public class ReversionNode implements Comparable<ReversionNode> {
    private final byte[] value;
    private Instant saveTime;

    public ReversionNode(byte[] _value) {
        value = _value;
        saveTime = Instant.MIN;
        
        populateTimeFromDoc();
    }
    
    public ReversionNode(byte[] _value, Instant _saveTime) {
        value = _value;
        saveTime = _saveTime;
    }
    
    /**
     * Isolates lengthy process in individual thread
     */
    private void populateTimeFromDoc() {
        new Thread() {
            public void run() {
                saveTime = getLastSaveTimeFromRawDoc();
            }
        }.start();
    }
    
    private Instant getLastSaveTimeFromRawDoc() {
        Instant ret;
        
        try {
            InputStream is = new ByteArrayInputStream(value);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc;
            doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            Node timeNode = doc.getElementsByTagName(PGTUtil.DICTIONARY_SAVE_DATE).item(0);
            
            if (timeNode != null) {
                ret = Instant.parse(timeNode.getTextContent());
            } else {
                ret = Instant.MIN;
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            IOHandler.writeErrorLog(e);
            ret = Instant.MIN;
        }
        
        return ret;
    }
    
    @Override
    public String toString() {
        String ret = "saved: ";
        
        if (!saveTime.equals(Instant.MIN)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
            ret += formatter.format(saveTime);
        } else {
            ret += "<UNKNOWN TIME>";
        }
        
        return ret;
    }

    @Override
    public int compareTo(ReversionNode o) {
        // returns in reverse order
        return -this.saveTime.compareTo(o.saveTime);
    }
    
    public byte[] getValue () {
        return value;
    }
}
