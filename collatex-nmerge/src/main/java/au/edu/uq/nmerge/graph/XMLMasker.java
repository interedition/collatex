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
package au.edu.uq.nmerge.graph;

/**
 * A class to mask bytes based on XML structure. We can either mask 
 * the content or mask the markup. That is, we will create a mask
 * that mirrors, byte for byte, the data. If the mask byte is 1 then 
 * we will align the data at that point, otherwise we will ignore it.
 * This will allow us to do a) linguistics texts with different markup 
 * but the same text, and b) multi-lingual texts with the same markup 
 * but different text.
 * @author Desmond Schmidt
 */
public class XMLMasker 
{
	/** should not occur in the source strings */
	static byte MASK_BYTE = '\0';
	/**
	 * Copy the passed in byte array and mask out the appropriate bytes.
	 * This is suitable for constructing a suffix tree
	 * @param data the data to make a masked-out copy of
	 * @param mask an array of 1s and zeros
	 * @return a copy of the same data blanked out appropriately
	 */
	public static byte[] maskOut( byte[] data, byte[] mask )
	{
		assert data.length==mask.length;
		byte[] copy = new byte[data.length];
		for ( int i=0;i<data.length;i++ )
			copy[i] = (mask[i]==0)?MASK_BYTE:data[i];
		return copy;
	}
	/**
	 * One method does it all: create a mask for a given byte array.
	 * @param data the data to make a mask of
	 * @param maskXML if true mask the XML, else mask the text
	 * @return the mask exactly the same length as the data
	 */
	public static byte[] getMask( byte[] data, boolean maskXML )
	{
		int state = 0;
		byte[] mask = new byte[data.length];
		// assume XML is syntactically correct
		for ( int i=0;i<data.length;i++ )
		{
			// set it for content by default
			mask[i] = (maskXML)?(byte)1:0;
			switch ( state )
			{
				case 0:	// initial state
					if ( data[i] == '<' )
					{
						state = 1;
						mask[i] = (maskXML)?(byte)0:1;
					}
					break;
				case 1: // in tag
					if ( data[i] == '>' )
						state = 2;
					mask[i] = (maskXML)?(byte)0:1;
					break;
				case 2: // in content
					if ( data[i] == '<' )
					{
						state = 1;
						mask[i] = (maskXML)?(byte)0:1;
					}
					break;
			}
		}
		return mask;
	}
}
