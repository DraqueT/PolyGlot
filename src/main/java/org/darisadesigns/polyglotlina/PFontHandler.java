/*
 * Copyright (c) 2019 - 2021, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina;

import java.io.IOException;

/**
 * Split out from IOHandler due to large portion devoted to handling fonts
 *
 * @author Draque Thompson
 */
public abstract class PFontHandler {

    /**
     * Sets the conlang and local lang fonts, if one exists and caches its file
     * for quicksaving
     *
     * @param _path The path of the PGD file
     * @param core the dictionary core
     * @throws java.io.IOException
     * @throws java.awt.FontFormatException
     */
    public abstract void setFontFrom(String _path, DictCore core) throws IOException, Exception;
    
    /**
     * Tests whether given string can have every character represented as a glyph for the given font
     * @param value string to test
     * @param conFont true if using the conlang font, false if using the local language font
     * @return 
     */
    public abstract boolean canStringBeRendered(String value, boolean conFont);
    
    /**
     * Pushes local font to any relevant objects
     */
    public abstract void updateLocalFont();
}
