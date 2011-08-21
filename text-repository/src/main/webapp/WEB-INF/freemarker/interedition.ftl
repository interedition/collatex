<#ftl>
<#assign cp = springMacroRequestContext.getContextPath()>

<#macro page title>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui-3.4.0/cssfonts/fonts-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui-3.4.0/cssreset/reset-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui-3.4.0/cssgrids/grids-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui-3.4.0/cssbase/base-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/interedition.css">

    <script type="text/javascript">var cp = "${cp?js_string}";</script>
    <script type="text/javascript" src="${cp}/static/yui-3.4.0/yui/yui-min.js"></script>
    <script type="text/javascript" src="${cp}/static/d3-1.29.6/d3.min.js"></script>
    <script type="text/javascript" src="${cp}/static/repository.js"></script>
    <script type="text/javascript" src="${cp}/static/text.js"></script>

    <title>${title} :: Interedition Text Repository</title>
</head>
<body class="yui3-skin-sam">
<div id="main-menu" class="yui3-menu yui3-menu-horizontal">
    <div class="yui3-menu-content">
        <ul>
            <li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/">Home</a></li>
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