<@c.page title="Collate">

    <h2>Witnesses</h2>

<div id="input">
        <form id="collate-form" method="post">
            <div class="yui3-g form-element">
                <div class="yui3-u-1-6 form-label"><label for="examples">Examples:</label></div>
                <div class="yui3-u form-input">
                    <select id="examples" name="examples" style="width: 40em">
                        <option value=""></option>
                    </select>
                </div>
            </div>
            <div id="witnesses">
            </div>
            <div class="yui-3">
                <div class="yui3-u-1-4">&nbsp;</div>
                <div class="yui3-u">
                    <button id="add-witness">Add</button>
                    <input type="submit" value="Collate">
                </div>
            </div>
        </form>
    </div>

    <h2>Results</h2>

    <div id="output">
      <h3>Variant Graph</h3>
      <div id="variant-graph-svg"></div>

      <h3>Alignment Table</h3>
      <div id="alignment-table"></div>

      <div class="yui3-g">
        <div class="yui3-u-1-3">
          <h3>GraphML</h3>
          <div id="graphml"></div>
        </div>
        <div class="yui3-u-1-3">
          <h3>GraphViz</h3>
          <div id="graphviz-dot"></div>
        </div>
        <div class="yui3-u">
          <h3>TEI-P5</h3>
          <div id="tei-ps"></div>
        </div>
      </div>
    </div>


    <script type="text/javascript" src="${cp}/static/examples.js"></script>
    <script type="text/javascript" src="${cp}/static/collate.js"></script>
    <style type="text/css">
        table.alignment th { border-width: 2px; padding: 0 1em  }
        table.alignment td.gap { border: 0; text-align: center }
        table.alignment td.invariant { border-color: #80BB00 }
        table.alignment td.variant { border-color: #ffa07a }
        table.alignment td { white-space: nowrap; }
        .form-element { margin: 1em 0; }
        .form-label { text-align: right }
        .form-label label { padding-right: 1em }
        .in-progress { padding: 2em; background: #90ee90; font-weight: bold }
        #variant-graph-svg, #alignment-table { overflow-x: auto; border: 1px solid #ccc; padding: 1em }
    </style>
</@c.page>