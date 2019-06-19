/*
 * Copyright (c) 2019, Draque Thompson
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

import PolyGlot.Screens.ScrAbout;
import java.awt.Component;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

/**
 *
 * @author draque
 */
public class OSIntegrations {    
    public static Object integrateToOs() {
        Object ret = null;
        
        if (PGTUtil.isOSX()) {
            ret = integrateToMac();
        }
        
        return ret;
    }
    
    private static Object integrateToMac() {
        System.setProperty("apple.awt.application.name", PGTUtil.displayName);
        
        com.apple.eawt.Application macApp = com.apple.eawt.Application.getApplication();
        macApp.setDockIconImage(PGTUtil.polyGlotIcon.getImage());
        macApp.setAboutHandler(new com.apple.eawt.AboutHandler() {
            @Override
            public void handleAbout(com.apple.eawt.AppEvent.AboutEvent ae) {
                ScrAbout.run(new DictCore()); // about menu doesn't need actual core...
            }
        });
        
        // populate menu bar with placeholders until they can be populated later
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(new JMenu(" "));
        menuBar.add(new JMenu("  "));
        menuBar.add(new JMenu("   "));
        menuBar.add(new JMenu("    "));
        menuBar.add(new JMenu("     "));
        macApp.setDefaultMenuBar(menuBar);
        
        return menuBar;
    }
    
    public static void integrateMacMenuBar(JMenuBar sourceMenuBar, Object targeObject) {
        if (targeObject instanceof JMenuBar) {
            JMenuBar targetBar = (JMenuBar)targeObject;
            com.apple.eawt.Application macApp = com.apple.eawt.Application.getApplication();
            Component[] sourceComponents = sourceMenuBar.getComponents();
            Component[] targetComponents = targetBar.getComponents();

            for (int i = 0; i < targetComponents.length; i++) {
                if (i < sourceComponents.length) {
                    JMenu targetMenu = (JMenu)targetComponents[i];
                    JMenu sourceMenu = (JMenu)sourceComponents[i];
                    
                    System.out.println(sourceMenu.getComponentCount());
                    for (Component subComp : sourceMenu.getMenuComponents()) {
                        targetMenu.add(subComp);
                    }
                    
                    targetMenu.setText(sourceMenu.getText());
                    sourceMenuBar.remove(sourceMenu);
                } else {
                    targetComponents[i].setVisible(false);
                }
            }

            macApp.setDefaultMenuBar(targetBar);
        }
    }
}
