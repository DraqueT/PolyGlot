/*
 * Copyright (c) 2014, Draque Thompson, draquemail@gmail.com
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelConReader {

    private final DictCore core;
    private String iConWord;
    private String iLocalWord;
    private String iType;
    private String iGender;
    private String iDefinition;
    private String iPronunciation;
    private String iPlural;
    private boolean bFirstLineLabels;
    private boolean bCreateGenders;
    private boolean bCreateTypes;

    public ExcelConReader(DictCore _core) {
        core = _core;
    }
    
    public void setOptions(String _iConWord, String _iLocalWord, String _iType,
            String _iGender, String _iDefinition, String _iPronunciation,
            String _iPlural,
            boolean _bFirstLineLabels, boolean _bCreateTypes,
            boolean _bCreateGenders) {
        iConWord = _iConWord;
        iLocalWord = _iLocalWord;
        iType = _iType;
        iGender = _iGender;
        iDefinition = _iDefinition;
        iPronunciation = _iPronunciation;
        iPlural = _iPlural;
        bFirstLineLabels = _bFirstLineLabels;
        bCreateTypes = _bCreateTypes;
        bCreateGenders = _bCreateGenders;
    }

    public void importExcel(String inputFile, Integer sheetNum) throws Exception {
        Workbook wb;
        Sheet mySheet;
        InputStream myFile;
        myFile = new FileInputStream(inputFile);
                
        wb = WorkbookFactory.create(myFile);

        mySheet = wb.getSheetAt(sheetNum);
 
        Iterator<Row> rowIterator = mySheet.iterator();

        // if first row is labels, skip
        if (bFirstLineLabels && rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            processWordRow(rowIterator.next());
        }

        myFile.close();
    }

    private Integer cellNumCheckGet(String entry) throws Exception {
        Integer ret;

        try {
            // initialy, try to parse as int, if failure there, try to parse as column letters
            try {
                ret = Integer.valueOf(entry.trim());
            } catch (Exception e) {
                ret = columnStringValue(entry.trim());
            }
        } catch (NumberFormatException e) {
            throw new Exception("non-integer value in field.");
        }

        return ret;
    }

    private void processWordRow(Row row) throws Exception {
        ConWord newWord = new ConWord();

        List<String> columnList;

        // add conword
        columnList = Arrays.asList(iConWord.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.equals("")) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            if (newWord.getValue().trim().equals("")) {
                newWord.setValue(row.getCell(cellNum) != null ? row.getCell(cellNum).toString() : "");
            } else {
                if (row.getCell(cellNum) != null) {
                    newWord.setDefinition(newWord.getDefinition() + ", " + row.getCell(cellNum).toString());
                }
            }
        }
        
        // if conword is blank, return. Bare minimum for imported word is a conword value.
        if (newWord.getValue().trim().equals("")) {
            return;
        }

        // add definition
        columnList = Arrays.asList(iDefinition.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.equals("")) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            if (newWord.getDefinition().trim().equals("")) {
                newWord.setDefinition(row.getCell(cellNum) != null ? row.getCell(cellNum).toString() : "");
            } else {
                if (row.getCell(cellNum) != null) {
                    newWord.setDefinition(newWord.getDefinition() + "\n\n" + row.getCell(cellNum).toString());
                }
            }
        }

        // add gender
        columnList = Arrays.asList(iGender.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.equals("")) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            if (newWord.getGender().trim().equals("")) {
                newWord.setGender(row.getCell(cellNum) != null ? row.getCell(cellNum).toString() : "");
            } else {
                if (row.getCell(cellNum) != null) {
                    newWord.setDefinition(newWord.getDefinition() + ", " + row.getCell(cellNum).toString());
                }
            }
        }
        // add gender to list of potential genders if applicable and user
        // specified
        if (bCreateGenders && !newWord.getGender().trim().equals("")
                && !core.getGenders().nodeExists(newWord.getGender())) {
            ConWord newGender = new ConWord();
            newGender.setValue(newWord.getGender());
            core.getGenders().addNode(newGender);
        }

        // add local word
        columnList = Arrays.asList(iLocalWord.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.equals("")) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            if (newWord.getLocalWord().trim().equals("")) {
                newWord.setLocalWord(row.getCell(cellNum) != null ? row.getCell(cellNum).toString() : "");
            } else {
                if (row.getCell(cellNum) != null) {
                    newWord.setDefinition(newWord.getDefinition() + ", " + row.getCell(cellNum).toString());
                }
            }
        }

        // add pronunciation
        columnList = Arrays.asList(iPronunciation.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.equals("")) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            if (newWord.getPronunciation().trim().equals("")) {
                newWord.setPronunciation(row.getCell(cellNum) != null ? row.getCell(cellNum).toString() : "");
            } else {
                if (row.getCell(cellNum) != null) {
                    newWord.setDefinition(newWord.getDefinition() + ", " + row.getCell(cellNum).toString());
                }
            }
        }

        // add type
        columnList = Arrays.asList(iType.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.equals("")) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            if (newWord.getWordType().trim().equals("")) {
                newWord.setWordType(row.getCell(cellNum) != null ? row.getCell(cellNum).toString() : "");
            } else {
                if (row.getCell(cellNum) != null) {
                    newWord.setDefinition(newWord.getDefinition() + ", " + row.getCell(cellNum).toString());
                }
            }
        }

        // add type to list of potential types if applicable and user
        // specified
        if (bCreateTypes && !newWord.getWordType().trim().equals("")
                && !core.getTypes().nodeExists(newWord.getWordType())) {
            core.getTypes().clear();
            TypeNode newType = core.getTypes().getBufferType();
            newType.setValue(newWord.getWordType());
            core.getTypes().insert();
        }

        core.getWordCollection().addWord(newWord);
    }
    
    /**
     * Gets int value of columns when addressed by letter names
     * @param _input string representing alpha value of column
     * @return int value of string for column
     * @throws Exception on invalid characters
     */
    private static int columnStringValue(String _input) throws Exception {
        int ret = 0;
        
        for (int i = 0; i < _input.length(); i++) {
            int baseMultiplier = (int)Math.pow(26, (_input.length() - 1) - i);
            
            ret += (getLetterValue(_input.substring(i, i+1)) * baseMultiplier);
        }
        
        return ret - 1;
    }
    
    /**
     * Gets base 26 style value of letter fed in
     * @param letter string of single letter to be evaluated
     * @return integer value of character in string
     * @throws Exception if string length != 1, or not a letter
     */
    private static int getLetterValue(String letter) throws Exception {
        int ret = 0;
        
        if (letter.length() > 1 || letter.length() == 0) {
            throw new Exception("Invalid character: " + letter);
        }
        
        Character c = letter.toUpperCase().charAt(0);
        
        switch(c) {
            case 'A': ret = 1; break;
            case 'B': ret = 2; break;
            case 'C': ret = 3; break;
            case 'D': ret = 4; break;
            case 'E': ret = 5; break;
            case 'F': ret = 6; break;
            case 'G': ret = 7; break;
            case 'H': ret = 8; break;
            case 'I': ret = 9; break;
            case 'J': ret = 10; break;
            case 'K': ret = 11; break;
            case 'L': ret = 12; break;
            case 'M': ret = 13; break;
            case 'N': ret = 14; break;
            case 'O': ret = 15; break;
            case 'P': ret = 16; break;
            case 'Q': ret = 17; break;
            case 'R': ret = 18; break;
            case 'S': ret = 19; break;
            case 'T': ret = 20; break;
            case 'U': ret = 21; break;
            case 'V': ret = 22; break;
            case 'W': ret = 23; break;
            case 'X': ret = 24; break;
            case 'Y': ret = 25; break;
            case 'Z': ret = 26; break;
            default: throw new Exception("Invalid character: " + letter);
        }
        
        return ret;
    }
}
