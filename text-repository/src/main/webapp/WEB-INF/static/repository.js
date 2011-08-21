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