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

import java.util.BitSet;
import java.util.LinkedList;
import java.util.ListIterator;
import au.edu.uq.nmerge.exception.*;
/**
 * Represent one Pair in an MVD
 * @author Desmond Schmidt 18/8/07
 */
public class Pair extends Serialiser
{
	static final long serialVersionUID = 1;
	static final int PARENT_FLAG = 0x80000000;
	static final int CHILD_FLAG = 0x40000000;
	static final int TRANSPOSE_MASK = 0xC0000000;
	static final int INVERSE_MASK = 0x0FFFFFFF;
	Pair parent;
	LinkedList<Pair> children;
	private byte[] data;
	public BitSet versions;
	public static int pairId = 1;
	/** parent id if subject of a transposition */
	int id;
	/**
	 * Create a basic pair
	 * @param versions its versions
	 * @param data its data
	 */
	public Pair( BitSet versions, byte[] data )
	{
		this.versions = versions;
		this.data = data;
	}
	/**
	 * Get the number of children we have
	 * @return the current size of the children list
	 */
	public int numChildren()
	{
		return (children==null)?0:children.size();
	}
	/**
	 * Get an iterator over all the children of this pair. 
	 * The caller should have first tested if there are any 
	 * children, of course. This would not be a runtime but
	 * a coding error, hence we just fail here with a 
	 * NullPointerException
	 * @return an iterator
	 */
	public ListIterator<Pair> getChildIterator()
	{
		return children.listIterator();
	}
	/**
	 * Add a child pair to this parent to be. Children 
	 * don't have any data.
	 * @param child the child to add
	 */
	public void addChild( Pair child )
	{
		if ( children == null )
			children = new LinkedList<Pair>();
		children.add( child );
		child.setParent( this );
	}
	/**
	 * Remove a child pair. If this was our only child, stop 
	 * being a parent.
	 * @param child the child to remove
	 */
	public void removeChild( Pair child )
	{
		children.remove( child );
		if ( children.size() == 0 )
			children = null;
	}
	/**
	 * Set the pair's parent i.e. make this a child
	 * @param parent the parent to be
	 */
	public void setParent( Pair parent )
	{
		this.parent = parent;
	}
	/**
	 * Just get the length of the data, even if it is transposed.
	 * @return the length of the pair in bytes
	 */
	int length()
	{
		return (parent!=null)?parent.length():data.length;
	}
	/**
	 * Return the size of the data used by this pair
	 * @return the size of the data only
	 */
	int dataSize()
	{
		if ( parent!=null || isHint() )
			return 0;
		else if ( data == null )
		{
			System.out.println("null");
			return 0;
		}
		else
			return data.length;
	}
	/**
	 * Fix a data offset in a pair already serialised
	 * @param data the data to fix
	 * @param p the offset into data where the serialised pair starts
	 * @param dataOffset the new value of dataoffset
	 * @param vSetSize the number of bytes in the version set
	 * @param parentId write this out as the third integer
	 */
	static void fixDataOffset( byte[] data, int p, int dataOffset, 
		int vSetSize, int parentId )
	{
		// read versions
		p += vSetSize;
		Pair dummy = new Pair( null, null );
		dummy.writeInt( data, p, dataOffset );
		p += 8;
		dummy.writeInt( data, p, parentId );
	}
	/**
	 * Return the size of the pair itself (minus the data)
	 * @return versionSetSize the size of a version set in bytes
	 * @return the size of the pair when serialised
	 */
	int pairSize( int versionSetSize )
	{
		int pSize = versionSetSize + 4 + 4;
		if ( isParent() || isChild() )
			pSize += 4;
		return pSize;
	}
	/**
	 * Write the pair itself in serialised form and also its data.
	 * Versions get written out LSB first.
	 * @param bytes the byte array to write to
	 * @param p the offset within bytes to start writing this pair
	 * @param setSize the number of bytes in the version info
	 * @param dataOffset offset reserved in the dataTable for this 
	 * @param dataTableOffset the start of the data table within data
	 * pair's data (might be the same as some other pair's)
	 * @param parentId the id of the parent or NULL_PID if none
	 * @return the number of bytes written to data
	 */
	int serialisePair( byte[] bytes, int p, int setSize, int dataOffset, 
		int dataTableOffset, int parentId ) throws MVDException
	{
		int oldP = p;
		int flag = 0;
		if ( parent != null )
			flag = CHILD_FLAG;
		else if ( children != null )
			flag = PARENT_FLAG;
		if ( bytes.length > p + pairSize(setSize) )
		{
			p += serialiseVersions( bytes, p, setSize );
			// write data offset
			// can't see the point of this any more
			//writeInt( bytes, p, (children==null)?dataOffset:-dataOffset );
			writeInt( bytes, p, dataOffset );
			p += 4;
			// write data length ORed with the parent/child flag
			int dataLength = (data==null)?0:data.length; 
			dataLength |= flag;
			writeInt( bytes, p, dataLength );
			p += 4;
			if ( parentId != MVD.NULL_PID )
			{
				writeInt( bytes, p, parentId );
				p += 4;
			}
			// write actual data
			if ( parent == null && !isHint() )
				p += writeData( bytes, dataTableOffset+dataOffset, data );
		}
		else
			throw new MVDException( "No room for pair during serialisation" );
		return p - oldP;
	}
	/**
	 * Serialise the versions
	 * @param bytes the byte array to write to
	 * @param setSize the size of the versions in bytes
	 * @param p the offset within bytes to start writing the versions
	 * @return the number of bytes written
	 */
	private int serialiseVersions( byte[] bytes, int p, int setSize )
	{
		// iterate through the bits
		for ( int i=versions.nextSetBit(0); i>=0; 
			i=versions.nextSetBit(i+1) ) 
		{
			int index = ((setSize*8-1)-i)/8;
			assert index >= 0:"serialising versions: byte index < 0";
			bytes[p+index] |= 1 << (i%8);
		}
		return setSize;
	}
	/**
	 * Does this pair contain the given version?
	 * @param version the version to test
	 * @return true if version intersects with this pair
	 */
	public boolean contains( short version )
	{
		return versions.nextSetBit(version) == version;
	}
	/**
	 * Is this pair really a hint?
	 * @return true if it is, false otherwise
	 */
	public boolean isHint()
	{
		return versions.nextSetBit(0)==0;
	}
	/**
	 * Is this pair a child, i.e. the object of a transposition?
	 * @return true if it is, false otherwise
	 */
	public boolean isChild()
	{
		return parent != null;
	}
	/**
	 * Is this pair a parent i.e. the subject of a transposition?
	 * @return true if it is, false otherwise
	 */
	public boolean isParent()
	{
		return children != null;
	}
	/**
	 * Convert a pair to a human-readable form
	 * @return the pair as a String
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( versions+": " );
		if ( parent != null )
		{
			sb.append("["+parent.id+":");
			sb.append( new String(parent.data) );
			sb.append( "]" );
		}
		else if ( children != null )
		{
			sb.append("{"+id+":");
			sb.append( new String(data) );
			sb.append( "}" );
			sb.append("; children=");
			for ( int i=0;i<children.size();i++ )
			{
				Pair p = children.get( i );
				sb.append(p.toString());
				if ( i < children.size()-1 )
					sb.append(",");
			}
		}
		else if ( data != null )
			sb.append( new String(data) );
		else
			sb.append("null");
		return sb.toString();
	}
	/**
	 * Get the parent of this child pair.
	 * @return the parent
	 */
	public Pair getParent()
	{
		return parent;
	}
	/**
	 * Get the data of this pair
	 * @return this pair's data or that of its parent
	 */
	public byte[] getData()
	{
		if ( parent != null )
			return parent.getData();
		else
			return data;
	}
	/**
	 * Set the data of this pair. Not to be used publicly!
	 * @param data the new data for this pair
	 */
	void setData( byte[] data )
	{
		this.data = data;
	}
	/**
	 * Get the child of a parent
	 * @param v the version to look for a child in
	 * @return the relevant pair or null
	 */
	Pair getChildInVersion( short v )
	{
		Pair child = null;
		ListIterator<Pair> iter = getChildIterator();
		while ( iter.hasNext() )
		{
			Pair q = iter.next();
			if ( q.contains(v) )
			{
				child = q;
				break;
			}
		}
		return child;
	}
	/**
	 * Check that this pair is valid
	 * @throws MVDException
	 */
	public void verify() throws MVDException
	{
		if ( data == null && parent == null && versions.nextSetBit(0) != 0 )
			throw new MVDException(
				"data in pair is null and it is not a child or hint");
	}
}
