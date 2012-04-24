YUI.add('interedition-text', function(Y) {
    var NS = Y.mix(Y.namespace("interedition"), {
        baseUrl: /(.+)\/static\/yui/.exec(Y.config.base)[1]
    })

    NS.RANGE_SORT = function(a, b) {
        var ra = a.range, rb = b.range;
        return (ra.start == rb.start ? rb.end - ra.end : ra.start - rb.start);
    };

    // ================================================================================ Name

    NS.Name = function(namespace, localName) {
        this.namespace = namespace;
        this.localName = localName;
    };
    Y.mix(NS.Name.prototype, {
       toString: function() {
           return (this.namespace == null ? "" : "{" + this.namespace + "}") + this.localName;
       },
       fromString: function(str) {
            var firstBrace = str.indexOf("{");
            if (firstBrace < 0) {
                return new NS.Name(null, str);
            }
            var secondBrace = str.indexOf("}");
            if (secondBrace < firstBrace || secondBrace >= (str.length - 1)) {
                Y.error("Invalid Name", str, { throwFail: true });
            }

            return new NS.Name(str.substring(firstBrace + 1, secondBrace), str.substring(secondBrace + 1))
        }
    });

    // ================================================================================ Range

    NS.Range = function(start, end) {
        this.start = start;
        this.end = end;
    };
    Y.mix(NS.Range.prototype, {
        length: function() { return this.end - this.start; },
        precedes: function(other) { return this.end <= other.start; },
        overlapsWith: function(other) { return this.amountOfOverlapWith(other) > 0; },

        of: function(text) { return text.substring(this.start, this.end); },
        equalsStartOf: function(other) { return  (this.start == other.start) && (this.end == other.start); },
        amountOfOverlapWith: function(other) { return (Math.min(this.end, other.end) - Math.max(this.start, other.start)); },

        fromId: function(str) {
            var components = str.replace("r", "").split("-");
            return new NS.Range(parseInt(components[0]), parseInt(components[1]));
        },
        toId: function() { return "r" + this.start.toString() + "-" + this.end.toString(); },

        toString: function() { return "[" + this.start + ", " + this.end + "]"; }
    });

    // ================================================================================ Annotation

    NS.Annotation = function(name, range, data) {
        this.name = name;
        this.range = range;
        this.data = data || [];
    };

    // ================================================================================ Text

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
                return NS.Name.fromString(n);
            });
        }
    });

    // ================================================================================ Repository


    NS.Repository = Y.Base.create("text-repository", Y.Base, [], {
        read: function(id, cb) {
            Y.io(this.toURI(id), {
                headers: {
                    "Accept": "application/json"
                },
                on: {
                    success: function(transactionId, resp) {
                        var data = Y.JSON.parse(resp.responseText);

                        var names = {};
                        Y.each(data.n || {}, function(n, id) {
                            names[id] = new NS.Name(n[0], n[1]);
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
        write: function(textContents, cb) {
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
        annotate: function(id, annotations, cb) {
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
        transform: function(id, transformConfig, cb) {
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
        redirectTo: function(id) {
            Y.config.win.location = this.toURI(id);
        },
        toURI: function(id) {
            return (this.get("base") + "/text/" + id.toString());
        }
    }, {
        ATTRS: {
            "base": { value: NS.baseUrl }
        }
    });

    Y.extend(NS.Repository, Y.Base, {
    });

    NS.XMLTransformationRule = Y.Base.create("text-xml-transformation-rule", Y.Model, [], {

    }, {
       ATTRS: {
           name: {},
           lineElement: { value: false },
           containerElement: { value: false },
           included: { value: false },
           excluded: { value: false },
           notable: { value: false }
       }
    });
    NS.XMLTransformation = Y.Base.create("text-xml-transformation", Y.Model, [], {
        baseUrl: NS.baseUrl + "/xml/transform",
        url: function() { return "/".join([ this.baseUrl, encodeURIComponent(this.get("name")) ]); },
        parse: function (response) {
            Y.log(response);
            if (response.data) {
                return response.data;
            }
            //this.fire('error', { type : 'parse', error: 'No data in the response.' });
        },
        sync: function (action, options, callback) {
            var onIo = {
                success: Y.bind(this._syncSucceeded, this, callback),
                failure: Y.bind(this._syncFailed, this, callback)
            };
            switch (action) {
                case 'create':
                    Y.io(this.url(), {
                        method: "post",
                        headers: { "Accept": "application/json", "Content-Type": "application/json" },
                        on: {
                            success: function() { this.sync("update", null, callback); },
                            failure: onIo.failure
                        }
                    });
                    return;
                case 'update':
                    Y.io(this.url(), {
                        method: "put",
                        data: Y.JSON.stringify(this.toJSON()),
                        headers: { "Accept": "application/json", "Content-Type": "application/json" },
                        on: onIo
                    });
                    return;
                case 'read':
                    Y.io(this.url(), { method: "get", headers: { "Accept": "application/json" }, on: onIo });
                    return;
                case 'delete':
                    Y.io(this.url(), { method: "delete", headers: { "Accept": "application/json" }, on: onIo });
                    return;
                default:
                    callback('Invalid action');
            }
        },
        _syncSucceeded: function (callback, id, response) {
            callback(null, Y.JSON.parse(response.responseText));
        },
        _syncFailed: function(callback, id, response) {
            callback(response);
        }
    }, {
        ATTRS: {
            name: {},
            description: {},
            transformTEI: { value: false },
            removeEmpty: { value: false },
            notableCharacter: {},
            compressingWhitespace: { value: false },
            removeLeadingWhitespace: { value: false},
            rules: {}
        }
    });
}, "1", {
    requires: ["io", "json", "base", "array-extras", "escape"]
});