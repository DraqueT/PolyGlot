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
package ChatGPTInterface;

import ChatGPTInterface.GptMessage.Role;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.darisadesigns.polyglotlina.CustomControls.GrammarSectionNode;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.WordClassCollection;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import org.darisadesigns.polyglotlina.Nodes.WordClassValue;
import org.darisadesigns.polyglotlina.WebInterface;

/**
 *
 * @author draquethompson
 */
public class PChatGptInterface {
    public final static String DEFAULT_GPT_MODEL = "gpt-3.5-turbo-16k";
    public final static Map<String, URL> COMPLETION_ADDRESS_LOOKUP;
    private final static int MAX_RELATED_HITS = 3;

    public final String apiToken;
    private final DictCore core;
    private HttpURLConnection con;
    private final List<GptMessage> messages;
    private final GptMessage SYSTEM_TRANSLATE_MESSAGE;

    public PChatGptInterface(DictCore _core, String _apiToken) {
        core = _core;
        apiToken = _apiToken;
        messages = new ArrayList<>();
        SYSTEM_TRANSLATE_MESSAGE = new GptMessage(
                Role.SYSTEM, "You are a translator who will learn the language "
                + core.getPropertiesManager().getLangName()
                + ". You will translate to and from it on request."
        );
    }
    
    public boolean isModelSupported(String model) {
        return getUrl(model) != null;
    }
    
    /**
     * Fetches appropriate URL for given model
     * @param model
     * @return 
     */
    private HttpURLConnection getUrlConnection(String model) throws IOException {
        var url = getUrl(model);
        
        if (url != null) {
            return (HttpURLConnection)url.openConnection();
        }
        
        return null;
    }
    
    private URL getUrl(String model) {
        for (Entry<String, URL> entry : COMPLETION_ADDRESS_LOOKUP.entrySet()) {
            if (model.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return null;
    }

    private void setupConnection(String model) throws IOException {
        con = getUrlConnection(model);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + apiToken);

        con.setDoOutput(true);
    }

    private static String buildRequestData(List<GptMessage> messages, String gptModel) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonObject = objectMapper.createObjectNode();
        ArrayNode jsonMessages = objectMapper.createArrayNode();
        
        jsonObject.put("model", gptModel);

        for (GptMessage message : messages) {
            jsonMessages.add(message.jsonMessage());
        }

        jsonObject.set("messages", jsonMessages);
        
        return jsonObject.toString();
    }

    /**
     * This adds both a user message and an assistant reply to the message
     * list. This changes the state of the messages, as both will remain in its
     * history.
     *
     * @param messageString
     * @param gptModel
     * @return
     * @throws java.io.IOException
     * @throws ChatGPTInterface.GPTException
     */
    public String doChat(String messageString, String gptModel) throws IOException, GPTException {
        messages.add(new GptMessage(Role.USER, messageString));
        String responseString = pullResponse(gptModel);
        messages.add(new GptMessage(Role.ASSISTANT, responseString));

        return responseString;
    }

    /**
     * Gets response but does not leave request on the message stack. The list of
     * messages is the same after the call is made.
     *
     * @param messageString
     * @param gptModel
     * @return
     * @throws java.io.IOException
     * @throws ChatGPTInterface.GPTException
     */
    public String getIdempotentResponse(String messageString, String gptModel) throws IOException, GPTException {
        messages.add(new GptMessage(Role.USER, messageString));
        String response = pullResponse(gptModel);
        messages.remove(messages.size() - 1);

        return response;
    }

    /**
     * This fetches a response to the current chain of messages (non
     * deterministic).
     *
     * @return
     * @throws IOException
     * @throws com.mycompany.chatgpttest.ChatGPTBetterTest.GPTException
     */
    private String pullResponse(String gptModel) throws IOException, GPTException {
        return pullResponse(messages, gptModel);
    }

    private String pullResponse(List<GptMessage> targetMessages, String gptModel) throws IOException, GPTException {
        setupConnection(gptModel);
        try (OutputStream os = con.getOutputStream()) {
            os.write(buildRequestData(targetMessages, gptModel).getBytes());
            os.flush();

            String responseMessage = con.getResponseMessage();
            handleErrorResponse(con.getResponseCode(), responseMessage);

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            return response.toString();
        }
        finally {
            con.disconnect();
        }
    }

    /**
     * This will throw an appropriate error if the response is a failure.
     *
     * @param response
     */
    private void handleErrorResponse(int response, String responseMessage) throws GPTException {
        switch (response) {
            case 400 -> {
                throw new GPTException("DATA BODY PROBLEM: " + responseMessage);
            }
            case 401 -> {
                throw new GPTException(
                        "API Key rejected. Please check your API key in the PolyGlot options menu"
                        + "\nor request one here: https://platform.openai.com/account/api-keys"
                        + "\n\nReturned error: " + responseMessage
                );
            }
            case 429 -> {
                throw new GPTException("Rate limit for messages exceeded or server overwhelmed (you may have run out of tokens): " + responseMessage);
            }
            case 500 -> {
                throw new GPTException("GPT Server Error: " + responseMessage);
            }
        }
    }

    public String getTranslate(String phrase, List<GrammarSectionNode> grammarSections, String gptModel) throws IOException, GPTException {
        phrase = phrase.trim();
        messages.clear();
        messages.add(SYSTEM_TRANSLATE_MESSAGE);

        buildLimitedDictionary(phrase);
        parseGrammar(grammarSections);

        messages.add(new GptMessage(
                Role.USER, "Please ignore word class unless it is relevant to grammar rules.")
        );
        messages.add(new GptMessage(
                Role.USER, "Please start and end " + core.getPropertiesManager().getLangName() + " text with square brackets.")
        );
        messages.add(new GptMessage(Role.USER, "Please respect conjugation and declension rules."));
        String jsonString = getIdempotentResponse(
                "Translate the following to "
                + core.getPropertiesManager().getLangName()
                + " and show your work {" + phrase + "}",
                gptModel);

        ReplyMessage reply = new ReplyMessage(jsonString);
        return reply.getContent();
    }

    /**
     * Builds a limited dictionary based on phrase passed in
     *
     * @param words
     */
    private void buildLimitedDictionary(String phrase) throws IOException, GPTException {
        messages.add(new GptMessage(Role.USER, "Following are dictionary entries for "
                + core.getPropertiesManager().getLangName() + "."));

        String[] targetWords = this.baseDictForms(phrase);

        for (String word : targetWords) {
            ConWord[] likelyWords = this.likelyWords(word);

            for (ConWord conWord : likelyWords) {
                messages.add(new GptMessage(Role.USER, formatWordDefinition(conWord)));
            }
        }
    }

    private void parseGrammar(List<GrammarSectionNode> grammarSections) {
        if (grammarSections.isEmpty()) {
            return;
        }

        for (GrammarSectionNode section : grammarSections) {
            String sectionText = WebInterface.getTextFromHtml(section.getSectionText());
            messages.add(new GptMessage(Role.USER, cleanGpt(sectionText)));
        }
    }

    private String formatWordDefinition(ConWord word) {
        String langName = core.getPropertiesManager().getLangName();
        String classes = getCommaDelimittedWordClasses(word);
        String definition = cleanGpt(WebInterface.getTextFromHtml(word.getDefinition()));
        String partOfSpeech = cleanGpt(word.getWordTypeDisplay());
        String synonym = cleanGpt(word.getLocalWord());
        
        return cleanGpt(
            (langName.isBlank() ? "Conlang" : langName) + " Word:" + word.getValue()
            + (synonym.isBlank() ? "" : "\nSynonym: " + synonym)
            + (partOfSpeech.isBlank() ? "" : "\nPart of Speech: " + partOfSpeech)
            + (classes.isBlank() ? "" : "\nClasses: " + classes)
            + (definition.isBlank() ? "" : "\nDefinition: " + definition)
        );
    }
    
    private String getCommaDelimittedWordClasses(ConWord word) {
        List<String> classes = new ArrayList<>();
        
        WordClassCollection classCollection = core.getWordClassCollection();
        
        for (Entry<Integer, Integer> entry : word.getClassValues()) {
            try {
                WordClass wordClass = classCollection.getNodeById(entry.getKey());
                
                if (wordClass != null) {
                    WordClassValue value = wordClass.getValueById(entry.getValue());
                    
                    if (value != null) {
                        classes.add(value.getValue());
                    }
                }
            } catch (Exception e) {
                // do nothing: missing word class entries are cleaned and reported elsewhere
            }
        }
        
        for (Entry<Integer, String> entry : word.getClassTextValues()) {
            WordClass wordClass = classCollection.getNodeById(entry.getKey());
            
            if (wordClass != null) {
                classes.add(wordClass.getValue() + ": " + entry.getValue());
            }
        }
        
        return String.join(", ", classes);
    }

    private String cleanGpt(String value) {
        return value.replace("\n", " ").replace(":", " ").replace("\r", " ");
    }

    private String[] baseDictForms(String test) throws IOException, GPTException {
        List<GptMessage> request = new ArrayList<>();
        List<String> words = new ArrayList<>();

        String instruction = "Please take all the words in messages I send to you after this message and "
                + "return their most basic dictionary forms (singular, non-conjugated). Please return "
                + "any given word no more than once. Please return them in a comma delimited list in "
                + "brackets. For example, if I sent the text \"I walked to the store with the dogs.\""
                + " you would return \"[I, walk, to, the, store, with, dog]\". Please return the list "
                + "by itself without commentary.";
        request.add(new GptMessage(Role.USER, instruction));
        request.add(new GptMessage(Role.USER, test));

        // There is no reason to waste tokens on a simple result: just use GPT-3.5 for this
        String responseJson = this.pullResponse(request, DEFAULT_GPT_MODEL).replace("'", "").replace("[", "").replace("]", "");
        ReplyMessage reply = new ReplyMessage(responseJson);

        for (String word : reply.getContent().split(",")) {
            words.add(word.trim());
        }
        
        // This is cheating, but it increases accuracy significantly
        if (!words.contains("to be")) {
            words.add("to be");
        }

        return words.toArray(String[]::new);
    }

    /**
     * Searches for potential words that may be related to a given word.
     *
     * @param target
     * @return
     */
    private ConWord[] likelyWords(String target) {
        List<WeightedWord> hits = new ArrayList<>();

        for (ConWord conWord : core.getWordCollection().getWordNodes()) {
            WeightedWord weightedWord = new WeightedWord(conWord);

            // highest value put on direct synonyms
            for (String word : conWord.getLocalWord().split(",")) {
                word = word.trim();

                if (word.isBlank()) {
                    continue;
                }

                if (target.equals(word)) {
                    weightedWord.weight += 10;
                    break;
                }
            }

            // if no direct hit, search for partial synonym match
            if (weightedWord.weight == 0 && conWord.getLocalWord().contains(target)) {
                weightedWord.weight += 5;
            }

            int defHits = countStringOccurrences(conWord.getDefinition(), target);
            defHits = defHits > 5 ? 5 : defHits;

            weightedWord.weight += defHits;

            if (weightedWord.weight > 0) {
                hits.add(weightedWord);
            }
        }

        Collections.sort(hits);

        // prevent ret length from exceeding max
        int retLength = hits.size() > MAX_RELATED_HITS ? MAX_RELATED_HITS : hits.size();

        ConWord[] returnHits = new ConWord[retLength];

        for (int i = 0; i < retLength; i++) {
            returnHits[i] = hits.get(i).word;
        }

        return returnHits;
    }

    
    public JsonNode getGptModels() throws GPTException, UnknownHostException {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpURLConnection modelCon = null;

        try {
            URL url = getUrl("get_models");
            modelCon = (HttpURLConnection) url.openConnection();
            modelCon.setRequestMethod("GET");
            modelCon.setRequestProperty("Authorization", "Bearer " + apiToken);

            this.handleErrorResponse(modelCon.getResponseCode(), "");

            StringBuilder response;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(modelCon.getInputStream()))) {
                String line;
                response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            return objectMapper.readTree(response.toString());
        } catch (UnknownHostException e) {
            throw e;
        } catch (IOException e) {
            throw new GPTException(e);
        } finally {
            if (modelCon != null) {
                modelCon.disconnect();
            }
        }
    }

    /**
     * Counts occurrences of one string in another.
     *
     * @param str
     * @param searchStr
     * @return
     */
    public static int countStringOccurrences(String str, String searchStr) {
        int count = 0;
        int lastIndex = 0;

        if (str.isBlank() || searchStr.isBlank()) {
            return 0;
        }

        while (lastIndex != -1) {
            lastIndex = str.indexOf(searchStr, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += searchStr.length();
            }
        }
        return count;
    }

    private class WeightedWord implements Comparable<WeightedWord> {

        public ConWord word;
        public int weight;

        WeightedWord(ConWord _word) {
            word = _word;
        }

        @Override
        public int compareTo(WeightedWord comp) {
            return Integer.compare(comp.weight, this.weight);
        }
    }
    
    static {
        COMPLETION_ADDRESS_LOOKUP = new HashMap<>();
        
        try {
            COMPLETION_ADDRESS_LOOKUP.put("gpt", new URL("https://api.openai.com/v1/chat/completions"));
            COMPLETION_ADDRESS_LOOKUP.put("get_models", new URL("https://api.openai.com/v1/models"));
        }
        catch (MalformedURLException e) {
            PolyGlot.getPolyGlot().getOSHandler().getInfoBox().error("PolyGlot Error", "Malformed URL: Unable to initialize GPT window.");
            PolyGlot.getPolyGlot().getOSHandler().getIOHandler().writeErrorLog(e);
        }
    }
}
