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
package PolyGlot.Screens;

import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PButton;
import PolyGlot.CustomControls.PDialog;
import PolyGlot.CustomControls.PLabel;
import PolyGlot.DictCore;
import PolyGlot.IOHandler;
import PolyGlot.WebInterface;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author draque
 */
public class ScrUpdateAlert extends PDialog {

    private final Map<String, String> buttonMap = new HashMap<>();

    /**
     * Creates new form ScrUpdateAlert
     *
     * @param verbose run in verbose mode
     * @param _core current dictionary core
     * @throws java.lang.Exception if unable to connect
     */
    public ScrUpdateAlert(boolean verbose, DictCore _core) throws Exception {
        core = _core;
        setupKeyStrokes();
        initComponents();
        
        jTextPane1.setContentType("text/html");
        jPanel1.setBackground(Color.white);
        super.getRootPane().getContentPane().setBackground(Color.white);

        Document doc = WebInterface.checkForUpdates();
        String ver = doc.getElementsByTagName("Version").item(0).getTextContent();
        Node message = doc.getElementsByTagName("VersionText").item(0);
        List<ConditionalMessage> condMessages = getConditionalMessages(doc);
        List<ConditionalMessage> urgentMessages = getUrgentMessages(condMessages);
        setupButtons(doc.getElementsByTagName("LinkButtons").item(0));

        if (!urgentMessages.isEmpty()) { // prioritize urgent messages first
            this.setTitle("--URGENT POLYGLOT ALERT--");
            String text = "";
            for (ConditionalMessage urgentMessage : urgentMessages) {
                text += urgentMessage.text + "<br>----------------<br>";
            }
            jTextPane1.setText(text);
            txtVersion.setText("--URGENT--");
            setVisible(true);
        } else if (!ver.equals(core.getVersion())) { // next, handle update alerts
            this.setTitle("PolyGlot " + ver + " available");
            jTextPane1.setText(message.getTextContent());
            txtVersion.setText("New Version: " + ver);
            setVisible(true);
        } else if (!condMessages.isEmpty()) { // last, handle all other messages
            this.setTitle("PolyGlot Info Alert");
            String text = "";
            for (ConditionalMessage condMessage : condMessages) {
                text += condMessage.text + "<br>----------------<br>";
            }
            jTextPane1.setText(text);
            txtVersion.setText("- Info -");
            setVisible(true);
        } else {
            if (verbose) { // if in verbose mode (user selected update) inform user they're good to go
                InfoBox.info("Update Status", "You're up to date and on the newest version: "
                    + core.getVersion() + ".", core.getRootWindow());
            }            
            this.setVisible(false);
            this.dispose();
        }
    }
    
    /**
     * Given a list of conditional messages, returns only the urgent ones
     * @param messages
     * @return 
     */
    private List<ConditionalMessage> getUrgentMessages(List<ConditionalMessage> messages) {
        List<ConditionalMessage> ret = new ArrayList<>();
        messages.forEach((message)->{
            if (message.type == MessageType.URGENT) {
                ret.add(message);
            }
        });
        return ret;
    }
    
    /**
     * Pulls conditional messages from XML node and returns those which apply to your situation
     * @param root
     * @return 
     */
    private List<ConditionalMessage> getConditionalMessages(Document root) throws Exception {
        List<ConditionalMessage> ret = new ArrayList<>();
        String myJavaVersion = System.getProperty("java.version");
        String myOs = System.getProperty("os.name");
        String myOsVersion = System.getProperty("os.version");
        String myPolyVersion = core.getVersion();
        
        NodeList condList = root.getElementsByTagName("ConditionalMessage");
        for (int i = 0; i < condList.getLength(); i ++) {
            Integer messageId = -1;
            try {
                Element entry = (Element)condList.item(i);
                messageId = Integer.parseInt(entry.getAttribute("id"));
                String javaVersion = entry.getElementsByTagName("JavaVersion").item(0).getTextContent();
                String javaVersionTest = entry.getElementsByTagName("JavaVersionTest").item(0).getTextContent();
                String os = entry.getElementsByTagName("OS").item(0).getTextContent();
                String osVersion = entry.getElementsByTagName("OSVersion").item(0).getTextContent();
                String polyVersion = entry.getElementsByTagName("PolyGlotVersion").item(0).getTextContent();
                String polyVersionTest = entry.getElementsByTagName("PolyVersionTest").item(0).getTextContent();
                String messageType = entry.getElementsByTagName("MessageType").item(0).getTextContent();
                String text = entry.getElementsByTagName("Text").item(0).getTextContent();

                // only add message to list if it matches for all given values
                if ((javaVersion.isEmpty() || compareJavaVersions(myJavaVersion, javaVersionTest, javaVersion))
                        && (os.isEmpty() || myOs.startsWith(os)) // startswith allows messages as general as I like
                        && (osVersion.isEmpty() || osVersion.equals(myOsVersion))
                        && (polyVersion.isEmpty() || comparePolyVersions(myPolyVersion, polyVersionTest, polyVersion))) {
                    ConditionalMessage message = new ConditionalMessage();
                    message.type = getMessageType(messageType);
                    message.text = text;
                    ret.add(message);
                }
            } catch (Exception e) {
                IOHandler.writeErrorLog(e);
                throw new Exception("Message: " + messageId + " malformed: \n" + e.getLocalizedMessage());
            }
        }
        
        return ret;
    }
    
    private boolean compareJavaVersions(String v1, String operator, String v2) throws Exception {
        boolean ret;
        // throw out comments after '-' and potentially split on sub versions with '_' delimiter
        String[] splitVersion1 = StringUtils.substringBefore(v1, "-").split("_");
        String[] splitVersion2 = StringUtils.substringBefore(v2, "-").split("_");
        int result;
        
        switch (operator) {
            case "eql":
                ret = Arrays.equals(splitVersion1, splitVersion2);
                break;
            case "gt":
                result = compareDotDelimited(splitVersion1[0], splitVersion2[0]);
                if (result == -1){
                    ret = true;
                } else if (result > 0 || (splitVersion1.length == 1 && splitVersion2.length == 1)) {
                    ret = false;
                } else {
                    ret = compareDotDelimited(splitVersion1[1], splitVersion2[1]) == -1;
                }
                break;
            case "lt":
                result = compareDotDelimited(splitVersion1[0], splitVersion2[0]);
                if (result == 1){
                    ret = true;
                } else if (result < 0 || (splitVersion1.length == 1 && splitVersion2.length == 1)) {
                    ret = false;
                } else {
                    ret = compareDotDelimited(splitVersion1[1], splitVersion2[1]) == 1;
                }
                break;

            default:
                throw new Exception("Malformed equality operator: " + operator);
        }
        
        return ret;
    }
    
    private boolean comparePolyVersions(String v1, String operator, String v2) throws Exception {
        boolean ret;
        
        switch (operator) {
            case "eql":
                ret = v1.equals(v2);
                break;
            case "gt":
                ret = compareDotDelimited(v1, v2) == -1;
                break;
            case "lt":
                ret = compareDotDelimited(v1, v2) == 1;
                break;
            default:
                throw new Exception("Malformed equality operator: " + operator);
        }
        
        return ret;
    }
    
    /**
     * Compares values of two dot delimited numbers
     * @param first first number to compare
     * @param second second number to compare
     * @return -1 if first is bigger, 0 if equal, 1 if second bigger
     */
    private int compareDotDelimited(String firstString, String secondString) {
        int first = firstString.contains(".") ? 
                Integer.parseInt(StringUtils.substringBefore(firstString, ".")) : 
                Integer.parseInt(firstString);
        int second = secondString.contains(".") ? 
                Integer.parseInt(StringUtils.substringBefore(secondString, ".")) : 
                Integer.parseInt(secondString);
        int ret;
        
        if (first > second) {
            ret = -1;
        } else if (first < second) {
            ret = 1;
        }else {
            String newFirst = StringUtils.substringAfter(firstString, ".");
            String newSecond = StringUtils.substringAfter(secondString, ".");
            
            if (newFirst.isEmpty() && newSecond.isEmpty()) {
                ret = 0;
            } else if (newSecond.isEmpty()) {
                ret = -1;
            } else {
                ret = compareDotDelimited(newFirst, newSecond);
            }
        }
        
        return ret;
    }
    
    private void setupButtons(Node buttons) {
        for (Node curButton = buttons.getFirstChild();
                curButton != null;
                curButton = curButton.getNextSibling()) {
            Node nameNode = ((Element) curButton.getChildNodes()).getElementsByTagName("Text").item(0);
            Node linkNode = ((Element) curButton.getChildNodes()).getElementsByTagName("Link").item(0);

            buttonMap.put(nameNode.getTextContent(), linkNode.getTextContent());

            PButton newButton = new PButton(core);
            newButton.addActionListener((ActionEvent e) -> {
                JButton thisButton = (JButton) e.getSource();
                String link = buttonMap.get(thisButton.getText());
                URI uri;
                
                try {
                    uri = new URI(link);
                    uri.normalize();
                    java.awt.Desktop.getDesktop().browse(uri);
                } catch (IOException | URISyntaxException ex) {
                    IOHandler.writeErrorLog(ex);
                    InfoBox.error("Browser Error", "Unable to open page: " + link, core.getRootWindow());
                }
            });

            newButton.setText(nameNode.getTextContent());
            newButton.setSize(358, 30);

            jPanel1.add(newButton);

            int height = this.getSize().height;
            newButton.setLocation(0, height - 30);
            this.setSize(height, height + newButton.getHeight());
        }
    }
    
    @Override
    public final Dimension getSize() {
        return super.getSize();
    }
    
    @Override
    public final void setTitle(String _title) {
        super.setTitle(_title);
    }
    
    @Override
    public final void setupKeyStrokes() {
        super.setupKeyStrokes();
    }
    
    @Override
    public final void setSize(int _width, int height) {
        super.setSize(_width, height);
    }
    
    @Override
    public void updateAllValues(DictCore _dictCore) {
        // No values to update
    }
    
    @Override
    public final void dispose() {
        super.dispose();
    }

    @Override
    public final void setVisible(boolean _visible) {
        super.setVisible(_visible);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        txtVersion = new PLabel("", core);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        setResizable(false);

        jScrollPane2.setViewportBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jTextPane1.setEditable(false);
        jTextPane1.setBackground(new java.awt.Color(204, 204, 204));
        jScrollPane2.setViewportView(jTextPane1);

        txtVersion.setText("--");
        txtVersion.setToolTipText("");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtVersion)
                        .addGap(0, 344, Short.MAX_VALUE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtVersion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void run(boolean verbose, DictCore core) throws Exception {
        ScrUpdateAlert s = new ScrUpdateAlert(verbose, core);
        s.setModal(false);
    }
    
    private class ConditionalMessage {
        public MessageType type;
        public String text;
    }
    
    private MessageType getMessageType (String val) throws Exception {
        MessageType ret;
        
        switch(val) {
            case "U":
                ret = MessageType.URGENT;
                break;
            case "O":
                ret = MessageType.ONETIME;
                break;
            case "I":
                ret = MessageType.INFO;
                break;
            default:
                throw new Exception("Malformed message type: " + val);
        }
        
        return ret;
    }
    
    private enum MessageType {
        URGENT, INFO, ONETIME
    }

    public void testRun() throws Exception {
        // simple int cases
        assert(compareJavaVersions("1", "eql", "1") == true);
        assert(compareJavaVersions("1", "eql", "2") == false);
        assert(compareJavaVersions("2", "eql", "1") == false);

        assert(compareJavaVersions("1", "gt", "1") == false);
        assert(compareJavaVersions("1", "gt", "2") == false);
        assert(compareJavaVersions("2", "gt", "1") == true);

        assert(compareJavaVersions("1", "lt", "1") == false);
        assert(compareJavaVersions("1", "lt", "2") == true);
        assert(compareJavaVersions("2", "lt", "1") == false);

        // delimited int cases
        assert(compareJavaVersions("1_1", "eql", "1_1") == true);
        assert(compareJavaVersions("1_1", "eql", "2_1") == false);
        assert(compareJavaVersions("2_1", "eql", "1_1") == false);

        assert(compareJavaVersions("1_1", "gt", "1_1") == false);
        assert(compareJavaVersions("1_1", "gt", "2_1") == false);
        assert(compareJavaVersions("2_1", "gt", "1_1") == true);

        assert(compareJavaVersions("1_1", "lt", "1_1") == false);
        assert(compareJavaVersions("1_1", "lt", "2_1") == true);
        assert(compareJavaVersions("2_1", "lt", "1_1") == false);

        assert(compareJavaVersions("1_1", "eql", "1_2") == false);

        assert(compareJavaVersions("1_1", "gt", "1_2") == false);
        assert(compareJavaVersions("1_2", "gt", "1_1") == true);

        // complex, delimited double cases
        assert(compareJavaVersions("1.1_1.1", "eql", "1.1_1.1") == true);
        assert(compareJavaVersions("1_1.1.1", "eql", "2_1.1") == false);
        assert(compareJavaVersions("2.1_1.1", "eql", "1.2_1.1") == false);

        assert(compareJavaVersions("1.1_1.1", "gt", "1.1_1.2") == false);
        assert(compareJavaVersions("1.1_1.2", "gt", "2.1_1.1") == false);
        assert(compareJavaVersions("2.1_1.1", "gt", "1.2_1.2") == true);

        assert(compareJavaVersions("1.1_1.2", "lt", "1.1_1.1") == false);
        assert(compareJavaVersions("1.1_1.2", "lt", "2.1_1.1") == true);
        assert(compareJavaVersions("2.1_1.2", "lt", "1.2_1.2") == false);

        // simple cases with stupid shit at the end
        assert(compareJavaVersions("1-HI!", "eql", "1") == true);
        assert(compareJavaVersions("1-DUH", "eql", "2") == false);
        assert(compareJavaVersions("2-BARF", "eql", "1") == false);

        assert(compareJavaVersions("1------", "gt", "1-AIOSdasdakudKYF#^&#@&G-asda") == false);
        assert(compareJavaVersions("1--asdakjbdasd", "gt", "2-asduhasd") == false);
        assert(compareJavaVersions("2", "gt", "1") == true);

        assert(compareJavaVersions("1", "lt", "1-asdoaishdsa") == false);
        assert(compareJavaVersions("1", "lt", "2-asdasjdbvsajdh-sa-da-da#") == true);
        assert(compareJavaVersions("2-asiuAUYDGAUFgA", "lt", "1") == false);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JLabel txtVersion;
    // End of variables declaration//GEN-END:variables
}
