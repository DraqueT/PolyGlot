/*
 * Copyright (c) 2018, DThompson
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
package PolyGlot.Nodes;

import PolyGlot.DictCore;
import PolyGlot.IOHandler;
import PolyGlot.ManagersCollections.ReversionManager;
import PolyGlot.PGTUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public final byte[] value;
    public Instant saveTime;
    public final ReversionManager parent;
        
    public ReversionNode(byte[] _value, ReversionManager _parent) {
        value = _value;
        parent = _parent;
    }
    
    @Override
    public String toString() {
        String ret = "saved: ";
        
        try {
            // First time load for these, the saveTime won't be populated...
            if (saveTime.equals(Instant.MIN)) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document tmpDoc = builder.parse(new ByteArrayInputStream(value));

                Node saveNode = tmpDoc.getElementsByTagName(PGTUtil.dictionarySaveDate).item(0);
                saveTime = Instant.parse(saveNode.getTextContent());
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
                ret += formatter.format(saveTime);
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            IOHandler.writeErrorLog(e);
            ret += "<UNKNOWN TIME>";
        }
        
        return ret;
    }

    @Override
    public int compareTo(ReversionNode o) {
        // returns in reverse order
        return -this.saveTime.compareTo(o.saveTime);
    }
}
