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


import java.io.Serializable;


/**
 * An interface for tokenizers that are used for String matching.
 * 
 * @author Arno Mittelbach
 *
 */
public interface Tokenizer extends Serializable {

	/**
	 * Tokenizes an input string producing a list of tokens.
	 * 
	 * @param input
	 * @return
	 */
	public String[] tokenize(String input);
}
