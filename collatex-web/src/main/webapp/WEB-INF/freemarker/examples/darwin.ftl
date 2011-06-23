<#--

    CollateX - a Java library for collating textual sources,
    for example, to produce an apparatus.

    Copyright (C) 2010 ESF COST Action "Interedition".

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

-->

<@c.page title="Darwin">
<style type="text/css">
.invariant {background: lightgrey;}
.semi_invariant {background: lightgrey;}
<!-- .semi_invariant {color:lightgrey; background: black;} -->
.variant    {background: grey;} 
</style>

	<h1>Darwin</h1>
	(<span class="invariant">INVARIANT</span>, <span class="semi_invariant">SEMI-INVARIANT</span>, <span class="variant">VARIANT</span>)
	
	<#list paragraphs as p>
		<h2>${p_index + 1}.</h2>
		<#if p.witnesses?has_content>
			<table>
				<tr><#list p.witnesses as w><th>${w?html}</th></#list></tr>
				<#list p.entries as e>
					<tr>
						<#list p.witnesses as w>
							<td class="${e.state?lower_case}"><#if e.containsWitness(w)>${e.getPhrase(w).content?html}<#else>&ndash;</#if></td>
						</#list>
					</tr>
				</#list>
			</table>
		</#if>
	</#list>
</@c.page>