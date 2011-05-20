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

<#import "/spring.ftl" as spring>
<#assign xhtmlCompliant = true in spring>
<#assign ctx = springMacroRequestContext.getContextPath()>

<#macro page title>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>${title} :: CollateX</title>
	<link rel="stylesheet" type="text/css" href="${ctx}/css/collatex.css" />
</head>
<body>
	<div id="mainmenu">
		<a href="${ctx}/examples/usecases" title="Use Cases">Use cases</a> |
		<a href="${ctx}/examples/beckett" title="Beckett">Beckett</a> |
		<a href="${ctx}/examples/darwin" title="Darwin">Darwin</a> |
		<a href="${ctx}/api/collate" title="REST service">REST service</a>
	</div>
	
	<#nested>
</body>
</html>
</#macro>