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
package org.darisadesigns.polyglotlina;

import java.awt.Window;

/**
 *
 * @author edga_
 */
public interface InfoBox {

    public void info(String title, String message);

    public void error(String title, String message);

    public void warning(String title, String message);

    public Integer yesNoCancel(String title, String message);

    /**
     * Displays confirmation to user for deletion of element
     *
     * @return true if chooser accepts, false otherwise
     */
    public boolean deletionConfirmation();
    
    /**
     * Displays confirmation of user action
     *
     * @param title title of query message
     * @param message shown to user
     * @return true if chooser accepts, false otherwise
     */
    public boolean actionConfirmation(String title, String message);
    
    /**
     * Wraps JOptionPane dialog mostly for neatness/standardization
     * @param title title of query window
     * @param message message on window given to user
     * @return string value if input, null if cancel hit
     */
    public String stringInputDialog(String title, String message);
    
    /**
     * Collects double value form user.Will re-call self if non-double value given.
     * @param title title of query window
     * @param message message on window given to user
     * @param warningMessage Warning message to show if user inputs wrong value (blank for default)
     * @return double value if input, null if cancel hit
     */
    public Double doubleInputDialog(String title, String message, String warningMessage);
}
