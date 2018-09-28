/*
 * Copyright (c) 2014-2018, Draque Thompson, draquemail@gmail.com
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

import PolyGlot.Screens.*;
import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * This contains various constant vales in PolyGlot
 * @author draque
 */
public class PGTUtil {
    public static final String dictionaryXID = "dictionary";
    public static final String pgVersionXID = "PolyGlotVer";
    public static final String dictionarySaveDate = "DictSaveDate";
    
    // properties on words
    public static final String lexiconXID = "lexicon";
    public static final String wordXID = "word";
    public static final String localWordXID = "localWord";
    public static final String conWordXID = "conWord";
    public static final String wordTypeXID = "wordType"; // LEGACY VALUE
    public static final String wordTypeIdXID = "wordTypeId";
    public static final String wordGenderXID = "wordGender"; // LEGACY VALUE
    public static final String wordIdXID = "wordId";
    public static final String wordPlurXID = "wordPlural"; // LEGACY VALUE
    public static final String wordProcOverrideXID = "wordProcOverride";
    public static final String wordDefXID = "definition";
    public static final String wordAutoDeclenOverrideXID = "autoDeclOverride";
    public static final String wordProcXID = "pronunciation";
    public static final String wordRuleOverrideXID = "wordRuleOverride";
    public static final String wordClassCollectionXID = "wordClassCollection";
    public static final String wordClassAndValueXID = "wordClassification";
    public static final String wordClassTextValueCollectionXID = "wordClassTextValueCollection";
    public static final String wordClassTextValueXID = "wordClassTextValue";
    public static final String wordEtymologyNotesXID = "wordEtymologyNotes";

    // properties for types/parts of speech
    public static final String typeCollectionXID = "partsOfSpeech";
    public static final String typeXID = "class";
    public static final String typeNameXID = "className";
    public static final String typeIdXID = "classId";
    public static final String typeNotesXID = "classNotes";
    public static final String typeGenderManXID = "genderMandatoryClass";
    public static final String typeProcManXID = "pronunciationMandatoryClass";
    public static final String typePlurManXID = "pluralityMandatoryClass";
    public static final String typeDefManXID = "definitionMandatoryClass";
    public static final String typePatternXID = "classPattern";
    public static final String typeGlossXID = "classGloss";

    // properties for genders
    public static final String genderXID = "gender";
    public static final String genderNameXID = "genderName";
    public static final String genderIdXID = "genderId";
    public static final String genderNotesXID = "genderNotes";

    // language properties
    public static final String langPropertiesXID = "languageProperties";
    public static final String fontConXID = "fontCon";
    public static final String fontLocalXID = "fontLocal";
    public static final String langPropLangNameXID = "langName";
    public static final String langPropFontSizeXID = "fontSize";
    public static final String langPropFontStyleXID = "fontStyle";
    public static final String langPropLocalFontSizeXID = "localFontSize";
    public static final String langPropAlphaOrderXID = "alphaOrder";
    public static final String langPropTypeMandatoryXID = "langPropTypeMandatory";
    public static final String langPropLocalMandatoryXID = "langPropLocalMandatory";
    public static final String langPropWordUniquenessXID = "langPropWordUniqueness";
    public static final String langPropLocalUniquenessXID = "langPropLocalUniqueness";
    public static final String langPropIgnoreCaseXID = "langPropIgnoreCase";
    public static final String langPropDisableProcRegexXID = "langPropDisableProcRegex";
    public static final String langPropEnforceRTLXID = "langPropEnforceRTL";
    public static final String langPropAuthCopyrightXID = "langPropAuthorCopyright";
    public static final String langPropLocalLangNameXID = "langPropLocalLangName";
    public static final String langPropKerningVal = "langPropKerningValue";
    
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
    public static final String declensionMandatoryXID = "declensionMandatory";
    public static final String declensionIsDimensionless = "declensionDimensionless";

    // dimensional declension properties
    public static final String dimensionNodeXID = "dimensionNode";
    public static final String dimensionIdXID = "dimensionId";
    public static final String dimensionMandXID = "dimensionMand";
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

    // string constants
    public static final String dictFileName = "PGDictionary.xml";
    public static final String conFontFileName = "conLangFont";
    public static final String localFontFileName = "localLangFont";
    public static final String logoGraphSavePath = "logoGraphs/";
    public static final String imagesSavePath = "images/";
    public static final String grammarSoundSavePath = "grammarSounds/";
    public static final String reversionSavePath = "reversion/";
    public static final String reversionBaseFileName = "reversionXMLFile";
    public static final String emptyFile = "<EMPTY>";
    public static final String tempFile = "xxTEMPPGTFILExx";
    public static final String polyGlotFont = "PolyGlot";
    public static final String conLangFont = "PolyGlotConlangGrammarFont";
    public static final String polyGlotIni = "PolyGlot.ini";
    public static final String emptyLogoImage = "/PolyGlot/ImageAssets/EmptyImage.png";
    public static final String IPAOtherSounds = "/PolyGlot/ImageAssets/IPA_Other.png";
    public static final String treeNodeImage = "/PolyGlot/ImageAssets/treeNode.png";
    public static final String jarArchiveName = "PolyGlot.jar";
    public static final String ipaSoundsLocation = "/PolyGlot/SoundAssets/";
    public static final String RTLMarker = "\u202e";
    public static final String LTRMarker = "\u202c";
    public static final String ImageIdAttribute = "imageIDAttribute";
    public static final String True = "T";
    public static final String False = "F";

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
    public static final int maxProcRecursion = 20;
    public static final int defaultMaxRollbackVersions = 10;
    public static final int maxFilePathLength = 1000;
    
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
    public static final int checkboxRounding = 3;

    // images and icons that only need to be loaded once
    public static final ImageIcon addButtonIcon;
    public static final ImageIcon delButtonIcon;
    public static final ImageIcon addButtonIconPressed;
    public static final ImageIcon delButtonIconPressed;
    
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
        
        /*
        Color selected;
        Color backGround;
        Color outLine;
        Color hover = Color.black;
        */
        
        scrNameLexicon = ScrLexicon.class.getName();
        scrNameGrammar = ScrGrammarGuide.class.getName();
        scrNameLogo = ScrLogoDetails.class.getName();
        scrNameFam = ScrFamilies.class.getName();
        scrIPARefChart = ScrIPARefChart.class.getName();
        scrQuizGenDialog = ScrQuizGenDialog.class.getName();
        
        addButtonIcon = new ImageIcon(new ImageIcon(
                    PGTUtil.class.getResource("/PolyGlot/ImageAssets/add_button.png"))
                    .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
        delButtonIcon = new ImageIcon(new ImageIcon(
                PGTUtil.class.getResource("/PolyGlot/ImageAssets/delete_button.png"))
                .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
        addButtonIconPressed = new ImageIcon(new ImageIcon(
                PGTUtil.class.getResource("/PolyGlot/ImageAssets/add_button_pressed.png"))
                .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
        delButtonIconPressed = new ImageIcon(new ImageIcon(
                PGTUtil.class.getResource("/PolyGlot/ImageAssets/delete_button_pressed.png"))
                .getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH));
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
     * @param encapsulate string to encapsulate
     * @return encapsulated string
     */
    public static String encapsulateRTL(String encapsulate) {
        return RTLMarker + encapsulate + LTRMarker;
    }

    /**
     * Strips string of RTL and LTR markers
     * @param strip string to strip
     * @return stripped string
     */
    public static String stripRTL(String strip) {
        return strip.replace(RTLMarker, "").replace(LTRMarker, "");
    }
}
