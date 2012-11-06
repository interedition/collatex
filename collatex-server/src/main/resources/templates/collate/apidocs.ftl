<#assign header>
<link href="${cp}/static/google-code-prettify/prettify.css" type="text/css" rel="stylesheet" />
<link href="${cp}/static/google-code-prettify/prettify-sunburst-theme.css" type="text/css" rel="stylesheet" />
<script src="${cp}/static/google-code-prettify/prettify.js" type="text/javascript"></script>
<style type="text/css">
  #rest-apidocs, #js-apidocs { padding: 1em }
</style>
</#assign>
<@ie.page title="CollateX RESTful/JavaScript API" header=header>
<p>
  This page documents the
  <a href="http://en.wikipedia.org/wiki/Application_programming_interface" title="Wikipedia Page">Application Programming Interface (API)</a>
  of CollateX via which you can provide textual versions (&ldquo;witnesses&rdquo;) to be compared and get the collation result back in a number of formats.
</p>

<div class="yui3-g">
    <div class="yui3-u-1-2">
      <div id="rest-apidocs">
        <h2>HTTP-based RESTful API</h2>

        <p>
          The CollateX service is callable via
          <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html" title="RFC">HTTP POST requests</a> to
          <a href="${cp}/collate" title="REST-API Endpoint">${cp}/collate</a>.</p>

        <h3>Input</h3>

        <p>It expects <strong>input</strong> formatted in <a href="http://json.org/" title="Website">JavaScript Object Notation (JSON)</a> as the request body;
          accordingly the content type of the HTTP request must be set to <code>application/json</code> by the client.</p>

        <p>
          An exemplary request’s body looks as follows:
        </p>

	<pre class="prettyprint">{
  "witnesses" : [ {
      "id" : "A",
      "content" : "A black cat in a black basket"
    }, {
      "id" : "B",
      "content" : "A black cat in a black basket"
    }, {
      "id" : "C",
      "content" : "A striped cat in a black basket"
    }, {
      "id" : "D",
      "content" : "A striped cat in a white basket"
    } ]
}</pre>

        <p>
          Each request body contains a single object. It has a required property <code>witnesses</code>, which contains a list of objects in turn.
          Each object represents a text version to be collated and has to contain a unique identifier in the property <code>id</code>.
          Besides the identifier each object can either contain the textual content of the witness as a string property named <code>content</code> (as shown above).
          The other option is a pre-tokenized witness, that is comprised of list of tokens notated as follows:
        </p>

        <p>
	<pre class="prettyprint">{
  "witnesses" : [ {
      "id" : "A",
      "tokens" : [
          { "t" : "A" },
          { "t" : "black" },
          { "t" : "cat" } ]
    }, {
      "id" : "B",
      "tokens" : [
          { "t" : "A" },
          { "t" : "white" },
          { "t" : "kitten.", "n" : "cat" } ]
    } ]
}</pre>
        </p>

        <p>
          Each token object has to contain a property <code>t</code>, which contains the token content itself.
          Optionally a “normalized” version of the token can be provided in the property <code>n</code>.
          It can be any kind of alternate reading the collator should use in precendence over the original token content during alignment.
          Apart from these 2&nbsp;known properties, token objects can contain an arbitrary number of additional properties,
          which will not be interpreted by the collator but just be passed through and reappear in the output.
        </p>

        <p>… Choose alignment algorithm … </p>

        <pre class="prettyprint">{
  "witnesses": [ … ],
  "algorithm": "dekker"
}</pre>
        
        <p>Available algorithms …</p>
        
        <table>
          <tr>
            <th>dekker</th>
            <td><a href="http://www.huygens.knaw.nl/en/dekker/" title="Homepage">Ronald Haentjens Dekker's</a> algorithm, includes transposition detection …</td>
          </tr>
          <tr>
            <th>dekker-experimental</th>
            <td>Experimental version of Dekker's algorithm with enhanced token matching, alpha-level quality …</td>
          </tr>
          <tr>
            <th>needleman-wunsch</th>
            <td>Adaptation of <a href="http://en.wikipedia.org/wiki/Needleman%E2%80%93Wunsch_algorithm" title="Wikipedia">Needleman-Wunsch's</a> global alignment algorithm …</td>
          </tr>
        </table>

        <p>Choose token matching function …</p>

        <pre class="prettyprint">{
  "witnesses": [ … ],
  "algorithm": "dekker",
  "tokenComparator": { type: "equality" }
}</pre>

        <p>Default: exact matching … Alternative: fuzzy matching via <a href="http://en.wikipedia.org/wiki/Levenshtein_distance" title="Wikipedia">Levenshtein distance</a> threshold:</p>

        <pre class="prettyprint">{
  "witnesses": [ … ],
  "algorithm": "dekker",
  "tokenComparator": {
    "type": "levenshtein",
    "distance": 2
  }
}</pre>
        
        <p>Property <code>distance</code> contains maximal edit distance to be deemed a match …</p>

        <p>More options (algorithms/ matching functions) added in forthcoming releases …</p>

        <h3>Output</h3>

        <p>
          The output format of the collator, contained in the response to an HTTP POST request, can be chosen via
          an <code>Accept</code> HTTP header in the request. The following output formats are supported:
        </p>

        <table>
          <tr>
            <th>application/json</th>
            <td><em>(per default)</em> the tabular alignment of the witnesses' tokens, represented in JSON</td>
          </tr>
          <tr>
            <th>application/tei+xml</th>
            <td>the collation result as a list of critical apparatus entries, encoded in <a href="http://www.tei-c.org/release/doc/tei-p5-doc/en/html/TC.html" title="TEI website">TEI P5 parallel segmentation mode</a></td>
          </tr>
          <tr>
            <th>application/graphml+xml</th>
            <td>the variant graph, represented in <a href="http://graphml.graphdrawing.org/">GraphML format</a></td>
          </tr>
          <tr>
            <th>text/plain</th>
            <td>the variant graph, represented in <a href="http://www.graphviz.org/doc/info/lang.html" title="Graphviz' DOT Language Specification">Graphviz' DOT Language</a></td>
          </tr>
          <tr>
            <th>image/svg+xml</th>
            <td>the variant graph, rendered as an <a href="http://www.w3.org/Graphics/SVG/" title="W3C SVG Homepage">SVG</a> vector graphics document</td>
          </tr>
        </table>

        <h3>Output Format: Alignment Table in JSON</h3>

        <p>The tabular output format, resembling matrices commonly used in <a href="http://en.wikipedia.org/wiki/Sequence_alignment" title="Wikipedia">sequence alignment</a> results, looks as follows (indentation/whitespace added for easier readability):</p>

	<pre class="prettyprint">{
    "rows": 3,
    "columns": 2,
    "sigils": ["A", "B"],
    "table":[
      [
        [ {"t":"A"} ],
        [ {"t":"A"} ]
      ],[
        [ {"t":"black"} ],
        [ {"t":"white"} ]
      ], [
        [ {"t":"cat"} ],
        [ {"t":"kitten.", "n":"cat"} ]
      ]
    ]
}</pre>

        <p>First 3&nbsp;properties: metadata … dimensions of table; <code>rows</code> aka. number of aligned segments; <code>columns</code> aka. number of witnesses;
          <code>sigils</code> contains ordered set of witness identifiers, maps to order of segments in table …</p>

        <p>Each row: matching segments, each colum/cell: the segment of a witness aka. a list of tokens comprising this segment or <code>null</code> in case of a gap …</p>

        <h3>Output Format: TEI-P5-encoded Critical Apparatus</h3>

	      <pre class="prettyprint">&lt;?xml version='1.0' encoding='UTF-8'?&gt;
&lt;cx:apparatus
  xmlns:cx="http://interedition.eu/collatex/ns/1.0"
  xmlns="http://www.tei-c.org/ns/1.0"&gt;
    A
    &lt;app&gt;
      &lt;rdg wit="A"&gt;black&lt;/rdg&gt;
      &lt;rdg wit="B"&gt;white&lt;/rdg&gt;
    &lt;/app&gt;
    &lt;app&gt;
      &lt;rdg wit="A"&gt;cat&lt;/rdg&gt;
      &lt;rdg wit="B"&gt;kitten.&lt;/rdg&gt;
    &lt;/app&gt;
&lt;/cx:apparatus&gt;</pre>

        <p>For further examples, take a look at sample output from the
          <a href="${cp}/collate/console" title="CollateX Console">web console</a>.</p>

        <h3>Output Format: Variant Graph in GraphML</h3>

        <p>The GraphML-formatted output of a variant graph is suitable for import of (possibly larger) graphs in tools
          for complex graph analysis and visualization, e. g. <a href="http://gephi.org/" title="Homepage">Gephi</a>.
          For an example GraphML document, take a look at sample output from the
        <a href="${cp}/collate/console" title="CollateX Console">web console</a>.</p>

        <h3>Output Format: Variant Graph in GraphViz' DOT</h3>

        <pre class="prettyprint">digraph G {
  v301 [label = ""];
  v303 [label = "A"];
  v304 [label = "black"];
  v306 [label = "white"];
  v305 [label = "cat"];
  v302 [label = ""];
  v301 -> v303 [label = "A, B"];
  v303 -> v304 [label = "A"];
  v303 -> v306 [label = "B"];
  v304 -> v305 [label = "A"];
  v306 -> v305 [label = "B"];
  v305 -> v302 [label = "A, B"];
}</pre>
      </div>
    </div>
    <div class="yui3-u-1-2">
      <div id="js-apidocs">
        <h2>JavaScript API</h2>
        
        <p>Enables the use of CollateX' RESTful API via JavaScript … Based on <a href="http://yuilibrary.com/" title="Homepage">YUI framework</a> …</p>

        <h3>Requirements</h3>
        
        <p>Add dependencies to header … YUI library plus CollateX module …</p>

        <pre class="prettyprint">&lt;script type="text/javascript"
  src="[ROOT]/static/yui-3.4.1/build/yui/yui-min.js"&gt;&lt;/script&gt;
&lt;script type="text/javascript"
  src="[ROOT]/static/collate.js"&gt;&lt;/script&gt;</pre>
        
        <p>Substitute URL prefix <code>[ROOT]</code> with the base URL of your installation, e.g.
          <a href="${cp}/" title="Base URL">this one</a> for the installation you are currently looking at …</p>
        
        <p>YUI module <code>interedition-collate</code> available now … supports cross-domain AJAX requests via
          <a href="http://en.wikipedia.org/wiki/Cross-Origin_Resource_Sharing" title="Wikipedia">CORS</a> …</p>
        
        <h3>Sample usage</h3>
        
        <pre class="prettyprint">&lt;div id="result"&gt;&lt;/div&gt;
&lt;script type="text/javascript"&gt;
  YUI().use("interedition-collate", function(Y) {
    var collator =
      new Y.interedition.collate.Collator();
    var results = Y.one("#result");
    collator.toTable([{
          id: "A",
          content: "Hello World"
      }, {
          id: "B",
          tokens: [
            { "t": "Hallo", "n": "hello" },
            { "t": "Welt", "n": "world" },
          ]
      }], results);
  });
&lt;/script&gt;</pre>
        
        <p>… <code>toTable()</code> takes witness array as first parameter; second parameter is DOM node which serves as container for
        the resulting HTML alignment table …</p>

        <p>… generic <code>collate(witnesses, callback)</code> as well as methods for other formats available:
        <code>toSVG()</code>, <code>toTEI()</code>, <code>toGraphViz()</code> …</p>

        <p>… configuration of a collator instance via methods like <code>withDekker()</code>, <code>withFuzzyMatching(maxDistance)</code> …</p>
      </div>
    </div>
  </div>

    <script type="text/javascript">
      prettyPrint();
    </script>
</@ie.page>