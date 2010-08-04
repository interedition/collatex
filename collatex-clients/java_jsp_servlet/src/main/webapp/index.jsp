<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Collate-X JSP/Servlet client</title>
</head>
<body>
<form method="post" action="Collatex">
	<table>
		<tr>
			<td colspan="2">CollateX REST-Service: <input type="text" name="rest_service" id="rest_service" size="50" value="http://localhost:8080/collatex-web-0.9/api/collate"/></td>
		</tr>
		<tr>
			<td>Text1:</td>
			<td>Text2:</td>
		</tr>
		<tr>
			<td><textarea name="text1" id="text1" rows="10" cols="40"></textarea></td>
			<td><textarea name="text2" id="text2" rows="10" cols="40"></textarea></td>
		</tr>
		<tr>
			<td>Text3:</td>
			<td>Text4:</td>
		</tr>
		<tr>
			<td><textarea name="text3" id="text3" rows="10" cols="40"></textarea></td>
			<td><textarea name="text4" id="text4" rows="10" cols="40"></textarea></td>
		</tr>
		<tr>
			<td colspan="2">Return output as:
				<select name="output_type" id="output_type">
					<option value="application/xml" selected="selected">xml-tei</option>
					<option value="application/json">json</option>
				</select>
			</td>
		</tr>
		<tr>
			<td><input type="submit" value="Collate!" /></td>
		</tr>
	</table>	
</form>
</body>
</html>