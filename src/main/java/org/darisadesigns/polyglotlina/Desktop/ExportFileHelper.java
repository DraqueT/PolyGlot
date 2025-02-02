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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.darisadesigns.polyglotlina.WebInterface;
import org.darisadesigns.polyglotlina.ManagersCollections.ConjugationManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import org.darisadesigns.polyglotlina.Nodes.WordClassValue;

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
}
