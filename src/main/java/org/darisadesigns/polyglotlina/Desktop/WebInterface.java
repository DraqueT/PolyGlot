/*
 * Copyright (c) 2021, Draque Thompson, draquemail@gmail.com
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
package org.darisadesigns.polyglotlina.Desktop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author edga_
 */
public class WebInterface {
    /**
     * This cycles through the body of HTML and generates an ordered list of
     * objects representing all of the items in the HTML. Consumers are
     * responsible for identifying objects.
     *
     * @param html HTML to extract from
     * @return
     * @throws java.io.IOException
     */
    public static List<Object> getElementsHTMLBody(String html) throws IOException {
        List<Object> ret = new ArrayList<>();
        String body = html.replaceAll(".*<body>", "");
        body = body.replaceAll("</body>.*", "");
        Pattern pattern = Pattern.compile("([^<]+|<[^>]+>)");//("(<[^>]+>)");
        Matcher matcher = pattern.matcher(body);

        // loops on unincumbered text and tags.
        while (matcher.find()) {
            String token = matcher.group(1);
            if (token.startsWith("<")) {
                if (token.contains("<img src=\"")) {
                    String path = token.replace("<img src=\"file:///", "").replace("\">", "");
                    ret.add(DesktopIOHandler.getInstance().getImage(path));
                } else {
                    // do nothing with unrecognized elements - might be upgraded later.
                }
            } else {
                // this is plaintext
                String add = token.trim();
                if (!add.isEmpty()) {
                    ret.add(add + " ");
                }
            }
        }

        return ret;
    }
}
