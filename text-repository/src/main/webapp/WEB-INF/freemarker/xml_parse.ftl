<#assign title="Parse XML Text #" + text.id?html maxLength=102400 truncated=(text.length gt maxLength)/>
<@ie.page title>
    <h1>${title}</h1>

    <div id="text-length" style="text-align: right; font-weight: bold; margin: 1em 0">
        Length: ${text.length} characters
        <#if truncated>(${maxLength} displayed)</#if>
    </div>

    <form method="post">
        <table style="margin: 0 auto">
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
                            <#assign nameId=(ns_index + "_" + nameChunk_index + "_" + localName_index)>
                            <tr>
                                <td>${localName?html}</td>
                                <td class="center"><input id="${nameId}_included" type="checkbox" checked="checked"></td>
                                <td class="center"><input id="${nameId}_excluded" type="checkbox"></td>
                                <td class="center"><input id="${nameId}_line" type="checkbox"></td>
                                <td class="center"><input id="${nameId}_container" type="checkbox"></td>
                                <td class="center"><input id="${nameId}_notable" type="checkbox"></td>
                            </tr>
                        </#list>
                    </#list>
                </tr>
            </#list>
        </table>
    </form>
</@ie.page>