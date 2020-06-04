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

import java.awt.Desktop;
import java.io.File;
import org.darisadesigns.polyglotlina.Nodes.ImageNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.jsoup.Jsoup;

/**
 * This class handles all web communication to and from PolyGlot
 *
 * @author draque
 */
public final class WebInterface {

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

            try (InputStream is = url.openStream();
                    Scanner s = new Scanner(is)) {
                while (s.hasNext()) {
                    xmlText += s.nextLine();
                }
            }
        } catch (MalformedURLException e) {
            throw new Exception("Server unavailable or not found.", e);
        } catch (IOException e) {
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
     * @param text
     * @return 
     */
    public static String getTextFromHtml(String text) {
        return Jsoup.parse(text).text();
    }
    
    /**
     * Takes archived HTML and translates it into display HTML.
     * - Replaces archival image references to temp image refs
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
                ImageNode image = (ImageNode)core.getImageCollection().getNodeById(imageId);
                html = html.replace("<img src=\""+ regPath + "\">", "<img src=\"file:///"+ image.getImagePath() + "\">");
            } catch (IOException | NumberFormatException e) {
                throw new Exception("problem loading image : " + e.getLocalizedMessage(), e);
            }
        }
        
        return html;
    }
    
    /**
     * Takes display HTML and translates it into archival HTML.
     * - Replaces actual image references with static, id based refs
     * @param html unarchived html
     * @return archivable html
     */
    public static String archiveHTML(String html) {
        // pattern for finding unarchived images
        Pattern pattern = Pattern.compile("(<img src=\"[^>,_]+_[^>]+\">)");
        Matcher matcher = pattern.matcher(html);
        
        while (matcher.find()) {
            String regPath = matcher.group(1);
            regPath = regPath.replace("<img src=\"file:///", "");
            regPath = regPath.replace("\"", "");
            regPath = regPath.replace(">", "");
            String fileName = IOHandler.getFilenameFromPath(regPath);
            String arcPath = fileName.replaceFirst("_.*", "");
            html = html.replace("file:///" + regPath, arcPath);
        }
        
        return html;
    }
    
    /**
     * This cycles through the body of HTML and generates an ordered list of objects
     * representing all of the items in the HTML. Consumers are responsible for
     * identifying objects.
     * @param html HTML to extract from
     * @return 
     * @throws java.io.IOException 
     */
    public static List<Object> getElementsHTMLBody(String html) throws IOException {
        List<Object> ret = new ArrayList<>();
        String body = html.replaceAll(".*<body>", "");
        body = body.replaceAll("</body>.*", "");
        Pattern pattern = Pattern.compile("([^<]+|<[^>]+>)");//("(<[^>]+>)");
        Matcher matcher = pattern.matcher(body);
        
        // loops on unincumbered text and tags.
        while (matcher.find()) {
            String token = matcher.group(1);
            if (token.startsWith("<")) {
                if (token.contains("<img src=\"")) {
                    String path = token.replace("<img src=\"file:///", "").replace("\">", "");
                    ret.add(IOHandler.getImage(path));
                } else {
                    // do nothing with unrecognized elements - might be upgraded later.
                }
            } else {
                // this is plaintext
                String add = token.trim();
                if (!add.isEmpty()) {
                    ret.add(add + " ");
                }
            }
        }
        
        return ret;
    }
    
    /**
     * Tests current internet connection based on google
     * @return true if connected
     */
    public static boolean isInternetConnected() {
        String address = "www.google.com";
        final int PORT = 80;
        final int TIMEOUT = 5000;
        boolean ret = false;
        
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(address, PORT), TIMEOUT);
            }
            ret = true;
        } catch (IOException e) {
            IOHandler.writeErrorLog(e, "Unable to reach: " + address);
        }
        
        return ret;
    }
    
    /**
     * Tell the OS to open a browser and go to a given web address.(distinct behavior in Linux due to lack of certain features)
     * @param url
     * @throws java.io.IOException 
     */
    public static void browseToLocation(String url) throws IOException {
        if (PGTUtil.IS_WINDOWS) {
            try {
                URI help = new URI(url);
                Desktop.getDesktop().browse(help);
            }
            catch (URISyntaxException e) {
                IOHandler.writeErrorLog(e);
                InfoBox.warning("Menu Warning", "Unable to open browser. Please load manually at:\n"
                    + url + "\n(copied to your clipboard for convenience)", null);
                new ClipboardHandler().setClipboardContents(url);
            }
        } else if (PGTUtil.IS_OSX) {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("open " + url);
        } else if (PGTUtil.IS_LINUX) {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("xdg-open " + url);
        } else {
            InfoBox.warning("Menu Warning", "Unable to open browser. Please load manually at:\n"
                    + url + "\n(copied to your clipboard for convenience)", null);
            new ClipboardHandler().setClipboardContents(url);
        }
    }

    private WebInterface() {}
}
