YUI().use("io", "json", "dump", "event", "node", "escape", "array-extras", function(Y) {
    var create = Y.Node.create, sub = Y.Lang.sub;

    function addWitness(e) {
        if (e) e.preventDefault();
        var witnesses = getWitnesses();
        witnesses.push("");
        setWitnesses(witnesses);
    }

    function getWitnesses() {
        var contents = []
        Y.all("#witnesses textarea").each(function(w) {
            var content = w.get("value").replace(/^\s*/, "").replace(/\s*$/, "");
            if (content.length > 0) contents.push(content);
        });
        return contents;
    }

    function setWitnesses(contents) {
        while (contents.length < 2) {
            contents.push("");
        }

        var witnessContainer = Y.one("#witnesses");
        witnessContainer.setContent("");

        for (var wc = 0; wc < contents.length; wc++) {
            var witnessData = { id: "witness-" + wc.toString(), label: "Witness #" + (wc + 1).toString(), contents: Y.Escape.html(contents[wc]) };
            witnessContainer.append(create('<div class="yui3-g form-element" />')
                    .append(create('<div class="yui3-u-1-6 form-label"/>').append(sub('<label for="{id}">{label}:</label>', witnessData)))
                    .append(create('<div class="yui3-u form-input"/>').append(sub('<textarea id="{id}" name="{id}" rows="3" cols="80" style="width: 50em">{contents}</textarea>', witnessData))));
        }

        Y.some(contents, function(c, i) {
            if (c.length == 0) {
                Y.one("#witness-" + i.toString()).focus();
                return true;
            }
            return false;
        });

        Y.on("focus", function(e) { this.select(); }, "#witnesses textarea");
        return contents;
    }

    function collate(e) {
        if (e) e.preventDefault();
        var witnesses = getWitnesses();
        if (witnesses.length > 1) {
            var results = Y.one("#results");
            results.setContent('<p class="in-progress">Collating, please wait ...</p>')

            var collation = { witnesses: [] };
            Y.each(witnesses, function(w, i) {
                collation.witnesses.push({ id: "W" + (i + 1).toString(), content: witnesses[i] });
            });
            Y.io(cp + "/", {
                method: "post",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                data: Y.JSON.stringify(collation),
                on: {
                    success: function(transactionId, resp) {
                        results.setContent("");

                        var at = Y.JSON.parse(resp.responseText);
                        var table = create('<table class="alignment"/>');
                        results.append(table);

                        var cells = []
                        var variantStatus = [];
                        Y.each(at.table, function(r) {
                            var cellContents = [];
                            Y.each(r, function(c) {
                                cellContents.push(c == null ? null : Y.Array.reduce(c, "", function(str, next) {
                                    return str + (str.length == 0 ? "" : " ") + next;
                                }));
                            });
                            cells.push(cellContents);
                            variantStatus.push(Y.Array.dedupe(Y.Array.filter(cellContents, function(c) { return (c != null); })).length == 1);
                        });

                        for (var wc = 0; wc < at.sigils.length; wc++) {
                            var column = create("<tr/>");
                            column.append('<th>'+ Y.Escape.html(at.sigils[wc]) + '</th>');

                            Y.each(cells, function(r, cc) {
                                var c = r[wc];
                                column.append('<td class="' + (variantStatus[cc] ? "invariant" : "variant") + (c == null ? " gap" : "") +  '">' + (c == null ? "" : Y.Escape.html(c)));
                            });
                            table.append(column);
                        }

                        results.scrollIntoView();
                    },
                    failure: function(transactionId, resp) {
                        results.setContent('<p class="error">' + Y.dump(resp) + '</p>');
                    }
                }
            });
        }
        setWitnesses(witnesses);
    }

    function selectExample(e) {
        var selected = this.get("value").replace(/^e/, "");
        if (selected.length == 0) {
            setWitnesses(["", ""]);
        } else {
            setWitnesses(examples[parseInt(selected)]);
            collate();
        }
    }

    Y.on("domready", function() {
        setWitnesses(["", ""]);

        var exampleSelect = Y.one("#examples");
        Y.each(examples, function(e, i) {
            var title = e[0];
            if (title.length > 20) title = title.substring(0, 80) + "…";
            var exampleData = { value: "e" + i.toString(), title : title };
            exampleSelect.append(sub('<option value="{value}">{title}</option>', exampleData));
        });

        Y.on("change", selectExample, "#examples");
        Y.on("click", addWitness, "#add-witness");
        Y.on("submit", collate, "#collate-form");
    });
});