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
package ChatGPTInterface;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import java.io.StringReader;

/**
 *
 * @author draquethompson
 */
public class ReplyMessage {
    private final String content;
    
    public ReplyMessage(String rawJson) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(rawJson))) {
            JsonObject jsonObject = jsonReader.readObject();

            // TODO: Expand what is done with this reply data
            /*
            Example reply
            {
                "id": "chatcmpl-7OI0nDKpWdHqHI1lkIQCeXiFLWbCi",
                "object": "chat.completion",
                "created": 1686023465,
                "model": "gpt-3.5-turbo-0301",
                "usage": {
                  "prompt_tokens": 357,
                  "completion_tokens": 27,
                  "total_tokens": 384
                },
                "choices": [
                  {
                    "message": {
                      "role": "assistant",
                      "content": "dz 7a 4d 6g 1b 3z 8g 5c 3jz."
                    },
                    "finish_reason": "stop",
                    "index": 0
                  }
                ]
              }
             */
            JsonValue choices = jsonObject.get("choices");
            
            JsonObject choice;
            
            if (choices.getValueType() == ValueType.ARRAY) {
                choice = choices.asJsonArray().getJsonObject(0);
            } else {
                choice = choices.asJsonObject();
            }
            
            content = choice.getJsonObject("message").getString("content");
        }
    }
    
    public String getContent() {
        return content;
    }
}

