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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
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
import org.darisadesigns.polyglotlina.ManagersCollections.PropertiesManager;
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
     * Exports Language to a PDF
     * @param target
     * @param coverImage
     * @param foreward - If not empty, create a page containing this as the foreward
     * @param printConLocal If true,
     * @param printLocalCon If true,
     * @param printOrtho If true, add a chart showing character(s) paired with
     * their appropriate pronunciations.
     * @param subTitleText Sub-title
     * @param titleText Title
     * @param printPageNumber If true, add the page number
     * @param printGlossKey If true, add a glossary
     * @param printGrammar
     * @param printWordEtymologies
     * @param printAllConjugations
     * @param printPhrases
     * @param chapterOrder
     * @param core The language to export
     * @throws IOException
     */
    public static void exportPdf(String target,
        String coverImage, String foreward,
        boolean printConLocal, boolean printLocalCon, boolean printOrtho,
        String subTitleText, String titleText,
        boolean printPageNumber, boolean printGlossKey, boolean printGrammar,
        boolean printWordEtymologies, boolean printAllConjugations, boolean printPhrases,
        String chapterOrder, DictCore core) throws IOException
    {
        // core references
        PropertiesManager propMan = core.getPropertiesManager();

        // pdfbox vars
        float conFontSize = (float) propMan.getConFontSize();
        float localFontSize = (float) propMan.getLocalFontSize();

        String[] chapOrderStr = chapterOrder.split(",");
        int[] chapOrder = new int[chapOrderStr.length];
        for (int i = 0; i < chapOrderStr.length; i++) {
            chapOrder[i] = Integer.parseInt(chapOrderStr[i]);
        }
        int pageNum = 1;

        // setup PDF
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        PDPageContentStream stream = new PDPageContentStream(doc, page);

        // export fonts contained in the zipped pgd file to regular temp files
        // so that pdfbox can actually import them
        File tmpConFontFile = File.createTempFile("PolyGlotLocalFont", ".ttf",
            PGTUtil.getTempDirectory().toFile());
        File tmpLocalFontFile = File.createTempFile("PolyGlotLocalFont", ".ttf",
            PGTUtil.getTempDirectory().toFile());
        tmpConFontFile.deleteOnExit(); 
        tmpLocalFontFile.deleteOnExit();
        PDFont conFont;
        PDFont localFont;
        try {
            DesktopIOHandler.getInstance().exportConFont(tmpConFontFile.getCanonicalPath(),
                core.getCurFileName());
            conFont = PDType0Font.load(doc, tmpConFontFile);
        } catch (IOException e) {
            System.out.println("Failed to import conlang font into pdfbox");
            conFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        }
        try {
            DesktopIOHandler.getInstance().exportLocalFont(tmpLocalFontFile.getCanonicalPath(),
                core.getCurFileName());
            localFont = PDType0Font.load(doc, tmpLocalFontFile);
        } catch (IOException e) {
            System.out.println("Failed to import local font into pdfbox");
            localFont = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        }

        // front page
        stream.beginText();
        float xStart = 0;
        float yStart = page.getMediaBox().getHeight() - 100;
        float width = page.getMediaBox().getWidth() - 100;
        stream.newLineAtOffset(xStart, yStart);
        String title = titleText;
        if (title.length() == 0) {
            String langName = propMan.getLangName();
            if (langName.length() == 0) {
                title = "LANGUAGE GUIDE";
            } else {
                title = WebInterface.getTextFromHtml(langName);
            }
        }
        stream.setLeading(14.5f);
        addCenteredText(stream, page, localFont, 36, 36f, title);
        addPageNumber(titleText, pageNum, stream, page.getMediaBox(), localFont);

        if (subTitleText.length() != 0) {
            yStart -= 100;
            addCenteredText(stream, page, localFont, 14, 14.5f, subTitleText);
        }
        stream.setFont(localFont, 14);
        stream.newLine();
        stream.newLine();
        // TODO cover image
        String copyright = propMan.getCopyrightAuthorInfo();
        if (copyright.length() != 0) {
            stream.newLine();
            stream.newLine();
            List<String> lines = wrapText(WebInterface.getTextFromHtml(copyright), localFont, localFontSize, width);
            for (String line : lines) {
                addCenteredText(stream, page, localFont, localFontSize, localFontSize, line);
            }
        }
        stream.endText();
        stream.close();
        // end front page

        // next set of pages should be created, but since they are optional,
        // need to track the order in which they are added for the table of contents
        List<String> chapTitles = new ArrayList<>();
        List<PDAnnotationLink> links = new ArrayList<>();
        
        // foreward
        if (foreward.length() != 0) {
            page = new PDPage();
            doc.addPage(page);
            stream = new PDPageContentStream(doc, page);
            stream.beginText();
            stream.newLineAtOffset(50, page.getMediaBox().getHeight() - 100);
            stream.setFont(localFont, 24);
            stream.showText("Author Foreword");
            stream.newLine();
            stream.newLineAtOffset(20, -20);
            stream.setFont(localFont, 20);
            stream.showText(foreward);

            pageNum += 1;
            addPageNumber(titleText, pageNum + 1, stream, page.getMediaBox(), localFont);
            stream.endText();
            stream.close();

            chapTitles.add("Author Foreward");
            PDAnnotationLink link = new PDAnnotationLink();
            PDPageDestination dest = new PDPageFitWidthDestination();
            dest.setPage(page);
            PDActionGoTo action = new PDActionGoTo();
            action.setDestination(dest);
            link.setAction(action);
            link.setPage(page);
            links.add(link);
        }
        // end foreward

        // following sections can be reordered
        float rect_height = 0;
        for (int chap : chapOrder) {
            rect_height += 100f;
            switch (chap) {
                case PGTUtil.CHAP_CONTOLOCAL:
                    if (printConLocal) {
                        //chapTitles.add("Dictionary: " + core.conLabel() + " to " + core.localLabel());
                    }
                    break;
                case PGTUtil.CHAP_GLOSSKEY:
                    if (printGlossKey) {
                        //chapTitles.add("Gloss Key");
                    }
                    break;
                case PGTUtil.CHAP_GRAMMAR:
                    if (printGrammar) {
                        //chapTitles.add("Grammar");
                    }
                    break;
                case PGTUtil.CHAP_LOCALTOCON:
                    if (printLocalCon) {
                        //chapTitles.add("Dictionary: " + core.localLabel() + " to " + core.conLabel());
                    }
                    break;
                case PGTUtil.CHAP_ORTHOGRAPHY:
                    if (printOrtho) {
                        page = new PDPage();
                        doc.addPage(page);
                        stream = new PDPageContentStream(doc, page);
                        stream.beginText();
                        stream.newLineAtOffset(50, page.getMediaBox().getHeight() - 100);
                        stream.setFont(localFont, 20);
                        stream.showText("Orthography");
                        stream.endText();
                        stream.close();

                        chapTitles.add("Orthography");
                        PDAnnotationLink link = new PDAnnotationLink();
                        PDPageDestination dest = new PDPageFitWidthDestination();
                        dest.setPage(page);
                        PDActionGoTo action = new PDActionGoTo();
                        action.setDestination(dest);
                        link.setAction(action);
                        link.setPage(page);
                        links.add(link);
                    }
                    break;
                case PGTUtil.CHAP_PHRASEBOOK:
                    if (printPhrases) {
                        //chapTitles.add("Phrasebook");
                    }
                    break;
                default:
                    System.out.println("Unrecognized chapter key: " + chap);
                    break;
            }
        }

        // table of contents
        page = new PDPage();
        PDPageTree pages = doc.getDocumentCatalog().getPages();
        pages.insertBefore(page, pages.get(1));
        stream = new PDPageContentStream(doc, page);

        // create rectangle around everything
        xStart = 50;
        yStart = page.getMediaBox().getHeight() - rect_height - 130;
        width = page.getMediaBox().getWidth() - 100;
        stream.setStrokingColor(0, 0, 0); // rgb black
        stream.setLineWidth(2);
        stream.addRect(xStart, yStart,
            width, rect_height); // rectangles draw upwards, not downwards
        stream.stroke();

        stream.beginText();
        stream.newLineAtOffset(50, page.getMediaBox().getHeight() - 100);
        stream.setFont(localFont, 36);
        stream.setLeading(36f);
        stream.showText("Table of Contents");
        stream.endText();

        yStart = page.getMediaBox().getHeight() - 200;
        for (int i = 0; i < chapTitles.size(); i++) {
            String chap = chapTitles.get(i);
            float textWidth = localFont.getStringWidth(chap) / 1000 * localFontSize;
            float textHeight = localFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * localFontSize;
            stream.beginText();
            stream.setFont(localFont, localFontSize);
            stream.newLineAtOffset(100, yStart);
            stream.showText(chapTitles.get(i));
            stream.endText();

            PDRectangle rect = new PDRectangle(95, yStart - 5, textWidth + 10, textHeight + 5);
            stream.setStrokingColor(1, 1, 1);
            stream.addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
            stream.stroke();

            links.get(i).setRectangle(rect);
            page.getAnnotations().add(links.get(i));
        }

        stream.beginText();
        pageNum += 1;
        addPageNumber(titleText, 2, stream, page.getMediaBox(), localFont);

        stream.close();
        // end table of contents

        // final page
        page = new PDPage();
        doc.addPage(page);
        stream = new PDPageContentStream(doc, page);
        stream.beginText();
        xStart = 0;
        yStart = page.getMediaBox().getHeight() - 100;
        width = page.getMediaBox().getWidth() - 100;
        stream.newLineAtOffset(xStart, yStart);

        addCenteredText(stream, page, localFont, 20, 20f,
            "Created with PolyGlot: Language Creation Tool Version " + PGTUtil.PGT_VERSION);
        addCenteredText(stream, page, localFont, 20, 20f,
            "PolyGlot Created by Draque Thompson");

        pageNum += 1;
        addPageNumber(titleText, pageNum, stream, page.getMediaBox(), localFont);
        stream.endText();
        stream.close();
        // end final page

        doc.save(new File(target));

        doc.close();
    }

    private static void addCenteredText(PDPageContentStream stream, PDPage page,
        PDFont font, float fontSize, float leading, String text)
        throws IOException
    {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float pageWidth = page.getMediaBox().getWidth();

        float x = (pageWidth - textWidth) / 2;

        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, 0);
        stream.showText(text);
        stream.newLineAtOffset(-x, -leading);
    }

    private static List<String> wrapText(String text, PDFont font, float fontSize, float width) throws IOException {
        List<String> lines = new java.util.ArrayList<>();

        for (String line : text.split("\\r?\\n")) {
            List<String> words = java.util.Arrays.asList(line.split("\\s+"));
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                float wordWidth = font.getStringWidth(word) * fontSize / 1000f;
                float currentLineWidth = font.getStringWidth(currentLine.toString());

                if (currentLineWidth + wordWidth > width) {
                    lines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder(word + " ");
                } else {
                    currentLine.append(word).append(" ");
                }
            }
            lines.add(currentLine.toString().trim()); // add the last line
        }
        return lines;
    }

    private static void addPageNumber(String name, int page, PDPageContentStream stream,
        PDRectangle mediaBox, PDFont font) throws IOException
    {
        // reset state to make logic simple
        stream.endText();
        stream.beginText();
        stream.setFont(font, 12);
        
        // language on the left side
        stream.newLineAtOffset(20, 10);
        stream.showText(name);
        stream.endText();
        stream.beginText();
        stream.setFont(font, 12);
        stream.newLineAtOffset(mediaBox.getWidth() - 50, 10);
        stream.showText("Page " + page);
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
        boolean separateDeclensions) throws Exception
    {
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
