<#assign searchQuery=query.query>
<@ie.page "Search">

<#if results?has_content>
    <h1>Search Results:</h1>

    <ol>
        <#list results as r>
            <li>
                <p><a href="${cp}/text/${r.text.id?c}" title="Gehe zu Text #${r.text.id}" style="font-size: large">Text #${r.text.id}</a></p>
                <p><span style="color: #8b0000;">${r.score}%</span>, Type ${r.text.type?html}, Created: ${r.text.created?string?html}, Size: ${r.text.length} character(s)</li></p>
                <p><span style="font-size: small; color: #a9a9a9;">${r.textStart?html}</span> <#if r.text.length gt 1000>[...]</#if></p>
            </li>
        </#list>
    </ol>
</#if>

<script type="text/javascript">
    YUI().use("node", function(Y) {
        Y.on("domready", function() {
            Y.one("#query").focus();
        })
    });
</script>

</@ie.page>