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
 *
 * @author Desmond Schmidt
 */
public class ChunkStateSet {
  /**
   * this is what defines who we are
   */
  private ChunkState[] states;

  /**
   * Construct a simple one-state chunk state set
   */
  public ChunkStateSet() {
    add(ChunkState.NONE);
  }

  /**
   * Construct a simple one-state chunk state set
   *
   * @param state the chunk state to start with
   */
  public ChunkStateSet(ChunkState state) {
    add(state);
  }

  /**
   * Copy constructor
   *
   * @param set a set of states to copy deeply
   */
  public ChunkStateSet(ChunkStateSet set) {
    this.states = new ChunkState[set.states.length];
    for (int i = 0; i < set.states.length; i++)
      this.states[i] = set.states[i];
  }

  /**
   * Create a new ChunkState set
   *
   * @param states a preconstructed array of chunk states
   */
  public ChunkStateSet(ChunkState[] states) {
    this.states = states;
  }

  /**
   * Add a state to the chunk. A chunk can have more than one state
   * such as found and deleted.
   *
   * @param state the state to add
   */
  public void add(ChunkState state) {
    if (!containsState(state)) {
      if (states == null)
        states = new ChunkState[1];
      else if (containsState(ChunkState.NONE)) {
        for (int i = 0; i < states.length; i++) {
          if (states[i] == ChunkState.NONE) {
            states[i] = state;
            break;
          }
        }
      } else {
        // expand
        ChunkState[] newStates = new ChunkState[states.length + 1];
        for (int i = 0; i < states.length; i++)
          newStates[i] = states[i];
        states = newStates;
      }
      states[states.length - 1] = state;
    }
  }

  /**
   * Is this chunk found?
   *
   * @return true if it is
   */
  public boolean isFound() {
    return containsState(ChunkState.FOUND);
  }

  /**
   * Is this chunk the parent of a transposition?
   *
   * @return true if it is
   */
  public boolean isParent() {
    return containsState(ChunkState.PARENT);
  }

  /**
   * Is this chunk the child of a transposition?
   *
   * @return true if it is
   */
  public boolean isChild() {
    return containsState(ChunkState.CHILD);
  }

  /**
   * Does this chunk state set contain the given state?
   *
   * @param state the state to test for
   * @return true if it is present
   */
  public boolean containsState(ChunkState state) {
    if (states != null) {
      for (int i = 0; i < states.length; i++)
        if (states[i] == state)
          return true;
    }
    return false;
  }

  /**
   * Is this chunk merged?
   *
   * @return true if it is
   */
  public boolean isMerged() {
    return containsState(ChunkState.MERGED);
  }

  /**
   * Does this set only contain the none state?
   *
   * @return true if it is
   */
  public boolean isEmpty() {
    return (states == null) || (states.length == 1 && states[0] == ChunkState.NONE);
  }

  /**
   * Using information contained in a supplied new pair,
   * compute a new transpose state
   *
   * @param p     the pair to shift states
   * @param state the state for versions missing in second, e.g.
   *              deleted or added
   * @param v     the second version compared to first
   * @return a new Chunkstate or the same one as us no change
   */
  ChunkStateSet next(Match p, ChunkState state, Witness v) {
    ChunkStateSet repl = this;
    if (!p.contains(v)) {
      if (!containsState(state)) {
        repl = new ChunkStateSet();
        repl.add(state);
      }
    }
    // contains version v
    else if (!isMerged()) {
      repl = new ChunkStateSet();
      repl.add(ChunkState.MERGED);
    }
    return repl;
  }

  /**
   * Get the states stored here
   *
   * @return an array of chunk states
   */
  ChunkState[] getStates() {
    return states;
  }

  /**
   * Are two sets of chunk states equal?
   *
   * @param other the other set of states
   * @return true if they are equal
   */
  public boolean equals(ChunkStateSet other) {
    if (this.states.length == other.states.length) {
      ChunkState[] s = states;
      for (int i = 0; i < s.length; i++)
        if (!other.containsState(s[i]))
          return false;
      return true;
    } else
      return false;
  }

  /**
   * Convert these states to a string for incorporation into a chunk
   *
   * @return the state set as a string
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    if (states != null) {
      for (int i = 0; i < states.length; i++) {
        sb.append(states[i].toString());
        if (i < states.length - 1)
          sb.append(",");
      }
    } else
      sb.append("empty");
    return sb.toString();
  }
}
