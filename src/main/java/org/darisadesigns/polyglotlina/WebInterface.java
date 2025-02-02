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
package org.darisadesigns.polyglotlina;

import org.darisadesigns.polyglotlina.Nodes.ImageNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.safety.Safelist;

/**
 * This class handles all web communication to and from PolyGlot
 *
 * @author draque
 */
public class WebInterface {

    /**
     * Checks for updates to PolyGlot
     *
     * @return The XML document retrieved from the web
     * @throws java.lang.Exception if something goes wrong along the way.
     */
    public static Document checkForUpdates() throws Exception {
        Document ret = null;
        String xmlText = "";
        URL url;

        try {
            url = new URL(PGTUtil.UPDATE_FILE_URL);

            try ( InputStream is = url.openStream();  Scanner s = new Scanner(is)) {
                while (s.hasNext()) {
                    xmlText += s.nextLine();
                }
            }
        }
        catch (MalformedURLException e) {
            throw new Exception("Server unavailable or not found.", e);
        }
        catch (IOException e) {
            throw new IOException("Update file not found or has been moved. Please check for updates manually at PolyGlot homepage.", e);
        }

        if (xmlText.contains("<TITLE>Moved Temporarily</TITLE>")) {
            throw new Exception("Update file not found or has been moved. Please check for updates manually at PolyGlot homepage.");
        }

        if (!xmlText.isEmpty()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlText));
            ret = builder.parse(is);
        }

        return ret;
    }

    /**
     * Gets only the text from a PTextPane's html
     *
     * @param text
     * @return
     */
    public static String getTextFromHtml(String text) {
        org.jsoup.nodes.Document.OutputSettings outputSettings 
                = new org.jsoup.nodes.Document.OutputSettings();
        outputSettings.prettyPrint(false);
        
        // jsoup is not great with preservartion of linebreaks...
        if (text.contains("<body>")) {
            text = text.substring(text.indexOf("<body>"), text.indexOf("</body>") + 7);
        }
        
        text = text.replaceAll("<p>", "\n").replaceAll("</p>", "").replaceAll("<br>", "\n");
        String strWithNewLines = org.jsoup.Jsoup.clean(text, "", Safelist.none(), outputSettings);
        
        return strWithNewLines;
    }

    /**
     * Takes archived HTML and translates it into display HTML. - Replaces
     * archival image references to temp image refs
     *
     * @param html archived html
     * @param core
     * @return unarchived html
     * @throws java.lang.Exception
     */
    public static String unarchiveHTML(String html, DictCore core) throws Exception {
        // pattern for finding archived images
        Pattern pattern = Pattern.compile("(<img src=\"[^>,_]+\">)");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            String regPath = matcher.group(1);
            regPath = regPath.replace("<img src=\"", "");
            regPath = regPath.replace("\"", "");
            regPath = regPath.replace(">", "");
            try {
                int imageId = Integer.parseInt(regPath);
                ImageNode image = (ImageNode) core.getImageCollection().getNodeById(imageId);
                html = html.replace("<img src=\"" + regPath + "\">", "<img src=\"file:///" + image.getImagePath() + "\">");
            }
            catch (IOException | NumberFormatException e) {
                throw new Exception("problem loading image : " + e.getLocalizedMessage(), e);
            }
        }

        return html;
    }

    /**
     * Takes display HTML and translates it into archival HTML. - Replaces
     * actual image references with static, id based refs
     *
     * @param html unarchived html
     * @param core
     * @return archivable html
     */
    public static String archiveHTML(String html, DictCore core) {
        // pattern for finding unarchived images
        Pattern pattern = Pattern.compile("(<img src=\"[^>,_]+_[^>]+\">)");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            String regPath = matcher.group(1);
            regPath = regPath.replace("<img src=\"file:///", "");
            regPath = regPath.replace("\"", "");
            regPath = regPath.replace(">", "");
            String fileName = core.getOSHandler().ioHandler.getFilenameFromPath(regPath);
            String arcPath = fileName.replaceFirst("_.*", "");
            html = html.replace("file:///" + regPath, arcPath);
        }

        return html;
    }

    /**
     * Tests current internet connection based on google
     *
     * @return true if connected
     */
    public static boolean isInternetConnected() {
        String address = "www.google.com";
        final int PORT = 80;
        final int TIMEOUT = 5000;
        boolean ret = false;

        try {
            try ( Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(address, PORT), TIMEOUT);
            }
            ret = true;
        }
        catch (IOException e) {
            // error simply means no connection
        }

        return ret;
    }

    /**
     * encodes html characters
     * @param s
     * @return 
     */
    public static String encodeHTML(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }

        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            switch (c) {
                case '&':
                    sb.replace(i, i + 1, "&amp;");
                    i += 4;
                    break;
                case '"':
                    sb.replace(i, i + 1, "&quot;");
                    i += 5;
                    break;
                case '\'':
                    sb.replace(i, i + 1, "&apos;");
                    i += 5;
                    break;
                case '>':
                    sb.replace(i, i + 1, "&gt;");
                    i += 3;
                    break;
                case '<':
                    sb.replace(i, i + 1, "&lt;");
                    i += 3;
                    break;
                default:
                    break;
            }
        }

        return sb.toString();
    }
    
    /**
     * Escapes html characters (symmetrical to encodeHTML)
     * @param s
     * @return 
     */
    public static String escapeHTML(String s) {
        return s.replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&gt;", ">")
                .replace("&lt;", "<");
    }

    private WebInterface() {
    }
}
