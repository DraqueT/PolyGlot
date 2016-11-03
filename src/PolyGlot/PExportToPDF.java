/*
 * Copyright (c) 2016, draque.thompson
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
import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.Nodes.PEntry;
import PolyGlot.Nodes.PronunciationNode;
import PolyGlot.Nodes.WordPropValueNode;
import PolyGlot.Nodes.WordProperty;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final List<Entry<Div, String>> chapList = new ArrayList<>();
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
    private byte[] conFontFile;
    private final byte[] unicodeFontFile;
    private final PdfFont conFont;
    private final PdfFont unicodeFont;
    private final int conFontSize;
    private boolean printLocalCon = false;
    private boolean printConLocal = false;
    private boolean printOrtho = false;
    private boolean printGrammar = false;
    private boolean printGlossKey = false;
    private boolean printPageNumber = false;
    private String coverImagePath = "";
    private String forewardText = "";
    private String titleText = "";
    private String subTitleText = "";

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
        unicodeFont = PdfFontFactory.createFont(unicodeFontFile, PdfEncodings.IDENTITY_H, true);
        unicodeFont.setSubset(true);
        
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
            if (!forewardText.equals("")) {
                chapTitles.put(FOREWORD, "Author Foreword");
                chapList.add(new PEntry(buildForward(FOREWORD), FOREWORD));
            }
            if (printOrtho) {
                chapTitles.put(ORTHOGRAPHY, "Orthography");
                chapList.add(new PEntry(buildOrthography(ORTHOGRAPHY), ORTHOGRAPHY));
            }
            if (printGlossKey) {
                chapTitles.put(GLOSSKEY, "Gloss Key");
                chapList.add(new PEntry(buildGlossKey(GLOSSKEY), GLOSSKEY));
            }

            if (printConLocal) {
                String title = "Dictionary: ";
                title += core.conLabel();

                title += " to ";

                title += core.localLabel();

                chapTitles.put(DICTCON2LOC, title);
                chapList.add(new PEntry(null, DICTCON2LOC));
            }
            if (printLocalCon) {
                String title = "Dictionary: ";
                title += core.localLabel();
                title += " to ";
                title += core.conLabel();

                chapTitles.put(DICTLOC2CON, title);
                chapList.add(new PEntry(null, DICTLOC2CON));
            }

            if (printGrammar) {
                chapTitles.put(GRAMMAR, "Grammar");
                chapList.add(new PEntry(buildGrammar(GRAMMAR), GRAMMAR));
            }

            // build table of contents
            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            document.add(new Paragraph(
                    new Text("Table of Contents")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(30)));
            Div ToC = new Div();
            for (Entry curChap : chapList) {
                Link link = new Link(chapTitles.get((String) curChap.getValue()),
                        PdfAction.createGoTo((String) curChap.getValue()));
                ToC.add(new Paragraph(link).add("\n").add(" "));

                // create subheadings for grammar chapter
                if (curChap.getValue().equals(GRAMMAR)) {
                    Paragraph subSec = new Paragraph();
                    subSec.setMarginLeft(20f);
                    for (SecEntry chapSect : chapSects) {
                        Link secLink = new Link((String) chapSect.getValue(),
                                PdfAction.createGoTo(String.valueOf((int) chapSect.getKey())));
                        secLink.setFontSize(9);
                        subSec.add(secLink).add("\n");
                    }
                    ToC.add(subSec);
                }
            }
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
        } catch (Exception e) {
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
            // work.
        }

        document.close();
    }

    /**
     * Gets map of types to their glosses (just type name if no gloss) and
     * returns it. This prevents the necessity of looking up each gloss name for
     * every instance on a word.
     *
     * @return
     */
    private Map getGlossKey() {
        Map<Integer, String> ret = new HashMap<>();

        Iterator<TypeNode> typeIt = core.getTypes().getNodeIterator();

        while (typeIt.hasNext()) {
            TypeNode curNode = typeIt.next();

            if (curNode.getGloss().equals("")) {
                ret.put(curNode.getId(), curNode.getValue());
            } else {
                ret.put(curNode.getId(), curNode.getGloss());
                setPrintGlossKey(true);
            }
        }

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

            // print large characters for alphabet sections
            if (!curLetter.equals(curWord.getValue().substring(0, 1))) {
                if (!curLetter.equals("")) {
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
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_ITALIC));
                dictEntry.add(varChunk.setFontSize(defFontSize));
                varChunk = new Text(" - ");
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            if (!curWord.getPronunciation().equals("")) {
                varChunk = new Text("/" + curWord.getPronunciation() + "/");
                varChunk.setFont(unicodeFont);
                varChunk.setFontSize(defFontSize);
                dictEntry.add(varChunk);
                varChunk = new Text(" - ");
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            // adds values 
            if (!curWord.getClassValues().isEmpty()) {
                String wordClasses = "";
                for (Entry<Integer, Integer> curEntry : curWord.getClassValues()) {
                    WordProperty prop;
                    WordPropValueNode value;

                    try {
                        prop = (WordProperty) core.getWordPropertiesCollection()
                                .getNodeById(curEntry.getKey());
                        value = prop.getValueById(curEntry.getValue());
                    } catch (Exception e) {
                        // TODO: put logging in here or something... 
                        continue;
                    }

                    if (!wordClasses.equals("")) {
                        wordClasses += ", ";
                    }

                    wordClasses += value.getValue();
                }
                varChunk = new Text(wordClasses);
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_ITALIC));
                dictEntry.add(varChunk.setFontSize(defFontSize));

                varChunk = new Text(" - ");
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            String curDef = WebInterface.getTextFromHtml(curWord.getDefinition());
            if (!curDef.equals("")) {
                dictEntry.add(new Text("\n" + curDef).setFontSize(defFontSize));
                dictEntry.add(new Text("\n"));
            }

            if (!curWord.getLocalWord().equals("")) {
                varChunk = new Text("Synonym(s): ");
                varChunk.setFont(unicodeFont);
                varChunk.setFontSize(defFontSize);
                dictEntry.add(varChunk);
                dictEntry.add(new Text(curWord.getLocalWord()).setFontSize(defFontSize));
                dictEntry.add(new Text("\n"));
            }

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

            if (curWord.getLocalWord().equals("")) {
                continue;
            }

            // print large characters for alphabet sections
            if (!curLetter.toLowerCase().equals(curWord.getLocalWord()
                    .substring(0, 1).toLowerCase())) {
                if (!curLetter.equals("")) {
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

            dictEntry.add(new Text(curWord.getLocalWord() + "\n")
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
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_ITALIC));
                dictEntry.add(varChunk.setFontSize(defFontSize));
                varChunk = new Text(" - ");
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            if (!curWord.getPronunciation().equals("")) {
                varChunk = new Text("/" + curWord.getPronunciation() + "/");
                varChunk.setFont(unicodeFont);
                varChunk.setFontSize(defFontSize);
                dictEntry.add(varChunk);
                varChunk = new Text(" - ");
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            // adds values 
            if (!curWord.getClassValues().isEmpty()) {
                String wordClasses = "";
                for (Entry<Integer, Integer> curEntry : curWord.getClassValues()) {
                    WordProperty prop;
                    WordPropValueNode value;

                    try {
                        prop = (WordProperty) core.getWordPropertiesCollection()
                                .getNodeById(curEntry.getKey());
                        value = prop.getValueById(curEntry.getValue());
                    } catch (Exception e) {
                        // TODO: put logging in here or something... 
                        continue;
                    }

                    if (!wordClasses.equals("")) {
                        wordClasses += ", ";
                    }

                    wordClasses += value.getValue();
                }
                varChunk = new Text(wordClasses);
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_ITALIC));
                dictEntry.add(varChunk.setFontSize(defFontSize));

                varChunk = new Text(" - ");
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            String curDef = WebInterface.getTextFromHtml(curWord.getDefinition());
            if (!curDef.equals("")) {
                dictEntry.add(new Text("\n" 
                        + curDef).setFontSize(defFontSize));
                dictEntry.add(new Text("\n"));
            }

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

        Iterator<PronunciationNode> orthIt = core.getPronunciationMgr().getPronunciations();

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
                for (Entry<String, PFontInfo> e : FormattedTextHelper.getSectionTextFontSpecifec(curSec.getSectionText(), core)) {
                    PFontInfo info = e.getValue();
                    String text = PGTUtil.stripRTL(e.getKey());
                    if (core.getPropertiesManager().isEnforceRTL()
                            && info.awtFont.equals(core.getPropertiesManager().getFontCon())) {
                        // PDF does not respect RTL characters
                        text = new StringBuilder(text).reverse().toString();
                    }
                    if (info.awtFont.equals(core.getPropertiesManager().getFontCon())) {

                        newSec.add(new Text(text).setFont(conFont) // TODO: Maybe actually build font here if I give more freedom in the grammar section
                                .setFontSize(conFontSize).setFontColor( // TODO: not setting size here. Later Rev (due to different size standards HTML vs Pt)
                                FormattedTextHelper.swtColorToItextColor(info.awtColor)));
                    } else {
                        newSec.add(new Text(text).setFont(unicodeFont) // TODO: Maybe actually build font here if I give more freedom in the grammar section
                                .setFontColor( // TODO: not setting size here. Later Rev (due to different size standards HTML vs Pt)
                                        FormattedTextHelper.swtColorToItextColor(info.awtColor)));
                    }
                }
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

        Iterator<TypeNode> glossIt = core.getTypes().getNodeIterator();

        while (glossIt.hasNext()) {
            TypeNode curType = glossIt.next();

            table.addCell(curType.getValue());
            table.addCell(curType.getGloss());
        }
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

        if (titleText.equals("")) {
            if (core.getPropertiesManager().getLangName().equals("")) {
                varChunk = new Text("LANGUAGE GUIDE");
            } else {
                varChunk = new Text(core.getPropertiesManager().getLangName());
            }
        } else {
            varChunk = new Text(titleText);
        }

        varChunk.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD));
        varChunk.setFontSize(36);
        ret.add(varChunk);

        if (!subTitleText.equals("")) {
            ret.add("\n");
            ret.add(new Text(subTitleText));
        }

        ret.add("\n");
        ret.add("\n");

        if (!coverImagePath.equals("")) {
            Image img = new Image(ImageDataFactory.create(coverImagePath));
            ret.add(img);
            ret.add("\n");
            ret.add("\n");
        }

        if (!core.getPropertiesManager().getCopyrightAuthorInfo().equals("")) {
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
            try {
                canvas.setFontAndSize(PdfFontFactory.createFont(FontConstants.HELVETICA), 12);
            } catch (IOException e) {
                // can't throw an exception here... built in/guaranteed font, though.
            }
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
}
