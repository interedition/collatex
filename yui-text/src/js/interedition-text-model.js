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
