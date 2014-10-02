package de.tud.kom.stringutils.preprocessing;

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
 * Performs the casefolding and adds spaces to turn punctuation marks into tokens.
 * 
 * <p>
 *  output = output.replaceAll("[^a-z0-9öäüß()\\[\\]\\-+%$@€.,{}]", " ");
 *	output = output.replaceAll("[.]", " . ");
 *	output = output.replaceAll("[,]", " , ");
 *	output = output.replaceAll("[(]", " ( ");
 *	output = output.replaceAll("[)]", " ) ");
 *	output = output.replaceAll("[{]", " { ");
 *	output = output.replaceAll("[}]", " } ");
 *	output = output.replaceAll("[\\[]", " [ ");
 *	output = output.replaceAll("[\\]]", " ] ");
 *	output = output.replaceAll("[\\-]", " - ");
 *  output = output.replaceAll("[+]", " + ");
 *	
 * 	output = output.replaceAll("\\b\\s{2,}\\b", " ");
 *	output = output.trim();
 * </p>
 * 
 * @author Arno Mittelbach
 *
 */
public class SimplePreprocessingExtended implements Preprocess, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4733578465599606320L;

	public String preprocessInput(String input) {
		String output = input.toLowerCase();
		
		output = output.replaceAll("[^a-z0-9öäüß()\\[\\]\\-+%$@€.,{}]", " ");
		output = output.replaceAll("[.]", " . ");
		output = output.replaceAll("[,]", " , ");
		output = output.replaceAll("[(]", " ( ");
		output = output.replaceAll("[)]", " ) ");
		output = output.replaceAll("[{]", " { ");
		output = output.replaceAll("[}]", " } ");
		output = output.replaceAll("[\\[]", " [ ");
		output = output.replaceAll("[\\]]", " ] ");
		output = output.replaceAll("[\\-]", " - ");
		output = output.replaceAll("[+]", " + ");
		
		output = output.replaceAll("\\b\\s{2,}\\b", " ");
		output = output.trim();
		
		return output;
	}

}
