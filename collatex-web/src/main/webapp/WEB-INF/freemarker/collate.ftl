<@c.page title="Collate">
    <div class="yui3-g">
        <div class="yui3-u-1-2">
            <h2>Witnesses</h2>

            <form id="collate-form" method="post">
                <div class="yui3-g form-element">
                    <div class="yui3-u-1-4"><label for="examples">Examples:</label></div>
                    <div class="yui3-u">
                        <select id="examples" name="examples">
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
        <div class="yui3-u-1-2">
            <h2>Results</h2>

            <div id="results"></div>
        </div>
    </div>

    <script type="text/javascript" src="${cp}/static/examples.js"></script>
    <script type="text/javascript" src="${cp}/static/collate.js"></script>
    <style type="text/css">
        .invariant { background: inherit; }
        .variant { background: #ffa07a}
        .form-element { margin: 1em 0; }
        .in-progress { padding: 2em; background: #90ee90; font-weight: bold }
        #results { overflow-x: auto; }
    </style>
</@c.page>