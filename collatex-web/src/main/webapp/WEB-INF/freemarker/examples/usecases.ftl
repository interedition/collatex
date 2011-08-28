<@c.page title="Use cases">
	<h1>Use cases</h1>
	
	<#list examples as e>
		<h2>${e_index + 1}.</h2>
		
		<#if e.size() gt 0>
			<table class="alignment">
				<#list e.rows as r>
					<tr>
						<th>${r.sigil?html}</th>
						<#list r.iterator() as cell>
							<td><#if cell.empty>&ndash;<#else>${cell.token.content?html}</#if></td>
						</#list>
					</tr>
				</#list>
			</table>
		</#if>
	</#list>
</@c.page>