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

/**
 *
 * @author draque
 */
// TODO: replace type string with type ID... offer some way to current users to
// upgrade their saved dictionaries.
public class ConWord extends DictNode {

    // so long as the conword is not blank, this can be blank
    private String localWord;
    private String wordType;
    private String definition;
    private String pronunciation;
    private String gender;
    private String plural;
    private boolean procOverride;

    public String getPlural() {
        return plural;
    }

    public void setPlural(String _plural) {
        plural = _plural;
    }

    public ConWord() {
        value = "";
        localWord = "";
        wordType = "";
        definition = "";
        pronunciation = "";
        gender = "";
        plural = "";
        id = -1;
        procOverride = false;
    }

    /**
     * @param _set sets all non ID values equal to that of parameter
     */
    @Override
    public void setEqual(DictNode _set) {
        ConWord set = (ConWord) _set;
        
        this.setValue(set.getValue());
        this.setLocalWord(set.getLocalWord());
        this.setWordType(set.getWordType());
        this.setDefinition(set.getDefinition());
        this.setPronunciation(set.getPronunciation());
        this.setId(set.getId());
        this.setGender(set.getGender());
        this.setPlural(set.getPlural());
        this.setProcOverride(set.isProcOverride());
    }

    public boolean isProcOverride() {
        return procOverride;
    }
    
    public void setProcOverride(boolean _procOverride) {
        procOverride = _procOverride;
    }
    
    public String getLocalWord() {
        return localWord;
    }

    public void setLocalWord(String localWord) {
        this.localWord = localWord.trim();
    }

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType.trim();
    }

    /**
     * Returns false if the word is invalid for any reason
     *
     * @return false if invalid
     */
    public boolean checkValid() {
        boolean ret = true;

        // There might be no local translation, but the constructed word must exist
        ret = ret && (!value.equals(""));

        return ret;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
