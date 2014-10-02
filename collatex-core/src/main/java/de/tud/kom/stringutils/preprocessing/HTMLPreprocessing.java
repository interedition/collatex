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
 * Removes HTML tags, performs casefolding and replaces all characters falling into [^a-zäöüß0-9] with a space.
 * 
 * @author Arno Mittelbach
 */
public class HTMLPreprocessing implements Preprocess {

	/**
	 * 
	 */
	private static final long serialVersionUID = -278539502091391505L;

	public String preprocessInput(String input) {
		String text = input.toLowerCase();
		
		text = text.replaceAll("\n", " ");
		text = text.replaceAll("(?s)<.*?>", "");
		text = text.replaceAll("[^a-zäöüß0-9]", " ");
		text = text.replaceAll("\\b\\s{2,}\\b", " ");
		text = text.trim();
		
		return text;
	}

	
}
