YUI.add('interedition-text', function(Y) {
    var NS = Y.namespace("interedition.text");

    NS.RANGE_SORT = function(a, b) {
        var ra = a.range, rb = b.range;
        return (ra.start == rb.start ? rb.end - ra.end : ra.start - rb.start);
    };

    NS.QName = function(namespace, localName) {
        this.namespace = namespace;
        this.localName = localName;
    };
    NS.QName.prototype.toString = function() {
        return (this.namespace == null ? "" : "{" + this.namespace + "}") + this.localName;
    };
    NS.QName.fromString = function(str) {
        var firstBrace = str.indexOf("{");
        if (firstBrace < 0) {
            return new NS.QName(null, str);
        }
        var secondBrace = str.indexOf("}");
        if (secondBrace < firstBrace || secondBrace >= (str.length - 1)) {
            Y.error("Invalid QName", str, { throwFail: true });
        }

        return new NS.QName(str.substring(firstBrace + 1, secondBrace), str.substring(secondBrace + 1))
    };

    NS.Range = function(start, end) {
        this.start = start;
        this.end = end;
    };
    NS.Range.prototype.length = function() {
        return this.end - this.start;
    };
    NS.Range.prototype.of = function(text) {
        return text.substring(this.start, this.end);
    };
    NS.Range.prototype.precedes = function(other) {
        return this.end <= other.start;
    };
    NS.Range.prototype.equalsStartOf = function(other) {
        return  (this.start == other.start) && (this.end == other.start);
    };

    NS.Range.prototype.overlapsWith = function(other) {
        return this.amountOfOverlapWith(other) > 0;
    };
    NS.Range.prototype.amountOfOverlapWith = function(other) {
        return (Math.min(this.end, other.end) - Math.max(this.start, other.start));
    };
    NS.Range.prototype.toId = function() {
        return "r" + this.start.toString() + "-" + this.end.toString();
    };
    NS.Range.fromId = function(str) {
        var components = str.replace("r", "").split("-");
        return new NS.Range(parseInt(components[0]), parseInt(components[1]));
    };
    NS.Range.prototype.toString = function() {
        return "[" + this.start + ", " + this.end + "]";
    };

    NS.Annotation = function(name, range, data) {
        this.name = name;
        this.range = range;
        this.data = data || [];
    };

    NS.Text = function(data) {
        NS.Text.superclass.constructor.apply(this, arguments);
    };
    NS.Text.NAME = "interedition-text";
    NS.Text.ATTRS = {
        text : { value: "" },
        annotations: { value: [] }
    };
    Y.extend(NS.Text, Y.Base, {
        partition: function() {
            var offsets = [];
            Y.each(this.get("annotations"), function(a) {
                var range = a.range;
                if (offsets.indexOf(range.start) < 0) offsets.push(range.start);
                if (offsets.indexOf(range.end) < 0) offsets.push(range.end);
            });
            offsets.sort(function(a, b) {
                return a - b;
            });

            var contentLength = this.get("text").length;
            if (offsets.length == 0 || offsets[0] > 0) offsets.unshift(0);
            if (offsets.length == 1 || offsets[offsets.length - 1] < contentLength) offsets.push(contentLength);

            var partitions = [];
            var start = -1;
            Y.each(offsets, function (end) {
                if (start >= 0) partitions.push(new NS.Range(start, end));
                start = end;
            });
            return partitions;
        },
        index: function() {
            var index = [];
            var annotations = this.get("annotations");
            Y.each(this.partition(), function(partition) {
                var overlapping = [];
                annotations = Y.Array.filter(annotations, function(annotation) {
                    var range = annotation.range;
                    if (range.overlapsWith(partition) || range.equalsStartOf(partition)) {
                        overlapping.push(annotation);
                    }
                    //return !range.precedes(partition);
                    return true;
                });
                index.push({ range: partition, annotations: overlapping });
            });
            return index;
        },
        names: function() {
            var names = Y.Array.map(this.get("annotations"), function(a) {
                return a.name.toString();
            });
            names = Y.Array.dedupe(names);
            names.sort();
            return Y.Array.map(names, function(n) {
                return NS.QName.fromString(n);
            });
        }
    });

    NS.Repository = function(config) {
        NS.Repository.superclass.constructor.apply(this, arguments);
    };
    NS.Repository.NAME = "text-repository";
    NS.Repository.ATTRS = {
        "base": {}
    };
    Y.extend(NS.Repository, Y.Base, {
        "read": function(id, cb) {
            Y.io(this.toURI(id), {
                headers: {
                    "Accept": "application/json"
                },
                on: {
                    success: function(transactionId, resp) {
                        var data = Y.JSON.parse(resp.responseText);

                        var names = {};
                        Y.each(data.n || {}, function(n, id) {
                            names[id] = new NS.QName(n[0], n[1]);
                        });

                        var annotations = Y.Array.map(data.a, function(a) {
                            var annotationData = Y.Array.map(a.d || [], function(ad) {
                                return [ names[ad[0].toString()], ad[1] ];
                            });
                            return new NS.Annotation(names[a.n.toString()], new NS.Range(a.r[0], a.r[1]), annotationData);
                        });

                        cb(new NS.Text({ text: (data.t || ""), annotations: annotations }));
                    }
                }
            });
        },
        "write": function(textContents, cb) {
            Y.io(this.get("base") + "/text", {
                method: "post",
                headers: {
                    "Content-Type": "text/plain",
                    "Accept": "application/json"

                },
                data: textContents,
                on: {
                    success: function(transactionId, resp) {
                        cb(Y.JSON.parse(resp.responseText));
                    }
                }
            });
        },
        "annotate": function(id, annotations, cb) {
            var names = {}, nameCount = 0, nameIndex = {}, nameRef = function(n) {
                var nameStr = n.toString();
                if (nameStr in nameIndex) {
                    return nameIndex[nameStr];
                } else {
                    var ref = (nameCount++).toString();
                    names[ref] = [ n.namespace, n.localName ],
                        nameIndex[nameStr] = ref;
                    return ref;
                }
            };

            var annotations = Y.Array.map(annotations, function(a) {
                var annotationData = Y.Array.map(a.data, function(d) { return [ nameRef(d[0]), d[1] ]; });
                return { "n": nameRef(a.name), "r": [a.range.start, a.range.end], "d": annotationData };
            });

            Y.io(this.toURI(id) + "/annotate", {
                method: "post",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                data: Y.JSON.stringify([ names, annotations ]),
                on: {
                    success: function(transactionId, resp) {
                        cb(Y.JSON.parse(resp.responseText));
                    }
                }
            });

        },
        "transform": function(id, transformConfig, cb) {
            Y.io(this.toURI(id) + "/transform", {
                method: "post",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                data: Y.JSON.stringify(transformConfig),
                on: {
                    success: function(transactionId, resp) {
                        cb(Y.JSON.parse(resp.responseText));
                    }
                }
            });
        },
        "redirectTo": function(id) {
            Y.config.win.location = this.toURI(id);
        },
        "toURI": function(id) {
            return (this.get("base") + "/text/" + id.toString());
        }
    });

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
    requires: ["io", "json", "node", "event", "base", "array-extras", "escape"]
});