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
/**
 * Represent a single node in the variant graph.
 * @author Desmond Schmidt &copy; 2008
 */
public class Node 
{
	/** the trailing pair of two that define the Node, 
	 * the one after the hint if any */
	Pair pair;
	/** the offset into the A version (the version to which 
	 * we are aligning a new or update version) */
	int aOffset;
	/** the next node in the queue */
	Node next;
	/**
	 * Create an instance of a Node
	 * @param pair the leading pair of the node
	 */
	Node( Pair pair )
	{
		this.aOffset = -1;
		this.pair = pair;
	}
	/**
	 * Prune this node from the queue if its aOffset is less than the given one
	 * @param toOffset the offset below which to prune Nodes
	 * @return the node at the head of the queue
	 */
	Node prune( int toOffset )
	{
		if ( aOffset < toOffset )
			return next.prune( toOffset );
		else
			return this;
	}
	/**
	 * Add a node to the end of the queue. This recursive definition should 
	 * suffice since we won't ever have more than a few nodes in the queue
	 * @param tail the node to append
	 * @return the node appended
	 */
	Node append( Node tail )
	{
		if ( next == null )
		{
			next = tail;
			return tail;
		}
		else
			return next.append( tail );
	}
	/**
	 * Set the a offset
	 * @param aOffset the value to set it to
	 */
	void setAOffset( int aOffset )
	{
		this.aOffset = aOffset;
	}
}
