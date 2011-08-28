<@c.page title="REST service result">
<style type="text/css">
.invariant {background: #d3d3d3;}
.variant   {color:#d3d3d3; background: black;}

.hilite {background: orange;}

.match,.baserow th {background: lightcoral;}
.none        {background: LightGoldenRodYellow;}
.addition    {background: lightblue;}
.replacement {background: lightgreen;}
.omission    {background: yellow;}

table {border-style:solid;border-width:1px;border-collapse:collapse;}
td, th {border-style:dotted;border-width:1px;padding:3px;}
th {background:white;}
</style>
	<h1>REST service result</h1>

  Alignment table:
  	
	<table class="alignment">
		<#list alignment.rows as r>
			<tr>
				<th>${r.sigil?html}</th>
				<#list r.iterator() as cell>
					<td><#if cell.empty>&ndash;<#else>${cell.token.content?html}</#if></td>
				</#list>
			</tr>
		</#list>
	</table>
  
 <#-- 
 
  <br/>
  <br/>
  
  Baseless:	(<span class="invariant">INVARIANT</span>, <span class="variant">VARIANT</span>)
	<table>
		<#list alignment.rows as r>
			<tr>
				<th>${r.sigil?html}</th>
				<#list r.iterator() as cell>
					<td align="center" class="${cell.column.state?lower_case}"><#if cell.empty>&ndash;<#else>${cell.token.content?html}</#if></td>
				</#list>
			</tr>
		</#list>
	</table>
	
  -->

 <#-- <p>
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
          <td align="center" class="${cell.getModification(s)?lower_case}"><#if cell.empty>&ndash;<#else>${cell.token.content?html}</#if></td>
        </#list>
      </tr>
    </#list>
  </table>
  <br/>
  </#list>
  -->
  
 <#-- 

  <br/>
  <br/>
     
  Vertex coloring: (hover over cell to see color hexvalue)
  <table>
    <#list alignment.rows as r>
      <tr>
        <th>${r.sigil?html}</th>
        <#list r.iterator() as cell>
          <#assign c = cell.getColor()> 
          <#assign c1 = cell.getColor()?replace("#","")> 
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
	
  -->
  

</@c.page>