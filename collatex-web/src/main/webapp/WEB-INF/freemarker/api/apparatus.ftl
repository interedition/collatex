<@c.page title="REST service result">
	<h1>REST service result</h1>
	
	<table class="alignment">
		<#list rows as r>
			<tr>
				<th>${r['sigil']?html}</th>
				<#list r['cells']	as cell>
					<td>${cell?html}</td>
				</#list>
			</tr>
		</#list>
	</table>
</@c.page>