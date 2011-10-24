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