[#ftl]
[@c.page title="REST service result"]
	<h1>REST service result</h1>
	
	<table>
		[#list alignment.rows as r]
			<tr>
				<th>${r.sigil?html}</th>
				[#list r.iterator() as cell]
					<td>[#if cell.empty]&ndash;[#else]${cell.token.content?html}[/#if]</td>
				[/#list]
			</tr>
		[/#list]
	</table>
[/@c.page]