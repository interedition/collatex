package de.tud.kom.stringmatching.shinglecloud;

import com.google.common.base.Joiner;

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
 * Represents a single shingle.
 * 
 * @author Arno Mittelbach
 *
 */
public class Shingle {
	

	/**
	 * Defines various shingle types.
	 * 
	 * @author Arno Mittelbach
	 */
	public enum ShingleType {
		/**
		 * This is the basic form of a shingle. It contains an n-gram that is used for matching.
		 */
		Shingle,
		
		/**
		 * Shingles can be grouped. The beginning of a group is marked with a GroupStart shingle.
		 */
		GroupStart,
		
		/**
		 * Shingles can be grouped. The end of a group is marked with a GroupEnd shingle.
		 */
		GroupEnd,
		
		/**
		 * MagicMatcher shingles will always match if they appear between two matches
		 */
		MagicMatcher
	}
	
	/**
	 * Stores this shingle's type
	 */
	private ShingleType type;
	
	/**
	 * Stores this shingle's id which is used in conjunction with shingle groups
	 */
	private String id = "";
	
	/**
	 * Stores the item array
	 */
	private String[] items;
	
	/**
	 * Stores the shingle with spaces as item separators.
	 */
	private String shingle;
	
	/**
	 * Creates a new shingle with the given items.
	 * 
	 * @param items
	 */
	public Shingle(String[] items){
		this.items = items;
		this.shingle = Joiner.on(' ').join(items);
		this.type = ShingleType.Shingle;
	}
	
	/**
	 * Creates a new shingle of the specified type and with the specified id.
	 * @param type
	 * @param id
	 */
	public Shingle(ShingleType type, String id)
	{
		this.type = type;
		this.id = id;
	}
	
	public Shingle(ShingleType type){
		if(type != ShingleType.MagicMatcher)
			throw new IllegalArgumentException();
		
		this.type = type;
	}
	
	/**
	 * 
	 * @return true if shingle is a basic text shingle.
	 */
	public boolean isBasicShingle(){
		return ShingleType.Shingle.equals(type);
	}
	
	/**
	 * 
	 * @return true if shingle is a magic matcher.
	 */
	public boolean isMagicMatcher(){
		return ShingleType.MagicMatcher.equals(type);
	}
	
	/**
	 * 
	 * @return true if shingle marks the beginning of a group
	 */
	public boolean isGroupBegin(){
		return ShingleType.GroupStart.equals(type);
	}

	/**
	 * @return true if shingle marks the end of a group
	 * @return
	 */
	public boolean isGroupEnd(){
		return ShingleType.GroupEnd.equals(type);
	}
	
	@Override
	public String toString(){
		if( type.equals(ShingleType.GroupStart))
			return "GroupStart: " + id;
		if(type.equals(ShingleType.MagicMatcher))
			return "MagicMatcher";
		return shingle;
	}
	
	
	/**
	 * 
	 * @return The individual items of this shingle.
	 */
	public String[] getItems() {
		return items;
	}

	/**
	 * 
	 * @return The shingle this object is representing.
	 */
	public String getShingle() {
		return shingle;
	}
	
	/**
	 * 
	 * @return This shingle's type
	 */
	public ShingleType getType(){
		return type;
	}


	/**
	 * Can be used to define this shingle's type. 
	 * @param type
	 */
	public void setType(ShingleType type) {
		this.type = type;
	}
	
	/**
	 * 
	 * @return This shingle's id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets this shingle's id
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

}
