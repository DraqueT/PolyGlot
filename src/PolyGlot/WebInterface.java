/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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
     * Checks for updates to PolyGlot     *
     * @param curVersion current version from core
     * @return The XML document retrieved from the web
     * @throws java.lang.Exception if something goes wrong along the way.
     */
    public static Document checkForUpdates(String curVersion) throws Exception {
        Document ret = null;
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
            
            ret = doc;
        }

        return ret;
    }
}
