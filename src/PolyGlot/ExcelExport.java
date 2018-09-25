/*
 * Copyright (c) 2014-2018, Draque Thompson, draquemail@gmail.com
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

import PolyGlot.CustomControls.InfoBox;
import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.PronunciationNode;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.Nodes.DeclensionPair;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.Nodes.WordClassValue;
import PolyGlot.Nodes.WordClass;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

/**
 * This class exports an existing dictionary to an excel spreadsheet
 *
 * @author Draque
 */
public class ExcelExport {
    
    private final DictCore core;
    HSSFWorkbook workbook = new HSSFWorkbook();
    HSSFSheet sheet;
    CellStyle localStyle = workbook.createCellStyle();
    CellStyle conStyle = workbook.createCellStyle();
    CellStyle boldHeader = workbook.createCellStyle();
    Font conFont = workbook.createFont();
    Font boldFont = workbook.createFont();
    
    private ExcelExport(DictCore _core) {
        core = _core;
        
        conFont.setFontName(core.getPropertiesManager().getFontCon().getFontName());
        boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        localStyle.setWrapText(true);
        conStyle.setWrapText(true);
        conStyle.setFont(conFont);
        boldHeader.setWrapText(true);
        boldHeader.setFont(boldFont);
    }
    
    /**
     * Exports a dictionary to an excel file (externally facing)
     *
     * @param fileName Filename to export to
     * @param core dictionary core
     * @param separateDeclensions whether to separate parts of speech into separate pages for declension values
     * @throws Exception on write error
     */
    public static void exportExcelDict(String fileName, DictCore core, boolean separateDeclensions) throws Exception {
        ExcelExport e = new ExcelExport(core);

        e.export(fileName, separateDeclensions);
    }

    private Object[] getWordForm(ConWord conWord) {
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
                WordClass prop = (WordClass) core.getWordPropertiesCollection().getNodeById(curEntry.getKey());
                WordClassValue value = prop.getValueById(curEntry.getValue());
                classes += value.getValue();
            } catch (Exception e) {
                classes = "ERROR: UNABLE TO PULL CLASS";
            }
        }
        ret.add(classes);

        List<DeclensionNode> declensions = core.getDeclensionManager().getDeclensionListWord(conWord.getId());

        declensionCell = declensions.stream().map(
                (curNode) -> {
                    return curNode.getNotes() + " : " + curNode.getValue() + "\n";
                }).reduce(declensionCell, String::concat);

        ret.add(declensionCell);
        ret.add(conWord.getDefinition());

        return ret.toArray();
    }
    
    /**
     * Exports a dictionary to an excel file
     *
     * @param fileName Filename to export to
     * @param separateDeclensions whether to separate parts of speech into separate pages for declension values
     * @throws Exception on write error
     */
    private void export(String fileName, boolean separateDeclensions) throws Exception {
        this.recordWords(separateDeclensions);
        
        // record types on sheet
        sheet = workbook.createSheet("Types");
        Iterator<TypeNode> typeIt = core.getTypes().getNodes().iterator();

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("TYPE");
        row.createCell(1).setCellValue("NOTES");

        for (Integer i = 1; typeIt.hasNext(); i++) {
            TypeNode curNode = typeIt.next();
            row = sheet.createRow(i);

            Cell cell = row.createCell(0);
            cell.setCellValue(curNode.getValue());
            cell = row.createCell(1);
            cell.setCellValue(curNode.getNotes());
            cell.setCellStyle(localStyle);
        }

        // record word classes on sheet
        sheet = workbook.createSheet("Lexical Classes");
        int propertyColumn = 0;
        for (WordClass curProp
                : core.getWordPropertiesCollection().getAllWordClasses()) {
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

        // record pronunciations on sheet
        sheet = workbook.createSheet("Pronunciations");
        Iterator<PronunciationNode> procIt = core.getPronunciationMgr().getPronunciations().iterator();

        row = sheet.createRow(0);
        row.createCell(0).setCellValue("CHARACTER(S)");
        row.createCell(1).setCellValue("PRONUNCIATION");

        for (Integer i = 1; procIt.hasNext(); i++) {
            PronunciationNode curNode = procIt.next();
            row = sheet.createRow(i);

            Cell cell = row.createCell(0);
            cell.setCellStyle(conStyle);
            cell.setCellValue(curNode.getValue());

            cell = row.createCell(1);
            cell.setCellStyle(localStyle);
            cell.setCellValue(curNode.getPronunciation());
        }

        try {
            try (FileOutputStream out = new FileOutputStream(new File(fileName))) {
                workbook.write(out);
            }
        } catch (IOException e) {
            throw new Exception("Unable to write file: " + fileName);
        }
    }
    
    private void recordWords(boolean separateDeclensions) {
        // separate words by part of speech if requested so that each POS can have distinct declension columns
        if (separateDeclensions) {
            // create separate page for each part of speech
            core.getTypes().getNodes().forEach((type) -> {
                sheet = workbook.createSheet("Lexicon: " + type.getValue());
                ConWord filter = new ConWord();
                filter.setWordTypeId(type.getId());
                
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue(core.conLabel().toUpperCase() + " WORD");
                row.createCell(1).setCellValue(core.localLabel().toUpperCase() + " WORD");
                row.createCell(2).setCellValue("PoS");
                row.createCell(3).setCellValue("PRONUNCIATION");
                
                // create column for each declension
                int colNum = 4;
                Iterator<DeclensionPair> decIt = core.getDeclensionManager().getAllCombinedIds(type.getId()).iterator();
                while (decIt.hasNext()) {
                    DeclensionPair curDec = decIt.next();
                    
                    
                    colNum++;
                }
                
                try {
                    core.getWordCollection().filteredList(filter).forEach((word) -> {
                        
                    });
                } catch (Exception e) {
                    InfoBox.error("Export Error", 
                            "Unable to export " + type.getValue() + " lexical values", 
                            core.getRootWindow());
                }
            });
        } else {
            recordWordsOld();
        }
    }
    
    /**
     * Old style of printing words
     */
    private void recordWordsOld () {
        sheet = workbook.createSheet("Lexicon");
        
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(core.conLabel().toUpperCase() + " WORD");
        row.createCell(1).setCellValue(core.localLabel().toUpperCase() + " WORD");
        row.createCell(2).setCellValue("PoS");
        row.createCell(3).setCellValue("PRONUNCIATION");
        row.createCell(4).setCellValue("CLASS(ES)");
        row.createCell(5).setCellValue("DECLENSIONS");
        row.createCell(6).setCellValue("DEFINITIONS");

        Iterator<ConWord> wordIt = core.getWordCollection().getWordNodes().iterator();
        for (Integer i = 1; wordIt.hasNext(); i++) {
            Object[] wordArray = getWordForm(wordIt.next());
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
}
