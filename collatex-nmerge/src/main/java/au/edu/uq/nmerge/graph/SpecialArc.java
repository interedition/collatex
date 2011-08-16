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

/**
 * A form of unaligned arc that also stores its position from the start, 
 * and its MUM.
 * @author Desmond Schmidt 12/11/08
 */
public class SpecialArc extends Arc
{
	/** if previously calculated save best MUM here */
	MUM best;
	/** position from the start of the new version */
	int position;
	/** 
	 * Override the Arc constructor
	 * @param versions the versions of the arc
	 * @param data the data of the special arc
	 * @param position the position of the arc along the new version
	 */
	public SpecialArc( BitSet versions, byte[] data, int position )
	{
		super( versions, data );
		this.position = position;
		// leave best null
	}
	/** 
	 * Override the Arc constructor
	 * @param versions the versions of the arc
	 * @param data the data of the special arc
	 * @param mask the mask for the special arc
	 * @param position the position of the arc along the new version
	 */
	public SpecialArc( BitSet versions, byte[] data, byte[] mask, 
		int position )
	{
		super( versions, data, mask );
		this.position = position;
		// leave best null
	}
	/**
	 * Get the best LCS or null
	 * @return null (and so you must calculate it) or an LCS
	 */
	public MUM getBest()
	{
		return best;
	}
	/**
	 * Set the best MUM
	 * @param best precalculated MUM or this arc
	 */
	public void setBest( MUM best )
	{
		this.best = best;
	}
	/**
	 * Reset best to null so it will be recalculate when required
	 */
	public void reset()
	{
		best = null;
	}
	/**
	 * Required to equate keys in the treemap: otherwise we 
	 * get duplicates
	 */
	public boolean equals( Object other )
	{
		if ( !(other instanceof SpecialArc) )
			return false;
			else
		{
			SpecialArc otherArc = (SpecialArc)other;
			boolean result = super.equals(other)&&position==otherArc.position;
			//if ( result )
			//	System.out.println("equals!");
			return result;
		}
	}
	/** debug - print out the special arc using mask */
	String printout()
	{
		StringBuffer sb = new StringBuffer(data.length);
		for ( int i=0;i<data.length;i++ )
		{
			if ( mask==null||mask[i]!=0 )
				sb.append((char)data[i]);
		}
		return sb.toString();
	}
	public String toString()
	{
		String matchStr = (best!=null)?best.getMatch().toString():"";
		return super.toString()+" Match: "+matchStr;
	}
}
