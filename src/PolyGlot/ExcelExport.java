/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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

import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.PronunciationNode;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.Nodes.WordPropValueNode;
import PolyGlot.Nodes.WordProperty;
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
    
    /**
     * Exports a dictionary to an excel file (externally facing)
     * @param fileName Filename to export to
     * @param core dictionary core
     * @throws Exception on write error
     */
    public static void exportExcelDict(String fileName, DictCore core) throws Exception {
        ExcelExport e = new ExcelExport();
        
        e.export(fileName, core);
    }
    
    private Object[] getWordForm(ConWord conWord, DictCore core) {
        List<String> ret = new ArrayList<>();
        String declensionCell = "";
        
        ret.add(conWord.getValue());
        ret.add(conWord.getLocalWord());
        ret.add(conWord.getWordTypeDisplay());
        ret.add(conWord.getPronunciation());
        
        String classes = "";
        for (Entry<Integer, Integer> curEntry : conWord.getClassValues()) {
            if (!classes.equals("")) {
                classes += ", ";
            }
            try {
                WordProperty prop = (WordProperty)core.getWordPropertiesCollection().getNodeById(curEntry.getKey());
                WordPropValueNode value = prop.getValueById(curEntry.getValue());
                classes += value.getValue();
            } catch (Exception e) {
                classes = "ERROR: UNABLE TO PULL CLASS";
            }
        }
        ret.add(classes);
        
        List<DeclensionNode> declensions = core.getDeclensionManager().getDeclensionListWord(conWord.getId());
        
        for(DeclensionNode curNode : declensions) {
            declensionCell += curNode.getNotes() + " : " + curNode.getValue() + "\n";
        }
        
        ret.add(declensionCell);
        ret.add(conWord.getDefinition());        
        
        return ret.toArray();
    }
    
    
    
    /**
     * Exports a dictionary to an excel file
     * @param fileName Filename to export to
     * @param core dictionary core
     * @throws Exception on write error
     */
    private void export(String fileName, DictCore core) throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet;
        CellStyle localStyle = workbook.createCellStyle();
        CellStyle conStyle = workbook.createCellStyle();
        CellStyle boldHeader = workbook.createCellStyle();
        Font conFont = workbook.createFont();
        Font boldFont = workbook.createFont();
        conFont.setFontName(core.getPropertiesManager().getFontCon().getFontName());
        boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        localStyle.setWrapText(true);        
        conStyle.setWrapText(true);
        conStyle.setFont(conFont);
        boldHeader.setWrapText(true);
        boldHeader.setFont(boldFont);
        
        // record words on sheet 1        
        sheet = workbook.createSheet("Lexicon");
        Iterator<ConWord> wordIt = core.getWordCollection().getWordNodes().iterator();
        
        Row row  = sheet.createRow(0);
        row.createCell(0).setCellValue(core.conLabel().toUpperCase() + " WORD");
        row.createCell(1).setCellValue(core.localLabel().toUpperCase() + " WORD");
        row.createCell(2).setCellValue("TYPE");
        row.createCell(3).setCellValue("PRONUNCIATION");
        row.createCell(4).setCellValue("CLASS(ES)");
        row.createCell(5).setCellValue("DECLENSIONS");
        row.createCell(6).setCellValue("DEFINITIONS");
        
        for (Integer i = 1; wordIt.hasNext(); i++) {
            Object[] wordArray = getWordForm(wordIt.next(), core);
            row = sheet.createRow(i);
            for (Integer j = 0; j < wordArray.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue((String)wordArray[j]);
                
                if (j == 0) {
                    cell.setCellStyle(conStyle);
                } else {
                    cell.setCellStyle(localStyle);
                }
            }
        }
        
        // record types on sheet 2
        sheet = workbook.createSheet("Types");
        Iterator<TypeNode> typeIt = core.getTypes().getNodes().iterator();
        
        row = sheet.createRow(0);
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
        
        // record word classes on sheet 3
        sheet = workbook.createSheet("Lexical Classes");
        int propertyColumn = 0;
        for (WordProperty curProp 
                : core.getWordPropertiesCollection().getAllWordProperties()) {            
            // get row, if not exist, create
            row = sheet.getRow(0);
            if (row == null) {
                row = sheet.createRow(0);
            }
            
            Cell cell = row.createCell(propertyColumn);
            cell.setCellValue(curProp.getValue());            
            cell.setCellStyle(boldHeader);
            
            int rowIndex = 1;
            for (WordPropValueNode curVal : curProp.getValues()) {
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
        
        // record pronunciations on sheet 4
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
}
