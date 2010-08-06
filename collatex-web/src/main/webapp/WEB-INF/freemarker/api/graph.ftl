[#ftl]
[@c.page title="REST service result" head='
<base url="http://localhost:8080"/>
<link type="text/css" href="http://localhost:8080${ctx}/css/graphvisualisation.css" rel="stylesheet" />
<script language="javascript" type="text/javascript" src="http://localhost:8080${ctx}/js/jit-yc.js"></script>
<script language="javascript" type="text/javascript" src="http://localhost:8080${ctx}/js/graphvisualisation.js"></script>
']

<h1>REST service result</h1>
	
<div id="container"> 
  <div id="left-container"> 
		[#list graph.iterator() as vertex]
			${vertex.normalized}<br/>
		[/#list]
    <div id="id-list"></div> 
  </div> 
 
  <div id="center-container"> 
    <div id="infovis"></div>    
  </div> 
 
  <div id="right-container"> 
    <div id="inner-details"></div> 
  </div> 
 
  <div id="log"></div> 
</div> 
	
<script language="javascript" type="text/javascript">
  var json = [
		[#list graph.iterator() as vertex]
    {
      "id": "${vertex}",
      "name": "${vertex.normalized}",
      "data": { "$color": "#83548B", "$type": "circle", "$dim": 10 },
      "adjacencies": [
        [#list graph.outgoingEdgesOf(vertex) as edge]
        { "nodeFrom": "${vertex}", "nodeTo": "${edge.endVertex}", "data": { "$color": "#557EAA" } },
        [/#list]
      ]
    },
		[/#list]
  ];

  showForcedDirected(json);
</script>

[/@c.page]