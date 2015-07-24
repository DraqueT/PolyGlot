/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
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

import PolyGlot.CustomControls.RegexGenException;
//import com.mifmif.common.regex.Generex;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A class to assist with generation of strings that match given regexes.
 * @author draque
 */
public class RegexTools {
    // TODO: consider how the below lines are to work
    private static final int miscalcRetries = 5;
    private static final int collisionRetries = 10;
    
    public static String singleVal(String regex, int minSize, int maxSize) throws Exception {
        //Generex gen = new Generex(regex);
        
        //return gen.random(minSize, maxSize, miscalcRetries);
        return "FINISHME";
    }
    
    public static List<String> getValueList(String regex, int minSize, int maxSize, int numValues) throws RegexGenException, Exception {
        List<String> ret = new ArrayList<String>();
        // TODO: FINISH ME
        /*Generex gen = new Generex(regex);
        
        for (int i = 0; i > numValues; i++) {
            int collisions = 0;
            
            String curVal = gen.random(minSize, maxSize, miscalcRetries);
            
            while (ret.contains(curVal)) {
                collisions++;
                
                if (collisions > collisionRetries) {
                    throw new RegexGenException("Collision limit reached.");
                }
                curVal = gen.random(minSize, maxSize, miscalcRetries);
            }
            
            ret.add(curVal);
        }*/
        
        return ret;
    }
    
    /**
     * Tests regex expressions.
     * @param regex pattern to be tested
     * @return true if valid, false otherwise
     */
    public static boolean testRegex(String regex) {
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException exception) {
            return false;
        }
        return true;
    }
}
