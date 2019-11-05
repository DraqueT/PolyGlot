/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 *  See LICENSE.TXT included with this code to read the full license agreement.

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
package org.darisadesigns.polyglotlina.ManagersCollections;

import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.IOHandler;
import org.darisadesigns.polyglotlina.Nodes.LogoNode;
import org.darisadesigns.polyglotlina.PGTUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class LogoCollection extends DictionaryCollection<LogoNode> {
    private final Map<Integer, ArrayList<Integer>> logoToWord;
    private final Map<Integer, ArrayList<Integer>> wordToLogo;
    private final DictCore core;
    
    public LogoCollection(DictCore _core) {
        super(new LogoNode());
        
        wordToLogo = new HashMap<>();
        logoToWord = new HashMap<>();
        
        core = _core;
    }
    
    /**
     * Adds relation between logograph and word
     * @param word
     * @param logo 
     * @return true if added, false if existing already
     */
    public boolean addWordLogoRelation(ConWord word, LogoNode logo) {
        if (!logoToWord.containsKey(logo.getId())) {
            logoToWord.put(logo.getId(), new ArrayList<>());
        }
        
        if(!wordToLogo.containsKey(word.getId())) {
            wordToLogo.put(word.getId(), new ArrayList<>());
        }
        
        if (wordToLogo.get(word.getId()).contains(logo.getId())) {
            return false;
        }
        
        logoToWord.get(logo.getId()).add(word.getId());
        wordToLogo.get(word.getId()).add(logo.getId());
        
        return true;
    }
    
    /**
     * Removes relation between logograph and word
     * @param word
     * @param logo 
     */
    public void removeWordLogoRelation(ConWord word, LogoNode logo) {
        logoToWord.get(logo.getId()).remove(word.getId());
        wordToLogo.get(word.getId()).remove(logo.getId());
    }
    
    /**
     * Deletes LogoNode by ID. Ensures all words relations are struck.
     * @param _id ID to delete
     * @throws Exception if no ID exists as listed
     */
    @Override
    public void deleteNodeById(Integer _id) throws Exception {
        LogoNode logo = (LogoNode)getNodeById(_id);
        Iterator<ConWord> it = getLogoWords(logo).iterator();
        
        while (it.hasNext()) {
            ConWord word = it.next();
            
            removeWordLogoRelation(word, logo);
        }
        
        super.deleteNodeById(_id);
    }
       
    /**
     * Gets all logographs in language
     * @return list of all logographs
     */
    public List<LogoNode> getAllLogos() {
        List<LogoNode> retList = new ArrayList<>(nodeMap.values());

        Collections.sort(retList);

        return retList;
    }
    
    /**
     * Returns list of logoNodes that match given filters
     * @param reading reading filter (logonode contains)
     * @param relWord related word filter
     * @param radical radical filter (by name)
     * @param strokes filter by num strokes
     * @param notes filter by string found in notes
     * @return 
     */
    public List<LogoNode> getFilteredList(String reading, 
            String relWord, 
            String radical, 
            int strokes, 
            String notes) {
        List<LogoNode> retList = new ArrayList<>();
        Iterator<LogoNode> it = getAllLogos().iterator();
        boolean ignoreCase = core.getPropertiesManager().isIgnoreCase();
        
        while (it.hasNext()) {
            LogoNode curNode = it.next();

            if (!reading.trim().isEmpty() && !curNode.containsReading(reading, ignoreCase)) {
                continue;
            } else if (!radical.trim().isEmpty() && !curNode.containsRadicalString(radical, ignoreCase)) {
                continue;
            } else if (strokes != 0 && strokes != curNode.getStrokes()) {
                continue;
            } else if (!relWord.trim().isEmpty() && !logoRelatedToWord(curNode, relWord)) {
                continue;
            } else if (!notes.trim().isEmpty() &&
                    ((ignoreCase && !curNode.getNotes().toLowerCase().contains(notes.toLowerCase())) 
                            || !curNode.getNotes().contains(notes))) {
                continue;                
            }            
            
            retList.add(curNode);
        }
        
        return retList;
    }
    
    /**
     * tests whether a logonode is related to a word via string search
     * @param node logonode in question
     * @param relWord string representation of word to be searched
     * @return true if related, false otherwise
     */
    private boolean logoRelatedToWord(LogoNode node, String relWord) {
        boolean ret = false;
        boolean ignoreCase = core.getPropertiesManager().isIgnoreCase();
        
        if (logoToWord.containsKey(node.getId())) {
            Iterator<Integer> it = logoToWord.get(node.getId()).iterator();
            
            while (it.hasNext()) {
                ConWord curWord = core.getWordCollection().getNodeById(it.next());
                if ((ignoreCase && curWord.getValue().equalsIgnoreCase(relWord))
                        || curWord.getValue().equals(relWord)) {
                    ret = true;
                    break;
                }
            }
        }
        
        return ret;
    }
    
    /**
     * Gets list of all logographs for given word
     * @param conWord word to search on
     * @return list of related logographs
     */
    public List<LogoNode> getWordLogos(ConWord conWord) {
        List<LogoNode> retList = new ArrayList<>();
        List<Integer> initialList = wordToLogo.get(conWord.getId());
        Iterator<Integer> it = null;
        
        if (initialList != null) {
            it = initialList.iterator();
        }
        
        while (it != null && it.hasNext()) {
            LogoNode curNode = nodeMap.get(it.next());
            retList.add(curNode);
        }
        
        return retList;
    }
    
    /**
     * Gets list of all ConWords for given word
     * @param logoNode logogram to search on
     * @return list of related words
     */
    public List<ConWord> getLogoWords(LogoNode logoNode) {
        List<ConWord> retList = new ArrayList<>();
        List<Integer>initialList = logoToWord.get(logoNode.getId());
        Iterator<Integer> it = null;
        
        if (initialList != null) {
            it = initialList.iterator();
        }
        
        while (it != null && it.hasNext()) {
            ConWord curNode = core.getWordCollection().getNodeById(it.next());
            retList.add(curNode);
        }
        
        return retList;
    }
    
    /**
     * returns list of only logonodes which are radicals
     * @return 
     */
    public List<LogoNode> getRadicals() {
        List<LogoNode> retList = new ArrayList<>();
        Iterator<LogoNode> it = new ArrayList<>(nodeMap.values()).iterator();

        while (it.hasNext()) {
            LogoNode curNode = it.next();
            if (curNode.isRadical()) {
                retList.add(curNode);
            }
        }
        
        Collections.sort(retList);

        return retList;
    }
    
    /**
     * Writes all word information to XML document
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element logoRoot = doc.createElement(PGTUtil.LOGO_ROOT_NOTE_XID);
        rootElement.appendChild(logoRoot);
        
        // write all logographs to XML
        Element logoCollection = doc.createElement(PGTUtil.LOGOGRAPHS_COLLECTION_XID);
        logoRoot.appendChild(logoCollection);
        
        getAllLogos().forEach((logo)->{
            logo.writeXML(doc, logoCollection);
        });
        
        // write all logo->word relations to XML (reverse will be inferred on load)
        Iterator<Entry<Integer, ArrayList<Integer>>> setIt = logoToWord.entrySet().iterator();
        Element relationsCollection = doc.createElement(PGTUtil.LOGO_RELATION_COLLECTION_XID);
        logoRoot.appendChild(relationsCollection);
        while (setIt.hasNext()) {
            Entry<Integer, ArrayList<Integer>> curEntry = setIt.next();
            Iterator<Integer> relIt = curEntry.getValue().iterator();
            String logoId = curEntry.getKey().toString();
            String wordIds = "";
            
            while (relIt.hasNext()) {
                wordIds += ("," + relIt.next());
            }
            
            // only add if there is one more more relation
            if (!wordIds.isEmpty()) {
                Element node = doc.createElement(PGTUtil.LOGO_WORD_RELATION_XID);
                // node is encoded with the logograph ID first, followed by all related words IDs
                node.appendChild(doc.createTextNode(logoId + wordIds));
                relationsCollection.appendChild(node);
            }
        }
    }
    
    public LogoNode getBufferNode() {
        return bufferNode;
    }
    
    @Override
    public Integer insert() throws Exception {
        int ret = insert(bufferNode.getId(), bufferNode);
        
        bufferNode = new LogoNode();
        
        return ret;
    }
    
    /**
     * Loads logograph and word relations with one another from comma delimited
     * string of node IDs
     * @param relations comma delimited list
     * @throws java.lang.Exception if problems loading logo relations
     */
    public void loadLogoRelations(String relations) throws Exception {
        String[] ids = relations.split(",");
        LogoNode relNode;
        String loadLog = "";
        
        try {
            relNode = (LogoNode)getNodeById(Integer.parseInt(ids[0]));
        } catch (NumberFormatException e) {
            throw new Exception("Unable to load logograph relations.", e);
        }
        
        if (relNode == null) {
            return;
        }
        
        for (int i = 1; i < ids.length; i++) {
            try {
                ConWord word = core.getWordCollection().getNodeById(Integer.parseInt(ids[i]));
                addWordLogoRelation(word, relNode);
            } catch (NumberFormatException e) {
                IOHandler.writeErrorLog(e);
                loadLog += "\nLogograph load error: " + e.getLocalizedMessage();
            }
        }
        
        if (!loadLog.isEmpty()) {
            throw new Exception("\nLogograph load errors:");
        }
    }
    
    /**
     * after pass 1 file loading, this tells all logoNodes to load their radicals
     * @throws java.lang.Exception if problems with loading any radical relations
     */
    public void loadRadicalRelations() throws Exception {
        Iterator<LogoNode> it = new ArrayList<>(nodeMap.values()).iterator();
        String loadLog = "";
        while (it.hasNext()) {
            try {
                LogoNode curNode = it.next();
                curNode.loadRadicalRelations(nodeMap);
            } catch (Exception e) {
                IOHandler.writeErrorLog(e);
                loadLog = e.getLocalizedMessage() + "\n";
            }
        }
        
        if (!loadLog.isEmpty()) {
            throw new Exception("Problem loading radicals:\n" + loadLog);
        }
    }
    
    @Override
    public void clear() {
        bufferNode = new LogoNode();
    }

    @Override
    public Object notFoundNode() {
        LogoNode emptyNode = new LogoNode();
        
        try {
            emptyNode.setLogoGraph(ImageIO.read(getClass().getResource(PGTUtil.NOT_FOUND_IMAGE)));
        } catch (IOException e) {
            IOHandler.writeErrorLog(e);
            InfoBox.error("INTERNAL ERROR", 
                    "Unable to locate missing-image image.\nThis is kind of an ironic error.", null);
        }
        
        emptyNode.setValue("LOGO NOT FOUND");
        
        return emptyNode;
    }
}
