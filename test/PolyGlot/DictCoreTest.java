/*
 * Copyright (c) 2019, draque
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

import PolyGlot.CustomControls.PFrame;
import PolyGlot.ManagersCollections.ConWordCollection;
import PolyGlot.ManagersCollections.DeclensionManager;
import PolyGlot.ManagersCollections.EtymologyManager;
import PolyGlot.ManagersCollections.FamilyManager;
import PolyGlot.ManagersCollections.GrammarManager;
import PolyGlot.ManagersCollections.ImageCollection;
import PolyGlot.ManagersCollections.LogoCollection;
import PolyGlot.ManagersCollections.OptionsManager;
import PolyGlot.ManagersCollections.PronunciationMgr;
import PolyGlot.ManagersCollections.PropertiesManager;
import PolyGlot.ManagersCollections.ReversionManager;
import PolyGlot.ManagersCollections.RomanizationManager;
import PolyGlot.ManagersCollections.ToDoManager;
import PolyGlot.ManagersCollections.TypeCollection;
import PolyGlot.ManagersCollections.VisualStyleManager;
import PolyGlot.ManagersCollections.WordClassCollection;
import PolyGlot.Nodes.DeclensionNode;
import PolyGlot.Screens.ScrMainMenu;
import java.awt.Color;
import java.time.Instant;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author draque
 */
public class DictCoreTest {
    
    public DictCoreTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of main method, of class DictCore opens with no errors without
     * input file.
     */
    @Test
    public void testMainNoFile() {
        String[] args = null;
        DictCore.main(args);
    }
    
    /**
     * Test of main method, of class DictCore opens with no errors with input
     * file.
     */
    @Test
    public void testMainWithFile() {
        String[] args = new String[]{"test/TestResources/Lodenkur_TEST.pgd"};
        DictCore.main(args);
    }
    
    /**
     * Test of main method, of class DictCore opens with no errors with missing
     * input file. (ultimately this should be handled via menus, so no error
     * is correct)
     */
    @Test
    public void testMainWithMissingFile() {
        String[] args = new String[]{"test/TestResources/MISSING_FILE.pgd"};
        DictCore.main(args);
    }
}
