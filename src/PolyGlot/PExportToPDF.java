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

import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.TypeNode;
import PolyGlot.Nodes.PEntry;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
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
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.Property;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.renderer.DocumentRenderer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

// TODO: add iText licensing to output at bottom of dictionaries
/**
 * Given a core dictionary, this class will print it to a PDF file.
 *
 * @author draque.thompson
 */
public class PExportToPDF {

    private final String DICTCON2LOC = "DICTCON2LOC";
    private final String DICTLOC2CON = "DICTLOC2CON";
    private final String FOREWORD = "FOREWORD";
    private final Map<String, String> glossKey;
    private final List<Entry<Div, String>> chapList = new ArrayList<>();
    private final Map<String, String> chapTitles = new HashMap<>();
    private final int offsetSize = 1; // TODO: Move this to the utils class
    private final int defFontSize = 8;
    private final int pageNumberY = 10;
    private final int pageNumberX = 550;
    protected PdfFormXObject template;
    private final DictCore core;
    private final String targetFile;
    private Document document;
    private final byte[] conFontFile;
    private final byte[] unicodeFontFile;
    private final PdfFont conFont;
    private final PdfFont unicodeFont;
    private final int conFontSize;
    private boolean printLocalCon = false;
    private boolean printConLocal = false;
    private boolean printOrtho = false;
    private boolean printGrammar = false;
    private boolean printGlossKey = false;
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

        // Document Exception if unable to read font, IO exception if problem loading font binary
        if (conFontFile == null) {
            // conFont may never be null
            conFont = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);
        } else {
            conFont = PdfFontFactory.createFont(conFontFile, PdfEncodings.IDENTITY_H, true);
        }

        unicodeFont = PdfFontFactory.createFont(unicodeFontFile, PdfEncodings.IDENTITY_H, true);
        unicodeFont.setSubset(true);
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
        DocumentRenderer defRender = new DocumentRenderer(document);
        document.setRenderer(defRender);
        ColumnDocumentRenderer dictRender = getColumnRender();

        // set up page numbers on document
        template = new PdfFormXObject(new Rectangle(pageNumberX, pageNumberY, 30, 30));
        PdfCanvas canvas = new PdfCanvas(template, pdf);
        HeaderHandler headerHandler = new HeaderHandler();
        headerHandler.setHeader(core.getPropertiesManager().getLangName());
        pdf.addEventHandler(PdfDocumentEvent.START_PAGE, headerHandler);

        try {
            // TODO: Move all constants to PGTUtil
            // TODO: Convert all chapters to Divs rather than Paragraphs
            // TODO: add option for print page number or not
            // TODO: Figure out why I can't hop back and forth between multiple renderers

            // front page is always built/added before chapter guide
            document.add(buildFrontPage()); // TODO: author info/copyright information and maybe image?
            if (!forewardText.equals("")) {
                chapTitles.put(FOREWORD, "Author Foreword");
                chapList.add(new PEntry(buildForward(FOREWORD), FOREWORD)); // TODO: reenable once this is a Div type rather than a Paragraph
            }
            if (printOrtho) {
                //chapList.add(new PEntry(buildOrthography(), ORTHOGRAPHY)); // TODO: this
            }
            if (printGlossKey) {
                //chapList.add(new PEntry(buildGlossKey(), GLOSSKEY)); // TODO: this
            }

            // parts of speech always printed
            if (printGrammar) {
                //chapList.add(new PEntry buildGrammar(), GRAMMAR));
            }

            //chapList.add(new PEntry(types = buildTypes(), TYPES); // TODO: this
            if (printConLocal) {
                String title = "Dictionary ";
                if (core.getPropertiesManager().getLocalLangName().equals("")) {
                    title += "Local ";
                } else {
                    title += core.getPropertiesManager().getLocalLangName() + " ";
                }
                
                title += "to ";
                
                if (core.getPropertiesManager().getLangName().equals("")) {
                    title += "Conlang";
                } else {
                    title += core.getPropertiesManager().getLangName();
                }
                chapTitles.put(DICTCON2LOC, title);
                chapList.add(new PEntry(buildConToLocalDictionary(DICTCON2LOC), DICTCON2LOC));
            }
            if (printLocalCon) {
                String title = "Dictionary ";
                if (core.getPropertiesManager().getLangName().equals("")) {
                    title += "Conlang ";
                } else {
                    title += core.getPropertiesManager().getLangName() + " ";
                }
                
                title += "to ";
                
                if (core.getPropertiesManager().getLocalLangName().equals("")) {
                    title += "local";
                } else {
                    title += core.getPropertiesManager().getLocalLangName();
                }
                chapTitles.put(DICTLOC2CON, title); // TODO: Add "<local> to <con>" or something here
                chapList.add(new PEntry(buildLocalToConDictionary(DICTLOC2CON), DICTLOC2CON));
            }

            // build table of contents
            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            document.add(new Paragraph(
                    new Text("Table of Contents")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(30))); // TODO: constants
            Paragraph ToC = new Paragraph();
            for (Entry curChap : chapList) {
                if (curChap.getKey() == null) {
                    continue;
                }
                Link link = new Link(chapTitles.get((String) curChap.getValue()), PdfAction.createGoTo((String) curChap.getValue()));
                ToC.add(link);
                ToC.add("\n");
                ToC.add(" ");
            }
            ToC.setBorder(new SolidBorder(0.5f)).setPadding(10);

            document.add(ToC);

            // add chapters (must be done in separate loop to maintain proper spacing in PDF)
            for (Entry curEntry : chapList) {
                Div curChap = (Div) curEntry.getKey();
                Text header = new Text((String) chapTitles.get((String) curEntry.getValue()));
                header.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD)); // TODO: Clean this up & correct size
                header.setTextAlignment(TextAlignment.CENTER);
                document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                document.add(new Paragraph(header));
                if (curEntry.getValue().equals(DICTCON2LOC) || curEntry.getValue().equals(DICTLOC2CON)) {
                    // TODO: Figure out why I can't switch back between multiple renderers
                    document.setRenderer(dictRender);
                    document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                    document.add(curChap);
                    document.setRenderer(defRender);
                } else {
                    document.add(curChap);
                }
            }
        } catch (Exception e) {
            // always close document before returning
            document.close();
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        // Drop page number information into place
        canvas.beginText();
        try {
            canvas.setFontAndSize(PdfFontFactory.createFont(FontConstants.HELVETICA), 12);
        } catch (IOException e) {
            e.printStackTrace(); // TODO: Remove, obviously
        }
        canvas.moveText(pageNumberX, pageNumberY);
        canvas.showText(Integer.toString(pdf.getNumberOfPages()));
        canvas.endText();
        canvas.release();

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
        Map<String, String> ret = new HashMap<>();

        Iterator<TypeNode> typeIt = core.getTypes().getNodeIterator();

        while (typeIt.hasNext()) {
            TypeNode curNode = typeIt.next();

            if (curNode.getGloss().equals("")) {
                ret.put(curNode.getValue(), curNode.getValue());
            } else {
                ret.put(curNode.getValue(), curNode.getGloss());
                printGlossKey = true;
            }
        }

        return ret;
    }

    /**
     * Builds dictionary chapter of Language Guide
     *
     * @return
     */
    private Div buildConToLocalDictionary(String anchorPoint) throws IOException {
        String curLetter = "";
        Div ret = new Div();
        Iterator<ConWord> allWords = core.getWordCollection().getNodeIterator();
        ret.add(new Paragraph(new Text("\n")));
        ret.setProperty(Property.DESTINATION, anchorPoint);

        while (allWords.hasNext()) {
            Paragraph dictEntry = new Paragraph();
            ConWord curWord = allWords.next();

            // print large characters for alphabet sections
            if (!curLetter.equals(curWord.getValue().substring(0, 1))) {
                if (!curLetter.equals("")) {
                    dictEntry.add(new Text("\n"));
                    dictEntry.add(new Text("\n"));
                }
                curLetter = curWord.getValue().substring(0, 1);
                Text varChunk = new Text(curLetter);
                varChunk.setFont(conFont);
                varChunk.setFontSize(conFontSize + 16); // TODO: constant
                dictEntry.add(varChunk);
                dictEntry.add(new Text(" WORDS:").setFontSize(defFontSize + 16)); // TODO: Constant
                dictEntry.add(new Text("\n"));
                dictEntry.add(new Text("\n"));
            }

            Text varChunk = new Text(curWord.getValue());
            varChunk.setFont(conFont);
            varChunk.setFontSize(conFontSize + offsetSize);

            dictEntry.add(varChunk);
            dictEntry.add(new Text("\n").setFontSize(1));

            // Add word type (if one exists)
            if (glossKey.containsKey(curWord.getWordType())) {
                varChunk = new Text(glossKey.get(curWord.getWordType()));
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_ITALIC));
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            // only drops separator if word has both a type and a pronunciation
            if (!curWord.getWordType().equals("") && !curWord.getPronunciation().equals("")) {
                dictEntry.add(new Text(" | ").setFontSize(defFontSize));
            }

            if (!curWord.getPronunciation().equals("")) {
                varChunk = new Text(curWord.getPronunciation());
                varChunk.setFont(unicodeFont);
                varChunk.setFontSize(defFontSize);
                dictEntry.add(varChunk);
            }

            // add newline if word has type or pronunciation
            if (!curWord.getWordType().equals("") || !curWord.getPronunciation().equals("")) {
                dictEntry.add(new Text("\n").setFontSize(1));
            }

            if (!curWord.getLocalWord().equals("")) {
                varChunk = new Text("Synonym(s): ");
                varChunk.setFont(unicodeFont);
                varChunk.setFontSize(defFontSize);
                dictEntry.add(varChunk);
                dictEntry.add(new Text(curWord.getLocalWord()).setFontSize(defFontSize));
                dictEntry.add(new Text("\n"));
            }

            if (!curWord.getDefinition().equals("")) {
                varChunk = new Text("Definition - ");
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                dictEntry.add(varChunk.setFontSize(defFontSize));
                dictEntry.add(new Text("\n" + curWord.getDefinition()).setFontSize(defFontSize));
                dictEntry.add(new Text("\n"));
            }

            dictEntry.setKeepTogether(true);
            ret.add(dictEntry);

            // add line break if more words
            if (allWords.hasNext()) {
                LineSeparator ls = new LineSeparator(new SolidLine(1f));
                ls.setWidthPercent(30);
                ls.setMarginTop(5);
                ret.add(ls);
            }
        }

        return ret;
    }

    /**
     * Builds dictionary chapter of Language Guide (lookup by localword)
     *
     * @return
     */
    private Div buildLocalToConDictionary(String anchorPoint) throws IOException { // rework with anchor
        String curLetter = "";
        Iterator<ConWord> allWords = core.getWordCollection().getNodeIteratorLocalOrder();
        Div ret = new Div();
        ret.add(new Paragraph(new Text("\n")));
        ret.setProperty(Property.DESTINATION, anchorPoint);

        while (allWords.hasNext()) {
            Paragraph dictEntry = new Paragraph();
            ConWord curWord = allWords.next();

            // Skip any entry without local word synonyms
            if (curWord.getLocalWord().trim().equals("")) {
                continue;
            }

            // print large characters for alphabet sections
            if (!curLetter.toLowerCase().equals(curWord.getLocalWord().toLowerCase().substring(0, 1))) {
                if (!curLetter.equals("")) {
                    dictEntry.add(new Text("\n"));
                    dictEntry.add(new Text("\n"));
                }
                curLetter = curWord.getLocalWord().substring(0, 1);
                Text varChunk = new Text(curLetter.toUpperCase());
                varChunk.setFontSize(defFontSize + 16); // TODO: constant
                dictEntry.add(varChunk);
                dictEntry.add(new Text(" WORDS:").setFontSize(defFontSize + 16)); // TODO: Constant
                dictEntry.add(new Text("\n"));
                dictEntry.add(new Text("\n"));
            }

            Text varChunk = new Text(curWord.getLocalWord());
            varChunk.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD));
            varChunk.setFontSize(defFontSize + offsetSize);
            dictEntry.add(varChunk);

            // Make certain the newline is large enough to account for potentially larger conlang fonts
            varChunk = new Text("\n");
            varChunk.setFont(conFont);
            varChunk.setFontSize(0);

            dictEntry.add(varChunk);

            dictEntry.add(new Text("Conword: ").setFontSize(defFontSize));
            varChunk = new Text(curWord.getValue());
            varChunk.setFont(conFont);
            varChunk.setFontSize(conFontSize);
            dictEntry.add(varChunk.setFontSize(defFontSize));

            // add newline only if word has type or pronunciation for proper line
            if (!curWord.getWordType().equals("") || !curWord.getPronunciation().equals("")) {
                dictEntry.add(new Text("\n").setFont(conFont).setFontSize(0));
            }

            // Add word type (if one exists)
            if (glossKey.containsKey(curWord.getWordType())) {
                varChunk = new Text(glossKey.get(curWord.getWordType()));
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_ITALIC));
                dictEntry.add(varChunk.setFontSize(defFontSize));
            }

            // only drops separator if word has both a type and a pronunciation
            if (!curWord.getWordType().equals("") && !curWord.getPronunciation().equals("")) {
                dictEntry.add(new Text(" | ").setFontSize(defFontSize));
            }

            if (!curWord.getPronunciation().equals("")) {
                varChunk = new Text(curWord.getPronunciation());
                varChunk.setFont(unicodeFont);
                varChunk.setFontSize(defFontSize);
                dictEntry.add(varChunk);
            }

            dictEntry.add("\n");

            if (!curWord.getDefinition().equals("")) {
                varChunk = new Text("Definition - ");
                varChunk.setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD));
                dictEntry.add(varChunk.setFontSize(defFontSize));
                dictEntry.add(new Text("\n" + curWord.getDefinition()).setFontSize(defFontSize));
                dictEntry.add("\n");
            }

            dictEntry.setKeepTogether(true);
            ret.add(dictEntry);

            // add line break if more words
            if (allWords.hasNext()) {
                LineSeparator ls = new LineSeparator(new SolidLine(1f));
                ls.setWidthPercent(30);
                ls.setMarginTop(5);
                ret.add(ls);
            }
        }

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

        // TODO: remove manual font creation here
        varChunk.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD));
        varChunk.setFontSize(36); // TODO: Address constants
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

        // TODO: make certain this is left justified and with a large margain to the right and left
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
        Rectangle[] columns = {
            new Rectangle(offSet, offSet, columnWidth, columnHeight),
            new Rectangle(
            offSet + columnWidth + gutter, offSet, columnWidth, columnHeight)};

        return new ColumnDocumentRenderer(document, columns);
    }

    private Div buildForward(String anchorPoint) {
        Div ret = new Div();
        ret.setProperty(Property.DESTINATION, anchorPoint);

        ret.add(new Paragraph(new Text(forewardText)).setPaddingLeft(15).setPaddingRight(15));

        return ret;
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
                e.printStackTrace(); // TODO: Remove, obviously
            }
            canvas.moveText(34, pageNumberY); // TODO: CONSTANT
            canvas.showText(language);
            canvas.moveText(450, 0); // TODO: CONSTANT
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
}
