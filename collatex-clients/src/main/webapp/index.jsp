<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Collate-X JSP/Servlet client</title>
	</head>
	<body>
		<form method="post" action="Collatex" name="form">
			<table>
				<tr>
					<td colspan="2">CollateX REST-Service: <input type="text" name="rest_service" id="rest_service" size="50" value="http://localhost:8080/collatex-web/api/collate"/></td>
				</tr>
				<tr>
					<td>Witness A : <input size="250" name="text1" id="text1"/></td>
				</tr>
				<tr>
					<td>Witness B : <input size="250" name="text2" id="text2"/></td>
				</tr>
				<tr>
					<td>Witness C : <input size="250" name="text3" id="text3"/></td>
				</tr>
				<tr>
					<td>Witness D : <input size="250" name="text4" id="text4"/></td>
				</tr>
				<tr>
					<td>Witness E : <input size="250" name="text5" id="text5"/></td>
				</tr>
				<tr>
					<td>Witness F : <input size="250" name="text6" id="text6"/></td>
				</tr>
				<tr>
					<td>Witness G : <input size="250" name="text7" id="text7"/></td>
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
    Examples:<br/>

<script type="text/javascript">
  function showExample(text1,text2,text3,text4,text5,text6,text7) {
    form = document.form;
    form.text1.value=text1;
    form.text2.value=text2;
    form.text3.value=text3;
    form.text4.value=text4;
    form.text5.value=text5;
    form.text6.value=text6;
    form.text7.value=text7;
  }

  var examples = { "example" : [
	  {
		  "name":"The black cat",
		  "text1":"The black cat",
		  "text2":"The black and white cat",
		  "text3":"The black and green cat",
		  "text4":"The black very special cat",
		  "text5":"The black not very special cat"
		},{
			"name":"The black dog chases a red cat",
			"text1":"The black dog chases a red cat.",
			"text2":"A red cat chases the black dog.",
			"text3":"A red cat chases the yellow dog"
		},{
		  "name":"the black cat and the black mat",
			"text1":"the black cat and the black mat",
			"text2":"the black dog and the black mat",
			"text3":"the black dog and the black mat"
		},{
			"name":"the black cat on the table",
			"text1":"the black cat on the table",
			"text2":"the black saw the black cat on the table",
			"text3":"the black saw the black cat on the table"
		},{
		  "name":"the black cat sat on the mat",
			"text1":"the black cat sat on the mat",
			"text2":"the cat sat on the black mat",
			"text3":"the cat sat on the black mat"
		},{
		  "name":"THE BLACK CAT",
			"text1":"the black cat",
			"text2":"THE BLACK CAT",
			"text3":"The black cat",
			"text4":"The, black cat"
		},{
		  "name":"the white and black cat",
			"text1":"the white and black cat",
			"text2":"The black cat",
			"text3":"the black and white cat",
			"text4":"the black and green cat"
		},{
		  "name":"cat or dog",
		  "text1":"a cat or dog",
		  "text2":"a cat and dog and",
		  "text3":"a cat and dog and"
		},{
		  "name":"agast",
		  "text1":"He was agast, so",
		  "text2":"He was agast",
		  "text3":"So he was agast",
		  "text4":"He was so agast",
		  "text5":"He was agast and feerd",
		  "text6":"So was he agast"
		},{
		  "name":"big head",
		  "text1":"the big bug had a big head",
		  "text2":"the bug big had a big head",
		  "text3":"the bug had a small head"
		},{
		  "name":"the big bug",
		  "text1":"the big bug had a big head",
		  "text2":"the bug had a small head"
		},{
		  "name":"the bug big",
		  "text1":"the bug big had a big head",
		  "text2":"the bug had a small head",
		  "text3":"the bug had a small head"
		},{
		  "name":"the drought of march",
		  "text1":"the drought of march hath perced to the root and is this the right",
		  "text2":"the first march of drought pierced to the root and this is the ",
		  "text3":"the first march of drought hath perced to the root"
		},{
		  "name":"the drought",
		  "text1":"the drought of march hath perced to the root",
		  "text2":"the march of the drought hath perced to the root",
		  "text3":"the march of drought hath perced to the root"
		},{
		  "name":"the very",
		  "text1":"the very first march of drought hath",
		  "text2":"the drought of march hath",
		  "text3":"the drought of march hath"
		},{
		  "name":"When April",
		  "text1":"When April with his showers sweet with fruit The drought of March has pierced unto the root",
		  "text2":"When showers sweet with April fruit The March of drought has pierced to the root",
		  "text3":"When showers sweet with April fruit The drought of March has pierced the rood"
		},{
		  "name":"This Carpenter",
		  "text1":"This Carpenter hadde wedded newe a wyf",
		  "text2":"This Carpenter hadde wedded a newe wyf",
		  "text3":"This Carpenter hadde newe wedded a wyf",
		  "text4":"This Carpenter hadde wedded newly a wyf",
		  "text5":"This Carpenter hadde E wedded newe a wyf",
		  "text6":"This Carpenter hadde newli wedded a wyf"
		},{
		  "name":"Almost every",
		  "text1":"Almost every aspect of what scholarly editors do may be changed",
		  "text2":"Hardly any aspect of what stupid editors do in the privacy of their own home may be changed again and again",
		  "text3":"very many aspects of what scholarly editors do in the livingrooms of their own home may not be changed"
		},{
		  "name":"Du kennst",
		  "text1":"Du kennst von Alters her meine Art, mich anzubauen, irgend mir an einem vertraulichen Orte ein Hüttchen aufzuschlagen, und da mit aller Einschränkung zu herbergen.",
		  "text2":"Du kennst von Altersher meine Art, mich anzubauen, mir irgend an einem vertraulichen Ort ein Hüttchen aufzuschlagen, und da mit aller Einschränkung zu herbergen."
		},{
		  "name":"Plätzchen",
		  "text1":"Auch hier hab ich wieder ein Plätzchen",
		  "text2":"Ich hab auch hier wieder ein Pläzchen"
		},{
		  "name":"this glass",
		  "text1":"I bought this glass, because it matches those dinner plates.",
		  "text2":"I bought those glasses."
		},{
		  "name":"One night",
		  "text1":"One night as he sat at his table head on hands he saw himself rise and go.",
		  "text2":"One night as he sat at his table head on hands he saw himself rise and go.",
		  "text3":"From where he sat with his head on his hands he saw himself rise and disappear. ",
		  "text4":"One night as he sat at his table head on hands he saw himself rise and go.",
		  "text5":"From where he sat with his head on his hands he saw himself rise & disappear. As from afar. ",
		  "text6":"From where he sat with his head on his hands he saw himself rise and disappear. As from afar. xxx ",
			"text7":"One night at his table head on hand he sees himself rise and go."
		},{
		  "name":"Darwin1",
			"text1":"WHEN we look to the individuals of the same variety or sub-variety of our older cultivated plants and animals, one of the first points which strikes us, is, that they generally differ much more from each other, than do the individuals of any one species or variety in a state of nature. When we reflect on the vast diversity of the plants and animals which have been cultivated, and which have varied during all ages under the most different climates and treatment, I think we are driven to conclude that this greater variability is simply due to our domestic productions having been raised under conditions of life not so uniform as, and somewhat different from, those to which the parent-species have been exposed under nature. There is, also, I think, some probability in the view propounded by Andrew Knight, that this variability may be partly connected with excess of food. It seems pretty clear that organic beings must be exposed during several generations to the new conditions of life to cause any appreciable amount of variation; and that when the organisation has once begun to vary, it generally continues to vary for many generations. No case is on record of a variable being ceasing to be variable under cultivation. Our oldest cultivated plants, such as wheat, still often yield new varieties: our oldest domesticated animals are still capable of rapid improvement or modification.",
			"text2":"WHEN we look to the individuals of the same variety or sub-variety of our older cultivated plants and animals, one of the first points which strikes us, is, that they generally differ more from each other than do the individuals of any one species or variety in a state of nature. When we reflect on the vast diversity of the plants and animals which have been cultivated, and which have varied during all ages under the most different climates and treatment, I think we are driven to conclude that this great variability is simply due to our domestic productions having been raised under conditions of life not so uniform as, and somewhat different from, those to which the parent-species have been exposed under nature. There is also, I think, some probability in the view propounded by Andrew Knight, that this variability may be partly connected with excess of food. It seems pretty clear that organic beings must be exposed during several generations to the new conditions of life to cause any appreciable amount of variation; and that when the organisation has once begun to vary, it generally continues to vary for many generations. No case is on record of a variable being ceasing to be variable under cultivation. Our oldest cultivated plants, such as wheat, still often yield new varieties: our oldest domesticated animals are still capable of rapid improvement or modification.",
			"text3":"WHEN we look to the individuals of the same variety or sub-variety of our older cultivated plants and animals, one of the first points which strikes us, is, that they generally differ more from each other than do the individuals of any one species or variety in a state of nature. When we reflect on the vast diversity of the plants and animals which have been cultivated, and which have varied during all ages under the most different climates and treatment, I think we are driven to conclude that this great variability is simply due to our domestic productions having been raised under conditions of life not so uniform as, and somewhat different from, those to which the parent-species have been exposed under nature. There is also, I think, some probability in the view propounded by Andrew Knight, that this variability may be partly connected with excess of food. It seems pretty clear that organic beings must be exposed during several generations to the new conditions of life to cause any appreciable amount of variation; and that when the organisation has once begun to vary, it generally continues to vary for many generations. No case is on record of a variable being ceasing to be variable under cultivation. Our oldest cultivated plants, such as wheat, still often yield new varieties: our oldest domesticated animals are still capable of rapid improvement or modification.",
			"text4":"Causes of Variability. WHEN we look to the individuals of the same variety or sub-variety of our older cultivated plants and animals, one of the first points which strikes us, is, that they generally differ more from each other than do the individuals of any one species or variety in a state of nature. When we reflect on the vast diversity of the plants and animals which have been cultivated, and which have varied during all ages under the most different climates and treatment, I think we are driven to conclude that this great variability is simply due to our domestic productions having been raised under conditions of life not so uniform as, and somewhat different from, those to which the parent-species have been exposed under nature. There is also, I think, some probability in the view propounded by Andrew Knight, that this variability may be partly connected with excess of food. It seems pretty clear that organic beings must be exposed during several generations to the new conditions of life to cause any appreciable amount of variation; and that, when the organisation has once begun to vary, it generally continues to vary for many generations. No case is on record of a variable being ceasing to be variable under cultivation. Our oldest cultivated plants, such as wheat, still often yield new varieties: our oldest domesticated animals are still capable of rapid improvement or modification.",
			"text5":"Causes of Variability. WHEN we compare the individuals of the same variety or sub-variety of our older cultivated plants and animals, one of the first points which strikes us is, that they generally differ from each other more than do the individuals of any one species or variety in a state of nature. And if we reflect on the vast diversity of the plants and animals which have been cultivated, and which have varied during all ages under the most different climates and treatment, we are driven to conclude that this great variability is due to our domestic productions having been raised under conditions of life not so uniform as, and somewhat different from, those to which the parent-species had been exposed under nature. There is also, I think, some probability in the view propounded by Andrew Knight, that this variability may be partly connected with excess of food. It seems clear that organic beings must be exposed during several generations to new conditions to cause any appreciable amount of variation; and that, when the organisation has once begun to vary, it generally con- tinues varying for many generations. No case is on record of a variable organism ceasing to vary under cultivation. Our oldest cultivated plants, such as wheat, still yield new varieties: our oldest domesticated animals are still capable of rapid improvement or modification.",
			"text6":"Causes of Variability. WHEN we compare the individuals of the same variety or sub-variety of our older cultivated plants and animals, one of the first points which strikes us is, that they generally differ more from each other than do the individuals of any one species or variety in a state of nature. And if we reflect on the vast diversity of the plants and animals which have been cultivated, and which have varied during all ages under the most different climates and treatment, we are driven to conclude that this great variability is due to our domestic productions having been raised under conditions of life not so uniform as, and somewhat different from, those to which the parent-species had been exposed under nature. There is, also, some probability in the view propounded by Andrew Knight, that this variability may be partly connected with excess of food. It seems clear that organic beings must be exposed during several generations to new conditions to cause any great amount of variation; and that, when the organisation has once begun to vary, it generally continues varying for many generations. No case is on record of a variable organism ceasing to vary under cultivation. Our oldest cultivated plants, such as wheat, still yield new varieties: our oldest domesticated animals are still capable of rapid improvement or modification."
		}
	]}

  for (i in examples.example){
	  var example = examples.example[i];
    document.write("<button onClick='showExample(\""+
    	    (example.text1||"")+"\",\""+
    	    (example.text2||"")+"\",\""+
    	    (example.text3||"")+"\",\""+
    	    (example.text4||"")+"\",\""+
    	    (example.text5||"")+"\",\""+
    	    (example.text6||"")+"\",\""+
    	    (example.text7||"")+"\");'>"+
    	    example.name+
    	    "</button>");
  }
</script>

  </body>
</html>