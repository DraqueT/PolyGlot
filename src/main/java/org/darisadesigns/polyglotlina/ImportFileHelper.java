/*
 * Copyright (c) 2014-2020, Draque Thompson, draquemail@gmail.com
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

import java.io.File;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.Nodes.WordClassValue;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.darisadesigns.polyglotlina.CustomControls.InfoBox;
import org.darisadesigns.polyglotlina.ManagersCollections.ConWordCollection;

public class ImportFileHelper {

    private final DictCore core;
    private String iConWord;
    private String iLocalWord;
    private String iType;
    private String iClass;
    private String iDefinition;
    private String iPronunciation;
    private CSVFormat format;
    private boolean bFirstLineLabels;
    private boolean bCreateTypes;
    private String quoteChar;
    private DuplicateOption dupOpt;

    public ImportFileHelper(DictCore _core) {
        core = _core;
    }

    public void setOptions(String _iConWord,
            String _iLocalWord,
            String _iType,
            String _iClass,
            String _iDefinition,
            String _iPronunciation,
            CSVFormat _format,
            boolean _bFirstLineLabels,
            boolean _bCreateTypes,
            String _quoteChar,
            DuplicateOption _dupOpt) {
        iConWord = _iConWord;
        iLocalWord = _iLocalWord;
        iType = _iType;
        iClass = _iClass;
        iDefinition = _iDefinition;
        iPronunciation = _iPronunciation;
        bFirstLineLabels = _bFirstLineLabels;
        bCreateTypes = _bCreateTypes;
        format = _format;
        quoteChar = _quoteChar;
        dupOpt = _dupOpt;
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
            importCSV(inputFile, format);
        } else {
            InfoBox.warning("Unrecognized Type", "Unrecognized type. Attempting to import as CSV", null);
            importCSV(inputFile, format);
        }
    }

    private void importExcel(String inputFile, int sheetNum) throws Exception {
        File csvFile = Java8Bridge.excelToCvs(inputFile, sheetNum);
        quoteChar = "\"";
        importCSV(csvFile.getAbsolutePath(), CSVFormat.EXCEL);
    }

    private void importCSV(String inputFile, CSVFormat _format) throws Exception {
        if (!quoteChar.isEmpty()) {
            _format = _format.withQuote(quoteChar.charAt(0));
        }

        Map<String, List<ConWord>> valueMap = core.getWordCollection().getValueMapping();
        List<List<String>> rows = getRows(inputFile, _format);
        ConWordCollection wordCollection = core.getWordCollection();

        if (bFirstLineLabels) {
            rows.remove(0);
        }

        for (List<String> row : rows) {
            List<String> columnList;
            ConWord newWord = new ConWord();

            // add conword
            columnList = Arrays.asList(iConWord.split(","));
            for (String entry : columnList) {
                if (entry == null || entry.isEmpty()) {
                    continue;
                }

                Integer cellNum = cellNumCheckGet(entry);

                // fail silently for files that truncate empty trailing fields
                if (cellNum >= row.size()) {
                    continue;
                }

                if (newWord.getValue().trim().isEmpty()) {
                    newWord.setValue(row.get(cellNum).trim());
                } else {
                    newWord.setValue(newWord.getValue() + ", " + row.get(cellNum).trim());
                }
            }

            // if conword is blank, continue. Bare minimum for imported word is a conword value.
            if (newWord.getValue().trim().isEmpty()) {
                continue;
            }

            // add definition
            columnList = Arrays.asList(iDefinition.split(","));
            for (String entry : columnList) {
                if (entry == null || entry.isEmpty()) {
                    continue;
                }

                Integer cellNum = cellNumCheckGet(entry);

                // fail silently for files that truncate empty trailing fields
                if (cellNum >= row.size()) {
                    continue;
                }

                if (newWord.getDefinition().trim().isEmpty()) {
                    newWord.setDefinition(row.get(cellNum).trim());
                } else {
                    newWord.setDefinition(newWord.getDefinition() + "\n\n" + row.get(cellNum).trim());
                }
            }

            // add classes
            columnList = Arrays.asList(iClass.split(","));
            for (String entry : columnList) {
                if (entry == null || entry.isEmpty()) {
                    continue;
                }

                Integer cellNum = cellNumCheckGet(entry);

                // fail silently for files that truncate empty trailing fields
                if (cellNum >= row.size()) {
                    continue;
                }

                String className = "CLASS" + cellNum; // guarantee unique name for user to rename later (based on column)
                WordClass wordProp = null;

                // find class
                for (WordClass findProp : core.getWordClassCollection().getAllWordClasses()) {
                    if (findProp.getValue().equals(className)) {
                        wordProp = findProp;
                        break;
                    }
                }

                // create class if doesn't yet exist
                if (wordProp == null) {
                    wordProp = new WordClass();
                    wordProp.setValue(className);
                    int propId = core.getWordClassCollection().addNode(wordProp);
                    wordProp = (WordClass) core.getWordClassCollection().getNodeById(propId);
                }

                // find class value
                WordClassValue wordVal = null;
                for (WordClassValue findVal : wordProp.getValues()) {
                    if (findVal.getValue().equals(row.get(cellNum).trim())) {
                        wordVal = findVal;
                        break;
                    }
                }

                // create class value if doesn't exist yet
                if (wordVal == null) {
                    wordVal = new WordClassValue();
                    wordVal.setValue(row.get(cellNum).trim());
                    wordVal = wordProp.addValue(row.get(cellNum).trim());
                }

                // add class value to word
                newWord.setClassValue(wordProp.getId(), wordVal.getId());
            }

            // add local word
            columnList = Arrays.asList(iLocalWord.split(","));
            for (String entry : columnList) {
                if (entry == null || entry.isEmpty()) {
                    continue;
                }

                Integer cellNum = cellNumCheckGet(entry);

                // fail silently for files that truncate empty trailing fields
                if (cellNum >= row.size()) {
                    continue;
                }

                if (newWord.getLocalWord().trim().isEmpty()) {
                    newWord.setLocalWord(row.get(cellNum).trim());
                } else {
                    newWord.setLocalWord(newWord.getLocalWord() + ", " + row.get(cellNum).trim());
                }
            }

            // add pronunciation
            columnList = Arrays.asList(iPronunciation.split(","));
            for (String entry : columnList) {
                if (entry == null || entry.isEmpty()) {
                    continue;
                }

                Integer cellNum = cellNumCheckGet(entry);

                // fail silently for files that truncate empty trailing fields
                if (cellNum >= row.size()) {
                    continue;
                }

                if (newWord.getPronunciation().trim().isEmpty()) {
                    newWord.setPronunciation(row.get(cellNum).trim());
                } else {
                    newWord.setPronunciation(newWord.getPronunciation() + ", " + row.get(cellNum).trim());
                }
            }

            // add type
            columnList = Arrays.asList(iType.split(","));
            for (String entry : columnList) {
                if (entry == null || entry.isEmpty()) {
                    continue;
                }

                Integer cellNum = cellNumCheckGet(entry);

                // fail silently for files that truncate empty trailing fields
                if (cellNum >= row.size()) {
                    continue;
                }

                if (newWord.getWordTypeId() == 0) {
                    newWord.setWordTypeId(core.getTypes().findOrCreate(row.get(cellNum).trim()).getId());
                } else {
                    newWord.setWordTypeId(core.getTypes().findOrCreate(newWord.getWordTypeDisplay() + ", " + row.get(cellNum).trim()).getId());
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

            switch (dupOpt) {
                case IMPORT_ALL:
                    wordCollection.addWord(newWord);
                    break;
                case IGNORE_DUPES:
                    // only write if does not already exist
                    if (!valueMap.containsKey(newWord.getValue())) {
                        wordCollection.addWord(newWord);
                    }
                    break;
                case OVERWRITE_DUPES:
                    // remove all values that this word would duplicate then write
                    if (valueMap.containsKey(newWord.getValue())) {
                        for (ConWord oldWord : valueMap.get(newWord.getValue())) {
                            wordCollection.deleteNodeById(oldWord.getId());
                        }
                    }

                    wordCollection.addWord(newWord);
                    break;
                default:
                // do nothing
            }
        }
    }

    /**
     * Collects all rows from given CSV file and returns string input values
     *
     * @param inputFile path of file to read
     * @return List of rows
     * @throws FileNotFoundException if CSV does not exist
     * @throws IOException if read error
     */
    private List<List<String>> getRows(String inputFile, CSVFormat format) throws FileNotFoundException, IOException, MalformedInputException {
        IllegalStateException lastException;

        try {
            return this.getRowsWithEncoding(inputFile, format, Charset.defaultCharset());
        } catch (IllegalStateException e) {
            lastException = e;
            for (Charset charset : Charset.availableCharsets().values()) {
                try {
                    return this.getRowsWithEncoding(inputFile, format, charset);
                } catch (IllegalStateException ex) {
                    lastException = ex;
                }
            }
        }

        core.getIOHandler().writeErrorLog(lastException);
        throw lastException;
    }

    private List<List<String>> getRowsWithEncoding(String inputFile,
            CSVFormat format,
            Charset charSet) throws FileNotFoundException, IOException, MalformedInputException {
        List<List<String>> ret = new ArrayList<>();
        
        try ( Reader reader = Files.newBufferedReader(Paths.get(inputFile), charSet);
                CSVParser csvParser = new CSVParser(reader, format)) {
            for (CSVRecord csvRecord : csvParser) {
                List<String> row = new ArrayList<>();

                for (int i = 0; i < csvRecord.size(); i++) {
                    row.add(csvRecord.get(i));
                }

                ret.add(deNullRow(row));
            }
        }
        
        return ret;
    }

    /**
     * Returns new list with empty strings rather than null values where
     * appropriate
     *
     * @param row input list (with potentially null values)
     * @return list guaranteed to contain no nulls
     */
    private List<String> deNullRow(List<String> row) {
        List<String> ret = new ArrayList<>();

        for (String cell : row) {
            ret.add(cell == null ? "" : cell);
        }

        return ret;
    }

    private Integer cellNumCheckGet(String entry) throws Exception {
        Integer ret;

        try {
            // initially, try to parse as int, if failure there, try to parse as column letters
            try {
                ret = Integer.valueOf(entry.trim());
            }
            catch (NumberFormatException e) {
                // questionable practice here... not logging because of nature of try/catch in programmatic logic
                // IOHandler.writeErrorLog(e);
                ret = columnStringValue(entry.trim());
            }
        }
        catch (NumberFormatException e) {
            core.getIOHandler().writeErrorLog(e);
            throw new Exception("non-integer value in field.");
        }

        return ret;
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
        int ret;

        if (letter.length() != 1) {
            throw new Exception("Invalid character: " + letter);
        }

        char c = letter.toUpperCase().charAt(0);

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

    public enum DuplicateOption {
        IMPORT_ALL, // import everything, ignoring duplicate values
        IGNORE_DUPES, // duplicates found in import file are skipped
        OVERWRITE_DUPES // duplicates found in import overwrite existing entries
    }
}
