[#ftl]
[@c.page title="REST service result"]
	<h1>REST service result</h1>

[#list graph.iterator() as vertex]
	${vertex.normalized}<br/>
[/#list]
	

[/@c.page]