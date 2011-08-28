<#--

    CollateX - a Java library for collating textual sources,
    for example, to produce an apparatus.

    Copyright (C) 2010 ESF COST Action "Interedition".

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

-->

<#assign cp = springMacroRequestContext.getContextPath()>

<#macro page title>
<!DOCTYPE html>
<html>
<head>
	<title>${title} :: CollateX</title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui-3.4.0/cssfonts/fonts-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui-3.4.0/cssreset/reset-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui-3.4.0/cssgrids/grids-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/yui-3.4.0/cssbase/base-min.css">
    <link rel="stylesheet" type="text/css" href="${cp}/static/interedition.css" />
	<link rel="stylesheet" type="text/css" href="${cp}/static/collatex.css" />
</head>
<body>
	<div id="mainmenu">
		<a href="${cp}/examples/usecases" title="Use Cases">Use cases</a> |
		<a href="${cp}/examples/beckett" title="Beckett">Beckett</a> |
		<a href="${cp}/examples/darwin" title="Darwin">Darwin</a> |
		<a href="${cp}/api/collate" title="REST service">REST service</a>
	</div>
	
	<#nested>
</body>
</html>
</#macro>