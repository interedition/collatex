<?php
/*
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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