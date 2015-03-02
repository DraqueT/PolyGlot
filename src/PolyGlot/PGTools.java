/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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

import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

/**
 *
 * @author draque
 */
public class PGTools {

    /**
     * Makes given component flash
     *
     * @param flashMe component to make flash
     * @param flashColor color to use for flashing
     * @param isBack whether display color is background (rather than foreground)
     */
    public static void flashComponent(final JComponent flashMe, final Color flashColor, final boolean isBack) {
        // this will pop out in its own little thread...
        final SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                Color originColor;
                if (isBack) {
                    originColor = flashMe.getBackground();
                } else {
                    originColor = flashMe.getForeground();
                }

                Color requiredColor = flashColor.equals(originColor)
                        ? Color.white : flashColor;

                try {
                    for (int i = 0; i < 4; i++) {
                        if (isBack) {
                            flashMe.setBackground(requiredColor);
                        } else {
                            flashMe.setEnabled(false);
                        }
                        Thread.sleep(200);
                        if (isBack) {
                            flashMe.setBackground(originColor);
                        } else {
                            flashMe.setEnabled(true);
                        }
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }

                return null;
            }
        };

        worker.execute();
    }
}
