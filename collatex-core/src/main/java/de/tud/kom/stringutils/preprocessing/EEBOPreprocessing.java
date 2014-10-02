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

/**
 * The preprocessing used for the comparison of Holinshed's Chronicles (http://www.cems.ox.ac.uk/holinshed/).
 * 
 * @author Arno Mittelbach
 */
public class EEBOPreprocessing implements Preprocess {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2030905405492008204L;

	public String preprocessInput(String input) {
		String output = input.toLowerCase();
		
		// replace long s with short s
		output = output.replace('ſ', 's');
		
		// replace vv with w
		output = output.replace("vv", "w");
		
		// replace ~ with n
		output = output.replace("~", "n");

		// remove strange characters that might appear in words
		output = output.replace("|", "");
		
		// replace & with and
		output = output.replaceAll("&", "and");
		
		// replace everything else with spaces
		output = output.replaceAll("[^a-z0-9]", " ");
		
		// remove vowels except for u
		output = output.replaceAll("[aeioy]", "");
		
		// transofrm u into v
		output = output.replace("u", "v");
		
		// remove double t, p, s, r, n, m
		output = output.replace("tt", "t");
		output = output.replace("pp", "p");
		output = output.replace("ss", "s");
		output = output.replace("rr", "r");
		output = output.replace("nn", "n");
		output = output.replace("mm", "m");
		
		// replace sc with s
		output = output.replace("sc", "s");
		
		/* additional */
		// turn double vv into w
		output = output.replace("lsl", "l"); // lsland
		output = output.replace("ll", "l");
		output = output.replace("dd", "d");
		output = output.replace("z", "s");
		
		// remove everything with less than two characters
		output = output.replaceAll("\\s[a-z0-9]{1}\\s", " ");
		
		// correct spaces
		output = output.replaceAll("\\b\\s{2,}\\b", " ");
		output = output.trim();
		
		return output;
	}

}
