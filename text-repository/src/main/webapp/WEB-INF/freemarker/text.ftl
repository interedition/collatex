<#assign maxLength=102400 truncated=(text.length gt maxLength)/>
<@ie.page text.description?html>
<h1>${text.description?html}</h1>

<div id="text-length" style="text-align: right; font-weight: bold; margin: 1em 0">
    Created: ${text.created?string?html}
    | Updated: ${text.updated?string?html}
    | Length: ${text.length} characters <#if truncated>(${maxLength} displayed)</#if>
    <#if text.type == "XML">| <a href="${cp}/xml/${text.id?c}/parse" title="Parse XML">Parse XML</a></#if>
</div>

    <#if text.summary?has_content>
    <h2>Summary</h2>
    <p>${text.summary?html?replace("\n", "<br>")}</p>
    </#if>

<div class="yui3-g">
    <div class="yui3-u-2-3">
        <h2><img src="${cp}/static/icon/text_${text.type?lower_case}.png" alt="${text.type?html}"> Content</h2>
        <#if text.type == "TXT">
            <div id="text-contents" style="margin: 1em; padding: 1em; font-size: 125%"></div>
        </#if>
        <#if text.type == "XML">
            <textarea style="width: 95%; height: 500px" readonly="readonly">${textContents?html}</textarea>
        </#if>
    </div>
    <div class="yui3-u-1-3" style="color: #999999">
        <div id="histogram"></div>
        <div id="annotations"></div>
    </div>
</div>

<p style="font-size: small; color: #dcdcdc;">SHA-512: ${text.digest?html}</p>

<script type="text/javascript">
    var textId = ${text.id?c};
    var textContents = "${textContents?js_string}";
    var textType = "${text.type?js_string}";

    YUI().use("node", "event", "dump", "escape", "interedition-text-repository", function(Y) {
        Y.on("domready", function() {
            var textEl = Y.one("#text-contents"), annotationEl = Y.one("#annotations");
            if (textEl == null) return;

            var selectedNames = [];

            function spanClick(e) {
                var range = Y.text.Range.fromId(e.target.getAttribute("id"));
                var annotations = "";
                Y.each(text.get("annotations"), function(a) {
                    if (a.range.overlapsWith(range)) {
                        if (Y.Array.some(selectedNames, function(n) { return n == a.name.toString(); })) {
                            annotations += "*";
                        }
                        annotations += Y.Lang.sub("<{name}; {range}>\n", a);
                    }
                });
                alert(annotations);
            }
            text = new Y.text.Text();
            text.set("text", textContents);


            text.after("annotationsChange", function() {
                var textContents = text.get("text");
                textEl.setContent("");
                Y.each(text.partition(), function(range) {
                    var start = range.start;
                    var end = range.end;
                    var spanId = range.toId();
                    textEl.append("<span id='" + spanId + "'>" + Y.Escape.html(textContents.substring(start, end)).replace("\n", "<br>") + "</span>");
                    Y.on("click", spanClick, "#" + spanId);
                });

                annotationEl.setContent("<h2>Annotations</h2>");
                var names = text.names();
                var nameClick = function(e) {
                    var selected = names[parseInt(e.target.getAttribute("id").replace("name-", ""))].toString();
                    if (e.target.get("checked")) {
                        selectedNames.push(selected);
                        selectedNames.sort();
                    } else {
                        selectedNames = Y.Array.filter(selectedNames, function(n) {
                            return n != selected
                        });
                    }

                    var highlighted = [];
                    Y.each(text.get("annotations"), function(a) {
                        if (Y.Array.some(selectedNames, function(n) { return a.name.toString() == n; })) highlighted.push(a);
                    });


                    var colorScale = d3.scale.category10();
                    Y.each(text.partition(), function(p) {
                        var hc = 0;
                        highlighted = Y.Array.filter(highlighted, function(h) {
                            if (p.overlapsWith(h.range)) hc++;
                            return true; //!(p.precedes(h.range));
                        });
                        Y.one("#" + p.toId()).setStyle("color", hc == 0 ? "black" : colorScale(hc));
                    });

                };
                Y.each(names, function(n, i) {
                    var nameId = "name-" + i;
                    var input = Y.Node.create(Y.Lang.sub("<input type='checkbox' id='{id}' name='{id}' />", { id: nameId }));
                    annotationEl.append(Y.Node.create("<p/>")
                            .append(input)
                            .append("&nbsp;")
                            .append(Y.Lang.sub("<label for='{id}'>{label}</label>", { id: nameId, label: n.toString() })));
                    Y.on("change", nameClick, input);
                });
            });

            // ======================================== Histogram

            text.after("annotationsChange", function(e) {
                var HIST_WIDTH = 300, HIST_HEIGHT = 50;
                var histogramData = [];
                for (var x = 0; x < HIST_WIDTH; x++) histogramData[x] = 0;

                var xScale = d3.scale.linear().domain([0, this.get("text").length]).range([0, HIST_WIDTH]);
                Y.each(text.get("annotations"), function(a) {
                    for (var x = xScale(a.range.start); x < xScale(a.range.end); x++) histogramData[x]++;
                });

                var yScale = d3.scale.linear().domain([0, d3.max(histogramData)]).range([0, HIST_HEIGHT]);
                var colorScale = d3.scale.linear().domain([0, d3.max(histogramData)]).range([0, 5]);

                var histogramEl = Y.one("#histogram");
                histogramEl.append("<h2>Histogram</h2>");

                var histogram = d3.select(Y.Node.getDOMNode(histogramEl))
                        .append("svg:svg")
                        .attr("width", HIST_WIDTH)
                        .attr("height", HIST_HEIGHT);

                histogram.selectAll("rect")
                        .data(histogramData)
                        .enter().append("svg:rect")
                            .attr("x", function(d, i) { return i; })
                            .attr("y", function(d) { return HIST_HEIGHT - yScale(d); })
                            .attr("width", 1)
                            .attr("height", yScale)
                            .attr("fill", function(d, i) { return d3.rgb("mediumslateblue").darker(colorScale(d)); });
            });

            Y.textRepository.getAnnotations(textId, text);
        });
    });
</script>
</@ie.page>