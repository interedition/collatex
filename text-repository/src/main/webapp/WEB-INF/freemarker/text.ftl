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
            <div id="text-contents" style="margin: 1em; padding: 1em; font-size: 125%">&nbsp;</div>
        <#elseif text.type == "XML">
            <textarea style="width: 95%; height: 500px" readonly="readonly">${textContents?html}</textarea>
        </#if>
    </div>
    <div class="yui3-u-1-3" style="color: #999999">
        <#if annotationNames?has_content>
            <h2>Annotations</h2>
            <ol>
                <#list annotationNames as a>
                    <li>${"{" + (a.namespaceURI?default("")?string + "}" + a.localName)?html}</li></#list>
            </ol>
        </#if>
    </div>
</div>

<p style="font-size: small; color: #dcdcdc;">SHA-512: ${text.digest?html}</p>

<script type="text/javascript">
    var textId = ${text.id?c};
    var textContents = "${textContents?js_string}";
    var textType = "${text.type?js_string}";

    YUI().use("node", "event", "dump", "escape", "interedition-text-repository", function(Y) {
        Y.on("domready", function() {
            var textEl = Y.one("#text-contents");
            if (textEl) {
                function spanClick(e) {
                    alert(e.target.getAttribute("id"));
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
                        textEl.append("<span id='" + spanId  + "'>" + Y.Escape.html(textContents.substring(start, end)).replace("\n", "<br>") + "</span>");
                        Y.on("click", spanClick, "#" + spanId);

                    });
                });

                Y.textRepository.getAnnotations(textId, text);
            }
        });
    });
</script>
</@ie.page>