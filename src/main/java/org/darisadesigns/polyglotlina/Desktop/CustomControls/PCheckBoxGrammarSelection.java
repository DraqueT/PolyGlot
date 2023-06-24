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
package org.darisadesigns.polyglotlina.Desktop.CustomControls;

import ChatGPTInterface.GPTTokenEstimater;
import java.awt.event.ItemEvent;
import org.darisadesigns.polyglotlina.CustomControls.GrammarSectionNode;
import org.darisadesigns.polyglotlina.WebInterface;

/**
 *
 * @author draquethompson
 */
public class PCheckBoxGrammarSelection extends PCheckBox {
    private final int gptTokens;
    private final GrammarSectionNode section;
    
    public PCheckBoxGrammarSelection(GrammarSectionNode _section) {
        super();
        section = _section;
        super.setSelected(section.isGptSelected());
        gptTokens = GPTTokenEstimater.estimateTokenCount(WebInterface.getTextFromHtml(section.getSectionText()));
        setText(section.getName());
        setupListeners();
    }
    
    public int getGptTokens() {
        return gptTokens;
    }
    
    public GrammarSectionNode getGrammarSection() {
        return section;
    }
    
    private void setupListeners() {
        this.addItemListener((e) -> {
            section.setGptSelected(e.getStateChange() == ItemEvent.SELECTED);
        });
    }
}
