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
import java.util.ListIterator;


/**
 * This version of MatchThread searches the arcs to the right 
 * of the subgraph using a different technique. We basically 
 * let matches run on as far as they need to.
 * @author Desmond Schmidt 31/1/09
 */
public class MatchThreadTransposeRight extends MatchThreadDirect
{
	/**
	 * Constructor for thread to search for matches
	 * @param mum the mum we have to update
	 * @param st the suffix tree representing the new version 
	 * @param a the arc to start searching from
	 * @param first the offset into a at which to start
	 * @param prevChars an array of characters preceding a[first]
	 * @param travelled the distance from the special arc
	 * @param forbidden don't travel beyond this node (should be null)
	 */
	MatchThreadTransposeRight( MaximalUniqueMatch mum, SuffixTree<Byte> st, VariantGraphArc a,
		int first, PrevChar[] prevChars, int travelled, VariantGraphNode forbidden )
	{
		super( mum, null, st, a, a.from, first, prevChars, forbidden );
		this.travelled = travelled;
	}
	/**
	 * Copy constructor for recursion
	 * @param mttr the MatchThreadTransposeLeft object to clone
	 */
	protected MatchThreadTransposeRight( MatchThreadTransposeRight mttr )
	{
		super( mttr );
	}
	/** 
	 * Move on to the next arc(s) - if you can - by recursion.
	 * This is an override of the direct method.
	 */
	protected void updateArc()
	{
		// arc was fully matched - save it
		addToPath( arc );
		boolean extended = false;
		ListIterator<VariantGraphArc> iter = arc.to.outgoingArcs();
		while ( iter.hasNext() )
		{
			VariantGraphArc a = iter.next();
			if ( a.versions.intersects(versions)
				&&a.versions.nextSetBit(mum.version)!=mum.version
				&&(!a.isParent()||!a.hasChildInVersion(mum.version)) )
			{
				this.arc = a;
				this.first = 0;
				MatchThreadTransposeRight mttr = new MatchThreadTransposeRight( this );
				mttr.run();
				extended |= mttr.first > 0;
			}
		}
		if ( !extended )
			mismatch();
	}
}
