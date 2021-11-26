/*
 * Copyright (c) 2020, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina;

import java.io.IOException;

/**
 *
 * @author draque
 */
public interface HelpHandler {
    
    public static String LEXICON_HELP = "BASIC_FUNCTIONALITY";
    public static String PARTSOFSPEECH_HELP = "-_Word_Types";
    public static String LEXICALCLASSES_HELP = "CLASSES";
    public static String GRAMMAR_HELP = "LODENKUR_-_an_example_language";
    public static String LOGOGRAPHS_HELP = "LOGOGRAPHIC_DICTIONARY";
    public static String PHONOLOGY_HELP = "PHONOLOGY";
    public static String LANGPROPERTIES_HELP = "-_Language_Properties";
    public static String QUIZGENERATOR_HELP = "QUIZ";
    public static String PHRASEBOOK_HELP = "-_Phrasebook";

    public void openHelp();

    public void openHelpToLocation(String location);

    public void openHelpLocal() throws IOException;
}
