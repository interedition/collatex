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
 * Keep track of a set of chunk states common to a run of successive 
 * pairs belonging to a particular version. Used when generating the 
 * output from the compare function.
 * @author Desmond Schmidt
 */
public class ChunkStateSet 
{
	/** this is what defines who we are */
	private ChunkState[] states;
	/** pass this on to child sets */
	private short backup;
	/**
	 * Construct a simple one-state chunk state set
	 */
	public ChunkStateSet()
	{
		backup = (short)0;
		add( ChunkState.none );
	}
	/**
	 * Construct a simple one-state chunk state set
	 * @param backup the backup version
	 * @param state the chunk state to start with
	 */
	public ChunkStateSet( short backup, ChunkState state )
	{
		this.backup = backup;
		add( state );
	}
	/**
	 * Create a default empty set with a backup we will pass on to our 
	 * children
	 * @param backup the backup version (usually NO_BACKUP)
	 */
	public ChunkStateSet( short backup )
	{
		this.backup = backup;
		add( ChunkState.none );
	}
	/**
	 * Copy constructor
	 * @param set a set of states to copy deeply
	 */
	public ChunkStateSet( ChunkStateSet set )
	{
		this.states = new ChunkState[set.states.length];
		for ( int i=0;i<set.states.length;i++ )
			this.states[i] = set.states[i];
		this.backup = set.backup;
	}
	/**
	 * Create a new ChunkState set
	 * @param backup the backup version (usually NO_BACKUP)
	 * @param states a preconstructed array of chunk states
	 */
	public ChunkStateSet( ChunkState[] states, short backup )
	{
		this.backup = backup;
		this.states = states;
	}
	/**
	 * Add a state to the chunk. A chunk can have more than one state 
	 * such as found and deleted.
	 * @param state the state to add
	 */
	public void add( ChunkState state )
	{
		if ( !containsState(state) )
		{
			if ( states == null )
				states = new ChunkState[1];
			else if ( containsState(ChunkState.none) )
			{
				for ( int i=0;i<states.length;i++ )
				{
					if ( states[i] == ChunkState.none )
					{
						states[i] = state;
						break;
					}
				}
			}
			else
			{
				// expand 
				ChunkState[] newStates = new ChunkState[states.length+1];
				for ( int i=0;i<states.length;i++ )
					newStates[i] = states[i];
				states = newStates;
			}
			states[states.length-1] = state;
		}
	}
	/**
	 * Is this chunk found?
	 * @return true if it is
	 */
	public boolean isFound()
	{
		return containsState( ChunkState.found );
	}
	/**
	 * Is this chunk the parent of a transposition?
	 * @return true if it is
	 */
	public boolean isParent()
	{
		return containsState( ChunkState.parent );
	}
	/**
	 * Is this chunk the child of a transposition?
	 * @return true if it is
	 */
	public boolean isChild()
	{
		return containsState( ChunkState.child );
	}
	/**
	 * Does this chunk state set contain the given state?
	 * @param state the state to test for
	 * @return true if it is present
	 */
	public boolean containsState( ChunkState state )
	{
		if ( states != null )
		{
			for ( int i=0;i<states.length;i++ )
				if ( states[i] == state )
					return true;
		}
		return false;
	}
	/**
	 * Is this chunk a backup?
	 * @return true if it is
	 */
	public boolean isBackup()
	{
		return containsState( ChunkState.backup );
	}
	/**
	 * Is this chunk merged?
	 * @return true if it is
	 */
	public boolean isMerged()
	{
		return containsState( ChunkState.merged );
	}
	/**
	 * Does this set only contain the none state?
	 * @return true if it is
	 */
	public boolean isEmpty()
	{
		return (states==null) || (states.length==1&&states[0]==ChunkState.none);
	}
	/**
	 * Using information contained in a supplied new pair, 
	 * compute a new transpose state
	 * @param p the pair to shift states
	 * @param state the state for versions missing in second, e.g. 
	 * deleted or added
	 * @param v the second version compared to first
	 * @return a new Chunkstate or the same one as us no change
	 */
	ChunkStateSet next( Pair p, ChunkState state, short v )
	{
		ChunkStateSet repl = this;
		if ( !p.contains(v) )
		{
			if ( !containsState(state) )
			{
				repl = new ChunkStateSet( backup );
				repl.add( state );
			}
		}
		// contains version v
		else if ( backup != Version.NO_BACKUP )
		{
			if ( !isBackup() )
			{
				repl = new ChunkStateSet( backup );
				repl.add( ChunkState.backup );
			}
		}
		else if ( !isMerged() )
		{
			repl = new ChunkStateSet( backup );
			repl.add( ChunkState.merged );
		}
		return repl;
	}
	/**
	 * Get the states stored here
	 * @return an array of chunk states
	 */
	ChunkState[] getStates()
	{
		return states;
	}
	/**
	 * Are two sets of chunk states equal?
	 * @param other the other set of states
	 * @return true if they are equal
	 */
	public boolean equals( ChunkStateSet other )
	{
		if ( this.states.length == other.states.length )
		{
			ChunkState[] s = states;
			for ( int i=0;i<s.length;i++ )
				if ( !other.containsState(s[i]) )
					return false;
			return true;
		}
		else
			return false;
	}
	/**
	 * Convert these states to a string for incorporation into a chunk
	 * @return the state set as a string
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		if ( states != null )
		{
			for ( int i=0;i<states.length;i++ )
			{
				sb.append( states[i].toString() );
				if ( i<states.length-1 )
					sb.append( "," );
			}
		}
		else
			sb.append("empty");
		return sb.toString();
	}
	/**
	 * Get the backup version
	 * @return the backup as a short
	 */
	public short getBackup()
	{
		return backup;
	}
}
