/*
 * Copyright (c) 2014-2022, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
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

package org.darisadesigns.polyglotlina;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This contains various constant vales in PolyGlot
 *
 * @author draque
 */
public class PGTUtil {

    // CONSTANTS
    protected static  final Map<String, Integer> VERSION_HIERARCHY;
    public static final String BUILD_DATE_TIME;
    public static final String DICTIONARY_XID = "dictionary";
    public static final String PGVERSION_XID = "PolyGlotVer";
    public static final String DICTIONARY_SAVE_DATE = "DictSaveDate";
    public static final String MAIN_MENU_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/PolyGlotBG.png";
    public static final String POLYGLOT_FILE_SUFFIX = "pgd";
    public static final String VERSION_LOCATION = "/assets/org/DarisaDesigns/version";
    public static final String PGT_VERSION;
    public static final boolean IS_BETA;
    public static final String HELP_FILE_ARCHIVE_LOCATION = "/assets/org/DarisaDesigns/readme.zip";
    public static final String EXAMPLE_LANGUAGE_ARCHIVE_LOCATION = "/assets/org/DarisaDesigns/exlex.zip";
    public static final String HELP_FILE_NAME = "readme.html";
    public static final String SWADESH_LOCATION = "/assets/org/DarisaDesigns/swadesh/";
    public static final String BUILD_DATE_TIME_LOCATION = "/assets/org/DarisaDesigns/buildDate";
    public static final String[] SWADESH_LISTS = {"Original_Swadesh", "Modern_Swadesh"};
    public static final int WINDOWS_CLIPBOARD_DELAY = 15;
    public static final int DEFAULT_MS_BETWEEN_AUTO_SAVES = 300000; // 5 minutes in microsecnds
    public static final int MAX_TOOLTIP_LENGTH = 55;
    public static final int MAX_MS_MENU_STARTUP_WAIT = 5000; // 5 seconds in microseconds
    public static final String AUTO_SAVE_FILE_NAME = ".pgtAutoSave.bak";
    public static final String TROUBLE_TICKET_URL = "https://github.com/DraqueT/PolyGlot/issues/new";
    public static final String ERROR_LOG_SPEARATOR = "\n-=-=-=-=-=-=-\n";

    // properties on words
    public static final String LEXICON_XID = "lexicon";
    public static final String WORD_XID = "word";
    public static final String LOCALWORD_XID = "localWord";
    public static final String CONWORD_XID = "conWord";
    public static final String WORD_POS_ID_XID = "wordTypeId";
    public static final String WORD_ID_XID = "wordId";
    public static final String WORD_PROCOVERRIDE_XID = "wordProcOverride";
    public static final String WORD_DEF_XID = "definition";
    public static final String WORD_AUTODECLOVERRIDE_XID = "autoDeclOverride";
    public static final String WORD_PROC_XID = "pronunciation";
    public static final String WORD_RULEOVERRIDE_XID = "wordRuleOverride";
    public static final String WORD_CLASSCOLLECTION_XID = "wordClassCollection";
    public static final String WORD_CLASS_AND_VALUE_XID = "wordClassification";
    public static final String WORD_CLASS_TEXT_VAL_COLLECTION_XID = "wordClassTextValueCollection";
    public static final String WORD_CLASS_TEXT_VAL_XID = "wordClassTextValue";
    public static final String WORD_ETY_NOTES_XID = "wordEtymologyNotes";
    

    // properties for types/parts of speech
    public static final String POS_COLLECTION_XID = "partsOfSpeech";
    public static final String POS_XID = "class";
    public static final String POS_NAME_XID = "className";
    public static final String POS_ID_XID = "classId";
    public static final String POS_NOTES_XID = "classNotes";
    public static final String POS_PROC_MAN_XID = "pronunciationMandatoryClass";
    public static final String POS_DEF_MAN_XID = "definitionMandatoryClass";
    public static final String POS_PATTERN_XID = "classPattern";
    public static final String POS_GLOSS_XID = "classGloss";

    // language properties
    public static final String LANG_PROPERTIES_XID = "languageProperties";
    public static final String FONT_CON_XID = "fontCon";
    public static final String FONT_LOCAL_XID = "fontLocal";
    public static final String LANG_PROP_LANG_NAME_XID = "langName";
    public static final String LANG_PROP_FONT_SIZE_XID = "fontSize";
    public static final String LANG_PROP_FONT_STYLE_XID = "fontStyle";
    public static final String LANG_PROP_LOCAL_FONT_SIZE_XID = "localFontSize";
    public static final String LANG_PROP_ALPHA_ORDER_XID = "alphaOrder";
    public static final String LANG_PROP_TYPE_MAND_XID = "langPropTypeMandatory";
    public static final String LANG_PROP_LOCAL_MAND_XID = "langPropLocalMandatory";
    public static final String LANG_PROP_WORD_UNIQUE_XID = "langPropWordUniqueness";
    public static final String LANG_PROP_LOCAL_UNIQUE_XID = "langPropLocalUniqueness";
    public static final String LANG_PROP_IGNORE_CASE_XID = "langPropIgnoreCase";
    public static final String LANG_PROP_DISABLE_PROC_REGEX = "langPropDisableProcRegex";
    public static final String LANG_PROP_ENFORCE_RTL_XID = "langPropEnforceRTL";
    public static final String LANG_PROP_AUTH_COPYRIGHT_XID = "langPropAuthorCopyright";
    public static final String LANG_PROP_LOCAL_NAME_XID = "langPropLocalLangName";
    public static final String LANG_PROP_USE_LOCAL_LEX_XID = "langPropUseLocalLexicon";
    public static final String LANG_PROP_KERN_VAL_XID = "langPropKerningValue";
    public static final String LANG_PROP_OVERRIDE_REGEX_FONT_XID = "langPropOverrideRegexFont";
    public static final String LANG_PROP_USE_SIMPLIFIED_CONJ = "langPropUseSimplifiedConjugations";
    public static final String LANG_PROP_EXPANDED_LEX_LIST_DISP = "expandedLexListDisplay";
    public static final String LANG_PROP_ZOMPIST_CATEGORIES = "zompistCategories";
    public static final String LANG_PROP_ZOMPIST_ILLEGAL_CLUSTERS = "zompistIllegalClusters";
    public static final String LANG_PROP_ZOMPIST_REWRITE_RULES = "zompistRewriteRules";
    public static final String LANG_PROP_ZOMPIST_SYLLABLES = "zompistSyllables";

    // character replacement pair values
    public static final String LANG_PROP_CHAR_REP_CONTAINER_XID = "langPropCharRep";
    public static final String LANG_PROPCHAR_REP_NODE_XID = "langPropCharRepNode";
    public static final String LANG_PROP_CHAR_REP_CHAR_XID = "langPropCharRepCharacter";
    public static final String LANG_PROP_CHAR_REP_VAL_XID = "langPropCharRepValue";

    // declension properties
    public static final String DECLENSION_COLLECTION_XID = "declensionCollection";
    public static final String DECLENSION_XID = "declensionNode";
    public static final String DECLENSION_ID_XID = "declensionId";
    public static final String DECLENSION_TEXT_XID = "declensionText";
    public static final String DECLENSION_COMB_DIM_XID = "combinedDimId";
    public static final String DECLENSION_NOTES_XID = "declensionNotes";
    public static final String DECLENSION_IS_TEMPLATE_XID = "declensionTemplate";
    public static final String DECLENSION_RELATED_ID_XID = "declensionRelatedId";
    public static final String DECLENSION_IS_DIMENSIONLESS_XID = "declensionDimensionless";

    // dimensional declension properties
    public static final String DIMENSION_NODE_XID = "dimensionNode";
    public static final String DIMENSION_ID_XID = "dimensionId";
    public static final String DIMENSION_NAME_XID = "dimensionName";

    // pronunciation properties
    public static final String ETYMOLOGY_COLLECTION_XID = "etymologyCollection";
    public static final String PRO_GUIDE_XID = "proGuide";
    public static final String PRO_GUIDE_BASE_XID = "proGuideBase";
    public static final String PRO_GUIDE_PHON_XID = "proGuidePhon";
    public static final String PRO_GUIDE_RECURSIVE_XID = "proGuideRecurse";
    public static final String PRO_GUIDE_SYLLABLES_LIST = "proFGFuideSyllableList";
    public static final String PRO_GUIDE_SYLLABLE = "proGuideSyllable";
    public static final String PRO_GUIDE_COMPOSITION_SYLLABLE = "proGuideSyllableComposition";

    // romanization properties
    public static final String ROM_GUIDE_XID = "romGuide";
    public static final String ROM_GUIDE_ENABLED_XID = "romGuideEnabled";
    public static final String ROM_GUIDE_NODE_XID = "romGuideNode";
    public static final String ROM_GUIDE_BASE_XID = "romGuideBase";
    public static final String ROM_GUIDE_PHON_XID = "romGuidePhon";
    public static final String ROM_GUIDE_RECURSE_XID = "romGuideRecurse";

    // family properties
    public static final String FAM_NODE_XID = "thesNode";
    public static final String FAM_NOTES_XID = "thesNotes";
    public static final String FAM_NAME_XID = "thesName";
    public static final String FAM_WORD_XID = "thesWord";

    // autodeclansion generation properties
    public static final String DEC_GEN_RULE_XID = "decGenRule";
    public static final String DEC_GEN_RULE_TYPE_XID = "decGenRuleTypeId";
    public static final String DEC_GEN_RULE_COMB_XID = "decGenRuleComb";
    public static final String DEC_GEN_RULE_REGEX_XID = "decGenRuleRegex";
    public static final String DEC_GEN_RULE_NAME_XID = "decGenRuleName";
    public static final String DEC_GEN_RULE_INDEX_XID = "decGenRuleIndex";
    public static final String DEC_GEN_RULE_APPLY_TO_CLASSES_XID = "decGenRuleApplyToClasses";
    public static final String DEC_GEN_RULE_APPLY_TO_CLASS_VALUE_XID = "decGenRuleApplyToClassValue";

    // autodeclension transform properties
    public static final String DEC_GEN_TRANS_XID = "decGenTrans";
    public static final String DEC_GEN_TRANS_REGEX_XID = "decGenTransRegex";
    public static final String DEC_GEN_TRANS_REPLACE_XID = "decGenTransReplace";

    // constructed declension dimension properties
    public static final String DEC_COMBINED_FORM_SECTION_XID = "decCombinedFormSection";
    public static final String DEC_COMBINED_FORM_XID = "decCombinedForm";
    public static final String DEC_COMBINED_ID_XID = "decCombinedId";
    public static final String DEC_COMBINED_SURPRESS_XID = "decCombinedSurpress";

    // properties for logographs
    public static final String LOGO_ROOT_NOTE_XID = "logoRootNode";
    public static final String LOGOGRAPHS_COLLECTION_XID = "logoGraphsCollection";
    public static final String LOGO_STROKES_XID = "logoStrokes";
    public static final String LOGO_NOTES_XID = "logoNotes";
    public static final String LOGO_IS_RADICAL_XID = "logoIsRadical";
    public static final String LOGO_RADICAL_LIST_XID = "logoRadicalList";
    public static final String LOGO_READING_LIST_XID = "logoReading";
    public static final String LOGOGRAPH_VALUE_XID = "logoGraphValue";
    public static final String LOGOGRAPH_ID_XID = "logoGraphId";
    public static final String LOGOGRAPH_NODE_XID = "LogoGraphNode";
    public static final String LOGO_WORD_RELATION_XID = "LogoWordRelation";
    public static final String LOGO_RELATION_COLLECTION_XID = "LogoRelationsCollection";

    // properties for the grammar dictioary
    public static final String GRAMMAR_SECTION_XID = "grammarCollection";
    public static final String GRAMMAR_CHAPTER_NODE_XID = "grammarChapterNode";
    public static final String GRAMMAR_CHAPTER_NAME_XID = "grammarChapterName";
    public static final String GRAMMAR_SECTIONS_LIST_XID = "grammarSectionsList";
    public static final String GRAMMAR_SECTION_NODE_XID = "grammarSectionNode";
    public static final String GRAMMAR_SECTION_NAME_XID = "grammarSectionName";
    public static final String GRAMMAR_SECTION_RECORDING_XID = "grammarSectionRecordingXID";
    public static final String GRAMMAR_SECTION_TEXT_XID = "grammarSectionText";

    // properties for word classes
    public static final String CLASSES_NODE_XID = "wordGrammarClassCollection";
    public static final String CLASS_XID = "wordGrammarClassNode";
    public static final String CLASS_ID_XID = "wordGrammarClassID";
    public static final String CLASS_NAME_XID = "wordGrammarClassName";
    public static final String CLASS_APPLY_TYPES_XID = "wordGrammarApplyTypes";
    public static final String CLASS_IS_FREETEXT_XID = "wordGrammarIsFreeTextField";
    public static final String CLASS_IS_ASSOCIATIVE_XID = "wordGrammarIsAssociative";
    public static final String CLASS_VALUES_COLLECTION_XID = "wordGrammarClassValuesCollection";
    public static final String CLASS_VALUES_NODE_XID = "wordGrammarClassValueNode";
    public static final String CLASS_VALUE_NAME_XID = "wordGrammarClassValueName";
    public static final String CLASS_VALUE_ID_XID = "wordGrammarClassValueId";
    
    // properties for phrasebook
    public static final String PHRASEBOOK_XID = "phraseBookCollection";
    public static final String PHRASE_NODE_XID = "phraseNode";
    public static final String PHRASE_ID_XID = "phraseId";
    public static final String PHRASE_GLOSS_XID = "phraseGloss";
    public static final String PHRASE_CONPHRASE_XID = "phraseConPhrase";
    public static final String PHRASE_LOCALPHRASE_XID = "phraseLocalPhrase";
    public static final String PHRASE_PRONUNCIATION_XID = "phrasePronunciation";
    public static final String PHRASE_PRONUNCIATION_OVERRIDE_XID = "phrasePronunciationOverride";
    public static final String PHRASE_NOTES_XID = "phraseNotes";
    public static final String PHRASE_ORDER_XID = "phraseNotesOrder";

    // etymology constants
    public static final String ETY_COLLECTION_XID = "EtymologyCollection";
    public static final String ETY_INT_RELATION_NODE_XID = "EtymologyInternalRelation";
    public static final String ETY_INT_CHILD_XID = "EtymologyInternalChild";
    public static final String ETY_CHILD_EXTERNALS_XID = "EtymologyChildToExternalsNode";
    public static final String ETY_EXTERNAL_WORD_NODE_XID = "EtymologyExternalWordNode";
    public static final String ETY_EXTERNAL_WORD_VALUE_XID = "EtymologyExternalWordValue";
    public static final String ETY_EXTERNAL_WORD_ORIGIN_XID = "EtymologyExternalWordOrigin";
    public static final String ETY_EXTERNAL_WORD_DEFINITION_XID = "EtymologyExternalWordDefinition";

    // TO DO Node constants
    public static final String TODO_LOG_XID = "ToDoLog";
    public static final String TODO_NODE_XID = "ToDoNodeHead";
    public static final String TODO_NODE_DONE_XID = "ToDoNodeDone";
    public static final String TODO_NODE_LABEL_XID = "ToDoNodeLabel";

    // constants for PolyGlot options found in PolyGlot.ini
    public static final int OPTIONS_NUM_LAST_FILES = 5;
    public static final String OPTIONS_LAST_FILES = "LastFiles";
    public static final String OPTIONS_SCREEN_POS = "ScreenPositions";
    public static final String OPTIONS_SCREENS_SIZE = "ScreenSizes";
    public static final String OPTIONS_SCREENS_OPEN = "ScreensUp";
    public static final String OPTIONS_AUTO_RESIZE = "OptionsResize";
    public static final String OPTIONS_MENU_FONT_SIZE = "OptionsMenuFontSize";
    public static final String OPTIONS_NIGHT_MODE = "OptionsNightMode";
    public static final String OPTIONS_REVERSIONS_COUNT = "OptionsReversionCount";
    public static final String OPTIONS_TODO_DIV_LOCATION = "ToDoDividerLocation";
    public static final String OPTIONS_DIVIDER_POSITION = "OptionsDividerPosition";
    public static final String OPTIONS_MAXIMIZED = "OptionsMaximized";
    public static final String OPTIONS_MSBETWEENSAVES = "MsBetweenSaves";

    // Java 8 bridge constants
    public static final String JAVA8_JAVA_COMMAND = "java";
    public static final String JAVA8_JAR_ARG = "-jar";
    public static final String JAVA8_VERSION_ARG = "-version";
    public static final String JAVA9P_VERSION_ARG = "--version";
    public static final String JAVA8_BRIDGERESOURCE = "/assets/org/DarisaDesigns/java_8_bridge.zip";
    public static final String JAVA8_JAR = "PolyGlot_J8_Bridge.jar";
    public static final String JAVA8_JAR_FOLDER = "dist";
    public static final String JAVA8_PDFCOMMAND = "pdf-export";
    public static final String JAVA8_EXCELTOCVSCOMMAND = "excel-to-cvs";
    public static final String JAVA8_EXPORTTOEXCELCOMMAND = "export-to-excel";
    public static final int CHAP_ORTHOGRAPHY = 0;
    public static final int CHAP_GLOSSKEY = 1;
    public static final int CHAP_CONTOLOCAL = 2;
    public static final int CHAP_LOCALTOCON = 3;
    public static final int CHAP_PHRASEBOOK = 4;
    public static final int CHAP_GRAMMAR = 5;

    // string constants
    public static final String LANG_FILE_NAME = "PGDictionary.xml";
    public static final String CON_FONT_FILE_NAME = "conLangFont";
    public static final String LOCAL_FONT_FILE_NAME = "localLangFont";
    public static final String LCD_FONT_LOCATION = "/assets/org/DarisaDesigns/FontAssets/lcdFont.ttf";
    public static final String UNICODE_FONT_LOCATION = "/assets/org/DarisaDesigns/FontAssets/CharisSIL-Regular.ttf";
    public static final String UNICODE_FONT_BOLD_LOCATION = "/assets/org/DarisaDesigns/FontAssets/CharisSIL-Bold.ttf";
    public static final String UNICODE_FONT_ITALIC_LOCATION = "/assets/org/DarisaDesigns/FontAssets/CharisSIL-Italic.ttf";
    public static final String UNICODE_FONT_BOLD_ITALIC_LOCATION = "/assets/org/DarisaDesigns/FontAssets/CharisSIL-BoldItalic.ttf";
    public static final String UNICODE_FONT_FAMILY_NAME = "Charis SIL";
    public static final String BUTTON_FONT_LOCATION = "/assets/org/DarisaDesigns/FontAssets/buttonFont.ttf";
    public static final String LOGOGRAPH_SAVE_PATH = "logoGraphs/";
    public static final String IMAGES_SAVE_PATH = "images/";
    public static final String GRAMMAR_SOUNDS_SAVE_PATH = "grammarSounds/";
    public static final String REVERSION_SAVE_PATH = "reversion/";
    public static final String REVERSION_BASE_FILE_NAME = "reversionXMLFile";
    public static final String ERROR_LOG_FILE = "PolyGlot_error.log";
    public static final String EMPTY_FILE = "<EMPTY>";
    public static final String TEMP_FILE = "xxTEMPPGTFILExx";
    public static final String CONLANG_FONT = "PolyGlotConlangGrammarFont";
    public static final String POLYGLOT_INI = "PolyGlot.ini";
    public static final String POLYGLOT_WORKINGDIRECTORY = "PolyGlot";
    public static final String EMPTY_LOGO_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/EmptyImage.png";
    public static final String TREE_NODE_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/treeNode.png";
    public static final String NOT_FOUND_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/not-found.png";
    public static final String POLYGLOT_ABOUT = "/assets/org/DarisaDesigns/ImageAssets/PolyGlot_About.png";
    public static final String POLYGLOT_EASTER = "/assets/org/DarisaDesigns/ImageAssets/n0rara_draque.png";
    public static final String TESTRESOURCES = "src/test/java/TestResources/";

    public static final String PLAY_BUTTON_UP = "/assets/org/DarisaDesigns/ImageAssets/play_OFF_BIG.png";
    public static final String PLAY_BUTTON_DOWN = "/assets/org/DarisaDesigns/ImageAssets/play_ON_BIG.png";
    public static final String RECORD_BUTTON_UP = "/assets/org/DarisaDesigns/ImageAssets/recording_OFF_BIG.png";
    public static final String RECORD_BUTTON_DOWN = "/assets/org/DarisaDesigns/ImageAssets/recording_ON_BIG.png";
    public static final String TRASH_BUTTON_UP = "/assets/org/DarisaDesigns/ImageAssets/trashcan_color.png";
    public static final String TRASH_BUTTON_DOWN = "/assets/org/DarisaDesigns/ImageAssets/trashcan_clicked.png";
    public static final String TRASH_BUTTON_DISABLED = "/assets/org/DarisaDesigns/ImageAssets/trashcan_bw.png";
    public static final String ADD_BUTTON = "/assets/org/DarisaDesigns/ImageAssets/add_button.png";
    public static final String DELETE_BUTTON = "/assets/org/DarisaDesigns/ImageAssets/delete_button.png";
    public static final String ADD_BUTTON_PRESSED = "/assets/org/DarisaDesigns/ImageAssets/add_button_pressed.png";
    public static final String DELETE_BUTTON_PRESSED = "/assets/org/DarisaDesigns/ImageAssets/delete_button_pressed.png";

    // Sound Recorder Constants
    public static final String IPA_VOWEL_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/IPA_Vowels.png";
    public static final String IPA_PULMONIC_CONSONANT_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/IPA_Pulmonic_Consonants.png";
    public static final String IPA_NON_PULMONIC_CONSONANTS_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/IPA_NonPulmonicConsonants.png";
    public static final String IPA_OTHER_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/IPA_Other.png";
    public static final String UCLA_WAV_LOCATION = "ucla_wavs/";
    public static final String WIKI_WAV_LOCATION = "wiki_wavs/";
    public static final String WAV_SUFFIX = ".wav";
    public static final String IPA_SOUNDS_LOCATION = "/assets/org/DarisaDesigns/SoundAssets/";
    
    public static final String RTL_CHARACTER = "\u202e";
    public static final String LTR_MARKER = "\u202c";
    public static final String IMAGE_ID_ATTRIBUTE = "imageIDAttribute";
    public static final String TRUE = "T";
    public static final String FALSE = "F";
    public static final String DISPLAY_NAME = "PolyGlot";

    // web locations
    public static final String UPDATE_FILE_URL = "https://draquet.github.io/PolyGlot/update.xml";
    public static final String HELP_FILE_URL = "http://draquet.github.io/PolyGlot/readme.html";

    // numeric constants...
    public static final Double DEFAULT_FONT_SIZE = 12.0;
    public static final int MAX_PROC_RECURSE = 100;
    public static final int DEFAULT_MAX_ROLLBACK_NUM = 10;
    public static final int MAX_FILE_PATH_LENGTH = 1000;
    public static final int MAX_LOG_CHARACTERS = 25000;
    public static final int PLABEL_MIN_FONT_SIZE = 3;
    public static final int PLABEL_MAX_FONT_SIZE = 240;

    // visual style constants
    public static final int CHECKBOX_ROUNDING = 3;

    // UI Elements to set on OSX (copy/paste/cut)
    public static final String[] INPUT_MAPS = {
        "Button.focusInputMap",
        "CheckBox.focusInputMap",
        "ComboBox.ancestorInputMap",
        "EditorPane.focusInputMap",
        "FileChooser.ancestorInputMap",
        "FormattedTextField.focusInputMap",
        "List.focusInputMap",
        "PasswordField.focusInputMap",
        "RadioButton.focusInputMap",
        "RootPane.ancestorInputMap",
        "ScrollBar.ancestorInputMap",
        "ScrollPane.ancestorInputMap",
        "Slider.focusInputMap",
        "Spinner.ancestorInputMap",
        "SplitPane.ancestorInputMap",
        "TabbedPane.ancestorInputMap",
        "TabbedPane.focusInputMap",
        "Table.ancestorInputMap",
        "TableHeader.ancestorInputMap",
        "ToolBar.ancestorInputMap",
        "Tree.ancestorInputMap",
        "TextArea.focusInputMap",
        "TextField.focusInputMap",
        "TextPane.focusInputMap",
        "ToggleButton.focusInputMap",
        "Tree.focusInputMap"
    };

    public static final boolean IS_OSX;
    public static final boolean IS_WINDOWS;
    public static final boolean IS_LINUX;

    // one time set for code driven static values
    static {
        IS_OSX = isOSX();
        IS_WINDOWS = isWindows();
        IS_LINUX = isLinux();
        
        // sets version number and beta status
        String version = getVersion();
        if (version.contains("B")) {
            IS_BETA = true;
            PGT_VERSION = version.replace("B", "");
        } else {
            IS_BETA = false;
            PGT_VERSION = version;
        }
        
        // populate version hierarchy
        VERSION_HIERARCHY = new HashMap<>();
        VERSION_HIERARCHY.put("0", 0);
        VERSION_HIERARCHY.put("0.5", 1);
        VERSION_HIERARCHY.put("0.5.1", 2);
        VERSION_HIERARCHY.put("0.6", 3);
        VERSION_HIERARCHY.put("0.6.1", 4);
        VERSION_HIERARCHY.put("0.6.5", 5);
        VERSION_HIERARCHY.put("0.7", 6);
        VERSION_HIERARCHY.put("0.7.5", 7);
        VERSION_HIERARCHY.put("0.7.6", 8);
        VERSION_HIERARCHY.put("0.7.6.1", 9);
        VERSION_HIERARCHY.put("0.8", 10);
        VERSION_HIERARCHY.put("0.8.1", 11);
        VERSION_HIERARCHY.put("0.8.1.1", 12);
        VERSION_HIERARCHY.put("0.8.1.2", 13);
        VERSION_HIERARCHY.put("0.8.5", 14);
        VERSION_HIERARCHY.put("0.9", 15);
        VERSION_HIERARCHY.put("0.9.1", 16);
        VERSION_HIERARCHY.put("0.9.2", 17);
        VERSION_HIERARCHY.put("0.9.9", 18);
        VERSION_HIERARCHY.put("0.9.9.1", 19);
        VERSION_HIERARCHY.put("1.0", 20);
        VERSION_HIERARCHY.put("1.0.1", 21);
        VERSION_HIERARCHY.put("1.1", 22);
        VERSION_HIERARCHY.put("1.2", 23);
        VERSION_HIERARCHY.put("1.2.1", 24);
        VERSION_HIERARCHY.put("1.2.2", 25);
        VERSION_HIERARCHY.put("1.3", 26);
        VERSION_HIERARCHY.put("1.4", 27);
        VERSION_HIERARCHY.put("2.0", 28);
        VERSION_HIERARCHY.put("2.1", 29);
        VERSION_HIERARCHY.put("2.2", 30);
        VERSION_HIERARCHY.put("2.3", 31);
        VERSION_HIERARCHY.put("2.3.1", 32);
        VERSION_HIERARCHY.put("2.3.2", 33);
        VERSION_HIERARCHY.put("2.3.3", 34);
        VERSION_HIERARCHY.put("2.4", 35);
        VERSION_HIERARCHY.put("2.5", 36);
        VERSION_HIERARCHY.put("3.0", 37);
        VERSION_HIERARCHY.put("3.0.1", 38);
        VERSION_HIERARCHY.put("3.1", 39);
        VERSION_HIERARCHY.put("3.1.1", 40);
        VERSION_HIERARCHY.put("3.1.2", 41);
        VERSION_HIERARCHY.put("3.1.3", 42);
        VERSION_HIERARCHY.put("3.2", 43);
        VERSION_HIERARCHY.put("3.3", 44);
        VERSION_HIERARCHY.put("3.3.1", 45);
        VERSION_HIERARCHY.put("3.3.1B", 46);
        VERSION_HIERARCHY.put("3.3.5", 47);
        VERSION_HIERARCHY.put("3.5", 48);
        
        BUILD_DATE_TIME = getBuildDate();
    }
    
    private static String getVersion() {
        URL version = PGTUtil.class.getResource(VERSION_LOCATION);
        
        if (version != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(version.openStream(), StandardCharsets.UTF_8))) {
                return br.readLine();
            } catch (IOException e) {
                // inappropriate to log here
                // DesktopIOHandler.getInstance().writeErrorLog(e, "Unable to fetch version at startup");
            }
        }

        return "?.?";
    }
    
    private static String getBuildDate() {
        URL buildDate = PGTUtil.class.getResource(BUILD_DATE_TIME_LOCATION);
        
        if (buildDate != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(buildDate.openStream(), StandardCharsets.UTF_8))) {
                return br.readLine();
            } catch (IOException e) {
                // Inappropriate to throw exception here
                // DesktopIOHandler.getInstance().writeErrorLog(e, "Unable to fetch build date at startup");
            }
        }

        return "BUILD DATE FILE NOT PRESENT";
    }
    
    // ENVIRONMENT VARIABLES
    private static File errorDirectory = null;
    private static boolean forceSuppressDialogs = false;
    private static boolean uiTestingMode = false;
    
    // OS CONSTANTS
    public static String OSX_FINDER_INFO_VALUE_DIC_FILES = "574443444D535350000000000000000000000000000000000000000000000000";
    public static String OSX_FINDER_METADATA_NAME = "com.apple.FinderInfo";

    /**
     * Strips string of RTL and LTR markers
     *
     * @param strip string to strip
     * @return stripped string
     */
    public static String stripRTL(String strip) {
        return strip.replace(RTL_CHARACTER, "").replace(LTR_MARKER, "");
    }

    /**
     * Tests and returns true if running OSX
     *
     * @return
     */
    private static boolean isOSX() {
        return System.getProperty("os.name").startsWith("Mac");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Win");
    }
    
    private static boolean isLinux() {
        return System.getProperty("os.name").contains("Linux");
    }

    /**
     * Default directory based on OS value for user dir
     *
     * @return
     */
    public static File getDefaultDirectory() {
        String defaultDirectoryPath = System.getProperty("user.home") + File.separator + POLYGLOT_WORKINGDIRECTORY;
        
        // for my own sanity, this keeps test stuff from overwriting options and stuff
        if (PGTUtil.isInJUnitTest()) {
            defaultDirectoryPath += "_TEST";
        }
        
        File ret = new File(defaultDirectoryPath);

        if (!ret.exists()) {
            ret.mkdir();
        }

        return ret;
    }

    /**
     * Error directory defaults based on OS settings (overrides respected)
     *
     * @return
     */
    public static File getErrorDirectory() {
        if (errorDirectory == null || !errorDirectory.exists()) {
            errorDirectory = getDefaultDirectory();
        }

        return errorDirectory;
    }
    
     /**
     * Gets hierarchical status placement of PolyGlot's version
     * @param version
     * @return lower numbers are lower 
     */
    public static int getVersionHierarchy(String version) {
        int ret = 9999; // version not found is presumed to be higher than any given version in records
        
        if (VERSION_HIERARCHY.containsKey(version)) {
            ret = VERSION_HIERARCHY.get(version);
        }
        
        return ret;
    }
    
    public static void validateVersion() throws Exception {
        if (!VERSION_HIERARCHY.containsKey(PGT_VERSION)) {
            throw new Exception("ERROR: CURRENT VERSION NOT ACCOUNTED FOR IN VERSION HISTORY.");
        }
    }
    
    /**
     * Returns true if in JUnit test. USE SPARINGLY.
     * @return 
     */
    public static boolean isInJUnitTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement[] list = stackTrace;
        for (StackTraceElement element : list) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }           
        }
        return false;
    }

    public static boolean isForceSuppressDialogs() {
        return forceSuppressDialogs;
    }

    public static void setForceSuppressDialogs(boolean _forceSuppressDialogs) {
        forceSuppressDialogs = _forceSuppressDialogs;
    }
    
    /**
     * Tests whether a regex pattern contains a lookahead or lookbehind
     * @param regex
     * @return true if given regex contains a lookahead or lookbehind
     */
    public static boolean regexContainsLookaheadOrBehind(String regex) {
        return regex.contains("(?=")
                || regex.contains("(?<=")
                || regex.contains("(?!")
                || regex.contains("(?<!");
    }
    
    /**
     * Used for getting the display version (potentially different than the internal version due to betas, etc.)
     * @return 
     */
    public static String getDisplayVersion() {
        String ret = PGTUtil.PGT_VERSION;
        
        if (PGTUtil.IS_BETA) {
            ret = ret + " BETA (last release: " + ret + ")";
        }
        
        return ret;
    }
    
    /**
     * Cross platform helper that can be overridden with platform specific check
     * Should be overridden since this is a very basic helper
     * @param test
     * @return if the string is blank
     */
    public boolean isBlank(String test) {
        for(int i = 0; i < test.length(); i++) {
            if (test.charAt(i) != ' ' ||
                test.charAt(i) != '\t' ||
                test.charAt(i) != '\n' ||
                test.charAt(i) != '\r')
                return false;
        }
        return true;
    }
    
    /**
     * For development purposes only
     */
    public static void enterUITestingMode() {
        uiTestingMode = true;
    }
    
    /**
     * returns true if currently in UI testing mode
     * @return 
     */
    public static boolean isUITestingMode() {
        return uiTestingMode;
    }

    protected PGTUtil() {}
}
