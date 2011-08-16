/*
 * NMerge is Copyright 2009-2011 Desmond Schmidt
 *
 * This file is part of NMerge. NMerge is a Java library for merging
 * multiple versions into multi-version documents (MVDs), and for
 * reading, searching and comparing them.
 *
 * NMerge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.edu.uq.nmerge;

/**
 * Search a byte array fairly efficiently. 
 * @author Desmond Schmidt 1/6/09
 */
public class KMPSearch
{
	static int n = 1;
	/**
	 * Initialise the next table
	 * @param pattern the pattern as a byte array in any encoding
	 * @return an array of next indices
	 */
	private static int[] initNext( byte[] pattern ) 
	{
		int[] next = new int [pattern.length];
		int i = 0, j = -1;
		next[0] = -1;
		while (i < pattern.length - 1) 
		{
			while ( j >= 0 && pattern[i] != pattern[j] )
				j = next[j];
			i++; j++;
			next[i] = j;
		}
		return next;
	}
	/**
	 * Perform the search
	 * @param text the byte array to search in
	 * @param offset within text from which to search
	 * @param pattern the byte array of the pattern
	 * @return the index into text where the pattern occurs or -1
	 */
	public static int search( byte[] text, int offset, byte[] pattern ) 
	{
		int[] next = initNext( pattern );
		int i = offset, j = 0;
		n = 1;
		while ( i < text.length ) 
		{
			while ( j >= 0 && pattern[j] != text[i] ) 
			{
				j = next[j];
			}
			i++; 
			j++;
			if ( j == pattern.length )
				return i - pattern.length;
		}
		return -1;
	}
}
