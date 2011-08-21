YUI.add("interedition-text", function(Y) {

    Y.namespace("text");

    Y.text.Text = function(data) {
        Y.text.Text.superclass.constructor.apply(this, arguments);
    };

    Y.text.Text.NAME = "text";
    Y.text.Text.ATTRS = {
        text : { value: "" },
        annotations: {
            value: {},
            setter: function(data) {
                var annotations = [];
                Y.each(data, function (a) {
                    annotations.push(new Y.text.Annotation(new Y.text.QName(a.n.ns, a.n.n), new Y.text.Range(a.r.s, a.r.e)));
                });
                annotations.sort(function(a, b) {
                    var ra = a.range, rb = b.range;
                    return (ra.start == rb.start ? rb.end - ra.end : ra.start - rb.start);
                });
                return annotations;
            }
        },
    }

    Y.extend(Y.text.Text, Y.Base, {
        partition: function() {
            var offsets = [];
            Y.each(this.get("annotations"), function(a) {
                var range = a.range;
                if (offsets.indexOf(range.start) < 0) offsets.push(range.start);
                if (offsets.indexOf(range.end) < 0) offsets.push(range.end);
            });
            offsets.sort(function(a, b) { return a - b; });

            var contentLength = this.get("text").length;
            if (offsets.length == 0 || offsets[0] > 0) offsets.unshift(0);
            if (offsets.length == 1 || offsets[offsets.length - 1] < contentLength) offsets.push(contentLength);

            var partitions = [];
            var start = -1;
            Y.each(offsets, function (end) {
                if (start >= 0) partitions.push(new Y.text.Range(start, end));
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
            var names = [];
            Y.each(this.get("annotations"), function(a) { names.push(a.name.toString()); });
            names = Y.Array.unique(names);
            names.sort();
            return Y.Array.map(names, function(n) { return Y.text.QName.fromString(n); });
        }
    });

    Y.text.QName = function(namespace, localName) {
        this.namespace = namespace;
        this.localName = localName;
    };
    Y.text.QName.prototype.toString = function() {
        return (this.namespace == null ? "" : "{" + this.namespace + "}") + this.localName;
    };
    Y.text.QName.fromString = function(str) {
        var firstBrace = str.indexOf("{");
        if (firstBrace < 0) {
            return new Y.text.QName(null, str);
        }
        var secondBrace = str.indexOf("}");
        if (secondBrace < firstBrace || secondBrace >= (str.length - 1)) {
            Y.error("Invalid QName", str, { throwFail: true });
        }

        return new QName(str.substring(firstBrace + 1, secondBrace), str.substring(secondBrace + 1))
    };

    Y.text.Range = function(start, end) {
        this.start = start;
        this.end = end;
    };
    Y.text.Range.prototype.length = function() {
        return this.end - this.start;
    };
    Y.text.Range.prototype.of = function(text) {
        return text.substring(this.start, this.end);
    };
    Y.text.Range.prototype.precedes = function(other) {
        return this.end <= other.start;
    };
    Y.text.Range.prototype.equalsStartOf = function(other) {
        return  (this.start == other.start) && (this.end == other.start);
    };

    Y.text.Range.prototype.overlapsWith = function(other) {
        return this.amountOfOverlapWith(other) > 0;
    };

    Y.text.Range.prototype.amountOfOverlapWith = function(other) {
        return (Math.min(this.end, other.end) - Math.max(this.start, other.start));
    };
    Y.text.Range.prototype.toId = function() {
        return "r" + this.start.toString() + "-" + this.end.toString();
    };

    Y.text.Range.prototype.toString = function() {
        return "[" + this.start + ", " + this.end + "]";
    };

    Y.text.Annotation = function(name, range) {
        this.name = name;
        this.range = range;
    }
}, "0", {
    requires: ["base", "array-extras"]
});