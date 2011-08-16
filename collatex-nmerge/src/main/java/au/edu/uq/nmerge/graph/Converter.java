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
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;
import java.util.HashMap;

import au.edu.uq.nmerge.exception.*;
import au.edu.uq.nmerge.mvd.Match;

/**
 * The purpose of this class is to serialise and deserialise 
 * an explicit variant graph to and from its pairs list 
 * representation. The main methods are create and serialise.
 * @author Desmond Schmidt 12/7/08
 */
public class Converter 
{
	/** debug */
	static int numParents;
	/** incomplete nodes during build */
	HashSet<VariantGraphNode> incomplete;
	/** unattached arcs during build */
	UnattachedSet unattached;
	/** used to optimise the size of the pairs list vector */
	int origSize;
	/** all versions contained in the graph */
	BitSet allVersions;
	/** current number of arcs */
	int nArcs;
	/** current number of nodes */
	int nNodes;
	/** the graph we are building */
	VariantGraph graph;
	/** map to help find parents for children */
	HashMap<VariantGraphArc,Match> parents;
	/** map to help find children for parents */
	HashMap<VariantGraphArc,Match> orphans;
	/**
	 * Create a Graph
	 * @param matches the list of pairs to build the graph from.
	 * @param numVersions the number of versions to go in the graph
	 */
	public VariantGraph create( Vector<Match> matches, int numVersions )
		throws Exception
	{
		unattached = new UnattachedSet();
		incomplete = new HashSet<VariantGraphNode>();
		allVersions = new BitSet();
		for ( int i=1;i<=numVersions;i++ )
			allVersions.set( i );
		graph = new VariantGraph();
		origSize = matches.size();
		if ( matches.size() > 0 )
			deserialise(matches);
		// generate the constraint set
		for ( int i=1;i<=numVersions;i++ )
			graph.constraint.set( i );
		parents = new HashMap<VariantGraphArc,Match>();
		orphans = new HashMap<VariantGraphArc,Match>();
		return graph;
	}
	/**
	 *	Create a Node. 
	 *	@return the new Node
	 */
	private VariantGraphNode createNode()
	{
		nNodes++;
		return new VariantGraphNode();
	}
	/**
	 *	Parse the data to build the graph from the pairs
	 *	@param matches a Vector containing the pairs to build
	 *	into a graph
	 */
	private void deserialise( Vector<Match> matches) throws Exception
	{
		graph.start = createNode();
		VariantGraphNode u = graph.start;
		HashMap<Match,VariantGraphArc> pnts = new HashMap<Match,VariantGraphArc>();
		HashMap<Match,VariantGraphArc> kids = new HashMap<Match,VariantGraphArc>();
		// go through the pairs and turn them into arcs
		for ( int i=0;i< matches.size();i++ )
		{
			VariantGraphNode v;
			Match p = matches.get( i );
			VariantGraphArc a = pairToArc( p, pnts, kids );
			if ( (i>0 && 
				a.versions.intersects(matches.get(i-1).versions))
				|| a.isHint() )
				u = v = createNode();
			else
				v = getIntersectingNode( u, a );
			v.addOutgoing( a );
			BitSet bs = a.versions;
			// in case a is itself a hint
			// don't attach incoming arcs that are hints
			if ( a.isHint() )
			{
				bs = cloneVersions( a.versions );
				bs.clear( 0 );
			}
			unattached.addAsIncoming( v, bs );
			unattached.add( a );
			// update incomplete set
			boolean x = v.isIncomplete();
			boolean y = incomplete.contains(v);
			if ( x && !y )
				incomplete.add( v );
			else if ( !x && y )
			{
				incomplete.remove( v );
				v.optimise( unattached );
			}
		}
		// attach dangling arcs to end node
		graph.end = createNode();
		unattached.addAllAsIncoming( graph.end );
		// check that all children found parents and vice versa
		if ( pnts.size() != 0 )
			throw new MVDException(
				"Unmatched parent node(s) after deserialisation");
		if ( kids.size() != 0 )
			throw new MVDException(
				"Unmatched child node(s) after deserialisation");
	}
	/**
	 * Regenerate the list of pairs by writing out the Graph
	 * @return a list of pairs with hints where needed
	 */
	public Vector<Match> serialise() throws MVDException
	{
		Match.pairId = 1;
		if ( origSize < 15 )
			origSize = 15;
		numParents = 0;
		Vector<Match> matches = new Vector<Match>( origSize );
		printAcross(matches, graph.start, allVersions );
		if ( parents.size() != 0 )
			throw new MVDToolException("Mismatched parent arc");
		if ( orphans.size() != 0 )
			throw new MVDToolException("Mismatched child arc");
		return matches;
	}
	/**
	 *	Find the node that should receive an outgoing arc
	 *	@param u the current node
	 *	@param a the arc for which the from node is needed
	 */
	private VariantGraphNode getIntersectingNode( VariantGraphNode u, VariantGraphArc a ) throws Exception
	{
		VariantGraphNode v;
		VariantGraphArc b = unattached.getIntersectingArc( a );
		if ( b != null )
		{
			if ( b.isHint() )
			{
				v = b.from;
				if ( unattached.removeEmptyArc(b,a.versions) )
					nArcs--;
			}
			else
				v = u;
		}
		else
		{
			v = u;
			Iterator<VariantGraphNode> iter = incomplete.iterator();
			while ( iter.hasNext() )
			{
				v = iter.next();
				if ( v.wants(a) )
					break;
			}
		}
		return v;
	}
	/**
	 * Convert a Pair to an Arc. The main problem here is keeping  
	 * track of parents and children involved in transpositions.
	 * This will only work if an entire pairs list is deserialised 
	 * into a graph in one go.
	 * @param p the pair to convert
	 * @param pnts potential parents of the new pair
	 * @param kids kids looking for parents
	 * @return an equivalent Arc we can use in the Graph
	 */
	private VariantGraphArc pairToArc( Match p, HashMap<Match,VariantGraphArc> pnts,
		HashMap<Match,VariantGraphArc> kids )
	{
		nArcs++;
		byte[] pData = (p.isChild()||p.isHint())?null:p.getData();
		VariantGraphArc a = new VariantGraphArc( cloneVersions(p.versions), pData );
		if ( p.isChild() )
		{
			// we're a child - find our parent
			Match parent = p.getParent();
			VariantGraphArc b = pnts.get( parent );
			if ( b != null )
			{
				b.addChild( a );
				// if this is the last child of the parent remove it
				if ( b.numChildren() == parent.numChildren() )
					pnts.remove( parent );
			}
			else	// we're orphaned for now
				kids.put( p, a );
		}
		else if ( p.isParent() )
		{
			ListIterator<Match> iter = p.getChildIterator();
			while ( iter.hasNext() )
			{
				Match child = iter.next();
				VariantGraphArc r = kids.get( child );
				if ( r != null )
				{
					a.addChild( r );
					kids.remove( child );
				}
			}
			if ( p.numChildren() > a.numChildren() )
				pnts.put( p, a );
		}
		return a;
	}
	/**
	 * The clone method is not infallible for bitsets
	 * @param bs the bitset to clone
	 * @return a cloned bitset
	 */
	private BitSet cloneVersions( BitSet bs )
	{
		BitSet clone = new BitSet();
		for ( int i=bs.nextSetBit(0);i>=0;i=bs.nextSetBit(i+1) )
			clone.set( i );
		return clone;
	}
	/**
	 *	Build a bit of the pairs-list starting from a node 
	 *	@param tuples the part-built pairs-list
	 *	@param u the node from which to take outgoing arcs
	 *	@param incoming the versions of the last incoming arc
	 */
	private void printAcross( Vector<Match> matches, VariantGraphNode u, BitSet incoming )
		throws MVDException
	{
		int hint = -1;
		VariantGraphArc selected = u.pickOutgoingArc( incoming );
		if ( selected != null )
		{
			assert !selected.to.isPrintedIncoming( selected.versions );
			// add an empty tuple as a hint if required
			BitSet clique = u.getClique(selected);
			if ( !clique.isEmpty() )
			{
				hint = matches.size();
				//System.out.println("creating hint at "+hint);
				clique.set( 0 );
				// create a hint
				Match h = new Match(clique,new byte[0]);
				matches.add( h );
			}
			hint = printDown(matches, selected, hint );
			ListIterator<VariantGraphArc> iter = u.outgoingArcs();
			while ( iter.hasNext() )
			{
				VariantGraphArc a = iter.next();
				if ( a != selected )
					hint = printDown(matches, a, hint );
			}
		} 
	}
	/**
	 *	Print a single tuple to the list. If this is the last incoming 
	 *	arc, then call printAcross.
	 *	@param tuples the part-built tuple-list
	 *	@param a the arc to print
	 *	@param hint the previous location of a hint
	 *	@return the hint or -1 if it was removed
	 */
	private int printDown( Vector<Match> matches, VariantGraphArc a, int hint )
		throws MVDException
	{
		if ( a.numChildren() > 0 )
			numParents++;
		Match p = a.toPair( parents, orphans );
		matches.add( p );
		a.to.printArc( a );
		if ( a.to != null )
		{
			VariantGraphNode u = a.to;
			if ( u.allPrintedIncoming() )
			{
				if ( hint != -1 )
					hint = reduceHint(matches, hint );
				printAcross(matches, u, a.versions );
				// next node produced, invalidating hint
				hint = -1;
			}
		}
		return hint;
	}
	/**
	 *	Reduce or remove the hint at offset hint and if removing 
	 *	shunt all the tuples along from hint to pos. Otherwise 
	 *	just reduce the number of versions at offset hint.
	 *	@param tuples the array of tuples to write to
	 *	@param hint the offset of the hint
	 *	@param pos the offset of the current write position
	 *	@return the index of the hint or -1 if it was removed
	 */
	private int reduceHint( Vector<Match> matches, int hint )
	{
		Match hintMatch = matches.get( hint );
		for ( int i=hint+2;i< matches.size();i++ )
		{
			au.edu.uq.nmerge.mvd.Match p = matches.get( i );
			hintMatch.versions.andNot( p.versions );
			if ( hintMatch.versions.nextSetBit(1)==-1 )
			{
				matches.remove( hint );
				//System.out.println("removing hint at "+hint);
				hint = -1;
				break;
			}
		}
		/*if ( hint != -1 )
			System.out.println("hint at "+hint+" versions="
				+hintPair.versions.toString());*/
		return hint;
	}
	/**
	 *	Is this graph isomorphic to another? We can do this by printing 
	 *	them both simultaneously breadth-first and noting down the same 
	 *	moves. Any mismatch results in failure.
	 *	@param other the other Textgraph to compare with this one
	 *	@return true if they are isomorphic, false otherwise
	 */
	public boolean isIsomorphic( VariantGraph other )
	{
		VariantGraphNode current,otherCurrent;
		NodeQueue q = new NodeQueue();
		NodeQueue otherQueue = new NodeQueue();
		q.push( graph.start );
		otherQueue.push( other.start );
		current = graph.start;
		int numArcs = 0;
		otherCurrent = other.start;
		while ( !q.isEmpty() && !otherQueue.isEmpty() )
		{
			current = q.pop();
			otherCurrent = otherQueue.pop();
			ListIterator<VariantGraphArc> iter = current.outgoingArcs();
			while ( iter.hasNext() )
			{
				VariantGraphArc a = iter.next();
				VariantGraphArc b = otherCurrent.pickOutgoingArc(a.versions);
				numArcs++;
				if ( a == null )
				{
					graph.clearPrinted();
					other.clearPrinted();
					return false;
				}
				if ( b == null )
				{
					graph.clearPrinted();
					other.clearPrinted();
					return false;
				}
				else
				{
					a.to.printArc(a);
					b.to.printArc( b );
				}
				if ( a.to.allPrintedIncoming() )
				{
					q.push( a.to );
					if ( b.to.allPrintedIncoming() )
						otherQueue.push( b.to );
					else
					{
						graph.clearPrinted();
						other.clearPrinted();
						return false;
					}
				}
			}
		}
		graph.clearPrinted();
		other.clearPrinted();
		return true;
	}
	/**
	 * Tell us stats about the graph
	 */
	public void report()
	{
		System.out.println( "nArcs="+nArcs+" nNodes="+nNodes );
	}
}
