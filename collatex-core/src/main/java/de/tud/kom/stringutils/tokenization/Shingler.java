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
 * Shingles the input data.
 * 
 * <p>By default words are used as tokens and 3-grams are created.</p>
 * 
 * @author Arno Mittelbach
 * @see WordTokenizer
 */
public class Shingler implements Tokenizer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 777094867437931878L;
	
	private Tokenizer tokenizer = new WordTokenizer();
	private int shingleSize = 3;

	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public int getShingleSize() {
		return shingleSize;
	}

	public void setShingleSize(int shingleSize) {
		this.shingleSize = shingleSize;
	}

	public String[] tokenize(String input) {
		String[] words = tokenizer.tokenize(input);
		String[] shingles = new String[words.length - shingleSize + 1];
		
		for(int i = 0; i < words.length - shingleSize + 1; i++)
		{
			StringBuffer buf = new StringBuffer();
			for(int j = 0; j < shingleSize; j++)
				buf.append(words[i+j]);
			
			shingles[i] = buf.toString();
		}
		
		return shingles;
	}

}
