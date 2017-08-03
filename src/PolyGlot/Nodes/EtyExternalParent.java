/*
 * Copyright (c) 2017, DThompson
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
package PolyGlot.Nodes;

/**
 * This node represents an external etymological parent
 * @author DThompson
 */
public class EtyExternalParent {
    
    private String externalWord = "";
    private String externalLanguage = "";
    private String definition = "";

    /**
     * @return the externalWord
     */
    public String getExternalWord() {
        return externalWord;
    }

    /**
     * @param externalWord the externalWord to set
     */
    public void setExternalWord(String externalWord) {
        this.externalWord = externalWord;
    }

    /**
     * @return the externalLanguage
     */
    public String getExternalLanguage() {
        return externalLanguage;
    }

    /**
     * @param externalLanguage the externalLanguage to set
     */
    public void setExternalLanguage(String externalLanguage) {
        this.externalLanguage = externalLanguage;
    }
    
    public String getUniqueId() {
        return externalWord + externalLanguage;
    }
    
    @Override
    public String toString() {
        return externalWord + " (" + externalLanguage + ")";
    }

    /**
     * @return the definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param definition the definition to set
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
