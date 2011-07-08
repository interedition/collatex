<#ftl>
<#assign cp = springMacroRequestContext.getContextPath()>

<#macro page title>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui3/cssfonts/fonts-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui3/cssreset/reset-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui3/cssgrids/grids-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui3/cssbase/base-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/interedition.css">
    <script type="text/javascript" src="${cp}/static/yui3/yui/yui-min.js"></script>
    <script type="text/javascript">var cp = "${cp?js_string}";</script>
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
<#nested />
<script type="text/javascript">
    YUI().use("node-menunav", function(Y) {
        Y.on("contentready", function() {
            this.plug(Y.Plugin.NodeMenuNav, { autoSubmenuDisplay: true, mouseOutHideDelay: 0 });
        }, "#main-menu");
    });
</script>
</body>
</html>
</#macro>