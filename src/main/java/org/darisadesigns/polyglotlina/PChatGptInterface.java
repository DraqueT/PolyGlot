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
package org.darisadesigns.polyglotlina;

//import com.plexpt.chatgpt.ChatGPT;
//import com.plexpt.chatgpt.entity.chat.ChatCompletion;
//import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
//import com.plexpt.chatgpt.entity.chat.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.darisadesigns.polyglotlina.CustomControls.GrammarChapNode;
import org.darisadesigns.polyglotlina.CustomControls.GrammarSectionNode;
import org.darisadesigns.polyglotlina.ManagersCollections.GrammarManager;
import org.darisadesigns.polyglotlina.Nodes.ConWord;

/**
 *
 * @author draquethompson
 */
public class PChatGptInterface {

//    private final DictCore core;
//    private final List<Message> chatMessages = new ArrayList<>();
//    private final ChatGPT chatGPT;
//    private final static String GPT_MODEL = ChatCompletion.Model.GPT_4.getName();// GPT_3_5_TURBO.getName();
//    private final static int MAX_RELATED_HITS = 3;

    public PChatGptInterface(DictCore _core) {
//        core = _core;
//        
//        chatGPT = ChatGPT.builder()
//                .apiKey("")
//                .timeout(900)
//                .apiHost("https://api.openai.com/")
//                .build()
//                .init();
//
//        chatMessages.add(Message.ofSystem("You are chatGPT and will have nice conversations."));
    }

    public String doChat(String message) {
//        chatMessages.add(Message.of(message));
//        String reply = "";
//
//        try {
//            ChatCompletion chatCompletion = ChatCompletion.builder()
//                    .model(GPT_MODEL)
//                    .messages(chatMessages)
//                    .maxTokens(2000)
//                    .temperature(0.9)
//                    .build();
//
//            ChatCompletionResponse chatResponse = chatGPT.chatCompletion(chatCompletion);
//
//            Message res = chatResponse.getChoices().get(0).getMessage();
//            reply = res.getContent();
//            chatMessages.add(Message.builder().role("assistant").content(reply).build());
//        }
//        catch (Exception e) {
//            core.getOSHandler().getInfoBox().error("GPT Error", e.getMessage());
//        }
//
//        return reply;
        return "";
    }

    public String getTranslate(String phrase) {
//        phrase = phrase.trim();
//        List<Message> setupMessages = new ArrayList<>();
//        setupMessages.add(Message.ofSystem("You will play the role of a translator for a language I define."));
//        setupMessages.add(Message.of("When asked, please translate text you are given. Do not explain the translation."));
//        
//        buildLimitedDictionary(phrase, setupMessages);
//        parseGrammar(core.getGrammarManager(), setupMessages);
//        
//        String[] words = baseDictForms(phrase);
//
//        setupMessages.add(Message.of("Please translate this to the described language: " + phrase));
//
//        try {
//            ChatCompletion chatCompletion = ChatCompletion.builder()
//                    .model(GPT_MODEL)
//                    .messages(setupMessages)
//                    .maxTokens(500)
//                    .temperature(0.9)
//                    .build();
//
//            ChatCompletionResponse chatResponse = chatGPT.chatCompletion(chatCompletion);
//
//            Message res = chatResponse.getChoices().get(0).getMessage();
//            return res.getContent().replace("'", "");
//        }
//        catch (Exception e) {
//            core.getOSHandler().getInfoBox().error("GPT Error", e.getMessage());
//        }

        return "";
    }

    /**
     * Builds a limited dictionary based on phrase passed in
     * @param words 
     */
//    private void buildLimitedDictionary(String phrase, List<Message> setupMessages) {
//        setupMessages.add(
//                Message.builder().role("user").content("I will give you the dictionary").build());
//        
//        String[] targetWords = this.baseDictForms(phrase);
//        
//        for (String word : targetWords) {
//            ConWord[] likelyWords = this.likelyWords(word);
//            
//            for (ConWord conWord : likelyWords) {
//                setupMessages.add(
//                        Message.builder().role("user").content(formatWordDefinition(conWord)).build()
//                );
//            }
//        }
//    }

//    private void parseGrammar(GrammarManager gramMan, List<Message> setupMessages) {
//        GrammarChapNode[] chapters = gramMan.getChapters();
//
//        if (chapters.length == 0) {
//            return;
//        }
//
//        for (GrammarChapNode chapter : chapters) {
//            for (int i = 0; i < chapter.getChildCount(); i++) {
//                GrammarSectionNode section = chapter.getChild(i);
//                String sectionText = WebInterface.getTextFromHtml(section.getSectionText());
//
//                setupMessages.add(
//                        Message
//                                .builder()
//                                .role("user")
//                                .content(cleanGpt(sectionText))
//                                .build()
//                );
//            }
//        }
//    }

//    private String formatWordDefinition(ConWord word) {
//        return cleanGpt("Word:" + word.getValue())
//                + "\nDefinition: " + cleanGpt(word.getLocalWord())
//                + "\nPart of Speech: " + cleanGpt(word.getWordTypeDisplay()
//                        + "\nDefinition: " + cleanGpt(WebInterface.getTextFromHtml(word.getDefinition())));
//    }

//    private String cleanGpt(String value) {
//        return value.replace("\n", " ").replace(":", " ").replace("\r", " ");
//    }
    
//    private String[] baseDictForms(String test) {
//        List<Message> request = new ArrayList<>();
//        List<String> words = new ArrayList<>();
//        
//        String instruction = "Please take all the words in message I send to you after this one and "
//                + "return their most basic dictionary forms (singular, non-conjugated). Please return "
//                + "any given word no more than once. Please return then in a comma delimited list in "
//                + "brackets. For example, if I sent the text \"I walked to the store with the dogs.\""
//                + " you would return \"[I, walk, to, the, store, with, dog]\". Please return the list "
//                + "by itself without commentary.";
//        request.add(Message.builder().role("user").content(instruction).build());
//        request.add(Message.builder().role("user").content(test).build());
//        
//        try {
//            ChatCompletion chatCompletion = ChatCompletion.builder()
//                    .model(GPT_MODEL)
//                    .messages(request)
//                    .maxTokens(2000)
//                    .temperature(0.9)
//                    .build();
//
//            ChatCompletionResponse chatResponse = chatGPT.chatCompletion(chatCompletion);
//
//            Message res = chatResponse.getChoices().get(0).getMessage();
//            String reply = res.getContent().replace("'", "").replace("[", "").replace("]", "");
//            
//            for (String word : reply.split(",")) {
//                words.add(word.trim());
//            }
//        }
//        catch (Exception e) {
//            core.getOSHandler().getInfoBox().error("GPT Error", e.getMessage());
//        }
//        
//        return words.toArray(String[]::new);
//    }
    
    /**
     * Searches for potential words that may be related to a given word
     * @param target
     * @return 
     */
//    private ConWord[] likelyWords(String target) {
//        List<WeightedWord> hits = new ArrayList<>();
//        
//        for (ConWord conWord : core.getWordCollection().getWordNodes()) {
//            WeightedWord weightedWord = new WeightedWord(conWord);
//            
//            // highest value put on direct synonyms
//            for (String word : conWord.getLocalWord().split(",")) {
//                word = word.trim();
//                
//                if (word.isBlank()) {
//                    continue;
//                }
//                
//                if (target.equals(word)) {
//                    // TODO: RETHINK VALUE HERE
//                    weightedWord.weight += 10;
//                    break;
//                }
//            }
//            
//            // if no direct hit, search for partial synonym match
//            if (weightedWord.weight == 0 && conWord.getLocalWord().contains(target)) {
//                weightedWord.weight += 5;
//            }
//            
//            int defHits = countStringOccurrences(conWord.getDefinition(), target);
//            defHits = defHits > 5 ? 5 : defHits;
//            
//            weightedWord.weight += defHits;
//            
//            if (weightedWord.weight > 0) {
//                hits.add(weightedWord);
//            }
//        }
//        
//        Collections.sort(hits);
//        
//        // prevent ret length from exceeding max
//        int retLength = hits.size() > MAX_RELATED_HITS ? MAX_RELATED_HITS : hits.size();
//        
//        ConWord[] returnHits = new ConWord[retLength];
//        
//        for (int i = 0; i < retLength; i++) {
//            returnHits[i] = hits.get(i).word;
//        }
//        
//        return returnHits;
//    }
    
    /**
     * Counts occurrences of one string in another
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
        
        public WeightedWord(ConWord _word) {
            word = _word;
        }
        
        @Override
        public int compareTo(WeightedWord comp) {
            return Integer.compare(comp.weight, this.weight);
        }
    }
}
