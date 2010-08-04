<?php
	$ch = curl_init();
	$headers = array ("Content-type: application/json;charset=UTF-8;",
						"Accept: " . $_POST['output_type']);
		
	$content = '{
		"witnesses" : [
		        {"id" : "A", "content" : "' . $_POST['text1'] . '" }, 
		        {"id" : "B", "content" : "' . $_POST['text2'] . '" },
		        {"id" : "C", "content" : "' . $_POST['text3'] . '" },
		        {"id" : "D", "content" : "' . $_POST['text4'] . '" }
		        ]
	}';
	
	curl_setopt($ch, CURLOPT_URL, $_POST['rest_service']);
	curl_setopt($ch, CURLOPT_POST, 1);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
	curl_setopt($ch, CURLOPT_POSTFIELDS, $content);
	$response = curl_exec ($ch);

	header("Content-type: " . $_POST['output_type']);
	echo $response; 
?>