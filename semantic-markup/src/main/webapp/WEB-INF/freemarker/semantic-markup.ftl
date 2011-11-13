<#ftl>
<#assign cp = springMacroRequestContext.getContextPath()>

<#macro page title>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <#--
    <link rel="stylesheet" type="text/css" href="${config['yui.base']}/cssfonts/fonts-min.css">
    <link rel="stylesheet" type="text/css" href="${config['yui.base']}/cssreset/reset-min.css">
    <link rel="stylesheet" type="text/css" href="${config['yui.base']}/cssgrids/grids-min.css">
    <link rel="stylesheet" type="text/css" href="${config['yui.base']}/cssbase/base-min.css">
    <script type="text/javascript" src="${config['yui.base']}/yui/yui-min.js"></script>
    -->
    <script type="text/javascript">var cp = "${cp?js_string}";</script>

    <title>${title} :: Semantic Markup Server</title>
</head>
<body<#-- class="yui3-skin-sam"-->>
<#nested />
</body>
</html>
</#macro>