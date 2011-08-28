/*
 * #%L
 * Text Repository: Datastore for texts based on Interedition's model.
 * %%
 * Copyright (C) 2010 - 2011 The Interedition Development Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
YUI.add("interedition-text-repository", function(Y) {
    Y.namespace("textRepository");

    Y.textRepository.getText = function(id, text) {
        Y.io(cp + "/text/" + id.toString(), {
            headers: {
                    "Accept": "text/plain"
                },
            on: {
                    success: function(transactionId, resp) { text.set("text", resp.responseText); }
                }
            });
    }

    Y.textRepository.getAnnotations = function(id, text) {
        Y.io(cp + "/text/" + id.toString() + "/annotations", {
            headers: {
                    "Accept": "application/json"
                },
            on: {
                    success: function(transactionId, resp) {
                        text.set("annotations", Y.JSON.parse(resp.responseText));
                    }
                }
            });
    }

    Y.textRepository.parseXML = function(id, parserConfig, cb) {
        Y.io(cp + "/xml/" + id.toString() + "/parse", {
            method: "post",
            headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
            data: Y.JSON.stringify(parserConfig),
            on: {
                    success: function(transactionId, resp) { cb(Y.JSON.parse(resp.responseText)); }
                }
            });
    }
}, "0", {
    requires: ["io", "json", "interedition-text"]
});
