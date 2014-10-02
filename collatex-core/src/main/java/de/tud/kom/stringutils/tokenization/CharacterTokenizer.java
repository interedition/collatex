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


/**
 * Creates a token for each character in the input data.
 * 
 * @author Arno Mittelbach
 */
public class CharacterTokenizer implements Tokenizer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7097431615659599290L;

	public String[] tokenize(String input) {
		String[] tokens = new String[input.length()];
		
		char[] cArray = input.toCharArray();
		for(int i = 0; i < input.length();i++)
			tokens[i] = String.valueOf(cArray[i]);
			
		return tokens;
	}

	
	
}
