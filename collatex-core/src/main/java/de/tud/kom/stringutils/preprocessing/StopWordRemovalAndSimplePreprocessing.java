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
 * Performs simple preprocessing ({@link SimplePreprocessing}) and stop wording ({@link StopWordRemoval}). 
 * @author Arno Mittelbach
 * @see SimplePreprocessing
 * @see StopWordRemoval
 */
public class StopWordRemovalAndSimplePreprocessing implements Preprocess {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9058482033851490535L;

	private static final StopWordRemoval stopWordRemoval = new StopWordRemoval();
	private static final SimplePreprocessing simplePreprocessing = new SimplePreprocessing();
	
	public String preprocessInput(String input) {
		return stopWordRemoval.preprocessInput(simplePreprocessing.preprocessInput(input));
	}

}
