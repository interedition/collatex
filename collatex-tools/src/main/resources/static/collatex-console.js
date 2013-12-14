var examples = [
    [
        "This morning the cat observed little birds in the trees.",
        "The cat was observing birds in the little trees this morning, it observed birds for two hours."
    ],
    [
        "The black cat",
        "The black and white cat",
        "The black and green cat",
        "The black very special cat",
        "The black not very special cat"
    ],
    [
        "The black dog chases a red cat.",
        "A red cat chases the black dog.",
        "A red cat chases the yellow dog"
    ],
    [
        "the black cat and the black mat",
        "the black dog and the black mat",
        "the black dog and the black mat"
    ],
    [
        "the black cat on the table",
        "the black saw the black cat on the table",
        "the black saw the black cat on the table"
    ],
    [
        "the black cat sat on the mat",
        "the cat sat on the black mat",
        "the cat sat on the black mat"
    ],
    [
        "the black cat",
        "THE BLACK CAT",
        "The black cat",
        "The, black cat"
    ],
    [
        "the white and black cat",
        "The black cat",
        "the black and white cat",
        "the black and green cat"
    ],
    [
        "a cat or dog",
        "a cat and dog and",
        "a cat and dog and"
    ],
    [
        "He was agast, so",
        "He was agast",
        "So he was agast",
        "He was so agast",
        "He was agast and feerd",
        "So was he agast"
    ],
    [
        "the big bug had a big head",
        "the bug big had a big head",
        "the bug had a small head"
    ],
    [
        "the big bug had a big head",
        "the bug had a small head"
    ],
    [
        "the bug big had a big head",
        "the bug had a small head",
        "the bug had a small head"
    ],
    [
        "the drought of march hath perced to the root and is this the right",
        "the first march of drought pierced to the root and this is the ",
        "the first march of drought hath perced to the root"
    ],
    [
        "the drought of march hath perced to the root",
        "the march of the drought hath perced to the root",
        "the march of drought hath perced to the root"
    ],
    [
        "the very first march of drought hath",
        "the drought of march hath",
        "the drought of march hath"
    ],
    [
        "When April with his showers sweet with fruit The drought of March has pierced unto the root",
        "When showers sweet with April fruit The March of drought has pierced to the root",
        "When showers sweet with April fruit The drought of March has pierced the rood"
    ],
    [
        "This Carpenter hadde wedded newe a wyf",
        "This Carpenter hadde wedded a newe wyf",
        "This Carpenter hadde newe wedded a wyf",
        "This Carpenter hadde wedded newly a wyf",
        "This Carpenter hadde E wedded newe a wyf",
        "This Carpenter hadde newli wedded a wyf",
        "This Carpenter hadde wedded a wyf"
    ],
    [
        "Almost every aspect of what scholarly editors do may be changed",
        "Hardly any aspect of what stupid editors do in the privacy of their own home may be changed again and again",
        "very many aspects of what scholarly editors do in the livingrooms of their own home may not be changed"
    ],
    [
        "Du kennst von Alters her meine Art, mich anzubauen, irgend mir an einem vertraulichen Orte ein Hüttchen aufzuschlagen, und da mit aller Einschränkung zu herbergen.",
        "Du kennst von Altersher meine Art, mich anzubauen, mir irgend an einem vertraulichen Ort ein Hüttchen aufzuschlagen, und da mit aller Einschränkung zu herbergen."
    ],
    [
        "Auch hier hab ich wieder ein Plätzchen",
        "Ich hab auch hier wieder ein Pläzchen",
        "Ich hab auch hier wieder ein Pläzchen"
    ],
    [
        "ταυτα ειπων ο ιϲ̅ εξηλθεν ϲυν τοιϲ μαθηταιϲ αυτου περαν του χειμαρρου των κεδρων οπου ην κηποϲ ειϲ ον ειϲηλθεν αυτοϲ και οι μαθηται αυτου",
        "ταυτα ειπων ― ιϲ̅ εξηλθεν ϲυν τοιϲ μαθηταιϲ αυτου περαν του χειμαρρου των κεδρων οπου ην κηποϲ ειϲ ον ειϲηλθεν αυτοϲ και οι μαθηται αυτου",
        "ταυτα ειπων ο ιη̅ϲ εξηλθεν ϲυν τοιϲ μαθηταιϲ αυτου περαν του χειμαρρου του κεδρου οπου ην κηποϲ ειϲ ον ειϲηλθεν αυτοϲ και οι μαθηται αυτου"
    ],
    [
        "I bought this glass, because it matches those dinner plates.",
        "I bought those glasses."
    ],
    [
        "For light too of a kind came through the clouded panes of a small window set high in say the northern wall.",
        "For light of a kind came then through the one small window set high in the wall.",
        "Light of a kind came then from the one window set high in the wall.",
        "Light of a kind came then from the one high window."
    ],
    [
        "For if not postulated thus could he have wondered thus or indeed at all? Could one out of his mind be reasonably said to wonder if he was not out of his mind and bring what  is more his reason to bear on this  perplexity in the way he must be said to do if he is to be said at all?",
        "[For if not so could he have wondered so ?] Could one not in his right mind be reasonably said to wonder if he was in his right mind and bring what is more his remains of reason to bear on this perplexity in the way he must be said to do if he is to be said at all?",
        "For could one not in his right mind be reasonably said to wonder if he was in his right mind and bring what is more his remains of reason to bear on this perplexity in the way he must be said to do if he is to be said at all?"
    ],
    [
        "The same clock as when for example Magee once died.",
        "The same as when for example Magee once died.",
        "The same as when for example McKee once died .",
        "The same as when among others Darly once died &amp; left him.",
        "The same as when Darly among others once died and left him.",
        "The same as when among others Darly once went andleft him ",
        "The same as when among others Darly once died on him & left him.",
        "The same as when among others Darly once died and left left him.",
        "The same as when among others Darly once died and left him.",
        "The same as when among others Darly pnce died and left him."
    ],
    [
        "Wait for him to reappear or not.",
        "Waiting to see if he will or will not.",
        "Waiting to see if he will or not.",
        "Waiting to see if he would or would not.",
        "Waiting to see (if he would or would not."
    ],
    [
        "Similarly for the half hour and hour when the half hour strikes that the hour may not.",
        "Similarly when the half hour comes will or ",
        "Similarly when the half hour comes.",
        "Similarly when the half hour strikes. comes.",
        "Similarly when the half hour came.",
        "Similarly when the half hour struck? struck.",
        "Similarly when the half hour struck.",
        "Similarly when the hour struck.",
        "Similarly when the half-hour struck."
    ]
];

YUI().use("event", "node", "button", "collatex", function(Y) {
    var collator = new Y.CollateX(),
        create = Y.Node.create,
        sub = Y.Lang.sub,
        svgContainer = null,
        tableContainer = null,
        graphVizDotContainer = null,
        graphmlContainer = null,
        teiPsContainer = null;

    function addWitness(e) {
        e && e.preventDefault();
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
                .append(create('<div class="yui3-u form-input"/>').append(sub('<textarea id="{id}" name="{id}" rows="3" cols="80" style="width: 40em">{contents}</textarea>', witnessData))));
        }

        Y.some(contents, function(c, i) {
            if (c.length == 0) {
                Y.one("#witness-" + i.toString()).focus();
                return true;
            }
            return false;
        });

        Y.on("focus", function() { this.select(); }, "#witnesses textarea");
        return contents;
    }

    function collate() {
        clearResults();

        var witnessTexts = getWitnesses();
        if (witnessTexts.length <= 1) {
            return;
        }

        // build the collation input
        var witnesses = [];
        Y.each(witnessTexts, function(w, i) {
            witnesses.push({ id:"W" + (i + 1).toString(), content: w });
        });

        collator.toSVG(witnesses, function(svg) {
            svgContainer.getDOMNode().appendChild(document.importNode(svg, true));
            tableContainer.scrollIntoView();
        });
        collator.toTable(witnesses, tableContainer);
        collator.toGraphViz(witnesses, function(resp) {
            var textArea = create('<textarea rows="10" style="width: 20em" readonly="readonly">' + Y.Escape.html(resp) + '</textarea>');
            graphVizDotContainer.append(textArea);
        });
        collator.toGraphML(witnesses, function(resp) {
            var textArea = create('<textarea rows="10" style="width: 20em" readonly="readonly">' + Y.Escape.html(resp) + '</textarea>');
            graphmlContainer.append(textArea);
        });

        collator.toTEI(witnesses, function(resp) {
            var textArea = create('<textarea rows="10" style="width: 20em" readonly="readonly">' + Y.Escape.html(resp) + '</textarea>');
            teiPsContainer.append(textArea);
        });
    }

    function selectExample() {
        clearResults();
        var selected = this.get("value").replace(/^e/, "");
        if (selected.length == 0) {
            setWitnesses(["", ""]);
        } else {
            setWitnesses(examples[parseInt(selected)]);
            collate();
        }
    }

    function selectAlgorithm() {
        var newValue = this.get("value");
        if (collator.algorithm != newValue) {
            collator.algorithm = newValue;
            collate();
        }
    }

    function selectJoined() {
        var newValue = this.get("checked");
        if (collator.joined != newValue) {
            collator.joined =  newValue;
            collate();
        }
    }

    function selectTranspositions() {
        var newValue = this.get("checked");
        if (collator.transpositions != newValue) {
            collator.transpositions =  newValue;
            collate();
        }
    }

    function clearResults() {
        svgContainer.empty();
        tableContainer.empty();
        graphVizDotContainer.empty();
        graphmlContainer.empty();
        teiPsContainer.empty();
    }

    Y.on("domready", function() {
        svgContainer = Y.one("#variant-graph-svg");
        tableContainer = Y.one("#alignment-table");
        graphVizDotContainer = Y.one("#graphviz-dot");
        graphmlContainer = Y.one("#graphml");
        teiPsContainer = Y.one("#tei-ps");

        setWitnesses(["", ""]);

        var exampleSelect = Y.one("#examples");
        Y.each(examples, function(e, i) {
            var title = e[0];
            if (title.length > 80) title = title.substring(0, 80) + "…";
            var exampleData = { value: "e" + i.toString(), title : title };
            exampleSelect.append(sub('<option value="{value}">{title}</option>', exampleData));
        });

        Y.on("change", selectJoined, "#joined");
        Y.on("change", selectTranspositions, "#transpositions");
        Y.on("change", selectAlgorithm, "#algorithm");
        Y.on("change", selectExample, "#examples");

        new Y.Button({ srcNode: "#add-witness", render: true }).on("click", addWitness);
        new Y.Button({ srcNode: "#collate", render: true }).on("click", collate);
    });
});