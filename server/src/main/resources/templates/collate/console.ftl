<@ie.page title="CollateX Console">
    <div id="input">
        <h2>Collation</h2>
        <form id="collate-form" method="post">
            <div class="yui3-g form-element">
              <div class="yui3-u-1-6 form-label"><label for="algorithm">Algorithm:</label></div>
              <div class="yui3-u form-input">
                <select id="algorithm" name="algorithm" style="width: 40em">
                  <option value="dekker">Dekker</option>
                  <option value="dekker-experimental">Dekker-Experimental</option>
                  <option value="needleman-wunsch">Needleman-Wunsch</option>
                </select>
              </div>
            </div>
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
          <div class="yui3-g form-element">
            <div class="yui3-u-1-6 form-label"><label for="examples">Segmentation:</label></div>
            <div class="yui3-u form-input"><input type="checkbox" id="joined" name="joined" checked="checked"></div>
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


    <div id="output">
      <h2>Results</h2>

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


    <script type="text/javascript" src="${cp}/static/collate-console.js"></script>
    <style type="text/css">
        .form-element { margin: 1em 0; }
        .form-label { text-align: right }
        .form-label label { padding-right: 1em }
        #variant-graph-svg, #alignment-table { overflow-x: auto; border: 1px solid #ccc; padding: 1em }
        #input, #output { margin-top: 2em; padding: 1em; border: 1px solid #ccc; }
    </style>
</@ie.page>