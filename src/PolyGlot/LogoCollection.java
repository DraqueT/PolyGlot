/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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
package PolyGlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author draque
 */
public class LogoCollection extends DictionaryCollection {
    private final Map<Integer, ArrayList<Integer>> logoToWord;
    private final Map<Integer, ArrayList<Integer>> wordToLogo;
    private final DictCore core;
    
    /**
     * Adds relation between logograph and word
     * @param word
     * @param logo 
     * @return true if added, false if existing already
     */
    public boolean addWordLogoRelation(ConWord word, LogoNode logo) {
        if (!logoToWord.containsKey(logo.getId())) {
            logoToWord.put(logo.getId(), new ArrayList<Integer>());
        }
        
        if(!wordToLogo.containsKey(word.getId())) {
            wordToLogo.put(word.getId(), new ArrayList<Integer>());
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
    
    public LogoCollection(DictCore _core) {
        wordToLogo = new HashMap<Integer, ArrayList<Integer>>();
        logoToWord = new HashMap<Integer, ArrayList<Integer>>();
        bufferNode = new LogoNode();
        
        core = _core;
    }
       
    /**
     * Gets all logographs in language
     * @return list of all logographs
     */
    public List<LogoNode> getAllLogos() {
        List<LogoNode> retList = new ArrayList<LogoNode>(nodeMap.values());

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
        List<LogoNode> retList = new ArrayList<LogoNode>();
        Iterator<LogoNode> it = getAllLogos().iterator();
        boolean ignoreCase = core.getPropertiesManager().isIgnoreCase();
        
        while (it.hasNext()) {
            LogoNode curNode = it.next();

            if (!reading.trim().equals("") && !curNode.containsReading(reading, ignoreCase)) {
                continue;
            } else if (!radical.trim().equals("") && !curNode.containsRadicalString(radical, ignoreCase)) {
                continue;
            } else if (strokes != 0 && strokes != curNode.getStrokes()) {
                continue;
            } else if (!relWord.trim().equals("") && !logoRelatedToWord(curNode, relWord)) {
                continue;
            } else if (!notes.trim().equals("") && 
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
                try {
                    ConWord curWord = core.getWordCollection().getNodeById(it.next());
                    if ((ignoreCase && curWord.getValue().equalsIgnoreCase(relWord))
                            || curWord.getValue().equals(relWord)) {
                        ret = true;
                        break;
                    }
                } catch (Exception e) {/*do nothing*/}
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
        List<LogoNode> retList = new ArrayList<LogoNode>();
        List<Integer> initialList = wordToLogo.get(conWord.getId());
        Iterator<Integer> it = null;
        
        if (initialList != null) {
            it = initialList.iterator();
        }
        
        while (it != null && it.hasNext()) {
            LogoNode curNode = (LogoNode)nodeMap.get(it.next());
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
        List<ConWord> retList = new ArrayList<ConWord>();
        List<Integer>initialList = logoToWord.get(logoNode.getId());
        Iterator<Integer> it = null;
        
        if (initialList != null) {
            it = initialList.iterator();
        }
        
        while (it != null && it.hasNext()) {
            try {
                ConWord curNode = core.getWordCollection().getNodeById(it.next());
                retList.add(curNode);
            } catch (Exception e) {/*Do nothing*/}
                        
        }
        
        return retList;
    }
    
    /**
     * returns list of only logonodes which are radicals
     * @return 
     */
    public List<LogoNode> getRadicals() {
        List<LogoNode> retList = new ArrayList<LogoNode>();
        Iterator<LogoNode> it = new ArrayList<LogoNode>(nodeMap.values()).iterator();

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
        Element logoRoot = doc.createElement(PGTUtil.logoRootNoteXID);
        rootElement.appendChild(logoRoot);
        
        // write all logographs to XML
        Iterator<LogoNode> it = getAllLogos().iterator();
        Element logoCollection = doc.createElement(PGTUtil.logoGraphsCollectionXID);
        logoRoot.appendChild(logoCollection);
        while (it.hasNext()) {
            Element logoGraph = doc.createElement(PGTUtil.logoGraphNodeXID);
            Element node;
            
            LogoNode curNode = it.next();
            
            node = doc.createElement(PGTUtil.logoGraphIdXID);
            node.appendChild(doc.createTextNode(curNode.getId().toString()));
            logoGraph.appendChild(node);
            
            node = doc.createElement(PGTUtil.logoGraphValueXID);
            node.appendChild(doc.createTextNode(curNode.getValue()));
            logoGraph.appendChild(node);
            
            node = doc.createElement(PGTUtil.logoIsRadicalXID);
            node.appendChild(doc.createTextNode(curNode.isRadical()?"T":"F"));
            logoGraph.appendChild(node);
            
            node = doc.createElement(PGTUtil.logoNotesXID);
            node.appendChild(doc.createTextNode(curNode.getNotes()));
            logoGraph.appendChild(node);
            
            node = doc.createElement(PGTUtil.logoRadicalListXID);
            node.appendChild(doc.createTextNode(curNode.getRadicalListString()));
            logoGraph.appendChild(node);
            
            node = doc.createElement(PGTUtil.logoStrokesXID);
            node.appendChild(doc.createTextNode(curNode.getStrokes().toString()));
            logoGraph.appendChild(node);
            
            Iterator<String> readings = curNode.getReadings().iterator();
            while (readings.hasNext()) {
                String curReading = readings.next();
                
                node = doc.createElement(PGTUtil.logoReadingXID);
                node.appendChild(doc.createTextNode(curReading));
                logoGraph.appendChild(node);
            }
            
            logoCollection.appendChild(logoGraph);
        }
        
        // write all logo->word relations to XML (reverse will be inferred on load)
        Iterator<Entry<Integer, ArrayList<Integer>>> setIt = logoToWord.entrySet().iterator();
        Element relationsCollection = doc.createElement(PGTUtil.logoRelationsCollectionXID);
        logoRoot.appendChild(relationsCollection);
        while (setIt.hasNext()) {
            Entry<Integer, ArrayList<Integer>> curEntry = setIt.next();
            Iterator<Integer> relIt = curEntry.getValue().iterator();
            String logoId = curEntry.getKey().toString();
            String wordIds = "";
            
            while (relIt.hasNext()) {
                wordIds += ("," + relIt.next().toString());
            }
            
            // only add if there is one more more relation
            if (!wordIds.equals("")) {
                Element node = doc.createElement(PGTUtil.logoWordRelationXID);
                // node is encoded with the logograph ID first, followed by all related words IDs
                node.appendChild(doc.createTextNode(logoId + wordIds));
                relationsCollection.appendChild(node);
            }
        }
    }
    
    public LogoNode getBufferNode() {
        return (LogoNode)bufferNode;
    }
    
    public int insert() throws Exception {
        int ret = insert(bufferNode.getId(), bufferNode);
        
        bufferNode = new LogoNode();
        
        return ret;
    }
    
    /**
     * Loads logograph and word relations with one another from comma delimited
     * string of node IDs
     * @param relations comma delimited list
     */
    public void loadLogoRelations(String relations) throws Exception {
        String[] ids = relations.split(",");
        LogoNode relNode = null;
        String loadLog = "";
        
        try {
            relNode = (LogoNode)getNodeById(Integer.parseInt(ids[0]));
        } catch (Exception e) {
            throw new Exception("Unable to load logograph relations.");
        }
        
        if (relNode == null) {
            return;
        }
        
        for (int i = 1; i < ids.length; i++) {
            try {
                ConWord word = (ConWord)core.getWordCollection().getNodeById(
                        Integer.parseInt(ids[i]));
                
                addWordLogoRelation(word, relNode);
            } catch (Exception e) {
                loadLog += "\nLogograph load error: " + e.getLocalizedMessage();
            }
        }
        
        if (!loadLog.equals("")) {
            throw new Exception("\nLogograph load errors:");
        }
    }
    
    /**
     * after pass 1 file loading, this tells all logoNodes to load their radicals
     */
    public void loadRadicalRelations() {
        Iterator<LogoNode> it = new ArrayList<LogoNode>(nodeMap.values()).iterator();
        
        while (it.hasNext()) {
            LogoNode curNode = it.next();
            curNode.loadRadicalRelations(nodeMap);
        }
    }
    
    @Override
    public void clear() {
        bufferNode = new LogoNode();
    }
    
}
