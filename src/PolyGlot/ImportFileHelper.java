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

import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.Nodes.WordClassValue;
import PolyGlot.Nodes.WordClass;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ImportFileHelper {

    private final DictCore core;
    private String iConWord;
    private String iLocalWord;
    private String iType;
    private String iClass;
    private String iDefinition;
    private String iPronunciation;
    private String delimiter;
    private boolean bFirstLineLabels;
    private boolean bCreateTypes;

    public ImportFileHelper(DictCore _core) {
        core = _core;
    }

    public void setOptions(String _iConWord, String _iLocalWord, String _iType,
            String _iClass, String _iDefinition, String _iPronunciation,
            String _delimiter, boolean _bFirstLineLabels, boolean _bCreateTypes) {
        iConWord = _iConWord;
        iLocalWord = _iLocalWord;
        iType = _iType;
        iClass = _iClass;
        iDefinition = _iDefinition;
        iPronunciation = _iPronunciation;
        bFirstLineLabels = _bFirstLineLabels;
        bCreateTypes = _bCreateTypes;
        delimiter = _delimiter;
    }

    /**
     * Imports lexicons from foreign formats
     *
     * @param inputFile file to import
     * @param sheetNum sheet number (use only for excel importing, can be 0
     * otherwise)
     * @throws Exception
     */
    public void importFile(String inputFile, Integer sheetNum) throws Exception {
        if (inputFile.endsWith("xls")
                || inputFile.endsWith("xlsx")
                || inputFile.endsWith("xlsm")) {
            importExcel(inputFile, sheetNum);
        } else if (inputFile.endsWith("csv")
                || inputFile.endsWith("txt")) {
            importCSV(inputFile);
        } else {
            importCSV(inputFile);
            throw new InvalidFormatException("Unrecognized file type for file: "
                    + inputFile + ". Defaulting to CSV functionality.");
        }
    }

    private void importExcel(String inputFile, Integer sheetNum) throws Exception {
        Workbook wb;
        Sheet mySheet;
        try (InputStream myFile = new FileInputStream(inputFile)) {
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
        }
    }

    private void importCSV(String inputFile) throws Exception {
        File file = new File(inputFile);
        try (FileReader reader = new FileReader(file)) {
            try (BufferedReader br = new BufferedReader(reader)) {
                String line;
                List<String> columnList;

                // skip first line if specified by user
                if (bFirstLineLabels) {
                    br.readLine();
                }

                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(delimiter);

                    ConWord newWord = new ConWord();

                    // add conword
                    columnList = Arrays.asList(iConWord.split(","));
                    for (String entry : columnList) {
                        if (entry == null || entry.length() == 0) {
                            continue;
                        }

                        Integer cellNum = cellNumCheckGet(entry);

                        // fail silently for files that truncate empty trailing fields
                        if (cellNum >= columns.length) {
                            continue;
                        }

                        if (newWord.getValue().trim().length() == 0) {
                            newWord.setValue(columns[cellNum].trim());
                        } else {
                            newWord.setValue(newWord.getValue() + ", " + columns[cellNum].trim());
                        }
                    }

                    // if conword is blank, continue. Bare minimum for imported word is a conword value.
                    if (newWord.getValue().trim().length() == 0) {
                        continue;
                    }

                    // add definition
                    columnList = Arrays.asList(iDefinition.split(","));
                    for (String entry : columnList) {
                        if (entry == null || entry.length() == 0) {
                            continue;
                        }

                        Integer cellNum = cellNumCheckGet(entry);

                        // fail silently for files that truncate empty trailing fields
                        if (cellNum >= columns.length) {
                            continue;
                        }

                        if (newWord.getDefinition().trim().length() == 0) {
                            newWord.setDefinition(columns[cellNum].trim());
                        } else {
                            newWord.setDefinition(newWord.getDefinition() + "\n\n" + columns[cellNum].trim());
                        }
                    }

                    // add classes
                    columnList = Arrays.asList(iClass.split(","));
                    for (String entry : columnList) {
                        if (entry == null || entry.length() == 0) {
                            continue;
                        }

                        Integer cellNum = cellNumCheckGet(entry);

                        // fail silently for files that truncate empty trailing fields
                        if (cellNum >= columns.length) {
                            continue;
                        }

                        String className = "CLASS" + cellNum.toString(); // guarantee unique name for user to rename later (based on column)
                        WordClass wordProp = null;

                        // find class
                        for (WordClass findProp : core.getWordPropertiesCollection().getAllWordClasses()) {
                            if (findProp.getValue().equals(className)) {
                                wordProp = findProp;
                                break;
                            }
                        }

                        // create class if doesn't yet exist
                        if (wordProp == null) {
                            wordProp = new WordClass();
                            wordProp.setValue(className);
                            int propId = core.getWordPropertiesCollection().addNode(wordProp);
                            wordProp = (WordClass) core.getWordPropertiesCollection().getNodeById(propId);
                        }

                        // find class value
                        WordClassValue wordVal = null;
                        for (WordClassValue findVal : wordProp.getValues()) {
                            if (findVal.getValue().equals(columns[cellNum].trim())) {
                                wordVal = findVal;
                                break;
                            }
                        }

                        // create class value if doesn't exist yet
                        if (wordVal == null) {
                            wordVal = new WordClassValue();
                            wordVal.setValue(columns[cellNum].trim());
                            wordVal = wordProp.addValue(columns[cellNum].trim());
                        }

                        // add class value to word
                        newWord.setClassValue(wordProp.getId(), wordVal.getId());
                    }

                    // add local word
                    columnList = Arrays.asList(iLocalWord.split(","));
                    for (String entry : columnList) {
                        if (entry == null || entry.length() == 0) {
                            continue;
                        }

                        Integer cellNum = cellNumCheckGet(entry);

                        // fail silently for files that truncate empty trailing fields
                        if (cellNum >= columns.length) {
                            continue;
                        }

                        if (newWord.getLocalWord().trim().length() == 0) {
                            newWord.setLocalWord(columns[cellNum].trim());
                        } else {
                            newWord.setLocalWord(newWord.getLocalWord() + ", " + columns[cellNum].trim());
                        }
                    }

                    // add pronunciation
                    columnList = Arrays.asList(iPronunciation.split(","));
                    for (String entry : columnList) {
                        if (entry == null || entry.length() == 0) {
                            continue;
                        }

                        Integer cellNum = cellNumCheckGet(entry);

                        // fail silently for files that truncate empty trailing fields
                        if (cellNum >= columns.length) {
                            continue;
                        }

                        if (newWord.getPronunciation().trim().length() == 0) {
                            newWord.setPronunciation(columns[cellNum].trim());
                        } else {
                            newWord.setPronunciation(newWord.getPronunciation() + ", " + columns[cellNum].trim());
                        }
                    }

                    // add type
                    columnList = Arrays.asList(iType.split(","));
                    for (String entry : columnList) {
                        if (entry == null || entry.length() == 0) {
                            continue;
                        }

                        Integer cellNum = cellNumCheckGet(entry);

                        // fail silently for files that truncate empty trailing fields
                        if (cellNum >= columns.length) {
                            continue;
                        }

                        if (newWord.getWordTypeId() == 0) {
                            newWord.setWordTypeId(core.getTypes().findOrCreate(columns[cellNum].trim()).getId());
                        } else {
                            newWord.setWordTypeId(core.getTypes().findOrCreate(newWord.getWordTypeDisplay() + ", " + columns[cellNum].trim()).getId());
                        }
                    }

                    // add type to list of potential types if applicable and user
                    // specified
                    if (bCreateTypes && newWord.getWordTypeId() != 0
                            && !core.getTypes().nodeExists(newWord.getWordTypeId())) {
                        core.getTypes().clear();
                        TypeNode newType = core.getTypes().getBufferType();
                        newType.setValue(newWord.getWordTypeDisplay());
                        core.getTypes().insert();
                    }

                    core.getWordCollection().addWord(newWord);
                }
            }
        }
    }

    private Integer cellNumCheckGet(String entry) throws Exception {
        Integer ret;

        try {
            // initialy, try to parse as int, if failure there, try to parse as column letters
            try {
                ret = Integer.valueOf(entry.trim());
            } catch (NumberFormatException e) {
                ret = columnStringValue(entry.trim());
            }
        } catch (NumberFormatException e) {
            throw new Exception("non-integer value in field.");
        }

        return ret;
    }

    /**
     * Processes Excel row
     *
     * @param row row to process
     * @throws Exception
     */
    private void processWordRow(Row row) throws Exception {
        ConWord newWord = new ConWord();

        List<String> columnList;

        // add conword
        columnList = Arrays.asList(iConWord.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.length() == 0) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            if (newWord.getValue().trim().length() == 0) {
                newWord.setValue(row.getCell(cellNum) != null ? row.getCell(cellNum).toString() : "");
            } else if (row.getCell(cellNum) != null) {
                newWord.setValue(newWord.getValue() + ", " + row.getCell(cellNum).toString());
            }
        }

        // if conword is blank, return. Bare minimum for imported word is a conword value.
        if (newWord.getValue().trim().length() == 0) {
            return;
        }

        // add definition
        columnList = Arrays.asList(iDefinition.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.length() == 0) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            if (newWord.getDefinition().trim().length() == 0) {
                newWord.setDefinition(row.getCell(cellNum) != null ? row.getCell(cellNum).toString() : "");
            } else if (row.getCell(cellNum) != null) {
                newWord.setDefinition(newWord.getDefinition() + "\n\n" + row.getCell(cellNum).toString());
            }
        }

        // add classes
        columnList = Arrays.asList(iClass.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.length() == 0) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            String className = "CLASS" + cellNum.toString(); // guarantee unique name for user to rename later (based on column)
            WordClass wordProp = null;

            // find class
            for (WordClass findProp : core.getWordPropertiesCollection().getAllWordClasses()) {
                if (findProp.getValue().equals(className)) {
                    wordProp = findProp;
                    break;
                }
            }

            // create class if doesn't yet exist
            if (wordProp == null) {
                wordProp = new WordClass();
                wordProp.setValue(className);
                int propId = core.getWordPropertiesCollection().addNode(wordProp);
                wordProp = (WordClass) core.getWordPropertiesCollection().getNodeById(propId);
            }

            // find class value
            Cell curCell = row.getCell(cellNum);
            WordClassValue wordVal = null;
            if (curCell != null) { // null = empty cell in many occasions, skip if this is the case
                for (WordClassValue findVal : wordProp.getValues()) {
                    if (findVal.getValue().equals(curCell.toString().trim())) {
                        wordVal = findVal;
                        break;
                    }
                }
            } else {
                continue;
            }

            // create class value if doesn't exist yet
            if (wordVal == null) {
                wordVal = new WordClassValue();
                wordVal.setValue(row.getCell(cellNum).toString().trim());
                wordVal = wordProp.addValue(row.getCell(cellNum).toString().trim());
            }

            // add class value to word
            newWord.setClassValue(wordProp.getId(), wordVal.getId());
        }

        // add local word
        columnList = Arrays.asList(iLocalWord.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.length() == 0) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            if (newWord.getLocalWord().trim().length() == 0) {
                newWord.setLocalWord(row.getCell(cellNum) != null ? row.getCell(cellNum).toString() : "");
            } else if (row.getCell(cellNum) != null) {
                newWord.setLocalWord(newWord.getLocalWord() + ", " + row.getCell(cellNum).toString());
            }
        }

        // add pronunciation
        columnList = Arrays.asList(iPronunciation.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.length() == 0) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            if (newWord.getPronunciation().trim().length() == 0) {
                newWord.setPronunciation(row.getCell(cellNum) != null ? row.getCell(cellNum).toString() : "");
            } else if (row.getCell(cellNum) != null) {
                newWord.setPronunciation(newWord.getPronunciation() + ", " + row.getCell(cellNum).toString());
            }
        }

        // add type
        columnList = Arrays.asList(iType.split(","));
        for (String entry : columnList) {
            if (entry == null || entry.length() == 0) {
                continue;
            }

            Integer cellNum = cellNumCheckGet(entry);

            if (newWord.getWordTypeId() == 0) {
                newWord.setWordTypeId(core.getTypes().findOrCreate(row.getCell(cellNum) != null ? row.getCell(cellNum).toString() : "").getId());
            } else if (row.getCell(cellNum) != null) {
                newWord.setWordTypeId(core.getTypes().findOrCreate(newWord.getWordTypeDisplay() + ", " + row.getCell(cellNum).toString()).getId());
            }
        }

        // add type to list of potential types if applicable and user
        // specified
        if (bCreateTypes && newWord.getWordTypeId() != 0
                && !core.getTypes().nodeExists(newWord.getWordTypeId())) {
            core.getTypes().clear();
            TypeNode newType = core.getTypes().getBufferType();
            newType.setValue(newWord.getWordTypeDisplay());
            core.getTypes().insert();
        }

        core.getWordCollection().addWord(newWord);
    }

    /**
     * Gets int value of columns when addressed by letter names
     *
     * @param _input string representing alpha value of column
     * @return int value of string for column
     * @throws Exception on invalid characters
     */
    private static int columnStringValue(String _input) throws Exception {
        int ret = 0;

        for (int i = 0; i < _input.length(); i++) {
            int baseMultiplier = (int) Math.pow(26, (_input.length() - 1) - i);

            ret += (getLetterValue(_input.substring(i, i + 1)) * baseMultiplier);
        }

        return ret - 1;
    }

    /**
     * Gets base 26 style value of letter fed in
     *
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

        switch (c) {
            case 'A':
                ret = 1;
                break;
            case 'B':
                ret = 2;
                break;
            case 'C':
                ret = 3;
                break;
            case 'D':
                ret = 4;
                break;
            case 'E':
                ret = 5;
                break;
            case 'F':
                ret = 6;
                break;
            case 'G':
                ret = 7;
                break;
            case 'H':
                ret = 8;
                break;
            case 'I':
                ret = 9;
                break;
            case 'J':
                ret = 10;
                break;
            case 'K':
                ret = 11;
                break;
            case 'L':
                ret = 12;
                break;
            case 'M':
                ret = 13;
                break;
            case 'N':
                ret = 14;
                break;
            case 'O':
                ret = 15;
                break;
            case 'P':
                ret = 16;
                break;
            case 'Q':
                ret = 17;
                break;
            case 'R':
                ret = 18;
                break;
            case 'S':
                ret = 19;
                break;
            case 'T':
                ret = 20;
                break;
            case 'U':
                ret = 21;
                break;
            case 'V':
                ret = 22;
                break;
            case 'W':
                ret = 23;
                break;
            case 'X':
                ret = 24;
                break;
            case 'Y':
                ret = 25;
                break;
            case 'Z':
                ret = 26;
                break;
            default:
                throw new Exception("Invalid character: " + letter);
        }

        return ret;
    }
}
