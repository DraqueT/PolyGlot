/*
 * Copyright (c) 2023, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.DomParser;

import java.util.List;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.PropertiesManager;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.w3c.dom.Node;

/**
 *
 * @author draquethompson
 */
public class LangPropertiesParser extends BaseParser {

    public LangPropertiesParser(List<String> _parseIssues) {
        super(_parseIssues);
    }

    @Override
    public void consumeChild(Node node, DictCore core) throws Exception {
        PropertiesManager propMan = core.getPropertiesManager();
        
        switch (node.getNodeName()) {
            case PGTUtil.LANG_PROP_FONT_STYLE_XID -> {
                propMan.setFontStyle(Integer.valueOf(node.getTextContent()));
            }
            case PGTUtil.LANG_PROP_LOCAL_FONT_SIZE_XID -> {
                propMan.setLocalFontSize(Double.parseDouble(node.getTextContent()));
            }
            case PGTUtil.LANG_PROP_FONT_SIZE_XID -> {
                propMan.setFontSize(Double.parseDouble(node.getTextContent()));
            }  
            case PGTUtil.LANG_PROP_LANG_NAME_XID -> {
                propMan.setLangName(node.getTextContent());
            }
            case PGTUtil.LANG_PROP_ALPHA_ORDER_XID -> {
                propMan.setAlphaOrder(node.getTextContent());
            }
            case PGTUtil.LANG_PROP_TYPE_MAND_XID -> {
                propMan.setTypesMandatory(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.LANG_PROP_LOCAL_MAND_XID -> {
                propMan.setLocalMandatory(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.LANG_PROP_LOCAL_UNIQUE_XID -> {
                propMan.setLocalUniqueness(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.LANG_PROP_WORD_UNIQUE_XID -> {
                propMan.setWordUniqueness(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.LANG_PROP_IGNORE_CASE_XID -> {
                core.getPropertiesManager().setIgnoreCase(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.LANG_PROP_DISABLE_PROC_REGEX -> {
                core.getPropertiesManager().setDisableProcRegex(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.LANG_PROP_ENFORCE_RTL_XID_DEPRECATED -> {
                // deprecated feature: do nothing
            }
            case PGTUtil.LANG_PROP_OVERRIDE_REGEX_FONT_XID -> {
                propMan.setOverrideRegexFont(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.LANG_PROP_USE_LOCAL_LEX_XID -> {
                propMan.setUseLocalWordLex(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.LANG_PROP_AUTH_COPYRIGHT_XID -> {
                propMan.setCopyrightAuthorInfo(node.getTextContent());
            }
            case PGTUtil.LANG_PROP_LOCAL_NAME_XID -> {
                propMan.setLocalLangName(node.getTextContent());
            }
            case PGTUtil.LANG_PROP_USE_SIMPLIFIED_CONJ -> {
                propMan.setUseSimplifiedConjugations(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.LANG_PROP_EXPANDED_LEX_LIST_DISP -> {
                propMan.setExpandedLexListDisplay(node.getTextContent().equals(PGTUtil.TRUE));
            }
            case PGTUtil.LANG_PROP_ZOMPIST_CATEGORIES -> {
                propMan.setZompistCategories(node.getTextContent());
            }
            case PGTUtil.LANG_PROP_ZOMPIST_ILLEGAL_CLUSTERS -> {
                propMan.setZompistIllegalClusters(node.getTextContent());
            }
            case PGTUtil.LANG_PROP_ZOMPIST_REWRITE_RULES -> {
                propMan.setZompistRewriteRules(node.getTextContent());
            }
            case PGTUtil.LANG_PROP_ZOMPIST_SYLLABLES -> {
                propMan.setZompistSyllableTypes(node.getTextContent());
            }
            case PGTUtil.LANG_PROP_ZOMPIST_DROPOFF_RATE -> {
                propMan.setZompistDropoffRate(Integer.parseInt(node.getTextContent()));
            }
            case PGTUtil.LANG_PROP_ZOMPIST_MONOSYLLABLE_FREQUENCY -> {
                propMan.setZompistMonosylableFrequency(Integer.parseInt(node.getTextContent()));
            }
            case PGTUtil.LANG_PROP_CHAR_REP_CONTAINER_XID -> {
                new CharacterReplacementCollectionParser(parseIssues).parse(node, core);
            }
            case PGTUtil.FONT_CON_XID -> {
                propMan.setFontCon(node.getTextContent());
            }
            case PGTUtil.LANG_PROP_KERNING_DEPRECATED -> {
                // This is no longer supported. Simply ignore.
            }
            default ->
                throw new PDomException("Unexpected node in " + this.getClass().getName() + " : " + node.getNodeName());
        }
    }
}
