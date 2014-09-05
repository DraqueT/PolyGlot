/*
 * Copyright (c) 2014, draque
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

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * This class handles all web communication to and from PolyGlot
 *
 * @author draque
 */
public class WebInterface {

    /**
     * Checks for updates to PolyGlot
     *
     * @return Entry containing the new version number and the associated message in String[2] array
     * @throws java.lang.Exception if something goes wrong along the way.
     */
    public static String[] checkForUpdates() throws Exception {
        String[] ret = new String[2];
        String xmlText = "";
        URL url;

        try {
            url = new URL("https://dl.dropboxusercontent.com/u/2750499/PolyGlot/update.xml");
            Scanner s = new Scanner(url.openStream());

            while (s.hasNext()) {
                xmlText += s.nextLine();
            }
        } catch (MalformedURLException e) {
            throw new MalformedURLException("Server unavailable or not found.");
        } catch (IOException e) {
            throw new IOException("Update file not found. Please check for updates manually at PolyGlot homepage.");
        }

        if (!xmlText.equals("")) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlText));
            Document doc = builder.parse(is);
            
            Node ver = doc.getElementsByTagName("Version").item(0);
            Node message = doc.getElementsByTagName("VersionText").item(0);
            
            // only show message if it's not this versino or an earlier version
            if (!ver.getTextContent().equals("0.7.5")) {
                ret[0] = ver.getTextContent();
                ret[1] = message.getTextContent();
            }
        }

        return ret;
    }
}
