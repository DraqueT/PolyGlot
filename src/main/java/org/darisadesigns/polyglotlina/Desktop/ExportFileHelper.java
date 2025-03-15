/*
 * Copyright (c) 2014-2025, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.darisadesigns.polyglotlina.WebInterface;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.ManagersCollections.RomanizationManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import org.darisadesigns.polyglotlina.Nodes.WordClassValue;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class ExportFileHelper {
    public ExportFileHelper() {
        //
    }

    /**
     * Exports a dictionary to an excel file
     * 
     * @param filename File to export to
     * @param core dictionary core
     * @param separateDeclensions whether to separate parts of speech into
     * separate pages for declension values
     * @throws java.io.Exception
     */
    public static void exportExcelToDict(String filename, DictCore core,
        boolean separateDeclensions) throws Exception {

        Workbook workbook = null;
        Sheet sheet = null;
        if (filename.endsWith(".xlsx")) {
            workbook = new XSSFWorkbook();
        } else if (filename.endsWith(".xls")) {
            workbook = new HSSFWorkbook();
        } else {
            throw new Exception("Unsupported file format: " + filename);
        }

        CellStyle localStyle = workbook.createCellStyle();
        localStyle.setWrapText(true);
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        CellStyle boldHeader = workbook.createCellStyle();
        boldHeader.setWrapText(true);
        boldHeader.setFont(boldFont);
        Font conFont = workbook.createFont();
        conFont.setFontName(core.getPropertiesManager().getFontCon().getFontName());
        CellStyle conStyle = workbook.createCellStyle();
        conStyle.setWrapText(true);
        conStyle.setFont(conFont);

        // record words
        // separate words by part of spech if requested so that each POS can have distinct declension
        // columns
        if (separateDeclensions) {
            // create separate page for each part of speech
            for (TypeNode type : core.getTypes().getNodes()) {
                ConWord filter = new ConWord();
                filter.setWordTypeId(type.getId());

                try {
                    ConWord[] list = core.getWordCollection().filteredList(filter);
                    if (list.length == 0) {
                        continue; // no words, no sheet
                    }

                    sheet = workbook.createSheet(legalWorksheetName("Lex-" + type.getValue()));

                    Row row = sheet.createRow(0);
                    row.createCell(0).setCellValue(core.conLabel().toUpperCase() + " WORD");
                    row.createCell(1).setCellValue(core.localLabel().toUpperCase() + " WORD");
                    row.createCell(2).setCellValue("PoS");
                    row.createCell(3).setCellValue("PRONUNCIATION");
                    row.createCell(4).setCellValue("ROMANIZATION");
                    row.createCell(5).setCellValue("CLASS(ES)");

                    // create column for each declension
                    ConjugationPair[] conjList = core.getConjugationManager().getAllCombinedIds(type.getId());
                    int colNum = 5;
                    for (ConjugationPair curDec : conjList) {
                        colNum++;
                        row.createCell(colNum).setCellValue(curDec.label.toUpperCase());;
                    }

                    row.createCell(colNum + 1).setCellValue("DEFINITION");

                    int rowCount  =1;
                    for (ConWord word : list) {
                        row = sheet.createRow(rowCount);

                        Object[] wordArray = getWordForm(core, word, conjList).toArray();
                        for (int colCount = 0; colCount < wordArray.length; colCount++) {
                            Cell cell = row.createCell(colCount);
                            cell.setCellValue((String)wordArray[colCount]);

                            if (colCount == 0 || (colCount > 4 && colCount < wordArray.length - 1)) {
                                cell.setCellStyle(conStyle);
                            } else {
                                cell.setCellStyle(localStyle);
                            }
                        }
                        rowCount++;
                    }
                } catch (Exception e) {
                    System.out.println("Unable to export " + type.getValue() + " lexical values");
                }
            }
        } else {
            // old style of printing words
            sheet = workbook.createSheet("Lexicon");
            Map<Integer, ConjugationPair[]> typeDecMap = new HashMap<>();
            
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue(core.conLabel().toUpperCase() + " WORD");
            row.createCell(1).setCellValue(core.localLabel().toUpperCase() + " WORD");
            row.createCell(2).setCellValue("PoS");
            row.createCell(3).setCellValue("PRONUNCIATION");
            row.createCell(4).setCellValue("CLASS(ES)");
            row.createCell(5).setCellValue("DECLENSIONS");
            row.createCell(6).setCellValue("DEFINITIONS");

            int i = 0;
            for (ConWord word : core.getWordCollection().getWordNodes()) {
                i++;
                ConjugationPair[] decList;

                if (typeDecMap.containsKey(word.getWordTypeId())) {
                    decList = typeDecMap.get(word.getWordTypeId());
                } else {
                    decList = core.getConjugationManager().getAllCombinedIds(word.getWordTypeId());
                    typeDecMap.put(word.getWordTypeId(), decList);
                }

                Object[] wordArray = getWordFormOld(core, word, decList);
                row = sheet.createRow(i);
                for (Integer j = 0; j < wordArray.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue((String) wordArray[j]);

                    if (j == 0) {
                        cell.setCellStyle(conStyle);
                    } else {
                        cell.setCellStyle(localStyle);
                    }
                }
            }
        }
        
        // record types on sheet
        sheet = workbook.createSheet("Parts of Speech");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("PoS");
        row.createCell(1).setCellValue("NOTES");

        int i = 0;
        for (TypeNode curNode : core.getTypes().getNodes()) {
            i++;

            row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(curNode.getValue());
            cell = row.createCell(1);
            cell.setCellValue(WebInterface.getTextFromHtml(curNode.getNotes()));
            cell.setCellStyle(localStyle);
        }

        // record word classes on sheet
        WordClass[] classes = core.getWordClassCollection().getAllWordClasses();

        if (classes.length != 0) {
            sheet = workbook.createSheet("Lexical Classes");
            int propertyColumn = 0;
            for (WordClass curProp : classes) {
                // get row, if not exist, create
                row = sheet.getRow(0);
                if (row == null) {
                    row = sheet.createRow(0);
                }

                Cell cell = row.createCell(propertyColumn);
                cell.setCellValue(curProp.getValue());
                cell.setCellStyle(boldHeader);

                int rowIndex = 1;
                for (WordClassValue curVal : curProp.getValues()) {
                    row = sheet.getRow(rowIndex);
                    if (row == null) {
                        row = sheet.createRow(rowIndex);
                    }

                    cell = row.createCell(propertyColumn);
                    cell.setCellStyle(localStyle);
                    cell.setCellValue(curVal.getValue());

                    rowIndex++;
                }

                propertyColumn++;
            }
        }

        // record pronunciation on sheet
        sheet = workbook.createSheet("Pronunciations");

        row = sheet.createRow(0);
        row.createCell(0).setCellValue("CHARACTER(S)");
        row.createCell(1).setCellValue("PRONUNCIATION");

        i = 0;

        for (PronunciationNode curNode : core.getPronunciationMgr().getPronunciations()) {
            i++;
            row = sheet.createRow(i);

            Cell cell = row.createCell(0);
            cell.setCellStyle(conStyle);
            cell.setCellValue(curNode.getValue());

            cell = row.createCell(1);
            cell.setCellStyle(localStyle);
            cell.setCellValue(curNode.getPronunciation());
        }

        try (FileOutputStream out = new FileOutputStream(new File(filename))) {
            workbook.write(out);
        } catch (Exception e) {
            workbook.close();
            throw new Exception("Unable to write to file: " + filename);
        }

        workbook.close();
    }

    /**
     * Returns a legal worksheet name from the string handed in
     * Illegal characters removed, forced size of 31 characters.
     * @param startName name requested
     * @return filtered name
     */
    private static String legalWorksheetName(String startName) {
        // illegal characters: \ / * [ ] : ?
        String ret = startName.replace("/|\\*|\\[|\\]|:|\\?", "");
        if (ret.length() > 31) {
            // max worksheet name length = 31 chars (why so short?!)
            ret = ret.substring(0, 30);
        }
        return ret;
    }
    
    /**
     * Returns list of all legal wordforms this word can take.
     * Accounts for overridden forms and properly filters forms marked as disabled
     * @param core language
     * @param conWord
     * @param conjList
     * @return 
     */
    private static List<String> getWordForm(DictCore core, ConWord conWord, ConjugationPair[] conjList) {
        List<String> ret = new ArrayList<>();

        ret.add(conWord.getValue());
        ret.add(conWord.getLocalWord());
        ret.add(conWord.getWordTypeDisplay());
        try {
            ret.add(conWord.getPronunciation());
        } catch (Exception e) {
            ret.add("<ERROR>");
        }
        
        try {
            ret.add(core.getRomManager().getPronunciation(conWord.getValue()));
        } catch (Exception e) {
            ret.add("<ERROR>");
        }

        String classes = "";
        for (Entry<Integer, Integer> curEntry : conWord.getClassValues()) {
            if (classes.length() != 0) {
                classes += ", ";
            }
            try {
                WordClass prop = (WordClass) core.getWordClassCollection().getNodeById(curEntry.getKey());
                WordClassValue value = prop.getValueById(curEntry.getValue());
                classes += value.getValue();
            } catch (Exception e) {
                classes = "ERROR: UNABLE TO PULL CLASS";
            }
        }
        
        for (Entry<Integer, String> curEntry : conWord.getClassTextValues()) {
            if (classes.length() != 0) {
                classes += ", ";
            }
            
            try {
                WordClass prop = (WordClass) core.getWordClassCollection().getNodeById(curEntry.getKey());
                classes += prop.getValue() + ":" + curEntry.getValue();
            } catch (Exception e) {
                classes = "ERROR: UNABLE TO PULL CLASS";
            }
        }
        
        ret.add(classes);

        for (ConjugationPair conjugation : conjList) {
            try {
                ConjugationManager conMan= core.getConjugationManager();
                ConjugationNode existingValue = conMan.getConjugationByCombinedId(conWord.getId(), conjugation.combinedId);
                
                if (existingValue != null && conWord.isOverrideAutoConjugate()) {
                    ret.add(existingValue.getValue());
                }
                else {
                    ret.add(conMan.declineWord(conWord, conjugation.combinedId));
                }
            } catch (Exception e) {
                ret.add("DECLENSION ERROR");
            }
        }
        
        ret.add(WebInterface.getTextFromHtml(conWord.getDefinition()));

        return ret;
    }

    private static Object[] getWordFormOld(DictCore core, ConWord conWord, ConjugationPair[] conjList) {
        List<String> ret = new ArrayList<>();
        String declensionCell = "";

        ret.add(conWord.getValue());
        ret.add(conWord.getLocalWord());
        ret.add(conWord.getWordTypeDisplay());
        try {
            ret.add(conWord.getPronunciation());
        } catch (Exception e) {
            ret.add("<ERROR>");
        }

        String classes = "";
        for (Entry<Integer, Integer> curEntry : conWord.getClassValues()) {
            if (classes.length() != 0) {
                classes += ", ";
            }
            try {
                ConjugationManager conMan= core.getConjugationManager();
                WordClass prop = (WordClass) core.getWordClassCollection().getNodeById(curEntry.getKey());
                WordClassValue value = prop.getValueById(curEntry.getValue());
                classes += value.getValue();
            } catch (Exception e) {
                classes = "ERROR: UNABLE TO PULL CLASS";
            }
        }
        ret.add(classes);
        
        for (ConjugationPair conjugation : conjList) {
            try {
                ConjugationManager conMan= core.getConjugationManager();
                ConjugationNode existingValue = conMan.getConjugationByCombinedId(conWord.getId(), conjugation.combinedId);
                
                if (existingValue != null && conWord.isOverrideAutoConjugate()) {
                    declensionCell += existingValue.getValue() + ":";
                }
                else {
                    declensionCell += conMan.declineWord(conWord, conjugation.combinedId) + ":";
                }
            } catch (Exception e) {
                declensionCell += "DECLENSION ERROR";
            }
        }

        ret.add(declensionCell);
        ret.add(WebInterface.getTextFromHtml(conWord.getDefinition()));

        return ret.toArray();
    }

    // markdown -> pdf
    public static void exportMarkdown(String target,
        String coverImage,
        String foreword,
        boolean printConLocal,
        boolean printLocalCon,
        boolean printOrtho,
        String subTitleText,
        String titleText,
        boolean printPageNumber,
        boolean printGlossKey,
        boolean printGrammar,
        boolean printWordEtymologies,
        boolean printAllConjugations,
        boolean printPhrases,
        String chapterOrder,
        DictCore core) throws IOException
    {
        // setup
        BufferedWriter writer = new BufferedWriter(new FileWriter(target));
        Boolean buildForeword = foreword.length() != 0;
        String conToLocalName = "Dictionary " + core.conLabel() +
            " to " + core.localLabel();
        String localToConName = "Dictionary " + core.localLabel() +
            " to " + core.conLabel();
        
        // copy fonts from pgd file and re-save as local files
        // that be loaded into html
        // TODO change from regular file to temp file
        DesktopIOHandler handler = DesktopIOHandler.getInstance();
        File curFileName = new File(core.getCurFileName());
        Boolean useConFont = false;
        Boolean useLocalFont = false;
        try (ZipFile zipFile = new ZipFile(curFileName)) {
            ZipEntry fontEntry = zipFile.getEntry(PGTUtil.CON_FONT_FILE_NAME);
            if (fontEntry != null) {
                useConFont = true;
                File cF = new File("conFont.ttf");
                cF.createNewFile();
                handler.exportConFont("conFont.ttf", core.getCurFileName());
            }
            fontEntry = zipFile.getEntry(PGTUtil.LOCAL_FONT_FILE_NAME);
            if (fontEntry != null) {
                useLocalFont = true;
                File lF = new File("localFont.ttf");
                lF.createNewFile();
                handler.exportLocalFont("localFont.ttf", core.getCurFileName());
            }
        }

        // styling
        writer.write("<style>\n");
        writer.write("@media print {\n");
        writer.write("  .page=break {\n");
        writer.write("    break-before: always;\n");
        writer.write("  }\n");
        writer.write("}\n");
        if (useConFont) {
            writer.write("@font-face {\n");
            writer.write("  font-family: 'conFont';\n");
            writer.write("  src: url('./conFont.ttf') format('truetype');\n");
            writer.write("  font-weight: normal;\n");
            writer.write("  font-style: normal;\n");
            writer.write("}\n");
            writer.write(".confont {\n");
            writer.write("  font-family: 'conFont';\n");
            writer.write("}\n");
        } else {
            writer.write(".confont {\n");
            writer.write("  font-family: 'Times New Roman';\n");
            writer.write("}\n");
        }
        if (useLocalFont) {
            writer.write("@font-face {\n");
            writer.write("  font-family: 'localFont';\n");
            writer.write("  src: url('./localFont.ttf') format('truetype');\n");
            writer.write("  font-weight: normal;\n");
            writer.write("  font-style: normal;\n");
            writer.write("}\n");
            writer.write(".localfont {\n");
            writer.write("  font-family: 'localFont';\n");
            writer.write("}\n");
        } else {
            writer.write(".localfont {\n");
            writer.write("  font-family: 'Times New Roman';\n");
            writer.write("}\n");
        }
        writer.write("</style>\n");

        String pagebreak = "<div class=\"page-break\"></div>\n\n"; // TODO does this work?

        // front page
        writer.write("<div style=\"text-align: center;\">\n");
        writer.write("<p style=\"font-size: 36px;\">" + titleText + "</p>\n");
        writer.write("<p style=\"font-size: 20px;\">" + subTitleText + "</p>\n");
        String copyrightInfo = core.getPropertiesManager().getCopyrightAuthorInfo();
        if (copyrightInfo.length() != 0) {
            writer.write(copyrightInfo + "\n");
        }
        writer.write("</div>\n");
        writer.write(pagebreak);

        // table of contents
        writer.write("## Table of Contents\n");
        if (buildForeword) {
            writer.write("- [Author Foreword](#author-foreword)\n");
        }
        if (printOrtho) {
            writer.write("- [Orthography](#orthography)\n");
        }
        if (printGlossKey) {
            writer.write("- [Gloss Key](#gloss-key)\n");
        }
        if (printConLocal) {
            writer.write("- [" + conToLocalName + "](#" + 
                conToLocalName.replace(' ', '-').toLowerCase() + ")\n");
        }
        if (printLocalCon) {
            writer.write("- [" + localToConName + "](#" +
                localToConName.replace(' ', '-').toLowerCase() + ")\n");
        }
        if (printPhrases) {
            writer.write("- [Phrasebook](#phrasebook)\n");
        }
        if (printGrammar) {
            writer.write("- [Grammar](#grammar)\n");
        }
        writer.write("\n" + pagebreak);

        if (buildForeword) {
            writer.write("## Author Foreword\n");
            writer.write(foreword + "\n");
            writer.write(pagebreak);
        }

        // orthography
        if (printOrtho) {
            writer.write("## Orthography\n");
            writer.write("Symbols $ and ^ indicate that elements of orthography must appear at the beginning or the end of a word.\n\n");
            // use html tables for finer grain formatting
            writer.write("<table>\n");
            writer.write("  <tr>\n");
            writer.write("    <th>Character(s)</th>\n");
            writer.write("    <th>Pronunciation</th>\n");
            writer.write("  </tr>\n");
            for (PronunciationNode curNode : core.getPronunciationMgr().getPronunciations()) {
                // TODO handle '$' & '^'
                writer.write("  <tr>\n");
                writer.write("    <td class=\"confont\">" + curNode.getValue() + "</td>\n");
                writer.write("    <td class=\"localfont\">" +
                    curNode.getPronunciation() + "</td>\n");
                writer.write("  </tr>\n");
            }
            writer.write("</table>\n");
            writer.write(pagebreak);
        }

        // glossary keys
        if (printGlossKey) {
            writer.write("## Gloss Key\n\n");
            // use html tables for more complicated formatting
            writer.write("<table>\n");
            writer.write("  <tr>\n");
            writer.write("    <th>Part of Speech</th>\n");
            writer.write("    <th>GLoss</th>\n");
            writer.write("  <tr>\n");
            for (TypeNode curType : core.getTypes().getNodes()) {
                writer.write("  <tr>\n");
                writer.write("    <td class=\"localfont\">" + curType.getValue() + "</td>\n");
                writer.write("    <td class=\"localfont\">" + curType.getGloss() + "</td>\n");
                writer.write("  </tr>\n");
            }
            writer.write("</table>\n");
            writer.write(pagebreak);
        }

        // conlang to native
        if (printConLocal) {
            // build gloss key
            Map<Integer, String> glossKey = new HashMap<>();
            for (TypeNode curNode : core.getTypes().getNodes()) {
                if (curNode.getGloss().length() == 0) {
                    glossKey.put(curNode.getId(), curNode.getValue());
                } else {
                    glossKey.put(curNode.getId(), curNode.getGloss());
                }
            }

            // do the writing stuff
            writer.write("## " + conToLocalName + "\n");
            String curLetter = "";
            RomanizationManager romMgr = core.getRomManager();
            Boolean romEnabled = romMgr.isEnabled();
            for (ConWord curWord : core.getWordCollection().getWordNodes()) {
                // generate header for alphabet sections
                String firstLetter = curWord.getValue().substring(0, 1);
                if (!curLetter.toLowerCase().equals(firstLetter)) {
                    curLetter = firstLetter;
                    writer.write("<h3><span class=\"confont\">" + curLetter + "</span> WORDS</h3>\n");
                    writer.write("<span class=\"confont\">" + curWord.getLocalWord() + "</span>");
                    writer.write("<span class=\"localfont\">");
                    // add word type (if one exists)
                    if (glossKey.containsKey(curWord.getWordTypeId())) {
                        writer.write(" - " + glossKey.get(curWord.getWordTypeId()));
                    }
                    // add pronunciation
                    try {
                        String p = curWord.getPronunciation();
                        if (p.length() != 0) {
                            writer.write(" - " + p);
                        }
                    } catch(Exception e) {
                        // why does this throw an exception???
                    }
                    writer.write("</span><br>\n");

                    // write romanization value if active and exists
                    if (romEnabled) {
                        String romStr;
                        try {
                            romStr = romMgr.getPronunciation(curWord.getValue());
                        } catch (Exception e) {
                            romStr = "&lt;ERROR&gt;";
                        }
                        if (!romStr.isEmpty()) {
                            writer.write("\nRoman: <i>" + romStr + "</i><br>\n");
                        }
                    }

                    // TODO print word etymology tree if appropriate

                    String txt = WebInterface.getTextFromHtml(curWord.getDefinition());
                    if (!txt.isEmpty()) {
                        writer.write("<p>" + txt + "</p><br>\n");
                    }

                    if (curWord.getLocalWord().length() != 0) {
                        writer.write("Synonym(s): <span class=\"localfont\">" + curWord.getLocalWord() + "</span>\n");
                    }
                    writer.write("\n");
                }
            }
            writer.write(pagebreak);
        }

        // native to conlang
        if (printLocalCon) {
            writer.write("## " + localToConName + "\n");
            writer.write(pagebreak);
        }

        // phrasebook
        if (printPhrases) {
            writer.write("## Phrasebook\n");
            writer.write(pagebreak);
        }

        // grammar
        if (printGrammar) {
            writer.write("## Grammar\n");
            writer.write(pagebreak);
        }
        writer.close();
    }

    //
    public static void exportPdf(String markdown, String target) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
    }
}
