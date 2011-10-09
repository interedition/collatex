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
    var NS = Y.namespace("interedition.text");

    NS.Repository = function(config) {
        NS.Repository.superclass.constructor.apply(this, arguments);
    };
    NS.Repository.NAME = "text-repository";
    NS.Repository.ATTRS = {
        "base": {}
    };

    //Y.io.transport({ xdr: { use: "native" }});

    Y.extend(NS.Repository, Y.Base, {
        "read": function(id, text) {
            Y.io(this.get("base") + "/text/" + id.toString(), {
                headers: {
                        "Accept": "application/json"
                    },
                on: {
                        success: function(transactionId, resp) {
                            var data = Y.JSON.parse(resp.responseText);

                            var names = {};
                            Y.each(data.n || {}, function(n, id) { names[id] = new NS.QName(n[0], n[1]); });

                            var annotations = Y.Array.map(data.a, function(a) {
                                var annotationData = Y.Array.map(a.d || [], function(ad) {
                                    return [ names[ad[0].toString()], ad[1] ];
                                });
                                return new NS.Annotation(names[a.n.toString()], new NS.Range(a.r[0], a.r[1]), annotationData);
                            });

                            text.set("data", [ (data.t || ""), annotations ]);
                        }
                    }
                });
        },
        "transform": function(id, transformConfig, cb) {
            Y.io(this.get("base") + "/text/" + id.toString() + "/transform", {
                method: "post",
                headers: {
                        "Content-Type": "application/json",
                        "Accept": "application/json"
                    },
                data: Y.JSON.stringify(transformConfig),
                on: {
                        success: function(transactionId, resp) { cb(Y.JSON.parse(resp.responseText)); }
                    }
                });
        }
    });
}, "0", {
    requires: ["array-extras", "io", "json", "interedition-text"]
});
