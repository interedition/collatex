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
.hilite      {color:black;background: orange;}
.none        {color: black;}
.match       {color: grey;}
.addition    {color: blue;font-weight:bold;}
.replacement {color: green;font-weight:bold;}
.omission    {color: red;font-weight:bold;}
.baserow {background:lightcyan;}
table {border-style:solid;border-width:1px;border-collapse:collapse;}
td, th {border-style:dotted;border-width:1px;padding:3px;}
th {background:lightblue;}
</style>
	<h1>REST service result</h1>

  Baseless:	
	<table>
		<#list alignment.rows as r>
			<tr>
				<th>${r.sigil?html}</th>
				<#list r.iterator() as cell>
					<td align="center"><#if cell.empty>&ndash;<#else>${cell.token.content?html}</#if></td>
				</#list>
			</tr>
		</#list>
	</table>
	
  <p>
    Modifications:
    <span class="none">NONE</span>
    <span class="match">MATCH</span>
    <span class="addition">ADDITION</span>
    <span class="replacement">REPLACEMENT</span>
    <span class="omission">OMISSION</span>
  </p>

  <#list alignment.sigla as s>
  Base: ${s}
  <table>
    <#list alignment.rows as r>
      <tr<#if r.sigil == s> class="baserow"</#if>>
        <th>${r.sigil?html}</th>
        <#list r.iterator() as cell>
          <td align="center"><span class="${cell.getModification(s)?lower_case}"><#if cell.empty>&ndash;<#else>${cell.token.content?html}</#if></span></td>
        </#list>
      </tr>
    </#list>
  </table>
  <br/>
  </#list>
  
  Vertex coloring: (hover over cell to see color hexvalue)
  <table>
    <#list alignment.rows as r>
      <tr>
        <th>${r.sigil?html}</th>
        <#list r.iterator() as cell>
          <#assign c = cell.getColor(r.sigil)> 
          <#assign c1 = cell.getColor(r.sigil)?replace("#","")> 
          <td align="center" class="c${c1}" onmouseover="hilitematches('c${c1}')" onmouseout="unhilitematches('c${c1}')"><span style="color:${c}" title="${c?upper_case}"><#if cell.empty>&ndash;<#else>${cell.token.content?html}</#if></span></td>
        </#list>
      </tr>
    </#list>
  </table>

<script type="text/javascript" src="http://api.prototypejs.org/javascripts/pdoc/prototype.js"></script>
<script>
function hilitematches(c){
  $$('.'+c).each(function(td) {
    td.addClassName('hilite');
  });
}  
function unhilitematches(c){
  $$('.'+c).each(function(td) {
    td.removeClassName('hilite');
  });
}  
</script>
  

</@c.page>