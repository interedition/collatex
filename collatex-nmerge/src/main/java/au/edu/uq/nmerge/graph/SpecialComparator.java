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

import java.util.Comparator;
public class SpecialComparator implements Comparator<SpecialArc> 
{
	/**
	 * This is used in TreeMap to order the keys. We sort first on MUM 
	 * values and then on alphabetical byte values. Order is reversed so 
	 * that the longest MUM will be at the top of the queue not the 
	 * smallest.
	 * @param one a special arc to compare with two
	 * @param two the two special arc to compare
	 * @return 0 if equal, -1 if we are greater, 1 if less (for reverse 
	 * ordering)
	 */
	public int compare( SpecialArc one, SpecialArc two ) 
	{
		int oneLen = one.dataLen();
		int twoLen = two.dataLen();
		int mumValue = (one.best!=null)?one.best.compareTo(two.best):0;
		if ( mumValue == 0 )
		{
			// MUMs equal: compare the data
			for ( int i=0;i<oneLen&&i<twoLen;i++ )
				if ( one.data[i] < two.data[i] )
					return 1;
				else if ( one.data[i] > two.data[i] )
					return -1;
			if ( oneLen<twoLen )
				return 1;
			else if ( oneLen>twoLen )
				return -1;
			else if ( one.equals(two) )
				return 0;
			// data equal: compare the from nodes and to nodes
			else if ( one.from != null )
			{
				if ( two.from != null )
				{
					if ( one.from.nodeId > two.from.nodeId )
						return -1;
					else if ( one.from.nodeId != two.from.nodeId )
						return 1;
					// from nodes equal, try to nodes
					else if ( one.to != null )
					{
						if ( two.to != null )
						{
							if ( one.to.nodeId > two.to.nodeId )
								return -1;
							else if ( one.to.nodeId < two.to.nodeId )
								return 1;
							else
								return 0;
						}
						else
							return -1;
					}
					else if ( two.to != null )
						return 1;
					else
						return 0;
				}
				else
					return -1;
			}
			else if ( two.from != null )
				return 1;
			else
				return 0;
		}
		return mumValue;
	}
}
