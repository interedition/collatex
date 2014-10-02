package de.tud.kom.stringmatching.gst;

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
 * A wrapper object for GST tiles.
 * 
 * <p>Tiles are created by the {@link GST} algorithm</p>
 * 
 * @author amittelbach
 * @see GST
 */
public class GSTTile {

	private int start;
	private int length;
	private String tile;
	
	/**
	 * Creates a new tile with the given start position, length and content.
	 * 
	 * @param start
	 * @param length
	 * @param tile
	 */
	public GSTTile(int start, int length, String tile) {
		this.start = start;
		this.length = length;
		this.tile = tile;
	}

	/**
	 * 
	 * @return The tile's start position.
	 */
	public int getStart() {
		return start;
	}

	/**
	 * 
	 * @return The tile's length.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * 
	 * @return The tile's content.
	 */
	public String getText() {
		return tile;
	}
	
	@Override
	public String toString(){
		return tile;
	}
	
}
