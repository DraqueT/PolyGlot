/*
 * Copyright (c) 2016-2018, Draque Thompson
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

import PolyGlot.CustomControls.GrammarChapNode;
import PolyGlot.CustomControls.GrammarSectionNode;
import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PPanelDrawEtymology;
import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.Nodes.DeclensionPair;
import PolyGlot.Nodes.ImageNode;
import PolyGlot.Nodes.PEntry;
import PolyGlot.Nodes.PronunciationNode;
import PolyGlot.Nodes.WordClassValue;
import PolyGlot.Nodes.WordClass;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.ColumnDocumentRenderer;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.Property;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.layout.renderer.DocumentRenderer;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Given a core dictionary, this class will print it to a PDF file.
 *
 * @author draque.thompson
 */
public class PExportToPDF {

    private final String DICTCON2LOC = "DICTCON2LOC";
    private final String DICTLOC2CON = "DICTLOC2CON";
    private final String FOREWORD = "FOREWORD";
    private final String ORTHOGRAPHY = "ORTHOGRAPHY";
    private final String GLOSSKEY = "GLOSSKEY";
    private final String GRAMMAR = "GRAMMAR";
    private final Map<Integer, String> glossKey;
    private final List<PEntry<Div, String>> chapList = new ArrayList<>();
    private final List<SecEntry> chapSects = new ArrayList<>();
    private final Map<String, String> chapTitles = new HashMap<>();
    private final int offsetSize = 1;
    private final int defFontSize = 8;
    private final int pageNumberY = 10;
    private final int pageNumberX = 550;
    protected PdfFormXObject template;
    private final DictCore core;
    private final String targetFile;
    private Document document;
    private final byte[] conFontFile;
    private final byte[] unicodeFontFile;
    private final byte[] unicodeFontItalicFile;
    private final PdfFont conFont;
    private final PdfFont unicodeFont;
    private final PdfFont unicodeFontItalic;
    private final int conFontSize;
    private boolean printLocalCon = false;
    private boolean printConLocal = false;
    private boolean printOrtho = false;
    private boolean printGrammar = false;
    private boolean printGlossKey = false;
    private boolean printPageNumber = false;
    private boolean printWordEtymologies = false;
    private boolean printAllConjugations = false;
    private String coverImagePath = "";
    private String forewardText = "";
    private String titleText = "";
    private String subTitleText = "";
    private String log = "";

    /**
     * Exports language to presentable PDF
     *
     * @param _core dictionary core
     * @param _targetFile target path to write
     * @throws IOException
     */
    public PExportToPDF(DictCore _core, String _targetFile) throws IOException {
        core = _core;
        targetFile = _targetFile;
        conFontFile = core.getPropertiesManager().getCachedFont();
        unicodeFontFile = new IOHandler().getUnicodeFontByteArray();
        unicodeFontItalicFile = new IOHandler().getUnicodeFontItalicByteArray();
        unicodeFont = PdfFontFactory.createFont(unicodeFontFile, PdfEncodings.IDENTITY_H, true);
        unicodeFont.setSubset(true);
        unicodeFontItalic = PdfFontFactory.createFont(unicodeFontItalicFile, PdfEncodings.IDENTITY_H, true);
        unicodeFontItalic.setSubset(true);

        // If font file still null, no custom font was loaded.
        if (conFontFile == null) {
            // If confont not specified, assume that the conlang requires unicode characters
            conFont = unicodeFont;
        } else {
            conFont = PdfFontFactory.createFont(conFontFile, PdfEncodings.IDENTITY_H, true);
        }

        conFontSize = core.getPropertiesManager().getFontSize();
        glossKey = getGlossKey();
    }

    /**
     * Prints PDF document given parameters provided
     *
     * @throws java.io.FileNotFoundException
     */
    public void print() throws FileNotFoundException, IOException, Exception {
        PdfDocument pdf = new PdfDocument(new PdfWriter(targetFile));
        document = new Document(pdf);
        DocumentRenderer defRender = new DocumentRenderer(document, false);
        document.setRenderer(defRender);
        ColumnDocumentRenderer dictRender = getColumnRender();
        PdfCanvas canvas = null;

        // set up page numbers on document
        if (printPageNumber) {
            template = new PdfFormXObject(new Rectangle(pageNumberX, pageNumberY, 30, 30));
            canvas = new PdfCanvas(template, pdf);
            HeaderHandler headerHandler = new HeaderHandler();
            headerHandler.setHeader(core.getPropertiesManager().getLangName());
            pdf.addEventHandler(PdfDocumentEvent.START_PAGE, headerHandler);
        }

        try {
            // front page is always built/added before chapter guide
            document.add(buildFrontPage());
            if (forewardText.length() != 0) {
                chapTitles.put(FOREWORD, "Author Foreword");
                chapList.add(new PEntry<>(buildForward(FOREWORD), FOREWORD));
            }
            if (printOrtho) {
                chapTitles.put(ORTHOGRAPHY, "Orthography");
                chapList.add(new PEntry<>(buildOrthography(ORTHOGRAPHY), ORTHOGRAPHY));
            }
            if (printGlossKey) {
                chapTitles.put(GLOSSKEY, "Gloss Key");
                chapList.add(new PEntry<>(buildGlossKey(GLOSSKEY), GLOSSKEY));
            }

            if (printConLocal) {
                String title = "Dictionary: ";
                title += core.conLabel();

                title += " to ";

                title += core.localLabel();

                chapTitles.put(DICTCON2LOC, title);
                chapList.add(new PEntry<>(null, DICTCON2LOC));
            }
            if (printLocalCon) {
                String title = "Dictionary: ";
                title += core.localLabel();
                title += " to ";
                title += core.conLabel();

                chapTitles.put(DICTLOC2CON, title);
                chapList.add(new PEntry<>(null, DICTLOC2CON));
            }

            if (printGrammar) {
                chapTitles.put(GRAMMAR, "Grammar");
                chapList.add(new PEntry<>(buildGrammar(GRAMMAR), GRAMMAR));
            }

            // build table of contents
            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            document.add(new Paragraph(
                    new Text("Table of Contents")
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(30)));
            Div ToC = new Div();
            chapList.forEach((curChap) -> {
                Link link = new Link(chapTitles.get((String) curChap.getValue()),
                        PdfAction.createGoTo((String) curChap.getValue()));
                ToC.add(new Paragraph(link).add("\n").add(" "));
                // create subheadings for grammar chapter
                if (curChap.getValue().equals(GRAMMAR)) {
                    Paragraph subSec = new Paragraph();
                    subSec.setMarginLeft(20f);
                    chapSects.forEach((chapSect) -> {
                        Link secLink = new Link((String) chapSect.getValue(),
                                PdfAction.createGoTo(String.valueOf((int) chapSect.getKey())));
                        secLink.setFontSize(9);
                        subSec.add(secLink).add("\n");
                    });
                    ToC.add(subSec);
                }
            });
            ToC.setBorder(new SolidBorder(0.5f)).setPadding(10);

            document.add(ToC);

            // add chapters (must be done in separate loop to maintain proper spacing in PDF)
            for (Entry curEntry : chapList) {
                Div curChap = (Div) curEntry.getKey();
                Text header = new Text((String) chapTitles.get((String) curEntry.getValue()) + "\n")
                        .setFontSize(20);
                header.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD));
                header.setTextAlignment(TextAlignment.CENTER);
                document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                // dictionary sections are 2 column style
                if (curEntry.getValue().equals(DICTCON2LOC)
                        || curEntry.getValue().equals(DICTLOC2CON)
                        || curEntry.getValue().equals(ORTHOGRAPHY)) {
                    dictRender.getCurrentArea().setBBox(defRender.getCurrentArea().getBBox());
                    document.setRenderer(dictRender);
                    document.add(new AreaBreak(AreaBreakType.LAST_PAGE));
                    document.add(new Paragraph(header));

                    // Sloppy architecture left over from iText5 upconversion...
                    if (curEntry.getValue().equals(DICTCON2LOC)) {
                        buildConToLocalDictionary(DICTCON2LOC);
                    } else if (curEntry.getValue().equals(DICTLOC2CON)) {
                        buildLocalToConDictionary(DICTLOC2CON);
                    } else if (curEntry.getValue().equals(ORTHOGRAPHY)) {
                        document.add(curChap);
                    }

                    document.setRenderer(defRender);
                    document.add(new AreaBreak(AreaBreakType.LAST_PAGE));
                } else {
                    document.add(new Paragraph(header));
                    document.add(curChap);
                }
            }

            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            Paragraph fin = new Paragraph("Created with PolyGlot: Language Creation Tool Version " + core.getVersion() + "\n");
            fin.add(new Link("Get PolyGlot Here", PdfAction.createURI("https://github.com/DraqueT/PolyGlot/releases")).setUnderline());
            fin.add(new Text("\nPolyGlot Created By Draque Thompson"));
            fin.setFontSize(20);
            fin.setFontColor(Color.LIGHT_GRAY);
            document.showTextAligned(fin, 297.5f, 400, document.getPdfDocument()
                    .getNumberOfPages(), TextAlignment.CENTER, VerticalAlignment.MIDDLE, 0);
            fin = new Paragraph("iText7 used in the creation of this document. See Polyglot documentation for full license.");
            fin.setFontColor(Color.LIGHT_GRAY);
            fin.setFontSize(8);
            document.showTextAligned(fin, 297.5f, 100, document.getPdfDocument()
                    .getNumberOfPages(), TextAlignment.CENTER, VerticalAlignment.MIDDLE, 0);
        } catch (IOException e) {
            // always close document before returning
            document.close();
            throw new Exception(e.getMessage());
        }

        // Drop page number information into place
        if (printPageNumber && canvas != null) {
            canvas.beginText();
            canvas.setFontAndSize(PdfFontFactory.createFont(FontConstants.HELVETICA), 12);
            canvas.moveText(pageNumberX, pageNumberY);
            canvas.showText(Integer.toString(pdf.getNumberOfPages()));
            canvas.endText();
            canvas.release();
        }

        try {
            defRender.flush();
            dictRender.flush();
        } catch (Exception e) {
            // Do nothing. These throw null errors if they haven't been written
            // to, and there is no good way to test beforehand. IsFlushed() doesn't
            // work, and is on the iText team's bugfix list currently.
            // IOHandler.writeErrorLog(e);
        }

        document.close();

        // inform user of errors
        if (log.length() != 0) {
            InfoBox.warning("PDF Generation Errors", "Problems with PDF generation:\n"
                    + log, core.getRootWindow());
        }
    }

    /**
     * Gets map of types to their glosses (just type name if no gloss) and returns it. This prevents the necessity of
     * looking up each gloss name for every instance on a word.
     *
     * @return
     */
    private Map<Integer, String> getGlossKey() {
        Map<Integer, String> ret = new HashMap<>();

        core.getTypes().getNodes().forEach((curNode) -> {
            if (curNode.getGloss().length() == 0) {
                ret.put(curNode.getId(), curNode.getValue());
            } else {
                ret.put(curNode.getId(), curNode.getGloss());
                setPrintGlossKey(true);
            }
        });

        return ret;
    }

    /**
     * Builds dictionary chapter of Language Guide
     *
     * @return
     */
    private void buildConToLocalDictionary(String anchorPoint) throws IOException {
        String curLetter = "";
        Div curLetterSec = new Div();
        Iterator<ConWord> allWords = core.getWordCollection().getWordNodes().iterator();
        curLetterSec.add(new Paragraph(new Text("\n")));
        curLetterSec.setProperty(Property.DESTINATION, anchorPoint);

        while (allWords.hasNext()) {
            Paragraph dictEntry = new Paragraph();
            ConWord curWord = allWords.next();

            dictEntry.setMultipliedLeading(0.6f);

            // print large characters for alphabet sections
            if (!curLetter.equals(curWord.getValue().substring(0, 1))) {
                if (curLetter.length() != 0) {
                    document.add(curLetterSec);
                    document.add(new AreaBreak(AreaBreakType.NEXT_AREA));
                    curLetterSec = new Div();
                }
                curLetter = curWord.getValue().substring(0, 1);
                Text varChunk = new Text(curLetter);
                varChunk.setFont(conFont);
                varChunk.setFontSize(conFontSize + 16);
                dictEntry.add(varChunk);
                varChunk = new Text(" WORDS:");
                varChunk.setFontSize(defFontSize + 16);
                dictEntry.add(varChunk);
                dictEntry.add(new Text("\n"));
                dictEntry.add(new Text("\n"));
            }

            String wordVal = PGTUtil.stripRTL(curWord.getValue());
            if (core.getPropertiesManager().isEnforceRTL()) {
                // PDFs do not respect RTL character
                wordVal = new StringBuilder(wordVal).reverse().toString();
            }
            Text varChunk = new Text(wordVal);
            varChunk.setFont(conFont);
            varChunk.setFontSize(conFontSize + offsetSize);
            dictEntry.add(varChunk);

            varChunk = new Text(" - ");
            varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
            dictEntry.add(varChunk.setFontSize(defFontSize));

            // Add word type (if one exists)
            if (glossKey.containsKey(curWord.getWordTypeId())) {
                varChunk = new Text(glossKey.get(curWord.getWordTypeId()));
                varChunk.setFont(unicodeFont);
                dictEntry.add(varChunk.setFontSize(defFontSize));
                varChunk = new Text(" - ");
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            try {
                if (curWord.getPronunciation().length() != 0) {
                    varChunk = new Text("/" + curWord.getPronunciation() + "/");
                    varChunk.setFont(unicodeFont);
                    varChunk.setFontSize(defFontSize);
                    dictEntry.add(varChunk);
                    varChunk = new Text(" - ");
                    varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                    dictEntry.add(varChunk.setFontSize(defFontSize));
                }
            } catch (Exception e) {
                // do nothing. On Print, simply continue without printing this
                // word's pronunciation.
                // IOHandler.writeErrorLog(e);
            }

            // adds values 
            if (!curWord.getClassValues().isEmpty()) {
                String wordClasses = "";
                for (Entry<Integer, Integer> curEntry : curWord.getClassValues()) {
                    WordClass prop;
                    WordClassValue value;

                    try {
                        prop = (WordClass) core.getWordPropertiesCollection()
                                .getNodeById(curEntry.getKey());
                        value = prop.getValueById(curEntry.getValue());
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        log += "\nProblem printing classes for word (" + curWord.getValue()
                                + "): " + e.getLocalizedMessage();
                        continue;
                    }

                    if (wordClasses.length() != 0) {
                        wordClasses += ", ";
                    }

                    wordClasses += value.getValue();
                }
                varChunk = new Text(wordClasses);
                varChunk.setFont(unicodeFont);
                dictEntry.add(varChunk.setFontSize(defFontSize));

                varChunk = new Text(" - ");
                varChunk.setFont(unicodeFont);
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            if (!curWord.getClassTextValues().isEmpty()) {
                varChunk = null;

                for (Entry<Integer, String> curEntry : curWord.getClassTextValues()) {
                    if (varChunk != null) {
                        dictEntry.add(new Text(", "));
                    }
                    
                    WordClass prop = (WordClass) core.getWordPropertiesCollection().getNodeById(curEntry.getKey());
                    varChunk = new Text(prop.getValue());
                    varChunk.setFont(unicodeFontItalic);
                    dictEntry.add(varChunk);
                    varChunk = new Text(" : " + curEntry.getValue());
                    varChunk.setFont(unicodeFont);
                    dictEntry.add(varChunk);
                }

                dictEntry.add(new Text(" - "));
            }

            // write romanization value for word if active and word has one
            if (core.getRomManager().isEnabled()) {
                String romStr;

                try {
                    romStr = core.getRomManager().getPronunciation(curWord.getValue());
                } catch (Exception e) {
                    IOHandler.writeErrorLog(e);
                    romStr = "<ERROR>";
                }

                if (!romStr.isEmpty()) {
                    dictEntry.add(new Text("\nRoman: ").setFont(unicodeFont));
                    dictEntry.add(new Text(romStr + "\n").setFont(unicodeFontItalic));
                }
            }

            // print word etymology tree if appropriate
            // TODO: correct how this aligns images
            if (printWordEtymologies) {
                BufferedImage etymImage = (new PPanelDrawEtymology(core, curWord)).getPanelImage();

                // null image means there is no etymology for this word
                if (etymImage != null) {
                    //curLetterSec.add(dictEntry);
                    //dictEntry = new Paragraph();
                    dictEntry.add(new Text("\n").setFont(conFont));
                    dictEntry.add(getScaledImage(etymImage, true));
                }

            }

            List<Object> defList = WebInterface.getElementsHTMLBody(curWord.getDefinition(), core);
            if (!defList.isEmpty()) {
                dictEntry.add(new Text("\n"));
                for (Object o : defList) {
                    if (o instanceof String) {
                        // remove HTML from text and add newline (each text object in list is a line)
                        String cleanedText = StringEscapeUtils.unescapeHtml4((String) o) + "\n";
                        dictEntry.add(new Text(cleanedText).setFontSize(defFontSize).setFont(unicodeFont));
                    } else if (o instanceof BufferedImage) {
                        // TODO: Address here and below why this doesn't insert images properly. Need to re-architect... everything.
                        // I am mis-using paragraphs. 
                        // must convert buffered image to bytes because WHY DOES iTEXT 7 NOT DO THIS ITSELF.
                        byte[] bytes = IOHandler.getBufferedImageByteArray((BufferedImage) o);
                        Image pdfImage = new Image(ImageDataFactory.create(bytes));
                        dictEntry.add(pdfImage);
                        //curLetterSec.add(dictEntry);
                        //dictEntry = new Paragraph();
                        //dictEntry.add(new Text("\n").setFont(conFont));
                        //dictEntry.add(getScaledImage((BufferedImage) o, true));
                    } else {
                        // Do nothing: May be expanded for further logic later
                    }
                }
            }

            if (curWord.getLocalWord().length() != 0) {
                varChunk = new Text("Synonym(s): ");
                varChunk.setFont(unicodeFont);
                varChunk.setFontSize(defFontSize);
                dictEntry.add(varChunk);
                dictEntry.add(new Text(curWord.getLocalWord()).setFontSize(defFontSize));
                dictEntry.add(new Text("\n"));
            }

            // print conjugations if specified by user
            printConjugationsToEntry(dictEntry, curWord);

            dictEntry.setKeepTogether(true);
            curLetterSec.add(dictEntry);

            // add line break if more words
            if (allWords.hasNext()) {
                LineSeparator ls = new LineSeparator(new SolidLine(1f));
                ls.setWidthPercent(30);
                ls.setMarginTop(5);
                curLetterSec.add(ls);
            }
        }

        // add last letter section
        document.add(curLetterSec);
    }

    /**
     * Builds dictionary chapter of Language Guide (lookup by localword)
     *
     * @return
     */
    private void buildLocalToConDictionary(String anchorPoint) throws IOException { // rework with anchor
        String curLetter = "";
        Div curLetterSec = new Div();
        Iterator<ConWord> allWords = core.getWordCollection().getNodeIteratorLocalOrder();
        curLetterSec.add(new Paragraph(new Text("\n")));
        curLetterSec.setProperty(Property.DESTINATION, anchorPoint);

        while (allWords.hasNext()) {
            Paragraph dictEntry = new Paragraph();
            ConWord curWord = allWords.next();

            dictEntry.setMultipliedLeading(0.6f);

            if (curWord.getLocalWord().length() == 0) {
                continue;
            }

            // print large characters for alphabet sections
            if (!curLetter.toLowerCase().equals(curWord.getLocalWord()
                    .substring(0, 1).toLowerCase())) {
                if (curLetter.length() != 0) {
                    document.add(curLetterSec);
                    document.add(new AreaBreak(AreaBreakType.NEXT_AREA));
                    curLetterSec = new Div();
                }
                curLetter = curWord.getLocalWord().substring(0, 1);
                Text varChunk = new Text(curLetter.toUpperCase() + " WORDS:");
                varChunk.setFontSize(defFontSize + 16);
                dictEntry.add(varChunk);
                dictEntry.add(new Text("\n"));
                dictEntry.add(new Text("\n"));
            }

            Text varChunk;

            dictEntry.add(new Text(curWord.getLocalWord() + "\n\n")
                    .setFontSize(defFontSize + offsetSize));

            String wordVal = PGTUtil.stripRTL(curWord.getValue());
            if (core.getPropertiesManager().isEnforceRTL()) {
                // PDF Does not respect RTL characters...
                wordVal = new StringBuilder(wordVal).reverse().toString();
            }
            varChunk = new Text(wordVal);
            varChunk.setFont(conFont);
            varChunk.setFontSize(conFontSize - offsetSize);
            dictEntry.add(varChunk);

            varChunk = new Text(" - ");
            varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
            dictEntry.add(varChunk.setFontSize(defFontSize));

            // Add word type (if one exists)
            if (glossKey.containsKey(curWord.getWordTypeId())) {
                varChunk = new Text(glossKey.get(curWord.getWordTypeId()));
                varChunk.setFont(unicodeFont);
                dictEntry.add(varChunk.setFontSize(defFontSize));
                varChunk = new Text(" - ");
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            try {
                if (curWord.getPronunciation().length() != 0) {
                    varChunk = new Text("/" + curWord.getPronunciation() + "/");
                    varChunk.setFont(unicodeFont);
                    varChunk.setFontSize(defFontSize);
                    dictEntry.add(varChunk);
                    varChunk = new Text(" - ");
                    varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                    dictEntry.add(varChunk.setFontSize(defFontSize));
                }
            } catch (Exception e) {
                // do nothing. On Print, simply continue without printing this
                // word's pronunciation.
                // IOHandler.writeErrorLog(e);
            }

            // adds values 
            if (!curWord.getClassValues().isEmpty()) {
                String wordClasses = "";
                for (Entry<Integer, Integer> curEntry : curWord.getClassValues()) {
                    WordClass prop;
                    WordClassValue value;

                    try {
                        prop = (WordClass) core.getWordPropertiesCollection()
                                .getNodeById(curEntry.getKey());
                        value = prop.getValueById(curEntry.getValue());
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        log += "\nProblem printing classes for word: " + curWord.getValue();
                        continue;
                    }

                    if (wordClasses.length() != 0) {
                        wordClasses += ", ";
                    }

                    wordClasses += value.getValue();
                }
                varChunk = new Text(wordClasses);
                varChunk.setFont(unicodeFont);
                dictEntry.add(varChunk.setFontSize(defFontSize));

                varChunk = new Text(" - ");
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            if (!curWord.getClassTextValues().isEmpty()) {
                varChunk = null;

                for (Entry<Integer, String> curEntry : curWord.getClassTextValues()) {
                    if (varChunk != null) {
                        dictEntry.add(new Text(", "));
                    }
                    
                    WordClass prop = (WordClass) core.getWordPropertiesCollection().getNodeById(curEntry.getKey());
                    varChunk = new Text(prop.getValue());
                    varChunk.setFont(unicodeFontItalic);
                    dictEntry.add(varChunk);
                    varChunk = new Text(" : " + curEntry.getValue());
                    varChunk.setFont(unicodeFont);
                    dictEntry.add(varChunk);
                }
            }

            // write romanization value for word if active and word has one
            if (core.getRomManager().isEnabled()) {
                String romStr;

                try {
                    romStr = core.getRomManager().getPronunciation(curWord.getValue());
                } catch (Exception e) {
                    IOHandler.writeErrorLog(e);
                    romStr = "<ERROR>";
                }

                if (!romStr.isEmpty()) {
                    dictEntry.add(new Text("\nRoman: ").setFont(unicodeFont));
                    dictEntry.add(new Text(romStr).setFont(unicodeFontItalic));
                }
            }

            // print word etymology tree if appropriate
            // TODO: correct how this aligns images
            if (printWordEtymologies) {
                BufferedImage etymImage = (new PPanelDrawEtymology(core, curWord)).getPanelImage();

                // null image means there is no etymology for this word
                if (etymImage != null) {
                    //curLetterSec.add(dictEntry);
                    //dictEntry = new Paragraph();
                    dictEntry.add(new Text("\n").setFont(conFont));
                    dictEntry.add(getScaledImage(etymImage, true));
                }

            }

            List<Object> defList = WebInterface.getElementsHTMLBody(curWord.getDefinition(), core);
            if (!defList.isEmpty()) {
                dictEntry.add(new Text("\n"));
                for (Object o : defList) {
                    if (o instanceof String) {
                        // remove HTML from text and add newline (each text object in list is a line)
                        String cleanedText = StringEscapeUtils.unescapeHtml4((String) o) + "\n";
                        dictEntry.add(new Text(cleanedText).setFontSize(defFontSize).setFont(unicodeFont));
                    } else if (o instanceof BufferedImage) {
                        // must convert buffered image to bytes because WHY DOES iTEXT 7 NOT DO THIS ITSELF.
                        byte[] bytes = IOHandler.getBufferedImageByteArray((BufferedImage) o);
                        Image pdfImage = new Image(ImageDataFactory.create(bytes));
                        dictEntry.add(pdfImage);
                    } else {
                        // Do nothing: May be expanded for further logic later
                    }
                }
            }

            // print conjugations if specified by user
            printConjugationsToEntry(dictEntry, curWord);

            dictEntry.setKeepTogether(true);
            curLetterSec.add(dictEntry);

            // add line break if more words
            if (allWords.hasNext()) {
                LineSeparator ls = new LineSeparator(new SolidLine(1f));
                ls.setWidthPercent(30);
                ls.setMarginTop(5);
                curLetterSec.add(ls);
            }
        }

        // add last letter section
        document.add(curLetterSec);
    }

    private Div buildOrthography(String anchorPoint) throws IOException {
        Div ret = new Div();
        ret.add(new Paragraph(new Text("\n")));
        ret.setProperty(Property.DESTINATION, anchorPoint);
        boolean usesRegEx = false;

        Table table = new Table(2);
        table.addCell(new Paragraph("Character(s)").setFont(PdfFontFactory.createFont(FontConstants.COURIER_BOLD)));
        table.addCell(new Paragraph("Pronunciation").setFont(PdfFontFactory.createFont(FontConstants.COURIER_BOLD)));

        Iterator<PronunciationNode> orthIt = core.getPronunciationMgr().getPronunciations().iterator();

        while (orthIt.hasNext()) {
            PronunciationNode curNode = orthIt.next();

            String chars = curNode.getValue();
            Paragraph finChars = new Paragraph();

            for (char c : chars.toCharArray()) {
                if (c == '$' || c == '^') {
                    finChars.add(new Text(String.valueOf(c)));
                    usesRegEx = true;
                } else {
                    finChars.add(new Text(String.valueOf(c)).setFont(conFont).setFontSize(conFontSize));
                }
            }

            table.addCell(finChars).setTextAlignment(TextAlignment.CENTER);
            table.addCell(new Paragraph(curNode.getPronunciation())
                    .setFont(unicodeFont)).setTextAlignment(TextAlignment.CENTER);
        }

        if (usesRegEx) {
            ret.add(new Paragraph("Symbols $ and ^ indicate that elements of orthography must appear at the beginning or the end of a word.\n\n"));
        }

        ret.add(table);

        return ret;
    }

    private void printConjugationsToEntry(Paragraph dictEntry, ConWord curWord) {
        Text varChunk;
        
        if (printAllConjugations) {
            for (DeclensionPair curPair : core.getDeclensionManager().getAllCombinedIds(curWord.getWordTypeId())) {
                DeclensionNode curDeclension
                        = core.getDeclensionManager().getDeclensionByCombinedId(
                                curWord.getId(), curPair.combinedId);

                if (core.getDeclensionManager().isCombinedDeclSurpressed(curPair.combinedId, curWord.getId())) {
                    continue;
                }

                varChunk = new Text(curPair.label + ": ");
                varChunk.setFont(unicodeFont);
                varChunk.setFontSize(defFontSize - 1);
                dictEntry.add(varChunk);

                String declensionValue = "";

                // if set value exists, use this
                if (curDeclension != null) {
                    declensionValue = curDeclension.getValue();
                } else { // otherwise generate a value
                    try {
                        declensionValue = core.getDeclensionManager().declineWord(curWord,
                                curPair.combinedId,
                                curWord.getValue());
                    } catch (Exception e) {
                        IOHandler.writeErrorLog(e);
                        log += "Problem generating " + curPair.label
                                + " due to bad regex. Please check regex for word form.";
                    }
                }

                varChunk = new Text(declensionValue);
                varChunk.setFont(conFont);
                varChunk.setFontSize(conFontSize / 2);
                dictEntry.add(varChunk);
                dictEntry.add(new Text("\n"));
            }
        }
    }

    /**
     * Builds chapter on Grammar
     *
     * @param anchorPoint
     * @return
     */
    private Div buildGrammar(String anchorPoint) throws IOException {
        Div ret = new Div();
        ret.setProperty(Property.DESTINATION, anchorPoint);

        List<GrammarChapNode> gramList = core.getGrammarManager().getChapters();

        for (GrammarChapNode chap : gramList) {
            String chapName = chap.getName();
            ret.add(new Paragraph(chapName).setFont(PdfFontFactory
                    .createFont(FontConstants.COURIER_BOLD)).setFontSize(20));

            Div chapDiv = new Div();

            // hash codes should be unique per chapter, even if titles are identical
            chapDiv.setProperty(Property.DESTINATION, String.valueOf(chapDiv.hashCode()));
            for (int i = 0; i < chap.getChildCount(); i++) {
                Paragraph newSec = new Paragraph();
                newSec.setMarginLeft(30);
                GrammarSectionNode curSec = (GrammarSectionNode) chap.getChildAt(i);
                newSec.add(new Text(curSec.getName()).setFont(PdfFontFactory
                        .createFont(FontConstants.COURIER_OBLIQUE)).setFontSize(18));
                newSec.add(new Text("\n"));
                // populate text ensuring that conlang font is maintained where appropriate
                FormattedTextHelper.getSectionTextFontSpecifec(curSec.getSectionText(), core).forEach((entry) -> {
                    PFontInfo info = entry.getValue();
                    String text = PGTUtil.stripRTL(entry.getKey());

                    if (text.startsWith("<img src")) {
                        try {
                            text = text.replace("<img src=\"", "").replace("\">", "");
                            int imgId = Integer.parseInt(text);
                            ImageNode imageNode = (ImageNode) core.getImageCollection().getNodeById(imgId);
                            byte[] bytes = IOHandler.getBufferedImageByteArray(imageNode.getImage());
                            Image pdfImage = new Image(ImageDataFactory.create(bytes));
                            newSec.add(pdfImage);
                        } catch (IOException | NumberFormatException e) {
                            IOHandler.writeErrorLog(e);
                            log += "\nUnable to include images from grammar section: " + curSec.getName();
                        }
                    } else {
                        if (core.getPropertiesManager().isEnforceRTL()
                                && info.awtFont.equals(core.getPropertiesManager().getFontCon())) {
                            // PDF does not respect RTL characters
                            text = new StringBuilder(text).reverse().toString();
                        }

                        if (info.awtFont.equals(core.getPropertiesManager().getFontCon())) {

                            newSec.add(new Text(text).setFont(conFont)
                                    .setFontSize(conFontSize).setFontColor( // TODO: not setting size here. Later Rev (due to different size standards HTML vs Pt)
                                    FormattedTextHelper.swtColorToItextColor(info.awtColor)));
                        } else {
                            newSec.add(new Text(text).setFont(unicodeFont)
                                    .setFontColor( // TODO: not setting size here. Later Rev (due to different size standards HTML vs Pt)
                                            FormattedTextHelper.swtColorToItextColor(info.awtColor)));
                        }
                    }
                });
                newSec.setFixedLeading(21f);
                chapDiv.add(newSec);
            }

            ret.add(chapDiv);
            chapSects.add(new SecEntry(chapDiv.hashCode(), chapName));
        }

        return ret;
    }

    /**
     * Builds and returns gloss key for parts of speech
     *
     * @param anchor
     * @return
     */
    private Div buildGlossKey(String anchorPoint) throws IOException {
        Div ret = new Div();
        ret.setProperty(Property.DESTINATION, anchorPoint);
        Table table = new Table(2);
        table.addCell(new Paragraph("Part of Speech").setFont(PdfFontFactory.createFont(FontConstants.COURIER_BOLD)));
        table.addCell(new Paragraph("Gloss").setFont(PdfFontFactory.createFont(FontConstants.COURIER_BOLD)));

        core.getTypes().getNodes().forEach((curType) -> {
        //for (TypeNode curType : core.getTypes().getNodes()) {
            table.addCell(curType.getValue());
            table.addCell(curType.getGloss());
        });
        ret.add(table);

        return ret;
    }

    /**
     * Builds and returns a new front page for the guide
     *
     * @return front page chapter
     * @throws BadElementException if picture can't be opened
     * @throws IOException if picture can't be opened
     */
    private Paragraph buildFrontPage() throws IOException {
        Paragraph ret = new Paragraph();
        Text varChunk;
        ret.setTextAlignment(TextAlignment.CENTER);

        if (titleText.length() == 0) {
            if (core.getPropertiesManager().getLangName().length() == 0) {
                varChunk = new Text("LANGUAGE GUIDE");
            } else {
                varChunk = new Text(core.getPropertiesManager().getLangName());
            }
        } else {
            varChunk = new Text(titleText);
        }

        varChunk.setFont(unicodeFont);
        varChunk.setFontSize(36);
        ret.add(varChunk);

        if (subTitleText.length() != 0) {
            ret.add("\n");
            ret.add(new Text(subTitleText));
        }

        ret.add("\n");
        ret.add("\n");

        if (coverImagePath.length() != 0) {
            Image img = new Image(ImageDataFactory.create(coverImagePath));
            ret.add(img);
            ret.add("\n");
            ret.add("\n");
        }

        if (core.getPropertiesManager().getCopyrightAuthorInfo().length() != 0) {
            varChunk = new Text(core.getPropertiesManager().getCopyrightAuthorInfo());
            varChunk.setTextAlignment(TextAlignment.LEFT);
            varChunk.setFontSize(defFontSize - 2);
            ret.add(varChunk);
        }

        return ret;
    }

    /**
     * @param printLocalCon the printLocalCon to set
     */
    public void setPrintLocalCon(boolean printLocalCon) {
        this.printLocalCon = printLocalCon;
    }

    /**
     * @param printConLocal the printConLocal to set
     */
    public void setPrintConLocal(boolean printConLocal) {
        this.printConLocal = printConLocal;
    }

    /**
     * @param printOrtho the printOrtho to set
     */
    public void setPrintOrtho(boolean printOrtho) {
        this.printOrtho = printOrtho;
    }

    /**
     * @param printGrammar the printGrammar to set
     */
    public void setPrintGrammar(boolean printGrammar) {
        this.printGrammar = printGrammar;
    }

    /**
     * @param coverImagePath the coverImagePath to set
     */
    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    /**
     * @param forewardText the forewardText to set
     */
    public void setForewardText(String forewardText) {
        this.forewardText = forewardText;
    }

    /**
     * @param titleText the titleText to set
     */
    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    /**
     * @param subTitleText the subTitleText to set
     */
    public void setSubTitleText(String subTitleText) {
        this.subTitleText = subTitleText;
    }

    public void setPrintAllConjugations(boolean _printAllConjugations) {
        this.printAllConjugations = _printAllConjugations;
    }

    private ColumnDocumentRenderer getColumnRender() {
        float offSet = 36;
        float gutter = 23;
        float columnWidth = (PageSize.A4.getWidth() - offSet * 2) / 2 - gutter;
        float columnHeight = PageSize.A4.getHeight() - offSet * 2;
        final Rectangle[] columns = {
            new Rectangle(offSet, offSet, columnWidth, columnHeight),
            new Rectangle(
            offSet + columnWidth + gutter, offSet, columnWidth, columnHeight)};

        return new ColumnDocumentRenderer(document, false, columns);
    }

    private Div buildForward(String anchorPoint) {
        Div ret = new Div();
        ret.setProperty(Property.DESTINATION, anchorPoint);

        ret.add(new Paragraph(new Text(forewardText)).setPaddingLeft(15).setPaddingRight(15));

        return ret;
    }

    /**
     * @param printPageNumber the printPageNumber to set
     */
    public void setPrintPageNumber(boolean printPageNumber) {
        this.printPageNumber = printPageNumber;
    }

    /**
     * @param printGlossKey the printGlossKey to set
     */
    public void setPrintGlossKey(boolean printGlossKey) {
        this.printGlossKey = printGlossKey;
    }

    /**
     * This is code that allows for easily adding page numbers.
     */
    public class HeaderHandler implements IEventHandler {

        protected String language;

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfPage page = docEvent.getPage();
            int pageNum = docEvent.getDocument().getPageNumber(page);
            PdfCanvas canvas = new PdfCanvas(page);
            canvas.beginText();
            canvas.setFontAndSize(unicodeFont, 12);
            canvas.moveText(34, pageNumberY);
            canvas.showText(language);
            canvas.moveText(450, 0);
            canvas.showText(String.format("Page %d of", pageNum));
            canvas.endText();
            canvas.stroke();
            canvas.addXObject(template, 0, 0);
            canvas.release();
        }

        public void setHeader(String _language) {
            this.language = _language;
        }
    }

    static class SecEntry implements Entry {

        final int key;
        String title;

        public SecEntry(int _key, String _title) {
            key = _key;
            title = _title;
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return title;
        }

        @Override
        public Object setValue(Object value) {
            title = (String) value;
            return title;
        }
    }

    /**
     * @param printWordEtymologies the printWordEtymologies to set
     */
    public void setPrintWordEtymologies(boolean printWordEtymologies) {
        this.printWordEtymologies = printWordEtymologies;
    }

    /**
     * Takes a buffered image and returns an Image scaled to the appropriate size. Scaled for full screen if columSize
     * is set to false, and to fit into a column if set to true. If an image is already small enough, its size will not
     * be scaled at all
     *
     * @param inputImage
     * @param columnSize
     * @return
     */
    private Image getScaledImage(BufferedImage inputImage, boolean columnSize) throws IOException {
        Image ret = new Image(ImageDataFactory.create(
                IOHandler.getBufferedImageByteArray(inputImage)));
        float docWidth = PageSize.A4.getWidth();
        float imageWidth = inputImage.getWidth();
        float imageHeight = inputImage.getHeight();

        if ((columnSize && imageWidth > (docWidth / 2)) || imageWidth > docWidth / 2) {
            float scaler = ((docWidth - document.getLeftMargin()
                    - document.getRightMargin()) / imageWidth);

            // slightly less than 1/2 due to buffer space between
            if (columnSize) {
                scaler /= 2.2;
            }

            ret = ret.scaleToFit(scaler * imageWidth, scaler * imageHeight);
        }

        return ret;
    }
}
