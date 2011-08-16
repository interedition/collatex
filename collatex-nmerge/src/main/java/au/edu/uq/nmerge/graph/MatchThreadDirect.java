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

import au.edu.uq.nmerge.graph.suffixtree.SuffixTree;
import au.edu.uq.nmerge.graph.suffixtree.SuffixTreePosition;
import java.util.BitSet;
import java.util.ListIterator;

/**
 * A MatchThread examines a run of characters, starting at some 
 * position, in a single arc. It could be a Thread, but in this 
 * implementation it is not because it is probably faster this 
 * way (no blocking required).
 * @author Desmond Schmidt 3/11/08 modified to use MUMs 14/1/09 
 * modified for new path specification 21/1/09
 */
public class MatchThreadDirect implements Runnable
{
	/** the MUM we need to update */
	protected MaximalUniqueMatch mum;
	/** the subgraph we are searching through */
	protected VariantGraph graph;
	/** the Suffix Tree we are matching against */
	protected SuffixTree<Byte> st;
	/** the last node we have seen before the match started */
	VariantGraphNode start;
	/** the start offset from start in bytes */
	int offset;
	/** the first offset in the current arc */
	int first;
	/** distance the match is from the special arc (transpose only) */
	protected int travelled;
	/** the arc being currently searched */
	VariantGraphArc arc;
	/** the current position in the SuffixTree */
	protected SuffixTreePosition position;
	/** Versions shared by all arcs in the path */
	protected BitSet versions;
	/** overall length of the path in bytes */
	protected int pathLen;
	/** array of immediately preceding bytes in the variant graph */
	protected PrevChar[] prevChars;
	/** Don't pass through this node. This is the start node of the 
	 * directly opposite subgraph. Since we only follow printed 
	 * nodes that can be reached from the forbidden node it is 
	 * impossible to miss it as we come back to the right. It thus 
	 * acts as a kind of barrier. */
	VariantGraphNode forbidden;
	/** true if a child was extended */
	boolean extended = false;
	/**
	 * Required default constructor.
	 */
	public MatchThreadDirect()
	{
	}
	/**
	 * A MatchThread looks for matches in the suffix tree 
	 * vis-a-vis the variant graph.
	 * @param mum the MUM to update if we find a longer match
	 * @param st the suffix tree of the arc
	 * @param arc the arc we are currently searching 
	 * @param graph the subgraph containing the arc
	 * @param start the last node we have seen
	 * @param prevChars an array of possible bytes that immediately 
	 * precede this match
	 */
	public MatchThreadDirect( MaximalUniqueMatch mum, VariantGraph graph, SuffixTree<Byte> st, VariantGraphArc arc,
		VariantGraphNode start, int offset, PrevChar[] prevChars, VariantGraphNode forbidden )
	{
		this.mum = mum;
		this.first = offset;
		this.st = st;
		this.arc = arc;
		this.start = start;
		this.graph = graph;
		this.position = new SuffixTreePosition( null, 0 );
		this.offset = offset;
		this.prevChars = prevChars;
		this.forbidden = forbidden;
	}
	/**
	 * Copy constructor for recursion
	 * @param mtd the MatchThreadDirect object to clone
	 */
	protected MatchThreadDirect( MatchThreadDirect mtd )
	{
		this.mum = mtd.mum;
		this.graph = mtd.graph;
		this.st = mtd.st;
		this.arc = mtd.arc;
		this.start = mtd.start;
		// copying this from the parent instance was a bug
		// it should always be 0
		this.first = 0;
		this.offset = mtd.offset;
		// don't forget to duplicate this!
		// or splits will update each other
		this.position = new SuffixTreePosition( mtd.position.node, mtd.position.edgePos );
		this.versions = new BitSet();
		this.versions.or( mtd.versions );
		this.pathLen = mtd.pathLen;
		this.prevChars = mtd.prevChars;
		this.travelled = mtd.travelled;
		this.forbidden = mtd.forbidden;
	}
	/**
	 * Did we match at least one byte?
	 * @return true if we did
	 */
	boolean extended()
	{
		return first>0||extended;
	}
	/**
	 * Search for runs starting in the current arc. If we get to 
	 * the end we call ourself recursively. This is because we may 
	 * have to split at the next node: a loop isn't possible.
	 */
	public void run() 
	{
		byte[] data = arc.getData();
		while ( first < data.length
			&& st.advance(position,data[first]) )
		{
			first++;
			pathLen++;
		}
		if ( first < data.length )
		{
			// If we report a mismatch in the first byte then 
			// multiple recursions at the end of a fully matched arc 
			// might fail and they will all get reported as mismatches  
			// of the same (preceding) arc. This incorrectly turns MUMs 
			// into MEMs. Hence we only report a mismatch if we match 
			// at least one character. If we were the first such 
			// incarnation of run(), matching 0 characters is in any case
			// uninteresting. See also the updateArc method.
			if ( first > 0 )
				mismatch();
		}
		else
			updateArc();
	}
	/**
	 * Mismatch in the current arc. We apply three tests to assess if a 
	 * match is valid: <ol><li> We don't accept matches below the minimum size.
	 * </li><li> Reject matches that don't end in a leaf. These can't be unique 
	 * and so can't be MUMs.</li><li> Reject any match that is not maximal. Using 
	 * the prevChars array we can work out if the match continues backwards 
	 * from its start by at least one character. Then it is not maximal and 
	 * hence is not a MUM.</li></ol> The result of applying these three tests 
	 * is that around 85% of matches that make it through are actually MUMs. The 
	 * remainder are filtered out by counting frequencies in the MUM class. See 
	 * {@link au.edu.uq.nmerge.mvd.MUM#getMatch() getMatch}.
	 */
	protected void mismatch()
	{
		// first test: are we long enough?
		if ( pathLen >= MaximalUniqueMatch.MIN_LEN )
		{
			// second test: are we unique in the suffix tree?
			if ( position.node.isLeaf() )
			{
				// third test
				if ( isMaximal() )		
				{
					addToPath( arc );
					// if we matched at least one byte of the current arc
					mum.update( start, offset, versions, 
						position.edgePos-pathLen, pathLen, travelled );
				}
				// else it's a substring of the maximum match
			}
			// else it can't be a unique match
		}
		// else it's a tiddler: throw it back!
	}
	/**
	 * Does the current location of pos point to a maximal match. 
	 * I.e. are all possibly preceding bytes not equal to the 
	 * preceding byte in the data arc?
	 * @return true only if the match is maximal
	 */
	protected boolean isMaximal()
	{
		int prevCharIndex = position.edgePos-(pathLen+1);
		if ( prevCharIndex >= 0 )
		{
			byte dataPrevChar = mum.arc.getData()[prevCharIndex];
			if ( prevCharIndex >= 0 )
			{
				BitSet pathVersions = new BitSet();
				if ( versions != null )
					pathVersions.or( versions );
				pathVersions.and( arc.versions );
				for ( int i=0;i<prevChars.length;i++ )
				{
					if ( prevChars[i] == null )
						System.out.println("null");
					if ( prevChars[i].previous == dataPrevChar 
						&& prevChars[i].versions.intersects(pathVersions) )
						return false;
				}
			}
		}
		return true;
	}
	/** 
	 * Move on to the next arc(s) - if you can - by recursion. There is a 
	 * problem here if we recurse into more than one child arc and NONE 
	 * of them match anything. Then we will get multiple calls to mismatch 
	 * (and hence generate multiple MUMs for the same match). This turns MUMs 
	 * into MEMs and so discards them. The revised code here checks for this 
	 * condition and avoids the problem.
	 */
	protected void updateArc()
	{
		// arc was fully matched - save it
		addToPath( arc );
		if ( arc.to != forbidden )
		{
			ListIterator<VariantGraphArc> iter = arc.to.outgoingArcs( graph );
			while ( iter.hasNext() )
			{
				VariantGraphArc a = iter.next();
				if ( a.versions.intersects(versions)&&(!a.isParent()
					||!a.hasChildInVersion(mum.version)) )
				{
                  this.arc = a;
                  //this.first = 0;
                  MatchThreadDirect mtd = new MatchThreadDirect( this );
                  mtd.run();
                  // extended is true iff at least one child
                  // mtd matched at least one character
                  extended |= mtd.extended();
                }
			}
		}
		// important! if none of the child incarnations of run
		// matched anything BUT we did, THEN call mismatch
		if ( !extended && first > 0 )
			mismatch();
	}
	/**
	 * Only preserve shared versions on the path. That is, 
	 * if a path consists of AB,BC,AB then the path belongs to 
	 * version B, or the AND of all the sets of versions.
	 * @param arc the arc to add to the current path
	 */
	protected void addToPath( VariantGraphArc arc )
	{
		if ( versions == null )
		{
			versions = new BitSet();
			versions.or( arc.versions );
			if ( graph != null )
				versions.and( graph.constraint );
		}
		else
			versions.and( arc.versions );
	}
}
