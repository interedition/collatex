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

public class WrappedPair 
{
	Match match;
	CompactNode defaultNode;
	WrappedPair( Match match)
	{
		this.match = match;
	}
	CompactNode getDefaultNode()
	{
		return defaultNode;
	}
	Match getMatch()
	{
		return this.match;
	}
	void setDefaultNode( CompactNode cn )
	{
		this.defaultNode = cn;
	}
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "pair: "+ match.toString()+"\n" );
		String defaultNodeString = (defaultNode==null)?"null":defaultNode.toString();
		sb.append( "defaultNode: "+defaultNodeString );
		return sb.toString();
	}
}
