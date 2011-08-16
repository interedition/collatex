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
package au.edu.uq.nmerge.mvd;
import java.io.UnsupportedEncodingException;
import java.io.Serializable;
import au.edu.uq.nmerge.exception.*;

/**
 * This class is used for writing out an MVD in binary form
 */
class Serialiser implements Serializable
{
	public static final long serialVersionUID = 1;
	public Serialiser()
	{
	}
	/**
	 * Write the given int value to the byte array. MSB first 
	 * (BigEndian order)
	 * @param data a byte array to write to
	 * @param p offset into data at which to begin writing
	 * @param value the int value to write
	 */
	protected void writeInt( byte[] data, int p, int value )
	{
		int mask = 0xFF;
		for ( int i=3;i>=0;i-- )
		{
			data[p+i] = (byte) (value & mask);
			value >>= 8;
		}
	}
	/**
	 * Write the given short value to the byte array. MSB first 
	 * (BigEndian order)
	 * @param data a byte array to write to
	 * @param p offset into data at which to begin writing
	 * @param value the short value to write
	 */
	protected void writeShort( byte[] data, int p, short value )
	{
		int mask = 0xFF;
		for ( int i=1;i>=0;i-- )
		{
			data[p+i] = (byte) (value & mask);
			value >>= 8;
		}
	}
	/**
	 * Serialise a String object as a 2-byte int preceded UTF-8 string
	 * @param data the byte array to write to
	 * @param p the offset within data to write to
	 * @param value the String value to serialise
	 * @return the number of bytes including the count int consumed
	 */
	protected int writeUtf8String( byte[] data, int p, String value ) 
		throws UnsupportedEncodingException
	{
		int len = 2;	// value length
		byte[] str = value.getBytes( "UTF-8" );
		writeShort( data, p, (short)str.length );
		p += 2;
		for ( int i=0;i<str.length;i++ )
			data[i+p] = str[i];
		len += str.length;
		return len;
	}
	/**
	 * Serialise a String object as a 2-byte int preceded UTF-8 string
	 * @param value the String value to serialise
	 * @return the number of bytes including the count int consumed
	 */
	protected int measureUtf8String( String value ) 
		throws UnsupportedEncodingException
	{
		byte[] str = value.getBytes( "UTF-8" );
		return str.length + 2;
	}
	/**
	 * Write bytes from source to destination
	 * @param dst the destination byte array
	 * @param p offset within data to write to
	 * @param src the source byte array
	 * @return number of bytes copied
	 */
	protected int writeData( byte[] dst, int p, byte[] src ) 
		throws MVDException
	{
		if ( p + src.length > dst.length )
			throw new MVDException( "No room for data in data table");
		for ( int i=0;i<src.length;i++ )
			dst[p++] = src[i];
		return src.length;
	}
}
