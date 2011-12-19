YUI.add('interedition-collatex', function(Y) {
    var NS = Y.namespace("interedition.collatex");

    NS.Collator = function(config) {
        NS.Collator.superclass.constructor.apply(this, arguments);
    };
    NS.Collator.NAME = "collatex-collator";
    NS.Collator.ATTRS = {
        "base": {}
    };

    Y.extend(NS.Collator, Y.Base, {
        collate: function(resultType, data, callback) {
            Y.io.queue.stop();
            Y.io.queue(this.get("base") + "/collate", {
                method:"post",
                headers:{
                    "Content-Type":"application/json",
                    "Accept": resultType
                },
                data: Y.JSON.stringify(data),
                on:{
                    success: function(transactionId, resp) { callback(resp); },
                    failure: function (transactionId, resp) { alert("Error in collator: " + resp.statusText); }
                }
            });
            Y.io.queue.start();
        },
        toJSON: function(data, callback) {
            this.collate("application/json", data, function(resp) {
                callback(Y.JSON.parse(resp.responseText));
            });
        },
        toTEI : function(data, callback) {
            this.collate("application/tei+xml", data, function(resp) {
                callback(resp.responseText);
            });
        },
        toSVG: function(data, callback) {
            this.collate("image/svg+xml", data, function(resp) {
                callback(resp.responseXML.documentElement);
            });
        },
        toGraphViz: function(data, callback) {
            this.collate("text/plain", data, function(resp) {
                callback(resp.responseText);
            });
        },
        toGraphML: function(data, callback) {
            this.collate("application/graphml+xml", data, function(resp) {
                callback(resp.responseText);
            });
        },
        toTable: function(data, container) {
            this.toJSON(data, function(at) {
                var table = Y.Node.create('<table class="alignment"/>');
                var cells = [];
                var variantStatus = [];
                Y.each(at.table, function (r) {
                    var cellContents = [];
                    Y.each(r, function (c) {
                        cellContents.push(c == null ? null : Y.Array.reduce(c, "", function (str, next) {
                            return str + (str.length == 0 ? "" : " ") + next;
                        }));
                    });
                    cells.push(cellContents);
                    variantStatus.push(Y.Array.dedupe(Y.Array.filter(cellContents, function (c) {
                        return (c != null);
                    })).length == 1);
                });
                for (var wc = 0; wc < at.sigils.length; wc++) {
                    var column = Y.Node.create("<tr/>");
                    column.append('<th>' + Y.Escape.html(at.sigils[wc]) + '</th>');
                    Y.each(cells, function (r, cc) {
                        var c = r[wc];
                        column.append('<td class="' + (variantStatus[cc] ? "invariant" : "variant") + (c == null ? " gap" : "") + '">' + (c == null ? "" : Y.Escape.html(c)));
                    });
                    table.append(column);
                }

                container.append(table);
            });
        }
    });
}, "1", {
    requires: ["base", "io", "io-queue", "json", "node", "array-extras", "escape"]
});
