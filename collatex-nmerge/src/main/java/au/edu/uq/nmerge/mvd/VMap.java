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

import java.util.HashMap;
import java.util.Vector;

public class VMap extends HashMap<Pair, Pair> 
{
	private static final long serialVersionUID = 1L;
	/**
	 * Generate a map of child or parent pairs that follow 
	 * one another in the pairs list belonging to version v
	 * @param v the version to test for
	 * @return a map of pair-pair relations
	 */
	VMap( short v, Vector<Pair> pairs )
	{
		HashMap<Pair,Pair> vMap = new HashMap<Pair,Pair>();
		if ( pairs.size() > 1 )
		{
			Pair last = null;
			for ( int i=0;i<pairs.size();i++ )
			{
				// examine all the v-pairs
				Pair p = pairs.get( i );
				if ( p.contains(v) )
				{
					if ( last == null )
						continue;
					else if ( last.isChild() && p.isChild() )
						vMap.put( last, p );
					else if ( last.isParent() && p.isParent() )
						vMap.put( last, p );
					last = p;
				}
			}
		}
	}
	/**
	 * Work out if a pair is contiguous as a parent or as a child 
	 * from the last pair to the current one. We know that p follows
	 * last in version u. All we need to compute is if their children 
	 * or parents in version v also follow one another. The vMap 
	 * stores all the parents or children that follow one another in 
	 * version v. So we just look in there for the children of the 
	 * parents in u or the parents of the children in u.
	 * @param last the previous pair, in version u
	 * @param p the current pair in version u
	 * @param v the version we are comparing to
	 * @return true if last and p both follow one another in their 
	 * respective versions
	 */
	boolean isContiguous( Pair last, Pair p, short v )
	{
		if ( last == null )
			return false;
		else if ( last.isParent() )
		{
			// simple test: if not both parents 
			// can't be contiguous
			if ( !p.isParent() )
				return false;
			else
			{
				// parents must have contiguous children in v
				Pair child1 = last.getChildInVersion( v ); 
				Pair child2 = p.getChildInVersion( v );
				if ( child1 != null && child2 != null )
					return get(child1)==child2 
						|| get(child2)==child1;
				else
					return false;
			}
		}
		else if ( last.isChild() )
		{
			if ( !p.isChild() )
				return false;
			else
			{
				// children must have contiguous parents in v
				Pair parent1 = last.getParent();
				Pair parent2 = p.getParent();
				boolean ans1 = parent1.contains( v ); 
				boolean ans2 = parent2.contains( v );
				if ( ans1 && ans2 )
				{
					return get(parent1)==parent2
						|| get(parent2)==parent1;
				}
				else
					return false;
			}
		}
		return true;
	}
}
