/*
 * Copyright (c) 2023, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
 * See LICENSE.TXT included with this code to read the full license agreement.

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
package org.darisadesigns.polyglotlina.Screens;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.HTMLEditorSkin;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PFrame;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.ManagersCollections.TranslationManager;
import org.darisadesigns.polyglotlina.ManagersCollections.TranslationManager.ResultColumn;
import org.darisadesigns.polyglotlina.ManagersCollections.TranslationManager.SourceLang;
import static org.darisadesigns.polyglotlina.ManagersCollections.TranslationManager.SourceLang.CONLANG;
import static org.darisadesigns.polyglotlina.ManagersCollections.TranslationManager.SourceLang.LOCALLANG;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 *
 * @author edga_
 */
public class ScrTranslate extends PFrame {
    
    private HTMLEditor editorSource;
    private HTMLEditor editorTranslation;
    private WebView viewTranslation;
    private StackPane stackPane;
    private SourceLang sourceLang = SourceLang.LOCALLANG;

    /**
     * Creates new form ScrTranslate
     * @param _core
     */
    public ScrTranslate(DictCore _core) {
        super(_core);
        initComponents();
        initJavaFXComponents();
        // Hack to set divider at the middle of the screen
        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                ScrTranslate.this.jSplitPane2.setDividerLocation(0.5);
            }
            @Override
            public void componentMoved(ComponentEvent e) {}
            @Override
            public void componentShown(ComponentEvent e) {}
            @Override
            public void componentHidden(ComponentEvent e) {}
        });
        
        if (_core.getTranslationManager().isInitialized()) {
            try {
                this.core.getTranslationManager().openConnection();
            }
            catch (IOException | SQLException ex) {
                Logger.getLogger(ScrTranslate.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.jPanel1.setVisible(false);
        }
    }
    
    /**
     * Initialized the JavaFX components and includes them in the Swing containers.
     */
    private void initJavaFXComponents() {
        Platform.runLater(() -> {
            this.editorSource = new HTMLEditor();
            this.editorTranslation = new HTMLEditor();
            this.stackPane = new StackPane();
            this.viewTranslation = new WebView();
            this.hideEditorControls(this.editorSource);
            this.hideEditorControls(this.editorTranslation);
            
            this.includeNodeInPanel(this.editorSource, this.jPanel6);
            
            JFXPanel webStackPanel = this.createJFXPanel(this.stackPane);
            this.jPanel7.setLayout(new GridLayout());
            this.jPanel7.add(webStackPanel, BorderLayout.CENTER);
            
            this.stackPane.getChildren().addAll(this.editorTranslation, this.viewTranslation);
            
            this.editorSource.addEventFilter(
                    KeyEvent.KEY_PRESSED,
                    e -> {
                        if (e.isControlDown() && e.getCode() == KeyCode.V) {
                            // TODO: decide if we want to modify the clipboard
                            // setClipboardToPlainText();
                            // At the moment, prevent pasting
                            e.consume();
                        }
                        else if (e.getCode() == KeyCode.ENTER) {
                            ScrTranslate.this.processTranslationRequest();
                            e.consume();
                        }
                    });
            
            WebView webView = (WebView) this.editorSource.lookup(".web-view");
            webView.setContextMenuEnabled(false);
            
            setInitialContent(webView.getEngine());
            setInitialContent(this.viewTranslation.getEngine());
            // setInitialContent(this.editorTranslation, false);
            
            pack();
        });
    }

    /**
     * Updates the clipboard to only hold plain text.  
     * This modifies the content of the clipboard which is not so good for UX.
     */
    private void setClipboardToPlainText() {
        Clipboard clipboard = Clipboard.getSystemClipboard();

        String plainText = clipboard.getString();
        ClipboardContent content = new ClipboardContent();
        content.putString(plainText);

        clipboard.setContent(content);
    }
    
    /**
     * Hides tool bars used to control the editor.
     * 
     * @param editor HTMLEditor to update.
     */
    private void hideEditorControls(HTMLEditor editor) {
        Node[] nodes = editor.lookupAll(".tool-bar").toArray(Node[]::new);
        for(Node node : nodes)
        {
            node.setVisible(false);
            node.setManaged(false);
        }
        editor.setVisible(true);
    }
    
    /**
     * Creates a new JFXPanel that can be added to Swing containers.
     * 
     * @param node JavaFX node to include in the panel.
     * @return 
     */
    private JFXPanel createJFXPanel(Parent node) {
        Scene scene = new Scene(node);
        JFXPanel fxPanel = new JFXPanel();
        fxPanel.setScene(scene);
        
        return fxPanel;
    }
    
    /**
     * Include a JavaFX node inside a Swing container.
     * 
     * @param node JavaFX node to include.
     * @param container Target Swing container.
     * @return JFXPanel that was added to the container.
     */
    private JFXPanel includeNodeInPanel(Parent node, Container container) {
        JFXPanel fxPanel = createJFXPanel(node);
        
        container.setLayout(new GridLayout());
        container.add(fxPanel, BorderLayout.CENTER);
        return fxPanel;
    }
    
    /**
     * Sets the initial HTML to the given web engine.
     * 
     * @param engine Engine to set initial html.
     */
    private void setInitialContent(WebEngine engine) {
        // TODO: add default CSS
        String html = "<html dir=\"ltr\"><head></head><body contenteditable=\"false\"></body></html>";
        
        engine.loadContent(html);
    }
    
    /**
     * Locks or unlocks the screen for long operations.  
     * 
     * @param b true to lock, false to unlock.
     */
    public void setLoading(boolean b) {
        ScrMainMenu mainScreen = (ScrMainMenu)SwingUtilities.getAncestorOfClass(ScrMainMenu.class, this.jLayeredPane1);
        mainScreen.setLoading(b);
        this.btnInitialize.setEnabled(!b);
    }
    
    private void initializeTranslationDB() {
        this.setLoading(true);
        new Thread(() -> {
            this.core.getTranslationManager().initializeTmpTranslationDB("", "");
            
            ScrTranslate.this.jPanel1.setVisible(false);
            ScrTranslate.this.setLoading(false);
        }).start();
    }
    
    /**
     * Process value to translate, calls translation manager to retrieve results,
     * and updates values in the corresponding elements.
     */
    private void processTranslationRequest() {
        TranslationManager manager = this.core.getTranslationManager();
        
        try {
            Document doc = Jsoup.parse(this.editorSource.getHtmlText());
            System.out.println(this.editorSource.getHtmlText());
            Elements paragraphs = doc.select("body p");
            String searchString = "";
            for (Element element : paragraphs) {
                searchString = element.text();
            }
            
            final var results = manager.searchTranslationsFor(this.sourceLang, searchString);
            if (results.size() <= 0) return;
            
            Platform.runLater(() -> {
                var bestResult = results.get(0);
                switch(ScrTranslate.this.sourceLang) {
                    case CONLANG -> ScrTranslate.this.editorTranslation.setHtmlText(bestResult.get(ResultColumn.LOCALLANG));
                    case LOCALLANG -> {
                        String translation = bestResult.get(ResultColumn.CONLANG);
                        ScrTranslate.this.editorTranslation.setHtmlText(translation);
                        ScrTranslate.this.viewTranslation.getEngine().loadContent(translation);
                    }
                }
                String sourceTemplate = "<p><span style=\"font-family: &quot;&quot;;\">%s</span></p>";
                ScrTranslate.this.editorSource.setHtmlText(sourceTemplate.formatted(bestResult.get(ResultColumn.HIGHLIGHT)));
                
                // Change to editor when implementing "improve translation"
                // stackPane.getChildren().clear();
                // stackPane.getChildren().addAll(viewTranslation, editorTranslation);
            });
        }
        catch (SQLException ex) {
            Logger.getLogger(ScrTranslate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLayeredPane1 = new javax.swing.JLayeredPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        btnInitialize = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        btnTranslate = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        jLabel3.setText("<html>\n<p>The translation engine needs to be initialized</p>\n<p>This will take the content of your phrasebook and process it to provide translations in your conlang</p>\n<p>Be miindful that this will increase considerably the size of your PGD file</p>\n</html>");
        jPanel1.add(jLabel3);

        btnInitialize.setText("Initialize translation engine");
        btnInitialize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnInitializeMouseClicked(evt);
            }
        });
        jPanel1.add(btnInitialize);

        jSplitPane2.setDividerLocation(300);
        jSplitPane2.setResizeWeight(0.5);

        jLabel1.setText("From");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 520, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 246, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane2.setLeftComponent(jPanel2);

        jLabel2.setText("Translation");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 520, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 246, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane2.setRightComponent(jPanel3);

        btnTranslate.setText("Translate");
        btnTranslate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnTranslateMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnTranslate)
                .addContainerGap())
            .addComponent(jSplitPane2, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jSplitPane2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnTranslate))
        );

        jLayeredPane1.setLayer(jPanel1, javax.swing.JLayeredPane.MODAL_LAYER);
        jLayeredPane1.setLayer(jPanel4, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 629, Short.MAX_VALUE))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 629, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTranslateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTranslateMouseClicked
        this.processTranslationRequest();
    }//GEN-LAST:event_btnTranslateMouseClicked

    private void btnInitializeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnInitializeMouseClicked
        this.initializeTranslationDB();
    }//GEN-LAST:event_btnInitializeMouseClicked

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void saveAllValues() {
        // Do nothing
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // Do nothing
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Component getWindow() {
        return this.jLayeredPane1;
    }
    
    @Override
    public void dispose() {
        try {
            this.core.getTranslationManager().close();
        }
        catch (Exception ex) {
            Logger.getLogger(ScrTranslate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnInitialize;
    private javax.swing.JButton btnTranslate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JSplitPane jSplitPane2;
    // End of variables declaration//GEN-END:variables
}
