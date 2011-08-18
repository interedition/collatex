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

import au.edu.uq.nmerge.Tuple;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Represent a piece of a version that has some characteristics,
 * e.g. a run of text not found in a previous or other version.
 * This could be displayed in another colour or highlighted as
 * the result of a search etc.
 *
 * @author Desmond Schmidt 19/9/07
 */
public class Chunk<T> {
  /**
   * parent, child or merge id or 0, unique for each witness
   */
  private int id;

  /**
   * after search say what version the Chunk belongs to
   */
  private Witness witness;

  private ChunkStateSet states;

  /**
   * the data of the chunk
   */
  private List<T> tokens = Lists.newArrayList();


  /**
   * Basic constructor for a chunk. Add states versions and ids later.
   */
  public Chunk() {
    this.states = new ChunkStateSet();
  }

  /**
   * Create a new Chunk
   *
   * @param id     the parent or child id or 0
   * @param cs     an initial set of chunk states
   * @param tokens the data to add
   */
  public Chunk(int id, ChunkState[] cs, List<T> tokens) {
    this.id = id;
    this.states = new ChunkStateSet(cs);
    this.tokens = tokens;
  }

  /**
   * Add a state to the set
   *
   * @param state the state to add
   */
  public void addState(ChunkState state) {
    if (state != ChunkState.NONE) {
      states.add(state);
    }
  }

  /**
   * Get the states of a chunk
   *
   * @return an array of chunk states
   */
  public ChunkStateSet getStates() {
    return states;
  }

  /**
   * Set the version. Only valid if it is already a found state.
   *
   * @param witness the version the state belongs to
   */
  public void setWitness(Witness witness) {
    this.witness = witness;
  }

  /**
   * Overlay a match onto an array of chunks. Return the modified
   * chunk array.
   *
   * @param hit    the match to overlay
   * @param chunks an array of chunks
   * @return an array of chunks with the match incorporated
   */
  public static <T> List<Chunk<T>> overlay(Hit<T> hit, Chunk<T>[] chunks) {
    int begin = 0;
    int matchStart = hit.offset;
    int matchEnd = hit.offset + hit.length;
    List<Chunk<T>> newChunks = Lists.newArrayList();
    for (int i = 0; i < chunks.length; i++) {
      Chunk<T> current = chunks[i];
      // 1. current overlaps match on the left
      if (matchStart < begin + current.getLength()
              && matchStart > begin) {
        Tuple<Chunk<T>> parts = current.split(matchStart - begin);
        newChunks.add(parts.first);
        begin += parts.second.getLength();
        current = parts.second;
      }
      // 2. current overlaps match on the right
      if (matchEnd < begin + current.getLength()
              && matchEnd > begin) {
        Tuple<Chunk<T>> parts = current.split(matchEnd - begin);
        parts.first.addState(hit.state);
        newChunks.add(parts.first);
        begin += parts.first.getLength();
        current = parts.second;
      }
      // 3. match completely overlaps current
      if (matchStart <= begin && matchEnd
              >= begin + current.getLength()) {
        current.addState(hit.state);
        current.witness = hit.getVersion();
        begin += current.getLength();
        newChunks.add(current);
      }
      // 4. match doesn't overlap current at all
      else /*if ( matchEnd <= begin || matchStart
				> begin+current.getLength() )*/ {
        newChunks.add(current);
        begin += current.getLength();
      }
    }
    return newChunks;
  }

  /**
   * Split a chunk into two halves at a single point
   *
   * @param offset the point within the chunk to split
   * @return an array of 2 chunks
   */
  Tuple<Chunk<T>> split(int offset) {
    // duplicate ids: this doesn't matter for chunks
    final Chunk<T> left = new Chunk<T>(id, states.getStates(), Lists.newArrayList(tokens.subList(0, offset)));
    left.witness = this.witness;

    final Chunk<T> right = new Chunk<T>(id, new ChunkStateSet(states).getStates(), Lists.newArrayList(tokens.subList(offset, tokens.size())));
    right.witness = this.witness;

    return new Tuple<Chunk<T>>(left, right);
  }

  /**
   * Set the id.
   *
   * @param id the id of the state. If 0 no exception is raised
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Get the id of this parent/child (or neither)
   *
   * @return an int parent/child id
   */
  public int getId() {
    return id;
  }

  /**
   * Get the version of this chunk.
   *
   * @return the version given when the chunk was created
   */
  public Witness getWitness() {
    return witness;
  }

  /**
   * Get the length of a Chunk's actual data
   *
   * @return the chunk's length
   */
  public int getLength() {
    return (tokens == null) ? 0 : tokens.size();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
            .add("id", id)
            .add("states", states)
            .add("witness", witness)
            .addValue(tokens)
            .toString();
  }

  public void add(List<T> tokens) {
    this.tokens.addAll(tokens);
  }
}
