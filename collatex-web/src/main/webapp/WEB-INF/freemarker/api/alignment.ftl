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

<@c.page title="REST service result">
<style type="text/css">
.none        {color: black;}
.match       {color: grey;}
.addition    {color: red;}
.omission    {color: blue;}
.replacement {color: purple;}
.baserow {background:lightcyan;}
</style>
	<h1>REST service result</h1>

  Baseless:	
	<table border="1">
		<#list alignment.rows as r>
			<tr>
				<th>${r.sigil?html}</th>
				<#list r.iterator() as cell>
					<#if cell.empty><td align="center">&ndash;</td><#else><td>${cell.token.content?html}</td></#if>
				</#list>
			</tr>
		</#list>
	</table>
	
  <p>Modifications:
  <span class="none">NONE</span>
  <span class="match">MATCH</span>
  <span class="addition">ADDITION</span>
  <span class="omission">OMISSION</span>
  <span class="replacement">REPLACEMENT</span></p>

  <#list alignment.sigla as s>
  Base: ${s}
  <table border="1">
    <#list alignment.rows as r>
      <tr<#if r.sigil == s> class="baserow"</#if>>
        <th>${r.sigil?html}</th>
        <#list r.iterator() as cell>
          <#if cell.empty><td align="center">&ndash;</td><#else><td><span class="${cell.getModification(s)?lower_case}">${cell.token.content?html}</span></td></#if>
        </#list>
      </tr>
    </#list>
  </table>
  <br/>
  </#list>

</@c.page>