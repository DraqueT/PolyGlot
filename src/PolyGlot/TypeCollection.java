/*
 * Copyright (c) 2014, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under:
 * Creative Commons Attribution-NonCommercial 4.0 International Public License
 * 
 * Please see the included LICENSE.TXT file for the full text of this license.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author draque
 */
public class TypeCollection extends DictionaryCollection {

    public TypeNode getBufferType() {
        return (TypeNode) bufferNode;
    }

    public TypeCollection() {
        bufferNode = new TypeNode();
    }

    /**
     * Tests whether type based requirements met for word
     *
     * @param word word to check
     * @return empty if no problems, string with problem description otherwise
     */
    public String typeRequirementsMet(ConWord word) {
        String ret = "";

        TypeNode type = this.findTypeByName(word.getWordType());

        // all requirements met if no type set at all.
        if (type != null) {
            if (type.isDefMandatory() && word.getDefinition().equals("")) {
                ret = word.getWordType() + "requires a definition.";
            } else if (type.isGenderMandatory() && word.getGender().equals("")) {
                ret = word.getWordType() + "requires a gender.";
            } else if (type.isPluralMandatory() && word.getPlural().equals("")) {
                ret = word.getWordType() + "requires plurality.";
            } else if (type.isProcMandatory() && word.getPronunciation().equals("")) {
                ret = word.getWordType() + "requires a pronunciation.";
            }
        }

        return ret;
    }

    /**
     * Finds/returns type (if extant) by name
     *
     * @param _name
     * @return found type node, null otherwise
     */
    public TypeNode findTypeByName(String _name) {
        TypeNode ret = null;

        if (!_name.equals("")) {
            Iterator<Entry<Integer, TypeNode>> it = nodeMap.entrySet().iterator();
            Entry<Integer, TypeNode> curEntry;

            while (it.hasNext()) {
                curEntry = it.next();

                if (curEntry.getValue().getValue().equals(_name)) {
                    ret = curEntry.getValue();
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * inserts current buffer word to conWord list based on id; blanks out
     * buffer
     *
     * @param _id
     * @return
     * @throws Exception
     */
    public Integer insert(Integer _id) throws Exception {
        Integer ret;

        TypeNode insWord = new TypeNode();
        insWord.setEqual(bufferNode);
        insWord.setId(_id);

        ret = super.insert(_id, bufferNode);

        bufferNode = new TypeNode();

        return ret;
    }

    /**
     * inserts current buffer to conWord list and generates id; blanks out
     * buffer
     *
     * @return ID of newly created node
     * @throws Exception
     */
    public Integer insert() throws Exception {
        Integer ret;

        ret = super.insert(bufferNode);

        bufferNode = new TypeNode();

        return ret;
    }

    @Override
    public TypeNode getNodeById(Integer _id) throws Exception {
        return (TypeNode) super.getNodeById(_id);
    }

    @Override
    public void clear() {
        bufferNode = new TypeNode();
    }

    /**
     * returns iterator of nodes with their IDs as the entry key (ordered)
     *
     * @return
     */
    public Iterator<TypeNode> getNodeIterator() {
        List<TypeNode> retList = new ArrayList<TypeNode>(nodeMap.values());

        Collections.sort(retList);

        return retList.iterator();
    }

    boolean nodeExists(String findType) {
        boolean ret = false;
        Iterator<Map.Entry<Integer, TypeNode>> searchList = nodeMap.entrySet()
                .iterator();

        while (searchList.hasNext()) {
            Map.Entry<Integer, TypeNode> curEntry = searchList.next();
            TypeNode curType = curEntry.getValue();

            if (curType.getValue().equals(findType)) {
                ret = true;
                break;
            }
        }

        return ret;
    }
}
