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

import java.util.BitSet;
import java.util.HashMap;
import au.edu.uq.nmerge.mvd.Pair;
import au.edu.uq.nmerge.exception.*;
import java.util.LinkedList;
import java.util.ListIterator;
/**
 * An Arc is a fragment of data, a set of versions in a variant graph
 * @author Desmond Schmidt
 */
public class Arc 
{
	/** the Adler32 modulus */
	static int MOD_ADLER = 65521;
	static String alphabet = new String(
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
	/** the set of versions */
	public BitSet versions;
	/** the origin Node */
	Node from;
	/** the destination Node */
	public Node to;
	/** the data of this Arc */
	byte[] data;
	/** the mask for this arc's data or null */
	byte[] mask;
	/** usually empty list of transpose children */
	LinkedList<Arc> children;
	/** parent - can't be set as well as children */
	Arc parent;
	/** parent id if subject of a transposition */
	int id;
	/** id counter: used for parent/child relationships */
	static int arcId = 1;
	/**
	 * Create a child arc
	 * @param versions the set of versions for the child
	 * @param parent the parent arc whose data will be copied
	 */
	public Arc( BitSet versions, Arc parent )
	{
		this.versions = versions;
		// may attempt to create child of a child
		if ( parent != null )
		{
			Arc temp = parent;
			while ( temp.parent != null )
				temp = temp.parent;
			if ( temp != parent )
				parent = temp;
			this.parent = temp;
			parent.addChild( this );
		}
		//checkForData("m_s id=\"page");
	}
	/**
	 * Get an error over the children. Shouldn't be called if no 
	 * children, hence we throw an exception
	 * @return a ListIterator over the arc's children
	 * @throws MVDException
	 */
	ListIterator<Arc> childIterator() throws MVDException
	{
		if ( children == null )
			throw new MVDException("no children to arc");
		return children.listIterator(0);
	}
	/**
	 * Create a vanilla Arc with a mask
	 * @param versions the versions it belongs to
	 * @param data its data content
	 */
	public Arc( BitSet versions, byte[] data, byte[] mask )
	{
		this.versions = versions;
		this.data = data;
		this.mask = mask;
		//checkForData("m_s id=\"page");
	}
	/**
	 * Create a vanilla Arc
	 * @param versions the versions it belongs to
	 * @param data its data content
	 */
	public Arc( BitSet versions, byte[] data )
	{
		this.versions = versions;
		this.data = data;
		//checkForData("m_s id=\"page");
	}
	/**
	 * Do we have a mask or are we normal?
	 * @return true if the data of this arc is masked
	 */
	public boolean hasMask()
	{
		return getMask() != null;
	}
	/**
	 * Get the mask
	 * @return the mask
	 */
	public byte[] getMask()
	{
		if ( parent != null )
			return parent.mask;
		else
			return mask;	
	}
	/**
	 * Set the to node
	 * @param to the new to node
	 */
	public void setTo( Node to )
	{
		this.to = to;
	}
	/**
	 * Set the from node
	 * @param from the new from node
	 */
	public void setFrom( Node from )
	{
		this.from = from;
	}
	/**
	 * Get the data of this arc
	 * @return the data as a byte array
	 */
	public byte[] getData()
	{
		if ( parent != null )
			return parent.data;
		else
			return data;
	}
	/**
	 * Get the length of the data of this arc
	 * @return the length as an int
	 */
	public int dataLen()
	{
		if ( parent != null )
			return parent.data.length;
		else
			return data.length;
	}
	/**
	 * Add one version to the arc
	 * @param version the version to add
	 */
	public void addVersion( int version )
	{
		versions.set( version );
		if ( to != null )
			to.addIncomingVersion( version );
		if ( from != null )
			from.addOutgoingVersion( version );
	}
	/**
	 * Add a child to the parent
	 * @param child the child arc
	 */
	public void addChild( Arc child )
	{
		if ( children == null )
		{
			children = new LinkedList<Arc>();
			id = Arc.arcId++;
		}
		children.add( child );
		child.parent = this;
	}
	/**
	 * Convert an Arc to a String for printing out
	 * @return the arc as a string
	 */
	public String toString()
	{
		try
		{
			StringBuffer sb = new StringBuffer();
			if ( from == null )
				sb.append("(0)");
			else if ( from.isIncomingEmpty() )
				sb.append("(s)");
			else
				sb.append("("+from.nodeId+")");
			for ( int i=versions.nextSetBit(1);i>=1;i=versions.nextSetBit(i+1) )
			{
				sb.append( alphabet.charAt(i-1) );
			}
			byte[] realData = getData();
			String dataString = new String( realData );
			dataString = dataString.replace('\r', '/');
			dataString = dataString.replace('\n', '/');
			sb.append( ": ");
			if ( parent != null )
				sb.append( "["+parent.id+":" );
			else if ( children != null )
				sb.append("{"+id+":");
			sb.append( "\""+dataString+"\"" );
			if ( parent != null )
				sb.append( "]" );
			else if ( children != null )
				sb.append("}");
			if ( to == null )
				sb.append("(0)");
			else if ( to.isOutgoingEmpty() )
				sb.append("(e)");
			else
				sb.append("("+to.nodeId+")");
			return sb.toString();
		}
		catch ( Exception e )
		{
			System.out.println(e);
			return "";
		}
	}
	/**
	 * Split an arc into two. This can get complex because arcs may be children 
	 * of a transposed parent or the parent itself or just a plain vanilla arc.
	 * @param offset the offset into data pointing to the first byte of the rhs
	 * @return an array of split arcs (or maybe just one)
	 */
	Arc[] split( int offset ) throws MVDException
	{
		Arc[] arcs=null;
		// handle simple cases first
		if ( offset == 0 || offset == dataLen())
		{
			arcs = new Arc[1];
			arcs[0] = this;
		}
		else if ( parent == null )
		{
			if ( children == null )
				arcs = splitDataArc( offset );
			else
				arcs = splitParent( offset, null );
		}
		else 
			arcs = splitChild( offset );
		return arcs;
	}
	/**
	 * Tell our parent to split, and that will split us
	 * @param offset the offset at which to split
	 * @return the split child arcs
	 */
	private Arc[] splitChild( int offset ) throws MVDException
	{
		Arc splitParent = parent;
		while ( splitParent.parent != null )
			splitParent = parent;
		return splitParent.splitParent( offset, this );
	}
	/**
	 * Split us, the parent, and adjust our children
	 * @param offset the offset at which to split
	 * @param desired if not null return the split arcs of this 
	 * child, not of the parent
	 * @return the split parent or child arc
	 */
	private Arc[] splitParent( int offset, Arc desired ) throws MVDException
	{
		Arc[] arcs = splitDataArc( offset );
		for ( int i=0;i<children.size();i++ )
		{
			Arc child = children.get( i );
			BitSet bs1 = new BitSet();
			bs1.or( child.versions );
			BitSet bs2 = new BitSet();
			bs2.or( child.versions );
			Arc b = new Arc( bs1, arcs[0] );
			Arc c = new Arc( bs2, arcs[1] );
			Node childFrom = child.from;
			Node childTo = child.to;
			childFrom.removeOutgoing( child );
			childTo.removeIncoming( child );
			childFrom.addOutgoing( b );
			childTo.addIncoming( c );
			Node n = new Node();
			n.addIncoming( b );
			n.addOutgoing( c );
			if ( child == desired )
			{
				arcs = new Arc[2];
				arcs[0] = b;
				arcs[1] = c;
			}
		}
		return arcs;
	}
	/**
	 * This is called whenever we have to physically split an arc 
	 * that is not a child and has its own data
	 * @param offset point before which to split
	 * @return an array of two split arcs
	 */
	private Arc[] splitDataArc( int offset ) throws MVDException
	{
		Arc[] arcs = new Arc[2];
		byte[] leftData = new byte[offset];
		byte[] leftMask = null;
		if ( hasMask() )
			leftMask = new byte[offset];
		BitSet leftVersions = new BitSet();
		BitSet rightVersions = new BitSet();
		leftVersions.or( versions );
		rightVersions.or( versions );
		for ( int i=0;i<offset;i++ )
		{
			leftData[i] = data[i];
			if ( leftMask != null )
				leftMask[i] = mask[i];
		}
		if ( leftData != null )
			arcs[0] = new Arc( leftVersions, leftData, leftMask );
		else
			arcs[0] = new Arc( leftVersions, leftData );
		byte[] rightData = new byte[dataLen()-offset];
		byte[] rightMask = null;
		if ( hasMask() )
			rightMask = new byte[dataLen()-offset];
		for ( int i=offset,j=0;i<dataLen();i++,j++ )
		{
			rightData[j] = data[i];
			if ( rightMask != null )
				rightMask[j] = mask[i];
		}
		if ( rightData != null )
			arcs[1] = new Arc( rightVersions, rightData, rightMask );
		else
			arcs[1] = new Arc( rightVersions, rightData );
		installSplit( arcs );
		return arcs;
	}
	/**
	 * Replace this arc in the graph with two split ones
	 * @param arcs two arcs to replace this one
	 */
	private void installSplit( Arc[] arcs ) throws MVDException
	{
		// now replace the existing arc with the two split ones
		from.replaceOutgoing( this, arcs[0] );
		Node inter = new Node();
		inter.addIncoming( arcs[0] );
		inter.addOutgoing( arcs[1] );
		to.replaceIncoming( this, arcs[1] );
	}
	/**
	 * Required for membership tests in hashmaps and treemaps
	 */
	public boolean equals( Object other )
	{
		Arc otherArc = (Arc)other;
		return versions.equals(otherArc.versions)
			&&dataEquals(otherArc)&&from==otherArc.from
			&&to==otherArc.to;
	}
	/**
	 * Is the data of the two arcs equal?
	 * @param otherArc the other arc to compare
	 * @return true if they two arcs have same data
	 */
	private boolean dataEquals( Arc otherArc )
	{
		byte[] data1 = getData();
		byte[] data2 = otherArc.getData();
		if ( (data1==null&&data2!=null)||(data1!=null&&data2==null) )
			return false;
		else if ( data1==null&&data2==null)
			return true;
		else if ( data1.length != data2.length )
			return false;
		else
		{
			for ( int i=0;i<data1.length;i++ )
				if ( data1[i] != data2[i] )
					return false;
		}
		return true;
	}
	/**
	 * Is this arc a hint?
	 * @return true if it is a hint
	 */
	boolean isHint()
	{
		return versions.nextSetBit(0)==0;
	}
	/**
	 * Convert and Arc to its pair form
	 * @param parents map of available parents
	 * @param orphans map of available orphans
	 * @return the pair
	 */
	Pair toPair( HashMap<Arc,Pair> parents, HashMap<Arc,Pair> orphans ) 
		throws MVDException
	{
		if ( versions.nextSetBit(0)==0)
			throw new MVDException("Ooops! hint detected!");
		Pair p = new Pair( versions, data );
		if ( this.parent != null )
		{
			// we're a child - find our parent
			Pair q = parents.get( parent );
			if ( q != null )
			{
				q.addChild( p );
				// if this is the last child of the parent remove it
				if ( parent.numChildren() == q.numChildren() )
					parents.remove( parent );
			}
			else	// we're orphaned for now
				orphans.put( this, p );
		}
		else if ( children != null )
		{
			// we're a parent
			for ( int i=0;i<children.size();i++ )
			{
				Arc child = children.get( i );
				Pair r = orphans.get( child );
				if ( r != null )
				{
					p.addChild( r );
					orphans.remove( child );
				}
			}
			if ( p.numChildren() < this.numChildren() )
				parents.put( this, p );
		}
		return p;
	}
	/**
	 * Does the arc have no data?
	 * @return true if so
	 */
	public boolean isEmpty()
	{
		return data.length == 0;
	}
	/**
	 * Is this arc not attached to any node at the end?
	 * @return true if this is so
	 */
	boolean unattached()
	{
		return to == null;
	}
	// extra routines for nmerge
	/**
	 * Get the from node
	 * @return a Node possibly null
	 */
	public Node getFrom()
	{
		return from;
	}
	/**
	 * Get the to node
	 * @return a Node possibly null
	 */
	public Node getTo()
	{
		return to;
	}
	/**
	 * Remove a child from the parent
	 * @param child the child arc
	 */
	public void removeChild( Arc child )
	{
		assert children.contains( child ) : 
			"removeChild: child "+child+" not found!";
		children.remove( child );
		if ( children.size() == 0 )
			children = null;
	}
	/**
	 * How many children do we have?
	 * @return the number of children in the child list
	 */
	int numChildren()
	{
		return (children==null)?0:children.size();
	}
	/**
	 * Is this arc a parent?
	 * @return true if it has children
	 */
	boolean isParent()
	{
		return children != null && children.size()>0;
	}
	/**
	 * Is this arc a child?
	 * @return true if it has a parent
	 */
	boolean isChild()
	{
		return parent != null;
	}
	/**
	 * We are dying. Pass our data on to our children. This
	 * happens if we are being deleted.
	 */
	public void passOnData()
	{
		ListIterator<Arc> iter = children.listIterator();
		Arc newParent = iter.next();
		newParent.data = this.data;
		newParent.setParent( null );
		while ( iter.hasNext() )
		{
			Arc a = iter.next();
			newParent.addChild( a );
		}
	}
	/**
	 * Set the new parent in case of adoption
	 * @param parent the new parent
	 */
	void setParent( Arc parent )
	{
		this.parent = parent;
	}
	/**
	 * Verify that our internal data is up to snuff
	 * @throws MVDException throw an exception if not
	 */
	void verify() throws MVDException
	{
		if ( data == null && parent == null )
			throw new MVDException("Arc data is null and shouldn't be");
	}
	/**
	 * Debug routine
	 * @param d the string to check for when an arc is created
	 */
	void checkForData( String d )
	{
		if ( data != null )
		{
			byte[] bData = d.getBytes();
			if ( bData.length == data.length )
			{
				for ( int i=0;i<bData.length;i++ )
					if ( bData[i]==data[i] )
						System.out.println("Match");
			}
		}
	}
	/**
	 * Do we have a child in the given version?
	 * @param version the version to test for
	 * @return true if we have children and one of them has the version
	 */
	boolean hasChildInVersion( short version )
	{
		if ( children != null )
		{
			ListIterator<Arc> iter = children.listIterator(0);
			while ( iter.hasNext() )
			{
				Arc child = iter.next();
				if ( child.versions.nextSetBit(version)==version )
					return true;
			}
			return false;
		}
		else
			return false;
	}
}