<#assign header>
  <link href="http://alexgorbatchev.com/pub/sh/current/styles/shThemeDefault.css" rel="stylesheet" type="text/css">
  <script src="http://alexgorbatchev.com/pub/sh/current/scripts/shCore.js" type="text/javascript"></script>
  <script src="http://alexgorbatchev.com/pub/sh/current/scripts/shBrushXml.js" type="text/javascript"></script>
  <script src="http://alexgorbatchev.com/pub/sh/current/scripts/shBrushJScript.js" type="text/javascript"></script>
</#assign>
<@ie.page title="CollateX REST API" header=header>
	<p>
	This is the REST service of CollateX.
	To call it, you can post witness data as specified below and get the collation result back in a number of formats.
	</p>
		
	<h2>Input</h2>
	
	<p>
	The service is callable via <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html" title="RFC">HTTP POST requests</a>. 
	It expects witness data formatted in <a href="http://json.org/" title="Website">JavaScript Object Notation (JSON)</a> as the request body;
	accordingly the content type of the request must be set to <code>application/json</code>.
	</p>
	
	<p>
	A typical request’s content might look as follows:
	</p>
	
	<p>
	<pre class="brush: js">Content-Type: application/json;charset=UTF-8
	
{
	"witnesses" : [
	        {"id" : "A", "content" : "A black cat in a black basket" }, 
	        {"id" : "B", "content" : "A black cat in a black basket" },
	        {"id" : "C", "content" : "A striped cat in a black basket" },
	        {"id" : "D", "content" : "A striped cat in a white basket" }
	        ]
}</pre>
	</p> 
	
	<p>
	The root object in the body must contain a property <code>witnesses</code>, which contains a list of witness objects in turn.
	Each witness object has to contain a unique identifier in the property <code>id</code>.
	Besides the object can either contain the textual content of the witness as a string property named <code>content</code> (as shown above).
	The other option is a pre-tokenized witness, that is comprised of list of tokens notated as follows:
	</p>
	
	<p>
	<pre class="brush: js">{
        "witnesses" : [
                {"id" : "A", "tokens" : [
                        { "t" : "A" },
                        { "t" : "black" },
                        { "t" : "cat" }
                        ]}, 
                {"id" : "B", "tokens" : [
                        { "t" : "A" },
                        { "t" : "white" },
                        { "t" : "kitten.", "n" : "cat" }
                        ]}
                ]
}</pre>
	</p>
	
	<p>
	Each token object has to contain a property <code>t</code>, which contains the token itself.
	Optionally a “normalized” version of the token can be provided in the property <code>n</code>.
	It can be any kind of alternate version, the collator should use in precendence over the original token while matching witnesses.
	Apart from these two known properties and two reserved ones (<code>position</code> and <code>sigle</code>), token objects can contain further properties, which will not be interpreted by the collator but just passed through in the output. 
	</p>
	
	<h2>Output</h2>
	
	<p>
	The output format of the collator, represented in the response to the HTTP POST request, can be chosen via the <code>Accept</code> HTTP header.
	The following MIME types aka. output formats are supported:
	</p>
	
	<dl>
		<dt><strong>application/json</strong>:</dt>
		<dd>the alignment of the witnesses, represented in JSON,</dd>
		<dt><strong>application/xml</strong>:</dt>
		<dd>the collation result as a critical apparatus, encoded in <a href="http://www.tei-c.org/release/doc/tei-p5-doc/en/html/TC.html" title="TEI website">TEI P5 parallel segmentation mode</a>,</dd>
		<dt><strong>application/graphml+xml</strong>:</dt>
		<dd>the variant graph, represented in <a href="http://graphml.graphdrawing.org/">GraphML format</a>, or</dd>		
		<dt><span style="font-style: italic">per default:</span></dt>
		<dd>a HTML representation of the alignment in tabular form.</dd>		
	</dl>
	
	<h3>Example: Alignment table in JSON</h3>
	
	<p>The indentation is added for readability and not included in the output. Gaps in the table are represented by <code>null</code>.</p>
	<pre class="brush: js">{"alignment":[
	{"witness":"A","tokens":[                
		{"t":"A","n":"a"},                
		{"t":"nice","n":"nice"},                
		{"t":"black","n":"black"},                
		{"t":"cat.","n":"cat"},                
		null,                
		null,                
		null]},        
	{"witness":"B","tokens":[                
		{"t":"A","n":"a"},                
		{"t":"white","n":"white"},                
		null,                
		{"t":"kitten","n":"cat"},                
		{"t":"in","n":"in"},
                {"t":"a","n":"a"},
                {"t":"basket.","n":"basket"}]}
        ]
}</pre>
	
	<h3>Example: Critical apparatus in TEI P5</h3>
	
	<pre class="brush: xml">&lt;?xml version="1.0" encoding="UTF-8" standalone="no"?>
&lt;collatex:apparatus xmlns:collatex="http://interedition.eu/collatex/ns/1.0" xmlns="http://www.tei-c.org/ns/1.0">A &lt;app>
        &lt;rdg wit="#A">nice black&lt;/rdg>
        &lt;rdg wit="#B">white&lt;/rdg>
    &lt;/app> &lt;app>
        &lt;rdg wit="#A">cat.&lt;/rdg>
        &lt;rdg wit="#B">kitten&lt;/rdg>
    &lt;/app> &lt;app>
        &lt;rdg wit="#A"/>
        &lt;rdg wit="#B">in a basket.&lt;/rdg>
    &lt;/app>
&lt;/collatex:apparatus></pre>
	
	<h3>Example: Variant graph in GraphML format</h3>
	
	<p>For the example witnesses from the Input section above, the output in GraphML format will look as follows.  
	The 'identical' key, not used here, is set for those nodes that are transposed duplicates of other nodes.
	</p>
	<pre class="brush: xml">&lt;?xml version="1.0" encoding="UTF-8" standalone="no"?>
&lt;graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
    &lt;key attr.name="number" attr.type="int" for="node" id="d1"/>
    &lt;key attr.name="token" attr.type="string" for="node" id="d0"/>
    &lt;key attr.name="identical" attr.type="string" for="node" id="d2"/>
    &lt;key attr.name="A" attr.type="string" for="edge" id="w0"/>
    &lt;key attr.name="B" attr.type="string" for="edge" id="w1"/>
    &lt;key attr.name="C" attr.type="string" for="edge" id="w2"/>
    &lt;key attr.name="D" attr.type="string" for="edge" id="w3"/>
    &lt;graph edgedefault="directed" id="g0" parse.edgeids="canonical" parse.edges="12" parse.nodeids="canonical" parse.nodes="11" parse.order="nodesfirst">
        &lt;node id="n0">
            &lt;data key="d0">#&lt;/data>
            &lt;data key="d1">n0&lt;/data>
        &lt;/node>
        &lt;node id="n1">
            &lt;data key="d0">a&lt;/data>
            &lt;data key="d1">n1&lt;/data>
        &lt;/node>
        &lt;node id="n2">
            &lt;data key="d0">black&lt;/data>
            &lt;data key="d1">n2&lt;/data>
        &lt;/node>
        &lt;node id="n3">
            &lt;data key="d0">striped&lt;/data>
            &lt;data key="d1">n3&lt;/data>
        &lt;/node>
        &lt;node id="n4">
            &lt;data key="d0">cat&lt;/data>
            &lt;data key="d1">n4&lt;/data>
        &lt;/node>
        &lt;node id="n5">
            &lt;data key="d0">in&lt;/data>
            &lt;data key="d1">n5&lt;/data>
        &lt;/node>
        &lt;node id="n6">
            &lt;data key="d0">a&lt;/data>
            &lt;data key="d1">n6&lt;/data>
        &lt;/node>
        &lt;node id="n7">
            &lt;data key="d0">black&lt;/data>
            &lt;data key="d1">n7&lt;/data>
        &lt;/node>
        &lt;node id="n8">
            &lt;data key="d0">white&lt;/data>
            &lt;data key="d1">n8&lt;/data>
        &lt;/node>
        &lt;node id="n9">
            &lt;data key="d0">basket&lt;/data>
            &lt;data key="d1">n9&lt;/data>
        &lt;/node>
        &lt;node id="n10">
            &lt;data key="d0">#&lt;/data>
            &lt;data key="d1">n10&lt;/data>
        &lt;/node>
        &lt;edge id="e0" source="n0" target="n1">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w3">D&lt;/data>
            &lt;data key="w2">C&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
        &lt;edge id="e1" source="n1" target="n2">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
        &lt;edge id="e2" source="n1" target="n3">
            &lt;data key="w3">D&lt;/data>
            &lt;data key="w2">C&lt;/data>
        &lt;/edge>
        &lt;edge id="e3" source="n2" target="n4">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
        &lt;edge id="e4" source="n3" target="n4">
            &lt;data key="w3">D&lt;/data>
            &lt;data key="w2">C&lt;/data>
        &lt;/edge>
        &lt;edge id="e5" source="n4" target="n5">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w3">D&lt;/data>
            &lt;data key="w2">C&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
        &lt;edge id="e6" source="n5" target="n6">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w3">D&lt;/data>
            &lt;data key="w2">C&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
        &lt;edge id="e7" source="n6" target="n7">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w2">C&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
        &lt;edge id="e8" source="n6" target="n8">
            &lt;data key="w3">D&lt;/data>
            &lt;data key="w2">C&lt;/data>
        &lt;/edge>
        &lt;edge id="e3" source="n2" target="n4">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
        &lt;edge id="e4" source="n3" target="n4">
            &lt;data key="w3">D&lt;/data>
            &lt;data key="w2">C&lt;/data>
        &lt;/edge>
        &lt;edge id="e5" source="n4" target="n5">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w3">D&lt;/data>
            &lt;data key="w2">C&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
        &lt;edge id="e6" source="n5" target="n6">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w3">D&lt;/data>
            &lt;data key="w2">C&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
        &lt;edge id="e7" source="n6" target="n7">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w2">C&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
        &lt;edge id="e8" source="n6" target="n8">
            &lt;data key="w3">D&lt;/data>
        &lt;/edge>
        &lt;edge id="e9" source="n7" target="n9">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w2">C&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
        &lt;edge id="e10" source="n8" target="n9">
            &lt;data key="w3">D&lt;/data>
        &lt;/edge>
        &lt;edge id="e11" source="n9" target="n10">
            &lt;data key="w0">A&lt;/data>
            &lt;data key="w3">D&lt;/data>
            &lt;data key="w2">C&lt;/data>
            &lt;data key="w1">B&lt;/data>
        &lt;/edge>
    &lt;/graph>
&lt;/graphml></pre>

    <script type="text/javascript">
      SyntaxHighlighter.defaults['toolbar'] = false;
      SyntaxHighlighter.all()
    </script>
</@ie.page>