[#ftl]
[#import "/spring.ftl" as spring]
[#assign xhtmlCompliant = true in spring]
[#assign ctx = springMacroRequestContext.getContextPath()]

[#macro page title]
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>${title} :: CollateX</title>
	<link rel="stylesheet" type="text/css" href="${ctx}/css/collatex.css" />
</head>
<body>
	[#nested]
</body>
</html>
[/#macro]