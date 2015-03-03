/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 * See LICENSE.TXT included with this code to read the full license agreement.
 *
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class PronunciationMgr {

    private final DictCore core;

    public PronunciationMgr(DictCore _core) {
        core = _core;
    }

    private List<PronunciationNode> pronunciations = new ArrayList<PronunciationNode>();

    /**
     * Sets list of pronunciations
     *
     * @param _pronunciations new list to replace old
     */
    public void setPronunciations(List<PronunciationNode> _pronunciations) {
        pronunciations = _pronunciations;
    }

    /**
     * gets iterator with all pronunciation pairs
     *
     * @return list of PronunciationNodes
     */
    public Iterator<PronunciationNode> getPronunciations() {
        return pronunciations.iterator();
    }

    /**
     * Returns index of pronunciation
     *
     * @param node to search for
     * @return node's index, -1 = not found
     */
    public int getProcIndex(PronunciationNode node) {
        int ret = -1;

        for (int i = 0; i < pronunciations.size(); i++) {
            if (node.equals(pronunciations.get(i))) {
                ret = i;
                break;
            }
        }

        return ret;
    }

    /**
     * Inserts a node at an arbitrary position
     *
     * @param index position to insert
     * @param newNode node to be inserted
     */
    public void addAtPosition(int index, PronunciationNode newNode) {
        pronunciations.add(index, newNode);
    }

    /**
     * replaces the pronunciation node at given index
     *
     * @param index index to modify
     * @param newNode new node
     */
    public void modifyProc(int index, PronunciationNode newNode) {
        pronunciations.remove(index);
        pronunciations.add(index, newNode);
    }

    /**
     * moves a pronunciation up one slot to increase priority by 1
     *
     * @param index index of node to move up
     */
    public void moveProcUp(int index) {
        PronunciationNode node = pronunciations.get(index);

        // -1 = not found, size 0 = start of list
        if (index == -1 || index == 0) {
            return;
        }

        pronunciations.remove(index);
        pronunciations.add(index - 1, node);
    }

    /**
     * moves a pronunciation down one slot to decrease priority by 1
     *
     * @param index index of the node to move down
     */
    public void moveProcDown(int index) {
        PronunciationNode node = pronunciations.get(index);

        // -1 = not found, size - 1 = end of list already
        if (index == -1 || index == pronunciations.size() - 1) {
            return;
        }

        pronunciations.remove(index);
        pronunciations.add(index + 1, node);
    }

    public void deletePronunciation(PronunciationNode remove) {
        List<PronunciationNode> newProcs = new ArrayList<PronunciationNode>();

        for (PronunciationNode curNode : pronunciations) {
            if (curNode.equals(remove)) {
                continue;
            }

            newProcs.add(curNode);
        }

        pronunciations = newProcs;
    }

    public void addPronunciation(PronunciationNode newNode) {
        pronunciations.add(newNode);
    }

    /**
     * Returns pronunciation of a given word
     *
     * @param base word to find pronunciation of
     * @return pronunciation string. If no perfect match found, empty string
     * returned
     */
    public String getPronunciation(String base) {
        String ret = "";

        Iterator<PronunciationNode> procCycle = getPronunciationElements(base, true).iterator();
        while (procCycle.hasNext()) {
            PronunciationNode curProc = procCycle.next();
            ret += curProc.getPronunciation() + " ";
        }

        return ret;
    }

    /**
     * Returns pronunciation elements of word
     *
     * @param base word to find pronunciation elements of
     * @return elements of pronunciation for word. Empty if no perfect match
     * found
     */
    public List<PronunciationNode> getPronunciationElements(String base) {
        return getPronunciationElements(base, true);
    }
    
    /**
     * returns pronunciation objects of a given word
     *
     * @param base word to find pronunciation objects of
     * @param isFirst set to true if first iteration.
     * @return pronunciation object list. If no perfect match found, empty
     * string returned
     */
    private List<PronunciationNode> getPronunciationElements(String base, boolean isFirst) {
        List<PronunciationNode> ret = new ArrayList<PronunciationNode>();
        Iterator<PronunciationNode> finder = getPronunciations();

        // return blank for empty string
        if (base.length() == 0 || !finder.hasNext()) {
            return ret;
        }

        while (finder.hasNext()) {
            PronunciationNode curNode = finder.next();
            String pattern = curNode.getValue();

            // split logic here to use either string comparison or regex matching
            if (core.getPropertiesManager().isDisableProcRegex()) {
                // do not overstep string
                if (pattern.length() > base.length()) {
                    continue;
                }

                // capture string to compare based on pattern length
                String comp = base.substring(0, curNode.getValue().length());
                
                if (core.getPropertiesManager().isIgnoreCase()) {
                    comp = comp.toLowerCase();
                    pattern = pattern.toLowerCase();
                }
                
                if (comp.equals(pattern)) {
                    List<PronunciationNode> temp
                            = getPronunciationElements(base.substring(pattern.length(), base.length()), false);

                    // if lengths are equal, success! return. If unequal and no further match found-failure
                    if (pattern.length() == base.length() || !temp.isEmpty()) {
                        ret.add(curNode);
                        ret.addAll(temp);
                        break;
                    }
                }
            } else {
                // skip if set as starting characters, but later in word
                if (pattern.startsWith("^") && !isFirst) {
                    continue;
                }

                // make pattern a starting pattern if not already, if it is already, allow it to accept following strings
                if (!pattern.startsWith("^")) {
                    pattern = "^(" + pattern + ").*";
                } else {
                    pattern = "^(" + pattern.substring(1) + ").*";
                }

                Pattern findString = Pattern.compile(pattern);
                Matcher matcher = findString.matcher(base);

                if (matcher.matches()) {
                    String leadingChars = matcher.group(1);
                    List<PronunciationNode> temp
                            = getPronunciationElements(base.substring(leadingChars.length(), base.length()), false);

                    // if lengths are equal, success! return. If unequal and no further match found-failure
                    if (leadingChars.length() == base.length() || !temp.isEmpty()) {
                        ret.add(curNode);
                        ret.addAll(temp);
                        break;
                    }
                }
            }
        }

        return ret;
    }
    
    /**
     * Writes all pronunciation information to XML document
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Iterator<PronunciationNode> procGuide = getPronunciations();
        Element wordNode;
        Element wordValue;
        
        while (procGuide.hasNext()) {
            PronunciationNode curNode = procGuide.next();

            wordNode = doc.createElement(PGTUtil.proGuideXID);
            rootElement.appendChild(wordNode);

            wordValue = doc.createElement(PGTUtil.proGuideBaseXID);
            wordValue.appendChild(doc.createTextNode(curNode.getValue()));
            wordNode.appendChild(wordValue);

            wordValue = doc.createElement(PGTUtil.proGuidePhonXID);
            wordValue.appendChild(doc.createTextNode(curNode.getPronunciation()));
            wordNode.appendChild(wordValue);
        }
    }
}
