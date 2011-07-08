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
<body>
    <#nested />
</body>
</html>
</#macro>