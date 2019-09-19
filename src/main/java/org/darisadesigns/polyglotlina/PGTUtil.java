/*
 * Copyright (c) 2014-2019, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina;

import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.ImageIcon;
import org.darisadesigns.polyglotlina.Screens.ScrFamilies;
import org.darisadesigns.polyglotlina.Screens.ScrGrammarGuide;
import org.darisadesigns.polyglotlina.Screens.ScrIPARefChart;
import org.darisadesigns.polyglotlina.Screens.ScrLexicon;
import org.darisadesigns.polyglotlina.Screens.ScrLogoDetails;
import org.darisadesigns.polyglotlina.Screens.ScrQuizGenDialog;

/**
 * This contains various constant vales in PolyGlot
 *
 * @author draque
 */
public class PGTUtil {

    private static File java8BridgeLocation = null;
    private static File errorDirectory;
    public static final String DICTIONARY_XID = "dictionary";
    public static final String PGVERSION_XID = "PolyGlotVer";
    public static final String DICTIONARY_SAVE_DATE = "DictSaveDate";

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
    public static final String WORD_RULEORVERRIDE_XID = "wordRuleOverride";
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

    // character replacement pair values
    public static final String langPropCharRepContainerXID = "langPropCharRep";
    public static final String langPropCharRepNodeXID = "langPropCharRepNode";
    public static final String langPropCharRepCharacterXID = "langPropCharRepCharacter";
    public static final String langPropCharRepValueXID = "langPropCharRepValue";

    // declension properties
    public static final String declensionCollectionXID = "declensionCollection";
    public static final String declensionXID = "declensionNode";
    public static final String declensionIdXID = "declensionId";
    public static final String declensionTextXID = "declensionText";
    public static final String declensionComDimIdXID = "combinedDimId";
    public static final String declensionNotesXID = "declensionNotes";
    public static final String declensionIsTemplateXID = "declensionTemplate";
    public static final String declensionRelatedIdXID = "declensionRelatedId";
    public static final String declensionIsDimensionless = "declensionDimensionless";

    // dimensional declension properties
    public static final String dimensionNodeXID = "dimensionNode";
    public static final String dimensionIdXID = "dimensionId";
    public static final String dimensionNameXID = "dimensionName";

    // pronunciation properties
    public static final String etymologyCollectionXID = "etymologyCollection";
    public static final String proGuideXID = "proGuide";
    public static final String proGuideBaseXID = "proGuideBase";
    public static final String proGuidePhonXID = "proGuidePhon";
    public static final String proGuideRecurseXID = "proGuideRecurse";

    // romanization properties
    public static final String romGuideXID = "romGuide";
    public static final String romGuideEnabledXID = "romGuideEnabled";
    public static final String romGuideNodeXID = "romGuideNode";
    public static final String romGuideBaseXID = "romGuideBase";
    public static final String romGuidePhonXID = "romGuidePhon";
    public static final String romGuideRecurseXID = "romGuideRecurse";

    // family properties
    public static final String famNodeXID = "thesNode";
    public static final String famNotesXID = "thesNotes";
    public static final String famNameXID = "thesName";
    public static final String famWordXID = "thesWord";

    // autodeclansion generation properties
    public static final String decGenRuleXID = "decGenRule";
    public static final String decGenRuleTypeXID = "decGenRuleTypeId";
    public static final String decGenRuleCombXID = "decGenRuleComb";
    public static final String decGenRuleRegexXID = "decGenRuleRegex";
    public static final String decGenRuleNameXID = "decGenRuleName";
    public static final String decGenRuleIndexXID = "decGenRuleIndex";
    public static final String decGenRuleApplyToClasses = "decGenRuleApplyToClasses";
    public static final String decGenRuleApplyToClassValue = "decGenRuleApplyToClassValue";

    // autodeclension transform properties
    public static final String decGenTransXID = "decGenTrans";
    public static final String decGenTransRegexXID = "decGenTransRegex";
    public static final String decGenTransReplaceXID = "decGenTransReplace";

    // constructed declension dimension properties
    public static final String decCombinedFormSectionXID = "decCombinedFormSection";
    public static final String decCombinedFormXID = "decCombinedForm";
    public static final String decCombinedIdXID = "decCombinedId";
    public static final String decCombinedSurpressXID = "decCombinedSurpress";

    // properties for logographs
    public static final String logoRootNoteXID = "logoRootNode";
    public static final String logoGraphsCollectionXID = "logoGraphsCollection";
    public static final String logoStrokesXID = "logoStrokes";
    public static final String logoNotesXID = "logoNotes";
    public static final String logoIsRadicalXID = "logoIsRadical";
    public static final String logoRadicalListXID = "logoRadicalList";
    public static final String logoReadingXID = "logoReading";
    public static final String logoGraphValueXID = "logoGraphValue";
    public static final String logoGraphIdXID = "logoGraphId";
    public static final String logoGraphNodeXID = "LogoGraphNode";
    public static final String logoWordRelationXID = "LogoWordRelation";
    public static final String logoRelationsCollectionXID = "LogoRelationsCollection";

    // properties for the grammar dictioary
    public static final String grammarSectionXID = "grammarCollection";
    public static final String grammarChapterNodeXID = "grammarChapterNode";
    public static final String grammarChapterNameXID = "grammarChapterName";
    public static final String grammarSectionsListXID = "grammarSectionsList";
    public static final String grammarSectionNodeXID = "grammarSectionNode";
    public static final String grammarSectionNameXID = "grammarSectionName";
    public static final String grammarSectionRecordingXID = "grammarSectionRecordingXID";
    public static final String grammarSectionTextXID = "grammarSectionText";

    // properties for word classes
    public static final String ClassesNodeXID = "wordGrammarClassCollection";
    public static final String ClassXID = "wordGrammarClassNode";
    public static final String ClassIdXID = "wordGrammarClassID";
    public static final String ClassNameXID = "wordGrammarClassName";
    public static final String ClassApplyTypesXID = "wordGrammarApplyTypes";
    public static final String ClassIsFreetextXID = "wordGrammarIsFreeTextField";
    public static final String ClassValuesCollectionXID = "wordGrammarClassValuesCollection";
    public static final String ClassValueNodeXID = "wordGrammarClassValueNode";
    public static final String ClassValueNameXID = "wordGrammarClassValueName";
    public static final String ClassValueIdXID = "wordGrammarClassValueId";

    // etymology constants
    public static final String EtyCollectionXID = "EtymologyCollection";
    public static final String EtyIntRelationNodeXID = "EtymologyInternalRelation";
    public static final String EtyIntChildXID = "EtymologyInternalChild";
    public static final String EtyChildExternalsXID = "EtymologyChildToExternalsNode";
    public static final String EtyExternalWordNodeXID = "EtymologyExternalWordNode";
    public static final String EtyExternalWordValueXID = "EtymologyExternalWordValue";
    public static final String EtyExternalWordOriginXID = "EtymologyExternalWordOrigin";
    public static final String EtyExternalWordDefinitionXID = "EtymologyExternalWordDefinition";

    // TODO Node constants
    public static final String ToDoLogXID = "ToDoLog";
    public static final String ToDoNodeXID = "ToDoNodeHead";
    public static final String ToDoNodeDoneXID = "ToDoNodeDone";
    public static final String ToDoNodeLabelXID = "ToDoNodeLabel";
    public static final String ToDoNodeColorXID = "ToDoNodeColor";
    public static final String ToDoRoot = "ToDoRoot";

    // constants for PolyGlot options found in PolyGlot.ini
    public static final int optionsNumLastFiles = 5;
    public static final String optionsLastFiles = "LastFiles";
    public static final String optionsScreenPos = "ScreenPositions";
    public static final String optionsScreensSize = "ScreenSizes";
    public static final String optionsScreensOpen = "ScreensUp";
    public static final String optionsAutoResize = "OptionsResize";
    public static final String optionsMenuFontSize = "OptionsMenuFontSize";
    public static final String optionsNightMode = "OptionsNightMode";
    public static final String optionsReversionsCount = "OptionsReversionCount";
    public static final String optionsToDoDividerLocation = "ToDoDividerLocation";

    // Java 8 bridge constants
    public static final String JAVA8_JAVA_COMMAND = "java";
    public static final String JAVA8_JAR_ARG = "-jar";
    public static final String JAVA8_VERSION_ARG = "--version";
    public static final String JAVA8_BRIDGERESOURCE = "/assets/org/DarisaDesigns/java_8_bridge.zip";
    public static final String JAVA8_JAR = "PolyGlot_J8_Bridge.jar";
    public static final String JAVA8_JAR_FOLDER = "dist";
    public static final String JAVA8_PDFCOMMAND = "pdf-export";
    public static final String JAVA8_EXCELTOCVSCOMMAND = "excel-to-cvs";
    public static final String JAVA8_EXPORTTOEXCELCOMMAND = "export-to-excel";

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
    public static final String POLYGLOT_FONT = "PolyGlot";
    public static final String CONLANG_FONT = "PolyGlotConlangGrammarFont";
    public static final String POLYGLOT_INI = "PolyGlot.ini";
    public static final String POLYGLOT_WORKINGDIRECTORY = "PolyGlot";
    public static final String EMPTY_LOGO_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/EmptyImage.png";
    public static final String TREE_NODE_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/treeNode.png";
    public static final String NOT_FOUND_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/not-found.png";
    public static final String POLYGLOT_ABOUT = "/assets/org/DarisaDesigns/ImageAssets/PolyGlot_About.png";
    public static final String POLYGLOT_EASTER = "/assets/org/DarisaDesigns/ImageAssets/n0rara_draque.png";
    public static final String TESTRESOURCES = "src/test/java/TestResources/";

    public static final String playButtonUp = "/assets/org/DarisaDesigns/ImageAssets/play_OFF_BIG.png";
    public static final String playButtonDown = "/assets/org/DarisaDesigns/ImageAssets/play_ON_BIG.png";
    public static final String recordButtonUp = "/assets/org/DarisaDesigns/ImageAssets/recording_OFF_BIG.png";
    public static final String recordButtonDown = "/assets/org/DarisaDesigns/ImageAssets/recording_ON_BIG.png";
    public static final String addButton = "/assets/org/DarisaDesigns/ImageAssets/add_button.png";
    public static final String deleteButton = "/assets/org/DarisaDesigns/ImageAssets/delete_button.png";
    public static final String addButtonPressed = "/assets/org/DarisaDesigns/ImageAssets/add_button_pressed.png";
    public static final String deleteButtonPressed = "/assets/org/DarisaDesigns/ImageAssets/delete_button_pressed.png";

    // Sound Recorder Constants
    public static final String ipa_vowels = "/assets/org/DarisaDesigns/ImageAssets/IPA_Vowels.png";
    public static final String pulmonic_consonants = "/assets/org/DarisaDesigns/ImageAssets/IPA_Pulmonic_Consonants.png";
    public static final String non_pulmonic_consonants = "/assets/org/DarisaDesigns/ImageAssets/IPA_NonPulmonicConsonants.png";
    public static final String ipa_other = "/assets/org/DarisaDesigns/ImageAssets/IPA_Other.png";
    public static final String mainMenuBG = "/assets/org/DarisaDesigns/ImageAssets/PolyGlotBG.png";
    public static final String ucla_location = "ucla_wavs/";
    public static final String wiki_location = "wiki_wavs/";
    public static final String wavSuffix = ".wav";

    public static final String ipaSoundsLocation = "/assets/org/DarisaDesigns/SoundAssets/";
    public static final String RTLMarker = "\u202e";
    public static final String LTRMarker = "\u202c";
    public static final String ImageIdAttribute = "imageIDAttribute";
    public static final String True = "T";
    public static final String False = "F";
    public static final String displayName = "PolyGlot";

    // web locations
    public static final String HOMEPAGE_URL = "http://draquet.github.io/PolyGlot/";
    public static final String UPDATE_FILE_URL = "https://drive.google.com/uc?export=download&id=0B2RMQ7sRXResN3VwLTAwTFE0ZlE";

    // screen names when they're required as constants...
    public static final String scrNameLexicon;
    public static final String scrNameGrammar;
    public static final String scrNameLogo;
    public static final String scrNameFam;
    public static final String scrIPARefChart;
    public static final String scrQuizGenDialog;

    // numeric constants...
    public static final Integer numMenuFlashes = 4;
    public static final Integer menuFlashSleep = 200;
    public static final Double defaultFontSize = 12.0;
    public static final int maxProcRecursion = 100;
    public static final int defaultMaxRollbackVersions = 10;
    public static final int maxFilePathLength = 1000;
    public static final int maxLogCharacters = 25000;

    // color constants
    public static final Color colorDisabledBG;
    public static final Color colorEnabledBG;
    public static final Color colorSelectedBG;
    public static final Color colorDisabledForeground;
    public static final Color colorMouseoverBorder;
    public static final Color colorText;
    public static final Color colorDefaultText;
    public static final Color colorDefaultTextNight;
    public static final Color colorTextBG;
    public static final Color colorTextNight;
    public static final Color colorTextBGNight;
    public static final Color colorTextDisabled;
    public static final Color colorTextDisabledBG;
    public static final Color colorTextDisabledNight;
    public static final Color colorTextDisabledBGNight;
    public static final Color colorCheckboxSelected;
    public static final Color colorCheckboxBackground;
    public static final Color colorCheckboxOutline;
    public static final Color colorCheckboxHover;
    public static final Color colorCheckboxClicked;
    public static final Color colorCheckBoxFieldBack;
    public static final Color colorCheckboxSelectedNight;
    public static final Color colorCheckboxBackgroundNight;
    public static final Color colorCheckboxOutlineNight;
    public static final Color colorCheckboxHoverNight;
    public static final Color colorCheckboxClickedNight;
    public static final Color colorCheckBoxFieldBackNight;
    public static final Color colorCheckboxSelectedDisabled;
    public static final Color colorCheckboxBackgroundDisabled;
    public static final Color colorCheckboxOutlineDisabled;
    public static final Color colorCheckboxHoverDisabled;
    public static final Color colorCheckboxClickedDisabled;
    public static final Color colorCheckBoxFieldBackDisabled;

    // visual style constants
    public static final int CHECKBOX_ROUNDING = 3;

    // UI Elements to set on OSX (copy/paste/cut)
    public static final String[] INPUT_MAPS = {"Button.focusInputMap",
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
        "Tree.focusInputMap"};

    // images and icons that only need to be loaded once
    public static final ImageIcon ADD_BUTTON_ICON;
    public static final ImageIcon DEL_BUTTON_ICON;
    public static final ImageIcon ADD_BUTTON_ICON_PRESSED;
    public static final ImageIcon DEL_BUTTON_ICON_PRESSED;
    public static final ImageIcon POLYGLOT_ICON;

    // Fonts stored here to cache values single time
    public static final Font MENU_FONT;

    public static final boolean IS_OSX;
    public static final boolean IS_WINDOWS;

    // one time set for code driven static values
    static {
        colorDisabledBG = Color.decode("#b0b0b0");
        colorEnabledBG = Color.decode("#66b2ff");
        colorSelectedBG = Color.decode("#7979ef");
        colorDisabledForeground = Color.decode("#808080");
        colorMouseoverBorder = Color.decode("#909090");
        colorText = Color.decode("#000000");
        colorTextBG = Color.decode("#ffffff");
        colorTextNight = Color.decode("#ffffff");
        colorTextBGNight = Color.decode("#000000");
        colorDefaultText = Color.lightGray;
        colorDefaultTextNight = Color.darkGray;
        colorTextDisabled = Color.lightGray;
        colorTextDisabledBG = Color.darkGray;
        colorTextDisabledNight = Color.lightGray;
        colorTextDisabledBGNight = Color.darkGray;
        colorCheckboxSelected = Color.black;
        colorCheckboxBackground = Color.white;
        colorCheckboxOutline = Color.black;
        colorCheckboxHover = Color.black;
        colorCheckboxClicked = Color.lightGray;
        colorCheckBoxFieldBack = Color.white;
        colorCheckboxSelectedNight = Color.gray;
        colorCheckboxBackgroundNight = Color.black;
        colorCheckboxOutlineNight = Color.darkGray;
        colorCheckboxHoverNight = Color.lightGray;
        colorCheckboxClickedNight = Color.white;
        colorCheckBoxFieldBackNight = Color.black;
        colorCheckboxSelectedDisabled = Color.gray;
        colorCheckboxBackgroundDisabled = Color.lightGray;
        colorCheckboxOutlineDisabled = Color.gray;
        colorCheckboxHoverDisabled = Color.darkGray;
        colorCheckboxClickedDisabled = Color.darkGray;
        colorCheckBoxFieldBackDisabled = Color.gray;

        // loads default font on system error (never came up, but for completeness...)
        Font tmpFont;
        try {
            tmpFont = PFontHandler.getMenuFont();
        } catch (IOException e) {
            InfoBox.error("PolyGlot Load Error", "Unable to load default button font.", null);
            IOHandler.writeErrorLog(e, "Initilization error (PGTUtil)");
            tmpFont = javax.swing.UIManager.getDefaults().getFont("Label.font");
        }
        MENU_FONT = tmpFont;

        scrNameLexicon = ScrLexicon.class.getName();
        scrNameGrammar = ScrGrammarGuide.class.getName();
        scrNameLogo = ScrLogoDetails.class.getName();
        scrNameFam = ScrFamilies.class.getName();
        scrIPARefChart = ScrIPARefChart.class.getName();
        scrQuizGenDialog = ScrQuizGenDialog.class.getName();

        ADD_BUTTON_ICON = new ImageIcon(new ImageIcon(
                PGTUtil.class.getResource("/assets/org/DarisaDesigns/ImageAssets/add_button.png"))
                .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
        DEL_BUTTON_ICON = new ImageIcon(new ImageIcon(
                PGTUtil.class.getResource("/assets/org/DarisaDesigns/ImageAssets/delete_button.png"))
                .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
        ADD_BUTTON_ICON_PRESSED = new ImageIcon(new ImageIcon(
                PGTUtil.class.getResource("/assets/org/DarisaDesigns/ImageAssets/add_button_pressed.png"))
                .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
        DEL_BUTTON_ICON_PRESSED = new ImageIcon(new ImageIcon(
                PGTUtil.class.getResource("/assets/org/DarisaDesigns/ImageAssets/delete_button_pressed.png"))
                .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
        POLYGLOT_ICON = new ImageIcon(
                PGTUtil.class.getResource("/assets/org/DarisaDesigns/ImageAssets/PolyGlotIcon.png"));

        IS_OSX = isOSX();
        IS_WINDOWS = isWindows();
    }

    /**
     * This records the mode of a given PDialog or PFrame window. Defaults to
     * STANDARD
     */
    public enum WindowMode {
        STANDARD, SINGLEVALUE, SELECTLIST
    }

    /**
     * Encapsulates a string in RTL characters, setting back to LTR after
     *
     * @param encapsulate string to encapsulate
     * @return encapsulated string
     */
    public static String encapsulateRTL(String encapsulate) {
        return RTLMarker + encapsulate + LTRMarker;
    }

    /**
     * Strips string of RTL and LTR markers
     *
     * @param strip string to strip
     * @return stripped string
     */
    public static String stripRTL(String strip) {
        return strip.replace(RTLMarker, "").replace(LTRMarker, "");
    }

    /**
     * Adds attributes to fontmapping
     *
     * @param key Key value
     * @param value value-value
     * @param font font to add value to
     * @return newly derived font
     */
    @SuppressWarnings("unchecked") // No good way to do this in a type safe manner.
    public static Font addFontAttribute(Object key, Object value, Font font) {
        Map attributes = font.getAttributes();
        attributes.put(key, value);
        return font.deriveFont(attributes);
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

    /**
     * Checks that the position is in bounds for the screen and places it in
     * visible area if not
     *
     * @param w
     */
    public static void checkPositionInBounds(Window w) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point location = w.getLocationOnScreen();

        // if this would appear offscreen, simply place it in the center of the screen
        if (screenSize.getWidth() < location.x || screenSize.getHeight() < location.y) {
            w.setLocationRelativeTo(null);
        }
    }

    /**
     * Gets Java8 bridge class location. Caches value.
     *
     * @return
     * @throws java.io.IOException
     */
    public static File getJava8BridgeLocation() throws IOException {
        if (java8BridgeLocation == null || !java8BridgeLocation.exists()) {
            java8BridgeLocation = Java8Bridge.getNewJavaBridgeLocation();
        }

        return java8BridgeLocation;
    }

    /**
     * Default directory based on OS value for user dir
     *
     * @return
     */
    public static File getDefaultDirectory() {
        File ret = new File(System.getProperty("user.home") + File.separator + PGTUtil.POLYGLOT_WORKINGDIRECTORY);

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

    public static void setErrorDirectory(File _errorDirectory) {
        errorDirectory = _errorDirectory;
    }

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img) {
        BufferedImage ret = null;
        
        if (img instanceof BufferedImage) {
            ret = (BufferedImage) img;
        } else if (img != null) {
            // Create a buffered image with transparency
            ret = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

            // Draw the image on to the buffered image
            Graphics2D bGr = ret.createGraphics();
            bGr.drawImage(img, 0, 0, null);
            bGr.dispose();
        }

        return ret;
    }
}
