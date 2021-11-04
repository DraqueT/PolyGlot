/*
 * Copyright (c) 2019-2021, Draque Thompson, draquemail@gmail.com
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
package TestResources;

import org.darisadesigns.polyglotlina.Desktop.DummyInfoBox;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.darisadesigns.polyglotlina.Desktop.DesktopHelpHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopOSHandler;
import org.darisadesigns.polyglotlina.Desktop.PFontHandler;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.ManagersCollections.DesktopGrammarManager;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.InfoBox;
import org.darisadesigns.polyglotlina.Desktop.PGTUtil;
import org.darisadesigns.polyglotlina.Desktop.PolyGlot;
import org.darisadesigns.polyglotlina.OSHandler;

/**
 * Allows for testing which requires a DictCore without exposing PolyGlot object
 * @author draque
 */
public class DummyCore extends DictCore {
    private DummyCore (DesktopPropertiesManager propsManager, OSHandler osHandler) {
        super(propsManager, osHandler, new PGTUtil(), new DesktopGrammarManager());
    }
    
    public static DummyCore newCore() {
        try {
            InfoBox infoBox = new DummyInfoBox();
            DesktopHelpHandler helpHandler = new DesktopHelpHandler();
            PFontHandler fontHandler = new PFontHandler();
            DesktopOSHandler osHandler = new DesktopOSHandler(DesktopIOHandler.getInstance(), infoBox, helpHandler, fontHandler);
            Constructor constructor = PolyGlot.class.getDeclaredConstructor(new Class[]{String.class, DictCore.class, DesktopOSHandler.class});
            constructor.setAccessible(true);
            DesktopPropertiesManager propsManager = new DesktopPropertiesManager();
            DummyCore core = new DummyCore(propsManager, osHandler);
            // Is this now really needed to be constructed?
            // Some screens use new PolyGlot static instance
            PolyGlot polyGlot = new PolyGlot(PGTUtil.TESTRESOURCES, (DictCore)core, osHandler);
            PolyGlot.setTestPolyGlot(polyGlot);
            return core;
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            System.err.println("Something's gone wrong with the Dummy Core generation: " + e.getLocalizedMessage());
        }
        catch (Exception e) {
            System.err.println("Something's gone wrong with the Dummy Core generation: " + e.getLocalizedMessage());
        }
        
        return null;
    }
}
