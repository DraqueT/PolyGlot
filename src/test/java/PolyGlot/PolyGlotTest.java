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

import org.darisadesigns.polyglotlina.PGTUtil;
import org.darisadesigns.polyglotlina.PolyGlot;
import org.junit.jupiter.api.Test;

/**
 *
 * @author draque
 */
public class PolyGlotTest {
    
    public PolyGlotTest() {
    }

    /**
     * Test of main method, of class DictCore opens with no errors without
     * input file.
     */
    @Test
    public void testMainNoFile() {
        String[] args = {"", "", PGTUtil.TRUE};
        PolyGlot.main(args);
    }
    
    /**
     * Test of main method, of class DictCore opens with no errors with input
     * file.
     */
    @Test
    public void testMainWithFile() {
        String[] args = new String[]{"test/TestResources/Lodenkur_TEST.pgd", "", PGTUtil.TRUE};
        PolyGlot.main(args);
    }
    
    /**
     * Test of main method, of class DictCore opens with no errors with missing
     * input file. (ultimately this should be handled via menus, so no error
     * is correct)
     */
    @Test
    public void testMainWithMissingFile() {
        String[] args = new String[]{"test/TestResources/MISSING_FILE.pgd", "", PGTUtil.TRUE};
        PolyGlot.main(args);
    }
}
