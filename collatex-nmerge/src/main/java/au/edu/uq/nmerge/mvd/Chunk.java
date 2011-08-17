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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Vector;

/**
 * Represent a piece of a version that has some characteristics,
 * e.g. a run of text not found in a previous or other version.
 * This could be displayed in another colour or highlighted as
 * the result of a search etc.
 *
 * @author Desmond Schmidt 19/9/07
 */
public class Chunk<T> extends BracketedData<T> {
  ChunkStateSet states;
  /**
   * after search say what version the Chunk belongs to
   */
  Witness version;
  /**
   * parent, child or merge id or 0, unique for each version
   */
  int id;
  /**
   * global chunk id
   */
  static int chunkId;

  /**
   * Basic constructor for a chunk. Add states versions and ids later.
   */
  public Chunk() {
    this.states = new ChunkStateSet();
  }

  /**
   * Create a new Chunk
   *
   * @param id       the parent or child id or 0
   * @param cs       an initial set of chunk states
   * @param data     the data to add
   */
  public Chunk(int id, ChunkState[] cs, List<T> data) {
    super(data);
    this.states = new ChunkStateSet(cs);
    this.id = id;
  }

  /**
   * Add a state to the set
   *
   * @param state the state to add
   */
  public void addState(ChunkState state) {
    if (state != ChunkState.NONE)
      states.add(state);
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
   * @param version the version the state belongs to
   */
  public void setVersion(Witness version) {
    this.version = version;
  }

  /**
   * Overlay a match onto an array of chunks. Return the modified
   * chunk array.
   *
   * @param hit    the match to overlay
   * @param chunks an array of chunks
   * @return an array of chunks with the match incorporated
   */
  public static Chunk[] overlay(Hit hit, Chunk[] chunks) {
    int begin = 0;
    int matchStart = hit.offset;
    int matchEnd = hit.offset + hit.length;
    Vector<Chunk> newChunks = new Vector<Chunk>();
    for (int i = 0; i < chunks.length; i++) {
      Chunk current = chunks[i];
      // 1. current overlaps match on the left
      if (matchStart < begin + current.getLength()
              && matchStart > begin) {
        Chunk[] parts = current.split(matchStart - begin);
        newChunks.add(parts[0]);
        begin += parts[0].getLength();
        current = parts[1];
      }
      // 2. current overlaps match on the right
      if (matchEnd < begin + current.getLength()
              && matchEnd > begin) {
        Chunk[] parts = current.split(matchEnd - begin);
        parts[0].addState(hit.state);
        newChunks.add(parts[0]);
        begin += parts[0].getLength();
        current = parts[1];
      }
      // 3. match completely overlaps current
      if (matchStart <= begin && matchEnd
              >= begin + current.getLength()) {
        current.addState(hit.state);
        current.version = hit.getVersion();
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
    Chunk[] array = new Chunk[newChunks.size()];
    return newChunks.toArray(array);
  }

  /**
   * Split a chunk into two halves at a single point
   *
   * @param offset the point within the chunk to split
   * @return an array of 2 chunks
   */
  Chunk[] split(int offset) {
    Chunk[] parts = new Chunk[2];
    // duplicate ids: this doesn't matter for chunks
    parts[0] = new Chunk(id, states.getStates(), Lists.newArrayList(realData.subList(0, offset)));
    parts[0].version = this.version;
    parts[1] = new Chunk(id, new ChunkStateSet(states).getStates(), Lists.newArrayList(realData.subList(offset, realData.size())));
    parts[1].version = this.version;
    return parts;
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
   * Get the length of the source data used to construct this
   * chunk
   *
   * @return the src data len in bytes
   */
  public int getSrcLen() {
    return srcLen;
  }

  /**
   * Get the version of this chunk.
   *
   * @return the version given when the chunk was created
   */
  public Witness getVersion() {
    return version;
  }

  /**
   * Get the length of a Chunk's actual data
   *
   * @return the chunk's length
   */
  public int getLength() {
    return (realData == null) ? 0 : realData.size();
  }

  /**
   * Convert a chunk into a string for debugging
   *
   * @return a String representation of the contents plus the state
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    {
      sb.append("[");
      if (states != null)
        sb.append(states.toString());
      if (id != 0)
        sb.append(":" + Integer.toString(id));
      if (version != null)
        sb.append(":" + version);
      sb.append(":");
      try {
        sb.append(Iterables.toString(realData));
      } catch (Exception e) {
        // this won't happen
      }
      sb.append("]");
    }
    return sb.toString();
  }
}
