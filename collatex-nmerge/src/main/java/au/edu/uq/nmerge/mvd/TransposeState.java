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

import java.util.ListIterator;

/**
 * When building chunks keep track of the state of transpositions
 *
 * @author Desmond Schmidt 23/5/09
 */
public class TransposeState {
  int id;
  ChunkState state;
  static int transposeId = Integer.MAX_VALUE;

  /**
   * Assign a fresh id to a parent and all its children. Pairs
   * don't intrinsically have ids after serialisation from arcs
   *
   * @param parent the parent pair
   */
  private void assignId(Match parent) {
    this.id = parent.id = TransposeState.transposeId--;
    ListIterator<Match> iter = parent.getChildIterator();
    while (iter.hasNext()) {
      Match child = iter.next();
      child.id = parent.id;
    }
  }

  /**
   * Create a new TransposeState. Don't assume there will be
   * any transpositions
   */
  public TransposeState() {
    state = ChunkState.NONE;
  }

  /**
   * Get the transpose id (parent or child)
   *
   * @return the id or 0 if none defined
   */
  public int getId() {
    return id;
  }

  /**
   * Get the chunk state - parent, child or none
   *
   * @return ChunkState.parent or ChunkStat.child or ChunkState.none
   */
  public ChunkState getChunkState() {
    return state;
  }

  /**
   * Using information contained in a supplied new pair,
   * compute a new transpose state.
   *
   * @param p   the pair to shift states in version u
   * @param old the old transpose state
   * @param u   the first version, contained in p
   * @param v   the version we are comparing to
   * @return a new TransposeState or ourselves
   */
  TransposeState next(Match p, short u, short v) {
    TransposeState repl = this;
    boolean wasTransposed = (state == ChunkState.PARENT
            || state == ChunkState.CHILD);
    if (p.isChild() && !p.contains(v)
            && p.parent.contains(v) && !p.parent.contains(u)) {
      if (p.id == 0)
        assignId(p.parent);
      repl = new TransposeState();
      repl.id = p.id;
      repl.state = ChunkState.CHILD;
    }
    // if it has a child in v, it might be a repetition
    else if (p.isParent() && !p.contains(v)
            && p.getChildInVersion(v) != null) {
      if (p.id == 0)
        assignId(p);
      repl = new TransposeState();
      repl.id = p.id;
      repl.state = ChunkState.PARENT;
    }
    // or it was a child or parent but not any more
    else if (wasTransposed)
      repl = new TransposeState();
    return repl;
  }

  /**
   * For debugging
   *
   * @return a string representation of this object
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("id=" + id + " state=" + state);
    return sb.toString();
  }

  /**
   * Is this a "clean" untransposed state?
   *
   * @return true if its chunkstate is none
   */
  public boolean isTransposed() {
    return state != ChunkState.NONE;
  }
}
