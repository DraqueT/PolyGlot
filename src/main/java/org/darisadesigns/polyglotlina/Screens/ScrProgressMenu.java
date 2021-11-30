/*
 * Copyright (c) 2019, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
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

import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.PLanguageStats.PLanguageStatsProgress;
import javax.swing.SwingUtilities;

/**
 *
 * @author draque
 */
public final class ScrProgressMenu extends javax.swing.JDialog implements PLanguageStatsProgress {

    private final int taskLength;
    private int progress;
    private String displayTextValue;
    private boolean isDisposed;
    private final boolean closeOnComplete;

    /**
     * Creates new form ProgressMenu
     * @param title label for window to have
     * @param _taskLength length of task to perform
     * @param displayText whether to display text updates below progress bar
     * @param _closeOnComplete Whether to close the window on task completion
     * @return 
     * @throws java.lang.InterruptedException 
     */
    public static ScrProgressMenu createScrProgressMenu(final String title, 
            final int _taskLength, final boolean displayText, final boolean _closeOnComplete) throws InterruptedException {
        final ScrProgressMenu[] ret = new ScrProgressMenu[1];
        
        Thread makeNew = new Thread(){
            @Override
            public void run() {
                ret[0] = new ScrProgressMenu(title, _taskLength, displayText, _closeOnComplete);
            }
        };
        
        makeNew.start();
        makeNew.join();
        
        return ret [0];
    }
    
    /**
     * Creates new form ProgressMenu (Use crateScrProgressMenu instead unless you have a good reason
     * @param title label for window to have
     * @param _taskLength length of task to perform
     * @param displayText whether to display text updates below progress bar
     * @param _closeOnComplete Whether to close the window on task completion
     */
    public ScrProgressMenu(String title, int _taskLength, boolean displayText, boolean _closeOnComplete) {
        taskLength = _taskLength;
        progress = 0;
        displayTextValue = "";
        isDisposed = false;
        closeOnComplete = _closeOnComplete;
        
        initComponents();
        
        SwingUtilities.invokeLater(()->{
            if (!displayText) {
                jScrollPane1.setVisible(false);
                this.setSize(this.getSize().width, jProgressBar1.getHeight() + 30);
            }

            this.setTitle(title);

            jProgressBar1.setMaximum(100);
        });
        
        
        startUpdateThread();
    }
    
    public void startUpdateThread() {
        Thread thread = new Thread(){
            @Override
            public void run(){
                try {
                    updateProcess();
                } catch (InterruptedException e) {
                    DesktopIOHandler.getInstance().writeErrorLog(e);
                    new DesktopInfoBox().error("Progress Error", "Error in progress bar: " + e.getLocalizedMessage());
                    dispose();
                }
            }
        };
        thread.start();
    }
    
    public void iterateTask() {
        progress++;
    }
    
    @Override
    public void iterateTask(String textUpdate) {
        iterateTask();
        displayTextValue += "\n" + textUpdate;
        
    }
    
    public void setTaskProgress(int _progress) {
        progress = _progress;
    }
    
    public int getProgress() {
        return progress;
    }
    
    /**
     * Background process to update screen values on the fly
     * @throws InterruptedException 
     */
    public void updateProcess() throws InterruptedException {
        // update display values (runs every 0.1 seconds)
        while (!isDisposed && progress < taskLength) {
            jTextArea1.setText(displayTextValue);
            jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
            jProgressBar1.setValue(Integer.divideUnsigned((progress*100),taskLength));
            Thread.sleep(100);
        }
        
        // if task happened so quickly that it's already done
        if (progress <= taskLength) {
            jTextArea1.setText(displayTextValue);
            jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
            jProgressBar1.setValue(100);
        }
        
        if (closeOnComplete) {
            this.dispose();
        }
    }
    
    @Override
    public void dispose() {
        isDisposed = true;
        super.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar(0, taskLength);
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jProgressBar1.setStringPainted(true);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
