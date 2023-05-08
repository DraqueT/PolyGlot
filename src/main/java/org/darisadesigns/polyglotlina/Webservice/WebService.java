/*
 * Copyright (c) 2023, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
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
package org.darisadesigns.polyglotlina.Webservice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.json.Json;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.darisadesigns.polyglotlina.Desktop.DesktopHelpHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopOSHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.DummyInfoBox;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopGrammarManager;
import org.darisadesigns.polyglotlina.Desktop.PFontHandler;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.Desktop.SoundRecorder;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.WebInterface;

/**
 *
 * @author draquethompson
 */
public class WebService {
    public final static String CONTENT_TYPE = "Content-Type";
    public final static String GET_METADATA = "/metadata";
    public final static String GET_XML = "/raw_xml";
    public final static String GET_FULL_FILE = "/file";
    public final static String GET_CONLANG_FONT = "/conlang_font";
    public final static String GET_LOCAL_FONT = "/local_font";
    public final static String GET_IMAGE = "/image";
    public final static String GET_SOUND = "/sound";
    public final static String REFRESH = "/refresh";
    public final static String AVALABLE_FILES = "/files";

    private final ConcurrentHashMap<String, TokenBucketRateLimiter> rateLimiterPerAddress;
    private final TokenBucketRateLimiter masterRateLimiter;
    private final TokenBucketRateLimiter refreshRateLimiter;
    private final PolyGlot polyGlot;
    private final File logFile;
    private HttpServer server;
    private Map<String, DictCore> pgdFiles;
    private boolean running = false;
    private String log = "";
    
    private byte[] charisUnicodeBytes;

    public WebService(PolyGlot _polyGlot) throws Exception {
        rateLimiterPerAddress = new ConcurrentHashMap<>();
        polyGlot = _polyGlot;
        logFile = new File(polyGlot.getWorkingDirectory() + File.separator + PGTUtil.WEB_SERVICE_LOG_FILE);
        
        // master rate limiter gives some basic DDOS protection
        masterRateLimiter = new TokenBucketRateLimiter(
                polyGlot.getOptionsManager().getWebServiceMasterTokenCapacity(),
                polyGlot.getOptionsManager().getWebServiceMasterTokenRefill()
        );

        // Limit of how often a refresh of files can be reqested (hard coded one per second)
        refreshRateLimiter = new TokenBucketRateLimiter(1, 1);
        
        doSetup();
    }

    public void doServe() throws IOException {
        if (running) {
            return;
        }
        
        // guarantee that this process shuts down with parent thread
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutDown();
            }
        });
        
        int port = polyGlot.getOptionsManager().getWebServicePort();
        
        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);

        server.createContext("/", (HttpExchange exchange) -> {
            if (rateLimitAllowed(exchange)) {
                try {
                    handleRequest(exchange);
                }
                catch (PWebServerException e) {
                    reject(exchange, e.getMessage());
                }
                catch (IOException e) {
                    error(exchange, e.getLocalizedMessage());
                }
                catch (Exception e) {
                    error(exchange, "Unhandled Server Exception");
                }

            } else {
                // too many requests
                rateLimitExceeded(exchange);
            }
        });
        
        // TODO: put the below in each response as is appropriate
        // exchange.sendResponseHeaders(200, response.length());
        
        
//        server = Undertow.builder()
//                .addHttpListener(port, "localhost")
//                .setHandler((HttpServerExchange exchange) -> {
//                    String clientIp = exchange.getSourceAddress().getAddress().getHostAddress();
//
//                    // Create a rate limiter for the client IP if it doesn't exist
//                    rateLimiterPerAddress.putIfAbsent(clientIp, new TokenBucketRateLimiter(
//                            polyGlot.getOptionsManager().getWebServiceIndividualTokenCapacity(),
//                            polyGlot.getOptionsManager().getWebServiceIndividualTokenRefil())
//                    );
//                    TokenBucketRateLimiter addressRateLimiter = rateLimiterPerAddress.get(clientIp);
//
//                    if (addressRateLimiter.tryConsume() && masterRateLimiter.tryConsume()) {
//                        try {
//                            handleRequest(exchange);
//                        }
//                        catch (PWebServerException e) {
//                            reject(exchange, e.getMessage());
//                        }
//                        catch (IOException e) {
//                            error(exchange, e.getLocalizedMessage());
//                        }
//                        catch (Exception e) {
//                            error(exchange, "Unhandled Server Exception");
//                        }
//                    } else {
//                        // too many requests
//                        rateLimitExceeded(exchange);
//                    }
//                }).build();

        server.start();
        running = true;
        log("Server started on port " + port);
    }
    
    private static Map<String, String> parseQueryParams(URI requestUri) {
        Map<String, String> queryParams = new HashMap<>();
        String query = requestUri.getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = idx > 0 ? pair.substring(0, idx) : pair;
                String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null;
                queryParams.put(key, value);
            }
        }
        return queryParams;
    }
    
    private boolean rateLimitAllowed(HttpExchange exchange) {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

        // Create a rate limiter for the client IP if it doesn't exist
        rateLimiterPerAddress.putIfAbsent(clientIp, new TokenBucketRateLimiter(
                polyGlot.getOptionsManager().getWebServiceIndividualTokenCapacity(),
                polyGlot.getOptionsManager().getWebServiceIndividualTokenRefil())
        );
        TokenBucketRateLimiter addressRateLimiter = rateLimiterPerAddress.get(clientIp);
        
        return addressRateLimiter.tryConsume() && masterRateLimiter.tryConsume();
    }

    private void doSetup() throws Exception {
        populateServedFiles();
        charisUnicodeBytes = getCharisUnicodeBytes();
    }
    
    private void log(String _log) {
        var timeStamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        var newLogLine = "\n" + timeStamp + " : " + _log;
        log += newLogLine;
        
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            if (logFile.exists()) {
                try ( BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                    writer.write(log);
                }
            }
        } catch(IOException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
        }
    }
    
    public String getLog() {
        return log;
    }

    private void handleRequest(HttpExchange exchange) throws Exception {
        var path = exchange.getRequestURI().getPath();

        exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType(path));

        // reject anything with a payload (none expected) or non-GET request
        String contentLengthHeader = exchange.getRequestHeaders().getFirst("Content-Length");
        long contentLength = contentLengthHeader != null ? Long.parseLong(contentLengthHeader) : 0;
        String method = exchange.getRequestMethod();

        if (contentLength > 0 || !method.equalsIgnoreCase("GET")) {
            reject(exchange, "Only GET requests without content accepted.");
        }

        switch (path) {
            case "/" -> {
                index(exchange);
            }
            case GET_METADATA -> {
                getMetadata(exchange);
            }
            case GET_FULL_FILE -> {
                getFullFile(exchange);
            }
            case GET_IMAGE -> {
                getImage(exchange);
            }
            case GET_LOCAL_FONT -> {
                getFont(exchange, false);
            }
            case GET_CONLANG_FONT -> {
                getFont(exchange, true);
            }
            case GET_SOUND -> {
                getSound(exchange);
            }
            case GET_XML -> {
                getRawXml(exchange);
            }
            case REFRESH -> {
                if (refreshRateLimiter.tryConsume()) {
                    refresh(exchange);
                } else {
                    rateLimitExceeded(exchange);
                }
            }
            case AVALABLE_FILES -> {
                getAvailableFiles(exchange);
            }
            default -> {
                process404(exchange);
            }
        }
    }

    /**
     * Fetches the base language file and returns it to the web request
     *
     * @param fileName
     * @return
     * @throws
     * org.darisadesigns.polyglotlina.Webservice.WebServer.PWebServerException
     */
    private File getLanguageFile(String fileName) throws PWebServerException {
        var file = new File(polyGlot.getOptionsManager().getWebServiceTargetFolder() + File.separator + fileName);

        if (file.exists()) {
            return file;
        }

        throw new PWebServerException("File Missing");
    }

    /**
     * Forces the server to refresh itself and re-scan which files are present
     * NOTE: This has its own individual rate limit
     *
     * @param exchange
     * @throws Exception
     */
    private void refresh(HttpExchange exchange) throws Exception {
        log("Service refresh requested from: " 
                + exchange.getRemoteAddress().getAddress().getHostAddress());
        
        populateServedFiles();
        index(exchange);
    }
    
    private void getAvailableFiles(HttpExchange exchange) throws PWebServerException, IOException {
        var params = parseQueryParams(exchange.getRequestURI());
        
        if (!params.isEmpty()) {
            throw new PWebServerException("Bad Request");
        }
        
        var jsonObject = Json.createObjectBuilder();
        
        log("File list requested from: " 
                + exchange.getRemoteAddress().getAddress().getHostAddress());
        
        for (var file : pgdFiles.keySet()) {
            jsonObject.add(file, pgdFiles.get(file).getPropertiesManager().getLangName());
        }
        
        var files = jsonObject.build();
        var response = files.toString().getBytes();

        exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType("json"));
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private void getMetadata(HttpExchange exchange) throws PWebServerException, IOException {
        var params = parseQueryParams(exchange.getRequestURI());
        var fileName = params.containsKey("file") ? params.get("file") : "";

        if (params.size() != 1 || !pgdFiles.containsKey(fileName)) {
            throw new PWebServerException("Bad Request");
        }
        
        log("Metadata for: " + fileName + " requested from: " 
                + exchange.getRemoteAddress().getAddress().getHostAddress());

        var core = pgdFiles.get(fileName);
        var propMan = (DesktopPropertiesManager) core.getPropertiesManager();

        var imageIds = new ArrayList<String>();
        for (var image : core.getImageCollection().getAllImages()) {
            imageIds.add(image.getId().toString());
        }

        var soundIds = new ArrayList<String>();
        for (var sound : core.getGrammarManager().getSoundMap().keySet()) {
            soundIds.add(sound.toString());
        }

        var jsonObject = Json.createObjectBuilder()
                .add("Language", propMan.getLangName())
                .add("Copyright", WebInterface.getTextFromHtml(propMan.getCopyrightAuthorInfo()))
                .add("Conlang Font", propMan.getFontCon().getFamily())
                .add("Local Font", propMan.getFontLocal().getFamily())
                .add("Image IDs", String.join(", ", imageIds))
                .add("Sound IDs", String.join(", ", soundIds))
                .build();
        
        var response = jsonObject.toString().getBytes();
        exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType("json"));
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private void getFont(HttpExchange exchange, boolean conFont) throws PWebServerException, IOException {
        var params = parseQueryParams(exchange.getRequestURI());
        var fileName = params.containsKey("file") ? params.get("file") : "";

        if (params.size() != 1 || !pgdFiles.containsKey(fileName)) {
            throw new PWebServerException("Bad Request");
        }
        
        log("Con font requested for: " + fileName + " from: " 
                + exchange.getRemoteAddress().getAddress().getHostAddress());
        
        DictCore core = pgdFiles.get(fileName);
        
        var propMan = ((DesktopPropertiesManager)core.getPropertiesManager());
        
        var font = conFont ? propMan.getCachedFont() : propMan.getCachedFont();
        
        if (font == null) {
            font = charisUnicodeBytes;
        }
        
        exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType(fileName));
        exchange.sendResponseHeaders(200, font.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(font);
        }
    }
    
    private void getFullFile(HttpExchange exchange) throws PWebServerException, IOException {
        var params = parseQueryParams(exchange.getRequestURI());
        var fileName = params.containsKey("file") ? params.get("file") : "";

        if (params.size() != 1 || !pgdFiles.containsKey(fileName)) {
            throw new PWebServerException("Bad Request");
        }
        
        log("Full file requested for: " + fileName + " from: " 
                + exchange.getRemoteAddress().getAddress().getHostAddress());

        try {
            var file = getLanguageFile(fileName);
            
            byte[] fileContent = Files.readAllBytes(file.toPath());
            
            exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType(fileName));
            exchange.sendResponseHeaders(200, fileContent.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileContent);
            }
        } catch (PWebServerException e) {
            process404(exchange);
        }        
    }

    private void getRawXml(HttpExchange exchange) throws PWebServerException {
        var params = parseQueryParams(exchange.getRequestURI());
        var fileName = params.containsKey("file") ? params.get("file") : "";

        if (params.size() != 1 || !pgdFiles.containsKey(fileName)) {
            throw new PWebServerException("Bad Request");
        }
        
        log("XML requested for: " + fileName + " from: " 
                + exchange.getRemoteAddress().getAddress().getHostAddress());

        try {
            var xmlBytes = pgdFiles.get(fileName).getRawXml().getBytes();

            exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType(".txt"));
            exchange.sendResponseHeaders(200, xmlBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(xmlBytes);
            }
        }
        catch (ParserConfigurationException | IOException | TransformerException e) {
            error(exchange, "Internal server error: " + e.getLocalizedMessage());
        }
    }

    private void reject(HttpExchange exchange, String reason) throws IOException {
        log("Rejected request from: " + exchange.getRemoteAddress().getAddress().getHostAddress() + " due to: " + reason);
        var page = buildDocument("Bad Request", reason).getBytes();
        
        exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType(".html"));
        exchange.sendResponseHeaders(400, page.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(page);
        }
    }

    private void error(HttpExchange exchange, String reason) {
        log("Errored request from: " + exchange.getRemoteAddress().getAddress().getHostAddress() + " due to: " + reason);
        
        var page = buildDocument("Internal Error", reason).getBytes();
        
        exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType(".html"));
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(500, page.length);
            os.write(page);
        } catch (IOException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
        }
    }

    private void rateLimitExceeded(HttpExchange exchange) throws IOException {
        log("Rate limit exceeded from: " + exchange.getRemoteAddress().getAddress().getHostAddress());
        
        var page = buildDocument("Rate Limit Exceeded", "Too many requests. Please try again later.").getBytes();
        
        exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType(".html"));
        exchange.sendResponseHeaders(429, page.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(page);
        }
    }

    private void getImage(HttpExchange exchange) throws PWebServerException, IOException {
        var params = parseQueryParams(exchange.getRequestURI());
        var fileName = params.containsKey("file") ? params.get("file") : "";
        var imageIdStr = params.containsKey("mediaId") ? params.get("mediaId") : "-1";
        int imageId;
        
        try {
            imageId = Integer.parseInt(imageIdStr);
        } catch (NumberFormatException e) {
            reject(exchange, "Bad Request");
            return;
        }

        log("Image with ID: " + imageIdStr + " requested for: " + fileName 
                + " from: " + exchange.getRemoteAddress().getAddress().getHostAddress());
        
        if (params.size() != 2 || !pgdFiles.containsKey(fileName) || imageId == -1) {
            throw new PWebServerException("Bad Request");
        }
        
        var core = pgdFiles.get(fileName);
        
        var imageNode = core.getImageCollection().getNodeById(imageId);
        var imageBytes = imageNode.getImageBytes();

        exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType("jpg"));
        exchange.sendResponseHeaders(200, imageBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(imageBytes);
        }
    }
    
    private void getSound(HttpExchange exchange) throws PWebServerException, IOException {
        var params = parseQueryParams(exchange.getRequestURI());
        var fileName = params.containsKey("file") ? params.get("file") : "";
        var mediaIdStr = params.containsKey("mediaId") ? params.get("mediaId") : "-1";
        int mediaId;
        
        try {
            mediaId = Integer.parseInt(mediaIdStr);
        } catch (NumberFormatException e) {
            reject(exchange, "Bad Request");
            return;
        }
        
        log("Sound with ID: " + mediaIdStr + " requested for: " + fileName + " from: " 
                + exchange.getRemoteAddress().getAddress().getHostAddress());

        if (params.size() != 2 || !pgdFiles.containsKey(fileName) || mediaId == -1) {
            throw new PWebServerException("Bad Request");
        }
        
        var core = pgdFiles.get(fileName);
        
        byte[] sound;
        
        try {
            byte[] rawSound = core.getGrammarManager().getRecording(mediaId);
            sound = SoundRecorder.pcmAudioToWav(rawSound);
        } catch (Exception e) {
            process404(exchange);
            return;
        }
        
        exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType(".wav"));
        exchange.sendResponseHeaders(200, sound.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(sound);
        }
    }
    
    private void index(HttpExchange exchange) throws IOException {
        var page = new Index(pgdFiles).buildPage().getBytes();
        
        exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType("html"));
        exchange.sendResponseHeaders(200, page.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(page);
        }
    }

    private void process404(HttpExchange exchange) throws IOException {
        var bodyContents = "<h1>Requested content at location: " + exchange.getRequestURI().getPath() + " not found.</h1>";
        var page = buildDocument("404 - not found", bodyContents).getBytes();
        
        exchange.getResponseHeaders().set(CONTENT_TYPE, getContentType("html"));
        exchange.sendResponseHeaders(404, page.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(page);
        }
        
        log("404 for request: "+ exchange.getRequestURI().getPath() + " from: " 
                + exchange.getRemoteAddress().getAddress().getHostAddress());
    }

    public static String buildDocument(String title, String contents) {
        return "<!DOCTYPE html><html><head><title>" + title + "</title></head><body>" + contents + "</body></html>";
    }
    
    public void shutDown() {
        if (running) {
            log("Shutting down webservice");
            if (server != null) {
                server.stop(0);
            }

            running = false;
            server = null;
        }
    }
    
    public boolean isRunning() {
        return running;
    }

    private void populateServedFiles() throws Exception {
        File servedDirectory = new File(polyGlot.getOptionsManager().getWebServiceTargetFolder());
        pgdFiles = new HashMap<>();

        if (!servedDirectory.exists()) {
            if (!servedDirectory.mkdir()) {
                throw new IOException("Unable to create directory to serve files.");
            }
            
            log("Creating webservice directory: " + servedDirectory.getAbsolutePath());
        }
        
        if (servedDirectory.isDirectory()) {
            for (File file : servedDirectory.listFiles()) {
                if (file.getName().endsWith(".pgd")) {
                    var helpHandler = new DesktopHelpHandler();
                    var fontHandler = new PFontHandler();
                    var osHandler = new DesktopOSHandler(DesktopIOHandler.getInstance(), new DummyInfoBox(), helpHandler, fontHandler);
                    var core = new DictCore(new DesktopPropertiesManager(), osHandler, new PGTUtil(), new DesktopGrammarManager());
                    PolyGlot.getTestShell(core);
                    core.readFile(file.getCanonicalPath());
                    pgdFiles.put(file.getName(), core);
                }
            }
        } else {
            throw new PWebServerException(servedDirectory.getAbsolutePath() + " must be a directory.");
        }
    }

    private static String getContentType(String path) {
        if (path.endsWith("html") || path.endsWith("htm") || path.endsWith("/")) {
            return "text/html";
        } else if (path.endsWith("txt") || path.endsWith(".java")) {
            return "text/plain";
        } else if (path.endsWith("gif")) {
            return "image/gif";
        } else if (path.endsWith("jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith("png")) {
            return "image/png";
        } else if (path.endsWith("pdf")) {
            return "application/pdf";
        } else if (path.endsWith("json")) {
            return "application/json";
        } else if (path.endsWith("wav")) {
            return "audio/wav";
        }

        return "application/octet-stream";
    }
    
    private byte[] getCharisUnicodeBytes() throws IOException {
        var is = this.getClass().getResourceAsStream(PGTUtil.UNICODE_FONT_LOCATION);
        var byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        return byteArrayOutputStream.toByteArray();
    }

    public class PWebServerException extends Exception {
        public PWebServerException(String message) {
            super(message);
        }
    }
}
