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

import org.darisadesigns.polyglotlina.InfoBox;

/**
 *
 * @author draque
 */
public class DummyInfoBox implements InfoBox {

    @Override
    public void info(String title, String message) {
        // dummy
    }

    @Override
    public void error(String title, String message) {
        // dummy
    }

    @Override
    public void warning(String title, String message) {
        // dummy
    }

    @Override
    public Integer yesNoCancel(String title, String message) {
        return 0; // dummy
    }

    @Override
    public boolean deletionConfirmation() {
        // dummy
        return false;
    }

    @Override
    public boolean actionConfirmation(String title, String message) {
        // dummy
        return false;
    }

    @Override
    public String stringInputDialog(String title, String message) {
        // dumy
        return "dummy";
    }

    @Override
    public Double doubleInputDialog(String title, String message, String warningMessage) {
        // dummy
        return 0.0d;
    }
    
}
