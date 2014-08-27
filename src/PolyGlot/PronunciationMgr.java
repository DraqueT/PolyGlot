/*
 * Copyright (c) 2014, Draque Thompson, draquemail@gmail.com
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

/**
 *
 * @author draque
 */
public class PronunciationMgr {

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
     * moves a pronunciation up one slot to increase priority by 1
     * @param node pronunciation to reposition
     */
    public void moveProcUp(PronunciationNode node) {
        int index = getProcIndex(node);
        
        // 0 = highest possible position, -1 = not found
        if (index == 0 || index == -1) {
            return;
        }

        pronunciations.remove(index);
        pronunciations.add(index - 1, node);
    }
    /**
     *  moves a pronunciation down one slot to decrease priority by 1
     * @param node pronunciation to reposition
     */
    public void moveProcDown(PronunciationNode node) {
        int index = getProcIndex(node);
        
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

        Iterator<PronunciationNode> procCycle = getPronunciationElements(base).iterator();
        while (procCycle.hasNext()) {
            PronunciationNode curProc = procCycle.next();
            ret += curProc.getPronunciation() + " ";
        }

        return ret;
    }
    
    /**
     * returns pronunciation objects of a given word
     * @param base word to find pronunciation objects of
     * @return pronunciation object list. If no perfect match found, empty
     * string returned
     */
    public List<PronunciationNode> getPronunciationElements(String base) {
        List<PronunciationNode> ret = new ArrayList<PronunciationNode>();
        Iterator<PronunciationNode> finder = getPronunciations();

        // return blank for empty string
        if (base.length() == 0 || !finder.hasNext()) {
            return ret;
        }

        while (finder.hasNext()) {
            PronunciationNode curNode = finder.next();

            // do not overstep string
            if (curNode.getValue().length() > base.length()) {
                continue;
            }

            String comp = base.substring(0, curNode.getValue().length());
            if (comp.equals(curNode.getValue())) {
                List<PronunciationNode> temp = getPronunciationElements(base.substring(curNode.getValue().length(), base.length()));

                // if lengths are equal, success! return. If unequal and no further match found-failure
                if (curNode.getValue().length() == base.length() || !temp.isEmpty()) {
                    ret.add(curNode);
                    ret.addAll(temp);
                    break;
                }
            }
        }

        return ret;
    }
}
