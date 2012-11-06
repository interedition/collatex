<#ftl>

<#macro page title header="">
<!DOCTYPE html>
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
  <title>${title} :: Interedition Microservices</title>

  <link rel="shortcut icon" href="${cp}/static/interedition_logo.ico" type="image/x-icon">
  <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?3.6.0/build/cssfonts/cssfonts-min.css&3.6.0/build/cssgrids/cssgrids-min.css&3.6.0/build/cssreset/cssreset-min.css&3.6.0/build/cssbase/cssbase-min.css&3.6.0/build/cssbutton/cssbutton-min.css">
  <link rel="stylesheet" type="text/css" href="${cp}/static/interedition.css">
  <link rel="stylesheet" type="text/css" href="${cp}/static/webapp.css">

  <script type="text/javascript">var cp = "${cp?js_string}";</script>
  <script type="text/javascript" src="http://yui.yahooapis.com/combo?3.6.0/build/yui/yui-min.js"></script>
  <script type="text/javascript">
    YUI.applyConfig({
      debug: true,
      combine: true,
      modules: {
        'interedition-collate': {
          fullpath: cp + '/static/collate.js',
          requires: ["base", "io", "json", "node", "array-extras", "escape", "dump"]
        }
      }
    });
  </script>
${header}
</head>
<body class="yui3-skin-sam">
<div id="header"><h1 class="boxed">${title?html}</h1></div>
<div id="main-menu" class="yui3-menu yui3-menu-horizontal yui3-menubuttonnav hidden">
    <div class="yui3-menu-content">
        <ul>
          <li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/">Introduction</a></li>
          <li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/collate/console">Console</a></li>
          <li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/collate/apidocs">API Documentation</a></li>
        </ul>
    </div>
</div>
<script type="text/javascript">
  YUI().use("node", "node-menunav", function(Y) {
    Y.on("contentready", function() {
      this.plug(Y.Plugin.NodeMenuNav, { autoSubmenuDisplay: true, mouseOutHideDelay: 0 });
      this.removeClass("hidden");
    }, "#main-menu");
  });
</script>
<#nested>
<div id="footer" class="color12">
  Copyright &copy; 2011, 2012 The Interedition Development Group. All rights reserved. See the <a href="http://www.interedition.eu/" title="Interedition Homepage">Interedition Homepage</a> for further information.
</div>
</body>
</html>
</#macro>