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
package TestResources;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.PolyGlot;

/**
 * Allows for testing which requires a DictCore without exposing PolyGlot object
 * @author draque
 */
public class DummyCore extends DictCore {
    private DummyCore (PolyGlot polyGlot) {
        super(polyGlot);
    }
    
    public static DummyCore newCore() {
        try {
            Constructor constructor = PolyGlot.class.getDeclaredConstructor(new Class[]{String.class});
            constructor.setAccessible(true);
            PolyGlot polyGlot = (PolyGlot)constructor.newInstance(PGTUtil.TESTRESOURCES);
            
            return new DummyCore(polyGlot);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            System.err.println("Something's gone wrong with the Dummy Core generation: " + e.getLocalizedMessage());
        }
        
        return null;
    }
}
