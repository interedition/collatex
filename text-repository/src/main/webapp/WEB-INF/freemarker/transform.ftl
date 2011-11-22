<#assign title="Transform XML Text #" + metadata.text.id?html maxLength=102400 truncated=(metadata.text.length gt maxLength)/>
<@ie.page title>
    <h1>${title}</h1>

    <div id="text-length" style="text-align: right; font-weight: bold; margin: 1em 0">
        Length: ${metadata.text.length} characters
        <#if truncated>(${maxLength} displayed)</#if>
    </div>

    <div class="yui3-g">
        <div class="yui3-u-1-2">
            <form method="post" id="parse-form">
                <p><input type="submit" value="Transform"> <input type="reset" value="Reset"></p>
                <table>
                    <tr>
                        <td colspan="5" class="right"><label for="removeEmpty">Remove empty annotations:</label></td>
                        <td class="center"><input id="removeEmpty" type="checkbox" checked="checked"></td>
                    </tr>
                    <tr>
                        <td colspan="5" class="right"><label for="transformTEI">Transform TEI annotations:</label></td>
                        <td class="center"><input id="transformTEI" type="checkbox" checked="checked"></td>
                    </tr>
                    <#list names?keys as ns>
                        <tr>
                            <th colspan="6" style="font-size: large; background: inherit" class="right">${ns?html}</th>
                            <#list names[ns]?chunk(10) as nameChunk>
                                <tr>
                                    <th class="left">Local name</th>
                                    <th>Include</th>
                                    <th>Exclude</th>
                                    <th>Line</th>
                                    <th>Container</th>
                                    <th>Notable</th>
                                </tr>
                                <#list nameChunk as localName>
                                    <#assign nameId=("name_" + ns_index + "_" + nameChunk_index + "_" + localName_index)>
                                    <tr>
                                        <td id="${nameId}">
                                            ${localName?html}
                                            <span class="hidden ns">${ns?html}</span>
                                            <span class="hidden ln">${localName?html}</span>
                                        </td>
                                        <td class="center"><input id="${nameId}_included" type="checkbox"></td>
                                        <td class="center"><input id="${nameId}_excluded" type="checkbox"></td>
                                        <td class="center"><input id="${nameId}_lineElements" type="checkbox"></td>
                                        <td class="center"><input id="${nameId}_containerElements" type="checkbox"></td>
                                        <td class="center"><input id="${nameId}_notableElements" type="checkbox"></td>
                                    </tr>
                                </#list>
                            </#list>
                        </tr>
                    </#list>
                </table>
            </form>
        </div>
        <div class="yui3-u-1-2" id="parsed-text">
            &nbsp;
        </div>
    </div>
    <script type="text/javascript">
        var textId = ${metadata.text.id?c};
        YUI().use("interedition-text", "dump", "node", "event", function(Y) {
            Y.on("domready", function() {
                Y.all("input[type='checkbox']").each(function(cb) {
                    var id = cb.get("id");
                    if (id == "removeEmpty" || id == "transformTEI") return;

                    var splitOn = id.lastIndexOf("_");
                    var cbName = id.substring(0, splitOn);
                    var cbType = id.substring(splitOn + 1, id.length);
                    if (cbType == "excluded" || cbType == "included") {
                        Y.on("change", function(e) {
                            var antagonist = Y.one("#" + cbName + "_" + (cbType == "excluded" ? "included" : "excluded"));
                            if (this.get("checked") && antagonist.get("checked")) {
                                antagonist.set("checked", false);
                            }
                        }, cb);
                    }
                });
                Y.on("submit", function(e) {
                    e.preventDefault();
                    var transformConfig = {
                        transformTEI: Y.one("#transformTEI").get("checked"),
                        removeEmpty: Y.one("#removeEmpty").get("checked"),
                        included: [],
                        excluded: [],
                        lineElements: [],
                        containerElements: [],
                        notableElements: []
                    };
                    Y.all("input[type='checkbox']").each(function(cb) {
                        var id = cb.get("id");
                        if (id == "removeEmpty" || id == "transformTEI") return;

                        if (this.get("checked")) {
                            var splitOn = id.lastIndexOf("_");
                            var cbName = id.substring(0, splitOn);
                            var cbType = id.substring(splitOn + 1, id.length);

                            var nameCell = Y.one("#" + cbName);
                            var name = [ nameCell.one(".ns").get("text"), nameCell.one(".ln").get("text") ];
                            transformConfig[cbType].push(name);
                        }
                    });

                    (new Y.interedition.text.Repository({ base: cp })).transform(textId, transformConfig, function(resp) {
                        Y.config.win.location.pathname = cp + "/text/" + resp.id.toString();
                    })
                }, "#parse-form");
            });
        })
    </script>
</@ie.page>