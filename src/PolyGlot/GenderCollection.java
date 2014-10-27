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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author draque
 */
public class GenderCollection extends DictionaryCollection{
    private final DictCore core;
    
    public GenderCollection(DictCore _core){
        bufferNode = new GenderNode();
        core = _core;
    }
    
    @Override
    public void clear() {
        bufferNode = new GenderNode();
    }
    
    public GenderNode getGenderBuffer(){
        return (GenderNode)bufferNode;
    }
    
    @Override
    /**
     * in addition to passing this to its super, this deletes all instances of
     * the gender's appearance in conwords
     */
    public void deleteNodeById(Integer _id) throws Exception {
        Iterator<ConWord> it;
        ConWord genderWord = new ConWord();
        GenderNode delGender = getNodeById(_id);

        genderWord.setGender(delGender.getValue());
        genderWord.setValue("");
        genderWord.setDefinition("");
        genderWord.setWordType("");
        genderWord.setLocalWord("");
        genderWord.setPronunciation("");

        it = core.getWordCollection().filteredList(genderWord);

        while (it.hasNext()) {
            ConWord modWord = it.next();

            modWord.setGender("");

            core.getWordCollection().modifyNode(modWord.getId(), modWord);
        }
        
        super.deleteNodeById(_id);
    }
    
    /**
     * Finds/returns gender (if extant) by name
     *
     * @param _name
     * @return found gender node, null otherwise
     */
    public GenderNode findGenderByName(String _name) {
        GenderNode ret = null;

        if (!_name.equals("")) {
            Iterator<Entry<Integer, GenderNode>> it = nodeMap.entrySet().iterator();
            Entry<Integer, GenderNode> curEntry;

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
        
        GenderNode insWord = new GenderNode();
        insWord.setEqual(bufferNode);
        insWord.setId(_id);

        ret = super.insert(_id, bufferNode);
        
        bufferNode = new GenderNode();
        
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
        
        bufferNode = new GenderNode();
        
        return ret;
    }
    
    @Override
    public GenderNode getNodeById(Integer _id) throws Exception {
        return (GenderNode)super.getNodeById(_id);
    }
    
    /**
     * returns iterator of nodes with their IDs as the entry key (ordered)
     * @return 
     */
    public Iterator<GenderNode> getNodeIterator() {
        List<GenderNode> retList = new ArrayList<GenderNode>(nodeMap.values());

        Collections.sort(retList);

        return retList.iterator();
    }
    
    boolean nodeExists(String findType) {
        boolean ret = false;        
        Iterator<Map.Entry<Integer, GenderNode>> searchList = nodeMap.entrySet()
                .iterator();
        
        while (searchList.hasNext()) {
            Map.Entry<Integer, GenderNode> curEntry = searchList.next();
            GenderNode curType = curEntry.getValue();
            
            if (curType.getValue().equals(findType)) {
                ret = true;
                break;
            }
        }
        
        return ret;
    }
    
    @Override
    /**
     * Safely modify a gender (updates words of this gender automatically
     *
     * @param id gender id
     * @param modGender new gender
     * @throws Exception
     */
    public void modifyNode(Integer id, DictNode modGender) throws Exception {
        Iterator<ConWord> it;
        ConWord genderWord = new ConWord();

        genderWord.setGender(getNodeById(id).getValue());
        genderWord.setValue("");
        genderWord.setDefinition("");
        genderWord.setWordType("");
        genderWord.setLocalWord("");
        genderWord.setPronunciation("");

        it = core.getWordCollection().filteredList(genderWord);

        while (it.hasNext()) {
            ConWord modWord = it.next();

            modWord.setGender(modGender.getValue());

            core.getWordCollection().modifyNode(modWord.getId(), modWord);
        }

        super.modifyNode(id, modGender);
    }
}
