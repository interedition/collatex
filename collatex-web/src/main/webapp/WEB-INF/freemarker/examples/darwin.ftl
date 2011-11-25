<@c.page title="Darwin">
    <style type="text/css">
        .invariant {background: inherit; }
        .semi_invariant {background: #f5deb3}
        .variant    {background: #f08080; }
    </style>

	<h1>Darwin</h1>

    <ul>
        <li><span class="invariant">invariant</span></li>
        <li><span class="semi_invariant">variant</span></li>
        <li><span class="variant">semi-variant</span></li>
    </ul>

	<#list paragraphs as p>
		<h2>${p_index + 1}.</h2>
		<#if p.witnesses?has_content>
			<table class="alignment">
				<tr><#list p.witnesses as w><th>${w?html}</th></#list></tr>
				<#list p.entries as e>
					<tr>
						<#list p.witnesses as w>
							<td class="${e.state?lower_case}"><#if e.containsWitness(w)><#list e.getPhrase(w) as t>${t.content?html} </#list><#else>&ndash;</#if></td>
						</#list>
					</tr>
				</#list>
			</table>
		</#if>
	</#list>
</@c.page>