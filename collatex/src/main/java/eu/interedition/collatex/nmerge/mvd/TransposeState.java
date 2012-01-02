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
package eu.interedition.collatex.nmerge.mvd;

import com.google.common.base.Objects;

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
  private void assignId(Match<?> parent) {
    this.id = TransposeState.transposeId--;
    parent.setId(this.id);
    for (Match<?> child : parent.getChildren()) {
      child.setId(parent.getId());
    }
  }

  /**
   * Create a new TransposeState. Don't assume there will be
   * any transpositions
   */
  public TransposeState() {
    this(0, ChunkState.NONE);
  }

  public TransposeState(int id, ChunkState state) {
    this.id = id;
    this.state = state;
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
  public ChunkState getState() {
    return state;
  }

  public boolean isTransposed() {
    return (state != ChunkState.NONE);
  }

  /**
   * Using information contained in a supplied new pair,
   * compute a new transpose state.
   *
   * @param match the pair to shift states in version u
   * @param u     the first version, contained in match
   * @param v     the version we are comparing to
   * @return a new TransposeState or ourselves
   */
  TransposeState next(Match<?> match, Witness u, Witness v) {
    TransposeState next = this;

    if (match.isChild() && !match.contains(v) && match.getParent().contains(v) && !match.getParent().contains(u)) {
      if (match.getId() == 0) {
        assignId(match.getParent());
      }
      next = new TransposeState(match.getId(), ChunkState.CHILD);
    } else if (match.isParent() && !match.contains(v) && match.getChildInVersion(v) != null) {
      // if it has a child in v, it might be a repetition
      if (match.getId() == 0) {
        assignId(match);
      }
      next = new TransposeState(match.getId(), ChunkState.PARENT);
    } else if (state == ChunkState.PARENT || state == ChunkState.CHILD) {
      // or it was a child or parent but not any more
      next = new TransposeState();
    }

    return next;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
            .add("id", id)
            .add("state", state)
            .toString();
  }
}
