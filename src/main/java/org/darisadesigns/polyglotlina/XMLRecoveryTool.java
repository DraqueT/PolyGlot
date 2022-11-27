/*
 * Copyright (c) 2022, Draque Thompson, draquemail@gmail.com
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author draque
 */
public class XMLRecoveryTool {
    
    private String source;
    private List<String> outputList;
    private final List<String> tokenList;
    
    
    public XMLRecoveryTool(String _source) {
        source = _source;
        tokenList = new ArrayList<>();
    }
    
    public String  recoverXml() {
        this.populateTokenList();
        this.fixMissingTags();

        return source;
    }
    
    private void populateTokenList() {
        tokenList.clear();
        
        var regex = "<[^<]*>|[^<]*"; // fetch encapsulated tags and data
        
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(source);

        while (m.find()) {
            tokenList.add(m.group());
        }
    }
    
    private void fixMissingTags() {
        var tagStack = new ArrayList<String>();
        outputList = new ArrayList<>();
        boolean failures = false;
        
        
        // first element is header
        outputList.add(tokenList.get(0));
        
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
                    } else { // Either an opening or closing tag is missing. Mark with failures
                        failures = true;

                        if (tagStack.contains(startTag)) { // if tag stack contains related opening tag, close all tags back up to opener
                            while (tagStack.contains(startTag)) {
                                String closer = tagStack.get(tagStack.size() - 1);
                                closer = new StringBuffer(closer).insert(1, "/").toString();
                                outputList.add(closer);
                                tagStack.remove(tagStack.size() - 1);
                            }
                        } 
                        
                        // Tags added to output (or not) as appropriate. Skip addition below via continue.
                        continue;
                    }
                }
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
    

    
    /***
     * removes string node from XML at given index
     * @param index
     */
    private void removeNodeAt(int index) {
        var targetLength = tokenList.get(index).length();
        var targetLocation = this.getLengthToNode(index);
        
        source = source.substring(0, targetLocation) 
                + source.substring(targetLocation + targetLength);
    }
    
    /***
     * Gets string length to node at given index
     * @param index
     * @return 
     */
    private int getLengthToNode(int index) {
        var length = 0;
        
        for (var i = 0; i < index; i++) {
            length += tokenList.get(i).length();
        }
        
        return length;
    }
}
