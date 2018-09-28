/*
 * Copyright (c) 2016-2018, Draque Thompson
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

import PolyGlot.CustomControls.GrammarChapNode;
import PolyGlot.CustomControls.GrammarSectionNode;
import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PPanelDrawEtymology;
import PolyGlot.ManagersCollections.DictionaryCollection;
import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.Nodes.DeclensionPair;
import PolyGlot.Nodes.ImageNode;
import PolyGlot.Nodes.PEntry;
import PolyGlot.Nodes.PronunciationNode;
import PolyGlot.Nodes.WordClassValue;
import PolyGlot.Nodes.WordClass;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Given a core dictionary, this class will print it to a PDF file.
 *
 * @author draque.thompson
 */
public class PExportToPDF {
    /**
     * Exports language to presentable PDF
     *
     * @param _core dictionary core
     * @param _targetFile target path to write
     * @throws IOException
     */
    public PExportToPDF(DictCore _core, String _targetFile) throws IOException {
        throw new IOException("This feature is disabled in your current version of PolyGlot.");
    }

    /**
     * Prints PDF document given parameters provided
     *
     * @throws java.io.FileNotFoundException
     */
    public void print() throws FileNotFoundException, IOException, Exception {
        throw new IOException("This feature is disabled in your current version of PolyGlot.");
    }

    /**
     * @param printLocalCon the printLocalCon to set
     */
    public void setPrintLocalCon(boolean printLocalCon) {
    }

    /**
     * @param printConLocal the printConLocal to set
     */
    public void setPrintConLocal(boolean printConLocal) {
    }

    /**
     * @param printOrtho the printOrtho to set
     */
    public void setPrintOrtho(boolean printOrtho) {
    }

    /**
     * @param printGrammar the printGrammar to set
     */
    public void setPrintGrammar(boolean printGrammar) {
    }

    /**
     * @param coverImagePath the coverImagePath to set
     */
    public void setCoverImagePath(String coverImagePath) {
    }

    /**
     * @param forewardText the forewardText to set
     */
    public void setForewardText(String forewardText) {
    }

    /**
     * @param titleText the titleText to set
     */
    public void setTitleText(String titleText) {
    }

    /**
     * @param subTitleText the subTitleText to set
     */
    public void setSubTitleText(String subTitleText) {
    }

    public void setPrintAllConjugations(boolean _printAllConjugations) {
    }

    /**
     * @param printPageNumber the printPageNumber to set
     */
    public void setPrintPageNumber(boolean printPageNumber) {
    }

    /**
     * @param printGlossKey the printGlossKey to set
     */
    public void setPrintGlossKey(boolean printGlossKey) {
    }

    /**
     * @param printWordEtymologies the printWordEtymologies to set
     */
    public void setPrintWordEtymologies(boolean printWordEtymologies) {
    }
}
