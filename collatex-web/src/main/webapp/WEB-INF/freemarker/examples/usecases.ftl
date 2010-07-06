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

[#ftl]
[@c.page title="Use cases"]
	<h1>Use cases</h1>
	
	[#list examples as e]
		<h2>${e_index + 1}.</h2>
		
		[#if e.size() > 0]
			<table>
				[#list e.rows as r]
					<tr>
						<th>${r.sigil?html}</th>
						[#list r.iterator() as cell]
							<td>[#if cell.empty]&ndash;[#else]${cell.token.content?html}[/#if]</td>
						[/#list]
					</tr>
				[/#list]
			</table>
		[/#if]
	[/#list]
[/@c.page]