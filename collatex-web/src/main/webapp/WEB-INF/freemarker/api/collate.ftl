<@c.page title="REST service">
	<h1>REST service</h1>
	
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
	<pre>Content-Type: application/json;charset=UTF-8
	
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
	<pre>{
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
	<pre>{"alignment":[        
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
	
	<pre><![CDATA[<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<collatex:apparatus xmlns:collatex="http://interedition.eu/collatex/ns/1.0" xmlns="http://www.tei-c.org/ns/1.0">A <app>
        <rdg wit="#A">nice black</rdg>
        <rdg wit="#B">white</rdg>
    </app> <app>
        <rdg wit="#A">cat.</rdg>
        <rdg wit="#B">kitten</rdg>
    </app> <app>
        <rdg wit="#A"/>
        <rdg wit="#B">in a basket.</rdg>
    </app>
</collatex:apparatus>]]></pre>
	
	<h3>Example: Variant graph in GraphML format</h3>
	
	<p>For the example witnesses from the Input section above, the output in GraphML format will look as follows.  
	The 'identical' key, not used here, is set for those nodes that are transposed duplicates of other nodes.
	</p>
	<pre><![CDATA[<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
    <key attr.name="number" attr.type="int" for="node" id="d1"/>
    <key attr.name="token" attr.type="string" for="node" id="d0"/>
    <key attr.name="identical" attr.type="string" for="node" id="d2"/>
    <key attr.name="A" attr.type="string" for="edge" id="w0"/>
    <key attr.name="B" attr.type="string" for="edge" id="w1"/>
    <key attr.name="C" attr.type="string" for="edge" id="w2"/>
    <key attr.name="D" attr.type="string" for="edge" id="w3"/>
    <graph edgedefault="directed" id="g0" parse.edgeids="canonical" parse.edges="12" parse.nodeids="canonical" parse.nodes="11" parse.order="nodesfirst">
        <node id="n0">
            <data key="d0">#</data>
            <data key="d1">n0</data>
        </node>
        <node id="n1">
            <data key="d0">a</data>
            <data key="d1">n1</data>
        </node>
        <node id="n2">
            <data key="d0">black</data>
            <data key="d1">n2</data>
        </node>
        <node id="n3">
            <data key="d0">striped</data>
            <data key="d1">n3</data>
        </node>
        <node id="n4">
            <data key="d0">cat</data>
            <data key="d1">n4</data>
        </node>
        <node id="n5">
            <data key="d0">in</data>
            <data key="d1">n5</data>
        </node>
        <node id="n6">
            <data key="d0">a</data>
            <data key="d1">n6</data>
        </node>
        <node id="n7">
            <data key="d0">black</data>
            <data key="d1">n7</data>
        </node>
        <node id="n8">
            <data key="d0">white</data>
            <data key="d1">n8</data>
        </node>
        <node id="n9">
            <data key="d0">basket</data>
            <data key="d1">n9</data>
        </node>
        <node id="n10">
            <data key="d0">#</data>
            <data key="d1">n10</data>
        </node>
        <edge id="e0" source="n0" target="n1">
            <data key="w0">A</data>
            <data key="w3">D</data>
            <data key="w2">C</data>
            <data key="w1">B</data>
        </edge>
        <edge id="e1" source="n1" target="n2">
            <data key="w0">A</data>
            <data key="w1">B</data>
        </edge>
        <edge id="e2" source="n1" target="n3">
            <data key="w3">D</data>
            <data key="w2">C</data>
        </edge>
        <edge id="e3" source="n2" target="n4">
            <data key="w0">A</data>
            <data key="w1">B</data>
        </edge>
        <edge id="e4" source="n3" target="n4">
            <data key="w3">D</data>
            <data key="w2">C</data>
        </edge>
        <edge id="e5" source="n4" target="n5">
            <data key="w0">A</data>
            <data key="w3">D</data>
            <data key="w2">C</data>
            <data key="w1">B</data>
        </edge>
        <edge id="e6" source="n5" target="n6">
            <data key="w0">A</data>
            <data key="w3">D</data>
            <data key="w2">C</data>
            <data key="w1">B</data>
        </edge>
        <edge id="e7" source="n6" target="n7">
            <data key="w0">A</data>
            <data key="w2">C</data>
            <data key="w1">B</data>
        </edge>
        <edge id="e8" source="n6" target="n8">
            <data key="w3">D</data>
            <data key="w2">C</data>
        </edge>
        <edge id="e3" source="n2" target="n4">
            <data key="w0">A</data>
            <data key="w1">B</data>
        </edge>
        <edge id="e4" source="n3" target="n4">
            <data key="w3">D</data>
            <data key="w2">C</data>
        </edge>
        <edge id="e5" source="n4" target="n5">
            <data key="w0">A</data>
            <data key="w3">D</data>
            <data key="w2">C</data>
            <data key="w1">B</data>
        </edge>
        <edge id="e6" source="n5" target="n6">
            <data key="w0">A</data>
            <data key="w3">D</data>
            <data key="w2">C</data>
            <data key="w1">B</data>
        </edge>
        <edge id="e7" source="n6" target="n7">
            <data key="w0">A</data>
            <data key="w2">C</data>
            <data key="w1">B</data>
        </edge>
        <edge id="e8" source="n6" target="n8">
            <data key="w3">D</data>
        </edge>
        <edge id="e9" source="n7" target="n9">
            <data key="w0">A</data>
            <data key="w2">C</data>
            <data key="w1">B</data>
        </edge>
        <edge id="e10" source="n8" target="n9">
            <data key="w3">D</data>
        </edge>
        <edge id="e11" source="n9" target="n10">
            <data key="w0">A</data>
            <data key="w3">D</data>
            <data key="w2">C</data>
            <data key="w1">B</data>
        </edge>
    </graph>
</graphml>]]></pre>
</@c.page>