package de.tud.kom.stringutils.tokenization;

/**
*
* This file is part of Shingle Cloud Library, Copyright (C) 2009 Arno Mittelbach, Lasse Lehmann
* 
* Shingle Cloud Library is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Shingle Cloud Library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with Shingle Cloud Library.  If not, see <http://www.gnu.org/licenses/>.
*
*
*/


import java.util.ArrayList;

/**
 * Creates one token for each XML tag and for each word in between tags.
 * 
 * @author Arno Mittelbach
 *
 */
public class XMLWordTokenizer implements Tokenizer{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4741718842839136874L;

	public String[] tokenize(String input) {
		ArrayList<String> tokens = new ArrayList<String>();
		
		int mode = 0;
		String token = "";
		for(char c : input.toCharArray()){
			switch(mode){
			case 0:  // not matching anything
				if(c=='<'){
					mode = 1;
					token += c;
				} else if(c != ' ') {
					mode = 2;
					token += c;
				}
				break;
			
			case 1:  // matching tag
				if(c=='>'){
					tokens.add((token + c).trim());
					token = "";
					mode = 0;
				} else
					token += c;
				break;
			
			case 2:  // matching word
				if(c == ' '){
					tokens.add((token + c).trim());
					token = "";
					mode = 0;
				} else if ( c == '<' ) {
					tokens.add(token);
					token = String.valueOf(c);
					mode = 1;
				} else
					token += c;
				break;
			}
		}
		if(! token.equals("")){
			tokens.add(token);
		}
		
		String [] tokenArray = new String[tokens.size()];
		tokens.toArray(tokenArray);
		return tokenArray;
	}

	
}
