<#assign searchQuery=query.query>
<@ie.page "Search">
    <#if results?has_content>
    <ol class="search-results">
        <#list results as r>
            <li>
                <p>
                    <img class="icon" src="${cp}/static/icon/text_${r.text.type?lower_case}.png" alt="${r.text.type?html}">
                    <a href="${cp}/text/${r.text.id?c}" title="Gehe zu Text #${r.text.id}" style="font-size: large">${r.description?html}</a>
                </p>

                <p>
                    <span style="color: #8b0000;">${r.score}%</span>,
                    Size: ${r.text.length} character(s)
                    Created: ${r.created?string?html},
                    Updated: ${r.updated?string?html},
                </p>

                <#if r.summary?has_content><p>${r.summary?html?replace("\n", "<br>")}</p></#if>
            </li>
        </#list>
    </ol>
    <#else>
        <p>No results!</p>
    </#if>
</@ie.page>