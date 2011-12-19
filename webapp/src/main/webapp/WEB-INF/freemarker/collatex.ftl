<#assign cp = springMacroRequestContext.getContextPath()>

<#macro page title header="">
<!DOCTYPE html>
<html>
<head>
    <title>${title} :: CollateX</title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/3.4.1/build/cssfonts/fonts-min.css">
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/3.4.1/build/cssreset/reset-min.css">
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/3.4.1/build/cssgrids/grids-min.css">
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/3.4.1/build/cssbase/base-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/interedition.css"/>
    <link rel="stylesheet" type="text/css" href="${cp}/static/collatex.css"/>
    <script type="text/javascript">var cp = "${cp?js_string}";</script>
    <script type="text/javascript" src="http://yui.yahooapis.com/3.4.1/build/yui/yui-min.js"></script>
    <#if header?has_content>${header}</#if>
</head>
<body class="yui3-skin-sam">
<table id="top-bar">
    <tr>
        <td style="width: 60px"><img src="${cp}/static/collatex_logo_small.png" alt="CollateX"></td>
        <td>
            <div id="main-menu" class="yui3-menu yui3-menu-horizontal">
                <div class="yui3-menu-content">
                    <ul>
                        <li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/">Collate</a></li>
                        <li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/tutorial">Documentation</a></li>
                        <li class="yui3-menuitem"><a class="yui3-menuitem-content" href="${cp}/examples/darwin">Darwin</a></li>
                    </ul>
                </div>
            </div>
        </td>
    </tr>
</table>
    <#nested>
<script type="text/javascript">
    YUI().use("node", "node-menunav", function(Y) {
        Y.on("contentready", function() {
            this.plug(Y.Plugin.NodeMenuNav, { autoSubmenuDisplay: true, mouseOutHideDelay: 0 });
        }, "#main-menu");
    });
</script>
</body>
</html>
</#macro>