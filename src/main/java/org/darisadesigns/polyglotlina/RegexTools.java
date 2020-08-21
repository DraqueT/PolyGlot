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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author draque
 */
public class RegexTools {

    public static String advancedReplace(String value,
            String regex,
            String replacement,
            ReplaceOptions instanceOption) throws Exception {
        String newVal = "";

        if (instanceOption == ReplaceOptions.All) {
            newVal = value.replaceAll(regex, replacement);
        } else {
            List<String> segments = new ArrayList<>();

            if (PGTUtil.regexContainsLookaheadOrBehind(replacement)) {
                throw new Exception("Replacement patterns with lookahead or lookbehind patterns\nmust use \"All Instances\" option.");
            }

            // break string into segments
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(value);

            int lastIndexMatch = 0;

            while (m.find()) {
                segments.add(value.substring(lastIndexMatch, m.start()));
                segments.add(m.group());
                lastIndexMatch = m.end();
            }

            // add segments past last match
            segments.add(value.substring(lastIndexMatch));

            for (int i = 0; i < segments.size(); i++) {
                // only non transformational segments will have even indicies
                if (i % 2 == 0) {
                    newVal += segments.get(i);
                    continue;
                }

                boolean isFirst = i == 1;
                boolean isMiddle = i > 1 && i < segments.size() - 2; // -2 accounts for possibility of segment after last match
                boolean isLast = i == segments.size() - 2;

                if (isFirst && (instanceOption == ReplaceOptions.FirstAndMiddleInstances
                        || instanceOption == ReplaceOptions.FirstInstanceOnly)
                        || isMiddle && (instanceOption == ReplaceOptions.FirstAndMiddleInstances
                        || instanceOption == ReplaceOptions.MiddleAndLastInsances
                        || instanceOption == ReplaceOptions.MiddleInstancesOnly)
                        || isLast && (instanceOption == ReplaceOptions.MiddleAndLastInsances
                        || instanceOption == ReplaceOptions.LastInsanceOnly)) {
                    newVal += segments.get(i).replaceAll(regex, replacement);
                } else {
                    newVal += segments.get(i);
                }
            }
        }

        return newVal;
    }

    /**
     * FirstInstanceOnly: LastInsanceOnly: MiddleInstancesOnly:
     */
    public enum ReplaceOptions {
        All("All Instances"),
        FirstInstanceOnly("First Instance Only"),
        FirstAndMiddleInstances("First and Middle Instances"),
        MiddleInstancesOnly("Middle Instances Only"),
        MiddleAndLastInsances("Middle and Last Instances"),
        LastInsanceOnly("Last Instance Only");

        private final String label;

        private ReplaceOptions(String _label) {
            label = _label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
