<#ftl>
<#assign cp = springMacroRequestContext.getContextPath()>

<#macro page title header="">
<!DOCTYPE html>
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
  <title>${title} :: Interedition Web Services</title>

  <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/3.4.1/build/cssfonts/fonts-min.css">
  <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/3.4.1/build/cssreset/reset-min.css">
  <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/3.4.1/build/cssgrids/grids-min.css">
  <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/3.4.1/build/cssbase/base-min.css">
  <link rel="stylesheet" type="text/css" href="${cp}/static/interedition.css">
  <link rel="stylesheet" type="text/css" href="${cp}/static/text-repository.css">

  <script type="text/javascript" src="${cp}/static/d3-1.29.6/d3.min.js"></script>
  <script type="text/javascript" src="${cp}/static/d3-1.29.6/d3.behavior.min.js"></script>
  <script type="text/javascript" src="http://yui.yahooapis.com/3.4.1/build/yui/yui-min.js"></script>
  <script type="text/javascript" src="${cp}/static/modules/collatex.js"></script>
  <script type="text/javascript" src="http://www.middell.net/gregor/interedition-text-min.js"></script>
  <script type="text/javascript">var cp = "${cp?js_string}";</script>

${header}
</head>
<body class="yui3-skin-sam">
<h1>${title?html}</h1>
<div id="main-menu" class="yui3-menu yui3-menu-horizontal yui3-menubuttonnav">
    <div class="yui3-menu-content">
        <ul>
          <li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/">Home</a></li>
          <li>
            <span class="yui3-menu-label"><em>Collate</em></span>
            <div class="yui3-menu">
              <div class="yui3-menu-content">
                <ul>
                  <li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/collate/console">Console</a></li>
                  <#--<li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/collate/darwin">Darwin Sample</a></li>-->
                  <li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/collate/tutorial">REST API</a></li>
                </ul>
              </div>
            </div>
          </li>
          <li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/text/">Upload</a></li>
        </ul>
    </div>
</div>
<div style="text-align: right; margin: 1em 0">
    <form id="quick-search-form" action="${cp}/search/">
        <input type="text" name="query" id="query" style="width: 10em" value="${searchQuery!''?html}">
        <input type="submit" value="Search…">
    </form>
</div>
<#nested />
<script type="text/javascript">
    YUI().use("node", "node-menunav", function(Y) {
        Y.on("contentready", function() {
            this.plug(Y.Plugin.NodeMenuNav, { autoSubmenuDisplay: true, mouseOutHideDelay: 0 });
        }, "#main-menu");

        Y.on("domready", function() {
            Y.one("#query").focus();
        });
    });
</script>
</body>
</html>
</#macro>