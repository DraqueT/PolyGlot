/*
 * Copyright (c) 2022-2023, Draque Thompson, draquemail@gmail.com
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author draque
 */
public class XMLRecoveryTool {
    
    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private final DocumentBuilder builder;
    private String source;
    private List<String> outputList;
    private final List<String> tokenList;
    
    
    public XMLRecoveryTool(String _source) throws ParserConfigurationException {
        source = _source;
        tokenList = new ArrayList<>();
        builder = factory.newDocumentBuilder();
    }
    
    public String  recoverXml() {
        this.cleanTruncatedXml();
        this.populateTokenList();
        this.fixMissingTags();

        return source;
    }
    
    /**
     * If XML ws truncated, discards unreadable portions
     */
    private void cleanTruncatedXml() {
        source = source.trim();
        
        // removes partial tags at end of XML
        source = source.replaceAll("<[^<]*\\Z", "");
    }
    
    private void populateTokenList() {
        tokenList.clear();
        
        String regex = "<[^<]*>|[^<]*"; // fetch encapsulated tags and data
        
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(source);

        while (m.find()) {
            var found = m.group();
            
            // test all open/close tags and reject if invalid
            if (found.startsWith("<") && !testOpenCloseTagValidity(found)) {
                continue;
            }
            
            tokenList.add(found);
        }
    }
    
    /**
     * Tests the validity of opening and closing tags
     * It does this by testing an empty open/close combo with a DOM parser
     * @param tag
     * @return 
     */
    private boolean testOpenCloseTagValidity(String tag) {
        String test;
        
        if (tag.startsWith("<?") && tag.endsWith("?>")) { 
            // processing tags can be ignored as they're weird
            return true;
        } else if (tag.startsWith("<") && tag.endsWith("/>")) {
            // empty tags...
            test = tag;
        } else if (tag.startsWith("</")) {
            // pair end tags with start tags...
            test = tag.replace("</", "<") + tag;
        } else {
            // pair start tags with end tags...
            test = tag + tag.replaceAll("<", "</");
        }
        
        try {
            builder.parse(new ByteArrayInputStream(test.getBytes()));
        } catch (IOException | SAXException e) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Fixes errors of simple tags.  
     * i.e. tags that only contain a value and not other tags inside it
     */
    private void fixMissingTags() {
        List<String> tagStack = new ArrayList<>();
        outputList = new ArrayList<>();
        boolean failures = false;
        String prevNonBlankToken;
        
        
        // first element is header
        outputList.add(tokenList.get(0));
        prevNonBlankToken = tokenList.get(1);
        
        for (int i = 1; i < tokenList.size(); i++) {
            String tag = tokenList.get(i);
            
            if (!tag.endsWith("/>") && tag.startsWith("<")) { // ignore empty tags and data
                if (!tag.startsWith("</")) { // start tags
                    tagStack.add(tag);
                } else { // end tags
                    String startTag = tag.replaceAll("/", "");
                    
                    if (!tagStack.isEmpty() && startTag.equals(tagStack.get(tagStack.size() - 1))) {
                        // tag is closed correctly. Remove from stack
                        tagStack.remove(tagStack.size() - 1);
                    } else if (!tagStack.isEmpty() && !tagStack.contains(startTag)) {
                        // Opening tag is missing
                        failures = true;
                        if (prevNonBlankToken.startsWith("<") && prevNonBlankToken.endsWith(">")) {
                            // Previous token is a tag, no content for this tag (insert "<tag/>")
                            outputList.add(new StringBuffer(startTag).insert(startTag.length() - 1, "/").toString());
                        } else {
                            // Insert opening tag before content
                            outputList.add(outputList.size() - 1, startTag);
                            // Also include closing tag
                            outputList.add(new StringBuffer(startTag).insert(1, "/").toString());
                        }
                        continue;
                    } else { // Either an opening or closing tag is missing. Mark with failures
                        failures = true;

                        if (tagStack.contains(startTag)) { // if tag stack contains related opening tag, close all tags back up to opener
                            while (tagStack.contains(startTag)) {
                                String closer = tagStack.get(tagStack.size() - 1);
                                // If tag being closed is the current one insert at the end
                                // Otherwise insert after content of the opening tag
                                int idx = closer.equals(startTag) ? outputList.size() : outputList.lastIndexOf(closer) + 2;
                                closer = new StringBuffer(closer).insert(1, "/").toString();
                                outputList.add(idx, closer);
                                tagStack.remove(tagStack.size() - 1);
                            }
                        } 
                        
                        // Tags added to output (or not) as appropriate. Skip addition below via continue.
                        continue;
                    }
                }
            }
            if(!tag.isBlank()) {
                prevNonBlankToken = tag;
            }
            
            outputList.add(tag);
        }
        
        // close any remaining unclosed tags in stack
        for (int i = tagStack.size(); i > 0; i--) {
            String closer = tagStack.get(i - 1);
            closer = new StringBuffer(closer).insert(1, "/").toString();
            outputList.add(closer);
        }

        source = String.join("", outputList);

        if (failures) { // if fail state, repopulate tokens and rerun
            populateTokenList();
            fixMissingTags();
        }
    }
}
