[#ftl]
[@c.page title="Darwin"]
	<h1>Darwin</h1>
	
	[#list paragraphs as p]
		<h2>${p_index + 1}.</h2>
		
		[#if p.sigli?has_content]
			<table>
				<tr>[#list p.sigli as s]<th>${s?html}</th>[/#list]</tr>
				[#list p.entries as e]
					<tr>
						[#list p.sigli as s]
							<td>[#if e.containsWitness(s)]${e.getPhrase(s).content?html}[#else]&ndash;[/#if]</td>
						[/#list]
					</tr>
				[/#list]
			</table>
		[/#if]
	[/#list]
[/@c.page]