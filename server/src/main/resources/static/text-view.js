YUI.add('interedition-text-view', function(Y) {
    var NS = Y.namespace("interedition.text");

    NS.TextPanel = function(cfg) {
        NS.TextPanel.superclass.constructor.apply(this, arguments);
    };
    NS.TextPanel.NAME = "interedition-text-panel";
    NS.TextPanel.ATTRS = {
        node: {
            writeOnce: "initOnly",
            setter: function(node) {
                var n = Y.one(node);
                if (!n) throw ("TextPanel: Invalid node given: " + node);
                return n;
            }
        },
        text: {
            writeOnce: "initOnly"
        },
        segments: {
            value: []
        }
    };
    Y.extend(NS.TextPanel, Y.Base, {
        initializer: function(cfg) {
            this.get("text").after("textChange", this.update, this);
            this.get("text").after("annotationsChange", this.update, this);
            this.update();
        },
        update: function() {
            var node = this.get("node"), text = this.get("text"), segments = this.get("segments");
            var textContents = text.get("text");

            node.empty();
            segments.length = 0;
            Y.each(text.partition(), function(range) {
                var start = range.start, end = range.end, spanId = range.toId();
                var segment = Y.Node.create("<span id='" + spanId + "'>" + Y.Escape.html(textContents.substring(start, end)).replace("\n", "<br>") + "</span>");

                node.append(segment);
                segments.push([range, segment]);
            });
            this.fire("update");
        }
    });

    NS.AnnotationNamesSelectionList = function(cfg) {
        NS.AnnotationNamesSelectionList.superclass.constructor.apply(this, arguments);
    };
    NS.AnnotationNamesSelectionList.NAME = "interedition-text-annotation-names-selection-list";
    NS.AnnotationNamesSelectionList.ATTRS = {
        node: {
            writeOnce: "initOnly",
            setter: function(node) {
                var n = Y.one(node);
                if (!n) throw ("AnnotationGutter: Invalid node given: " + node);
                return n;
            }
        },
        textPanel: {},
        names: {
            value: []
        },
        selectedNames: {
            value: []
        }
    };
    Y.extend(NS.AnnotationNamesSelectionList, Y.Base, {
        initializer: function(cfg) {
            this.get("textPanel").on("update", this.update, this);
            this.update();
        },
        entryChanged: function(e) {
            var names = this.get("names");
            var selected = names[parseInt(e.target.getAttribute("id").replace("name-", ""))].toString();
            var selectedNames = this.get("selectedNames");
            if (e.target.get("checked")) {
                selectedNames.push(selected);
                selectedNames.sort();
            } else {
                selectedNames = Y.Array.filter(selectedNames, function(n) {
                    return (n != selected);
                });
            }
            this.set("selectedNames", selectedNames);
        },
        update: function() {
            var names = this.get("textPanel").get("text").names();
            this.set("names", names);

            var container = this.get("node");
            container.empty();
            Y.each(names, function(n, i) {
                var nameId = "name-" + i;
                var input = Y.Node.create(Y.Lang.sub("<input type='checkbox' id='{id}' name='{id}' />", { id: nameId }));
                container.append(Y.Node.create("<p/>")
                    .append(input)
                    .append("&nbsp;")
                    .append(Y.Lang.sub("<label for='{id}'>{label}</label>", { id: nameId, label: n.toString() })));
                Y.on("change", this.entryChanged, input, this);
            }, this);

        }
    });

    NS.AnnotationGutter = function(cfg) {
        NS.AnnotationGutter.superclass.constructor.apply(this, arguments);
    };
    NS.AnnotationGutter.NAME = "interedition-text-annotation-gutter";
    NS.AnnotationGutter.ATTRS = {
        node: {
            writeOnce: "initOnly",
            setter: function(node) {
                var n = Y.one(node);
                if (!n) throw ("AnnotationGutter: Invalid node given: " + node);
                return n;
            }
        },
        textPanel: {}
    }
    Y.extend(NS.AnnotationGutter, Y.Base, {
        initializer: function(cfg) {
            this.get("textPanel").on("update", this.update, this);
            this.update();
        },
        update: function() {
            var textPanel = this.get("textPanel"), gutterNode = this.get("node");

            var textPanelRegion = textPanel.get("node").get("region");

            gutterNode.empty();
            var gutter = d3.select(gutterNode.getDOMNode())
                .append("svg:svg")
                .attr("width", "100%")
                .attr("height", textPanelRegion.height + "px");

            gutter.append("svg:rect")
                .attr("x", 10).attr("y", 0).attr("width", 30).attr("height", textPanelRegion.height)
                .attr("stroke", "#fff").attr("fill", "#ffc")
                .on("click", function() {
                    alert("Click!");
                });

        }
    });
}, "1", {
    requires: ["interedition-text", "node", "event", "base", "array-extras", "escape"]
});