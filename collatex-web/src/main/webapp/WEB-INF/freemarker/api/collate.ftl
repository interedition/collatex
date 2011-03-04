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
	
	<p>For the example witnesses from the Input section above, the output in GraphML format will look as follows:</p>
	<pre><![CDATA[<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
    <key attr.name="number" attr.type="int" for="node" id="d1"/>
    <key attr.name="token" attr.type="string" for="node" id="d0"/>
    <key attr.name="A" attr.type="string" for="edge" id="w0"/>
    <key attr.name="B" attr.type="string" for="edge" id="w1"/>
    <key attr.name="C" attr.type="string" for="edge" id="w2"/>
    <key attr.name="D" attr.type="string" for="edge" id="w3"/>
    <graph edgedefault="directed" id="0">
        <node id="0">
            <data key="d0">#</data>
            <data key="d1">0</data>
        </node>
        <node id="1">
            <data key="d0">a</data>
            <data key="d1">1</data>
        </node>
        <node id="2">
            <data key="d0">black</data>
            <data key="d1">2</data>
        </node>
        <node id="3">
            <data key="d0">striped</data>
            <data key="d1">3</data>
        </node>
        <node id="4">
            <data key="d0">cat</data>
            <data key="d1">4</data>
        </node>
        <node id="5">
            <data key="d0">in</data>
            <data key="d1">5</data>
        </node>
        <node id="6">
            <data key="d0">a</data>
            <data key="d1">6</data>
        </node>
        <node id="7">
            <data key="d0">black</data>
            <data key="d1">7</data>
        </node>
        <node id="8">
            <data key="d0">white</data>
            <data key="d1">8</data>
        </node>
        <node id="9">
            <data key="d0">basket</data>
            <data key="d1">9</data>
        </node>
        <node id="10">
            <data key="d0">#</data>
            <data key="d1">10</data>
        </node>
        <edge id="0" source="0" target="1">
            <data key="w0">A</data>
            <data key="w1">B</data>
            <data key="w2">C</data>
            <data key="w3">D</data>
        </edge>
        <edge id="1" source="1" target="2">
            <data key="w0">A</data>
            <data key="w1">B</data>
        </edge>
        <edge id="2" source="1" target="3">
            <data key="w2">C</data>
            <data key="w3">D</data>
        </edge>
        <edge id="3" source="2" target="4">
            <data key="w0">A</data>
            <data key="w1">B</data>
        </edge>
        <edge id="4" source="3" target="4">
            <data key="w2">C</data>
            <data key="w3">D</data>
        </edge>
        <edge id="5" source="4" target="5">
            <data key="w0">A</data>
            <data key="w1">B</data>
            <data key="w2">C</data>
            <data key="w3">D</data>
        </edge>
        <edge id="6" source="5" target="6">
            <data key="w0">A</data>
            <data key="w1">B</data>
            <data key="w2">C</data>
            <data key="w3">D</data>
        </edge>
        <edge id="7" source="6" target="7">
            <data key="w0">A</data>
            <data key="w1">B</data>
            <data key="w2">C</data>
        </edge>
        <edge id="8" source="6" target="8">
            <data key="w3">D</data>
        </edge>
        <edge id="9" source="7" target="9">
            <data key="w0">A</data>
            <data key="w1">B</data>
            <data key="w2">C</data>
        </edge>
        <edge id="10" source="8" target="9">
            <data key="w3">D</data>
        </edge>
        <edge id="11" source="9" target="10">
            <data key="w0">A</data>
            <data key="w1">B</data>
            <data key="w2">C</data>
            <data key="w3">D</data>
        </edge>
    </graph>
</graphml>]]></pre>
</@c.page>