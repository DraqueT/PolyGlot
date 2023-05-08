/*
 * Copyright (c) 2023, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Webservice;

import java.util.Map;
import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.WebInterface;
import static org.darisadesigns.polyglotlina.Webservice.WebService.GET_CONLANG_FONT;
import static org.darisadesigns.polyglotlina.Webservice.WebService.GET_FULL_FILE;
import static org.darisadesigns.polyglotlina.Webservice.WebService.GET_IMAGE;
import static org.darisadesigns.polyglotlina.Webservice.WebService.GET_LOCAL_FONT;
import static org.darisadesigns.polyglotlina.Webservice.WebService.GET_METADATA;
import static org.darisadesigns.polyglotlina.Webservice.WebService.GET_SOUND;
import static org.darisadesigns.polyglotlina.Webservice.WebService.GET_XML;
import static org.darisadesigns.polyglotlina.Webservice.WebService.REFRESH;

/**
 *
 * @author draquethompson
 */
public class Index {
    private final Map<String, DictCore> pgdFiles;
    
    public Index(Map<String, DictCore> _pgdFiles) {
        pgdFiles = _pgdFiles;
    }
    
    public String buildPage() {
        var bodyContents = "\n<h1>PolyGlot Webservice</h1>";
        bodyContents += "\n<br><a href = \"" + REFRESH + "\">Refresh Files</a>";

        bodyContents += "\n<br><div>";

        for (String key : pgdFiles.keySet()) {
            bodyContents += buildLanguageOptions(key, pgdFiles.get(key)) + "<br>";
        }

        bodyContents += "\n</div>";
        bodyContents += getJavaScript();
        
        return WebService.buildDocument("PolyGlot Webservice", bodyContents);
    }
    
    private String buildLanguageOptions(String fileName, DictCore core) {
        var langOpt = "\n<div><h2>Language: " + core.getPropertiesManager().getLangName() + "</h2>";
        var fileNoDot = fileName.replace(".", "");
        langOpt += "\n<b>by: " + WebInterface.getTextFromHtml(core.getPropertiesManager().getCopyrightAuthorInfo()) + "</b>";
        langOpt += "\n<ul>";
        langOpt += "\n<li><a href=\"" + GET_METADATA + "?file=" + fileName + "\">" + "Get Language Metadata (JSON)" + "</a></li>";
        langOpt += "\n<li><a href=\"" + GET_FULL_FILE + "?file=" + fileName + "\">" + "Get Full Language File" + "</a></li>";
        langOpt += "\n<li><a href=\"" + GET_CONLANG_FONT + "?file=" + fileName + "\">" + "Get Conlang Font" + "</a></li>";
        langOpt += "\n<li><a href=\"" + GET_LOCAL_FONT + "?file=" + fileName + "\">" + "Get Local Lang Font" + "</a></li>";
        langOpt += "\n<li><a href=\"" + GET_XML + "?file=" + fileName + "\">" + "Get Raw XML" + "</a></li>";
        langOpt += "\n<li><input type=\"text\" id=\"" + fileNoDot + "_img_txt\"><button type=\"button\" id=\"" + fileNoDot + "_img_button\">Get Image By Id</button></li>";
        langOpt += "\n<li><input type=\"text\" id=\"" + fileNoDot + "_sound_txt\"><button type=\"button\" id=\"" + fileNoDot + "_sound_button\">Get Sound By Id</button></li>";
        langOpt += "\n</ul>";

        return langOpt;
    }

    private String getJavaScript() {
        String script = "<script>";

        // Sets up sound/image download textboxes/buttons
        for (var key : pgdFiles.keySet()) {
            var buttonCode = """
                
                document.getElementById('%s').addEventListener('click', async function() {
                    fileName = '%s'
                    const textInput = document.getElementById('%s').value;
                    window.location.href = '%s?file=%s&mediaId=' + encodeURIComponent(textInput);
                });
                """;
            var fileNoDot = key.replace(".", "");
            
            script += String.format(buttonCode, fileNoDot + "_img_button", key, fileNoDot + "_img_txt", GET_IMAGE, key);
            script += String.format(buttonCode, fileNoDot + "_sound_button", key, fileNoDot + "_sound_txt", GET_SOUND, key);
        }

        script += "</script>";

        return script;
    }
}
